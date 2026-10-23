package com.wangc.bill.reference;

import android.os.Handler;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class OriginalFinancialSchedulerDeviceTest {
    @Test public void delayedPoolHasFourIndependentWorkersAndMainIsSeparate() throws Exception {OriginalFinancialScheduler scheduler=new OriginalFinancialScheduler();CountDownLatch entered=new CountDownLatch(4),release=new CountDownLatch(1),main=new CountDownLatch(1);Set<Long> workers=Collections.synchronizedSet(new HashSet<>());AtomicBoolean wrongLooper=new AtomicBoolean();try{for(int i=0;i<4;i++)scheduler.schedule(()->{workers.add(Thread.currentThread().getId());if(Looper.myLooper()==Looper.getMainLooper())wrongLooper.set(true);entered.countDown();try{release.await(4,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}},0);assertTrue("Delay tasks were serialized or sent to MAIN",entered.await(3,TimeUnit.SECONDS));assertEquals(4,workers.size());scheduler.executeMain(()->{if(Looper.myLooper()!=Looper.getMainLooper())wrongLooper.set(true);main.countDown();});assertTrue(main.await(3,TimeUnit.SECONDS));assertFalse(wrongLooper.get());}finally{release.countDown();scheduler.close();}}
    @Test public void creditExecutorDropsBeforeInitializationThenRunsSerially() throws Exception {OriginalFinancialScheduler scheduler=new OriginalFinancialScheduler();List<Integer> order=Collections.synchronizedList(new ArrayList<>());CountDownLatch first=new CountDownLatch(1),release=new CountDownLatch(1),second=new CountDownLatch(1);try{scheduler.executeCredit(()->order.add(99));scheduler.initializeCredit();scheduler.executeCredit(()->{order.add(1);first.countDown();try{release.await(3,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}});assertTrue(first.await(3,TimeUnit.SECONDS));scheduler.executeCredit(()->{order.add(2);second.countDown();});assertEquals(Collections.singletonList(1),new ArrayList<>(order));release.countDown();assertTrue(second.await(3,TimeUnit.SECONDS));assertEquals(java.util.Arrays.asList(1,2),order);}finally{release.countDown();scheduler.close();}}
    @Test public void shutdownExecutorRejectionIsNotRewrittenAsSuccessfulScheduling(){ScheduledExecutorService delay=Executors.newScheduledThreadPool(4);ExecutorService credit=Executors.newSingleThreadExecutor();delay.shutdownNow();credit.shutdownNow();OriginalFinancialScheduler scheduler=new OriginalFinancialScheduler(delay,credit,new Handler(Looper.getMainLooper()));try{try{scheduler.schedule(()->{},0);fail("Swallowed a nonnull shutdown delay executor rejection");}catch(RejectedExecutionException expected){}try{scheduler.executeCredit(()->{});fail("Swallowed a nonnull shutdown credit executor rejection");}catch(RejectedExecutionException expected){}}finally{scheduler.close();}}
}
