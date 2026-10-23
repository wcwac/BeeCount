package com.wangc.bill.reference.capture;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.wangc.bill.reference.Json;
import com.wangc.bill.reference.ReferenceApplication;
import com.wangc.bill.reference.ui.CaptureOverlay;
import java.lang.ref.WeakReference;
import org.json.JSONObject;

public final class ReferenceCaptureService extends AccessibilityService {
    private static volatile WeakReference<ReferenceCaptureService> connected = new WeakReference<>(null);
    private final Handler main = new Handler(Looper.getMainLooper());
    @Override protected void onServiceConnected() { connected = new WeakReference<>(this); applySettings(); ReferenceDiagnostics.result("CONNECTED"); }
    public static ReferenceCaptureService current() { return connected.get(); }
    public static void refresh() { ReferenceCaptureService s = current(); if (s != null) { if (Looper.myLooper() == Looper.getMainLooper()) s.applySettings(); else s.main.post(s::applySettings); } }
    private void applySettings() {
        AccessibilityServiceInfo info = getServiceInfo(); if (info == null) return;
        info.packageNames = AutoApps.selected().toArray(new String[0]);
        info.eventTypes = AutoApps.enabled() && info.packageNames.length > 0 ? AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED : 0;
        // The original APK includes non-important and interactive-window nodes.
        // Keep these original read flags: they materially change hybrid pages.
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.notificationTimeout = 10;
        setServiceInfo(info);
        if (!AutoApps.enabled()) CaptureOverlay.close();
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString(); if (!AutoApps.accepts(pkg)) return;
        String page = event.getClassName() == null ? "" : event.getClassName().toString();
        ReferenceDiagnostics.event(pkg, page);
        AccessibilityNodeInfo source;
        try {
            source = ReferenceApplication.prefs().getBoolean("use_active_window", false) ? getRootInActiveWindow() : event.getSource();
        } catch (RuntimeException error) { ReferenceDiagnostics.result("SOURCE_UNAVAILABLE"); return; }
        if (source != null && source.getPackageName() != null && !pkg.equals(source.getPackageName().toString())) { recycle(source); ReferenceDiagnostics.result("WINDOW_CHANGED"); return; }
        long receivedAt=System.nanoTime();int eventType=event.getEventType();
        EventBacklog.Result queued=EventBacklog.enqueue(YimuKernel.WORKER,new EventBacklog.EventTask(pkg,eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
            @Override protected void process(){ReferenceCaptureService.this.process(pkg,page,source,(System.nanoTime()-receivedAt)/1_000_000,eventType);}
            @Override protected void closeResource(){recycle(source);}
        });
        ReferenceDiagnostics.queueChanged(queued.coalesced,queued.evicted);
        if(!queued.accepted){if(queued.rejection==EventBacklog.Rejection.QUEUE_FULL)ReferenceDiagnostics.skipped();else ReferenceDiagnostics.result("WORKER_STOPPED");}
    }
    private void process(String pkg, String page, AccessibilityNodeInfo source,long waited,int eventType) {
        try {
            if (current() != this || !AutoApps.accepts(pkg)) return;
            ReferenceDiagnostics.processingStarted(pkg,page,waited,eventType);
            long beforeRead = ReferenceDiagnostics.readCount(), beforeAttempt = ReferenceDiagnostics.attemptCount();
            YimuKernel.SOURCE.set(pkg);
            OriginalDispatch.accept(this, pkg, page, source);
            if (ReferenceDiagnostics.attemptCount() == beforeAttempt) ReferenceDiagnostics.result(
                source == null ? "SOURCE_UNAVAILABLE" : ReferenceDiagnostics.readCount() == beforeRead ? "PAGE_GATE_NOT_MATCHED" : ReferenceDiagnostics.snapshot().optInt("textCount")==0?"EMPTY_TREE":"NO_BILL_FROM_ORIGINAL_RULES");
        } catch (NodeReader.Aborted error) { ReferenceDiagnostics.failure("NODE_READ_"+error.reason.name(),error); }
        catch (Exception error) { ReferenceDiagnostics.failure("PROCESSING_FAILED",error); }
        finally { YimuKernel.SOURCE.remove(); ReferenceDiagnostics.processingFinished(); }
    }
    @SuppressWarnings("deprecation") private static void recycle(AccessibilityNodeInfo node) { if (node != null && Build.VERSION.SDK_INT < 33) try { node.recycle(); } catch (RuntimeException ignored) {} }
    public static JSONObject status() {
        ReferenceCaptureService service = current();
        ComponentName component = new ComponentName(ReferenceApplication.app(), ReferenceCaptureService.class);
        String raw = Settings.Secure.getString(ReferenceApplication.app().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        boolean granted = false;
        if (raw != null) for (String name : raw.split(":")) if (component.equals(ComponentName.unflattenFromString(name))) granted = true;
        AccessibilityServiceInfo info=null;try{if(service!=null)info=service.getServiceInfo();}catch(RuntimeException ignored){}
        return Json.object("enabled", AutoApps.enabled(), "permissionGranted", granted, "connected", service != null,
            "selectedPackages", new org.json.JSONArray(AutoApps.selected()), "toolIdentity",Build.VERSION.SDK_INT>=31&&info!=null&&info.isAccessibilityTool(), "flags", info == null ? 0 : info.flags,
            "eventTypes", info == null ? 0 : info.eventTypes, "effectivePackages", info == null || info.packageNames == null ? new org.json.JSONArray() : new org.json.JSONArray(java.util.Arrays.asList(info.packageNames)),
            "workerQueueSize",YimuKernel.WORKER.getQueue().size(),"workerActiveCount",YimuKernel.WORKER.getActiveCount(),
            "overlayEnabled",ReferenceApplication.prefs().getBoolean("show_overlay",true),"autoSave",ReferenceApplication.prefs().getBoolean("auto_save",false),"overlay",CaptureOverlay.status(),
            "diagnostics", ReferenceDiagnostics.snapshot());
    }
    @Override public void onInterrupt() { ReferenceDiagnostics.result("FEEDBACK_INTERRUPTED"); }
    @Override public boolean onUnbind(Intent intent) { disconnect(); return super.onUnbind(intent); }
    @Override public void onDestroy() { disconnect(); super.onDestroy(); }
    private void disconnect() { if (current() == this) { connected.clear(); CaptureOverlay.close(); ReferenceDiagnostics.result("DISCONNECTED"); } }
}
