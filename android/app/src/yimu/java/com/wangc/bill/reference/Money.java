package com.wangc.bill.reference;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

public final class Money {
    private Money() {}
    public static int scale(String currency) {
        int value = Currency.getInstance(currency).getDefaultFractionDigits();
        if (value < 0 || value > 3) throw new IllegalArgumentException("不支持的币种精度");
        return value;
    }
    public static long signed(String raw, String currency) {
        if (raw == null || !raw.trim().matches("[+-]?(?:[0-9]+|[0-9]{1,3}(?:,[0-9]{3})+)(?:\\.[0-9]+)?"))
            throw new IllegalArgumentException("请输入有效金额");
        try {
            long value = new BigDecimal(raw.trim().replace(",", "")).movePointRight(scale(currency)).longValueExact();
            if (value < -999_999_999_999_99L || value > 999_999_999_999_99L) throw new ArithmeticException();
            return value;
        } catch (ArithmeticException error) { throw new IllegalArgumentException("金额过大或小数位超过币种精度"); }
    }
    public static long positive(String raw, String currency) {
        long value = signed(raw, currency);
        if (value <= 0) throw new IllegalArgumentException("金额必须大于零");
        return value;
    }
    public static String text(long value, String currency) {
        return BigDecimal.valueOf(value, scale(currency)).setScale(scale(currency), RoundingMode.UNNECESSARY).toPlainString();
    }
    public static String label(long value, String currency) { return currency + " " + text(value, currency); }
}
