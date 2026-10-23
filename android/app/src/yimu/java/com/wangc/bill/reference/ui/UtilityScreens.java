package com.wangc.bill.reference.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wangc.bill.reference.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

final class UtilityScreens {
    private UtilityScreens(){}
    static void render(ManagerActivity a){switch(a.page){case "search","range_records","trash"->records(a);case "backup"->backup(a);case "import"->imports(a);case "attachments"->attachments(a);case "settings"->settings(a);case "theme"->theme(a);case "profile"->profile(a);case "widgets"->widgets(a);case "help"->help(a);case "about"->about(a);}}
    static void records(ManagerActivity a){boolean trash=a.page.equals("trash"),search=a.page.equals("search");LinearLayout root=Ui.column(a);root.addView(Ui.toolbar(a,trash?"回收站":search?"搜索账单":"账单明细",null,null));EditText field=new EditText(a);field.setSingleLine(true);field.setHint("搜索备注、商户或分类");if(search)root.addView(field);RecyclerView list=new RecyclerView(a);list.setLayoutManager(new LinearLayoutManager(a));root.addView(list,new LinearLayout.LayoutParams(-1,0,1));Ui.content(a,root);long start=a.getIntent().getLongExtra("start",0),end=a.getIntent().getLongExtra("end",Long.MAX_VALUE);
        Runnable refresh=()->{List<JSONObject> rows=a.store().transactions(a.bookId(),start,end,search?field.getText().toString():null,trash);RecordAdapter adapter=new RecordAdapter(rows,null,row->{if(!trash){Ui.edit(a,row.optLong("id"),null);return;}new AlertDialog.Builder(a).setTitle("回收站账单").setItems(new String[]{"恢复账单","永久删除"},(d,w)->{if(w==0){a.store().trash(row.optLong("id"),false);a.reload();}else Ui.confirm(a,"永久删除账单","此操作无法撤销；有关联回款的账单必须先删除关联账单。",()->{a.store().purge(row.optLong("id"));a.reload();});}).show();});adapter.allowTrash=!trash;list.setAdapter(adapter);};refresh.run();
        if(search)field.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int p,int c,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){list.removeCallbacks(refresh);list.postDelayed(refresh,200);}public void afterTextChanged(Editable e){}});
    }
    static void backup(ManagerActivity a){View r=a.original("数据备份","activity_backup");Ui.click(r,"local_backup",a::requestBackup);Ui.click(r,"restore_backup",a::requestRestore);Ui.click(r,"local_backup_file_manager",a::requestRestore);Ui.text(r,"local_backup_path","通过系统文件选择器保存完整 ZIP 备份");Ui.click(r,"local_backup_move",a::requestBackup);Ui.click(r,"local_backup_path",a::requestBackup);
        for(String name:new String[]{"switch_remote","switch_many_device","switch_file_auto","auto_webdav_backup"})disable(r,name,"网络和自动同步未接入");
        for(String name:new String[]{"webdav_setup","webdav_backup","restore_webdav_backup","remote_backup_file_manager","webdav_sync","data_sync","auto_backup","history_data"})Ui.click(r,name,()->Ui.offline(a));
        Ui.hide(r,"data_clear","local_data_clear");
    }
    static void imports(ManagerActivity a){a.body("数据导入与导出",null,null);a.rows.addView(Ui.row(a,"导出当前账本 CSV",a.store().book(a.bookId()).optString("name")+" · UTF-8，包含账户、分类、标签和收支标志",a::requestCsvExport));a.rows.addView(Ui.row(a,"导入 CSV 账单","支持本实验版表头与常见中文表头；文件整体校验，重复文件跳过。",a::requestCsvImport));a.rows.addView(Ui.row(a,"完整备份与恢复","ZIP 包含全部账本、采集记录、周期规则、目标与附件",()->Ui.page(a,"backup")));}
    static void attachments(ManagerActivity a){a.body("账单附件",null,null);List<JSONObject> transactions=a.store().query("SELECT id,remark,time,attachments FROM transactions WHERE deleted=0 AND attachments<>'[]' ORDER BY time DESC");int count=0;for(JSONObject t:transactions){JSONArray files=Json.array(t.optString("attachments"));for(int i=0;i<files.length();i++){JSONObject file=files.optJSONObject(i);if(file==null)continue;count++;LinearLayout row=Ui.row(a,file.optString("name","附件"),Ui.date(t.optLong("time"),"yyyy-MM-dd")+" · "+t.optString("remark"),()->LocalFiles.openAttachment(a,file));row.setOnLongClickListener(v->{Ui.edit(a,t.optLong("id"),null);return true;});a.rows.addView(row);}}if(count==0)a.rows.addView(Ui.row(a,"暂无附件","在记账页面点击附件按钮添加图片、PDF或文本。",null));}
    static void settings(ManagerActivity a){View r=a.original("设置","activity_setting");
        View remarkMain=Ui.view(r,"switch_remark_main");if(remarkMain instanceof CompoundButton button){button.setChecked(OriginalSettings.remarkMain(ReferenceApplication.prefs()));button.setOnCheckedChangeListener((view,value)->ReferenceApplication.prefs().edit().putBoolean(OriginalSettings.REMARK_MAIN,value).apply());}
        preference(r,"switch_show_time",OriginalSettings.SHOW_TIME,OriginalSettings.DEFAULT_SHOW_TIME);preference(r,"navigation_font","navigation_labels",false);preference(r,"switch_blur_app","privacy_screen",false);
        preference(r,"switch_show_old_cost",OriginalSettings.SHOW_OLD_COST,OriginalSettings.DEFAULT_SHOW_OLD_COST);
        preference(r,"switch_remove_recent","remove_recent",false);
        Ui.click(r,"default_asset_layout",()->FinancialScreens.chooseAccount(a,a.currency(),id->{ReferenceApplication.prefs().edit().putLong("default_account",id).apply();a.reload();}));JSONObject account=a.store().one("SELECT name FROM accounts WHERE id=?",""+ReferenceApplication.prefs().getLong("default_account",0));Ui.text(r,"default_asset",account==null?"无账户":account.optString("name"));
        Ui.click(r,"navigation_font_layout",()->{SharedPreferences p=ReferenceApplication.prefs();p.edit().putBoolean("navigation_labels",!p.getBoolean("navigation_labels",false)).apply();a.reload();});
        Ui.click(r,"change_permission",()->Ui.page(a,"capture"));Ui.click(r,"icon_setting",()->Ui.page(a,"categories"));Ui.click(r,"quick_option",()->Ui.page(a,"capture_settings"));
        Ui.click(r,"function_module_layout",()->Ui.page(a,"capture"));Ui.click(r,"function_num_keyboard",()->Ui.edit(a,0,null));
        // Avoid functional-looking switches with no implementation in this experiment.
        for(String name:new String[]{"switch_lock","switch_gesture","switch_currency","switch_category_simple","switch_show_asset_icon","switch_location","screenshot_delete","switch_vibrator","switch_manual_btn_position","switch_add_bill_tip","switch_remark_history","switch_asset_tip","switch_show_repayment","switch_calendar_lunar","switch_exit_check"})disable(r,name,"此高级选项尚未接入实验版");
        for(String name:new String[]{"currency_layout","bill_num_color","font_size_layout","month_start_layout","calendar_week_start_layout","home_pull_type","float_ball_position_layout","float_ball_action_layout","tile_type_layout"}){View v=Ui.view(r,name);if(v!=null)v.setVisibility(View.GONE);}
    }
    static void preference(View r,String id,String key,boolean initial){View view=Ui.view(r,id);if(view instanceof CompoundButton b){b.setChecked(ReferenceApplication.prefs().getBoolean(key,initial));b.setOnCheckedChangeListener((button,value)->{ReferenceApplication.prefs().edit().putBoolean(key,value).apply();if(key.equals("privacy_screen"))Privacy.apply((android.app.Activity)r.getContext());});}}
    static void disable(View r,String name,String reason){View v=Ui.view(r,name);if(v instanceof CompoundButton b){b.setChecked(false);b.setEnabled(false);b.setContentDescription(reason);}else if(v!=null)v.setEnabled(false);}
    static void theme(ManagerActivity a){a.body("主题设置",null,null);a.rows.addView(Ui.row(a,"一木原版浅色主题","保留原 APK 配色、主页植物背景、图标和底部导航。",null));a.rows.addView(Ui.row(a,"底部导航文字",ReferenceApplication.prefs().getBoolean("navigation_labels",false)?"显示文字":"仅显示图标",()->{boolean value=ReferenceApplication.prefs().getBoolean("navigation_labels",false);ReferenceApplication.prefs().edit().putBoolean("navigation_labels",!value).apply();a.reload();}));a.rows.addView(Ui.row(a,"隐藏资产总额",ReferenceApplication.prefs().getBoolean("hide_balances",false)?"已隐藏":"显示金额",()->{boolean value=ReferenceApplication.prefs().getBoolean("hide_balances",false);ReferenceApplication.prefs().edit().putBoolean("hide_balances",!value).apply();a.reload();}));}
    static void profile(ManagerActivity a){a.body("本地个人资料",null,null);a.rows.addView(Ui.row(a,"昵称",ReferenceApplication.prefs().getString("nickname","本地用户"),()->Dialogs.input(a,"本地昵称",ReferenceApplication.prefs().getString("nickname","本地用户"),false,value->{String name=value.trim();if(name.isEmpty()||name.length()>40)throw new IllegalArgumentException("昵称需要1至40个字符");ReferenceApplication.prefs().edit().putString("nickname",name).apply();a.reload();})));a.rows.addView(Ui.row(a,"登录与会员","实验版跳过联网实现",()->Ui.offline(a)));}
    static void widgets(ManagerActivity a){a.body("桌面小组件",null,null);a.rows.addView(Ui.row(a,"本地记账快捷组件","显示当前账本本月收支，点击打开实验版。刷新只读取本地数据库。",()->{
        android.appwidget.AppWidgetManager manager=android.appwidget.AppWidgetManager.getInstance(a);android.content.ComponentName provider=new android.content.ComponentName(a,ReferenceWidget.class);if(Build.VERSION.SDK_INT>=26&&manager.isRequestPinAppWidgetSupported())manager.requestPinAppWidget(provider,null,null);else Ui.toast(a,"请在桌面长按空白处，选择一木对比实验小组件");}));}
    static void help(ManagerActivity a){a.body("使用指南",null,null);a.rows.addView(Ui.row(a,"自动记账测试步骤","1. 开启自动记账总开关和系统无障碍权限。\n2. 在应用适配列表确认目标应用已勾选。\n3. 打开目标应用的支付成功或订单详情页。\n4. 检查浮窗或待确认列表。\n5. 没有记录时打开诊断，区分没有事件、空节点树、页面门槛与金额解析失败。",()->Ui.page(a,"capture")));a.rows.addView(Ui.row(a,"原版规则的限制","原版适配器针对一木 6.3.0 所对应的应用界面。支付应用更新、页面结构或系统权限变化都可能使规则失效。诊断开关可对比事件节点与活动窗口两条读取路径。",()->Ui.page(a,"diagnostics")));a.rows.addView(Ui.row(a,"数据与备份","全部数据保存在本机。卸载前请导出完整 ZIP 备份；CSV 适合迁移账单，不包含附件。",()->Ui.page(a,"backup")));}
    static void about(ManagerActivity a){a.body("关于实验版",null,null);a.rows.addView(Ui.row(a,"一木采集对比实验","Android 本地实验，独立包名和数据库。原版布局与已核对解析代码用于行为对比；其他本地业务使用可读的重建代码。",null));a.rows.addView(Ui.row(a,"安全边界","未包含原生库、DEX 动态加载、广告或联网 SDK。无 INTERNET 权限，日志只记录采集阶段、包名、页面类名和计数。",()->Ui.page(a,"diagnostics")));a.rows.addView(Ui.row(a,"构建信息",com.wangc.bill.BuildConfig.VERSION_NAME+" ("+com.wangc.bill.BuildConfig.VERSION_CODE+")",null));a.rows.addView(Ui.row(a,"第三方开源许可","AndroidX / Apache License 2.0；本实验版未加载一木的联网或商业 SDK。",null));}
}
