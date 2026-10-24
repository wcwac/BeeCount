package com.wangc.bill.reference;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wangc.bill.reference.ui.Ui;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Restored visible X/Y/Z detail path; original protected onCreate remains an explicit gap. */
public final class OriginalCreditStatementActivity extends Activity implements OriginalFinancialEffects.Listener {
    private OriginalCreditBill bill;private int owner,bookOwner;private View body;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);bill=Build.VERSION.SDK_INT>=33?getIntent().getParcelableExtra("CreditBill",OriginalCreditBill.class):legacyBill();owner=getIntent().getIntExtra("owner",0);bookOwner=getIntent().getIntExtra("bookOwner",0);if(bill==null){finish();return;}
        LinearLayout root=Ui.column(this);root.addView(Ui.toolbar(this,"本期账单","消费分期",()->{org.json.JSONObject account=ReferenceApplication.store().assetRepository().p(bill.getAssetId(),owner);if(account!=null)com.wangc.bill.reference.ui.InstalmentScreens.chooseConsumption(this,account.optLong("id"));}));body=Ui.layout(this,"activity_credit_current_bill");root.addView(body,new LinearLayout.LayoutParams(-1,0,1));Ui.content(this,root);OriginalFinancialEffects.subscribe(ReferenceApplication.store(),this);render();onFinancialEvent(OriginalFinancialEffects.Event.CREDIT_CHANGED);
    }
    @SuppressWarnings("deprecation") private OriginalCreditBill legacyBill(){return getIntent().getParcelableExtra("CreditBill");}
    private void render(){
        List<OriginalCreditBillInfo> ordinary=bill.getBillInfoList()==null?new ArrayList<>():new ArrayList<>(bill.getBillInfoList()),instalment=bill.getInstalmentInfoList()==null?new ArrayList<>():new ArrayList<>(bill.getInstalmentInfoList());double income=0,pay=0,instalmentTotal=0;for(OriginalCreditBillInfo row:ordinary){if(row.getNumber()>=0)income+=row.getNumber();else pay+=row.getNumber();}for(OriginalCreditBillInfo row:instalment)instalmentTotal+=row.getNumber();Collections.sort(ordinary);Collections.sort(instalment);
        Ui.text(body,"instalment_number","分期金额："+OriginalCreditFormatting.amount(instalmentTotal));Ui.text(body,"bill_income","收入："+OriginalCreditFormatting.amount(income));Ui.text(body,"bill_pay","支出："+OriginalCreditFormatting.amount(pay));Ui.show(body,"instalment_layout",!instalment.isEmpty());Ui.show(body,"bill_layout",!ordinary.isEmpty());bind("bill_list",ordinary);bind("instalment_list",instalment);
    }
    private void bind(String id,List<OriginalCreditBillInfo> rows){RecyclerView view=Ui.view(body,id);view.setLayoutManager(new LinearLayoutManager(this));view.setNestedScrollingEnabled(false);view.setHasFixedSize(true);view.setAdapter(new Details(rows));}
    @Override public void onFinancialEvent(OriginalFinancialEffects.Event event){if(bill==null||isFinishing())return;for(OriginalCreditBill updated:OriginalFinancialEffects.creditStatements(ReferenceApplication.store(),owner,bookOwner,bill.getAssetId()))if(java.util.Objects.equals(updated.getTitle(),bill.getTitle())){bill=updated;render();return;}if(event==OriginalFinancialEffects.Event.CREDIT_CHANGED&&OriginalFinancialEffects.creditStatus(ReferenceApplication.store(),owner,bookOwner,bill.getAssetId())==OriginalFinancialEffects.CreditStatus.READY){bill.setBillInfoList(new ArrayList<>());bill.setInstalmentInfoList(new ArrayList<>());render();}}
    @Override protected void onDestroy(){OriginalFinancialEffects.unsubscribe(ReferenceApplication.store(),this);super.onDestroy();}
    private final class Details extends RecyclerView.Adapter<Holder>{
        private final List<OriginalCreditBillInfo> rows;Details(List<OriginalCreditBillInfo> rows){this.rows=rows;}
        @Override public int getItemCount(){return rows.size();}
        @Override public Holder onCreateViewHolder(ViewGroup parent,int type){return new Holder(Ui.layout(parent.getContext(),"item_credit_bill_info",parent));}
        @Override public void onBindViewHolder(Holder holder,int index){OriginalCreditBillInfo row=rows.get(index);View view=holder.itemView;Ui.text(view,"category",row.getTitle());Ui.text(view,"remark",row.getRemark());Ui.show(view,"remark",row.getRemark()!=null&&!row.getRemark().isEmpty());Ui.text(view,"cost",OriginalCreditFormatting.amount(row.getNumber()));Ui.text(view,"date",OriginalInstalmentSchedule.format(row.getDate(),"MM月dd日"));Ui.image(view,"icon",row.getIcon());Ui.hide(view,"refund","reimbursement");view.setOnClickListener(row.getBillId()>0&&row.getNumber()<0?v->com.wangc.bill.reference.ui.InstalmentScreens.openCreditItem(v.getContext(),row.getBillId(),owner,bookOwner):null);}
    }
    private static final class Holder extends RecyclerView.ViewHolder{Holder(View view){super(view);}}
}
