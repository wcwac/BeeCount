package com.tntlikely.beecount.capture

import java.util.UUID

/** Access only on the capture worker. Each package owns an expiring transaction session. */
class CaptureEngine(private val adapters: CaptureAdapterRegistry = CaptureAdapterRegistry()) {
    private data class Session(
        val id: String, val ledgerId: Long, val ledgerKey: String?, var lastAt: Long, var fields: CaptureFields,
        var finalized: Boolean = false,
    )
    private val sessions = mutableMapOf<String, Session>()
    private data class Epoch(val revision: Long, val consent: Long)
    private var epoch: Epoch? = null

    fun clear() = sessions.clear()

    fun accept(snapshot: CaptureSnapshot, ledgerId: Long, ledgerKey: String? = null,
        sessionRevision: Long = 0, consentGeneration: Long = 0): CaptureCandidate? {
        val app = CaptureApps.byPackage[snapshot.packageName] ?: return null
        val observedEpoch = Epoch(sessionRevision, consentGeneration)
        if (epoch != observedEpoch) { clear(); epoch = observedEpoch }
        val fields = adapters.parse(app, snapshot)
        sessions.entries.removeAll { snapshot.elapsedAt - it.value.lastAt !in 0..SESSION_TTL }
        if (fields.page == CapturePage.NONE) {
            if (fields.resetSession) sessions.remove(snapshot.packageName)
            return null
        }
        if (fields.page == CapturePage.FAILURE) {
            if (snapshot.source == "accessibility") sessions.remove(snapshot.packageName)
            return null
        }
        if (snapshot.source == "notification") {
            // A notification must carry its own amount and evidence. Never
            // borrow an unrelated checkout session merely because it is recent.
            if (fields.page == CapturePage.CHECKOUT) return null
            val identity = snapshot.sourceEventId ?: return null
            return CaptureCandidate(
                id = captureHash("notification:$identity"), sourcePackage = app.packageName,
                appName = app.name, adapterId = app.adapter, sessionId = identity,
                ledgerId = ledgerId, ledgerKey = ledgerKey, observedAt = snapshot.observedAt,
                amountMinor = fields.amountMinor, merchant = fields.merchant, remark = fields.remark,
                paymentMethod = fields.paymentMethod, discountMinor = fields.discountMinor,
                transactionTime = fields.transactionTime, referenceHash = fields.referenceHash,
                direction = fields.direction, evidence = fields.evidence,
                issues = fields.issues + if (fields.amountMinor == null) setOf("AMOUNT_MISSING") else emptySet(),
                source = "notification",
                transactionIdentity = transactionIdentity(app, fields),
            )
        }
        var session = sessions[snapshot.packageName]?.takeIf { it.ledgerId == ledgerId && it.ledgerKey == ledgerKey }
        val prior = session
        // A new order may appear before either checkout has produced a result.
        // Do not lend that order the previous order's partially observed fields.
        // Order IDs and PSP transaction IDs remain separate kinds of reference.
        val differentReference = prior != null && fields.referenceHash != null && prior.fields.referenceHash != null &&
            fields.referenceIsTransaction == prior.fields.referenceIsTransaction && fields.referenceHash != prior.fields.referenceHash
        val distinctResult = prior != null && prior.finalized && fields.page != CapturePage.CHECKOUT && (
            fields.page != prior.fields.page ||
            (fields.amountMinor != null && prior.fields.amountMinor != null && fields.amountMinor != prior.fields.amountMinor) ||
            (fields.merchant != null && prior.fields.merchant != null && fields.merchant != prior.fields.merchant) ||
            (fields.transactionTime != null && prior.fields.transactionTime != null && fields.transactionTime != prior.fields.transactionTime)
        )
        val newRefund = fields.page == CapturePage.REFUND && prior?.fields?.page != CapturePage.REFUND
        if (session == null || differentReference || distinctResult || newRefund || (fields.page == CapturePage.CHECKOUT && session.finalized)) {
            session = Session(UUID.randomUUID().toString(), ledgerId, ledgerKey, snapshot.elapsedAt, fields)
            sessions[snapshot.packageName] = session
        }
        val previous = session.fields
        val checkout = fields.page == CapturePage.CHECKOUT
        val conflict = fields.amountMinor != null && previous.amountMinor != null &&
            fields.amountMinor != previous.amountMinor && !checkout
        // A checkout order number and a later PSP transaction number are
        // different identifiers of the same payment, not conflicting payments.
        // Once present, a transaction reference must survive a weaker snapshot.
        val keepPreviousReference = previous.referenceIsTransaction &&
            (!fields.referenceIsTransaction || fields.referenceHash == null)
        val inheritedIssues = previous.issues.filter { issue ->
            when (issue) {
                "AMOUNT_UNANCHORED", "AMOUNT_CONFLICT" -> fields.amountMinor == null
                "MERCHANT_UNCERTAIN" -> fields.merchant == null
                // An explicit payment result can resolve an earlier order-only
                // observation. Other unresolved warnings must not disappear
                // merely because the next event contains less page content.
                "PAYMENT_UNCONFIRMED" -> "PAYMENT_UNCONFIRMED" in fields.issues
                else -> true
            }
        }
        val differentDirections = previous.direction != null && fields.direction != null && previous.direction != fields.direction
        val previousDirectionIsExplicit = previous.evidence["direction"] in EXPLICIT_DIRECTIONS
        val currentDirectionIsExplicit = fields.evidence["direction"] in EXPLICIT_DIRECTIONS
        val directionConflict = differentDirections && previousDirectionIsExplicit && currentDirectionIsExplicit
        val keepPreviousDirection = (fields.direction == null && "PAYMENT_UNCONFIRMED" !in fields.issues) ||
            (differentDirections && previousDirectionIsExplicit && !currentDirectionIsExplicit)
        val mergedIssues = fields.issues + inheritedIssues +
            (if (conflict) setOf("AMOUNT_CHANGED") else emptySet()) +
            (if (directionConflict) setOf("DIRECTION_UNCERTAIN") else emptySet())
        val mergedEvidence = previous.evidence + fields.evidence +
            if (keepPreviousDirection && previous.evidence["direction"] != null)
                mapOf("direction" to previous.evidence.getValue("direction")) else emptyMap()
        val amountRejected = "AMOUNT_CONFLICT" in fields.issues ||
            fields.evidence["amount"] == "refund:original-amount-rejected"
        val merged = fields.copy(
            amountMinor = fields.amountMinor ?: previous.amountMinor.takeUnless { amountRejected },
            merchant = fields.merchant ?: previous.merchant,
            remark = fields.remark ?: previous.remark,
            paymentMethod = fields.paymentMethod ?: previous.paymentMethod,
            discountMinor = fields.discountMinor ?: previous.discountMinor,
            transactionTime = fields.transactionTime ?: previous.transactionTime,
            direction = if ("DIRECTION_UNCERTAIN" in mergedIssues) null
                else if (keepPreviousDirection) previous.direction else fields.direction,
            referenceHash = if (keepPreviousReference) previous.referenceHash else fields.referenceHash ?: previous.referenceHash,
            referenceIsTransaction = if (keepPreviousReference || fields.referenceHash == null)
                previous.referenceIsTransaction else fields.referenceIsTransaction,
            evidence = mergedEvidence,
            issues = mergedIssues,
        )
        session.lastAt = snapshot.elapsedAt
        session.fields = merged
        if (checkout) return null
        session.finalized = true
        return CaptureCandidate(
            // The ID remains stable when a later snapshot enriches the session
            // with a transaction reference. The inbox resolves durable aliases.
            id = session.id, sourcePackage = app.packageName, appName = app.name, adapterId = app.adapter,
            sessionId = session.id, ledgerId = ledgerId, observedAt = snapshot.observedAt,
            amountMinor = merged.amountMinor, merchant = merged.merchant, remark = merged.remark,
            paymentMethod = merged.paymentMethod, discountMinor = merged.discountMinor,
            transactionTime = merged.transactionTime, referenceHash = merged.referenceHash,
            direction = merged.direction, evidence = merged.evidence,
            issues = merged.issues + if (merged.amountMinor == null) setOf("AMOUNT_MISSING") else emptySet(),
            ledgerKey = ledgerKey,
            transactionIdentity = transactionIdentity(app, merged),
        )
    }

    private fun transactionIdentity(app: CaptureApp, fields: CaptureFields): String? {
        // Order IDs may cover instalments or several partial refunds. Never
        // use them, or an uncertain direction, as a unique payment identity.
        if (!fields.referenceIsTransaction || fields.referenceHash == null ||
            fields.page == CapturePage.REFUND || fields.direction == null) return null
        return captureHash("${app.adapter}|${fields.referenceHash}|${fields.direction}")
    }

    companion object {
        private const val SESSION_TTL = 5 * 60_000L
        private val EXPLICIT_DIRECTIONS = setOf("incoming-label", "signed-result", "signed-detail", "refund")
    }
}
