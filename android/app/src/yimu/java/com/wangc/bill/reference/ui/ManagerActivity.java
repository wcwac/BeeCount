package com.wangc.bill.reference.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.wangc.bill.reference.*;
import com.wangc.bill.reference.capture.*;
import org.json.JSONObject;

public final class ManagerActivity extends AppCompatActivity {
    public String page;public long entity;public View template;public LinearLayout rows;
    private long pendingBook;
    public ReferenceStore store(){return ReferenceApplication.store();}
    public long bookId(){long id=ReferenceApplication.prefs().getLong("current_book",1);return store().book(id)!=null?id:store().books().get(0).optLong("id");}
    public String currency(){return store().book(bookId()).optString("currency");}
    @Override protected void onCreate(Bundle state){super.onCreate(state);page=getIntent().getStringExtra("page");if(page==null)page="settings";entity=getIntent().getLongExtra("id",0);pendingBook=state==null?bookId():state.getLong("pendingBook",bookId());}
    @Override protected void onResume(){super.onResume();reload();}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putLong("pendingBook",pendingBook);}
    public void reload(){if(isFinishing())return;try{
        switch(page){
            case "capture", "capture_settings", "apps", "diagnostics", "inbox", "rules"->CaptureScreens.render(this);
            case "books", "edit_book", "accounts", "new_account", "edit_account", "account_detail", "categories", "edit_category", "tags", "edit_tag", "budget"->CatalogScreens.render(this);
            case "goals", "edit_goal", "plans", "edit_plan", "lending", "reimbursement", "investments", "loans"->FinancialScreens.render(this);
            case "search", "range_records", "trash", "backup", "import", "attachments", "settings", "theme", "profile", "widgets", "help", "about"->UtilityScreens.render(this);
            default->{body("本地功能",null,null);rows.addView(Ui.row(this,"此入口未接入","可以继续使用本地记账、统计和自动采集功能。",null));}
        }
    }catch(Exception error){body("操作未完成",null,null);rows.addView(Ui.row(this,error.getMessage()==null?"读取失败":error.getMessage(),"点击重试",this::reload));}}
    public void body(String title,String right,Runnable action){template=null;LinearLayout column=Ui.column(this);column.addView(Ui.toolbar(this,title,right,action));ScrollView scroll=new ScrollView(this);rows=Ui.column(this);scroll.addView(rows,new ScrollView.LayoutParams(-1,-2));column.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));Ui.content(this,column);}
    public View original(String title,String resource){template=Ui.layout(this,resource);View back=Ui.view(template,"btn_back");if(back!=null){Ui.click(template,"btn_back",this::finish);Ui.text(template,"title",title);Ui.content(this,template);}else{LinearLayout column=Ui.column(this);column.addView(Ui.toolbar(this,title,null,null));column.addView(template,new LinearLayout.LayoutParams(-1,0,1));Ui.content(this,column);}return template;}
    public void open(String name,long id){startActivity(new Intent(this,ManagerActivity.class).putExtra("page",name).putExtra("id",id));}
    public void requestBackup(){pendingBook=bookId();startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/zip").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"一木实验版备份-"+Ui.date(System.currentTimeMillis(),"yyyyMMdd-HHmm")+".zip"),501);}
    public void requestRestore(){startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE),502);}
    public void requestCsvExport(){pendingBook=bookId();startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/csv").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"账单-"+Ui.date(System.currentTimeMillis(),"yyyyMMdd")+".csv"),503);}
    public void requestCsvImport(){pendingBook=bookId();startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE),504);}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;android.net.Uri uri=data.getData();
        if(request==501)FileTasks.run(this,"正在创建完整备份",()->{LocalFiles.exportBackup(this,uri);return true;},done->Ui.toast(this,"备份完成，包含账单和附件"));
        if(request==502)Ui.confirm(this,"恢复完整备份","恢复会替换当前本地数据并关闭自动采集。建议先导出当前备份。",()->FileTasks.run(this,"正在恢复本地数据",()->{LocalFiles.restoreBackup(this,uri);return true;},done->{PlanScheduler.sync();reload();Ui.toast(this,"恢复完成，请重新核对采集设置");}));
        if(request==503)FileTasks.run(this,"正在导出CSV",()->{LocalFiles.writeText(this,uri,CsvFiles.exportBook(pendingBook));return true;},done->Ui.toast(this,"CSV导出完成"));
        if(request==504)Ui.confirm(this,"导入CSV账单","账单将加入当前目标账本；同一文件重复导入会跳过。任何一行校验失败时整批回滚。",()->FileTasks.run(this,"正在校验并导入账单",()->CsvFiles.importBook(LocalFiles.readText(this,uri),pendingBook),counts->{reload();Ui.toast(this,"已导入 "+counts[0]+" 条，跳过重复 "+counts[1]+" 条");}));
    }
}
