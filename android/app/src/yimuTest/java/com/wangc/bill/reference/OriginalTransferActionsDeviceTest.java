package com.wangc.bill.reference;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real SQLite save boundaries and local tails of w2.g/h/q0; no network or financial command. */
@RunWith(AndroidJUnit4.class)
public final class OriginalTransferActionsDeviceTest {
    private static final String DATABASE = "reference_original_transfer_actions_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private OriginalTransferRepository transfers;

    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase();
        transfers = new OriginalTransferRepository(store);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DATABASE); }

    private OriginalTransferRecord raw(int owner, long semantic) {
        OriginalTransferRecord r = new OriginalTransferRecord();
        r.userId = owner; r.transferId = semantic; r.updateTime = 123; r.time = -456;
        r.fromAssetId = Long.MIN_VALUE; r.toAssetId = Long.MAX_VALUE;
        r.cost = -10.125; r.toCost = 7.5; r.serviceCharge = -.25;
        r.billId = -1; r.remark = "合成转账保存阶段"; return r;
    }
    private OriginalTransferActions actions(int owner, Probe probe, long time, int... ids) {
        return new OriginalTransferActions(store, owner, probe,
                new OriginalTransferRepository(store, new SequenceRandom(ids)), () -> time);
    }
    private long rows() { return store.scalar("SELECT COUNT(*) n FROM original_transfers"); }

    private final class Probe implements OriginalTransferActions.Effects {
        int calls;
        OriginalTransferRecord record;
        OriginalTransferRecord persisted;
        boolean saved;
        @Override public void transferChanged(OriginalTransferRecord record, boolean saved) {
            // The tail can observe the committed row or completed failed save. It
            // is serialized with other store stages, not inside the entity save.
            assertTrue(Thread.holdsLock(store));
            assertFalse(store.getWritableDatabase().inTransaction());
            calls++; this.record = record; this.saved = saved;
            persisted = transfers.byLocalRowId(record.localRowId);
        }
    }

    @Test public void assignedSaveForcesOwnerStampsAndRetriesOnlyCurrentOwnerZeroBeforeItsLocalTail() {
        transfers.saveOriginalRaw(raw(7, 0)); transfers.saveOriginalRaw(raw(8, 42));
        Probe probe = new Probe(); OriginalTransferActions actions = actions(7, probe, 555, 0, 42);
        OriginalTransferRecord r = raw(99, 0); OriginalTransferActions.Result result = actions.save(r);
        assertSame(r, result.record); assertSame(r, result.save.record); assertTrue(result.save.saved);
        assertEquals(42, result.semanticId); assertEquals(7, r.userId); assertEquals(555, r.updateTime);
        assertEquals(1, probe.calls); assertSame(r, probe.record); assertTrue(probe.saved);
        assertNotNull(probe.persisted); assertEquals(555, probe.persisted.updateTime);
        assertEquals(r.localRowId, probe.persisted.localRowId);
        OriginalTransferRecord saved = transfers.byLocalRowId(r.localRowId);
        assertEquals(7, saved.userId); assertEquals(42, saved.transferId); assertEquals(555, saved.updateTime);
        assertEquals(-456, saved.time); assertEquals(Long.MIN_VALUE, saved.fromAssetId);
        assertEquals(Long.MAX_VALUE, saved.toAssetId); assertEquals(-1, saved.billId);
        assertEquals(-10.125, saved.cost, 0); assertEquals(7.5, saved.toCost, 0);
        assertEquals(-.25, saved.serviceCharge, 0); assertEquals(r.remark, saved.remark);
        OriginalTransferRecord known = raw(-9, Long.MAX_VALUE); transfers.saveOriginalRaw(known);
        long retainedRow = known.localRowId; OriginalTransferActions.Result retained = actions.save(known);
        assertTrue(retained.save.saved); assertEquals(Long.MAX_VALUE, retained.semanticId);
        assertEquals(retainedRow, known.localRowId); assertEquals(7, known.userId);
        assertNull(transfers.byIdentity(-9, Long.MAX_VALUE)); assertEquals(2, probe.calls);
    }

    @Test public void silentSaveKeepsZeroAsAnAllocatedResultAndNeverEmitsForFailedOrSuccessfulSaves() {
        Probe probe = new Probe(); OriginalTransferActions actions = actions(7, probe, 600, 0, 61);
        SQLiteDatabase db = store.getWritableDatabase();
        db.execSQL("CREATE TRIGGER synthetic_transfer_ignore BEFORE INSERT ON original_transfers BEGIN SELECT RAISE(IGNORE); END");
        OriginalTransferRecord r = raw(99, 0); OriginalTransferActions.Result failed = actions.saveSilently(r);
        assertFalse(failed.save.saved); assertEquals(0, failed.semanticId); assertEquals(0, r.localRowId);
        assertEquals(7, r.userId); assertEquals(600, r.updateTime); assertEquals(0, rows()); assertEquals(0, probe.calls);
        db.execSQL("DROP TRIGGER synthetic_transfer_ignore"); r.userId = -99; r.updateTime = -99;
        OriginalTransferActions.Result saved = actions.saveSilently(r);
        assertTrue(saved.save.saved); assertEquals(61, saved.semanticId); assertEquals(7, r.userId);
        assertEquals(600, r.updateTime); assertTrue(r.localRowId > 0); assertEquals(0, probe.calls);
        assertEquals(61, transfers.byLocalRowId(r.localRowId).transferId);
    }

    @Test public void updateOnlyStampsAndNotifiesWithoutAssigningTheCallerOwnerOrZeroIdentity() {
        Probe probe = new Probe(); OriginalTransferActions actions = actions(7, probe, 800);
        OriginalTransferRecord r = raw(Integer.MIN_VALUE, 0); OriginalTransferActions.Result result = actions.update(r);
        assertTrue(result.save.saved); assertEquals(0, result.semanticId);
        assertEquals(Integer.MIN_VALUE, r.userId); assertEquals(800, r.updateTime);
        assertEquals(1, probe.calls); assertSame(r, probe.record); assertTrue(probe.saved);
        OriginalTransferRecord saved = transfers.byIdentity(Integer.MIN_VALUE, 0);
        assertEquals(r.localRowId, saved.localRowId); assertEquals(800, saved.updateTime);
        assertNull(transfers.byIdentity(7, 0)); assertEquals(1, rows());
    }

    @Test public void assignedHeaderSaveFalseStillEmitsWithItsAllocatedIdentityOwnerAndTimestamp() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_transfer_ignore BEFORE INSERT ON original_transfers BEGIN SELECT RAISE(IGNORE); END");
        Probe probe = new Probe(); OriginalTransferRecord r = raw(-5, 0);
        OriginalTransferActions.Result result = actions(7, probe, 901, 88).save(r);
        assertFalse(result.save.saved); assertFalse(result.save.failureType.isEmpty());
        assertSame(r, result.record); assertSame(r, result.save.record);
        assertEquals(88, result.semanticId); assertEquals(88, r.transferId);
        assertEquals(7, r.userId); assertEquals(901, r.updateTime); assertEquals(0, r.localRowId);
        assertEquals(1, probe.calls); assertSame(r, probe.record); assertFalse(probe.saved);
        assertNull(probe.persisted);
        assertEquals(0, rows());
    }

    @Test public void updateSaveFalseStillEmitsAfterRollbackWithoutNormalizingItsRawIdentity() {
        OriginalTransferRecord r = raw(-5, 0); transfers.saveOriginalRaw(r);
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_transfer_abort BEFORE UPDATE ON original_transfers BEGIN SELECT RAISE(ABORT,'synthetic update'); END");
        Probe probe = new Probe(); r.cost = 99;
        OriginalTransferActions.Result result = actions(7, probe, 902).update(r);
        assertFalse(result.save.saved); assertFalse(result.save.failureType.isEmpty());
        assertEquals(0, result.semanticId); assertEquals(-5, r.userId); assertEquals(902, r.updateTime);
        assertEquals(99, r.cost, 0); assertEquals(1, probe.calls); assertFalse(probe.saved);
        assertNotNull(probe.persisted); assertEquals(123, probe.persisted.updateTime);
        assertEquals(-10.125, probe.persisted.cost, 0);
        OriginalTransferRecord persisted = transfers.byLocalRowId(r.localRowId);
        assertEquals(123, persisted.updateTime); assertEquals(-10.125, persisted.cost, 0);
        assertEquals(-5, persisted.userId); assertEquals(0, persisted.transferId); assertEquals(1, rows());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test public void lateGenericTypeFailureEmitsFalseWithTheAssignedHandleDespiteNoPersistedRow() {
        Probe probe = new Probe(); OriginalTransferRecord r = raw(-5, 0);
        List invalid = new ArrayList(); invalid.add(1L); invalid.add("not a boxed Long"); r.tags = invalid;
        OriginalTransferActions.Result result = actions(7, probe, 903, 77).save(r);
        assertFalse(result.save.saved); assertEquals(ClassCastException.class.getName(), result.save.failureType);
        assertTrue(r.localRowId > 0); assertSame(r, result.save.record); assertSame(r, probe.record);
        assertEquals(7, r.userId); assertEquals(77, result.semanticId); assertEquals(903, r.updateTime);
        assertEquals(1, probe.calls); assertFalse(probe.saved); assertSame(invalid, r.tags);
        assertNull(probe.persisted);
        assertNull(transfers.byLocalRowId(r.localRowId)); assertEquals(0, rows());
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfer_tags"));
    }

    @Test public void anErrorFromGenericPersistencePropagatesUnchangedAndSkipsTheLocalTail() {
        Probe probe = new Probe(); OriginalTransferRecord r = raw(-5, 0);
        AssertionError original = new AssertionError("synthetic generic Error");
        r.tags = new AbstractList<Long>() {
            @Override public int size() { return 1; }
            @Override public Long get(int index) { throw original; }
        };
        try { actions(7, probe, 904, 43).save(r); fail("Error became save=false"); }
        catch (AssertionError error) { assertSame(original, error); }
        assertEquals(7, r.userId); assertEquals(43, r.transferId); assertEquals(904, r.updateTime);
        assertTrue(r.localRowId > 0); assertNull(transfers.byLocalRowId(r.localRowId));
        assertEquals(0, rows()); assertEquals(0, probe.calls);
        assertFalse(store.getWritableDatabase().inTransaction());
    }

    @Test public void outerTransactionsAreRejectedBeforeMutationAndLocalSavesDoNotTouchFinancialStores() {
        long account = store.saveAccount(0, "合成未发生余额变动账户", "cash", "CNY", 12345);
        long balance = store.accountBalance(account), history = store.scalar("SELECT COUNT(*) n FROM asset_histories");
        long curd = store.scalar("SELECT COUNT(*) n FROM original_curd_histories");
        long bills = store.scalar("SELECT COUNT(*) n FROM transactions");
        Probe probe = new Probe(); OriginalTransferActions actions = new OriginalTransferActions(store, 7, probe,
                new OriginalTransferRepository(store, new SequenceRandom()), () -> { throw new AssertionError("Clock ran in an outer transaction"); });
        OriginalTransferRecord r = raw(-5, 0); SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
        try {
            for (int operation = 0; operation < 3; operation++) {
                try {
                    if (operation == 0) actions.save(r);
                    else if (operation == 1) actions.saveSilently(r);
                    else actions.update(r);
                    fail("Outer transaction accepted");
                } catch (IllegalStateException expected) { assertNotNull(expected.getMessage()); }
                assertEquals(-5, r.userId); assertEquals(123, r.updateTime); assertEquals(0, r.transferId);
                assertEquals(0, r.localRowId); assertEquals(0, probe.calls); assertEquals(0, rows());
            }
        } finally { db.endTransaction(); }
        r.fromAssetId = account; r.toAssetId = account;
        OriginalTransferActions local = actions(7, probe, 905, 14);
        assertTrue(local.save(r).save.saved); assertTrue(local.saveSilently(r).save.saved);
        assertTrue(local.update(r).save.saved); assertEquals(2, probe.calls); assertEquals(1, rows());
        assertEquals(balance, store.accountBalance(account));
        assertEquals(history, store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        assertEquals(curd, store.scalar("SELECT COUNT(*) n FROM original_curd_histories"));
        assertEquals(bills, store.scalar("SELECT COUNT(*) n FROM transactions"));
    }

    private static final class SequenceRandom extends Random {
        private final int[] values;
        private int position;
        SequenceRandom(int... values) { this.values = values; }
        @Override public int nextInt(int bound) { return values[position++]; }
    }
}
