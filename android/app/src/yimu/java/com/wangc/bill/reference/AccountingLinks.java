package com.wangc.bill.reference;

import org.json.JSONObject;

/** Fee/interest Bills are edited and restored together with their owning operation. */
public final class AccountingLinks {
    private AccountingLinks() {}
    public static JSONObject ownerOfBill(ReferenceStore store,long billId){
        String filter="%\"fee_bill_id\":"+billId+"%";
        for(JSONObject receipt:store.query("SELECT entry_kind,entry_id,payload FROM accounting_operations WHERE payload LIKE ?",filter)){
            if(Json.parse(receipt.optString("payload")).optLong("fee_bill_id")==billId)
                return store.accountingEntry(receipt.optString("entry_kind"),receipt.optLong("entry_id"));
        }
        for(JSONObject receipt:store.query("SELECT entity_row_id,payload FROM accounting_financial_operations WHERE entity_kind='lend' AND action IN ('loan','borrow','collection','repayment') AND payload LIKE ?",filter)){
            if(Json.parse(receipt.optString("payload")).optLong("fee_bill_id")==billId)
                return store.accountingEntry("lend",receipt.optLong("entity_row_id"));
        }
        for(JSONObject candidate:store.query("SELECT * FROM candidates WHERE state='SAVED' AND save_kind IN ('transfer','lend') AND payload LIKE ?",filter)){
            if(Json.parse(candidate.optString("payload")).optLong("fee_bill_id")!=billId)continue;
            String kind=candidate.optString("save_kind");
            JSONObject receipt=store.one("SELECT * FROM candidate_"+(kind.equals("transfer")?"transfer":"lend")+"_receipts WHERE candidate_id=?",candidate.optString("id"));
            if(receipt!=null)return store.accountingEntry(kind,receipt.optLong(kind+"_row_id"));
        }
        return null;
    }
}
