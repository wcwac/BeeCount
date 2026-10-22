package com.tntlikely.beecount.capture

import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.ContentValues
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Uses only prefixed instrumentation fixtures, never the user's ledger. */
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

    @Test fun legacyLongTextIsAdaptedWithoutHidingOtherDraftsOrRewritingStoredPayloads() {
        val legacy = candidate().copy(id = "legacy-long-text", remark = "r".repeat(600),
            paymentMethod = "p".repeat(600), referenceHash = "legacy-reference",
            transactionIdentity = "legacy-trade")
        val normal = candidate(2500).copy(id = "normal-text", remark = "r".repeat(512),
            paymentMethod = "p".repeat(12), referenceHash = "normal-reference",
            transactionIdentity = "normal-trade")
        assertTrue(store.put(legacy))
        assertTrue(store.put(normal))
        fun stored(id: String) = store.readableDatabase.rawQuery(
            "SELECT state,payload FROM candidates WHERE id=?", arrayOf(id),
        ).use {
            assertTrue(it.moveToFirst())
            it.getString(0) to it.getString(1)
        }
        val legacyBefore = stored(legacy.id)
        val normalBefore = stored(normal.id)
        assertEquals("READY", legacyBefore.first)
        store.close()
        store = CaptureInboxStore(context)

        val pending = store.pending()
        assertEquals(2, pending.size)
        assertEquals(2, store.count())
        val adapted = pending.single { it["id"] == legacy.id }
        assertEquals("r".repeat(512), adapted["remark"])
        assertEquals("p".repeat(512), adapted["paymentMethod"])
        assertEquals("NEEDS_REVIEW", adapted["state"])
        assertEquals(listOf("TEXT_TRUNCATED"), adapted["issues"])
        assertEquals(legacy.ledgerId, (adapted["ledgerId"] as Number).toLong())
        assertEquals(legacy.amountMinor, (adapted["amountMinor"] as Number).toLong())
        assertEquals(legacy.referenceHash, adapted["referenceHash"])
        assertEquals(legacy.transactionIdentity, adapted["transactionIdentity"])
        assertEquals(CaptureInboxStore.jsonMap(JSONObject(normalBefore.second)),
            pending.single { it["id"] == normal.id })
        assertEquals(listOf("TEXT_TRUNCATED"), store.pending().single { it["id"] == legacy.id }["issues"])
        assertEquals(legacyBefore, stored(legacy.id))
        assertEquals(normalBefore, stored(normal.id))
        assertEquals(600, JSONObject(stored(legacy.id).second).getString("remark").length)
    }

    @Test fun replayRacingWithCommitAcrossDatabaseConnectionsCannotReopenThePayment() {
        store.put(candidate().copy(transactionIdentity = "trade-one"))
        val other = CaptureInboxStore(captureTestContext())
        val workers = Executors.newFixedThreadPool(2)
        val start = CountDownLatch(1)
        try {
            assertEquals(1, other.count()) // Open a second, independent SQLite connection.
            val replay = workers.submit(Callable {
                check(start.await(10, TimeUnit.SECONDS))
                other.put(candidate().copy(id = "replayed-session", sessionId = "session-two",
                    transactionIdentity = "trade-one"))
            })
            val commit = workers.submit(Callable {
                check(start.await(10, TimeUnit.SECONDS))
                store.complete("fixture-one", 42)
            })
            start.countDown()
            assertFalse(replay.get(10, TimeUnit.SECONDS))
            commit.get(10, TimeUnit.SECONDS)
        } finally {
            start.countDown()
            workers.shutdownNow()
            check(workers.awaitTermination(10, TimeUnit.SECONDS))
            other.close()
        }
        // A delayed dismissal from the editor must not replace a committed ACK.
        store.complete("fixture-one", null)
        store.close()
        store = CaptureInboxStore(captureTestContext())
        assertEquals(0, store.count())
        assertFalse(store.put(candidate().copy(id = "after-recreation", transactionIdentity = "trade-one")))
        store.readableDatabase.rawQuery(
            "SELECT state,transaction_id FROM candidates WHERE id=?", arrayOf("fixture-one"),
        ).use {
            assertTrue(it.moveToFirst())
            assertEquals("COMMITTED", it.getString(0))
            assertEquals(42L, it.getLong(1))
        }
    }

    @Test fun aFullInboxRollsBackRejectedInsertAndStillAllowsEnrichmentAndAcknowledgement() {
        // Batch only fixture setup; the rejected insert and subsequent operations
        // each use the production store's own real SQLite transaction boundary.
        val db = store.writableDatabase
        db.beginTransaction()
        try {
            repeat(1000) { index ->
                assertTrue(store.put(candidate().copy(id = "full-$index", observedAt = candidate().observedAt + index)))
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        try {
            store.put(candidate().copy(id = "over-capacity"))
            fail("A full inbox must reject another payment")
        } catch (error: IllegalStateException) {
            assertEquals("INBOX_FULL", error.message)
        }
        assertEquals(1000, store.count())
        assertFalse(store.put(candidate().copy(id = "full-0", merchant = "Enriched shop")))
        assertEquals("Enriched shop", store.pending(1000).single { it["id"] == "full-0" }["merchant"])
        store.complete("full-0", 7)
        assertTrue(store.put(candidate().copy(id = "after-full")))
        store.close()
        store = CaptureInboxStore(captureTestContext())
        val pending = store.pending(1000)
        assertEquals(1000, pending.size)
        assertFalse(pending.any { it["id"] == "over-capacity" || it["id"] == "full-0" })
        assertTrue(pending.any { it["id"] == "after-full" })
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
