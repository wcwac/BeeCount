package com.wangc.bill.reference;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/** AutoAdd's h2.J/N/S, including their deliberately different fee predicates. */
public final class OriginalTransferAmount {
    private OriginalTransferAmount() {}

    // j5.d.u/p recovered as encoded String values in classes2.dex, rather than
    // substituting a locale or currency parser for the original regular expressions.
    private static final String DECIMAL = "^-?([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|0?\\.0+|0)$";
    private static final String INTEGER = "^(-?[1-9]\\d*)|0$";

    public static boolean isJ(String value) {
        if (value.matches(DECIMAL) || value.matches(INTEGER)) return true;
        if (!value.isEmpty()) {
            try { Double.parseDouble(value); return true; }
            catch (NumberFormatException ignored) {}
        }
        return false;
    }

    public static boolean isN(String value) { return value.matches("\\+?-?[0-9]+.*[0-9]*"); }

    public static double parseS(String value) {
        try { return Double.parseDouble(value.replace(",", ".")); }
        catch (NumberFormatException ignored) { return 0.0d; }
    }

    /** h2.i: locale-sensitive balance text, at most two decimals, no grouping. */
    public static String formatBalance(double value) {
        NumberFormat format = NumberFormat.getInstance();
        format.setRoundingMode(RoundingMode.HALF_UP); format.setMaximumFractionDigits(2);
        format.setGroupingUsed(false); return format.format(value);
    }

    /** h2.q, including its h2.r -> S zero gate and literal decimal-point prefix checks. */
    public static String formatDelta(double value) {
        if (value == 0.0d || roundR(value) == 0.0d) return "0.00";
        DecimalFormat format = new DecimalFormat("#,###.00"); format.setRoundingMode(RoundingMode.HALF_UP);
        String text = format.format(value);
        if (text.startsWith(".")) return "0" + text;
        if (!text.startsWith("-.")) return text;
        return "-0" + text.replace("-", "");
    }

    private static double roundR(double value) {
        DecimalFormat format = new DecimalFormat("0.00"); format.setRoundingMode(RoundingMode.HALF_UP);
        return parseS(format.format(value));
    }
}
