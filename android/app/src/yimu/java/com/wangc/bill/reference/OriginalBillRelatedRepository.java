package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.json.JSONObject;

/** d0's owner-scoped relation records. Raw Bill IDs survive deletion; pairs are not unique. */
public final class OriginalBillRelatedRepository {
    public static final String TABLE = "original_bill_related";
    private final ReferenceStore store;
    private final Random random;

    public OriginalBillRelatedRepository(ReferenceStore store) { this(store, new Random()); }
    OriginalBillRelatedRepository(ReferenceStore store, Random random) {
        if (store == null || random == null) throw new IllegalArgumentException("关联记录存储不能为空");
        this.store = store; this.random = random;
    }

    public static void installV5Schema(SQLiteDatabase db) {
        // Original fields have no uniqueness annotation or Bill association. In particular,
        // deleting a Bill must not cascade raw relations or coalesce repeated/reversed pairs.
        db.execSQL("CREATE TABLE original_bill_related(id INTEGER PRIMARY KEY AUTOINCREMENT,owner_semantic_id INTEGER NOT NULL,related_semantic_id INTEGER NOT NULL,one_bill_semantic_id INTEGER NOT NULL,two_bill_semantic_id INTEGER NOT NULL,update_time INTEGER NOT NULL DEFAULT 0,original_delete_flag INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE INDEX original_bill_related_identity ON original_bill_related(owner_semantic_id,related_semantic_id)");
        db.execSQL("CREATE INDEX original_bill_related_one ON original_bill_related(owner_semantic_id,one_bill_semantic_id)");
        db.execSQL("CREATE INDEX original_bill_related_two ON original_bill_related(owner_semantic_id,two_bill_semantic_id)");
    }

    public static final class SaveResult {
        public final boolean saved;
        public final OriginalBillRelatedRecord record;
        public final String failureType;
        private SaveResult(boolean saved, OriginalBillRelatedRecord record, Exception error) {
            this.saved = saved; this.record = record;
            failureType = error == null ? "" : error.getClass().getName();
        }
    }

    /** d0.d allocates before entity save; zero is a valid fresh semantic ID. */
    public SaveResult tryCreate(int owner, int one, int two, long now) {
        synchronized (store) {
            OriginalBillRelatedRecord record = new OriginalBillRelatedRecord();
            record.userId = owner; record.oneBillId = one; record.twoBillId = two;
            record.updateTime = now; record.relatedId = allocateSemanticId(owner);
            return trySaveOriginalRaw(record);
        }
    }

    public int allocateSemanticId(int owner) {
        synchronized (store) {
            int id;
            do { id = random.nextInt(Integer.MAX_VALUE); }
            while (byIdentity(owner, id) != null);
            return id;
        }
    }

    /** Support.save catches Exception only; a normal zero-row update is still true. */
    public SaveResult trySaveOriginalRaw(OriginalBillRelatedRecord record) {
        try { saveOriginalRaw(record); return new SaveResult(true, record, null); }
        catch (Exception error) { return new SaveResult(false, record, error); }
    }

    /** No allocation, timestamp, network, Bill deletion or balance side effects. */
    public long saveOriginalRaw(OriginalBillRelatedRecord record) {
        if (record == null || record.localRowId < 0) throw new IllegalArgumentException("关联记录本地主键无效");
        synchronized (store) {
            SQLiteDatabase db = store.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues value = new ContentValues();
                value.put("owner_semantic_id", record.userId); value.put("related_semantic_id", record.relatedId);
                value.put("one_bill_semantic_id", record.oneBillId); value.put("two_bill_semantic_id", record.twoBillId);
                value.put("update_time", record.updateTime); value.put("original_delete_flag", record.originalDeleted ? 1 : 0);
                if (record.localRowId > 0) db.update("original_bill_related", value, "id=?", new String[]{"" + record.localRowId});
                else {
                    long inserted = db.insertOrThrow("original_bill_related", null, value);
                    if (inserted == -1L) throw new IllegalStateException("关联记录未写入");
                    record.localRowId = inserted;
                }
                db.setTransactionSuccessful(); return record.localRowId;
            } finally { db.endTransaction(); }
        }
    }

    public OriginalBillRelatedRecord byLocalRowId(long rowId) {
        synchronized (store) { return read(store.one("SELECT * FROM original_bill_related WHERE id=?", "" + rowId)); }
    }
    public OriginalBillRelatedRecord byIdentity(int owner, long semantic) {
        synchronized (store) { return read(store.one("SELECT * FROM original_bill_related WHERE owner_semantic_id=? AND related_semantic_id=? ORDER BY id LIMIT 1", "" + owner, "" + semantic)); }
    }
    public List<OriginalBillRelatedRecord> forBill(int owner, int bill) {
        synchronized (store) { return readAll(store.query("SELECT * FROM original_bill_related WHERE owner_semantic_id=? AND(one_bill_semantic_id=? OR two_bill_semantic_id=?) ORDER BY id", "" + owner, "" + bill, "" + bill)); }
    }
    public List<OriginalBillRelatedRecord> allForOwner(int owner) {
        synchronized (store) { return readAll(store.query("SELECT * FROM original_bill_related WHERE owner_semantic_id=? ORDER BY id", "" + owner)); }
    }
    public long pairCount(int owner, int one, int two) {
        synchronized (store) { return store.scalar("SELECT COUNT(*) n FROM original_bill_related WHERE owner_semantic_id=? AND((one_bill_semantic_id=? AND two_bill_semantic_id=?) OR(one_bill_semantic_id=? AND two_bill_semantic_id=?))", "" + owner, "" + one, "" + two, "" + two, "" + one); }
    }
    /** d0.k removes every symmetric pair for this owner; raw IDs are not local FKs. */
    public int deletePair(int owner, int one, int two) {
        synchronized (store) { return store.getWritableDatabase().delete("original_bill_related", "owner_semantic_id=? AND((one_bill_semantic_id=? AND two_bill_semantic_id=?) OR(one_bill_semantic_id=? AND two_bill_semantic_id=?))", new String[]{"" + owner, "" + one, "" + two, "" + two, "" + one}); }
    }
    public int deleteIdentity(int owner, long semantic) {
        synchronized (store) { return store.getWritableDatabase().delete("original_bill_related", "owner_semantic_id=? AND related_semantic_id=?", new String[]{"" + owner, "" + semantic}); }
    }
    public void validateRestored() {
        synchronized (store) { for (JSONObject row : store.query("SELECT * FROM original_bill_related")) read(row); }
    }

    private static List<OriginalBillRelatedRecord> readAll(List<JSONObject> rows) {
        List<OriginalBillRelatedRecord> result = new ArrayList<>();
        for (JSONObject row : rows) result.add(read(row)); return result;
    }
    private static OriginalBillRelatedRecord read(JSONObject row) {
        if (row == null) return null;
        OriginalBillRelatedRecord value = new OriginalBillRelatedRecord();
        value.localRowId = integer(row, "id", 1, Long.MAX_VALUE);
        value.userId = (int) integer(row, "owner_semantic_id", Integer.MIN_VALUE, Integer.MAX_VALUE);
        value.relatedId = integer(row, "related_semantic_id", Long.MIN_VALUE, Long.MAX_VALUE);
        value.oneBillId = (int) integer(row, "one_bill_semantic_id", Integer.MIN_VALUE, Integer.MAX_VALUE);
        value.twoBillId = (int) integer(row, "two_bill_semantic_id", Integer.MIN_VALUE, Integer.MAX_VALUE);
        value.updateTime = integer(row, "update_time", Long.MIN_VALUE, Long.MAX_VALUE);
        value.originalDeleted = integer(row, "original_delete_flag", 0, 1) == 1; return value;
    }
    private static long integer(JSONObject row, String column, long min, long max) {
        Object value = row.opt(column);
        if (!(value instanceof Integer || value instanceof Long)) throw new IllegalArgumentException("关联记录整数类型无效");
        long number = ((Number) value).longValue();
        if (number < min || number > max) throw new IllegalArgumentException("关联记录整数范围无效");
        return number;
    }
}
