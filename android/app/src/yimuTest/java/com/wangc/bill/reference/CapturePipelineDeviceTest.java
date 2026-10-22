package com.wangc.bill.reference;

import android.app.UiAutomation;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.wangc.bill.reference.capture.*;
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
        ReferenceApplication.prefs().edit().putBoolean("show_overlay",false).putBoolean("auto_save",false).putBoolean("use_active_window",false).putLong("capture_book",1).putLong("default_account",0).putStringSet("enabled_packages",new HashSet<>(Arrays.asList("com.tencent.mm","com.taobao.taobao"))).commit();
        AutoApps.enable(true);String component=ReferenceApplication.app().getPackageName()+"/com.wangc.bill.reference.capture.ReferenceCaptureService";shell("settings put secure enabled_accessibility_services "+quote(component));shell("settings put secure accessibility_enabled 1");
        await(()->ReferenceCaptureService.current()!=null,10000,"Accessibility service did not connect");assertTrue("Original tool identity is missing",ReferenceCaptureService.status().optBoolean("toolIdentity"));ReferenceDiagnostics.clear();
    }
    @After public void restore()throws Exception{
        if(preferences==null)return;
        AutoApps.enable(false);for(String id:drafts)ReferenceApplication.store().dismiss(id);
        SharedPreferences.Editor edit=ReferenceApplication.prefs().edit().clear();for(Map.Entry<String,?> e:preferences.entrySet()){Object v=e.getValue();if(v instanceof Boolean b)edit.putBoolean(e.getKey(),b);else if(v instanceof Long l)edit.putLong(e.getKey(),l);else if(v instanceof Integer i)edit.putInt(e.getKey(),i);else if(v instanceof Float f)edit.putFloat(e.getKey(),f);else if(v instanceof Set<?> set){Set<String> values=new HashSet<>();for(Object item:set)values.add((String)item);edit.putStringSet(e.getKey(),values);}else if(v instanceof String s)edit.putString(e.getKey(),s);}edit.commit();
        shell(priorServices.equals("null")?"settings delete secure enabled_accessibility_services":"settings put secure enabled_accessibility_services "+quote(priorServices));shell(priorEnabled.equals("null")?"settings delete secure accessibility_enabled":"settings put secure accessibility_enabled "+quote(priorEnabled));
    }
    @Test public void eventsReachOriginalParsersAndRuntimeScopeUpdates()throws Exception{
        long began=System.currentTimeMillis();launch("com.tencent.mm/com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI");JSONObject wechat=waitDraft("com.tencent.mm",began);JSONObject first=Json.parse(wechat.optString("payload"));assertEquals(1230,first.optLong("amount"));assertEquals("实验小卖部",first.optString("remark"));assertEquals("微信",first.optString("origin"));
        long transaction=ReferenceApplication.store().saveTransaction(first,0,wechat.optString("id"));assertEquals(transaction,ReferenceApplication.store().saveTransaction(first,0,wechat.optString("id")));YimuKernel.complete(wechat.optString("id"));
        SystemClock.sleep(1300);began=System.currentTimeMillis();launch("com.taobao.taobao/com.taobao.android.purchase.aura.TBBuyActivity");SystemClock.sleep(1600);launch("com.taobao.taobao/com.taobao.android.tbabilitykit.pop.StdPopContainerActivity");JSONObject taobao=waitDraft("com.taobao.taobao",began);JSONObject second=Json.parse(taobao.optString("payload"));assertEquals(3990,second.optLong("amount"));assertEquals("实验商品",second.optString("remark"));assertEquals("淘宝",second.optString("origin"));
        AutoApps.select("com.tencent.mm",false);await(()->ReferenceCaptureService.status().optJSONArray("effectivePackages").length()==1,5000,"Runtime package filter did not update");YimuKernel.WORKER.submit(()->{}).get(5,TimeUnit.SECONDS);SystemClock.sleep(500);long events=ReferenceDiagnostics.snapshot().optLong("events");
        launch("io.github.wcwac.paymentfixture.ignored/com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI");SystemClock.sleep(1200);launch("com.tencent.mm/com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI");SystemClock.sleep(1200);assertEquals("Unselected/unknown applications leaked events",events,ReferenceDiagnostics.snapshot().optLong("events"));
        AutoApps.enable(false);await(()->ReferenceCaptureService.status().optInt("eventTypes")==0,5000,"Disabled capture still listens for events");long attempts=ReferenceDiagnostics.snapshot().optLong("submissionAttempts");launch("com.taobao.taobao/com.taobao.android.tbabilitykit.pop.StdPopContainerActivity");SystemClock.sleep(1300);assertEquals(attempts,ReferenceDiagnostics.snapshot().optLong("submissionAttempts"));
    }
    private JSONObject waitDraft(String pkg,long since)throws Exception{final JSONObject[] found={null};await(()->{found[0]=ReferenceApplication.store().one("SELECT * FROM candidates WHERE package_name=? AND created>=? AND state='PENDING' ORDER BY created DESC LIMIT 1",pkg,""+since);return found[0]!=null;},12000,"No draft for "+pkg+": "+ReferenceCaptureService.status());drafts.add(found[0].optString("id"));return found[0];}
    interface Check{boolean ready();}
    private void await(Check check,long timeout,String message)throws Exception{long end=SystemClock.elapsedRealtime()+timeout;while(SystemClock.elapsedRealtime()<end){if(check.ready())return;SystemClock.sleep(50);}throw new AssertionError(message+"\n"+ReferenceCaptureService.status());}
    private void launch(String component)throws Exception{String result=shell("am start -W -n "+quote(component));assertFalse(result,result.contains("Error:"));}
    private String shell(String command)throws Exception{try(ParcelFileDescriptor descriptor=automation.executeShellCommand(command);InputStream input=new ParcelFileDescriptor.AutoCloseInputStream(descriptor);ByteArrayOutputStream output=new ByteArrayOutputStream()){byte[] bytes=new byte[4096];int n;while((n=input.read(bytes))!=-1){output.write(bytes,0,n);if(output.size()>1024*1024)throw new IOException("Shell output too large");}return output.toString(StandardCharsets.UTF_8.name());}}
    private static String quote(String value){return "'"+value.replace("'","'\\''")+"'";}
}
