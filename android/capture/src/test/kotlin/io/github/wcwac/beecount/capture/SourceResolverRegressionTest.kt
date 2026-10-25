package io.github.wcwac.beecount.capture

import org.junit.Assert.*
import org.junit.Test

class SourceResolverRegressionTest {
    private class Node(val pkg: String, val window: Int) { var released = 0 }
    private class Window(val id: Int, val root: Node, val active: Boolean = false) { var released = 0; var reads = 0 }
    private class Provider(var active: Node?, val available: List<Window> = emptyList()) : SourceResolver.Provider<Node, Window> {
        var time = 0L; var activeDelay = 0L; var windowReads = 0
        override fun activeRoot(): Node? { time += activeDelay; return active }
        override fun windows(): List<Window> { windowReads++; return available }
        override fun isApplication(window: Window) = true
        override fun windowId(window: Window) = window.id
        override fun isActive(window: Window) = window.active
        override fun isFocused(window: Window) = false
        override fun windowRoot(window: Window): Node { window.reads++; return window.root }
        override fun packageName(node: Node) = node.pkg
        override fun nodeWindowId(node: Node) = node.window
        override fun releaseNode(node: Node) { node.released++ }
        override fun releaseWindow(window: Window) { window.released++ }
        fun resolve(initial: Node?, age: Long = 0) = SourceResolver.resolvePage(initial, "selected.app", 7, age, this) { time }
    }

    @Test fun leafSourceUsesCompleteRootAndNullSourceUsesMatchingInteractiveWindow() {
        // Existing CompletePageSourceResolverTest.aValidLeafSourceDoesNotPreventReadingTheCompleteSameWindow.
        val leaf = Node("selected.app", 7); val complete = Node("selected.app", 7)
        val provider = Provider(complete)
        val result = provider.resolve(leaf)
        assertSame(complete, result.node); assertEquals("ACTIVE_ROOT", result.route)
        assertEquals(1, leaf.released); assertEquals(0, complete.released); assertEquals(0, provider.windowReads)
        // Existing EventSourceResolverTest: null sources resolve through matching application windows.
        val other = Node("other.app", 8); val target = Node("selected.app", 7); val window = Window(7, target)
        val recovered = Provider(other, listOf(window)).resolve(null)
        assertSame(target, recovered.node); assertEquals("INTERACTIVE_WINDOW", recovered.route)
        assertEquals(1, other.released); assertEquals(1, window.released); assertEquals(0, target.released)
    }

    @Test fun wrongWindowOrSlowLookupCannotReplaceTheAlreadyValidatedEventSource() {
        // Existing CompletePageSourceResolverTest.aSamePackageDifferentWindowCannotReplaceTheOriginalSource.
        val leaf = Node("selected.app", 7); val wrong = Node("selected.app", 8); val window = Window(8, wrong)
        val result = Provider(wrong, listOf(window)).resolve(leaf)
        assertSame(leaf, result.node); assertEquals("EVENT_SOURCE", result.route)
        assertEquals(1, wrong.released); assertEquals(1, window.released); assertEquals(0, window.reads); assertEquals(0, leaf.released)
        // Existing CompletePageSourceResolverTest: a slow lookup retains the checked event source.
        val source = Node("selected.app", 7); val delayedRoot = Node("selected.app", 7)
        val delayed = Provider(delayedRoot).apply { activeDelay = 2001 }
        assertSame(source, delayed.resolve(source).node)
        assertEquals(1, delayedRoot.released); assertEquals(0, delayed.windowReads); assertEquals(0, source.released)
    }
}
