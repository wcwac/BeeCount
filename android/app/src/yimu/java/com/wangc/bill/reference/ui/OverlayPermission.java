package com.wangc.bill.reference.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.fragment.app.FragmentActivity;
import com.wangc.bill.reference.ReferenceApplication;

/** Original application-overlay permission, requested only from an explicit UI action. */
public final class OverlayPermission {
    private OverlayPermission() {}
    public static boolean granted() {
        return Settings.canDrawOverlays(ReferenceApplication.app());
    }
    public static void request(Activity activity) {
        request(activity, Activity::startActivity);
    }
    /** AutoActivity.t0 calls a.b directly, including when SAW is already granted. */
    public static void requestDirect(Activity activity) {
        requestDirect(activity, Activity::startActivity);
    }
    @FunctionalInterface interface SettingsLauncher { void launch(Activity activity, Intent intent); }
    static void requestDirect(Activity activity, SettingsLauncher launcher) {
        openSettings(activity, launcher);
    }
    /** The injected boundary observes the actual system Intent without changing permission state. */
    static void request(Activity activity, SettingsLauncher launcher) {
        if (granted()) return;
        if (!(activity instanceof FragmentActivity host))
            throw new IllegalArgumentException("原版权限提示需要 FragmentActivity");
        OriginalPermissionTipDialog.create("提示",
                "为了能使自动记账、悬浮球记账、流水对账窗口能正常弹出，需要您手动授予悬浮窗权限？",
                "去授权", "取消")
                .callbacks(new OriginalPermissionTipDialog.Callback() {
                    @Override public void confirm() { openSettings(activity, launcher); }
                    @Override public void cancel() { }
                }).show(host.getSupportFragmentManager(), "tip");
    }
    private static void openSettings(Activity activity, SettingsLauncher launcher) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launcher.launch(activity, intent);
        } catch (Exception ignored) {
            // Original floatPermission.a.b silently catches Exception, not Error.
        }
    }
}
