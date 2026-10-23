package com.wangc.bill.reference;

import android.content.ContentValues;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.json.JSONObject;

/** The experimental candidate receipt around the real AutoAddLayout.V action. */
final class OriginalRefundConfirmation {
    private OriginalRefundConfirmation() {}

    static long save(ReferenceStore store, JSONObject input, String candidateId,
            OriginalBillRecord selectedParent, Long receiverSemanticId) {
        // ReferenceStore's monitor spans preflight, the independently committed source
        // stages, and this host receipt. Never wrap the financial action in a transaction.
        if (candidateId == null || candidateId.isEmpty()) throw new IllegalArgumentException("采集记录不存在");
        JSONObject candidate = store.candidate(candidateId);
        if (candidate == null) throw new IllegalArgumentException("采集记录不存在");
        if ("SAVED".equals(candidate.optString("state"))) return candidate.optLong("transaction_id");
        if (!"PENDING".equals(candidate.optString("state"))) throw new IllegalArgumentException("采集记录已忽略");
        return savePrepared(store,input,candidateId,selectedParent,receiverSemanticId,
                preflight(store,input,selectedParent,receiverSemanticId));
    }

    static final class Prepared {
        final JSONObject parentProjection,headerBook;
        final int bookOwner;
        Prepared(JSONObject parent,JSONObject header,int owner){parentProjection=parent;headerBook=header;bookOwner=owner;}
    }

    /** Host-only scope/input guards precede a destructive repeat replacement. */
    static Prepared preflight(ReferenceStore store,JSONObject input,OriginalBillRecord selectedParent,Long receiverSemanticId) {
        if (store.getWritableDatabase().inTransaction()) throw new IllegalStateException("退款阶段需要独立保存");
        if (selectedParent == null) throw new IllegalArgumentException("请选择退款账单");
        JSONObject projectedParent = store.transaction(selectedParent.localRowId);
        OriginalBillRecord persisted = new OriginalBillRepository(store).byLocalRowId(selectedParent.localRowId);
        if (persisted == null || persisted.billId != selectedParent.billId || projectedParent.optInt("deleted") != 0)
            throw new IllegalArgumentException("退款账单已删除或不存在");
        JSONObject headerBook = store.book(input.optLong("book_id"));
        if (headerBook == null || headerBook.optInt("archived") != 0) throw new IllegalArgumentException("请选择有效账本");
        if (!"CNY".equals(headerBook.optString("currency")) || !"CNY".equals(projectedParent.optString("currency"))
                || selectedParent.currencyInfo != null && !selectedParent.currencyInfo.isEmpty())
            throw new IllegalArgumentException("外币退款的界面换算尚待接入，请保留原始数据");
        long minor = input.optLong("amount"), time = input.optLong("time"), inTime = input.optLong("in_time");
        if (minor == 0 || minor < -99999999999999L || minor > 99999999999999L)
            throw new IllegalArgumentException("请输入非零退款金额");
        if (time <= 0 || time > 8640000000000000L || inTime < 0 || inTime > 8640000000000000L)
            throw new IllegalArgumentException("退款时间无效");
        if (receiverSemanticId != null) {
            JSONObject asset = store.assetRepository().p(receiverSemanticId, ReferenceStore.LOCAL_OFFLINE_OWNER);
            if (asset == null || asset.optInt("archived") != 0 || !AssetRepository.ORIGINAL.equals(asset.optString("asset_origin")))
                throw new IllegalArgumentException("请选择当前离线身份的原版退款账户");
            if (!asset.isNull("asset_currency") && !asset.optString("asset_currency").isEmpty())
                throw new IllegalArgumentException("显式币种账户的退款换算尚待接入");
        }
        // Read-only bridge preflight also checks the retained snapshot's original
        // book, rather than silently replacing it with a newer database snapshot.
        ContentValues retainedProjection=project(store,selectedParent,false,projectedParent.optLong("book_id"));
        return new Prepared(projectedParent,headerBook,store.book(retainedProjection.getAsLong("book_id")).optInt("owner_semantic_id"));
    }

    /** A source-legal retained Bill can have been physically deleted by the repeat popup. */
    static long savePrepared(ReferenceStore store,JSONObject input,String candidateId,OriginalBillRecord selectedParent,
            Long receiverSemanticId,Prepared prepared) {
        JSONObject projectedParent=prepared.parentProjection,headerBook=prepared.headerBook;
        int owner=ReferenceStore.LOCAL_OFFLINE_OWNER,bookOwner=prepared.bookOwner;
        long minor=input.optLong("amount"),time=input.optLong("time"),inTime=input.optLong("in_time");
        OriginalFinancialEffects effects = new OriginalFinancialEffects(store, owner, bookOwner);
        OriginalInstalmentCommands instalments = new OriginalInstalmentCommands(store, owner, bookOwner, effects::creditRefresh);
        OriginalAutoRefundCommands command = new OriginalAutoRefundCommands(store, owner, bookOwner,
                new OriginalAutoRefundCommands.Effects() {
                    @Override public void parentUpdated(OriginalBillRecord bill, boolean saved) { effects.parentUpdated(bill, saved); }
                    @Override public void adjustInstalment(OriginalBillRecord bill) { instalments.adjustAfterRefund(bill); }
                    @Override public void refreshCredit(OriginalBillRecord bill) { effects.refreshCredit(bill); }
                    @Override public void refundAggregateChanged(int parentId, double total, boolean saved) { effects.refundAggregateChanged(parentId, total, saved); }
                    @Override public void refreshGroup(OriginalBillRecord bill) { effects.refreshGroup(bill); }
                });
        OriginalAutoRefundCommands.Result result = command.save(selectedParent, receiverSemanticId,
                BigDecimal.valueOf(minor, 2).doubleValue(), input.optString("remark"), time, inTime,
                (bill, difference) -> project(store, bill, difference,projectedParent.optLong("book_id")));
        if (!result.saved) throw new IllegalStateException("退款部分保存失败，余额、历史或原账单可能已改变；请先核对数据，勿重复提交");
        OriginalRefundRepository.Snapshot refund = result.refundSave.record;
        String itemRaw = refund.data.rawInfos.get(0);
        JSONObject confirmed = Json.copy(input);
        Json.put(confirmed, "financial_type", "original_refund");
        Json.put(confirmed, "refund_parent_row_id", selectedParent.localRowId);
        Json.put(confirmed, "refund_parent_semantic_id", selectedParent.billId);
        Json.put(confirmed, "raw_asset_semantic_id", receiverSemanticId);
        // Many confirmations can point to the same parent. Do not steal its unique
        // candidate_id or change its original book because the header book differs.
        ContentValues receipt = new ContentValues();
        receipt.put("state", "SAVED"); receipt.put("save_kind", "refund");
        receipt.put("transaction_id", selectedParent.localRowId); receipt.put("book_id", headerBook.optLong("id"));
        receipt.put("payload", confirmed.toString()); receipt.put("refund_owner_semantic_id", refund.ownerSemanticId);
        receipt.put("refund_semantic_id", refund.refundSemanticId); receipt.put("refund_item_raw", itemRaw);
        if (store.getWritableDatabase().update("candidates", receipt, "id=? AND state='PENDING'", new String[]{candidateId}) != 1)
            throw new IllegalStateException("退款已完成但确认状态保存失败；请核对退款历史，勿重复提交");
        return selectedParent.localRowId;
    }

    private static ContentValues project(ReferenceStore store, OriginalBillRecord bill, boolean difference,long parentBookRowId) {
        ContentValues values = new ContentValues();
        values.put("amount", minor(Math.abs(bill.cost))); // Source E3 admits a fully refunded zero-cost Bill.
        // V copies the parent's book. Resolve its already-known host row, before
        // a0.g's owner fallback; do not infer a book from the yet-unassigned userId.
        JSONObject book = store.book(parentBookRowId);
        if (book == null || book.isNull("semantic_id") || book.optLong("semantic_id")!=bill.bookId)
            book=store.one("SELECT * FROM books WHERE semantic_id=? AND owner_semantic_id=? ORDER BY id LIMIT 1",""+bill.bookId,""+bill.userId);
        if(book==null||!"CNY".equals(book.optString("currency")))throw new IllegalArgumentException("原退款账单的账本或外币映射尚待接入");
        String kind = bill.parentCategoryId == 9 ? "income" : "expense";
        JSONObject category = bill.childCategoryId>0?store.categoryRepository().child(bill.parentCategoryId,bill.childCategoryId)
                :store.categoryRepository().parent(bill.parentCategoryId);
        values.put("book_id", book.optLong("id")); values.put("kind", kind); values.put("currency", book.optString("currency"));
        if (category == null) values.putNull("category_id"); else values.put("category_id", category.optLong("id"));
        values.put("parent_semantic_id", bill.parentCategoryId); values.put("child_semantic_id", bill.childCategoryId);
        values.put("owner_semantic_id", ReferenceStore.LOCAL_OFFLINE_OWNER);
        values.put("raw_asset_semantic_id", bill.assetId); values.putNull("raw_to_asset_semantic_id");
        // Raw zero remains distinct from no asset. The difference Bill does not
        // acquire the selected refund receiver's FK; its asset ID remains zero.
        JSONObject asset=difference?null:store.assetRepository().p(bill.assetId,ReferenceStore.LOCAL_OFFLINE_OWNER);
        if(asset==null)values.putNull("account_id");else values.put("account_id",asset.optLong("id"));
        values.putNull("to_account_id");
        // Source ordinary-save omits null strings, including on a retained model.
        if(bill.remark!=null)values.put("remark",bill.remark);
        values.put("time", bill.time);
        if(difference)values.put("created", bill.recordTime);
        values.put("discount", minor(Math.abs(bill.discountNumber)));
        values.put("not_total", bill.notIntoTotal ? 1 : 0); values.put("not_budget", bill.notIntoBudget ? 1 : 0);
        return values;
    }

    private static long minor(double value) {
        return BigDecimal.valueOf(value).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    /** Validate our receipt, not an invented original one-to-one Bill/candidate link. */
    static void validateReceipt(ReferenceStore store, JSONObject candidate) {
        boolean saved = "SAVED".equals(candidate.optString("state"));
        String kind = candidate.optString("save_kind");
        if ("bill".equals(kind)) {
            if (!candidate.isNull("refund_owner_semantic_id") || !candidate.isNull("refund_semantic_id") || !candidate.isNull("refund_item_raw"))
                throw new IllegalArgumentException("普通采集记录不应带退款确认信息");
            if (saved) {
                JSONObject transaction = store.one("SELECT candidate_id FROM transactions WHERE id=?", "" + candidate.optLong("transaction_id"));
                if (transaction == null || !candidate.optString("id").equals(transaction.optString("candidate_id")))
                    throw new IllegalArgumentException("备份采集关联无效");
            }
            return;
        }
        if (!"refund".equals(kind) || !saved || candidate.isNull("refund_owner_semantic_id")
                || candidate.isNull("refund_semantic_id") || candidate.isNull("refund_item_raw"))
            throw new IllegalArgumentException("退款采集确认信息不完整");
        OriginalRefundRepository.Snapshot refund = new OriginalRefundRepository(store).byIdentity(
                candidate.optInt("refund_owner_semantic_id"), candidate.optLong("refund_semantic_id"));
        OriginalBillRecord bill = new OriginalBillRepository(store).byLocalRowId(candidate.optLong("transaction_id"));
        JSONObject payload=Json.parse(candidate.optString("payload"));
        long parentRow=candidate.optLong("transaction_id");
        if (refund == null || parentRow<=0 || payload.optLong("refund_parent_row_id")!=parentRow
                || payload.optInt("refund_parent_semantic_id")!=refund.data.parentBillSemanticId
                || bill!=null&&refund.data.parentBillSemanticId!=bill.billId
                || refund.data.parentTransactionRowId!=null&&refund.data.parentTransactionRowId.longValue()!=parentRow
                || bill==null&&refund.data.parentTransactionRowId!=null
                || refund.data.rawInfos == null || !refund.data.rawInfos.contains(candidate.optString("refund_item_raw")))
            throw new IllegalArgumentException("退款采集关联无效");
    }
}
