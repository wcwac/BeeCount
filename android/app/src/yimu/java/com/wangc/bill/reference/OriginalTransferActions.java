package com.wangc.bill.reference;

import java.util.function.LongSupplier;

/** Local w2.g/h/q0 stages. The original e() HTTP stage is deliberately absent. */
public final class OriginalTransferActions {
    @FunctionalInterface public interface Effects {
        /** Local n0 tail, after the entity save returns, including save=false. */
        void transferChanged(OriginalTransferRecord record, boolean saved);
    }

    public static final class Result {
        /** The same mutable model passed to the save and notification stages. */
        public final OriginalTransferRecord record;
        public final OriginalTransferRepository.SaveResult save;
        /** Like g/h's return, observed after the optional local notification. */
        public final long semanticId;
        private Result(OriginalTransferRecord record, OriginalTransferRepository.SaveResult save) {
            this.record = record; this.save = save; semanticId = record.transferId;
        }
    }

    private final ReferenceStore store;
    private final int currentOwner;
    private final Effects effects;
    private final OriginalTransferRepository repository;
    private final LongSupplier clock;

    public OriginalTransferActions(ReferenceStore store, int currentOwner, Effects effects) {
        this(store, currentOwner, effects, new OriginalTransferRepository(store), System::currentTimeMillis);
    }

    OriginalTransferActions(ReferenceStore store, int currentOwner, Effects effects,
            OriginalTransferRepository repository, LongSupplier clock) {
        if (store == null || effects == null || repository == null || clock == null)
            throw new IllegalArgumentException("Transfer action dependencies are required");
        this.store = store; this.currentOwner = currentOwner; this.effects = effects;
        this.repository = repository; this.clock = clock;
    }

    /** w2.g: force current owner, stamp, allocate only a zero ID, save, then notify. */
    public Result save(OriginalTransferRecord record) { return persist(record, true, true); }

    /** w2.h: the same assignment/save stages without the local n0 notification. */
    public Result saveSilently(OriginalTransferRecord record) { return persist(record, true, false); }

    /** w2.q0: stamp and save the existing raw identity/owner, then notify. */
    public Result update(OriginalTransferRecord record) { return persist(record, false, true); }

    private Result persist(OriginalTransferRecord record, boolean assignIdentity, boolean notify) {
        if (record == null) throw new IllegalArgumentException("Transfer is required");
        synchronized (store) {
            // A surrounding transaction would hide save rollback/commit from the
            // following effect and change these independently saved source stages.
            if (store.getWritableDatabase().inTransaction())
                throw new IllegalStateException("Original Transfer stages require independent saves");
            if (assignIdentity) record.userId = currentOwner;
            record.updateTime = clock.getAsLong();
            if (assignIdentity && record.transferId == 0)
                record.transferId = repository.allocateSemanticId(currentOwner);
            OriginalTransferRepository.SaveResult saved = repository.trySaveOriginalRaw(record);
            // SaveResult catches Exception only. An Error bypasses this tail;
            // neither false nor a missing persisted row suppresses notification.
            if (notify) effects.transferChanged(record, saved.saved);
            return new Result(record, saved);
        }
    }
}
