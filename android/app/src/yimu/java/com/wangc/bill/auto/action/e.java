// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 8ee84c4b329522c3f14052ace75e7d02d255c75d761a69e083b44c71d89a5ff2
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
public class e extends c implements x1.a {

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private static final int f13687i = 1;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private static e f13688j;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private String f13689e;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13690f;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private String f13691g;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private boolean f13692h = false;

    private void q(Context context, List<String> list, String str) {
        y.l("start find node :" + str);
        if (list == null || list.isEmpty()) {
            return;
        }
        if (a(list, "支付成功", false) || a(list, "购买成功", false)) {
            BillInfo billInfoR = this.a == 1 ? r(this.f13689e, this.f13690f, this.f13691g) : null;
            if (billInfoR != null) {
                this.b = false;
                this.f13689e = null;
                this.f13691g = null;
                this.f13690f = null;
                this.f13692h = false;
                billInfoR.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                SelectToSpeakService.l(context, billInfoR, this);
            }
        }
    }

    public static e t() {
        if (f13688j == null) {
            f13688j = new e();
        }
        return f13688j;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
        this.b = false;
        this.f13689e = null;
        this.f13690f = null;
    }

    public BillInfo r(String str, String str2, String str3) {
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("快手");
        billInfo.setRemark("快手");
        billInfo.setShopName("快手");
        if (!TextUtils.isEmpty(str3)) {
            billInfo.setAsset(str3);
        }
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
        List<String> listM = null;
        if (str.equals("com.yxcorp.gifshow.HomeActivity") || str.equals("com.kuaishou.merchant.pagedy.page.MerchantEraActivity") || str.equals("com.kuaishou.merchant.container.base.MerchantKwaiDialog") || str.equals("com.yxcorp.plugin.search.SearchActivity") || str.equals("com.yxcorp.gifshow.detail.PhotoDetailActivity") || str.equals("com.kuaishou.live.core.basic.activity.LivePlayActivity") || str.equals("com.kuaishou.merchant.transaction.detail.contentdetail.MerchantContentDetailActivity")) {
            this.a = 0;
            this.b = false;
            this.f13692h = false;
            this.f13689e = null;
            this.f13690f = null;
            this.f13691g = null;
        } else if (str.equals("com.kuaishou.merchant.transaction.live.dynamic.page.MerchantPurchasePanelContainerV2Activity") || str.equals("com.kwai.kds.krn.api.page.KwaiRnActivity") || str.equals("com.kuaishou.merchant.basic.widget.MerchantInterceptOutsideTouchDialog")) {
            this.f13692h = true;
        }
        if (this.f13692h && accessibilityNodeInfo != null) {
            listM = m(accessibilityNodeInfo);
            for (int size = listM.size() - 1; size >= 0; size--) {
                String str2 = listM.get(size);
                String strReplace = "";
                if (str2.startsWith("立即支付")) {
                    strReplace = str2.replace("立即支付", "").replace("¥", "").replace(" ", "").replace(" ", "").replace(",", "");
                } else if (str2.startsWith("0元下单")) {
                    strReplace = str2.replace("0元下单, 确认收货后付款", "").replace("¥", "").replace(" ", "").replace(" ", "").replace(",", "");
                } else if (str2.startsWith("免密支付")) {
                    strReplace = str2.replace("免密支付", "").replace("¥", "").replace(" ", "").replace(" ", "").replace(",", "");
                } else if (str2.startsWith("实付")) {
                    strReplace = str2.replace("实付", "").replace("¥", "").replace(" ", "").replace(" ", "").replace(",", "");
                }
                if (h2.J(strReplace)) {
                    this.f13690f = strReplace;
                }
                if (TextUtils.isEmpty(this.f13689e) && (("关注".equals(str2) || "进店".equals(str2)) && size < listM.size() - 1)) {
                    this.f13689e = listM.get(size + 1);
                }
                if (TextUtils.isEmpty(this.f13689e) && str2.endsWith("门店通用") && size < listM.size() - 1) {
                    this.f13689e = listM.get(size + 1);
                }
            }
            y.l("sssss", "remark:" + this.f13689e, "num:" + this.f13690f);
            if (!this.b && a(listM, "支付成功", true)) {
                this.b = true;
                this.a = 1;
                int iD = d(listM, "¥", true);
                if (iD >= 0 && iD < listM.size() - 2) {
                    StringBuilder sb = new StringBuilder();
                    int i6 = iD + 1;
                    sb.append(listM.get(i6));
                    sb.append(listM.get(iD + 2));
                    String string = sb.toString();
                    this.f13690f = string;
                    if (!h2.J(string)) {
                        this.f13690f = listM.get(i6);
                    }
                    String str3 = listM.get(d(listM, "付款", false));
                    this.f13691g = str3;
                    this.f13691g = str3.substring(0, str3.indexOf("付款"));
                    y.l("check asset : " + this.f13691g + "   num:" + this.f13690f);
                }
                y.l("nodeInfo tiktok can add : " + this.b);
            } else if (!this.b && listM.contains("购买成功") && listM.contains("完成")) {
                this.b = true;
                this.a = 1;
            }
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        q(context, listM, str);
    }
}
