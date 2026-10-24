package com.wangc.bill.reference.ui;

import android.content.ComponentName;
import android.content.Intent;
import androidx.fragment.app.FragmentActivity;

/** Original t1.a's local manufacturer table and exact candidate fall-through. */
final class OriginalOemSettingsLauncher {
    private OriginalOemSettingsLauncher() {}
    private static final String[][] PROFILES = {
        {"Xiaomi", "com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity", "com.miui.securitycenter"},
        {"samsung", "com.samsung.android.sm_cn/com.samsung.android.sm.ui.ram.AutoRunActivity", "com.samsung.android.sm_cn/com.samsung.android.sm.ui.appmanagement.AppManagementActivity", "com.samsung.android.sm_cn/com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity", "com.samsung.android.sm_cn/.ui.ram.RamActivity", "com.samsung.android.sm_cn/.app.dashboard.SmartManagerDashBoardActivity", "com.samsung.android.sm/com.samsung.android.sm.ui.ram.AutoRunActivity", "com.samsung.android.sm/com.samsung.android.sm.ui.appmanagement.AppManagementActivity", "com.samsung.android.sm/com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity", "com.samsung.android.sm/.ui.ram.RamActivity", "com.samsung.android.sm/.app.dashboard.SmartManagerDashBoardActivity", "com.samsung.android.lool/com.samsung.android.sm.ui.battery.BatteryActivity", "com.samsung.android.sm_cn", "com.samsung.android.sm"},
        {"HUAWEI", "com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity", "com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity", "com.huawei.systemmanager/.optimize.process.ProtectActivity", "com.huawei.systemmanager/.optimize.bootstart.BootStartActivity", "com.huawei.systemmanager"},
        {"vivo", "com.iqoo.secure/.ui.phoneoptimize.BgStartUpManager", "com.iqoo.secure/.safeguard.PurviewTabActivity", "com.vivo.permissionmanager/.activity.BgStartUpManagerActivity", "com.iqoo.secure", "com.vivo.permissionmanager"},
        {"Meizu", "com.meizu.safe/.permission.SmartBGActivity", "com.meizu.safe/.permission.PermissionMainActivity", "com.meizu.safe"},
        {"OPPO", "com.oplus.battery/com.oplus.startupapp.view.StartupAppListActivity", "com.coloros.safecenter/.startupapp.StartupAppListActivity", "com.coloros.safecenter/.permission.startup.StartupAppListActivity", "com.oppo.safe/.permission.startup.StartupAppListActivity", "com.coloros.oppoguardelf/com.coloros.powermanager.fuelgaue.PowerUsageModelActivity", "com.coloros.safecenter/com.coloros.privacypermissionsentry.PermissionTopActivity", "com.coloros.safecenter", "com.oppo.safe", "com.coloros.oppoguardelf"},
        {"oneplus", "com.oneplus.security/.chainlaunch.view.ChainLaunchAppListActivity", "com.oneplus.security"},
        {"letv", "com.letv.android.letvsafe/.AutobootManageActivity", "com.letv.android.letvsafe/.BackgroundAppManageActivity", "com.letv.android.letvsafe"},
        {"zte", "com.zte.heartyservice/.autorun.AppAutoRunManager", "com.zte.heartyservice"},
        {"F", "com.gionee.softmanager/.MainActivity", "com.gionee.softmanager"},
        {"smartisanos", "com.smartisanos.security/.invokeHistory.InvokeHistoryActivity", "com.smartisanos.security"},
        {"360", "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity", "com.yulong.android.coolsafe"},
        {"ulong", "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity", "com.yulong.android.coolsafe"},
        {"coolpad", "com.yulong.android.security/com.yulong.android.seccenter.tabbarmain", "com.yulong.android.security"},
        {"lenovo", "com.lenovo.security/.purebackground.PureBackgroundActivity", "com.lenovo.security"},
        {"htc", "com.htc.pitroad/.landingpage.activity.LandingPageActivity", "com.htc.pitroad"},
        {"asus", "com.asus.mobilemanager/.MainActivity", "com.asus.mobilemanager"},
        {"YuLong", "com.yulong.android.softmanager/.SpeedupActivity", "com.yulong.android.security/com.yulong.android.seccenter.tabbarmain", "com.yulong.android.security"},
        {"HONOR", "com.hihonor.systemmanager/com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity", "com.hihonor.systemmanager"}
    };

    static void open(FragmentActivity host, String manufacturer, OriginalOemPermissionGuidance.Launcher launcher) {
        // Only one case-insensitive key can match, so replacing HashMap iteration
        // with this literal table does not change the source's per-device order.
        for (String[] profile : PROFILES) {
            if (!profile[0].equalsIgnoreCase(manufacturer)) continue;
            for (int index = 1; index < profile.length; index++) {
                String entry = profile[index];
                try {
                    Intent intent;
                    if (entry.contains("/")) {
                        intent = new Intent().putExtra("packageName", host.getPackageName())
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .setComponent(ComponentName.unflattenFromString(entry));
                    } else {
                        // Null, flags, and package-launch payload are forwarded
                        // unchanged. Source catches startActivity(null), then tries on.
                        intent = launcher.packageIntent(host, entry);
                    }
                    launcher.launch(host, intent);
                    break;
                } catch (Exception unavailable) {
                    // Original tries the next candidate; Error still propagates.
                    // Host omits the original exception/phone-model log.
                }
            }
        }
        // No matched profile or all attempts failed: source has no generic fallback.
    }
}
