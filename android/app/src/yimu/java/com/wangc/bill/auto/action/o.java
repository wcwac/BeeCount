// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 f2c99ce204cc28fc9132ac4abab076d1162586dd4c686ef86555ab6ad725d97d
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
public class o extends c implements x1.a {

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private static final int f13762h = 4;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private static final int f13763i = 5;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private static final int f13764j = 12;

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    private static o f13765k;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    String f13766e = null;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    String f13767f = null;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private boolean f13768g = false;

    private void r(Context context, List<String> list, HashMap<String, Boolean> map) {
        BillInfo billInfoX;
        int iIndexOf;
        int iIndexOf2;
        a2.a("start find node");
        s6.b.a("start find node");
        if (list == null || list.isEmpty()) {
            return;
        }
        boolean zContainsKey = map.containsKey("支付成功");
        if (this.a == 12) {
            if (map.containsKey("付款卡") && (iIndexOf2 = list.indexOf("付款卡")) < list.size() - 1) {
                this.f13767f = list.get(iIndexOf2 + 1);
            }
            if (map.containsKey("收款卡") && (iIndexOf = list.indexOf("收款卡")) < list.size() - 1) {
                this.f13766e = list.get(iIndexOf + 1);
            }
        }
        if (!zContainsKey && this.a == 12) {
            zContainsKey = true;
        }
        if (!zContainsKey && this.a == 5) {
            zContainsKey = true;
        }
        if (zContainsKey) {
            a2.a("start check pay:" + this.a);
            int i6 = this.a;
            if (i6 == 4) {
                billInfoX = w(list);
            } else if (i6 != 5) {
                billInfoX = i6 == 12 ? x(list, this.f13767f, this.f13766e) : null;
            } else if (a(list, "信用卡号", true) && a(list, "还款:", false)) {
                billInfoX = x(list, null, null);
            } else if (a(list, "订单状态", true) || !a(list, "订单状态", false)) {
                billInfoX = (a(list, "当前状态", true) && (a(list, "订单时间", true) || a(list, "扣款时间", true) || a(list, "退款时间", true))) ? v(list) : t(list);
            } else {
                billInfoX = u(list);
            }
            if (billInfoX != null) {
                this.f13767f = null;
                this.f13766e = null;
                this.b = false;
                this.f13768g = false;
                billInfoX.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                SelectToSpeakService.l(context, billInfoX, this);
            }
        }
    }

    public static o y() {
        if (f13765k == null) {
            f13765k = new o();
        }
        return f13765k;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
        this.f13767f = null;
        this.f13766e = null;
        this.b = false;
    }

    public boolean q(String str, String str2) {
        return str.equals("com.unionpay") && (str2.equals("com.unionpay.activity.UPActivityMain") || str2.equals("com.unionpay.activity.payment.UPActivityScan") || str2.equals("com.unionpay.activity.payment.UPActivityPaymentQrCodeOut"));
    }

    public void s(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        List<String> listG;
        HashMap<String, Boolean> map;
        if (str.equals("com.unionpay.activity.UPActivityMain")) {
            this.a = 0;
            this.f13768g = false;
            this.b = false;
        } else {
            if (!str.equals("com.unionpay.activity.payment.UPActivityScan") && !str.equals("com.unionpay.activity.payment.UPActivityPaymentQrCodeOut")) {
                if (accessibilityNodeInfo != null) {
                    y.l("sssss", "start get data");
                    listG = g(accessibilityNodeInfo);
                    map = new HashMap<>();
                    Iterator<String> it = listG.iterator();
                    while (it.hasNext()) {
                        map.put(it.next(), Boolean.TRUE);
                    }
                    if (map.containsKey("支付成功") && map.containsKey("付款方式")) {
                        this.a = 4;
                        this.b = true;
                    } else if (map.containsKey("账单详情") && map.containsKey("转给本人") && map.containsKey("转账金额")) {
                        this.a = 12;
                        this.b = true;
                    } else if (map.containsKey("实收金额（元）") || map.containsKey("实付金额（元）") || ((map.containsKey("当前状态") && map.containsKey("订单时间")) || ((map.containsKey("当前状态") && map.containsKey("扣款时间")) || ((map.containsKey("交易详情") && map.containsKey("时间")) || ((map.containsKey("交易详情") && map.containsKey("交易时间")) || (map.containsKey("当前状态") && map.containsKey("退款时间"))))))) {
                        this.a = 5;
                        this.b = true;
                    } else if ((map.containsKey("转账成功") && map.containsKey("完成")) || (map.containsKey("账单详情") && map.containsKey("转出成功"))) {
                        this.a = 12;
                        this.b = true;
                    } else if (map.containsKey("本次还款金额") && map.containsKey("本期账单") && map.containsKey("立即还") && map.containsKey("分期还")) {
                        int iF = f(listG, "[", false);
                        if (iF >= 0) {
                            this.f13766e = listG.get(iF);
                            this.f13768g = true;
                        }
                        y.l("sssss", Boolean.valueOf(this.f13768g), this.f13766e);
                    }
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
                }
                r(context, listG, map);
                return;
            }
            this.a = 4;
            this.b = true;
        }
        listG = null;
        map = null;
        if (this.b) {
        }
    }

    public BillInfo t(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("云闪付");
        boolean z5 = false;
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if ((str.equals("支出金额") || str.equals("订单金额")) && i6 < list.size() - 1) {
                String strReplace = list.get(i6 + 1).replace("元", "").replace(",", "");
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                }
            } else if ((str.equals("实付金额（元）") || str.equals("实收金额（元）")) && i6 < list.size() - 1) {
                String strReplace2 = list.get(i6 + 1).replace("元", "").replace(",", "");
                if (h2.J(strReplace2)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace2)) + "");
                }
            } else if (str.equals("收入金额") && i6 < list.size() - 1) {
                String strReplace3 = list.get(i6 + 1).replace("元", "").replace(",", "");
                if (h2.J(strReplace3)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace3)) + "");
                }
                billInfo.setIncome(true);
                z5 = true;
            } else if (str.equals("交易详情") && i6 < list.size() - 2) {
                String strReplace4 = list.get(i6 + 2).replace("¥", "").replace(",", "").replace("+", "");
                if (h2.J(strReplace4)) {
                    double dS = h2.S(strReplace4);
                    billInfo.setNumber(Math.abs(dS) + "");
                    if (dS > 0.0d) {
                        billInfo.setIncome(true);
                        z5 = true;
                    }
                }
                int i7 = i6 + 1;
                billInfo.setRemark(list.get(i7));
                billInfo.setShopName(list.get(i7));
            } else if ((str.equals("商户名称") || str.equals("乘车线路")) && i6 < list.size() - 1) {
                billInfo.setShopName(list.get(i6 + 1));
            } else if (str.equals("交易类型") && TextUtils.isEmpty(billInfo.getShopName()) && i6 < list.size() - 1) {
                billInfo.setShopName(list.get(i6 + 1));
            } else if (str.equals("卡号") && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1).replace("尾号", ""));
                if (i6 < list.size() - 2) {
                    int i8 = i6 + 2;
                    if (list.get(i8).contains("****")) {
                        billInfo.setAsset(billInfo.getAsset() + list.get(i8).substring(list.get(i8).indexOf("****") + 4));
                    }
                }
            } else if (str.equals("付款卡") && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1));
            } else if (str.equals("收款卡") && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1));
            } else if (str.equals("付款卡号") && i6 < list.size() - 1) {
                String str2 = list.get(i6 + 1);
                billInfo.setAsset(str2.substring(str2.length() - 4));
                y.l(billInfo.getAsset());
            } else if ((str.equals("时间") || str.equals("订单时间") || str.equals("扣款时间") || str.equals("交易时间")) && i6 < list.size() - 1) {
                int i9 = i6 + 1;
                billInfo.setTime(t0.X0(list.get(i9), "yyyy-MM-dd HH:mm"));
                if (billInfo.getTime() == -1 || billInfo.getTime() == 0) {
                    billInfo.setTime(t0.X0(list.get(i9), "yyyy-MM-dd HH:mm:ss"));
                }
            } else if (str.equals("分类") && i6 < list.size() - 1) {
                billInfo.setRemark(list.get(i6 + 1));
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark())) {
            if (TextUtils.isEmpty(billInfo.getShopName())) {
                billInfo.setRemark(billInfo.getOrigin());
            } else {
                billInfo.setRemark(billInfo.getShopName());
            }
        }
        if (z5) {
            billInfo.setShopName("收入-" + billInfo.getShopName());
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    /* JADX WARN: Code duplicated, block: B:68:0x01bf  */
    public BillInfo u(List<String> list) {
        String str;
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("云闪付");
        boolean z5 = false;
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str2 = list.get(i6);
            int i7 = 1;
            String str3 = "";
            if ((str2.equals("实付金额（元）") || str2.equals("实收金额（元）")) && i6 < list.size() - 1) {
                if (str2.equals("实收金额（元）")) {
                    billInfo.setIncome(true);
                    z5 = true;
                }
                String strReplace = list.get(i6 + 1).replace("元", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                }
            } else if (str2.contains("订单状态") && str2.contains("交易类型")) {
                List listAsList = Arrays.asList(str2.split(" "));
                if (listAsList.size() > 0) {
                    int i8 = 0;
                    while (i8 < listAsList.size()) {
                        String str4 = (String) listAsList.get(i8);
                        if (str4.contains("商户名称")) {
                            billInfo.setShopName(str4.replace("商户名称", str3));
                        } else if (str4.contains("乘车线路")) {
                            billInfo.setShopName(str4.replace("乘车线路", str3));
                        } else if (str4.contains("交易类型") && TextUtils.isEmpty(billInfo.getShopName())) {
                            billInfo.setShopName(str4.replace("交易类型", str3));
                        } else if (str4.contains("付款卡号")) {
                            String strReplace2 = str4.replace("付款卡号", str3);
                            billInfo.setAsset(strReplace2.substring(strReplace2.length() - 4));
                        } else if (str4.contains("信用卡卡号")) {
                            String strReplace3 = str4.replace("信用卡卡号", str3);
                            billInfo.setAsset(strReplace3.substring(strReplace3.length() - 4));
                        } else if (str4.contains("转入卡号")) {
                            String strReplace4 = str4.replace("转入卡号", str3);
                            billInfo.setAsset(strReplace4.substring(strReplace4.length() - 4));
                        } else if (str4.contains("银行名称")) {
                            billInfo.setAsset(str4.replace("银行名称", str3) + billInfo.getAsset());
                        } else if (str4.contains("付款卡")) {
                            billInfo.setAsset(str4.replace("付款卡", str3));
                        } else if (str4.contains("收款卡")) {
                            billInfo.setAsset(str4.replace("收款卡", str3));
                        } else {
                            if (str4.contains("订单时间")) {
                                String strReplace5 = str4.replace("订单时间", str3);
                                if (i8 < listAsList.size() - i7) {
                                    String str5 = (String) listAsList.get(i8 + 1);
                                    if (str5.contains(":")) {
                                        str = str3;
                                        long jX0 = t0.X0(strReplace5 + " " + str5, "yyyy-MM-dd HH:mm:ss");
                                        if (jX0 != -1) {
                                            billInfo.setTime(jX0);
                                        }
                                    } else {
                                        str = str3;
                                    }
                                } else {
                                    str = str3;
                                }
                                if (billInfo.getTime() <= 0) {
                                    billInfo.setTime(t0.X0(strReplace5, "yyyy-MM-dd HH:mm:ss"));
                                    if (billInfo.getTime() == -1 || billInfo.getTime() == 0) {
                                        billInfo.setTime(t0.X0(strReplace5, "yyyy-MM-dd HH:mm"));
                                    }
                                    if (billInfo.getTime() == -1 || billInfo.getTime() == 0) {
                                        billInfo.setTime(t0.X0(strReplace5, "yyyy-MM-dd"));
                                    }
                                }
                            }
                            i8++;
                            str3 = str;
                            i7 = 1;
                        }
                        str = str3;
                        i8++;
                        str3 = str;
                        i7 = 1;
                    }
                }
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark())) {
            if (TextUtils.isEmpty(billInfo.getShopName())) {
                billInfo.setRemark(billInfo.getOrigin());
            } else {
                billInfo.setRemark(billInfo.getShopName());
            }
        }
        if (z5) {
            billInfo.setShopName("收入-" + billInfo.getShopName());
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo v(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("云闪付");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if ((str.contains("当前状态") || str.contains("优惠信息")) && i6 > 0) {
                int i7 = i6 - 1;
                String strReplace = list.get(i7).replace("¥", "").replace(",", "").replace("+", "");
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                    if (i6 > 1 && TextUtils.isEmpty(billInfo.getRemark())) {
                        int i8 = i6 - 2;
                        billInfo.setRemark(list.get(i8));
                        billInfo.setShopName(list.get(i8));
                    }
                    if (list.get(i7).contains("+")) {
                        billInfo.setIncome(true);
                    }
                }
            } else if (str.equals("卡号") && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1).replace("尾号", ""));
                if (i6 < list.size() - 2) {
                    int i9 = i6 + 2;
                    if (list.get(i9).contains("****")) {
                        billInfo.setAsset(billInfo.getAsset() + list.get(i9).substring(list.get(i9).indexOf("****") + 4));
                    }
                }
            } else if (str.equals("收款方式") && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1).replace("****", ""));
            } else if (str.equals("付款方式") && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1).replace("****", ""));
            } else if ((str.equals("订单时间") || str.equals("退款时间")) && i6 < list.size() - 1) {
                int i10 = i6 + 1;
                billInfo.setTime(t0.X0(list.get(i10), "yyyy年MM月dd日 HH:mm:ss"));
                if (billInfo.getTime() == -1) {
                    billInfo.setTime(t0.X0(list.get(i10), "yyyy.MM.dd HH:mm:ss"));
                }
            } else if (str.equals("扣款时间") && i6 < list.size() - 1) {
                int i11 = i6 + 1;
                billInfo.setTime(t0.X0(list.get(i11).replace("延迟扣费|", ""), "yyyy年MM月dd日 HH:mm:ss"));
                if (billInfo.getTime() == -1) {
                    billInfo.setTime(t0.X0(list.get(i11).replace("延迟扣费|", ""), "yyyy.MM.dd HH:mm:ss"));
                }
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo w(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("云闪付");
        if (this.f13768g) {
            billInfo.setTransfer(true);
            billInfo.setToAsset(this.f13766e);
        }
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (TextUtils.isEmpty(billInfo.getNumber()) && str.contains("¥")) {
                String strReplace = str.replace("¥", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                    if (i6 > 0) {
                        billInfo.setRemark(list.get(i6 - 1));
                        billInfo.setShopName(billInfo.getRemark());
                    }
                }
            } else if (str.equals("订单信息") && i6 < list.size() - 1) {
                int i7 = i6 + 1;
                billInfo.setRemark(list.get(i7));
                billInfo.setShopName(list.get(i7));
            } else if (str.equals("付款方式") && i6 < list.size() - 1) {
                int i8 = i6 + 1;
                billInfo.setAsset(list.get(i8));
                if (i6 < list.size() - 3) {
                    int i9 = i6 + 3;
                    if (list.get(i9).contains("[") && list.get(i9).contains("]")) {
                        billInfo.setAsset(list.get(i8) + list.get(i6 + 2) + list.get(i9).substring(0, list.get(i9).indexOf("[")).replace(" ", ""));
                    } else {
                        int i10 = i6 + 2;
                        if (list.get(i10).contains("[") && list.get(i10).contains("]")) {
                            billInfo.setAsset(list.get(i8) + list.get(i10).substring(0, list.get(i10).indexOf("[")).replace(" ", ""));
                        }
                    }
                } else if (i6 < list.size() - 2) {
                    int i11 = i6 + 2;
                    if (list.get(i11).contains("[") && list.get(i11).contains("]")) {
                        billInfo.setAsset(list.get(i8) + list.get(i11).substring(0, list.get(i11).indexOf("[")).replace(" ", ""));
                    }
                }
                if (this.f13768g) {
                    billInfo.setFromAsset(billInfo.getAsset());
                }
            }
        }
        billInfo.setTime(System.currentTimeMillis());
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    /* JADX WARN: Code duplicated, block: B:104:0x0296 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:26:0x00b0  */
    /* JADX WARN: Code duplicated, block: B:28:0x00c2  */
    public BillInfo x(List<String> list, String str, String str2) {
        String strReplace;
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setTransfer(true);
        billInfo.setOrigin("云闪付");
        billInfo.setFromAsset(str);
        billInfo.setToAsset(str2);
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str3 = list.get(i6);
            if (str3.contains("¥")) {
                String strReplace2 = str3.replace("¥", "").replace(",", "");
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace2)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace2)) + "");
                }
            } else if (str3.contains("手续费")) {
                if (!str3.equals("手续费") || i6 >= list.size() - 1) {
                    strReplace = str3.substring(0, str3.lastIndexOf("手续费")).replace(",", "");
                    if (h2.J(strReplace)) {
                        billInfo.setServiceCharge(Math.abs(h2.S(strReplace)));
                    }
                } else {
                    int i7 = i6 + 1;
                    if (list.get(i7).contains("元")) {
                        String strReplace3 = list.get(i7).replace("元", "").replace(",", "");
                        if (h2.J(strReplace3)) {
                            billInfo.setServiceCharge(Math.abs(h2.S(strReplace3)));
                        }
                    } else {
                        strReplace = str3.substring(0, str3.lastIndexOf("手续费")).replace(",", "");
                        if (h2.J(strReplace)) {
                            billInfo.setServiceCharge(Math.abs(h2.S(strReplace)));
                        }
                    }
                }
            } else if (str3.equals("订单金额") && i6 < list.size() - 1) {
                String strReplace4 = list.get(i6 + 1).replace("¥", "").replace(",", "");
                if (h2.J(strReplace4)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace4)) + "");
                }
            } else if (str3.equals("转账金额") && i6 < list.size() - 1) {
                String strReplace5 = list.get(i6 + 1).replace("¥", "").replace(",", "");
                if (h2.J(strReplace5)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace5)) + "");
                }
            } else if ((str3.equals("收款银行") || str3.contains("信用卡号") || str3.contains("收款方式")) && i6 < list.size() - 1) {
                billInfo.setToAsset(list.get(i6 + 1));
            } else if ((str3.equals("付款银行") || str3.contains("付款方式")) && i6 < list.size() - 1) {
                billInfo.setFromAsset(list.get(i6 + 1));
            } else if (str3.contains("付款卡号") && i6 < list.size() - 1) {
                String str4 = list.get(i6 + 1);
                String strSubstring = str4.substring(str4.length() - 4);
                if (h2.J(strSubstring)) {
                    billInfo.setFromAsset(billInfo.getFromAsset() + strSubstring);
                }
            } else if (str3.contains("收款卡号") && i6 < list.size() - 1) {
                String str5 = list.get(i6 + 1);
                String strSubstring2 = str5.substring(str5.length() - 4);
                if (h2.J(strSubstring2)) {
                    billInfo.setToAsset(billInfo.getToAsset() + strSubstring2);
                }
            } else if (str3.contains("创建时间") && i6 < list.size() - 1) {
                String str6 = list.get(i6 + 1);
                billInfo.setTime(t0.X0(str6, "yyyy-MM-dd HH:mm:ss"));
                if (billInfo.getTime() == -1 || billInfo.getTime() == 0) {
                    billInfo.setTime(t0.X0(str6, "yyyy-MM-dd HH:mm"));
                }
                if (billInfo.getTime() == -1 || billInfo.getTime() == 0) {
                    billInfo.setTime(t0.X0(str6, "yyyy-MM-dd"));
                }
            } else if (str3.contains("订单时间") && i6 < list.size() - 1) {
                billInfo.setTime(t0.X0(list.get(i6 + 1), "yyyy年MM月dd日 HH:mm:ss"));
            }
        }
        if (TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }
}
