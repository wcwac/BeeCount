package com.wangc.bill.reference;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Recovered Transfer's 12 own fields; semantic IDs never substitute for the local PK. */
public final class OriginalTransferRecord {
    public long localRowId;
    /** Inherited BaseLitePal.delete value; physical deletion is a separate operation. */
    public boolean originalDeleted;
    public int billId;
    public double cost;
    public long fromAssetId;
    public String remark;
    public double serviceCharge;
    /** Raw storage can contain duplicates; only an explicit setter normalizes them. */
    public List<Long> tags = new ArrayList<>();
    public long time;
    public long toAssetId;
    public double toCost;
    public long transferId;
    public long updateTime;
    public int userId;

    /** Original field selection only: no absolute value, FX, rounding or finite-value guard. */
    public double getCurrencyCost(long queriedAssetId) {
        return toCost == 0.0d || queriedAssetId == fromAssetId ? cost : toCost;
    }

    /** Like the original getter, returns the actual mutable list, including null. */
    public List<Long> getTags() { return tags; }

    /** Original setTags keeps each first occurrence in order; null remains null. */
    public void setTags(List<Long> values) {
        tags = values == null ? null : new ArrayList<>(new LinkedHashSet<>(values));
    }

    public void addTag(Long value) {
        if (tags == null || !tags.contains(value)) {
            if (tags == null) tags = new ArrayList<>();
            tags.add(value);
        }
    }

    /** Raw copy deliberately bypasses setTags so loaded duplicates and null survive. */
    public OriginalTransferRecord copy() {
        OriginalTransferRecord result = new OriginalTransferRecord();
        result.localRowId = localRowId; result.originalDeleted = originalDeleted;
        result.billId = billId; result.cost = cost; result.fromAssetId = fromAssetId;
        result.remark = remark; result.serviceCharge = serviceCharge;
        result.tags = tags == null ? null : new ArrayList<>(tags);
        result.time = time; result.toAssetId = toAssetId; result.toCost = toCost;
        result.transferId = transferId; result.updateTime = updateTime; result.userId = userId;
        return result;
    }
}
