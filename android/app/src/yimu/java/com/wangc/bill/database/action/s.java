package com.wangc.bill.database.action;
import com.wangc.bill.reference.ReferenceApplication;
/** Original s.i reads AutoSetting.isAutoAdd; it does not select the transaction timestamp. */
public final class s { public static boolean i(){return ReferenceApplication.prefs().getBoolean("auto_save",false);} }
