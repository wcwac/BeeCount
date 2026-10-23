package com.wangc.bill.reference;

/** ReimbursementDetail.c0's colon view; raw asset identity survives a missing UI account. */
public final class OriginalReimbursementEntry {
    public final String rawInfo;
    public final long assetSemanticId, time;
    public final double amount, addNumber;
    public final String remark;

    private OriginalReimbursementEntry(String raw,long asset,double amount,long time,String remark,double add) {
        rawInfo=raw;assetSemanticId=asset;this.amount=amount;this.time=time;this.remark=remark;addNumber=add;
    }

    /**
     * Java split removes trailing empty fields, retains interior empty remarks, and the
     * original detail ignores fields after index4. This strict finite numeric view is
     * intentionally narrower than h2.S's error coercion. Unsupported raw text is still
     * stored verbatim by the repository. generalInfo's edit writer remains unported.
     */
    public static OriginalReimbursementEntry parseStrict(String raw) {
        if (raw==null) throw new IllegalArgumentException("报销记录不能为空");
        String[] parts=raw.split(":");
        if(parts.length<2) throw new IllegalArgumentException("报销记录字段不足");
        try {
            long asset=Long.parseLong(parts[0]),time=parts.length>=3?Long.parseLong(parts[2]):0;
            double amount=Double.parseDouble(parts[1]),add=parts.length>=5?Double.parseDouble(parts[4]):0;
            if(!Double.isFinite(amount)||!Double.isFinite(add))throw new NumberFormatException();
            return new OriginalReimbursementEntry(raw,asset,amount,time,parts.length>=4?parts[3]:null,add);
        }catch(NumberFormatException error){throw new IllegalArgumentException("报销记录数字格式无效",error);}
    }

    public double aggregateReduction() { return addNumber!=0.0d?addNumber:amount; }
}
