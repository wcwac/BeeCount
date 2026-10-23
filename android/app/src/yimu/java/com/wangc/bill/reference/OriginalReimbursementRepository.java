package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.json.JSONObject;
import static com.wangc.bill.reference.OriginalRefundRepository.*;

/**
 * Recovered Reimbursement metadata and raw colon strings, separate from Refund JSON.
 * b2.g/G/r/u/t/w/v/C establish the storage/query scopes. No Bill cost or account
 * mutation is implied by this storage API; generalInfo's edit writer is not guessed.
 */
public final class OriginalReimbursementRepository {
    private final ReferenceStore store;
    private final Random random;

    public OriginalReimbursementRepository(ReferenceStore store) {this(store,new Random());}
    OriginalReimbursementRepository(ReferenceStore store,Random random) {
        if(store==null||random==null)throw new IllegalArgumentException("报销存储及标识生成器不能为空");
        this.store=store;this.random=random;
    }
    /** Four statements from the explicit port schema, not copied LitePal-generated DDL. */
    public static void installV4Schema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE original_reimbursements(id INTEGER PRIMARY KEY AUTOINCREMENT,owner_semantic_id INTEGER NOT NULL,reimbursement_semantic_id INTEGER NOT NULL,parent_bill_semantic_id INTEGER NOT NULL,parent_transaction_row_id INTEGER REFERENCES transactions(id) ON DELETE SET NULL,asset_semantic_id INTEGER NOT NULL DEFAULT 0,reimbursement_asset_semantic_id INTEGER NOT NULL DEFAULT 0,reimbursement_num REAL NOT NULL DEFAULT 0,end INTEGER NOT NULL DEFAULT 0,create_time INTEGER NOT NULL DEFAULT 0,update_time INTEGER NOT NULL DEFAULT 0,original_delete_flag INTEGER NOT NULL DEFAULT 0,entries_is_null INTEGER NOT NULL DEFAULT 1,UNIQUE(owner_semantic_id,reimbursement_semantic_id))");
        db.execSQL("CREATE INDEX original_reimbursements_parent ON original_reimbursements(owner_semantic_id,parent_bill_semantic_id)");
        db.execSQL("CREATE INDEX original_reimbursements_asset ON original_reimbursements(owner_semantic_id,asset_semantic_id,end)");
        db.execSQL("CREATE TABLE original_reimbursement_items(reimbursement_row_id INTEGER NOT NULL REFERENCES original_reimbursements(id) ON DELETE CASCADE,position INTEGER NOT NULL,raw_info TEXT NOT NULL,PRIMARY KEY(reimbursement_row_id,position))");
    }

    public static final class Data {
        public int parentBillSemanticId;
        public Long parentTransactionRowId;
        public long assetSemanticId,reimbursementAssetSemanticId,createTime;
        public double reimbursementNumber;
        public boolean end,originalDeleted;
        public List<String> rawEntries;
        public Data copy() {
            Data value=new Data();value.parentBillSemanticId=parentBillSemanticId;value.parentTransactionRowId=parentTransactionRowId;
            value.assetSemanticId=assetSemanticId;value.reimbursementAssetSemanticId=reimbursementAssetSemanticId;
            value.createTime=createTime;value.reimbursementNumber=reimbursementNumber;value.end=end;value.originalDeleted=originalDeleted;
            value.rawEntries=copyEntries(rawEntries);return value;
        }
        public void appendRaw(String raw) {if(raw==null)invalid();if(rawEntries==null)rawEntries=new ArrayList<>();rawEntries.add(raw);}
        public boolean removeFirstRaw(String raw) {return rawEntries!=null&&rawEntries.remove(raw);}
        /** Reimbursement.add/reduce initializes even a previously null raw list. */
        public void addNumber(double amount) {reimbursementNumber+=amount;if(rawEntries==null)rawEntries=new ArrayList<>();}
        public void reduceNumber(double amount) {reimbursementNumber-=amount;if(rawEntries==null)rawEntries=new ArrayList<>();}
    }
    public static final class Snapshot {
        public final long rowId,reimbursementSemanticId,updateTime;
        public final int ownerSemanticId;
        public final Data data;
        private Snapshot(long row,int owner,long semantic,long now,Data data) {
            rowId=row;ownerSemanticId=owner;reimbursementSemanticId=semantic;updateTime=now;this.data=data.copy();
        }
    }
    public static final class SaveResult {
        public final boolean saved;
        public final Snapshot record;
        public final String failureType;
        private SaveResult(boolean saved,Snapshot record,Exception error) {
            this.saved=saved;this.record=record;failureType=error==null?"":error.getClass().getName();
        }
    }

    /** b2.g always allocates a new semantic ID and resets end/createTime for creation. */
    public Snapshot create(int owner,Data input,long now) {
        synchronized(store) {Data data=creation(input,now);return insert(owner,nextSemantic(owner),data,now);}
    }
    /** Explicit recovered data preserves end/createTime and long semantic identity verbatim. */
    public Snapshot insertRecovered(int owner,long semantic,Data input,long updateTime) {
        synchronized(store) {return insert(owner,semantic,checked(input),updateTime);}
    }
    public Snapshot update(long row,int owner,Data input,long now) {
        synchronized(store) {
            Snapshot previous=byRowId(row,owner);if(previous==null)throw new IllegalArgumentException("报销记录不存在");
            return update(previous,checked(input),now);
        }
    }
    public SaveResult tryCreate(int owner,Data input,long now) {
        synchronized(store) {
            Data data=creation(input,now);long semantic=nextSemantic(owner);long[] assignedRow={0};
            try{return new SaveResult(true,insert(owner,semantic,data,now,assignedRow),null);}
            catch(Exception error){return new SaveResult(false,new Snapshot(assignedRow[0],owner,semantic,now,data),error);}
        }
    }
    public SaveResult tryUpdate(Snapshot previous,Data input,long now) {
        synchronized(store) {
            if(previous==null||previous.rowId<=0)throw new IllegalArgumentException("报销本地主键无效");
            Data data=checked(input);Snapshot proposed=new Snapshot(previous.rowId,previous.ownerSemanticId,previous.reimbursementSemanticId,now,data);
            try{return new SaveResult(true,update(previous,data,now),null);}
            catch(Exception error){return new SaveResult(false,proposed,error);}
        }
    }
    public SaveResult tryUpdate(long row,int owner,Data input,long now) {
        synchronized(store) {
            Snapshot previous=byRowId(row,owner);if(previous==null)throw new IllegalArgumentException("报销记录不存在");
            return tryUpdate(previous,input,now);
        }
    }

    public Snapshot byRowId(long row,int owner) {
        synchronized(store) {return read(store.one("SELECT * FROM original_reimbursements WHERE id=? AND owner_semantic_id=?",""+row,""+owner));}
    }
    public Snapshot byIdentity(int owner,long semantic) {
        synchronized(store) {return read(store.one("SELECT * FROM original_reimbursements WHERE owner_semantic_id=? AND reimbursement_semantic_id=?",""+owner,""+semantic));}
    }
    public Snapshot firstByParent(int owner,int bill) {
        synchronized(store) {return read(store.one("SELECT * FROM original_reimbursements WHERE owner_semantic_id=? AND parent_bill_semantic_id=? ORDER BY id LIMIT 1",""+owner,""+bill));}
    }
    public List<Snapshot> allByParent(int owner,int bill) {
        synchronized(store) {return readAll(store.query("SELECT * FROM original_reimbursements WHERE owner_semantic_id=? AND parent_bill_semantic_id=? ORDER BY id",""+owner,""+bill));}
    }
    public List<Snapshot> allForOwner(int owner) {
        synchronized(store) {return readAll(store.query("SELECT * FROM original_reimbursements WHERE owner_semantic_id=? ORDER BY id",""+owner));}
    }
    /** b2.t orders descending; b2.w's open-only set is the same owner/asset predicate. */
    public List<Snapshot> forAsset(int owner,long asset,Boolean ended) {
        synchronized(store) {
            if(ended==null)return readAll(store.query("SELECT * FROM original_reimbursements WHERE owner_semantic_id=? AND asset_semantic_id=? ORDER BY id DESC",""+owner,""+asset));
            return readAll(store.query("SELECT * FROM original_reimbursements WHERE owner_semantic_id=? AND asset_semantic_id=? AND end=? ORDER BY id DESC",""+owner,""+asset,ended?"1":"0"));
        }
    }
    /** b2.v deliberately has no user predicate; ordinary UI lists must use allForOwner. */
    public List<Snapshot> endedAllOwners() {
        synchronized(store) {return readAll(store.query("SELECT * FROM original_reimbursements WHERE end=1 ORDER BY id"));}
    }
    public List<Snapshot> rawAssetLike(int owner,long asset) {
        synchronized(store) {return readAll(store.query("SELECT * FROM original_reimbursements WHERE owner_semantic_id=? AND id IN (SELECT reimbursement_row_id FROM original_reimbursement_items WHERE raw_info LIKE ?) ORDER BY id",""+owner,asset+":%"));}
    }
    /** Explicit aggregate deletion, not the financial reversal performed by b2.k. */
    public boolean delete(long row,int owner) {
        synchronized(store) {return store.getWritableDatabase().delete("original_reimbursements","id=? AND owner_semantic_id=?",new String[]{""+row,""+owner})>0;}
    }

    private long nextSemantic(int owner) {
        int semantic;do{semantic=random.nextInt(Integer.MAX_VALUE);}while(byIdentity(owner,semantic)!=null);return semantic;
    }
    private Data creation(Data input,long now) {Data data=checked(input);data.end=false;data.createTime=now;return data;}
    private Data checked(Data input) {
        if(input==null)invalid();Data data=input.copy();if(!Double.isFinite(data.reimbursementNumber))invalid();
        checkParent(store,data.parentTransactionRowId,data.parentBillSemanticId);return data;
    }
    private Snapshot insert(int owner,long semantic,Data data,long now) {
        return insert(owner,semantic,data,now,null);
    }
    private Snapshot insert(int owner,long semantic,Data data,long now,long[] assignedRow) {
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();
        try {
            long row=db.insertOrThrow("original_reimbursements",null,values(owner,semantic,data,now));
            if(row==-1)throw new IllegalStateException("报销记录未插入");
            if(assignedRow!=null)assignedRow[0]=row;
            replaceEntries(db,row,data.rawEntries);
            Snapshot result=new Snapshot(row,owner,semantic,now,data);db.setTransactionSuccessful();return result;
        }finally{db.endTransaction();}
    }
    private Snapshot update(Snapshot previous,Data data,long now) {
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();
        try {
            checkExistingIdentity(db,"original_reimbursements","reimbursement_semantic_id",previous.rowId,previous.ownerSemanticId,previous.reimbursementSemanticId);
            ContentValues changes=values(previous.ownerSemanticId,previous.reimbursementSemanticId,data,now);
            if(data.rawEntries==null)changes.remove("entries_is_null");
            db.update("original_reimbursements",changes,
                "id=? AND owner_semantic_id=? AND reimbursement_semantic_id=?",new String[]{""+previous.rowId,""+previous.ownerSemanticId,""+previous.reimbursementSemanticId});
            if(data.rawEntries!=null)replaceEntries(db,previous.rowId,data.rawEntries);
            Snapshot result=new Snapshot(previous.rowId,previous.ownerSemanticId,previous.reimbursementSemanticId,now,data);
            db.setTransactionSuccessful();return result;
        }finally{db.endTransaction();}
    }
    private static ContentValues values(int owner,long semantic,Data data,long now) {
        ContentValues value=new ContentValues();value.put("owner_semantic_id",owner);value.put("reimbursement_semantic_id",semantic);
        value.put("parent_bill_semantic_id",data.parentBillSemanticId);value.put("parent_transaction_row_id",data.parentTransactionRowId);
        value.put("asset_semantic_id",data.assetSemanticId);value.put("reimbursement_asset_semantic_id",data.reimbursementAssetSemanticId);
        value.put("reimbursement_num",data.reimbursementNumber);value.put("end",data.end?1:0);value.put("create_time",data.createTime);
        value.put("update_time",now);value.put("original_delete_flag",data.originalDeleted?1:0);value.put("entries_is_null",data.rawEntries==null?1:0);return value;
    }
    private static void replaceEntries(SQLiteDatabase db,long row,List<String> entries) {
        db.delete("original_reimbursement_items","reimbursement_row_id=?",new String[]{""+row});if(entries==null)return;
        for(int i=0;i<entries.size();i++) {
            ContentValues value=new ContentValues();value.put("reimbursement_row_id",row);value.put("position",i);value.put("raw_info",entries.get(i));
            db.insertOrThrow("original_reimbursement_items",null,value);
        }
    }
    private Snapshot read(JSONObject row) {
        if(row==null)return null;Data data=new Data();data.parentBillSemanticId=row.optInt("parent_bill_semantic_id");
        if(!row.isNull("parent_transaction_row_id"))data.parentTransactionRowId=Long.valueOf(row.optLong("parent_transaction_row_id"));
        data.assetSemanticId=row.optLong("asset_semantic_id");data.reimbursementAssetSemanticId=row.optLong("reimbursement_asset_semantic_id");
        data.createTime=row.optLong("create_time");data.reimbursementNumber=row.optDouble("reimbursement_num");
        data.end=row.optInt("end")!=0;data.originalDeleted=row.optInt("original_delete_flag")!=0;
        if(row.optInt("entries_is_null")==0) {
            data.rawEntries=new ArrayList<>();
            for(JSONObject entry:store.query("SELECT raw_info FROM original_reimbursement_items WHERE reimbursement_row_id=? ORDER BY position",""+row.optLong("id")))data.rawEntries.add(entry.optString("raw_info"));
        }
        return new Snapshot(row.optLong("id"),row.optInt("owner_semantic_id"),row.optLong("reimbursement_semantic_id"),row.optLong("update_time"),data);
    }
    private List<Snapshot> readAll(List<JSONObject> rows) {List<Snapshot> result=new ArrayList<>();for(JSONObject row:rows)result.add(read(row));return result;}

    public void validateRestored() {
        synchronized(store) {
            try(Cursor rows=store.getReadableDatabase().rawQuery("SELECT * FROM original_reimbursements",null)) {
                while(rows.moveToNext()) {
                    long row=integer(rows,"id");if(row<=0)invalid();signedInt(integer(rows,"owner_semantic_id"));
                    integer(rows,"reimbursement_semantic_id");long parent=integer(rows,"parent_bill_semantic_id");signedInt(parent);
                    checkParent(store,nullablePositive(rows,"parent_transaction_row_id"),(int)parent);
                    integer(rows,"asset_semantic_id");integer(rows,"reimbursement_asset_semantic_id");finite(rows,"reimbursement_num");
                    flag(rows,"end");integer(rows,"create_time");integer(rows,"update_time");flag(rows,"original_delete_flag");
                    validatePositions(store,"original_reimbursement_items","reimbursement_row_id","raw_info",row,flag(rows,"entries_is_null"));
                }
            }
            checkItemParents(store,"original_reimbursement_items","reimbursement_row_id","original_reimbursements");
        }
    }
}
