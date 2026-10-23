package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real relation persistence; deliberately no Bill foreign-key cascade or unique pair. */
@RunWith(AndroidJUnit4.class)
public final class OriginalBillRelatedStoreDeviceTest {
    private static final String DATABASE = "reference_original_bill_related_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private OriginalBillRelatedRepository relations;
    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase();
        relations = new OriginalBillRelatedRepository(store);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DATABASE); }
    private OriginalBillRelatedRecord relation(int owner, long semantic, int one, int two) {
        OriginalBillRelatedRecord r = new OriginalBillRelatedRecord();
        r.userId = owner; r.relatedId = semantic; r.oneBillId = one; r.twoBillId = two;
        r.updateTime = 123; relations.saveOriginalRaw(r); return r;
    }
    @Test public void rawLongIdentityOwnerAndPrimitiveZeroRemainIndependentOfLocalPk() {
        OriginalBillRelatedRecord first = relation(7, Long.MAX_VALUE, 0, -1);
        relation(8, Long.MAX_VALUE, 1, 2);
        OriginalBillRelatedRecord read = relations.byIdentity(7, Long.MAX_VALUE);
        assertEquals(first.localRowId, read.localRowId); assertNotEquals(first.relatedId, read.localRowId);
        assertEquals(0, read.oneBillId); assertEquals(-1, read.twoBillId); assertFalse(read.originalDeleted);
        assertEquals(1, relations.forBill(7, 0).size()); assertTrue(relations.forBill(8, 0).isEmpty());
        relations.validateRestored();
    }
    @Test public void reversedAndDuplicatePairsAreNotCoalescedAndDeletionStaysOwnerScoped() {
        relation(7, 1, 10, 20); relation(7, 2, 20, 10); relation(7, 3, 10, 20);
        relation(8, 1, 10, 20);
        assertEquals(3, relations.pairCount(7, 20, 10)); assertEquals(3, relations.forBill(7, 10).size());
        assertEquals(3, relations.deletePair(7, 10, 20));
        assertTrue(relations.allForOwner(7).isEmpty()); assertEquals(1, relations.pairCount(8, 10, 20));
    }
    @Test public void semanticAllocationIncludesZeroAndOnlyRetriesSameOwnerCollision() {
        relation(7, 0, 1, 2); relation(8, 42, 1, 2);
        OriginalBillRelatedRepository deterministic = new OriginalBillRelatedRepository(store, new SequenceRandom(0, 42));
        assertEquals(42, deterministic.allocateSemanticId(7));
        assertEquals(0, new OriginalBillRelatedRepository(store, new SequenceRandom(0)).allocateSemanticId(99));
        OriginalBillRelatedRepository.SaveResult fresh = new OriginalBillRelatedRepository(store, new SequenceRandom(0)).tryCreate(99, 5, 6, 777);
        assertTrue(fresh.saved); assertEquals(0, fresh.record.relatedId); assertEquals(777, fresh.record.updateTime);
    }
    @Test public void saveFalseRetainsAllocatedIdentityButNormalZeroRowUpdateStillSucceeds() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_related_ignore BEFORE INSERT ON original_bill_related BEGIN SELECT RAISE(IGNORE); END");
        OriginalBillRelatedRepository.SaveResult ignored = new OriginalBillRelatedRepository(store, new SequenceRandom(0)).tryCreate(7, 1, 2, 888);
        assertFalse(ignored.saved); assertEquals(0, ignored.record.relatedId); assertEquals(0, ignored.record.localRowId);
        assertEquals(888, ignored.record.updateTime); assertFalse(ignored.failureType.isEmpty());
        store.getWritableDatabase().execSQL("DROP TRIGGER synthetic_related_ignore");
        OriginalBillRelatedRecord stale = relation(7, 9, 1, 2);
        relations.deleteIdentity(7, 9); stale.twoBillId = 3;
        assertTrue(relations.trySaveOriginalRaw(stale).saved);
        assertNull(relations.byIdentity(7, 9));
    }
    @Test public void rawSaveWritesExplicitZeroFlagsAndDoesNotInventOwnerOrTimestamp() {
        OriginalBillRelatedRecord record = relation(7, 9, 1, 2); record.originalDeleted = true;
        relations.saveOriginalRaw(record);
        record.userId = 0; record.relatedId = 0; record.oneBillId = 0; record.twoBillId = 0;
        record.updateTime = 0; record.originalDeleted = false;
        relations.saveOriginalRaw(record);
        assertNull(relations.byIdentity(7, 9)); OriginalBillRelatedRecord read = relations.byIdentity(0, 0);
        assertEquals(record.localRowId, read.localRowId); assertEquals(0, read.updateTime); assertFalse(read.originalDeleted);
    }
    @Test public void deletingCanonicalBillDoesNotCascadeRawOrphanRelation() {
        OriginalBillRecord parent = new OriginalBillRecord(); parent.userId = 7; parent.billId = 99;
        parent.bookId = 1; parent.parentCategoryId = 99; parent.cost = 1; parent.time = 1;
        ContentValues projection = new ContentValues(); projection.put("book_id", 1); projection.put("kind", "expense");
        projection.put("amount", 100); projection.put("currency", "CNY"); projection.put("time", 1); projection.put("created", 1);
        new OriginalBillRepository(store).saveOriginalRaw(parent, projection);
        OriginalBillRelatedRecord raw = relation(7, 10, 99, 100);
        store.getWritableDatabase().delete("transactions", "id=?", new String[]{"" + parent.localRowId});
        assertEquals(raw.localRowId, relations.byIdentity(7, 10).localRowId);
        assertEquals(99, relations.byIdentity(7, 10).oneBillId); relations.validateRestored();
    }
    @Test public void restoreValidatorRejectsNonIntegralRawFieldsRatherThanTruncating() {
        OriginalBillRelatedRecord raw = relation(7, 10, 1, 2);
        store.getWritableDatabase().execSQL("UPDATE original_bill_related SET one_bill_semantic_id=1.5 WHERE id=?", new Object[]{raw.localRowId});
        try { relations.validateRestored(); fail("fractional Bill semantic ID accepted"); }
        catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
    }
    private static final class SequenceRandom extends Random {
        private final int[] values; private int position;
        SequenceRandom(int... values) { this.values = values; }
        @Override public int nextInt(int bound) { return values[position++]; }
    }
}
