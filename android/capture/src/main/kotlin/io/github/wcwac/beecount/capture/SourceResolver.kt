package io.github.wcwac.beecount.capture

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import java.util.IdentityHashMap

/** Acquires a complete page belonging to the event's application and window only. */
internal object SourceResolver {
    data class Resolved<N>(val node: N?, val route: String, val result: String)

    /** The platform boundary also lets the existing source-selection regressions run on the JVM. */
    interface Provider<N : Any, W : Any> {
        fun activeRoot(): N?
        fun windows(): List<W>
        fun isApplication(window: W): Boolean
        fun windowId(window: W): Int
        fun isActive(window: W): Boolean
        fun isFocused(window: W): Boolean
        fun windowRoot(window: W): N?
        fun packageName(node: N): String?
        fun nodeWindowId(node: N): Int
        fun releaseNode(node: N)
        fun releaseWindow(window: W)
    }

    fun resolve(service: AccessibilityService, initial: AccessibilityNodeInfo?, pkg: String, windowId: Int, age: Long): Resolved<AccessibilityNodeInfo> =
        resolvePage(initial, pkg, windowId, age, object : Provider<AccessibilityNodeInfo, AccessibilityWindowInfo> {
            override fun activeRoot() = service.rootInActiveWindow
            override fun windows() = service.windows.orEmpty()
            override fun isApplication(window: AccessibilityWindowInfo) = window.type == AccessibilityWindowInfo.TYPE_APPLICATION
            override fun windowId(window: AccessibilityWindowInfo) = window.id
            override fun isActive(window: AccessibilityWindowInfo) = window.isActive
            override fun isFocused(window: AccessibilityWindowInfo) = window.isFocused
            override fun windowRoot(window: AccessibilityWindowInfo) = window.root
            override fun packageName(node: AccessibilityNodeInfo) = node.packageName?.toString()
            override fun nodeWindowId(node: AccessibilityNodeInfo) = node.windowId
            override fun releaseNode(node: AccessibilityNodeInfo) = release(node)
            override fun releaseWindow(window: AccessibilityWindowInfo) {
                if (Build.VERSION.SDK_INT < 33) { @Suppress("DEPRECATION") window.recycle() }
            }
        }, SystemClock::elapsedRealtime)

    fun <N : Any, W : Any> resolvePage(initial: N?, pkg: String, windowId: Int, age: Long, provider: Provider<N, W>, now: () -> Long): Resolved<N> {
        val owned = java.util.Collections.newSetFromMap(IdentityHashMap<N, Boolean>())
        val windows = java.util.Collections.newSetFromMap(IdentityHashMap<W, Boolean>())
        val start = now(); var reason = "SOURCE_UNAVAILABLE"; var route = "NONE"
        var selected: N? = null; var retained: N? = null
        fun within(): Boolean {
            val elapsed = now() - start
            if (elapsed >= 2000) { reason = "SOURCE_LOOKUP_TIMEOUT"; return false }
            if (age + elapsed > 2500) { reason = "SOURCE_EXPIRED"; return false }
            return true
        }
        fun accept(node: N): Boolean {
            if (provider.packageName(node) != pkg || windowId >= 0 && provider.nodeWindowId(node) != windowId) { reason = "SOURCE_WINDOW_MISMATCH"; return false }
            return true
        }
        fun own(node: N?): N? { node?.let(owned::add); return node }
        try {
            own(initial)
            try { if (initial != null && accept(initial)) retained = initial } catch (_: RuntimeException) { reason = "SOURCE_LOOKUP_FAILED" }
            if (within()) {
                try { val active = own(provider.activeRoot()); if (within() && active != null && accept(active)) { selected = active; route = "ACTIVE_ROOT" } }
                catch (_: RuntimeException) { reason = "SOURCE_LOOKUP_FAILED" }
            }
            if (selected == null && within()) {
                try {
                    val available = provider.windows(); windows.addAll(available); var count = 0
                    for (window in available) {
                        if (!within() || count >= 8) break
                        try {
                            if (!provider.isApplication(window)) continue
                            if (windowId >= 0) { if (provider.windowId(window) != windowId) continue } else if (!provider.isActive(window) && !provider.isFocused(window)) continue
                            count++; val root = own(provider.windowRoot(window))
                            if (!within()) break
                            if (root != null && accept(root)) { selected = root; route = "INTERACTIVE_WINDOW"; break }
                        } catch (_: RuntimeException) { reason = "SOURCE_LOOKUP_FAILED" }
                    }
                } catch (_: RuntimeException) { reason = "SOURCE_LOOKUP_FAILED" }
            }
            // Preserve 73: an expired fallback lookup cannot invalidate an already owned, validated event source.
            if (selected == null && retained != null) { selected = retained; route = "EVENT_SOURCE" }
            return Resolved(selected, route, if (selected != null) "RESOLVED" else reason)
        } finally {
            owned.filter { it !== selected }.forEach { try { provider.releaseNode(it) } catch (_: RuntimeException) {} }
            windows.forEach { try { provider.releaseWindow(it) } catch (_: RuntimeException) {} }
        }
    }
    fun release(node: AccessibilityNodeInfo?) { if (node != null && Build.VERSION.SDK_INT < 33) try { @Suppress("DEPRECATION") node.recycle() } catch (_: RuntimeException) {} }
}
