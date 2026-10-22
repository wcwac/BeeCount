// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 c43814ca081f237a264b1f93f3a1b2fb90e9da5271d14bdcb0c9f9ec04de585c
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class i extends c implements x1.a {

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    private static final int f13709k = 7;

    /* JADX INFO: renamed from: l, reason: collision with root package name */
    private static final int f13710l = 8;

    /* JADX INFO: renamed from: m, reason: collision with root package name */
    private static final int f13711m = 9;

    /* JADX INFO: renamed from: n, reason: collision with root package name */
    private static final int f13712n = 10;

    /* JADX INFO: renamed from: o, reason: collision with root package name */
    private static i f13713o;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private String f13714e;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13715f;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private final List<String> f13716g = Arrays.asList("支付方式", "创建时间");

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private final List<String> f13717h = Arrays.asList("交易成功", "退款成功", "运输中", "查看订单状态");

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private boolean f13718i = false;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private boolean f13719j = false;

    private void w(Context context, List<String> list, HashMap<String, Boolean> map) {
        BillInfo billInfoV;
        int iIndexOf;
        a2.a("start find node");
        y.l("sssss", "start find node");
        if (list == null || list.isEmpty()) {
            return;
        }
        int i6 = this.a;
        boolean z5 = true;
        boolean zContainsKey = i6 == 7 || i6 == 10;
        if (!zContainsKey && i6 == 8) {
            zContainsKey = map.containsKey("支付成功");
        }
        if (!zContainsKey && this.a == 9) {
            if (!map.containsKey("支付成功") && !map.containsKey("付款成功")) {
                z5 = false;
            }
            zContainsKey = z5;
        }
        if (this.a == 8 && map.containsKey("¥") && (iIndexOf = list.indexOf("¥")) < list.size() - 2) {
            this.f13714e = list.get(iIndexOf + 2);
        }
        if (zContainsKey) {
            a2.a("start check pay:" + this.a);
            int i7 = this.a;
            if (i7 == 7) {
                billInfoV = q(list);
            } else if (i7 == 10) {
                billInfoV = r(list);
            } else if (i7 == 8) {
                billInfoV = t(list, this.f13714e);
            } else {
                billInfoV = i7 == 9 ? v(list, this.f13714e, this.f13715f) : null;
            }
            if (billInfoV != null) {
                this.b = false;
                this.f13714e = null;
                this.f13715f = null;
                this.f13719j = false;
                this.f13718i = false;
                billInfoV.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                SelectToSpeakService.l(context, billInfoV, this);
            }
        }
    }

    public static i x() {
        if (f13713o == null) {
            f13713o = new i();
        }
        return f13713o;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
        this.b = false;
        this.f13714e = null;
        this.f13715f = null;
    }

    public BillInfo q(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("京东金融");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String strTrim = list.get(i6).trim();
            if ((strTrim.equals("交易成功") || strTrim.equals("退款成功") || strTrim.equals("运输中") || strTrim.contains("已退款") || strTrim.contains("查看订单状态")) && i6 > 1) {
                int i7 = i6 - 1;
                String strReplace = list.get(i7).replace("+", "").replace("-", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                    if (list.get(i7).contains("+")) {
                        billInfo.setIncome(true);
                    }
                }
                String str = list.get(i6 - 2);
                billInfo.setRemark(str);
                billInfo.setShopName(str);
            } else if ((strTrim.equals("交易说明：") || strTrim.equals("退款说明：") || strTrim.equals("交易说明") || strTrim.equals("退款说明")) && i6 < list.size() - 1) {
                billInfo.setRemark(list.get(i6 + 1));
            } else if ((strTrim.equals("支付方式：") || strTrim.equals("退款至：") || strTrim.equals("支付方式") || strTrim.equals("退款至")) && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1));
            } else if ((strTrim.equals("创建时间：") || strTrim.equals("创建时间")) && i6 < list.size() - 1) {
                int i8 = i6 + 1;
                billInfo.setTime(t0.X0(list.get(i8), "yyyy-MM-dd HH:mm:ss"));
                if (billInfo.getTime() == -1 && i6 < list.size() - 2) {
                    billInfo.setTime(t0.X0(list.get(i8) + " " + list.get(i6 + 2), "yyyy-MM-dd HH:mm:ss"));
                }
                if (billInfo.getTime() == -1) {
                    billInfo.setTime(t0.X0(list.get(i8), "yyyy-MM-dd"));
                }
            } else if (strTrim.equals("支付立减") && i6 < list.size() - 1) {
                String strReplace2 = list.get(i6 + 1).replace("+", "").replace("-", "").replace(",", "");
                if (h2.J(strReplace2)) {
                    billInfo.setDiscount(strReplace2);
                }
            } else if ((strTrim.equals("完成") || strTrim.equals("等待收货")) && i6 > 5) {
                String str2 = list.get(i6 - 1);
                if (str2.startsWith("共") && str2.endsWith("件")) {
                    billInfo.setRemark(list.get(i6 - 2));
                }
            } else if (strTrim.equals("服务详情")) {
                String str3 = list.get(i6 + 1);
                if (str3.startsWith("共") && str3.endsWith("笔订单") && i6 < list.size() - 2) {
                    StringBuilder sb = new StringBuilder();
                    for (int i9 = i6 + 2; i9 < list.size() - 1; i9++) {
                        sb.append(list.get(i9));
                    }
                    billInfo.setRemark(sb.toString());
                }
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark())) {
            billInfo.setRemark(billInfo.getShopName());
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo r(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("京东金融");
        billInfo.setTransfer(true);
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.equals("还款成功") && i6 > 1) {
                String strReplace = list.get(i6 - 1).replace("+", "").replace("-", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                    String str2 = list.get(i6 - 2);
                    billInfo.setRemark(str2);
                    billInfo.setShopName(str2);
                    if (str2.equals("京东白条")) {
                        billInfo.setToAsset("京东白条");
                    }
                }
            } else if ((str.equals("交易说明：") || str.equals("交易说明")) && i6 < list.size() - 1) {
                billInfo.setRemark(list.get(i6 + 1));
            } else if ((str.equals("支付方式：") || str.equals("支付方式")) && i6 < list.size() - 1) {
                billInfo.setFromAsset(list.get(i6 + 1));
            } else if ((str.equals("创建时间：") || str.equals("创建时间")) && i6 < list.size() - 1) {
                billInfo.setTime(t0.X0(list.get(i6 + 1), "yyyy-MM-dd HH:mm:ss"));
            } else if (str.equals("支付立减") && i6 < list.size() - 1) {
                String strReplace2 = list.get(i6 + 1).replace("+", "").replace(",", "");
                if (h2.J(strReplace2)) {
                    billInfo.setServiceCharge(h2.S(strReplace2));
                }
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public void s(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        HashMap<String, Boolean> map;
        List<String> listG = null;
        if (str.equals("com.jd.jrapp.bm.mainbox.main.MainActivity")) {
            this.a = 0;
            this.f13715f = null;
            this.b = false;
            this.f13719j = false;
        } else if (str.equals("com.jd.jrapp.bm.jrv8.JRCustomDyPageActivity")) {
            this.f13719j = true;
        }
        if (!this.f13719j || accessibilityNodeInfo == null) {
            map = null;
        } else {
            listG = g(accessibilityNodeInfo);
            map = new HashMap<>();
            Iterator<String> it = listG.iterator();
            while (it.hasNext()) {
                map.put(it.next().trim(), Boolean.TRUE);
            }
            if (b(map, this.f13716g)) {
                if (b(map, this.f13717h) || a(listG, "已退款", false)) {
                    this.a = 7;
                    this.b = true;
                } else if (map.containsKey("还款成功")) {
                    this.a = 10;
                    this.b = true;
                }
            } else if ((map.containsKey("向京东商城支付") && map.containsKey("京东支付")) || map.containsKey("支付成功")) {
                this.a = 8;
                this.b = true;
            } else if (listG.size() < 4) {
                for (int i6 = 0; i6 < listG.size(); i6++) {
                    String[] strArrSplit = listG.get(i6).split(" ");
                    if (strArrSplit.length > 10) {
                        listG.addAll(Arrays.asList(strArrSplit));
                        this.a = 7;
                        this.b = true;
                        break;
                    }
                }
            }
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        if (listG == null) {
            listG = g(accessibilityNodeInfo);
            map = new HashMap<>();
            Iterator<String> it2 = listG.iterator();
            while (it2.hasNext()) {
                map.put(it2.next().trim(), Boolean.TRUE);
            }
        }
        w(context, listG, map);
    }

    public BillInfo t(List<String> list, String str) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("京东支付");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str2 = list.get(i6);
            if (str2.equals("元") && i6 > 0) {
                String strTrim = list.get(i6 - 1).replace(",", "").trim();
                if (h2.J(strTrim)) {
                    billInfo.setNumber(strTrim);
                }
            } else if (str2.equals("收款方") && i6 < list.size() - 1) {
                billInfo.setRemark(list.get(i6 + 1));
                billInfo.setShopName(billInfo.getRemark());
            }
        }
        if (!TextUtils.isEmpty(str)) {
            billInfo.setAsset(str);
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public void u(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        HashMap<String, Boolean> map;
        int iD;
        int iD2;
        str.hashCode();
        List<String> listG = null;
        switch (str) {
            case "com.jd.lib.search.view.Activity.ProductListActivity":
            case "com.jingdong.app.mall.MainFrameActivity":
            case "com.jd.lib.productdetail.ProductDetailActivity":
            case "com.jd.lib.cart.ShoppingCartNewActivity":
                this.a = 0;
                this.b = false;
                this.f13718i = false;
                this.f13719j = false;
                this.f13715f = null;
                break;
            case "com.jingdong.common.jdreactFramework.activities.JDReactNativeCommonActivity":
            case "com.wangyin.payment.jdpaysdk.counter.ui.pay.CounterActivity":
            case "com.jingdong.app.mall.bundle.cashierfinish.view.CashierUserContentCompletePopActivity":
            case "com.jd.lib.settlement.fillorder.activity.PopupNewFillOrderActivity":
            case "com.jd.lib.cashier.complete.view.CashierCompleteActivity":
            case "com.jd.lib.jdpaysdk.JDPayActivity":
            case "com.jd.lib.settlement.fillorder.activity.NewFillOrderActivity":
            case "com.jd.lib.cashier.sdk.pay.view.CashierPayActivity":
                this.f13718i = true;
                break;
            case "com.jingdong.app.mall.WebActivity":
                this.f13719j = true;
                break;
        }
        if (accessibilityNodeInfo == null || !this.f13718i) {
            map = null;
        } else {
            listG = g(accessibilityNodeInfo);
            map = new HashMap<>();
            Iterator<String> it = listG.iterator();
            while (it.hasNext()) {
                map.put(it.next(), Boolean.TRUE);
            }
            if (str.equals("com.jd.lib.cashier.complete.view.CashierCompleteActivity") || str.equals("com.jd.lib.cashier.sdk.complete.view.CashierCompleteActivity") || ((map.containsKey("付款成功") && a(listG, "实付", false)) || (map.containsKey("支付成功") && map.containsKey("查看订单")))) {
                this.a = 9;
                this.b = true;
            } else if (map.containsKey("单单免费做公益")) {
                int iIndexOf = listG.indexOf("单单免费做公益");
                if (iIndexOf < listG.size() - 1) {
                    String str2 = listG.get(iIndexOf + 1);
                    this.f13714e = str2;
                    if (!TextUtils.isEmpty(str2) && this.f13714e.contains("白条")) {
                        this.f13714e = "白条";
                    }
                    y.l("sssss", "asset:" + this.f13714e);
                }
            } else if (map.containsKey("单单做公益")) {
                int iIndexOf2 = listG.indexOf("单单做公益");
                if (iIndexOf2 < listG.size() - 1) {
                    String str3 = listG.get(iIndexOf2 + 1);
                    this.f13714e = str3;
                    if (!TextUtils.isEmpty(str3) && this.f13714e.contains("白条")) {
                        this.f13714e = "白条";
                    }
                    y.l("sssss", "asset:" + this.f13714e);
                }
            } else if (map.containsKey("填写订单") || map.containsKey("确认订单") || (map.containsKey("自己买") && map.containsKey("送朋友"))) {
                if (map.containsKey("商品名称")) {
                    int iIndexOf3 = listG.indexOf("商品名称");
                    if (iIndexOf3 < listG.size() - 1) {
                        this.f13715f = listG.get(iIndexOf3 + 1);
                        y.l("sssss", "remark:" + this.f13715f);
                    }
                } else if (TextUtils.isEmpty(this.f13715f) && (iD = d(listG, "¥", false)) > 0) {
                    String str4 = listG.get(iD - 1);
                    if (!"送朋友".equals(str4)) {
                        if (iD > 1) {
                            String str5 = listG.get(iD - 2);
                            if (!"京东自营".equals(str5)) {
                                this.f13715f = str5 + " " + str4;
                            }
                        } else {
                            this.f13715f = str4;
                        }
                        y.l("sssss", "remark:" + this.f13715f);
                    }
                }
            } else if (map.containsKey("列表")) {
                int iD3 = d(listG, "%用户选择", false);
                if (iD3 > 0) {
                    this.f13715f = listG.get(iD3 - 1);
                    y.l("sssss", "remark:" + this.f13715f);
                } else if (TextUtils.isEmpty(this.f13715f) && (iD2 = d(listG, "列表", true)) > 0 && iD2 < listG.size() - 1) {
                    this.f13715f = listG.get(iD2 + 1);
                    y.l("sssss", "remark:" + this.f13715f);
                }
            } else if (map.containsKey("打包费") || map.containsKey("送达备注")) {
                int iD4 = d(listG, "¥", false);
                if (iD4 > 0) {
                    this.f13715f = listG.get(iD4 - 1);
                }
                y.l("sssss", "remark:" + this.f13715f);
            }
        }
        if (accessibilityNodeInfo != null && this.f13719j) {
            listG = g(accessibilityNodeInfo);
            map = new HashMap<>();
            Iterator<String> it2 = listG.iterator();
            while (it2.hasNext()) {
                map.put(it2.next(), Boolean.TRUE);
            }
            if (b(map, this.f13716g)) {
                if (b(map, this.f13717h) || a(listG, "已退款", false)) {
                    this.a = 7;
                    this.b = true;
                } else if (map.containsKey("还款成功")) {
                    this.a = 10;
                    this.b = true;
                }
            } else if (listG.size() < 4) {
                for (int i6 = 0; i6 < listG.size(); i6++) {
                    String[] strArrSplit = listG.get(i6).split(" ");
                    if (strArrSplit.length > 10) {
                        listG.addAll(Arrays.asList(strArrSplit));
                        this.a = 7;
                        this.b = true;
                    }
                }
            }
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        if (listG == null) {
            listG = g(accessibilityNodeInfo);
            map = new HashMap<>();
            Iterator<String> it3 = listG.iterator();
            while (it3.hasNext()) {
                map.put(it3.next(), Boolean.TRUE);
            }
        }
        w(context, listG, map);
    }

    /* JADX WARN: Code duplicated, block: B:43:0x0126  */
    public BillInfo v(List<String> list, String str, String str2) {
        String str3;
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("京东商城");
        int iD = d(list, "元", false);
        if (iD > 0) {
            String strTrim = list.get(iD).replace("实付", "").replace("元", "").replace(",", "").trim();
            if (h2.J(strTrim)) {
                billInfo.setNumber(strTrim);
            }
            if (iD < list.size() - 1) {
                String str4 = list.get(iD + 1);
                if (str4.contains("已优惠")) {
                    String strTrim2 = str4.replace("白条付款", "").replace("京东支付", "").replace("在线支付", "").replace("已优惠", "").replace("微信支付", "").replace("数字人民币", "").replace("云闪付", "").replace("¥", "").replace(",", "").trim();
                    if (h2.J(strTrim2)) {
                        billInfo.setDiscount(strTrim2);
                    }
                }
            }
        }
        int iD2 = d(list, "支付成功", false);
        if (iD2 < 0 || iD2 >= list.size() - 1) {
            str3 = str;
        } else {
            String str5 = list.get(iD2 + 1);
            if (str5.contains("¥")) {
                String[] strArrSplit = str5.split("，");
                String str6 = strArrSplit[0];
                String strTrim3 = str6.substring(str6.indexOf("¥")).replace("¥", "").replace(",", "").trim();
                if (h2.J(strTrim3)) {
                    billInfo.setNumber(strTrim3);
                }
                if (strArrSplit[0].contains("白条付款")) {
                    str3 = "白条";
                } else if (strArrSplit[0].contains("数字人民币")) {
                    str3 = "数字人民币";
                } else {
                    str3 = strArrSplit[0].contains("微信支付") ? "微信钱包" : str;
                }
                if (strArrSplit.length > 1 && strArrSplit[1].contains("共优惠")) {
                    String strTrim4 = strArrSplit[1].replace("共优惠", "").replace("¥", "").replace(",", "").trim();
                    if (h2.J(strTrim4)) {
                        billInfo.setDiscount(strTrim4);
                    }
                }
            } else {
                str3 = str;
            }
        }
        billInfo.setRemark("京东购物");
        billInfo.setShopName("京东商城");
        if (!TextUtils.isEmpty(str3)) {
            billInfo.setAsset(str3);
        }
        if (!TextUtils.isEmpty(str2)) {
            billInfo.setRemark(str2);
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }
}
