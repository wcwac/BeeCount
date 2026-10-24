package com.wangc.bill.reference.ui;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.wangc.bill.reference.ReferenceApplication;
import com.wangc.bill.reference.ReferenceWindowGeometry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real attached original dialogs with supplied OEM inputs; not evidence of an OEM Settings installation. */
@RunWith(AndroidJUnit4.class)
public final class OriginalOemPermissionGuidanceDeviceTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private UiAutomation automation;
    private String[] priorObserverPackages;
    private ActivityScenario<ManagerActivity> scenario;
    private final AtomicReference<OriginalOemPermissionGuidance.GuidanceDialog> fragment = new AtomicReference<>();
    private final AtomicReference<Dialog> dialog = new AtomicReference<>();

    @Before public void open() {
        assertEquals("true", InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        assertEquals(36, Build.VERSION.SDK_INT);
        assertTrue(Build.HARDWARE.equals("ranchu") || Build.HARDWARE.equals("goldfish"));
        assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        automation = instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        AccessibilityServiceInfo observer = automation.getServiceInfo(); priorObserverPackages = observer.packageNames == null ? null : observer.packageNames.clone();
        observer.packageNames = new String[]{ReferenceApplication.app().getPackageName()}; automation.setServiceInfo(observer);
        scenario = ActivityScenario.launch(new Intent(ReferenceApplication.app(), ManagerActivity.class).putExtra("page", "settings"));
    }
    @After public void close() {
        try { if (scenario != null) scenario.close(); }
        finally {
            if (automation != null) {
                AccessibilityServiceInfo observer = automation.getServiceInfo(); observer.packageNames = priorObserverPackages; automation.setServiceInfo(observer);
            }
        }
    }

    @Test public void selectionUsesExactManufacturerThenOnlyTheOriginalFlymePropertyPredicate() {
        AtomicInteger reads = new AtomicInteger();
        assertEquals(OriginalOemPermissionGuidance.Kind.HUAWEI, OriginalOemPermissionGuidance.select("HUAWEI", 36, () -> { reads.incrementAndGet(); return null; }));
        assertEquals(OriginalOemPermissionGuidance.Kind.HONOR, OriginalOemPermissionGuidance.select("HONOR", 36, () -> { reads.incrementAndGet(); return null; }));
        assertEquals(OriginalOemPermissionGuidance.Kind.XIAOMI_34, OriginalOemPermissionGuidance.select("Xiaomi", 34, () -> { reads.incrementAndGet(); return null; }));
        assertEquals(OriginalOemPermissionGuidance.Kind.XIAOMI_SIMPLE, OriginalOemPermissionGuidance.select("Xiaomi", 35, () -> { reads.incrementAndGet(); return null; }));
        assertEquals(0, reads.get());
        assertEquals(OriginalOemPermissionGuidance.Kind.NONE, OriginalOemPermissionGuidance.select("huawei", 36, () -> "Android"));
        assertEquals(OriginalOemPermissionGuidance.Kind.NONE, OriginalOemPermissionGuidance.select("Meizu", 36, () -> null));
        assertEquals(OriginalOemPermissionGuidance.Kind.MEIZU, OriginalOemPermissionGuidance.select("other", 36, () -> "flyme 10"));
        assertEquals("FLYME", OriginalOemPermissionGuidance.normalizedDisplayId("  FLYME  "));
        assertNull(OriginalOemPermissionGuidance.normalizedDisplayId("  ")); assertNull(OriginalOemPermissionGuidance.normalizedDisplayId(null));
        AtomicInteger launches = new AtomicInteger();
        scenario.onActivity(host -> assertNull(OriginalOemPermissionGuidance.show(host, "other", 36, () -> null, (owner, intent) -> launches.incrementAndGet())));
        instrumentation.waitForIdleSync(); assertEquals(0, launches.get());
        scenario.onActivity(host -> assertNull(host.getSupportFragmentManager().findFragmentByTag("auto_tip")));
    }

    @Test public void huaweiConfirmsAfterDismissalAndTriesEveryExactComponentBeforeThePackageLauncher() {
        List<Intent> attempts = new ArrayList<>(); AtomicInteger packageLookups = new AtomicInteger(); AtomicBoolean dismissed = new AtomicBoolean(true);
        show("HUAWEI", 36, null, new OriginalOemPermissionGuidance.Launcher() {
            @Override public void launch(androidx.fragment.app.FragmentActivity host, Intent intent) {
                dismissed.set(dismissed.get() && !dialog.get().isShowing()); attempts.add(new Intent(intent));
                if (intent.getComponent() != null) throw new IllegalStateException("Synthetic missing Huawei component");
            }
            @Override public Intent packageIntent(androidx.fragment.app.FragmentActivity host, String name) {
                assertEquals("com.huawei.systemmanager", name); packageLookups.incrementAndGet(); return new Intent("synthetic.HUAWEI");
            }
        });
        assertOriginalInstructionNewlines(4);
        tap("confirm"); hidden(); assertTrue(dismissed.get()); assertEquals(5, attempts.size()); assertEquals(1, packageLookups.get());
        String[] components = {"com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity",
                "com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity", "com.huawei.systemmanager/.optimize.process.ProtectActivity",
                "com.huawei.systemmanager/.optimize.bootstart.BootStartActivity"};
        for (int i = 0; i < components.length; i++) assertComponentIntent(attempts.get(i), components[i]);
        assertEquals("synthetic.HUAWEI", attempts.get(4).getAction()); assertEquals(0, attempts.get(4).getFlags());
        assertNull(attempts.get(4).getStringExtra("packageName"));
    }

    @Test public void honorTriesItsOwnTwoEntriesAndSwallowsTheirLaunchExceptionsWithoutAGenericFallback() {
        List<Intent> attempts = new ArrayList<>();
        show("HONOR", 36, null, new OriginalOemPermissionGuidance.Launcher() {
            @Override public void launch(androidx.fragment.app.FragmentActivity host, Intent intent) {
                assertFalse(dialog.get().isShowing()); attempts.add(new Intent(intent)); throw new IllegalStateException("Synthetic missing Honor entry");
            }
            @Override public Intent packageIntent(androidx.fragment.app.FragmentActivity host, String name) {
                assertEquals("com.hihonor.systemmanager", name); return new Intent("synthetic.HONOR");
            }
        });
        assertOriginalInstructionNewlines(5);
        tap("confirm"); hidden(); assertEquals(2, attempts.size());
        assertComponentIntent(attempts.get(0), "com.hihonor.systemmanager/com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
        assertEquals("synthetic.HONOR", attempts.get(1).getAction()); scenario.onActivity(host -> assertFalse(host.isFinishing()));
    }

    @Test public void meizuShowsTheOriginalGuidanceAndItsConfirmOnlyDismisses() {
        AtomicInteger launches = new AtomicInteger(); show("other", 36, "FLYME 10", (host, intent) -> launches.incrementAndGet());
        assertWindowSize(); assertOriginalInstructionNewlines(2); tap("confirm"); hidden(); assertEquals(0, launches.get());
    }

    @Test public void androidFourteenXiaomiRestoresItsOriginalDialogAndItsSourceEmptyCallbackDoesNotNavigate() {
        AtomicInteger launches = new AtomicInteger(); show("Xiaomi", 34, null, (host, intent) -> launches.incrementAndGet());
        assertWindowSize(); fragment.set(null); dialog.set(null); scenario.recreate(); shown(); assertWindowSize();
        scenario.onActivity(host -> assertEquals("MIUI优化", ((android.widget.TextView) Ui.view(fragment.get().requireView(), "title")).getText().toString()));
        assertOriginalInstructionNewlines(8);
        tap("confirm"); hidden(); assertEquals(0, launches.get());
    }

    @Test public void otherXiaomiSdkUsesTheActualBatteryRequestIntentWithNoFlagsAndDoesNotGrantAnExemption() {
        PowerManager power = (PowerManager) ReferenceApplication.app().getSystemService(android.content.Context.POWER_SERVICE);
        boolean before = power.isIgnoringBatteryOptimizations(ReferenceApplication.app().getPackageName());
        IntentFilter filter = new IntentFilter("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"); filter.addDataScheme("package");
        Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor(filter, new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null), true);
        instrumentation.addMonitor(monitor);
        try {
            AtomicReference<Intent> request = new AtomicReference<>();
            show("Xiaomi", 36, null, (host, intent) -> {
                assertFalse(dialog.get().isShowing()); request.set(new Intent(intent)); host.startActivity(intent);
            });
            assertEquals(0, monitor.getHits()); tap("confirm"); hidden(); await(() -> monitor.getHits() == 1);
            assertEquals("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", request.get().getAction());
            assertEquals("package:" + ReferenceApplication.app().getPackageName(), request.get().getDataString());
            assertEquals(0, request.get().getFlags()); assertNull(request.get().getComponent());
            assertEquals(before, power.isIgnoringBatteryOptimizations(ReferenceApplication.app().getPackageName()));
        } finally { instrumentation.removeMonitor(monitor); }
    }

    @Test public void simpleXiaomiLaunchFailurePropagatesAfterTheOriginalDialogDismissal() {
        IllegalStateException original = new IllegalStateException("Synthetic Xiaomi launch failure"); AtomicInteger attempts = new AtomicInteger();
        show("Xiaomi", 33, null, (host, intent) -> { attempts.incrementAndGet(); assertFalse(dialog.get().isShowing()); throw original; });
        scenario.onActivity(host -> {
            try { Ui.view(fragment.get().requireView(), "confirm").performClick(); fail("Source MiuiSimple has no catch"); }
            catch (IllegalStateException actual) { assertSame(original, actual); }
        });
        hidden(); assertEquals(1, attempts.get());
    }

    @Test public void sourceOemDialogsAllowCancelBackAndOutsideTouchWithoutAnySettingsLaunch() {
        AtomicInteger launches = new AtomicInteger(); show("HUAWEI", 36, null, (host, intent) -> launches.incrementAndGet());
        assertWindowSize(); tap("cancel"); hidden();
        resetDialog(); show("HONOR", 36, null, (host, intent) -> launches.incrementAndGet());
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); hidden();
        resetDialog(); show("other", 36, "FLYME", (host, intent) -> launches.incrementAndGet());
        AtomicReference<Point> outside = new AtomicReference<>();
        scenario.onActivity(host -> outside.set(ReferenceWindowGeometry.outside(dialog.get(), host.getWindow().getDecorView())));
        touch(outside.get()); hidden(); assertEquals(0, launches.get());
    }

    private void show(String manufacturer, int sdk, String display, OriginalOemPermissionGuidance.Launcher launcher) {
        scenario.onActivity(host -> OriginalOemPermissionGuidance.show(host, manufacturer, sdk, () -> display, launcher)); shown();
    }
    private void resetDialog() {
        scenario.onActivity(host -> host.getSupportFragmentManager().executePendingTransactions()); fragment.set(null); dialog.set(null);
    }
    private void shown() {
        await(() -> { scenario.onActivity(host -> {
            androidx.fragment.app.Fragment value = host.getSupportFragmentManager().findFragmentByTag("auto_tip");
            if (value instanceof OriginalOemPermissionGuidance.GuidanceDialog current && current.isResumed() && current.getDialog() != null
                    && current.getDialog().isShowing() && current.getDialog().getWindow().getDecorView().hasWindowFocus()
                    && current.getView() != null && current.getView().isAttachedToWindow() && current.getView().isLaidOut()) {
                fragment.set(current); dialog.set(current.getDialog());
            }
        }); return dialog.get() != null; });
    }
    private void hidden() { await(() -> { AtomicBoolean visible = new AtomicBoolean(); scenario.onActivity(host -> visible.set(dialog.get().isShowing())); return !visible.get(); }); }
    private void assertWindowSize() {
        scenario.onActivity(host -> {
            assertEquals(OriginalAutoTipDialog.screenWidth(host) - Ui.dp(host, 40), dialog.get().getWindow().getAttributes().width);
            assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, dialog.get().getWindow().getAttributes().height);
            assertTrue(Ui.view(fragment.get().requireView(), "cancel").isShown()); assertTrue(Ui.view(fragment.get().requireView(), "confirm").isShown());
        });
    }
    private void assertOriginalInstructionNewlines(int expected) {
        // Counts come from the original APK's raw and typed AXML string pools:
        // Huawei4, Honor5, Meizu2, Miui8; not from this app's string resources.
        scenario.onActivity(host -> {
            View root = fragment.get().requireView(); assertTrue(root.isAttachedToWindow()); assertTrue(dialog.get().isShowing());
            List<android.widget.TextView> instructions = new ArrayList<>();
            collectInstructions(root, instructions);
            assertEquals("Exactly one original numbered instruction must be attached", 1, instructions.size());
            android.widget.TextView instruction = instructions.get(0);
            assertTrue(instruction.isAttachedToWindow()); assertTrue(instruction.isShown());
            String value = instruction.getText().toString(); int newlines = 0;
            for (int i = 0; i < value.length(); i++) if (value.charAt(i) == '\n') newlines++;
            assertEquals("Original U+000A count after resource compilation", expected, newlines);
            assertFalse("Newline escapes must not remain literal text", value.contains("\\n"));
            assertFalse("Decompiler slash10 must not remain literal text", value.contains("\\10"));
        });
    }
    private static void collectInstructions(View root, List<android.widget.TextView> result) {
        if (root instanceof android.widget.TextView text && text.getText().toString().startsWith("1.")) result.add(text);
        if (root instanceof ViewGroup group) for (int i = 0; i < group.getChildCount(); i++) collectInstructions(group.getChildAt(i), result);
    }
    private static void assertComponentIntent(Intent intent, String component) {
        assertEquals(ComponentName.unflattenFromString(component), intent.getComponent()); assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.getFlags());
        assertEquals(ReferenceApplication.app().getPackageName(), intent.getStringExtra("packageName")); assertNull(intent.getAction()); assertNull(intent.getData());
    }
    private void tap(String id) {
        AtomicReference<Point> point = new AtomicReference<>();
        scenario.onActivity(host -> point.set(ReferenceWindowGeometry.center(Ui.view(fragment.get().requireView(), id))));
        touch(point.get());
    }
    private void touch(Point point) {
        long time = SystemClock.uptimeMillis(); MotionEvent down = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, point.x, point.y, 0);
        MotionEvent up = MotionEvent.obtain(time, time + 60, MotionEvent.ACTION_UP, point.x, point.y, 0);
        try { down.setSource(InputDevice.SOURCE_TOUCHSCREEN); up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            assertTrue(automation.injectInputEvent(down, true)); assertTrue(automation.injectInputEvent(up, true)); }
        finally { down.recycle(); up.recycle(); }
        instrumentation.waitForIdleSync();
    }
    private void await(BooleanSupplier condition) {
        long until = SystemClock.uptimeMillis() + 5000;
        while (SystemClock.uptimeMillis() < until) { if (condition.getAsBoolean()) return; SystemClock.sleep(25); }
        assertTrue("Actual OEM guidance did not reach its expected window state", condition.getAsBoolean());
    }
}
