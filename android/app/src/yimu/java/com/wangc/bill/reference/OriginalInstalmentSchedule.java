package com.wangc.bill.reference;

import com.wangc.bill.utils.h2;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/** Recovered g1.q/t/s/A/C/z and b2 date boundaries. Calendar remains lenient. */
public final class OriginalInstalmentSchedule {
    private OriginalInstalmentSchedule(){}
    public static final class SelfInfo {
        public int periods;public double number,service;
        public SelfInfo(int periods,double number,double service){this.periods=periods;this.number=number;this.service=service;}
    }
    public static final class Period {
        public int periods;public double number,serviceCost;public String date,repaymentDay;
    }
    public static Map<Integer,SelfInfo> selfInfo(String raw){
        Map<Integer,SelfInfo> values=new HashMap<>();if(raw==null||raw.isEmpty())return values;
        try{JSONArray list=new JSONArray(raw);for(int i=0;i<list.length();i++){JSONObject row=list.getJSONObject(i);SelfInfo value=new SelfInfo(row.optInt("periods"),row.optDouble("number",0),row.optDouble("service",0));values.put(value.periods,value);}}
        catch(org.json.JSONException error){throw new IllegalArgumentException("Invalid original instalment selfInfo",error);}return values;
    }
    public static double number(OriginalInstalmentRecord row,int period,Map<Integer,SelfInfo> overrides){
        if(overrides.containsKey(period)){SelfInfo value=overrides.get(period);return value.number+value.service;}
        if(row.serviceNumber==0.0d||row.serviceType==1)return split(row,row.totalNumber+row.serviceNumber,period);
        if(row.serviceNumber!=0.0d&&row.serviceType==2){double value=split(row,row.totalNumber,period);return period==1?value+row.serviceNumber:value;}
        if(row.serviceNumber!=0.0d&&row.serviceType==3)return split(row,row.totalNumber,period);
        if(row.serviceNumber==0.0d||row.serviceType!=4)return 0.0d;
        return period==row.periods?split(row,row.serviceNumber,period)+row.totalNumber:split(row,row.serviceNumber,period);
    }
    public static double service(OriginalInstalmentRecord row,int period,Map<Integer,SelfInfo> overrides){
        if(overrides.containsKey(period))return overrides.get(period).service;
        if(row.serviceNumber==0.0d||row.serviceType==1)return split(row,row.serviceNumber,period);
        if(row.serviceNumber!=0.0d&&(row.serviceType==2||row.serviceType==3))return period==1?row.serviceNumber:0.0d;
        return row.serviceNumber!=0.0d&&row.serviceType==4?split(row,row.serviceNumber,period):0.0d;
    }
    /** e0.c rounds its decimal multiplication to scale2 before the source int narrowing. */
    private static double hundred(double value){return new BigDecimal(Double.toString(h2.r(value))).multiply(new BigDecimal("100.0")).setScale(2,RoundingMode.HALF_UP).doubleValue();}
    public static double split(OriginalInstalmentRecord row,double total,int period){
        int remainder=((int)hundred(total))%row.periods;
        if(remainder==0)return h2.r(total/(double)row.periods);
        double base=((hundred(total)-(double)remainder)/(double)row.periods)/100.0d;
        double last=(double)remainder/100.0d;
        if(row.remainderType==0)return period==1?h2.r(base+last):base;
        if(row.remainderType==1)return period==row.periods?h2.r(base+last):base;
        if(row.remainderType==2){double rounded=h2.r(total/(double)row.periods);return period==1?h2.r(total-(row.periods-1)*rounded):rounded;}
        if(row.remainderType!=3)return 0.0d;
        double rounded=h2.r(total/(double)row.periods);return period==row.periods?h2.r(total-(row.periods-1)*rounded):rounded;
    }
    /** g1.t, including direct month iteration and last custom override for duplicate periods. */
    public static List<Period> periods(OriginalInstalmentRecord row,Map<Integer,SelfInfo> overrides){
        List<Period> result=new ArrayList<>();int year=year(row.inAssetTime),month=month(row.inAssetTime),day=day(row.inAssetTime);
        for(int i=1;i<=row.periods;i++){Period value=new Period();value.periods=i;value.number=number(row,i,overrides);value.serviceCost=service(row,i,overrides);value.date=year+"年"+month+"月"+Math.min(daysInMonth(year,month-1),day)+"日";result.add(value);month++;if(month>12){year++;month=1;}}return result;
    }
    public static List<Period> periods(OriginalInstalmentRecord row){return periods(row,selfInfo(row.selfInfo));}
    public static OriginalDayInfo statementPeriod(int startDay,long time,int configuredMonthStart){
        int year=year(time),month=month(time)-1,currentDay=day(time);if(startDay==0)startDay=configuredMonthStart;
        Calendar begin=Calendar.getInstance();begin.clear();begin.set(year,month,1,0,0,0);begin.set(Calendar.MILLISECOND,0);int normalized=startDay==0?1:startDay;
        if(currentDay<normalized){begin.add(Calendar.MONTH,-1);if(begin.getActualMaximum(Calendar.DAY_OF_MONTH)<normalized){begin.add(Calendar.MONTH,1);begin.set(Calendar.DAY_OF_MONTH,1);}else begin.set(Calendar.DAY_OF_MONTH,normalized);}else begin.set(Calendar.DAY_OF_MONTH,normalized);
        Calendar end=Calendar.getInstance();end.clear();end.set(year,month,1,23,59,59);end.set(Calendar.MILLISECOND,999);
        if(startDay<=1)end.set(Calendar.DAY_OF_MONTH,end.getActualMaximum(Calendar.DAY_OF_MONTH));else{if(currentDay>=startDay)end.add(Calendar.MONTH,1);int max=end.getActualMaximum(Calendar.DAY_OF_MONTH);end.set(Calendar.DAY_OF_MONTH,max<startDay?max:startDay-1);}
        OriginalDayInfo result=new OriginalDayInfo();result.setMinDay(begin.getTimeInMillis());result.setMaxDay(end.getTimeInMillis());return result;
    }
    /** Asset.getCurrentOutAccountDate mutates its text normalization on the loaded object. */
    public static int statementDay(String raw,boolean outDayNotNext){if(raw==null||raw.isEmpty())return 0;int value=Integer.parseInt(raw.replace("每月","").replace("号",""));return outDayNotNext?(value>=31?1:value+1):value;}
    /** s2.u differs deliberately from g1.s for an empty inAccountDate. */
    public static String repaymentDay(String rawIn,OriginalDayInfo period,boolean creditStatement){
        int lastDay=day(period.getMaxDay()),chosen,overflow=0;boolean empty=rawIn==null||rawIn.isEmpty();
        if(empty)chosen=creditStatement?daysInMonth(year(period.getMaxDay()),month(period.getMaxDay())-1):lastDay;
        else{chosen=rawIn.startsWith("出账日")?Integer.parseInt(rawIn.replace("出账日",""))+lastDay:Integer.parseInt(rawIn);int maximum=daysInMonth(year(period.getMaxDay()),month(period.getMaxDay())-1);if(chosen>maximum){overflow=chosen-maximum;chosen=maximum;}}
        int year=year(period.getMaxDay()),month=month(period.getMaxDay());
        if(lastDay<chosen||(lastDay==chosen&&overflow>0)||(creditStatement&&empty))return format(addDays(parse(year+"."+month+"."+chosen,"yyyy.MM.dd"),overflow),"yyyy-MM-dd");
        month++;if(month>12){month=1;year++;}return format(parse(year+"."+month+"."+chosen,"yyyy.MM.dd"),"yyyy-MM-dd");
    }
    public static Period repaymentPeriod(OriginalInstalmentRecord row,int index,Map<Integer,SelfInfo> overrides,String rawOut,boolean outDayNotNext,String rawIn,int configuredMonthStart){
        int year=year(row.inAssetTime),m=month(row.inAssetTime)+index,month=m-1;if(month>12){month=m-13;year++;}
        long time=parse(year+"."+month+"."+Math.min(daysInMonth(year,month-1),day(row.inAssetTime)),"yyyy.MM.dd");
        Period result=new Period();result.periods=index;result.number=number(row,index,overrides);result.serviceCost=service(row,index,overrides);result.date=format(time,"yyyy-MM-dd");result.repaymentDay=repaymentDay(rawIn,statementPeriod(statementDay(rawOut,outDayNotNext),time,configuredMonthStart),false);return result;
    }
    public static long parse(String text,String pattern){try{return new SimpleDateFormat(pattern,Locale.getDefault()).parse(text).getTime();}catch(ParseException error){return -1;}}
    public static String format(long time,String pattern){return new SimpleDateFormat(pattern,Locale.getDefault()).format(new java.util.Date(time));}
    public static int year(long time){Calendar c=Calendar.getInstance();c.setTimeInMillis(time);return c.get(Calendar.YEAR);}
    public static int month(long time){Calendar c=Calendar.getInstance();c.setTimeInMillis(time);return c.get(Calendar.MONTH)+1;}
    public static int day(long time){Calendar c=Calendar.getInstance();c.setTimeInMillis(time);return c.get(Calendar.DAY_OF_MONTH);}
    public static int daysInMonth(int year,int zeroMonth){Calendar c=Calendar.getInstance();c.clear();c.set(year,zeroMonth,1);return c.getActualMaximum(Calendar.DAY_OF_MONTH);}
    public static long addDays(long time,int count){Calendar c=Calendar.getInstance();c.clear();c.setTimeInMillis(time);c.add(Calendar.DAY_OF_MONTH,count);return c.getTimeInMillis();}
    /** Preserve b2.L's signed-int seconds narrowing rather than silently fixing year2038. */
    public static long dayStart(long time){Calendar c=Calendar.getInstance();c.clear();c.setTimeInMillis(time);c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return (long)(int)(c.getTimeInMillis()/1000L)*1000L;}
    public static long dayEnd(long time){Calendar c=Calendar.getInstance();c.clear();c.setTimeInMillis(time);c.set(Calendar.HOUR_OF_DAY,23);c.set(Calendar.MINUTE,59);c.set(Calendar.SECOND,59);c.set(Calendar.MILLISECOND,999);return c.getTimeInMillis();}
}
