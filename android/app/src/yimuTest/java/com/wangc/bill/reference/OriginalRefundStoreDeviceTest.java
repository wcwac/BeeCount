package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real SQLite tests for the new original sidecars, using an isolated synthetic database. */
@RunWith(AndroidJUnit4.class)
public final class OriginalRefundStoreDeviceTest {
    private static final String DATABASE="reference_original_refund_store_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private OriginalRefundRepository refunds;
    private OriginalReimbursementRepository reimbursements;
    @Before public void open() {
        context=ApplicationProvider.getApplicationContext();context.deleteDatabase(DATABASE);
        store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();
        refunds=new OriginalRefundRepository(store);reimbursements=new OriginalReimbursementRepository(store);
    }
    @After public void close() {if(store!=null)store.close();context.deleteDatabase(DATABASE);}
    private static OriginalRefundRepository.Data refund(int parent,double amount,String... entries) {
        OriginalRefundRepository.Data data=new OriginalRefundRepository.Data();data.parentBillSemanticId=parent;data.refundNumber=amount;
        if(entries!=null)data.rawInfos=new ArrayList<>(Arrays.asList(entries));return data;
    }
    private static OriginalReimbursementRepository.Data reimbursement(int parent,double amount,String... entries) {
        OriginalReimbursementRepository.Data data=new OriginalReimbursementRepository.Data();data.parentBillSemanticId=parent;data.reimbursementNumber=amount;
        if(entries!=null)data.rawEntries=new ArrayList<>(Arrays.asList(entries));return data;
    }
    private static final class SequenceRandom extends Random {
        private final int[] values;private int position;
        SequenceRandom(int... values){this.values=values;}
        @Override public int nextInt(int bound){assertEquals(Integer.MAX_VALUE,bound);assertTrue("Unexpected allocation retry",position<values.length);return values[position++];}
    }
    private long parent(int semantic,int user) {
        OriginalBillRecord bill=new OriginalBillRecord();bill.billId=semantic;bill.userId=user;bill.fromUserId=19;bill.bookId=1;
        bill.cost=10;bill.parentCategoryId=8;bill.childCategoryId=811;bill.time=1735689600000L;
        return new OriginalBillRepository(store).saveOriginalRaw(bill,projection(1000));
    }
    private ContentValues projection(long amount) {
        ContentValues value=new ContentValues();value.put("book_id",store.books().get(0).optLong("id"));
        value.put("kind","expense");value.put("amount",amount);value.put("currency","CNY");
        value.put("time",1735689600000L);value.put("created",1735689600000L);return value;
    }

    @Test public void freshSidecarsDoNotConvertLegacyRelatedTransactionsOrInferIdentities() {
        ContentValues value=projection(900);long legacy=store.getWritableDatabase().insertOrThrow("transactions",null,value);
        ContentValues child=projection(200);child.put("related_id",legacy);long receipt=store.getWritableDatabase().insertOrThrow("transactions",null,child);
        assertTrue(refunds.allForOwner(0).isEmpty());assertTrue(reimbursements.allForOwner(0).isEmpty());
        assertEquals(legacy,store.transaction(receipt).optLong("related_id"));assertEquals(900,store.transaction(legacy).optLong("amount"));
        assertEquals("legacy",store.transaction(legacy).optString("bill_origin"));assertTrue(store.transaction(legacy).isNull("bill_semantic_id"));
    }
    @Test public void nullableEmptyAndDuplicateRefundStringsSurviveWithoutCanonicalization() {
        OriginalRefundRepository.Snapshot row=refunds.insertRecovered(7,Long.MAX_VALUE,refund(18,-2,(String[])null),1);
        assertNull(refunds.byRowId(row.rowId,7).data.rawInfos);
        OriginalRefundRepository.Data data=row.data.copy();data.rawInfos=new ArrayList<>();refunds.update(row.rowId,7,data,2);
        assertTrue(refunds.byRowId(row.rowId,7).data.rawInfos.isEmpty());
        String compact="{\"number\":2,\"unknown\":true}",spaced=" { \"number\" : 2.00 } ";
        data.rawInfos.add(compact);data.rawInfos.add(spaced);data.rawInfos.add(compact);data.prependRaw("unsupported raw text");
        refunds.update(row.rowId,7,data,3);assertEquals(data.rawInfos,refunds.byRowId(row.rowId,7).data.rawInfos);
        data.removeFirstRaw(compact);refunds.update(row.rowId,7,data,4);
        assertEquals(Arrays.asList("unsupported raw text",spaced,compact),refunds.byRowId(row.rowId,7).data.rawInfos);
        data.rawInfos=null;assertNull(refunds.update(row.rowId,7,data,5).data.rawInfos);
        assertEquals(Arrays.asList("unsupported raw text",spaced,compact),refunds.byRowId(row.rowId,7).data.rawInfos);
        data.rawInfos=new ArrayList<>();refunds.update(row.rowId,7,data,6);assertTrue(refunds.byRowId(row.rowId,7).data.rawInfos.isEmpty());
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM original_refund_items"));refunds.validateRestored();
    }
    @Test public void sourceIdentityOwnerScopeAndDuplicateParentsRemainIndependent() {
        OriginalRefundRepository.Snapshot first=refunds.insertRecovered(-7,0,refund(42,3,"same"),1);
        OriginalRefundRepository.Snapshot second=refunds.insertRecovered(-7,Long.MIN_VALUE,refund(42,4,"same"),2);
        OriginalRefundRepository.Snapshot other=refunds.insertRecovered(8,0,refund(42,5,"other"),3);
        assertNotEquals(first.rowId,second.rowId);assertNotEquals(first.rowId,other.rowId);
        assertEquals(2,refunds.allByParent(-7,42).size());assertEquals(first.rowId,refunds.firstByParent(-7,42).rowId);
        assertEquals(other.rowId,refunds.byIdentity(8,0).rowId);assertNull(refunds.byRowId(first.rowId,8));assertFalse(refunds.delete(first.rowId,8));
        assertEquals(Long.MIN_VALUE,refunds.byIdentity(-7,Long.MIN_VALUE).refundSemanticId);refunds.validateRestored();
    }
    @Test public void freshRandomIdsIncludeZeroAndRetryOnlyCollisionsWithinTheExplicitOwner() {
        refunds.insertRecovered(7,0,refund(1,0),1);refunds.insertRecovered(99,9,refund(1,0),1);
        OriginalRefundRepository allocator=new OriginalRefundRepository(store,new SequenceRandom(0,9));
        OriginalRefundRepository.Snapshot row=allocator.create(7,refund(1,0),7);assertEquals(9,row.refundSemanticId);
        OriginalReimbursementRepository reim=new OriginalReimbursementRepository(store,new SequenceRandom(0));
        OriginalReimbursementRepository.Data data=reimbursement(1,0);data.end=true;data.createTime=-4;
        OriginalReimbursementRepository.Snapshot created=reim.create(7,data,8);
        assertEquals(0,created.reimbursementSemanticId);assertFalse(created.data.end);assertEquals(8,created.data.createTime);
    }
    @Test public void aResolvedParentMayHaveADifferentOwnerAndDeletionOnlyClearsTheLocalBridge() {
        long bill=parent(42,91);OriginalRefundRepository.Data data=refund(42,3,"{\"assetId\":0}");data.parentTransactionRowId=bill;
        OriginalRefundRepository.Snapshot row=refunds.insertRecovered(7,5,data,10);
        OriginalReimbursementRepository.Data reim=reimbursement(42,2,"0:2:1");reim.parentTransactionRowId=bill;
        OriginalReimbursementRepository.Snapshot receipt=reimbursements.insertRecovered(7,5,reim,10);
        refunds.validateRestored();reimbursements.validateRestored();
        store.getWritableDatabase().delete("transactions","id=?",new String[]{""+bill});
        assertNull(refunds.byRowId(row.rowId,7).data.parentTransactionRowId);assertEquals(42,refunds.byRowId(row.rowId,7).data.parentBillSemanticId);
        assertEquals(data.rawInfos,refunds.byRowId(row.rowId,7).data.rawInfos);
        assertNull(reimbursements.byRowId(receipt.rowId,7).data.parentTransactionRowId);assertEquals(reim.rawEntries,reimbursements.byRowId(receipt.rowId,7).data.rawEntries);
        assertTrue(refunds.delete(row.rowId,7));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM original_refund_items"));
        assertNotNull(reimbursements.byRowId(receipt.rowId,7));refunds.validateRestored();reimbursements.validateRestored();
    }
    @Test public void resolvedParentCannotUseALegacyRowOrAnUnrelatedSemanticBill() {
        long legacy=store.getWritableDatabase().insertOrThrow("transactions",null,projection(300));
        OriginalRefundRepository.Data data=refund(42,3);data.parentTransactionRowId=legacy;
        try{refunds.create(7,data,1);fail("Claimed a legacy bill as an original parent");}catch(IllegalArgumentException expected){}
        data.parentTransactionRowId=parent(43,7);
        try{refunds.create(7,data,1);fail("Accepted an unrelated original semantic Bill ID");}catch(IllegalArgumentException expected){}
        data.parentTransactionRowId=null;assertNotNull(refunds.create(7,data,1));assertEquals(1,refunds.allForOwner(7).size());
    }
    @Test public void replacingAnOrderedListRollsBackHeaderAndEarlierItemsWhenAnItemSaveFails() {
        OriginalRefundRepository.Snapshot row=refunds.insertRecovered(7,8,refund(42,3,"old","old"),1);
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER reject_refund_item BEFORE INSERT ON original_refund_items WHEN NEW.raw_json='REJECT' BEGIN SELECT RAISE(ABORT,'synthetic rejection'); END");
        OriginalRefundRepository.Data change=row.data.copy();change.refundNumber=9;change.rawInfos=Arrays.asList("new","REJECT");
        OriginalRefundRepository.SaveResult failed=refunds.tryUpdate(row,change,2);
        assertFalse(failed.saved);assertTrue(failed.failureType.endsWith("SQLiteConstraintException"));assertEquals(9,failed.record.data.refundNumber,0);
        OriginalRefundRepository.Snapshot stored=refunds.byRowId(row.rowId,7);assertEquals(3,stored.data.refundNumber,0);assertEquals(1,stored.updateTime);
        assertEquals(Arrays.asList("old","old"),stored.data.rawInfos);
        OriginalRefundRepository.SaveResult late=refunds.tryCreate(7,change,7);
        assertFalse(late.saved);assertTrue("A late generic failure discarded the assigned model PK",late.record.rowId>0);
        assertNull(refunds.byRowId(late.record.rowId,7));assertEquals(Arrays.asList("new","REJECT"),late.record.data.rawInfos);
        store.getWritableDatabase().execSQL("DROP TRIGGER reject_refund_item");assertTrue(refunds.tryUpdate(row,change,3).saved);
        assertEquals(Arrays.asList("new","REJECT"),refunds.byRowId(row.rowId,7).data.rawInfos);
    }
    @Test public void failedEntityCreateRetainsItsRandomIdentityAndRawInputWithoutStoringPartialRows() {
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER reject_refund BEFORE INSERT ON original_refunds BEGIN SELECT RAISE(ABORT,'synthetic rejection'); END");
        OriginalRefundRepository repo=new OriginalRefundRepository(store,new SequenceRandom(0));
        OriginalRefundRepository.SaveResult failed=repo.tryCreate(7,refund(42,-2,"raw","raw"),55);
        assertFalse(failed.saved);assertEquals(0,failed.record.rowId);assertEquals(0,failed.record.refundSemanticId);
        assertEquals(7,failed.record.ownerSemanticId);assertEquals(55,failed.record.updateTime);assertEquals(Arrays.asList("raw","raw"),failed.record.data.rawInfos);
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM original_refunds"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM original_refund_items"));
        store.getWritableDatabase().execSQL("DROP TRIGGER reject_refund");
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER ignore_refund BEFORE INSERT ON original_refunds BEGIN SELECT RAISE(IGNORE); END");
        assertFalse(new OriginalRefundRepository(store,new SequenceRandom(1)).tryCreate(7,refund(42,0),56).saved);
    }
    @Test public void aSavedButDeletedHeaderCanReturnTrueForZeroRowUpdateButCannotOverwriteReusedIdentity() {
        OriginalRefundRepository.Snapshot old=refunds.insertRecovered(7,0,refund(42,1,(String[])null),1);assertTrue(refunds.delete(old.rowId,7));
        assertTrue(refunds.tryUpdate(old,old.data,2).saved);assertNull(refunds.byRowId(old.rowId,7));
        ContentValues header=new ContentValues();header.put("id",old.rowId);header.put("owner_semantic_id",99);header.put("refund_semantic_id",0);header.put("parent_bill_semantic_id",42);header.put("infos_is_null",0);
        store.getWritableDatabase().insertOrThrow("original_refunds",null,header);
        ContentValues item=new ContentValues();item.put("refund_row_id",old.rowId);item.put("position",0);item.put("raw_json","other owner");store.getWritableDatabase().insertOrThrow("original_refund_items",null,item);
        assertFalse(refunds.tryUpdate(old,old.data,3).saved);assertEquals(Arrays.asList("other owner"),refunds.byRowId(old.rowId,99).data.rawInfos);
    }
    @Test public void originalLiteralAssetQueriesRetainTheirDifferentMatchingRules() {
        refunds.insertRecovered(7,1,refund(1,1,"{\"assetId\":12}"),1);
        refunds.insertRecovered(7,2,refund(2,1,"{\"assetId\": 1}"),1);
        refunds.insertRecovered(8,1,refund(1,1,"{\"assetId\":1}"),1);
        assertEquals(1,refunds.rawAssetLike(7,1).size());assertEquals(1,refunds.rawAssetLike(7,1).get(0).refundSemanticId);
        reimbursements.insertRecovered(7,1,reimbursement(1,1,"12:1:2"),1);reimbursements.insertRecovered(7,2,reimbursement(2,1,"1:1:2"),1);
        assertEquals(1,reimbursements.rawAssetLike(7,1).size());assertEquals(2,reimbursements.rawAssetLike(7,1).get(0).reimbursementSemanticId);
    }
    @Test public void reimbursementsKeepOrderedColonDataAndAssetEndQueriesDoNotMergeOwners() {
        OriginalReimbursementRepository.Data first=reimbursement(42,-2,(String[])null);first.assetSemanticId=-1;first.reimbursementAssetSemanticId=Long.MAX_VALUE;first.end=true;first.createTime=-7;
        OriginalReimbursementRepository.Snapshot a=reimbursements.insertRecovered(7,Long.MAX_VALUE,first,1);assertNull(a.data.rawEntries);
        first.addNumber(0);assertTrue(first.rawEntries.isEmpty());first.appendRaw("-1:2:3::4");first.appendRaw("-1:2:3::4");
        reimbursements.update(a.rowId,7,first,2);assertEquals(first.rawEntries,reimbursements.byRowId(a.rowId,7).data.rawEntries);
        OriginalReimbursementRepository.Data absent=first.copy();absent.rawEntries=null;
        assertNull(reimbursements.update(a.rowId,7,absent,2).data.rawEntries);
        assertEquals(first.rawEntries,reimbursements.byRowId(a.rowId,7).data.rawEntries);
        OriginalReimbursementRepository.Data open=reimbursement(42,1,"opaque");open.assetSemanticId=-1;
        OriginalReimbursementRepository.Snapshot b=reimbursements.insertRecovered(7,0,open,3);reimbursements.insertRecovered(8,0,first,4);
        assertEquals(2,reimbursements.allByParent(7,42).size());assertEquals(b.rowId,reimbursements.forAsset(7,-1,null).get(0).rowId);
        assertEquals(1,reimbursements.forAsset(7,-1,false).size());assertEquals(1,reimbursements.forAsset(7,-1,true).size());
        assertEquals(2,reimbursements.endedAllOwners().size());assertNull(reimbursements.byRowId(a.rowId,8));
        assertEquals(Long.MAX_VALUE,reimbursements.byRowId(a.rowId,7).data.reimbursementAssetSemanticId);reimbursements.validateRestored();
    }
    @Test public void reimbursementStorageFailuresRemainFalseAndDoNotMutateOtherFinancialTables() {
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER ignore_reimbursement BEFORE INSERT ON original_reimbursements BEGIN SELECT RAISE(IGNORE); END");
        OriginalReimbursementRepository repo=new OriginalReimbursementRepository(store,new SequenceRandom(0));
        OriginalReimbursementRepository.SaveResult failed=repo.tryCreate(7,reimbursement(42,3,"raw:entries"),9);
        assertFalse(failed.saved);assertEquals(0,failed.record.rowId);assertEquals(0,failed.record.reimbursementSemanticId);
        assertFalse(failed.record.data.end);assertEquals(9,failed.record.data.createTime);assertEquals(Arrays.asList("raw:entries"),failed.record.data.rawEntries);
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        store.getWritableDatabase().execSQL("DROP TRIGGER ignore_reimbursement");
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER reject_reimbursement_item BEFORE INSERT ON original_reimbursement_items BEGIN SELECT RAISE(ABORT,'synthetic rejection'); END");
        OriginalReimbursementRepository.SaveResult late=new OriginalReimbursementRepository(store,new SequenceRandom(2)).tryCreate(7,reimbursement(42,1,"raw"),10);
        assertFalse(late.saved);assertTrue(late.record.rowId>0);assertNull(reimbursements.byRowId(late.record.rowId,7));
        assertEquals(Arrays.asList("raw"),late.record.data.rawEntries);assertEquals(0,store.scalar("SELECT COUNT(*) n FROM original_reimbursement_items"));
        store.getWritableDatabase().execSQL("DROP TRIGGER reject_reimbursement_item");
        OriginalReimbursementRepository.Snapshot row=reimbursements.insertRecovered(7,1,reimbursement(42,1),10);assertTrue(reimbursements.delete(row.rowId,7));
        assertTrue(reimbursements.tryUpdate(row,row.data,11).saved);
    }
    @Test public void backupRoundTripPreservesRawListsSignedNumbersFlagsAndUnknownParentBridges() {
        OriginalRefundRepository.Data raw=refund(Integer.MIN_VALUE,-2," { \"number\" : 2.00 } ","same","same");raw.originalDeleted=true;
        OriginalRefundRepository.Snapshot refund=refunds.insertRecovered(Integer.MIN_VALUE,Long.MIN_VALUE,raw,-3);
        OriginalReimbursementRepository.Data reim=reimbursement(Integer.MAX_VALUE,-5,(String[])null);reim.end=true;reim.assetSemanticId=Long.MIN_VALUE;
        OriginalReimbursementRepository.Snapshot receipt=reimbursements.insertRecovered(8,Long.MAX_VALUE,reim,0);
        JSONObject backup=store.backup();refunds.delete(refund.rowId,Integer.MIN_VALUE);reimbursements.delete(receipt.rowId,8);store.restore(backup);
        OriginalRefundRepository.Snapshot restored=refunds.byIdentity(Integer.MIN_VALUE,Long.MIN_VALUE);assertEquals(refund.rowId,restored.rowId);
        assertEquals(raw.rawInfos,restored.data.rawInfos);assertTrue(restored.data.originalDeleted);assertNull(restored.data.parentTransactionRowId);assertEquals(-2,restored.data.refundNumber,0);
        assertNull(reimbursements.byIdentity(8,Long.MAX_VALUE).data.rawEntries);assertTrue(reimbursements.byIdentity(8,Long.MAX_VALUE).data.end);
        refunds.validateRestored();reimbursements.validateRestored();
    }
    @Test public void malformedRestoreRollsBackListsRatherThanRepairingPositionsOrNullFlags() {
        OriginalRefundRepository.Snapshot row=refunds.insertRecovered(7,9,refund(42,3,"old"),1);
        for(boolean badFlag:new boolean[]{false,true}) {
            JSONObject backup=store.backup(),tables=backup.optJSONObject("tables");
            if(badFlag)Json.put(tables.optJSONArray("original_refunds").optJSONObject(0),"infos_is_null",1);
            else Json.put(tables.optJSONArray("original_refund_items").optJSONObject(0),"position",2);
            try{store.restore(backup);fail("Repaired malformed original list metadata");}catch(IllegalArgumentException expected){}
            assertEquals(Arrays.asList("old"),refunds.byRowId(row.rowId,7).data.rawInfos);assertEquals(3,refunds.byRowId(row.rowId,7).data.refundNumber,0);
        }
    }
}
