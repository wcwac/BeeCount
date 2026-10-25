package io.github.wcwac.beecount.data

import io.github.wcwac.beecount.domain.*
import java.security.MessageDigest

internal class FinancialEngine(private val repo: NativeRepository) {
    private val dao get() = repo.dao

    suspend fun execute(command: FinancialCommand): CommitReceipt {
        require(command.operationId.isNotBlank() && command.operationId.length <= 128) { "保存标识无效" }
        val fingerprint = hash(command.toString())
        dao.receipt(command.operationId)?.let {
            require(it.fingerprint == fingerprint) { "相同保存标识不能用于不同内容" }
            return CommitReceipt(it.operationId, it.entryId, it.revision)
        }
        val entry = when (command) {
            is FinancialCommand.Save -> save(command)
            is FinancialCommand.Delete -> setDeleted(command.id, command.expectedRevision, true)
            is FinancialCommand.Restore -> setDeleted(command.id, command.expectedRevision, false)
            is FinancialCommand.Purge -> purge(command.id, command.expectedRevision)
            is FinancialCommand.MarkReimbursement -> markReimbursement(command)
        }
        val receipt = ReceiptEntity(command.operationId, fingerprint, entry.id, entry.revision)
        dao.putReceipt(receipt)
        return CommitReceipt(receipt.operationId, receipt.entryId, receipt.revision)
    }

    private suspend fun save(command: FinancialCommand.Save): EntryEntity {
        val candidate = command.candidateId?.let { requireNotNull(dao.candidate(it)) { "待确认记录不存在" } }
        if (candidate?.state == "SAVED") {
            return requireNotNull(candidate.entryId?.let { dao.entry(it) }) { "该采集记录已处理，原账单已删除" }
        }
        if (candidate != null) {
            require(candidate.state == "PENDING") { "采集记录已处理" }
            require(candidate.revision == command.candidateRevision) { "采集内容已更新，请重新确认" }
        }
        val old = dao.entry(command.draft.id)
        require(candidate == null || old == null) { "待确认记录应保存为新账单，不能覆盖已有账单" }
        require(old != null || dao.receiptEntryCount(command.draft.id) == 0) { "该记录标识已经使用过，请新建账单" }
        require(old == null && command.expectedRevision == null || old != null && old.revision == command.expectedRevision) { "账单已变化，请重新打开" }
        require(old?.deleted != true) { "请先恢复账单" }
        require(old == null || old.kind == command.draft.kind.name) { "编辑不能改变金融记录类型" }
        val validated = validate(command.draft, old)
        if (old != null) {
            val children = dao.children(old.id)
            if (children.isNotEmpty()) {
                require(old.bookId == validated.bookId && old.currency == validated.amount.currency.value && old.kind == validated.kind.name) { "已有关联记录，不能更换账本或类型" }
                require(old.accountId == validated.accountId && old.otherAccountId == validated.otherAccountId) { "已有关联记录，不能更换账户" }
                require(old.categoryId == validated.categoryId && old.excludedFromTotals == validated.excludedFromTotals && old.excludedFromBudget == validated.excludedFromBudget) { "已有关联记录，不能更改分类或统计标记" }
                val returned = children.filter { it.kind in childKinds }.fold(0L) { total, child -> Math.addExact(total, child.amount) }
                require(validated.amount.minor >= returned) { "金额不能小于已退或已结清金额" }
            }
            if (dao.instalments(old.id).isNotEmpty()) require(old.amount == validated.amount.minor && old.accountId == validated.accountId && old.bookId == validated.bookId && old.currency == validated.amount.currency.value) { "请先处理分期计划再修改本金、账本或账户" }
            if (dao.instalmentForPayment(old.id) != null) require(old.amount == validated.amount.minor && old.feeMinor == validated.feeMinor && old.otherAccountId == validated.otherAccountId && old.parentId == validated.parentId && old.bookId == validated.bookId) { "分期还款的金额与原消费由计划确定" }
        }
        val entity = validated.entity((old?.revision ?: 0) + 1)
        dao.putEntry(entity)
        replacePostings(entity)
        dao.deleteEntryTags(entity.id)
        dao.putEntryTags(validated.tagIds.distinct().map { EntryTagEntity(entity.id, it) })
        dao.deleteEntryAttachments(entity.id)
        dao.putEntryAttachments(validated.attachmentIds.distinct().map { EntryAttachmentEntity(entity.id, it) })
        if (candidate != null) dao.putCandidate(candidate.copy(state = "SAVED", entryId = entity.id, pendingWindow = null, revision = candidate.revision + 1))
        return entity
    }

    internal suspend fun validate(input: EntryDraft, old: EntryEntity? = null, allowArchived: Boolean = false): EntryDraft {
        repo.validId(input.id)
        val book = requireNotNull(dao.book(input.bookId)) { "账本不存在" }
        require(allowArchived || !book.archived) { "请先恢复账本" }
        require(input.kind in setOf(EntryKind.EXPENSE, EntryKind.INCOME, EntryKind.REFUND, EntryKind.OPENING, EntryKind.ADJUSTMENT) || book.currency == input.amount.currency.value) { "此类金融记录币种必须与账本一致" }
        require(input.occurredAt in 1..8_640_000_000_000_000L) { "记账日期无效" }
        require(input.merchant.length <= 500 && input.note.length <= 4000) { "商户或备注过长" }
        val signed = input.kind in setOf(EntryKind.OPENING, EntryKind.ADJUSTMENT)
        checkedMoney(input.amount.minor, signed)
        require(input.feeMinor in 0..MAX_MONEY && input.interestMinor in 0..MAX_MONEY) { "费用或利息金额无效" }
        Math.addExact(Math.addExact(input.amount.minor, input.feeMinor), input.interestMinor)
        require(input.interestMinor == 0L || input.kind in setOf(EntryKind.COLLECT, EntryKind.REPAY)) { "此类记录不支持利息" }
        require(!signed || input.feeMinor == 0L && input.interestMinor == 0L) { "期初或调账不能包含费用" }
        suspend fun account(id: String?): AccountEntity? {
            if (id == null) return null
            val account = requireNotNull(dao.account(id)) { "账户不存在" }
            require(allowArchived || !account.archived) { "请先恢复账户" }
            require(account.currency == input.amount.currency.value) { "账户和记录必须使用同一币种" }
            return account
        }
        val from = account(input.accountId)
        val to = account(input.otherAccountId)
        if (input.kind in setOf(EntryKind.EXPENSE, EntryKind.INCOME, EntryKind.OPENING, EntryKind.ADJUSTMENT)) require(to == null) { "此类记录只能选择一个账户" }
        require(from == null || to == null || from.id != to.id) { "请选择两个不同账户" }
        val two = input.kind in setOf(EntryKind.TRANSFER, EntryKind.CREDIT_PAYMENT, EntryKind.LEND, EntryKind.BORROW, EntryKind.REIMBURSEMENT)
        require(!two || from != null && to != null) { "请选择资金账户和目标账户" }
        require(!signed || from != null) { "调账必须选择账户" }
        if (input.kind == EntryKind.CREDIT_PAYMENT) require(to?.kind == AccountKind.CREDIT.name) { "还款目标必须是信用账户" }
        if (input.kind in setOf(EntryKind.LEND, EntryKind.REIMBURSEMENT)) require(to?.kind == AccountKind.RECEIVABLE.name) { "请选择应收账户" }
        if (input.kind == EntryKind.BORROW) require(to?.kind == AccountKind.PAYABLE.name) { "请选择应付账户" }
        if (from != null && !signed) require(from.kind !in setOf(AccountKind.RECEIVABLE.name, AccountKind.PAYABLE.name)) { "请选择实际收付款账户" }
        val category = input.categoryId?.let { requireNotNull(dao.category(it)) { "分类不存在" } }
        if (category != null) {
            require(allowArchived || !category.archived || old?.categoryId == category.id) { "分类已归档" }
            if (input.kind == EntryKind.INCOME) require(category.income) { "请选择收入分类" }
            if (input.kind in setOf(EntryKind.EXPENSE, EntryKind.REIMBURSEMENT)) require(!category.income) { "请选择支出分类" }
        }
        require(input.tagIds.size <= 50 && input.attachmentIds.size <= 100) { "标签或附件过多" }
        input.tagIds.forEach { requireNotNull(dao.tag(it)) { "标签不存在" } }
        input.attachmentIds.forEach { requireNotNull(dao.attachment(it)) { "附件不存在" } }
        var result = input
        if (input.kind.name in childKinds) {
            val parent = requireNotNull(input.parentId?.let { dao.entry(it) }) { "请选择原始消费或借贷记录" }
            require(!parent.deleted && parent.id != input.id && parent.bookId == book.id && parent.currency == input.amount.currency.value) { "原记录不存在、已删除或币种不同" }
            val expected = when (input.kind) { EntryKind.REFUND -> "EXPENSE"; EntryKind.COLLECT -> "LEND"; EntryKind.REPAY -> "BORROW"; else -> "REIMBURSEMENT" }
            require(parent.kind == expected) { "原记录类型不匹配" }
            val already = dao.children(parent.id).filter { it.id != input.id && it.kind in childKinds }.fold(0L) { sum, item -> Math.addExact(sum, item.amount) }
            require(Math.addExact(already, input.amount.minor) <= parent.amount) { "金额超过尚未退款或结清的本金" }
            require(from != null) { "请选择本次收付款账户" }
            if (input.kind != EntryKind.REFUND) require(parent.otherAccountId?.let { dao.account(it) }?.let { (allowArchived || !it.archived) && it.currency == book.currency } == true) { "原记录的应收或应付账户不可用" }
            if (input.kind == EntryKind.REFUND) require(dao.instalments(parent.id).isEmpty()) { "请先取消尚未还款的分期计划；已有还款的消费暂不支持退款" }
            result = input.copy(otherAccountId = if (input.kind == EntryKind.REFUND) null else parent.otherAccountId,
                categoryId = if (input.kind in setOf(EntryKind.REFUND, EntryKind.REIMBURSEMENT_PAYMENT)) parent.categoryId else input.categoryId,
                excludedFromTotals = if (input.kind in setOf(EntryKind.REFUND, EntryKind.REIMBURSEMENT_PAYMENT)) parent.excludedFromTotals else input.excludedFromTotals,
                excludedFromBudget = if (input.kind in setOf(EntryKind.REFUND, EntryKind.REIMBURSEMENT_PAYMENT)) parent.excludedFromBudget else input.excludedFromBudget)
        } else {
            require(input.parentId == null || input.kind == EntryKind.CREDIT_PAYMENT) { "此记录不能关联父账单" }
            if (input.parentId != null) {
                val parent = requireNotNull(dao.entry(requireNotNull(input.parentId))) { "原消费不存在" }
                require(!parent.deleted && parent.kind == "EXPENSE" && parent.bookId == book.id && parent.accountId == input.otherAccountId) { "分期还款与原消费不匹配" }
                val paid = dao.children(parent.id).filter { it.kind == "CREDIT_PAYMENT" && it.id != input.id }.fold(0L) { sum, child -> Math.addExact(sum, child.amount) }
                require(Math.addExact(paid, input.amount.minor) <= parent.amount) { "分期还款本金超过原消费" }
            }
        }
        return result
    }

    private suspend fun setDeleted(id: String, revision: Long, deleted: Boolean): EntryEntity {
        val old = requireNotNull(dao.entry(id)) { "账单不存在" }
        require(old.revision == revision) { "账单已变化，请重新打开" }
        if (old.deleted == deleted) return old
        require(dao.children(id).isEmpty()) { "请先处理关联的退款或结清记录" }
        require(dao.instalments(id).isEmpty()) { "请先删除未还款分期计划；已有还款计划请保留消费记录" }
        val value = if (!deleted) {
            if (old.kind == "CREDIT_PAYMENT" && old.parentId != null) require(dao.instalmentForPayment(id) != null) { "该期已由另一笔还款处理，不能恢复旧记录" }
            validate(old.draft(dao.entryTags(id), dao.entryAttachments(id)), old).entity(old.revision + 1)
        } else old.copy(deleted = true, revision = old.revision + 1)
        dao.putEntry(value)
        replacePostings(value)
        return value
    }

    private suspend fun purge(id: String, revision: Long): EntryEntity {
        val old = requireNotNull(dao.entry(id)) { "账单不存在" }
        require(old.revision == revision && old.deleted) { "只能清除回收站内未变化的记录" }
        require(dao.childCount(id) == 0 && dao.instalments(id).isEmpty() && dao.instalmentForPayment(id) == null) { "该记录仍有关联，请先处理关联记录" }
        dao.deleteEntry(id)
        // Candidate and operation receipts intentionally retain this never-reused UUID.
        return old.copy(revision = old.revision + 1)
    }

    private suspend fun markReimbursement(command: FinancialCommand.MarkReimbursement): EntryEntity {
        val old = requireNotNull(dao.entry(command.id)) { "账单不存在" }
        require(old.revision == command.expectedRevision && !old.deleted && old.kind == "EXPENSE") { "请选择未删除且未变化的普通支出" }
        require(dao.children(old.id).isEmpty() && dao.instalments(old.id).isEmpty()) { "关联退款或分期的消费不能标为报销" }
        val draft = validate(old.draft(dao.entryTags(old.id), dao.entryAttachments(old.id)).copy(kind = EntryKind.REIMBURSEMENT, otherAccountId = command.receivableAccountId), old)
        val result = draft.entity(old.revision + 1)
        dao.putEntry(result)
        replacePostings(result)
        return result
    }

    internal suspend fun replacePostings(entry: EntryEntity) {
        dao.deletePostings(entry.id)
        if (entry.deleted) return
        val amounts = calculatePostings(entry)
        amounts.forEach { (account, delta) -> Math.addExact(dao.balance(account), delta) }
        dao.putPostings(amounts.map { AccountEntryEntity(entry.id, it.key, it.value) })
    }

    internal fun calculatePostings(entry: EntryEntity): Map<String, Long> {
        if (entry.deleted) return emptyMap()
        val amounts = linkedMapOf<String, Long>()
        fun add(account: String?, amount: Long) { if (account != null) amounts[account] = Math.addExact(amounts[account] ?: 0, amount) }
        val amount = entry.amount
        val outgoing = Math.negateExact(Math.addExact(amount, entry.feeMinor))
        when (EntryKind.valueOf(entry.kind)) {
            EntryKind.EXPENSE -> add(entry.accountId, outgoing)
            EntryKind.INCOME -> add(entry.accountId, Math.subtractExact(amount, entry.feeMinor))
            EntryKind.TRANSFER, EntryKind.CREDIT_PAYMENT, EntryKind.LEND, EntryKind.REIMBURSEMENT -> { add(entry.accountId, outgoing); add(entry.otherAccountId, amount) }
            EntryKind.BORROW -> { add(entry.accountId, Math.subtractExact(amount, entry.feeMinor)); add(entry.otherAccountId, -amount) }
            EntryKind.COLLECT -> { add(entry.accountId, Math.subtractExact(Math.addExact(amount, entry.interestMinor), entry.feeMinor)); add(entry.otherAccountId, -amount) }
            EntryKind.REPAY -> { add(entry.accountId, Math.subtractExact(outgoing, entry.interestMinor)); add(entry.otherAccountId, amount) }
            EntryKind.REFUND -> add(entry.accountId, Math.subtractExact(amount, entry.feeMinor))
            EntryKind.REIMBURSEMENT_PAYMENT -> { add(entry.accountId, Math.subtractExact(amount, entry.feeMinor)); add(entry.otherAccountId, -amount) }
            EntryKind.OPENING, EntryKind.ADJUSTMENT -> add(entry.accountId, amount)
        }
        return amounts
    }

    companion object {
        const val MAX_MONEY = Money.MAX_INPUT_MINOR
        val childKinds = setOf("REFUND", "COLLECT", "REPAY", "REIMBURSEMENT_PAYMENT")
        fun checkedMoney(value: Long, signed: Boolean = false) { require(value in -MAX_MONEY..MAX_MONEY && if (signed) value != 0L else value > 0L) { "金额必须在有效范围内" } }
        fun hash(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it.toInt() and 255) }
    }
}
