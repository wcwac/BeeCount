package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real v6 parent-table migration, independent Transfer receipts, and whole-v7 restore rollback. */
@RunWith(AndroidJUnit4.class)
public final class ReferenceSchemaV7DeviceTest {
    private static final String[] V6_TABLES = {"books", "accounts", "categories", "tags", "candidates", "transactions",
            "transaction_tags", "rules", "plans", "goals", "category_hidden_books", "category_accounts", "asset_show_books",
            "asset_histories", "original_refunds", "original_refund_items", "original_reimbursements",
            "original_reimbursement_items", "original_bill_related", "original_instalments", "original_curd_histories",
            "original_transfers", "original_transfer_tags"};
    private static final String IMPORT_KEY = "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc";
    private Context context;
    private String database;
    private ReferenceStore store;
    private final List<String> extraDatabases = new ArrayList<>();

    @Before public void prepare() {
        context = ApplicationProvider.getApplicationContext(); database = "reference-v7-" + UUID.randomUUID() + ".sqlite";
    }
    @After public void close() {
        if (store != null) store.close();
        if (context != null) {
            if (database != null) context.deleteDatabase(database);
            for (String name : extraDatabases) context.deleteDatabase(name);
        }
    }
    private void openLatest() { store = new ReferenceStore(context, database); store.getWritableDatabase(); }

    @Test public void realVersionSixMigrationPreservesCandidateParentReferencesAndRollsBackInvalidForeignKeys() {
        createVersionSixGraph(database); openLatest(); assertEquals(7, store.getReadableDatabase().getVersion()); assertOldGraph();
        assertEquals(1, scalar(store.getReadableDatabase(), "PRAGMA foreign_keys"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM candidate_transfer_receipts"));
        try (Cursor fks = store.getReadableDatabase().rawQuery("PRAGMA foreign_key_list(transactions)", null)) {
            boolean candidateParent = false;
            while (fks.moveToNext()) if ("candidate_id".equals(fks.getString(fks.getColumnIndexOrThrow("from")))) {
                assertEquals("candidates", fks.getString(fks.getColumnIndexOrThrow("table"))); candidateParent = true;
            }
            assertTrue(candidateParent);
        }
        expectSqlFailure(() -> store.getWritableDatabase().delete("candidates", "id='v6-saved'", null));
        assertNotNull(store.candidate("v6-saved")); assertOldGraph();
        ContentValues next = projection(1); assertTrue(store.getWritableDatabase().insertOrThrow("transactions", null, next) > 900);
        OriginalTransferRecord nextTransfer = new OriginalTransferRecord(); nextTransfer.transferId = 902; nextTransfer.userId = 7;
        new OriginalTransferRepository(store).saveOriginalRaw(nextTransfer); assertTrue(nextTransfer.localRowId > 7000);
        ContentValues duplicate = projection(1); duplicate.put("import_key", IMPORT_KEY);
        expectSqlFailure(() -> store.getWritableDatabase().insertOrThrow("transactions", null, duplicate));

        String corrupt = "reference-v7-invalid-v6-" + UUID.randomUUID() + ".sqlite"; extraDatabases.add(corrupt);
        createVersionSixGraph(corrupt);
        try (VersionSix old = new VersionSix(context, corrupt)) {
            SQLiteDatabase db = old.getWritableDatabase(); db.setForeignKeyConstraintsEnabled(false);
            db.execSQL("UPDATE candidates SET book_id=999999 WHERE id='v6-saved'");
        }
        try (ReferenceStore invalid = new ReferenceStore(context, corrupt)) {
            try { invalid.getWritableDatabase(); fail("A broken foreign key committed schema7"); }
            catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
        }
        try (SQLiteDatabase original = SQLiteDatabase.openDatabase(context.getDatabasePath(corrupt).getPath(), null, SQLiteDatabase.OPEN_READONLY)) {
            assertEquals(6, original.getVersion());
            assertEquals(0, scalar(original, "SELECT COUNT(*) FROM sqlite_master WHERE name='candidate_transfer_receipts'"));
            assertEquals(0, scalar(original, "SELECT COUNT(*) FROM sqlite_master WHERE name='candidates_v7'"));
            assertEquals(123, scalar(original, "SELECT transaction_id FROM candidates WHERE id='v6-saved'"));
            assertEquals(999999, scalar(original, "SELECT book_id FROM candidates WHERE id='v6-saved'"));
        }
        assertRealVersionThreeSelfReferenceUpgrade();
    }

    @Test public void freshVersionSevenRoundTripsTwentyFourTablesAndReservesDeletedTransferAnchorsAcrossDatabases() {
        openLatest(); assertEquals(7, store.getReadableDatabase().getVersion());
        assertEquals(1, scalar(store.getReadableDatabase(), "PRAGMA foreign_keys"));
        OriginalTransferRecord live = new OriginalTransferRecord(); live.userId = 7; live.transferId = Long.MAX_VALUE;
        new OriginalTransferRepository(store).saveOriginalRaw(live);
        ContentValues sameNumberBill = projection(500); sameNumberBill.put("id", live.localRowId);
        store.getWritableDatabase().insertOrThrow("transactions", null, sameNumberBill);
        addReceipt("live-transfer", live.localRowId, 7, Long.MAX_VALUE);
        store.getWritableDatabase().execSQL("INSERT INTO original_transfers(id,owner_semantic_id,transfer_semantic_id,cost) VALUES(9000,7,-5,-1.25)");
        addReceipt("deleted-transfer", 9000, 7, -5);
        OriginalTransferRepository repository = new OriginalTransferRepository(store);
        assertEquals(1, repository.deleteRaw(repository.byLocalRowId(9000)));
        assertEquals(9000, receipt("deleted-transfer").optLong("transfer_row_id"));
        assertTrue(receipt("deleted-transfer").isNull("resolved_transfer_row_id"));
        JSONObject backup = store.backup(); assertEquals(7, backup.optInt("version"));
        assertEquals(24, backup.optJSONObject("tables").length());
        store.close(); store = null; context.deleteDatabase(database); openLatest();
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfers")); store.restore(backup); assertTablesEqual(backup);
        assertTrue(store.candidate("live-transfer").isNull("transaction_id"));
        assertEquals(500, store.transaction(live.localRowId).optLong("amount"));
        assertTrue(store.transaction(live.localRowId).isNull("candidate_id"));
        repository = new OriginalTransferRepository(store);
        assertEquals(Long.MAX_VALUE, repository.byLocalRowId(live.localRowId).transferId);
        assertNull(repository.byLocalRowId(9000)); assertTrue(receipt("deleted-transfer").isNull("resolved_transfer_row_id"));
        for (String column : new String[]{"transfer_row_id", "owner_semantic_id", "transfer_semantic_id"}) {
            ContentValues changed = new ContentValues(); changed.put(column, 555L);
            expectSqlFailure(() -> store.getWritableDatabase().update("candidate_transfer_receipts", changed, "candidate_id='live-transfer'", null));
        }
        OriginalTransferRecord later = new OriginalTransferRecord(); later.userId = 7; later.transferId = 44;
        repository.saveOriginalRaw(later); assertTrue("Deleted receipt anchor was reused", later.localRowId > 9000);
        store.getWritableDatabase().delete("candidates", "id='live-transfer'", null);
        assertNull(receipt("live-transfer")); assertNotNull(repository.byLocalRowId(live.localRowId)); assertForeignKeysClean();
    }

    @Test public void aRealVersionSixBackupIgnoresForgedReceiptTablesAndClearsOnlyTheNewReceiptDomain() {
        JSONObject old = createVersionSixGraph(database); openLatest(); addReceipt("new-transfer", 60, 7, 901);
        Json.put(old.optJSONObject("tables"), "candidate_transfer_receipts", new JSONArray().put(Json.object(
                "candidate_id", "forged", "transfer_row_id", "not an integer", "owner_semantic_id", 2147483648L)));
        store.restore(old); assertOldGraph(); assertNull(store.candidate("new-transfer"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM candidate_transfer_receipts"));
        assertEquals(901, new OriginalTransferRepository(store).byLocalRowId(60).transferId); assertForeignKeysClean();
        JSONObject valid = store.backup(), forgedKind = Json.copy(old);
        Json.put(forgedKind.optJSONObject("tables").optJSONArray("candidates").optJSONObject(0), "save_kind", "transfer");
        expectInvalid(forgedKind); assertTablesEqual(valid); assertOldGraph();
    }

    @Test public void missingReceiptTablesAndInvalidIdentityCandidateOrPayloadFieldsRollBackEveryTable() {
        createVersionSixGraph(database); openLatest(); addReceipt("live-transfer", 60, 7, 901);
        JSONObject valid = store.backup(), missing = changedGraph(valid);
        missing.optJSONObject("tables").remove("candidate_transfer_receipts"); expectInvalid(missing); assertTablesEqual(valid);
        Object[][] defects = {{"owner_semantic_id", 2147483648L}, {"owner_semantic_id", "7"},
                {"transfer_semantic_id", 1.5d}, {"transfer_row_id", 0}, {"transfer_row_id", 60.5d},
                {"resolved_transfer_row_id", 61}, {"candidate_id", "missing-candidate"}};
        for (Object[] defect : defects) {
            JSONObject bad = changedGraph(valid);
            Json.put(bad.optJSONObject("tables").optJSONArray("candidate_transfer_receipts").optJSONObject(0), (String) defect[0], defect[1]);
            expectInvalid(bad); assertTablesEqual(valid); assertOldGraph();
        }
        for (Object[] defect : new Object[][]{{"state", "PENDING"}, {"save_kind", "bill"}, {"transaction_id", 0}, {"refund_owner_semantic_id", 7}}) {
            JSONObject bad = changedGraph(valid); Json.put(candidateRow(bad, "live-transfer"), (String) defect[0], defect[1]);
            expectInvalid(bad); assertTablesEqual(valid); assertOldGraph();
        }
        for (String key : new String[]{"transfer_row_id", "transfer_owner_semantic_id", "transfer_semantic_id"}) {
            JSONObject bad = changedGraph(valid), candidate = candidateRow(bad, "live-transfer");
            JSONObject payload = Json.parse(candidate.optString("payload")); Json.put(payload, key, 999L);
            Json.put(candidate, "payload", payload.toString()); expectInvalid(bad); assertTablesEqual(valid);
        }
        // Both SQL foreign keys are legal, but a Transfer receipt cannot double as
        // the reverse candidate link of a Bill in the independent transaction table.
        SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
        try {
            ContentValues reverseLink = new ContentValues(); reverseLink.put("candidate_id", "live-transfer");
            assertEquals(1, db.update("transactions", reverseLink, "id=124", null));
            assertTrue(store.candidate("live-transfer").isNull("transaction_id"));
            ReferenceSchemaV7.checkForeignKeys(db);
            try { ReferenceSchemaV7.validateRestored(store); fail("A Transfer receipt also accepted a reverse Bill association"); }
            catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
        } finally { db.endTransaction(); }
        assertTablesEqual(valid); assertTrue(store.transaction(124).isNull("candidate_id"));
        JSONObject crossLinked = changedGraph(valid), wrongBill = null;
        JSONArray bills = crossLinked.optJSONObject("tables").optJSONArray("transactions");
        for (int i = 0; i < bills.length(); i++) if (bills.optJSONObject(i).optLong("id") == 124) wrongBill = bills.optJSONObject(i);
        assertNotNull(wrongBill); Json.put(wrongBill, "candidate_id", "live-transfer");
        expectInvalid(crossLinked); assertTablesEqual(valid);
        assertOldGraph(); assertForeignKeysClean();
    }

    @Test public void deletedAnchorsCannotResolveToMissingOrReusedRowsAndFailedRestorePreservesTheirSequence() {
        createVersionSixGraph(database); openLatest();
        store.getWritableDatabase().execSQL("INSERT INTO original_transfers(id,owner_semantic_id,transfer_semantic_id) VALUES(9000,7,902)");
        addReceipt("deleted-transfer", 9000, 7, 902); OriginalTransferRepository repository = new OriginalTransferRepository(store);
        repository.deleteRaw(repository.byLocalRowId(9000)); JSONObject valid = store.backup();
        long sequence = scalar(store.getReadableDatabase(), "SELECT seq FROM sqlite_sequence WHERE name='original_transfers'");
        JSONObject missingResolution = changedGraph(valid);
        Json.put(missingResolution.optJSONObject("tables").optJSONArray("candidate_transfer_receipts").optJSONObject(0), "resolved_transfer_row_id", 9000);
        expectInvalid(missingResolution); assertTablesEqual(valid);
        JSONObject detachedLive = changedGraph(valid), detachedReceipt = detachedLive.optJSONObject("tables").optJSONArray("candidate_transfer_receipts").optJSONObject(0);
        Json.put(detachedReceipt, "transfer_row_id", 60); expectInvalid(detachedLive); assertTablesEqual(valid);
        JSONObject reused = changedGraph(valid), replacement = Json.copy(reused.optJSONObject("tables").optJSONArray("original_transfers").optJSONObject(0));
        Json.put(replacement, "id", 9000); Json.put(replacement, "owner_semantic_id", 8); Json.put(replacement, "transfer_semantic_id", 999);
        reused.optJSONObject("tables").optJSONArray("original_transfers").put(replacement);
        expectInvalid(reused); assertTablesEqual(valid);
        JSONObject duplicate = changedGraph(valid), other = Json.copy(candidateRow(duplicate, "deleted-transfer")); Json.put(other, "id", "second-candidate");
        duplicate.optJSONObject("tables").optJSONArray("candidates").put(other);
        JSONObject second = Json.copy(duplicate.optJSONObject("tables").optJSONArray("candidate_transfer_receipts").optJSONObject(0)); Json.put(second, "candidate_id", "second-candidate");
        duplicate.optJSONObject("tables").optJSONArray("candidate_transfer_receipts").put(second);
        expectInvalid(duplicate); assertTablesEqual(valid);
        assertEquals(sequence, scalar(store.getReadableDatabase(), "SELECT seq FROM sqlite_sequence WHERE name='original_transfers'"));
        assertOldGraph(); assertForeignKeysClean();
    }

    private void addReceipt(String id, long anchor, int owner, long semantic) {
        JSONObject payload = Json.object("book_id", 1, "kind", "transfer", "currency", "CNY", "amount", 125,
                "time", 1735689600000L, "financial_type", "original_transfer", "transfer_row_id", anchor,
                "transfer_owner_semantic_id", owner, "transfer_semantic_id", semantic);
        ContentValues candidate = new ContentValues(); candidate.put("id", id); candidate.put("payload", payload.toString());
        candidate.put("package_name", "synthetic.v7"); candidate.put("book_id", 1); candidate.put("created", 1735689600000L);
        candidate.put("state", "SAVED"); candidate.put("save_kind", "transfer"); candidate.putNull("transaction_id");
        ContentValues receipt = new ContentValues(); receipt.put("candidate_id", id); receipt.put("transfer_row_id", anchor);
        receipt.put("resolved_transfer_row_id", anchor); receipt.put("owner_semantic_id", owner); receipt.put("transfer_semantic_id", semantic);
        SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
        try {
            db.insertOrThrow("candidates", null, candidate); db.insertOrThrow("candidate_transfer_receipts", null, receipt);
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }
    private JSONObject receipt(String id) { return store.one("SELECT * FROM candidate_transfer_receipts WHERE candidate_id=?", id); }
    private static JSONObject candidateRow(JSONObject backup, String id) {
        JSONArray candidates = backup.optJSONObject("tables").optJSONArray("candidates");
        for (int i = 0; i < candidates.length(); i++) if (id.equals(candidates.optJSONObject(i).optString("id"))) return candidates.optJSONObject(i);
        throw new AssertionError("Synthetic candidate is missing");
    }
    private JSONObject changedGraph(JSONObject source) {
        JSONObject bad = Json.copy(source), tables = bad.optJSONObject("tables");
        Json.put(tables.optJSONArray("books").optJSONObject(0), "name", "不应留下的部分恢复");
        Json.put(tables.optJSONArray("transactions").optJSONObject(0), "remark", "不应留下的部分恢复"); return bad;
    }
    private void expectInvalid(JSONObject backup) {
        try { store.restore(backup); fail("Invalid Transfer receipt was accepted"); }
        catch (IllegalArgumentException | android.database.SQLException expected) { assertNotNull(expected.getMessage()); }
    }
    private static void expectSqlFailure(Runnable operation) {
        try { operation.run(); fail("A protected SQL relation changed"); }
        catch (android.database.SQLException expected) { assertNotNull(expected.getMessage()); }
    }
    private void assertTablesEqual(JSONObject expected) {
        JSONObject wanted = expected.optJSONObject("tables"), actual = store.backup().optJSONObject("tables");
        assertEquals(24, wanted.length()); assertEquals(24, actual.length());
        for (java.util.Iterator<String> keys = wanted.keys(); keys.hasNext();) {
            String table = keys.next(); assertEquals(table, wanted.optJSONArray(table).toString(), actual.optJSONArray(table).toString());
        }
    }
    private void assertOldGraph() {
        assertEquals("SAVED", store.candidate("v6-saved").optString("state"));
        assertEquals("bill", store.candidate("v6-saved").optString("save_kind"));
        assertEquals(123, store.candidate("v6-saved").optLong("transaction_id"));
        assertEquals("v6-saved", store.transaction(123).optString("candidate_id"));
        assertEquals(123, store.transaction(124).optLong("related_id"));
        assertEquals(IMPORT_KEY, store.transaction(123).optString("import_key"));
        assertEquals(734, store.accountBalance(42));
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transaction_tags WHERE transaction_id=123 AND tag_id=77"));
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM rules WHERE id=4 AND account_id=42"));
        OriginalTransferRecord original = new OriginalTransferRepository(store).byLocalRowId(60);
        assertEquals(901, original.transferId); assertEquals(7, original.userId); assertEquals(-2.5, original.cost, 0);
        assertEquals(java.util.Arrays.asList(4L, 4L, null), original.tags); assertForeignKeysClean();
    }
    private void assertForeignKeysClean() { ReferenceSchemaV7.checkForeignKeys(store.getReadableDatabase()); }
    private static ContentValues projection(long amount) {
        ContentValues row = new ContentValues(); row.put("book_id", 1); row.put("kind", "expense"); row.put("amount", amount);
        row.put("currency", "CNY"); row.put("time", 1735689600000L); row.put("created", 1735689600000L); return row;
    }
    private void assertRealVersionThreeSelfReferenceUpgrade() {
        String oldName = "reference-v7-self-v3-" + UUID.randomUUID() + ".sqlite"; extraDatabases.add(oldName);
        try (ReferenceSchemaV4DeviceTest.VersionThree old = new ReferenceSchemaV4DeviceTest.VersionThree(context, oldName)) {
            SQLiteDatabase db = old.getWritableDatabase(); assertEquals(3, db.getVersion());
            JSONObject payload = Json.object("book_id", 1, "kind", "expense", "amount", 500, "currency", "CNY", "time", 1735689600000L);
            db.execSQL("INSERT INTO candidates(id,payload,package_name,book_id,created,state,transaction_id) VALUES('v3-self-parent',?,'synthetic.v3',1,1,'SAVED',321)", new Object[]{payload.toString()});
            ContentValues parent = projection(500); parent.put("id", 321); parent.put("candidate_id", "v3-self-parent"); db.insertOrThrow("transactions", null, parent);
            ContentValues related = projection(100); related.put("id", 322); related.put("kind", "income"); related.put("related_id", 321); db.insertOrThrow("transactions", null, related);
            db.execSQL("INSERT INTO tags(id,name) VALUES(88,'v3自关联保留标签')");
            db.execSQL("INSERT INTO transaction_tags(transaction_id,tag_id) VALUES(321,88)");
            ContentValues high = projection(1); high.put("id", 999); db.insertOrThrow("transactions", null, high); db.execSQL("DELETE FROM transactions WHERE id=999");
        }
        try (ReferenceStore migrated = new ReferenceStore(context, oldName)) {
            SQLiteDatabase db = migrated.getWritableDatabase(); assertEquals(7, db.getVersion());
            assertEquals(321, migrated.transaction(322).optLong("related_id"));
            assertEquals("v3-self-parent", migrated.transaction(321).optString("candidate_id"));
            assertEquals(321, migrated.candidate("v3-self-parent").optLong("transaction_id"));
            assertEquals(1, migrated.scalar("SELECT COUNT(*) n FROM transaction_tags WHERE transaction_id=321 AND tag_id=88"));
            try (Cursor fks = db.rawQuery("PRAGMA foreign_key_list(transactions)", null)) {
                boolean self = false;
                while (fks.moveToNext()) if ("related_id".equals(fks.getString(fks.getColumnIndexOrThrow("from")))) {
                    assertEquals("transactions", fks.getString(fks.getColumnIndexOrThrow("table"))); self = true;
                }
                assertTrue("The rebuilt table lost its self reference", self);
            }
            ReferenceSchemaV7.checkForeignKeys(db);
            assertTrue(db.insertOrThrow("transactions", null, projection(1)) > 999);
        }
        // The cloud fixture checks the final FK and populated v3 migration. Its
        // API36 SQLite runtime does not by itself exercise Android26/27/28.
    }
    private JSONObject createVersionSixGraph(String name) {
        try (VersionSix old = new VersionSix(context, name)) {
            SQLiteDatabase db = old.getWritableDatabase(); assertEquals(6, db.getVersion());
            assertEquals(0, scalar(db, "SELECT COUNT(*) FROM sqlite_master WHERE name='candidate_transfer_receipts'"));
            long category = scalar(db, "SELECT id FROM categories WHERE level='parent' AND semantic_id=99");
            db.execSQL("INSERT INTO accounts(id,name,opening) VALUES(42,'v6旧账户',1234)");
            JSONObject payload = Json.object("book_id", 1, "kind", "expense", "currency", "CNY", "amount", 500, "category_id", category, "time", 1735689600000L);
            db.execSQL("INSERT INTO candidates(id,payload,package_name,book_id,created,state,transaction_id) VALUES('v6-saved',?,'synthetic.v6',1,1,'SAVED',123)", new Object[]{payload.toString()});
            ContentValues bill = projection(500); bill.put("id", 123); bill.put("category_id", category); bill.put("account_id", 42);
            bill.put("candidate_id", "v6-saved"); bill.put("import_key", IMPORT_KEY); db.insertOrThrow("transactions", null, bill);
            ContentValues related = projection(200); related.put("id", 124); related.put("kind", "income"); related.put("related_id", 123); db.insertOrThrow("transactions", null, related);
            db.execSQL("INSERT INTO tags(id,name) VALUES(77,'v6旧标签')"); db.execSQL("INSERT INTO transaction_tags(transaction_id,tag_id) VALUES(123,77)");
            db.execSQL("INSERT INTO rules(id,merchant,book_id,category_id,account_id) VALUES(4,'v6旧规则',1,?,42)", new Object[]{category});
            db.execSQL("INSERT INTO original_transfers(id,owner_semantic_id,transfer_semantic_id,cost) VALUES(60,7,901,-2.5)");
            db.execSQL("INSERT INTO original_transfer_tags(id,transfer_row_id,position,value) VALUES(70,60,0,4),(71,60,1,4),(72,60,2,NULL)");
            ContentValues high = projection(1); high.put("id", 900); db.insertOrThrow("transactions", null, high); db.execSQL("DELETE FROM transactions WHERE id=900");
            db.execSQL("INSERT INTO original_transfers(id) VALUES(7000)"); db.execSQL("DELETE FROM original_transfers WHERE id=7000");
            JSONObject tables = new JSONObject(); for (String table : V6_TABLES) Json.put(tables, table, rows(db, table));
            return Json.object("format", "YimuReferenceBackup", "version", 6, "created", 1735689600000L, "tables", tables);
        }
    }
    private static long scalar(SQLiteDatabase db, String sql) {
        try (Cursor rows = db.rawQuery(sql, null)) { assertTrue(rows.moveToFirst()); return rows.getLong(0); }
    }
    private static JSONArray rows(SQLiteDatabase db, String table) {
        JSONArray result = new JSONArray(); try (Cursor cursor = db.rawQuery("SELECT * FROM " + table + " ORDER BY rowid", null)) {
            while (cursor.moveToNext()) {
                JSONObject row = new JSONObject(); for (int i = 0; i < cursor.getColumnCount(); i++) Json.put(row, cursor.getColumnName(i), switch (cursor.getType(i)) {
                    case Cursor.FIELD_TYPE_NULL -> null; case Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(i);
                    case Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(i); default -> cursor.getString(i);
                }); result.put(row);
            }
        } return result;
    }
    private static final class VersionSix extends SQLiteOpenHelper {
        VersionSix(Context context, String name) { super(context, name, null, 6); }
        @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }
        @Override public void onCreate(SQLiteDatabase db) {
            ReferenceSchemaV4DeviceTest.VersionThree.createSchema(db);
            ReferenceSchemaV4.install(db); ReferenceSchemaV5.install(db); ReferenceSchemaV6.install(db);
        }
        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { throw new AssertionError("This fixture only creates the real version6 schema"); }
    }
}
