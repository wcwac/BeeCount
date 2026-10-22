package com.tntlikely.beecount.capture

import org.junit.Assert.*
import org.junit.Test

class CaptureDiagnosticsTest {
    @Test fun stagesDistinguishNoInputUnreadableAndUnrecognizedWithoutDrafts() {
        var now = 1000L
        val state = CaptureDiagnosticState { now }
        val initial = state.snapshot()
        assertEquals(0L, initial["eventCount"])
        assertNull(initial["lastOutcome"])
        now = 2000
        state.recordEvent("com.tencent.mm", "accessibility")
        state.recordRead("com.tencent.mm", 2, 0, "EMPTY", "accessibility")
        val empty = state.snapshot()
        assertEquals(2000L, empty["lastEventAt"])
        assertEquals("EMPTY", empty["lastReadResult"])
        assertEquals(0L, empty["processedCount"])
        now = 3000
        state.recordRead("com.tencent.mm", 5, 3, "READABLE", "accessibility")
        state.recordProcessed("com.tencent.mm", "accessibility", "UNRECOGNIZED")
        val unrecognized = state.snapshot()
        assertEquals(1L, unrecognized["eventCount"])
        assertEquals(2L, unrecognized["readCount"])
        assertEquals(1L, unrecognized["processedCount"])
        assertEquals(0L, unrecognized["savedCount"])
        assertEquals("UNRECOGNIZED", unrecognized["lastOutcome"])
        // Previously returned status is a snapshot, not a live mutable map.
        assertEquals("EMPTY", empty["lastReadResult"])
        assertEquals(1000L, unrecognized["startedAt"])
    }

    @Test fun writesDuplicatesAndFailuresRemainDistinctAcrossSources() {
        val state = CaptureDiagnosticState { 4000L }
        state.recordProcessed("com.tencent.mm", "accessibility", "WAITING_RESULT")
        state.recordProcessed("com.tencent.mm", "accessibility", "DRAFT_SAVED")
        state.recordProcessed("com.tencent.mm", "accessibility", "DUPLICATE")
        state.recordEvent("com.eg.android.AlipayGphone", "notification")
        state.recordRead("com.eg.android.AlipayGphone", 0, 0, "READ_FAILED", "notification")
        state.recordProcessed("com.eg.android.AlipayGphone", "notification", "PROCESSING_FAILED")
        val snapshot = state.snapshot()
        assertEquals(4L, snapshot["processedCount"])
        assertEquals(1L, snapshot["savedCount"])
        assertEquals("notification", snapshot["lastEventSource"])
        assertEquals("notification", snapshot["lastProcessedSource"])
        assertEquals("READ_FAILED", snapshot["lastReadResult"])
        assertEquals("PROCESSING_FAILED", snapshot["lastOutcome"])
        assertEquals(setOf("startedAt", "eventCount", "readCount", "processedCount", "savedCount",
            "lastEventAt", "lastEventPackage", "lastEventSource", "lastReadAt", "lastReadPackage", "lastReadSource",
            "lastReadResult", "lastNodeCount", "lastTextCount", "lastProcessedAt", "lastProcessedPackage",
            "lastProcessedSource", "lastOutcome"), snapshot.keys)
        // A new process starts empty rather than presenting historical success as current health.
        assertEquals(0L, CaptureDiagnosticState { 5000L }.snapshot()["savedCount"])
    }
}
