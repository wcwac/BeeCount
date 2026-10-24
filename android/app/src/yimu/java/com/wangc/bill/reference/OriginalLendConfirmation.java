package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONObject;

/** Host-only candidate receipt following AutoAddLayout.X's independent financial stages. */
final class OriginalLendConfirmation {
    private OriginalLendConfirmation() {}

    static long save(ReferenceStore store, JSONObject input, String candidateId, Runnable warning) {
        JSONObject candidate = candidateId == null ? null : store.candidate(candidateId);
        if (candidate == null) throw new IllegalArgumentException("采集记录不存在");
        if ("SAVED".equals(candidate.optString("state"))) {
            if (!"lend".equals(candidate.optString("save_kind")))
                throw new IllegalArgumentException("该采集记录已保存为其他类型");
            ReferenceSchemaV8.validateReceipt(store, candidate);
            return store.one("SELECT lend_row_id FROM candidate_lend_receipts WHERE candidate_id=?", candidateId)
                    .optLong("lend_row_id");
        }
        if (!"PENDING".equals(candidate.optString("state"))) throw new IllegalArgumentException("采集记录已忽略");
        if ((store.getWritableDatabase().inTransaction() && !AccountingCommitScope.active(store))) throw new IllegalStateException("还款各阶段需要独立保存");
        JSONObject header = store.book(input.optLong("book_id"));
        if (header == null || header.optInt("archived") != 0) throw new IllegalArgumentException("请选择有效账本");
        if (!"transfer".equals(input.optString("kind"))) throw new IllegalArgumentException("请选择转账还款类型");
        int owner = ReferenceStore.LOCAL_OFFLINE_OWNER;
        // U0 already chose X using the retained selected object and checkbox. A
        // metadata edit between acceptance and the worker must not turn X into Z.
        long fromSemantic = selectedIdentity(store, input, "account_id", "raw_asset_semantic_id");
        long toSemantic = selectedIdentity(store, input, "to_account_id", "raw_to_asset_semantic_id");
        if (fromSemantic == toSemantic) throw new IllegalArgumentException("请选择不同的账户");
        JSONArray files = input.optJSONArray("attachments");
        if (files != null && files.length() != 0)
            throw new IllegalArgumentException("原版还款附件关联尚待接入，请先保留附件");
        String number = input.optString("transfer_amount_text");
        if (number.isEmpty()) throw new IllegalArgumentException("请输入金额");
        double amount = OriginalTransferAmount.parseS(number);
        String fee = input.optString("transfer_fee_text");
        if (!Double.isFinite(amount) || !Double.isFinite(OriginalTransferAmount.parseS(fee)))
            throw new IllegalArgumentException("还款金额超出本地存储范围");
        long time = input.optLong("time");
        if (time <= 0 || time > 8640000000000000L) throw new IllegalArgumentException("记账时间无效");
        if (!input.isNull("import_key") && !input.optString("import_key").isEmpty())
            throw new IllegalArgumentException("采集记录不能使用导入标识");
        OriginalAssetBookResolver feeBooks = new OriginalAssetBookResolver(store, owner);
        long currentRow = store.context().getSharedPreferences("reference_settings", Context.MODE_PRIVATE)
                .getLong("current_book", 1);
        JSONObject current = store.bookRepository().resolveCurrent(owner, currentRow);
        if (current == null) throw new IllegalArgumentException("当前原版账本不存在");
        int bookOwner = current.optInt("owner_semantic_id");
        if (OriginalTransferAmount.isN(fee) && OriginalTransferAmount.parseS(fee) != 0) {
            JSONObject from = store.assetRepository().p(fromSemantic, owner);
            JSONObject feeBook = store.book(feeBooks.resolveIdentity(fromSemantic).rowId);
            if (from == null || feeBook == null || !"CNY".equals(feeBook.optString("currency"))
                    || !from.isNull("asset_currency") && !from.optString("asset_currency").isEmpty())
                throw new IllegalArgumentException("还款利息账单的外币界面映射尚待接入，请保留原始金额");
            OriginalTransferConfirmation.minor(Math.abs(OriginalTransferAmount.parseS(fee)));
        }
        OriginalFinancialEffects effects = new OriginalFinancialEffects(store, owner, bookOwner);
        OriginalAutoCreditRepaymentCommands command = new OriginalAutoCreditRepaymentCommands(store, owner,
                bookOwner, new OriginalLendActions(store, owner), feeBooks::resolve);
        int mode = store.context().getSharedPreferences("reference_settings", Context.MODE_PRIVATE)
                .getInt("original_transfer_mode", 0);
        OriginalAutoCreditRepaymentCommands.Result result = command.save(fromSemantic, toSemantic, amount, fee, mode,
                input.optString("remark"), time, bill -> OriginalTransferConfirmation.projectFee(store, bill),
                new OriginalAutoCreditRepaymentCommands.Effects() {
                    @Override public void warningNonPositiveArrival() { if (warning != null) warning.run(); }
                    @Override public void repaymentChanged(OriginalLendRecord lend, boolean saved) {
                        effects.lendChanged(lend, saved);
                    }
                }, null);
        if (!result.saved)
            throw new IllegalStateException("还款未能完整保存，可安全重试");
        OriginalLendRecord lend = result.lend;
        JSONObject confirmed = Json.copy(input);
        Json.put(confirmed,"fee_bill_id",result.interestBill==null?null:result.interestBill.localRowId);
        Json.put(confirmed, "financial_type", "original_lend");
        Json.put(confirmed, "lend_row_id", lend.localRowId);
        Json.put(confirmed, "lend_owner_semantic_id", lend.userId);
        Json.put(confirmed, "lend_semantic_id", lend.lendId);
        // Retain the selected identity even where source X stores a missing fresh
        // repayment endpoint as zero. Those are separate pieces of evidence.
        Json.put(confirmed, "raw_asset_semantic_id", fromSemantic);
        Json.put(confirmed, "raw_to_asset_semantic_id", toSemantic);
        SQLiteDatabase db = store.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues receipt = new ContentValues();
            receipt.put("candidate_id", candidateId); receipt.put("lend_row_id", lend.localRowId);
            receipt.put("resolved_lend_row_id", lend.localRowId); receipt.put("owner_semantic_id", lend.userId);
            receipt.put("lend_semantic_id", lend.lendId);
            if (db.insertOrThrow("candidate_lend_receipts", null, receipt) == -1)
                throw new IllegalStateException("还款确认记录未能保存，可安全重试");
            ContentValues state = new ContentValues();
            state.put("state", "SAVED"); state.put("save_kind", "lend"); state.putNull("transaction_id");
            state.put("book_id", header.optLong("id")); state.put("payload", confirmed.toString());
            if (db.update("candidates", state, "id=? AND state='PENDING'", new String[]{candidateId}) != 1)
                throw new IllegalStateException("还款确认状态未能保存，可安全重试");
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
        return lend.localRowId;
    }

    private static long selectedIdentity(ReferenceStore store, JSONObject input, String rowKey, String rawKey) {
        if (input.optLong(rowKey) <= 0) throw new IllegalArgumentException("请选择转出或转入账户");
        Object selected = input.opt(rawKey);
        if (selected instanceof Integer || selected instanceof Long) {
            Object owner = input.opt("owner_semantic_id");
            if (!(owner instanceof Integer || owner instanceof Long)
                    || ((Number) owner).longValue() != ReferenceStore.LOCAL_OFFLINE_OWNER)
                throw new IllegalArgumentException("请选择当前离线身份的原版还款账户");
            // The original retains the selected object, then f.P re-reads by its
            // identity. Do not require the fresh source to exist before X's gates.
            return ((Number) selected).longValue();
        }
        JSONObject row = store.assetRepository().byRowId(input.optLong(rowKey));
        if (row == null || !AssetRepository.ORIGINAL.equals(row.optString("asset_origin"))
                || row.optInt("owner_semantic_id") != ReferenceStore.LOCAL_OFFLINE_OWNER)
            throw new IllegalArgumentException("请选择当前离线身份的原版还款账户");
        return row.optLong("semantic_id");
    }
}
