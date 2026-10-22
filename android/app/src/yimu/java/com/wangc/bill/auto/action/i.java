// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 c43814ca081f237a264b1f93f3a1b2fb90e9da5271d14bdcb0c9f9ec04de585c
package com.wangc.bill.auto.action;

import static com.wangc.bill.reference.RecoveredValues.*;

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

    /** DEX-exact branch transcription of v; original register and block addresses retained. */
    public com.wangc.bill.http.entity.BillInfo v(java.util.List arg0, java.lang.String arg1, java.lang.String arg2) {
        Object[] v=new Object[20]; Object[] result=new Object[2]; int pc=0;
        v[16]=this;
        v[17]=arg0;
        v[18]=arg1;
        v[19]=arg2;
        while(true) {
            if(pc==-1) { return ((com.wangc.bill.http.entity.BillInfo)dexR(result[1])); }
            if(pc<0x158) pc=recovered_v_block0(v,result,pc);
            else pc=recovered_v_block1(v,result,pc);
        }
    }
    private int recovered_v_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                v[0]=v[16];
                v[1]=v[17];
                result[0]=dexN((((java.util.List)dexR(v[17])).isEmpty())?1:0);
                v[2]=result[0];
                v[3]=dexN(0L);
                pc=(dexZero(v[2]))?0xc:0xb; continue;
            }
            case 0xb: {
                result[1]=v[3]; return -1;
            }
            case 0xc: {
                v[2]=new DexReference(); // new com.wangc.bill.http.entity.BillInfo
                ((DexReference)v[2]).value=new com.wangc.bill.http.entity.BillInfo();
                v[4]="京东商城";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setOrigin(((java.lang.String)dexR(v[4])));
                v[5]="元";
                v[6]=dexN(0L);
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).d(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[5])),dexZ(v[6])));
                v[7]=result[0];
                v[8]="微信支付";
                v[9]="白条付款";
                v[10]="数字人民币";
                v[11]=",";
                v[12]="¥";
                v[13]=dexN(1L);
                v[14]="";
                pc=(dexI(v[7])<=0)?0x9a:0x2c; continue;
            }
            case 0x2c: {
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[7]));
                v[15]=result[0];
                v[15]=((java.lang.String)dexR(v[15]));
                v[3]="实付";
                result[0]=((java.lang.String)dexR(v[15])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[11])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).trim();
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x4d:0x4a; continue;
            }
            case 0x4a: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[3])));
                pc=0x4d; continue;
            }
            case 0x4d: {
                result[0]=dexN(((java.util.List)dexR(v[17])).size());
                v[3]=result[0];
                v[3]=dexN(dexI(v[3])-dexI(v[13]));
                pc=(dexI(v[7])>=dexI(v[3]))?0x9a:0x54; continue;
            }
            case 0x54: {
                v[7]=dexN(dexI(v[7])+dexI(v[13]));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[7]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[5]="已优惠";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x9a:0x63; continue;
            }
            case 0x63: {
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                v[7]="京东支付";
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[7])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                v[7]="在线支付";
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[7])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                v[5]="云闪付";
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[12])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[11])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).trim();
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x9a:0x97; continue;
            }
            case 0x97: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setDiscount(((java.lang.String)dexR(v[3])));
                pc=0x9a; continue;
            }
            case 0x9a: {
                v[3]="支付成功";
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).d(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[3])),dexZ(v[6])));
                v[3]=result[0];
                pc=(dexI(v[3])<0)?0x126:0xa2; continue;
            }
            case 0xa2: {
                result[0]=dexN(((java.util.List)dexR(v[17])).size());
                v[5]=result[0];
                v[5]=dexN(dexI(v[5])-dexI(v[13]));
                pc=(dexI(v[3])>=dexI(v[5]))?0x126:0xa9; continue;
            }
            case 0xa9: {
                v[3]=dexN(dexI(v[3])+dexI(v[13]));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[3]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[12]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x126:0xb6; continue;
            }
            case 0xb6: {
                v[3]="，";
                result[0]=((java.lang.String)dexR(v[1])).split(((java.lang.String)dexR(v[3])));
                v[1]=result[0];
                v[3]=((Object[])dexR(v[1]))[dexI(v[6])];
                result[0]=dexN(((java.lang.String)dexR(v[3])).indexOf(((java.lang.String)dexR(v[12]))));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).substring(dexI(v[5]));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[12])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[11])),((java.lang.CharSequence)dexR(v[14])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).trim();
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0xdb:0xd8; continue;
            }
            case 0xd8: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[3])));
                pc=0xdb; continue;
            }
            case 0xdb: {
                v[3]=((Object[])dexR(v[1]))[dexI(v[6])];
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[9]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0xe6:0xe3; continue;
            }
            case 0xe3: {
                v[3]="白条";
                pc=0xfd; continue;
            }
            case 0xe6: {
                v[3]=((Object[])dexR(v[1]))[dexI(v[6])];
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[10]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0xf0:0xee; continue;
            }
            case 0xee: {
                v[3]=v[10];
                pc=0xfd; continue;
            }
            case 0xf0: {
                v[3]=((Object[])dexR(v[1]))[dexI(v[6])];
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[8]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0xfb:0xf8; continue;
            }
            case 0xf8: {
                v[3]="微信钱包";
                pc=0xfd; continue;
            }
            case 0xfb: {
                v[3]=v[18];
                pc=0xfd; continue;
            }
            case 0xfd: {
                v[5]=dexN(dexLength(dexR(v[1])));
                pc=(dexI(v[5])<=dexI(v[13]))?0x128:0x100; continue;
            }
            case 0x100: {
                v[5]=((Object[])dexR(v[1]))[dexI(v[13])];
                v[6]="共优惠";
                result[0]=dexN((((java.lang.String)dexR(v[5])).contains(((java.lang.CharSequence)dexR(v[6]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x128:0x10a; continue;
            }
            case 0x10a: {
                v[1]=((Object[])dexR(v[1]))[dexI(v[13])];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[14])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[12])),((java.lang.CharSequence)dexR(v[14])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[11])),((java.lang.CharSequence)dexR(v[14])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).trim();
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x128:0x122; continue;
            }
            case 0x122: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setDiscount(((java.lang.String)dexR(v[1])));
                pc=0x128; continue;
            }
            case 0x126: {
                v[3]=v[18];
                pc=0x128; continue;
            }
            case 0x128: {
                v[1]="京东购物";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setRemark(((java.lang.String)dexR(v[1])));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setShopName(((java.lang.String)dexR(v[4])));
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x139:0x136; continue;
            }
            case 0x136: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setAsset(((java.lang.String)dexR(v[3])));
                pc=0x139; continue;
            }
            case 0x139: {
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[19]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x144:0x13f; continue;
            }
            case 0x13f: {
                v[1]=v[19];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x144; continue;
            }
            case 0x144: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getRemark();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x159:0x14e; continue;
            }
            case 0x14e: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getNumber();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x159:0x158; continue;
            }
            default: return pc;
        }
    }
    private int recovered_v_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x158: {
                result[1]=v[2]; return -1;
            }
            case 0x159: {
                v[1]=dexN(0L);
                result[1]=v[1]; return -1;
            }
            default: return pc;
        }
    }
}
