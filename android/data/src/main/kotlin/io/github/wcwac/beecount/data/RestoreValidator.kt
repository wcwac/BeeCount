package io.github.wcwac.beecount.data

import android.database.Cursor
import io.github.wcwac.beecount.domain.*

/** Runs against the isolated staging database before the application replaces live data. */
internal class RestoreValidator(private val repo: NativeRepository) {
    private val dao get() = repo.dao
    private val sql get() = repo.db.openHelper.readableDatabase

    suspend fun validate() {
        sql.query("PRAGMA foreign_key_check").use { require(!it.moveToFirst()) { "备份关联数据损坏" } }
        var activeBooks = 0
        scan("books") { row -> id(row, "id"); repo.name(text(row, "name")); CurrencyCode(text(row, "currency")); if (!boolean(row, "archived")) activeBooks++ }
        require(activeBooks > 0) { "备份必须包含可用账本" }
        scan("accounts") { row ->
            id(row, "id"); repo.name(text(row, "name")); CurrencyCode(text(row, "currency")); AccountKind.valueOf(text(row, "kind")); boolean(row, "archived")
            require(integer(row, "statementDay") in 1..31 && integer(row, "repaymentDay") in 1..31) { "备份信用日期无效" }
        }
        scan("categories") { row ->
            id(row, "id"); repo.name(text(row, "name")); boolean(row, "income"); boolean(row, "archived")
            nullable(row, "parentId")?.let { parentId ->
                val parent = requireNotNull(dao.category(parentId)) { "备份上级分类不存在" }
                require(parentId != text(row, "id") && parent.parentId == null && parent.income == boolean(row, "income")) { "备份分类层级无效" }
            }
        }
        scan("tags") { row -> id(row, "id"); repo.name(text(row, "name")) }
        scan("attachments") { row ->
            id(row, "id"); require(text(row, "name").length in 1..200 && text(row, "mime").length in 1..100 && integer(row, "size") in 0..16L*1024*1024) { "备份附件元信息无效" }
            require(text(row, "localPath").isNotBlank() && text(row, "localPath").length <= 1024) { "备份附件路径无效" }
        }
        scan("entries") { row -> boolean(row, "deleted"); boolean(row, "excludedFromTotals"); boolean(row, "excludedFromBudget") }
        var after = ""
        while (true) {
            val page = dao.entriesPage(after, 64)
            if (page.isEmpty()) break
            for (entry in page) {
                repo.validId(entry.id); val kind = EntryKind.valueOf(entry.kind); CurrencyCode(entry.currency)
                FinancialEngine.checkedMoney(entry.amount, kind == EntryKind.OPENING || kind == EntryKind.ADJUSTMENT)
                require(entry.revision > 0 && entry.occurredAt in 1..8_640_000_000_000_000L && entry.feeMinor in 0..FinancialEngine.MAX_MONEY && entry.interestMinor in 0..FinancialEngine.MAX_MONEY && entry.merchant.length <= 500 && entry.note.length <= 4000) { "备份账单数值无效" }
                val book = requireNotNull(dao.book(entry.bookId)) { "备份账本不存在" }
                require(kind in setOf(EntryKind.EXPENSE, EntryKind.INCOME, EntryKind.REFUND, EntryKind.OPENING, EntryKind.ADJUSTMENT) || book.currency == entry.currency) { "备份金融记录币种不匹配" }
                listOfNotNull(entry.accountId, entry.otherAccountId).forEach { require(dao.account(it)?.currency == entry.currency) { "备份账户币种不匹配" } }
                entry.parentId?.let { require(dao.entry(it) != null && it != entry.id) { "备份原记录关联无效" } }
                if (!entry.deleted) {
                    val draft = entry.draft(dao.entryTags(entry.id), dao.entryAttachments(entry.id))
                    val canonical = repo.engine.validate(draft, entry, allowArchived = true)
                    require(canonical == draft) { "备份关联账单与原记录不一致" }
                }
                val expected = repo.engine.calculatePostings(entry)
                val actual = dao.postings(entry.id).associate { it.accountId to it.amount }
                require(expected == actual) { "备份账户变动与账单不一致" }
            }
            after = page.last().id
        }
        // SQLite throws on an overflowing SUM instead of accepting a corrupted balance.
        scan("accounts") { row -> dao.balance(text(row, "id")) }
        scan("candidates") { row ->
            id(row, "id"); EntryKind.valueOf(text(row, "kind")); CurrencyCode(text(row, "currency")); FinancialEngine.checkedMoney(integer(row, "amount"))
            boolean(row, "visibleTransactionTime"); boolean(row, "requiresConfirmation")
            require(integer(row, "occurredAt") in 1..8_640_000_000_000_000L && integer(row, "feeMinor") in 0..FinancialEngine.MAX_MONEY && integer(row, "discountMinor") in 0..FinancialEngine.MAX_MONEY && text(row, "description").length <= 4000 && text(row, "merchant").length <= 500) { "备份候选金额或文字无效" }
            val state = CandidateState.valueOf(text(row, "state")); require(integer(row, "revision") > 0) { "备份候选版本无效" }
            text(row, "reasons").split('|').filter { it.isNotEmpty() }.forEach { ConfirmationReason.valueOf(it) }
            nullable(row, "bookId")?.let { require(dao.book(it) != null) { "备份采集账本不存在" } }
            nullable(row, "accountId")?.let { require(dao.account(it) != null) { "备份采集账户不存在" } }
            nullable(row, "otherAccountId")?.let { require(dao.account(it) != null) { "备份采集目标账户不存在" } }
            nullable(row, "categoryId")?.let { require(dao.category(it) != null) { "备份采集分类不存在" } }
            if (state != CandidateState.PENDING) require(nullable(row, "pendingWindow") == null) { "已处理候选不能占用待确认窗口" }
            if (state == CandidateState.SAVED) require(!nullable(row, "entryId").isNullOrBlank()) { "已保存候选缺少入账回执" }
        }
        scan("budgets") { row ->
            id(row, "id"); require(dao.book(text(row, "bookId")) != null) { "预算账本不存在" }; repo.name(text(row, "name")); FinancialEngine.checkedMoney(integer(row, "amount")); require(integer(row, "start") >= 0 && integer(row, "endExclusive") > integer(row, "start")) { "预算日期无效" }
            nullable(row, "categoryId")?.let { require(dao.category(it)?.income == false) { "预算分类无效" } }
        }
        scan("goals") { row ->
            id(row, "id"); val book = requireNotNull(dao.book(text(row, "bookId"))) { "目标账本不存在" }; repo.name(text(row, "name")); FinancialEngine.checkedMoney(integer(row, "target")); require(integer(row, "progress") in 0..FinancialEngine.MAX_MONEY) { "目标进度无效" }
            val mode = text(row, "mode"); require(mode in setOf("manual", "account", "income", "balance")) { "目标模式无效" }
            if (mode == "account") require(nullable(row, "accountId")?.let { dao.account(it) }?.currency == book.currency) { "目标账户币种无效" }
        }
        scan("recurring_rules") { row ->
            id(row, "id"); val rule = requireNotNull(dao.recurring(text(row, "id")))
            boolean(row, "enabled"); boolean(row, "template_deleted"); boolean(row, "template_excludedFromTotals"); boolean(row, "template_excludedFromBudget")
            require(rule.interval in 1..365 && rule.unit in setOf("DAY", "WEEK", "MONTH", "YEAR") && rule.nextAt > 0 && rule.executed >= 0 && rule.anchorDay in 1..31 && (rule.maxCount == null || rule.maxCount > 0 && rule.executed <= rule.maxCount)) { "周期规则无效" }
            require(rule.template.kind in setOf("EXPENSE", "INCOME", "TRANSFER", "CREDIT_PAYMENT")) { "周期账单类型无效" }
            repo.engine.validate(rule.template.draft(splitIds(rule.tagIds), splitIds(rule.attachmentIds)), rule.template, allowArchived = true)
        }
        scan("candidate_templates") { row ->
            boolean(row, "template_deleted"); boolean(row, "template_excludedFromTotals"); boolean(row, "template_excludedFromBudget")
            val candidate = requireNotNull(dao.candidate(text(row, "candidateId"))) { "周期候选不存在" }
            val template = requireNotNull(dao.candidateTemplate(candidate.id))
            require(template.template.bookId == candidate.bookId && template.template.amount == candidate.amount && template.template.currency == candidate.currency) { "周期候选内容不一致" }
            // Processed candidate templates may retain attachment IDs later removed from the bill.
            if (candidate.state == "PENDING") repo.engine.validate(template.template.draft(splitIds(template.tagIds), splitIds(template.attachmentIds)), template.template, allowArchived = true)
        }
        sql.query("SELECT paymentId FROM instalments WHERE paymentId IS NOT NULL GROUP BY paymentId HAVING COUNT(*)>1 LIMIT 1").use { rows ->
            require(!rows.moveToFirst()) { "一笔分期还款不能同时结清多个期次" }
        }
        scan("instalments") { row ->
            id(row, "id"); val parent = requireNotNull(dao.entry(text(row, "entryId"))) { "分期消费不存在" }
            require(parent.kind == "EXPENSE" && !parent.deleted && parent.currency == dao.book(parent.bookId)?.currency && parent.accountId?.let { dao.account(it) }?.kind == "CREDIT") { "分期原消费无效" }
            require(integer(row, "sequence") in 1..120 && integer(row, "principal") > 0 && integer(row, "fee") in 0..FinancialEngine.MAX_MONEY && integer(row, "dueAt") > 0) { "分期数值无效" }
            nullable(row, "paymentId")?.let { paymentId ->
                val payment = requireNotNull(dao.entry(paymentId)) { "分期还款不存在" }
                require(payment.kind == "CREDIT_PAYMENT" && payment.parentId == parent.id && payment.bookId == parent.bookId && payment.currency == parent.currency && payment.otherAccountId == parent.accountId && payment.amount == integer(row, "principal") && payment.feeMinor == integer(row, "fee")) { "分期还款与原消费、金额或账户不符" }
            }
        }
        sql.query("SELECT p.entryId,SUM(p.principal) amount,e.amount original,COUNT(*) n,MAX(p.sequence) maxSequence,SUM(p.fee) totalFee FROM instalments p JOIN entries e ON e.id=p.entryId GROUP BY p.entryId").use { rows ->
            while (rows.moveToNext()) require(rows.getLong(1) == rows.getLong(2) && rows.getLong(3) == rows.getLong(4) && rows.getLong(5) in 0..FinancialEngine.MAX_MONEY) { "分期本金、期数或总手续费无效" }
        }
        sql.query("SELECT e.id FROM entries e WHERE e.kind='CREDIT_PAYMENT' AND e.parentId IS NOT NULL AND e.deleted=0 AND NOT EXISTS(SELECT 1 FROM instalments p WHERE p.paymentId=e.id) LIMIT 1").use { rows ->
            require(!rows.moveToFirst()) { "有效分期还款缺少对应期次，不能恢复为未还计划" }
        }
        scan("investments") { row ->
            id(row, "id"); repo.name(text(row, "name")); require(dao.book(text(row, "bookId"))?.currency == text(row, "currency")) { "投资币种无效" }
            repo.validateInvestment(text(row, "id"))
        }
        scan("investment_events") { row -> id(row, "id"); require(integer(row, "occurredAt") > 0) { "投资记录日期无效" } }
        scan("merchant_rules") { row ->
            id(row, "id"); require(text(row, "merchant").isNotBlank() || text(row, "packageName").isNotBlank() || text(row, "keyword").isNotBlank()) { "商户规则缺少条件" }
            nullable(row, "categoryId")?.let { require(dao.category(it) != null) { "规则分类不存在" } }; nullable(row, "accountId")?.let { require(dao.account(it) != null) { "规则账户不存在" } }
        }
        scan("account_mappings") { row -> id(row, "id"); require(text(row, "packageName").isNotBlank() && dao.account(text(row, "accountId")) != null) { "付款方式映射无效" } }
        scan("period_notes") { row -> require(dao.book(text(row, "bookId")) != null && integer(row, "endExclusive") > integer(row, "start") && text(row, "note").length <= 10000) { "本期总结无效" } }
        scan("operation_receipts") { row -> require(text(row, "operationId").length in 1..128 && text(row, "fingerprint").matches(Regex("[a-f0-9]{64}")) && text(row, "entryId").isNotBlank() && integer(row, "revision") > 0) { "操作回执无效" } }
    }

    private suspend fun scan(table: String, block: suspend (Cursor) -> Unit) { sql.query("SELECT * FROM $table").use { rows -> while (rows.moveToNext()) block(rows) } }
    private fun text(row: Cursor, name: String): String { val i = row.getColumnIndexOrThrow(name); require(row.getType(i) == Cursor.FIELD_TYPE_STRING) { "备份文字字段类型无效：$name" }; return row.getString(i) }
    private fun nullable(row: Cursor, name: String): String? = if (row.isNull(row.getColumnIndexOrThrow(name))) null else text(row, name)
    private fun integer(row: Cursor, name: String): Long { val i = row.getColumnIndexOrThrow(name); require(row.getType(i) == Cursor.FIELD_TYPE_INTEGER) { "备份数值字段类型无效：$name" }; return row.getLong(i) }
    private fun boolean(row: Cursor, name: String): Boolean { val value = integer(row, name); require(value in 0..1) { "备份开关字段无效" }; return value == 1L }
    private fun id(row: Cursor, name: String) { repo.validId(text(row, name)) }
}
