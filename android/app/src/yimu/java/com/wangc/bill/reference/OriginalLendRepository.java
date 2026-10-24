package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Raw original Lend persistence and i1 queries; balances, i1.d/w0 and X belong to their callers. */
public final class OriginalLendRepository {
    public static final String TABLE = "original_lends";
    private final ReferenceStore store;
    private final Random random;

    public OriginalLendRepository(ReferenceStore store) { this(store, new Random()); }
    OriginalLendRepository(ReferenceStore store, Random random) {
        if (store == null || random == null) throw new IllegalArgumentException("Lend storage is required");
        this.store = store; this.random = random;
    }

    /**
     * Host bridge schema, not the unknown original SQLite DDL. There are no source
     * generic lists or book/FX fields, and no Asset/Bill semantic FK or cascade.
     */
    public static void installV8Schema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE original_lends(id INTEGER PRIMARY KEY AUTOINCREMENT,lend_semantic_id INTEGER NOT NULL DEFAULT 0,owner_semantic_id INTEGER NOT NULL DEFAULT 0,asset_semantic_id INTEGER NOT NULL DEFAULT 0,repayment_asset_semantic_id INTEGER NOT NULL DEFAULT 0,bill_semantic_id INTEGER NOT NULL DEFAULT 0,type INTEGER NOT NULL DEFAULT 0,number REAL NOT NULL DEFAULT 0,from_cost REAL NOT NULL DEFAULT 0,interest REAL NOT NULL DEFAULT 0,general_service_bill INTEGER NOT NULL DEFAULT 0,out_time INTEGER NOT NULL DEFAULT 0,in_time INTEGER NOT NULL DEFAULT 0,create_time INTEGER NOT NULL DEFAULT 0,update_time INTEGER NOT NULL DEFAULT 0,remark TEXT,original_delete_flag INTEGER NOT NULL DEFAULT 0)");
        // Source allocation checks owner+semantic collisions, but is not a UNIQUE annotation.
        db.execSQL("CREATE INDEX original_lends_identity ON original_lends(owner_semantic_id,lend_semantic_id)");
        db.execSQL("CREATE INDEX original_lends_repayment ON original_lends(owner_semantic_id,repayment_asset_semantic_id)");
        db.execSQL("CREATE INDEX original_lends_interest ON original_lends(owner_semantic_id,asset_semantic_id,type,out_time)");
    }

    public static final class SaveResult {
        public final boolean saved;
        /** The exact mutable object, including any assigned baseObjId. */
        public final OriginalLendRecord record;
        public final String failureType;
        private SaveResult(boolean saved, OriginalLendRecord record, Exception error) {
            this.saved = saved; this.record = record;
            failureType = error == null ? "" : error.getClass().getName();
        }
    }
    /** LitePalSupport.save catches Exception, not Error. */
    public SaveResult trySaveOriginalRaw(OriginalLendRecord record) {
        try { saveOriginalRaw(record); return new SaveResult(true, record, null); }
        catch (Exception error) { AccountingCommitScope.failed(store,error); return new SaveResult(false, record, error); }
    }

    /** No owner, ID, time, event or financial inference; primitive zero/false still writes. */
    public long saveOriginalRaw(OriginalLendRecord record) {
        if (record == null || record.localRowId < 0) throw new IllegalArgumentException("Invalid Lend local key");
        synchronized (store) {
            SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
            try {
                ContentValues values = values(record);
                if (record.localRowId > 0) {
                    // Original SaveHandler ignores a normal UPDATE's affected-row count.
                    AccountingCommitScope.requireChanged(store, db.update(TABLE, values, "id=?", new String[]{"" + record.localRowId}));
                } else {
                    long inserted = db.insertOrThrow(TABLE, null, values);
                    if (inserted == -1L) throw new IllegalStateException("Lend was not inserted");
                    record.localRowId = inserted;
                }
                db.setTransactionSuccessful(); return record.localRowId;
            } finally { db.endTransaction(); }
        }
    }
    private static ContentValues values(OriginalLendRecord r) {
        // Explicit host storage boundary; the source model's primitive setters lack this gate.
        if (!Double.isFinite(r.number) || !Double.isFinite(r.fromCost) || !Double.isFinite(r.interest))
            throw new IllegalArgumentException("Nonfinite Lend amount");
        ContentValues v = new ContentValues();
        v.put("lend_semantic_id", r.lendId); v.put("owner_semantic_id", r.userId);
        v.put("asset_semantic_id", r.assetId); v.put("repayment_asset_semantic_id", r.repaymentAssetId);
        v.put("bill_semantic_id", r.billId); v.put("type", r.type); v.put("number", r.number);
        v.put("from_cost", r.fromCost); v.put("interest", r.interest);
        v.put("general_service_bill", r.generalServiceBill ? 1 : 0);
        v.put("out_time", r.outTime); v.put("in_time", r.inTime);
        v.put("create_time", r.createTime); v.put("update_time", r.updateTime);
        if (r.remark != null) v.put("remark", r.remark);
        v.put("original_delete_flag", r.originalDeleted ? 1 : 0); return v;
    }

    /** i1.u includes zero and retries same-current-owner collisions only. */
    public int allocateSemanticId(int owner) {
        synchronized (store) {
            int id;
            do { id = random.nextInt(Integer.MAX_VALUE); } while (byIdentity(owner, id) != null);
            return id;
        }
    }
    public OriginalLendRecord byLocalRowId(long rowId) {
        synchronized (store) { return first("id=?", "" + rowId); }
    }
    /** i1.Q does not impose a book, type, delete or explicit order predicate. */
    public OriginalLendRecord byIdentity(int owner, int semanticId) {
        synchronized (store) { return first("owner_semantic_id=? AND lend_semantic_id=?", "" + owner, "" + semanticId); }
    }
    public List<OriginalLendRecord> allForOwner(int owner) {
        synchronized (store) { return list("owner_semantic_id=?", null, "" + owner); }
    }
    /** i1.V: repayment endpoint only, all types, no time/book/delete restriction or ORDER BY. */
    public List<OriginalLendRecord> forRepaymentAsset(int owner, long assetSemanticId) {
        synchronized (store) { return list("owner_semantic_id=? AND repayment_asset_semantic_id=?", null, "" + owner, "" + assetSemanticId); }
    }
    /** i1.g0 returns all interest signs; its credit caller subsequently sums positive interest only. */
    public List<OriginalLendRecord> interestRows(int owner, long assetSemanticId, long inclusiveStart, long inclusiveEnd) {
        synchronized (store) {
            return list("owner_semantic_id=? AND asset_semantic_id=? AND type=4 AND out_time>=? AND out_time<=?",
                    "out_time DESC", "" + owner, "" + assetSemanticId, "" + inclusiveStart, "" + inclusiveEnd);
        }
    }
    /** i1.M: type-scoped insertion order, then a0.a0 access and requested income direction. */
    public OriginalBillRecord latestInterestBill(int owner, int currentBookOwner, int type, boolean income) {
        synchronized (store) {
            OriginalBillRepository bills = new OriginalBillRepository(store);
            try (Cursor rows = store.getReadableDatabase().rawQuery(
                    "SELECT bill_semantic_id FROM original_lends WHERE owner_semantic_id=? AND type=? AND bill_semantic_id<>0 ORDER BY id DESC",
                    new String[]{"" + owner, "" + type})) {
                while (rows.moveToNext()) {
                    OriginalBillRecord bill = bills.findAccessible(owner, currentBookOwner, intValue(rows, "bill_semantic_id"));
                    if (bill != null && (bill.parentCategoryId == 9) == income) return bill;
                }
            }
            return null;
        }
    }

    /** Physical storage delete only; i1.k's files/HTTP and financial inverse belong to its caller. */
    public int deleteRaw(OriginalLendRecord record) {
        if (record == null || record.localRowId < 0) throw new IllegalArgumentException("Invalid Lend local key");
        synchronized (store) {
            SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
            try {
                int rows = db.delete(TABLE, "id=?", new String[]{"" + record.localRowId});
                record.localRowId = 0; db.setTransactionSuccessful(); return rows;
            } finally { db.endTransaction(); }
        }
    }
    private OriginalLendRecord first(String where, String... args) {
        try (Cursor rows = store.getReadableDatabase().rawQuery("SELECT * FROM original_lends WHERE " + where + " LIMIT 1", args)) {
            return rows.moveToFirst() ? read(rows) : null;
        }
    }
    private List<OriginalLendRecord> list(String where, String order, String... args) {
        List<OriginalLendRecord> result = new ArrayList<>();
        String sql = "SELECT * FROM original_lends WHERE " + where + (order == null ? "" : " ORDER BY " + order);
        try (Cursor rows = store.getReadableDatabase().rawQuery(sql, args)) { while (rows.moveToNext()) result.add(read(rows)); }
        return result;
    }
    private static OriginalLendRecord read(Cursor c) {
        OriginalLendRecord r = new OriginalLendRecord();
        r.localRowId = integer(c, "id"); if (r.localRowId <= 0) invalid();
        r.lendId = intValue(c, "lend_semantic_id"); r.userId = intValue(c, "owner_semantic_id");
        r.assetId = integer(c, "asset_semantic_id"); r.repaymentAssetId = integer(c, "repayment_asset_semantic_id");
        r.billId = intValue(c, "bill_semantic_id"); r.type = intValue(c, "type");
        r.number = real(c, "number"); r.fromCost = real(c, "from_cost"); r.interest = real(c, "interest");
        r.generalServiceBill = flag(c, "general_service_bill"); r.originalDeleted = flag(c, "original_delete_flag");
        r.outTime = integer(c, "out_time"); r.inTime = integer(c, "in_time");
        r.createTime = integer(c, "create_time"); r.updateTime = integer(c, "update_time");
        r.remark = text(c, "remark"); return r;
    }
    public void validateRestored() {
        synchronized (store) {
            try (Cursor rows = store.getReadableDatabase().rawQuery("SELECT * FROM original_lends", null)) {
                while (rows.moveToNext()) read(rows);
            }
        }
    }
    private static long integer(Cursor c, String key) {
        int i = c.getColumnIndexOrThrow(key); if (c.getType(i) != Cursor.FIELD_TYPE_INTEGER) invalid(); return c.getLong(i);
    }
    private static int intValue(Cursor c, String key) {
        long value = integer(c, key); if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) invalid(); return (int) value;
    }
    private static double real(Cursor c, String key) {
        int i = c.getColumnIndexOrThrow(key), type = c.getType(i);
        if ((type != Cursor.FIELD_TYPE_FLOAT && type != Cursor.FIELD_TYPE_INTEGER) || !Double.isFinite(c.getDouble(i))) invalid();
        return c.getDouble(i);
    }
    private static boolean flag(Cursor c, String key) {
        long value = integer(c, key); if (value != 0 && value != 1) invalid(); return value != 0;
    }
    private static String text(Cursor c, String key) {
        int i = c.getColumnIndexOrThrow(key); if (c.isNull(i)) return null;
        if (c.getType(i) != Cursor.FIELD_TYPE_STRING) invalid(); return c.getString(i);
    }
    private static void invalid() { throw new IllegalArgumentException("Invalid original Lend metadata"); }
}
