package com.wangc.bill.reference;

import org.json.JSONObject;

/** A storage anchor for an unresolved capture is never a selected ledger. */
public final class AccountingTargets {
    private AccountingTargets() {}
    public static void requireResolved(ReferenceStore store,JSONObject input,String candidateId){
        boolean unresolved=input!=null&&input.optBoolean("capture_target_book_unresolved");
        if(candidateId!=null){
            JSONObject candidate=store.candidate(candidateId);
            if(candidate!=null&&"PENDING".equals(candidate.optString("state"))){
                LocalPlanCandidates.requireInputBook(store,candidate,input);
                unresolved|=Json.parse(candidate.optString("payload")).optBoolean("capture_target_book_unresolved");
            }
        }
        if(unresolved)throw new IllegalArgumentException("请先在待确认列表为这条记录选择目标账本");
    }
}
