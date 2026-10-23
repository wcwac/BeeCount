package com.wangc.bill.reference;

/** Recovered RefundInfo fields, independent of Android, ORM and currency conversion. */
public final class OriginalRefundInfo {
    public long assetId, inTime, relatedBillId, time;
    public double assetNum, costNum, currentNumber, number, restoreCostNum, restoreNum;
    public String remark;
    /** Exact stored string for List.remove(Object); never a semantic item identifier. */
    public String rawJson;

    public double getCurrentRestoreNum() { return restoreNum == 0.0d ? restoreCostNum : restoreNum; }

    public OriginalRefundInfo copy() {
        OriginalRefundInfo value = new OriginalRefundInfo();
        value.assetId=assetId; value.inTime=inTime; value.relatedBillId=relatedBillId; value.time=time;
        value.assetNum=assetNum; value.costNum=costNum; value.currentNumber=currentNumber; value.number=number;
        value.restoreCostNum=restoreCostNum; value.restoreNum=restoreNum; value.remark=remark; value.rawJson=rawJson;
        return value;
    }

    /** Finite-only new commands are an explicit port policy, not a recovered Gson rule. */
    public void validateFinite() {
        if (!Double.isFinite(assetNum) || !Double.isFinite(costNum) || !Double.isFinite(currentNumber)
                || !Double.isFinite(number) || !Double.isFinite(restoreCostNum) || !Double.isFinite(restoreNum))
            throw new IllegalArgumentException("退款数值必须是有限数");
    }
}
