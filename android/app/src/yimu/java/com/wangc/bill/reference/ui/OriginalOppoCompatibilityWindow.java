package com.wangc.bill.reference.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * o2's local compatibility window, independent of the confirmation overlay and its preference.
 * Source: o2.java:16-87, classes4.dex methods28425..28430. Call on the main thread.
 */
public final class OriginalOppoCompatibilityWindow {
    private static OriginalOppoCompatibilityWindow singleton;
    private OriginalOppoFloatTempLayout view;
    private Context context;
    private WindowManager.LayoutParams parameters;

    private OriginalOppoCompatibilityWindow(Context value) {
        context = value;
    }

    public static OriginalOppoCompatibilityWindow instance(Context value) {
        if (singleton == null) singleton = new OriginalOppoCompatibilityWindow(value);
        // Preserve o2.d: replace the controller context, without recreating an existing view.
        singleton.context = value;
        return singleton;
    }

    public boolean isAttached() {
        return manager() != null && view != null && view.isAttachedToWindow();
    }

    public void remove() {
        WindowManager manager = manager();
        if (manager == null || view == null || !view.isAttachedToWindow()) return;
        manager.removeView(view);
        view = null;
        // Source keeps parameters: a future show takes the reused-position branch.
    }

    @SuppressLint("InlinedApi")
    public void show() {
        if (isAttached() || !Settings.canDrawOverlays(context.getApplicationContext())) return;
        WindowManager manager = manager();
        if (parameters == null) {
            parameters = new WindowManager.LayoutParams();
            parameters.gravity = Gravity.START | Gravity.TOP;
            updateDimensionsAndX();
            parameters.y = (OriginalAutoTipDialog.screenHeight(context) / 5) * 4;
            parameters.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            parameters.flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            parameters.format = PixelFormat.RGBA_8888;
        } else {
            updateDimensionsAndX();
            parameters.y = OriginalAutoTipDialog.screenHeight(context) / 2;
        }
        if (view == null) view = new OriginalOppoFloatTempLayout(context);
        if (manager != null) {
            try {
                manager.addView(view, parameters);
            } catch (IllegalStateException ignored) {
                // Only this source exception is caught. SecurityException/BadToken propagate.
                Log.d("o2", "view has already been added to the window mManager.");
            }
        }
    }

    private void updateDimensionsAndX() {
        parameters.width = OriginalDimensions.dp(2f);
        parameters.height = OriginalDimensions.dp(5f);
        parameters.x = OriginalAutoTipDialog.screenWidth(context) - parameters.width;
    }

    private WindowManager manager() {
        return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    OriginalOppoFloatTempLayout retainedView() {
        return view;
    }

    WindowManager.LayoutParams retainedParams() {
        if (parameters == null) return null;
        WindowManager.LayoutParams snapshot = new WindowManager.LayoutParams();
        snapshot.copyFrom(parameters);
        return snapshot;
    }
}
