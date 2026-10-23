package com.wangc.bill.reference;

import android.database.sqlite.SQLiteDatabase;

/** Independent original Transfer storage; legacy transaction rows keep their identities. */
final class ReferenceSchemaV6 {
    private ReferenceSchemaV6() {}
    static void install(SQLiteDatabase db) {
        OriginalTransferRepository.installV6Schema(db);
    }
    static void validateRestored(ReferenceStore store) {
        new OriginalTransferRepository(store).validateRestored();
    }
}
