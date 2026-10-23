package com.wangc.bill.reference;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Financial expectations transcribed from g1.A/C/z and b2 calendar contracts. */
public final class OriginalInstalmentScheduleTest {
    private Locale locale;private TimeZone zone;
    @Before public void stableDates(){locale=Locale.getDefault();zone=TimeZone.getDefault();Locale.setDefault(Locale.US);TimeZone.setDefault(TimeZone.getTimeZone("UTC"));}
    @After public void restoreDates(){Locale.setDefault(locale);TimeZone.setDefault(zone);}
    private OriginalInstalmentRecord row(){OriginalInstalmentRecord r=new OriginalInstalmentRecord();r.periods=3;r.totalNumber=10;r.inAssetTime=OriginalInstalmentSchedule.parse("2025-01-31","yyyy-MM-dd");return r;}
    @Test public void centRemainderBelongsToTheSelectedFirstOrLastPeriod(){OriginalInstalmentRecord r=row();r.remainderType=0;assertEquals(3.34,OriginalInstalmentSchedule.split(r,10,1),0);assertEquals(3.33,OriginalInstalmentSchedule.split(r,10,3),0);r.remainderType=1;assertEquals(3.33,OriginalInstalmentSchedule.split(r,10,1),0);assertEquals(3.34,OriginalInstalmentSchedule.split(r,10,3),0);r.remainderType=2;assertEquals(3.34,OriginalInstalmentSchedule.split(r,10,1),0);r.remainderType=3;assertEquals(3.34,OriginalInstalmentSchedule.split(r,10,3),0);}
    @Test public void feeModesPreservePrincipalAndFeeTiming(){OriginalInstalmentRecord r=row();r.totalNumber=30;r.serviceNumber=6;Map<Integer,OriginalInstalmentSchedule.SelfInfo> empty=Collections.emptyMap();r.serviceType=1;assertEquals(12,OriginalInstalmentSchedule.number(r,1,empty),0);assertEquals(2,OriginalInstalmentSchedule.service(r,3,empty),0);r.serviceType=2;assertEquals(16,OriginalInstalmentSchedule.number(r,1,empty),0);assertEquals(10,OriginalInstalmentSchedule.number(r,2,empty),0);r.serviceType=3;assertEquals(10,OriginalInstalmentSchedule.number(r,1,empty),0);assertEquals(6,OriginalInstalmentSchedule.service(r,1,empty),0);r.serviceType=4;assertEquals(2,OriginalInstalmentSchedule.number(r,1,empty),0);assertEquals(32,OriginalInstalmentSchedule.number(r,3,empty),0);}
    @Test public void explicitCustomPeriodOverridesBothAmountAndFee(){OriginalInstalmentRecord r=row();Map<Integer,OriginalInstalmentSchedule.SelfInfo> values=new HashMap<>();values.put(2,new OriginalInstalmentSchedule.SelfInfo(2,1.25,.35));assertEquals(1.6,OriginalInstalmentSchedule.number(r,2,values),0);assertEquals(.35,OriginalInstalmentSchedule.service(r,2,values),0);assertEquals(3.34,OriginalInstalmentSchedule.number(r,1,values),0);}
    @Test public void monthIterationKeepsTheOriginalDayAcrossFebruary(){List<OriginalInstalmentSchedule.Period> rows=OriginalInstalmentSchedule.periods(row(),Collections.emptyMap());assertEquals("2025年1月31日",rows.get(0).date);assertEquals("2025年2月28日",rows.get(1).date);assertEquals("2025年3月31日",rows.get(2).date);}
    @Test public void zeroMonthStartUsesTheWholeCalendarMonth(){OriginalDayInfo d=OriginalInstalmentSchedule.statementPeriod(0,OriginalInstalmentSchedule.parse("2025-02-14","yyyy-MM-dd"),0);assertEquals("2025-02-01",OriginalInstalmentSchedule.format(d.getMinDay(),"yyyy-MM-dd"));assertEquals("2025-02-28 23:59:59.999",OriginalInstalmentSchedule.format(d.getMaxDay(),"yyyy-MM-dd HH:mm:ss.SSS"));}
    @Test public void shortMonthStatementBoundaryDoesNotOverflowIntoMarch(){OriginalDayInfo d=OriginalInstalmentSchedule.statementPeriod(31,OriginalInstalmentSchedule.parse("2025-02-14","yyyy-MM-dd"),0);assertEquals("2025-01-31",OriginalInstalmentSchedule.format(d.getMinDay(),"yyyy-MM-dd"));assertEquals("2025-02-28",OriginalInstalmentSchedule.format(d.getMaxDay(),"yyyy-MM-dd"));}
    @Test public void emptyRepaymentDateDiffersBetweenStatementAndInstalment(){OriginalDayInfo d=OriginalInstalmentSchedule.statementPeriod(0,OriginalInstalmentSchedule.parse("2025-01-14","yyyy-MM-dd"),0);assertEquals("2025-01-31",OriginalInstalmentSchedule.repaymentDay(null,d,true));assertEquals("2025-03-03",OriginalInstalmentSchedule.repaymentDay(null,d,false));assertEquals("2025-02-10",OriginalInstalmentSchedule.repaymentDay("出账日10",d,true));assertEquals(1,OriginalInstalmentSchedule.statementDay("每月31号",true));}
    @Test public void zeroPeriodsRetainsTheOriginalArithmeticFailure(){OriginalInstalmentRecord r=row();r.periods=0;try{OriginalInstalmentSchedule.split(r,10,1);fail("Invented a zero-period repayment");}catch(ArithmeticException expected){}}
}
