package io.github.wcwac.beecount.nativeapp

import android.Manifest
import android.app.KeyguardManager
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.wcwac.beecount.capture.CapturePermissions
import io.github.wcwac.beecount.domain.*
import io.github.wcwac.beecount.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private val container by lazy { NativeApplication.container(this) }
    private val ready = MutableStateFlow(false)
    private val unlocked = MutableStateFlow(false)
    private val route = MutableStateFlow<String?>(null)
    private val permissionState = MutableStateFlow(PermissionSnapshot())
    private val activityState by lazy { ViewModelProvider(this)[NativeActivityState::class.java] }
    private val working get() = activityState.working
    private var biometricSignal: CancellationSignal? = null
    private var authenticating = false
    private var credentialPending = false

    private val saveZip = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) activityState.exportBackup(container, uri)
    }
    private val openZip = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) activityState.prepareRestore(container, uri)
    }
    private val saveCsv = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        val book = activityState.pendingCsvBook
        if (uri != null && book != null) activityState.exportCsv(container, book, uri)
    }
    private val openCsv = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val book = activityState.pendingCsvBook
        if (uri != null && book != null) activityState.prepareCsv(container, book, uri)
    }
    private val selectTree = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) activityState.selectTree(container, uri)
    }
    private val selectAttachments = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        activityState.importAttachments(container, uris)
    }
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { refreshPermissions() }
    private val credential = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        authenticating = false; credentialPending = false
        if (result.resultCode == RESULT_OK) unlocked.value = true else message("尚未解锁")
    }

    private val platform = object : NativePlatformActions {
        override val preferences get() = container.settings.preferences
        override val captureStatus get() = container.status
        override val permissions: StateFlow<PermissionSnapshot> get() = permissionState
        override val attachmentResults get() = activityState.attachmentResults
        override suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
            val desired = transform(container.settings.load())
            if (desired.appLock) require(getSystemService(KeyguardManager::class.java).isDeviceSecure) { "请先在系统中设置设备密码或生物识别" }
            container.updatePreferences(transform)
        }
        override fun refreshPermissions() = this@MainActivity.refreshPermissions()
        override fun openAccessibilitySettings() = launchSystem(CapturePermissions.accessibilityIntent())
        override fun openOverlaySettings() = launchSystem(CapturePermissions.overlayIntent(this@MainActivity))
        override fun requestNotifications() {
            if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            else launchSystem(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
        }
        override fun openBatterySettings() = launchSystem(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        override fun previewOverlay() { if (Settings.canDrawOverlays(this@MainActivity)) container.overlay().preview() else openOverlaySettings() }
        override fun exportBackup() { saveZip.launch("beecount-native-${LocalDate.now()}.zip") }
        override fun restoreBackup() { openZip.launch(arrayOf("application/zip", "application/octet-stream")) }
        override fun exportCsv(bookId: String) { activityState.pendingCsvBook = bookId; saveCsv.launch("beecount-${LocalDate.now()}.csv") }
        override fun importCsv(bookId: String) { activityState.pendingCsvBook = bookId; openCsv.launch(arrayOf("text/*", "application/csv", "application/octet-stream")) }
        override fun selectBackupDirectory() { selectTree.launch(null) }
        override fun backupNow() { activityState.backupNow(container) }
        override fun pickAttachments(draftId: String) { activityState.attachmentTarget = draftId; selectAttachments.launch(arrayOf("*/*")) }
        override fun consumeAttachmentResults(draftId: String, ids: List<String>) = activityState.consumeAttachments(draftId, ids)
        override fun openAttachment(attachment: Attachment) {
            runCatching {
                val root = File(filesDir, "attachments").canonicalFile
                val file = File(attachment.localPath).canonicalFile
                require(file.path.startsWith(root.path + File.separator) && file.isFile) { "附件文件不可用" }
                val uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.files", file)
                launchSystem(Intent(Intent.ACTION_VIEW).setDataAndType(uri, attachment.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
            }.onFailure { message(it.message ?: "无法打开附件") }
        }
        override fun pinWidget(kind: String) {
            val manager = getSystemService(AppWidgetManager::class.java)
            if (manager.isRequestPinAppWidgetSupported) manager.requestPinAppWidget(ComponentName(this@MainActivity, if (kind == "repayment") RepaymentWidget::class.java else NativeWidget::class.java), null, null)
            else message("请从系统桌面的小组件列表添加")
        }
        override fun copyDiagnostics() {
            val s = container.status.value; val p = permissionState.value; val c = container.captureConfig.value
            val text = JSONObject().put("enabled", c.enabled).put("permissionGranted", p.accessibilityGranted)
                .put("connected", s.connected).put("overlayGranted", p.overlayGranted).put("effectivePackages", org.json.JSONArray(s.effectivePackages.toList()))
                .put("events", s.events).put("reads", s.reads).put("proposals", s.proposals).put("candidates", s.candidates).put("skipped", s.skipped)
                .put("stage", s.stage).put("result", s.lastResult).put("package", s.lastPackage).put("pageClass", s.lastPage)
                .put("eventAt", s.lastEventAt).put("failure", s.failure).put("overlay", s.overlayResult).toString()
            getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("BeeCount 诊断", text)); message("诊断摘要已复制")
        }
        override fun showQuickEntry() { if (Settings.canDrawOverlays(this@MainActivity)) container.overlay().quickEntry() else openOverlaySettings() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) route.value = intent.getStringExtra("route")
        setContent {
            val prefs by container.settings.preferences.collectAsStateWithLifecycle()
            val initialized by ready.collectAsStateWithLifecycle()
            val authenticated by unlocked.collectAsStateWithLifecycle()
            val operationMessage by activityState.messages.collectAsStateWithLifecycle()
            val failure by container.initializationFailure.collectAsStateWithLifecycle()
            val focus = LocalFocusManager.current
            val keyboard = LocalSoftwareKeyboardController.current
            val locked = prefs.appLock && !authenticated
            val blocked = locked || working != null
            LaunchedEffect(operationMessage, locked) { if (!locked) operationMessage?.let { message(it); activityState.messages.value = null } }
            LaunchedEffect(blocked) { if (blocked) { focus.clearFocus(force = true); keyboard?.hide() } }
            LaunchedEffect(prefs.secureScreen, prefs.appLock) {
                if (prefs.secureScreen) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                if (Build.VERSION.SDK_INT >= 33) setRecentsScreenshotEnabled(!prefs.secureScreen && !prefs.appLock)
            }
            NativeTheme(prefs.theme) {
                Surface(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize()) {
                        if (!initialized) CenterContent {
                            if (failure == null) CircularProgressIndicator() else { Text(failure.orEmpty()); Button(onClick = ::initialize) { Text("重试") } }
                        } else {
                            Box(Modifier.fillMaxSize().then(if (blocked) Modifier.clearAndSetSemantics {}.alpha(0f) else Modifier)) {
                                key(activityState.uiEpoch) { NativeApp(container.repository, platform, externalRoute = route, windowsAllowed = !blocked) }
                            }
                            if (locked) Surface(modifier = Modifier.fillMaxSize(), onClick = {}) {
                                CenterContent { Text("BeeCount 已锁定", style = MaterialTheme.typography.titleLarge); Button(onClick = ::unlock) { Text("解锁") } }
                            } else if (working != null) Surface(modifier = Modifier.fillMaxSize(), onClick = {}) {
                                CenterContent { CircularProgressIndicator(); Text(working.orEmpty()) }
                            }
                        }
                    }
                    if (!locked && working == null) activityState.confirmation?.let { request ->
                        AlertDialog(onDismissRequest = { activityState.decline(container) }, title = { Text(request.title) }, text = { Text(request.text) },
                            confirmButton = { TextButton(onClick = { activityState.accept(container); route.value = null }) { Text("确认") } },
                            dismissButton = { TextButton(onClick = { activityState.decline(container) }) { Text("取消") } })
                    }
                }
            }
        }
        initialize()
    }

    private fun initialize() {
        lifecycleScope.launch {
            try { withContext(Dispatchers.IO) { container.initialize() }; ready.value = true }
            catch (failure: Exception) { if (failure is CancellationException) throw failure; container.initializationFailure.value = failure.message ?: "数据库初始化失败" }
        }
    }

    override fun onResume() { super.onResume(); refreshPermissions() }
    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) { unlocked.value = false; if (!credentialPending) biometricSignal?.cancel() }
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); intent.getStringExtra("route")?.let(::navigate) }

    private fun navigate(value: String) { route.value = "$value#${newId()}" }
    private fun launchSystem(intent: Intent) { runCatching { startActivity(intent) }.onFailure { message("当前系统无法打开该入口，请在系统设置中操作") } }
    private fun refreshPermissions() {
        permissionState.value = PermissionSnapshot(CapturePermissions.accessibilityGranted(this), Settings.canDrawOverlays(this),
            NotificationManagerCompat.from(this).areNotificationsEnabled(), getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName))
    }
    private fun message(text: String) { Toast.makeText(this, text, Toast.LENGTH_LONG).show() }

    private fun unlock() {
        if (authenticating) return
        authenticating = true
        if (Build.VERSION.SDK_INT >= 28) {
            val signal = CancellationSignal().also { biometricSignal = it }
            val builder = BiometricPrompt.Builder(this).setTitle("解锁 BeeCount")
            if (Build.VERSION.SDK_INT >= 30) builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            else builder.setNegativeButton("使用设备密码", mainExecutor) { _, _ -> authenticating = false; unlockWithCredential() }
            builder.build().authenticate(signal, mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { authenticating = false; unlocked.value = true }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    authenticating = false
                    if (errorCode == BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS || errorCode == BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT) unlockWithCredential()
                    else if (errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED && errorCode != BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED) message(errString.toString())
                }
            })
        } else unlockWithCredential()
    }

    @Suppress("DEPRECATION")
    private fun unlockWithCredential() {
        val intent = getSystemService(KeyguardManager::class.java).createConfirmDeviceCredentialIntent("解锁 BeeCount", "验证设备密码以查看账务")
        if (intent == null) { authenticating = false; message("设备凭据不可用，请检查系统锁屏设置") }
        else { authenticating = true; credentialPending = true; credential.launch(intent) }
    }
}

/** Retains file jobs through Activity recreation without retaining an Activity or a Compose callback. */
internal class NativeActivityState(private val saved: SavedStateHandle) : ViewModel() {
    var working by mutableStateOf<String?>(null)
        private set
    var confirmation by mutableStateOf<FileConfirmation?>(null)
        private set
    var uiEpoch by mutableLongStateOf(saved.get<Long>("uiEpoch") ?: 0L)
        private set
    val messages = MutableStateFlow<String?>(null)
    var pendingCsvBook: String?
        get() = saved["csvBook"]
        set(value) { saved["csvBook"] = value }
    var attachmentTarget: String?
        get() = saved["attachmentTarget"]
        set(value) { saved["attachmentTarget"] = value }
    private val tokens = saved.getStateFlow("attachmentTokens", arrayListOf<String>())
    val attachmentResults: StateFlow<Map<String, List<String>>> = tokens.map { decode(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, decode(tokens.value))
    private var discardPrepared: (() -> Unit)? = null

    data class FileConfirmation(val title: String, val text: String, val restore: NativeFiles.RestorePackage? = null, val csv: NativeFiles.CsvPreview? = null)

    private fun decode(values: List<String>): Map<String, List<String>> = values.groupBy({ it.substringBefore(':') }, { it.substringAfter(':') })
    fun consumeAttachments(draftId: String, ids: List<String>) {
        val consumed = ids.map { "$draftId:$it" }.toSet()
        saved["attachmentTokens"] = ArrayList(tokens.value.filterNot { it in consumed })
    }
    fun exportBackup(container: NativeContainer, uri: Uri) = run("正在备份") { container.files.exportBackup(uri); "备份已保存" }
    fun exportCsv(container: NativeContainer, book: String, uri: Uri) = run("正在导出普通收支") { container.files.exportCsv(book, uri); "CSV 已保存；完整财务关系请使用 ZIP 备份" }
    fun backupNow(container: NativeContainer) = run("正在自动备份目录中保存") { "已保存 ${container.files.backupNow()}" }
    fun selectTree(container: NativeContainer, uri: Uri) = run("正在保存备份目录") {
        container.context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        container.updatePreferences { it.copy(backupTree = uri.toString()) }; "备份目录已设置"
    }
    fun prepareRestore(container: NativeContainer, uri: Uri) = run("正在校验备份") {
        val prepared = container.files.prepareRestore(uri)
        withContext(Dispatchers.Main) {
            discardPrepared = { container.scope.launch { container.files.discardRestore(prepared) } }
            confirmation = FileConfirmation("覆盖恢复", "备份包含 ${prepared.rows} 条数据。继续将覆盖当前账务和附件关联；恢复后自动保存会关闭。", restore = prepared)
        }
        null
    }
    fun prepareCsv(container: NativeContainer, book: String, uri: Uri) = run("正在校验 CSV") {
        val preview = container.files.previewCsv(book, uri)
        withContext(Dispatchers.Main) { confirmation = FileConfirmation("导入普通收支", "已校验 ${preview.entries.size} 条记录。确认后整批写入当前账本，任一记录失败会全部回滚。", csv = preview) }
        null
    }
    fun importAttachments(container: NativeContainer, uris: List<Uri>) {
        val target = attachmentTarget; attachmentTarget = null
        if (uris.isEmpty()) return
        if (target == null) { messages.value = "未找到附件对应的草稿，请在记账页面重新选择"; return }
        run("正在导入附件") {
            require(uris.size <= 20) { "每次最多选择 20 个附件" }
            val imported = uris.map { container.files.importAttachment(it) }
            withContext(Dispatchers.Main) { saved["attachmentTokens"] = ArrayList((tokens.value + imported.map { "$target:${it.id}" }).distinct()) }
            "已导入 ${imported.size} 个附件"
        }
    }
    fun accept(container: NativeContainer) {
        if (working != null) return
        val request = confirmation ?: return
        confirmation = null; discardPrepared = null
        request.restore?.let { prepared ->
            run("正在恢复数据") {
                try { container.files.applyRestore(prepared) }
                finally { withContext(NonCancellable + Dispatchers.Main) {
                    saved["attachmentTokens"] = arrayListOf<String>(); attachmentTarget = null
                    uiEpoch++; saved["uiEpoch"] = uiEpoch
                } }
                "恢复完成，自动保存已关闭"
            }
        }
        request.csv?.let { preview -> run("正在导入") { "已导入 ${container.files.importCsv(preview)} 条记录" } }
    }
    fun decline(container: NativeContainer) {
        val request = confirmation; confirmation = null; discardPrepared = null
        request?.restore?.let { prepared -> container.scope.launch { container.files.discardRestore(prepared) } }
    }
    private fun run(label: String, action: suspend () -> String?) {
        if (working != null) return
        working = label
        viewModelScope.launch {
            try { withContext(Dispatchers.IO) { action() }?.let { messages.value = it } }
            catch (failure: Exception) { if (failure is CancellationException) throw failure; messages.value = failure.message ?: "操作失败，请重试" }
            finally { working = null }
        }
    }
    override fun onCleared() { discardPrepared?.invoke(); discardPrepared = null; super.onCleared() }
}

@Composable
private fun CenterContent(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}
