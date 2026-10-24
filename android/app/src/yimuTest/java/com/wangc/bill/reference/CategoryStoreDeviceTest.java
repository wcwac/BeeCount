package com.wangc.bill.reference;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class CategoryStoreDeviceTest {
    private static final String DATABASE = "reference_categories_test.sqlite";
    private static final String SOURCE = "reference_categories_v1_source.sqlite";
    private Context context;
    private ReferenceStore store;

    @Before public void open() {
        context=ApplicationProvider.getApplicationContext();context.deleteDatabase(DATABASE);
        store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();ReferenceTestAccounts.installLegacyFixture(store);
    }
    @After public void close() { store.close();context.deleteDatabase(SOURCE); }
    private CategoryRepository categories() { return store.categoryRepository(); }
    private JSONObject draft(long categoryRowId) {
        return Json.object("book_id",1,"kind","expense","amount",1234,"currency","CNY",
            "category_id",categoryRowId,"account_id",1,"time",1735689600000L);
    }

    @Test public void freshDatabaseUsesOriginalSeedsAndKeepsRowIdsSeparateFromSemanticIds() {
        assertEquals(10,store.getReadableDatabase().getVersion());
        assertEquals(10,store.scalar("SELECT COUNT(*) n FROM categories WHERE level='parent' AND category_type=1"));
        assertEquals(66,store.scalar("SELECT COUNT(*) n FROM categories WHERE level='child' AND category_type=1"));
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM categories WHERE level='legacy'"));
        JSONObject parent=categories().parent(99),child=categories().child(9,911);
        assertNotNull(parent);assertEquals("其他",parent.optString("name"));assertNotEquals(99,parent.optLong("id"));
        assertEquals(parent.optLong("id"),store.defaultCategory("expense",1));
        assertEquals(child.optLong("id"),store.defaultCategory("income",1));
        assertEquals(99,parent.optLong("position_weight"));assertEquals(0,parent.optLong("update_time"));
        assertEquals(0,child.optInt("parent_child_semantic_id"));assertEquals(3,store.accounts().size());
    }

    @Test public void freshVersionThreeDatabaseHasNoAutomaticAccountSeeds() {
        String database="reference_categories_fresh_v3.sqlite";
        context.deleteDatabase(database);
        try(ReferenceStore fresh=new ReferenceStore(context,database)) {
            // This database never receives the compatibility fixture installed in @Before.
            assertEquals(10,fresh.getWritableDatabase().getVersion());
            assertEquals(0,fresh.scalar("SELECT COUNT(*) n FROM accounts"));
            assertTrue(fresh.accounts().isEmpty());
            assertEquals(0,fresh.scalar("SELECT COUNT(*) n FROM category_accounts"));
            assertEquals(76,fresh.scalar("SELECT COUNT(*) n FROM categories"));
        } finally {
            context.deleteDatabase(database);
        }
    }

    @Test public void realVersionOneUpgradePreservesEveryOldRowAndReferenceWithoutNameInference() {
        store.close();context.deleteDatabase(DATABASE);createV1(DATABASE);
        store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();
        assertEquals(10,store.getReadableDatabase().getVersion());
        assertLegacyReferences();
        JSONObject original=categories().parent(8);
        assertNotNull(original);assertNotEquals(8,original.optLong("id"));
        assertEquals("购物消费",original.optString("name"));
        assertEquals(78,store.scalar("SELECT COUNT(*) n FROM categories"));
        store.close();store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();
        assertEquals(78,store.scalar("SELECT COUNT(*) n FROM categories"));assertLegacyReferences();
    }

    @Test public void bootstrapDoesNotRestoreAnIndividuallyDeletedSystemChild() {
        long row=categories().child(8,801).optLong("id");
        store.getWritableDatabase().delete("categories","id=?",new String[]{""+row});
        store.close();store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();
        assertNull(categories().child(8,801));assertEquals(65,store.scalar("SELECT COUNT(*) n FROM categories WHERE level='child' AND category_type=1"));
        assertEquals(10,store.scalar("SELECT COUNT(*) n FROM categories WHERE level='parent'"));
    }

    @Test public void independentChildBootstrapRestoresAllSeedsOnlyAfterNoSystemChildRemains() {
        long custom=categories().saveChild(0,"收入自定义","ic_income_other",9,0);
        store.getWritableDatabase().delete("categories","level='child' AND category_type=1",null);
        store.close();store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();
        assertNotNull(categories().byRowId(custom));assertEquals(66,store.scalar("SELECT COUNT(*) n FROM categories WHERE level='child' AND category_type=1"));
        assertEquals(10,store.scalar("SELECT COUNT(*) n FROM categories WHERE level='parent'"));
    }

    @Test public void bookExclusionsChangePickerOrderButDoNotInvalidateAnExistingSelection() {
        long other=store.saveBook(0,"第二账本","CNY",0);
        JSONObject first=categories().parent(99),parent=categories().parent(8),child=categories().child(8,811);
        categories().setHiddenInBook(first.optLong("id"),1,true);
        assertEquals(parent.optLong("id"),store.defaultCategory("expense",1));
        assertEquals(first.optLong("id"),store.defaultCategory("expense",other));
        categories().setHiddenInBook(parent.optLong("id"),1,true);categories().setHiddenInBook(child.optLong("id"),1,true);
        assertEquals(7,categories().expenseParents(1).get(0).optInt("semantic_id"));
        assertEquals(810,categories().children(8,1).get(0).optInt("semantic_id"));
        assertEquals(811,categories().children(8,other).get(0).optInt("semantic_id"));
        assertEquals(child.optLong("id"),categories().rowForSelection("expense",8,811).optLong("id"));
        JSONObject input=draft(child.optLong("id"));Json.put(input,"parent_semantic_id",8);Json.put(input,"child_semantic_id",811);
        long saved=store.saveTransaction(input,0,null);assertEquals(8,store.transaction(saved).optInt("parent_semantic_id"));assertEquals(811,store.transaction(saved).optInt("child_semantic_id"));
    }

    @Test public void incomeTopLevelAndNestedChildrenRemainSeparateAndCustomWeightControlsDefault() {
        long topRow=categories().saveChild(0,"自定义收入","ic_income_other",9,0);
        int top=categories().byRowId(topRow).optInt("semantic_id");
        long nested=categories().saveChild(0,"细分收入","ic_income_other",9,top);
        assertEquals(topRow,categories().incomeTopLevel(1).get(0).optLong("id"));
        assertEquals(topRow,store.defaultCategory("income",1));
        assertEquals(12,categories().incomeTopLevel(1).size());assertEquals(1,categories().nestedIncome(top,1).size());
        assertEquals(nested,categories().nestedIncome(top,1).get(0).optLong("id"));
        categories().setHiddenInBook(topRow,1,true);assertEquals(categories().child(9,911).optLong("id"),store.defaultCategory("income",1));
        assertEquals(nested,categories().rowForSelection("income",9,categories().byRowId(nested).optInt("semantic_id")).optLong("id"));
    }

    @Test public void selectingAllHiddenCategoriesHasNoInventedDefaultAndStillAllowsParentOnlySave() {
        for(JSONObject parent:categories().expenseParents(1))categories().setHiddenInBook(parent.optLong("id"),1,true);
        assertEquals(0,store.defaultCategory("expense",1));
        long saved=store.saveTransaction(draft(categories().parent(99).optLong("id")),0,null);
        assertEquals(99,store.transaction(saved).optInt("parent_semantic_id"));assertEquals(-1,store.transaction(saved).optInt("child_semantic_id"));
    }

    @Test public void parentOnlySentinelsKeepDistinctExactAccountMappingsIncludingNoAccount() {
        long parent=categories().parent(99).optLong("id");
        JSONObject manual=draft(parent);long manualId=store.saveTransaction(manual,0,null);
        JSONObject automatic=draft(parent);Json.put(automatic,"child_semantic_id",0);Json.put(automatic,"account_id",2);long autoId=store.saveTransaction(automatic,0,null);
        assertEquals(-1,store.transaction(manualId).optInt("child_semantic_id"));assertEquals(0,store.transaction(autoId).optInt("child_semantic_id"));
        assertEquals(1,categories().rememberedAccountRowId(99,-1));assertEquals(2,categories().rememberedAccountRowId(99,0));
        Json.put(manual,"account_id",0);store.saveTransaction(manual,0,null);
        assertEquals(-1,categories().rememberedAccountRowId(99,-1));assertEquals(2,categories().rememberedAccountRowId(99,0));
        assertEquals(3,store.scalar("SELECT COUNT(*) n FROM category_accounts"));
        JSONObject legacy=store.one("SELECT * FROM category_accounts WHERE owner_semantic_id IS NULL AND parent_semantic_id=99 AND child_semantic_id=-1");
        assertNotNull(legacy);assertEquals(1,legacy.optLong("account_row_id"));assertTrue(legacy.isNull("raw_asset_semantic_id"));
        JSONObject noAccount=store.one("SELECT * FROM category_accounts WHERE owner_semantic_id=0 AND parent_semantic_id=99 AND child_semantic_id=-1");
        assertNotNull(noAccount);assertTrue(noAccount.isNull("account_row_id"));assertEquals(0,noAccount.optLong("raw_asset_semantic_id"));
        assertNotEquals(legacy.optLong("id"),noAccount.optLong("id"));
    }

    @Test public void learningUsesOrdinaryBillsAndExactPairsWithoutParentFallback() {
        long parent=categories().parent(8).optLong("id"),child=categories().child(8,811).optLong("id");
        store.saveTransaction(draft(parent),0,null);assertEquals(1,categories().rememberedAccountRowId(8,-1));assertEquals(-1,categories().rememberedAccountRowId(8,811));
        JSONObject regular=draft(child);Json.put(regular,"account_id",2);Json.put(regular,"financial_type","reimbursement");store.saveTransaction(regular,0,null);
        assertEquals(2,categories().rememberedAccountRowId(8,811));
        Json.put(regular,"account_id",3);Json.put(regular,"financial_type","lend");store.saveTransaction(regular,0,null);assertEquals(2,categories().rememberedAccountRowId(8,811));
        JSONObject transfer=draft(child);Json.put(transfer,"kind","transfer");Json.put(transfer,"account_id",3);Json.put(transfer,"to_account_id",2);store.saveTransaction(transfer,0,null);assertEquals(2,categories().rememberedAccountRowId(8,811));
        assertNull(categories().rowForSelection("expense",8,101));
        try{categories().rowForSelection("income",8,811);fail();}catch(IllegalArgumentException expected){}
    }

    @Test public void versionThreeBackupKeepsHierarchyHiddenBooksSemanticPairsAndMapping() {
        JSONObject child=categories().child(8,811);long saved=store.saveTransaction(draft(child.optLong("id")),0,null);
        categories().setHiddenInBook(child.optLong("id"),1,true);JSONObject backup=store.backup();assertEquals(10,backup.optInt("version"));
        store.restore(backup);assertEquals(child.optLong("id"),categories().child(8,811).optLong("id"));
        assertTrue(categories().isHiddenInBook(child.optLong("id"),1));assertEquals(811,store.transaction(saved).optInt("child_semantic_id"));assertEquals(1,categories().rememberedAccountRowId(8,811));
        assertEquals(0,foreignKeyErrors());
    }

    @Test public void oldBackupRestorePreservesLegacyNullIdentityAndAddsOriginalSeeds() {
        JSONObject old=createV1(SOURCE);
        JSONObject category=old.optJSONObject("tables").optJSONArray("categories").optJSONObject(0);
        // Even a mislabeled v1 document cannot smuggle inferred identity into a legacy row.
        Json.put(category,"level","parent");Json.put(category,"semantic_id",8);Json.put(category,"category_type",1);
        store.restore(old);assertLegacyReferences();assertEquals(78,store.scalar("SELECT COUNT(*) n FROM categories"));
        store.saveTransaction(draft(8),0,null);assertEquals(0,store.scalar("SELECT COUNT(*) n FROM category_accounts"));
        store.restore(store.backup());assertLegacyReferences();assertEquals(0,foreignKeyErrors());
    }

    @Test public void invalidOldBackupRollsBackExistingDataAndCategoryMetadataTogether() {
        long saved=store.saveTransaction(draft(categories().parent(99).optLong("id")),0,null);
        categories().setHiddenInBook(categories().parent(8).optLong("id"),1,true);
        JSONObject old=createV1(SOURCE);Json.put(old.optJSONObject("tables").optJSONArray("transactions").optJSONObject(0),"category_id",999999);
        try{store.restore(old);fail();}catch(IllegalArgumentException expected){}
        assertNotNull(store.transaction(saved));assertEquals(76,store.scalar("SELECT COUNT(*) n FROM categories"));
        assertTrue(categories().isHiddenInBook(categories().parent(8).optLong("id"),1));assertEquals(1,categories().rememberedAccountRowId(99,-1));assertEquals(0,foreignKeyErrors());
    }

    @Test public void inconsistentVersionThreeSemanticIdentityRollsBackWithoutDeletingTheBill() {
        long saved=store.saveTransaction(draft(categories().child(8,811).optLong("id")),0,null);JSONObject backup=store.backup();
        Json.put(backup.optJSONObject("tables").optJSONArray("transactions").optJSONObject(0),"child_semantic_id",810);
        try{store.restore(backup);fail();}catch(IllegalArgumentException expected){}
        assertEquals(811,store.transaction(saved).optInt("child_semantic_id"));assertEquals(76,store.scalar("SELECT COUNT(*) n FROM categories"));assertEquals(0,foreignKeyErrors());
    }

    @Test public void missingNestedIncomeMetadataCannotSilentlyPromoteItToTopLevel() {
        long topRow=categories().saveChild(0,"原上级","ic_income_other",9,0);int top=categories().byRowId(topRow).optInt("semantic_id");
        long nested=categories().saveChild(0,"原子类","ic_income_other",9,top);JSONObject backup=store.backup();
        backupCategory(backup,nested).remove("parent_child_semantic_id");
        try{store.restore(backup);fail();}catch(IllegalArgumentException expected){}
        assertEquals(top,categories().byRowId(nested).optInt("parent_child_semantic_id"));assertEquals(nested,categories().nestedIncome(top,1).get(0).optLong("id"));
    }

    @Test public void oversizedParentIdsCannotTruncateIntoAnExistingIncomeParent() {
        long child=categories().child(9,911).optLong("id");JSONObject backup=store.backup();
        Json.put(backupCategory(backup,child),"parent_semantic_id",4294967305L);
        try{store.restore(backup);fail();}catch(IllegalArgumentException expected){}
        assertEquals(9,categories().byRowId(child).optLong("parent_semantic_id"));assertEquals(child,store.defaultCategory("income",1));assertEquals(0,foreignKeyErrors());
        JSONObject invalid=draft(categories().parent(99).optLong("id"));Json.put(invalid,"parent_semantic_id",4294967395L);
        String pending=store.addCandidate(invalid,"test.invalid-semantic",1);
        try{store.saveTransaction(invalid,0,pending);fail();}catch(IllegalArgumentException expected){}
        assertEquals("PENDING",store.candidate(pending).optString("state"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
    }

    @Test public void incomeNestingRejectsCyclesAndAnotherLevelThatItsPickerCannotReach() {
        long nested=categories().saveChild(0,"收入细类","ic_income_other",9,904);int nestedId=categories().byRowId(nested).optInt("semantic_id");
        try{categories().saveChild(0,"再细分","ic_income_other",9,nestedId);fail();}catch(IllegalArgumentException expected){}
        JSONObject backup=store.backup();Json.put(backupCategory(backup,nested),"parent_child_semantic_id",nestedId);
        try{store.restore(backup);fail();}catch(IllegalArgumentException expected){}
        assertEquals(904,categories().byRowId(nested).optInt("parent_child_semantic_id"));assertEquals(77,store.scalar("SELECT COUNT(*) n FROM categories"));
    }

    private JSONObject backupCategory(JSONObject backup,long rowId) {
        JSONArray rows=backup.optJSONObject("tables").optJSONArray("categories");
        for(int i=0;i<rows.length();i++)if(rows.optJSONObject(i).optLong("id")==rowId)return rows.optJSONObject(i);
        throw new AssertionError("No category row in backup");
    }

    private long foreignKeyErrors() { try(Cursor cursor=store.getReadableDatabase().rawQuery("PRAGMA foreign_key_check",null)){return cursor.getCount();} }
    private void assertLegacyReferences() {
        JSONObject legacy=categories().byRowId(8);assertEquals("legacy",legacy.optString("level"));assertEquals("购物消费",legacy.optString("name"));assertTrue(legacy.isNull("semantic_id"));assertTrue(legacy.isNull("parent_semantic_id"));
        JSONObject transaction=store.transaction(123);assertEquals(8,transaction.optLong("category_id"));assertEquals("购物消费",transaction.optString("category"));assertTrue(transaction.isNull("parent_semantic_id"));assertTrue(transaction.isNull("child_semantic_id"));
        assertEquals(8,store.one("SELECT category_id FROM rules WHERE id=4").optLong("category_id"));
        assertEquals(8,Json.parse(store.candidate("old-candidate").optString("payload")).optLong("category_id"));
        assertEquals(8,Json.parse(store.one("SELECT payload FROM plans WHERE id=6").optString("payload")).optLong("category_id"));
        assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transaction_tags WHERE transaction_id=123 AND tag_id=3"));
    }

    /** A real v1 schema, independent of the current helper's onCreate implementation. */
    private JSONObject createV1(String name) {
        context.deleteDatabase(name);
        try(SQLiteDatabase db=context.openOrCreateDatabase(name,Context.MODE_PRIVATE,null)) {
            db.setForeignKeyConstraintsEnabled(true);
            db.execSQL("CREATE TABLE books(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,currency TEXT NOT NULL DEFAULT 'CNY',budget INTEGER NOT NULL DEFAULT 0,archived INTEGER NOT NULL DEFAULT 0,created INTEGER NOT NULL)");
            db.execSQL("CREATE TABLE accounts(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,type TEXT NOT NULL DEFAULT 'cash',currency TEXT NOT NULL DEFAULT 'CNY',opening INTEGER NOT NULL DEFAULT 0,archived INTEGER NOT NULL DEFAULT 0)");
            db.execSQL("CREATE TABLE categories(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,kind TEXT NOT NULL,icon TEXT NOT NULL,archived INTEGER NOT NULL DEFAULT 0)");
            db.execSQL("CREATE TABLE tags(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE)");
            db.execSQL("CREATE TABLE candidates(id TEXT PRIMARY KEY,payload TEXT NOT NULL,package_name TEXT NOT NULL,book_id INTEGER NOT NULL REFERENCES books(id),created INTEGER NOT NULL,state TEXT NOT NULL DEFAULT 'PENDING',transaction_id INTEGER)");
            db.execSQL("CREATE TABLE transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,book_id INTEGER NOT NULL REFERENCES books(id),kind TEXT NOT NULL,amount INTEGER NOT NULL,currency TEXT NOT NULL,category_id INTEGER REFERENCES categories(id),account_id INTEGER REFERENCES accounts(id),to_account_id INTEGER REFERENCES accounts(id),remark TEXT NOT NULL DEFAULT '',merchant TEXT NOT NULL DEFAULT '',origin TEXT NOT NULL DEFAULT '',asset_text TEXT NOT NULL DEFAULT '',discount INTEGER NOT NULL DEFAULT 0,fee INTEGER NOT NULL DEFAULT 0,time INTEGER NOT NULL,created INTEGER NOT NULL,not_total INTEGER NOT NULL DEFAULT 0,not_budget INTEGER NOT NULL DEFAULT 0,deleted INTEGER NOT NULL DEFAULT 0,financial_type TEXT NOT NULL DEFAULT '',settled INTEGER NOT NULL DEFAULT 0,attachments TEXT NOT NULL DEFAULT '[]',candidate_id TEXT UNIQUE REFERENCES candidates(id),related_id INTEGER REFERENCES transactions(id),import_key TEXT)");
            db.execSQL("CREATE TABLE transaction_tags(transaction_id INTEGER REFERENCES transactions(id) ON DELETE CASCADE,tag_id INTEGER REFERENCES tags(id),PRIMARY KEY(transaction_id,tag_id))");
            db.execSQL("CREATE TABLE rules(id INTEGER PRIMARY KEY AUTOINCREMENT,merchant TEXT NOT NULL UNIQUE,category_id INTEGER REFERENCES categories(id),book_id INTEGER REFERENCES books(id),account_id INTEGER REFERENCES accounts(id))");
            db.execSQL("CREATE TABLE plans(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,interval TEXT NOT NULL,payload TEXT NOT NULL,next_due INTEGER NOT NULL,enabled INTEGER NOT NULL DEFAULT 1)");
            db.execSQL("CREATE TABLE goals(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,target INTEGER NOT NULL,saved INTEGER NOT NULL DEFAULT 0,created INTEGER NOT NULL)");
            db.execSQL("INSERT INTO books(id,name,created) VALUES(1,'原账本',1735689600000)");db.execSQL("INSERT INTO accounts(id,name) VALUES(1,'原账户')");
            db.execSQL("INSERT INTO categories(id,name,kind,icon) VALUES(8,'购物消费','expense','ic_parent_shopping'),(77,'其他','income','ic_income_other')");
            db.execSQL("INSERT INTO tags(id,name) VALUES(3,'原标签')");
            JSONObject payload=draft(8);String json=payload.toString();
            db.execSQL("INSERT INTO candidates(id,payload,package_name,book_id,created,state,transaction_id) VALUES('old-candidate',?,'test.legacy',1,1735689600000,'SAVED',123)",new Object[]{json});
            db.execSQL("INSERT INTO transactions(id,book_id,kind,amount,currency,category_id,account_id,time,created,candidate_id) VALUES(123,1,'expense',1234,'CNY',8,1,1735689600000,1735689600000,'old-candidate')");
            db.execSQL("INSERT INTO transaction_tags(transaction_id,tag_id) VALUES(123,3)");db.execSQL("INSERT INTO rules(id,merchant,category_id,book_id,account_id) VALUES(4,'旧商户',8,1,1)");
            db.execSQL("INSERT INTO plans(id,name,interval,payload,next_due) VALUES(6,'旧周期','monthly',?,1735689600000)",new Object[]{json});
            db.setVersion(1);JSONObject tables=new JSONObject();
            for(String table:new String[]{"books","accounts","categories","tags","candidates","transactions","transaction_tags","rules","plans","goals"}) {
                JSONArray rows=new JSONArray();try(Cursor cursor=db.rawQuery("SELECT * FROM "+table,null)) {
                    while(cursor.moveToNext()) {JSONObject row=new JSONObject();for(int i=0;i<cursor.getColumnCount();i++)Json.put(row,cursor.getColumnName(i),switch(cursor.getType(i)){case Cursor.FIELD_TYPE_NULL->null;case Cursor.FIELD_TYPE_INTEGER->cursor.getLong(i);default->cursor.getString(i);});rows.put(row);}
                }Json.put(tables,table,rows);
            }
            return Json.object("format","YimuReferenceBackup","version",1,"created",1735689600000L,"tables",tables);
        }
    }
}
