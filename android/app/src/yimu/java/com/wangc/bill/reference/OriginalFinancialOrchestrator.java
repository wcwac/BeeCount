package com.wangc.bill.reference;

import android.content.ContentValues;
import java.util.function.LongSupplier;
import org.json.JSONObject;

/**
 * Executable offline financial stages from a0.g/h and f.t1/r/s/h/o1.
 * Raw calls retain the recovered independent-save behavior. Functional Store entries
 * join AccountingCommitScope, which rolls back every persisted stage on failure.
 * Network calls are deliberately absent.
 * The default effects implement local credit projections, operation history and
 * MAIN subscriptions. BillFiles/AutoTag/BillGroup and remaining domains are explicit
 * unfinished ports; the hooks preserve their original position in the call sequence.
 */
public final class OriginalFinancialOrchestrator {
    public interface Effects {
        void assetChanged(OriginalAssetBalanceStore.LoadedAsset asset, boolean refreshCredit);
        void schedule(Runnable callback, long delayMillis);
        void billSaved(OriginalBillRecord bill, boolean saved);
        default void beforeBillEntitySave(OriginalBillRecord bill) {}
        default void afterBillEntitySaveBeforeLearning(OriginalBillRecord bill,boolean saved) {}
        default void billSaved(OriginalBillRecord bill,boolean saved,boolean emitNormalEvents){billSaved(bill,saved);}
        default void assetOperationRecorded(OriginalAssetBalanceStore.LoadedAsset asset) {}
        default void assetBalanceChangeCompleted(OriginalAssetBalanceStore.LoadedAsset asset) {}
    }
    private final ReferenceStore store;
    private final int currentOwner, currentBookOwner;
    private final OriginalBillRepository bills;
    private final AssetHistoryRepository histories;
    private final OriginalAssetBalanceStore balances;
    private final Effects effects;
    private final LongSupplier clock;

    public OriginalFinancialOrchestrator(ReferenceStore store, int currentOwner, int currentBookOwner) {
        this(store, currentOwner, currentBookOwner, new OriginalFinancialEffects(store,currentOwner,currentBookOwner), System::currentTimeMillis);
    }
    OriginalFinancialOrchestrator(ReferenceStore store, int owner, int bookOwner, Effects effects, LongSupplier clock) {
        if (store == null || effects == null || clock == null) throw new IllegalArgumentException("Financial dependencies are required");
        this.store = store; currentOwner = owner; currentBookOwner = bookOwner;
        this.effects = effects; this.clock = clock;
        bills = new OriginalBillRepository(store);
        histories = new AssetHistoryRepository(store);
        balances = new OriginalAssetBalanceStore(store, this::derivedReimbursementBalance);
    }

    /** a0.g: f.t1 precedes fresh Bill-ID allocation and the independent Bill.save. */
    public OriginalBillRepository.SaveResult saveNormal(OriginalBillRecord bill, ContentValues projection) {
        return saveNormal(bill,projection,true);
    }
    /** a0.h omits a0.g's final normal-bill/asset events while preserving the shared stages. */
    public OriginalBillRepository.SaveResult saveNormal(OriginalBillRecord bill, ContentValues projection,boolean emitNormalEvents) {
        if (bill == null) throw new IllegalArgumentException("Bill is required");
        synchronized (store) {
            requireIndependentAction();
            if (bill.userId == 0) bill.userId = currentBookOwner;
            if (bill.userId != currentOwner) bill.fromUserId = currentOwner;
            bill.updateTime = clock.getAsLong();
            long retainedAsset=bill.assetId;
            boolean noSelectedAccount=AccountingCommitScope.active(store)&&projection!=null&&projection.containsKey("account_id")&&projection.getAsLong("account_id")==null;
            if(noSelectedAccount)bill.assetId=-1L;
            try{editBillAssets(bill);}finally{bill.assetId=retainedAsset;}
            if (bill.localRowId == 0) bill.billId = bills.allocateSemanticId(currentOwner);
            effects.beforeBillEntitySave(bill);
            OriginalBillRepository.SaveResult result = bills.trySaveOriginalRaw(bill, projection);
            effects.afterBillEntitySaveBeforeLearning(bill,result.saved);
            // i0.a is after Bill.save and is not suppressed by a returned false.
            Long localAsset = projection == null ? null : projection.getAsLong("account_id");
            Long exactBridge = bill.assetId == (long) (int) bill.assetId ? localAsset : null;
            store.assetRepository().tryLearnCategoryAccount(store.getWritableDatabase(), currentOwner,
                    bill.parentCategoryId, bill.childCategoryId, bill.assetId, exactBridge, clock.getAsLong());
            effects.billSaved(bill, result.saved,emitNormalEvents);
            return result;
        }
    }

    /** Real loaded objects and fresh lookups, rather than a predicted atomic ledger delta. */
    public void editBillAssets(OriginalBillRecord incoming) {
        synchronized (store) {
            requireIndependentAction();
            String description = description(incoming);
            boolean existing=AccountingCommitScope.active(store)?incoming.localRowId>0:incoming.billId!=0;
            double refund = !existing || !empty(incoming.currencyInfo) ? 0.0d : refundAmount(incoming);
            boolean oldPath = false, accountChanged = false;
            double oldDelta = 0.0d;
            OriginalBillRecord old = !existing ? null : AccountingCommitScope.active(store)
                    ? bills.byLocalRowId(incoming.localRowId) : bills.findAccessible(currentOwner, currentBookOwner, incoming.billId);
            if (old != null && old.assetId != -1L && (!AccountingCommitScope.active(store)||store.transaction(old.localRowId).optLong("account_id")>0)) {
                old.cost = Math.abs(old.cost) + refund; // This object is not persisted here.
                OriginalAssetBalanceStore.LoadedAsset asset = balances.load(currentOwner, old.assetId);
                if (asset != null) {
                    double amount = Math.abs(old.getCurrencyCost());
                    if (old.parentCategoryId == 9) { asset.reduceNumber(amount); oldDelta = amount * -1.0d; }
                    else { asset.addNumber(amount); oldDelta = amount; }
                    asset.setUpdateTime(clock.getAsLong());
                    balances.saveLoaded(asset); // Original save boolean is ignored.
                    effects.assetChanged(asset, false);
                    oldPath = true; accountChanged = old.assetId != incoming.assetId;
                    if (accountChanged) {
                        double current=asset.getAssetNumber();
                        if(oldDelta!=0.0d)histories.appendOperation(currentOwner, asset.semanticId, oldDelta,
                                current, "账单账户变更：" + description);
                    }
                }
            }
            if (incoming.assetId == -1L) return;
            // Even for the same semantic ID, read after the old save/callback/history.
            OriginalAssetBalanceStore.LoadedAsset asset = balances.load(currentOwner, incoming.assetId);
            if (asset == null) return;
            double amount = Math.abs(incoming.getCurrencyCost());
            if (incoming.parentCategoryId == 9) asset.addNumber(amount + refund);
            else asset.reduceNumber(amount + refund);
            asset.setUpdateTime(clock.getAsLong());
            balances.saveLoaded(asset);
            // Capture this exact mutable handle, not its ID or a later fresh DB lookup.
            effects.schedule(() -> effects.assetChanged(asset, false), 1000L);
            double change;
            String prefix;
            if (!oldPath) {
                prefix = "新增账单：";
                change = incoming.parentCategoryId == 9 ? amount + refund : amount * -1.0d - refund;
            } else if (accountChanged) {
                prefix = "账单账户变更：";
                change = incoming.parentCategoryId == 9 ? amount + refund : -amount - refund;
            } else {
                prefix = "账单金额编辑：";
                change = incoming.parentCategoryId == 9 ? amount + refund + oldDelta : oldDelta - amount - refund;
            }
            double current=asset.getAssetNumber(); // Also initializes type-9's cache when the zero history is skipped.
            if (change != 0.0d) histories.appendOperation(currentOwner, asset.semanticId,
                    change, current, prefix + description);
        }
    }

    /** f.r always writes a history for a found asset, including zero; f.s writes none. */
    public void reverseDeletedBill(OriginalBillRecord bill, boolean withHistory) {
        synchronized (store) {
            requireIndependentAction();
            if (bill.assetId == -1L) return;
            OriginalAssetBalanceStore.LoadedAsset asset = balances.load(currentOwner, bill.assetId);
            if (asset == null) return;
            double amount = Math.abs(bill.getCurrencyCost());
            double delta = bill.parentCategoryId == 9 ? amount * -1.0d : amount;
            if (bill.parentCategoryId == 9) asset.reduceNumber(amount); else asset.addNumber(amount);
            asset.setUpdateTime(clock.getAsLong()); balances.saveLoaded(asset);
            if (withHistory) effects.assetChanged(asset, false);
            else effects.assetOperationRecorded(asset);
            if (withHistory) histories.appendOperation(currentOwner, asset.semanticId, delta,
                    asset.getAssetNumber(), "删除账单：" + description(bill));
        }
    }

    /** f.h/o1: only the input has a zero gate; a rounded resulting delta of zero is recorded. */
    public void changeAsset(long semanticId, double amount, boolean reduce, String content) {
        synchronized (store) {
            requireIndependentAction();
            if (amount == 0.0d) return;
            OriginalAssetBalanceStore.LoadedAsset asset = balances.load(currentOwner, semanticId);
            if (asset == null) return;
            double before = asset.getAssetNumber();
            asset.setUpdateTime(clock.getAsLong());
            if (reduce) asset.reduceNumber(amount); else asset.addNumber(amount);
            balances.saveLoaded(asset);
            histories.appendOperation(currentOwner, semanticId, asset.getAssetNumber() - before,
                    asset.getAssetNumber(), content);
            effects.assetChanged(asset, true);
            // f.h/o1 posts g7.d after m(asset,true); m alone has no such event.
            effects.assetBalanceChangeCompleted(asset);
        }
    }

    public double readBalance(long semanticId) {
        synchronized (store) {
            OriginalAssetBalanceStore.LoadedAsset asset = balances.load(currentOwner, semanticId);
            return asset == null ? 0.0d : asset.getAssetNumber();
        }
    }
    private double refundAmount(OriginalBillRecord bill) {
        OriginalRefundRepository.Snapshot refund = new OriginalRefundRepository(store).firstByParent(currentOwner, bill.billId);
        if (refund == null) return 0.0d;
        if (bill.cost != 0.0d) return refund.data.refundNumber;
        if (refund.data.rawInfos == null) return 0.0d;
        double amount = 0.0d;
        for (String raw : refund.data.rawInfos) {
            OriginalRefundInfo info = OriginalRefundCodec.decodeStrict(raw);
            double restore = info.getCurrentRestoreNum();
            amount += Math.abs(restore == 0.0d && info.relatedBillId == 0 ? info.number : restore);
        }
        return amount;
    }
    /** b2.D -> a0.o0, whose aggregate LIMIT does not select a single matching Bill. */
    private double derivedReimbursementBalance(int owner, long assetId) {
        double value = 0.0d;
        for (OriginalReimbursementRepository.Snapshot record : new OriginalReimbursementRepository(store).forAsset(owner, assetId, false)) {
            JSONObject sum = store.one("SELECT COALESCE(SUM(ABS(bill_cost)),0) n FROM transactions WHERE bill_origin='original' AND bill_semantic_id=? AND bill_parent_category_id<>9 AND (bill_user_id=? OR bill_from_user_id=? OR bill_user_id=? OR bill_from_user_id=?) LIMIT 1",
                    "" + record.data.parentBillSemanticId, "" + owner, "" + owner, "" + currentBookOwner, "" + currentBookOwner);
            double cost = sum.optDouble("n");
            if (cost != 0.0d && cost > record.data.reimbursementNumber) value = value + cost - record.data.reimbursementNumber;
        }
        return value;
    }
    public String description(OriginalBillRecord bill) {
        if (!empty(bill.remark)) return bill.remark;
        JSONObject parent = store.categoryRepository().parent(bill.parentCategoryId);
        String name = parent == null ? null : parent.optString("name");
        // Original q3 uses the global child-ID map, not a parent-scoped lookup.
        JSONObject child = store.one("SELECT name FROM categories WHERE level='child' AND semantic_id=? ORDER BY id LIMIT 1", "" + bill.childCategoryId);
        return child == null ? name : name + "-" + child.optString("name");
    }
    private void requireIndependentAction() {
        if ((store.getWritableDatabase().inTransaction() && !AccountingCommitScope.active(store))) throw new IllegalStateException("Original financial stages require independent saves");
    }
    private static boolean empty(String value) { return value == null || value.isEmpty(); }
}
