package io.github.wcwac.beecount.capture

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** Event-owned resources are released on completion, replacement, rejection and shutdown. */
internal abstract class CaptureEventTask(val packageName: String, val contentEvent: Boolean) : Runnable {
    private val state = AtomicInteger(0)
    protected abstract fun process()
    protected abstract fun closeResource()
    final override fun run() { if (!state.compareAndSet(0, 1)) return; try { process() } finally { state.set(3); closeResource() } }
    fun discard() { if (state.compareAndSet(0, 2)) closeResource() }
}
internal class CaptureQueue {
    private val worker = ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, ArrayBlockingQueue(32), { task -> Thread(task, "beecount-capture") }, ThreadPoolExecutor.AbortPolicy())
    init { worker.allowCoreThreadTimeOut(true) }
    data class Accepted(val accepted: Boolean, val coalesced: Int, val evicted: Int)
    @Synchronized fun offer(task: CaptureEventTask): Accepted {
        if (worker.isShutdown) { task.discard(); return Accepted(false, 0, 0) }
        var coalesced = 0; var evicted = 0
        if (task.contentEvent) {
            for (queued in worker.queue.toTypedArray().reversed()) {
                val event = queued as? CaptureEventTask ?: break
                if (!event.contentEvent) break
                if (event.packageName == task.packageName && worker.remove(event)) { event.discard(); coalesced++ }
            }
        }
        val limit = worker.queue.size
        while (true) {
            try { worker.execute(task); return Accepted(true, coalesced, evicted) }
            catch (_: RejectedExecutionException) {
                if (!task.contentEvent && !worker.isShutdown && evicted < limit) {
                    val suffix = worker.queue.toTypedArray().reversed().takeWhile { it is CaptureEventTask && it.contentEvent }
                    val victim = suffix.lastOrNull() as? CaptureEventTask
                    if (victim != null && worker.remove(victim)) { victim.discard(); evicted++; continue }
                }
                task.discard(); return Accepted(false, coalesced, evicted)
            }
        }
    }
    /** Non-event callbacks remain ordering barriers and are never silently conflated. */
    @Synchronized fun execute(task: () -> Unit): Boolean = try { worker.execute(task); true } catch (_: RejectedExecutionException) { false }
    @Synchronized fun discardPending() { worker.queue.toTypedArray().filterIsInstance<CaptureEventTask>().forEach { if (worker.remove(it)) it.discard() } }
    @Synchronized fun close() { worker.shutdownNow().filterIsInstance<CaptureEventTask>().forEach(CaptureEventTask::discard) }
}
