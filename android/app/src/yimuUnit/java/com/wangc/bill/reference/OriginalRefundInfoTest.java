package com.wangc.bill.reference;

import org.junit.Test;
import static org.junit.Assert.*;

public final class OriginalRefundInfoTest {
    @Test public void recoveredPrimitiveDefaultsAndNullableTextRemainUninvented() {
        OriginalRefundInfo info=new OriginalRefundInfo();
        assertEquals(0,info.assetId);assertEquals(0,info.inTime);assertEquals(0,info.relatedBillId);assertEquals(0,info.time);
        assertEquals(0,info.number,0);assertEquals(0,info.getCurrentRestoreNum(),0);assertNull(info.remark);assertNull(info.rawJson);
    }
    @Test public void currentRestoreChoosesNonzeroDisplayCapBeforeBaseCapWithoutAbsoluteValue() {
        OriginalRefundInfo info=new OriginalRefundInfo();info.restoreCostNum=7;assertEquals(7,info.getCurrentRestoreNum(),0);
        info.restoreNum=-3;assertEquals(-3,info.getCurrentRestoreNum(),0);info.restoreNum=-0.0;assertEquals(7,info.getCurrentRestoreNum(),0);
    }
    @Test public void copyingDoesNotNarrowIdsOrNormalizeTheRawIdentity() {
        OriginalRefundInfo info=new OriginalRefundInfo();info.assetId=Long.MIN_VALUE;info.relatedBillId=Long.MAX_VALUE;
        info.time=-7;info.inTime=9;info.assetNum=-1.5;info.costNum=2.5;info.currentNumber=4;info.number=-8;
        info.restoreCostNum=11;info.restoreNum=12;info.remark="退款：备注";info.rawJson=" { \"number\" : -8.00 } ";
        OriginalRefundInfo copy=info.copy();assertNotSame(info,copy);assertEquals(Long.MIN_VALUE,copy.assetId);assertEquals(Long.MAX_VALUE,copy.relatedBillId);
        assertEquals(info.rawJson,copy.rawJson);assertEquals(info.remark,copy.remark);assertEquals(-1.5,copy.assetNum,0);assertEquals(2.5,copy.costNum,0);
        assertEquals(4,copy.currentNumber,0);assertEquals(-8,copy.number,0);assertEquals(11,copy.restoreCostNum,0);assertEquals(12,copy.restoreNum,0);
    }
    @Test public void finiteCommandPolicyChecksEveryIndependentNumber() {
        for(int i=0;i<6;i++)for(double bad:new double[]{Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY}) {
            OriginalRefundInfo info=new OriginalRefundInfo();
            switch(i){case 0->info.assetNum=bad;case 1->info.costNum=bad;case 2->info.currentNumber=bad;case 3->info.number=bad;case 4->info.restoreCostNum=bad;case 5->info.restoreNum=bad;default->throw new AssertionError();}
            try{info.validateFinite();fail("Accepted a nonfinite refund command");}catch(IllegalArgumentException expected){}
        }
    }
}
