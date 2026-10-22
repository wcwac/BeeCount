package com.wangc.bill.reference.ui;
import android.app.Activity;
import android.view.WindowManager;
import com.wangc.bill.reference.ReferenceApplication;
public final class Privacy {
    private Privacy(){}
    public static void apply(Activity a){if(ReferenceApplication.prefs().getBoolean("privacy_screen",false))a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);else a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);}
}
