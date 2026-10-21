package com.tntlikely.beecount.capture

internal enum class CaptureEnqueueResult(val accepted: Boolean, val skipped: Boolean, val importantSkipped: Boolean = false) {
    ADDED(true, false), COALESCED(true, false), REPLACED_CONTENT(true, true),
    CONTENT_DROPPED(false, true), IMPORTANT_DROPPED(false, true, true),
}

/** Caller owns synchronization. Important observations retain FIFO order.
 * Superseded ordinary content can be coalesced; checkout/result/reset evidence
 * cannot. Reserving slots prevents ordinary animation churn filling the budget.
 */
internal class CaptureScheduler<T : Any>(private val capacity: Int, reservedImportant: Int) {
    private data class Entry<T>(val value: T, val key: Any?, val important: Boolean)
    private val contentLimit = capacity - reservedImportant
    private val entries = mutableListOf<Entry<T>>()
    init { require(capacity > 0 && reservedImportant in 0..capacity) }
    val size: Int get() = entries.size
    val isEmpty: Boolean get() = entries.isEmpty()

    fun offer(value: T, key: Any?, important: Boolean): CaptureEnqueueResult {
        val next = Entry(value, key, important)
        if (!important && key != null) {
            val previous = entries.indexOfFirst { !it.important && it.key == key }
            if (previous >= 0) {
                entries.removeAt(previous)
                entries += next // The replacement belongs at its new observation time.
                return CaptureEnqueueResult.COALESCED
            }
        }
        val oldestContent = entries.indexOfFirst { !it.important }
        val ordinaryLimitReached = !important && entries.count { !it.important } >= contentLimit
        if (entries.size >= capacity || ordinaryLimitReached) {
            if (oldestContent < 0) return if (important) CaptureEnqueueResult.IMPORTANT_DROPPED
                else CaptureEnqueueResult.CONTENT_DROPPED
            entries.removeAt(oldestContent)
            entries += next
            return CaptureEnqueueResult.REPLACED_CONTENT
        }
        entries += next
        return CaptureEnqueueResult.ADDED
    }

    fun poll(): T? = if (entries.isEmpty()) null else entries.removeAt(0).value
}

/** Caller owns synchronization. Acknowledging an old cursor cannot hide newer loss. */
internal class CaptureOverflowTracker(total: Long = 0, acknowledged: Long = 0, lastImportant: Long = 0) {
    private var total = total.coerceAtLeast(0)
    private var acknowledged = acknowledged.coerceIn(0, this.total)
    private var lastImportant = lastImportant.coerceIn(0, this.total)
    data class State(val total: Long, val acknowledged: Long, val lastImportant: Long) {
        val pending: Long get() = total - acknowledged
        val includesImportant: Boolean get() = lastImportant > acknowledged
    }

    fun snapshot() = State(total, acknowledged, lastImportant)
    fun record(important: Boolean) {
        if (total < Long.MAX_VALUE) total++
        if (important) lastImportant = total
    }
    fun acknowledge(observedCursor: Long) {
        require(observedCursor in 0..total) { "CAPTURE_WARNING_CHANGED" }
        acknowledged = maxOf(acknowledged, observedCursor)
    }
}
