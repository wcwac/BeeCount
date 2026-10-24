package com.wangc.bill.reference;

import android.app.Dialog;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import static org.junit.Assert.*;

/** Real-view input coordinates for test windows; never changes a view or a window. */
public final class ReferenceWindowGeometry {
    private ReferenceWindowGeometry() {}

    public static Rect visibleBoundsInScreen(View view) {
        assertNotNull("Input target is missing", view);
        Rect visible = new Rect();
        assertTrue("Input target has no visible rectangle: " + describe(view), view.getGlobalVisibleRect(visible));
        // getGlobalVisibleRect is relative to the window's root, whereas input
        // injection uses screen coordinates. Window offsets are not always0.
        int[] screen = new int[2], window = new int[2];
        view.getLocationOnScreen(screen); view.getLocationInWindow(window);
        visible.offset(screen[0] - window[0], screen[1] - window[1]);
        assertFalse("Input target has an empty screen rectangle: " + describe(view), visible.isEmpty());
        return visible;
    }

    public static Point center(View view) {
        assertNotNull("Input target is missing", view);
        assertTrue("Input target is detached: " + describe(view), view.isAttachedToWindow());
        assertTrue("Input target is hidden: " + describe(view), view.isShown());
        assertTrue("Input target is disabled: " + describe(view), view.isEnabled());
        assertTrue("Input target is not laid out: " + describe(view), view.isLaidOut() && view.getWidth() > 0 && view.getHeight() > 0);
        int[] screen = new int[2]; view.getLocationOnScreen(screen);
        Point point = new Point(screen[0] + view.getWidth() / 2, screen[1] + view.getHeight() / 2);
        Rect visible = visibleBoundsInScreen(view);
        assertTrue("Input target center is clipped: target=" + point + ", screenVisible=" + visible + ", " + describe(view), visible.contains(point.x, point.y));
        return point;
    }

    public static Point outside(Dialog dialog, View activityDecor) {
        assertNotNull("Outside-touch dialog is missing", dialog);
        assertTrue("Outside-touch dialog is hidden: " + describeWindow(dialog), dialog.isShowing());
        Window window = dialog.getWindow(); assertNotNull("Outside-touch dialog has no window", window);
        View decor = window.getDecorView();
        assertTrue("Outside-touch window is detached: " + describeWindow(dialog), decor.isAttachedToWindow());
        assertTrue("Outside-touch window has no focus: " + describeWindow(dialog), decor.hasWindowFocus());
        assertTrue("Outside-touch window is not laid out: " + describeWindow(dialog), decor.isLaidOut() && decor.getWidth() > 0 && decor.getHeight() > 0);
        assertNotNull("Outside-touch host decor is missing", activityDecor);
        assertTrue("Outside-touch host decor is detached", activityDecor.isAttachedToWindow());
        Rect frame = boundsInScreen(decor);
        int slop = ViewConfiguration.get(decor.getContext()).getScaledWindowTouchSlop();
        Rect expanded = new Rect(frame); expanded.inset(-slop, -slop);
        Rect safe = new Rect(); activityDecor.getWindowVisibleDisplayFrame(safe);
        Rect host = boundsInScreen(activityDecor);
        WindowInsets insets = activityDecor.getRootWindowInsets();
        if (insets != null) {
            if (Build.VERSION.SDK_INT >= 30) {
                Insets bars = insets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                host.left += bars.left; host.top += bars.top; host.right -= bars.right; host.bottom -= bars.bottom;
            } else {
                host.left += insets.getStableInsetLeft(); host.top += insets.getStableInsetTop();
                host.right -= insets.getStableInsetRight(); host.bottom -= insets.getStableInsetBottom();
            }
        }
        assertTrue("No safe host area for outside touch: frame=" + frame + ", safe=" + safe + ", host=" + host, safe.intersect(host));
        assertTrue("Safe outside-touch area is too small: " + safe, safe.width() > 2 && safe.height() > 2);
        int middleX = clamp(frame.centerX(), safe.left + 1, safe.right - 2);
        int middleY = clamp(frame.centerY(), safe.top + 1, safe.bottom - 2);
        Point[] candidates = {
                new Point(safe.left + 1, middleY), new Point(safe.right - 2, middleY),
                new Point(middleX, safe.top + 1), new Point(middleX, safe.bottom - 2)};
        Point selected = null; long best = 0;
        for (Point point : candidates) {
            if (!safe.contains(point.x, point.y) || !strictlyOutside(expanded, point)) continue;
            long dx = point.x < expanded.left ? expanded.left - point.x : point.x > expanded.right ? point.x - expanded.right : 0;
            long dy = point.y < expanded.top ? expanded.top - point.y : point.y > expanded.bottom ? point.y - expanded.bottom : 0;
            long clearance = dx * dx + dy * dy;
            if (clearance > best) { best = clearance; selected = point; }
        }
        String geometry = "frame=" + frame + ", slop=" + slop + ", expanded=" + expanded + ", safe=" + safe + ", selected=" + selected + ", " + describeWindow(dialog);
        assertNotNull("No reachable point strictly beyond the window touch slop: " + geometry, selected);
        assertTrue("Outside point is outside the safe screen area: " + geometry, safe.contains(selected.x, selected.y));
        // Rect.right/bottom are exclusive, but Dialog's out-of-bounds test uses
        // strict > comparisons. Equality at those edges must not pass this gate.
        assertTrue("Outside point is still within the window touch slop: " + geometry, strictlyOutside(expanded, selected));
        return selected;
    }

    public static String describe(View view) {
        if (view == null) return "view=null";
        try {
            int[] screen = new int[2], window = new int[2];
            view.getLocationOnScreen(screen); view.getLocationInWindow(window);
            Rect global = new Rect(), local = new Rect(), display = new Rect();
            boolean globalVisible = view.getGlobalVisibleRect(global), localVisible = view.getLocalVisibleRect(local);
            view.getWindowVisibleDisplayFrame(display);
            return "view=" + view.getClass().getName() + ",id=0x" + Integer.toHexString(view.getId())
                    + ",attached=" + view.isAttachedToWindow() + ",shown=" + view.isShown() + ",enabled=" + view.isEnabled()
                    + ",focus=" + view.hasWindowFocus() + ",laidOut=" + view.isLaidOut() + ",layoutRequested=" + view.isLayoutRequested()
                    + ",screen=" + screen[0] + ":" + screen[1] + ",window=" + window[0] + ":" + window[1]
                    + ",size=" + view.getWidth() + "x" + view.getHeight() + ",alpha=" + view.getAlpha()
                    + ",translation=" + view.getTranslationX() + ":" + view.getTranslationY()
                    + ",globalVisible=" + globalVisible + ":" + global + ",localVisible=" + localVisible + ":" + local + ",display=" + display
                    + ",viewAnimation=" + (view.getAnimation() != null);
        } catch (RuntimeException error) { return "viewMetadataError=" + error.getClass().getName(); }
    }

    public static String describeWindow(Dialog dialog) {
        if (dialog == null) return "dialog=null";
        try {
            Window window = dialog.getWindow(); if (window == null) return "showing=" + dialog.isShowing() + ",window=null";
            WindowManager.LayoutParams params = window.getAttributes();
            return "showing=" + dialog.isShowing() + ",type=" + params.type + ",flags=" + params.flags + ",gravity=" + params.gravity
                    + ",params=" + params.width + "x" + params.height + ":" + params.x + ":" + params.y
                    + ",animations=" + params.windowAnimations + ",touchSlop=" + ViewConfiguration.get(window.getDecorView().getContext()).getScaledWindowTouchSlop()
                    + "," + describe(window.getDecorView());
        } catch (RuntimeException error) { return "windowMetadataError=" + error.getClass().getName(); }
    }

    private static Rect boundsInScreen(View view) {
        int[] screen = new int[2]; view.getLocationOnScreen(screen);
        return new Rect(screen[0], screen[1], screen[0] + view.getWidth(), screen[1] + view.getHeight());
    }
    private static boolean strictlyOutside(Rect bounds, Point point) {
        return point.x < bounds.left || point.x > bounds.right || point.y < bounds.top || point.y > bounds.bottom;
    }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
