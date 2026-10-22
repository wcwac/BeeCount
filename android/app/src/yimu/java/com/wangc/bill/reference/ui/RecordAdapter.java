package com.wangc.bill.reference.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.wangc.bill.reference.Money;
import com.wangc.bill.reference.ReferenceApplication;
import org.json.JSONObject;
import java.util.List;

public final class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.Holder> {
    public interface Click { void record(JSONObject value); }
    private final List<JSONObject> rows;
    private final View header;
    private final Click click;
    public boolean allowTrash=true;
    public RecordAdapter(List<JSONObject> rows, View header, Click click) { this.rows=rows;this.header=header;this.click=click; }
    @Override public int getItemCount(){return rows.size()+(header==null?0:1);}
    @Override public int getItemViewType(int position){return header!=null&&position==0?0:1;}
    @Override public Holder onCreateViewHolder(ViewGroup parent,int type){return new Holder(type==0?header:Ui.layout(parent.getContext(),"item_bill_main_list"));}
    @Override public void onBindViewHolder(Holder holder,int position){
        if(getItemViewType(position)==0)return;
        int at=position-(header==null?0:1);JSONObject row=rows.get(at);View v=holder.itemView;String kind=row.optString("kind"),currency=row.optString("currency","CNY");long time=row.optLong("time");
        Ui.hide(v,"radio_button","reimbursement","refund","discount","old_cost","group_asset_list","file_click_layout","tag_one","tag_two","tag_three","tag_four","group_tag","has_file_tag","create_user");
        boolean first=at==0||!Ui.date(time,"yyyyMMdd").equals(Ui.date(rows.get(at-1).optLong("time"),"yyyyMMdd"));Ui.show(v,"time_layout",first);
        if(first){Ui.text(v,"date_info",Ui.date(time,"MM月dd日"));Ui.text(v,"week_info",Ui.date(time,"EEEE"));long income=0,pay=0;for(int i=at;i<rows.size();i++){JSONObject d=rows.get(i);if(!Ui.date(time,"yyyyMMdd").equals(Ui.date(d.optLong("time"),"yyyyMMdd")))break;if(d.optInt("not_total")==0){if(d.optString("kind").equals("income"))income+=d.optLong("amount");if(d.optString("kind").equals("expense"))pay+=d.optLong("amount");pay+=d.optLong("fee");}}Ui.text(v,"income",Money.text(income,currency));Ui.text(v,"pay",Money.text(pay,currency));Ui.show(v,"income_layout",income!=0);Ui.show(v,"pay_layout",pay!=0);}
        Ui.text(v,"category",kind.equals("transfer")?"转账":row.optString("category",kind.equals("income")?"其他收入":"其他支出"));
        Ui.text(v,"remark",row.optString("remark"));Ui.show(v,"remark",!row.optString("remark").isEmpty()&&ReferenceApplication.prefs().getBoolean("show_remark",true));Ui.show(v,"time",ReferenceApplication.prefs().getBoolean("show_time",true));
        Ui.text(v,"time",Ui.date(time,"HH:mm"));String account=row.optString("account","");if(account.isEmpty()||account.equals("null"))account=row.optString("asset_text","");Ui.text(v,"asset",account);Ui.show(v,"asset",!account.isEmpty());
        Ui.text(v,"cost",(kind.equals("income")?"+":kind.equals("expense")?"-":"")+Money.text(row.optLong("amount"),currency));
        android.widget.TextView cost=Ui.view(v,"cost");if(cost!=null)cost.setTextColor(kind.equals("income")?Ui.GREEN:kind.equals("expense")?Ui.RED:Ui.color(v.getContext(),"black",Color.BLACK));
        String icon=row.optString("category_icon","ic_parent_other");if(icon.equals("null")||Ui.res(v.getContext(),"mipmap",icon)==0)icon=kind.equals("income")?"ic_income_other":"ic_parent_other";Ui.image(v,"icon",icon);
        Ui.show(v,"discount",row.optLong("discount")>0);Ui.show(v,"reimbursement",row.optString("financial_type").equals("reimbursement")&&row.optInt("settled")==0);
        if(!row.optString("origin").isEmpty()){Ui.show(v,"create_user",true);Ui.text(v,"create_user","来源·"+row.optString("origin"));}
        List<JSONObject> tags=ReferenceApplication.store().query("SELECT g.name FROM tags g JOIN transaction_tags t ON t.tag_id=g.id WHERE t.transaction_id=? ORDER BY g.id LIMIT 4",""+row.optLong("id"));
        String[] slots={"tag_one","tag_two","tag_three","tag_four"};for(int i=0;i<tags.size();i++){Ui.show(v,slots[i],true);Ui.text(v,slots[i],tags.get(i).optString("name"));}
        v.setOnClickListener(x->{if(click!=null)click.record(row);});v.setOnLongClickListener(x->{if(!allowTrash)return false;Ui.confirm(v.getContext(),"删除账单","账单将移入回收站，可在设置中恢复。",()->{ReferenceApplication.store().trash(row.optLong("id"),true);rows.remove(row);notifyDataSetChanged();});return true;});
    }
    static final class Holder extends RecyclerView.ViewHolder{Holder(View view){super(view);}}
}
