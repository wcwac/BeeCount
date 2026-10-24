package com.wangc.bill.reference.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.wangc.bill.reference.ReferenceApplication;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Actual denied/granted app-op and attached DialogFragment; Settings launches are intercepted. */
@RunWith(AndroidJUnit4.class)
public final class OriginalOverlayPermissionDeviceTest {
    private UiAutomation automation;
    private String priorOverlay;
    private String[] priorObserverPackages;
    private ActivityScenario<ManagerActivity> scenario;
    private final AtomicReference<OriginalPermissionTipDialog> tip=new AtomicReference<>();
    private final AtomicReference<Dialog> dialog=new AtomicReference<>();

    @Before public void open() throws Exception {
        assertEquals("true",InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        assertEquals(36,Build.VERSION.SDK_INT);
        assertTrue(Build.HARDWARE.equals("ranchu")||Build.HARDWARE.equals("goldfish"));
        assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        automation=InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        AccessibilityServiceInfo observer=automation.getServiceInfo();
        priorObserverPackages=observer.packageNames==null?null:observer.packageNames.clone();
        observer.packageNames=new String[]{ReferenceApplication.app().getPackageName()};automation.setServiceInfo(observer);
        priorOverlay=overlayMode();
    }

    @After public void restore() throws Exception {
        try{if(scenario!=null)scenario.close();}
        finally{try{if(priorOverlay!=null)shell("appops set --user current "+token(ReferenceApplication.app().getPackageName())+" SYSTEM_ALERT_WINDOW "+token(priorOverlay));}
            finally{if(automation!=null){AccessibilityServiceInfo observer=automation.getServiceInfo();observer.packageNames=priorObserverPackages;automation.setServiceInfo(observer);}}}
    }

    private void await(BooleanSupplier condition){long until=SystemClock.uptimeMillis()+5000;while(SystemClock.uptimeMillis()<until){if(condition.getAsBoolean())return;SystemClock.sleep(25);}assertTrue("Actual permission dialog timed out",condition.getAsBoolean());}
    private void permission(boolean granted)throws Exception{shell("appops set --user current "+token(ReferenceApplication.app().getPackageName())+" SYSTEM_ALERT_WINDOW "+(granted?"allow":"ignore"));await(()->Settings.canDrawOverlays(ReferenceApplication.app())==granted);assertEquals(granted,OverlayPermission.granted());}
    private void launch(){scenario=ActivityScenario.launch(new Intent(ReferenceApplication.app(),ManagerActivity.class).putExtra("page","settings"));}
    private void shown(){await(()->{scenario.onActivity(a->{androidx.fragment.app.Fragment fragment=a.getSupportFragmentManager().findFragmentByTag("tip");if(fragment instanceof OriginalPermissionTipDialog current&&current.isResumed()&&current.getDialog()!=null&&current.getDialog().isShowing()&&current.getDialog().getWindow()!=null&&current.getDialog().getWindow().getDecorView().hasWindowFocus()&&current.getView()!=null&&current.getView().isAttachedToWindow()&&current.getView().isLaidOut()){tip.set(current);dialog.set(current.getDialog());}});return dialog.get()!=null;});}
    private void hidden(){await(()->{AtomicBoolean showing=new AtomicBoolean();scenario.onActivity(a->showing.set(dialog.get().isShowing()));return !showing.get();});}
    private void tap(String control){AtomicReference<Point> point=new AtomicReference<>();scenario.onActivity(a->{View view=Ui.view(tip.get().requireView(),control);Rect bounds=new Rect();assertNotNull(view);assertTrue(view.isShown());assertTrue(view.getGlobalVisibleRect(bounds));assertFalse(bounds.isEmpty());int[] location=new int[2];view.getLocationOnScreen(location);point.set(new Point(location[0]+view.getWidth()/2,location[1]+view.getHeight()/2));});touch(point.get());}
    private void touch(Point point){long at=SystemClock.uptimeMillis();MotionEvent down=MotionEvent.obtain(at,at,MotionEvent.ACTION_DOWN,point.x,point.y,0),up=MotionEvent.obtain(at,at+60,MotionEvent.ACTION_UP,point.x,point.y,0);try{down.setSource(InputDevice.SOURCE_TOUCHSCREEN);up.setSource(InputDevice.SOURCE_TOUCHSCREEN);assertTrue(automation.injectInputEvent(down,true));assertTrue(automation.injectInputEvent(up,true));}finally{down.recycle();up.recycle();}InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
    private void stillShowing(){scenario.onActivity(a->assertTrue(dialog.get().isShowing()));}

    @Test public void deniedPermissionShowsTheOriginalNoncancelableTipAndCancelDoesNotOpenSettings()throws Exception{
        permission(false);launch();AtomicInteger launches=new AtomicInteger();scenario.onActivity(a->OverlayPermission.request(a,(host,intent)->launches.incrementAndGet()));shown();
        scenario.onActivity(a->{View root=tip.get().requireView();assertEquals("提示",((TextView)Ui.view(root,"title")).getText().toString());assertEquals("为了能使自动记账、悬浮球记账、流水对账窗口能正常弹出，需要您手动授予悬浮窗权限？",((TextView)Ui.view(root,"content")).getText().toString());assertEquals("去授权",((TextView)Ui.view(root,"confirm")).getText().toString());assertEquals("取消",((TextView)Ui.view(root,"cancel")).getText().toString());assertEquals(View.VISIBLE,Ui.view(root,"cancel").getVisibility());assertEquals(a.getResources().getDisplayMetrics().widthPixels-Ui.dp(a,60),dialog.get().getWindow().getAttributes().width);assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT,dialog.get().getWindow().getAttributes().height);});
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);InstrumentationRegistry.getInstrumentation().waitForIdleSync();stillShowing();
        AtomicReference<Point> outside=new AtomicReference<>();scenario.onActivity(a->outside.set(com.wangc.bill.reference.ReferenceWindowGeometry.outside(dialog.get(),a.getWindow().getDecorView())));touch(outside.get());stillShowing();assertEquals(0,launches.get());
        tap("cancel");hidden();assertEquals(0,launches.get());assertFalse(OverlayPermission.granted());
    }

    @Test public void confirmDismissesBeforeLaunchingTheOriginalNewTaskPackageIntent()throws Exception{
        permission(false);launch();AtomicReference<Intent> launched=new AtomicReference<>();AtomicBoolean dismissedBeforeLaunch=new AtomicBoolean();AtomicInteger launches=new AtomicInteger();scenario.onActivity(a->OverlayPermission.request(a,(host,intent)->{assertSame(a,host);dismissedBeforeLaunch.set(!dialog.get().isShowing());launched.set(new Intent(intent));launches.incrementAndGet();}));shown();assertEquals(0,launches.get());tap("confirm");await(()->launched.get()!=null);hidden();assertEquals(1,launches.get());assertTrue(dismissedBeforeLaunch.get());assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,launched.get().getAction());assertEquals("package:"+ReferenceApplication.app().getPackageName(),launched.get().getDataString());assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK,launched.get().getFlags());assertNull(launched.get().getComponent());assertFalse(OverlayPermission.granted());
    }

    @Test public void anAlreadyGrantedPermissionReturnsWithoutShowingATipOrLaunchingSettings()throws Exception{
        permission(true);launch();AtomicInteger launches=new AtomicInteger();scenario.onActivity(a->OverlayPermission.request(a,(host,intent)->launches.incrementAndGet()));InstrumentationRegistry.getInstrumentation().waitForIdleSync();scenario.onActivity(a->assertNull(a.getSupportFragmentManager().findFragmentByTag("tip")));assertEquals(0,launches.get());assertTrue(OverlayPermission.granted());
    }

    @Test public void aSettingsLaunchExceptionIsCaughtAfterTheTipHasBeenDismissed()throws Exception{
        permission(false);launch();AtomicInteger launches=new AtomicInteger();AtomicBoolean dismissed=new AtomicBoolean();scenario.onActivity(a->OverlayPermission.request(a,(host,intent)->{dismissed.set(!dialog.get().isShowing());launches.incrementAndGet();throw new IllegalStateException("synthetic unavailable Settings handler");}));shown();tap("confirm");await(()->launches.get()==1);hidden();assertTrue(dismissed.get());scenario.onActivity(a->assertFalse(a.isFinishing()));assertFalse(OverlayPermission.granted());
    }

    @Test public void theDefaultLauncherUsesStartActivityOnlyAfterTheConfirmButton()throws Exception{
        permission(false);launch();Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();IntentFilter filter=new IntentFilter(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);filter.addDataScheme("package");Instrumentation.ActivityMonitor monitor=new Instrumentation.ActivityMonitor(filter,new Instrumentation.ActivityResult(Activity.RESULT_CANCELED,null),true);instrumentation.addMonitor(monitor);
        try{scenario.onActivity(OverlayPermission::request);shown();assertEquals(0,monitor.getHits());tap("confirm");await(()->monitor.getHits()==1);hidden();assertEquals(1,monitor.getHits());scenario.onActivity(a->assertFalse(a.isFinishing()));assertFalse(OverlayPermission.granted());}
        finally{instrumentation.removeMonitor(monitor);}
    }

    @Test public void deniedPermissionDirectRequestOpensSettingsWithoutATipAndCatchesLaunchFailure()throws Exception{
        assertDirectRequest(false);
        AtomicInteger launches=new AtomicInteger();
        scenario.onActivity(a->{OverlayPermission.requestDirect(a,(host,intent)->{launches.incrementAndGet();throw new IllegalStateException("synthetic unavailable Settings handler");});assertFalse(a.isFinishing());assertNull(a.getSupportFragmentManager().findFragmentByTag("tip"));});
        assertEquals(1,launches.get());assertFalse(OverlayPermission.granted());
    }

    @Test public void grantedPermissionDirectRequestStillOpensSettingsWithoutATip()throws Exception{
        assertDirectRequest(true);
    }

    private void assertDirectRequest(boolean granted)throws Exception{
        permission(granted);launch();AtomicReference<Intent> launched=new AtomicReference<>();AtomicInteger launches=new AtomicInteger();
        scenario.onActivity(a->{OverlayPermission.requestDirect(a,(host,intent)->{assertSame(a,host);launched.set(new Intent(intent));launches.incrementAndGet();});assertNull(a.getSupportFragmentManager().findFragmentByTag("tip"));});
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertEquals(1,launches.get());assertNotNull(launched.get());assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,launched.get().getAction());assertEquals("package:"+ReferenceApplication.app().getPackageName(),launched.get().getDataString());assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK,launched.get().getFlags());assertNull(launched.get().getComponent());assertEquals(granted,OverlayPermission.granted());
        Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();IntentFilter filter=new IntentFilter(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);filter.addDataScheme("package");Instrumentation.ActivityMonitor monitor=new Instrumentation.ActivityMonitor(filter,new Instrumentation.ActivityResult(Activity.RESULT_CANCELED,null),true);instrumentation.addMonitor(monitor);
        try{scenario.onActivity(OverlayPermission::requestDirect);await(()->monitor.getHits()==1);assertEquals(1,monitor.getHits());scenario.onActivity(a->{assertNull(a.getSupportFragmentManager().findFragmentByTag("tip"));assertFalse(a.isFinishing());});assertEquals(granted,OverlayPermission.granted());}
        finally{instrumentation.removeMonitor(monitor);}
    }

    private String overlayMode()throws Exception{String response=shell("appops get --user current "+token(ReferenceApplication.app().getPackageName())+" SYSTEM_ALERT_WINDOW");Matcher match=Pattern.compile("SYSTEM_ALERT_WINDOW:\\s*(allow|deny|ignore|default|foreground)\\b").matcher(response);if(match.find())return match.group(1);if(response.contains("No operations"))return "default";throw new AssertionError("Unexpected overlay app-op response");}
    private String shell(String command)throws Exception{try(ParcelFileDescriptor descriptor=automation.executeShellCommand(command);InputStream in=new ParcelFileDescriptor.AutoCloseInputStream(descriptor);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] bytes=new byte[4096];int n;while((n=in.read(bytes))!=-1){out.write(bytes,0,n);if(out.size()>1048576)throw new IllegalStateException("Shell output too large");}return out.toString(StandardCharsets.UTF_8.name());}}
    private static String token(String value){if(!value.matches("[A-Za-z0-9_./:]+"))throw new IllegalArgumentException("Invalid emulator token");return value;}
}
