package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Actual Store/Asset-command entry points with synthetic data; no pixel-equivalence claim. */
@RunWith(AndroidJUnit4.class)
public final class OriginalFinancialUiBridgeDeviceTest {
    private static final String DATABASE="reference_original_financial_bridge_test.sqlite";
    private static final long TIME=1735689600000L;
    private Context context;
    private ReferenceStore store;
    @Before public void open() {
        context=ApplicationProvider.getApplicationContext();context.deleteDatabase(DATABASE);
        store=new ReferenceStore(context,DATABASE);store.getWritableDatabase();
    }
    @After public void close(){if(store!=null)store.close();context.deleteDatabase(DATABASE);}
    private JSONObject input(long minor,long account) {
        return Json.object("book_id",store.books().get(0).optLong("id"),"kind","expense","amount",minor,
            "currency","CNY","category_id",store.categoryRepository().parent(99).optLong("id"),
            "account_id",account,"time",TIME,"remark","真实Store桥回归","fee",0,"discount",0);
    }
    private long fund(double number){return new OriginalAssetCommands(store,ReferenceStore.LOCAL_OFFLINE_OWNER).createFund("合成资金账户",null,number);}
    private double balance(long row){return store.assetRepository().byRowId(row).optDouble("asset_number");}
    private List<JSONObject> history(){return store.query("SELECT * FROM asset_histories ORDER BY id");}
    private void saveFails(JSONObject input,String candidate) {
        try{store.saveTransaction(input,0,candidate);fail("Expected an explicit partial-save failure");}
        catch(IllegalStateException expected){}
    }

    @Test public void ordinaryManualNoAccountStoresRawZeroAndTypeTwoWithoutInventingAnAccount() {
        long began=System.currentTimeMillis();long row=store.saveTransaction(input(500,0),0,null);
        OriginalBillRecord bill=new OriginalBillRepository(store).byLocalRowId(row);assertNotNull(bill);
        assertEquals(0,bill.assetId);assertEquals(2,bill.billType);assertEquals(5,bill.cost,0);assertEquals(TIME,bill.time);assertEquals("",bill.currencyInfo);
        assertTrue(bill.recordTime>=began);assertEquals(0,bill.inAssetTime);assertEquals(99,bill.parentCategoryId);assertEquals(-1,bill.childCategoryId);
        JSONObject projected=store.transaction(row);assertEquals("original",projected.optString("bill_origin"));assertTrue(projected.isNull("account_id"));
        assertEquals(0,projected.optLong("raw_asset_semantic_id"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM accounts"));assertTrue(history().isEmpty());
    }
    @Test public void automaticNoAccountKeepsMinusOneTypeFiveAndCandidateIdempotency() {
        JSONObject data=input(500,0);String candidate=store.addCandidate(data,"com.eg.android.AlipayGphone",data.optLong("book_id"));
        long row=store.saveTransaction(data,0,candidate);OriginalBillRecord bill=new OriginalBillRepository(store).byLocalRowId(row);
        assertEquals(-1,bill.assetId);assertEquals(5,bill.billType);assertEquals(0,bill.childCategoryId);assertNull(bill.currencyInfo);assertEquals("SAVED",store.candidate(candidate).optString("state"));
        assertEquals(row,store.candidate(candidate).optLong("transaction_id"));assertEquals(candidate,store.transaction(row).optString("candidate_id"));
        assertEquals(-1,Json.parse(store.candidate(candidate).optString("payload")).optLong("raw_asset_semantic_id"));
        assertEquals(row,store.saveTransaction(data,0,candidate));assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transactions"));assertTrue(history().isEmpty());
    }
    @Test public void realFundCreateExpenseAndEditUseStoredBalanceWithoutDoubleCountingTheLedger() {
        long account=fund(100);JSONObject data=input(500,account);long row=store.saveTransaction(data,0,null);
        assertEquals(95,balance(account),0);assertEquals(9500,store.accountBalance(account));
        JSONObject edit=store.transaction(row);Json.put(edit,"amount",700);store.saveTransaction(edit,row,null);
        assertEquals(93,balance(account),0);assertEquals(9300,store.accountBalance(account));assertEquals(700,store.transaction(row).optLong("amount"));
        List<JSONObject> history=history();assertEquals(3,history.size());
        assertEquals(100,history.get(0).optDouble("change_number"),0);assertEquals(100,history.get(0).optDouble("current_number"),0);
        assertEquals(-5,history.get(1).optDouble("change_number"),0);assertEquals(95,history.get(1).optDouble("current_number"),0);
        assertEquals(-2,history.get(2).optDouble("change_number"),0);assertEquals(93,history.get(2).optDouble("current_number"),0);
        assertEquals(0,store.assetRepository().byRowId(account).optLong("opening"));
    }
    @Test public void initialAndEditedFundHistoryUseGetterDifferencesAndDoNotEmitAnUnchangedAmount() {
        OriginalAssetCommands commands=new OriginalAssetCommands(store,0);long account=fund(0);assertTrue(history().isEmpty());
        commands.editFund(account,"合成资金账户重命名","CNY",0,0);assertTrue(history().isEmpty());
        commands.editFund(account,"合成资金账户重命名","CNY",12,0);assertEquals(12,balance(account),0);
        assertEquals(1,history().size());assertEquals(12,history().get(0).optDouble("change_number"),0);assertEquals(12,history().get(0).optDouble("current_number"),0);
        commands.editFund(account,"合成资金账户重命名","CNY",9,12);assertEquals(-3,history().get(1).optDouble("change_number"),0);assertEquals(9,balance(account),0);
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
    }
    @Test public void failedBillSaveRetainsEarlierBalanceHistoryAndLeavesItsCandidatePending() {
        long account=fund(100);JSONObject data=input(500,account);String candidate=store.addCandidate(data,"com.eg.android.AlipayGphone",data.optLong("book_id"));
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER fail_original_bill BEFORE INSERT ON transactions WHEN NEW.bill_origin='original' BEGIN SELECT RAISE(ABORT,'synthetic rejection'); END");
        saveFails(data,candidate);
        assertEquals(95,balance(account),0);assertEquals(2,history().size());assertEquals(-5,history().get(1).optDouble("change_number"),0);
        assertEquals("PENDING",store.candidate(candidate).optString("state"));assertEquals(0,store.candidate(candidate).optLong("transaction_id"));
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertTrue(store.assetRepository().categoryMapping(0,99,0).exists);assertFalse(store.assetRepository().categoryMapping(0,99,-1).exists);
    }
    @Test public void failedAssetSaveKeepsItsMutatedHistoryViewAndStillAllowsTheIndependentBillSave() {
        long account=fund(100);JSONObject data=input(500,account);String candidate=store.addCandidate(data,"com.eg.android.AlipayGphone",data.optLong("book_id"));
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER fail_original_asset BEFORE UPDATE OF asset_number ON accounts BEGIN SELECT RAISE(ABORT,'synthetic rejection'); END");
        long row=store.saveTransaction(data,0,candidate);
        assertEquals(100,balance(account),0);assertEquals(10000,store.accountBalance(account));assertEquals(500,store.transaction(row).optLong("amount"));
        assertEquals(2,history().size());assertEquals(-5,history().get(1).optDouble("change_number"),0);assertEquals(95,history().get(1).optDouble("current_number"),0);
        assertEquals("SAVED",store.candidate(candidate).optString("state"));assertEquals(row,store.candidate(candidate).optLong("transaction_id"));
    }
    @Test public void anOldLegacyBillStaysUnmappedWhenEditingItsOrdinaryFields() {
        ContentValues value=new ContentValues();value.put("book_id",1);value.put("kind","expense");value.put("amount",500);value.put("currency","CNY");
        value.put("category_id",store.categoryRepository().parent(99).optLong("id"));value.put("time",TIME);value.put("created",TIME);
        long row=store.getWritableDatabase().insertOrThrow("transactions",null,value);JSONObject edit=store.transaction(row);Json.put(edit,"amount",700);
        store.saveTransaction(edit,row,null);JSONObject saved=store.transaction(row);
        assertEquals("legacy",saved.optString("bill_origin"));assertEquals(700,saved.optLong("amount"));assertTrue(saved.isNull("bill_semantic_id"));assertTrue(saved.isNull("bill_cost"));
        assertNull(new OriginalBillRepository(store).byLocalRowId(row));assertTrue(history().isEmpty());
    }
    @Test public void anExplicitUnknownAssetIdentityNeverPromotesImportedDataToOriginalBill() {
        JSONObject data=input(500,0);Json.put(data,"owner_semantic_id",null);Json.put(data,"raw_asset_semantic_id",null);Json.put(data,"raw_to_asset_semantic_id",null);
        long row=store.saveTransaction(data,0,null);JSONObject saved=store.transaction(row);
        assertEquals("legacy",saved.optString("bill_origin"));assertTrue(saved.isNull("owner_semantic_id"));assertTrue(saved.isNull("raw_asset_semantic_id"));
        assertTrue(saved.isNull("bill_semantic_id"));assertTrue(saved.isNull("bill_user_id"));assertTrue(history().isEmpty());
    }
    @Test public void aKnownForeignAssetOwnerWithNoLocalFkCannotModifyTheOfflineOwnersSameSemanticAsset() {
        long account=fund(100),semantic=store.assetRepository().byRowId(account).optLong("semantic_id");JSONObject data=input(500,0);
        Json.put(data,"owner_semantic_id",7);Json.put(data,"raw_asset_semantic_id",semantic);
        try{store.saveTransaction(data,0,null);fail("Crossed the explicit asset-owner domain");}catch(IllegalArgumentException expected){}
        assertEquals(100,balance(account),0);assertEquals(1,history().size());assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
    }
    @Test public void ignoredCreatesKeepTheirHistoryAndExplicitCurrencyBillsRejectBeforeEffects() {
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER ignore_original_fund BEFORE INSERT ON accounts WHEN NEW.asset_origin='original' BEGIN SELECT RAISE(IGNORE); END");
        try{fund(100);fail("A negative insert result was reported as a saved account");}catch(IllegalStateException expected){}
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM accounts"));assertEquals(1,history().size());
        JSONObject history=history().get(0);assertEquals(100,history.optDouble("change_number"),0);assertEquals(100,history.optDouble("current_number"),0);
        assertEquals(0,history.optInt("owner_semantic_id"));assertTrue(history.optLong("asset_semantic_id")>=0);assertTrue(history.optLong("asset_semantic_id")<Integer.MAX_VALUE);
        store.getWritableDatabase().execSQL("DROP TRIGGER ignore_original_fund");
        for(String explicit:new String[]{"CNY","USD"}) {
            long account=new OriginalAssetCommands(store,0).createFund("显式币种元数据",explicit,10);
            int historyCount=history().size();
            try{store.saveTransaction(input(500,account),0,null);fail("Guessed a conversion for explicit currency "+explicit);}catch(IllegalArgumentException expected){}
            assertEquals(10,balance(account),0);assertEquals(historyCount,history().size());assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
        }
    }
    @Test public void actualPickerFiltersOriginalVisibilityTypeAndSemanticBookWhileDisplayingRawNullCurrency() {
        long book=store.books().get(0).optLong("id"),semanticBook=store.book(book).optLong("semantic_id"),visible=fund(10);
        AssetRepository.Definition hidden=new AssetRepository.Definition("隐藏合成账户",1,null);hidden.hide=true;
        long hiddenRow=store.assetRepository().insertRecovered(0,9000000001L,50,0,hidden);
        long credit=store.assetRepository().insertRecovered(0,9000000002L,49,0,new AssetRepository.Definition("报销合成账户",9,null));
        AssetRepository.Definition foreignBook=new AssetRepository.Definition("别本合成账户",1,null);foreignBook.showBook.add(semanticBook+1000);
        long excluded=store.assetRepository().insertRecovered(0,9000000003L,48,0,foreignBook);
        AssetRepository.Definition allowed=new AssetRepository.Definition("本账本合成账户",1,null);allowed.showBook.add(semanticBook);
        long included=store.assetRepository().insertRecovered(0,9000000004L,47,0,allowed);
        List<JSONObject> selected=store.selectableAccounts(book);
        assertEquals(2,selected.size());assertEquals(included,selected.get(0).optLong("id"));assertEquals(visible,selected.get(1).optLong("id"));
        for(JSONObject row:selected){assertEquals("CNY",row.optString("currency"));assertNotEquals(hiddenRow,row.optLong("id"));assertNotEquals(credit,row.optLong("id"));assertNotEquals(excluded,row.optLong("id"));}
        assertNotNull(store.accountInDisplayCurrency(visible,"CNY"));assertNull(store.accountInDisplayCurrency(visible,"USD"));
        assertEquals("",store.assetRepository().byRowId(visible).optString("currency"));assertTrue(store.assetRepository().byRowId(visible).isNull("asset_currency"));
    }
    @Test public void repeatedTrashAndRestoreOnlyApplyEachBalanceStageOnceAndPurgeDoesNotReverseAgain() {
        long account=fund(100),row=store.saveTransaction(input(500,account),0,null);assertEquals(95,balance(account),0);
        store.trash(row,true);store.trash(row,true);assertEquals(100,balance(account),0);assertEquals(3,history().size());
        store.trash(row,false);store.trash(row,false);assertEquals(95,balance(account),0);assertEquals(4,history().size());
        assertEquals(0,store.transaction(row).optInt("deleted"));
        store.trash(row,true);assertEquals(100,balance(account),0);assertEquals(5,history().size());store.purge(row);
        assertNull(store.transaction(row));assertEquals(100,balance(account),0);assertEquals(5,history().size());
        assertEquals(5,history().get(2).optDouble("change_number"),0);assertEquals(-5,history().get(3).optDouble("change_number"),0);
    }
    @Test public void canonicalRepeatPhysicallyReplacesItsOldBillAndCandidatesWithoutDoubleApplyingOnRetry() {
        long account=fund(100);JSONObject first=input(500,account);
        String oldCandidate=store.addCandidate(first,"com.eg.android.AlipayGphone",first.optLong("book_id"));long old=store.saveTransaction(first,0,oldCandidate);
        JSONObject next=input(700,account);Json.put(next,"original_number","5.00");Json.put(next,"original_time",TIME);
        Json.put(next,"original_income",false);Json.put(next,"original_currency","CNY");
        String newCandidate=store.addCandidate(next,"com.eg.android.AlipayGphone",next.optLong("book_id"));
        assertEquals(1,store.findRepeatTransactions(next).size());long replacement=store.saveCandidateReplacing(next,newCandidate,old);
        assertNotEquals(old,replacement);assertNull(store.transaction(old));assertEquals(700,store.transaction(replacement).optLong("amount"));
        assertEquals(93,balance(account),0);assertEquals(4,history().size());
        assertEquals("DISMISSED",store.candidate(oldCandidate).optString("state"));assertEquals(0,store.candidate(oldCandidate).optLong("transaction_id"));
        assertEquals("SAVED",store.candidate(newCandidate).optString("state"));assertEquals(replacement,store.candidate(newCandidate).optLong("transaction_id"));
        assertEquals(5,history().get(2).optDouble("change_number"),0);assertEquals(-7,history().get(3).optDouble("change_number"),0);
        assertEquals(replacement,store.saveCandidateReplacing(next,newCandidate,old));assertEquals(93,balance(account),0);assertEquals(4,history().size());
    }
}
