package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;

/** Experimental-storage bridge; it does not claim to be the original LitePal schema. */
final class ReferenceSchemaV3 {
    private ReferenceSchemaV3() {}
    private static final Set<String> OLD_BOOK = fields("id,name,currency,budget,archived,created");
    private static final Set<String> OLD_ACCOUNT = fields("id,name,type,currency,opening,archived");
    private static final Set<String> OLD_MAPPING = fields("parent_semantic_id,child_semantic_id,account_row_id,updated");
    private static final Set<String> RAW_TRANSACTION = fields("owner_semantic_id,raw_asset_semantic_id,raw_to_asset_semantic_id");

    static void install(SQLiteDatabase db) {
        BookRepository.installV3Schema(db);
        AssetRepository.installV3Schema(db);
        db.execSQL("ALTER TABLE transactions ADD COLUMN owner_semantic_id INTEGER");
        db.execSQL("ALTER TABLE transactions ADD COLUMN raw_asset_semantic_id INTEGER");
        db.execSQL("ALTER TABLE transactions ADD COLUMN raw_to_asset_semantic_id INTEGER");
    }

    /** Older files cannot assign a guessed semantic identity, even if extra keys are present. */
    static boolean acceptsOldColumn(int version, String table, String column) {
        if (version >= 3) return true;
        if (table.equals("books")) return OLD_BOOK.contains(column);
        if (table.equals("accounts")) return OLD_ACCOUNT.contains(column);
        if (table.equals("category_accounts")) return OLD_MAPPING.contains(column);
        if (table.equals("transactions") && RAW_TRANSACTION.contains(column)) return false;
        if (version == 1 && table.equals("categories")) return fields("id,name,kind,icon,archived").contains(column);
        return version != 1 || !table.equals("transactions")
            || !(column.equals("parent_semantic_id") || column.equals("child_semantic_id"));
    }

    /** A raw sentinel is stored separately from the nullable experimental FK. */
    static void applyAssetReferences(ReferenceStore store, JSONObject input, long accountRow,
            long destinationRow, boolean automatic, ContentValues values) {
        Long owner;
        if(!input.has("owner_semantic_id"))owner=Long.valueOf(ReferenceStore.LOCAL_OFFLINE_OWNER);
        else if(input.isNull("owner_semantic_id"))owner=null;
        else owner=Long.valueOf(integer(input,"owner_semantic_id",Integer.MIN_VALUE,Integer.MAX_VALUE));
        Long source = null;
        JSONObject account = accountRow > 0 ? store.assetRepository().byRowId(accountRow) : null;
        JSONObject destination = destinationRow > 0 ? store.assetRepository().byRowId(destinationRow) : null;
        if (accountRow <= 0) {
            source = rawOrDefault(input, "raw_asset_semantic_id", noAccountDefault(owner,automatic));
        } else if (account != null && AssetRepository.ORIGINAL.equals(account.optString("asset_origin"))) {
            source = account.optLong("semantic_id");
            owner = (long) account.optInt("owner_semantic_id");
            requireSameIfPresent(input, "raw_asset_semantic_id", source);
            requireSameIfPresent(input, "owner_semantic_id", owner);
        }
        Long target = null;
        if (destinationRow <= 0) {
            target = rawOrDefault(input, "raw_to_asset_semantic_id", noAccountDefault(owner,automatic));
        } else if (destination != null && AssetRepository.ORIGINAL.equals(destination.optString("asset_origin"))) {
            target = destination.optLong("semantic_id");
            long targetOwner = destination.optInt("owner_semantic_id");
            boolean explicitOwner=input.has("owner_semantic_id")&&!input.isNull("owner_semantic_id");
            boolean originalSource=account!=null&&AssetRepository.ORIGINAL.equals(account.optString("asset_origin"));
            if (owner != null && (explicitOwner||originalSource) && owner != targetOwner) throw new IllegalArgumentException("转账账户不属于同一本地身份");
            owner = targetOwner;
            requireSameIfPresent(input, "raw_to_asset_semantic_id", target);
        }
        if(owner==null&&(source!=null||target!=null))throw new IllegalArgumentException("账户语义编号缺少身份");
        putNullable(values, "owner_semantic_id", owner);
        putNullable(values, "raw_asset_semantic_id", source);
        putNullable(values, "raw_to_asset_semantic_id", target);
    }

    private static Long rawOrDefault(JSONObject input, String key, Long fallback) {
        if(!input.has(key))return fallback;
        if(input.isNull(key))return null;
        return Long.valueOf(integer(input,key,Long.MIN_VALUE,Long.MAX_VALUE));
    }

    private static Long noAccountDefault(Long owner,boolean automatic) {
        if(owner==null)return null;
        return Long.valueOf(automatic?-1L:0L);
    }

    private static long integer(JSONObject row, String key, long min, long max) {
        Object value = row.opt(key);
        if (!(value instanceof Integer || value instanceof Long)) throw new IllegalArgumentException("账户标识必须为整数");
        long result = ((Number)value).longValue();
        if (result < min || result > max) throw new IllegalArgumentException("账户标识超出范围");
        return result;
    }

    private static void requireSameIfPresent(JSONObject input, String key, long expected) {
        if (input.has(key) && !input.isNull(key) && integer(input, key, Long.MIN_VALUE, Long.MAX_VALUE) != expected)
            throw new IllegalArgumentException("账户语义编号与本地引用不一致");
    }

    private static void putNullable(ContentValues values, String key, Long value) {
        if (value == null) values.putNull(key); else values.put(key, value);
    }

    static void validateRestored(ReferenceStore store) {
        for (JSONObject row : store.query("SELECT * FROM transactions")) {
            Long owner = row.isNull("owner_semantic_id") ? null
                : integer(row, "owner_semantic_id", Integer.MIN_VALUE, Integer.MAX_VALUE);
            validateReference(store, row, owner, "account_id", "raw_asset_semantic_id");
            validateReference(store, row, owner, "to_account_id", "raw_to_asset_semantic_id");
        }
    }

    private static void validateReference(ReferenceStore store, JSONObject row, Long owner, String localKey, String rawKey) {
        Long raw = row.isNull(rawKey) ? null : integer(row, rawKey, Long.MIN_VALUE, Long.MAX_VALUE);
        if (row.isNull(localKey)) {
            if (raw != null && owner == null) throw new IllegalArgumentException("备份账户语义编号缺少身份");
            return;
        }
        JSONObject account = store.assetRepository().byRowId(row.optLong(localKey));
        if (account == null) throw new IllegalArgumentException("备份账户引用不存在");
        if (AssetRepository.ORIGINAL.equals(account.optString("asset_origin"))) {
            if (owner == null || raw == null || owner.longValue() != account.optInt("owner_semantic_id") || raw.longValue() != account.optLong("semantic_id"))
                throw new IllegalArgumentException("备份账户语义编号与本地主键不一致");
        } else if (raw != null) throw new IllegalArgumentException("旧账户不能推测原版语义编号");
    }

    private static Set<String> fields(String csv) { return new HashSet<>(Arrays.asList(csv.split(","))); }
}
