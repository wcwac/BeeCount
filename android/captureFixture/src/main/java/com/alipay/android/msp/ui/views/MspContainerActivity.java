package com.alipay.android.msp.ui.views;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/** Project-owned synthetic page; never contains or executes Alipay application code. */
public final class MspContainerActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private RelativeLayout root;
    private boolean missingEventSource;
    private static final int MISSING_VIRTUAL_NODE = 7391;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        missingEventSource = getIntent().getBooleanExtra("fixture_null_source", false);
        root = new RelativeLayout(this);
        if (missingEventSource) root.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onInitializeAccessibilityEvent(host, event);
                redirectMissingSource(event);
            }
            @Override public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
                boolean allowed = super.onRequestSendAccessibilityEvent(host, child, event);
                // Include natural descendant CONTENT updates when TextViews change.
                redirectMissingSource(event);
                return allowed;
            }
        });
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
        }, missingEventSource ? 1300 : 650);
        // Burst and subsequent updates retain the natural RelativeLayout class.
        // A changed payment page must still be read when content events coalesce.
        for (int i = 0; i < 36; i++) {
            handler.postDelayed(() -> root.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED), 100L + i * 55L);
        }
        handler.postDelayed(() -> root.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED), 2600);
    }

    private void redirectMissingSource(AccessibilityEvent event) {
        if (missingEventSource && root != null) {
            // Keep the real window ID, but refer to a virtual descendant that does
            // not exist. The physical root remains readable. Since this View has
            // no virtual node provider, the receiving service's getSource() must
            // resolve to null rather than a fabricated/test-only Java result.
            event.setSource(root, MISSING_VIRTUAL_NODE);
        }
    }

    @Override public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        boolean populated = super.dispatchPopulateAccessibilityEvent(event);
        // Activity STATE events also use the missing node. Even a queued early
        // event cannot read later payment text through a valid event source.
        redirectMissingSource(event);
        return populated;
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
