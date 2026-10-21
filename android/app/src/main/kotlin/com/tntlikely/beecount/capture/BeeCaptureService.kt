package com.tntlikely.beecount.capture

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.lang.ref.WeakReference

class BeeCaptureService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val collector = SnapshotCollector()
    private var pending: Runnable? = null
    private var firstPendingAt = 0L

    override fun onServiceConnected() {
        connected = WeakReference(this)
        refreshSettings()
        CaptureRuntime.worker.execute { CaptureRuntime.inbox(this).prune() }
    }

    fun refreshSettings() {
        handler.removeCallbacksAndMessages(null)
        pending = null
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
        val now = SystemClock.elapsedRealtime()
        val pageClass = event.className?.toString().orEmpty()
        val windowId = event.windowId
        // Copy event metadata immediately; no live AccessibilityEvent or source
        // node is retained by a delayed Runnable.
        val fallbackTexts = (event.text.take(8).mapNotNull { it?.toString()?.trim()?.take(240)?.takeIf(String::isNotEmpty) } +
            listOfNotNull(event.contentDescription?.toString()?.trim()?.take(240)?.takeIf(String::isNotEmpty))).distinct()
        val urgent = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            fallbackTexts.any { text -> listOf("支付成功", "付款成功", "退款成功", "交易成功", "支付失败", "付款失败").any(text::contains) }
        pending?.let(handler::removeCallbacks)
        if (urgent) {
            pending = null
            firstPendingAt = 0
            val source = try { event.source } catch (_: RuntimeException) { null }
            observe(packageName, pageClass, windowId, fallbackTexts, source)
            return
        }
        if (firstPendingAt == 0L) firstPendingAt = now
        val task = Runnable {
            pending = null; firstPendingAt = 0
            observe(packageName, pageClass, windowId, fallbackTexts)
        }
        pending = task
        handler.postDelayed(task, if (now - firstPendingAt >= 500) 0 else 150)
    }

    private fun observe(packageName: String, pageClass: String, windowId: Int,
        eventTexts: List<String>, source: AccessibilityNodeInfo? = null) {
        var root: AccessibilityNodeInfo? = null
        try {
            if (!CaptureSettings.read(this).accepts(packageName)) return
            root = try { rootInActiveWindow } catch (_: RuntimeException) { null }
            fun eligible(node: AccessibilityNodeInfo?) = node != null &&
                node.packageName?.toString() == packageName && (windowId < 0 || node.windowId == windowId)
            val selected = when {
                eligible(root) -> root
                eligible(source) -> source
                else -> null
            }
            val budget = CaptureReadBudget()
            var collected = selected?.let { collector.collect(it, budget) }
            // Known event IDs already match the eligible tree. If the event
            // has no window ID, coalesce by the window whose tree was read.
            var collectedWindowId = selected?.windowId?.takeIf { it >= 0 } ?: windowId
            // Some hybrid pages expose an opaque window tree while the event
            // source still has meaningful payment content. Try that source with
            // the remaining budget, keeping the two trees separate.
            fun hasCaptureContext(value: CollectedCaptureNodes?) = value?.nodes?.any { node ->
                CAPTURE_CONTEXT.any(node.text::contains)
            } == true
            if (!hasCaptureContext(collected) && source !== selected && eligible(source)) {
                val fromSource = collector.collect(source!!, budget)
                if (hasCaptureContext(fromSource) || collected?.nodes?.none { it.text.isNotBlank() } != false) {
                    collected = fromSource
                    collectedWindowId = source.windowId
                }
            }
            val usefulTree = collected?.nodes?.any { it.text.isNotBlank() } == true
            val nodes = if (usefulTree) collected!!.nodes else eventTexts.map { CaptureNode(it) }
            // Fallback metadata must not be added to a different/newer visible
            // tree: that could combine an old success message with a new checkout.
            if (nodes.any { it.text.isNotBlank() } || CaptureAdapterRegistry.isResetPage(pageClass)) CaptureRuntime.submit(applicationContext, CaptureSnapshot(
                packageName, pageClass, if (usefulTree) collectedWindowId else windowId,
                System.currentTimeMillis(), SystemClock.elapsedRealtime(), nodes,
                truncated = collected?.truncated == true || !usefulTree,
            ))
        } catch (_: RuntimeException) {
            // A package/window transition can invalidate a node during a callback.
        } finally {
            recycleCaptureNode(root)
            if (source !== root) recycleCaptureNode(source)
        }
    }

    override fun onInterrupt() { handler.removeCallbacksAndMessages(null); pending = null; firstPendingAt = 0; CaptureRuntime.reset() }
    override fun onDestroy() {
        if (connected?.get() === this) connected = null
        onInterrupt()
        super.onDestroy()
    }

    companion object {
        private val CAPTURE_CONTEXT = listOf("支付成功", "付款成功", "交易成功", "退款成功", "支付失败", "付款失败",
            "确认付款", "确认订单", "提交订单", "交易详情", "账单详情", "下单成功", "实付", "合计", "现价")
        private var connected: WeakReference<BeeCaptureService>? = null
        fun isConnected() = connected?.get() != null
        fun settingsChanged() { connected?.get()?.refreshSettings() }
    }
}
