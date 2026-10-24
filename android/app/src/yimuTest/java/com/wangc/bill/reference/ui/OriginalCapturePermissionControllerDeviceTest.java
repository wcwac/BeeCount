package com.wangc.bill.reference.ui;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.wangc.bill.reference.OriginalSettings;
import com.wangc.bill.reference.ReferenceApplication;
import com.wangc.bill.reference.capture.AutoApps;
import com.wangc.bill.reference.capture.OriginalServiceState;
import com.wangc.bill.reference.capture.ReferenceCaptureService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * The first eight cases are controller truth tables with actual guidance views,
 * not Manager lifecycle E2E tests or OS notification-grant tests. The remaining
 * cases exercise real Manager controls and Activity start/stop entrypoints.
 */
@RunWith(AndroidJUnit4.class)
public final class OriginalCapturePermissionControllerDeviceTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private UiAutomation automation;
    private String[] priorPackages;
    private int priorObserverFlags;
    private String priorServices, priorAccessibility, priorOverlay;
    private Map<String, ?> preferences;
    private ActivityScenario<ManagerActivity> scenario;
    private final AtomicReference<DialogFragment> tip = new AtomicReference<>();
    private final AtomicReference<Dialog> dialog = new AtomicReference<>();

    @Before public void open() throws Exception {
        assertEquals("true", InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        assertEquals(36, Build.VERSION.SDK_INT);
        assertTrue(Build.HARDWARE.equals("ranchu") || Build.HARDWARE.equals("goldfish"));
        assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        automation = instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        AccessibilityServiceInfo observer = automation.getServiceInfo(); priorPackages = observer.packageNames == null ? null : observer.packageNames.clone();
        priorObserverFlags = observer.flags; observer.packageNames = new String[]{ReferenceApplication.app().getPackageName()};
        observer.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        automation.setServiceInfo(observer);
        preferences = new HashMap<>(ReferenceApplication.prefs().getAll());
        priorServices = shell("settings get secure enabled_accessibility_services").trim();
        priorAccessibility = shell("settings get secure accessibility_enabled").trim(); priorOverlay = overlayMode();
        // Fixture shutdown remains the host filter, never the source user-OFF action.
        AutoApps.enable(false); instrumentation.runOnMainSync(() -> {});
        ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.NOTIFY_SHOW, false)
                .putBoolean(OriginalSettings.AUTO_BACKGROUND_TIP_SHOWN, true).commit();
    }

    @After public void close() throws Exception {
        if (preferences == null) return;
        try {
            if (scenario != null) scenario.close(); AutoApps.enable(false); instrumentation.runOnMainSync(() -> {});
            SharedPreferences.Editor editor = ReferenceApplication.prefs().edit().clear();
            for (Map.Entry<String, ?> entry : preferences.entrySet()) {
                String key = entry.getKey(); Object value = entry.getValue();
                if (value instanceof Boolean b) editor.putBoolean(key, b); else if (value instanceof Long l) editor.putLong(key, l);
                else if (value instanceof Integer i) editor.putInt(key, i); else if (value instanceof Float f) editor.putFloat(key, f);
                else if (value instanceof String s) editor.putString(key, s);
                else if (value instanceof Set<?> values) { Set<String> strings = new HashSet<>(); for (Object item : values) strings.add((String) item); editor.putStringSet(key, strings); }
            }
            assertTrue(editor.commit());
        } finally {
            try {
                if (priorServices != null) shell(priorServices.equals("null") ? "settings delete secure enabled_accessibility_services"
                        : "settings put secure enabled_accessibility_services " + token(priorServices));
            } finally {
                try {
                    if (priorAccessibility != null) shell(priorAccessibility.equals("null") ? "settings delete secure accessibility_enabled"
                            : "settings put secure accessibility_enabled " + token(priorAccessibility));
                    ReferenceCaptureService.refresh(); instrumentation.runOnMainSync(() -> {});
                } finally {
                    try {
                        if (priorOverlay != null) shell("appops set --user current " + token(packageName()) + " SYSTEM_ALERT_WINDOW " + token(priorOverlay));
                    } finally {
                        AccessibilityServiceInfo observer = automation.getServiceInfo(); observer.packageNames = priorPackages; observer.flags = priorObserverFlags;
                        automation.setServiceInfo(observer);
                    }
                }
            }
        }
    }

    @Test public void aDisabledActualCapturePageSkipsTheRunningProbeAndPromptsButSynchronizesSawsWithItsListenerDetached() throws Exception {
        saw(false); launch("capture"); AtomicInteger oldListener = new AtomicInteger(), probes = new AtomicInteger(), notifications = new AtomicInteger(), launches = new AtomicInteger(), oem = new AtomicInteger();
        for (boolean granted : new boolean[]{false, true}) {
            saw(granted);
            scenario.onActivity(host -> {
                CompoundButton master = control(host, "switch_auto_bill"), overlay = control(host, "switch_overlays"); assertFalse(master.isChecked());
                overlay.setOnCheckedChangeListener(null); overlay.setChecked(!granted);
                overlay.setOnCheckedChangeListener((button, value) -> oldListener.incrementAndGet());
                OriginalCapturePermissionController controller = new OriginalCapturePermissionController(host,
                        () -> { probes.incrementAndGet(); return false; }, (owner, outcome) -> notifications.incrementAndGet(),
                        intent -> launches.incrementAndGet(), owner -> oem.incrementAndGet());
                controller.onAutoPageStarted(master, overlay); assertEquals(granted, overlay.isChecked());
                assertNull(host.getSupportFragmentManager().findFragmentByTag("tip")); assertNull(host.getSupportFragmentManager().findFragmentByTag("auto_tip"));
            });
        }
        assertEquals(0, probes.get()); assertEquals(0, notifications.get()); assertEquals(0, launches.get()); assertEquals(0, oem.get()); assertEquals(0, oldListener.get()); assertFalse(AutoApps.enabled());
    }

    @Test public void enablingWithARunningDelegateReturnsImmediatelyEvenWhenActualSawsIsDenied() throws Exception {
        saw(false); launch("settings"); AtomicInteger probes = new AtomicInteger(), notifications = new AtomicInteger(), launches = new AtomicInteger(), oem = new AtomicInteger();
        ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.NOTIFY_SHOW, true).commit();
        scenario.onActivity(host -> {
            OriginalCapturePermissionController controller = new OriginalCapturePermissionController(host,
                    () -> { assertTrue("f0 persists before checking running", AutoApps.enabled()); probes.incrementAndGet(); return true; },
                    (owner, outcome) -> notifications.incrementAndGet(), intent -> launches.incrementAndGet(), owner -> oem.incrementAndGet());
            controller.onMasterChanged(true);
            assertNull(host.getSupportFragmentManager().findFragmentByTag("tip")); assertNull(host.getSupportFragmentManager().findFragmentByTag("auto_tip"));
        });
        instrumentation.waitForIdleSync(); assertTrue(AutoApps.enabled()); assertFalse(OverlayPermission.granted());
        assertEquals(1, probes.get()); assertEquals(0, notifications.get()); assertEquals(0, launches.get()); assertEquals(0, oem.get());
    }

    @Test public void missingServiceWithoutNotificationUsesTheActualAutoTipThenTheNewTaskAccessibilityIntent() throws Exception {
        saw(false); launch("settings"); AtomicInteger notifications = new AtomicInteger(); List<Intent> intents = new ArrayList<>(); AtomicBoolean dismissed = new AtomicBoolean();
        scenario.onActivity(host -> new OriginalCapturePermissionController(host, () -> { assertTrue(AutoApps.enabled()); return false; },
                (owner, outcome) -> notifications.incrementAndGet(), intent -> { dismissed.set(!dialog.get().isShowing()); intents.add(new Intent(intent)); }, owner -> fail("Missing service must not reach OEM")).onMasterChanged(true));
        shown(OriginalAutoTipDialog.class, "auto_tip"); assertEquals(0, notifications.get()); assertTrue(intents.isEmpty());
        tapTip("confirm"); hidden(); assertTrue(dismissed.get()); assertEquals(1, intents.size());
        assertIntent(intents.get(0), Settings.ACTION_ACCESSIBILITY_SETTINGS, Intent.FLAG_ACTIVITY_NEW_TASK); assertTrue(AutoApps.enabled()); assertFalse(OverlayPermission.granted());
    }

    @Test public void accessibilityLaunchExceptionFallsBackToGeneralSettingsWithoutNewTaskAfterDismissal() throws Exception {
        launch("settings"); List<Intent> intents = new ArrayList<>(); AtomicBoolean dismissed = new AtomicBoolean(true);
        scenario.onActivity(host -> new OriginalCapturePermissionController(host, () -> false,
                (owner, outcome) -> fail("Notification option is disabled"), intent -> {
                    dismissed.set(dismissed.get() && !dialog.get().isShowing()); intents.add(new Intent(intent));
                    if (intents.size() == 1) throw new IllegalStateException("Synthetic unavailable accessibility settings");
                }, owner -> fail("Missing service must not reach OEM")).onMasterChanged(true));
        shown(OriginalAutoTipDialog.class, "auto_tip"); tapTip("confirm"); hidden();
        assertTrue(dismissed.get()); assertEquals(2, intents.size());
        assertIntent(intents.get(0), Settings.ACTION_ACCESSIBILITY_SETTINGS, Intent.FLAG_ACTIVITY_NEW_TASK); assertIntent(intents.get(1), Settings.ACTION_SETTINGS, 0);
    }

    @Test public void bothControlledNotificationOutcomesContinueToTheRealAutoTipWithoutPretendingToGrantSaws() throws Exception {
        saw(false); launch("settings"); ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.NOTIFY_SHOW, true).commit();
        AtomicInteger notifications = new AtomicInteger(); AtomicReference<OriginalNotificationPermission.Outcome> callback = new AtomicReference<>();
        AtomicReference<OriginalCapturePermissionController> controller = new AtomicReference<>();
        scenario.onActivity(host -> controller.set(new OriginalCapturePermissionController(host, () -> false,
                (owner, outcome) -> { notifications.incrementAndGet(); callback.set(outcome); }, intent -> fail("Cancel must not navigate"), owner -> fail("Missing service must not reach OEM"))));
        for (boolean grant : new boolean[]{true, false}) {
            scenario.onActivity(host -> {
                controller.get().onMasterChanged(true); assertNotNull(callback.get());
                assertNull(host.getSupportFragmentManager().findFragmentByTag("auto_tip"));
                if (grant) callback.get().granted(); else callback.get().denied();
            });
            shown(OriginalAutoTipDialog.class, "auto_tip"); tapTip("cancel"); hidden(); clearTip(); assertFalse(OverlayPermission.granted()); assertTrue(AutoApps.enabled());
        }
        assertEquals(2, notifications.get());
    }

    @Test public void aRunningPageWithDeniedSawsShowsTheActualPermissionTipBeforeAnyOemOrNotificationBranch() throws Exception {
        saw(false); launch("capture"); ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.AUTO_BACKGROUND_TIP_SHOWN, false).commit();
        AtomicInteger notifications = new AtomicInteger(), oem = new AtomicInteger();
        scenario.onActivity(host -> {
            OriginalCapturePermissionController controller = new OriginalCapturePermissionController(host, () -> true,
                    (owner, outcome) -> notifications.incrementAndGet(), intent -> fail("Permission tip needs a real explicit click first"), owner -> oem.incrementAndGet());
            startChecked(host, controller); assertFalse(control(host, "switch_overlays").isChecked());
        });
        shown(OriginalPermissionTipDialog.class, "tip"); assertEquals(0, notifications.get()); assertEquals(0, oem.get());
        assertFalse(ReferenceApplication.prefs().getBoolean(OriginalSettings.AUTO_BACKGROUND_TIP_SHOWN, true));
        tapTip("cancel"); hidden(); assertTrue(AutoApps.enabled());
    }

    @Test public void aGrantedFirstStartStoresTheMarkerBeforeOemSelectionAndNeverRepeatsItOnTheNextStart() throws Exception {
        saw(true); launch("capture"); ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.AUTO_BACKGROUND_TIP_SHOWN, false).commit();
        AtomicInteger probes = new AtomicInteger(), oem = new AtomicInteger();
        scenario.onActivity(host -> {
            OriginalCapturePermissionController controller = new OriginalCapturePermissionController(host, () -> { probes.incrementAndGet(); return true; },
                    (owner, outcome) -> fail("Running service must not request notification"), intent -> fail("Programmatic SAW synchronization must not navigate"), owner -> {
                        assertTrue(ReferenceApplication.prefs().getBoolean(OriginalSettings.AUTO_BACKGROUND_TIP_SHOWN, false)); oem.incrementAndGet();
                    });
            startChecked(host, controller); controller.onAutoPageStarted(control(host, "switch_auto_bill"), control(host, "switch_overlays"));
            assertTrue(control(host, "switch_overlays").isChecked());
        });
        assertEquals(2, probes.get()); assertEquals(1, oem.get());
    }

    @Test public void missingServiceOnStartTakesPrecedenceOverDeniedSawsAndIgnoresBothAutoJumpValues() throws Exception {
        saw(false); launch("capture"); AtomicInteger probes = new AtomicInteger(), oem = new AtomicInteger();
        for (boolean autoJump : new boolean[]{false, true}) {
            ReferenceApplication.prefs().edit().putBoolean("accessibility_shortcut", autoJump).commit();
            scenario.onActivity(host -> {
                OriginalCapturePermissionController controller = new OriginalCapturePermissionController(host, () -> { probes.incrementAndGet(); return false; },
                        (owner, outcome) -> fail("Notification option is off"), intent -> fail("Cancel must not navigate"), owner -> oem.incrementAndGet());
                startChecked(host, controller); assertNull(host.getSupportFragmentManager().findFragmentByTag("tip"));
            });
            shown(OriginalAutoTipDialog.class, "auto_tip"); tapTip("cancel"); hidden(); clearTip();
            assertEquals(autoJump, ReferenceApplication.prefs().getBoolean("accessibility_shortcut", !autoJump));
        }
        assertEquals(2, probes.get()); assertEquals(0, oem.get());
    }

    @Test public void theActualSawsSwitchKeepsItsToggledValueAndViewUntilARealStopStartSynchronizesGrantAndRevoke() throws Exception {
        saw(false); launch("capture"); AtomicReference<View> original = new AtomicReference<>();
        scenario.onActivity(host -> { original.set(control(host, "switch_overlays")); assertFalse(((CompoundButton) original.get()).isChecked()); });
        IntentFilter filter = new IntentFilter(Settings.ACTION_MANAGE_OVERLAY_PERMISSION); filter.addDataScheme("package");
        Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor(filter, new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null), true);
        instrumentation.addMonitor(monitor);
        try {
            tapMain("switch_overlays"); await(() -> monitor.getHits() == 1, "SAW switch did not launch the actual settings action");
            sameOverlayControl(original.get(), true); assertFalse(OverlayPermission.granted());
            scenario.moveToState(Lifecycle.State.CREATED); scenario.moveToState(Lifecycle.State.RESUMED);
            sameOverlayControl(original.get(), false); assertEquals(1, monitor.getHits());
            scenario.moveToState(Lifecycle.State.CREATED); saw(true); scenario.moveToState(Lifecycle.State.RESUMED);
            sameOverlayControl(original.get(), true); assertEquals(1, monitor.getHits());
            tapMain("switch_overlays"); await(() -> monitor.getHits() == 2, "Granted SAW switch must still open its direct settings action");
            sameOverlayControl(original.get(), false); assertTrue(OverlayPermission.granted());
            scenario.moveToState(Lifecycle.State.CREATED); scenario.moveToState(Lifecycle.State.RESUMED);
            sameOverlayControl(original.get(), true); assertEquals(2, monitor.getHits());
            scenario.moveToState(Lifecycle.State.CREATED); saw(false); scenario.moveToState(Lifecycle.State.RESUMED);
            sameOverlayControl(original.get(), false); assertEquals(2, monitor.getHits()); assertFalse(AutoApps.enabled());
        } finally { instrumentation.removeMonitor(monitor); }
    }

    @Test public void theActualDiagnosticMasterOffDisablesTheServiceAndOnShowsAutoTipWithoutReauthorizingIt() throws Exception {
        saw(true); ReferenceApplication.prefs().edit().putStringSet("enabled_packages", new HashSet<>(Collections.singletonList("com.tencent.mm"))).commit();
        AutoApps.enable(true); shell("settings put secure enabled_accessibility_services " + token(component())); shell("settings put secure accessibility_enabled 1");
        await(() -> ReferenceCaptureService.current() != null && ReferenceCaptureService.status().optBoolean("permissionGranted") && OriginalServiceState.isRunning(ReferenceApplication.app()), "Own service did not connect for the real master control");
        launch("diagnostics"); tapMain("switch_auto_bill");
        await(() -> !AutoApps.enabled() && ReferenceCaptureService.current() == null && !ReferenceCaptureService.status().optBoolean("permissionGranted")
                && !OriginalServiceState.isRunning(ReferenceApplication.app()), "UI OFF did not request actual disableSelf");
        assertFalse(ownComponentEnabled()); tapMain("switch_auto_bill"); shown(OriginalAutoTipDialog.class, "auto_tip");
        assertTrue(AutoApps.enabled()); assertNull(ReferenceCaptureService.current()); assertFalse(ReferenceCaptureService.status().optBoolean("permissionGranted"));
        assertFalse(ownComponentEnabled()); tapTip("cancel"); hidden(); assertTrue(AutoApps.enabled());
    }

    @Test public void anEnabledActualCapturePageWithNoRunningServiceShowsAutoTipAgainAfterARealStopStart() throws Exception {
        saw(false); authorizeOwnService(false);
        await(() -> ReferenceCaptureService.current() == null && !ReferenceCaptureService.status().optBoolean("permissionGranted")
                && !OriginalServiceState.isRunning(ReferenceApplication.app()), "Own service remained running after removing only its authorization");
        AutoApps.enable(true); launch("capture");
        shown(OriginalAutoTipDialog.class, "auto_tip");
        scenario.onActivity(host -> {
            assertTrue(control(host, "switch_auto_bill").isChecked()); assertFalse(control(host, "switch_overlays").isChecked());
            assertNull("Missing service must precede the denied-SAW branch", host.getSupportFragmentManager().findFragmentByTag("tip"));
        });
        assertTrue(AutoApps.enabled()); assertFalse(ownComponentEnabled()); assertFalse(OverlayPermission.granted());
        tapTip("cancel"); hidden(); clearTip();
        scenario.moveToState(Lifecycle.State.CREATED); scenario.moveToState(Lifecycle.State.RESUMED);
        shown(OriginalAutoTipDialog.class, "auto_tip");
        scenario.onActivity(host -> {
            assertTrue(control(host, "switch_auto_bill").isChecked()); assertFalse(control(host, "switch_overlays").isChecked());
            assertNull(host.getSupportFragmentManager().findFragmentByTag("tip"));
        });
        assertTrue(AutoApps.enabled()); assertFalse(ownComponentEnabled()); assertNull(ReferenceCaptureService.current());
        assertFalse(OriginalServiceState.isRunning(ReferenceApplication.app())); assertFalse(OverlayPermission.granted());
        tapTip("cancel"); hidden();
    }

    @Test public void aRunningActualCapturePageShowsSawsTipThenPersistsTheFirstStartMarkerAfterRealGrantAndRestart() throws Exception {
        saw(false); ReferenceApplication.prefs().edit()
                .putStringSet("enabled_packages", new HashSet<>(Collections.singletonList("com.tencent.mm")))
                .putBoolean(OriginalSettings.AUTO_BACKGROUND_TIP_SHOWN, false).commit();
        AutoApps.enable(true); authorizeOwnService(true);
        await(() -> ReferenceCaptureService.current() != null && ReferenceCaptureService.status().optBoolean("permissionGranted")
                && OriginalServiceState.isRunning(ReferenceApplication.app()), "Own service did not connect before the real capture start");
        launch("capture"); shown(OriginalPermissionTipDialog.class, "tip");
        scenario.onActivity(host -> {
            assertTrue(control(host, "switch_auto_bill").isChecked()); assertFalse(control(host, "switch_overlays").isChecked());
            assertNull("Running service must not take the missing-service branch", host.getSupportFragmentManager().findFragmentByTag("auto_tip"));
        });
        assertFalse(ReferenceApplication.prefs().getBoolean(OriginalSettings.AUTO_BACKGROUND_TIP_SHOWN, true));
        tapTip("cancel"); hidden(); clearTip();
        scenario.moveToState(Lifecycle.State.CREATED); saw(true); scenario.moveToState(Lifecycle.State.RESUMED);
        await(() -> ReferenceApplication.prefs().getBoolean(OriginalSettings.AUTO_BACKGROUND_TIP_SHOWN, false), "Real onStart did not persist the first-start marker after SAW grant");
        AtomicReference<androidx.fragment.app.Fragment> firstOemGuidance = new AtomicReference<>();
        scenario.onActivity(host -> {
            host.getSupportFragmentManager().executePendingTransactions();
            assertTrue(control(host, "switch_auto_bill").isChecked()); assertTrue(control(host, "switch_overlays").isChecked());
            assertNull(host.getSupportFragmentManager().findFragmentByTag("tip"));
            // The cloud brand may have no original OEM dialog. Preserve the
            // actual result rather than substituting a simulated brand.
            firstOemGuidance.set(host.getSupportFragmentManager().findFragmentByTag("auto_tip"));
        });
        scenario.moveToState(Lifecycle.State.CREATED); scenario.moveToState(Lifecycle.State.RESUMED);
        scenario.onActivity(host -> {
            host.getSupportFragmentManager().executePendingTransactions();
            assertTrue(control(host, "switch_auto_bill").isChecked()); assertTrue(control(host, "switch_overlays").isChecked());
            assertNull(host.getSupportFragmentManager().findFragmentByTag("tip"));
            assertSame("The persisted marker must prevent another OEM guidance instance", firstOemGuidance.get(), host.getSupportFragmentManager().findFragmentByTag("auto_tip"));
        });
        assertTrue(ReferenceApplication.prefs().getBoolean(OriginalSettings.AUTO_BACKGROUND_TIP_SHOWN, false));
        assertTrue(AutoApps.enabled()); assertTrue(ownComponentEnabled()); assertTrue(OverlayPermission.granted());
        assertNotNull(ReferenceCaptureService.current()); assertTrue(OriginalServiceState.isRunning(ReferenceApplication.app()));
    }

    private void startChecked(ManagerActivity host, OriginalCapturePermissionController controller) {
        AutoApps.enable(true); CompoundButton master = control(host, "switch_auto_bill"); master.setOnCheckedChangeListener(null); master.setChecked(true);
        controller.onAutoPageStarted(master, control(host, "switch_overlays"));
    }
    private static CompoundButton control(ManagerActivity host, String id) {
        View found = host.findViewById(Ui.id(host, id)); assertTrue("Missing original control " + id, found instanceof CompoundButton); return (CompoundButton) found;
    }
    private void sameOverlayControl(View original, boolean checked) {
        scenario.onActivity(host -> { assertSame(original, control(host, "switch_overlays")); assertEquals(checked, ((CompoundButton) original).isChecked()); });
    }
    private void launch(String page) { scenario = ActivityScenario.launch(new Intent(ReferenceApplication.app(), ManagerActivity.class).putExtra("page", page)); }
    private void shown(Class<? extends DialogFragment> expected, String tag) {
        await(() -> { scenario.onActivity(host -> {
            androidx.fragment.app.Fragment value = host.getSupportFragmentManager().findFragmentByTag(tag);
            if (expected.isInstance(value) && value.isResumed()) {
                DialogFragment current = (DialogFragment) value;
                if (current.getDialog() != null && current.getDialog().isShowing() && current.getDialog().getWindow().getDecorView().hasWindowFocus()
                        && current.getView() != null && current.getView().isAttachedToWindow() && current.getView().isLaidOut()) { tip.set(current); dialog.set(current.getDialog()); }
            }
        }); return dialog.get() != null; }, "The actual source guidance was not attached");
    }
    private void hidden() { await(() -> { AtomicBoolean showing = new AtomicBoolean(); scenario.onActivity(host -> showing.set(dialog.get().isShowing())); return !showing.get(); }, "Guidance did not dismiss"); }
    private void clearTip() { scenario.onActivity(host -> host.getSupportFragmentManager().executePendingTransactions()); tip.set(null); dialog.set(null); }
    private void tapTip(String id) { AtomicReference<Point> target = new AtomicReference<>(); scenario.onActivity(host -> target.set(center(Ui.view(tip.get().requireView(), id)))); touch(target.get()); }
    private void tapMain(String id) {
        AtomicReference<Point> target = new AtomicReference<>();
        await(() -> { scenario.onActivity(host -> { if (host.hasWindowFocus()) target.set(center(control(host, id))); }); return target.get() != null; }, "Main control has no focused window");
        touch(target.get());
    }
    private static Point center(View control) {
        assertNotNull(control); assertTrue(control.isShown()); assertTrue(control.isEnabled()); Rect visible = new Rect(); assertTrue(control.getGlobalVisibleRect(visible));
        int[] position = new int[2]; control.getLocationOnScreen(position); int x = position[0] + control.getWidth() / 2, y = position[1] + control.getHeight() / 2;
        assertTrue("Original control center is clipped", visible.contains(x, y)); return new Point(x, y);
    }
    private void touch(Point point) {
        long now = SystemClock.uptimeMillis(); MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, point.x, point.y, 0);
        MotionEvent up = MotionEvent.obtain(now, now + 60, MotionEvent.ACTION_UP, point.x, point.y, 0);
        try { down.setSource(InputDevice.SOURCE_TOUCHSCREEN); up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            assertTrue(automation.injectInputEvent(down, true)); assertTrue(automation.injectInputEvent(up, true)); }
        finally { down.recycle(); up.recycle(); }
        instrumentation.waitForIdleSync();
    }
    private static void assertIntent(Intent intent, String action, int flags) {
        assertEquals(action, intent.getAction()); assertEquals(flags, intent.getFlags()); assertNull(intent.getData()); assertNull(intent.getExtras()); assertNull(intent.getComponent());
    }
    private void saw(boolean granted) throws Exception {
        shell("appops set --user current " + token(packageName()) + " SYSTEM_ALERT_WINDOW " + (granted ? "allow" : "ignore"));
        await(() -> OverlayPermission.granted() == granted, "Actual SAW app-op did not change");
    }
    private String overlayMode() throws Exception {
        String output = shell("appops get --user current " + token(packageName()) + " SYSTEM_ALERT_WINDOW");
        Matcher match = Pattern.compile("SYSTEM_ALERT_WINDOW:\\s*(allow|deny|ignore|default|foreground)\\b").matcher(output);
        if (match.find()) return match.group(1); assertTrue(output.contains("No operations")); return "default";
    }
    private boolean ownComponentEnabled() throws Exception {
        ComponentName own = new ComponentName(ReferenceApplication.app(), ReferenceCaptureService.class);
        for (String value : shell("settings get secure enabled_accessibility_services").trim().split(":"))
            if (own.equals(ComponentName.unflattenFromString(value))) return true;
        return false;
    }
    private void authorizeOwnService(boolean enabled) throws Exception {
        ComponentName own = new ComponentName(ReferenceApplication.app(), ReferenceCaptureService.class);
        String configured = shell("settings get secure enabled_accessibility_services").trim();
        List<String> services = new ArrayList<>();
        if (!configured.isEmpty() && !configured.equals("null")) for (String value : configured.split(":")) {
            if (!value.isEmpty() && !own.equals(ComponentName.unflattenFromString(value))) services.add(value);
        }
        if (enabled) services.add(own.flattenToString());
        shell("settings put secure enabled_accessibility_services " + token(String.join(":", services)));
        if (enabled || services.isEmpty()) shell("settings put secure accessibility_enabled " + (enabled ? "1" : "0"));
    }
    private static String component() { return new ComponentName(ReferenceApplication.app(), ReferenceCaptureService.class).flattenToString(); }
    private static String packageName() { return ReferenceApplication.app().getPackageName(); }
    private static String token(String value) {
        if (!value.matches("[A-Za-z0-9_./:$-]*")) throw new IllegalArgumentException("Unexpected synthetic shell token"); return "'" + value + "'";
    }
    private String shell(String command) throws Exception {
        return com.wangc.bill.reference.ReferenceShell.run(automation, command);
    }
    private static void await(BooleanSupplier condition, String message) {
        long end = SystemClock.uptimeMillis() + 10000;
        while (SystemClock.uptimeMillis() < end) { if (condition.getAsBoolean()) return; SystemClock.sleep(25); }
        assertTrue(message, condition.getAsBoolean());
    }
}
