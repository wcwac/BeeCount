package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;
import java.util.Random;
import java.util.function.LongSupplier;
import org.json.JSONObject;

/** Local AssetHistory persistence recovered from k.e/n/o/p/q/r/u. */
public final class AssetHistoryRepository {
    public static final String TABLE = "asset_histories";
    private final ReferenceStore store;
    private final Random random;
    private final LongSupplier clock;

    public AssetHistoryRepository(ReferenceStore store) { this(store,new Random(),System::currentTimeMillis); }
    AssetHistoryRepository(ReferenceStore store,Random random,LongSupplier clock) {
        if(store==null||random==null||clock==null)throw new IllegalArgumentException("历史记录依赖不能为空");
        this.store=store;this.random=random;this.clock=clock;
    }

    /** Eight original fields plus an independent local PK. No Bill or Asset FK. */
    public static void installV4Schema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE asset_histories(id INTEGER PRIMARY KEY AUTOINCREMENT,semantic_id INTEGER NOT NULL,asset_semantic_id INTEGER NOT NULL,change_content TEXT,change_number REAL NOT NULL,current_number REAL NOT NULL,time INTEGER NOT NULL,update_time INTEGER NOT NULL,owner_semantic_id INTEGER NOT NULL,UNIQUE(owner_semantic_id,semantic_id))");
        db.execSQL("CREATE INDEX asset_histories_asset_time ON asset_histories(owner_semantic_id,asset_semantic_id,time)");
        db.execSQL("CREATE INDEX asset_histories_updated ON asset_histories(owner_semantic_id,update_time)");
    }

    public static final class SavedHistory {
        public final long rowId,semanticId,time,updateTime;
        public final int ownerSemanticId;
        public final boolean saved;
        public final String stage="ASSET_HISTORY_SAVE";
        /** Exception class only: SQL text and supplied financial values are never logged. */
        public final String errorType;
        private SavedHistory(long rowId,int owner,long semantic,long time,long updated,boolean saved,String errorType) {
            this.rowId=rowId;ownerSemanticId=owner;semanticId=semantic;this.time=time;updateTime=updated;this.saved=saved;this.errorType=errorType;
        }
    }

    /**
     * k.e has no zero gate. Its caller decides whether a zero change is meaningful.
     * time/updateTime are two clock calls, not the related Bill's timestamp.
     * LitePalSupport.save catches Exception and returns false; k.e ignores that
     * boolean. Expose it without preventing the caller's subsequent effects.
     */
    public SavedHistory appendOperation(int owner,long assetSemantic,double change,double current,String content) {
        synchronized(store) {
            long time=clock.getAsLong(),updated=clock.getAsLong();
            int semantic;
            do{semantic=random.nextInt(Integer.MAX_VALUE);}while(byIdentity(owner,semantic)!=null);
            ContentValues value=new ContentValues();
            value.put("semantic_id",semantic);value.put("asset_semantic_id",assetSemantic);value.put("owner_semantic_id",owner);
            value.put("time",time);value.put("update_time",updated);value.put("change_content",content);
            value.put("change_number",change);value.put("current_number",current);
            try {
                long row=store.getWritableDatabase().insertOrThrow(TABLE,null,value);
                // SaveHandler.afterSave calls throwIfSaveFailed(-1), even when a
                // SQLite IGNORE path returns normally without an exception.
                if(row==-1)throw new IllegalStateException("账户历史未插入");
                return new SavedHistory(row,owner,semantic,time,updated,true,"");
            } catch(Exception error) {
                // Privacy adaptation: preserve save=false without the original stack trace.
                return new SavedHistory(0,owner,semantic,time,updated,false,error.getClass().getName());
            }
        }
    }

    public JSONObject byIdentity(int owner,long semantic) {
        return store.one("SELECT * FROM asset_histories WHERE owner_semantic_id=? AND semantic_id=? LIMIT 1",""+owner,""+semantic);
    }
    public List<JSONObject> forAsset(int owner,long assetSemantic) {
        return store.query("SELECT * FROM asset_histories WHERE owner_semantic_id=? AND asset_semantic_id=? ORDER BY time DESC",""+owner,""+assetSemantic);
    }
    public List<JSONObject> atOrAfter(int owner,long assetSemantic,long time) {
        return store.query("SELECT * FROM asset_histories WHERE owner_semantic_id=? AND time>=? AND asset_semantic_id=? ORDER BY time ASC",""+owner,""+time,""+assetSemantic);
    }
    public JSONObject before(int owner,long assetSemantic,long time) {
        return store.one("SELECT * FROM asset_histories WHERE owner_semantic_id=? AND time<? AND asset_semantic_id=? ORDER BY time DESC LIMIT 1",""+owner,""+time,""+assetSemantic);
    }
    public List<JSONObject> updatedAfter(int owner,long updateTime) {
        return store.query("SELECT * FROM asset_histories WHERE owner_semantic_id=? AND update_time>?",""+owner,""+updateTime);
    }
    public long count(int owner) { return store.scalar("SELECT COUNT(*) n FROM asset_histories WHERE owner_semantic_id=?",""+owner); }
    public long maxUpdated(int owner) { return store.scalar("SELECT COALESCE(MAX(update_time),0) n FROM asset_histories WHERE owner_semantic_id=?",""+owner); }

    /** History deletion never changes an account's stored balance. */
    public int deleteHistory(int owner,long semantic) {
        synchronized(store){return store.getWritableDatabase().delete(TABLE,"owner_semantic_id=? AND semantic_id=?",new String[]{""+owner,""+semantic});}
    }
    public int deleteForAsset(int owner,long assetSemantic) {
        synchronized(store){return store.getWritableDatabase().delete(TABLE,"owner_semantic_id=? AND asset_semantic_id=?",new String[]{""+owner,""+assetSemantic});}
    }

    /** Strict backup validation is an offline safety boundary, not an original DAO gate. */
    public void validateRestored() {
        synchronized(store) {
            for(JSONObject row:store.query("SELECT * FROM asset_histories")) {
                if(!integer(row,"id",1,Long.MAX_VALUE)||!integer(row,"semantic_id",0,Integer.MAX_VALUE-1L)
                        ||!integer(row,"asset_semantic_id",Long.MIN_VALUE,Long.MAX_VALUE)
                        ||!integer(row,"owner_semantic_id",Integer.MIN_VALUE,Integer.MAX_VALUE)
                        ||!integer(row,"time",Long.MIN_VALUE,Long.MAX_VALUE)||!integer(row,"update_time",Long.MIN_VALUE,Long.MAX_VALUE))
                    throw new IllegalArgumentException("备份账户历史标识或时间无效");
                for(String key:new String[]{"change_number","current_number"})
                    if(!(row.opt(key) instanceof Number)||!Double.isFinite(((Number)row.opt(key)).doubleValue()))throw new IllegalArgumentException("备份账户历史金额无效");
                if(!row.isNull("change_content")&&!(row.opt("change_content") instanceof String))throw new IllegalArgumentException("备份账户历史说明无效");
            }
        }
    }
    private static boolean integer(JSONObject row,String key,long min,long max) {
        Object value=row.opt(key);
        return (value instanceof Integer||value instanceof Long)&&((Number)value).longValue()>=min&&((Number)value).longValue()<=max;
    }
}
