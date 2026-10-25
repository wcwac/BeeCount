package io.github.wcwac.beecount.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.wcwac.beecount.domain.*

@Composable
internal fun InvestmentsScreen(vm: NativeViewModel, nav: Navigator, book: Book?) {
    if (book == null) return
    val items by remember(book.id) { vm.repository.observeInvestments(book.id) }.collectAsStateWithLifecycle(emptyList())
    var creating by remember { mutableStateOf(false) }
    PageColumn {
        Text("投资日志独立管理，不自动增减现金账户，也不重复计入账户净资产。", style = MaterialTheme.typography.bodySmall)
        Button({ creating = true }, Modifier.fillMaxWidth()) { Text("新增持仓标的") }
        items.forEach { investment -> InfoCard(investment.name, "持有 ${investment.quantity} 份 · 成本 ${money(investment.cost, investment.currency)}\n${investment.marketValue?.let { "市值 ${money(it, investment.currency)}" } ?: "尚未记录价格"}", { nav.open(Screen.INVESTMENT_DETAIL, investment.id) }) }
        if (items.isEmpty()) EmptyState("手工记录股票、基金或其他投资")
    }
    if (creating) InvestmentForm(vm, book, null) { creating = false }
}

@Composable
private fun InvestmentForm(vm: NativeViewModel, book: Book, old: Investment?, close: () -> Unit) {
    var name by remember { mutableStateOf(old?.name ?: "") }
    FormDialog(if (old == null) "新增投资标的" else "编辑投资标的", close) {
        TextFieldRow("名称", name, { name = it }); Text("记账币种：${old?.currency ?: book.currency}")
        Button({ vm.run(after = close) { require(name.isNotBlank()) { "请输入名称" }; vm.repository.saveInvestment(old?.copy(name = name.trim()) ?: Investment(bookId = book.id, name = name.trim(), currency = book.currency)) } }, Modifier.fillMaxWidth()) { Text("保存") }
    }
}

@Composable
internal fun InvestmentDetailScreen(vm: NativeViewModel, book: Book?, investmentId: String) {
    if (book == null) return
    val investments by remember(book.id) { vm.repository.observeInvestments(book.id) }.collectAsStateWithLifecycle(emptyList())
    val events by remember(investmentId) { vm.repository.observeInvestmentEvents(investmentId) }.collectAsStateWithLifecycle(emptyList())
    val investment = investments.find { it.id == investmentId }
    var editing by remember { mutableStateOf(false) }; var adding by remember { mutableStateOf<String?>(null) }; var deleting by remember { mutableStateOf<String?>(null) }
    if (investment == null) { EmptyState("持仓不存在或已删除"); return }
    PageColumn {
        InfoCard(investment.name, "份额 ${investment.quantity}\n持仓成本 ${money(investment.cost, investment.currency)}\n已实现收益 ${money(investment.realized, investment.currency)}", { editing = true })
        investment.marketValue?.let { InfoCard("估值", "市值 ${money(it, investment.currency)}\n浮动盈亏 ${money(it - investment.cost, investment.currency)}") }
        listOf("buy", "sell", "dividend", "price").chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { action -> OutlinedButton({ adding = action }, Modifier.weight(1f)) { Text(investmentKindLabel(action)) } } } }
        SectionTitle("投资事件")
        events.forEach { event -> InfoCard(investmentKindLabel(event.kind), "${dateText(event.occurredAt)} · ${money(event.amount, investment.currency)}${if (event.kind in listOf("buy", "sell")) " · ${event.quantity} 份" else ""}", trailing = { TextButton({ deleting = event.id }) { Text("删除") } }) }
        if (events.isEmpty()) EmptyState("添加第一笔买入或价格记录")
        OutlinedButton({ deleting = "investment" }, Modifier.fillMaxWidth()) { Text("删除投资标的") }
    }
    if (editing) InvestmentForm(vm, book, investment) { editing = false }
    adding?.let { kind ->
        var amount by remember(kind) { mutableStateOf("") }; var quantity by remember(kind) { mutableStateOf("") }; var date by remember(kind) { mutableStateOf(dateText(System.currentTimeMillis())) }; var error by remember { mutableStateOf<String?>(null) }
        FormDialog(investmentKindLabel(kind), { adding = null }) {
            if (kind in listOf("buy", "sell")) TextFieldRow("份额", quantity, { quantity = it }, numeric = true)
            TextFieldRow(if (kind == "price") "每份价格 · ${investment.currency}" else "实际${if (kind == "buy") "支出" else "收入"}总额 · ${investment.currency}", amount, { amount = it }, numeric = true, supporting = if (kind in listOf("buy", "sell")) "填写已包含交易费用的实际金额" else "")
            TextFieldRow("日期", date, { date = it }, supporting = "yyyy-MM-dd")
            InlineError(error)
            Button({ try {
                val value = Money.parse(amount, CurrencyCode(investment.currency)).minor; require(value >= 0) { "金额不能为负数" }
                val shares = if (kind in listOf("buy", "sell")) quantity.trim().toBigDecimal().also { require(it.signum() > 0) { "份额必须大于零" } }.toPlainString() else "0"
                val time = parseDay(date)
                vm.run(after = { adding = null }) { vm.repository.addInvestmentEvent(InvestmentEvent(investmentId = investment.id, kind = kind, quantity = shares, amount = value, occurredAt = time)) }
            } catch (failure: Exception) { error = failure.message } }, Modifier.fillMaxWidth()) { Text("保存事件") }
        }
    }
    deleting?.let { id -> Confirmation("删除投资记录", if (id == "investment") "有投资事件的标的需先删除事件。" else "删除后将重新计算持仓、成本与收益。", { deleting = null }) { deleting = null; vm.run { if (id == "investment") vm.repository.deleteInvestment(investmentId) else vm.repository.deleteInvestmentEvent(id) } } }
}

private fun investmentKindLabel(kind: String): String = when (kind) { "buy" -> "买入"; "sell" -> "卖出"; "dividend" -> "分红"; else -> "手工价格" }
