package com.wangc.bill.reference;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.wangc.bill.reference.capture.AutoApps;
import com.wangc.bill.reference.capture.ReferenceCaptureService;
import com.wangc.bill.reference.capture.ReferenceDiagnostics;
import com.wangc.bill.reference.capture.YimuKernel;
import com.wangc.bill.reference.ui.CaptureOverlay;
import com.wangc.bill.reference.ui.Ui;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real SAW window and worker saves, confined to the disposable authorized CI emulator. */
@RunWith(AndroidJUnit4.class)
public final class CaptureRefundOverlayDeviceTest {
    private UiAutomation automation;
    private Map<String, ?> preferences;
    private String priorServices, priorEnabled, priorOverlay;
    private String[] priorObserverPackages;
    private int priorObserverFlags;
    private JSONObject backup;
    private ReferenceStore store;
    private long book;

    @Before public void open() throws Exception {
        assertEquals("true", InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        assertEquals(36, Build.VERSION.SDK_INT);
        assertTrue(Build.HARDWARE.equals("ranchu") || Build.HARDWARE.equals("goldfish"));
        assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        automation = InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        preferences = new HashMap<>(ReferenceApplication.prefs().getAll());
        priorServices = shell("settings get secure enabled_accessibility_services").trim();
        priorEnabled = shell("settings get secure accessibility_enabled").trim();
        priorOverlay = overlayMode();
        AccessibilityServiceInfo observer = automation.getServiceInfo();
        priorObserverPackages = observer.packageNames == null ? null : observer.packageNames.clone(); priorObserverFlags = observer.flags;
        observer.packageNames = new String[]{ReferenceApplication.app().getPackageName()};
        observer.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        automation.setServiceInfo(observer);
        shell("appops set --user current " + token(ReferenceApplication.app().getPackageName()) + " SYSTEM_ALERT_WINDOW allow");
        await(() -> Settings.canDrawOverlays(ReferenceApplication.app()), "Overlay permission was not granted");
        AutoApps.enable(false); CaptureOverlay.closeForRestore(); barrier();
        store = ReferenceApplication.store(); backup = store.backup(); book = store.books().get(0).optLong("id");
        ReferenceApplication.prefs().edit().putBoolean("show_overlay", true).putBoolean("auto_save", false)
                .putLong("capture_book", book).putLong("default_account", 0)
                .putStringSet("enabled_packages", new HashSet<>(Arrays.asList("com.tencent.mm"))).commit();
        String component = ReferenceApplication.app().getPackageName() + "/com.wangc.bill.reference.capture.ReferenceCaptureService";
        shell("settings put secure enabled_accessibility_services " + token(component)); shell("settings put secure accessibility_enabled 1");
        await(() -> ReferenceCaptureService.current() != null, "Capture service did not connect");
        AutoApps.enable(true); barrier(); ReferenceDiagnostics.clear();
    }

    @After public void restore() throws Exception {
        if (preferences == null) return;
        try {
            AutoApps.enable(false); CaptureOverlay.closeForRestore(); barrier();
            if (backup != null) {
                FutureTask<Void> reset = new FutureTask<>(() -> { store.restore(backup); YimuKernel.resetAfterRestore(); return null; });
                YimuKernel.enqueue(reset); reset.get(10, TimeUnit.SECONDS);
            }
        } finally {
            SharedPreferences.Editor edit = ReferenceApplication.prefs().edit().clear();
            for (Map.Entry<String, ?> e : preferences.entrySet()) {
                Object v = e.getValue();
                if (v instanceof Boolean b) edit.putBoolean(e.getKey(), b); else if (v instanceof Long l) edit.putLong(e.getKey(), l);
                else if (v instanceof Integer i) edit.putInt(e.getKey(), i); else if (v instanceof Float f) edit.putFloat(e.getKey(), f);
                else if (v instanceof String s) edit.putString(e.getKey(), s);
                else if (v instanceof Set<?> set) { Set<String> values = new HashSet<>(); for (Object item : set) values.add((String) item); edit.putStringSet(e.getKey(), values); }
            }
            edit.commit();
            try {
                if (priorServices != null) shell(priorServices.equals("null") ? "settings delete secure enabled_accessibility_services" : "settings put secure enabled_accessibility_services " + token(priorServices));
            } finally {
                try {
                    if (priorEnabled != null) shell(priorEnabled.equals("null") ? "settings delete secure accessibility_enabled" : "settings put secure accessibility_enabled " + token(priorEnabled));
                } finally {
                    try {
                        if (priorOverlay != null) shell("appops set --user current " + token(ReferenceApplication.app().getPackageName()) + " SYSTEM_ALERT_WINDOW " + token(priorOverlay));
                    } finally {
                        AccessibilityServiceInfo observer = automation.getServiceInfo(); observer.packageNames = priorObserverPackages; observer.flags = priorObserverFlags; automation.setServiceInfo(observer);
                    }
                }
            }
        }
    }

    private OriginalBillRecord bill(int semantic, boolean income, double number, String remark) {
        OriginalBillRecord r = new OriginalBillRecord(); r.billId = semantic; r.userId = 0; r.assetId = -1;
        r.bookId = store.book(book).optLong("semantic_id"); r.parentCategoryId = income ? 9 : 99; r.childCategoryId = 0;
        r.cost = number; r.remark = remark; r.time = 1735689600000L;
        android.content.ContentValues p = new android.content.ContentValues(); p.put("book_id", book); p.put("kind", income ? "income" : "expense");
        p.put("amount", java.math.BigDecimal.valueOf(number).movePointRight(2).longValueExact()); p.put("currency", "CNY");
        p.put("category_id", store.categoryRepository().parent(r.parentCategoryId).optLong("id")); p.put("parent_semantic_id", r.parentCategoryId); p.put("child_semantic_id", 0);
        p.put("time", r.time); p.put("created", r.time); p.put("remark", remark);
        new OriginalBillRepository(store).saveOriginalRaw(r, p); return r;
    }
    private String candidate(long amount, String remark) {
        return candidate(amount, remark, 0);
    }
    private String candidate(long amount, String remark, long receivingAccount) {
        JSONObject data = Json.object("book_id", book, "kind", "income", "currency", "CNY", "amount", amount,
                "original_amount", amount, "original_number", Money.text(amount, "CNY"), "original_income", true,
                "original_remark", remark, "original_time", 1735689600000L, "original_currency", "CNY", "time", 1735689600000L,
                "remark", remark, "account_id", receivingAccount, "category_id", store.defaultCategory("income", book), "discount", 0, "fee", 0);
        return store.addCandidate(data, "com.tencent.mm", book);
    }
    private void show(String id) throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> CaptureOverlay.show(id, null));
        await(() -> node("btn_save") != null, "No real confirmation overlay");
        assertEquals(2038, CaptureOverlay.status().optInt("windowType"));
    }

    @Test public void explicitRefundTabKeepsTheCapturedParentAcrossEditsAndBookChoiceAndConfirmsBillRepeat() throws Exception {
        String rawRemark = "退款浮窗原始商户";
        OriginalBillRecord parent = bill(1900069501, false, 239.17d, rawRemark);
        OriginalBillRecord ordinaryRepeat = bill(1900069502, true, 239.17d, rawRemark);
        bill(1900069503, false, 1.0d, "用户改过备注");
        long otherBook = store.saveBook(0, "退款浮窗另一本", "CNY", 0);
        String id = candidate(23917, rawRemark); show(id);
        assertTrue(CaptureOverlay.status().optBoolean("repeatDetected"));
        setText("cost", "1.00"); setText("remark", "用户改过备注"); clickText("退款");
        await(() -> text("bill_remark").equals(rawRemark), "Refund matching used edited fields instead of the capture");
        assertEquals("已选择1条账单", text("bill_count")); assertEquals("PENDING", store.candidate(id).optString("state"));
        assertEquals(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL, node("cost").getInputType());
        assertEquals("1.00", text("cost"));
        clickId("account_book_layout"); clickText("退款浮窗另一本");
        awaitRefundBook("退款浮窗另一本", rawRemark);
        assertEquals("已选择1条账单", text("bill_count")); assertEquals(rawRemark, text("bill_remark"));
        long before = store.scalar("SELECT COUNT(*) n FROM transactions"); clickId("btn_save");
        await(() -> node("confirm") != null, "Repeated refund did not open the original confirmation popup");
        assertEquals("PENDING", store.candidate(id).optString("state")); clickId("confirm");
        await(() -> "SAVED".equals(store.candidate(id).optString("state")), "Refund candidate was not saved through its real worker path");
        barrier(); OriginalBillRepository bills = new OriginalBillRepository(store);
        assertEquals(238.17d, bills.byLocalRowId(parent.localRowId).cost, 0.0d);
        assertEquals(parent.bookId, bills.byLocalRowId(parent.localRowId).bookId);
        assertEquals(239.17d, bills.byLocalRowId(ordinaryRepeat.localRowId).cost, 0.0d);
        assertEquals(before, store.scalar("SELECT COUNT(*) n FROM transactions"));
        JSONObject saved = store.candidate(id); assertEquals("refund", saved.optString("save_kind"));
        assertEquals(parent.localRowId, saved.optLong("transaction_id")); assertEquals(otherBook, saved.optLong("book_id"));
        OriginalRefundRepository.Snapshot refund = new OriginalRefundRepository(store).firstByParent(0, parent.billId);
        assertNotNull(refund); assertEquals(1.0d, refund.data.refundNumber, 0.0d);
        OriginalRefundInfo info = OriginalRefundCodec.decodeStrict(refund.data.rawInfos.get(0));
        assertEquals("用户改过备注", info.remark); assertEquals(1.0d, info.number, 0.0d); assertEquals(0, info.inTime); assertEquals(0, info.assetId);
        await(() -> !CaptureOverlay.status().optBoolean("attached"), "Refund confirmation did not detach");
        assertEquals(0, ReferenceDiagnostics.snapshot().optLong("failures"));
    }

    @Test public void replacingTheSingleRepeatedBillPhysicallyDeletesItBeforeSavingTheRetainedRefundParent() throws Exception {
        String remark = "退款浮窗替换原始商户";
        OriginalBillRecord parent = bill(1900069511, false, 2.0d, remark);
        OriginalBillRecord repeat = bill(1900069512, true, 2.0d, remark);
        String id = candidate(200, remark); show(id); clickText("退款");
        await(() -> remark.equals(text("bill_remark")), "Refund parent was not selected"); clickId("btn_save");
        await(() -> node("confirm") != null, "Repeated refund replacement popup did not open");
        clickId("check_layout"); clickId("confirm");
        await(() -> "SAVED".equals(store.candidate(id).optString("state")), "Replacing the repeated Bill did not save the refund"); barrier();
        OriginalBillRepository bills = new OriginalBillRepository(store);
        assertNull(bills.byLocalRowId(repeat.localRowId)); assertEquals(0.0d, bills.byLocalRowId(parent.localRowId).cost, 0.0d);
        assertEquals(parent.localRowId, store.candidate(id).optLong("transaction_id")); assertEquals("refund", store.candidate(id).optString("save_kind"));
        assertEquals(2.0d, new OriginalRefundRepository(store).firstByParent(0, parent.billId).data.refundNumber, 0.0d);
        assertEquals(0, ReferenceDiagnostics.snapshot().optLong("failures"));
    }

    @Test public void unmatchedRefundLeavesItsCandidatePendingAndPreviewNeverWritesFinancialData() throws Exception {
        long before = store.scalar("SELECT COUNT(*) n FROM transactions"), refunds = store.scalar("SELECT COUNT(*) n FROM original_refunds");
        String id = candidate(88888888888L, "退款浮窗未匹配唯一备注"); show(id); clickText("退款");
        await(() -> "已选择0条账单".equals(text("bill_count")), "Unmatched refund page did not render"); clickId("btn_save");
        await(() -> "REFUND_PARENT_REQUIRED".equals(CaptureOverlay.status().optString("lastResult")), "Missing parent was not reported by the refund UI");
        assertEquals("PENDING", store.candidate(id).optString("state")); assertTrue(CaptureOverlay.status().optBoolean("attached"));
        assertEquals(before, store.scalar("SELECT COUNT(*) n FROM transactions")); assertEquals(refunds, store.scalar("SELECT COUNT(*) n FROM original_refunds"));
        // Source X0 retries after a prior miss whenever no parent is retained.
        OriginalBillRecord later = bill(1900069521, false, 888888888.88d, "退款浮窗未匹配唯一备注");
        clickText("收入"); clickText("退款");
        await(() -> later.remark.equals(text("bill_remark")), "Re-entering refunds did not retry a missing parent");
        assertEquals("已选择1条账单", text("bill_count")); assertEquals("PENDING", store.candidate(id).optString("state"));
        long withMatchFixture = before + 1;
        assertEquals(withMatchFixture, store.scalar("SELECT COUNT(*) n FROM transactions"));
        clickId("close_btn"); await(() -> !CaptureOverlay.status().optBoolean("attached"), "Refund cancellation did not detach");
        assertEquals("DISMISSED", store.candidate(id).optString("state"));
        long candidates = store.scalar("SELECT COUNT(*) n FROM candidates");
        InstrumentationRegistry.getInstrumentation().runOnMainSync(CaptureOverlay::preview);
        await(() -> node("btn_save") != null, "Preview window did not attach"); clickText("退款");
        await(() -> node("bill_count") != null, "Refund preview page did not render"); clickId("btn_save");
        await(() -> !CaptureOverlay.status().optBoolean("attached"), "Refund preview did not close"); barrier();
        assertEquals(candidates, store.scalar("SELECT COUNT(*) n FROM candidates")); assertEquals(withMatchFixture, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(refunds, store.scalar("SELECT COUNT(*) n FROM original_refunds")); assertEquals(0, ReferenceDiagnostics.snapshot().optLong("failures"));
    }

    @Test public void manualRefundPickerUsesTheGlobalBookAndChangesTheReceiverOnlyForAPositiveResolvedAsset() throws Exception {
        ReferenceApplication.prefs().edit().putLong("current_book", book).commit();
        long otherBook = store.saveBook(0, "退款手选另一账本", "CNY", 0);
        AssetRepository.Definition first = new AssetRepository.Definition("退款手选原账户", 1, null); first.assetNumber = 100d;
        AssetRepository.Definition second = new AssetRepository.Definition("退款手选已有账户", 1, null); second.assetNumber = 200d;
        long firstId = 1900069601L, secondId = 1900069602L;
        long firstRow = store.assetRepository().insertRecovered(0, firstId, 1, 1, first);
        long secondRow = store.assetRepository().insertRecovered(0, secondId, 2, 1, second);
        OriginalBillRepository bills = new OriginalBillRepository(store);
        OriginalBillRecord automatic = bill(1900069603, false, 5d, "退款手选自动匹配"); automatic.assetId = firstId;
        OriginalBillRecord income = bill(1900069604, true, 4d, "退款手选收入候选"); income.assetId = firstId;
        OriginalBillRecord zero = bill(1900069605, false, 3d, "退款手选零账户候选"); zero.assetId = 0;
        long recent = System.currentTimeMillis() + 60000;
        automatic.time = recent; income.time = recent; zero.time = recent;
        bills.saveOriginalRaw(automatic, null); bills.saveOriginalRaw(income, null);
        zero.bookId = store.book(otherBook).optLong("semantic_id");
        android.content.ContentValues moved = new android.content.ContentValues(); moved.put("book_id", otherBook);
        bills.saveOriginalRaw(zero, moved);
        String id = candidate(500, automatic.remark, secondRow); show(id); clickText("退款");
        await(() -> automatic.remark.equals(text("bill_remark")), "Automatic refund parent was not rendered");
        assertEquals("退款手选已有账户", text("asset_name")); // X0 does not call manual J0.
        clickId("account_book_layout"); clickText("退款手选另一账本");
        awaitRefundBook("退款手选另一账本", automatic.remark);
        clickId("total_layout"); clickContainingText(income.remark); clickText("确定");
        await(() -> income.remark.equals(text("bill_remark")), "Default picker used the header book instead of the global book");
        assertEquals("退款手选原账户", text("asset_name")); assertEquals("-", text("num_symbol"));
        clickId("total_layout"); clickText("所有账本"); clickContainingText(zero.remark); clickText("确定");
        await(() -> zero.remark.equals(text("bill_remark")), "All-book selection did not retain its typed parent");
        assertEquals("退款手选原账户", text("asset_name")); assertEquals("+", text("num_symbol"));
        setText("cost", "1.23"); clickId("btn_save");
        await(() -> "SAVED".equals(store.candidate(id).optString("state")), "Manually selected refund was not saved"); barrier();
        assertEquals(zero.localRowId, store.candidate(id).optLong("transaction_id"));
        assertEquals(1.77d, bills.byLocalRowId(zero.localRowId).cost, 0.000000000001d);
        assertEquals(4d, bills.byLocalRowId(income.localRowId).cost, 0d);
        OriginalRefundRepository.Snapshot refund = new OriginalRefundRepository(store).firstByParent(0, zero.billId);
        assertNotNull(refund); assertEquals(firstId, OriginalRefundCodec.decodeStrict(refund.data.rawInfos.get(0)).assetId);
        assertEquals(101.23d, store.assetRepository().byRowId(firstRow).optDouble("asset_number"), 0.000000000001d);
        assertEquals(200d, store.assetRepository().byRowId(secondRow).optDouble("asset_number"), 0d);
        assertEquals(0, ReferenceDiagnostics.snapshot().optLong("failures"));
    }

    @Test public void originalTransferPageIgnoresBookPickerFilteringAndSavesThroughTheActualWorkerWithItsFee() throws Exception {
        ReferenceApplication.prefs().edit().putLong("current_book", book).putInt("original_transfer_mode", 0).commit();
        long header = store.saveBook(0, "转账浮窗另一本", "CNY", 0);
        AssetRepository.Definition from = new AssetRepository.Definition("转账浮窗转出账户", 1, null); from.assetNumber = 100;
        AssetRepository.Definition to = new AssetRepository.Definition("转账浮窗转入账户", 1, null); to.assetNumber = 5;
        from.showBook.add(store.book(book).optLong("semantic_id")); to.showBook.add(store.book(book).optLong("semantic_id"));
        long fromRow = store.assetRepository().insertRecovered(0, 1900070501L, 1, 1, from);
        long toRow = store.assetRepository().insertRecovered(0, 1900070502L, 2, 1, to);
        String remark = "转账浮窗原始备注"; bill(1900070503, true, 10, remark);
        String id = candidate(1000, remark); show(id); assertTrue(CaptureOverlay.status().optBoolean("repeatDetected"));
        clickText("转账"); clickId("account_book_layout"); clickText("转账浮窗另一本");
        await(() -> visibleTextEquals("account_book_name", "转账浮窗另一本") && node("choice_from_asset") != null,
                "Transfer page did not finish rebuilding after the book selection");
        clickId("choice_from_asset"); clickText("转账浮窗转出账户");
        await(() -> visibleTextEquals("from_asset_name", "转账浮窗转出账户"), "Source selection was filtered by the header book");
        clickId("choice_to_asset"); clickText("转账浮窗转入账户");
        await(() -> visibleTextEquals("to_asset_name", "转账浮窗转入账户"), "Destination selection was not retained");
        clickId("check_asset"); await(() -> visibleTextEquals("from_asset_name", "转账浮窗转入账户"), "Account swap did not change the source");
        clickId("check_asset"); await(() -> visibleTextEquals("from_asset_name", "转账浮窗转出账户"), "Second swap did not restore the source");
        setText("cost", "10"); setText("service_charge_edit", "2");
        clickId("radio_two"); await(() -> ReferenceApplication.prefs().getInt("original_transfer_mode", -1) == 1,
                "Original fee mode was not persisted by the radio group");
        await(() -> visibleTextEquals("reduce_num", "-10.00") && visibleTextEquals("add_num", "+8.00"),
                "Transfer numbers did not finish rendering after the fee mode change");
        assertEquals("-10.00", text("reduce_num")); assertEquals("+8.00", text("add_num"));
        long beforeBills = store.scalar("SELECT COUNT(*) n FROM transactions"); clickId("btn_save");
        await(() -> "SAVED".equals(store.candidate(id).optString("state")), "Transfer worker did not save its actual typed receipt"); barrier();
        assertNull(node("confirm")); // A Bill repeat exists, but AutoAdd.Z never enters that popup.
        JSONObject receipt = store.one("SELECT * FROM candidate_transfer_receipts WHERE candidate_id=?", id);
        assertNotNull(receipt); OriginalTransferRecord transfer = new OriginalTransferRepository(store).byLocalRowId(receipt.optLong("transfer_row_id"));
        assertEquals(1900070501L, transfer.fromAssetId); assertEquals(1900070502L, transfer.toAssetId);
        assertEquals(8.0d, transfer.cost, 0); assertEquals(2.0d, transfer.serviceCharge, 0); assertEquals(0.0d, transfer.toCost, 0);
        assertEquals(90.0d, store.assetRepository().byRowId(fromRow).optDouble("asset_number"), 0);
        assertEquals(13.0d, store.assetRepository().byRowId(toRow).optDouble("asset_number"), 0);
        OriginalBillRecord fee = new OriginalBillRepository(store).byExactUser(0, transfer.billId);
        assertNotNull(fee); assertEquals(store.book(book).optLong("semantic_id"), fee.bookId);
        assertEquals(beforeBills + 1, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(header, store.candidate(id).optLong("book_id")); assertTrue(store.candidate(id).isNull("transaction_id"));
        assertEquals("transfer", store.candidate(id).optString("save_kind")); assertEquals(0, ReferenceDiagnostics.snapshot().optLong("failures"));
    }

    @Test public void missingTransferAmountOrSelectionsKeepTheRealWindowOpenBeforeAnyWorkerSave() throws Exception {
        String id = candidate(1700, "转账缺少输入的原始备注"); show(id); clickText("转账");
        long before = store.scalar("SELECT COUNT(*) n FROM original_transfers");
        setText("cost", ""); clickId("btn_save"); barrier();
        assertTrue(CaptureOverlay.status().optBoolean("attached")); assertEquals("PENDING", store.candidate(id).optString("state"));
        setText("cost", "17"); clickId("btn_save"); barrier();
        assertTrue(CaptureOverlay.status().optBoolean("attached")); assertEquals("PENDING", store.candidate(id).optString("state"));
        assertEquals(before, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        assertEquals(0, ReferenceDiagnostics.snapshot().optLong("failures"));
    }

    private AccessibilityNodeInfo node(String id) {
        String resource = ReferenceApplication.app().getResources().getResourceName(Ui.id(ReferenceApplication.app(), id));
        for (AccessibilityWindowInfo window : automation.getWindows()) if (window.getType() == AccessibilityWindowInfo.TYPE_SYSTEM) {
            AccessibilityNodeInfo root = window.getRoot(); if (root == null || !ReferenceApplication.app().getPackageName().contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) continue;
            java.util.List<AccessibilityNodeInfo> matches = root.findAccessibilityNodeInfosByViewId(resource); if (!matches.isEmpty()) return matches.get(0);
        }
        return null;
    }
    private String text(String id) { AccessibilityNodeInfo n = node(id); return n == null || n.getText() == null ? "" : n.getText().toString(); }
    private boolean visibleTextEquals(String id, String expected) {
        AccessibilityNodeInfo n = node(id);
        return n != null && n.isVisibleToUser() && n.getText() != null && expected.contentEquals(n.getText());
    }
    private void awaitRefundBook(String name, String parentRemark) throws Exception {
        // Injecting the item click does not establish that the modal window has
        // dismissed and the replacement category_pager accessibility tree is ready.
        // Require the selected header AND the same visible refund parent together.
        await(() -> visibleTextEquals("account_book_name", name)
                && visibleTextEquals("bill_count", "已选择1条账单")
                && visibleTextEquals("bill_remark", parentRemark),
                "Selected book and retained refund parent did not finish rendering");
        assertEquals(name, text("account_book_name"));
    }
    private void clickId(String id) throws Exception { await(() -> node(id) != null && node(id).isVisibleToUser(), "No visible overlay control " + id); touch(node(id)); }
    private void clickText(String text) throws Exception {
        clickText(text, true);
    }
    private void clickContainingText(String text) throws Exception {
        clickText(text, false);
    }
    private void clickText(String text, boolean exact) throws Exception {
        final AccessibilityNodeInfo[] found = {null};
        await(() -> {
            for (AccessibilityWindowInfo window : automation.getWindows()) if (window.getType() == AccessibilityWindowInfo.TYPE_SYSTEM) {
                AccessibilityNodeInfo root = window.getRoot(); if (root == null || !ReferenceApplication.app().getPackageName().contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) continue;
                for (AccessibilityNodeInfo candidate : root.findAccessibilityNodeInfosByText(text)) {
                    String value = candidate.getText() == null ? "" : candidate.getText().toString();
                    if (candidate.isVisibleToUser() && (exact ? text.equals(value) : value.contains(text))) { found[0] = candidate; return true; }
                }
            }
            return false;
        }, "No real overlay text " + text); touch(found[0]);
    }
    private void setText(String id, String text) {
        AccessibilityNodeInfo input = node(id); assertNotNull(input); Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text); assertTrue(input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args));
    }
    private void touch(AccessibilityNodeInfo node) {
        Rect r = new Rect(); node.getBoundsInScreen(r); assertFalse(r.isEmpty()); Point p = new Point(r.centerX(), r.centerY());
        long time = SystemClock.uptimeMillis(); MotionEvent down = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, p.x, p.y, 0);
        MotionEvent up = MotionEvent.obtain(time, time + 80, MotionEvent.ACTION_UP, p.x, p.y, 0);
        try { down.setSource(InputDevice.SOURCE_TOUCHSCREEN); up.setSource(InputDevice.SOURCE_TOUCHSCREEN); assertTrue(automation.injectInputEvent(down, true)); assertTrue(automation.injectInputEvent(up, true)); }
        finally { down.recycle(); up.recycle(); }
    }
    private interface Check { boolean ready(); }
    private void await(Check check, String message) throws Exception {
        long end = SystemClock.elapsedRealtime() + 7000;
        while (SystemClock.elapsedRealtime() < end) { if (check.ready()) return; SystemClock.sleep(50); }
        throw new AssertionError(message + " " + liveUiSummary() + " " + CaptureOverlay.status() + " " + ReferenceDiagnostics.snapshot());
    }
    /** Synthetic emulator fixture labels only; no amount, capture payload or SQL is dumped. */
    private JSONObject liveUiSummary() {
        JSONObject result = Json.object("header", text("account_book_name"), "bill_count", text("bill_count"),
                "parent_fixture_remark", text("bill_remark"), "edited_fixture_remark", text("remark"),
                "renderedRefundPage", node("bill_count") != null);
        try {
            AccessibilityNodeInfo focus = automation.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            Json.put(result, "inputFocus", focus == null ? "none" : focus.getViewIdResourceName());
            org.json.JSONArray tabs = new org.json.JSONArray();
            AccessibilityNodeInfo strip = node("tab_layout"); collectTabState(strip, tabs);
            Json.put(result, "tabNodes", tabs);
            Json.put(result, "tabSelectionLimit", "ReferenceTabs does not expose its selected index as an accessibility flag");
            org.json.JSONArray windows = new org.json.JSONArray();
            for (AccessibilityWindowInfo window : automation.getWindows()) windows.put(Json.object(
                    "type", window.getType(), "focused", window.isFocused(), "active", window.isActive(), "layer", window.getLayer()));
            Json.put(result, "windows", windows);
        } catch (RuntimeException error) { Json.put(result, "snapshotFailureType", error.getClass().getName()); }
        return result;
    }
    private void collectTabState(AccessibilityNodeInfo node, org.json.JSONArray tabs) {
        if (node == null) return;
        if (node.getText() != null) tabs.put(Json.object("text", node.getText().toString(), "selected", node.isSelected(), "visible", node.isVisibleToUser()));
        for (int i = 0; i < node.getChildCount(); i++) collectTabState(node.getChild(i), tabs);
    }
    private void barrier() throws Exception { FutureTask<Void> task = new FutureTask<>(() -> null); YimuKernel.enqueue(task); task.get(10, TimeUnit.SECONDS); }
    private String overlayMode() throws Exception {
        String result = shell("appops get --user current " + token(ReferenceApplication.app().getPackageName()) + " SYSTEM_ALERT_WINDOW");
        Matcher m = Pattern.compile("SYSTEM_ALERT_WINDOW:\\s*(allow|deny|ignore|default|foreground)\\b").matcher(result);
        if (m.find()) return m.group(1); if (result.contains("No operations")) return "default"; throw new AssertionError("Unexpected overlay app-op response");
    }
    private String shell(String command) throws Exception {
        try (ParcelFileDescriptor descriptor = automation.executeShellCommand(command); InputStream in = new ParcelFileDescriptor.AutoCloseInputStream(descriptor); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[4096]; int n; while ((n = in.read(bytes)) != -1) { out.write(bytes, 0, n); if (out.size() > 1048576) throw new IllegalStateException("Shell output too large"); }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
    private static String token(String value) { if (!value.matches("[A-Za-z0-9_./:]+")) throw new IllegalArgumentException("Invalid emulator token"); return value; }
}
