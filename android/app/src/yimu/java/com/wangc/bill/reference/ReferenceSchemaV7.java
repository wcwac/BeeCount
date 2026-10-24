package com.wangc.bill.reference;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** Host-only Transfer receipts use their own domain; transaction_id remains a Bill row key. */
final class ReferenceSchemaV7 {
    static final String RECEIPTS = "candidate_transfer_receipts";
    private ReferenceSchemaV7() {}

    /** Called inside the helper's create/upgrade transaction, with FK enforcement disabled outside it. */
    static void install(SQLiteDatabase db) {
        // Never rename the old parent: doing so can rewrite transactions.candidate_id's
        // REFERENCES clause to the temporary name. Keep every old candidate column.
        db.execSQL("CREATE TABLE candidates_v7(id TEXT PRIMARY KEY,payload TEXT NOT NULL,package_name TEXT NOT NULL,book_id INTEGER NOT NULL REFERENCES books(id),created INTEGER NOT NULL,state TEXT NOT NULL DEFAULT 'PENDING',transaction_id INTEGER,save_kind TEXT NOT NULL DEFAULT 'bill' CHECK(save_kind IN ('bill','refund','transfer')),refund_owner_semantic_id INTEGER,refund_semantic_id INTEGER,refund_item_raw TEXT)");
        String columns = "id,payload,package_name,book_id,created,state,transaction_id,save_kind,refund_owner_semantic_id,refund_semantic_id,refund_item_raw";
        db.execSQL("INSERT INTO candidates_v7(" + columns + ") SELECT " + columns + " FROM candidates");
        db.execSQL("DROP TABLE candidates");
        db.execSQL("ALTER TABLE candidates_v7 RENAME TO candidates");
        db.execSQL("CREATE INDEX candidates_state ON candidates(state,created)");
        // The immutable anchor survives physical source deletion. Only its live
        // resolution is a FK, so deletion clears that resolution without rebinding.
        db.execSQL("CREATE TABLE candidate_transfer_receipts(candidate_id TEXT PRIMARY KEY NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,transfer_row_id INTEGER NOT NULL UNIQUE CHECK(transfer_row_id>0),resolved_transfer_row_id INTEGER REFERENCES original_transfers(id) ON DELETE SET NULL,owner_semantic_id INTEGER NOT NULL,transfer_semantic_id INTEGER NOT NULL)");
        db.execSQL("CREATE TRIGGER candidate_transfer_anchor_immutable BEFORE UPDATE OF transfer_row_id,owner_semantic_id,transfer_semantic_id ON candidate_transfer_receipts WHEN NEW.transfer_row_id IS NOT OLD.transfer_row_id OR NEW.owner_semantic_id IS NOT OLD.owner_semantic_id OR NEW.transfer_semantic_id IS NOT OLD.transfer_semantic_id BEGIN SELECT RAISE(ABORT,'Transfer receipt identity is immutable'); END");
        // Check before SQLiteOpenHelper commits either the DDL or user_version.
        // An onOpen-only check would be too late to roll a bad migration back.
        checkForeignKeys(db);
    }

    static void checkForeignKeys(SQLiteDatabase db) {
        try (Cursor rows = db.rawQuery("PRAGMA foreign_key_check", null)) {
            if (rows.moveToFirst()) throw new IllegalArgumentException("数据关联损坏");
        }
    }

    static void validateRestored(ReferenceStore store) {
        synchronized (store) {
            SQLiteDatabase db = store.getReadableDatabase();
            try (Cursor rows = db.rawQuery("SELECT r.*,c.id AS matched_candidate,c.state AS candidate_state,c.save_kind AS candidate_kind,c.transaction_id AS candidate_transaction_id,t.id AS matched_transfer,t.owner_semantic_id AS transfer_owner,t.transfer_semantic_id AS transfer_identity FROM candidate_transfer_receipts r LEFT JOIN candidates c ON c.id=r.candidate_id LEFT JOIN original_transfers t ON t.id=r.transfer_row_id", null)) {
                while (rows.moveToNext()) {
                    text(rows, "candidate_id"); text(rows, "matched_candidate");
                    long anchor = integer(rows, "transfer_row_id"); if (anchor <= 0) invalid();
                    long owner = integer(rows, "owner_semantic_id");
                    if (owner < Integer.MIN_VALUE || owner > Integer.MAX_VALUE) invalid();
                    long identity = integer(rows, "transfer_semantic_id");
                    if (!"SAVED".equals(text(rows, "candidate_state"))
                            || !"transfer".equals(text(rows, "candidate_kind"))
                            || !rows.isNull(rows.getColumnIndexOrThrow("candidate_transaction_id"))) invalid();
                    int resolvedIndex = rows.getColumnIndexOrThrow("resolved_transfer_row_id");
                    boolean present = !rows.isNull(rows.getColumnIndexOrThrow("matched_transfer"));
                    if (rows.isNull(resolvedIndex)) {
                        if (present) invalid();
                    } else {
                        if (integer(rows, "resolved_transfer_row_id") != anchor || !present
                                || integer(rows, "transfer_owner") != owner
                                || integer(rows, "transfer_identity") != identity) invalid();
                    }
                }
            }
            // A Bill's reverse link is a separate column: a NULL transaction_id on
            // the Transfer candidate alone does not exclude that cross-domain link.
            try (Cursor missing = db.rawQuery("SELECT c.id FROM candidates c LEFT JOIN candidate_transfer_receipts r ON r.candidate_id=c.id WHERE c.save_kind='transfer' AND (c.state<>'SAVED' OR c.transaction_id IS NOT NULL OR r.candidate_id IS NULL OR EXISTS(SELECT 1 FROM transactions t WHERE t.candidate_id=c.id)) LIMIT 1", null)) {
                if (missing.moveToFirst()) invalid();
            }
        }
    }

    /** A restored receipt for a deleted Transfer still reserves its independent local PK. */
    static void reserveTransferAnchors(SQLiteDatabase db) {
        long anchor;
        try (Cursor rows = db.rawQuery("SELECT COALESCE(MAX(transfer_row_id),0) FROM candidate_transfer_receipts", null)) {
            if (!rows.moveToFirst()) throw new IllegalStateException("Transfer receipt sequence is unavailable");
            anchor = rows.getLong(0);
        }
        db.execSQL("INSERT INTO sqlite_sequence(name,seq) SELECT 'original_transfers',? WHERE NOT EXISTS(SELECT 1 FROM sqlite_sequence WHERE name='original_transfers')", new Object[]{anchor});
        db.execSQL("UPDATE sqlite_sequence SET seq=MAX(COALESCE(seq,0),?) WHERE name='original_transfers'", new Object[]{anchor});
    }

    private static long integer(Cursor rows, String column) {
        int index = rows.getColumnIndexOrThrow(column);
        if (rows.getType(index) != Cursor.FIELD_TYPE_INTEGER) invalid();
        return rows.getLong(index);
    }
    private static String text(Cursor rows, String column) {
        int index = rows.getColumnIndexOrThrow(column);
        if (rows.getType(index) != Cursor.FIELD_TYPE_STRING) invalid();
        return rows.getString(index);
    }
    private static void invalid() { throw new IllegalArgumentException("转账采集确认关联无效"); }
}
