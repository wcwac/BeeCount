package com.wangc.bill.reference;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.wangc.bill.reference.capture.*;
import com.wangc.bill.reference.ui.ManagerActivity;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

/** Real accessibility events from project-owned fixtures, only on the disposable CI emulator. */
@RunWith(AndroidJUnit4.class)
public final class CapturePipelineDeviceTest {
    private Map<String,?> preferences;
    private String priorServices,priorEnabled;
    private UiAutomation automation;
    private final List<String> drafts=new ArrayList<>();
    @Before public void enable()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        assertEquals(36,Build.VERSION.SDK_INT);assertTrue(Build.HARDWARE.equals("ranchu")||Build.HARDWARE.equals("goldfish"));assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        automation=InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        preferences=new HashMap<>(ReferenceApplication.prefs().getAll());priorServices=shell("settings get secure enabled_accessibility_services").trim();priorEnabled=shell("settings get secure accessibility_enabled").trim();
        // End the preceding test's synthetic Activity and delayed events; no real app
        // packages are installed on this disposable emulator.
        for(String fixture:Arrays.asList("com.tencent.mm","com.taobao.taobao","com.eg.android.AlipayGphone","io.github.wcwac.paymentfixture.ignored"))shell("am force-stop "+token(fixture));
        ReferenceApplication.prefs().edit().putBoolean("show_overlay",false).putBoolean("auto_save",false).putBoolean("use_active_window",false).putLong("capture_book",1).putLong("default_account",0).putStringSet("enabled_packages",new HashSet<>(Arrays.asList("com.tencent.mm","com.taobao.taobao"))).commit();
        // This permission is scoped to the disposable emulator and avoids covering the master switch with a dialog.
        shell("pm grant "+token(ReferenceApplication.app().getPackageName())+" android.permission.POST_NOTIFICATIONS");assertEquals(PackageManager.PERMISSION_GRANTED,ReferenceApplication.app().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS));
        AutoApps.enable(false);String component=ReferenceApplication.app().getPackageName()+"/com.wangc.bill.reference.capture.ReferenceCaptureService";shell("settings put secure enabled_accessibility_services "+token(component));shell("settings put secure accessibility_enabled 1");assertEquals("Secure setting was not written as a component",component,shell("settings get secure enabled_accessibility_services").trim());
        await(()->ReferenceCaptureService.current()!=null,10000,"Accessibility service did not connect");assertTrue("Original tool identity is missing",ReferenceCaptureService.status().optBoolean("toolIdentity"));assertRuntimeEnabled(false);ReferenceDiagnostics.clear();
    }
    @After public void restore()throws Exception{
        if(preferences==null)return;
        AutoApps.enable(false);for(String id:drafts)ReferenceApplication.store().dismiss(id);
        SharedPreferences.Editor edit=ReferenceApplication.prefs().edit().clear();for(Map.Entry<String,?> e:preferences.entrySet()){Object v=e.getValue();if(v instanceof Boolean b)edit.putBoolean(e.getKey(),b);else if(v instanceof Long l)edit.putLong(e.getKey(),l);else if(v instanceof Integer i)edit.putInt(e.getKey(),i);else if(v instanceof Float f)edit.putFloat(e.getKey(),f);else if(v instanceof Set<?> set){Set<String> values=new HashSet<>();for(Object item:set)values.add((String)item);edit.putStringSet(e.getKey(),values);}else if(v instanceof String s)edit.putString(e.getKey(),s);}edit.commit();
        shell(priorServices.equals("null")?"settings delete secure enabled_accessibility_services":"settings put secure enabled_accessibility_services "+token(priorServices));shell(priorEnabled.equals("null")?"settings delete secure accessibility_enabled":"settings put secure accessibility_enabled "+token(priorEnabled));
    }
    @Test public void eventsReachOriginalParsersAndRuntimeScopeUpdates()throws Exception{
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){
            touchMaster(scenario,false);assertRuntimeEnabled(true);
            scenario.recreate();visibleMaster(scenario,true);
        }
        SystemClock.sleep(1300);long began=System.currentTimeMillis();launch("com.tencent.mm/com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI");JSONObject wechat=waitDraft("com.tencent.mm",began);JSONObject first=Json.parse(wechat.optString("payload"));assertEquals(1230,first.optLong("amount"));assertEquals("实验小卖部",first.optString("remark"));assertEquals("微信",first.optString("origin"));
        long transaction=ReferenceApplication.store().saveTransaction(first,0,wechat.optString("id"));assertEquals(transaction,ReferenceApplication.store().saveTransaction(first,0,wechat.optString("id")));YimuKernel.complete(wechat.optString("id"));
        SystemClock.sleep(1300);began=System.currentTimeMillis();launch("com.taobao.taobao/com.taobao.android.purchase.aura.TBBuyActivity");SystemClock.sleep(1600);launch("com.taobao.taobao/com.taobao.android.tbabilitykit.pop.StdPopContainerActivity");JSONObject taobao=waitDraft("com.taobao.taobao",began);JSONObject second=Json.parse(taobao.optString("payload"));assertEquals(3990,second.optLong("amount"));assertEquals("实验商品",second.optString("remark"));assertEquals("淘宝",second.optString("origin"));
        AutoApps.select("com.tencent.mm",false);await(()->ReferenceCaptureService.status().optJSONArray("effectivePackages").length()==1&&"com.taobao.taobao".equals(ReferenceCaptureService.status().optJSONArray("effectivePackages").optString(0)),5000,"Runtime package filter did not update");YimuKernel.WORKER.submit(()->{}).get(5,TimeUnit.SECONDS);SystemClock.sleep(500);JSONObject before=ReferenceDiagnostics.snapshot().optJSONObject("eventsByPackage");
        launch("io.github.wcwac.paymentfixture.ignored/com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI");SystemClock.sleep(1200);launch("com.tencent.mm/com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI");SystemClock.sleep(1200);JSONObject after=ReferenceDiagnostics.snapshot().optJSONObject("eventsByPackage");assertEquals("Unselected WeChat leaked events",before.optLong("com.tencent.mm"),after.optLong("com.tencent.mm"));assertEquals("Unknown application leaked events",before.optLong("other"),after.optLong("other"));
        AutoApps.enable(false);await(()->ReferenceCaptureService.status().optInt("eventTypes")==0,5000,"Disabled capture still listens for events");long attempts=ReferenceDiagnostics.snapshot().optLong("submissionAttempts");launch("com.taobao.taobao/com.taobao.android.tbabilitykit.pop.StdPopContainerActivity");SystemClock.sleep(1300);assertEquals(attempts,ReferenceDiagnostics.snapshot().optLong("submissionAttempts"));
    }
    @Test public void visibleDiagnosticMasterUpdatesCapturePageAndRuntimeListener()throws Exception{
        try(ActivityScenario<ManagerActivity> scenario=manager("diagnostics")){
            touchMaster(scenario,false);assertRuntimeEnabled(true);
            scenario.recreate();visibleMaster(scenario,true);
        }
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){
            visibleMaster(scenario,true);scenario.recreate();visibleMaster(scenario,true);
        }
        try(ActivityScenario<ManagerActivity> scenario=manager("diagnostics")){
            touchMaster(scenario,true);assertRuntimeEnabled(false);
        }
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){
            visibleMaster(scenario,false);scenario.recreate();visibleMaster(scenario,false);
        }
    }
    @Test public void alipayWidgetContentUpdatesReachOriginalRulesAndWorkerContinues()throws Exception{
        AutoApps.select("com.eg.android.AlipayGphone",true);
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        SystemClock.sleep(1300);long began=System.currentTimeMillis();launchAmount("com.eg.android.AlipayGphone/com.alipay.android.msp.ui.views.MspContainerActivity","18.60");
        JSONObject candidate=waitDraft("com.eg.android.AlipayGphone",began),payload=Json.parse(candidate.optString("payload"));
        assertEquals(1860,payload.optLong("amount"));assertEquals("实验支付宝商户",payload.optString("merchant"));assertEquals("实验支付宝商户",payload.optString("remark"));assertEquals("支付宝",payload.optString("origin"));
        // Explicit RelativeLayout updates continue after the payment page settles.
        // This must not leave the worker stuck while events accumulate in its queue.
        await(()->"android.widget.RelativeLayout".equals(ReferenceDiagnostics.snapshot().optString("pageClass")),6000,"No natural widget content event reached the service");
        await(()->{JSONObject counts=ReferenceDiagnostics.snapshot().optJSONObject("widgetContentReadsByPackage");return counts!=null&&counts.optLong("com.eg.android.AlipayGphone")>0;},6000,"Alipay widget CONTENT events never completed a nonempty traversal");
        assertEquals("Widget event processing raised an exception",0,ReferenceDiagnostics.snapshot().optLong("failures"));
        ReferenceApplication.store().dismiss(candidate.optString("id"));YimuKernel.complete(candidate.optString("id"));
        SystemClock.sleep(1300);began=System.currentTimeMillis();launchAmount("com.tencent.mm/com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI","15.70");
        JSONObject next=waitDraft("com.tencent.mm",began);assertEquals("Worker did not process the next application's payment",1570,Json.parse(next.optString("payload")).optLong("amount"));
        assertEquals(0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    @Test public void accessibilityOverlayAppearsSavesAndBackgroundCloseKeepsDraft()throws Exception{
        AutoApps.select("com.eg.android.AlipayGphone",true);
        ReferenceApplication.prefs().edit().putBoolean("show_overlay",true).commit();
        observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("diagnostics")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        SystemClock.sleep(1300);long began=System.currentTimeMillis();launchAmount("com.eg.android.AlipayGphone/com.alipay.android.msp.ui.views.MspContainerActivity","19.80");
        JSONObject saved=waitDraft("com.eg.android.AlipayGphone",began);String savedId=saved.optString("id");
        Point save=visibleOverlayControl("btn_save");
        assertTrue("Overlay option unexpectedly disabled",ReferenceApplication.prefs().getBoolean("show_overlay",false));
        assertEquals("A visible overlay must already have a persisted draft","PENDING",ReferenceApplication.store().candidate(savedId).optString("state"));
        touch(save);
        await(()->"SAVED".equals(ReferenceApplication.store().candidate(savedId).optString("state")),5000,"Touching the real overlay save button did not save its draft");
        JSONObject row=ReferenceApplication.store().candidate(savedId);assertTrue(row.optLong("transaction_id")>0);assertEquals(1980,ReferenceApplication.store().transaction(row.optLong("transaction_id")).optLong("amount"));
        await(()->!hasOverlay(),5000,"Saved overlay did not close");
        SystemClock.sleep(1300);began=System.currentTimeMillis();launchAmount("com.eg.android.AlipayGphone/com.alipay.android.msp.ui.views.MspContainerActivity","20.80");
        JSONObject pending=waitDraft("com.eg.android.AlipayGphone",began);String pendingId=pending.optString("id");visibleOverlayControl("btn_save");
        AccessibilityNodeInfo background=overlayControl("background"),content=overlayControl("content_layout");assertNotNull(background);assertNotNull(content);
        Rect outside=new Rect(),sheet=new Rect();background.getBoundsInScreen(outside);content.getBoundsInScreen(sheet);
        assertTrue("No exposed overlay background to close",sheet.top>outside.top+2);touch(new Point(outside.centerX(),outside.top+(sheet.top-outside.top)/2));
        await(()->!hasOverlay(),5000,"Touching the overlay background did not close the window");
        assertEquals("Closing the window lost the persisted pending draft","PENDING",ReferenceApplication.store().candidate(pendingId).optString("state"));
        assertEquals("An overlay operation raised an exception",0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    @Test public void diagnosticConfirmationModeRecoversNullSourceAndShowsRealOverlay()throws Exception{
        AutoApps.select("com.eg.android.AlipayGphone",true);
        ReferenceApplication.prefs().edit().putBoolean("auto_save",true).putBoolean("show_overlay",true).commit();observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("diagnostics")){
            touchMaster(scenario,false);assertRuntimeEnabled(true);
            touchRow(scenario,"改为浮窗确认");
            await(()->!ReferenceApplication.prefs().getBoolean("auto_save",true)&&ReferenceApplication.prefs().getBoolean("show_overlay",false),5000,"Diagnostic action did not select confirmation mode");
            scenario.recreate();assertFalse(ReferenceApplication.prefs().getBoolean("auto_save",true));assertTrue(ReferenceApplication.prefs().getBoolean("show_overlay",false));
        }
        SystemClock.sleep(1300);long began=System.currentTimeMillis();launchNullSourcePayment("21.80");
        JSONObject pending=waitDraft("com.eg.android.AlipayGphone",began);String id=pending.optString("id");
        assertEquals(2180,Json.parse(pending.optString("payload")).optLong("amount"));assertFallbackDraft();visibleOverlayControl("btn_save");
        assertEquals("PENDING",ReferenceApplication.store().candidate(id).optString("state"));
        closeOverlayBackground();await(()->!hasOverlay(),5000,"Null-source payment overlay did not close");
        assertEquals("Closing the recovered payment overlay lost its pending draft","PENDING",ReferenceApplication.store().candidate(id).optString("state"));
        assertEquals(0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    @Test public void nullSourceAutomaticSavePersistsTransactionAndSkipsOverlay()throws Exception{
        AutoApps.select("com.eg.android.AlipayGphone",true);
        ReferenceApplication.prefs().edit().putBoolean("auto_save",true).putBoolean("show_overlay",true).commit();observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        SystemClock.sleep(1300);long began=System.currentTimeMillis();launchNullSourcePayment("22.80");
        JSONObject saved=waitCandidate("com.eg.android.AlipayGphone",began,"SAVED");assertFallbackDraft();
        assertTrue(saved.optLong("transaction_id")>0);assertEquals(2280,ReferenceApplication.store().transaction(saved.optLong("transaction_id")).optLong("amount"));
        await(()->{JSONObject overlay=ReferenceCaptureService.status().optJSONObject("overlay");return overlay!=null&&"SKIPPED_AUTO_SAVE".equals(overlay.optString("lastResult"));},5000,"Automatic save did not explain why its overlay was skipped");
        assertFalse("Automatic save unexpectedly opened an actual accessibility overlay",hasOverlay());
        assertTrue("auto_save must remain selected",ReferenceApplication.prefs().getBoolean("auto_save",false));assertTrue(ReferenceApplication.prefs().getBoolean("show_overlay",false));
        assertEquals(0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    private ActivityScenario<ManagerActivity> manager(String page){return ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(),ManagerActivity.class).putExtra("page",page));}
    private void assertRuntimeEnabled(boolean enabled)throws Exception{
        await(()->{JSONObject status=ReferenceCaptureService.status();return AutoApps.enabled()==enabled&&status.optBoolean("enabled")==enabled&&status.optBoolean("permissionGranted")&&status.optBoolean("connected")&&status.optInt("eventTypes")== (enabled?2080:0);},5000,"Master switch, permission, service connection and event listener disagree");
    }
    private Point visibleMaster(ActivityScenario<ManagerActivity> scenario,boolean checked)throws Exception{
        Point point=new Point();
        await(()->{boolean[] visible={false};scenario.onActivity(a->{CompoundButton button=a.findViewById(com.wangc.bill.R.id.switch_auto_bill);Rect bounds=new Rect();visible[0]=button!=null&&button.isShown()&&button.isEnabled()&&button.isChecked()==checked&&a.hasWindowFocus()&&button.getGlobalVisibleRect(bounds)&&!bounds.isEmpty();});return visible[0];},5000,"Expected a visible, enabled master switch checked="+checked);
        scenario.onActivity(a->{
            CompoundButton button=a.findViewById(com.wangc.bill.R.id.switch_auto_bill);assertNotNull(button);assertTrue("Master switch is hidden",button.isShown());assertTrue(button.isEnabled());assertEquals(checked,button.isChecked());assertTrue(a.hasWindowFocus());
            Rect global=new Rect(),local=new Rect(),window=new Rect();assertTrue(button.getGlobalVisibleRect(global));assertFalse(global.isEmpty());assertTrue(button.getLocalVisibleRect(local));assertTrue("Master switch center is clipped",local.contains(button.getWidth()/2,button.getHeight()/2));
            int[] location=new int[2];button.getLocationOnScreen(location);point.set(location[0]+button.getWidth()/2,location[1]+button.getHeight()/2);a.getWindow().getDecorView().getWindowVisibleDisplayFrame(window);assertTrue("Master switch is outside the screen",window.contains(point.x,point.y));
        });return point;
    }
    private void touchMaster(ActivityScenario<ManagerActivity> scenario,boolean checked)throws Exception{
        touch(visibleMaster(scenario,checked));visibleMaster(scenario,!checked);
    }
    private void touch(Point point)throws Exception{
        long downTime=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(downTime,downTime,MotionEvent.ACTION_DOWN,point.x,point.y,0);down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try{assertTrue("Master switch touch down was rejected",automation.injectInputEvent(down,true));}finally{down.recycle();}
        SystemClock.sleep(60);MotionEvent up=MotionEvent.obtain(downTime,SystemClock.uptimeMillis(),MotionEvent.ACTION_UP,point.x,point.y,0);up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try{assertTrue("Master switch touch up was rejected",automation.injectInputEvent(up,true));}finally{up.recycle();}
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
    private void touchRow(ActivityScenario<ManagerActivity> scenario,String title)throws Exception{
        scenario.onActivity(a->{View label=findText(a.getWindow().getDecorView(),title);assertNotNull("Missing diagnostic action: "+title,label);label.requestRectangleOnScreen(new Rect(0,0,label.getWidth(),label.getHeight()),true);});
        final Point[] point={null};await(()->{scenario.onActivity(a->{View label=findText(a.getWindow().getDecorView(),title);Rect bounds=new Rect();if(label!=null&&label.isShown()&&a.hasWindowFocus()&&label.getGlobalVisibleRect(bounds)&&!bounds.isEmpty())point[0]=new Point(bounds.centerX(),bounds.centerY());});return point[0]!=null;},5000,"Diagnostic action is not visible: "+title);touch(point[0]);
    }
    private static View findText(View root,String text){if(root instanceof TextView label&&text.contentEquals(label.getText()))return root;if(root instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){View found=findText(group.getChildAt(i),text);if(found!=null)return found;}return null;}
    private void observeWindows(){AccessibilityServiceInfo observer=automation.getServiceInfo();observer.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;automation.setServiceInfo(observer);}
    private void assertFallbackDraft()throws Exception{await(()->Arrays.asList("ACTIVE_ROOT","INTERACTIVE_WINDOW").contains(ReferenceDiagnostics.snapshot().optString("lastDraftSourceRoute")),5000,"The actual saved draft did not come from a window fallback");JSONObject meta=ReferenceDiagnostics.snapshot();assertTrue("Fixture did not produce a real null event.getSource()",meta.optLong("eventSourceNull")>0);assertTrue("No same-package window fallback succeeded",meta.optLong("fallbackSuccess")>0);assertFalse("Test accidentally selected the manual active-window comparison mode",ReferenceApplication.prefs().getBoolean("use_active_window",false));}
    private void closeOverlayBackground()throws Exception{AccessibilityNodeInfo background=overlayControl("background"),content=overlayControl("content_layout");assertNotNull(background);assertNotNull(content);Rect outside=new Rect(),sheet=new Rect();background.getBoundsInScreen(outside);content.getBoundsInScreen(sheet);assertTrue("No exposed overlay background to close",sheet.top>outside.top+2);touch(new Point(outside.centerX(),outside.top+(sheet.top-outside.top)/2));}
    private Point visibleOverlayControl(String name)throws Exception{
        final Point[] point={null};await(()->{AccessibilityNodeInfo node=overlayControl(name);if(node==null||!node.isVisibleToUser()||!node.isEnabled())return false;Rect bounds=new Rect();node.getBoundsInScreen(bounds);if(bounds.isEmpty())return false;point[0]=new Point(bounds.centerX(),bounds.centerY());return true;},7000,"No real TYPE_ACCESSIBILITY_OVERLAY window with visible "+name);return point[0];
    }
    private AccessibilityNodeInfo overlayControl(String name){
        int id=switch(name){case "btn_save"->com.wangc.bill.R.id.btn_save;case "background"->com.wangc.bill.R.id.background;case "content_layout"->com.wangc.bill.R.id.content_layout;default->throw new IllegalArgumentException("Unknown overlay control");};
        String resource=ReferenceApplication.app().getResources().getResourceName(id);
        for(AccessibilityWindowInfo window:automation.getWindows())if(window.getType()==AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY){AccessibilityNodeInfo root=window.getRoot();if(root==null||!ReferenceApplication.app().getPackageName().contentEquals(root.getPackageName()==null?"":root.getPackageName()))continue;List<AccessibilityNodeInfo> found=root.findAccessibilityNodeInfosByViewId(resource);if(!found.isEmpty())return found.get(0);}return null;
    }
    private boolean hasOverlay(){return overlayControl("btn_save")!=null;}
    private JSONObject waitDraft(String pkg,long since)throws Exception{return waitCandidate(pkg,since,"PENDING");}
    private JSONObject waitCandidate(String pkg,long since,String state)throws Exception{final JSONObject[] found={null};await(()->{found[0]=ReferenceApplication.store().one("SELECT * FROM candidates WHERE package_name=? AND created>=? AND state=? ORDER BY created DESC LIMIT 1",pkg,""+since,state);return found[0]!=null;},12000,"No "+state+" candidate for "+pkg+": "+ReferenceCaptureService.status());drafts.add(found[0].optString("id"));return found[0];}
    interface Check{boolean ready();}
    private void await(Check check,long timeout,String message)throws Exception{long end=SystemClock.elapsedRealtime()+timeout;while(SystemClock.elapsedRealtime()<end){if(check.ready())return;SystemClock.sleep(50);}throw new AssertionError(message+"\n"+ReferenceCaptureService.status());}
    private void launch(String component)throws Exception{String result=shell("am start -W -n "+token(component));assertFalse(result,result.contains("Error:"));}
    private void launchAmount(String component,String amount)throws Exception{if(!amount.matches("[1-9][0-9]{0,3}\\.[0-9]{2}"))throw new IllegalArgumentException("Invalid synthetic amount");String result=shell("am start -W -S -n "+token(component)+" --es fixture_amount "+amount);assertFalse(result,result.contains("Error:"));}
    private void launchNullSourcePayment(String amount)throws Exception{if(!amount.matches("[1-9][0-9]{0,3}\\.[0-9]{2}"))throw new IllegalArgumentException("Invalid synthetic amount");String result=shell("am start -W -S -n com.eg.android.AlipayGphone/com.alipay.android.msp.ui.views.MspContainerActivity --es fixture_amount "+amount+" --ez fixture_null_source true");assertFalse(result,result.contains("Error:"));}
    private String shell(String command)throws Exception{try(ParcelFileDescriptor descriptor=automation.executeShellCommand(command);InputStream input=new ParcelFileDescriptor.AutoCloseInputStream(descriptor);ByteArrayOutputStream output=new ByteArrayOutputStream()){byte[] bytes=new byte[4096];int n;while((n=input.read(bytes))!=-1){output.write(bytes,0,n);if(output.size()>1024*1024)throw new IOException("Shell output too large");}return output.toString(StandardCharsets.UTF_8.name());}}
    // UiAutomation executes a tokenized command, not a shell script; quotes become literal data.
    private static String token(String value){if(!value.matches("[A-Za-z0-9_./:]+"))throw new IllegalArgumentException("Unexpected emulator command token");return value;}
}
