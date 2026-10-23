package com.alipay.android.msp.ui.views;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/** Project-owned synthetic page; never contains or executes Alipay application code. */
public final class MspContainerActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        RelativeLayout root = new RelativeLayout(this);
        root.setPadding(24, 48, 24, 24);
        LinearLayout lines = new LinearLayout(this);
        lines.setOrientation(LinearLayout.VERTICAL);
        root.addView(lines, new RelativeLayout.LayoutParams(-1, -2));
        add(lines, "正在处理模拟支付");
        setContentView(root);
        // Preserve natural Activity STATE and widget CONTENT event classes. The older
        // fixtures rewrote every class to an Activity and did not exercise this path.
        handler.postDelayed(() -> {
            lines.removeAllViews();
            // b.w: success + payment method, without bill detail/payment time, chooses
            // state 2. b.r reads the next amount and the 收款方 label's next item.
            String amount = getIntent().getStringExtra("fixture_amount");
            if (amount == null || !amount.matches("[1-9][0-9]{0,3}\\.[0-9]{2}")) amount = "18.60";
            for (String line : new String[]{"支付成功", "￥" + amount, "收款方", "实验支付宝商户", "付款方式", "余额"}) add(lines, line);
        }, 650);
        // Burst and subsequent updates all have an actual RelativeLayout source.
        // A changed payment page must still be read when content events coalesce.
        for (int i = 0; i < 36; i++) {
            handler.postDelayed(() -> root.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED), 100L + i * 55L);
        }
        handler.postDelayed(() -> root.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED), 2600);
    }

    private void add(LinearLayout lines, String value) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(22);
        text.setPadding(0, 15, 0, 15);
        if (android.os.Build.VERSION.SDK_INT >= 34) text.setAccessibilityDataSensitive(View.ACCESSIBILITY_DATA_SENSITIVE_YES);
        lines.addView(text);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
