package com.wangc.bill.reference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.SystemClock;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Actual X completion -> independent MAIN g7.u, separate from i1 persistence and s2 refresh. */
@RunWith(AndroidJUnit4.class)
public final class OriginalLendEffectsDeviceTest {
    private static final String DATABASE="reference_original_lend_effects_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private SharedPreferences preferences;
    private boolean hadBook;
    private long oldBook;

    @Before public void open(){
        context=ApplicationProvider.getApplicationContext();context.deleteDatabase(DATABASE);
        store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();
        preferences=context.getSharedPreferences("reference_settings",Context.MODE_PRIVATE);
        hadBook=preferences.contains("current_book");oldBook=preferences.getLong("current_book",1);
        assertTrue(preferences.edit().putLong("current_book",store.books().get(0).optLong("id")).commit());
    }
    @After public void close(){
        if(store!=null)store.close();
        if(preferences!=null){SharedPreferences.Editor e=preferences.edit();if(hadBook)e.putLong("current_book",oldBook);else e.remove("current_book");e.commit();}
        if(context!=null)context.deleteDatabase(DATABASE);
    }
    private void await(BooleanSupplier condition){
        long until=SystemClock.uptimeMillis()+8000;
        while(SystemClock.uptimeMillis()<until){if(condition.getAsBoolean())return;SystemClock.sleep(25);}
        assertTrue("Actual Lend effect timed out",condition.getAsBoolean());
    }
    private long asset(long semantic,int type,double balance){
        AssetRepository.Definition d=new AssetRepository.Definition("合成借贷事件账户 "+semantic,type,null);
        d.assetNumber=balance;d.outAccountDate="每月1号";d.inAccountDate="出账日1";
        return store.assetRepository().insertRecovered(0,semantic,1,0,d);
    }
    private OriginalLendRecord lend(int semantic,double fromCost){
        OriginalLendRecord r=new OriginalLendRecord();r.userId=0;r.lendId=semantic;
        r.assetId=199;r.repaymentAssetId=99;r.type=1;r.billId=700;
        r.fromCost=fromCost;r.number=90;r.outTime=OriginalInstalmentSchedule.parse("2025-01-15","yyyy-MM-dd");
        return r;
    }
    private OriginalAutoCreditRepaymentCommands command(){
        return new OriginalAutoCreditRepaymentCommands(store,0,0,new OriginalLendActions(store,0),
                semantic->store.book(store.books().get(0).optLong("id")).optLong("semantic_id"));
    }
    private OriginalAutoCreditRepaymentCommands.Effects effects(AtomicBoolean files){
        OriginalFinancialEffects real=new OriginalFinancialEffects(store,0,0);
        return new OriginalAutoCreditRepaymentCommands.Effects(){
            @Override public void warningNonPositiveArrival(){fail("This synthetic X must not skip principal");}
            @Override public void repaymentChanged(OriginalLendRecord row,boolean saved){
                assertTrue("The source u tail must follow the attachment stage",files.get());
                assertFalse(store.getWritableDatabase().inTransaction());real.lendChanged(row,saved);
            }
        };
    }

    @Test public void actualRepaymentCompletionPublishesItsOwnMainEventAfterTheEntityAndAttachmentStages() throws Exception {
        long source=asset(99,1,100),destination=asset(199,1,10);
        List<OriginalFinancialEffects.Event> events=new CopyOnWriteArrayList<>();AtomicBoolean main=new AtomicBoolean(),files=new AtomicBoolean();
        AtomicLong visible=new AtomicLong(-1);CountDownLatch delivered=new CountDownLatch(1);
        OriginalFinancialEffects.Listener listener=e->{events.add(e);if(e==OriginalFinancialEffects.Event.LENDS_CHANGED){
            main.set(Looper.myLooper()==Looper.getMainLooper());visible.set(store.scalar("SELECT COUNT(*) n FROM original_lends"));delivered.countDown();}};
        OriginalFinancialEffects.subscribe(store,listener);
        try{
            OriginalAutoCreditRepaymentCommands.Result result=command().save(99L,199L,5,"",0,"合成真实还款尾事件",1,
                    bill->{throw new AssertionError("A no-interest X must not create a Bill");},effects(files),
                    semantic->{assertNotNull(new OriginalLendRepository(store).byIdentity(0,(int)semantic));files.set(true);});
            assertTrue(result.saved);assertTrue(delivered.await(3,TimeUnit.SECONDS));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();assertTrue(main.get());assertEquals(1,visible.get());
            assertEquals(Arrays.asList(OriginalFinancialEffects.Event.ASSETS_CHANGED,OriginalFinancialEffects.Event.ASSETS_CHANGED,
                    OriginalFinancialEffects.Event.LENDS_CHANGED),events);
            assertEquals(5,result.lend.number,0);assertEquals(0,result.lend.fromCost,0);
            assertEquals(95,store.assetRepository().byRowId(source).optDouble("asset_number"),0);
            assertEquals(15,store.assetRepository().byRowId(destination).optDouble("asset_number"),0);
            assertEquals(2,store.scalar("SELECT COUNT(*) n FROM asset_histories"));
            assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
        }finally{OriginalFinancialEffects.unsubscribe(store,listener);}
    }

    @Test public void entityActionsStaySilentAndACompletedReturnedFalseRepaymentStillPublishesU() throws Exception {
        List<OriginalFinancialEffects.Event> events=new CopyOnWriteArrayList<>();
        OriginalFinancialEffects.Listener listener=events::add;OriginalFinancialEffects.subscribe(store,listener);
        try{
            OriginalLendActions actions=new OriginalLendActions(store,0);
            OriginalLendActions.Result added=actions.add(lend(901,5));assertTrue(added.save.saved);
            added.record.fromCost=6;assertTrue(actions.update(added.record).save.saved);
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();assertTrue("i1.d/w0 do not post g7.u",events.isEmpty());
            asset(99,1,100);asset(199,1,10);AtomicBoolean files=new AtomicBoolean();
            store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER reject_lend_effect BEFORE INSERT ON original_lends BEGIN SELECT RAISE(ABORT,'synthetic rejection'); END");
            OriginalAutoCreditRepaymentCommands.Result failed=command().save(99L,199L,5,"",0,"合成返回false尾事件",1,
                    bill->{throw new AssertionError("A no-interest X must not create a Bill");},effects(files),
                    semantic->{assertEquals(1,store.scalar("SELECT COUNT(*) n FROM original_lends"));files.set(true);});
            assertFalse(failed.persistedLend);assertFalse(failed.saved);assertEquals(0,failed.lend.localRowId);
            await(()->events.size()==3);InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            assertEquals(Arrays.asList(OriginalFinancialEffects.Event.ASSETS_CHANGED,OriginalFinancialEffects.Event.ASSETS_CHANGED,
                    OriginalFinancialEffects.Event.LENDS_CHANGED),events);
            assertEquals(6,new OriginalLendRepository(store).byLocalRowId(added.record.localRowId).fromCost,0);
            assertEquals(1,store.scalar("SELECT COUNT(*) n FROM original_lends"));
            assertEquals(2,store.scalar("SELECT COUNT(*) n FROM asset_histories"));
            assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
        }finally{OriginalFinancialEffects.unsubscribe(store,listener);}
    }

    @Test public void theLendEventPreservesAnExistingCreditRevisionUntilARealRefreshIsRequested() throws Exception {
        long account=asset(99,2,-20);OriginalLendRecord row=lend(901,5);new OriginalLendRepository(store).saveOriginalRaw(row);
        CountDownLatch initialized=new CountDownLatch(1);
        OriginalFinancialEffects.Listener startup=e->{if(e==OriginalFinancialEffects.Event.CREDIT_CHANGED)initialized.countDown();};
        OriginalFinancialEffects.subscribe(store,startup);
        try{
            await(()->OriginalFinancialEffects.creditStatus(store,0,0,99)==OriginalFinancialEffects.CreditStatus.READY);
            assertTrue(initialized.await(3,TimeUnit.SECONDS));
        }finally{OriginalFinancialEffects.unsubscribe(store,startup);}
        long before=OriginalFinancialEffects.creditRevision(store,0,0,99);
        assertEquals(5,OriginalFinancialEffects.creditStatements(store,0,0,99).get(0).getNumber(),0);
        List<OriginalFinancialEffects.Event> events=new CopyOnWriteArrayList<>();
        CountDownLatch changed=new CountDownLatch(1),rebuilt=new CountDownLatch(1);
        OriginalFinancialEffects.Listener listener=e->{events.add(e);if(e==OriginalFinancialEffects.Event.LENDS_CHANGED)changed.countDown();
            if(e==OriginalFinancialEffects.Event.CREDIT_CHANGED)rebuilt.countDown();};
        OriginalFinancialEffects.subscribe(store,listener);
        try{
            row.fromCost=9;OriginalLendActions.Result updated=new OriginalLendActions(store,0).update(row);assertTrue(updated.save.saved);
            OriginalFinancialEffects effects=new OriginalFinancialEffects(store,0,0);effects.lendChanged(row,updated.save.saved);
            assertTrue(changed.await(3,TimeUnit.SECONDS));InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            assertEquals(before,OriginalFinancialEffects.creditRevision(store,0,0,99));
            assertEquals(5,OriginalFinancialEffects.creditStatements(store,0,0,99).get(0).getNumber(),0);
            assertEquals(Collections.singletonList(OriginalFinancialEffects.Event.LENDS_CHANGED),events);
            effects.creditRefresh(99);assertTrue(rebuilt.await(3,TimeUnit.SECONDS));
            await(()->OriginalFinancialEffects.creditStatus(store,0,0,99)==OriginalFinancialEffects.CreditStatus.READY
                    &&OriginalFinancialEffects.creditRevision(store,0,0,99)>before);
            assertEquals(9,OriginalFinancialEffects.creditStatements(store,0,0,99).get(0).getNumber(),0);
            assertEquals(Arrays.asList(OriginalFinancialEffects.Event.LENDS_CHANGED,OriginalFinancialEffects.Event.CREDIT_CHANGED),events);
            assertEquals(-20,store.assetRepository().byRowId(account).optDouble("asset_number"),0);
            assertEquals(0,store.scalar("SELECT COUNT(*) n FROM asset_histories"));
            assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
        }finally{OriginalFinancialEffects.unsubscribe(store,listener);}
    }
}
