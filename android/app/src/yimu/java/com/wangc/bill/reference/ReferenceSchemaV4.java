package com.wangc.bill.reference;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Local storage adapter for recovered Bill fields and the independent financial records. */
final class ReferenceSchemaV4 {
    private ReferenceSchemaV4() {}

    static void install(SQLiteDatabase db) {
        // A refunded original Bill may save cost=0. Legacy input still requires amount>0.
        // Rebuild under SQLiteOpenHelper's transaction, preserving PKs, self references,
        // candidate links, tag links and AUTOINCREMENT's deleted-row high-water mark.
        db.execSQL("PRAGMA defer_foreign_keys=ON");
        long previousSequence = 0;
        try (Cursor c = db.rawQuery("SELECT seq FROM sqlite_sequence WHERE name='transactions'", null)) {
            if (c.moveToFirst()) previousSequence = c.getLong(0);
        }
        List<String> columns = new ArrayList<>();
        try (Cursor c = db.rawQuery("PRAGMA table_info(transactions)", null)) {
            while (c.moveToNext()) columns.add(c.getString(c.getColumnIndexOrThrow("name")));
        }
        db.execSQL("CREATE TEMP TABLE reference_v4_transaction_tags AS SELECT transaction_id,tag_id FROM transaction_tags");
        db.execSQL("DROP TABLE transaction_tags");
        db.execSQL("CREATE TABLE reference_v4_transactions("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,book_id INTEGER NOT NULL REFERENCES books(id),"
                + "kind TEXT NOT NULL CHECK(kind IN ('expense','income','transfer')),"
                + "amount INTEGER NOT NULL CHECK(amount>0 OR(amount=0 AND bill_origin='original')),"
                + "currency TEXT NOT NULL,category_id INTEGER REFERENCES categories(id),"
                + "account_id INTEGER REFERENCES accounts(id),to_account_id INTEGER REFERENCES accounts(id),"
                + "remark TEXT NOT NULL DEFAULT '',merchant TEXT NOT NULL DEFAULT '',origin TEXT NOT NULL DEFAULT '',"
                + "asset_text TEXT NOT NULL DEFAULT '',discount INTEGER NOT NULL DEFAULT 0 CHECK(discount>=0),"
                + "fee INTEGER NOT NULL DEFAULT 0 CHECK(fee>=0),time INTEGER NOT NULL,created INTEGER NOT NULL,"
                + "not_total INTEGER NOT NULL DEFAULT 0,not_budget INTEGER NOT NULL DEFAULT 0,"
                + "deleted INTEGER NOT NULL DEFAULT 0,financial_type TEXT NOT NULL DEFAULT '',"
                + "settled INTEGER NOT NULL DEFAULT 0,attachments TEXT NOT NULL DEFAULT '[]',"
                + "candidate_id TEXT UNIQUE REFERENCES candidates(id),"
                + "related_id INTEGER REFERENCES reference_v4_transactions(id),import_key TEXT,"
                + "parent_semantic_id INTEGER,child_semantic_id INTEGER,owner_semantic_id INTEGER,"
                + "raw_asset_semantic_id INTEGER,raw_to_asset_semantic_id INTEGER,"
                + "bill_origin TEXT NOT NULL DEFAULT 'legacy')");
        String names = String.join(",", columns);
        db.execSQL("INSERT INTO reference_v4_transactions(" + names + ") SELECT " + names + " FROM transactions");
        db.execSQL("DROP TABLE transactions");
        db.execSQL("ALTER TABLE reference_v4_transactions RENAME TO transactions");
        db.execSQL("CREATE INDEX transactions_book_time ON transactions(book_id,deleted,time)");
        db.execSQL("CREATE UNIQUE INDEX transactions_import_key ON transactions(import_key) WHERE import_key IS NOT NULL");
        db.execSQL("INSERT INTO sqlite_sequence(name,seq) SELECT 'transactions',? WHERE NOT EXISTS(SELECT 1 FROM sqlite_sequence WHERE name='transactions')", new Object[]{previousSequence});
        db.execSQL("UPDATE sqlite_sequence SET seq=MAX(seq,?) WHERE name='transactions'", new Object[]{previousSequence});
        db.execSQL("CREATE TABLE transaction_tags(transaction_id INTEGER REFERENCES transactions(id) ON DELETE CASCADE,tag_id INTEGER REFERENCES tags(id),PRIMARY KEY(transaction_id,tag_id))");
        db.execSQL("INSERT INTO transaction_tags(transaction_id,tag_id) SELECT transaction_id,tag_id FROM reference_v4_transaction_tags");
        db.execSQL("DROP TABLE reference_v4_transaction_tags");
        OriginalBillRepository.installV4Schema(db);
        AssetHistoryRepository.installV4Schema(db);
        OriginalRefundRepository.installV4Schema(db);
        OriginalReimbursementRepository.installV4Schema(db);
    }

    /** A v1/v2/v3 file cannot promote a legacy row to an original Bill using extra keys. */
    static boolean acceptsBackupColumn(int version, String table, String column) {
        if (version < 4 && table.equals("transactions")
                && (column.equals("bill_origin") || Arrays.asList(OriginalBillRepository.CANONICAL_COLUMNS).contains(column))) return false;
        return ReferenceSchemaV3.acceptsOldColumn(version, table, column);
    }

    static void validateRestored(ReferenceStore store) {
        new OriginalBillRepository(store).validateRestored();
        new AssetHistoryRepository(store).validateRestored();
        new OriginalRefundRepository(store).validateRestored();
        new OriginalReimbursementRepository(store).validateRestored();
    }
}
