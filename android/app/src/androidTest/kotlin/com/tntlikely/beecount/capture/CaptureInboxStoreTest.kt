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

    @Test fun aSparseDurableAliasPreservesKnownFieldsEvidenceAndWarningsAfterRecreation() {
        val original = candidate().copy(
            transactionIdentity = "trade-one", referenceHash = "reference-one",
            remark = "Breakfast", paymentMethod = "Debit card", discountMinor = 100,
            transactionTime = 1_792_598_300_000L,
            evidence = mapOf("amount" to "label:支付金额", "direction" to "signed-result", "merchant" to "label"),
            issues = setOf("SNAPSHOT_TRUNCATED", "CURRENCY_UNCERTAIN", "FEE_PRESENT"),
        )
        store.put(original)
        store.close()
        store = CaptureInboxStore(context)
        assertFalse(store.put(candidate(null).copy(
            id = "after-restart", sessionId = "new-session", transactionIdentity = "trade-one",
            merchant = null, direction = null, evidence = mapOf("state" to "success-label"),
        )))
        val saved = store.pending().single()
        assertEquals("fixture-one", saved["id"])
        assertEquals(1250L, (saved["amountMinor"] as Number).toLong())
        assertEquals(original.merchant, saved["merchant"])
        assertEquals(original.remark, saved["remark"])
        assertEquals(original.paymentMethod, saved["paymentMethod"])
        assertEquals(100L, (saved["discountMinor"] as Number).toLong())
        assertEquals(original.transactionTime, (saved["transactionTime"] as Number).toLong())
        assertEquals(original.referenceHash, saved["referenceHash"])
        assertEquals(original.direction, saved["direction"])
        val evidence = saved["evidence"] as Map<*, *>
        assertEquals("label:支付金额", evidence["amount"])
        assertEquals("signed-result", evidence["direction"])
        assertEquals("success-label", evidence["state"])
        val issues = saved["issues"] as List<*>
        assertTrue(issues.containsAll(original.issues))
        assertFalse(issues.contains("AMOUNT_MISSING"))
    }

    @Test fun aDurableAliasCanSupplyMissingFieldsAndResolveTheirWarnings() {
        store.put(candidate(null).copy(
            transactionIdentity = "trade-one", merchant = null, direction = null,
            issues = setOf("AMOUNT_MISSING", "AMOUNT_UNANCHORED", "MERCHANT_UNCERTAIN", "PAYMENT_UNCONFIRMED", "SNAPSHOT_TRUNCATED"),
        ))
        store.close()
        store = CaptureInboxStore(context)
        assertFalse(store.put(candidate().copy(
            id = "after-restart", transactionIdentity = "trade-one", paymentMethod = "Debit card",
            transactionTime = 1_792_598_300_000L,
            evidence = mapOf("amount" to "label:支付金额", "direction" to "payment-state"),
        )))
        val saved = store.pending().single()
        assertEquals(1250L, (saved["amountMinor"] as Number).toLong())
        assertEquals("Test shop", saved["merchant"])
        assertEquals("Debit card", saved["paymentMethod"])
        assertEquals(1_792_598_300_000L, (saved["transactionTime"] as Number).toLong())
        assertEquals("expense", saved["direction"])
        assertEquals(listOf("SNAPSHOT_TRUNCATED"), saved["issues"])
    }

    @Test fun explicitAmountConflictInvalidatesTheAmountOfADurableAlias() {
        store.put(candidate().copy(transactionIdentity = "trade-one"))
        store.close()
        store = CaptureInboxStore(context)
        store.put(candidate(null).copy(
            id = "after-restart", transactionIdentity = "trade-one",
            issues = setOf("AMOUNT_MISSING", "AMOUNT_CONFLICT"),
        ))
        val saved = store.pending().single()
        assertNull(saved["amountMinor"])
        assertTrue((saved["issues"] as List<*>).containsAll(setOf("AMOUNT_MISSING", "AMOUNT_CONFLICT")))
    }

    @Test fun rejectedOriginalRefundAmountCannotBeRestoredFromADurableAlias() {
        store.put(candidate().copy(transactionIdentity = "trade-one"))
        store.close()
        store = CaptureInboxStore(context)
        store.put(candidate(null).copy(
            id = "after-restart", transactionIdentity = "trade-one",
            evidence = mapOf("amount" to "refund:original-amount-rejected"),
        ))
        val saved = store.pending().single()
        assertNull(saved["amountMinor"])
        assertTrue((saved["issues"] as List<*>).contains("AMOUNT_MISSING"))
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
