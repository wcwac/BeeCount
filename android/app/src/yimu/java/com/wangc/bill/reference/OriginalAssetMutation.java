package com.wangc.bill.reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Numeric stages of the recovered f.t1, classes4 method 29425, code 0x4fc4dc.
 *
 * This class performs no lookup, persistence, callback, currency conversion or clock read.
 * The caller must finish each stage's ordered effects before advancing to the next stage.
 * In particular, a missing new account does not undo the already completed old-account stage.
 * This is not an atomic database plan. Exceptions from real effects must propagate; a false
 * save return, unlike an exception, did not stop the original method.
 */
public final class OriginalAssetMutation {
    private OriginalAssetMutation() {}

    public enum Effect {
        SET_OLD_BILL_COST_IN_MEMORY,
        SET_OLD_ACCOUNT_BALANCE, STAMP_OLD_ACCOUNT, SAVE_OLD_ACCOUNT,
        IMMEDIATE_OLD_ACCOUNT_CALLBACK,
        CREATE_OLD_HISTORY, WRITE_OLD_HISTORY,
        SET_NEW_ACCOUNT_BALANCE, STAMP_NEW_ACCOUNT, SAVE_NEW_ACCOUNT,
        SCHEDULE_NEW_ACCOUNT_CALLBACK_AFTER_1000_MS,
        CREATE_NEW_HISTORY, WRITE_NEW_HISTORY
    }

    public enum HistoryKind { ADD, ACCOUNT_CHANGED, AMOUNT_EDIT }
    public enum NewAccountStatus { FOUND, NO_ACCOUNT_SENTINEL, LOOKUP_MISSING }

    /** IDs are original semantic IDs, including raw zero; they are not local row IDs. */
    public static final class IncomingBill {
        public final int billId, parentCategoryId;
        public final long assetId;
        public final boolean hasCurrencyInfo;

        public IncomingBill(int billId, int parentCategoryId, long assetId,
                boolean hasCurrencyInfo) {
            this.billId = billId;
            this.parentCategoryId = parentCategoryId;
            this.assetId = assetId;
            this.hasCurrencyInfo = hasCurrencyInfo;
        }
    }

    public static final class OldBill {
        public final int parentCategoryId;
        public final long assetId;
        public final double cost;

        public OldBill(int parentCategoryId, long assetId, double cost) {
            this.parentCategoryId = parentCategoryId;
            this.assetId = assetId;
            this.cost = cost;
        }
    }

    /**
     * Asset.getAssetNumber reads the stored field for every integer type except 9.
     * Type 9's getter is derived from reimbursements and requires a separate adapter.
     * No finite-number filter is added: the original numeric path admits NaN and infinities.
     */
    public static final class AccountValue {
        public final long assetId;
        public final int assetType;
        public final double balance;

        public AccountValue(long assetId, int assetType, double balance) {
            if (assetType == 9) {
                throw new IllegalArgumentException("Unsupported original account balance semantics");
            }
            this.assetId = assetId;
            this.assetType = assetType;
            this.balance = balance;
        }
    }

    public static final class History {
        public final long assetId;
        public final HistoryKind kind;
        public final double change, currentBalance;

        private History(long assetId, HistoryKind kind, double change, double currentBalance) {
            this.assetId = assetId;
            this.kind = kind;
            this.change = change;
            this.currentBalance = currentBalance;
        }

        /** Exact cmpl-double/if-eqz behavior: both zeros skip; NaN does not skip. */
        public boolean shouldWrite() { return change != 0.0d; }
    }

    /** Supply the already-read a2.F value; it is ignored outside the original refund gate. */
    public static Edit begin(IncomingBill incoming, double refundReaderValue) {
        if (incoming == null) throw new IllegalArgumentException("Missing incoming bill");
        return new Edit(incoming, incoming.billId != 0 && !incoming.hasCurrencyInfo
                ? refundReaderValue : 0.0d);
    }

    public static final class Edit {
        public final IncomingBill incoming;
        public final double refundAmount;

        private Edit(IncomingBill incoming, double refundAmount) {
            this.incoming = incoming;
            this.refundAmount = refundAmount;
        }

        public boolean requiresOldBillLookup() { return incoming.billId != 0; }

        /** Null means the original old-Bill lookup found no row. */
        public OldBillStep oldBill(OldBill found) {
            if (!requiresOldBillLookup() && found != null) {
                throw new IllegalArgumentException("A new bill has no old-bill lookup");
            }
            return new OldBillStep(this, found);
        }
    }

    public static final class OldBillStep {
        public final Edit edit;
        public final OldBill oldBill;
        public final boolean oldLookupAttempted, oldBillFound, memoryCostChanged;
        public final double correctedOldCost;

        private OldBillStep(Edit edit, OldBill oldBill) {
            this.edit = edit;
            this.oldBill = oldBill;
            oldLookupAttempted = edit.requiresOldBillLookup();
            oldBillFound = oldBill != null;
            memoryCostChanged = oldBillFound && oldBill.assetId != -1L;
            correctedOldCost = memoryCostChanged ? Math.abs(oldBill.cost) + edit.refundAmount
                    : oldBillFound ? oldBill.cost : 0.0d;
        }

        public boolean requiresOldAssetLookup() { return memoryCostChanged; }

        public List<Effect> effects() {
            return memoryCostChanged ? ordered(Effect.SET_OLD_BILL_COST_IN_MEMORY)
                    : Collections.emptyList();
        }

        /**
         * After applying the in-memory cost correction, supply the resulting getCurrencyCost
         * value from a separately reviewed conversion. The old Bill itself is not saved here.
         */
        public OldAssetStep oldAsset(AccountValue found, double correctedOldCurrencyCost) {
            if (found != null && (!requiresOldAssetLookup()
                    || found.assetId != oldBill.assetId)) {
                throw new IllegalArgumentException("Old account does not match the lookup");
            }
            return new OldAssetStep(this, found, correctedOldCurrencyCost);
        }
    }

    public static final class OldAssetStep {
        public final OldBillStep billStep;
        public final boolean oldAssetLookupAttempted, oldAssetFound;
        public final boolean oldPathEntered, accountChanged;
        public final double oldBalanceBefore, oldBalanceAfter, oldDelta;
        public final History oldHistory;

        private OldAssetStep(OldBillStep billStep, AccountValue found, double currencyCost) {
            this.billStep = billStep;
            oldAssetLookupAttempted = billStep.requiresOldAssetLookup();
            oldAssetFound = found != null;
            oldPathEntered = oldAssetLookupAttempted && oldAssetFound;
            accountChanged = oldPathEntered
                    && billStep.oldBill.assetId != billStep.edit.incoming.assetId;
            oldBalanceBefore = oldAssetFound ? found.balance : 0.0d;
            if (oldPathEntered) {
                double amount = Math.abs(currencyCost);
                boolean income = billStep.oldBill.parentCategoryId == 9;
                oldBalanceAfter = income ? found.balance - amount : found.balance + amount;
                oldDelta = income ? amount * -1.0d : amount;
            } else {
                oldBalanceAfter = oldBalanceBefore;
                oldDelta = 0.0d;
            }
            oldHistory = accountChanged ? new History(found.assetId, HistoryKind.ACCOUNT_CHANGED,
                    oldDelta, oldBalanceAfter) : null;
        }

        public List<Effect> effectsBeforeSaveResult() {
            return oldPathEntered ? ordered(Effect.SET_OLD_ACCOUNT_BALANCE,
                    Effect.STAMP_OLD_ACCOUNT, Effect.SAVE_OLD_ACCOUNT) : Collections.emptyList();
        }

        /** Call after the actual old save returns normally, even when its return is false. */
        public OldSavedStep afterOldSave(boolean returned) {
            if (!oldPathEntered) throw new IllegalStateException("No old-account save was attempted");
            return new OldSavedStep(this, returned);
        }

        public NewLookup withoutOldAssetEffects() {
            if (oldPathEntered) throw new IllegalStateException("Finish the old-account effects first");
            return new NewLookup(this, null);
        }
    }

    public static final class OldSavedStep {
        public final OldAssetStep oldStep;
        /** Observed return only; the original did not branch on this value. */
        public final boolean oldSaveReturned;

        private OldSavedStep(OldAssetStep oldStep, boolean returned) {
            this.oldStep = oldStep;
            oldSaveReturned = returned;
        }

        public List<Effect> effects() {
            if (oldStep.oldHistory == null) return ordered(Effect.IMMEDIATE_OLD_ACCOUNT_CALLBACK);
            return oldStep.oldHistory.shouldWrite()
                    ? ordered(Effect.IMMEDIATE_OLD_ACCOUNT_CALLBACK, Effect.CREATE_OLD_HISTORY,
                            Effect.WRITE_OLD_HISTORY)
                    : ordered(Effect.IMMEDIATE_OLD_ACCOUNT_CALLBACK, Effect.CREATE_OLD_HISTORY);
        }

        /** Advance only after the immediate callback and conditional old history are complete. */
        public NewLookup afterImmediateCallbackAndHistory() {
            return new NewLookup(oldStep, Boolean.valueOf(oldSaveReturned));
        }
    }

    public static final class NewLookup {
        public final OldAssetStep oldStep;
        /** Null means no old-account save path; false is not the same as no old path. */
        public final Boolean oldSaveReturned;

        private NewLookup(OldAssetStep oldStep, Boolean oldSaveReturned) {
            this.oldStep = oldStep;
            this.oldSaveReturned = oldSaveReturned;
        }

        public boolean requiresNewAssetLookup() { return incoming().assetId != -1L; }
        private IncomingBill incoming() { return oldStep.billStep.edit.incoming; }

        /**
         * Supply a fresh post-old-effects lookup, including for the same account ID. Do not
         * assume old save succeeded or reuse its predicted balance as a database read result.
         */
        public NewAssetStep newAsset(AccountValue fresh, double resolvedNewCurrencyCost) {
            if (fresh != null && (!requiresNewAssetLookup() || fresh.assetId != incoming().assetId)) {
                throw new IllegalArgumentException("New account does not match the lookup");
            }
            return new NewAssetStep(this, fresh, resolvedNewCurrencyCost);
        }
    }

    public static final class NewAssetStep {
        public final NewLookup lookup;
        public final NewAccountStatus status;
        public final double balanceBefore, balanceAfter, amountWithRefund;
        public final History history;

        private NewAssetStep(NewLookup lookup, AccountValue fresh, double currencyCost) {
            this.lookup = lookup;
            status = !lookup.requiresNewAssetLookup() ? NewAccountStatus.NO_ACCOUNT_SENTINEL
                    : fresh == null ? NewAccountStatus.LOOKUP_MISSING : NewAccountStatus.FOUND;
            balanceBefore = fresh == null ? 0.0d : fresh.balance;
            if (status != NewAccountStatus.FOUND) {
                balanceAfter = balanceBefore;
                amountWithRefund = 0.0d;
                history = null;
                return;
            }
            double amount = Math.abs(currencyCost);
            double refund = lookup.oldStep.billStep.edit.refundAmount;
            boolean income = lookup.incoming().parentCategoryId == 9;
            amountWithRefund = amount + refund;
            balanceAfter = income ? fresh.balance + amountWithRefund
                    : fresh.balance - amountWithRefund;
            HistoryKind kind;
            double delta;
            if (!lookup.oldStep.oldPathEntered) {
                kind = HistoryKind.ADD;
                delta = income ? amount + refund : (amount * -1.0d) - refund;
            } else if (lookup.oldStep.accountChanged) {
                kind = HistoryKind.ACCOUNT_CHANGED;
                delta = income ? amount + refund : (-amount) - refund;
            } else {
                kind = HistoryKind.AMOUNT_EDIT;
                delta = income ? (amount + refund) + lookup.oldStep.oldDelta
                        : (lookup.oldStep.oldDelta - amount) - refund;
            }
            history = new History(fresh.assetId, kind, delta, balanceAfter);
        }

        public List<Effect> effectsBeforeSaveResult() {
            return status == NewAccountStatus.FOUND ? ordered(Effect.SET_NEW_ACCOUNT_BALANCE,
                    Effect.STAMP_NEW_ACCOUNT, Effect.SAVE_NEW_ACCOUNT) : Collections.emptyList();
        }

        public NewSavedStep afterNewSave(boolean returned) {
            if (status != NewAccountStatus.FOUND) throw new IllegalStateException("No new-account save");
            return new NewSavedStep(this, returned);
        }
    }

    public static final class NewSavedStep {
        public static final long CALLBACK_DELAY_MILLISECONDS = 1000L;
        public final NewAssetStep newStep;
        /** Like the old save, a normally returned false does not suppress subsequent effects. */
        public final boolean newSaveReturned;

        private NewSavedStep(NewAssetStep newStep, boolean returned) {
            this.newStep = newStep;
            newSaveReturned = returned;
        }

        /** Queue the delayed callback before constructing/writing history; do not run it now. */
        public List<Effect> effects() {
            return newStep.history.shouldWrite()
                    ? ordered(Effect.SCHEDULE_NEW_ACCOUNT_CALLBACK_AFTER_1000_MS,
                            Effect.CREATE_NEW_HISTORY, Effect.WRITE_NEW_HISTORY)
                    : ordered(Effect.SCHEDULE_NEW_ACCOUNT_CALLBACK_AFTER_1000_MS,
                            Effect.CREATE_NEW_HISTORY);
        }
    }

    private static List<Effect> ordered(Effect... effects) {
        return Collections.unmodifiableList(Arrays.asList(effects));
    }
}
