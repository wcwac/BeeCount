package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real i1.V/g0 SQLite inputs to the existing s2 credit worker, using synthetic records only. */
@RunWith(AndroidJUnit4.class)
public final class OriginalLendCreditDeviceTest {
    private static final String DATABASE="reference_original_lend_credit_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private SharedPreferences preferences;
    private boolean hadBook;
    private long oldBook,january,february;

    @Before public void open(){
        context=ApplicationProvider.getApplicationContext();context.deleteDatabase(DATABASE);
        store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();
        preferences=context.getSharedPreferences("reference_settings",Context.MODE_PRIVATE);
        hadBook=preferences.contains("current_book");oldBook=preferences.getLong("current_book",1);
        assertTrue(preferences.edit().putLong("current_book",store.books().get(0).optLong("id")).commit());
        january=date("2025-01-15");february=date("2025-02-15");
    }
    @After public void close(){
        if(store!=null)store.close();
        if(preferences!=null){SharedPreferences.Editor e=preferences.edit();if(hadBook)e.putLong("current_book",oldBook);else e.remove("current_book");e.commit();}
        if(context!=null)context.deleteDatabase(DATABASE);
    }
    private static long date(String value){return OriginalInstalmentSchedule.parse(value,"yyyy-MM-dd");}
    private void await(BooleanSupplier condition){
        long until=SystemClock.uptimeMillis()+8000;
        while(SystemClock.uptimeMillis()<until){if(condition.getAsBoolean())return;SystemClock.sleep(25);}
        assertTrue("Actual Lend credit rebuild timed out",condition.getAsBoolean());
    }
    private long asset(int owner,long semantic,int type,double balance){
        AssetRepository.Definition d=new AssetRepository.Definition("合成借贷账户 "+semantic,type,null);
        d.assetNumber=balance;d.outAccountDate="每月1号";d.inAccountDate="出账日1";
        return store.assetRepository().insertRecovered(owner,semantic,1,0,d);
    }
    private OriginalLendRecord lend(int owner,int semantic,long principal,long repayment,int type,double rawCost,double number,long time){
        OriginalLendRecord row=new OriginalLendRecord();row.userId=owner;row.lendId=semantic;
        row.assetId=principal;row.repaymentAssetId=repayment;row.type=type;row.fromCost=rawCost;
        row.number=number;row.outTime=time;row.remark="synthetic-lend-"+semantic;
        new OriginalLendRepository(store).saveOriginalRaw(row);return row;
    }
    private OriginalLendRecord save(OriginalLendRecord row){new OriginalLendRepository(store).saveOriginalRaw(row);return row;}
    private OriginalBillRecord bill(int owner,int fromUser,int semantic,long asset,double number){
        long book=store.books().get(0).optLong("id");
        OriginalBillRecord row=new OriginalBillRecord();row.userId=owner;row.fromUserId=fromUser;
        row.billId=semantic;row.assetId=asset;row.cost=number;row.time=january;
        row.bookId=store.book(book).optLong("semantic_id");row.parentCategoryId=99;row.childCategoryId=-1;
        ContentValues projection=new ContentValues();projection.put("book_id",book);projection.put("kind","expense");
        projection.put("amount",Math.round(number*100));projection.put("currency","CNY");
        projection.put("category_id",store.categoryRepository().parent(99).optLong("id"));
        projection.put("parent_semantic_id",99);projection.put("child_semantic_id",-1);
        projection.put("owner_semantic_id",owner);projection.put("raw_asset_semantic_id",asset);
        projection.put("time",january);projection.put("created",january);
        new OriginalBillRepository(store).saveOriginalRaw(row,projection);return row;
    }
    private void rebuild(int owner,int bookOwner,long semantic){
        await(()->OriginalFinancialEffects.creditStatus(store,owner,bookOwner,semantic)==OriginalFinancialEffects.CreditStatus.READY);
        long before=OriginalFinancialEffects.creditRevision(store,owner,bookOwner,semantic);
        new OriginalFinancialEffects(store,owner,bookOwner).creditRefresh(semantic);
        await(()->OriginalFinancialEffects.creditStatus(store,owner,bookOwner,semantic)==OriginalFinancialEffects.CreditStatus.READY
                &&OriginalFinancialEffects.creditRevision(store,owner,bookOwner,semantic)>before);
    }
    private List<OriginalCreditBill> statements(int owner,int bookOwner,long semantic){return OriginalFinancialEffects.creditStatements(store,owner,bookOwner,semantic);}
    private Map<String,OriginalCreditBillInfo> details(OriginalCreditBill bill){
        Map<String,OriginalCreditBillInfo> result=new HashMap<>();
        if(bill.getBillInfoList()!=null)for(OriginalCreditBillInfo info:bill.getBillInfoList())assertNull("Duplicate synthetic remark",result.put(info.getRemark(),info));
        return result;
    }
    private void noFinancialReplay(){assertEquals(0,store.scalar("SELECT COUNT(*) n FROM asset_histories"));}

    @Test public void actualRepaymentFeedKeepsCurrentOwnerAllTypesButExcludesDeletedRowsWithoutPrincipalOrBookExpansion(){
        long target=9000000099L;
        AssetRepository.Definition hidden=new AssetRepository.Definition("隐藏别本借贷信用账户",2,null);
        hidden.hide=true;hidden.showBook.add(9999L);store.assetRepository().insertRecovered(7,target,1,0,hidden);
        OriginalLendRecord first=lend(7,Integer.MIN_VALUE,199,target,4,0,88,january);
        first.billId=781;first.generalServiceBill=true;first.interest=-2;first.inTime=february;first.originalDeleted=true;save(first);
        lend(7,0,299,target,99,-12.125,55,february);
        lend(8,0,199,target,4,100,100,january);
        lend(7,1,target,199,4,200,200,january);
        lend(7,2,199,0,0,.5,99,january);
        List<OriginalCreditProjection.Lend> read=new OriginalCreditDataSource(store,7).lends(target);
        assertEquals(1,read.size());Map<String,OriginalCreditProjection.Lend> values=new HashMap<>();
        for(OriginalCreditProjection.Lend row:read)values.put(row.remark,row);
        assertNull(values.get(first.remark));OriginalLendRecord zero=new OriginalLendRepository(store).byLocalRowId(first.localRowId);assertNotNull(zero);assertTrue(zero.originalDeleted);
        assertEquals(0,zero.fromCost,0);assertEquals(88,first.getCurrencyCost(target),0);
        assertEquals(199,zero.assetId);assertEquals(target,zero.repaymentAssetId);assertEquals(4,zero.type);
        assertEquals(781,zero.billId);assertTrue(zero.generalServiceBill);assertEquals(-2,zero.interest,0);
        assertEquals(january,zero.getTime());assertEquals(january,zero.outTime);assertNotEquals(first.lendId,first.localRowId);
        assertEquals(-12.125,values.get("synthetic-lend-0").fromCost,0);
        assertEquals(99,values.get("synthetic-lend-0").type);
        assertEquals(1,new OriginalCreditDataSource(store,7).lends(0).size());
        assertTrue(new OriginalCreditDataSource(store,0).lends(target).isEmpty());
        noFinancialReplay();
    }

    @Test public void persistedRawFromCostDrivesEveryOriginalTypeBranchAndNeverFallsBackToNumber(){
        long account=asset(0,99,2,-99);asset(0,199,1,0);
        int[] types={1,2,3,4,1,2,3,4};double[] amounts={-2,-3,-5,-7,11,13,17,0};
        for(int i=0;i<types.length;i++){
            OriginalLendRecord row=lend(0,900+i,199,99,types[i],amounts[i],71,january);
            row.billId=i<4?1000+i:0;save(row);
        }
        rebuild(0,0,99);OriginalCreditBill statement=statements(0,0,99).get(0);
        assertEquals(6,statement.getNumber(),0);assertEquals(8,statement.getBillInfoList().size());
        double[] expected={-2,3,5,-7,-11,13,17,0};Map<String,OriginalCreditBillInfo> values=details(statement);
        for(int i=0;i<types.length;i++){
            OriginalCreditBillInfo info=values.get("synthetic-lend-"+(900+i));assertNotNull(info);
            assertEquals(expected[i],info.getNumber(),0);assertEquals(0,info.getBillId());assertEquals(january,info.getDate());
        }
        assertEquals("还款-合成借贷账户 199",values.get("synthetic-lend-907").getTitle());
        assertEquals("ic_data_repayment",values.get("synthetic-lend-907").getIcon());
        assertEquals(-99,store.assetRepository().byRowId(account).optDouble("asset_number"),0);
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));noFinancialReplay();
    }

    @Test public void linkedBillsAreCountedOnceAndTypeSevenRepaymentsKeepTheOriginalGeneralServiceException(){
        asset(0,99,2,-50);asset(0,199,7,0);asset(0,299,1,0);OriginalBillRecord linked=bill(0,0,501,99,2);
        for(int type=1;type<=3;type++){OriginalLendRecord row=lend(0,900+type,199,99,type,20,20,january);row.billId=linked.billId;save(row);}
        OriginalLendRecord special=lend(0,904,199,99,4,30,30,january);special.billId=linked.billId;save(special);
        OriginalLendRecord general=lend(0,905,199,99,4,1,1,january);general.billId=linked.billId;general.generalServiceBill=true;save(general);
        OriginalLendRecord ordinary=lend(0,906,299,99,4,3,3,january);ordinary.billId=linked.billId;save(ordinary);
        rebuild(0,0,99);OriginalCreditBill statement=statements(0,0,99).get(0);
        assertEquals(-2,statement.getNumber(),0);assertEquals(3,statement.getBillInfoList().size());
        assertEquals(linked.billId,statement.getBillInfoList().get(0).getBillId());
        Map<String,OriginalCreditBillInfo> values=details(statement);
        assertFalse(values.containsKey(special.remark));assertEquals(-1,values.get(general.remark).getNumber(),0);
        assertEquals(-3,values.get(ordinary.remark).getNumber(),0);
        assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transactions"));noFinancialReplay();
    }

    @Test public void bookOwnerNeverExpandsLendOwnershipOrTheLinkedBillCurrentUserFromUserLookup(){
        asset(7,99,2,-20);asset(8,99,2,-90);
        OriginalBillRecord foreign=bill(8,9,501,99,7),shared=bill(8,7,502,99,4);
        OriginalLendRecord include=lend(7,901,199,99,2,3,80,january);include.billId=foreign.billId;save(include);
        OriginalLendRecord skip=lend(7,902,199,99,2,90,90,january);skip.billId=shared.billId;save(skip);
        lend(8,903,199,99,2,100,100,january);
        OriginalCreditDataSource data=new OriginalCreditDataSource(store,7);assertNull(data.billBySemantic(501));assertNotNull(data.billBySemantic(502));
        assertEquals(2,data.lends(99).size());rebuild(7,8,99);
        OriginalCreditBill statement=statements(7,8,99).get(0);assertEquals(1,statement.getNumber(),0);
        assertEquals(2,statement.getBillInfoList().size());assertEquals(shared.billId,statement.getBillInfoList().get(0).getBillId());
        assertEquals(3,details(statement).get(include.remark).getNumber(),0);assertFalse(details(statement).containsKey(skip.remark));
        assertEquals(-20,store.assetRepository().p(99,7).optDouble("asset_number"),0);
        assertEquals(-90,store.assetRepository().p(99,8).optDouble("asset_number"),0);noFinancialReplay();
    }

    @Test public void originalOutTimeSelectsTheStatementMonthWithoutAnInTimeOrCreationTimeFallback(){
        asset(0,99,2,-10);
        OriginalLendRecord januaryRow=lend(0,901,199,99,4,2,99,january);januaryRow.inTime=february;januaryRow.createTime=february;save(januaryRow);
        OriginalLendRecord februaryRow=lend(0,902,199,99,4,3,99,february);februaryRow.inTime=january;save(februaryRow);
        rebuild(0,0,99);List<OriginalCreditBill> statements=statements(0,0,99);assertEquals(2,statements.size());
        assertEquals(2,statements.get(0).getMonth());assertEquals(-3,statements.get(0).getNumber(),0);
        assertEquals(february,statements.get(0).getBillInfoList().get(0).getDate());
        assertEquals(1,statements.get(1).getMonth());assertEquals(-2,statements.get(1).getNumber(),0);
        assertEquals(january,statements.get(1).getBillInfoList().get(0).getDate());noFinancialReplay();
    }

    @Test public void actualInterestFeedUsesPrincipalAssetTypeFourInclusiveOutTimeAndExcludesDeletedInterest(){
        long start=date("2025-01-02"),end=OriginalInstalmentSchedule.dayEnd(date("2025-02-01"));
        OriginalLendRecord first=lend(7,901,99,199,4,1,999,start);first.interest=3;first.originalDeleted=true;save(first);
        OriginalLendRecord last=lend(7,902,99,299,4,2,999,end);last.interest=-4;save(last);
        OriginalLendRecord zero=lend(7,903,99,0,4,0,999,start+1);zero.interest=0;save(zero);
        lend(7,904,99,199,4,1,1,start-1);lend(7,905,99,199,4,1,1,end+1);
        lend(8,906,99,199,4,1,1,start);lend(7,907,199,99,4,1,1,start);lend(7,908,99,199,3,1,1,start);
        List<OriginalCreditProjection.Lend> rows=new OriginalCreditDataSource(store,7).interestRows(99,start,end);
        assertEquals(2,rows.size());assertEquals(end,rows.get(0).outTime);assertEquals(-4,rows.get(0).interest,0);
        assertEquals(start+1,rows.get(1).time);assertEquals(0,rows.get(1).interest,0);
        OriginalLendRecord deleted=new OriginalLendRepository(store).byLocalRowId(first.localRowId);assertTrue(deleted.originalDeleted);
        assertEquals(start,deleted.outTime);assertEquals(3,deleted.interest,0);assertEquals(1,deleted.fromCost,0);
        assertEquals(199,deleted.repaymentAssetId);assertEquals(99,deleted.assetId);noFinancialReplay();
    }

    @Test public void persistedPositiveInterestAtBothRepaymentBoundariesReducesActualInstalmentServiceRemainderOnly(){
        long account=asset(0,99,2,-20);
        OriginalInstalmentRecord instalment=new OriginalInstalmentRecord();instalment.userId=0;instalment.instalmentId=501;
        instalment.assetId=99;instalment.periods=1;instalment.totalNumber=20;instalment.serviceNumber=10;instalment.serviceType=1;
        instalment.inAssetTime=january;new OriginalInstalmentRepository(store).saveOriginalRaw(instalment);
        long start=date("2025-01-02"),end=OriginalInstalmentSchedule.dayEnd(date("2025-02-01"));
        long[] times={start,end,january,january+1,start-1,end+1};double[] interests={3,4,-200,0,100,100};
        for(int i=0;i<times.length;i++){OriginalLendRecord row=lend(0,900+i,99,199,4,0,700,times[i]);row.interest=interests[i];save(row);}
        OriginalLendRecord foreign=lend(8,906,99,199,4,0,700,start);foreign.interest=100;save(foreign);
        OriginalLendRecord wrongAsset=lend(0,907,299,199,4,0,700,start);wrongAsset.interest=100;save(wrongAsset);
        OriginalLendRecord wrongType=lend(0,908,99,199,3,0,700,start);wrongType.interest=100;save(wrongType);
        rebuild(0,0,99);List<OriginalCreditBill> statements=statements(0,0,99);assertEquals(1,statements.size());
        OriginalCreditBill statement=statements.get(0);assertEquals("2025-02-01",statement.getRepaymentDay());
        assertEquals(30,statement.getNumber(),0);assertEquals(10,statement.getServiceCost(),0);
        assertEquals(3,statement.getRemainServiceCost(),0);assertEquals(23,statement.getRemindNumber(),0);
        assertEquals(1,statement.getInstalmentInfoList().size());assertEquals(-30,statement.getInstalmentInfoList().get(0).getNumber(),0);
        assertTrue(new OriginalCreditDataSource(store,0).lends(99).isEmpty());
        assertEquals(-20,store.assetRepository().byRowId(account).optDouble("asset_number"),0);
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));noFinancialReplay();
    }

    @Test public void restoredLendRowsRebuildTheirOriginalCreditAmountsWithoutReplayingAnAssetOrHistoryDelta(){
        long account=asset(0,99,2,-20);OriginalLendRecord row=lend(0,901,199,99,4,-5,90,january);
        rebuild(0,0,99);assertEquals(-5,statements(0,0,99).get(0).getNumber(),0);JSONObject backup=store.backup();
        row.fromCost=-9;save(row);rebuild(0,0,99);assertEquals(-9,statements(0,0,99).get(0).getNumber(),0);
        store.restore(backup);rebuild(0,0,99);assertEquals(-5,new OriginalCreditDataSource(store,0).lends(99).get(0).fromCost,0);
        assertEquals(-5,statements(0,0,99).get(0).getNumber(),0);
        OriginalLendRecord restored=new OriginalLendRepository(store).byIdentity(0,901);
        assertEquals(90,restored.number,0);assertEquals(row.localRowId,restored.localRowId);
        assertEquals(-20,store.assetRepository().byRowId(account).optDouble("asset_number"),0);
        assertEquals(1,store.scalar("SELECT COUNT(*) n FROM original_lends"));
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));noFinancialReplay();
    }
}
