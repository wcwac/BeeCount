package com.wangc.bill.reference;

import java.util.function.LongSupplier;

/** Local i1.d/w0 stages; their g() HTTP call is excluded, and neither method posts a local event. */
public final class OriginalLendActions {
    public static final class Result {
        public final OriginalLendRecord record;
        public final OriginalLendRepository.SaveResult save;
        /** i1.d returns its int lendId widened to long, including after save=false. */
        public final long semanticId;
        private Result(OriginalLendRecord record, OriginalLendRepository.SaveResult save) {
            this.record = record; this.save = save; semanticId = record.lendId;
        }
    }

    private final ReferenceStore store;
    private final int currentOwner;
    private final OriginalLendRepository repository;
    private final LongSupplier clock;

    public OriginalLendActions(ReferenceStore store, int currentOwner) {
        this(store, currentOwner, new OriginalLendRepository(store), System::currentTimeMillis);
    }

    OriginalLendActions(ReferenceStore store, int currentOwner, OriginalLendRepository repository, LongSupplier clock) {
        if (store == null || repository == null || clock == null)
            throw new IllegalArgumentException("Lend action dependencies are required");
        this.store = store; this.currentOwner = currentOwner; this.repository = repository; this.clock = clock;
    }

    /** i1.d always replaces the semantic ID, even for an already saved/nonzero-ID model. */
    public Result add(OriginalLendRecord record) {
        if (record == null) throw new IllegalArgumentException("Lend is required");
        synchronized (store) {
            independent();
            record.userId = currentOwner;
            record.updateTime = clock.getAsLong();
            record.lendId = repository.allocateSemanticId(currentOwner);
            // The source makes a second clock call after allocation; do not reuse updateTime.
            record.createTime = clock.getAsLong();
            return new Result(record, repository.trySaveOriginalRaw(record));
        }
    }

    /** i1.w0 stamps only updateTime; owner, semantic ID and createTime stay as supplied. */
    public Result update(OriginalLendRecord record) {
        if (record == null) throw new IllegalArgumentException("Lend is required");
        synchronized (store) {
            independent();
            record.updateTime = clock.getAsLong();
            return new Result(record, repository.trySaveOriginalRaw(record));
        }
    }

    private void independent() {
        if (store.getWritableDatabase().inTransaction())
            throw new IllegalStateException("Original Lend stages require independent saves");
    }
}
