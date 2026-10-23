package com.wangc.bill.reference.capture;

import com.wangc.bill.reference.Money;
import com.wangc.bill.utils.h2;
import java.math.BigDecimal;

/** Amount shown by AutoAddLayout.q0, then parsed by the shared save action. */
public final class CapturedAmount {
    private CapturedAmount() {}

    public static long minorUnits(String raw) {
        if (raw == null) throw new IllegalArgumentException("金额为空");
        // Original q0: h2.S(number) -> Math.abs -> h2.t (two decimal places).
        // h2.S reads that displayed value again, including a locale's decimal comma.
        double displayed = h2.S(h2.t(Math.abs(h2.S(raw))));
        return Money.positive(BigDecimal.valueOf(displayed).toPlainString(), "CNY");
    }
}
