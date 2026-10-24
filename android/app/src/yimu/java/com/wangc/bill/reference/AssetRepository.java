package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.json.JSONObject;

/**
 * Asset metadata and original-ID lookups, recovered from database.action.f/i0.
 * Original asset IDs and showBook values are semantic IDs, never SQLite row IDs.
 * Balance/history mutations and type-9's derived getAssetNumber are deliberately
 * left to their original operation-specific callers; this class never sums Bills.
 */
public final class AssetRepository {
    public static final String LEGACY = "legacy", ORIGINAL = "original";
    private final ReferenceStore store;
    public AssetRepository(ReferenceStore store) { this.store=store; }

    /** Only the supplied callback DB is used; no recursive SQLiteOpenHelper open. */
    public static void installV3Schema(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE accounts ADD COLUMN asset_origin TEXT NOT NULL DEFAULT 'legacy'");
        db.execSQL("ALTER TABLE accounts ADD COLUMN semantic_id INTEGER");
        db.execSQL("ALTER TABLE accounts ADD COLUMN owner_semantic_id INTEGER");
        db.execSQL("ALTER TABLE accounts ADD COLUMN asset_type INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE accounts ADD COLUMN asset_icon TEXT");
        db.execSQL("ALTER TABLE accounts ADD COLUMN icon_night TEXT");
        // The existing NOT NULL currency column belongs to the old experiment.
        // Asset.currency's constructor value is NULL, not a proven CNY default.
        db.execSQL("ALTER TABLE accounts ADD COLUMN asset_currency TEXT");
        db.execSQL("ALTER TABLE accounts ADD COLUMN asset_number REAL NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE accounts ADD COLUMN total_quota REAL NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE accounts ADD COLUMN position_weight INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE accounts ADD COLUMN hide INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE accounts ADD COLUMN into_total_asset INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE accounts ADD COLUMN out_day_not_next INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE accounts ADD COLUMN legacy_book_semantic_id INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE accounts ADD COLUMN share_quota_asset_semantic_id INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE accounts ADD COLUMN update_time INTEGER NOT NULL DEFAULT 0");
        db.execSQL("ALTER TABLE accounts ADD COLUMN group_name TEXT");
        db.execSQL("ALTER TABLE accounts ADD COLUMN simple_name TEXT");
        db.execSQL("ALTER TABLE accounts ADD COLUMN card_code TEXT");
        db.execSQL("ALTER TABLE accounts ADD COLUMN remark TEXT");
        db.execSQL("ALTER TABLE accounts ADD COLUMN in_account_date TEXT");
        db.execSQL("ALTER TABLE accounts ADD COLUMN out_account_date TEXT");
        db.execSQL("CREATE UNIQUE INDEX accounts_original_identity ON accounts(owner_semantic_id,semantic_id) WHERE asset_origin='original'");
        db.execSQL("CREATE INDEX accounts_original_order ON accounts(owner_semantic_id,position_weight DESC)");
        // Preserve list order and duplicates: getAccountBookId also uses the first
        // surviving list entry, although f.v itself only tests list membership.
        db.execSQL("CREATE TABLE asset_show_books(account_row_id INTEGER NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,position INTEGER NOT NULL,book_semantic_id INTEGER NOT NULL,PRIMARY KEY(account_row_id,position))");
        db.execSQL("CREATE INDEX asset_show_books_membership ON asset_show_books(account_row_id,book_semantic_id)");
        // v2's pair-only PK cannot represent per-user mappings. Old rows retain
        // their FK/time but have unknown raw ID and owner; neither is inferred.
        db.execSQL("ALTER TABLE category_accounts RENAME TO category_accounts_v2");
        db.execSQL("CREATE TABLE category_accounts(id INTEGER PRIMARY KEY AUTOINCREMENT,parent_semantic_id INTEGER NOT NULL,child_semantic_id INTEGER NOT NULL,account_row_id INTEGER REFERENCES accounts(id) ON DELETE SET NULL,updated INTEGER NOT NULL,owner_semantic_id INTEGER,raw_asset_semantic_id INTEGER,category_asset_semantic_id INTEGER,bridge_revision INTEGER NOT NULL DEFAULT 0,UNIQUE(owner_semantic_id,parent_semantic_id,child_semantic_id))");
        db.execSQL("CREATE UNIQUE INDEX category_accounts_legacy_pair ON category_accounts(parent_semantic_id,child_semantic_id) WHERE owner_semantic_id IS NULL");
        db.execSQL("INSERT INTO category_accounts(parent_semantic_id,child_semantic_id,account_row_id,updated) SELECT parent_semantic_id,child_semantic_id,account_row_id,updated FROM category_accounts_v2");
        db.execSQL("DROP TABLE category_accounts_v2");
    }

    /**
     * Asset() supplies primitive 0/false and null references, apart from its
     * ignored reimAssetNumber=-1. Entry forms must explicitly supply their type,
     * name and nullable currency, rather than deriving a type from cash/wallet.
     */
    public static final class Definition {
        public final String name;
        public final int assetType;
        public final String currency;
        public String assetIcon, iconNight, groupName, simpleName, cardCode, remark, inAccountDate, outAccountDate;
        public double assetNumber, totalQuota;
        public boolean hide, intoTotalAsset, outDayNotNext;
        public long legacyBookSemanticId, shareQuotaAssetSemanticId;
        public final List<Long> showBook = new ArrayList<>();
        public Definition(String name,int assetType,String currency) {
            if (name == null) throw new IllegalArgumentException("账户名称不能为null");
            this.name=name; this.assetType=assetType; this.currency=currency;
        }
    }

    /**
     * f.d metadata: random semantic ID including possible 0, max weight+1 and
     * current updateTime. The caller must perform any creation-history effects
     * in its surrounding Store transaction. No bootstrap accounts are created.
     */
    public long createOriginal(int ownerSemanticId, Definition definition) {
        CreationResult result = tryCreateOriginal(ownerSemanticId, definition);
        if (!result.saved) throw new IllegalStateException("原版账户元数据保存失败：" + result.failureType);
        return result.rowId;
    }

    public static final class CreationResult {
        public final long rowId, semanticId, updateTime;
        public final int ownerSemanticId, positionWeight;
        public final boolean saved;
        public final String failureType;
        private CreationResult(long row, int owner, int semantic, int weight, long time, Exception error) {
            rowId=row;ownerSemanticId=owner;semanticId=semantic;positionWeight=weight;updateTime=time;
            saved=error==null;failureType=error==null?"":error.getClass().getName();
        }
    }

    /** f.d allocates identity before save(); its caller can continue after save=false. */
    public CreationResult tryCreateOriginal(int ownerSemanticId, Definition definition) {
        if (definition == null || !Double.isFinite(definition.assetNumber) || !Double.isFinite(definition.totalQuota))
            throw new IllegalArgumentException("账户元数据无效");
        synchronized (store) {
            SQLiteDatabase db=store.getWritableDatabase();
            long now=System.currentTimeMillis();
            Random random=new Random(); int semanticId;
            do { semanticId=random.nextInt(Integer.MAX_VALUE); }
            while (p(semanticId,ownerSemanticId) != null);
            JSONObject maximum=store.one("SELECT COALESCE(MAX(position_weight),0) n FROM accounts WHERE asset_origin='original' AND owner_semantic_id=?",""+ownerSemanticId);
            int weight=maximum.optInt("n")+1;
            long[] assignedRow={0};
            try {
                db.beginTransaction();
                try {
                    long rowId=insertOriginal(db,ownerSemanticId,semanticId,weight,now,definition,row->assignedRow[0]=row);
                    db.setTransactionSuccessful();return new CreationResult(rowId,ownerSemanticId,semanticId,weight,now,null);
                } finally { db.endTransaction(); }
            } catch (Exception error) { AccountingCommitScope.failed(store,error); return new CreationResult(assignedRow[0],ownerSemanticId,semanticId,weight,now,error); }
        }
    }

    /** Explicit recovered/import metadata; not a name-to-ID seed or a Bill operation. */
    public long insertRecovered(int ownerSemanticId,long assetSemanticId,int positionWeight,long updateTime,Definition definition) {
        synchronized (store) {
            SQLiteDatabase db=store.getWritableDatabase(); db.beginTransaction();
            try {
                long rowId=insertOriginal(db,ownerSemanticId,assetSemanticId,positionWeight,updateTime,definition);
                db.setTransactionSuccessful(); return rowId;
            } finally { db.endTransaction(); }
        }
    }

    private static long insertOriginal(SQLiteDatabase db,int owner,long semanticId,int weight,long time,Definition value) {
        return insertOriginal(db,owner,semanticId,weight,time,value,null);
    }
    private static long insertOriginal(SQLiteDatabase db,int owner,long semanticId,int weight,long time,Definition value,java.util.function.LongConsumer assignedRow) {
        if (value == null || !Double.isFinite(value.assetNumber) || !Double.isFinite(value.totalQuota)) throw new IllegalArgumentException("账户元数据无效");
        ContentValues fields=new ContentValues();
        fields.put("name",value.name); fields.put("type","original");
        fields.put("currency",value.currency == null ? "" : value.currency);
        fields.put("asset_origin",ORIGINAL); fields.put("semantic_id",semanticId); fields.put("owner_semantic_id",owner);
        fields.put("asset_type",value.assetType); fields.put("asset_currency",value.currency);
        fields.put("asset_icon",value.assetIcon); fields.put("icon_night",value.iconNight);
        fields.put("asset_number",value.assetNumber); fields.put("total_quota",value.totalQuota);
        fields.put("position_weight",weight); fields.put("update_time",time);
        fields.put("hide",value.hide?1:0); fields.put("into_total_asset",value.intoTotalAsset?1:0); fields.put("out_day_not_next",value.outDayNotNext?1:0);
        fields.put("legacy_book_semantic_id",value.legacyBookSemanticId); fields.put("share_quota_asset_semantic_id",value.shareQuotaAssetSemanticId);
        fields.put("group_name",value.groupName); fields.put("simple_name",value.simpleName); fields.put("card_code",value.cardCode); fields.put("remark",value.remark);
        fields.put("in_account_date",value.inAccountDate); fields.put("out_account_date",value.outAccountDate);
        long rowId=db.insertOrThrow("accounts",null,fields);
        if(rowId==-1)throw new IllegalStateException("原版账户未插入");
        if(assignedRow!=null)assignedRow.accept(rowId);
        replaceShowBook(db,rowId,value.showBook);
        return rowId;
    }

    public JSONObject byRowId(long rowId) { return store.one("SELECT * FROM accounts WHERE id=?",""+rowId); }

    /** f.P deliberately has no hide, type, archived or book predicate. */
    public JSONObject p(long rawAssetSemanticId,int userSemanticId) {
        return store.one("SELECT * FROM accounts WHERE asset_origin='original' AND owner_semantic_id=? AND semantic_id=? ORDER BY id LIMIT 1",""+userSemanticId,""+rawAssetSemanticId);
    }

    /** Caller supplies the application's current semantic book ID for f.Q. */
    public JSONObject q(long rawAssetSemanticId,int userSemanticId,long currentBookSemanticId) { return r(rawAssetSemanticId,userSemanticId,currentBookSemanticId); }

    /** f.R only adds its explicit book's showBook allowlist; hide/type stay unfiltered. */
    public JSONObject r(long rawAssetSemanticId,int userSemanticId,long selectedBookSemanticId) {
        return store.one("SELECT a.* FROM accounts a WHERE a.asset_origin='original' AND a.owner_semantic_id=? AND a.semantic_id=? AND "+allowsBookSql()+" ORDER BY a.id LIMIT 1",""+userSemanticId,""+rawAssetSemanticId,""+selectedBookSemanticId);
    }

    private static String allowsBookSql() {
        return "(NOT EXISTS(SELECT 1 FROM asset_show_books s WHERE s.account_row_id=a.id) OR EXISTS(SELECT 1 FROM asset_show_books s WHERE s.account_row_id=a.id AND s.book_semantic_id=?))";
    }

    /** manager.c.m -> f.u0, or f.f0 when book is null (original book argument -1). */
    public List<JSONObject> picker(int userSemanticId,Long bookSemanticId) {
        return ordered(userSemanticId,bookSemanticId,"a.hide=0 AND a.asset_type IN(1,2,3,4,5)");
    }

    /** f.A ignores hide/book globally; f.j0 adds both for the scoped match path. */
    public List<JSONObject> matching(int userSemanticId,Long bookSemanticId) {
        return ordered(userSemanticId,bookSemanticId,(bookSemanticId==null?"":"a.hide=0 AND ")+"a.asset_type NOT IN(6,7,8,9)");
    }

    private List<JSONObject> ordered(int owner,Long book,String condition) {
        String sql="SELECT a.* FROM accounts a WHERE a.asset_origin='original' AND a.owner_semantic_id=? AND "+condition;
        if (book != null) sql+=" AND "+allowsBookSql();
        sql+=" ORDER BY a.position_weight DESC,a.id";
        return book==null?store.query(sql,""+owner):store.query(sql,""+owner,""+book);
    }

    /** Old rows remain available explicitly; names/currency/type never imply original identity. */
    public List<JSONObject> legacyPicker() { return store.query("SELECT * FROM accounts WHERE asset_origin='legacy' AND archived=0 ORDER BY id"); }

    public List<Long> showBook(long accountRowId) {
        synchronized (store) {
            List<Long> result=new ArrayList<>();
            for (JSONObject row:store.query("SELECT book_semantic_id FROM asset_show_books WHERE account_row_id=? ORDER BY position",""+accountRowId)) result.add(row.optLong("book_semantic_id"));
            return result;
        }
    }

    public boolean allowsBook(long accountRowId,long bookSemanticId) {
        synchronized (store) {
            return store.one("SELECT a.id FROM accounts a WHERE a.id=? AND "+allowsBookSql(),""+accountRowId,""+bookSemanticId) != null;
        }
    }

    public void setShowBook(long accountRowId,List<Long> bookSemanticIds) {
        synchronized (store) {
            SQLiteDatabase db=store.getWritableDatabase(); db.beginTransaction();
            try {
                JSONObject account=byRowId(accountRowId);
                if (account==null || !ORIGINAL.equals(account.optString("asset_origin"))) throw new IllegalArgumentException("原账户不存在");
                replaceShowBook(db,accountRowId,bookSemanticIds);
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
        }
    }

    private static void replaceShowBook(SQLiteDatabase db,long rowId,List<Long> semanticIds) {
        if (semanticIds==null) throw new IllegalArgumentException("账本可见列表不能为null");
        db.delete("asset_show_books","account_row_id=?",new String[]{""+rowId});
        for (int i=0;i<semanticIds.size();i++) {
            Long book=semanticIds.get(i); if (book==null) throw new IllegalArgumentException("账本语义标识不能为null");
            ContentValues value=new ContentValues(); value.put("account_row_id",rowId); value.put("position",i); value.put("book_semantic_id",book);
            db.insertOrThrow("asset_show_books",null,value);
        }
    }

    public static OriginalAssetSelection.AssetRef reference(JSONObject account) {
        return account==null || !ORIGINAL.equals(account.optString("asset_origin")) || account.isNull("semantic_id") ? null
            : new OriginalAssetSelection.AssetRef(account.optLong("id"),account.optLong("semantic_id"));
    }

    public OriginalAssetSelection.Mapping categoryMapping(int ownerSemanticId,int parentSemanticId,int childSemanticId) {
        return mapping(store.one("SELECT * FROM category_accounts WHERE owner_semantic_id=? AND parent_semantic_id=? AND child_semantic_id=?",""+ownerSemanticId,""+parentSemanticId,""+childSemanticId));
    }

    /** Explicit bridge for a v2 pair whose raw ID and original owner were lost. */
    public OriginalAssetSelection.Mapping legacyCategoryMapping(int parentSemanticId,int childSemanticId) {
        return mapping(store.one("SELECT * FROM category_accounts WHERE owner_semantic_id IS NULL AND parent_semantic_id=? AND child_semantic_id=?",""+parentSemanticId,""+childSemanticId));
    }

    private static OriginalAssetSelection.Mapping mapping(JSONObject row) {
        if (row==null) return OriginalAssetSelection.Mapping.missing();
        Long local=row.isNull("account_row_id")?null:row.optLong("account_row_id");
        return row.isNull("raw_asset_semantic_id")?OriginalAssetSelection.Mapping.unrecovered(local)
            : OriginalAssetSelection.Mapping.known(row.optLong("raw_asset_semantic_id"),local);
    }

    /** Legacy compatibility transaction; i0.a casts long raw ID to int. */
    public void learnCategoryAccount(SQLiteDatabase db,int ownerSemanticId,int parentSemanticId,int childSemanticId,
            long rawAssetSemanticId,Long accountRowId,long now) {
        learnCategoryAccount(db,ownerSemanticId,parentSemanticId,childSemanticId,rawAssetSemanticId,accountRowId,now,false);
    }

    /** i0.b's allocation/lookup can throw; only entity save catches Exception and returns false. */
    public boolean tryLearnCategoryAccount(SQLiteDatabase db,int ownerSemanticId,int parentSemanticId,int childSemanticId,
            long rawAssetSemanticId,Long accountRowId,long now) {
        return learnCategoryAccount(db,ownerSemanticId,parentSemanticId,childSemanticId,rawAssetSemanticId,accountRowId,now,true);
    }

    private boolean learnCategoryAccount(SQLiteDatabase db,int ownerSemanticId,int parentSemanticId,int childSemanticId,
            long rawAssetSemanticId,Long accountRowId,long now,boolean ignoreSaveException) {
        synchronized (store) {
            int storedRaw=(int)rawAssetSemanticId;
            if (accountRowId!=null) {
                JSONObject account=byRowId(accountRowId);
                if (account==null || !ORIGINAL.equals(account.optString("asset_origin")) || account.isNull("owner_semantic_id")
                    || account.optInt("owner_semantic_id")!=ownerSemanticId || account.optLong("semantic_id")!=storedRaw) throw new IllegalArgumentException("分类账户的语义标识与本地主键不匹配");
            }
            JSONObject old=store.one("SELECT id,category_asset_semantic_id FROM category_accounts WHERE owner_semantic_id=? AND parent_semantic_id=? AND child_semantic_id=?",""+ownerSemanticId,""+parentSemanticId,""+childSemanticId);
            int mappingId;
            if (old!=null && !old.isNull("category_asset_semantic_id") && old.optInt("category_asset_semantic_id")!=0) mappingId=old.optInt("category_asset_semantic_id");
            else mappingId=(int)store.scalar("SELECT COALESCE(MAX(category_asset_semantic_id),0) n FROM category_accounts WHERE owner_semantic_id=?",""+ownerSemanticId)+1;
            ContentValues value=new ContentValues(); value.put("owner_semantic_id",ownerSemanticId); value.put("parent_semantic_id",parentSemanticId); value.put("child_semantic_id",childSemanticId);
            // Experimental compatibility ordering: original updateTime can tie in
            // one millisecond. This field never participates in i0/G0's lookup.
            value.put("bridge_revision",nextBridgeRevision());
            value.put("raw_asset_semantic_id",storedRaw); value.put("category_asset_semantic_id",mappingId); value.put("updated",now);
            if (accountRowId==null) value.putNull("account_row_id"); else value.put("account_row_id",accountRowId);
            try {
                if (old==null) {
                    if(db.insertOrThrow("category_accounts",null,value)==-1)throw new IllegalStateException("分类账户映射未插入");
                } else AccountingCommitScope.requireChanged(store,db.update("category_accounts",value,"id=?",new String[]{""+old.optLong("id")}));
                return true;
            } catch(Exception error) { AccountingCommitScope.failed(store,error);
                if(ignoreSaveException)return false;
                throw error;
            }
        }
    }

    long nextBridgeRevision() {
        return Math.addExact(store.scalar("SELECT COALESCE(MAX(bridge_revision),0) n FROM category_accounts"),1L);
    }

    /** Validate imported original metadata without applying picker visibility as a save gate. */
    public void validateRestored() {
        synchronized (store) {
            for (JSONObject row:store.query("SELECT * FROM accounts")) {
                String origin=row.optString("asset_origin");
                if (!integer(row,"id",1,Long.MAX_VALUE,false)) throw new IllegalArgumentException("备份账户本地主键无效");
                if (!LEGACY.equals(origin) && !ORIGINAL.equals(origin)) throw new IllegalArgumentException("备份账户来源无效");
                if (LEGACY.equals(origin)) {
                    if (!row.isNull("semantic_id") || !row.isNull("owner_semantic_id")) throw new IllegalArgumentException("旧账户不应携带推测的原始标识");
                } else if (!integer(row,"semantic_id",Long.MIN_VALUE,Long.MAX_VALUE,false) || !integer(row,"owner_semantic_id",Integer.MIN_VALUE,Integer.MAX_VALUE,false)) throw new IllegalArgumentException("备份账户原始标识无效");
                for (String key:new String[]{"asset_type","position_weight"}) if(!integer(row,key,Integer.MIN_VALUE,Integer.MAX_VALUE,false))throw new IllegalArgumentException("备份账户元数值无效");
                for (String key:new String[]{"legacy_book_semantic_id","share_quota_asset_semantic_id","update_time"}) if(!integer(row,key,Long.MIN_VALUE,Long.MAX_VALUE,false))throw new IllegalArgumentException("备份账户元数值无效");
                for (String key:new String[]{"hide","into_total_asset","out_day_not_next"}) if(!integer(row,key,0,1,false))throw new IllegalArgumentException("备份账户开关无效");
                for (String key:new String[]{"asset_number","total_quota"}) if(!(row.opt(key) instanceof Number) || !Double.isFinite(((Number)row.opt(key)).doubleValue()))throw new IllegalArgumentException("备份账户金额无效");
                for (String key:new String[]{"asset_currency","asset_icon","icon_night","group_name","simple_name","card_code","remark","in_account_date","out_account_date"})
                    if(!row.isNull(key) && !(row.opt(key) instanceof String))throw new IllegalArgumentException("备份账户文本元数据无效");
            }
            for (JSONObject row:store.query("SELECT * FROM asset_show_books")) {
                JSONObject account=byRowId(row.optLong("account_row_id"));
                if (account==null || !ORIGINAL.equals(account.optString("asset_origin")) || !integer(row,"position",0,Integer.MAX_VALUE,false)
                    || !integer(row,"book_semantic_id",Long.MIN_VALUE,Long.MAX_VALUE,false)) throw new IllegalArgumentException("备份账户账本范围无效");
            }
            for (JSONObject row:store.query("SELECT * FROM category_accounts")) {
                if(!integer(row,"bridge_revision",0,Long.MAX_VALUE,false))throw new IllegalArgumentException("备份分类账户桥接顺序无效");
                boolean legacy=row.isNull("owner_semantic_id");
                if (legacy) {
                    if(!row.isNull("raw_asset_semantic_id") || !row.isNull("category_asset_semantic_id"))throw new IllegalArgumentException("旧分类账户映射不应携带推测的原始标识");
                } else if (!integer(row,"owner_semantic_id",Integer.MIN_VALUE,Integer.MAX_VALUE,false)
                    || !integer(row,"raw_asset_semantic_id",Integer.MIN_VALUE,Integer.MAX_VALUE,false)
                    || !integer(row,"category_asset_semantic_id",Integer.MIN_VALUE,Integer.MAX_VALUE,false)) throw new IllegalArgumentException("备份分类账户映射原始标识无效");
                if (!legacy && !row.isNull("account_row_id")) {
                    JSONObject account=p(row.optLong("raw_asset_semantic_id"),row.optInt("owner_semantic_id"));
                    if (account==null || account.optLong("id")!=row.optLong("account_row_id")) throw new IllegalArgumentException("备份分类账户映射引用不一致");
                }
            }
        }
    }

    private static boolean integer(JSONObject row,String key,long min,long max,boolean nullable) {
        if(row.isNull(key))return nullable;
        Object value=row.opt(key);
        return (value instanceof Integer || value instanceof Long) && ((Number)value).longValue()>=min && ((Number)value).longValue()<=max;
    }
}
