package com.wangc.bill.reference;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class OriginalAssetBalanceStoreDeviceTest {
    private Context context;
    private String database;
    private ReferenceStore store;
    @Before public void open(){context=ApplicationProvider.getApplicationContext();database="reference-balance-"+UUID.randomUUID()+".sqlite";store=new ReferenceStore(context,database);store.getWritableDatabase();}
    @After public void close(){if(store!=null)store.close();if(context!=null&&database!=null)context.deleteDatabase(database);}

    @Test public void everyIntegerTypeExceptNineReadsStoredNumberWithoutLedgerOrOpening(){
        AtomicInteger derivedCalls=new AtomicInteger();OriginalAssetBalanceStore balances=new OriginalAssetBalanceStore(store,(owner,semantic)->{derivedCalls.incrementAndGet();return 999;});
        for(int type:new int[]{Integer.MIN_VALUE,-1,0,1,2,3,4,5,6,7,8,10,99,Integer.MAX_VALUE}){
            long semantic=7000L+type,row=asset(7,semantic,type,17.25d);store.getWritableDatabase().execSQL("UPDATE accounts SET opening=999999 WHERE id=?",new Object[]{row});
            OriginalAssetBalanceStore.LoadedAsset loaded=balances.load(7,semantic);assertNotNull(loaded);assertEquals(row,loaded.rowId);assertEquals(semantic,loaded.semanticId);assertEquals(type,loaded.assetType);assertEquals(7,loaded.ownerSemanticId);
            assertEquals(17.25d,loaded.getAssetNumber(),0.0d);
        }
        assertEquals(0,derivedCalls.get());assertNull(balances.load(8,7001));
        long legacy=store.saveAccount(0,"旧实验账户","cash","CNY",100);assertNull(balances.load(7,legacy));
    }

    @Test public void freshLoadsRemainDistinctWhileACapturedHandleRemainsTheActualMutableObject(){
        asset(7,901,1,10);OriginalAssetBalanceStore balances=new OriginalAssetBalanceStore(store);
        OriginalAssetBalanceStore.LoadedAsset captured=balances.load(7,901),other=balances.load(7,901);assertNotSame(captured,other);
        captured.addNumber(2);captured.setUpdateTime(101);assertTrue(balances.saveLoaded(captured));assertEquals(10,other.getAssetNumber(),0);
        OriginalAssetBalanceStore.LoadedAsset fresh=balances.load(7,901);assertEquals(12,fresh.getAssetNumber(),0);fresh.setStoredNumber(99);fresh.setUpdateTime(202);assertTrue(balances.saveLoaded(fresh));
        AtomicReference<OriginalAssetBalanceStore.LoadedAsset> observed=new AtomicReference<>();Runnable capturedCallback=()->observed.set(captured);
        captured.addNumber(3);capturedCallback.run();assertSame(captured,observed.get());assertEquals(15,observed.get().getAssetNumber(),0);
        assertEquals(99,balances.load(7,901).getAssetNumber(),0);assertEquals(202,balances.load(7,901).updateTime());
    }

    @Test public void saveWritesAnExactZeroAndTimestampWithoutRewritingMetadataOrAnotherOwner(){
        long row=asset(7,901,2,7.5d),other=asset(8,901,2,33);OriginalAssetBalanceStore balances=new OriginalAssetBalanceStore(store);
        JSONObject before=store.assetRepository().byRowId(row);OriginalAssetBalanceStore.LoadedAsset loaded=balances.load(7,901);loaded.reduceNumber(7.5d);loaded.setUpdateTime(12345);
        assertTrue(balances.saveLoaded(loaded));JSONObject after=store.assetRepository().byRowId(row);assertEquals(0.0d,after.optDouble("asset_number"),0);assertEquals(12345,after.optLong("update_time"));
        for(String key:new String[]{"name","asset_origin","asset_type","asset_currency","total_quota","opening","position_weight","share_quota_asset_semantic_id"})assertEquals(key,before.opt(key),after.opt(key));
        assertEquals(33,store.assetRepository().byRowId(other).optDouble("asset_number"),0);assertEquals(0,store.scalar("SELECT COUNT(*) n FROM asset_histories"));assertEquals(1,balances.lastOutcome().affectedRows);
    }

    @Test public void anUpdateOfADeletedLoadedRowStillReturnsTrueAsTheOriginalOrmDid(){
        long row=asset(7,901,1,8);long other=asset(8,901,1,55);List<OriginalAssetBalanceStore.SaveOutcome> observations=new ArrayList<>();
        OriginalAssetBalanceStore balances=new OriginalAssetBalanceStore(store,null,(asset,outcome)->observations.add(outcome));OriginalAssetBalanceStore.LoadedAsset loaded=balances.load(7,901);
        store.getWritableDatabase().delete("accounts","id=?",new String[]{""+row});loaded.addNumber(2);assertTrue(balances.saveLoaded(loaded));
        assertEquals(1,observations.size());assertTrue(observations.get(0).returned);assertEquals(0,observations.get(0).affectedRows);assertEquals("",observations.get(0).errorType);
        assertNull(balances.load(7,901));assertEquals(10,loaded.getAssetNumber(),0);assertEquals(55,store.assetRepository().byRowId(other).optDouble("asset_number"),0);
    }

    @Test public void aRealSqliteFailureReturnsFalseAndAllowsSubsequentHistoryAndEffects(){
        long row=asset(7,901,1,8);AtomicReference<OriginalAssetBalanceStore.LoadedAsset> observed=new AtomicReference<>();List<Boolean> returns=new ArrayList<>();
        OriginalAssetBalanceStore balances=new OriginalAssetBalanceStore(store,null,(asset,outcome)->{observed.set(asset);returns.add(outcome.returned);throw new IllegalStateException("Observer is not a business gate");});
        OriginalAssetBalanceStore.LoadedAsset loaded=balances.load(7,901);loaded.reduceNumber(3);loaded.setUpdateTime(100);
        store.getWritableDatabase().execSQL("CREATE TRIGGER fixture_balance_failure BEFORE UPDATE OF asset_number ON accounts BEGIN SELECT RAISE(ABORT,'synthetic balance failure'); END");
        assertFalse(balances.saveLoaded(loaded));assertSame(loaded,observed.get());assertFalse(returns.get(0));assertEquals(-1,balances.lastOutcome().affectedRows);assertTrue(balances.lastOutcome().errorType.endsWith("SQLiteConstraintException"));
        assertEquals(5,loaded.getAssetNumber(),0);assertEquals(8,store.assetRepository().byRowId(row).optDouble("asset_number"),0);
        // This is the original caller's ignored-false continuation, not a successful balance save.
        AssetHistoryRepository histories=new AssetHistoryRepository(store);assertTrue(histories.appendOperation(7,901,-3,loaded.getAssetNumber(),"正常false后的历史").saved);assertEquals(1,histories.count(7));
        store.getWritableDatabase().execSQL("DROP TRIGGER fixture_balance_failure");assertTrue(balances.saveLoaded(loaded));assertEquals(2,returns.size());assertTrue(returns.get(1));assertEquals(5,balances.load(7,901).getAssetNumber(),0);
    }

    @Test public void typeNineKeepsItsDerivedObjectCacheSeparateFromItsPersistedNumber(){
        long row=asset(7,901,9,10);AtomicInteger calls=new AtomicInteger();AtomicReference<Double> derived=new AtomicReference<>(50.0d);
        OriginalAssetBalanceStore balances=new OriginalAssetBalanceStore(store,(owner,semantic)->{assertEquals(7,owner);assertEquals(901,semantic);calls.incrementAndGet();return derived.get();});
        OriginalAssetBalanceStore.LoadedAsset loaded=balances.load(7,901);assertEquals(50,loaded.getAssetNumber(),0);derived.set(60.0d);loaded.addNumber(3);loaded.setUpdateTime(123);assertTrue(balances.saveLoaded(loaded));
        assertEquals(13,loaded.storedNumber(),0);assertEquals(13,store.assetRepository().byRowId(row).optDouble("asset_number"),0);assertEquals(50,loaded.getAssetNumber(),0);assertEquals(1,calls.get());
        assertEquals(60,balances.load(7,901).getAssetNumber(),0);assertEquals(2,calls.get());
    }

    @Test public void typeNineMinusOneSentinelRecomputesAndMissingProviderNeverUsesStoredNumber(){
        asset(7,901,9,10);AtomicInteger calls=new AtomicInteger();OriginalAssetBalanceStore balances=new OriginalAssetBalanceStore(store,(owner,semantic)->calls.incrementAndGet()==1?-1.0d:70.0d);
        OriginalAssetBalanceStore.LoadedAsset loaded=balances.load(7,901);assertEquals(-1,loaded.getAssetNumber(),0);assertEquals(70,loaded.getAssetNumber(),0);assertEquals(70,loaded.getAssetNumber(),0);assertEquals(2,calls.get());
        OriginalAssetBalanceStore missing=new OriginalAssetBalanceStore(store);OriginalAssetBalanceStore.LoadedAsset unsupported=missing.load(7,901);assertEquals(10,unsupported.storedNumber(),0);
        try{unsupported.getAssetNumber();fail("Derived account silently used stored asset_number");}catch(IllegalStateException expected){}
        try{missing.saveLoaded(loaded);fail("Another adapter's handle was accepted");}catch(IllegalArgumentException expected){}
    }
    private long asset(int owner,long semantic,int type,double number){AssetRepository.Definition value=new AssetRepository.Definition("合成余额账户",type,"CNY");value.assetNumber=number;value.totalQuota=100;return store.assetRepository().insertRecovered(owner,semantic,1,0,value);}
}
