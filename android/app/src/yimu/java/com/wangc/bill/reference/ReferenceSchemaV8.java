package com.wangc.bill.reference;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;

/** Host Lend receipts are independent from both Bill and Transfer row identities. */
final class ReferenceSchemaV8 {
    static final String RECEIPTS = "candidate_lend_receipts";
    private ReferenceSchemaV8() {}

    /** Called in create/upgrade's transaction, with foreign keys disabled before it starts. */
    static void install(SQLiteDatabase db) {
        // Rebuild the parent without renaming it first: the final FK target of
        // transactions and existing Transfer receipts must remain candidates.
        db.execSQL("CREATE TABLE candidates_v8(id TEXT PRIMARY KEY,payload TEXT NOT NULL,package_name TEXT NOT NULL,book_id INTEGER NOT NULL REFERENCES books(id),created INTEGER NOT NULL,state TEXT NOT NULL DEFAULT 'PENDING',transaction_id INTEGER,save_kind TEXT NOT NULL DEFAULT 'bill' CHECK(save_kind IN ('bill','refund','transfer','lend')),refund_owner_semantic_id INTEGER,refund_semantic_id INTEGER,refund_item_raw TEXT)");
        String columns = "id,payload,package_name,book_id,created,state,transaction_id,save_kind,refund_owner_semantic_id,refund_semantic_id,refund_item_raw";
        db.execSQL("INSERT INTO candidates_v8(" + columns + ") SELECT " + columns + " FROM candidates");
        db.execSQL("DROP TABLE candidates");
        db.execSQL("ALTER TABLE candidates_v8 RENAME TO candidates");
        db.execSQL("CREATE INDEX candidates_state ON candidates(state,created)");
        OriginalLendRepository.installV8Schema(db);
        // Only the live resolution is a FK. The immutable positive anchor remains
        // after physical deletion and reserves its own domain's AUTOINCREMENT ID.
        db.execSQL("CREATE TABLE candidate_lend_receipts(candidate_id TEXT PRIMARY KEY NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,lend_row_id INTEGER NOT NULL UNIQUE CHECK(lend_row_id>0),resolved_lend_row_id INTEGER REFERENCES original_lends(id) ON DELETE SET NULL,owner_semantic_id INTEGER NOT NULL,lend_semantic_id INTEGER NOT NULL)");
        db.execSQL("CREATE TRIGGER candidate_lend_anchor_immutable BEFORE UPDATE OF candidate_id,lend_row_id,owner_semantic_id,lend_semantic_id ON candidate_lend_receipts WHEN NEW.candidate_id IS NOT OLD.candidate_id OR NEW.lend_row_id IS NOT OLD.lend_row_id OR NEW.owner_semantic_id IS NOT OLD.owner_semantic_id OR NEW.lend_semantic_id IS NOT OLD.lend_semantic_id BEGIN SELECT RAISE(ABORT,'Lend receipt identity is immutable'); END");
        ReferenceSchemaV7.checkForeignKeys(db);
    }

    static void validateRestored(ReferenceStore store) {
        synchronized (store) {
            new OriginalLendRepository(store).validateRestored();
            // Start from each side: orphan receipt rows and SAVED-kind rows that
            // lack a receipt must both fail, even before foreign_key_check.
            for (JSONObject receipt : store.query("SELECT candidate_id FROM candidate_lend_receipts")) {
                JSONObject candidate = store.candidate(receipt.optString("candidate_id"));
                if (candidate == null) invalid();
                validateReceipt(store, candidate);
            }
            for (JSONObject candidate : store.query("SELECT * FROM candidates WHERE save_kind='lend'"))
                validateReceipt(store, candidate);
        }
    }

    /** Also used before a host confirmation returns its previously saved anchor. */
    static void validateReceipt(ReferenceStore store, JSONObject candidate) {
        synchronized (store) {
            if (candidate == null || !"SAVED".equals(candidate.optString("state"))
                    || !"lend".equals(candidate.optString("save_kind"))
                    || !candidate.isNull("transaction_id")
                    || !candidate.isNull("refund_owner_semantic_id") || !candidate.isNull("refund_semantic_id")
                    || !candidate.isNull("refund_item_raw")) invalid();
            String candidateId = candidate.optString("id");
            JSONObject payload = Json.parse(candidate.optString("payload"));
            if (!"transfer".equals(payload.optString("kind"))
                    || !"original_lend".equals(payload.optString("financial_type"))
                    || store.one("SELECT id FROM transactions WHERE candidate_id=? LIMIT 1", candidateId) != null
                    || store.one("SELECT candidate_id FROM candidate_transfer_receipts WHERE candidate_id=?", candidateId) != null)
                invalid();
            try (Cursor rows = store.getReadableDatabase().rawQuery(
                    "SELECT r.*,l.id AS matched_lend,l.owner_semantic_id AS lend_owner,l.lend_semantic_id AS lend_identity FROM candidate_lend_receipts r LEFT JOIN original_lends l ON l.id=r.lend_row_id WHERE r.candidate_id=?",
                    new String[]{candidateId})) {
                if (!rows.moveToFirst() || !candidateId.equals(text(rows, "candidate_id"))) invalid();
                long anchor = integer(rows, "lend_row_id"); if (anchor <= 0) invalid();
                long owner = intValue(rows, "owner_semantic_id"), semantic = intValue(rows, "lend_semantic_id");
                boolean present = !rows.isNull(rows.getColumnIndexOrThrow("matched_lend"));
                if (rows.isNull(rows.getColumnIndexOrThrow("resolved_lend_row_id"))) {
                    if (present) invalid();
                } else if (integer(rows, "resolved_lend_row_id") != anchor || !present
                        || integer(rows, "lend_owner") != owner || integer(rows, "lend_identity") != semantic) invalid();
                // Original i1.d may reassign owner/semantic ID even on a saved PK.
                // It is deliberately unchanged. This host's fresh-confirmation
                // receipt rejects later identity drift; it never rebinds/detaches
                // a live row to pretend that source action preserves identity.
                if (!sameInteger(payload, "lend_row_id", anchor)
                        || !sameInteger(payload, "lend_owner_semantic_id", owner)
                        || !sameInteger(payload, "lend_semantic_id", semantic)) invalid();
            }
        }
    }

    static void reserveLendAnchors(SQLiteDatabase db) {
        long anchor;
        try (Cursor rows = db.rawQuery("SELECT COALESCE(MAX(lend_row_id),0) FROM candidate_lend_receipts", null)) {
            if (!rows.moveToFirst()) throw new IllegalStateException("Lend receipt sequence is unavailable");
            anchor = rows.getLong(0);
        }
        db.execSQL("INSERT INTO sqlite_sequence(name,seq) SELECT 'original_lends',? WHERE NOT EXISTS(SELECT 1 FROM sqlite_sequence WHERE name='original_lends')", new Object[]{anchor});
        db.execSQL("UPDATE sqlite_sequence SET seq=MAX(COALESCE(seq,0),?) WHERE name='original_lends'", new Object[]{anchor});
    }

    private static boolean sameInteger(JSONObject value, String key, long expected) {
        Object number = value.opt(key);
        return (number instanceof Long || number instanceof Integer) && ((Number) number).longValue() == expected;
    }
    private static long integer(Cursor rows, String key) {
        int index = rows.getColumnIndexOrThrow(key);
        if (rows.getType(index) != Cursor.FIELD_TYPE_INTEGER) invalid();
        return rows.getLong(index);
    }
    private static long intValue(Cursor rows, String key) {
        long value = integer(rows, key);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) invalid();
        return value;
    }
    private static String text(Cursor rows, String key) {
        int index = rows.getColumnIndexOrThrow(key);
        if (rows.getType(index) != Cursor.FIELD_TYPE_STRING) invalid();
        return rows.getString(index);
    }
    private static void invalid() { throw new IllegalArgumentException("还款采集确认关联无效"); }
}
