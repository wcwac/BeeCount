package com.wangc.bill.reference;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.wangc.bill.reference.ui.CaptureOverlay;
import com.wangc.bill.reference.ui.LocalFiles;
import com.wangc.bill.reference.ui.OriginalPermissionTipDialog;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runner.Description;
import org.junit.rules.TestWatcher;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.*;

/**
 * Real accessibility events from project-owned fixtures, only on the disposable CI emulator.
 * Same-window fallback is an explicit compatibility extension. These synthetic cases verify
 * that extension and the selected recovered rules, not full equivalence to the original APK.
 */
@RunWith(AndroidJUnit4.class)
public final class CapturePipelineDeviceTest {
    @Rule public final TestWatcher failureMetadata=new TestWatcher(){
        @Override protected void failed(Throwable error,Description description){
            String metadata="REFERENCE_CAPTURE_FAILURE "+description.getMethodName()+" "+ReferenceDiagnostics.snapshot()+" phases="+phaseMarkers;
            System.out.println(metadata);
            // Android redirects stdout to logcat, whose bounded tail can omit an
            // early case. Keep the same metadata in the reported failure stack too.
            error.addSuppressed(new AssertionError(metadata));
        }
    };
    private Map<String,?> preferences;
    private String priorServices,priorEnabled,priorOverlayMode;
    private String[] priorObserverPackages;
    private boolean observerScopeSaved;
    private UiAutomation automation;
    private final List<JSONObject> phaseMarkers=new ArrayList<>();
    private final List<String> drafts=new ArrayList<>();
    private final List<String> windowCandidates=new ArrayList<>();
    private final List<Long> windowTransactions=new ArrayList<>(),windowBooks=new ArrayList<>();
    @Before public void enable()throws Exception{
        assertEquals("true",InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        assertEquals(36,Build.VERSION.SDK_INT);assertTrue(Build.HARDWARE.equals("ranchu")||Build.HARDWARE.equals("goldfish"));assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        automation=InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        preferences=new HashMap<>(ReferenceApplication.prefs().getAll());priorServices=shell("settings get secure enabled_accessibility_services").trim();priorEnabled=shell("settings get secure accessibility_enabled").trim();
        AccessibilityServiceInfo observer=automation.getServiceInfo();priorObserverPackages=observer.packageNames==null?null:observer.packageNames.clone();observerScopeSaved=true;
        // Observe our own windows without consuming payment-app events ahead of
        // the original FLAG_DEFAULT capture service. Window access is independent.
        observer.packageNames=new String[]{ReferenceApplication.app().getPackageName()};automation.setServiceInfo(observer);
        priorOverlayMode=overlayMode();setOverlayMode("allow",true);
        // End the preceding test's synthetic Activity and delayed events; no real app
        // packages are installed on this disposable emulator.
        for(String fixture:Arrays.asList("com.tencent.mm","com.taobao.taobao","com.eg.android.AlipayGphone","io.github.wcwac.paymentfixture.ignored"))shell("am force-stop "+token(fixture));
        ReferenceApplication.prefs().edit().putBoolean("show_overlay",false).putBoolean("auto_save",false).putBoolean("use_active_window",false).putLong("capture_book",1).putLong("default_account",0).putStringSet("enabled_packages",new HashSet<>(Arrays.asList("com.tencent.mm","com.taobao.taobao"))).commit();
        // This permission is scoped to the disposable emulator and avoids covering the master switch with a dialog.
        shell("pm grant "+token(ReferenceApplication.app().getPackageName())+" android.permission.POST_NOTIFICATIONS");assertEquals(PackageManager.PERMISSION_GRANTED,ReferenceApplication.app().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS));
        AutoApps.enable(false);String component=ReferenceApplication.app().getPackageName()+"/com.wangc.bill.reference.capture.ReferenceCaptureService";shell("settings put secure enabled_accessibility_services "+token(component));shell("settings put secure accessibility_enabled 1");assertEquals("Secure setting was not written as a component",component,shell("settings get secure enabled_accessibility_services").trim());
        await(()->ReferenceCaptureService.current()!=null,10000,"Accessibility service did not connect");assertTrue("Original tool identity is missing",ReferenceCaptureService.status().optBoolean("toolIdentity"));assertRuntimeEnabled(false);awaitCaptureWorker();ReferenceTestAccounts.installLegacyFixture(ReferenceApplication.store());ReferenceDiagnostics.clear();
    }
    @After public void restore()throws Exception{
        if(preferences==null)return;
        try{
            AutoApps.enable(false);
            try{awaitCaptureWorker();for(String id:drafts)ReferenceApplication.store().dismiss(id);cleanWindowData();}
            finally{
                SharedPreferences.Editor edit=ReferenceApplication.prefs().edit().clear();for(Map.Entry<String,?> e:preferences.entrySet()){Object v=e.getValue();if(v instanceof Boolean b)edit.putBoolean(e.getKey(),b);else if(v instanceof Long l)edit.putLong(e.getKey(),l);else if(v instanceof Integer i)edit.putInt(e.getKey(),i);else if(v instanceof Float f)edit.putFloat(e.getKey(),f);else if(v instanceof Set<?> set){Set<String> values=new HashSet<>();for(Object item:set)values.add((String)item);edit.putStringSet(e.getKey(),values);}else if(v instanceof String s)edit.putString(e.getKey(),s);}edit.commit();
                shell(priorServices.equals("null")?"settings delete secure enabled_accessibility_services":"settings put secure enabled_accessibility_services "+token(priorServices));shell(priorEnabled.equals("null")?"settings delete secure accessibility_enabled":"settings put secure accessibility_enabled "+token(priorEnabled));
            }
        }finally{try{if(priorOverlayMode!=null){writeOverlayMode(priorOverlayMode);assertEquals("Overlay app-op was not restored",priorOverlayMode,overlayMode());}}finally{if(observerScopeSaved){AccessibilityServiceInfo observer=automation.getServiceInfo();observer.packageNames=priorObserverPackages;automation.setServiceInfo(observer);}}}
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
            touchMaster(scenario,true);assertOriginalMasterDisabled();
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
        assertEquals(1860,payload.optLong("amount"));assertEquals("实验支付宝商户",payload.optString("merchant"));assertEquals("实验支付宝商户",payload.optString("remark"));assertEquals("支付宝",payload.optString("origin"));assertEquals("The non-important payment-method label was not read","余额宝",payload.optString("asset_text"));
        // Explicit RelativeLayout updates continue after the payment page settles.
        // This must not leave the worker stuck while events accumulate in its queue.
        await(()->"android.widget.RelativeLayout".equals(ReferenceDiagnostics.snapshot().optString("pageClass")),6000,"No natural widget content event reached the service");
        await(()->{JSONObject counts=ReferenceDiagnostics.snapshot().optJSONObject("widgetContentReadsByPackage");return counts!=null&&counts.optLong("com.eg.android.AlipayGphone")>0;},6000,"Alipay widget CONTENT events never completed a nonempty traversal");
        assertEquals("Widget event processing raised an exception",0,ReferenceDiagnostics.snapshot().optLong("failures"));
        ReferenceApplication.store().dismiss(candidate.optString("id"));YimuKernel.complete(candidate.optString("id"));
        SystemClock.sleep(1300);began=System.currentTimeMillis();launchAmount("com.tencent.mm/com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI","15.70");
        JSONObject next=waitDraft("com.tencent.mm",began);assertEquals("Worker did not process the next application's payment",1570,Json.parse(next.optString("payload")).optLong("amount"));
        assertEquals(ReferenceDiagnostics.snapshot().toString(),0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    @Test public void applicationOverlayAppearsSavesAndIgnoresBackgroundTouch()throws Exception{
        AutoApps.select("com.eg.android.AlipayGphone",true);
        ReferenceApplication.prefs().edit().putBoolean("show_overlay",true).commit();
        observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("diagnostics")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        SystemClock.sleep(1300);long began=System.currentTimeMillis();launchAmount("com.eg.android.AlipayGphone/com.alipay.android.msp.ui.views.MspContainerActivity","19.80");
        JSONObject saved=waitDisplayedDraft("com.eg.android.AlipayGphone",began,1980);String savedId=saved.optString("id");
        Point save=visibleOverlayControl("btn_save");
        assertTrue("Overlay option unexpectedly disabled",ReferenceApplication.prefs().getBoolean("show_overlay",false));
        assertEquals("A visible overlay must already have a persisted draft","PENDING",ReferenceApplication.store().candidate(savedId).optString("state"));
        touch(save);
        await(()->"SAVED".equals(ReferenceApplication.store().candidate(savedId).optString("state")),5000,"Touching the real overlay save button did not save its draft");
        JSONObject row=ReferenceApplication.store().candidate(savedId);assertTrue(row.optLong("transaction_id")>0);assertEquals(1980,ReferenceApplication.store().transaction(row.optLong("transaction_id")).optLong("amount"));
        await(()->!hasOverlay(),5000,"Saved overlay did not close");
        SystemClock.sleep(1300);began=System.currentTimeMillis();launchAmount("com.eg.android.AlipayGphone/com.alipay.android.msp.ui.views.MspContainerActivity","20.80");
        JSONObject pending=waitDisplayedDraft("com.eg.android.AlipayGphone",began,2080);String pendingId=pending.optString("id");visibleOverlayControl("btn_save");
        AccessibilityNodeInfo background=overlayControl("background"),content=overlayControl("content_layout");assertNotNull(background);assertNotNull(content);
        Rect outside=new Rect(),sheet=new Rect();background.getBoundsInScreen(outside);content.getBoundsInScreen(sheet);
        assertTrue("No exposed overlay background to exercise",sheet.top>outside.top+2);touch(new Point(outside.centerX(),outside.top+(sheet.top-outside.top)/2));
        assertTrue("The original background has no dismiss action",hasOverlay());assertEquals("PENDING",ReferenceApplication.store().candidate(pendingId).optString("state"));
        touch(visibleOverlayControl("close_btn"));await(()->!hasOverlay(),5000,"The explicit cancel action did not close the window");
        assertEquals("Explicit cancellation did not dismiss its candidate","DISMISSED",ReferenceApplication.store().candidate(pendingId).optString("state"));assertEquals(0,ReferenceApplication.store().candidate(pendingId).optLong("transaction_id"));
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
        touchOverlayBackground();assertTrue("A background tap unexpectedly dismissed the recovered confirmation",hasOverlay());assertEquals("PENDING",ReferenceApplication.store().candidate(id).optString("state"));
        touch(visibleOverlayControl("close_btn"));await(()->!hasOverlay(),5000,"Null-source payment overlay did not cancel");
        assertEquals("Explicit cancellation left its recovered draft pending","DISMISSED",ReferenceApplication.store().candidate(id).optString("state"));assertEquals(0,ReferenceApplication.store().candidate(id).optLong("transaction_id"));
        assertEquals(ReferenceDiagnostics.snapshot().toString(),0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    @Test public void nullSourceAutoAddShowsCountdownBeforeSavingTransaction()throws Exception{
        AutoApps.select("com.eg.android.AlipayGphone",true);
        ReferenceApplication.prefs().edit().putBoolean("auto_save",true).putBoolean("show_overlay",true).commit();observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        SystemClock.sleep(1300);String paymentId="CLOUD"+UUID.randomUUID().toString().replace("-","");long began=System.currentTimeMillis();launchNullSourcePayment("22.80",paymentId);
        JSONObject pending=waitDraft("com.eg.android.AlipayGphone",began);String id=pending.optString("id");long observed=SystemClock.elapsedRealtime();assertStrongAutoEvidence(pending,paymentId);assertFallbackDraft();visibleOverlayControl("btn_save");
        // Original AutoAddLayout.onAttachedToWindow starts CountDownTimer(5000,1000),
        // whose onFinish clicks btnSave. onTick floors millis/1000, so the first
        // visible label can be 新增(4); do not invent an initial 新增(5) requirement.
        await(()->{AccessibilityNodeInfo label=overlayControl("btn_save_text");return label!=null&&label.getText()!=null&&label.getText().toString().matches("新增\\([1-5]\\)");},2500,"The original automatic-add countdown label never appeared");
        assertEquals("Countdown saved without first allowing confirmation","PENDING",ReferenceApplication.store().candidate(id).optString("state"));
        SystemClock.sleep(1200);assertEquals("Automatic add bypassed the five-second countdown","PENDING",ReferenceApplication.store().candidate(id).optString("state"));
        await(()->"SAVED".equals(ReferenceApplication.store().candidate(id).optString("state")),10000,"The automatic-add countdown did not save its payment");
        assertTrue("Automatic add completed too early",SystemClock.elapsedRealtime()-observed>=4000);
        JSONObject saved=ReferenceApplication.store().candidate(id);
        assertTrue(saved.optLong("transaction_id")>0);assertEquals(2280,ReferenceApplication.store().transaction(saved.optLong("transaction_id")).optLong("amount"));
        await(()->!hasOverlay(),5000,"The automatically saved confirmation window did not close");
        assertTrue("auto_save must remain selected",ReferenceApplication.prefs().getBoolean("auto_save",false));assertTrue(ReferenceApplication.prefs().getBoolean("show_overlay",false));
        assertEquals(ReferenceDiagnostics.snapshot().toString(),0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    @Test public void selectingCategoryCancelsAutomaticAddButStillAllowsManualSave()throws Exception{
        AutoApps.select("com.eg.android.AlipayGphone",true);
        ReferenceApplication.prefs().edit().putBoolean("auto_save",true).putBoolean("show_overlay",true).commit();observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        SystemClock.sleep(1300);String paymentId="CLOUD"+UUID.randomUUID().toString().replace("-","");long began=System.currentTimeMillis();launchNullSourcePayment("23.80",paymentId);
        JSONObject pending=waitDraft("com.eg.android.AlipayGphone",began);String id=pending.optString("id");assertStrongAutoEvidence(pending,paymentId);assertFallbackDraft();visibleOverlayControl("btn_save");
        await(()->{AccessibilityNodeInfo label=overlayControl("btn_save_text");return label!=null&&label.getText()!=null&&label.getText().toString().matches("新增\\([1-5]\\)");},2500,"No automatic-add countdown before category selection");
        JSONObject selected=null;long initialCategory=Json.parse(pending.optString("payload")).optLong("category_id");
        for(JSONObject category:ReferenceApplication.store().categories("expense"))if(category.optLong("id")!=initialCategory){selected=category;break;}assertNotNull("No alternate category to exercise the real selection",selected);
        // RecyclerView publishes collection metadata after its first adapter layout.
        // Keep the six-column assertion while waiting for that actual window state.
        await(()->{AccessibilityNodeInfo grid=overlayControl("bill_parent_type_list");return grid!=null&&grid.getCollectionInfo()!=null
            &&grid.getCollectionInfo().getColumnCount()==6;},1500,"Automatic-entry categories did not expose the recovered six-column grid");
        // Recovered AutoAddLayout.I0 starts with a1(): cancel timer, clear it and
        // restore 新增. Inject the real category item touch, not an arbitrary tap.
        Point categoryPoint=visibleOverlayCategory(selected.optString("name"));
        assertTrue("The automatic-add timer stopped before the actual category touch",CaptureOverlay.status().optBoolean("countdownActive"));
        assertEquals("The countdown saved before the category touch","PENDING",ReferenceApplication.store().candidate(id).optString("state"));
        touch(categoryPoint);
        await(()->{AccessibilityNodeInfo label=overlayControl("btn_save_text");return label!=null&&"新增".contentEquals(label.getText()==null?"":label.getText());},3000,"Category selection did not cancel the countdown label");
        SystemClock.sleep(5500);assertEquals("A cancelled automatic-add countdown still saved the draft","PENDING",ReferenceApplication.store().candidate(id).optString("state"));assertTrue("Cancelling the countdown should keep its editable confirmation window",hasOverlay());
        touch(visibleOverlayControl("btn_save"));await(()->"SAVED".equals(ReferenceApplication.store().candidate(id).optString("state")),5000,"Manual save stopped working after cancelling automatic add");
        JSONObject saved=ReferenceApplication.store().candidate(id),transaction=ReferenceApplication.store().transaction(saved.optLong("transaction_id"));assertNotNull(transaction);assertEquals(2380,transaction.optLong("amount"));assertEquals("The real category item selection was not applied",selected.optLong("id"),transaction.optLong("category_id"));
        assertEquals(selected.optInt("semantic_id"),transaction.optInt("parent_semantic_id"));assertEquals("The original automatic-entry parent-only save omits childCategoryId, retaining its default zero",0,transaction.optInt("child_semantic_id"));
        await(()->!hasOverlay(),5000,"Manually saved window did not close");assertEquals(ReferenceDiagnostics.snapshot().toString(),0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    @Test public void missingOverlayPermissionExplainsTheBlockAndPreviewCreatesNoWindowOrDraft()throws Exception{
        ReferenceApplication.prefs().edit().putBoolean("show_overlay",true).putBoolean("auto_save",false).commit();observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("diagnostics")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        assertCapturePermissionSwitch(true);setOverlayMode("ignore",false);assertCapturePermissionSwitch(false);
        long candidates=ReferenceApplication.store().scalar("SELECT COUNT(*) n FROM candidates");
        try(ActivityScenario<ManagerActivity> scenario=manager("diagnostics")){
            scenario.onActivity(a->{assertNotNull("No visible permission action in diagnostics",findText(a.getWindow().getDecorView(),"授权悬浮窗"));assertFalse(ReferenceCaptureService.status().optJSONObject("overlay").optBoolean("permissionGranted"));});
            touchRow(scenario,"测试确认浮窗");
            await(()->"OVERLAY_PERMISSION_REQUIRED".equals(ReferenceCaptureService.status().optJSONObject("overlay").optString("lastResult")),5000,"Missing overlay permission was not explained");
            scenario.recreate();scenario.onActivity(a->{assertNotNull(findText(a.getWindow().getDecorView(),"授权悬浮窗"));assertTrue("Diagnostics did not explain the overlay permission block",hasTextContaining(a.getWindow().getDecorView(),"尚未获得系统悬浮窗权限"));});
        }
        assertFalse("An ungranted overlay was actually shown",hasOverlay());
        assertEquals("Permission preview inserted a candidate",candidates,ReferenceApplication.store().scalar("SELECT COUNT(*) n FROM candidates"));
        setOverlayMode("allow",true);assertCapturePermissionSwitch(true);
        assertEquals(ReferenceDiagnostics.snapshot().toString(),0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    @Test public void automaticAddCannotBypassAWindowThatHasNoOverlayPermission()throws Exception{
        AutoApps.select("com.eg.android.AlipayGphone",true);
        ReferenceApplication.prefs().edit().putBoolean("auto_save",true).putBoolean("show_overlay",true).commit();observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        setOverlayMode("ignore",false);SystemClock.sleep(1300);long began=System.currentTimeMillis();launchNullSourcePayment("24.80");
        JSONObject pending=waitDraft("com.eg.android.AlipayGphone",began);String id=pending.optString("id");assertFallbackDraft();
        await(()->"OVERLAY_PERMISSION_REQUIRED".equals(ReferenceCaptureService.status().optJSONObject("overlay").optString("lastResult")),5000,"Automatic-add window did not report its missing permission");
        // The original timer starts in the attached view. A rejected window may
        // leave our persisted safety draft, but it must never save in the background.
        SystemClock.sleep(6000);assertEquals("Automatic add silently saved without an attached confirmation window","PENDING",ReferenceApplication.store().candidate(id).optString("state"));
        assertEquals(0,ReferenceApplication.store().candidate(id).optLong("transaction_id"));assertFalse(hasOverlay());
        assertEquals(ReferenceDiagnostics.snapshot().toString(),0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    /** Synthetic database setup tests the real repeat window independently of payment parsing. */
    @Test public void aRepeatedBillStopsAutomaticAddAndCanReplaceThroughTheActualPopup()throws Exception{
        ReferenceApplication.prefs().edit().putBoolean("auto_save",true).putBoolean("show_overlay",true).commit();observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        ReferenceStore store=ReferenceApplication.store();long baseline=store.accountBalance(1);long time=1735689600000L;
        JSONObject old=windowData(3120,time);String oldCandidate=windowCandidate(old);long oldId=store.saveTransaction(old,0,oldCandidate);windowTransactions.add(oldId);
        JSONObject replacement=windowData(3220,time);Json.put(replacement,"original_amount",3120);Json.put(replacement,"original_number","31.20");
        String id=windowCandidate(replacement);showWindow(id);visibleOverlayControl("btn_save");
        assertTrue("The matching bill was not identified by the actual window",ReferenceCaptureService.status().optJSONObject("overlay").optBoolean("repeatDetected"));
        assertFalse("A repeated bill must not start the automatic-add timer",ReferenceCaptureService.status().optJSONObject("overlay").optBoolean("countdownActive"));
        SystemClock.sleep(5500);assertEquals("A repeated bill was automatically added","PENDING",store.candidate(id).optString("state"));assertNotNull(store.transaction(oldId));assertTrue(hasOverlay());
        touch(visibleOverlayControl("btn_save"));touch(visibleOverlayControl("check_layout"));touch(visibleOverlayControl("confirm"));
        await(()->"SAVED".equals(store.candidate(id).optString("state")),5000,"The real replacement confirmation did not save its candidate");
        JSONObject saved=store.candidate(id);long newId=saved.optLong("transaction_id");assertTrue(newId>0&&newId!=oldId);assertNull("Replacement retained the old bill",store.transaction(oldId));
        assertEquals(3220,store.transaction(newId).optLong("amount"));assertEquals(id,store.transaction(newId).optString("candidate_id"));
        assertEquals("DISMISSED",store.candidate(oldCandidate).optString("state"));assertEquals(0,store.candidate(oldCandidate).optLong("transaction_id"));assertEquals(baseline-3220,store.accountBalance(1));
        await(()->!hasOverlay(),5000,"Replacement confirmation left the entry window open");assertEquals(ReferenceDiagnostics.snapshot().toString(),0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    /** Select a book through the public window UI; do not edit its data object in the test. */
    @Test public void theActualWindowBookPickerSavesTheSelectedBookAndCandidatePayload()throws Exception{
        ReferenceApplication.prefs().edit().putBoolean("auto_save",false).putBoolean("show_overlay",true).commit();observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        ReferenceStore store=ReferenceApplication.store();String name="窗口回归账本_"+UUID.randomUUID().toString().substring(0,8);long book=store.saveBook(0,name,"CNY",0);windowBooks.add(book);
        String id=windowCandidate(windowData(3430,1735689720000L));showWindow(id);touch(visibleOverlayControl("account_book_layout"));touch(visibleSystemText(name));
        await(()->{AccessibilityNodeInfo label=overlayControl("account_book_name");return label!=null&&name.contentEquals(label.getText()==null?"":label.getText());},5000,"The book picker did not update the actual entry window");
        touch(visibleOverlayControl("btn_save"));await(()->"SAVED".equals(store.candidate(id).optString("state")),5000,"The selected-book entry was not saved");
        JSONObject candidate=store.candidate(id),transaction=store.transaction(candidate.optLong("transaction_id"));assertNotNull(transaction);assertEquals(book,transaction.optLong("book_id"));assertEquals(3430,transaction.optLong("amount"));assertEquals(book,candidate.optLong("book_id"));assertEquals(book,Json.parse(candidate.optString("payload")).optLong("book_id"));assertEquals(id,transaction.optString("candidate_id"));
        await(()->!hasOverlay(),5000,"The selected-book entry window did not close");assertEquals(ReferenceDiagnostics.snapshot().toString(),0,ReferenceDiagnostics.snapshot().optLong("failures"));
    }
    @Test public void backgroundBackupRestoreClosesTheWindowAndInvalidatesQueuedSaves()throws Exception{
        ReferenceApplication.prefs().edit().putBoolean("auto_save",false).putBoolean("show_overlay",true).commit();observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        File archive=File.createTempFile("reference-restore-",".zip",ReferenceApplication.app().getCacheDir());
        CountDownLatch busy=new CountDownLatch(1),release=new CountDownLatch(1),observed=new CountDownLatch(1);AtomicBoolean stayedPending=new AtomicBoolean();
        FutureTask<Void> restore=null;
        try{
            LocalFiles.exportBackup(ReferenceApplication.app(),Uri.fromFile(archive));
            String id=windowCandidate(windowData(3540,1735689840000L));showWindow(id);visibleOverlayControl("btn_save");
            YimuKernel.enqueue(()->{busy.countDown();try{release.await(60,TimeUnit.SECONDS);}catch(InterruptedException error){Thread.currentThread().interrupt();}});
            assertTrue("The capture worker did not reach the controlled in-flight operation",busy.await(5,TimeUnit.SECONDS));
            touch(visibleOverlayControl("btn_save"));
            assertEquals("The queued save ran while the capture worker was occupied","PENDING",ReferenceApplication.store().candidate(id).optString("state"));
            YimuKernel.enqueue(()->{JSONObject row=ReferenceApplication.store().candidate(id);stayedPending.set(row!=null&&"PENDING".equals(row.optString("state"))&&ReferenceApplication.store().one("SELECT id FROM transactions WHERE candidate_id=?",id)==null);observed.countDown();});
            String attached=windowCandidate(windowData(3650,1735689960000L));showWindow(attached);visibleOverlayControl("btn_save");
            // Exercise close from the instrumentation/background thread while the
            // real 2038 view is attached, independently of the service's main-thread refresh.
            CaptureOverlay.close();await(()->!hasOverlay(),5000,"Background close left its real window attached");
            assertEquals("Background close wrote or dismissed its candidate","PENDING",ReferenceApplication.store().candidate(attached).optString("state"));
            showWindow(attached);visibleOverlayControl("btn_save");
            restore=new FutureTask<>(()->{LocalFiles.restoreBackup(ReferenceApplication.app(),Uri.fromFile(archive));return null;});
            new Thread(restore,"reference-restore-test").start();
            // Save and the observation barrier precede restoration. The restore task
            // can only enter this queue after disabling capture and closing its session.
            await(()->!AutoApps.enabled()&&YimuKernel.WORKER.getQueue().stream().anyMatch(task->task instanceof FutureTask<?>),5000,"Background restoration did not reach the serial capture barrier");
            release.countDown();assertTrue("The pending save observation did not run",observed.await(10,TimeUnit.SECONDS));restore.get(10,TimeUnit.SECONDS);
            assertTrue("A closed window wrote its queued save before restoration",stayedPending.get());
            assertNull("Restoration retained a candidate created after its backup",ReferenceApplication.store().candidate(id));
            assertNull("Restoration retained the attached window's candidate",ReferenceApplication.store().candidate(attached));
            assertNull(ReferenceApplication.store().one("SELECT id FROM transactions WHERE candidate_id=?",id));
            await(()->!hasOverlay(),5000,"Background restoration left an application overlay attached");
            assertFalse(AutoApps.enabled());assertFalse(ReferenceApplication.prefs().getBoolean("auto_save",true));assertEquals(ReferenceDiagnostics.snapshot().toString(),0,ReferenceDiagnostics.snapshot().optLong("failures"));
            FutureTask<Boolean> reentrant=new FutureTask<>(()->{try{LocalFiles.restoreBackup(ReferenceApplication.app(),Uri.fromFile(archive));return false;}catch(IllegalStateException expected){return true;}});
            YimuKernel.enqueue(reentrant);assertTrue("Restoration on its own serial worker must reject before waiting on itself",reentrant.get(5,TimeUnit.SECONDS));
        }finally{
            release.countDown();try{if(restore!=null)restore.get(20,TimeUnit.SECONDS);}finally{archive.delete();}
        }
    }
    @Test public void ordinaryWindowCloseDoesNotWithdrawAnAcceptedManualSave()throws Exception{
        ReferenceApplication.prefs().edit().putBoolean("auto_save",false).putBoolean("show_overlay",true).commit();observeWindows();
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        CountDownLatch busy=new CountDownLatch(1),release=new CountDownLatch(1);
        String id=windowCandidate(windowData(3760,1735690080000L));showWindow(id);visibleOverlayControl("btn_save");
        try{
            YimuKernel.enqueue(()->{busy.countDown();try{release.await(30,TimeUnit.SECONDS);}catch(InterruptedException error){Thread.currentThread().interrupt();}});
            assertTrue(busy.await(5,TimeUnit.SECONDS));touch(visibleOverlayControl("btn_save"));CaptureOverlay.close();release.countDown();
            await(()->"SAVED".equals(ReferenceApplication.store().candidate(id).optString("state")),5000,"Ordinary close withdrew a manual save already accepted by the original Z0 path");
            JSONObject row=ReferenceApplication.store().candidate(id);assertEquals(3760,ReferenceApplication.store().transaction(row.optLong("transaction_id")).optLong("amount"));assertFalse(hasOverlay());
        }finally{release.countDown();}
    }
    @Test public void restoringBackupReleasesRealParserCallbacksAndAllowsTheSamePaymentAgain()throws Exception{
        AutoApps.select("com.eg.android.AlipayGphone",true);
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){touchMaster(scenario,false);assertRuntimeEnabled(true);}
        File archive=File.createTempFile("reference-parser-restore-",".zip",ReferenceApplication.app().getCacheDir());
        try{
            LocalFiles.exportBackup(ReferenceApplication.app(),Uri.fromFile(archive));
            recordPhase("BEFORE_FIRST_LAUNCH");
            SystemClock.sleep(1300);long began=System.currentTimeMillis();launchAmount("com.eg.android.AlipayGphone/com.alipay.android.msp.ui.views.MspContainerActivity","38.70");
            JSONObject prior=waitDraft("com.eg.android.AlipayGphone",began);
            // A committed candidate can be observed before submit publishes its
            // callback. Join that worker task before checking the registration.
            awaitCaptureWorker();
            assertTrue("The actual recovered parser did not register its dismissal callback",YimuKernel.registeredCallbackCount()>0);
            assertTrue("The first candidate has no callback",YimuKernel.hasRegisteredCallback(prior.optString("id")));
            recordPhase("BEFORE_RESTORE");
            // Keep the remote tree alive until restoration itself disables capture
            // and reaches its serial barrier. Killing it immediately after a draft
            // races the fixture's remaining content events with Binder getChild.
            LocalFiles.restoreBackup(ReferenceApplication.app(),Uri.fromFile(archive));
            recordPhase("AFTER_RESTORE");
            assertRuntimeEnabled(false);awaitCaptureWorker();
            recordPhase("AFTER_DISABLED_BARRIER");
            assertEquals("Restoration retained obsolete parser registrations",0,YimuKernel.registeredCallbackCount());assertFalse(YimuKernel.hasRegisteredCallback(prior.optString("id")));assertNull(ReferenceApplication.store().candidate(prior.optString("id")));
            assertFalse(AutoApps.enabled());
            shell("am force-stop com.eg.android.AlipayGphone");recordPhase("AFTER_STOP");AutoApps.enable(true);assertRuntimeEnabled(true);
            began=System.currentTimeMillis();launchAmount("com.eg.android.AlipayGphone/com.alipay.android.msp.ui.views.MspContainerActivity","38.70");
            JSONObject next=waitDraft("com.eg.android.AlipayGphone",began);assertNotEquals(prior.optString("id"),next.optString("id"));assertEquals(3870,Json.parse(next.optString("payload")).optLong("amount"));
            awaitCaptureWorker();
            recordPhase("AFTER_RECAPTURE");
            assertTrue(YimuKernel.registeredCallbackCount()>0);assertTrue("The recaptured candidate has no callback",YimuKernel.hasRegisteredCallback(next.optString("id")));assertFalse("An obsolete callback reappeared",YimuKernel.hasRegisteredCallback(prior.optString("id")));assertEquals(ReferenceDiagnostics.snapshot().toString(),0,ReferenceDiagnostics.snapshot().optLong("failures"));
        }catch(Exception|AssertionError error){
            // TestWatcher's failed() runs after @After cleanup. Preserve this
            // case's live state too, before cleanup disconnects the service.
            error.addSuppressed(new AssertionError("REFERENCE_CAPTURE_BEFORE_CLEANUP "+ReferenceDiagnostics.snapshot()+" callbacks="+YimuKernel.registeredCallbackCount()+" phases="+phaseMarkers));
            throw error;
        }finally{archive.delete();}
    }
    private JSONObject windowData(long amount,long time){return Json.object("book_id",1,"kind","expense","currency","CNY","amount",amount,"original_amount",amount,"original_number",Money.text(amount,"CNY"),"time",time,"original_time",time,"original_income",false,"original_currency","CNY","account_id",1,"category_id",ReferenceApplication.store().defaultCategory("expense"),"remark","窗口交互回归","merchant","窗口交互回归","origin","合成窗口测试","fee",0,"discount",0);}
    private String windowCandidate(JSONObject data){String id=ReferenceApplication.store().addCandidate(data,"test.fixture.window",data.optLong("book_id"));windowCandidates.add(id);return id;}
    private void showWindow(String id){InstrumentationRegistry.getInstrumentation().runOnMainSync(()->CaptureOverlay.show(id,null));}
    private void awaitCaptureWorker()throws Exception{
        FutureTask<Void> barrier=new FutureTask<>(()->null);YimuKernel.enqueue(barrier);barrier.get(10,TimeUnit.SECONDS);
    }
    private void recordPhase(String stage){
        JSONObject meta=ReferenceDiagnostics.snapshot();
        phaseMarkers.add(Json.object("stage",stage,"at",System.currentTimeMillis(),"failures",meta.optLong("failures"),
            "lastFailureAt",meta.optLong("lastFailureAt"),"lastFailureCode",meta.optString("lastFailureCode"),
            "readsStarted",meta.optLong("readsStarted"),"readsAborted",meta.optLong("readsAborted"),"registeredCallbacks",YimuKernel.registeredCallbackCount()));
    }
    private void cleanWindowData()throws Exception{
        if(windowCandidates.isEmpty()&&windowTransactions.isEmpty()&&windowBooks.isEmpty())return;
        InstrumentationRegistry.getInstrumentation().runOnMainSync(CaptureOverlay::close);
        YimuKernel.WORKER.submit(()->{}).get(10,TimeUnit.SECONDS);
        ReferenceStore store=ReferenceApplication.store();Set<Long> transactions=new LinkedHashSet<>(windowTransactions);
        for(String id:windowCandidates)for(JSONObject row:store.query("SELECT id FROM transactions WHERE candidate_id=?",id))transactions.add(row.optLong("id"));
        for(long id:transactions)if(store.transaction(id)!=null){store.trash(id,true);store.purge(id);}
        for(String id:windowCandidates)store.getWritableDatabase().delete("candidates","id=?",new String[]{id});
        for(long id:windowBooks)store.removeBook(id);
    }
    private ActivityScenario<ManagerActivity> manager(String page){return ActivityScenario.launch(new Intent(ApplicationProvider.getApplicationContext(),ManagerActivity.class).putExtra("page",page));}
    private void assertRuntimeEnabled(boolean enabled)throws Exception{
        await(()->{JSONObject status=ReferenceCaptureService.status();return AutoApps.enabled()==enabled&&status.optBoolean("enabled")==enabled&&status.optBoolean("permissionGranted")&&status.optBoolean("connected")&&status.optInt("eventTypes")== (enabled?2080:0);},5000,"Master switch, permission, service connection and event listener disagree");
    }
    private void assertOriginalMasterDisabled()throws Exception{
        // UI OFF is the original disableSelf path. Internal enable(false), used
        // by fixture setup and backup restoration, must keep its stronger grant
        // and connection assertions in assertRuntimeEnabled(false).
        await(()->{JSONObject status=ReferenceCaptureService.status();return !AutoApps.enabled()&&!status.optBoolean("enabled")
                &&!status.optBoolean("permissionGranted")&&!status.optBoolean("connected")&&ReferenceCaptureService.current()==null
                &&!OriginalServiceState.isRunning(ReferenceApplication.app());},10000,"UI master OFF did not revoke and disconnect the original service");
        ComponentName own=new ComponentName(ReferenceApplication.app(),ReferenceCaptureService.class);
        for(String value:shell("settings get secure enabled_accessibility_services").trim().split(":"))
            assertNotEquals("UI master OFF left its component authorized",own,ComponentName.unflattenFromString(value));
        awaitCaptureWorker();
    }
    private Point visibleMaster(ActivityScenario<ManagerActivity> scenario,boolean checked)throws Exception{
        Point point=new Point();
        await(()->{boolean[] visible={false};scenario.onActivity(a->{CompoundButton button=a.findViewById(com.wangc.bill.R.id.switch_auto_bill);Rect bounds=new Rect();visible[0]=button!=null&&button.isShown()&&button.isEnabled()&&button.isChecked()==checked&&a.hasWindowFocus()&&button.getGlobalVisibleRect(bounds)&&!bounds.isEmpty();});return visible[0];},5000,"Expected a visible, enabled master switch checked="+checked);
        scenario.onActivity(a->{
            CompoundButton button=a.findViewById(com.wangc.bill.R.id.switch_auto_bill);assertNotNull(button);assertTrue("Master switch is hidden",button.isShown());assertTrue(button.isEnabled());assertEquals(checked,button.isChecked());assertTrue(a.hasWindowFocus());
            Rect global=new Rect(),local=new Rect(),window=new Rect();assertTrue(button.getGlobalVisibleRect(global));assertFalse(global.isEmpty());assertTrue(button.getLocalVisibleRect(local));assertTrue("Master switch center is clipped",local.contains(button.getWidth()/2,button.getHeight()/2));
            Point center=ReferenceWindowGeometry.center(button);point.set(center.x,center.y);a.getWindow().getDecorView().getWindowVisibleDisplayFrame(window);assertTrue("Master switch is outside the screen",window.contains(point.x,point.y));
        });return point;
    }
    private void touchMaster(ActivityScenario<ManagerActivity> scenario,boolean checked)throws Exception{
        touch(visibleMaster(scenario,checked));visibleMaster(scenario,!checked);
    }
    private void assertCapturePermissionSwitch(boolean granted)throws Exception{
        try(ActivityScenario<ManagerActivity> scenario=manager("capture")){
            if(!granted)cancelCaptureOverlayPermissionTip(scenario);
            scenario.onActivity(a->{CompoundButton button=a.findViewById(com.wangc.bill.R.id.switch_overlays);assertNotNull("The capture page has no system overlay permission switch",button);assertEquals("The permission switch disagrees with the actual app-op",granted,button.isChecked());});
            scenario.recreate();if(!granted)cancelCaptureOverlayPermissionTip(scenario);
            scenario.onActivity(a->{CompoundButton button=a.findViewById(com.wangc.bill.R.id.switch_overlays);assertNotNull(button);assertEquals("The permission switch lost its system state after recreation",granted,button.isChecked());});
        }
    }
    private void cancelCaptureOverlayPermissionTip(ActivityScenario<ManagerActivity> scenario)throws Exception{
        // Running+master ON+SAW denied now deliberately shows the recovered
        // onStart prompt. Exercise its actual cancel control before checking the
        // underlying page, including the fresh prompt after Activity recreation.
        android.app.Dialog[] pending={null};Point point=new Point();
        await(()->{scenario.onActivity(a->{
            androidx.fragment.app.Fragment fragment=a.getSupportFragmentManager().findFragmentByTag("tip");
            if(!(fragment instanceof OriginalPermissionTipDialog tip)||!tip.isResumed()||tip.getDialog()==null
                    ||!tip.getDialog().isShowing()||!tip.getDialog().getWindow().getDecorView().hasWindowFocus()||tip.getView()==null)return;
            View cancel=tip.getView().findViewById(com.wangc.bill.R.id.cancel);Rect local=new Rect();
            if(cancel==null||!cancel.isShown()||!cancel.isEnabled()||!cancel.getLocalVisibleRect(local)
                    ||!local.contains(cancel.getWidth()/2,cancel.getHeight()/2))return;
            // A Dialog's global visible rectangle is in its root/window space;
            // the injected pointer must instead use the shared screen mapping.
            Point center=ReferenceWindowGeometry.center(cancel);point.set(center.x,center.y);pending[0]=tip.getDialog();
        });return pending[0]!=null;},5000,"Denied SAW did not show its real capture onStart permission prompt");
        touch(point);
        await(()->{boolean[] dismissed={false};scenario.onActivity(a->dismissed[0]=!pending[0].isShowing()&&a.hasWindowFocus());return dismissed[0];},5000,"Actual permission cancellation did not return focus to the capture page");
        assertFalse("Cancelling the prompt unexpectedly granted SAW",Settings.canDrawOverlays(ReferenceApplication.app()));
        assertTrue("Cancelling SAW guidance changed the master selection",AutoApps.enabled());
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
        final Point[] point={null};await(()->{scenario.onActivity(a->{View label=findText(a.getWindow().getDecorView(),title);Rect bounds=new Rect();if(label!=null&&label.isShown()&&a.hasWindowFocus()&&label.getGlobalVisibleRect(bounds)&&!bounds.isEmpty())point[0]=ReferenceWindowGeometry.center(label);});return point[0]!=null;},5000,"Diagnostic action is not visible: "+title);touch(point[0]);
    }
    private static View findText(View root,String text){if(root instanceof TextView label&&text.contentEquals(label.getText()))return root;if(root instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){View found=findText(group.getChildAt(i),text);if(found!=null)return found;}return null;}
    private static boolean hasTextContaining(View root,String text){if(root instanceof TextView label&&label.getText().toString().contains(text))return true;if(root instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++)if(hasTextContaining(group.getChildAt(i),text))return true;return false;}
    private void observeWindows(){AccessibilityServiceInfo observer=automation.getServiceInfo();observer.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;automation.setServiceInfo(observer);}
    private void assertFallbackDraft()throws Exception{await(()->Arrays.asList("ACTIVE_ROOT","INTERACTIVE_WINDOW").contains(ReferenceDiagnostics.snapshot().optString("lastDraftSourceRoute")),5000,"The actual saved draft did not come from a window fallback");JSONObject meta=ReferenceDiagnostics.snapshot();assertTrue("Fixture did not produce a real null event.getSource()",meta.optLong("eventSourceNull")>0);assertTrue("No same-package window fallback succeeded",meta.optLong("fallbackSuccess")>0);assertFalse("Test accidentally selected the manual active-window comparison mode",ReferenceApplication.prefs().getBoolean("use_active_window",false));}
    private void touchOverlayBackground()throws Exception{AccessibilityNodeInfo background=overlayControl("background"),content=overlayControl("content_layout");assertNotNull(background);assertNotNull(content);Rect outside=new Rect(),sheet=new Rect();background.getBoundsInScreen(outside);content.getBoundsInScreen(sheet);assertTrue("No exposed overlay background to exercise",sheet.top>outside.top+2);touch(new Point(outside.centerX(),outside.top+(sheet.top-outside.top)/2));}
    private Point visibleOverlayControl(String name)throws Exception{
        final Point[] point={null};await(()->{AccessibilityNodeInfo node=overlayControl(name);if(node==null||!node.isVisibleToUser()||!node.isEnabled())return false;Rect bounds=new Rect();node.getBoundsInScreen(bounds);if(bounds.isEmpty())return false;point[0]=new Point(bounds.centerX(),bounds.centerY());return true;},7000,"No real TYPE_APPLICATION_OVERLAY window with visible "+name);assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,ReferenceCaptureService.status().optJSONObject("overlay").optInt("windowType"));return point[0];
    }
    private Point visibleOverlayCategory(String name)throws Exception{final Point[] point={null};await(()->{AccessibilityNodeInfo grid=overlayControl("category_pager");if(grid==null)return false;for(AccessibilityNodeInfo node:grid.findAccessibilityNodeInfosByText(name)){if(!name.contentEquals(node.getText()==null?"":node.getText())||!node.isVisibleToUser())continue;Rect bounds=new Rect();node.getBoundsInScreen(bounds);if(!bounds.isEmpty()){point[0]=new Point(bounds.centerX(),bounds.centerY());return true;}}return false;},3000,"No real visible category item: "+name);return point[0];}
    private Point visibleSystemText(String text)throws Exception{final Point[] point={null};await(()->{for(AccessibilityWindowInfo window:automation.getWindows())if(window.getType()==AccessibilityWindowInfo.TYPE_SYSTEM){AccessibilityNodeInfo root=window.getRoot();if(root==null||!ReferenceApplication.app().getPackageName().contentEquals(root.getPackageName()==null?"":root.getPackageName()))continue;for(AccessibilityNodeInfo node:root.findAccessibilityNodeInfosByText(text)){if(!text.contentEquals(node.getText()==null?"":node.getText())||!node.isVisibleToUser())continue;Rect bounds=new Rect();node.getBoundsInScreen(bounds);if(!bounds.isEmpty()){point[0]=new Point(bounds.centerX(),bounds.centerY());return true;}}}return false;},5000,"No real application-overlay text: "+text);return point[0];}
    private AccessibilityNodeInfo overlayControl(String name){
        int id=switch(name){case "cost"->com.wangc.bill.R.id.cost;case "btn_save"->com.wangc.bill.R.id.btn_save;case "btn_save_text"->com.wangc.bill.R.id.btn_save_text;case "close_btn"->com.wangc.bill.R.id.close_btn;case "category_pager"->com.wangc.bill.R.id.category_pager;case "bill_parent_type_list"->com.wangc.bill.R.id.bill_parent_type_list;case "account_book_layout"->com.wangc.bill.R.id.account_book_layout;case "account_book_name"->com.wangc.bill.R.id.account_book_name;case "check_layout"->com.wangc.bill.R.id.check_layout;case "confirm"->com.wangc.bill.R.id.confirm;case "background"->com.wangc.bill.R.id.background;case "content_layout"->com.wangc.bill.R.id.content_layout;default->throw new IllegalArgumentException("Unknown overlay control");};
        String resource=ReferenceApplication.app().getResources().getResourceName(id);
        // AccessibilityWindowInfo uses its own small type enum: a WM application
        // overlay (2038) is a TYPE_SYSTEM window here, not the integer 2038.
        for(AccessibilityWindowInfo window:automation.getWindows())if(window.getType()==AccessibilityWindowInfo.TYPE_SYSTEM){AccessibilityNodeInfo root=window.getRoot();if(root==null||!ReferenceApplication.app().getPackageName().contentEquals(root.getPackageName()==null?"":root.getPackageName()))continue;List<AccessibilityNodeInfo> found=root.findAccessibilityNodeInfosByViewId(resource);if(!found.isEmpty())return found.get(0);}return null;
    }
    private boolean hasOverlay(){return overlayControl("btn_save")!=null;}
    private JSONObject waitDisplayedDraft(String pkg,long since,long expectedMinor)throws Exception{
        final JSONObject[] found={null};
        await(()->{
            JSONObject overlay=CaptureOverlay.status();if(!overlay.optBoolean("attached"))return false;
            String id=overlay.optString("candidateId");if(id.isEmpty())return false;
            JSONObject candidate=ReferenceApplication.store().candidate(id);
            if(candidate==null||!"PENDING".equals(candidate.optString("state"))||!pkg.equals(candidate.optString("package_name"))||candidate.optLong("created")<since)return false;
            JSONObject payload=Json.parse(candidate.optString("payload"));if(payload.optLong("amount")!=expectedMinor)return false;
            AccessibilityNodeInfo cost=overlayControl("cost");
            if(cost==null||!cost.isVisibleToUser()||cost.getText()==null||!Money.text(expectedMinor,"CNY").contentEquals(cost.getText()))return false;
            // A same-package repaint may preserve a different review-only PENDING
            // row. Bind only the real window being touched, never the newest row.
            if(!id.equals(CaptureOverlay.status().optString("candidateId")))return false;
            found[0]=candidate;return true;
        },12000,"No actual overlay attached to the expected synthetic payment");
        drafts.add(found[0].optString("id"));return found[0];
    }
    private JSONObject waitDraft(String pkg,long since)throws Exception{return waitCandidate(pkg,since,"PENDING");}
    private JSONObject waitCandidate(String pkg,long since,String state)throws Exception{final JSONObject[] found={null};await(()->{found[0]=ReferenceApplication.store().one("SELECT * FROM candidates WHERE package_name=? AND created>=? AND state=? ORDER BY created DESC LIMIT 1",pkg,""+since,state);return found[0]!=null;},12000,"No "+state+" candidate for "+pkg+": "+ReferenceCaptureService.status());drafts.add(found[0].optString("id"));return found[0];}
    interface Check{boolean ready();}
    private void await(Check check,long timeout,String message)throws Exception{long end=SystemClock.elapsedRealtime()+timeout;while(SystemClock.elapsedRealtime()<end){if(check.ready())return;SystemClock.sleep(50);}throw new AssertionError(message+"\n"+ReferenceCaptureService.status());}
    private void launch(String component)throws Exception{String result=shell("am start -W -n "+token(component));assertFalse(result,result.contains("Error:"));}
    private void launchAmount(String component,String amount)throws Exception{if(!amount.matches("[1-9][0-9]{0,3}\\.[0-9]{2}"))throw new IllegalArgumentException("Invalid synthetic amount");String result=shell("am start -W -S -n "+token(component)+" --es fixture_amount "+amount);assertFalse(result,result.contains("Error:"));}
    private void launchNullSourcePayment(String amount)throws Exception{launchNullSourcePayment(amount,null);}
    private void launchNullSourcePayment(String amount,String paymentId)throws Exception{
        if(!amount.matches("[1-9][0-9]{0,3}\\.[0-9]{2}"))throw new IllegalArgumentException("Invalid synthetic amount");
        if(paymentId!=null&&!paymentId.matches("[A-Za-z0-9_-]{8,128}"))throw new IllegalArgumentException("Invalid synthetic payment ID");
        String result=shell("am start -W -S -n com.eg.android.AlipayGphone/com.alipay.android.msp.ui.views.MspContainerActivity --es fixture_amount "+amount+" --ez fixture_null_source true"+(paymentId==null?"":" --es fixture_payment_id "+token(paymentId)));
        assertFalse(result,result.contains("Error:"));
    }
    private void assertStrongAutoEvidence(JSONObject candidate,String paymentId){
        JSONObject payload=Json.parse(candidate.optString("payload"));
        assertEquals("PAYMENT_ID",payload.optString("capture_identity_kind"));assertEquals(paymentId,payload.optString("capture_payment_id"));
        assertFalse("A proved payment with explicit default account must allow the real timer",payload.optBoolean("capture_requires_confirmation"));
        assertEquals("The fixture setup explicitly selected no balance account","USER_DEFAULT_NONE",payload.optString("capture_account_resolution"));assertEquals(0,payload.optLong("account_id"));
    }
    private String overlayMode()throws Exception{String value=shell("appops get --user current "+token(ReferenceApplication.app().getPackageName())+" SYSTEM_ALERT_WINDOW");Matcher match=Pattern.compile("SYSTEM_ALERT_WINDOW:\\s*(allow|deny|ignore|default|foreground)\\b").matcher(value);if(match.find())return match.group(1);if(value.contains("No operations"))return "default";throw new AssertionError("Unexpected overlay app-op response: "+value);}
    private void writeOverlayMode(String mode)throws Exception{if(!Arrays.asList("allow","deny","ignore","default","foreground").contains(mode))throw new IllegalArgumentException("Unexpected overlay app-op mode");String output=shell("appops set --user current "+token(ReferenceApplication.app().getPackageName())+" SYSTEM_ALERT_WINDOW "+mode);assertFalse(output,output.contains("Error:"));}
    private void setOverlayMode(String mode,boolean granted)throws Exception{writeOverlayMode(mode);assertEquals("Overlay app-op mode was not written",mode,overlayMode());await(()->Settings.canDrawOverlays(ReferenceApplication.app())==granted,5000,"SYSTEM_ALERT_WINDOW grant did not take effect");}
    private String shell(String command) throws Exception {
        return com.wangc.bill.reference.ReferenceShell.run(automation, command);
    }
    // Empty enabled-service lists and nested-class '$' are legitimate saved
    // settings. Quote this restricted alphabet so restoration preserves both.
    private static String token(String value){if(!value.matches("[A-Za-z0-9_./:$-]*"))throw new IllegalArgumentException("Unexpected emulator command token");return "'"+value+"'";}
}
