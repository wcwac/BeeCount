package com.wangc.bill.reference;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import com.wangc.bill.reference.ui.MainActivity;
import com.wangc.bill.reference.ui.Ui;

/** Real default repayment widget; original configuration/loan domains remain separate ports. */
public final class OriginalRepaymentWidget extends AppWidgetProvider {
    public static final String ACTION_CLICK="com.wangc.bill.CALENDAR_CLICK_BILL_ACTION";
    public static void refreshInstalled(ReferenceStore store){
        // Isolated device-test stores share applicationContext but never publish production rows.
        if(!ReferenceApplication.isProductionStore(store))return;Context context=store.context();AppWidgetManager manager=AppWidgetManager.getInstance(context);ComponentName provider=new ComponentName(context,OriginalRepaymentWidget.class);int[] ids=manager.getAppWidgetIds(provider);if(ids.length==0)return;
        context.sendBroadcast(new Intent(context,OriginalRepaymentWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setFlags(268435456).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids));
    }
    @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids){for(int id:ids){manager.notifyAppWidgetViewDataChanged(id,required(context,"id","repayment_list"));manager.updateAppWidget(id,views(context,id));}}
    public static RemoteViews views(Context context,int widgetId){
        RemoteViews views=new RemoteViews(context.getPackageName(),required(context,"layout","layout_app_widget_repayment"));
        views.setImageViewResource(required(context,"id","background"),required(context,"drawable","shape_bg_white_15"));views.setInt(required(context,"id","background"),"setImageAlpha",(int)(0.8f*255.0f));
        views.setViewVisibility(required(context,"id","open_vip_layout"),View.GONE);
        // Default source action opens TransparentActivity, whose protected initialization is
        // not restored. Keep this unported button hidden rather than inventing an action.
        views.setViewVisibility(required(context,"id","btn_add"),View.GONE);
        Intent adapter=new Intent(context,OriginalRepaymentWidgetService.class).setType(""+widgetId).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId);views.setRemoteAdapter(required(context,"id","repayment_list"),adapter);
        PendingIntent launch=PendingIntent.getActivity(context,widgetId,new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);views.setOnClickPendingIntent(required(context,"id","title"),launch);
        Intent template=new Intent(context,OriginalRepaymentWidget.class).setAction(ACTION_CLICK).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId);int flags=PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=31?PendingIntent.FLAG_MUTABLE:0);views.setPendingIntentTemplate(required(context,"id","repayment_list"),PendingIntent.getBroadcast(context,widgetId,template,flags));return views;
    }
    @Override public void onReceive(Context context,Intent intent){
        super.onReceive(context,intent);if(!ACTION_CLICK.equals(intent.getAction())||!"credit".equals(intent.getStringExtra("type")))return;
        OriginalCreditBill bill=Build.VERSION.SDK_INT>=33?intent.getParcelableExtra("CreditBill",OriginalCreditBill.class):legacyBill(intent);if(bill==null)return;
        context.startActivity(new Intent(context,OriginalCreditStatementActivity.class).putExtra("CreditBill",bill).putExtra("owner",ReferenceStore.LOCAL_OFFLINE_OWNER).putExtra("bookOwner",ReferenceStore.LOCAL_OFFLINE_OWNER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    @SuppressWarnings("deprecation") private static OriginalCreditBill legacyBill(Intent intent){return intent.getParcelableExtra("CreditBill");}
    static int required(Context context,String type,String name){int value=Ui.res(context,type,name);if(value==0)throw new IllegalStateException("Missing repayment resource: "+type+"/"+name);return value;}
}
