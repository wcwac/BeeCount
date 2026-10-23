package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real V stages and partial SQL failures, with receiver histories independently checked. */
@RunWith(AndroidJUnit4.class)
public final class OriginalAutoRefundDeviceTest {
    private static final String DATABASE = "reference_original_auto_refund_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private OriginalBillRepository bills;
    private OriginalRefundRepository refunds;
    private Recorder effects;
    private OriginalFinancialOrchestrator financial;
    private OriginalAutoRefundCommands command;
    private AtomicLong clock;
    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase();
        bills = new OriginalBillRepository(store); refunds = new OriginalRefundRepository(store);
        effects = new Recorder(); clock = new AtomicLong(1000);
        financial = new OriginalFinancialOrchestrator(store, 7, 7, effects, clock::incrementAndGet);
        command = new OriginalAutoRefundCommands(store, 7, 7, effects, clock::incrementAndGet, financial);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DATABASE); }
    private long asset(long semantic, double balance) {
        AssetRepository.Definition d = new AssetRepository.Definition("合成账户 " + semantic, 1, null);
        d.assetNumber = balance; return store.assetRepository().insertRecovered(7, semantic, 1, 1, d);
    }
    private OriginalBillRecord parent(int user, int semantic, int category, long asset, double cost) {
        OriginalBillRecord r = new OriginalBillRecord();
        r.userId = user; r.billId = semantic; r.bookId = 1; r.parentCategoryId = category;
        r.childCategoryId = category == 9 ? 911 : -1; r.assetId = asset; r.cost = cost;
        r.time = 1735689600000L; r.recordTime = r.time; r.remark = "原账单";
        bills.saveOriginalRaw(r, projection(r, false)); return r;
    }
    private ContentValues projection(OriginalBillRecord bill, boolean difference) {
        ContentValues v = new ContentValues();
        v.put("book_id", 1); v.put("kind", bill.parentCategoryId == 9 ? "income" : "expense");
        v.put("amount", BigDecimal.valueOf(Math.abs(bill.cost)).movePointRight(2).longValueExact());
        v.put("currency", "CNY"); v.put("time", bill.time); v.put("created", bill.recordTime);
        v.put("parent_semantic_id", bill.parentCategoryId); v.put("child_semantic_id", bill.childCategoryId);
        JSONObject category = store.categoryRepository().rowForSelection(bill.parentCategoryId == 9 ? "income" : "expense", bill.parentCategoryId, bill.childCategoryId);
        if (category == null) v.putNull("category_id"); else v.put("category_id", category.optLong("id"));
        JSONObject account = store.assetRepository().p(bill.assetId, 7);
        if (account == null) v.putNull("account_id"); else v.put("account_id", account.optLong("id"));
        v.put("owner_semantic_id", 7); v.put("raw_asset_semantic_id", bill.assetId); return v;
    }
    private OriginalAutoRefundCommands.Result refund(OriginalBillRecord parent, Long receiver, double amount) {
        return command.save(parent, receiver, amount, "到账备注", 1735776000000L, 0, this::projection);
    }
    private OriginalRefundRepository.Snapshot aggregate(int bill) { return refunds.firstByParent(7, bill); }
    private List<JSONObject> histories() { return store.query("SELECT * FROM asset_histories ORDER BY id"); }

    @Test public void partialExpenseRefundCreditsReceiverAndDoesNotReplayParentAccount() {
        asset(10, 100); asset(20, 5);
        OriginalBillRecord parent = parent(7, 101, 99, 10, 100);
        OriginalAutoRefundCommands.Result result = refund(parent, 20L, 30);
        assertTrue(result.saved); assertNull(result.differenceSave); assertNull(result.relationSave);
        assertEquals(70.0d, bills.byLocalRowId(parent.localRowId).cost, 0);
        assertEquals(100.0d, financial.readBalance(10), 0); assertEquals(35.0d, financial.readBalance(20), 0);
        assertEquals(1, histories().size()); assertEquals(30.0d, histories().get(0).optDouble("change_number"), 0);
        assertEquals("收到退款：原账单", histories().get(0).optString("change_content"));
        assertEquals(30.0d, aggregate(101).data.refundNumber, 0);
        OriginalRefundInfo info = OriginalRefundCodec.decodeStrict(aggregate(101).data.rawInfos.get(0));
        assertEquals(20, info.assetId); assertEquals(30.0d, info.number, 0); assertEquals(-70.0d, info.currentNumber, 0);
        assertEquals(0, info.inTime); assertEquals(0, info.relatedBillId);
        assertEquals(Arrays.asList("asset:true", "parent:true", "instalment", "credit", "refund:true", "group"), effects.trace);
    }
    @Test public void refundOfIncomeDebitsReceiverAndUsesSignedItemFields() {
        asset(10, 200); OriginalBillRecord parent = parent(7, 102, 9, 10, 100);
        OriginalAutoRefundCommands.Result result = refund(parent, 10L, 30);
        assertTrue(result.saved); assertEquals(170.0d, financial.readBalance(10), 0);
        assertEquals(-30.0d, result.item.number, 0); assertEquals(-30.0d, result.item.assetNum, 0);
        assertEquals(-30.0d, result.item.costNum, 0); assertEquals(70.0d, result.item.currentNumber, 0);
        assertEquals("退款：原账单", histories().get(0).optString("change_content"));
        assertEquals(-30.0d, histories().get(0).optDouble("change_number"), 0);
    }
    @Test public void equalRefundPersistsNetZeroWithoutInventingDifferenceBill() {
        OriginalBillRecord parent = parent(7, 103, 99, -1, 100);
        OriginalAutoRefundCommands.Result result = refund(parent, null, 100);
        assertTrue(result.saved); assertEquals(0.0d, bills.byLocalRowId(parent.localRowId).cost, 0);
        assertEquals(0, store.transaction(parent.localRowId).optLong("amount"));
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_bill_related"));
        assertEquals(0.0d, result.item.restoreCostNum, 0); assertEquals(100.0d, result.item.number, 0);
        assertEquals(0, result.item.relatedBillId); bills.validateRestored();
    }
    @Test public void excessExpenseRefundSavesIncomeDifferenceWithOriginalAssetZeroAndIndependentRelation() {
        asset(0, 50); asset(20, 20);
        OriginalBillRecord parent = parent(7, 104, 99, -1, 100);
        OriginalAutoRefundCommands.Result result = refund(parent, 20L, 150);
        assertTrue(result.saved); assertTrue(result.differenceSave.saved); assertTrue(result.relationSave.saved);
        OriginalBillRecord difference = result.differenceSave.record;
        assertEquals(50.0d, difference.cost, 0); assertEquals(9, difference.parentCategoryId); assertEquals(911, difference.childCategoryId);
        assertEquals(0, difference.assetId); assertEquals(1, difference.billType); assertEquals(1, difference.bookId);
        assertEquals(100.0d, financial.readBalance(0), 0); assertEquals(170.0d, financial.readBalance(20), 0);
        assertEquals(100.0d, result.item.restoreCostNum, 0); assertEquals(difference.billId, result.item.relatedBillId);
        assertEquals(104, result.relationSave.record.oneBillId); assertEquals(difference.billId, result.relationSave.record.twoBillId);
        assertEquals(7, result.relationSave.record.userId); assertEquals(0.0d, bills.byLocalRowId(parent.localRowId).cost, 0);
        assertEquals(2, histories().size()); assertEquals(150.0d, histories().get(0).optDouble("change_number"), 0);
        assertEquals(50.0d, histories().get(1).optDouble("change_number"), 0);
    }
    @Test public void excessIncomeRefundUsesExpenseParent99AndNewChildZero() {
        asset(0, 100); asset(20, 200);
        OriginalBillRecord parent = parent(7, 105, 9, -1, 100);
        OriginalAutoRefundCommands.Result result = refund(parent, 20L, 150);
        assertTrue(result.saved); OriginalBillRecord difference = result.differenceSave.record;
        assertEquals(99, difference.parentCategoryId); assertEquals(0, difference.childCategoryId);
        assertEquals(50.0d, difference.cost, 0); assertEquals(50.0d, financial.readBalance(0), 0);
        assertEquals(50.0d, financial.readBalance(20), 0); assertEquals(-150.0d, result.item.number, 0);
        assertEquals(0.0d, result.item.currentNumber, 0);
    }
    @Test public void priorDifferenceCategoryAndParentOwnerAreRetainedAcrossAnotherBook() {
        OriginalBillRecord prior = parent(8, 106, 9, -1, 1);
        prior.remark = "退款差额"; prior.childCategoryId = 909; prior.fromUserId = 7;
        prior.bookId = 999; bills.saveOriginalRaw(prior, null);
        OriginalBillRecord parent = parent(8, 107, 99, -1, 10);
        OriginalAutoRefundCommands.Result result = refund(parent, null, 20);
        assertTrue(result.saved); OriginalBillRecord difference = result.differenceSave.record;
        assertEquals(9, difference.parentCategoryId); assertEquals(909, difference.childCategoryId);
        assertEquals(8, difference.userId); assertEquals(7, difference.fromUserId); assertEquals(parent.bookId, difference.bookId);
        assertEquals(7, result.refundSave.record.ownerSemanticId); assertEquals(7, result.relationSave.record.userId);
        assertEquals(8, bills.byLocalRowId(parent.localRowId).userId);
    }
    @Test public void sequentialRefundPrependsItemsAndCapsExcessAtOldNetCost() {
        asset(20, 0); OriginalBillRecord parent = parent(7, 108, 99, -1, 100);
        OriginalAutoRefundCommands.Result first = refund(parent, 20L, 30);
        String firstRaw = aggregate(108).data.rawInfos.get(0);
        OriginalAutoRefundCommands.Result second = refund(parent, 20L, 80);
        assertTrue(first.saved); assertTrue(second.saved); assertEquals(110.0d, financial.readBalance(20), 0);
        assertEquals(10.0d, second.differenceSave.record.cost, 0); assertEquals(70.0d, second.item.restoreCostNum, 0);
        OriginalRefundRepository.Snapshot all = aggregate(108);
        assertEquals(110.0d, all.data.refundNumber, 0); assertEquals(2, all.data.rawInfos.size());
        assertEquals(firstRaw, all.data.rawInfos.get(1));
        assertEquals(80.0d, OriginalRefundCodec.decodeStrict(all.data.rawInfos.get(0)).number, 0);
        assertEquals(first.refundSave.record.rowId, second.refundSave.record.rowId);
    }
    @Test public void parentSaveFalseKeepsReceiverAndAggregateAndRunsFollowingEffects() {
        asset(20, 10); OriginalBillRecord parent = parent(7, 109, 99, -1, 100);
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_parent_save_failure BEFORE UPDATE OF bill_cost ON transactions BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        OriginalAutoRefundCommands.Result result = refund(parent, 20L, 30);
        assertFalse(result.saved); assertFalse(result.parentSave.saved); assertTrue(result.refundSave.saved);
        assertEquals(100.0d, bills.byLocalRowId(parent.localRowId).cost, 0);
        assertEquals(70.0d, result.parent.cost, 0); assertEquals(-70.0d, result.item.currentNumber, 0);
        assertEquals(40.0d, financial.readBalance(20), 0); assertEquals(30.0d, aggregate(109).data.refundNumber, 0);
        assertEquals(Arrays.asList("asset:true", "parent:false", "instalment", "credit", "refund:true", "group"), effects.trace);
        assertFalse(result.firstFailureType().contains("synthetic"));
    }
    @Test public void differenceSaveFalseStillCreatesRawRelationAndUpdatesParentAndRefund() {
        asset(20, 0); OriginalBillRecord parent = parent(7, 110, 99, -1, 100);
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_difference_failure BEFORE INSERT ON transactions WHEN NEW.bill_origin='original' BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        OriginalAutoRefundCommands.Result result = refund(parent, 20L, 150);
        assertFalse(result.saved); assertFalse(result.differenceSave.saved); assertEquals(0, result.differenceSave.record.localRowId);
        assertTrue(result.relationSave.saved); assertTrue(result.parentSave.saved); assertTrue(result.refundSave.saved);
        assertEquals(result.differenceSave.record.billId, result.relationSave.record.twoBillId);
        assertEquals(result.differenceSave.record.billId, result.item.relatedBillId);
        assertEquals(150.0d, financial.readBalance(20), 0); assertEquals(0.0d, bills.byLocalRowId(parent.localRowId).cost, 0);
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(150.0d, aggregate(110).data.refundNumber, 0);
    }
    @Test public void relationSaveFalseDoesNotRollBackSavedDifferenceOrStopRefundAggregate() {
        OriginalBillRecord parent = parent(7, 111, 99, -1, 100);
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_relation_failure BEFORE INSERT ON original_bill_related BEGIN SELECT RAISE(IGNORE); END");
        OriginalAutoRefundCommands.Result result = refund(parent, null, 150);
        assertFalse(result.saved); assertTrue(result.differenceSave.saved); assertFalse(result.relationSave.saved);
        assertTrue(result.parentSave.saved); assertTrue(result.refundSave.saved);
        assertEquals(2, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_bill_related"));
        assertEquals(result.differenceSave.record.billId, result.item.relatedBillId);
        assertEquals("group", effects.trace.get(effects.trace.size() - 1));
    }
    @Test public void aggregateSaveFalseLeavesEarlierNetCostAndHistoryAndStillUpdatesCacheHook() {
        asset(20, 0); OriginalBillRecord parent = parent(7, 112, 99, -1, 100);
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_refund_failure BEFORE INSERT ON original_refunds BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        OriginalAutoRefundCommands.Result result = refund(parent, 20L, 30);
        assertFalse(result.saved); assertTrue(result.parentSave.saved); assertFalse(result.refundSave.saved);
        assertEquals(70.0d, bills.byLocalRowId(parent.localRowId).cost, 0); assertEquals(30.0d, financial.readBalance(20), 0);
        assertEquals(1, histories().size()); assertNull(aggregate(112));
        assertEquals(Arrays.asList("asset:true", "parent:true", "instalment", "credit", "refund:false", "group"), effects.trace);
        assertEquals(30.0d, result.refundSave.record.data.refundNumber, 0);
    }
    @Test public void e3AdoptsAccessibleRowButMissingParentDoesNotInsertAnOrdinaryBill() {
        OriginalBillRecord stored = parent(7, 113, 99, -1, 100);
        OriginalBillRecord selected = bills.byLocalRowId(stored.localRowId); selected.localRowId = 0;
        OriginalAutoRefundCommands.Result adopted = refund(selected, null, 30);
        assertTrue(adopted.saved); assertEquals(stored.localRowId, selected.localRowId);
        OriginalBillRecord absent = new OriginalBillRecord(); absent.userId = 7; absent.billId = 114;
        absent.bookId = 1; absent.cost = 20; absent.parentCategoryId = 99; absent.assetId = -1;
        OriginalAutoRefundCommands.Result noSave = refund(absent, null, 5);
        assertNull(noSave.parentSave); assertEquals(0, absent.localRowId); assertTrue(noSave.refundSave.saved);
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertNull(aggregate(114).data.parentTransactionRowId);
    }
    @Test public void absentReceiverAndSelectedSemanticZeroHaveDistinctAccountEffects() {
        asset(0, 100); OriginalBillRecord first = parent(7, 115, 99, -1, 100);
        OriginalAutoRefundCommands.Result noReceiver = refund(first, null, 30);
        assertEquals(0, noReceiver.item.assetId); assertEquals(100.0d, financial.readBalance(0), 0); assertTrue(histories().isEmpty());
        OriginalBillRecord second = parent(7, 116, 99, -1, 100);
        OriginalAutoRefundCommands.Result explicitZero = refund(second, 0L, 30);
        assertEquals(0, explicitZero.item.assetId); assertEquals(130.0d, financial.readBalance(0), 0);
        assertEquals(1, histories().size()); assertEquals(0, histories().get(0).optLong("asset_semantic_id"));
    }
    @Test public void automaticSingleAmountLeavesOriginalFxFieldsUntouchedAndRejectsZeroBeforeEffects() {
        OriginalBillRecord parent = parent(7, 117, 99, -1, 100);
        parent.currencyInfo = "USD 100"; parent.currencyAssetNumber = 7; parent.discountNumber = 3;
        bills.saveOriginalRaw(parent, null);
        OriginalAutoRefundCommands.Result result = command.save(parent, null, 30, "备注", 222, 333, this::projection);
        OriginalBillRecord read = bills.byLocalRowId(parent.localRowId);
        assertEquals("USD 100", read.currencyInfo); assertEquals(7.0d, read.currencyAssetNumber, 0); assertEquals(3.0d, read.discountNumber, 0);
        assertEquals(70.0d, read.cost, 0); assertEquals(30.0d, result.item.assetNum, 0); assertEquals(30.0d, result.item.costNum, 0);
        assertEquals(222, result.item.time); assertEquals(333, result.item.inTime);
        long count = store.scalar("SELECT COUNT(*) n FROM original_refund_items"); int traces = effects.trace.size();
        try { refund(parent, null, 0); fail("zero accepted"); } catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
        assertEquals(count, store.scalar("SELECT COUNT(*) n FROM original_refund_items")); assertEquals(traces, effects.trace.size());
    }
    private static final class Recorder implements OriginalAutoRefundCommands.Effects, OriginalFinancialOrchestrator.Effects {
        final List<String> trace = new ArrayList<>();
        @Override public void assetChanged(OriginalAssetBalanceStore.LoadedAsset asset, boolean refresh) { trace.add("asset:" + refresh); }
        @Override public void schedule(Runnable callback, long delay) {}
        @Override public void billSaved(OriginalBillRecord bill, boolean saved) { trace.add("difference:" + saved); }
        @Override public void parentUpdated(OriginalBillRecord parent, boolean saved) { trace.add("parent:" + saved); }
        @Override public void adjustInstalment(OriginalBillRecord parent) { trace.add("instalment"); }
        @Override public void refreshCredit(OriginalBillRecord parent) { trace.add("credit"); }
        @Override public void refundAggregateChanged(int bill, double number, boolean saved) { trace.add("refund:" + saved); }
        @Override public void refreshGroup(OriginalBillRecord parent) { trace.add("group"); }
    }
}
