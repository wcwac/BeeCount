package com.wangc.bill.reference.capture;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.wangc.bill.reference.ReferenceApplication;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Source master-OFF versus the retained host filter, only on the opted-in disposable emulator. */
@RunWith(AndroidJUnit4.class)
public final class OriginalMasterLifecycleDeviceTest {
    private UiAutomation automation;
    private String priorServices, priorAccessibility;
    private String[] priorObserverPackages;
    private Map<String, ?> preferences;

    @Before public void prepare() throws Exception {
        assertEquals("true", InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        assertEquals(36, Build.VERSION.SDK_INT);
        assertTrue(Build.HARDWARE.equals("ranchu") || Build.HARDWARE.equals("goldfish"));
        assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        automation = InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        AccessibilityServiceInfo observer = automation.getServiceInfo();
        priorObserverPackages = observer.packageNames == null ? null : observer.packageNames.clone();
        observer.packageNames = new String[]{ReferenceApplication.app().getPackageName()}; automation.setServiceInfo(observer);
        preferences = new HashMap<>(ReferenceApplication.prefs().getAll());
        priorServices = shell("settings get secure enabled_accessibility_services").trim();
        priorAccessibility = shell("settings get secure accessibility_enabled").trim();
    }

    @After public void restore() throws Exception {
        if (preferences == null) return;
        try {
            // Cleanup is deliberately the host filter. It must not asynchronously
            // disable the service whose previous secure state is restored below.
            AutoApps.enable(false); barrier();
            SharedPreferences.Editor editor = ReferenceApplication.prefs().edit().clear();
            for (Map.Entry<String, ?> item : preferences.entrySet()) {
                Object value = item.getValue(); String key = item.getKey();
                if (value instanceof Boolean b) editor.putBoolean(key, b);
                else if (value instanceof Long l) editor.putLong(key, l);
                else if (value instanceof Integer i) editor.putInt(key, i);
                else if (value instanceof Float f) editor.putFloat(key, f);
                else if (value instanceof String s) editor.putString(key, s);
                else if (value instanceof Set<?> values) {
                    Set<String> strings = new HashSet<>(); for (Object string : values) strings.add((String) string);
                    editor.putStringSet(key, strings);
                }
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
                    ReferenceCaptureService.refresh(); barrier();
                } finally {
                    AccessibilityServiceInfo observer = automation.getServiceInfo(); observer.packageNames = priorObserverPackages;
                    automation.setServiceInfo(observer);
                }
            }
        }
    }

    @Test public void cloudShellTransportPreservesEmptyAndDollarArgumentsAndReportsStderrAndFailure() throws Exception {
        assertEquals("<>\n<literal$token>\n", shell("printf '<%s>\\n' '' 'literal$token'"));
        assertEquals("transport-stderr\n", shell("printf '%s\\n' 'transport-stderr' >&2"));
        try {
            shell("(printf '%s\\n' 'transport-failure' >&2; exit 7)");
            fail("A failing fixture command must not look successful");
        } catch (java.io.IOException expected) {
            assertTrue(expected.getMessage().contains("exit 7"));
            assertTrue(expected.getMessage().contains("transport-failure"));
        }
    }

    @Test public void runningProbeUsesTheExactClassAndMaximumCountRatherThanConnectionOrPermission() {
        String target = ReferenceCaptureService.class.getName(); AtomicInteger maximum = new AtomicInteger();
        ActivityManager.RunningServiceInfo other = service("example.other", target + "Suffix");
        ActivityManager.RunningServiceInfo match = service("example.own", target);
        assertTrue(OriginalServiceState.isRunning(limit -> { maximum.set(limit); return Arrays.asList(other, match); }, target));
        assertEquals(Integer.MAX_VALUE, maximum.get());
        assertFalse(OriginalServiceState.isRunning(limit -> Collections.singletonList(other), target));
        assertFalse(OriginalServiceState.isRunning(limit -> Collections.emptyList(), target));
        assertFalse(OriginalServiceState.isRunning(limit -> null, target));
        // Original f compares class names; package comparison is not an extra gate.
        assertTrue(OriginalServiceState.isRunning(limit -> Collections.singletonList(match), target));
    }

    @Test public void runningProbeCatchesExceptionsAcrossLookupAndIterationButPropagatesErrors() {
        String target = ReferenceCaptureService.class.getName();
        assertFalse(OriginalServiceState.isRunning(limit -> { throw new SecurityException("synthetic probe denial"); }, target));
        assertFalse(OriginalServiceState.isRunning(limit -> Arrays.asList(new ActivityManager.RunningServiceInfo(), service("example.own", target)), target));
        assertTrue(OriginalServiceState.isRunning(limit -> Arrays.asList(service("example.own", target), null), target));
        AssertionError original = new AssertionError("synthetic probe Error");
        try { OriginalServiceState.isRunning(limit -> { throw original; }, target); fail("The original probe does not catch Error"); }
        catch (AssertionError error) { assertSame(original, error); }
    }

    @Test public void anOwnedCreatedSubscriberReceivesMainOffBeforeConnectionAndAfterItsUnbind() throws Exception {
        // An owned adapter fixture, not an unattached Android Service whose
        // framework lifecycle is invoked manually. It exercises production dispatch.
        Object fixture = new Object(); AtomicReference<WeakReference<Object>> created = new AtomicReference<>(new WeakReference<>(fixture));
        AtomicReference<Object> connected = new AtomicReference<>(); AtomicInteger calls = new AtomicInteger();
        AtomicBoolean allOnMain = new AtomicBoolean(true); Handler main = new Handler(Looper.getMainLooper());
        CountDownLatch beforeConnection = new CountDownLatch(1);
        OriginalServiceState.dispatchMasterOff(created.get().get(), () -> created.get().get(), target -> {
            assertSame(fixture, target); assertNull(connected.get()); calls.incrementAndGet();
            allOnMain.set(allOnMain.get() && Looper.myLooper() == Looper.getMainLooper()); beforeConnection.countDown();
        }, task -> main.post(task));
        assertTrue(beforeConnection.await(3, TimeUnit.SECONDS));
        connected.set(fixture); connected.set(null); CountDownLatch afterUnbind = new CountDownLatch(1);
        OriginalServiceState.dispatchMasterOff(created.get().get(), () -> created.get().get(), target -> {
            assertSame(fixture, target); assertNull(connected.get()); calls.incrementAndGet();
            allOnMain.set(allOnMain.get() && Looper.myLooper() == Looper.getMainLooper()); afterUnbind.countDown();
        }, task -> main.post(task));
        assertTrue(afterUnbind.await(3, TimeUnit.SECONDS)); assertEquals(2, calls.get()); assertTrue(allOnMain.get());
    }

    @Test public void aQueuedOffNeverReplaysAgainstADestroyedSubscriberOrANewService() {
        Object old = new Object(), later = new Object(); AtomicReference<WeakReference<Object>> created = new AtomicReference<>(new WeakReference<>(old));
        List<Runnable> main = new ArrayList<>(); AtomicInteger calls = new AtomicInteger();
        OriginalServiceState.dispatchMasterOff(created.get().get(), () -> created.get().get(), target -> calls.incrementAndGet(), main::add);
        assertEquals(1, main.size()); created.get().clear(); created.set(new WeakReference<>(later)); main.remove(0).run(); assertEquals(0, calls.get());
        created.get().clear();
        OriginalServiceState.dispatchMasterOff(created.get().get(), () -> created.get().get(), target -> calls.incrementAndGet(), main::add);
        assertTrue(main.isEmpty()); created.set(new WeakReference<>(later)); assertEquals(0, calls.get());
        OriginalServiceState.dispatchMasterOff(later, () -> created.get().get(), target -> { assertSame(later, target); calls.incrementAndGet(); }, main::add);
        main.remove(0).run(); assertEquals(1, calls.get());
    }

    @Test public void theExistingHostEnableFalseOnlyPausesFilteringAndLeavesTheActualServiceAuthorized() throws Exception {
        authorizeOwnService(); AutoApps.enable(true);
        await(() -> ReferenceCaptureService.status().optInt("eventTypes") == 2080, "Host enable did not configure events");
        ReferenceCaptureService retained = ReferenceCaptureService.current(); assertNotNull(retained);
        AutoApps.enable(false); barrier(); JSONObject paused = ReferenceCaptureService.status();
        assertFalse(paused.optBoolean("enabled")); assertEquals(0, paused.optInt("eventTypes"));
        assertTrue(paused.optBoolean("permissionGranted")); assertTrue(paused.optBoolean("connected"));
        assertTrue(paused.optBoolean("running")); assertSame(retained, ReferenceCaptureService.current());
        assertTrue(containsOwnComponent()); AutoApps.enable(true);
        await(() -> ReferenceCaptureService.status().optInt("eventTypes") == 2080, "Host resume required a new system authorization");
        assertSame(retained, ReferenceCaptureService.current());
    }

    @Test public void originalMasterOffReallyDisablesAccessibilityAndOriginalOnRequiresExplicitSystemReauthorization() throws Exception {
        authorizeOwnService(); AutoApps.setOriginalMasterEnabled(true);
        await(() -> ReferenceCaptureService.status().optInt("eventTypes") == 2080, "Original ON did not persist/filter");
        AutoApps.setOriginalMasterEnabled(false); assertFalse(AutoApps.enabled());
        await(() -> !ReferenceCaptureService.status().optBoolean("permissionGranted") && ReferenceCaptureService.current() == null
                && !OriginalServiceState.isRunning(ReferenceApplication.app()), "Real disableSelf did not revoke and disconnect our service");
        assertFalse(containsOwnComponent()); assertFalse(ReferenceCaptureService.status().optBoolean("connected"));
        // Production ON only persists. A MAIN barrier cannot stand in for a
        // system grant, and it must not enqueue any future auto-authorization.
        AutoApps.setOriginalMasterEnabled(true); barrier();
        assertTrue(AutoApps.enabled()); assertNull(ReferenceCaptureService.current()); assertFalse(containsOwnComponent());
        assertFalse(ReferenceCaptureService.status().optBoolean("permissionGranted"));
        writeOwnAuthorization();
        await(() -> ReferenceCaptureService.current() != null && ReferenceCaptureService.status().optInt("eventTypes") == 2080,
                "Disposable-emulator system reauthorization did not reconnect the actual service");
        assertTrue(ReferenceCaptureService.status().optBoolean("permissionGranted")); assertTrue(containsOwnComponent());
    }

    private void authorizeOwnService() throws Exception {
        ReferenceApplication.prefs().edit().putStringSet("enabled_packages", new HashSet<>(Collections.singletonList("com.tencent.mm"))).commit();
        AutoApps.enable(false); writeOwnAuthorization();
        await(() -> ReferenceCaptureService.current() != null && ReferenceCaptureService.status().optBoolean("permissionGranted"), "Own accessibility service did not connect");
        barrier();
    }
    private void writeOwnAuthorization() throws Exception {
        shell("settings put secure enabled_accessibility_services " + token(component())); shell("settings put secure accessibility_enabled 1");
    }
    private boolean containsOwnComponent() throws Exception {
        String enabled = shell("settings get secure enabled_accessibility_services").trim();
        ComponentName own = new ComponentName(ReferenceApplication.app(), ReferenceCaptureService.class);
        for (String value : enabled.split(":")) if (own.equals(ComponentName.unflattenFromString(value))) return true;
        return false;
    }
    private static ActivityManager.RunningServiceInfo service(String pkg, String name) {
        ActivityManager.RunningServiceInfo info = new ActivityManager.RunningServiceInfo(); info.service = new ComponentName(pkg, name); return info;
    }
    private static String component() { return new ComponentName(ReferenceApplication.app(), ReferenceCaptureService.class).flattenToString(); }
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
    private static void barrier() { InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {}); }
}
