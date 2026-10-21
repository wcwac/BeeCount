package com.tntlikely.beecount.capture

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CaptureSettingsTest {
    private val context = captureTestContext()
    @Before fun setup() {
        check(context.packageName.endsWith(".test"))
        clear()
    }
    @After fun clear() {
        check(context.packageName.endsWith(".test"))
        context.getSharedPreferences("local_capture_settings", Context.MODE_PRIVATE).edit().clear().commit()
    }
    private fun enabled() = CaptureSettings(true, setOf("com.tencent.mm", "com.eg.android.AlipayGphone"), 1, "ledger-one")

    @Test fun accessIsDisabledUntilExplicitlyConfigured() {
        val settings = CaptureSettings.read(context)
        assertFalse(settings.enabled)
        assertFalse(settings.notifications)
        assertFalse(settings.accepts("com.tencent.mm"))
    }

    @Test fun offAndOnCannotReuseTheOldConsentGeneration() {
        CaptureSettings.save(context, enabled())
        val observed = CaptureSettings.read(context)
        CaptureSettings.save(context, observed.copy(enabled = false))
        CaptureSettings.save(context, enabled())
        val current = CaptureSettings.read(context)
        assertTrue(current.enabled)
        assertNotEquals(observed.consentGeneration, current.consentGeneration)
    }

    @Test fun removingAPackageOrNotificationAccessInvalidatesQueuedObservations() {
        CaptureSettings.save(context, enabled().copy(notifications = true))
        val original = CaptureSettings.read(context)
        CaptureSettings.save(context, original.copy(packages = setOf("com.tencent.mm")))
        val narrowed = CaptureSettings.read(context)
        assertTrue(narrowed.consentGeneration > original.consentGeneration)
        CaptureSettings.save(context, narrowed.copy(notifications = false))
        assertTrue(CaptureSettings.read(context).consentGeneration > narrowed.consentGeneration)
    }

    @Test fun ledgerChangeDoesNotReassignAlreadyObservedWork() {
        CaptureSettings.save(context, enabled())
        val frozen = CaptureSettings.read(context)
        CaptureSettings.save(context, frozen.copy(ledgerId = 2, ledgerKey = "ledger-two"))
        val current = CaptureSettings.read(context)
        assertEquals(frozen.consentGeneration, current.consentGeneration)
        assertEquals(1L, frozen.ledgerId)
        assertEquals("ledger-one", frozen.ledgerKey)
        assertEquals(2L, current.ledgerId)
    }

    @Test fun concurrentRevocationsFromStaleSnapshotsCannotLoseTheConsentGeneration() {
        CaptureSettings.save(context, enabled().copy(notifications = true))
        val frozen = CaptureSettings.read(context)
        val otherContext = captureTestContext()
        val workers = Executors.newFixedThreadPool(2)
        val start = CountDownLatch(1)
        try {
            val removePackage = workers.submit(Callable {
                check(start.await(10, TimeUnit.SECONDS))
                CaptureSettings.save(context, frozen.copy(packages = setOf("com.tencent.mm")))
            })
            val removeNotifications = workers.submit(Callable {
                check(start.await(10, TimeUnit.SECONDS))
                CaptureSettings.save(otherContext, frozen.copy(notifications = false))
            })
            start.countDown()
            removePackage.get(10, TimeUnit.SECONDS)
            removeNotifications.get(10, TimeUnit.SECONDS)
        } finally {
            start.countDown()
            workers.shutdownNow()
            check(workers.awaitTermination(10, TimeUnit.SECONDS))
        }
        val current = CaptureSettings.read(captureTestContext())
        assertEquals(frozen.consentGeneration + 2, current.consentGeneration)
        assertTrue(current.enabled)
        assertEquals(frozen.ledgerId, current.ledgerId)
        assertEquals(frozen.ledgerKey, current.ledgerKey)
        // Either complete edit may win; neither may keep the old consent cursor.
        assertTrue(
            (current.packages == setOf("com.tencent.mm") && current.notifications) ||
                (current.packages == frozen.packages && !current.notifications),
        )
        assertEquals(current, CaptureSettings.read(otherContext))
        assertTrue(frozen.notifications)
        assertEquals(2, frozen.packages.size)
    }
}
