package com.wangc.bill.reference;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Atomic checked repayment API and idempotent receipt; raw X stages are tested separately. */
@RunWith(AndroidJUnit4.class)
public final class OriginalLendConfirmationDeviceTest {
    private static final String DATABASE = "reference_lend_confirmation_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private SharedPreferences prefs;
    private long priorBook, book, from, to;
    private int priorMode;
    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        prefs = context.getSharedPreferences("reference_settings", Context.MODE_PRIVATE);
        priorBook = prefs.getLong("current_book", 1); priorMode = prefs.getInt("original_transfer_mode", 0);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase(); book = store.books().get(0).optLong("id");
        prefs.edit().putLong("current_book", book).putInt("original_transfer_mode", 0).commit();
        from = asset(1001, "还款来源合成账户", 1, 100); to = asset(1002, "收款合成信贷账户", 2, 5);
    }
    @After public void close() {
        if (store != null) store.close(); context.deleteDatabase(DATABASE);
        prefs.edit().putLong("current_book", priorBook).putInt("original_transfer_mode", priorMode).commit();
    }
    private long asset(long semantic, String name, int type, double balance) {
        AssetRepository.Definition d = new AssetRepository.Definition(name, type, null); d.assetNumber = balance;
        return store.assetRepository().insertRecovered(0, semantic, 1, 0, d);
    }
    private double balance(long row) { return store.assetRepository().byRowId(row).optDouble("asset_number"); }
    private JSONObject input(String amount, String fee) {
        JSONObject input = Json.object("book_id", book, "kind", "transfer", "amount", 1000, "currency", "CNY",
                "time", 1735689600000L, "remark", "原始还款", "transfer_amount_text", amount, "transfer_fee_text", fee,
                "transfer_repayment", true, "transfer_repayment_route", true);
        store.selectAccountForInput(input, "account_id", from, true);
        store.selectAccountForInput(input, "to_account_id", to, true); return input;
    }
    private String candidate(JSONObject input) { return store.addCandidate(input, "com.eg.android.AlipayGphone", input.optLong("book_id")); }
    private long confirm(JSONObject input, String id) { return store.saveCandidateCreditRepayment(input, id, () -> {}); }
    private OriginalLendRecord lend(long row) { return new OriginalLendRepository(store).byLocalRowId(row); }

    @Test public void principalInterestAndRepaymentUseIndependentRecordsAndATypedIdempotentReceipt() {
        JSONObject input = input("10", "2"); String id = candidate(input); long row = confirm(input, id);
        OriginalLendRecord repayment = lend(row);
        assertEquals(4, repayment.type); assertEquals(1002, repayment.assetId); assertEquals(1001, repayment.repaymentAssetId);
        assertEquals(10, repayment.number, 0); assertEquals(0, repayment.fromCost, 0); assertEquals(2, repayment.interest, 0);
        assertEquals("原始还款", repayment.remark); assertEquals(88, balance(from), 0); assertEquals(15, balance(to), 0);
        OriginalBillRecord fee = new OriginalBillRepository(store).byExactUser(0, repayment.billId);
        assertNotNull(fee); assertEquals("收款合成信贷账户 还款利息", fee.remark); assertEquals(2, fee.cost, 0);
        assertTrue(store.transaction(fee.localRowId).isNull("candidate_id"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        JSONObject saved = store.candidate(id); assertEquals("lend", saved.optString("save_kind")); assertTrue(saved.isNull("transaction_id"));
        JSONObject receipt = store.one("SELECT * FROM candidate_lend_receipts WHERE candidate_id=?", id);
        assertEquals(row, receipt.optLong("lend_row_id")); assertEquals(row, receipt.optLong("resolved_lend_row_id"));
        long history = store.scalar("SELECT COUNT(*) n FROM asset_histories");
        assertEquals(row, confirm(input, id)); assertEquals(history, store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        store.restore(store.backup()); assertEquals(row, confirm(input, id)); assertEquals(history, store.scalar("SELECT COUNT(*) n FROM asset_histories"));
    }

    @Test public void interestBookUsesSourceShowBookWhileReceiptRetainsTheOverlayHeader() {
        long feeBook = store.saveBook(0, "合成利息账本", "CNY", 0), header = store.saveBook(0, "合成浮窗账本", "CNY", 0);
        long semantic = store.book(feeBook).optLong("semantic_id"); store.assetRepository().setShowBook(from, Arrays.asList(-9L, semantic));
        JSONObject input = input("10", "-2"); Json.put(input, "book_id", header);
        String id = candidate(input); OriginalLendRecord repayment = lend(confirm(input, id));
        OriginalBillRecord discount = new OriginalBillRepository(store).byExactUser(0, repayment.billId);
        assertEquals(semantic, discount.bookId); assertEquals(feeBook, store.transaction(discount.localRowId).optLong("book_id"));
        assertEquals(header, store.candidate(id).optLong("book_id")); assertEquals(9, discount.parentCategoryId); assertEquals(911, discount.childCategoryId);
        assertEquals("收款合成信贷账户 还款优惠", discount.remark); assertEquals(92, balance(from), 0); assertEquals(15, balance(to), 0);
    }

    @Test public void nonPositiveRepaymentArrivalRejectsBeforeWarningsOrAnyFinancialEffects() {
        prefs.edit().putInt("original_transfer_mode",1).commit();
        for(String amount:new String[]{"1","2"}) {
            JSONObject input=input(amount,"2");String id=candidate(input);AtomicInteger warnings=new AtomicInteger();
            JSONObject before=store.backup().optJSONObject("tables");
            try{store.saveCandidateCreditRepayment(input,id,warnings::incrementAndGet);fail("Saved a nonpositive repayment arrival");}
            catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains("到账"));}
            assertEquals(0,warnings.get());assertEquals(before.toString(),store.backup().optJSONObject("tables").toString());
            assertEquals(100,balance(from),0);assertEquals(5,balance(to),0);assertPendingWithoutReceipt(id);
        }
    }

    @Test public void differingFeePredicatesPreservePrincipalDeductionAndInterestClassification() {
        prefs.edit().putInt("original_transfer_mode", 1).commit();
        JSONObject decimal = input("10", ".5"); OriginalLendRecord first = lend(confirm(decimal, candidate(decimal)));
        assertEquals(9.5, first.number, 0); assertEquals(0, first.interest, 0); assertEquals(-1, first.billId);
        JSONObject comma = input("10", "1,2"); OriginalLendRecord second = lend(confirm(comma, candidate(comma)));
        assertEquals(10, second.number, 0); assertEquals(1.2, second.interest, 0); assertTrue(second.billId >= 0);
        assertEquals(79.3, balance(from), .0000001); assertEquals(24.5, balance(to), 0);
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transactions"));
    }

    @Test public void nonPositiveRepaymentPrincipalRejectsWithoutCreatingAnAbsoluteDebtRecord() {
        for(String amount:new String[]{"-10","0","-0.00"}) {
            JSONObject input=input(amount,"");String id=candidate(input);JSONObject before=store.backup().optJSONObject("tables");
            try{confirm(input,id);fail("Saved a nonpositive repayment principal");}
            catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains("本金"));}
            assertEquals(before.toString(),store.backup().optJSONObject("tables").toString());
            assertEquals(100,balance(from),0);assertEquals(5,balance(to),0);assertPendingWithoutReceipt(id);
        }
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM original_lends"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
    }

    @Test public void theExplicitRepaymentRouteDoesNotRecheckTheFreshDestinationType() {
        JSONObject input = input("10", ""); String id = candidate(input);
        store.getWritableDatabase().execSQL("UPDATE accounts SET asset_type=1 WHERE id=?", new Object[]{to});
        assertEquals(4, lend(confirm(input, id)).type); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
    }

    @Test public void aRetainedMissingSourceCannotCreateARepaymentWhoseArrivalWouldBeSkipped() {
        prefs.edit().putInt("original_transfer_mode",1).commit();
        JSONObject input=input(".1",".5");String id=candidate(input);
        store.getWritableDatabase().delete("accounts","id=?",new String[]{""+from});
        JSONObject before=store.backup().optJSONObject("tables");AtomicInteger warnings=new AtomicInteger();
        try{store.saveCandidateCreditRepayment(input,id,warnings::incrementAndGet);fail("Saved a skipped-principal repayment");}
        catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains("到账"));}
        assertEquals(0,warnings.get());assertEquals(5,balance(to),0);assertPendingWithoutReceipt(id);
        assertEquals(before.toString(),store.backup().optJSONObject("tables").toString());
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM original_lends"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(1001,Json.parse(store.candidate(id).optString("payload")).optLong("raw_asset_semantic_id"));
    }

    @Test public void filesForeignCurrencyAndInvalidHostInputsRejectBeforeFinancialChanges() {
        JSONObject input = input("10", "2"); String id = candidate(input); Json.put(input, "attachments", new JSONArray().put("synthetic"));
        expectRejected(input, id); input.remove("attachments"); Json.put(input, "transfer_amount_text", "NaN"); expectRejected(input, id);
        Json.put(input, "transfer_amount_text", "10"); Json.put(input, "time", 0); expectRejected(input, id);
        Json.put(input, "time", 1735689600000L); store.getWritableDatabase().execSQL("UPDATE accounts SET asset_currency='USD' WHERE id=?", new Object[]{from});
        expectRejected(input, id); assertEquals(100, balance(from), 0); assertEquals(5, balance(to), 0);
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_lends")); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals("PENDING", store.candidate(id).optString("state"));
    }

    @Test public void failedLendSaveRollsBackPrincipalAndInterestBeforeASafeRetry() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_lend_failure BEFORE INSERT ON original_lends BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        JSONObject input=input("10","2");String id=candidate(input);
        assertRollbackAndRetry(input,id,"synthetic_lend_failure",88,2);
    }

    @Test public void failedInterestSaveCannotLeaveALendOrBalanceChangeAndCanRetryOnce() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_interest_failure BEFORE INSERT ON transactions BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        JSONObject input=input("10","2");String id=candidate(input);
        assertRollbackAndRetry(input,id,"synthetic_interest_failure",88,2);
    }

    @Test public void anIgnoredRepaymentReceiptRollsBackAllFinancialStagesBeforeRetry() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_lend_receipt_ignored BEFORE INSERT ON candidate_lend_receipts BEGIN SELECT RAISE(IGNORE); END");
        JSONObject input=input("10","");String id=candidate(input);
        assertRollbackAndRetry(input,id,"synthetic_lend_receipt_ignored",90,0);
    }

    @Test public void repaymentReceiptsCannotEscapeThroughBillRefundRepeatOrTransferApis() {
        JSONObject input = input("10", ""); String id = candidate(input); long row = confirm(input, id);
        long history = store.scalar("SELECT COUNT(*) n FROM asset_histories");
        try { store.saveTransaction(input, 0, id); fail("Lend key was returned as a Bill key"); } catch (IllegalArgumentException expected) {}
        try { store.saveCandidateRefund(input, id, null, null); fail("Lend was treated as Refund"); } catch (IllegalArgumentException expected) {}
        try { store.saveCandidateRefundReplacing(input, id, null, null, row); fail("Lend was treated as Refund repeat"); } catch (IllegalArgumentException expected) {}
        try { store.saveCandidateReplacing(input, id, row); fail("Lend was treated as Bill repeat"); } catch (IllegalArgumentException expected) {}
        try { store.saveCandidateTransfer(input, id, null); fail("Lend key was returned as a Transfer key"); } catch (IllegalArgumentException expected) {}
        assertEquals(history, store.scalar("SELECT COUNT(*) n FROM asset_histories")); assertEquals(row, confirm(input, id));
    }

    private void expectRejected(JSONObject input, String id) {
        try { confirm(input, id); fail("invalid host input changed financial state"); } catch (IllegalArgumentException expected) {}
    }
    private void assertPendingWithoutReceipt(String id) {
        assertEquals("PENDING", store.candidate(id).optString("state")); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM candidate_lend_receipts"));
    }
    private void assertRollbackAndRetry(JSONObject input,String id,String trigger,double expectedFrom,double interest) {
        JSONObject before=store.backup().optJSONObject("tables");
        try{confirm(input,id);fail("The injected SQL failure was reported as saved");}catch(RuntimeException expected){}
        assertEquals(before.toString(),store.backup().optJSONObject("tables").toString());
        assertEquals(100,balance(from),0);assertEquals(5,balance(to),0);assertPendingWithoutReceipt(id);
        store.getWritableDatabase().execSQL("DROP TRIGGER "+trigger);
        long row=confirm(input,id);OriginalLendRecord repayment=lend(row);assertNotNull(repayment);
        assertEquals(4,repayment.type);assertEquals(10,repayment.number,0);assertEquals(interest,repayment.interest,0);
        assertEquals(expectedFrom,balance(from),0);assertEquals(15,balance(to),0);
        assertEquals(1,store.scalar("SELECT COUNT(*) n FROM original_lends"));assertEquals(interest==0?0:1,store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals("SAVED",store.candidate(id).optString("state"));assertTrue(store.candidate(id).isNull("transaction_id"));
        assertEquals(row,store.one("SELECT lend_row_id FROM candidate_lend_receipts WHERE candidate_id=?",id).optLong("lend_row_id"));
        JSONObject committed=store.backup().optJSONObject("tables");assertEquals(row,confirm(input,id));
        assertEquals(committed.toString(),store.backup().optJSONObject("tables").toString());
    }

}
