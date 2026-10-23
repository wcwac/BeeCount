package com.wangc.bill.reference;

import java.util.ArrayList;
import java.util.List;

/**
 * The 26 instance fields of the recovered Bill, plus the independent local PK.
 * No experimental amount, currency, account FK or category FK supplies these values.
 * Public fields preserve raw data; currencyInfo formatting belongs to its entry caller.
 */
public final class OriginalBillRecord {
    public long localRowId;
    public long assetId;
    public int billId;
    public String billShareInfo;
    public int billType;
    public long bookId;
    public int childCategoryId;
    public double cost;
    public double currencyAssetNumber;
    public double currencyDiscountNumber;
    public String currencyInfo;
    public double discountNumber;
    public int fromUserId;
    public long inAssetTime;
    public boolean notIntoBudget;
    public boolean notIntoTotal;
    public int parentCategoryId;
    public String poiAddress;
    public long recordTime;
    public boolean reimbursement;
    public boolean reimbursementEnd;
    public String remark;
    public List<Long> tags = new ArrayList<>();
    public long time;
    public String totalAddress;
    public long updateTime;
    public int userId;

    /** Bill.getCurrencyCost: field selection only, without parsing, FX or rounding. */
    public double getCurrencyCost() {
        return currencyAssetNumber == 0.0d || currencyInfo == null || currencyInfo.isEmpty()
                ? cost : currencyAssetNumber;
    }

    /** Matches Bill.equals's semantic-ID comparison, not a database identity check. */
    @Override public boolean equals(Object other) {
        return this == other || other instanceof OriginalBillRecord
                && billId == ((OriginalBillRecord) other).billId;
    }

    @Override public int hashCode() { return Integer.hashCode(billId); }
}
