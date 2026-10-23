package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;

/** Explicit synthetic rows for old local-FK regression tests, never production bootstrap. */
final class ReferenceTestAccounts {
    private ReferenceTestAccounts() {}

    static void installLegacyFixture(ReferenceStore store) {
        synchronized (store) {
            SQLiteDatabase db=store.getWritableDatabase();
            db.beginTransaction();
            try {
                for (int id=1;id<=3;id++) {
                    JSONObject existing=store.one("SELECT * FROM accounts WHERE id=?",""+id);
                    if (existing!=null) {
                        if (!"legacy".equals(existing.optString("asset_origin"))
                                || !existing.isNull("semantic_id") || !existing.isNull("owner_semantic_id")) {
                            throw new AssertionError("Synthetic legacy fixture would replace original account "+id);
                        }
                        continue;
                    }
                    ContentValues values=new ContentValues();
                    values.put("id",id);
                    values.put("name","合成兼容账户"+id);
                    values.put("type","cash");
                    values.put("currency","CNY");
                    values.put("opening",0);
                    values.put("archived",0);
                    values.put("asset_origin","legacy");
                    values.putNull("semantic_id");
                    values.putNull("owner_semantic_id");
                    db.insertOrThrow("accounts",null,values);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }
}
