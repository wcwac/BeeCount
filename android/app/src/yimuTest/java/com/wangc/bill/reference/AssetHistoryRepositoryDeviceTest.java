package com.wangc.bill.reference;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real SQLite operations on disposable, entirely synthetic financial data. */
@RunWith(AndroidJUnit4.class)
public final class AssetHistoryRepositoryDeviceTest {
    private Context context;
    private String database;
    private ReferenceStore store;
    private AssetHistoryRepository history;
    @Before public void open(){context=ApplicationProvider.getApplicationContext();database="reference-history-"+UUID.randomUUID()+".sqlite";store=new ReferenceStore(context,database);store.getWritableDatabase();history=new AssetHistoryRepository(store);}
    @After public void close(){if(store!=null)store.close();if(context!=null&&database!=null)context.deleteDatabase(database);}

    @Test public void appendKeepsEightFieldsSeparateIdsTwoClocksAndAnExplicitZero(){
        AssetHistoryRepository repository=new AssetHistoryRepository(store,new SequenceRandom(7001),new SequenceClock(101,205));
        AssetHistoryRepository.SavedHistory saved=repository.appendOperation(7,901,-0.0d,0.0d,null);
        assertTrue(saved.saved);assertEquals("",saved.errorType);assertNotEquals(7001,saved.rowId);assertEquals(7001,saved.semanticId);
        JSONObject row=repository.byIdentity(7,7001);assertEquals(9,row.length());assertEquals(saved.rowId,row.optLong("id"));
        assertEquals(901,row.optLong("asset_semantic_id"));assertEquals(7,row.optInt("owner_semantic_id"));assertEquals(101,row.optLong("time"));assertEquals(205,row.optLong("update_time"));
        assertTrue(row.isNull("change_content"));assertEquals(0.0d,row.optDouble("change_number"),0.0d);assertEquals(0.0d,row.optDouble("current_number"),0.0d);
        try(Cursor keys=store.getReadableDatabase().rawQuery("PRAGMA foreign_key_list(asset_histories)",null)){assertEquals("History unexpectedly acquired an Asset/Bill cascade",0,keys.getCount());}
        assertFalse(row.has("bill_id"));assertFalse(row.has("book_id"));assertFalse(row.has("currency"));repository.validateRestored();
    }

    @Test public void randomIdentityRetriesOnlyWithinItsOwnerAndAcceptsZero(){
        AtomicLong clock=new AtomicLong(100);
        AssetHistoryRepository repository=new AssetHistoryRepository(store,new SequenceRandom(7001,7001,0,7001),clock::incrementAndGet);
        AssetHistoryRepository.SavedHistory first=repository.appendOperation(7,901,1,1,"第一条");
        AssetHistoryRepository.SavedHistory zero=repository.appendOperation(7,901,2,3,"第二条");
        AssetHistoryRepository.SavedHistory other=repository.appendOperation(8,901,4,4,"另一归属");
        assertEquals(7001,first.semanticId);assertEquals(0,zero.semanticId);assertEquals(7001,other.semanticId);
        assertNotEquals(first.rowId,other.rowId);assertEquals(2,repository.count(7));assertEquals(1,repository.count(8));
        assertEquals(zero.rowId,repository.byIdentity(7,0).optLong("id"));assertNull(repository.byIdentity(9,7001));
    }

    @Test public void assetQueriesKeepOwnerScopeAndTheOriginalTimeBoundaries(){
        AssetHistoryRepository repository=new AssetHistoryRepository(store,new SequenceRandom(7001,7002,7003,7004,7005),new SequenceClock(100,110,200,210,300,310,250,260,400,410));
        repository.appendOperation(7,901,1,1,"早");repository.appendOperation(7,901,2,3,"中");repository.appendOperation(7,901,3,6,"晚");
        repository.appendOperation(8,901,4,4,"另一用户");repository.appendOperation(7,902,5,5,"另一账户");
        List<JSONObject> rows=repository.forAsset(7,901);assertEquals(3,rows.size());assertEquals(300,rows.get(0).optLong("time"));assertEquals(100,rows.get(2).optLong("time"));
        rows=repository.atOrAfter(7,901,200);assertEquals(2,rows.size());assertEquals(200,rows.get(0).optLong("time"));assertEquals(300,rows.get(1).optLong("time"));
        assertEquals(100,repository.before(7,901,200).optLong("time"));assertNull(repository.before(7,901,100));
        assertEquals(2,repository.updatedAfter(7,210).size());assertEquals(410,repository.maxUpdated(7));assertEquals(4,repository.count(7));assertEquals(0,repository.count(99));
    }

    @Test public void deletingHistoryNeverChangesStoredBalanceAndDoesNotCrossOwners(){
        long account=asset(7,901,42.25d);
        AssetHistoryRepository.SavedHistory first=history.appendOperation(7,901,-1,42.25,"删除测试");
        history.appendOperation(7,901,0,42.25,"零也由调用者保留");history.appendOperation(8,901,9,9,"另一用户");
        assertEquals(1,history.deleteHistory(7,first.semanticId));assertEquals(0,history.deleteHistory(7,first.semanticId));assertEquals(1,history.deleteForAsset(7,901));
        assertEquals(42.25d,store.assetRepository().byRowId(account).optDouble("asset_number"),0.0d);assertEquals(1,history.count(8));assertEquals(0,history.count(7));
    }

    @Test public void historySurvivesAssetDeletionBecauseItHasNoAssetForeignKey(){
        long account=asset(7,901,11);
        AssetHistoryRepository.SavedHistory saved=history.appendOperation(7,901,-2,11,"原账户随后删除");
        store.getWritableDatabase().delete("accounts","id=?",new String[]{""+account});
        assertNull(store.assetRepository().p(901,7));assertEquals(saved.rowId,history.byIdentity(7,saved.semanticId).optLong("id"));
        history.validateRestored();try(Cursor violations=store.getReadableDatabase().rawQuery("PRAGMA foreign_key_check",null)){assertFalse(violations.moveToFirst());}
    }

    @Test public void sqliteInsertFailureReturnsMetadataWithoutSuppressingLaterCallerEffects(){
        AssetHistoryRepository repository=new AssetHistoryRepository(store,new SequenceRandom(7001,7001,7001),new SequenceClock(100,101,150,151,200,201));
        store.getWritableDatabase().execSQL("CREATE TRIGGER fixture_history_failure BEFORE INSERT ON asset_histories BEGIN SELECT RAISE(ABORT,'synthetic history failure'); END");
        AssetHistoryRepository.SavedHistory failed=repository.appendOperation(7,901,-1,9,"不得泄露到异常元数据");
        assertFalse(failed.saved);assertEquals(0,failed.rowId);assertEquals(7001,failed.semanticId);assertEquals(100,failed.time);assertEquals(101,failed.updateTime);
        assertTrue(failed.errorType.endsWith("SQLiteConstraintException"));assertFalse(failed.errorType.contains("synthetic"));assertEquals(0,repository.count(7));
        store.getWritableDatabase().execSQL("DROP TRIGGER fixture_history_failure");
        store.getWritableDatabase().execSQL("CREATE TRIGGER fixture_history_ignore BEFORE INSERT ON asset_histories BEGIN SELECT RAISE(IGNORE); END");
        AssetHistoryRepository.SavedHistory ignored=repository.appendOperation(7,901,0,9,"未插入");assertFalse(ignored.saved);assertEquals(0,ignored.rowId);assertEquals(0,repository.count(7));
        store.getWritableDatabase().execSQL("DROP TRIGGER fixture_history_ignore");
        AssetHistoryRepository.SavedHistory next=repository.appendOperation(7,901,0,9,"后续阶段");assertTrue(next.saved);assertEquals(7001,next.semanticId);assertEquals(1,repository.count(7));
    }

    @Test public void backupRoundTripKeepsHistoryIdentityWithoutReplayingItsBalanceChange(){
        long account=asset(7,901,11.25d);AssetHistoryRepository.SavedHistory saved=history.appendOperation(7,901,-3.5d,11.25d,"保留原历史");
        JSONObject backup=store.backup();assertNotNull(backup.optJSONObject("tables").optJSONArray(AssetHistoryRepository.TABLE));
        store.getWritableDatabase().execSQL("UPDATE accounts SET asset_number=99 WHERE id=?",new Object[]{account});history.deleteForAsset(7,901);
        store.restore(backup);JSONObject row=history.byIdentity(7,saved.semanticId);
        assertEquals(saved.rowId,row.optLong("id"));assertEquals(-3.5d,row.optDouble("change_number"),0.0d);assertEquals(11.25d,row.optDouble("current_number"),0.0d);
        assertEquals(11.25d,store.assetRepository().byRowId(account).optDouble("asset_number"),0.0d);assertEquals(1,history.count(7));history.validateRestored();
    }

    @Test public void invalidHistoryBackupsRollBackEveryTableInsteadOfPartiallyRestoring(){
        long account=asset(7,901,17);AssetHistoryRepository.SavedHistory saved=history.appendOperation(7,901,-2,17,null);JSONObject valid=store.backup();
        Object[][] defects={{"id",0},{"semantic_id",-1},{"semantic_id",1.5d},{"owner_semantic_id",2147483648L},{"time",1.5d},{"current_number",null},{"change_content",true}};
        for(Object[] defect:defects){
            JSONObject invalid=Json.copy(valid);JSONArray rows=invalid.optJSONObject("tables").optJSONArray(AssetHistoryRepository.TABLE);Json.put(rows.optJSONObject(0),(String)defect[0],defect[1]);
            try{store.restore(invalid);fail("Accepted malformed history field "+defect[0]);}catch(IllegalArgumentException|android.database.SQLException expected){}
            assertEquals(1,history.count(7));assertEquals(saved.rowId,history.byIdentity(7,saved.semanticId).optLong("id"));assertEquals(17,store.assetRepository().byRowId(account).optDouble("asset_number"),0);
        }
    }

    private long asset(int owner,long semantic,double number){AssetRepository.Definition value=new AssetRepository.Definition("合成历史账户",1,"CNY");value.assetNumber=number;return store.assetRepository().insertRecovered(owner,semantic,1,0,value);}
    private static final class SequenceRandom extends Random {
        private final int[] values;private int index;
        SequenceRandom(int... values){this.values=values;}
        @Override public int nextInt(int bound){if(index>=values.length)throw new AssertionError("Unexpected random draw");assertEquals(Integer.MAX_VALUE,bound);return values[index++];}
    }
    private static final class SequenceClock implements LongSupplier {
        private final long[] values;private int index;
        SequenceClock(long... values){this.values=values;}
        public long getAsLong(){if(index>=values.length)throw new AssertionError("Unexpected clock read");return values[index++];}
    }
}
