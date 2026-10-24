package com.wangc.bill.reference.ui;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Opaque lock gate: cancel never finishes into an unverified ledger screen. */
public final class LocalUnlockActivity extends Activity {
    private static final int CONFIRM=6103;
    private boolean waiting;
    private boolean credentialWaiting;
    private boolean credentialSucceeded;
    private android.os.CancellationSignal biometric;
    private TextView status;
    private Button unlock;
    private Object back;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        LocalAppLock.shown();credentialWaiting=state!=null&&state.getBoolean("credentialWaiting");waiting=credentialWaiting;
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setGravity(Gravity.CENTER);
        page.setBackgroundColor(Color.WHITE);int padding=(int)(getResources().getDisplayMetrics().density*28);
        page.setPadding(padding,padding,padding,padding);
        TextView title=new TextView(this);title.setText("解锁记账本");title.setTextColor(Color.BLACK);title.setTextSize(25);title.setGravity(Gravity.CENTER);
        page.addView(title,new LinearLayout.LayoutParams(-1,-2));
        status=new TextView(this);status.setTextColor(Color.DKGRAY);status.setTextSize(16);status.setGravity(Gravity.CENTER);
        status.setPadding(0,padding,0,padding);page.addView(status,new LinearLayout.LayoutParams(-1,-2));
        unlock=new Button(this);unlock.setText("系统身份验证");unlock.setOnClickListener(view->confirm());page.addView(unlock,new LinearLayout.LayoutParams(-1,-2));
        Button settings=new Button(this);settings.setText("设备安全设置");settings.setOnClickListener(view->LocalAppLock.openSecurity(this));page.addView(settings,new LinearLayout.LayoutParams(-1,-2));
        Button exit=new Button(this);exit.setText("返回桌面");exit.setOnClickListener(view->leaveTask());page.addView(exit,new LinearLayout.LayoutParams(-1,-2));
        setContentView(page);
        if(Build.VERSION.SDK_INT>=33){android.window.OnBackInvokedCallback callback=this::leaveTask;back=callback;getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,callback);}
        render();if(state==null&&LocalAppLock.needsUnlock())page.post(this::confirm);
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);LocalAppLock.shown();render();}
    @Override protected void onResume(){super.onResume();LocalAppLock.shown();if(credentialSucceeded){credentialSucceeded=false;if(LocalAppLock.authenticated(this)){finish();return;}}if(!LocalAppLock.needsUnlock()){finish();return;}render();}
    private void render(){
        if(status==null)return;
        status.setText(!LocalAppLock.deviceSecure(this)?"请先在设备安全设置中配置 PIN、图案或密码。记账本会继续保持锁定。":waiting?"请完成系统身份验证。":"使用设备的系统身份验证后继续。取消验证会保持锁定。");
        unlock.setEnabled(!waiting&&LocalAppLock.deviceSecure(this));
    }
    private void confirm(){
        if(waiting||!LocalAppLock.needsUnlock())return;
        KeyguardManager manager=(KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if(manager==null||!manager.isDeviceSecure()){render();return;}
        // Modern Android owns biometric enrollment, fallback credentials and all secrets.
        if(Build.VERSION.SDK_INT>=30){try{if(showBiometric())return;}catch(RuntimeException unavailable){if(biometric!=null)biometric.cancel();biometric=null;waiting=false;}}
        confirmCredential(manager);
    }
    @android.annotation.TargetApi(30)
    private boolean showBiometric(){
        android.hardware.biometrics.BiometricManager manager=getSystemService(android.hardware.biometrics.BiometricManager.class);
        int authenticators=android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
                |android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
        if(manager==null||manager.canAuthenticate(authenticators)!=android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS)return false;
        android.hardware.biometrics.BiometricPrompt prompt=new android.hardware.biometrics.BiometricPrompt.Builder(this)
                .setTitle("解锁记账本").setSubtitle("验证设备身份后查看本地账本")
                .setAllowedAuthenticators(authenticators).build();
        waiting=true;render();biometric=new android.os.CancellationSignal();
        prompt.authenticate(biometric,getMainExecutor(),new android.hardware.biometrics.BiometricPrompt.AuthenticationCallback(){
            @Override public void onAuthenticationSucceeded(android.hardware.biometrics.BiometricPrompt.AuthenticationResult result){
                if(isDestroyed()||isFinishing())return;
                waiting=false;biometric=null;
                if(LocalAppLock.authenticated(LocalUnlockActivity.this))finish();else render();
            }
            @Override public void onAuthenticationError(int code,CharSequence message){
                if(isDestroyed()||isFinishing())return;
                waiting=false;biometric=null;render();status.setText("尚未解锁。可重新验证，或返回桌面。");
            }
        });
        return true;
    }
    private void confirmCredential(KeyguardManager manager){
        Intent intent=manager.createConfirmDeviceCredentialIntent("解锁记账本","验证设备身份后查看本地账本");
        if(intent==null){status.setText("系统身份验证暂不可用，请检查设备安全设置。");return;}
        try{credentialWaiting=true;waiting=true;render();startActivityForResult(intent,CONFIRM);}
        catch(ActivityNotFoundException unavailable){credentialWaiting=false;waiting=false;render();status.setText("找不到系统身份验证页面，请检查设备安全设置。");}
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);if(request!=CONFIRM)return;
        credentialWaiting=false;waiting=false;
        if(result==RESULT_OK){credentialSucceeded=true;return;}render();status.setText("尚未解锁。可重新验证，或返回桌面。");
    }
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putBoolean("credentialWaiting",credentialWaiting);}
    private void leaveTask(){moveTaskToBack(true);}
    @Override public void onBackPressed(){leaveTask();}
    @Override protected void onDestroy(){
        if(biometric!=null){biometric.cancel();biometric=null;}
        if(Build.VERSION.SDK_INT>=33&&back!=null)getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback((android.window.OnBackInvokedCallback)back);
        super.onDestroy();
    }
}
