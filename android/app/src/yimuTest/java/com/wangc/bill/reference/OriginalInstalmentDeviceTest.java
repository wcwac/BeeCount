package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Actual SQLite g1 stages with synthetic models, never a repayment/date approximation. */
@RunWith(AndroidJUnit4.class)
public final class OriginalInstalmentDeviceTest {
    private static final String DATABASE = "reference_original_instalment_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private OriginalInstalmentRepository repository;
    private final List<Long> refreshed = new ArrayList<>();
    private OriginalInstalmentCommands commands;
    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase();
        repository = new OriginalInstalmentRepository(store); refreshed.clear();
        commands = new OriginalInstalmentCommands(store, 7, 8, refreshed::add, () -> 999L);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DATABASE); }
    private OriginalInstalmentRecord row(int owner, long semantic, int bill, long asset, double total) {
        OriginalInstalmentRecord r = new OriginalInstalmentRecord(); r.userId = owner; r.instalmentId = semantic;
        r.billId = bill; r.assetId = asset; r.totalNumber = total; r.periods = 3;
        return r;
    }
    private long asset(int owner, long semantic, int type) {
        return store.assetRepository().insertRecovered(owner, semantic, 1, 0,
                new AssetRepository.Definition("合成账户 " + semantic, type, null));
    }
    private OriginalBillRecord bill(int owner, int semantic, long asset, double cost) {
        OriginalBillRecord r = new OriginalBillRecord(); r.userId = owner; r.billId = semantic;
        r.assetId = asset; r.cost = cost; r.bookId = 1; r.parentCategoryId = 99; r.childCategoryId = -1;
        ContentValues p = new ContentValues(); p.put("book_id", 1); p.put("kind", "expense"); p.put("amount", 100);
        p.put("currency", "CNY"); p.put("category_id", store.categoryRepository().parent(99).optLong("id"));
        p.put("parent_semantic_id", 99); p.put("child_semantic_id", -1); p.put("time", 1); p.put("created", 1);
        new OriginalBillRepository(store).saveOriginalRaw(r, p); return r;
    }

    @Test public void rawStorageKeepsOwnersLongIdsDuplicatesNullAndEmptyReferencesSeparate() {
        OriginalInstalmentRecord r = row(7, Long.MAX_VALUE, -4, Long.MIN_VALUE, -3.125d);
        r.accountMonth = " raw-month "; r.selfInfo = " unrecognized JSON "; r.originalDeleted = true;
        r.inAssetTime = Long.MIN_VALUE; r.instalmentType = -2; r.remainderType = 99;
        r.serviceNumber = -0.125d; r.serviceType = -3; r.updateTime = -9;
        repository.saveOriginalRaw(r); OriginalInstalmentRecord same = repository.byIdentity(7, Long.MAX_VALUE);
        assertNotEquals(r.instalmentId, same.localRowId); assertEquals(Long.MIN_VALUE, same.assetId);
        assertEquals(-4, same.billId); assertEquals(-3.125d, same.totalNumber, 0.0d); assertEquals(-0.125d, same.serviceNumber, 0.0d);
        assertEquals(Long.MIN_VALUE, same.inAssetTime); assertEquals(-2, same.instalmentType); assertEquals(99, same.remainderType);
        assertEquals(-3, same.serviceType); assertEquals(-9, same.updateTime); assertTrue(same.originalDeleted);
        same.accountMonth = null; same.selfInfo = null; same.totalNumber = 0; repository.saveOriginalRaw(same);
        assertEquals(" raw-month ", repository.byLocalRowId(r.localRowId).accountMonth);
        assertEquals(" unrecognized JSON ", repository.byLocalRowId(r.localRowId).selfInfo);
        same.accountMonth = ""; same.selfInfo = ""; repository.saveOriginalRaw(same);
        assertEquals("", repository.byLocalRowId(r.localRowId).selfInfo); assertEquals(0, repository.byLocalRowId(r.localRowId).totalNumber, 0);
        repository.saveOriginalRaw(row(7, Long.MAX_VALUE, -4, 1, 2)); repository.saveOriginalRaw(row(8, Long.MAX_VALUE, -4, 1, 3));
        assertEquals(2, repository.allForOwner(7).size()); assertEquals(1, repository.allForOwner(8).size());
        repository.validateRestored();
    }

    @Test public void allocatorAcceptsZeroRetriesOnlyItsOwnerAndNeverUsesTheLocalPrimaryKey() {
        repository.saveOriginalRaw(row(7, 0, 0, 0, 0)); repository.saveOriginalRaw(row(8, 55, 0, 0, 0));
        assertEquals(55, new OriginalInstalmentRepository(store, new SequenceRandom(0, 55)).allocateSemanticId(7));
        assertEquals(0, new OriginalInstalmentRepository(store, new SequenceRandom(0)).allocateSemanticId(99));
    }

    @Test public void rawQueriesKeepTheOriginalBillAssetOwnerAndStrictTimePredicates() {
        OriginalInstalmentRecord a = row(7, 1, 901, -1, 10); a.updateTime = 10; repository.saveOriginalRaw(a);
        OriginalInstalmentRecord b = row(7, 2, 0, -1, 20); b.updateTime = 11; repository.saveOriginalRaw(b);
        repository.saveOriginalRaw(row(8, 1, 901, -1, 99));
        assertEquals(a.localRowId, repository.firstByBill(7, 901).localRowId);
        assertEquals(2, repository.forAsset(7, -1).size()); assertEquals(1, repository.billLinkedForOwner(7).size());
        assertEquals(1, repository.updatedAfter(7, 10).size()); assertEquals(b.localRowId, repository.updatedAfter(7, 10).get(0).localRowId);
        assertEquals(11, repository.maxUpdatedTime(7)); assertEquals(0, repository.maxUpdatedTime(99));
    }

    @Test public void zeroRowUpdateStillSucceedsAndIgnoredInsertKeepsAnUnassignedLocalId() {
        OriginalInstalmentRecord absent = row(7, 8, 0, 0, 0); absent.localRowId = 99999;
        assertTrue(repository.trySaveOriginalRaw(absent).saved); assertEquals(0, repository.allForOwner(7).size());
        store.getWritableDatabase().execSQL("CREATE TRIGGER ignore_instalment BEFORE INSERT ON original_instalments BEGIN SELECT RAISE(IGNORE); END");
        OriginalInstalmentRecord r = row(7, 9, 0, 0, 0); OriginalInstalmentRepository.SaveResult failed = repository.trySaveOriginalRaw(r);
        assertFalse(failed.saved); assertSame(r, failed.record); assertEquals(0, r.localRowId); assertEquals(9, r.instalmentId);
        assertFalse(failed.failureType.isEmpty());
    }

    @Test public void refundCostChangeOnlyReducesTheFirstCurrentOwnersInstalmentThenRefreshesItsCreditAsset() {
        asset(7, 10, 2); asset(8, 10, 2); OriginalBillRecord parent = bill(8, 901, 10, 12);
        OriginalInstalmentRecord own = row(7, 1, 901, 0, 20); own.serviceNumber = 3; repository.saveOriginalRaw(own);
        OriginalInstalmentRecord duplicate = row(7, 2, 901, 0, 30); repository.saveOriginalRaw(duplicate);
        OriginalInstalmentRecord other = row(8, 3, 901, 0, 40); repository.saveOriginalRaw(other);
        commands.adjustAfterRefund(parent);
        assertEquals(12, repository.byLocalRowId(own.localRowId).totalNumber, 0);
        assertEquals(3, repository.byLocalRowId(own.localRowId).serviceNumber, 0);
        assertEquals(999, repository.byLocalRowId(own.localRowId).updateTime);
        assertEquals(30, repository.byLocalRowId(duplicate.localRowId).totalNumber, 0);
        assertEquals(40, repository.byLocalRowId(other.localRowId).totalNumber, 0); assertEquals(Arrays.asList(10L), refreshed);
        parent.cost = 50; commands.adjustAfterRefund(parent); parent.cost = -12; commands.adjustAfterRefund(parent);
        assertEquals(Arrays.asList(10L), refreshed); // No increase and no save for equal absolute cost.
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM asset_histories"));
    }

    @Test public void exactAndSignedZeroRefundPhysicallyDeleteInsteadOfSavingATombstoneOrZeroPrincipal() {
        asset(7, 10, 2); OriginalBillRecord parent = bill(7, 901, 10, 0);
        for (double zero : new double[]{0.0d, -0.0d}) {
            OriginalInstalmentRecord r = row(7, 1, 901, 0, 20); repository.saveOriginalRaw(r);
            parent.cost = zero; commands.adjustAfterRefund(parent); assertNull(repository.byLocalRowId(r.localRowId));
        }
        assertEquals(Arrays.asList(10L, 10L), refreshed); assertNotNull(new OriginalBillRepository(store).byLocalRowId(parent.localRowId));
    }

    @Test public void aFalseInstalmentSaveStillRefreshesAfterTheIndependentBillSave() {
        asset(7, 10, 2); OriginalBillRecord parent = bill(7, 901, 10, 12);
        OriginalInstalmentRecord r = row(7, 1, 901, 0, 20); repository.saveOriginalRaw(r);
        store.getWritableDatabase().execSQL("CREATE TRIGGER reject_instalment_update BEFORE UPDATE ON original_instalments BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        commands.adjustAfterRefund(parent);
        assertEquals(20, repository.byLocalRowId(r.localRowId).totalNumber, 0); assertEquals(0, repository.byLocalRowId(r.localRowId).updateTime);
        assertEquals(Arrays.asList(10L), refreshed); assertEquals(12, new OriginalBillRepository(store).byLocalRowId(parent.localRowId).cost, 0);
        OriginalInstalmentRecord model = repository.byLocalRowId(r.localRowId); model.totalNumber = 11;
        OriginalInstalmentRepository.SaveResult failed = commands.saveUpdated(model);
        assertFalse(failed.saved); assertSame(model, failed.record); assertEquals(11, model.totalNumber, 0); assertEquals(999, model.updateTime);
        assertEquals(Arrays.asList(10L, 10L), refreshed);
    }

    @Test public void creditRefreshUsesBillPrecedenceRawMinusOneAndTypeTwoWithoutAnAssetFallback() {
        asset(7, 10, 2); asset(7, -1, 2); asset(7, 11, 1);
        bill(7, 901, -1, 10); bill(7, 902, 0, 10); bill(99, 903, 10, 10);
        commands.saveUpdated(row(7, 1, 0, 10, 1)); // Standalone.
        commands.saveUpdated(row(7, 2, 901, 10, 1)); // Linked Bill's -1 passes the original !=0 gate.
        commands.saveUpdated(row(7, 3, 902, 10, 1)); // Bill asset0 must not use its own asset10.
        commands.saveUpdated(row(7, 4, 903, 10, 1)); // Inaccessible Bill must not fall back.
        commands.saveUpdated(row(7, 5, 999, 10, 1)); // Missing Bill must not fall back.
        commands.saveUpdated(row(7, 6, 0, 11, 1)); // Noncredit asset.
        commands.saveUpdated(row(7, 7, 0, 0, 1));
        assertEquals(Arrays.asList(10L, -1L), refreshed);
    }

    @Test public void assignedSaveRewritesTheModelsUserAndDeleteZeroRowsStillRefreshesButDeleteErrorsDoNot() {
        asset(7, 10, 2); OriginalInstalmentRecord r = row(8, 9, 0, 10, 2); repository.saveOriginalRaw(r);
        assertTrue(commands.saveAssigned(r).saved); assertEquals(7, r.userId); assertEquals(999, r.updateTime);
        assertNull(repository.byIdentity(8, 9)); assertNotNull(repository.byIdentity(7, 9));
        assertEquals(1, commands.delete(r)); assertEquals(0, r.localRowId); assertEquals(9, r.instalmentId);
        assertEquals(0, commands.delete(r)); assertEquals(Arrays.asList(10L, 10L, 10L), refreshed);
        repository.saveOriginalRaw(r);
        store.getWritableDatabase().execSQL("CREATE TRIGGER reject_instalment_delete BEFORE DELETE ON original_instalments BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        long before = r.localRowId;
        try { commands.delete(r); fail(); } catch (android.database.sqlite.SQLiteConstraintException expected) {}
        assertEquals(before, r.localRowId); assertNotNull(repository.byLocalRowId(before)); assertEquals(3, refreshed.size());
    }

    private static final class SequenceRandom extends Random {
        private final int[] values; private int next;
        SequenceRandom(int... values) { this.values = values; }
        @Override public int nextInt(int bound) { assertEquals(Integer.MAX_VALUE, bound); return values[next++]; }
    }
}
