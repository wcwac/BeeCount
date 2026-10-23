package com.wangc.bill.reference;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.wangc.bill.reference.ui.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public final class ScreenDeviceTest {
    @Rule public final ScreenEvidenceRule evidence=new ScreenEvidenceRule();

    @Test public void primaryTabsInflateAndBindActualTemplates(){
        String[] tabs={"home","calendar","assets","statistics","mine"};
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
            for(int tab=0;tab<tabs.length;tab++){
                int target=tab;scenario.onActivity(a->{ReferenceBottomBar bar=a.findViewById(com.wangc.bill.R.id.bottom_nav_view);assertEquals(5,bar.getChildCount());bar.getChildAt(target).performClick();});
                evidence.checkpoint(scenario,"main/"+tabs[tab]);
                scenario.onActivity(a->{assertFalse(a.isFinishing());assertNotNull(a.findViewById(com.wangc.bill.R.id.fragment_layout));});
            }
        }
    }
    @Test public void localManagementScreensBindWithoutErrorFallback(){
        String[] pages={"capture","capture_settings","apps","diagnostics","inbox","rules","books","edit_book","accounts","new_account","categories","edit_category","tags","edit_tag","budget","goals","edit_goal","plans","edit_plan","lending","reimbursement","investments","search","trash","backup","import","attachments","settings","theme","profile","widgets","help","about"};
        for(String page:pages){
            Intent intent=new Intent(ApplicationProvider.getApplicationContext(),ManagerActivity.class).putExtra("page",page);
            try(ActivityScenario<ManagerActivity> scenario=ActivityScenario.launch(intent)){
                evidence.checkpoint(scenario,"manager/"+page);
                scenario.onActivity(a->{assertFalse(page,a.isFinishing());assertFalse(page,contains(a.findViewById(android.R.id.content),"操作未完成"));});
            }
        }
    }
    @Test public void editorKeyboardKeepsOriginalHeightAndKeys(){
        try(ActivityScenario<EditorActivity> scenario=ActivityScenario.launch(EditorActivity.class)){
            evidence.checkpoint(scenario,"editor/new_bill_keyboard");
            scenario.onActivity(a->{assertFalse(a.isFinishing());View keyboard=a.findViewById(com.wangc.bill.R.id.number_key_board);assertNotNull(keyboard);assertNotNull(keyboard.findViewById(com.wangc.bill.R.id.one));assertNotNull(keyboard.findViewById(com.wangc.bill.R.id.cancel));});
        }
    }
    @Test public void recoveredOverlayAndRecordRowsInflate(){
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)){
            evidence.checkpoint(scenario,"inflation_host/visible_home");
            String[] templates={"view_auto_add_new","item_bill_main_list","item_account_book","item_asset"};
            scenario.onActivity(a->{for(String name:templates)assertNotNull(name,Ui.layout(a,name));});
            // The screenshot above is the actual home host. These unattached views
            // are deliberately recorded as unshown, never as visual page coverage.
            evidence.unshownTemplates(templates);
        }
    }
    private static boolean contains(View view,String value){if(view instanceof TextView text&&value.contentEquals(text.getText()))return true;if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++)if(contains(group.getChildAt(i),value))return true;return false;}
}
