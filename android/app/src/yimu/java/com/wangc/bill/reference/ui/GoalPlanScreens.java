package com.wangc.bill.reference.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wangc.bill.reference.AssetRepository;
import com.wangc.bill.reference.CategoryRepository;
import com.wangc.bill.reference.Json;
import com.wangc.bill.reference.LocalGoalPlans;
import com.wangc.bill.reference.Money;
import com.wangc.bill.reference.capture.CaptureNotifications;
import com.wangc.bill.reference.capture.PlanScheduler;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/** Forms for real local progress and recurring drafts, independent from financial transfers. */
public final class GoalPlanScreens {
    private static final int ATTACHMENT_REQUEST=461;
    private static final String DRAFT="local_goal_plan_form";
    private static final String[] MODES={"manual","assets","income","expense","balance"};
    private static final String[] MODE_NAMES={"手动累计","资产余额自动跟踪","收入累计","支出累计","收支结余累计"};
    private static final String[] FREQUENCIES={"daily","weekly","monthly","yearly"};
    private static final String[] FREQUENCY_NAMES={"天","周","月","年"};
    private GoalPlanScreens(){}
    public static void render(ManagerActivity a){switch(a.page){case "goals"->goals(a);case "edit_goal"->goalForm(a);case "plans"->plans(a);case "edit_plan"->planForm(a);default->throw new IllegalArgumentException("目标或周期页面不存在");}}
    private static LocalGoalPlans service(ManagerActivity a){return new LocalGoalPlans(a.store());}

    private static void goals(ManagerActivity a){
        a.body("存钱目标","新增",()->a.open("edit_goal",0));List<JSONObject> values=service(a).goals();
        a.rows.addView(Ui.label(a,"自动进度读取所选账户或账单；手动调整只更新目标记录，不会扣款或转账。",13));
        if(values.isEmpty())a.rows.addView(Ui.row(a,"暂无目标","点击新增设置目标与进度来源。",null));
        for(JSONObject value:values){String currency=value.optString("currency");
            String detail=modeName(value.optString("mode"))+" · "+Money.text(value.optLong("progress"),currency)+" / "+Money.text(value.optLong("target"),currency)
                    +" · "+Math.min(100,Math.round(value.optLong("progress")*100.0/value.optLong("target")))+"%";
            a.rows.addView(Ui.row(a,value.optString("name")+(value.optBoolean("completed")?" · 已达成":""),detail,()->goalActions(a,value.optLong("id"))));
        }
    }
    private static void goalActions(ManagerActivity a,long id){
        JSONObject value=service(a).goal(id);String currency=value.optString("currency");
        new AlertDialog.Builder(a).setTitle(value.optString("name")).setMessage("当前进度："+Money.text(value.optLong("progress"),currency)+"\n自动部分："+Money.text(value.optLong("automatic"),currency)+"\n手动基数："+Money.text(value.optLong("saved"),currency))
                .setPositiveButton("手动加减",(dialog,which)->{String operation=UUID.randomUUID().toString();Dialogs.input(a,"调整进度（正数增加，负数减少；不更改账户余额）","0",true,raw->{service(a).adjustGoal(id,Money.signed(raw,currency),"手动调整",System.currentTimeMillis(),operation);a.reload();});})
                .setNeutralButton("历史",(dialog,which)->goalHistory(a,id,currency)).setNegativeButton("编辑",(dialog,which)->a.open("edit_goal",id)).show();
    }
    private static void goalHistory(ManagerActivity a,long id,String currency){
        List<JSONObject> entries=service(a).goalHistory(id);RecyclerView list=new RecyclerView(a);list.setLayoutManager(new LinearLayoutManager(a));
        list.setAdapter(new RecyclerView.Adapter<Holder>(){
            public int getItemCount(){return entries.size();}
            public Holder onCreateViewHolder(ViewGroup parent,int type){return new Holder(Ui.row(a,"","",null));}
            public void onBindViewHolder(Holder holder,int index){JSONObject row=entries.get(index);LinearLayout view=(LinearLayout)holder.itemView;
                ((TextView)view.getChildAt(0)).setText((row.optLong("delta")>0?"+":"")+Money.text(row.optLong("delta"),currency)+" · 进度 "+Money.text(row.optLong("balance"),currency));
                ((TextView)view.getChildAt(1)).setText(Ui.date(row.optLong("time"),"yyyy-MM-dd HH:mm")+" · "+row.optString("remark"));}
        });
        LinearLayout content=Ui.column(a);content.addView(Ui.label(a,entries.isEmpty()?"暂无手动调整历史。":"完整手动调整历史；自动进度随账户和账单实时计算。",13));
        content.addView(list,new LinearLayout.LayoutParams(-1,Ui.dp(a,380)));
        new AlertDialog.Builder(a).setTitle("目标进度历史").setView(content).setPositiveButton("关闭",null).show();
    }

    private static void goalForm(ManagerActivity a){
        JSONObject initial=a.entity>0?service(a).goal(a.entity):Json.object("name","","target",0,"saved",0,"currency",a.currency(),"mode","manual","asset_mode","net","ratio_bps",10000,"start_time",0,"end_time",0,"book_ids",new JSONArray().put(a.bookId()),"account_ids",new JSONArray());
        JSONObject data=draft(a,initial);a.body(a.entity==0?"新增目标":"编辑目标",null,null);
        field(a,data,"name_text","目标名称",data.optString("name"),false);
        String currency=data.optString("currency","CNY");
        a.rows.addView(Ui.row(a,"币种",currency+(a.entity>0?" · 历史币种固定":""),a.entity>0?null:()->Dialogs.currencies(a,currency,chosen->{if(!chosen.equals(currency)){Json.put(data,"currency",chosen);Json.put(data,"target_text","0");Json.put(data,"saved_text","0");Json.put(data,"book_ids",new JSONArray());Json.put(data,"account_ids",new JSONArray());remember(a,data);a.reload();}})));
        field(a,data,"target_text","目标金额",Money.text(data.optLong("target"),currency),true);
        field(a,data,"saved_text","手动起始基数（自动模式可用于补记）",Money.text(data.optLong("saved"),currency),true);
        a.rows.addView(Ui.row(a,"进度方式",modeName(data.optString("mode")),()->new AlertDialog.Builder(a).setTitle("进度来源").setItems(MODE_NAMES,(dialog,index)->{Json.put(data,"mode",MODES[index]);remember(a,data);a.reload();}).show()));
        if(!data.optString("mode").equals("manual")){
            field(a,data,"ratio_text","自动金额计入比例（0—100%）",Money.text(data.optInt("ratio_bps",10000),"CNY"),true);
            a.rows.addView(Ui.row(a,"账户范围",selectionLabel(data.optJSONArray("account_ids"),"同币种全部可见账户"),()->selectAccounts(a,currency,data.optJSONArray("account_ids"),selected->{Json.put(data,"account_ids",selected);remember(a,data);a.reload();})));
            if(data.optString("mode").equals("assets"))a.rows.addView(Ui.row(a,"资产口径",data.optString("asset_mode").equals("deposit")?"仅正余额":"净余额（包含负债）",()->new AlertDialog.Builder(a).setItems(new String[]{"净余额（包含负债）","仅正余额"},(dialog,index)->{Json.put(data,"asset_mode",index==0?"net":"deposit");remember(a,data);a.reload();}).show()));
            else{
                a.rows.addView(Ui.row(a,"账本范围",selectionLabel(data.optJSONArray("book_ids"),"同币种全部账本"),()->selectBooks(a,currency,data.optJSONArray("book_ids"),selected->{Json.put(data,"book_ids",selected);remember(a,data);a.reload();})));
                date(a,data,"start_time","累计开始日期",false,true);date(a,data,"end_time","累计截至日期",true,true);
            }
            a.rows.addView(Ui.label(a,"收支按主页相同的统计口径累计，排除回收站和不计入收支的账单；资产模式读取当前余额。空范围表示全部同币种数据。",13));
        }
        action(a,"保存目标",()->{JSONObject value=Json.copy(data);Json.put(value,"name",data.optString("name_text"));Json.put(value,"target",Money.positive(data.optString("target_text"),currency));Json.put(value,"saved",Money.signed(data.optString("saved_text"),currency));Json.put(value,"ratio_bps",Money.signed(data.optString("ratio_text","100.00"),"CNY"));service(a).saveGoal(a.entity,value);a.finish();});
        if(a.entity>0)action(a,"删除目标",()->Ui.confirm(a,"删除目标","此目标和手动调整历史会删除；账户余额和真实账单不受影响。",()->{service(a).deleteGoal(a.entity);a.finish();}));
    }

    private static void plans(ManagerActivity a){
        a.body("周期记账","新增",()->a.open("edit_plan",0));
        a.rows.addView(Ui.row(a,"检查到期账单","规则生成待确认记录，核对后才记入账本与账户。",()->FileTasks.run(a,"正在检查到期账单",()->service(a).processDue(System.currentTimeMillis()),count->{PlanScheduler.sync();CaptureNotifications.update();Ui.page(a,"inbox");})));
        List<JSONObject> plans=service(a).plans();if(plans.isEmpty())a.rows.addView(Ui.row(a,"暂无周期规则","支持间隔、次数和截至日期。",null));
        for(JSONObject value:plans){JSONObject payload=value.optJSONObject("data");String currency=payload.optString("currency","CNY");
            String detail=Money.text(payload.optLong("amount"),currency)+" · 每 "+value.optInt("every_n")+frequencyName(value.optString("interval"))+" · 下次 "+Ui.date(value.optLong("next_due"),"yyyy-MM-dd HH:mm")
                    +"\n已生成 "+value.optLong("emitted_count")+(value.optInt("max_occurrences")==0?" 次":" / "+value.optInt("max_occurrences")+" 次")+(value.optInt("enabled")==1?" · 已启用":" · 已暂停")
                    +(value.optLong("until_time")>0?" · 截至 "+Ui.date(value.optLong("until_time"),"yyyy-MM-dd"):"")+(value.optString("last_error").isEmpty()?"":"\n"+value.optString("last_error"));
            LinearLayout row=Ui.row(a,value.optString("name"),detail,()->a.open("edit_plan",value.optLong("id")));
            row.setOnLongClickListener(view->{try{service(a).enablePlan(value.optLong("id"),value.optInt("enabled")!=1);PlanScheduler.sync();a.reload();}catch(Exception error){Ui.error(a,error);}return true;});a.rows.addView(row);
        }
    }
    private static void planForm(ManagerActivity a){
        JSONObject initial=a.entity>0?service(a).plan(a.entity):Json.object("name","","interval","monthly","next_due",System.currentTimeMillis(),"enabled",true,"every_n",1,"max_occurrences",0,"until_time",0,"data",Json.object("book_id",a.bookId(),"kind","expense","category_id",a.store().defaultCategory("expense",a.bookId()),"account_id",0,"to_account_id",0,"tag_ids",new JSONArray(),"attachments","[]","not_total",false,"not_budget",false,"fee",0,"discount",0));
        JSONObject data=draft(a,initial),payload=data.optJSONObject("data");if(payload==null)throw new IllegalArgumentException("周期内容不存在");
        JSONObject book=a.store().book(payload.optLong("book_id"));String currency=book==null?a.currency():book.optString("currency");
        a.body(a.entity==0?"新增周期规则":"编辑周期规则",null,null);field(a,data,"name_text","规则名称",data.optString("name"),false);
        a.rows.addView(Ui.row(a,"账本",book==null?"请选择":book.optString("name")+" · "+currency,()->{
            List<JSONObject> books=a.store().books();String[] names=new String[books.size()];for(int i=0;i<names.length;i++)names[i]=books.get(i).optString("name")+" · "+books.get(i).optString("currency");
            new AlertDialog.Builder(a).setTitle("周期目标账本").setItems(names,(dialog,index)->{JSONObject selected=books.get(index);Json.put(payload,"book_id",selected.optLong("id"));
                if(!currency.equals(selected.optString("currency"))){Json.put(data,"amount_text","0");Json.put(data,"fee_text","0");Json.put(payload,"account_id",0);Json.put(payload,"to_account_id",0);payload.remove("owner_semantic_id");payload.remove("raw_asset_semantic_id");payload.remove("raw_to_asset_semantic_id");}
                remember(a,data);a.reload();}).show();
        }));
        field(a,data,"amount_text","每次金额",Money.text(payload.optLong("amount"),currency),true);
        a.rows.addView(Ui.row(a,"记账类型",kindName(payload.optString("kind")),()->new AlertDialog.Builder(a).setItems(new String[]{"支出","收入","转账"},(dialog,index)->{
            String kind=new String[]{"expense","income","transfer"}[index];Json.put(payload,"kind",kind);Json.put(payload,"financial_type","");Json.put(payload,"transfer_repayment_route",false);
            if(!kind.equals("transfer")){Json.put(payload,"category_id",a.store().defaultCategory(kind,payload.optLong("book_id")));Json.put(data,"fee_text","0");}
            payload.remove("parent_semantic_id");payload.remove("child_semantic_id");remember(a,data);a.reload();}).show()));
        if(!payload.optString("kind").equals("transfer")){
            JSONObject category=a.store().categoryRepository().byRowId(payload.optLong("category_id"));
            a.rows.addView(Ui.row(a,"分类",category==null?"请选择":category.optString("name"),()->chooseCategory(a,payload,()->{remember(a,data);a.reload();})));
        }
        a.rows.addView(Ui.row(a,payload.optString("kind").equals("transfer")?"转出账户":"记账账户",accountName(a,payload.optLong("account_id")),()->chooseAccount(a,payload,false,()->{remember(a,data);a.reload();})));
        if(payload.optString("kind").equals("transfer")){
            a.rows.addView(Ui.row(a,"转入账户",accountName(a,payload.optLong("to_account_id")),()->chooseAccount(a,payload,true,()->{remember(a,data);a.reload();})));
            field(a,data,"fee_text","每次手续费（负数表示返还）",Money.text(payload.optLong("fee"),currency),true);
        }
        field(a,data,"remark_text","每次账单备注",payload.optString("remark"),false);
        a.rows.addView(Ui.row(a,"周期单位",frequencyName(data.optString("interval")),()->new AlertDialog.Builder(a).setItems(new String[]{"每天","每周","每月","每年"},(dialog,index)->{Json.put(data,"interval",FREQUENCIES[index]);remember(a,data);a.reload();}).show()));
        field(a,data,"every_text","每隔多少个周期（1—1000）",""+data.optInt("every_n",1),false,true);
        date(a,data,"next_due","下一次生成日期",false,false);date(a,data,"until_time","截至日期",true,true);
        field(a,data,"count_text","最多生成次数（0为不限，包含已生成次数）",""+data.optInt("max_occurrences"),false,true);
        toggle(a,data,"enabled","启用周期规则",flag(data,"enabled",true));
        toggle(a,payload,"not_total","不计入收支合计",flag(payload,"not_total",false),()->remember(a,data));
        toggle(a,payload,"not_budget","不计入预算",flag(payload,"not_budget",false),()->remember(a,data));
        a.rows.addView(Ui.row(a,"标签",selectionLabel(payload.optJSONArray("tag_ids"),"无标签"),()->selectRows(a,a.store().query("SELECT id,name FROM tags ORDER BY id"),payload.optJSONArray("tag_ids"),selected->{Json.put(payload,"tag_ids",selected);remember(a,data);a.reload();})));
        JSONArray files=Json.array(payload.optString("attachments","[]"));a.rows.addView(Ui.row(a,"附件",files.length()+" 个 · 每次生成时保留引用",()->{
            remember(a,data);a.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE),ATTACHMENT_REQUEST);
        }));
        for(int i=0;i<files.length();i++){JSONObject file=files.optJSONObject(i);final int index=i;
            a.rows.addView(Ui.row(a,file.optString("name","附件"),"点击打开，长按移除",()->LocalFiles.openAttachment(a,file)));
            View row=a.rows.getChildAt(a.rows.getChildCount()-1);row.setOnLongClickListener(view->{JSONArray kept=new JSONArray();for(int j=0;j<files.length();j++)if(j!=index)kept.put(files.opt(j));Json.put(payload,"attachments",kept.toString());remember(a,data);a.reload();return true;});
        }
        a.rows.addView(Ui.label(a,"周期记录先进入待确认，不会直接扣款。结束次数/日期达到后自动暂停；账户失效时保留规则并显示修正原因。",13));
        action(a,"保存周期规则",()->{JSONObject value=Json.copy(data),content=value.optJSONObject("data");Json.put(value,"name",data.optString("name_text"));
            Json.put(content,"amount",Money.positive(data.optString("amount_text"),currency));Json.put(content,"fee",Money.signed(data.optString("fee_text","0"),currency));Json.put(content,"remark",data.optString("remark_text"));
            Json.put(value,"every_n",parseInteger(data.optString("every_text")));Json.put(value,"max_occurrences",parseInteger(data.optString("count_text")));
            service(a).savePlan(a.entity,value);PlanScheduler.sync();a.finish();});
        if(a.entity>0)action(a,"删除周期规则",()->Ui.confirm(a,"删除周期规则","已生成的待确认记录与账单保留。",()->{service(a).deletePlan(a.entity);PlanScheduler.sync();a.finish();}));
    }

    public static boolean handleActivityResult(ManagerActivity a,int request,int result,Intent intent){
        if(request!=ATTACHMENT_REQUEST||!"edit_plan".equals(a.page))return false;
        if(result!=Activity.RESULT_OK||intent==null||intent.getData()==null)return true;
        FileTasks.run(a,"正在保存周期附件",()->LocalFiles.importAttachment(a,intent.getData()),file->{
            JSONObject form=readDraft(a);if(form==null||form.optJSONObject("data")==null){Ui.toast(a,"规则页面已关闭，请重新添加附件");return;}
            JSONObject payload=form.optJSONObject("data");JSONArray files=Json.array(payload.optString("attachments","[]"));if(files.length()>=100){Ui.toast(a,"单条周期规则最多100个附件");return;}
            files.put(file);Json.put(payload,"attachments",files.toString());remember(a,form);a.reload();
        });return true;
    }

    private static JSONObject draft(ManagerActivity a,JSONObject fallback){JSONObject value=readDraft(a);if(value==null)value=Json.copy(fallback);Json.put(value,"draft_page",a.page);Json.put(value,"draft_entity",a.entity);remember(a,value);return value;}
    private static JSONObject readDraft(ManagerActivity a){String raw=a.getIntent().getStringExtra(DRAFT);if(raw==null)return null;JSONObject value=Json.parse(raw);return a.page.equals(value.optString("draft_page"))&&a.entity==value.optLong("draft_entity")?value:null;}
    private static void remember(ManagerActivity a,JSONObject value){a.getIntent().putExtra(DRAFT,value.toString());}
    private static EditText field(ManagerActivity a,JSONObject data,String key,String hint,String initial,boolean decimal){return field(a,data,key,hint,initial,decimal,false);}
    private static EditText field(ManagerActivity a,JSONObject data,String key,String hint,String initial,boolean decimal,boolean integer){
        a.rows.addView(Ui.label(a,hint,13));EditText view=new EditText(a);view.setSingleLine(true);view.setHint(hint);view.setContentDescription(hint);
        if(decimal||integer)view.setInputType(InputType.TYPE_CLASS_NUMBER|(decimal?InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED:0));
        String text=data.has(key)?data.optString(key):initial;view.setText(text);Json.put(data,key,text);remember(a,data);
        view.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence value,int start,int count,int after){}public void onTextChanged(CharSequence value,int start,int before,int count){Json.put(data,key,value.toString());remember(a,data);}public void afterTextChanged(Editable value){}});a.rows.addView(view);return view;
    }
    private static void action(ManagerActivity a,String title,Runnable save){TextView button=Ui.label(a,title,17);button.setPadding(Ui.dp(a,16),Ui.dp(a,18),Ui.dp(a,16),Ui.dp(a,18));button.setTextColor(Ui.GREEN);button.setContentDescription(title);a.rows.addView(button);boolean[] busy={false};button.setOnClickListener(view->{if(busy[0])return;busy[0]=true;try{save.run();if(a.isFinishing()||a.isDestroyed())button.setEnabled(false);else busy[0]=false;}catch(Exception error){busy[0]=false;Ui.error(a,error);}});}
    private static void date(ManagerActivity a,JSONObject data,String key,String title,boolean end,boolean optional){
        long selected=data.optLong(key);a.rows.addView(Ui.row(a,title,selected==0?"不限":Ui.date(selected,"yyyy-MM-dd HH:mm"),()->{
            Runnable choose=()->{Calendar calendar=Calendar.getInstance();if(selected>0)calendar.setTimeInMillis(selected);new DatePickerDialog(a,(picker,year,month,day)->{Calendar value=Calendar.getInstance();value.clear();value.set(year,month,day,end?23:0,end?59:0,end?59:0);value.set(Calendar.MILLISECOND,end?999:0);Json.put(data,key,value.getTimeInMillis());remember(a,data);a.reload();},calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();};
            if(optional)new AlertDialog.Builder(a).setTitle(title).setItems(new String[]{"选择日期","不限"},(dialog,index)->{if(index==0)choose.run();else{Json.put(data,key,0);remember(a,data);a.reload();}}).show();else choose.run();
        }));
    }
    private static void toggle(ManagerActivity a,JSONObject data,String key,String title,boolean initial){toggle(a,data,key,title,initial,()->remember(a,data));}
    private static void toggle(ManagerActivity a,JSONObject data,String key,String title,boolean initial,Runnable changed){Switch view=new Switch(a);view.setText(title);view.setContentDescription(title);view.setChecked(initial);Json.put(data,key,initial);view.setOnCheckedChangeListener((button,checked)->{Json.put(data,key,checked);changed.run();});a.rows.addView(view);}
    private interface Selected{void accept(JSONArray selected);}
    private static void selectBooks(ManagerActivity a,String currency,JSONArray selected,Selected callback){List<JSONObject> rows=a.store().query("SELECT id,name FROM books WHERE currency=? ORDER BY id",currency);selectRows(a,rows,selected,callback);}
    private static void selectAccounts(ManagerActivity a,String currency,JSONArray selected,Selected callback){List<JSONObject> rows=new ArrayList<>();for(JSONObject account:a.store().accounts())if(currency.equals(account.optString("currency")))rows.add(account);selectRows(a,rows,selected,callback);}
    private static void selectRows(ManagerActivity a,List<JSONObject> rows,JSONArray selected,Selected callback){
        String[] names=new String[rows.size()];boolean[] checked=new boolean[rows.size()];for(int i=0;i<rows.size();i++){names[i]=rows.get(i).optString("name");for(int j=0;selected!=null&&j<selected.length();j++)if(selected.optLong(j)==rows.get(i).optLong("id"))checked[i]=true;}
        new AlertDialog.Builder(a).setTitle("选择范围").setMultiChoiceItems(names,checked,(dialog,index,on)->checked[index]=on).setPositiveButton("确定",(dialog,which)->{JSONArray values=new JSONArray();for(int i=0;i<rows.size();i++)if(checked[i])values.put(rows.get(i).optLong("id"));callback.accept(values);}).setNeutralButton("清空选择",(dialog,which)->callback.accept(new JSONArray())).setNegativeButton("取消",null).show();
    }
    private static void chooseCategory(ManagerActivity a,JSONObject payload,Runnable changed){List<JSONObject> rows=a.store().query("SELECT * FROM categories WHERE archived=0 AND kind=? AND level<>'legacy' ORDER BY position_weight DESC,id",payload.optString("kind"));String[] names=new String[rows.size()];for(int i=0;i<rows.size();i++)names[i]=rows.get(i).optString("name");new AlertDialog.Builder(a).setTitle("周期账单分类").setItems(names,(dialog,index)->{Json.put(payload,"category_id",rows.get(index).optLong("id"));payload.remove("parent_semantic_id");payload.remove("child_semantic_id");changed.run();}).show();}
    private static void chooseAccount(ManagerActivity a,JSONObject payload,boolean destination,Runnable changed){JSONObject book=a.store().book(payload.optLong("book_id"));if(book==null)throw new IllegalArgumentException("请先选择有效账本");List<JSONObject> rows=new ArrayList<>();for(JSONObject account:a.store().selectableAccounts(book.optLong("id"))){int type=account.optInt("asset_type");String host=account.optString("type");if(book.optString("currency").equals(account.optString("currency"))&&type!=6&&type!=7&&type!=8&&type!=9&&!host.equals("receivable")&&!host.equals("debt"))rows.add(account);}
        String[] names=new String[rows.size()+1];names[0]="无账户";for(int i=0;i<rows.size();i++)names[i+1]=rows.get(i).optString("name");new AlertDialog.Builder(a).setTitle(destination?"转入账户":"记账账户").setItems(names,(dialog,index)->{a.store().selectAccountForInput(payload,destination?"to_account_id":"account_id",index==0?0:rows.get(index-1).optLong("id"),false);changed.run();}).show();}
    private static String accountName(ManagerActivity a,long id){JSONObject value=a.store().assetRepository().byRowId(id);return value==null?"无账户":value.optString("name");}
    private static String selectionLabel(JSONArray values,String none){return values==null||values.length()==0?none:"已选 "+values.length()+" 个";}
    private static String modeName(String value){for(int i=0;i<MODES.length;i++)if(MODES[i].equals(value))return MODE_NAMES[i];return MODE_NAMES[0];}
    private static String frequencyName(String value){for(int i=0;i<FREQUENCIES.length;i++)if(FREQUENCIES[i].equals(value))return FREQUENCY_NAMES[i];return "月";}
    private static String kindName(String value){return value.equals("income")?"收入":value.equals("transfer")?"转账":"支出";}
    private static int parseInteger(String value){try{return Integer.parseInt(value.trim());}catch(RuntimeException error){throw new IllegalArgumentException("请输入有效的周期或次数");}}
    private static boolean flag(JSONObject value,String key,boolean fallback){Object raw=value.opt(key);return raw==null||raw==JSONObject.NULL?fallback:Boolean.TRUE.equals(raw)||raw instanceof Number&&((Number)raw).longValue()==1||"true".equals(raw);}
    private static final class Holder extends RecyclerView.ViewHolder{Holder(View view){super(view);}}
}
