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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.wcwac.beecount.domain.*

@Composable
internal fun FinancialHistoryScreen(vm: NativeViewModel, nav: Navigator, book: Book?, accounts: List<Account>) {
    if (book == null) return
    var group by rememberSaveable { mutableStateOf("借出") }
    val kinds = when (group) { "借出" -> setOf(EntryKind.LEND); "借入" -> setOf(EntryKind.BORROW); "报销" -> setOf(EntryKind.REIMBURSEMENT); else -> setOf(EntryKind.COLLECT, EntryKind.REPAY, EntryKind.REIMBURSEMENT_PAYMENT, EntryKind.REFUND) }
    val rows = remember(book.id, group) { vm.repository.history(HistoryFilter(bookId = book.id, kinds = kinds)) }.collectAsLazyPagingItems()
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("借出", "借入", "报销", "结算记录").forEach { label -> FilterChip(group == label, { group = label }, { Text(label) }) } }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton({ nav.open(Screen.EDITOR, kind = EntryKind.LEND.name) }) { Text("新增借出") }; OutlinedButton({ nav.open(Screen.EDITOR, kind = EntryKind.BORROW.name) }) { Text("新增借入") } }
                Text("点击原记录登记收回、归还或报销到账。报销本金不计入普通收支与预算。", style = MaterialTheme.typography.bodySmall)
            }
        }
        items(rows.itemCount, key = rows.itemKey { it.draft.id }) { index -> rows[index]?.let { entry ->
            TransactionRow(entry, false) { nav.open(Screen.DETAIL, entry.draft.id) }
        } }
        item { PagingStatus(rows.loadState.refresh, rows.itemCount, rows::retry) }
    }
}

@Composable
internal fun CreditScreen(vm: NativeViewModel, nav: Navigator, accounts: List<Account>) {
    val creditAccounts = accounts.filter { !it.archived && it.kind == AccountKind.CREDIT }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = creditAccounts.find { it.id == selectedId } ?: creditAccounts.firstOrNull()
    PageColumn {
        ChoiceField("信用账户", creditAccounts, selected, Account::name, { selectedId = it.id })
        if (selected == null) EmptyState("先添加一个信用账户", "管理账户", { nav.open(Screen.ACCOUNTS) })
        else {
            InfoCard(selected.name, "当前余额 ${money(selected.balance, selected.currency)}\n账单日 ${selected.statementDay} 日 · 还款日 ${selected.repaymentDay} 日")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button({ nav.open(Screen.EDITOR, kind = EntryKind.CREDIT_PAYMENT.name) }) { Text("记录还款") }; OutlinedButton({ nav.open(Screen.ACCOUNT_DETAIL, selected.id) }) { Text("查看全部流水") } }
            Text("消费分期从对应消费账单详情进入。账期金额按本地账单计算，请与银行账单核对。", style = MaterialTheme.typography.bodySmall)
            CreditStatements(vm, selected)
        }
    }
}

@Composable
private fun CreditStatements(vm: NativeViewModel, account: Account) {
    val rows by remember(account.id) { vm.repository.observeCreditStatements(account.id) }.collectAsStateWithLifecycle(emptyList())
    rows.forEach { row -> InfoCard("${dateText(row.start)} — ${dateText(row.endExclusive - 1)}", "到期日 ${dateText(row.repaymentAt)}\n本期新增欠款 ${money(row.charges, account.currency)}\n本期还款与入账 ${money(row.repayments, account.currency)}\n期末余额 ${money(row.closingBalance, account.currency)}") }
}
