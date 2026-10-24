package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.json.JSONObject;

/**
 * Original Refund header plus its ordered List<String>. This repository changes no
 * Bill cost, balance, history, parser cache or legacy related_id transaction.
 * Evidence: a2.g/m/u/v/D/K and Refund.addRefundInfo; private refund-storage-port-plan.
 */
public final class OriginalRefundRepository {
    private final ReferenceStore store;
    private final Random random;

    public OriginalRefundRepository(ReferenceStore store) { this(store,new Random()); }
    OriginalRefundRepository(ReferenceStore store,Random random) {
        if(store==null||random==null)throw new IllegalArgumentException("退款存储及标识生成器不能为空");
        this.store=store;this.random=random;
    }

    /** Called once by SQLiteOpenHelper's create/upgrade transaction; no recursive DB opening. */
    public static void installV4Schema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE original_refunds(id INTEGER PRIMARY KEY AUTOINCREMENT,owner_semantic_id INTEGER NOT NULL,refund_semantic_id INTEGER NOT NULL,parent_bill_semantic_id INTEGER NOT NULL,parent_transaction_row_id INTEGER REFERENCES transactions(id) ON DELETE SET NULL,refund_num REAL NOT NULL DEFAULT 0,update_time INTEGER NOT NULL DEFAULT 0,original_delete_flag INTEGER NOT NULL DEFAULT 0,infos_is_null INTEGER NOT NULL DEFAULT 1,UNIQUE(owner_semantic_id,refund_semantic_id))");
        db.execSQL("CREATE INDEX original_refunds_parent ON original_refunds(owner_semantic_id,parent_bill_semantic_id)");
        db.execSQL("CREATE TABLE original_refund_items(refund_row_id INTEGER NOT NULL REFERENCES original_refunds(id) ON DELETE CASCADE,position INTEGER NOT NULL,raw_json TEXT NOT NULL,PRIMARY KEY(refund_row_id,position))");
    }

    public static final class Data {
        public int parentBillSemanticId;
        public Long parentTransactionRowId;
        public double refundNumber;
        public boolean originalDeleted;
        /** null and empty are different; invalid/unrecognized JSON still remains exact raw data. */
        public List<String> rawInfos;

        public Data copy() {
            Data value=new Data();value.parentBillSemanticId=parentBillSemanticId;
            value.parentTransactionRowId=parentTransactionRowId;value.refundNumber=refundNumber;
            value.originalDeleted=originalDeleted;value.rawInfos=copyEntries(rawInfos);return value;
        }
        /** Original Refund.addRefundInfo prepends and initializes a null list. */
        public void prependRaw(String raw) {
            if(raw==null)invalid();if(rawInfos==null)rawInfos=new ArrayList<>();rawInfos.add(0,raw);
        }
        /** Detail's remove(Object) removes only the first exactly equal stored string. */
        public boolean removeFirstRaw(String raw) { return rawInfos!=null&&rawInfos.remove(raw); }
    }

    public static final class Snapshot {
        public final long rowId,refundSemanticId,updateTime;
        public final int ownerSemanticId;
        public final Data data;
        private Snapshot(long row,int owner,long semantic,long updated,Data data) {
            rowId=row;ownerSemanticId=owner;refundSemanticId=semantic;updateTime=updated;this.data=data.copy();
        }
    }
    public static final class SaveResult {
        public final boolean saved;
        /** A late generic-list failure retains the assigned PK even though SQL rolls back. */
        public final Snapshot record;
        public final String failureType;
        private SaveResult(boolean saved,Snapshot record,Exception error) {
            this.saved=saved;this.record=record;failureType=error==null?"":error.getClass().getName();
        }
    }

    public Snapshot create(int owner,Data input,long now) {
        synchronized(store) {
            Data data=checked(input);long semantic=nextSemantic(owner);
            return insert(owner,semantic,data,now);
        }
    }
    /** Imported/recovered model IDs are long, unlike the int-sized fresh allocator. */
    public Snapshot insertRecovered(int owner,long semantic,Data input,long updateTime) {
        synchronized(store) { return insert(owner,semantic,checked(input),updateTime); }
    }
    public Snapshot update(long rowId,int owner,Data input,long now) {
        synchronized(store) {
            Snapshot previous=byRowId(rowId,owner);if(previous==null)throw new IllegalArgumentException("退款记录不存在");
            return update(previous,checked(input),now);
        }
    }
    /**
     * Allocator/preflight are outside entity save's catch, just as a2.m precedes save().
     * Only the single storage operation rolls back on Exception. No Error is swallowed.
     */
    public SaveResult tryCreate(int owner,Data input,long now) {
        synchronized(store) {
            Data data=checked(input);long semantic=nextSemantic(owner);
            long[] assignedRow={0};
            try {return new SaveResult(true,insert(owner,semantic,data,now,assignedRow),null);}
            catch(Exception error){ AccountingCommitScope.failed(store,error);return new SaveResult(false,new Snapshot(assignedRow[0],owner,semantic,now,data),error);}
        }
    }
    /**
     * Use the retained model, not a fresh lookup: LitePal.save on a saved-but-deleted
     * header ignores UPDATE's affected-row count. Generic-entry FK failures still fail save.
     */
    public SaveResult tryUpdate(Snapshot previous,Data input,long now) {
        synchronized(store) {
            if(previous==null||previous.rowId<=0)throw new IllegalArgumentException("退款本地主键无效");
            Data data=checked(input);
            Snapshot proposed=new Snapshot(previous.rowId,previous.ownerSemanticId,previous.refundSemanticId,now,data);
            try {return new SaveResult(true,update(previous,data,now),null);}
            catch(Exception error){ AccountingCommitScope.failed(store,error);return new SaveResult(false,proposed,error);}
        }
    }
    public SaveResult tryUpdate(long rowId,int owner,Data input,long now) {
        synchronized(store) {
            Snapshot previous=byRowId(rowId,owner);if(previous==null)throw new IllegalArgumentException("退款记录不存在");
            return tryUpdate(previous,input,now);
        }
    }

    public Snapshot byRowId(long rowId,int owner) {
        synchronized(store) {return read(store.one("SELECT * FROM original_refunds WHERE id=? AND owner_semantic_id=?",""+rowId,""+owner));}
    }
    public Snapshot byIdentity(int owner,long semantic) {
        synchronized(store) {return read(store.one("SELECT * FROM original_refunds WHERE owner_semantic_id=? AND refund_semantic_id=?",""+owner,""+semantic));}
    }
    public Snapshot firstByParent(int owner,int parentBillSemanticId) {
        synchronized(store) {return read(store.one("SELECT * FROM original_refunds WHERE owner_semantic_id=? AND parent_bill_semantic_id=? ORDER BY id LIMIT 1",""+owner,""+parentBillSemanticId));}
    }
    public List<Snapshot> allByParent(int owner,int parentBillSemanticId) {
        synchronized(store) {return readAll(store.query("SELECT * FROM original_refunds WHERE owner_semantic_id=? AND parent_bill_semantic_id=? ORDER BY id",""+owner,""+parentBillSemanticId));}
    }
    public List<Snapshot> allForOwner(int owner) {
        synchronized(store) {return readAll(store.query("SELECT * FROM original_refunds WHERE owner_semantic_id=? ORDER BY id",""+owner));}
    }
    /** a2.D's literal LIKE deliberately retains whitespace sensitivity and numeric prefix matches. */
    public List<Snapshot> rawAssetLike(int owner,long rawAssetSemanticId) {
        synchronized(store) {return readAll(store.query("SELECT * FROM original_refunds WHERE owner_semantic_id=? AND id IN (SELECT refund_row_id FROM original_refund_items WHERE raw_json LIKE ?) ORDER BY id",""+owner,"%\"assetId\":"+rawAssetSemanticId+"%"));}
    }
    /** Storage-only physical delete. Financial reversals must be performed by the caller. */
    public boolean delete(long rowId,int owner) {
        synchronized(store) {return store.getWritableDatabase().delete("original_refunds","id=? AND owner_semantic_id=?",new String[]{""+rowId,""+owner})>0;}
    }

    private long nextSemantic(int owner) {
        int semantic;
        do {semantic=random.nextInt(Integer.MAX_VALUE);}while(byIdentity(owner,semantic)!=null);
        return semantic;
    }
    private Data checked(Data input) {
        if(input==null)invalid();Data data=input.copy();
        if(!Double.isFinite(data.refundNumber))invalid();
        checkParent(store,data.parentTransactionRowId,data.parentBillSemanticId);
        return data;
    }
    private Snapshot insert(int owner,long semantic,Data data,long now) {
        return insert(owner,semantic,data,now,null);
    }
    private Snapshot insert(int owner,long semantic,Data data,long now,long[] assignedRow) {
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();
        try {
            long row=db.insertOrThrow("original_refunds",null,values(owner,semantic,data,now));
            if(row==-1)throw new IllegalStateException("退款记录未插入");
            // SaveHandler.afterSave assigns the model's PK before writing its generic collection.
            if(assignedRow!=null)assignedRow[0]=row;
            replaceEntries(db,row,data.rawInfos);Snapshot result=new Snapshot(row,owner,semantic,now,data);
            db.setTransactionSuccessful();return result;
        }finally{db.endTransaction();}
    }
    private Snapshot update(Snapshot previous,Data data,long now) {
        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();
        try {
            checkExistingIdentity(db,"original_refunds","refund_semantic_id",previous.rowId,previous.ownerSemanticId,previous.refundSemanticId);
            // Ignore affected-row count like original SaveHandler.updating. Never reassign ownership.
            ContentValues changes=values(previous.ownerSemanticId,previous.refundSemanticId,data,now);
            if(data.rawInfos==null)changes.remove("infos_is_null");
            AccountingCommitScope.requireChanged(store,db.update("original_refunds",changes,
                "id=? AND owner_semantic_id=? AND refund_semantic_id=?",new String[]{""+previous.rowId,""+previous.ownerSemanticId,""+previous.refundSemanticId}));
            // Original updateGenericTables omits null collections; [] explicitly clears.
            if(data.rawInfos!=null)replaceEntries(db,previous.rowId,data.rawInfos);
            Snapshot result=new Snapshot(previous.rowId,previous.ownerSemanticId,previous.refundSemanticId,now,data);
            db.setTransactionSuccessful();return result;
        }finally{db.endTransaction();}
    }
    private static ContentValues values(int owner,long semantic,Data data,long now) {
        ContentValues values=new ContentValues();values.put("owner_semantic_id",owner);values.put("refund_semantic_id",semantic);
        values.put("parent_bill_semantic_id",data.parentBillSemanticId);values.put("parent_transaction_row_id",data.parentTransactionRowId);
        values.put("refund_num",data.refundNumber);values.put("update_time",now);values.put("original_delete_flag",data.originalDeleted?1:0);
        values.put("infos_is_null",data.rawInfos==null?1:0);return values;
    }
    private void replaceEntries(SQLiteDatabase db,long row,List<String> entries) {
        db.delete("original_refund_items","refund_row_id=?",new String[]{""+row});
        if(entries==null)return;
        for(int i=0;i<entries.size();i++) {
            ContentValues values=new ContentValues();values.put("refund_row_id",row);values.put("position",i);values.put("raw_json",entries.get(i));
            if(db.insertOrThrow("original_refund_items",null,values)==-1&&AccountingCommitScope.active(store))throw new IllegalStateException("关联明细未保存");
        }
    }
    private Snapshot read(JSONObject row) {
        if(row==null)return null;Data data=new Data();data.parentBillSemanticId=row.optInt("parent_bill_semantic_id");
        if(!row.isNull("parent_transaction_row_id"))data.parentTransactionRowId=Long.valueOf(row.optLong("parent_transaction_row_id"));
        data.refundNumber=row.optDouble("refund_num");data.originalDeleted=row.optInt("original_delete_flag")!=0;
        if(row.optInt("infos_is_null")==0) {
            data.rawInfos=new ArrayList<>();
            for(JSONObject item:store.query("SELECT raw_json FROM original_refund_items WHERE refund_row_id=? ORDER BY position",""+row.optLong("id")))data.rawInfos.add(item.optString("raw_json"));
        }
        return new Snapshot(row.optLong("id"),row.optInt("owner_semantic_id"),row.optLong("refund_semantic_id"),row.optLong("update_time"),data);
    }
    private List<Snapshot> readAll(List<JSONObject> rows) {List<Snapshot> result=new ArrayList<>();for(JSONObject row:rows)result.add(read(row));return result;}

    /** Strict restore metadata validation; opaque raw strings are intentionally never decoded here. */
    public void validateRestored() {
        synchronized(store) {
            try(Cursor rows=store.getReadableDatabase().rawQuery("SELECT * FROM original_refunds",null)) {
                while(rows.moveToNext()) {
                    long row=integer(rows,"id");if(row<=0)invalid();signedInt(integer(rows,"owner_semantic_id"));
                    integer(rows,"refund_semantic_id");long parent=integer(rows,"parent_bill_semantic_id");signedInt(parent);
                    Long bridge=nullablePositive(rows,"parent_transaction_row_id");checkParent(store,bridge,(int)parent);
                    finite(rows,"refund_num");integer(rows,"update_time");flag(rows,"original_delete_flag");
                    validatePositions(store,"original_refund_items","refund_row_id","raw_json",row,flag(rows,"infos_is_null"));
                }
            }
            checkItemParents(store,"original_refund_items","refund_row_id","original_refunds");
        }
    }

    static List<String> copyEntries(List<String> input) {
        if(input==null)return null;List<String> result=new ArrayList<>(input.size());
        for(String raw:input){if(raw==null)invalid();result.add(raw);}return result;
    }
    static void checkParent(ReferenceStore store,Long bridge,int rawParent) {
        if(bridge==null)return;if(bridge<=0)invalid();
        JSONObject parent=store.one("SELECT bill_origin,bill_semantic_id FROM transactions WHERE id=?",""+bridge);
        if(parent==null||!"original".equals(parent.optString("bill_origin"))||parent.isNull("bill_semantic_id")
                ||parent.optLong("bill_semantic_id")!=rawParent)throw new IllegalArgumentException("退款或报销的原账单标识与本地主键不匹配");
        // Aggregate.userId is the current user; parent Bill.userId may be the selected book's owner.
    }
    static long integer(Cursor rows,String column) {
        int i=rows.getColumnIndexOrThrow(column);if(rows.getType(i)!=Cursor.FIELD_TYPE_INTEGER)invalid();return rows.getLong(i);
    }
    static void signedInt(long value) {if(value<Integer.MIN_VALUE||value>Integer.MAX_VALUE)invalid();}
    static Long nullablePositive(Cursor rows,String column) {
        int i=rows.getColumnIndexOrThrow(column);if(rows.isNull(i))return null;long value=integer(rows,column);if(value<=0)invalid();return Long.valueOf(value);
    }
    static boolean flag(Cursor rows,String column) {long value=integer(rows,column);if(value!=0&&value!=1)invalid();return value!=0;}
    static void finite(Cursor rows,String column) {
        int i=rows.getColumnIndexOrThrow(column),type=rows.getType(i);
        if((type!=Cursor.FIELD_TYPE_INTEGER&&type!=Cursor.FIELD_TYPE_FLOAT)||!Double.isFinite(rows.getDouble(i)))invalid();
    }
    static void validatePositions(ReferenceStore store,String table,String parentColumn,String rawColumn,long row,boolean nullList) {
        try(Cursor entries=store.getReadableDatabase().rawQuery("SELECT position,"+rawColumn+" FROM "+table+" WHERE "+parentColumn+"=? ORDER BY position",new String[]{""+row})) {
            long position=0;
            while(entries.moveToNext()) {
                if(nullList||integer(entries,"position")!=position++||entries.getType(1)!=Cursor.FIELD_TYPE_STRING)invalid();
            }
        }
    }
    static void checkItemParents(ReferenceStore store,String items,String fk,String parents) {
        if(store.one("SELECT i."+fk+" FROM "+items+" i LEFT JOIN "+parents+" p ON p.id=i."+fk+" WHERE p.id IS NULL LIMIT 1")!=null)invalid();
    }
    static void checkExistingIdentity(SQLiteDatabase db,String table,String semanticColumn,long row,int owner,long semantic) {
        try(Cursor current=db.rawQuery("SELECT owner_semantic_id,"+semanticColumn+" FROM "+table+" WHERE id=?",new String[]{""+row})) {
            // A replaced DB may reuse a row ID. A stale model must not replace another owner's entries.
            if(current.moveToFirst()&&(current.getLong(0)!=owner||current.getLong(1)!=semantic))invalid();
        }
    }
    static void invalid() {throw new IllegalArgumentException("退款或报销元数据无效");}
}
