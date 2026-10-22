package com.tntlikely.beecount.capture

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

/** Separate native inbox: no native writes to the Flutter/Drift ledger database. */
class CaptureInboxStore(context: Context) : SQLiteOpenHelper(context.applicationContext, "capture_inbox.sqlite", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE candidates (
            id TEXT PRIMARY KEY, ledger_id INTEGER NOT NULL, observed_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL, state TEXT NOT NULL, payload TEXT NOT NULL,
            transaction_id INTEGER, transaction_identity TEXT)""")
        db.execSQL("CREATE INDEX capture_pending ON candidates(state, observed_at)")
        createIdentityIndex(db)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE candidates ADD COLUMN transaction_identity TEXT")
            createIdentityIndex(db)
        }
    }

    private fun createIdentityIndex(db: SQLiteDatabase) {
        db.execSQL("CREATE UNIQUE INDEX capture_transaction_identity ON candidates(transaction_identity) WHERE transaction_identity IS NOT NULL")
    }

    private data class Existing(val id: String, val state: String, val ledgerId: Long,
        val ledgerKey: String, val amount: Long?, val identity: String?, val payload: JSONObject)

    private fun existing(db: SQLiteDatabase, column: String, value: String): Existing? {
        require(column == "id" || column == "transaction_identity")
        return db.rawQuery("SELECT id,state,ledger_id,payload,transaction_identity FROM candidates WHERE $column=?", arrayOf(value)).use {
            if (!it.moveToFirst()) null else {
                val payload = JSONObject(it.getString(3))
                Existing(it.getString(0), it.getString(1), it.getLong(2),
                    if (payload.isNull("ledgerKey")) "" else payload.optString("ledgerKey"),
                    if (payload.isNull("amountMinor")) null else payload.getLong("amountMinor"),
                    if (it.isNull(4)) null else it.getString(4), payload)
            }
        }
    }

    /** Recreating a session can rediscover only part of an already durable payment.
     * Match the engine's sparse-observation semantics without retaining a rejected amount.
     */
    private fun mergeAlias(candidate: CaptureCandidate, previous: Existing): CaptureCandidate {
        val payload = previous.payload
        fun string(key: String) = if (payload.isNull(key)) null else payload.getString(key)
        fun number(key: String) = if (payload.isNull(key)) null else payload.getLong(key)
        val oldIssues = payload.optJSONArray("issues")?.let { values ->
            (0 until values.length()).map { values.getString(it) }.toSet()
        }.orEmpty()
        val oldEvidence = payload.optJSONObject("evidence")?.let { value ->
            value.keys().asSequence().associateWith { value.getString(it) }
        }.orEmpty()
        val inheritedIssues = oldIssues.filter { issue ->
            when (issue) {
                "AMOUNT_MISSING" -> false // Recalculate after filling an absent value.
                "AMOUNT_UNANCHORED", "AMOUNT_CONFLICT" -> candidate.amountMinor == null
                "MERCHANT_UNCERTAIN" -> candidate.merchant == null
                "PAYMENT_UNCONFIRMED" -> "PAYMENT_UNCONFIRMED" in candidate.issues
                else -> true
            }
        }
        val amountRejected = "AMOUNT_CONFLICT" in candidate.issues ||
            candidate.evidence["amount"] == "refund:original-amount-rejected"
        val amount = candidate.amountMinor ?: previous.amount.takeUnless { amountRejected }
        val oldDirection = string("direction")
        val differentDirections = oldDirection != null && candidate.direction != null && oldDirection != candidate.direction
        val explicitDirections = setOf("incoming-label", "signed-result", "signed-detail", "refund")
        val oldDirectionIsExplicit = oldEvidence["direction"] in explicitDirections
        val newDirectionIsExplicit = candidate.evidence["direction"] in explicitDirections
        val directionConflict = differentDirections && oldDirectionIsExplicit && newDirectionIsExplicit
        val keepOldDirection = (candidate.direction == null && "PAYMENT_UNCONFIRMED" !in candidate.issues) ||
            (differentDirections && oldDirectionIsExplicit && !newDirectionIsExplicit)
        val issues = (candidate.issues - "AMOUNT_MISSING") + inheritedIssues +
            (if (amount == null) setOf("AMOUNT_MISSING") else emptySet()) +
            (if (directionConflict) setOf("DIRECTION_UNCERTAIN") else emptySet())
        val evidence = oldEvidence + candidate.evidence +
            if (keepOldDirection && oldEvidence["direction"] != null)
                mapOf("direction" to oldEvidence.getValue("direction")) else emptyMap()
        return candidate.copy(
            amountMinor = amount,
            merchant = candidate.merchant ?: string("merchant"),
            remark = candidate.remark ?: string("remark"),
            paymentMethod = candidate.paymentMethod ?: string("paymentMethod"),
            discountMinor = candidate.discountMinor ?: number("discountMinor"),
            transactionTime = candidate.transactionTime ?: number("transactionTime"),
            referenceHash = candidate.referenceHash ?: string("referenceHash"),
            direction = if ("DIRECTION_UNCERTAIN" in issues) null else if (keepOldDirection) oldDirection else candidate.direction,
            evidence = evidence,
            issues = issues,
        )
    }

    /** Enrich pending records; never reopen a committed or dismissed candidate. */
    fun put(candidate: CaptureCandidate): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val byId = existing(db, "id", candidate.id)
            val byIdentity = candidate.transactionIdentity?.let { existing(db, "transaction_identity", it) }
            val compatible = byIdentity != null && byIdentity.ledgerId == candidate.ledgerId &&
                byIdentity.ledgerKey == (candidate.ledgerKey ?: "") &&
                (byIdentity.amount == null || candidate.amountMinor == null || byIdentity.amount == candidate.amountMinor)
            // A not-yet-published session can reuse a durable identity after
            // service/process recreation. Never replace an already published
            // candidate ID with another one underneath an open Flutter editor.
            val canonicalId = if (byId == null && compatible) byIdentity!!.id else candidate.id
            val old = byId ?: byIdentity?.takeIf { it.id == canonicalId }
            val identityConflict = byIdentity != null && (!compatible || byIdentity.id != canonicalId)
            val enriched = if (byId == null && compatible) mergeAlias(candidate, byIdentity!!) else candidate
            val effective = enriched.copy(
                id = canonicalId,
                transactionIdentity = if (identityConflict) old?.identity else candidate.transactionIdentity ?: old?.identity,
                issues = enriched.issues + if (identityConflict) setOf("REFERENCE_CONFLICT") else emptySet(),
            )
            if (old != null && (old.state in setOf("COMMITTED", "DISMISSED") || old.ledgerId != effective.ledgerId ||
                old.ledgerKey != (effective.ledgerKey ?: ""))) {
                db.setTransactionSuccessful(); return false
            }
            if (old == null) {
                val count = db.rawQuery("SELECT COUNT(*) FROM candidates WHERE state IN ('READY','NEEDS_REVIEW')", null).use { it.moveToFirst(); it.getInt(0) }
                check(count < 1000) { "INBOX_FULL" }
            }
            val payload = effective.asMap()
            val values = ContentValues().apply {
                put("id", effective.id); put("ledger_id", effective.ledgerId)
                put("observed_at", effective.observedAt); put("updated_at", System.currentTimeMillis())
                put("state", payload["state"] as String); put("payload", JSONObject(payload).toString())
                if (effective.transactionIdentity == null) putNull("transaction_identity")
                else put("transaction_identity", effective.transactionIdentity)
            }
            if (old == null) db.insertOrThrow("candidates", null, values)
            else db.update("candidates", values, "id=?", arrayOf(effective.id))
            db.setTransactionSuccessful()
            return old == null
        } finally { db.endTransaction() }
    }

    /** Older parser versions could persist notification fields beyond the bridge
     * contract. Adapt only their display payload; retain the original SQLite row.
     */
    private fun pendingPayload(json: JSONObject): Map<String, Any?> {
        val payload = jsonMap(json).toMutableMap()
        var truncated = false
        for (key in listOf("remark", "paymentMethod")) {
            val value = payload[key] as? String ?: continue
            if (value.length > 512) {
                payload[key] = value.take(512)
                truncated = true
            }
        }
        if (truncated) {
            payload["issues"] = ((payload["issues"] as? List<*>).orEmpty() + "TEXT_TRUNCATED").distinct()
            payload["state"] = "NEEDS_REVIEW"
        }
        return payload
    }

    fun pending(limit: Int = 100): List<Map<String, Any?>> = readableDatabase.rawQuery(
        "SELECT payload FROM candidates WHERE state IN ('READY','NEEDS_REVIEW') ORDER BY observed_at DESC, id LIMIT ?",
        arrayOf(limit.coerceIn(1, 1000).toString()),
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(pendingPayload(JSONObject(cursor.getString(0)))) } }

    fun count(): Int = readableDatabase.rawQuery(
        "SELECT COUNT(*) FROM candidates WHERE state IN ('READY','NEEDS_REVIEW')", null,
    ).use { it.moveToFirst(); it.getInt(0) }

    fun complete(id: String, transactionId: Long?) {
        val values = ContentValues().apply {
            put("state", if (transactionId == null) "DISMISSED" else "COMMITTED")
            put("updated_at", System.currentTimeMillis())
            if (transactionId != null) put("transaction_id", transactionId)
        }
        // Terminal states cannot be changed by a later dismiss callback.
        writableDatabase.update("candidates", values, "id=? AND state IN ('READY','NEEDS_REVIEW')", arrayOf(id))
    }

    fun prune() {
        writableDatabase.delete("candidates", "state IN ('COMMITTED','DISMISSED') AND updated_at < ?",
            arrayOf((System.currentTimeMillis() - 30L * 86_400_000).toString()))
    }

    companion object {
        private fun jsonValue(value: Any?): Any? = when (value) {
            JSONObject.NULL, null -> null
            is JSONObject -> jsonMap(value)
            is JSONArray -> (0 until value.length()).map { jsonValue(value.get(it)) }
            else -> value
        }
        fun jsonMap(json: JSONObject): Map<String, Any?> = json.keys().asSequence().associateWith { jsonValue(json.get(it)) }
    }
}
