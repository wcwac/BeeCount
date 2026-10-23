package com.wangc.bill.reference.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import com.wangc.bill.reference.ReferenceApplication;

/** Original application-overlay permission, requested only from an explicit UI action. */
public final class OverlayPermission {
    private OverlayPermission() {}
    public static boolean granted() {
        try { return Settings.canDrawOverlays(ReferenceApplication.app()); }
        catch (RuntimeException ignored) { return false; }
    }
    public static void request(Activity activity) {
        try {
            activity.startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:"+activity.getPackageName())));
        } catch (RuntimeException ignored) {
            Ui.toast(activity,"无法打开悬浮窗授权，请在系统设置中管理本应用权限");
        }
    }
}
