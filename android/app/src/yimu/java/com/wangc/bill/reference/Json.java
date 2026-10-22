package com.wangc.bill.reference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class Json {
    private Json() {}
    public static JSONObject object(Object... pairs) {
        JSONObject result = new JSONObject();
        try {
            for (int i = 0; i < pairs.length; i += 2) result.put((String) pairs[i], pairs[i + 1] == null ? JSONObject.NULL : pairs[i + 1]);
            return result;
        } catch (JSONException error) { throw new IllegalArgumentException(error); }
    }
    public static JSONObject parse(String raw) {
        try { return new JSONObject(raw); } catch (JSONException error) { throw new IllegalArgumentException("数据格式无效", error); }
    }
    public static JSONObject copy(JSONObject value) { return parse(value.toString()); }
    public static void put(JSONObject object, String key, Object value) {
        try { object.put(key, value == null ? JSONObject.NULL : value); }
        catch (JSONException error) { throw new IllegalArgumentException(error); }
    }
    public static JSONArray array(String raw) {
        try { return new JSONArray(raw); } catch (JSONException error) { throw new IllegalArgumentException("列表格式无效", error); }
    }
}
