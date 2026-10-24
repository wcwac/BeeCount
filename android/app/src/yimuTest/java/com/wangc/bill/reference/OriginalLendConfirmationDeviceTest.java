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

/** Store's checked repayment API and the independent receipt after original X's writes. */
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

    @Test public void arrivalWarningStillSavesTheInterestAndLendWithoutChangingThePrincipal() {
        prefs.edit().putInt("original_transfer_mode", 1).commit();
        JSONObject input = input("1", "2"); String id = candidate(input); AtomicInteger warnings = new AtomicInteger();
        OriginalLendRecord repayment = lend(store.saveCandidateCreditRepayment(input, id, warnings::incrementAndGet));
        assertEquals(1, warnings.get()); assertEquals(1, repayment.number, 0); assertEquals(2, repayment.interest, 0);
        assertEquals(98, balance(from), 0); assertEquals(5, balance(to), 0); assertEquals("SAVED", store.candidate(id).optString("state"));
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

    @Test public void signedAndZeroPrincipalBecomeAbsoluteLendNumbersWithoutABillAmountGate() {
        JSONObject negative = input("-10", ""); OriginalLendRecord first = lend(confirm(negative, candidate(negative)));
        assertEquals(10, first.number, 0); assertEquals(-1, first.billId); assertEquals(90, balance(from), 0); assertEquals(15, balance(to), 0);
        JSONObject zero = input("0", ""); assertEquals(0, lend(confirm(zero, candidate(zero))).number, 0);
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions"));
    }

    @Test public void theExplicitRepaymentRouteDoesNotRecheckTheFreshDestinationType() {
        JSONObject input = input("10", ""); String id = candidate(input);
        store.getWritableDatabase().execSQL("UPDATE accounts SET asset_type=1 WHERE id=?", new Object[]{to});
        assertEquals(4, lend(confirm(input, id)).type); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
    }

    @Test public void aRetainedMissingSourceCanReachTheOriginalWarningAndZeroInterestBranch() {
        prefs.edit().putInt("original_transfer_mode", 1).commit();
        JSONObject input = input(".1", ".5"); String id = candidate(input);
        store.getWritableDatabase().delete("accounts", "id=?", new String[]{"" + from});
        AtomicInteger warnings = new AtomicInteger(); OriginalLendRecord repayment = lend(store.saveCandidateCreditRepayment(input, id, warnings::incrementAndGet));
        assertEquals(1, warnings.get()); assertEquals(0, repayment.repaymentAssetId); assertEquals(.1, repayment.number, 0);
        assertEquals(0, repayment.interest, 0); assertEquals(-1, repayment.billId); assertEquals(5, balance(to), 0);
        JSONObject payload = Json.parse(store.candidate(id).optString("payload")); assertEquals(1001, payload.optLong("raw_asset_semantic_id"));
        store.restore(store.backup()); assertEquals(repayment.localRowId, confirm(input, id));
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

    @Test public void failedLendSaveRetainsEarlierPrincipalAndInterestWithoutAConfirmationReceipt() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_lend_failure BEFORE INSERT ON original_lends BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        JSONObject input = input("10", "2"); String id = candidate(input); expectPartial(input, id);
        assertEquals(88, balance(from), 0); assertEquals(15, balance(to), 0); assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_lends")); assertPendingWithoutReceipt(id);
    }

    @Test public void failedInterestSaveStillPersistsTheSourceLendAndEarlierBalances() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_interest_failure BEFORE INSERT ON transactions BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        JSONObject input = input("10", "2"); String id = candidate(input); expectPartial(input, id);
        assertEquals(88, balance(from), 0); assertEquals(15, balance(to), 0); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM original_lends")); assertPendingWithoutReceipt(id);
    }

    @Test public void ignoredReceiptInsertCannotUndoFinancialStagesOrMarkTheCandidateSaved() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_lend_receipt_ignored BEFORE INSERT ON candidate_lend_receipts BEGIN SELECT RAISE(IGNORE); END");
        JSONObject input = input("10", ""); String id = candidate(input); expectPartial(input, id);
        assertEquals(90, balance(from), 0); assertEquals(15, balance(to), 0); assertEquals(1, store.scalar("SELECT COUNT(*) n FROM original_lends"));
        assertPendingWithoutReceipt(id);
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
    private void expectPartial(JSONObject input, String id) {
        try { confirm(input, id); fail("partial financial save was reported as complete"); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("勿重复提交")); }
    }
    private void assertPendingWithoutReceipt(String id) {
        assertEquals("PENDING", store.candidate(id).optString("state")); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM candidate_lend_receipts"));
    }
}
