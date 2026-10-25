package io.github.wcwac.beecount.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.wcwac.beecount.domain.*

/** Shared by the activity inbox and the native overlay host. */
@Composable
fun CandidateReviewContent(
    candidate: CaptureCandidate,
    books: List<Book>,
    accounts: List<Account>,
    categories: List<Category>,
    busy: Boolean,
    onSave: (EntryDraft) -> Unit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    countdownSeconds: Int? = null,
    onInteraction: () -> Unit = {},
    initialDraft: EntryDraft? = null,
    inlineChoices: Boolean = false,
) {
    var amount by rememberSaveable(candidate.id) { mutableStateOf(candidate.proposal.amount.decimal()) }
    var merchant by rememberSaveable(candidate.id) { mutableStateOf(candidate.proposal.merchant) }
    var note by rememberSaveable(candidate.id) { mutableStateOf(candidate.proposal.description) }
    var bookId by rememberSaveable(candidate.id) { mutableStateOf(candidate.bookId) }
    var accountId by rememberSaveable(candidate.id) { mutableStateOf(candidate.accountId) }
    var otherAccountId by rememberSaveable(candidate.id) { mutableStateOf(candidate.otherAccountId ?: initialDraft?.otherAccountId) }
    var categoryId by rememberSaveable(candidate.id) { mutableStateOf(candidate.categoryId) }
    var error by remember { mutableStateOf<String?>(null) }
    val currency = candidate.proposal.amount.currency
    val ordinary = candidate.proposal.kind in listOf(EntryKind.EXPENSE, EntryKind.INCOME, EntryKind.REFUND)
    val eligibleBooks = books.filter { !it.archived && (ordinary || it.currency == currency.value) }
    val eligibleAccounts = accounts.filter { !it.archived && it.currency == currency.value }
    val fundingAccounts = eligibleAccounts.filter { it.kind !in listOf(AccountKind.RECEIVABLE, AccountKind.PAYABLE) }
    val twoAccounts = candidate.proposal.kind in listOf(EntryKind.TRANSFER, EntryKind.CREDIT_PAYMENT, EntryKind.LEND, EntryKind.BORROW, EntryKind.REIMBURSEMENT)
    val targetAccounts = eligibleAccounts.filter { it.id != accountId && when (candidate.proposal.kind) {
        EntryKind.CREDIT_PAYMENT -> it.kind == AccountKind.CREDIT
        EntryKind.LEND, EntryKind.REIMBURSEMENT -> it.kind == AccountKind.RECEIVABLE
        EntryKind.BORROW -> it.kind == AccountKind.PAYABLE
        else -> true
    } }
    val income = candidate.proposal.kind == EntryKind.INCOME
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("确认${kindLabel(candidate.proposal.kind)}", style = MaterialTheme.typography.titleLarge)
        Text(candidate.proposal.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (candidate.reasons.isNotEmpty()) Text(candidate.reasons.joinToString("、") { reasonLabel(it) }, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        TextFieldRow("金额 · ${currency.value}", amount, { onInteraction(); amount = it }, numeric = true)
        TextFieldRow("商户", merchant, { onInteraction(); merchant = it })
        CandidateChoice("账本", eligibleBooks, eligibleBooks.find { it.id == bookId }, Book::name, inlineChoices, onInteraction, { bookId = it.id })
        CandidateChoice("实际收付款账户", fundingAccounts, fundingAccounts.find { it.id == accountId }, Account::name, inlineChoices, onInteraction, { accountId = it.id })
        if (twoAccounts) CandidateChoice(when (candidate.proposal.kind) { EntryKind.LEND, EntryKind.REIMBURSEMENT -> "应收账户"; EntryKind.BORROW -> "应付账户"; else -> "收款账户" }, targetAccounts, targetAccounts.find { it.id == otherAccountId }, Account::name, inlineChoices, onInteraction, { otherAccountId = it.id })
        CandidateChoice("分类", categories.filter { !it.archived && it.income == income }, categories.find { it.id == categoryId }, Category::name, inlineChoices, onInteraction, { categoryId = it.id })
        TextFieldRow("备注", note, { onInteraction(); note = it })
        InlineError(error)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onInteraction(); onDismiss() }, enabled = !busy) { Text("忽略") }
            TextButton(onClick = { onInteraction(); onEdit() }, enabled = !busy) { Text("完整编辑") }
            Spacer(Modifier.weight(1f))
            Button(onClick = {
                onInteraction()
                try {
                    val chosenBook = requireNotNull(eligibleBooks.find { it.id == bookId }) { "请选择有效账本" }
                    val parsed = Money.parse(amount, currency)
                    require(parsed.minor > 0) { "金额必须大于零" }
                    require(fundingAccounts.any { it.id == accountId }) { "请选择有效的实际收付款账户" }
                    require(!twoAccounts || targetAccounts.any { it.id == otherAccountId }) { "请选择有效的目标账户" }
                    require(candidate.proposal.packageName != "local.recurring" || initialDraft != null) { "周期记录正在读取，请稍后确认" }
                    error = null
                    val base = initialDraft ?: EntryDraft(bookId = chosenBook.id, kind = candidate.proposal.kind, amount = parsed, occurredAt = candidate.proposal.occurredAt, feeMinor = candidate.proposal.feeMinor)
                    onSave(base.copy(bookId = chosenBook.id, amount = parsed, accountId = accountId, otherAccountId = otherAccountId, categoryId = categoryId, merchant = merchant.trim(), note = note.trim()))
                } catch (failure: IllegalArgumentException) { error = failure.message }
            }, enabled = !busy) { Text(if (busy) "保存中" else countdownSeconds?.let { "确认 ${it}s" } ?: "确认") }
        }
    }
}

@Composable
private fun <T> CandidateChoice(label: String, options: List<T>, selected: T?, title: (T) -> String, inline: Boolean, interact: () -> Unit, choose: (T) -> Unit) {
    if (!inline) { ChoiceField(label, options, selected, title, { interact(); choose(it) }); return }
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        OutlinedButton({ interact(); expanded = !expanded }, Modifier.fillMaxWidth()) { Text("$label：${selected?.let(title) ?: "请选择"}") }
        if (expanded) {
            if (options.isEmpty()) Text("暂无可选项", style = MaterialTheme.typography.bodySmall)
            options.forEach { option -> TextButton({ interact(); choose(option); expanded = false }, Modifier.fillMaxWidth()) { Text(title(option)) } }
        }
    }
}

private fun reasonLabel(reason: ConfirmationReason): String = when (reason) {
    ConfirmationReason.WEAK_IDENTITY -> "交易标识不足，请核对"
    ConfirmationReason.ACCOUNT_UNRESOLVED -> "账户需要确认"
    ConfirmationReason.BOOK_UNAVAILABLE -> "请选择目标账本"
    ConfirmationReason.CURRENCY_CONFLICT -> "币种需要确认"
    ConfirmationReason.EVIDENCE_CONFLICT -> "页面信息存在差异"
    ConfirmationReason.POSSIBLE_DUPLICATE -> "可能重复，请核对"
}
