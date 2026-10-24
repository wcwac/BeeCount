package com.wangc.bill.reference.ui;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Dialog;
import android.app.Application;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
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
import java.util.List;
import java.util.concurrent.TimeoutException;
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
    private volatile String lastSystemUi = "No system window sampled";
    private volatile String lastBridgeUi = "No bridge window sampled";
    private volatile Result lastResult;
    private volatile String lastOsFlow = "No actual denial request observed";
    private final AtomicInteger bridgeCreations = new AtomicInteger();
    private boolean bridgeObserverRegistered;
    private final Application.ActivityLifecycleCallbacks bridgeObserver = new Application.ActivityLifecycleCallbacks() {
        @Override public void onActivityCreated(Activity activity, Bundle state) {
            if (activity instanceof OriginalPermissionRequestActivity) bridgeCreations.incrementAndGet();
        }
        @Override public void onActivityStarted(Activity activity) {}
        @Override public void onActivityResumed(Activity activity) {}
        @Override public void onActivityPaused(Activity activity) {}
        @Override public void onActivityStopped(Activity activity) {}
        @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) {}
        @Override public void onActivityDestroyed(Activity activity) {}
    };

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
        onMain(() -> {
            assertEquals(0, OriginalNotificationPermission.pendingRequestCount());
            ReferenceApplication.app().registerActivityLifecycleCallbacks(bridgeObserver);
            bridgeObserverRegistered = true;
        });
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
                if (bridgeObserverRegistered) onMain(() -> {
                    ReferenceApplication.app().unregisterActivityLifecycleCallbacks(bridgeObserver);
                    bridgeObserverRegistered = false;
                });
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
            awaitExplanationAttached(result);
            OriginalPermissionRequestActivity bridge = (OriginalPermissionRequestActivity) monitor.waitForActivityWithTimeout(5000);
            assertNotNull("The actual permission bridge was not launched", bridge);
            clickSystemButton("permission_allow_button"); await(() -> result.granted.get() == 1);
            assertTrue(OriginalNotificationPermission.granted()); assertFinished(result, 1, 0);
            // A late contradictory dispatch cannot consume the same callback again.
            onMain(() -> bridge.onRequestPermissionsResult(999,
                    new String[]{Manifest.permission.CAMERA}, new int[]{PackageManager.PERMISSION_DENIED}));
            instrumentation.waitForIdleSync(); assertFinished(result, 1, 0);
        } finally { instrumentation.removeMonitor(monitor); }
    }

    @Test public void actualOsDenialDismissesTopInfoBeforeItsDistinctDeniedCallback() throws Exception {
        requireInitialPermission(false, false); launch(); Result result = new Result(); request(result);
        assertTrue(result.explanationBeforeLaunch.get()); awaitExplanationAttached(result); clickSystemButton("permission_deny_button");
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
                driveActualOsDenial(result); assertFinished(result, 0, 1);
                // The source requires another real request with no app cooldown
                // or Settings shortcut. USER_FIXED does not define whether this
                // particular OS image will draw another permission window.
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
        assertTrue(result.explanationBeforeLaunch.get()); awaitExplanationAttached(result); scenario.recreate();
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
        launch(); Result actualOutcome = new Result();
        AtomicReference<Dialog> explanation = new AtomicReference<>(), continuation = new AtomicReference<>();
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
            new OriginalCapturePermissionController(host, () -> false,
                    (owner, outcome) -> launchObservedRequest(owner, actualOutcome, observeAndForward(actualOutcome, outcome)),
                    intent -> settingsStarts.incrementAndGet(), owner -> oemStarts.incrementAndGet()).onMasterChanged(true);
            OriginalTopInfoDialog top = (OriginalTopInfoDialog) host.getSupportFragmentManager().findFragmentByTag("tip");
            assertNotNull(top); assertNotNull(top.getDialog()); explanation.set(top.getDialog()); assertTrue(top.getDialog().isShowing());
            assertNull(host.getSupportFragmentManager().findFragmentByTag("auto_tip"));
            assertTrue(AutoApps.enabled());
        });
        if (grant) clickSystemButton("permission_allow_button");
        else driveActualOsDenial(actualOutcome);
        await(() -> {
            scenario.onActivity(host -> {
                Fragment fragment = host.getSupportFragmentManager().findFragmentByTag("auto_tip");
                lastBridgeUi = "controllerFragment=" + (fragment == null ? "null" : fragment.getClass().getName())
                        + ",explanation=" + com.wangc.bill.reference.ReferenceWindowGeometry.describeWindow(explanation.get())
                        + ",continuation=" + com.wangc.bill.reference.ReferenceWindowGeometry.describeWindow(
                                fragment instanceof OriginalAutoTipDialog current ? current.getDialog() : null);
                if (fragment instanceof OriginalAutoTipDialog tip && tip.isResumed() && tip.getDialog() != null
                        && tip.getDialog().isShowing() && tip.getView() != null && tip.getView().isAttachedToWindow()
                        && tip.getView().isLaidOut() && tip.getDialog().getWindow().getDecorView().hasWindowFocus())
                    continuation.set(tip.getDialog());
            });
            return continuation.get() != null;
        });
        assertFinished(actualOutcome, grant ? 1 : 0, grant ? 0 : 1);
        assertSame(explanation.get(), actualOutcome.dialog.get());
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
        scenario.onActivity(host -> launchObservedRequest(host, result, result));
    }
    private void launchObservedRequest(FragmentActivity host, Result observation, OriginalNotificationPermission.Outcome outcome) {
        OriginalNotificationPermission.request(host, outcome, (owner, intent) -> {
            assertSame(host, owner); observeExplanation(owner, observation); observation.launches.incrementAndGet();
            observation.bridgeCreationsBefore.set(bridgeCreations.get());
            assertNotNull(intent.getComponent()); assertEquals(OriginalPermissionRequestActivity.class.getName(), intent.getComponent().getClassName());
            assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.getFlags());
            assertNotNull(intent.getStringExtra(OriginalPermissionRequestActivity.EXTRA_TOKEN)); owner.startActivity(intent);
        });
    }
    private static OriginalNotificationPermission.Outcome observeAndForward(Result observation, OriginalNotificationPermission.Outcome continuation) {
        // Passive observation of the real bridge callback. Never manufacture an
        // outcome or call the controller directly to complete the request.
        return new OriginalNotificationPermission.Outcome() {
            @Override public void granted() { observation.granted(); continuation.granted(); }
            @Override public void denied() { observation.denied(); continuation.denied(); }
            @Override public void unavailable(String code) { observation.unavailable(code); continuation.unavailable(code); }
        };
    }
    private void observeExplanation(FragmentActivity host, Result result) {
        lastResult = result;
        OriginalTopInfoDialog tip = (OriginalTopInfoDialog) host.getSupportFragmentManager().findFragmentByTag("tip");
        assertNotNull(tip); assertNotNull(tip.getDialog()); result.dialog.set(tip.getDialog());
        assertEquals(OriginalNotificationPermission.TITLE, ((TextView) Ui.view(tip.requireView(), "title")).getText().toString());
        assertEquals(OriginalNotificationPermission.CONTENT, ((TextView) Ui.view(tip.requireView(), "content")).getText().toString());
        assertTrue("The explanation must be shown before launching the permission bridge", tip.getDialog().isShowing());
        result.explanationView.set(tip.requireView());
        // executePendingTransactions creates and shows the real Dialog. ViewRoot
        // attachment/layout belongs to a later traversal, not this launch call.
        result.explanationBeforeLaunch.set(tip.getDialog().isShowing());
        lastBridgeUi = com.wangc.bill.reference.ReferenceWindowGeometry.describeWindow(tip.getDialog());
    }
    private void awaitExplanationAttached(Result result) {
        await(() -> {
            AtomicBoolean ready = new AtomicBoolean();
            onMain(() -> {
                View view = result.explanationView.get(); Dialog tip = result.dialog.get(); Rect visible = new Rect();
                lastBridgeUi = com.wangc.bill.reference.ReferenceWindowGeometry.describeWindow(tip);
                ready.set(tip != null && tip.isShowing() && view != null && view.isAttachedToWindow()
                        && view.isLaidOut() && view.isShown() && view.getWidth() > 0 && view.getHeight() > 0
                        && view.getGlobalVisibleRect(visible) && !visible.isEmpty());
            });
            return ready.get();
        });
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
    private void clickSystemButton(String resource) {
        long until = SystemClock.uptimeMillis() + 7000;
        while (SystemClock.uptimeMillis() < until) {
            SystemButton before = focusedSystemButton(resource);
            if (before != null) {
                long remaining = until - SystemClock.uptimeMillis();
                if (remaining <= 0) break;
                try {
                    // A node can precede the interactive permission window's
                    // input channel during its entry transition. Quietness and
                    // a fresh focused/active-window sample share this deadline.
                    automation.waitForIdle(Math.min(100L, remaining), remaining);
                } catch (TimeoutException timeout) {
                    lastSystemUi = "Permission window did not become idle within the existing readiness budget; " + lastSystemUi;
                    break;
                }
                if (SystemClock.uptimeMillis() >= until) break;
                SystemButton after = focusedSystemButton(resource);
                if (SystemClock.uptimeMillis() >= until) break;
                if (after != null && before.windowId == after.windowId && before.bounds.equals(after.bounds)
                        && before.windowBounds.equals(after.windowBounds)) {
                    int x = after.bounds.centerX(), y = after.bounds.centerY();
                    boolean injected = device.click(x, y);
                    assertTrue("Actual OS touch injection failed: " + diagnostic(), injected);
                    return; // Exactly one real touch; no ACTION_CLICK or blind retry.
                }
            }
            long remaining = until - SystemClock.uptimeMillis();
            if (remaining > 0) SystemClock.sleep(Math.min(25L, remaining));
        }
        fail("Actual OS permission button was not in a stable focused interactive window: " + resource + "; " + diagnostic());
    }
    private void driveActualOsDenial(Result result) {
        long until = SystemClock.uptimeMillis() + 7000;
        boolean observedOsUi = false; int physicalTouches = 0;
        while (SystemClock.uptimeMillis() < until) {
            lastOsFlow = "observedOSUI=" + observedOsUi + ",physicalTouches=" + physicalTouches
                    + ",bridgeCreated=" + (bridgeCreations.get() - result.bridgeCreationsBefore.get());
            if (result.granted.get() != 0) fail("A denial scenario must not become granted: " + diagnostic());
            if (result.unavailable.get() != 0) fail("A bridge failure is not an OS denial: " + diagnostic());
            if (result.denied.get() > 1) fail("Duplicate actual denied callbacks: " + diagnostic());
            if (result.denied.get() == 1) {
                assertEquals("The request must really launch its bridge", 1, result.launches.get());
                assertTrue("The launch observation must precede Activity creation", result.bridgeCreationsBefore.get() >= 0);
                assertEquals("Exactly one real bridge Activity must be created per request",
                        1, bridgeCreations.get() - result.bridgeCreationsBefore.get());
                assertFalse("Actual OS denied callback must reflect the current permission", OriginalNotificationPermission.granted());
                assertTrue(result.onMain.get()); assertTrue(result.dismissedBeforeCallback.get());
                assertNotNull(result.dialog.get()); assertFalse(result.dialog.get().isShowing());
                onMain(() -> assertEquals(0, OriginalNotificationPermission.pendingRequestCount()));
                android.util.Log.i("ReferenceNotificationFlow", lastOsFlow + ",actualOutcome=DENIED");
                return;
            }
            // Pending==0 alone never satisfies this test: a real callback plus
            // fresh denied permission and bridge lifecycle are required above.
            int pending = actualPendingRequests();
            if (pending != 0 && pending != 1) fail("Unexpected pending request count: " + diagnostic());
            if (pending == 1 && physicalTouches == 0) {
                SystemButton before = focusedSystemButton("permission_deny_button");
                if (before != null) {
                    observedOsUi = true;
                    lastOsFlow = "observedOSUI=true,physicalTouches=" + physicalTouches
                            + ",bridgeCreated=" + (bridgeCreations.get() - result.bridgeCreationsBefore.get());
                    long remaining = until - SystemClock.uptimeMillis();
                    if (remaining <= 0) break;
                    try { automation.waitForIdle(Math.min(100L, remaining), remaining); }
                    catch (TimeoutException timeout) {
                        lastSystemUi = "Permission window did not become idle within the existing readiness budget; " + lastSystemUi;
                        break;
                    }
                    if (SystemClock.uptimeMillis() >= until) break;
                    // The OS may already have completed a noninteractive denial.
                    // Do not click an underlying app window after that result.
                    if (result.denied.get() != 0 || result.granted.get() != 0 || result.unavailable.get() != 0) continue;
                    SystemButton after = focusedSystemButton("permission_deny_button");
                    int freshPending = actualPendingRequests();
                    // Both calls above can cross process/thread boundaries. They
                    // cannot extend the deadline or permit a late physical tap.
                    if (SystemClock.uptimeMillis() >= until) break;
                    if (after != null && before.windowId == after.windowId && before.bounds.equals(after.bounds)
                            && before.windowBounds.equals(after.windowBounds) && freshPending == 1
                            && result.denied.get() == 0 && result.granted.get() == 0 && result.unavailable.get() == 0) {
                        physicalTouches++;
                        lastOsFlow = "observedOSUI=true,physicalTouches=" + physicalTouches
                                + ",bridgeCreated=" + (bridgeCreations.get() - result.bridgeCreationsBefore.get());
                        if (!device.click(after.bounds.centerX(), after.bounds.centerY()))
                            fail("Actual OS deny touch injection failed: " + diagnostic());
                        // Never inject again. Wait for this actual OS result
                        // within the same original seven-second deadline.
                    }
                }
            }
            long remaining = until - SystemClock.uptimeMillis();
            if (remaining > 0) SystemClock.sleep(Math.min(25L, remaining));
        }
        fail("The real OS denial did not complete within its original budget: " + diagnostic());
    }
    private int actualPendingRequests() {
        AtomicInteger pending = new AtomicInteger();
        onMain(() -> pending.set(OriginalNotificationPermission.pendingRequestCount()));
        return pending.get();
    }
    private SystemButton focusedSystemButton(String resource) {
        // UiAutomator may reconfigure its shared observer between real OS requests.
        // Read IDs from the observer used for this query, never the capture service flags.
        AccessibilityServiceInfo observer=automation.getServiceInfo();
        assertNotNull("The real UI observer must stay connected",observer);
        int required=AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS|AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        if((observer.flags&required)!=required){observer.flags|=required;automation.setServiceInfo(observer);}
        List<AccessibilityWindowInfo> windows = automation.getWindows();
        StringBuilder metadata = new StringBuilder("button=").append(resource).append(":observerFlags=").append(observer.flags);
        SystemButton selected = null;
        try {
            for (AccessibilityWindowInfo window : windows) {
                Rect windowBounds = new Rect(); window.getBoundsInScreen(windowBounds);
                if (metadata.length() < 1800) metadata.append(";window=").append(window.getId()).append(":type=")
                        .append(window.getType()).append(":active=").append(window.isActive()).append(":focus=")
                        .append(window.isFocused()).append(":bounds=").append(windowBounds);
                AccessibilityNodeInfo root = window.getRoot();
                if (root == null) continue;
                try {
                    boolean permissionWindow = "com.android.permissioncontroller".contentEquals(root.getPackageName() == null ? "" : root.getPackageName());
                    if (metadata.length() < 1800) metadata.append(":package=").append(root.getPackageName());
                    if (!permissionWindow || window.getType() != AccessibilityWindowInfo.TYPE_APPLICATION
                            || !window.isActive() || !window.isFocused()) continue;
                    // A second platform request can reuse the permission-controller
                    // process while replacing its window/tree. Refresh this owned root.
                    boolean refreshed=root.refresh();
                    if(metadata.length()<1800)metadata.append(":refreshed=").append(refreshed);
                    if(!refreshed)continue;
                    String[] controls=resource.equals("permission_deny_button")
                            ?new String[]{"permission_deny_button","permission_deny_and_dont_ask_again_button"}:new String[]{resource};
                    for(String control:controls){
                        List<AccessibilityNodeInfo> buttons = root.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/" + control);
                        try {
                            for (AccessibilityNodeInfo button : buttons) {
                                Rect bounds = new Rect(); button.getBoundsInScreen(bounds);
                                if (metadata.length() < 1800) metadata.append(":matchedId=").append(control).append(":buttonBounds=").append(bounds)
                                        .append(":visible=").append(button.isVisibleToUser()).append(":enabled=").append(button.isEnabled())
                                        .append(":clickable=").append(button.isClickable());
                                if (button.getWindowId()==window.getId()&&button.isVisibleToUser() && button.isEnabled() && button.isClickable() && !bounds.isEmpty()
                                        && "com.android.permissioncontroller".contentEquals(button.getPackageName()==null?"":button.getPackageName())
                                        && windowBounds.contains(bounds.centerX(), bounds.centerY()))
                                    selected = new SystemButton(window.getId(), new Rect(bounds), new Rect(windowBounds));
                            }
                        } finally { for (AccessibilityNodeInfo button : buttons) button.recycle(); }
                    }
                } finally { root.recycle(); }
            }
        } finally { for (AccessibilityWindowInfo window : windows) window.recycle(); }
        lastSystemUi = metadata.substring(0, Math.min(2000, metadata.length()));
        return selected;
    }
    private String diagnostic() {
        AtomicInteger pending = new AtomicInteger(); onMain(() -> {
            pending.set(OriginalNotificationPermission.pendingRequestCount());
            Result result = lastResult;
            if (result != null && result.dialog.get() != null)
                lastBridgeUi = com.wangc.bill.reference.ReferenceWindowGeometry.describeWindow(result.dialog.get());
        });
        String value = "pending=" + pending.get() + ",bridgeFailure=" + OriginalNotificationPermission.lastBridgeFailureCode()
                + ",permissionGranted=" + OriginalNotificationPermission.granted()
                + ",result=" + (lastResult == null ? "controller/outcome tracked by its actual continuation"
                        : lastResult.granted.get() + ":" + lastResult.denied.get() + ":" + lastResult.unavailable.get()
                                + ":launches=" + lastResult.launches.get() + ":showBeforeLaunch=" + lastResult.explanationBeforeLaunch.get())
                + ",osFlow=" + lastOsFlow + ",system=" + lastSystemUi + ",bridge=" + lastBridgeUi;
        return value.substring(0, Math.min(3500, value.length()));
    }
    private static final class SystemButton {
        final int windowId; final Rect bounds, windowBounds;
        SystemButton(int windowId, Rect bounds, Rect windowBounds) { this.windowId = windowId; this.bounds = bounds; this.windowBounds = windowBounds; }
    }
    private void await(BooleanSupplier condition) {
        long until = SystemClock.uptimeMillis() + 7000;
        while (SystemClock.uptimeMillis() < until) { if (condition.getAsBoolean()) return; SystemClock.sleep(25); }
        assertTrue("Actual notification permission flow timed out: " + diagnostic(), condition.getAsBoolean());
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
        final AtomicInteger bridgeCreationsBefore = new AtomicInteger(-1);
        final AtomicBoolean onMain = new AtomicBoolean(), dismissedBeforeCallback = new AtomicBoolean(), explanationBeforeLaunch = new AtomicBoolean();
        final AtomicReference<Dialog> dialog = new AtomicReference<>();
        final AtomicReference<View> explanationView = new AtomicReference<>();
        final AtomicReference<String> failure = new AtomicReference<>();
        private void observed() { onMain.set(Looper.myLooper() == Looper.getMainLooper()); dismissedBeforeCallback.set(dialog.get() == null || !dialog.get().isShowing()); }
        @Override public void granted() { observed(); granted.incrementAndGet(); }
        @Override public void denied() { observed(); denied.incrementAndGet(); }
        @Override public void unavailable(String code) { observed(); failure.set(code); unavailable.incrementAndGet(); }
    }
}
