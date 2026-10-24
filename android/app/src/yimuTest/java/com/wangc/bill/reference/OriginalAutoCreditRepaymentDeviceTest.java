package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** AutoAdd.X's independently committed stages; requires the integrated schema8. */
@RunWith(AndroidJUnit4.class)
public final class OriginalAutoCreditRepaymentDeviceTest {
    private static final String DATABASE = "reference_original_auto_credit_repayment_test.sqlite";
    private static final long TIME = 1735689600000L, FROM = 101, TO = 102;
    private Context context;
    private ReferenceStore store;
    private long book;
    private int warnings, changes, feeBookReads;
    private OriginalLendRecord changedRecord;
    private boolean changedSaved;
    private final List<String> stages = new ArrayList<>();

    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase(); book = store.books().get(0).optLong("id");
        asset(FROM, "合成还款来源", 1, 100); asset(TO, "合成信贷去向", 2, 10);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DATABASE); }
    private void asset(long semantic, String name, int type, double amount) {
        AssetRepository.Definition value = new AssetRepository.Definition(name, type, null); value.assetNumber = amount;
        store.assetRepository().insertRecovered(0, semantic, 10, TIME, value);
    }
    private double balance(long semantic) { return store.assetRepository().p(semantic, 0).optDouble("asset_number"); }
    private List<JSONObject> histories() { return store.query("SELECT * FROM asset_histories ORDER BY id"); }
    private OriginalAutoCreditRepaymentCommands.Effects effects() {
        return new OriginalAutoCreditRepaymentCommands.Effects() {
            @Override public void warningNonPositiveArrival() { warnings++; stages.add("WARNING"); }
            @Override public void repaymentChanged(OriginalLendRecord record, boolean saved) {
                assertFalse(store.getWritableDatabase().inTransaction()); changes++; changedRecord = record; changedSaved = saved;
                stages.add("U:" + saved);
            }
        };
    }
    private OriginalAutoCreditRepaymentCommands command() {
        OriginalFinancialOrchestrator financial = new OriginalFinancialOrchestrator(store, 0, 0,
                new OriginalFinancialOrchestrator.Effects() {
                    @Override public void assetChanged(OriginalAssetBalanceStore.LoadedAsset value, boolean credit) { stages.add("ASSET:" + value.semanticId); }
                    @Override public void schedule(Runnable callback, long delay) { stages.add("SCHEDULE:" + delay); }
                    @Override public void billSaved(OriginalBillRecord bill, boolean saved) { stages.add("BILL:" + saved); }
                }, () -> TIME + 1);
        return new OriginalAutoCreditRepaymentCommands(store, 0, 0, new OriginalLendActions(store, 0), semantic -> {
            assertEquals(FROM, semantic); feeBookReads++; return store.book(book).optLong("semantic_id");
        }, () -> TIME + 1, financial);
    }
    private OriginalAutoCreditRepaymentCommands.Result save(double amount, String fee, int mode) {
        return command().save(FROM, TO, amount, fee, mode, "合成自动还款", TIME, this::projection, effects(), null);
    }
    private ContentValues projection(OriginalBillRecord bill) {
        JSONObject target = store.bookRepository().byIdentity(bill.userId, bill.bookId); assertNotNull(target);
        String kind = bill.parentCategoryId == 9 ? "income" : "expense";
        JSONObject category = store.categoryRepository().rowForSelection(kind, bill.parentCategoryId, bill.childCategoryId);
        assertNotNull("This fixture must use an existing valid category", category);
        ContentValues row = new ContentValues(); row.put("book_id", target.optLong("id")); row.put("kind", kind);
        row.put("amount", Math.round(bill.cost * 100)); row.put("currency", "CNY"); row.put("category_id", category.optLong("id"));
        row.put("parent_semantic_id", bill.parentCategoryId); row.put("child_semantic_id", bill.childCategoryId);
        row.put("time", bill.time); row.put("created", bill.recordTime);
        if (bill.remark != null) row.put("remark", bill.remark);
        JSONObject account = store.assetRepository().p(bill.assetId, 0);
        if (account != null) row.put("account_id", account.optLong("id"));
        row.put("owner_semantic_id", 0); row.put("raw_asset_semantic_id", bill.assetId);
        row.put("not_total", bill.notIntoTotal ? 1 : 0); row.put("not_budget", bill.notIntoBudget ? 1 : 0); return row;
    }

    @Test public void modeZeroKeepsReceiverInterestMemoSourceBookAndAbsoluteLendNumberWithoutInventingFromCost() {
        OriginalAutoCreditRepaymentCommands.Result result = save(-10, "2", 0);
        assertTrue(result.saved); assertTrue(result.persistedLend); assertFalse(result.principalSkipped);
        assertEquals(20, balance(TO), 0); assertEquals(88, balance(FROM), 0); assertEquals(3, histories().size());
        assertEquals(TO, histories().get(0).optLong("asset_semantic_id")); assertEquals(10, histories().get(0).optDouble("change_number"), 0);
        assertEquals(FROM, histories().get(1).optLong("asset_semantic_id")); assertEquals(-10, histories().get(1).optDouble("change_number"), 0);
        assertEquals("从合成还款来源转入", histories().get(0).optString("change_content"));
        assertEquals("还款给合成信贷去向", histories().get(1).optString("change_content"));
        assertEquals(-2, histories().get(2).optDouble("change_number"), 0);
        assertEquals("合成信贷去向 还款利息", result.interestBill.remark); assertEquals(99, result.interestBill.parentCategoryId);
        assertEquals(0, result.interestBill.childCategoryId); assertEquals(FROM, result.interestBill.assetId);
        assertEquals(store.book(book).optLong("semantic_id"), result.interestBill.bookId); assertEquals(1, feeBookReads);
        assertEquals(TO, result.lend.assetId); assertEquals(FROM, result.lend.repaymentAssetId); assertEquals(4, result.lend.type);
        assertEquals(10, result.lend.number, 0); assertEquals(0, result.lend.fromCost, 0); assertEquals(2, result.lend.interest, 0);
        assertEquals(TIME, result.lend.getTime()); assertEquals(0, result.lend.inTime); assertFalse(result.lend.generalServiceBill);
        assertEquals(result.interestBill.billId, result.lend.billId); assertEquals("合成自动还款", result.lend.remark);
        OriginalLendRecord stored = new OriginalLendRepository(store).byLocalRowId(result.lend.localRowId);
        assertEquals(0, stored.getFromCost(), 0); assertEquals(10, stored.getCurrencyCost(FROM), 0);
        assertEquals(1, changes); assertSame(result.lend, changedRecord); assertTrue(changedSaved);
        assertTrue(stages.indexOf("ASSET:102") < stages.indexOf("ASSET:101"));
        assertTrue(stages.indexOf("BILL:true") < stages.indexOf("U:true"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        store.categoryRepository().validateRestored();
    }

    @Test public void modeOneDeductsPositiveInterestAndAddsNegativeDiscountBeforeTakingTheLendAbsoluteValue() {
        OriginalAutoCreditRepaymentCommands.Result charged = save(10, "2", 1);
        assertEquals(8, charged.lend.number, 0); assertEquals(2, charged.lend.interest, 0);
        assertEquals(18, balance(TO), 0); assertEquals(90, balance(FROM), 0);
        OriginalAutoCreditRepaymentCommands.Result discount = save(10, "-2", 1);
        assertTrue(discount.saved); assertEquals(12, discount.lend.number, 0); assertEquals(-2, discount.lend.interest, 0);
        assertEquals(9, discount.interestBill.parentCategoryId); assertEquals(911, discount.interestBill.childCategoryId);
        assertEquals("合成信贷去向 还款优惠", discount.interestBill.remark);
        assertEquals(30, balance(TO), 0); assertEquals(80, balance(FROM), 0); assertEquals(6, histories().size());
        assertEquals(2, histories().get(5).optDouble("change_number"), 0); assertEquals(2, changes);
        assertEquals(0, charged.lend.fromCost, 0); assertEquals(0, discount.lend.fromCost, 0);
        store.categoryRepository().validateRestored();
    }

    @Test public void aLeadingDotDeductsPrincipalWithoutInterestWhileACommaCreatesOnlyTheNInterestBill() {
        OriginalAutoCreditRepaymentCommands.Result dot = save(10, ".5", 1);
        assertTrue(dot.saved); assertEquals(9.5, dot.lend.number, 0); assertEquals(0, dot.lend.interest, 0);
        assertNull(dot.interestBill); assertEquals(-1, dot.lend.billId); assertEquals(0, feeBookReads);
        assertEquals(19.5, balance(TO), 0); assertEquals(90.5, balance(FROM), 0);
        OriginalAutoCreditRepaymentCommands.Result comma = save(10, "1,2", 1);
        assertEquals(10, comma.lend.number, 0); assertEquals(1.2, comma.lend.interest, 0);
        assertEquals(1.2, comma.interestBill.cost, 0); assertEquals(29.5, balance(TO), 0);
        assertEquals(79.3, balance(FROM), .0000001); assertEquals(1, feeBookReads); assertEquals(2, changes);
    }

    @Test public void equalAndSmallerArrivalsWarnButStillSaveInterestAndLendWithoutPrincipalChanges() {
        for (double amount : new double[]{2, 1}) {
            OriginalAutoCreditRepaymentCommands.Result result = save(amount, "2", 1);
            assertTrue(result.principalSkipped); assertTrue(result.saved); assertEquals(amount, result.lend.number, 0);
            assertEquals(2, result.lend.interest, 0);
        }
        assertEquals(2, warnings); assertEquals(10, balance(TO), 0); assertEquals(96, balance(FROM), 0);
        assertEquals(2, histories().size()); assertEquals(2, changes);
        assertEquals("WARNING", stages.get(0)); assertTrue(stages.indexOf("WARNING") < stages.indexOf("BILL:true"));
        assertEquals(2, store.scalar("SELECT COUNT(*) n FROM " + OriginalLendRepository.TABLE));
    }

    @Test public void aVanishedFreshSourceCanPersistRepaymentZeroOnlyThroughTheWarningAndNoNInterestPath() {
        long sourceRow = store.assetRepository().p(FROM, 0).optLong("id");
        assertEquals(1, store.getWritableDatabase().delete("accounts", "id=?", new String[]{"" + sourceRow}));
        assertNull(store.assetRepository().p(FROM, 0));
        OriginalAutoCreditRepaymentCommands.Result result = save(.25, ".5", 1);
        assertTrue(result.saved); assertTrue(result.principalSkipped); assertEquals(1, warnings);
        assertEquals(TO, result.lend.assetId); assertEquals(0, result.lend.repaymentAssetId);
        assertEquals(.25, result.lend.number, 0); assertEquals(0, result.lend.fromCost, 0); assertEquals(0, result.lend.interest, 0);
        assertNull(result.interestBill); assertEquals(-1, result.lend.billId); assertEquals(0, feeBookReads);
        assertTrue(histories().isEmpty()); assertEquals(10, balance(TO), 0); assertEquals(1, changes);
        assertEquals(Arrays.asList("WARNING", "U:true"), stages);
    }

    @Test public void failedInterestSaveKeepsAllEarlierBalancesAndStillSavesLendThenAttachmentsThenU() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_repayment_interest BEFORE INSERT ON transactions BEGIN SELECT RAISE(ABORT,'synthetic interest'); END");
        List<Long> files = new ArrayList<>(); OriginalAutoCreditRepaymentCommands.Result result = command().save(FROM, TO, 10, "2", 0,
                "合成自动还款", TIME, this::projection, effects(), semantic -> { stages.add("FILES"); files.add(semantic); });
        assertFalse(result.saved); assertTrue(result.persistedLend); assertFalse(result.interestBillSave.saved);
        assertEquals(0, result.interestBill.localRowId); assertEquals(result.interestBill.billId, result.lend.billId);
        assertEquals(88, balance(FROM), 0); assertEquals(20, balance(TO), 0); assertEquals(3, histories().size());
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions"));
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM " + OriginalLendRepository.TABLE));
        assertEquals(Arrays.asList(result.lendSave.semanticId), files); assertTrue(result.attachmentsInvoked);
        assertEquals(1, changes); assertTrue(changedSaved);
        assertTrue(stages.indexOf("BILL:false") < stages.indexOf("FILES")); assertTrue(stages.indexOf("FILES") < stages.indexOf("U:true"));
    }

    @Test public void failedLendSaveKeepsTheInterestBillAndStillExecutesAttachmentsBeforeTheFalseUTail() {
        store.getWritableDatabase().execSQL("CREATE TRIGGER synthetic_repayment_lend BEFORE INSERT ON " + OriginalLendRepository.TABLE + " BEGIN SELECT RAISE(IGNORE); END");
        List<Long> files = new ArrayList<>(); OriginalAutoCreditRepaymentCommands.Result result = command().save(FROM, TO, 10, "2", 0,
                "合成自动还款", TIME, this::projection, effects(), semantic -> { stages.add("FILES"); files.add(semantic); });
        assertFalse(result.saved); assertFalse(result.persistedLend); assertTrue(result.interestBillSave.saved);
        assertTrue(result.interestBill.localRowId > 0); assertEquals(0, result.lend.localRowId);
        assertEquals(88, balance(FROM), 0); assertEquals(20, balance(TO), 0); assertEquals(3, histories().size());
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transactions")); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM " + OriginalLendRepository.TABLE));
        assertEquals(Arrays.asList(result.lendSave.semanticId), files); assertTrue(result.attachmentsInvoked);
        assertEquals(1, changes); assertFalse(changedSaved); assertSame(result.lend, changedRecord);
        assertTrue(stages.indexOf("BILL:true") < stages.indexOf("FILES")); assertTrue(stages.indexOf("FILES") < stages.indexOf("U:false"));
    }

    @Test public void anAttachmentExceptionLeavesTheSavedLendAndPrincipalButPreventsTheFinalUEvent() {
        IllegalStateException original = new IllegalStateException("synthetic attachment stage"); AtomicLong fileId = new AtomicLong(Long.MIN_VALUE);
        try {
            command().save(FROM, TO, 10, "", 0, "合成自动还款", TIME, this::projection, effects(), semantic -> {
                stages.add("FILES"); fileId.set(semantic); throw original;
            }); fail("Attachment failure was swallowed");
        } catch (IllegalStateException error) { assertSame(original, error); }
        assertEquals(90, balance(FROM), 0); assertEquals(20, balance(TO), 0); assertEquals(2, histories().size());
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM " + OriginalLendRepository.TABLE));
        assertNotEquals(Long.MIN_VALUE, fileId.get()); assertNotNull(new OriginalLendRepository(store).byIdentity(0, (int) fileId.get()));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions")); assertEquals(0, feeBookReads); assertEquals(0, changes);
        assertEquals("FILES", stages.get(stages.size() - 1)); assertFalse(store.getWritableDatabase().inTransaction());
    }

    @Test public void priorTypeFourInterestSelectionCopiesFlagsAndRunsTheExplicitBillTagDeduplication() {
        OriginalBillRecord expense = priorBill(201, 99, -1); expense.tags = Arrays.asList(8L, 8L, 9L, null);
        expense.notIntoBudget = true; expense.notIntoTotal = true;
        new OriginalBillRepository(store).saveOriginalRaw(expense, projection(expense)); link(201, 4);
        priorBill(202, 9, 911); link(202, 4); priorBill(203, 1, -1); link(203, 3);
        OriginalAutoCreditRepaymentCommands.Result result = save(10, "2", 0);
        assertTrue(result.saved); assertEquals(99, result.interestBill.parentCategoryId); assertEquals(-1, result.interestBill.childCategoryId);
        assertTrue(result.interestBill.notIntoBudget); assertTrue(result.interestBill.notIntoTotal);
        assertEquals(Arrays.asList(8L, 9L, null), result.interestBill.tags);
        assertEquals("合成信贷去向 还款利息", result.interestBill.remark); assertEquals(4, result.lend.type);
        assertEquals(0, result.lend.fromCost, 0); store.categoryRepository().validateRestored();
    }
    private OriginalBillRecord priorBill(int semantic, int parent, int child) {
        OriginalBillRecord bill = new OriginalBillRecord(); bill.billId = semantic; bill.userId = 0;
        bill.bookId = store.book(book).optLong("semantic_id"); bill.assetId = -1; bill.parentCategoryId = parent; bill.childCategoryId = child;
        bill.cost = 1; bill.time = TIME - 1; bill.recordTime = TIME - 1;
        new OriginalBillRepository(store).saveOriginalRaw(bill, projection(bill));
        assertTrue(store.transaction(bill.localRowId).isNull("bill_remark")); assertEquals("", store.transaction(bill.localRowId).optString("remark")); return bill;
    }
    private void link(int bill, int type) {
        OriginalLendRecord row = new OriginalLendRecord(); row.userId = 0; row.billId = bill; row.type = type;
        new OriginalLendRepository(store).saveOriginalRaw(row);
    }
}
