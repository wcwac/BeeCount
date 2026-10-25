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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.wcwac.beecount.domain.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import java.time.*

@Composable
internal fun LedgerScreen(vm: NativeViewModel, nav: Navigator, books: List<Book>, book: Book?, prefs: UserPreferences, pending: Int) {
    if (book == null) { EmptyState("请先创建账本", "创建账本", { nav.open(Screen.BOOKS) }); return }
    var query by rememberSaveable { mutableStateOf("") }
    var calendar by rememberSaveable { mutableStateOf(false) }
    var selectedDay by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    val range = remember(monthOffset) { monthRange(monthOffset) }
    val summary by remember(book.id, range) { vm.repository.observeSummary(book.id, range.first, range.second) }.collectAsStateWithLifecycle(Summary())
    LaunchedEffect(book.id, query, calendar, selectedDay) {
        delay(180)
        val day = LocalDate.parse(selectedDay)
        vm.filter.value = HistoryFilter(bookId = book.id, query = query,
            start = if (calendar) parseDay(selectedDay) else null,
            endExclusive = if (calendar) parseDay(day.plusDays(1).toString()) else null)
    }
    val entries = vm.history.collectAsLazyPagingItems()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 92.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceField("当前账本", books.filter { !it.archived }, book, Book::name, { selected -> vm.preferences { it.copy(currentBookId = selected.id) } })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton({ monthOffset-- }) { Text("上月") }
                    Text(dateText(range.first).take(7), style = MaterialTheme.typography.titleMedium)
                    TextButton({ monthOffset++ }) { Text("下月") }
                }
                SummaryCard(summary, book.currency, prefs.hideAmounts)
                if (pending > 0) InfoCard("$pending 条待确认", "查看自动记账采集结果", { nav.open(Screen.INBOX) })
                TextFieldRow("搜索商户、备注或分类", query, { query = it })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!calendar, { calendar = false }, { Text("全部流水") })
                    FilterChip(calendar, { calendar = true }, { Text("日历") })
                }
                if (calendar) {
                    DaySelector(selectedDay, { selectedDay = it })
                    Text("${selectedDay} 的记录", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        items(entries.itemCount, key = entries.itemKey { it.draft.id }) { index -> entries[index]?.let { row ->
            val previous = if (index > 0) entries.peek(index - 1) else null
            if (previous == null || dateText(previous.draft.occurredAt) != dateText(row.draft.occurredAt)) Text(dateText(row.draft.occurredAt), Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TransactionRow(row, prefs.hideAmounts) { nav.open(Screen.DETAIL, row.draft.id) }
        } }
        item { PagingStatus(entries.loadState.refresh, entries.itemCount, entries::retry) }
        if (entries.loadState.append is LoadState.Loading) item { LinearProgressIndicator(Modifier.fillMaxWidth().padding(16.dp)) }
        if (entries.loadState.append is LoadState.Error) item { EmptyState("较早记录加载失败", "重试", entries::retry) }
    }
}

@Composable
internal fun DaySelector(selected: String, onSelect: (String) -> Unit) {
    val day = LocalDate.parse(selected)
    val start = day.withDayOfMonth(1)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton({ onSelect(day.minusMonths(1).withDayOfMonth(1).toString()) }) { Text("上一月") }
        TextButton({ onSelect(LocalDate.now().toString()) }) { Text("今天") }
        TextButton({ onSelect(day.plusMonths(1).withDayOfMonth(1).toString()) }) { Text("下一月") }
    }
    Row(Modifier.fillMaxWidth()) { listOf("一", "二", "三", "四", "五", "六", "日").forEach { Text(it, Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center) } }
    val blanks = start.dayOfWeek.value - 1
    val count = start.lengthOfMonth()
    repeat((blanks + count + 6) / 7) { week ->
        Row(Modifier.fillMaxWidth()) {
            repeat(7) { column ->
                val value = week * 7 + column - blanks + 1
                if (value in 1..count) {
                    val item = start.withDayOfMonth(value)
                    TextButton({ onSelect(item.toString()) }, Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) { Text(value.toString(), color = if (item == day) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
                } else Spacer(Modifier.weight(1f).height(48.dp))
            }
        }
    }
}

@Composable
internal fun PagingStatus(state: LoadState, count: Int, retry: () -> Unit) {
    when { state is LoadState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth().padding(16.dp)); state is LoadState.Error -> EmptyState(state.error.message ?: "读取失败", "重试", retry); count == 0 -> EmptyState("这里还没有记录") }
}

@Composable
internal fun HistoryScreen(vm: NativeViewModel, nav: Navigator, destination: Destination, book: Book?, prefs: UserPreferences) {
    var query by rememberSaveable(destination) { mutableStateOf("") }
    LaunchedEffect(destination, book?.id, query) { delay(180); vm.filter.value = HistoryFilter(bookId = if (destination.screen == Screen.ACCOUNT_DETAIL) null else book?.id, accountId = destination.id.takeIf { destination.screen == Screen.ACCOUNT_DETAIL }, query = query, deleted = destination.screen == Screen.TRASH, budgetId = destination.id.takeIf { destination.screen == Screen.BUDGET_RECORDS }) }
    val entries = vm.history.collectAsLazyPagingItems()
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Box(Modifier.padding(16.dp)) { TextFieldRow("搜索记录", query, { query = it }) } }
        items(entries.itemCount, key = entries.itemKey { it.draft.id }) { index -> entries[index]?.let { TransactionRow(it, prefs.hideAmounts) { nav.open(Screen.DETAIL, it.draft.id) } } }
        item { PagingStatus(entries.loadState.refresh, entries.itemCount, entries::retry) }
    }
}

@Composable
internal fun AssetsScreen(vm: NativeViewModel, nav: Navigator, accounts: List<Account>, prefs: UserPreferences) {
    PageColumn {
        accounts.filter { !it.archived }.groupBy { it.currency }.forEach { (currency, values) ->
            val asset = values.sumOf { it.balance.coerceAtLeast(0) }; val debt = values.sumOf { -it.balance.coerceAtMost(0) }
            InfoCard("净资产 · $currency", if (prefs.hideAmounts) "••••" else "${money(asset - debt, currency)}\n资产 ${money(asset, currency)} · 负债 ${money(debt, currency)}")
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(EntryKind.TRANSFER, EntryKind.CREDIT_PAYMENT, EntryKind.LEND, EntryKind.BORROW).forEach { kind -> OutlinedButton({ nav.open(Screen.EDITOR, kind = kind.name) }) { Text(kindLabel(kind)) } }
        }
        accounts.filter { !it.archived }.forEach { account -> InfoCard(account.name, "${accountLabel(account.kind)} · ${if (prefs.hideAmounts) "••••" else money(account.balance, account.currency)}", { nav.open(Screen.ACCOUNT_DETAIL, account.id) }) }
        if (accounts.none { !it.archived }) EmptyState("添加现金、银行卡或信用账户", "管理账户", { nav.open(Screen.ACCOUNTS) })
        SectionTitle("资产相关")
        listOf(Screen.ACCOUNTS, Screen.CREDIT, Screen.LOANS, Screen.INVESTMENTS).forEach { screen -> InfoCard(screen.label, "", { nav.open(screen) }) }
        Spacer(Modifier.height(70.dp))
    }
}

@Composable
internal fun StatisticsScreen(vm: NativeViewModel, nav: Navigator, book: Book?, prefs: UserPreferences) {
    if (book == null) return
    var offset by rememberSaveable { mutableIntStateOf(0) }
    var mode by rememberSaveable { mutableStateOf("月") }
    var startText by rememberSaveable { mutableStateOf(dateText(monthRange().first)) }
    var endText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val customRange = remember(startText, endText) { runCatching { parseDay(startText) to parseDay(LocalDate.parse(endText).plusDays(1).toString()) }.getOrNull()?.takeIf { it.second > it.first } }
    val range = remember(mode, offset, startText, endText) {
        when (mode) {
            "年" -> { val start = LocalDate.now().plusYears(offset.toLong()).withDayOfYear(1); parseDay(start.toString()) to parseDay(start.plusYears(1).toString()) }
            "近7天" -> parseDay(LocalDate.now().minusDays(6).toString()) to parseDay(LocalDate.now().plusDays(1).toString())
            "自定义" -> customRange ?: (0L to 0L)
            else -> monthRange(offset)
        }
    }
    val summary by remember(book.id, range) { vm.repository.observeSummary(book.id, range.first, range.second) }.collectAsStateWithLifecycle(Summary())
    val chart by remember(book.id, range) { vm.repository.observeChart(book.id, range.first, range.second) }.collectAsStateWithLifecycle(emptyList())
    val categories by remember(book.id, range) { vm.repository.observeCategoryTotals(book.id, range.first, range.second) }.collectAsStateWithLifecycle(emptyList())
    val periodNote by remember(book.id, range) { vm.repository.observePeriodNote(book.id, range.first, range.second) }.collectAsStateWithLifecycle("")
    var editingNote by remember { mutableStateOf(false) }
    PageColumn {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("近7天", "月", "年", "自定义").forEach { title -> FilterChip(mode == title, { mode = title; offset = 0 }, { Text(title) }) } }
        if (mode in listOf("月", "年")) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { TextButton({ offset-- }) { Text("上一$mode") }; Text(dateText(range.first)); TextButton({ offset++ }) { Text("下一$mode") } }
        if (mode == "自定义") { TextFieldRow("开始日期", startText, { startText = it }, supporting = "yyyy-MM-dd"); TextFieldRow("结束日期", endText, { endText = it }, supporting = "yyyy-MM-dd") }
        if (mode == "自定义" && customRange == null) { InlineError("请输入有效日期，结束日期不能早于开始日期"); return@PageColumn }
        Text("仅统计 ${book.currency} 币种，不折算其他币种。", style = MaterialTheme.typography.bodySmall)
        SummaryCard(summary, book.currency, prefs.hideAmounts)
        if (!prefs.hideAmounts) {
            TrendChart(chart, book.currency)
            SectionTitle("支出分类")
            if (categories.isEmpty()) EmptyState("所选期间暂无分类支出") else CategoryChart(categories, book.currency)
        }
        InfoCard("预算", "月预算、分类预算与自定义期间", { nav.open(Screen.BUDGETS) })
        InfoCard("存钱目标", "手工进度与自动进度", { nav.open(Screen.GOALS) })
        InfoCard("本期总结", periodNote.ifBlank { "记录这个期间的收支心得" }, { editingNote = true })
    }
    if (editingNote) {
        var text by remember(book.id, range) { mutableStateOf(periodNote) }
        FormDialog("本期总结", { editingNote = false }) { TextFieldRow("总结", text, { text = it }, singleLine = false); Button({ vm.run(after = { editingNote = false }) { vm.repository.savePeriodNote(book.id, range.first, range.second, text) } }, Modifier.fillMaxWidth()) { Text("保存") } }
    }
}
