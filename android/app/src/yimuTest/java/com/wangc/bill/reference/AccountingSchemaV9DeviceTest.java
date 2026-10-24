package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.Arrays;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Actual v8 file upgrade and functional v9 receipt/identity restore. Cloud-only, not run locally. */
@RunWith(AndroidJUnit4.class)
public final class AccountingSchemaV9DeviceTest {
    private static final long TIME=1750000000000L;
    private static final String[] OLD_TABLES={"books","accounts","categories","tags","candidates","transactions","transaction_tags","rules","plans","goals","category_hidden_books","category_accounts","asset_show_books","asset_histories","original_refunds","original_refund_items","original_reimbursements","original_reimbursement_items","original_bill_related","original_instalments","original_curd_histories","original_transfers","original_transfer_tags","candidate_transfer_receipts","original_lends","candidate_lend_receipts"};
    private Context context;private String name;private ReferenceStore store;
    private SharedPreferences prefs;private boolean hadBook,hadMode;private long priorBook;private int priorMode;
    @Before public void open(){
        assertEquals("true",InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        context=ApplicationProvider.getApplicationContext();name="accounting-v9-"+UUID.randomUUID()+".sqlite";
        prefs=context.getSharedPreferences("reference_settings",Context.MODE_PRIVATE);
        hadBook=prefs.contains("current_book");priorBook=prefs.getLong("current_book",1);
        hadMode=prefs.contains("original_transfer_mode");priorMode=prefs.getInt("original_transfer_mode",0);
        prefs.edit().putLong("current_book",1).putInt("original_transfer_mode",0).commit();
    }
    @After public void close(){
        try{if(store!=null)store.close();}finally{
            if(context!=null&&name!=null)context.deleteDatabase(name);
            if(prefs!=null){SharedPreferences.Editor edit=prefs.edit();if(hadBook)edit.putLong("current_book",priorBook);else edit.remove("current_book");if(hadMode)edit.putInt("original_transfer_mode",priorMode);else edit.remove("original_transfer_mode");edit.commit();}
        }
    }
    @Test public void realVersionEightFileKeepsItsGraphAndBalanceWithoutReplayingAndStillAcceptsItsOldBackup(){
        JSONObject oldTables;
        try(VersionEight old=new VersionEight(context,name)){
            SQLiteDatabase db=old.getWritableDatabase();assertEquals(8,db.getVersion());
            assertFalse(hasColumn(db,"candidates","capture_key"));
            assertEquals(0,scalar(db,"SELECT COUNT(*) FROM sqlite_master WHERE name='accounting_operations'"));
            long book=scalar(db,"SELECT id FROM books ORDER BY id LIMIT 1"),bookSemantic=scalar(db,"SELECT semantic_id FROM books WHERE id="+book);
            long category=scalar(db,"SELECT id FROM categories WHERE level='parent' AND semantic_id=99 LIMIT 1");
            ContentValues asset=new ContentValues();asset.put("id",71);asset.put("name","旧资金账户");asset.put("type","original");asset.put("currency","");asset.put("asset_origin","original");asset.put("owner_semantic_id",0);asset.put("semantic_id",771);asset.put("asset_type",1);asset.put("asset_number",95d);asset.put("position_weight",1);
            assertEquals(71,db.insertOrThrow("accounts",null,asset));
            JSONObject payload=Json.object("book_id",book,"kind","expense","amount",500,"category_id",category,"account_id",71,"currency","CNY","time",TIME,"remark","旧原账单","owner_semantic_id",0,"raw_asset_semantic_id",771,"raw_to_asset_semantic_id",null,"parent_semantic_id",99,"child_semantic_id",-1);
            ContentValues candidate=new ContentValues();candidate.put("id","v8-bill");candidate.put("book_id",book);candidate.put("payload",payload.toString());candidate.put("package_name","com.eg.android.AlipayGphone");candidate.put("created",TIME);candidate.put("state","SAVED");candidate.put("transaction_id",21);db.insertOrThrow("candidates",null,candidate);
            ContentValues bill=new ContentValues();bill.put("id",21);bill.put("book_id",book);bill.put("kind","expense");bill.put("currency","CNY");bill.put("amount",500);bill.put("category_id",category);bill.put("account_id",71);bill.put("time",TIME);bill.put("created",TIME);bill.put("remark","旧原账单");bill.put("candidate_id","v8-bill");bill.put("bill_origin","original");bill.put("parent_semantic_id",99);bill.put("child_semantic_id",-1);bill.put("owner_semantic_id",0);bill.put("raw_asset_semantic_id",771);
            for(String column:OriginalBillRepository.CANONICAL_COLUMNS){
                if(Arrays.asList("bill_share_info","bill_currency_info","bill_poi_address","bill_remark","bill_tags","bill_total_address").contains(column))bill.putNull(column);
                else if(Arrays.asList("bill_cost","bill_currency_asset_number","bill_currency_discount_number","bill_discount_number").contains(column))bill.put(column,0d);
                else bill.put(column,0L);
            }
            bill.put("bill_semantic_id",0);bill.put("bill_asset_semantic_id",771);bill.put("bill_book_semantic_id",bookSemantic);bill.put("bill_cost",5d);bill.put("bill_parent_category_id",99);bill.put("bill_child_category_id",-1);bill.put("bill_remark","旧原账单");bill.put("bill_time",TIME);bill.put("bill_record_time",TIME);bill.put("bill_type",5);
            assertEquals(21,db.insertOrThrow("transactions",null,bill));
            db.execSQL("INSERT INTO tags(id,name) VALUES(77,'旧标签')");db.execSQL("INSERT INTO transaction_tags(transaction_id,tag_id) VALUES(21,77)");
            db.execSQL("INSERT INTO rules(id,merchant,category_id,book_id,account_id) VALUES(41,'旧商户',?,?,71)",new Object[]{category,book});
            db.execSQL("INSERT INTO asset_histories(id,semantic_id,asset_semantic_id,owner_semantic_id,time,update_time,change_content,change_number,current_number) VALUES(31,331,771,0,?,?,'旧扣款',-5,95)",new Object[]{TIME,TIME});
            oldTables=new JSONObject();for(String table:OLD_TABLES)Json.put(oldTables,table,rows(db,"SELECT * FROM "+table));
        }
        store=new ReferenceStore(context,name);assertEquals(11,store.getWritableDatabase().getVersion());
        assertOldGraph(oldTables);assertEquals(9500,store.accountBalance(71));
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM accounting_operations"));
        JSONObject oldBackup=Json.object("format","YimuReferenceBackup","version",8,"created",TIME,"tables",oldTables);
        store.restore(oldBackup);assertOldGraph(oldTables);assertEquals(9500,store.accountBalance(71));
        assertTrue(store.candidate("v8-bill").isNull("capture_key"));assertTrue(store.candidate("v8-bill").isNull("capture_window_key"));
        // Semantic Bill ID zero is an existing bill: a real edit must reverse then apply once.
        JSONObject edit=store.transaction(21);Json.put(edit,"amount",700);Json.put(edit,"tag_ids",new JSONArray().put(77));
        assertEquals(21,store.saveTransaction(edit,21,null));assertEquals(9300,store.accountBalance(71));
        assertEquals(0,store.transaction(21).optInt("bill_semantic_id"));
    }
    @Test public void versionNineRoundTripsManualAndCandidateReceiptsAndCaptureIdentityWhileRejectingBadAnchorsAtomically(){
        store=new ReferenceStore(context,name);store.getWritableDatabase();long book=store.books().get(0).optLong("id");
        prefs.edit().putLong("current_book",book).commit();OriginalAssetCommands assets=new OriginalAssetCommands(store,0);
        long from=assets.createFund("转出",null,100),to=assets.createFund("转入",null,50);
        JSONObject transfer=Json.object("book_id",book,"kind","transfer","amount",1000,"fee",100,"time",TIME,"remark","真实转账","transfer_amount_text","10.00","transfer_fee_text","1.00","capture_window_key","window:strong");
        store.selectAccountForInput(transfer,"account_id",from,true);store.selectAccountForInput(transfer,"to_account_id",to,true);
        String candidate=store.upsertCapturedCandidate(transfer,"com.eg.android.AlipayGphone",book,"payment:strong","PAYMENT_ID");
        long captured=store.saveCandidateTransfer(transfer,candidate,()->{throw new AssertionError("No principal warning expected");});
        long selectedBook=store.saveBook(0,"手工另选账本","CNY",0);
        JSONObject manual=Json.copy(transfer);Json.put(manual,"book_id",selectedBook);Json.put(manual,"amount",500);Json.put(manual,"fee",100);Json.put(manual,"time",TIME+1000);
        String operation=UUID.randomUUID().toString();JSONObject receipt=store.saveTransfer(manual,operation);
        assertEquals(selectedBook,store.transaction(receipt.optLong("fee_bill_id")).optLong("book_id"));
        assertEquals(book,prefs.getLong("current_book",0));
        JSONObject weak=Json.object("book_id",book,"kind","expense","amount",600,"currency","CNY","time",TIME,"capture_window_key","window:pending");
        String weakId=store.upsertCapturedCandidate(weak,"com.tencent.mm",book,"window:pending","WINDOW_RECEIPT");
        JSONObject backup=store.backup();assertEquals(11,backup.optInt("version"));assertEquals(37,backup.optJSONObject("tables").length());
        store.restore(backup);assertEquals(8300,store.accountBalance(from));assertEquals(6500,store.accountBalance(to));
        assertEquals("PAYMENT_ID",store.candidate(candidate).optString("evidence_kind"));assertEquals("payment:strong",store.candidate(candidate).optString("capture_key"));assertEquals("window:strong",store.candidate(candidate).optString("capture_window_key"));
        assertEquals(captured,store.saveCandidateTransfer(transfer,candidate,()->{}));assertEquals(receipt.toString(),store.saveTransfer(manual,operation).toString());
        assertEquals(weakId,store.upsertCapturedCandidate(weak,"com.tencent.mm",book,"window:pending","WINDOW_RECEIPT"));
        JSONObject stable=store.backup().optJSONObject("tables");
        JSONObject bad=Json.copy(backup);JSONObject badOperation=bad.optJSONObject("tables").optJSONArray("accounting_operations").optJSONObject(0);
        Json.put(badOperation,"semantic_id",badOperation.optLong("semantic_id")+1);expectBadRestore(bad);assertEquals(stable.toString(),store.backup().optJSONObject("tables").toString());
        JSONObject duplicate=Json.copy(backup);JSONArray candidates=duplicate.optJSONObject("tables").optJSONArray("candidates");
        for(int i=0;i<candidates.length();i++)if(weakId.equals(candidates.optJSONObject(i).optString("id"))){JSONObject row=candidates.optJSONObject(i);Json.put(row,"package_name","com.eg.android.AlipayGphone");Json.put(row,"capture_key","payment:strong");Json.put(row,"evidence_kind","PAYMENT_ID");}
        expectBadRestore(duplicate);assertEquals(stable.toString(),store.backup().optJSONObject("tables").toString());
    }
    @Test public void manualBillUuidSurvivesRetryAndBackupRestoreWhileANewUuidCreatesANewRealExpense(){
        store=new ReferenceStore(context,name);store.getWritableDatabase();long book=store.books().get(0).optLong("id");
        prefs.edit().putLong("current_book",book).commit();long account=new OriginalAssetCommands(store,0).createFund("手工幂等账户",null,100);
        JSONObject input=Json.object("book_id",book,"kind","expense","amount",500,"category_id",store.categoryRepository().parent(99).optLong("id"),"currency","CNY","remark","手工重试","time",TIME);
        store.selectAccountForInput(input,"account_id",account,false);String operation=UUID.randomUUID().toString();
        long first=store.saveManualTransaction(input,operation);assertEquals(9500,store.accountBalance(account));
        assertEquals(operation,store.transaction(first).optString("manual_operation_id"));
        assertEquals(first,store.saveManualTransaction(input,operation));assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transactions"));assertEquals(9500,store.accountBalance(account));
        JSONObject backup=store.backup();store.restore(backup);
        Json.put(input,"amount",700);assertEquals(first,store.saveManualTransaction(input,operation));
        assertEquals(500,store.transaction(first).optLong("amount"));assertEquals(9500,store.accountBalance(account));
        long next=store.saveManualTransaction(input,UUID.randomUUID().toString());assertNotEquals(first,next);
        assertEquals(2,store.scalar("SELECT COUNT(*) n FROM transactions"));assertEquals(8800,store.accountBalance(account));
        assertEquals(700,store.transaction(next).optLong("amount"));
    }
    private void expectBadRestore(JSONObject value){try{store.restore(value);fail("Invalid v9 receipt committed");}catch(IllegalArgumentException|SQLException expected){assertNotNull(expected.getMessage());}}
    private void assertOldGraph(JSONObject old){for(String table:OLD_TABLES){JSONArray actual=rows(store.getReadableDatabase(),"SELECT * FROM "+table);if(table.equals("candidates"))for(int i=0;i<actual.length();i++)for(String column:new String[]{"capture_key","evidence_kind","capture_window_key"})actual.optJSONObject(i).remove(column);if(table.equals("transactions"))for(int i=0;i<actual.length();i++)actual.optJSONObject(i).remove("manual_operation_id");assertEquals(table,old.optJSONArray(table).toString(),actual.toString());}}
    private static boolean hasColumn(SQLiteDatabase db,String table,String column){try(Cursor c=db.rawQuery("PRAGMA table_info("+table+")",null)){while(c.moveToNext())if(column.equals(c.getString(c.getColumnIndexOrThrow("name"))))return true;}return false;}
    private static long scalar(SQLiteDatabase db,String sql){try(Cursor c=db.rawQuery(sql,null)){return c.moveToFirst()?c.getLong(0):0;}}
    private static JSONArray rows(SQLiteDatabase db,String sql){JSONArray result=new JSONArray();try(Cursor c=db.rawQuery(sql,null)){while(c.moveToNext()){JSONObject row=new JSONObject();for(int i=0;i<c.getColumnCount();i++)Json.put(row,c.getColumnName(i),switch(c.getType(i)){case Cursor.FIELD_TYPE_NULL->null;case Cursor.FIELD_TYPE_INTEGER->c.getLong(i);case Cursor.FIELD_TYPE_FLOAT->c.getDouble(i);default->c.getString(i);});result.put(row);}}return result;}
    /** Genuine source v8 DDL, not creation of v9 followed by destructive schema surgery. */
    private static final class VersionEight extends SQLiteOpenHelper {
        VersionEight(Context context,String name){super(context,name,null,8);}
        @Override public void onConfigure(SQLiteDatabase db){db.setForeignKeyConstraintsEnabled(false);}
        @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){throw new AssertionError("Fresh v8 fixture only");}
        @Override public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE books(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,currency TEXT NOT NULL DEFAULT 'CNY',budget INTEGER NOT NULL DEFAULT 0,archived INTEGER NOT NULL DEFAULT 0,created INTEGER NOT NULL)");
                db.execSQL("CREATE TABLE accounts(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,type TEXT NOT NULL DEFAULT 'cash',currency TEXT NOT NULL DEFAULT 'CNY',opening INTEGER NOT NULL DEFAULT 0,archived INTEGER NOT NULL DEFAULT 0)");
                db.execSQL("CREATE TABLE categories(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,kind TEXT NOT NULL,icon TEXT NOT NULL,archived INTEGER NOT NULL DEFAULT 0)");
                db.execSQL("CREATE TABLE tags(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE)");
                db.execSQL("CREATE TABLE candidates(id TEXT PRIMARY KEY,payload TEXT NOT NULL,package_name TEXT NOT NULL,book_id INTEGER NOT NULL REFERENCES books(id),created INTEGER NOT NULL,state TEXT NOT NULL DEFAULT 'PENDING',transaction_id INTEGER)");
                db.execSQL("CREATE TABLE transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,book_id INTEGER NOT NULL REFERENCES books(id),kind TEXT NOT NULL CHECK(kind IN ('expense','income','transfer')),amount INTEGER NOT NULL CHECK(amount>0),currency TEXT NOT NULL,category_id INTEGER REFERENCES categories(id),account_id INTEGER REFERENCES accounts(id),to_account_id INTEGER REFERENCES accounts(id),remark TEXT NOT NULL DEFAULT '',merchant TEXT NOT NULL DEFAULT '',origin TEXT NOT NULL DEFAULT '',asset_text TEXT NOT NULL DEFAULT '',discount INTEGER NOT NULL DEFAULT 0 CHECK(discount>=0),fee INTEGER NOT NULL DEFAULT 0 CHECK(fee>=0),time INTEGER NOT NULL,created INTEGER NOT NULL,not_total INTEGER NOT NULL DEFAULT 0,not_budget INTEGER NOT NULL DEFAULT 0,deleted INTEGER NOT NULL DEFAULT 0,financial_type TEXT NOT NULL DEFAULT '',settled INTEGER NOT NULL DEFAULT 0,attachments TEXT NOT NULL DEFAULT '[]',candidate_id TEXT UNIQUE REFERENCES candidates(id))");
                db.execSQL("CREATE INDEX transactions_book_time ON transactions(book_id,deleted,time)");
                db.execSQL("ALTER TABLE transactions ADD COLUMN related_id INTEGER REFERENCES transactions(id)");
                db.execSQL("ALTER TABLE transactions ADD COLUMN import_key TEXT");
                db.execSQL("CREATE UNIQUE INDEX transactions_import_key ON transactions(import_key) WHERE import_key IS NOT NULL");
                db.execSQL("CREATE INDEX candidates_state ON candidates(state,created)");
                db.execSQL("CREATE TABLE transaction_tags(transaction_id INTEGER REFERENCES transactions(id) ON DELETE CASCADE,tag_id INTEGER REFERENCES tags(id),PRIMARY KEY(transaction_id,tag_id))");
                db.execSQL("CREATE TABLE rules(id INTEGER PRIMARY KEY AUTOINCREMENT,merchant TEXT NOT NULL UNIQUE,category_id INTEGER REFERENCES categories(id),book_id INTEGER REFERENCES books(id),account_id INTEGER REFERENCES accounts(id))");
                db.execSQL("CREATE TABLE plans(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,interval TEXT NOT NULL,payload TEXT NOT NULL,next_due INTEGER NOT NULL,enabled INTEGER NOT NULL DEFAULT 1)");
                db.execSQL("CREATE TABLE goals(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,target INTEGER NOT NULL CHECK(target>0),saved INTEGER NOT NULL DEFAULT 0,created INTEGER NOT NULL)");
                CategoryRepository.installV2Schema(db);
                CategoryRepository.bootstrap(db);
                ReferenceSchemaV3.install(db);
                ReferenceSchemaV4.install(db);
                ReferenceSchemaV5.install(db);
                ReferenceSchemaV6.install(db);
                ReferenceSchemaV7.install(db);
                ReferenceSchemaV8.install(db);
                BookRepository.bootstrapNewLocalOwner(db, ReferenceStore.LOCAL_OFFLINE_OWNER, System.currentTimeMillis());
            }
    }
}
