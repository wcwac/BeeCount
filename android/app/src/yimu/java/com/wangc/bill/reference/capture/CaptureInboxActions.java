package com.wangc.bill.reference.capture;

import android.content.ContentValues;
import com.wangc.bill.reference.AccountingCommitScope;
import com.wangc.bill.reference.Json;
import com.wangc.bill.reference.LocalPlanCandidates;
import com.wangc.bill.reference.ReferenceStore;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;

/** Explicit user actions on stored observations; never changes a monetary balance. */
public final class CaptureInboxActions {
    private CaptureInboxActions() {}
    public static JSONObject resolveBook(ReferenceStore store,String candidateId,long chosenBook) {
        return AccountingCommitScope.run(store,()->{
            JSONObject candidate=store.candidate(candidateId),book=store.book(chosenBook);
            if(candidate==null||!"PENDING".equals(candidate.optString("state")))throw new IllegalArgumentException("采集记录已处理或不存在");
            if(LocalPlanCandidates.isLocalPlan(candidate))return resolveLocalPlanBook(store,candidate,book);
            if(book==null||book.optInt("archived")!=0||!"CNY".equals(book.optString("currency")))
                throw new IllegalArgumentException("请选择有效的人民币账本");
            JSONObject payload=Json.parse(candidate.optString("payload"));
            Json.put(payload,"book_id",chosenBook);Json.put(payload,"currency","CNY");
            Json.put(payload,"capture_target_book_unresolved",false);
            Json.put(payload,"capture_requires_confirmation",true);
            String kind=payload.optString("kind").equals("income")?"income":"expense";
            JSONObject category=store.categoryRepository().byRowId(payload.optLong("category_id"));
            if(category==null||category.optInt("archived")!=0||!kind.equals(category.optString("kind"))
                    ||store.categoryRepository().isHiddenInBook(category.optLong("id"),chosenBook)) {
                Json.put(payload,"category_id",store.defaultCategory(kind,chosenBook));
                payload.remove("parent_semantic_id");payload.remove("child_semantic_id");
            }
            Set<Long> available=new HashSet<>();
            for(JSONObject account:CaptureAccountMappings.choices(store,chosenBook))
                if(CaptureAccountMappings.eligible(account))available.add(account.optLong("id"));
            for(String field:new String[]{"account_id","to_account_id"})
                if(payload.optLong(field)>0&&!available.contains(payload.optLong(field)))store.selectAccountForInput(payload,field,0,true);
            // An unresolved capture deliberately has no guessed account. The editor lets
            // the user choose it after the target book has been explicitly confirmed.
            Json.put(payload,"capture_account_requires_confirmation",true);
            Json.put(payload,"capture_account_resolution","TARGET_BOOK_CHANGED_REVIEW");
            ContentValues update=new ContentValues();update.put("book_id",chosenBook);update.put("payload",payload.toString());
            AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("candidates",update,
                    "id=? AND state='PENDING'",new String[]{candidateId}));
            return store.candidate(candidateId);
        });
    }
    /** Only the selected local draft changes; neither rule nor capture-book preferences change. */
    private static JSONObject resolveLocalPlanBook(ReferenceStore store,JSONObject candidate,JSONObject book){
        LocalPlanCandidates.requireBook(candidate,book);long chosenBook=book.optLong("id");
        JSONObject payload=Json.parse(candidate.optString("payload"));String currency=LocalPlanCandidates.currency(candidate),kind=payload.optString("kind");
        Json.put(payload,"book_id",chosenBook);Json.put(payload,"currency",currency);
        Json.put(payload,"capture_target_book_unresolved",false);Json.put(payload,"capture_requires_confirmation",true);
        if(!kind.equals("transfer")){
            JSONObject category=store.categoryRepository().byRowId(payload.optLong("category_id"));
            if(category==null||category.optInt("archived")!=0||!kind.equals(category.optString("kind"))||store.categoryRepository().isHiddenInBook(category.optLong("id"),chosenBook)){
                Json.put(payload,"category_id",store.defaultCategory(kind,chosenBook));payload.remove("parent_semantic_id");payload.remove("child_semantic_id");
            }
        }
        Set<Long> available=new HashSet<>();
        for(JSONObject account:CaptureAccountMappings.choices(store,chosenBook))if(LocalPlanCandidates.accountInCurrency(account,currency))available.add(account.optLong("id"));
        for(String field:new String[]{"account_id","to_account_id"})if(payload.optLong(field)>0&&!available.contains(payload.optLong(field)))store.selectAccountForInput(payload,field,0,true);
        Json.put(payload,"capture_account_requires_confirmation",true);Json.put(payload,"capture_account_resolution","LOCAL_PLAN_BOOK_CHANGED_REVIEW");
        ContentValues update=new ContentValues();update.put("book_id",chosenBook);update.put("payload",payload.toString());
        AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("candidates",update,"id=? AND state='PENDING'",new String[]{candidate.optString("id")}));
        return store.candidate(candidate.optString("id"));
    }
    public static JSONObject reopen(ReferenceStore store,String candidateId) {
        return AccountingCommitScope.run(store,()->{
            JSONObject candidate=store.candidate(candidateId);
            if(candidate==null||!"DISMISSED".equals(candidate.optString("state")))throw new IllegalArgumentException("此记录不能恢复为待确认");
            JSONObject payload=Json.parse(candidate.optString("payload"));
            if(candidate.optLong("transaction_id")>0||!candidate.isNull("refund_owner_semantic_id")
                    ||!candidate.isNull("refund_semantic_id")||!candidate.isNull("refund_item_raw")||(!payload.isNull("capture_resolved_candidate_id")&&!payload.optString("capture_resolved_candidate_id").isEmpty())
                    ||store.one("SELECT id FROM transactions WHERE candidate_id=? LIMIT 1",candidateId)!=null
                    ||store.one("SELECT candidate_id FROM candidate_transfer_receipts WHERE candidate_id=?",candidateId)!=null
                    ||store.one("SELECT candidate_id FROM candidate_lend_receipts WHERE candidate_id=?",candidateId)!=null)
                throw new IllegalArgumentException("此记录已有保存关联，请在账单明细中处理");
            String key=candidate.isNull("capture_key")?"":candidate.optString("capture_key");
            String window=candidate.isNull("capture_window_key")?"":candidate.optString("capture_window_key");
            if(store.one("SELECT id FROM candidates WHERE id<>? AND package_name=? AND state='PENDING' AND (capture_key=? OR capture_window_key=?) LIMIT 1",
                    candidateId,candidate.optString("package_name"),key,window)!=null)
                throw new IllegalArgumentException("相同付款页面已有待确认记录，请先处理该记录");
            Json.put(payload,"capture_requires_confirmation",true);
            ContentValues update=new ContentValues();update.put("state","PENDING");update.put("payload",payload.toString());
            AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("candidates",update,
                    "id=? AND state='DISMISSED'",new String[]{candidateId}));
            return store.candidate(candidateId);
        });
    }
}
