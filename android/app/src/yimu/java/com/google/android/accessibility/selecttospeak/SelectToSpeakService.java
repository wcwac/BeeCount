package com.google.android.accessibility.selecttospeak;
import android.content.Context;
import com.wangc.bill.http.entity.BillInfo;
import com.wangc.bill.auto.x1;
import com.wangc.bill.reference.capture.YimuKernel;
/** Replaces only the original handlers' submission boundary, not Google's service. */
public final class SelectToSpeakService {
 public static volatile long f1838j=0;
 public static void l(Context context,BillInfo bill,x1.a callback){YimuKernel.submit(bill,callback);}
 public static void E(Context context,String remark){YimuKernel.enrichRemark(remark);}
}
