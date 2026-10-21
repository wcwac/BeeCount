package com.tntlikely.beecount.capture

import org.junit.Assert.*
import org.junit.Test

/** Constructed semantic fixtures, not recordings or a compatibility claim. */
class CaptureRefundTest {
    private fun snapshot(packageName: String, vararg texts: String, elapsed: Long = 1000) = CaptureSnapshot(
        packageName, "fixture.Refund", 1, 1_792_598_400_000L + elapsed, elapsed, texts.map { CaptureNode(it) },
    )
    private val wechat = "com.tencent.mm"
    private val meituan = "com.sankuai.meituan"

    @Test fun everyClientUsesTheCurrentRefundInsteadOfTheOriginalPayment() {
        for (app in CaptureApps.all) {
            val result = CaptureEngine().accept(snapshot(app.packageName, "退款成功",
                "实付款", "¥100.00", "本次退款金额", "¥10.00", "支付优惠", "¥5.00"), 1)!!
            assertEquals(app.packageName, 1000L, result.amountMinor)
            assertEquals(app.packageName, "income", result.direction)
            assertNull(app.packageName, result.discountMinor)
        }
    }

    @Test fun anOriginalPriceAloneCannotStandInForAMissingRefundAmount() {
        for (app in CaptureApps.all) {
            val result = CaptureEngine().accept(snapshot(app.packageName,
                "交易详情", "实付款", "¥100.00", "退款成功", "退款时间", "2020-06-15 12:34:56"), 1)!!
            assertNull(app.packageName, result.amountMinor)
            assertTrue(app.packageName, "AMOUNT_MISSING" in result.issues)
        }
    }

    @Test fun refundDetailsSelectTheirOwnStatusAndPaymentDestination() {
        val result = CaptureEngine().accept(snapshot(meituan,
            "原订单商户", "-100.00", "支付成功", "支付方式", "原付款卡",
            "早餐店", "+10.00", "退款成功", "退款至", "退款储蓄卡",
            "退款受理时间", "2020-06-15 12:34:56"), 1)!!
        assertEquals(1000L, result.amountMinor)
        assertEquals("早餐店", result.merchant)
        assertEquals("退款储蓄卡", result.paymentMethod)
        assertEquals("income", result.direction)
    }

    @Test fun aRefundCannotUseTheRequestedAmountAsProofOfTheReturnedAmount() {
        val result = CaptureEngine().accept(snapshot(meituan,
            "申请退款金额", "¥20.00", "退款成功"), 1)!!
        assertNull(result.amountMinor)
    }

    @Test fun aStatusAdjacentValueCannotOverrideConflictingRefundLabels() {
        val result = CaptureEngine().accept(snapshot(meituan,
            "退款金额", "¥10.00", "退款金额", "¥20.00", "早餐店", "+10.00", "退款成功"), 1)!!
        assertNull(result.amountMinor)
        assertTrue("AMOUNT_CONFLICT" in result.issues)
    }

    @Test fun differentCurrentRefundLabelsMustAgree() {
        val result = CaptureEngine().accept(snapshot(wechat, "退款成功",
            "本次退款金额", "¥10.00", "实际退款金额", "¥20.00"), 1)!!
        assertNull(result.amountMinor)
        assertTrue("AMOUNT_CONFLICT" in result.issues)
    }

    @Test fun rejectingAnOriginalPriceCannotReviveAnEarlierRefundAmount() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "退款成功", "退款金额", "¥10.00"), 1)
        val result = engine.accept(snapshot(wechat, "退款成功", "实付款", "¥100.00", elapsed = 2000), 1)!!
        assertNull(result.amountMinor)
        assertTrue("AMOUNT_MISSING" in result.issues)
    }

    @Test fun explicitAmountConflictCannotBeHiddenByACachedCheckout() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "¥10.00"), 1)
        val result = engine.accept(snapshot(wechat, "支付成功", "支付金额", "¥10.00",
            "支付金额", "¥20.00", elapsed = 2000), 1)!!
        assertNull(result.amountMinor)
        assertTrue("AMOUNT_CONFLICT" in result.issues)
    }

    @Test fun equalPartialRefundsWithDifferentRefundIdsRemainSeparate() {
        val engine = CaptureEngine()
        val first = engine.accept(snapshot(wechat, "退款成功", "退款金额", "¥10.00",
            "交易单号", "PAYMENT202610210001", "退款单号", "REFUND202610210001"), 1)!!
        val second = engine.accept(snapshot(wechat, "退款成功", "退款金额", "¥10.00",
            "交易单号", "PAYMENT202610210001", "退款单号", "REFUND202610210002", elapsed = 2000), 1)!!
        assertNotEquals(first.referenceHash, second.referenceHash)
        assertNotEquals(first.id, second.id)
    }

    @Test fun transferPrincipalIsSeparateFromTheVisibleFeeAndStillNeedsDirectionReview() {
        val result = CaptureEngine().accept(snapshot(wechat, "转账成功",
            "转账金额", "¥100.00", "手续费", "¥1.00"), 1)!!
        assertEquals(10000L, result.amountMinor)
        assertNull(result.direction)
        assertTrue("DIRECTION_UNCERTAIN" in result.issues)
        assertTrue("FEE_PRESENT" in result.issues)
    }

    @Test fun aReceiptUsesActualReceivedAmountBeforeTheGenericTransactionAmount() {
        val result = CaptureEngine().accept(snapshot(wechat, "收款成功",
            "交易金额", "¥100.00", "实收金额", "¥99.00"), 1)!!
        assertEquals(9900L, result.amountMinor)
        assertEquals("income", result.direction)
    }

    @Test fun meituanDeferredPaymentDeductionHasItsOwnSuccessEvidence() {
        val result = CaptureEngine().accept(snapshot(meituan, "早餐店", "-12.50",
            "先用后付自动扣款成功", "创建时间", "2020-06-15 12:34:56"), 1)!!
        assertEquals(1250L, result.amountMinor)
        assertEquals("expense", result.direction)
    }
}
