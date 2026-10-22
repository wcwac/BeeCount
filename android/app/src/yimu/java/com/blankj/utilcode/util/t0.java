package com.blankj.utilcode.util;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.Locale;
public final class t0 {
 public static long X0(String text, String pattern) {
  if (text == null) return -1;
  try { ParsePosition p = new ParsePosition(0); java.util.Date d = new SimpleDateFormat(pattern, Locale.CHINA).parse(text,p); return d == null ? -1 : d.getTime(); }
  catch (IllegalArgumentException e) { return -1; }
 }
}
