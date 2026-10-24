package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Isolated Store tests for the next schema8 integration; no temporary schema/version recreation. */
@RunWith(AndroidJUnit4.class)
public final class OriginalLendStoreDeviceTest {
    private static final String DB = "reference_original_lend_store_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private OriginalLendRepository lends;
    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DB);
        store = new ReferenceStore(context, DB); store.getWritableDatabase(); lends = new OriginalLendRepository(store);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DB); }
    private OriginalLendRecord raw(int owner, int semantic) {
        OriginalLendRecord r = new OriginalLendRecord(); r.userId = owner; r.lendId = semantic;
        r.assetId = Long.MIN_VALUE; r.repaymentAssetId = Long.MAX_VALUE; r.billId = -2;
        r.number = -12.5; r.fromCost = -9.75; r.interest = -.25; r.type = -7;
        r.generalServiceBill = true; r.originalDeleted = true;
        r.outTime = -100; r.inTime = 200; r.createTime = 300; r.updateTime = 400; return r;
    }
    private OriginalLendRecord saved(int owner, int semantic, long asset, long repayment, int type, long time, double interest) {
        OriginalLendRecord r = raw(owner, semantic); r.assetId = asset; r.repaymentAssetId = repayment;
        r.type = type; r.outTime = time; r.interest = interest; lends.saveOriginalRaw(r); return r;
    }
    private long rows() { return store.scalar("SELECT COUNT(*) n FROM original_lends"); }
    private OriginalBillRecord bill(int semantic, int owner, int sender, int parent, int child) {
        long book = store.books().get(0).optLong("id"); String kind = parent == 9 ? "income" : "expense";
        OriginalBillRecord b = new OriginalBillRecord(); b.billId = semantic; b.userId = owner; b.fromUserId = sender;
        b.bookId = store.book(book).optLong("semantic_id"); b.parentCategoryId = parent; b.childCategoryId = child;
        b.assetId = -1; b.cost = 1; b.time = 1735689600000L; b.recordTime = b.time;
        ContentValues p = new ContentValues(); p.put("book_id", book); p.put("kind", kind); p.put("amount", 100);
        p.put("currency", "CNY"); p.put("time", b.time); p.put("created", b.recordTime);
        p.put("parent_semantic_id", parent); p.put("child_semantic_id", child);
        JSONObject category = store.categoryRepository().rowForSelection(kind, parent, child);
        if (category != null) p.put("category_id", category.optLong("id")); else p.putNull("category_id");
        new OriginalBillRepository(store).saveOriginalRaw(b, p); return b;
    }
    private void link(int owner, int type, int bill) {
        OriginalLendRecord r = raw(owner, 0); r.type = type; r.billId = bill; lends.saveOriginalRaw(r);
    }

    @Test public void allFifteenRawFieldsKeepTheirTypesAndDoNotNeedSemanticAssetOrBillRows() {
        OriginalLendRecord r = raw(Integer.MIN_VALUE, Integer.MAX_VALUE); r.remark = "原始合成借贷";
        long row = lends.saveOriginalRaw(r); assertTrue(row > 0); assertEquals(row, r.localRowId);
        assertNotEquals((long) r.lendId, row); OriginalLendRecord copy = lends.byLocalRowId(row);
        assertEquals(Integer.MIN_VALUE, copy.userId); assertEquals(Integer.MAX_VALUE, copy.lendId);
        assertEquals(Long.MIN_VALUE, copy.assetId); assertEquals(Long.MAX_VALUE, copy.repaymentAssetId);
        assertEquals(-2, copy.billId); assertEquals(-7, copy.type); assertEquals(-12.5, copy.number, 0);
        assertEquals(-9.75, copy.fromCost, 0); assertEquals(-.25, copy.interest, 0);
        assertTrue(copy.generalServiceBill); assertTrue(copy.originalDeleted); assertEquals(-100, copy.getTime());
        assertEquals(200, copy.inTime); assertEquals(300, copy.createTime); assertEquals(400, copy.updateTime);
        assertEquals(r.remark, copy.remark); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM accounts"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions")); lends.validateRestored();
    }
    @Test public void primitiveZerosAndFalseOverwriteButANullRemarkOmitsTheExistingReference() {
        OriginalLendRecord r = raw(7, 9); r.remark = "保留的原始备注"; lends.saveOriginalRaw(r);
        OriginalLendRecord zero = new OriginalLendRecord(); zero.localRowId = r.localRowId;
        assertTrue(lends.trySaveOriginalRaw(zero).saved); OriginalLendRecord copy = lends.byLocalRowId(r.localRowId);
        assertEquals(0, copy.userId); assertEquals(0, copy.lendId); assertEquals(0, copy.assetId);
        assertEquals(0, copy.repaymentAssetId); assertEquals(0, copy.billId); assertEquals(0, copy.type);
        assertEquals(0, copy.number, 0); assertEquals(0, copy.fromCost, 0); assertEquals(0, copy.interest, 0);
        assertFalse(copy.originalDeleted); assertFalse(copy.generalServiceBill);
        assertEquals(0, copy.outTime); assertEquals(0, copy.inTime); assertEquals(0, copy.createTime); assertEquals(0, copy.updateTime);
        assertEquals("保留的原始备注", copy.remark); assertNull(zero.remark);
    }
    @Test public void nullInsertEmptyStringUpdateAndLaterNullRemainDistinct() {
        OriginalLendRecord r = new OriginalLendRecord(); lends.saveOriginalRaw(r);
        assertNull(lends.byLocalRowId(r.localRowId).remark);
        r.remark = ""; lends.saveOriginalRaw(r); assertEquals("", lends.byLocalRowId(r.localRowId).remark);
        r.remark = null; lends.saveOriginalRaw(r); assertEquals("", lends.byLocalRowId(r.localRowId).remark);
        assertEquals(1, rows());
    }
    @Test public void rawIdentityDuplicatesArePreservedAndSemanticAllocationScopesCollisionsToItsOwner() {
        OriginalLendRecord first = raw(7, 0), duplicate = raw(7, 0), foreign = raw(8, 45);
        lends.saveOriginalRaw(first); lends.saveOriginalRaw(duplicate); lends.saveOriginalRaw(foreign);
        assertEquals(2, lends.allForOwner(7).size()); assertEquals(1, lends.allForOwner(8).size());
        assertNotEquals(first.localRowId, duplicate.localRowId); assertNotNull(lends.byIdentity(7, 0));
        OriginalLendRepository controlled = new OriginalLendRepository(store, new SequenceRandom(0, 45, 0));
        assertEquals(45, controlled.allocateSemanticId(7)); assertEquals(0, controlled.allocateSemanticId(9));
        assertEquals(3, rows()); assertNull(lends.byIdentity(9, 0));
    }
    @Test public void creditVMatchesOnlyTheCurrentOwnersRepaymentEndpointWithoutAdditionalFilters() {
        OriginalLendRecord old = saved(7, 1, 99, 5, 1, 900, -2);
        OriginalLendRecord otherType = saved(7, 2, 98, 5, -99, 100, 0);
        saved(7, 3, 5, 99, 4, 200, 2); saved(8, 4, 99, 5, 4, 300, 3);
        List<OriginalLendRecord> rows = lends.forRepaymentAsset(7, 5); assertEquals(2, rows.size());
        java.util.Set<Long> ids = new java.util.HashSet<>(); for (OriginalLendRecord row : rows) ids.add(row.localRowId);
        assertEquals(new java.util.HashSet<>(Arrays.asList(old.localRowId, otherType.localRowId)), ids);
        assertTrue(rows.get(0).originalDeleted); assertTrue(rows.get(0).generalServiceBill);
        assertEquals(1, lends.forRepaymentAsset(8, 5).size()); assertTrue(lends.forRepaymentAsset(9, 5).isEmpty());
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM accounts"));
    }
    @Test public void interestRowsKeepAllSignsAndUseInclusiveOutTimeTypeFourAndOwnerScope() {
        OriginalLendRecord lower = saved(7, 1, 5, 99, 4, 100, -2), upper = saved(7, 2, 5, 99, 4, 200, 0);
        saved(7, 3, 5, 99, 4, 201, 5); saved(7, 4, 5, 99, 4, 99, 5);
        saved(7, 5, 5, 99, 3, 150, 5); saved(8, 6, 5, 99, 4, 150, 5); saved(7, 7, 99, 5, 4, 150, 5);
        List<OriginalLendRecord> rows = lends.interestRows(7, 5, 100, 200); assertEquals(2, rows.size());
        assertEquals(upper.localRowId, rows.get(0).localRowId); assertEquals(lower.localRowId, rows.get(1).localRowId);
        assertEquals(0, rows.get(0).interest, 0); assertEquals(-2, rows.get(1).interest, 0);
        assertTrue(rows.get(0).generalServiceBill); assertTrue(rows.get(0).originalDeleted);
        assertTrue(lends.interestRows(7, 5, 200, 100).isEmpty());
    }
    @Test public void MSelectsTypeScopedInsertionOrderDirectionAndEvenNegativeRawBillIds() {
        OriginalBillRecord expense = bill(101, 0, 0, 99, 0); expense.tags = Arrays.asList(8L, 8L, null);
        new OriginalBillRepository(store).saveOriginalRaw(expense, null); link(0, 4, 101);
        bill(102, 0, 0, 99, 42); link(0, 3, 102);
        bill(103, 0, 0, 9, 911); link(0, 4, 103); link(7, 4, 102); link(0, 4, 0); link(0, 4, -1);
        assertEquals(101, lends.latestInterestBill(0, 0, 4, false).billId);
        assertEquals(Arrays.asList(8L, 8L, null), lends.latestInterestBill(0, 0, 4, false).tags);
        assertEquals(103, lends.latestInterestBill(0, 0, 4, true).billId);
        OriginalBillRecord negative = bill(-2, 0, 0, 99, 0); link(0, 4, -2);
        assertEquals(-2, lends.latestInterestBill(0, 0, 4, false).billId);
        store.getWritableDatabase().delete("transactions", "id=?", new String[]{"" + negative.localRowId});
        assertEquals(101, lends.latestInterestBill(0, 0, 4, false).billId);
        assertEquals(102, lends.latestInterestBill(0, 0, 3, false).billId);
    }
    @Test public void MUsesTheCurrentBookOwnersAccessibleBillDomainRatherThanFeeBookEquality() {
        bill(201, 0, 0, 99, 0); link(0, 4, 201);
        bill(202, 7, 7, 99, 0); link(0, 4, 202);
        assertEquals(201, lends.latestInterestBill(0, 0, 4, false).billId);
        assertEquals(202, lends.latestInterestBill(0, 7, 4, false).billId);
        assertNull(lends.latestInterestBill(7, 7, 4, false));
    }
    @Test public void ignoredHeaderInsertReturnsFalseWithoutAssigningAPkOrChangingRawOwnerAndTimes() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER ignored_raw_lend BEFORE INSERT ON original_lends BEGIN SELECT RAISE(IGNORE); END");
        OriginalLendRecord r = raw(-7, -8); OriginalLendRepository.SaveResult result = lends.trySaveOriginalRaw(r);
        assertFalse(result.saved); assertSame(r, result.record); assertFalse(result.failureType.isEmpty());
        assertEquals(0, r.localRowId); assertEquals(-7, r.userId); assertEquals(-8, r.lendId);
        assertEquals(300, r.createTime); assertEquals(400, r.updateTime); assertEquals(0, rows());
        assertFalse(store.getWritableDatabase().inTransaction());
    }
    @Test public void updateExceptionsReturnFalseButNormalZeroRowUpdatesRemainSuccessful() {
        OriginalLendRecord r = raw(7, 8); lends.saveOriginalRaw(r); long row = r.localRowId;
        store.getWritableDatabase().execSQL("CREATE TRIGGER rejected_raw_lend_update BEFORE UPDATE ON original_lends BEGIN SELECT RAISE(ABORT,'synthetic scalar'); END");
        r.number = 99; OriginalLendRepository.SaveResult failed = lends.trySaveOriginalRaw(r);
        assertFalse(failed.saved); assertSame(r, failed.record); assertEquals(99, r.number, 0);
        assertEquals(-12.5, lends.byLocalRowId(row).number, 0); assertFalse(store.getWritableDatabase().inTransaction());
        store.getWritableDatabase().execSQL("DROP TRIGGER rejected_raw_lend_update");
        store.getWritableDatabase().delete("original_lends", "id=?", new String[]{"" + row});
        OriginalLendRepository.SaveResult missing = lends.trySaveOriginalRaw(r);
        assertTrue(missing.saved); assertEquals(row, r.localRowId); assertEquals(0, rows());
    }
    @Test public void physicalDeleteResetsOnlyTheModelPkAndLeavesAssetsBillsAndFinancialHistoriesAlone() {
        AssetRepository.Definition definition = new AssetRepository.Definition("合成原始账户", 1, null); definition.assetNumber = 100;
        long account = store.assetRepository().insertRecovered(0, 77, 1, 0, definition);
        OriginalBillRecord bill = bill(301, 0, 0, 99, 0);
        OriginalLendRecord r = raw(0, 0); r.assetId = 77; r.repaymentAssetId = -1; r.billId = 301; lends.saveOriginalRaw(r);
        long row = r.localRowId; assertEquals(1, lends.deleteRaw(r)); assertEquals(0, r.localRowId);
        assertEquals(0, r.lendId); assertEquals(77, r.assetId); assertEquals(-1, r.repaymentAssetId); assertEquals(301, r.billId);
        assertNull(lends.byLocalRowId(row)); assertEquals(0, lends.deleteRaw(r));
        assertNotNull(new OriginalBillRepository(store).byLocalRowId(bill.localRowId));
        assertEquals(100, store.assetRepository().byRowId(account).optDouble("asset_number"), 0);
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_curd_histories"));
    }
    @Test public void strictStorageRejectsMalformedIntegersFlagsAndNonfiniteAmountsWithoutChangingRawModels() {
        OriginalLendRecord r = raw(Integer.MIN_VALUE, Integer.MAX_VALUE); lends.saveOriginalRaw(r); SQLiteDatabase db = store.getWritableDatabase();
        for (String mutation : new String[]{"owner_semantic_id=2147483648", "lend_semantic_id=-2147483649", "bill_semantic_id=2147483648",
                "type=2147483648", "out_time=1.5", "number='invalid'", "general_service_bill=-1", "original_delete_flag=2", "id=-7"}) {
            db.beginTransaction();
            try {
                db.execSQL("UPDATE original_lends SET " + mutation);
                try { lends.validateRestored(); fail("Malformed raw Lend accepted: " + mutation); }
                catch (IllegalArgumentException expected) {}
            } finally { db.endTransaction(); }
            assertEquals(1, rows()); assertNotNull(lends.byLocalRowId(r.localRowId));
        }
        for (int field = 0; field < 3; field++) {
            OriginalLendRecord bad = r.copy();
            if (field == 0) bad.number = Double.NaN; else if (field == 1) bad.fromCost = Double.POSITIVE_INFINITY; else bad.interest = Double.NEGATIVE_INFINITY;
            OriginalLendRepository.SaveResult result = lends.trySaveOriginalRaw(bad);
            assertFalse(result.saved); assertSame(bad, result.record); assertEquals(r.localRowId, bad.localRowId);
            assertEquals(IllegalArgumentException.class.getName(), result.failureType);
            assertEquals(-12.5, lends.byLocalRowId(r.localRowId).number, 0);
        }
        lends.validateRestored();
    }
    private static final class SequenceRandom extends Random {
        private final int[] values; private int position;
        SequenceRandom(int... values) { this.values = values; }
        @Override public int nextInt(int bound) { assertEquals(Integer.MAX_VALUE, bound); return values[position++]; }
    }
}
