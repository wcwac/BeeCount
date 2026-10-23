package com.wangc.bill.reference;

import org.junit.Test;
import static org.junit.Assert.*;

public final class OriginalReimbursementEntryTest {
    @Test public void colonViewsKeepSignedAmountsAndFullWidthLongIdentities() {
        String raw=Long.MIN_VALUE+":-3.50:"+Long.MAX_VALUE;OriginalReimbursementEntry entry=OriginalReimbursementEntry.parseStrict(raw);
        assertEquals(Long.MIN_VALUE,entry.assetSemanticId);assertEquals(Long.MAX_VALUE,entry.time);assertEquals(-3.5,entry.amount,0);
        assertEquals(-3.5,entry.aggregateReduction(),0);assertNull(entry.remark);assertEquals(raw,entry.rawInfo);
    }
    @Test public void emptyInteriorRemarkKeepsTheIndependentAddNumber() {
        OriginalReimbursementEntry entry=OriginalReimbursementEntry.parseStrict("0:2.0:7::-1.25");
        assertEquals("",entry.remark);assertEquals(2,entry.amount,0);assertEquals(-1.25,entry.addNumber,0);assertEquals(-1.25,entry.aggregateReduction(),0);
    }
    @Test public void fullwidthColonsInRemarksAreNotFieldSeparators() {
        OriginalReimbursementEntry entry=OriginalReimbursementEntry.parseStrict("17:4.2:9:费用：交通:0");
        assertEquals("费用：交通",entry.remark);assertEquals(4.2,entry.aggregateReduction(),0);assertEquals(9,entry.time);
    }
    @Test public void javaSplitTrailingRemovalAndDetailExtraFieldBehaviorRemainExplicit() {
        OriginalReimbursementEntry shortEntry=OriginalReimbursementEntry.parseStrict("-1:0::");
        assertEquals(0,shortEntry.time);assertNull(shortEntry.remark);assertEquals(-1,shortEntry.assetSemanticId);
        OriginalReimbursementEntry extra=OriginalReimbursementEntry.parseStrict("7:1:2:r:3:untouched");
        assertEquals(3,extra.addNumber,0);assertEquals("7:1:2:r:3:untouched",extra.rawInfo);
    }
    @Test public void unsupportedNumericViewsFailWithoutRewritingRawStorage() {
        for(String raw:new String[]{"","7","bad:1","1:bad","1:2:bad","1:2:3:r:NaN","1:Infinity","9223372036854775808:2"}) {
            try{OriginalReimbursementEntry.parseStrict(raw);fail("Accepted invalid typed view "+raw);}catch(IllegalArgumentException expected){}
        }
    }
}
