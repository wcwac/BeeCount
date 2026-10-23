package com.wangc.bill.reference;

import org.junit.Test;
import static org.junit.Assert.*;

public final class OriginalBillRecordTest {
    @Test public void constructorKeepsOriginalPrimitiveDefaultsAndOnlyInitializesTags() {
        OriginalBillRecord bill = new OriginalBillRecord();
        assertEquals(0, bill.localRowId); assertEquals(0, bill.billId); assertEquals(0, bill.assetId);
        assertEquals(0, bill.userId); assertEquals(0, bill.fromUserId); assertEquals(0, bill.bookId);
        assertEquals(0.0d, bill.cost, 0.0d); assertEquals(0.0d, bill.currencyAssetNumber, 0.0d);
        assertFalse(bill.reimbursement); assertFalse(bill.reimbursementEnd);
        assertFalse(bill.notIntoBudget); assertFalse(bill.notIntoTotal);
        assertNull(bill.remark); assertNull(bill.currencyInfo); assertNull(bill.billShareInfo);
        assertNull(bill.poiAddress); assertNull(bill.totalAddress);
        assertNotNull(bill.tags); assertTrue(bill.tags.isEmpty());
    }

    @Test public void absentCurrencyInfoUsesRawCostEvenWithANonzeroOverride() {
        OriginalBillRecord bill = new OriginalBillRecord();
        bill.cost = -12.34567d; bill.currencyAssetNumber = 80.0d;
        assertEquals(-12.34567d, bill.getCurrencyCost(), 0.0d);
        bill.currencyInfo = "";
        assertEquals(-12.34567d, bill.getCurrencyCost(), 0.0d);
    }

    @Test public void bothSignedZeroOverridesUseCostWithoutRevaluation() {
        OriginalBillRecord bill = new OriginalBillRecord();
        bill.cost = -3.125d; bill.currencyInfo = "USD 999";
        bill.currencyAssetNumber = 0.0d; assertEquals(-3.125d, bill.getCurrencyCost(), 0.0d);
        bill.currencyAssetNumber = -0.0d; assertEquals(-3.125d, bill.getCurrencyCost(), 0.0d);
    }

    @Test public void anyNonemptyInfoEnablesTheExactNonzeroOverride() {
        OriginalBillRecord bill = new OriginalBillRecord();
        bill.cost = 50.0d; bill.currencyAssetNumber = -1.234567d;
        bill.currencyInfo = "malformed"; assertEquals(-1.234567d, bill.getCurrencyCost(), 0.0d);
        bill.currencyInfo = " "; assertEquals(-1.234567d, bill.getCurrencyCost(), 0.0d);
    }

    @Test public void semanticEqualityDoesNotIdentifyAnOwnerOrLocalDatabaseRow() {
        OriginalBillRecord first = new OriginalBillRecord(), second = new OriginalBillRecord();
        first.billId = second.billId = 901;
        first.localRowId = 1; second.localRowId = 200;
        first.userId = 7; second.userId = 8;
        assertEquals(first, second); assertEquals(first.hashCode(), second.hashCode());
        second.billId = 902; assertNotEquals(first, second);
        assertNotEquals(first, null); assertNotEquals(first, Long.valueOf(901));
    }
}
