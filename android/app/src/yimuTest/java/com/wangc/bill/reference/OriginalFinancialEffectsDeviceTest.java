package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Synthetic DB -> normal production command -> actual asynchronous cache/MAIN/RemoteViews. */
@RunWith(AndroidJUnit4.class)
public final class OriginalFinancialEffectsDeviceTest {
    private static final String DATABASE="reference_original_effects_test.sqlite";
    private Context context;private ReferenceStore store;private SharedPreferences prefs;private long oldBook;
    @Before public void open(){context=ApplicationProvider.getApplicationContext();context.deleteDatabase(DATABASE);store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();prefs=context.getSharedPreferences("reference_settings",Context.MODE_PRIVATE);oldBook=prefs.getLong("current_book",1);prefs.edit().putLong("current_book",store.books().get(0).optLong("id")).commit();}
    @After public void close(){if(store!=null)store.close();prefs.edit().putLong("current_book",oldBook).commit();context.deleteDatabase(DATABASE);}
    private void await(BooleanSupplier value){long until=SystemClock.uptimeMillis()+8000;while(SystemClock.uptimeMillis()<until){if(value.getAsBoolean())return;SystemClock.sleep(25);}assertTrue("Timed out waiting for actual financial work",value.getAsBoolean());}
    private long credit(int owner,long semantic,double balance,boolean hide,Long book){AssetRepository.Definition d=new AssetRepository.Definition("合成信用账户",2,null);d.assetNumber=balance;d.inAccountDate="出账日1";d.hide=hide;if(book!=null)d.showBook.add(book);return store.assetRepository().insertRecovered(owner,semantic,1,0,d);}
    private JSONObject input(long account){return Json.object("book_id",store.books().get(0).optLong("id"),"kind","expense","amount",500,"currency","CNY","category_id",store.categoryRepository().parent(99).optLong("id"),"account_id",account,"time",System.currentTimeMillis(),"remark","合成信用回归","fee",0,"discount",0);}
    private long revision(long semantic){return OriginalFinancialEffects.creditRevision(store,0,0,semantic);}
    private void ready(long semantic){await(()->OriginalFinancialEffects.creditStatus(store,0,0,semantic)==OriginalFinancialEffects.CreditStatus.READY);}
    private OriginalBillRecord raw(int owner,int from,int id,long asset){OriginalBillRecord r=new OriginalBillRecord();r.userId=owner;r.fromUserId=from;r.billId=id;r.assetId=asset;r.cost=5;r.time=System.currentTimeMillis();r.bookId=1;r.parentCategoryId=99;r.childCategoryId=-1;ContentValues p=new ContentValues();p.put("book_id",store.books().get(0).optLong("id"));p.put("kind","expense");p.put("amount",500);p.put("currency","CNY");p.put("category_id",store.categoryRepository().parent(99).optLong("id"));p.put("time",r.time);p.put("created",r.time);new OriginalBillRepository(store).saveOriginalRaw(r,p);return r;}

    @Test public void normalSaveBuildsActualCreditCacheAndDeliversEventsOnMain(){long account=credit(0,99,0,false,null);ready(99);long before=revision(99);CountDownLatch notified=new CountDownLatch(1);AtomicBoolean main=new AtomicBoolean();OriginalFinancialEffects.Listener listener=e->{if(e==OriginalFinancialEffects.Event.BILLS_CHANGED){main.set(Looper.myLooper()==Looper.getMainLooper());notified.countDown();}};OriginalFinancialEffects.subscribe(store,listener);try{JSONObject automatic=input(account);String candidate=store.addCandidate(automatic,"com.eg.android.AlipayGphone",automatic.optLong("book_id"));long row=store.saveTransaction(automatic,0,candidate);await(()->revision(99)>before);ready(99);List<OriginalCreditBill> bills=OriginalFinancialEffects.creditStatements(store,0,0,99);assertEquals(1,bills.size());assertEquals(5,bills.get(0).getNumber(),0);assertEquals(5,bills.get(0).getRemindNumber(),0);assertEquals(new OriginalBillRepository(store).byLocalRowId(row).billId,bills.get(0).getBillInfoList().get(0).getBillId());assertEquals(-5,store.assetRepository().byRowId(account).optDouble("asset_number"),0);await(()->notified.getCount()==0);assertTrue(main.get());}finally{OriginalFinancialEffects.unsubscribe(store,listener);}}
    @Test public void failedUserBillSaveRollsBackAndOnlyASuccessfulRetryRebuildsCredit(){
        long account=credit(0,99,0,false,null);ready(99);long before=revision(99);
        JSONObject beforeTables=store.backup().optJSONObject("tables");
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER fail_effect_bill BEFORE INSERT ON transactions WHEN NEW.bill_origin='original' BEGIN SELECT RAISE(ABORT,'synthetic rejection'); END");
        try{store.saveTransaction(input(account),0,null);fail("A failed user save must be reported");}catch(IllegalStateException expected){}
        assertEquals(beforeTables.toString(),store.backup().optJSONObject("tables").toString());
        assertEquals(before,revision(99));ready(99);
        assertTrue(OriginalFinancialEffects.creditStatements(store,0,0,99).isEmpty());
        assertEquals(0,store.assetRepository().byRowId(account).optDouble("asset_number"),0);
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
        store.getWritableDatabase().execSQL("DROP TRIGGER fail_effect_bill");
        long saved=store.saveTransaction(input(account),0,null);
        await(()->revision(99)>before);ready(99);
        List<OriginalCreditBill> rebuilt=OriginalFinancialEffects.creditStatements(store,0,0,99);
        assertEquals(1,rebuilt.size());assertEquals(5,rebuilt.get(0).getNumber(),0);
        assertEquals(5,rebuilt.get(0).getRemindNumber(),0);
        assertEquals(new OriginalBillRepository(store).byLocalRowId(saved).billId,rebuilt.get(0).getBillInfoList().get(0).getBillId());
        assertEquals(-5,store.assetRepository().byRowId(account).optDouble("asset_number"),0);
        assertEquals(1,store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transactions"));
    }
    @Test public void queuedCreditUsesTheCapturedMutableHandleInsteadOfReloadingItsBalance(){credit(0,99,-5,false,null);raw(0,0,123,99);ready(99);OriginalFinancialEffects effects=new OriginalFinancialEffects(store,0,0);long before=revision(99);synchronized(store){OriginalAssetBalanceStore.LoadedAsset retained=new OriginalAssetBalanceStore(store).load(0,99);effects.assetChanged(retained,true);retained.setStoredNumber(-3);}await(()->revision(99)>before);assertEquals(3,OriginalFinancialEffects.creditStatements(store,0,0,99).get(0).getRemindNumber(),0);assertEquals(-5,store.assetRepository().p(99,0).optDouble("asset_number"),0);}
    @Test public void changingBooksRebuildsCreditVisibilityWithoutCancellingAssetDelayedCallbacks(){long semantic=store.book(store.books().get(0).optLong("id")).optLong("semantic_id");credit(0,99,-5,true,semantic);raw(0,0,123,99);ready(99);assertEquals(1,OriginalFinancialEffects.creditStatements(store,0,0,99).size());CountDownLatch delayed=new CountDownLatch(1);OriginalFinancialEffects effects=new OriginalFinancialEffects(store,0,0);effects.schedule(delayed::countDown,150);OriginalFinancialEffects.bookSelectionChanged(store);ready(99);await(()->delayed.getCount()==0);assertEquals(1,OriginalFinancialEffects.creditStatements(store,0,0,99).size());assertTrue(OriginalRepaymentWidgetService.upcoming(store,System.currentTimeMillis()).isEmpty());}
    @Test public void fullRestoreEpochCancelsOldDelayedWorkButRetainsLiveListeners() throws Exception {OriginalFinancialEffects effects=new OriginalFinancialEffects(store,0,0);AtomicBoolean ran=new AtomicBoolean();CountDownLatch restored=new CountDownLatch(1);OriginalFinancialEffects.Listener listener=e->{if(e==OriginalFinancialEffects.Event.RESTORED)restored.countDown();};OriginalFinancialEffects.subscribe(store,listener);try{effects.schedule(()->ran.set(true),200);OriginalFinancialEffects.resetAfterRestore(store);assertTrue(restored.await(3,TimeUnit.SECONDS));SystemClock.sleep(350);assertFalse(ran.get());}finally{OriginalFinancialEffects.unsubscribe(store,listener);}}
    @Test public void creditQueriesUseCurrentOwnerOrFromUserWithoutCurrentBookOwnerExpansion(){credit(7,99,0,false,null);OriginalBillRecord excluded=raw(8,9,123,99),included=raw(8,7,124,99);OriginalCreditDataSource source=new OriginalCreditDataSource(store,7);assertNull(source.billBySemantic(excluded.billId));assertNotNull(source.billBySemantic(included.billId));assertEquals(1,source.bills(99).size());assertEquals(124,source.bills(99).get(0).billId);OriginalReimbursementRepository.Data a=new OriginalReimbursementRepository.Data();a.parentBillSemanticId=123;a.rawEntries=java.util.Arrays.asList("99:2:"+excluded.time);new OriginalReimbursementRepository(store).insertRecovered(7,1,a,1);OriginalReimbursementRepository.Data b=new OriginalReimbursementRepository.Data();b.parentBillSemanticId=124;b.rawEntries=java.util.Arrays.asList("99:3:"+included.time);new OriginalReimbursementRepository(store).insertRecovered(7,2,b,2);assertEquals(1,source.receipts(99).size());assertEquals(3,source.receipts(99).get(0).amount,0);}
    @Test public void refundProjectionReadsFreshDatabaseTotalRatherThanTheSeparateMutationCache(){OriginalBillRecord bill=raw(0,0,123,0);OriginalRefundRepository.Data data=new OriginalRefundRepository.Data();data.parentBillSemanticId=123;data.refundNumber=2;new OriginalRefundRepository(store).insertRecovered(0,1,data,1);new OriginalFinancialEffects(store,0,0).refundAggregateChanged(123,9,false);OriginalCreditProjection.RefundAdjustment read=new OriginalCreditDataSource(store,0).refund(bill);assertTrue(read.exists);assertEquals(2,read.amount,0);}
    @Test public void retainedMetadataStillReachesTheNameCacheAfterEntitySaveReturnsFalse(){store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER fail_effect_asset BEFORE INSERT ON accounts WHEN NEW.asset_origin='original' BEGIN SELECT RAISE(ABORT,'synthetic rejection'); END");try{new OriginalAssetCommands(store,0).createFund("未落库的合成名称",null,7);fail("Reported a failed entity as saved");}catch(IllegalStateException expected){}JSONObject history=store.one("SELECT * FROM asset_histories ORDER BY id LIMIT 1");JSONObject cached=OriginalFinancialEffects.cachedAsset(store,0,history.optLong("asset_semantic_id"));assertNotNull(cached);assertEquals("未落库的合成名称",cached.optString("name"));assertEquals(7,cached.optDouble("asset_number"),0);assertEquals(0,store.scalar("SELECT COUNT(*) n FROM accounts"));}
    @Test public void malformedOriginalStatementDatePublishesFailureMetadataWithoutPretendingReady(){long account=credit(0,99,-5,false,null);ready(99);store.getWritableDatabase().execSQL("UPDATE accounts SET out_account_date='not-a-day' WHERE id=?",new Object[]{account});OriginalFinancialEffects effects=new OriginalFinancialEffects(store,0,0);effects.creditRefresh(99);await(()->OriginalFinancialEffects.creditStatus(store,0,0,99)==OriginalFinancialEffects.CreditStatus.FAILED);assertEquals("java.lang.NumberFormatException",OriginalFinancialEffects.creditFailureType(store,0,0,99));}
    @Test public void actualRemoteViewsInflateAndFactoryRendersRealDebtWithTheOriginalEmptyContract(){credit(0,99,-5,false,null);raw(0,0,123,99);ready(99);OriginalRepaymentWidgetService.Factory factory=new OriginalRepaymentWidgetService.Factory(store,17);factory.onDataSetChanged();assertEquals(1,factory.getCount());assertEquals(3,factory.getViewTypeCount());assertTrue(factory.hasStableIds());assertNull(factory.getLoadingView());AtomicReference<View> container=new AtomicReference<>(),row=new AtomicReference<>();InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{container.set(OriginalRepaymentWidget.views(context,17).apply(context,new FrameLayout(context)));row.set(factory.getViewAt(0).apply(context,new FrameLayout(context)));});assertNotNull(container.get());TextView name=row.get().findViewById(OriginalRepaymentWidget.required(context,"id","asset_name")),cost=row.get().findViewById(OriginalRepaymentWidget.required(context,"id","cost"));assertEquals("合成信用账户",name.getText().toString());assertEquals("5.00",cost.getText().toString());TextView timing=row.get().findViewById(OriginalRepaymentWidget.required(context,"id","time_info"));assertTrue(timing.getText().toString().endsWith("天后还款5.00"));assertEquals(View.VISIBLE,timing.getVisibility());factory.onDestroy();assertEquals(1,factory.getCount());AtomicReference<View> empty=new AtomicReference<>();InstrumentationRegistry.getInstrumentation().runOnMainSync(()->empty.set(factory.getViewAt(0).apply(context,new FrameLayout(context))));assertNotNull(empty.get());assertNull(empty.get().findViewById(OriginalRepaymentWidget.required(context,"id","cost")));assertFalse(ReferenceApplication.isProductionStore(store));}
}
