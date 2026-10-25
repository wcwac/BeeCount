package io.github.wcwac.beecount.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.wcwac.beecount.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class NativeRepository(val db: NativeDatabase, private val configProvider: () -> CaptureConfig = { CaptureConfig() }, private val generationProvider: () -> Long = { 0L }) : FinancialWriter, CaptureInbox {
    val dao: NativeDao = db.dao()
    /** Shared with streaming backup/restore. No public mutator may bypass this lock. */
    val maintenanceMutex = Mutex()
    internal val engine = FinancialEngine(this)
    internal suspend fun <T> mutate(block: suspend () -> T): T {
        val generation = generationProvider()
        return maintenanceMutex.withLock {
            require(generation == generationProvider()) { "数据已恢复，请重新打开当前页面" }
            db.withTransaction { block() }
        }
    }

    suspend fun initialize(): Book = mutate {
        val books = dao.books()
        books.firstOrNull { !it.archived }?.domain() ?: run {
            val book = BookEntity(newId(), "日常账本", "CNY")
            dao.putBook(book)
            if (books.isEmpty()) {
                dao.putAccount(AccountEntity(newId(), "现金", "CNY", "CASH"))
                listOf("餐饮", "购物", "交通", "居家", "娱乐", "医疗", "其他支出").forEach { dao.putCategory(CategoryEntity(newId(), it)) }
                listOf("工资", "奖金", "其他收入").forEach { dao.putCategory(CategoryEntity(newId(), it, true)) }
            }
            book.domain()
        }
    }
    suspend fun validateForRestore() = mutate { RestoreValidator(this).validate() }
    internal suspend fun validateInvestment(id: String) { investmentPosition(requireNotNull(dao.investment(id))) }

    fun observeBooks(): Flow<List<Book>> = dao.observeBooks().map { rows -> rows.map { it.domain() } }
    fun observeAccounts(): Flow<List<Account>> = dao.observeAccounts().map { rows -> rows.map { it.domain() } }
    fun observeCategories(): Flow<List<Category>> = dao.observeCategories().map { rows -> rows.map { it.domain() } }
    fun observeTags(): Flow<List<Tag>> = dao.observeTags().map { rows -> rows.map { Tag(it.id, it.name) } }
    /** Room emits table invalidations even when these bounded existence probes are unchanged. */
    fun observeWidgetChanges(): Flow<Unit> = dao.observeWidgetChanges().map { Unit }
    fun observeEntry(id: String): Flow<LedgerEntry?> = dao.observeEntry(id).map { it?.domain() }
    suspend fun entry(id: String): LedgerEntry? = dao.entryRow(id)?.domain()
    fun history(filter: HistoryFilter): Flow<PagingData<LedgerEntry>> {
        val where = mutableListOf("e.deleted=?")
        val args = mutableListOf<Any>(if (filter.deleted) 1 else 0)
        fun condition(sql: String, value: Any?) { if (value != null) { where += sql; args += value } }
        condition("e.bookId=?", filter.bookId)
        filter.accountId?.let { account -> where += "(e.accountId=? OR e.otherAccountId=?)"; args += account; args += account }
        condition("e.occurredAt>=?", filter.start); condition("e.occurredAt<?", filter.endExclusive)
        condition("e.categoryId=?", filter.categoryId)
        if (filter.attachmentsOnly) where += "EXISTS(SELECT 1 FROM entry_attachments x WHERE x.entryId=e.id)"
        if (filter.kinds.isNotEmpty()) { where += "e.kind IN (${filter.kinds.joinToString(",") { "?" }})"; filter.kinds.forEach { args += it.name } }
        filter.budgetId?.let { budget ->
            where += "e.excludedFromBudget=0 AND (${ReportSql.EXPENSE})<>0 AND EXISTS(SELECT 1 FROM budgets b JOIN books bk ON bk.id=b.bookId WHERE b.id=? AND b.bookId=e.bookId AND e.currency=bk.currency AND e.occurredAt>=b.start AND e.occurredAt<b.endExclusive AND (b.categoryId IS NULL OR b.categoryId=e.categoryId OR e.categoryId IN(SELECT id FROM categories WHERE parentId=b.categoryId)))"
            args += budget
        }
        if (filter.query.isNotBlank()) {
            where += "(e.merchant LIKE ? ESCAPE '\\' OR e.note LIKE ? ESCAPE '\\' OR c.name LIKE ? ESCAPE '\\')"
            val q = "%${filter.query.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")}%"
            repeat(3) { args += q }
        }
        val sql = EntrySql.SELECT + " WHERE ${where.joinToString(" AND ")} ORDER BY e.occurredAt DESC,e.id DESC"
        return Pager(PagingConfig(pageSize = 50, initialLoadSize = 50, enablePlaceholders = false, maxSize = 200)) { dao.history(SimpleSQLiteQuery(sql, args.toTypedArray())) }.flow.map { page -> page.map { it.domain() } }
    }
    fun observeSummary(bookId: String, start: Long, endExclusive: Long): Flow<Summary> = dao.observeSummary(bookId, start, endExclusive).map { Summary(it.income, it.expense) }
    fun observeChart(bookId: String, start: Long, endExclusive: Long): Flow<List<ChartPoint>> = dao.observeChart(bookId, start, endExclusive).map { rows -> rows.map { ChartPoint(it.label, it.income, it.expense) } }
    fun observeCategoryTotals(bookId: String, start: Long, endExclusive: Long): Flow<List<CategoryTotal>> = dao.observeCategoryTotals(bookId, start, endExclusive).map { rows -> rows.map { CategoryTotal(it.categoryId, it.label, it.amount) } }

    override suspend fun execute(command: FinancialCommand): CommitResult = try {
        CommitResult.Success(mutate {
            if (command is FinancialCommand.Save) require(command.expectedGeneration == null || command.expectedGeneration == generationProvider()) { "采集会话已失效，请重新打开付款页面" }
            engine.execute(command)
        })
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (error: IllegalArgumentException) { CommitResult.Rejected(error.message ?: "输入内容无效") }
    catch (error: ArithmeticException) { CommitResult.Rejected("金额计算超出允许范围，未保存任何变更") }
    catch (error: android.database.SQLException) { CommitResult.Rejected("保存未完成，所有变更已撤销，请重试") }

    suspend fun saveBook(value: Book) = mutate {
        validId(value.id); name(value.name); CurrencyCode(value.currency)
        val old = dao.book(value.id)
        require(!value.archived || dao.books().any { it.id != value.id && !it.archived }) { "请至少保留一个可用账本" }
        require(old == null || old.currency == value.currency || !hasBookReferences(value.id)) { "已有记录或计划的账本不能修改币种" }
        dao.putBook(BookEntity(value.id, value.name.trim(), value.currency, value.archived))
    }
    suspend fun archiveBook(id: String, archived: Boolean) = mutate {
        val value = requireNotNull(dao.book(id)) { "账本不存在" }
        require(!archived || dao.books().count { !it.archived } > 1 || value.archived) { "请至少保留一个可用账本" }
        dao.putBook(value.copy(archived = archived))
    }
    suspend fun saveAccount(value: Account, openingMinor: Long? = null) = mutate {
        validId(value.id); name(value.name); CurrencyCode(value.currency)
        require(value.statementDay in 1..31 && value.repaymentDay in 1..31) { "账单日和还款日应为1至31" }
        val old = dao.account(value.id)
        require(old == null || old.currency == value.currency && old.kind == value.kind.name || !hasAccountReferences(value.id)) { "已有流水或计划引用的账户不能修改币种或类型" }
        require(old == null || openingMinor == null) { "已有账户请使用调账记录修改余额" }
        dao.putAccount(AccountEntity(value.id, value.name.trim(), value.currency, value.kind.name, value.archived, value.statementDay, value.repaymentDay))
        if (openingMinor != null && openingMinor != 0L) {
            val books = dao.books().filter { !it.archived }
            val book = books.firstOrNull { it.currency == value.currency } ?: books.firstOrNull() ?: throw IllegalArgumentException("请先创建可用账本")
            engine.execute(FinancialCommand.Save(EntryDraft(id = stableId("opening:${value.id}"), bookId = book.id, kind = EntryKind.OPENING, amount = Money(openingMinor, CurrencyCode(value.currency)), accountId = value.id, excludedFromTotals = true, excludedFromBudget = true), operationId = stableId("opening-operation:${value.id}")))
        }
    }
    suspend fun archiveAccount(id: String, archived: Boolean) = mutate { dao.putAccount(requireNotNull(dao.account(id)) { "账户不存在" }.copy(archived = archived)) }
    suspend fun saveCategory(value: Category) = mutate {
        validId(value.id); name(value.name)
        val old = dao.category(value.id)
        require(old == null || old.income == value.income || !hasCategoryReferences(value.id)) { "已有账单、子分类或计划引用的分类不能更改收支类型" }
        val parent = value.parentId?.let { requireNotNull(dao.category(it)) { "上级分类不存在" } }
        require(parent == null || parent.id != value.id && parent.parentId == null && parent.income == value.income && !parent.archived) { "分类最多两级，上级必须是同类可用分类" }
        require(parent == null || dao.categories().none { it.parentId == value.id }) { "已有子分类的分类不能移为子分类" }
        dao.putCategory(CategoryEntity(value.id, value.name.trim(), value.income, value.parentId, value.archived))
    }
    suspend fun archiveCategory(id: String, archived: Boolean) = mutate {
        val value = requireNotNull(dao.category(id)) { "分类不存在" }
        if (!archived && value.parentId != null) require(dao.category(value.parentId)?.archived == false) { "请先恢复上级分类" }
        dao.putCategory(value.copy(archived = archived))
        if (archived) dao.categories().filter { it.parentId == id }.forEach { dao.putCategory(it.copy(archived = true)) }
    }
    suspend fun saveTag(value: Tag) = mutate { validId(value.id); name(value.name); dao.putTag(TagEntity(value.id, value.name.trim())) }
    suspend fun deleteTag(id: String) = mutate {
        require(!hasTemplateReference("tagIds", id)) { "周期计划或待确认记录仍引用此标签" }
        dao.deleteTag(id)
    }

    override fun observePending(): Flow<List<CaptureCandidate>> = dao.observePending().map { rows -> rows.map { it.domain() } }
    override suspend fun candidate(id: String): CaptureCandidate? = dao.candidate(id)?.domain()
    suspend fun candidateDraft(id: String): EntryDraft? {
        val candidate = dao.candidate(id) ?: return null
        dao.candidateTemplate(id)?.let { return it.template.draft(splitIds(it.tagIds), splitIds(it.attachmentIds)) }
        val book = candidate.bookId ?: return null
        return EntryDraft(id = stableId("candidate:$id"), bookId = book, kind = EntryKind.valueOf(candidate.kind), amount = Money(candidate.amount, CurrencyCode(candidate.currency)), accountId = candidate.accountId, otherAccountId = candidate.otherAccountId, categoryId = candidate.categoryId, merchant = candidate.merchant, note = candidate.description, occurredAt = candidate.occurredAt, feeMinor = candidate.feeMinor)
    }
    override suspend fun dismiss(id: String, revision: Long) = mutate {
        val old = requireNotNull(dao.candidate(id)) { "待确认记录不存在" }
        require(old.revision == revision) { "内容已更新，请重新确认" }
        if (old.state == "PENDING") dao.putCandidate(old.copy(state = "DISMISSED", pendingWindow = null, revision = old.revision + 1))
    }
    override suspend fun enrichRemark(id: String, remark: String, expectedDescription: String?) = mutate {
        require(remark.length <= 4000) { "备注过长" }
        val old = dao.candidate(id)
        if (old != null && old.state == "PENDING" && (if (expectedDescription == null) old.description.isBlank() else old.description == expectedDescription) && remark.isNotBlank() && old.description != remark.trim()) dao.putCandidate(old.copy(description = remark.trim(), revision = old.revision + 1))
    }
    override suspend fun upsert(proposal: CaptureProposal): CaptureUpsertResult = mutate { upsertInside(proposal) }
    suspend fun importEntries(drafts: List<EntryDraft>): Int = mutate {
        require(drafts.size in 1..2000) { "每批导入应为1至2000条" }
        require(drafts.map { it.id }.distinct().size == drafts.size) { "导入包含重复标识" }
        drafts.forEach { draft ->
            require(draft.kind in setOf(EntryKind.EXPENSE, EntryKind.INCOME) && draft.parentId == null) { "CSV仅支持普通收入和支出" }
            engine.execute(FinancialCommand.Save(draft, operationId = stableId("csv-import:${draft.id}")))
        }
        drafts.size
    }
    suspend fun validateImportEntries(drafts: List<EntryDraft>) = mutate {
        require(drafts.size in 1..2000) { "每批导入应为1至2000条" }
        drafts.forEach { require(it.kind in setOf(EntryKind.EXPENSE, EntryKind.INCOME) && it.parentId == null) { "CSV仅支持普通收支" }; engine.validate(it) }
    }

    internal suspend fun upsertInside(proposal: CaptureProposal): CaptureUpsertResult {
        val config = configProvider()
        if (!config.enabled || proposal.packageName !in config.selectedPackages) return CaptureUpsertResult(CaptureUpsertKind.ALREADY_HANDLED, null)
        FinancialEngine.checkedMoney(proposal.amount.minor)
        require(proposal.packageName.length in 1..300 && proposal.windowKey.length in 1..2048 && proposal.merchant.length <= 500 && proposal.description.length <= 4000) { "采集内容无效或过长" }
        require(proposal.feeMinor in 0..FinancialEngine.MAX_MONEY && proposal.discountMinor in 0..FinancialEngine.MAX_MONEY) { "采集费用无效" }
        val strong = proposal.strongKey?.takeIf { proposal.identityKind == "PAYMENT_ID" && !proposal.paymentId.isNullOrBlank() && it.isNotBlank() }
        require(strong == null || strong.length <= 2048) { "交易标识过长" }
        val identity = strong?.let { FinancialEngine.hash("${proposal.packageName}|${proposal.kind}|$it") }
        val window = FinancialEngine.hash("${proposal.packageName}|${proposal.windowKey}")
        val byStrong = identity?.let { dao.strongCandidate(it) }
        val byWindow = dao.windowCandidate(window)
        var previous = byStrong ?: byWindow?.takeUnless { identity != null && it.strongIdentity != null && identity != it.strongIdentity }
        if (byStrong != null && byWindow != null && byWindow.id != byStrong.id && byWindow.strongIdentity == null) dao.putCandidate(byWindow.copy(state = "DISMISSED", pendingWindow = null, revision = byWindow.revision + 1))
        if (previous != null) {
            val conflict = previous.amount != proposal.amount.minor || previous.currency != proposal.amount.currency.value || previous.kind != proposal.kind.name || previous.feeMinor != proposal.feeMinor
            if (previous.state != "PENDING") {
                if (conflict) {
                    val reasons = previous.domain().reasons + ConfirmationReason.EVIDENCE_CONFLICT
                    val changed = previous.copy(reasons = encodeReasons(reasons), requiresConfirmation = true)
                    if (changed != previous) { previous = changed.copy(revision = previous.revision + 1); dao.putCandidate(previous) }
                }
                return CaptureUpsertResult(if (conflict) CaptureUpsertKind.CONFLICT else CaptureUpsertKind.ALREADY_HANDLED, previous.domain())
            }
            val resolved = resolve(proposal)
            val previousReasons = previous.domain().reasons
            val parserRequires = if (identity != null) proposal.requiresConfirmation else previous.strongIdentity == null || ConfirmationReason.WEAK_IDENTITY in previousReasons
            val merged = previous.copy(merchant = previous.merchant.ifBlank { proposal.merchant }, description = previous.description.ifBlank { proposal.description },
                paymentId = previous.paymentId ?: proposal.paymentId, strongIdentity = previous.strongIdentity ?: identity, strongKey = previous.strongKey ?: strong,
                identityKind = if (identity != null) "PAYMENT_ID" else previous.identityKind, bookId = previous.bookId ?: resolved.bookId, accountId = previous.accountId ?: resolved.accountId,
                categoryId = previous.categoryId ?: resolved.categoryId, otherAccountId = previous.otherAccountId ?: resolved.otherAccountId,
                paymentMethod = previous.paymentMethod.ifBlank { proposal.paymentMethod }, fromPaymentMethod = previous.fromPaymentMethod.ifBlank { proposal.fromPaymentMethod }, toPaymentMethod = previous.toPaymentMethod.ifBlank { proposal.toPaymentMethod },
                orderReference = previous.orderReference ?: proposal.orderReference,
                visibleTransactionTime = previous.visibleTransactionTime || proposal.visibleTransactionTime,
                occurredAt = if (!previous.visibleTransactionTime && proposal.visibleTransactionTime) proposal.occurredAt else previous.occurredAt)
            val reasons = previousReasons.filter { it == ConfirmationReason.EVIDENCE_CONFLICT || it == ConfirmationReason.POSSIBLE_DUPLICATE }.toMutableSet()
            if (conflict) reasons += ConfirmationReason.EVIDENCE_CONFLICT
            if (merged.strongIdentity == null || parserRequires) reasons += ConfirmationReason.WEAK_IDENTITY
            val book = merged.bookId?.let { dao.book(it) }
            if (book == null || book.archived) reasons += ConfirmationReason.BOOK_UNAVAILABLE
            if (book != null && book.currency != merged.currency && merged.kind !in setOf("EXPENSE", "INCOME", "REFUND")) reasons += ConfirmationReason.CURRENCY_CONFLICT
            suspend fun usable(id: String?): Boolean = id?.let { dao.account(it) }?.let { !it.archived && it.currency == merged.currency } == true
            if (!usable(merged.accountId) || merged.kind in setOf("TRANSFER", "CREDIT_PAYMENT", "LEND", "BORROW") && !usable(merged.otherAccountId)) reasons += ConfirmationReason.ACCOUNT_UNRESOLVED
            val canonical = merged.copy(requiresConfirmation = reasons.isNotEmpty(), reasons = encodeReasons(reasons))
            val updated = if (canonical == previous) previous else canonical.copy(revision = previous.revision + 1).also { dao.putCandidate(it) }
            return CaptureUpsertResult(if (conflict) CaptureUpsertKind.CONFLICT else CaptureUpsertKind.UPDATED, updated.domain())
        }
        require(dao.pendingCount() < 1000) { "待确认列表已满，请先处理已有记录" }
        val resolved = resolve(proposal)
        val reasons = resolved.reasons + if (identity == null || proposal.requiresConfirmation) setOf(ConfirmationReason.WEAK_IDENTITY) else emptySet()
        val value = CandidateEntity(newId(), proposal.packageName, proposal.pageClass.take(500), proposal.sessionId.take(300), proposal.windowId, proposal.kind.name, proposal.amount.minor, proposal.amount.currency.value, proposal.merchant, proposal.description, proposal.paymentMethod.take(500), proposal.occurredAt, proposal.visibleTransactionTime, proposal.paymentId?.take(2048), proposal.orderReference?.take(2048), proposal.identityKind, identity, proposal.windowKey, if (byWindow == null) window else null, proposal.requiresConfirmation || reasons.isNotEmpty(), resolved.bookId, resolved.accountId, resolved.categoryId, reasons = encodeReasons(reasons), fromPaymentMethod = proposal.fromPaymentMethod.take(500), toPaymentMethod = proposal.toPaymentMethod.take(500), feeMinor = proposal.feeMinor, discountMinor = proposal.discountMinor, sourceRoute = proposal.sourceRoute.take(300), otherAccountId = resolved.otherAccountId, strongKey = strong)
        dao.putCandidate(value)
        return CaptureUpsertResult(CaptureUpsertKind.CREATED, value.domain())
    }

    private data class Resolution(val bookId: String?, val accountId: String?, val otherAccountId: String?, val categoryId: String?, val reasons: Set<ConfirmationReason>)
    private suspend fun resolve(proposal: CaptureProposal): Resolution {
        val reasons = linkedSetOf<ConfirmationReason>()
        val book = configProvider().bookId?.let { dao.book(it) }
        if (book == null || book.archived) reasons += ConfirmationReason.BOOK_UNAVAILABLE
        if (book != null && book.currency != proposal.amount.currency.value && proposal.kind !in setOf(EntryKind.EXPENSE, EntryKind.INCOME, EntryKind.REFUND)) reasons += ConfirmationReason.CURRENCY_CONFLICT
        val normalizedMerchant = normalize(proposal.merchant)
        val match = dao.rules().filter { rule ->
            (rule.packageName.isBlank() || rule.packageName == proposal.packageName) &&
                (rule.merchant.isBlank() || normalize(rule.merchant) == normalizedMerchant) &&
                (rule.keyword.isBlank() || normalize(proposal.merchant + " " + proposal.description).contains(normalize(rule.keyword)))
        }.sortedWith(compareByDescending<MerchantRuleEntity> { if (it.merchant.isNotBlank()) 4 else 0 }.thenByDescending { if (it.packageName.isNotBlank()) 2 else 0 }.thenByDescending { it.keyword.length }.thenBy { it.id }).firstOrNull()
        val mappings = dao.mappings()
        suspend fun mapped(method: String, fallback: String? = null): String? {
            val exact = mappings.firstOrNull { it.packageName == proposal.packageName && normalize(it.paymentMethod) == normalize(method) }
            val raw = exact?.accountId ?: fallback
            val account = raw?.let { dao.account(it) }
            return account?.takeIf { !it.archived && it.currency == proposal.amount.currency.value }?.id
        }
        val from = mapped(proposal.fromPaymentMethod.ifBlank { proposal.paymentMethod }, match?.accountId)
        val to = proposal.toPaymentMethod.takeIf { it.isNotBlank() }?.let { mapped(it) }
        if (from == null || proposal.kind in setOf(EntryKind.TRANSFER, EntryKind.CREDIT_PAYMENT, EntryKind.LEND, EntryKind.BORROW) && to == null) reasons += ConfirmationReason.ACCOUNT_UNRESOLVED
        val category = match?.categoryId?.let { dao.category(it) }?.takeIf { !it.archived && it.income == (proposal.kind == EntryKind.INCOME) }
            ?: dao.categories().firstOrNull { !it.archived && it.income == (proposal.kind == EntryKind.INCOME) && it.name.startsWith("其他") }
        return Resolution(book?.id, from, to, category?.id, reasons)
    }

    internal fun name(value: String) { require(value.trim().isNotEmpty() && value.length <= 100) { "名称应为1至100个字符" } }
    fun observeBudgets(bookId: String): Flow<List<Budget>> = combine(dao.observeBudgets(bookId), dao.observeEntryChanges(), dao.observeCategories()) { rows, _, _ ->
        rows.map { Budget(it.id, it.bookId, it.name, it.amount, it.start, it.endExclusive, it.categoryId, dao.budgetSpent(it.bookId, it.start, it.endExclusive, it.categoryId)) }
    }
    suspend fun saveBudget(value: Budget) = mutate {
        validId(value.id); name(value.name); usableBook(value.bookId); FinancialEngine.checkedMoney(value.amount)
        require(value.start >= 0 && value.endExclusive > value.start) { "预算日期范围无效" }
        value.categoryId?.let { require(dao.category(it)?.let { c -> !c.archived && !c.income } == true) { "请选择可用支出分类" } }
        dao.putBudget(BudgetEntity(value.id, value.bookId, value.name.trim(), value.amount, value.start, value.endExclusive, value.categoryId))
    }
    suspend fun deleteBudget(id: String) = mutate { dao.deleteBudget(id) }
    fun observeGoals(bookId: String): Flow<List<Goal>> = combine(dao.observeGoals(bookId), dao.observeEntryChanges()) { rows, _ -> rows.map { row ->
        val progress = when (row.mode) { "account" -> row.accountId?.let { dao.balance(it) } ?: 0; "income" -> dao.totals(row.bookId).income; "balance" -> dao.totals(row.bookId).let { Math.subtractExact(it.income, it.expense) }; else -> row.progress }
        Goal(row.id, row.bookId, row.name, row.target, progress.coerceAtLeast(0), row.mode, row.accountId)
    } }
    suspend fun saveGoal(value: Goal) = mutate {
        validId(value.id); name(value.name); val book = usableBook(value.bookId); FinancialEngine.checkedMoney(value.target)
        require(value.progress in 0..FinancialEngine.MAX_MONEY && value.mode in setOf("manual", "account", "income", "balance")) { "目标进度或方式无效" }
        if (value.mode == "account") require(value.accountId?.let { dao.account(it) }?.let { !it.archived && it.currency == book.currency } == true) { "请选择同币种可用账户" }
        dao.putGoal(GoalEntity(value.id, value.bookId, value.name.trim(), value.target, if (value.mode == "manual") value.progress else 0, value.mode, if (value.mode == "account") value.accountId else null))
    }
    suspend fun deleteGoal(id: String) = mutate { dao.deleteGoal(id) }
    fun observeRecurring(bookId: String): Flow<List<RecurringRule>> = dao.observeRecurring(bookId).map { rows -> rows.map { it.domain() } }
    suspend fun saveRecurring(value: RecurringRule) = mutate {
        validId(value.id); require(value.interval in 1..365 && value.unit in setOf("DAY", "WEEK", "MONTH", "YEAR")) { "周期规则无效" }
        require(value.nextAt > 0 && (value.until ?: Long.MAX_VALUE) >= value.nextAt && (value.maxCount ?: Int.MAX_VALUE) > 0) { "周期结束条件无效" }
        require(value.draft.kind in setOf(EntryKind.EXPENSE, EntryKind.INCOME, EntryKind.TRANSFER, EntryKind.CREDIT_PAYMENT)) { "周期计划支持收支、转账和信用还款" }
        val old = dao.recurring(value.id)
        val draft = engine.validate(value.draft, old?.template, allowArchived = !value.enabled)
        val executed = old?.executed ?: 0
        require((value.maxCount ?: Int.MAX_VALUE) >= executed) { "执行次数不能少于已生成次数" }
        val day = Instant.ofEpochMilli(value.nextAt).atZone(ZoneId.systemDefault()).dayOfMonth
        dao.putRecurring(RecurringEntity(value.id, draft.entity(), value.interval, value.unit, value.nextAt, value.until, value.maxCount, executed, value.enabled && executed < (value.maxCount ?: Int.MAX_VALUE), draft.tagIds.joinToString("|"), draft.attachmentIds.joinToString("|"), if (old != null && old.nextAt == value.nextAt) old.anchorDay else day))
    }
    suspend fun deleteRecurring(id: String) = mutate { dao.deleteRecurring(id) }
    /** Due plans create durable confirmation drafts, never silently debit an account. */
    suspend fun generateRecurringCandidates(now: Long = System.currentTimeMillis(), limit: Int = 100): Int = mutate {
        require(limit in 1..1000) { "生成批量范围无效" }
        var generated = 0
        while (generated < limit && dao.pendingCount() < 1000) {
            val rule = dao.dueRecurring(now, 1).firstOrNull() ?: break
            if (rule.until != null && rule.nextAt > rule.until || rule.maxCount != null && rule.executed >= rule.maxCount) { dao.putRecurring(rule.copy(enabled = false)); continue }
            val occurrence = rule.nextAt
            val id = stableId("recurring:${rule.id}:$occurrence")
            if (dao.candidate(id) == null) {
                val draft = rule.template.copy(id = stableId("recurring-entry:${rule.id}:$occurrence"), occurredAt = occurrence)
                val book = dao.book(draft.bookId)
                val reasons = linkedSetOf(ConfirmationReason.WEAK_IDENTITY)
                if (book == null || book.archived) reasons += ConfirmationReason.BOOK_UNAVAILABLE
                if (draft.accountId != null && dao.account(draft.accountId)?.archived != false) reasons += ConfirmationReason.ACCOUNT_UNRESOLVED
                dao.putCandidate(CandidateEntity(id, "local.recurring", "", rule.id, -1, draft.kind, draft.amount, draft.currency, draft.merchant, draft.note, "周期账单", occurrence, true, null, null, "SCHEDULE", "schedule:$id", id, "schedule:$id", true, draft.bookId, draft.accountId, draft.categoryId, reasons = encodeReasons(reasons), feeMinor = draft.feeMinor, otherAccountId = draft.otherAccountId, templateId = rule.id))
                dao.putCandidateTemplate(CandidateTemplateEntity(id, draft, rule.tagIds, rule.attachmentIds))
                generated++
            }
            var next = advance(occurrence, rule.interval, rule.unit)
            if (rule.unit == "MONTH" || rule.unit == "YEAR") {
                val date = Instant.ofEpochMilli(next).atZone(ZoneId.systemDefault())
                next = date.withDayOfMonth(minOf(rule.anchorDay, date.toLocalDate().lengthOfMonth())).toInstant().toEpochMilli()
            }
            val count = Math.addExact(rule.executed, 1)
            dao.putRecurring(rule.copy(nextAt = next, executed = count, enabled = (rule.until == null || next <= rule.until) && (rule.maxCount == null || count < rule.maxCount)))
        }
        generated
    }
    fun observeInstalments(entryId: String): Flow<List<Instalment>> = combine(dao.observeInstalments(entryId), dao.observeEntryChanges()) { rows, _ -> rows.map { row ->
        row.domain().copy(paymentId = row.paymentId?.takeIf { dao.entry(it)?.deleted == false })
    } }
    suspend fun setInstalments(entryId: String, periods: Int, totalFee: Long, firstDueAt: Long) = mutate {
        val entry = requireNotNull(dao.entry(entryId)) { "消费记录不存在" }
        require(!entry.deleted && entry.kind == "EXPENSE" && entry.accountId?.let { dao.account(it) }?.kind == "CREDIT") { "请选择信用账户中的普通消费" }
        require(dao.book(entry.bookId)?.currency == entry.currency) { "分期消费必须与账本使用同一币种" }
        require(dao.children(entryId).isEmpty()) { "该消费已有退款或还款记录" }
        require(dao.instalments(entryId).none { it.paymentId?.let { id -> dao.entry(id)?.deleted == false } == true }) { "已有实际还款，不能重建计划" }
        require(periods in 1..120 && entry.amount >= periods && totalFee in 0..FinancialEngine.MAX_MONEY && firstDueAt > 0) { "期数、手续费或首次还款日期无效" }
        Math.addExact(entry.amount, totalFee)
        dao.deleteInstalments(entryId)
        for (part in InstalmentSchedule.create(entry.amount, totalFee, periods, firstDueAt)) {
            dao.putInstalment(InstalmentEntity(newId(), entryId, part.sequence, part.dueAt, part.principal, part.fee))
        }
    }
    suspend fun deleteInstalments(entryId: String) = mutate {
        require(dao.instalments(entryId).none { it.paymentId?.let { id -> dao.entry(id)?.deleted == false } == true }) { "已有实际还款的计划不能删除" }
        dao.deleteInstalments(entryId)
    }
    suspend fun payInstalment(id: String, fundingAccountId: String, operationId: String = newId()): CommitResult = try {
        CommitResult.Success(mutate {
            val part = requireNotNull(dao.instalment(id)) { "分期不存在" }
            val parent = requireNotNull(dao.entry(part.entryId)) { "原消费不存在" }
            require(!parent.deleted) { "原消费已删除" }
            dao.receipt(operationId)?.let { receipt ->
                val paid = requireNotNull(dao.entry(receipt.entryId)) { "原还款记录已不存在" }
                require(dao.instalmentForPayment(paid.id)?.id == id && paid.accountId == fundingAccountId && !paid.deleted) { "此保存标识用于另一笔还款或已撤销" }
                return@mutate CommitReceipt(operationId, paid.id, receipt.revision)
            }
            require(part.paymentId?.let { dao.entry(it)?.deleted == false } != true) { "该期已经还款" }
            val draft = EntryDraft(id = stableId("instalment-payment:$operationId"), bookId = parent.bookId, kind = EntryKind.CREDIT_PAYMENT, amount = Money(part.principal, CurrencyCode(parent.currency)), accountId = fundingAccountId, otherAccountId = parent.accountId, note = "分期第${part.sequence}期", parentId = parent.id, feeMinor = part.fee)
            val saved = engine.execute(FinancialCommand.Save(draft, operationId = operationId))
            dao.putInstalment(part.copy(paymentId = saved.entryId))
            saved
        })
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (error: IllegalArgumentException) { CommitResult.Rejected(error.message ?: "还款内容无效") }
    catch (error: ArithmeticException) { CommitResult.Rejected("还款金额超出允许范围") }
    catch (error: android.database.SQLException) { CommitResult.Rejected("保存未完成，未发生任何扣款") }

    fun observeInvestments(bookId: String): Flow<List<Investment>> = combine(dao.observeInvestments(bookId), dao.observeInvestmentChanges()) { rows, _ -> rows.map { investmentPosition(it) } }
    fun observeInvestmentEvents(investmentId: String): Flow<List<InvestmentEvent>> = dao.observeInvestmentEvents(investmentId).map { rows -> rows.map { it.domain() } }
    suspend fun saveInvestment(value: Investment) = mutate {
        validId(value.id); name(value.name); val book = usableBook(value.bookId)
        require(value.currency == book.currency) { "投资日志币种必须与账本一致" }
        val old = dao.investment(value.id)
        require(old == null || old.bookId == value.bookId && old.currency == value.currency || dao.investmentEventCount(value.id) == 0) { "已有投资记录不能更换账本或币种" }
        dao.putInvestment(InvestmentEntity(value.id, value.bookId, value.name.trim(), value.currency))
    }
    suspend fun deleteInvestment(id: String) = mutate { require(dao.investmentEventCount(id) == 0) { "请先删除持仓日志记录" }; dao.deleteInvestment(id) }
    suspend fun addInvestmentEvent(value: InvestmentEvent) = mutate {
        validId(value.id); val instrument = requireNotNull(dao.investment(value.investmentId)) { "投资标的不存在" }
        usableBook(instrument.bookId)
        require(value.kind in setOf("buy", "sell", "dividend", "price") && value.occurredAt > 0 && value.amount in 0..FinancialEngine.MAX_MONEY) { "投资记录无效" }
        val quantity = quantity(value.quantity)
        if (value.kind in setOf("buy", "sell")) require(quantity.signum() > 0 && value.amount > 0) { "买卖数量与总金额必须大于零" }
        else require(quantity.signum() == 0) { "分红与估值不改变持仓数量" }
        val old = dao.investmentEvent(value.id)
        require(old == null || old.investmentId == value.investmentId) { "不能移动已有投资记录到另一标的" }
        dao.putInvestmentEvent(InvestmentEventEntity(value.id, value.investmentId, value.kind, quantity.toPlainString(), value.amount, value.occurredAt))
        investmentPosition(instrument)
    }
    suspend fun deleteInvestmentEvent(id: String) = mutate {
        val old = requireNotNull(dao.investmentEvent(id)) { "投资记录不存在" }
        dao.deleteInvestmentEvent(id)
        investmentPosition(requireNotNull(dao.investment(old.investmentId)))
    }
    private suspend fun investmentPosition(row: InvestmentEntity): Investment {
        var holding = BigDecimal.ZERO; var cost = 0L; var realized = 0L; var price: Long? = null
        var afterTime = Long.MIN_VALUE; var afterId = ""
        while (true) {
            val page = dao.investmentEventsPage(row.id, afterTime, afterId, 64)
            if (page.isEmpty()) break
            for (event in page) {
            val q = quantity(event.quantity)
            require(event.amount in 0..FinancialEngine.MAX_MONEY && event.occurredAt > 0) { "投资记录数值无效" }
            require(event.kind in setOf("buy", "sell", "dividend", "price")) { "投资记录类型无效" }
            if (event.kind == "buy" || event.kind == "sell") require(q.signum() > 0 && event.amount > 0) { "买卖数量或金额无效" } else require(q.signum() == 0) { "分红估值不能改变数量" }
            when (event.kind) {
                "buy" -> { holding = holding.add(q); cost = Math.addExact(cost, event.amount) }
                "sell" -> {
                    require(q.signum() > 0 && q <= holding) { "卖出或删除后持仓数量不能为负" }
                    val allocated = if (q.compareTo(holding) == 0) cost else BigDecimal.valueOf(cost).multiply(q).divide(holding, 0, RoundingMode.HALF_UP).longValueExact()
                    cost = Math.subtractExact(cost, allocated); realized = Math.addExact(realized, Math.subtractExact(event.amount, allocated)); holding = holding.subtract(q)
                }
                "dividend" -> realized = Math.addExact(realized, event.amount)
                "price" -> price = event.amount
                else -> error("投资记录类型无效")
            }
            }
            afterTime = page.last().occurredAt; afterId = page.last().id
        }
        val market = price?.let { BigDecimal.valueOf(it).multiply(holding).setScale(0, RoundingMode.HALF_UP).longValueExact() }
        return Investment(row.id, row.bookId, row.name, row.currency, holding.stripTrailingZeros().toPlainString(), cost, market, realized)
    }
    private fun quantity(raw: String): BigDecimal { require(raw.matches(Regex("[0-9]{1,15}(?:\\.[0-9]{1,6})?"))) { "数量最多6位小数" }; return raw.toBigDecimal() }

    fun observeRules(): Flow<List<MerchantRule>> = dao.observeRules().map { rows -> rows.map { it.domain() } }
    fun observeMappings(): Flow<List<AccountMapping>> = dao.observeMappings().map { rows -> rows.map { it.domain() } }
    suspend fun saveRule(value: MerchantRule) = mutate {
        validId(value.id); require(value.merchant.isNotBlank() || value.keyword.isNotBlank() || value.packageName.isNotBlank()) { "请填写匹配条件" }
        require(value.merchant.length <= 500 && value.keyword.length <= 500 && value.packageName.length <= 300) { "规则内容过长" }
        require(value.categoryId != null || value.accountId != null) { "请指定分类或账户" }
        value.categoryId?.let { require(dao.category(it)?.archived == false) { "分类不可用" } }
        value.accountId?.let { require(dao.account(it)?.archived == false) { "账户不可用" } }
        dao.putRule(MerchantRuleEntity(value.id, value.merchant.trim(), value.packageName.trim(), value.keyword.trim(), value.categoryId, value.accountId))
    }
    suspend fun deleteRule(id: String) = mutate { dao.deleteRule(id) }
    suspend fun saveMapping(value: AccountMapping) = mutate {
        validId(value.id); require(value.packageName.isNotBlank() && value.packageName.length <= 300 && value.paymentMethod.length <= 500) { "应用或付款方式无效" }
        require(dao.account(value.accountId)?.archived == false) { "账户不可用" }
        dao.putMapping(AccountMappingEntity(value.id, value.packageName.trim(), normalize(value.paymentMethod), value.accountId))
    }
    suspend fun deleteMapping(id: String) = mutate { dao.deleteMapping(id) }
    fun pagedAttachments(): Flow<PagingData<Attachment>> = Pager(PagingConfig(pageSize = 50, initialLoadSize = 50, enablePlaceholders = false, maxSize = 200)) { dao.pagedAttachments() }.flow.map { page -> page.map { it.domain() } }
    fun observeAttachments(ids: List<String>): Flow<List<Attachment>> {
        require(ids.size <= 100) { "每笔账单最多关联100个附件" }
        return if (ids.isEmpty()) flowOf(emptyList()) else dao.observeAttachments(ids.distinct()).map { rows -> rows.map { it.domain() } }
    }
    suspend fun attachment(id: String): Attachment? = dao.attachment(id)?.domain()
    suspend fun saveAttachment(value: Attachment) = mutate {
        require(UUID.fromString(value.id).toString() == value.id) { "附件标识无效" }; require(value.name.length in 1..200 && value.mime.length in 1..100 && value.size in 0..16L * 1024 * 1024 && value.localPath.isNotBlank() && value.localPath.length <= 1024 && !value.localPath.contains('\u0000')) { "附件信息无效" }
        val old = dao.attachment(value.id)
        require(old == null || old.localPath == value.localPath && old.size == value.size && old.mime == value.mime) { "不能替换已有附件内容" }
        dao.putAttachment(AttachmentEntity(value.id, value.name, value.mime, value.size, value.localPath))
    }
    suspend fun deleteAttachment(id: String) = mutate { require(dao.attachmentReferences(id) == 0 && !hasTemplateReference("attachmentIds", id)) { "账单或计划仍引用该附件" }; dao.deleteAttachment(id) }
    fun observePeriodNote(bookId: String, start: Long, endExclusive: Long): Flow<String> = dao.observePeriodNote(bookId, start, endExclusive).map { it.orEmpty() }
    fun observeOutstanding(entryId: String): Flow<Long> = dao.observeEntryChanges().map {
        val parent = dao.entry(entryId)
        if (parent == null || parent.deleted) 0 else Math.subtractExact(parent.amount, dao.children(entryId).filter { child -> child.kind in FinancialEngine.childKinds }.fold(0L) { sum, child -> Math.addExact(sum, child.amount) })
    }
    /** Account movements and closing balances are factual; no repayment allocation is guessed. */
    fun observeCreditStatements(accountId: String): Flow<List<CreditStatement>> = combine(dao.observeAccounts(), dao.observeEntryChanges()) { accounts, _ ->
        val account = accounts.firstOrNull { it.account.id == accountId }?.account
        if (account == null || account.kind != "CREDIT") emptyList() else {
            val zone = ZoneId.systemDefault()
            val current = java.time.YearMonth.now(zone)
            (0..11).map { offset ->
                val month = current.minusMonths(offset.toLong())
                val closingDate = month.atDay(minOf(account.statementDay, month.lengthOfMonth()))
                val previous = month.minusMonths(1)
                val start = previous.atDay(minOf(account.statementDay, previous.lengthOfMonth())).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = closingDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val repaymentMonth = if (account.repaymentDay > account.statementDay) month else month.plusMonths(1)
                val repaymentAt = repaymentMonth.atDay(minOf(account.repaymentDay, repaymentMonth.lengthOfMonth())).atStartOfDay(zone).toInstant().toEpochMilli()
                val movement = dao.creditMovement(accountId, start, end)
                CreditStatement(start, end, repaymentAt, movement.charges, movement.repayments, dao.balanceBefore(accountId, end))
            }
        }
    }
    suspend fun savePeriodNote(bookId: String, start: Long, endExclusive: Long, note: String) = mutate { usableBook(bookId); require(start >= 0 && endExclusive > start && note.length <= 10000) { "总结内容或日期无效" }; dao.putPeriodNote(PeriodNoteEntity(bookId, start, endExclusive, note)) }
    private fun advance(time: Long, amount: Int, unit: String): Long {
        val value = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault())
        return when (unit) { "DAY" -> value.plusDays(amount.toLong()); "WEEK" -> value.plusWeeks(amount.toLong()); "MONTH" -> value.plusMonths(amount.toLong()); "YEAR" -> value.plusYears(amount.toLong()); else -> throw IllegalArgumentException("周期单位无效") }.toInstant().toEpochMilli()
    }
    internal fun validId(value: String) { require(value.matches(Regex("[A-Za-z0-9._:-]{1,128}"))) { "记录标识无效" } }
    internal suspend fun usableBook(id: String): BookEntity = requireNotNull(dao.book(id)) { "账本不存在" }.also { require(!it.archived) { "账本已归档" } }
    internal fun normalize(value: String) = value.trim().lowercase(java.util.Locale.ROOT).replace(Regex("\\s+"), " ")
    internal fun encodeReasons(values: Set<ConfirmationReason>) = values.map { it.name }.sorted().joinToString("|")
    internal fun stableId(value: String) = UUID.nameUUIDFromBytes(value.toByteArray(Charsets.UTF_8)).toString()
    private fun hasBookReferences(id: String): Boolean = db.openHelper.readableDatabase.query("SELECT (SELECT COUNT(*) FROM entries WHERE bookId=?)+(SELECT COUNT(*) FROM candidates WHERE bookId=?)+(SELECT COUNT(*) FROM budgets WHERE bookId=?)+(SELECT COUNT(*) FROM goals WHERE bookId=?)+(SELECT COUNT(*) FROM recurring_rules WHERE template_bookId=?)+(SELECT COUNT(*) FROM investments WHERE bookId=?)+(SELECT COUNT(*) FROM period_notes WHERE bookId=?)", arrayOf(id,id,id,id,id,id,id)).use { it.moveToFirst(); it.getLong(0)>0 }
    private fun hasAccountReferences(id: String): Boolean = db.openHelper.readableDatabase.query("SELECT 1 FROM entries WHERE accountId=? OR otherAccountId=? UNION ALL SELECT 1 FROM candidates WHERE accountId=? OR otherAccountId=? UNION ALL SELECT 1 FROM goals WHERE accountId=? UNION ALL SELECT 1 FROM recurring_rules WHERE template_accountId=? OR template_otherAccountId=? UNION ALL SELECT 1 FROM account_mappings WHERE accountId=? UNION ALL SELECT 1 FROM merchant_rules WHERE accountId=? LIMIT 1", arrayOf(id,id,id,id,id,id,id,id,id)).use { it.moveToFirst() }
    private fun hasCategoryReferences(id: String): Boolean = db.openHelper.readableDatabase.query("SELECT 1 FROM entries WHERE categoryId=? UNION ALL SELECT 1 FROM categories WHERE parentId=? UNION ALL SELECT 1 FROM candidates WHERE categoryId=? UNION ALL SELECT 1 FROM recurring_rules WHERE template_categoryId=? UNION ALL SELECT 1 FROM budgets WHERE categoryId=? UNION ALL SELECT 1 FROM merchant_rules WHERE categoryId=? LIMIT 1", arrayOf(id,id,id,id,id,id)).use { it.moveToFirst() }
    private fun hasTemplateReference(column: String, id: String): Boolean {
        require(column == "tagIds" || column == "attachmentIds")
        return db.openHelper.readableDatabase.query("SELECT 1 FROM recurring_rules WHERE ('|' || $column || '|') LIKE ? UNION ALL SELECT 1 FROM candidate_templates t JOIN candidates c ON c.id=t.candidateId WHERE c.state='PENDING' AND ('|' || t.$column || '|') LIKE ? LIMIT 1", arrayOf("%|$id|%", "%|$id|%")).use { it.moveToFirst() }
    }
}
