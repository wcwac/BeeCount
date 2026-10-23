package com.wangc.bill.reference;

import android.content.ContentValues;
import java.util.function.LongSupplier;

/**
 * AutoAddLayout.V's ordered local action. This is a refund-tab command, not an
 * inference from income/remark or the unused BillInfo.isRefund field.
 */
public final class OriginalAutoRefundCommands {
    /** Real local consumers are supplied by the host; this command never calls HTTP. */
    public interface Effects {
        void parentUpdated(OriginalBillRecord parent, boolean saved);
        void adjustInstalment(OriginalBillRecord parent);
        void refreshCredit(OriginalBillRecord parent);
        void refundAggregateChanged(int parentSemanticId, double total, boolean saved);
        void refreshGroup(OriginalBillRecord parent);
    }
    /** Supplies only the host FK/minor-unit projection, after raw values are determined. */
    public interface Projection {
        ContentValues project(OriginalBillRecord bill, boolean difference);
    }
    public static final class Result {
        public final OriginalBillRecord parent;
        /** Null means E3's no-local-PK/no-accessible-row branch did not call save. */
        public final OriginalBillRepository.SaveResult parentSave;
        public final OriginalBillRepository.SaveResult differenceSave;
        public final OriginalBillRelatedRepository.SaveResult relationSave;
        public final OriginalRefundRepository.SaveResult refundSave;
        public final OriginalRefundInfo item;
        public final boolean saved;
        private Result(OriginalBillRecord parent, OriginalBillRepository.SaveResult parentSave,
                OriginalBillRepository.SaveResult differenceSave,
                OriginalBillRelatedRepository.SaveResult relationSave,
                OriginalRefundRepository.SaveResult refundSave, OriginalRefundInfo item) {
            this.parent = parent; this.parentSave = parentSave; this.differenceSave = differenceSave;
            this.relationSave = relationSave; this.refundSave = refundSave; this.item = item;
            saved = (parentSave == null || parentSave.saved) && (differenceSave == null || differenceSave.saved)
                    && (relationSave == null || relationSave.saved) && refundSave.saved;
        }
        public String firstFailureType() {
            if (differenceSave != null && !differenceSave.saved) return differenceSave.failureType;
            if (relationSave != null && !relationSave.saved) return relationSave.failureType;
            if (parentSave != null && !parentSave.saved) return parentSave.failureType;
            return refundSave.failureType;
        }
    }

    private final ReferenceStore store;
    private final int currentOwner, currentBookOwner;
    private final Effects effects;
    private final LongSupplier clock;
    private final OriginalFinancialOrchestrator financial;
    private final OriginalBillRepository bills;
    private final OriginalRefundRepository refunds;
    private final OriginalBillRelatedRepository relations;
    private final OriginalRefundMatcher matcher;

    public OriginalAutoRefundCommands(ReferenceStore store, int owner, int bookOwner, Effects effects) {
        this(store, owner, bookOwner, effects, System::currentTimeMillis,
                new OriginalFinancialOrchestrator(store, owner, bookOwner));
    }
    OriginalAutoRefundCommands(ReferenceStore store, int owner, int bookOwner, Effects effects,
            LongSupplier clock, OriginalFinancialOrchestrator financial) {
        if (store == null || effects == null || clock == null || financial == null)
            throw new IllegalArgumentException("自动退款依赖不能为空");
        this.store = store; currentOwner = owner; currentBookOwner = bookOwner;
        this.effects = effects; this.clock = clock; this.financial = financial;
        bills = new OriginalBillRepository(store); refunds = new OriginalRefundRepository(store);
        relations = new OriginalBillRelatedRepository(store); matcher = new OriginalRefundMatcher(store);
    }

    /**
     * Caller retains the selected parent, as original adapter.B0 does. An absent
     * receiver object differs from a selected asset with semantic ID zero or -1.
     * Individual save=false results do not stop later source stages or roll back
     * earlier balance/history. Non-save exceptions and Error still propagate.
     */
    public Result save(OriginalBillRecord parent, Long receiverSemanticId, double amountJ,
            String remark, long time, long inTime, Projection projection) {
        if (parent == null || projection == null || !Double.isFinite(amountJ) || amountJ == 0.0d
                || !Double.isFinite(parent.cost)) throw new IllegalArgumentException("请选择账单并输入非零退款金额");
        synchronized (store) {
            if (store.getWritableDatabase().inTransaction())
                throw new IllegalStateException("自动退款阶段需要独立保存");
            OriginalRefundRepository.Snapshot previous = refunds.firstByParent(currentOwner, parent.billId);
            OriginalRefundRepository.Data data = previous == null ? new OriginalRefundRepository.Data() : previous.data.copy();
            data.parentBillSemanticId = parent.billId;
            data.refundNumber += amountJ;
            OriginalRefundInfo item = new OriginalRefundInfo();
            item.remark = remark; item.time = time; item.inTime = inTime;
            if (receiverSemanticId != null) {
                item.assetId = receiverSemanticId;
                financial.changeAsset(receiverSemanticId, Math.abs(amountJ), parent.parentCategoryId == 9,
                        (parent.parentCategoryId == 9 ? "退款：" : "收到退款：") + financial.description(parent));
            }

            OriginalBillRepository.SaveResult differenceSave = null;
            OriginalBillRelatedRepository.SaveResult relationSave = null;
            double oldCost = Math.abs(parent.cost);
            if (oldCost >= amountJ) parent.cost = oldCost - amountJ;
            else {
                OriginalBillRecord difference = new OriginalBillRecord();
                difference.time = time; difference.cost = amountJ - oldCost; difference.remark = "退款差额";
                boolean income = parent.parentCategoryId != 9;
                OriginalRefundMatcher.Categories categories = matcher.latestDifferenceCategories(currentOwner, income);
                if (categories != null) {
                    difference.parentCategoryId = categories.parent; difference.childCategoryId = categories.child;
                } else if (income) {
                    difference.parentCategoryId = 9; difference.childCategoryId = 911;
                } else difference.parentCategoryId = 99; // New Bill's child remains zero.
                difference.recordTime = clock.getAsLong(); difference.billType = 1;
                difference.bookId = parent.bookId; difference.userId = parent.userId;
                // New Bill's assetId remains 0. It is not the selected receiver or -1.
                differenceSave = financial.saveNormal(difference, projection.project(difference, true), true);
                relationSave = relations.tryCreate(currentOwner, parent.billId, difference.billId, clock.getAsLong());
                item.restoreCostNum = oldCost; parent.cost = 0.0d; item.relatedBillId = difference.billId;
            }

            // E3 is not a normal edit: it never calls f.t1 or learns an account again.
            parent.updateTime = clock.getAsLong();
            if (parent.localRowId == 0) {
                OriginalBillRecord existing = bills.findAccessible(currentOwner, currentBookOwner, parent.billId);
                if (existing != null) parent.localRowId = existing.localRowId;
            }
            OriginalBillRepository.SaveResult parentSave = parent.localRowId == 0 ? null
                    : bills.trySaveOriginalRaw(parent, projection.project(parent, false));
            effects.parentUpdated(parent, parentSave == null || parentSave.saved);
            effects.adjustInstalment(parent);
            effects.refreshCredit(parent);
            if (parent.parentCategoryId == 9) {
                item.currentNumber = Math.abs(parent.cost); item.number = -amountJ;
                item.assetNum = -Math.abs(amountJ); item.costNum = -Math.abs(amountJ);
            } else {
                item.currentNumber = -Math.abs(parent.cost); item.number = amountJ;
                item.assetNum = Math.abs(amountJ); item.costNum = Math.abs(amountJ);
            }
            data.prependRaw(OriginalRefundCodec.encodeNew(item));
            // The optional experimental bridge must not make a source-legal orphan fail
            // merely because the selected object's row was deleted before entity.save.
            OriginalBillRecord persisted = parent.localRowId > 0 ? bills.byLocalRowId(parent.localRowId) : null;
            data.parentTransactionRowId = persisted != null && persisted.billId == parent.billId ? parent.localRowId : null;
            OriginalRefundRepository.SaveResult refundSave = previous == null
                    ? refunds.tryCreate(currentOwner, data, clock.getAsLong())
                    : refunds.tryUpdate(previous, data, clock.getAsLong());
            effects.refundAggregateChanged(parent.billId, data.refundNumber, refundSave.saved);
            effects.refreshGroup(parent);
            return new Result(parent, parentSave, differenceSave, relationSave, refundSave, item);
        }
    }
}
