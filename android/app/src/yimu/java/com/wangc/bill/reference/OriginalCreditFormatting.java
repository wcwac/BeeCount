package com.wangc.bill.reference;

import com.wangc.bill.utils.h2;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/** Recovered h2.b/q display, including the strict 99,999 threshold for 万. */
public final class OriginalCreditFormatting {
    private OriginalCreditFormatting(){}
    public static String amount(double value){
        double absolute=Math.abs(value);DecimalFormat format=new DecimalFormat("#,###.00");format.setRoundingMode(RoundingMode.HALF_UP);
        if(absolute>99999.0d&&absolute<100000000.0d)return format.format(value/10000.0d)+"万";
        if(absolute>=100000000.0d)return format.format(value/100000000.0d)+"亿";
        if(value==0.0d||h2.r(value)==0.0d)return "0.00";String text=format.format(value);if(text.startsWith("."))return "0"+text;if(text.startsWith("-."))return "-0"+text.replace("-","");return text;
    }
}
