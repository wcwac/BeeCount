package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Local experimental schema. Does not open or modify any BeeCount database. */
public final class ReferenceStore extends SQLiteOpenHelper {
    /** Offline experimental identity, not a recovered original User default. */
    public static final int LOCAL_OFFLINE_OWNER = 0;
    private final CategoryRepository categoryRepository = new CategoryRepository(this);
    private final AssetRepository assetRepository = new AssetRepository(this);
    private final BookRepository bookRepository = new BookRepository(this);
    public ReferenceStore(Context context) { this(context,"yimu_reference.sqlite"); }
    ReferenceStore(Context context,String name) { super(context.getApplicationContext(), name, null, 4); }
    @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE books(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,currency TEXT NOT NULL DEFAULT 'CNY',budget INTEGER NOT NULL DEFAULT 0,archived INTEGER NOT NULL DEFAULT 0,created INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE accounts(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,type TEXT NOT NULL DEFAULT 'cash',currency TEXT NOT NULL DEFAULT 'CNY',opening INTEGER NOT NULL DEFAULT 0,archived INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE categories(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,kind TEXT NOT NULL,icon TEXT NOT NULL,archived INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE tags(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE)");
        db.execSQL("CREATE TABLE candidates(id TEXT PRIMARY KEY,payload TEXT NOT NULL,package_name TEXT NOT NULL,book_id INTEGER NOT NULL REFERENCES books(id),created INTEGER NOT NULL,state TEXT NOT NULL DEFAULT 'PENDING',transaction_id INTEGER)");
        db.execSQL("CREATE TABLE transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,book_id INTEGER NOT NULL REFERENCES books(id),kind TEXT NOT NULL CHECK(kind IN ('expense','income','transfer')),amount INTEGER NOT NULL CHECK(amount>0),currency TEXT NOT NULL,category_id INTEGER REFERENCES categories(id),account_id INTEGER REFERENCES accounts(id),to_account_id INTEGER REFERENCES accounts(id),remark TEXT NOT NULL DEFAULT '',merchant TEXT NOT NULL DEFAULT '',origin TEXT NOT NULL DEFAULT '',asset_text TEXT NOT NULL DEFAULT '',discount INTEGER NOT NULL DEFAULT 0 CHECK(discount>=0),fee INTEGER NOT NULL DEFAULT 0 CHECK(fee>=0),time INTEGER NOT NULL,created INTEGER NOT NULL,not_total INTEGER NOT NULL DEFAULT 0,not_budget INTEGER NOT NULL DEFAULT 0,deleted INTEGER NOT NULL DEFAULT 0,financial_type TEXT NOT NULL DEFAULT '',settled INTEGER NOT NULL DEFAULT 0,attachments TEXT NOT NULL DEFAULT '[]',candidate_id TEXT UNIQUE REFERENCES candidates(id))");
        db.execSQL("CREATE INDEX transactions_book_time ON transactions(book_id,deleted,time)");
        db.execSQL("ALTER TABLE transactions ADD COLUMN related_id INTEGER REFERENCES transactions(id)");
        db.execSQL("ALTER TABLE transactions ADD COLUMN import_key TEXT");
        db.execSQL("CREATE UNIQUE INDEX transactions_import_key ON transactions(import_key) WHERE import_key IS NOT NULL");
        db.execSQL("CREATE INDEX candidates_state ON candidates(state,created)");
        db.execSQL("CREATE TABLE transaction_tags(transaction_id INTEGER REFERENCES transactions(id) ON DELETE CASCADE,tag_id INTEGER REFERENCES tags(id),PRIMARY KEY(transaction_id,tag_id))");
        db.execSQL("CREATE TABLE rules(id INTEGER PRIMARY KEY AUTOINCREMENT,merchant TEXT NOT NULL UNIQUE,category_id INTEGER REFERENCES categories(id),book_id INTEGER REFERENCES books(id),account_id INTEGER REFERENCES accounts(id))");
        db.execSQL("CREATE TABLE plans(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,interval TEXT NOT NULL,payload TEXT NOT NULL,next_due INTEGER NOT NULL,enabled INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE goals(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,target INTEGER NOT NULL CHECK(target>0),saved INTEGER NOT NULL DEFAULT 0,created INTEGER NOT NULL)");
        CategoryRepository.installV2Schema(db);
        CategoryRepository.bootstrap(db);
        ReferenceSchemaV3.install(db);
        ReferenceSchemaV4.install(db);
        BookRepository.bootstrapNewLocalOwner(db, LOCAL_OFFLINE_OWNER, System.currentTimeMillis());
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1 || oldVersion > 3 || newVersion != 4) throw new IllegalStateException("未知数据版本");
        // ALTER preserves every local category PK and its transaction/rule/payload references.
        // The new columns default to legacy/NULL; names never imply original semantic IDs.
        if (oldVersion < 2) CategoryRepository.installV2Schema(db);
        CategoryRepository.bootstrap(db);
        if (oldVersion < 3) ReferenceSchemaV3.install(db);
        ReferenceSchemaV4.install(db);
    }
    @Override public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (db.isReadOnly()) return;
        db.beginTransaction();
        try { CategoryRepository.bootstrap(db); db.setTransactionSuccessful(); }
        finally { db.endTransaction(); }
    }
    public CategoryRepository categoryRepository() { return categoryRepository; }
    public AssetRepository assetRepository() { return assetRepository; }
    public BookRepository bookRepository() { return bookRepository; }

    /** Explicit picker action replaces a raw reference; an unrelated edit preserves unknown NULLs. */
    public synchronized void selectAccountForInput(JSONObject input,String field,long rowId,boolean automatic) {
        if(!field.equals("account_id")&&!field.equals("to_account_id"))throw new IllegalArgumentException("账户字段无效");
        String raw=field.equals("account_id")?"raw_asset_semantic_id":"raw_to_asset_semantic_id";
        JSONObject account=rowId>0?assetRepository.byRowId(rowId):null;
        if(rowId>0&&account==null)throw new IllegalArgumentException("账户不存在");
        Json.put(input,field,rowId>0?rowId:0);
        if(account!=null&&AssetRepository.ORIGINAL.equals(account.optString("asset_origin"))) {
            Json.put(input,raw,account.optLong("semantic_id"));Json.put(input,"owner_semantic_id",account.optInt("owner_semantic_id"));
        } else {
            Json.put(input,raw,account==null?(automatic?-1L:0L):null);
            if(input.isNull("owner_semantic_id"))Json.put(input,"owner_semantic_id",LOCAL_OFFLINE_OWNER);
        }
    }

    public synchronized List<JSONObject> query(String sql, String... args) {
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, args)) {
            List<JSONObject> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                JSONObject row = new JSONObject();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    Object value = switch (cursor.getType(i)) {
                        case Cursor.FIELD_TYPE_NULL -> null;
                        case Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(i);
                        case Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(i);
                        default -> cursor.getString(i);
                    };
                    Json.put(row, cursor.getColumnName(i), value);
                }
                result.add(row);
            }
            return result;
        }
    }
    public synchronized JSONObject one(String sql, String... args) { List<JSONObject> rows = query(sql, args); return rows.isEmpty() ? null : rows.get(0); }
    public synchronized long scalar(String sql, String... args) { JSONObject row = one(sql, args); return row == null ? 0 : row.optLong("n"); }
    public List<JSONObject> books() { return query("SELECT * FROM books WHERE archived=0 ORDER BY id"); }
    public JSONObject book(long id) { return one("SELECT * FROM books WHERE id=?", "" + id); }
    public List<JSONObject> accounts() {
        List<JSONObject> result = query("SELECT * FROM accounts WHERE archived=0 ORDER BY id");
        for (JSONObject row : result) {
            Json.put(row, "balance", accountBalance(row.optLong("id")));
            if (AssetRepository.ORIGINAL.equals(row.optString("asset_origin")) && row.optString("currency").isEmpty())
                Json.put(row, "currency", "CNY"); // Host display only; asset_currency stays raw/nullable.
        }
        return result;
    }
    /** Original picker predicate/order plus explicitly retained legacy experiment accounts. */
    public synchronized List<JSONObject> selectableAccounts(long bookRowId) {
        List<JSONObject> result=new ArrayList<>();JSONObject book=book(bookRowId);
        if(book!=null&&!book.isNull("semantic_id")) {
            for(JSONObject row:assetRepository.picker(LOCAL_OFFLINE_OWNER,book.optLong("semantic_id")))
                if(row.optInt("archived")==0)result.add(row);
        }
        result.addAll(assetRepository.legacyPicker());
        for(JSONObject row:result) {
            if(AssetRepository.ORIGINAL.equals(row.optString("asset_origin"))&&row.optString("currency").isEmpty())Json.put(row,"currency","CNY");
            Json.put(row,"balance",accountBalance(row.optLong("id")));
        }
        return result;
    }

    /** Host default/rule lookup; display currency never overwrites raw asset_currency. */
    public synchronized JSONObject accountInDisplayCurrency(long rowId,String currency) {
        return one("SELECT * FROM accounts WHERE id=? AND archived=0 AND (CASE WHEN asset_origin='original' AND currency='' THEN 'CNY' ELSE currency END)=?", ""+rowId,currency);
    }
    public synchronized long accountBalance(long id) {
        JSONObject row = one("SELECT * FROM accounts WHERE id=?", "" + id);
        if (row == null) return 0;
        if (AssetRepository.ORIGINAL.equals(row.optString("asset_origin"))) {
            double amount = new OriginalFinancialOrchestrator(this, row.optInt("owner_semantic_id"), LOCAL_OFFLINE_OWNER)
                    .readBalance(row.optLong("semantic_id"));
            String currency = row.optString("currency");
            if (currency.isEmpty()) currency = "CNY"; // Explicit host display currency; raw nullable field remains intact.
            return java.math.BigDecimal.valueOf(amount).movePointRight(Money.scale(currency))
                    .setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
        }
        return row.optLong("opening") + scalar("SELECT COALESCE(SUM(CASE WHEN account_id=? THEN CASE WHEN kind='income' THEN amount-fee ELSE -amount-fee END ELSE 0 END + CASE WHEN kind='transfer' AND to_account_id=? THEN amount ELSE 0 END),0) n FROM transactions WHERE deleted=0", "" + id, "" + id);
    }
    /** Compatibility list: original expense parents or top-level income children in the first book. */
    public List<JSONObject> categories(String kind) { synchronized(this) { long book=firstBookId(); return kind.equals("income")?categoryRepository.incomeTopLevel(book):categoryRepository.expenseParents(book); } }
    public long defaultCategory(String kind) { synchronized(this) { return defaultCategory(kind,firstBookId()); } }
    public long defaultCategory(String kind,long bookRowId) { return categoryRepository.defaultRowId(kind,bookRowId); }
    private long firstBookId() { List<JSONObject> all=books();return all.isEmpty()?0:all.get(0).optLong("id"); }
    public JSONObject transaction(long id) { return one("SELECT t.*,c.name category,c.icon category_icon,a.name account,b.name book_name FROM transactions t LEFT JOIN categories c ON c.id=t.category_id LEFT JOIN accounts a ON a.id=t.account_id JOIN books b ON b.id=t.book_id WHERE t.id=?", "" + id); }
    public List<JSONObject> transactions(long book, long start, long end, String search, boolean trash) {
        String filter = " WHERE t.book_id=? AND t.deleted=? AND t.time>=? AND t.time<?";
        List<String> args = new ArrayList<>(java.util.Arrays.asList("" + book, trash ? "1" : "0", "" + start, "" + end));
        if (search != null && !search.trim().isEmpty()) { filter += " AND (t.remark LIKE ? ESCAPE '\\' OR t.merchant LIKE ? ESCAPE '\\' OR c.name LIKE ? ESCAPE '\\')"; String value = "%" + search.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%"; args.add(value); args.add(value); args.add(value); }
        return query("SELECT t.*,c.name category,c.icon category_icon,a.name account FROM transactions t LEFT JOIN categories c ON c.id=t.category_id LEFT JOIN accounts a ON a.id=t.account_id" + filter + " ORDER BY t.time DESC,t.id DESC LIMIT 2000", args.toArray(new String[0]));
    }
    public JSONObject totals(long book, long start, long end) {
        return one("SELECT COALESCE(SUM(CASE WHEN kind='income' AND not_total=0 THEN amount ELSE 0 END),0) income,COALESCE(SUM(CASE WHEN not_total=0 THEN CASE WHEN kind='expense' THEN amount+fee ELSE fee END ELSE 0 END),0) pay,COALESCE(SUM(CASE WHEN not_budget=0 THEN CASE WHEN kind='expense' THEN amount+fee ELSE fee END ELSE 0 END),0) budget_pay,COUNT(*) count FROM transactions WHERE book_id=? AND deleted=0 AND time>=? AND time<?", "" + book, "" + start, "" + end);
    }
    public List<JSONObject> categoryTotals(long book, long start, long end, String kind) {
        return query("SELECT COALESCE(c.name,'其他') name,COALESCE(c.icon,'ic_other') icon,SUM(t.amount) amount,COUNT(*) count FROM transactions t LEFT JOIN categories c ON c.id=t.category_id WHERE t.book_id=? AND t.deleted=0 AND t.not_total=0 AND t.kind=? AND t.time>=? AND t.time<? GROUP BY t.category_id ORDER BY amount DESC", "" + book, kind, "" + start, "" + end);
    }

    public synchronized long saveTransaction(JSONObject input, long editingId, String candidateId) {
        JSONObject old = editingId > 0 ? transaction(editingId) : null;
        if (old != null && OriginalBillRepository.ORIGINAL.equals(old.optString("bill_origin")))
            return saveOriginalTransaction(input, editingId, candidateId);
        JSONObject selectedAccount = input.optLong("account_id") > 0 ? assetRepository.byRowId(input.optLong("account_id")) : null;
        boolean originalAccount = selectedAccount != null && AssetRepository.ORIGINAL.equals(selectedAccount.optString("asset_origin"));
        if (editingId == 0 && usesOriginalCreation(input))
            return saveOriginalTransaction(input, 0, candidateId);
        JSONObject destination = input.optLong("to_account_id") > 0 ? assetRepository.byRowId(input.optLong("to_account_id")) : null;
        if (originalAccount || destination != null && AssetRepository.ORIGINAL.equals(destination.optString("asset_origin")))
            throw new IllegalArgumentException("此类旧账单或金融操作尚未接入原版账户流水，请使用普通收支记账");
        return saveLegacyTransaction(input, editingId, candidateId);
    }

    private boolean usesOriginalCreation(JSONObject input) {
        JSONObject book=book(input.optLong("book_id")),category=categoryRepository.byRowId(input.optLong("category_id"));
        JSONObject account=input.optLong("account_id")>0?assetRepository.byRowId(input.optLong("account_id")):null;
        return (input.optString("kind").equals("income")||input.optString("kind").equals("expense"))
                &&input.optString("financial_type").isEmpty()&&input.optLong("fee")==0&&input.optLong("related_id")==0
                &&book!=null&&!book.isNull("semantic_id")&&category!=null&&!CategoryRepository.LEGACY.equals(category.optString("level"))
                &&(!input.has("owner_semantic_id")||!input.isNull("owner_semantic_id"))
                &&(!input.has("raw_asset_semantic_id")||!input.isNull("raw_asset_semantic_id"))
                &&(account==null||AssetRepository.ORIGINAL.equals(account.optString("asset_origin")));
    }

    /** Canonical Bill + f.t1. Preflight is read-only; financial saves are independently ordered. */
    private long saveOriginalTransaction(JSONObject input, long editingId, String candidateId) {
        JSONObject candidate = candidateId == null ? null : candidate(candidateId);
        if (candidateId != null) {
            if (candidate == null) throw new IllegalArgumentException("采集记录不存在");
            if ("SAVED".equals(candidate.optString("state"))) return candidate.optLong("transaction_id");
            if (!"PENDING".equals(candidate.optString("state"))) throw new IllegalArgumentException("采集记录已忽略");
            if (editingId > 0) throw new IllegalArgumentException("采集记录不能覆盖已有账单");
        }
        String importKey = input.isNull("import_key") ? "" : input.optString("import_key");
        if (!importKey.isEmpty()) {
            if (!importKey.matches("[a-f0-9]{64}") || candidateId != null) throw new IllegalArgumentException("导入标识无效");
            JSONObject previous = one("SELECT id FROM transactions WHERE import_key=?", importKey);
            if (previous != null && previous.optLong("id") != editingId) {
                if (editingId > 0) throw new IllegalArgumentException("导入标识属于其他账单");
                return previous.optLong("id");
            }
        }
        JSONObject book = book(input.optLong("book_id"));
        if (book == null || book.optInt("archived") != 0 || book.isNull("semantic_id") || book.isNull("owner_semantic_id"))
            throw new IllegalArgumentException("请选择具备原版身份的账本");
        String kind = input.optString("kind"), currency = book.optString("currency");
        if(!"CNY".equals(currency))throw new IllegalArgumentException("此原版流程的外币转换尚待接入，请先保留外币元数据");
        if (!(kind.equals("expense") || kind.equals("income")) || !input.optString("financial_type").isEmpty()
                || input.optLong("fee") != 0 || input.optLong("related_id") != 0)
            throw new IllegalArgumentException("此账单使用原版普通收支流程，其他金融操作尚待接入");
        long amount = input.optLong("amount"), discount = input.optLong("discount"), account = input.optLong("account_id");
        if (amount <= 0 || amount > 99999999999999L || discount < 0 || discount > 99999999999999L) throw new IllegalArgumentException("请输入有效金额");
        validateAccount(account, currency);
        JSONObject asset = account > 0 ? assetRepository.byRowId(account) : null;
        if (asset != null && (!AssetRepository.ORIGINAL.equals(asset.optString("asset_origin")) || asset.optInt("owner_semantic_id") != LOCAL_OFFLINE_OWNER))
            throw new IllegalArgumentException("请选择当前离线身份的原版账户");
        if(asset!=null&&!asset.isNull("asset_currency")&&!asset.optString("asset_currency").isEmpty())
            throw new IllegalArgumentException("显式账户币种须经原版外币信息流程，尚待接入");
        JSONObject category = categoryRepository.byRowId(input.optLong("category_id"));
        if (category == null || category.optInt("archived") != 0 || !kind.equals(category.optString("kind"))
                || CategoryRepository.LEGACY.equals(category.optString("level"))) throw new IllegalArgumentException("请选择对应收支分类");
        long time = input.optLong("time");
        if (time <= 0 || time > 8640000000000000L) throw new IllegalArgumentException("记账时间无效");
        JSONArray tags = input.optJSONArray("tag_ids");
        if (tags != null) for (int i = 0; i < tags.length(); i++)
            if (one("SELECT id FROM tags WHERE id=?", "" + tags.optLong(i)) == null) throw new IllegalArgumentException("标签已不存在");
        OriginalBillRepository bills = new OriginalBillRepository(this);
        OriginalBillRecord bill = editingId > 0 ? bills.byLocalRowId(editingId) : new OriginalBillRecord();
        JSONObject old = editingId > 0 ? transaction(editingId) : null;
        if (editingId > 0 && (bill == null || old.optInt("deleted") != 0)) throw new IllegalArgumentException("账单已删除或不存在");
        ContentValues values = new ContentValues();
        values.put("book_id", book.optLong("id")); values.put("kind", kind); values.put("amount", amount); values.put("currency", currency);
        values.put("category_id", category.optLong("id"));
        JSONObject selection=input;
        if(candidateId!=null&&CategoryRepository.PARENT.equals(category.optString("level"))&&!input.has("child_semantic_id")) {
            selection=Json.copy(input);Json.put(selection,"child_semantic_id",0); // AutoAdd.S leaves new BillInfo.child at 0.
        }
        categoryRepository.applySelection(selection, category, values);
        if (account > 0) values.put("account_id", account); else values.putNull("account_id");
        values.putNull("to_account_id");
        ReferenceSchemaV3.applyAssetReferences(this, input, account, 0, candidateId != null, values);
        Integer assetOwner=values.getAsInteger("owner_semantic_id");
        if(assetOwner!=null&&assetOwner!=LOCAL_OFFLINE_OWNER)throw new IllegalArgumentException("账户语义引用不属于当前离线身份");
        for (String field : new String[]{"remark", "merchant", "origin", "asset_text"}) values.put(field, bounded(input.optString(field), 2000));
        values.put("discount", discount); values.put("fee", 0); values.put("time", time);
        values.put("not_total", inputFlag(input,"not_total") ? 1 : 0); values.put("not_budget", inputFlag(input,"not_budget") ? 1 : 0);
        values.put("attachments", input.optString("attachments", "[]"));
        if (!importKey.isEmpty()) values.put("import_key", importKey);
        long now = System.currentTimeMillis();
        if (editingId == 0) { values.put("created", now); if (candidateId != null) values.put("candidate_id", candidateId); bill.recordTime = now; bill.billType = candidateId == null ? 2 : 5; }
        Long rawAsset = values.getAsLong("raw_asset_semantic_id");
        if (rawAsset != null) bill.assetId = rawAsset;
        else if (editingId == 0) throw new IllegalArgumentException("原版账单缺少账户语义编号");
        bill.bookId = book.optLong("semantic_id"); bill.userId = book.optInt("owner_semantic_id");
        bill.parentCategoryId = values.getAsInteger("parent_semantic_id"); bill.childCategoryId = values.getAsInteger("child_semantic_id");
        bill.cost = java.math.BigDecimal.valueOf(amount, Money.scale(currency)).doubleValue();
        bill.discountNumber = java.math.BigDecimal.valueOf(discount, Money.scale(currency)).doubleValue();
        bill.time = time; bill.remark = values.getAsString("remark");
        bill.notIntoTotal = inputFlag(input,"not_total"); bill.notIntoBudget = inputFlag(input,"not_budget");
        if (editingId > 0 && bill.currencyInfo != null && !bill.currencyInfo.isEmpty())
            throw new IllegalArgumentException("含原版外币信息的账单须等待外币编辑流程接入");
        if(candidateId==null)bill.currencyInfo=""; // Manual AddBill's explicit base-currency branch; auto new Bill keeps null.
        // Local tag PKs are retained only in the UI bridge. They never invent original Tag.tagId.
        OriginalBillRepository.SaveResult result = new OriginalFinancialOrchestrator(this, LOCAL_OFFLINE_OWNER, book.optInt("owner_semantic_id"))
                .saveNormal(bill, values);
        if (!result.saved) throw new IllegalStateException("原版账单保存失败，账户阶段可能已完成；请检查资产历史后处理，勿直接重复提交");
        long id = bill.localRowId;
        SQLiteDatabase db = getWritableDatabase();
        if (tags != null) {
            db.beginTransaction();
            try {
                db.delete("transaction_tags", "transaction_id=?", new String[]{"" + id});
                for (int i = 0; i < tags.length(); i++) db.execSQL("INSERT OR IGNORE INTO transaction_tags(transaction_id,tag_id) VALUES(?,?)", new Object[]{id, tags.optLong(i)});
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
        }
        if (candidate != null) {
            JSONObject confirmed = Json.copy(input); Json.put(confirmed, "currency", currency);
            for (String key : new String[]{"parent_semantic_id", "child_semantic_id", "owner_semantic_id", "raw_asset_semantic_id", "raw_to_asset_semantic_id"}) Json.put(confirmed, key, values.getAsLong(key));
            db.execSQL("UPDATE candidates SET state='SAVED',transaction_id=?,book_id=?,payload=? WHERE id=?", new Object[]{id, book.optLong("id"), confirmed.toString(), candidateId});
        }
        if (input.optBoolean("remember_rule") && !input.optString("merchant").trim().isEmpty()) {
            ContentValues rule = new ContentValues(); rule.put("merchant", bounded(input.optString("merchant"), 2000));
            rule.put("category_id", category.optLong("id")); rule.put("book_id", book.optLong("id")); if (account > 0) rule.put("account_id", account);
            JSONObject previous = one("SELECT id FROM rules WHERE merchant=?", input.optString("merchant"));
            if (previous == null) db.insertOrThrow("rules", null, rule); else db.update("rules", rule, "id=?", new String[]{"" + previous.optLong("id")});
        }
        return id;
    }

    private long saveLegacyTransaction(JSONObject input, long editingId, String candidateId) {
        SQLiteDatabase db = getWritableDatabase(); db.beginTransaction();
        try {
            JSONObject candidate = candidateId == null ? null : candidate(candidateId);
            // Android JSONObject.NULL is a present value: optString would turn
            // a database NULL into the literal "null" when editing an ordinary bill.
            String importKey=input.isNull("import_key")?"":input.optString("import_key");
            if(!importKey.isEmpty()){
                if(!importKey.matches("[a-f0-9]{64}"))throw new IllegalArgumentException("导入标识无效");
                if(candidateId!=null)throw new IllegalArgumentException("采集记录不能使用导入标识");
                JSONObject previous=one("SELECT id FROM transactions WHERE import_key=?",importKey);
                if(previous!=null&&previous.optLong("id")!=editingId){
                    if(editingId>0)throw new IllegalArgumentException("导入标识属于其他账单");
                    db.setTransactionSuccessful();return previous.optLong("id");
                }
            }
            if (candidateId != null) {
                if (candidate == null) throw new IllegalArgumentException("采集记录不存在");
                if ("SAVED".equals(candidate.optString("state"))) { db.setTransactionSuccessful(); return candidate.optLong("transaction_id"); }
                if (!"PENDING".equals(candidate.optString("state"))) throw new IllegalArgumentException("采集记录已忽略");
            }
            long bookId = input.optLong("book_id"); JSONObject book = book(bookId);
            if (book == null || book.optInt("archived") != 0) throw new IllegalArgumentException("请选择有效账本");
            String currency = book.optString("currency"), kind = input.optString("kind");
            if (!java.util.Arrays.asList("expense", "income", "transfer").contains(kind)) throw new IllegalArgumentException("记账类型无效");
            long amount = input.optLong("amount"), fee = input.optLong("fee"), discount = input.optLong("discount");
            if (amount <= 0 || amount > 999_999_999_999_99L || fee < 0 || fee>999_999_999_999_99L || discount < 0 || discount>999_999_999_999_99L) throw new IllegalArgumentException("请输入有效金额");
            long account = input.optLong("account_id"), destination = input.optLong("to_account_id"), category = input.optLong("category_id");
            validateAccount(account, currency); validateAccount(destination, currency);
            if (kind.equals("transfer") && (account <= 0 || destination <= 0 || account == destination)) throw new IllegalArgumentException("请选择两个不同的转账账户");
            JSONObject selectedCategory = null;
            if (!kind.equals("transfer")) {
                JSONObject c = one("SELECT * FROM categories WHERE id=? AND archived=0", "" + category);
                if (c == null || !kind.equals(c.optString("kind"))) throw new IllegalArgumentException("请选择对应收支类型的分类");
                selectedCategory = c;
            }
            long time = input.optLong("time"); if (time <= 0 || time > 8640000000000000L) throw new IllegalArgumentException("记账时间无效");
            long related=input.optLong("related_id");
            if(related>0){JSONObject parent=transaction(related);if(parent==null||parent.optInt("deleted")!=0||parent.optLong("book_id")!=bookId||parent.optString("kind").equals(kind)||kind.equals("transfer"))throw new IllegalArgumentException("关联回款的账本或收支方向无效");long returned=scalar("SELECT COALESCE(SUM(amount),0) n FROM transactions WHERE related_id=? AND deleted=0 AND id<>?",""+related,""+editingId);if(amount>parent.optLong("amount")-returned)throw new IllegalArgumentException("回款超过原账单剩余金额");}
            if(editingId>0){JSONObject old=transaction(editingId);if(old!=null){long returned=scalar("SELECT COALESCE(SUM(amount),0) n FROM transactions WHERE related_id=? AND deleted=0",""+editingId);if(returned>amount||(returned>0&&(bookId!=old.optLong("book_id")||!kind.equals(old.optString("kind")))))throw new IllegalArgumentException("已有回款的原账单不能改变账本、收支方向或小于已回款金额");}}
            ContentValues values = new ContentValues();
            values.put("book_id", bookId); values.put("kind", kind); values.put("amount", amount); values.put("currency", currency);
            if (kind.equals("transfer")) values.putNull("category_id"); else values.put("category_id", category);
            if (selectedCategory == null) { values.putNull("parent_semantic_id"); values.putNull("child_semantic_id"); }
            else categoryRepository.applySelection(input, selectedCategory, values);
            if (account <= 0) values.putNull("account_id"); else values.put("account_id", account);
            if (!kind.equals("transfer")) values.putNull("to_account_id"); else values.put("to_account_id", destination);
            ReferenceSchemaV3.applyAssetReferences(this,input,account,kind.equals("transfer")?destination:0,candidateId!=null,values);
            for (String field : java.util.Arrays.asList("remark", "merchant", "origin", "asset_text", "financial_type")) values.put(field, bounded(input.optString(field), 2000));
            values.put("discount", discount); values.put("fee", fee); values.put("time", time);
            values.put("not_total", inputFlag(input,"not_total") ? 1 : 0); values.put("not_budget", inputFlag(input,"not_budget") ? 1 : 0);
            values.put("settled", input.optInt("settled")); values.put("attachments", input.optString("attachments", "[]"));
            if(input.optLong("related_id")>0)values.put("related_id",input.optLong("related_id"));
            if(!importKey.isEmpty())values.put("import_key",importKey);
            long id;
            if (editingId > 0) {
                JSONObject old = transaction(editingId); if (old == null || old.optInt("deleted") != 0) throw new IllegalArgumentException("账单已删除或不存在");
                if (candidateId != null) throw new IllegalArgumentException("采集记录不能覆盖已有账单");
                db.update("transactions", values, "id=?", new String[]{"" + editingId}); id = editingId;
            } else {
                values.put("created", System.currentTimeMillis()); if (candidateId != null) values.put("candidate_id", candidateId);
                id = db.insertOrThrow("transactions", null, values);
            }
            JSONArray tags = input.optJSONArray("tag_ids");
            if (tags != null) {
                db.delete("transaction_tags", "transaction_id=?", new String[]{"" + id});
                for (int i = 0; i < tags.length(); i++) {
                    long tag = tags.optLong(i); if (one("SELECT id FROM tags WHERE id=?", "" + tag) == null) throw new IllegalArgumentException("标签已不存在");
                    db.execSQL("INSERT OR IGNORE INTO transaction_tags(transaction_id,tag_id) VALUES(?,?)", new Object[]{id, tag});
                }
            }
            if (candidate != null) {
                JSONObject confirmed=Json.copy(input);Json.put(confirmed,"currency",currency);
                Json.put(confirmed,"parent_semantic_id",values.getAsLong("parent_semantic_id"));Json.put(confirmed,"child_semantic_id",values.getAsLong("child_semantic_id"));
                for(String field:new String[]{"owner_semantic_id","raw_asset_semantic_id","raw_to_asset_semantic_id"})Json.put(confirmed,field,values.getAsLong(field));
                db.execSQL("UPDATE candidates SET state='SAVED',transaction_id=?,book_id=?,payload=? WHERE id=?",
                    new Object[]{id,bookId,confirmed.toString(),candidateId});
            }
            if (!kind.equals("transfer") && input.optBoolean("remember_rule") && !input.optString("merchant").trim().isEmpty()) {
                ContentValues rule = new ContentValues(); rule.put("merchant", bounded(input.optString("merchant"), 2000)); rule.put("category_id", category); rule.put("book_id", bookId); if (account > 0) rule.put("account_id", account);
                JSONObject old = one("SELECT id FROM rules WHERE merchant=?", input.optString("merchant"));
                if (old == null) db.insertOrThrow("rules", null, rule); else db.update("rules", rule, "id=?", new String[]{"" + old.optLong("id")});
            }
            String financialType=input.optString("financial_type");
            if(selectedCategory!=null&&!CategoryRepository.LEGACY.equals(selectedCategory.optString("level"))
                &&(financialType.isEmpty()||financialType.equals("reimbursement"))) {
                Long raw=values.getAsLong("raw_asset_semantic_id");
                if(raw!=null) {
                    // i0 stores an int even though Asset.assetId is long. A
                    // narrowed ID must not fabricate a link to the full-width row.
                    Long mappedRow=account>0&&raw.longValue()==(long)raw.intValue()?account:null;
                    assetRepository.learnCategoryAccount(db,values.getAsInteger("owner_semantic_id"),values.getAsInteger("parent_semantic_id"),values.getAsInteger("child_semantic_id"),raw,mappedRow,System.currentTimeMillis());
                } else categoryRepository.learn(db,values.getAsInteger("parent_semantic_id"),values.getAsInteger("child_semantic_id"),account);
            }
            db.setTransactionSuccessful(); return id;
        } finally { db.endTransaction(); }
    }
    private void validateAccount(long id, String currency) {
        if (id <= 0) return;
        JSONObject account = one("SELECT * FROM accounts WHERE id=? AND archived=0", "" + id);
        String accountCurrency=account==null?"":account.optString("currency");
        if(account!=null&&AssetRepository.ORIGINAL.equals(account.optString("asset_origin"))&&accountCurrency.isEmpty())accountCurrency="CNY";
        if (account == null || !currency.equals(accountCurrency)) throw new IllegalArgumentException("账户不存在或币种与账本不同");
    }
    /** Original a0.y/C2 matches the unedited captured time +/- one minute, absolute amount
     * +/- 0.0001 and income direction, across books. Transfers/debts use other original tables. */
    public synchronized List<JSONObject> findRepeatTransactions(JSONObject data) {
        long time=data.has("original_time")?data.optLong("original_time"):data.optLong("time");
        double amount=data.has("original_number")?Math.abs(com.wangc.bill.utils.h2.S(data.optString("original_number")))
            :Math.abs((data.has("original_amount")?data.optLong("original_amount"):data.optLong("amount"))/100.0);
        boolean income=data.has("original_income")?data.optBoolean("original_income"):"income".equals(data.optString("kind"));
        if(time<Long.MIN_VALUE+60000||time>Long.MAX_VALUE-60000||!Double.isFinite(amount))return new ArrayList<>();
        String currency=data.optString("original_currency",data.optString("currency","CNY"));
        return query("SELECT t.*,b.name book_name FROM transactions t JOIN books b ON b.id=t.book_id"
            +" WHERE t.deleted=0 AND t.currency=? AND t.time>=? AND t.time<=? AND t.amount/100.0>=CAST(? AS REAL) AND t.amount/100.0<=CAST(? AS REAL) AND t.kind=?"
            +" AND t.financial_type IN ('','reimbursement') ORDER BY t.id",
            currency,""+(time-60000),""+(time+60000),""+(amount-0.0001),""+(amount+0.0001),income?"income":"expense");
    }
    /** The original repeat dialog can replace its sole matching bill. All local changes,
     * including the old candidate link, commit together; failed validation retains the old bill. */
    public synchronized long saveCandidateReplacing(JSONObject data,String candidateId,long oldId) {
        if(candidateId==null||candidateId.isEmpty())throw new IllegalArgumentException("采集记录不存在");
        JSONObject previous=transaction(oldId);
        if(previous!=null&&(OriginalBillRepository.ORIGINAL.equals(previous.optString("bill_origin"))||usesOriginalCreation(data))) {
            JSONObject pending=candidate(candidateId);
            if(pending==null)throw new IllegalArgumentException("采集记录不存在");
            if("SAVED".equals(pending.optString("state")))return pending.optLong("transaction_id");
            if(!"PENDING".equals(pending.optString("state")))throw new IllegalArgumentException("采集记录已处理");
            List<JSONObject> repeats=findRepeatTransactions(data);
            if(repeats.size()!=1||repeats.get(0).optLong("id")!=oldId)throw new IllegalArgumentException("重复账单已变化，请重新确认");
            if(!data.isNull("import_key")&&!data.optString("import_key").isEmpty())throw new IllegalArgumentException("采集记录不能使用导入标识");
            if(OriginalBillRepository.ORIGINAL.equals(previous.optString("bill_origin")))requireSimpleOriginalDeletion(oldId);
            else if(previous.optLong("related_id")>0||scalar("SELECT COUNT(*) n FROM transactions WHERE related_id=?",""+oldId)>0)
                throw new IllegalArgumentException("重复账单有关联回款，请先处理关联账单");
            // Original AutoAdd's replacement calls a0.s before its new-save callback.
            // Unlike the legacy compatibility path below, this is not one transaction.
            OriginalBillRecord bill=new OriginalBillRepository(this).byLocalRowId(oldId);
            getWritableDatabase().execSQL("UPDATE candidates SET state='DISMISSED',transaction_id=NULL WHERE transaction_id=?",new Object[]{oldId});
            if(getWritableDatabase().delete("transactions","id=?",new String[]{""+oldId})>0&&bill!=null)
                new OriginalFinancialOrchestrator(this,LOCAL_OFFLINE_OWNER,bill.userId).reverseDeletedBill(bill,true);
            return saveTransaction(data,0,candidateId);
        }
        SQLiteDatabase db=getWritableDatabase();db.beginTransaction();
        try {
            JSONObject candidate=candidate(candidateId);
            if(candidate==null)throw new IllegalArgumentException("采集记录不存在");
            if("SAVED".equals(candidate.optString("state"))){db.setTransactionSuccessful();return candidate.optLong("transaction_id");}
            if(!"PENDING".equals(candidate.optString("state")))throw new IllegalArgumentException("采集记录已处理");
            List<JSONObject> repeats=findRepeatTransactions(data);
            if(repeats.size()!=1||repeats.get(0).optLong("id")!=oldId)throw new IllegalArgumentException("重复账单已变化，请重新确认");
            JSONObject old=repeats.get(0);
            if(old.optLong("related_id")>0||scalar("SELECT COUNT(*) n FROM transactions WHERE related_id=?",""+oldId)>0)
                throw new IllegalArgumentException("重复账单有关联回款，请先处理关联账单");
            long id=saveTransaction(data,0,candidateId);
            db.execSQL("UPDATE candidates SET state='DISMISSED',transaction_id=NULL WHERE transaction_id=?",new Object[]{oldId});
            if(db.delete("transactions","id=?",new String[]{""+oldId})!=1)throw new IllegalStateException("重复账单已变化");
            db.setTransactionSuccessful();return id;
        } finally { db.endTransaction(); }
    }
    public synchronized void trash(long id, boolean deleted) {
        JSONObject row=transaction(id);
        if(row==null||row.optInt("deleted")== (deleted?1:0))return;
        OriginalBillRecord original=new OriginalBillRepository(this).byLocalRowId(id);
        if(original==null){getWritableDatabase().execSQL("UPDATE transactions SET deleted=? WHERE id=?",new Object[]{deleted?1:0,id});return;}
        requireSimpleOriginalDeletion(id);
        getWritableDatabase().execSQL("UPDATE transactions SET deleted=? WHERE id=?",new Object[]{deleted?1:0,id});
        OriginalFinancialOrchestrator command=new OriginalFinancialOrchestrator(this,LOCAL_OFFLINE_OWNER,original.userId);
        // Reversible trash is a host extension. Original a0.r/s physically delete first.
        // Apply/reverse the stored original amount once, rather than a legacy ledger SUM.
        if(deleted)command.reverseDeletedBill(original,true);
        else{original.billId=0;command.editBillAssets(original);}
    }

    private void requireSimpleOriginalDeletion(long id) {
        JSONObject row=transaction(id);
        if(row==null)throw new IllegalArgumentException("原账单不存在");
        if(row.optLong("related_id")>0||scalar("SELECT COUNT(*) n FROM transactions WHERE related_id=?",""+id)>0)
            throw new IllegalArgumentException("请先处理关联回款账单");
        int semantic=row.optInt("bill_semantic_id");
        if(!new OriginalRefundRepository(this).allByParent(LOCAL_OFFLINE_OWNER,semantic).isEmpty()
                ||!new OriginalReimbursementRepository(this).allByParent(LOCAL_OFFLINE_OWNER,semantic).isEmpty())
            throw new IllegalArgumentException("含原版退款或报销的账单删除流程尚待接入，请先保留账单");
    }
    public synchronized void purge(long id){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{JSONObject row=transaction(id);if(row==null||row.optInt("deleted")!=1)throw new IllegalArgumentException("只能永久删除回收站中的账单");if(scalar("SELECT COUNT(*) n FROM transactions WHERE related_id=?",""+id)>0)throw new IllegalArgumentException("请先删除关联的回款账单");if(OriginalBillRepository.ORIGINAL.equals(row.optString("bill_origin")))requireSimpleOriginalDeletion(id);String candidate=row.optString("candidate_id");if(!candidate.isEmpty()&&!candidate.equals("null"))db.execSQL("UPDATE candidates SET state='DISMISSED',transaction_id=NULL WHERE id=?",new Object[]{candidate});db.delete("transactions","id=?",new String[]{""+id});db.setTransactionSuccessful();}finally{db.endTransaction();}}
    public synchronized void dismiss(String id) { getWritableDatabase().execSQL("UPDATE candidates SET state='DISMISSED' WHERE id=? AND state='PENDING'", new Object[]{id}); }
    public synchronized void settle(long id, boolean settled) { getWritableDatabase().execSQL("UPDATE transactions SET settled=? WHERE id=?", new Object[]{settled ? 1 : 0, id}); }
    public synchronized long outstanding(long id){JSONObject t=transaction(id);if(t==null)return 0;long returned=scalar("SELECT COALESCE(SUM(amount),0) n FROM transactions WHERE related_id=? AND deleted=0",""+id);return Math.max(0,t.optLong("amount")-returned);}
    public synchronized long financialReturn(long id,long amount,long account,long time){
        if(new OriginalBillRepository(this).byLocalRowId(id)!=null)
            throw new IllegalArgumentException("原版退款与报销流程尚待接入，不能替换为普通回款账单");
        SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{
            JSONObject original=transaction(id);if(original==null||original.optInt("deleted")!=0)throw new IllegalArgumentException("原账单不存在");
            if(amount<=0||amount>outstanding(id))throw new IllegalArgumentException("回款金额超过剩余金额");
            String type=original.optString("financial_type"),kind=type.equals("borrow")?"expense":"income";
            String next=type.equals("lend")?"lend_return":type.equals("borrow")?"borrow_repay":type.equals("reimbursement")?"reimbursement_return":"refund";
            JSONObject value=Json.object("book_id",original.optLong("book_id"),"kind",kind,"amount",amount,"category_id",defaultCategory(kind,original.optLong("book_id")),"account_id",account,"remark",(kind.equals("income")?"回款·":"还款·")+original.optString("remark"),"merchant",original.optString("merchant"),"time",time,"financial_type",next,"related_id",id);
            long saved=saveTransaction(value,0,null);if(outstanding(id)==0)db.execSQL("UPDATE transactions SET settled=1 WHERE id=?",new Object[]{id});db.setTransactionSuccessful();return saved;
        }finally{db.endTransaction();}
    }
    public JSONObject candidate(String id) { return one("SELECT * FROM candidates WHERE id=?", id); }
    public List<JSONObject> candidates() { return query("SELECT * FROM candidates WHERE state='PENDING' ORDER BY created DESC LIMIT 1000"); }
    public synchronized String addCandidate(JSONObject payload, String packageName, long book) {
        if (book(book) == null) throw new IllegalArgumentException("采集目标账本不存在");
        if (scalar("SELECT COUNT(*) n FROM candidates WHERE state='PENDING'") >= 1000) throw new IllegalStateException("待确认列表已满");
        String id = UUID.randomUUID().toString();
        ContentValues values = new ContentValues(); values.put("id", id); values.put("payload", payload.toString()); values.put("package_name", packageName); values.put("book_id", book); values.put("created", System.currentTimeMillis());
        getWritableDatabase().insertOrThrow("candidates", null, values); return id;
    }
    public synchronized void updateCandidateRemark(String id, String remark) {
        JSONObject row = candidate(id); if (row == null || !"PENDING".equals(row.optString("state"))) return;
        JSONObject data = Json.parse(row.optString("payload")); Json.put(data, "remark", bounded(remark, 2000));
        getWritableDatabase().execSQL("UPDATE candidates SET payload=? WHERE id=? AND state='PENDING'", new Object[]{data.toString(), id});
    }
    public synchronized long saveBook(long id, String name, String currency, long budget) {
        Money.scale(currency); name = requiredName(name); if (budget < 0) throw new IllegalArgumentException("预算不能小于零");
        JSONObject old = book(id);
        if (old != null && !currency.equals(old.optString("currency")) && scalar("SELECT COUNT(*) n FROM transactions WHERE book_id=?", "" + id) > 0) throw new IllegalArgumentException("已有账单的账本不能更换币种");
        if(old!=null&&!currency.equals(old.optString("currency"))&&(scalar("SELECT COUNT(*) n FROM candidates WHERE book_id=?",""+id)>0||hasPlanForBook(id)))throw new IllegalArgumentException("有采集记录或周期账单的账本不能更换币种");
        ContentValues v = new ContentValues(); v.put("name", name); v.put("currency", currency); v.put("budget", budget);
        if (id > 0) { if (old == null) throw new IllegalArgumentException("账本不存在"); getWritableDatabase().update("books", v, "id=?", new String[]{"" + id}); return id; }
        long now=System.currentTimeMillis();
        JSONObject created=bookRepository.createOriginal(LOCAL_OFFLINE_OWNER,name,null,false,false,now);
        long rowId=created.optLong("id");
        getWritableDatabase().update("books",v,"id=?",new String[]{""+rowId});return rowId;
    }

    /** Explicit atomic CSV compatibility import; it does not fabricate original Bill IDs. */
    public synchronized long saveLegacyImport(JSONObject input) {
        if(input.isNull("import_key")||!input.optString("import_key").matches("[a-f0-9]{64}"))throw new IllegalArgumentException("导入标识无效");
        for(String key:new String[]{"account_id","to_account_id"}) {
            JSONObject asset=assetRepository.byRowId(input.optLong(key));
            if(asset!=null&&AssetRepository.ORIGINAL.equals(asset.optString("asset_origin")))
                throw new IllegalArgumentException("此CSV导入尚未接入原版账户，请使用独立旧账户");
        }
        return saveLegacyTransaction(input,0,null);
    }
    public synchronized void removeBook(long id) {
        if (books().size() <= 1) throw new IllegalArgumentException("至少保留一个账本");
        if (scalar("SELECT COUNT(*) n FROM transactions WHERE book_id=?", "" + id) > 0 || scalar("SELECT COUNT(*) n FROM candidates WHERE book_id=?", "" + id) > 0) throw new IllegalArgumentException("账本内还有账单或采集记录，请保留或先迁移数据");
        if(hasPlanForBook(id))throw new IllegalArgumentException("请先删除关联的周期记账规则");
        getWritableDatabase().delete("rules", "book_id=?", new String[]{"" + id});
        getWritableDatabase().delete("books", "id=?", new String[]{"" + id});
    }
    public synchronized long saveAccount(long id, String name, String type, String currency, long opening) {
        Money.scale(currency); ContentValues v = new ContentValues(); v.put("name", requiredName(name)); v.put("type", type); v.put("currency", currency); v.put("opening", opening);
        if (id > 0) {
            JSONObject old = one("SELECT * FROM accounts WHERE id=?", "" + id); if (old == null) throw new IllegalArgumentException("账户不存在");
            if (!currency.equals(old.optString("currency")) && scalar("SELECT COUNT(*) n FROM transactions WHERE account_id=? OR to_account_id=?", "" + id, "" + id) > 0) throw new IllegalArgumentException("已有账单的账户不能更换币种");
            getWritableDatabase().update("accounts", v, "id=?", new String[]{"" + id}); return id;
        }
        return getWritableDatabase().insertOrThrow("accounts", null, v);
    }
    public synchronized void archiveAccount(long id) { getWritableDatabase().execSQL("UPDATE accounts SET archived=1 WHERE id=?", new Object[]{id}); }
    public synchronized long saveCategory(long id, String name, String kind, String icon) {
        if (!java.util.Arrays.asList("expense", "income").contains(kind)) throw new IllegalArgumentException("分类类型无效");
        JSONObject old=id>0?categoryRepository.byRowId(id):null;
        if(id>0&&(old==null||!kind.equals(old.optString("kind"))))throw new IllegalArgumentException("不能改变已建分类的收支类型");
        if(old!=null&&CategoryRepository.LEGACY.equals(old.optString("level"))){ContentValues v=new ContentValues();v.put("name",requiredName(name));v.put("icon",icon);getWritableDatabase().update("categories",v,"id=?",new String[]{""+id});return id;}
        if(old!=null&&CategoryRepository.CHILD.equals(old.optString("level")))return categoryRepository.saveChild(id,name,icon,old.optInt("parent_semantic_id"),old.optInt("parent_child_semantic_id"));
        return old!=null||kind.equals("expense")?categoryRepository.saveParent(id,name,icon):categoryRepository.saveChild(0,name,icon,9,0);
    }
    public synchronized void archiveCategory(long id) { getWritableDatabase().execSQL("UPDATE categories SET archived=1 WHERE id=?", new Object[]{id}); }
    public synchronized long saveTag(String name) { ContentValues v = new ContentValues(); v.put("name", requiredName(name)); return getWritableDatabase().insertOrThrow("tags", null, v); }
    public synchronized void deleteTag(long id) { SQLiteDatabase db = getWritableDatabase(); db.beginTransaction(); try { db.delete("transaction_tags", "tag_id=?", new String[]{"" + id}); db.delete("tags", "id=?", new String[]{"" + id}); db.setTransactionSuccessful(); } finally { db.endTransaction(); } }
    public synchronized long saveGoal(long id, String name, long target, long saved) {
        if (target <= 0 || saved < 0) throw new IllegalArgumentException("目标金额必须大于零，已存金额不能小于零");
        ContentValues v = new ContentValues(); v.put("name", requiredName(name)); v.put("target", target); v.put("saved", saved);
        if (id > 0) { getWritableDatabase().update("goals", v, "id=?", new String[]{"" + id}); return id; }
        v.put("created", System.currentTimeMillis()); return getWritableDatabase().insertOrThrow("goals", null, v);
    }
    public synchronized long savePlan(long id,String name,String interval,JSONObject payload,long due){
        if(!java.util.Arrays.asList("daily","weekly","monthly","yearly").contains(interval)||due<=0)throw new IllegalArgumentException("周期设置无效");
        validatePlan(payload);
        ContentValues v=new ContentValues();v.put("name",requiredName(name));v.put("interval",interval);v.put("payload",payload.toString());v.put("next_due",due);
        if(id>0){getWritableDatabase().update("plans",v,"id=?",new String[]{""+id});return id;}return getWritableDatabase().insertOrThrow("plans",null,v);
    }
    public synchronized void enablePlan(long id,boolean enabled){getWritableDatabase().execSQL("UPDATE plans SET enabled=? WHERE id=?",new Object[]{enabled?1:0,id});}
    private boolean hasPlanForBook(long id){for(JSONObject row:query("SELECT payload FROM plans"))if(Json.parse(row.optString("payload")).optLong("book_id")==id)return true;return false;}
    private void validatePlan(JSONObject payload){JSONObject book=book(payload.optLong("book_id"));long amount=payload.optLong("amount");if(book==null||book.optInt("archived")!=0||amount<=0||amount>99999999999999L)throw new IllegalArgumentException("周期账单缺少有效账本或金额");if(!java.util.Arrays.asList("expense","income").contains(payload.optString("kind")))throw new IllegalArgumentException("周期账单收支方向无效");if(payload.optLong("category_id")>0&&categoryRepository.byRowId(payload.optLong("category_id"))==null)throw new IllegalArgumentException("周期账单分类不存在");Json.put(payload,"currency",book.optString("currency"));}
    public synchronized int processPlans(long now){
        SQLiteDatabase db=getWritableDatabase();db.beginTransaction();int count=0;try{
            for(JSONObject plan:query("SELECT * FROM plans WHERE enabled=1 AND next_due<=? ORDER BY next_due LIMIT 50",""+now)){
                JSONObject payload=Json.parse(plan.optString("payload"));long due=plan.optLong("next_due");int generated=0;
                while(due<=now&&generated++<12){JSONObject draft=Json.copy(payload);validatePlan(draft);Json.put(draft,"time",due);Json.put(draft,"origin","周期记账");addCandidate(draft,"local.plan",payload.optLong("book_id"));count++;java.util.Calendar c=java.util.Calendar.getInstance();c.setTimeInMillis(due);String interval=plan.optString("interval");if(interval.equals("monthly")||interval.equals("yearly")){int day=payload.optInt("anchor_day",c.get(java.util.Calendar.DAY_OF_MONTH));c.set(java.util.Calendar.DAY_OF_MONTH,1);c.add(interval.equals("monthly")?java.util.Calendar.MONTH:java.util.Calendar.YEAR,1);c.set(java.util.Calendar.DAY_OF_MONTH,Math.min(day,c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)));}else c.add(java.util.Calendar.DATE,interval.equals("weekly")?7:1);due=c.getTimeInMillis();}
                db.execSQL("UPDATE plans SET next_due=? WHERE id=?",new Object[]{due,plan.optLong("id")});
            }
            db.setTransactionSuccessful();return count;
        }finally{db.endTransaction();}
    }
    public synchronized void deleteSimple(String table, long id) { if (!java.util.Arrays.asList("goals", "plans", "rules").contains(table)) throw new IllegalArgumentException(); getWritableDatabase().delete(table, "id=?", new String[]{"" + id}); }
    private static boolean inputFlag(JSONObject input,String key){Object value=input.opt(key);return Boolean.TRUE.equals(value)||value instanceof Number&&((Number)value).longValue()==1||"true".equals(value);}
    private static String requiredName(String value) { String name = value == null ? "" : value.trim(); if (name.isEmpty() || name.length() > 100) throw new IllegalArgumentException("名称需要1至100个字符"); return name; }
    private static String bounded(String value, int max) { return value == null ? "" : value.substring(0, Math.min(value.length(), max)); }

    private static final String[] V1_BACKUP_TABLES = {"books", "accounts", "categories", "tags", "candidates", "transactions", "transaction_tags", "rules", "plans", "goals"};
    private static final String[] V2_BACKUP_TABLES = {"books", "accounts", "categories", "tags", "candidates", "transactions", "transaction_tags", "rules", "plans", "goals", "category_hidden_books", "category_accounts"};
    private static final String[] V3_BACKUP_TABLES = {"books", "accounts", "categories", "tags", "candidates", "transactions", "transaction_tags", "rules", "plans", "goals", "category_hidden_books", "category_accounts", "asset_show_books"};
    private static final String[] BACKUP_TABLES = {"books", "accounts", "categories", "tags", "candidates", "transactions", "transaction_tags", "rules", "plans", "goals", "category_hidden_books", "category_accounts", "asset_show_books", "asset_histories", "original_refunds", "original_refund_items", "original_reimbursements", "original_reimbursement_items"};
    public synchronized JSONObject backup() {
        SQLiteDatabase db = getWritableDatabase(); db.beginTransaction();
        try {
            JSONObject tables = new JSONObject(); for (String table : BACKUP_TABLES) Json.put(tables, table, new JSONArray(query("SELECT * FROM " + table)));
            JSONObject result = Json.object("format", "YimuReferenceBackup", "version", 4, "created", System.currentTimeMillis(), "tables", tables);
            db.setTransactionSuccessful(); return result;
        } finally { db.endTransaction(); }
    }
    public synchronized void restore(JSONObject backup) {
        int version=backup.optInt("version");
        if (!"YimuReferenceBackup".equals(backup.optString("format")) || (version!=1&&version!=2&&version!=3&&version!=4)) throw new IllegalArgumentException("请选择本实验版的完整备份");
        JSONObject tables = backup.optJSONObject("tables"); if (tables == null || tables.optJSONArray("books") == null || tables.optJSONArray("books").length() == 0) throw new IllegalArgumentException("备份缺少账本");
        SQLiteDatabase db = getWritableDatabase(); db.beginTransaction();
        try {
            db.execSQL("PRAGMA defer_foreign_keys=ON");
            for (int i = BACKUP_TABLES.length - 1; i >= 0; i--) db.delete(BACKUP_TABLES[i], null, null);
            for (String table : version==1?V1_BACKUP_TABLES:version==2?V2_BACKUP_TABLES:version==3?V3_BACKUP_TABLES:BACKUP_TABLES) {
                JSONArray rows = tables.optJSONArray(table); if (rows == null || rows.length() > 100000) throw new IllegalArgumentException("备份表缺失或过大：" + table);
                List<String> allowed = new ArrayList<>();java.util.Map<String,String> types=new java.util.HashMap<>();
                try (Cursor c = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
                    while (c.moveToNext()) {
                        String column=c.getString(c.getColumnIndexOrThrow("name"));allowed.add(column);
                        types.put(column,c.getString(c.getColumnIndexOrThrow("type")));
                    }
                }
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.optJSONObject(i); if (row == null) throw new IllegalArgumentException("备份记录格式错误");
                    if(version>=2)for(String column:allowed)if(ReferenceSchemaV4.acceptsBackupColumn(version,table,column)&&!row.has(column))throw new IllegalArgumentException("备份字段缺失："+table+"."+column);
                    ContentValues values = new ContentValues();
                    for (String column : allowed) if (row.has(column)) {
                        if(!ReferenceSchemaV4.acceptsBackupColumn(version,table,column))continue;
                        Object value = row.opt(column);
                        if (value == JSONObject.NULL) values.putNull(column);
                        else if ("INTEGER".equals(types.get(column))&&(value instanceof Long || value instanceof Integer)) values.put(column, ((Number)value).longValue());
                        else if("REAL".equals(types.get(column))&&value instanceof Number&&Double.isFinite(((Number)value).doubleValue()))values.put(column,((Number)value).doubleValue());
                        else if ("TEXT".equals(types.get(column))&&value instanceof String text && text.length() <= 200000) values.put(column, text);
                        else throw new IllegalArgumentException("备份字段格式错误：" + column);
                    }
                    if(version==1&&table.equals("categories"))values.put("level",CategoryRepository.LEGACY);
                    db.insertOrThrow(table, null, values);
                }
            }
            categoryRepository.validateRestored();
            assetRepository.validateRestored();
            bookRepository.validateRestored();
            ReferenceSchemaV3.validateRestored(this);
            ReferenceSchemaV4.validateRestored(this);
            CategoryRepository.bootstrap(db);
            try (Cursor c = db.rawQuery("PRAGMA foreign_key_check", null)) { if (c.moveToFirst()) throw new IllegalArgumentException("备份的账本或账户关联损坏"); }
            for (JSONObject book : query("SELECT * FROM books")) Money.scale(book.optString("currency"));
            if(scalar("SELECT COUNT(*) n FROM books WHERE archived=0")==0)throw new IllegalArgumentException("备份中没有可用账本");
            if(scalar("SELECT COUNT(*) n FROM transactions t JOIN books b ON b.id=t.book_id LEFT JOIN accounts a ON a.id=t.account_id LEFT JOIN accounts z ON z.id=t.to_account_id WHERE t.currency<>b.currency OR (a.id IS NOT NULL AND (CASE WHEN a.asset_origin='original' AND a.currency='' THEN 'CNY' ELSE a.currency END)<>t.currency) OR (z.id IS NOT NULL AND (CASE WHEN z.asset_origin='original' AND z.currency='' THEN 'CNY' ELSE z.currency END)<>t.currency) OR t.amount>99999999999999 OR t.fee>99999999999999 OR t.discount>99999999999999 OR t.not_total NOT IN(0,1) OR t.not_budget NOT IN(0,1) OR t.deleted NOT IN(0,1)")>0)throw new IllegalArgumentException("备份账单的币种或数值无效");
            for(JSONObject plan:query("SELECT * FROM plans")){if(!java.util.Arrays.asList("daily","weekly","monthly","yearly").contains(plan.optString("interval"))||plan.optLong("next_due")<=0)throw new IllegalArgumentException("备份周期规则无效");validatePlan(Json.parse(plan.optString("payload")));}
            for(JSONObject candidate:query("SELECT * FROM candidates")){
                if(!java.util.Arrays.asList("PENDING","SAVED","DISMISSED").contains(candidate.optString("state")))throw new IllegalArgumentException("备份采集状态无效");
                JSONObject payload=Json.parse(candidate.optString("payload"));if(payload.optLong("book_id")!=candidate.optLong("book_id"))throw new IllegalArgumentException("备份采集账本不匹配");
                if(payload.optLong("category_id")>0&&categoryRepository.byRowId(payload.optLong("category_id"))==null)throw new IllegalArgumentException("备份采集分类关联无效");
                if(candidate.optString("state").equals("SAVED")){JSONObject t=one("SELECT candidate_id FROM transactions WHERE id=?",""+candidate.optLong("transaction_id"));if(t==null||!candidate.optString("id").equals(t.optString("candidate_id")))throw new IllegalArgumentException("备份采集关联无效");}
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }
}
