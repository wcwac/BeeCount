package com.wangc.bill.reference;

/** The recovered Lend's 15 fields, with its independent LitePal PK and inherited delete flag. */
public final class OriginalLendRecord {
    public static final int LEND = 1, LEND_COLLECTION = 2, BORROW = 3, BORROW_REPAYMENT = 4;
    public long localRowId;
    public boolean originalDeleted;
    public long assetId;
    public int billId;
    public long createTime;
    public double fromCost;
    public boolean generalServiceBill;
    public long inTime;
    public double interest;
    public int lendId;
    public double number;
    public long outTime;
    public String remark;
    public long repaymentAssetId;
    public int type;
    public long updateTime;
    public int userId;

    /** Raw source selection only. Credit consumers separately call getFromCost. */
    public double getCurrencyCost(long queriedAssetId) {
        return fromCost != 0.0d && queriedAssetId == repaymentAssetId ? fromCost : number;
    }
    public double getFromCost() { return fromCost; }
    public long getTime() { return outTime; }

    public OriginalLendRecord copy() {
        OriginalLendRecord copy = new OriginalLendRecord();
        copy.localRowId = localRowId; copy.originalDeleted = originalDeleted;
        copy.assetId = assetId; copy.billId = billId; copy.createTime = createTime;
        copy.fromCost = fromCost; copy.generalServiceBill = generalServiceBill;
        copy.inTime = inTime; copy.interest = interest; copy.lendId = lendId;
        copy.number = number; copy.outTime = outTime; copy.remark = remark;
        copy.repaymentAssetId = repaymentAssetId; copy.type = type;
        copy.updateTime = updateTime; copy.userId = userId; return copy;
    }
}
