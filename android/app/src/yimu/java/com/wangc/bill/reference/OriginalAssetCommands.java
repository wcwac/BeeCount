package com.wangc.bill.reference;

import android.content.ContentValues;
import org.json.JSONObject;

/** First UI-backed original Asset command: the ordinary type-1 funds account. */
public final class OriginalAssetCommands {
    private final ReferenceStore store;
    private final int owner;
    public OriginalAssetCommands(ReferenceStore store,int owner) { this.store=store;this.owner=owner; }

    /** f.d: independent Asset.save followed by a nonzero initial history, not ledger opening+SUM. */
    public long createFund(String name,String currency,double number) {
        if(name==null||name.isEmpty()||!Double.isFinite(number))throw new IllegalArgumentException("账户名称或余额无效");
        synchronized(store) {
            independent();
            AssetRepository.Definition value=new AssetRepository.Definition(name,1,currency);
            value.assetNumber=number;value.intoTotalAsset=true;
            AssetRepository.CreationResult result=store.assetRepository().tryCreateOriginal(owner,value);
            if(number!=0.0d)new AssetHistoryRepository(store).appendOperation(owner,result.semanticId,number,number,"资产账户创建");
            JSONObject retained=Json.object("id",result.rowId,"semantic_id",result.semanticId,"owner_semantic_id",owner,
                    "name",name,"asset_type",1,"asset_currency",currency,"asset_icon",value.assetIcon,
                    "icon_night",value.iconNight,"into_total_asset",1,"asset_number",number);
            new OriginalFinancialEffects(store,owner,owner).assetCreatedOrEdited(result.semanticId,retained);
            if(!result.saved)throw new IllegalStateException("原版账户保存失败；已完成的历史记录会保留，请检查后处理");
            return result.rowId;
        }
    }

    /** q1's supplied old amount is the pre-edit getter value, never the legacy opening field. */
    public void editFund(long rowId,String name,String currency,double number,double previousGetter) {
        if(name==null||name.isEmpty()||!Double.isFinite(number)||!Double.isFinite(previousGetter))throw new IllegalArgumentException("账户名称或余额无效");
        synchronized(store) {
            independent();JSONObject old=store.assetRepository().byRowId(rowId);
            if(old==null||!AssetRepository.ORIGINAL.equals(old.optString("asset_origin"))||old.optInt("owner_semantic_id")!=owner||old.optInt("asset_type")!=1)
                throw new IllegalArgumentException("请选择当前身份的资金账户");
            ContentValues values=new ContentValues();values.put("name",name);
            // Ordinary LitePal model-save omits null references. Empty text still writes.
            if(currency!=null){values.put("currency",currency);values.put("asset_currency",currency);}
            values.put("asset_number",number);values.put("update_time",System.currentTimeMillis());
            Exception failure=null;
            try{store.getWritableDatabase().update("accounts",values,"id=?",new String[]{""+rowId});}
            catch(Exception error){failure=error;}
            double change=number-previousGetter;
            if(change!=0.0d)new AssetHistoryRepository(store).appendOperation(owner,old.optLong("semantic_id"),change,number,"资产账户编辑");
            JSONObject retained=Json.copy(old);Json.put(retained,"name",name);Json.put(retained,"asset_number",number);
            if(currency!=null)Json.put(retained,"asset_currency",currency);
            new OriginalFinancialEffects(store,owner,owner).assetCreatedOrEdited(old.optLong("semantic_id"),retained);
            if(failure!=null)throw new IllegalStateException("原版账户修改保存失败；已完成的历史记录会保留，请检查后处理");
        }
    }
    private void independent(){if((store.getWritableDatabase().inTransaction() && !AccountingCommitScope.active(store)))throw new IllegalStateException("Original Asset saves require independent stages");}
}
