package com.wangc.bill.reference.ui;

import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wangc.bill.reference.*;
import org.json.JSONObject;
import java.util.*;

/** Audited local bindings for the recovered book/account/category layouts. */
final class CatalogScreens {
    private CatalogScreens(){}
    static void render(ManagerActivity a){switch(a.page){
        case "books"->books(a);case "edit_book"->book(a);case "accounts"->accounts(a);
        case "new_account","edit_account"->account(a);case "account_detail"->movements(a);
        case "categories"->categories(a);case "edit_category"->category(a);
        case "tags"->tags(a);case "edit_tag"->tag(a);case "budget"->budget(a);
    }}
    interface Binder {void bind(View view,JSONObject row);}
    static void list(RecyclerView list,List<JSONObject> rows,String layout,Binder binder){
        list.setLayoutManager(new LinearLayoutManager(list.getContext()));
        list.setAdapter(new RecyclerView.Adapter<RecordAdapter.Holder>(){
            public int getItemCount(){return rows.size();}
            public RecordAdapter.Holder onCreateViewHolder(ViewGroup parent,int type){return new RecordAdapter.Holder(Ui.layout(parent.getContext(),layout));}
            public void onBindViewHolder(RecordAdapter.Holder h,int p){binder.bind(h.itemView,rows.get(p));}
        });
    }
    static void books(ManagerActivity a){View r=a.original("账本管理","activity_account_book");Ui.text(r,"right_text","新增");Ui.click(r,"right_text",()->a.open("edit_book",0));RecyclerView list=Ui.view(r,"account_book_list");list(list,a.store().books(),"item_account_book",(v,b)->{
        long id=b.optLong("id");JSONObject sums=a.store().totals(id,0,Long.MAX_VALUE);String currency=b.optString("currency");
        Ui.text(v,"asset_book_name",b.optString("name"));Ui.text(v,"system_book",id==a.bookId()?"当前账本":currency);Ui.show(v,"system_book",true);
        Ui.text(v,"pay",Money.text(sums.optLong("pay"),currency));Ui.text(v,"income",Money.text(sums.optLong("income"),currency));Ui.text(v,"num",sums.optLong("count")+"笔");Ui.text(v,"time",Ui.date(b.optLong("created"),"yyyy-MM-dd"));
        Ui.click(v,"book_edit",()->a.open("edit_book",id));v.setOnClickListener(x->{ReferenceApplication.prefs().edit().putLong("current_book",id).apply();Ui.toast(a,"已切换账本");a.reload();});
    });}
    static void book(ManagerActivity a){JSONObject old=a.entity>0?a.store().book(a.entity):null;if(a.entity>0&&old==null)throw new IllegalArgumentException("账本不存在");
        View r=a.original(a.entity>0?"编辑账本":"新增账本",a.entity>0?"activity_edit_account_book":"activity_add_account_book");EditText name=Ui.view(r,"account_book_name");name.setText(old==null?"":old.optString("name"));
        final String[] currency={old==null?"CNY":old.optString("currency")};ViewGroup currencyRow=Ui.view(r,"icon_layout");Ui.hide(r,"icon");for(int i=0;i<currencyRow.getChildCount();i++)if(currencyRow.getChildAt(i) instanceof TextView label)label.setText("账本币种");TextView currencyValue=Ui.label(a,currency[0],14);RelativeLayout.LayoutParams currencyParams=new RelativeLayout.LayoutParams(-2,-2);currencyParams.addRule(RelativeLayout.CENTER_VERTICAL);currencyParams.addRule(RelativeLayout.ALIGN_PARENT_END);currencyParams.setMarginEnd(Ui.dp(a,30));currencyRow.addView(currencyValue,currencyParams);Ui.click(r,"icon_layout",()->Dialogs.currencies(a,currency[0],value->{currency[0]=value;currencyValue.setText(value);}));for(String id:new String[]{"switch_hide_all_category","switch_show_transfer","switch_show_stock"}){View option=Ui.view(r,id);if(option!=null&&option.getParent() instanceof View row)row.setVisibility(View.GONE);}
        Ui.click(r,"btn_complete",()->{try{long id=a.store().saveBook(a.entity,name.getText().toString(),currency[0],old==null?0:old.optLong("budget"));if(a.entity==0)ReferenceApplication.prefs().edit().putLong("current_book",id).apply();a.finish();}catch(Exception e){Ui.error(a,e);}});
        Ui.click(r,"btn_delete",()->Ui.confirm(a,"删除空账本","仅能删除没有账单和采集记录的账本。",()->{a.store().removeBook(a.entity);if(a.bookId()==a.entity)ReferenceApplication.prefs().edit().putLong("current_book",a.store().books().get(0).optLong("id")).apply();a.finish();}));
    }
    static void accounts(ManagerActivity a){a.body("账户管理","新增",()->a.open("new_account",0));for(JSONObject row:a.store().accounts()){
        LinearLayout view=Ui.row(a,row.optString("name"),row.optString("currency")+" · "+Money.text(row.optLong("balance"),row.optString("currency")),()->a.open("account_detail",row.optLong("id")));view.setOnLongClickListener(v->{a.open("edit_account",row.optLong("id"));return true;});a.rows.addView(view);
    }}
    static void account(ManagerActivity a){JSONObject old=a.entity>0?a.store().one("SELECT * FROM accounts WHERE id=?",""+a.entity):null;if(a.entity>0&&old==null)throw new IllegalArgumentException("账户不存在");View r=a.original(old==null?"新增账户":"编辑账户","activity_add_capital");
        EditText name=Ui.view(r,"type_content"),opening=Ui.view(r,"type_number");name.setText(old==null?"":old.optString("name"));String[] currency={old==null?a.currency():old.optString("currency")};String[] type={old==null?"cash":old.optString("type")};opening.setText(Money.text(old==null?0:old.optLong("opening"),currency[0]));Ui.text(r,"currency",currency[0]);
        Ui.hide(r,"night_icon_layout","auto_income_layout","group_name_layout","account_book_layout","switch_add_total");Ui.text(r,"type_simple_name","账户类型");Ui.click(r,"asset_icon_layout",()->new AlertDialog.Builder(a).setTitle("账户类型").setItems(new String[]{"现金","电子钱包","银行卡","信用账户","投资账户"},(d,w)->{type[0]=new String[]{"cash","wallet","bank","credit","investment"}[w];Ui.toast(a,"已选择账户类型");}).show());
        Ui.click(r,"currency_layout",()->Dialogs.currencies(a,currency[0],value->{currency[0]=value;Ui.text(r,"currency",value);opening.setText("0");}));
        Ui.click(r,"btn_complete",()->{try{a.store().saveAccount(a.entity,name.getText().toString(),type[0],currency[0],Money.signed(opening.getText().toString(),currency[0]));a.finish();}catch(Exception e){Ui.error(a,e);}});
        Ui.hide(r,"remark","card_code","remark_title","card_code_title","type_simple_name","type_simple_title");Ui.text(r,"content_title","账户名称");Ui.text(r,"type_title","期初余额");
    }
    static void movements(ManagerActivity a){JSONObject account=a.store().one("SELECT * FROM accounts WHERE id=?",""+a.entity);if(account==null)throw new IllegalArgumentException("账户不存在");View r=a.original(account.optString("name"),"activity_asset_info");Ui.hide(r,"edit_layout","float_ball_layout","total_speech_layout","speech_layout","statistics","right_icon");Ui.click(r,"title",()->a.open("edit_account",a.entity));RecyclerView list=Ui.view(r,"data_list");list.setLayoutManager(new LinearLayoutManager(a));List<JSONObject> data=a.store().query("SELECT t.*,c.name category,c.icon category_icon,z.name account FROM transactions t LEFT JOIN categories c ON c.id=t.category_id LEFT JOIN accounts z ON z.id=t.account_id WHERE t.deleted=0 AND (t.account_id=? OR t.to_account_id=?) ORDER BY t.time DESC LIMIT 2000",""+a.entity,""+a.entity);
        LinearLayout header=Ui.column(a);header.addView(Ui.row(a,"余额 "+Money.text(a.store().accountBalance(a.entity),account.optString("currency")),account.optString("currency")+" · 点击编辑账户",()->a.open("edit_account",a.entity)));header.addView(Ui.row(a,"归档账户","保留已有账单，记账时不再显示此账户",()->Ui.confirm(a,"归档账户","已有账单与余额会保留。",()->{a.store().archiveAccount(a.entity);a.finish();})));list.setAdapter(new RecordAdapter(data,header,t->Ui.edit(a,t.optLong("id"),null)));
    }
    static void categories(ManagerActivity a){
        a.body("分类管理","新增",()->new AlertDialog.Builder(a).setTitle("新增分类")
            .setItems(new String[]{"支出一级分类","支出二级分类","收入分类","收入子分类"},(d,w)->{
                if(w==0)newCategory(a,CategoryRepository.PARENT,0,0);
                else if(w==2)newCategory(a,CategoryRepository.CHILD,9,0);
                else chooseCategoryParent(a,w==3);
            }).show());
        CategoryRepository repository=a.store().categoryRepository();
        a.rows.addView(Ui.section(a,"支出分类"));
        for(JSONObject parent:repository.all("expense"))if(CategoryRepository.PARENT.equals(parent.optString("level"))&&parent.optInt("archived")==0){
            categoryRow(a,parent,false);
            for(JSONObject child:a.store().query("SELECT * FROM categories WHERE level='child' AND parent_semantic_id=? AND archived=0 ORDER BY position_weight DESC,id",""+parent.optInt("semantic_id")))categoryRow(a,child,true);
        }
        a.rows.addView(Ui.section(a,"收入分类"));
        for(JSONObject top:repository.all("income"))if(CategoryRepository.CHILD.equals(top.optString("level"))&&top.optInt("parent_child_semantic_id")==0&&top.optInt("archived")==0){
            categoryRow(a,top,false);
            for(JSONObject child:a.store().query("SELECT * FROM categories WHERE level='child' AND parent_semantic_id=9 AND parent_child_semantic_id=? AND archived=0 ORDER BY position_weight DESC,id",""+top.optInt("semantic_id")))categoryRow(a,child,true);
        }
        for(String kind:new String[]{"expense","income"}){
            List<JSONObject> legacy=repository.legacy(kind);if(legacy.isEmpty())continue;
            a.rows.addView(Ui.section(a,kind.equals("expense")?"旧实验版支出分类":"旧实验版收入分类"));
            for(JSONObject row:legacy)a.rows.addView(Ui.row(a,row.optString("name"),"保留原有账单和分类，不自动合并",()->a.open("edit_category",row.optLong("id"))));
        }
    }
    private static void categoryRow(ManagerActivity a,JSONObject category,boolean nested){
        String description=a.store().categoryRepository().isHiddenInBook(category.optLong("id"),a.bookId())?"本账本已隐藏 · 点击编辑":"点击编辑";
        a.rows.addView(Ui.row(a,(nested?"　":"")+category.optString("name"),description,()->a.open("edit_category",category.optLong("id"))));
    }
    private static void newCategory(ManagerActivity a,String level,int parentSemanticId,int parentChildSemanticId){
        a.startActivity(new android.content.Intent(a,ManagerActivity.class).putExtra("page","edit_category")
            .putExtra("category_level",level).putExtra("parent_semantic_id",parentSemanticId)
            .putExtra("parent_child_semantic_id",parentChildSemanticId).putExtra("kind",parentSemanticId==9?"income":"expense"));
    }
    private static void chooseCategoryParent(ManagerActivity a,boolean income){
        List<JSONObject> parents=a.store().query(income?
            "SELECT * FROM categories WHERE level='child' AND parent_semantic_id=9 AND parent_child_semantic_id=0 AND archived=0 ORDER BY position_weight DESC,id":
            "SELECT * FROM categories WHERE level='parent' AND semantic_id<>9 AND archived=0 ORDER BY position_weight DESC,id");
        if(parents.isEmpty()){Ui.toast(a,"请先添加上级分类");return;}
        String[] labels=new String[parents.size()];for(int i=0;i<labels.length;i++)labels[i]=parents.get(i).optString("name");
        new AlertDialog.Builder(a).setTitle("选择上级分类").setItems(labels,(d,w)->newCategory(a,CategoryRepository.CHILD,income?9:parents.get(w).optInt("semantic_id"),income?parents.get(w).optInt("semantic_id"):0)).show();
    }
    static void category(ManagerActivity a){JSONObject old=a.entity>0?a.store().categoryRepository().byRowId(a.entity):null;if(a.entity>0&&old==null)throw new IllegalArgumentException("分类不存在");View r=a.original(a.entity==0?"新增分类":"编辑分类","activity_add_category");EditText name=Ui.view(r,"name");name.setText(old==null?"":old.optString("name"));String kind=old==null?a.getIntent().getStringExtra("kind"):old.optString("kind");if(kind==null)kind="expense";final String selectedKind=kind;String level=old==null?a.getIntent().getStringExtra("category_level"):old.optString("level");final String selectedLevel=level==null?(kind.equals("income")?CategoryRepository.CHILD:CategoryRepository.PARENT):level;final int parentId=old==null?a.getIntent().getIntExtra("parent_semantic_id",kind.equals("income")?9:0):old.optInt("parent_semantic_id");final int parentChildId=old==null?a.getIntent().getIntExtra("parent_child_semantic_id",0):old.optInt("parent_child_semantic_id");String[] icon={old==null?(kind.equals("income")?"ic_income_other":"ic_parent_other"):old.optString("icon")};Ui.image(r,"icon",icon[0]);Ui.hide(r,"menu_list","icon_list","night_icon_layout");
        CompoundButton hide=Ui.view(r,"switch_hide_account_book");hide.setChecked(old!=null&&a.store().categoryRepository().isHiddenInBook(old.optLong("id"),a.bookId()));
        ViewGroup hideLayout=Ui.view(r,"hide_layout");if(hideLayout.getChildAt(0) instanceof ViewGroup labels&&labels.getChildAt(0) instanceof TextView label)label.setText("在当前账本隐藏");Ui.text(r,"hide_tip","隐藏后不出现在当前账本的分类选择列表");
        Ui.click(r,"choice_icon",()->new AlertDialog.Builder(a).setTitle("选择图标").setItems(new String[]{"餐饮","购物","交通","住房","医疗","教育","礼物","工资","其他"},(d,w)->{icon[0]=new String[]{"ic_parent_restaurant","ic_parent_shopping","ic_parent_traffic","ic_parent_home","ic_parent_medical","ic_parent_education","ic_parent_gift","ic_income_wages","ic_parent_other"}[w];Ui.image(r,"icon",icon[0]);}).show());
        Ui.click(r,"save_btn",()->{try{synchronized(a.store()){android.database.sqlite.SQLiteDatabase db=a.store().getWritableDatabase();db.beginTransaction();try{
            long id;if(CategoryRepository.LEGACY.equals(selectedLevel))id=a.store().saveCategory(a.entity,name.getText().toString(),selectedKind,icon[0]);
            else if(CategoryRepository.PARENT.equals(selectedLevel))id=a.store().categoryRepository().saveParent(a.entity,name.getText().toString(),icon[0]);
            else id=a.store().categoryRepository().saveChild(a.entity,name.getText().toString(),icon[0],parentId,parentChildId);
            a.store().categoryRepository().setHiddenInBook(id,a.bookId(),hide.isChecked());db.setTransactionSuccessful();
        }finally{db.endTransaction();}}a.finish();}catch(Exception e){Ui.error(a,e);}});
        Ui.show(r,"delete_btn",a.entity>0&&CategoryRepository.LEGACY.equals(selectedLevel));Ui.click(r,"delete_btn",()->Ui.confirm(a,"归档旧分类","已有账单保留此分类。",()->{a.store().archiveCategory(a.entity);a.finish();}));
    }
    static void tags(ManagerActivity a){a.body("标签管理","新增",()->a.open("edit_tag",0));for(JSONObject tag:a.store().query("SELECT * FROM tags ORDER BY id"))a.rows.addView(Ui.row(a,tag.optString("name"),"点击编辑；在编辑页长按名称删除",()->a.open("edit_tag",tag.optLong("id"))));}
    static void tag(ManagerActivity a){JSONObject old=a.entity>0?a.store().one("SELECT * FROM tags WHERE id=?",""+a.entity):null;View r=a.original(old==null?"新增标签":"编辑标签","activity_add_tag");EditText name=Ui.view(r,"tag_name");name.setText(old==null?"":old.optString("name"));Ui.hide(r,"choice_parent_tag","auto_tag","auto_tag_info","choice_tag_color");Ui.click(r,"btn_complete",()->{try{String value=name.getText().toString().trim();if(value.isEmpty()||value.length()>100)throw new IllegalArgumentException("标签名称需要1至100个字符");if(old==null)a.store().saveTag(value);else a.store().getWritableDatabase().execSQL("UPDATE tags SET name=? WHERE id=?",new Object[]{value,a.entity});a.finish();}catch(Exception e){Ui.error(a,e);}});if(old!=null)name.setOnLongClickListener(v->{Ui.confirm(a,"删除标签","账单保留，标签关联将移除。",()->{a.store().deleteTag(a.entity);a.finish();});return true;});}
    static void budget(ManagerActivity a){View r=a.original("预算管理","activity_budget_manager");JSONObject b=a.store().book(a.bookId());long[] month=Ui.month(System.currentTimeMillis());JSONObject sums=a.store().totals(a.bookId(),month[0],month[1]);long limit=b.optLong("budget"),pay=sums.optLong("budget_pay");Ui.text(r,"month",Ui.date(month[0],"yyyy年MM月"));Ui.text(r,"month_budget",Money.text(limit,a.currency()));Ui.text(r,"month_pay",Money.text(pay,a.currency()));Ui.text(r,"budget_surplus",Money.text(limit-pay,a.currency()));Ui.text(r,"day_pay",Money.text(pay/Math.max(1,Calendar.getInstance().get(Calendar.DAY_OF_MONTH)),a.currency()));Ui.hide(r,"self_date_layout","add_category_budget","category_budget_list","budget_line_chart","progress_view_one","progress_view_two");Ui.click(r,"month_budget_layout",()->Dialogs.input(a,"本账本每月预算",Money.text(limit,a.currency()),true,value->{a.store().saveBook(b.optLong("id"),b.optString("name"),a.currency(),Math.max(0,Money.signed(value,a.currency())));a.reload();}));Ui.text(r,"budget_day_surplus","本地月预算");Ui.text(r,"budget_day_remain",limit==0?"点击预算金额设置":"剩余 "+Money.text(limit-pay,a.currency()));}
}
