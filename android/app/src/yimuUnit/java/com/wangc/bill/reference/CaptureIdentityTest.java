package com.wangc.bill.reference;

import com.wangc.bill.reference.capture.CaptureIdentity;
import com.wangc.bill.reference.capture.CapturedAmount;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public final class CaptureIdentityTest {
    private static CaptureIdentity.Identity identity(String pkg,int window,String kind,long amount,long time,String... texts) {
        return CaptureIdentity.identify(pkg,window,kind,amount,time,"同一商户","零钱",Arrays.asList(texts));
    }
    @Test public void aRealPaymentIdSurvivesWindowAmountAndTimestampCorrections() {
        CaptureIdentity.Identity first=identity("wechat",7,"expense",3010,1,"交易单号","PAY202610230001");
        CaptureIdentity.Identity detail=identity("wechat",8,"expense",3011,2,"交易单号：PAY202610230001");
        assertEquals(CaptureIdentity.PAYMENT_ID,first.kind);assertFalse(first.requiresConfirmation);
        assertEquals(first.key,detail.key);assertNotEquals(first.windowKey,detail.windowKey);
    }
    @Test public void identicalPaymentsInDifferentAppsAreIndependent() {
        assertNotEquals(identity("wechat",7,"expense",3010,1,"交易单号","PAY202610230001").key,
                identity("meituan",7,"expense",3010,1,"交易单号","PAY202610230001").key);
    }
    @Test public void theDirectionIsPartOfTheStrongPaymentIdentity() {
        assertNotEquals(identity("wechat",7,"expense",3010,1,"支付单号","PAY202610230001").key,
                identity("wechat",7,"income",3010,1,"支付单号","PAY202610230001").key);
    }
    @Test public void merchantOrdersAreAdvisoryAndPartialPaymentsRemainDistinct() {
        CaptureIdentity.Identity first=identity("taobao",7,"expense",3010,1,"商户订单号","ORDER202610230001");
        CaptureIdentity.Identity second=identity("taobao",7,"expense",5000,1,"商户订单号","ORDER202610230001");
        assertEquals(CaptureIdentity.WINDOW_RECEIPT,first.kind);assertTrue(first.requiresConfirmation);
        assertEquals("",first.paymentId);assertEquals("ORDER202610230001",first.orderReference);
        assertNotEquals(first.key,second.key);
    }
    @Test public void separateTransactionTimesAreNeverThrottledByOneSecond() {
        assertNotEquals(identity("wechat",7,"expense",3010,100,"付款方式","零钱").key,
                identity("wechat",7,"expense",3010,101,"付款方式","零钱").key);
    }
    @Test public void sameMinorAmountIgnoresItsOriginalDecimalSpelling() {
        long first=CapturedAmount.minorUnits("30.1"), second=CapturedAmount.minorUnits("30.10");
        assertEquals(identity("wechat",7,"expense",first,1).key,identity("wechat",7,"expense",second,1).key);
    }
    @Test public void conflictingPaymentIdsAndMaskedValuesRequireManualConfirmation() {
        CaptureIdentity.Identity many=identity("alipay",7,"expense",3010,1,"交易单号","PAY202610230001","支付单号","PAY202610230002");
        assertTrue(many.requiresConfirmation);assertEquals("",many.paymentId);
        assertTrue(identity("alipay",7,"expense",3010,1,"交易单号","2026****0001").requiresConfirmation);
        assertTrue(identity("alipay",7,"expense",3010,1,"交易单号","2026-10-23").requiresConfirmation);
    }
    @Test public void refundCannotUseItsOriginalPaymentIdAsAUniqueRefundId() {
        CaptureIdentity.Identity first=identity("alipay",7,"income",3010,1,"退款成功","交易单号","PAY202610230001");
        CaptureIdentity.Identity partial=identity("alipay",7,"income",1000,2,"退款成功","交易单号","PAY202610230001");
        assertTrue(first.requiresConfirmation);assertNotEquals(first.key,partial.key);
        CaptureIdentity.Identity proved=identity("alipay",7,"income",3010,1,"退款成功","退款单号","REF202610230001","交易单号","PAY202610230001");
        assertEquals("REF202610230001",proved.paymentId);assertFalse(proved.requiresConfirmation);
    }
    @Test public void aNewlyVisiblePaymentIdKeepsTheSamePendingWindowGroupingKey() {
        CaptureIdentity.Identity weak=identity("wechat",7,"expense",3010,1,"付款方式","零钱");
        CaptureIdentity.Identity strong=identity("wechat",7,"expense",3010,1,"付款方式","零钱","交易单号","PAY202610230001");
        assertEquals(weak.windowKey,strong.windowKey);assertNotEquals(weak.key,strong.key);
    }
    @Test public void unknownWindowAndNarrativeTextCannotProduceAHardIdentity() {
        CaptureIdentity.Identity value=identity("wechat",-1,"expense",3010,1,"请核对交易单号PAY202610230001");
        assertNull(value.key);assertEquals(CaptureIdentity.UNRESOLVED,value.kind);assertTrue(value.requiresConfirmation);
    }
    @Test public void differentPaymentMethodsDoNotCollapseWithinAnUnresolvedWindow() {
        CaptureIdentity.Identity first=CaptureIdentity.identify("wechat",7,"expense",3010,1,"店铺","零钱",Collections.emptyList());
        CaptureIdentity.Identity second=CaptureIdentity.identify("wechat",7,"expense",3010,1,"店铺","银行卡",Collections.emptyList());
        assertNotEquals(first.key,second.key);
    }
    @Test public void aReusedWindowIdAfterServiceRestartCannotMergeASeparateWeakPayment() {
        CaptureIdentity.Identity first=CaptureIdentity.identify("wechat","session-one",7,"expense",3010,0,"店铺","零钱",Collections.emptyList());
        CaptureIdentity.Identity restarted=CaptureIdentity.identify("wechat","session-two",7,"expense",3010,0,"店铺","零钱",Collections.emptyList());
        assertNotEquals(first.key,restarted.key);
        java.util.List<String> proved=Arrays.asList("交易单号","PAY202610230001");
        assertEquals(CaptureIdentity.identify("wechat","session-one",7,"expense",3010,0,"店铺","零钱",proved).key,
                CaptureIdentity.identify("wechat","session-two",7,"expense",3010,0,"店铺","零钱",proved).key);
    }
    @Test public void aSynthesizedParserClockIsNotEvidenceOfAnotherPayment() {
        assertEquals(0,CaptureIdentity.visibleTransactionTime(1720000000000L,Arrays.asList("支付成功","付款方式","余额宝")));
        assertEquals(1720000000000L,CaptureIdentity.visibleTransactionTime(1720000000000L,
                Arrays.asList("交易时间","2025-06-15 12:34:56")));
        assertEquals(1720000000000L,CaptureIdentity.visibleTransactionTime(1720000000000L,
                Arrays.asList("交易时间：2025年06月15日 12时34分56秒")));
    }
}
