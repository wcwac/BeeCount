// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 62986e41eec2777e302e2501373ebcc74a45b2a4dfd6fd7280ab723b3efcf728
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
public class f extends c implements x1.a {

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private static final int f13693g = 1;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private static final int f13694h = 2;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private static final int f13695i = 3;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private static final int f13696j = 4;

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    private static f f13697k;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private boolean f13698e = false;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13699f;

    private void v(Context context, List<String> list, String str) {
        y.l("start find node :" + str);
        if (list == null || list.size() <= 0) {
            return;
        }
        BillInfo billInfoR = this.a == 1 ? r(list) : null;
        int i6 = this.a;
        if (i6 == 3) {
            billInfoR = u(list);
        } else if (i6 == 2) {
            billInfoR = s(list);
        } else if (i6 == 4) {
            billInfoR = t(list);
        }
        if (billInfoR != null) {
            this.b = false;
            this.f13698e = false;
            billInfoR.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
            SelectToSpeakService.l(context, billInfoR, this);
        }
    }

    public static f w() {
        if (f13697k == null) {
            f13697k = new f();
        }
        return f13697k;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:54:0x00f1  */
    public void q(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        List<String> listM;
        int iIndexOf;
        str.hashCode();
        switch (str) {
            case "com.huawei.nfc.carrera.ui.swipe.SwipeResultActivity":
                this.a = 1;
                this.f13698e = true;
                this.b = true;
                listM = null;
                break;
            case "com.huawei.wallet.nfc.bankcard.transactionhistory.ui.activity.UnCardTransRecordFilterActivity":
            case "com.huawei.wallet.view.MainActivity":
                this.a = 0;
                this.b = false;
                this.f13698e = false;
                listM = null;
                break;
            case "com.huawei.wallet.nfc.bankcard.transactionhistory.ui.activity.UnCardTransRecordDetailActivity":
                this.a = 2;
                this.f13698e = true;
                this.b = true;
                listM = null;
                break;
            case "com.huawei.petalpay.userapp.function.billdetails.activity.BillDetailInfoActivity":
                this.a = 4;
                this.f13698e = true;
                this.b = true;
                listM = null;
                break;
            case "com.huawei.wallet.bankcard.scanpay.ui.activity.ScanQrCodePayResultActivity":
                this.a = 3;
                this.f13698e = true;
                this.b = true;
                listM = null;
                break;
            case "com.huawei.wallet.nfc.bankcard.paymentcode.ui.PaymentCodeActivity":
            case "com.huawei.nfc.carrera.ui.swipe.SwipeActivity":
                if (accessibilityNodeInfo != null) {
                    listM = m(accessibilityNodeInfo);
                    if (listM != null && !listM.isEmpty() && (iIndexOf = listM.indexOf("二维码付款码")) >= 0 && iIndexOf < listM.size() - 1) {
                        this.f13699f = listM.get(iIndexOf + 1).replace("（", "").replace("）", "").replace("尾号", "").replace(" ", "").trim();
                        y.l("sssss", "asset:" + this.f13699f);
                    }
                    break;
                } else {
                    listM = null;
                    break;
                }
                break;
            default:
                listM = null;
                break;
        }
        if (this.f13698e && accessibilityNodeInfo != null) {
            listM = m(accessibilityNodeInfo);
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        v(context, listM, str);
    }

    public BillInfo r(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("华为支付");
        int iD = d(list, "¥", true);
        if (iD > 0 && iD < list.size() - 1) {
            String str = list.get(iD + 1);
            if (h2.J(str)) {
                billInfo.setNumber(str);
                billInfo.setRemark(list.get(iD - 1));
                if (iD > 2) {
                    billInfo.setAsset(list.get(iD - 3) + list.get(iD - 2));
                } else {
                    billInfo.setAsset(this.f13699f);
                }
                return billInfo;
            }
        }
        return null;
    }

    public BillInfo s(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("华为支付");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.contains("¥") && TextUtils.isEmpty(billInfo.getNumber()) && i6 < list.size() - 3) {
                String strReplace = list.get(i6 + 1).replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                    billInfo.setIncome(!str.contains("-"));
                    billInfo.setAsset(list.get(i6 + 3));
                }
            } else if (str.equals("商家") && i6 < list.size() - 1) {
                String str2 = list.get(i6 + 1);
                billInfo.setRemark(str2);
                billInfo.setShopName(str2);
            } else if (str.equals("优惠金额") && i6 < list.size() - 1) {
                String strReplace2 = list.get(i6 + 1).replace("¥", "");
                if (h2.J(strReplace2)) {
                    billInfo.setDiscount(strReplace2);
                }
            } else if (str.equals("日期") && i6 < list.size() - 1) {
                billInfo.setTime(t0.X0(list.get(i6 + 1), "yyyy-MM-dd HH:mm:ss"));
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo t(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("华为支付");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.equals("交易成功") && i6 > 0 && i6 < list.size() - 1) {
                String str2 = list.get(i6 - 1);
                String strReplace = str2.replace(",", "").replace("+", "").replace("-", "").replace(" ", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                    billInfo.setIncome(!str2.contains("-"));
                    billInfo.setRemark(list.get(i6 + 1));
                    billInfo.setShopName(billInfo.getRemark());
                }
            } else if (str.equals("付款银行") && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1).replace(" ", "").replace("(", "").replace(")", ""));
            } else if (str.equals("商品名称") && i6 < list.size() - 1) {
                billInfo.setRemark(list.get(i6 + 1));
            } else if (str.equals("交易时间") && i6 < list.size() - 1) {
                billInfo.setTime(t0.X0(list.get(i6 + 1), "yyyy-MM-dd HH:mm:ss"));
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo u(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("华为支付");
        int iD = d(list, "¥", true);
        if (iD > 2 && iD < list.size() - 2) {
            String str = list.get(iD + 1);
            if (h2.J(str)) {
                billInfo.setNumber(str);
                billInfo.setRemark(list.get(iD + 2));
                int iD2 = d(list, "付款方式", true);
                if (iD2 > 0 && iD2 < list.size() - 1) {
                    billInfo.setAsset(list.get(iD2 + 1));
                }
                return billInfo;
            }
        }
        return null;
    }
}
