package com.wangc.bill.reference;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/** A user operation commits its ledger, balances, history and receipt together. */
public final class AccountingCommitScope {
    private AccountingCommitScope() {}
    @FunctionalInterface public interface Operation<T> { T run(); }
    private static final ThreadLocal<Frame> CURRENT = new ThreadLocal<>();
    private static final class Frame {
        final ReferenceStore store;
        final List<Runnable> afterCommit = new ArrayList<>();
        RuntimeException failure;
        Frame(ReferenceStore store) { this.store = store; }
    }
    public static boolean active(ReferenceStore store) {
        Frame frame = CURRENT.get();
        return frame != null && frame.store == store;
    }
    /** Low-level source-compatible save=false becomes rollback-only in a user operation. */
    public static void failed(ReferenceStore store, Exception cause) {
        Frame frame = CURRENT.get();
        if (frame != null && frame.store == store && frame.failure == null)
            frame.failure = new IllegalStateException("保存未完成，所有记账变更已撤销，可安全重试", cause);
    }
    public static void requireChanged(ReferenceStore store, int rows) {
        if (active(store) && rows != 1)
            throw new IllegalStateException("数据已变化，未能完整保存记账操作");
    }
    /** Returns true only if the effect was queued. Database history is never queued here. */
    public static boolean defer(ReferenceStore store, Runnable effect) {
        Frame frame = CURRENT.get();
        if (frame == null || frame.store != store) return false;
        frame.afterCommit.add(effect);
        return true;
    }
    public static <T> T run(ReferenceStore store, Operation<T> operation) {
        if (store == null || operation == null) throw new IllegalArgumentException("保存操作不能为空");
        synchronized (store) {
            Frame previous = CURRENT.get();
            if (previous != null) {
                if (previous.store != store) throw new IllegalStateException("不能混合不同数据库的记账操作");
                try { return operation.run(); }
                catch (RuntimeException error) { failed(store, error); throw error; }
            }
            SQLiteDatabase db = store.getWritableDatabase();
            if (db.inTransaction()) throw new IllegalStateException("记账操作不能加入未知的外部事务");
            Frame frame = new Frame(store);
            T result;
            db.beginTransaction();
            CURRENT.set(frame);
            try {
                result = operation.run();
                if (frame.failure != null) throw frame.failure;
                db.setTransactionSuccessful();
            } finally {
                CURRENT.remove();
                db.endTransaction();
            }
            // Persistence has succeeded. A listener failure must never invite another debit.
            for (Runnable effect : frame.afterCommit) {
                try { effect.run(); }
                catch (RuntimeException error) {
                    Log.w("AccountingCommit", "Post-commit effect failed: " + error.getClass().getName());
                }
            }
            return result;
        }
    }
}
