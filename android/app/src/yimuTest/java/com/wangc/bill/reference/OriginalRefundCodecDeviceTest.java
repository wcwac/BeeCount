package com.wangc.bill.reference;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class OriginalRefundCodecDeviceTest {
    @Test public void explicitNewWriterRoundTripsAllIndependentFieldsAndFullWidthIds() {
        OriginalRefundInfo info=new OriginalRefundInfo();info.assetId=Long.MIN_VALUE;info.relatedBillId=Long.MAX_VALUE;
        info.inTime=8;info.time=9;info.assetNum=-1.5;info.costNum=2.5;info.currentNumber=-3.5;info.number=4.5;
        info.restoreCostNum=5.5;info.restoreNum=6.5;
        String raw=OriginalRefundCodec.encodeNew(info);OriginalRefundInfo decoded=OriginalRefundCodec.decodeStrict(raw);
        assertEquals(Long.MIN_VALUE,decoded.assetId);assertEquals(Long.MAX_VALUE,decoded.relatedBillId);assertEquals(8,decoded.inTime);assertEquals(9,decoded.time);
        assertEquals(-1.5,decoded.assetNum,0);assertEquals(2.5,decoded.costNum,0);assertEquals(-3.5,decoded.currentNumber,0);assertEquals(4.5,decoded.number,0);
        assertEquals(5.5,decoded.restoreCostNum,0);assertEquals(6.5,decoded.restoreNum,0);assertNull(decoded.remark);assertEquals(raw,decoded.rawJson);
    }
    @Test public void rawUnknownKeysAndNumberSpellingAreNeverReencodedForIdentity() {
        String raw=" { \"number\" : 2.00, \"unknown\" : [1,true], \"remark\":\"退：款\" } ";
        OriginalRefundInfo info=OriginalRefundCodec.decodeStrict(raw);assertEquals(raw,info.rawJson);assertEquals("退：款",info.remark);
        assertEquals(0,info.assetId);assertEquals(2,info.number,0);
        try{OriginalRefundCodec.encodeNew(info);fail("Reencoded a stored identity");}catch(IllegalArgumentException expected){}
    }
    @Test public void strictViewsRejectCoercedOrNonfiniteCommandsWithoutClaimingGsonCompatibility() {
        for(String raw:new String[]{"{\"assetId\":\"1\"}","{\"assetId\":1.5}","{\"assetId\":9223372036854775808}","{\"number\":\"NaN\"}","{\"number\":1e400}","{\"remark\":9}"}) {
            try{OriginalRefundCodec.decodeStrict(raw);fail("Accepted unsupported typed JSON "+raw);}catch(IllegalArgumentException expected){}
        }
    }
    @Test public void legacyAliasesAreAnExplicitDetailOnlyPartialMutationWithTheOriginalGuard() {
        String legacy="{\"a\":-3,\"b\":2.0,\"c\":4,\"d\":\"旧备注\",\"e\":7}";
        OriginalRefundInfo info=OriginalRefundCodec.decodeStrict(legacy);assertEquals(0,info.assetId);assertEquals(0,info.time);assertEquals(0,info.number,0);
        assertTrue(OriginalRefundCodec.applyLegacyAliasesForDetail(info,legacy));assertEquals(-3,info.assetId);assertEquals(2,info.number,0);assertEquals(4,info.currentNumber,0);
        assertEquals("旧备注",info.remark);assertEquals(7,info.time);assertEquals(legacy,info.rawJson);
        assertFalse(OriginalRefundCodec.applyLegacyAliasesForDetail(info,"{\"a\":9}"));assertEquals(-3,info.assetId);
        OriginalRefundInfo partial=new OriginalRefundInfo();assertFalse(OriginalRefundCodec.applyLegacyAliasesForDetail(partial,"{\"a\":12}"));
        assertEquals(12,partial.assetId);assertEquals(0,partial.number,0);assertEquals(0,partial.time);
    }
}
