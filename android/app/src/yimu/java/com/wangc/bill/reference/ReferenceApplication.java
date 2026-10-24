package com.wangc.bill.reference;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public final class ReferenceApplication extends Application {
    private static ReferenceApplication instance;
    private ReferenceStore store;
    @Override public void onCreate() {
        super.onCreate();
        instance = this;
        store = new ReferenceStore(this);
        store.getWritableDatabase();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks(){
            public void onActivityCreated(android.app.Activity a,android.os.Bundle b){}
            public void onActivityStarted(android.app.Activity a){}
            public void onActivityResumed(android.app.Activity a){com.wangc.bill.reference.ui.Privacy.apply(a);com.wangc.bill.reference.ui.QuickEntryWindow.onActivityResumed(a);android.app.ActivityManager manager=(android.app.ActivityManager)getSystemService(ACTIVITY_SERVICE);if(manager!=null)for(android.app.ActivityManager.AppTask task:manager.getAppTasks())if(task.getTaskInfo().id==a.getTaskId())task.setExcludeFromRecents(prefs().getBoolean("remove_recent",false));}
            public void onActivityPaused(android.app.Activity a){}
            public void onActivityStopped(android.app.Activity a){}
            public void onActivitySaveInstanceState(android.app.Activity a,android.os.Bundle b){}
            public void onActivityDestroyed(android.app.Activity a){}
        });
        com.wangc.bill.reference.ui.LocalAppLock.install(this);
        com.wangc.bill.reference.capture.PlanScheduler.sync();
        com.wangc.bill.reference.ui.QuickEntryWindow.refresh();
    }
    @Override public void onConfigurationChanged(android.content.res.Configuration configuration) {
        super.onConfigurationChanged(configuration);
        com.wangc.bill.reference.ui.QuickEntryWindow.refresh();
    }
    public static ReferenceApplication app() {
        if (instance == null) throw new IllegalStateException("应用尚未初始化");
        return instance;
    }
    public static ReferenceStore store() { return app().store; }
    public static boolean isProductionStore(ReferenceStore store){return instance!=null&&instance.store==store;}
    public static SharedPreferences prefs() { return app().getSharedPreferences("reference_settings", Context.MODE_PRIVATE); }
    public static void selectCurrentBook(long rowId){
        long previous=prefs().getLong("current_book",1);
        prefs().edit().putLong("current_book",rowId).apply();
        if(previous!=rowId)OriginalFinancialEffects.bookSelectionChanged(store());
    }
}
