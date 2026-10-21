package com.tntlikely.beecount.capture

import org.junit.Assert.*
import org.junit.Test

/** Pure queue/cursor cases; these do not establish Android thread/IPC timings. */
class CaptureSchedulerTest {
    @Test fun importantObservationsDisplaceOrdinaryContentAndRetainTheirOrder() {
        val queue = CaptureScheduler<String>(capacity = 3, reservedImportant = 1)
        queue.offer("content-a", "a", false)
        queue.offer("content-b", "b", false)
        assertEquals(CaptureEnqueueResult.ADDED, queue.offer("checkout", null, true))
        assertEquals(CaptureEnqueueResult.REPLACED_CONTENT, queue.offer("success", null, true))
        assertEquals(3, queue.size)
        assertEquals("content-b", queue.poll())
        assertEquals("checkout", queue.poll())
        assertEquals("success", queue.poll())
        assertNull(queue.poll())
    }

    @Test fun coalescingMovesTheReplacementToItsActualObservationPosition() {
        val queue = CaptureScheduler<String>(capacity = 3, reservedImportant = 1)
        queue.offer("old-a", "a", false)
        queue.offer("other-window", "b", false)
        queue.offer("checkout", null, true)
        assertEquals(CaptureEnqueueResult.COALESCED, queue.offer("new-a", "a", false))
        assertEquals("other-window", queue.poll())
        assertEquals("checkout", queue.poll())
        assertEquals("new-a", queue.poll())
    }

    @Test fun ordinaryContentCannotConsumeReservedImportantSlots() {
        val queue = CaptureScheduler<String>(capacity = 3, reservedImportant = 2)
        queue.offer("content-a", "a", false)
        assertEquals(CaptureEnqueueResult.REPLACED_CONTENT, queue.offer("content-b", "b", false))
        assertEquals(CaptureEnqueueResult.ADDED, queue.offer("checkout", null, true))
        assertEquals(CaptureEnqueueResult.ADDED, queue.offer("refund", null, true))
        assertEquals(3, queue.size)
    }

    @Test fun aFullImportantQueueRejectsNewWorkWithoutReorderingOrEvictingOldEvidence() {
        val queue = CaptureScheduler<Int>(capacity = 3, reservedImportant = 1)
        for (id in 1..3) queue.offer(id, null, true)
        val dropped = queue.offer(4, null, true)
        assertFalse(dropped.accepted)
        assertTrue(dropped.skipped)
        assertTrue(dropped.importantSkipped)
        assertEquals(CaptureEnqueueResult.CONTENT_DROPPED, queue.offer(5, "content", false))
        assertEquals(listOf(1, 2, 3), List(3) { queue.poll() })
        assertTrue(queue.isEmpty)
    }

    @Test fun targetLedgerAndConsentArePartOfTheCoalescingKey() {
        val queue = CaptureScheduler<String>(capacity = 4, reservedImportant = 1)
        queue.offer("old-target", listOf("window", 1, "ledger-a", 0), false)
        queue.offer("new-target", listOf("window", 2, "ledger-b", 0), false)
        queue.offer("new-consent", listOf("window", 2, "ledger-b", 1), false)
        assertEquals(3, queue.size)
        assertEquals(listOf("old-target", "new-target", "new-consent"), List(3) { queue.poll() })
    }

    @Test fun anOldAcknowledgementCannotHideANewerImportantLoss() {
        val health = CaptureOverflowTracker()
        health.record(important = false)
        val shown = health.snapshot()
        health.record(important = true)
        health.acknowledge(shown.total)
        assertEquals(1L, health.snapshot().pending)
        assertTrue(health.snapshot().includesImportant)
        health.acknowledge(health.snapshot().total)
        assertEquals(0L, health.snapshot().pending)
        health.record(important = false)
        assertEquals(1L, health.snapshot().pending)
        assertFalse(health.snapshot().includesImportant)
    }

    @Test fun persistedLossStateCanBeRestoredWithoutReopeningAcknowledgedWarnings() {
        val first = CaptureOverflowTracker()
        first.record(important = true)
        first.acknowledge(first.snapshot().total)
        first.record(important = false)
        val saved = first.snapshot()
        val restored = CaptureOverflowTracker(saved.total, saved.acknowledged, saved.lastImportant)
        assertEquals(saved, restored.snapshot())
        assertEquals(1L, restored.snapshot().pending)
        assertFalse(restored.snapshot().includesImportant)
    }

    @Test fun aCursorFromAnotherRuntimeCannotAcknowledgeUnseenFutureLoss() {
        val health = CaptureOverflowTracker()
        health.record(important = true)
        try {
            health.acknowledge(2)
            fail("Future cursor must be rejected")
        } catch (_: IllegalArgumentException) { /* Expected. */ }
        assertEquals(1L, health.snapshot().pending)
    }

    @Test fun checkoutResultsFailuresAndHomeResetsAreClassifiedAsImportant() {
        val adapters = CaptureAdapterRegistry()
        fun input(text: String, page: String = "fixture.Page") = CaptureSnapshot(
            "com.tencent.mm", page, 1, 1_792_598_400_000L, 1000, listOf(CaptureNode(text)))
        for (text in listOf("确认付款", "支付成功", "退款成功", "付款失败", "交易详情",
            "收款成功", "购买成功", "转账成功", "免密支付成功", "先用后付自动扣款成功")) {
            assertTrue(text, adapters.mayAffectSession(input(text)))
        }
        assertTrue(adapters.mayAffectSession(input("首页", "com.tencent.mm.ui.LauncherUI")))
        assertFalse(adapters.mayAffectSession(input("轮播图第 3 页")))
    }
}
