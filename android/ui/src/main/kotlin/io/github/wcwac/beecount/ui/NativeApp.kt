package io.github.wcwac.beecount.ui

import androidx.compose.foundation.layout.*
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.wcwac.beecount.data.NativeRepository
import io.github.wcwac.beecount.domain.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
internal enum class Screen(val label: String) {
    LEDGER("账单"), ASSETS("资产"), STATS("统计"), MORE("更多"),
    EDITOR("记一笔"), DETAIL("账单详情"), BOOKS("账本管理"), ACCOUNTS("账户管理"), ACCOUNT_DETAIL("账户流水"), CATEGORIES("分类管理"), TAGS("标签管理"),
    CAPTURE("自动记账"), APPS("采集应用"), INBOX("待确认记录"), CANDIDATE("确认记录"), RULES("分类规则"), MAPPINGS("账户映射"), DIAGNOSTICS("采集诊断"),
    BUDGETS("预算"), BUDGET_RECORDS("预算计入明细"), GOALS("存钱目标"), PLANS("周期记账"), INVESTMENTS("投资持仓"), INVESTMENT_DETAIL("持仓详情"),
    DATA("数据与备份"), SETTINGS("外观与隐私"), TRASH("回收站"), ATTACHMENTS("账单附件"), LOANS("借贷与报销"), CREDIT("信用账期"), WIDGETS("桌面与快捷入口"), HELP("帮助与关于")
}
@Serializable
internal data class Destination(val screen: Screen, val id: String = "", val kind: String = "", val parentId: String = "", val candidateId: String = "")
internal class Navigator(private val controller: NavHostController) {
    fun open(screen: Screen, id: String = "", kind: String = "", parentId: String = "", candidateId: String = "") { controller.navigate(Destination(screen, id, kind, parentId, candidateId)) }
    fun root(screen: Screen) {
        if (controller.currentBackStackEntry?.toRoute<Destination>()?.screen == screen) return
        controller.navigate(Destination(screen)) { popUpTo(controller.graph.id); launchSingleTop = true }
    }
    fun back() { controller.popBackStack() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeApp(repository: NativeRepository, platform: NativePlatformActions, initialCandidateId: String? = null, externalRoute: StateFlow<String?>? = null, windowsAllowed: Boolean = true) {
    val vm: NativeViewModel = viewModel(factory = NativeViewModel.factory(repository, platform))
    SideEffect { vm.platform = platform }
    val prefs by platform.preferences.collectAsStateWithLifecycle()
    val books by vm.books.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val tags by vm.tags.collectAsStateWithLifecycle()
    val pending by vm.pending.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val controller = rememberNavController()
    val nav = remember(controller) { Navigator(controller) }
    val currentEntry by controller.currentBackStackEntryAsState()
    val tabStates = rememberSaveableStateHolder()
    val snackbar = remember { SnackbarHostState() }
    val book = books.find { it.id == prefs.currentBookId && !it.archived } ?: books.firstOrNull { !it.archived }
    val destination = currentEntry?.toRoute<Destination>() ?: Destination(Screen.LEDGER)
    val isRoot = destination.screen in listOf(Screen.LEDGER, Screen.ASSETS, Screen.STATS, Screen.MORE)
    val routeRequest = externalRoute?.collectAsStateWithLifecycle()?.value
    LaunchedEffect(routeRequest) {
        // The host attaches a request token so repeated shortcut actions are distinct events.
        val requestedRoute = routeRequest?.substringBefore('#')
        when {
            requestedRoute == "editor" -> nav.open(Screen.EDITOR)
            requestedRoute == "inbox" -> nav.open(Screen.INBOX)
            requestedRoute == "capture" -> nav.open(Screen.CAPTURE)
            requestedRoute == "assets" -> nav.root(Screen.ASSETS)
            requestedRoute == "financial" -> nav.open(Screen.LOANS)
            requestedRoute == "instalments" -> nav.open(Screen.CREDIT)
            requestedRoute?.startsWith("candidate-edit:") == true -> nav.open(Screen.EDITOR, candidateId = requestedRoute.substringAfter(':'))
            requestedRoute?.startsWith("candidate:") == true -> nav.open(Screen.CANDIDATE, requestedRoute.substringAfter(':'))
        }
    }
    LaunchedEffect(initialCandidateId) { if (!initialCandidateId.isNullOrBlank()) nav.open(Screen.CANDIDATE, initialCandidateId) }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); if (vm.message.value == it) vm.message.value = null } }
    CompositionLocalProvider(LocalUiWindowsAllowed provides windowsAllowed) {
    NativeTheme(prefs.theme) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(destination.screen.label) }, navigationIcon = { if (!isRoot) TextButton(onClick = nav::back) { Text("返回") } }, actions = { if (busy) CircularProgressIndicator(Modifier.size(22.dp).padding(2.dp), strokeWidth = 2.dp) }) },
            bottomBar = {
                if (isRoot) NavigationBar {
                    listOf(Screen.LEDGER to "账", Screen.ASSETS to "资", Screen.STATS to "统", Screen.MORE to "•••").forEach { (screen, icon) ->
                        NavigationBarItem(selected = destination.screen == screen, onClick = { nav.root(screen) }, icon = { Text(icon) }, label = { Text(screen.label) })
                    }
                }
            },
            floatingActionButton = {
                if (destination.screen in listOf(Screen.LEDGER, Screen.ASSETS)) FloatingActionButton(onClick = { if (destination.screen == Screen.ASSETS) nav.open(Screen.ACCOUNTS) else nav.open(Screen.EDITOR) }) { Text(if (destination.screen == Screen.ASSETS) "账户" else "记账", Modifier.padding(horizontal = 12.dp)) }
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                NavHost(navController = controller, startDestination = Destination(Screen.LEDGER), enterTransition = { EnterTransition.None }, exitTransition = { ExitTransition.None }) {
                composable<Destination> { backStackEntry ->
                val page = backStackEntry.toRoute<Destination>()
                val pageContent: @Composable () -> Unit = {
                if (book == null && books.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
                } else when (page.screen) {
                    Screen.LEDGER -> LedgerScreen(vm, nav, books, book, prefs, pending.size)
                    Screen.ASSETS -> AssetsScreen(vm, nav, accounts, prefs)
                    Screen.STATS -> StatisticsScreen(vm, nav, book, prefs)
                    Screen.MORE -> MoreScreen(nav, pending.size)
                    Screen.EDITOR -> EntryEditor(vm, nav, page, books, book, accounts, categories, tags)
                    Screen.DETAIL -> EntryDetail(vm, nav, page.id, accounts)
                    Screen.BOOKS, Screen.ACCOUNTS, Screen.CATEGORIES, Screen.TAGS -> CatalogScreen(vm, nav, page.screen, books, accounts, categories, tags)
                    Screen.ACCOUNT_DETAIL, Screen.TRASH, Screen.BUDGET_RECORDS -> HistoryScreen(vm, nav, page, book, prefs)
                    Screen.CAPTURE, Screen.APPS, Screen.DIAGNOSTICS -> CaptureSettingsScreen(vm, nav, page.screen, books)
                    Screen.INBOX -> InboxScreen(vm, nav, pending)
                    Screen.CANDIDATE -> CandidateScreen(vm, nav, page.id, books, accounts, categories)
                    Screen.RULES, Screen.MAPPINGS -> RulesScreen(vm, page.screen, accounts, categories)
                    Screen.BUDGETS -> BudgetScreen(vm, nav, book, categories)
                    Screen.GOALS -> GoalsScreen(vm, book, accounts)
                    Screen.PLANS -> PlansScreen(vm, book, accounts, categories)
                    Screen.INVESTMENTS -> InvestmentsScreen(vm, nav, book)
                    Screen.INVESTMENT_DETAIL -> InvestmentDetailScreen(vm, book, page.id)
                    Screen.LOANS -> FinancialHistoryScreen(vm, nav, book, accounts)
                    Screen.CREDIT -> CreditScreen(vm, nav, accounts)
                    Screen.ATTACHMENTS -> AttachmentsScreen(vm)
                    Screen.DATA, Screen.SETTINGS, Screen.WIDGETS, Screen.HELP -> UtilityScreen(vm, nav, page.screen, book)
                }
                }
                if (page.screen in listOf(Screen.LEDGER, Screen.ASSETS, Screen.STATS, Screen.MORE)) tabStates.SaveableStateProvider(page.screen.name) { pageContent() } else pageContent()
                }
                }
            }
        }
    }
    }
}

@Composable
internal fun MoreScreen(nav: Navigator, pending: Int) {
    PageColumn {
        InfoCard("自动记账", if (pending > 0) "$pending 条记录等待确认" else "权限、应用与采集设置", { nav.open(Screen.CAPTURE) })
        listOf("账务管理" to listOf(Screen.BOOKS, Screen.CATEGORIES, Screen.TAGS, Screen.PLANS, Screen.BUDGETS, Screen.GOALS), "数据与设置" to listOf(Screen.DATA, Screen.ATTACHMENTS, Screen.TRASH, Screen.SETTINGS, Screen.WIDGETS, Screen.HELP)).forEach { (title, items) ->
            SectionTitle(title)
            items.forEach { screen -> InfoCard(screen.label, "", { nav.open(screen) }) }
        }
    }
}
