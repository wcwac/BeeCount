package com.wangc.bill.reference;

import java.util.function.LongSupplier;
import org.json.JSONObject;

/** Local g1.D/d/E/k/g/m stages. Networking and CurdHistory retry records are separate ports. */
public final class OriginalInstalmentCommands {
    @FunctionalInterface public interface Effects { void creditRefresh(long assetSemanticId); }
    private final ReferenceStore store;
    private final int currentOwner, currentBookOwner;
    private final Effects effects;
    private final LongSupplier clock;
    private final OriginalInstalmentRepository repository;

    public OriginalInstalmentCommands(ReferenceStore store, int owner, int bookOwner, Effects effects) {
        this(store, owner, bookOwner, effects, System::currentTimeMillis);
    }
    public OriginalInstalmentCommands(ReferenceStore store, int owner, int bookOwner, Effects effects, LongSupplier clock) {
        if (store == null || effects == null || clock == null) throw new IllegalArgumentException("Instalment dependencies are required");
        this.store = store; currentOwner = owner; currentBookOwner = bookOwner; this.effects = effects; this.clock = clock;
        repository = new OriginalInstalmentRepository(store);
    }

    /** g1.D uses current owner + parent Bill semantic ID, not Bill.userId or a local FK. */
    public void adjustAfterRefund(OriginalBillRecord parent) {
        if (parent == null) throw new IllegalArgumentException("Parent Bill is required");
        synchronized (store) {
            independent();
            OriginalInstalmentRecord r = repository.firstByBill(currentOwner, parent.billId);
            if (r == null) return;
            if (parent.cost == 0.0d) delete(r);
            else if (r.totalNumber > Math.abs(parent.cost)) {
                r.totalNumber = Math.abs(parent.cost); saveUpdated(r);
            }
        }
    }
    /** g1.E: save false does not suppress its following local credit refresh. */
    public OriginalInstalmentRepository.SaveResult saveUpdated(OriginalInstalmentRecord record) {
        if (record == null) throw new IllegalArgumentException("Instalment is required");
        synchronized (store) {
            independent(); record.updateTime = clock.getAsLong();
            OriginalInstalmentRepository.SaveResult result = repository.trySaveOriginalRaw(record);
            refresh(record); return result;
        }
    }
    /** g1.d assigns current user/time and allocates only when the raw semantic ID is zero. */
    public OriginalInstalmentRepository.SaveResult saveAssigned(OriginalInstalmentRecord record) {
        if (record == null) throw new IllegalArgumentException("Instalment is required");
        synchronized (store) {
            independent(); record.userId = currentOwner; record.updateTime = clock.getAsLong();
            if (record.instalmentId == 0) record.instalmentId = repository.allocateSemanticId(currentOwner);
            OriginalInstalmentRepository.SaveResult result = repository.trySaveOriginalRaw(record);
            refresh(record); return result;
        }
    }
    /** g1.k: only a normal physical-delete return proceeds to m; delete errors propagate. */
    public int delete(OriginalInstalmentRecord record) {
        synchronized (store) {
            independent(); int deleted = repository.deleteRaw(record); refresh(record); return deleted;
        }
    }
    /** g1.g and m have the same local branch and no standalone-asset fallback for a missing Bill. */
    private void refresh(OriginalInstalmentRecord r) {
        JSONObject asset = null;
        if (r.billId != 0) {
            OriginalBillRecord bill = new OriginalBillRepository(store).findAccessible(currentOwner, currentBookOwner, r.billId);
            if (bill != null && bill.assetId != 0) asset = store.assetRepository().p(bill.assetId, currentOwner);
        } else if (r.assetId != 0) asset = store.assetRepository().p(r.assetId, currentOwner);
        if (asset != null && asset.optInt("asset_type") == 2) effects.creditRefresh(asset.optLong("semantic_id"));
    }
    private void independent() {
        if (store.getWritableDatabase().inTransaction()) throw new IllegalStateException("Original Instalment stages require independent saves");
    }
}
