package io.github.wcwac.beecount.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.wcwac.beecount.domain.*
import java.time.*
import java.time.format.DateTimeFormatter

internal val LocalUiWindowsAllowed = staticCompositionLocalOf { true }
internal val SupportedCurrencies by lazy { java.util.Currency.getAvailableCurrencies().filter { it.defaultFractionDigits in 0..3 }.map { it.currencyCode }.sorted() }

internal fun dateText(time: Long): String = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
internal fun timeText(time: Long): String = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
internal fun parseDay(value: String): Long = LocalDate.parse(value.trim()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
internal fun monthRange(offset: Int = 0): Pair<Long, Long> {
    val start = LocalDate.now().withDayOfMonth(1).plusMonths(offset.toLong())
    return start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() to start.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
internal fun money(value: Long, currency: String = "CNY") = Money(value, CurrencyCode(currency)).display()
internal fun kindLabel(kind: EntryKind): String = when (kind) {
    EntryKind.EXPENSE -> "支出"; EntryKind.INCOME -> "收入"; EntryKind.TRANSFER -> "转账"
    EntryKind.CREDIT_PAYMENT -> "信用还款"; EntryKind.LEND -> "借出"; EntryKind.BORROW -> "借入"
    EntryKind.COLLECT -> "收回借款"; EntryKind.REPAY -> "归还借款"; EntryKind.REFUND -> "退款"
    EntryKind.REIMBURSEMENT -> "待报销支出"; EntryKind.REIMBURSEMENT_PAYMENT -> "报销回款"
    EntryKind.OPENING -> "期初余额"; EntryKind.ADJUSTMENT -> "余额调整"
}
internal fun accountLabel(kind: AccountKind): String = when (kind) {
    AccountKind.CASH -> "现金"; AccountKind.BANK -> "银行卡"; AccountKind.CREDIT -> "信用账户"
    AccountKind.RECEIVABLE -> "应收款"; AccountKind.PAYABLE -> "应付款"; AccountKind.STORED_VALUE -> "储值账户"
}

@Composable
internal fun PageColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
}

@Composable
internal fun SectionTitle(text: String) { Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp)) }

@Composable
internal fun InfoCard(title: String, detail: String, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    val modifier = Modifier.fillMaxWidth().then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
    ElevatedCard(modifier) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailing?.invoke()
        }
    }
}

@Composable
internal fun ToggleRow(title: String, detail: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    InfoCard(title, detail, trailing = { Switch(checked, onChange) })
}

@Composable
internal fun EmptyState(text: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (action != null) OutlinedButton(onAction) { Text(action) }
    }
}

@Composable
internal fun AmountText(value: Long, currency: String = "CNY", hidden: Boolean = false, income: Boolean? = null, modifier: Modifier = Modifier) {
    Text(if (hidden) "••••" else money(value, currency), modifier, color = when (income) { true -> MaterialTheme.colorScheme.primary; false -> MaterialTheme.colorScheme.error; null -> MaterialTheme.colorScheme.onSurface }, style = MaterialTheme.typography.titleMedium)
}

@Composable
internal fun SummaryCard(summary: Summary, currency: String, hidden: Boolean = false) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("收入" to summary.income, "支出" to summary.expense, "结余" to summary.income - summary.expense).forEach { (label, amount) ->
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(label, style = MaterialTheme.typography.labelLarge); Text(if (hidden) "••••" else Money(amount, CurrencyCode(currency)).decimal(), style = MaterialTheme.typography.titleMedium); Text(currency, style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

@Composable
internal fun TransactionRow(entry: LedgerEntry, hidden: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(entry.draft.merchant.ifBlank { entry.categoryName.ifBlank { kindLabel(entry.draft.kind) } }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text(listOf(kindLabel(entry.draft.kind), entry.accountName, entry.draft.note).filter(String::isNotBlank).joinToString(" · "), maxLines = 2, overflow = TextOverflow.Ellipsis) },
        trailingContent = { Column(horizontalAlignment = Alignment.End) { AmountText(entry.draft.amount.minor, entry.draft.amount.currency.value, hidden); Text(timeText(entry.draft.occurredAt), style = MaterialTheme.typography.labelSmall) } },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
internal fun TextFieldRow(label: String, value: String, onValueChange: (String) -> Unit, numeric: Boolean = false, supporting: String = "", singleLine: Boolean = true) {
    OutlinedTextField(value, onValueChange, Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
        supportingText = if (supporting.isEmpty()) null else ({ Text(supporting) }))
}

@Composable
internal fun <T> ChoiceField(label: String, options: List<T>, selected: T?, title: (T) -> String, onChoose: (T) -> Unit, empty: String = "请选择") {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("$label：${selected?.let(title) ?: empty}", modifier = Modifier.weight(1f)); Text("⌄") }
        DropdownMenu(expanded && LocalUiWindowsAllowed.current, { expanded = false }) {
            if (options.isEmpty()) DropdownMenuItem(text = { Text("暂无可选项") }, enabled = false, onClick = {})
            options.forEach { option -> DropdownMenuItem(text = { Text(title(option)) }, onClick = { expanded = false; onChoose(option) }) }
        }
    }
}

@Composable
internal fun Confirmation(title: String, message: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    if (!LocalUiWindowsAllowed.current) return
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(message) }, confirmButton = { TextButton(onConfirm) { Text("确认") } }, dismissButton = { TextButton(onDismiss) { Text("取消") } })
}

@Composable
internal fun InlineError(error: String?) { if (!error.isNullOrBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }

@Composable
internal fun TrendChart(points: List<ChartPoint>, currency: String) {
    val positive = MaterialTheme.colorScheme.primary
    val negative = MaterialTheme.colorScheme.error
    val max = points.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1) ?: 1L
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("收入 / 支出趋势", style = MaterialTheme.typography.titleMedium)
        Canvas(Modifier.fillMaxWidth().height(170.dp)) {
            val count = points.size.coerceAtLeast(1); val step = size.width / count
            points.forEachIndexed { index, point ->
                val width = (step * .28f).coerceAtLeast(1f)
                val income = (point.income.toDouble() / max * size.height).toFloat()
                val expense = (point.expense.toDouble() / max * size.height).toFloat()
                drawRect(positive, Offset(index * step + step * .16f, size.height - income), Size(width, income))
                drawRect(negative, Offset(index * step + step * .52f, size.height - expense), Size(width, expense))
            }
        }
        if (points.isNotEmpty()) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(points.first().label, style = MaterialTheme.typography.labelSmall); Text(points.last().label, style = MaterialTheme.typography.labelSmall) }
        Text("绿色为收入，红色为支出 · $currency", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun CategoryChart(rows: List<CategoryTotal>, currency: String) {
    val colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary, Color(0xFFB67925), Color(0xFF8757AA), Color(0xFF9D5159))
    val total = rows.sumOf { it.amount }.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(Modifier.fillMaxWidth().height(150.dp)) {
            val diameter = size.minDimension; val offset = Offset((size.width - diameter) / 2, 0f); var start = -90f
            rows.forEachIndexed { index, row -> val sweep = (row.amount.toDouble() / total * 360).toFloat(); drawArc(colors[index % colors.size], start, sweep, false, offset, Size(diameter, diameter), style = androidx.compose.ui.graphics.drawscope.Stroke(30.dp.toPx(), cap = StrokeCap.Butt)); start += sweep }
        }
        rows.forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(row.label, Modifier.weight(1f)); Text(money(row.amount, currency)) } }
    }
}
