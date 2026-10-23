package com.wangc.bill.reference;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public final class OriginalTransferRecordTest {
    @Test public void constructorKeepsOriginalPrimitiveDefaultsAndAnIndependentEmptyTagList() {
        OriginalTransferRecord r = new OriginalTransferRecord(), other = new OriginalTransferRecord();
        assertEquals(0, r.localRowId); assertFalse(r.originalDeleted);
        assertEquals(0, r.billId); assertEquals(0, r.userId); assertNull(r.remark);
        assertEquals(0, r.fromAssetId); assertEquals(0, r.toAssetId); assertEquals(0, r.transferId);
        assertEquals(0, r.time); assertEquals(0, r.updateTime);
        assertEquals(0d, r.cost, 0d); assertEquals(0d, r.toCost, 0d); assertEquals(0d, r.serviceCharge, 0d);
        assertNotNull(r.tags); assertTrue(r.tags.isEmpty()); assertNotSame(r.tags, other.tags);
    }

    @Test public void eitherSignedZeroReceivingAmountFallsBackToTheUnchangedSendingAmountForEveryQuery() {
        OriginalTransferRecord r = new OriginalTransferRecord(); r.fromAssetId = 11; r.toAssetId = 12; r.cost = -3.125d;
        for (double zero : new double[]{0.0d, -0.0d}) {
            r.toCost = zero;
            assertEquals(-3.125d, r.getCurrencyCost(11), 0d);
            assertEquals(-3.125d, r.getCurrencyCost(12), 0d);
            assertEquals(-3.125d, r.getCurrencyCost(Long.MIN_VALUE), 0d);
        }
        r.cost = -0.0d;
        assertEquals(Double.doubleToRawLongBits(-0.0d), Double.doubleToRawLongBits(r.getCurrencyCost(12)));
    }

    @Test public void aNonzeroReceivingAmountAppliesToEveryAssetExceptTheSendingAssetWithoutAbsoluteValues() {
        OriginalTransferRecord r = new OriginalTransferRecord(); r.fromAssetId = Long.MIN_VALUE; r.toAssetId = Long.MAX_VALUE;
        r.cost = -17.25d; r.toCost = -2.75d;
        assertEquals(-17.25d, r.getCurrencyCost(Long.MIN_VALUE), 0d);
        assertEquals(-2.75d, r.getCurrencyCost(Long.MAX_VALUE), 0d);
        assertEquals(-2.75d, r.getCurrencyCost(0), 0d);
        assertEquals(-2.75d, r.getCurrencyCost(-1), 0d);
        r.toAssetId = r.fromAssetId;
        assertEquals(-17.25d, r.getCurrencyCost(r.toAssetId), 0d);
    }

    @Test public void theNumericGetterDoesNotIntroduceAFiniteValuePolicyOrExchangeRateCalculation() {
        OriginalTransferRecord r = new OriginalTransferRecord(); r.fromAssetId = 5; r.cost = -11d; r.toCost = Double.NaN;
        assertEquals(-11d, r.getCurrencyCost(5), 0d); assertTrue(Double.isNaN(r.getCurrencyCost(99)));
        r.toCost = Double.POSITIVE_INFINITY; r.cost = Double.NEGATIVE_INFINITY;
        assertEquals(Double.NEGATIVE_INFINITY, r.getCurrencyCost(5), 0d);
        assertEquals(Double.POSITIVE_INFINITY, r.getCurrencyCost(99), 0d);
    }

    @Test public void explicitTagAssignmentDeduplicatesFirstOccurrencesIncludingNullAndCopiesTheInput() {
        OriginalTransferRecord r = new OriginalTransferRecord();
        ArrayList<Long> input = new ArrayList<>(Arrays.asList(8L, null, 5L, 8L, null, Long.MIN_VALUE, 5L));
        r.setTags(input);
        assertEquals(Arrays.asList(8L, null, 5L, Long.MIN_VALUE), r.getTags()); assertNotSame(input, r.getTags());
        input.clear(); assertEquals(Arrays.asList(8L, null, 5L, Long.MIN_VALUE), r.getTags());
        r.tags.add(9L); assertTrue(input.isEmpty());
    }

    @Test public void nullAndEmptyExplicitTagAssignmentsRemainDistinct() {
        OriginalTransferRecord r = new OriginalTransferRecord(); r.setTags(null);
        assertNull(r.tags); assertNull(r.getTags());
        r.setTags(new ArrayList<>()); assertNotNull(r.getTags()); assertTrue(r.getTags().isEmpty());
    }

    @Test public void addTagCreatesAMissingListAndSuppressesEqualLongAndNullDuplicates() {
        OriginalTransferRecord r = new OriginalTransferRecord(); r.tags = null;
        r.addTag(Long.valueOf(2048)); r.addTag(Long.valueOf(2048)); r.addTag(null); r.addTag(null); r.addTag(-1L);
        assertEquals(Arrays.asList(2048L, null, -1L), r.tags);
    }

    @Test public void rawCopyPreservesAllFieldsLongIdentityBoundsAndDuplicateTagOrderWithoutAliasing() {
        OriginalTransferRecord r = new OriginalTransferRecord(); r.localRowId = 3; r.originalDeleted = true;
        r.billId = Integer.MIN_VALUE; r.userId = Integer.MAX_VALUE; r.transferId = Long.MAX_VALUE;
        r.fromAssetId = Long.MIN_VALUE; r.toAssetId = -1; r.time = Long.MIN_VALUE; r.updateTime = Long.MAX_VALUE;
        r.cost = -7.125d; r.toCost = 9.875d; r.serviceCharge = -0.25d; r.remark = "  raw:转账  ";
        r.tags = new ArrayList<>(Arrays.asList(4L, 4L, null, 2L, null));
        OriginalTransferRecord c = r.copy();
        assertNotSame(r, c); assertEquals(3, c.localRowId); assertTrue(c.originalDeleted);
        assertEquals(Integer.MIN_VALUE, c.billId); assertEquals(Integer.MAX_VALUE, c.userId);
        assertEquals(Long.MAX_VALUE, c.transferId); assertEquals(Long.MIN_VALUE, c.fromAssetId); assertEquals(-1, c.toAssetId);
        assertEquals(Long.MIN_VALUE, c.time); assertEquals(Long.MAX_VALUE, c.updateTime);
        assertEquals(-7.125d, c.cost, 0d); assertEquals(9.875d, c.toCost, 0d); assertEquals(-0.25d, c.serviceCharge, 0d);
        assertEquals("  raw:转账  ", c.remark); assertEquals(Arrays.asList(4L, 4L, null, 2L, null), c.tags);
        assertNotSame(r.tags, c.tags); c.tags.remove(0); c.localRowId = 99; c.transferId = 0;
        assertEquals(Arrays.asList(4L, 4L, null, 2L, null), r.tags); assertEquals(3, r.localRowId); assertEquals(Long.MAX_VALUE, r.transferId);
    }

    @Test public void rawCopyPreservesNullReferencesEmptyStringsAndSignedZero() {
        OriginalTransferRecord r = new OriginalTransferRecord(); r.tags = null; r.remark = null; r.toCost = -0.0d;
        OriginalTransferRecord c = r.copy(); assertNull(c.tags); assertNull(c.remark);
        assertEquals(Double.doubleToRawLongBits(-0.0d), Double.doubleToRawLongBits(c.toCost));
        r.remark = ""; c = r.copy(); assertEquals("", c.remark); assertNull(c.tags);
    }

    @Test public void rawDuplicatesAndTheLiveGetterRemainIntactUntilAnExplicitSetterIsUsed() {
        OriginalTransferRecord r = new OriginalTransferRecord(); r.tags = new ArrayList<>(Arrays.asList(6L, 6L));
        assertSame(r.tags, r.getTags()); r.addTag(6L); r.addTag(7L);
        assertEquals(Arrays.asList(6L, 6L, 7L), r.tags);
        r.getTags().add(null); assertEquals(Arrays.asList(6L, 6L, 7L, null), r.tags);
        r.setTags(r.getTags()); assertEquals(Arrays.asList(6L, 7L, null), r.tags);
    }
}
