package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.UUID;
import org.json.JSONObject;

/** User-facing lifecycle for typed transfers and credit repayments; raw identities stay stable. */
public final class AccountingTypedEntries {
    public static final String EDITS="accounting_entry_edits";
    private final ReferenceStore store;
    public AccountingTypedEntries(ReferenceStore store){this.store=store;}
    public static void installSchema(SQLiteDatabase db){
        db.execSQL("CREATE TABLE accounting_entry_edits(operation_id TEXT PRIMARY KEY,entry_kind TEXT NOT NULL CHECK(entry_kind IN ('transfer','lend')),entry_id INTEGER NOT NULL CHECK(entry_id>0),owner_semantic_id INTEGER NOT NULL,semantic_id INTEGER NOT NULL,payload TEXT NOT NULL,created INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX accounting_entry_edits_entry ON accounting_entry_edits(entry_kind,entry_id)");
    }
    public JSONObject detail(String kind,long id){synchronized(store){return describe(load(kind,id));}}

    public JSONObject edit(String kind,long id,JSONObject input,String operationId){
        JSONObject snapshot=Json.copy(input);String operation=uuid(operationId);
        return AccountingCommitScope.run(store,()->{
            AccountingTargets.requireResolved(store,snapshot,null);
            State old=load(kind,id);
            if(!snapshot.has("tag_ids")&&old.metadata.has("tag_ids"))Json.put(snapshot,"tag_ids",old.metadata.opt("tag_ids"));
            if(!snapshot.has("attachments")&&old.metadata.has("attachments"))Json.put(snapshot,"attachments",old.metadata.opt("attachments"));
            JSONObject committed=store.one("SELECT * FROM "+EDITS+" WHERE operation_id=?",operation);
            if(committed!=null){
                if(!kind.equals(committed.optString("entry_kind"))||id!=committed.optLong("entry_id"))throw new IllegalArgumentException("此编辑标识已用于其他记录");
                return describe(old);
            }
            if(store.one("SELECT id FROM transactions WHERE manual_operation_id=?",operation)!=null
                    ||store.one("SELECT operation_id FROM accounting_operations WHERE operation_id=?",operation)!=null
                    ||store.one("SELECT operation_id FROM accounting_financial_operations WHERE operation_id=?",operation)!=null)
                throw new IllegalArgumentException("此保存标识已用于新增记录");
            if(old.deleted)throw new IllegalArgumentException("请先恢复回收站中的记录再编辑");
            requireAssets(old);
            // Reverse the actual old principal. Its fee is a separate normal Bill.
            principal(old,true,"编辑记录：撤回原本金");
            JSONObject oldFee=feeBill(old);
            if(oldFee!=null&&oldFee.optInt("deleted")==0)store.trash(oldFee.optLong("id"),true);

            Json.put(snapshot,"kind","transfer");Json.put(snapshot,"transfer_mode",0);
            String temporaryOperation=UUID.randomUUID().toString();
            JSONObject created=new AccountingOperations(store).saveTransfer(snapshot,temporaryOperation,kind.equals("lend"));
            State replacement=load(kind,created.optLong("entry_id"));
            if(kind.equals("transfer")){
                OriginalTransferRecord value=replacement.transfer.copy();
                value.localRowId=id;value.transferId=old.semantic;value.userId=old.owner;
                value.tags=old.transfer.tags==null?null:new java.util.ArrayList<>(old.transfer.tags);
                OriginalTransferActions.Result saved=new OriginalTransferActions(store,old.owner,effects(old)::transferChanged).update(value);
                if(!saved.save.saved)throw new IllegalStateException("转账修改未能完整保存，可安全重试");
            }else{
                OriginalLendRecord value=replacement.lend.copy();
                value.localRowId=id;value.lendId=(int)old.semantic;value.userId=old.owner;value.createTime=old.lend.createTime;
                OriginalLendActions.Result saved=new OriginalLendActions(store,old.owner).update(value);
                if(!saved.save.saved)throw new IllegalStateException("还款修改未能完整保存，可安全重试");
                effects(old).lendChanged(value,true);
            }
            // The existing creation command applies the new balances/fee once. Its
            // temporary entity/receipt are removed in this same outer transaction.
            SQLiteDatabase db=store.getWritableDatabase();
            AccountingCommitScope.requireChanged(store,db.delete("accounting_operations","operation_id=?",new String[]{temporaryOperation}));
            AccountingCommitScope.requireChanged(store,db.delete(rawTable(kind),"id=?",new String[]{""+replacement.id}));
            if(oldFee!=null)store.purge(oldFee.optLong("id"));

            JSONObject metadata=Json.copy(created);metadata.remove("operation_id");
            Json.put(metadata,"entry_id",id);Json.put(metadata,"id",id);Json.put(metadata,"entry_key",kind+":"+id);
            Json.put(metadata,"entry_owner_semantic_id",old.owner);Json.put(metadata,"entry_semantic_id",old.semantic);
            Json.put(metadata,"typed_fee_restore_owned",false);Json.put(metadata,"last_edit_operation_id",operation);
            State updated=loadRaw(kind,id,metadata,metadata.optLong("book_id"));
            JSONObject result=describe(updated);persist(updated,result);
            ContentValues receipt=new ContentValues();receipt.put("operation_id",operation);receipt.put("entry_kind",kind);
            receipt.put("entry_id",id);receipt.put("owner_semantic_id",old.owner);receipt.put("semantic_id",old.semantic);
            receipt.put("payload",Json.object("entry_kind",kind,"entry_id",id,"entry_key",kind+":"+id,"entry_owner_semantic_id",old.owner,"entry_semantic_id",old.semantic).toString());receipt.put("created",System.currentTimeMillis());
            if(db.insertOrThrow(EDITS,null,receipt)==-1)throw new IllegalStateException("编辑凭据未能保存，可安全重试");
            return result;
        });
    }

    public JSONObject trash(String kind,long id,boolean deleted){
        return AccountingCommitScope.run(store,()->{
            State state=load(kind,id);
            if(state.deleted==deleted)return describe(state);
            requireAssets(state);
            JSONObject fee=feeBill(state);
            boolean owned=state.metadata.optBoolean("typed_fee_restore_owned");
            if(!deleted&&owned&&fee==null)throw new IllegalArgumentException("关联手续费已不存在，无法完整恢复此记录");
            if(deleted){
                principal(state,true,"删除记录：退回原本金");
                owned=fee!=null&&fee.optInt("deleted")==0;
                if(owned)store.trash(fee.optLong("id"),true);
            }else{
                principal(state,false,"恢复记录：重新记入本金");
                if(owned&&fee!=null)store.trash(fee.optLong("id"),false);
                owned=false;
            }
            if(kind.equals("transfer")){
                state.transfer.originalDeleted=deleted;
                OriginalTransferActions.Result saved=new OriginalTransferActions(store,state.owner,effects(state)::transferChanged).update(state.transfer);
                if(!saved.save.saved)throw new IllegalStateException("转账状态未能保存，可安全重试");
            }else{
                state.lend.originalDeleted=deleted;
                OriginalLendActions.Result saved=new OriginalLendActions(store,state.owner).update(state.lend);
                if(!saved.save.saved)throw new IllegalStateException("还款状态未能保存，可安全重试");
                effects(state).lendChanged(state.lend,true);
            }
            state.deleted=deleted;Json.put(state.metadata,"typed_fee_restore_owned",owned);
            JSONObject result=describe(state);persist(state,result);return result;
        });
    }

    private void principal(State state,boolean reverse,String content){
        OriginalFinancialOrchestrator financial=new OriginalFinancialOrchestrator(store,state.owner,state.owner);
        financial.changeAsset(state.from,Math.abs(state.amount),!reverse,content);
        financial.changeAsset(state.to,Math.abs(state.amount),reverse,content);
    }
    private void requireAssets(State state){
        if(state.owner!=ReferenceStore.LOCAL_OFFLINE_OWNER)throw new IllegalArgumentException("记录不属于当前离线身份");
        if(state.from==state.to)throw new IllegalArgumentException("记录的两个账户不能相同");
        for(long raw:new long[]{state.from,state.to}){
            JSONObject asset=store.assetRepository().p(raw,state.owner);
            if(asset==null||asset.optInt("asset_type")==9||!asset.isNull("asset_currency")&&!asset.optString("asset_currency").isEmpty())
                throw new IllegalArgumentException("原账户不存在或使用未接入的币种，无法安全调整余额");
        }
        if(state.transfer!=null&&state.transfer.toCost!=0||state.lend!=null&&state.lend.fromCost!=0)
            throw new IllegalArgumentException("跨币种历史记录暂不支持修改余额");
    }

    private State load(String kind,long id){
        validKind(kind,id);
        JSONObject manual=store.one("SELECT * FROM accounting_operations WHERE entry_kind=? AND entry_id=?",kind,""+id);
        if(manual!=null){
            State state=loadRaw(kind,id,Json.parse(manual.optString("payload")),manual.optLong("book_id"));
            if(state.owner!=manual.optLong("owner_semantic_id")||state.semantic!=manual.optLong("semantic_id"))throw new IllegalArgumentException("原记录身份已经改变，不能调整另一笔记录的余额");
            return state;
        }
        JSONObject candidate=store.one("SELECT c.payload,c.book_id,r.owner_semantic_id AS matched_owner,r."+kind+"_semantic_id AS matched_semantic FROM candidates c JOIN "+receiptTable(kind)+" r ON r.candidate_id=c.id WHERE r."+kind+"_row_id=?", ""+id);
        if(candidate==null)throw new IllegalArgumentException("此记录尚未关联本地账本，无法安全管理");
        State state=loadRaw(kind,id,Json.parse(candidate.optString("payload")),candidate.optLong("book_id"));
        if(state.owner!=candidate.optLong("matched_owner")||state.semantic!=candidate.optLong("matched_semantic"))throw new IllegalArgumentException("采集收据关联的原记录已经改变");
        return state;
    }
    private State loadRaw(String kind,long id,JSONObject metadata,long book){
        State state=new State();state.kind=kind;state.id=id;state.metadata=Json.copy(metadata);state.book=book;
        if(kind.equals("transfer")){
            state.transfer=new OriginalTransferRepository(store).byLocalRowId(id);
            if(state.transfer==null)throw new IllegalArgumentException("转账记录已不存在");
            state.owner=state.transfer.userId;state.semantic=state.transfer.transferId;
            state.from=state.transfer.fromAssetId;state.to=state.transfer.toAssetId;
            state.amount=state.transfer.cost;state.feeSemantic=state.transfer.billId;
            state.time=state.transfer.time;state.remark=state.transfer.remark;state.deleted=state.transfer.originalDeleted;
        }else{
            state.lend=new OriginalLendRepository(store).byLocalRowId(id);
            if(state.lend==null||state.lend.type!=4)throw new IllegalArgumentException("此借贷记录需要使用借出或借入管理入口");
            state.owner=state.lend.userId;state.semantic=state.lend.lendId;
            state.from=state.lend.repaymentAssetId;state.to=state.lend.assetId;
            state.amount=state.lend.number;state.feeSemantic=state.lend.billId;
            state.time=state.lend.outTime;state.remark=state.lend.remark;state.deleted=state.lend.originalDeleted;
        }
        if(state.owner!=ReferenceStore.LOCAL_OFFLINE_OWNER||!Double.isFinite(state.amount))throw new IllegalArgumentException("记录的本地身份或金额无效");
        return state;
    }

    private JSONObject describe(State state){
        JSONObject value=Json.copy(state.metadata),from=store.assetRepository().p(state.from,state.owner),to=store.assetRepository().p(state.to,state.owner);
        JSONObject fee=feeBill(state);long feeAmount=0;
        if(fee!=null&&(fee.optInt("deleted")==0||state.deleted&&state.metadata.optBoolean("typed_fee_restore_owned")))
            feeAmount=fee.optLong("amount")*(fee.optString("kind").equals("income")?-1:1);
        Json.put(value,"entry_kind",state.kind);Json.put(value,"entry_id",state.id);Json.put(value,"id",state.id);Json.put(value,"entry_key",state.kind+":"+state.id);
        Json.put(value,"entry_owner_semantic_id",state.owner);Json.put(value,"entry_semantic_id",state.semantic);
        Json.put(value,"book_id",state.book);Json.put(value,"kind","transfer");Json.put(value,"financial_type",state.kind.equals("lend")?"original_lend":"original_transfer");
        Json.put(value,"lend_type",state.lend==null?0:state.lend.type);Json.put(value,"amount",OriginalTransferConfirmation.minor(Math.abs(state.amount)));
        Json.put(value,"currency","CNY");Json.put(value,"fee",feeAmount);Json.put(value,"fee_bill_id",fee==null?null:fee.optLong("id"));
        Json.put(value,"time",state.time);Json.put(value,"remark",state.remark==null?"":state.remark);Json.put(value,"deleted",state.deleted?1:0);
        Json.put(value,"account_id",from==null?0:from.optLong("id"));Json.put(value,"from_account_id",from==null?0:from.optLong("id"));
        Json.put(value,"to_account_id",to==null?0:to.optLong("id"));Json.put(value,"raw_asset_semantic_id",state.from);Json.put(value,"raw_to_asset_semantic_id",state.to);
        Json.put(value,"owner_semantic_id",state.owner);Json.put(value,"transfer_mode",0);
        Json.put(value,"not_total",1);Json.put(value,"not_budget",1);Json.put(value,"settled",1);Json.put(value,"discount",0);
        if(!value.has("attachments"))Json.put(value,"attachments","[]");
        Json.put(value,"transfer_amount_text",Money.text(value.optLong("amount"),"CNY"));Json.put(value,"transfer_fee_text",Money.text(feeAmount,"CNY"));
        return value;
    }

    private JSONObject feeBill(State state){
        if(state.feeSemantic<0)return null;
        boolean anchored=state.metadata.has("fee_bill_id");
        JSONObject fee=state.metadata.optLong("fee_bill_id")>0?store.transaction(state.metadata.optLong("fee_bill_id")):null;
        if(anchored&&fee==null)return null;
        if(fee!=null&&(!OriginalBillRepository.ORIGINAL.equals(fee.optString("bill_origin"))||fee.optInt("bill_user_id")!=state.owner||fee.optInt("bill_semantic_id")!=state.feeSemantic))
            throw new IllegalArgumentException("手续费记录的关联已变化");
        if(fee==null)fee=store.one("SELECT * FROM transactions WHERE bill_origin='original' AND bill_user_id=? AND bill_semantic_id=? ORDER BY id LIMIT 1",""+state.owner,""+state.feeSemantic);
        return fee;
    }
    private void persist(State state,JSONObject result){
        SQLiteDatabase db=store.getWritableDatabase();int owners=0;
        for(JSONObject operation:store.query("SELECT operation_id FROM accounting_operations WHERE entry_kind=? AND entry_id=?",state.kind,""+state.id)){
            JSONObject payload=Json.copy(result);Json.put(payload,"operation_id",operation.optString("operation_id"));
            ContentValues values=new ContentValues();values.put("payload",payload.toString());values.put("book_id",state.book);
            AccountingCommitScope.requireChanged(store,db.update("accounting_operations",values,"operation_id=?",new String[]{operation.optString("operation_id")}));owners++;
        }
        for(JSONObject candidate:store.query("SELECT c.* FROM candidates c JOIN "+receiptTable(state.kind)+" r ON r.candidate_id=c.id WHERE r."+state.kind+"_row_id=?",""+state.id)){
            JSONObject payload=Json.parse(candidate.optString("payload"));
            for(String field:new String[]{"entry_kind","entry_id","entry_key","entry_owner_semantic_id","entry_semantic_id","book_id","kind","financial_type","lend_type","amount","currency","fee","fee_bill_id","time","remark","deleted","account_id","to_account_id","raw_asset_semantic_id","raw_to_asset_semantic_id","owner_semantic_id","transfer_mode","attachments","tag_ids","transfer_amount_text","transfer_fee_text","typed_fee_restore_owned","last_edit_operation_id"})
                if(result.has(field))Json.put(payload,field,result.opt(field));
            Json.put(payload,state.kind+"_row_id",state.id);Json.put(payload,state.kind+"_owner_semantic_id",state.owner);Json.put(payload,state.kind+"_semantic_id",state.semantic);
            ContentValues values=new ContentValues();values.put("payload",payload.toString());values.put("book_id",state.book);
            AccountingCommitScope.requireChanged(store,db.update("candidates",values,"id=?",new String[]{candidate.optString("id")}));owners++;
        }
        if(owners==0)throw new IllegalStateException("记录保存凭据已不存在");
    }
    private OriginalFinancialEffects effects(State state){return new OriginalFinancialEffects(store,state.owner,state.owner);}
    private static String rawTable(String kind){return kind.equals("transfer")?"original_transfers":"original_lends";}
    private static String receiptTable(String kind){return kind.equals("transfer")?"candidate_transfer_receipts":"candidate_lend_receipts";}
    private static void validKind(String kind,long id){if(!("transfer".equals(kind)||"lend".equals(kind))||id<=0)throw new IllegalArgumentException("记录类型或编号无效");}
    private static String uuid(String value){if(value==null||!UUID.fromString(value).toString().equals(value))throw new IllegalArgumentException("编辑保存标识无效");return value;}
    public static void validateRestored(ReferenceStore store){
        synchronized(store){for(JSONObject receipt:store.query("SELECT * FROM "+EDITS)){
            uuid(receipt.optString("operation_id"));State state=new AccountingTypedEntries(store).load(receipt.optString("entry_kind"),receipt.optLong("entry_id"));
            JSONObject payload=Json.parse(receipt.optString("payload"));
            if(state.owner!=receipt.optLong("owner_semantic_id")||state.semantic!=receipt.optLong("semantic_id")
                    ||!state.kind.equals(payload.optString("entry_kind"))||state.id!=payload.optLong("entry_id"))throw new IllegalArgumentException("编辑凭据与记录身份不匹配");
        }}
    }
    private static final class State{
        String kind,remark;long id,semantic,from,to,book,time;int owner,feeSemantic;double amount;boolean deleted;
        JSONObject metadata;OriginalTransferRecord transfer;OriginalLendRecord lend;
    }
}
