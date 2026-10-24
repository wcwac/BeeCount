package com.wangc.bill.reference.ui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wangc.bill.reference.*;
import com.wangc.bill.reference.capture.*;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

final class CaptureScreens {
    static void render(ManagerActivity a){switch(a.page){case "capture"->capture(a);case "capture_settings"->settings(a);case "apps"->apps(a);case "inbox"->inbox(a);case "rules"->rules(a);default->diagnostics(a);}}
    private static void capture(ManagerActivity a){View v=a.original("自动记账","activity_auto");Ui.hide(v,"auto_notify_layout","screenshot_tip","delete_screenshot_layout");
        bind(v,"switch_auto_bill",AutoApps.enabled(),value->setEnabled(a,value));
        CompoundButton master=Ui.view(v,"switch_auto_bill");master.setContentDescription("启用自动记账");Ui.click(v,"log_mode",master::performClick);
        // Original AutoActivity.t0 opens the system grant screen; this switch reflects its grant.
        bind(v,"switch_overlays",OverlayPermission.granted(),value->a.capturePermissions().onOverlaySwitchRequested());
        // This retained host row shortcut uses the same direct request as the switch;
        // the original native onCreate's extra row bindings remain unproven.
        Ui.click(v,"overlays_layout",()->OverlayPermission.requestDirect(a));
        bind(v,"switch_auto_jump_accessibility",ReferenceApplication.prefs().getBoolean("accessibility_shortcut",OriginalSettings.DEFAULT_AUTO_JUMP_ACCESSIBILITY),value->ReferenceApplication.prefs().edit().putBoolean("accessibility_shortcut",value).apply());
        View screenshot=Ui.view(v,"switch_screenshot");if(screenshot instanceof CompoundButton button){button.setChecked(false);button.setOnCheckedChangeListener((b,on)->{if(on){button.setChecked(false);Ui.offline(a);}});}
        JSONObject status=ReferenceCaptureService.status();JSONObject meta=status.optJSONObject("diagnostics");
        Ui.show(v,"allow_run_back",true);
        Ui.text(v,"auto_tip",condition(status)+(status.optBoolean("enabled")?"\n下方总开关可关闭自动记账。":"\n系统授权后，还需开启下方“启用自动记账”。")+"\n"+(status.optBoolean("autoSave")?"自动新增已开启：支付凭据和账户明确时，浮窗显示5秒后保存；其余记录保留待确认。":OverlayPermission.granted()?"识别成功后生成本地待确认账单。":"识别结果保存在待确认列表；确认浮窗未授权，请点击“悬浮窗权限”授权。")+"\n最近处理："+result(meta.optString("result"))+"\n点击查看诊断。");Ui.click(v,"text_tip_layout",()->a.open("diagnostics",0));Ui.click(v,"auto_guide",()->a.open("help",0));Ui.click(v,"not_work_layout",()->a.open("auto_work",0));Ui.click(v,"allow_run_back",()->OriginalAutoWorkScreen.showCaptureBackgroundSettings(a));Ui.click(v,"auto_setting_layout",()->a.open("capture_settings",0));Ui.click(v,"notify_setting_layout",()->{if(Build.VERSION.SDK_INT>=26)a.startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,a.getPackageName()));});Ui.click(v,"app_setting_layout",()->a.open("apps",0));Ui.click(v,"auto_manager",()->a.open("rules",0));Ui.click(v,"auto_bill",()->a.open("inbox",0));Ui.click(v,"send_log",()->copy(a));Ui.text(v,"send_log","复制诊断信息");Ui.click(v,"delete_log",()->{ReferenceDiagnostics.clear();a.reload();});Ui.text(v,"delete_log","清空诊断计数");}
    private static void backgroundSettings(ManagerActivity a){
        String manufacturer=(Build.MANUFACTURER+" "+Build.BRAND).toLowerCase(java.util.Locale.ROOT);
        String guide;
        if(manufacturer.contains("huawei")||manufacturer.contains("honor"))guide="在手机管家或系统管家的“应用启动管理”中，检查本应用的自启动、关联启动和后台活动设置。";
        else if(manufacturer.contains("vivo"))guide="在应用信息中检查自启动和后台运行设置；在耗电管理中检查是否限制本应用的后台活动。";
        else if(manufacturer.contains("xiaomi")||manufacturer.contains("redmi"))guide="在应用信息中检查自启动，把后台省电配置设为允许持续运行；也可在近期任务中锁定本应用。";
        else guide="在应用信息中检查自启动、后台运行和省电限制；不同手机的设置名称可能不同。";
        new AlertDialog.Builder(a).setTitle("后台运行设置").setMessage(guide+"\n\n若切换应用后采集中断，可按需调整这些设置，再回来检查无障碍服务是否连接。")
            .setPositiveButton("应用信息",(dialog,which)->openSystemSetting(a,new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+a.getPackageName()))))
            .setNeutralButton("电池优化设置",(dialog,which)->openSystemSetting(a,new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)))
            .setNegativeButton("关闭",null).show();
    }
    private static void openSystemSetting(ManagerActivity a,Intent intent){try{a.startActivity(intent);}catch(RuntimeException ignored){Ui.toast(a,"无法打开此设置，请在系统设置中管理本应用");}}
    private static String condition(JSONObject status){return !status.optBoolean("enabled")?"自动记账已关闭，请开启应用内总开关":AutoApps.selected().isEmpty()?"尚未选择支持的应用":!status.optBoolean("permissionGranted")?"尚未获得无障碍权限":!status.optBoolean("connected")?"已授权，服务尚未连接":"自动记账已开启，服务已连接";}
    private static void setEnabled(ManagerActivity a,boolean enabled){a.capturePermissions().onMasterChanged(enabled);}
    static void bind(View root,String id,boolean checked,java.util.function.Consumer<Boolean> callback){View v=Ui.view(root,id);if(v instanceof CompoundButton button){button.setOnCheckedChangeListener(null);button.setChecked(checked);button.setOnCheckedChangeListener((b,value)->callback.accept(value));}}
    private static void settings(ManagerActivity a){View v=a.original("自动记账设置","activity_auto_setting");Ui.text(v,"default_asset",accountName(a,ReferenceApplication.prefs().getLong("default_account",0)));Ui.click(v,"default_asset_layout",()->accounts(a,id->{ReferenceApplication.prefs().edit().putLong("default_account",id).apply();a.reload();}));bind(v,"switch_add_remark",ReferenceApplication.prefs().getBoolean("add_remark",true),on->ReferenceApplication.prefs().edit().putBoolean("add_remark",on).apply());autoSaveSwitch(a,v);bind(v,"switch_choice_book",true,on->{chooseBook(a);});bind(v,"switch_close_total",ReferenceApplication.prefs().getBoolean("capture_not_total",false),on->ReferenceApplication.prefs().edit().putBoolean("capture_not_total",on).apply());bind(v,"switch_close_budget",ReferenceApplication.prefs().getBoolean("capture_not_budget",false),on->ReferenceApplication.prefs().edit().putBoolean("capture_not_budget",on).apply());bind(v,"switch_repeat_check",true,on->{Ui.toast(a,"按来源和支付凭据识别重复；缺少唯一凭据时保留待确认，不会自动入账");a.reload();});Ui.text(v,"window_style","详细");Ui.click(v,"window_style_layout",()->Ui.toast(a,"使用一木原版详细浮窗布局"));Ui.click(v,"choice_shop_layout",()->a.open("apps",0));Ui.click(v,"choice_book_layout",()->chooseBook(a));
        if(v instanceof ScrollView scroll && scroll.getChildAt(0) instanceof LinearLayout content)
            content.addView(Ui.row(a,"付款方式到账户映射","明确选择每个应用付款方式对应的本地账户；不猜测支付渠道背后的银行卡。",()->mappingPicker(a)));
    }
    private static void autoSaveSwitch(ManagerActivity a,View view){bind(view,"switch_auto_add",ReferenceApplication.prefs().getBoolean("auto_save",false),on->{if(!on){ReferenceApplication.prefs().edit().putBoolean("auto_save",false).apply();return;}bind(view,"switch_auto_add",false,ignored->{});AlertDialog dialog=new AlertDialog.Builder(a).setTitle("启用自动新增").setMessage("支付凭据和付款账户明确时，弹窗显示5秒后自动新增账单。缺少凭据、账户或有效账本的记录只保留待确认。选择分类、切换类型、账户、账本或日期，以及打开键盘编辑时，会停止倒计时，等待你手动确认；重复账单也需要手动确认。").setNegativeButton("取消",null).setPositiveButton("启用",(d,w)->ReferenceApplication.prefs().edit().putBoolean("auto_save",true).apply()).create();dialog.setOnDismissListener(d->{if(!a.isFinishing())a.reload();});dialog.show();});}
    private static void chooseBook(ManagerActivity a){List<JSONObject> books=new ArrayList<>();for(JSONObject b:a.store().books())if(b.optString("currency").equals("CNY"))books.add(b);if(books.isEmpty()){Ui.toast(a,"请先建立人民币账本");return;}String[] names=new String[books.size()];for(int i=0;i<names.length;i++)names[i]=books.get(i).optString("name");new AlertDialog.Builder(a).setTitle("采集目标账本").setItems(names,(d,w)->{ReferenceApplication.prefs().edit().putLong("capture_book",books.get(w).optLong("id")).apply();a.reload();}).show();}
    interface AccountResult{void value(long id);}
    static void accounts(ManagerActivity a,AccountResult callback){List<JSONObject> accounts=new ArrayList<>();for(JSONObject row:CaptureAccountMappings.choices(a.store(),ReferenceApplication.prefs().getLong("capture_book",a.bookId())))if(CaptureAccountMappings.eligible(row))accounts.add(row);String[] names=new String[accounts.size()+1];names[0]="无账户";for(int i=0;i<accounts.size();i++)names[i+1]=accounts.get(i).optString("name")+" · CNY";new AlertDialog.Builder(a).setTitle("选择账户").setItems(names,(d,w)->callback.value(w==0?0:accounts.get(w-1).optLong("id"))).show();}
    private static void mappingPicker(ManagerActivity a){
        String[] applications=new String[AutoApps.ALL.length];
        for(int i=0;i<applications.length;i++)applications[i]=AutoApps.ALL[i].name;
        new AlertDialog.Builder(a).setTitle("选择来源应用").setItems(applications,(d,w)->mappingMethod(a,AutoApps.ALL[w])).show();
    }
    private static void mappingMethod(ManagerActivity a,AutoApps.App app){
        java.util.LinkedHashSet<String> observed=new java.util.LinkedHashSet<>();
        for(JSONObject row:a.store().query("SELECT payload FROM candidates WHERE package_name=? ORDER BY created DESC LIMIT 200",app.packageName)){
            String method=Json.parse(row.optString("payload")).optString("asset_text").trim();if(!method.isEmpty())observed.add(method);
        }
        List<String> methods=new ArrayList<>(observed);String[] choices=new String[methods.size()+1];
        for(int i=0;i<methods.size();i++)choices[i]=methods.get(i);choices[methods.size()]="手动输入付款方式";
        new AlertDialog.Builder(a).setTitle(app.name+" · 付款方式").setItems(choices,(d,w)->{
            if(w<methods.size()){mappingAccount(a,app,methods.get(w));return;}
            EditText input=new EditText(a);input.setSingleLine(true);input.setHint("填写页面识别到的付款方式");
            AlertDialog entry=new AlertDialog.Builder(a).setTitle("付款方式").setView(input).setNegativeButton("取消",null).setPositiveButton("继续",null).create();
            entry.setOnShowListener(ignored->entry.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button->{String value=input.getText().toString().trim();if(value.isEmpty()){input.setError("请输入付款方式");return;}entry.dismiss();mappingAccount(a,app,value);}));entry.show();
        }).show();
    }
    private static void mappingAccount(ManagerActivity a,AutoApps.App app,String method){
        List<JSONObject> eligible=new ArrayList<>();for(JSONObject row:CaptureAccountMappings.choices(a.store(),ReferenceApplication.prefs().getLong("capture_book",a.bookId())))if(CaptureAccountMappings.eligible(row))eligible.add(row);
        String[] labels=new String[eligible.size()+1];labels[0]="明确不关联账户";for(int i=0;i<eligible.size();i++)labels[i+1]=eligible.get(i).optString("name");
        new AlertDialog.Builder(a).setTitle(app.name+" · "+method).setItems(labels,(d,w)->{CaptureAccountMappings.put(app.packageName,method,w==0?0:eligible.get(w-1).optLong("id"));Ui.toast(a,"映射已保存，对后续采集生效");})
                .setNeutralButton("移除此映射",(d,w)->{CaptureAccountMappings.remove(app.packageName,method);Ui.toast(a,"映射已移除");}).setNegativeButton("取消",null).show();
    }
    private static String accountName(ManagerActivity a,long id){JSONObject row=a.store().one("SELECT name FROM accounts WHERE id=?",""+id);return row==null?"无账户":row.optString("name");}
    private static void apps(ManagerActivity a) {
        View screen = a.original("配置支持的应用", "activity_auto_app_setting");
        RecyclerView list = Ui.view(screen, "app_list");
        list.setLayoutManager(new LinearLayoutManager(a));
        list.setAdapter(new RecyclerView.Adapter<MainActivity.SimpleHolder>() {
            @Override public int getItemCount() { return AutoApps.ALL.length; }
            @Override public MainActivity.SimpleHolder onCreateViewHolder(ViewGroup parent, int type) {
                LinearLayout row = new LinearLayout(a);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(android.view.Gravity.CENTER_VERTICAL);
                row.setPadding(Ui.dp(a,16), Ui.dp(a,12), Ui.dp(a,12), Ui.dp(a,12));
                row.setBackgroundColor(Ui.color(a,"white",0xffffffff));
                LinearLayout copy = new LinearLayout(a);
                copy.setOrientation(LinearLayout.VERTICAL);
                copy.setPadding(0,0,Ui.dp(a,12),0);
                copy.addView(Ui.label(a,"",15));
                TextView detail = Ui.label(a,"",12);
                detail.setTextColor(Ui.color(a,"textGray",0xff8a8a8a));
                detail.setPadding(0,Ui.dp(a,5),0,0);
                copy.addView(detail);
                row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
                Switch toggle = new Switch(a);
                toggle.setMinWidth(Ui.dp(a,48)); toggle.setMinHeight(Ui.dp(a,48));
                row.addView(toggle,new LinearLayout.LayoutParams(-2,-2));
                RecyclerView.LayoutParams bounds = new RecyclerView.LayoutParams(-1,-2);
                bounds.bottomMargin = Ui.dp(a,1); row.setLayoutParams(bounds);
                row.setOnClickListener(view -> toggle.performClick());
                return new MainActivity.SimpleHolder(row);
            }
            @Override public void onBindViewHolder(MainActivity.SimpleHolder holder, int position) {
                AutoApps.App app = AutoApps.ALL[position];
                LinearLayout row = (LinearLayout) holder.itemView;
                LinearLayout copy = (LinearLayout) row.getChildAt(0);
                ((TextView)copy.getChildAt(0)).setText(app.name);
                ((TextView)copy.getChildAt(1)).setText(app.description+"\n"+app.packageName);
                Switch toggle = (Switch) row.getChildAt(1);
                toggle.setContentDescription(app.name+"采集开关");
                toggle.setOnCheckedChangeListener(null);
                toggle.setChecked(AutoApps.selected().contains(app.packageName));
                toggle.setOnCheckedChangeListener((button,on) -> AutoApps.select(app.packageName,on));
                row.setOnLongClickListener(view -> {
                    List<JSONObject> tags = a.store().query("SELECT * FROM tags ORDER BY id");
                    String[] labels = new String[tags.size()+1]; labels[0]="不自动添加标签";
                    for(int i=0;i<tags.size();i++) labels[i+1]=tags.get(i).optString("name");
                    new AlertDialog.Builder(a).setTitle(app.name+"来源标签").setItems(labels,(dialog,which) ->
                        ReferenceApplication.prefs().edit().putString("source_tag_"+app.packageName,
                            which==0?"":""+tags.get(which-1).optLong("id")).apply()).show();
                    return true;
                });
            }
        });
    }
    private static void inbox(ManagerActivity a){
        boolean dismissed=a.getIntent().getBooleanExtra("show_dismissed",false);
        List<JSONObject> rows=dismissed?a.store().query("SELECT * FROM candidates WHERE state='DISMISSED' ORDER BY created DESC LIMIT 1000"):a.store().candidates();
        LinearLayout screen=Ui.column(a);a.template=null;
        screen.addView(Ui.toolbar(a,dismissed?"已忽略的采集记录":"自动记账待确认",dismissed?"待确认":"已忽略",()->{a.getIntent().putExtra("show_dismissed",!dismissed);a.reload();}));
        LinearLayout controls=new LinearLayout(a);
        for(String action:new String[]{"刷新",dismissed?"付款方式映射":"采集目标账本",dismissed?"返回采集设置":"批量忽略"}){
            TextView button=Ui.label(a,action,14);button.setGravity(android.view.Gravity.CENTER);button.setMinHeight(Ui.dp(a,48));
            controls.addView(button,new LinearLayout.LayoutParams(0,-2,1));
            button.setOnClickListener(v->{if(action.equals("刷新"))a.reload();else if(action.equals("采集目标账本"))chooseBook(a);else if(action.equals("付款方式映射"))mappingPicker(a);else if(action.equals("批量忽略"))dismissSelected(a,rows);else a.open("capture",0);});
        }
        screen.addView(controls);
        int unresolved=0;for(JSONObject row:rows)if(needsBook(a,row))unresolved++;
        TextView summary=Ui.label(a,rows.isEmpty()?(dismissed?"暂无已忽略记录":"暂无待确认记录"):
                "共 "+rows.size()+" 条"+(unresolved>0?"，其中 "+unresolved+" 条需要重新选择账本":"")+(dismissed?"。点击可恢复未保存的记录。":"。点击核对并记账，长按查看更多操作。"),13);
        summary.setPadding(Ui.dp(a,18),Ui.dp(a,12),Ui.dp(a,18),Ui.dp(a,12));screen.addView(summary);
        RecyclerView list=new RecyclerView(a);list.setLayoutManager(new LinearLayoutManager(a));screen.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        list.setAdapter(new RecyclerView.Adapter<MainActivity.SimpleHolder>(){
            @Override public int getItemCount(){return rows.size();}
            @Override public MainActivity.SimpleHolder onCreateViewHolder(ViewGroup p,int type){return new MainActivity.SimpleHolder(Ui.row(a,"","",null));}
            @Override public void onBindViewHolder(MainActivity.SimpleHolder holder,int position){
                JSONObject row=rows.get(position),data=Json.parse(row.optString("payload"));LinearLayout item=(LinearLayout)holder.itemView;
                ((TextView)item.getChildAt(0)).setText(AutoApps.name(row.optString("package_name"))+" · "+Money.text(data.optLong("amount"),data.optString("currency","CNY")));
                JSONObject book=a.store().book(row.optLong("book_id"));
                String target=needsBook(a,row)?"尚未选择有效账本（记录已保留）":book==null?"账本不存在":book.optString("name");
                ((TextView)item.getChildAt(1)).setText(data.optString("merchant")+"\n"+data.optString("remark")+"\n"+target+" · "+Ui.date(row.optLong("created"),"MM.dd HH:mm")
                    +(data.optLong("amount")==0?"\n金额缺失，请手动核对":"")
                    +(data.optBoolean("capture_duplicate_hint")?"\n疑似重复页面，请核对是否为另一笔支付":"")
                    +(data.optBoolean("capture_account_requires_confirmation")?"\n付款账户需核对，可在编辑页选择":""));
                item.setOnClickListener(v->{if(dismissed)restoreIgnored(a,row);else editCandidate(a,row);});
                item.setOnLongClickListener(v->{if(dismissed){restoreIgnored(a,row);return true;}
                    new AlertDialog.Builder(a).setTitle("处理采集记录").setItems(new String[]{"核对并记账","选择目标账本","显示确认浮窗","配置付款账户映射","忽略此记录"},(dialog,which)->{
                        if(which==0)editCandidate(a,row);
                        else if(which==1)chooseCandidateBook(a,row,()->a.reload());
                        else if(which==2)withCandidateBook(a,row,()->{JSONObject current=a.store().candidate(row.optString("id"));if(!LocalPlanCandidates.canShowCnyOverlay(a.store(),current)){Ui.toast(a,"此周期记录请在编辑页按原币种人工确认");return;}if(!AutoApps.enabled()||!OverlayPermission.granted()||!ReferenceApplication.prefs().getBoolean("show_overlay",true)){Ui.toast(a,"请先启用自动记账和确认浮窗，或直接选择核对并记账");return;}CaptureOverlay.show(row.optString("id"),null);});
                        else if(which==3){AutoApps.App app=appFor(row.optString("package_name"));if(app==null)mappingPicker(a);else mappingMethod(a,app);}
                        else Ui.confirm(a,"忽略此记录","不会记入账本；之后可在已忽略列表恢复。",()->{ignoreCandidate(a,row.optString("id"));a.reload();});
                    }).show();return true;});
            }
        });Ui.content(a,screen);
    }
    private static AutoApps.App appFor(String pkg){for(AutoApps.App app:AutoApps.ALL)if(app.packageName.equals(pkg))return app;return null;}
    private static boolean needsBook(ManagerActivity a,JSONObject row){
        if(LocalPlanCandidates.isLocalPlan(row))return LocalPlanCandidates.needsBook(a.store(),row);
        JSONObject data=Json.parse(row.optString("payload")),book=a.store().book(row.optLong("book_id"));
        return data.optBoolean("capture_target_book_unresolved")||book==null||book.optInt("archived")!=0||!"CNY".equals(book.optString("currency"));
    }
    private static void withCandidateBook(ManagerActivity a,JSONObject row,Runnable ready){if(needsBook(a,row))chooseCandidateBook(a,row,ready);else ready.run();}
    private static void editCandidate(ManagerActivity a,JSONObject row){withCandidateBook(a,row,()->{CaptureOverlay.close();Ui.edit(a,0,row.optString("id"));});}
    private static void chooseCandidateBook(ManagerActivity a,JSONObject row,Runnable ready){
        boolean localPlan=LocalPlanCandidates.isLocalPlan(row);String currency;
        try{currency=localPlan?LocalPlanCandidates.currency(row):"CNY";}catch(IllegalArgumentException error){Ui.error(a,error);return;}
        List<JSONObject> books=new ArrayList<>();for(JSONObject book:a.store().books())if(localPlan?LocalPlanCandidates.canUseBook(row,book):"CNY".equals(book.optString("currency")))books.add(book);
        if(books.isEmpty()){new AlertDialog.Builder(a).setTitle(localPlan?"需要同币种账本":"需要人民币账本").setMessage(localPlan?"周期记录已保留，请建立可用的 "+currency+" 账本后回来选择；金额不会自动换算。":"采集记录已经保留，请先建立人民币账本，再回来选择。")
                .setPositiveButton("新建账本",(dialog,which)->a.open("edit_book",0)).setNegativeButton("暂不处理",null).show();return;}
        String[] names=new String[books.size()];for(int i=0;i<names.length;i++)names[i]=books.get(i).optString("name")+(localPlan?" · "+currency:"");
        CheckBox future=new CheckBox(a);future.setText("同时作为后续采集的目标账本");future.setChecked(!localPlan);
        new AlertDialog.Builder(a).setTitle(localPlan?"选择周期记录的同币种账本":"选择此记录的目标账本").setView(localPlan?null:future).setItems(names,(dialog,which)->{
            try{long chosen=books.get(which).optLong("id");CaptureInboxActions.resolveBook(a.store(),row.optString("id"),chosen);
                if(!localPlan&&future.isChecked())ReferenceApplication.prefs().edit().putLong("capture_book",chosen).apply();ready.run();}
            catch(Exception error){Ui.error(a,error);}
        }).setNegativeButton("取消",null).show();
    }
    private static void restoreIgnored(ManagerActivity a,JSONObject row){Ui.confirm(a,"恢复为待确认","恢复后仍需手动核对，不会自动入账。",()->{
        CaptureInboxActions.reopen(a.store(),row.optString("id"));CaptureNotifications.update();a.getIntent().putExtra("show_dismissed",false);a.reload();
    });}
    private static void ignoreCandidate(ManagerActivity a,String id){a.store().dismiss(id);CaptureOverlay.complete(id);CaptureNotifications.update();}
    private static void dismissSelected(ManagerActivity a,List<JSONObject> rows){
        if(rows.isEmpty()){Ui.toast(a,"没有待确认记录");return;}String[] labels=new String[rows.size()];boolean[] checked=new boolean[rows.size()];
        for(int i=0;i<labels.length;i++){JSONObject row=rows.get(i),data=Json.parse(row.optString("payload"));labels[i]=AutoApps.name(row.optString("package_name"))+" · "+Money.text(data.optLong("amount"),data.optString("currency","CNY"))+" · "+data.optString("merchant");}
        new AlertDialog.Builder(a).setTitle("选择要忽略的记录").setMultiChoiceItems(labels,checked,(dialog,which,value)->checked[which]=value)
                .setPositiveButton("忽略所选",(dialog,which)->{try{for(int i=0;i<checked.length;i++)if(checked[i])ignoreCandidate(a,rows.get(i).optString("id"));a.reload();}catch(Exception error){Ui.error(a,error);}}).setNegativeButton("取消",null).show();
    }
    private static void rules(ManagerActivity a){a.body("自动修正管理",null,null);a.rows.addView(Ui.row(a,"商户沿用规则","在账单编辑的配置中开启“记住此商户”，后续会沿用所选分类和账户。",null));for(JSONObject rule:a.store().query("SELECT r.*,c.name category,a.name account FROM rules r LEFT JOIN categories c ON c.id=r.category_id LEFT JOIN accounts a ON a.id=r.account_id ORDER BY r.id"))a.rows.addView(Ui.row(a,rule.optString("merchant"),rule.optString("category")+" · "+rule.optString("account","无账户"),()->Ui.confirm(a,"删除修正规则",rule.optString("merchant"),()->{a.store().deleteSimple("rules",rule.optLong("id"));a.reload();})));}
    private static void diagnostics(ManagerActivity a){
        a.body("自动记账诊断","刷新",a::reload);
        JSONObject status=ReferenceCaptureService.status(),meta=status.optJSONObject("diagnostics");
        diagnosticMaster(a);
        a.rows.addView(Ui.row(a,"当前状态",condition(status)+"\n最近处理："+result(meta.optString("result")),null));
        a.rows.addView(Ui.row(a,"授权与连接","应用内总开关："+(status.optBoolean("enabled")?"已开启":"已关闭")+"\n无障碍授权："+(status.optBoolean("permissionGranted")?"已授权":"未授权")+"\n服务运行："+(status.optBoolean("running")?"正在运行":"未运行")+"\n服务连接："+(status.optBoolean("connected")?"已连接":"未连接")+"\n窗口内容读取："+(status.optBoolean("canRetrieveWindowContent")?"已允许":"未允许")+"（能力值 "+status.optInt("capabilities")+"）\n点击管理系统无障碍授权",()->a.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))));
        a.rows.addView(Ui.row(a,"授权悬浮窗",OverlayPermission.granted()?"已授权；点击管理“显示在其他应用上层”权限":"未授权；点击授予“显示在其他应用上层”权限，确认浮窗需要此权限",()->{
            if(OverlayPermission.granted())OverlayPermission.requestDirect(a);else OverlayPermission.request(a);
        }));
        a.rows.addView(Ui.row(a,"后台运行设置","查看手机自启动与省电设置。",()->backgroundSettings(a)));
        long target=ReferenceApplication.prefs().getLong("capture_book",1);JSONObject book=a.store().book(target);
        a.rows.addView(Ui.row(a,"原版无障碍工具标记",status.optBoolean("toolIdentity")?"已启用：用于对比敏感节点读取":"未启用或系统不支持",null));
        a.rows.addView(Ui.row(a,"采集目标账本",book==null?"不存在":book.optString("name")+" · "+book.optString("currency"),()->chooseBook(a)));
        a.rows.addView(Ui.row(a,"所选应用与实际监听","已选 "+AutoApps.selected().size()+" 个\n实际包名："+status.optJSONArray("effectivePackages")+"\nflags="+status.optInt("flags")+"，eventTypes="+status.optInt("eventTypes"),()->a.open("apps",0)));
        a.rows.addView(Ui.row(a,"本次运行计数","事件 "+meta.optLong("events")+" · 读取 "+meta.optLong("reads")+" · 处理器提交 "+meta.optLong("submissionAttempts")+" · 草稿 "+meta.optLong("drafts")+"\n读取开始 "+meta.optLong("readsStarted")+" · 中止 "+meta.optLong("readsAborted")+"\n内容事件合并 "+meta.optLong("coalesced")+" · 状态事件让位 "+meta.optLong("evicted")+" · 队列跳过 "+meta.optLong("skipped"),null));
        a.rows.addView(Ui.row(a,"当前处理阶段",stage(meta.optString("activeStage"))+"\n"+meta.optString("activePackage")+" "+meta.optString("activePageClass")+"\n已处理 "+meta.optLong("activeForMs")+" ms · 入队等待 "+meta.optLong("queueWaitMs")+" ms\n最近访问 "+meta.optInt("liveVisitedNodes")+" 个节点 · 队列剩余 "+status.optInt("workerQueueSize"),null));
        a.rows.addView(Ui.row(a,"页面节点来源",sourcePolicy(status.optString("sourcePolicy"))+"\n事件节点为空 "+meta.optLong("eventSourceNull")+" 次 · 获取异常 "+meta.optLong("eventSourceErrors")+" 次\n回退尝试 "+meta.optLong("fallbackAttempts")+" 次 · 成功 "+meta.optLong("fallbackSuccess")+" 次\n累计来源查询 "+meta.optLong("sourceLookups")+" 次 · 窗口候选 "+meta.optLong("sourceWindows")+" 个 · 异常 "+meta.optLong("sourceErrors")+" 次",null));
        a.rows.addView(Ui.row(a,"最近节点解析","来源："+sourceRoute(meta.optString("lastSourceRoute"))+"\n结果："+sourceReason(meta.optString("lastSourceReason"))+"\n事件窗口："+meta.optInt("lastSourceWindowId",-1)+"\n事件来源异常类型："+empty(meta.optString("lastEventSourceErrorType"))+"\n窗口查询异常类型："+empty(meta.optString("lastSourceErrorType"))+"\n最近草稿来源："+sourceRoute(meta.optString("lastDraftSourceRoute")),null));
        a.rows.addView(Ui.row(a,"当前工作线程",meta.optJSONArray("workerStack")==null?"暂无任务":meta.optJSONArray("workerStack").toString(),null));
        JSONObject overlay=status.optJSONObject("overlay");
        a.rows.addView(Ui.row(a,"确认浮窗",(status.optBoolean("overlayEnabled")?"浮窗开关已开启":"浮窗开关已关闭")+"\n"+(status.optBoolean("autoSave")?"自动新增已开启：确认浮窗显示5秒后新增账单；重复或编辑会停止倒计时":"识别成功后生成待确认账单")+"\n窗口状态："+overlayState(overlay==null?"":overlay.optString("lastResult"))+"\n使用与原版一致的应用悬浮窗，需要系统“显示在其他应用上层”授权。",()->a.open("capture",0)));
        Switch display=new Switch(a);display.setText("显示确认浮窗");display.setChecked(status.optBoolean("overlayEnabled"));display.setOnCheckedChangeListener((button,on)->{ReferenceApplication.prefs().edit().putBoolean("show_overlay",on).apply();if(!on)CaptureOverlay.close();a.reload();});a.rows.addView(display);
        a.rows.addView(Ui.row(a,"改为浮窗确认","关闭5秒自动新增并开启确认浮窗，后续识别结果由你核对后保存。",()->{
            ReferenceApplication.prefs().edit().putBoolean("auto_save",false).putBoolean("show_overlay",true).apply();
            a.reload();Ui.toast(a,"已改为浮窗确认");
        }));
        a.rows.addView(Ui.row(a,"测试确认浮窗","仅验证窗口显示，不生成或保存测试账单。",CaptureOverlay::preview));
        a.rows.addView(Ui.row(a,"各应用事件计数",meta.optJSONObject("eventsByPackage")==null?"暂无事件":meta.optJSONObject("eventsByPackage").toString(),null));
        a.rows.addView(Ui.row(a,"最近事件",meta.optString("package")+"\n"+meta.optString("pageClass")+"\n"+(meta.optLong("lastEventAt")>0?Ui.date(meta.optLong("lastEventAt"),"MM.dd HH:mm:ss"):"暂无事件"),null));
        a.rows.addView(Ui.row(a,"最近异常（保留至手动清空）",meta.optLong("failures")==0?"暂无异常":meta.optLong("failures")+" 次 · "+meta.optString("lastFailureCode")+"\n"+meta.optString("lastFailureType")+"\n"+Ui.date(meta.optLong("lastFailureAt"),"MM.dd HH:mm:ss"),null));
        a.rows.addView(Ui.row(a,"最近原版遍历","文字项 "+meta.optInt("textCount")+" · 遍历分支 "+meta.optInt("visitedBranches"),null));
        a.rows.addView(Ui.row(a,"读取策略说明","优先读取事件所属应用的同一完整窗口，窗口不可用时保留事件节点。只访问同包、同窗口的新鲜页面，不读取后台其他应用。",null));
        a.rows.addView(Ui.row(a,"付款方式到账户映射","配置识别付款方式对应的本地账户，歧义会保留为待确认。",()->mappingPicker(a)));
        a.rows.addView(Ui.row(a,"复制诊断信息","只复制开关、包名、页面类、时间、计数、处理阶段与结果码。",()->copy(a)));
        a.rows.addView(Ui.row(a,"打开待确认列表","已有 "+a.store().candidates().size()+" 条记录",()->a.open("inbox",0)));
    }
    private static String stage(String value){return switch(value){case "RESOLVING_SOURCE"->"正在获取同一应用窗口节点";case "READING_NODES"->"正在访问页面节点";case "PARSING_RULES"->"正在执行原版识别规则";case "DISPATCHING_RULES"->"正在进入原版处理器";case "READ_ABORTED"->"本次读取已中止";default->"等待页面事件";};}
    private static String sourcePolicy(String value){return "COMPLETE_SAME_WINDOW_FIRST".equals(value)||"ACTIVE_WINDOW_FIRST".equals(value)?"优先同一应用窗口，再尝试事件节点":"优先事件节点，失败后回退同包同窗口";}
    private static String sourceRoute(String value){return switch(value){case "EVENT_SOURCE"->"事件节点";case "ACTIVE_ROOT"->"同一应用的活动窗口";case "INTERACTIVE_WINDOW"->"同一应用的交互窗口";default->"暂无可用来源";};}
    private static String sourceReason(String value){return switch(value){case "RESOLVED"->"已获取可用节点";case "UNAVAILABLE"->"尚无可用节点";case "EXPIRED"->"事件已过期，停止回退";case "DEADLINE"->"获取节点超时";case "PACKAGE_MISMATCH"->"来源应用与事件不同，已拒绝";case "WINDOW_MISMATCH"->"来源窗口与事件不同，已拒绝";case "LOOKUP_FAILED"->"查询来源发生异常";default->"尚未获取节点";};}
    private static String empty(String value){return value.isEmpty()?"无":value;}
    private static String overlayState(String value){return switch(value){case "SHOWN"->"已显示";case "MANUAL_CONFIRMATION_REQUIRED"->"需要核对后手动保存";case "CLOSED"->"已关闭";case "SHOW_FAILED"->"显示失败，请查看最近异常";case "CREATING_WINDOW"->"正在创建窗口";case "SERVICE_DISCONNECTED"->"无障碍服务未连接";case "CAPTURE_DISABLED"->"自动记账未开启";case "OVERLAY_DISABLED"->"浮窗开关关闭";case "OVERLAY_PERMISSION_REQUIRED"->"尚未获得系统悬浮窗权限";case "NO_PENDING_DRAFT"->"没有待确认账单";case "BOOK_UNAVAILABLE"->"目标账本不可用";case "WINDOW_ALREADY_SHOWING"->"已有确认浮窗，新采集记录保留在待确认列表";case "SAVING"->"正在新增账单";case "AUTO_SAVED"->"5秒倒计时结束，账单已新增";case "SAVED"->"账单已新增";case "SAVE_FAILED"->"新增失败，账单仍在待确认列表";case "SAVE_CANCELLED"->"本次新增已取消，尚未保存的记录保留在待确认列表";case "SAVE_QUEUE_FULL"->"采集任务繁忙，请稍后重试新增";case "AUXILIARY_SHOW_FAILED"->"选择或重复确认窗口显示失败";default->"尚未触发，需要先识别出账单";};}
    private static void diagnosticMaster(ManagerActivity a){Switch master=new Switch(a);master.setId(com.wangc.bill.R.id.switch_auto_bill);master.setText("启用自动记账（应用内总开关）");master.setContentDescription("启用自动记账");master.setMinHeight(Ui.dp(a,60));master.setPadding(Ui.dp(a,18),Ui.dp(a,8),Ui.dp(a,18),Ui.dp(a,8));master.setChecked(AutoApps.enabled());master.setOnCheckedChangeListener((button,on)->setEnabled(a,on));a.rows.addView(master,new LinearLayout.LayoutParams(-1,-2));}
    private static void copy(ManagerActivity a){JSONObject status=ReferenceCaptureService.status();((ClipboardManager)a.getSystemService(android.content.Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("自动记账诊断",status.toString()));Ui.toast(a,"已复制诊断信息");}
    private static String result(String value){return switch(value){case "NO_EVENT"->"尚未收到所选应用的事件";case "CONNECTED"->"服务已连接，等待交易页面";case "EVENT_RECEIVED"->"收到页面事件，等待处理";case "SOURCE_UNAVAILABLE"->"事件与同一应用窗口均未提供可用节点";case "SOURCE_EXPIRED"->"事件已过期，本次不再读取窗口";case "SOURCE_LOOKUP_TIMEOUT"->"获取窗口节点超时，本次未生成账单";case "SOURCE_WINDOW_MISMATCH"->"事件与来源应用或窗口不一致，已跳过";case "SOURCE_LOOKUP_FAILED"->"获取窗口节点发生异常，请查看来源诊断";case "INVALID_AMOUNT"->"原版规则提交的金额格式无效";case "STOPPED"->"采集已关闭或来源应用未勾选";case "WINDOW_CHANGED"->"事件与节点所属应用不同，已跳过";case "EMPTY_TREE"->"原版遍历未读取到文字";case "PAGE_GATE_NOT_MATCHED"->"未进入原版处理器支持的页面状态";case "NO_BILL_FROM_ORIGINAL_RULES"->"原版规则未提交账单，请核对页面与客户端版本";case "DRAFT_SAVED"->"已生成待确认账单";case "DRAFT_REUSED"->"重复页面已合并到已有待确认账单";case "DRAFT_REVIEW_ONLY"->"同页重绘已保留待确认，不重复弹窗；如为另一笔付款可在列表手动确认";case "PAYMENT_ALREADY_CAPTURED"->"此支付凭据已有已处理记录，未重复入账";case "PAYMENT_EVIDENCE_CONFLICT"->"同一支付凭据的金额或类型不一致，未重复入账，请核对已保存账单";case "AUTO_SAVED"->"5秒倒计时结束，已新增账单";case "SAVE_CANCELLED"->"本次新增已取消，请核对待确认列表";case "BILL_SAVED"->"已新增账单";case "TARGET_BOOK_UNAVAILABLE"->"目标人民币账本不存在或不可用";case "DRAFT_WAITING_FOR_BOOK"->"记录已保留，请在待确认列表选择目标账本";case "NO_INBOX_STORAGE"->"没有可用的本地账本保存采集记录，请先建立账本";case "QUEUE_FULL"->"采集队列拥堵，有页面事件被跳过";case "PROCESSING_FAILED"->"原版采集处理发生异常";case "RESTORE_PARSER_RESET_FAILED"->"恢复后采集处理器未能复位，请重新启动应用";case "STORE_FAILED","PLAN_STORE_FAILED"->"本地保存失败，请核对待确认列表";case "OVERLAY_UNAVAILABLE"->"浮窗未显示，账单仍在待确认列表";case "NODE_READ_NODE_LIMIT","NODE_READ_TEXT_LIMIT","NODE_READ_DEPTH_LIMIT"->"页面节点超过读取保护边界，本次未生成账单";case "NODE_READ_TIME_LIMIT"->"读取节点超时，本次页面已停止处理";case "NODE_READ_CYCLE"->"检测到循环页面节点，本次读取已停止";case "NODE_READ_INTERRUPTED"->"本次节点读取已中断";case "WORKER_STOPPED"->"采集线程已停止";case "DISCONNECTED"->"服务已断开，请重新开启无障碍服务";default->value;};}
}
