package io.github.wcwac.beecount.capture

import org.junit.Assert.*
import org.junit.Test
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CaptureQueueRegressionTest {
    private class Task(pkg: String, content: Boolean, private val label: String, private val observed: MutableList<String>) : CaptureEventTask(pkg, content) {
        val processed = AtomicInteger()
        val closed = AtomicInteger()
        override fun process() { processed.incrementAndGet(); observed.add(label) }
        override fun closeResource() { closed.incrementAndGet() }
    }
    private fun block(queue: CaptureQueue, release: CountDownLatch) {
        val entered = CountDownLatch(1)
        assertTrue(queue.execute { entered.countDown(); try { release.await() } catch (_: InterruptedException) { Thread.currentThread().interrupt() } })
        assertTrue("Worker did not start", entered.await(3, TimeUnit.SECONDS))
    }
    private fun drain(queue: CaptureQueue, release: CountDownLatch) {
        val done = CountDownLatch(1)
        assertTrue(queue.execute { done.countDown() })
        release.countDown()
        assertTrue("Worker did not drain", done.await(3, TimeUnit.SECONDS))
    }

    @Test(timeout = 10000) fun burstKeepsOnlyNewestQueuedSourceAndClosesEveryOwnerOnce() {
        // Ported EventBacklogTest.aContentBurstProcessesOnlyTheNewestPendingNode.
        val queue = CaptureQueue(); val release = CountDownLatch(1)
        val observed = Collections.synchronizedList(mutableListOf<String>())
        val tasks = mutableListOf<Task>()
        try {
            block(queue, release)
            var coalesced = 0
            repeat(500) { index ->
                val task = Task("payment", true, "content-$index", observed); tasks += task
                val result = queue.offer(task); assertTrue(result.accepted); coalesced += result.coalesced
            }
            assertEquals(499, coalesced)
            drain(queue, release)
            assertEquals(listOf("content-499"), observed)
            tasks.forEachIndexed { index, task -> assertEquals(if (index == 499) 1 else 0, task.processed.get()); assertEquals(1, task.closed.get()); task.discard(); assertEquals(1, task.closed.get()) }
        } finally { release.countDown(); queue.close() }
    }

    @Test(timeout = 10000) fun checkoutAndCompletionEventsRetainTheirOrderingBoundary() {
        // Ported EventBacklogTest.stateBoundaryPreservesTheCheckoutContentItMayNeed.
        val queue = CaptureQueue(); val release = CountDownLatch(1)
        val observed = Collections.synchronizedList(mutableListOf<String>())
        val tasks = listOf(Task("a", true, "checkout", observed), Task("a", false, "success", observed), Task("a", true, "result", observed))
        try {
            block(queue, release)
            tasks.forEach { val result = queue.offer(it); assertTrue(result.accepted); assertEquals(0, result.coalesced) }
            drain(queue, release)
            assertEquals(listOf("checkout", "success", "result"), observed)
            tasks.forEach { assertEquals(1, it.closed.get()) }
        } finally { release.countDown(); queue.close() }
    }
}
