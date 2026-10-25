package io.github.wcwac.beecount.nativeapp

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.work.Configuration
import io.github.wcwac.beecount.capture.CaptureApps
import io.github.wcwac.beecount.capture.NativeCaptureService
import io.github.wcwac.beecount.data.NativeDatabase
import io.github.wcwac.beecount.data.NativeRepository
import io.github.wcwac.beecount.domain.*
import io.github.wcwac.beecount.ui.EditorDraftSnapshot
import io.github.wcwac.beecount.ui.EditorDraftHandoff
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong

class NativeApplication : Application(), CaptureHostProvider, Configuration.Provider {
    val container by lazy { NativeContainer(this) }
    override val captureHost: CaptureHost get() = container
    override val workManagerConfiguration: Configuration get() = Configuration.Builder().setMinimumLoggingLevel(android.util.Log.WARN).build()
    companion object {
        fun container(context: Context): NativeContainer = (context.applicationContext as NativeApplication).container
    }
}

class NativeContainer(val context: Context) : CaptureHost {
    val status = MutableStateFlow(CaptureStatus())
    val initializationFailure = MutableStateFlow<String?>(null)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, error ->
        status.update { it.copy(failure = error.javaClass.simpleName, lastResult = "BACKGROUND_FAILED") }
    })
    val settings = NativeSettings(context, scope)
    override val captureConfig: StateFlow<CaptureConfig> = settings.preferences.map { it.capture }
        .stateIn(scope, SharingStarted.Eagerly, CaptureConfig())
    private val generation = AtomicLong(1)
    override val captureGeneration: Long get() = generation.get()
    val database by lazy { NativeDatabase.open(context) }
    val repository by lazy { NativeRepository(database, configProvider = { captureConfig.value }, generationProvider = { captureGeneration }) }
    override val captureInbox: CaptureInbox get() = repository
    override suspend fun awaitReady() { initialize() }
    val files by lazy { NativeFiles(this) }
    private var overlayInstance: NativeOverlay? = null
    private val initializationMutex = Mutex()
    private var initialized = false
    private data class HeldDraft(val value: EditorDraftHandoff, val expiresAt: Long)
    private val editorDrafts = LinkedHashMap<String, HeldDraft>()

    fun stageEditorDraft(snapshot: EditorDraftSnapshot, candidateId: String?, candidateRevision: Long?, expectedGeneration: Long = captureGeneration): String = synchronized(editorDrafts) {
        require(expectedGeneration == captureGeneration) { "数据已恢复，请重新打开当前页面" }
        require(snapshot.amountText.length <= 100 && snapshot.draft.merchant.length <= 500 && snapshot.draft.note.length <= 4000) { "输入内容过长，请缩短后再编辑" }
        val now = SystemClock.elapsedRealtime()
        editorDrafts.entries.removeAll { it.value.expiresAt <= now || it.value.value.generation != captureGeneration }
        while (editorDrafts.size >= 8) editorDrafts.remove(editorDrafts.keys.first())
        val token = newId()
        val value = EditorDraftHandoff(snapshot.copy(draft = snapshot.draft.copy(tagIds = snapshot.draft.tagIds.toList(), attachmentIds = snapshot.draft.attachmentIds.toList())), candidateId, candidateRevision, expectedGeneration)
        editorDrafts[token] = HeldDraft(value, now + 30 * 60_000L)
        token
    }

    fun editorDraft(token: String): EditorDraftHandoff? = synchronized(editorDrafts) {
        val item = editorDrafts[token] ?: return@synchronized null
        if (item.expiresAt <= SystemClock.elapsedRealtime() || item.value.generation != captureGeneration) { editorDrafts.remove(token); null } else item.value
    }

    fun forgetEditorDraft(token: String) { synchronized(editorDrafts) { editorDrafts.remove(token) } }

    suspend fun <T> withRestoreGate(block: suspend () -> T): T = initializationMutex.withLock { block() }

    suspend fun initialize(): Unit = initializationMutex.withLock {
        if (initialized && !files.hasPendingRecovery()) return@withLock
        settings.load()
        files.recoverInterruptedRestore()
        val original = settings.load()
        val book = repository.initialize()
        if (original.currentBookId == null) {
            settings.update { it.copy(currentBookId = book.id, capture = it.capture.copy(
                bookId = book.id, selectedPackages = CaptureApps.all.map { app -> app.packageName }.toSet()
            )) }
        }
        val alreadyInitialized = initialized
        initialized = true
        initializationFailure.value = null
        NativeWork.schedule(context)
        scope.launch { repository.generateRecurringCandidates() }
        if (!alreadyInitialized) scope.launch { repository.observeWidgetChanges().collect { NativeWidget.updateAll(context) } }
    }

    override fun onCaptureStatus(status: CaptureStatus) {
        this.status.update { status.copy(overlayResult = it.overlayResult) }
        if (!status.connected) scope.launch(Dispatchers.Main.immediate) { overlayInstance?.close() }
    }

    override fun onCandidateAvailable(candidate: CaptureCandidate) {
        scope.launch(Dispatchers.Main.immediate) {
            if (!captureConfig.value.enabled || !captureConfig.value.overlayEnabled) return@launch
            overlay().offer(candidate)
        }
    }

    fun overlay(): NativeOverlay {
        check(android.os.Looper.myLooper() == android.os.Looper.getMainLooper())
        return overlayInstance ?: NativeOverlay(this).also { overlayInstance = it }
    }

    suspend fun invalidateForRestore() {
        generation.incrementAndGet()
        synchronized(editorDrafts) { editorDrafts.clear() }
        withContext(Dispatchers.Main.immediate) { overlayInstance?.close(clearQueue = true); NativeCaptureService.invalidateForRestore() }
    }

    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        val expected = kotlin.coroutines.coroutineContext[MutationGeneration]?.expectedGeneration ?: captureGeneration
        val previous = settings.load()
        settings.update { current ->
            require(expected == captureGeneration) { "数据已恢复，请重新打开设置页面" }
            transform(current)
        }
        val latest = settings.load()
        if (!latest.capture.enabled || !latest.capture.overlayEnabled || !latest.capture.autoSave || latest.appLock || previous.secureScreen != latest.secureScreen) {
            withContext(Dispatchers.Main.immediate) {
                if (!latest.capture.enabled || !latest.capture.overlayEnabled || previous.secureScreen != latest.secureScreen) overlayInstance?.close(clearQueue = true)
                else overlayInstance?.cancelCountdown()
            }
        }
        NativeWork.schedule(context)
        scope.launch { NativeWidget.updateAll(context) }
    }
}
