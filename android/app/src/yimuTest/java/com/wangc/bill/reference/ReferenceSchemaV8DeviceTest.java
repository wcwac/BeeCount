package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real v7 migration and v8 whole-backup checks; no temporary current-version schema mutation. */
@RunWith(AndroidJUnit4.class)
public final class ReferenceSchemaV8DeviceTest {
    private static final String[] V7_TABLES = {"books", "accounts", "categories", "tags", "candidates", "transactions",
            "transaction_tags", "rules", "plans", "goals", "category_hidden_books", "category_accounts", "asset_show_books",
            "asset_histories", "original_refunds", "original_refund_items", "original_reimbursements",
            "original_reimbursement_items", "original_bill_related", "original_instalments", "original_curd_histories",
            "original_transfers", "original_transfer_tags", "candidate_transfer_receipts"};
    private static final long TIME = 1735689600000L;
    private Context context;
    private String database;
    private ReferenceStore store;
    private final List<String> extraDatabases = new ArrayList<>();

    @Before public void prepare() {
        context = ApplicationProvider.getApplicationContext(); database = "reference-v8-" + UUID.randomUUID() + ".sqlite";
    }
    @After public void close() {
        if (store != null) store.close();
        if (context != null) {
            if (database != null) context.deleteDatabase(database);
            for (String name : extraDatabases) context.deleteDatabase(name);
        }
    }
    private void openLatest() { store = new ReferenceStore(context, database); store.getWritableDatabase(); }

    @Test public void realVersionSevenMigrationPreservesAllTwentyFourPopulatedTablesAndTheirParentReferences() {
        JSONObject old = createVersionSevenGraph(database); openLatest();
        assertEquals(11, store.getReadableDatabase().getVersion()); assertOldTablesEqual(old);
        for (String table : V7_TABLES) assertTrue("The v7 fixture must populate " + table,
                old.optJSONObject("tables").optJSONArray(table).length() > 0);
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_lends"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM candidate_lend_receipts"));
        assertEquals(1, scalar(store.getReadableDatabase(), "PRAGMA foreign_keys"));
        assertParentTarget("transactions", "candidate_id", "candidates");
        assertParentTarget("candidate_transfer_receipts", "candidate_id", "candidates");
        assertParentTarget("transactions", "related_id", "transactions");
        assertParentTarget("candidate_lend_receipts", "resolved_lend_row_id", "original_lends");
        assertTrue(new OriginalTransferRepository(store).byLocalRowId(60).tags.equals(Arrays.asList(4L, 4L, null)));
        assertTrue(store.transaction(123).isNull("bill_semantic_id")); assertEquals("legacy", store.transaction(123).optString("bill_origin"));
        assertEquals(123, store.transaction(124).optLong("related_id"));
        expectSqlFailure(() -> store.getWritableDatabase().delete("candidates", "id='v7-saved'", null));
        assertOldTablesEqual(old); assertForeignKeysClean();
        assertTrue(store.getWritableDatabase().insertOrThrow("transactions", null, projection(store.getWritableDatabase(), 1)) > 900);
        OriginalTransferRecord later = new OriginalTransferRecord(); new OriginalTransferRepository(store).saveOriginalRaw(later);
        assertTrue(later.localRowId > 7000);
    }

    @Test public void aBrokenRealVersionSevenForeignKeyRollsBackBothSchemaEightDdlAndUserVersion() {
        createVersionSevenGraph(database);
        try (VersionSeven old = new VersionSeven(context, database)) {
            SQLiteDatabase db = old.getWritableDatabase(); db.setForeignKeyConstraintsEnabled(false);
            db.execSQL("UPDATE candidates SET book_id=999999 WHERE id='v7-saved'");
        }
        try (ReferenceStore invalid = new ReferenceStore(context, database)) {
            try { invalid.getWritableDatabase(); fail("Broken v7 references committed schema8"); }
            catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
        }
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(context.getDatabasePath(database).getPath(), null, SQLiteDatabase.OPEN_READONLY)) {
            assertEquals(7, db.getVersion());
            for (String table : new String[]{"original_lends", "candidate_lend_receipts", "candidates_v8"})
                assertEquals(0, scalar(db, "SELECT COUNT(*) FROM sqlite_master WHERE name='" + table + "'"));
            assertEquals(999999, scalar(db, "SELECT book_id FROM candidates WHERE id='v7-saved'"));
            assertEquals(123, scalar(db, "SELECT transaction_id FROM candidates WHERE id='v7-saved'"));
            assertEquals(60, scalar(db, "SELECT resolved_transfer_row_id FROM candidate_transfer_receipts WHERE candidate_id='v7-transfer'"));
            assertEquals(1, scalar(db, "SELECT COUNT(*) FROM transaction_tags WHERE transaction_id=123 AND tag_id=77"));
        }
    }

    @Test public void freshVersionEightRoundTripsTwentySixTablesAndReservesDeletedLendAnchorsInANewDatabase() {
        openLatest(); assertEquals(11, store.getReadableDatabase().getVersion());
        OriginalLendRecord live = lend(Integer.MIN_VALUE, Integer.MAX_VALUE); addReceipt("live-lend", live);
        OriginalLendRecord zero = lend(0, 0); addReceipt("zero-lend", zero);
        OriginalLendRecord negative = lend(Integer.MAX_VALUE, Integer.MIN_VALUE); addReceipt("negative-lend", negative);
        ContentValues sameNumberBill = projection(store.getWritableDatabase(), 500); sameNumberBill.put("id", live.localRowId);
        store.getWritableDatabase().insertOrThrow("transactions", null, sameNumberBill);
        store.getWritableDatabase().execSQL("INSERT INTO original_transfers(id,owner_semantic_id,transfer_semantic_id) VALUES(?,7,901)", new Object[]{live.localRowId});
        store.getWritableDatabase().execSQL("INSERT INTO original_lends(id,owner_semantic_id,lend_semantic_id,number) VALUES(9000,7,-5,-1.25)");
        OriginalLendRecord deleted = new OriginalLendRepository(store).byLocalRowId(9000); addReceipt("deleted-lend", deleted);
        new OriginalLendRepository(store).deleteRaw(deleted);
        assertEquals(9000, receipt("deleted-lend").optLong("lend_row_id")); assertTrue(receipt("deleted-lend").isNull("resolved_lend_row_id"));
        JSONObject backup = store.backup(); assertEquals(11, backup.optInt("version")); assertEquals(37, backup.optJSONObject("tables").length());
        store.close(); store = null; context.deleteDatabase(database); openLatest();
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_lends")); store.restore(backup); assertTablesEqual(backup);
        assertEquals(500, store.transaction(live.localRowId).optLong("amount"));
        assertNotNull(new OriginalTransferRepository(store).byLocalRowId(live.localRowId));
        assertTrue(store.candidate("live-lend").isNull("transaction_id")); assertTrue(store.transaction(live.localRowId).isNull("candidate_id"));
        assertEquals(Integer.MAX_VALUE, new OriginalLendRepository(store).byLocalRowId(live.localRowId).lendId);
        assertEquals(0, new OriginalLendRepository(store).byLocalRowId(zero.localRowId).lendId);
        assertEquals(Integer.MIN_VALUE, new OriginalLendRepository(store).byLocalRowId(negative.localRowId).lendId);
        for (String column : new String[]{"candidate_id", "lend_row_id", "owner_semantic_id", "lend_semantic_id"}) {
            ContentValues changed = new ContentValues();
            if (column.equals("candidate_id")) changed.put(column, "zero-lend"); else changed.put(column, 555L);
            expectSqlFailure(() -> store.getWritableDatabase().update("candidate_lend_receipts", changed, "candidate_id='live-lend'", null));
        }
        OriginalLendRecord later = lend(7, 44); assertTrue("Deleted Lend anchor was reused", later.localRowId > 9000);
        JSONObject again = store.backup(); store.restore(again); assertTablesEqual(again);
        store.getWritableDatabase().delete("candidates", "id='live-lend'", null);
        assertNull(receipt("live-lend")); assertNotNull(new OriginalLendRepository(store).byLocalRowId(live.localRowId)); assertForeignKeysClean();
    }

    @Test public void aVersionSevenBackupIgnoresForgedLendTablesAndClearsOnlyTheNewDomain() {
        JSONObject old = createVersionSevenGraph(database); openLatest(); addReceipt("new-lend", lend(7, 44));
        Json.put(old.optJSONObject("tables"), "original_lends", new JSONArray().put(Json.object("id", "forged", "owner_semantic_id", "7")));
        Json.put(old.optJSONObject("tables"), "candidate_lend_receipts", new JSONArray().put(Json.object("candidate_id", "forged", "lend_row_id", 0)));
        store.restore(old); assertNull(store.candidate("new-lend"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_lends")); assertEquals(0, store.scalar("SELECT COUNT(*) n FROM candidate_lend_receipts"));
        assertOldTablesEqual(old); assertNotNull(store.one("SELECT * FROM candidate_transfer_receipts WHERE candidate_id='v7-transfer'"));
        JSONObject valid = store.backup(); store.restore(valid); assertTablesEqual(valid);
        JSONObject forgedKind = Json.copy(old); Json.put(candidateRow(forgedKind, "v7-transfer"), "save_kind", "lend");
        expectInvalid(forgedKind); assertTablesEqual(valid); assertForeignKeysClean();
    }

    @Test public void missingTablesOrMalformedRawLendFieldsRollBackAllTwentySixTables() {
        createVersionSevenGraph(database); openLatest(); addReceipt("live-lend", lend(7, 44)); JSONObject valid = restorableBackup();
        for (String table : new String[]{"original_lends", "candidate_lend_receipts"}) {
            JSONObject bad = changedGraph(valid); bad.optJSONObject("tables").remove(table); expectInvalid(bad); assertTablesEqual(valid);
        }
        Object[][] defects = {{"id", 0}, {"lend_semantic_id", 2147483648L}, {"owner_semantic_id", -2147483649L},
                {"type", 1.5d}, {"out_time", 1.5d}, {"general_service_bill", 2}, {"original_delete_flag", -1},
                {"remark", 42}, {"number", "2"}, {"repayment_asset_semantic_id", "0"}};
        for (Object[] defect : defects) {
            JSONObject bad = changedGraph(valid);
            Json.put(bad.optJSONObject("tables").optJSONArray("original_lends").optJSONObject(0), (String) defect[0], defect[1]);
            expectInvalid(bad); assertTablesEqual(valid);
        }
        JSONObject missingField = changedGraph(valid); missingField.optJSONObject("tables").optJSONArray("original_lends").optJSONObject(0).remove("remark");
        expectInvalid(missingField); assertTablesEqual(valid); assertForeignKeysClean();
    }

    @Test public void malformedReceiptCandidatePayloadAndCrossDomainLinksRollBackTheCompleteBackup() {
        createVersionSevenGraph(database); openLatest(); addReceipt("live-lend", lend(7, 44)); JSONObject valid = restorableBackup();
        Object[][] defects = {{"owner_semantic_id", 2147483648L}, {"owner_semantic_id", "7"}, {"lend_semantic_id", -2147483649L},
                {"lend_semantic_id", 1.5d}, {"lend_row_id", 0}, {"lend_row_id", 1.5d}, {"resolved_lend_row_id", 99999}, {"candidate_id", "missing"}};
        for (Object[] defect : defects) {
            JSONObject bad = changedGraph(valid);
            Json.put(bad.optJSONObject("tables").optJSONArray("candidate_lend_receipts").optJSONObject(0), (String) defect[0], defect[1]);
            expectInvalid(bad); assertTablesEqual(valid);
        }
        for (Object[] defect : new Object[][]{{"state", "PENDING"}, {"save_kind", "bill"}, {"transaction_id", 0},
                {"refund_owner_semantic_id", 7}, {"refund_semantic_id", 0}, {"refund_item_raw", ""}}) {
            JSONObject bad = changedGraph(valid); Json.put(candidateRow(bad, "live-lend"), (String) defect[0], defect[1]);
            expectInvalid(bad); assertTablesEqual(valid);
        }
        for (String key : new String[]{"lend_row_id", "lend_owner_semantic_id", "lend_semantic_id"}) {
            for (Object value : new Object[]{null, "7", 1.5d, 999L}) {
                JSONObject bad = changedGraph(valid), row = candidateRow(bad, "live-lend"), payload = Json.parse(row.optString("payload"));
                if (value == null) payload.remove(key); else Json.put(payload, key, value);
                Json.put(row, "payload", payload.toString()); expectInvalid(bad); assertTablesEqual(valid);
            }
        }
        for (Object[] defect : new Object[][]{{"kind", "expense"}, {"financial_type", "original_transfer"}}) {
            JSONObject bad = changedGraph(valid), row = candidateRow(bad, "live-lend"), payload = Json.parse(row.optString("payload"));
            Json.put(payload, (String) defect[0], defect[1]); Json.put(row, "payload", payload.toString()); expectInvalid(bad); assertTablesEqual(valid);
        }
        JSONObject crossBill = changedGraph(valid); Json.put(tableRow(crossBill, "transactions", "id", 124), "candidate_id", "live-lend");
        expectInvalid(crossBill); assertTablesEqual(valid);
        JSONObject crossTransfer = changedGraph(valid);
        JSONObject independentTransfer = Json.copy(crossTransfer.optJSONObject("tables").optJSONArray("original_transfers").optJSONObject(0));
        Json.put(independentTransfer, "id", 61); Json.put(independentTransfer, "transfer_semantic_id", 902);
        crossTransfer.optJSONObject("tables").optJSONArray("original_transfers").put(independentTransfer);
        JSONObject independentTag = Json.copy(crossTransfer.optJSONObject("tables").optJSONArray("original_transfer_tags").optJSONObject(0));
        Json.put(independentTag, "id", 80); Json.put(independentTag, "transfer_row_id", 61);
        crossTransfer.optJSONObject("tables").optJSONArray("original_transfer_tags").put(independentTag);
        crossTransfer.optJSONObject("tables").optJSONArray("candidate_transfer_receipts").put(Json.object("candidate_id", "live-lend",
                "transfer_row_id", 61, "resolved_transfer_row_id", 61, "owner_semantic_id", 7, "transfer_semantic_id", 902));
        expectInvalid(crossTransfer); assertTablesEqual(valid); assertForeignKeysClean();
        // The second receipt has its own legal Transfer anchor. Prove V8 itself
        // rejects the dual domain, rather than relying on a UNIQUE/FK or V7's gate.
        SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
        try {
            db.execSQL("INSERT INTO original_transfers(id,owner_semantic_id,transfer_semantic_id) VALUES(61,7,902)");
            db.execSQL("INSERT INTO original_transfer_tags(id,transfer_row_id,position,value) VALUES(80,61,0,4)");
            db.execSQL("INSERT INTO candidate_transfer_receipts(candidate_id,transfer_row_id,resolved_transfer_row_id,owner_semantic_id,transfer_semantic_id) VALUES('live-lend',61,61,7,902)");
            new OriginalTransferRepository(store).validateRestored(); ReferenceSchemaV7.checkForeignKeys(db);
            try { ReferenceSchemaV8.validateReceipt(store, store.candidate("live-lend")); fail("V8 accepted a candidate shared with the independent Transfer domain"); }
            catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
        } finally { db.endTransaction(); }
        assertTablesEqual(valid); assertForeignKeysClean();
    }

    @Test public void deletedAnchorsCannotDetachOrRebindAndFailedRestoresPreserveTheReservedSequence() {
        createVersionSevenGraph(database); openLatest(); addReceipt("live-lend", lend(7, 44));
        store.getWritableDatabase().execSQL("INSERT INTO original_lends(id,owner_semantic_id,lend_semantic_id) VALUES(9000,7,45)");
        OriginalLendRecord deleted = new OriginalLendRepository(store).byLocalRowId(9000); addReceipt("deleted-lend", deleted);
        new OriginalLendRepository(store).deleteRaw(deleted); JSONObject valid = restorableBackup();
        long sequence = scalar(store.getReadableDatabase(), "SELECT seq FROM sqlite_sequence WHERE name='original_lends'");
        JSONObject missing = changedGraph(valid); Json.put(tableRow(missing, "candidate_lend_receipts", "candidate_id", "deleted-lend"), "resolved_lend_row_id", 9000);
        expectInvalid(missing); assertTablesEqual(valid);
        JSONObject detached = changedGraph(valid); Json.put(tableRow(detached, "candidate_lend_receipts", "candidate_id", "live-lend"), "resolved_lend_row_id", null);
        expectInvalid(detached); assertTablesEqual(valid);
        JSONObject reused = changedGraph(valid), replacement = Json.copy(reused.optJSONObject("tables").optJSONArray("original_lends").optJSONObject(0));
        Json.put(replacement, "id", 9000); Json.put(replacement, "owner_semantic_id", 8); Json.put(replacement, "lend_semantic_id", 999);
        reused.optJSONObject("tables").optJSONArray("original_lends").put(replacement); expectInvalid(reused); assertTablesEqual(valid);
        JSONObject duplicate = changedGraph(valid), other = Json.copy(candidateRow(duplicate, "deleted-lend")); Json.put(other, "id", "second-lend");
        duplicate.optJSONObject("tables").optJSONArray("candidates").put(other);
        JSONObject second = Json.copy(tableRow(duplicate, "candidate_lend_receipts", "candidate_id", "deleted-lend")); Json.put(second, "candidate_id", "second-lend");
        duplicate.optJSONObject("tables").optJSONArray("candidate_lend_receipts").put(second); expectInvalid(duplicate); assertTablesEqual(valid);
        assertEquals(sequence, scalar(store.getReadableDatabase(), "SELECT seq FROM sqlite_sequence WHERE name='original_lends'")); assertForeignKeysClean();
    }

    @Test public void sourceAddStillReassignsASavedRowWhileTheHostReceiptRejectsItsIdentityDrift() {
        openLatest(); OriginalLendRecord value = lend(7, 0); addReceipt("fresh-lend", value); JSONObject valid = restorableBackup();
        long retainedRow = value.localRowId;
        Random sequence = new Random() { private int step; @Override public int nextInt(int bound) { return step++ == 0 ? 0 : 22; } };
        OriginalLendActions.Result result = new OriginalLendActions(store, 7, new OriginalLendRepository(store, sequence), () -> TIME + 1).add(value);
        assertTrue(result.save.saved); assertEquals(22, value.lendId); assertEquals(retainedRow, value.localRowId);
        assertEquals(22, new OriginalLendRepository(store).byLocalRowId(retainedRow).lendId);
        assertEquals(0, receipt("fresh-lend").optLong("lend_semantic_id"));
        try { ReferenceSchemaV8.validateReceipt(store, store.candidate("fresh-lend")); fail("The host rebound its immutable receipt after source d changed identity"); }
        catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
        try { ReferenceSchemaV8.validateRestored(store); fail("Live Lend identity drift was silently accepted"); }
        catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
        JSONObject drifted = store.backup(); store.restore(valid); assertTablesEqual(valid);
        expectInvalid(drifted); assertTablesEqual(valid); assertEquals(0, new OriginalLendRepository(store).byLocalRowId(retainedRow).lendId);
    }

    private OriginalLendRecord lend(int owner, int semantic) {
        OriginalLendRecord row = new OriginalLendRecord(); row.userId = owner; row.lendId = semantic;
        row.assetId = Long.MAX_VALUE; row.repaymentAssetId = Long.MIN_VALUE; row.billId = -2; row.type = 4;
        row.number = -10.25; row.fromCost = 3.75; row.interest = -.5; row.outTime = TIME; row.remark = "合成还款原始字段";
        new OriginalLendRepository(store).saveOriginalRaw(row); return row;
    }
    private void addReceipt(String id, OriginalLendRecord value) {
        SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
        try { addReceipt(db, id, value.localRowId, value.userId, value.lendId); db.setTransactionSuccessful(); }
        finally { db.endTransaction(); }
    }
    private static void addReceipt(SQLiteDatabase db, String id, long anchor, int owner, int semantic) {
        JSONObject payload = Json.object("book_id", 1, "kind", "transfer", "currency", "CNY", "amount", 125, "time", TIME,
                "financial_type", "original_lend", "lend_row_id", anchor, "lend_owner_semantic_id", owner, "lend_semantic_id", semantic);
        ContentValues candidate = new ContentValues(); candidate.put("id", id); candidate.put("payload", payload.toString());
        candidate.put("package_name", "synthetic.v8"); candidate.put("book_id", 1); candidate.put("created", TIME);
        candidate.put("state", "SAVED"); candidate.put("save_kind", "lend"); candidate.putNull("transaction_id"); db.insertOrThrow("candidates", null, candidate);
        ContentValues receipt = new ContentValues(); receipt.put("candidate_id", id); receipt.put("lend_row_id", anchor);
        receipt.put("resolved_lend_row_id", anchor); receipt.put("owner_semantic_id", owner); receipt.put("lend_semantic_id", semantic);
        db.insertOrThrow("candidate_lend_receipts", null, receipt);
    }
    private JSONObject receipt(String id) { return store.one("SELECT * FROM candidate_lend_receipts WHERE candidate_id=?", id); }
    private JSONObject restorableBackup() { JSONObject valid = store.backup(); store.restore(valid); assertTablesEqual(valid); return valid; }
    private static JSONObject candidateRow(JSONObject backup, String id) { return tableRow(backup, "candidates", "id", id); }
    private static JSONObject tableRow(JSONObject backup, String table, String key, Object value) {
        JSONArray rows = backup.optJSONObject("tables").optJSONArray(table);
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.optJSONObject(i);
            if (value instanceof Number ? row.optLong(key) == ((Number) value).longValue() : value.equals(row.optString(key))) return row;
        }
        throw new AssertionError("Synthetic row is missing: " + table);
    }
    private static JSONObject changedGraph(JSONObject source) {
        JSONObject changed = Json.copy(source);
        Json.put(changed.optJSONObject("tables").optJSONArray("books").optJSONObject(0), "name", "不应留下的部分恢复");
        Json.put(changed.optJSONObject("tables").optJSONArray("transactions").optJSONObject(0), "remark", "不应留下的部分恢复"); return changed;
    }
    private void expectInvalid(JSONObject backup) {
        try { store.restore(backup); fail("Invalid Lend backup was accepted"); }
        catch (IllegalArgumentException | android.database.SQLException expected) { assertNotNull(expected.getMessage()); }
    }
    private static void expectSqlFailure(Runnable operation) {
        try { operation.run(); fail("A protected SQL relation changed"); }
        catch (android.database.SQLException expected) { assertNotNull(expected.getMessage()); }
    }
    private void assertOldTablesEqual(JSONObject old) {
        JSONObject actual = store.backup().optJSONObject("tables"), wanted = old.optJSONObject("tables");
        for (String table : V7_TABLES) {
            JSONArray rows=actual.optJSONArray(table);
            if(table.equals("candidates"))for(int i=0;i<rows.length();i++)for(String column:new String[]{"capture_key","evidence_kind","capture_window_key"}){
                assertTrue(column,rows.optJSONObject(i).isNull(column));rows.optJSONObject(i).remove(column);
            }
            if(table.equals("transactions"))for(int i=0;i<rows.length();i++){
                assertTrue(rows.optJSONObject(i).isNull("manual_operation_id"));rows.optJSONObject(i).remove("manual_operation_id");
            }
            assertEquals(table,wanted.optJSONArray(table).toString(),rows.toString());
        }
    }
    private void assertTablesEqual(JSONObject expected) {
        JSONObject actual = store.backup().optJSONObject("tables"), wanted = expected.optJSONObject("tables");
        assertEquals(37, wanted.length()); assertEquals(37, actual.length());
        for (java.util.Iterator<String> keys = wanted.keys(); keys.hasNext();) {
            String table = keys.next(); assertEquals(table, wanted.optJSONArray(table).toString(), actual.optJSONArray(table).toString());
        }
    }
    private void assertParentTarget(String table, String column, String parent) {
        boolean found = false;
        try (Cursor rows = store.getReadableDatabase().rawQuery("PRAGMA foreign_key_list(" + table + ")", null)) {
            while (rows.moveToNext()) if (column.equals(rows.getString(rows.getColumnIndexOrThrow("from")))) {
                assertEquals(parent, rows.getString(rows.getColumnIndexOrThrow("table"))); found = true;
            }
        }
        assertTrue("Missing foreign key " + table + "." + column, found);
    }
    private void assertForeignKeysClean() { ReferenceSchemaV7.checkForeignKeys(store.getReadableDatabase()); }
    private static long scalar(SQLiteDatabase db, String sql) {
        try (Cursor rows = db.rawQuery(sql, null)) { assertTrue(rows.moveToFirst()); return rows.getLong(0); }
    }
    private static ContentValues projection(SQLiteDatabase db, long amount) {
        long category = scalar(db, "SELECT id FROM categories WHERE level='parent' AND semantic_id=99 ORDER BY id LIMIT 1"); assertTrue(category > 0);
        ContentValues row = new ContentValues(); row.put("book_id", 1); row.put("kind", "expense"); row.put("amount", amount);
        row.put("currency", "CNY"); row.put("category_id", category); row.put("parent_semantic_id", 99); row.put("child_semantic_id", -1);
        row.put("time", TIME); row.put("created", TIME); return row;
    }
    private JSONObject createVersionSevenGraph(String name) {
        try (VersionSeven old = new VersionSeven(context, name)) {
            SQLiteDatabase db = old.getWritableDatabase(); assertEquals(7, db.getVersion());
            assertEquals(0, scalar(db, "SELECT COUNT(*) FROM sqlite_master WHERE name='original_lends'"));
            long category = scalar(db, "SELECT id FROM categories WHERE level='parent' AND semantic_id=99");
            db.execSQL("INSERT INTO accounts(id,name,opening) VALUES(42,'v7旧账户',1234)");
            db.execSQL("INSERT INTO accounts(id,name,asset_origin,owner_semantic_id,semantic_id,asset_type,asset_number) VALUES(43,'v7原字段账户','original',7,101,1,25.5)");
            JSONObject payload = Json.object("book_id", 1, "kind", "expense", "currency", "CNY", "amount", 500, "category_id", category, "time", TIME);
            db.execSQL("INSERT INTO candidates(id,payload,package_name,book_id,created,state,transaction_id) VALUES('v7-saved',?,'synthetic.v7',1,1,'SAVED',123)", new Object[]{payload.toString()});
            ContentValues bill = projection(db, 500); bill.put("id", 123); bill.put("account_id", 42); bill.put("candidate_id", "v7-saved");
            bill.put("import_key", "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"); db.insertOrThrow("transactions", null, bill);
            ContentValues related = projection(db, 200); related.put("id", 124); related.put("related_id", 123); db.insertOrThrow("transactions", null, related);
            db.execSQL("INSERT INTO tags(id,name) VALUES(77,'v7旧标签')"); db.execSQL("INSERT INTO transaction_tags(transaction_id,tag_id) VALUES(123,77)");
            db.execSQL("INSERT INTO rules(id,merchant,book_id,category_id,account_id) VALUES(4,'v7旧规则',1,?,42)", new Object[]{category});
            db.execSQL("INSERT INTO plans(id,name,interval,payload,next_due) VALUES(5,'v7旧计划','monthly',?,?)", new Object[]{payload.toString(), TIME});
            db.execSQL("INSERT INTO goals(id,name,target,saved,created) VALUES(6,'v7旧目标',1000,30,?)", new Object[]{TIME});
            db.execSQL("INSERT INTO category_hidden_books(category_row_id,book_row_id) VALUES(?,1)", new Object[]{category});
            db.execSQL("INSERT INTO category_accounts(parent_semantic_id,child_semantic_id,account_row_id,updated) VALUES(99,-1,42,?)", new Object[]{TIME});
            db.execSQL("INSERT INTO asset_show_books(account_row_id,position,book_semantic_id) VALUES(43,0,1)");
            db.execSQL("INSERT INTO asset_histories(id,semantic_id,asset_semantic_id,change_content,change_number,current_number,time,update_time,owner_semantic_id) VALUES(51,301,101,'v7资产历史',1.5,25.5,?,?,7)", new Object[]{TIME, TIME});
            db.execSQL("INSERT INTO original_refunds(id,owner_semantic_id,refund_semantic_id,parent_bill_semantic_id,infos_is_null) VALUES(52,7,302,999,0)");
            db.execSQL("INSERT INTO original_refund_items(refund_row_id,position,raw_json) VALUES(52,0,'{\"opaque\":true}')");
            db.execSQL("INSERT INTO original_reimbursements(id,owner_semantic_id,reimbursement_semantic_id,parent_bill_semantic_id,entries_is_null) VALUES(53,7,303,999,0)");
            db.execSQL("INSERT INTO original_reimbursement_items(reimbursement_row_id,position,raw_info) VALUES(53,0,'v7:保留:原字符串')");
            db.execSQL("INSERT INTO original_bill_related(id,owner_semantic_id,related_semantic_id,one_bill_semantic_id,two_bill_semantic_id) VALUES(54,7,304,998,999)");
            db.execSQL("INSERT INTO original_instalments(id,asset_semantic_id,parent_bill_semantic_id,in_asset_time,instalment_semantic_id,instalment_type,periods,remainder_type,service_number,service_type,total_number,update_time,owner_semantic_id,original_delete_flag) VALUES(55,101,999,0,305,0,0,0,0,0,0,0,7,0)");
            db.execSQL("INSERT INTO original_curd_histories(id,owner_semantic_id,entity_type,entity_semantic_id,action_type) VALUES(56,7,16,306,1)");
            db.execSQL("INSERT INTO original_transfers(id,owner_semantic_id,transfer_semantic_id,cost) VALUES(60,7,901,-2.5)");
            db.execSQL("INSERT INTO original_transfer_tags(id,transfer_row_id,position,value) VALUES(70,60,0,4),(71,60,1,4),(72,60,2,NULL)");
            JSONObject transferPayload = Json.object("book_id", 1, "kind", "transfer", "currency", "CNY", "amount", 125, "time", TIME,
                    "financial_type", "original_transfer", "transfer_row_id", 60, "transfer_owner_semantic_id", 7, "transfer_semantic_id", 901);
            db.execSQL("INSERT INTO candidates(id,payload,package_name,book_id,created,state,save_kind) VALUES('v7-transfer',?,'synthetic.v7',1,1,'SAVED','transfer')", new Object[]{transferPayload.toString()});
            db.execSQL("INSERT INTO candidate_transfer_receipts(candidate_id,transfer_row_id,resolved_transfer_row_id,owner_semantic_id,transfer_semantic_id) VALUES('v7-transfer',60,60,7,901)");
            ContentValues high = projection(db, 1); high.put("id", 900); db.insertOrThrow("transactions", null, high); db.execSQL("DELETE FROM transactions WHERE id=900");
            db.execSQL("INSERT INTO original_transfers(id) VALUES(7000)"); db.execSQL("DELETE FROM original_transfers WHERE id=7000");
            JSONObject tables = new JSONObject(); for (String table : V7_TABLES) Json.put(tables, table, rows(db, table));
            return Json.object("format", "YimuReferenceBackup", "version", 7, "created", TIME, "tables", tables);
        }
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
    private static final class VersionSeven extends SQLiteOpenHelper {
        VersionSeven(Context context, String name) { super(context, name, null, 7); }
        @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(false); }
        @Override public void onCreate(SQLiteDatabase db) {
            ReferenceSchemaV4DeviceTest.VersionThree.createSchema(db);
            ReferenceSchemaV4.install(db); ReferenceSchemaV5.install(db); ReferenceSchemaV6.install(db); ReferenceSchemaV7.install(db);
        }
        @Override public void onOpen(SQLiteDatabase db) { super.onOpen(db); db.setForeignKeyConstraintsEnabled(true); ReferenceSchemaV7.checkForeignKeys(db); }
        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { throw new AssertionError("This fixture only creates the real version7 schema"); }
    }
}
