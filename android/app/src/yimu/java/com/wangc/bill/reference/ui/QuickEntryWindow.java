package com.wangc.bill.reference.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;
import com.wangc.bill.reference.ReferenceApplication;

/** A movable manual shortcut, independent of payment capture and its confirmation window. */
public final class QuickEntryWindow {
    public static final String ENABLED = "quick_entry_enabled";
    private static final String X = "quick_entry_x_permille", Y = "quick_entry_y_permille";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static WindowManager manager;
    private static Context windowContext;
    private static volatile TextView button;
    private static WindowManager.LayoutParams parameters;
    private static volatile String result = "OFF";
    private QuickEntryWindow() {}

    public static boolean enabled() { return ReferenceApplication.prefs().getBoolean(ENABLED, false); }
    public static boolean attached() { TextView value = button; return value != null && value.isAttachedToWindow(); }
    public static String statusText() {
        if (!enabled()) return "已关闭";
        if (!OverlayPermission.granted()) return "等待悬浮窗授权";
        if (attached()) return "已显示，点击＋打开记账；拖动可调整位置";
        return result.equals("FAILED") ? "暂未显示，请关闭后重新开启" : "等待显示";
    }
    public static void setEnabled(boolean value) {
        ReferenceApplication.prefs().edit().putBoolean(ENABLED, value).apply(); refresh();
    }
    public static void resetPosition() {
        ReferenceApplication.prefs().edit().remove(X).remove(Y).apply(); refresh();
    }
    public static void onActivityResumed(Activity activity) {
        if (activity instanceof OriginalPermissionRequestActivity) hide(); else refresh();
    }
    public static void refresh() {
        if (Looper.myLooper() != Looper.getMainLooper()) { MAIN.post(QuickEntryWindow::refresh); return; }
        if (!enabled() || !OverlayPermission.granted()) { remove(); result = enabled() ? "NEEDS_PERMISSION" : "OFF"; return; }
        try {
            if (manager == null) {
                Context app = ReferenceApplication.app();
                Display display = app.getSystemService(DisplayManager.class).getDisplay(Display.DEFAULT_DISPLAY);
                if (display == null) throw new IllegalStateException("Display unavailable");
                Context visual = app.createDisplayContext(display);
                windowContext = Build.VERSION.SDK_INT >= 30
                        ? visual.createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null) : visual;
                manager = windowContext.getSystemService(WindowManager.class);
            }
            int size = Ui.dp(windowContext, 48);
            parameters = parameters == null ? new WindowManager.LayoutParams(size, size,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT) : parameters;
            parameters.width = parameters.height = size;
            parameters.gravity = Gravity.TOP | Gravity.START;
            Point area = availableArea();
            parameters.x = Math.round(Math.max(0, area.x - size) * fraction(X, 1000));
            parameters.y = Math.round(Math.max(0, area.y - size) * fraction(Y, 500));
            if (button != null && (button.getParent() != null || button.isAttachedToWindow())) manager.updateViewLayout(button, parameters);
            else { button = createButton(); manager.addView(button, parameters); }
            result = "SHOWN";
        } catch (RuntimeException failure) {
            remove(); result = "FAILED";
            Log.w("QuickEntry", "Window unavailable: " + failure.getClass().getSimpleName());
        }
    }
    public static void hide() {
        if (Looper.myLooper() != Looper.getMainLooper()) { MAIN.post(QuickEntryWindow::hide); return; }
        remove();
    }
    private static float fraction(String key, int fallback) {
        return Math.max(0, Math.min(1000, ReferenceApplication.prefs().getInt(key, fallback))) / 1000f;
    }
    private static Point availableArea() {
        if (Build.VERSION.SDK_INT >= 30) {
            android.view.WindowMetrics metrics = manager.getCurrentWindowMetrics();
            Rect bounds = metrics.getBounds();
            android.graphics.Insets safe = metrics.getWindowInsets().getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            return new Point(Math.max(0, bounds.width() - safe.left - safe.right),
                    Math.max(0, bounds.height() - safe.top - safe.bottom));
        }
        Point value = new Point(); manager.getDefaultDisplay().getSize(value); return value;
    }
    @SuppressLint("ClickableViewAccessibility")
    private static TextView createButton() {
        TextView view = new TextView(windowContext);
        view.setText("＋"); view.setTextSize(30); view.setTextColor(0xffffffff); view.setGravity(Gravity.CENTER);
        view.setContentDescription("打开快捷记账");
        GradientDrawable shape = new GradientDrawable(); shape.setShape(GradientDrawable.OVAL); shape.setColor(Ui.GREEN);
        view.setBackground(shape); view.setElevation(Ui.dp(windowContext, 4));
        view.setOnClickListener(clicked -> {
            if (!enabled() || button != view || !view.isAttachedToWindow()) return;
            try {
                windowContext.startActivity(new Intent(windowContext, EditorActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            } catch (RuntimeException failure) {
                result = "OPEN_FAILED"; Ui.toast(windowContext, "暂时无法打开，请从应用主页进入记账");
                Log.w("QuickEntry", "Editor unavailable: " + failure.getClass().getSimpleName());
            }
        });
        int slop = ViewConfiguration.get(windowContext).getScaledTouchSlop();
        view.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY; int startX, startY; boolean moved;
            @Override public boolean onTouch(View target, MotionEvent event) {
                if (target != button || !target.isAttachedToWindow()) return true;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN -> {
                        downX = event.getRawX(); downY = event.getRawY(); startX = parameters.x; startY = parameters.y; moved = false;
                    }
                    case MotionEvent.ACTION_MOVE -> {
                        float dx = event.getRawX() - downX, dy = event.getRawY() - downY;
                        if (dx * dx + dy * dy > slop * slop) moved = true;
                        if (moved) {
                            Point area = availableArea();
                            parameters.x = Math.max(0, Math.min(Math.max(0, area.x - parameters.width), startX + Math.round(dx)));
                            parameters.y = Math.max(0, Math.min(Math.max(0, area.y - parameters.height), startY + Math.round(dy)));
                            try { manager.updateViewLayout(target, parameters); }
                            catch (RuntimeException failure) { remove(); result = "FAILED"; }
                        }
                    }
                    case MotionEvent.ACTION_UP -> { if (moved) rememberPosition(); else target.performClick(); }
                    case MotionEvent.ACTION_CANCEL -> { if (moved) rememberPosition(); }
                    default -> { moved = true; }
                }
                return true;
            }
        });
        return view;
    }
    private static void rememberPosition() {
        Point area = availableArea();
        ReferenceApplication.prefs().edit()
                .putInt(X, Math.round(1000f * parameters.x / Math.max(1, area.x - parameters.width)))
                .putInt(Y, Math.round(1000f * parameters.y / Math.max(1, area.y - parameters.height))).apply();
    }
    private static void remove() {
        TextView old = button; button = null;
        if (old != null && (old.getParent() != null || old.isAttachedToWindow())) {
            try { manager.removeView(old); }
            catch (RuntimeException failure) { Log.w("QuickEntry", "Window already detached: " + failure.getClass().getSimpleName()); }
        }
    }
}
