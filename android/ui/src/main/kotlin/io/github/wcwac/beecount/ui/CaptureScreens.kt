package io.github.wcwac.beecount.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.wcwac.beecount.domain.*

internal val SupportedApps = listOf(
    "支付宝" to "com.eg.android.AlipayGphone", "微信" to "com.tencent.mm", "云闪付" to "com.unionpay", "京东金融" to "com.jd.jrapp",
    "京东" to "com.jingdong.app.mall", "拼多多" to "com.xunmeng.pinduoduo", "美团" to "com.sankuai.meituan", "大众点评" to "com.dianping.v1", "美团外卖" to "com.sankuai.meituan.takeoutnew",
    "1688" to "com.alibaba.wireless", "闲鱼" to "com.taobao.idlefish", "盒马" to "com.wudaokou.hippo", "饿了么" to "me.ele", "华为钱包" to "com.huawei.wallet", "建行生活" to "com.ccb.longjiLife",
    "快手" to "com.smile.gifmaker", "快手极速版" to "com.kuaishou.nebula", "抖音" to "com.ss.android.ugc.aweme", "抖音商城" to "com.ss.android.ugc.livelite", "抖音极速版" to "com.ss.android.ugc.aweme.lite", "抖音精选" to "com.ss.android.yumme.video", "淘宝" to "com.taobao.taobao",
)

@Composable
internal fun CaptureSettingsScreen(vm: NativeViewModel, nav: Navigator, screen: Screen, books: List<Book>) {
    val prefs by vm.platform.preferences.collectAsStateWithLifecycle()
    val status by vm.platform.captureStatus.collectAsStateWithLifecycle()
    val permissions by vm.platform.permissions.collectAsStateWithLifecycle()
    val config = prefs.capture
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val listener = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) vm.platform.refreshPermissions() }
        owner.lifecycle.addObserver(listener); vm.platform.refreshPermissions()
        onDispose { owner.lifecycle.removeObserver(listener) }
    }
    PageColumn {
        when (screen) {
            Screen.CAPTURE -> {
                ToggleRow("自动记账", "只读取所选应用中的支付信息", config.enabled, { enabled -> vm.preferences { it.copy(capture = it.capture.copy(enabled = enabled)) } })
                InfoCard("无障碍授权", if (permissions.accessibilityGranted) "已授权" else "需要授权", vm.platform::openAccessibilitySettings)
                InfoCard("服务连接", if (status.connected) "已连接" else "尚未连接，请检查系统无障碍服务")
                InfoCard("悬浮窗权限", if (permissions.overlayGranted) "已授权" else "需要授权以显示支付确认", vm.platform::openOverlaySettings)
                InfoCard("通知权限", if (permissions.notificationsGranted) "已允许" else "点此允许采集提示", vm.platform::requestNotifications)
                InfoCard("后台运行", if (permissions.batteryUnrestricted) "已允许后台运行" else "检查系统省电设置", vm.platform::openBatterySettings)
                ChoiceField("目标账本", books.filter { !it.archived }, books.find { it.id == config.bookId }, Book::name, { book -> vm.preferences { it.copy(capture = it.capture.copy(bookId = book.id)) } })
                ToggleRow("显示确认浮窗", "记录先保存为待确认，浮窗显示失败时仍可在列表处理", config.overlayEnabled, { value -> vm.preferences { it.copy(capture = it.capture.copy(overlayEnabled = value)) } })
                ToggleRow("倒计时自动保存", "仅证据充分且账户明确的记录可自动保存；操作浮窗会停止倒计时", config.autoSave, { value -> vm.preferences { it.copy(capture = it.capture.copy(autoSave = value)) } })
                OutlinedButton(vm.platform::previewOverlay, Modifier.fillMaxWidth()) { Text("预览确认浮窗") }
                listOf(Screen.APPS, Screen.INBOX, Screen.RULES, Screen.MAPPINGS, Screen.DIAGNOSTICS).forEach { item -> InfoCard(item.label, if (item == Screen.APPS) "已选择 ${config.selectedPackages.size} 个应用" else "", { nav.open(item) }) }
            }
            Screen.APPS -> {
                Text("应用界面变化可能影响读取。勾选仅开启监听，并不代表已验证所有客户端版本。", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton({ vm.preferences { it.copy(capture = it.capture.copy(selectedPackages = SupportedApps.map { pair -> pair.second }.toSet())) } }) { Text("全选") }
                    TextButton({ vm.preferences { it.copy(capture = it.capture.copy(selectedPackages = emptySet())) } }) { Text("清空") }
                }
                SupportedApps.forEach { (name, packageName) -> ToggleRow(name, packageName, packageName in config.selectedPackages, { enabled -> vm.preferences { it.copy(capture = it.capture.copy(selectedPackages = if (enabled) it.capture.selectedPackages + packageName else it.capture.selectedPackages - packageName)) } }) }
            }
            Screen.DIAGNOSTICS -> {
                InfoCard("授权与连接", "总开关 ${config.enabled}\n无障碍授权 ${permissions.accessibilityGranted}\n服务连接 ${status.connected}\n悬浮窗权限 ${permissions.overlayGranted}")
                InfoCard("采集进度", "事件 ${status.events} · 读取 ${status.reads}\n提取 ${status.proposals} · 候选 ${status.candidates} · 跳过 ${status.skipped}")
                InfoCard("最近阶段", "${status.stage}\n${status.lastResult}\n${status.lastPackage}\n${status.lastPage}")
                InfoCard("确认浮窗", status.overlayResult)
                InfoCard("实际监听应用", status.effectivePackages.sorted().joinToString("\n").ifEmpty { "尚未设置" })
                if (status.failure.isNotEmpty()) InfoCard("最近异常", status.failure)
                Button(vm.platform::copyDiagnostics, Modifier.fillMaxWidth()) { Text("复制诊断信息") }
                Text("诊断不包含页面原文、金额或商户。", style = MaterialTheme.typography.bodySmall)
            }
            else -> Unit
        }
    }
}

@Composable
internal fun InboxScreen(vm: NativeViewModel, nav: Navigator, pending: List<CaptureCandidate>) {
    PageColumn {
        if (pending.isEmpty()) EmptyState("没有待确认记录")
        pending.forEach { candidate -> InfoCard(candidate.proposal.merchant.ifBlank { kindLabel(candidate.proposal.kind) }, "${candidate.proposal.amount.display()} · ${timeText(candidate.proposal.occurredAt)}\n${SupportedApps.find { it.second == candidate.proposal.packageName }?.first ?: candidate.proposal.packageName}", { nav.open(Screen.CANDIDATE, candidate.id) }) }
    }
}

@Composable
internal fun CandidateScreen(vm: NativeViewModel, nav: Navigator, id: String, books: List<Book>, accounts: List<Account>, categories: List<Category>) {
    val pending by vm.pending.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val candidate = pending.find { it.id == id }
    val draft by produceState<EntryDraft?>(null, candidate?.revision, id) { value = vm.repository.candidateDraft(id) }
    if (candidate == null) { EmptyState("这条记录已处理或不存在", "返回", nav::back); return }
    PageColumn {
        CandidateReviewContent(candidate, books, accounts, categories, busy,
            onSave = { vm.commit(FinancialCommand.Save(it, candidateId = candidate.id, candidateRevision = candidate.revision), nav::back) },
            onDismiss = { vm.run(success = "已忽略", after = nav::back) { vm.repository.dismiss(candidate.id, candidate.revision) } },
            onEdit = { snapshot ->
                val token = vm.platform.stageEditorDraft(snapshot, candidate.id, candidate.revision)
                nav.open(Screen.EDITOR, candidateId = candidate.id, draftToken = token)
            }, initialDraft = draft)
    }
}

@Composable
internal fun RulesScreen(vm: NativeViewModel, screen: Screen, accounts: List<Account>, categories: List<Category>) {
    val rules by remember { vm.repository.observeRules() }.collectAsStateWithLifecycle(emptyList())
    val mappings by remember { vm.repository.observeMappings() }.collectAsStateWithLifecycle(emptyList())
    var editing by remember(screen) { mutableStateOf<String?>(null) }
    PageColumn {
        Text(if (screen == Screen.RULES) "按商户、应用和关键词匹配分类，不使用模型或网络。" else "将支付页面中的付款方式关联到本地账户。", style = MaterialTheme.typography.bodySmall)
        Button({ editing = "" }, Modifier.fillMaxWidth()) { Text("新增规则") }
        if (screen == Screen.RULES) rules.forEach { rule -> InfoCard(rule.merchant.ifBlank { rule.keyword.ifBlank { rule.packageName } }, "${categories.find { it.id == rule.categoryId }?.name ?: "保持分类"} · ${accounts.find { it.id == rule.accountId }?.name ?: "保持账户"}", { editing = rule.id }) }
        else mappings.forEach { mapping -> InfoCard(SupportedApps.find { it.second == mapping.packageName }?.first ?: mapping.packageName, "${mapping.paymentMethod.ifBlank { "默认付款方式" }} → ${accounts.find { it.id == mapping.accountId }?.name.orEmpty()}", { editing = mapping.id }) }
    }
    editing?.let { id ->
        val old = rules.find { it.id == id }; val mapping = mappings.find { it.id == id }
        var packageName by remember(id) { mutableStateOf(old?.packageName ?: mapping?.packageName ?: "") }
        var merchant by remember(id) { mutableStateOf(old?.merchant ?: "") }; var keyword by remember(id) { mutableStateOf(old?.keyword ?: "") }
        var method by remember(id) { mutableStateOf(mapping?.paymentMethod ?: "") }; var account by remember(id) { mutableStateOf(old?.accountId ?: mapping?.accountId) }; var category by remember(id) { mutableStateOf(old?.categoryId) }
        FormDialog(if (screen == Screen.RULES) "分类匹配规则" else "付款方式映射", { editing = null }) {
            ChoiceField("来源应用", SupportedApps, SupportedApps.find { it.second == packageName }, { it.first }, { packageName = it.second }, if (screen == Screen.RULES) "全部应用" else "请选择")
            if (screen == Screen.RULES) {
                TextButton({ packageName = "" }) { Text("适用于全部应用") }
                TextFieldRow("商户包含", merchant, { merchant = it }); TextFieldRow("备注关键词", keyword, { keyword = it })
                ChoiceField("分类", categories.filter { !it.archived }, categories.find { it.id == category }, Category::name, { category = it.id })
            } else TextFieldRow("付款方式包含", method, { method = it }, supporting = "留空可作为该应用的默认账户")
            ChoiceField("账户", accounts.filter { !it.archived }, accounts.find { it.id == account }, Account::name, { account = it.id })
            Button({ vm.run(after = { editing = null }) {
                if (screen == Screen.RULES) vm.repository.saveRule(MerchantRule(old?.id ?: newId(), merchant.trim(), packageName, keyword.trim(), category, account))
                else { require(packageName.isNotBlank() && account != null) { "请选择应用和账户" }; vm.repository.saveMapping(AccountMapping(mapping?.id ?: newId(), packageName, method.trim(), account!!)) }
            } }, Modifier.fillMaxWidth()) { Text("保存") }
            if (id.isNotEmpty()) OutlinedButton({ vm.run(after = { editing = null }) { if (screen == Screen.RULES) vm.repository.deleteRule(id) else vm.repository.deleteMapping(id) } }, Modifier.fillMaxWidth()) { Text("删除规则") }
        }
    }
}
