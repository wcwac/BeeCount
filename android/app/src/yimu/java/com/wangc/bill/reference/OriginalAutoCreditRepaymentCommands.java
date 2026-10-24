package com.wangc.bill.reference;

import android.content.ContentValues;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.function.LongSupplier;
import org.json.JSONObject;

/** AutoAddLayout.X's checked credit repayment, independently saved as Lend type 4. */
public final class OriginalAutoCreditRepaymentCommands {
    @FunctionalInterface public interface Projection { ContentValues project(OriginalBillRecord interestBill); }
    public interface Effects {
        void warningNonPositiveArrival();
        /** X posts g7.u after i1.d and the file loop, including an entity save returning false. */
        void repaymentChanged(OriginalLendRecord lend, boolean saved);
    }
    @FunctionalInterface public interface Attachments {
        /** Original a2.g -> r3.K, not a host attachment JSON list. */
        void copyAndLink(long lendSemanticId);
    }
    public static final class Result {
        public final OriginalLendRecord lend;
        public final OriginalLendActions.Result lendSave;
        public final OriginalBillRecord interestBill;
        public final OriginalBillRepository.SaveResult interestBillSave;
        public final boolean persistedLend, saved, principalSkipped, attachmentsInvoked;
        private Result(OriginalLendActions.Result action, OriginalBillRecord interestBill,
                OriginalBillRepository.SaveResult interestSave, boolean skipped, boolean attachmentsInvoked) {
            lendSave = action; lend = action.record; this.interestBill = interestBill; interestBillSave = interestSave;
            persistedLend = action.save.saved; saved = persistedLend && (interestSave == null || interestSave.saved);
            principalSkipped = skipped; this.attachmentsInvoked = attachmentsInvoked;
        }
    }
    private final ReferenceStore store;
    private final int owner, bookOwner;
    private final OriginalLendActions lends;
    private final OriginalLendRepository repository;
    private final OriginalAutoTransferCommands.FeeBookResolver feeBooks;
    private final OriginalFinancialOrchestrator financial;
    private final LongSupplier clock;

    public OriginalAutoCreditRepaymentCommands(ReferenceStore store, int owner, int bookOwner,
            OriginalLendActions lends, OriginalAutoTransferCommands.FeeBookResolver feeBooks) {
        this(store, owner, bookOwner, lends, feeBooks, System::currentTimeMillis,
                new OriginalFinancialOrchestrator(store, owner, bookOwner));
    }
    OriginalAutoCreditRepaymentCommands(ReferenceStore store, int owner, int bookOwner,
            OriginalLendActions lends, OriginalAutoTransferCommands.FeeBookResolver feeBooks,
            LongSupplier clock, OriginalFinancialOrchestrator financial) {
        if (store == null || lends == null || feeBooks == null || clock == null || financial == null)
            throw new IllegalArgumentException("Repayment dependencies are required");
        this.store = store; this.owner = owner; this.bookOwner = bookOwner; this.lends = lends;
        this.feeBooks = feeBooks; this.clock = clock; this.financial = financial;
        repository = new OriginalLendRepository(store);
    }

    /** The outer U0 owns checkbox/type routing. X itself does not add a positive-amount gate. */
    public Result save(Long selectedFrom, Long selectedTo, double amount, String feeText, int mode,
            String remark, long time, Projection projection, Effects effects, Attachments attachments) {
        if (selectedFrom == null) throw new IllegalArgumentException("请选择转出账户");
        if (selectedTo == null) throw new IllegalArgumentException("请选择转入账户");
        if (selectedFrom.longValue() == selectedTo.longValue()) throw new IllegalArgumentException("请选择不同的账户");
        if (feeText == null || projection == null || effects == null) throw new IllegalArgumentException("Repayment input is required");
        synchronized (store) {
            if ((store.getWritableDatabase().inTransaction() && !AccountingCommitScope.active(store)))
                throw new IllegalStateException("Original repayment stages require independent saves");
            // Functional user entries cannot record a transfer whose principal was skipped.
            // Direct raw-source callers outside the commit scope retain their recovered behavior.
            if(AccountingCommitScope.active(store)) {
                if(!Double.isFinite(amount)||amount<=0.0d)throw new IllegalArgumentException("转账或还款本金必须大于零");
                if(mode==1&&!feeText.isEmpty()&&OriginalTransferAmount.isJ(feeText)
                        &&amount<=OriginalTransferAmount.parseS(feeText))throw new IllegalArgumentException("到账金额必须大于零");
            }
            JSONObject from = store.assetRepository().p(selectedFrom, owner), to = store.assetRepository().p(selectedTo, owner);
            double principal = amount; boolean skipped = false;
            if (mode == 0 || feeText.isEmpty() || !OriginalTransferAmount.isJ(feeText)) {
                financial.changeAsset(to.optLong("semantic_id"), Math.abs(principal), false, "从" + from.optString("name") + "转入");
                financial.changeAsset(from.optLong("semantic_id"), Math.abs(principal), true, "还款给" + to.optString("name"));
            } else if (principal <= OriginalTransferAmount.parseS(feeText)) {
                skipped = true; effects.warningNonPositiveArrival();
            } else {
                principal -= OriginalTransferAmount.parseS(feeText);
                financial.changeAsset(to.optLong("semantic_id"), principal, false, "从" + from.optString("name") + "转入");
                financial.changeAsset(from.optLong("semantic_id"), principal, true, "还款给" + to.optString("name"));
            }
            double interest = feeText.isEmpty() || !OriginalTransferAmount.isN(feeText) ? 0.0d : OriginalTransferAmount.parseS(feeText);
            OriginalBillRecord bill = null; OriginalBillRepository.SaveResult billSave = null; int billId = -1;
            if (interest != 0.0d) {
                bill = new OriginalBillRecord(); bill.time = time;
                bill.remark = to.optString("name") + (interest > 0.0d ? " 还款利息" : " 还款优惠");
                OriginalBillRecord previous = repository.latestInterestBill(owner, bookOwner, 4, interest < 0.0d);
                if (previous != null) {
                    bill.parentCategoryId = previous.parentCategoryId; bill.childCategoryId = previous.childCategoryId;
                    bill.notIntoBudget = previous.notIntoBudget; bill.notIntoTotal = previous.notIntoTotal;
                    bill.tags = previous.tags == null ? null : new ArrayList<>(new LinkedHashSet<>(previous.tags));
                } else if (interest > 0.0d) bill.parentCategoryId = 99;
                else { bill.parentCategoryId = 9; bill.childCategoryId = 911; }
                bill.cost = Math.abs(interest); bill.recordTime = clock.getAsLong(); bill.billType = 1;
                bill.assetId = from.optLong("semantic_id"); bill.bookId = feeBooks.resolve(bill.assetId); bill.userId = owner;
                billSave = financial.saveNormal(bill, projection.project(bill), true); billId = bill.billId;
            }
            OriginalLendRecord lend = new OriginalLendRecord();
            lend.assetId = to.optLong("semantic_id"); lend.billId = billId; lend.outTime = time;
            lend.remark = remark; lend.number = Math.abs(principal); lend.interest = interest; lend.type = 4;
            // X's null check is real: with skipped principal and no N-fee a vanished
            // fresh source object can reach this stage, preserving repaymentAssetId=0.
            if (from != null) lend.repaymentAssetId = from.optLong("semantic_id");
            OriginalLendActions.Result action = lends.add(lend);
            boolean copied = attachments != null;
            if (copied) attachments.copyAndLink(action.semanticId);
            effects.repaymentChanged(lend, action.save.saved);
            return new Result(action, bill, billSave, skipped, copied);
        }
    }
}
