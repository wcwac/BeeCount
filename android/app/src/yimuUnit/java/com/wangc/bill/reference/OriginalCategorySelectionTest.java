package com.wangc.bill.reference;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public final class OriginalCategorySelectionTest {
    @Test public void expenseBindingSelectsTheFirstVisibleParentWithoutChoosingAChild() {
        OriginalCategorySelection selection = OriginalCategorySelection.empty()
            .bindExpense(Arrays.asList(99, 8, 7));
        assertEquals(99, selection.expensePair().parentCategoryId);
        assertEquals(-1, selection.expensePair().childCategoryId);
        assertEquals(-1, selection.incomeChildCategoryId);
    }

    @Test public void incomeBindingCanChooseACustomCategoryAheadOfIncomeOther() {
        OriginalCategorySelection selection = OriginalCategorySelection.empty()
            .bindIncome(Arrays.asList(2000001, 911, 910));
        assertEquals(2000001, selection.incomeTopLevelChildCategoryId);
        assertEquals(9, selection.incomePair().parentCategoryId);
        assertEquals(2000001, selection.incomePair().childCategoryId);
    }

    @Test public void selectingAnotherExpenseParentClearsThePreviousChild() {
        OriginalCategorySelection before = OriginalCategorySelection.empty().withExpense(8, 811);
        OriginalCategorySelection after = before.selectExpenseParent(7);
        assertEquals(7, after.expensePair().parentCategoryId);
        assertEquals(-1, after.expensePair().childCategoryId);
        assertEquals(811, before.expenseChildCategoryId);
    }

    @Test public void rebindingForAnotherBookKeepsExistingSelectionsEvenWhenTheyAreHidden() {
        OriginalCategorySelection selected = OriginalCategorySelection.empty()
            .withExpense(8, 811).withIncome(904, 10001);
        OriginalCategorySelection rebound = selected.bindExpense(Arrays.asList(99, 7))
            .bindIncome(Arrays.asList(911, 910));
        assertEquals(8, rebound.expenseParentCategoryId);
        assertEquals(811, rebound.expenseChildCategoryId);
        assertEquals(904, rebound.incomeTopLevelChildCategoryId);
        assertEquals(10001, rebound.incomeChildCategoryId);
    }

    @Test public void selectingANestedIncomeChildKeepsItsTopLevelButSavesUnderCommonParentNine() {
        OriginalCategorySelection nested = OriginalCategorySelection.empty()
            .selectIncomeTopLevel(904).selectIncomeChild(10001);
        assertEquals(904, nested.incomeTopLevelChildCategoryId);
        assertEquals(9, nested.incomePair().parentCategoryId);
        assertEquals(10001, nested.incomePair().childCategoryId);
        assertEquals(910, nested.selectIncomeTopLevel(910).incomeChildCategoryId);
    }

    @Test public void emptyVisibleListsDoNotInventAFallbackOrResetExistingSelections() {
        OriginalCategorySelection empty = OriginalCategorySelection.empty()
            .bindExpense(Collections.emptyList()).bindIncome(Collections.emptyList());
        assertEquals(-1, empty.expenseParentCategoryId);
        assertEquals(-1, empty.incomeChildCategoryId);
        OriginalCategorySelection selected = empty.withExpense(99, -1)
            .bindExpense(Collections.emptyList());
        assertEquals(99, selected.expenseParentCategoryId);
        assertEquals(-1, selected.expenseChildCategoryId);
    }
}
