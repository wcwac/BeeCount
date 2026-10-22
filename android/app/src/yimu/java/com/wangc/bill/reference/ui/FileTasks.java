package com.wangc.bill.reference.ui;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import java.util.concurrent.*;

public final class FileTasks {
    public interface Result<T>{void success(T value);}
    private static final ThreadPoolExecutor IO=new ThreadPoolExecutor(1,1,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(3),r->new Thread(r,"reference-files"),new ThreadPoolExecutor.AbortPolicy());
    private FileTasks(){}
    public static <T> void run(Activity activity,String title,Callable<T> task,Result<T> result){
        AlertDialog wait=new AlertDialog.Builder(activity).setTitle(title).setView(new ProgressBar(activity)).setCancelable(false).create();wait.show();
        android.app.Application.ActivityLifecycleCallbacks lifecycle=new android.app.Application.ActivityLifecycleCallbacks(){
            public void onActivityCreated(Activity a,android.os.Bundle b){}public void onActivityStarted(Activity a){}public void onActivityResumed(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,android.os.Bundle b){}
            public void onActivityDestroyed(Activity a){if(a==activity){wait.dismiss();activity.getApplication().unregisterActivityLifecycleCallbacks(this);}}
        };activity.getApplication().registerActivityLifecycleCallbacks(lifecycle);
        try{IO.execute(()->{T value=null;Exception error=null;try{value=task.call();}catch(Exception e){error=e;}T done=value;Exception failed=error;new Handler(Looper.getMainLooper()).post(()->{activity.getApplication().unregisterActivityLifecycleCallbacks(lifecycle);wait.dismiss();if(activity.isFinishing()||activity.isDestroyed())return;if(failed!=null)Ui.error(activity,failed);else result.success(done);});});}
        catch(RejectedExecutionException e){activity.getApplication().unregisterActivityLifecycleCallbacks(lifecycle);wait.dismiss();Ui.toast(activity,"文件操作繁忙，请稍后重试");}
    }
}
