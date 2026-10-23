package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Raw Instalment storage from g1, independent of Bill/asset mutations and credit projection. */
public final class OriginalInstalmentRepository {
    public static final String TABLE = "original_instalments";
    private final ReferenceStore store;
    private final Random random;

    public OriginalInstalmentRepository(ReferenceStore store) { this(store, new Random()); }
    OriginalInstalmentRepository(ReferenceStore store, Random random) {
        if (store == null || random == null) throw new IllegalArgumentException("Instalment storage is required");
        this.store = store; this.random = random;
    }

    /** Explicit local schema, not copied LitePal DDL. Original links may remain unresolved. */
    public static void installV5Schema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE original_instalments(id INTEGER PRIMARY KEY AUTOINCREMENT,account_month TEXT,asset_semantic_id INTEGER NOT NULL,parent_bill_semantic_id INTEGER NOT NULL,in_asset_time INTEGER NOT NULL,instalment_semantic_id INTEGER NOT NULL,instalment_type INTEGER NOT NULL,periods INTEGER NOT NULL,remainder_type INTEGER NOT NULL,self_info TEXT,service_number REAL NOT NULL,service_type INTEGER NOT NULL,total_number REAL NOT NULL,update_time INTEGER NOT NULL,owner_semantic_id INTEGER NOT NULL,original_delete_flag INTEGER NOT NULL)");
        // The recovered fields declare no unique constraint. Semantic identities are not PKs.
        db.execSQL("CREATE INDEX original_instalments_identity ON original_instalments(owner_semantic_id,instalment_semantic_id)");
        db.execSQL("CREATE INDEX original_instalments_bill ON original_instalments(owner_semantic_id,parent_bill_semantic_id)");
        db.execSQL("CREATE INDEX original_instalments_asset ON original_instalments(owner_semantic_id,asset_semantic_id)");
    }

    public OriginalInstalmentRecord byLocalRowId(long rowId) {
        synchronized (store) { return first("id=?", "" + rowId); }
    }
    /** g1.r: exact current owner and raw long instalment identity. */
    public OriginalInstalmentRecord byIdentity(int owner, long semanticId) {
        synchronized (store) { return first("owner_semantic_id=? AND instalment_semantic_id=?", "" + owner, "" + semanticId); }
    }
    /** g1.x: first current-owner aggregate by semantic Bill ID, without a book predicate. */
    public OriginalInstalmentRecord firstByBill(int owner, long billSemanticId) {
        synchronized (store) { return first("owner_semantic_id=? AND parent_bill_semantic_id=?", "" + owner, "" + billSemanticId); }
    }
    public List<OriginalInstalmentRecord> allForOwner(int owner) {
        synchronized (store) { return list("owner_semantic_id=?", "" + owner); }
    }
    /** g1.w includes both standalone and Bill-linked records; no instalmentType predicate. */
    public List<OriginalInstalmentRecord> forAsset(int owner, long assetSemanticId) {
        synchronized (store) { return list("owner_semantic_id=? AND asset_semantic_id=?", "" + owner, "" + assetSemanticId); }
    }
    public List<OriginalInstalmentRecord> billLinkedForOwner(int owner) {
        synchronized (store) { return list("owner_semantic_id=? AND parent_bill_semantic_id>0", "" + owner); }
    }
    public List<OriginalInstalmentRecord> updatedAfter(int owner, long time) {
        synchronized (store) { return list("owner_semantic_id=? AND update_time>?", "" + owner, "" + time); }
    }
    public long maxUpdatedTime(int owner) {
        synchronized (store) { return store.scalar("SELECT COALESCE(MAX(update_time),0) n FROM original_instalments WHERE owner_semantic_id=?", "" + owner); }
    }
    /** g1.n allocates int-sized nonnegative values including zero, independently of local PK. */
    public int allocateSemanticId(int owner) {
        synchronized (store) {
            int semantic;
            do { semantic = random.nextInt(Integer.MAX_VALUE); } while (byIdentity(owner, semantic) != null);
            return semantic;
        }
    }

    public static final class SaveResult {
        public final boolean saved;
        /** Same mutable object, including any PK assigned before a later transaction failure. */
        public final OriginalInstalmentRecord record;
        public final String failureType;
        private SaveResult(boolean saved, OriginalInstalmentRecord record, Exception error) {
            this.saved = saved; this.record = record; failureType = error == null ? "" : error.getClass().getName();
        }
    }
    public SaveResult trySaveOriginalRaw(OriginalInstalmentRecord record) {
        try { saveOriginalRaw(record); return new SaveResult(true, record, null); }
        catch (Exception error) { return new SaveResult(false, record, error); }
    }

    /** Save's primitive-zero/null-reference behavior, without g1.d/E timestamps or callbacks. */
    public long saveOriginalRaw(OriginalInstalmentRecord record) {
        if (record == null) throw new IllegalArgumentException("Instalment is required");
        synchronized (store) {
            SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
            try {
                ContentValues values = values(record);
                if (record.localRowId > 0) {
                    // Original SaveHandler ignores the number of affected rows.
                    db.update(TABLE, values, "id=?", new String[]{"" + record.localRowId});
                } else {
                    long inserted = db.insertOrThrow(TABLE, null, values);
                    if (inserted == -1L) throw new IllegalStateException("Instalment was not inserted");
                    record.localRowId = inserted;
                }
                db.setTransactionSuccessful(); return record.localRowId;
            } finally { db.endTransaction(); }
        }
    }

    /** Support.delete physically deletes and clears the model PK on a normal return, even zero rows. */
    public int deleteRaw(OriginalInstalmentRecord record) {
        if (record == null) throw new IllegalArgumentException("Instalment is required");
        synchronized (store) {
            SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
            try {
                int rows = db.delete(TABLE, "id=?", new String[]{"" + record.localRowId});
                record.localRowId = 0;
                db.setTransactionSuccessful(); return rows;
            } finally { db.endTransaction(); }
        }
    }

    private static ContentValues values(OriginalInstalmentRecord r) {
        // Explicit finite storage policy; original primitive setters have no such universal gate.
        if (!Double.isFinite(r.totalNumber) || !Double.isFinite(r.serviceNumber)) throw new IllegalArgumentException("Nonfinite Instalment amount");
        ContentValues v = new ContentValues();
        if (r.accountMonth != null) v.put("account_month", r.accountMonth);
        v.put("asset_semantic_id", r.assetId); v.put("parent_bill_semantic_id", r.billId);
        v.put("in_asset_time", r.inAssetTime); v.put("instalment_semantic_id", r.instalmentId);
        v.put("instalment_type", r.instalmentType); v.put("periods", r.periods); v.put("remainder_type", r.remainderType);
        if (r.selfInfo != null) v.put("self_info", r.selfInfo);
        v.put("service_number", r.serviceNumber); v.put("service_type", r.serviceType); v.put("total_number", r.totalNumber);
        v.put("update_time", r.updateTime); v.put("owner_semantic_id", r.userId); v.put("original_delete_flag", r.originalDeleted ? 1 : 0);
        return v;
    }
    private OriginalInstalmentRecord first(String where, String... args) {
        try (Cursor rows = store.getReadableDatabase().rawQuery("SELECT * FROM original_instalments WHERE " + where + " LIMIT 1", args)) {
            return rows.moveToFirst() ? read(rows) : null;
        }
    }
    private List<OriginalInstalmentRecord> list(String where, String... args) {
        List<OriginalInstalmentRecord> result = new ArrayList<>();
        try (Cursor rows = store.getReadableDatabase().rawQuery("SELECT * FROM original_instalments WHERE " + where, args)) {
            while (rows.moveToNext()) result.add(read(rows));
        }
        return result;
    }
    private static OriginalInstalmentRecord read(Cursor c) {
        OriginalInstalmentRecord r = new OriginalInstalmentRecord();
        r.localRowId = integer(c, "id"); if (r.localRowId <= 0) invalid();
        r.accountMonth = text(c, "account_month"); r.assetId = integer(c, "asset_semantic_id");
        r.billId = intValue(c, "parent_bill_semantic_id"); r.inAssetTime = integer(c, "in_asset_time");
        r.instalmentId = integer(c, "instalment_semantic_id"); r.instalmentType = intValue(c, "instalment_type");
        r.periods = intValue(c, "periods"); r.remainderType = intValue(c, "remainder_type"); r.selfInfo = text(c, "self_info");
        r.serviceNumber = real(c, "service_number"); r.serviceType = intValue(c, "service_type"); r.totalNumber = real(c, "total_number");
        r.updateTime = integer(c, "update_time"); r.userId = intValue(c, "owner_semantic_id");
        long deleted = integer(c, "original_delete_flag"); if (deleted != 0 && deleted != 1) invalid();
        r.originalDeleted = deleted == 1; return r;
    }
    public void validateRestored() {
        synchronized (store) {
            try (Cursor rows = store.getReadableDatabase().rawQuery("SELECT * FROM original_instalments", null)) {
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
    private static String text(Cursor c, String key) {
        int i = c.getColumnIndexOrThrow(key); if (c.isNull(i)) return null;
        if (c.getType(i) != Cursor.FIELD_TYPE_STRING) invalid(); return c.getString(i);
    }
    private static void invalid() { throw new IllegalArgumentException("Invalid Instalment metadata"); }
}
