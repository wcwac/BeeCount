// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 07d2a0fb9b4c8a653beac688cd9b5166c31cac093e1c4f561670cd0203bb5ea0
package com.wangc.bill.auto.action;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import com.blankj.utilcode.util.t0;
import com.blankj.utilcode.util.y;
import com.google.android.accessibility.selecttospeak.SelectToSpeakService;
import com.wangc.bill.auto.x1;
import com.wangc.bill.http.entity.BillInfo;
import com.wangc.bill.utils.h2;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class j extends c implements x1.a {

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private static final int f13720g = 1;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private static final int f13721h = 2;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private static final int f13722i = 3;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private static j f13723j;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private boolean f13724e = false;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13725f;

    private void u(Context context, List<String> list, String str) {
        BillInfo billInfoT;
        y.l("start find node :" + str);
        if (list == null || list.size() <= 0) {
            return;
        }
        if (this.a == 1 && list.contains("支付成功")) {
            billInfoT = r(list);
        } else {
            int i6 = this.a;
            if (i6 == 2) {
                billInfoT = s(list);
            } else {
                billInfoT = i6 == 3 ? t(list) : null;
            }
        }
        if (billInfoT != null) {
            this.b = false;
            this.f13724e = false;
            billInfoT.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
            SelectToSpeakService.l(context, billInfoT, this);
        }
    }

    public static j v() {
        if (f13723j == null) {
            f13723j = new j();
        }
        return f13723j;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
    }

    public void q(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        List<String> listM;
        if (str.equals("com.ccb.longjiLife.MainActivity") || str.equals("com.ccb.cloudmerchant.wallet.newOne.transactions.TransactionsActivity2") || str.equals("com.ccb.cloudmerchant.wallet.newOne.WalletActivity")) {
            this.a = 0;
            this.b = false;
            this.f13724e = false;
        } else if (str.equals("com.ccb.cloudmerchant.wallet.newOne.view.CodeActivity") || str.equals("com.ccb.cloudmerchant.view.WebViewActivity")) {
            this.f13724e = true;
        } else if (str.equals("com.ccb.cloudmerchant.wallet.newOne.view.CodeResultActivity")) {
            this.b = true;
            this.a = 1;
        } else if (str.equals("com.ccb.cloudmerchant.wallet.transactions.TransactionsDetailActivity")) {
            this.b = true;
            this.f13724e = true;
            this.a = 2;
        }
        if (!this.f13724e || accessibilityNodeInfo == null) {
            listM = null;
        } else {
            listM = m(accessibilityNodeInfo);
            int iD = d(listM, "优先选择此方式支付", true);
            if (iD > 0) {
                this.f13725f = listM.get(iD - 1);
                if (iD > 1) {
                    int i6 = iD - 2;
                    if (listM.get(i6).contains("数字人民币")) {
                        this.f13725f = listM.get(i6);
                    }
                }
            }
            if (a(listM, "支付成功", true) && a(listM, "订单号", true)) {
                this.b = true;
                this.a = 3;
            }
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        u(context, listM, str);
    }

    public BillInfo r(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("建行生活");
        billInfo.setAsset(this.f13725f);
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.contains("¥") && TextUtils.isEmpty(billInfo.getNumber()) && i6 < list.size() - 1) {
                String strReplace = list.get(i6 + 1).replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                }
            } else if (str.equals("商户名称") && i6 < list.size() - 1) {
                String str2 = list.get(i6 + 1);
                billInfo.setRemark(str2);
                billInfo.setShopName(str2);
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo s(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("建行生活");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.equals("交易详情") && i6 < list.size() - 1) {
                String strReplace = list.get(i6 + 1).replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace.replace("-", "").replace("+", ""));
                    billInfo.setIncome(!strReplace.contains("-"));
                }
            } else if (str.equals("商户名") && i6 < list.size() - 1) {
                String str2 = list.get(i6 + 1);
                billInfo.setRemark(str2);
                billInfo.setShopName(str2);
            } else if (str.equals("支付方式") && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1));
            } else if (str.equals("交易时间") && i6 < list.size() - 1) {
                billInfo.setTime(t0.X0(list.get(i6 + 1), "yyyy/MM/dd HH:mm:ss"));
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo t(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("建行生活");
        billInfo.setAsset("建设银行");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.contains("¥")) {
                String strReplace = str.replace(" ", "").replace("¥", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                }
            } else if (str.contains("商品名称")) {
                String strReplace2 = str.replace("商品名称", "");
                billInfo.setRemark(strReplace2);
                billInfo.setShopName(strReplace2);
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }
}
