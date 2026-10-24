package com.wangc.bill.reference.ui;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Layout;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.wangc.bill.reference.ReferenceApplication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Attached source dialogs, actual touch/key events and a real Material entry guide; synthetic UI only. */
@RunWith(AndroidJUnit4.class)
public final class OriginalPermissionGuidanceDeviceTest {
    private ActivityScenario<ManagerActivity> scenario;
    private UiAutomation automation;
    private String[] priorPackages;
    private int priorFlags;
    private Map<String,?> settingsBefore;
    private final AtomicReference<DialogFragment> fragment=new AtomicReference<>();
    private final AtomicReference<Dialog> dialog=new AtomicReference<>();

    @Before public void open(){
        automation=InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        AccessibilityServiceInfo observer=automation.getServiceInfo();priorPackages=observer.packageNames==null?null:observer.packageNames.clone();priorFlags=observer.flags;
        observer.packageNames=new String[]{ReferenceApplication.app().getPackageName()};
        observer.flags|=AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS|AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        automation.setServiceInfo(observer);
        scenario=ActivityScenario.launch(new Intent(ReferenceApplication.app(),ManagerActivity.class).putExtra("page","settings"));
        settingsBefore=ReferenceApplication.prefs().getAll();
    }
    @After public void close(){
        try{if(scenario!=null)scenario.close();}
        finally{try{restorePreferences();}
            finally{if(automation!=null){AccessibilityServiceInfo observer=automation.getServiceInfo();observer.packageNames=priorPackages;observer.flags=priorFlags;automation.setServiceInfo(observer);}}}
    }
    private void restorePreferences(){
        if(settingsBefore==null||settingsBefore.equals(ReferenceApplication.prefs().getAll()))return;
        SharedPreferences.Editor edit=ReferenceApplication.prefs().edit().clear();
        for(Map.Entry<String,?> entry:settingsBefore.entrySet()){
            String key=entry.getKey();Object value=entry.getValue();
            if(value instanceof String text)edit.putString(key,text);else if(value instanceof Boolean flag)edit.putBoolean(key,flag);
            else if(value instanceof Integer number)edit.putInt(key,number);else if(value instanceof Long number)edit.putLong(key,number);
            else if(value instanceof Float number)edit.putFloat(key,number);else if(value instanceof Set<?> values){
                Set<String> strings=new HashSet<>();for(Object item:values){assertTrue(item instanceof String);strings.add((String)item);}edit.putStringSet(key,strings);
            }else throw new AssertionError("Unexpected SharedPreferences value type");
        }
        assertTrue("Restore synthetic UI settings after a failed assertion",edit.commit());
    }
    private void await(BooleanSupplier condition){long until=SystemClock.uptimeMillis()+5000;while(SystemClock.uptimeMillis()<until){if(condition.getAsBoolean())return;SystemClock.sleep(25);}assertTrue("Actual permission guidance timed out",condition.getAsBoolean());}
    private void show(DialogFragment value){
        fragment.set(value);dialog.set(null);scenario.onActivity(a->value.show(a.getSupportFragmentManager(),"original-guidance"));
        await(()->{scenario.onActivity(a->{if(value.isResumed()&&value.getDialog()!=null&&value.getDialog().isShowing()
                &&value.getDialog().getWindow()!=null&&value.getDialog().getWindow().getDecorView().hasWindowFocus()
                &&value.getView()!=null&&value.getView().isAttachedToWindow()&&value.getView().isLaidOut())dialog.set(value.getDialog());});return dialog.get()!=null;});
    }
    private void hidden(){await(()->{AtomicBoolean showing=new AtomicBoolean();scenario.onActivity(a->showing.set(dialog.get().isShowing()));return !showing.get();});InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
    private void stillShowing(){scenario.onActivity(a->assertTrue(dialog.get().isShowing()));}
    private void unchanged(){assertEquals("A guidance action must not mutate local feature settings",settingsBefore,ReferenceApplication.prefs().getAll());}
    private void size(int margin,boolean top){scenario.onActivity(a->{
        WindowManager manager=(WindowManager)a.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);assertNotNull(manager);
        int width;if(Build.VERSION.SDK_INT>=30)width=manager.getCurrentWindowMetrics().getBounds().width();else{Point p=new Point();manager.getDefaultDisplay().getSize(p);width=p.x;}
        WindowManager.LayoutParams attrs=dialog.get().getWindow().getAttributes();
        assertEquals(width-Math.round(margin*a.getResources().getDisplayMetrics().density),attrs.width);
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT,attrs.height);if(top)assertEquals(Gravity.TOP,attrs.gravity);
    });}
    private void tap(String name){AtomicReference<Point> target=new AtomicReference<>();scenario.onActivity(a->{
        View view=Ui.view(fragment.get().requireView(),name);assertNotNull(view);assertTrue(view.isShown());
        Rect bounds=new Rect();assertTrue(view.getGlobalVisibleRect(bounds));assertFalse(bounds.isEmpty());
        int[] location=new int[2];view.getLocationOnScreen(location);target.set(new Point(location[0]+view.getWidth()/2,location[1]+view.getHeight()/2));
    });touch(target.get());}
    private void touch(Point point){long at=SystemClock.uptimeMillis();MotionEvent down=MotionEvent.obtain(at,at,MotionEvent.ACTION_DOWN,point.x,point.y,0),up=MotionEvent.obtain(at,at+60,MotionEvent.ACTION_UP,point.x,point.y,0);try{down.setSource(InputDevice.SOURCE_TOUCHSCREEN);up.setSource(InputDevice.SOURCE_TOUCHSCREEN);assertTrue(automation.injectInputEvent(down,true));assertTrue(automation.injectInputEvent(up,true));}finally{down.recycle();up.recycle();}InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
    private void back(){InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
    private void outside(){AtomicReference<Point> target=new AtomicReference<>();scenario.onActivity(a->{
        View decor=dialog.get().getWindow().getDecorView();int[] position=new int[2];decor.getLocationOnScreen(position);assertTrue(position[0]>0);
        target.set(new Point(Math.max(1,position[0]/2),position[1]+decor.getHeight()/2));
    });touch(target.get());}
    private void span(String name,int start,int end,String expected){
        AtomicReference<Point> target=new AtomicReference<>();scenario.onActivity(a->{
            TextView view=Ui.view(fragment.get().requireView(),name);assertTrue(view.getMovementMethod() instanceof LinkMovementMethod);
            assertTrue(view.getText() instanceof Spanned);Spanned text=(Spanned)view.getText();
            ClickableSpan[] clicks=text.getSpans(start,end,ClickableSpan.class);assertEquals(1,clicks.length);
            assertEquals(start,text.getSpanStart(clicks[0]));assertEquals(end,text.getSpanEnd(clicks[0]));assertEquals(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,text.getSpanFlags(clicks[0]));
            assertEquals(expected,text.subSequence(start,end).toString());
            ForegroundColorSpan[] colors=text.getSpans(start,end,ForegroundColorSpan.class);assertEquals(1,colors.length);
            assertEquals(a.getColor(Ui.res(a,"color","colorPrimary")),colors[0].getForegroundColor());
            Layout layout=view.getLayout();assertNotNull(layout);int offset=start+(end-start)/2,line=layout.getLineForOffset(offset);
            assertEquals(line,layout.getLineForOffset(offset+1));float first=layout.getPrimaryHorizontal(offset),second=layout.getPrimaryHorizontal(offset+1);
            int[] position=new int[2];view.getLocationOnScreen(position);
            int x=position[0]+view.getTotalPaddingLeft()-view.getScrollX()+Math.round((first+second)/2.0f);
            int y=position[1]+view.getTotalPaddingTop()-view.getScrollY()+(layout.getLineTop(line)+layout.getLineBottom(line))/2;
            Rect visible=new Rect();assertTrue(view.getGlobalVisibleRect(visible));assertTrue(visible.contains(x,y));target.set(new Point(x,y));
        });touch(target.get());
    }
    private String resource(String name){return ReferenceApplication.app().getResources().getResourceName(Ui.id(ReferenceApplication.app(),name));}
    private Rect activeBounds(String name){
        AccessibilityNodeInfo root=automation.getRootInActiveWindow();if(root==null)return null;
        try{List<AccessibilityNodeInfo> found=root.findAccessibilityNodeInfosByViewId(resource(name));try{
            for(AccessibilityNodeInfo value:found)if(value.isVisibleToUser()){Rect bounds=new Rect();value.getBoundsInScreen(bounds);if(!bounds.isEmpty())return bounds;}
            return null;
        }finally{for(AccessibilityNodeInfo value:found)value.recycle();}}finally{root.recycle();}
    }
    private boolean activeText(String text){
        AccessibilityNodeInfo root=automation.getRootInActiveWindow();if(root==null)return false;
        try{List<AccessibilityNodeInfo> found=root.findAccessibilityNodeInfosByText(text);try{
            for(AccessibilityNodeInfo value:found)if(value.isVisibleToUser()&&text.contentEquals(value.getText()==null?"":value.getText())){
                Rect bounds=new Rect();value.getBoundsInScreen(bounds);if(!bounds.isEmpty())return true;}
            return false;
        }finally{for(AccessibilityNodeInfo value:found)value.recycle();}}finally{root.recycle();}
    }
    private void swipeGuide(){
        Rect bounds=activeBounds("tip_list");assertNotNull(bounds);assertTrue(bounds.height()>100);
        int x=bounds.centerX(),start=bounds.top+bounds.height()*3/4,end=bounds.top+bounds.height()/4;long at=SystemClock.uptimeMillis();
        for(int i=0;i<=7;i++){
            int action=i==0?MotionEvent.ACTION_DOWN:i==7?MotionEvent.ACTION_UP:MotionEvent.ACTION_MOVE;
            int y=start+(end-start)*Math.min(i,6)/6;MotionEvent event=MotionEvent.obtain(at,SystemClock.uptimeMillis(),action,x,y,0);
            try{event.setSource(InputDevice.SOURCE_TOUCHSCREEN);assertTrue(automation.injectInputEvent(event,true));}finally{event.recycle();}
            if(i<7)SystemClock.sleep(35);
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test public void autoTipUsesItsOriginalContentImageWidthAndCancelWithoutChangingSettings(){
        AtomicInteger confirms=new AtomicInteger();show(OriginalAutoTipDialog.create().onConfirm(confirms::incrementAndGet));size(40,false);
        scenario.onActivity(a->{View root=fragment.get().requireView();assertEquals("开启无障碍",((TextView)Ui.view(root,"title")).getText().toString());
            assertEquals("去授权",((TextView)Ui.view(root,"confirm")).getText().toString());
            String content=((TextView)Ui.view(root,"content")).getText().toString();
            assertEquals("为了使用自动记账，需要您手动授予「无障碍权限」\n您可在“已下载的应用”、“已安装的服务”等入口找到“自动记账”设置项，不同机型入口点此查看",content);
            assertEquals(69,content.length());assertTrue(root instanceof ViewGroup);assertTrue(hasOriginalImage((ViewGroup)root));
        });tap("cancel");hidden();assertEquals(0,confirms.get());unchanged();
    }
    private boolean hasOriginalImage(ViewGroup root){
        Drawable expected=root.getContext().getDrawable(Ui.res(root.getContext(),"mipmap","auto_tip"));assertTrue(expected instanceof BitmapDrawable);
        for(int i=0;i<root.getChildCount();i++){
            View child=root.getChildAt(i);
            if(child instanceof android.widget.ImageView image&&image.isShown()&&image.getDrawable() instanceof BitmapDrawable actual
                    &&actual.getBitmap().sameAs(((BitmapDrawable)expected).getBitmap()))return true;
            if(child instanceof ViewGroup group&&hasOriginalImage(group))return true;
        }return false;
    }

    @Test public void autoTipBackAndOutsideBothCancelWithoutCallingItsConfirmation(){
        AtomicInteger confirms=new AtomicInteger();show(OriginalAutoTipDialog.create().onConfirm(confirms::incrementAndGet));back();hidden();
        show(OriginalAutoTipDialog.create().onConfirm(confirms::incrementAndGet));outside();hidden();assertEquals(0,confirms.get());unchanged();
    }

    @Test public void autoTipConfirmHidesTheActualDialogBeforeInvokingTheContinuation(){
        AtomicInteger confirms=new AtomicInteger();AtomicBoolean hiddenAtCallback=new AtomicBoolean();
        show(OriginalAutoTipDialog.create().onConfirm(()->{hiddenAtCallback.set(!dialog.get().isShowing());confirms.incrementAndGet();}));
        assertEquals(0,confirms.get());tap("confirm");hidden();assertEquals(1,confirms.get());assertTrue(hiddenAtCallback.get());unchanged();
    }

    @Test public void autoTipRealClickableSpanOpensAllSixOriginalBrandRowsAndClosingTheSheetKeepsTheParent(){
        AtomicInteger confirms=new AtomicInteger();show(OriginalAutoTipDialog.create().onConfirm(confirms::incrementAndGet));
        span("content",59,69,"不同机型入口点此查看");await(()->activeBounds("tip_list")!=null);stillShowing();
        String[][] rows={{"小米","无障碍→已下载的应用→自动记账"},{"OPPO","无障碍→已下载的应用→自动记账"},
            {"华为","无障碍→已安装的服务→自动记账"},{"VIVO","无障碍→已下载的服务→自动记账"},
            {"魅族","无障碍→已下载的应用→自动记账"},{"三星","辅助功能→已安装的服务→自动记账"}};
        for(String[] row:rows){int swipes=0;while((!activeText(row[0])||!activeText(row[1]))&&swipes++<8)swipeGuide();
            await(()->activeText(row[0])&&activeText(row[1]));}
        Rect close=activeBounds("btn_close");assertNotNull(close);touch(new Point(close.centerX(),close.centerY()));
        await(()->activeBounds("tip_list")==null);stillShowing();assertEquals(0,confirms.get());tap("cancel");hidden();unchanged();
    }

    @Test public void autoCloseCancelBackAndOutsideDoNotLaunchSettingsOrChangeFeaturePreferences(){
        AtomicInteger launches=new AtomicInteger();
        for(int action=0;action<3;action++){
            show(OriginalAutoCloseTipDialog.create().settingsLauncher((activity,intent)->launches.incrementAndGet()));size(40,false);
            if(action==0)tap("cancel");else if(action==1)back();else outside();hidden();
        }
        assertEquals(0,launches.get());unchanged();
    }

    @Test public void autoCloseDefaultConfirmationActuallyStartsAccessibilitySettingsWithoutNewTaskAfterDismissal(){
        AtomicReference<Intent> launched=new AtomicReference<>();AtomicBoolean hiddenAtLaunch=new AtomicBoolean();AtomicInteger calls=new AtomicInteger();
        Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor=new Instrumentation.ActivityMonitor(){@Override public Instrumentation.ActivityResult onStartActivity(Intent intent){
            if(!Settings.ACTION_ACCESSIBILITY_SETTINGS.equals(intent.getAction()))return null;
            launched.set(new Intent(intent));hiddenAtLaunch.set(!dialog.get().isShowing());calls.incrementAndGet();return new Instrumentation.ActivityResult(Activity.RESULT_CANCELED,null);
        }};instrumentation.addMonitor(monitor);
        try{show(OriginalAutoCloseTipDialog.create());tap("confirm");await(()->launched.get()!=null);hidden();
            assertEquals(1,calls.get());assertTrue(hiddenAtLaunch.get());assertEquals(0,launched.get().getFlags());
            assertNull(launched.get().getData());assertNull(launched.get().getComponent());assertNull(launched.get().getExtras());unchanged();
        }finally{instrumentation.removeMonitor(monitor);}
    }

    @Test public void autoCloseSettingsExceptionFallsBackToGeneralSettingsOnlyAfterTheDialogIsHidden(){
        List<Intent> launched=new ArrayList<>();AtomicBoolean allHidden=new AtomicBoolean(true);
        show(OriginalAutoCloseTipDialog.create().settingsLauncher((activity,intent)->{
            allHidden.set(allHidden.get()&&!dialog.get().isShowing());launched.add(new Intent(intent));
            if(launched.size()==1)throw new IllegalStateException("synthetic unavailable accessibility settings");
        }));tap("confirm");hidden();assertEquals(2,launched.size());assertTrue(allHidden.get());
        assertEquals(Settings.ACTION_ACCESSIBILITY_SETTINGS,launched.get(0).getAction());assertEquals(Settings.ACTION_SETTINGS,launched.get(1).getAction());
        for(Intent intent:launched){assertEquals(0,intent.getFlags());assertNull(intent.getData());assertNull(intent.getExtras());assertNull(intent.getComponent());}
        unchanged();
    }

    @Test public void autoCloseActualGuideAndCloseTextSpansNavigateToSeparateRealRoutesWithoutDismissingOrToggling(){
        List<Intent> launched=new ArrayList<>();List<Boolean> visible=new ArrayList<>();Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor=new Instrumentation.ActivityMonitor(){@Override public Instrumentation.ActivityResult onStartActivity(Intent intent){
            if(intent.getComponent()==null||!ManagerActivity.class.getName().equals(intent.getComponent().getClassName()))return null;
            launched.add(new Intent(intent));visible.add(dialog.get().isShowing());return new Instrumentation.ActivityResult(Activity.RESULT_CANCELED,null);
        }};instrumentation.addMonitor(monitor);
        try{show(OriginalAutoCloseTipDialog.create());span("tip_content",16,24,"自动记账保活教程");await(()->launched.size()==1);stillShowing();
            span("close_content",11,15,"点击此处");await(()->launched.size()==2);stillShowing();
            assertEquals("auto_work",launched.get(0).getStringExtra("page"));assertEquals("capture",launched.get(1).getStringExtra("page"));
            assertEquals(Arrays.asList(true,true),visible);unchanged();tap("cancel");hidden();
        }finally{instrumentation.removeMonitor(monitor);}
    }

    @Test public void topInfoRendersItsOriginalTopWindowAndRejectsBackAndOutsideCancellation(){
        show(OriginalTopInfoDialog.create("通知栏权限使用说明","通知栏可显示自动记账当前状态，并提高自动记账稳定性"));size(20,true);
        scenario.onActivity(a->{assertEquals("通知栏权限使用说明",((TextView)Ui.view(fragment.get().requireView(),"title")).getText().toString());
            assertEquals("通知栏可显示自动记账当前状态，并提高自动记账稳定性",((TextView)Ui.view(fragment.get().requireView(),"content")).getText().toString());});
        back();stillShowing();outside();stillShowing();unchanged();scenario.onActivity(a->((OriginalTopInfoDialog)fragment.get()).dismissForOutcome());hidden();
    }

    @Test public void topInfoOutcomeDismissalIsSynchronousBeforeTheCallerContinuesAndItsArgumentsSurviveRecreation(){
        show(OriginalTopInfoDialog.create("合成通知说明","合成内容"));scenario.recreate();
        await(()->{scenario.onActivity(a->{androidx.fragment.app.Fragment restored=a.getSupportFragmentManager().findFragmentByTag("original-guidance");
            if(restored instanceof OriginalTopInfoDialog top&&top.isResumed()&&top.getDialog()!=null&&top.getDialog().isShowing()
                    &&top.getView()!=null&&top.getView().isAttachedToWindow()&&top.getView().isLaidOut()){
                fragment.set(top);dialog.set(top.getDialog());}});return fragment.get().isResumed();});
        AtomicBoolean continued=new AtomicBoolean(),hiddenBeforeContinuation=new AtomicBoolean();
        scenario.onActivity(a->{assertEquals("合成通知说明",((TextView)Ui.view(fragment.get().requireView(),"title")).getText().toString());
            assertEquals("合成内容",((TextView)Ui.view(fragment.get().requireView(),"content")).getText().toString());
            ((OriginalTopInfoDialog)fragment.get()).dismissForOutcome();hiddenBeforeContinuation.set(!dialog.get().isShowing());continued.set(true);
        });assertTrue(continued.get());assertTrue(hiddenBeforeContinuation.get());hidden();unchanged();
    }
}
