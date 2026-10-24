package com.wangc.bill.reference.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wangc.bill.reference.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/** User-facing record operations share the same accounting commands as the editor. */
final class RecordActions {
    private RecordActions() {}
    static void bill(Context context,JSONObject selected){
        JSONObject owner=AccountingLinks.ownerOfBill(ReferenceApplication.store(),selected.optLong("id"));
        if(owner!=null){Ui.record(context,owner);return;}
        JSONObject row=ReferenceApplication.store().transaction(selected.optLong("id"));
        if(row==null){Ui.toast(context,"账单不存在");return;}
        String currency=row.optString("currency","CNY");
        String title=Money.text(row.optLong("amount"),currency)+" · "+row.optString("category","账单");
        String detail=Ui.date(row.optLong("time"),"yyyy-MM-dd HH:mm")
                +(row.optString("merchant").isEmpty()?"":"\n商户："+row.optString("merchant"))
                +(row.optString("account").isEmpty()?"":"\n账户："+row.optString("account"))
                +(row.optString("remark").isEmpty()?"":"\n备注："+row.optString("remark"));
        new AlertDialog.Builder(context).setTitle(title).setMessage(detail).setNegativeButton("关闭",null)
                .setPositiveButton("编辑",(dialog,which)->Ui.edit(context,row.optLong("id"),null))
                .setNeutralButton("更多",(dialog,which)->billMenu(context,row)).show();
    }
    private static void billMenu(Context context,JSONObject row){
        List<String> actions=new ArrayList<>();boolean expense=row.optString("kind").equals("expense");
        boolean marked=row.optString("financial_type").equals("reimbursement")||row.optInt("bill_reimbursement")!=0;
        if(expense&&row.optLong("amount")>0)actions.add("记录退款");
        if(expense)actions.add(marked?"取消待报销":"标记待报销");
        if(marked)actions.add("记录报销到账");
        if(Json.array(row.optString("attachments","[]")).length()>0)actions.add("查看附件");
        actions.add("删除账单");
        new AlertDialog.Builder(context).setTitle("账单操作").setItems(actions.toArray(new String[0]),(dialog,index)->{
            String action=actions.get(index);
            if(action.equals("记录退款"))money(context,row,"退款金额",false,(input,uuid)->new AccountingFinancialFlows(ReferenceApplication.store()).refundBill(row.optLong("id"),input,uuid));
            else if(action.equals("记录报销到账")){
                JSONObject remaining=new AccountingFinancialFlows(ReferenceApplication.store()).reimbursementDetail(row.optLong("id"));JSONObject source=Json.copy(row);Json.put(source,"outstanding",remaining.optLong("outstanding"));
                money(context,source,"报销到账金额",false,(input,uuid)->new AccountingFinancialFlows(ReferenceApplication.store()).receiveReimbursement(row.optLong("id"),input,uuid));
            }
            else if(action.equals("标记待报销")||action.equals("取消待报销")){
                try{new AccountingFinancialFlows(ReferenceApplication.store()).markReimbursement(row.optLong("id"),!marked,UUID.randomUUID().toString());Ui.refresh(context);Ui.toast(context,marked?"已取消待报销":"已标记待报销");}
                catch(Exception error){Ui.error(context,error);}
            }else if(action.equals("查看附件"))attachments(context,Json.array(row.optString("attachments","[]")));
            else Ui.confirm(context,"删除账单","余额变更会一并撤销，账单可在回收站恢复。",()->{ReferenceApplication.store().trash(row.optLong("id"),true);Ui.refresh(context);});
        }).show();
    }
    static void settle(Context context,JSONObject loan){
        money(context,loan,loan.optInt("lend_type")==1?"收回借款本金":"偿还借款本金",true,
                (input,uuid)->new AccountingFinancialFlows(ReferenceApplication.store()).settleLoan(loan.optLong("entry_id"),input,uuid));
    }
    interface Save {JSONObject save(JSONObject input,String uuid);}
    private static void money(Context context,JSONObject source,String title,boolean interest,Save save){
        String currency=source.optString("currency","CNY");long book=source.optLong("book_id");
        LinearLayout content=Ui.column(context);int pad=Ui.dp(context,18);content.setPadding(pad,pad,pad,pad);
        EditText amount=new EditText(context);amount.setSingleLine(true);amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amount.setHint(title);amount.setText(Money.text(source.optLong("outstanding",source.optLong("amount")),currency));content.addView(amount);
        EditText extra=new EditText(context);extra.setSingleLine(true);extra.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);extra.setHint("利息（可填0）");extra.setText("0");if(interest)content.addView(extra);
        EditText remark=new EditText(context);remark.setHint("备注");content.addView(remark);
        long[] account={source.optLong("account_id")};
        TextView accountLabel=Ui.label(context,"",15);accountLabel.setPadding(0,pad,0,pad);content.addView(accountLabel);
        Runnable showAccount=()->{JSONObject selected=ReferenceApplication.store().assetRepository().byRowId(account[0]);accountLabel.setText("到账/付款账户："+(selected==null?"请选择":selected.optString("name")));};
        showAccount.run();accountLabel.setOnClickListener(view->fundingAccount(context,book,id->{account[0]=id;showAccount.run();}));
        String uuid=UUID.randomUUID().toString();
        AlertDialog dialog=new AlertDialog.Builder(context).setTitle(title).setView(content).setNegativeButton("取消",null).setPositiveButton("保存",null).create();
        dialog.setOnShowListener(view->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button->{
            try{JSONObject input=Json.object("book_id",book,"amount",Money.positive(amount.getText().toString(),currency),"interest",interest?Money.signed(extra.getText().toString(),currency):0,
                    "account_id",account[0],"time",System.currentTimeMillis(),"remark",remark.getText().toString());
                save.save(input,uuid);dialog.dismiss();Ui.refresh(context);Ui.toast(context,"已保存");
            }catch(Exception error){Ui.error(context,error);}
        }));dialog.show();
    }
    interface Account {void selected(long row);}
    private static void fundingAccount(Context context,long book,Account callback){
        List<JSONObject> accounts=new ArrayList<>();
        for(JSONObject value:ReferenceApplication.store().selectableAccounts(book)){
            int type=value.optInt("asset_type");String host=value.optString("type");
            if(AssetRepository.ORIGINAL.equals(value.optString("asset_origin"))&&type!=6&&type!=7&&type!=8&&type!=9&&!host.equals("receivable")&&!host.equals("debt"))accounts.add(value);
        }
        String[] names=new String[accounts.size()+1];names[0]="无账户";for(int i=0;i<accounts.size();i++)names[i+1]=accounts.get(i).optString("name");
        new AlertDialog.Builder(context).setTitle("资金账户").setItems(names,(dialog,index)->callback.selected(index==0?0:accounts.get(index-1).optLong("id"))).show();
    }
    private static void attachments(Context context,JSONArray files){
        String[] names=new String[files.length()];for(int i=0;i<names.length;i++)names[i]=files.optJSONObject(i).optString("name","附件");
        new AlertDialog.Builder(context).setTitle("附件").setItems(names,(dialog,index)->LocalFiles.openAttachment(context,files.optJSONObject(index))).show();
    }
}
