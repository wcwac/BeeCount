// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 f4db9b57c4ddcdbdca27380aa233349218e632876ff44c26b03564428aecbe86
package com.wangc.bill.auto.action;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import com.blankj.utilcode.util.t0;
import com.blankj.utilcode.util.y;
import com.google.android.accessibility.selecttospeak.SelectToSpeakService;
import com.wangc.bill.auto.x1;
import com.wangc.bill.database.action.s;
import com.wangc.bill.http.entity.BillInfo;
import com.wangc.bill.utils.a2;
import com.wangc.bill.utils.b2;
import com.wangc.bill.utils.h2;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class p extends c implements x1.a {

    /* JADX INFO: renamed from: l, reason: collision with root package name */
    private static final int f13769l = 1;

    /* JADX INFO: renamed from: m, reason: collision with root package name */
    private static final int f13770m = 3;

    /* JADX INFO: renamed from: n, reason: collision with root package name */
    private static final int f13771n = 4;

    /* JADX INFO: renamed from: o, reason: collision with root package name */
    private static final int f13772o = 10;

    /* JADX INFO: renamed from: p, reason: collision with root package name */
    private static final int f13773p = 11;

    /* JADX INFO: renamed from: q, reason: collision with root package name */
    private static p f13774q;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private long f13780j;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    String f13775e = null;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    String f13776f = null;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private boolean f13777g = false;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private boolean f13778h = false;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private boolean f13779i = false;

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    private boolean f13781k = false;

    public static p A() {
        if (f13774q == null) {
            f13774q = new p();
        }
        return f13774q;
    }

    private void r(Context context, List<String> list, HashMap<String, Boolean> map) {
        boolean z5;
        BillInfo billInfoU;
        int iIndexOf;
        int iIndexOf2;
        int iF;
        y.l("sssss", "start find node", Integer.valueOf(this.a));
        a2.a("start find node");
        if (list == null || list.isEmpty()) {
            return;
        }
        if (this.a == 1) {
            z5 = (map.containsKey("付款成功") || map.containsKey("支付成功")) && !map.containsKey("当前状态");
            if (map.containsKey("付款方式") && (iF = A().f(list, "付款方式", true)) < list.size() - 2) {
                String str = list.get(iF + 1);
                this.f13775e = str;
                if ("更改".equals(str) && iF < list.size() - 3) {
                    String str2 = list.get(iF + 2);
                    this.f13775e = str2;
                    if ("更改".equals(str2) && iF < list.size() - 4) {
                        this.f13775e = list.get(iF + 3);
                    }
                }
            }
            if (map.containsKey("优先使用此支付方式付款")) {
                int iIndexOf3 = list.indexOf("优先使用此支付方式付款") - 1;
                if (iIndexOf3 >= 0) {
                    this.f13775e = list.get(iIndexOf3);
                }
            } else if (map.containsKey("优先使用此付款方式")) {
                int iIndexOf4 = list.indexOf("优先使用此付款方式") - 1;
                if (iIndexOf4 >= 0) {
                    this.f13775e = list.get(iIndexOf4);
                }
            } else if (map.containsKey("优先付款方式") && (iIndexOf2 = list.indexOf("优先付款方式")) < list.size() - 2) {
                String str3 = list.get(iIndexOf2 + 1);
                this.f13775e = str3;
                if ("更改".equals(str3)) {
                    this.f13775e = list.get(iIndexOf2 + 2);
                }
            }
        } else {
            z5 = true;
        }
        if (this.a == 10) {
            if (map.containsKey("充值方式")) {
                int iIndexOf5 = list.indexOf("充值方式");
                if (iIndexOf5 < list.size() - 1) {
                    this.f13776f = list.get(iIndexOf5 + 1);
                }
            } else if (map.containsKey("到账银行卡") && (iIndexOf = list.indexOf("到账银行卡")) < list.size() - 1) {
                this.f13776f = list.get(iIndexOf + 1);
            }
        }
        boolean z6 = map.containsKey("已收款") || map.containsKey("资金待入账") || a(list, "你已收款", false) || a(list, "已收款", false);
        boolean zA = a(list, "的红包", false);
        if (!z5) {
            if (z6) {
                a2.a("start check receive");
                BillInfo billInfoX = x(list);
                if (billInfoX != null) {
                    this.b = false;
                    billInfoX.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                    SelectToSpeakService.l(context, billInfoX, this);
                    return;
                }
                return;
            }
            if (zA) {
                a2.a("start check red package");
                BillInfo billInfoS = s(list);
                if (billInfoS == null) {
                    billInfoS = q(list);
                }
                if (billInfoS != null) {
                    this.b = false;
                    billInfoS.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                    SelectToSpeakService.l(context, billInfoS, this);
                    return;
                }
                return;
            }
            return;
        }
        a2.a("start check pay:" + this.a);
        int i6 = this.a;
        if (i6 == 1) {
            billInfoU = w(list, this.f13775e);
        } else if (i6 == 3) {
            billInfoU = v(list);
        } else if (i6 == 10) {
            billInfoU = y(list, this.f13776f);
        } else if (i6 == 11) {
            billInfoU = z(list);
        } else {
            billInfoU = i6 == 4 ? u(list) : null;
        }
        if (billInfoU != null) {
            this.f13776f = null;
            this.b = false;
            this.f13781k = false;
            this.f13779i = false;
            a2.a("start addBill");
            billInfoU.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
            SelectToSpeakService.l(context, billInfoU, this);
        }
    }

    public static BillInfo s(List<String> list) {
        int iIndexOf;
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("微信");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.contains("红包金额")) {
                int iIndexOf2 = str.indexOf("元");
                if (iIndexOf2 > 4) {
                    String strReplace = str.substring(0, iIndexOf2).replace("红包金额", "").replace(",", "");
                    if (h2.J(strReplace)) {
                        billInfo.setNumber(strReplace);
                        billInfo.setRemark("发送微信红包");
                        billInfo.setShopName(billInfo.getRemark());
                        return billInfo;
                    }
                } else {
                    continue;
                }
            } else if (str.contains("个红包共")) {
                int iIndexOf3 = str.indexOf("元");
                if (iIndexOf3 > 4) {
                    String strReplace2 = str.substring(str.indexOf("个红包共") + 4, iIndexOf3).replace(",", "");
                    if (h2.J(strReplace2)) {
                        billInfo.setNumber(strReplace2);
                        billInfo.setRemark("发送微信红包");
                        billInfo.setShopName(billInfo.getRemark());
                        return billInfo;
                    }
                } else {
                    continue;
                }
            } else if (str.contains("人已领取")) {
                int iIndexOf4 = str.indexOf("元");
                if (iIndexOf4 > 4) {
                    String strReplace3 = str.substring(str.indexOf("人已领取") + 6, iIndexOf4).replace(",", "");
                    if (h2.J(strReplace3)) {
                        billInfo.setNumber(strReplace3);
                        billInfo.setRemark("发送微信红包");
                        billInfo.setShopName(billInfo.getRemark());
                        return billInfo;
                    }
                } else {
                    continue;
                }
            } else if (str.contains("个，共") && (iIndexOf = str.indexOf("元")) > 4) {
                String strReplace4 = str.substring(str.lastIndexOf("/") + 1, iIndexOf).replace(",", "");
                if (h2.J(strReplace4)) {
                    billInfo.setNumber(strReplace4);
                    billInfo.setRemark("发送微信红包");
                    billInfo.setShopName(billInfo.getRemark());
                    return billInfo;
                }
            }
        }
        return null;
    }

    public boolean B(List<AccessibilityNodeInfo> list) {
        return (list == null || list.isEmpty()) ? false : true;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
        this.b = false;
        this.f13781k = false;
        this.f13779i = false;
    }

    public BillInfo q(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("微信");
        billInfo.setAsset("微信");
        billInfo.setIncome(true);
        for (int i6 = 0; i6 < list.size(); i6++) {
            String strReplace = list.get(i6).replace("元", "").replace(",", "");
            if (h2.J(strReplace)) {
                billInfo.setNumber(strReplace);
                if (i6 < list.size() - 1 && (list.get(i6).contains("元") || list.get(i6 + 1).contains("元"))) {
                    if (i6 > 1) {
                        int i7 = i6 - 2;
                        if (list.get(i7).contains("的红包")) {
                            billInfo.setRemark(list.get(i7));
                            billInfo.setShopName(billInfo.getRemark());
                            return billInfo;
                        }
                    }
                    if (i6 > 0) {
                        int i8 = i6 - 1;
                        if (list.get(i8).contains("的红包")) {
                            billInfo.setRemark(list.get(i8));
                            billInfo.setShopName(billInfo.getRemark());
                            return billInfo;
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        return null;
    }

    /* JADX WARN: Code duplicated, block: B:120:0x023f  */
    /* JADX WARN: Code duplicated, block: B:247:0x0412  */
    /* JADX WARN: Code duplicated, block: B:281:0x04af  */
    /* JADX WARN: Code duplicated, block: B:283:0x04b9  */
    /* JADX WARN: Code duplicated, block: B:301:0x04ef  */
    /* JADX WARN: Code duplicated, block: B:304:0x04f8  */
    /* JADX WARN: Code duplicated, block: B:317:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:87:0x018e A[ADDED_TO_REGION] */
    public void t(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        Object obj;
        Object obj2;
        Object obj3;
        Object obj4;
        String str2;
        List<String> listG;
        HashMap<String, Boolean> map;
        Object obj5;
        Object obj6;
        String str3;
        Object obj7;
        Object obj8;
        String str4 = "付款方式";
        HashMap<String, Boolean> map2 = null;
        if (!str.equals("com.tencent.mm.ui.LauncherUI") || accessibilityNodeInfo == null) {
            if (str.equals("com.tencent.mm.plugin.webview.ui.tools.MMWebViewUI") || str.equals("com.tencent.mm.plugin.webview.ui.tools.WebViewUI") || str.equals("com.tencent.mm.framework.app.UIPageFragmentActivity") || str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceBusiUI") || str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceUI") || str.equals("com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI") || str.equals("com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyPrepareUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNewDetailUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI") || str.equals("com.tencent.mm.plugin.wallet_index.ui.OrderHandlerUI") || str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceDetailUI") || str.equals("com.tencent.mm.plugin.wallet_core.ui.WalletOrderInfoNewUI") || str.equals("com.tencent.mm.plugin.lite.ui.WxaLiteAppTransparentLiteUI") || str.equals("com.tencent.mm.plugin.aa.ui.PaylistAAUI") || str.equals("com.tencent.mm.plugin.appbrand.ui.AppBrandUI00")) {
                obj = "当前状态";
                obj2 = "支付成功";
                obj3 = "交易单号";
                obj4 = "com.tencent.mm.plugin.remittance.ui.RemittanceDetailUI";
                this.f13777g = true;
                if (str.equals("com.tencent.mm.plugin.aa.ui.PaylistAAUI")) {
                    this.f13778h = true;
                }
            } else if (str.equals("com.tencent.mm.plugin.scanner.ui.BaseScanUI")) {
                this.f13781k = true;
            } else {
                if (!str.equals("com.tencent.mm.plugin.appbrand.ui.AppBrandPluginUI")) {
                    if (!this.f13781k || accessibilityNodeInfo == null) {
                        str2 = str4;
                    } else {
                        if (!B(accessibilityNodeInfo.findAccessibilityNodeInfosByText("支付成功"))) {
                            str2 = str4;
                            if (B(accessibilityNodeInfo.findAccessibilityNodeInfosByText(str2))) {
                            }
                            if (this.f13777g || accessibilityNodeInfo == null) {
                                Object obj9 = obj4;
                                if (str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceBusiUI") || str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceUI") || str.equals("com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI") || str.equals("com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyPrepareUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNewDetailUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI") || str.equals("com.tencent.mm.plugin.wallet_index.ui.OrderHandlerUI") || str.equals(obj9)) {
                                    this.a = 1;
                                    this.b = true;
                                }
                            } else {
                                listG = m(accessibilityNodeInfo);
                                y.l("sssss", "start get node", Integer.valueOf(listG.size()));
                                HashMap<String, Boolean> map3 = new HashMap<>();
                                for (Iterator<String> it = listG.iterator(); it.hasNext(); it = it) {
                                    map3.put(it.next(), Boolean.TRUE);
                                }
                                if (map3.containsKey(obj) && (map3.containsKey("提现单号") || map3.containsKey("充值完成") || map3.containsKey("还款成功"))) {
                                    obj5 = "二维码收款";
                                } else {
                                    if (map3.containsKey(obj3)) {
                                        obj5 = "二维码收款";
                                        if (a(listG, "转入零钱通", false)) {
                                        }
                                        if (s.i() && !map3.containsKey(obj6) && ((map3.containsKey("优先使用此付款方式") && map3.containsKey(obj8)) || ((map3.containsKey("优先付款方式") && map3.containsKey(obj8)) || ((map3.containsKey("优先使用此支付方式付款") && map3.containsKey(obj8)) || (map3.containsKey(str3) && (map3.containsKey("使用密码") || map3.containsKey("使用指纹") || map3.containsKey("请输入支付密码") || ((map3.containsKey(obj7) && map3.containsKey("1") && map3.containsKey("2")) || (map3.containsKey(obj7) && map3.containsKey(str3))))))))) {
                                            this.f13780j = System.currentTimeMillis();
                                            y.l("sssss-time", Boolean.valueOf(map3.containsKey(obj6)), "time:" + this.f13780j);
                                        }
                                        map2 = map3;
                                    } else {
                                        obj5 = "二维码收款";
                                    }
                                    if (!map3.containsKey("账单详情") || ((map3.containsKey("商品名称") && map3.containsKey("交易状态")) || ((map3.containsKey(obj) && (map3.containsKey("转账单号") || map3.containsKey(obj3) || map3.containsKey("付款单号") || map3.containsKey("收款单号"))) || ((map3.containsKey("退款状态") && map3.containsKey("退款单号")) || ((map3.containsKey("付款商家") && map3.containsKey("付款单号")) || ((map3.containsKey("详情") && a(listG, "零钱通余额", false)) || (map3.containsKey("零钱通余额") && map3.containsKey(obj3)))))))) {
                                        obj6 = obj2;
                                        str3 = str4;
                                        obj7 = "更改";
                                        obj8 = obj5;
                                        this.a = 3;
                                        this.b = true;
                                    } else if (this.f13778h && a(listG, "发起的群收款", false)) {
                                        this.a = 4;
                                        this.b = true;
                                        obj6 = obj2;
                                        str3 = str4;
                                        obj7 = "更改";
                                        obj8 = obj5;
                                    } else {
                                        if (str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceBusiUI") || str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceUI") || str.equals("com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI") || str.equals("com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyPrepareUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNewDetailUI") || str.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI") || str.equals("com.tencent.mm.plugin.wallet_index.ui.OrderHandlerUI") || str.equals(obj4)) {
                                            obj6 = obj2;
                                        } else {
                                            obj6 = obj2;
                                            if ((!map3.containsKey(obj6) && !map3.containsKey("付款成功")) || map3.containsKey(obj) || map3.containsKey("查看账单详情") || map3.containsKey("常见问题") || map3.containsKey("订单支付成功通知") || map3.containsKey(obj3) || map3.containsKey("交易时间")) {
                                                obj8 = obj5;
                                                if (map3.containsKey(obj8) && (map3.containsKey("优先付款方式") || map3.containsKey("优先使用此支付方式付款") || map3.containsKey("优先使用此付款方式"))) {
                                                    str3 = str4;
                                                } else {
                                                    str3 = str4;
                                                    if (!map3.containsKey(str3)) {
                                                        obj7 = "更改";
                                                    } else if (!map3.containsKey("使用密码") && !map3.containsKey("使用指纹") && !map3.containsKey("请输入支付密码")) {
                                                        obj7 = "更改";
                                                        if (!map3.containsKey(obj7) || !map3.containsKey("1") || !map3.containsKey("2")) {
                                                        }
                                                    }
                                                    if ((map3.containsKey("充值成功") && map3.containsKey("完成")) || ((map3.containsKey("零钱提现") && map3.containsKey("到账成功")) || ((map3.containsKey("已转入") && map3.containsKey("完成")) || ((map3.containsKey("已转出") && map3.containsKey("完成")) || (map3.containsKey("转出金额") && map3.containsKey("完成")))))) {
                                                        this.a = 10;
                                                        this.b = true;
                                                    }
                                                }
                                                obj7 = "更改";
                                            }
                                            this.a = 1;
                                            this.b = true;
                                        }
                                        str3 = str4;
                                        obj7 = "更改";
                                        obj8 = obj5;
                                        this.a = 1;
                                        this.b = true;
                                    }
                                    if (s.i()) {
                                        this.f13780j = System.currentTimeMillis();
                                        y.l("sssss-time", Boolean.valueOf(map3.containsKey(obj6)), "time:" + this.f13780j);
                                    }
                                    map2 = map3;
                                }
                                if (map3.containsKey("查看账单详情") || map3.containsKey("常见问题")) {
                                    if (map3.containsKey("账单详情")) {
                                        obj6 = obj2;
                                        str3 = str4;
                                        obj7 = "更改";
                                        obj8 = obj5;
                                        this.a = 3;
                                        this.b = true;
                                    } else {
                                        obj6 = obj2;
                                        str3 = str4;
                                        obj7 = "更改";
                                        obj8 = obj5;
                                        this.a = 3;
                                        this.b = true;
                                    }
                                    if (s.i()) {
                                        this.f13780j = System.currentTimeMillis();
                                        y.l("sssss-time", Boolean.valueOf(map3.containsKey(obj6)), "time:" + this.f13780j);
                                    }
                                    map2 = map3;
                                } else {
                                    this.a = 11;
                                    this.b = true;
                                }
                                obj6 = obj2;
                                str3 = str4;
                                obj7 = "更改";
                                obj8 = obj5;
                                if (s.i()) {
                                    this.f13780j = System.currentTimeMillis();
                                    y.l("sssss-time", Boolean.valueOf(map3.containsKey(obj6)), "time:" + this.f13780j);
                                }
                                map2 = map3;
                            }
                            if (this.b || accessibilityNodeInfo == null) {
                            }
                            if (listG == null) {
                                listG = g(accessibilityNodeInfo);
                                map = new HashMap<>();
                                Iterator<String> it2 = listG.iterator();
                                while (it2.hasNext()) {
                                    map.put(it2.next(), Boolean.TRUE);
                                }
                            } else {
                                map = map2;
                            }
                            r(context, listG, map);
                            return;
                        }
                        str2 = str4;
                        this.f13777g = true;
                        str4 = str2;
                    }
                    boolean z5 = this.f13779i;
                    if (!z5 || accessibilityNodeInfo == null) {
                        str4 = str2;
                        obj = "当前状态";
                        obj2 = "支付成功";
                        obj3 = "交易单号";
                        obj4 = "com.tencent.mm.plugin.remittance.ui.RemittanceDetailUI";
                        if ((this.f13781k || z5) && accessibilityNodeInfo != null && B(accessibilityNodeInfo.findAccessibilityNodeInfosByText("通讯录")) && B(accessibilityNodeInfo.findAccessibilityNodeInfosByText("发现")) && B(accessibilityNodeInfo.findAccessibilityNodeInfosByText("微信"))) {
                            this.f13781k = false;
                            this.f13779i = false;
                            this.a = 0;
                            this.b = false;
                            this.f13777g = false;
                            this.f13778h = false;
                        }
                    } else {
                        List<String> listG2 = g(accessibilityNodeInfo);
                        str4 = str2;
                        obj = "当前状态";
                        if (listG2.contains(obj)) {
                            obj2 = "支付成功";
                            obj3 = "交易单号";
                            if (listG2.contains(obj3)) {
                                this.f13777g = true;
                            }
                            listG = listG2;
                            obj4 = "com.tencent.mm.plugin.remittance.ui.RemittanceDetailUI";
                        } else {
                            obj2 = "支付成功";
                            obj3 = "交易单号";
                        }
                        listG = listG2;
                        obj4 = "com.tencent.mm.plugin.remittance.ui.RemittanceDetailUI";
                    }
                    if (this.f13777g) {
                        Object obj10 = obj4;
                        if (str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceBusiUI")) {
                            this.a = 1;
                            this.b = true;
                        } else {
                            this.a = 1;
                            this.b = true;
                        }
                    } else {
                        Object obj11 = obj4;
                        if (str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceBusiUI")) {
                            this.a = 1;
                            this.b = true;
                        } else {
                            this.a = 1;
                            this.b = true;
                        }
                    }
                    if (this.b) {
                    }
                }
                this.f13779i = true;
            }
            listG = null;
            if (this.f13777g) {
                Object obj12 = obj4;
                if (str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceBusiUI")) {
                    this.a = 1;
                    this.b = true;
                } else {
                    this.a = 1;
                    this.b = true;
                }
            } else {
                Object obj13 = obj4;
                if (str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceBusiUI")) {
                    this.a = 1;
                    this.b = true;
                } else {
                    this.a = 1;
                    this.b = true;
                }
            }
            if (this.b) {
            }
        }
        this.a = 0;
        this.b = false;
        this.f13777g = false;
        this.f13778h = false;
        obj = "当前状态";
        obj2 = "支付成功";
        obj3 = "交易单号";
        obj4 = "com.tencent.mm.plugin.remittance.ui.RemittanceDetailUI";
        listG = null;
        if (this.f13777g) {
            Object obj14 = obj4;
            if (str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceBusiUI")) {
                this.a = 1;
                this.b = true;
            } else {
                this.a = 1;
                this.b = true;
            }
        } else {
            Object obj15 = obj4;
            if (str.equals("com.tencent.mm.plugin.remittance.ui.RemittanceBusiUI")) {
                this.a = 1;
                this.b = true;
            } else {
                this.a = 1;
                this.b = true;
            }
        }
        if (this.b) {
        }
    }

    public BillInfo u(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("微信");
        billInfo.setAsset("微信");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.contains("¥")) {
                if (str.contains("已收到")) {
                    billInfo.setIncome(true);
                    String strReplace = str.replace("已收到¥", "").replace(",", "");
                    if (h2.J(strReplace)) {
                        billInfo.setNumber(strReplace);
                        if (i6 > 0) {
                            billInfo.setRemark(list.get(i6 - 1));
                            billInfo.setShopName(billInfo.getRemark());
                        }
                    }
                } else if (str.contains("已支付") && !list.contains("你需支付")) {
                    billInfo.setIncome(false);
                    String strReplace2 = str.replace("已支付¥", "").replace(",", "");
                    if (h2.J(strReplace2)) {
                        billInfo.setNumber(strReplace2);
                        if (i6 > 0) {
                            billInfo.setRemark(list.get(i6 - 1));
                            billInfo.setShopName(billInfo.getRemark());
                        }
                    }
                } else if (str.contains("收到")) {
                    billInfo.setIncome(true);
                    String strReplace3 = str.replace("收到¥", "").replace(",", "");
                    if (h2.J(strReplace3)) {
                        billInfo.setNumber(strReplace3);
                    }
                }
            } else if (str.contains("已收齐") && i6 > 0) {
                billInfo.setRemark(list.get(i6 - 1));
                billInfo.setShopName(billInfo.getRemark());
            }
            if (!TextUtils.isEmpty(billInfo.getNumber())) {
                return billInfo;
            }
        }
        return null;
    }

    public BillInfo v(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("微信");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.contains(",支出") && str.contains("元")) {
                String strReplace = str.substring(str.indexOf(",支出") + 3, str.length() - 1).replace(",", "");
                String strSubstring = str.substring(0, str.indexOf(",支出"));
                y.l("numText:" + strReplace);
                y.l("remark:" + strSubstring);
                if (h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                    billInfo.setRemark(strSubstring);
                    billInfo.setShopName(strSubstring);
                    billInfo.setIncome(false);
                }
            } else if (str.contains(",收入") && str.contains("元")) {
                String strReplace2 = str.substring(str.indexOf(",收入") + 3, str.length() - 1).replace(",", "");
                String strSubstring2 = str.substring(0, str.indexOf(",收入"));
                y.l("numText:" + strReplace2);
                y.l("remark:" + strSubstring2);
                if (h2.J(strReplace2)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace2)) + "");
                    billInfo.setRemark(strSubstring2);
                    billInfo.setShopName(strSubstring2);
                    billInfo.setIncome(true);
                }
            } else if (str.contains("零钱通转出-到") && i6 < list.size() - 1) {
                if (!str.contains("支出¥")) {
                    str = list.get(i6 + 1);
                }
                if (str.contains("支出¥")) {
                    String strReplace3 = str.substring(str.indexOf("支出¥") + 3).replace(",", "");
                    if (h2.J(strReplace3)) {
                        billInfo.setNumber(Math.abs(h2.S(strReplace3)) + "");
                        billInfo.setRemark("零钱通转出");
                        billInfo.setTransfer(true);
                        billInfo.setFromAsset("零钱通");
                        billInfo.setToAsset("微信零钱");
                    }
                }
            } else if (str.contains("信用卡还款-") && i6 < list.size() - 1) {
                String str2 = list.get(i6 + 1);
                if (h2.J(str2)) {
                    billInfo.setNumber(Math.abs(h2.S(str2)) + "");
                    billInfo.setRemark("信用卡还款");
                    billInfo.setTransfer(true);
                    billInfo.setFromAsset("零钱通");
                    billInfo.setToAsset(str.replace("信用卡还款-", "").replace("还款", ""));
                }
            } else if (TextUtils.isEmpty(billInfo.getNumber()) && str.contains("收入¥")) {
                String strReplace4 = str.substring(str.indexOf("收入¥") + 3).replace(",", "");
                y.l("sssss", strReplace4);
                if (h2.J(strReplace4)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace4)) + "");
                    billInfo.setIncome(true);
                    billInfo.setRemark(str.substring(0, str.indexOf("收入¥")));
                    billInfo.setShopName(billInfo.getRemark());
                }
            } else if (TextUtils.isEmpty(billInfo.getNumber()) && (str.contains("-") || str.contains("+") || str.contains("¥"))) {
                String strReplace5 = str.replace("+", "").replace("-", "").replace("¥", "").replace(",", "");
                if (h2.J(strReplace5) && h2.S(strReplace5) > 0.0d) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace5)) + "");
                    if (i6 > 0) {
                        billInfo.setRemark(list.get(i6 - 1));
                        billInfo.setShopName(billInfo.getRemark());
                    }
                    if (str.contains("+")) {
                        billInfo.setIncome(true);
                    }
                }
            } else if ((str.equals("支付时间") || str.equals("转账时间") || str.equals("收款时间") || str.equals("到账时间") || str.equals("退款时间") || str.equals("交易时间")) && i6 < list.size() - 1) {
                if (!str.equals("收款时间") || billInfo.isIncome() || billInfo.getTime() == 0) {
                    int i7 = i6 + 1;
                    if (t0.X0(list.get(i7), "yyyy年MM月dd日 HH:mm:ss") != -1) {
                        billInfo.setTime(t0.X0(list.get(i7), "yyyy年MM月dd日 HH:mm:ss"));
                    } else if (t0.X0(list.get(i7), "yyyy-MM-dd HH:mm:ss") != -1) {
                        billInfo.setTime(t0.X0(list.get(i7), "yyyy-MM-dd HH:mm:ss"));
                    } else if (t0.X0(list.get(i7), "yyyy年MM月dd日 HH点mm分ss秒") != -1) {
                        billInfo.setTime(t0.X0(list.get(i7), "yyyy年MM月dd日 HH点mm分ss秒"));
                    }
                }
            } else if (str.contains("交易时间")) {
                if (t0.X0(str.replace("交易时间 ", ""), "yyyy年MM月dd日 HH点mm分ss秒") != -1) {
                    billInfo.setTime(t0.X0(str.replace("交易时间 ", ""), "yyyy年MM月dd日 HH点mm分ss秒"));
                }
            } else if (str.equals("优惠") && i6 < list.size() - 1) {
                String str3 = list.get(i6 + 1);
                if (str3.equals("支付成功") && i6 < list.size() - 2) {
                    str3 = list.get(i6 + 2);
                }
                if (str3.contains("￥")) {
                    String strSubstring3 = str3.substring(str3.indexOf("￥"));
                    if (!TextUtils.isEmpty(strSubstring3)) {
                        String strReplace6 = strSubstring3.replace(",", "").replace("￥", "");
                        if (h2.J(strReplace6)) {
                            billInfo.setDiscount(strReplace6);
                        }
                    }
                }
            } else if ((str.equals("支付方式") || str.equals("收款方式") || str.equals("退款方式")) && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1));
                if (billInfo.isTransfer()) {
                    if ("零钱".equals(billInfo.getAsset())) {
                        billInfo.setFromAsset("微信钱包");
                    } else {
                        billInfo.setFromAsset(billInfo.getAsset());
                    }
                }
            } else if ((str.equals("商品") || str.equals("商品名称") || str.equals("付款备注") || str.equals("转账说明") || str.equals("付款方留言") || str.equals("支付场景") || str.equals("商户简称")) && i6 < list.size() - 1) {
                billInfo.setRemark(list.get(i6 + 1));
            } else if (str.contains("收款账号") && billInfo.isTransfer()) {
                billInfo.setToAsset(str.replace("收款账号", "").trim());
            } else if (str.equals("收款方备注") && i6 < list.size() - 1) {
                String str4 = list.get(i6 + 1);
                if (!"二维码收款".equals(str4) || TextUtils.isEmpty(billInfo.getRemark())) {
                    billInfo.setRemark(str4);
                }
                if ("二维码收款".equals(str4) && TextUtils.isEmpty(billInfo.getAsset())) {
                    billInfo.setAsset("微信");
                }
            } else if (str.equals("已存入经营账户")) {
                billInfo.setAsset("微信经营账户");
            } else if (str.equals("已存入零钱")) {
                billInfo.setAsset("微信钱包");
            } else if (str.equals("优惠") && i6 < list.size() - 1) {
                String str5 = list.get(i6 + 1);
                if (str5.contains("¥")) {
                    String[] strArrSplit = str5.split("¥");
                    if (strArrSplit.length > 0) {
                        String strReplace7 = strArrSplit[strArrSplit.length - 1].replace("¥", "").replace(",", "");
                        if (h2.J(strReplace7)) {
                            billInfo.setDiscount(strReplace7);
                        }
                    }
                }
            }
        }
        if ("零钱".equals(billInfo.getAsset())) {
            billInfo.setAsset("微信钱包");
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    /* JADX WARN: Code duplicated, block: B:109:0x01d1 A[ADDED_TO_REGION, REMOVE, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:64:0x0187  */
    /* JADX WARN: Code duplicated, block: B:66:0x018f  */
    /* JADX WARN: Code duplicated, block: B:67:0x0194  */
    /* JADX WARN: Code duplicated, block: B:71:0x01a0  */
    /* JADX WARN: Code duplicated, block: B:73:0x01b6  */
    /* JADX WARN: Code duplicated, block: B:76:0x01c0  */
    /* JADX WARN: Code duplicated, block: B:78:0x01c8  */
    public BillInfo w(List<String> list, String str) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("微信");
        if (s.i()) {
            if (b2.m0(this.f13780j, System.currentTimeMillis())) {
                billInfo.setTime(this.f13780j);
            } else {
                billInfo.setTime(System.currentTimeMillis());
            }
        }
        boolean z5 = false;
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str2 = list.get(i6);
            String strReplace = str2.replace("￥", "").replace("¥", "").replace(",", "");
            if (("¥".equals(str2) || "￥".equals(str2)) && i6 < list.size() - 1) {
                String strReplace2 = list.get(i6 + 1).replace(",", "");
                if (h2.J(strReplace2)) {
                    strReplace = strReplace2;
                }
            }
            if ((z5 || TextUtils.isEmpty(billInfo.getNumber())) && h2.J(strReplace) && (str2.contains("￥") || str2.contains("¥"))) {
                billInfo.setNumber(strReplace);
                if (z5) {
                    billInfo.setRemark(list.get(i6 - 1));
                    billInfo.setShopName(billInfo.getRemark());
                }
            } else if (str2.equals("优惠") && i6 < list.size() - 1) {
                String str3 = list.get(i6 + 1);
                if (str3.contains("￥")) {
                    String strSubstring = str3.substring(str3.indexOf("￥"));
                    if (!TextUtils.isEmpty(strSubstring)) {
                        String strReplace3 = strSubstring.replace(",", "").replace("￥", "");
                        if (h2.J(strReplace3)) {
                            billInfo.setDiscount(strReplace3);
                        }
                    }
                }
            } else if (str2.equals("收款方") && i6 < list.size() - 1) {
                billInfo.setRemark(list.get(i6 + 1));
                billInfo.setShopName(billInfo.getRemark());
            } else if ((str2.equals("支付成功") || str2.equals("付款成功")) && i6 < list.size() - 2) {
                int i7 = i6 + 1;
                if (!list.get(i7).contains("¥")) {
                    billInfo.setRemark(list.get(i7));
                    billInfo.setShopName(billInfo.getRemark());
                    if (billInfo.getRemark().startsWith("待") && billInfo.getRemark().endsWith("确认收款")) {
                        billInfo.setRemark("转账给" + billInfo.getRemark().substring(1, billInfo.getRemark().length() - 4));
                    }
                } else if (str2.equals("保存收款码")) {
                    billInfo.setIncome(true);
                    z5 = true;
                } else if (str2.contains("￥") || str2.contains("¥")) {
                    String strReplace4 = str2.replace("￥", "").replace("¥", "").replace(",", "");
                    if (!TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace4)) {
                        billInfo.setNumber(strReplace4);
                    } else if (!list.contains("订单状态") && h2.J(strReplace4)) {
                        billInfo.setNumber(strReplace4);
                    }
                }
            } else if (str2.equals("保存收款码")) {
                billInfo.setIncome(true);
                z5 = true;
            } else if (str2.contains("￥")) {
                String strReplace5 = str2.replace("￥", "").replace("¥", "").replace(",", "");
                if (!TextUtils.isEmpty(billInfo.getNumber())) {
                    if (!list.contains("订单状态")) {
                    }
                } else if (!list.contains("订单状态")) {
                }
            } else {
                String strReplace6 = str2.replace("￥", "").replace("¥", "").replace(",", "");
                if (!TextUtils.isEmpty(billInfo.getNumber())) {
                    if (!list.contains("订单状态")) {
                    }
                } else if (!list.contains("订单状态")) {
                }
            }
        }
        if (!TextUtils.isEmpty(str)) {
            billInfo.setAsset(str);
        }
        if ("零钱".equals(billInfo.getAsset())) {
            billInfo.setAsset("微信钱包");
        }
        y.l("asset:" + str);
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo x(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("微信");
        billInfo.setAsset("微信");
        boolean z5 = false;
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if ((str.contains("已收款") || str.contains("你已收款")) && i6 < list.size() - 2) {
                String strReplace = list.get(i6 + 1).replace("￥", "").replace("¥", "").replace("元", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                }
                if (str.contains("你已收款")) {
                    z5 = true;
                }
            } else if (z5 && str.equals("收款时间") && i6 < list.size() - 1) {
                long jX0 = t0.X0(list.get(i6 + 1), "yyyy年MM月dd日 HH:mm:ss");
                if (jX0 != -1) {
                    billInfo.setTime(jX0);
                }
            } else if (!z5 && str.equals("转账时间") && i6 < list.size() - 1) {
                long jX1 = t0.X0(list.get(i6 + 1), "yyyy年MM月dd日 HH:mm:ss");
                if (jX1 != -1) {
                    billInfo.setTime(jX1);
                }
            } else if (str.equals("转账说明") && i6 < list.size() - 1) {
                billInfo.setRemark(list.get(i6 + 1));
            }
        }
        if (TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        if (TextUtils.isEmpty(billInfo.getRemark())) {
            if (z5) {
                billInfo.setRemark("微信收款");
            } else {
                billInfo.setRemark("微信转账");
            }
        }
        billInfo.setIncome(z5);
        billInfo.setShopName(billInfo.getRemark());
        return billInfo;
    }

    public BillInfo y(List<String> list, String str) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setTransfer(true);
        billInfo.setOrigin("微信");
        if (a(list, "充值成功", true)) {
            billInfo.setFromAsset(str);
            billInfo.setToAsset("微信钱包");
        } else if (a(list, "已转入", true)) {
            billInfo.setFromAsset("微信钱包");
            billInfo.setToAsset("零钱通");
        } else if (a(list, "已转出", true)) {
            billInfo.setFromAsset("零钱通");
            billInfo.setToAsset("微信钱包");
        } else if (a(list, "零钱提现", true)) {
            billInfo.setFromAsset("微信钱包");
            billInfo.setToAsset(str);
        } else if (a(list, "转出金额", true)) {
            billInfo.setFromAsset("零钱通");
            billInfo.setToAsset(str);
        }
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str2 = list.get(i6);
            if ((str2.equals("充值成功") || str2.equals("提现金额")) && i6 < list.size() - 1) {
                String strReplace = list.get(i6 + 1).replace("¥", "").replace("￥", "").replace(",", "");
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                }
            } else if (str2.equals("已转入") && i6 < list.size() - 2) {
                String strReplace2 = list.get(i6 + 2).replace("¥", "").replace("￥", "").replace(",", "");
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace2)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace2)) + "");
                }
            } else if (str2.equals("已转出") && i6 < list.size() - 2) {
                String strReplace3 = list.get(i6 + 2).replace("¥", "").replace("￥", "").replace(",", "");
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace3)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace3)) + "");
                }
            } else if (str2.equals("转出金额") && i6 < list.size() - 1) {
                String strReplace4 = list.get(i6 + 1).replace("¥", "").replace("￥", "").replace(",", "");
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace4)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace4)) + "");
                }
            } else if (str2.equals("服务费") && i6 < list.size() - 1) {
                String strReplace5 = list.get(i6 + 1).replace("¥", "").replace("￥", "").replace(",", "");
                if (h2.J(strReplace5)) {
                    billInfo.setServiceCharge(Math.abs(h2.S(strReplace5)));
                }
            }
        }
        if (TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo z(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setTransfer(true);
        billInfo.setOrigin("微信");
        if (a(list, "零钱提现", false)) {
            billInfo.setFromAsset("微信钱包");
        } else if (a(list, "零钱充值", false)) {
            billInfo.setToAsset("微信钱包");
        } else if (a(list, "转入零钱通", false)) {
            billInfo.setToAsset("零钱通");
        }
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.equals("提现金额") && i6 < list.size() - 1) {
                String strReplace = list.get(i6 + 1).replace("¥", "").replace("￥", "").replace(",", "");
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                }
            } else if (str.equals("服务费") && i6 < list.size() - 1) {
                String strReplace2 = list.get(i6 + 1).replace("¥", "").replace("￥", "").replace(",", "");
                if (h2.J(strReplace2)) {
                    billInfo.setServiceCharge(Math.abs(h2.S(strReplace2)));
                }
            } else if (TextUtils.isEmpty(billInfo.getNumber()) && str.contains("收入¥")) {
                String strReplace3 = str.substring(str.indexOf("收入¥") + 3).replace(",", "");
                if (h2.J(strReplace3)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace3)) + "");
                    billInfo.setIncome(true);
                    billInfo.setRemark(str.substring(0, str.indexOf("收入¥")));
                    billInfo.setShopName(billInfo.getRemark());
                }
            } else if (TextUtils.isEmpty(billInfo.getNumber()) && i6 < list.size() - 1 && "当前状态".equals(list.get(i6 + 1))) {
                String strReplace4 = str.replace(",", "");
                if (h2.J(strReplace4)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace4)) + "");
                    if (i6 > 0) {
                        billInfo.setRemark(list.get(i6 - 1));
                        billInfo.setShopName(billInfo.getRemark());
                    }
                }
            } else if (str.contains("转入方式")) {
                billInfo.setFromAsset(str.replace("转入方式", "").trim());
            } else if (str.equals("提现银行") && i6 < list.size() - 1) {
                billInfo.setToAsset(list.get(i6 + 1));
            } else if (str.equals("支付方式") && i6 < list.size() - 1) {
                billInfo.setFromAsset(list.get(i6 + 1));
            } else if ((str.equals("申请时间") || str.equals("到账时间") || str.equals("充值时间") || str.equals("支付时间")) && i6 < list.size() - 1) {
                int i7 = i6 + 1;
                billInfo.setTime(t0.X0(list.get(i7), "yyyy年MM月dd日 HH:mm:ss"));
                if (billInfo.getTime() == -1) {
                    billInfo.setTime(t0.X0(list.get(i7), "yyyy-MM-dd HH:mm:ss"));
                }
            } else if (str.contains("交易时间")) {
                if (t0.X0(str.replace("交易时间 ", ""), "yyyy年MM月dd日 HH点mm分ss秒") != -1) {
                    billInfo.setTime(t0.X0(str.replace("交易时间 ", ""), "yyyy年MM月dd日 HH点mm分ss秒"));
                }
            } else if (str.contains(",") && str.contains("元")) {
                String strSubstring = str.substring(str.lastIndexOf(",") + 1, str.length() - 1);
                if (h2.J(strSubstring)) {
                    billInfo.setNumber(Math.abs(h2.S(strSubstring)) + "");
                }
            } else if (str.contains("零钱充值-") && i6 < list.size() - 1) {
                String strReplace5 = list.get(i6 + 1).replace("元", "").replace(",", "");
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace5)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace5)) + "");
                }
            } else if (str.contains("转入零钱通-") && i6 < list.size() - 1) {
                String strReplace6 = list.get(i6 + 1).replace("元", "").replace(",", "");
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace6)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace6)) + "");
                }
            }
        }
        if ("零钱".equals(billInfo.getAsset())) {
            billInfo.setAsset("微信钱包");
        }
        if (TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }
}
