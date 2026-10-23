package com.wangc.bill.reference;

import java.math.BigDecimal;
import org.json.JSONException;
import org.json.JSONObject;

/** Safe data codec. Does not reproduce or load Gson adapters or serialize imported raw JSON. */
public final class OriginalRefundCodec {
    private OriginalRefundCodec() {}

    /** Unknown keys and original spelling remain available in rawJson, not in the typed view. */
    public static OriginalRefundInfo decodeStrict(String raw) {
        JSONObject json=Json.parse(raw);OriginalRefundInfo result=new OriginalRefundInfo();
        result.rawJson=raw;
        result.assetId=integer(json,"assetId");result.inTime=integer(json,"inTime");
        result.relatedBillId=integer(json,"relatedBillId");result.time=integer(json,"time");
        result.assetNum=number(json,"assetNum");result.costNum=number(json,"costNum");
        result.currentNumber=number(json,"currentNumber");result.number=number(json,"number");
        result.restoreCostNum=number(json,"restoreCostNum");result.restoreNum=number(json,"restoreNum");
        if(!json.isNull("remark")) {
            Object remark=json.opt("remark");if(!(remark instanceof String))invalid();result.remark=(String)remark;
        }
        result.validateFinite();return result;
    }

    /**
     * Encode only an explicitly new DTO. Imported JSON must retain its exact raw string;
     * this writer's key order/null policy is our format, not a claim of Gson equivalence.
     */
    public static String encodeNew(OriginalRefundInfo value) {
        if(value==null)throw new IllegalArgumentException("退款记录不能为空");
        if(value.rawJson!=null)throw new IllegalArgumentException("已有退款原文必须原样保留");
        value.validateFinite();
        return Json.object("assetId",value.assetId,"assetNum",value.assetNum,"costNum",value.costNum,
            "currentNumber",value.currentNumber,"inTime",value.inTime,"number",value.number,
            "relatedBillId",value.relatedBillId,"remark",value.remark,"restoreCostNum",value.restoreCostNum,
            "restoreNum",value.restoreNum,"time",value.time).toString();
    }

    /**
     * a2.I's legacy fallback is only for explicit detail/delete migration callers.
     * Keep its guard and sequential partial mutation when a later alias is missing.
     * Ordinary decoding, amount getters and repository storage never call it.
     */
    public static boolean applyLegacyAliasesForDetail(OriginalRefundInfo value,String raw) {
        if(value==null)throw new IllegalArgumentException("退款记录不能为空");
        if(value.time!=0||value.assetId!=0)return false;
        try {
            JSONObject json=new JSONObject(raw);
            value.assetId=json.getInt("a");value.number=json.getDouble("b");
            value.currentNumber=json.getDouble("c");value.remark=json.getString("d");
            value.time=json.getLong("e");return true;
        }catch(JSONException error){return false;}
    }

    private static long integer(JSONObject json,String key) {
        if(json.isNull(key))return 0;
        Object value=json.opt(key);
        if(!(value instanceof Number))invalid();
        try {return new BigDecimal(value.toString()).longValueExact();}
        catch(ArithmeticException|NumberFormatException error){throw new IllegalArgumentException("退款标识或时间不是64位整数",error);}
    }
    private static double number(JSONObject json,String key) {
        if(json.isNull(key))return 0;
        Object value=json.opt(key);if(!(value instanceof Number))invalid();
        double number=((Number)value).doubleValue();if(!Double.isFinite(number))invalid();return number;
    }
    private static void invalid(){throw new IllegalArgumentException("退款字段类型无效");}
}
