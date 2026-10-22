// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 f4db9b57c4ddcdbdca27380aa233349218e632876ff44c26b03564428aecbe86
package com.wangc.bill.auto.action;

import static com.wangc.bill.reference.RecoveredValues.*;

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

    /** Restored from the actual DEX fallback labels L47..L527; JADX's structured
     * output lost joins and emitted empty if blocks. No new page rules added. */
    public void t(Context context, String page, AccessibilityNodeInfo node) {
        List<String> texts = null;
        HashMap<String, Boolean> keys = null;
        // L47..L188: original page/session gates.
        if (page.equals("com.tencent.mm.ui.LauncherUI") && node != null) {
            this.a = 0; this.b = false; this.f13777g = false; this.f13778h = false;
        } else if (recognizedWechatPage(page)) {
            this.f13777g = true;
            if (page.equals("com.tencent.mm.plugin.aa.ui.PaylistAAUI")) this.f13778h = true;
        } else if (page.equals("com.tencent.mm.plugin.scanner.ui.BaseScanUI")) {
            this.f13781k = true;
        } else if (page.equals("com.tencent.mm.plugin.appbrand.ui.AppBrandPluginUI")) {
            this.f13779i = true;
        } else {
            boolean scanPay = this.f13781k && node != null
                && (B(node.findAccessibilityNodeInfosByText("支付成功")) || B(node.findAccessibilityNodeInfosByText("付款方式")));
            if (scanPay) {
                this.f13777g = true;
            } else if (this.f13779i && node != null) {
                texts = g(node);
                if (texts.contains("当前状态") && texts.contains("交易单号")) this.f13777g = true;
            } else if ((this.f13781k || this.f13779i) && node != null
                && B(node.findAccessibilityNodeInfosByText("通讯录"))
                && B(node.findAccessibilityNodeInfosByText("发现"))
                && B(node.findAccessibilityNodeInfosByText("微信"))) {
                this.f13781k = false; this.f13779i = false; this.a = 0;
                this.b = false; this.f13777g = false; this.f13778h = false;
            }
        }
        // L18a..L4ac: description-aware traversal and original state priority.
        if (this.f13777g && node != null) {
            texts = m(node);
            keys = new HashMap<>();
            for (String text : texts) keys.put(text, Boolean.TRUE);
            int kind = recoveredPageKind(page, texts, this.f13778h,keys.keySet());
            if (kind != 0) { this.a = kind; this.b = true; }
            boolean priority = keys.containsKey("二维码收款") && (keys.containsKey("优先使用此付款方式")
                || keys.containsKey("优先付款方式") || keys.containsKey("优先使用此支付方式付款"));
            boolean password = keys.containsKey("使用密码") || keys.containsKey("使用指纹") || keys.containsKey("请输入支付密码")
                || (keys.containsKey("更改") && keys.containsKey("1") && keys.containsKey("2"))
                || (keys.containsKey("更改") && keys.containsKey("付款方式"));
            if (s.i() && !keys.containsKey("支付成功") && (priority || (keys.containsKey("付款方式") && password))) {
                this.f13780j = System.currentTimeMillis();
            }
        } else if (paymentWechatPage(page)) {
            // L4af..L4ef: these native payment activities arm state 1.
            this.a = 1; this.b = true;
        }
        // L4f4..L527: only armed sessions reach the original bill parsers.
        if (!this.b || node == null) return;
        if (texts == null) {
            texts = g(node); keys = new HashMap<>();
            for (String text : texts) keys.put(text, Boolean.TRUE);
        }
        r(context, texts, keys);
    }

    private static boolean recognizedWechatPage(String page) {
        return paymentWechatPage(page) || switch (page) {
            case "com.tencent.mm.plugin.webview.ui.tools.MMWebViewUI",
                 "com.tencent.mm.plugin.webview.ui.tools.WebViewUI",
                 "com.tencent.mm.framework.app.UIPageFragmentActivity",
                 "com.tencent.mm.plugin.wallet_core.ui.WalletOrderInfoNewUI",
                 "com.tencent.mm.plugin.lite.ui.WxaLiteAppTransparentLiteUI",
                 "com.tencent.mm.plugin.aa.ui.PaylistAAUI",
                 "com.tencent.mm.plugin.appbrand.ui.AppBrandUI00" -> true;
            default -> false;
        };
    }
    private static boolean paymentWechatPage(String page) {
        return switch (page) {
            case "com.tencent.mm.plugin.remittance.ui.RemittanceBusiUI",
                 "com.tencent.mm.plugin.remittance.ui.RemittanceUI",
                 "com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI",
                 "com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI",
                 "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyPrepareUI",
                 "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI",
                 "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNewDetailUI",
                 "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI",
                 "com.tencent.mm.plugin.wallet_index.ui.OrderHandlerUI",
                 "com.tencent.mm.plugin.remittance.ui.RemittanceDetailUI" -> true;
            default -> false;
        };
    }
    /** DEX L1c8..L407. Return 0 means preserve the previous session state. */
    public static int recoveredPageKind(String page, List<String> texts, boolean groupMode) {
        return recoveredPageKind(page,texts,groupMode,new java.util.HashSet<>(texts));
    }
    private static int recoveredPageKind(String page,List<String> texts,boolean groupMode,java.util.Set<String> keys){
        boolean special = (keys.contains("当前状态") && (keys.contains("提现单号") || keys.contains("充值完成") || keys.contains("还款成功")))
            || (keys.contains("交易单号") && containsRecovered(texts, "转入零钱通"));
        if (special && !keys.contains("查看账单详情") && !keys.contains("常见问题")) return 11;
        boolean detail = keys.contains("账单详情") || (keys.contains("商品名称") && keys.contains("交易状态"))
            || (keys.contains("当前状态") && (keys.contains("转账单号") || keys.contains("交易单号") || keys.contains("付款单号") || keys.contains("收款单号")))
            || (keys.contains("退款状态") && keys.contains("退款单号")) || (keys.contains("付款商家") && keys.contains("付款单号"))
            || (keys.contains("详情") && containsRecovered(texts,"零钱通余额")) || (keys.contains("零钱通余额") && keys.contains("交易单号"));
        if (detail) return 3;
        if (groupMode && containsRecovered(texts,"发起的群收款")) return 4;
        boolean success = (keys.contains("支付成功") || keys.contains("付款成功")) && !keys.contains("当前状态")
            && !keys.contains("查看账单详情") && !keys.contains("常见问题") && !keys.contains("订单支付成功通知")
            && !keys.contains("交易单号") && !keys.contains("交易时间");
        boolean priority = keys.contains("二维码收款") && (keys.contains("优先付款方式") || keys.contains("优先使用此支付方式付款") || keys.contains("优先使用此付款方式"));
        boolean password = keys.contains("付款方式") && (keys.contains("使用密码") || keys.contains("使用指纹") || keys.contains("请输入支付密码")
            || (keys.contains("更改") && keys.contains("1") && keys.contains("2")));
        if (paymentWechatPage(page) || success || priority || password) return 1;
        boolean transfer = (keys.contains("充值成功") && keys.contains("完成")) || (keys.contains("零钱提现") && keys.contains("到账成功"))
            || (keys.contains("已转入") && keys.contains("完成")) || (keys.contains("已转出") && keys.contains("完成")) || (keys.contains("转出金额") && keys.contains("完成"));
        return transfer ? 10 : 0;
    }
    private static boolean containsRecovered(List<String> texts, String part) {
        for (String text : texts) if (text != null && text.contains(part)) return true;
        return false;
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

    /** DEX-exact branch transcription of w; original register and block addresses retained. */
    public com.wangc.bill.http.entity.BillInfo w(java.util.List arg0, java.lang.String arg1) {
        Object[] v=new Object[16]; Object[] result=new Object[2]; int pc=0;
        v[13]=this;
        v[14]=arg0;
        v[15]=arg1;
        while(true) {
            if(pc==-1) { return ((com.wangc.bill.http.entity.BillInfo)dexR(result[1])); }
            if(pc<0x12a) pc=recovered_w_block0(v,result,pc);
            else pc=recovered_w_block1(v,result,pc);
        }
    }
    private int recovered_w_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                result[0]=dexN((((java.util.List)dexR(v[14])).isEmpty())?1:0);
                v[0]=result[0];
                v[1]=dexN(0L);
                pc=(dexZero(v[0]))?0x8:0x7; continue;
            }
            case 0x7: {
                result[1]=v[1]; return -1;
            }
            case 0x8: {
                v[0]=new DexReference(); // new com.wangc.bill.http.entity.BillInfo
                ((DexReference)v[0]).value=new com.wangc.bill.http.entity.BillInfo();
                v[2]="微信";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setOrigin(((java.lang.String)dexR(v[2])));
                result[0]=dexN((com.wangc.bill.database.action.s.i())?1:0);
                v[2]=result[0];
                pc=(dexZero(v[2]))?0x31:0x18; continue;
            }
            case 0x18: {
                v[2]=dexN(((com.wangc.bill.auto.action.p)dexR(v[13])).f13780j);
                result[0]=dexN(java.lang.System.currentTimeMillis());
                v[4]=result[0];
                result[0]=dexN((com.wangc.bill.utils.b2.m0(dexL(v[2]),dexL(v[4])))?1:0);
                v[2]=result[0];
                pc=(dexZero(v[2]))?0x2a:0x24; continue;
            }
            case 0x24: {
                v[2]=dexN(((com.wangc.bill.auto.action.p)dexR(v[13])).f13780j);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setTime(dexL(v[2]));
                pc=0x31; continue;
            }
            case 0x2a: {
                result[0]=dexN(java.lang.System.currentTimeMillis());
                v[2]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setTime(dexL(v[2]));
                pc=0x31; continue;
            }
            case 0x31: {
                v[2]=dexN(0L);
                v[3]=v[2];
                pc=0x33; continue;
            }
            case 0x33: {
                result[0]=dexN(((java.util.List)dexR(v[14])).size());
                v[4]=result[0];
                pc=(dexI(v[2])>=dexI(v[4]))?0x1d5:0x39; continue;
            }
            case 0x39: {
                result[0]=((java.util.List)dexR(v[14])).get(dexI(v[2]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                v[5]="￥";
                v[6]="";
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[6])));
                v[7]=result[0];
                v[8]="¥";
                result[0]=((java.lang.String)dexR(v[7])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[6])));
                v[7]=result[0];
                v[9]=",";
                result[0]=((java.lang.String)dexR(v[7])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[6])));
                v[7]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[4]))))?1:0);
                v[10]=result[0];
                v[11]=dexN(1L);
                pc=(!dexZero(v[10]))?0x60:0x5a; continue;
            }
            case 0x5a: {
                result[0]=dexN((((java.lang.String)dexR(v[5])).equals(((java.lang.Object)dexR(v[4]))))?1:0);
                v[10]=result[0];
                pc=(dexZero(v[10]))?0x7a:0x60; continue;
            }
            case 0x60: {
                result[0]=dexN(((java.util.List)dexR(v[14])).size());
                v[10]=result[0];
                v[10]=dexN(dexI(v[10])-dexI(v[11]));
                pc=(dexI(v[2])>=dexI(v[10]))?0x7a:0x67; continue;
            }
            case 0x67: {
                v[10]=dexN(dexI(v[2])+(1));
                result[0]=((java.util.List)dexR(v[14])).get(dexI(v[10]));
                v[10]=result[0];
                v[10]=((java.lang.String)dexR(v[10]));
                result[0]=((java.lang.String)dexR(v[10])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[6])));
                v[10]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[10]))))?1:0);
                v[12]=result[0];
                pc=(dexZero(v[12]))?0x7a:0x79; continue;
            }
            case 0x79: {
                v[7]=v[10];
                pc=0x7a; continue;
            }
            case 0x7a: {
                pc=(!dexZero(v[3]))?0x86:0x7c; continue;
            }
            case 0x7c: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getNumber();
                v[10]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[10]))))?1:0);
                v[10]=result[0];
                pc=(dexZero(v[10]))?0xb1:0x86; continue;
            }
            case 0x86: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[7]))))?1:0);
                v[10]=result[0];
                pc=(dexZero(v[10]))?0xb1:0x8c; continue;
            }
            case 0x8c: {
                result[0]=dexN((((java.lang.String)dexR(v[4])).contains(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[10]=result[0];
                pc=(!dexZero(v[10]))?0x98:0x92; continue;
            }
            case 0x92: {
                result[0]=dexN((((java.lang.String)dexR(v[4])).contains(((java.lang.CharSequence)dexR(v[8]))))?1:0);
                v[10]=result[0];
                pc=(dexZero(v[10]))?0xb1:0x98; continue;
            }
            case 0x98: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setNumber(((java.lang.String)dexR(v[7])));
                pc=(dexZero(v[3]))?0x1d1:0x9d; continue;
            }
            case 0x9d: {
                v[4]=dexN(dexI(v[2])+(-1));
                result[0]=((java.util.List)dexR(v[14])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setRemark(((java.lang.String)dexR(v[4])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getRemark();
                v[4]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setShopName(((java.lang.String)dexR(v[4])));
                pc=0x1d1; continue;
            }
            case 0xb1: {
                v[7]="优惠";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[7]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0xef:0xb9; continue;
            }
            case 0xb9: {
                result[0]=dexN(((java.util.List)dexR(v[14])).size());
                v[7]=result[0];
                v[7]=dexN(dexI(v[7])-dexI(v[11]));
                pc=(dexI(v[2])>=dexI(v[7]))?0xef:0xc0; continue;
            }
            case 0xc0: {
                v[4]=dexN(dexI(v[2])+(1));
                result[0]=((java.util.List)dexR(v[14])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                result[0]=dexN((((java.lang.String)dexR(v[4])).contains(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x1d1:0xce; continue;
            }
            case 0xce: {
                result[0]=dexN(((java.lang.String)dexR(v[4])).indexOf(((java.lang.String)dexR(v[5]))));
                v[7]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).substring(dexI(v[7]));
                v[4]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[7]=result[0];
                pc=(!dexZero(v[7]))?0x1d1:0xdc; continue;
            }
            case 0xdc: {
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[6])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[6])));
                v[4]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x1d1:0xea; continue;
            }
            case 0xea: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setDiscount(((java.lang.String)dexR(v[4])));
                pc=0x1d1; continue;
            }
            case 0xef: {
                v[7]="收款方";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[7]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x112:0xf7; continue;
            }
            case 0xf7: {
                result[0]=dexN(((java.util.List)dexR(v[14])).size());
                v[7]=result[0];
                v[7]=dexN(dexI(v[7])-dexI(v[11]));
                pc=(dexI(v[2])>=dexI(v[7]))?0x112:0xfe; continue;
            }
            case 0xfe: {
                v[4]=dexN(dexI(v[2])+(1));
                result[0]=((java.util.List)dexR(v[14])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setRemark(((java.lang.String)dexR(v[4])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getRemark();
                v[4]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setShopName(((java.lang.String)dexR(v[4])));
                pc=0x1d1; continue;
            }
            case 0x112: {
                v[7]="支付成功";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[7]))))?1:0);
                v[7]=result[0];
                pc=(!dexZero(v[7]))?0x122:0x11a; continue;
            }
            case 0x11a: {
                v[7]="付款成功";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[7]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x187:0x122; continue;
            }
            case 0x122: {
                result[0]=dexN(((java.util.List)dexR(v[14])).size());
                v[7]=result[0];
                v[7]=dexN(dexI(v[7])+(-2));
                pc=(dexI(v[2])>=dexI(v[7]))?0x187:0x12a; continue;
            }
            default: return pc;
        }
    }
    private int recovered_w_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x12a: {
                v[7]=dexN(dexI(v[2])+(1));
                result[0]=((java.util.List)dexR(v[14])).get(dexI(v[7]));
                v[10]=result[0];
                v[10]=((java.lang.String)dexR(v[10]));
                result[0]=dexN((((java.lang.String)dexR(v[10])).contains(((java.lang.CharSequence)dexR(v[8]))))?1:0);
                v[10]=result[0];
                pc=(!dexZero(v[10]))?0x187:0x138; continue;
            }
            case 0x138: {
                result[0]=((java.util.List)dexR(v[14])).get(dexI(v[7]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setRemark(((java.lang.String)dexR(v[4])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getRemark();
                v[4]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setShopName(((java.lang.String)dexR(v[4])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getRemark();
                v[4]=result[0];
                v[5]="待";
                result[0]=dexN((((java.lang.String)dexR(v[4])).startsWith(((java.lang.String)dexR(v[5]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1d1:0x154; continue;
            }
            case 0x154: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getRemark();
                v[4]=result[0];
                v[5]="确认收款";
                result[0]=dexN((((java.lang.String)dexR(v[4])).endsWith(((java.lang.String)dexR(v[5]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1d1:0x160; continue;
            }
            case 0x160: {
                v[4]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[4]).value=new java.lang.StringBuilder();
                v[5]="转账给";
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[5])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getRemark();
                v[5]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getRemark();
                v[6]=result[0];
                result[0]=dexN(((java.lang.String)dexR(v[6])).length());
                v[6]=result[0];
                v[6]=dexN(dexI(v[6])+(-4));
                result[0]=((java.lang.String)dexR(v[5])).substring(dexI(v[11]),dexI(v[6]));
                v[5]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[5])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).toString();
                v[4]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setRemark(((java.lang.String)dexR(v[4])));
                pc=0x1d1; continue;
            }
            case 0x187: {
                v[7]="保存收款码";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[7]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x194:0x18f; continue;
            }
            case 0x18f: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setIncome(dexZ(v[11]));
                v[3]=v[11];
                pc=0x1d1; continue;
            }
            case 0x194: {
                result[0]=dexN((((java.lang.String)dexR(v[4])).contains(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[7]=result[0];
                pc=(!dexZero(v[7]))?0x1a0:0x19a; continue;
            }
            case 0x19a: {
                result[0]=dexN((((java.lang.String)dexR(v[4])).contains(((java.lang.CharSequence)dexR(v[8]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x1d1:0x1a0; continue;
            }
            case 0x1a0: {
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[6])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[6])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[6])));
                v[4]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getNumber();
                v[5]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x1c0:0x1b6; continue;
            }
            case 0x1b6: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x1c0:0x1bc; continue;
            }
            case 0x1bc: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setNumber(((java.lang.String)dexR(v[4])));
                pc=0x1d1; continue;
            }
            case 0x1c0: {
                v[5]="订单状态";
                result[0]=dexN((((java.util.List)dexR(v[14])).contains(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x1d1:0x1c8; continue;
            }
            case 0x1c8: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x1d1:0x1ce; continue;
            }
            case 0x1ce: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setNumber(((java.lang.String)dexR(v[4])));
                pc=0x1d1; continue;
            }
            case 0x1d1: {
                v[2]=dexN(dexI(v[2])+(1));
                pc=0x33; continue;
            }
            case 0x1d5: {
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[15]))))?1:0);
                v[14]=result[0];
                pc=(!dexZero(v[14]))?0x1de:0x1db; continue;
            }
            case 0x1db: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setAsset(((java.lang.String)dexR(v[15])));
                pc=0x1de; continue;
            }
            case 0x1de: {
                v[14]="零钱";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getAsset();
                v[2]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[2]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x1ef:0x1ea; continue;
            }
            case 0x1ea: {
                v[14]="微信钱包";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setAsset(((java.lang.String)dexR(v[14])));
                pc=0x1ef; continue;
            }
            case 0x1ef: {
                v[14]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[14]).value=new java.lang.StringBuilder();
                v[2]="asset:";
                result[0]=((java.lang.StringBuilder)dexR(v[14])).append(((java.lang.String)dexR(v[2])));
                result[0]=((java.lang.StringBuilder)dexR(v[14])).append(((java.lang.String)dexR(v[15])));
                result[0]=((java.lang.StringBuilder)dexR(v[14])).toString();
                v[14]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[14]))};
                v[14]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[14])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getRemark();
                v[14]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(!dexZero(v[14]))?0x21c:0x211; continue;
            }
            case 0x211: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getNumber();
                v[14]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(!dexZero(v[14]))?0x21c:0x21b; continue;
            }
            case 0x21b: {
                result[1]=v[0]; return -1;
            }
            case 0x21c: {
                result[1]=v[1]; return -1;
            }
            default: return pc;
        }
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
