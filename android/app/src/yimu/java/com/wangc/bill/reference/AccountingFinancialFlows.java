package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * User-facing local debt, refund and reimbursement operations. Original entities
 * carry the accounting facts; this host receipt supplies retry keys and the
 * explicit parent link absent from raw Lend. It is not an original LitePal table.
 * All amounts accepted by this API are minor CNY units. Principal transfers never
 * create an income/expense Bill; only interest does.
 */
public final class AccountingFinancialFlows {
    public static final String TABLE="accounting_financial_operations";
    private static final int OWNER=ReferenceStore.LOCAL_OFFLINE_OWNER;
    private static final long MAX=99999999999999L;
    private static final String LOAN_ACTIONS="('loan','borrow','collection','repayment')";
    private final ReferenceStore store;
    public AccountingFinancialFlows(ReferenceStore store) {
        if(store==null)throw new IllegalArgumentException("记账数据库不能为空");
        this.store=store;
    }

    public static void installV10Schema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE accounting_financial_operations(operation_id TEXT PRIMARY KEY,action TEXT NOT NULL CHECK(action IN ('loan','borrow','collection','repayment','loan_edit','refund','reimbursement_mark','reimbursement_receive','reimbursement_bill','reimbursement_edit')),entity_kind TEXT NOT NULL CHECK(entity_kind IN ('lend','bill')),entity_row_id INTEGER NOT NULL CHECK(entity_row_id>0),owner_semantic_id INTEGER NOT NULL,semantic_id INTEGER NOT NULL,book_id INTEGER NOT NULL REFERENCES books(id),parent_lend_row_id INTEGER REFERENCES original_lends(id),payload TEXT NOT NULL,created INTEGER NOT NULL,deleted INTEGER NOT NULL DEFAULT 0 CHECK(deleted IN(0,1)))");
        db.execSQL("CREATE UNIQUE INDEX accounting_financial_lend ON accounting_financial_operations(entity_row_id) WHERE entity_kind='lend' AND action IN ('loan','borrow','collection','repayment')");
        db.execSQL("CREATE INDEX accounting_financial_parent ON accounting_financial_operations(parent_lend_row_id,deleted)");
        db.execSQL("CREATE INDEX accounting_financial_book ON accounting_financial_operations(book_id,created)");
    }

    /** account_id=funds; to_account_id=receivable/payable; financial_type=lend/borrow. */
    public JSONObject saveLoan(JSONObject input,String operationId) {
        JSONObject value=Json.copy(input);String key=uuid(operationId);
        AccountingTargets.requireResolved(store,value,null);
        return AccountingCommitScope.run(store,()->{
            String action="borrow".equals(value.optString("financial_type"))?"borrow":"loan";
            if(!("lend".equals(value.optString("financial_type"))||"borrow".equals(value.optString("financial_type"))))
                throw new IllegalArgumentException("请选择借出或借入");
            JSONObject old=prior(key,action,0);if(old!=null)return old;
            JSONObject book=book(value.optLong("book_id"));
            long amount=positive(value,"amount"),time=time(value),due=value.optLong("in_time");
            if(due<0||due>8640000000000000L)throw new IllegalArgumentException("到期时间无效");
            JSONObject funds=funds(value.optLong("account_id"));
            JSONObject debt=debt(value,action.equals("borrow"),book);
            different(funds,debt);
            OriginalLendRecord lend=new OriginalLendRecord();
            lend.type=action.equals("borrow")?3:1;lend.number=number(amount);lend.fromCost=lend.number;
            lend.assetId=debt.optLong("semantic_id");lend.repaymentAssetId=funds.optLong("semantic_id");
            lend.billId=-1;lend.outTime=time;lend.inTime=due;lend.remark=remark(value);
            applyPrincipal(lend,false);
            saveLend(lend,true);
            JSONObject result=loanDto(lend,book.optLong("id"),funds.optLong("id"),debt.optLong("id"),0,0,value);
            record(key,action,"lend",lend.localRowId,lend.lendId,book.optLong("id"),null,result,false);
            return result;
        });
    }

    /** Partial/full collection for a lend, or repayment for a borrow. Interest is separate. */
    public JSONObject settleLoan(long parentRow,JSONObject input,String operationId) {
        JSONObject value=Json.copy(input);String key=uuid(operationId);
        AccountingTargets.requireResolved(store,value,null);
        return AccountingCommitScope.run(store,()->{
            JSONObject parentReceipt=loanReceipt(parentRow);OriginalLendRecord parent=lend(parentRow,true);
            if(parent.type!=1&&parent.type!=3)throw new IllegalArgumentException("请选择原借出或借入记录");
            String action=parent.type==1?"collection":"repayment";
            JSONObject old=prior(key,action,parentRow);if(old!=null)return old;
            if(parent.originalDeleted)throw new IllegalArgumentException("原借贷记录已删除");
            long amount=positive(value,"amount"),remaining=outstanding(parentRow);
            if(amount>remaining)throw new IllegalArgumentException("结清本金不能超过未结金额");
            long interest=signed(value,"interest"),when=time(value);
            JSONObject book=book(parentReceipt.optLong("book_id"));
            JSONObject funds=funds(value.optLong("account_id"));
            JSONObject debt=assetBySemantic(parent.assetId);requireDebt(debt,parent.type==3);different(funds,debt);
            OriginalLendRecord settlement=new OriginalLendRecord();
            settlement.type=parent.type==1?2:4;settlement.assetId=parent.assetId;
            settlement.repaymentAssetId=funds.optLong("semantic_id");settlement.number=number(amount);
            settlement.fromCost=settlement.number;settlement.interest=number(interest);
            settlement.outTime=when;settlement.remark=remark(value);settlement.billId=-1;
            applyPrincipal(settlement,false);
            long feeRow=interestBill(settlement,book,interest);
            saveLend(settlement,true);
            JSONObject result=loanDto(settlement,book.optLong("id"),funds.optLong("id"),debt.optLong("id"),parentRow,feeRow,value);
            record(key,action,"lend",settlement.localRowId,settlement.lendId,book.optLong("id"),parentRow,result,false);
            return result;
        });
    }

    public static boolean ownsLend(ReferenceStore store,long row) {
        synchronized(store){return store.one("SELECT operation_id FROM accounting_financial_operations WHERE entity_kind='lend' AND entity_row_id=? AND action IN "+LOAN_ACTIONS,""+row)!=null;}
    }
    public static JSONObject detail(ReferenceStore store,long row) {return new AccountingFinancialFlows(store).loanDetail(row);}
    private JSONObject loanDetail(long row) {
        synchronized(store){
            JSONObject receipt=loanReceipt(row);OriginalLendRecord raw=lend(row,true);
            JSONObject value=Json.parse(receipt.optString("payload"));
            Json.put(value,"amount",minor(raw.number));Json.put(value,"time",raw.outTime);Json.put(value,"remark",raw.remark==null?"":raw.remark);
            Json.put(value,"in_time",raw.inTime);Json.put(value,"interest",minorSigned(raw.interest));
            Json.put(value,"deleted",raw.originalDeleted?1:0);Json.put(value,"lend_type",raw.type);
            if(raw.type==1||raw.type==3){long left=outstanding(row);Json.put(value,"outstanding",left);Json.put(value,"settled",left==0?1:0);}
            else{Json.put(value,"outstanding",0);Json.put(value,"settled",1);}
            return value;
        }
    }

    public static JSONObject edit(ReferenceStore store,long row,JSONObject input,String editId) {
        return new AccountingFinancialFlows(store).editLoan(row,input,editId);
    }
    private JSONObject editLoan(long row,JSONObject input,String editId) {
        JSONObject value=Json.copy(input);String key=uuid(editId);
        AccountingTargets.requireResolved(store,value,null);
        return AccountingCommitScope.run(store,()->{
            JSONObject old=prior(key,"loan_edit",row);if(old!=null)return loanDetail(row);
            JSONObject receipt=loanReceipt(row);OriginalLendRecord raw=lend(row,false);
            JSONObject previous=Json.parse(receipt.optString("payload"));
            for(String metadata:new String[]{"tag_ids","attachments","counterparty"})if(!value.has(metadata)&&previous.has(metadata))Json.put(value,metadata,previous.opt(metadata));
            JSONObject book=book(value.optLong("book_id",receipt.optLong("book_id")));
            if(book.optLong("id")!=receipt.optLong("book_id"))throw new IllegalArgumentException("借贷记录暂不支持跨账本编辑");
            long amount=positive(value,"amount"),interest=(raw.type==2||raw.type==4)?signed(value,"interest"):0;
            long parent=receipt.optLong("parent_lend_row_id");
            if(parent==0){long paid=paid(row);if(amount<paid)throw new IllegalArgumentException("本金不能小于已结清金额");}
            else if(amount>outstanding(parent)+minor(raw.number))throw new IllegalArgumentException("结清本金不能超过原借贷余额");
            JSONObject funds=funds(value.optLong("account_id",previous.optLong("account_id")));
            JSONObject target;
            if(parent!=0)target=assetBySemantic(lend(parent,false).assetId);
            else target=debt(value.has("to_account_id")||value.has("debt_account_id")?value:Json.object("to_account_id",previous.optLong("to_account_id")),raw.type==3,book);
            requireDebt(target,raw.type==3||raw.type==4);different(funds,target);
            if(parent==0&&paid(row)>0&&raw.assetId!=target.optLong("semantic_id"))throw new IllegalArgumentException("已有结清记录时不能变更债务账户");
            time(value); // all preflight completes before any reversal
            applyPrincipal(raw,true);removeInterest(previous,true);
            raw.number=number(amount);raw.fromCost=raw.number;raw.interest=number(interest);
            raw.assetId=target.optLong("semantic_id");raw.repaymentAssetId=funds.optLong("semantic_id");
            raw.outTime=time(value);raw.inTime=value.optLong("in_time",raw.inTime);raw.remark=remark(value);
            if(raw.inTime<0||raw.inTime>8640000000000000L)throw new IllegalArgumentException("到期时间无效");
            applyPrincipal(raw,false);raw.billId=-1;raw.generalServiceBill=false;
            long fee=interestBill(raw,book,interest);saveLend(raw,false);
            JSONObject result=loanDto(raw,book.optLong("id"),funds.optLong("id"),target.optLong("id"),parent,fee,value);
            updateLoanReceipt(receipt,result,false);
            record(key,"loan_edit","lend",row,raw.lendId,book.optLong("id"),parent==0?null:parent,result,false);
            return loanDetail(row);
        });
    }

    public static void trash(ReferenceStore store,long row,boolean deleted) {new AccountingFinancialFlows(store).trashLoan(row,deleted);}
    private void trashLoan(long row,boolean deleted) {
        AccountingCommitScope.run(store,()->{
            JSONObject receipt=loanReceipt(row);OriginalLendRecord raw=lend(row,true);
            if(raw.originalDeleted==deleted)return null;
            long parent=receipt.optLong("parent_lend_row_id");
            if(parent==0&&paid(row)>0)throw new IllegalArgumentException("请先移除关联的结清记录");
            if(!deleted&&parent!=0){lend(parent,false);if(minor(raw.number)>outstanding(parent))throw new IllegalArgumentException("恢复后结清金额超过原本金");}
            assetBySemantic(raw.assetId);assetBySemantic(raw.repaymentAssetId);
            JSONObject value=Json.parse(receipt.optString("payload"));
            if(deleted){applyPrincipal(raw,true);Json.put(value,"fee_restore_owned",removeInterest(value,false));}
            else{
                applyPrincipal(raw,false);
                long fee=value.optLong("fee_bill_id");
                if(fee>0&&value.optBoolean("fee_restore_owned")){
                    if(store.transaction(fee)==null)throw new IllegalArgumentException("关联利息账单已不存在，不能完整恢复借贷记录");
                    store.trash(fee,false);
                }
                Json.put(value,"fee_restore_owned",false);
            }
            raw.originalDeleted=deleted;saveLend(raw,false);updateLoanReceipt(receipt,value,deleted);
            return null;
        });
    }

    public static List<JSONObject> entries(ReferenceStore store,long book,long start,long end,String search,boolean trash) {
        synchronized(store){
            List<JSONObject> result=new ArrayList<>();String query=search==null?"":search.toLowerCase(java.util.Locale.ROOT);
            AccountingFinancialFlows flows=new AccountingFinancialFlows(store);
            for(JSONObject row:store.query("SELECT entity_row_id FROM accounting_financial_operations WHERE entity_kind='lend' AND action IN "+LOAN_ACTIONS+" AND book_id=? AND deleted=?",""+book,trash?"1":"0")){
                JSONObject item=flows.loanDetail(row.optLong("entity_row_id"));
                if(item.optLong("time")>=start&&item.optLong("time")<end&&(query.isEmpty()||item.optString("remark").toLowerCase(java.util.Locale.ROOT).contains(query)||item.optString("category_name").contains(query)))result.add(item);
            }
            return result;
        }
    }

    /** Ordinary income/expense refund: fresh Bill, real Refund aggregate, no synthetic income Bill. */
    public JSONObject refundBill(long billRow,JSONObject input,String operationId) {
        JSONObject value=Json.copy(input);String key=uuid(operationId);
        AccountingTargets.requireResolved(store,value,null);
        return AccountingCommitScope.run(store,()->{
            JSONObject old=prior(key,"refund",billRow);if(old!=null)return old;
            OriginalBillRecord parent=bill(billRow);JSONObject host=store.transaction(billRow);
            if(parent.reimbursement)throw new IllegalArgumentException("报销账单请先取消报销标记后再退款");
            long amount=positive(value,"amount");if(amount>minor(parent.cost))throw new IllegalArgumentException("退款金额不能超过剩余账单金额");
            long when=time(value);JSONObject receiver=funds(value.optLong("account_id",host.optLong("account_id")));
            int bookOwner=book(host.optLong("book_id")).optInt("owner_semantic_id");
            OriginalFinancialEffects effects=new OriginalFinancialEffects(store,OWNER,bookOwner);
            OriginalInstalmentCommands instalments=new OriginalInstalmentCommands(store,OWNER,bookOwner,effects::creditRefresh);
            OriginalAutoRefundCommands commands=new OriginalAutoRefundCommands(store,OWNER,bookOwner,new OriginalAutoRefundCommands.Effects(){
                public void parentUpdated(OriginalBillRecord record,boolean saved){effects.parentUpdated(record,saved);}
                public void adjustInstalment(OriginalBillRecord record){instalments.adjustAfterRefund(record);}
                public void refreshCredit(OriginalBillRecord record){effects.refreshCredit(record);}
                public void refundAggregateChanged(int semantic,double total,boolean saved){effects.refundAggregateChanged(semantic,total,saved);}
                public void refreshGroup(OriginalBillRecord record){effects.refreshGroup(record);}
            });
            OriginalAutoRefundCommands.Result result=commands.save(parent,receiver.optLong("semantic_id"),number(amount),remark(value),when,value.optLong("in_time"),(record,difference)->projection(record));
            if(!result.saved)throw new IllegalStateException("退款保存未完成，已撤销，可安全重试");
            JSONObject response=Json.object("entry_kind","bill","entry_id",billRow,"entry_key","bill:"+billRow,"id",billRow,
                    "book_id",host.optLong("book_id"),"financial_type","refund","amount",amount,"time",when,
                    "refund_row_id",result.refundSave.record.rowId,"refund_raw",result.refundSave.record.data.rawInfos.get(0),"remaining",minor(parent.cost),"account_id",receiver.optLong("id"));
            record(key,"refund","bill",billRow,parent.billId,host.optLong("book_id"),null,response,false);
            return response;
        });
    }

    /** New reimbursement expense debits once through the ordinary Bill path, then marks it. */
    public JSONObject saveReimbursementBill(JSONObject input,String operationId) {
        JSONObject value=Json.copy(input);String key=uuid(operationId);
        AccountingTargets.requireResolved(store,value,null);
        return AccountingCommitScope.run(store,()->{
            JSONObject previous=prior(key,"reimbursement_bill",0);if(previous!=null)return previous;
            JSONObject book=book(value.optLong("book_id"));funds(value.optLong("account_id"));
            Json.put(value,"financial_type","");Json.put(value,"kind","expense");Json.put(value,"related_id",0);Json.put(value,"fee",0);
            long row=store.saveTransaction(value,0,null);
            JSONObject result=markReimbursementInScope(row,true);
            record(key,"reimbursement_bill","bill",row,bill(row).billId,book.optLong("id"),null,result,false);
            return result;
        });
    }

    public JSONObject markReimbursement(long billRow,boolean marked,String operationId) {
        String key=uuid(operationId);
        return AccountingCommitScope.run(store,()->{
            JSONObject previous=prior(key,"reimbursement_mark",billRow);if(previous!=null){if(previous.optBoolean("marked")!=marked)throw new IllegalArgumentException("此保存标识用于不同的报销操作");return previous;}
            JSONObject result=markReimbursementInScope(billRow,marked);JSONObject host=store.transaction(billRow);
            record(key,"reimbursement_mark","bill",billRow,bill(billRow).billId,host.optLong("book_id"),null,result,false);return result;
        });
    }

    private JSONObject markReimbursementInScope(long billRow,boolean marked) {
        OriginalBillRecord raw=bill(billRow);JSONObject host=store.transaction(billRow);
        if(marked)AccountingInstalments.requireNoSchedule(store,billRow);
        if(raw.parentCategoryId==9)throw new IllegalArgumentException("收入账单不能标记为报销支出");
        OriginalRefundRepository.Snapshot refund=new OriginalRefundRepository(store).firstByParent(OWNER,raw.billId);
        if(marked&&refund!=null&&refund.data.refundNumber!=0)throw new IllegalArgumentException("已有退款的账单请先处理退款再报销");
        OriginalReimbursementRepository repository=new OriginalReimbursementRepository(store);
        OriginalReimbursementRepository.Snapshot aggregate=repository.firstByParent(OWNER,raw.billId);
        JSONObject earlierMark=lastMark(billRow);
        boolean priorTotal=raw.reimbursement?(earlierMark!=null&&earlierMark.optBoolean("prior_not_total")):raw.notIntoTotal;
        boolean priorBudget=raw.reimbursement?(earlierMark!=null&&earlierMark.optBoolean("prior_not_budget")):raw.notIntoBudget;
        ContentValues projection=projection(raw);
        if(marked){
            if(aggregate==null){
                JSONObject asset=reimbursementAccount();OriginalReimbursementRepository.Data data=new OriginalReimbursementRepository.Data();
                data.parentBillSemanticId=raw.billId;data.parentTransactionRowId=billRow;data.assetSemanticId=asset.optLong("semantic_id");data.rawEntries=new ArrayList<>();
                aggregate=repository.create(OWNER,data,System.currentTimeMillis());
            }
            raw.reimbursement=true;raw.reimbursementEnd=aggregate.data.end;raw.notIntoTotal=true;raw.notIntoBudget=true;
            projection.put("financial_type","reimbursement");projection.put("not_total",1);projection.put("not_budget",1);projection.put("settled",aggregate.data.end?1:0);
        }else{
            if(aggregate!=null&&aggregate.data.reimbursementNumber!=0)throw new IllegalArgumentException("已有报销到账记录，不能取消报销标记");
            if(aggregate!=null)repository.delete(aggregate.rowId,OWNER);
            raw.reimbursement=false;raw.reimbursementEnd=false;
            raw.notIntoTotal=priorTotal;raw.notIntoBudget=priorBudget;
            projection.put("financial_type","");projection.put("not_total",raw.notIntoTotal?1:0);projection.put("not_budget",raw.notIntoBudget?1:0);projection.put("settled",0);
        }
        raw.updateTime=System.currentTimeMillis();saveBillRaw(raw,projection);
        OriginalFinancialEffects effects=new OriginalFinancialEffects(store,OWNER,OWNER);effects.parentUpdated(raw,true);effects.refreshCredit(raw);
        if(aggregate!=null)effects.assetCreatedOrEdited(aggregate.data.assetSemanticId);
        JSONObject result=reimbursementDetail(billRow);Json.put(result,"marked",marked);
        Json.put(result,"prior_not_total",priorTotal);Json.put(result,"prior_not_budget",priorBudget);
        return result;
    }

    public JSONObject receiveReimbursement(long billRow,JSONObject input,String operationId) {
        JSONObject value=Json.copy(input);String key=uuid(operationId);
        AccountingTargets.requireResolved(store,value,null);
        return AccountingCommitScope.run(store,()->{
            JSONObject old=prior(key,"reimbursement_receive",billRow);if(old!=null)return old;
            OriginalBillRecord raw=bill(billRow);if(!raw.reimbursement)throw new IllegalArgumentException("请先将账单标记为报销");
            OriginalReimbursementRepository repository=new OriginalReimbursementRepository(store);
            OriginalReimbursementRepository.Snapshot aggregate=repository.firstByParent(OWNER,raw.billId);
            if(aggregate==null||aggregate.data.originalDeleted)throw new IllegalArgumentException("报销记录不存在");
            long amount=positive(value,"amount"),remaining=Math.max(0,minor(raw.cost)-minor(aggregate.data.reimbursementNumber));
            if(amount>remaining)throw new IllegalArgumentException("报销到账不能超过未报销金额");
            JSONObject receiver=funds(value.optLong("account_id"));long when=time(value);
            OriginalReimbursementRepository.Data data=aggregate.data.copy();data.reimbursementAssetSemanticId=receiver.optLong("semantic_id");
            data.addNumber(number(amount));data.end=amount==remaining;
            String text=receiver.optLong("semantic_id")+":"+BigDecimal.valueOf(amount,2).toPlainString()+":"+when;
            if(!remark(value).isEmpty())text+=":"+remark(value).replace(":","：");
            data.appendRaw(text);repository.update(aggregate.rowId,OWNER,data,System.currentTimeMillis());
            new OriginalFinancialOrchestrator(store,OWNER,OWNER).changeAsset(receiver.optLong("semantic_id"),number(amount),false,"报销到账："+safe(raw.remark));
            raw.reimbursementEnd=data.end;raw.updateTime=System.currentTimeMillis();ContentValues projection=projection(raw);projection.put("settled",data.end?1:0);saveBillRaw(raw,projection);
            OriginalFinancialEffects effects=new OriginalFinancialEffects(store,OWNER,OWNER);effects.parentUpdated(raw,true);effects.assetCreatedOrEdited(data.assetSemanticId);
            JSONObject result=reimbursementDetail(billRow);Json.put(result,"received_amount",amount);Json.put(result,"received_raw",text);Json.put(result,"time",when);
            record(key,"reimbursement_receive","bill",billRow,raw.billId,store.transaction(billRow).optLong("book_id"),null,result,false);return result;
        });
    }

    public JSONObject reimbursementDetail(long billRow) {
        synchronized(store){OriginalBillRecord raw=new OriginalBillRepository(store).byLocalRowId(billRow);JSONObject value=store.transaction(billRow);
            if(raw==null||value==null||raw.userId!=OWNER)throw new IllegalArgumentException("报销账单已不存在");
            OriginalReimbursementRepository.Snapshot aggregate=new OriginalReimbursementRepository(store).firstByParent(OWNER,raw.billId);
            Json.put(value,"entry_kind","bill");Json.put(value,"entry_id",billRow);Json.put(value,"entry_key","bill:"+billRow);
            Json.put(value,"reimbursement_received",aggregate==null?0:minor(aggregate.data.reimbursementNumber));
            Json.put(value,"outstanding",Math.max(0,minor(raw.cost)-(aggregate==null?0:minor(aggregate.data.reimbursementNumber))));
            Json.put(value,"marked",raw.reimbursement);return value;}
    }

    /** Keep a marked expense's received money and reporting policy valid during ordinary edits. */
    public static JSONObject prepareBillEdit(ReferenceStore store,long billRow,JSONObject input) {
        JSONObject value=Json.copy(input);AccountingTargets.requireResolved(store,value,null);
        synchronized(store){
            OriginalBillRecord raw=new OriginalBillRepository(store).byLocalRowId(billRow);
            if(raw!=null&&raw.reimbursement){
                if(!"expense".equals(value.optString("kind")))throw new IllegalArgumentException("报销账单必须保留支出方向");
                OriginalReimbursementRepository.Snapshot aggregate=new OriginalReimbursementRepository(store).firstByParent(OWNER,raw.billId);
                long received=aggregate==null?0:minor(aggregate.data.reimbursementNumber);
                if(positive(value,"amount")<received)throw new IllegalArgumentException("账单金额不能小于已报销到账金额");
                Json.put(value,"not_total",true);Json.put(value,"not_budget",true);
            }
            return value;
        }
    }

    public JSONObject editReimbursementBill(long billRow,JSONObject input,String operationId) {
        JSONObject value=Json.copy(input);String key=uuid(operationId);AccountingTargets.requireResolved(store,value,null);
        return AccountingCommitScope.run(store,()->{
            JSONObject old=prior(key,"reimbursement_edit",billRow);if(old!=null)return reimbursementDetail(billRow);
            OriginalBillRecord raw=bill(billRow);if(!raw.reimbursement)throw new IllegalArgumentException("此账单未标记报销");
            JSONObject prepared=prepareBillEdit(store,billRow,value);
            Json.put(prepared,"financial_type","");Json.put(prepared,"fee",0);Json.put(prepared,"related_id",0);
            store.saveTransaction(prepared,billRow,null);
            raw=bill(billRow);OriginalReimbursementRepository repository=new OriginalReimbursementRepository(store);
            OriginalReimbursementRepository.Snapshot aggregate=repository.firstByParent(OWNER,raw.billId);
            if(aggregate!=null){OriginalReimbursementRepository.Data data=aggregate.data.copy();data.end=minor(raw.cost)==minor(data.reimbursementNumber);repository.update(aggregate.rowId,OWNER,data,System.currentTimeMillis());
                raw.reimbursementEnd=data.end;ContentValues projection=projection(raw);projection.put("settled",data.end?1:0);saveBillRaw(raw,projection);
                new OriginalFinancialEffects(store,OWNER,OWNER).assetCreatedOrEdited(data.assetSemanticId);}
            JSONObject result=reimbursementDetail(billRow);record(key,"reimbursement_edit","bill",billRow,raw.billId,store.transaction(billRow).optLong("book_id"),null,result,false);return result;
        });
    }

    /** Called in the same purge transaction, after the UI has passed its ordinary association gates. */
    public static void purgeBillReceipts(ReferenceStore store,long billRow) {
        if(!AccountingCommitScope.active(store))throw new IllegalStateException("账单凭据清理需要原子记账操作");
        store.getWritableDatabase().delete(TABLE,"entity_kind='bill' AND entity_row_id=?",new String[]{""+billRow});
    }

    private JSONObject debt(JSONObject value,boolean borrow,JSONObject book) {
        long id=value.optLong("to_account_id",value.optLong("debt_account_id"));
        if(id>0){JSONObject selected=account(id);requireDebt(selected,borrow);return selected;}
        String name=value.optString("counterparty").trim();if(name.isEmpty())name=remark(value).trim();
        if(name.isEmpty())throw new IllegalArgumentException("请选择应收或应付账户，或填写借贷对象");
        if(name.length()>100)name=name.substring(0,100);
        AssetRepository.Definition definition=new AssetRepository.Definition(name,borrow?7:6,null);definition.intoTotalAsset=true;
        definition.showBook.add(book.optLong("semantic_id"));
        long row=store.assetRepository().createOriginal(OWNER,definition);JSONObject created=account(row);
        new OriginalFinancialEffects(store,OWNER,OWNER).assetCreatedOrEdited(created.optLong("semantic_id"));return created;
    }
    private JSONObject reimbursementAccount(){
        JSONObject existing=store.one("SELECT * FROM accounts WHERE asset_origin='original' AND owner_semantic_id=? AND asset_type=9 AND archived=0 ORDER BY id LIMIT 1",""+OWNER);
        if(existing!=null){if(!existing.isNull("asset_currency")&&!existing.optString("asset_currency").isEmpty())throw new IllegalArgumentException("报销账户币种不受支持");return existing;}
        AssetRepository.Definition value=new AssetRepository.Definition("待报销",9,null);value.intoTotalAsset=true;
        return store.assetRepository().byRowId(store.assetRepository().createOriginal(OWNER,value));
    }

    private void applyPrincipal(OriginalLendRecord raw,boolean reverse) {
        JSONObject debt=assetBySemantic(raw.assetId),funds=assetBySemantic(raw.repaymentAssetId);different(debt,funds);
        double amount=Math.abs(raw.number);boolean cashReduce=raw.type==1||raw.type==4;
        if(raw.type<1||raw.type>4)throw new IllegalArgumentException("借贷类型不支持");
        OriginalFinancialOrchestrator financial=new OriginalFinancialOrchestrator(store,OWNER,OWNER);
        String description=(reverse?"撤销":"")+(raw.type==1?"借出":raw.type==2?"收回借款":raw.type==3?"借入":"归还借款")+"："+safe(raw.remark);
        financial.changeAsset(raw.repaymentAssetId,amount,cashReduce!=reverse,description);
        financial.changeAsset(raw.assetId,amount,cashReduce==reverse,description);
    }
    private long interestBill(OriginalLendRecord raw,JSONObject book,long interest) {
        if(interest==0)return 0;
        boolean income=(raw.type==2)==(interest>0);OriginalBillRecord bill=new OriginalBillRecord();
        bill.parentCategoryId=income?9:99;bill.childCategoryId=income?911:0;
        bill.cost=number(Math.abs(interest));bill.assetId=raw.repaymentAssetId;bill.bookId=book.optLong("semantic_id");bill.userId=OWNER;
        bill.time=raw.outTime;bill.recordTime=System.currentTimeMillis();bill.billType=2;bill.currencyInfo="";
        bill.remark=(raw.type==2?"借出回款利息：":"借入还款利息：")+safe(raw.remark);
        OriginalBillRepository.SaveResult save=new OriginalFinancialOrchestrator(store,OWNER,OWNER).saveNormal(bill,OriginalTransferConfirmation.projectFee(store,bill));
        if(!save.saved)throw new IllegalStateException("利息账单未能保存");raw.billId=bill.billId;raw.generalServiceBill=true;return bill.localRowId;
    }
    /** Replacement purges the superseded Bill; reversible trash records who removed it. */
    private boolean removeInterest(JSONObject value,boolean purge) {
        long fee=value.optLong("fee_bill_id");if(fee<=0)return false;
        JSONObject bill=store.transaction(fee);if(bill==null)throw new IllegalArgumentException("关联利息账单已不存在");
        boolean removed=bill.optInt("deleted")==0;
        if(removed)store.trash(fee,true);
        if(purge)store.purge(fee);
        return removed;
    }
    private void saveLend(OriginalLendRecord lend,boolean create) {
        OriginalLendActions actions=new OriginalLendActions(store,OWNER);OriginalLendActions.Result result=create?actions.add(lend):actions.update(lend);
        if(!result.save.saved)throw new IllegalStateException("借贷记录保存未完成");new OriginalFinancialEffects(store,OWNER,OWNER).lendChanged(lend,true);
    }
    private long paid(long row) {
        long total=0;for(JSONObject receipt:store.query("SELECT entity_row_id FROM accounting_financial_operations WHERE parent_lend_row_id=? AND action IN ('collection','repayment') AND deleted=0",""+row)){
            OriginalLendRecord raw=lend(receipt.optLong("entity_row_id"),false);total=Math.addExact(total,minor(raw.number));}
        return total;
    }
    private long outstanding(long row){OriginalLendRecord parent=lend(row,true);return Math.max(0,minor(parent.number)-paid(row));}
    private JSONObject loanReceipt(long row){JSONObject value=store.one("SELECT * FROM accounting_financial_operations WHERE entity_kind='lend' AND entity_row_id=? AND action IN "+LOAN_ACTIONS,""+row);if(value==null)throw new IllegalArgumentException("借贷记账凭据不存在");return value;}
    private OriginalLendRecord lend(long row,boolean includeDeleted){
        OriginalLendRecord value=new OriginalLendRepository(store).byLocalRowId(row);
        if(value==null||value.userId!=OWNER||(!includeDeleted&&value.originalDeleted))throw new IllegalArgumentException("借贷记录不存在或已删除");return value;
    }
    private JSONObject loanDto(OriginalLendRecord raw,long book,long funds,long debt,long parent,long fee,JSONObject input){
        String action=raw.type==1?"loan":raw.type==2?"collection":raw.type==3?"borrow":"repayment";
        JSONArray tags=input.optJSONArray("tag_ids");if(tags==null)tags=new JSONArray();
        for(int index=0;index<tags.length();index++){Object tag=tags.opt(index);if(!(tag instanceof Integer||tag instanceof Long)||store.one("SELECT id FROM tags WHERE id=?",""+((Number)tag).longValue())==null)throw new IllegalArgumentException("借贷标签不存在");}
        return Json.object("entry_kind","lend","entry_id",raw.localRowId,"entry_key","lend:"+raw.localRowId,"id",raw.localRowId,
                "entry_owner_semantic_id",OWNER,"entry_semantic_id",raw.lendId,"book_id",book,"kind","transfer",
                "financial_type",raw.type==1||raw.type==2?"lend":"borrow","operation_kind",action,"lend_type",raw.type,
                "amount",minor(raw.number),"currency","CNY","interest",minorSigned(raw.interest),"fee",0,"fee_bill_id",fee==0?null:fee,
                "account_id",funds,"from_account_id",funds,"to_account_id",debt,"related_id",parent,"time",raw.outTime,"in_time",raw.inTime,
                "remark",safe(raw.remark),"merchant",input.optString("counterparty"),"counterparty",input.optString("counterparty"),"deleted",raw.originalDeleted?1:0,
                "settled",parent==0?0:1,"outstanding",parent==0?minor(raw.number):0,"not_total",1,"not_budget",1,
                "category_name",raw.type==1?"借出":raw.type==2?"收回借款":raw.type==3?"借入":"归还借款","icon","ic_other","tags","","tag_ids",tags,"discount",0,"attachments",input.optString("attachments","[]"));
    }
    private void updateLoanReceipt(JSONObject receipt,JSONObject value,boolean deleted){
        JSONObject stored=Json.copy(value);Json.put(stored,"deleted",deleted?1:0);Json.put(stored,"operation_id",receipt.optString("operation_id"));
        ContentValues fields=new ContentValues();fields.put("payload",stored.toString());fields.put("deleted",deleted?1:0);
        AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update(TABLE,fields,"operation_id=?",new String[]{receipt.optString("operation_id")}));
    }
    private JSONObject prior(String key,String action,long parent){
        JSONObject old=store.one("SELECT * FROM accounting_financial_operations WHERE operation_id=?",key);
        if(old!=null){JSONObject value=Json.parse(old.optString("payload"));
            boolean same=action.equals(old.optString("action"));
            if(parent>0)same&=(action.equals("collection")||action.equals("repayment"))?old.optLong("parent_lend_row_id")==parent:old.optLong("entity_row_id")==parent;
            if(!same)throw new IllegalArgumentException("此保存标识已用于其他操作");return value;}
        if(store.one("SELECT operation_id FROM accounting_operations WHERE operation_id=?",key)!=null||store.one("SELECT operation_id FROM accounting_entry_edits WHERE operation_id=?",key)!=null||store.one("SELECT id FROM transactions WHERE manual_operation_id=?",key)!=null)
            throw new IllegalArgumentException("此保存标识已用于其他记账操作");
        return null;
    }
    private void record(String key,String action,String kind,long row,long semantic,long book,Long parent,JSONObject payload,boolean deleted){
        Json.put(payload,"operation_id",key);ContentValues fields=new ContentValues();fields.put("operation_id",key);fields.put("action",action);fields.put("entity_kind",kind);
        fields.put("entity_row_id",row);fields.put("owner_semantic_id",OWNER);fields.put("semantic_id",semantic);fields.put("book_id",book);fields.put("parent_lend_row_id",parent);
        fields.put("payload",payload.toString());fields.put("created",System.currentTimeMillis());fields.put("deleted",deleted?1:0);
        if(store.getWritableDatabase().insertOrThrow(TABLE,null,fields)==-1)throw new IllegalStateException("记账凭据未能保存");
    }
    private JSONObject lastMark(long billRow){JSONObject receipt=store.one("SELECT payload FROM accounting_financial_operations WHERE entity_kind='bill' AND entity_row_id=? AND action IN ('reimbursement_mark','reimbursement_bill') ORDER BY created DESC,rowid DESC LIMIT 1",""+billRow);return receipt==null?null:Json.parse(receipt.optString("payload"));}

    private OriginalBillRecord bill(long row){OriginalBillRecord raw=new OriginalBillRepository(store).byLocalRowId(row);JSONObject host=store.transaction(row);
        if(raw==null||host==null||host.optInt("deleted")!=0||raw.userId!=OWNER||raw.fromUserId!=0&&raw.fromUserId!=OWNER)
            throw new IllegalArgumentException("请选择当前离线身份的有效原账单");
        book(host.optLong("book_id"));if(raw.currencyInfo!=null&&!raw.currencyInfo.isEmpty())throw new IllegalArgumentException("外币账单请先使用本币流程");
        if(!Double.isFinite(raw.cost)||raw.cost<0)throw new IllegalArgumentException("账单金额无效");return raw;
    }
    private ContentValues projection(OriginalBillRecord raw){
        JSONObject old=store.transaction(raw.localRowId);if(old==null)throw new IllegalArgumentException("原账单不存在");
        ContentValues fields=new ContentValues();fields.put("amount",minor(raw.cost));fields.put("not_total",raw.notIntoTotal?1:0);fields.put("not_budget",raw.notIntoBudget?1:0);
        fields.put("book_id",old.optLong("book_id"));fields.put("kind",raw.parentCategoryId==9?"income":"expense");fields.put("currency","CNY");
        if(raw.remark!=null)fields.put("remark",raw.remark);fields.put("time",raw.time);
        return fields;
    }
    private void saveBillRaw(OriginalBillRecord raw,ContentValues projection){if(!new OriginalBillRepository(store).trySaveOriginalRaw(raw,projection).saved)throw new IllegalStateException("账单保存未完成");}
    private JSONObject book(long row){JSONObject value=store.book(row);if(value==null||value.optInt("archived")!=0||!"CNY".equals(value.optString("currency"))||value.isNull("semantic_id")||value.isNull("owner_semantic_id")||value.optInt("owner_semantic_id")!=OWNER)throw new IllegalArgumentException("请选择当前离线人民币账本");return value;}
    private JSONObject account(long row){JSONObject value=store.assetRepository().byRowId(row);if(value==null||!AssetRepository.ORIGINAL.equals(value.optString("asset_origin"))||value.isNull("owner_semantic_id")||value.optInt("owner_semantic_id")!=OWNER||value.isNull("semantic_id")||value.optInt("archived")!=0||(!value.isNull("asset_currency")&&!value.optString("asset_currency").isEmpty()))throw new IllegalArgumentException("请选择当前离线身份的有效本币账户");return value;}
    private JSONObject assetBySemantic(long semantic){JSONObject value=store.assetRepository().p(semantic,OWNER);if(value==null)throw new IllegalArgumentException("关联账户已不存在");return account(value.optLong("id"));}
    private JSONObject funds(long row){JSONObject value=account(row);int type=value.optInt("asset_type");String host=value.optString("type");if(type==6||type==7||type==8||type==9||host.equals("receivable")||host.equals("debt"))throw new IllegalArgumentException("请选择实际收付款资金账户");return value;}
    private static void requireDebt(JSONObject account,boolean borrow){int type=account.optInt("asset_type");String host=account.optString("type");if(borrow?type!=7&&!host.equals("debt"):type!=6&&!host.equals("receivable"))throw new IllegalArgumentException(borrow?"请选择应付或借入账户":"请选择应收或借出账户");}
    private static void different(JSONObject first,JSONObject second){if(first.optLong("id")==second.optLong("id"))throw new IllegalArgumentException("资金账户与债务账户必须不同");}
    private static long positive(JSONObject value,String field){long amount=signed(value,field);if(amount<=0)throw new IllegalArgumentException("请输入大于零的金额");return amount;}
    private static long signed(JSONObject value,String field){Object raw=value.opt(field);if(raw==null||raw==JSONObject.NULL)return 0;if(!(raw instanceof Integer||raw instanceof Long))throw new IllegalArgumentException("金额必须使用分的整数");long amount=((Number)raw).longValue();if(amount< -MAX||amount>MAX)throw new IllegalArgumentException("金额超出范围");return amount;}
    private static long time(JSONObject value){long when=value.optLong("time");if(when<=0||when>8640000000000000L)throw new IllegalArgumentException("记账时间无效");return when;}
    private static String remark(JSONObject value){String text=value.optString("remark");if(text.length()>2000)throw new IllegalArgumentException("备注过长");return text;}
    private static String safe(String value){return value==null?"":value;}
    private static double number(long minor){return BigDecimal.valueOf(minor,2).doubleValue();}
    private static long minor(double value){if(!Double.isFinite(value)||value<0)throw new IllegalArgumentException("原记录金额无效");return minorSigned(value);}
    private static long minorSigned(double value){if(!Double.isFinite(value))throw new IllegalArgumentException("原记录金额无效");long minor=BigDecimal.valueOf(value).movePointRight(2).setScale(0,RoundingMode.HALF_UP).longValueExact();if(minor< -MAX||minor>MAX)throw new IllegalArgumentException("原记录金额超出范围");return minor;}
    private static String uuid(String value){if(value==null)throw new IllegalArgumentException("保存操作标识缺失");try{if(!UUID.fromString(value).toString().equals(value))throw new IllegalArgumentException();return value;}catch(IllegalArgumentException invalid){throw new IllegalArgumentException("保存操作标识无效",invalid);}}

    /** Validate host links without inventing a raw Lend parent property or replaying balances. */
    public static void validateRestored(ReferenceStore store){
        synchronized(store){AccountingFinancialFlows flows=new AccountingFinancialFlows(store);
            for(JSONObject row:store.query("SELECT * FROM accounting_financial_operations")){
                uuid(row.optString("operation_id"));JSONObject payload=Json.parse(row.optString("payload"));long local=row.optLong("entity_row_id");
                if(local<=0||row.optLong("owner_semantic_id")!=OWNER||store.book(row.optLong("book_id"))==null||row.optLong("book_id")!=payload.optLong("book_id")||!row.optString("operation_id").equals(payload.optString("operation_id")))throw new IllegalArgumentException("金融操作凭据无效");
                if(store.one("SELECT operation_id FROM accounting_operations WHERE operation_id=?",row.optString("operation_id"))!=null||store.one("SELECT operation_id FROM accounting_entry_edits WHERE operation_id=?",row.optString("operation_id"))!=null||store.one("SELECT id FROM transactions WHERE manual_operation_id=?",row.optString("operation_id"))!=null)throw new IllegalArgumentException("金融操作标识冲突");
                if(row.optString("entity_kind").equals("lend")){
                    OriginalLendRecord raw=flows.lend(local,true);
                    String action=row.optString("action");
                    if(!(action.equals("loan")&&raw.type==1||action.equals("borrow")&&raw.type==3||action.equals("collection")&&raw.type==2||action.equals("repayment")&&raw.type==4||action.equals("loan_edit")))throw new IllegalArgumentException("借贷操作类型不匹配");
                    if(raw.lendId!=row.optLong("semantic_id")||payload.optLong("entry_id")!=local||!"lend".equals(payload.optString("entry_kind"))||!payload.optString("entry_key").equals("lend:"+local))throw new IllegalArgumentException("借贷操作身份无效");
                    if(minor(raw.number)<=0||raw.fromCost!=raw.number)throw new IllegalArgumentException("借贷本金无效");
                    long parent=row.optLong("parent_lend_row_id");
                    if(parent!=0){OriginalLendRecord origin=flows.lend(parent,true);flows.loanReceipt(parent);if(!(origin.type==1&&raw.type==2||origin.type==3&&raw.type==4)||raw.assetId!=origin.assetId||!raw.originalDeleted&&origin.originalDeleted)throw new IllegalArgumentException("借贷结清关联无效");}
                    else if(raw.type==2||raw.type==4)throw new IllegalArgumentException("借贷结清缺少原本金记录");
                    if(!row.optString("action").equals("loan_edit")&&(raw.originalDeleted!=(row.optInt("deleted")!=0)||raw.type<1||raw.type>4))throw new IllegalArgumentException("借贷回收状态无效");
                }else{
                    if(row.optString("action").equals("loan")||row.optString("action").equals("borrow")||row.optString("action").equals("collection")||row.optString("action").equals("repayment")||row.optString("action").equals("loan_edit"))throw new IllegalArgumentException("借贷凭据不能指向普通账单");
                    OriginalBillRecord bill=new OriginalBillRepository(store).byLocalRowId(local);
                    if(bill==null||bill.userId!=OWNER||bill.billId!=row.optLong("semantic_id"))throw new IllegalArgumentException("金融操作原账单关联无效");
                }
            }
            for(JSONObject row:store.query("SELECT entity_row_id FROM accounting_financial_operations WHERE action IN ('loan','borrow')")){
                long local=row.optLong("entity_row_id");if(flows.paid(local)>minor(flows.lend(local,true).number))throw new IllegalArgumentException("结清本金超过原借贷金额");
            }
        }
    }
}
