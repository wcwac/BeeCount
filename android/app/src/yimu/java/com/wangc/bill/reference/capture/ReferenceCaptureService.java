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
import android.view.accessibility.AccessibilityWindowInfo;
import com.wangc.bill.reference.Json;
import com.wangc.bill.reference.ReferenceApplication;
import com.wangc.bill.reference.OriginalSettings;
import com.wangc.bill.reference.ui.OriginalOppoCompatibilityWindow;
import com.wangc.bill.reference.ui.CaptureOverlay;
import com.wangc.bill.reference.ui.OverlayPermission;
import java.lang.ref.WeakReference;
import java.util.List;
import org.json.JSONObject;

public final class ReferenceCaptureService extends AccessibilityService {
    // Original g7.a registers in onCreate, before the accessibility connection,
    // and remains subscribed after onUnbind until onDestroy.
    private static volatile WeakReference<ReferenceCaptureService> created = new WeakReference<>(null);
    private static volatile WeakReference<ReferenceCaptureService> connected = new WeakReference<>(null);
    private final String captureSession=java.util.UUID.randomUUID().toString();
    private final Handler main = new Handler(Looper.getMainLooper());
    @Override public void onCreate() {
        super.onCreate(); created = new WeakReference<>(this);
        // Source checks only s.n here, independently of OEM/API and the autoBill master.
        if (ReferenceApplication.prefs().getBoolean(OriginalSettings.OPPO_FLOAT, OriginalSettings.DEFAULT_OPPO_FLOAT))
            OriginalOppoCompatibilityWindow.instance(ReferenceApplication.app()).show();
    }
    @Override protected void onServiceConnected() { connected = new WeakReference<>(this); applySettings(); ReferenceDiagnostics.result("CONNECTED"); }
    public static ReferenceCaptureService current() { return connected.get(); }
    public static void requestOriginalMasterOff() {
        ReferenceCaptureService service = created.get();
        OriginalServiceState.dispatchMasterOff(service, () -> created.get(), ReferenceCaptureService::disableSelf,
                task -> { if (Looper.myLooper() == Looper.getMainLooper()) task.run(); else if (service != null) service.main.post(task); });
    }
    public static void refresh() { ReferenceCaptureService s = current(); if (s != null) { if (Looper.myLooper() == Looper.getMainLooper()) s.applySettings(); else s.main.post(s::applySettings); } }
    private void applySettings() {
        AccessibilityServiceInfo info = getServiceInfo(); if (info == null) return;
        info.packageNames = AutoApps.selected().toArray(new String[0]);
        info.eventTypes = AutoApps.enabled() && info.packageNames.length > 0 ? AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED : 0;
        // Yimu 6.3.0 xml/accessibility_service_config.xml: the original keeps flags 75
        // when onServiceConnected changes packageNames. UI inspection flags belong to the observer.
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            | AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
            | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS | AccessibilityServiceInfo.DEFAULT;
        info.notificationTimeout = 10;
        setServiceInfo(info);
        if (!AutoApps.enabled()) CaptureOverlay.close();
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString(); if (!AutoApps.accepts(pkg)) return;
        String page = event.getClassName() == null ? "" : event.getClassName().toString();
        ReferenceDiagnostics.event(pkg, page);
        // An event can carry package/class information while its virtual source is unavailable.
        // Capture the owned source now; the framework owns the event after this callback returns.
        long receivedAt=System.nanoTime();
        AccessibilityNodeInfo source=null;
        RuntimeException failure=null;
        try{source=event.getSource();}catch(RuntimeException error){failure=error;}
        ReferenceDiagnostics.eventSource(source!=null,failure);
        EventBacklog.Result queued=EventBacklog.enqueue(YimuKernel.WORKER,
            new SourceEvent(pkg,page,event.getEventType(),event.getWindowId(),receivedAt,source));
        ReferenceDiagnostics.queueChanged(queued.coalesced,queued.evicted);
        if(!queued.accepted){if(queued.rejection==EventBacklog.Rejection.QUEUE_FULL)ReferenceDiagnostics.skipped();else ReferenceDiagnostics.result("WORKER_STOPPED");}
    }
    private final class SourceEvent extends EventBacklog.EventTask {
        private final String page;
        private final int eventType,windowId;
        private final long receivedAt;
        private AccessibilityNodeInfo owned;
        SourceEvent(String pkg,String page,int eventType,int windowId,long receivedAt,AccessibilityNodeInfo source){
            super(pkg,eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
            this.page=page;this.eventType=eventType;this.windowId=windowId;this.receivedAt=receivedAt;owned=source;
        }
        @Override protected void process(){
            if(current()!=ReferenceCaptureService.this||!AutoApps.accepts(packageName))return;
            long waited=(System.nanoTime()-receivedAt)/1_000_000;
            ReferenceDiagnostics.processingStarted(packageName,page,waited,eventType);
            try{
                ReferenceDiagnostics.resolvingSource();
                AccessibilityNodeInfo initial=owned;owned=null; // Resolver consumes this reference.
                EventSourceResolver.Result<AccessibilityNodeInfo> resolved=EventSourceResolver.resolvePage(
                    initial,packageName,windowId,waited,windows,()->System.nanoTime()/1_000_000);
                owned=resolved.node;
                ReferenceDiagnostics.sourceResolved(resolved.route.name(),resolved.reason.name(),resolved.lookups,
                    resolved.candidateWindows,resolved.errors,resolved.lastErrorType,windowId);
                if(current()!=ReferenceCaptureService.this||!AutoApps.accepts(packageName))return;
                long beforeRead=ReferenceDiagnostics.readCount(),beforeAttempt=ReferenceDiagnostics.attemptCount();
                YimuKernel.SOURCE.set(packageName);
                CaptureEvidence.begin(packageName,page,windowId,eventType,resolved.route.name(),captureSession+":"+YimuKernel.observationEpoch());
                // Keep event class and null-source state transitions used by the recovered handlers.
                OriginalDispatch.accept(ReferenceCaptureService.this,packageName,page,owned);
                if(ReferenceDiagnostics.attemptCount()==beforeAttempt)ReferenceDiagnostics.result(
                    owned==null?sourceFailure(resolved.reason):ReferenceDiagnostics.readCount()==beforeRead?
                    "PAGE_GATE_NOT_MATCHED":ReferenceDiagnostics.snapshot().optInt("textCount")==0?
                    "EMPTY_TREE":"NO_BILL_FROM_ORIGINAL_RULES");
            }catch(NodeReader.Aborted error){ReferenceDiagnostics.failure("NODE_READ_"+error.reason.name(),error);}
            catch(Exception error){ReferenceDiagnostics.failure("PROCESSING_FAILED",error);}
            finally{CaptureEvidence.clear();YimuKernel.SOURCE.remove();ReferenceDiagnostics.processingFinished();}
        }
        @Override protected void closeResource(){AccessibilityNodeInfo node=owned;owned=null;recycle(node);}
    }
    private static String sourceFailure(EventSourceResolver.Reason reason){return switch(reason){
        case EXPIRED->"SOURCE_EXPIRED";case DEADLINE->"SOURCE_LOOKUP_TIMEOUT";
        case PACKAGE_MISMATCH,WINDOW_MISMATCH->"SOURCE_WINDOW_MISMATCH";
        case LOOKUP_FAILED->"SOURCE_LOOKUP_FAILED";default->"SOURCE_UNAVAILABLE";
    };}
    private final EventSourceResolver.Provider<AccessibilityNodeInfo,AccessibilityWindowInfo> windows=new EventSourceResolver.Provider<>(){
        public AccessibilityNodeInfo activeRoot(){return getRootInActiveWindow();}
        public List<AccessibilityWindowInfo> windows(){return getWindows();}
        public boolean isApplication(AccessibilityWindowInfo window){return window.getType()==AccessibilityWindowInfo.TYPE_APPLICATION;}
        public int windowId(AccessibilityWindowInfo window){return window.getId();}
        public boolean isActive(AccessibilityWindowInfo window){return window.isActive();}
        public boolean isFocused(AccessibilityWindowInfo window){return window.isFocused();}
        public AccessibilityNodeInfo windowRoot(AccessibilityWindowInfo window){return window.getRoot();}
        public String packageName(AccessibilityNodeInfo node){CharSequence value=node.getPackageName();return value==null?null:value.toString();}
        public int nodeWindowId(AccessibilityNodeInfo node){return node.getWindowId();}
        public void releaseNode(AccessibilityNodeInfo node){recycle(node);}
        @SuppressWarnings("deprecation") public void releaseWindow(AccessibilityWindowInfo window){if(Build.VERSION.SDK_INT<33)window.recycle();}
    };
    @SuppressWarnings("deprecation") private static void recycle(AccessibilityNodeInfo node) { if (node != null && Build.VERSION.SDK_INT < 33) try { node.recycle(); } catch (RuntimeException ignored) {} }
    public static JSONObject status() {
        ReferenceCaptureService service = current();
        ComponentName component = new ComponentName(ReferenceApplication.app(), ReferenceCaptureService.class);
        String raw = Settings.Secure.getString(ReferenceApplication.app().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        boolean granted = false;
        if (raw != null) for (String name : raw.split(":")) if (component.equals(ComponentName.unflattenFromString(name))) granted = true;
        AccessibilityServiceInfo info=null;try{if(service!=null)info=service.getServiceInfo();}catch(RuntimeException ignored){}
        return Json.object("enabled", AutoApps.enabled(), "permissionGranted", granted, "connected", service != null,
            "running", OriginalServiceState.isRunning(ReferenceApplication.app()),
            "selectedPackages", new org.json.JSONArray(AutoApps.selected()), "toolIdentity",Build.VERSION.SDK_INT>=31&&info!=null&&info.isAccessibilityTool(), "flags", info == null ? 0 : info.flags,
            "capabilities",info==null?0:info.getCapabilities(),"canRetrieveWindowContent",info!=null&&(info.getCapabilities()&AccessibilityServiceInfo.CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT)!=0,
            "eventTypes", info == null ? 0 : info.eventTypes, "effectivePackages", info == null || info.packageNames == null ? new org.json.JSONArray() : new org.json.JSONArray(java.util.Arrays.asList(info.packageNames)),
            "callbackRegistrations",YimuKernel.registeredCallbackCount(),"workerQueueSize",YimuKernel.WORKER.getQueue().size(),"workerActiveCount",YimuKernel.WORKER.getActiveCount(),
            "overlayEnabled",ReferenceApplication.prefs().getBoolean("show_overlay",true),"autoSave",ReferenceApplication.prefs().getBoolean("auto_save",false),"overlay",CaptureOverlay.status(),
            "sourcePolicy","COMPLETE_SAME_WINDOW_FIRST", "overlayPermissionModel","SYSTEM_ALERT_WINDOW", "overlayPermissionGranted",OverlayPermission.granted(),
            "diagnostics", ReferenceDiagnostics.snapshot());
    }
    @Override public void onInterrupt() { ReferenceDiagnostics.result("FEEDBACK_INTERRUPTED"); }
    @Override public boolean onUnbind(Intent intent) { disconnect(); return super.onUnbind(intent); }
    @Override public void onDestroy() {
        if (created.get() == this) created.clear(); disconnect(); super.onDestroy();
        // Preserve the source quirk: a stored false option skips removal even if a prior toggle showed the window.
        if (ReferenceApplication.prefs().getBoolean(OriginalSettings.OPPO_FLOAT, OriginalSettings.DEFAULT_OPPO_FLOAT))
            OriginalOppoCompatibilityWindow.instance(ReferenceApplication.app()).remove();
    }
    private void disconnect() { if (current() == this) { connected.clear(); CaptureOverlay.close(); ReferenceDiagnostics.result("DISCONNECTED"); } }
}
