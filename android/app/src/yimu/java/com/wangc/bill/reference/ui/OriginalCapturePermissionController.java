package com.wangc.bill.reference.ui;

import android.content.Intent;
import android.provider.Settings;
import android.widget.CompoundButton;
import androidx.fragment.app.FragmentActivity;
import com.wangc.bill.reference.OriginalSettings;
import com.wangc.bill.reference.ReferenceApplication;
import com.wangc.bill.reference.capture.AutoApps;
import com.wangc.bill.reference.capture.OriginalServiceState;
import com.wangc.bill.reference.capture.ReferenceDiagnostics;

/** Host controller for distinct recovered AutoActivity entrypoints, not its native onCreate. */
final class OriginalCapturePermissionController {
    @FunctionalInterface interface RunningProbe { boolean running(); }
    @FunctionalInterface interface NotificationRequest {
        void request(FragmentActivity host, OriginalNotificationPermission.Outcome outcome);
    }
    @FunctionalInterface interface SettingsLauncher { void launch(Intent intent); }
    @FunctionalInterface interface OemGuidance { void show(FragmentActivity host); }

    private final FragmentActivity host;
    private final RunningProbe running;
    private final NotificationRequest notifications;
    private final SettingsLauncher settings;
    private final OemGuidance oem;
    private boolean notificationPending;

    OriginalCapturePermissionController(FragmentActivity host) {
        this(host, () -> OriginalServiceState.isRunning(host), OriginalNotificationPermission::request,
                host::startActivity, OriginalOemPermissionGuidance::showForAutoStart);
    }
    OriginalCapturePermissionController(FragmentActivity host, RunningProbe running,
            NotificationRequest notifications, SettingsLauncher settings, OemGuidance oem) {
        this.host = host; this.running = running; this.notifications = notifications;
        this.settings = settings; this.oem = oem;
    }

    /** r0/f0: store the choice first; running service returns without any permission prompt. */
    void onMasterChanged(boolean enabled) {
        AutoApps.setOriginalMasterEnabled(enabled);
        if (enabled && !running.running()) missingService();
    }

    /** Source onStart uses the actual existing checked value, separately from master enable. */
    void onAutoPageStarted(CompoundButton master, CompoundButton overlays) {
        if (master.isChecked()) {
            if (running.running()) {
                if (!OverlayPermission.granted()) OverlayPermission.request(host);
                else if (!ReferenceApplication.prefs().getBoolean(OriginalSettings.AUTO_BACKGROUND_TIP_SHOWN, false)) {
                    // s.w(true) is before brand selection, even for an unmatched brand.
                    ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.AUTO_BACKGROUND_TIP_SHOWN, true).commit();
                    oem.show(host);
                }
            } else missingService();
        }
        // AutoActivity's listener is detached during actual SAW grant synchronization.
        overlays.setOnCheckedChangeListener(null);
        overlays.setChecked(OverlayPermission.granted());
        overlays.setOnCheckedChangeListener((button, checked) -> onOverlaySwitchRequested());
    }

    void onOverlaySwitchRequested() { OverlayPermission.requestDirect(host); }

    private void missingService() {
        if (!ReferenceApplication.prefs().getBoolean(OriginalSettings.NOTIFY_SHOW, OriginalSettings.DEFAULT_NOTIFY_SHOW)) {
            showAccessibilityTip(); return;
        }
        // Only the host transient bridge's outstanding request is coalesced. This
        // is not a permission grant or a permanent source suppression preference.
        if (notificationPending) return;
        notificationPending = true;
        try {
            notifications.request(host, new OriginalNotificationPermission.Outcome() {
                @Override public void granted() { continueToAccessibility(); }
                @Override public void denied() { continueToAccessibility(); }
                @Override public void unavailable(String code) {
                    notificationPending = false;
                    // Native transient dispatch is unknown; do not invent a source
                    // success/denial when this explicit host bridge loses its owner.
                    ReferenceDiagnostics.result("PERMISSION_REQUEST_UNAVAILABLE");
                }
                private void continueToAccessibility() { notificationPending = false; showAccessibilityTip(); }
            });
        } catch (RuntimeException error) { notificationPending = false; throw error; }
    }

    private void showAccessibilityTip() {
        if (host.isFinishing() || host.isDestroyed()) return;
        OriginalAutoTipDialog.create().onConfirm(() -> {
            try { settings.launch(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
            catch (Exception ignored) { settings.launch(new Intent(Settings.ACTION_SETTINGS)); }
        }).show(host.getSupportFragmentManager(), "auto_tip");
    }
}
