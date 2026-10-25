package io.github.wcwac.beecount.capture

import io.github.wcwac.beecount.domain.EntryKind
import java.security.MessageDigest

internal object CaptureIdentity {
    data class Identity(val strongKey: String?, val windowKey: String, val kind: String, val paymentId: String?, val orderReference: String?, val visibleTime: Boolean)
    private val paymentLabels = listOf("微信支付交易单号", "支付宝交易号", "交易单号", "支付单号", "付款单号", "收款单号", "退款单号", "转账单号", "交易号")
    private val refundLabels = listOf("退款交易单号", "退款交易号", "退款单号")
    private val orderLabels = listOf("商户订单号", "商家订单号", "订单编号", "订单号")
    private val timeLabels = listOf("交易时间", "支付时间", "付款时间", "创建时间", "退款时间", "转账时间", "收款时间", "交易日期")
    private val clock = Regex("[0-9]{1,2}[:时点][0-9]{1,2}")
    fun identify(pkg: String, session: String, windowId: Int, kind: EntryKind, amount: Long, parsedTime: Long, merchant: String, method: String, texts: List<String>): Identity {
        val order = field(texts, orderLabels)
        val refund = texts.any { text -> listOf("退款成功", "退款完成", "退款单号", "退款交易").any(text::contains) }
        val payment = field(texts, if (refund) refundLabels else paymentLabels)
        val visible = parsedTime > 0 && texts.indices.any { index -> timeLabels.any { label ->
            val token = texts[index].trim()
            token.startsWith(label) && (clock.containsMatchIn(token.removePrefix(label)) || index + 1 < texts.size && clock.containsMatchIn(texts[index + 1]))
        } }
        val business = kind.name.lowercase() + if (refund) "/refund" else "/payment"
        // Unknown window IDs receive a fresh observation key and cannot coalesce unrelated payments.
        val window = if (windowId < 0) "unresolved:" + java.util.UUID.randomUUID() else "window:v1:" + hash(pkg, session, windowId.toString(), business, amount.toString(), if (visible) parsedTime.toString() else "0", normalize(merchant), normalize(method), order.orEmpty())
        return Identity(payment?.let { "payment:v1:" + hash(pkg, business, it) }, window, if (payment != null) "PAYMENT_ID" else if (windowId >= 0) "WINDOW_RECEIPT" else "UNRESOLVED", payment, order, visible)
    }
    private fun field(texts: List<String>, labels: List<String>): String? {
        var found: String? = null
        texts.forEachIndexed { index, text -> labels.forEach labelLoop@ { label ->
            val token = text.trim(); if (!token.startsWith(label)) return@labelLoop
            val tail = token.substring(label.length())
            if (tail.isNotEmpty() && tail.first() != ':' && tail.first() != '：' && !tail.first().isWhitespace()) return@labelLoop
            val suffix = tail.trim().removePrefix(":").removePrefix("：").trim()
            val candidate = valid(suffix) ?: if (suffix.isEmpty()) texts.getOrNull(index + 1)?.let(::valid) else null
            if (candidate != null) { if (found != null && found != candidate) return null; found = candidate }
        } }
        return found
    }
    private fun valid(text: String): String? = text.trim().takeIf { it.length in 8..128 && it.matches(Regex("[A-Za-z0-9][A-Za-z0-9_-]*")) && !it.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")) }
    private fun normalize(value: String) = value.trim().replace(Regex("\\s+"), " ")
    private fun hash(vararg fields: String): String {
        val hash = MessageDigest.getInstance("SHA-256")
        fields.forEach { field -> val bytes = field.toByteArray(Charsets.UTF_8); hash.update(byteArrayOf((bytes.size ushr 24).toByte(), (bytes.size ushr 16).toByte(), (bytes.size ushr 8).toByte(), bytes.size.toByte())); hash.update(bytes) }
        return hash.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
    }
}
