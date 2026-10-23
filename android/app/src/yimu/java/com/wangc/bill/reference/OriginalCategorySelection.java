package com.wangc.bill.reference;

import java.util.List;

/**
 * Semantic category IDs from the original pager adapters; these are never SQLite row IDs.
 * Bind inputs must already be visible in the chosen book and ordered by positionWeight descending.
 * This helper does not implement native Activity entry initialization or persistence validation.
 */
public final class OriginalCategorySelection {
    public static final int UNSELECTED = -1;

    public final int expenseParentCategoryId;
    public final int expenseChildCategoryId;
    public final int incomeTopLevelChildCategoryId;
    public final int incomeChildCategoryId;

    private OriginalCategorySelection(int expenseParentCategoryId, int expenseChildCategoryId,
            int incomeTopLevelChildCategoryId, int incomeChildCategoryId) {
        this.expenseParentCategoryId = expenseParentCategoryId;
        this.expenseChildCategoryId = expenseChildCategoryId;
        this.incomeTopLevelChildCategoryId = incomeTopLevelChildCategoryId;
        this.incomeChildCategoryId = incomeChildCategoryId;
    }

    public static final class CategoryPair {
        public final int parentCategoryId;
        public final int childCategoryId;

        private CategoryPair(int parentCategoryId, int childCategoryId) {
            this.parentCategoryId = parentCategoryId;
            this.childCategoryId = childCategoryId;
        }
    }

    /** Raw selected semantic IDs; -1 remains the original no-child/unselected sentinel. */
    public CategoryPair expensePair() {
        return new CategoryPair(expenseParentCategoryId, expenseChildCategoryId);
    }

    public CategoryPair incomePair() {
        return new CategoryPair(OriginalCategories.INCOME_PARENT_ID, incomeChildCategoryId);
    }

    public static OriginalCategorySelection empty() {
        return new OriginalCategorySelection(UNSELECTED, UNSELECTED, UNSELECTED, UNSELECTED);
    }

    /** CategoryPagerAdapter.I / AutoCategoryPagerAdapter.I: initialize only while still -1. */
    public OriginalCategorySelection bindExpense(List<Integer> visibleParents) {
        if (expenseParentCategoryId != UNSELECTED || visibleParents.isEmpty()) return this;
        return selectExpenseParent(visibleParents.get(0).intValue());
    }

    /** Income top-level entries are ChildCategory IDs, not the common parent ID 9. */
    public OriginalCategorySelection bindIncome(List<Integer> visibleTopLevelChildren) {
        if (incomeTopLevelChildCategoryId != UNSELECTED || visibleTopLevelChildren.isEmpty()) return this;
        return selectIncomeTopLevel(visibleTopLevelChildren.get(0).intValue());
    }

    /** CategoryPagerAdapter.S0 / AutoCategoryPagerAdapter.M0 never select the first child. */
    public OriginalCategorySelection selectExpenseParent(int categoryId) {
        return withExpense(categoryId, UNSELECTED);
    }

    public OriginalCategorySelection selectExpenseChild(int categoryId) {
        return withExpense(expenseParentCategoryId, categoryId);
    }

    /** Used when an original Bill/BillInfo already supplies both semantic category IDs. */
    public OriginalCategorySelection withExpense(int parentId, int childId) {
        return new OriginalCategorySelection(parentId, childId, incomeTopLevelChildCategoryId, incomeChildCategoryId);
    }

    public OriginalCategorySelection selectIncomeTopLevel(int categoryId) {
        return withIncome(categoryId, categoryId);
    }

    public OriginalCategorySelection selectIncomeChild(int categoryId) {
        return withIncome(incomeTopLevelChildCategoryId, categoryId);
    }

    /** A nested income row supplies its own ID plus its parentChildCategoryId as the top level. */
    public OriginalCategorySelection withIncome(int topLevelId, int childId) {
        return new OriginalCategorySelection(expenseParentCategoryId, expenseChildCategoryId, topLevelId, childId);
    }

    // Original o1/g1(bookId) only changes the adapter's book and notifies it. Rebinding
    // with a new visible list therefore preserves every existing non--1 selection,
    // including one hidden in the newly selected book. Do not silently replace it here.
    // The editor and automatic-entry grids share this state, while retaining their
    // distinct manual -1 / automatic omitted-child 0 persistence conventions.
}
