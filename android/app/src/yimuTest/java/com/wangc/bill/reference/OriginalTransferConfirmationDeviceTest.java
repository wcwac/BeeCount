package com.wangc.bill.reference;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Atomic user-entry dispatcher, typed receipt, fee book and safe retry; raw commands are tested separately. */
@RunWith(AndroidJUnit4.class)
public final class OriginalTransferConfirmationDeviceTest {
    private static final String DATABASE = "reference_transfer_confirmation_test.sqlite";
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
        from = asset(1001, "转出合成账户", 100); to = asset(1002, "转入合成账户", 5);
    }
    @After public void close() {
        if (store != null) store.close(); context.deleteDatabase(DATABASE);
        prefs.edit().putLong("current_book", priorBook).putInt("original_transfer_mode", priorMode).commit();
    }
    private long asset(long semantic, String name, double balance) {
        AssetRepository.Definition d = new AssetRepository.Definition(name, 1, null); d.assetNumber = balance;
        return store.assetRepository().insertRecovered(0, semantic, 1, 0, d);
    }
    private double balance(long row) { return store.assetRepository().byRowId(row).optDouble("asset_number"); }
    private JSONObject input(String amount, String fee) {
        return Json.object("book_id", book, "kind", "transfer", "amount", 1000, "currency", "CNY", "time", 1735689600000L,
                "account_id", from, "to_account_id", to, "remark", "原始转账", "transfer_amount_text", amount, "transfer_fee_text", fee);
    }
    private String candidate(JSONObject input) { return store.addCandidate(input, "com.eg.android.AlipayGphone", input.optLong("book_id")); }
    private long confirm(JSONObject input, String id) { return store.saveCandidateTransfer(input, id, () -> {}); }

    @Test public void principalAndFeeUseSeparateOriginalRecordsAndATypedIdempotentReceipt() {
        JSONObject input = input("10", "2"); String id = candidate(input); long row = confirm(input, id);
        OriginalTransferRecord transfer = new OriginalTransferRepository(store).byLocalRowId(row);
        assertEquals(10.0d, transfer.cost, 0); assertEquals(2.0d, transfer.serviceCharge, 0);
        assertEquals(88.0d, balance(from), 0); assertEquals(15.0d, balance(to), 0);
        OriginalBillRecord fee = new OriginalBillRepository(store).byExactUser(0, transfer.billId);
        assertNotNull(fee); assertEquals(2.0d, fee.cost, 0); assertEquals("转出合成账户 转账手续费", fee.remark);
        assertEquals(99, fee.parentCategoryId); assertEquals(0, fee.childCategoryId);
        assertTrue(store.transaction(fee.localRowId).isNull("candidate_id"));
        JSONObject saved = store.candidate(id); assertEquals("transfer", saved.optString("save_kind"));
        assertTrue(saved.isNull("transaction_id")); assertEquals(row, store.one("SELECT * FROM candidate_transfer_receipts WHERE candidate_id=?", id).optLong("transfer_row_id"));
        long history = store.scalar("SELECT COUNT(*) n FROM asset_histories");
        assertEquals(row, confirm(input, id)); assertEquals(history, store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transactions")); assertEquals(1, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        store.restore(store.backup()); assertEquals(row, confirm(input, id));
    }
    @Test public void feeBookComesFromTheSourcesOrderedShowBookInsteadOfTheOverlayHeader() {
        long feeBook = store.saveBook(0, "合成手续费账本", "CNY", 0);
        long header = store.saveBook(0, "合成浮窗账本", "CNY", 0);
        long semantic = store.book(feeBook).optLong("semantic_id");
        store.assetRepository().setShowBook(from, Arrays.asList(999999999999L, semantic));
        JSONObject input = input("10", "-2"); Json.put(input, "book_id", header);
        String id = candidate(input); long row = confirm(input, id);
        OriginalTransferRecord transfer = new OriginalTransferRepository(store).byLocalRowId(row);
        OriginalBillRecord fee = new OriginalBillRepository(store).byExactUser(0, transfer.billId);
        assertEquals(semantic, fee.bookId); assertEquals(feeBook, store.transaction(fee.localRowId).optLong("book_id"));
        assertEquals(header, store.candidate(id).optLong("book_id")); assertEquals(9, fee.parentCategoryId); assertEquals(911, fee.childCategoryId);
        assertEquals("转出合成账户 转账优惠", fee.remark); assertEquals(92.0d, balance(from), 0); assertEquals(15.0d, balance(to), 0);
    }
    @Test public void nonPositiveArrivalIsRejectedBeforeWarningsOrAnyTransferEffects() {
        prefs.edit().putInt("original_transfer_mode",1).commit();
        for(String amount:new String[]{"1","2"}) {
            JSONObject input=input(amount,"2");String id=candidate(input);AtomicInteger warnings=new AtomicInteger();
            JSONObject before=store.backup().optJSONObject("tables");
            try{store.saveCandidateTransfer(input,id,warnings::incrementAndGet);fail("Saved a nonpositive arrival");}
            catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains("到账"));}
            assertEquals(0,warnings.get());assertEquals(before.toString(),store.backup().optJSONObject("tables").toString());
            assertEquals(100,balance(from),0);assertEquals(5,balance(to),0);assertEquals("PENDING",store.candidate(id).optString("state"));
        }
    }
    @Test public void nonPositiveTransferPrincipalIsRejectedWithoutAnyFinancialMutation() {
        for(String amount:new String[]{"-10","0","-0.00"}) {
            JSONObject input=input(amount,"");String id=candidate(input);JSONObject before=store.backup().optJSONObject("tables");
            try{confirm(input,id);fail("Saved a nonpositive transfer principal");}
            catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains("本金"));}
            assertEquals(before.toString(),store.backup().optJSONObject("tables").toString());
            assertEquals(100,balance(from),0);assertEquals(5,balance(to),0);assertEquals("PENDING",store.candidate(id).optString("state"));
        }
        assertEquals(0,store.scalar("SELECT COUNT(*) n FROM transactions"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM original_transfers"));
    }
    @Test public void checkedRepaymentAndUnportedFilesFailBeforeAnyFinancialMutation() {
        store.getWritableDatabase().execSQL("UPDATE accounts SET asset_type=2 WHERE id=?", new Object[]{to});
        JSONObject input = input("10", "2"); Json.put(input, "transfer_repayment", true); String id = candidate(input);
        try { confirm(input, id); fail("repayment was misrouted to Transfer"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("借贷")); }
        assertEquals(100.0d, balance(from), 0); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        Json.put(input, "transfer_repayment", false); Json.put(input, "attachments", new org.json.JSONArray().put("synthetic"));
        try { confirm(input, id); fail("host JSON files were treated as original BillFile rows"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("附件")); }
        assertEquals(100.0d, balance(from), 0); assertEquals("PENDING", store.candidate(id).optString("state"));
    }
    @Test public void explicitFalseRouteAndLegacyNonCreditSelectionStayOrdinaryTransfers() {
        JSONObject legacy = input("10", ""); Json.put(legacy, "transfer_repayment", true);
        assertNotNull(new OriginalTransferRepository(store).byLocalRowId(confirm(legacy, candidate(legacy))));
        store.getWritableDatabase().execSQL("UPDATE accounts SET asset_type=2 WHERE id=?", new Object[]{to});
        JSONObject retained = input("5", ""); Json.put(retained, "transfer_repayment", true);
        Json.put(retained, "transfer_repayment_route", false);
        assertNotNull(new OriginalTransferRepository(store).byLocalRowId(confirm(retained, candidate(retained))));
        assertEquals(85.0d, balance(from), 0); assertEquals(20.0d, balance(to), 0);
        assertEquals(2, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_lends"));
    }
    @Test public void failedTransferSaveRollsBackPrincipalAndFeeAndCanRetryOnce() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_transfer_save_failure BEFORE INSERT ON original_transfers BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        JSONObject input=input("10","2");String id=candidate(input);
        assertRollbackAndRetry(input,id,"synthetic_transfer_save_failure",88,2);
    }
    @Test public void receiptFailureRollsBackTheTransferAndRetryDoesNotDebitTwice() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_transfer_receipt_failure BEFORE INSERT ON candidate_transfer_receipts BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        JSONObject input=input("10","");String id=candidate(input);
        assertRollbackAndRetry(input,id,"synthetic_transfer_receipt_failure",90,0);
    }
    @Test public void restoringAMissingOrTruncatedSemanticIdentityRejectsAndRollsBackTheWholeBackup() {
        JSONObject input = input("10", ""); String id = candidate(input); confirm(input, id); JSONObject backup = store.backup();
        JSONObject saved = backup.optJSONObject("tables").optJSONArray("candidates").optJSONObject(0);
        JSONObject payload = Json.parse(saved.optString("payload")); payload.remove("transfer_owner_semantic_id"); Json.put(saved, "payload", payload.toString());
        try { store.restore(backup); fail("missing owner zero was accepted by optInt fallback"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("转账")); }
        assertEquals("SAVED", store.candidate(id).optString("state")); assertEquals(1, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
    }

    @Test public void anIgnoredReceiptInsertRollsBackTheTransferBeforeAnIdempotentRetry() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_transfer_receipt_ignored BEFORE INSERT ON candidate_transfer_receipts BEGIN SELECT RAISE(IGNORE); END");
        JSONObject input=input("10","");String id=candidate(input);
        assertRollbackAndRetry(input,id,"synthetic_transfer_receipt_ignored",90,0);
    }

    @Test public void savedTransferReceiptsCannotBeReplayedThroughTheBillRefundOrRepeatEntryPoints() {
        JSONObject input = input("10", ""); String id = candidate(input); long row = confirm(input, id);
        long history = store.scalar("SELECT COUNT(*) n FROM asset_histories");
        try { store.saveTransaction(input, 0, id); fail("Transfer local PK escaped as a Bill PK"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("转账")); }
        try { store.saveCandidateRefund(input, id, null, null); fail("Transfer was treated as an existing refund"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("转账")); }
        try { store.saveCandidateReplacing(input, id, row); fail("Transfer was treated as a Bill repeat"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("转账")); }
        assertEquals(history, store.scalar("SELECT COUNT(*) n FROM asset_histories")); assertEquals(row, confirm(input, id));
    }
    private void assertRollbackAndRetry(JSONObject input,String id,String trigger,double expectedFrom,double fee) {
        JSONObject before=store.backup().optJSONObject("tables");
        try{confirm(input,id);fail("The injected SQL failure was reported as saved");}catch(RuntimeException expected){}
        assertEquals(before.toString(),store.backup().optJSONObject("tables").toString());
        assertEquals(100,balance(from),0);assertEquals(5,balance(to),0);
        assertEquals("PENDING",store.candidate(id).optString("state"));assertEquals(0,store.scalar("SELECT COUNT(*) n FROM candidate_transfer_receipts"));
        store.getWritableDatabase().execSQL("DROP TRIGGER "+trigger);
        long row=confirm(input,id);OriginalTransferRecord transfer=new OriginalTransferRepository(store).byLocalRowId(row);
        assertNotNull(transfer);assertEquals(10,transfer.cost,0);assertEquals(fee,transfer.serviceCharge,0);
        assertEquals(expectedFrom,balance(from),0);assertEquals(15,balance(to),0);
        assertEquals(1,store.scalar("SELECT COUNT(*) n FROM original_transfers"));assertEquals(fee==0?0:1,store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals("SAVED",store.candidate(id).optString("state"));assertTrue(store.candidate(id).isNull("transaction_id"));
        assertEquals(row,store.one("SELECT transfer_row_id FROM candidate_transfer_receipts WHERE candidate_id=?",id).optLong("transfer_row_id"));
        JSONObject committed=store.backup().optJSONObject("tables");assertEquals(row,confirm(input,id));
        assertEquals(committed.toString(),store.backup().optJSONObject("tables").toString());
    }

}
