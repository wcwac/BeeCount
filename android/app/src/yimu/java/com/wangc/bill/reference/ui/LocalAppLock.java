package com.wangc.bill.reference.ui;

import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.wangc.bill.reference.ReferenceApplication;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Foreground-session lock. The operating system owns all credential verification. */
public final class LocalAppLock implements Application.ActivityLifecycleCallbacks {
    private static final String ENABLED="local_app_lock";
    private static LocalAppLock instance;
    private final Set<Activity> started=Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Activity,Cover> covers=new WeakHashMap<>();
    private boolean verified;
    private boolean launchPending;

    private LocalAppLock(){}
    /** Register after the existing application callbacks, so this final guard also follows Privacy.apply. */
    public static void install(Application application){
        if(instance!=null)return;
        instance=new LocalAppLock();application.registerActivityLifecycleCallbacks(instance);
    }
    public static boolean enabled(){return ReferenceApplication.prefs().getBoolean(ENABLED,false);}
    public static boolean deviceSecure(Context context){
        KeyguardManager manager=(KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        return manager!=null&&manager.isDeviceSecure();
    }

    /** Bind the existing settings switch; no independent settings screen or password is stored. */
    public static void bind(Activity activity,View root){
        View control=Ui.view(root,"switch_lock");
        if(!(control instanceof CompoundButton button))return;
        button.setOnCheckedChangeListener(null);button.setEnabled(true);button.setChecked(enabled());
        button.setContentDescription("应用锁：返回后台后，使用系统身份验证解锁");
        button.setOnCheckedChangeListener((view,value)->{
            if(value&&!deviceSecure(activity)){
                view.setOnCheckedChangeListener(null);view.setChecked(false);bind(activity,root);
                new android.app.AlertDialog.Builder(activity).setTitle("请先设置系统锁屏")
                        .setMessage("应用锁使用设备的系统身份验证。请先设置设备 PIN、图案或密码，然后回来开启。")
                        .setNegativeButton("取消",null).setPositiveButton("安全设置",(dialog,which)->openSecurity(activity)).show();
                return;
            }
            if(!ReferenceApplication.prefs().edit().putBoolean(ENABLED,value).commit()){
                bind(activity,root);Ui.toast(activity,"未能保存应用锁设置，请重试");return;
            }
            LocalAppLock lock=instance;
            if(lock==null)return;
            lock.verified=false;
            if(value){lock.cover(activity);lock.launch(activity);}else lock.clearCovers();
        });
    }

    static void openSecurity(Activity activity){
        try{activity.startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));}
        catch(android.content.ActivityNotFoundException unavailable){
            activity.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        }
    }
    static boolean needsUnlock(){return enabled()&&(instance==null||!instance.verified);}
    static void shown(){if(instance!=null)instance.launchPending=false;}
    static boolean authenticated(Activity gate){
        if(instance==null||!enabled()||!instance.started.contains(gate)||gate.isFinishing()||gate.isDestroyed())return false;
        instance.verified=true;instance.clearCovers();return true;
    }

    private boolean dataActivity(Activity activity){
        return !(activity instanceof LocalUnlockActivity)
                &&!activity.getClass().getName().equals("com.wangc.bill.reference.ui.OriginalPermissionRequestActivity");
    }
    private void guard(Activity activity,boolean navigate){
        if(!enabled()){removeCover(activity);return;}
        if(!dataActivity(activity))return;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        if(!verified){cover(activity);if(navigate)launch(activity);}else removeCover(activity);
    }
    private void launch(Activity activity){
        if(launchPending||activity.isFinishing()||activity.isDestroyed()||activity instanceof LocalUnlockActivity)return;
        launchPending=true;
        try{activity.startActivity(new Intent(activity,LocalUnlockActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP));}
        catch(RuntimeException failure){launchPending=false;throw failure;}
    }
    private void cover(Activity activity){
        if(!dataActivity(activity)||activity.isDestroyed())return;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        Cover existing=covers.get(activity);
        if(existing!=null&&existing.view.getParent()!=null){existing.view.bringToFront();return;}
        View decor=activity.getWindow().getDecorView();if(!(decor instanceof ViewGroup parent))return;
        FrameLayout shield=new FrameLayout(activity);shield.setBackgroundColor(Color.WHITE);
        shield.setClickable(true);shield.setFocusable(true);shield.setFocusableInTouchMode(true);
        shield.setElevation(10000);shield.setContentDescription("应用已锁定");
        TextView title=new TextView(activity);title.setText("应用已锁定");title.setTextColor(Color.DKGRAY);
        title.setTextSize(20);title.setGravity(Gravity.CENTER);
        shield.addView(title,new FrameLayout.LayoutParams(-1,-1));
        View content=activity.findViewById(android.R.id.content);
        int accessibility=content==null?View.IMPORTANT_FOR_ACCESSIBILITY_AUTO:content.getImportantForAccessibility();
        if(content!=null)content.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        covers.put(activity,new Cover(shield,content,accessibility));
        parent.addView(shield,new ViewGroup.LayoutParams(-1,-1));shield.bringToFront();shield.requestFocus();
    }
    private void removeCover(Activity activity){
        Cover cover=covers.remove(activity);if(cover==null)return;
        if(cover.content!=null)cover.content.setImportantForAccessibility(cover.accessibility);
        if(cover.view.getParent() instanceof ViewGroup parent)parent.removeView(cover.view);
    }
    private void clearCovers(){
        for(Activity activity:new java.util.ArrayList<>(covers.keySet())){
            if(activity==null)continue;removeCover(activity);
            if(enabled())activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);else Privacy.apply(activity);
        }
    }
    @Override public void onActivityCreated(Activity activity,Bundle saved){guard(activity,false);}
    @Override public void onActivityStarted(Activity activity){started.add(activity);guard(activity,false);}
    @Override public void onActivityResumed(Activity activity){guard(activity,true);}
    @Override public void onActivityPaused(Activity activity){if(enabled())cover(activity);}
    @Override public void onActivityStopped(Activity activity){
        started.remove(activity);
        if(started.isEmpty()&&!activity.isChangingConfigurations())verified=false;
    }
    @Override public void onActivityDestroyed(Activity activity){started.remove(activity);removeCover(activity);}
    @Override public void onActivitySaveInstanceState(Activity activity,Bundle state){}
    private static final class Cover{
        final View view,content;final int accessibility;
        Cover(View view,View content,int accessibility){this.view=view;this.content=content;this.accessibility=accessibility;}
    }
}
