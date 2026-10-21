package com.tntlikely.beecount.capture

import java.math.BigDecimal
import java.security.MessageDigest

/** Immutable, bounded input. Live AccessibilityNodeInfo objects never enter the parser queue. */
data class CaptureNode(
    val text: String,
    val viewId: String? = null,
    val parent: Int = -1,
    val depth: Int = 0,
)

data class CaptureSnapshot(
    val packageName: String,
    val pageClass: String,
    val windowId: Int,
    val observedAt: Long,
    val elapsedAt: Long,
    val nodes: List<CaptureNode>,
    val source: String = "accessibility",
    val truncated: Boolean = false,
    val sourceEventId: String? = null,
) {
    val texts: List<String> get() = nodes.map { it.text.trim() }.filter { it.isNotEmpty() }
}

enum class CapturePage { NONE, CHECKOUT, SUCCESS, REFUND, FAILURE }

data class CaptureFields(
    val page: CapturePage,
    val amountMinor: Long? = null,
    val merchant: String? = null,
    val remark: String? = null,
    val paymentMethod: String? = null,
    val discountMinor: Long? = null,
    val transactionTime: Long? = null,
    val referenceHash: String? = null,
    val direction: String? = null,
    val evidence: Map<String, String> = emptyMap(),
    val issues: Set<String> = emptySet(),
    val resetSession: Boolean = false,
    val referenceIsTransaction: Boolean = false,
)

data class CaptureCandidate(
    val id: String,
    val sourcePackage: String,
    val appName: String,
    val adapterId: String,
    val sessionId: String,
    val ledgerId: Long,
    val observedAt: Long,
    val amountMinor: Long?,
    val merchant: String?,
    val remark: String?,
    val paymentMethod: String?,
    val discountMinor: Long?,
    val transactionTime: Long?,
    val referenceHash: String?,
    val direction: String?,
    val evidence: Map<String, String>,
    val issues: Set<String>,
    val currency: String = "CNY",
    val parserVersion: String = "capture-v1",
    val ledgerKey: String? = null,
    val source: String = "accessibility",
    val transactionIdentity: String? = null,
) {
    fun asMap(): Map<String, Any?> = mapOf(
        "id" to id, "sourcePackage" to sourcePackage, "appName" to appName,
        "adapterId" to adapterId, "sessionId" to sessionId, "ledgerId" to ledgerId, "ledgerKey" to ledgerKey,
        "observedAt" to observedAt, "amountMinor" to amountMinor,
        "merchant" to merchant, "remark" to remark, "paymentMethod" to paymentMethod,
        "discountMinor" to discountMinor, "transactionTime" to transactionTime,
        "referenceHash" to referenceHash, "transactionIdentity" to transactionIdentity,
        "direction" to direction, "currency" to currency,
        "parserVersion" to parserVersion, "evidence" to evidence, "issues" to issues.toList(), "source" to source,
        "state" to if (amountMinor != null && direction != null && issues.isEmpty()) "READY" else "NEEDS_REVIEW",
    )
}

internal fun captureHash(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

/** Money stays integral until the existing Flutter repository boundary. */
private val CAPTURE_MONEY_PREFIX = Regex("^(?:[+−-]\\s*[¥￥]?|[¥￥]\\s*[+−-]?|)\\s*")
private val CAPTURE_MONEY_NUMBER = Regex("(?:[0-9]{1,9}|[0-9]{1,3}(?:,[0-9]{3})+)(?:\\.[0-9]{1,2})?")
private val CAPTURE_YUAN_SUFFIX = Regex("\\s*元$")

internal fun captureMoney(raw: String): Long? {
    val value = raw.trim().replace(CAPTURE_MONEY_PREFIX, "").replace(CAPTURE_YUAN_SUFFIX, "").trim()
    if (!CAPTURE_MONEY_NUMBER.matches(value)) return null
    return runCatching { BigDecimal(value.replace(",", "")).movePointRight(2).longValueExact() }
        .getOrNull()?.takeIf { it in 1..99_999_999_999L }
}
