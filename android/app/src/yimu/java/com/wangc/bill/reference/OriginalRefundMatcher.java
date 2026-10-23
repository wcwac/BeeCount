package com.wangc.bill.reference;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/** a0.p3/J2 queries on canonical raw Bills; local row IDs never become semantic IDs. */
public final class OriginalRefundMatcher {
    private final ReferenceStore store;
    private final OriginalBillRepository bills;
    public OriginalRefundMatcher(ReferenceStore store) {
        if (store == null) throw new IllegalArgumentException("退款账单存储不能为空");
        this.store = store; bills = new OriginalBillRepository(store);
    }

    /**
     * Original p3 has no owner/book/time-window filter. Reversible host trash is an
     * extension absent from that physical-delete path, so the UI excludes it explicitly.
     */
    public OriginalBillRecord findParent(double capturedNumber, String capturedRemark, boolean capturedIncome) {
        return findParent(capturedNumber, capturedRemark, capturedIncome, false);
    }

    public OriginalBillRecord findParent(double capturedNumber, String capturedRemark,
            boolean capturedIncome, boolean includeHostTrash) {
        if (!Double.isFinite(capturedNumber)) throw new IllegalArgumentException("退款匹配金额无效");
        synchronized (store) {
            String remark = capturedRemark;
            if (remark != null && !remark.isEmpty()) remark = remark.replace("'", "").replace("?", "").replace("退款-", "");
            // X0 passes !capturedIncome to p3: an incoming refund looks for an expense.
            String direction = capturedIncome ? "bill_parent_category_id<>9" : "bill_parent_category_id=9";
            String alive = includeHostTrash ? "" : " AND deleted=0";
            String base = "bill_origin='original'" + alive + " AND " + direction;
            double absolute = Math.abs(capturedNumber), low = absolute - 1.0E-4d, high = absolute + 1.0E-4d;
            String amount = "((bill_cost>=? AND bill_cost<=?) OR(bill_cost<=? AND bill_cost>=?))";
            String[] range = {"" + low, "" + high, "" + -low, "" + -high};
            OriginalBillRecord found;
            if (remark != null && !remark.isEmpty()) {
                found = first(base + " AND " + amount + " AND bill_remark=?", with(range, remark));
                if (found != null) return found;
                found = first(base + " AND " + amount + " AND bill_remark LIKE ?", with(range, "%" + remark + "%"));
                if (found != null) return found;
                found = first(base + " AND bill_remark=?", remark);
                if (found != null) return found;
                found = first(base + " AND bill_remark LIKE ?", "%" + remark + "%");
                if (found != null) return found;
            }
            return first(base + " AND " + amount, range);
        }
    }

    /** Raw semantic pair selected by a0.J2, before the explicit 9/911 or 99/0 fallback. */
    public static final class Categories {
        public final int parent, child;
        private Categories(int parent, int child) { this.parent = parent; this.child = child; }
    }
    public Categories latestDifferenceCategories(int currentOwner, boolean income) {
        synchronized (store) {
            JSONObject row = store.one("SELECT bill_parent_category_id,bill_child_category_id FROM transactions WHERE bill_origin='original' AND deleted=0 AND(bill_user_id=? OR bill_from_user_id=?) AND bill_remark=? AND bill_parent_category_id"
                    + (income ? "=9" : "<>9") + " ORDER BY bill_time DESC,id DESC LIMIT 1", "" + currentOwner, "" + currentOwner, "退款差额");
            return row == null ? null : new Categories(row.optInt("bill_parent_category_id"), row.optInt("bill_child_category_id"));
        }
    }
    /**
     * BillChoiceDialog.P's default, empty-search page. A zero raw book semantic ID
     * means all books for current user; positive local book IDs are separate values.
     * Asset/date/advanced D3 search controls are not silently approximated here.
     */
    public List<OriginalBillRecord> manualParents(int currentOwner, long currentBookRowId,
            OriginalBillRecord selectedParent) {
        synchronized (store) {
            JSONObject book = currentBookRowId == 0 ? null : store.book(currentBookRowId);
            if (currentBookRowId != 0 && (book == null || book.isNull("semantic_id") || book.isNull("owner_semantic_id")))
                throw new IllegalArgumentException("账本的原始身份尚未恢复");
            String where = "bill_origin='original' AND deleted=0 AND bill_user_id=?";
            String[] arguments;
            if (book != null && book.optLong("semantic_id") != 0) {
                where += " AND bill_book_semantic_id=?";
                arguments = new String[]{"" + book.optInt("owner_semantic_id"), "" + book.optLong("semantic_id")};
            } else arguments = new String[]{"" + currentOwner};
            List<OriginalBillRecord> result = new ArrayList<>();
            for (JSONObject row : store.query("SELECT id FROM transactions WHERE " + where + " ORDER BY bill_time DESC,id DESC LIMIT 100", arguments)) {
                OriginalBillRecord candidate = bills.byLocalRowId(row.optLong("id"));
                // Dialog.y0 calls removeAll(M) after LIMIT; Bill.equals compares semantic
                // ID only. Do not replace this with local-PK filtering before LIMIT.
                if (candidate != null && !candidate.equals(selectedParent)) result.add(candidate);
            }
            return result;
        }
    }
    private OriginalBillRecord first(String where, String... arguments) {
        JSONObject row = store.one("SELECT id FROM transactions WHERE " + where + " ORDER BY bill_time DESC,id DESC LIMIT 1", arguments);
        return row == null ? null : bills.byLocalRowId(row.optLong("id"));
    }
    private static String[] with(String[] range, String remark) {
        List<String> result = new ArrayList<>();
        for (String value : range) result.add(value); result.add(remark);
        return result.toArray(new String[0]);
    }
}
