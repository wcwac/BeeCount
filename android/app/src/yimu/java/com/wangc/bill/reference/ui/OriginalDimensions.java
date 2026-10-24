package com.wangc.bill.reference.ui;

import android.content.res.Resources;

/** Original m.w -> x0.w -> p0.b: system density, float addition and Java truncation. */
public final class OriginalDimensions {
    private OriginalDimensions() {}
    public static int dp(float value) {
        return (int) (value * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }
}
