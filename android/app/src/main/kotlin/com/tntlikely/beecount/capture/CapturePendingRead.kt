package com.tntlikely.beecount.capture

/** Main-thread owner of at most one delayed read and its source handle.
 * No AccessibilityEvent or live node enters the parser worker.
 */
internal class CapturePendingRead<T : Any>(
    private val post: (Runnable, Long) -> Unit,
    private val remove: (Runnable) -> Unit,
    private val release: (T) -> Unit,
) {
    private var task: Runnable? = null
    private var source: T? = null

    fun cancel() {
        val previousTask = task
        val previousSource = source
        task = null
        source = null
        previousTask?.let(remove)
        previousSource?.let(release)
    }

    fun replace(next: T?, delayMillis: Long, read: (T?) -> Unit) {
        cancel()
        lateinit var nextTask: Runnable
        nextTask = Runnable {
            // A removed callback must not consume or release a newer source.
            if (task !== nextTask) return@Runnable
            val owned = source
            task = null
            source = null
            try { read(owned) }
            finally { owned?.let(release) }
        }
        task = nextTask
        source = next
        try { post(nextTask, delayMillis) }
        catch (error: RuntimeException) { cancel(); throw error }
    }
}
