package com.wangc.bill.reference.ui;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.Configurator;
import androidx.test.uiautomator.UiDevice;
import com.wangc.bill.reference.Json;
import com.wangc.bill.reference.OriginalSettings;
import com.wangc.bill.reference.ReferenceApplication;
import com.wangc.bill.reference.ReferenceShell;
import com.wangc.bill.reference.ReferenceWindowGeometry;
import com.wangc.bill.reference.capture.AutoApps;
import com.wangc.bill.reference.capture.OriginalServiceState;
import com.wangc.bill.reference.capture.ReferenceCaptureService;
import com.wangc.bill.reference.capture.ReferenceDiagnostics;
import com.wangc.bill.reference.capture.YimuKernel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Source-derived AutoWork.X/W/Y and click handlers, AutoActivity.notWork/allowRunBack,
 * and the o2/OppoFloatTempLayout window lifecycle, exercised through actual host UI.
 * DeviceInfo controls ONLY the branch selector. SAW, WindowManager, input, real
 * service lifecycle and the project-owned payment fixture are never faked.
 * No original APK, real payment application, vendor firmware, native onCreate,
 * full Glide pipeline, or background-survival equivalence is claimed.
 */
@RunWith(AndroidJUnit4.class)
public final class OriginalAutoWorkDeviceTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private UiAutomation automation;
    private UiDevice device;
    private ActivityScenario<ManagerActivity> scenario;
    private OriginalAutoWorkController controller;
    private Map<String, ?> preferences;
    private String[] observerPackages;
    private int observerFlags, automationFlags;
    private String overlayMode, servicesBefore, accessibilityBefore;
    private boolean serviceFixtureUsed, paymentFixtureUsed;
    private Field createdServiceField;
    private Instrumentation.ActivityMonitor navigationMonitor;
    private final AtomicInteger actualSettingsStarts = new AtomicInteger();
    private final List<String> candidates = new ArrayList<>();
    private final List<ManagerActivity> childActivities = new ArrayList<>();
    private final AtomicReference<ManagerActivity> host = new AtomicReference<>();

    @Before public void prepareDisposableCloudHost() throws Exception {
        assertEquals("true", InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        assertEquals(36, Build.VERSION.SDK_INT);
        assertTrue(Build.HARDWARE.equals("ranchu") || Build.HARDWARE.equals("goldfish"));
        assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        automationFlags = Configurator.getInstance().getUiAutomationFlags();
        Configurator.getInstance().setUiAutomationFlags(automationFlags | UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        automation = instrumentation.getUiAutomation(automationFlags | UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        AccessibilityServiceInfo observer = automation.getServiceInfo();
        observerPackages = observer.packageNames == null ? null : observer.packageNames.clone(); observerFlags = observer.flags;
        // Own windows and the actual Settings return route only; never subscribe
        // the observer to the payment fixture ahead of the original DEFAULT service.
        observer.packageNames = new String[]{packageName(), "com.android.settings"};
        observer.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        automation.setServiceInfo(observer); device = UiDevice.getInstance(instrumentation);
        preferences = new HashMap<>(ReferenceApplication.prefs().getAll()); overlayMode = readOverlayMode();
        AutoApps.enable(false);
        main(() -> window().remove()); // Explicit fixture cleanup, not source lifecycle.
        navigationMonitor = new Instrumentation.ActivityMonitor() {
            @Override public Instrumentation.ActivityResult onStartActivity(Intent intent) {
                String action = intent.getAction(); ComponentName component = intent.getComponent();
                if (action != null && action.startsWith("android.settings.")
                        || component != null && "com.android.settings".equals(component.getPackageName())
                        || "com.android.settings".equals(intent.getPackage())) actualSettingsStarts.incrementAndGet();
                // Observe actual Settings destinations without blocking a result.
                // Framework EmptyActivity/recreate starts are deliberately outside this scope.
                return null;
            }
        };
        instrumentation.addMonitor(navigationMonitor);
    }

    @After public void restoreFixtureWithoutRewritingSourceLifecycle() throws Exception {
        if (preferences == null) return;
        try {
            AutoApps.enable(false);
            main(() -> {
                if (controller != null) controller.close();
                for (ManagerActivity child : childActivities) if (!child.isDestroyed()) child.finish();
                window().remove();
            });
            if (scenario != null) scenario.close();
            // Pausing acceptance and joining prior readers must precede killing
            // the remote tree. A running read is not cancelled by enable(false).
            YimuKernel.WORKER.submit(() -> {}).get(5, TimeUnit.SECONDS);
            if (paymentFixtureUsed) shell("am force-stop com.eg.android.AlipayGphone");
            for (String id : candidates) { ReferenceApplication.store().dismiss(id); YimuKernel.complete(id); }
        } finally {
            try {
                restorePreferences();
                if (serviceFixtureUsed) {
                    shell(servicesBefore.equals("null") ? "settings delete secure enabled_accessibility_services"
                            : "settings put secure enabled_accessibility_services " + quote(servicesBefore));
                    shell(accessibilityBefore.equals("null") ? "settings delete secure accessibility_enabled"
                            : "settings put secure accessibility_enabled " + quote(accessibilityBefore));
                }
                ReferenceCaptureService.refresh();
                if (overlayMode != null) setOverlayMode(overlayMode, null);
                // Test teardown deliberately removes the temporary window. It
                // must not be mistaken for source onPause/onDestroy behavior.
                main(() -> window().remove());
            } finally {
                if (navigationMonitor != null) instrumentation.removeMonitor(navigationMonitor);
                AccessibilityServiceInfo observer = automation.getServiceInfo(); observer.packageNames = observerPackages;
                observer.flags = observerFlags; automation.setServiceInfo(observer);
                Configurator.getInstance().setUiAutomationFlags(automationFlags);
            }
        }
    }

    @Test public void theRealNotWorkingRowOpensTheActualWorkPageAndBackKeepsTheCaptureControls() {
        launch("capture"); AtomicReference<View> original = new AtomicReference<>();
        scenario.onActivity(activity -> original.set(Ui.view(activity.template, "switch_auto_bill")));
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(ManagerActivity.class.getName(), null, false);
        try {
            tap(Ui.view(host.get().template, "not_work_layout"));
            Activity opened = monitor.waitForActivityWithTimeout(7000); assertTrue(opened instanceof ManagerActivity);
            ManagerActivity work = (ManagerActivity) opened; childActivities.add(work);
            await(() -> { AtomicBoolean ready = new AtomicBoolean(); main(() -> ready.set(work.hasWindowFocus()
                    && work.template != null && work.template.isAttachedToWindow())); return ready.get(); });
            main(() -> {
                assertEquals("auto_work", work.page); assertEquals("自动记账不工作", text(work.template, "title"));
                assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, work.getRequestedOrientation());
                assertEquals(View.VISIBLE, Ui.view(work.template, "right_text").getVisibility());
                assertEquals("", text(work.template, "right_text")); assertEquals(View.GONE, Ui.view(work.template, "right_icon").getVisibility());
            });
            assertTrue(device.pressBack()); await(() -> focused(host.get()));
            scenario.onActivity(activity -> assertSame(original.get(), Ui.view(activity.template, "switch_auto_bill")));
            assertFalse(AutoApps.enabled());
        } finally { instrumentation.removeMonitor(monitor); }
    }

    @Test public void theActualAutoCloseGuideSpanNavigatesToWorkAndReturnsToTheStillShownParent() {
        launch("settings"); OriginalAutoCloseTipDialog value = OriginalAutoCloseTipDialog.create();
        scenario.onActivity(activity -> value.show(activity.getSupportFragmentManager(), "draft-parent"));
        Dialog parent = shown(value);
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(ManagerActivity.class.getName(), null, false);
        try {
            Point point = spanPoint(value, "tip_content", 16, 24, "自动记账保活教程");
            assertTrue(device.click(point.x, point.y)); Activity opened = monitor.waitForActivityWithTimeout(7000);
            assertTrue(opened instanceof ManagerActivity); ManagerActivity work = (ManagerActivity) opened; childActivities.add(work);
            await(() -> focused(work)); main(() -> { assertEquals("auto_work", work.page); assertTrue(parent.isShowing()); });
            assertTrue(device.pressBack()); await(() -> { AtomicBoolean ready = new AtomicBoolean(); main(() -> ready.set(parent.isShowing()
                    && parent.getWindow().getDecorView().hasWindowFocus())); return ready.get(); });
            assertFalse(AutoApps.enabled()); main(() -> assertTrue(value.requireView().isAttachedToWindow()));
        } finally { instrumentation.removeMonitor(monitor); }
    }

    @Test public void freshAttachedPagesPreserveCaseSensitiveOemApiAndFlymePrioritySelectors() {
        launch("auto_work"); List<Intent> launches = new ArrayList<>();
        // Fixed vectors from original X/XML, not computed by the port's selector.
        // Bits: miui,samsung,oppoSwitch,autoRun,ignore,runManager,runBg,allBg,meizu,vivoRelated,vivoBackend.
        Object[][] profiles = {
            {"other","other",36,"plain","00011010000"}, {"Xiaomi","x",33,"plain","10011010000"},
            {"Xiaomi","x",34,"plain","00011010000"}, {"Xiaomi","x",35,"plain","00010010000"},
            {"xiaomi","x",36,"plain","00011010000"}, {"HUAWEI","h",36,"plain","00000110000"},
            {"HONOR","h",36,"plain","00001110000"}, {"huawei","h",36,"plain","00011010000"},
            {"vivo","v",36,"plain","00011000011"}, {"VIVO","v",36,"plain","00011010000"},
            {"OPPO","o",34,"plain","00011001000"}, {"OPPO","o",35,"plain","00111001000"},
            {"OnePlus","o",35,"plain","00111001000"}, {"realme","r",35,"plain","00111001000"},
            {"oppo","o",35,"plain","00011010000"}, {"other","SAMSUNG",36,"plain","01011010000"},
            {"other","x",36,"FLYME 10","00001010100"}, {"other","samsung",36,"FLYME 10","00001010100"},
            {"OPPO","samsung",35,"FLYME 10","00111001000"}
        };
        String[] ids = {"miui_setting","samsung_setting","oppo_float_layout","allow_auto_run","ignore_battery_optimization",
                "run_manager","allow_run_background","allow_all_run_background","meizu_manager","vivo_related","vivo_backend"};
        for (Object[] profile : profiles) {
            render(new OriginalAutoWorkController.DeviceInfo((String) profile[0], (String) profile[1], (Integer) profile[2], (String) profile[3]),
                    (owner, intent) -> launches.add(new Intent(intent)));
            main(() -> {
                String expected = (String) profile[4]; assertEquals(ids.length, expected.length());
                for (int i = 0; i < ids.length; i++) assertEquals(Arrays.toString(profile) + ":" + ids[i],
                        expected.charAt(i) == '1' ? View.VISIBLE : View.GONE, Ui.view(controller.root(), ids[i]).getVisibility());
                assertEquals(View.GONE, Ui.view(controller.root(), "guide_layout").getVisibility());
                if (profile[0].equals("HONOR")) assertEquals("系统管家-应用工具箱-应用启动管理，关闭「自动管理」，允许自启动、后台活动、关联启动", text(controller.root(), "run_manager_tip"));
                if (profile[0].equals("HUAWEI")) assertEquals("手机管家-应用启动管理，关闭「自动管理」，允许自启动、后台活动、关联启动", text(controller.root(), "run_manager_tip"));
            });
        }
        assertTrue(launches.isEmpty());
    }

    @Test public void aConditionalRowStartsRealApplicationSettingsAndReturnRetainsTheSamePageWithoutGrantingAnything() {
        launch("auto_work"); render(info("OPPO", 35), (owner, intent) -> owner.startActivity(intent));
        View root = controller.root(); AtomicReference<Intent> observed = new AtomicReference<>();
        boolean saw = OverlayPermission.granted(); PowerManager power = (PowerManager) ReferenceApplication.app().getSystemService(android.content.Context.POWER_SERVICE);
        boolean exempt = power.isIgnoringBatteryOptimizations(packageName());
        Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor() {
            @Override public Instrumentation.ActivityResult onStartActivity(Intent intent) {
                if (Settings.ACTION_APPLICATION_DETAILS_SETTINGS.equals(intent.getAction())) observed.set(new Intent(intent));
                return null; // Observe, do not block or replace the real Settings Activity.
            }
        }; instrumentation.addMonitor(monitor);
        try {
            tap(Ui.view(root, "allow_all_run_background")); await(() -> observed.get() != null && focusedPackage("com.android.settings"));
            assertDetails(observed.get()); assertTrue(device.pressBack()); await(() -> focused(host.get()));
            main(() -> assertSame(root, host.get().template));
            assertEquals(saw, OverlayPermission.granted()); assertEquals(exempt, power.isIgnoringBatteryOptimizations(packageName()));
            assertFalse(AutoApps.enabled());
        } finally { instrumentation.removeMonitor(monitor); }
    }

    @Test public void realRowTouchesEmitExactConditionalIntentsAndOnlyTheSourceOldAutostartFallback() {
        launch("auto_work"); List<Intent> attempts = new ArrayList<>();
        render(info("OPPO", 34), (owner, intent) -> { attempts.add(new Intent(intent)); if (attempts.size() == 1) throw new IllegalStateException("synthetic absent SubSettings"); });
        tap(Ui.view(controller.root(), "allow_auto_run")); assertEquals(2, attempts.size());
        assertEquals("com.android.settings/.SubSettings", attempts.get(0).getComponent().flattenToShortString());
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, attempts.get(0).getFlags()); assertEquals(packageName(), attempts.get(0).getStringExtra("packageName")); assertDetails(attempts.get(1));
        attempts.clear(); render(info("OPPO", 35), (owner, intent) -> attempts.add(new Intent(intent)));
        tap(Ui.view(controller.root(), "allow_auto_run")); assertEquals(1, attempts.size()); assertSimple(attempts.get(0), Settings.ACTION_SETTINGS);
        attempts.clear(); render(info("OPPO", 34), (owner, intent) -> attempts.add(new Intent(intent)));
        tap(Ui.view(controller.root(), "ignore_battery_optimization")); assertSimple(attempts.get(0), Settings.ACTION_BATTERY_SAVER_SETTINGS);
        attempts.clear(); render(info("other", 34), (owner, intent) -> attempts.add(new Intent(intent)));
        tap(Ui.view(controller.root(), "ignore_battery_optimization")); Intent old = attempts.get(0);
        assertEquals(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, old.getAction()); assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, old.getFlags());
        assertEquals(new HashSet<>(Arrays.asList(Intent.CATEGORY_DEFAULT)), old.getCategories());
        assertEquals("com.android.settings/.Settings$HighPowerApplicationsActivity", old.getComponent().flattenToShortString()); assertNull(old.getData()); assertNull(old.getExtras());
        attempts.clear(); render(info("other", 35), (owner, intent) -> attempts.add(new Intent(intent)));
        tap(Ui.view(controller.root(), "ignore_battery_optimization")); assertSimple(attempts.get(0), Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        attempts.clear(); tap(Ui.view(controller.root(), "allow_run_background"));
        assertEquals(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, attempts.get(0).getAction());
        assertEquals("package:" + packageName(), attempts.get(0).getDataString()); assertEquals(0, attempts.get(0).getFlags()); assertNull(attempts.get(0).getExtras());
        assertFalse(AutoApps.enabled()); // Intent observation never fabricates an exemption.
    }

    @Test public void theDistinctCaptureBackgroundEntryShowsCancelableCommonGuidanceAndUnknownHidesItsRealRow() {
        launch("capture"); List<Intent> attempts = new ArrayList<>(); AtomicBoolean hiddenBeforeLaunch = new AtomicBoolean();
        AtomicReference<Dialog> common = new AtomicReference<>();
        OriginalAutoWorkController.Launcher launcher = (owner, intent) -> { hiddenBeforeLaunch.set(!common.get().isShowing()); attempts.add(new Intent(intent)); };
        // Selector injection is limited to the recovered entry method. The
        // required source capture template/row and the actual dialog are real.
        for (int action = 0; action < 4; action++) {
            main(() -> OriginalAutoWorkScreen.showCaptureBackgroundSettings(host.get(), info("OPPO", 35), launcher));
            OriginalCaptureBackgroundDialog value = fragment("tip", OriginalCaptureBackgroundDialog.class); common.set(shown(value));
            main(() -> { assertEquals("允许后台运行", text(value.requireView(), "title")); assertEquals("点击「耗电管理」-「允许完全后台行为」", text(value.requireView(), "content")); assertWindow(common.get(), 60); });
            if (action == 0) tap(Ui.view(value.requireView(), "cancel"));
            else if (action == 1) assertTrue(device.pressBack());
            else if (action == 2) { AtomicReference<Point> point = new AtomicReference<>(); main(() -> point.set(ReferenceWindowGeometry.outside(common.get(), host.get().getWindow().getDecorView()))); assertTrue(device.click(point.get().x, point.get().y)); }
            else tap(Ui.view(value.requireView(), "confirm"));
            await(() -> !common.get().isShowing());
        }
        assertEquals(1, attempts.size()); assertTrue(hiddenBeforeLaunch.get()); assertDetails(attempts.get(0));
        main(() -> OriginalAutoWorkScreen.showCaptureBackgroundSettings(host.get(), info("unknown", 36), (owner, intent) -> fail("No generic source fallback")));
        main(() -> assertEquals(View.GONE, Ui.view(host.get().template, "allow_run_back").getVisibility())); assertFalse(AutoApps.enabled());
    }

    @Test public void actualSwitchTouchesPersistBeforeTheMasterSelectedEffectForAllFourCellsAndKeepTinyGeometry() throws Exception {
        setOverlayMode("allow", true); launch("auto_work");
        // The real source singleton retains params across cases. Do not reset it
        // or claim an unobserved first-allocation branch was covered.
        AtomicReference<WindowManager.LayoutParams> before = new AtomicReference<>(); main(() -> before.set(window().retainedParams()));
        main(() -> window().show()); awaitWindow(true); assertTiny(before.get() == null);
        AtomicReference<View> attached = new AtomicReference<>(); AtomicReference<WindowManager.LayoutParams> attachedParams = new AtomicReference<>();
        main(() -> { attached.set(window().retainedView()); attachedParams.set(window().retainedParams()); window().show(); assertSame(attached.get(), window().retainedView()); assertEquals(attachedParams.get().y, window().retainedParams().y); });
        main(() -> window().remove()); awaitWindow(false); main(() -> window().show()); awaitWindow(true); assertTiny(false);
        for (boolean master : new boolean[]{false, true}) for (boolean checked : new boolean[]{false, true}) {
            AutoApps.enable(master); main(() -> { if (master) window().remove(); else window().show(); }); awaitWindow(!master);
            ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.OPPO_FLOAT, !checked).commit();
            render(info("OPPO", 35), (owner, intent) -> fail("The source switch launches no Settings or permission dialog"));
            AtomicInteger observed = new AtomicInteger(); AtomicBoolean memory = new AtomicBoolean(), priorWindow = new AtomicBoolean();
            SharedPreferences.OnSharedPreferenceChangeListener listener = (settings, key) -> {
                if (OriginalSettings.OPPO_FLOAT.equals(key)) { observed.incrementAndGet(); memory.set(settings.getBoolean(key, !checked) == checked); priorWindow.set(window().isAttached()); }
            };
            ReferenceApplication.prefs().registerOnSharedPreferenceChangeListener(listener);
            try { tap(Ui.view(controller.root(), "switch_oppo_float")); awaitWindow(master); }
            finally { ReferenceApplication.prefs().unregisterOnSharedPreferenceChangeListener(listener); }
            assertEquals(1, observed.get()); assertTrue(memory.get()); assertEquals(!master, priorWindow.get());
            assertEquals(checked, ReferenceApplication.prefs().getBoolean(OriginalSettings.OPPO_FLOAT, !checked)); assertEquals(master, AutoApps.enabled());
            main(() -> { assertEquals(checked, ((CompoundButton) Ui.view(controller.root(), "switch_oppo_float")).isChecked()); assertNull(host.get().getSupportFragmentManager().findFragmentByTag("tip")); });
        }
    }

    @Test public void deniedRealSawPersistsTheOptionWithoutInventingAWindowOrPermissionDialog() throws Exception {
        setOverlayMode("ignore", false); launch("auto_work"); AutoApps.enable(true);
        ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.OPPO_FLOAT, false).commit();
        AtomicInteger launches = new AtomicInteger(); render(info("OPPO", 35), (owner, intent) -> launches.incrementAndGet());
        tap(Ui.view(controller.root(), "switch_oppo_float"));
        assertTrue(ReferenceApplication.prefs().getBoolean(OriginalSettings.OPPO_FLOAT, false)); assertTrue(AutoApps.enabled());
        main(() -> { assertFalse(window().isAttached()); assertNull(host.get().getSupportFragmentManager().findFragmentByTag("tip")); assertNull(host.get().getSupportFragmentManager().findFragmentByTag("auto_tip")); });
        assertEquals(0, launches.get()); assertFalse(OverlayPermission.granted());
    }

    @Test public void realServiceCreateAndDestroyUseOnlyTheStoredOptionIncludingTheFalsePreferenceRetainedWindowQuirk() throws Exception {
        setOverlayMode("allow", true); useServiceFixture(); ownService(false); awaitServiceDestroyed();
        AutoApps.enable(false); ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.OPPO_FLOAT, true).commit();
        ownService(true); await(() -> ReferenceCaptureService.current() != null); awaitWindow(true); assertFalse(AutoApps.enabled());
        ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.OPPO_FLOAT, false).commit();
        ownService(false); awaitServiceDestroyed(); awaitWindow(true);
        // This is explicit teardown of the observed quirk, not a source fix.
        main(() -> window().remove()); awaitWindow(false);
        ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.OPPO_FLOAT, true).commit();
        ownService(true); await(() -> ReferenceCaptureService.current() != null); awaitWindow(true);
        ownService(false); awaitServiceDestroyed(); awaitWindow(false);
    }

    @Test public void theActualNonFocusableTinyWindowCoexistsWithPaymentFixtureFallbackAndProducesAPendingCandidate() throws Exception {
        setOverlayMode("allow", true); useServiceFixture(); ownService(false); awaitServiceDestroyed();
        long book = ReferenceApplication.store().books().get(0).optLong("id");
        ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.OPPO_FLOAT, true).putBoolean("show_overlay", false)
                .putBoolean("auto_save", false).putBoolean("use_active_window", false).putLong("capture_book", book).putLong("default_account", 0)
                .putStringSet("enabled_packages", new HashSet<>(Arrays.asList("com.eg.android.AlipayGphone"))).commit();
        ownService(true); await(() -> ReferenceCaptureService.current() != null); AutoApps.enable(true); awaitWindow(true);
        await(() -> ReferenceCaptureService.status().optInt("eventTypes") == 2080); ReferenceDiagnostics.clear();
        ReferenceCaptureService service = ReferenceCaptureService.current(); long began = System.currentTimeMillis(); paymentFixtureUsed = true;
        String output = shell("am start -W -S -n com.eg.android.AlipayGphone/com.alipay.android.msp.ui.views.MspContainerActivity --es fixture_amount 63.42 --ez fixture_null_source true");
        assertFalse(output, output.contains("Error:")); AtomicReference<JSONObject> found = new AtomicReference<>();
        await(12000, () -> { found.set(ReferenceApplication.store().one("SELECT * FROM candidates WHERE package_name=? AND created>=? AND state='PENDING' ORDER BY created DESC LIMIT 1", "com.eg.android.AlipayGphone", Long.toString(began))); return found.get() != null; });
        candidates.add(found.get().optString("id")); JSONObject payload = Json.parse(found.get().optString("payload"));
        assertEquals(6342, payload.optLong("amount")); assertEquals("实验支付宝商户", payload.optString("merchant")); assertEquals("支付宝", payload.optString("origin"));
        YimuKernel.WORKER.submit(() -> {}).get(5, TimeUnit.SECONDS); JSONObject diagnostics = ReferenceDiagnostics.snapshot();
        assertEquals(diagnostics.toString(), 0, diagnostics.optLong("failures")); assertTrue(diagnostics.optLong("fallbackSuccess") > 0);
        assertTrue(Arrays.asList("ACTIVE_ROOT", "INTERACTIVE_WINDOW").contains(diagnostics.optString("lastDraftSourceRoute")));
        assertEquals("com.eg.android.AlipayGphone", diagnostics.optString("package")); assertSame(service, ReferenceCaptureService.current());
        assertTrue(focusedPackage("com.eg.android.AlipayGphone")); main(() -> { assertTrue(window().isAttached()); assertFalse(window().retainedView().hasWindowFocus()); assertTrue((window().retainedParams().flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0); });
    }

    @Test public void exactLockImageSelectionLoadsARealBitmapAndSurvivesDialogRecreationWithoutClaimingGlideParity() {
        launch("auto_work"); LocalEffects before = new LocalEffects();
        String[][] vectors = {{"OPPO","lock_background_oppo"},{"Xiaomi","lock_background_xiaomi"},{"OnePlus","lock_background_vivo"}};
        for (String[] row : vectors) {
            render(info(row[0], 35), (owner, intent) -> fail("Image guidance does not navigate")); tap(Ui.view(controller.root(), "lock_background_layout"));
            OriginalImageTipDialog value = fragment("tip", OriginalImageTipDialog.class); Dialog dialog = shown(value);
            main(() -> { assertEquals(Ui.res(host.get(), "mipmap", row[1]), value.requireArguments().getInt("imageRes")); assertWindow(dialog, 60); });
            awaitDecodedImage(value);
            tap(Ui.view(value.requireView(), "confirm_btn")); await(() -> !dialog.isShowing());
        }
        render(info("OPPO", 35), (owner, intent) -> fail("No image Settings action")); tap(Ui.view(controller.root(), "lock_background_layout"));
        OriginalImageTipDialog previous = fragment("tip", OriginalImageTipDialog.class); shown(previous); ImageView priorImage = awaitDecodedImage(previous);
        scenario.recreate(); scenario.onActivity(host::set); OriginalImageTipDialog restored = fragment("tip", OriginalImageTipDialog.class); shown(restored);
        main(() -> assertEquals(Ui.res(host.get(), "mipmap", "lock_background_oppo"), restored.requireArguments().getInt("imageRes")));
        assertNotSame("Recreated guidance must decode into its actual new target", priorImage, awaitDecodedImage(restored));
        before.assertUnchanged();
    }

    @Test public void samsungGuidanceUsesTheActualCancelableWindowAndOriginalCompiledLineFeedWithoutGrantingAnything() {
        launch("auto_work"); LocalEffects before = new LocalEffects(); AtomicInteger launches = new AtomicInteger();
        for (int action = 0; action < 4; action++) {
            render(new OriginalAutoWorkController.DeviceInfo("other", "SAMSUNG", 36, "plain"), (owner, intent) -> launches.incrementAndGet());
            tap(Ui.view(controller.root(), "samsung_setting")); OriginalSamsungTipDialog value = fragment("auto_tip", OriginalSamsungTipDialog.class); Dialog dialog = shown(value);
            main(() -> { assertWindow(dialog, 40); assertTrue(hasText(value.requireView(), "三星系统更新至最新版本后，会默认开启“高耗电限制”选项，您需要手动关闭后，再重新配置自动记账：\n电池-后台控制-右上角更多设置-关闭高耗电限制")); });
            if (action == 0) tap(Ui.view(value.requireView(), "cancel")); else if (action == 1) tap(Ui.view(value.requireView(), "confirm"));
            else if (action == 2) assertTrue(device.pressBack()); else { AtomicReference<Point> point = new AtomicReference<>(); main(() -> point.set(ReferenceWindowGeometry.outside(dialog, host.get().getWindow().getDecorView()))); assertTrue(device.click(point.get().x, point.get().y)); }
            await(() -> !dialog.isShowing());
        }
        assertEquals(0, launches.get()); assertFalse(AutoApps.enabled()); before.assertUnchanged();
    }

    @Test public void paymentProtectionShowsAllFiveLocalGuideRowsAndClosingItRetainsTheWorkPage() {
        launch("auto_work"); LocalEffects before = new LocalEffects(); AtomicInteger launches = new AtomicInteger(); render(info("other", 36), (owner, intent) -> launches.incrementAndGet()); View root = controller.root();
        tap(Ui.view(root, "pay_product")); String[][] rows = {{"小米","系统设置中搜索「支付安全检测」或者「支付保险箱」"},{"OPPO","系统设置中搜索「支付保护」"},
                {"华为","系统设置中搜索「支付保护中心」"},{"VIVO","i管家→实用工具→支付保险箱"},{"其他","前往系统设置或者手机管家中查找类似入口"}};
        await(() -> activeBounds("tip_list") != null);
        for (String[] row : rows) {
            int scrolls = 0; while ((!activeText(row[0]) || !activeText(row[1])) && scrolls++ < 8) { assertVisibleGuideOrder(rows); swipeActiveList(); }
            await(() -> activeText(row[0]) && activeText(row[1])); assertVisibleGuideOrder(rows);
        }
        Rect close = activeBounds("btn_close"); assertNotNull(close); assertTrue(device.click(close.centerX(), close.centerY()));
        await(() -> activeBounds("tip_list") == null && focused(host.get())); main(() -> assertSame(root, host.get().template));
        assertEquals(0, launches.get()); assertFalse(AutoApps.enabled()); before.assertUnchanged();
    }

    @Test public void syntheticCanvasRoundedCornersKeepDimensionsCenterAndEdgesWhileClearingCornersAndRejectingNonpositiveRadius() {
        // Explicit host-bitmap bridge vector, not a reference-app golden image.
        Bitmap input = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888), output = null; input.eraseColor(0xff336699);
        try {
            output = OriginalImageTipDialog.roundCorners(input, 5); assertNotSame(input, output);
            assertEquals(16, output.getWidth()); assertEquals(16, output.getHeight()); assertEquals(Bitmap.Config.ARGB_8888, output.getConfig()); assertTrue(output.hasAlpha());
            assertEquals(0xff336699, output.getPixel(8, 8)); assertEquals(0xff336699, output.getPixel(8, 0)); assertEquals(0xff336699, output.getPixel(0, 8));
            assertEquals(0, Color.alpha(output.getPixel(0, 0))); assertEquals(0, Color.alpha(output.getPixel(15, 15))); assertEquals(0xff336699, input.getPixel(0, 0));
            for (int radius : new int[]{0, -1}) { try { OriginalImageTipDialog.roundCorners(input, radius); fail("A nonpositive radius must be rejected"); } catch (IllegalArgumentException expected) { assertEquals("roundingRadius must be greater than 0.", expected.getMessage()); } }
        } finally { if (output != null) output.recycle(); input.recycle(); }
    }

    private void launch(String page) { scenario = ActivityScenario.launch(new Intent(ReferenceApplication.app(), ManagerActivity.class).putExtra("page", page)); scenario.onActivity(host::set); await(() -> focused(host.get())); }
    private void render(OriginalAutoWorkController.DeviceInfo info, OriginalAutoWorkController.Launcher launcher) {
        assertEquals("auto_work", host.get().page);
        main(() -> { if (controller != null) controller.close(); controller = OriginalAutoWorkScreen.render(host.get(), info, launcher); });
        await(() -> { AtomicBoolean ready = new AtomicBoolean(); main(() -> ready.set(host.get().hasWindowFocus() && attached(controller.root()))); return ready.get(); });
    }
    private static OriginalAutoWorkController.DeviceInfo info(String manufacturer, int sdk) { return new OriginalAutoWorkController.DeviceInfo(manufacturer, "other", sdk, "plain"); }
    private <T extends DialogFragment> T fragment(String tag, Class<T> type) {
        AtomicReference<T> result = new AtomicReference<>(); await(() -> { main(() -> { Fragment value = host.get().getSupportFragmentManager().findFragmentByTag(tag); if (type.isInstance(value)) result.set(type.cast(value)); }); return result.get() != null; }); return result.get();
    }
    private Dialog shown(DialogFragment value) {
        AtomicReference<Dialog> result = new AtomicReference<>(); await(() -> { main(() -> { Dialog dialog = value.getDialog(); if (value.isResumed() && dialog != null && dialog.isShowing()
                && dialog.getWindow().getDecorView().hasWindowFocus() && attached(value.getView())) result.set(dialog); }); return result.get() != null; }); return result.get();
    }
    private ImageView awaitDecodedImage(DialogFragment value) {
        AtomicReference<ImageView> decoded = new AtomicReference<>();
        await(() -> {
            main(() -> {
                ImageView image = Ui.view(value.requireView(), "tip_image");
                if (attached(image) && image.isShown() && image.getDrawable() instanceof BitmapDrawable drawable) {
                    Bitmap bitmap = drawable.getBitmap();
                    if (bitmap != null && !bitmap.isRecycled() && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) decoded.set(image);
                }
            });
            return decoded.get() != null;
        });
        return decoded.get();
    }
    private static boolean attached(View view) { return view != null && view.isAttachedToWindow() && view.isLaidOut() && view.getWidth() > 0 && view.getHeight() > 0; }
    private static boolean focused(ManagerActivity activity) { AtomicBoolean value = new AtomicBoolean(); main(() -> value.set(activity != null && !activity.isDestroyed() && activity.hasWindowFocus())); return value.get(); }
    private void tap(View view) {
        assertNotNull(view); main(() -> view.requestRectangleOnScreen(new Rect(0, 0, view.getWidth(), view.getHeight()), true)); AtomicReference<Point> point = new AtomicReference<>();
        await(() -> { main(() -> { Rect local = new Rect(); if (view.isAttachedToWindow() && view.isShown() && view.isEnabled() && view.hasWindowFocus()
                && view.isLaidOut() && view.getLocalVisibleRect(local) && local.contains(view.getWidth() / 2, view.getHeight() / 2)) point.set(ReferenceWindowGeometry.center(view)); }); return point.get() != null; });
        assertTrue("Real focused-view touch was rejected", device.click(point.get().x, point.get().y)); instrumentation.waitForIdleSync();
    }
    private Point spanPoint(DialogFragment fragment, String id, int start, int end, String expected) {
        AtomicReference<Point> point = new AtomicReference<>(); main(() -> {
            TextView view = Ui.view(fragment.requireView(), id); assertTrue(view.getText() instanceof Spanned); Spanned text = (Spanned) view.getText();
            assertEquals(expected, text.subSequence(start, end).toString()); ClickableSpan[] spans = text.getSpans(start, end, ClickableSpan.class); assertTrue(spans.length > 0);
            // All source onResume copies may accumulate the same range; do not
            // invent a one-span invariant after an actual lifecycle return.
            for (ClickableSpan span : spans) { assertEquals(start, text.getSpanStart(span)); assertEquals(end, text.getSpanEnd(span)); }
            Layout layout = view.getLayout(); assertNotNull(layout); int offset = start + (end - start) / 2, line = layout.getLineForOffset(offset);
            assertEquals(line, layout.getLineForOffset(offset + 1)); int[] at = new int[2]; view.getLocationOnScreen(at);
            int x = at[0] + view.getTotalPaddingLeft() - view.getScrollX() + Math.round((layout.getPrimaryHorizontal(offset) + layout.getPrimaryHorizontal(offset + 1)) / 2);
            int y = at[1] + view.getTotalPaddingTop() - view.getScrollY() + (layout.getLineTop(line) + layout.getLineBottom(line)) / 2;
            assertTrue(ReferenceWindowGeometry.visibleBoundsInScreen(view).contains(x, y)); point.set(new Point(x, y));
        }); return point.get();
    }
    private void assertTiny(boolean firstParameters) {
        await(() -> { AtomicBoolean measured = new AtomicBoolean(); main(() -> {
            View view = window().retainedView(); measured.set(attached(view) && view.getWidth() == dp(2) && view.getHeight() == dp(5));
        }); return measured.get(); });
        main(() -> {
            View view = window().retainedView(); WindowManager.LayoutParams p = window().retainedParams(); assertNotNull(view); assertTrue(view.isAttachedToWindow());
            int height = ((WindowManager) ReferenceApplication.app().getSystemService(android.content.Context.WINDOW_SERVICE)).getCurrentWindowMetrics().getBounds().height();
            int width = ((WindowManager) ReferenceApplication.app().getSystemService(android.content.Context.WINDOW_SERVICE)).getCurrentWindowMetrics().getBounds().width();
            assertEquals(dp(2), p.width); assertEquals(dp(5), p.height); assertEquals(width - dp(2), p.x); assertEquals(firstParameters ? (height / 5) * 4 : height / 2, p.y);
            assertEquals(dp(2), view.getWidth()); assertEquals(dp(5), view.getHeight());
            assertEquals(2038, p.type); assertEquals(524328, p.flags); assertEquals(PixelFormat.RGBA_8888, p.format); assertEquals(Gravity.START | Gravity.TOP, p.gravity);
            assertFalse(view.hasWindowFocus()); assertEquals(0.01f, ((ViewGroup) ((ViewGroup) view).getChildAt(0)).getChildAt(0).getAlpha(), 0f);
            p.width = 9999; assertEquals("Inspection must not mutate live parameters", dp(2), window().retainedParams().width);
        });
    }
    private void awaitWindow(boolean expected) { await(() -> { AtomicBoolean value = new AtomicBoolean(); main(() -> value.set(window().isAttached() == expected)); return value.get(); }); }
    private static OriginalOppoCompatibilityWindow window() { return OriginalOppoCompatibilityWindow.instance(ReferenceApplication.app()); }
    private void useServiceFixture() throws Exception {
        if (!serviceFixtureUsed) {
            // Test-only read of this project's fixed existing lifecycle marker.
            // No production hook, class lookup by name, field write, or lifecycle invocation.
            createdServiceField = ReferenceCaptureService.class.getDeclaredField("created");
            createdServiceField.setAccessible(true);
            servicesBefore = shell("settings get secure enabled_accessibility_services").trim();
            accessibilityBefore = shell("settings get secure accessibility_enabled").trim();
            serviceFixtureUsed = true;
        }
    }
    private void awaitServiceDestroyed() {
        assertNotNull("The actual service lifecycle observer was not prepared", createdServiceField);
        await(() -> {
            AtomicBoolean destroyed = new AtomicBoolean();
            main(() -> {
                try {
                    Object value = createdServiceField.get(null);
                    assertTrue("Own created lifecycle marker changed type", value instanceof WeakReference<?>);
                    // MAIN cannot interleave this read with onDestroy. Reading its
                    // cleared marker here observes the end of that real callback;
                    // current()==null alone would observe only onUnbind/disconnect.
                    destroyed.set(((WeakReference<?>) value).get() == null && ReferenceCaptureService.current() == null
                            && !OriginalServiceState.isRunning(ReferenceApplication.app()));
                } catch (IllegalAccessException failure) { throw new AssertionError("Cannot observe own service destruction", failure); }
            });
            return destroyed.get();
        });
    }
    private void ownService(boolean enabled) throws Exception {
        ComponentName own = new ComponentName(ReferenceApplication.app(), ReferenceCaptureService.class); String current = shell("settings get secure enabled_accessibility_services").trim(); List<String> values = new ArrayList<>();
        if (!current.isEmpty() && !current.equals("null")) for (String value : current.split(":")) if (!own.equals(ComponentName.unflattenFromString(value))) values.add(value);
        if (enabled) values.add(own.flattenToString()); shell("settings put secure enabled_accessibility_services " + quote(String.join(":", values))); if (enabled) shell("settings put secure accessibility_enabled 1");
    }
    private void setOverlayMode(String mode, Boolean expected) throws Exception { assertTrue(Arrays.asList("allow", "deny", "ignore", "default", "foreground").contains(mode)); shell("appops set --user current " + quote(packageName()) + " SYSTEM_ALERT_WINDOW " + mode); assertEquals(mode, readOverlayMode()); if (expected != null) await(() -> OverlayPermission.granted() == expected); }
    private String readOverlayMode() throws Exception { String value = shell("appops get --user current " + quote(packageName()) + " SYSTEM_ALERT_WINDOW"); Matcher match = Pattern.compile("SYSTEM_ALERT_WINDOW:\\s*(allow|deny|ignore|default|foreground)\\b").matcher(value); if (match.find()) return match.group(1); assertTrue(value, value.contains("No operations")); return "default"; }
    private String shell(String command) throws Exception { return ReferenceShell.run(automation, command); }
    private static String quote(String value) { if (!value.matches("[A-Za-z0-9_./:$-]*")) throw new IllegalArgumentException("Invalid cloud fixture token"); return "'" + value + "'"; }
    private void restorePreferences() {
        if (preferences == null) return; SharedPreferences.Editor edit = ReferenceApplication.prefs().edit().clear();
        for (Map.Entry<String, ?> entry : preferences.entrySet()) { Object value = entry.getValue(); String key = entry.getKey();
            if (value instanceof Boolean b) edit.putBoolean(key, b); else if (value instanceof String s) edit.putString(key, s); else if (value instanceof Long l) edit.putLong(key, l);
            else if (value instanceof Integer i) edit.putInt(key, i); else if (value instanceof Float f) edit.putFloat(key, f); else if (value instanceof Set<?> set) { Set<String> strings = new HashSet<>(); for (Object item : set) strings.add((String) item); edit.putStringSet(key, strings); }
            else throw new AssertionError("Unexpected preference type");
        } assertTrue(edit.commit());
    }
    private final class LocalEffects {
        private final int settingsStarts = actualSettingsStarts.get();
        private final Map<String, ?> stored = new HashMap<>(ReferenceApplication.prefs().getAll());
        private final boolean saw = OverlayPermission.granted();
        void assertUnchanged() {
            assertEquals("Local guidance started a Settings destination directly or through its injected launcher", settingsStarts, actualSettingsStarts.get());
            assertEquals("Local guidance changed stored state", stored, ReferenceApplication.prefs().getAll());
            assertEquals("Local guidance changed actual SAW", saw, OverlayPermission.granted());
        }
    }
    private static String text(View root, String id) { return ((TextView) Ui.view(root, id)).getText().toString(); }
    // Original p0.b DEX uses system Resources density, not Activity/app density.
    // Evidence: evidence/behavior-baseline/auto-work-dp-exact-recovery.json; not predicted via Ui.dp.
    private static int dp(int value) { return (int) (Resources.getSystem().getDisplayMetrics().density * value + 0.5f); }
    private static void assertDetails(Intent intent) { assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.getAction()); assertEquals("package:" + packageName(), intent.getDataString()); assertEquals(0, intent.getFlags()); assertNull(intent.getComponent()); assertNull(intent.getExtras()); }
    private static void assertSimple(Intent intent, String action) { assertEquals(action, intent.getAction()); assertEquals(0, intent.getFlags()); assertNull(intent.getData()); assertNull(intent.getComponent()); assertNull(intent.getExtras()); assertNull(intent.getCategories()); }
    private void assertWindow(Dialog dialog, int marginDp) { int width = ((WindowManager) ReferenceApplication.app().getSystemService(android.content.Context.WINDOW_SERVICE)).getCurrentWindowMetrics().getBounds().width(); assertEquals(width - dp(marginDp), dialog.getWindow().getAttributes().width); assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, dialog.getWindow().getAttributes().height); }
    private static boolean hasText(View root, String text) { if (root instanceof TextView view && text.contentEquals(view.getText())) return true; if (root instanceof ViewGroup group) for (int i = 0; i < group.getChildCount(); i++) if (hasText(group.getChildAt(i), text)) return true; return false; }
    private boolean focusedPackage(String name) {
        List<AccessibilityWindowInfo> windows = automation.getWindows();
        try { for (AccessibilityWindowInfo info : windows) {
            if (!info.isActive() || !info.isFocused()) continue;
            AccessibilityNodeInfo root = info.getRoot();
            if (root != null) try { if (name.contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) return true; }
            finally { root.recycle(); }
        } return false; }
        finally { for (AccessibilityWindowInfo info : windows) info.recycle(); }
    }
    private Rect activeBounds(String id) { return activeNode(id, null); }
    private boolean activeText(String value) { return activeNode(null, value) != null; }
    private void assertVisibleGuideOrder(String[][] rows) {
        AccessibilityNodeInfo root = automation.getRootInActiveWindow(); assertNotNull(root); List<int[]> visible = new ArrayList<>();
        try {
            for (int i = 0; i < rows.length; i++) {
                List<AccessibilityNodeInfo> matches = root.findAccessibilityNodeInfosByText(rows[i][0]);
                try { for (AccessibilityNodeInfo node : matches) if (node.isVisibleToUser() && rows[i][0].contentEquals(node.getText() == null ? "" : node.getText())) {
                    Rect bounds = new Rect(); node.getBoundsInScreen(bounds); if (!bounds.isEmpty()) visible.add(new int[]{i, bounds.centerY()});
                } } finally { for (AccessibilityNodeInfo node : matches) node.recycle(); }
            }
        } finally { root.recycle(); }
        visible.sort((first, second) -> Integer.compare(first[1], second[1]));
        for (int i = 1; i < visible.size(); i++) assertTrue("Local guide order differs from the original fixed five rows", visible.get(i - 1)[0] < visible.get(i)[0]);
    }
    private Rect activeNode(String id, String text) {
        AccessibilityNodeInfo root = automation.getRootInActiveWindow(); if (root == null) return null;
        try { List<AccessibilityNodeInfo> nodes = id == null ? root.findAccessibilityNodeInfosByText(text) : root.findAccessibilityNodeInfosByViewId(ReferenceApplication.app().getResources().getResourceName(Ui.id(ReferenceApplication.app(), id)));
            try { for (AccessibilityNodeInfo node : nodes) { if (!node.isVisibleToUser() || text != null && !text.contentEquals(node.getText() == null ? "" : node.getText())) continue; Rect bounds = new Rect(); node.getBoundsInScreen(bounds); if (!bounds.isEmpty()) return bounds; } }
            finally { for (AccessibilityNodeInfo node : nodes) node.recycle(); }
        } finally { root.recycle(); } return null;
    }
    private void swipeActiveList() { Rect bounds = activeBounds("tip_list"); assertNotNull(bounds); assertTrue(bounds.height() > 100); assertTrue(device.swipe(bounds.centerX(), bounds.top + bounds.height() * 3 / 4, bounds.centerX(), bounds.top + bounds.height() / 4, 12)); }
    private static void main(Runnable action) { InstrumentationRegistry.getInstrumentation().runOnMainSync(action); }
    private static String packageName() { return ReferenceApplication.app().getPackageName(); }
    private void await(BooleanSupplier check) { await(7000, check); }
    private void await(long millis, BooleanSupplier check) { long end = SystemClock.uptimeMillis() + millis; while (SystemClock.uptimeMillis() < end) { if (check.getAsBoolean()) return; SystemClock.sleep(25); } assertTrue("Actual AutoWork state did not settle; " + ReferenceCaptureService.status(), check.getAsBoolean()); }
}
