package com.tntlikely.beecount.capture

/** Process-local metadata only. Never retain screen text, amounts or transaction identities. */
internal class CaptureDiagnosticState(private val clock: () -> Long = System::currentTimeMillis) {
    private val startedAt = clock()
    private var eventCount = 0L
    private var readCount = 0L
    private var processedCount = 0L
    private var savedCount = 0L
    private val latest = mutableMapOf<String, Any>()

    @Synchronized fun recordEvent(packageName: String, source: String) {
        eventCount++
        latest["lastEventAt"] = clock()
        latest["lastEventPackage"] = packageName
        latest["lastEventSource"] = source
    }

    @Synchronized fun recordRead(packageName: String, nodeCount: Int, textCount: Int, result: String, source: String) {
        readCount++
        latest["lastReadAt"] = clock()
        latest["lastReadPackage"] = packageName
        latest["lastReadSource"] = source
        latest["lastReadResult"] = result
        latest["lastNodeCount"] = nodeCount.coerceAtLeast(0)
        latest["lastTextCount"] = textCount.coerceAtLeast(0)
    }

    @Synchronized fun recordProcessed(packageName: String, source: String, outcome: String) {
        processedCount++
        if (outcome == "DRAFT_SAVED") savedCount++
        latest["lastProcessedAt"] = clock()
        latest["lastProcessedPackage"] = packageName
        latest["lastProcessedSource"] = source
        latest["lastOutcome"] = outcome
    }

    @Synchronized fun snapshot(): Map<String, Any> = mapOf(
        "startedAt" to startedAt, "eventCount" to eventCount, "readCount" to readCount,
        "processedCount" to processedCount, "savedCount" to savedCount,
    ) + latest
}

internal object CaptureDiagnostics {
    private val state = CaptureDiagnosticState()
    fun recordEvent(packageName: String, source: String = "accessibility") = state.recordEvent(packageName, source)
    fun recordRead(packageName: String, nodeCount: Int, textCount: Int, result: String,
        source: String = "accessibility") = state.recordRead(packageName, nodeCount, textCount, result, source)
    fun recordProcessed(packageName: String, source: String, outcome: String) = state.recordProcessed(packageName, source, outcome)
    fun snapshot() = state.snapshot()
}
