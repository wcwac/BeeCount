package com.tntlikely.beecount.capture

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.lang.ref.WeakReference

open class BeeCaptureService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val collector = SnapshotCollector()
    private val adapters = CaptureAdapterRegistry()
    private val pending = CapturePendingRead<AccessibilityNodeInfo>(
        post = { task, delay -> check(handler.postDelayed(task, delay)) },
        remove = handler::removeCallbacks,
        release = ::recycleCaptureNode,
    )
    private var firstPendingAt = 0L

    override fun onServiceConnected() {
        connected = WeakReference(this)
        refreshSettings()
        CaptureRuntime.worker.execute { CaptureRuntime.inbox(this).prune() }
    }

    fun refreshSettings() {
        handler.removeCallbacksAndMessages(null)
        pending.cancel()
        firstPendingAt = 0
        CaptureRuntime.reset()
        val settings = CaptureSettings.read(this)
        val info = serviceInfo ?: return
        // The in-code gate also prevents an empty package filter from collecting other apps.
        info.packageNames = settings.packages.toTypedArray()
        info.eventTypes = if (settings.enabled && settings.packages.isNotEmpty())
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED else 0
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val packageName = event.packageName?.toString() ?: return
        if (!CaptureSettings.read(this).accepts(packageName)) return
        CaptureDiagnostics.recordEvent(packageName)
        val now = SystemClock.elapsedRealtime()
        val pageClass = event.className?.toString().orEmpty()
        val windowId = event.windowId
        // Copy metadata immediately. A delayed read owns at most one source
        // handle on this thread, refreshes it before use, and never keeps events.
        val fallbackTexts = (event.text.take(8).mapNotNull { it?.toString()?.trim()?.take(240)?.takeIf(String::isNotEmpty) } +
            listOfNotNull(event.contentDescription?.toString()?.trim()?.take(240)?.takeIf(String::isNotEmpty))).distinct()
        val urgent = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            fallbackTexts.any { text -> listOf("支付成功", "付款成功", "退款成功", "交易成功", "支付失败", "付款失败").any(text::contains) }
        pending.cancel()
        var sourceReadFailed = false
        val source = try { readEventSource(event) } catch (_: RuntimeException) { sourceReadFailed = true; null }
        if (urgent) {
            firstPendingAt = 0
            try { observe(packageName, pageClass, windowId, fallbackTexts, source, sourceReadFailed = sourceReadFailed) }
            finally { recycleCaptureNode(source) }
            return
        }
        if (firstPendingAt == 0L) firstPendingAt = now
        try {
            pending.replace(source, if (now - firstPendingAt >= 500) 0 else 150) { retained ->
                firstPendingAt = 0
                observe(packageName, pageClass, windowId, fallbackTexts, retained,
                    refreshSource = true, sourceReadFailed = sourceReadFailed)
            }
        } catch (_: RuntimeException) {
            firstPendingAt = 0
            CaptureDiagnostics.recordRead(packageName, 0, 0, "READ_FAILED")
        }
    }

    private fun observe(packageName: String, pageClass: String, windowId: Int,
        eventTexts: List<String>, source: AccessibilityNodeInfo? = null, refreshSource: Boolean = false,
        sourceReadFailed: Boolean = false) {
        var root: AccessibilityNodeInfo? = null
        try {
            if (!CaptureSettings.read(this).accepts(packageName)) return
            var readFailed = sourceReadFailed
            val refreshed = if (refreshSource && source != null)
                try { refreshEventSource(source) } catch (_: RuntimeException) { readFailed = true; false } else true
            val usableSource = source?.takeIf { refreshed }
            root = try { readActiveRoot() } catch (_: RuntimeException) { readFailed = true; null }
            fun eligible(node: AccessibilityNodeInfo?) = node != null &&
                node.packageName?.toString() == packageName && (windowId < 0 || node.windowId == windowId)
            val selected = when {
                eligible(root) -> root
                eligible(usableSource) -> usableSource
                else -> null
            }
            val budget = CaptureReadBudget()
            var collected = selected?.let { collector.collect(it, budget) }
            // Known event IDs already match the eligible tree. If the event
            // has no window ID, coalesce by the window whose tree was read.
            var collectedWindowId = selected?.windowId?.takeIf { it >= 0 } ?: windowId
            // Some hybrid pages expose an opaque window tree while the event
            // source still has meaningful payment content. Try that source with
            // a reserved time slice, keeping the trees separate. Node/character
            // counters and the total 36 ms read deadline remain shared.
            fun hasCaptureContext(value: CollectedCaptureNodes?) = adapters.hasCaptureContext(value?.nodes.orEmpty())
            if (!hasCaptureContext(collected) && usableSource !== selected && eligible(usableSource)) {
                budget.beginSourceAttempt()
                val fromSource = collector.collect(usableSource!!, budget)
                if (hasCaptureContext(fromSource) || collected?.nodes?.none { it.text.isNotBlank() } != false) {
                    collected = fromSource
                    collectedWindowId = usableSource.windowId
                }
            }
            val usefulTree = collected?.nodes?.any { it.text.isNotBlank() } == true
            val nodes = if (usefulTree) collected!!.nodes else eventTexts.map { CaptureNode(it) }
            val textCount = nodes.count { it.text.isNotBlank() }
            CaptureDiagnostics.recordRead(packageName, nodes.size, textCount,
                if (textCount == 0 && (readFailed || !refreshed)) "READ_FAILED"
                else if (collected?.truncated == true) "TRUNCATED"
                else if (textCount > 0) "READABLE" else "EMPTY")
            // Fallback metadata must not be added to a different/newer visible
            // tree: that could combine an old success message with a new checkout.
            if (nodes.any { it.text.isNotBlank() } || CaptureAdapterRegistry.isResetPage(pageClass)) publishSnapshot(CaptureSnapshot(
                packageName, pageClass, if (usefulTree) collectedWindowId else windowId,
                System.currentTimeMillis(), SystemClock.elapsedRealtime(), nodes,
                truncated = collected?.truncated == true || !usefulTree,
            ))
        } catch (_: RuntimeException) {
            // A package/window transition can invalidate a node during a callback.
            CaptureDiagnostics.recordRead(packageName, 0, 0, "READ_FAILED")
        } finally {
            // The immediate caller or pending owner releases the source once.
            if (root !== source) recycleCaptureNode(root)
        }
    }

    // Isolate Android IPC boundaries for instrumentation without binding to or
    // launching a third-party application. Production always uses these APIs.
    internal open fun readEventSource(event: AccessibilityEvent): AccessibilityNodeInfo? = event.source
    internal open fun readActiveRoot(): AccessibilityNodeInfo? = rootInActiveWindow
    internal open fun refreshEventSource(node: AccessibilityNodeInfo): Boolean = node.refresh()
    internal open fun publishSnapshot(snapshot: CaptureSnapshot) = CaptureRuntime.submit(applicationContext, snapshot)

    override fun onInterrupt() {
        // This interrupts accessibility feedback, not the service connection.
        // There is no feedback to stop; retain the pending read and checkout.
    }

    private fun disconnect() {
        handler.removeCallbacksAndMessages(null)
        pending.cancel()
        firstPendingAt = 0
        // Local cleanup is idempotent. An old instance must not invalidate a
        // newer connection or its checkout/result session.
        if (connected?.get() === this) {
            connected = null
            CaptureRuntime.reset()
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        disconnect()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    companion object {
        @Volatile private var connected: WeakReference<BeeCaptureService>? = null
        fun isConnected() = connected?.get() != null
        fun settingsChanged() { connected?.get()?.refreshSettings() }
    }
}
