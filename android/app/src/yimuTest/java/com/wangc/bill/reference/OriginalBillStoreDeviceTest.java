package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Domain persistence only: no implicit asset mutation, FX, parser or category learning. */
@RunWith(AndroidJUnit4.class)
public final class OriginalBillStoreDeviceTest {
    private static final String DATABASE = "reference_original_bill_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private OriginalBillRepository bills;
    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase();
        bills = new OriginalBillRepository(store);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DATABASE); }

    private OriginalBillRecord record(int user, int semantic) {
        OriginalBillRecord r = new OriginalBillRecord();
        r.billId = semantic; r.userId = user; r.bookId = 9000000001L;
        r.cost = 12.0d; r.parentCategoryId = 99; r.childCategoryId = -1;
        r.time = 1735689600000L;
        return r;
    }
    private ContentValues projection(long amount) {
        ContentValues p = new ContentValues();
        p.put("book_id", 1); p.put("kind", "expense"); p.put("amount", amount); p.put("currency", "CNY");
        p.put("category_id", store.categoryRepository().parent(99).optLong("id")); p.put("parent_semantic_id", 99); p.put("child_semantic_id", -1);
        p.put("time", 1735689600000L); p.put("created", 1735689600000L);
        return p;
    }

    @Test public void semanticIdentityOwnerSenderAndAssetBridgeRemainSeparate() {
        OriginalBillRecord first = record(7, 901), second = record(8, 901);
        first.fromUserId = 9;
        ContentValues firstProjection = projection(1200); firstProjection.put("owner_semantic_id", 55);
        bills.saveOriginalRaw(first, firstProjection); bills.saveOriginalRaw(second, projection(1200));
        assertNotEquals(901, first.localRowId); assertNotEquals(first.localRowId, second.localRowId);
        assertEquals(first.localRowId, bills.byExactUser(7, 901).localRowId);
        assertEquals(second.localRowId, bills.byExactUser(8, 901).localRowId);
        assertNull(bills.byExactUser(55, 901)); assertNull(bills.byExactUser(9, 901));
        assertEquals(first.localRowId, bills.findAccessible(9, 9, 901).localRowId);
        assertEquals(second.localRowId, bills.findAccessible(99, 8, 901).localRowId);
        assertNull(bills.findAccessible(55, 55, 901));
        assertEquals(55, store.transaction(first.localRowId).optInt("owner_semantic_id"));
    }

    @Test public void semanticAllocationIncludesZeroAndRetriesCurrentUserOrSenderCollisions() {
        OriginalBillRecord occupied = record(8, 0); occupied.fromUserId = 7;
        bills.saveOriginalRaw(occupied, projection(1200));
        OriginalBillRecord otherOwner = record(88, 55); bills.saveOriginalRaw(otherOwner, projection(1200));
        assertEquals(55, new OriginalBillRepository(store, new SequenceRandom(0, 55)).allocateSemanticId(7));
        assertEquals(0, new OriginalBillRepository(store, new SequenceRandom(0)).allocateSemanticId(99));
        assertEquals(0, bills.byExactUser(8, 0).billId);
    }

    @Test public void netZeroAndNegativeRawCostPersistWithoutInferringMinorUnitsOrFx() {
        OriginalBillRecord zero = record(7, 902); zero.cost = 0.0d;
        bills.saveOriginalRaw(zero, projection(0));
        assertEquals(0.0d, bills.byLocalRowId(zero.localRowId).cost, 0.0d);
        assertEquals(0, store.transaction(zero.localRowId).optLong("amount"));
        OriginalBillRecord negative = record(7, 903); negative.cost = -1.234567d;
        negative.currencyInfo = "unparsed"; negative.currencyAssetNumber = 7.125d;
        bills.saveOriginalRaw(negative, projection(9900));
        OriginalBillRecord saved = bills.byLocalRowId(negative.localRowId);
        assertEquals(-1.234567d, saved.cost, 0.0d); assertEquals(7.125d, saved.getCurrencyCost(), 0.0d);
        assertEquals(9900, store.transaction(negative.localRowId).optLong("amount"));
        bills.validateRestored();
    }

    @Test public void nullReferencesOmitExistingValuesWhileEmptyStringsAndZeroFlagsWrite() {
        OriginalBillRecord original = record(7, 904); original.remark = "kept"; original.currencyInfo = "USD 10";
        original.cost = 10; original.notIntoBudget = true; original.reimbursement = true;
        bills.saveOriginalRaw(original, projection(1000));
        OriginalBillRecord edited = bills.byLocalRowId(original.localRowId);
        edited.remark = null; edited.currencyInfo = null; edited.cost = 0;
        edited.notIntoBudget = false; edited.reimbursement = false;
        bills.saveOriginalRaw(edited, projection(0));
        OriginalBillRecord preserved = bills.byLocalRowId(original.localRowId);
        assertEquals("kept", preserved.remark); assertEquals("USD 10", preserved.currencyInfo);
        assertEquals(0.0d, preserved.cost, 0.0d); assertFalse(preserved.notIntoBudget); assertFalse(preserved.reimbursement);
        edited.remark = ""; edited.currencyInfo = ""; bills.saveOriginalRaw(edited, null);
        assertEquals("", bills.byLocalRowId(original.localRowId).remark);
        assertEquals("", bills.byLocalRowId(original.localRowId).currencyInfo);
    }

    @Test public void tagsPreserveOrderDuplicatesAndNullAndOnlyExplicitEmptyClears() {
        OriginalBillRecord r = record(7, 905);
        r.tags = new ArrayList<>(Arrays.asList(9L, 3L, 9L, null)); bills.saveOriginalRaw(r, projection(1200));
        assertEquals(Arrays.asList(9L, 3L, 9L, null), bills.byLocalRowId(r.localRowId).tags);
        r.tags = null; bills.saveOriginalRaw(r, null);
        assertEquals(Arrays.asList(9L, 3L, 9L, null), bills.byLocalRowId(r.localRowId).tags);
        r.tags = new ArrayList<>(); bills.saveOriginalRaw(r, null); assertTrue(bills.byLocalRowId(r.localRowId).tags.isEmpty());
        OriginalBillRecord absent = record(7, 906); absent.tags = null; bills.saveOriginalRaw(absent, projection(1200));
        assertNull(bills.byLocalRowId(absent.localRowId).tags);
    }

    @Test public void legacyRowsNeverAcquireARecoveredIdentityFromTheirPrimaryKey() {
        ContentValues fixture = projection(500); fixture.put("bill_origin", "legacy");
        long legacy = store.getWritableDatabase().insertOrThrow("transactions", null, fixture);
        assertNull(bills.byLocalRowId(legacy)); assertNull(bills.byExactUser(0, legacy));
        for (String column : OriginalBillRepository.CANONICAL_COLUMNS) assertTrue(column, store.transaction(legacy).isNull(column));
        OriginalBillRecord forbidden = record(7, 907); forbidden.localRowId = legacy;
        OriginalBillRepository.SaveResult result = bills.trySaveOriginalRaw(forbidden, projection(1200));
        assertFalse(result.saved); assertSame(forbidden, result.record);
        assertEquals("legacy", store.transaction(legacy).optString("bill_origin"));
        assertEquals(500, store.transaction(legacy).optLong("amount"));
    }

    @Test public void laterGenericFailureRollsBackSqlButKeepsTheAssignedInMemoryId() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_bill_tags_failure BEFORE UPDATE OF bill_tags ON transactions BEGIN SELECT RAISE(ABORT,'private synthetic marker'); END");
        OriginalBillRecord r = record(7, 908);
        OriginalBillRepository.SaveResult result = bills.trySaveOriginalRaw(r, projection(1200));
        assertFalse(result.saved); assertSame(r, result.record); assertFalse(result.failureType.isEmpty());
        assertFalse(result.failureType.contains("private synthetic marker")); assertTrue(r.localRowId > 0);
        assertEquals(908, r.billId); assertNull(bills.byLocalRowId(r.localRowId));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions"));
    }

    @Test public void anUpdateAffectingZeroRowsStillReportsSaveSuccessWithoutInserting() {
        OriginalBillRecord r = record(7, 909); r.localRowId = 99999;
        OriginalBillRepository.SaveResult result = bills.trySaveOriginalRaw(r, null);
        assertTrue(result.saved); assertEquals("", result.failureType); assertEquals(99999, r.localRowId);
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions"));
    }

    @Test public void ignoredInsertFailsBeforeAssigningTheInMemoryPrimaryKey() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_bill_ignore BEFORE INSERT ON transactions WHEN NEW.bill_origin='original' BEGIN SELECT RAISE(IGNORE); END");
        OriginalBillRecord r = record(7, 911); r.tags = null;
        OriginalBillRepository.SaveResult result = bills.trySaveOriginalRaw(r, projection(1200));
        assertFalse(result.saved); assertSame(r, result.record);
        assertEquals(0, r.localRowId); assertEquals(911, r.billId);
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions"));
    }

    @Test public void saveDoesNotCatchErrorsAndItsTransactionStillCloses() {
        AssertionError failure = new AssertionError("synthetic generic failure");
        OriginalBillRecord r = record(7, 910);
        r.tags = new AbstractList<Long>() {
            @Override public Long get(int index) { throw failure; }
            @Override public int size() { return 1; }
        };
        try { bills.trySaveOriginalRaw(r, projection(1200)); fail(); }
        catch (AssertionError error) { assertSame(failure, error); }
        assertTrue(r.localRowId > 0); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertFalse(store.getWritableDatabase().inTransaction());
    }

    @Test public void allOriginalScalarFieldsRoundTripWithoutUsingExperimentalIds() {
        OriginalBillRecord r = record(-7, Integer.MIN_VALUE);
        r.assetId = Long.MIN_VALUE; r.bookId = Long.MAX_VALUE; r.billType = -3;
        r.childCategoryId = -1; r.parentCategoryId = 9; r.cost = -7.5d;
        r.currencyAssetNumber = 20.25d; r.currencyDiscountNumber = 0.125d; r.discountNumber = 0.5d;
        r.fromUserId = Integer.MAX_VALUE; r.inAssetTime = -4; r.recordTime = -5; r.time = 0; r.updateTime = -6;
        r.notIntoBudget = true; r.notIntoTotal = true; r.reimbursement = true; r.reimbursementEnd = true;
        r.currencyInfo = "raw text"; r.billShareInfo = "raw share"; r.remark = "raw remark";
        r.poiAddress = "raw poi"; r.totalAddress = "raw total";
        bills.saveOriginalRaw(r, projection(750));
        OriginalBillRecord got = bills.byLocalRowId(r.localRowId);
        assertEquals(Integer.MIN_VALUE, got.billId); assertEquals(-7, got.userId); assertEquals(Integer.MAX_VALUE, got.fromUserId);
        assertEquals(Long.MIN_VALUE, got.assetId); assertEquals(Long.MAX_VALUE, got.bookId); assertEquals(-3, got.billType);
        assertEquals(9, got.parentCategoryId); assertEquals(-1, got.childCategoryId); assertEquals(-7.5d, got.cost, 0.0d);
        assertEquals(20.25d, got.currencyAssetNumber, 0.0d); assertEquals(0.125d, got.currencyDiscountNumber, 0.0d); assertEquals(0.5d, got.discountNumber, 0.0d);
        assertEquals(-4, got.inAssetTime); assertEquals(-5, got.recordTime); assertEquals(0, got.time); assertEquals(-6, got.updateTime);
        assertTrue(got.notIntoBudget); assertTrue(got.notIntoTotal); assertTrue(got.reimbursement); assertTrue(got.reimbursementEnd);
        assertEquals("raw text", got.currencyInfo); assertEquals("raw share", got.billShareInfo); assertEquals("raw remark", got.remark);
        assertEquals("raw poi", got.poiAddress); assertEquals("raw total", got.totalAddress); assertTrue(got.tags.isEmpty());
        bills.validateRestored();
    }

    private static final class SequenceRandom extends Random {
        private final int[] values;
        private int index;
        SequenceRandom(int... values) { this.values = values; }
        @Override public int nextInt(int bound) { assertEquals(Integer.MAX_VALUE, bound); return values[index++]; }
    }
}
