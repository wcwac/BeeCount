package com.tntlikely.beecount.capture

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

/** Real Handler and node reads, with only Android's external-window IPC replaced.
 * No service binding, third-party client, ledger write or notification is used.
 */
@RunWith(AndroidJUnit4::class)
class CaptureSourceReadTest {
    private val context = captureTestContext()
    private val snapshots = mutableListOf<CaptureSnapshot>() // Main thread only.
    private val published = CountDownLatch(1)
    private lateinit var service: FixtureService

    private fun onMain(action: () -> Unit) {
        val task = FutureTask(Callable { action() })
        check(Handler(Looper.getMainLooper()).post(task))
        task.get(10, TimeUnit.SECONDS)
    }

    @Before fun setup() {
        CaptureSettings.save(context, CaptureSettings(true, setOf("com.tencent.mm"), 1, "fixture-ledger"))
        onMain { service = FixtureService(context) { snapshots += it; published.countDown() } }
    }

    @After fun cleanup() {
        if (::service.isInitialized) onMain { service.onDestroy() }
        context.getSharedPreferences("local_capture_settings", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Suppress("DEPRECATION")
    private fun contentEvent(): AccessibilityEvent = AccessibilityEvent.obtain(
        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED).apply {
        packageName = "com.tencent.mm"
        className = "fixture.HybridContent"
        // Empty event text makes this nonurgent; only its source has the result.
    }

    @Test fun aNonurgentContentEventCanReadItsSourceBehindAnOpaqueWindowRoot() {
        onMain {
            val event = contentEvent()
            try { service.onAccessibilityEvent(event) }
            finally { recycleEvent(event) }
            assertTrue(snapshots.isEmpty()) // The real debounce is still pending.
        }
        assertTrue(published.await(10, TimeUnit.SECONDS))
        onMain {
            assertEquals(1, service.refreshCount)
            assertEquals(listOf("支付成功"), snapshots.single().texts)
        }
    }

    @Test fun anUnrefreshableDelayedSourceIsNotUsedAsPaymentEvidence() {
        onMain {
            service.refreshResult = false
            val event = contentEvent()
            try { service.onAccessibilityEvent(event) }
            finally { recycleEvent(event) }
        }
        assertTrue(published.await(10, TimeUnit.SECONDS))
        onMain {
            assertEquals(1, service.refreshCount)
            assertEquals(listOf("工具栏"), snapshots.single().texts)
        }
    }

    @Test fun aProfileReceiptStatusCanSelectTheDelayedSourceBehindAnOpaqueRoot() {
        onMain {
            service.sourceText = "已收款"
            val event = contentEvent()
            try { service.onAccessibilityEvent(event) }
            finally { recycleEvent(event) }
        }
        assertTrue(published.await(10, TimeUnit.SECONDS))
        onMain {
            assertEquals(1, service.refreshCount)
            assertEquals(listOf("已收款"), snapshots.single().texts)
        }
    }

    @Test fun settingsRefreshCancelsThePendingReadBeforeItCanPublish() {
        val fence = CountDownLatch(1)
        onMain {
            val event = contentEvent()
            try { service.onAccessibilityEvent(event) }
            finally { recycleEvent(event) }
            service.refreshSettings()
            // Runs after the original read's deadline, without sleeping or
            // relying on a test-thread race against the main-thread callback.
            check(Handler(Looper.getMainLooper()).postDelayed({ fence.countDown() }, 200))
        }
        assertTrue(fence.await(10, TimeUnit.SECONDS))
        onMain { assertTrue(snapshots.isEmpty()); assertEquals(0, service.refreshCount) }
    }

    @Suppress("DEPRECATION")
    private fun recycleEvent(event: AccessibilityEvent) { event.recycle() }

    private class FixtureService(context: Context, private val sink: (CaptureSnapshot) -> Unit) : BeeCaptureService() {
        var refreshResult = true
        var refreshCount = 0
        var sourceText = "支付成功"
        init { attachBaseContext(context) }
        override fun readEventSource(event: AccessibilityEvent): AccessibilityNodeInfo = node(sourceText)
        override fun readActiveRoot(): AccessibilityNodeInfo = node("工具栏")
        override fun refreshEventSource(node: AccessibilityNodeInfo): Boolean {
            refreshCount++
            return refreshResult
        }
        override fun publishSnapshot(snapshot: CaptureSnapshot) = sink(snapshot)
    }

    companion object {
        @Suppress("DEPRECATION")
        private fun node(value: String): AccessibilityNodeInfo = AccessibilityNodeInfo.obtain().apply {
            text = value
            packageName = "com.tencent.mm"
        }
    }
}
