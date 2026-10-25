package io.github.wcwac.beecount.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.wcwac.beecount.domain.*
import java.time.*
import java.time.format.DateTimeFormatter

private data class LoadedEditor(val ready: Boolean = false, val draft: EntryDraft? = null, val revision: Long? = null, val candidate: CaptureCandidate? = null, val error: String? = null, val amountText: String? = null, val generation: Long? = null, val notice: String? = null)

@Composable
internal fun EntryEditor(vm: NativeViewModel, nav: Navigator, route: Destination, books: List<Book>, book: Book?, accounts: List<Account>, categories: List<Category>, tags: List<Tag>) {
    val canReadDraft = LocalUiWindowsAllowed.current
    val loaded by produceState(LoadedEditor(), route, canReadDraft) {
        if (!canReadDraft) return@produceState
        value = try {
            val handoff = route.draftToken.takeIf { it.isNotBlank() }?.let(vm.platform::editorDraft)
            val result = when {
                handoff != null -> {
                    require(handoff.candidateId.orEmpty() == route.candidateId) { "临时草稿与候选记录不匹配，请重新打开" }
                    val candidate = handoff.candidateId?.let { vm.repository.candidate(it) }
                    if (handoff.candidateId != null && (candidate == null || candidate.state != CandidateState.PENDING)) LoadedEditor(true, error = "这条采集记录已处理或不存在")
                    else if (candidate != null && candidate.revision != handoff.candidateRevision) LoadedEditor(true, error = "候选内容已更新，请返回待确认列表重新核对")
                    else LoadedEditor(true, handoff.snapshot.draft, candidate = candidate, amountText = handoff.snapshot.amountText, generation = handoff.generation)
                }
                route.candidateId.isNotEmpty() -> {
                    val candidate = vm.repository.candidate(route.candidateId)
                    if (candidate == null || candidate.state != CandidateState.PENDING) LoadedEditor(true, error = "这条采集记录已处理或不存在")
                    else {
                        val proposal = candidate.proposal
                        val fallbackBook = candidate.bookId ?: book?.id
                        val draft = vm.repository.candidateDraft(candidate.id) ?: fallbackBook?.let { EntryDraft(bookId = it, kind = proposal.kind, amount = proposal.amount, accountId = candidate.accountId, otherAccountId = candidate.otherAccountId, categoryId = candidate.categoryId, merchant = proposal.merchant, note = proposal.description, occurredAt = proposal.occurredAt, feeMinor = proposal.feeMinor) }
                        LoadedEditor(true, draft, candidate = candidate, error = if (draft == null) "请先创建可用账本" else null)
                    }
                }
                route.id.isNotEmpty() -> vm.repository.entry(route.id).let { LoadedEditor(true, it?.draft, it?.revision, error = if (it == null) "记录已不存在" else null) }
                else -> LoadedEditor(true)
            }
            if (route.draftToken.isNotBlank() && handoff == null) result.copy(notice = if (route.candidateId.isNotEmpty()) "临时草稿已失效，已重新读取原候选；请核对表单后保存。" else "临时草稿已失效，请重新填写。") else result
        } catch (failure: Exception) { if (failure is kotlinx.coroutines.CancellationException) throw failure; LoadedEditor(true, error = failure.message ?: "读取失败") }
    }
    if (!loaded.ready) { LinearProgressIndicator(Modifier.fillMaxWidth()); return }
    if (loaded.error != null) { EmptyState(loaded.error!!); return }
    val initial = loaded.draft
    if (initial == null && book == null) { EmptyState("请先创建账本"); return }
    key(route, initial?.id) { EditorForm(vm, nav, route, loaded, books, book, accounts, categories, tags) }
}

@Composable
private fun EditorForm(vm: NativeViewModel, nav: Navigator, route: Destination, loaded: LoadedEditor, books: List<Book>, defaultBook: Book?, accounts: List<Account>, categories: List<Category>, tags: List<Tag>) {
    val initial = loaded.draft
    val draftId = rememberSaveable { initial?.id ?: newId() }
    var kindName by rememberSaveable { mutableStateOf(initial?.kind?.name ?: route.kind.ifEmpty { EntryKind.EXPENSE.name }) }
    var bookId by rememberSaveable { mutableStateOf(initial?.bookId?.takeIf { it.isNotBlank() } ?: defaultBook?.id.orEmpty()) }
    var amount by rememberSaveable { mutableStateOf(loaded.amountText ?: initial?.amount?.decimal() ?: "") }
    var currencyCode by rememberSaveable { mutableStateOf(initial?.amount?.currency?.value ?: defaultBook?.currency ?: "CNY") }
    var accountId by rememberSaveable { mutableStateOf(initial?.accountId) }
    var otherAccountId by rememberSaveable { mutableStateOf(initial?.otherAccountId) }
    var categoryId by rememberSaveable { mutableStateOf(initial?.categoryId) }
    var parentId by rememberSaveable { mutableStateOf(initial?.parentId ?: route.parentId.takeIf(String::isNotEmpty)) }
    var merchant by rememberSaveable { mutableStateOf(initial?.merchant ?: "") }
    var note by rememberSaveable { mutableStateOf(initial?.note ?: "") }
    var day by rememberSaveable { mutableStateOf(dateText(initial?.occurredAt ?: System.currentTimeMillis())) }
    var time by rememberSaveable { mutableStateOf(Instant.ofEpochMilli(initial?.occurredAt ?: System.currentTimeMillis()).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var fee by rememberSaveable { mutableStateOf(initial?.let { Money(it.feeMinor, it.amount.currency).decimal() } ?: "0") }
    var interest by rememberSaveable { mutableStateOf(initial?.let { Money(it.interestMinor, it.amount.currency).decimal() } ?: "0") }
    var excludedTotals by rememberSaveable { mutableStateOf(initial?.excludedFromTotals ?: false) }
    var excludedBudget by rememberSaveable { mutableStateOf(initial?.excludedFromBudget ?: false) }
    var tagIds by rememberSaveable { mutableStateOf(initial?.tagIds.orEmpty()) }
    var attachmentIds by rememberSaveable { mutableStateOf(initial?.attachmentIds.orEmpty()) }
    val attachmentResults by vm.platform.attachmentResults.collectAsStateWithLifecycle()
    LaunchedEffect(draftId, attachmentResults[draftId]) {
        attachmentResults[draftId]?.let { imported ->
            val combined = (attachmentIds + imported).distinct()
            if (combined.size > 100) vm.message.value = "每条账单最多关联 100 个附件；其余已导入文件可在附件列表查看"
            attachmentIds = combined.take(100)
            vm.platform.consumeAttachmentResults(draftId, imported)
        }
    }
    var error by remember { mutableStateOf<String?>(null) }
    var parentPicker by remember { mutableStateOf(false) }
    val busy by vm.busy.collectAsStateWithLifecycle()
    val kind = EntryKind.valueOf(kindName)
    val book = books.find { it.id == bookId } ?: defaultBook
    val parent by produceState<LedgerEntry?>(null, parentId) { value = parentId?.let { vm.repository.entry(it) } }
    val ordinary = kind in listOf(EntryKind.EXPENSE, EntryKind.INCOME)
    val signed = kind in listOf(EntryKind.OPENING, EntryKind.ADJUSTMENT)
    val currency = CurrencyCode(when { ordinary -> currencyCode; kind == EntryKind.REFUND -> parent?.draft?.amount?.currency?.value ?: initial?.amount?.currency?.value ?: currencyCode; signed -> accounts.find { it.id == accountId }?.currency ?: currencyCode; else -> book?.currency ?: currencyCode })
    val eligibleAccounts = accounts.filter { !it.archived && it.currency == currency.value }
    val eligibleCategories = categories.filter { !it.archived && it.income == (kind == EntryKind.INCOME) }
    val transfer = kind in listOf(EntryKind.TRANSFER, EntryKind.CREDIT_PAYMENT)
    val twoAccounts = transfer || kind in listOf(EntryKind.LEND, EntryKind.BORROW, EntryKind.REIMBURSEMENT)
    val fundingAccounts = eligibleAccounts.filter { signed || it.kind !in listOf(AccountKind.RECEIVABLE, AccountKind.PAYABLE) }
    val targetAccounts = eligibleAccounts.filter { it.id != accountId && when (kind) {
        EntryKind.CREDIT_PAYMENT -> it.kind == AccountKind.CREDIT
        EntryKind.LEND, EntryKind.REIMBURSEMENT -> it.kind == AccountKind.RECEIVABLE
        EntryKind.BORROW -> it.kind == AccountKind.PAYABLE
        else -> true
    } }
    val child = kind in listOf(EntryKind.REFUND, EntryKind.COLLECT, EntryKind.REPAY, EntryKind.REIMBURSEMENT_PAYMENT)
    val selectedAttachments by remember(attachmentIds.toList()) { vm.repository.observeAttachments(attachmentIds.toList()) }.collectAsStateWithLifecycle(emptyList())
    LaunchedEffect(currency.value) {
        if (accountId != null && accounts.any { it.id == accountId && it.currency != currency.value }) accountId = null
        if (otherAccountId != null && accounts.any { it.id == otherAccountId && it.currency != currency.value }) otherAccountId = null
    }
    PageColumn {
        loaded.notice?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        if (loaded.revision == null && !child && loaded.candidate == null) ChoiceField("类型", listOf(EntryKind.EXPENSE, EntryKind.INCOME, EntryKind.TRANSFER, EntryKind.CREDIT_PAYMENT, EntryKind.LEND, EntryKind.BORROW), kind, ::kindLabel, { kindName = it.name })
        else Text(kindLabel(kind), style = MaterialTheme.typography.titleLarge)
        ChoiceField("账本", books.filter { !it.archived }, book, Book::name, { selected -> if (!ordinary && selected.currency != currency.value) { accountId = null; otherAccountId = null }; bookId = selected.id })
        if (ordinary) ChoiceField("记录币种", SupportedCurrencies, currency.value, { it }, { currencyCode = it })
        TextFieldRow("金额 · ${currency.value}", amount, { amount = it }, numeric = true)
        ChoiceField(if (transfer) "付款账户" else "实际收付款账户", fundingAccounts, fundingAccounts.find { it.id == accountId }, { "${it.name} · ${accountLabel(it.kind)}" }, { accountId = it.id })
        if (accountId != null && !twoAccounts && !child) TextButton({ accountId = null }) { Text("不关联账户") }
        if (twoAccounts) ChoiceField(when (kind) { EntryKind.LEND, EntryKind.REIMBURSEMENT -> "应收账户"; EntryKind.BORROW -> "应付账户"; else -> "收款账户" }, targetAccounts, targetAccounts.find { it.id == otherAccountId }, Account::name, { otherAccountId = it.id })
        if (kind in listOf(EntryKind.EXPENSE, EntryKind.INCOME, EntryKind.REIMBURSEMENT)) ChoiceField("分类", eligibleCategories, eligibleCategories.find { it.id == categoryId }, Category::name, { categoryId = it.id })
        if (child) InfoCard("关联原记录", parent?.draft?.let { "${kindLabel(it.kind)} · ${it.merchant.ifBlank { it.note }} · ${it.amount.display()}" } ?: "必须选择原始交易", { parentPicker = true })
        TextFieldRow(if (kind in listOf(EntryKind.LEND, EntryKind.BORROW, EntryKind.COLLECT, EntryKind.REPAY)) "往来对象" else "商户", merchant, { merchant = it })
        TextFieldRow("备注", note, { note = it }, singleLine = false)
        TextFieldRow("日期", day, { day = it }, supporting = "yyyy-MM-dd")
        TextFieldRow("时间", time, { time = it }, supporting = "HH:mm")
        if (transfer || kind in listOf(EntryKind.LEND, EntryKind.BORROW, EntryKind.COLLECT, EntryKind.REPAY)) TextFieldRow("手续费", fee, { fee = it }, numeric = true)
        if (kind in listOf(EntryKind.COLLECT, EntryKind.REPAY)) TextFieldRow("利息", interest, { interest = it }, numeric = true)
        ToggleRow("不计入收支统计", "保留流水和账户变动", excludedTotals, { excludedTotals = it })
        ToggleRow("不计入预算", "不占用分类与期间预算", excludedBudget, { excludedBudget = it })
        SectionTitle("标签")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { tags.forEach { tag -> FilterChip(tag.id in tagIds, { tagIds = if (tag.id in tagIds) tagIds - tag.id else tagIds + tag.id }, { Text(tag.name) }) } }
        if (tags.isEmpty()) Text("可在“更多 → 标签管理”添加标签", style = MaterialTheme.typography.bodySmall)
        SectionTitle("附件")
        selectedAttachments.forEach { item -> InfoCard(item.name, "${item.size / 1024} KB", { vm.platform.openAttachment(item) }, trailing = { TextButton({ attachmentIds = attachmentIds - item.id }) { Text("移除") } }) }
        OutlinedButton({ vm.platform.pickAttachments(draftId) }) { Text("添加图片或文件") }
        InlineError(error)
        Button(onClick = {
            try {
                require(books.any { it.id == bookId && !it.archived }) { "请选择有效账本" }
                val parsed = Money.parse(amount, currency); require(if (signed) parsed.minor != 0L else parsed.minor > 0) { if (signed) "调整金额不能为零" else "金额必须大于零" }
                val instant = LocalDateTime.of(LocalDate.parse(day), LocalTime.parse(time)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                if (child) require(!parentId.isNullOrEmpty()) { "请选择关联原记录" }
                val draft = EntryDraft(id = draftId, bookId = bookId, kind = kind, amount = parsed, accountId = accountId, otherAccountId = otherAccountId, categoryId = categoryId, merchant = merchant.trim(), note = note.trim(), occurredAt = instant, parentId = parentId,
                    feeMinor = Money.parse(fee.ifBlank { "0" }, currency).minor, interestMinor = Money.parse(interest.ifBlank { "0" }, currency).minor, excludedFromTotals = excludedTotals, excludedFromBudget = excludedBudget, tagIds = tagIds, attachmentIds = attachmentIds)
                error = null
                vm.commit(FinancialCommand.Save(draft, loaded.revision, loaded.candidate?.id, loaded.candidate?.revision, expectedGeneration = loaded.generation)) {
                    if (route.draftToken.isNotBlank()) vm.platform.forgetEditorDraft(route.draftToken)
                    nav.back()
                }
            } catch (failure: Exception) { error = failure.message ?: "请检查填写内容" }
        }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "保存中…" else "保存") }
    }
    if (parentPicker) ParentPicker(vm, bookId, kind, { parentPicker = false }) { selected -> parentId = selected.draft.id; parentPicker = false }
}

@Composable
private fun ParentPicker(vm: NativeViewModel, bookId: String, kind: EntryKind, onDismiss: () -> Unit, onChoose: (LedgerEntry) -> Unit) {
    var query by remember { mutableStateOf("") }
    val allowed = when (kind) { EntryKind.REFUND -> listOf(EntryKind.EXPENSE); EntryKind.COLLECT -> listOf(EntryKind.LEND); EntryKind.REPAY -> listOf(EntryKind.BORROW); else -> listOf(EntryKind.REIMBURSEMENT) }
    val entries = remember(bookId, query, kind) { vm.repository.history(HistoryFilter(bookId = bookId, query = query, kinds = allowed.toSet())) }.collectAsLazyPagingItems()
    if (!LocalUiWindowsAllowed.current) return
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().heightIn(max = 580.dp).padding(16.dp)) {
                Text("选择原记录", style = MaterialTheme.typography.titleLarge)
                TextFieldRow("搜索原记录", query, { query = it })
                LazyColumn(Modifier.weight(1f, fill = false)) {
                    items(entries.itemCount, key = entries.itemKey { it.draft.id }) { index -> entries[index]?.takeIf { it.draft.kind in allowed }?.let { TransactionRow(it, false) { onChoose(it) } } }
                    item { PagingStatus(entries.loadState.refresh, entries.itemCount, entries::retry) }
                }
                TextButton(onDismiss, Modifier.fillMaxWidth()) { Text("取消") }
            }
        }
    }
}

@Composable
internal fun EntryDetail(vm: NativeViewModel, nav: Navigator, id: String, accounts: List<Account>) {
    val entry by remember(id) { vm.repository.observeEntry(id) }.collectAsStateWithLifecycle(null)
    val busy by vm.busy.collectAsStateWithLifecycle()
    val outstanding by remember(id) { vm.repository.observeOutstanding(id) }.collectAsStateWithLifecycle(0L)
    var confirm by remember { mutableStateOf<String?>(null) }
    var reimburse by remember { mutableStateOf(false) }
    var instalments by remember { mutableStateOf(false) }
    val item = entry
    if (item == null) { EmptyState("记录不存在或正在加载"); return }
    PageColumn {
        Text(kindLabel(item.draft.kind), style = MaterialTheme.typography.headlineSmall)
        AmountText(item.draft.amount.minor, item.draft.amount.currency.value)
        InfoCard(item.draft.merchant.ifBlank { "账单信息" }, listOf(dateText(item.draft.occurredAt), item.accountName, item.categoryName, item.draft.note).filter(String::isNotBlank).joinToString("\n"))
        if (item.draft.feeMinor != 0L) Text("手续费 ${money(item.draft.feeMinor, item.draft.amount.currency.value)}")
        if (item.draft.interestMinor != 0L) Text("利息 ${money(item.draft.interestMinor, item.draft.amount.currency.value)}")
        if (item.draft.excludedFromTotals) Text("不计入收支统计")
        if (item.draft.excludedFromBudget) Text("不计入预算")
        if (item.draft.kind in listOf(EntryKind.LEND, EntryKind.BORROW, EntryKind.REIMBURSEMENT)) InfoCard("尚未结清", money(outstanding, item.draft.amount.currency.value))
        if (item.deleted) {
            Button({ vm.commit(FinancialCommand.Restore(id, item.revision), nav::back) }, enabled = !busy) { Text("恢复账单") }
            OutlinedButton({ confirm = "purge" }, enabled = !busy) { Text("永久删除") }
        } else {
            Button({ nav.open(Screen.EDITOR, id) }) { Text("编辑账单") }
            when (item.draft.kind) {
                EntryKind.EXPENSE, EntryKind.REIMBURSEMENT -> {
                    if (item.draft.kind == EntryKind.EXPENSE) OutlinedButton({ nav.open(Screen.EDITOR, kind = EntryKind.REFUND.name, parentId = id) }) { Text("登记退款") }
                    if (item.draft.kind == EntryKind.EXPENSE) OutlinedButton({ reimburse = true }) { Text("标记待报销") }
                    else OutlinedButton({ nav.open(Screen.EDITOR, kind = EntryKind.REIMBURSEMENT_PAYMENT.name, parentId = id) }) { Text("登记报销回款") }
                    if (accounts.find { it.id == item.draft.accountId }?.kind == AccountKind.CREDIT) OutlinedButton({ instalments = true }) { Text("分期安排") }
                }
                EntryKind.LEND -> OutlinedButton({ nav.open(Screen.EDITOR, kind = EntryKind.COLLECT.name, parentId = id) }) { Text("收回借款") }
                EntryKind.BORROW -> OutlinedButton({ nav.open(Screen.EDITOR, kind = EntryKind.REPAY.name, parentId = id) }) { Text("归还借款") }
                else -> Unit
            }
            item.draft.parentId?.let { parent -> OutlinedButton({ nav.open(Screen.DETAIL, parent) }) { Text("查看关联原记录") } }
            OutlinedButton({ confirm = "delete" }, enabled = !busy) { Text("移入回收站") }
        }
    }
    confirm?.let { action -> Confirmation(if (action == "purge") "永久删除" else "删除账单", if (action == "purge") "删除后无法恢复；有后续结算的记录需先处理关联流水。" else "账单将进入回收站，相应账户变动也会撤销。", { confirm = null }) {
        confirm = null; vm.commit(if (action == "purge") FinancialCommand.Purge(id, item.revision) else FinancialCommand.Delete(id, item.revision), nav::back)
    } }
    if (reimburse) {
        var selected by remember { mutableStateOf<String?>(null) }
        val eligible = accounts.filter { !it.archived && it.kind == AccountKind.RECEIVABLE && it.currency == item.draft.amount.currency.value }
        if (LocalUiWindowsAllowed.current) AlertDialog(onDismissRequest = { reimburse = false }, title = { Text("标记待报销") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("报销本金将移出普通支出与预算；后续到账只结清应收款。", style = MaterialTheme.typography.bodySmall); ChoiceField("应收账户", eligible, eligible.find { it.id == selected }, Account::name, { selected = it.id }) } }, confirmButton = { TextButton({ selected?.let { vm.commit(FinancialCommand.MarkReimbursement(id, item.revision, it)); reimburse = false } }, enabled = selected != null && !busy) { Text("确认") } }, dismissButton = { TextButton({ reimburse = false }) { Text("取消") } })
    }
    if (instalments) InstalmentPanel(vm, id, accounts) { instalments = false }
}

@Composable
internal fun InstalmentPanel(vm: NativeViewModel, entryId: String, accounts: List<Account>, onDismiss: () -> Unit) {
    val rows by remember(entryId) { vm.repository.observeInstalments(entryId) }.collectAsStateWithLifecycle(emptyList())
    val entry by remember(entryId) { vm.repository.observeEntry(entryId) }.collectAsStateWithLifecycle(null)
    val currency = entry?.draft?.amount?.currency ?: CurrencyCode.CNY
    var periods by remember { mutableStateOf("3") }; var fee by remember { mutableStateOf("0") }; var first by remember { mutableStateOf(LocalDate.now().plusMonths(1).toString()) }
    var accountId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    if (!LocalUiWindowsAllowed.current) return
    Dialog(onDismissRequest = onDismiss) { Surface(shape = MaterialTheme.shapes.large) { PageColumn {
        Text("消费分期", style = MaterialTheme.typography.titleLarge)
        Text("分期只安排还款计划，不重复扣除原消费本金。", style = MaterialTheme.typography.bodySmall)
        TextFieldRow("期数", periods, { periods = it }, numeric = true)
        TextFieldRow("总手续费 · ${currency.value}", fee, { fee = it }, numeric = true)
        TextFieldRow("首次到期日", first, { first = it }, supporting = "yyyy-MM-dd")
        InlineError(error)
        Button({ try { val n = periods.toInt(); val totalFee = Money.parse(fee, currency).minor; val due = parseDay(first); vm.run { vm.repository.setInstalments(entryId, n, totalFee, due) }; error = null } catch (failure: Exception) { error = failure.message } }, enabled = entry != null) { Text("保存分期安排") }
        ChoiceField("还款资金账户", accounts.filter { !it.archived && it.currency == currency.value && it.kind !in listOf(AccountKind.CREDIT, AccountKind.RECEIVABLE, AccountKind.PAYABLE) }, accounts.find { it.id == accountId }, Account::name, { accountId = it.id })
        rows.forEach { row -> InfoCard("第 ${row.sequence} 期 · ${dateText(row.dueAt)}", "本金 ${money(row.principal, currency.value)} · 费用 ${money(row.fee, currency.value)}", trailing = { if (row.paymentId == null) TextButton({ accountId?.let { funding -> vm.run { when (val result = vm.repository.payInstalment(row.id, funding)) { is CommitResult.Rejected -> throw IllegalArgumentException(result.reason); is CommitResult.Success -> Unit } } } }, enabled = accountId != null) { Text("还款") } else Text("已还") }) }
        if (rows.isNotEmpty()) OutlinedButton({ vm.run { vm.repository.deleteInstalments(entryId) } }) { Text("移除未付分期安排") }
        TextButton(onDismiss) { Text("关闭") }
    } } }
}
