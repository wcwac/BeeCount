package io.github.wcwac.beecount.nativeapp

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.core.content.ContextCompat
import androidx.savedstate.*
import io.github.wcwac.beecount.capture.NativeCaptureService
import io.github.wcwac.beecount.domain.*
import io.github.wcwac.beecount.ui.CandidateReviewContent
import io.github.wcwac.beecount.ui.EditorDraftSnapshot
import io.github.wcwac.beecount.ui.NativeTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

/** Owns one system window. It never stores a reference to the app Activity. */
class NativeOverlay(private val container: NativeContainer) {
    private val context = container.context
    private val windows = context.getSystemService(WindowManager::class.java)
    private var view: ComposeView? = null
    private var owner: OverlayOwner? = null
    private var current by mutableStateOf<CaptureCandidate?>(null)
    private var draft by mutableStateOf<EntryDraft?>(null)
    private var busy by mutableStateOf(false)
    private var error by mutableStateOf<String?>(null)
    private var seconds by mutableStateOf<Int?>(null)
    private var countdown: Job? = null
    private var setup: Job? = null
    private val queued = ArrayDeque<CaptureCandidate>()
    private var preview = false
    private var manualQuick = false
    private var dirty = false
    private var formRevision by mutableLongStateOf(0)
    private val muted = LinkedHashSet<String>()
    private var screenReceiverRegistered = false
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { if (intent?.action == Intent.ACTION_SCREEN_OFF) close(clearQueue = true) }
    }
    private var windowGeneration = 0L

    fun offer(candidate: CaptureCandidate) {
        if (!Settings.canDrawOverlays(context)) { close(clearQueue = true); result("PERMISSION_MISSING"); return }
        if (candidate.state != CandidateState.PENDING) return
        if (candidate.id in muted) return
        if (current?.id == candidate.id) {
            if (current?.revision != candidate.revision) {
                cancelCountdown(); current = candidate
                if (!dirty) { formRevision = candidate.revision; reloadDraft(candidate) }
            }
            return
        }
        if (view != null) {
            if (queued.none { it.id == candidate.id } && queued.size < 8) queued.addLast(candidate)
            return
        }
        manualQuick = false
        show(candidate, false)
    }

    fun preview() { close(clearQueue = true); manualQuick = false; show(null, true) }

    fun quickEntry() {
        close(clearQueue = true)
        container.scope.launch(Dispatchers.Main.immediate) {
            val preferences = container.settings.preferences.value
            val books = container.repository.observeBooks().first()
            val book = books.firstOrNull { it.id == preferences.currentBookId && !it.archived } ?: books.firstOrNull { !it.archived } ?: return@launch
            val account = container.repository.observeAccounts().first().firstOrNull { !it.archived && it.currency == book.currency && it.kind !in setOf(AccountKind.RECEIVABLE, AccountKind.PAYABLE) }
            val id = newId()
            val proposal = CaptureProposal(context.packageName, "QuickEntry", id, -1, EntryKind.EXPENSE, Money(0, CurrencyCode(book.currency)), windowKey = id)
            manualQuick = true
            show(CaptureCandidate(id, proposal, book.id, account?.id, null), false)
            draft = EntryDraft(bookId = book.id, amount = proposal.amount, accountId = account?.id)
        }
    }

    private fun show(candidate: CaptureCandidate?, isPreview: Boolean) {
        if (!Settings.canDrawOverlays(context)) { result("PERMISSION_MISSING"); return }
        if (context.getSystemService(KeyguardManager::class.java).isKeyguardLocked) { result("DEVICE_LOCKED"); return }
        val config = container.captureConfig.value
        if (!isPreview && !manualQuick && (!config.enabled || !config.overlayEnabled)) { result("DISABLED"); return }
        current = candidate; draft = null; busy = false; error = null; preview = isPreview
        dirty = false; formRevision = candidate?.revision ?: 0
        windowGeneration = container.captureGeneration
        val lifecycle = OverlayOwner().also { it.create() }
        val content = ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycle)
            setViewTreeViewModelStoreOwner(lifecycle)
            setViewTreeSavedStateRegistryOwner(lifecycle)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val prefs by container.settings.preferences.collectAsState()
                NativeTheme(prefs.theme) {
                    Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp, shadowElevation = 8.dp) {
                        Column(Modifier.fillMaxWidth().heightIn(max = (context.resources.displayMetrics.heightPixels / context.resources.displayMetrics.density * .75f).dp).verticalScroll(rememberScrollState())) {
                            if (preview) {
                                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("确认浮窗已连接", style = MaterialTheme.typography.titleLarge)
                                    Text("支付页面识别成功后，会先保存待确认记录，再显示此浮窗。")
                                    Button(onClick = { close() }) { Text("关闭") }
                                }
                            } else if (prefs.appLock) {
                                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("有一笔记录待确认")
                                    Button(onClick = { openEditor(false) }) { Text("解锁并查看") }
                                    TextButton(onClick = { close() }) { Text("稍后处理") }
                                }
                            } else {
                                val candidateNow = current
                                val books by container.repository.observeBooks().collectAsState(emptyList())
                                val accounts by container.repository.observeAccounts().collectAsState(emptyList())
                                val categories by container.repository.observeCategories().collectAsState(emptyList())
                                if (candidateNow != null) {
                                    key(candidateNow.id, formRevision) {
                                    CandidateReviewContent(
                                        candidateNow, books, accounts, categories, busy,
                                        onSave = { save(it, automatic = false) }, onDismiss = { dismiss() },
                                        onEdit = { snapshot -> openEditor(true, snapshot) }, countdownSeconds = seconds,
                                        onInteraction = { dirty = true; cancelCountdown() }, initialDraft = draft, inlineChoices = true
                                    )
                                    }
                                }
                                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp)) }
                                TextButton(onClick = { close() }, enabled = !busy, modifier = Modifier.padding(horizontal = 16.dp)) { Text("稍后处理") }
                            }
                        }
                    }
                }
            }
        }
        val density = context.resources.displayMetrics.density
        val width = minOf((420 * density).toInt(), context.resources.displayMetrics.widthPixels - (24 * density).toInt())
        val params = WindowManager.LayoutParams(width, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or (if (container.settings.preferences.value.secureScreen) WindowManager.LayoutParams.FLAG_SECURE else 0),
            PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = (36 * density).toInt()
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }
        try {
            owner = lifecycle; view = content
            windows.addView(content, params)
            ContextCompat.registerReceiver(context, screenReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF), ContextCompat.RECEIVER_NOT_EXPORTED)
            screenReceiverRegistered = true
            lifecycle.resume(); result("ATTACHED")
            if (candidate != null && !manualQuick) reloadDraft(candidate, startCountdown = true)
        } catch (failure: RuntimeException) {
            if (screenReceiverRegistered) { runCatching { context.unregisterReceiver(screenReceiver) }; screenReceiverRegistered = false }
            runCatching { windows.removeViewImmediate(content) }
            content.disposeComposition(); lifecycle.destroy(); owner = null; view = null
            result("ATTACH_FAILED_${failure.javaClass.simpleName}")
        }
    }

    private fun reloadDraft(candidate: CaptureCandidate, startCountdown: Boolean = false) {
        setup?.cancel()
        setup = container.scope.launch(Dispatchers.Main.immediate) {
            try {
                val loaded = container.repository.candidateDraft(candidate.id)
                if (current?.id != candidate.id || current?.revision != candidate.revision || view == null) return@launch
                draft = loaded
                if (startCountdown && loaded != null && eligible(candidate, loaded)) startTimer(candidate, loaded)
            } catch (failure: Exception) { if (failure is CancellationException) throw failure; error = failure.message ?: "无法读取候选"; result("CANDIDATE_LOAD_FAILED") }
        }
    }

    private fun eligible(candidate: CaptureCandidate, value: EntryDraft): Boolean {
        val prefs = container.settings.preferences.value
        val config = container.captureConfig.value
        return !manualQuick && view?.isAttachedToWindow == true && windowGeneration == container.captureGeneration &&
            config.enabled && config.overlayEnabled && config.autoSave && container.status.value.connected &&
            !prefs.appLock && !context.getSystemService(KeyguardManager::class.java).isKeyguardLocked &&
            Settings.canDrawOverlays(context) && candidate.reasons.isEmpty() && candidate.proposal.strongKey != null &&
            value.accountId != null && value.kind in setOf(EntryKind.EXPENSE, EntryKind.INCOME, EntryKind.TRANSFER, EntryKind.CREDIT_PAYMENT) &&
            (value.kind !in setOf(EntryKind.TRANSFER, EntryKind.CREDIT_PAYMENT) || value.otherAccountId != null)
    }

    private fun startTimer(candidate: CaptureCandidate, value: EntryDraft) {
        cancelCountdown()
        countdown = container.scope.launch(Dispatchers.Main.immediate) {
            for (remaining in 5 downTo 1) {
                if (!eligible(candidate, value) || current?.revision != candidate.revision) { seconds = null; return@launch }
                seconds = remaining; delay(1000)
            }
            seconds = null
            if (eligible(candidate, value)) save(value, automatic = true)
        }
    }

    fun cancelCountdown() { countdown?.cancel(); countdown = null; seconds = null }

    private fun save(value: EntryDraft, automatic: Boolean) {
        val candidate = current ?: return
        if (busy || windowGeneration != container.captureGeneration) return
        if (automatic && !eligible(candidate, value)) { cancelCountdown(); return }
        cancelCountdown(); busy = true; error = null
        val acceptedGeneration = windowGeneration
        val isManualQuick = manualQuick
        // Once accepted, a manual write belongs to the application, not to this window.
        container.scope.launch {
            val outcome = try {
                if (acceptedGeneration != container.captureGeneration) CommitResult.Rejected("数据已恢复，请重新打开候选")
                else container.repository.execute(FinancialCommand.Save(
                    value, candidateId = candidate.id.takeUnless { isManualQuick }, candidateRevision = candidate.revision.takeUnless { isManualQuick },
                    operationId = UUID.nameUUIDFromBytes("candidate:${candidate.id}".toByteArray(Charsets.UTF_8)).toString(),
                    expectedGeneration = acceptedGeneration
                ))
            } catch (failure: Exception) {
                if (failure is CancellationException) throw failure
                CommitResult.Rejected(failure.message ?: "保存失败，记录仍在待确认列表")
            }
            withContext(Dispatchers.Main.immediate) {
                if (outcome is CommitResult.Success) {
                    NativeCaptureService.candidateHandled(candidate.id)
                    result("SAVED")
                    if (current?.id == candidate.id) close()
                } else if (current?.id == candidate.id) { busy = false; error = (outcome as CommitResult.Rejected).reason; result("SAVE_REJECTED") }
            }
        }
    }

    private fun dismiss() {
        val candidate = current ?: return
        if (busy) return
        if (manualQuick) { close(); return }
        cancelCountdown(); busy = true
        container.scope.launch(Dispatchers.Main.immediate) {
            try {
                container.repository.dismiss(candidate.id, candidate.revision)
                NativeCaptureService.candidateHandled(candidate.id); close()
            } catch (failure: Exception) { if (failure is CancellationException) throw failure; busy = false; error = failure.message ?: "无法忽略记录" }
        }
    }

    private fun openEditor(edit: Boolean, snapshot: EditorDraftSnapshot? = null) {
        if (busy) return
        val candidate = current ?: return
        cancelCountdown()
        val candidateId = candidate.id.takeUnless { manualQuick }
        var token: String? = null
        try {
            token = snapshot?.let { container.stageEditorDraft(it, candidateId, candidate.revision.takeUnless { manualQuick }, windowGeneration) }
            val route = if (token != null) "draft-edit:$token:${candidateId.orEmpty()}" else if (manualQuick) "editor" else (if (edit) "candidate-edit:" else "candidate:") + candidate.id
            context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("route", route))
            close(clearQueue = true)
        } catch (failure: RuntimeException) {
            token?.let(container::forgetEditorDraft)
            error = if (failure is IllegalArgumentException) failure.message ?: "无法转交当前输入" else "无法打开完整编辑，请稍后重试"
        }
    }

    fun close(clearQueue: Boolean = false) {
        cancelCountdown(); setup?.cancel(); setup = null
        if (screenReceiverRegistered) { runCatching { context.unregisterReceiver(screenReceiver) }; screenReceiverRegistered = false }
        current?.let { muted.add(it.id); while (muted.size > 128) muted.remove(muted.first()) }
        if (clearQueue) queued.clear()
        view?.let { runCatching { windows.removeViewImmediate(it) }; it.disposeComposition() }
        view = null; owner?.destroy(); owner = null; current = null; draft = null; busy = false
        if (queued.isNotEmpty()) {
            val next = queued.removeFirst()
            container.scope.launch(Dispatchers.Main.immediate) {
                container.repository.candidate(next.id)?.takeIf { it.state == CandidateState.PENDING }?.let(::offer)
            }
        }
    }

    private fun result(code: String) { container.status.update { it.copy(overlayResult = code) } }

    private class OverlayOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
        private val registry = LifecycleRegistry(this)
        private val saved = SavedStateRegistryController.create(this)
        override val lifecycle: Lifecycle get() = registry
        override val savedStateRegistry: SavedStateRegistry get() = saved.savedStateRegistry
        override val viewModelStore = ViewModelStore()
        fun create() { saved.performAttach(); saved.performRestore(null); registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE) }
        fun resume() { registry.handleLifecycleEvent(Lifecycle.Event.ON_START); registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
        fun destroy() { registry.currentState = Lifecycle.State.DESTROYED; viewModelStore.clear() }
    }
}
