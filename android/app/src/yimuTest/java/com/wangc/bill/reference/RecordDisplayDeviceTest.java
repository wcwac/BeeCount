package com.wangc.bill.reference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.wangc.bill.reference.ui.MainActivity;
import com.wangc.bill.reference.ui.ManagerActivity;
import com.wangc.bill.reference.ui.RecordAdapter;
import com.wangc.bill.reference.ui.Ui;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Exercises the real settings controls and attached RecyclerView rows using synthetic bills. */
@RunWith(AndroidJUnit4.class)
public final class RecordDisplayDeviceTest {
    private final Map<String,Boolean> previous=new LinkedHashMap<>();
    private SharedPreferences preferences;
    private Locale previousLocale;
    private int listId;

    @Before public void saveDisplayChoices() {
        preferences=ReferenceApplication.prefs();
        previousLocale=Locale.getDefault();Locale.setDefault(Locale.US);
        for(String key:new String[]{OriginalSettings.REMARK_MAIN,OriginalSettings.SHOW_TIME,OriginalSettings.SHOW_OLD_COST,"show_remark"})
            previous.put(key,preferences.contains(key)?preferences.getBoolean(key,false):null);
        assertTrue(preferences.edit().remove(OriginalSettings.REMARK_MAIN).remove(OriginalSettings.SHOW_TIME).remove(OriginalSettings.SHOW_OLD_COST).remove("show_remark").commit());
        listId=View.generateViewId();
    }
    @After public void restoreDisplayChoices() {
        SharedPreferences.Editor edit=preferences.edit();
        for(Map.Entry<String,Boolean> entry:previous.entrySet()) {
            if(entry.getValue()==null)edit.remove(entry.getKey());else edit.putBoolean(entry.getKey(),entry.getValue());
        }
        try{assertTrue(edit.commit());}finally{Locale.setDefault(previousLocale);}
    }

    @Test public void originalDefaultsKeepCategoryTitlesAndHideTransactionTime() {
        try(ActivityScenario<ManagerActivity> settings=settings()) {
            settings.onActivity(activity->assertSwitches(activity,false,false));
            settings.recreate();
            settings.onActivity(activity->assertSwitches(activity,false,false));
        }
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            showRows(screen,new ArrayList<>(Arrays.asList(bill("餐饮","早餐","expense"),bill("交通","","expense"))));
            screen.onActivity(activity->{
                assertRow(activity,0,"餐饮","早餐",false);
                assertRow(activity,1,"交通",null,false);
                assertEquals(View.GONE,Ui.view(row(activity,1),"second_layout").getVisibility());
            });
        }
    }

    @Test public void visibleSettingsPersistAndSwapRemarkWithCategoryIncludingEmptyRemark() {
        try(ActivityScenario<ManagerActivity> settings=settings()) {
            settings.onActivity(activity->{
                CompoundButton remark=activity.findViewById(com.wangc.bill.R.id.switch_remark_main);
                CompoundButton time=activity.findViewById(com.wangc.bill.R.id.switch_show_time);
                assertTrue(remark.isShown());assertTrue(time.isShown());
                remark.performClick();time.performClick();
                assertSwitches(activity,true,true);
            });
            settings.recreate();
            settings.onActivity(activity->assertSwitches(activity,true,true));
        }
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            showRows(screen,new ArrayList<>(Arrays.asList(bill("餐饮","早餐","expense"),bill("交通","","expense"),
                bill("其他","未细分开销","expense"),bill("忽略分类","账户调整","transfer"))));
            screen.onActivity(activity->{
                assertRow(activity,0,"早餐","餐饮",true);
                assertRow(activity,1,"交通",null,true);
                assertRow(activity,2,"未细分开销",null,true);
                assertRow(activity,3,"账户调整","转账",true);
            });
        }
    }

    @Test public void switchingRemarkMainOffRestoresTheRemarkDescriptionAndPersistsTimeOff() {
        assertTrue(preferences.edit().putBoolean(OriginalSettings.REMARK_MAIN,true).putBoolean(OriginalSettings.SHOW_TIME,true).commit());
        try(ActivityScenario<ManagerActivity> settings=settings()) {
            settings.onActivity(activity->{
                assertSwitches(activity,true,true);
                ((CompoundButton)activity.findViewById(com.wangc.bill.R.id.switch_remark_main)).performClick();
                ((CompoundButton)activity.findViewById(com.wangc.bill.R.id.switch_show_time)).performClick();
            });
            settings.recreate();settings.onActivity(activity->assertSwitches(activity,false,false));
        }
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            showRows(screen,new ArrayList<>(Arrays.asList(bill("餐饮","午餐","expense"))));
            screen.onActivity(activity->assertRow(activity,0,"餐饮","午餐",false));
        }
    }

    @Test public void explicitChoicesFromEarlierBuildsRemainUntilTheUserChangesTheSetting() {
        assertTrue(preferences.edit().putBoolean("show_remark",true).putBoolean(OriginalSettings.SHOW_TIME,true).commit());
        try(ActivityScenario<ManagerActivity> settings=settings()) {
            settings.onActivity(activity->assertSwitches(activity,true,true));
            settings.recreate();settings.onActivity(activity->{
                assertSwitches(activity,true,true);
                ((CompoundButton)activity.findViewById(com.wangc.bill.R.id.switch_remark_main)).performClick();
            });
        }
        // The newer explicit choice wins even though the old key still exists after an upgrade.
        assertTrue(preferences.getBoolean("show_remark",false));
        try(ActivityScenario<ManagerActivity> settings=settings()) {settings.onActivity(activity->assertSwitches(activity,false,true));}
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            showRows(screen,new ArrayList<>(Arrays.asList(bill("餐饮","晚餐","expense"))));
            screen.onActivity(activity->assertRow(activity,0,"餐饮","晚餐",true));
        }
    }

    @Test public void rebindingRowsClearsStaleTextAndRestoresTheSecondLineWhenNeeded() {
        JSONObject value=bill("餐饮","早餐","expense");List<JSONObject> values=new ArrayList<>(Arrays.asList(value));
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            showRows(screen,values);
            screen.onActivity(activity->{assertRow(activity,0,"餐饮","早餐",false);Json.put(value,"remark","");list(activity).getAdapter().notifyItemChanged(0);});
            idle();
            screen.onActivity(activity->{assertRow(activity,0,"餐饮",null,false);assertEquals("",((TextView)Ui.view(row(activity,0),"remark")).getText().toString());assertEquals(View.GONE,Ui.view(row(activity,0),"second_layout").getVisibility());Json.put(value,"remark","晚餐");list(activity).getAdapter().notifyItemChanged(0);});
            idle();
            screen.onActivity(activity->{assertRow(activity,0,"餐饮","晚餐",false);assertEquals(View.VISIBLE,Ui.view(row(activity,0),"second_layout").getVisibility());});
        }
    }

    @Test public void originalCostDefaultsToVisibleForDiscountedBillsAndExcludesTransfers() {
        try(ActivityScenario<ManagerActivity> settings=settings()) {
            settings.onActivity(activity->assertOldCostSwitch(activity,true));
            settings.recreate();settings.onActivity(activity->assertOldCostSwitch(activity,true));
        }
        JSONObject expense=bill("餐饮","优惠午餐","expense"),income=bill("其他收入","优惠收入","income"),transfer=bill("转账","转账手续费","transfer");
        Json.put(expense,"discount",250);Json.put(expense,"fee",99);Json.put(income,"discount",125);Json.put(transfer,"discount",250);
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            showRows(screen,new ArrayList<>(Arrays.asList(expense,income,transfer)));
            screen.onActivity(activity->{
                assertOldCost(activity,0,"14.80");assertOldCost(activity,1,"13.55");assertOldCost(activity,2,null);
                assertEquals("The old amount must not change the actual charge","-12.30",((TextView)Ui.view(row(activity,0),"cost")).getText().toString());
            });
        }
    }

    @Test public void originalCostSwitchChangesAttachedRowsAndPersistsAcrossRecreation() {
        try(ActivityScenario<ManagerActivity> settings=settings()) {
            settings.onActivity(activity->{assertOldCostSwitch(activity,true);((CompoundButton)activity.findViewById(com.wangc.bill.R.id.switch_show_old_cost)).performClick();assertOldCostSwitch(activity,false);});
            settings.recreate();settings.onActivity(activity->assertOldCostSwitch(activity,false));
        }
        JSONObject discounted=bill("购物","优惠商品","expense");Json.put(discounted,"discount",250);
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            showRows(screen,new ArrayList<>(Arrays.asList(discounted)));screen.onActivity(activity->assertOldCost(activity,0,null));
        }
        try(ActivityScenario<ManagerActivity> settings=settings()) {
            settings.onActivity(activity->{assertOldCostSwitch(activity,false);((CompoundButton)activity.findViewById(com.wangc.bill.R.id.switch_show_old_cost)).performClick();assertOldCostSwitch(activity,true);});
            settings.recreate();settings.onActivity(activity->assertOldCostSwitch(activity,true));
        }
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            showRows(screen,new ArrayList<>(Arrays.asList(discounted)));screen.onActivity(activity->assertOldCost(activity,0,"14.80"));
        }
    }

    @Test public void originalCostUsesTheNonzeroDiscountGateAndClearsRecycledPriceDecoration() {
        // Original Bill.discountNumber permits a signed value. This raw display fixture
        // exercises that branch without weakening the store's nonnegative input validation.
        JSONObject value=bill("餐饮","原版有符号优惠","expense");Json.put(value,"discount",-230);
        try(ActivityScenario<MainActivity> screen=ActivityScenario.launch(MainActivity.class)) {
            showRows(screen,new ArrayList<>(Arrays.asList(value)));
            screen.onActivity(activity->{assertOldCost(activity,0,"10.00");Json.put(value,"discount",0);list(activity).getAdapter().notifyItemChanged(0);});
            idle();
            screen.onActivity(activity->{assertOldCost(activity,0,null);Json.put(value,"discount",150);list(activity).getAdapter().notifyItemChanged(0);});
            idle();
            screen.onActivity(activity->{assertOldCost(activity,0,"13.80");Json.put(value,"discount",250);Json.put(value,"financial_type","lend");list(activity).getAdapter().notifyItemChanged(0);});
            idle();screen.onActivity(activity->assertOldCost(activity,0,null));
        }
    }

    private ActivityScenario<ManagerActivity> settings() {
        return ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(),ManagerActivity.class).putExtra("page","settings"));
    }
    private static void assertSwitches(ManagerActivity activity,boolean remark,boolean time) {
        CompoundButton main=activity.findViewById(com.wangc.bill.R.id.switch_remark_main),clock=activity.findViewById(com.wangc.bill.R.id.switch_show_time);
        assertNotNull(main);assertNotNull(clock);assertEquals(remark,main.isChecked());assertEquals(time,clock.isChecked());
    }
    private void assertOldCostSwitch(ManagerActivity activity,boolean checked) {
        CompoundButton control=activity.findViewById(com.wangc.bill.R.id.switch_show_old_cost);assertNotNull(control);assertTrue(control.isEnabled());assertTrue(control.isShown());assertEquals(checked,control.isChecked());assertEquals(checked,OriginalSettings.showOldCost(preferences));
    }
    private JSONObject bill(String category,String remark,String kind) {
        Calendar calendar=Calendar.getInstance();calendar.set(2026,Calendar.OCTOBER,24,8,9,0);calendar.set(Calendar.MILLISECOND,0);
        return Json.object("id",-10000,"kind",kind,"currency","CNY","amount",1230,"time",calendar.getTimeInMillis(),
            "category",category,"category_icon","ic_parent_other","remark",remark,"account","","asset_text","","origin","");
    }
    private void showRows(ActivityScenario<MainActivity> screen,List<JSONObject> values) {
        screen.onActivity(activity->{RecyclerView list=new RecyclerView(activity);list.setId(listId);list.setItemAnimator(null);
            list.setLayoutManager(new LinearLayoutManager(activity));list.setAdapter(new RecordAdapter(values,null,null));
            activity.setContentView(list,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));});
        idle();
    }
    private RecyclerView list(MainActivity activity) {return activity.findViewById(listId);}
    private View row(MainActivity activity,int position) {
        RecyclerView.ViewHolder holder=list(activity).findViewHolderForAdapterPosition(position);
        assertNotNull("Attached record at "+position,holder);return holder.itemView;
    }
    private void assertRow(MainActivity activity,int position,String title,String description,boolean time) {
        View row=row(activity,position);TextView main=Ui.view(row,"category"),detail=Ui.view(row,"remark"),clock=Ui.view(row,"time");
        assertEquals(title,main.getText().toString());assertTrue(main.isShown());
        assertEquals(description==null?View.GONE:View.VISIBLE,detail.getVisibility());
        if(description!=null){assertEquals(description,detail.getText().toString());assertTrue(detail.isShown());}
        assertEquals(time?View.VISIBLE:View.GONE,clock.getVisibility());
        if(time){assertEquals("08:09",clock.getText().toString());assertTrue(clock.isShown());}
    }
    private void assertOldCost(MainActivity activity,int position,String expected) {
        TextView old=Ui.view(row(activity,position),"old_cost");assertNotNull(old);assertEquals(expected==null?View.GONE:View.VISIBLE,old.getVisibility());
        assertEquals(expected==null?"":expected,old.getText().toString());assertEquals(expected!=null,(old.getPaintFlags()&Paint.STRIKE_THRU_TEXT_FLAG)!=0);
        if(expected!=null)assertTrue(old.isShown());
    }
    private static void idle() {InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
}
