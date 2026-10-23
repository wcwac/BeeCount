package com.wangc.bill.reference;

import org.junit.Test;
import static org.junit.Assert.*;

public final class OriginalInstalmentRecordTest {
    @Test public void constructorKeepsPrimitiveDefaultsAndTwoNullableRawStrings() {
        OriginalInstalmentRecord r = new OriginalInstalmentRecord();
        assertEquals(0, r.localRowId); assertEquals(0, r.instalmentId); assertEquals(0, r.userId);
        assertEquals(0, r.assetId); assertEquals(0, r.billId); assertEquals(0, r.inAssetTime);
        assertEquals(0, r.instalmentType); assertEquals(0, r.periods); assertEquals(0, r.remainderType);
        assertEquals(0.0d, r.serviceNumber, 0.0d); assertEquals(0, r.serviceType);
        assertEquals(0.0d, r.totalNumber, 0.0d); assertEquals(0, r.updateTime);
        assertNull(r.accountMonth); assertNull(r.selfInfo); assertFalse(r.originalDeleted);
    }
    @Test public void copyKeepsAllRawFieldsAndIdentitySpacesWithoutDecodingSelfInfo() {
        OriginalInstalmentRecord r = new OriginalInstalmentRecord();
        r.localRowId = 3; r.instalmentId = Long.MAX_VALUE; r.userId = Integer.MIN_VALUE;
        r.accountMonth = " 2020-99 "; r.assetId = Long.MIN_VALUE; r.billId = -7; r.inAssetTime = -8;
        r.instalmentType = -9; r.periods = Integer.MAX_VALUE; r.remainderType = -10;
        r.selfInfo = "unrecognized raw JSON"; r.serviceNumber = -1.25d; r.serviceType = 99;
        r.totalNumber = 3.125d; r.updateTime = Long.MIN_VALUE; r.originalDeleted = true;
        OriginalInstalmentRecord c = r.copy();
        assertNotSame(r, c); assertEquals(3, c.localRowId); assertEquals(Long.MAX_VALUE, c.instalmentId);
        assertEquals(Integer.MIN_VALUE, c.userId); assertEquals(r.accountMonth, c.accountMonth);
        assertEquals(Long.MIN_VALUE, c.assetId); assertEquals(-7, c.billId); assertEquals(-8, c.inAssetTime);
        assertEquals(-9, c.instalmentType); assertEquals(Integer.MAX_VALUE, c.periods); assertEquals(-10, c.remainderType);
        assertEquals(r.selfInfo, c.selfInfo); assertEquals(-1.25d, c.serviceNumber, 0.0d); assertEquals(99, c.serviceType);
        assertEquals(3.125d, c.totalNumber, 0.0d); assertEquals(Long.MIN_VALUE, c.updateTime); assertTrue(c.originalDeleted);
        c.totalNumber = 0; c.selfInfo = ""; assertEquals(3.125d, r.totalNumber, 0.0d); assertEquals("unrecognized raw JSON", r.selfInfo);
    }
}
