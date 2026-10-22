package com.wangc.bill.utils;
import java.util.Calendar;
public final class b2 {
 public static boolean m0(long first,long second) { Calendar a=Calendar.getInstance(),b=Calendar.getInstance();a.setTimeInMillis(first);b.setTimeInMillis(second);return a.get(Calendar.ERA)==b.get(Calendar.ERA)&&a.get(Calendar.YEAR)==b.get(Calendar.YEAR)&&a.get(Calendar.DAY_OF_YEAR)==b.get(Calendar.DAY_OF_YEAR); }
}
