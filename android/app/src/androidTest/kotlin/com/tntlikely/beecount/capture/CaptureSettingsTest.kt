package com.tntlikely.beecount.capture

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
}
