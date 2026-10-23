package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;
import org.json.JSONObject;

/** q0/f.n local sync bookkeeping. It never changes balances or AssetHistory. */
public final class OriginalCurdHistoryRepository {
    public static final String TABLE="original_curd_histories";
    private final ReferenceStore store;
    public OriginalCurdHistoryRepository(ReferenceStore store){if(store==null)throw new IllegalArgumentException("Store is required");this.store=store;}

    public static void installV5Schema(SQLiteDatabase db){
        db.execSQL("CREATE TABLE original_curd_histories(id INTEGER PRIMARY KEY AUTOINCREMENT,owner_semantic_id INTEGER NOT NULL DEFAULT 0,entity_type INTEGER NOT NULL DEFAULT 0,entity_semantic_id INTEGER NOT NULL DEFAULT 0,action_type INTEGER NOT NULL DEFAULT 0,bill_book_semantic_id INTEGER NOT NULL DEFAULT 0,bill_owner_semantic_id INTEGER NOT NULL DEFAULT 0)");
        // q0 implements lookup-before-save, not a SQLite uniqueness constraint.
        db.execSQL("CREATE INDEX original_curd_identity ON original_curd_histories(owner_semantic_id,entity_type,entity_semantic_id,action_type)");
    }
    public static final class Data {
        public int ownerSemanticId,type,typeId,actionType,billOwnerSemanticId;
        public long billBookSemanticId;
        public Data copy(){Data d=new Data();d.ownerSemanticId=ownerSemanticId;d.type=type;d.typeId=typeId;d.actionType=actionType;d.billOwnerSemanticId=billOwnerSemanticId;d.billBookSemanticId=billBookSemanticId;return d;}
    }
    public static final class SaveResult {
        public final boolean saved,alreadyPresent;
        public final long rowId;
        /** Class name only, never raw SQL or financial values. */
        public final String failureType;
        private SaveResult(boolean saved,boolean present,long row,String error){this.saved=saved;alreadyPresent=present;rowId=row;failureType=error;}
    }
    /** f.n checks the newly constructed model's owner0 before q0.a assigns current owner. */
    public SaveResult recordAssetOperation(int currentOwner,long semanticAssetId){
        synchronized(store){
            Data value=new Data();value.type=6;value.typeId=(int)semanticAssetId;value.actionType=1;
            JSONObject old=exact(value);
            return old==null?add(currentOwner,value):new SaveResult(true,true,old.optLong("id"),"");
        }
    }
    /** q0.a: action0 removes conflicting nonzero actions before the independent save. */
    public SaveResult add(int currentOwner,Data supplied){
        if(supplied==null)throw new IllegalArgumentException("History is required");
        synchronized(store){
            Data value=supplied.copy();value.ownerSemanticId=currentOwner;
            SQLiteDatabase db=store.getWritableDatabase();
            if(value.actionType==0)db.delete(TABLE,"owner_semantic_id=? AND entity_type=? AND entity_semantic_id=? AND action_type<>0",new String[]{""+currentOwner,""+value.type,""+value.typeId});
            JSONObject old=exact(value);if(old!=null)return new SaveResult(true,true,old.optLong("id"),"");
            ContentValues fields=new ContentValues();fields.put("owner_semantic_id",value.ownerSemanticId);fields.put("entity_type",value.type);fields.put("entity_semantic_id",value.typeId);fields.put("action_type",value.actionType);fields.put("bill_book_semantic_id",value.billBookSemanticId);fields.put("bill_owner_semantic_id",value.billOwnerSemanticId);
            try{
                long row=db.insertOrThrow(TABLE,null,fields);
                return row==-1?new SaveResult(false,false,0,"java.lang.IllegalStateException"):new SaveResult(true,false,row,"");
            }catch(Exception error){return new SaveResult(false,false,0,error.getClass().getName());}
        }
    }
    public JSONObject exact(Data value){
        if(value==null)throw new IllegalArgumentException("History is required");
        synchronized(store){return store.one("SELECT * FROM "+TABLE+" WHERE owner_semantic_id=? AND entity_type=? AND entity_semantic_id=? AND action_type=? ORDER BY id LIMIT 1",""+value.ownerSemanticId,""+value.type,""+value.typeId,""+value.actionType);}
    }
    public List<JSONObject> forOwner(int owner){synchronized(store){return store.query("SELECT * FROM "+TABLE+" WHERE owner_semantic_id=? ORDER BY id",""+owner);}}
    /** q0.d does not narrow its long query argument before comparing the stored int. */
    public int deleteEntity(int owner,int type,long semanticId){synchronized(store){return store.getWritableDatabase().delete(TABLE,"owner_semantic_id=? AND entity_type=? AND entity_semantic_id=?",new String[]{""+owner,""+type,""+semanticId});}}
    public int deleteAction(int owner,int type,int semanticId,int action){synchronized(store){return store.getWritableDatabase().delete(TABLE,"owner_semantic_id=? AND entity_type=? AND entity_semantic_id=? AND action_type=?",new String[]{""+owner,""+type,""+semanticId,""+action});}}
    public int clearOwner(int owner){synchronized(store){return store.getWritableDatabase().delete(TABLE,"owner_semantic_id=?",new String[]{""+owner});}}
    public void validateRestored(){
        synchronized(store){try(Cursor c=store.getReadableDatabase().rawQuery("SELECT * FROM "+TABLE,null)){
            while(c.moveToNext()){
                if(OriginalRefundRepository.integer(c,"id")<=0)throw new IllegalArgumentException("Invalid CurdHistory local ID");
                for(String field:new String[]{"owner_semantic_id","entity_type","entity_semantic_id","action_type","bill_owner_semantic_id"})OriginalRefundRepository.signedInt(OriginalRefundRepository.integer(c,field));
                OriginalRefundRepository.integer(c,"bill_book_semantic_id");
            }
        }}
    }
}
