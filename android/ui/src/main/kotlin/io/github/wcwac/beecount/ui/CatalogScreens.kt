package io.github.wcwac.beecount.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.wcwac.beecount.domain.*

@Composable
internal fun CatalogScreen(vm: NativeViewModel, nav: Navigator, screen: Screen, books: List<Book>, accounts: List<Account>, categories: List<Category>, tags: List<Tag>) {
    var editing by remember(screen) { mutableStateOf<String?>(null) }
    var showArchived by remember(screen) { mutableStateOf(false) }
    PageColumn {
        Button({ editing = "" }, modifier = Modifier.fillMaxWidth()) { Text("新增${when (screen) { Screen.BOOKS -> "账本"; Screen.ACCOUNTS -> "账户"; Screen.CATEGORIES -> "分类"; else -> "标签" }}") }
        if (screen != Screen.TAGS) ToggleRow("显示已归档项目", "归档不会删除历史记录", showArchived, { showArchived = it })
        when (screen) {
            Screen.BOOKS -> books.filter { showArchived || !it.archived }.forEach { book -> InfoCard(book.name, book.currency + if (book.archived) " · 已归档" else "", { editing = book.id }) }
            Screen.ACCOUNTS -> accounts.filter { showArchived || !it.archived }.forEach { account -> InfoCard(account.name, "${accountLabel(account.kind)} · ${money(account.balance, account.currency)}" + if (account.archived) " · 已归档" else "", { editing = account.id }, trailing = { TextButton({ nav.open(Screen.ACCOUNT_DETAIL, account.id) }) { Text("流水") } }) }
            Screen.CATEGORIES -> categories.filter { showArchived || !it.archived }.forEach { category -> InfoCard(category.name, (if (category.income) "收入" else "支出") + (category.parentId?.let { id -> " · ${categories.find { it.id == id }?.name.orEmpty()}" } ?: "") + if (category.archived) " · 已归档" else "", { editing = category.id }) }
            Screen.TAGS -> tags.forEach { tag -> InfoCard(tag.name, "", { editing = tag.id }) }
            else -> Unit
        }
    }
    editing?.let { id ->
        when (screen) {
            Screen.BOOKS -> BookForm(vm, books.find { it.id == id }) { editing = null }
            Screen.ACCOUNTS -> AccountForm(vm, accounts.find { it.id == id }) { editing = null }
            Screen.CATEGORIES -> CategoryForm(vm, categories.find { it.id == id }, categories) { editing = null }
            Screen.TAGS -> TagForm(vm, tags.find { it.id == id }) { editing = null }
            else -> Unit
        }
    }
}

@Composable
internal fun FormDialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    if (!LocalUiWindowsAllowed.current) return
    Dialog(onDismissRequest = onDismiss) { Surface(shape = MaterialTheme.shapes.large) { PageColumn {
        Text(title, style = MaterialTheme.typography.titleLarge)
        content()
        TextButton(onDismiss, Modifier.fillMaxWidth()) { Text("关闭") }
    } } }
}

@Composable
private fun BookForm(vm: NativeViewModel, old: Book?, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(old?.name ?: "") }; var currency by remember { mutableStateOf(old?.currency ?: "CNY") }; var error by remember { mutableStateOf<String?>(null) }
    FormDialog(if (old == null) "新增账本" else "编辑账本", onDismiss) {
        TextFieldRow("账本名称", name, { name = it })
        if (old == null) TextFieldRow("币种代码", currency, { currency = it.uppercase() }, supporting = "例如 CNY、USD、JPY；创建后币种不可改变") else Text("币种：${old.currency}")
        InlineError(error)
        Button({ try { require(name.isNotBlank()) { "请输入名称" }; CurrencyCode(currency); vm.run(after = onDismiss) { vm.repository.saveBook(Book(old?.id ?: newId(), name.trim(), currency, old?.archived ?: false)) } } catch (failure: Exception) { error = failure.message } }, Modifier.fillMaxWidth()) { Text("保存") }
        old?.let { OutlinedButton({ vm.run(after = onDismiss) { vm.repository.archiveBook(it.id, !it.archived) } }, Modifier.fillMaxWidth()) { Text(if (it.archived) "取消归档" else "归档账本") } }
    }
}

@Composable
private fun AccountForm(vm: NativeViewModel, old: Account?, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(old?.name ?: "") }; var currency by remember { mutableStateOf(old?.currency ?: "CNY") }; var kind by remember { mutableStateOf(old?.kind ?: AccountKind.CASH) }
    var balance by remember { mutableStateOf("0") }; var statement by remember { mutableStateOf((old?.statementDay ?: 1).toString()) }; var repayment by remember { mutableStateOf((old?.repaymentDay ?: 10).toString()) }; var error by remember { mutableStateOf<String?>(null) }
    var adjustment by remember { mutableStateOf("") }
    FormDialog(if (old == null) "新增账户" else "编辑账户", onDismiss) {
        TextFieldRow("账户名称", name, { name = it })
        if (old == null) { ChoiceField("账户类型", AccountKind.entries, kind, ::accountLabel, { kind = it }); TextFieldRow("币种代码", currency, { currency = it.uppercase() }); TextFieldRow("期初余额", balance, { balance = it }, numeric = true, supporting = "欠款使用负数，例如 -1000") }
        else Text("${accountLabel(old.kind)} · 当前余额 ${money(old.balance, old.currency)}")
        if (kind == AccountKind.CREDIT) { TextFieldRow("账单日", statement, { statement = it }, numeric = true); TextFieldRow("还款日", repayment, { repayment = it }, numeric = true) }
        InlineError(error)
        Button({ try { require(name.isNotBlank()) { "请输入账户名称" }; val cur = CurrencyCode(currency); val initial = if (old == null) Money.parse(balance, cur).minor else null; val statementDay = statement.toInt(); val repaymentDay = repayment.toInt(); require(statementDay in 1..31 && repaymentDay in 1..31) { "日期需在1至31日之间" }; vm.run(after = onDismiss) { vm.repository.saveAccount(Account(old?.id ?: newId(), name.trim(), currency, kind, old?.balance ?: 0, old?.archived ?: false, statementDay, repaymentDay), initial) } } catch (failure: Exception) { error = failure.message } }, Modifier.fillMaxWidth()) { Text("保存") }
        if (old != null && !old.archived) {
            TextFieldRow("余额增减 · ${old.currency}", adjustment, { adjustment = it }, numeric = true, supporting = "正数增加、负数减少；不计普通收支与预算")
            OutlinedButton({ try {
                val amount = Money.parse(adjustment, CurrencyCode(old.currency)); require(amount.minor != 0L) { "调整金额不能为零" }
                val book = requireNotNull(vm.books.value.firstOrNull { !it.archived && it.currency == old.currency } ?: vm.books.value.firstOrNull { !it.archived }) { "请先创建可用账本" }
                vm.commit(FinancialCommand.Save(EntryDraft(bookId = book.id, kind = EntryKind.ADJUSTMENT, amount = amount, accountId = old.id, note = "余额对账调整", excludedFromTotals = true, excludedFromBudget = true)), onDismiss)
            } catch (failure: Exception) { error = failure.message } }, Modifier.fillMaxWidth()) { Text("保存余额调整记录") }
        }
        old?.let { OutlinedButton({ vm.run(after = onDismiss) { vm.repository.archiveAccount(it.id, !it.archived) } }, Modifier.fillMaxWidth()) { Text(if (it.archived) "取消归档" else "归档账户") } }
    }
}

@Composable
private fun CategoryForm(vm: NativeViewModel, old: Category?, all: List<Category>, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(old?.name ?: "") }; var income by remember { mutableStateOf(old?.income ?: false) }; var parent by remember { mutableStateOf(old?.parentId) }
    FormDialog(if (old == null) "新增分类" else "编辑分类", onDismiss) {
        TextFieldRow("分类名称", name, { name = it })
        ToggleRow("收入分类", if (income) "用于收入" else "用于支出", income, { income = it; parent = null })
        val eligible = all.filter { !it.archived && it.income == income && it.parentId == null && it.id != old?.id }
        ChoiceField("父分类", eligible, eligible.find { it.id == parent }, Category::name, { parent = it.id }, "顶级分类")
        if (parent != null) TextButton({ parent = null }) { Text("设为顶级分类") }
        Button({ vm.run(after = onDismiss) { require(name.isNotBlank()) { "请输入分类名称" }; vm.repository.saveCategory(Category(old?.id ?: newId(), name.trim(), income, parent, old?.archived ?: false)) } }, Modifier.fillMaxWidth()) { Text("保存") }
        old?.let { OutlinedButton({ vm.run(after = onDismiss) { vm.repository.archiveCategory(it.id, !it.archived) } }, Modifier.fillMaxWidth()) { Text(if (it.archived) "取消归档" else "归档分类") } }
    }
}

@Composable
private fun TagForm(vm: NativeViewModel, old: Tag?, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(old?.name ?: "") }; var deleting by remember { mutableStateOf(false) }
    FormDialog(if (old == null) "新增标签" else "编辑标签", onDismiss) {
        TextFieldRow("标签名称", name, { name = it })
        Button({ vm.run(after = onDismiss) { require(name.isNotBlank()) { "请输入标签名称" }; vm.repository.saveTag(Tag(old?.id ?: newId(), name.trim())) } }, Modifier.fillMaxWidth()) { Text("保存") }
        if (old != null) OutlinedButton({ deleting = true }, Modifier.fillMaxWidth()) { Text("删除标签") }
    }
    if (deleting && old != null) Confirmation("删除标签", "已有账单将移除这个标签，账单本身保留。", { deleting = false }) { vm.run(after = onDismiss) { vm.repository.deleteTag(old.id) } }
}
