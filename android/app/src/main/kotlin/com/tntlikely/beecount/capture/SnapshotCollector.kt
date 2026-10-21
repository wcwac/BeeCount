package com.tntlikely.beecount.capture

import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo

internal data class CollectedCaptureNodes(val nodes: List<CaptureNode>, val truncated: Boolean)

/** Shared by window/source attempts in the same observation. */
internal class CaptureReadBudget {
    val started = SystemClock.elapsedRealtime()
    var visited = 0
    var characters = 0
}

/** Reads one owned tree, with a shared budget for every descendant. */
internal class SnapshotCollector {
    fun collect(root: AccessibilityNodeInfo, budget: CaptureReadBudget = CaptureReadBudget()): CollectedCaptureNodes {
        val nodes = mutableListOf<CaptureNode>()
        var truncated = false
        fun exhausted() = budget.visited >= MAX_NODES || budget.characters >= MAX_CHARACTERS ||
            SystemClock.elapsedRealtime() - budget.started >= MAX_MILLIS

        fun addText(value: String, parent: Int, depth: Int, viewId: String?): Int {
            val index = nodes.size
            val remaining = minOf(MAX_NODE_CHARACTERS, MAX_CHARACTERS - budget.characters)
            val safe = value.take(remaining.coerceAtLeast(0))
            if (safe.length < value.length) truncated = true
            nodes += CaptureNode(safe, viewId, parent, depth)
            budget.characters += safe.length
            return index
        }

        fun visit(node: AccessibilityNodeInfo, parent: Int, depth: Int) {
            if (depth > MAX_DEPTH || exhausted()) { truncated = true; return }
            budget.visited++
            val text = node.text?.toString()?.trim().orEmpty()
            val description = node.contentDescription?.toString()?.trim().orEmpty()
            val id = node.viewIdResourceName
            // Structural nodes retain their indices even when they have no text.
            val index = addText(text, parent, depth, id)
            if (description.isNotEmpty() && description != text) {
                addText(description, index, depth + 1, id)
            }
            val children = node.childCount
            for (i in 0 until children) {
                if (depth >= MAX_DEPTH || exhausted()) { truncated = true; break }
                val child = node.getChild(i) ?: continue
                if (child === node) { truncated = true; continue }
                try { visit(child, index, depth + 1) }
                finally { recycleCaptureNode(child) }
            }
        }
        try { visit(root, -1, 0) }
        catch (_: RuntimeException) { truncated = true }
        return CollectedCaptureNodes(nodes.toList(), truncated)
    }

    companion object {
        private const val MAX_NODES = 400
        private const val MAX_CHARACTERS = 6000
        private const val MAX_NODE_CHARACTERS = 240
        private const val MAX_DEPTH = 16
        private const val MAX_MILLIS = 24L
    }
}

/** API 33 removed pooling; older Android versions still require owned nodes returned. */
@Suppress("DEPRECATION")
internal fun recycleCaptureNode(node: AccessibilityNodeInfo?) {
    if (node != null && Build.VERSION.SDK_INT < 33) {
        try { node.recycle() } catch (_: RuntimeException) { /* A vanished window must not stop capture. */ }
    }
}
