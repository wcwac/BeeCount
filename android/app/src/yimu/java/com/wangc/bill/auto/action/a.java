// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 43cee4cdbf61ecc40309257b713bfcfaa065567633cb0b3d5f53591c94e8b748
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
public class a extends c implements x1.a {

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private static final int f13666h = 1;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private static a f13667i;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private String f13668e;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13669f;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private boolean f13670g = false;

    private void s(Context context, List<String> list, String str) {
        y.l("start find node :" + str);
        if (list == null || list.size() <= 0) {
            return;
        }
        if (a(list, "支付成功", true) || a(list, "付款成功", true)) {
            BillInfo billInfoR = this.a == 1 ? r(this.f13668e, this.f13669f) : null;
            if (billInfoR != null) {
                this.b = false;
                this.f13668e = null;
                this.f13669f = null;
                this.f13670g = false;
                billInfoR.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                SelectToSpeakService.l(context, billInfoR, this);
            }
        }
    }

    public static a t() {
        if (f13667i == null) {
            f13667i = new a();
        }
        return f13667i;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
        this.b = false;
        this.f13668e = null;
        this.f13669f = null;
    }

    public void q(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        List<String> listM;
        if (str.equals("com.alibaba.wireless.launch.home.V5HomeActivity") || str.equals("com.alibaba.wireless.search.aksearch.resultpage.SearchResultActivity") || str.equals("com.alibaba.wireless.detail_flow.OfferDetailFlowActivity")) {
            this.a = 0;
            this.b = false;
            this.f13670g = false;
        } else if (str.equals("com.alibaba.wireless.container.windvane.AliWindvaneActivity") || str.equals("com.alibaba.wireless.popwindow.core.PopPageWindow")) {
            this.f13670g = true;
        }
        if (!this.f13670g || accessibilityNodeInfo == null) {
            listM = null;
        } else {
            listM = m(accessibilityNodeInfo);
            if (listM.contains("确认订单") || listM.contains("提交订单")) {
                int iD = d(listM, "有限公司", false);
                if (iD >= 0 && iD < listM.size() - 1) {
                    this.f13668e = listM.get(iD + 1);
                } else if (listM.size() > 7) {
                    this.f13668e = listM.get(7);
                }
                int iF = f(listM, "￥", true);
                if (iF < listM.size() - 2) {
                    String str2 = listM.get(iF + 1) + listM.get(iF + 2);
                    if (h2.J(str2)) {
                        this.f13669f = str2;
                    }
                }
                y.l("sssss", "remark:" + this.f13668e, "num:" + this.f13669f);
            } else if (listM.contains("立即支付") || listM.contains("0元下单，收货后付款")) {
                int iD2 = d(listM, "立即支付", true);
                if (iD2 == -1) {
                    iD2 = d(listM, "0元下单，收货后付款", true);
                }
                String str3 = listM.get(iD2 + 1);
                if (str3.contains("￥")) {
                    String strReplace = str3.replace("￥", "").replace(",", "");
                    if (h2.J(strReplace)) {
                        this.f13669f = strReplace;
                    }
                }
                y.l("sssss", "remark:" + this.f13668e, "num:" + this.f13669f);
            } else if ((listM.contains("支付成功") || listM.contains("付款成功")) && listM.contains("查看订单")) {
                this.b = true;
                this.a = 1;
                y.l("sssss", "can add");
            }
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        s(context, listM, str);
    }

    public BillInfo r(String str, String str2) {
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("阿里巴巴");
        billInfo.setRemark("阿里巴巴购物");
        billInfo.setShopName("阿里巴巴");
        billInfo.setAsset("支付宝");
        if (!TextUtils.isEmpty(str)) {
            billInfo.setRemark(str);
        }
        if (!TextUtils.isEmpty(str2)) {
            billInfo.setNumber(str2);
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }
}
