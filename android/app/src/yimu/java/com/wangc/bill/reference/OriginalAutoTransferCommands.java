package com.wangc.bill.reference;

import android.content.ContentValues;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.function.LongSupplier;
import org.json.JSONObject;

/** AutoAddLayout.Z only. Checked credit repayment belongs to the distinct X/Lend action. */
public final class OriginalAutoTransferCommands {
    @FunctionalInterface public interface FeeBookResolver {
        /** The source Asset.getAccountBookId() result, never the overlay header's local PK. */
        long resolve(long sourceAssetSemanticId);
    }
    @FunctionalInterface public interface Projection { ContentValues project(OriginalBillRecord feeBill); }
    @FunctionalInterface public interface Effects { void warningNonPositiveArrival(); }
    @FunctionalInterface public interface Attachments {
        /** Original a2.g -> r3.L stage; a host JSON attachment list is not a BillFile implementation. */
        void copyAndLink(long transferSemanticId);
    }
    public static final class Result {
        public final OriginalTransferRecord transfer;
        public final OriginalTransferActions.Result transferSave;
        public final OriginalBillRecord feeBill;
        public final OriginalBillRepository.SaveResult feeBillSave;
        /** The entity save's boolean, separately from any fee save and earlier balance effects. */
        public final boolean persistedTransfer, saved, principalSkipped, attachmentsInvoked;
        private Result(OriginalTransferActions.Result transferSave, OriginalBillRecord feeBill,
                OriginalBillRepository.SaveResult feeBillSave, boolean principalSkipped, boolean attachmentsInvoked) {
            this.transferSave = transferSave; transfer = transferSave.record;
            this.feeBill = feeBill; this.feeBillSave = feeBillSave;
            persistedTransfer = transferSave.save.saved;
            saved = persistedTransfer && (feeBillSave == null || feeBillSave.saved);
            this.principalSkipped = principalSkipped; this.attachmentsInvoked = attachmentsInvoked;
        }
        public String firstFailureType() {
            return feeBillSave != null && !feeBillSave.saved ? feeBillSave.failureType : transferSave.save.failureType;
        }
    }

    private final ReferenceStore store;
    private final int owner, bookOwner;
    private final OriginalTransferActions transfers;
    private final FeeBookResolver feeBooks;
    private final OriginalFinancialOrchestrator financial;
    private final OriginalTransferRepository repository;
    private final LongSupplier clock;

    public OriginalAutoTransferCommands(ReferenceStore store, int owner, int bookOwner,
            OriginalTransferActions transfers, FeeBookResolver feeBooks) {
        this(store, owner, bookOwner, transfers, feeBooks, System::currentTimeMillis,
                new OriginalFinancialOrchestrator(store, owner, bookOwner));
    }
    OriginalAutoTransferCommands(ReferenceStore store, int owner, int bookOwner,
            OriginalTransferActions transfers, FeeBookResolver feeBooks, LongSupplier clock,
            OriginalFinancialOrchestrator financial) {
        if (store == null || transfers == null || feeBooks == null || clock == null || financial == null)
            throw new IllegalArgumentException("Transfer dependencies are required");
        this.store = store; this.owner = owner; this.bookOwner = bookOwner;
        this.transfers = transfers; this.feeBooks = feeBooks; this.clock = clock; this.financial = financial;
        repository = new OriginalTransferRepository(store);
    }

    /**
     * j is U0's parsed amount, not a minor-unit number. No positive/zero check is
     * added. The caller owns FX/host-storage admission and repayment routing.
     * Each account, fee Bill and Transfer is independently saved, including false.
     */
    public Result save(Long selectedFrom, Long selectedTo, double j, String feeText, int mode,
            String remark, long time, Projection projection, Effects effects, Attachments attachments) {
        // These are the three original selection/toast gates, before fresh P lookups.
        if (selectedFrom == null) throw new IllegalArgumentException("请选择转出账户");
        if (selectedTo == null) throw new IllegalArgumentException("请选择转入账户");
        if (selectedFrom.longValue() == selectedTo.longValue()) throw new IllegalArgumentException("请选择不同的账户");
        if (feeText == null || projection == null || effects == null) throw new IllegalArgumentException("Transfer input is required");
        synchronized (store) {
            if (store.getWritableDatabase().inTransaction())
                throw new IllegalStateException("Original Transfer stages require independent saves");
            // f.P ignores showBook, hide and type. Missing freshly queried objects
            // are not replaced with selected/stale objects or an invented account.
            JSONObject from = store.assetRepository().p(selectedFrom, owner);
            JSONObject to = store.assetRepository().p(selectedTo, owner);
            String fromName = from.optString("name"), toName = to.optString("name");
            long fromId = from.optLong("semantic_id"), toId = to.optLong("semantic_id");
            double principal = j;
            boolean skipped = false;
            boolean feeJ = OriginalTransferAmount.isJ(feeText);
            if (mode == 0 || feeText.isEmpty() || !feeJ) {
                financial.changeAsset(toId, Math.abs(principal), false, "从" + fromName + "转入");
                financial.changeAsset(fromId, Math.abs(principal), true, "转出到" + toName);
            } else if (principal <= OriginalTransferAmount.parseS(feeText)) {
                // Real Z pc0x73 continues to fee/Transfer saving after this toast.
                skipped = true; effects.warningNonPositiveArrival();
            } else {
                principal -= OriginalTransferAmount.parseS(feeText);
                financial.changeAsset(toId, principal, false, "从" + fromName + "转入");
                financial.changeAsset(fromId, principal, true, "转出到" + toName);
            }

            double fee = feeText.isEmpty() || !OriginalTransferAmount.isN(feeText)
                    ? 0.0d : OriginalTransferAmount.parseS(feeText);
            OriginalBillRecord feeBill = null;
            OriginalBillRepository.SaveResult feeSave = null;
            int billId = -1;
            if (fee != 0.0d) {
                feeBill = new OriginalBillRecord(); feeBill.time = time;
                feeBill.remark = fromName + (fee > 0.0d ? " 转账手续费" : " 转账优惠");
                OriginalBillRecord previous = repository.latestFeeBill(owner, bookOwner, fee < 0.0d);
                if (previous != null) {
                    feeBill.parentCategoryId = previous.parentCategoryId;
                    feeBill.childCategoryId = previous.childCategoryId;
                    feeBill.notIntoBudget = previous.notIntoBudget; feeBill.notIntoTotal = previous.notIntoTotal;
                    // This call is Bill.setTags, whose explicit setter deduplicates.
                    feeBill.tags = previous.tags == null ? null : new ArrayList<>(new LinkedHashSet<>(previous.tags));
                } else if (fee > 0.0d) feeBill.parentCategoryId = 99;
                else { feeBill.parentCategoryId = 9; feeBill.childCategoryId = 911; }
                feeBill.cost = Math.abs(fee); feeBill.recordTime = clock.getAsLong(); feeBill.billType = 1;
                feeBill.assetId = fromId;
                // Invoke the dynamic resolver only when the source constructs a fee Bill.
                feeBill.bookId = feeBooks.resolve(fromId); feeBill.userId = owner;
                feeSave = financial.saveNormal(feeBill, projection.project(feeBill), true);
                billId = feeBill.billId; // a0.g returns this semantic ID even after save=false.
            }
            OriginalTransferRecord transfer = new OriginalTransferRecord();
            transfer.fromAssetId = fromId; transfer.toAssetId = toId; transfer.cost = principal; transfer.billId = billId;
            if (feeJ) transfer.serviceCharge = OriginalTransferAmount.parseS(feeText);
            transfer.time = time; transfer.remark = remark;
            // Fresh tags remain [], and toCost remains 0: Auto Z performs no FX assignment.
            OriginalTransferActions.Result transferSave = transfers.save(transfer);
            boolean attachmentsInvoked = attachments != null;
            if (attachmentsInvoked) attachments.copyAndLink(transferSave.semanticId);
            return new Result(transferSave, feeBill, feeSave, skipped, attachmentsInvoked);
        }
    }
}
