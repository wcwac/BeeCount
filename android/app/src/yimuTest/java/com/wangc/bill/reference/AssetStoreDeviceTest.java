package com.wangc.bill.reference;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class AssetStoreDeviceTest {
    private static final String DATABASE="reference_original_assets_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private AssetRepository assets;

    @Before public void open() {
        context=ApplicationProvider.getApplicationContext();context.deleteDatabase(DATABASE);
        store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();assets=new AssetRepository(store);
    }
    @After public void close() { if(store!=null)store.close();context.deleteDatabase(DATABASE); }

    private long asset(int owner,long semantic,int type,int weight,String currency,boolean hidden,Long... books) {
        AssetRepository.Definition definition=new AssetRepository.Definition("合成账户"+semantic,type,currency);
        definition.hide=hidden;definition.showBook.addAll(Arrays.asList(books));
        return assets.insertRecovered(owner,semantic,weight,1735689600000L,definition);
    }
    private void learn(int owner,int parent,int child,long raw,Long row) {
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();
        try { assets.learnCategoryAccount(db,owner,parent,child,raw,row,1735689600000L);db.setTransactionSuccessful(); }
        finally { db.endTransaction(); }
    }
    private static List<Long> semanticIds(List<JSONObject> rows) {
        List<Long> result=new ArrayList<>();for(JSONObject row:rows)result.add(row.optLong("semantic_id"));return result;
    }

    @Test public void freshDatabaseDoesNotInventThreeOriginalOrLegacyAccounts() {
        assertEquals(11,store.getReadableDatabase().getVersion());
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM accounts"));
        assertTrue(assets.picker(0,1L).isEmpty());assertTrue(assets.legacyPicker().isEmpty());
        assertNull(assets.p(0,0));assertNull(assets.r(-1,0,1));
    }

    @Test public void versionTwoSchemaUpgradePreservesLocalKeysForeignKeysAndUnknownMappingIdentity() {
        try(SQLiteDatabase db=SQLiteDatabase.create(null)) {
            db.setForeignKeyConstraintsEnabled(true);
            db.execSQL("CREATE TABLE accounts(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,type TEXT NOT NULL DEFAULT 'cash',currency TEXT NOT NULL DEFAULT 'CNY',opening INTEGER NOT NULL DEFAULT 0,archived INTEGER NOT NULL DEFAULT 0)");
            db.execSQL("CREATE TABLE category_accounts(parent_semantic_id INTEGER NOT NULL,child_semantic_id INTEGER NOT NULL,account_row_id INTEGER REFERENCES accounts(id),updated INTEGER NOT NULL,PRIMARY KEY(parent_semantic_id,child_semantic_id))");
            db.execSQL("CREATE TABLE retained_bill(id INTEGER PRIMARY KEY,account_id INTEGER REFERENCES accounts(id))");
            db.execSQL("INSERT INTO accounts(id,name,type,currency,opening) VALUES(42,'旧实验账户','wallet','USD',750)");
            db.execSQL("INSERT INTO retained_bill VALUES(9,42)");
            db.execSQL("INSERT INTO category_accounts VALUES(8,811,42,123)");
            db.execSQL("INSERT INTO category_accounts VALUES(99,-1,NULL,124)");
            db.beginTransaction();try { AssetRepository.installV3Schema(db);db.setTransactionSuccessful(); }finally{db.endTransaction();}
            try(Cursor account=db.rawQuery("SELECT * FROM accounts WHERE id=42",null)) {
                assertTrue(account.moveToFirst());assertEquals("旧实验账户",account.getString(account.getColumnIndexOrThrow("name")));
                assertEquals("legacy",account.getString(account.getColumnIndexOrThrow("asset_origin")));
                assertTrue(account.isNull(account.getColumnIndexOrThrow("semantic_id")));assertTrue(account.isNull(account.getColumnIndexOrThrow("owner_semantic_id")));
                assertTrue(account.isNull(account.getColumnIndexOrThrow("asset_currency")));assertEquals("USD",account.getString(account.getColumnIndexOrThrow("currency")));
                assertEquals(750,account.getLong(account.getColumnIndexOrThrow("opening")));
            }
            try(Cursor mappings=db.rawQuery("SELECT * FROM category_accounts ORDER BY updated",null)) {
                assertEquals(2,mappings.getCount());assertTrue(mappings.moveToFirst());assertEquals(42,mappings.getLong(mappings.getColumnIndexOrThrow("account_row_id")));
                assertTrue(mappings.isNull(mappings.getColumnIndexOrThrow("raw_asset_semantic_id")));assertTrue(mappings.isNull(mappings.getColumnIndexOrThrow("owner_semantic_id")));
                assertTrue(mappings.moveToNext());assertTrue(mappings.isNull(mappings.getColumnIndexOrThrow("account_row_id")));
            }
            try(Cursor bill=db.rawQuery("SELECT account_id FROM retained_bill WHERE id=9",null)){assertTrue(bill.moveToFirst());assertEquals(42,bill.getLong(0));}
            try(Cursor errors=db.rawQuery("PRAGMA foreign_key_check",null)){assertFalse(errors.moveToFirst());}
        }
    }

    @Test public void unknownCurrencyAndModelDefaultsRemainDistinctFromLegacyCnyAndTypeNineIsMetadataOnly() {
        AssetRepository.Definition definition=new AssetRepository.Definition("合成报销账户",9,null);
        long row=assets.insertRecovered(0,700,4,0,definition);JSONObject account=assets.byRowId(row);
        assertEquals("original",account.optString("asset_origin"));assertEquals(700,account.optLong("semantic_id"));
        assertTrue(account.isNull("asset_currency"));assertEquals("",account.optString("currency"));
        assertEquals(0,account.optDouble("asset_number"),0);assertEquals(0,account.optDouble("total_quota"),0);
        assertEquals(0,account.optInt("into_total_asset"));assertEquals(0,account.optInt("hide"));
        assertEquals(0,account.optInt("out_day_not_next"));assertTrue(account.isNull("simple_name"));
        assertEquals(9,assets.p(700,0).optInt("asset_type"));assertTrue(assets.showBook(row).isEmpty());
        assertTrue(assets.picker(0,1L).isEmpty());
        OriginalAssetSelection.AssetRef reference=AssetRepository.reference(account);
        assertEquals(row,reference.localRowId);assertEquals(700,reference.semanticId);
    }

    @Test public void ordinaryPickerFiltersHideAndTypesButDefaultLookupsDoNot() {
        long hidden=asset(0,101,9,100,"CNY",true,1L);
        long excluded=asset(0,102,1,90,"CNY",false,2L);
        asset(0,103,3,20,"CNY",false);asset(0,104,2,30,"CNY",false,1L);
        // archived is the former experiment's flag, not the recovered Asset.hide.
        store.getWritableDatabase().execSQL("UPDATE accounts SET archived=1 WHERE id=?",new Object[]{hidden});
        assertEquals(hidden,assets.p(101,0).optLong("id"));assertEquals(hidden,assets.q(101,0,1).optLong("id"));
        assertNull(assets.r(101,0,2));assertEquals(excluded,assets.p(102,0).optLong("id"));
        assertEquals(Arrays.asList(104L,103L),semanticIds(assets.picker(0,1L)));
        assertEquals(Arrays.asList(102L,104L,103L),semanticIds(assets.picker(0,null)));
    }

    @Test public void showBookIsOrderedAllowlistWithEmptyMeaningAllAndNeverActsAsGlobalHide() {
        long row=asset(0,5,1,0,"CNY",false);
        assertTrue(assets.allowsBook(row,123));
        assets.setShowBook(row,Arrays.asList(2L,1L,2L));
        assertEquals(Arrays.asList(2L,1L,2L),assets.showBook(row));
        assertTrue(assets.allowsBook(row,1));assertTrue(assets.allowsBook(row,2));assertFalse(assets.allowsBook(row,3));
        assertNotNull(assets.p(5,0));assertNull(assets.r(5,0,3));assertEquals(0,assets.byRowId(row).optInt("hide"));
        assets.setShowBook(row,new ArrayList<>());assertNotNull(assets.r(5,0,3));
    }

    @Test public void matchingScopesHaveDifferentHideRulesAndOwnerIdsNeverLeak() {
        asset(0,11,1,10,"CNY",true,2L);asset(0,12,0,20,"CNY",false,1L);
        asset(0,13,8,30,"CNY",false,1L);long another=asset(7,12,1,99,"USD",false,1L);
        assertEquals(Arrays.asList(12L,11L),semanticIds(assets.matching(0,null)));
        assertEquals(Arrays.asList(12L),semanticIds(assets.matching(0,1L)));
        assertTrue(assets.picker(0,1L).isEmpty());assertNull(assets.p(11,7));
        assertEquals(another,assets.r(12,7,1).optLong("id"));assertEquals(1,assets.picker(7,1L).size());
    }

    @Test public void originalCreationAllocatesIndependentSemanticIdentityAndOwnerLocalWeightWithoutSeeds() {
        asset(0,801,1,41,"CNY",false);
        AssetRepository.Definition definition=new AssetRepository.Definition("新建合成账户",2,"USD");definition.intoTotalAsset=true;
        long row=assets.createOriginal(0,definition);JSONObject account=assets.byRowId(row);
        assertEquals(42,account.optInt("position_weight"));assertEquals(0,account.optInt("owner_semantic_id"));
        assertTrue(account.optLong("semantic_id")>=0);assertTrue(account.optLong("semantic_id")<Integer.MAX_VALUE);
        assertEquals(row,assets.p(account.optLong("semantic_id"),0).optLong("id"));assertEquals(1,account.optInt("into_total_asset"));
        assertEquals(2,store.scalar("SELECT COUNT(*) n FROM accounts"));
        long other=assets.createOriginal(7,new AssetRepository.Definition("其他owner合成账户",1,null));
        assertEquals(1,assets.byRowId(other).optInt("position_weight"));assertEquals(0,assets.legacyPicker().size());
        asset(8,801,1,Integer.MAX_VALUE,null,false);
        long wrapped=assets.createOriginal(8,new AssetRepository.Definition("原int权重边界",1,null));
        assertEquals(Integer.MIN_VALUE,assets.byRowId(wrapped).optInt("position_weight"));
    }

    @Test public void categoryMappingsKeepAbsenceExplicitZeroExplicitMinusOneAndLegacyUnknownSeparate() {
        long zero=asset(0,0,1,1,"CNY",false,1L);
        assertFalse(assets.categoryMapping(0,8,811).exists);
        store.getWritableDatabase().execSQL("INSERT INTO category_accounts(parent_semantic_id,child_semantic_id,account_row_id,updated) VALUES(8,811,NULL,12)");
        OriginalAssetSelection.Mapping legacy=assets.legacyCategoryMapping(8,811);
        assertTrue(legacy.exists);assertFalse(legacy.rawKnown);assertNull(legacy.localRowId);
        learn(0,8,811,0,zero);
        OriginalAssetSelection.Mapping mapped=assets.categoryMapping(0,8,811);
        assertTrue(mapped.exists);assertTrue(mapped.rawKnown);assertEquals(0,mapped.rawSemanticId);assertEquals(Long.valueOf(zero),mapped.localRowId);
        learn(0,8,-1,-1,null);learn(0,8,0,0,null);learn(7,8,811,-1,null);
        assertEquals(-1,assets.categoryMapping(0,8,-1).rawSemanticId);assertEquals(0,assets.categoryMapping(0,8,0).rawSemanticId);
        assertEquals(-1,assets.categoryMapping(7,8,811).rawSemanticId);assertEquals(0,assets.categoryMapping(0,8,811).rawSemanticId);
        assertFalse(assets.legacyCategoryMapping(8,811).rawKnown);assertEquals(5,store.scalar("SELECT COUNT(*) n FROM category_accounts"));
    }

    @Test public void categoryLearningUsesOriginalIntCastAndUpdatesTheExistingMappingIdentity() {
        learn(0,8,811,(1L<<32)-1,null);
        JSONObject before=store.one("SELECT * FROM category_accounts WHERE owner_semantic_id=0");
        assertEquals(-1,assets.categoryMapping(0,8,811).rawSemanticId);
        learn(0,8,811,0,null);JSONObject after=store.one("SELECT * FROM category_accounts WHERE owner_semantic_id=0");
        assertEquals(before.optLong("id"),after.optLong("id"));assertEquals(before.optLong("category_asset_semantic_id"),after.optLong("category_asset_semantic_id"));
        assertEquals(0,assets.categoryMapping(0,8,811).rawSemanticId);assertEquals(1,store.scalar("SELECT COUNT(*) n FROM category_accounts"));
        store.getWritableDatabase().execSQL("UPDATE category_accounts SET category_asset_semantic_id=? WHERE owner_semantic_id=0",new Object[]{Integer.MAX_VALUE});
        learn(0,8,810,-1,null);
        assertEquals(Integer.MIN_VALUE,store.one("SELECT category_asset_semantic_id FROM category_accounts WHERE owner_semantic_id=0 AND child_semantic_id=810").optInt("category_asset_semantic_id"));
    }

    @Test public void legacyToNoAccountToAnotherLegacyAccountKeepsTheLastExplicitBridgeChoice() {
        long first=store.saveAccount(0,"第一次合成兼容账户","wallet","CNY",0);
        long last=store.saveAccount(0,"最后合成兼容账户","wallet","CNY",0);
        JSONObject input=Json.object("book_id",1,"kind","expense","amount",1250,"currency","CNY",
            "category_id",store.categoryRepository().child(8,811).optLong("id"),"time",1735689600000L,"account_id",first);
        store.saveTransaction(input,0,null);assertEquals(first,store.categoryRepository().rememberedAccountRowId(8,811));
        Json.put(input,"account_id",0);store.saveTransaction(input,0,null);
        assertEquals(-1,store.categoryRepository().rememberedAccountRowId(8,811));
        Json.put(input,"account_id",last);store.saveTransaction(input,0,null);
        assertEquals(last,store.categoryRepository().rememberedAccountRowId(8,811));
        // Equal source wall times must not allow the older owner-0 row to win.
        store.getWritableDatabase().execSQL("UPDATE category_accounts SET updated=123");
        assertEquals(last,store.categoryRepository().rememberedAccountRowId(8,811));
        assertEquals(2,store.scalar("SELECT COUNT(*) n FROM category_accounts WHERE parent_semantic_id=8 AND child_semantic_id=811"));
        OriginalAssetSelection.Mapping original=assets.categoryMapping(0,8,811),legacy=assets.legacyCategoryMapping(8,811);
        assertTrue(original.exists);assertTrue(original.rawKnown);assertEquals(0,original.rawSemanticId);assertNull(original.localRowId);
        assertTrue(legacy.exists);assertFalse(legacy.rawKnown);assertEquals(Long.valueOf(last),legacy.localRowId);
        // A different owner's later activity cannot replace this local compatibility choice.
        learn(7,8,811,-1,null);assertEquals(last,store.categoryRepository().rememberedAccountRowId(8,811));
        store.restore(store.backup());assertEquals(last,store.categoryRepository().rememberedAccountRowId(8,811));
        assertEquals(0,assets.categoryMapping(0,8,811).rawSemanticId);assertFalse(assets.legacyCategoryMapping(8,811).rawKnown);
    }

    @Test public void invalidShowBookReplacementRollsBackAndCannotLeavePartlyChangedMetadata() {
        long row=asset(0,22,1,1,"CNY",false,1L);
        try{assets.setShowBook(row,Arrays.asList(2L,null));fail();}catch(IllegalArgumentException expected){}
        assertEquals(Arrays.asList(1L),assets.showBook(row));
        AssetRepository.Definition bad=new AssetRepository.Definition("无效合成账户",1,null);bad.showBook.add(null);
        try{assets.insertRecovered(0,23,1,0,bad);fail();}catch(IllegalArgumentException expected){}
        assertNull(assets.p(23,0));assertEquals(1,store.scalar("SELECT COUNT(*) n FROM accounts"));
    }

    @Test public void metadataBackupRoundTripsBalancesAllowlistAndRawMappingWithoutRecomputingBills() {
        AssetRepository.Definition definition=new AssetRepository.Definition("原始小数余额",9,null);
        definition.assetNumber=12.25;definition.totalQuota=39.5;definition.showBook.addAll(Arrays.asList(1L,200L));definition.hide=true;
        long row=assets.insertRecovered(0,903,12,56,definition);learn(0,8,811,903,row);
        JSONObject backup=store.backup();assertEquals(11,backup.optInt("version"));store.restore(backup);
        JSONObject restored=assets.p(903,0);assertEquals(row,restored.optLong("id"));assertTrue(restored.isNull("asset_currency"));
        assertEquals(12.25,restored.optDouble("asset_number"),0);assertEquals(39.5,restored.optDouble("total_quota"),0);
        assertEquals(Arrays.asList(1L,200L),assets.showBook(row));assertEquals(903,assets.categoryMapping(0,8,811).rawSemanticId);
        assertEquals(Long.valueOf(row),assets.categoryMapping(0,8,811).localRowId);assertNotNull(assets.r(903,0,1));assertTrue(assets.picker(0,1L).isEmpty());
    }

    @Test public void inconsistentMappingBackupRollsBackAllExistingAssetMetadata() {
        long row=asset(0,901,1,30,"USD",true,1L);learn(0,8,811,901,row);
        JSONObject backup=store.backup();JSONArray mappings=backup.optJSONObject("tables").optJSONArray("category_accounts");
        assertNotNull(mappings);Json.put(mappings.optJSONObject(0),"raw_asset_semantic_id",902);
        try{store.restore(backup);fail();}catch(IllegalArgumentException expected){}
        assertEquals(row,assets.p(901,0).optLong("id"));assertEquals(901,assets.categoryMapping(0,8,811).rawSemanticId);
        assertEquals(Arrays.asList(1L),assets.showBook(row));assertEquals(1,assets.byRowId(row).optInt("hide"));
    }
}
