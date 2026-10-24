package com.wangc.bill.reference.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.wangc.bill.reference.OriginalSettings;
import com.wangc.bill.reference.ReferenceApplication;
import com.wangc.bill.reference.capture.AutoApps;
import java.util.function.Supplier;

/** Readable AutoWorkActivity.X and handlers; native onCreate and vivo_backend remain separate. */
public final class OriginalAutoWorkController implements AutoCloseable {
    static final class DeviceInfo {
        final String manufacturer, brand;
        final int sdk;
        private final Supplier<String> display;

        DeviceInfo(String manufacturer, String brand, int sdk, String displayId) {
            this(manufacturer, brand, sdk, () -> displayId);
        }
        private DeviceInfo(String manufacturer, String brand, int sdk, Supplier<String> display) {
            this.manufacturer = manufacturer;
            this.brand = brand;
            this.sdk = sdk;
            this.display = display;
        }
        static DeviceInfo current() {
            return new DeviceInfo(Build.MANUFACTURER, Build.BRAND, Build.VERSION.SDK_INT,
                    OriginalOemPermissionGuidance::sourceDisplayId);
        }
        boolean oppoFamily() {
            return "OPPO".equals(manufacturer) || "OnePlus".equals(manufacturer) || "realme".equals(manufacturer);
        }
        boolean flyme() {
            String value = OriginalOemPermissionGuidance.normalizedDisplayId(display.get());
            // Source uses the default Locale here, as does its property helper.
            return value != null && value.toUpperCase().contains("FLYME");
        }
        boolean samsung() {
            return "samsung".equalsIgnoreCase(brand) || "samsung".equalsIgnoreCase(manufacturer);
        }
    }

    @FunctionalInterface interface Launcher {
        void launch(FragmentActivity host, Intent intent);
        default Intent packageIntent(FragmentActivity host, String name) {
            return host.getPackageManager().getLaunchIntentForPackage(name);
        }
    }

    private static final String[][] PAYMENT_PROTECTION = {
        {"小米", "系统设置中搜索「支付安全检测」或者「支付保险箱」"},
        {"OPPO", "系统设置中搜索「支付保护」"},
        {"华为", "系统设置中搜索「支付保护中心」"},
        {"VIVO", "i管家→实用工具→支付保险箱"},
        {"其他", "前往系统设置或者手机管家中查找类似入口"}
    };
    private final ManagerActivity host;
    private final View root;
    private final DeviceInfo device;
    private final Launcher launcher;
    private OriginalLocalTipSheetDialog paymentSheet;

    OriginalAutoWorkController(ManagerActivity host, View root, DeviceInfo device, Launcher launcher) {
        if (host == null || root == null || device == null || launcher == null) throw new IllegalArgumentException("AutoWork dependencies are required");
        this.host = host; this.root = root; this.device = device; this.launcher = launcher;
    }

    public View root() { return root; }

    void bind() {
        configureRows();
        click("allow_all_run_background", this::applicationDetails);
        click("allow_auto_run", this::allowAutoRun);
        click("allow_run_background", () -> launcher.launch(host, requestIgnore(host)));
        click("close_low_battery", () -> launcher.launch(host, new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)));
        click("guide_layout", () -> {
            // Original o1.b explicitly opens UserGuideActivity with this URL and no
            // flags. The host route preserves its payload; online content is excluded.
            launcher.launch(host, new Intent(host, ManagerActivity.class).putExtra("page", "original_user_guide")
                    .putExtra("url", "https://www.yimuapp.com/doc/auto/keepalivemd.html"));
        });
        click("ignore_battery_optimization", this::ignoreBatteryOptimization);
        click("lock_background_layout", this::lockBackground);
        click("meizu_manager", this::applicationDetails);
        click("miui_setting", () -> showOem(OriginalOemPermissionGuidance.Kind.XIAOMI_34));
        click("pay_product", this::paymentProtection);
        click("run_manager", this::runManager);
        click("samsung_setting", () -> OriginalSamsungTipDialog.create().show(host.getSupportFragmentManager(), "auto_tip"));
        click("vivo_related", this::applicationDetails);
        required("tip_text").setOnLongClickListener(view -> {
            Toast.makeText(host, device.manufacturer, Toast.LENGTH_SHORT).show(); return true;
        });
        // No invented click for vivo_backend: it is bound/shown in readable code,
        // while any native initialization of that row has not been recovered.
    }

    private void configureRows() {
        visible("miui_setting", false); visible("samsung_setting", false);
        if (device.oppoFamily() && device.sdk >= 35) {
            visible("oppo_float_layout", true);
            View value = required("switch_oppo_float");
            if (!(value instanceof CompoundButton toggle)) throw new IllegalStateException("原版兼容模式开关缺失");
            toggle.setOnCheckedChangeListener(null);
            toggle.setChecked(ReferenceApplication.prefs().getBoolean(OriginalSettings.OPPO_FLOAT, OriginalSettings.DEFAULT_OPPO_FLOAT));
            styleSwitch(toggle);
            toggle.setOnCheckedChangeListener((button, checked) -> {
                // AutoSetting.oppoFloat -> host preference mapping; apply publishes
                // the value in memory before either effect. No fake WM/SAW adapter.
                ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.OPPO_FLOAT, checked).apply();
                OriginalOppoCompatibilityWindow window = OriginalOppoCompatibilityWindow.instance(host.getApplicationContext());
                // Original Y selects by autoBill, not by the new checked value.
                if (AutoApps.enabled()) window.show(); else window.remove();
            });
        } else visible("oppo_float_layout", false);

        if ("Xiaomi".equals(device.manufacturer)) {
            visible("miui_setting", device.sdk == 33);
            text("run_background_tip", "将后台配置改为「无限制」");
            text("ignore_battery_optimization_tip", "找到「一木记账」，设置「不优化」");
            text("auto_run_tip", "进入应用信息，打开「自启动」");
            text("lock_background_tip", "进入「后台管理」，长按「一木记账」，锁定");
            text("close_low_battery_tip", "关闭「省电模式」");
            if (device.sdk >= 35) visible("ignore_battery_optimization", false);
            return;
        }
        if ("HUAWEI".equals(device.manufacturer)) {
            visible("allow_auto_run", false); visible("ignore_battery_optimization", false);
            visible("run_manager", true); text("run_manager_title", "应用启动管理（必做）");
            text("run_background_tip", "允许「忽略电池优化」");
            text("ignore_battery_optimization_tip", "找到「一木记账」，设置「不允许」");
            text("run_manager_tip", "手机管家-应用启动管理，关闭「自动管理」，允许自启动、后台活动、关联启动");
            text("lock_background_tip", "进入「后台管理」，下滑「一木记账」，锁定");
            text("close_low_battery_tip", "关闭「省电模式」"); return;
        }
        if ("HONOR".equals(device.manufacturer)) {
            visible("allow_auto_run", false); visible("run_manager", true);
            text("run_manager_title", "应用启动管理（必做）");
            text("run_background_tip", "允许「忽略电池优化」");
            text("ignore_battery_optimization_tip", "找到「一木记账」，设置「不允许」");
            text("run_manager_tip", "系统管家-应用工具箱-应用启动管理，关闭「自动管理」，允许自启动、后台活动、关联启动");
            text("lock_background_tip", "进入「后台管理」，下滑「一木记账」，锁定");
            text("close_low_battery_tip", "关闭「省电模式」"); return;
        }
        if ("vivo".equals(device.manufacturer)) {
            text("run_background_tip", "设置「允许后台运行」"); visible("allow_run_background", false);
            text("ignore_battery_optimization_tip", "找到「一木记账」，设置「允许后台高耗电」");
            text("auto_run_tip", "找到「一木记账」，允许「自启动」");
            text("lock_background_tip", "进入「后台管理」，下滑「一木记账」，锁定");
            text("close_low_battery_tip", "关闭「省电模式」");
            visible("vivo_related", true); visible("vivo_backend", true); return;
        }
        if (device.oppoFamily()) {
            visible("allow_all_run_background", true); visible("allow_run_background", false);
            text("run_background_tip", "设置「允许后台运行」");
            text("run_all_background_tip", "「耗电管理」-「允许完全后台行为」");
            text("ignore_battery_optimization_tip", device.sdk >= 35 ? "「耗电管理」-「耗电异常优化」-「不优化」" : "「更多设置」-「耗电异常优化」-「不优化」");
            text("auto_run_tip", device.sdk >= 35 ? "「应用」-「自启动」「关联启动」" : "「耗电管理」-「允许应用自启动」「允许应用关联启动」");
            text("lock_background_tip", "进入「后台管理」，点击右上角「更多」-「锁定」");
            text("close_low_battery_tip", "关闭「省电模式」"); return;
        }
        if (device.flyme()) {
            visible("meizu_manager", true); visible("allow_auto_run", false);
            defaultText("进入「后台管理」，下滑「一木记账」，锁定"); return;
        }
        if (device.samsung()) visible("samsung_setting", true);
        defaultText("进入「后台管理」，找到「一木记账」，锁定");
    }

    private void defaultText(String lock) {
        text("run_background_tip", "设置「允许后台运行」");
        text("ignore_battery_optimization_tip", "找到「一木记账」，设置「不优化」");
        text("auto_run_tip", "找到「一木记账」，允许「自启动」");
        text("lock_background_tip", lock); text("close_low_battery_tip", "关闭「省电模式」");
    }

    private void styleSwitch(CompoundButton toggle) {
        // Existing resource preparation bridges original SwitchButton to framework
        // Switch. Its thumb colors are source-derived; skin-name selection currently
        // follows the host's night resources rather than the unported skin manager.
        boolean night = (host.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        int checked = host.getColor(Ui.res(host, "color", night ? "black" : "colorPrimary"));
        int unchecked = host.getColor(Ui.res(host, "color", "divider"));
        ColorStateList colors = new ColorStateList(new int[][]{{android.R.attr.state_checked}, {-android.R.attr.state_checked}},
                new int[]{checked, unchecked});
        if (toggle instanceof Switch platform) platform.setThumbTintList(colors);
    }

    private void allowAutoRun() {
        if (!device.oppoFamily()) { openAutostart(); return; }
        if (device.sdk >= 35) { launcher.launch(host, new Intent(Settings.ACTION_SETTINGS)); return; }
        try {
            Intent intent = new Intent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("packageName", host.getPackageName())
                    .setComponent(ComponentName.unflattenFromString("com.android.settings/.SubSettings"));
            launcher.launch(host, intent);
        } catch (Exception unavailable) { applicationDetails(); }
    }

    private void ignoreBatteryOptimization() {
        if (device.oppoFamily()) {
            if (device.sdk >= 35) applicationDetails();
            else launcher.launch(host, new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS));
            return;
        }
        launcher.launch(host, ignoreOptimization(device.sdk));
    }

    private void applicationDetails() { applicationDetails(host, launcher); }
    private static void applicationDetails(ManagerActivity host, Launcher launcher) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", host.getPackageName(), null));
        try { launcher.launch(host, intent); }
        catch (Exception unavailable) { /* Source prints; host omits exception text to protect local data. */ }
    }

    private void runManager() {
        if ("HONOR".equals(device.manufacturer)) showOem(OriginalOemPermissionGuidance.Kind.HONOR);
        else if ("HUAWEI".equals(device.manufacturer)) showOem(OriginalOemPermissionGuidance.Kind.HUAWEI);
        else openAutostart();
    }
    private void showOem(OriginalOemPermissionGuidance.Kind kind) {
        OriginalOemPermissionGuidance.showExplicit(host, kind, device.manufacturer, oemLauncher(launcher));
    }
    private void openAutostart() { OriginalOemSettingsLauncher.open(host, device.manufacturer, oemLauncher(launcher)); }
    private static OriginalOemPermissionGuidance.Launcher oemLauncher(Launcher launcher) {
        return new OriginalOemPermissionGuidance.Launcher() {
            @Override public void launch(FragmentActivity host, Intent intent) { launcher.launch(host, intent); }
            @Override public Intent packageIntent(FragmentActivity host, String name) { return launcher.packageIntent(host, name); }
        };
    }
    private void lockBackground() {
        String image = "OPPO".equals(device.manufacturer) ? "lock_background_oppo"
                : "Xiaomi".equals(device.manufacturer) ? "lock_background_xiaomi" : "lock_background_vivo";
        int id = Ui.res(host, "mipmap", image);
        if (id == 0) throw new IllegalStateException("原版后台锁定图片缺失：" + image);
        OriginalImageTipDialog.create(id).show(host.getSupportFragmentManager(), "tip");
    }
    private void paymentProtection() {
        if (paymentSheet != null) paymentSheet.dismiss();
        paymentSheet = new OriginalLocalTipSheetDialog(host, PAYMENT_PROTECTION);
        paymentSheet.show();
    }
    @Override public void close() { if (paymentSheet != null) { paymentSheet.dismiss(); paymentSheet = null; } }

    private View required(String name) { return OriginalAutoWorkScreen.required(root, name); }
    private void click(String name, Runnable action) { required(name).setOnClickListener(view -> action.run()); }
    private void visible(String name, boolean visible) { required(name).setVisibility(visible ? View.VISIBLE : View.GONE); }
    private void text(String name, String value) { ((TextView) required(name)).setText(value); }

    static Intent requestIgnore(ManagerActivity host) {
        return new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                .setData(Uri.parse("package:" + host.getPackageName()));
    }
    static Intent ignoreOptimization(int sdk) {
        if (sdk >= 35) return new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        return new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(Intent.CATEGORY_DEFAULT).setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                .setComponent(ComponentName.unflattenFromString("com.android.settings/.Settings$HighPowerApplicationsActivity"));
    }

    static void showCaptureBackgroundSettings(ManagerActivity host, DeviceInfo device, Launcher launcher) {
        if ("Xiaomi".equals(device.manufacturer)) { launcher.launch(host, requestIgnore(host)); return; }
        if (device.oppoFamily()) {
            // Source CommonDialog's window contract must be restored independently;
            // this class is supplied alongside this controller, not PermissionTipDialog.
            OriginalCaptureBackgroundDialog.create(() -> applicationDetails(host, launcher))
                    .show(host.getSupportFragmentManager(), "tip"); return;
        }
        if ("vivo".equals(device.manufacturer)) { launcher.launch(host, ignoreOptimization(device.sdk)); return; }
        if ("HONOR".equals(device.manufacturer)) {
            OriginalOemPermissionGuidance.showExplicit(host, OriginalOemPermissionGuidance.Kind.HONOR,
                    device.manufacturer, oemLauncher(launcher)); return;
        }
        if ("HUAWEI".equals(device.manufacturer)) {
            OriginalOemPermissionGuidance.showExplicit(host, OriginalOemPermissionGuidance.Kind.HUAWEI,
                    device.manufacturer, oemLauncher(launcher)); return;
        }
        // AutoActivity allowRunBack checks Samsung before Flyme; X checks Flyme first.
        if (device.samsung()) OriginalSamsungTipDialog.create().show(host.getSupportFragmentManager(), "auto_tip");
        else if (device.flyme()) OriginalOemPermissionGuidance.showExplicit(host,
                OriginalOemPermissionGuidance.Kind.MEIZU, device.manufacturer, oemLauncher(launcher));
        else if (host.template != null) OriginalAutoWorkScreen.required(host.template, "allow_run_back").setVisibility(View.GONE);
    }
}
