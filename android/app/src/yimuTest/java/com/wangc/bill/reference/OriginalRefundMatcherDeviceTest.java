package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Actual raw-Bill SQL queries, including source tiers and the explicit host trash policy. */
@RunWith(AndroidJUnit4.class)
public final class OriginalRefundMatcherDeviceTest {
    private static final String DATABASE = "reference_original_refund_matcher_test.sqlite";
    private Context context;
    private ReferenceStore store;
    private OriginalBillRepository bills;
    private OriginalRefundMatcher matcher;
    @Before public void open() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(DATABASE);
        store = new ReferenceStore(context, DATABASE); store.getWritableDatabase();
        bills = new OriginalBillRepository(store); matcher = new OriginalRefundMatcher(store);
    }
    @After public void close() { if (store != null) store.close(); context.deleteDatabase(DATABASE); }
    private OriginalBillRecord bill(int user, int semantic, int parent, int child, double cost, String remark, long time) {
        OriginalBillRecord bill = new OriginalBillRecord();
        bill.userId = user; bill.billId = semantic; bill.parentCategoryId = parent; bill.childCategoryId = child;
        bill.cost = cost; bill.remark = remark; bill.time = time; bill.bookId = 1; bill.assetId = -1;
        ContentValues projection = new ContentValues();
        projection.put("book_id", 1); projection.put("kind", parent == 9 ? "income" : "expense");
        projection.put("amount", 1); projection.put("currency", "CNY"); projection.put("time", time); projection.put("created", 1);
        bills.saveOriginalRaw(bill, projection); return bill;
    }
    private void erase(OriginalBillRecord bill) { store.getWritableDatabase().delete("transactions", "id=?", new String[]{"" + bill.localRowId}); }
    private long match(double number, String remark, boolean income) { return matcher.findParent(number, remark, income).localRowId; }

    @Test public void fiveTiersPreferStrongerMatchBeforeMoreRecentBill() {
        OriginalBillRecord exactAmount = bill(7, 101, 99, 0, 100, "shop", 10);
        OriginalBillRecord likeAmount = bill(7, 102, 99, 0, 100, "prefix-shop-suffix", 20);
        OriginalBillRecord exactOnly = bill(7, 103, 99, 0, 80, "shop", 30);
        OriginalBillRecord likeOnly = bill(7, 104, 99, 0, 70, "another-shop", 40);
        OriginalBillRecord amountOnly = bill(7, 105, 99, 0, 100, "different", 50);
        assertEquals(exactAmount.localRowId, match(100, "shop", true)); erase(exactAmount);
        assertEquals(likeAmount.localRowId, match(100, "shop", true)); erase(likeAmount);
        assertEquals(exactOnly.localRowId, match(100, "shop", true)); erase(exactOnly);
        assertEquals(likeOnly.localRowId, match(100, "shop", true)); erase(likeOnly);
        assertEquals(amountOnly.localRowId, match(100, "shop", true));
    }
    @Test public void capturedIncomeSeeksExpenseAndCapturedExpenseSeeksIncome() {
        OriginalBillRecord expense = bill(7, 111, 99, -1, 20, "same", 10);
        OriginalBillRecord income = bill(7, 112, 9, 911, 20, "same", 20);
        assertEquals(expense.localRowId, match(20, "same", true));
        assertEquals(income.localRowId, match(20, "same", false));
    }
    @Test public void parentMatcherKeepsGlobalOwnerBookScopeAndUsesLocalIdForTimeTies() {
        OriginalBillRecord old = bill(7, 900, 99, 0, 20, "same", 10);
        OriginalBillRecord latest = bill(99, 1, 99, 0, 20, "same", 10);
        latest.bookId = 987654321; latest.reimbursement = true; latest.notIntoTotal = true;
        bills.saveOriginalRaw(latest, null);
        assertEquals(latest.localRowId, match(20, "same", true));
        assertNotEquals(old.localRowId, latest.localRowId);
    }
    @Test public void amountToleranceTestsPositiveAndNegativeRawCostWithoutFxGetters() {
        bill(7, 121, 99, 0, 10.00009, null, 10);
        OriginalBillRecord negative = bill(7, 122, 99, 0, -9.99991, null, 20);
        negative.currencyInfo = "USD 999"; negative.currencyAssetNumber = 999; bills.saveOriginalRaw(negative, null);
        bill(7, 123, 99, 0, -10.0002, null, 30);
        assertEquals(negative.localRowId, match(-10, null, true));
        assertNull(matcher.findParent(9, "", true));
    }
    @Test public void originalSanitizationAndLikeWildcardsArePreservedWithBoundParameters() {
        OriginalBillRecord literal = bill(7, 131, 99, 0, 33, "shop_100%", 10);
        OriginalBillRecord wildcard = bill(7, 132, 99, 0, 33, "shopx100anything", 20);
        assertEquals(literal.localRowId, match(33, "退款-'shop_100%?", true)); erase(literal);
        assertEquals(wildcard.localRowId, match(33, "退款-'shop_100%?", true));
        assertNull(matcher.findParent(33, "' OR 1=1 --", false));
    }
    @Test public void hostTrashExclusionIsExplicitAndLegacyRowsNeverAcquireRawIdentity() {
        OriginalBillRecord live = bill(7, 141, 99, 0, 44, "same", 10);
        OriginalBillRecord trash = bill(7, 142, 99, 0, 44, "same", 20);
        store.getWritableDatabase().execSQL("UPDATE transactions SET deleted=1 WHERE id=?", new Object[]{trash.localRowId});
        assertEquals(live.localRowId, match(44, "same", true));
        assertEquals(trash.localRowId, matcher.findParent(44, "same", true, true).localRowId);
        store.getWritableDatabase().execSQL("INSERT INTO transactions(book_id,kind,amount,currency,time,created,remark) VALUES(1,'expense',4400,'CNY',999,1,'same')");
        assertEquals(live.localRowId, match(44, "same", true));
    }
    @Test public void differenceCategoriesUseCurrentOwnerOrSenderButNoBookAndKeepZeroChild() {
        bill(8, 151, 9, 911, 1, "退款差额", 10);
        OriginalBillRecord viaSender = bill(8, 152, 9, 909, 1, "退款差额", 20);
        viaSender.fromUserId = 7; viaSender.bookId = 999; bills.saveOriginalRaw(viaSender, null);
        bill(8, 153, 9, 902, 1, "退款差额", 30); // Unrelated owner does not win.
        OriginalRefundMatcher.Categories income = matcher.latestDifferenceCategories(7, true);
        assertEquals(9, income.parent); assertEquals(909, income.child);
        OriginalBillRecord expense = bill(7, 154, 99, 0, 1, "退款差额", 40);
        OriginalRefundMatcher.Categories expensePair = matcher.latestDifferenceCategories(7, false);
        assertEquals(expense.parentCategoryId, expensePair.parent); assertEquals(0, expensePair.child);
        assertNull(matcher.latestDifferenceCategories(99, false));
    }
    @Test public void manualDefaultPageUsesBookOwnerAndSemanticBookButNoDirectionOrPositiveCostFilter() {
        OriginalBillRecord zeroExpense = bill(0, 201, 99, -1, 0, "zero", 10);
        OriginalBillRecord income = bill(0, 202, 9, 911, 20, "income", 20);
        OriginalBillRecord senderOnly = bill(7, 203, 99, -1, 30, "sender", 30);
        senderOnly.fromUserId = 0; bills.saveOriginalRaw(senderOnly, null);
        OriginalBillRecord differentBook = bill(0, 204, 99, -1, 40, "other book", 40);
        differentBook.bookId = 777; bills.saveOriginalRaw(differentBook, null);
        assertEquals(2, matcher.manualParents(7, 1, null).size());
        assertEquals(income.localRowId, matcher.manualParents(7, 1, null).get(0).localRowId);
        assertEquals(zeroExpense.localRowId, matcher.manualParents(7, 1, income).get(0).localRowId);
        assertEquals(1, matcher.manualParents(7, 0, null).size());
        assertEquals(senderOnly.localRowId, matcher.manualParents(7, 0, null).get(0).localRowId);
        assertEquals(3, matcher.manualParents(0, 0, null).size());
        OriginalBillRecord sameSemanticOtherOwner = bill(8, 202, 99, -1, 1, "another owner", 50);
        assertEquals(1, matcher.manualParents(7, 1, sameSemanticOtherOwner).size());
    }
    @Test public void manualSelectionIsExcludedAfterTheHundredRowLimitWithoutRefillingThePage() {
        OriginalBillRecord latest = null;
        for (int i = 0; i < 101; i++) latest = bill(0, 1000 + i, 99, -1, 1, "row " + i, i + 1);
        assertEquals(100, matcher.manualParents(0, 1, null).size());
        java.util.List<OriginalBillRecord> selected = matcher.manualParents(0, 1, latest);
        assertEquals(99, selected.size()); assertEquals(1099, selected.get(0).billId);
        assertEquals(1001, selected.get(selected.size() - 1).billId);
    }
}
