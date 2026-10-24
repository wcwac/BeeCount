package com.wangc.bill.reference;

import android.content.ContentValues;
import java.math.BigDecimal;
import org.json.JSONObject;

/** Local account form operations commit balance, history and metadata together. */
public final class AccountingAccounts {
    private AccountingAccounts() {}
    public static long save(ReferenceStore store,long rowId,String rawName,int type,long minor,String outDay,String inDay) {
        String name=rawName==null?"":rawName.trim();
        if(name.isEmpty()||name.length()>100||Math.abs((double)minor)>99999999999999L||!(type==1||type==2))
            throw new IllegalArgumentException("请输入有效的账户名称、类型和余额");
        if(type==2){day(outDay);day(inDay);}
        double number=BigDecimal.valueOf(minor,2).doubleValue();
        return AccountingCommitScope.run(store,()->{
            JSONObject previous=rowId==0?null:store.assetRepository().byRowId(rowId);
            if(rowId!=0&&(previous==null||!AssetRepository.ORIGINAL.equals(previous.optString("asset_origin"))
                    ||previous.optInt("owner_semantic_id")!=ReferenceStore.LOCAL_OFFLINE_OWNER||previous.optInt("archived")!=0
                    ||previous.optInt("asset_type")!=type||(!previous.isNull("asset_currency")&&!previous.optString("asset_currency").isEmpty())))
                throw new IllegalArgumentException("请选择可用的本币资金或信用账户；已有账户不能改变类型");
            int owner=ReferenceStore.LOCAL_OFFLINE_OWNER;long id,semantic;
            double delta;
            if(previous==null){
                AssetRepository.Definition definition=new AssetRepository.Definition(name,type,null);
                definition.assetNumber=number;definition.intoTotalAsset=true;
                if(type==2){definition.outAccountDate=outDay;definition.inAccountDate=inDay;}
                id=store.assetRepository().createOriginal(owner,definition);
                JSONObject created=store.assetRepository().byRowId(id);semantic=created.optLong("semantic_id");delta=number;
            }else{
                id=rowId;semantic=previous.optLong("semantic_id");delta=number-previous.optDouble("asset_number");
                ContentValues fields=new ContentValues();fields.put("name",name);fields.put("asset_number",number);
                fields.put("update_time",System.currentTimeMillis());
                if(type==2){fields.put("out_account_date",outDay);fields.put("in_account_date",inDay);}
                AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("accounts",fields,"id=?",new String[]{""+id}));
            }
            if(delta!=0)new AssetHistoryRepository(store).appendOperation(owner,semantic,delta,number,previous==null?"资产账户创建":"资产账户编辑");
            OriginalFinancialEffects effects=new OriginalFinancialEffects(store,owner,owner);
            effects.assetCreatedOrEdited(semantic);
            if(type==2)effects.creditRefresh(semantic);
            return id;
        });
    }
    private static void day(String value){
        try{int day=Integer.parseInt(value);if(day<1||day>31)throw new NumberFormatException();}
        catch(RuntimeException invalid){throw new IllegalArgumentException("账单日和还款日需要在1至31之间");}
    }
}
