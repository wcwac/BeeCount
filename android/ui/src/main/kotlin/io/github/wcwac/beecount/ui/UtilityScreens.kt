package io.github.wcwac.beecount.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import io.github.wcwac.beecount.domain.*
import java.io.File

private object LocalImages {
    @Volatile private var instance: ImageLoader? = null
    fun get(context: android.content.Context): ImageLoader = instance ?: synchronized(this) {
        instance ?: ImageLoader.Builder(context.applicationContext).memoryCache { MemoryCache.Builder().maxSizeBytes(8L * 1024 * 1024).build() }.diskCache(null).build().also { instance = it }
    }
}

@Composable
internal fun AttachmentsScreen(vm: NativeViewModel) {
    val attachments = remember { vm.repository.pagedAttachments() }.collectAsLazyPagingItems()
    val context = LocalContext.current
    val loader = remember(context.applicationContext) { LocalImages.get(context) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { PagingStatus(attachments.loadState.refresh, attachments.itemCount, attachments::retry) }
        items(attachments.itemCount, key = attachments.itemKey { it.id }) { index ->
            val attachment = attachments[index] ?: return@items
            InfoCard(attachment.name, "${attachment.size / 1024} KB · ${attachment.mime}", { vm.platform.openAttachment(attachment) }, trailing = {
                if (attachment.mime.startsWith("image/")) AsyncImage(model = ImageRequest.Builder(context).data(File(attachment.localPath)).size(160, 160).build(), imageLoader = loader, contentDescription = "附件缩略图", modifier = Modifier.size(56.dp))
            })
        }
        if (attachments.loadState.append is LoadState.Error) item { EmptyState("较早附件加载失败", "重试", attachments::retry) }
    }
}

@Composable
internal fun UtilityScreen(vm: NativeViewModel, nav: Navigator, screen: Screen, book: Book?) {
    val prefs by vm.platform.preferences.collectAsStateWithLifecycle()
    var restoring by remember { mutableStateOf(false) }
    PageColumn {
        when (screen) {
            Screen.DATA -> {
                SectionTitle("本地完整备份")
                InfoCard("导出完整备份", "保存所有账本、财务关联、规则、目标及附件", vm.platform::exportBackup)
                InfoCard("恢复完整备份", "恢复本应用导出的文件，将替换本机数据", { restoring = true })
                InfoCard("自动备份文件夹", if (prefs.backupTree == null) "选择系统文件夹以启用" else "已设置 · 点此更换", vm.platform::selectBackupDirectory)
                if (prefs.backupTree != null) {
                    ChoiceField("保留最近备份", listOf(7, 14, 30), prefs.backupRetention, { "$it 份" }, { retention -> vm.preferences { it.copy(backupRetention = retention) } })
                    OutlinedButton(vm.platform::backupNow, Modifier.fillMaxWidth()) { Text("立即备份") }
                    TextButton({ vm.preferences { it.copy(backupTree = null) } }) { Text("关闭每日自动备份") }
                }
                SectionTitle("账单迁移")
                InfoCard("导出当前账本 CSV", book?.name ?: "请先选择账本", { book?.let { vm.platform.exportCsv(it.id) } })
                InfoCard("导入 CSV", "导入到当前账本；校验失败整体回滚", { book?.let { vm.platform.importCsv(it.id) } })
                Text("文件由系统文件选择器保存。卸载应用前请自行保留备份。", style = MaterialTheme.typography.bodySmall)
            }
            Screen.SETTINGS -> {
                val themes = listOf("system" to "跟随系统", "light" to "浅色", "dark" to "深色")
                ChoiceField("外观", themes, themes.find { it.first == prefs.theme }, { it.second }, { theme -> vm.preferences { it.copy(theme = theme.first) } })
                ToggleRow("隐藏金额", "首页、资产和统计不直接展示金额", prefs.hideAmounts, { value -> vm.preferences { it.copy(hideAmounts = value) } })
                ToggleRow("应用锁", "使用设备生物识别或锁屏凭据，离开应用后锁定", prefs.appLock, { value -> vm.preferences { it.copy(appLock = value) } })
                ToggleRow("保护画面", "阻止应用截图并隐藏最近任务画面", prefs.secureScreen, { value -> vm.preferences { it.copy(secureScreen = value) } })
                InfoCard("自动记账权限", "总开关、无障碍、浮窗和后台运行", { nav.open(Screen.CAPTURE) })
                InfoCard("数据与备份", "本地导出、导入和自动备份", { nav.open(Screen.DATA) })
            }
            Screen.WIDGETS -> {
                InfoCard("收支桌面组件", "显示当前账本收支并提供记账入口", { vm.platform.pinWidget("summary") })
                InfoCard("近期还款组件", "显示信用账户和到期提醒", { vm.platform.pinWidget("repayment") })
                InfoCard("快捷记账浮窗", "显示一个可随时打开记账页的轻量入口", vm.platform::showQuickEntry)
            }
            Screen.HELP -> {
                InfoCard("自动记账", "开启总开关并授予系统无障碍权限；选择目标账本和应用。支付页面读取成功后会生成待确认记录。浮窗需要额外的悬浮窗权限。", { nav.open(Screen.CAPTURE) })
                InfoCard("找不到记录", "查看诊断中的事件、读取、候选和窗口状态。应用版本更新或系统限制可能影响识别。", { nav.open(Screen.DIAGNOSTICS) })
                InfoCard("账务与币种", "普通收支支持账本币种。复杂转账、借贷和分期以人民币流程为基线，不进行联网换汇。")
                InfoCard("投资记录", "投资持仓独立于现金账户；买卖日志不会自动改变现金余额。")
                InfoCard("本地与隐私", "所有数据保存在本机。应用不申请网络权限，不提供账户登录、会员或AI功能。")
                InfoCard("关于", "BeeCount Native · Kotlin / Jetpack Compose\n独立本地应用，不自动读取其他版本数据库。")
                InfoCard("第三方组件", "AndroidX / Material3 / Kotlin Coroutines：Apache-2.0\nCoil：Apache-2.0")
            }
            else -> Unit
        }
    }
    if (restoring) Confirmation("恢复备份", "当前本地数据将被替换，自动采集将关闭。请先导出现有数据。", { restoring = false }) { restoring = false; vm.platform.restoreBackup() }
}
