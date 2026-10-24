package com.wangc.bill.reference;

import android.app.job.JobParameters;
import android.app.job.JobService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/** A cancellable, private JobScheduler worker; no foreground or additional permission. */
public final class LocalBackupJobService extends JobService {
    private Future<?> running;
    @Override public boolean onStartJob(JobParameters parameters) {
        if (parameters.getJobId()!=LocalBackupScheduler.JOB_ID || !ReferenceApplication.prefs().getBoolean(LocalBackupScheduler.ENABLED,false)) return false;
        FutureTask<Void> task = new FutureTask<>(()->{
            try { LocalBackupScheduler.backupNow(this); } catch (Exception ignored) { /* State is visible in backup settings. */ }
            if (!Thread.currentThread().isInterrupted()) jobFinished(parameters,false);
            return null;
        });
        running=task;new Thread(task,"reference-auto-backup").start();return true;
    }
    @Override public boolean onStopJob(JobParameters parameters) { if(running!=null)running.cancel(true);return true; }
    @Override public void onDestroy() { if(running!=null)running.cancel(true);super.onDestroy(); }
}
