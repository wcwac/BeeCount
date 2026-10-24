package com.wangc.bill.reference;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real SQLite user-entry commit/retry tests, not claims about raw source save ordering or UI traversal. */
@RunWith(AndroidJUnit4.class)
public final class FunctionalAccountingCommitDeviceTest {
    private static final String DATABASE = "reference_functional_accounting_commit.sqlite";
    private static final long TIME = 1750000000000L;
    private Context context;
    private ReferenceStore store;
    private SharedPreferences prefs;
    private boolean hadBook, hadMode;
    private long priorBook, book, from, to;
    private int priorMode;

    @Before public void open() {
        assertEquals("true", InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        prefs = context.getSharedPreferences("reference_settings", Context.MODE_PRIVATE);
        hadBook = prefs.contains("current_book"); priorBook = prefs.getLong("current_book", 1);
        hadMode = prefs.contains("original_transfer_mode"); priorMode = prefs.getInt("original_transfer_mode", 0);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase();
        book = store.books().get(0).optLong("id");
        prefs.edit().putLong("current_book", book).putInt("original_transfer_mode", 0).commit();
        OriginalAssetCommands assets = new OriginalAssetCommands(store, ReferenceStore.LOCAL_OFFLINE_OWNER);
        from = assets.createFund("功能提交转出账户", null, 100);
        to = assets.createFund("功能提交转入账户", null, 50);
    }

    @After public void close() {
        try { if (store != null) store.close(); }
        finally {
            if (context != null) context.deleteDatabase(DATABASE);
            if (prefs != null) {
                SharedPreferences.Editor edit = prefs.edit();
                if (hadBook) edit.putLong("current_book", priorBook); else edit.remove("current_book");
                if (hadMode) edit.putInt("original_transfer_mode", priorMode); else edit.remove("original_transfer_mode");
                edit.commit();
            }
        }
    }

    @Test public void candidateStateFailureRollsBackTheBillAndRetryDeductsItsAccountOnlyOnce() {
        JSONObject value = expense(); String candidate = candidate(value);
        JSONObject before = store.backup().optJSONObject("tables");
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER reject_saved_candidate BEFORE UPDATE OF state ON candidates "
                + "WHEN NEW.state='SAVED' BEGIN SELECT RAISE(ABORT,'synthetic candidate state failure'); END");
        fails(() -> store.saveTransaction(value, 0, candidate));
        assertUnchanged(before, candidate);
        store.getWritableDatabase().execSQL("DROP TRIGGER reject_saved_candidate");
        long row = store.saveTransaction(value, 0, candidate);
        assertExpense(row, candidate);
        JSONObject committed = store.backup().optJSONObject("tables");
        assertEquals(row, store.saveTransaction(value, 0, candidate));
        assertEquals(committed.toString(), store.backup().optJSONObject("tables").toString());
    }

    @Test public void lateTagFailureDoesNotLeaveABalanceChangeOrBillBeforeASafeCandidateRetry() {
        long tag = store.saveTag("功能提交标签");
        JSONObject value = expense(); Json.put(value, "tag_ids", new JSONArray().put(tag));
        String candidate = candidate(value); JSONObject before = store.backup().optJSONObject("tables");
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER reject_bill_tag BEFORE INSERT ON transaction_tags "
                + "BEGIN SELECT RAISE(ABORT,'synthetic late tag failure'); END");
        fails(() -> store.saveTransaction(value, 0, candidate));
        assertUnchanged(before, candidate);
        store.getWritableDatabase().execSQL("DROP TRIGGER reject_bill_tag");
        long row = store.saveTransaction(value, 0, candidate); assertExpense(row, candidate);
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transaction_tags WHERE transaction_id=? AND tag_id=?", "" + row, "" + tag));
        long histories = store.scalar("SELECT COUNT(*) n FROM asset_histories");
        assertEquals(row, store.saveTransaction(value, 0, candidate));
        assertEquals(histories, store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transaction_tags"));
    }

    @Test public void transferReceiptFailureRollsBackPrincipalAndFeeBeforeAnIdempotentRetry() {
        JSONObject value = Json.object("book_id", book, "kind", "transfer", "currency", "CNY", "amount", 1000,
                "time", TIME, "remark", "功能提交转账", "transfer_amount_text", "10.00", "transfer_fee_text", "1.00",
                "transfer_repayment_route", false);
        store.selectAccountForInput(value, "account_id", from, true);
        store.selectAccountForInput(value, "to_account_id", to, true);
        String candidate = candidate(value); JSONObject before = store.backup().optJSONObject("tables");
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER reject_transfer_receipt BEFORE INSERT ON candidate_transfer_receipts "
                + "BEGIN SELECT RAISE(ABORT,'synthetic receipt failure'); END");
        fails(() -> store.saveCandidateTransfer(value, candidate, () -> {}));
        assertUnchanged(before, candidate);
        store.getWritableDatabase().execSQL("DROP TRIGGER reject_transfer_receipt");
        long row = store.saveCandidateTransfer(value, candidate, () -> {});
        assertEquals(89d, balance(from), 0d); assertEquals(60d, balance(to), 0d);
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transactions"));
        OriginalTransferRecord transfer = new OriginalTransferRepository(store).byLocalRowId(row);
        assertNotNull(transfer); assertEquals(10d, transfer.cost, 0d); assertEquals(1d, transfer.serviceCharge, 0d);
        OriginalBillRecord fee = new OriginalBillRepository(store).byExactUser(ReferenceStore.LOCAL_OFFLINE_OWNER, transfer.billId);
        assertNotNull(fee); assertEquals(1d, fee.cost, 0d);
        assertEquals("SAVED", store.candidate(candidate).optString("state"));
        assertEquals("transfer", store.candidate(candidate).optString("save_kind"));
        assertTrue(store.candidate(candidate).isNull("transaction_id"));
        assertEquals(row, store.one("SELECT transfer_row_id FROM candidate_transfer_receipts WHERE candidate_id=?", candidate).optLong("transfer_row_id"));
        JSONObject committed = store.backup().optJSONObject("tables");
        assertEquals(row, store.saveCandidateTransfer(value, candidate, () -> {}));
        assertEquals(committed.toString(), store.backup().optJSONObject("tables").toString());
    }

    private JSONObject expense() {
        JSONObject value = Json.object("book_id", book, "kind", "expense", "currency", "CNY", "amount", 500,
                "category_id", store.categoryRepository().parent(99).optLong("id"), "time", TIME,
                "remark", "功能提交支出", "fee", 0, "discount", 0);
        store.selectAccountForInput(value, "account_id", from, true); return value;
    }
    private String candidate(JSONObject value) { return store.addCandidate(value, "com.eg.android.AlipayGphone", book); }
    private double balance(long row) { return store.assetRepository().byRowId(row).optDouble("asset_number"); }
    private void assertUnchanged(JSONObject before, String candidate) {
        assertEquals("PENDING", store.candidate(candidate).optString("state"));
        assertTrue(store.candidate(candidate).isNull("transaction_id"));
        assertEquals(100d, balance(from), 0d); assertEquals(50d, balance(to), 0d);
        assertEquals("Every persisted table must be unchanged after the failed user entry", before.toString(), store.backup().optJSONObject("tables").toString());
    }
    private void assertExpense(long row, String candidate) {
        assertEquals(95d, balance(from), 0d); assertEquals(50d, balance(to), 0d);
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(500, store.transaction(row).optLong("amount"));
        assertEquals(from, store.transaction(row).optLong("account_id"));
        assertEquals("SAVED", store.candidate(candidate).optString("state"));
        assertEquals(row, store.candidate(candidate).optLong("transaction_id"));
        assertEquals(500, store.totals(book, 0, Long.MAX_VALUE).optLong("pay"));
    }
    private static void fails(Runnable action) {
        try { action.run(); fail("The actual injected SQL failure was reported as saved"); }
        catch (RuntimeException expected) { /* The successful retry proves this was not a permanent invalid fixture. */ }
    }
}
