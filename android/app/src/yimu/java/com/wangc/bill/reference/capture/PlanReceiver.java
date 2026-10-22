package com.wangc.bill.reference.capture;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public final class PlanReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent intent){if(intent==null)return;String action=intent.getAction();if(!Intent.ACTION_BOOT_COMPLETED.equals(action)&&!"com.wangc.bill.reference.GENERATE_DUE".equals(action))return;PendingResult pending=goAsync();try{YimuKernel.WORKER.execute(()->{try{PlanScheduler.runOnce();}finally{pending.finish();}});}catch(java.util.concurrent.RejectedExecutionException error){pending.finish();ReferenceDiagnostics.skipped();PlanScheduler.schedule();}}
}
