package com.wangc.bill.reference.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import com.wangc.bill.reference.Json;
import com.wangc.bill.reference.LocalInvestments;
import com.wangc.bill.reference.Money;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;

/** Local buy/sell/dividend/price forms; investment principal never becomes daily spending. */
public final class InvestmentScreens {
    private static final String NOTICE="独立持仓日记：这里的买卖、分红不会改变现金账户，也不计入日常收支。持仓估值与手工投资账户分开显示，请勿重复计入资产。";
    private InvestmentScreens() {}
    public static void render(ManagerActivity activity) {
        if("investment_detail".equals(activity.page)) detail(activity); else list(activity);
    }
    private static LocalInvestments data(ManagerActivity a){return new LocalInvestments(a.store());}
    private static String money(long amount){return "¥"+Money.text(amount,"CNY");}
    private static String type(String value){return switch(value){case "stock"->"股票";case "fund"->"基金";default->"自定义标的";};}
    private static String action(String value){return switch(value){case "buy"->"买入";case "sell"->"卖出";case "dividend"->"分红";default->"手工价格";};}

    private static void list(ManagerActivity a){
        LocalInvestments investments=data(a);
        a.body("本地投资","新增",()->instrumentForm(a,0));
        a.rows.addView(Ui.row(a,"本地持仓 · CNY",NOTICE,null));
        JSONObject summary=investments.portfolio(a.bookId());
        a.rows.addView(Ui.row(a,"持仓估值 "+money(summary.optLong("market_value_minor")),
                "持仓成本 "+money(summary.optLong("cost_minor"))+"\n已实现 "+money(summary.optLong("realized_minor"))
                        +" · 浮动盈亏 "+money(summary.optLong("unrealized_minor")),null));
        if(summary.optInt("unpriced_count")>0)a.rows.addView(Ui.row(a,"部分标的未设置价格","未设置价格的持仓暂按成本估值，不能代表当前行情。进入标的可记录手工价格。",null));
        List<JSONObject> positions=investments.positions(a.bookId(),true);
        if(positions.isEmpty())a.rows.addView(Ui.row(a,"尚无持仓标的","新增股票、基金或自定义标的，再记录买入。已有持仓可按原始买入日期、份额和成本补录。",()->instrumentForm(a,0)));
        boolean archiveSection=false;
        for(JSONObject position:positions){
            boolean archived=position.optInt("archived")!=0;
            if(archived&&!archiveSection){a.rows.addView(Ui.section(a,"已归档 · 点击可恢复"));archiveSection=true;}
            String title=position.optString("name")+(position.optString("code").isEmpty()?"":" · "+position.optString("code"));
            String text=type(position.optString("instrument_type"))+" · "+position.optString("quantity_text")+" 份/股"
                    +"\n估值 "+money(position.optLong("market_value_minor"))+" · 累计盈亏 "+money(position.optLong("total_profit_minor"));
            a.rows.addView(Ui.row(a,title,text,()->a.open("investment_detail",position.optLong("id"))));
        }
        a.rows.addView(Ui.section(a,"已有手工投资账户"));
        for(JSONObject account:a.store().query("SELECT * FROM accounts WHERE archived=0 AND type='investment' ORDER BY id"))
            a.rows.addView(Ui.row(a,account.optString("name"),"手工余额 "+Money.label(a.store().accountBalance(account.optLong("id")),account.optString("currency"))
                    +" · 与上方持仓日记独立",()->a.open("account_detail",account.optLong("id"))));
    }

    private static void detail(ManagerActivity a){
        LocalInvestments investments=data(a);JSONObject position=investments.position(a.entity);
        boolean archived=position.optInt("archived")!=0;
        a.body(position.optString("name"),archived?"恢复":"记交易",()->{
            if(archived)mutate(a,()->investments.setArchived(a.entity,false,UUID.randomUUID().toString()));
            else new AlertDialog.Builder(a).setTitle("记录投资").setItems(new String[]{"买入","卖出","分红","手工价格"},
                    (dialog,index)->eventForm(a,a.entity,null,new String[]{"buy","sell","dividend","price"}[index])).show();
        });
        a.rows.addView(Ui.row(a,type(position.optString("instrument_type"))+" · "+position.optString("code"),NOTICE,null));
        a.rows.addView(Ui.row(a,"持有 "+position.optString("quantity_text")+" 份/股", "估值 "+money(position.optLong("market_value_minor"))
                +"\n持仓成本 "+money(position.optLong("cost_minor"))+" · 平均成本 ¥"+position.optString("average_cost_text"),null));
        a.rows.addView(Ui.row(a,"累计盈亏 "+money(position.optLong("total_profit_minor")),"已实现 "+money(position.optLong("realized_minor"))
                +" · 浮动盈亏 "+money(position.optLong("unrealized_minor"))+"\n净分红 "+money(position.optLong("dividends_minor"))
                +" · 累计费用 "+money(position.optLong("fees_minor")),null));
        a.rows.addView(Ui.row(a,position.optBoolean("has_price")?"手工价格 ¥"+position.optString("price_text"):"尚未设置价格",
                position.optBoolean("has_price")?"记录时间 "+Ui.date(position.optLong("price_time"),"yyyy-MM-dd HH:mm")+" · 价格仅来自你的手工记录"
                        :"暂按成本估值；点击记录当前单价。",archived?null:()->eventForm(a,a.entity,null,"price")));
        a.rows.addView(Ui.row(a,"计算方式","成交金额由你填写，买入费用计入成本；卖出按移动加权平均成本计算已实现盈亏。份额、单价最多6位小数；成本分摊和估值四舍五入至分，全部卖出时清除剩余成本。",null));
        if(!position.optString("remark").isEmpty())a.rows.addView(Ui.row(a,"备注",position.optString("remark"),null));
        if(!archived){
            a.rows.addView(Ui.row(a,"编辑标的","名称、代码、类型、账本与备注",()->instrumentForm(a,a.entity)));
            a.rows.addView(Ui.row(a,"归档标的","持仓为零时可归档，历史收益和交易会保留。",()->Ui.confirm(a,"归档标的","交易历史会保留，可以重新恢复。",()->mutate(a,()->investments.setArchived(a.entity,true,UUID.randomUUID().toString())))));
        }
        a.rows.addView(Ui.section(a,"交易历史 · 点击编辑或删除，已删除记录可恢复"));
        List<JSONObject> events=investments.events(a.entity,true);
        if(events.isEmpty())a.rows.addView(Ui.row(a,"尚无交易","点击右上角记交易，补录已有持仓或记录新买入。",null));
        for(int i=events.size()-1;i>=0;i--){
            JSONObject event=events.get(i);boolean deleted=event.optInt("deleted")!=0;
            View row=Ui.layout(a,"item_stock_info");
            Ui.hide(row,"bill_layout","status_wait","name");
            Ui.text(row,"type",(deleted?"[已删除] ":"")+action(event.optString("action")));
            Ui.text(row,"date",Ui.date(event.optLong("happened_at"),"yyyy-MM-dd HH:mm"));
            Ui.text(row,"remark",event.optString("remark"));
            Ui.text(row,"total_cost",event.optString("action").equals("price")?"¥"+LocalInvestments.priceText(event.optLong("price_micros")):money(event.optLong("amount_minor")));
            Ui.text(row,"asset_info",event.optString("action").equals("price")?"手工单价":
                    (event.optString("action").equals("dividend")?"分红":"份额 "+LocalInvestments.unitsText(event.optLong("quantity_micros")))+" · 费用 "+money(event.optLong("fee_minor")));
            row.setAlpha(deleted?.55f:1f);row.setOnClickListener(v->{
                if(archived){Ui.toast(a,"请先恢复已归档标的");return;}
                if(deleted)Ui.confirm(a,"恢复交易","恢复后将重新计算完整持仓；如果造成超卖，本次操作不会保存。",()->mutate(a,()->investments.trashEvent(event.optLong("id"),false,UUID.randomUUID().toString())));
                else new AlertDialog.Builder(a).setTitle(action(event.optString("action"))).setItems(new String[]{"编辑","删除"},(dialog,index)->{
                    if(index==0)eventForm(a,a.entity,event,event.optString("action"));
                    else Ui.confirm(a,"删除交易","将重新计算持仓和盈亏；如影响后续卖出造成超卖，本次操作不会保存。",()->mutate(a,()->investments.trashEvent(event.optLong("id"),true,UUID.randomUUID().toString())));
                }).show();
            });a.rows.addView(row);
        }
    }

    private static void instrumentForm(ManagerActivity a,long id){
        try{
            JSONObject old=id==0?null:data(a).position(id);
            LinearLayout form=form(a);EditText name=field(a,form,"标的名称",old==null?"":old.optString("name"),false);
            EditText code=field(a,form,"代码（可空）",old==null?"":old.optString("code"),false);
            form.addView(Ui.label(a,"类型",14));Spinner type=new Spinner(a);type.setAdapter(new ArrayAdapter<>(a,android.R.layout.simple_spinner_dropdown_item,new String[]{"股票","基金","自定义"}));form.addView(type);
            String oldType=old==null?"stock":old.optString("instrument_type");type.setSelection(oldType.equals("fund")?1:oldType.equals("custom")?2:0);
            long[] book={old==null?a.bookId():old.optLong("book_id")};TextView selected=Ui.label(a,"",15);form.addView(selected);
            Runnable refresh=()->{JSONObject b=a.store().book(book[0]);selected.setText("账本："+(b==null?"请选择人民币账本":b.optString("name"))+" ›");};refresh.run();
            selected.setPadding(0,Ui.dp(a,16),0,Ui.dp(a,16));selected.setOnClickListener(v->{
                List<JSONObject> books=new ArrayList<>();for(JSONObject b:a.store().books())if("CNY".equals(b.optString("currency"))&&b.optInt("archived")==0)books.add(b);
                if(books.isEmpty()){Ui.toast(a,"请先创建人民币账本");return;}
                String[] names=new String[books.size()];for(int i=0;i<names.length;i++)names[i]=books.get(i).optString("name");
                new AlertDialog.Builder(a).setTitle("所属账本").setItems(names,(dialog,index)->{book[0]=books.get(index).optLong("id");refresh.run();}).show();
            });
            EditText remark=field(a,form,"备注（可空）",old==null?"":old.optString("remark"),false);
            form.addView(Ui.label(a,NOTICE,12));
            String operation=UUID.randomUUID().toString();
            saveDialog(a,id==0?"新增投资标的":"编辑投资标的",form,()->data(a).saveInstrument(id,Json.object("book_id",book[0],"name",name.getText().toString(),"code",code.getText().toString(),"instrument_type",new String[]{"stock","fund","custom"}[type.getSelectedItemPosition()],"remark",remark.getText().toString()),operation));
        }catch(Exception error){Ui.error(a,error);}
    }

    private static void eventForm(ManagerActivity a,long investmentId,JSONObject old,String eventAction){
        try{
            LinearLayout form=form(a);boolean price=eventAction.equals("price"),dividend=eventAction.equals("dividend");
            form.addView(Ui.label(a,NOTICE,12));
            EditText quantity=field(a,form,"成交份额（最多6位小数）",old==null?"":LocalInvestments.unitsText(old.optLong("quantity_micros")),true);
            EditText amount=field(a,form,(dividend?"分红总额":"成交总金额")+"（费用前，人民币元）",old==null?"":Money.text(old.optLong("amount_minor"),"CNY"),true);
            EditText fee=field(a,form,"费用（人民币元，未填写按0）",old==null?"0.00":Money.text(old.optLong("fee_minor"),"CNY"),true);
            JSONObject position=data(a).position(investmentId);
            EditText quote=field(a,form,"手工单价（元/份或股，最多6位小数）",old==null?(position.optBoolean("has_price")?position.optString("price_text"):""):LocalInvestments.priceText(old.optLong("price_micros")),true);
            // Hide the labels together with their inputs; no invisible amount is silently reused.
            fieldVisible(form,quantity,!price&&!dividend);fieldVisible(form,amount,!price);fieldVisible(form,fee,!price);fieldVisible(form,quote,price);
            Calendar time=Calendar.getInstance();time.setTimeInMillis(old==null?System.currentTimeMillis():old.optLong("happened_at"));
            TextView date=Ui.label(a,"",15),clock=Ui.label(a,"",15);form.addView(date);form.addView(clock);
            Runnable refreshTime=()->{date.setText("日期："+Ui.date(time.getTimeInMillis(),"yyyy-MM-dd")+" ›");clock.setText("时间："+Ui.date(time.getTimeInMillis(),"HH:mm")+" ›");};refreshTime.run();
            for(TextView t:new TextView[]{date,clock})t.setPadding(0,Ui.dp(a,12),0,Ui.dp(a,12));
            date.setOnClickListener(v->new DatePickerDialog(a,(picker,y,m,d)->{time.set(y,m,d);refreshTime.run();},time.get(Calendar.YEAR),time.get(Calendar.MONTH),time.get(Calendar.DAY_OF_MONTH)).show());
            clock.setOnClickListener(v->new TimePickerDialog(a,(picker,h,m)->{time.set(Calendar.HOUR_OF_DAY,h);time.set(Calendar.MINUTE,m);time.set(Calendar.SECOND,0);time.set(Calendar.MILLISECOND,0);refreshTime.run();},time.get(Calendar.HOUR_OF_DAY),time.get(Calendar.MINUTE),true).show());
            EditText remark=field(a,form,"备注（可空）",old==null?"":old.optString("remark"),false);
            form.addView(Ui.label(a,price?"单价可以是0；估值按最新时间的有效手工价格计算，无在线行情。":"请填写实际成交总金额，不会根据单价猜测或自动扣款。编辑后按交易时间重新计算；同一时间按创建顺序计算。",12));
            String operation=UUID.randomUUID().toString();
            saveDialog(a,(old==null?"记录":"编辑")+action(eventAction),form,()->{
                long q=price||dividend?0:LocalInvestments.units(quantity.getText().toString());
                long total=price?0:Money.signed(amount.getText().toString(),"CNY");
                long charge=price?0:Money.signed(fee.getText().toString().trim().isEmpty()?"0":fee.getText().toString(),"CNY");
                return data(a).saveEvent(old==null?0:old.optLong("id"),Json.object("investment_id",investmentId,"action",eventAction,"quantity_micros",q,"amount_minor",total,"fee_minor",charge,
                        "price_micros",price?LocalInvestments.price(quote.getText().toString()):0L,"happened_at",time.getTimeInMillis(),"remark",remark.getText().toString()),operation);
            });
        }catch(Exception error){Ui.error(a,error);}
    }
    private static LinearLayout form(ManagerActivity a){LinearLayout body=Ui.column(a);body.setPadding(Ui.dp(a,18),Ui.dp(a,10),Ui.dp(a,18),Ui.dp(a,14));return body;}
    private static EditText field(ManagerActivity a,LinearLayout parent,String title,String value,boolean decimal){
        TextView label=Ui.label(a,title,13);label.setPadding(0,Ui.dp(a,10),0,0);parent.addView(label);
        EditText input=new EditText(a);input.setSingleLine(true);input.setInputType(decimal?InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL:InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(value);parent.addView(input,new LinearLayout.LayoutParams(-1,-2));return input;
    }
    private static void fieldVisible(LinearLayout parent,View field,boolean visible){int index=parent.indexOfChild(field);field.setVisibility(visible?View.VISIBLE:View.GONE);parent.getChildAt(index-1).setVisibility(visible?View.VISIBLE:View.GONE);}
    private interface Save{JSONObject run();}
    private static void saveDialog(ManagerActivity a,String title,LinearLayout form,Save save){
        ScrollView scroll=new ScrollView(a);scroll.addView(form);
        AlertDialog dialog=new AlertDialog.Builder(a).setTitle(title).setView(scroll).setNegativeButton("取消",null).setPositiveButton("保存",null).create();
        boolean[] committed={false};dialog.setOnShowListener(ignored->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button->{
            if(committed[0])return;
            try{save.run();}catch(Exception error){Ui.error(a,error);return;}
            committed[0]=true;dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);dialog.dismiss();a.reload();
        }));dialog.show();
    }
    private static void mutate(ManagerActivity a,Save operation){try{operation.run();a.reload();}catch(Exception error){Ui.error(a,error);}}
}
