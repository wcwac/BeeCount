package com.wangc.bill.reference;

/** BillRelated's five primitive fields, inherited delete flag and independent local PK. */
public final class OriginalBillRelatedRecord {
    public long localRowId;
    public int oneBillId;
    public long relatedId;
    public int twoBillId;
    public long updateTime;
    public int userId;
    public boolean originalDeleted;
}
