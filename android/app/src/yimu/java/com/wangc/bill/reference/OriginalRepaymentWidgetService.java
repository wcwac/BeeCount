package com.wangc.bill.reference;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/** Source repayment RemoteViews collection with current-store credit rows and real fill-in intents. */
public final class OriginalRepaymentWidgetService extends RemoteViewsService {
    @Override public RemoteViewsFactory onGetViewFactory(Intent intent){return new Factory(ReferenceApplication.store(),intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID));}
    public static final class Factory implements RemoteViewsFactory {
        private final ReferenceStore store;private final Context context;private final int widgetId;
        private List<OriginalCreditBill> rows=new ArrayList<>();
        public Factory(ReferenceStore store,int widgetId){this.store=store;context=store.context();this.widgetId=widgetId;}
        @Override public void onCreate(){}
        @Override public void onDestroy(){rows=new ArrayList<>();}
        @Override public void onDataSetChanged(){rows=upcoming(store,System.currentTimeMillis());}
        @Override public int getCount(){return rows.isEmpty()?1:rows.size();}
        @Override public long getItemId(int position){return position;}
        @Override public RemoteViews getLoadingView(){return null;}
        @Override public int getViewTypeCount(){return 3;}
        @Override public boolean hasStableIds(){return true;}
        @Override public RemoteViews getViewAt(int position){
            if(position<0||position>=rows.size())return new RemoteViews(context.getPackageName(),OriginalRepaymentWidget.required(context,"layout","item_widget_no_tip_small"));
            OriginalCreditBill bill=rows.get(position);RemoteViews views=new RemoteViews(context.getPackageName(),OriginalRepaymentWidget.required(context,"layout","item_widget_repayment_list"));JSONObject asset=OriginalFinancialEffects.cachedAsset(store,ReferenceStore.LOCAL_OFFLINE_OWNER,bill.getAssetId());
            views.setTextViewText(OriginalRepaymentWidget.required(context,"id","asset_name"),asset==null?"":asset.optString("name"));views.setTextViewText(OriginalRepaymentWidget.required(context,"id","cost"),OriginalCreditFormatting.amount(bill.getRemindNumber()));
            int days=(int)((OriginalInstalmentSchedule.dayStart(OriginalInstalmentSchedule.parse(bill.getRepaymentDay(),"yyyy-MM-dd"))-OriginalInstalmentSchedule.dayStart(System.currentTimeMillis()))/86400000L);views.setTextViewText(OriginalRepaymentWidget.required(context,"id","time_info"),days+"天后还款"+OriginalCreditFormatting.amount(bill.getRemindNumber()));
            String icon=asset==null?"ic_repayment_bank":asset.optString("asset_icon","ic_repayment_bank");int image=com.wangc.bill.reference.ui.Ui.res(context,"mipmap",icon);if(image==0)image=OriginalRepaymentWidget.required(context,"mipmap","ic_repayment_bank");views.setImageViewResource(OriginalRepaymentWidget.required(context,"id","icon"),image);
            views.setOnClickFillInIntent(OriginalRepaymentWidget.required(context,"id","total_layout"),new Intent().putExtra("type","credit").putExtra("CreditBill",bill));return views;
        }
        public int widgetId(){return widgetId;}
    }
    /** Source scans fixed24-hour steps through one Calendar month, then its original comparator. */
    public static List<OriginalCreditBill> upcoming(ReferenceStore store,long now){
        Map<String,List<OriginalCreditBill>> schedule=OriginalFinancialEffects.repaymentSchedule(store,ReferenceStore.LOCAL_OFFLINE_OWNER,ReferenceStore.LOCAL_OFFLINE_OWNER);List<OriginalCreditBill> result=new ArrayList<>();Calendar end=Calendar.getInstance();end.clear();end.setTimeInMillis(now);end.add(Calendar.MONTH,1);
        synchronized(store){for(long cursor=now;cursor<end.getTimeInMillis();cursor+=86400000L){List<OriginalCreditBill> rows=schedule.get(OriginalInstalmentSchedule.format(cursor,"yyyy-MM-dd"));if(rows==null)continue;for(OriginalCreditBill bill:rows){JSONObject asset=store.assetRepository().p(bill.getAssetId(),ReferenceStore.LOCAL_OFFLINE_OWNER);if(asset!=null&&asset.optInt("hide")==0&&bill.getRemindNumber()>0.0d)result.add(bill);}}}
        // DEX45988 overwrites parameter2 with its instanceof result and compares only
        // zero minus parameter1's date. This odd source comparator is intentionally retained.
        result.sort((first,ignored)->{long difference=0L-OriginalInstalmentSchedule.parse(first.getRepaymentDay(),"yyyy-MM-dd");return difference>0?-1:difference<0?1:0;});return result;
    }
}
