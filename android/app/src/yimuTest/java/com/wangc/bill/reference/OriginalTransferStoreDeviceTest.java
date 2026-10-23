package com.wangc.bill.reference;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Raw entities and bundled SaveHandler boundaries; no transfer command or UI is claimed. */
@RunWith(AndroidJUnit4.class)
public final class OriginalTransferStoreDeviceTest {
    private static final String DATABASE = "reference_original_transfer_store_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private OriginalTransferRepository transfers;
    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase();
        transfers = new OriginalTransferRepository(store);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DATABASE); }
    private OriginalTransferRecord raw(int owner, long semantic, long from, long to, long time) {
        OriginalTransferRecord r = new OriginalTransferRecord();
        r.userId = owner; r.transferId = semantic; r.fromAssetId = from; r.toAssetId = to;
        r.time = time; r.updateTime = 123; r.cost = -10.125; r.toCost = 7.5;
        r.serviceCharge = -0.25; r.billId = -1; r.remark = "合成原转账";
        return r;
    }
    private OriginalTransferRecord save(int owner, long semantic, long from, long to, long time) {
        OriginalTransferRecord r = raw(owner, semantic, from, to, time); transfers.saveOriginalRaw(r); return r;
    }
    private long rows() { return store.scalar("SELECT COUNT(*) n FROM original_transfers"); }
    private long tags() { return store.scalar("SELECT COUNT(*) n FROM original_transfer_tags"); }

    @Test public void originalLongIdsSignedFieldsAndZeroRemainIndependentOfAllLocalKeys() {
        OriginalTransferRecord r = raw(Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE, 0, Long.MIN_VALUE);
        r.billId = Integer.MAX_VALUE; r.updateTime = Long.MAX_VALUE; r.originalDeleted = true;
        transfers.saveOriginalRaw(r); OriginalTransferRecord read = transfers.byIdentity(Integer.MIN_VALUE, Long.MAX_VALUE);
        assertNotEquals(r.transferId, r.localRowId); assertEquals(r.localRowId, read.localRowId);
        assertEquals(Long.MIN_VALUE, read.fromAssetId); assertEquals(0, read.toAssetId);
        assertEquals(-10.125, read.cost, 0); assertEquals(7.5, read.toCost, 0);
        assertEquals(-.25, read.serviceCharge, 0); assertEquals(Integer.MAX_VALUE, read.billId);
        assertEquals(Long.MIN_VALUE, read.time); assertEquals(Long.MAX_VALUE, read.updateTime);
        assertTrue(read.originalDeleted); assertEquals("合成原转账", read.remark);
        assertTrue(read.tags.isEmpty()); transfers.validateRestored();
    }

    @Test public void ownerIdentityDuplicatesAndEndpointTimeOrderHaveNoBookOrDeleteFilter() {
        OriginalTransferRecord first = save(7, 4, 99, 199, 20);
        OriginalTransferRecord duplicate = save(7, 4, 299, 99, 10);
        duplicate.originalDeleted = true; transfers.saveOriginalRaw(duplicate);
        save(8, 4, 99, 199, 30); save(7, 5, 41, 42, 40);
        assertEquals(3, transfers.allForOwner(7).size());
        assertEquals(first.localRowId, transfers.byIdentity(7, 4).localRowId);
        List<OriginalTransferRecord> read = transfers.forAsset(7, 99);
        assertEquals(2, read.size()); assertEquals(20, read.get(0).time);
        assertEquals(duplicate.localRowId, read.get(1).localRowId); assertTrue(read.get(1).originalDeleted);
        assertEquals(1, transfers.forAsset(8, 99).size()); assertTrue(transfers.forAsset(0, 99).isEmpty());
        assertEquals(duplicate.localRowId, transfers.lastTo(7, 99).localRowId);
        assertEquals(first.localRowId, transfers.lastFrom(7, 99).localRowId); transfers.validateRestored();
    }

    @Test public void primitiveZerosOverwriteWhileNullRemarkAndNullUpdateCollectionAreOmitted() {
        OriginalTransferRecord r = save(7, 8, 99, 199, 20);
        r.tags = Arrays.asList(3L, 4L); r.originalDeleted = true; transfers.saveOriginalRaw(r);
        r.userId = 0; r.transferId = 0; r.fromAssetId = 0; r.toAssetId = 0; r.billId = 0;
        r.cost = 0; r.toCost = 0; r.serviceCharge = 0; r.time = 0; r.updateTime = 0;
        r.remark = null; r.tags = null; r.originalDeleted = false; transfers.saveOriginalRaw(r);
        assertNull(transfers.byIdentity(7, 8)); OriginalTransferRecord read = transfers.byIdentity(0, 0);
        assertEquals(r.localRowId, read.localRowId); assertEquals(0, read.fromAssetId); assertEquals(0, read.toAssetId);
        assertEquals(0, read.billId); assertEquals(0, read.time); assertEquals(0, read.updateTime);
        assertEquals(0, read.cost, 0); assertEquals(0, read.toCost, 0); assertEquals(0, read.serviceCharge, 0);
        assertFalse(read.originalDeleted); assertEquals("合成原转账", read.remark);
        assertEquals(Arrays.asList(3L, 4L), read.tags);
        r.remark = ""; r.tags = new ArrayList<>(); transfers.saveOriginalRaw(r);
        assertEquals("", transfers.byLocalRowId(r.localRowId).remark);
        assertTrue(transfers.byLocalRowId(r.localRowId).tags.isEmpty()); assertEquals(0, tags());
    }

    @Test public void nullListEmptyListDuplicatesAndBoxedNullElementsRoundTripWithoutNormalization() {
        OriginalTransferRecord absent = raw(7, 1, 0, -1, 0); absent.tags = null; absent.remark = null;
        transfers.saveOriginalRaw(absent); assertNull(transfers.byLocalRowId(absent.localRowId).tags);
        assertNull(transfers.byLocalRowId(absent.localRowId).remark);
        OriginalTransferRecord r = raw(7, 2, 0, -1, 0);
        r.tags = Arrays.asList(Long.MAX_VALUE, null, Long.MIN_VALUE, Long.MAX_VALUE, null);
        transfers.saveOriginalRaw(r); OriginalTransferRecord read = transfers.byLocalRowId(r.localRowId);
        assertEquals(r.tags, read.tags); assertEquals(5, tags());
        assertEquals(2, store.scalar("SELECT COUNT(*) n FROM original_transfer_tags WHERE value IS NULL"));
        r.setTags(r.tags); transfers.saveOriginalRaw(r);
        assertEquals(Arrays.asList(Long.MAX_VALUE, null, Long.MIN_VALUE), transfers.byLocalRowId(r.localRowId).tags);
        transfers.validateRestored();
    }

    @Test public void semanticAllocatorIncludesZeroAndRetriesOnlyTheSameOwner() {
        save(7, 0, 1, 2, 0); save(8, 42, 1, 2, 0);
        OriginalTransferRepository deterministic = new OriginalTransferRepository(store, new SequenceRandom(0, 42));
        assertEquals(42, deterministic.allocateSemanticId(7));
        assertEquals(0, new OriginalTransferRepository(store, new SequenceRandom(0)).allocateSemanticId(99));
        assertEquals(2, rows()); // Allocation alone does not save, stamp, or reserve an entity.
    }

    @Test public void ignoredHeaderInsertionReturnsFalseWithoutInventingOwnerTimeOrPersistedPk() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_transfer_ignore BEFORE INSERT ON original_transfers BEGIN SELECT RAISE(IGNORE); END");
        OriginalTransferRecord r = raw(7, 0, 0, -1, 0); r.updateTime = 0;
        OriginalTransferRepository.SaveResult result = transfers.trySaveOriginalRaw(r);
        assertFalse(result.saved); assertSame(r, result.record); assertFalse(result.failureType.isEmpty());
        assertEquals(0, r.localRowId); assertEquals(7, r.userId); assertEquals(0, r.transferId);
        assertEquals(0, r.updateTime); assertEquals(0, rows()); assertEquals(0, tags());
    }

    @Test public void genericSqlFailureDropsOnlyItsItemAndDoesNotTurnSaveIntoFalse() {
        SQLiteDatabase db = store.getWritableDatabase();
        for (String action : Arrays.asList("ABORT,'synthetic item'", "IGNORE")) {
            db.execSQL("CREATE TRIGGER synthetic_transfer_tag BEFORE INSERT ON original_transfer_tags WHEN NEW.value=2 BEGIN SELECT RAISE(" + action + "); END");
            OriginalTransferRecord r = raw(7, rows() + 1, 0, -1, 0); r.tags = Arrays.asList(1L, 2L, 3L);
            OriginalTransferRepository.SaveResult result = transfers.trySaveOriginalRaw(r);
            assertTrue(result.saved); assertEquals("", result.failureType); assertTrue(r.localRowId > 0);
            assertEquals(Arrays.asList(1L, 2L, 3L), r.tags);
            assertEquals(Arrays.asList(1L, 3L), transfers.byLocalRowId(r.localRowId).tags);
            r.cost = 99; r.tags = Arrays.asList(2L, 8L); assertTrue(transfers.trySaveOriginalRaw(r).saved);
            assertEquals(99, transfers.byLocalRowId(r.localRowId).cost, 0);
            assertEquals(Arrays.asList(8L), transfers.byLocalRowId(r.localRowId).tags);
            transfers.validateRestored(); db.execSQL("DROP TRIGGER synthetic_transfer_tag");
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test public void lateRuntimeGenericTypeExceptionRollsBackButPreservesTheAssignedModelPk() {
        OriginalTransferRecord r = raw(7, 9, 0, -1, 0); List invalid = new ArrayList();
        invalid.add(1L); invalid.add("not a boxed Long"); r.tags = invalid;
        OriginalTransferRepository.SaveResult result = transfers.trySaveOriginalRaw(r);
        assertFalse(result.saved); assertSame(r, result.record); assertTrue(r.localRowId > 0);
        assertEquals(ClassCastException.class.getName(), result.failureType);
        assertNull(transfers.byLocalRowId(r.localRowId)); assertEquals(0, rows()); assertEquals(0, tags());
        OriginalTransferRecord existing = save(7, 10, 1, 2, 1);
        existing.tags = Arrays.asList(7L); transfers.saveOriginalRaw(existing);
        existing.cost = 99; existing.tags = invalid;
        assertFalse(transfers.trySaveOriginalRaw(existing).saved);
        assertEquals(-10.125, transfers.byLocalRowId(existing.localRowId).cost, 0);
        assertEquals(Arrays.asList(7L), transfers.byLocalRowId(existing.localRowId).tags);
    }

    @Test public void lateErrorPropagatesUnchangedAndStillRollsBackTheEntityTransaction() {
        OriginalTransferRecord r = raw(7, 9, 0, -1, 0); AssertionError original = new AssertionError("synthetic failure");
        r.tags = new AbstractList<Long>() {
            @Override public int size() { return 1; }
            @Override public Long get(int index) { throw original; }
        };
        try { transfers.trySaveOriginalRaw(r); fail("Error was caught as save=false"); }
        catch (AssertionError error) { assertSame(original, error); }
        assertTrue(r.localRowId > 0); assertNull(transfers.byLocalRowId(r.localRowId));
        assertEquals(0, rows()); assertEquals(0, tags()); assertFalse(store.getWritableDatabase().inTransaction());
    }

    @Test public void aSavedButDeletedEntityIgnoresZeroUpdateRowsAndGenericInsertMinusOne() {
        OriginalTransferRecord r = save(7, 9, 1, 2, 0); long retained = r.localRowId;
        store.getWritableDatabase().delete("original_transfers", "id=?", new String[]{"" + retained});
        r.cost = 99; r.tags = Arrays.asList(1L, 2L);
        assertTrue(transfers.trySaveOriginalRaw(r).saved); assertEquals(retained, r.localRowId);
        assertNull(transfers.byIdentity(7, 9)); assertEquals(0, rows()); assertEquals(0, tags());
    }

    @Test public void physicalDeleteClearsOnlyTheModelPkAndItsGenericRowsWithoutFinancialEffects() {
        long account = store.saveAccount(0, "合成旧账户", "cash", "CNY", 12345);
        OriginalTransferRecord r = save(7, 9, account, Long.MAX_VALUE, 0);
        r.billId = Integer.MAX_VALUE; r.tags = Arrays.asList(1L, 1L); transfers.saveOriginalRaw(r);
        long opening = store.accountBalance(account), history = store.scalar("SELECT COUNT(*) n FROM asset_histories");
        assertEquals(1, transfers.deleteRaw(r)); assertEquals(0, r.localRowId); assertEquals(9, r.transferId);
        assertEquals(account, r.fromAssetId); assertEquals(0, rows()); assertEquals(0, tags());
        assertEquals(0, transfers.deleteRaw(r)); assertEquals(opening, store.accountBalance(account));
        assertEquals(history, store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions"));
    }

    @Test public void strictStorageValidationRejectsTruncationAndNonfiniteValuesButAllowsNullTags() {
        OriginalTransferRecord r = save(7, 9, 1, 2, 0); r.tags = Arrays.asList((Long) null); transfers.saveOriginalRaw(r);
        for (Object[] invalid : new Object[][]{{"owner_semantic_id", 2147483648L}, {"bill_semantic_id", 1.25},
                {"original_delete_flag", 2}, {"tags_is_null", 1}}) {
            SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
            try {
                db.execSQL("UPDATE original_transfers SET " + invalid[0] + "=? WHERE id=?", new Object[]{invalid[1], r.localRowId});
                try { transfers.validateRestored(); fail("Invalid raw metadata accepted: " + invalid[0]); }
                catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
            } finally { db.endTransaction(); }
        }
        SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
        try {
            db.execSQL("UPDATE original_transfer_tags SET value=1.25 WHERE transfer_row_id=?", new Object[]{r.localRowId});
            try { transfers.validateRestored(); fail("fractional tag accepted"); }
            catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
        } finally { db.endTransaction(); }
        transfers.validateRestored(); assertEquals(Arrays.asList((Long) null), transfers.byLocalRowId(r.localRowId).tags);
        r.cost = Double.NaN; assertFalse(transfers.trySaveOriginalRaw(r).saved);
        assertEquals(-10.125, transfers.byLocalRowId(r.localRowId).cost, 0);
    }

    private static final class SequenceRandom extends Random {
        private final int[] values; private int position;
        SequenceRandom(int... values) { this.values = values; }
        @Override public int nextInt(int bound) { return values[position++]; }
    }
}
