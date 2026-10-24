package com.wangc.bill.reference.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

/** Recovered OppoFloatTempLayout: three constructors, the original tiny child layout. */
public final class OriginalOppoFloatTempLayout extends RelativeLayout {
    public OriginalOppoFloatTempLayout(Context context) {
        super(context);
        inflateOriginal();
    }

    public OriginalOppoFloatTempLayout(Context context, AttributeSet attributes) {
        super(context, attributes);
        inflateOriginal();
    }

    public OriginalOppoFloatTempLayout(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
        inflateOriginal();
    }

    private void inflateOriginal() {
        int resource = Ui.res(getContext(), "layout", "layout_oppo_float_temp");
        if (resource == 0) throw new IllegalStateException("原版兼容悬浮窗布局缺失");
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(resource, this);
    }
}
