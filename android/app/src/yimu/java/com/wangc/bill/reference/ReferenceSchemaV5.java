package com.wangc.bill.reference;

import android.database.sqlite.SQLiteDatabase;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Local storage for the recovered independent relations/instalments/operation queue. */
final class ReferenceSchemaV5 {
    private ReferenceSchemaV5() {}
    private static final Set<String> CANDIDATE_FIELDS=new HashSet<>(Arrays.asList(
            "save_kind","refund_owner_semantic_id","refund_semantic_id","refund_item_raw"));
    static void install(SQLiteDatabase db) {
        OriginalBillRelatedRepository.installV5Schema(db);
        OriginalInstalmentRepository.installV5Schema(db);
        OriginalCurdHistoryRepository.installV5Schema(db);
        // Host idempotence links differ: many refund confirmations can refer to one Bill.
        // None of these fields is claimed to be part of the original Refund/Bill model.
        db.execSQL("ALTER TABLE candidates ADD COLUMN save_kind TEXT NOT NULL DEFAULT 'bill' CHECK(save_kind IN ('bill','refund'))");
        db.execSQL("ALTER TABLE candidates ADD COLUMN refund_owner_semantic_id INTEGER");
        db.execSQL("ALTER TABLE candidates ADD COLUMN refund_semantic_id INTEGER");
        db.execSQL("ALTER TABLE candidates ADD COLUMN refund_item_raw TEXT");
    }
    static boolean acceptsBackupColumn(int version,String table,String column) {
        if(version<5&&table.equals("candidates")&&CANDIDATE_FIELDS.contains(column))return false;
        return ReferenceSchemaV4.acceptsBackupColumn(version,table,column);
    }
    static void validateRestored(ReferenceStore store) {
        new OriginalBillRelatedRepository(store).validateRestored();
        new OriginalInstalmentRepository(store).validateRestored();
        new OriginalCurdHistoryRepository(store).validateRestored();
    }
}
