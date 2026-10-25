package io.github.wcwac.beecount.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.wcwac.beecount.domain.*
import java.time.LocalDate

@Composable
internal fun BudgetScreen(vm: NativeViewModel, nav: Navigator, book: Book?, categories: List<Category>) {
    if (book == null) return
    val rows by remember(book.id) { vm.repository.observeBudgets(book.id) }.collectAsStateWithLifecycle(emptyList())
    var editing by remember { mutableStateOf<String?>(null) }
    PageColumn {
        Text("预算按账单日期和分类计算，已标记“不计预算”的记录会排除。仅统计 ${book.currency} 币种，不折算其他币种。", style = MaterialTheme.typography.bodySmall)
        Button({ editing = "" }, Modifier.fillMaxWidth()) { Text("新增预算") }
        rows.forEach { budget ->
            ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(budget.name, style = MaterialTheme.typography.titleMedium)
                Text("${dateText(budget.start)} — ${dateText(budget.endExclusive - 1)}")
                Text("已用 ${money(budget.spent, book.currency)} / ${money(budget.amount, book.currency)}")
                LinearProgressIndicator(progress = { if (budget.amount <= 0) 0f else (budget.spent.toDouble() / budget.amount).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                Text(if (budget.spent > budget.amount) "超出 ${money(budget.spent - budget.amount, book.currency)}" else "剩余 ${money(budget.amount - budget.spent, book.currency)}", color = if (budget.spent > budget.amount) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton({ editing = budget.id }) { Text("编辑预算") }; TextButton({ nav.open(Screen.BUDGET_RECORDS, budget.id) }) { Text("计入账单") } }
            } }
        }
        if (rows.isEmpty()) EmptyState("还没有预算")
    }
    editing?.let { id ->
        val old = rows.find { it.id == id }
        var name by remember(id) { mutableStateOf(old?.name ?: "月度预算") }; var amount by remember(id) { mutableStateOf(old?.let { Money(it.amount, CurrencyCode(book.currency)).decimal() } ?: "") }
        var start by remember(id) { mutableStateOf(dateText(old?.start ?: monthRange().first)) }; var end by remember(id) { mutableStateOf(dateText((old?.endExclusive ?: monthRange().second) - 1)) }; var category by remember(id) { mutableStateOf(old?.categoryId) }; var error by remember { mutableStateOf<String?>(null) }
        FormDialog("预算设置", { editing = null }) {
            TextFieldRow("预算名称", name, { name = it }); TextFieldRow("预算金额 · ${book.currency}", amount, { amount = it }, numeric = true)
            TextFieldRow("开始日期", start, { start = it }, supporting = "yyyy-MM-dd"); TextFieldRow("结束日期", end, { end = it }, supporting = "yyyy-MM-dd，包含这一天")
            ChoiceField("分类", categories.filter { !it.archived && !it.income }, categories.find { it.id == category }, Category::name, { category = it.id }, "全部支出")
            TextButton({ category = null }) { Text("使用全部支出") }
            InlineError(error)
            Button({ try { val value = Money.parse(amount, CurrencyCode(book.currency)).minor; val first = parseDay(start); val last = parseDay(LocalDate.parse(end).plusDays(1).toString()); require(last > first && value > 0 && name.isNotBlank()) { "请核对名称、金额和日期范围" }; vm.run(after = { editing = null }) { vm.repository.saveBudget(Budget(old?.id ?: newId(), book.id, name.trim(), value, first, last, category)) } } catch (failure: Exception) { error = failure.message } }, Modifier.fillMaxWidth()) { Text("保存") }
            if (old != null) OutlinedButton({ vm.run(after = { editing = null }) { vm.repository.deleteBudget(old.id) } }, Modifier.fillMaxWidth()) { Text("删除预算") }
        }
    }
}

@Composable
internal fun GoalsScreen(vm: NativeViewModel, book: Book?, accounts: List<Account>) {
    if (book == null) return
    val rows by remember(book.id) { vm.repository.observeGoals(book.id) }.collectAsStateWithLifecycle(emptyList())
    var editing by remember { mutableStateOf<String?>(null) }
    PageColumn {
        Button({ editing = "" }, Modifier.fillMaxWidth()) { Text("新增目标") }
        rows.forEach { goal ->
            ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(goal.name, style = MaterialTheme.typography.titleMedium)
                Text("${money(goal.progress, book.currency)} / ${money(goal.target, book.currency)}")
                LinearProgressIndicator(progress = { if (goal.target <= 0) 0f else (goal.progress.toDouble() / goal.target).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                TextButton({ editing = goal.id }) { Text("编辑与更新进度") }
            } }
        }
        if (rows.isEmpty()) EmptyState("添加一个储蓄目标")
    }
    editing?.let { id ->
        val old = rows.find { it.id == id }
        var name by remember(id) { mutableStateOf(old?.name ?: "") }; var target by remember(id) { mutableStateOf(old?.let { Money(it.target, CurrencyCode(book.currency)).decimal() } ?: "") }; var progress by remember(id) { mutableStateOf(old?.let { Money(it.progress, CurrencyCode(book.currency)).decimal() } ?: "0") }
        var mode by remember(id) { mutableStateOf(old?.mode ?: "manual") }; var account by remember(id) { mutableStateOf(old?.accountId) }; var error by remember { mutableStateOf<String?>(null) }
        val modes = listOf("manual" to "手工维护", "account" to "指定账户余额", "income" to "账本累计收入", "balance" to "账本累计结余")
        FormDialog("存钱目标", { editing = null }) {
            TextFieldRow("目标名称", name, { name = it }); TextFieldRow("目标金额 · ${book.currency}", target, { target = it }, numeric = true)
            ChoiceField("进度来源", modes, modes.find { it.first == mode }, { it.second }, { mode = it.first })
            if (mode == "manual") TextFieldRow("当前进度", progress, { progress = it }, numeric = true)
            if (mode == "account") ChoiceField("关联账户", accounts.filter { !it.archived && it.currency == book.currency }, accounts.find { it.id == account }, Account::name, { account = it.id })
            InlineError(error)
            Button({ try { val cur = CurrencyCode(book.currency); val goal = Goal(old?.id ?: newId(), book.id, name.trim(), Money.parse(target, cur).minor, if (mode == "manual") Money.parse(progress, cur).minor else old?.progress ?: 0, mode, if (mode == "account") account else null); require(goal.name.isNotBlank() && goal.target > 0) { "请输入名称和大于零的目标金额" }; if (mode == "account") require(account != null) { "请选择账户" }; vm.run(after = { editing = null }) { vm.repository.saveGoal(goal) } } catch (failure: Exception) { error = failure.message } }, Modifier.fillMaxWidth()) { Text("保存") }
            if (old != null) OutlinedButton({ vm.run(after = { editing = null }) { vm.repository.deleteGoal(old.id) } }, Modifier.fillMaxWidth()) { Text("删除目标") }
        }
    }
}

@Composable
internal fun PlansScreen(vm: NativeViewModel, book: Book?, accounts: List<Account>, categories: List<Category>) {
    if (book == null) return
    val rows by remember(book.id) { vm.repository.observeRecurring(book.id) }.collectAsStateWithLifecycle(emptyList())
    var editing by remember { mutableStateOf<String?>(null) }
    PageColumn {
        Text("到期生成待确认记录，确认后记账；系统省电设置可能延迟提醒。", style = MaterialTheme.typography.bodySmall)
        Button({ editing = "" }, Modifier.fillMaxWidth()) { Text("新增周期规则") }
        rows.forEach { rule -> InfoCard(rule.draft.note.ifBlank { kindLabel(rule.draft.kind) }, "${rule.draft.amount.display()} · 下次 ${dateText(rule.nextAt)}\n每 ${rule.interval} ${unitLabel(rule.unit)} · 已生成 ${rule.executed} 次" + if (rule.enabled) "" else " · 已暂停", { editing = rule.id }) }
        if (rows.isEmpty()) EmptyState("适合房租、工资等定期记录")
    }
    editing?.let { id ->
        val old = rows.find { it.id == id }; val initial = old?.draft
        var kind by remember(id) { mutableStateOf(initial?.kind ?: EntryKind.EXPENSE) }; var amount by remember(id) { mutableStateOf(initial?.amount?.decimal() ?: "") }; var note by remember(id) { mutableStateOf(initial?.note ?: "") }
        var currencyCode by remember(id) { mutableStateOf(initial?.amount?.currency?.value ?: book.currency) }
        val currency = CurrencyCode(if (kind == EntryKind.TRANSFER) book.currency else currencyCode)
        var account by remember(id) { mutableStateOf(initial?.accountId) }; var other by remember(id) { mutableStateOf(initial?.otherAccountId) }; var category by remember(id) { mutableStateOf(initial?.categoryId) }
        var interval by remember(id) { mutableStateOf((old?.interval ?: 1).toString()) }; var unit by remember(id) { mutableStateOf(old?.unit ?: "MONTH") }; var next by remember(id) { mutableStateOf(dateText(old?.nextAt ?: System.currentTimeMillis())) }
        var until by remember(id) { mutableStateOf(old?.until?.let(::dateText) ?: "") }; var count by remember(id) { mutableStateOf(old?.maxCount?.toString() ?: "") }; var enabled by remember(id) { mutableStateOf(old?.enabled ?: true) }; var error by remember { mutableStateOf<String?>(null) }
        FormDialog("周期记账规则", { editing = null }) {
            ChoiceField("记账类型", listOf(EntryKind.EXPENSE, EntryKind.INCOME, EntryKind.TRANSFER), kind, ::kindLabel, { kind = it })
            if (kind != EntryKind.TRANSFER) ChoiceField("记录币种", SupportedCurrencies, currencyCode, { it }, { currencyCode = it; account = null; other = null })
            TextFieldRow("金额 · ${currency.value}", amount, { amount = it }, numeric = true); TextFieldRow("备注", note, { note = it })
            val eligible = accounts.filter { !it.archived && it.currency == currency.value && it.kind !in listOf(AccountKind.RECEIVABLE, AccountKind.PAYABLE) }
            ChoiceField("账户", eligible, eligible.find { it.id == account }, Account::name, { account = it.id })
            if (kind == EntryKind.TRANSFER) ChoiceField("转入账户", eligible.filter { it.id != account }, eligible.find { it.id == other }, Account::name, { other = it.id })
            else ChoiceField("分类", categories.filter { !it.archived && it.income == (kind == EntryKind.INCOME) }, categories.find { it.id == category }, Category::name, { category = it.id })
            TextFieldRow("间隔", interval, { interval = it }, numeric = true); ChoiceField("周期", listOf("DAY", "WEEK", "MONTH", "YEAR"), unit, ::unitLabel, { unit = it })
            TextFieldRow("下次日期", next, { next = it }, supporting = "yyyy-MM-dd"); TextFieldRow("结束日期（可选）", until, { until = it }, supporting = "yyyy-MM-dd"); TextFieldRow("总生成次数（可选）", count, { count = it }, numeric = true)
            ToggleRow("启用规则", "可暂停后再次恢复", enabled, { enabled = it })
            InlineError(error)
            Button({ try {
                val parsed = Money.parse(amount, currency); val first = parseDay(next); val end = until.takeIf(String::isNotBlank)?.let { parseDay(LocalDate.parse(it).plusDays(1).toString()) - 1 }; val max = count.takeIf(String::isNotBlank)?.toInt(); val every = interval.toInt()
                require(parsed.minor > 0 && every > 0 && (max == null || max > 0)) { "金额、间隔和次数必须大于零" }; require(end == null || end >= first) { "结束日期不能早于下次日期" }
                if (kind == EntryKind.TRANSFER) require(account != null && other != null && account != other) { "周期转账需选择两个同币种账户" }
                val draft = (initial ?: EntryDraft(bookId = book.id, amount = parsed)).copy(bookId = book.id, kind = kind, amount = parsed, accountId = account, otherAccountId = if (kind == EntryKind.TRANSFER) other else null, categoryId = category, note = note.trim(), occurredAt = first)
                vm.run(after = { editing = null }) { vm.repository.saveRecurring(RecurringRule(old?.id ?: newId(), draft, every, unit, first, end, max, old?.executed ?: 0, enabled)) }
            } catch (failure: Exception) { error = failure.message } }, Modifier.fillMaxWidth()) { Text("保存") }
            if (old != null) OutlinedButton({ vm.run(after = { editing = null }) { vm.repository.deleteRecurring(old.id) } }, Modifier.fillMaxWidth()) { Text("删除周期规则") }
        }
    }
}

private fun unitLabel(unit: String): String = when (unit) { "DAY" -> "天"; "WEEK" -> "周"; "YEAR" -> "年"; else -> "月" }
