package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Store's actual refund dispatcher/receipt, real account effects and strict backup boundary. */
@RunWith(AndroidJUnit4.class)
public final class OriginalAutoRefundConfirmationDeviceTest {
    private static final String DATABASE = "reference_original_refund_confirmation_test.sqlite";
    private static final long TIME = 1735689600000L;
    private Context context;
    private ReferenceStore store;
    private OriginalBillRepository bills;
    private long book;
    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase();
        bills = new OriginalBillRepository(store); book = store.books().get(0).optLong("id");
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DATABASE); }
    private JSONObject input(long minor, long account) {
        return Json.object("book_id", book, "kind", "expense", "amount", minor, "currency", "CNY",
                "category_id", store.categoryRepository().parent(99).optLong("id"), "account_id", account,
                "time", TIME, "remark", "原始备注", "discount", 0, "fee", 0,
                "original_number", "30.00", "original_income", true, "original_time", TIME);
    }
    private long fund(double initial) { return new OriginalAssetCommands(store, 0).createFund("合成账户 " + store.scalar("SELECT COUNT(*) n FROM accounts"), null, initial); }
    private long semantic(long row) { return store.assetRepository().byRowId(row).optLong("semantic_id"); }
    private double balance(long row) { return store.assetRepository().byRowId(row).optDouble("asset_number"); }
    private long expense(long account) { return store.saveTransaction(input(10000, account), 0, null); }
    private String candidate(JSONObject input) { return store.addCandidate(input, "com.eg.android.AlipayGphone", input.optLong("book_id")); }
    private OriginalRefundRepository.Snapshot aggregate(OriginalBillRecord parent) { return new OriginalRefundRepository(store).firstByParent(0, parent.billId); }
    private long confirm(JSONObject input, String candidate, OriginalBillRecord parent, Long receiver) { return store.saveCandidateRefund(input, candidate, parent, receiver); }
    private List<JSONObject> histories() { return store.query("SELECT * FROM asset_histories ORDER BY id"); }

    @Test public void actualFundPartialRefundPersistsOriginalAggregateAndSeparateCandidateReceipt() {
        long account = fund(100), row = expense(account); OriginalBillRecord parent = bills.byLocalRowId(row);
        JSONObject input = input(3000, account); String candidate = candidate(input);
        assertEquals(row, confirm(input, candidate, parent, semantic(account)));
        assertEquals(30.0d, balance(account), 0); assertEquals(70.0d, bills.byLocalRowId(row).cost, 0);
        assertEquals(7000, store.transaction(row).optLong("amount")); assertEquals(3, histories().size());
        assertEquals(30.0d, histories().get(2).optDouble("change_number"), 0);
        JSONObject receipt = store.candidate(candidate); assertEquals("SAVED", receipt.optString("state")); assertEquals("refund", receipt.optString("save_kind"));
        assertEquals(row, receipt.optLong("transaction_id")); assertTrue(store.transaction(row).isNull("candidate_id"));
        OriginalRefundRepository.Snapshot aggregate = aggregate(parent);
        assertEquals(30.0d, aggregate.data.refundNumber, 0); assertEquals(aggregate.refundSemanticId, receipt.optLong("refund_semantic_id"));
        assertEquals(aggregate.data.rawInfos.get(0), receipt.optString("refund_item_raw"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions WHERE related_id IS NOT NULL"));
    }
    @Test public void actualEqualRefundKeepsZeroBillInsteadOfReplacingItWithAStandaloneReturn() {
        long account = fund(100), row = expense(account); OriginalBillRecord parent = bills.byLocalRowId(row);
        JSONObject input = input(10000, account); String candidate = candidate(input); confirm(input, candidate, parent, semantic(account));
        assertEquals(100.0d, balance(account), 0); assertEquals(0.0d, bills.byLocalRowId(row).cost, 0);
        assertEquals(0, store.transaction(row).optLong("amount")); assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_bill_related"));
        JSONObject backup = store.backup(); store.restore(backup);
        assertEquals("refund", store.candidate(candidate).optString("save_kind")); assertEquals(0.0d, bills.byLocalRowId(row).cost, 0);
    }
    @Test public void actualExcessRefundCreatesOppositeDifferenceAndIndependentSemanticRelation() {
        long account = fund(200), row = expense(account); OriginalBillRecord parent = bills.byLocalRowId(row);
        JSONObject input = input(15000, account); String candidate = candidate(input); confirm(input, candidate, parent, semantic(account));
        assertEquals(250.0d, balance(account), 0); assertEquals(0.0d, bills.byLocalRowId(row).cost, 0);
        List<OriginalBillRelatedRecord> related = new OriginalBillRelatedRepository(store).forBill(0, parent.billId);
        assertEquals(1, related.size());
        OriginalBillRecord difference = bills.byExactUser(0, related.get(0).twoBillId);
        assertNotNull(difference); assertEquals(50.0d, difference.cost, 0); assertEquals(9, difference.parentCategoryId);
        assertEquals(911, difference.childCategoryId); assertEquals(1, difference.billType); assertEquals(0, difference.assetId);
        assertTrue(store.transaction(difference.localRowId).isNull("related_id"));
        OriginalRefundInfo info = OriginalRefundCodec.decodeStrict(aggregate(parent).data.rawInfos.get(0));
        assertEquals(difference.billId, info.relatedBillId); assertEquals(100.0d, info.restoreCostNum, 0);
        store.restore(store.backup()); assertEquals(2, store.scalar("SELECT COUNT(*) n FROM transactions"));
    }
    @Test public void differenceRawAssetZeroCanAffectAnActualSemanticZeroAssetWithoutUsingReceiverFk() {
        AssetRepository.Definition zero = new AssetRepository.Definition("合成语义零账户", 1, null); zero.assetNumber = 50;
        long zeroRow = store.assetRepository().insertRecovered(0, 0, 1, 0, zero);
        long account = fund(100), row = expense(account); OriginalBillRecord parent = bills.byLocalRowId(row);
        JSONObject input = input(15000, account); confirm(input, candidate(input), parent, semantic(account));
        assertEquals(150.0d, balance(account), 0); assertEquals(100.0d, balance(zeroRow), 0);
        int differenceId = new OriginalBillRelatedRepository(store).forBill(0, parent.billId).get(0).twoBillId;
        OriginalBillRecord difference = bills.byExactUser(0, differenceId);
        assertEquals(0, difference.assetId); assertTrue(store.transaction(difference.localRowId).isNull("account_id"));
        assertEquals(0, store.transaction(difference.localRowId).optLong("raw_asset_semantic_id"));
    }
    @Test public void signedNegativeAmountKeepsVArithmeticInsteadOfSilentlyTakingAbsoluteInput() {
        long account = fund(200), row = expense(account); OriginalBillRecord parent = bills.byLocalRowId(row);
        JSONObject input = input(-3000, account); confirm(input, candidate(input), parent, semantic(account));
        assertEquals(130.0d, bills.byLocalRowId(row).cost, 0); assertEquals(130.0d, balance(account), 0);
        assertEquals(-30.0d, aggregate(parent).data.refundNumber, 0);
        OriginalRefundInfo info = OriginalRefundCodec.decodeStrict(aggregate(parent).data.rawInfos.get(0));
        assertEquals(-30.0d, info.number, 0); assertEquals(30.0d, info.assetNum, 0); assertEquals(-130.0d, info.currentNumber, 0);
    }
    @Test public void multipleRefundReceiptsAreIdempotentAndKeepParentCandidateAndBook() {
        long account = fund(100); JSONObject purchase = input(10000, account); String purchaseCandidate = candidate(purchase);
        long row = store.saveTransaction(purchase, 0, purchaseCandidate); OriginalBillRecord parent = bills.byLocalRowId(row);
        long headerBook = store.saveBook(0, "另一本合成账本", "CNY", 0);
        JSONObject first = input(3000, account); Json.put(first, "book_id", headerBook); String firstCandidate = candidate(first);
        confirm(first, firstCandidate, parent, semantic(account));
        long historyCount = store.scalar("SELECT COUNT(*) n FROM asset_histories");
        assertEquals(row, confirm(first, firstCandidate, parent, semantic(account)));
        assertEquals(historyCount, store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        JSONObject next = input(2000, account); String nextCandidate = candidate(next); confirm(next, nextCandidate, parent, semantic(account));
        assertEquals(50.0d, balance(account), 0); assertEquals(50.0d, aggregate(parent).data.refundNumber, 0);
        assertEquals(2, aggregate(parent).data.rawInfos.size()); assertEquals(purchaseCandidate, store.transaction(row).optString("candidate_id"));
        assertEquals(book, store.transaction(row).optLong("book_id")); assertEquals(headerBook, store.candidate(firstCandidate).optLong("book_id"));
        assertEquals(row, store.candidate(firstCandidate).optLong("transaction_id")); assertEquals(row, store.candidate(nextCandidate).optLong("transaction_id"));
        store.restore(store.backup()); assertEquals("SAVED", store.candidate(purchaseCandidate).optString("state"));
    }
    @Test public void parentSaveFailureLeavesEarlierEffectsAndCandidatePendingWithAnExplicitWarning() {
        long account = fund(100), row = expense(account); OriginalBillRecord parent = bills.byLocalRowId(row);
        JSONObject input = input(3000, account); String candidate = candidate(input);
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_refund_parent_failure BEFORE UPDATE OF bill_cost ON transactions BEGIN SELECT RAISE(ABORT,'synthetic'); END");
        try { confirm(input, candidate, parent, semantic(account)); fail("partial source save was reported successful"); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("勿重复提交")); }
        assertEquals(30.0d, balance(account), 0); assertEquals(100.0d, bills.byLocalRowId(row).cost, 0);
        assertEquals(30.0d, aggregate(parent).data.refundNumber, 0); assertEquals(3, histories().size());
        assertEquals("PENDING", store.candidate(candidate).optString("state")); assertTrue(store.candidate(candidate).isNull("refund_item_raw"));
    }
    @Test public void retainedParentRewritesItsProjectionAndNullRemarkStillOmitsExistingText() {
        long firstAccount = fund(100), secondAccount = fund(0), row = expense(firstAccount);
        OriginalBillRecord selected = bills.byLocalRowId(row);
        long anotherBook = store.saveBook(0, "后改合成账本", "CNY", 0);
        OriginalBillRecord changed = bills.byLocalRowId(row); changed.bookId = store.book(anotherBook).optLong("semantic_id");
        changed.assetId = semantic(secondAccount); changed.parentCategoryId = 1; changed.childCategoryId = 101;
        changed.time = TIME + 3600000; changed.remark = "后改备注"; changed.discountNumber = 2; changed.notIntoTotal = true; changed.notIntoBudget = true;
        ContentValues projection = new ContentValues(); projection.put("book_id", anotherBook); projection.put("account_id", secondAccount);
        projection.put("category_id", store.categoryRepository().child(1, 101).optLong("id")); projection.put("parent_semantic_id", 1); projection.put("child_semantic_id", 101);
        projection.put("raw_asset_semantic_id", changed.assetId); projection.put("owner_semantic_id", 0); projection.put("remark", changed.remark);
        projection.put("merchant", "保留宿主商户"); projection.put("time", changed.time); projection.put("discount", 200); projection.put("not_total", 1); projection.put("not_budget", 1);
        bills.saveOriginalRaw(changed, projection);
        JSONObject input = input(3000, 0); confirm(input, candidate(input), selected, null);
        JSONObject restored = store.transaction(row);
        assertEquals(book, restored.optLong("book_id")); assertEquals(firstAccount, restored.optLong("account_id"));
        assertEquals(store.categoryRepository().parent(99).optLong("id"), restored.optLong("category_id"));
        assertEquals("原始备注", restored.optString("remark")); assertEquals("保留宿主商户", restored.optString("merchant"));
        assertEquals(TIME, restored.optLong("time")); assertEquals(0, restored.optLong("discount"));
        assertEquals(0, restored.optInt("not_total")); assertEquals(0, restored.optInt("not_budget")); assertEquals(7000, restored.optLong("amount"));
        OriginalBillRecord nullRemark = bills.byLocalRowId(row); nullRemark.remark = null;
        store.getWritableDatabase().execSQL("UPDATE transactions SET bill_remark='必须保留',remark='必须保留' WHERE id=?", new Object[]{row});
        JSONObject next = input(100, 0); confirm(next, candidate(next), nullRemark, null);
        assertEquals("必须保留", bills.byLocalRowId(row).remark); assertEquals("必须保留", store.transaction(row).optString("remark"));
    }
    @Test public void receiptCannotBeReboundToDifferentOwnerWithSameSemanticBillAndRestoreRollsBack() {
        long account = fund(100), row = expense(account); OriginalBillRecord parent = bills.byLocalRowId(row);
        JSONObject input = input(3000, account); String candidate = candidate(input); confirm(input, candidate, parent, semantic(account));
        long foreignBook = BookRepository.bootstrapNewLocalOwner(store.getWritableDatabase(), 8, TIME);
        OriginalBillRecord foreign = new OriginalBillRecord(); foreign.userId = 8; foreign.billId = parent.billId;
        foreign.bookId = 1; foreign.parentCategoryId = 99; foreign.childCategoryId = -1; foreign.assetId = -1; foreign.cost = 70; foreign.time = TIME;
        ContentValues projection = new ContentValues(); projection.put("book_id", foreignBook); projection.put("kind", "expense");
        projection.put("amount", 7000); projection.put("currency", "CNY"); projection.put("time", TIME); projection.put("created", TIME);
        projection.put("category_id", store.categoryRepository().parent(99).optLong("id")); projection.put("parent_semantic_id", 99); projection.put("child_semantic_id", -1);
        projection.put("owner_semantic_id", 0); projection.put("raw_asset_semantic_id", -1);
        bills.saveOriginalRaw(foreign, projection);
        JSONObject backup = store.backup(); JSONArray rows = backup.optJSONObject("tables").optJSONArray("candidates");
        for (int i = 0; i < rows.length(); i++) {
            JSONObject receipt = rows.optJSONObject(i); if (!candidate.equals(receipt.optString("id"))) continue;
            Json.put(receipt, "transaction_id", foreign.localRowId); JSONObject payload = Json.parse(receipt.optString("payload"));
            Json.put(payload, "refund_parent_row_id", foreign.localRowId); Json.put(receipt, "payload", payload.toString());
        }
        try { store.restore(backup); fail("receipt rebound across owner accepted"); }
        catch (IllegalArgumentException | android.database.SQLException expected) { assertNotNull(expected.getMessage()); }
        assertEquals(row, store.candidate(candidate).optLong("transaction_id")); assertEquals(30.0d, balance(account), 0);
        assertEquals(70.0d, bills.byLocalRowId(row).cost, 0); assertEquals(1, aggregate(parent).data.rawInfos.size());
    }
    @Test public void unknownOriginalCategoryPairKeepsNullBridgeAndRoundTripsWithoutFallback() {
        long account = fund(100), row = expense(account); OriginalBillRecord parent = bills.byLocalRowId(row);
        parent.childCategoryId = 987654321;
        ContentValues projection = new ContentValues(); projection.putNull("category_id"); projection.put("child_semantic_id", parent.childCategoryId);
        bills.saveOriginalRaw(parent, projection);
        JSONObject input = input(3000, account); String candidate = candidate(input); confirm(input, candidate, parent, semantic(account));
        assertTrue(store.transaction(row).isNull("category_id")); assertEquals(987654321, bills.byLocalRowId(row).childCategoryId);
        store.restore(store.backup()); assertEquals(987654321, store.transaction(row).optInt("child_semantic_id"));
        assertTrue(store.transaction(row).isNull("category_id")); assertEquals("SAVED", store.candidate(candidate).optString("state"));
        assertEquals(30.0d, balance(account), 0);
    }
    @Test public void refundReplacementPhysicallyDeletesAndReversesOldIncomeBeforeTheRefundStages() {
        long account = fund(100), parentRow = expense(account); OriginalBillRecord parent = bills.byLocalRowId(parentRow);
        JSONObject oldIncome = input(3000, account); Json.put(oldIncome, "kind", "income");
        Json.put(oldIncome, "category_id", store.categoryRepository().child(9, 911).optLong("id"));
        String oldCandidate = candidate(oldIncome); long oldRow = store.saveTransaction(oldIncome, 0, oldCandidate);
        assertEquals(30.0d, balance(account), 0);
        JSONObject refund = Json.copy(oldIncome); Json.put(refund, "original_amount", 3000);
        Json.put(refund, "original_income", true); Json.put(refund, "original_currency", "CNY");
        Json.put(refund, "original_time", TIME); String nextCandidate = candidate(refund);
        long returned = store.saveCandidateRefundReplacing(refund, nextCandidate, parent, semantic(account), oldRow);
        assertEquals(parentRow, returned); assertNull(store.transaction(oldRow));
        assertEquals(30.0d, balance(account), 0); assertEquals(70.0d, bills.byLocalRowId(parentRow).cost, 0);
        assertEquals("DISMISSED", store.candidate(oldCandidate).optString("state"));
        assertEquals("SAVED", store.candidate(nextCandidate).optString("state"));
        assertEquals("refund", store.candidate(nextCandidate).optString("save_kind"));
        List<JSONObject> history = histories(); assertEquals(5, history.size());
        assertEquals(-30.0d, history.get(3).optDouble("change_number"), 0);
        assertEquals(0.0d, history.get(3).optDouble("current_number"), 0);
        assertEquals(30.0d, history.get(4).optDouble("change_number"), 0);
        assertEquals(30.0d, history.get(4).optDouble("current_number"), 0);
        assertEquals(parentRow, store.saveCandidateRefundReplacing(refund, nextCandidate, parent, semantic(account), oldRow));
        assertEquals(5, histories().size()); assertEquals(1, aggregate(parent).data.rawInfos.size());
    }
    @Test public void replacingTheSelectedIncomeParentKeepsItsRetainedZeroRowSaveAndAnOrphanReceipt() {
        long account = fund(100); JSONObject oldIncome = input(3000, account); Json.put(oldIncome, "kind", "income");
        Json.put(oldIncome, "category_id", store.categoryRepository().child(9, 911).optLong("id"));
        String oldCandidate = candidate(oldIncome); long oldRow = store.saveTransaction(oldIncome, 0, oldCandidate);
        OriginalBillRecord retainedParent = bills.byLocalRowId(oldRow);
        JSONObject refund = Json.copy(oldIncome); Json.put(refund, "original_amount", 3000); Json.put(refund, "original_currency", "CNY");
        String nextCandidate = candidate(refund);
        assertEquals(oldRow, store.saveCandidateRefundReplacing(refund, nextCandidate, retainedParent, semantic(account), oldRow));
        assertNull(store.transaction(oldRow)); assertEquals(oldRow, retainedParent.localRowId); assertEquals(0.0d, retainedParent.cost, 0);
        assertEquals(70.0d, balance(account), 0); assertEquals("DISMISSED", store.candidate(oldCandidate).optString("state"));
        JSONObject receipt = store.candidate(nextCandidate); assertEquals("SAVED", receipt.optString("state"));
        assertEquals("refund", receipt.optString("save_kind")); assertEquals(oldRow, receipt.optLong("transaction_id"));
        OriginalRefundRepository.Snapshot raw = aggregate(retainedParent); assertNotNull(raw);
        assertNull(raw.data.parentTransactionRowId); assertEquals(retainedParent.billId, raw.data.parentBillSemanticId);
        assertEquals(-30.0d, OriginalRefundCodec.decodeStrict(raw.data.rawInfos.get(0)).number, 0);
        assertEquals(4, histories().size()); assertEquals(-30.0d, histories().get(2).optDouble("change_number"), 0);
        assertEquals(-30.0d, histories().get(3).optDouble("change_number"), 0);
        // This host receipt establishes metadata consistency, not proof that an
        // externally supplied backup's absent historical row once existed.
        store.restore(store.backup()); assertNull(store.transaction(oldRow));
        assertEquals(oldRow, store.saveCandidateRefundReplacing(refund, nextCandidate, retainedParent, semantic(account), oldRow));
        assertEquals(70.0d, balance(account), 0); assertEquals(4, histories().size());
    }
    @Test public void unsupportedHostCurrencyAndInvalidAmountAreRejectedBeforeDeletingARepeatedBill() {
        long account = fund(100); JSONObject oldIncome = input(3000, account); Json.put(oldIncome, "kind", "income");
        Json.put(oldIncome, "category_id", store.categoryRepository().child(9, 911).optLong("id"));
        String oldCandidate = candidate(oldIncome); long oldRow = store.saveTransaction(oldIncome, 0, oldCandidate);
        OriginalBillRecord selected = bills.byLocalRowId(oldRow);
        for (int scenario = 0; scenario < 3; scenario++) {
            JSONObject refund = Json.copy(oldIncome); Json.put(refund, "original_amount", 3000); Json.put(refund, "original_currency", "CNY");
            selected.currencyInfo = scenario == 0 ? "USD 30" : "";
            if (scenario == 1) Json.put(refund, "amount", 0);
            if (scenario == 2) store.getWritableDatabase().execSQL("UPDATE accounts SET asset_currency='CNY',currency='CNY' WHERE id=?", new Object[]{account});
            String nextCandidate = candidate(refund);
            try { store.saveCandidateRefundReplacing(refund, nextCandidate, selected, semantic(account), oldRow); fail("host preflight ran after deletion"); }
            catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
            assertNotNull(store.transaction(oldRow)); assertEquals("SAVED", store.candidate(oldCandidate).optString("state"));
            assertEquals("PENDING", store.candidate(nextCandidate).optString("state"));
            assertEquals(130.0d, balance(account), 0); assertEquals(2, histories().size());
            assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_refunds"));
        }
    }
    @Test public void restoringAnOrphanReceiptIntoAFreshDatabaseReservesItsDeletedLocalRowId() {
        long account = fund(100); JSONObject oldIncome = input(3000, account); Json.put(oldIncome, "kind", "income");
        Json.put(oldIncome, "category_id", store.categoryRepository().child(9, 911).optLong("id"));
        String oldCandidate = candidate(oldIncome); long oldRow = store.saveTransaction(oldIncome, 0, oldCandidate);
        OriginalBillRecord retained = bills.byLocalRowId(oldRow); JSONObject refund = Json.copy(oldIncome);
        Json.put(refund, "original_currency", "CNY"); String receiptId = candidate(refund);
        store.saveCandidateRefundReplacing(refund, receiptId, retained, semantic(account), oldRow);
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions"));
        JSONObject backup = store.backup(); String freshName = DATABASE + ".fresh.sqlite";
        context.deleteDatabase(freshName);
        try (ReferenceStore restored = new ReferenceStore(context, freshName)) {
            restored.getWritableDatabase(); restored.restore(backup);
            assertNull(restored.transaction(oldRow)); assertEquals(oldRow, restored.candidate(receiptId).optLong("transaction_id"));
            long restoredBook = restored.books().get(0).optLong("id");
            JSONObject next = Json.object("book_id", restoredBook, "kind", "expense", "amount", 100, "currency", "CNY",
                    "category_id", restored.categoryRepository().parent(99).optLong("id"), "account_id", account,
                    "time", TIME + 1000, "remark", "恢复后新增账单", "discount", 0, "fee", 0);
            long nextRow = restored.saveTransaction(next, 0, null);
            assertTrue("A new Bill reused an orphan receipt's historical local PK", nextRow > oldRow);
            assertNull(restored.transaction(oldRow));
            assertEquals(69.0d, restored.assetRepository().byRowId(account).optDouble("asset_number"), 0);
            restored.restore(restored.backup());
            assertEquals(nextRow, restored.transaction(nextRow).optLong("id"));
            assertEquals(oldRow, restored.candidate(receiptId).optLong("transaction_id"));
            assertEquals("refund", restored.candidate(receiptId).optString("save_kind"));
        } finally { context.deleteDatabase(freshName); }
    }
}
