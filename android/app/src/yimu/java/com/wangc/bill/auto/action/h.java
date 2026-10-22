// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 2ddaf582378699f79fc7bb0a61ed2ccf32777ee3a8b9fcd2d35b3d07e139d902
package com.wangc.bill.auto.action;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import com.blankj.utilcode.util.y;
import com.google.android.accessibility.selecttospeak.SelectToSpeakService;
import com.wangc.bill.auto.x1;
import com.wangc.bill.http.entity.BillInfo;
import com.wangc.bill.utils.h2;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class h extends c implements x1.a {

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private static final int f13704h = 1;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private static h f13705i;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private String f13706e;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13707f;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private boolean f13708g = false;

    private void q(Context context, List<String> list, String str) {
        y.l("start find node :" + str);
        if (list == null || list.size() <= 0 || !a(list, "支付成功", false) || !a(list, "返回首页", false)) {
            return;
        }
        BillInfo billInfoR = this.a == 1 ? r(this.f13706e, this.f13707f) : null;
        if (billInfoR != null) {
            this.b = false;
            this.f13706e = null;
            this.f13707f = null;
            this.f13708g = false;
            billInfoR.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
            SelectToSpeakService.l(context, billInfoR, this);
        }
    }

    public static h t() {
        if (f13705i == null) {
            f13705i = new h();
        }
        return f13705i;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
        this.b = false;
        this.f13706e = null;
        this.f13707f = null;
    }

    public BillInfo r(String str, String str2) {
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("闲鱼");
        billInfo.setRemark("购物");
        billInfo.setShopName("闲鱼");
        billInfo.setAsset("支付宝");
        if (!TextUtils.isEmpty(str)) {
            billInfo.setRemark(str);
        }
        if (!TextUtils.isEmpty(str2) && h2.J(str2)) {
            billInfo.setNumber(str2);
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public void s(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        List<String> listM;
        if (str.equals("com.taobao.idlefish.maincontainer.activity.MainActivity") || str.equals("com.taobao.idlefish.search_implement.SearchResultActivity")) {
            this.a = 0;
            this.b = false;
            this.f13708g = false;
        } else if (str.equals("com.idlefish.flutterbridge.flutterboost.boost.FishFlutterBoostActivity")) {
            this.f13708g = true;
        }
        if (!this.f13708g || accessibilityNodeInfo == null) {
            listM = null;
        } else {
            listM = m(accessibilityNodeInfo);
            if (d(listM, "现价", false) > 0) {
                int iD = d(listM, "现价", false);
                if (iD > 0) {
                    String str2 = listM.get(iD);
                    this.f13707f = str2;
                    String strReplace = str2.substring(str2.indexOf("现价") + 2).replace(" ", "");
                    this.f13707f = strReplace;
                    if (strReplace.contains(",")) {
                        String str3 = this.f13707f;
                        this.f13707f = str3.substring(0, str3.indexOf(","));
                    }
                    if (iD < listM.size() - 1) {
                        String str4 = listM.get(iD + 1);
                        this.f13706e = str4;
                        if (!TextUtils.isEmpty(str4)) {
                            this.f13706e = this.f13706e.split("\n")[0];
                        }
                    }
                }
                y.l("sssss", "remark:" + this.f13706e, "num:" + this.f13707f);
            } else if (a(listM, "支付成功", false)) {
                this.b = true;
                this.a = 1;
                y.l("sssss", "can add");
            }
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        q(context, listM, str);
    }
}
