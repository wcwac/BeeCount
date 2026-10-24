package com.wangc.bill.reference.capture;

import com.wangc.bill.reference.AssetRepository;
import com.wangc.bill.reference.Json;
import com.wangc.bill.reference.ReferenceApplication;
import com.wangc.bill.reference.ReferenceStore;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/** Explicit app/payment-method mappings. Local row IDs are never original semantic asset IDs. */
public final class CaptureAccountMappings {
    private static final String PREF="capture_account_mappings";
    private CaptureAccountMappings() {}
    private static String key(String pkg,String asset) { return pkg+"\n"+CaptureIdentity.normalize(asset); }
    public static Long alias(String pkg,String asset) {
        JSONObject mappings;
        try {mappings=Json.parse(ReferenceApplication.prefs().getString(PREF,"{}"));}
        catch(RuntimeException invalidSetting){return -1L;} // Preserve the draft; never guess a balance target.
        String key=key(pkg,asset);
        if(!mappings.has(key)||mappings.isNull(key))return null;
        Object value=mappings.opt(key);
        if(!(value instanceof Number))return -1L;
        double raw=((Number)value).doubleValue();long id=((Number)value).longValue();
        return raw>=0 && raw==id?id:-1L;
    }
    public static void put(String pkg,String asset,long rowId) {
        if(pkg==null||pkg.isEmpty()||CaptureIdentity.normalize(asset).isEmpty()||rowId<0)
            throw new IllegalArgumentException("请选择来源应用、付款方式和账户");
        JSONObject mappings=Json.parse(ReferenceApplication.prefs().getString(PREF,"{}"));
        Json.put(mappings,key(pkg,asset),rowId);
        ReferenceApplication.prefs().edit().putString(PREF,mappings.toString()).apply();
    }
    public static void remove(String pkg,String asset) {
        JSONObject mappings=Json.parse(ReferenceApplication.prefs().getString(PREF,"{}"));mappings.remove(key(pkg,asset));
        ReferenceApplication.prefs().edit().putString(PREF,mappings.toString()).apply();
    }
    /** No balance getter, SUM or credit projection is needed to identify eligible local accounts. */
    public static List<JSONObject> choices(ReferenceStore store,long bookRowId) {
        List<JSONObject> rows=new ArrayList<>();JSONObject book=store.book(bookRowId);
        if(book!=null&&!book.isNull("semantic_id"))rows.addAll(store.assetRepository().picker(
                ReferenceStore.LOCAL_OFFLINE_OWNER,book.optLong("semantic_id")));
        rows.addAll(store.assetRepository().legacyPicker());
        return rows;
    }
    public static boolean eligible(JSONObject account) {
        // The production ordinary-bill path requires an original local-owner asset with raw
        // currency absent. Display CNY is not evidence that FX metadata is implemented.
        return account!=null && account.optLong("id")>0 && account.optInt("archived")==0
                && AssetRepository.ORIGINAL.equals(account.optString("asset_origin"))
                && !account.isNull("semantic_id") && !account.isNull("owner_semantic_id")
                && account.optInt("owner_semantic_id")==ReferenceStore.LOCAL_OFFLINE_OWNER
                && (account.isNull("asset_currency")||account.optString("asset_currency").isEmpty())
                && account.optInt("asset_type")!=9;
    }
    public static CapturedAccountResolver.Resolution resolve(ReferenceStore store,long book,String pkg,String asset,Long merchantRule) {
        return resolve(store,book,pkg,asset,merchantRule,true);
    }
    public static CapturedAccountResolver.Resolution resolve(ReferenceStore store,long book,String pkg,String asset,Long merchantRule,boolean useDefault) {
        ArrayList<CapturedAccountResolver.Account> local=new ArrayList<>();
        for(JSONObject row:choices(store,book))local.add(new CapturedAccountResolver.Account(
                row.optLong("id"),row.optString("name"),eligible(row)));
        Long defaultId=null;
        if(useDefault&&ReferenceApplication.prefs().contains("default_account")) {
            try{defaultId=ReferenceApplication.prefs().getLong("default_account",0);}
            catch(ClassCastException invalidSetting){defaultId=-1L;}
        }
        return CapturedAccountResolver.resolve(asset,alias(pkg,asset),merchantRule,defaultId,local);
    }
}
