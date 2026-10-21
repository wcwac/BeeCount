package com.tntlikely.beecount.capture

import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.ContentValues
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Uses only the instrumentation APK's private sandbox, never the user's ledger. */
@RunWith(AndroidJUnit4::class)
class CaptureInboxStoreTest {
    private val context = captureTestContext()
    private lateinit var store: CaptureInboxStore
    @Before fun setup() {
        check(context.packageName.endsWith(".test"))
        context.deleteDatabase("capture_inbox.sqlite")
        store = CaptureInboxStore(context)
    }
    @After fun cleanup() { store.close(); context.deleteDatabase("capture_inbox.sqlite") }

    private fun candidate(amount: Long? = 1250) = CaptureCandidate(
        id = "fixture-one", sourcePackage = "com.tencent.mm", appName = "Fixture",
        adapterId = "wechat", sessionId = "session-one", ledgerId = 1, ledgerKey = "ledger-one",
        observedAt = 1_792_598_400_000, amountMinor = amount, merchant = "Test shop",
        remark = null, paymentMethod = null, discountMinor = null, transactionTime = null,
        referenceHash = null, direction = "expense", evidence = mapOf("amount" to "fixture"),
        issues = if (amount == null) setOf("AMOUNT_MISSING") else emptySet(),
    )

    @Test fun reopeningStoreRetainsDraftWithoutFlutter() {
        store.put(candidate())
        store.close()
        store = CaptureInboxStore(context)
        assertEquals(1, store.count())
        assertEquals("fixture-one", store.pending().single()["id"])
    }

    @Test fun pendingDraftCanBeEnrichedButTerminalDraftCannotReopen() {
        assertTrue(store.put(candidate(null)))
        assertFalse(store.put(candidate(1250)))
        assertEquals(1250L, (store.pending().single()["amountMinor"] as Number).toLong())
        store.complete("fixture-one", 7)
        assertFalse(store.put(candidate(1500)))
        assertEquals(0, store.count())
    }

    @Test fun dismissedDraftCannotReopenFromRepeatedEvents() {
        store.put(candidate())
        store.complete("fixture-one", null)
        store.put(candidate())
        assertEquals(0, store.count())
    }

    @Test fun changedLedgerIdentityDoesNotRetargetAnExistingDraft() {
        store.put(candidate())
        store.put(candidate().copy(ledgerKey = "replacement-ledger"))
        assertEquals("ledger-one", store.pending().single()["ledgerKey"])
    }

    @Test fun aLaterReferenceEnrichesTheSameDraftAcrossRecreation() {
        store.put(candidate())
        store.put(candidate().copy(transactionIdentity = "trade-one"))
        store.close()
        store = CaptureInboxStore(context)
        assertFalse(store.put(candidate().copy(id = "after-restart", sessionId = "new-session", transactionIdentity = "trade-one")))
        assertEquals(1, store.count())
        assertEquals("fixture-one", store.pending().single()["id"])
    }

    @Test fun durableReferenceCannotReopenACommittedPayment() {
        store.put(candidate().copy(transactionIdentity = "trade-one"))
        store.complete("fixture-one", 42)
        store.close()
        store = CaptureInboxStore(context)
        assertFalse(store.put(candidate().copy(id = "after-restart", transactionIdentity = "trade-one")))
        assertEquals(0, store.count())
    }

    @Test fun aConflictingAmountMustNotOverwriteAnExistingPayment() {
        store.put(candidate().copy(transactionIdentity = "trade-one"))
        store.put(candidate(3000).copy(id = "conflicting-payment", transactionIdentity = "trade-one"))
        val pending = store.pending()
        assertEquals(2, pending.size)
        val conflict = pending.single { it["id"] == "conflicting-payment" }
        assertTrue((conflict["issues"] as List<*>).contains("REFERENCE_CONFLICT"))
        assertEquals(1250L, (pending.single { it["id"] == "fixture-one" }["amountMinor"] as Number).toLong())
    }

    @Test fun identityEnrichmentDoesNotRenameAnotherPublishedDraft() {
        store.put(candidate().copy(transactionIdentity = "trade-one"))
        store.put(candidate().copy(id = "already-published"))
        store.put(candidate().copy(id = "already-published", transactionIdentity = "trade-one"))
        val second = store.pending().single { it["id"] == "already-published" }
        assertTrue((second["issues"] as List<*>).contains("REFERENCE_CONFLICT"))
        assertEquals(2, store.count())
    }

    @Test fun versionOneMigrationPreservesDraftsAndAddsDurableIdentityIndex() {
        store.close()
        val legacy = candidate()
        context.openOrCreateDatabase("capture_inbox.sqlite", 0, null).use { db ->
            db.execSQL("""CREATE TABLE candidates (
                id TEXT PRIMARY KEY, ledger_id INTEGER NOT NULL, observed_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL, state TEXT NOT NULL, payload TEXT NOT NULL,
                transaction_id INTEGER)""")
            db.execSQL("CREATE INDEX capture_pending ON candidates(state, observed_at)")
            val payload = JSONObject(legacy.asMap()).apply { remove("transactionIdentity") }
            db.insertOrThrow("candidates", null, ContentValues().apply {
                put("id", legacy.id); put("ledger_id", legacy.ledgerId)
                put("observed_at", legacy.observedAt); put("updated_at", legacy.observedAt)
                put("state", "READY"); put("payload", payload.toString())
            })
            db.version = 1
        }
        store = CaptureInboxStore(context)
        assertEquals("fixture-one", store.pending().single()["id"])
        assertFalse(store.put(legacy.copy(transactionIdentity = "trade-after-upgrade")))
        store.close()
        store = CaptureInboxStore(context)
        assertFalse(store.put(legacy.copy(id = "recreated-session", transactionIdentity = "trade-after-upgrade")))
        assertEquals(1, store.count())
        assertEquals(2, store.readableDatabase.version)
    }
}
