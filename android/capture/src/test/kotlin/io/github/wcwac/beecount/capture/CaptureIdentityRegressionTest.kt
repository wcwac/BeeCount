package io.github.wcwac.beecount.capture

import io.github.wcwac.beecount.domain.EntryKind
import org.junit.Assert.*
import org.junit.Test

/** Identity regressions ported from the existing 73 tests. */
class CaptureIdentityRegressionTest {
    private fun identity(pkg: String = "wechat", session: String = "session-one", window: Int = 7,
                         amount: Long = 3010, time: Long = 1, texts: List<String>) =
        CaptureIdentity.identify(pkg, session, window, EntryKind.EXPENSE, amount, time, "同一商户", "零钱", texts)

    @Test fun realPaymentIdentitySurvivesDisplayCorrectionsButNotPackageChanges() {
        // CaptureIdentityTest.aRealPaymentIdSurvivesWindowAmountAndTimestampCorrections /
        // identicalPaymentsInDifferentAppsAreIndependent.
        val first = identity(texts = listOf("交易单号", "PAY202610230001"))
        val detail = identity(window = 8, amount = 3011, time = 2, texts = listOf("交易单号：PAY202610230001"))
        assertEquals("PAYMENT_ID", first.kind)
        assertNotNull(first.strongKey)
        assertEquals(first.strongKey, detail.strongKey)
        assertNotEquals(first.windowKey, detail.windowKey)
        assertNotEquals(first.strongKey, identity(pkg = "meituan", texts = listOf("交易单号", "PAY202610230001")).strongKey)
        // CaptureIdentityTest.aReusedWindowIdAfterServiceRestartCannotMergeASeparateWeakPayment.
        val weak = identity(texts = emptyList())
        val restarted = identity(session = "session-two", texts = emptyList())
        assertNotEquals(weak.windowKey, restarted.windowKey)
        val proof = listOf("交易单号", "PAY202610230001")
        assertEquals(identity(texts = proof).strongKey, identity(session = "session-two", texts = proof).strongKey)

    }

    @Test fun merchantOrdersCannotMergeSeparatePartialPayments() {
        // CaptureIdentityTest.merchantOrdersAreAdvisoryAndPartialPaymentsRemainDistinct.
        val first = identity(texts = listOf("商户订单号", "ORDER202610230001"))
        val second = identity(amount = 5000, texts = listOf("商户订单号", "ORDER202610230001"))
        assertEquals("WINDOW_RECEIPT", first.kind)
        assertNull(first.paymentId)
        assertNull(first.strongKey)
        assertEquals("ORDER202610230001", first.orderReference)
        assertNotEquals(first.windowKey, second.windowKey)

        // CaptureIdentityTest.conflictingPaymentIdsAndMaskedValuesRequireManualConfirmation /
        // refundCannotUseItsOriginalPaymentIdAsAUniqueRefundId.
        assertNull(identity(texts = listOf("交易单号", "PAY202610230001", "支付单号", "PAY202610230002")).strongKey)
        assertNull(identity(texts = listOf("交易单号", "2026****0001")).strongKey)
        assertNull(identity(texts = listOf("交易单号", "2026-10-23")).strongKey)
        assertNull(identity(texts = listOf("退款成功", "交易单号", "PAY202610230001")).strongKey)
        assertEquals("REF202610230001", identity(texts = listOf("退款成功", "退款单号", "REF202610230001", "交易单号", "PAY202610230001")).paymentId)
    }

}
