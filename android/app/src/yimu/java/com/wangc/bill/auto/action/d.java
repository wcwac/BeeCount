// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 f168c0f60d456e43ef70a35510e644e425e5e217f76abc0ba1e65b2e6042e652
package com.wangc.bill.auto.action;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import com.blankj.utilcode.util.y;
import com.google.android.accessibility.selecttospeak.SelectToSpeakService;
import com.wangc.bill.auto.x1;
import com.wangc.bill.http.entity.BillInfo;
import com.wangc.bill.utils.a2;
import com.wangc.bill.utils.h2;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class d extends c implements x1.a {

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private static d f13682i;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private String f13683e;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13684f;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private String f13685g;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private boolean f13686h = false;

    private void s(Context context, List<String> list) {
        a2.a("start find node");
        if (list == null || list.size() <= 0) {
            return;
        }
        a2.a("start check pay:" + this.a);
        BillInfo billInfoR = r(this.f13683e, this.f13684f, this.f13685g);
        if (billInfoR != null) {
            this.b = false;
            this.f13683e = null;
            this.f13684f = null;
            this.f13685g = null;
            billInfoR.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
            SelectToSpeakService.l(context, billInfoR, this);
        }
    }

    public static d t() {
        if (f13682i == null) {
            f13682i = new d();
        }
        return f13682i;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
        this.b = false;
        this.f13683e = null;
        this.f13684f = null;
        this.f13685g = null;
    }

    public void q(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        List<String> listG;
        if (str.equals("me.ele.application.ui.Launcher.LauncherActivity") || str.equals("me.ele.shopdetailv2.lmagex.ShopDetailV3Activity")) {
            this.a = 0;
            this.b = false;
            this.f13686h = false;
        } else if (str.equals("me.ele.newbooking.checkout.entrypoint.WMCheckoutActivity") || str.equals("me.ele.newretail.submit.RetailSubmitActivity") || str.equals("me.ele.shopdetailv2.ShopDetailV2Activity") || str.equals("me.ele.android.emagex.container.EMagexPopupActivity")) {
            this.f13686h = true;
        }
        if (!this.f13686h || accessibilityNodeInfo == null) {
            listG = null;
        } else {
            listG = g(accessibilityNodeInfo);
            if (listG.contains("点餐") && listG.contains("评价")) {
                this.f13684f = listG.get(0);
                y.l("sssss", "asset:" + this.f13683e, "remark:" + this.f13684f, "num:" + this.f13685g);
            } else if (listG.contains("提交订单")) {
                int iIndexOf = listG.indexOf("提交订单");
                if (iIndexOf > 0) {
                    String strReplace = listG.get(iIndexOf - 1).replace(",", "").replace("¥", "");
                    if (h2.J(strReplace)) {
                        this.f13685g = strReplace;
                    }
                }
                int iIndexOf2 = listG.indexOf("已选");
                if (iIndexOf2 != -1 && iIndexOf2 < listG.size() - 1) {
                    this.f13683e = listG.get(iIndexOf2 + 1);
                }
                y.l("sssss", "asset:" + this.f13683e, "remark:" + this.f13684f, "num:" + this.f13685g);
            } else if ((listG.contains("下单成功，等待商家接单") || listG.contains("下单成功，商家已接单") || listG.contains("下单成功，商家正在备餐") || listG.contains("下单成功，商家备餐中") || listG.contains("下单成功，商家备货完成")) && listG.contains("完成") && System.currentTimeMillis() - SelectToSpeakService.f1838j > 30000) {
                this.b = true;
            }
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        s(context, listG);
    }

    public BillInfo r(String str, String str2, String str3) {
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("饿了么");
        billInfo.setRemark("饿了么");
        billInfo.setShopName("饿了么");
        if (TextUtils.isEmpty(str)) {
            billInfo.setAsset("支付宝");
        } else {
            billInfo.setAsset(str);
        }
        if (!TextUtils.isEmpty(str2)) {
            billInfo.setRemark(str2);
        }
        if (!TextUtils.isEmpty(str3)) {
            billInfo.setNumber(str3);
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }
}
