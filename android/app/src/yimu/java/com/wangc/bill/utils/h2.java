package com.wangc.bill.utils;
import java.text.DecimalFormat;
import java.math.RoundingMode;
/** Small inspected numeric subset of the recovered helper; no unrelated SDK utilities. */
public final class h2 {
 public static boolean J(String value) { if(value==null||value.isEmpty())return false;try{Double.parseDouble(value);return true;}catch(NumberFormatException e){return false;} }
 public static double S(String value) { try{return Double.parseDouble(value.replace(",","."));}catch(NumberFormatException e){return 0;} }
 public static double r(double value) { DecimalFormat f=new DecimalFormat("0.00");f.setRoundingMode(RoundingMode.HALF_UP);return S(f.format(value)); }
 public static String t(double value) { if(value==0||r(value)==0)return "0.00";DecimalFormat f=new DecimalFormat("#.00");f.setRoundingMode(RoundingMode.HALF_UP);String s=f.format(value);if(s.startsWith("."))return "0"+s;if(s.startsWith("-."))return "-0"+s.replace("-","");return s; }
}
