package com.wangc.bill.reference.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import com.wangc.bill.auto.x1;
import com.wangc.bill.reference.*;
import com.wangc.bill.reference.capture.*;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Original automatic-entry XML hosted by the explicitly enabled accessibility service. */
public final class CaptureOverlay {
    private static volatile View shown;private static WindowManager manager;private static String candidate;private static AlertDialog accountDialog;
    private static volatile String lastResult="NOT_REQUESTED";
    private static volatile long lastChanged;
    private CaptureOverlay(){}
    private static void state(String value){lastResult=value;lastChanged=System.currentTimeMillis();}
    /** Automatic saving intentionally has no confirmation-window request. */
    public static void autoSaved(){state("SKIPPED_AUTO_SAVE");}
    public static JSONObject status(){String outcome=lastResult;View view=shown;return Json.object("attached",view!=null&&view.isAttachedToWindow(),"lastResult",outcome,"lastChangedAt",lastChanged,"windowType",WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);}
    private static boolean ready(ReferenceCaptureService service){if(service==null){state("SERVICE_DISCONNECTED");return false;}if(!AutoApps.enabled()){state("CAPTURE_DISABLED");return false;}if(!ReferenceApplication.prefs().getBoolean("show_overlay",true)){state("OVERLAY_DISABLED");return false;}return true;}
    /** An explicit window test. Its sample values are never inserted into the database. */
    public static void preview(){ReferenceCaptureService service=ReferenceCaptureService.current();if(!ready(service)){Ui.toast(ReferenceApplication.app(),"请先开启自动记账、连接无障碍服务，并开启确认浮窗");return;}long book=ReferenceApplication.prefs().getLong("capture_book",1);if(ReferenceApplication.store().book(book)==null){state("BOOK_UNAVAILABLE");Ui.toast(service,"请先选择可用账本");return;}JSONObject data=Json.object("kind","expense","amount",123,"discount",0,"fee",0,"time",System.currentTimeMillis(),"remark","测试浮窗，不生成账单","account_id",0,"category_id",ReferenceApplication.store().defaultCategory("expense"));display(service,null,Json.object("book_id",book,"payload",data.toString()),true);}
    public static void show(String id,x1.a callback){
        ReferenceCaptureService service=ReferenceCaptureService.current();if(!ready(service))return;
        JSONObject row=ReferenceApplication.store().candidate(id);if(row==null||!row.optString("state").equals("PENDING")){state("NO_PENDING_DRAFT");return;}
        display(service,id,row,false);
    }
    private static void display(ReferenceCaptureService service,String id,JSONObject row,boolean preview){
        close();
        state("CREATING_WINDOW");
        try{
            Context c=new ContextThemeWrapper(service,com.wangc.bill.R.style.ReferenceTheme);View view=Ui.layout(c,"view_auto_add_new");JSONObject data=Json.parse(row.optString("payload"));
            candidate=id;shown=view;manager=(WindowManager)service.getSystemService(Context.WINDOW_SERVICE);
            Ui.text(view,"account_book_name",ReferenceApplication.store().book(row.optLong("book_id")).optString("name"));Ui.image(view,"account_book_icon","ic_main_account_book");
            Ui.text(view,"cost",Money.text(data.optLong("amount"),"CNY"));Ui.text(view,"num_symbol",data.optString("kind").equals("income")?"+":"-");Ui.text(view,"date",Ui.date(data.optLong("time"),"MM.dd"));Ui.text(view,"remark",data.optString("remark"));
            Ui.text(view,"discount_edit",Money.text(data.optLong("discount"),"CNY"));Ui.text(view,"service_charge_edit",Money.text(data.optLong("fee"),"CNY"));
            Ui.hide(view,"lend_date_layout","refund_in_date_layout","reimbursement_layout","file_layout","tag_list","file_list","asset_num_tip","repeat_tip");
            Runnable render=()->{
                Ui.text(view,"asset_name",accountName(data.optLong("account_id")));Ui.text(view,"num_symbol",data.optString("kind").equals("income")?"+":data.optString("kind").equals("expense")?"-":"");
                FrameLayout container=Ui.view(view,"category_pager");container.removeAllViews();
                if(data.optString("kind").equals("transfer")){LinearLayout column=Ui.column(c);column.addView(Ui.row(c,"转出账户",accountName(data.optLong("account_id")),()->edit(c,id,preview)));column.addView(Ui.row(c,"转入账户",accountName(data.optLong("to_account_id")),()->edit(c,id,preview)));container.addView(column);return;}
                List<JSONObject> cats=ReferenceApplication.store().categories(data.optString("kind"));GridView grid=new GridView(c);grid.setNumColumns(5);grid.setVerticalSpacing(Ui.dp(c,4));grid.setAdapter(new BaseAdapter(){public int getCount(){return cats.size();}public Object getItem(int i){return cats.get(i);}public long getItemId(int i){return i;}public View getView(int i,View old,ViewGroup parent){JSONObject category=cats.get(i);LinearLayout cell=new LinearLayout(c);cell.setOrientation(LinearLayout.VERTICAL);cell.setGravity(android.view.Gravity.CENTER);cell.setPadding(0,Ui.dp(c,7),0,Ui.dp(c,7));ImageView icon=new ImageView(c);int image=Ui.res(c,"mipmap",category.optString("icon"));if(image==0)image=Ui.res(c,"mipmap","ic_parent_other");icon.setImageResource(image);cell.addView(icon,new LinearLayout.LayoutParams(Ui.dp(c,26),Ui.dp(c,26)));cell.addView(Ui.label(c,category.optString("name"),11));if(category.optLong("id")==data.optLong("category_id"))cell.setBackground(Ui.rounded(0x2248ab93,8,c));return cell;}});grid.setOnItemClickListener((p,v,index,rowId)->{Json.put(data,"category_id",cats.get(index).optLong("id"));((BaseAdapter)grid.getAdapter()).notifyDataSetChanged();});container.addView(grid,new FrameLayout.LayoutParams(-1,Ui.dp(c,135)));
            };
            ReferenceTabs tabs=Ui.view(view,"tab_layout");tabs.configure(new String[]{"支出","收入","转账"},data.optString("kind").equals("income")?1:data.optString("kind").equals("transfer")?2:0,index->{String kind=new String[]{"expense","income","transfer"}[index];Json.put(data,"kind",kind);Json.put(data,"category_id",ReferenceApplication.store().defaultCategory(kind.equals("transfer")?"expense":kind));render.run();});
            Ui.click(view,"asset_layout",()->{
                List<JSONObject> accounts=new ArrayList<>();for(JSONObject a:ReferenceApplication.store().accounts())if(a.optString("currency").equals("CNY"))accounts.add(a);
                String[] names=new String[accounts.size()+1];names[0]="无账户";for(int i=0;i<accounts.size();i++)names[i+1]=accounts.get(i).optString("name");
                AlertDialog dialog=new AlertDialog.Builder(c,android.R.style.Theme_Material_Light_Dialog_Alert).setTitle("记账账户").setItems(names,(d,w)->{Json.put(data,"account_id",w==0?0:accounts.get(w-1).optLong("id"));render.run();}).create();dialog.setOnDismissListener(d->{if(accountDialog==dialog)accountDialog=null;});accountDialog=dialog;try{dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);dialog.show();}catch(RuntimeException error){accountDialog=null;ReferenceDiagnostics.failure("OVERLAY_UNAVAILABLE",error);Ui.toast(c,"账户窗口未能显示，请在应用内选择账户");}
            });
            Ui.click(view,"cost_layout",()->edit(c,id,preview));Ui.click(view,"account_book_layout",()->edit(c,id,preview));Ui.click(view,"date_layout",()->edit(c,id,preview));
            Ui.click(view,"background",CaptureOverlay::close);
            if(preview){Ui.text(view,"btn_save_text","关闭测试浮窗");Ui.text(view,"close_btn","关闭测试");}
            Ui.click(view,"close_btn",()->{if(!preview){ReferenceApplication.store().dismiss(id);YimuKernel.complete(id);}close();CaptureNotifications.update();});
            Ui.click(view,"btn_save",()->{
                if(preview){close();return;}
                try{EditText cost=Ui.view(view,"cost"),note=Ui.view(view,"remark"),discount=Ui.view(view,"discount_edit"),fee=Ui.view(view,"service_charge_edit");Json.put(data,"amount",Money.positive(cost.getText().toString(),"CNY"));if(note!=null)Json.put(data,"remark",note.getText().toString());if(discount!=null)Json.put(data,"discount",Money.signed(discount.getText().toString(),"CNY"));if(fee!=null)Json.put(data,"fee",Money.signed(fee.getText().toString(),"CNY"));ReferenceApplication.store().saveTransaction(data,0,id);complete(id);CaptureNotifications.update();Ui.toast(c,"已保存账单");}
                catch(Exception e){Ui.error(c,e);}
            });
            render.run();WindowManager.LayoutParams params=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT);params.softInputMode=WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;manager.addView(view,params);state("SHOWN");
        }catch(Exception e){ReferenceDiagnostics.failure("OVERLAY_UNAVAILABLE",e);close();state("SHOW_FAILED");}
    }
    private static void edit(Context context,String id,boolean preview){if(preview)Ui.toast(context,"这是测试浮窗，不会生成账单");else openEditor(context,id);}
    private static String accountName(long id){JSONObject a=ReferenceApplication.store().one("SELECT name FROM accounts WHERE id=?",""+id);return a==null?"无账户":a.optString("name");}
    private static void openEditor(Context context,String id){try{context.startActivity(new Intent(context,EditorActivity.class).putExtra("candidate",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));close();}catch(RuntimeException e){Ui.toast(context,"请从应用内待确认列表打开记录");}}
    public static void complete(String id){YimuKernel.complete(id);if(id!=null&&id.equals(candidate))close();}
    public static void close(){AlertDialog dialog=accountDialog;accountDialog=null;if(dialog!=null)try{dialog.dismiss();}catch(RuntimeException ignored){}if(shown!=null&&manager!=null)try{manager.removeView(shown);}catch(RuntimeException ignored){}if(shown!=null)state("CLOSED");shown=null;manager=null;candidate=null;}
}
