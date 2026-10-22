package com.wangc.bill.reference;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.wangc.bill.R;
import com.wangc.bill.reference.ui.MainActivity;
import com.wangc.bill.reference.ui.Ui;
import org.json.JSONObject;
public final class ReferenceWidget extends AppWidgetProvider {
    @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids){
        ReferenceStore store=ReferenceApplication.store();long bookId=ReferenceApplication.prefs().getLong("current_book",1);JSONObject book=store.book(bookId);if(book==null)book=store.books().get(0);long[] month=Ui.month(System.currentTimeMillis());JSONObject sums=store.totals(book.optLong("id"),month[0],month[1]);
        for(int id:ids){RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.reference_widget);views.setTextViewText(R.id.widget_title,book.optString("name")+" · 本月");views.setTextViewText(R.id.widget_amounts,"支出 "+Money.text(sums.optLong("pay"),book.optString("currency"))+"   收入 "+Money.text(sums.optLong("income"),book.optString("currency")));PendingIntent launch=PendingIntent.getActivity(context,0,new Intent(context,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);views.setOnClickPendingIntent(R.id.widget_root,launch);manager.updateAppWidget(id,views);}
    }
}
