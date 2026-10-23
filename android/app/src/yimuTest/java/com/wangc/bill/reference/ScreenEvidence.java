package com.wangc.bill.reference;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Application;
import android.app.UiAutomation;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeoutException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.runner.Description;

/** Read-only UI evidence; screenshots do not establish original-APK or behavioral equivalence. */
final class ScreenEvidence {
    private static final String TAG="ReferenceScreenEvidence";
    private static final int MAX_IMAGES=64,MAX_LONG_EDGE=1600,MAX_IMAGE_BYTES=6*1024*1024;
    private static final int MAX_TREE_NODES=1500,MAX_DEPTH=40,MAX_CHILDREN=200,MAX_TEXT=300,MAX_TREE_BYTES=1024*1024;
    private static final long MAX_TOTAL_BYTES=128L*1024*1024,TREE_BUDGET_MS=150,SETTLE_TIMEOUT_MS=3000;
    private static final Object FILE_LOCK=new Object();
    private static File runDirectory;
    private static long writtenBytes;
    private static int imageCount,sequence;

    private ScreenEvidence() {}

    static Session open(Description description)throws Exception {
        Session session=new Session(description);
        try { session.start();return session; }
        catch(Exception|Error error) { session.close();throw error; }
    }

    static final class Session {
        private final Description test;
        private final Application application=(Application)InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        private UiAutomation automation;
        private String[] previousPackages;
        private boolean scopeSaved,registered,closed;
        private volatile Activity resumed;
        private String lastCheckpoint,outcome="UNKNOWN";
        private final Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks() {
            public void onActivityCreated(Activity activity,Bundle state) {}
            public void onActivityStarted(Activity activity) {}
            public void onActivityResumed(Activity activity) { resumed=activity; }
            public void onActivityPaused(Activity activity) { if(resumed==activity)resumed=null; }
            public void onActivityStopped(Activity activity) {}
            public void onActivitySaveInstanceState(Activity activity,Bundle state) {}
            public void onActivityDestroyed(Activity activity) { if(resumed==activity)resumed=null; }
        };

        Session(Description description) { test=description; }

        void start()throws Exception {
            if(!"true".equals(InstrumentationRegistry.getArguments().getString("reference_cloud_only"))
                ||Build.VERSION.SDK_INT!=36||!(Build.HARDWARE.equals("ranchu")||Build.HARDWARE.equals("goldfish"))
                ||!Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"))
                throw new IllegalStateException("Screen evidence requires the opted-in disposable API36 x86_64 emulator");
            automation=InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
            AccessibilityServiceInfo observer=automation.getServiceInfo();previousPackages=observer.packageNames==null?null:observer.packageNames.clone();scopeSaved=true;
            // Take screenshots without suppressing the application service or subscribing
            // the observer to payment-app events ahead of its FLAG_DEFAULT listener.
            observer.packageNames=new String[]{application.getPackageName()};automation.setServiceInfo(observer);
            application.registerActivityLifecycleCallbacks(lifecycle);registered=true;
            synchronized(FILE_LOCK) {
                if(runDirectory==null) {
                    File external=application.getExternalFilesDir("test-evidence");
                    if(external==null)throw new IOException("Cloud test evidence storage is unavailable");
                    runDirectory=new File(external,"run-"+System.currentTimeMillis()+"-"+SystemClock.elapsedRealtime());
                    if(!runDirectory.mkdirs())throw new IOException("Cannot create cloud evidence directory");
                    JSONObject info=Json.object("schema_version",1,"package",application.getPackageName(),"version_name",com.wangc.bill.BuildConfig.VERSION_NAME,
                        "version_code",com.wangc.bill.BuildConfig.VERSION_CODE,"sdk",Build.VERSION.SDK_INT,"hardware",Build.HARDWARE,"abis",new JSONArray(Arrays.asList(Build.SUPPORTED_ABIS)),
                        "synthetic_cloud_data_only",true,"original_apk_executed",false,"pixel_parity_verified",false,
                        "scope","Attached current entry screens only; screenshots supplement independent semantic assertions.",
                        "uncovered_states",uncoveredStates(),
                        "max_images",MAX_IMAGES,"max_total_bytes",MAX_TOTAL_BYTES,"max_image_long_edge",MAX_LONG_EDGE,
                        "max_tree_nodes",MAX_TREE_NODES,"max_tree_depth",MAX_DEPTH,"tree_budget_ms",TREE_BUDGET_MS);
                    writeJson(new File(runDirectory,"run-info.json"),info,MAX_TREE_BYTES);
                }
                append(Json.object("event","test_started","test",testName(),"time_ms",System.currentTimeMillis()));
            }
        }

        <A extends Activity> void checkpoint(ActivityScenario<A> scenario,String scene)throws Exception {
            long started=SystemClock.elapsedRealtime();Snapshot snapshot=null;int previous=0;boolean hasPrevious=false,stable=false;
            while(SystemClock.elapsedRealtime()-started<SETTLE_TIMEOUT_MS) {
                final Snapshot[] current={null};scenario.onActivity(activity->current[0]=snapshot(activity));snapshot=current[0];
                if(snapshot.attached&&snapshot.focused&&snapshot.width>0&&snapshot.height>0&&!snapshot.layoutRequested) {
                    if(hasPrevious&&previous==snapshot.signature) { stable=true;break; }
                    previous=snapshot.signature;hasPrevious=true;
                } else hasPrevious=false;
                SystemClock.sleep(80);
            }
            if(!stable)throw new IOException("The current Activity did not reach a stable attached checkpoint: "+scene);
            try { automation.waitForIdle(80,1000); } catch(TimeoutException ignored) { /* The captured tree's stability is recorded separately. */ }
            final Snapshot[] latest={null};scenario.onActivity(activity->latest[0]=snapshot(activity));
            if(!latest[0].attached||!latest[0].focused||latest[0].layoutRequested||latest[0].signature!=snapshot.signature)
                throw new IOException("The actual screen changed before its screenshot checkpoint: "+scene);
            snapshot=latest[0];
            capture(scene,snapshot,"CHECKPOINT","ENTRY_RENDER_ONLY");
        }

        void unshownTemplates(String[] names)throws Exception {
            synchronized(FILE_LOCK) {
                append(Json.object("event","coverage_not_displayed","test",testName(),"time_ms",System.currentTimeMillis(),
                    "templates",new JSONArray(Arrays.asList(names)),"status","INFLATED_ONLY",
                    "reason","These templates were inflated without being attached; no screenshot is attributed to them."));
            }
        }

        void failed(Throwable error) {
            outcome="FAILED";
            JSONObject failure=Json.object("event","test_failed","test",testName(),"time_ms",System.currentTimeMillis(),
                "exception",error.getClass().getName(),"message",bounded(error.getMessage()),"last_checkpoint",lastCheckpoint,
                "failure_capture","BEST_EFFORT");
            try {
                Activity activity=resumed;
                if(activity!=null&&!activity.isDestroyed()) {
                    final Snapshot[] current={null};InstrumentationRegistry.getInstrumentation().runOnMainSync(()->current[0]=snapshot(activity));
                    if(current[0].attached&&current[0].focused)capture("failure/current",current[0],"FAILURE","CURRENT_FAILURE_SURFACE_ONLY");
                    else Json.put(failure,"screenshot_unavailable","NO_FOCUSED_ATTACHED_TARGET_ACTIVITY");
                } else Json.put(failure,"screenshot_unavailable","NO_RESUMED_TARGET_ACTIVITY; retain the last checkpoint evidence");
            } catch(Exception|Error captureError) {
                Json.put(failure,"failure_capture_error",captureError.getClass().getName());
                Log.w(TAG,"Best-effort failure screenshot unavailable: "+captureError.getClass().getName());
            }
            // Losing the optional failure screenshot must not also lose the failure
            // marker or its pointer to the last successfully captured checkpoint.
            try { synchronized(FILE_LOCK) { append(failure); } }
            catch(Exception|Error indexError) { Log.w(TAG,"Best-effort failure index unavailable: "+indexError.getClass().getName()); }
        }

        private void capture(String scene,Snapshot snapshot,String event,String coverage)throws Exception {
            synchronized(FILE_LOCK) {
                if(closed||runDirectory==null)throw new IOException("Evidence session is not available");
                if(imageCount>=MAX_IMAGES)throw new IOException("The shared cloud screenshot budget is exhausted");
                String stem=String.format(Locale.ROOT,"%03d-%s-%s",++sequence,test.getMethodName(),scene).replaceAll("[^A-Za-z0-9_.-]","_");
                File tree=new File(runDirectory,stem+".tree.json"),png=new File(runDirectory,stem+".png"),metadata=new File(runDirectory,stem+".json");
                JSONObject meta=Json.object("schema_version",1,"event",event,"test",testName(),"scene",scene,"coverage",coverage,
                    "time_ms",System.currentTimeMillis(),"elapsed_ms",SystemClock.elapsedRealtime(),"activity",snapshot.activity,
                    "attached",snapshot.attached,"focused",snapshot.focused,"tree_nodes",snapshot.nodes,"tree_truncated",snapshot.truncated,
                    "stability",event.equals("CHECKPOINT")?"Two consecutive bounded control-tree samples; no pixel-difference claim.":"Single best-effort failure snapshot; no stability claim.",
                    "locale",Locale.getDefault().toLanguageTag(),"font_scale",application.getResources().getConfiguration().fontScale,
                    "density_dpi",application.getResources().getDisplayMetrics().densityDpi,"synthetic_cloud_data_only",true,
                    "original_apk_executed",false,"pixel_parity_verified",false,"uncovered_states",uncoveredStates(),"tree",tree.getName(),"screenshot",png.getName());
                writeJson(tree,snapshot.tree,MAX_TREE_BYTES);
                Bitmap original=null,scaled=null;
                try {
                    original=automation.takeScreenshot();if(original==null)throw new IOException("UiAutomation did not return a real screenshot");
                    Json.put(meta,"original_width",original.getWidth());Json.put(meta,"original_height",original.getHeight());
                    int longest=Math.max(original.getWidth(),original.getHeight());scaled=original;
                    if(longest>MAX_LONG_EDGE)scaled=Bitmap.createScaledBitmap(original,Math.max(1,original.getWidth()*MAX_LONG_EDGE/longest),Math.max(1,original.getHeight()*MAX_LONG_EDGE/longest),true);
                    try(OutputStream stream=new LimitedOutput(new FileOutputStream(png),Math.min(MAX_IMAGE_BYTES,MAX_TOTAL_BYTES-writtenBytes))) {
                        if(!scaled.compress(Bitmap.CompressFormat.PNG,100,stream))throw new IOException("Cannot encode cloud screenshot");
                    }
                    writtenBytes+=png.length();imageCount++;
                    Json.put(meta,"width",scaled.getWidth());Json.put(meta,"height",scaled.getHeight());Json.put(meta,"status","CAPTURED");
                    writeJson(metadata,meta,MAX_TREE_BYTES);append(meta);lastCheckpoint=metadata.getName();
                    Log.i(TAG,"Captured "+testName()+" scene="+scene+" artifact="+metadata.getName());
                } catch(Exception|Error error) {
                    if(png.exists()&& !"CAPTURED".equals(meta.optString("status")))png.delete();
                    Json.put(meta,"status","EVIDENCE_FAILED");Json.put(meta,"exception",error.getClass().getName());
                    try { writeJson(metadata,meta,MAX_TREE_BYTES);append(meta); } catch(Exception ignored) {}
                    throw error;
                } finally {
                    if(scaled!=null&&scaled!=original)scaled.recycle();if(original!=null)original.recycle();
                }
            }
        }

        String testName() { return test.getClassName()+"."+test.getMethodName(); }
        void succeeded() { outcome="PASSED"; }

        void close() {
            if(closed)return;closed=true;
            try { if(runDirectory!=null)synchronized(FILE_LOCK) { append(Json.object("event","test_finished","test",testName(),"outcome",outcome,"time_ms",System.currentTimeMillis(),"last_checkpoint",lastCheckpoint,"images_so_far",imageCount,"bytes_so_far",writtenBytes)); } }
            catch(Exception error) {
                if(outcome.equals("PASSED"))throw new IllegalStateException("Cannot finalize successful screen evidence",error);
                Log.w(TAG,"Cannot record evidence completion: "+error.getClass().getName());
            }
            finally {
                if(registered)application.unregisterActivityLifecycleCallbacks(lifecycle);resumed=null;
                if(scopeSaved) { AccessibilityServiceInfo observer=automation.getServiceInfo();observer.packageNames=previousPackages;automation.setServiceInfo(observer); }
            }
        }
    }

    private static Snapshot snapshot(Activity activity) {
        View root=activity.getWindow().getDecorView();JSONArray nodes=new JSONArray();ArrayDeque<Node> pending=new ArrayDeque<>();pending.add(new Node(root,-1,0));
        boolean truncated=false;long started=SystemClock.elapsedRealtime();
        while(!pending.isEmpty()) {
            if(nodes.length()>=MAX_TREE_NODES||SystemClock.elapsedRealtime()-started>TREE_BUDGET_MS) { truncated=true;break; }
            Node next=pending.removeFirst();View view=next.view;Rect bounds=new Rect();view.getGlobalVisibleRect(bounds);String resource="";
            if(view.getId()!=View.NO_ID)try { resource=view.getResources().getResourceName(view.getId()); } catch(android.content.res.Resources.NotFoundException ignored) {}
            JSONObject item=Json.object("index",nodes.length(),"parent",next.parent,"depth",next.depth,"class",view.getClass().getName(),"resource",resource,
                "visibility",view.getVisibility(),"shown",view.isShown(),"enabled",view.isEnabled(),"clickable",view.isClickable(),"focused",view.isFocused(),
                "bounds",new JSONArray(Arrays.asList(bounds.left,bounds.top,bounds.right,bounds.bottom)),"content_description",bounded(view.getContentDescription()));
            if(view instanceof TextView text) { Json.put(item,"text",bounded(text.getText()));Json.put(item,"text_truncated",text.length()>MAX_TEXT); }
            if(view instanceof CompoundButton button)Json.put(item,"checked",button.isChecked());
            int parent=nodes.length();nodes.put(item);
            if(view instanceof ViewGroup group) {
                if(next.depth>=MAX_DEPTH) { if(group.getChildCount()>0)truncated=true;continue; }
                int children=Math.min(Math.min(MAX_CHILDREN,group.getChildCount()),Math.max(0,MAX_TREE_NODES-nodes.length()-pending.size()));if(children<group.getChildCount())truncated=true;
                for(int i=0;i<children;i++)pending.addLast(new Node(group.getChildAt(i),parent,next.depth+1));
            }
        }
        JSONObject tree=Json.object("schema_version",1,"tree_kind","ACTUAL_ANDROID_VIEW_HIERARCHY","activity",activity.getClass().getName(),
            "node_limit",MAX_TREE_NODES,"depth_limit",MAX_DEPTH,"text_limit",MAX_TEXT,"time_budget_ms",TREE_BUDGET_MS,
            "visited",nodes.length(),"truncated",truncated,"nodes",nodes);
        return new Snapshot(activity.getClass().getName(),root.isAttachedToWindow(),activity.hasWindowFocus(),root.isLayoutRequested(),root.getWidth(),root.getHeight(),nodes.length(),truncated,nodes.toString().hashCode(),tree);
    }

    private static String bounded(CharSequence value) { if(value==null)return "";return value.subSequence(0,Math.min(MAX_TEXT,value.length())).toString(); }
    private static JSONArray uncoveredStates() { return new JSONArray(Arrays.asList("Unopened dialogs and nested routes","Input validation, save/cancel outcomes and process restart","Pixels outside the current viewport","Original APK pixel or behavioral equivalence")); }
    private static void writeJson(File file,JSONObject value,int maximum)throws IOException {
        byte[] bytes=value.toString().getBytes(StandardCharsets.UTF_8);if(bytes.length>maximum||writtenBytes+bytes.length>MAX_TOTAL_BYTES)throw new IOException("Cloud evidence JSON exceeds its size budget");
        try(FileOutputStream stream=new FileOutputStream(file)) { stream.write(bytes); }writtenBytes+=bytes.length;
    }
    private static void append(JSONObject value)throws IOException {
        byte[] bytes=(value.toString()+"\n").getBytes(StandardCharsets.UTF_8);if(bytes.length>MAX_TREE_BYTES||writtenBytes+bytes.length>MAX_TOTAL_BYTES)throw new IOException("Cloud evidence index exceeds its size budget");
        try(FileOutputStream stream=new FileOutputStream(new File(runDirectory,"index.jsonl"),true)) { stream.write(bytes); }writtenBytes+=bytes.length;
    }
    private record Node(View view,int parent,int depth) {}
    private record Snapshot(String activity,boolean attached,boolean focused,boolean layoutRequested,int width,int height,int nodes,boolean truncated,int signature,JSONObject tree) {}
    private static final class LimitedOutput extends OutputStream {
        private final OutputStream output;private final long maximum;private long written;
        LimitedOutput(OutputStream output,long maximum) { this.output=output;this.maximum=maximum; }
        @Override public void write(int value)throws IOException { if(++written>maximum)throw new IOException("Cloud screenshot exceeds its byte budget");output.write(value); }
        @Override public void write(byte[] values,int offset,int count)throws IOException { if(count>maximum-written)throw new IOException("Cloud screenshot exceeds its byte budget");output.write(values,offset,count);written+=count; }
        @Override public void close()throws IOException { output.close(); }
    }
}
