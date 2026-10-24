package com.wangc.bill.reference;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real v4 creation/upgrade and typed v5 restoration; no financial replay. */
@RunWith(AndroidJUnit4.class)
public final class ReferenceSchemaV5DeviceTest {
    private Context context;
    private ReferenceStore store;
    private String database;
    @Before public void prepare(){context=ApplicationProvider.getApplicationContext();database="reference-v5-"+UUID.randomUUID()+".sqlite";}
    @After public void close(){if(store!=null)store.close();context.deleteDatabase(database);}
    private void open(){store=new ReferenceStore(context,database);store.getWritableDatabase();}

    @Test public void realVersionFourUpgradePreservesRowsAndIntroducesEmptyIndependentStores(){
        try(VersionFour old=new VersionFour(context,database)){
            SQLiteDatabase db=old.getWritableDatabase();assertEquals(4,db.getVersion());
            db.execSQL("INSERT INTO accounts(id,name,opening) VALUES(42,'原v4实验账户',1234)");
            long category;
            try(Cursor c=db.rawQuery("SELECT id FROM categories WHERE level='parent' AND semantic_id=99",null)){assertTrue(c.moveToFirst());category=c.getLong(0);}
            JSONObject data=Json.object("book_id",1,"category_id",category,"kind","expense","amount",500,"time",1735689600000L);
            db.execSQL("INSERT INTO candidates(id,payload,package_name,book_id,created,state,transaction_id) VALUES('v4-saved',?,'test.v4',1,1,'SAVED',100)",new Object[]{data.toString()});
            db.execSQL("INSERT INTO transactions(id,book_id,kind,amount,currency,category_id,parent_semantic_id,child_semantic_id,account_id,time,created,candidate_id) VALUES(100,1,'expense',500,'CNY',?,99,-1,42,1735689600000,1,'v4-saved')",new Object[]{category});
            db.execSQL("INSERT INTO transactions(id,book_id,kind,amount,currency,time,created) VALUES(900,1,'income',1,'CNY',1,1)");
            db.execSQL("DELETE FROM transactions WHERE id=900");
        }
        open();assertEquals(7,store.getReadableDatabase().getVersion());
        assertEquals(734,store.accountBalance(42));
        assertEquals("legacy",store.transaction(100).optString("bill_origin"));assertEquals(42,store.transaction(100).optLong("account_id"));
        JSONObject candidate=store.candidate("v4-saved");assertEquals("bill",candidate.optString("save_kind"));
        for(String field:new String[]{"refund_owner_semantic_id","refund_semantic_id","refund_item_raw"})assertTrue(candidate.isNull(field));
        assertNewStoresEmpty();store.restore(store.backup());assertEquals("v4-saved",store.transaction(100).optString("candidate_id"));
        store.getWritableDatabase().execSQL("INSERT INTO transactions(book_id,kind,amount,currency,time,created) VALUES(1,'income',1,'CNY',1,1)");
        assertTrue(store.scalar("SELECT MAX(id) n FROM transactions")>900);
    }

    @Test public void versionFiveRoundTripsRawNullTextLongIdsAndIndependentOrphans(){
        open();OriginalBillRelatedRecord relation=new OriginalBillRelatedRecord();relation.relatedId=9000000001L;
        relation.oneBillId=-5;relation.twoBillId=0;relation.userId=7;relation.originalDeleted=true;
        new OriginalBillRelatedRepository(store).saveOriginalRaw(relation);
        OriginalInstalmentRecord instalment=new OriginalInstalmentRecord();instalment.instalmentId=9000000002L;
        instalment.assetId=Long.MAX_VALUE;instalment.billId=-5;instalment.userId=7;instalment.periods=3;
        instalment.totalNumber=42.125;instalment.selfInfo="{\"unrecognized\": [1, null]}";
        new OriginalInstalmentRepository(store).saveOriginalRaw(instalment);
        OriginalCurdHistoryRepository.Data operation=new OriginalCurdHistoryRepository.Data();operation.type=6;operation.typeId=-99;
        operation.billBookSemanticId=Long.MAX_VALUE;operation.billOwnerSemanticId=-7;
        assertTrue(new OriginalCurdHistoryRepository(store).add(7,operation).saved);
        JSONObject backup=store.backup();assertEquals(7,backup.optInt("version"));
        assertEquals(24,backup.optJSONObject("tables").length());store.restore(backup);
        OriginalBillRelatedRecord restored=new OriginalBillRelatedRepository(store).byLocalRowId(relation.localRowId);
        assertEquals(9000000001L,restored.relatedId);assertEquals(-5,restored.oneBillId);assertTrue(restored.originalDeleted);
        OriginalInstalmentRecord value=new OriginalInstalmentRepository(store).byLocalRowId(instalment.localRowId);
        assertEquals(Long.MAX_VALUE,value.assetId);assertNull(value.accountMonth);assertEquals(instalment.selfInfo,value.selfInfo);assertEquals(42.125,value.totalNumber,0);
        assertEquals(Long.MAX_VALUE,new OriginalCurdHistoryRepository(store).forOwner(7).get(0).optLong("bill_book_semantic_id"));
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM asset_histories"));
    }

    @Test public void corruptNewFieldsRollBackEveryTable(){
        open();OriginalInstalmentRecord record=new OriginalInstalmentRecord();record.instalmentId=101;record.userId=7;record.totalNumber=12.5;
        new OriginalInstalmentRepository(store).saveOriginalRaw(record);JSONObject valid=store.backup();
        Object[][] defects={{"parent_bill_semantic_id",2147483648L},{"periods",1.5},{"total_number","12.5"},{"self_info",new JSONArray()},{"original_delete_flag",2}};
        for(Object[] defect:defects){JSONObject invalid=Json.copy(valid);Json.put(invalid.optJSONObject("tables").optJSONArray("original_instalments").optJSONObject(0),(String)defect[0],defect[1]);expectInvalid(invalid);assertEquals(12.5,new OriginalInstalmentRepository(store).byLocalRowId(record.localRowId).totalNumber,0);}
        JSONObject missing=Json.copy(valid);missing.optJSONObject("tables").remove("original_bill_related");expectInvalid(missing);
        assertEquals(1,store.books().size());assertEquals(1,store.scalar("SELECT COUNT(*) n FROM original_instalments"));
    }

    @Test public void oldVersionFourBackupCannotForgeRefundReceiptsOrNewTables(){
        open();long book=store.books().get(0).optLong("id");
        JSONObject input=Json.object("book_id",book,"kind","expense","amount",100,"currency","CNY","category_id",store.defaultCategory("expense",book),"time",1735689600000L);
        String candidate=store.addCandidate(input,"test.old",book);JSONObject old=store.backup();Json.put(old,"version",4);
        JSONObject tables=old.optJSONObject("tables");
        tables.remove("original_bill_related");tables.remove("original_instalments");tables.remove("original_curd_histories");
        JSONObject row=tables.optJSONArray("candidates").optJSONObject(0);
        Json.put(row,"save_kind","refund");Json.put(row,"refund_owner_semantic_id",7);Json.put(row,"refund_semantic_id",123);Json.put(row,"refund_item_raw","forged");
        Json.put(tables,"original_instalments",new JSONArray().put(Json.object("id",1,"total_number",999)));
        store.restore(old);JSONObject restored=store.candidate(candidate);assertEquals("PENDING",restored.optString("state"));assertEquals("bill",restored.optString("save_kind"));
        assertTrue(restored.isNull("refund_semantic_id"));assertNewStoresEmpty();
    }

    @Test public void missingVersionFiveReceiptColumnsRejectButMissingVersionFourColumnsDefault(){
        open();long book=store.books().get(0).optLong("id");String id=store.addCandidate(Json.object("book_id",book,"time",1),"test.v5",book);
        JSONObject missing=store.backup();missing.optJSONObject("tables").optJSONArray("candidates").optJSONObject(0).remove("save_kind");
        expectInvalid(missing);assertNotNull(store.candidate(id));
        Json.put(missing,"version",4);JSONObject tables=missing.optJSONObject("tables");
        tables.remove("original_bill_related");tables.remove("original_instalments");tables.remove("original_curd_histories");
        store.restore(missing);assertEquals("bill",store.candidate(id).optString("save_kind"));
    }

    private void assertNewStoresEmpty(){for(String table:new String[]{"original_bill_related","original_instalments","original_curd_histories"})assertEquals(table,0,store.scalar("SELECT COUNT(*) n FROM "+table));}
    private void expectInvalid(JSONObject backup){try{store.restore(backup);fail("Invalid v5 backup was accepted");}catch(IllegalArgumentException|android.database.SQLException expected){}}
    private static final class VersionFour extends SQLiteOpenHelper {
        VersionFour(Context context,String name){super(context,name,null,4);}
        @Override public void onConfigure(SQLiteDatabase db){db.setForeignKeyConstraintsEnabled(true);}
        @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){throw new AssertionError("Create a real v4 fixture only");}
        @Override public void onCreate(SQLiteDatabase db){ReferenceSchemaV4DeviceTest.VersionThree.createSchema(db);ReferenceSchemaV4.install(db);}
    }
}
