package com.wangc.bill.reference;

import android.content.ContentValues;
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

/** Actual helper-version upgrades; the source fixture never calls current onCreate. */
@RunWith(AndroidJUnit4.class)
public final class ReferenceSchemaV4DeviceTest {
    private static final String[] V3_TABLES={"books","accounts","categories","tags","candidates","transactions","transaction_tags","rules","plans","goals","category_hidden_books","category_accounts","asset_show_books"};
    private Context context;
    private String database;
    private ReferenceStore store;
    @Before public void prepare(){context=ApplicationProvider.getApplicationContext();database="reference-v4-"+UUID.randomUUID()+".sqlite";}
    @After public void close(){if(store!=null)store.close();if(context!=null&&database!=null)context.deleteDatabase(database);}

    @Test public void realVersionThreeHelperUpgradePreservesEveryLinkAndDeletedIdHighWater(){
        JSONObject old=createV3(true);assertEquals(3,old.optInt("version"));openLatest();assertEquals(7,store.getReadableDatabase().getVersion());
        assertLegacyGraph();JSONObject row=store.transaction(123);
        assertEquals("legacy",row.optString("bill_origin"));for(String column:OriginalBillRepository.CANONICAL_COLUMNS)assertTrue(column,row.isNull(column));
        assertEquals(42,row.optLong("account_id"));assertTrue(row.isNull("owner_semantic_id"));assertTrue(row.isNull("raw_asset_semantic_id"));
        long next=store.getWritableDatabase().insertOrThrow("transactions",null,projection(101));assertTrue("Upgrade reused an AUTOINCREMENT ID that existed before migration",next>900);
        try{ContentValues duplicate=projection(222);duplicate.put("import_key",importKey());store.getWritableDatabase().insertOrThrow("transactions",null,duplicate);fail("Import unique index was lost");}catch(android.database.SQLException expected){}
        assertEquals(0,foreignKeyErrors());
    }

    @Test public void anEmptyTransactionTableStillPreservesItsDeletedRowHighWater(){
        createV3(false);openLatest();assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
        long row=store.getWritableDatabase().insertOrThrow("transactions",null,projection(101));assertTrue("Empty-table migration reset the deleted-row high-water mark",row>900);
        assertEquals("legacy",store.transaction(row).optString("bill_origin"));assertEquals(0,foreignKeyErrors());
    }

    @Test public void zeroProjectionAmountsAreAllowedOnlyForExplicitOriginalBills(){
        openLatest();
        try{store.getWritableDatabase().insertOrThrow("transactions",null,projection(0));fail("Legacy zero amount was admitted");}catch(android.database.SQLException expected){}
        ContentValues negative=projection(-1);negative.put("bill_origin","original");
        try{store.getWritableDatabase().insertOrThrow("transactions",null,negative);fail("An original negative projection was admitted");}catch(android.database.SQLException expected){}
        OriginalBillRecord record=original();long row=new OriginalBillRepository(store).saveOriginalRaw(record,projection(0));
        assertEquals(0,store.transaction(row).optLong("amount"));assertEquals("original",store.transaction(row).optString("bill_origin"));assertEquals(0.0d,new OriginalBillRepository(store).byLocalRowId(row).cost,0);
        store.restore(store.backup());assertEquals(0,store.transaction(row).optLong("amount"));
    }

    @Test public void aVersionThreeBackupCannotSmuggleOriginalBillFieldsOrNewTables(){
        JSONObject old=createV3(true);JSONObject rows=old.optJSONObject("tables");JSONObject transaction=rows.optJSONArray("transactions").optJSONObject(0);
        Json.put(transaction,"bill_origin","original");
        for(String column:OriginalBillRepository.CANONICAL_COLUMNS)Json.put(transaction,column,"spoofed original field");
        Json.put(rows,"asset_histories",new JSONArray().put(Json.object("id",1,"semantic_id",99)));
        openLatest();new AssetHistoryRepository(store).appendOperation(7,901,1,1,"替换前历史");store.restore(old);assertLegacyGraph();
        JSONObject saved=store.transaction(123);assertEquals("legacy",saved.optString("bill_origin"));for(String column:OriginalBillRepository.CANONICAL_COLUMNS)assertTrue(column,saved.isNull(column));
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM asset_histories"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM original_refunds"));
        JSONObject zero=Json.copy(old);Json.put(zero.optJSONObject("tables").optJSONArray("transactions").optJSONObject(0),"amount",0);
        expectInvalidRestore(zero);assertLegacyGraph();assertEquals(1234,store.transaction(123).optLong("amount"));
    }

    @Test public void malformedVersionFourFieldsRollBackTheOriginalBillAndIndependentHistory(){
        openLatest();OriginalBillRecord record=original();record.cost=-12.34d;long row=new OriginalBillRepository(store).saveOriginalRaw(record,projection(1234));
        AssetHistoryRepository histories=new AssetHistoryRepository(store);AssetHistoryRepository.SavedHistory history=histories.appendOperation(7,901,-12.34d,9.5d,"保留历史");JSONObject valid=store.backup();
        Object[][] defects={{"bill_origin","unexpected"},{"bill_semantic_id",1.5d},{"bill_user_id",2147483648L},{"bill_not_into_total",2},{"bill_cost",null},{"bill_tags","[1.5]"}};
        for(Object[] defect:defects){JSONObject invalid=Json.copy(valid);Json.put(invalid.optJSONObject("tables").optJSONArray("transactions").optJSONObject(0),(String)defect[0],defect[1]);expectInvalidRestore(invalid);assertEquals(-12.34d,new OriginalBillRepository(store).byLocalRowId(row).cost,0);assertEquals(history.rowId,histories.byIdentity(7,history.semanticId).optLong("id"));}
        JSONObject missing=Json.copy(valid);missing.optJSONObject("tables").remove("asset_histories");expectInvalidRestore(missing);assertNotNull(store.transaction(row));assertEquals(1,histories.count(7));
    }

    @Test public void versionFourBackupRoundTripsAllNewTablesWithoutBalanceReplay(){
        openLatest();AssetRepository.Definition definition=new AssetRepository.Definition("备份余额",1,"CNY");definition.assetNumber=19.25d;
        long account=store.assetRepository().insertRecovered(7,901,1,100,definition);OriginalBillRecord record=original();record.cost=-2.5d;
        ContentValues projection=projection(250);projection.put("account_id",account);projection.put("owner_semantic_id",7);projection.put("raw_asset_semantic_id",901);
        record.assetId=901;long bill=new OriginalBillRepository(store).saveOriginalRaw(record,projection);AssetHistoryRepository histories=new AssetHistoryRepository(store);histories.appendOperation(7,901,-2.5d,19.25d,"合成历史");
        JSONObject backup=store.backup();assertEquals(7,backup.optInt("version"));
        for(String table:new String[]{"asset_histories","original_refunds","original_refund_items","original_reimbursements","original_reimbursement_items"})assertNotNull(table,backup.optJSONObject("tables").optJSONArray(table));
        store.getWritableDatabase().execSQL("UPDATE accounts SET asset_number=999 WHERE id=?",new Object[]{account});store.restore(backup);
        assertEquals(19.25d,store.assetRepository().byRowId(account).optDouble("asset_number"),0);assertEquals(1,histories.count(7));assertEquals(-2.5d,new OriginalBillRepository(store).byLocalRowId(bill).cost,0);assertEquals(0,foreignKeyErrors());
    }

    @Test public void aVersionThreeLegacyRepeatCanBeReplacedByAnOriginalCandidateAndRoundTrip(){
        createV3(false);
        try(SQLiteDatabase source=context.openOrCreateDatabase(database,Context.MODE_PRIVATE,null)){
            assertEquals(3,source.getVersion());
            long category=scalar(source,"SELECT id FROM categories WHERE level='child' AND parent_semantic_id=8 AND semantic_id=811");
            JSONObject payload=Json.object("book_id",1,"kind","expense","amount",1234,"currency","CNY","category_id",category,"time",1735689600000L);
            source.execSQL("INSERT INTO candidates(id,payload,package_name,book_id,created,state,transaction_id) VALUES('v3-repeat-old',?,'test.v3',1,1735689600000,'SAVED',123)",new Object[]{payload.toString()});
            source.execSQL("INSERT INTO transactions(id,book_id,kind,amount,currency,category_id,parent_semantic_id,child_semantic_id,time,created,candidate_id) VALUES(123,1,'expense',1234,'CNY',?,8,811,1735689600000,1735689600000,'v3-repeat-old')",new Object[]{category});
        }
        openLatest();assertEquals("legacy",store.transaction(123).optString("bill_origin"));assertTrue(store.transaction(123).isNull("account_id"));
        JSONObject replacement=Json.object("book_id",1,"kind","expense","amount",1234,"currency","CNY","category_id",store.categoryRepository().child(8,811).optLong("id"),"time",1735689600000L);
        String candidate=store.addCandidate(replacement,"test.v3-legacy-replacement",1);
        assertEquals("PENDING",store.candidate(candidate).optString("state"));assertEquals(1,store.findRepeatTransactions(replacement).size());
        assertEquals(123,store.findRepeatTransactions(replacement).get(0).optLong("id"));
        long saved=store.saveCandidateReplacing(replacement,candidate,123);
        assertTrue(saved>900);assertFalse(store.getWritableDatabase().inTransaction());assertNull(store.transaction(123));
        assertEquals("original",store.transaction(saved).optString("bill_origin"));assertTrue(store.transaction(saved).isNull("account_id"));
        assertEquals(-1,store.transaction(saved).optLong("raw_asset_semantic_id"));
        OriginalBillRecord bill=new OriginalBillRepository(store).byLocalRowId(saved);assertNotNull(bill);assertEquals(-1,bill.assetId);assertEquals(12.34d,bill.cost,0);
        assertEquals("DISMISSED",store.candidate("v3-repeat-old").optString("state"));assertTrue(store.candidate("v3-repeat-old").isNull("transaction_id"));
        assertEquals("SAVED",store.candidate(candidate).optString("state"));assertEquals(saved,store.candidate(candidate).optLong("transaction_id"));
        assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transactions"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM asset_histories"));assertEquals(0,foreignKeyErrors());
        store.restore(store.backup());
        assertNull(store.transaction(123));assertEquals("original",store.transaction(saved).optString("bill_origin"));assertEquals(12.34d,new OriginalBillRepository(store).byLocalRowId(saved).cost,0);
        assertEquals("DISMISSED",store.candidate("v3-repeat-old").optString("state"));assertTrue(store.candidate("v3-repeat-old").isNull("transaction_id"));
        assertEquals("SAVED",store.candidate(candidate).optString("state"));assertEquals(saved,store.candidate(candidate).optLong("transaction_id"));assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transactions"));assertEquals(0,foreignKeyErrors());
    }

    @Test public void aFailedUpgradeRollsBackTheTableRebuildAndKeepsVersionThreeReadable(){
        createV3(true);
        try(SQLiteDatabase source=context.openOrCreateDatabase(database,Context.MODE_PRIVATE,null)){source.execSQL("CREATE TABLE original_refunds(id INTEGER PRIMARY KEY)");}
        try(ReferenceStore attempt=new ReferenceStore(context,database)){try{attempt.getWritableDatabase();fail("The conflicting target schema did not fail");}catch(android.database.SQLException expected){}}
        try(SQLiteDatabase source=context.openOrCreateDatabase(database,Context.MODE_PRIVATE,null)){
            assertEquals(3,source.getVersion());assertEquals(2,scalar(source,"SELECT COUNT(*) FROM transactions"));assertEquals(1,scalar(source,"SELECT COUNT(*) FROM transaction_tags WHERE transaction_id=123 AND tag_id=3"));
            assertEquals(123,scalar(source,"SELECT related_id FROM transactions WHERE id=124"));assertEquals(900,scalar(source,"SELECT seq FROM sqlite_sequence WHERE name='transactions'"));
            boolean originalColumn=false;try(Cursor columns=source.rawQuery("PRAGMA table_info(transactions)",null)){while(columns.moveToNext())if("bill_origin".equals(columns.getString(columns.getColumnIndexOrThrow("name"))))originalColumn=true;}assertFalse(originalColumn);
            source.execSQL("DROP TABLE original_refunds");
        }
        openLatest();assertLegacyGraph();assertEquals(7,store.getReadableDatabase().getVersion());
    }

    private void openLatest(){store=new ReferenceStore(context,database);store.getWritableDatabase();}
    private void expectInvalidRestore(JSONObject backup){try{store.restore(backup);fail("Malformed backup was admitted");}catch(IllegalArgumentException|android.database.SQLException expected){}}
    private int foreignKeyErrors(){try(Cursor errors=store.getReadableDatabase().rawQuery("PRAGMA foreign_key_check",null)){return errors.getCount();}}
    private static long scalar(SQLiteDatabase db,String sql){try(Cursor row=db.rawQuery(sql,null)){assertTrue(row.moveToFirst());return row.getLong(0);}}
    private static String importKey(){return "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";}
    private void assertLegacyGraph(){
        assertNotNull(store.transaction(123));assertEquals(123,store.transaction(124).optLong("related_id"));assertEquals(importKey(),store.transaction(123).optString("import_key"));
        assertEquals("v3-candidate",store.transaction(123).optString("candidate_id"));assertEquals(123,store.candidate("v3-candidate").optLong("transaction_id"));assertEquals("SAVED",store.candidate("v3-candidate").optString("state"));
        assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transaction_tags WHERE transaction_id=123 AND tag_id=3"));assertEquals(1,store.scalar("SELECT COUNT(*) n FROM rules WHERE id=4 AND account_id=42"));assertEquals(0,foreignKeyErrors());
    }
    private static OriginalBillRecord original(){OriginalBillRecord record=new OriginalBillRecord();record.userId=7;record.billId=7001;record.bookId=1;record.assetId=-1;record.parentCategoryId=8;record.childCategoryId=811;record.time=1735689600000L;record.updateTime=1735689600100L;return record;}
    private ContentValues projection(long amount){ContentValues value=new ContentValues();value.put("book_id",1);value.put("kind","expense");value.put("amount",amount);value.put("currency","CNY");value.put("category_id",store.categoryRepository().child(8,811).optLong("id"));value.put("parent_semantic_id",8);value.put("child_semantic_id",811);value.put("time",1735689600000L);value.put("created",1735689600000L);return value;}

    private JSONObject createV3(boolean populated){
        try(VersionThree helper=new VersionThree(context,database)){
            SQLiteDatabase db=helper.getWritableDatabase();assertEquals(3,db.getVersion());
            if(populated){
                db.execSQL("INSERT INTO accounts(id,name) VALUES(42,'升级前旧账户')");db.execSQL("INSERT INTO tags(id,name) VALUES(3,'升级前标签')");
                long category=scalar(db,"SELECT id FROM categories WHERE level='parent' AND semantic_id=99");
                long income=scalar(db,"SELECT id FROM categories WHERE level='child' AND parent_semantic_id=9 AND semantic_id=911");
                JSONObject payload=Json.object("book_id",1,"kind","expense","amount",1234,"currency","CNY","category_id",category,"account_id",42,"time",1735689600000L);
                db.execSQL("INSERT INTO candidates(id,payload,package_name,book_id,created,state,transaction_id) VALUES('v3-candidate',?,'test.v3',1,1735689600000,'SAVED',123)",new Object[]{payload.toString()});
                db.execSQL("INSERT INTO transactions(id,book_id,kind,amount,currency,category_id,account_id,parent_semantic_id,child_semantic_id,time,created,candidate_id,import_key) VALUES(123,1,'expense',1234,'CNY',?,42,99,-1,1735689600000,1735689600000,'v3-candidate',?)",new Object[]{category,importKey()});
                db.execSQL("INSERT INTO transactions(id,book_id,kind,amount,currency,category_id,parent_semantic_id,child_semantic_id,time,created,related_id) VALUES(124,1,'income',200,'CNY',?,9,911,1735689600000,1735689600000,123)",new Object[]{income});
                db.execSQL("INSERT INTO transaction_tags(transaction_id,tag_id) VALUES(123,3)");db.execSQL("INSERT INTO rules(id,merchant,book_id,category_id,account_id) VALUES(4,'升级前商户',1,?,42)",new Object[]{category});
            }
            db.execSQL("INSERT INTO transactions(id,book_id,kind,amount,currency,time,created) VALUES(900,1,'expense',100,'CNY',1,1)");db.execSQL("DELETE FROM transactions WHERE id=900");
            JSONObject tables=new JSONObject();for(String table:V3_TABLES)Json.put(tables,table,rows(db,table));
            return Json.object("format","YimuReferenceBackup","version",3,"created",1735689600000L,"tables",tables);
        }
    }
    private static JSONArray rows(SQLiteDatabase db,String table){JSONArray result=new JSONArray();try(Cursor cursor=db.rawQuery("SELECT * FROM "+table+" ORDER BY rowid",null)){while(cursor.moveToNext()){JSONObject row=new JSONObject();for(int i=0;i<cursor.getColumnCount();i++)Json.put(row,cursor.getColumnName(i),switch(cursor.getType(i)){case Cursor.FIELD_TYPE_NULL->null;case Cursor.FIELD_TYPE_INTEGER->cursor.getLong(i);case Cursor.FIELD_TYPE_FLOAT->cursor.getDouble(i);default->cursor.getString(i);});result.put(row);}}return result;}

    /** DDL copied from the committed v3 ReferenceStore, not current v4 onCreate. */
    static final class VersionThree extends SQLiteOpenHelper {
        VersionThree(Context context,String name){super(context,name,null,3);}
        @Override public void onConfigure(SQLiteDatabase db){db.setForeignKeyConstraintsEnabled(true);}
        @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){throw new AssertionError("This source fixture must only create version3");}
        @Override public void onCreate(SQLiteDatabase db){
            createSchema(db);
        }
        static void createSchema(SQLiteDatabase db){
            db.execSQL("CREATE TABLE books(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,currency TEXT NOT NULL DEFAULT 'CNY',budget INTEGER NOT NULL DEFAULT 0,archived INTEGER NOT NULL DEFAULT 0,created INTEGER NOT NULL)");
            db.execSQL("CREATE TABLE accounts(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,type TEXT NOT NULL DEFAULT 'cash',currency TEXT NOT NULL DEFAULT 'CNY',opening INTEGER NOT NULL DEFAULT 0,archived INTEGER NOT NULL DEFAULT 0)");
            db.execSQL("CREATE TABLE categories(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,kind TEXT NOT NULL,icon TEXT NOT NULL,archived INTEGER NOT NULL DEFAULT 0)");
            db.execSQL("CREATE TABLE tags(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE)");
            db.execSQL("CREATE TABLE candidates(id TEXT PRIMARY KEY,payload TEXT NOT NULL,package_name TEXT NOT NULL,book_id INTEGER NOT NULL REFERENCES books(id),created INTEGER NOT NULL,state TEXT NOT NULL DEFAULT 'PENDING',transaction_id INTEGER)");
            db.execSQL("CREATE TABLE transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,book_id INTEGER NOT NULL REFERENCES books(id),kind TEXT NOT NULL CHECK(kind IN ('expense','income','transfer')),amount INTEGER NOT NULL CHECK(amount>0),currency TEXT NOT NULL,category_id INTEGER REFERENCES categories(id),account_id INTEGER REFERENCES accounts(id),to_account_id INTEGER REFERENCES accounts(id),remark TEXT NOT NULL DEFAULT '',merchant TEXT NOT NULL DEFAULT '',origin TEXT NOT NULL DEFAULT '',asset_text TEXT NOT NULL DEFAULT '',discount INTEGER NOT NULL DEFAULT 0 CHECK(discount>=0),fee INTEGER NOT NULL DEFAULT 0 CHECK(fee>=0),time INTEGER NOT NULL,created INTEGER NOT NULL,not_total INTEGER NOT NULL DEFAULT 0,not_budget INTEGER NOT NULL DEFAULT 0,deleted INTEGER NOT NULL DEFAULT 0,financial_type TEXT NOT NULL DEFAULT '',settled INTEGER NOT NULL DEFAULT 0,attachments TEXT NOT NULL DEFAULT '[]',candidate_id TEXT UNIQUE REFERENCES candidates(id))");
            db.execSQL("CREATE INDEX transactions_book_time ON transactions(book_id,deleted,time)");db.execSQL("ALTER TABLE transactions ADD COLUMN related_id INTEGER REFERENCES transactions(id)");db.execSQL("ALTER TABLE transactions ADD COLUMN import_key TEXT");db.execSQL("CREATE UNIQUE INDEX transactions_import_key ON transactions(import_key) WHERE import_key IS NOT NULL");
            db.execSQL("CREATE INDEX candidates_state ON candidates(state,created)");db.execSQL("CREATE TABLE transaction_tags(transaction_id INTEGER REFERENCES transactions(id) ON DELETE CASCADE,tag_id INTEGER REFERENCES tags(id),PRIMARY KEY(transaction_id,tag_id))");
            db.execSQL("CREATE TABLE rules(id INTEGER PRIMARY KEY AUTOINCREMENT,merchant TEXT NOT NULL UNIQUE,category_id INTEGER REFERENCES categories(id),book_id INTEGER REFERENCES books(id),account_id INTEGER REFERENCES accounts(id))");
            db.execSQL("CREATE TABLE plans(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,interval TEXT NOT NULL,payload TEXT NOT NULL,next_due INTEGER NOT NULL,enabled INTEGER NOT NULL DEFAULT 1)");db.execSQL("CREATE TABLE goals(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,target INTEGER NOT NULL CHECK(target>0),saved INTEGER NOT NULL DEFAULT 0,created INTEGER NOT NULL)");
            CategoryRepository.installV2Schema(db);CategoryRepository.bootstrap(db);ReferenceSchemaV3.install(db);BookRepository.bootstrapNewLocalOwner(db,ReferenceStore.LOCAL_OFFLINE_OWNER,1735689600000L);
        }
    }
}
