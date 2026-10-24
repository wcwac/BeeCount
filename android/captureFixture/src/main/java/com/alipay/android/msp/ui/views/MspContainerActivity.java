package com.alipay.android.msp.ui.views;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/** Project-owned synthetic page; never contains or executes Alipay application code. */
public final class MspContainerActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private RelativeLayout root;
    private boolean missingEventSource;
    private boolean leafEventSource;
    private TextView paymentAmount;
    private static final int MISSING_VIRTUAL_NODE = 7391;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        missingEventSource = getIntent().getBooleanExtra("fixture_null_source", false);
        leafEventSource = getIntent().getBooleanExtra("fixture_leaf_source", false);
        root = new RelativeLayout(this);
        if (missingEventSource) root.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            private final AccessibilityNodeProvider provider = new AccessibilityNodeProvider() {
                @Override public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualId) {
                    if (virtualId != HOST_VIEW_ID) return null;
                    // Calling root.createAccessibilityNodeInfo() here would recurse
                    // through this provider. Initialize the real host directly so
                    // a window-root traversal still sees its physical children.
                    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(root);
                    root.onInitializeAccessibilityNodeInfo(node);
                    if (node.getChildCount() == 0) for (int i = 0; i < root.getChildCount(); i++) node.addChild(root.getChildAt(i));
                    return node;
                }
            };
            @Override public AccessibilityNodeProvider getAccessibilityNodeProvider(View host) {
                return provider;
            }
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
        if (leafEventSource) root.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onInitializeAccessibilityEvent(host, event); redirectLeafSource(event);
            }
            @Override public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
                boolean allowed = super.onRequestSendAccessibilityEvent(host, child, event);
                redirectLeafSource(event); return allowed;
            }
        });
        root.setPadding(24, 48, 24, 24);
        LinearLayout lines = new LinearLayout(this);
        lines.setOrientation(LinearLayout.VERTICAL);
        root.addView(lines, new RelativeLayout.LayoutParams(-1, -2));
        add(lines, "正在处理模拟支付");
        if (leafEventSource) {
            // A stable real leaf is attached before the initial Activity event.
            // Never replace/detach it: queued events must not fall back merely
            // because a synthetic source was destroyed during the update.
            paymentAmount = new TextView(this); paymentAmount.setTextSize(22);
            paymentAmount.setPadding(0, 15, 0, 15);
            paymentAmount.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            if (android.os.Build.VERSION.SDK_INT >= 34) paymentAmount.setAccessibilityDataSensitive(View.ACCESSIBILITY_DATA_SENSITIVE_YES);
            lines.addView(paymentAmount);
        }
        setContentView(root);
        // Preserve natural Activity STATE and widget CONTENT event classes. The older
        // fixtures rewrote every class to an Activity and did not exercise this path.
        handler.postDelayed(() -> {
            // b.w: success + payment method, without bill detail/payment time, chooses
            // state 2. b.r reads the next amount and the 收款方 label's next item.
            String amount = getIntent().getStringExtra("fixture_amount");
            if (amount == null || !amount.matches("[1-9][0-9]{0,3}\\.[0-9]{2}")) amount = "18.60";
            // b.r normalizes 余额/账户余额/可用余额 back to 支付宝. 余额宝
            // remains distinct here and can only come from the adjacent 付款方式
            // field: this merchant has no 商品说明/余额宝收益发放 shortcut.
            if (leafEventSource) {
                // Keep the original amount leaf attached throughout the update.
                // Its own text cannot provide the sibling labels the parser needs.
                lines.removeViewAt(0);
                TextView success = new TextView(this); success.setText("支付成功");
                success.setTextSize(22); success.setPadding(0, 15, 0, 15);
                lines.addView(success, 0); paymentAmount.setText("￥" + amount);
                for (String line : new String[]{"收款方", "实验支付宝商户", "付款方式", "余额宝"}) add(lines, line);
                root.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            } else {
                lines.removeAllViews();
                for (String line : new String[]{"支付成功", "￥" + amount, "收款方", "实验支付宝商户", "付款方式", "余额宝"}) add(lines, line);
            }
            // Optional evidence for automatic-save workflow cases. Default fixtures
            // stay weak; this is displayed as real nodes, never a Kernel test hook.
            String paymentId = getIntent().getStringExtra("fixture_payment_id");
            if (paymentId != null && paymentId.matches("[A-Za-z0-9_-]{8,128}")) {
                add(lines, "交易单号"); add(lines, paymentId);
            }
        }, missingEventSource ? 1300 : 650);
        // Burst and subsequent updates retain the natural RelativeLayout class.
        // A changed payment page must still be read when content events coalesce.
        if (!leafEventSource) {
            for (int i = 0; i < 36; i++) {
                handler.postDelayed(() -> root.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED), 100L + i * 55L);
            }
            handler.postDelayed(() -> root.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED), 2600);
        }
    }

    private void redirectMissingSource(AccessibilityEvent event) {
        if (missingEventSource && root != null) {
            // Keep the real window ID, but refer to a virtual descendant that does
            // not exist. A real provider explicitly returns null for this ID;
            // otherwise Android can resolve a no-provider View to its host.
            // HOST_VIEW_ID remains readable for the actual window traversal.
            event.setSource(root, MISSING_VIRTUAL_NODE);
        }
    }

    @Override public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        boolean populated = super.dispatchPopulateAccessibilityEvent(event);
        // Activity STATE events also use the missing node. Even a queued early
        // event cannot read later payment text through a valid event source.
        redirectMissingSource(event); redirectLeafSource(event);
        return populated;
    }

    private void redirectLeafSource(AccessibilityEvent event) {
        if (leafEventSource && paymentAmount != null) event.setSource(paymentAmount);
    }

    private void add(LinearLayout lines, String value) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(22);
        text.setPadding(0, 15, 0, 15);
        // The recovered reader requests INCLUDE_NOT_IMPORTANT_VIEWS. This real
        // payment-method label must remain available for both its gate and asset.
        if (value.equals("付款方式")) text.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        if (android.os.Build.VERSION.SDK_INT >= 34) text.setAccessibilityDataSensitive(View.ACCESSIBILITY_DATA_SENSITIVE_YES);
        lines.addView(text);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
