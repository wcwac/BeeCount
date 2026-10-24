package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Raw Transfer entities, not the legacy transaction ledger or transfer financial action. */
public final class OriginalTransferRepository {
    public static final String TABLE = "original_transfers";
    public static final String TAGS_TABLE = "original_transfer_tags";
    private final ReferenceStore store;
    private final Random random;

    public OriginalTransferRepository(ReferenceStore store) { this(store, new Random()); }
    OriginalTransferRepository(ReferenceStore store, Random random) {
        if (store == null || random == null) throw new IllegalArgumentException("Transfer storage is required");
        this.store = store; this.random = random;
    }

    /** Host schema: semantic identifiers are independent of all local row keys. */
    public static void installV6Schema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE original_transfers(id INTEGER PRIMARY KEY AUTOINCREMENT,transfer_semantic_id INTEGER NOT NULL DEFAULT 0,owner_semantic_id INTEGER NOT NULL DEFAULT 0,from_asset_semantic_id INTEGER NOT NULL DEFAULT 0,to_asset_semantic_id INTEGER NOT NULL DEFAULT 0,cost REAL NOT NULL DEFAULT 0,to_cost REAL NOT NULL DEFAULT 0,service_charge REAL NOT NULL DEFAULT 0,time INTEGER NOT NULL DEFAULT 0,update_time INTEGER NOT NULL DEFAULT 0,bill_semantic_id INTEGER NOT NULL DEFAULT 0,remark TEXT,original_delete_flag INTEGER NOT NULL DEFAULT 0,tags_is_null INTEGER NOT NULL DEFAULT 0)");
        // There is no recovered unique annotation or original Bill/account FK.
        db.execSQL("CREATE INDEX original_transfers_identity ON original_transfers(owner_semantic_id,transfer_semantic_id)");
        db.execSQL("CREATE INDEX original_transfers_from ON original_transfers(owner_semantic_id,from_asset_semantic_id)");
        db.execSQL("CREATE INDEX original_transfers_to ON original_transfers(owner_semantic_id,to_asset_semantic_id)");
        // Nullable boxed Long values preserve raw input. The original generic-table DDL
        // is not recovered; this compatibility choice is not a claim about that DDL.
        db.execSQL("CREATE TABLE original_transfer_tags(id INTEGER PRIMARY KEY AUTOINCREMENT,transfer_row_id INTEGER NOT NULL REFERENCES original_transfers(id) ON DELETE CASCADE,position INTEGER NOT NULL,value INTEGER)");
        db.execSQL("CREATE INDEX original_transfer_tags_parent ON original_transfer_tags(transfer_row_id,position)");
    }

    public static final class SaveResult {
        public final boolean saved;
        /** Retains a PK assigned before an exception during generic-list persistence. */
        public final OriginalTransferRecord record;
        public final String failureType;
        private SaveResult(boolean saved, OriginalTransferRecord record, Exception error) {
            this.saved = saved; this.record = record;
            failureType = error == null ? "" : error.getClass().getName();
        }
    }

    /** Support.save catches Exception only; Error still unwinds the entity transaction. */
    public SaveResult trySaveOriginalRaw(OriginalTransferRecord record) {
        try { saveOriginalRaw(record); return new SaveResult(true, record, null); }
        catch (Exception error) { AccountingCommitScope.failed(store,error); return new SaveResult(false, record, error); }
    }

    /** No owner normalization, semantic allocation, timestamp, balance, files or UI effects. */
    public long saveOriginalRaw(OriginalTransferRecord record) {
        if (record == null || record.localRowId < 0) throw new IllegalArgumentException("Invalid Transfer local key");
        synchronized (store) {
            SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
            try {
                ContentValues values = values(record);
                if (record.localRowId > 0) {
                    // SaveHandler writes primitive zero and ignores the UPDATE row count.
                    AccountingCommitScope.requireChanged(store, db.update(TABLE, values, "id=?", new String[]{"" + record.localRowId}));
                } else {
                    values.put("tags_is_null", record.tags == null ? 1 : 0);
                    long inserted = db.insertOrThrow(TABLE, null, values);
                    if (inserted == -1L) throw new IllegalStateException("Transfer was not inserted");
                    // afterSave assigns the PK before updateGenericTables. A later failure
                    // rolls SQL back but does not restore the in-memory model's baseObjId.
                    record.localRowId = inserted;
                }
                if (record.tags != null) replaceTags(db, record.localRowId, record.tags);
                db.setTransactionSuccessful(); return record.localRowId;
            } finally { db.endTransaction(); }
        }
    }

    private static ContentValues values(OriginalTransferRecord r) {
        // Explicit host storage boundary; source primitive setters have no finite gate.
        if (!Double.isFinite(r.cost) || !Double.isFinite(r.toCost) || !Double.isFinite(r.serviceCharge))
            throw new IllegalArgumentException("Nonfinite Transfer amount");
        ContentValues v = new ContentValues();
        v.put("transfer_semantic_id", r.transferId); v.put("owner_semantic_id", r.userId);
        v.put("from_asset_semantic_id", r.fromAssetId); v.put("to_asset_semantic_id", r.toAssetId);
        v.put("cost", r.cost); v.put("to_cost", r.toCost); v.put("service_charge", r.serviceCharge);
        v.put("time", r.time); v.put("update_time", r.updateTime); v.put("bill_semantic_id", r.billId);
        if (r.remark != null) v.put("remark", r.remark);
        v.put("original_delete_flag", r.originalDeleted ? 1 : 0);
        // A null collection on UPDATE omits both its flag and its existing generic rows.
        if (r.tags != null) v.put("tags_is_null", 0);
        return v;
    }

    private void replaceTags(SQLiteDatabase db, long rowId, List<Long> tags) {
        db.delete(TAGS_TABLE, "transfer_row_id=?", new String[]{"" + rowId});
        long position = 0;
        for (int i = 0; i < tags.size(); i++) {
            // The generic type is deliberate: a raw List containing another runtime
            // type fails here, rather than silently converting it to a long or text.
            Long tag = tags.get(i);
            ContentValues v = new ContentValues();
            v.put("transfer_row_id", rowId); v.put("position", position); v.put("value", tag);
            // The bundled SaveHandler ignores generic SQLiteDatabase.insert's -1.
            // Keep the successful subsequence ordered without inventing a missing
            // position in our additional ordering metadata. Never change the model list.
            long inserted = db.insert(TAGS_TABLE, null, v);
            if(inserted == -1L && AccountingCommitScope.active(store)) throw new IllegalStateException("转账标签保存失败");
            if (inserted != -1L) position++;
        }
    }

    /** w2.q: owner-scoped nonnegative int allocation includes zero; raw IDs remain long. */
    public int allocateSemanticId(int owner) {
        synchronized (store) {
            int id;
            do { id = random.nextInt(Integer.MAX_VALUE); } while (byIdentity(owner, id) != null);
            return id;
        }
    }
    public OriginalTransferRecord byLocalRowId(long rowId) {
        synchronized (store) { return first("id=?", "id", "" + rowId); }
    }
    /** w2.N finds the first exact current-owner semantic identity, without a book filter. */
    public OriginalTransferRecord byIdentity(int owner, long semanticId) {
        synchronized (store) { return first("owner_semantic_id=? AND transfer_semantic_id=?", "id", "" + owner, "" + semanticId); }
    }
    public List<OriginalTransferRecord> allForOwner(int owner) {
        synchronized (store) { return list("owner_semantic_id=?", "id", "" + owner); }
    }
    /** w2.b0: both endpoints, current user only, time DESC; no showBook/hide/delete filter. */
    public List<OriginalTransferRecord> forAsset(int owner, long assetSemanticId) {
        synchronized (store) { return list("owner_semantic_id=? AND(from_asset_semantic_id=? OR to_asset_semantic_id=?)", "time DESC", "" + owner, "" + assetSemanticId, "" + assetSemanticId); }
    }
    /** w2.x/z use local insertion order for manual account suggestions. */
    public OriginalTransferRecord lastFrom(int owner, long assetSemanticId) {
        synchronized (store) { return first("owner_semantic_id=? AND from_asset_semantic_id=?", "id DESC", "" + owner, "" + assetSemanticId); }
    }
    public OriginalTransferRecord lastTo(int owner, long assetSemanticId) {
        synchronized (store) { return first("owner_semantic_id=? AND to_asset_semantic_id=?", "id DESC", "" + owner, "" + assetSemanticId); }
    }

    /** w2.y: insertion-order Transfer links, then a0.a0's user/fromUser access domain. */
    public OriginalBillRecord latestFeeBill(int owner, int currentBookOwner, boolean income) {
        synchronized (store) {
            OriginalBillRepository bills = new OriginalBillRepository(store);
            try (Cursor rows = store.getReadableDatabase().rawQuery(
                    "SELECT bill_semantic_id FROM original_transfers WHERE owner_semantic_id=? AND bill_semantic_id<>0 ORDER BY id DESC",
                    new String[]{"" + owner})) {
                while (rows.moveToNext()) {
                    OriginalBillRecord bill = bills.findAccessible(owner, currentBookOwner, intValue(rows, "bill_semantic_id"));
                    if (bill != null && (bill.parentCategoryId == 9) == income) return bill;
                }
            }
            return null;
        }
    }

    /** Storage-only physical delete. The caller owns fee deletion and signed principal inverses. */
    public int deleteRaw(OriginalTransferRecord record) {
        if (record == null || record.localRowId < 0) throw new IllegalArgumentException("Invalid Transfer local key");
        synchronized (store) {
            SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
            try {
                int rows = db.delete(TABLE, "id=?", new String[]{"" + record.localRowId});
                record.localRowId = 0; db.setTransactionSuccessful(); return rows;
            } finally { db.endTransaction(); }
        }
    }

    private OriginalTransferRecord first(String where, String order, String... args) {
        try (Cursor rows = store.getReadableDatabase().rawQuery("SELECT * FROM original_transfers WHERE " + where + " ORDER BY " + order + " LIMIT 1", args)) {
            return rows.moveToFirst() ? read(rows) : null;
        }
    }
    private List<OriginalTransferRecord> list(String where, String order, String... args) {
        List<OriginalTransferRecord> result = new ArrayList<>();
        try (Cursor rows = store.getReadableDatabase().rawQuery("SELECT * FROM original_transfers WHERE " + where + " ORDER BY " + order, args)) {
            while (rows.moveToNext()) result.add(read(rows));
        }
        return result;
    }
    private OriginalTransferRecord read(Cursor c) {
        OriginalTransferRecord r = new OriginalTransferRecord();
        r.localRowId = integer(c, "id"); if (r.localRowId <= 0) invalid();
        r.transferId = integer(c, "transfer_semantic_id"); r.userId = intValue(c, "owner_semantic_id");
        r.fromAssetId = integer(c, "from_asset_semantic_id"); r.toAssetId = integer(c, "to_asset_semantic_id");
        r.cost = real(c, "cost"); r.toCost = real(c, "to_cost"); r.serviceCharge = real(c, "service_charge");
        r.time = integer(c, "time"); r.updateTime = integer(c, "update_time"); r.billId = intValue(c, "bill_semantic_id");
        r.remark = text(c, "remark"); r.originalDeleted = flag(c, "original_delete_flag");
        boolean nullTags = flag(c, "tags_is_null"); r.tags = nullTags ? null : new ArrayList<>();
        try (Cursor tags = store.getReadableDatabase().rawQuery("SELECT * FROM original_transfer_tags WHERE transfer_row_id=? ORDER BY position,id", new String[]{"" + r.localRowId})) {
            long next = 0;
            while (tags.moveToNext()) {
                if (nullTags || integer(tags, "id") <= 0 || integer(tags, "transfer_row_id") != r.localRowId
                        || integer(tags, "position") != next++) invalid();
                int value = tags.getColumnIndexOrThrow("value");
                r.tags.add(tags.isNull(value) ? null : Long.valueOf(integer(tags, "value")));
            }
        }
        return r;
    }

    /** Strict restore validation preserves NULL/raw identifiers and rejects coercion/truncation. */
    public void validateRestored() {
        synchronized (store) {
            try (Cursor rows = store.getReadableDatabase().rawQuery("SELECT * FROM original_transfers", null)) {
                while (rows.moveToNext()) read(rows);
            }
            try (Cursor orphan = store.getReadableDatabase().rawQuery("SELECT t.id FROM original_transfer_tags t LEFT JOIN original_transfers p ON p.id=t.transfer_row_id WHERE p.id IS NULL LIMIT 1", null)) {
                if (orphan.moveToFirst()) invalid();
            }
        }
    }
    private static long integer(Cursor c, String key) {
        int i = c.getColumnIndexOrThrow(key); if (c.getType(i) != Cursor.FIELD_TYPE_INTEGER) invalid(); return c.getLong(i);
    }
    private static int intValue(Cursor c, String key) {
        long value = integer(c, key); if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) invalid(); return (int) value;
    }
    private static boolean flag(Cursor c, String key) {
        long value = integer(c, key); if (value != 0 && value != 1) invalid(); return value != 0;
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
    private static void invalid() { throw new IllegalArgumentException("Invalid original Transfer metadata"); }
}
