package com.wangc.bill.reference;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.UiAutomation;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import com.wangc.bill.reference.ui.MainActivity;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Actual system host/service/fill-in-PendingIntent path, confined to the temporary CI emulator. */
@RunWith(AndroidJUnit4.class)
public final class OriginalRepaymentWidgetDeviceTest {
    private static final int HOST=0x59524d;
    private void await(BooleanSupplier condition){long until=SystemClock.uptimeMillis()+10000;while(SystemClock.uptimeMillis()<until){if(condition.getAsBoolean())return;SystemClock.sleep(30);}assertTrue("Actual AppWidgetHost path timed out",condition.getAsBoolean());}
    private static View find(View root,String text){if(root instanceof TextView t&&t.getText().toString().contains(text))return root;if(root instanceof ViewGroup g)for(int i=0;i<g.getChildCount();i++){View value=find(g.getChildAt(i),text);if(value!=null)return value;}return null;}
    private static void tap(UiAutomation automation,Point point){long now=SystemClock.uptimeMillis();MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,point.x,point.y,0),up=MotionEvent.obtain(now,now+60,MotionEvent.ACTION_UP,point.x,point.y,0);try{down.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);up.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);assertTrue(automation.injectInputEvent(down,true));assertTrue(automation.injectInputEvent(up,true));}finally{down.recycle();up.recycle();}}
    @Test public void actualSystemHostLoadsTheCollectionAndItsVisibleRowOpensTheStatement() {
        assertEquals("true",InstrumentationRegistry.getArguments().getString("reference_cloud_only"));assertEquals(36,Build.VERSION.SDK_INT);assertTrue(Build.HARDWARE.equals("ranchu")||Build.HARDWARE.equals("goldfish"));assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        Context context=ReferenceApplication.app();ReferenceStore store=ReferenceApplication.store();JSONObject backup=store.backup();long oldBook=ReferenceApplication.prefs().getLong("current_book",1);
        UiAutomation automation=InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);AccessibilityServiceInfo observer=automation.getServiceInfo();String[] oldPackages=observer.packageNames==null?null:observer.packageNames.clone();observer.packageNames=new String[]{context.getPackageName()};automation.setServiceInfo(observer);
        AppWidgetHost host=new AppWidgetHost(context,HOST);int id=AppWidgetManager.INVALID_APPWIDGET_ID;ActivityScenario<MainActivity> scenario=null;boolean adopted=false;AtomicReference<AppWidgetHostView> shown=new AtomicReference<>();
        try{
            long book=0;for(JSONObject candidate:store.books())if(!candidate.isNull("semantic_id")&&candidate.optInt("owner_semantic_id")==0&&"CNY".equals(candidate.optString("currency"))){book=candidate.optLong("id");break;}assertTrue("No original owner0 book",book>0);ReferenceApplication.selectCurrentBook(book);
            long semantic=Long.MAX_VALUE-99;assertNull(store.assetRepository().p(semantic,0));AssetRepository.Definition definition=new AssetRepository.Definition("云端合成还款账户",2,null);definition.assetNumber=-5;definition.inAccountDate="出账日1";store.assetRepository().insertRecovered(0,semantic,999,0,definition);
            OriginalBillRecord bill=new OriginalBillRecord();bill.userId=0;bill.fromUserId=0;bill.billId=Integer.MAX_VALUE-99;bill.assetId=semantic;bill.bookId=store.book(book).optLong("semantic_id");bill.parentCategoryId=99;bill.childCategoryId=-1;bill.cost=5;bill.time=System.currentTimeMillis();bill.remark="云端合成还款明细";
            ContentValues bridge=new ContentValues();bridge.put("book_id",book);bridge.put("kind","expense");bridge.put("amount",500);bridge.put("currency","CNY");bridge.put("category_id",store.categoryRepository().parent(99).optLong("id"));bridge.put("time",bill.time);bridge.put("created",bill.time);new OriginalBillRepository(store).saveOriginalRaw(bill,bridge);
            long before=OriginalFinancialEffects.creditRevision(store,0,0,semantic);new OriginalFinancialEffects(store,0,0).creditRefresh(semantic);await(()->OriginalFinancialEffects.creditStatus(store,0,0,semantic)==OriginalFinancialEffects.CreditStatus.READY&&OriginalFinancialEffects.creditRevision(store,0,0,semantic)>before);InstrumentationRegistry.getInstrumentation().waitForIdleSync();assertEquals(1,OriginalRepaymentWidgetService.upcoming(store,System.currentTimeMillis()).stream().filter(row->row.getAssetId()==semantic).count());
            automation.adoptShellPermissionIdentity("android.permission.BIND_APPWIDGET");adopted=true;id=host.allocateAppWidgetId();AppWidgetManager manager=AppWidgetManager.getInstance(context);ComponentName component=new ComponentName(context,OriginalRepaymentWidget.class);assertTrue("Temporary BIND_APPWIDGET permission did not allow binding",manager.bindAppWidgetIdIfAllowed(id,component));assertNotNull(manager.getAppWidgetInfo(id));host.startListening();final int widgetId=id;
            scenario=ActivityScenario.launch(new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));scenario.onActivity(activity->{AppWidgetHostView view=host.createView(activity,widgetId,manager.getAppWidgetInfo(widgetId));view.setAppWidget(widgetId,manager.getAppWidgetInfo(widgetId));FrameLayout root=new FrameLayout(activity);root.addView(view,new FrameLayout.LayoutParams(-1,activity.getResources().getDisplayMetrics().heightPixels/2));activity.setContentView(root);shown.set(view);});
            new OriginalRepaymentWidget().onUpdate(context,manager,new int[]{id});
            AtomicReference<Point> rowPoint=new AtomicReference<>();await(()->{InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{AppWidgetHostView root=shown.get();View name=root==null?null:find(root,"云端合成还款账户"),amount=name==null||!(name.getParent() instanceof ViewGroup)?null:find((View)name.getParent(),"天后还款5.00");Rect bounds=new Rect();if(name!=null&&name.isShown()&&amount!=null&&amount.isShown()&&name.getGlobalVisibleRect(bounds)&&!bounds.isEmpty())rowPoint.set(new Point(bounds.centerX(),bounds.centerY()));});return rowPoint.get()!=null;});tap(automation,rowPoint.get());
            AtomicReference<OriginalCreditStatementActivity> detail=new AtomicReference<>();await(()->{InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{for(Activity activity:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(activity instanceof OriginalCreditStatementActivity statement&&find(statement.getWindow().getDecorView(),"支出：-5.00")!=null&&find(statement.getWindow().getDecorView(),"云端合成还款明细")!=null)detail.set(statement);});return detail.get()!=null;});InstrumentationRegistry.getInstrumentation().runOnMainSync(()->detail.get().finish());
        }finally{
            try{InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{for(Stage stage:new Stage[]{Stage.RESUMED,Stage.PAUSED,Stage.STOPPED})for(Activity activity:new java.util.ArrayList<>(ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage)))if(activity instanceof OriginalCreditStatementActivity)activity.finish();});}finally{try{if(scenario!=null)scenario.close();}finally{try{host.stopListening();}finally{try{if(id!=AppWidgetManager.INVALID_APPWIDGET_ID)host.deleteAppWidgetId(id);}finally{try{host.deleteHost();}finally{try{if(adopted)automation.dropShellPermissionIdentity();}finally{try{store.restore(backup);}finally{try{ReferenceApplication.selectCurrentBook(oldBook);OriginalFinancialEffects.bookSelectionChanged(store);}finally{AccessibilityServiceInfo restored=automation.getServiceInfo();restored.packageNames=oldPackages;automation.setServiceInfo(restored);}}}}}}}}
        }
    }
}
