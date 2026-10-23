package com.wangc.bill.reference;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
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

/** Synthetic records in per-case disposable databases, not original APK execution. */
@RunWith(AndroidJUnit4.class)
public final class BookRepositoryDeviceTest {
    private Context context;
    private String databaseName;
    private ReferenceStore store;
    private BookRepository books;

    @Before public void openDatabase() {
        context = ApplicationProvider.getApplicationContext();
        databaseName = "reference-books-" + UUID.randomUUID() + ".sqlite";
        store = new ReferenceStore(context, databaseName);
        store.getWritableDatabase();
        books = new BookRepository(store);
    }

    @After public void deleteDatabase() {
        if (store != null) store.close();
        if (context != null && databaseName != null) context.deleteDatabase(databaseName);
    }

    @Test public void v2MigrationPreservesLocalKeysLinksAndUnmappedIdentity() {
        try (SQLiteDatabase legacy = SQLiteDatabase.create(null)) {
            legacy.setForeignKeyConstraintsEnabled(true);
            legacy.execSQL("CREATE TABLE books(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,currency TEXT NOT NULL DEFAULT 'CNY',budget INTEGER NOT NULL DEFAULT 0,archived INTEGER NOT NULL DEFAULT 0,created INTEGER NOT NULL)");
            legacy.execSQL("CREATE TABLE old_links(id INTEGER PRIMARY KEY,book_row_id INTEGER REFERENCES books(id))");
            legacy.execSQL("INSERT INTO books(id,name,currency,budget,created) VALUES(81,'日常账本','USD',432,123)");
            legacy.execSQL("INSERT INTO old_links(id,book_row_id) VALUES(1,81)");
            legacy.beginTransaction();
            try { BookRepository.installV3Schema(legacy); legacy.setTransactionSuccessful(); }
            finally { legacy.endTransaction(); }
            try (Cursor rows = legacy.rawQuery("SELECT b.*,l.book_row_id FROM books b JOIN old_links l ON l.book_row_id=b.id", null)) {
                assertTrue(rows.moveToFirst());
                assertEquals(81, rows.getLong(rows.getColumnIndexOrThrow("id")));
                assertEquals(81, rows.getLong(rows.getColumnIndexOrThrow("book_row_id")));
                assertEquals("日常账本", rows.getString(rows.getColumnIndexOrThrow("name")));
                assertEquals("USD", rows.getString(rows.getColumnIndexOrThrow("currency")));
                assertEquals(432, rows.getLong(rows.getColumnIndexOrThrow("budget")));
                assertEquals(123, rows.getLong(rows.getColumnIndexOrThrow("created")));
                assertEquals(BookRepository.LEGACY, rows.getString(rows.getColumnIndexOrThrow("book_origin")));
                assertTrue(rows.isNull(rows.getColumnIndexOrThrow("semantic_id")));
                assertTrue(rows.isNull(rows.getColumnIndexOrThrow("owner_semantic_id")));
                assertEquals(0, rows.getInt(rows.getColumnIndexOrThrow("book_type")));
                assertFalse(rows.moveToNext());
            }
            try (Cursor count = legacy.rawQuery("SELECT COUNT(*) FROM books", null)) {
                assertTrue(count.moveToFirst());
                assertEquals("Migration implicitly added a claimed default book", 1, count.getInt(0));
            }
            try (Cursor violations = legacy.rawQuery("PRAGMA foreign_key_check", null)) { assertFalse(violations.moveToFirst()); }
        }
    }

    @Test public void bootstrapUsesSemanticOneAndRegisterTimeWithoutAssumingRowOne() {
        // This row's familiar name is deliberately insufficient to claim an original identity.
        store.getWritableDatabase().execSQL("INSERT INTO books(id,name,created) VALUES(81,'日常账本',77)");
        long row = BookRepository.bootstrapNewLocalOwner(store.getWritableDatabase(), 7, 123456789L);
        assertNotEquals("A semantic book ID was returned as a local primary key", 1L, row);
        JSONObject seeded = books.byRowId(row);
        assertEquals(BookRepository.RECOVERED_LOCAL, seeded.optString("book_origin"));
        assertEquals(1, seeded.optLong("semantic_id")); assertEquals(7, seeded.optInt("owner_semantic_id"));
        assertEquals(BookRepository.TYPE_SYSTEM, seeded.optInt("book_type"));
        assertEquals("日常账本", seeded.optString("name")); assertEquals(123456789L, seeded.optLong("created"));
        assertEquals(0, seeded.optLong("update_time")); assertEquals(0, seeded.optInt("hide"));
        assertEquals(0, seeded.optInt("not_show_transfer")); assertEquals(0, seeded.optInt("not_show_stock"));
        assertEquals(row, books.byIdentity(7, 1).optLong("id"));
        assertTrue(books.byRowId(81).isNull("semantic_id")); assertTrue(books.byRowId(81).isNull("owner_semantic_id"));
        books.validateRestored();
    }

    @Test public void repeatedBootstrapPreservesItsRowAndScopesTheDefaultToEachOwner() {
        long first = BookRepository.bootstrapNewLocalOwner(store.getWritableDatabase(), 7, 111);
        store.getWritableDatabase().execSQL("UPDATE books SET name='已重命名的默认账本',created=222,update_time=333 WHERE id=?", new Object[]{first});
        assertEquals(first, BookRepository.bootstrapNewLocalOwner(store.getWritableDatabase(), 7, 999));
        assertEquals("已重命名的默认账本", books.byRowId(first).optString("name"));
        assertEquals(222, books.byRowId(first).optLong("created")); assertEquals(333, books.byRowId(first).optLong("update_time"));
        long other = BookRepository.bootstrapNewLocalOwner(store.getWritableDatabase(), 8, 444);
        assertNotEquals(first, other);
        assertEquals(first, books.systemBook(7).optLong("id")); assertEquals(other, books.systemBook(8).optLong("id"));
        assertEquals(1, books.byRowId(other).optLong("semantic_id")); assertEquals(444, books.byRowId(other).optLong("created"));
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM books WHERE owner_semantic_id=7 AND semantic_id=1"));
        assertEquals(1, store.scalar("SELECT COUNT(*) n FROM books WHERE owner_semantic_id=8 AND semantic_id=1"));
    }

    @Test public void creationKeepsBothIdSpacesAndTheOwnersExactName() {
        BookRepository deterministic = new BookRepository(store, new SequenceRandom(7001, 7002));
        JSONObject first = deterministic.createOriginal(7, "  出差  ", "icon/local", true, false, 7654321L);
        assertNotEquals(first.optLong("id"), first.optLong("semantic_id"));
        assertEquals(7001, first.optLong("semantic_id")); assertEquals(7, first.optInt("owner_semantic_id"));
        assertEquals("  出差  ", first.optString("name")); assertEquals("icon/local", first.optString("icon_url"));
        assertEquals(7654321L, first.optLong("created")); assertEquals(7654321L, first.optLong("update_time"));
        assertEquals(BookRepository.TYPE_USER, first.optInt("book_type"));
        assertEquals(1, first.optInt("not_show_transfer")); assertEquals(0, first.optInt("not_show_stock"));
        assertEquals(first.optLong("id"), deterministic.byIdentity(7, 7001).optLong("id"));
        assertNull(deterministic.byIdentity(7, first.optLong("id")));
        expectInvalid(() -> deterministic.createOriginal(7, "  出差  ", null, false, false, 10));
        JSONObject sameNameOtherOwner = deterministic.createOriginal(8, "  出差  ", null, false, true, 11);
        assertEquals(7002, sameNameOtherOwner.optLong("semantic_id"));
        assertEquals(8, sameNameOtherOwner.optInt("owner_semantic_id"));
        expectInvalid(() -> deterministic.createOriginal(7, "", null, false, false, 10));
    }

    @Test public void randomCollisionsRetryWithinTheOwnerAndZeroRemainsARealIdentity() {
        BookRepository.bootstrapNewLocalOwner(store.getWritableDatabase(), 7, 123);
        BookRepository deterministic = new BookRepository(store, new SequenceRandom(1, 7001, 7001, 0));
        JSONObject first = deterministic.createOriginal(7, "第一本", null, false, false, 10);
        assertEquals("The existing default's semantic ID must collide", 7001, first.optLong("semantic_id"));
        JSONObject anotherOwner = deterministic.createOriginal(8, "另一用户", null, false, false, 11);
        assertEquals(7001, anotherOwner.optLong("semantic_id"));
        JSONObject zero = deterministic.createOriginal(0, "零不是未映射", null, false, false, 12);
        assertFalse(zero.isNull("semantic_id")); assertFalse(zero.isNull("owner_semantic_id"));
        assertEquals(0, zero.optLong("semantic_id")); assertEquals(0, zero.optInt("owner_semantic_id"));
        assertEquals(zero.optLong("id"), deterministic.byIdentity(0, 0).optLong("id"));
        deterministic.validateRestored();
    }

    @Test public void creationUsesGlobalPositionAndOwnedVisibilityUsesDescendingOrder() {
        long system = BookRepository.bootstrapNewLocalOwner(store.getWritableDatabase(), 7, 1);
        long other = BookRepository.bootstrapNewLocalOwner(store.getWritableDatabase(), 8, 2);
        store.getWritableDatabase().execSQL("UPDATE books SET position_weight=50 WHERE id=?", new Object[]{other});
        BookRepository deterministic = new BookRepository(store, new SequenceRandom(7001, 7002));
        JSONObject first = deterministic.createOriginal(7, "较早新增", null, false, false, 3);
        JSONObject second = deterministic.createOriginal(7, "较晚新增", null, false, false, 4);
        assertEquals(51, first.optInt("position_weight")); assertEquals(52, second.optInt("position_weight"));
        List<JSONObject> visible = deterministic.visibleOwned(7);
        assertEquals(3, visible.size()); assertEquals(second.optLong("id"), visible.get(0).optLong("id"));
        assertEquals(first.optLong("id"), visible.get(1).optLong("id")); assertEquals(system, visible.get(2).optLong("id"));
        store.getWritableDatabase().execSQL("UPDATE books SET hide=1 WHERE id=?", new Object[]{first.optLong("id")});
        store.getWritableDatabase().execSQL("UPDATE books SET archived=1 WHERE id=?", new Object[]{second.optLong("id")});
        assertEquals(1, deterministic.visibleOwned(7).size());
        assertEquals(system, deterministic.visibleOwned(7).get(0).optLong("id"));
    }

    @Test public void currentSelectionConsumesALocalRowAndFallsBackWithinTheOwner() {
        store.getWritableDatabase().execSQL("INSERT INTO books(id,name,created) VALUES(81,'未映射旧账本',1)");
        long system = BookRepository.bootstrapNewLocalOwner(store.getWritableDatabase(), 7, 1);
        BookRepository deterministic = new BookRepository(store, new SequenceRandom(9007, 9008));
        JSONObject mine = deterministic.createOriginal(7, "选中的本地行", null, false, false, 2);
        JSONObject foreign = deterministic.createOriginal(8, "其他用户的本地行", null, false, false, 3);
        assertEquals(mine.optLong("id"), deterministic.resolveCurrent(7, mine.optLong("id")).optLong("id"));
        assertEquals(system, deterministic.resolveCurrent(7, 9007).optLong("id"));
        assertEquals(system, deterministic.resolveCurrent(7, 0).optLong("id"));
        assertEquals(system, deterministic.resolveCurrent(7, 81).optLong("id"));
        assertEquals(system, deterministic.resolveCurrent(7, foreign.optLong("id")).optLong("id"));
        store.getWritableDatabase().execSQL("UPDATE books SET share_users='[7]' WHERE id=?", new Object[]{foreign.optLong("id")});
        assertEquals(foreign.optLong("id"), deterministic.resolveCurrent(7, foreign.optLong("id")).optLong("id"));
        deterministic.validateRestored();
    }

    @Test public void backupRoundTripPreservesBookIdentityAndFinancialLocalReferences() {
        BookRepository deterministic = new BookRepository(store, new SequenceRandom(8080));
        JSONObject book = deterministic.createOriginal(7, "备份身份测试", "icon/synthetic", true, true, 1735689600000L);
        long rowId = book.optLong("id");
        JSONObject input = Json.object("book_id", rowId, "kind", "expense", "amount", 1234,
            "category_id", store.defaultCategory("expense"), "account_id", 0, "time", 1735689600000L);
        String candidate = store.addCandidate(input, "test.fixture.books", rowId);
        long transaction = store.saveTransaction(input, 0, candidate);
        JSONObject backup = store.backup();
        store.getWritableDatabase().execSQL("UPDATE books SET semantic_id=8081,owner_semantic_id=8,name='临时修改',share_users='[8]' WHERE id=?", new Object[]{rowId});
        store.restore(backup);
        JSONObject restored = books.byRowId(rowId);
        assertEquals(8080, restored.optLong("semantic_id")); assertEquals(7, restored.optInt("owner_semantic_id"));
        assertEquals("备份身份测试", restored.optString("name")); assertEquals("icon/synthetic", restored.optString("icon_url"));
        assertEquals(1, restored.optInt("not_show_transfer")); assertEquals(1, restored.optInt("not_show_stock"));
        assertEquals("[]", restored.optString("share_users"));
        assertEquals(rowId, books.byIdentity(7, 8080).optLong("id"));
        assertEquals(rowId, store.transaction(transaction).optLong("book_id"));
        assertEquals(1234, store.transaction(transaction).optLong("amount"));
        assertEquals(rowId, store.candidate(candidate).optLong("book_id"));
        assertEquals(rowId, Json.parse(store.candidate(candidate).optString("payload")).optLong("book_id"));
        assertEquals(transaction, store.candidate(candidate).optLong("transaction_id"));
        books.validateRestored();
    }

    @Test public void malformedRestoredIdentityTypesAndFlagsRollBackTheExistingBooks() {
        BookRepository deterministic = new BookRepository(store, new SequenceRandom(8181));
        JSONObject book = deterministic.createOriginal(7, "拒绝损坏身份", null, false, false, 99);
        long rowId = book.optLong("id");
        JSONObject backup = store.backup();
        for (String field : new String[]{"id", "owner_semantic_id", "semantic_id", "book_type", "position_weight", "not_show_stock", "share_users"}) {
            JSONObject damaged = Json.copy(backup);
            JSONObject row = backupBook(damaged, rowId);
            Json.put(row, field, switch (field) {
                case "id" -> 0;
                case "owner_semantic_id" -> JSONObject.NULL;
                case "semantic_id" -> "not-a-number";
                case "book_type" -> 77;
                case "position_weight" -> 2147483648L;
                case "not_show_stock" -> 2;
                default -> "[true]";
            });
            expectInvalid(() -> store.restore(damaged));
            assertEquals(8181, books.byRowId(rowId).optLong("semantic_id"));
            assertEquals(7, books.byRowId(rowId).optInt("owner_semantic_id"));
            assertEquals("拒绝损坏身份", books.byRowId(rowId).optString("name"));
            assertEquals(0, books.byRowId(rowId).optInt("not_show_stock"));
            books.validateRestored();
        }
    }

    private static JSONObject backupBook(JSONObject backup, long rowId) {
        JSONArray rows = backup.optJSONObject("tables").optJSONArray("books");
        for (int i = 0; i < rows.length(); i++) if (rows.optJSONObject(i).optLong("id") == rowId) return rows.optJSONObject(i);
        throw new AssertionError("Synthetic backup book missing");
    }

    private static void expectInvalid(Runnable action) {
        try { action.run(); fail("Malformed or duplicate book data was accepted"); }
        catch (IllegalArgumentException expected) { /* Preserve the original database on rejection. */ }
    }

    private static final class SequenceRandom extends Random {
        private final int[] values;
        private int at;
        SequenceRandom(int... values) { this.values = values.clone(); }
        @Override public int nextInt(int bound) {
            assertEquals(Integer.MAX_VALUE, bound);
            if (at >= values.length) throw new AssertionError("Unexpected extra random ID request");
            return values[at++];
        }
    }
}
