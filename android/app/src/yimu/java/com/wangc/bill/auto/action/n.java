// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 3f8290ad250d3c17b53e594bd3946cd582ad17ee985df88c860d2ed71fc57649
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
import java.util.Arrays;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class n extends c implements x1.a {

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    private static n f13753k = null;

    /* JADX INFO: renamed from: l, reason: collision with root package name */
    private static final int f13754l = 4;

    /* JADX INFO: renamed from: m, reason: collision with root package name */
    private static final int f13755m = 5;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private String f13756e;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13757f;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private String f13758g;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private String f13759h;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private boolean f13760i = false;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private boolean f13761j = false;

    private void q(Context context, List<String> list) {
        BillInfo billInfoU;
        a2.a("start find node");
        if (list == null || list.isEmpty()) {
            return;
        }
        if (this.a == 4) {
            if (a(list, "抖音月付", false)) {
                this.f13756e = "抖音月付";
            }
            a2.a("start check pay:" + this.a);
            billInfoU = r(this.f13756e, this.f13757f, this.f13758g);
        } else {
            billInfoU = (list.contains("抖音月付还款") || list.contains("还款成功")) ? u(list) : t(context, list);
        }
        if (billInfoU != null) {
            if (this.a != 5) {
                this.f13760i = false;
            }
            this.b = false;
            this.f13756e = null;
            this.f13757f = null;
            this.f13759h = null;
            this.f13758g = null;
            billInfoU.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
            SelectToSpeakService.l(context, billInfoU, this);
        }
    }

    public static n v() {
        if (f13753k == null) {
            f13753k = new n();
        }
        return f13753k;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
    }

    public BillInfo r(String str, String str2, String str3) {
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("抖音");
        billInfo.setRemark("抖音");
        billInfo.setShopName("抖音");
        if (TextUtils.isEmpty(str)) {
            billInfo.setAsset("抖音支付");
        } else {
            billInfo.setAsset(str);
        }
        if (!TextUtils.isEmpty(str2)) {
            billInfo.setRemark(str2);
        }
        if (!TextUtils.isEmpty(str3) && h2.J(str3)) {
            billInfo.setNumber(str3);
        }
        if (!TextUtils.isEmpty(this.f13759h) && h2.J(this.f13759h)) {
            billInfo.setDiscount(this.f13759h);
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    /* JADX WARN: Code duplicated, block: B:160:0x02f6  */
    /* JADX WARN: Code duplicated, block: B:167:0x031e  */
    /* JADX WARN: Code duplicated, block: B:174:0x0342  */
    /* JADX WARN: Code duplicated, block: B:181:0x0388  */
    /* JADX WARN: Code duplicated, block: B:189:0x03aa  */
    /* JADX WARN: Code duplicated, block: B:191:0x03b2  */
    /* JADX WARN: Code duplicated, block: B:194:0x03bb  */
    /* JADX WARN: Code duplicated, block: B:197:0x03c4  */
    /* JADX WARN: Code duplicated, block: B:199:0x03e9  */
    /* JADX WARN: Code duplicated, block: B:200:0x03ec  */
    /* JADX WARN: Code duplicated, block: B:208:0x0403  */
    /* JADX WARN: Code duplicated, block: B:220:0x0435  */
    /* JADX WARN: Code duplicated, block: B:222:0x0469  */
    /* JADX WARN: Code duplicated, block: B:223:0x046d  */
    /* JADX WARN: Instruction removed from duplicated block: B:197:0x03c4, please report this as an issue */
    /* JADX WARN: Instruction removed from duplicated block: B:220:0x0435, please report this as an issue */
    public void s(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        boolean z5;
        int iD;
        int iD2;
        char c;
        String str2;
        String str3;
        String strReplace;
        String str4;
        String strReplace2;
        String strReplace3;
        List<String> list = null;
        char c2 = 0;
        if (str.equals("com.ss.android.ugc.aweme.main.MainActivity") || str.equals("com.ss.android.ugc.aweme.newdetail.DitoPoiDetailActivity") || str.equals("com.ss.android.ugc.aweme.search.activity.SearchResultActivity") || str.equals("com.ss.android.ugc.aweme.detail.ui.LiveDetailActivity") || str.equals("com.ss.android.ugc.aweme.detail.ui.DetailActivity") || str.equals("com.ss.android.ugc.aweme.live.LivePlayActivity") || str.equals("com.ss.android.ugc.aweme.commerce.sdk.MallContainerActivity") || str.equals("com.bytedance.android.shopping.anchorv4.containers.AnchorV4Activity")) {
            this.f13760i = false;
            this.f13757f = null;
            this.f13758g = null;
            this.f13756e = null;
            this.f13759h = null;
            this.b = false;
            this.f13761j = false;
        }
        if (str.equals("com.ss.android.ugc.aweme.bullet.ui.BulletContainerActivity") || str.equals("com.android.ttcjpaysdk.base.ui.dialog.CJPayCommonDialog") || str.equals("com.android.ttcjpaysdk.thirdparty.front.counter.dypay.activity.DyPayETActivity") || str.equals("com.bytedance.android.ec.common.impl.view.FixedDialog") || str.equals("com.bytedance.android.shopping.buynow.container.BuyNowFullscreenActivity") || str.equals("com.bytedance.ies.xelement.overlay.LynxOverlayDialog") || str.equals("com.ss.android.ugc.aweme.live.LiveDummyActivity") || str.equals("com.bytedance.android.shopping.buynow.container.BuyNowFullscreenDialog") || str.equals("com.android.ttcjpaysdk.thirdparty.front.counter.dypay.activity.DyPayStandardActivity") || str.equals("com.android.ttcjpaysdk.bdpay.security.loading.view.CJPaySecurityLoadingActivity") || str.startsWith("X.") || (str.equals("com.lynx.tasm.behavior.KeyboardMonitor") && accessibilityNodeInfo != null && w(accessibilityNodeInfo.findAccessibilityNodeInfosByText("提交订单")))) {
            this.f13760i = true;
        } else if (str.equals("com.lynx.tasm.behavior.KeyboardMonitor")) {
            this.f13761j = true;
        }
        if (this.f13761j && accessibilityNodeInfo != null && w(accessibilityNodeInfo.findAccessibilityNodeInfosByText("提交订单"))) {
            this.f13761j = false;
            this.f13760i = true;
        }
        if (this.f13760i) {
            int i6 = 2;
            if (accessibilityNodeInfo != null) {
                List<String> listL = l(accessibilityNodeInfo);
                if (this.c >= 199 && listL.contains("扫一扫") && listL.contains("通知消息") && listL.contains("常用功能")) {
                    this.f13760i = false;
                    return;
                }
                if (listL.contains("账单详情")) {
                    this.b = true;
                    this.a = 5;
                    if (listL.size() == 4 && !TextUtils.isEmpty(listL.get(3))) {
                        listL.addAll(Arrays.asList(listL.get(3).split(" ")));
                    } else if (listL.size() == 3 && !TextUtils.isEmpty(listL.get(2))) {
                        listL.addAll(Arrays.asList(listL.get(2).split(" ")));
                    }
                } else {
                    int size = listL.size() - 1;
                    String str5 = null;
                    while (size >= 0) {
                        String str6 = listL.get(size);
                        if (str6.contains("【") && str6.contains("】")) {
                            this.f13757f = str6;
                        }
                        if (str6.equals("团购头图") && size < listL.size() - 1) {
                            String str7 = listL.get(size + 1);
                            if (!str7.equals("团购头图")) {
                                this.f13757f = str7;
                            }
                        }
                        if (str6.startsWith("已选择")) {
                            this.f13757f = str6.replace("已选择", "");
                        }
                        if ((str6.contains("实付") && str6.contains("元")) || (str6.contains("实付") && str6.contains("¥"))) {
                            String strReplace4 = str6.replace("实付", "").replace("元", "").replace("¥", "").replace(",", "");
                            if (h2.J(strReplace4)) {
                                this.f13758g = strReplace4;
                            }
                        } else if (str6.equals("提交订单,按钮") && size > i6) {
                            String str8 = listL.get(size - 1);
                            if (!str8.contains("¥")) {
                                str8 = listL.get(size - 2);
                            }
                            if (str8.contains("¥")) {
                                String[] strArrSplit = str8.split("¥");
                                if (strArrSplit.length > 0) {
                                    String str9 = strArrSplit[c2];
                                    if (TextUtils.isEmpty(str9) && strArrSplit.length > 1) {
                                        str9 = strArrSplit[1];
                                    }
                                    String strReplace5 = str9.replace("¥", "").replace(",", "");
                                    if (h2.J(strReplace5)) {
                                        this.f13758g = strReplace5;
                                    }
                                }
                            }
                        } else if (!str6.equals("提交订单") || size <= 2) {
                            if (str6.equals("实付价") && "¥".equals(listL.get(size + 1))) {
                                if (size < listL.size() - 3) {
                                    String strReplace6 = listL.get(size + 2).replace(",", "");
                                    String str10 = listL.get(size + 3);
                                    if (!TextUtils.isEmpty(str10) && str10.startsWith(".")) {
                                        strReplace6 = strReplace6 + str10;
                                    }
                                    if (h2.J(strReplace6)) {
                                        this.f13758g = strReplace6;
                                    }
                                }
                            }
                            if (listL.contains("支付成功") && str6.contains("¥")) {
                                strReplace3 = str6.replace("¥", "").replace(",", "").replace(" ", "");
                                if (h2.J(strReplace3)) {
                                    this.f13758g = strReplace3;
                                }
                            }
                            if (str6.contains("支付成功") && str6.contains("¥")) {
                                strReplace2 = str6.replace("¥", "").replace("支付成功", "").replace(" ", "").replace(",", "").replace(" ", "");
                                if (h2.J(strReplace2)) {
                                    this.f13758g = strReplace2;
                                }
                            }
                            if (str6.equals("支付方式") && size < listL.size() - 1) {
                                str4 = listL.get(size + 1);
                                if (!str4.equals("支付方式")) {
                                    this.f13756e = str4.replace("IB", "").replace("(", "").replace(")", "").replace(" ", "").replace("\u200b", "");
                                }
                            }
                            if (str6.contains("付款成功") && str6.contains("¥")) {
                                strReplace = str6.replace("¥", "").replace("付款成功", "").replace(" ", "").replace(",", "").replace(" ", "");
                                if (h2.J(strReplace)) {
                                    this.f13758g = strReplace;
                                }
                                if (TextUtils.isEmpty(this.f13756e) && size < listL.size() - 1) {
                                    this.f13756e = listL.get(size + 1);
                                }
                            }
                            if (str6.equals("¥")) {
                                if (size != listL.size() - 4) {
                                    c = 5;
                                    if (size == listL.size() - 5) {
                                    }
                                } else {
                                    c = 5;
                                }
                                if (size < listL.size() - 2) {
                                    str3 = listL.get(size + 1) + listL.get(size + 2);
                                    if (h2.J(str3)) {
                                        this.f13758g = str3;
                                    }
                                }
                            } else {
                                c = 5;
                            }
                            if (!TextUtils.isEmpty(str5) && str6.contains("(") && str6.contains(")")) {
                                str2 = str6;
                            } else {
                                str2 = str5;
                            }
                            if (TextUtils.isEmpty(this.f13756e) && str6.contains("，已选中")) {
                                this.f13756e = str6.replace("，已选中", "");
                                if (str6.contains("抖音支付") && !TextUtils.isEmpty(str2)) {
                                    this.f13756e = str2;
                                }
                            }
                            if (str6.startsWith("已优惠")) {
                                this.f13759h = str6.replace("已优惠", "").replace("元", "").replace(",", "");
                                y.l("sssss", "discountNum:" + this.f13759h);
                                if (!h2.J(this.f13759h)) {
                                    this.f13759h = null;
                                }
                            }
                            size--;
                            str5 = str2;
                            c2 = 0;
                            i6 = 2;
                        } else {
                            String strReplace7 = listL.get(size - 1).replace("¥", "").replace(",", "");
                            if (!h2.J(strReplace7)) {
                                strReplace7 = listL.get(size - 2).replace("¥", "").replace(",", "");
                            }
                            if (h2.J(strReplace7)) {
                                this.f13758g = strReplace7;
                            }
                        }
                        if (listL.contains("支付成功")) {
                            strReplace3 = str6.replace("¥", "").replace(",", "").replace(" ", "");
                            if (h2.J(strReplace3)) {
                                this.f13758g = strReplace3;
                            }
                        }
                        if (str6.contains("支付成功")) {
                            strReplace2 = str6.replace("¥", "").replace("支付成功", "").replace(" ", "").replace(",", "").replace(" ", "");
                            if (h2.J(strReplace2)) {
                                this.f13758g = strReplace2;
                            }
                        }
                        if (str6.equals("支付方式")) {
                            str4 = listL.get(size + 1);
                            if (!str4.equals("支付方式")) {
                                this.f13756e = str4.replace("IB", "").replace("(", "").replace(")", "").replace(" ", "").replace("\u200b", "");
                            }
                        }
                        if (str6.contains("付款成功")) {
                            strReplace = str6.replace("¥", "").replace("付款成功", "").replace(" ", "").replace(",", "").replace(" ", "");
                            if (h2.J(strReplace)) {
                                this.f13758g = strReplace;
                            }
                            if (TextUtils.isEmpty(this.f13756e)) {
                                this.f13756e = listL.get(size + 1);
                            }
                        }
                        if (str6.equals("¥")) {
                            if (size != listL.size() - 4) {
                                c = 5;
                                if (size == listL.size() - 5) {
                                }
                            } else {
                                c = 5;
                            }
                            if (size < listL.size() - 2) {
                                str3 = listL.get(size + 1) + listL.get(size + 2);
                                if (h2.J(str3)) {
                                    this.f13758g = str3;
                                }
                            }
                        } else {
                            c = 5;
                        }
                        if (!TextUtils.isEmpty(str5)) {
                            str2 = str5;
                        } else {
                            str2 = str5;
                        }
                        if (TextUtils.isEmpty(this.f13756e)) {
                            this.f13756e = str6.replace("，已选中", "");
                            if (str6.contains("抖音支付")) {
                                this.f13756e = str2;
                            }
                        }
                        if (str6.startsWith("已优惠")) {
                            this.f13759h = str6.replace("已优惠", "").replace("元", "").replace(",", "");
                            y.l("sssss", "discountNum:" + this.f13759h);
                            if (!h2.J(this.f13759h)) {
                                this.f13759h = null;
                            }
                        }
                        size--;
                        str5 = str2;
                        c2 = 0;
                        i6 = 2;
                    }
                    y.l("sssss", "asset:" + this.f13756e, "remark:" + this.f13757f, "num:" + this.f13758g);
                }
                list = listL;
            }
            if (!this.b && accessibilityNodeInfo != null) {
                if (a(list, "购买成功", true)) {
                    z5 = false;
                } else {
                    z5 = false;
                    if (a(list, "支付成功", false)) {
                    }
                }
                this.b = true;
                this.a = 4;
                if (a(list, "抖音支付 (", z5) && (iD2 = d(list, "抖音支付 (", z5)) >= 0 && iD2 < list.size()) {
                    this.f13756e = list.get(iD2).replace("抖音支付 (", "").replace(") 付款", "");
                    y.l("check asset : " + this.f13756e);
                }
                if (a(list, "支付成功", true) && (iD = d(list, "支付成功", true)) >= 0 && iD < list.size() - 1) {
                    String str11 = list.get(iD + 1);
                    if (str11.contains("¥")) {
                        String strReplace8 = str11.replace("¥", "").replace(",", "").replace("确认收货后抖音月付付款", "");
                        if (h2.J(strReplace8)) {
                            this.f13758g = strReplace8;
                            y.l("check num : " + this.f13758g);
                        } else if ("¥".equals(str11) && iD < list.size() - 2) {
                            String strReplace9 = list.get(iD + 2).replace(",", "");
                            if (h2.J(strReplace9)) {
                                this.f13758g = strReplace9;
                                y.l("check num : " + this.f13758g);
                            }
                        }
                    }
                }
                y.l("nodeInfo tiktok can add : " + this.b);
            }
        }
        if (!this.b || accessibilityNodeInfo == null || list == null) {
            return;
        }
        q(context, list);
    }

    /* JADX WARN: Code duplicated, block: B:47:0x0124  */
    /* JADX WARN: Code duplicated, block: B:53:0x0141  */
    public BillInfo t(Context context, List<String> list) {
        int i6;
        int i7;
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("抖音");
        for (int i8 = 0; i8 < list.size(); i8++) {
            String str = list.get(i8);
            if ((str.equals("支付成功") || str.equals("退款成功") || str.equals("已过期") || str.equals("已全额退款") || str.equals("入账成功") || str.equals("确认收货后付款") || str.equals("到账成功") || str.equals("充值成功") || str.equals("自动付款成功") || str.startsWith("已退款(￥")) && i8 > 2) {
                String strReplace = list.get(i8 - 1).replace("¥", "").replace(",", "").replace("+", "");
                if (!h2.J(strReplace)) {
                    strReplace = list.get(i8 - 2).replace("¥", "").replace(",", "").replace("+", "");
                    i6 = 2;
                } else if (h2.J(strReplace)) {
                    i6 = 1;
                } else {
                    strReplace = list.get(i8 - 3).replace("¥", "").replace(",", "").replace("+", "");
                    i6 = 3;
                }
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                    if (i8 > i6 && TextUtils.isEmpty(billInfo.getRemark())) {
                        int i9 = (i8 - i6) - 1;
                        if (!list.get(i9).startsWith(u0.c.f16993e)) {
                            billInfo.setRemark(list.get(i9));
                            billInfo.setShopName(list.get(i9));
                        } else if (i8 > i6 + 1) {
                            i7 = (i8 - i6) - 2;
                            if (!list.get(i7).startsWith(u0.c.f16993e)) {
                                billInfo.setRemark(list.get(i7));
                                billInfo.setShopName(list.get(i7));
                            }
                        }
                    } else if (i8 > i6 + 1 && TextUtils.isEmpty(billInfo.getRemark())) {
                        i7 = (i8 - i6) - 2;
                        if (!list.get(i7).startsWith(u0.c.f16993e)) {
                            billInfo.setRemark(list.get(i7));
                            billInfo.setShopName(list.get(i7));
                        }
                    }
                    if (list.get(i8 - i6).contains("+")) {
                        billInfo.setIncome(true);
                    }
                }
            } else if ((str.equals("支付方式") || str.equals("退款方式") || str.equals("到账方式") || str.equals("充值方式") || str.equals("付款方式")) && i8 < list.size() - 2) {
                billInfo.setAsset(list.get(i8 + 2));
            } else if ((str.equals("交易方式") || str.equals("入账账户")) && i8 < list.size() - 1) {
                billInfo.setAsset(list.get(i8 + 1));
            } else if ((str.equals("退款时间") || str.equals("过期时间") || str.equals("入账时间") || str.equals("到账时间") || str.equals("发起话费充值") || str.equals("充值时间")) && i8 < list.size() - 1) {
                int i10 = i8 + 1;
                billInfo.setTime(t0.X0(list.get(i10), "yyyy-MM-dd HH:mm:ss"));
                if (billInfo.getTime() == -1 && i8 < list.size() - 2) {
                    billInfo.setTime(t0.X0(list.get(i10) + " " + list.get(i8 + 2), "yyyy-MM-dd HH:mm:ss"));
                }
                if (billInfo.getTime() == -1) {
                    billInfo.setTime(t0.X0(list.get(i10), "yyyy-MM-dd"));
                }
            } else if ((str.equals("创建时间") || str.equals("支付时间")) && i8 < list.size() - 1) {
                int i11 = i8 + 1;
                String str2 = list.get(i11);
                if (i8 < list.size() - 2) {
                    String str3 = list.get(i8 + 2);
                    if (str3.contains(":") && !str2.contains(":")) {
                        str2 = str2 + " " + str3;
                    }
                }
                billInfo.setTime(t0.X0(str2, "yyyy-MM-dd HH:mm:ss"));
                if (billInfo.getTime() == -1) {
                    billInfo.setTime(t0.X0(list.get(i11), "yyyy-MM-dd"));
                }
            } else {
                if (str.equals("商品订单") && i8 < list.size() - 2) {
                    String str4 = list.get(i8 + 1);
                    billInfo.setRemark(null);
                    if ((!TextUtils.isEmpty(str4) && (str4.endsWith("~240x0") || str4.contains("&x-expires="))) || "在此商户的交易".equals(str4)) {
                        billInfo.setRemark(list.get(i8 + 2));
                    } else {
                        if ("申请电子回单".equals(str4) || "查看月付账单".equals(str4) || "联系商家".equals(str4) || "申请电子交易凭证".equals(str4)) {
                            return null;
                        }
                        billInfo.setRemark(str4);
                    }
                    if (!TextUtils.isEmpty(billInfo.getRemark())) {
                        SelectToSpeakService.E(context, billInfo.getRemark());
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(billInfo.getAsset()) && billInfo.getAsset().contains("抖音支付")) {
            billInfo.setAsset(billInfo.getAsset().replace("抖音支付", "").replace("(", "").replace(")", "").trim());
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo u(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setTransfer(true);
        billInfo.setOrigin("抖音");
        billInfo.setToAsset("抖音月付");
        billInfo.setRemark("抖音月付还款");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if ((str.equals("支付成功") || str.equals("还款成功")) && i6 > 0) {
                String strReplace = list.get(i6 - 1).replace("¥", "").replace(",", "").replace("+", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                }
                if (str.equals("还款成功") && i6 > 1) {
                    billInfo.setRemark(list.get(i6 - 2));
                }
            } else if ((str.equals("还款方式") || str.equals("付款方式")) && i6 < list.size() - 2) {
                billInfo.setFromAsset(list.get(i6 + 2).replace("抖音支付", "").replace("(", "").replace(")", "").trim());
            } else if (str.equals("还款到") && i6 < list.size() - 1) {
                billInfo.setToAsset(list.get(i6 + 1).replace("(", "").replace(")", "").trim());
            } else if ((str.equals("还款时间") || str.equals("发起时间")) && i6 < list.size() - 1) {
                billInfo.setTime(t0.X0(list.get(i6 + 1), "yyyy-MM-dd HH:mm:ss"));
            }
        }
        if (TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public boolean w(List<AccessibilityNodeInfo> list) {
        return (list == null || list.isEmpty()) ? false : true;
    }
}
