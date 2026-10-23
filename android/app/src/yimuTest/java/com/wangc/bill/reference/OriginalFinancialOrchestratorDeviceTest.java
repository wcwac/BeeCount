package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
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

/** Real SQL stages with independently checked amounts; no whole-action rollback assumption. */
@RunWith(AndroidJUnit4.class)
public final class OriginalFinancialOrchestratorDeviceTest {
    private static final String DATABASE = "reference_original_financial_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private OriginalBillRepository bills;
    private AssetHistoryRepository histories;
    private Recorder effects;
    private OriginalFinancialOrchestrator command;

    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase();
        bills = new OriginalBillRepository(store); histories = new AssetHistoryRepository(store);
        effects = new Recorder(); command = orchestrator(7, 7, effects);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DATABASE); }

    private OriginalFinancialOrchestrator orchestrator(int owner, int bookOwner, Recorder recorder) {
        AtomicLong clock = new AtomicLong(1000);
        return new OriginalFinancialOrchestrator(store, owner, bookOwner, recorder, clock::incrementAndGet);
    }
    private long asset(int owner, long semantic, int type, double balance) {
        AssetRepository.Definition data = new AssetRepository.Definition("合成账户 " + semantic, type, "CNY");
        data.assetNumber = balance;
        return store.assetRepository().insertRecovered(owner, semantic, 1, 0, data);
    }
    private OriginalBillRecord bill(int user, int semantic, long asset, double cost) {
        OriginalBillRecord r = new OriginalBillRecord();
        r.userId = user; r.billId = semantic; r.assetId = asset; r.cost = cost;
        r.parentCategoryId = 99; r.childCategoryId = -1; r.bookId = 1;
        r.time = 1735689600000L; r.remark = "合成账单";
        return r;
    }
    private ContentValues projection(long amount, Long account) {
        ContentValues v = new ContentValues();
        v.put("book_id", 1); v.put("kind", "expense"); v.put("amount", amount); v.put("currency", "CNY");
        v.put("category_id", store.categoryRepository().parent(99).optLong("id"));
        v.put("parent_semantic_id", 99); v.put("child_semantic_id", -1);
        v.put("time", 1735689600000L); v.put("created", 1735689600000L);
        if (account == null) v.putNull("account_id"); else v.put("account_id", account);
        return v;
    }
    private OriginalBillRecord existing(int user, int semantic, long asset, double cost, long account) {
        OriginalBillRecord r = bill(user, semantic, asset, cost);
        bills.saveOriginalRaw(r, projection((long) (Math.abs(cost) * 100), account));
        return r;
    }
    private List<JSONObject> orderedHistory() { return store.query("SELECT * FROM asset_histories ORDER BY id"); }
    private void refund(OriginalBillRecord parent, double total, OriginalRefundInfo... items) {
        OriginalRefundRepository.Data data = new OriginalRefundRepository.Data();
        data.parentBillSemanticId = parent.billId; data.parentTransactionRowId = parent.localRowId;
        data.refundNumber = total; data.rawInfos = new ArrayList<>();
        for (OriginalRefundInfo item : items) data.rawInfos.add(OriginalRefundCodec.encodeNew(item));
        new OriginalRefundRepository(store).insertRecovered(7, 801, data, 1);
    }

    @Test public void billSaveFalseKeepsEarlierAssetHistoryAndCategoryLearning() {
        long row = asset(7, 10, 1, 100);
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_bill_save_failure BEFORE INSERT ON transactions WHEN NEW.bill_origin='original' BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        OriginalBillRecord r = bill(0, 0, 10, 12);
        OriginalBillRepository.SaveResult result = command.saveNormal(r, projection(1200, row));
        assertFalse(result.saved); assertSame(r, result.record); assertEquals(7, r.userId);
        assertEquals(88.0d, command.readBalance(10), 0.0d);
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(-12.0d, orderedHistory().get(0).optDouble("change_number"), 0.0d);
        assertEquals(88.0d, orderedHistory().get(0).optDouble("current_number"), 0.0d);
        assertEquals(0, effects.historyCountsWhenScheduled.get(0).longValue());
        assertTrue(store.assetRepository().categoryMapping(7, 99, -1).exists);
        assertEquals(10, store.assetRepository().categoryMapping(7, 99, -1).rawSemanticId);
        assertEquals(Arrays.asList(false), effects.billResults); assertEquals(1, effects.delayed.size());
    }

    @Test public void assetSaveFalseStillUsesTheMutatedHandleForHistoryAndLaterBillSave() {
        long row = asset(7, 10, 1, 100);
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_asset_save_failure BEFORE UPDATE OF asset_number ON accounts BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        OriginalBillRecord r = bill(7, 0, 10, 12);
        assertTrue(command.saveNormal(r, projection(1200, row)).saved);
        assertEquals(100.0d, command.readBalance(10), 0.0d);
        assertEquals(12.0d, bills.byLocalRowId(r.localRowId).cost, 0.0d);
        assertEquals(-12.0d, orderedHistory().get(0).optDouble("change_number"), 0.0d);
        assertEquals(88.0d, orderedHistory().get(0).optDouble("current_number"), 0.0d);
        effects.delayed.get(0).run();
        assertEquals(88.0d, effects.changed.get(0).getAssetNumber(), 0.0d);
        assertEquals(Arrays.asList(true), effects.billResults);
    }

    @Test public void categorySaveFalseKeepsTheSavedBillBalanceHistoryAndFinalCallback() {
        long row = asset(7, 10, 1, 100);
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_category_save_failure BEFORE INSERT ON category_accounts BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        OriginalBillRecord r = bill(7, 0, 10, 12);
        OriginalBillRepository.SaveResult result = command.saveNormal(r, projection(1200, row));
        assertTrue(result.saved); assertSame(r, result.record); assertTrue(r.localRowId > 0);
        assertEquals(12.0d, bills.byLocalRowId(r.localRowId).cost, 0.0d);
        assertEquals(88.0d, command.readBalance(10), 0.0d);
        assertEquals(1, orderedHistory().size());
        assertEquals(-12.0d, orderedHistory().get(0).optDouble("change_number"), 0.0d);
        assertEquals(88.0d, orderedHistory().get(0).optDouble("current_number"), 0.0d);
        assertFalse(store.assetRepository().categoryMapping(7, 99, -1).exists);
        assertEquals(Arrays.asList(true), effects.billResults); assertEquals(1, effects.delayed.size());
    }

    @Test public void sameAccountEditReversesOldThenAppliesNewAndWritesOneDifference() {
        long row = asset(7, 10, 1, 80);
        OriginalBillRecord old = existing(7, 901, 10, 20, row);
        OriginalBillRecord edited = bills.byLocalRowId(old.localRowId); edited.cost = 30;
        assertTrue(command.saveNormal(edited, projection(3000, row)).saved);
        assertEquals(70.0d, command.readBalance(10), 0.0d);
        assertEquals(100.0d, effects.changed.get(0).getAssetNumber(), 0.0d);
        assertEquals(0, effects.historyCountsWhenChanged.get(0).longValue());
        assertEquals(1, orderedHistory().size());
        assertEquals(-10.0d, orderedHistory().get(0).optDouble("change_number"), 0.0d);
        assertTrue(orderedHistory().get(0).optString("change_content").startsWith("账单金额编辑："));
        assertEquals(30.0d, bills.byLocalRowId(old.localRowId).cost, 0.0d);
    }

    @Test public void differentAccountEditWritesOldHistoryBeforeTheNewLookupEffects() {
        long oldRow = asset(7, 10, 1, 80), nextRow = asset(7, 11, 1, 50);
        OriginalBillRecord old = existing(7, 901, 10, 20, oldRow);
        OriginalBillRecord edited = bills.byLocalRowId(old.localRowId); edited.assetId = 11; edited.cost = 30;
        assertTrue(command.saveNormal(edited, projection(3000, nextRow)).saved);
        assertEquals(100.0d, command.readBalance(10), 0.0d); assertEquals(20.0d, command.readBalance(11), 0.0d);
        List<JSONObject> rows = orderedHistory(); assertEquals(2, rows.size());
        assertEquals(10, rows.get(0).optLong("asset_semantic_id")); assertEquals(20.0d, rows.get(0).optDouble("change_number"), 0.0d);
        assertEquals(11, rows.get(1).optLong("asset_semantic_id")); assertEquals(-30.0d, rows.get(1).optDouble("change_number"), 0.0d);
        assertEquals(0, effects.historyCountsWhenChanged.get(0).longValue());
        assertEquals(1, effects.historyCountsWhenScheduled.get(0).longValue());
    }

    @Test public void missingNewAccountLeavesTheOldRollbackAndItsHistoryCommitted() {
        long row = asset(7, 10, 1, 80);
        OriginalBillRecord old = existing(7, 901, 10, 20, row);
        OriginalBillRecord edited = bills.byLocalRowId(old.localRowId); edited.assetId = 999; edited.cost = 30;
        assertTrue(command.saveNormal(edited, projection(3000, null)).saved);
        assertEquals(100.0d, command.readBalance(10), 0.0d);
        assertEquals(1, orderedHistory().size()); assertEquals(20.0d, orderedHistory().get(0).optDouble("change_number"), 0.0d);
        assertTrue(effects.delayed.isEmpty()); assertEquals(999, bills.byLocalRowId(old.localRowId).assetId);
        assertEquals(999, store.assetRepository().categoryMapping(7, 99, -1).rawSemanticId);
    }

    @Test public void zeroNetBillUsesCappedRefundItemsRatherThanTheExcessAggregate() {
        long oldRow = asset(7, 10, 1, 110); asset(7, 11, 1, 50);
        OriginalBillRecord old = existing(7, 901, 10, 0, oldRow);
        OriginalRefundInfo item = new OriginalRefundInfo(); item.number = 30; item.restoreCostNum = 20; item.relatedBillId = 902;
        refund(old, 30, item);
        OriginalBillRecord edited = bills.byLocalRowId(old.localRowId); edited.assetId = 11;
        command.editBillAssets(edited);
        assertEquals(130.0d, command.readBalance(10), 0.0d); assertEquals(30.0d, command.readBalance(11), 0.0d);
        assertEquals(20.0d, orderedHistory().get(0).optDouble("change_number"), 0.0d);
        assertEquals(-20.0d, orderedHistory().get(1).optDouble("change_number"), 0.0d);
    }

    @Test public void nonemptyCurrencyInfoSuppressesRefundAndSelectsTheStoredOverride() {
        long row = asset(7, 10, 1, 95);
        OriginalBillRecord old = existing(7, 901, 10, 5, row);
        OriginalRefundInfo item = new OriginalRefundInfo(); item.number = 2; refund(old, 2, item);
        OriginalBillRecord edited = bills.byLocalRowId(old.localRowId);
        edited.cost = 10; edited.currencyInfo = "unparsed"; edited.currencyAssetNumber = 20;
        command.editBillAssets(edited);
        assertEquals(80.0d, command.readBalance(10), 0.0d);
        assertEquals(-15.0d, orderedHistory().get(0).optDouble("change_number"), 0.0d);
        assertEquals(5.0d, bills.byLocalRowId(old.localRowId).cost, 0.0d);
    }

    @Test public void delayedCallbackRetainsTheExactLoadedHandleInsteadOfReloadingItsId() {
        long row = asset(7, 10, 1, 100);
        assertTrue(command.saveNormal(bill(7, 0, 10, 10), projection(1000, row)).saved);
        assertEquals(1000, effects.delays.get(0).longValue());
        command.changeAsset(10, 5, false, "later independent operation");
        OriginalAssetBalanceStore.LoadedAsset fresh = effects.changed.get(0);
        assertEquals(95.0d, fresh.getAssetNumber(), 0.0d);
        effects.delayed.get(0).run();
        OriginalAssetBalanceStore.LoadedAsset captured = effects.changed.get(1);
        assertNotSame(fresh, captured); assertEquals(fresh.semanticId, captured.semanticId);
        assertEquals(90.0d, captured.getAssetNumber(), 0.0d); assertEquals(95.0d, command.readBalance(10), 0.0d);
    }

    @Test public void zeroInputSkipsChangeButDeleteWithHistoryStillRecordsZero() {
        asset(7, 10, 1, 100);
        command.changeAsset(10, 0.0d, false, "skipped"); command.changeAsset(10, -0.0d, true, "skipped");
        assertEquals(0, histories.count(7)); assertTrue(effects.changed.isEmpty());
        assertEquals(0, store.assetRepository().p(10, 7).optLong("update_time"));
        OriginalBillRecord zero = bill(7, 901, 10, 0);
        command.reverseDeletedBill(zero, true);
        assertEquals(1, histories.count(7)); assertEquals(0.0d, orderedHistory().get(0).optDouble("change_number"), 0.0d);
        assertEquals(100.0d, orderedHistory().get(0).optDouble("current_number"), 0.0d);
        assertEquals(1, effects.changed.size());
        command.reverseDeletedBill(zero, false);
        assertEquals(1, histories.count(7)); assertEquals(1, effects.changed.size());
    }

    @Test public void nonzeroInputWhoseDoubleDeltaRoundsToZeroStillWritesHistory() {
        asset(7, 10, 1, 1.0E20d);
        command.changeAsset(10, 1.0d, false, "nonzero input");
        assertEquals(1.0E20d, command.readBalance(10), 0.0d); assertEquals(1, histories.count(7));
        assertEquals(0.0d, orderedHistory().get(0).optDouble("change_number"), 0.0d);
        assertEquals(Arrays.asList(true), effects.refreshCredit);
    }

    @Test public void typeNineDerivesOutstandingAcrossAccessibleBillsAndOnlyItsAggregateOwner() {
        asset(7, 900, 9, 1234);
        long ordinary = asset(7, 10, 1, 0);
        OriginalBillRecord own = existing(7, 901, 10, 30, ordinary);
        existing(8, 901, 10, 40, ordinary);
        OriginalBillRecord sender = bill(9, 901, 10, 5); sender.fromUserId = 7; bills.saveOriginalRaw(sender, projection(500, ordinary));
        existing(99, 901, 10, 500, ordinary);
        OriginalBillRecord income = bill(7, 901, 10, 999); income.parentCategoryId = 9;
        ContentValues incomeProjection = projection(99900, ordinary);
        incomeProjection.put("kind", "income"); incomeProjection.put("category_id", store.categoryRepository().parent(9).optLong("id"));
        incomeProjection.put("parent_semantic_id", 9); incomeProjection.put("child_semantic_id", 0);
        bills.saveOriginalRaw(income, incomeProjection);
        OriginalReimbursementRepository repository = new OriginalReimbursementRepository(store);
        OriginalReimbursementRepository.Data owed = new OriginalReimbursementRepository.Data();
        owed.parentBillSemanticId = 901; owed.parentTransactionRowId = own.localRowId; owed.assetSemanticId = 900; owed.reimbursementNumber = 10;
        repository.insertRecovered(7, 1001, owed, 1);
        OriginalReimbursementRepository.Data otherOwner = owed.copy(); otherOwner.reimbursementNumber = 1;
        repository.insertRecovered(8, 1002, otherOwner, 1);
        OriginalReimbursementRepository.Data ended = owed.copy(); ended.end = true; repository.insertRecovered(7, 1003, ended, 1);
        OriginalFinancialOrchestrator sharedBook = orchestrator(7, 8, new Recorder());
        assertEquals(65.0d, sharedBook.readBalance(900), 0.0d);
        assertEquals(1234.0d, store.assetRepository().p(900, 7).optDouble("asset_number"), 0.0d);
    }

    private final class Recorder implements OriginalFinancialOrchestrator.Effects {
        final List<OriginalAssetBalanceStore.LoadedAsset> changed = new ArrayList<>();
        final List<Boolean> refreshCredit = new ArrayList<>(), billResults = new ArrayList<>();
        final List<Runnable> delayed = new ArrayList<>();
        final List<Long> delays = new ArrayList<>(), historyCountsWhenChanged = new ArrayList<>(), historyCountsWhenScheduled = new ArrayList<>();
        @Override public void assetChanged(OriginalAssetBalanceStore.LoadedAsset asset, boolean refresh) {
            changed.add(asset); refreshCredit.add(refresh); historyCountsWhenChanged.add(histories.count(7));
        }
        @Override public void schedule(Runnable callback, long delayMillis) {
            delayed.add(callback); delays.add(delayMillis); historyCountsWhenScheduled.add(histories.count(7));
        }
        @Override public void billSaved(OriginalBillRecord bill, boolean saved) { billResults.add(saved); }
    }
}
