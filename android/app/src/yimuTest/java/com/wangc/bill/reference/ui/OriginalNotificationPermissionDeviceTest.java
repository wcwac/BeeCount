package com.wangc.bill.reference.ui;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Dialog;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.Settings;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Configurator;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;
import com.wangc.bill.reference.ReferenceApplication;
import com.wangc.bill.reference.OriginalSettings;
import com.wangc.bill.reference.capture.AutoApps;
import com.wangc.bill.reference.capture.ReferenceCaptureService;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

/** Actual OS POST_NOTIFICATIONS requests, confined to the disposable authorized cloud emulator. */
@RunWith(AndroidJUnit4.class)
public final class OriginalNotificationPermissionDeviceTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private UiAutomation automation;
    private UiDevice device;
    private ActivityScenario<ManagerActivity> scenario;
    private String[] priorObserverPackages;
    private int priorAutomationFlags, priorObserverFlags;
    private boolean controllerPreferencesSaved;
    private Boolean previousCaptureSetting, previousNotifySetting;

    @Before public void open() throws Exception {
        assertEquals("true", InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        assertEquals(36, Build.VERSION.SDK_INT);
        assertTrue(Build.HARDWARE.equals("ranchu") || Build.HARDWARE.equals("goldfish"));
        assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        priorAutomationFlags = Configurator.getInstance().getUiAutomationFlags();
        Configurator.getInstance().setUiAutomationFlags(priorAutomationFlags | UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        automation = instrumentation.getUiAutomation(priorAutomationFlags | UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        AccessibilityServiceInfo observer = automation.getServiceInfo(); priorObserverPackages = observer.packageNames == null ? null : observer.packageNames.clone();
        priorObserverFlags = observer.flags;
        observer.packageNames = new String[]{packageName(), "com.android.permissioncontroller"};
        observer.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        automation.setServiceInfo(observer); device = UiDevice.getInstance(instrumentation);
        onMain(() -> assertEquals(0, OriginalNotificationPermission.pendingRequestCount()));
    }

    @After public void restore() throws Exception {
        try {
            if (device != null) {
                UiObject2 deny = device.findObject(By.res("com.android.permissioncontroller", "permission_deny_button"));
                if (deny != null) deny.click();
            }
            if (scenario != null) scenario.close();
        }
        finally {
            try {
                if (controllerPreferencesSaved) {
                    // This is fixture filtering, never the original master-OFF
                    // action; do not revoke an existing service's authorization.
                    AutoApps.enable(false); onMain(() -> {});
                    android.content.SharedPreferences.Editor editor = ReferenceApplication.prefs().edit();
                    if (previousCaptureSetting == null) editor.remove("capture_enabled"); else editor.putBoolean("capture_enabled", previousCaptureSetting);
                    if (previousNotifySetting == null) editor.remove(OriginalSettings.NOTIFY_SHOW); else editor.putBoolean(OriginalSettings.NOTIFY_SHOW, previousNotifySetting);
                    assertTrue(editor.commit()); ReferenceCaptureService.refresh(); onMain(() -> {});
                }
            } finally {
                if (automation != null) {
                    AccessibilityServiceInfo observer = automation.getServiceInfo(); observer.packageNames = priorObserverPackages;
                    observer.flags = priorObserverFlags;
                    automation.setServiceInfo(observer);
                    Configurator.getInstance().setUiAutomationFlags(priorAutomationFlags);
                }
            }
        }
    }

    @Test public void aRealAlreadyGrantedPermissionCallsBackImmediatelyWithoutATipOrTransientActivity() throws Exception {
        requireInitialPermission(true, false); launch(); Result result = new Result(); AtomicInteger launches = new AtomicInteger();
        scenario.onActivity(host -> {
            OriginalNotificationPermission.request(host, result, (owner, intent) -> launches.incrementAndGet());
            assertEquals(1, result.granted.get()); assertEquals(0, result.denied.get());
            assertNull(host.getSupportFragmentManager().findFragmentByTag("tip"));
            assertEquals(0, OriginalNotificationPermission.pendingRequestCount());
        });
        assertTrue(result.onMain.get()); assertEquals(0, launches.get()); assertEquals(0, result.unavailable.get());
    }

    @Test public void actualOsGrantDismissesTopInfoBeforeExactlyOneFreshGrantedCallback() throws Exception {
        requireInitialPermission(false, false); launch(); Result result = new Result();
        Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor(
                OriginalPermissionRequestActivity.class.getName(), null, false);
        instrumentation.addMonitor(monitor);
        try {
            request(result); assertTrue(result.explanationBeforeLaunch.get());
            OriginalPermissionRequestActivity bridge = (OriginalPermissionRequestActivity) monitor.waitForActivityWithTimeout(5000);
            assertNotNull("The actual permission bridge was not launched", bridge);
            systemButton("permission_allow_button").click(); await(() -> result.granted.get() == 1);
            assertTrue(OriginalNotificationPermission.granted()); assertFinished(result, 1, 0);
            // A late contradictory dispatch cannot consume the same callback again.
            onMain(() -> bridge.onRequestPermissionsResult(999,
                    new String[]{Manifest.permission.CAMERA}, new int[]{PackageManager.PERMISSION_DENIED}));
            instrumentation.waitForIdleSync(); assertFinished(result, 1, 0);
        } finally { instrumentation.removeMonitor(monitor); }
    }

    @Test public void actualOsDenialDismissesTopInfoBeforeItsDistinctDeniedCallback() throws Exception {
        requireInitialPermission(false, false); launch(); Result result = new Result(); request(result);
        assertTrue(result.explanationBeforeLaunch.get()); systemButton("permission_deny_button").click();
        await(() -> result.denied.get() == 1); assertFalse(OriginalNotificationPermission.granted());
        assertFinished(result, 0, 1);
    }

    @Test public void aUserFixedDenialStillRequestsAgainWithoutOpeningSettingsOrUsingACooldown() throws Exception {
        requireInitialPermission(false, true); launch();
        IntentFilter filter = new IntentFilter(Settings.ACTION_APPLICATION_DETAILS_SETTINGS); filter.addDataScheme("package");
        Instrumentation.ActivityMonitor settings = new Instrumentation.ActivityMonitor(filter,
                new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null), true);
        instrumentation.addMonitor(settings);
        try {
            for (int i = 0; i < 2; i++) {
                Result result = new Result(); request(result);
                await(() -> result.denied.get() == 1); assertFinished(result, 0, 1);
                assertEquals(1, result.launches.get()); assertTrue(result.explanationBeforeLaunch.get());
                assertFalse(OriginalNotificationPermission.granted());
            }
            assertEquals(0, settings.getHits());
        } finally { instrumentation.removeMonitor(settings); }
    }

    @Test public void destroyingTheOwnerWhileTheRealOsRequestIsPendingReportsOnlyAHostFailure() throws Exception {
        requireInitialPermission(false, false); launch(); Result result = new Result(); request(result);
        systemButton("permission_deny_button");
        scenario.onActivity(host -> host.finish());
        await(() -> result.unavailable.get() == 1);
        assertEquals("OWNER_DESTROYED", result.failure.get()); assertEquals(0, result.granted.get()); assertEquals(0, result.denied.get());
        assertTrue(result.dismissedBeforeCallback.get()); onMain(() -> assertEquals(0, OriginalNotificationPermission.pendingRequestCount()));
        // The platform may itself remove the permission window while the
        // caller finishes. If it remains, dismiss its actual pending request.
        UiObject2 remaining = device.findObject(By.res("com.android.permissioncontroller", "permission_deny_button"));
        if (remaining != null) remaining.click(); instrumentation.waitForIdleSync();
        assertEquals(1, result.unavailable.get()); assertEquals(0, result.granted.get()); assertEquals(0, result.denied.get());
        assertFalse(OriginalNotificationPermission.granted());
    }

    @Test public void anUnownedTransientReconstructionFailsClosedWithoutRequestingOrInventingAnOutcome() throws Exception {
        requireInitialPermission(false, false);
        Intent orphan = new Intent(ReferenceApplication.app(), OriginalPermissionRequestActivity.class)
                .putExtra(OriginalPermissionRequestActivity.EXTRA_TOKEN, "synthetic-unowned-process-token");
        try (ActivityScenario<OriginalPermissionRequestActivity> bridge = ActivityScenario.launch(orphan)) {
            await(() -> bridge.getState() == Lifecycle.State.DESTROYED);
            assertEquals("REQUEST_STATE_MISSING", OriginalNotificationPermission.lastBridgeFailureCode());
            onMain(() -> assertEquals(0, OriginalNotificationPermission.pendingRequestCount()));
            assertFalse(OriginalNotificationPermission.granted());
            assertNull(device.findObject(By.res("com.android.permissioncontroller", "permission_allow_button")));
        }
    }

    @Test public void aSynchronousLaunchFailurePropagatesAfterCleaningTheRegistryWithoutAnOutcome() throws Exception {
        requireInitialPermission(false, false); launch(); Result result = new Result();
        scenario.onActivity(host -> {
            IllegalStateException failure = new IllegalStateException("synthetic notification launcher failure");
            try {
                OriginalNotificationPermission.request(host, result, (owner, intent) -> {
                    observeExplanation(owner, result); throw failure;
                });
                fail("The source synchronous exception must propagate");
            } catch (IllegalStateException actual) { assertSame(failure, actual); }
            assertEquals(0, OriginalNotificationPermission.pendingRequestCount());
            assertFalse(result.dialog.get().isShowing());
        });
        assertEquals(0, result.granted.get()); assertEquals(0, result.denied.get()); assertEquals(0, result.unavailable.get());
        assertFalse(OriginalNotificationPermission.granted());
    }

    @Test public void aRecreatedOwnerDropsItsOrphanExplanationWithoutInventingAPermissionOutcome() throws Exception {
        requireInitialPermission(false, false); launch(); Result result = new Result();
        // Hold only the host launch boundary so the Activity's actual saved
        // FragmentManager state can be recreated without an unrelated OS dialog.
        scenario.onActivity(host -> OriginalNotificationPermission.request(host, result, (owner, intent) -> observeExplanation(owner, result)));
        assertTrue(result.explanationBeforeLaunch.get()); scenario.recreate();
        await(() -> result.unavailable.get() == 1);
        scenario.onActivity(host -> {
            // The real ManagerActivity.onStart hook must remove the restored
            // orphan; this test must not repair missing production wiring.
            host.getSupportFragmentManager().executePendingTransactions();
            assertNull(host.getSupportFragmentManager().findFragmentByTag("tip"));
            assertEquals(0, OriginalNotificationPermission.pendingRequestCount());
        });
        assertEquals("OWNER_DESTROYED", result.failure.get());
        assertEquals("OWNER_STATE_MISSING", OriginalNotificationPermission.lastBridgeFailureCode());
        assertEquals(0, result.granted.get()); assertEquals(0, result.denied.get());
        assertTrue(result.dismissedBeforeCallback.get()); assertFalse(OriginalNotificationPermission.granted());
    }

    @Test public void aRealOsGrantContinuesThroughTheControllerToAutoTipAfterTopInfoHasDismissed() throws Exception {
        assertActualControllerContinuation(true);
    }

    @Test public void aRealOsDenialContinuesThroughTheControllerToAutoTipAfterTopInfoHasDismissed() throws Exception {
        assertActualControllerContinuation(false);
    }

    private void assertActualControllerContinuation(boolean grant) throws Exception {
        requireInitialPermission(false, false);
        android.content.SharedPreferences preferences = ReferenceApplication.prefs();
        previousCaptureSetting = preferences.contains("capture_enabled") ? preferences.getBoolean("capture_enabled", false) : null;
        previousNotifySetting = preferences.contains(OriginalSettings.NOTIFY_SHOW) ? preferences.getBoolean(OriginalSettings.NOTIFY_SHOW, false) : null;
        controllerPreferencesSaved = true; assertTrue(preferences.edit().putBoolean(OriginalSettings.NOTIFY_SHOW, true).commit());
        launch(); AtomicReference<Dialog> explanation = new AtomicReference<>(), continuation = new AtomicReference<>();
        AtomicBoolean hiddenBeforeAutoTipAttach = new AtomicBoolean(); AtomicInteger settingsStarts = new AtomicInteger(), oemStarts = new AtomicInteger();
        FragmentManager.FragmentLifecycleCallbacks observer = new FragmentManager.FragmentLifecycleCallbacks() {
            @Override public void onFragmentPreAttached(FragmentManager manager, Fragment fragment, Context context) {
                if (fragment instanceof OriginalAutoTipDialog)
                    hiddenBeforeAutoTipAttach.set(explanation.get() != null && !explanation.get().isShowing());
            }
        };
        scenario.onActivity(host -> {
            host.getSupportFragmentManager().registerFragmentLifecycleCallbacks(observer, false);
            // Only the running-service branch is controlled. The real permission
            // delegate and real controller continuation run without outcome injection.
            new OriginalCapturePermissionController(host, () -> false, OriginalNotificationPermission::request,
                    intent -> settingsStarts.incrementAndGet(), owner -> oemStarts.incrementAndGet()).onMasterChanged(true);
            OriginalTopInfoDialog top = (OriginalTopInfoDialog) host.getSupportFragmentManager().findFragmentByTag("tip");
            assertNotNull(top); assertNotNull(top.getDialog()); explanation.set(top.getDialog()); assertTrue(top.getDialog().isShowing());
            assertNull(host.getSupportFragmentManager().findFragmentByTag("auto_tip"));
            assertTrue(AutoApps.enabled());
        });
        systemButton(grant ? "permission_allow_button" : "permission_deny_button").click();
        await(() -> {
            scenario.onActivity(host -> {
                Fragment fragment = host.getSupportFragmentManager().findFragmentByTag("auto_tip");
                if (fragment instanceof OriginalAutoTipDialog tip && tip.isResumed() && tip.getDialog() != null
                        && tip.getDialog().isShowing() && tip.getView() != null && tip.getView().isAttachedToWindow()
                        && tip.getView().isLaidOut() && tip.getDialog().getWindow().getDecorView().hasWindowFocus())
                    continuation.set(tip.getDialog());
            });
            return continuation.get() != null;
        });
        assertEquals(grant, OriginalNotificationPermission.granted()); assertTrue(hiddenBeforeAutoTipAttach.get());
        assertFalse(explanation.get().isShowing()); assertEquals(0, settingsStarts.get()); assertEquals(0, oemStarts.get());
        onMain(() -> assertEquals(0, OriginalNotificationPermission.pendingRequestCount()));
        UiObject2 cancel = device.wait(Until.findObject(By.res(packageName(), "cancel")), 7000);
        assertNotNull("The actual controller AutoTip cancel control was not shown", cancel); cancel.click();
        await(() -> !continuation.get().isShowing()); assertTrue(AutoApps.enabled());
        assertTrue(preferences.getBoolean(OriginalSettings.NOTIFY_SHOW, false));
        assertEquals(0, settingsStarts.get()); assertEquals(0, oemStarts.get());
        scenario.onActivity(host -> host.getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(observer));
    }

    private void request(Result result) {
        scenario.onActivity(host -> OriginalNotificationPermission.request(host, result, (owner, intent) -> {
            assertSame(host, owner); observeExplanation(owner, result); result.launches.incrementAndGet();
            assertNotNull(intent.getComponent()); assertEquals(OriginalPermissionRequestActivity.class.getName(), intent.getComponent().getClassName());
            assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.getFlags());
            assertNotNull(intent.getStringExtra(OriginalPermissionRequestActivity.EXTRA_TOKEN)); owner.startActivity(intent);
        }));
    }
    private void observeExplanation(FragmentActivity host, Result result) {
        OriginalTopInfoDialog tip = (OriginalTopInfoDialog) host.getSupportFragmentManager().findFragmentByTag("tip");
        assertNotNull(tip); assertNotNull(tip.getDialog()); result.dialog.set(tip.getDialog());
        assertEquals(OriginalNotificationPermission.TITLE, ((TextView) Ui.view(tip.requireView(), "title")).getText().toString());
        assertEquals(OriginalNotificationPermission.CONTENT, ((TextView) Ui.view(tip.requireView(), "content")).getText().toString());
        result.explanationBeforeLaunch.set(tip.getDialog().isShowing() && tip.requireView().isAttachedToWindow());
    }
    private void assertFinished(Result result, int granted, int denied) {
        instrumentation.waitForIdleSync(); assertEquals(granted, result.granted.get()); assertEquals(denied, result.denied.get());
        assertEquals(0, result.unavailable.get()); assertTrue(result.onMain.get()); assertTrue(result.dismissedBeforeCallback.get());
        assertNotNull(result.dialog.get()); assertFalse(result.dialog.get().isShowing());
        onMain(() -> assertEquals(0, OriginalNotificationPermission.pendingRequestCount()));
    }
    private void launch() {
        scenario = ActivityScenario.launch(new Intent(ReferenceApplication.app(), ManagerActivity.class).putExtra("page", "settings"));
    }
    private void requireInitialPermission(boolean granted, boolean fixed) throws Exception {
        // The cloud shell configures each exact case before its separate am
        // instrument invocation. Revoking this UID from inside its own runner
        // could kill instrumentation, so this class never mutates grants/flags.
        assertEquals("Incorrect cloud scenario permission precondition", granted, OriginalNotificationPermission.granted());
        Matcher permission = Pattern.compile("android\\.permission\\.POST_NOTIFICATIONS: granted=(?:true|false), flags=\\[([^]]*)]")
                .matcher(shell("dumpsys package " + token(packageName())));
        assertTrue("Notification runtime permission flags were not available", permission.find());
        assertEquals("Incorrect cloud scenario user-fixed precondition", fixed, permission.group(1).contains("USER_FIXED"));
        if (fixed) assertTrue(permission.group(1).contains("USER_SET"));
        else if (!granted) assertFalse("A fresh denied scenario must clear USER_SET", permission.group(1).contains("USER_SET"));
    }
    private UiObject2 systemButton(String resource) {
        UiObject2 button = device.wait(Until.findObject(By.res("com.android.permissioncontroller", resource)), 7000);
        assertNotNull("The actual OS notification permission control was not shown: " + resource, button); return button;
    }
    private void await(BooleanSupplier condition) {
        long until = SystemClock.uptimeMillis() + 7000;
        while (SystemClock.uptimeMillis() < until) { if (condition.getAsBoolean()) return; SystemClock.sleep(25); }
        assertTrue("Actual notification permission flow timed out", condition.getAsBoolean());
    }
    private static void onMain(Runnable action) { InstrumentationRegistry.getInstrumentation().runOnMainSync(action); }
    private static String packageName() { return ReferenceApplication.app().getPackageName(); }
    private String shell(String command) throws Exception {
        try (ParcelFileDescriptor descriptor = automation.executeShellCommand(command);
                InputStream input = new ParcelFileDescriptor.AutoCloseInputStream(descriptor);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[4096]; int count;
            while ((count = input.read(bytes)) != -1) { output.write(bytes, 0, count); if (output.size() > 1048576) throw new IllegalStateException("Shell output too large"); }
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }
    private static String token(String value) {
        if (!value.matches("[A-Za-z0-9_./:]+")) throw new IllegalArgumentException("Invalid emulator token"); return value;
    }
    private static final class Result implements OriginalNotificationPermission.Outcome {
        final AtomicInteger granted = new AtomicInteger(), denied = new AtomicInteger(), unavailable = new AtomicInteger(), launches = new AtomicInteger();
        final AtomicBoolean onMain = new AtomicBoolean(), dismissedBeforeCallback = new AtomicBoolean(), explanationBeforeLaunch = new AtomicBoolean();
        final AtomicReference<Dialog> dialog = new AtomicReference<>();
        final AtomicReference<String> failure = new AtomicReference<>();
        private void observed() { onMain.set(Looper.myLooper() == Looper.getMainLooper()); dismissedBeforeCallback.set(dialog.get() == null || !dialog.get().isShowing()); }
        @Override public void granted() { observed(); granted.incrementAndGet(); }
        @Override public void denied() { observed(); denied.incrementAndGet(); }
        @Override public void unavailable(String code) { observed(); failure.set(code); unavailable.incrementAndGet(); }
    }
}
