package com.wangc.bill.reference.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/** AutoActivity.onStart's OEM branch; the caller persists the one-time marker before entering. */
public final class OriginalOemPermissionGuidance {
    private OriginalOemPermissionGuidance() {}
    enum Kind { NONE, HUAWEI, HONOR, MEIZU, XIAOMI_SIMPLE, XIAOMI_34 }
    @FunctionalInterface interface Launcher {
        void launch(FragmentActivity host, Intent intent);
        default Intent packageIntent(FragmentActivity host, String name) {
            return host.getPackageManager().getLaunchIntentForPackage(name);
        }
    }

    public static void showForAutoStart(FragmentActivity host) {
        show(host, Build.MANUFACTURER, Build.VERSION.SDK_INT,
                OriginalOemPermissionGuidance::displayId, FragmentActivity::startActivity);
    }

    static Kind select(String manufacturer, int sdk, Supplier<String> displayId) {
        // Original o.b is Build.MANUFACTURER, with these exact case-sensitive gates.
        if ("HUAWEI".equals(manufacturer)) return Kind.HUAWEI;
        if ("Xiaomi".equals(manufacturer)) return sdk == 34 ? Kind.XIAOMI_34 : Kind.XIAOMI_SIMPLE;
        if ("HONOR".equals(manufacturer)) return Kind.HONOR;
        String display = displayId.get();
        return display != null && !display.isEmpty() && display.toUpperCase().contains("FLYME") ? Kind.MEIZU : Kind.NONE;
    }

    static GuidanceDialog show(FragmentActivity host, String manufacturer, int sdk,
            Supplier<String> displayId, Launcher launcher) {
        Kind kind = select(manufacturer, sdk, displayId);
        if (kind == Kind.NONE) return null;
        GuidanceDialog dialog = new GuidanceDialog(); Bundle args = new Bundle();
        args.putString("kind", kind.name()); args.putString("manufacturer", manufacturer);
        dialog.setArguments(args); dialog.launcher = launcher;
        dialog.show(host.getSupportFragmentManager(), "auto_tip"); return dialog;
    }

    /** Fixed, read-only platform ROM query from floatPermission.c.c/b.i; no arbitrary inputs. */
    private static String displayId() {
        Method getter = PropertyGetter.GET;
        if (getter != null) {
            try { return normalizedDisplayId((String) getter.invoke(null, "ro.build.display.id")); }
            catch (Exception ignored) {}
        }
        return null;
    }
    static String normalizedDisplayId(String value) {
        String trimmed = value == null ? null : value.trim();
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }
    private static final class PropertyGetter {
        // Source c's cached Method is initialized only when the property helper is first used.
        static final Method GET = findDisplayGetter();
    }
    @SuppressLint("PrivateApi") // Source ROM detection: only SystemProperties.get of the fixed display ID.
    private static Method findDisplayGetter() {
        Class<?> platform;
        try {
            try { platform = Class.forName("android.os.SystemProperties"); }
            catch (ClassNotFoundException first) {
                platform = ClassLoader.getSystemClassLoader().loadClass("android.os.SystemProperties");
            }
        } catch (ClassNotFoundException missing) { return null; }
        try { return platform.getMethod("get", String.class); }
        catch (Exception unavailable) { return null; }
    }

    /** Public no-arg Fragment for the ordinary FragmentManager restoration path. */
    public static final class GuidanceDialog extends DialogFragment {
        private Launcher launcher = FragmentActivity::startActivity;
        private Kind kind() { return Kind.valueOf(requireArguments().getString("kind")); }
        @Override public void onCreate(Bundle state) {
            super.onCreate(state); setStyle(STYLE_NO_TITLE, Ui.res(requireContext(), "style", "AlertDialogStyle"));
        }
        @Override public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
            String layout = switch (kind()) {
                case HUAWEI -> "dialog_huawei_tip"; case HONOR -> "dialog_honor_tip";
                case MEIZU -> "dialog_meizu_tip"; case XIAOMI_SIMPLE -> "dialog_xiaomi_tip";
                case XIAOMI_34 -> "dialog_miui_tip"; default -> throw new IllegalStateException("No OEM dialog selected");
            };
            View view = inflater.inflate(Ui.res(requireContext(), "layout", layout), parent, false);
            Ui.click(view, "cancel", this::dismiss); Ui.click(view, "confirm", this::confirm); return view;
        }
        private void confirm() {
            dismiss();
            switch (kind()) {
                case HUAWEI, HONOR -> openWhitelist(requireActivity(), requireArguments().getString("manufacturer"), launcher);
                case XIAOMI_SIMPLE -> {
                    FragmentActivity host = requireActivity();
                    Intent intent = new Intent("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
                    intent.setData(Uri.parse("package:" + host.getPackageName()));
                    // Original MiuiSimpleTipDialog has no flags and no catch/fallback.
                    launcher.launch(host, intent);
                }
                // AutoActivity's SDK34 Miui callback s0 is empty; Meizu has no callback.
                case XIAOMI_34, MEIZU -> { }
                default -> throw new IllegalStateException("No OEM dialog selected");
            }
        }
        @Override public void onResume() {
            Dialog dialog = requireDialog(); WindowManager.LayoutParams size = dialog.getWindow().getAttributes();
            size.width = OriginalAutoTipDialog.screenWidth(requireContext()) - Ui.dp(requireContext(), 40);
            size.height = ViewGroup.LayoutParams.WRAP_CONTENT; dialog.getWindow().setAttributes(size);
            dialog.setCancelable(true); dialog.setCanceledOnTouchOutside(true); super.onResume();
        }
    }

    private static void openWhitelist(FragmentActivity host, String manufacturer, Launcher launcher) {
        // t1.a's other manufacturers are unreachable through this AutoActivity branch.
        String[] entries = "HUAWEI".equalsIgnoreCase(manufacturer) ? new String[]{
                "com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity",
                "com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity",
                "com.huawei.systemmanager/.optimize.process.ProtectActivity",
                "com.huawei.systemmanager/.optimize.bootstart.BootStartActivity", "com.huawei.systemmanager"}
                : new String[]{"com.hihonor.systemmanager/com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity", "com.hihonor.systemmanager"};
        for (String entry : entries) {
            try {
                Intent intent;
                if (entry.contains("/")) {
                    intent = new Intent().putExtra("packageName", host.getPackageName()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .setComponent(ComponentName.unflattenFromString(entry));
                } else intent = launcher.packageIntent(host, entry);
                launcher.launch(host, intent); break;
            } catch (Exception ignored) { /* Source tries the next entry; no grant or generic fallback is invented. */ }
        }
    }
}
