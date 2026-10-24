package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONObject;

/**
 * Offline budgets in the book's minor currency units. Original Budget exposes
 * bookId/year/month/type/startTime/endTime/num/budgetName, and CategoryBudget
 * exposes parentCategory/childCategory/num/budgetId. Their resources provide
 * monthly, date-range and parent/child category views. Local row IDs, integer
 * money and end-exclusive ranges are explicit host storage, not LitePal copies.
 * Unrecovered addNum rollover/automatic increase rules are not guessed here.
 */
public final class LocalBudgets {
    public static final String TABLE = "local_budgets";
    public static final String CATEGORY_TABLE = "local_category_budgets";
    private static final long MAX_AMOUNT = 99_999_999_999_999L;
    private final ReferenceStore store;

    public LocalBudgets(ReferenceStore store) {
        if (store == null) throw new IllegalArgumentException("预算数据库不能为空");
        this.store = store;
    }

    /** Called once by schema 11; old scalar budgets have no historical month identity. */
    public static void installV11Schema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE local_budgets(id INTEGER PRIMARY KEY AUTOINCREMENT,book_id INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE,currency TEXT NOT NULL,name TEXT NOT NULL,period_type TEXT NOT NULL CHECK(period_type IN ('month','custom')),year INTEGER NOT NULL,month INTEGER NOT NULL,start_time INTEGER NOT NULL,end_time INTEGER NOT NULL CHECK(end_time>start_time),amount INTEGER NOT NULL CHECK(typeof(amount)='integer' AND amount>=0 AND amount<=99999999999999),created INTEGER NOT NULL,updated INTEGER NOT NULL,CHECK((period_type='month' AND year BETWEEN 1900 AND 9998 AND month BETWEEN 1 AND 12) OR (period_type='custom' AND year=0 AND month=0)))");
        db.execSQL("CREATE UNIQUE INDEX local_budgets_month ON local_budgets(book_id,year,month) WHERE period_type='month'");
        db.execSQL("CREATE INDEX local_budgets_period ON local_budgets(book_id,period_type,start_time)");
        db.execSQL("CREATE TABLE local_category_budgets(id INTEGER PRIMARY KEY AUTOINCREMENT,budget_id INTEGER NOT NULL REFERENCES local_budgets(id) ON DELETE CASCADE,category_id INTEGER NOT NULL REFERENCES categories(id),amount INTEGER NOT NULL CHECK(typeof(amount)='integer' AND amount>=0 AND amount<=99999999999999),updated INTEGER NOT NULL,UNIQUE(budget_id,category_id))");
        migrateLegacy(db);
    }

    /** Restore versions <=10 only: the old scalar identifies no earlier month. */
    public static void migrateLegacy(SQLiteDatabase db) {
        Calendar now = Calendar.getInstance();
        long[] range = monthRange(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
        long stamp = System.currentTimeMillis();
        db.execSQL("INSERT OR IGNORE INTO local_budgets(book_id,currency,name,period_type,year,month,start_time,end_time,amount,created,updated) SELECT id,currency,'月预算','month',?,?,?,?,budget,?,? FROM books WHERE budget>0",
                new Object[]{now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, range[0], range[1], stamp, stamp});
    }

    public static JSONObject month(ReferenceStore store, long bookId, long time) {
        return new LocalBudgets(store).month(bookId, time);
    }

    public JSONObject month(long bookId, long time) {
        Calendar selected = Calendar.getInstance(); selected.setTimeInMillis(time);
        return month(bookId, selected.get(Calendar.YEAR), selected.get(Calendar.MONTH) + 1);
    }

    /** An unconfigured month is a virtual zero budget; merely viewing it writes nothing. */
    public JSONObject month(long bookId, int year, int month) {
        synchronized (store) {
            JSONObject book = book(bookId);
            long[] range = monthRange(year, month);
            JSONObject row = store.one("SELECT * FROM local_budgets WHERE book_id=? AND period_type='month' AND year=? AND month=?",
                    "" + bookId, "" + year, "" + month);
            if (row == null) row = Json.object("id", 0, "book_id", bookId, "currency", book.optString("currency"),
                    "name", "月预算", "period_type", "month", "year", year, "month", month,
                    "start_time", range[0], "end_time", range[1], "amount", 0);
            return decorate(row);
        }
    }

    public JSONObject snapshot(long budgetId) {
        synchronized (store) { return decorate(requireBudget(budgetId)); }
    }

    public List<JSONObject> customs(long bookId) {
        synchronized (store) {
            book(bookId);
            List<JSONObject> result = new ArrayList<>();
            for (JSONObject row : store.query("SELECT * FROM local_budgets WHERE book_id=? AND period_type='custom' ORDER BY start_time DESC,id DESC", "" + bookId))
                result.add(decorate(row));
            return result;
        }
    }

    public long saveMonth(long bookId, int year, int month, long amount) {
        return AccountingCommitScope.run(store, () -> {
            JSONObject book = book(bookId); money(amount); long[] range = monthRange(year, month);
            JSONObject prior = store.one("SELECT id FROM local_budgets WHERE book_id=? AND period_type='month' AND year=? AND month=?", "" + bookId, "" + year, "" + month);
            ContentValues value = values(bookId, book.optString("currency"), "月预算", "month", range[0], range[1], amount);
            value.put("year", year); value.put("month", month);
            long id = write(prior == null ? 0 : prior.optLong("id"), value);
            // Compatibility for existing current-month overview consumers only.
            Calendar now = Calendar.getInstance();
            if (now.get(Calendar.YEAR) == year && now.get(Calendar.MONTH) + 1 == month)
                store.getWritableDatabase().execSQL("UPDATE books SET budget=? WHERE id=?", new Object[]{amount, bookId});
            return id;
        });
    }

    /** Start inclusive and end exclusive, both local midnight from the date picker. */
    public long saveCustom(long id, long bookId, String name, long start, long end, long amount) {
        return AccountingCommitScope.run(store, () -> {
            JSONObject book = book(bookId); money(amount); validateRange(start, end);
            String title = name == null ? "" : name.trim();
            if (title.isEmpty() || title.length() > 100) throw new IllegalArgumentException("预算名称需要1至100个字符");
            if (id != 0) {
                JSONObject old = requireBudget(id);
                if (!old.optString("period_type").equals("custom") || old.optLong("book_id") != bookId)
                    throw new IllegalArgumentException("不能改变预算所属账本或类型");
            }
            ContentValues value = values(bookId, book.optString("currency"), title, "custom", start, end, amount);
            value.put("year", 0); value.put("month", 0);
            return write(id, value);
        });
    }

    public long saveCategory(long budgetId, long categoryId, long amount) {
        return AccountingCommitScope.run(store, () -> {
            requireBudget(budgetId); expenseCategory(categoryId); money(amount);
            JSONObject prior = store.one("SELECT id FROM local_category_budgets WHERE budget_id=? AND category_id=?", "" + budgetId, "" + categoryId);
            ContentValues values = new ContentValues(); values.put("budget_id", budgetId); values.put("category_id", categoryId);
            values.put("amount", amount); values.put("updated", System.currentTimeMillis());
            if (prior != null) {
                AccountingCommitScope.requireChanged(store, store.getWritableDatabase().update(CATEGORY_TABLE, values, "id=?", new String[]{"" + prior.optLong("id")}));
                return prior.optLong("id");
            }
            long id = store.getWritableDatabase().insertOrThrow(CATEGORY_TABLE, null, values);
            if (id <= 0) throw new IllegalStateException("分类预算保存失败");
            return id;
        });
    }

    public void removeCategory(long budgetId, long categoryId) {
        AccountingCommitScope.run(store, () -> {
            requireBudget(budgetId);
            store.getWritableDatabase().delete(CATEGORY_TABLE, "budget_id=? AND category_id=?", new String[]{"" + budgetId, "" + categoryId});
            return null;
        });
    }

    public void remove(long budgetId) {
        AccountingCommitScope.run(store, () -> {
            JSONObject prior = requireBudget(budgetId);
            store.getWritableDatabase().delete(CATEGORY_TABLE, "budget_id=?", new String[]{"" + budgetId});
            AccountingCommitScope.requireChanged(store, store.getWritableDatabase().delete(TABLE, "id=?", new String[]{"" + budgetId}));
            Calendar now = Calendar.getInstance();
            if (prior.optString("period_type").equals("month") && prior.optInt("year") == now.get(Calendar.YEAR)
                    && prior.optInt("month") == now.get(Calendar.MONTH) + 1)
                store.getWritableDatabase().execSQL("UPDATE books SET budget=0 WHERE id=?", new Object[]{prior.optLong("book_id")});
            return null;
        });
    }

    public List<JSONObject> categories(long budgetId) {
        synchronized (store) {
            JSONObject budget = requireBudget(budgetId); List<JSONObject> result = new ArrayList<>();
            for (JSONObject row : store.query("SELECT b.*,c.name,c.level,c.semantic_id,c.parent_semantic_id,c.icon,c.archived FROM local_category_budgets b JOIN categories c ON c.id=b.category_id WHERE b.budget_id=? ORDER BY c.position_weight DESC,c.id", "" + budgetId)) {
                long spent = spending(budget, row.optLong("category_id"));
                Json.put(row, "spent", spent); Json.put(row, "remaining", row.optLong("amount") - spent);
                Json.put(row, "over", spent > row.optLong("amount")); result.add(row);
            }
            return result;
        }
    }

    /** Includes visible expense parents, children and explicit legacy flat categories. */
    public List<JSONObject> availableCategories(long bookId) {
        synchronized (store) {
            book(bookId);
            return store.query("SELECT c.*,p.name parent_name FROM categories c LEFT JOIN categories p ON c.level='child' AND p.level='parent' AND p.semantic_id=c.parent_semantic_id WHERE c.kind='expense' AND c.archived=0 AND NOT EXISTS(SELECT 1 FROM category_hidden_books h WHERE h.category_row_id=c.id AND h.book_row_id=?) ORDER BY COALESCE(p.position_weight,c.position_weight) DESC,CASE WHEN c.level='child' THEN 1 ELSE 0 END,c.position_weight DESC,c.id", "" + bookId);
        }
    }

    /** No UI pagination cap: every bill contributing to this budget can be inspected. */
    public List<JSONObject> bills(long budgetId, long categoryId) {
        synchronized (store) { return bills(requireBudget(budgetId), categoryId); }
    }

    public List<JSONObject> billsForMonth(long bookId, int year, int month, long categoryId) {
        synchronized (store) { return bills(month(bookId, year, month), categoryId); }
    }

    private List<JSONObject> bills(JSONObject budget, long categoryId) {
        Query query = query(budget, categoryId);
        return store.query("SELECT t.*,c.name category,c.icon category_icon,a.name account,CASE WHEN t.kind='expense' THEN t.amount+t.fee ELSE t.fee END budget_cost FROM transactions t LEFT JOIN categories c ON c.id=t.category_id LEFT JOIN accounts a ON a.id=t.account_id" + query.where + " ORDER BY t.time DESC,t.id DESC", query.args.toArray(new String[0]));
    }

    private long spending(JSONObject budget, long categoryId) {
        Query query = query(budget, categoryId);
        return store.scalar("SELECT COALESCE(SUM(CASE WHEN t.kind='expense' THEN t.amount+t.fee ELSE t.fee END),0) n FROM transactions t LEFT JOIN categories c ON c.id=t.category_id" + query.where, query.args.toArray(new String[0]));
    }

    private Query query(JSONObject budget, long categoryId) {
        Query result = new Query();
        result.where = " WHERE t.book_id=? AND t.currency=? AND t.deleted=0 AND t.not_budget=0 AND t.time>=? AND t.time<? AND (t.kind='expense' OR t.fee>0)";
        result.args.add("" + budget.optLong("book_id")); result.args.add(budget.optString("currency"));
        result.args.add("" + budget.optLong("start_time")); result.args.add("" + budget.optLong("end_time"));
        if (categoryId > 0) {
            JSONObject category = expenseCategory(categoryId);
            // Parent budgets contain their direct parent bills and all child bills.
            if (category.optString("level").equals(CategoryRepository.PARENT)) {
                result.where += " AND (t.category_id=? OR t.parent_semantic_id=? OR (c.level='child' AND c.parent_semantic_id=?))";
                result.args.add("" + categoryId); result.args.add("" + category.optInt("semantic_id")); result.args.add("" + category.optInt("semantic_id"));
            } else if (category.optString("level").equals(CategoryRepository.CHILD)) {
                result.where += " AND (t.category_id=? OR (t.parent_semantic_id=? AND t.child_semantic_id=?))";
                result.args.add("" + categoryId); result.args.add("" + category.optInt("parent_semantic_id")); result.args.add("" + category.optInt("semantic_id"));
            } else { result.where += " AND t.category_id=?"; result.args.add("" + categoryId); }
        }
        return result;
    }

    private static final class Query { String where; final List<String> args = new ArrayList<>(); }

    private JSONObject decorate(JSONObject original) {
        JSONObject row = Json.copy(original); long spent = spending(row, 0), limit = row.optLong("amount");
        Json.put(row, "spent", spent); Json.put(row, "remaining", limit - spent); Json.put(row, "over", row.optLong("id") > 0 && spent > limit);
        Json.put(row, "configured", row.optLong("id") > 0); return row;
    }

    private JSONObject requireBudget(long id) {
        JSONObject row = store.one("SELECT * FROM local_budgets WHERE id=?", "" + id);
        if (row == null) throw new IllegalArgumentException("预算不存在或已删除");
        JSONObject book = book(row.optLong("book_id"));
        if (!book.optString("currency").equals(row.optString("currency"))) throw new IllegalArgumentException("预算与账本币种不一致");
        return row;
    }

    private JSONObject book(long id) {
        JSONObject row = store.book(id);
        if (row == null || row.optInt("archived") != 0) throw new IllegalArgumentException("请选择有效账本");
        Money.scale(row.optString("currency")); return row;
    }

    private JSONObject expenseCategory(long id) {
        JSONObject row = store.categoryRepository().byRowId(id);
        if (row == null || !row.optString("kind").equals("expense")) throw new IllegalArgumentException("分类预算只能使用支出分类");
        return row;
    }

    private ContentValues values(long book, String currency, String name, String type, long start, long end, long amount) {
        ContentValues result = new ContentValues(); result.put("book_id", book); result.put("currency", currency);
        result.put("name", name); result.put("period_type", type); result.put("start_time", start); result.put("end_time", end);
        result.put("amount", amount); result.put("updated", System.currentTimeMillis()); return result;
    }

    private long write(long id, ContentValues values) {
        if (id > 0) AccountingCommitScope.requireChanged(store, store.getWritableDatabase().update(TABLE, values, "id=?", new String[]{"" + id}));
        else { values.put("created", System.currentTimeMillis()); id = store.getWritableDatabase().insertOrThrow(TABLE, null, values); if (id <= 0) throw new IllegalStateException("预算保存失败"); }
        return id;
    }

    private static void money(long value) { if (value < 0 || value > MAX_AMOUNT) throw new IllegalArgumentException("预算金额无效"); }
    private static void validateRange(long start, long end) {
        Calendar min = Calendar.getInstance(); min.clear(); min.set(1900, Calendar.JANUARY, 1);
        if (start < min.getTimeInMillis() || end <= start) throw new IllegalArgumentException("请选择1900年以后的有效起止日期");
        Calendar max = Calendar.getInstance(); max.clear(); max.set(9999, Calendar.JANUARY, 1);
        if (end > max.getTimeInMillis()) throw new IllegalArgumentException("预算日期过大");
    }
    public static long[] monthRange(int year, int month) {
        if (year < 1900 || year > 9998 || month < 1 || month > 12) throw new IllegalArgumentException("请选择有效年月");
        Calendar first = Calendar.getInstance(); first.clear(); first.set(year, month - 1, 1);
        long start = first.getTimeInMillis(); first.add(Calendar.MONTH, 1); return new long[]{start, first.getTimeInMillis()};
    }

    /** Schema-11 backups must not silently remap local IDs or money to other books. */
    public static void validateRestored(ReferenceStore store) {
        synchronized (store) {
            if (store.scalar("SELECT COUNT(*) n FROM local_budgets b LEFT JOIN books k ON k.id=b.book_id WHERE typeof(b.id)<>'integer' OR b.id<=0 OR k.id IS NULL OR b.currency<>k.currency OR typeof(b.amount)<>'integer' OR b.amount<0 OR b.amount>99999999999999 OR length(trim(b.name))=0 OR length(b.name)>100 OR b.period_type NOT IN ('month','custom') OR typeof(b.start_time)<>'integer' OR typeof(b.end_time)<>'integer' OR b.created<0 OR b.updated<0") > 0)
                throw new IllegalArgumentException("备份预算字段无效");
            for (JSONObject row : store.query("SELECT * FROM local_budgets")) {
                Money.scale(row.optString("currency")); money(row.optLong("amount")); validateRange(row.optLong("start_time"), row.optLong("end_time"));
                if (row.optString("period_type").equals("month")) {
                    long[] range = monthRange(row.optInt("year"), row.optInt("month"));
                    // A backup can be restored in another time zone. Preserve its
                    // recorded range; do not reject a valid month for a TZ offset.
                    if (Math.abs(range[0] - row.optLong("start_time")) > 2 * 86_400_000L
                            || Math.abs(range[1] - row.optLong("end_time")) > 2 * 86_400_000L)
                        throw new IllegalArgumentException("备份月预算的日期不一致");
                } else if (row.optInt("year") != 0 || row.optInt("month") != 0) throw new IllegalArgumentException("备份自定义预算年月无效");
            }
            if (store.scalar("SELECT COUNT(*) n FROM local_category_budgets x LEFT JOIN local_budgets b ON b.id=x.budget_id LEFT JOIN categories c ON c.id=x.category_id WHERE typeof(x.id)<>'integer' OR x.id<=0 OR b.id IS NULL OR c.id IS NULL OR c.kind<>'expense' OR typeof(x.amount)<>'integer' OR x.amount<0 OR x.amount>99999999999999 OR x.updated<0") > 0)
                throw new IllegalArgumentException("备份分类预算的关联或金额无效");
        }
    }
}
