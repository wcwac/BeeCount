package com.wangc.bill.reference;

import org.json.JSONObject;

/** Currency boundaries for locally scheduled drafts; payment capture keeps its CNY policy. */
public final class LocalPlanCandidates {
    private LocalPlanCandidates() {}
    public static boolean isLocalPlan(JSONObject candidate){return candidate!=null&&"local.plan".equals(candidate.optString("package_name"));}
    /** Stored minor units belong to this declaration, never to a newly selected book. */
    public static String currency(JSONObject candidate){
        Object value=Json.parse(candidate.optString("payload")).opt("currency");
        if(!(value instanceof String)||((String)value).isEmpty())throw new IllegalArgumentException("周期记录缺少原始币种，请保留记录并核对原规则");
        String currency=(String)value;Money.scale(currency);return currency;
    }
    public static boolean canUseBook(JSONObject candidate,JSONObject book){
        if(book==null||book.optInt("archived")!=0)return false;
        try{return currency(candidate).equals(book.optString("currency"))&&(!Json.parse(candidate.optString("payload")).optString("kind").equals("transfer")||LocalGoalPlans.canConfirmTransferBook(book));}
        catch(IllegalArgumentException error){return false;}
    }
    public static void requireBook(JSONObject candidate,JSONObject book){
        currency(candidate); // Keep a missing declaration distinct from a missing ledger.
        if(!canUseBook(candidate,book))throw new IllegalArgumentException("周期记录只能确认到相同币种的有效账本，请先在待确认列表重新选择");
    }
    public static boolean needsBook(ReferenceStore store,JSONObject candidate){
        JSONObject payload=Json.parse(candidate.optString("payload"));
        return payload.optBoolean("capture_target_book_unresolved")||!canUseBook(candidate,store.book(candidate.optLong("book_id")));
    }
    public static boolean canShowCnyOverlay(ReferenceStore store,JSONObject candidate){
        if(!isLocalPlan(candidate))return true;
        try{return "CNY".equals(currency(candidate))&&!needsBook(store,candidate);}
        catch(IllegalArgumentException error){return false;}
    }
    /** Save preflight also covers stale editors and direct calls to a candidate API. */
    public static void requireInputBook(ReferenceStore store,JSONObject candidate,JSONObject input){
        if(isLocalPlan(candidate))requireBook(candidate,store.book(input.optLong("book_id")));
    }
    public static boolean accountInCurrency(JSONObject account,String currency){
        if(account==null||account.optInt("archived")!=0||account.optInt("asset_type")==9)return false;
        String display=account.optString("currency");
        if(AssetRepository.ORIGINAL.equals(account.optString("asset_origin"))){
            if(!account.isNull("asset_currency")&&!account.optString("asset_currency").isEmpty())return false;
            if(display.isEmpty())display="CNY";
        }
        return currency.equals(display);
    }
}
