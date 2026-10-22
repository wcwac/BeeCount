package com.wangc.bill.reference.capture;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.wangc.bill.reference.ReferenceApplication;

public final class PlanScheduler {
    private PlanScheduler(){}
    public static void sync(){try{YimuKernel.WORKER.execute(PlanScheduler::runOnce);}catch(java.util.concurrent.RejectedExecutionException e){ReferenceDiagnostics.skipped();schedule();}}
    static void runOnce(){try{int generated=ReferenceApplication.store().processPlans(System.currentTimeMillis());if(generated>0)CaptureNotifications.update();}catch(Exception error){ReferenceDiagnostics.failure("PLAN_STORE_FAILED",error);}finally{schedule();}}
    static void schedule(){Context context=ReferenceApplication.app();AlarmManager alarms=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);PendingIntent pending=PendingIntent.getBroadcast(context,6302,new Intent(context,PlanReceiver.class).setAction("com.wangc.bill.reference.GENERATE_DUE"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);long next=ReferenceApplication.store().scalar("SELECT MIN(next_due) n FROM plans WHERE enabled=1");if(next<=0){alarms.cancel(pending);return;}alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,Math.max(System.currentTimeMillis()+15*60*1000L,next),pending);}
}
