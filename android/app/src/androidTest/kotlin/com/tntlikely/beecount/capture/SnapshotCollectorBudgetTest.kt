package com.tntlikely.beecount.capture

import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SnapshotCollectorBudgetTest {
    @Suppress("DEPRECATION")
    private fun node(value: String) = AccessibilityNodeInfo.obtain().apply { text = value }

    @Test fun aSourceTreeCanStillBeReadAfterTheRootTimeSliceWasConsumed() {
        var now = 0L
        val budget = CaptureReadBudget { now }
        val collector = SnapshotCollector()
        val root = node("工具栏")
        val source = node("支付成功")
        try {
            assertEquals("工具栏", collector.collect(root, budget).nodes.single().text)
            now = 24
            assertTrue(collector.collect(source, budget).nodes.isEmpty())
            budget.beginSourceAttempt()
            val selected = collector.collect(source, budget)
            assertEquals("支付成功", selected.nodes.single().text)
            assertFalse(selected.truncated)
            assertEquals(2, budget.visited)
        } finally { recycleCaptureNode(root); recycleCaptureNode(source) }
    }

    @Test fun sourceTimeReservationDoesNotGrantExtraNodeOrCharacterCapacity() {
        val source = node("支付成功")
        try {
            for (nodeLimit in listOf(true, false)) {
                val budget = CaptureReadBudget { 0L }
                if (nodeLimit) budget.visited = 400 else budget.characters = 6000
                budget.beginSourceAttempt()
                val result = SnapshotCollector().collect(source, budget)
                assertTrue(result.nodes.isEmpty())
                assertTrue(result.truncated)
            }
        } finally { recycleCaptureNode(source) }
    }
}
