package com.wangc.bill.reference;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.wangc.bill.reference.OriginalAssetMutation.Effect.*;
import static com.wangc.bill.reference.OriginalAssetMutation.HistoryKind.*;
import static com.wangc.bill.reference.OriginalAssetMutation.NewAccountStatus.*;

public final class OriginalAssetMutationTest {
    private static OriginalAssetMutation.IncomingBill bill(int id, int parent, long account) {
        return new OriginalAssetMutation.IncomingBill(id, parent, account, false);
    }

    private static OriginalAssetMutation.AccountValue account(long id, double balance) {
        return new OriginalAssetMutation.AccountValue(id, 1, balance);
    }

    private static OriginalAssetMutation.OldAssetStep old(
            OriginalAssetMutation.IncomingBill incoming, double refund,
            OriginalAssetMutation.OldBill previous, double oldBalance, double oldCurrencyCost) {
        return OriginalAssetMutation.begin(incoming, refund).oldBill(previous)
                .oldAsset(account(previous.assetId, oldBalance), oldCurrencyCost);
    }

    private static OriginalAssetMutation.NewLookup finishOld(
            OriginalAssetMutation.OldAssetStep step, boolean saved) {
        return step.afterOldSave(saved).afterImmediateCallbackAndHistory();
    }

    private static void bits(double expected, double actual) {
        assertEquals(Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(actual));
    }

    @Test public void newIncomeIgnoresRefundReaderAndHasNoOldLookup() {
        OriginalAssetMutation.Edit edit = OriginalAssetMutation.begin(bill(0, 9, 10), 90.0d);
        assertFalse(edit.requiresOldBillLookup());
        bits(0.0d, edit.refundAmount);
        OriginalAssetMutation.OldAssetStep skipped = edit.oldBill(null).oldAsset(null, Double.NaN);
        assertFalse(skipped.billStep.oldLookupAttempted);
        assertFalse(skipped.oldPathEntered);
        OriginalAssetMutation.NewAssetStep added = skipped.withoutOldAssetEffects()
                .newAsset(account(10, 100.0d), 5.0d);
        assertEquals(105.0d, added.balanceAfter, 0.0d);
        assertEquals(ADD, added.history.kind);
        assertEquals(5.0d, added.history.change, 0.0d);
    }

    @Test public void newExpenseUsesAbsoluteCostBeforeSubtractingFromBalance() {
        OriginalAssetMutation.NewAssetStep added = OriginalAssetMutation.begin(bill(0, 7, 10), 0.0d)
                .oldBill(null).oldAsset(null, 0.0d).withoutOldAssetEffects()
                .newAsset(account(10, 100.0d), -5.0d);
        assertEquals(95.0d, added.balanceAfter, 0.0d);
        assertEquals(-5.0d, added.history.change, 0.0d);
        assertEquals(ADD, added.history.kind);
    }

    @Test public void currencyInfoSuppressesRefundEvenOnAnExistingBill() {
        OriginalAssetMutation.Edit edit = OriginalAssetMutation.begin(
                new OriginalAssetMutation.IncomingBill(12, 7, 10, true), Double.NaN);
        assertTrue(edit.requiresOldBillLookup());
        bits(0.0d, edit.refundAmount);
        OriginalAssetMutation.OldBillStep previous = edit.oldBill(
                new OriginalAssetMutation.OldBill(7, 10, -20.0d));
        assertEquals(20.0d, previous.correctedOldCost, 0.0d);
    }

    @Test public void refundIsAddedToOldMemoryCostAndNewAccountOperand() {
        OriginalAssetMutation.OldBillStep previous = OriginalAssetMutation.begin(bill(12, 9, 20), 10.0d)
                .oldBill(new OriginalAssetMutation.OldBill(7, 10, -20.0d));
        assertEquals(30.0d, previous.correctedOldCost, 0.0d);
        assertEquals(Arrays.asList(SET_OLD_BILL_COST_IN_MEMORY), previous.effects());
        OriginalAssetMutation.OldAssetStep reversed = previous.oldAsset(account(10, 100.0d), 30.0d);
        assertEquals(130.0d, reversed.oldBalanceAfter, 0.0d);
        OriginalAssetMutation.NewAssetStep applied = finishOld(reversed, true)
                .newAsset(account(20, 200.0d), 40.0d);
        assertEquals(50.0d, applied.amountWithRefund, 0.0d);
        assertEquals(250.0d, applied.balanceAfter, 0.0d);
        assertEquals(30.0d, reversed.oldHistory.change, 0.0d);
        assertEquals(ACCOUNT_CHANGED, applied.history.kind);
        assertEquals(50.0d, applied.history.change, 0.0d);
    }

    @Test public void sameExpenseRestoresOldAmountThenRecordsOnlyTheDifference() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 7, 10), 0.0d,
                new OriginalAssetMutation.OldBill(7, 10, 20.0d), 100.0d, 20.0d);
        assertTrue(reversed.oldPathEntered);
        assertFalse(reversed.accountChanged);
        assertNull(reversed.oldHistory);
        assertEquals(120.0d, reversed.oldBalanceAfter, 0.0d);
        OriginalAssetMutation.NewAssetStep applied = finishOld(reversed, true)
                .newAsset(account(10, 120.0d), 30.0d);
        assertEquals(90.0d, applied.balanceAfter, 0.0d);
        assertEquals(AMOUNT_EDIT, applied.history.kind);
        assertEquals(-10.0d, applied.history.change, 0.0d);
    }

    @Test public void sameIncomeReversesOldIncomeBeforeAddingTheNewIncome() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 9, 10), 0.0d,
                new OriginalAssetMutation.OldBill(9, 10, 20.0d), 100.0d, 20.0d);
        assertEquals(80.0d, reversed.oldBalanceAfter, 0.0d);
        assertEquals(-20.0d, reversed.oldDelta, 0.0d);
        OriginalAssetMutation.NewAssetStep applied = finishOld(reversed, true)
                .newAsset(account(10, 80.0d), 30.0d);
        assertEquals(110.0d, applied.balanceAfter, 0.0d);
        assertEquals(10.0d, applied.history.change, 0.0d);
    }

    @Test public void changingDirectionOnTheSameAccountIncludesBothSignedChanges() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 7, 10), 0.0d,
                new OriginalAssetMutation.OldBill(9, 10, 20.0d), 100.0d, 20.0d);
        OriginalAssetMutation.NewAssetStep applied = finishOld(reversed, true)
                .newAsset(account(10, 80.0d), 30.0d);
        assertEquals(50.0d, applied.balanceAfter, 0.0d);
        assertEquals(AMOUNT_EDIT, applied.history.kind);
        assertEquals(-50.0d, applied.history.change, 0.0d);
    }

    @Test public void missingNewAccountDoesNotEraseTheAlreadyEnteredOldAccountPath() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 7, 20), 0.0d,
                new OriginalAssetMutation.OldBill(7, 10, 20.0d), 100.0d, 20.0d);
        OriginalAssetMutation.NewLookup lookup = finishOld(reversed, true);
        assertTrue(lookup.requiresNewAssetLookup());
        OriginalAssetMutation.NewAssetStep missing = lookup.newAsset(null, Double.NaN);
        assertEquals(LOOKUP_MISSING, missing.status);
        assertTrue(missing.lookup.oldStep.oldPathEntered);
        assertEquals(120.0d, missing.lookup.oldStep.oldBalanceAfter, 0.0d);
        assertTrue(missing.lookup.oldStep.oldHistory.shouldWrite());
        assertNull(missing.history);
        assertTrue(missing.effectsBeforeSaveResult().isEmpty());
    }

    @Test public void noAccountSentinelIsCheckedOnlyAfterOldEffects() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 7, -1), 0.0d,
                new OriginalAssetMutation.OldBill(7, 10, 20.0d), 100.0d, 20.0d);
        OriginalAssetMutation.NewLookup lookup = finishOld(reversed, true);
        assertFalse(lookup.requiresNewAssetLookup());
        OriginalAssetMutation.NewAssetStep skipped = lookup.newAsset(null, 30.0d);
        assertEquals(NO_ACCOUNT_SENTINEL, skipped.status);
        assertTrue(reversed.oldPathEntered);
        assertEquals(ACCOUNT_CHANGED, reversed.oldHistory.kind);
        assertNull(skipped.history);
    }

    @Test public void missingOldBillStillUsesRefundGateButNewHistoryIsAdd() {
        OriginalAssetMutation.OldAssetStep missing = OriginalAssetMutation.begin(bill(12, 7, 10), 5.0d)
                .oldBill(null).oldAsset(null, 0.0d);
        assertTrue(missing.billStep.oldLookupAttempted);
        assertFalse(missing.billStep.oldBillFound);
        assertFalse(missing.oldAssetLookupAttempted);
        OriginalAssetMutation.NewAssetStep added = missing.withoutOldAssetEffects()
                .newAsset(account(10, 100.0d), 10.0d);
        assertEquals(85.0d, added.balanceAfter, 0.0d);
        assertEquals(ADD, added.history.kind);
        assertEquals(-15.0d, added.history.change, 0.0d);
    }

    @Test public void oldNoAccountSentinelSkipsEvenTheOldCostMutation() {
        OriginalAssetMutation.OldBillStep previous = OriginalAssetMutation.begin(bill(12, 7, 10), 5.0d)
                .oldBill(new OriginalAssetMutation.OldBill(7, -1, -20.0d));
        assertTrue(previous.oldBillFound);
        assertFalse(previous.memoryCostChanged);
        assertFalse(previous.requiresOldAssetLookup());
        assertEquals(-20.0d, previous.correctedOldCost, 0.0d);
        assertTrue(previous.effects().isEmpty());
        assertFalse(previous.oldAsset(null, 0.0d).oldPathEntered);
    }

    @Test public void missingOldAccountDoesNotUndoTheOldInMemoryCostCorrection() {
        OriginalAssetMutation.OldBillStep previous = OriginalAssetMutation.begin(bill(12, 7, 10), 5.0d)
                .oldBill(new OriginalAssetMutation.OldBill(7, 10, -20.0d));
        assertEquals(25.0d, previous.correctedOldCost, 0.0d);
        OriginalAssetMutation.OldAssetStep missing = previous.oldAsset(null, Double.NaN);
        assertTrue(missing.oldAssetLookupAttempted);
        assertFalse(missing.oldAssetFound);
        assertFalse(missing.oldPathEntered);
        assertNull(missing.oldHistory);
        assertEquals(25.0d, missing.billStep.correctedOldCost, 0.0d);
    }

    @Test public void falseOldSaveDoesNotChangeFlagsOrInventASuccessfulFreshBalance() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 7, 10), 0.0d,
                new OriginalAssetMutation.OldBill(7, 10, 20.0d), 100.0d, 20.0d);
        OriginalAssetMutation.NewLookup lookup = finishOld(reversed, false);
        assertEquals(Boolean.FALSE, lookup.oldSaveReturned);
        assertTrue(lookup.oldStep.oldPathEntered);
        // The caller actually read 100 after a false save; the kernel must not substitute 120.
        OriginalAssetMutation.NewAssetStep applied = lookup.newAsset(account(10, 100.0d), 30.0d);
        assertEquals(70.0d, applied.balanceAfter, 0.0d);
        assertEquals(AMOUNT_EDIT, applied.history.kind);
        assertEquals(-10.0d, applied.history.change, 0.0d);
    }

    @Test public void falseNewSaveStillSchedulesCallbackBeforeWritingHistory() {
        OriginalAssetMutation.NewAssetStep added = OriginalAssetMutation.begin(bill(0, 7, 10), 0.0d)
                .oldBill(null).oldAsset(null, 0.0d).withoutOldAssetEffects()
                .newAsset(account(10, 100.0d), 5.0d);
        OriginalAssetMutation.NewSavedStep saved = added.afterNewSave(false);
        assertFalse(saved.newSaveReturned);
        assertEquals(1000L, OriginalAssetMutation.NewSavedStep.CALLBACK_DELAY_MILLISECONDS);
        assertEquals(Arrays.asList(SCHEDULE_NEW_ACCOUNT_CALLBACK_AFTER_1000_MS,
                CREATE_NEW_HISTORY, WRITE_NEW_HISTORY), saved.effects());
    }

    @Test public void oldSaveImmediateCallbackAndHistoryAreSeparateOrderedBoundaries() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 7, 20), 0.0d,
                new OriginalAssetMutation.OldBill(9, 10, 20.0d), 100.0d, 20.0d);
        assertEquals(Arrays.asList(SET_OLD_ACCOUNT_BALANCE, STAMP_OLD_ACCOUNT, SAVE_OLD_ACCOUNT),
                reversed.effectsBeforeSaveResult());
        OriginalAssetMutation.OldSavedStep saved = reversed.afterOldSave(true);
        assertEquals(Arrays.asList(IMMEDIATE_OLD_ACCOUNT_CALLBACK, CREATE_OLD_HISTORY,
                WRITE_OLD_HISTORY), saved.effects());
        OriginalAssetMutation.NewAssetStep next = saved.afterImmediateCallbackAndHistory()
                .newAsset(account(20, 200.0d), 30.0d);
        assertEquals(Arrays.asList(SET_NEW_ACCOUNT_BALANCE, STAMP_NEW_ACCOUNT, SAVE_NEW_ACCOUNT),
                next.effectsBeforeSaveResult());
        assertEquals(-20.0d, reversed.oldHistory.change, 0.0d);
        assertEquals(80.0d, reversed.oldHistory.currentBalance, 0.0d);
    }

    @Test public void positiveAndNegativeZeroHistoriesSkipWithoutSuppressingSavesOrCallbacks() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 7, 20), 0.0d,
                new OriginalAssetMutation.OldBill(9, 10, 0.0d), 100.0d, -0.0d);
        bits(-0.0d, reversed.oldDelta);
        assertFalse(reversed.oldHistory.shouldWrite());
        assertEquals(Arrays.asList(IMMEDIATE_OLD_ACCOUNT_CALLBACK, CREATE_OLD_HISTORY),
                reversed.afterOldSave(true).effects());
        OriginalAssetMutation.NewAssetStep added = OriginalAssetMutation.begin(bill(0, 7, 20), 0.0d)
                .oldBill(null).oldAsset(null, 0.0d).withoutOldAssetEffects()
                .newAsset(account(20, 100.0d), -0.0d);
        bits(-0.0d, added.history.change);
        assertFalse(added.history.shouldWrite());
        assertEquals(3, added.effectsBeforeSaveResult().size());
        assertEquals(Arrays.asList(SCHEDULE_NEW_ACCOUNT_CALLBACK_AFTER_1000_MS,
                CREATE_NEW_HISTORY), added.afterNewSave(true).effects());
        OriginalAssetMutation.NewAssetStep income = OriginalAssetMutation.begin(bill(0, 9, 20), 0.0d)
                .oldBill(null).oldAsset(null, 0.0d).withoutOldAssetEffects()
                .newAsset(account(20, 100.0d), 0.0d);
        bits(0.0d, income.history.change);
        assertFalse(income.history.shouldWrite());
    }

    @Test public void nanIsNotConvertedIntoZeroOrSilentlyRejectedByTheNumericKernel() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 7, 20), Double.NaN,
                new OriginalAssetMutation.OldBill(7, 10, 20.0d), 100.0d, Double.NaN);
        assertTrue(Double.isNaN(reversed.billStep.correctedOldCost));
        assertTrue(Double.isNaN(reversed.oldBalanceAfter));
        assertTrue(reversed.oldHistory.shouldWrite());
        OriginalAssetMutation.NewAssetStep applied = finishOld(reversed, true)
                .newAsset(account(20, 200.0d), 30.0d);
        assertTrue(Double.isNaN(applied.balanceAfter));
        assertTrue(Double.isNaN(applied.history.change));
        assertTrue(applied.history.shouldWrite());
    }

    @Test public void expenseHistoryKeepsSubtractionOrderEvenWhenBalanceChangeRoundsDifferently() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 7, 10), 1.0d,
                new OriginalAssetMutation.OldBill(7, 10, 1.0e16d), 0.0d, 1.0e16d);
        OriginalAssetMutation.NewAssetStep applied = finishOld(reversed, true)
                .newAsset(account(10, 1.0e16d), 1.0e16d);
        // (10^16 - 10^16) - 1 = -1; adding 1 to the balance operand first rounds away.
        assertEquals(-1.0d, applied.history.change, 0.0d);
        bits(0.0d, applied.balanceAfter);
    }

    @Test public void incomeHistoryAddsRefundBeforeAddingTheSignedOldDelta() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 9, 10), 1.0d,
                new OriginalAssetMutation.OldBill(9, 10, 1.0e16d), 1.0e16d, 1.0e16d);
        OriginalAssetMutation.NewAssetStep applied = finishOld(reversed, true)
                .newAsset(account(10, 0.0d), 1.0e16d);
        // (10^16 + 1) + -10^16 rounds to zero; reassociation would yield 1.
        bits(0.0d, applied.history.change);
        assertFalse(applied.history.shouldWrite());
    }

    @Test public void rawZeroAccountIdsRemainRealLookupKeys() {
        OriginalAssetMutation.OldAssetStep reversed = old(bill(12, 7, 0), 0.0d,
                new OriginalAssetMutation.OldBill(7, 0, 20.0d), 100.0d, 20.0d);
        assertTrue(reversed.oldAssetLookupAttempted);
        assertTrue(reversed.oldPathEntered);
        OriginalAssetMutation.NewLookup lookup = finishOld(reversed, true);
        assertTrue(lookup.requiresNewAssetLookup());
        OriginalAssetMutation.NewAssetStep applied = lookup.newAsset(account(0, 120.0d), 30.0d);
        assertEquals(FOUND, applied.status);
        assertEquals(90.0d, applied.balanceAfter, 0.0d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void derivedReimbursementBalanceIsOutsideThisStoredNumericPort() {
        new OriginalAssetMutation.AccountValue(10, 9, 100.0d);
    }

    @Test public void typeEightUsesStoredBalanceForOldRollbackAndNewAmountEdit() {
        OriginalAssetMutation.OldAssetStep reversed = OriginalAssetMutation.begin(bill(12, 7, 10), 0.0d)
                .oldBill(new OriginalAssetMutation.OldBill(7, 10, 12.0d))
                .oldAsset(new OriginalAssetMutation.AccountValue(10, 8, 50.0d), 12.0d);
        assertTrue(reversed.oldPathEntered);
        assertEquals(62.0d, reversed.oldBalanceAfter, 0.0d);
        OriginalAssetMutation.NewAssetStep applied = finishOld(reversed, true)
                .newAsset(new OriginalAssetMutation.AccountValue(10, 8, 62.0d), 20.0d);
        assertEquals(42.0d, applied.balanceAfter, 0.0d);
        assertEquals(AMOUNT_EDIT, applied.history.kind);
        assertEquals(-8.0d, applied.history.change, 0.0d);
        assertEquals(42.0d, applied.history.currentBalance, 0.0d);
    }

    @Test public void defaultZeroTypeUsesTheOriginalStoredGetterWithoutATypeGate() {
        OriginalAssetMutation.OldAssetStep reversed=OriginalAssetMutation.begin(bill(12,7,10),2.0d)
                .oldBill(new OriginalAssetMutation.OldBill(7,10,5.0d))
                .oldAsset(new OriginalAssetMutation.AccountValue(10,0,100.0d),7.0d);
        assertEquals(7.0d,reversed.billStep.correctedOldCost,0.0d);
        assertEquals(107.0d,reversed.oldBalanceAfter,0.0d);
        OriginalAssetMutation.NewAssetStep applied=finishOld(reversed,true)
                .newAsset(new OriginalAssetMutation.AccountValue(10,0,107.0d),10.0d);
        assertEquals(95.0d,applied.balanceAfter,0.0d);
        assertEquals(AMOUNT_EDIT,applied.history.kind);
        assertEquals(-5.0d,applied.history.change,0.0d);
    }

    @Test public void otherIntegerTypesAlsoUseTheOriginalStoredGetter() {
        for(int type:new int[]{99,-7,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
            OriginalAssetMutation.NewLookup lookup=OriginalAssetMutation.begin(bill(0,9,10),0.0d)
                    .oldBill(null).oldAsset(null,0.0d).withoutOldAssetEffects();
            OriginalAssetMutation.NewAssetStep applied=lookup.newAsset(new OriginalAssetMutation.AccountValue(10,type,20.0d),8.0d);
            assertEquals(28.0d,applied.balanceAfter,0.0d);
            assertEquals(ADD,applied.history.kind);
            assertEquals(8.0d,applied.history.change,0.0d);
            assertEquals(28.0d,applied.history.currentBalance,0.0d);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void anEnteredOldPathCannotSkipItsSaveAndImmediateEffects() {
        old(bill(12, 7, 20), 0.0d, new OriginalAssetMutation.OldBill(7, 10, 20.0d),
                100.0d, 20.0d).withoutOldAssetEffects();
    }
}
