package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Original semantic category IDs are separate from categories.id, the local foreign key. */
public final class CategoryRepository {
    public static final String LEGACY = "legacy", PARENT = "parent", CHILD = "child";
    private final ReferenceStore store;

    CategoryRepository(ReferenceStore store) { this.store = store; }

    /** Used only with the database supplied to SQLiteOpenHelper callbacks. */
    static void installV2Schema(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE categories ADD COLUMN level TEXT NOT NULL DEFAULT 'legacy'");
        db.execSQL("ALTER TABLE categories ADD COLUMN semantic_id INTEGER");
        db.execSQL("ALTER TABLE categories ADD COLUMN parent_semantic_id INTEGER");
        db.execSQL("ALTER TABLE categories ADD COLUMN parent_child_semantic_id INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE categories ADD COLUMN category_type INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE categories ADD COLUMN position_weight INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE categories ADD COLUMN icon_night TEXT");
        db.execSQL("ALTER TABLE categories ADD COLUMN update_time INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE transactions ADD COLUMN parent_semantic_id INTEGER");
        db.execSQL("ALTER TABLE transactions ADD COLUMN child_semantic_id INTEGER");
        db.execSQL("CREATE INDEX categories_semantic ON categories(level,parent_semantic_id,semantic_id)");
        db.execSQL("CREATE INDEX categories_order ON categories(level,parent_semantic_id,position_weight DESC)");
        db.execSQL("CREATE TABLE category_hidden_books(category_row_id INTEGER NOT NULL REFERENCES categories(id) ON DELETE CASCADE,book_row_id INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE,PRIMARY KEY(category_row_id,book_row_id))");
        // NULL stores the original no-account -1 without inventing an accounts row.
        db.execSQL("CREATE TABLE category_accounts(parent_semantic_id INTEGER NOT NULL,child_semantic_id INTEGER NOT NULL,account_row_id INTEGER REFERENCES accounts(id),updated INTEGER NOT NULL,PRIMARY KEY(parent_semantic_id,child_semantic_id))");
    }

    /** database.d.b and database.a.b have independent 'no system row' gates. */
    static void bootstrap(SQLiteDatabase db) {
        if (!exists(db, "SELECT 1 FROM categories WHERE level='parent' AND category_type=1 LIMIT 1")) {
            for (OriginalCategories.ParentSeed seed : OriginalCategories.parents()) {
                if (exists(db, "SELECT 1 FROM categories WHERE level='parent' AND semantic_id=? LIMIT 1", "" + seed.categoryId)) continue;
                db.insertOrThrow("categories", null, seedValues(seed, PARENT, null));
            }
        }
        if (!exists(db, "SELECT 1 FROM categories WHERE level='child' AND category_type=1 LIMIT 1")) {
            for (OriginalCategories.ChildSeed seed : OriginalCategories.children())
                db.insertOrThrow("categories", null, seedValues(seed, CHILD, seed.parentCategoryId));
        }
    }

    private static ContentValues seedValues(OriginalCategories.Seed seed, String level, Integer parent) {
        ContentValues values = new ContentValues();
        values.put("name", seed.categoryName); values.put("icon", seed.iconUrl);
        values.put("level", level); values.put("semantic_id", seed.categoryId);
        if (parent != null) values.put("parent_semantic_id", parent);
        values.put("kind", (parent == null ? seed.categoryId : parent.intValue()) == 9 ? "income" : "expense");
        values.put("category_type", seed.categoryType); values.put("position_weight", seed.positionWeight);
        values.put("update_time", seed.updateTime);
        return values;
    }

    private static boolean exists(SQLiteDatabase db, String sql, String... arguments) {
        try (Cursor cursor = db.rawQuery(sql, arguments)) { return cursor.moveToFirst(); }
    }

    public JSONObject byRowId(long rowId) {
        return store.one("SELECT * FROM categories WHERE id=?", "" + rowId);
    }

    public JSONObject parent(int semanticId) {
        return store.one("SELECT * FROM categories WHERE level='parent' AND semantic_id=? ORDER BY id LIMIT 1", "" + semanticId);
    }

    public JSONObject child(int parentSemanticId, int childSemanticId) {
        return store.one("SELECT * FROM categories WHERE level='child' AND parent_semantic_id=? AND semantic_id=? ORDER BY id LIMIT 1", "" + parentSemanticId, "" + childSemanticId);
    }

    private List<JSONObject> visible(String where, long bookRowId, String... arguments) {
        List<String> args = new ArrayList<>(java.util.Arrays.asList(arguments)); args.add("" + bookRowId);
        return store.query("SELECT c.* FROM categories c WHERE c.archived=0 AND " + where
            + " AND NOT EXISTS(SELECT 1 FROM category_hidden_books h WHERE h.category_row_id=c.id AND h.book_row_id=?) ORDER BY c.position_weight DESC,c.id", args.toArray(new String[0]));
    }

    public List<JSONObject> expenseParents(long bookRowId) {
        return visible("c.level='parent' AND c.semantic_id<>9", bookRowId);
    }

    /** m0.E for expense; income top-level has the extra m0.T parentChild=0 condition. */
    public List<JSONObject> children(int parentSemanticId, long bookRowId) {
        return visible("c.level='child' AND c.parent_semantic_id=?"
            + (parentSemanticId == 9 ? " AND c.parent_child_semantic_id=0" : ""), bookRowId, "" + parentSemanticId);
    }

    public List<JSONObject> incomeTopLevel(long bookRowId) { return children(9, bookRowId); }

    public List<JSONObject> nestedIncome(int topChildSemanticId, long bookRowId) {
        return visible("c.level='child' AND c.parent_semantic_id=9 AND c.parent_child_semantic_id=?", bookRowId, "" + topChildSemanticId);
    }

    /** Existing experimental flat rows are retained explicitly, never matched to seeds by name. */
    public List<JSONObject> legacy(String kind) {
        return store.query("SELECT * FROM categories WHERE level='legacy' AND kind=? AND archived=0 ORDER BY id", kind);
    }

    public List<JSONObject> legacy(String kind,long bookRowId) {
        return visible("c.level='legacy' AND c.kind=?",bookRowId,kind);
    }

    public List<JSONObject> all(String kind) {
        return store.query("SELECT * FROM categories WHERE kind=? ORDER BY CASE level WHEN 'parent' THEN 0 WHEN 'child' THEN 1 ELSE 2 END,position_weight DESC,id", kind);
    }

    /** Hidden selections remain resolvable. Visibility is a picker filter, not a save gate. */
    public JSONObject rowForSelection(String kind, int parentSemanticId, int childSemanticId) {
        synchronized (store) {
            if (!kind.equals(parentSemanticId == 9 ? "income" : "expense")) throw new IllegalArgumentException("分类收支方向不匹配");
            if(childSemanticId < -1)throw new IllegalArgumentException("子分类标识无效");
            JSONObject row = childSemanticId > 0 ? child(parentSemanticId, childSemanticId) : parent(parentSemanticId);
            if (row == null || !kind.equals(row.optString("kind"))) return null;
            return row;
        }
    }

    public long defaultRowId(String kind, long bookRowId) {
        synchronized (store) {
            OriginalCategorySelection state = OriginalCategorySelection.empty();
            List<Integer> semanticIds = new ArrayList<>();
            for (JSONObject row : kind.equals("income") ? incomeTopLevel(bookRowId) : expenseParents(bookRowId)) semanticIds.add(row.optInt("semantic_id"));
            OriginalCategorySelection.CategoryPair pair = kind.equals("income")
                ? state.bindIncome(semanticIds).incomePair() : state.bindExpense(semanticIds).expensePair();
            if (semanticIds.isEmpty()) return 0;
            JSONObject row = rowForSelection(kind, pair.parentCategoryId, pair.childCategoryId);
            return row == null ? 0 : row.optLong("id");
        }
    }

    public boolean isHiddenInBook(long categoryRowId, long bookRowId) {
        return store.one("SELECT 1 hidden FROM category_hidden_books WHERE category_row_id=? AND book_row_id=?", "" + categoryRowId, "" + bookRowId) != null;
    }

    public void setHiddenInBook(long categoryRowId, long bookRowId, boolean hidden) {
        synchronized (store) {
            SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
            try {
                if (byRowId(categoryRowId) == null || store.book(bookRowId) == null) throw new IllegalArgumentException("分类或账本不存在");
                if (hidden) db.execSQL("INSERT OR IGNORE INTO category_hidden_books(category_row_id,book_row_id) VALUES(?,?)", new Object[]{categoryRowId, bookRowId});
                else db.delete("category_hidden_books", "category_row_id=? AND book_row_id=?", new String[]{"" + categoryRowId, "" + bookRowId});
                db.execSQL("UPDATE categories SET update_time=? WHERE id=?", new Object[]{System.currentTimeMillis(), categoryRowId});
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
        }
    }

    public long saveParent(long rowId, String name, String icon) { return save(rowId, name, icon, PARENT, 0, 0); }
    public long saveChild(long rowId, String name, String icon, int parentSemanticId, int parentChildSemanticId) {
        return save(rowId, name, icon, CHILD, parentSemanticId, parentChildSemanticId);
    }

    /** Existing rows keep their identity; moving/merging categories is a separate original action. */
    private long save(long rowId, String name, String icon, String level, int parentId, int parentChildId) {
        synchronized (store) {
            String trimmed = name == null ? "" : name.trim();
            if (trimmed.isEmpty() || trimmed.length() > 100 || icon == null || !icon.matches("[A-Za-z_][A-Za-z0-9_]*")) throw new IllegalArgumentException("分类名称或图标无效");
            SQLiteDatabase db = store.getWritableDatabase(); db.beginTransaction();
            try {
                JSONObject old = rowId > 0 ? byRowId(rowId) : null;
                if (rowId > 0 && (old == null || !level.equals(old.optString("level")))) throw new IllegalArgumentException("分类层级不匹配");
                if (CHILD.equals(level)) {
                    JSONObject incomeTop=parentChildId>0?child(9,parentChildId):null;
                    if (parent(parentId) == null || parentChildId < 0 || (parentId != 9 && parentChildId != 0)
                        || (parentChildId > 0 && (incomeTop == null || incomeTop.optInt("parent_child_semantic_id") != 0))) throw new IllegalArgumentException("父分类不存在");
                    if(old!=null&&parentChildId>0&&old.optInt("semantic_id")==parentChildId)throw new IllegalArgumentException("分类不能包含自身");
                    if (old != null && (old.optInt("parent_semantic_id") != parentId || old.optInt("parent_child_semantic_id") != parentChildId)) throw new IllegalArgumentException("本次编辑不能改变分类层级");
                }
                ContentValues values = new ContentValues(); values.put("name", trimmed); values.put("icon", icon); values.put("update_time", System.currentTimeMillis());
                if (old != null) { db.update("categories", values, "id=?", new String[]{"" + rowId}); db.setTransactionSuccessful(); return rowId; }
                int semanticId = nextSemanticId(level);
                values.put("level", level); values.put("semantic_id", semanticId);
                if (CHILD.equals(level)) values.put("parent_semantic_id", parentId);
                values.put("parent_child_semantic_id", parentChildId); values.put("category_type", OriginalCategories.TYPE_USER);
                values.put("kind", CHILD.equals(level) && parentId == 9 ? "income" : "expense");
                JSONObject maximum = CHILD.equals(level) ? store.one("SELECT COALESCE(MAX(position_weight),0) n FROM categories WHERE level='child' AND parent_semantic_id=?", "" + parentId)
                    : store.one("SELECT COALESCE(MAX(position_weight),0) n FROM categories WHERE level='parent'");
                values.put("position_weight", Math.addExact(maximum.optInt("n"), 1));
                long created = db.insertOrThrow("categories", null, values); db.setTransactionSuccessful(); return created;
            } finally { db.endTransaction(); }
        }
    }

    private int nextSemanticId(String level) {
        Random random = new Random(); int candidate;
        do { candidate = random.nextInt(Integer.MAX_VALUE); }
        while (candidate == 0 || store.one("SELECT id FROM categories WHERE level=? AND semantic_id=?", level, "" + candidate) != null);
        return candidate;
    }

    /** Compatibility FK bridge. Original selection uses AssetRepository's raw mapping and f.R. */
    public long rememberedAccountRowId(int parentSemanticId, int childSemanticId) {
        synchronized(store) {
            JSONObject row=store.one("SELECT account_row_id FROM category_accounts WHERE parent_semantic_id=? AND child_semantic_id=? AND (owner_semantic_id=? OR owner_semantic_id IS NULL) ORDER BY bridge_revision DESC,updated DESC,id DESC LIMIT 1",
                ""+parentSemanticId,""+childSemanticId,""+ReferenceStore.LOCAL_OFFLINE_OWNER);
            return row==null||row.isNull("account_row_id")?-1:row.optLong("account_row_id");
        }
    }

    /** Legacy-FK compatibility only; never fabricates the raw original asset ID or owner. */
    void learn(SQLiteDatabase db, int parentSemanticId, int childSemanticId, long accountRowId) {
        ContentValues values = new ContentValues(); values.put("parent_semantic_id", parentSemanticId); values.put("child_semantic_id", childSemanticId);
        if (accountRowId > 0) values.put("account_row_id", accountRowId); else values.putNull("account_row_id");
        values.put("updated", System.currentTimeMillis());
        values.put("bridge_revision",store.assetRepository().nextBridgeRevision());
        if(db.insertWithOnConflict("category_accounts", null, values, SQLiteDatabase.CONFLICT_REPLACE)<0)throw new IllegalStateException("分类账户学习未能保存");
    }

    /** Derives saved semantic columns without interpreting legacy names or checking hideBook. */
    void applySelection(JSONObject input, JSONObject category, ContentValues values) {
        if (LEGACY.equals(category.optString("level"))) { values.putNull("parent_semantic_id"); values.putNull("child_semantic_id"); return; }
        if(input.has("parent_semantic_id")&&!input.isNull("parent_semantic_id")&&!integerInRange(input,"parent_semantic_id",1,Integer.MAX_VALUE,false))throw new IllegalArgumentException("父分类标识无效");
        if(input.has("child_semantic_id")&&!input.isNull("child_semantic_id")&&!integerInRange(input,"child_semantic_id",-1,Integer.MAX_VALUE,false))throw new IllegalArgumentException("子分类标识无效");
        int parentId = PARENT.equals(category.optString("level")) ? category.optInt("semantic_id") : category.optInt("parent_semantic_id");
        int childId = CHILD.equals(category.optString("level")) ? category.optInt("semantic_id") : input.optInt("child_semantic_id", -1);
        if (PARENT.equals(category.optString("level")) && childId != -1 && childId != 0) throw new IllegalArgumentException("请选择父分类对应的子分类记录");
        if (input.has("parent_semantic_id") && !input.isNull("parent_semantic_id") && input.optInt("parent_semantic_id") != parentId) throw new IllegalArgumentException("父分类标识不匹配");
        if (CHILD.equals(category.optString("level")) && input.has("child_semantic_id") && !input.isNull("child_semantic_id") && input.optInt("child_semantic_id") != childId) throw new IllegalArgumentException("子分类标识不匹配");
        values.put("parent_semantic_id", parentId); values.put("child_semantic_id", childId);
    }

    /** Backup validation is independent of visibility: hidden selected categories remain valid. */
    void validateRestored() {
        java.util.Map<Long,JSONObject> categoryRows=new java.util.HashMap<>();
        for (JSONObject row : store.query("SELECT * FROM categories")) {
            categoryRows.put(row.optLong("id"),row);
            String level = row.optString("level"), kind = row.optString("kind");
            if (!java.util.Arrays.asList("expense", "income").contains(kind) || !java.util.Arrays.asList(LEGACY, PARENT, CHILD).contains(level)) throw new IllegalArgumentException("备份分类类型无效");
            if(!integerInRange(row,"position_weight",Integer.MIN_VALUE,Integer.MAX_VALUE,false)
                ||!integerInRange(row,"parent_child_semantic_id",0,Integer.MAX_VALUE,false)
                ||!integerInRange(row,"parent_semantic_id",1,Integer.MAX_VALUE,true)
                ||!integerInRange(row,"archived",0,1,false))throw new IllegalArgumentException("备份分类数值无效");
            if (LEGACY.equals(level)) {
                if (!row.isNull("semantic_id") || !row.isNull("parent_semantic_id") || row.optInt("parent_child_semantic_id")!=0 || !integerInRange(row,"category_type",0,0,false)) throw new IllegalArgumentException("旧分类不应携带推测的原始标识");
                continue;
            }
            if(!integerInRange(row,"semantic_id",1,Integer.MAX_VALUE,false)||!integerInRange(row,"category_type",1,2,false))throw new IllegalArgumentException("备份分类标识无效");
            int parentId = PARENT.equals(level) ? row.optInt("semantic_id") : row.optInt("parent_semantic_id");
            if (!kind.equals(parentId == 9 ? "income" : "expense")) throw new IllegalArgumentException("备份分类收支方向无效");
            if (PARENT.equals(level) && (!row.isNull("parent_semantic_id") || row.optInt("parent_child_semantic_id") != 0)) throw new IllegalArgumentException("备份父分类层级无效");
            if (CHILD.equals(level) && (row.isNull("parent_semantic_id") || parent(parentId) == null || row.optInt("parent_child_semantic_id") < 0 || (parentId != 9 && row.optInt("parent_child_semantic_id") != 0))) throw new IllegalArgumentException("备份子分类层级无效");
            if (CHILD.equals(level) && row.optInt("parent_child_semantic_id") > 0) {
                JSONObject top=child(9,row.optInt("parent_child_semantic_id"));
                if(top==null||top.optInt("parent_child_semantic_id")!=0||top.optInt("semantic_id")==row.optInt("semantic_id"))throw new IllegalArgumentException("备份收入子分类的上级无效");
            }
        }
        for (JSONObject transaction : store.query("SELECT * FROM transactions WHERE kind<>'transfer'")) {
            JSONObject category = categoryRows.get(transaction.optLong("category_id"));
            // Original Bills can retain raw category pairs after a category disappears.
            // An unmapped bridge stays NULL instead of silently selecting a fallback.
            if(category==null&&transaction.isNull("category_id")&&OriginalBillRepository.ORIGINAL.equals(transaction.optString("bill_origin"))) {
                if(transaction.isNull("parent_semantic_id")||transaction.isNull("child_semantic_id")
                        ||transaction.optLong("parent_semantic_id")!=transaction.optLong("bill_parent_category_id")
                        ||transaction.optLong("child_semantic_id")!=transaction.optLong("bill_child_category_id")
                        ||!transaction.optString("kind").equals(transaction.optInt("bill_parent_category_id")==9?"income":"expense"))
                    throw new IllegalArgumentException("原版账单的未映射分类无效");
                continue;
            }
            if (category == null || !transaction.optString("kind").equals(category.optString("kind"))) throw new IllegalArgumentException("备份账单分类关联无效");
            ContentValues expected = new ContentValues(); applySelection(transaction, category, expected);
            for (String key : new String[]{"parent_semantic_id", "child_semantic_id"}) {
                Long value = expected.getAsLong(key);
                if (value == null ? !transaction.isNull(key) : transaction.isNull(key) || transaction.optLong(key) != value.longValue()) throw new IllegalArgumentException("备份账单分类标识不匹配");
            }
        }
        for (JSONObject mapping : store.query("SELECT * FROM category_accounts")) {
            if(!integerInRange(mapping,"parent_semantic_id",1,Integer.MAX_VALUE,false)||!integerInRange(mapping,"child_semantic_id",-1,Integer.MAX_VALUE,false))throw new IllegalArgumentException("备份分类账户标识无效");
            if (rowForSelection(mapping.optInt("parent_semantic_id") == 9 ? "income" : "expense", mapping.optInt("parent_semantic_id"), mapping.optInt("child_semantic_id")) == null
                    && mapping.isNull("category_asset_semantic_id")) throw new IllegalArgumentException("备份分类账户映射无效");
        }
    }

    private static boolean integerInRange(JSONObject row,String key,long minimum,long maximum,boolean nullable) {
        if(row.isNull(key))return nullable;
        Object value=row.opt(key);
        if(!(value instanceof Long)&&!(value instanceof Integer))return false;
        long number=((Number)value).longValue();return number>=minimum&&number<=maximum;
    }
}
