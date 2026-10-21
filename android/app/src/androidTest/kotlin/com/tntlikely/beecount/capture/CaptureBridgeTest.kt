package com.tntlikely.beecount.capture

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

/** Real bridge, runtime worker, parser, preferences and SQLite. No service or
 * Flutter engine is launched, and no notification or financial row is written.
 */
@RunWith(AndroidJUnit4::class)
class CaptureBridgeTest {
    private val context = object : ContextWrapper(captureTestContext("runtime_bridge")) {
        override fun getApplicationContext(): Context = this
        override fun getSystemService(name: String): Any? =
            if (name == Context.NOTIFICATION_SERVICE) null else super.getSystemService(name)
    }

    @Before fun setup() { clearFixtures() }
    @After fun cleanup() { clearFixtures() }

    private fun clearFixtures() {
        CaptureRuntime.worker.submit(Callable {
            CaptureRuntime.reset()
            val inbox = CaptureRuntime.inbox(context)
            // Never clean another test group's store or leave the cached helper
            // pointing at a closed/deleted database for a later test.
            check(inbox.writableDatabase.path == context.getDatabasePath("capture_inbox.sqlite").path)
            inbox.writableDatabase.delete("candidates", null, null)
            check(context.getSharedPreferences("local_capture_settings", Context.MODE_PRIVATE)
                .edit().clear().commit())
        }).get(10, TimeUnit.SECONDS)
    }

    private fun onMain(action: () -> Unit) {
        val task = FutureTask(Callable { action() })
        check(Handler(Looper.getMainLooper()).post(task))
        task.get(10, TimeUnit.SECONDS) // Propagate main-thread failures without an unbounded sync wait.
    }

    private fun payment(event: String, elapsed: Long) = CaptureSnapshot(
        packageName = "com.tencent.mm", pageClass = "notification", windowId = -1,
        observedAt = 1_792_598_400_000 + elapsed, elapsedAt = elapsed,
        nodes = listOf("支付成功", "支付金额", "￥12.50").map { CaptureNode(it) },
        source = "notification", sourceEventId = event,
    )

    @Test fun disablingCaptureAcknowledgesOnlyAfterOldWritesAndRejectsRemainingQueuedWork() {
        CaptureSettings.save(context, CaptureSettings(true, setOf("com.tencent.mm"), 1,
            "ledger-one", notifications = true))
        val frozen = CaptureSettings.read(context)
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val blockedWorker = CaptureRuntime.worker.submit(Callable {
            entered.countDown()
            check(release.await(10, TimeUnit.SECONDS))
        })
        val result = RecordingResult()
        var bridge: CaptureBridge? = null
        try {
            assertTrue(entered.await(10, TimeUnit.SECONDS))
            // drainOne yields after each observation: the settings operation must
            // run between these two queued payments rather than after the batch.
            CaptureRuntime.submit(context, payment("before-revocation", 1000))
            CaptureRuntime.submit(context, payment("still-queued-at-revocation", 2000))
            onMain {
                bridge = CaptureBridge(FixtureActivity(context), RegistrationMessenger())
                bridge!!.onMethodCall(MethodCall("updateSettings", mapOf(
                    "enabled" to false, "packages" to listOf("com.tencent.mm"),
                    "ledgerId" to 1L, "ledgerKey" to "ledger-one", "notifications" to true,
                )), result)
            }
            // The worker is still held, so these assertions need no timing race.
            assertEquals(1L, result.completed.count)
            assertTrue(CaptureSettings.read(context).enabled)
            release.countDown()
            blockedWorker.get(10, TimeUnit.SECONDS)
            assertTrue(result.completed.await(10, TimeUnit.SECONDS))
            assertTrue(result.succeeded)
            assertNull(result.errorCode)
            assertTrue(result.calledOnMainThread)
            val current = CaptureSettings.read(context)
            assertFalse(current.enabled)
            assertEquals(frozen.consentGeneration + 1, current.consentGeneration)

            CaptureRuntime.submit(context, payment("after-successful-revocation", 3000))
            val pending = CaptureRuntime.worker.submit(Callable {
                CaptureRuntime.inbox(context).pending()
            }).get(10, TimeUnit.SECONDS)
            assertEquals(1, pending.size)
            assertEquals("before-revocation", pending.single()["sessionId"])
            assertEquals(1250L, (pending.single()["amountMinor"] as Number).toLong())
            assertEquals("ledger-one", pending.single()["ledgerKey"])
        } finally {
            release.countDown()
            blockedWorker.get(10, TimeUnit.SECONDS)
            onMain { bridge?.close() }
        }
    }

    private class FixtureActivity(context: Context) : Activity() {
        init { attachBaseContext(context) }
    }

    // Only the Dart transport is absent. Production MethodCall routing and result
    // scheduling run unchanged; unexpected outbound traffic fails the fixture.
    private class RegistrationMessenger : BinaryMessenger {
        override fun setMessageHandler(channel: String, handler: BinaryMessenger.BinaryMessageHandler?) = Unit
        override fun send(channel: String, message: ByteBuffer?) {
            error("Unexpected outgoing message on $channel")
        }
        override fun send(channel: String, message: ByteBuffer?, callback: BinaryMessenger.BinaryReply?) {
            error("Unexpected outgoing message on $channel")
        }
    }

    private class RecordingResult : MethodChannel.Result {
        val completed = CountDownLatch(1)
        @Volatile var succeeded = false
        @Volatile var errorCode: String? = null
        @Volatile var calledOnMainThread = false
        override fun success(result: Any?) {
            succeeded = true
            calledOnMainThread = Looper.myLooper() == Looper.getMainLooper()
            completed.countDown()
        }
        override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
            this.errorCode = errorCode
            completed.countDown()
        }
        override fun notImplemented() { error("NOT_IMPLEMENTED", null, null) }
    }
}
