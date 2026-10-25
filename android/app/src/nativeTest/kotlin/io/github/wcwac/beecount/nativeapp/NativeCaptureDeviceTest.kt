package io.github.wcwac.beecount.nativeapp

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.UiAutomation
import android.content.ComponentName
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.wcwac.beecount.capture.NativeCaptureService
import io.github.wcwac.beecount.domain.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Existing null/leaf event-source regressions against our synthetic payment fixture, not a real payment application. */
@RunWith(AndroidJUnit4::class)
class NativeCaptureDeviceTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private lateinit var automation: UiAutomation
    private lateinit var container: NativeContainer
    private var original: UserPreferences? = null
    private var priorServices: String? = null
    private var priorEnabled: String? = null
    private var priorOverlay: String? = null
    private var observerPackages: Array<String>? = null
    private var observerFlags = 0
    private var observerSaved = false
    private lateinit var book: Book
    private lateinit var account: Account
    private lateinit var mapping: AccountMapping

    @Before fun prepare() {
        NativeDeviceSupport.cloudOnly()
        automation = NativeDeviceSupport.automation()
        container = NativeApplication.container(context)
        runBlocking { container.initialize(); original = container.settings.load(); container.updatePreferences { it.copy(capture = it.capture.copy(enabled = false), appLock = false, secureScreen = false) }; NativeCaptureService.invalidateAndDrain() }
        instrumentation.runOnMainSync { container.overlay().close(clearQueue = true); NativeCaptureService.resumeAfterRestore() }
        priorServices = shell("settings get secure enabled_accessibility_services").trim()
        priorEnabled = shell("settings get secure accessibility_enabled").trim()
        priorOverlay = Regex("SYSTEM_ALERT_WINDOW: ([a-z]+)").find(shell("appops get --user current ${quote(context.packageName)} SYSTEM_ALERT_WINDOW"))?.groupValues?.get(1) ?: "default"
        val observer = automation.serviceInfo
        observerPackages = observer.packageNames?.clone(); observerFlags = observer.flags; observerSaved = true
        observer.packageNames = arrayOf(context.packageName)
        observer.flags = observer.flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        automation.serviceInfo = observer
        shell("am force-stop ${quote(ALIPAY)}")
        book = Book(newId(), "云端采集验收")
        account = Account(newId(), "云端采集余额宝")
        mapping = AccountMapping(packageName = ALIPAY, paymentMethod = "余额宝", accountId = account.id)
        runBlocking {
            container.repository.saveBook(book); container.repository.saveAccount(account, 10_000); container.repository.saveMapping(mapping)
            container.updatePreferences { it.copy(capture = CaptureConfig(false, setOf(ALIPAY), book.id, overlayEnabled = true, autoSave = false)) }
        }
        shell("appops set --user current ${quote(context.packageName)} SYSTEM_ALERT_WINDOW allow")
        NativeDeviceSupport.await("Overlay permission was not granted") { Settings.canDrawOverlays(context) }
        val component = ComponentName(context, NativeCaptureService::class.java).flattenToString()
        val services = priorServices.orEmpty().takeUnless { it == "null" }.orEmpty().split(':').filter { it.isNotBlank() }.toMutableSet().apply { add(component) }
        shell("settings put secure enabled_accessibility_services ${quote(services.joinToString(":"))}")
        shell("settings put secure accessibility_enabled 1")
        NativeDeviceSupport.await("Capture service did not connect") { container.status.value.connected }
        runBlocking { container.updatePreferences { it.copy(capture = it.capture.copy(enabled = true)) } }
        NativeDeviceSupport.await("Selected package filter was not applied") { container.status.value.effectivePackages == setOf(ALIPAY) }
    }

    @After fun restore() {
        if (!::automation.isInitialized) return
        try {
            if (::container.isInitialized) {
                runBlocking { container.updatePreferences { it.copy(capture = it.capture.copy(enabled = false)) }; NativeCaptureService.invalidateAndDrain() }
                instrumentation.runOnMainSync { container.overlay().close(clearQueue = true) }
                shell("am force-stop ${quote(ALIPAY)}")
                runBlocking {
                    if (::book.isInitialized) container.repository.observePending().first().filter { it.bookId == book.id }.forEach { container.repository.dismiss(it.id, it.revision) }
                    if (::mapping.isInitialized) container.repository.deleteMapping(mapping.id)
                    if (::account.isInitialized) container.repository.archiveAccount(account.id, true)
                    if (::book.isInitialized) container.repository.archiveBook(book.id, true)
                }
            }
        } finally {
            try { priorServices?.let { restoreSecure("enabled_accessibility_services", it) } }
            finally {
                try { priorEnabled?.let { restoreSecure("accessibility_enabled", it) } }
                finally {
                    try { priorOverlay?.let { shell("appops set --user current ${quote(context.packageName)} SYSTEM_ALERT_WINDOW ${quote(it)}") } }
                    finally {
                        if (observerSaved) { val observer = automation.serviceInfo; observer.packageNames = observerPackages; observer.flags = observerFlags; automation.serviceInfo = observer }
                        instrumentation.runOnMainSync { NativeCaptureService.resumeAfterRestore() }
                        original?.let { old -> runBlocking { container.updatePreferences { old } } }
                    }
                }
            }
        }
    }

    @Test fun nullEventSourceFallsBackToVerifiedPaymentWindow() {
        launch("--ez fixture_null_source true --es fixture_amount 28.60")
        val candidate = awaitCandidate(2860)
        assertEquals("实验支付宝商户", candidate.proposal.merchant)
        assertEquals("余额宝", candidate.proposal.paymentMethod)
        assertEquals(account.id, candidate.accountId)
        assertTrue(candidate.reasons.contains(ConfirmationReason.WEAK_IDENTITY))
        assertTrue(container.status.value.reads > 0)
        NativeDeviceSupport.await("Candidate exists but overlay did not attach: ${container.status.value}") { container.status.value.overlayResult == "ATTACHED" }
        NativeDeviceSupport.screenshot(automation, "capture-null-source-overlay")
    }

    @Test fun amountLeafReadsWholeWindowAndOverlaySavesExactlyOnce() {
        launch("--ez fixture_leaf_source true --es fixture_amount 31.40")
        val candidate = awaitCandidate(3140)
        assertEquals("实验支付宝商户", candidate.proposal.merchant); assertEquals("余额宝", candidate.proposal.paymentMethod)
        NativeDeviceSupport.await("Manual confirmation overlay did not attach") { container.status.value.overlayResult == "ATTACHED" }
        NativeDeviceSupport.screenshot(automation, "capture-leaf-source-overlay")
        NativeDeviceSupport.clickText(automation, "确认", scroll = true)
        NativeDeviceSupport.await("Overlay confirmation did not persist: ${container.status.value}") { runBlocking { container.repository.candidate(candidate.id)?.state == CandidateState.SAVED } }
        runBlocking {
            val saved = requireNotNull(container.repository.candidate(candidate.id))
            val entry = requireNotNull(saved.entryId?.let { container.repository.entry(it) })
            assertEquals(3140L, entry.draft.amount.minor)
            assertEquals(6860L, container.repository.observeAccounts().first().single { it.id == account.id }.balance)
            val retried = container.repository.execute(FinancialCommand.Save(entry.draft, candidateId = candidate.id, candidateRevision = candidate.revision))
            assertTrue(retried.toString(), retried is CommitResult.Success)
            assertEquals(6860L, container.repository.observeAccounts().first().single { it.id == account.id }.balance)
        }
    }

    private fun awaitCandidate(amount: Long): CaptureCandidate {
        var found: CaptureCandidate? = null
        NativeDeviceSupport.await("Fixture produced no candidate: ${container.status.value}", 25_000) {
            found = runBlocking { container.repository.observePending().first().firstOrNull { it.bookId == book.id && it.proposal.packageName == ALIPAY && it.proposal.amount.minor == amount } }; found != null
        }
        return requireNotNull(found)
    }
    private fun launch(extras: String) { shell("am start -W -n ${quote("$ALIPAY/com.alipay.android.msp.ui.views.MspContainerActivity")} $extras") }
    private fun shell(command: String) = NativeDeviceSupport.shell(automation, command)
    private fun quote(value: String) = NativeDeviceSupport.quote(value)
    private fun restoreSecure(key: String, value: String) { shell(if (value == "null") "settings delete secure $key" else "settings put secure $key ${quote(value)}") }
    private companion object { const val ALIPAY = "com.eg.android.AlipayGphone" }
}
