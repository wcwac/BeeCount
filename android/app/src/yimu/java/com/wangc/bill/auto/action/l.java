// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 7322871c7364ce26db50802d59c6e4639841c4f5672ae1039bdc4f4192c6b721
package com.wangc.bill.auto.action;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import com.blankj.utilcode.util.t0;
import com.blankj.utilcode.util.y;
import com.google.android.accessibility.selecttospeak.SelectToSpeakService;
import com.wangc.bill.auto.x1;
import com.wangc.bill.http.entity.BillInfo;
import com.wangc.bill.utils.a2;
import com.wangc.bill.utils.h2;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class l extends c implements x1.a {

    /* JADX INFO: renamed from: l, reason: collision with root package name */
    private static final int f13736l = 1;

    /* JADX INFO: renamed from: m, reason: collision with root package name */
    private static final int f13737m = 2;

    /* JADX INFO: renamed from: n, reason: collision with root package name */
    private static l f13738n;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private String f13739e;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13740f;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private String f13741g;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private boolean f13742h = false;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private boolean f13743i = false;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private boolean f13744j = false;

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    private boolean f13745k = false;

    private void q(Context context, List<String> list) {
        BillInfo billInfoT;
        a2.a("start find node");
        if (list == null || list.isEmpty()) {
            return;
        }
        int i6 = this.a;
        boolean zA = i6 == 2;
        if (i6 == 1) {
            zA = a(list, "拼单成功", false);
        }
        if (!zA && this.a == 1) {
            zA = a(list, "支付成功", true);
        }
        if (!zA) {
            zA = a(list, "已支付", false);
        }
        if (zA) {
            a2.a("start check pay:" + this.a);
            int i7 = this.a;
            if (i7 == 1) {
                billInfoT = r(list, this.f13739e, this.f13740f, this.f13741g);
            } else {
                billInfoT = i7 == 2 ? t(list) : null;
            }
            if (billInfoT != null) {
                this.b = false;
                this.f13742h = false;
                this.f13743i = false;
                this.f13744j = false;
                this.f13739e = null;
                this.f13740f = null;
                this.f13745k = false;
                billInfoT.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                SelectToSpeakService.l(context, billInfoT, this);
            }
        }
    }

    public static l u() {
        if (f13738n == null) {
            f13738n = new l();
        }
        return f13738n;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
        this.b = false;
        this.f13739e = null;
        this.f13740f = null;
    }

    public BillInfo r(List<String> list, String str, String str2, String str3) {
        int iIndexOf;
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("拼多多");
        billInfo.setRemark("拼多多");
        billInfo.setShopName("拼多多");
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
            if (a(list, "已支付，多多支付已减", true) && (iIndexOf = list.indexOf("已支付，多多支付已减")) >= 0 && iIndexOf < list.size() - 1) {
                String strReplace = list.get(iIndexOf + 1).replace("¥", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setDiscount(strReplace);
                    billInfo.setNumber((h2.S(str3) - h2.S(strReplace)) + "");
                }
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    /* JADX WARN: Code duplicated, block: B:101:0x022d  */
    /* JADX WARN: Code duplicated, block: B:103:0x0247  */
    /* JADX WARN: Instruction removed from duplicated block: B:103:0x0247, please report this as an issue */
    public void s(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        String strReplace;
        int i6;
        int i7 = 1;
        List<String> listG = null;
        if (str.equals("com.xunmeng.pinduoduo.ui.activity.HomeActivity")) {
            if (this.f13745k) {
                this.a = 0;
                this.b = false;
                this.f13739e = null;
                this.f13740f = null;
                this.f13742h = false;
                this.f13743i = false;
                this.f13744j = false;
            } else {
                this.f13745k = true;
            }
        } else if (str.equals("com.xunmeng.pinduoduo.ui.activity.MainFrameActivity")) {
            this.a = 0;
            this.b = false;
            this.f13739e = null;
            this.f13740f = null;
            this.f13742h = false;
            this.f13743i = false;
            this.f13744j = false;
        } else if (str.equals("com.xunmeng.pinduoduo.sku_checkout.a") || str.equals("e.s.y.h9.q0") || (accessibilityNodeInfo != null && v(accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.xunmeng.pinduoduo:id/tv_select_sku")))) {
            this.f13742h = true;
            this.f13745k = false;
        } else if (str.equals("com.xunmeng.pinduoduo.activity.NewPageActivity") && accessibilityNodeInfo != null) {
            this.f13743i = true;
        } else if (this.f13743i && str.equals("android.webkit.WebView")) {
            this.f13744j = true;
        } else if (this.f13744j && accessibilityNodeInfo != null && v(accessibilityNodeInfo.findAccessibilityNodeInfosByText("账单详情"))) {
            this.f13742h = true;
        } else if (this.f13743i && accessibilityNodeInfo != null && this.f13742h && ((v(accessibilityNodeInfo.findAccessibilityNodeInfosByText("商品")) && v(accessibilityNodeInfo.findAccessibilityNodeInfosByText("搜索")) && v(accessibilityNodeInfo.findAccessibilityNodeInfosByText("综合"))) || (v(accessibilityNodeInfo.findAccessibilityNodeInfosByText("我的订单")) && v(accessibilityNodeInfo.findAccessibilityNodeInfosByText("全部")) && v(accessibilityNodeInfo.findAccessibilityNodeInfosByText("待付款"))))) {
            this.f13742h = false;
        }
        if (this.f13742h && accessibilityNodeInfo != null) {
            listG = g(accessibilityNodeInfo);
            if (!TextUtils.isEmpty(this.f13740f) && ((a(listG, "拼单成功", false) || a(listG, "已支付", false) || a(listG, "免拼成功", false)) && (listG.contains("微信好友") || listG.contains("继续逛逛") || a(listG, "查看订单详情", false)))) {
                this.a = 1;
                this.b = true;
                y.l("nodeInfo pdd can add : " + this.b);
            } else if (listG.contains("当前状态") && listG.contains("交易单号")) {
                this.a = 2;
                this.b = true;
                y.l("nodeInfo pdd can add : " + this.b);
            } else if (!TextUtils.isEmpty(this.f13740f) && listG.contains("支付成功")) {
                this.a = 1;
                this.b = true;
                y.l("nodeInfo pdd can add : " + this.b);
            } else if (a(listG, "已选：", false) || a(listG, "已选:", false)) {
                int iF = f(listG, "¥", false);
                if (iF > 0) {
                    String str2 = listG.get(iF);
                    if (str2.indexOf("¥") < 2) {
                        while (true) {
                            if (i7 < 5 && (i6 = iF - i7) >= 0) {
                                String str3 = listG.get(i6);
                                if (str3.contains("¥")) {
                                    String strReplace2 = str3.substring(str3.indexOf("¥")).replace(" ", "").replace("¥", "").replace(",", "");
                                    if (h2.J(strReplace2)) {
                                        this.f13741g = strReplace2;
                                        y.l("sssss", "num1:" + this.f13741g);
                                    }
                                }
                                i7++;
                            } else if (str2.contains(" ¥")) {
                                strReplace = str2.substring(str2.indexOf("¥")).replace(" ", "").replace("¥", "").replace(",", "");
                                if (h2.J(strReplace)) {
                                    this.f13741g = strReplace;
                                    y.l("sssss", "num2:" + this.f13741g);
                                }
                            }
                        }
                    } else if (str2.contains(" ¥")) {
                        strReplace = str2.substring(str2.indexOf("¥")).replace(" ", "").replace("¥", "").replace(",", "");
                        if (h2.J(strReplace)) {
                            this.f13741g = strReplace;
                            y.l("sssss", "num2:" + this.f13741g);
                        }
                    }
                }
                int iD = d(listG, "已选择：", false);
                if (iD >= 0) {
                    this.f13740f = listG.get(iD).replace("已选择：", "").trim();
                    y.l("sssss", "remark:" + this.f13740f);
                } else {
                    int iD2 = d(listG, "已选：", false);
                    if (iD2 >= 0) {
                        this.f13740f = listG.get(iD2).replace("已选：", "").trim();
                        y.l("sssss", "remark:" + this.f13740f);
                    } else {
                        int iD3 = d(listG, "已选:", false);
                        if (iD3 >= 0) {
                            this.f13740f = listG.get(iD3).replace("已选:", "").trim();
                            y.l("sssss", "remark:" + this.f13740f);
                        }
                    }
                }
                int iD4 = d(listG, "使用#", false);
                if (iD4 > 0) {
                    String strReplace3 = listG.get(iD4).replace("优先使用#", "").replace("使用#", "");
                    this.f13739e = strReplace3;
                    if (strReplace3.contains("，")) {
                        String str4 = this.f13739e;
                        this.f13739e = str4.substring(0, str4.indexOf("，"));
                    }
                    if (this.f13739e.contains("最高减")) {
                        String str5 = this.f13739e;
                        this.f13739e = str5.substring(0, str5.indexOf("最高减"));
                    }
                    y.l("sssss", "asset:" + this.f13739e);
                }
            }
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        q(context, listG);
    }

    public BillInfo t(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("拼多多");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if ((str.contains("-") || str.contains("+")) && i6 > 0) {
                if (str.contains("+")) {
                    billInfo.setIncome(true);
                }
                String strReplace = str.replace("+", "").replace("-", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                    billInfo.setRemark(list.get(i6 - 1));
                    billInfo.setShopName(billInfo.getRemark());
                }
            } else if ((str.contains("支付时间") || str.contains("退款时间") || str.contains("到账时间")) && i6 < list.size() - 1) {
                billInfo.setTime(t0.X0(list.get(i6 + 1), "yyyy-MM-dd HH:mm:ss"));
            } else if ((str.equals("商品详情") || str.equals("退款商品") || str.equals("关联商品")) && i6 < list.size() - 1) {
                if (str.equals("退款商品")) {
                    billInfo.setRemark("退款-" + list.get(i6 + 1));
                } else {
                    billInfo.setRemark(list.get(i6 + 1));
                }
            } else if ((str.equals("支付方式") || str.equals("退款至")) && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1));
            } else if (str.equals("优惠") && i6 < list.size() - 1) {
                String strReplace2 = list.get(i6 + 1).replace("多多支付立减优惠¥", "");
                if (h2.J(strReplace2)) {
                    billInfo.setDiscount(strReplace2);
                }
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public boolean v(List<AccessibilityNodeInfo> list) {
        return (list == null || list.isEmpty()) ? false : true;
    }
}
