package com.wangc.bill.reference;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real entity saves for i1.d/w0; requires the integrated schema8, not a temporary added table. */
@RunWith(AndroidJUnit4.class)
public final class OriginalLendActionsDeviceTest {
    private static final String DATABASE = "reference_original_lend_actions_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private OriginalLendRepository lends;

    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase(); lends = new OriginalLendRepository(store);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DATABASE); }

    private OriginalLendRecord raw(int owner, int semantic) {
        OriginalLendRecord r = new OriginalLendRecord(); r.userId = owner; r.lendId = semantic;
        r.updateTime = 111; r.createTime = 222; r.assetId = Long.MIN_VALUE; r.repaymentAssetId = Long.MAX_VALUE;
        r.number = -10.125; r.fromCost = 0; r.interest = -.25; r.outTime = -456; r.inTime = 0;
        r.billId = -2; r.type = 4; r.remark = "合成借贷保存阶段"; r.generalServiceBill = true; r.originalDeleted = true; return r;
    }
    private OriginalLendActions actions(int owner, LongSupplier clock, int... ids) {
        return new OriginalLendActions(store, owner, new OriginalLendRepository(store, new SequenceRandom(ids)), clock);
    }
    private long rows() { return store.scalar("SELECT COUNT(*) n FROM " + OriginalLendRepository.TABLE); }

    @Test public void addForcesOwnerThenSamplesUpdateAllocatesEvenANonzeroIdAndSeparatelySamplesCreateTime() {
        OriginalLendRecord r = raw(99, 51); AtomicInteger samples = new AtomicInteger(); AtomicInteger allocations = new AtomicInteger();
        LongSupplier clock = () -> {
            assertTrue(Thread.holdsLock(store)); assertFalse(store.getWritableDatabase().inTransaction());
            assertEquals(7, r.userId);
            if (samples.getAndIncrement() == 0) {
                assertEquals(111, r.updateTime); assertEquals(51, r.lendId); assertEquals(222, r.createTime); return 1001;
            }
            assertEquals(1001, r.updateTime); assertEquals(0, r.lendId); assertEquals(222, r.createTime); return 2002;
        };
        Random random = new Random() {
            @Override public int nextInt(int bound) {
                assertEquals(Integer.MAX_VALUE, bound); assertEquals(1, samples.get()); assertEquals(7, r.userId);
                assertEquals(1001, r.updateTime); assertEquals(51, r.lendId); assertEquals(222, r.createTime);
                allocations.incrementAndGet(); return 0;
            }
        };
        OriginalLendActions.Result result = new OriginalLendActions(store, 7, new OriginalLendRepository(store, random), clock).add(r);
        assertTrue(result.save.saved); assertSame(r, result.record); assertSame(r, result.save.record);
        assertEquals(0, result.semanticId); assertEquals(2, samples.get()); assertEquals(1, allocations.get());
        assertTrue(r.localRowId > 0); OriginalLendRecord saved = lends.byLocalRowId(r.localRowId);
        assertEquals(7, saved.userId); assertEquals(0, saved.lendId); assertEquals(1001, saved.updateTime); assertEquals(2002, saved.createTime);
        assertEquals(Long.MIN_VALUE, saved.assetId); assertEquals(Long.MAX_VALUE, saved.repaymentAssetId);
        assertEquals(-10.125, saved.number, 0); assertEquals(0, saved.fromCost, 0); assertEquals(-.25, saved.interest, 0);
        assertEquals(-456, saved.getTime()); assertEquals(0, saved.inTime); assertEquals(-2, saved.billId);
        assertEquals(4, saved.type); assertTrue(saved.generalServiceBill); assertTrue(saved.originalDeleted); assertEquals(r.remark, saved.remark);
    }

    @Test public void addingAnExistingHandleReallocatesItsIdentityAndRetriesItsOwnOwnerCollisionOnly() {
        OriginalLendRecord r = raw(7, 0); lends.saveOriginalRaw(r); long retained = r.localRowId;
        lends.saveOriginalRaw(raw(8, 22)); SequenceClock clock = new SequenceClock(3001, 3002);
        OriginalLendActions.Result result = actions(7, clock, 0, 22).add(r);
        assertTrue(result.save.saved); assertEquals(22, result.semanticId); assertEquals(retained, r.localRowId);
        assertEquals(2, clock.samples); assertEquals(3001, r.updateTime); assertEquals(3002, r.createTime);
        assertNull(lends.byIdentity(7, 0)); assertEquals(retained, lends.byIdentity(7, 22).localRowId);
        assertNotNull(lends.byIdentity(8, 22)); assertEquals(2, rows());
    }

    @Test public void updateUsesOneSampleAndPreservesTheRawOwnerNegativeIdentityAndCreateTime() {
        OriginalLendRecord r = raw(Integer.MIN_VALUE, Integer.MIN_VALUE); lends.saveOriginalRaw(r);
        SequenceClock clock = new SequenceClock(4001); OriginalLendActions.Result result = actions(7, clock).update(r);
        assertTrue(result.save.saved); assertEquals((long) Integer.MIN_VALUE, result.semanticId); assertEquals(1, clock.samples);
        OriginalLendRecord saved = lends.byLocalRowId(r.localRowId);
        assertEquals(Integer.MIN_VALUE, saved.userId); assertEquals(Integer.MIN_VALUE, saved.lendId);
        assertEquals(4001, saved.updateTime); assertEquals(222, saved.createTime); assertEquals(-10.125, saved.number, 0);
        assertNull(lends.byIdentity(7, Integer.MIN_VALUE)); assertEquals(1, rows());
    }

    @Test public void ignoredAddReturnsFalseButRetainsItsNewIdOwnerAndTwoIndependentTimestamps() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_lend_ignore BEFORE INSERT ON " + OriginalLendRepository.TABLE + " BEGIN SELECT RAISE(IGNORE); END");
        OriginalLendRecord r = raw(99, 51); SequenceClock clock = new SequenceClock(5001, 5002);
        OriginalLendActions.Result result = actions(7, clock, 42).add(r);
        assertFalse(result.save.saved); assertFalse(result.save.failureType.isEmpty());
        assertSame(r, result.record); assertSame(r, result.save.record); assertEquals(42, result.semanticId);
        assertEquals(7, r.userId); assertEquals(42, r.lendId); assertEquals(5001, r.updateTime); assertEquals(5002, r.createTime);
        assertEquals(2, clock.samples); assertEquals(0, r.localRowId); assertEquals(0, rows());
        assertFalse(store.getWritableDatabase().inTransaction());
    }

    @Test public void failedUpdateKeepsItsNewModelTimestampWithoutReassigningOwnerIdOrCreateTime() {
        OriginalLendRecord r = raw(-5, -6); lends.saveOriginalRaw(r); r.number = 99;
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_lend_abort BEFORE UPDATE ON " + OriginalLendRepository.TABLE + " BEGIN SELECT RAISE(ABORT,'synthetic update'); END");
        SequenceClock clock = new SequenceClock(6001); OriginalLendActions.Result result = actions(7, clock).update(r);
        assertFalse(result.save.saved); assertFalse(result.save.failureType.isEmpty());
        assertEquals(-6, result.semanticId); assertEquals(-5, r.userId); assertEquals(222, r.createTime);
        assertEquals(6001, r.updateTime); assertEquals(99, r.number, 0); assertEquals(1, clock.samples);
        OriginalLendRecord unchanged = lends.byLocalRowId(r.localRowId);
        assertEquals(111, unchanged.updateTime); assertEquals(222, unchanged.createTime); assertEquals(-10.125, unchanged.number, 0);
        assertEquals(-5, unchanged.userId); assertEquals(-6, unchanged.lendId); assertFalse(store.getWritableDatabase().inTransaction());
    }

    @Test public void clockOrAllocationFailuresPropagateAndKeepOnlyTheAssignmentsBeforeThatStage() {
        OriginalLendRecord first = raw(99, 51); IllegalStateException firstFailure = new IllegalStateException("synthetic first clock");
        try { actions(7, () -> { throw firstFailure; }, 42).add(first); fail("First clock exception was swallowed"); }
        catch (IllegalStateException error) { assertSame(firstFailure, error); }
        assertEquals(7, first.userId); assertEquals(111, first.updateTime); assertEquals(51, first.lendId); assertEquals(222, first.createTime);
        OriginalLendRecord second = raw(99, 51); AtomicInteger samples = new AtomicInteger();
        AssertionError allocationFailure = new AssertionError("synthetic allocation Error");
        Random broken = new Random() { @Override public int nextInt(int bound) { throw allocationFailure; } };
        OriginalLendActions allocation = new OriginalLendActions(store, 7, new OriginalLendRepository(store, broken), () -> { samples.incrementAndGet(); return 7001; });
        try { allocation.add(second); fail("Allocation Error became save=false"); }
        catch (AssertionError error) { assertSame(allocationFailure, error); }
        assertEquals(7, second.userId); assertEquals(7001, second.updateTime); assertEquals(51, second.lendId);
        assertEquals(222, second.createTime); assertEquals(1, samples.get());
        OriginalLendRecord third = raw(99, 51); AtomicInteger thirdSamples = new AtomicInteger();
        IllegalStateException createFailure = new IllegalStateException("synthetic create clock");
        try {
            actions(7, () -> { if (thirdSamples.getAndIncrement() == 0) return 7002; throw createFailure; }, 43).add(third);
            fail("Second clock exception was swallowed");
        } catch (IllegalStateException error) { assertSame(createFailure, error); }
        assertEquals(7, third.userId); assertEquals(7002, third.updateTime); assertEquals(43, third.lendId); assertEquals(222, third.createTime);
        assertEquals(2, thirdSamples.get()); assertEquals(0, first.localRowId); assertEquals(0, second.localRowId);
        assertEquals(0, third.localRowId); assertEquals(0, rows()); assertFalse(store.getWritableDatabase().inTransaction());
    }

    @Test public void aStaleSavedHandleKeepsItsIdentityAndReturnsTrueWhenTheUpdateAffectsZeroRows() {
        OriginalLendRecord r = raw(-5, -6); lends.saveOriginalRaw(r); long retained = r.localRowId;
        store.getWritableDatabase().delete(OriginalLendRepository.TABLE, "id=?", new String[]{"" + retained});
        OriginalLendActions.Result result = actions(7, new SequenceClock(8001)).update(r);
        assertTrue(result.save.saved); assertEquals("", result.save.failureType); assertEquals(retained, r.localRowId);
        assertEquals(-6, result.semanticId); assertEquals(-5, r.userId); assertEquals(222, r.createTime); assertEquals(8001, r.updateTime);
        assertNull(lends.byLocalRowId(retained)); assertEquals(0, rows());
    }

    @Test public void outerTransactionsAreRejectedBeforeAssignmentsAndEntityActionsLeaveFinancialStoresUntouched() {
        AssetRepository.Definition source = new AssetRepository.Definition("合成借贷来源", 1, null); source.assetNumber = 100;
        AssetRepository.Definition destination = new AssetRepository.Definition("合成借贷去向", 1, null); destination.assetNumber = 10;
        store.assetRepository().insertRecovered(7, 101, 10, 1735689600000L, source);
        store.assetRepository().insertRecovered(7, 102, 10, 1735689600000L, destination);
        long histories = store.scalar("SELECT COUNT(*) n FROM asset_histories"), bills = store.scalar("SELECT COUNT(*) n FROM transactions");
        long curd = store.scalar("SELECT COUNT(*) n FROM original_curd_histories");
        OriginalLendRecord r = raw(99, 51); OriginalLendActions blocked = actions(7, () -> { throw new AssertionError("Clock ran inside an outer transaction"); });
        SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
        try {
            for (boolean add : new boolean[]{true, false}) {
                try { if (add) blocked.add(r); else blocked.update(r); fail("Outer transaction accepted"); }
                catch (IllegalStateException expected) { assertNotNull(expected.getMessage()); }
                assertEquals(99, r.userId); assertEquals(51, r.lendId); assertEquals(111, r.updateTime); assertEquals(222, r.createTime);
                assertEquals(0, r.localRowId); assertEquals(0, rows());
            }
        } finally { db.endTransaction(); }
        r.assetId = 102; r.repaymentAssetId = 101;
        OriginalLendActions local = actions(7, new SequenceClock(9001, 9002, 9003), 44);
        assertTrue(local.add(r).save.saved); assertTrue(local.update(r).save.saved); assertEquals(1, rows());
        assertEquals(100, store.assetRepository().p(101, 7).optDouble("asset_number"), 0);
        assertEquals(10, store.assetRepository().p(102, 7).optDouble("asset_number"), 0);
        assertEquals(histories, store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        assertEquals(bills, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(curd, store.scalar("SELECT COUNT(*) n FROM original_curd_histories"));
    }

    private static final class SequenceRandom extends Random {
        private final int[] values; private int position;
        SequenceRandom(int... values) { this.values = values; }
        @Override public int nextInt(int bound) { return values[position++]; }
    }
    private static final class SequenceClock implements LongSupplier {
        private final long[] values; private int samples;
        SequenceClock(long... values) { this.values = values; }
        @Override public long getAsLong() { return values[samples++]; }
    }
}
