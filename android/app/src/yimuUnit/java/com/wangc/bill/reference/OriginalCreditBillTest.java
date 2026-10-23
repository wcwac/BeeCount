package com.wangc.bill.reference;

import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

public final class OriginalCreditBillTest {
    @Test public void decimalAccumulatorsAvoidBinaryAdditionDriftWithoutRoundingRawSetters(){OriginalCreditBill b=new OriginalCreditBill();b.addNumber(.1);b.addNumber(.2);assertEquals(.3,b.getNumber(),0);b.reduceNumber(.1);assertEquals(.2,b.getNumber(),0);b.addServiceCost(.1);b.addServiceCost(.2);assertEquals(.3,b.getServiceCost(),0);b.setNumber(1.2345);assertEquals(1.2345,b.getNumber(),0);}
    @Test public void snapshotsDetachMutableListsWhileKeepingAbsentListsNull(){OriginalCreditBill b=new OriginalCreditBill();assertNull(b.copy().getBillInfoList());OriginalCreditBillInfo info=new OriginalCreditBillInfo();info.setTitle("合成行");b.addBill(info);b.setRemainServiceCost(2.125);OriginalCreditBill copy=b.copy();info.setTitle("修改原对象");assertEquals("合成行",copy.getBillInfoList().get(0).getTitle());assertEquals(2.125,copy.getRemainServiceCost(),0);assertNotSame(b.getBillInfoList(),copy.getBillInfoList());}
    @Test public void displayUsesTheRecoveredStrictWanThresholdAndSignedSmallAmounts(){Locale before=Locale.getDefault();try{Locale.setDefault(Locale.US);assertEquals("99,999.00",OriginalCreditFormatting.amount(99999));assertEquals("10.00万",OriginalCreditFormatting.amount(100000));assertEquals("1.00亿",OriginalCreditFormatting.amount(100000000));assertEquals("-0.25",OriginalCreditFormatting.amount(-.25));assertEquals("0.00",OriginalCreditFormatting.amount(-.001));}finally{Locale.setDefault(before);}}
}
