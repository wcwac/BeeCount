package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.json.JSONArray;
import org.json.JSONObject;

/** Host confirmation receipt around AutoAddLayout.Z; Transfer and Bill IDs never share a domain. */
final class OriginalTransferConfirmation {
    private OriginalTransferConfirmation() {}

    static long save(ReferenceStore store, JSONObject input, String candidateId, Runnable warning) {
        JSONObject candidate = candidateId == null ? null : store.candidate(candidateId);
        if (candidate == null) throw new IllegalArgumentException("采集记录不存在");
        if ("SAVED".equals(candidate.optString("state"))) {
            if (!"transfer".equals(candidate.optString("save_kind")))
                throw new IllegalArgumentException("该采集记录已保存为其他类型");
            validateReceipt(store, candidate);
            return store.one("SELECT transfer_row_id FROM candidate_transfer_receipts WHERE candidate_id=?", candidateId)
                    .optLong("transfer_row_id");
        }
        if (!"PENDING".equals(candidate.optString("state"))) throw new IllegalArgumentException("采集记录已忽略");
        if ((store.getWritableDatabase().inTransaction() && !AccountingCommitScope.active(store))) throw new IllegalStateException("转账各阶段需要独立保存");
        int owner = ReferenceStore.LOCAL_OFFLINE_OWNER;
        JSONObject header = store.book(input.optLong("book_id"));
        if (header == null || header.optInt("archived") != 0) throw new IllegalArgumentException("请选择有效账本");
        if (!"transfer".equals(input.optString("kind"))) throw new IllegalArgumentException("请选择转账类型");
        JSONObject from = selected(store, input.optLong("account_id"));
        JSONObject to = selected(store, input.optLong("to_account_id"));
        long fromSemantic = from.optLong("semantic_id"), toSemantic = to.optLong("semantic_id");
        if (fromSemantic == toSemantic) throw new IllegalArgumentException("请选择不同的账户");
        boolean repaymentRoute = input.has("transfer_repayment_route")
                ? input.optBoolean("transfer_repayment_route")
                : to.optInt("asset_type") == 2 && input.optBoolean("transfer_repayment");
        if (repaymentRoute)
            throw new IllegalArgumentException("勾选的信贷还款走独立借贷记录流程，请使用还款保存入口");
        JSONArray files = input.optJSONArray("attachments");
        if (files != null && files.length() != 0)
            throw new IllegalArgumentException("原版转账附件关联尚待接入，请先保留附件");
        String number = input.optString("transfer_amount_text");
        if (number.isEmpty()) throw new IllegalArgumentException("请输入金额");
        double amount = OriginalTransferAmount.parseS(number);
        String fee = input.optString("transfer_fee_text");
        if (!Double.isFinite(amount) || !Double.isFinite(OriginalTransferAmount.parseS(fee)))
            throw new IllegalArgumentException("转账金额超出本地存储范围");
        long time = input.optLong("time");
        if (time <= 0 || time > 8640000000000000L) throw new IllegalArgumentException("记账时间无效");
        if (!input.isNull("import_key") && !input.optString("import_key").isEmpty())
            throw new IllegalArgumentException("采集记录不能使用导入标识");
        OriginalAssetBookResolver feeBooks = new OriginalAssetBookResolver(store, owner);
        // The overlay's header is not Asset.getAccountBookId or MyApplication.g.
        long currentRow = store.context().getSharedPreferences("reference_settings", Context.MODE_PRIVATE)
                .getLong("current_book", 1);
        JSONObject current = store.bookRepository().resolveCurrent(owner, currentRow);
        if (current == null) throw new IllegalArgumentException("当前原版账本不存在");
        int bookOwner = current.optInt("owner_semantic_id");
        if (OriginalTransferAmount.isN(fee) && OriginalTransferAmount.parseS(fee) != 0) {
            JSONObject feeBook = store.book(feeBooks.resolveIdentity(fromSemantic).rowId);
            if (!"CNY".equals(feeBook.optString("currency"))
                    || !from.isNull("asset_currency") && !from.optString("asset_currency").isEmpty())
                throw new IllegalArgumentException("手续费账单的外币界面映射尚待接入，请保留原始金额");
            minor(Math.abs(OriginalTransferAmount.parseS(fee))); // Check only our Bill display bridge before balance writes.
        }
        OriginalFinancialEffects effects = new OriginalFinancialEffects(store, owner, bookOwner);
        OriginalTransferActions actions = new OriginalTransferActions(store, owner, effects::transferChanged);
        OriginalAutoTransferCommands command = new OriginalAutoTransferCommands(store, owner, bookOwner, actions, feeBooks::resolve);
        int mode = store.context().getSharedPreferences("reference_settings", Context.MODE_PRIVATE)
                .getInt("original_transfer_mode", 0);
        OriginalAutoTransferCommands.Result result = command.save(fromSemantic, toSemantic, amount, fee, mode,
                input.optString("remark"), time, bill -> projectFee(store, bill),
                () -> { if (warning != null) warning.run(); }, null);
        if (!result.saved) throw new IllegalStateException("转账未能完整保存，可安全重试");
        OriginalTransferRecord transfer = result.transfer;
        JSONObject confirmed = Json.copy(input);
        Json.put(confirmed, "financial_type", "original_transfer");
        Json.put(confirmed, "transfer_row_id", transfer.localRowId);
        Json.put(confirmed, "transfer_owner_semantic_id", transfer.userId);
        Json.put(confirmed, "transfer_semantic_id", transfer.transferId);
        Json.put(confirmed, "raw_asset_semantic_id", transfer.fromAssetId);
        Json.put(confirmed, "raw_to_asset_semantic_id", transfer.toAssetId);
        // The functional Store entry already owns the outer transaction. This nested
        // receipt stage and all preceding financial writes commit or roll back together.
        SQLiteDatabase db = store.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues receipt = new ContentValues();
            receipt.put("candidate_id", candidateId); receipt.put("transfer_row_id", transfer.localRowId);
            receipt.put("resolved_transfer_row_id", transfer.localRowId); receipt.put("owner_semantic_id", transfer.userId);
            receipt.put("transfer_semantic_id", transfer.transferId);
            if (db.insertOrThrow("candidate_transfer_receipts", null, receipt) == -1)
                throw new IllegalStateException("转账确认记录未能保存，可安全重试");
            ContentValues state = new ContentValues();
            state.put("state", "SAVED"); state.put("save_kind", "transfer"); state.putNull("transaction_id");
            state.put("book_id", header.optLong("id")); state.put("payload", confirmed.toString());
            if (db.update("candidates", state, "id=? AND state='PENDING'", new String[]{candidateId}) != 1)
                throw new IllegalStateException("转账确认状态未能保存，可安全重试");
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
        return transfer.localRowId;
    }

    private static JSONObject selected(ReferenceStore store, long rowId) {
        JSONObject row = store.assetRepository().byRowId(rowId);
        if (row == null || !AssetRepository.ORIGINAL.equals(row.optString("asset_origin"))
                || row.optInt("owner_semantic_id") != ReferenceStore.LOCAL_OFFLINE_OWNER)
            throw new IllegalArgumentException("请选择当前离线身份的原版转账账户");
        // f.P re-reads by original identity. It has no hide/book/type predicate.
        return store.assetRepository().p(row.optLong("semantic_id"), ReferenceStore.LOCAL_OFFLINE_OWNER);
    }

    static ContentValues projectFee(ReferenceStore store, OriginalBillRecord bill) {
        JSONObject book = store.one("SELECT * FROM books WHERE owner_semantic_id=? AND semantic_id=? ORDER BY id LIMIT 1",
                "" + bill.userId, "" + bill.bookId);
        if (book == null || !"CNY".equals(book.optString("currency")))
            throw new IllegalArgumentException("原版手续费账本映射无效");
        String kind = bill.parentCategoryId == 9 ? "income" : "expense";
        JSONObject category = store.categoryRepository().rowForSelection(kind, bill.parentCategoryId, bill.childCategoryId);
        JSONObject asset = store.assetRepository().p(bill.assetId, bill.userId);
        ContentValues values = new ContentValues();
        values.put("book_id", book.optLong("id")); values.put("kind", kind); values.put("currency", book.optString("currency"));
        values.put("amount", minor(Math.abs(bill.cost))); values.put("discount", 0); values.put("fee", 0);
        values.put("parent_semantic_id", bill.parentCategoryId); values.put("child_semantic_id", bill.childCategoryId);
        if (category == null) values.putNull("category_id"); else values.put("category_id", category.optLong("id"));
        if (asset == null) values.putNull("account_id"); else values.put("account_id", asset.optLong("id"));
        values.putNull("to_account_id"); values.put("raw_asset_semantic_id", bill.assetId); values.putNull("raw_to_asset_semantic_id");
        values.put("owner_semantic_id", bill.userId); values.put("time", bill.time); values.put("created", bill.recordTime);
        if (bill.remark != null) values.put("remark", bill.remark);
        values.put("not_total", bill.notIntoTotal ? 1 : 0); values.put("not_budget", bill.notIntoBudget ? 1 : 0);
        return values;
    }

    static long minor(double amount) {
        long value = BigDecimal.valueOf(amount).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        if (value < 0 || value > 99999999999999L) throw new IllegalArgumentException("手续费超出本地账单范围");
        return value;
    }

    static void validateReceipt(ReferenceStore store, JSONObject candidate) {
        JSONObject receipt = store.one("SELECT * FROM candidate_transfer_receipts WHERE candidate_id=?", candidate.optString("id"));
        JSONObject payload = Json.parse(candidate.optString("payload"));
        if (!"SAVED".equals(candidate.optString("state")) || !"transfer".equals(candidate.optString("save_kind"))
                || !candidate.isNull("transaction_id") || receipt == null
                || !candidate.isNull("refund_owner_semantic_id") || !candidate.isNull("refund_semantic_id") || !candidate.isNull("refund_item_raw")
                || !"transfer".equals(payload.optString("kind")) || !"original_transfer".equals(payload.optString("financial_type"))
                || store.one("SELECT id FROM transactions WHERE candidate_id=? LIMIT 1", candidate.optString("id")) != null
                || !sameInteger(payload, "transfer_row_id", receipt.optLong("transfer_row_id"))
                || !sameInteger(payload, "transfer_owner_semantic_id", receipt.optInt("owner_semantic_id"))
                || !sameInteger(payload, "transfer_semantic_id", receipt.optLong("transfer_semantic_id")))
            throw new IllegalArgumentException("转账采集确认关联无效");
    }

    private static boolean sameInteger(JSONObject payload, String key, long expected) {
        Object value = payload.opt(key);
        return (value instanceof Integer || value instanceof Long) && ((Number) value).longValue() == expected;
    }
}
