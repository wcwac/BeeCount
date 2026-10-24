package com.wangc.bill.reference;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.TextView;
import android.widget.CompoundButton;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.wangc.bill.reference.capture.AutoApps;
import com.wangc.bill.reference.capture.ReferenceCaptureService;
import com.wangc.bill.reference.capture.ReferenceDiagnostics;
import com.wangc.bill.reference.capture.YimuKernel;
import com.wangc.bill.reference.ui.CaptureOverlay;
import com.wangc.bill.reference.ui.MainActivity;
import com.wangc.bill.reference.ui.ManagerActivity;
import com.wangc.bill.reference.ui.EditorActivity;
import com.wangc.bill.reference.ui.QuickEntryWindow;
import com.wangc.bill.reference.ui.Ui;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real service -> own synthetic payment windows -> SAW touch -> persisted accounting results.
 * These are functional tests, not execution of the original APK or current payment-app compatibility. */
@RunWith(AndroidJUnit4.class)
public final class FunctionalCaptureDeviceTest {
    private static final String ALIPAY = "com.eg.android.AlipayGphone";
    private static final String WECHAT = "com.tencent.mm";
    private static final String MEITUAN = "com.sankuai.meituan";
    private static final String ALIPAY_PAGE = ALIPAY + "/com.alipay.android.msp.ui.views.MspContainerActivity";
    private static final String WECHAT_PAGE = WECHAT + "/com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI";
    private static final String MEITUAN_PAGE = MEITUAN + "/com.meituan.android.mrn.container.MRNBaseActivity";
    private UiAutomation automation;
    private ReferenceStore store;
    private JSONObject backup;
    private Map<String, ?> preferences;
    private String priorServices, priorEnabled, priorOverlay;
    private String[] priorObserverPackages;
    private int priorObserverFlags;
    private boolean observerSaved;
    private long book, account;

    @Rule public final TestWatcher failureMetadata = new TestWatcher() {
        @Override protected void failed(Throwable failure, Description description) {
            failure.addSuppressed(new AssertionError("Functional capture metadata: overlay=" + CaptureOverlay.status()
                    + "; diagnostics=" + ReferenceDiagnostics.snapshot()));
        }
    };

    @Before public void open() throws Exception {
        assertEquals("true", InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        assertEquals(36, Build.VERSION.SDK_INT);
        assertTrue(Build.HARDWARE.equals("ranchu") || Build.HARDWARE.equals("goldfish"));
        assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        assertEquals("Main regression must be granted outside instrumentation", PackageManager.PERMISSION_GRANTED,
                ReferenceApplication.app().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS));
        automation = InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        preferences = new HashMap<>(ReferenceApplication.prefs().getAll());
        priorServices = shell("settings get secure enabled_accessibility_services").trim();
        priorEnabled = shell("settings get secure accessibility_enabled").trim(); priorOverlay = overlayMode();
        AccessibilityServiceInfo observer = automation.getServiceInfo();
        priorObserverPackages = observer.packageNames == null ? null : observer.packageNames.clone(); priorObserverFlags = observer.flags;
        observerSaved = true;
        // Do not subscribe this observer to payment packages and compete with FLAG_DEFAULT.
        observer.packageNames = new String[]{ReferenceApplication.app().getPackageName()};
        observer.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        automation.setServiceInfo(observer);
        AutoApps.enable(false); main(() -> { CaptureOverlay.closeForRestore(); QuickEntryWindow.setEnabled(false); }); barrier(); stopFixtures();
        store = ReferenceApplication.store(); backup = store.backup();
        book = store.saveBook(0, "功能采集验收账本", "CNY", 0);
        account = new OriginalAssetCommands(store, ReferenceStore.LOCAL_OFFLINE_OWNER).createFund("功能采集资金账户", null, 100);
        ReferenceApplication.prefs().edit().putLong("current_book", book).putLong("capture_book", book)
                .putLong("default_account", account).putBoolean("show_overlay", true).putBoolean("auto_save", false)
                .putBoolean("use_active_window", false).putBoolean("add_remark", true).putBoolean("remember_merchants", false)
                .putStringSet("enabled_packages", new HashSet<>(Arrays.asList(ALIPAY, WECHAT, MEITUAN))).commit();
        shell("appops set --user current " + quote(ReferenceApplication.app().getPackageName()) + " SYSTEM_ALERT_WINDOW allow");
        await(() -> Settings.canDrawOverlays(ReferenceApplication.app()), "SAW was not granted");
        String own = new ComponentName(ReferenceApplication.app(), ReferenceCaptureService.class).flattenToString();
        Set<String> services = new java.util.LinkedHashSet<>();
        if (!priorServices.equals("null") && !priorServices.trim().isEmpty()) services.addAll(Arrays.asList(priorServices.split(":")));
        services.add(own); shell("settings put secure enabled_accessibility_services " + quote(String.join(":", services)));
        shell("settings put secure accessibility_enabled 1");
        await(() -> ReferenceCaptureService.current() != null, "Capture service did not connect");
        AutoApps.enable(true); barrier(); ReferenceDiagnostics.clear();
    }

    @After public void restore() throws Exception {
        if (preferences == null) return;
        try {
            AutoApps.enable(false); main(() -> { CaptureOverlay.closeForRestore(); QuickEntryWindow.setEnabled(false); }); barrier();
            // A running NodeReader must finish before its remote fixture process dies.
            stopFixtures();
            if (backup != null) {
                FutureTask<Void> reset = new FutureTask<>(() -> { store.restore(backup); YimuKernel.resetAfterRestore(); return null; });
                YimuKernel.enqueue(reset); reset.get(10, TimeUnit.SECONDS);
            }
        } finally {
            try { restorePreferences(); }
            finally {
                try { restoreSecure("enabled_accessibility_services", priorServices); }
                finally {
                    try { restoreSecure("accessibility_enabled", priorEnabled); }
                    finally {
                        try { if (priorOverlay != null) shell("appops set --user current " + quote(ReferenceApplication.app().getPackageName())
                                + " SYSTEM_ALERT_WINDOW " + quote(priorOverlay)); }
                        finally { try { main(QuickEntryWindow::refresh); }
                            finally { if (observerSaved) { AccessibilityServiceInfo observer = automation.getServiceInfo();
                                observer.packageNames = priorObserverPackages; observer.flags = priorObserverFlags; automation.setServiceInfo(observer); } } }
                    }
                }
            }
        }
    }

    @Test public void aRealAmountLeafEventReadsTheWholePaymentWindowAndManualSaveDeductsOnce() throws Exception {
        ReferenceApplication.prefs().edit().putBoolean("auto_save", true).commit();
        Set<String> before = candidates();
        launch(ALIPAY_PAGE, "31.40", " --ez fixture_leaf_source true");
        JSONObject draft = draft(ALIPAY, before); JSONObject data = Json.parse(draft.optString("payload"));
        assertEquals(3140, data.optLong("amount")); assertEquals("实验支付宝商户", data.optString("merchant"));
        assertEquals("余额宝", data.optString("asset_text")); assertEquals(account, data.optLong("account_id"));
        assertTrue("A weak page must require an explicit user decision", data.optBoolean("capture_requires_confirmation"));
        assertFullWindowDraft(); assertWindow("31.40", "实验支付宝商户");
        assertFalse("Weak evidence must not start auto-save", CaptureOverlay.status().optBoolean("countdownActive"));
        save(draft.optString("id")); assertSaved(draft.optString("id"), 3140, 68.60d);
        assertHomeRecord("实验支付宝商户", "-31.40");
    }

    @Test public void aMeituanPaymentDetailCreatesAVisibleConfirmationAndSavesItsMerchantTimeAndAccount() throws Exception {
        Set<String> before = candidates(); launch(MEITUAN_PAGE, "27.80", "");
        JSONObject draft = draft(MEITUAN, before); JSONObject data = Json.parse(draft.optString("payload"));
        assertEquals(2780, data.optLong("amount")); assertEquals("功能验收同一商户", data.optString("merchant"));
        assertEquals("美团", data.optString("origin")); assertEquals("微信支付", data.optString("asset_text"));
        assertTrue(data.optLong("original_time") > 0); assertEquals(0, data.optLong("original_time") % 1000);
        assertEquals(account, data.optLong("account_id")); assertFullWindowDraft(); assertWindow("27.80", "功能验收同一商户");
        save(draft.optString("id")); assertSaved(draft.optString("id"), 2780, 72.20d);
        JSONObject row = store.transaction(store.candidate(draft.optString("id")).optLong("transaction_id"));
        assertEquals(data.optLong("original_time"), row.optLong("time")); assertHomeRecord("功能验收同一商户", "-27.80");
    }

    @Test public void equalPaymentsAtTheSameCapturedSecondFromWechatAndMeituanRemainTwoTransactions() throws Exception {
        Set<String> before = candidates(); launch(WECHAT_PAGE, "10.00", " --ez fixture_detail true --ez fixture_single_event true");
        JSONObject wechat = draft(WECHAT, before); JSONObject first = Json.parse(wechat.optString("payload"));
        assertEquals(1000, first.optLong("amount")); assertTrue(first.optLong("original_time") > 0);
        assertWindow("10.00", "功能验收同一商户"); save(wechat.optString("id"));
        assertSaved(wechat.optString("id"), 1000, 90d); closeAnyWeakRepaint();
        before = candidates(); launch(MEITUAN_PAGE, "10.00", "");
        JSONObject meituan = draft(MEITUAN, before); JSONObject second = Json.parse(meituan.optString("payload"));
        assertNotEquals(wechat.optString("id"), meituan.optString("id"));
        assertEquals(first.optLong("original_time"), second.optLong("original_time"));
        assertEquals(first.optLong("amount"), second.optLong("amount"));
        assertEquals(first.optString("merchant"), second.optString("merchant"));
        assertWindow("10.00", "功能验收同一商户"); save(meituan.optString("id"));
        assertSaved(meituan.optString("id"), 1000, 80d);
        assertEquals(2, store.scalar("SELECT COUNT(*) n FROM transactions WHERE book_id=? AND deleted=0", "" + book));
        assertEquals(2000, store.totals(book, 0, Long.MAX_VALUE).optLong("pay"));
        assertNotEquals(store.candidate(wechat.optString("id")).optLong("transaction_id"), store.candidate(meituan.optString("id")).optLong("transaction_id"));
        // A weak repaint may remain pending; it cannot silently become another debit.
        assertEquals(80d, store.assetRepository().byRowId(account).optDouble("asset_number"), 0.0000001d);
    }

    @Test public void theIndependentQuickEntrySwitchAndBallOpenTheRealEditorAndSaveWithoutCapture() throws Exception {
        AutoApps.enable(false); barrier(); assertFalse(AutoApps.enabled());
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(EditorActivity.class.getName(), null, false);
        Activity[] opened = {null};
        try (ActivityScenario<ManagerActivity> scenario = ActivityScenario.launch(
                new Intent(ReferenceApplication.app(), ManagerActivity.class).putExtra("page", "quick_entry"))) {
            Point[] toggle = {null};
            await(() -> { scenario.onActivity(activity -> {
                View button = described(activity.getWindow().getDecorView(), "显示快捷记账悬浮球");
                Rect local = new Rect();
                if (button instanceof CompoundButton check && !check.isChecked() && button.isShown()
                        && activity.hasWindowFocus() && button.isLaidOut() && button.getLocalVisibleRect(local)
                        && local.contains(button.getWidth()/2, button.getHeight()/2)) toggle[0] = ReferenceWindowGeometry.center(button);
            }); return toggle[0] != null; }, "No real disabled QuickEntry setting switch");
            touch(toggle[0]);
            await(() -> QuickEntryWindow.enabled() && QuickEntryWindow.attached()
                    && systemPointByDescription("打开快捷记账") != null, "The actual switch did not attach its independent ball");
            assertFalse(AutoApps.enabled()); Point[] ball = {null};
            await(() -> (ball[0] = systemPointByDescription("打开快捷记账")) != null, "The actual QuickEntry ball is not touchable");
            touch(ball[0]);
            opened[0] = instrumentation.waitForMonitorWithTimeout(monitor, 7000);
            assertTrue("The real ball did not launch EditorActivity", opened[0] instanceof EditorActivity);
            EditorActivity editor = (EditorActivity) opened[0];
            await(() -> { boolean[] ready = {false}; main(() -> {
                TextView asset = editor.findViewById(Ui.id(editor, "asset_name"));
                ready[0] = editor.hasWindowFocus() && asset != null && asset.isShown()
                        && "功能采集资金账户".contentEquals(asset.getText());
            }); return ready[0]; }, "The actual shortcut editor did not select the configured account");
            for (String key : new String[]{"six", "point", "two", "five"}) touchEditorKey(editor, key);
            String[] displayed = {null}; main(() -> displayed[0] = ((TextView) editor.findViewById(Ui.id(editor, "cost"))).getText().toString());
            assertEquals("6.25", displayed[0]);
            touchEditorKey(editor, "cancel"); // NumberKeyboard's real 完成 control.
            await(() -> store.scalar("SELECT COUNT(*) n FROM transactions WHERE book_id=?", "" + book) == 1,
                    "The real shortcut Editor completion did not save");
            JSONObject saved = store.one("SELECT * FROM transactions WHERE book_id=?", "" + book);
            assertEquals(625, saved.optLong("amount")); assertEquals(account, saved.optLong("account_id"));
            assertEquals("expense", saved.optString("kind"));
            assertEquals(93.75d, store.assetRepository().byRowId(account).optDouble("asset_number"), 0.0000001d);
            assertEquals(0, store.scalar("SELECT COUNT(*) n FROM candidates WHERE book_id=?", "" + book));
            assertFalse(AutoApps.enabled());
        } finally {
            instrumentation.removeMonitor(monitor);
            if (opened[0] != null) main(() -> { if (!opened[0].isFinishing()) opened[0].finish(); });
        }
    }

    private void assertFullWindowDraft() {
        assertTrue("The durable draft must come from a full matching window", Arrays.asList("ACTIVE_ROOT", "INTERACTIVE_WINDOW")
                .contains(ReferenceDiagnostics.snapshot().optString("lastDraftSourceRoute")));
    }
    private void assertWindow(String amount, String merchant) throws Exception {
        await(() -> CaptureOverlay.status().optBoolean("attached") && overlayText("remark").equals(merchant)
                && overlayText("asset_name").equals("功能采集资金账户") && overlayPoint("btn_save") != null,
                "No usable real confirmation with its selected account and merchant");
        assertEquals(0, new BigDecimal(amount).compareTo(new BigDecimal(overlayText("cost"))));
    }
    private void save(String id) throws Exception {
        touch(waitPoint("btn_save"));
        await(() -> "SAVED".equals(store.candidate(id).optString("state")) || overlayPoint("confirm") != null,
                "The real save touch neither saved nor opened duplicate confirmation");
        if (!"SAVED".equals(store.candidate(id).optString("state"))) touch(waitPoint("confirm"));
        await(() -> "SAVED".equals(store.candidate(id).optString("state")), "The actual overlay worker did not commit its candidate");
        barrier(); closeAnyWeakRepaint();
    }
    private void assertSaved(String id, long amount, double balance) {
        JSONObject candidate = store.candidate(id); JSONObject row = store.transaction(candidate.optLong("transaction_id"));
        assertNotNull(row); assertEquals(book, row.optLong("book_id")); assertEquals(account, row.optLong("account_id"));
        assertEquals(amount, row.optLong("amount")); assertEquals("expense", row.optString("kind"));
        assertEquals(balance, store.assetRepository().byRowId(account).optDouble("asset_number"), 0.0000001d);
        assertEquals(0, ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    private void closeAnyWeakRepaint() throws Exception {
        if (!CaptureOverlay.status().optBoolean("attached")) return;
        assertFalse("A weak repaint must never auto-save", CaptureOverlay.status().optBoolean("countdownActive"));
        for (JSONObject row : store.query("SELECT * FROM candidates WHERE book_id=? AND state='PENDING'", "" + book))
            assertTrue(Json.parse(row.optString("payload")).optBoolean("capture_requires_confirmation"));
        touch(waitPoint("close_btn"));
        await(() -> !CaptureOverlay.status().optBoolean("attached"), "Explicitly cancelling the weak repaint did not close its window");
    }
    private void assertHomeRecord(String merchant, String amount) throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(new Intent(ReferenceApplication.app(), MainActivity.class))) {
            await(() -> { boolean[] ready = {false}; scenario.onActivity(activity -> ready[0] = activity.hasWindowFocus()
                    && visibleText(activity.getWindow().getDecorView(), merchant) && visibleText(activity.getWindow().getDecorView(), amount));
                return ready[0]; }, "The saved transaction is not visible on the actual home screen");
        }
    }
    private boolean visibleText(View view, String value) {
        if (view instanceof TextView text && text.isShown() && text.isAttachedToWindow() && value.contentEquals(text.getText())) return true;
        if (view instanceof ViewGroup group) for (int i = 0; i < group.getChildCount(); i++) if (visibleText(group.getChildAt(i), value)) return true;
        return false;
    }
    private Set<String> candidates() { Set<String> ids = new HashSet<>(); for (JSONObject row : store.query("SELECT id FROM candidates")) ids.add(row.optString("id")); return ids; }
    private JSONObject draft(String pkg, Set<String> before) throws Exception {
        JSONObject[] found = {null};
        await(() -> { for (JSONObject row : store.query("SELECT * FROM candidates WHERE package_name=? AND book_id=? ORDER BY created,id", pkg, "" + book))
            if (!before.contains(row.optString("id"))) { found[0] = row; return true; } return false; }, "No durable candidate from " + pkg);
        barrier(); return found[0];
    }
    private void launch(String component, String amount, String flags) throws Exception {
        shell("am start -W -S -n " + quote(component) + " --es fixture_amount " + quote(amount) + flags);
    }
    private String overlayText(String name) { String value = overlayValue(name, node -> node.getText() == null ? "" : node.getText().toString()); return value == null ? "" : value; }
    private Point overlayPoint(String name) {
        return overlayValue(name, node -> { if (!node.isVisibleToUser() || !node.isEnabled()) return null;
            Rect bounds = new Rect(); node.getBoundsInScreen(bounds); return bounds.isEmpty() ? null : new Point(bounds.centerX(), bounds.centerY()); });
    }
    private Point systemPointByDescription(String description) {
        return systemValue(description, true, node -> { if (!node.isVisibleToUser() || !node.isEnabled()) return null;
            Rect bounds = new Rect(); node.getBoundsInScreen(bounds); return bounds.isEmpty() ? null : new Point(bounds.centerX(), bounds.centerY()); });
    }
    private void touchEditorKey(EditorActivity editor, String name) throws Exception {
        Point[] point = {null};
        await(() -> { main(() -> { View view = editor.findViewById(Ui.id(editor, name)); Rect local = new Rect();
            if (view != null && view.isShown() && view.isEnabled() && editor.hasWindowFocus() && view.isLaidOut()
                    && view.getLocalVisibleRect(local) && local.contains(view.getWidth()/2, view.getHeight()/2))
                point[0] = ReferenceWindowGeometry.center(view); }); return point[0] != null; }, "No real Editor keyboard control " + name);
        touch(point[0]);
    }
    private View described(View view, String value) {
        if (value.contentEquals(view.getContentDescription() == null ? "" : view.getContentDescription())) return view;
        if (view instanceof ViewGroup group) for (int i = 0; i < group.getChildCount(); i++) {
            View found = described(group.getChildAt(i), value); if (found != null) return found;
        }
        return null;
    }
    private Point waitPoint(String name) throws Exception { Point[] point = {null}; await(() -> (point[0] = overlayPoint(name)) != null, "No visible overlay control " + name); return point[0]; }
    private <T> T overlayValue(String name, Function<AccessibilityNodeInfo, T> action) {
        return systemValue(name, false, action);
    }
    private <T> T systemValue(String name, boolean description, Function<AccessibilityNodeInfo, T> action) {
        List<AccessibilityWindowInfo> windows = automation.getWindows();
        try {
            for (AccessibilityWindowInfo window : windows) {
                if (window.getType() != AccessibilityWindowInfo.TYPE_SYSTEM) continue;
                AccessibilityNodeInfo root = window.getRoot(); if (root == null) continue;
                if (root.getPackageName() == null || !ReferenceApplication.app().getPackageName().contentEquals(root.getPackageName())) { root.recycle(); continue; }
                ArrayDeque<AccessibilityNodeInfo> pending = new ArrayDeque<>(); pending.add(root);
                try {
                    while (!pending.isEmpty()) {
                        AccessibilityNodeInfo node = pending.removeFirst();
                        try {
                            String resource = node.getViewIdResourceName();
                            boolean matches = description ? name.contentEquals(node.getContentDescription() == null ? "" : node.getContentDescription())
                                    : resource != null && resource.endsWith(":id/" + name);
                            if (matches) { T result = action.apply(node); if (result != null) return result; }
                            for (int i = 0; i < node.getChildCount(); i++) { AccessibilityNodeInfo child = node.getChild(i); if (child != null) pending.add(child); }
                        } finally { node.recycle(); }
                    }
                } finally { while (!pending.isEmpty()) pending.removeFirst().recycle(); }
            }
            return null;
        } finally { for (AccessibilityWindowInfo window : windows) window.recycle(); }
    }
    private void touch(Point point) throws Exception {
        long began = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(began, began, MotionEvent.ACTION_DOWN, point.x, point.y, 0); down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try { assertTrue(automation.injectInputEvent(down, true)); } finally { down.recycle(); }
        MotionEvent up = MotionEvent.obtain(began, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, point.x, point.y, 0); up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try { assertTrue(automation.injectInputEvent(up, true)); } finally { up.recycle(); }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
    private void stopFixtures() throws Exception { for (String pkg : Arrays.asList(ALIPAY, WECHAT, MEITUAN)) shell("am force-stop " + quote(pkg)); }
    private void main(Runnable action) { InstrumentationRegistry.getInstrumentation().runOnMainSync(action); }
    private void barrier() throws Exception { FutureTask<Void> task = new FutureTask<>(() -> null); YimuKernel.enqueue(task); task.get(10, TimeUnit.SECONDS); main(() -> {}); }
    private String shell(String command) throws Exception { return ReferenceShell.run(automation, command); }
    private static String quote(String value) { return "'" + value.replace("'", "'\\''") + "'"; }
    private String overlayMode() throws Exception {
        String value = shell("appops get --user current " + quote(ReferenceApplication.app().getPackageName()) + " SYSTEM_ALERT_WINDOW");
        java.util.regex.Matcher match = java.util.regex.Pattern.compile("SYSTEM_ALERT_WINDOW(?:\\s*\\([^)]*\\))?:\\s*(allow|ignore|deny|default|foreground)").matcher(value);
        if (match.find()) return match.group(1); if (value.contains("No operations")) return "default";
        throw new AssertionError("Cannot identify initial overlay app-op");
    }
    private void restoreSecure(String key, String value) throws Exception {
        if (value != null) shell(value.equals("null") ? "settings delete secure " + key : "settings put secure " + key + " " + quote(value));
    }
    private void restorePreferences() {
        SharedPreferences.Editor edit = ReferenceApplication.prefs().edit().clear();
        for (Map.Entry<String, ?> entry : preferences.entrySet()) {
            Object value = entry.getValue(); String key = entry.getKey();
            if (value instanceof Boolean b) edit.putBoolean(key, b); else if (value instanceof Long l) edit.putLong(key, l);
            else if (value instanceof Integer i) edit.putInt(key, i); else if (value instanceof Float f) edit.putFloat(key, f);
            else if (value instanceof String s) edit.putString(key, s);
            else if (value instanceof Set<?> set) { Set<String> values = new HashSet<>(); for (Object item : set) values.add((String) item); edit.putStringSet(key, values); }
        }
        edit.commit();
    }
    private interface Condition { boolean ready() throws Exception; }
    private void await(Condition condition, String message) throws Exception {
        long until = SystemClock.uptimeMillis() + 7000;
        while (SystemClock.uptimeMillis() < until) { if (condition.ready()) return; SystemClock.sleep(35); }
        assertTrue(message + "; overlay=" + CaptureOverlay.status() + "; diagnostics=" + ReferenceDiagnostics.snapshot(), condition.ready());
    }
}
