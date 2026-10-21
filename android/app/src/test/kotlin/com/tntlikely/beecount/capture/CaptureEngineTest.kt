package com.tntlikely.beecount.capture

import org.junit.Assert.*
import org.junit.Test

/** Synthetic, redacted page inputs. These are not a real-device compatibility claim. */
class CaptureEngineTest {
    private fun snapshot(packageName: String, vararg texts: String, elapsed: Long = 1000) = CaptureSnapshot(
        packageName, "test.Page", 1, 1_792_598_400_000 + elapsed, elapsed,
        texts.map { CaptureNode(it) },
    )
    private val wechat = "com.tencent.mm"

    @Test fun transactionTimeRequiresTheWholeLabelValueToBeValid() {
        fun read(time: String) = CaptureReader(snapshot(wechat, "支付时间", time)).transactionTime()
        assertNotNull(read("2020-06-15 12:34:56"))
        assertNotNull(read("2020-06-15 12:34"))
        assertNull(read("2020-06-15 12:34:56 invalid"))
        assertNull(read("2020-06-15 12:34:99"))
        assertNull(read("2020-02-30 12:34"))
    }

    @Test fun moneyRejectsNoiseAndPreservesExactCents() {
        assertEquals(123456L, captureMoney("￥1,234.56"))
        assertEquals(1001L, captureMoney("-¥10.01"))
        assertNull(captureMoney("12,34.00"))
        assertNull(captureMoney("12.345"))
        assertNull(captureMoney("订单12345"))
        assertNull(captureMoney("0"))
    }

    @Test fun productPricesAreNotPaymentEvidence() {
        assertNull(CaptureEngine().accept(snapshot("com.taobao.taobao", "￥99.00", "原价￥199.00", "立即购买"), 1))
    }

    @Test fun checkoutAmountSurvivesAResultPageWithoutAmount() {
        val engine = CaptureEngine()
        assertNull(engine.accept(snapshot("com.taobao.taobao", "提交订单", "实付款", "￥15.80"), 1))
        val result = engine.accept(snapshot("com.taobao.taobao", "支付成功", elapsed = 2000), 1)!!
        assertEquals(1580L, result.amountMinor)
        assertEquals("expense", result.direction)
        assertEquals("label:实付款", result.evidence["amount"])
    }

    @Test fun aChangedCheckoutOrderCannotBorrowThePreviousOrdersFields() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "¥100.00", "收款方", "Original shop",
            "付款方式", "Original card", "订单号", "ORDER202610220001"), 1)
        assertNull(engine.accept(snapshot(wechat, "确认付款", "订单号", "ORDER202610220002", elapsed = 1500), 1))
        val result = engine.accept(snapshot(wechat, "支付成功", elapsed = 2000), 1)!!
        assertEquals(captureHash("wechat:ORDER202610220002"), result.referenceHash)
        assertNull(result.amountMinor)
        assertNull(result.merchant)
        assertNull(result.paymentMethod)
        assertTrue("AMOUNT_MISSING" in result.issues)
    }

    @Test fun aDifferentResultTransactionCannotBorrowAnUnfinishedCheckout() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "¥100.00", "收款方", "Original shop",
            "交易单号", "TRADE202610220001"), 1)
        val result = engine.accept(snapshot(wechat, "支付成功", "交易单号", "TRADE202610220002", elapsed = 2000), 1)!!
        assertEquals(captureHash("wechat:TRADE202610220002"), result.referenceHash)
        assertNull(result.amountMinor)
        assertNull(result.merchant)
        assertTrue("AMOUNT_MISSING" in result.issues)
    }

    @Test fun cancelledCheckoutCannotSupplyTheNextPayment() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "￥20.00"), 1)
        engine.accept(snapshot(wechat, "支付失败", elapsed = 1500), 1)
        val result = engine.accept(snapshot(wechat, "支付成功", elapsed = 2000), 1)!!
        assertNull(result.amountMinor)
        assertTrue("AMOUNT_MISSING" in result.issues)
    }

    @Test fun anEmptyKnownHomePageStillClearsThePreviousCheckout() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "¥20.00"), 1)
        val home = snapshot(wechat, elapsed = 1500).copy(pageClass = "com.tencent.mm.ui.LauncherUI")
        assertNull(engine.accept(home, 1))
        val next = engine.accept(snapshot(wechat, "支付成功", elapsed = 2000), 1)!!
        assertNull(next.amountMinor)
        assertTrue("AMOUNT_MISSING" in next.issues)
    }

    @Test fun aResetBoundaryPreservesOldQueuedResultsAndIsolatesNewObservations() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "¥20.00"), 1, sessionRevision = 0)
        // Both were observed before reset; draining them afterwards keeps their
        // checkout/result relationship. A later batch cannot borrow that amount.
        val oldResult = engine.accept(snapshot(wechat, "支付成功", elapsed = 1500), 1, sessionRevision = 0)!!
        assertEquals(2000L, oldResult.amountMinor)
        val newResult = engine.accept(snapshot(wechat, "支付成功", elapsed = 2000), 1, sessionRevision = 1)!!
        assertNull(newResult.amountMinor)
        assertNotEquals(oldResult.id, newResult.id)
    }

    @Test fun consentChangesAlsoInvalidateCachedFieldsWithoutAServiceCallback() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "¥20.00"), 1,
            sessionRevision = 3, consentGeneration = 4)
        val result = engine.accept(snapshot(wechat, "支付成功", elapsed = 2000), 1,
            sessionRevision = 3, consentGeneration = 5)!!
        assertNull(result.amountMinor)
    }

    @Test fun independentEqualAmountPurchasesRemainSeparate() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "￥10.00"), 1)
        val first = engine.accept(snapshot(wechat, "支付成功", "￥10.00", elapsed = 2000), 1)!!
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "￥10.00", elapsed = 3000), 1)
        val second = engine.accept(snapshot(wechat, "支付成功", "￥10.00", elapsed = 4000), 1)!!
        assertNotEquals(first.id, second.id)
    }

    @Test fun repeatedResultEventsUseOneCandidateIdentity() {
        val engine = CaptureEngine()
        val first = engine.accept(snapshot(wechat, "支付成功", "￥10.00"), 1)!!
        val second = engine.accept(snapshot(wechat, "支付成功", "￥10.00", elapsed = 1500), 1)!!
        assertEquals(first.id, second.id)
    }

    @Test fun ledgerChangesCannotReuseOldCheckoutData() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "￥20.00"), 1)
        val result = engine.accept(snapshot(wechat, "支付成功", elapsed = 2000), 2)!!
        assertNull(result.amountMinor)
        assertEquals(2L, result.ledgerId)
    }

    @Test fun staleCheckoutDataExpires() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "￥20.00"), 1)
        val result = engine.accept(snapshot(wechat, "支付成功", elapsed = 400_000), 1)!!
        assertNull(result.amountMinor)
    }

    @Test fun actualPaymentLabelWinsOverCouponAmount() {
        val result = CaptureEngine().accept(snapshot("com.sankuai.meituan", "支付成功", "实付", "￥18.00", "优惠金额", "￥2.00"), 1)!!
        assertEquals(1800L, result.amountMinor)
    }

    @Test fun ambiguousCurrencyAmountsNeedManualReview() {
        val result = CaptureEngine().accept(snapshot(wechat, "支付成功", "￥10.00", "￥20.00"), 1)!!
        assertNull(result.amountMinor)
        assertTrue("AMOUNT_CONFLICT" in result.issues)
    }

    @Test fun refundsAreIncomeAndTransfersRequireDirectionReview() {
        val refund = CaptureEngine().accept(snapshot(wechat, "退款成功", "￥10.00"), 1)!!
        assertEquals("income", refund.direction)
        val transfer = CaptureEngine().accept(snapshot(wechat, "支付成功", "转账", "￥10.00"), 1)!!
        assertNull(transfer.direction)
        assertTrue("DIRECTION_UNCERTAIN" in transfer.issues)
    }

    @Test fun meituanDetailsUseRealMerchantSeparatelyFromRemark() {
        val result = CaptureEngine().accept(snapshot("com.dianping.v1", "早餐店", "-12.50", "支付成功", "创建时间", "2026-10-21 08:00:00"), 1)!!
        assertEquals("早餐店", result.merchant)
        assertEquals(1250L, result.amountMinor)
    }

    @Test fun refundNeverBorrowsTheFullAmountFromCheckout() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "￥100.00"), 1)
        val refund = engine.accept(snapshot(wechat, "退款成功", elapsed = 2000), 1)!!
        assertNull(refund.amountMinor)
        assertEquals("income", refund.direction)
    }

    @Test fun aNotificationCannotBorrowCheckoutAmount() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "￥100.00"), 1)
        val notification = snapshot(wechat, "支付成功", elapsed = 2000)
            .copy(source = "notification", sourceEventId = "notification-one")
        assertNull(engine.accept(notification, 1)!!.amountMinor)
    }

    @Test fun unknownCheckoutCannotCrossARestoredLedgerWithTheSameNumericId() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "￥100.00"), 1, "old-ledger")
        val result = engine.accept(snapshot(wechat, "支付成功", elapsed = 2000), 1, "restored-ledger")!!
        assertNull(result.amountMinor)
    }

    @Test fun paymentInstructionsAreNotSuccessEvidence() {
        assertNull(CaptureEngine().accept(snapshot(wechat, "支付成功后将返回商家", "￥10.00"), 1))
    }

    @Test fun sparseResultCannotEraseWarningsAttachedToBorrowedFields() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "转账", "￥10.00", "美元").copy(truncated = true), 1)
        val result = engine.accept(snapshot(wechat, "支付成功", elapsed = 2000), 1)!!
        assertEquals(1000L, result.amountMinor)
        assertNull(result.direction)
        assertTrue(result.issues.containsAll(setOf("AMOUNT_UNANCHORED", "DIRECTION_UNCERTAIN", "CURRENCY_UNCERTAIN", "SNAPSHOT_TRUNCATED")))
    }

    @Test fun laterSparseEventsKeepTheObservedTransactionTime() {
        val engine = CaptureEngine()
        val detail = engine.accept(snapshot(wechat, "支付成功", "支付金额", "￥10.00", "支付时间", "2020-06-15 12:34:56"), 1)!!
        val sparse = engine.accept(snapshot(wechat, "支付成功", elapsed = 2000), 1)!!
        assertNotNull(detail.transactionTime)
        assertEquals(detail.transactionTime, sparse.transactionTime)
        assertEquals(detail.id, sparse.id)
    }

    @Test fun genericPaymentStateCannotReverseAnExplicitIncomeDirection() {
        val engine = CaptureEngine()
        val income = engine.accept(snapshot("com.huawei.wallet", "+¥10.00", "交易成功", "Test shop"), 1)!!
        val sparse = engine.accept(snapshot("com.huawei.wallet", "交易成功", elapsed = 2000), 1)!!
        assertEquals("income", income.direction)
        assertEquals("income", sparse.direction)
        assertEquals("signed-result", sparse.evidence["direction"])
        assertEquals(income.id, sparse.id)
    }

    @Test fun splitCurrencyDigitsKeepTheirFraction() {
        val result = CaptureEngine().accept(snapshot("com.kuaishou.nebula", "支付成功", "实付", "¥", "12", ".50"), 1)!!
        assertEquals(1250L, result.amountMinor)
    }

    @Test fun laterTransactionReferenceDoesNotChangePublishedCandidateId() {
        val engine = CaptureEngine()
        val first = engine.accept(snapshot(wechat, "支付成功", "￥10.00"), 1)!!
        val enriched = engine.accept(snapshot(wechat, "支付成功", "￥10.00", "交易单号", "TRADE202610210001", elapsed = 2000), 1)!!
        assertEquals(first.id, enriched.id)
        assertNull(first.transactionIdentity)
        assertNotNull(enriched.transactionIdentity)
    }

    @Test fun orderReferenceDoesNotMakeTwoInstalmentsOnePayment() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "￥10.00", "订单号", "ORDER202610210001"), 1)
        val first = engine.accept(snapshot(wechat, "支付成功", "￥10.00", elapsed = 2000), 1)!!
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "￥10.00", "订单号", "ORDER202610210001", elapsed = 3000), 1)
        val second = engine.accept(snapshot(wechat, "支付成功", "￥10.00", elapsed = 4000), 1)!!
        assertNotEquals(first.id, second.id)
        assertNull(first.transactionIdentity)
        assertNull(second.transactionIdentity)
    }

    @Test fun orderToTransactionReferenceUpgradeKeepsThePublishedIdentity() {
        val engine = CaptureEngine()
        engine.accept(snapshot(wechat, "确认付款", "支付金额", "￥10.00", "订单号", "ORDER202610210001"), 1)
        val result = engine.accept(snapshot(wechat, "支付成功", "￥10.00", elapsed = 2000), 1)!!
        val enriched = engine.accept(snapshot(wechat, "支付成功", "￥10.00", "交易单号", "TRADE202610210002", elapsed = 3000), 1)!!
        assertEquals(result.id, enriched.id)
        assertNotNull(enriched.transactionIdentity)
        val weaker = engine.accept(snapshot(wechat, "支付成功", "￥10.00", "订单号", "ORDER202610210001", elapsed = 4000), 1)!!
        assertEquals(enriched.id, weaker.id)
        assertEquals(enriched.transactionIdentity, weaker.transactionIdentity)
        assertEquals(enriched.referenceHash, weaker.referenceHash)
    }

    @Test fun missingMerchantCannotTurnAnotherFieldLabelIntoAMerchant() {
        val result = CaptureEngine().accept(snapshot(wechat, "支付成功", "收款方", "支付金额", "￥10.00"), 1)!!
        assertNull(result.merchant)
        assertEquals(1000L, result.amountMinor)
    }

    @Test fun imageUrlsAreNotMerchantNames() {
        val result = CaptureEngine().accept(snapshot("com.huawei.wallet", "-¥10.00", "交易成功", "https://example.invalid/image.png"), 1)!!
        assertNull(result.merchant)
    }

    @Test fun priceFragmentsPreserveTheDecimalAcrossDifferentNodeLayouts() {
        for (parts in listOf(listOf("12", ".50"), listOf("12.", "50"),
            listOf("12", ".", "50"), listOf("12", ".50元"))) {
            val input = listOf("购买成功", "实付", "¥") + parts
            val result = CaptureEngine().accept(snapshot("com.smile.gifmaker", *input.toTypedArray()), 1)!!
            assertEquals(parts.toString(), 1250L, result.amountMinor)
        }
    }

    @Test fun invalidDecimalFragmentCannotBecomeAnIntegerPayment() {
        val result = CaptureEngine().accept(snapshot("com.smile.gifmaker", "购买成功", "实付", "¥", "12", ".345"), 1)!!
        assertNull(result.amountMinor)
        assertTrue("AMOUNT_MISSING" in result.issues)
    }
}
