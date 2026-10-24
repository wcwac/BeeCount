package com.wangc.bill.reference;

import org.junit.Test;
import static org.junit.Assert.*;

public final class OriginalLendRecordTest {
    @Test public void theSourceConstructorLeavesEveryPrimitiveAtZeroAndRemarkNull() {
        OriginalLendRecord r = new OriginalLendRecord();
        assertEquals(0, r.localRowId); assertFalse(r.originalDeleted);
        assertEquals(0, r.assetId); assertEquals(0, r.billId); assertEquals(0, r.createTime);
        assertEquals(0, r.fromCost, 0); assertFalse(r.generalServiceBill); assertEquals(0, r.inTime);
        assertEquals(0, r.interest, 0); assertEquals(0, r.lendId); assertEquals(0, r.number, 0);
        assertEquals(0, r.outTime); assertNull(r.remark); assertEquals(0, r.repaymentAssetId);
        assertEquals(0, r.type); assertEquals(0, r.updateTime); assertEquals(0, r.userId);
        assertEquals(1, OriginalLendRecord.LEND); assertEquals(2, OriginalLendRecord.LEND_COLLECTION);
        assertEquals(3, OriginalLendRecord.BORROW); assertEquals(4, OriginalLendRecord.BORROW_REPAYMENT);
    }
    @Test public void sourceTimeIsOutTimeEvenWhenInTimeAndCreationTimeDiffer() {
        OriginalLendRecord r = new OriginalLendRecord(); r.outTime = -5; r.inTime = 99; r.createTime = 100; r.updateTime = 101;
        assertEquals(-5, r.getTime()); r.outTime = 0; assertEquals(0, r.getTime());
    }
    @Test public void currencyGetterUsesNonzeroFromCostOnlyForTheRepaymentEndpoint() {
        OriginalLendRecord r = new OriginalLendRecord(); r.assetId = 7; r.repaymentAssetId = 8;
        r.number = -12; r.fromCost = -9;
        assertEquals(-9, r.getCurrencyCost(8), 0); assertEquals(-12, r.getCurrencyCost(7), 0);
        assertEquals(-12, r.getCurrencyCost(Long.MAX_VALUE), 0); assertEquals(-9, r.getFromCost(), 0);
        r.repaymentAssetId = 0; assertEquals(-9, r.getCurrencyCost(0), 0);
    }
    @Test public void positiveAndNegativeZeroFromCostDoNotBecomeTheCreditFeedsNumber() {
        OriginalLendRecord r = new OriginalLendRecord(); r.repaymentAssetId = -1; r.number = 12;
        for (double zero : new double[]{0.0d, -0.0d}) {
            r.fromCost = zero; assertEquals(12, r.getCurrencyCost(-1), 0);
            assertEquals(Double.doubleToRawLongBits(zero), Double.doubleToRawLongBits(r.getFromCost()));
        }
    }
    @Test public void modelAccessorsPreserveNonfiniteAndSignedRawValuesWithoutAStoragePolicy() {
        OriginalLendRecord r = new OriginalLendRecord(); r.repaymentAssetId = 4; r.number = Double.NEGATIVE_INFINITY;
        r.fromCost = Double.NaN; assertTrue(Double.isNaN(r.getCurrencyCost(4))); assertTrue(Double.isNaN(r.getFromCost()));
        assertEquals(Double.NEGATIVE_INFINITY, r.getCurrencyCost(5), 0);
        r.fromCost = Double.POSITIVE_INFINITY; assertEquals(Double.POSITIVE_INFINITY, r.getCurrencyCost(4), 0);
    }
    @Test public void aRetainedCopyPreservesEveryIdentityAndRawFieldWithoutNormalizingIt() {
        OriginalLendRecord r = new OriginalLendRecord(); r.localRowId = 88; r.originalDeleted = true;
        r.assetId = Long.MIN_VALUE; r.billId = -2; r.createTime = -11; r.fromCost = -1.25;
        r.generalServiceBill = true; r.inTime = Long.MAX_VALUE; r.interest = -.5; r.lendId = Integer.MIN_VALUE;
        r.number = -4.75; r.outTime = -12; r.remark = "原始借贷备注"; r.repaymentAssetId = Long.MAX_VALUE;
        r.type = -9; r.updateTime = -13; r.userId = Integer.MAX_VALUE;
        OriginalLendRecord copy = r.copy(); assertNotSame(r, copy);
        assertEquals(88, copy.localRowId); assertTrue(copy.originalDeleted); assertEquals(Long.MIN_VALUE, copy.assetId);
        assertEquals(-2, copy.billId); assertEquals(-11, copy.createTime); assertEquals(-1.25, copy.fromCost, 0);
        assertTrue(copy.generalServiceBill); assertEquals(Long.MAX_VALUE, copy.inTime); assertEquals(-.5, copy.interest, 0);
        assertEquals(Integer.MIN_VALUE, copy.lendId); assertEquals(-4.75, copy.number, 0); assertEquals(-12, copy.outTime);
        assertEquals(r.remark, copy.remark); assertEquals(Long.MAX_VALUE, copy.repaymentAssetId); assertEquals(-9, copy.type);
        assertEquals(-13, copy.updateTime); assertEquals(Integer.MAX_VALUE, copy.userId);
        copy.remark = null; copy.number = 0; assertEquals("原始借贷备注", r.remark); assertEquals(-4.75, r.number, 0);
        assertNull(new OriginalLendRecord().copy().remark);
    }
}
