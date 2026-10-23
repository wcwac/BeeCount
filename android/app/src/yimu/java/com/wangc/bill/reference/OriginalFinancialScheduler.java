package com.wangc.bill.reference;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** j2.p's four-thread delay pool is separate from j2.q's serial credit worker and MAIN. */
public final class OriginalFinancialScheduler implements AutoCloseable {
    private ScheduledExecutorService delayed;
    private ExecutorService credit;
    private final Handler main;
    private volatile long epoch;
    private volatile boolean disposed;
    public OriginalFinancialScheduler(){this(Executors.newScheduledThreadPool(4),null,new Handler(Looper.getMainLooper()));}
    OriginalFinancialScheduler(ScheduledExecutorService delayed,ExecutorService credit,Handler main){this.delayed=delayed;this.credit=credit;this.main=main;}
    /** s2.k -> j2.e; replace the old dedicated credit executor. */
    public synchronized void initializeCredit(){if(disposed)return;if(credit!=null)credit.shutdownNow();credit=Executors.newSingleThreadExecutor();}
    public void schedule(Runnable task,long millis){
        ScheduledExecutorService executor;long stamp;
        synchronized(this){if(disposed)return;executor=delayed;stamp=epoch;}
        // The original null-executor branch drops work; synchronous rejection propagates.
        if(executor!=null)executor.schedule(guard(task,stamp),millis,TimeUnit.MILLISECONDS);
    }
    public void executeCredit(Runnable task){
        ExecutorService executor;long stamp;
        synchronized(this){if(disposed)return;executor=credit;stamp=epoch;}
        if(executor!=null)executor.execute(guard(task,stamp));
    }
    public void executeMain(Runnable task){
        if(disposed)return;long stamp=epoch;Runnable guarded=guard(task,stamp);
        // Host lock-safety extension: subscribers are always posted, never run under a save lock.
        if(main!=null)main.post(guarded);
    }
    public long epoch(){return epoch;}
    private Runnable guard(Runnable task,long stamp){if(task==null)throw new IllegalArgumentException("Task is required");return ()->{if(!disposed&&epoch==stamp)task.run();};}
    /** Host safety extension: old delayed/queued callbacks cannot operate on a restored DB. */
    public synchronized void resetAfterRestore(){
        if(disposed)return;epoch++;if(delayed!=null)delayed.shutdownNow();if(credit!=null)credit.shutdownNow();if(main!=null)main.removeCallbacksAndMessages(null);
        delayed=Executors.newScheduledThreadPool(4);credit=null;
    }
    @Override public synchronized void close(){
        if(disposed)return;disposed=true;epoch++;if(delayed!=null)delayed.shutdownNow();if(credit!=null)credit.shutdownNow();if(main!=null)main.removeCallbacksAndMessages(null);
    }
}
