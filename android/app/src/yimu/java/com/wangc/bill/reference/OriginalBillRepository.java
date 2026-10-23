package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Raw original Bill persistence, deliberately separate from f.t1 and the UI bridge.
 * This experiment stores the generic tags list as JSON, not an original LitePal table.
 */
public final class OriginalBillRepository {
    public static final String ORIGINAL = "original", LEGACY = "legacy";
    private final ReferenceStore store;
    private final Random random;

    /** Nullable columns leave every pre-v4 identity/value explicitly unrecovered. */
    public static final String[] CANONICAL_COLUMNS = {
        "bill_asset_semantic_id", "bill_semantic_id", "bill_share_info", "bill_type",
        "bill_book_semantic_id", "bill_child_category_id", "bill_cost",
        "bill_currency_asset_number", "bill_currency_discount_number", "bill_currency_info",
        "bill_discount_number", "bill_from_user_id", "bill_in_asset_time",
        "bill_not_into_budget", "bill_not_into_total", "bill_parent_category_id",
        "bill_poi_address", "bill_record_time", "bill_reimbursement", "bill_reimbursement_end",
        "bill_remark", "bill_tags", "bill_time", "bill_total_address", "bill_update_time",
        "bill_user_id"
    };
    private static final Set<String> TEXT_COLUMNS = set("bill_share_info", "bill_currency_info",
            "bill_poi_address", "bill_remark", "bill_tags", "bill_total_address");
    private static final Set<String> REAL_COLUMNS = set("bill_cost", "bill_currency_asset_number",
            "bill_currency_discount_number", "bill_discount_number");
    private static final Set<String> PROJECTION_COLUMNS = set("book_id", "kind", "amount", "currency",
            "category_id", "account_id", "to_account_id", "remark", "merchant", "origin", "asset_text",
            "discount", "fee", "time", "created", "not_total", "not_budget", "deleted", "financial_type",
            "settled", "attachments", "candidate_id", "related_id", "import_key", "parent_semantic_id",
            "child_semantic_id", "owner_semantic_id", "raw_asset_semantic_id", "raw_to_asset_semantic_id");

    public OriginalBillRepository(ReferenceStore store) { this(store, new Random()); }

    OriginalBillRepository(ReferenceStore store, Random random) {
        if (store == null || random == null) throw new IllegalArgumentException("Bill storage is required");
        this.store = store;
        this.random = random;
    }

    /** Root's transactions rebuild creates bill_origin; this method never adds it twice. */
    public static void installV4Schema(SQLiteDatabase db) {
        for (String column : CANONICAL_COLUMNS) {
            String type = TEXT_COLUMNS.contains(column) ? "TEXT" : REAL_COLUMNS.contains(column) ? "REAL" : "INTEGER";
            db.execSQL("ALTER TABLE transactions ADD COLUMN " + column + " " + type);
        }
        // Original @Column(index=true) is not a uniqueness constraint.
        db.execSQL("CREATE INDEX transactions_bill_identity ON transactions(bill_origin,bill_user_id,bill_semantic_id)");
        db.execSQL("CREATE INDEX transactions_bill_sender_identity ON transactions(bill_origin,bill_from_user_id,bill_semantic_id)");
        db.execSQL("CREATE INDEX transactions_bill_book_time ON transactions(bill_origin,bill_user_id,bill_book_semantic_id,bill_time)");
    }

    public OriginalBillRecord byLocalRowId(long rowId) {
        synchronized (store) {
            JSONObject row = store.one("SELECT * FROM transactions WHERE id=? AND bill_origin=?", "" + rowId, ORIGINAL);
            return row == null ? null : read(row);
        }
    }

    /** a0.Z: exactly Bill.userId, independently of an asset bridge owner. */
    public OriginalBillRecord byExactUser(int userId, long billId) {
        synchronized (store) {
            JSONObject row = store.one("SELECT * FROM transactions WHERE bill_origin=? AND bill_user_id=? AND bill_semantic_id=? LIMIT 1",
                    ORIGINAL, "" + userId, "" + billId);
            return row == null ? null : read(row);
        }
    }

    /** a0.a0: current user plus current book's owner, each matching user OR fromUser. */
    public OriginalBillRecord findAccessible(int currentUserId, int currentBookOwnerId, long billId) {
        synchronized (store) {
            JSONObject row = store.one("SELECT * FROM transactions WHERE bill_origin=? AND bill_semantic_id=? AND (bill_user_id=? OR bill_from_user_id=? OR bill_user_id=? OR bill_from_user_id=?) LIMIT 1",
                    ORIGINAL, "" + billId, "" + currentUserId, "" + currentUserId, "" + currentBookOwnerId, "" + currentBookOwnerId);
            return row == null ? null : read(row);
        }
    }

    /** a0.A uses nextInt(MAX), including zero, and checks current user OR sender. */
    public int allocateSemanticId(int currentUserId) {
        synchronized (store) {
            int id;
            do { id = random.nextInt(Integer.MAX_VALUE); }
            while (store.one("SELECT id FROM transactions WHERE bill_origin=? AND bill_semantic_id=? AND (bill_user_id=? OR bill_from_user_id=?) LIMIT 1",
                    ORIGINAL, "" + id, "" + currentUserId, "" + currentUserId) != null);
            return id;
        }
    }

    public static final class SaveResult {
        public final boolean saved;
        public final OriginalBillRecord record;
        /** Class name only: raw SQL, remarks and financial data are not reported here. */
        public final String failureType;
        private SaveResult(boolean saved, OriginalBillRecord record, String failureType) {
            this.saved = saved; this.record = record; this.failureType = failureType;
        }
    }

    /** Support.save catches Exception, not Error. It does not roll back earlier f.t1 effects. */
    public SaveResult trySaveOriginalRaw(OriginalBillRecord record, ContentValues bridgeProjection) {
        try {
            saveOriginalRaw(record, bridgeProjection);
            return new SaveResult(true, record, "");
        } catch (Exception error) {
            return new SaveResult(false, record, error.getClass().getName());
        }
    }

    /**
     * Primitive zero/false values are written; ordinary null reference values are omitted.
     * Tags null means do not rewrite the generic collection; an empty list clears it.
     * No semantic-ID allocation, owner inference, timestamp, FX, category learning or f.t1.
     * The caller explicitly supplies any experimental FK/minor-unit projection.
     */
    public long saveOriginalRaw(OriginalBillRecord record, ContentValues bridgeProjection) {
        if (record == null) throw new IllegalArgumentException("Bill is required");
        synchronized (store) {
            SQLiteDatabase db = store.getWritableDatabase();
            db.beginTransaction();
            try {
                if (record.localRowId > 0) {
                    JSONObject existing = store.one("SELECT bill_origin FROM transactions WHERE id=?", "" + record.localRowId);
                    if (existing != null && !ORIGINAL.equals(existing.optString("bill_origin")))
                        throw new IllegalArgumentException("A legacy row has no recovered Bill identity");
                }
                ContentValues values = projection(bridgeProjection);
                values.put("bill_origin", ORIGINAL);
                writeScalarFields(record, values);
                if (record.localRowId > 0) {
                    // SaveHandler.updating ignores the affected-row count, even when zero.
                    db.update("transactions", values, "id=?", new String[]{"" + record.localRowId});
                } else {
                    long inserted = db.insertOrThrow("transactions", null, values);
                    // A trigger's RAISE(IGNORE) can return -1 without throwing.
                    // Original afterSave checks it before assigning baseObjId.
                    if (inserted == -1L) throw new IllegalStateException("Bill was not inserted");
                    record.localRowId = inserted;
                    // Original assigns baseObjId before generic-list persistence. A later
                    // failure rolls back SQL while this in-memory ID remains assigned.
                }
                if (record.tags != null) {
                    ContentValues tags = new ContentValues();
                    tags.put("bill_tags", encodeTags(record.tags));
                    db.update("transactions", tags, "id=?", new String[]{"" + record.localRowId});
                }
                db.setTransactionSuccessful();
                return record.localRowId;
            } finally { db.endTransaction(); }
        }
    }

    private static ContentValues projection(ContentValues supplied) {
        ContentValues result = supplied == null ? new ContentValues() : new ContentValues(supplied);
        for (String column : result.keySet()) if (!PROJECTION_COLUMNS.contains(column))
            throw new IllegalArgumentException("Unsupported Bill projection column: " + column);
        return result;
    }

    private static void writeScalarFields(OriginalBillRecord r, ContentValues v) {
        v.put("bill_asset_semantic_id", r.assetId); v.put("bill_semantic_id", r.billId);
        putReference(v, "bill_share_info", r.billShareInfo); v.put("bill_type", r.billType);
        v.put("bill_book_semantic_id", r.bookId); v.put("bill_child_category_id", r.childCategoryId);
        putReal(v, "bill_cost", r.cost); putReal(v, "bill_currency_asset_number", r.currencyAssetNumber);
        putReal(v, "bill_currency_discount_number", r.currencyDiscountNumber);
        putReference(v, "bill_currency_info", r.currencyInfo); putReal(v, "bill_discount_number", r.discountNumber);
        v.put("bill_from_user_id", r.fromUserId); v.put("bill_in_asset_time", r.inAssetTime);
        v.put("bill_not_into_budget", r.notIntoBudget ? 1 : 0); v.put("bill_not_into_total", r.notIntoTotal ? 1 : 0);
        v.put("bill_parent_category_id", r.parentCategoryId); putReference(v, "bill_poi_address", r.poiAddress);
        v.put("bill_record_time", r.recordTime); v.put("bill_reimbursement", r.reimbursement ? 1 : 0);
        v.put("bill_reimbursement_end", r.reimbursementEnd ? 1 : 0); putReference(v, "bill_remark", r.remark);
        v.put("bill_time", r.time); putReference(v, "bill_total_address", r.totalAddress);
        v.put("bill_update_time", r.updateTime); v.put("bill_user_id", r.userId);
    }

    private static void putReference(ContentValues v, String key, String value) { if (value != null) v.put(key, value); }

    private static void putReal(ContentValues v, String key, double value) {
        // Explicit experimental storage policy, not an original primitive setter rule.
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Nonfinite Bill data cannot be stored safely");
        v.put(key, value);
    }

    private static String encodeTags(List<Long> tags) {
        JSONArray result = new JSONArray();
        for (Long tag : tags) result.put(tag == null ? JSONObject.NULL : tag);
        return result.toString();
    }

    private static List<Long> decodeTags(JSONObject row) {
        if (row.isNull("bill_tags")) return null;
        Object value = row.opt("bill_tags");
        if (!(value instanceof String)) throw new IllegalArgumentException("Bill tags must be JSON text");
        JSONArray data = Json.array((String) value);
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            Object tag = data.opt(i);
            if (tag == JSONObject.NULL) result.add(null);
            else if (tag instanceof Integer || tag instanceof Long) result.add(((Number) tag).longValue());
            else throw new IllegalArgumentException("Bill tag IDs must be integers or null");
        }
        return result;
    }

    private static OriginalBillRecord read(JSONObject row) {
        OriginalBillRecord r = new OriginalBillRecord();
        r.localRowId = integer(row, "id", 1, Long.MAX_VALUE);
        r.assetId = integer(row, "bill_asset_semantic_id", Long.MIN_VALUE, Long.MAX_VALUE);
        r.billId = intValue(row, "bill_semantic_id"); r.billShareInfo = text(row, "bill_share_info");
        r.billType = intValue(row, "bill_type"); r.bookId = integer(row, "bill_book_semantic_id", Long.MIN_VALUE, Long.MAX_VALUE);
        r.childCategoryId = intValue(row, "bill_child_category_id"); r.cost = real(row, "bill_cost");
        r.currencyAssetNumber = real(row, "bill_currency_asset_number"); r.currencyDiscountNumber = real(row, "bill_currency_discount_number");
        r.currencyInfo = text(row, "bill_currency_info"); r.discountNumber = real(row, "bill_discount_number");
        r.fromUserId = intValue(row, "bill_from_user_id"); r.inAssetTime = integer(row, "bill_in_asset_time", Long.MIN_VALUE, Long.MAX_VALUE);
        r.notIntoBudget = flag(row, "bill_not_into_budget"); r.notIntoTotal = flag(row, "bill_not_into_total");
        r.parentCategoryId = intValue(row, "bill_parent_category_id"); r.poiAddress = text(row, "bill_poi_address");
        r.recordTime = integer(row, "bill_record_time", Long.MIN_VALUE, Long.MAX_VALUE);
        r.reimbursement = flag(row, "bill_reimbursement"); r.reimbursementEnd = flag(row, "bill_reimbursement_end");
        r.remark = text(row, "bill_remark"); r.tags = decodeTags(row);
        r.time = integer(row, "bill_time", Long.MIN_VALUE, Long.MAX_VALUE); r.totalAddress = text(row, "bill_total_address");
        r.updateTime = integer(row, "bill_update_time", Long.MIN_VALUE, Long.MAX_VALUE); r.userId = intValue(row, "bill_user_id");
        return r;
    }

    public void validateRestored() {
        synchronized (store) {
            for (JSONObject row : store.query("SELECT * FROM transactions")) {
                String origin = row.optString("bill_origin");
                if (ORIGINAL.equals(origin)) read(row);
                else if (LEGACY.equals(origin)) {
                    for (String column : CANONICAL_COLUMNS) if (!row.isNull(column))
                        throw new IllegalArgumentException("Legacy Bill data must not acquire inferred original fields");
                } else throw new IllegalArgumentException("Unknown Bill origin");
            }
        }
    }

    private static int intValue(JSONObject row, String key) { return (int) integer(row, key, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    private static boolean flag(JSONObject row, String key) { return integer(row, key, 0, 1) == 1; }
    private static long integer(JSONObject row, String key, long min, long max) {
        Object value = row.opt(key);
        if (!(value instanceof Integer || value instanceof Long)) throw new IllegalArgumentException("Invalid Bill integer: " + key);
        long number = ((Number) value).longValue();
        if (number < min || number > max) throw new IllegalArgumentException("Bill integer out of range: " + key);
        return number;
    }
    private static double real(JSONObject row, String key) {
        Object value = row.opt(key);
        if (!(value instanceof Number) || !Double.isFinite(((Number) value).doubleValue()))
            throw new IllegalArgumentException("Invalid Bill number: " + key);
        return ((Number) value).doubleValue();
    }
    private static String text(JSONObject row, String key) {
        if (row.isNull(key)) return null;
        Object value = row.opt(key);
        if (!(value instanceof String)) throw new IllegalArgumentException("Invalid Bill text: " + key);
        return (String) value;
    }
    private static Set<String> set(String... values) { return new HashSet<>(Arrays.asList(values)); }
}
