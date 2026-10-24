package com.wangc.bill.reference;

import android.content.Context;
import java.util.List;
import org.json.JSONObject;

/** Asset.getAccountBookId: ordered showBook IDs, then the application's current book. */
public final class OriginalAssetBookResolver {
    public enum Route {SHOW_BOOK, CURRENT_BOOK, OWN_SYSTEM_BOOK}

    /** Both ID spaces remain explicit; callers writing original Bill.bookId use semanticId. */
    public static final class Resolution {
        public final long rowId, semanticId;
        public final int ownerSemanticId;
        public final Route route;
        private Resolution(JSONObject row, Route route) {
            if (row == null || row.isNull("semantic_id") || row.isNull("owner_semantic_id"))
                throw new IllegalStateException("原版手续费账本缺少身份");
            rowId = row.optLong("id"); semanticId = row.optLong("semantic_id");
            ownerSemanticId = row.optInt("owner_semantic_id"); this.route = route;
        }
    }

    private final ReferenceStore store;
    private final int currentOwner;

    public OriginalAssetBookResolver(ReferenceStore store, int currentOwner) {
        if (store == null) throw new IllegalArgumentException("账本存储不能为空");
        this.store = store; this.currentOwner = currentOwner;
    }

    public long resolve(long sourceAssetSemanticId) { return resolveIdentity(sourceAssetSemanticId).semanticId; }

    /** AutoAdd.Z has already selected the current user's source Asset.P, never a local PK. */
    public Resolution resolveIdentity(long sourceAssetSemanticId) {
        synchronized (store) {
            JSONObject asset = store.assetRepository().p(sourceAssetSemanticId, currentOwner);
            if (asset == null) throw new IllegalArgumentException("原版转出账户不存在");
            return resolveShowBook(store.assetRepository().showBook(asset.optLong("id")));
        }
    }

    /**
     * Asset.java:88-100, a.n/C: a valid entry needs only current-owner + semantic identity.
     * Hidden/archived/type and the obsolete Asset.accountBookId field are not predicates.
     * A null list mirrors getShowBook()'s empty-list getter without persisting a mutation.
     */
    public Resolution resolveShowBook(List<Long> showBook) {
        synchronized (store) {
            if (showBook != null) for (Long semantic : showBook) {
                JSONObject book = store.bookRepository().byIdentity(currentOwner, semantic.longValue());
                if (book != null) return new Resolution(book, Route.SHOW_BOOK);
            }
            // Host bridge: current_book is the global local SQLite PK, as p0.u -> a.t.
            // The original MyApplication caches c(); this host rereads its selected-book
            // preference. resolveCurrent restores g()'s ownership/share-users fallback.
            long rawCurrent = store.context().getSharedPreferences("reference_settings", Context.MODE_PRIVATE)
                    .getLong("current_book", 1L);
            JSONObject current = store.bookRepository().resolveCurrent(currentOwner, rawCurrent);
            if (current == null) throw new IllegalStateException("当前原版账本不存在");
            if (current.optInt("owner_semantic_id") != currentOwner) {
                JSONObject own = store.bookRepository().systemBook(currentOwner);
                // The original dereferences a missing a.C(); expose the missing state
                // explicitly instead of inventing semantic ID 1 or creating a book.
                if (own == null) throw new IllegalStateException("当前用户的原版系统账本不存在");
                return new Resolution(own, Route.OWN_SYSTEM_BOOK);
            }
            return new Resolution(current, Route.CURRENT_BOOK);
        }
    }
}
