package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONObject;

/** AccountBook's owner/semantic identity is independent of the local books.id foreign key. */
public final class BookRepository {
    public static final String LEGACY = "legacy", RECOVERED_LOCAL = "recovered_local";
    public static final long DEFAULT_SEMANTIC_ID = 1;
    public static final int TYPE_SYSTEM = 1, TYPE_USER = 2;
    private final ReferenceStore store;
    private final Random random;

    public BookRepository(ReferenceStore store) { this(store, new Random()); }

    BookRepository(ReferenceStore store, Random random) {
        if (store == null || random == null) throw new IllegalArgumentException("账本存储和标识生成器不能为空");
        this.store = store;
        this.random = random;
    }

    /** SQLiteOpenHelper's create/upgrade transaction owns these ALTER statements. */
    public static void installV3Schema(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE books ADD COLUMN book_origin TEXT NOT NULL DEFAULT 'legacy'");
        db.execSQL("ALTER TABLE books ADD COLUMN semantic_id INTEGER");
        db.execSQL("ALTER TABLE books ADD COLUMN owner_semantic_id INTEGER");
        db.execSQL("ALTER TABLE books ADD COLUMN book_type INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE books ADD COLUMN position_weight INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE books ADD COLUMN update_time INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE books ADD COLUMN icon_url TEXT");
        db.execSQL("ALTER TABLE books ADD COLUMN current_budget_semantic_id INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE books ADD COLUMN hide INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE books ADD COLUMN not_show_transfer INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE books ADD COLUMN not_show_stock INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE books ADD COLUMN share_users TEXT NOT NULL DEFAULT '[]'");
        db.execSQL("CREATE UNIQUE INDEX books_original_identity ON books(owner_semantic_id,semantic_id) WHERE owner_semantic_id IS NOT NULL AND semantic_id IS NOT NULL");
        db.execSQL("CREATE INDEX books_original_order ON books(owner_semantic_id,hide,position_weight DESC)");
    }

    /**
     * database.c.d: create only a missing semantic-1 book for this explicit owner.
     * Returns its local SQLite row ID. A synthetic offline owner is the caller's policy;
     * neither an old row's name nor its PK supplies an original owner or semantic ID.
     */
    public static long bootstrapNewLocalOwner(SQLiteDatabase db, int owner, long registerTime) {
        db.beginTransaction();
        try {
            Long existing = identityRowId(db, owner, DEFAULT_SEMANTIC_ID);
            if (existing != null) {
                db.setTransactionSuccessful();
                return existing;
            }
            ContentValues values = values(owner, DEFAULT_SEMANTIC_ID, "日常账本", registerTime, TYPE_SYSTEM);
            values.put("position_weight", nextPosition(db));
            long rowId = db.insertOrThrow("books", null, values);
            db.setTransactionSuccessful();
            return rowId;
        } finally { db.endTransaction(); }
    }

    public JSONObject byRowId(long rowId) {
        return store.one("SELECT * FROM books WHERE id=?", "" + rowId);
    }

    public JSONObject byIdentity(int owner, long semanticId) {
        return store.one("SELECT * FROM books WHERE owner_semantic_id=? AND semantic_id=?", "" + owner, "" + semanticId);
    }

    /** a.C/J select the owner's system type; they do not assume its SQLite PK is 1. */
    public JSONObject systemBook(int owner) {
        return store.one("SELECT * FROM books WHERE owner_semantic_id=? AND book_type=1 ORDER BY id LIMIT 1", "" + owner);
    }

    /**
     * MyApplication.g consumes the raw current-book SQLite PK. Unmapped legacy rows
     * cannot establish original ownership; callers can retain a separate legacy UI path.
     * This method neither reads nor rewrites preferences.
     */
    public JSONObject resolveCurrent(int owner, long rawLocalRowId) {
        synchronized (store) {
            JSONObject selected = byRowId(rawLocalRowId);
            if (selected != null && !selected.isNull("owner_semantic_id")) {
                if (selected.optInt("owner_semantic_id") == owner || sharedWith(selected, owner)) return selected;
            }
            return systemBook(owner);
        }
    }

    /** a.z's owned visible portion; archived is an additional local experimental state. */
    public List<JSONObject> visibleOwned(int owner) {
        return store.query("SELECT * FROM books WHERE owner_semantic_id=? AND hide=0 AND archived=0 ORDER BY position_weight DESC,id", "" + owner);
    }

    /**
     * AddAccountBook.complete + a.c/o. The returned row exposes BOTH id spaces.
     * The exact original name check does not trim whitespace. currency/budget remain
     * this experiment's existing fields and are not original AccountBook properties.
     */
    public JSONObject createOriginal(int owner, String name, String iconUrl,
            boolean notShowTransfer, boolean notShowStock, long now) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("账本名称不能为空");
        synchronized (store) {
            SQLiteDatabase db = store.getWritableDatabase();
            db.beginTransaction();
            try {
                if (store.one("SELECT id FROM books WHERE owner_semantic_id=? AND name=? LIMIT 1", "" + owner, name) != null)
                    throw new IllegalArgumentException("账本已经存在");
                int semantic;
                do { semantic = random.nextInt(Integer.MAX_VALUE); }
                while (identityRowId(db, owner, semantic) != null);
                ContentValues values = values(owner, semantic, name, now, TYPE_USER);
                values.put("icon_url", iconUrl);
                values.put("not_show_transfer", notShowTransfer ? 1 : 0);
                values.put("not_show_stock", notShowStock ? 1 : 0);
                values.put("update_time", now);
                values.put("position_weight", nextPosition(db));
                long rowId = db.insertOrThrow("books", null, values);
                JSONObject result = byRowId(rowId);
                db.setTransactionSuccessful();
                return result;
            } finally { db.endTransaction(); }
        }
    }

    /** Reject malformed imported metadata without turning NULL identities into owner/ID zero. */
    public void validateRestored() {
        synchronized (store) {
            try (Cursor rows = store.getReadableDatabase().rawQuery("SELECT * FROM books", null)) {
                while (rows.moveToNext()) {
                    if (integer(rows, "id") <= 0) invalid();
                    String origin = string(rows, "book_origin", false);
                    boolean legacy = LEGACY.equals(origin);
                    if (!legacy && !RECOVERED_LOCAL.equals(origin)) invalid();
                    boolean ownerNull = rows.isNull(rows.getColumnIndexOrThrow("owner_semantic_id"));
                    boolean semanticNull = rows.isNull(rows.getColumnIndexOrThrow("semantic_id"));
                    long type = integer(rows, "book_type");
                    if (legacy) {
                        if (!ownerNull || !semanticNull || type != 0) invalid();
                    } else {
                        if (ownerNull || semanticNull) invalid();
                        signedInt(integer(rows, "owner_semantic_id"));
                        long semantic = integer(rows, "semantic_id");
                        // Recovered local allocation is Random.nextInt(Integer.MAX_VALUE).
                        // Zero is an actual possible result; NULL means unmapped instead.
                        if (semantic < 0 || semantic >= Integer.MAX_VALUE || (type != TYPE_SYSTEM && type != TYPE_USER)) invalid();
                    }
                    signedInt(integer(rows, "position_weight"));
                    integer(rows, "update_time");
                    integer(rows, "current_budget_semantic_id");
                    flag(rows, "hide"); flag(rows, "not_show_transfer"); flag(rows, "not_show_stock");
                    string(rows, "icon_url", true);
                    JSONArray shares = Json.array(string(rows, "share_users", false));
                    for (int i = 0; i < shares.length(); i++) {
                        Object value = shares.opt(i);
                        if (!(value instanceof Integer) && !(value instanceof Long)) invalid();
                        signedInt(((Number) value).longValue());
                    }
                }
            }
        }
    }

    private static ContentValues values(int owner, long semantic, String name, long created, int type) {
        ContentValues values = new ContentValues();
        values.put("name", name); values.put("created", created);
        values.put("book_origin", RECOVERED_LOCAL); values.put("owner_semantic_id", owner);
        values.put("semantic_id", semantic); values.put("book_type", type);
        return values;
    }

    private static Long identityRowId(SQLiteDatabase db, int owner, long semantic) {
        try (Cursor row = db.rawQuery("SELECT id FROM books WHERE owner_semantic_id=? AND semantic_id=? LIMIT 1", new String[]{"" + owner, "" + semantic})) {
            return row.moveToFirst() ? Long.valueOf(row.getLong(0)) : null;
        }
    }

    private static int nextPosition(SQLiteDatabase db) {
        try (Cursor row = db.rawQuery("SELECT COALESCE(MAX(position_weight),0) FROM books", null)) {
            row.moveToFirst();
            return row.getInt(0) + 1; // Original G()+1 is Java int arithmetic, including its boundary.
        }
    }

    private static boolean sharedWith(JSONObject book, int owner) {
        JSONArray users = Json.array(book.optString("share_users", "[]"));
        for (int i = 0; i < users.length(); i++) {
            Object value = users.opt(i);
            if ((value instanceof Integer || value instanceof Long) && ((Number) value).longValue() == owner) return true;
        }
        return false;
    }

    private static long integer(Cursor row, String column) {
        int at = row.getColumnIndexOrThrow(column);
        if (row.getType(at) != Cursor.FIELD_TYPE_INTEGER) invalid();
        return row.getLong(at);
    }

    private static String string(Cursor row, String column, boolean nullable) {
        int at = row.getColumnIndexOrThrow(column);
        if (nullable && row.isNull(at)) return null;
        if (row.getType(at) != Cursor.FIELD_TYPE_STRING) invalid();
        return row.getString(at);
    }

    private static void signedInt(long value) { if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) invalid(); }
    private static void flag(Cursor row, String column) { long value = integer(row, column); if (value != 0 && value != 1) invalid(); }
    private static void invalid() { throw new IllegalArgumentException("备份账本的原始身份或显示配置无效"); }
}
