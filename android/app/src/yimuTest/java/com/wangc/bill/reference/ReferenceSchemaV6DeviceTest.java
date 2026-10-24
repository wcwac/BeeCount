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
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Actual version5 fixture migration and whole-database version6 restore boundaries. */
@RunWith(AndroidJUnit4.class)
public final class ReferenceSchemaV6DeviceTest {
    private static final String[] V5_TABLES = {"books", "accounts", "categories", "tags", "candidates", "transactions",
            "transaction_tags", "rules", "plans", "goals", "category_hidden_books", "category_accounts", "asset_show_books",
            "asset_histories", "original_refunds", "original_refund_items", "original_reimbursements",
            "original_reimbursement_items", "original_bill_related", "original_instalments", "original_curd_histories"};
    private static final String IMPORT_KEY = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
    private Context context;
    private String database;
    private ReferenceStore store;

    @Before public void prepare() {
        context = ApplicationProvider.getApplicationContext(); database = "reference-v6-" + UUID.randomUUID() + ".sqlite";
    }
    @After public void close() { if (store != null) store.close(); if (context != null && database != null) context.deleteDatabase(database); }
    private void openLatest() { store = new ReferenceStore(context, database); store.getWritableDatabase(); }

    @Test public void realVersionFiveUpgradePreservesLegacyAndCanonicalGraphsWithoutInferringTransfersOrReplayingBalances() {
        createVersionFiveGraph(); openLatest(); assertEquals(8, store.getReadableDatabase().getVersion()); assertOldGraph();
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfer_tags"));
        assertEquals("transfer", store.transaction(125).optString("kind"));
        assertEquals("legacy", store.transaction(125).optString("bill_origin"));
        for (String column : OriginalBillRepository.CANONICAL_COLUMNS) assertTrue(column, store.transaction(125).isNull(column));
        ContentValues next = projection(1); long inserted = store.getWritableDatabase().insertOrThrow("transactions", null, next);
        assertTrue("Migration reset a deleted transaction's AUTOINCREMENT high-water mark", inserted > 900);
        ContentValues duplicate = projection(1); duplicate.put("import_key", IMPORT_KEY);
        try { store.getWritableDatabase().insertOrThrow("transactions", null, duplicate); fail("Import uniqueness was lost"); }
        catch (android.database.SQLException expected) {}
        assertEquals(0, foreignKeyErrors());
    }

    @Test public void versionSixBackupRoundTripsAllTablesAndRawTransferFieldsWithOrderedDuplicateNullAndEmptyTags() {
        createVersionFiveGraph(); openLatest(); OriginalTransferRepository repository = new OriginalTransferRepository(store);
        OriginalTransferRecord tagged = transfer(Long.MAX_VALUE); tagged.tags = new ArrayList<>(Arrays.asList(4L, 4L, null, -1L, Long.MAX_VALUE));
        OriginalTransferRecord missing = transfer(Long.MAX_VALUE); missing.tags = null; missing.remark = null;
        OriginalTransferRecord empty = transfer(Long.MIN_VALUE); empty.userId = 8; empty.tags.clear(); empty.remark = "";
        repository.saveOriginalRaw(tagged); repository.saveOriginalRaw(missing); repository.saveOriginalRaw(empty);
        JSONObject backup = store.backup(); assertEquals(8, backup.optInt("version")); assertEquals(26, backup.optJSONObject("tables").length());
        assertNotNull(backup.optJSONObject("tables").optJSONArray("original_transfers"));
        assertNotNull(backup.optJSONObject("tables").optJSONArray("original_transfer_tags"));
        store.getWritableDatabase().execSQL("UPDATE accounts SET asset_number=999 WHERE id=43");
        store.getWritableDatabase().execSQL("UPDATE original_transfers SET cost=888,remark='替换前'"); store.restore(backup);
        assertOldGraph(); assertTablesEqual(backup);
        OriginalTransferRecord restored = repository.byLocalRowId(tagged.localRowId);
        assertEquals(Long.MAX_VALUE, restored.transferId); assertNotEquals(restored.localRowId, restored.transferId);
        assertEquals(7, restored.userId); assertEquals(Long.MIN_VALUE, restored.fromAssetId); assertEquals(Long.MAX_VALUE, restored.toAssetId);
        assertEquals(-9, restored.billId); assertEquals(Long.MIN_VALUE, restored.time); assertEquals(Long.MAX_VALUE, restored.updateTime);
        assertEquals(-2.5d, restored.cost, 0d); assertEquals(3.125d, restored.toCost, 0d); assertEquals(-0.125d, restored.serviceCharge, 0d);
        assertTrue(restored.originalDeleted); assertEquals("  原始转账备注  ", restored.remark);
        assertEquals(Arrays.asList(4L, 4L, null, -1L, Long.MAX_VALUE), restored.tags);
        assertNull(repository.byLocalRowId(missing.localRowId).tags); assertNull(repository.byLocalRowId(missing.localRowId).remark);
        assertNotNull(repository.byLocalRowId(empty.localRowId).tags); assertTrue(repository.byLocalRowId(empty.localRowId).tags.isEmpty());
        assertEquals("", repository.byLocalRowId(empty.localRowId).remark); assertEquals(2, repository.allForOwner(7).size());
        assertEquals(1, repository.allForOwner(8).size()); assertEquals(0, foreignKeyErrors());
    }

    @Test public void aRealVersionFiveBackupIgnoresForgedTransferTablesAndClearsNewStoresWithoutChangingItsGraph() {
        JSONObject old = createVersionFiveGraph(); openLatest();
        OriginalTransferRecord current = transfer(77); current.tags = new ArrayList<>(Arrays.asList(1L, null));
        new OriginalTransferRepository(store).saveOriginalRaw(current);
        JSONObject tables = old.optJSONObject("tables");
        Json.put(tables, "original_transfers", new JSONArray().put(Json.object("id", "forged", "cost", "not numeric")));
        Json.put(tables, "original_transfer_tags", new JSONArray().put(Json.object("id", -1, "transfer_row_id", 999, "position", -1)));
        store.restore(old); assertOldGraph();
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfers"));
        assertEquals(0, store.scalar("SELECT COUNT(*) n FROM original_transfer_tags"));
        assertNull(new OriginalTransferRepository(store).byLocalRowId(current.localRowId));
        assertEquals(0, foreignKeyErrors());
    }

    @Test public void missingVersionSixTablesAndInvalidRawTransferHeaderTypesRollBackEveryExistingTable() {
        createVersionFiveGraph(); openLatest(); OriginalTransferRecord record = transfer(66);
        record.tags = new ArrayList<>(Arrays.asList(3L, null)); new OriginalTransferRepository(store).saveOriginalRaw(record);
        JSONObject valid = store.backup();
        for (String table : new String[]{"original_transfers", "original_transfer_tags"}) {
            JSONObject missing = changedGraph(valid); missing.optJSONObject("tables").remove(table);
            expectInvalid(missing); assertTablesEqual(valid); assertOldGraph();
        }
        Object[][] defects = {{"owner_semantic_id", 2147483648L}, {"bill_semantic_id", -2147483649L},
                {"transfer_semantic_id", 1.5d}, {"cost", "12.5"}, {"to_cost", new JSONArray()},
                {"service_charge", JSONObject.NULL}, {"original_delete_flag", 2}, {"tags_is_null", 2}};
        for (Object[] defect : defects) {
            JSONObject invalid = changedGraph(valid);
            Json.put(invalid.optJSONObject("tables").optJSONArray("original_transfers").optJSONObject(0), (String) defect[0], defect[1]);
            expectInvalid(invalid); assertTablesEqual(valid); assertOldGraph();
        }
    }

    @Test public void invalidTransferTagParentsPositionsAndValueTypesRollBackTheWholeOldGraph() {
        createVersionFiveGraph(); openLatest(); OriginalTransferRecord record = transfer(88);
        record.tags = new ArrayList<>(Arrays.asList(3L, 3L, null)); new OriginalTransferRepository(store).saveOriginalRaw(record);
        JSONObject valid = store.backup();
        Object[][] defects = {{"transfer_row_id", 999999L}, {"position", -1}, {"position", 9},
                {"position", 1}, {"value", 1.5d}, {"value", "not an integer"}, {"id", -1}};
        for (Object[] defect : defects) {
            JSONObject invalid = changedGraph(valid);
            Json.put(invalid.optJSONObject("tables").optJSONArray("original_transfer_tags").optJSONObject(0), (String) defect[0], defect[1]);
            expectInvalid(invalid); assertTablesEqual(valid); assertOldGraph();
        }
        JSONObject contradictory = changedGraph(valid);
        Json.put(contradictory.optJSONObject("tables").optJSONArray("original_transfers").optJSONObject(0), "tags_is_null", 1);
        expectInvalid(contradictory); assertTablesEqual(valid); assertOldGraph();
    }

    private static OriginalTransferRecord transfer(long semantic) {
        OriginalTransferRecord r = new OriginalTransferRecord(); r.transferId = semantic; r.userId = 7;
        r.fromAssetId = Long.MIN_VALUE; r.toAssetId = Long.MAX_VALUE; r.cost = -2.5d; r.toCost = 3.125d;
        r.serviceCharge = -0.125d; r.billId = -9; r.time = Long.MIN_VALUE; r.updateTime = Long.MAX_VALUE;
        r.remark = "  原始转账备注  "; r.originalDeleted = true; return r;
    }
    private JSONObject changedGraph(JSONObject valid) {
        JSONObject invalid = Json.copy(valid); JSONObject tables = invalid.optJSONObject("tables");
        Json.put(tables.optJSONArray("books").optJSONObject(0), "name", "不应留下的部分恢复");
        for (int i = 0; i < tables.optJSONArray("transactions").length(); i++) {
            JSONObject row = tables.optJSONArray("transactions").optJSONObject(i);
            if (row.optLong("id") == 123) Json.put(row, "remark", "不应留下的部分恢复");
        }
        return invalid;
    }
    private void expectInvalid(JSONObject backup) {
        try { store.restore(backup); fail("Invalid version6 transfer data was accepted"); }
        catch (IllegalArgumentException | android.database.SQLException expected) {}
    }
    private void assertTablesEqual(JSONObject expected) {
        JSONObject actual = store.backup().optJSONObject("tables"), tables = expected.optJSONObject("tables");
        assertEquals(26, actual.length()); assertEquals(26, tables.length());
        for (java.util.Iterator<String> names = tables.keys(); names.hasNext();) {
            String name = names.next(); assertEquals(name, tables.optJSONArray(name).toString(), actual.optJSONArray(name).toString());
        }
    }
    private void assertOldGraph() {
        JSONObject legacy = store.transaction(123), canonical = store.transaction(124);
        assertEquals("legacy", legacy.optString("bill_origin")); assertEquals(42, legacy.optLong("account_id"));
        assertEquals(IMPORT_KEY, legacy.optString("import_key")); assertEquals("v5-saved", legacy.optString("candidate_id"));
        assertEquals("SAVED", store.candidate("v5-saved").optString("state")); assertEquals(123, store.candidate("v5-saved").optLong("transaction_id"));
        assertEquals("bill", store.candidate("v5-saved").optString("save_kind"));
        assertEquals(123, canonical.optLong("related_id")); assertEquals(1, store.scalar("SELECT COUNT(*) n FROM transaction_tags WHERE transaction_id=123 AND tag_id=77"));
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM rules WHERE id=4 AND account_id=42")); assertEquals(734, store.accountBalance(42));
        assertEquals(23.5d, store.assetRepository().byRowId(43).optDouble("asset_number"), 0d);
        OriginalBillRecord bill = new OriginalBillRepository(store).byLocalRowId(124);
        assertEquals(901, bill.billId); assertEquals(501, bill.assetId); assertEquals(-3.25d, bill.cost, 0d);
        assertEquals(Arrays.asList(44L, 44L), bill.tags);
        assertEquals(901, new OriginalBillRelatedRepository(store).byLocalRowId(51).oneBillId);
        assertEquals(902, new OriginalBillRelatedRepository(store).byLocalRowId(51).twoBillId);
        assertEquals(901, new OriginalInstalmentRepository(store).byLocalRowId(52).billId);
        assertEquals(12.5d, new OriginalInstalmentRepository(store).byLocalRowId(52).totalNumber, 0d);
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM asset_histories"));
        assertEquals(23.5d, new AssetHistoryRepository(store).byIdentity(7, 61).optDouble("current_number"), 0d);
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM original_curd_histories")); assertEquals(0, foreignKeyErrors());
    }
    private int foreignKeyErrors() { try (Cursor rows = store.getReadableDatabase().rawQuery("PRAGMA foreign_key_check", null)) { return rows.getCount(); } }
    private static ContentValues projection(long amount) {
        ContentValues value = new ContentValues(); value.put("book_id", 1); value.put("kind", "expense"); value.put("amount", amount);
        value.put("currency", "CNY"); value.put("time", 1735689600000L); value.put("created", 1735689600000L); return value;
    }
    private JSONObject createVersionFiveGraph() {
        try (VersionFive old = new VersionFive(context, database)) {
            SQLiteDatabase db = old.getWritableDatabase(); assertEquals(5, db.getVersion());
            assertEquals(0, scalar(db, "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='original_transfers'"));
            long category = scalar(db, "SELECT id FROM categories WHERE level='parent' AND semantic_id=99");
            db.execSQL("INSERT INTO accounts(id,name,opening) VALUES(42,'v5旧实验账户',1234)");
            db.execSQL("INSERT INTO accounts(id,name,type,currency,asset_origin,semantic_id,owner_semantic_id,asset_type,asset_number) VALUES(43,'v5原账户','original','', 'original',501,7,1,23.5)");
            JSONObject payload = Json.object("book_id", 1, "kind", "expense", "amount", 500, "category_id", category, "currency", "CNY", "time", 1735689600000L);
            db.execSQL("INSERT INTO candidates(id,payload,package_name,book_id,created,state,transaction_id) VALUES('v5-saved',?,'test.v5',1,1,'SAVED',123)", new Object[]{payload.toString()});
            ContentValues legacy = projection(500); legacy.put("id", 123); legacy.put("category_id", category); legacy.put("account_id", 42);
            legacy.put("parent_semantic_id", 99); legacy.put("child_semantic_id", -1); legacy.put("candidate_id", "v5-saved"); legacy.put("import_key", IMPORT_KEY);
            db.insertOrThrow("transactions", null, legacy);
            db.execSQL("INSERT INTO tags(id,name) VALUES(77,'v5保留标签')"); db.execSQL("INSERT INTO transaction_tags(transaction_id,tag_id) VALUES(123,77)");
            db.execSQL("INSERT INTO rules(id,merchant,category_id,book_id,account_id) VALUES(4,'v5规则',?,1,42)", new Object[]{category});
            ContentValues canonical = projection(325); canonical.put("id", 124); canonical.put("related_id", 123); canonical.put("bill_origin", "original");
            canonical.put("category_id", category); canonical.put("account_id", 43); canonical.put("parent_semantic_id", 99); canonical.put("child_semantic_id", -1);
            canonical.put("owner_semantic_id", 7); canonical.put("raw_asset_semantic_id", 501);
            for (String field : new String[]{"bill_asset_semantic_id", "bill_semantic_id", "bill_type", "bill_book_semantic_id", "bill_child_category_id", "bill_from_user_id", "bill_in_asset_time", "bill_not_into_budget", "bill_not_into_total", "bill_parent_category_id", "bill_record_time", "bill_reimbursement", "bill_reimbursement_end", "bill_time", "bill_update_time", "bill_user_id"}) canonical.put(field, 0L);
            for (String field : new String[]{"bill_cost", "bill_currency_asset_number", "bill_currency_discount_number", "bill_discount_number"}) canonical.put(field, 0d);
            canonical.put("bill_asset_semantic_id", 501); canonical.put("bill_semantic_id", 901); canonical.put("bill_cost", -3.25d);
            canonical.put("bill_book_semantic_id", scalar(db, "SELECT semantic_id FROM books WHERE id=1")); canonical.put("bill_child_category_id", -1);
            canonical.put("bill_parent_category_id", 99); canonical.put("bill_user_id", 7); canonical.put("bill_tags", "[44,44]");
            db.insertOrThrow("transactions", null, canonical);
            ContentValues transfer = projection(77); transfer.put("id", 125); transfer.put("kind", "transfer"); db.insertOrThrow("transactions", null, transfer);
            db.execSQL("INSERT INTO asset_histories(id,semantic_id,asset_semantic_id,change_content,change_number,current_number,time,update_time,owner_semantic_id) VALUES(50,61,501,'v5原历史',-3.25,23.5,1,2,7)");
            db.execSQL("INSERT INTO original_bill_related(id,owner_semantic_id,related_semantic_id,one_bill_semantic_id,two_bill_semantic_id,update_time,original_delete_flag) VALUES(51,7,9000000001,901,902,3,0)");
            db.execSQL("INSERT INTO original_instalments(id,account_month,asset_semantic_id,parent_bill_semantic_id,in_asset_time,instalment_semantic_id,instalment_type,periods,remainder_type,self_info,service_number,service_type,total_number,update_time,owner_semantic_id,original_delete_flag) VALUES(52,NULL,501,901,1,9000000002,0,3,0,NULL,0,0,12.5,3,7,0)");
            db.execSQL("INSERT INTO original_curd_histories(id,owner_semantic_id,entity_type,entity_semantic_id,action_type,bill_book_semantic_id,bill_owner_semantic_id) VALUES(53,7,6,501,1,0,0)");
            ContentValues high = projection(1); high.put("id", 900); db.insertOrThrow("transactions", null, high); db.execSQL("DELETE FROM transactions WHERE id=900");
            JSONObject tables = new JSONObject(); for (String table : V5_TABLES) Json.put(tables, table, rows(db, table));
            return Json.object("format", "YimuReferenceBackup", "version", 5, "created", 1735689600000L, "tables", tables);
        }
    }
    private static long scalar(SQLiteDatabase db, String sql) { try (Cursor row = db.rawQuery(sql, null)) { assertTrue(row.moveToFirst()); return row.getLong(0); } }
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
    private static final class VersionFive extends SQLiteOpenHelper {
        VersionFive(Context context, String name) { super(context, name, null, 5); }
        @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }
        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { throw new AssertionError("Only create a real version5 fixture"); }
        @Override public void onCreate(SQLiteDatabase db) {
            ReferenceSchemaV4DeviceTest.VersionThree.createSchema(db); ReferenceSchemaV4.install(db); ReferenceSchemaV5.install(db);
        }
    }
}
