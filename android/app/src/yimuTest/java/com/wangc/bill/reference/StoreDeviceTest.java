package com.wangc.bill.reference;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;
import org.junit.*;
import org.junit.runner.RunWith;
import org.json.JSONObject;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public final class StoreDeviceTest {
    private ReferenceStore store;
    @Before public void setUp(){android.content.Context context=ApplicationProvider.getApplicationContext();context.deleteDatabase("reference_test.sqlite");store=new ReferenceStore(context,"reference_test.sqlite");store.getWritableDatabase();}
    @After public void close(){store.close();}
    private JSONObject draft(long amount){return Json.object("book_id",1,"kind","expense","amount",amount,"category_id",store.defaultCategory("expense"),"account_id",1,"time",System.currentTimeMillis());}
    @Test public void candidateConfirmationIsIdempotentAndBookFrozen(){JSONObject input=draft(1234);String candidate=store.addCandidate(input,"test.fixture",1);long id=store.saveTransaction(input,0,candidate);assertEquals(id,store.saveTransaction(input,0,candidate));assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transactions"));assertEquals(-1234,store.accountBalance(1));}
    @Test public void invalidCurrencyDoesNotConsumeCandidate(){long other=store.saveBook(0,"USD","USD",0);JSONObject input=draft(100);Json.put(input,"book_id",other);String candidate=store.addCandidate(input,"test.fixture",other);try{store.saveTransaction(input,0,candidate);fail();}catch(IllegalArgumentException expected){}assertEquals("PENDING",store.candidate(candidate).optString("state"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));}
    @Test public void transferConservesValueExceptFee(){JSONObject input=draft(500);Json.put(input,"kind","transfer");Json.put(input,"to_account_id",2);Json.put(input,"fee",3);store.saveTransaction(input,0,null);assertEquals(-503,store.accountBalance(1));assertEquals(500,store.accountBalance(2));assertEquals(3,store.totals(1,0,Long.MAX_VALUE).optLong("pay"));}
    @Test public void partialReturnCannotExceedOriginal(){JSONObject input=draft(1000);Json.put(input,"financial_type","lend");long id=store.saveTransaction(input,0,null);store.financialReturn(id,400,1,System.currentTimeMillis());assertEquals(600,store.outstanding(id));try{store.financialReturn(id,601,1,System.currentTimeMillis());fail();}catch(IllegalArgumentException expected){}assertEquals(600,store.outstanding(id));}
    @Test public void failedRestoreRollsBackEntireDatabase(){store.saveTransaction(draft(123),0,null);JSONObject backup=store.backup();Json.put(backup.optJSONObject("tables").optJSONArray("transactions").optJSONObject(0),"book_id",999);try{store.restore(backup);fail();}catch(Exception expected){}assertEquals(1,store.scalar("SELECT COUNT(*) n FROM transactions"));assertNotNull(store.book(1));}
    @Test public void successfulBackupRetainsCandidateLink(){JSONObject input=draft(500);String candidate=store.addCandidate(input,"test.fixture",1);long id=store.saveTransaction(input,0,candidate);JSONObject backup=store.backup();store.restore(backup);assertEquals(id,store.candidate(candidate).optLong("transaction_id"));assertEquals("SAVED",store.candidate(candidate).optString("state"));}
    @Test public void pendingCandidateFreezesCurrency(){store.addCandidate(draft(500),"test.fixture",1);try{store.saveBook(1,"日常账本","JPY",0);fail();}catch(IllegalArgumentException expected){}assertEquals("CNY",store.book(1).optString("currency"));}
    @Test public void monthlyPlanKeepsJanuaryAnchorThroughFebruary(){java.util.Calendar due=java.util.Calendar.getInstance();due.set(2025,0,31,9,0,0);due.set(java.util.Calendar.MILLISECOND,0);JSONObject payload=draft(100);Json.put(payload,"anchor_day",31);store.savePlan(0,"月末","monthly",payload,due.getTimeInMillis());java.util.Calendar end=java.util.Calendar.getInstance();end.set(2025,2,31,23,0,0);assertEquals(3,store.processPlans(end.getTimeInMillis()));java.util.Calendar next=java.util.Calendar.getInstance();next.setTimeInMillis(store.scalar("SELECT next_due n FROM plans"));assertEquals(java.util.Calendar.APRIL,next.get(java.util.Calendar.MONTH));assertEquals(30,next.get(java.util.Calendar.DAY_OF_MONTH));}
}
