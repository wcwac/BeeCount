package com.meituan.android.mrn.container;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Own synthetic page using the package/page and text gates read by action.k.
 * Contains no Meituan code and does not establish current real-app compatibility. */
public final class MRNBaseActivity extends Activity {
    private final Handler main = new Handler(Looper.getMainLooper());
    private LinearLayout root;
    private TextView amountLeaf;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24, 48, 24, 24);
        add("正在处理模拟美团支付");
        amountLeaf = new TextView(this); amountLeaf.setTextSize(22); amountLeaf.setPadding(0, 15, 0, 15);
        amountLeaf.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        if (android.os.Build.VERSION.SDK_INT >= 34) amountLeaf.setAccessibilityDataSensitive(View.ACCESSIBILITY_DATA_SENSITIVE_YES);
        root.addView(amountLeaf);
        root.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onInitializeAccessibilityEvent(host, event);
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) event.setClassName(MRNBaseActivity.class.getName());
                redirect(event);
            }
            @Override public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
                boolean accepted = super.onRequestSendAccessibilityEvent(host, child, event); redirect(event); return accepted;
            }
        });
        setContentView(root);
        main.postDelayed(() -> {
            String amount = getIntent().getStringExtra("fixture_amount");
            if (amount == null || !amount.matches("[1-9][0-9]{0,3}\\.[0-9]{2}")) amount = "27.80";
            // k.t recognizes this page. q requires 创建时间 + 支付成功;
            // v takes merchant and signed amount immediately before 支付成功.
            root.removeViewAt(0);
            TextView merchant = label("功能验收同一商户"); root.addView(merchant, 0);
            amountLeaf.setText("-" + amount);
            add("支付成功"); add("创建时间"); add("2025-06-15 12:34:56"); add("支付方式"); add("微信支付");
            root.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        }, 750);
    }

    private TextView label(String text) {
        TextView value = new TextView(this); value.setText(text); value.setTextSize(22); value.setPadding(0, 15, 0, 15);
        if (android.os.Build.VERSION.SDK_INT >= 34) value.setAccessibilityDataSensitive(View.ACCESSIBILITY_DATA_SENSITIVE_YES);
        return value;
    }
    private void add(String text) { root.addView(label(text)); }
    private void redirect(AccessibilityEvent event) { if (amountLeaf != null) event.setSource(amountLeaf); }
    @Override public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        boolean populated = super.dispatchPopulateAccessibilityEvent(event); redirect(event); return populated;
    }
    @Override protected void onDestroy() { main.removeCallbacksAndMessages(null); super.onDestroy(); }
}
