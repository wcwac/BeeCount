package com.wangc.bill.reference;

import android.content.ContentValues;
import org.json.JSONObject;

/** Mutable loaded Asset objects, separate from metadata and the legacy ledger SUM. */
public final class OriginalAssetBalanceStore {
    @FunctionalInterface public interface DerivedBalanceProvider { double balance(int ownerSemanticId,long assetSemanticId); }
    @FunctionalInterface public interface SaveOutcomeObserver { void afterSave(LoadedAsset asset,SaveOutcome outcome); }
    public static final class SaveOutcome {
        public final boolean returned;
        public final String stage="ASSET_BALANCE_SAVE";
        /** A normal UPDATE with zero matching rows still returned true from original save(). */
        public final int affectedRows;
        public final String errorType;
        private SaveOutcome(boolean returned,int rows,String errorType){this.returned=returned;affectedRows=rows;this.errorType=errorType;}
    }

    private final ReferenceStore store;
    private final DerivedBalanceProvider derived;
    private final SaveOutcomeObserver observer;
    private SaveOutcome lastOutcome;
    public OriginalAssetBalanceStore(ReferenceStore store){this(store,null,null);}
    public OriginalAssetBalanceStore(ReferenceStore store,DerivedBalanceProvider derived){this(store,derived,null);}
    public OriginalAssetBalanceStore(ReferenceStore store,DerivedBalanceProvider derived,SaveOutcomeObserver observer){
        if(store==null)throw new IllegalArgumentException("账户数据库不能为空");
        this.store=store;this.derived=derived;this.observer=observer;
    }

    /** A fresh handle per lookup, including repeated lookups of the same semantic ID. */
    public LoadedAsset load(int owner,long semantic) {
        synchronized(store) {
            JSONObject row=store.assetRepository().p(semantic,owner);
            return row==null?null:new LoadedAsset(this,row);
        }
    }

    public static final class LoadedAsset {
        private final OriginalAssetBalanceStore source;
        public final long rowId,semanticId;
        public final int ownerSemanticId,assetType;
        private double storedNumber,reimbursementNumber=-1.0d;
        private long updateTime;
        private LoadedAsset(OriginalAssetBalanceStore source,JSONObject row){
            this.source=source;rowId=row.optLong("id");semanticId=row.optLong("semantic_id");ownerSemanticId=row.optInt("owner_semantic_id");assetType=row.optInt("asset_type");
            storedNumber=row.optDouble("asset_number");updateTime=row.optLong("update_time");
        }
        public double storedNumber(){synchronized(source.store){return storedNumber;}}
        public double getAssetNumber(){
            synchronized(source.store){
                if(assetType!=9)return storedNumber;
                if(reimbursementNumber!=-1.0d)return reimbursementNumber;
                if(source.derived==null)throw new IllegalStateException("报销账户需要原报销数据余额读取器");
                reimbursementNumber=source.derived.balance(ownerSemanticId,semanticId);return reimbursementNumber;
            }
        }
        /** Original Asset.addNumber/reduceNumber do not invalidate type9's ignored cache. */
        public void addNumber(double value){synchronized(source.store){storedNumber+=value;}}
        public void reduceNumber(double value){synchronized(source.store){storedNumber-=value;}}
        public void setStoredNumber(double value){synchronized(source.store){storedNumber=value;}}
        public long updateTime(){synchronized(source.store){return updateTime;}}
        public void setUpdateTime(long value){synchronized(source.store){updateTime=value;}}
    }

    /**
     * LitePalSupport.save catches Exception, not Error. SaveHandler.updating
     * ignores SQLite.update's row count. Only these two loaded fields are written;
     * metadata and histories belong to the surrounding financial command.
     * The owner/semantic/origin guards additionally prevent an obsolete local
     * row ID from overwriting a different identity after a database replacement;
     * these guards are an offline compatibility safeguard, not LitePal's WHERE.
     * Each SQL statement is atomic; no nested transaction poisons the caller's
     * transaction solely because it intentionally observes a normal save=false.
     */
    public boolean saveLoaded(LoadedAsset asset) {
        if(asset==null||asset.source!=this)throw new IllegalArgumentException("账户句柄不属于本余额存储");
        synchronized(store) {
            SaveOutcome outcome;
            try {
                ContentValues values=new ContentValues();values.put("asset_number",asset.storedNumber);values.put("update_time",asset.updateTime);
                int rows=store.getWritableDatabase().update("accounts",values,"id=? AND asset_origin='original' AND owner_semantic_id=? AND semantic_id=?",
                    new String[]{""+asset.rowId,""+asset.ownerSemanticId,""+asset.semanticId});
                outcome=new SaveOutcome(true,rows,"");
            }catch(Exception error){
                // Privacy adaptation: report the class, never print financial SQL/values.
                outcome=new SaveOutcome(false,-1,error.getClass().getName());
            }
            lastOutcome=outcome;
            if(observer!=null)try{observer.afterSave(asset,outcome);}catch(Exception ignored){/* Diagnostic observers cannot change save's boolean. */}
            return outcome.returned;
        }
    }
    public SaveOutcome lastOutcome(){synchronized(store){return lastOutcome;}}
}
