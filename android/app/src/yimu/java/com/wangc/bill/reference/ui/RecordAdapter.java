package com.wangc.bill.reference.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.wangc.bill.reference.Money;
import com.wangc.bill.reference.OriginalSettings;
import com.wangc.bill.reference.ReferenceApplication;
import org.json.JSONObject;
import java.util.List;

public final class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.Holder> {
    public interface Click { void record(JSONObject value); }
    private final List<JSONObject> rows;
    private final View header;
    private final Click click;
    public boolean allowTrash=true;
    /** Optional paged owner reloads complete day totals after an inline deletion. */
    public Runnable historyReload;
    public RecordAdapter(List<JSONObject> rows, View header, Click click) { this.rows=rows;this.header=header;this.click=click; }
    @Override public int getItemCount(){return rows.size()+(header==null?0:1);}
    @Override public int getItemViewType(int position){return header!=null&&position==0?0:1;}
    @Override public Holder onCreateViewHolder(ViewGroup parent,int type){return new Holder(type==0?header:Ui.layout(parent.getContext(),"item_bill_main_list"));}
    @Override public void onBindViewHolder(Holder holder,int position){
        if(getItemViewType(position)==0)return;
        int at=position-(header==null?0:1);JSONObject row=rows.get(at);View v=holder.itemView;String kind=row.optString("kind"),currency=row.optString("currency","CNY");long time=row.optLong("time");
        Ui.hide(v,"radio_button","reimbursement","refund","discount","old_cost","asset_icon","group_asset_list","file_click_layout","tag_one","tag_two","tag_three","tag_four","group_tag","has_file_tag","create_user");
        android.widget.TextView oldCost=Ui.view(v,"old_cost");oldCost.setText("");oldCost.setPaintFlags(oldCost.getPaintFlags()&~Paint.STRIKE_THRU_TEXT_FLAG);
        boolean first=at==0||!Ui.date(time,"yyyyMMdd").equals(Ui.date(rows.get(at-1).optLong("time"),"yyyyMMdd"));Ui.show(v,"time_layout",first);
        if(first){Ui.text(v,"date_info",Ui.date(time,"MM月dd日"));Ui.text(v,"week_info",Ui.date(time,"EEEE"));long income=0,pay=0;for(int i=at;i<rows.size();i++){JSONObject d=rows.get(i);if(!Ui.date(time,"yyyyMMdd").equals(Ui.date(d.optLong("time"),"yyyyMMdd")))break;if(d.optInt("not_total")==0){if(d.optString("kind").equals("income"))income+=d.optLong("amount");if(d.optString("kind").equals("expense"))pay+=d.optLong("amount");if(d.optString("entry_kind","bill").equals("bill"))pay+=d.optLong("fee");}}if(row.has("history_day_income")){income=row.optLong("history_day_income");pay=row.optLong("history_day_pay");}Ui.text(v,"income",Money.text(income,currency));Ui.text(v,"pay",Money.text(pay,currency));Ui.show(v,"income_layout",income!=0);Ui.show(v,"pay_layout",pay!=0);}
        String category=row.optString("entry_kind").equals("lend")?Ui.entryTitle(row).replace("记录",""):kind.equals("transfer")?"转账":row.optString("category",kind.equals("income")?"其他收入":"其他支出");
        String remark=row.optString("remark"),title=category,description=remark;
        // Original u.u0: a nonempty remark becomes the title only when RemarkMain is enabled.
        // An empty remark retains its category; the generic category “其他” adds no description.
        if(OriginalSettings.remarkMain(ReferenceApplication.prefs())&&!remark.isEmpty()){
            title=remark;description=!kind.equals("transfer")&&category.equals("其他")?"":category;
        }
        boolean showTime=OriginalSettings.showTime(ReferenceApplication.prefs());
        Ui.text(v,"category",title);Ui.text(v,"remark",description);Ui.show(v,"remark",!description.isEmpty());Ui.show(v,"time",showTime);
        Ui.text(v,"time",Ui.date(time,"HH:mm"));String account=row.optString("account","");
        if(!row.optString("entry_kind","bill").equals("bill")){
            JSONObject from=ReferenceApplication.store().assetRepository().byRowId(row.optLong("account_id")),to=ReferenceApplication.store().assetRepository().byRowId(row.optLong("to_account_id"));
            account=(from==null?"未指定":from.optString("name"))+" → "+(to==null?"未指定":to.optString("name"));
        }if(account.isEmpty()||account.equals("null"))account=row.optString("asset_text","");Ui.text(v,"asset",account);Ui.show(v,"asset",!account.isEmpty());
        Ui.text(v,"cost",(kind.equals("income")?"+":kind.equals("expense")?"-":"")+Money.text(row.optLong("amount"),currency));
        android.widget.TextView cost=Ui.view(v,"cost");if(cost!=null)cost.setTextColor(kind.equals("income")?Ui.GREEN:kind.equals("expense")?Ui.RED:Ui.color(v.getContext(),"black",Color.BLACK));
        String financial=row.optString("financial_type");
        boolean ordinaryBill=!kind.equals("transfer")&&(financial.isEmpty()||financial.equals("reimbursement"));
        if(ordinaryBill&&row.optLong("discount")!=0&&OriginalSettings.showOldCost(ReferenceApplication.prefs())){
            // Original u.u0, classes4 method 24498 / code 0x46fa44, pc 0x07c7..0x0822:
            // a nonzero discount shows abs(current currency amount)+discount, with strike-through.
            // Refund display awaits the separate net-amount/Refund domain mapping; this schema
            // retains the parent's amount before refunds, so do not add related returns again.
            double value=(Math.abs((double)row.optLong("amount"))+row.optLong("discount"))/Math.pow(10,Money.scale(currency));
            oldCost.setText(com.wangc.bill.utils.h2.t(value));oldCost.setPaintFlags(oldCost.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);oldCost.setVisibility(View.VISIBLE);
        }
        String icon=row.optString("category_icon","ic_parent_other");if(icon.equals("null")||Ui.res(v.getContext(),"mipmap",icon)==0)icon=kind.equals("income")?"ic_income_other":"ic_parent_other";Ui.image(v,"icon",icon);
        Ui.show(v,"discount",row.optLong("discount")>0);Ui.show(v,"reimbursement",row.optString("financial_type").equals("reimbursement")&&row.optInt("settled")==0);
        if(!row.optString("origin").isEmpty()){Ui.show(v,"create_user",true);Ui.text(v,"create_user","来源·"+row.optString("origin"));}
        boolean typed=!row.optString("entry_kind","bill").equals("bill");
        List<JSONObject> tags=new java.util.ArrayList<>();
        if(typed){org.json.JSONArray ids=row.optJSONArray("tag_ids");if(ids!=null)for(int i=0;i<ids.length()&&tags.size()<4;i++){JSONObject tag=ReferenceApplication.store().one("SELECT name FROM tags WHERE id=?",""+ids.optLong(i));if(tag!=null)tags.add(tag);}}
        else tags=ReferenceApplication.store().query("SELECT g.name FROM tags g JOIN transaction_tags t ON t.tag_id=g.id WHERE t.transaction_id=? ORDER BY g.id LIMIT 4",""+row.optLong("id"));
        String[] slots={"tag_one","tag_two","tag_three","tag_four"};for(int i=0;i<tags.size();i++){Ui.show(v,slots[i],true);Ui.text(v,slots[i],tags.get(i).optString("name"));}
        Ui.show(v,"second_layout",showTime||!description.isEmpty()||!tags.isEmpty()||!row.optString("origin").isEmpty());
        v.setOnClickListener(x->{if(click!=null)click.record(row);});v.setOnLongClickListener(x->{if(!allowTrash)return false;if(!typed){JSONObject owner=com.wangc.bill.reference.AccountingLinks.ownerOfBill(ReferenceApplication.store(),row.optLong("id"));if(owner!=null){Ui.record(v.getContext(),owner);return true;}}Ui.confirm(v.getContext(),"删除账单","账单将移入回收站，可在设置中恢复。",()->{if(typed)ReferenceApplication.store().trashAccountingEntry(row.optString("entry_kind"),row.optLong("entry_id",row.optLong("id")),true);else ReferenceApplication.store().trash(row.optLong("id"),true);if(historyReload!=null){historyReload.run();return;}rows.remove(row);notifyDataSetChanged();});return true;});
    }
    static final class Holder extends RecyclerView.ViewHolder{Holder(View view){super(view);}}
}
