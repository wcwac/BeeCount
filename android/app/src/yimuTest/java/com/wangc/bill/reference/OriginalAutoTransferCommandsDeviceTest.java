package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real independent account/Bill/Transfer saves, without a surrounding ledger transaction. */
@RunWith(AndroidJUnit4.class)
public final class OriginalAutoTransferCommandsDeviceTest {
    private static final String DB = "original_auto_transfer_commands_test.sqlite";
    private static final long TIME = 1735689600000L, FROM = 101, TO = 102;
    private Context context;
    private ReferenceStore store;
    private long book;
    private int feeBookReads, warnings;
    private final List<String> stages = new ArrayList<>();

    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DB);
        store = new ReferenceStore(context, DB); store.getWritableDatabase();
        book = store.books().get(0).optLong("id");
        asset(FROM, "转出合成账户", 100); asset(TO, "转入合成账户", 10);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DB); }
    private void asset(long semantic, String name, double amount) {
        AssetRepository.Definition value = new AssetRepository.Definition(name, 1, null); value.assetNumber = amount;
        store.assetRepository().insertRecovered(0, semantic, 10, TIME, value);
    }
    private double balance(long semantic) { return store.assetRepository().p(semantic, 0).optDouble("asset_number"); }
    private List<JSONObject> histories() { return store.query("SELECT * FROM asset_histories ORDER BY id"); }
    private OriginalFinancialOrchestrator financial(int bookOwner) {
        return new OriginalFinancialOrchestrator(store, 0, bookOwner, new OriginalFinancialOrchestrator.Effects() {
            public void assetChanged(OriginalAssetBalanceStore.LoadedAsset value, boolean credit) { stages.add("ASSET:" + value.semanticId); }
            public void schedule(Runnable callback, long delay) { stages.add("SCHEDULE:" + delay); }
            public void billSaved(OriginalBillRecord bill, boolean saved) { stages.add("BILL:" + saved); }
        }, () -> TIME + 1);
    }
    private OriginalAutoTransferCommands command() {
        return command(0, semantic -> { feeBookReads++; assertEquals(FROM, semantic); return store.book(book).optLong("semantic_id"); });
    }
    private OriginalAutoTransferCommands command(int bookOwner, OriginalAutoTransferCommands.FeeBookResolver books) {
        OriginalTransferActions actions = new OriginalTransferActions(store, 0, (record, saved) -> {
            assertFalse(store.getWritableDatabase().inTransaction()); stages.add("TRANSFER:" + saved);
        });
        return new OriginalAutoTransferCommands(store, 0, bookOwner, actions, books, () -> TIME + 1, financial(bookOwner));
    }
    private ContentValues projection(OriginalBillRecord bill) {
        JSONObject target = store.bookRepository().byIdentity(bill.userId, bill.bookId);
        if (target == null) target = store.bookRepository().byIdentity(0, bill.bookId);
        assertNotNull(target);
        String kind = bill.parentCategoryId == 9 ? "income" : "expense";
        ContentValues value = new ContentValues(); value.put("book_id", target.optLong("id")); value.put("kind", kind);
        value.put("amount", Math.round(bill.cost * 100)); value.put("currency", "CNY");
        JSONObject category = store.categoryRepository().rowForSelection(kind, bill.parentCategoryId, bill.childCategoryId);
        if (category != null) value.put("category_id", category.optLong("id")); else value.putNull("category_id");
        value.put("time", bill.time); value.put("created", bill.recordTime);
        // Match projectFee: an absent raw remark omits the host display column,
        // whose default is ''. Explicit SQL NULL would violate its host schema.
        if (bill.remark != null) value.put("remark", bill.remark);
        JSONObject account = store.assetRepository().p(bill.assetId, 0);
        if (account != null) value.put("account_id", account.optLong("id"));
        value.put("owner_semantic_id", 0); value.put("raw_asset_semantic_id", bill.assetId);
        value.put("parent_semantic_id", bill.parentCategoryId); value.put("child_semantic_id", bill.childCategoryId);
        value.put("not_total", bill.notIntoTotal ? 1 : 0); value.put("not_budget", bill.notIntoBudget ? 1 : 0);
        return value;
    }
    private OriginalAutoTransferCommands.Result save(double amount, String fee, int mode) {
        return command().save(FROM, TO, amount, fee, mode, "合成转账", TIME, this::projection,
                () -> { warnings++; stages.add("WARNING"); }, null);
    }
    private OriginalBillRecord priorBill(int id, int owner, int sender, int parent, int child) {
        OriginalBillRecord bill = new OriginalBillRecord(); bill.billId = id; bill.userId = owner; bill.fromUserId = sender;
        bill.bookId = store.book(book).optLong("semantic_id"); bill.parentCategoryId = parent; bill.childCategoryId = child;
        bill.assetId = -1; bill.cost = 3; bill.time = TIME - 1; bill.recordTime = TIME - 1;
        new OriginalBillRepository(store).saveOriginalRaw(bill, projection(bill));
        assertTrue(store.transaction(bill.localRowId).isNull("bill_remark"));
        assertEquals("", store.transaction(bill.localRowId).optString("remark"));
        return bill;
    }
    private void link(int owner, int feeId, boolean deleted) {
        OriginalTransferRecord record = new OriginalTransferRecord(); record.userId = owner; record.billId = feeId;
        record.originalDeleted = deleted; new OriginalTransferRepository(store).saveOriginalRaw(record);
    }

    @Test public void modeZeroSavesDestinationThenSourceThenIndependentFeeAndFreshRawTransfer() {
        OriginalAutoTransferCommands.Result result = save(10, "2", 0);
        assertTrue(result.saved); assertTrue(result.persistedTransfer); assertFalse(result.principalSkipped); assertFalse(result.attachmentsInvoked);
        assertEquals(20, balance(TO), 0); assertEquals(88, balance(FROM), 0); assertEquals(3, histories().size());
        assertEquals(TO, histories().get(0).optLong("asset_semantic_id")); assertEquals(10, histories().get(0).optDouble("change_number"), 0);
        assertEquals(FROM, histories().get(1).optLong("asset_semantic_id")); assertEquals(-10, histories().get(1).optDouble("change_number"), 0);
        assertEquals(-2, histories().get(2).optDouble("change_number"), 0);
        assertEquals("从转出合成账户转入", histories().get(0).optString("change_content"));
        assertEquals("转出到转入合成账户", histories().get(1).optString("change_content"));
        assertEquals(99, result.feeBill.parentCategoryId); assertEquals(0, result.feeBill.childCategoryId);
        assertEquals(1, result.feeBill.billType); assertEquals(2, result.feeBill.cost, 0); assertEquals(TIME + 1, result.feeBill.recordTime);
        assertNull(result.feeBill.currencyInfo); assertEquals(FROM, result.feeBill.assetId); assertEquals(1, feeBookReads);
        assertEquals(result.feeBill.billId, result.transfer.billId); assertEquals(10, result.transfer.cost, 0);
        assertEquals(2, result.transfer.serviceCharge, 0); assertEquals(0, result.transfer.toCost, 0);
        assertTrue(result.transfer.tags.isEmpty()); assertEquals(TIME, result.transfer.time); assertEquals("合成转账", result.transfer.remark);
        assertTrue(stages.indexOf("ASSET:102") < stages.indexOf("ASSET:101"));
        assertTrue(stages.indexOf("BILL:true") < stages.indexOf("TRANSFER:true"));
    }
    @Test public void modeOneDeductsTheFeeFromBothPrincipalLegsAndStillSavesTheFeeBill() {
        OriginalAutoTransferCommands.Result result = save(10, "2", 1);
        assertEquals(18, balance(TO), 0); assertEquals(90, balance(FROM), 0);
        assertEquals(8, result.transfer.cost, 0); assertEquals(2, result.transfer.serviceCharge, 0);
        assertEquals(8, histories().get(0).optDouble("change_number"), 0);
        assertEquals(-8, histories().get(1).optDouble("change_number"), 0); assertTrue(result.saved);
    }
    @Test public void equalAndSmallerArrivalAmountsWarnAndContinueWithoutEitherPrincipalMutation() {
        for (double amount : new double[]{2, 1}) {
            OriginalAutoTransferCommands.Result result = save(amount, "2", 1);
            assertTrue(result.principalSkipped); assertTrue(result.saved); assertEquals(amount, result.transfer.cost, 0);
        }
        assertEquals(2, warnings); assertEquals(10, balance(TO), 0); assertEquals(96, balance(FROM), 0);
        assertEquals(2, histories().size()); assertEquals(2, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        assertEquals("WARNING", stages.get(0)); assertTrue(stages.indexOf("WARNING") < stages.indexOf("BILL:true"));
    }
    @Test public void zeroAndSignedNegativeAmountsRemainSourceValuesAndDoNotRequireAFeeBook() {
        OriginalAutoTransferCommands.Result zero = save(0, "", 0); assertTrue(zero.saved); assertTrue(histories().isEmpty());
        assertEquals(0, zero.transfer.cost, 0); assertEquals(-1, zero.transfer.billId); assertNull(zero.feeBill);
        OriginalAutoTransferCommands.Result negative = save(-5, "", 0);
        assertEquals(-5, negative.transfer.cost, 0); assertEquals(15, balance(TO), 0); assertEquals(95, balance(FROM), 0);
        assertEquals(0, feeBookReads); assertTrue(negative.saved); assertEquals(2, histories().size());
    }
    @Test public void distinctJAndNGatesPreserveLeadingDotAndCommaBehavior() {
        OriginalAutoTransferCommands.Result dot = save(10, ".5", 1);
        assertEquals(9.5, dot.transfer.cost, 0); assertEquals(.5, dot.transfer.serviceCharge, 0);
        assertNull(dot.feeBill); assertEquals(0, feeBookReads); assertEquals(90.5, balance(FROM), 0);
        OriginalAutoTransferCommands.Result comma = save(10, "1,2", 1);
        assertEquals(10, comma.transfer.cost, 0); assertEquals(0, comma.transfer.serviceCharge, 0);
        assertEquals(1.2, comma.feeBill.cost, 0); assertEquals(29.5, balance(TO), 0); assertEquals(79.3, balance(FROM), .0000001);
        assertEquals(1, feeBookReads); assertTrue(comma.saved);
    }
    @Test public void negativeFeeCreatesTheRealIncomeDefaultAndIncreasesModeOnePrincipal() {
        OriginalAutoTransferCommands.Result result = save(10, "-2", 1);
        assertEquals(12, result.transfer.cost, 0); assertEquals(-2, result.transfer.serviceCharge, 0);
        assertEquals(9, result.feeBill.parentCategoryId); assertEquals(911, result.feeBill.childCategoryId);
        assertEquals("转出合成账户 转账优惠", result.feeBill.remark);
        assertEquals(22, balance(TO), 0); assertEquals(90, balance(FROM), 0); assertEquals(2, histories().get(2).optDouble("change_number"), 0);
    }
    @Test public void latestFeeLookupUsesOwnerInsertionOrderDirectionAndExplicitBillTagSetter() {
        OriginalBillRecord expense = priorBill(201, 0, 0, 99, 42);
        expense.notIntoBudget = true; expense.notIntoTotal = true; expense.tags = Arrays.asList(8L, 8L, 9L, null);
        new OriginalBillRepository(store).saveOriginalRaw(expense, projection(expense)); link(0, 201, true);
        OriginalBillRecord income = priorBill(202, 0, 0, 9, 987); link(0, income.billId, false);
        OriginalBillRecord foreignLink = priorBill(203, 0, 0, 99, 55); link(7, foreignLink.billId, false);
        link(0, -1, false); link(0, 0, false);
        OriginalAutoTransferCommands.Result outgoingFee = save(10, "2", 0);
        assertEquals(99, outgoingFee.feeBill.parentCategoryId); assertEquals(42, outgoingFee.feeBill.childCategoryId);
        assertTrue(outgoingFee.feeBill.notIntoBudget); assertTrue(outgoingFee.feeBill.notIntoTotal);
        assertEquals(Arrays.asList(8L, 9L, null), outgoingFee.feeBill.tags); assertTrue(outgoingFee.transfer.tags.isEmpty());
        OriginalAutoTransferCommands.Result discount = save(10, "-1", 0);
        assertEquals(9, discount.feeBill.parentCategoryId); assertEquals(987, discount.feeBill.childCategoryId);
        assertTrue(discount.saved);
    }
    @Test public void feeQueryUsesTheCurrentBookOwnerAccessDomainWithoutBecomingABookFilter() {
        OriginalBillRecord own = priorBill(301, 0, 0, 99, 3); link(0, own.billId, false);
        OriginalBillRecord other = priorBill(302, 7, 7, 99, 4); link(0, other.billId, false);
        OriginalTransferRepository repo = new OriginalTransferRepository(store);
        assertEquals(301, repo.latestFeeBill(0, 0, false).billId);
        assertEquals(302, repo.latestFeeBill(0, 7, false).billId);
        assertNull(repo.latestFeeBill(7, 7, false));
    }
    @Test public void failedFeeSaveDoesNotUndoItsEarlierBalanceHistoryOrPreventTransferAndAttachments() {
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER reject_transfer_fee BEFORE INSERT ON transactions BEGIN SELECT RAISE(ABORT,'synthetic fee'); END");
        List<Long> files = new ArrayList<>();
        OriginalAutoTransferCommands.Result result = command().save(FROM, TO, 10, "2", 0, "合成转账", TIME,
                this::projection, () -> warnings++, semantic -> { stages.add("FILES"); files.add(semantic); });
        assertFalse(result.saved); assertTrue(result.persistedTransfer); assertFalse(result.feeBillSave.saved);
        assertEquals(0, result.feeBill.localRowId); assertEquals(result.feeBill.billId, result.transfer.billId);
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions")); assertEquals(1, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        assertEquals(88, balance(FROM), 0); assertEquals(20, balance(TO), 0); assertEquals(3, histories().size());
        assertEquals(Arrays.asList(result.transferSave.semanticId), files); assertTrue(result.attachmentsInvoked);
        assertTrue(stages.indexOf("TRANSFER:true") < stages.indexOf("FILES")); assertFalse(result.firstFailureType().isEmpty());
    }
    @Test public void failedTransferSaveStillKeepsTheFeeAndPrincipalAndExecutesItsSourceTail() {
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER ignore_auto_transfer BEFORE INSERT ON original_transfers BEGIN SELECT RAISE(IGNORE); END");
        List<Long> files = new ArrayList<>();
        OriginalAutoTransferCommands.Result result = command().save(FROM, TO, 10, "2", 0, "合成转账", TIME,
                this::projection, () -> warnings++, files::add);
        assertFalse(result.saved); assertFalse(result.persistedTransfer); assertTrue(result.feeBillSave.saved);
        assertTrue(result.feeBill.localRowId > 0); assertEquals(0, result.transfer.localRowId);
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transactions")); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        assertEquals(88, balance(FROM), 0); assertEquals(3, histories().size()); assertTrue(stages.contains("TRANSFER:false"));
        assertEquals(Arrays.asList(result.transferSave.semanticId), files); assertTrue(result.attachmentsInvoked);
    }
    @Test public void ignoredDestinationBalanceSaveStillRecordsItsLoadedMutationAndContinues() {
        store.getWritableDatabase().execSQL("CREATE TEMP TRIGGER reject_incoming_principal BEFORE UPDATE OF asset_number ON accounts WHEN OLD.semantic_id=102 BEGIN SELECT RAISE(ABORT,'synthetic principal'); END");
        OriginalAutoTransferCommands.Result result = save(10, "2", 0);
        assertTrue(result.saved); assertEquals(10, balance(TO), 0); assertEquals(88, balance(FROM), 0);
        assertEquals(20, histories().get(0).optDouble("current_number"), 0); assertEquals(10, histories().get(0).optDouble("change_number"), 0);
        assertEquals(3, histories().size()); assertNotNull(new OriginalTransferRepository(store).byLocalRowId(result.transfer.localRowId));
    }
    @Test public void feeBookIsResolvedOnlyForTheFeeAndRetainsItsOwnRawAndProjectedIdentityAcrossRestore() {
        JSONObject other = store.bookRepository().createOriginal(0, "来源账户专用账本", null, false, false, TIME);
        OriginalAutoTransferCommands command = command(0, semantic -> { feeBookReads++; assertEquals(FROM, semantic); return other.optLong("semantic_id"); });
        OriginalAutoTransferCommands.Result result = command.save(FROM, TO, 10, "2", 0, "合成转账", TIME,
                this::projection, () -> warnings++, null);
        assertEquals(other.optLong("semantic_id"), result.feeBill.bookId);
        assertEquals(other.optLong("id"), store.transaction(result.feeBill.localRowId).optLong("book_id")); assertEquals(1, feeBookReads);
        JSONObject backup = store.backup(); store.restore(backup);
        assertEquals(88, balance(FROM), 0); assertEquals(20, balance(TO), 0); assertEquals(3, histories().size());
        OriginalTransferRecord restored = new OriginalTransferRepository(store).byLocalRowId(result.transfer.localRowId);
        assertEquals(result.feeBill.billId, restored.billId); assertEquals(10, restored.cost, 0);
        assertEquals(other.optLong("semantic_id"), new OriginalBillRepository(store).byLocalRowId(result.feeBill.localRowId).bookId);
    }
    @Test public void selectionAndOuterTransactionGatesPerformNoFinancialWork() {
        OriginalAutoTransferCommands command = command();
        for (Long[] pair : new Long[][]{{null, TO}, {FROM, null}, {FROM, FROM}}) {
            try { command.save(pair[0], pair[1], 10, "2", 0, "合成转账", TIME, this::projection, () -> warnings++, null); fail(); }
            catch (IllegalArgumentException expected) {}
        }
        store.getWritableDatabase().beginTransaction();
        try {
            try { command.save(FROM, TO, 10, "2", 0, "合成转账", TIME, this::projection, () -> warnings++, null); fail(); }
            catch (IllegalStateException expected) {}
        } finally { store.getWritableDatabase().endTransaction(); }
        assertEquals(100, balance(FROM), 0); assertEquals(10, balance(TO), 0);
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM transactions")); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        assertTrue(histories().isEmpty()); assertTrue(stages.isEmpty()); assertEquals(0, warnings); assertEquals(0, feeBookReads);
    }
}
