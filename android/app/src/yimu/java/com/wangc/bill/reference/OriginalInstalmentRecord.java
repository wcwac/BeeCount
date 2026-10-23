package com.wangc.bill.reference;

/** The recovered Instalment's 14 own fields and its independent local database identity. */
public final class OriginalInstalmentRecord {
    public long localRowId;
    /** Inherited BaseLitePal.delete flag; physical deletion is a separate operation. */
    public boolean originalDeleted;
    public String accountMonth;
    public long assetId;
    public int billId;
    public long inAssetTime;
    public long instalmentId;
    public int instalmentType;
    public int periods;
    public int remainderType;
    /** Exact nullable JSON text. This storage port neither parses nor regenerates it. */
    public String selfInfo;
    public double serviceNumber;
    public int serviceType;
    public double totalNumber;
    public long updateTime;
    public int userId;

    public OriginalInstalmentRecord copy() {
        OriginalInstalmentRecord r = new OriginalInstalmentRecord();
        r.localRowId = localRowId; r.originalDeleted = originalDeleted;
        r.accountMonth = accountMonth; r.assetId = assetId; r.billId = billId;
        r.inAssetTime = inAssetTime; r.instalmentId = instalmentId; r.instalmentType = instalmentType;
        r.periods = periods; r.remainderType = remainderType; r.selfInfo = selfInfo;
        r.serviceNumber = serviceNumber; r.serviceType = serviceType; r.totalNumber = totalNumber;
        r.updateTime = updateTime; r.userId = userId;
        return r;
    }
}
