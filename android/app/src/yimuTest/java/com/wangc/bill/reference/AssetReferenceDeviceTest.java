package com.wangc.bill.reference;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Tests the storage bridge, not the still unported original balance/history persistence. */
@RunWith(AndroidJUnit4.class)
public final class AssetReferenceDeviceTest {
    private static final String DATABASE="reference_asset_references_test.sqlite";
    private ReferenceStore store;
    private Context context;
    @Before public void open(){context=ApplicationProvider.getApplicationContext();context.deleteDatabase(DATABASE);store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();}
    @After public void close(){if(store!=null)store.close();context.deleteDatabase(DATABASE);}

    private JSONObject draft(){return Json.object("book_id",1,"kind","expense","amount",350,"category_id",store.categoryRepository().child(8,811).optLong("id"),"time",1735689600000L);}
    private long originalAsset(int owner,long semantic){return store.assetRepository().insertRecovered(owner,semantic,1,0,new AssetRepository.Definition("明确语义的合成账户",1,"CNY"));}

    @Test public void manualAndAutomaticNoAccountRemainDifferentRawIdsAndMappings(){
        JSONObject manual=draft();long manualId=store.saveTransaction(manual,0,null);
        assertTrue(store.transaction(manualId).isNull("account_id"));
        assertEquals(0,store.transaction(manualId).optLong("raw_asset_semantic_id"));
        assertEquals(0,store.assetRepository().categoryMapping(0,8,811).rawSemanticId);
        JSONObject automatic=draft();String candidate=store.addCandidate(automatic,"local.synthetic",1);
        long automaticId=store.saveTransaction(automatic,0,candidate);
        assertEquals(-1,store.transaction(automaticId).optLong("raw_asset_semantic_id"));
        assertEquals(-1,store.assetRepository().categoryMapping(0,8,811).rawSemanticId);
        JSONObject payload=Json.parse(store.candidate(candidate).optString("payload"));
        assertEquals(-1,payload.optLong("raw_asset_semantic_id"));assertEquals(0,payload.optInt("owner_semantic_id"));
        assertEquals(2,store.scalar("SELECT COUNT(*) n FROM transactions"));
        store.restore(store.backup());
        assertEquals(0,store.transaction(manualId).optLong("raw_asset_semantic_id"));
        assertEquals(-1,store.transaction(automaticId).optLong("raw_asset_semantic_id"));
    }

    @Test public void realSemanticZeroAccountIsDistinctFromANullForeignKey(){
        long account=originalAsset(0,0);JSONObject input=draft();Json.put(input,"account_id",account);
        long id=store.saveTransaction(input,0,null);JSONObject saved=store.transaction(id);
        assertEquals(account,saved.optLong("account_id"));assertEquals(0,saved.optLong("raw_asset_semantic_id"));
        OriginalAssetSelection.Mapping mapping=store.assetRepository().categoryMapping(0,8,811);
        assertEquals(Long.valueOf(account),mapping.localRowId);assertEquals(0,mapping.rawSemanticId);
        store.restore(store.backup());assertEquals(account,store.transaction(id).optLong("account_id"));
    }

    @Test public void originalIdentityAndOwnerNeverComeFromTheLocalForeignKey(){
        long account=originalAsset(7,901);JSONObject input=draft();Json.put(input,"account_id",account);
        long id=store.saveTransaction(input,0,null);JSONObject saved=store.transaction(id);
        assertEquals(901,saved.optLong("raw_asset_semantic_id"));assertEquals(7,saved.optInt("owner_semantic_id"));
        assertTrue(store.assetRepository().categoryMapping(7,8,811).exists);assertFalse(store.assetRepository().categoryMapping(0,8,811).exists);
        assertEquals(Long.valueOf(account),store.assetRepository().categoryMapping(7,8,811).localRowId);
        Json.put(input,"raw_asset_semantic_id",account==901?902:account);
        try{store.saveTransaction(input,0,null);fail();}catch(IllegalArgumentException expected){}
        assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transactions"));
    }

    @Test public void legacyForeignKeysDoNotAcquireFabricatedRawIds(){
        long account=store.saveAccount(0,"旧实验合成账户","wallet","CNY",0);JSONObject input=draft();Json.put(input,"account_id",account);
        long id=store.saveTransaction(input,0,null);assertTrue(store.transaction(id).isNull("raw_asset_semantic_id"));
        OriginalAssetSelection.Mapping legacy=store.assetRepository().legacyCategoryMapping(8,811);
        assertTrue(legacy.exists);assertFalse(legacy.rawKnown);assertEquals(Long.valueOf(account),legacy.localRowId);
        assertFalse(store.assetRepository().categoryMapping(0,8,811).exists);
        store.restore(store.backup());assertTrue(store.transaction(id).isNull("raw_asset_semantic_id"));
    }

    @Test public void explicitAutomaticMinusOneSurvivesLaterEditingWithoutGuessingFromNullableFk(){
        JSONObject input=draft();Json.put(input,"raw_asset_semantic_id",-1);Json.put(input,"owner_semantic_id",0);
        long id=store.saveTransaction(input,0,null);JSONObject edited=store.transaction(id);Json.put(edited,"amount",420);
        store.saveTransaction(edited,id,null);assertEquals(-1,store.transaction(id).optLong("raw_asset_semantic_id"));
        assertEquals(-1,store.assetRepository().categoryMapping(0,8,811).rawSemanticId);
    }

    @Test public void editingAnOldUnknownNoAccountPreservesNullInsteadOfInventingZero(){
        JSONObject input=draft();Json.put(input,"owner_semantic_id",null);Json.put(input,"raw_asset_semantic_id",null);Json.put(input,"raw_to_asset_semantic_id",null);
        long id=store.saveTransaction(input,0,null);JSONObject edited=store.transaction(id);Json.put(edited,"amount",420);
        store.saveTransaction(edited,id,null);JSONObject saved=store.transaction(id);
        assertTrue(saved.isNull("owner_semantic_id"));assertTrue(saved.isNull("raw_asset_semantic_id"));assertTrue(saved.isNull("raw_to_asset_semantic_id"));
        assertFalse(store.assetRepository().categoryMapping(0,8,811).exists);
        store.restore(store.backup());assertTrue(store.transaction(id).isNull("raw_asset_semantic_id"));
    }

    @Test public void explicitPickerSelectionReplacesUnknownAndClearsStaleRawIds(){
        JSONObject input=draft();Json.put(input,"owner_semantic_id",null);Json.put(input,"raw_asset_semantic_id",null);Json.put(input,"raw_to_asset_semantic_id",null);
        long account=originalAsset(0,901);store.selectAccountForInput(input,"account_id",account,false);
        assertEquals(901,input.optLong("raw_asset_semantic_id"));long id=store.saveTransaction(input,0,null);
        JSONObject edited=store.transaction(id);store.selectAccountForInput(edited,"account_id",0,false);store.saveTransaction(edited,id,null);
        assertEquals(0,store.transaction(id).optLong("raw_asset_semantic_id"));assertTrue(store.transaction(id).isNull("account_id"));
        store.selectAccountForInput(edited,"account_id",0,true);store.saveTransaction(edited,id,null);
        assertEquals(-1,store.transaction(id).optLong("raw_asset_semantic_id"));
    }

    @Test public void fullWidthAssetIdDoesNotCreateAForeignKeyToItsNarrowedCategoryMapping(){
        long semantic=(1L<<32)+901;long account=originalAsset(0,semantic);JSONObject input=draft();Json.put(input,"account_id",account);
        long id=store.saveTransaction(input,0,null);assertEquals(semantic,store.transaction(id).optLong("raw_asset_semantic_id"));
        OriginalAssetSelection.Mapping mapping=store.assetRepository().categoryMapping(0,8,811);
        assertEquals(901,mapping.rawSemanticId);assertNull(mapping.localRowId);
        store.restore(store.backup());assertEquals(account,store.transaction(id).optLong("account_id"));assertEquals(semantic,store.transaction(id).optLong("raw_asset_semantic_id"));
    }

    @Test public void transferStoresBothSemanticIdentitiesAndRejectsDifferentOwners(){
        long from=originalAsset(7,901),to=originalAsset(7,902),foreign=originalAsset(8,902);
        JSONObject input=draft();Json.put(input,"kind","transfer");Json.put(input,"account_id",from);Json.put(input,"to_account_id",to);
        long id=store.saveTransaction(input,0,null);assertEquals(901,store.transaction(id).optLong("raw_asset_semantic_id"));assertEquals(902,store.transaction(id).optLong("raw_to_asset_semantic_id"));
        assertFalse(store.assetRepository().categoryMapping(7,8,811).exists);
        Json.put(input,"to_account_id",foreign);try{store.saveTransaction(input,0,null);fail();}catch(IllegalArgumentException expected){}
        assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transactions"));
        store.restore(store.backup());assertEquals(7,store.transaction(id).optInt("owner_semantic_id"));
    }

    @Test public void mixedLegacyAndOriginalTransfersDeriveOnlyTheKnownIdentityInEitherDirection(){
        long legacy=store.saveAccount(0,"旧实验合成账户","cash","CNY",0),original=originalAsset(7,901);
        JSONObject first=draft();Json.put(first,"kind","transfer");Json.put(first,"account_id",legacy);Json.put(first,"to_account_id",original);
        long firstId=store.saveTransaction(first,0,null);JSONObject firstSaved=store.transaction(firstId);
        assertTrue(firstSaved.isNull("raw_asset_semantic_id"));assertEquals(901,firstSaved.optLong("raw_to_asset_semantic_id"));assertEquals(7,firstSaved.optInt("owner_semantic_id"));
        JSONObject second=draft();Json.put(second,"kind","transfer");Json.put(second,"account_id",original);Json.put(second,"to_account_id",legacy);
        long secondId=store.saveTransaction(second,0,null);JSONObject secondSaved=store.transaction(secondId);
        assertEquals(901,secondSaved.optLong("raw_asset_semantic_id"));assertTrue(secondSaved.isNull("raw_to_asset_semantic_id"));assertEquals(7,secondSaved.optInt("owner_semantic_id"));
        store.restore(store.backup());assertEquals(2,store.scalar("SELECT COUNT(*) n FROM transactions"));assertTrue(store.transaction(firstId).isNull("raw_asset_semantic_id"));
    }

    @Test public void mismatchedRestoredRawIdentityRollsBackTransactionAndMappingTogether(){
        long account=originalAsset(7,901);JSONObject input=draft();Json.put(input,"account_id",account);long id=store.saveTransaction(input,0,null);
        JSONObject backup=store.backup();Json.put(backup.optJSONObject("tables").optJSONArray("transactions").optJSONObject(0),"raw_asset_semantic_id",902);
        try{store.restore(backup);fail();}catch(IllegalArgumentException expected){}
        assertEquals(901,store.transaction(id).optLong("raw_asset_semantic_id"));assertEquals(901,store.assetRepository().categoryMapping(7,8,811).rawSemanticId);
    }

    @Test public void numericStringsAreRejectedBeforeSqliteAffinityCanHideMalformedBackupTypes(){
        long account=originalAsset(7,901);JSONObject input=draft();Json.put(input,"account_id",account);long id=store.saveTransaction(input,0,null);
        String[][] changes={{"transactions","raw_asset_semantic_id","901"},{"transactions","owner_semantic_id","7"},{"books","hide","0"},{"accounts","asset_number","0"},{"category_accounts","bridge_revision","1"}};
        for(String[] change:changes){
            JSONObject backup=store.backup();Json.put(backup.optJSONObject("tables").optJSONArray(change[0]).optJSONObject(0),change[1],change[2]);
            try{store.restore(backup);fail(change[0]+"."+change[1]);}catch(IllegalArgumentException expected){}
            assertEquals(901,store.transaction(id).optLong("raw_asset_semantic_id"));assertEquals(Long.valueOf(account),store.assetRepository().categoryMapping(7,8,811).localRowId);
        }
        store.restore(store.backup());assertEquals(901,store.transaction(id).optLong("raw_asset_semantic_id"));
    }

    @Test public void oldBackupCannotSmuggleOriginalIdentityThroughExtraKeys(){
        long account=store.saveAccount(0,"旧实验合成账户","cash","CNY",0);JSONObject input=draft();Json.put(input,"account_id",account);long id=store.saveTransaction(input,0,null);
        JSONObject old=store.backup();Json.put(old,"version",2);JSONObject tables=old.optJSONObject("tables");tables.remove("asset_show_books");
        for(String table:new String[]{"books","accounts","transactions","category_accounts"}){
            JSONArray rows=tables.optJSONArray(table);
            for(int i=0;i<rows.length();i++){
                JSONObject row=rows.optJSONObject(i);List<String> keys=new ArrayList<>();Iterator<String> names=row.keys();while(names.hasNext())keys.add(names.next());
                for(String key:keys)if(!ReferenceSchemaV3.acceptsOldColumn(2,table,key))row.remove(key);
            }
        }
        JSONObject oldAccount=tables.optJSONArray("accounts").optJSONObject(0);Json.put(oldAccount,"asset_origin","original");Json.put(oldAccount,"semantic_id",account);Json.put(oldAccount,"owner_semantic_id",0);
        JSONObject oldBook=tables.optJSONArray("books").optJSONObject(0);Json.put(oldBook,"book_origin","recovered_local");Json.put(oldBook,"semantic_id",1);Json.put(oldBook,"owner_semantic_id",0);
        JSONObject oldTransaction=tables.optJSONArray("transactions").optJSONObject(0);Json.put(oldTransaction,"raw_asset_semantic_id",account);Json.put(oldTransaction,"owner_semantic_id",0);
        JSONObject oldMap=tables.optJSONArray("category_accounts").optJSONObject(0);Json.put(oldMap,"raw_asset_semantic_id",account);Json.put(oldMap,"owner_semantic_id",0);
        store.restore(old);
        assertTrue(store.assetRepository().byRowId(account).isNull("semantic_id"));assertTrue(store.book(1).isNull("semantic_id"));
        assertTrue(store.transaction(id).isNull("raw_asset_semantic_id"));assertTrue(store.transaction(id).isNull("owner_semantic_id"));
        assertFalse(store.assetRepository().categoryMapping(0,8,811).exists);assertFalse(store.assetRepository().legacyCategoryMapping(8,811).rawKnown);
        store.restore(store.backup());assertEquals(id,store.transaction(id).optLong("id"));
    }
}
