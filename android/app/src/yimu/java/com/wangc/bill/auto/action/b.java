// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 2f70da8df9fa2a299ae071afbae0bd49e3869b2f37fddcee6dbe1dff1dd0b7d9
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class b extends c implements x1.a {

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private static final int f13671h = 2;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private static final int f13672i = 6;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private static final int f13673j = 7;

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    private static final int f13674k = 9;

    /* JADX INFO: renamed from: l, reason: collision with root package name */
    private static final int f13675l = 10;

    /* JADX INFO: renamed from: m, reason: collision with root package name */
    private static final int f13676m = 11;

    /* JADX INFO: renamed from: n, reason: collision with root package name */
    private static b f13677n;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private long f13678e;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private boolean f13679f = false;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private final List<String> f13680g = Arrays.asList("提现说明", "转出说明", "余额转入", "充值说明", "网商银行转账", "还款成功", "余额宝-单次转入", "余额宝-转出到余额");

    public static b A() {
        if (f13677n == null) {
            f13677n = new b();
        }
        return f13677n;
    }

    private void y(Context context, List<String> list, HashMap<String, Boolean> map) {
        boolean zA;
        BillInfo billInfoR;
        BillInfo billInfoR2;
        y.l("sssss", "start find node:" + this.a);
        if (list == null || list.isEmpty()) {
            return;
        }
        boolean z5 = true;
        boolean z6 = this.a != 2 || map.containsKey("支付成功") || map.containsKey("转账成功") || map.containsKey("代付成功") || map.containsKey("交易成功") || map.containsKey("充值成功") || map.containsKey("还款成功") || map.containsKey("退款成功") || map.containsKey("赔付成功") || map.containsKey("自动扣款成功") || map.containsKey("有退款") || map.containsKey("已全额退款") || map.containsKey("缴费中") || map.containsKey("派送中") || a(list, "亲情卡付款成功", false) || a(list, "付款成功", false);
        boolean zContainsKey = map.containsKey("转账成功");
        boolean z7 = map.containsKey("已收款") || map.containsKey("资金待入账") || map.containsKey("你已收款");
        if (!map.containsKey("支付宝红包") && !map.containsKey("查看红包记录") && !map.containsKey("查看红包")) {
            z5 = false;
        }
        if (z5) {
            zA = a(list, "红包金额", false);
            if (!zA) {
                zA = a(list, "个红包共", false);
            }
            if (!zA) {
                zA = a(list, "发的红包", false);
            }
            if (!zA) {
                zA = a(list, "领取成功", false);
            }
            if (!zA) {
                zA = map.containsKey("查看红包");
            }
            if (zA) {
                z6 = false;
            }
        } else {
            zA = false;
        }
        if (z6) {
            a2.a("start check pay:" + this.a);
            int i6 = this.a;
            if (i6 == 2 || i6 == 6 || i6 == 7) {
                billInfoR2 = r(context, list);
            } else if (i6 == 9) {
                billInfoR2 = u(list);
            } else if (i6 == 10) {
                billInfoR2 = t(list);
            } else {
                billInfoR2 = i6 == 11 ? s(context, list) : null;
            }
            if (billInfoR2 != null) {
                this.b = false;
                this.f13679f = false;
                billInfoR2.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                SelectToSpeakService.l(context, billInfoR2, this);
                return;
            }
            return;
        }
        if (zContainsKey) {
            BillInfo billInfoV = v(list);
            if (billInfoV != null) {
                this.b = false;
                this.f13679f = false;
                billInfoV.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                SelectToSpeakService.l(context, billInfoV, this);
                return;
            }
            return;
        }
        if (z7) {
            if (this.a != 2 || (billInfoR = r(context, list)) == null) {
                return;
            }
            this.b = false;
            this.f13679f = false;
            billInfoR.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
            SelectToSpeakService.l(context, billInfoR, this);
            return;
        }
        if (zA) {
            BillInfo billInfoZ = z(list);
            if (billInfoZ == null) {
                billInfoZ = x(list);
            }
            if (billInfoZ != null) {
                this.b = false;
                this.f13679f = false;
                billInfoZ.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                SelectToSpeakService.l(context, billInfoZ, this);
            }
        }
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
        this.b = false;
    }

    public boolean q(String str, String str2) {
        return str.equals("com.eg.android.AlipayGphone") && (str2.equals("com.eg.android.AlipayGphone.AlipayLogin") || str2.equals("com.alipay.android.msp.ui.views.MspContainerActivity") || str2.equals("com.alipay.android.msp.ui.views.MspUniRenderActivity") || str2.equals("com.alipay.android.phone.discovery.envelope.get.SnsCouponDetailActivity"));
    }

    /* JADX WARN: Code duplicated, block: B:130:0x0283 A[PHI: r20
      0x0283: PHI (r20v1 java.lang.String) = (r20v2 java.lang.String), (r20v3 java.lang.String) binds: [B:132:0x0289, B:128:0x0280] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:166:0x0369  */
    /* JADX WARN: Code duplicated, block: B:168:0x0373  */
    /* JADX WARN: Code duplicated, block: B:196:0x0431  */
    /* JADX WARN: Code duplicated, block: B:204:0x044c  */
    /* JADX WARN: Code duplicated, block: B:208:0x046b  */
    /* JADX WARN: Code duplicated, block: B:213:0x0488  */
    /* JADX WARN: Code duplicated, block: B:219:0x04a2  */
    public BillInfo r(Context context, List<String> list) {
        String str;
        String str2;
        String str3;
        BillInfo billInfo;
        Context context2;
        int i6;
        int i7;
        Context context3 = context;
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo2 = new BillInfo();
        String str4 = "支付宝";
        billInfo2.setOrigin("支付宝");
        if (s.i()) {
            if (b2.m0(this.f13678e, System.currentTimeMillis())) {
                billInfo2.setTime(this.f13678e);
            } else {
                this.f13678e = System.currentTimeMillis();
            }
        }
        int i8 = 0;
        while (i8 < list.size()) {
            String str5 = list.get(i8);
            if (!str5.equals("收款方") || i8 >= list.size() - 1) {
                str2 = str4;
                if (this.a != 2 || (!(str5.equals("支付成功") || str5.equals("充值成功") || str5.equals("代付成功") || str5.equals("转账成功")) || i8 >= list.size() - 2)) {
                    BillInfo billInfo3 = billInfo2;
                    if (str5.equals("有退款") || str5.equals("自动扣款成功") || str5.equals("已全额退款")) {
                        str3 = "支付成功";
                    } else {
                        str3 = "支付成功";
                        if ((!str5.equals("支付成功") || this.a != 6) && ((!str5.equals("充值成功") || this.a != 6) && !str5.equals("退款成功") && !str5.equals("赔付成功") && !str5.equals("交易成功") && !str5.equals("还款成功") && !str5.equals("代付成功") && !str5.equals("派送中") && !str5.equals("领取中") && !str5.equals("等待对方发货") && !str5.equals("等待确认收货") && !str5.equals("交易成功(代付成功)") && !str5.equals("退回成功") && !str5.equals("支付成功(代付成功)") && !str5.contains("付款成功") && !str5.contains("缴费中") && !str5.contains("支付宝小荷包付款成功") && !str5.contains("已退款(") && !str5.contains("亲情卡付款成功") && !str5.equals("免密支付成功") && !str5.equals("自动续费成功") && !str5.equals("充值成功") && !str5.equals("收款成功") && !str5.equals("等待对方确认收货"))) {
                            billInfo = billInfo3;
                            if (!str5.equals("收款理由") && i8 < list.size() - 1) {
                                billInfo.setRemark(list.get(i8 + 1).replaceFirst("收款理由", ""));
                            } else if (TextUtils.isEmpty(billInfo.getNumber()) || !(str5.contains("￥") || str5.contains("¥") || str5.contains("-") || str5.contains("+") || str5.contains("支出"))) {
                                if ((!str5.equals("付款方式") || str5.equals("付款信息") || str5.equals("交易方式") || str5.equals("退款方式")) && i8 < list.size() - 1) {
                                    billInfo.setAsset(list.get(i8 + 1));
                                    if ("帮助".equals(billInfo.getAsset()) && i8 < list.size() - 2) {
                                        billInfo.setAsset(list.get(i8 + 2));
                                    }
                                    if (TextUtils.isEmpty(billInfo.getAsset()) && billInfo.getAsset().contains("亲情卡") && billInfo.isIncome()) {
                                        billInfo.setIncome(false);
                                    }
                                } else if (str5.equals("创建时间") && i8 < list.size() - 1) {
                                    billInfo.setTime(t0.X0(list.get(i8 + 1).replace("创建时间", ""), "yyyy-MM-dd HH:mm:ss"));
                                } else if (!str5.equals("支付时间") || i8 >= list.size() - 1) {
                                    if (str5.equals("收款时间")) {
                                        i6 = 1;
                                        if (i8 < list.size() - 1) {
                                            billInfo.setTime(t0.X0(list.get(i8 + 1), "yyyy-MM-dd HH:mm:ss"));
                                            billInfo.setIncome(true);
                                        }
                                    } else {
                                        i6 = 1;
                                    }
                                    if (this.a == 7 && str5.equals("订单金额") && i8 < list.size() - i6) {
                                        String strReplace = list.get(i8 + 1).replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("元", "").replace("-", "");
                                        if (h2.J(strReplace)) {
                                            billInfo.setNumber(strReplace);
                                        }
                                    } else if (this.a == 7 && ((str5.equals("花呗收钱服务费") || str5.equals("平台服务费")) && i8 < list.size() - 1)) {
                                        String strReplace2 = list.get(i8 + 1).replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("元", "").replace("-", "");
                                        if (h2.J(strReplace2)) {
                                            billInfo.setNumber((h2.S(billInfo.getNumber()) - h2.S(strReplace2)) + "");
                                        }
                                    } else if (str5.equals("商品说明") && i8 < list.size() - 1) {
                                        billInfo.setRemark(list.get(i8 + 1).replaceFirst("商品说明", ""));
                                        if (billInfo.getRemark().contains("余额宝") && billInfo.getRemark().contains("收益发放")) {
                                            billInfo.setAsset("余额宝");
                                        }
                                    } else if (!str5.equals("订单详情") || i8 >= list.size() - 1) {
                                        context2 = context;
                                        if (str5.equals("交易详情") && i8 < list.size() - 1) {
                                            String str6 = list.get(i8 + 1);
                                            if (!TextUtils.isEmpty(str6) && !str6.equals("更多") && !str6.equals("推荐服务") && !str6.equals("服务推荐") && i8 < list.size() - 2) {
                                                String str7 = list.get(i8 + 2);
                                                if (!TextUtils.isEmpty(str7)) {
                                                    billInfo.setRemark(str7);
                                                    SelectToSpeakService.E(context2, billInfo.getRemark());
                                                }
                                            }
                                        } else if (str5.equals("缴费说明") && i8 < list.size() - 1) {
                                            billInfo.setRemark(list.get(i8 + 1).replaceFirst("缴费说明", ""));
                                        } else if (str5.equals("充值说明") && i8 < list.size() - 1) {
                                            billInfo.setRemark(list.get(i8 + 1).replaceFirst("充值说明", ""));
                                        } else if (str5.equals("付款备注") && i8 < list.size() - 1) {
                                            billInfo.setRemark(list.get(i8 + 1).replaceFirst("付款备注", ""));
                                        } else if (str5.equals("红包说明") && i8 < list.size() - 1) {
                                            billInfo.setRemark(list.get(i8 + 1).replaceFirst("红包说明", ""));
                                        } else if (str5.equals("理由") && i8 < list.size() - 1) {
                                            billInfo.setRemark(list.get(i8 + 1).replaceFirst("理由", ""));
                                        } else if (str5.equals("转账备注") && i8 < list.size() - 1) {
                                            int i9 = i8 + 1;
                                            if (!"转账".equals(list.get(i9))) {
                                                billInfo.setRemark(list.get(i9));
                                            } else if (billInfo.isIncome()) {
                                                billInfo.setRemark("收到" + billInfo.getShopName() + "转账");
                                            } else {
                                                billInfo.setRemark("转账给" + billInfo.getShopName());
                                            }
                                            if (billInfo.isIncome()) {
                                                billInfo.setShopName("收到" + billInfo.getShopName() + "转账");
                                            } else {
                                                billInfo.setShopName("转账给" + billInfo.getShopName());
                                            }
                                        } else if (TextUtils.isEmpty(billInfo.getDiscount()) && (((this.f13679f && str5.startsWith("-￥")) || str5.equals("碰一下支付立减") || str5.equals("支付宝随机立减") || str5.equals("碰一下立减") || str5.equals("碰一下共减") || str5.equals("视频红包") || str5.equals("优惠") || str5.equals("网商银行福利金抵扣")) && i8 < list.size() - 1)) {
                                            String str8 = list.get(i8 + 1);
                                            if (str8.equals("¥") && i8 < list.size() - 2) {
                                                str8 = list.get(i8 + 2);
                                            }
                                            String strReplace3 = str8.replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("元", "").replace("-", "");
                                            if (h2.J(strReplace3)) {
                                                billInfo.setDiscount(strReplace3);
                                            }
                                        } else if (this.a != 7 && str5.equals("订单金额") && i8 < list.size() - 1) {
                                            String strReplace4 = list.get(i8 + 1).replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("元", "").replace("-", "");
                                            if (h2.J(strReplace4) && !TextUtils.isEmpty(billInfo.getNumber())) {
                                                billInfo.setDiscount(h2.t(h2.S(strReplace4) - h2.S(billInfo.getNumber())));
                                            }
                                        }
                                    } else {
                                        int i10 = i8 + 1;
                                        if (!str3.equals(list.get(i10))) {
                                            billInfo.setRemark(list.get(i10));
                                        }
                                        if ("商品图片".equals(billInfo.getRemark())) {
                                            billInfo.setRemark(list.get(i8 + 2));
                                            context2 = context;
                                            SelectToSpeakService.E(context2, billInfo.getRemark());
                                        }
                                    }
                                } else {
                                    String strReplace5 = list.get(i8 + 1).replace("支付时间", "");
                                    if (strReplace5.contains("-")) {
                                        billInfo.setTime(t0.X0(strReplace5, "yyyy-MM-dd HH:mm:ss"));
                                    } else if (strReplace5.contains("年")) {
                                        billInfo.setTime(t0.X0(strReplace5, "yyyy年MM月dd日 HH:mm:ss"));
                                    }
                                }
                                context2 = context;
                            } else {
                                String strReplace6 = str5.replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("支出", "").replace("元", "");
                                if (!h2.J(strReplace6) && i8 < list.size() - 1) {
                                    str5 = list.get(i8 + 1);
                                    strReplace6 = str5.replace("￥", "").replace("¥", "").replace(",", "").replace("+", "");
                                }
                                if (h2.J(strReplace6)) {
                                    billInfo.setNumber(Math.abs(h2.S(strReplace6)) + "");
                                    if (str5.contains("+")) {
                                        billInfo.setRemark("支付宝收款");
                                        billInfo.setShopName(billInfo.getRemark());
                                        billInfo.setIncome(true);
                                    }
                                }
                                if (TextUtils.isEmpty(billInfo.getRemark()) && i8 > 0) {
                                    billInfo.setRemark(list.get(i8 - 1));
                                    billInfo.setShopName(billInfo.getRemark());
                                }
                            }
                            context2 = context;
                        }
                        i8++;
                        billInfo2 = billInfo;
                        context3 = context2;
                        str4 = str2;
                    }
                    if (i8 > 0) {
                        int i11 = i8 - 1;
                        String strReplace7 = list.get(i11).replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("元", "").replace("-", "").replace("支出", "");
                        if (h2.J(strReplace7)) {
                            billInfo = billInfo3;
                            billInfo.setNumber(strReplace7);
                            if (list.get(i11).contains("+") || (!(list.get(i11).contains("-") || list.get(i11).contains("支出")) || str5.equals("退回成功"))) {
                                i7 = 1;
                                billInfo.setIncome(true);
                            } else {
                                i7 = 1;
                            }
                            if (str5.contains("扣款成功")) {
                                billInfo.setIncome(false);
                            }
                            if (i8 > i7) {
                                billInfo.setRemark(list.get(i8 - 2));
                            }
                            if (str5.equals("退款成功")) {
                                billInfo.setRemark("退款-来自" + billInfo.getRemark());
                                billInfo.setIncome(true);
                            }
                            billInfo.setShopName(billInfo.getRemark());
                        } else {
                            billInfo = billInfo3;
                            if (strReplace7.equals("NaN")) {
                                billInfo.setNumber("0");
                            }
                        }
                    } else {
                        billInfo = billInfo3;
                        if (!str5.equals("收款理由")) {
                            if (TextUtils.isEmpty(billInfo.getNumber())) {
                            }
                            if (str5.equals("付款方式")) {
                                billInfo.setAsset(list.get(i8 + 1));
                                if ("帮助".equals(billInfo.getAsset())) {
                                    billInfo.setAsset(list.get(i8 + 2));
                                }
                                if (TextUtils.isEmpty(billInfo.getAsset())) {
                                }
                            } else {
                                billInfo.setAsset(list.get(i8 + 1));
                                if ("帮助".equals(billInfo.getAsset())) {
                                    billInfo.setAsset(list.get(i8 + 2));
                                }
                                if (TextUtils.isEmpty(billInfo.getAsset())) {
                                }
                            }
                            context2 = context;
                        } else {
                            if (TextUtils.isEmpty(billInfo.getNumber())) {
                            }
                            if (str5.equals("付款方式")) {
                                billInfo.setAsset(list.get(i8 + 1));
                                if ("帮助".equals(billInfo.getAsset())) {
                                    billInfo.setAsset(list.get(i8 + 2));
                                }
                                if (TextUtils.isEmpty(billInfo.getAsset())) {
                                }
                            } else {
                                billInfo.setAsset(list.get(i8 + 1));
                                if ("帮助".equals(billInfo.getAsset())) {
                                    billInfo.setAsset(list.get(i8 + 2));
                                }
                                if (TextUtils.isEmpty(billInfo.getAsset())) {
                                }
                            }
                            context2 = context;
                        }
                        i8++;
                        billInfo2 = billInfo;
                        context3 = context2;
                        str4 = str2;
                    }
                    context2 = context;
                } else {
                    int i12 = i8 + 1;
                    String strReplace8 = list.get(i12).replace("￥", "").replace("¥", "").replace("元", "").replace(",", "").replace("支出", "");
                    if (h2.J(strReplace8)) {
                        billInfo2.setNumber(strReplace8);
                    }
                    String strReplace9 = list.get(i8 + 2).replace("￥", "").replace("¥", "").replace("元", "").replace(",", "").replace("支出", "");
                    if (!h2.J(strReplace8) && h2.J(strReplace9)) {
                        billInfo2.setNumber(strReplace9);
                        if (this.f13679f) {
                            if (("¥".equals(list.get(i12)) || "￥".equals(list.get(i12))) && i8 < list.size() - 3) {
                                billInfo2.setRemark(list.get(i8 + 3));
                            } else {
                                billInfo2.setRemark(strReplace8);
                            }
                            billInfo2.setShopName(billInfo2.getRemark());
                        }
                    } else if (!strReplace9.equals("付款方式")) {
                        if (!this.f13679f) {
                            int iD = d(list, "￥", false);
                            String strReplace10 = iD > 0 ? list.get(iD).replace("￥", "").replace("¥", "").replace(",", "") : null;
                            if (iD > 1 && !TextUtils.isEmpty(strReplace10) && h2.J(strReplace10)) {
                                strReplace9 = list.get(iD - 1);
                                if (h2.J(billInfo2.getNumber())) {
                                    billInfo2.setDiscount(h2.t(h2.S(strReplace10) - h2.S(billInfo2.getNumber())));
                                }
                            }
                        }
                        billInfo2.setRemark(strReplace9);
                        billInfo2.setShopName(strReplace9);
                    }
                    context2 = context;
                    billInfo = billInfo2;
                }
            } else {
                billInfo2.setRemark(list.get(i8 + 1));
                billInfo2.setShopName(billInfo2.getRemark());
                context2 = context3;
                billInfo = billInfo2;
                str2 = str4;
            }
            i8++;
            billInfo2 = billInfo;
            context3 = context2;
            str4 = str2;
        }
        BillInfo billInfo4 = billInfo2;
        String str9 = str4;
        if (!TextUtils.isEmpty(billInfo4.getNumber()) && TextUtils.isEmpty(billInfo4.getRemark())) {
            if (billInfo4.isIncome()) {
                billInfo4.setRemark("支付宝收款");
                billInfo4.setShopName(billInfo4.getRemark());
            } else {
                billInfo4.setRemark("支付宝付款");
                billInfo4.setShopName(billInfo4.getRemark());
            }
        }
        if (billInfo4.isIncome() && TextUtils.isEmpty(billInfo4.getAsset())) {
            str = str9;
            billInfo4.setAsset(str);
        } else {
            str = str9;
        }
        if (!TextUtils.isEmpty(billInfo4.getAsset())) {
            billInfo4.setAsset(billInfo4.getAsset().trim());
        }
        if ("账户余额".equals(billInfo4.getAsset()) || "余额".equals(billInfo4.getAsset()) || "可用余额".equals(billInfo4.getAsset())) {
            billInfo4.setAsset(str);
        }
        if (TextUtils.isEmpty(billInfo4.getRemark()) || TextUtils.isEmpty(billInfo4.getNumber())) {
            return null;
        }
        return billInfo4;
    }

    /* JADX WARN: Code duplicated, block: B:130:0x0283 A[PHI: r20
      0x0283: PHI (r20v1 java.lang.String) = (r20v2 java.lang.String), (r20v3 java.lang.String) binds: [B:132:0x0289, B:128:0x0280] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:166:0x0369  */
    /* JADX WARN: Code duplicated, block: B:168:0x0373  */
    /* JADX WARN: Code duplicated, block: B:196:0x0431  */
    /* JADX WARN: Code duplicated, block: B:204:0x044c  */
    /* JADX WARN: Code duplicated, block: B:208:0x046b  */
    /* JADX WARN: Code duplicated, block: B:213:0x0488  */
    /* JADX WARN: Code duplicated, block: B:219:0x04a2  */
    public BillInfo s(Context context, List<String> list) {
        String str;
        String str2;
        String str3;
        BillInfo billInfo;
        Context context2;
        int i6;
        int i7;
        Context context3 = context;
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo2 = new BillInfo();
        String str4 = "支付宝";
        billInfo2.setOrigin("支付宝");
        if (s.i()) {
            if (b2.m0(this.f13678e, System.currentTimeMillis())) {
                billInfo2.setTime(this.f13678e);
            } else {
                this.f13678e = System.currentTimeMillis();
            }
        }
        int i8 = 0;
        while (i8 < list.size()) {
            String str5 = list.get(i8);
            if (!str5.equals("收款方") || i8 >= list.size() - 1) {
                str2 = str4;
                if (this.a != 2 || (!(str5.equals("支付成功") || str5.equals("充值成功") || str5.equals("代付成功") || str5.equals("转账成功")) || i8 >= list.size() - 2)) {
                    BillInfo billInfo3 = billInfo2;
                    if (str5.equals("有退款") || str5.equals("Auto debit successful") || str5.equals("已全额退款")) {
                        str3 = "支付成功";
                    } else {
                        str3 = "支付成功";
                        if ((!str5.equals("支付成功") || this.a != 6) && ((!str5.equals("充值成功") || this.a != 6) && !str5.equals("退款成功") && !str5.equals("赔付成功") && !str5.equals("Transaction successful") && !str5.equals("还款成功") && !str5.equals("代付成功") && !str5.equals("派送中") && !str5.equals("领取中") && !str5.equals("等待对方发货") && !str5.equals("等待确认收货") && !str5.equals("交易成功(代付成功)") && !str5.equals("退回成功") && !str5.equals("支付成功(代付成功)") && !str5.contains("付款成功") && !str5.contains("缴费中") && !str5.contains("支付宝小荷包付款成功") && !str5.contains("已退款(") && !str5.contains("亲情卡付款成功") && !str5.equals("免密支付成功") && !str5.equals("自动续费成功") && !str5.equals("充值成功") && !str5.equals("收款成功") && !str5.equals("等待对方确认收货"))) {
                            billInfo = billInfo3;
                            if (!str5.equals("收款理由") && i8 < list.size() - 1) {
                                billInfo.setRemark(list.get(i8 + 1).replaceFirst("收款理由", ""));
                            } else if (TextUtils.isEmpty(billInfo.getNumber()) || !(str5.contains("￥") || str5.contains("¥") || str5.contains("-") || str5.contains("+") || str5.contains("支出"))) {
                                if ((!str5.equals("Payment method") || str5.equals("付款信息") || str5.equals("交易方式") || str5.equals("退款方式")) && i8 < list.size() - 1) {
                                    billInfo.setAsset(list.get(i8 + 1));
                                    if ("帮助".equals(billInfo.getAsset()) && i8 < list.size() - 2) {
                                        billInfo.setAsset(list.get(i8 + 2));
                                    }
                                    if (TextUtils.isEmpty(billInfo.getAsset()) && billInfo.getAsset().contains("亲情卡") && billInfo.isIncome()) {
                                        billInfo.setIncome(false);
                                    }
                                } else if (str5.equals("Order time") && i8 < list.size() - 1) {
                                    billInfo.setTime(t0.X0(list.get(i8 + 1).replace("Order time", ""), "yyyy-MM-dd HH:mm:ss"));
                                } else if (!str5.equals("Payment time") || i8 >= list.size() - 1) {
                                    if (str5.equals("收款时间")) {
                                        i6 = 1;
                                        if (i8 < list.size() - 1) {
                                            billInfo.setTime(t0.X0(list.get(i8 + 1), "yyyy-MM-dd HH:mm:ss"));
                                            billInfo.setIncome(true);
                                        }
                                    } else {
                                        i6 = 1;
                                    }
                                    if (this.a == 7 && str5.equals("订单金额") && i8 < list.size() - i6) {
                                        String strReplace = list.get(i8 + 1).replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("元", "").replace("-", "");
                                        if (h2.J(strReplace)) {
                                            billInfo.setNumber(strReplace);
                                        }
                                    } else if (this.a == 7 && ((str5.equals("花呗收钱服务费") || str5.equals("平台服务费")) && i8 < list.size() - 1)) {
                                        String strReplace2 = list.get(i8 + 1).replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("元", "").replace("-", "");
                                        if (h2.J(strReplace2)) {
                                            billInfo.setNumber((h2.S(billInfo.getNumber()) - h2.S(strReplace2)) + "");
                                        }
                                    } else if (str5.equals("Item description") && i8 < list.size() - 1) {
                                        billInfo.setRemark(list.get(i8 + 1).replaceFirst("Item description", ""));
                                        if (billInfo.getRemark().contains("余额宝") && billInfo.getRemark().contains("收益发放")) {
                                            billInfo.setAsset("余额宝");
                                        }
                                    } else if (!str5.equals("Order Details") || i8 >= list.size() - 1) {
                                        context2 = context;
                                        if (str5.equals("交易详情") && i8 < list.size() - 1) {
                                            String str6 = list.get(i8 + 1);
                                            if (!TextUtils.isEmpty(str6) && !str6.equals("更多") && !str6.equals("推荐服务") && !str6.equals("服务推荐") && i8 < list.size() - 2) {
                                                String str7 = list.get(i8 + 2);
                                                if (!TextUtils.isEmpty(str7)) {
                                                    billInfo.setRemark(str7);
                                                    SelectToSpeakService.E(context2, billInfo.getRemark());
                                                }
                                            }
                                        } else if (str5.equals("Payment Instructions") && i8 < list.size() - 1) {
                                            billInfo.setRemark(list.get(i8 + 1).replaceFirst("Payment Instructions", ""));
                                        } else if (str5.equals("充值说明") && i8 < list.size() - 1) {
                                            billInfo.setRemark(list.get(i8 + 1).replaceFirst("充值说明", ""));
                                        } else if (str5.equals("付款备注") && i8 < list.size() - 1) {
                                            billInfo.setRemark(list.get(i8 + 1).replaceFirst("付款备注", ""));
                                        } else if (str5.equals("红包说明") && i8 < list.size() - 1) {
                                            billInfo.setRemark(list.get(i8 + 1).replaceFirst("红包说明", ""));
                                        } else if (str5.equals("Reason") && i8 < list.size() - 1) {
                                            billInfo.setRemark(list.get(i8 + 1).replaceFirst("Reason", ""));
                                        } else if (str5.equals("Transfer notes") && i8 < list.size() - 1) {
                                            int i9 = i8 + 1;
                                            if (!"Transfer".equals(list.get(i9))) {
                                                billInfo.setRemark(list.get(i9));
                                            } else if (billInfo.isIncome()) {
                                                billInfo.setRemark("收到" + billInfo.getShopName() + "转账");
                                            } else {
                                                billInfo.setRemark("转账给" + billInfo.getShopName());
                                            }
                                            if (billInfo.isIncome()) {
                                                billInfo.setShopName("收到" + billInfo.getShopName() + "转账");
                                            } else {
                                                billInfo.setShopName("转账给" + billInfo.getShopName());
                                            }
                                        } else if (TextUtils.isEmpty(billInfo.getDiscount()) && (((this.f13679f && str5.startsWith("-￥")) || str5.equals("碰一下支付立减") || str5.equals("支付宝随机立减") || str5.equals("碰一下立减") || str5.equals("碰一下共减") || str5.equals("视频红包") || str5.equals("优惠")) && i8 < list.size() - 1)) {
                                            String str8 = list.get(i8 + 1);
                                            if (str8.equals("¥") && i8 < list.size() - 2) {
                                                str8 = list.get(i8 + 2);
                                            }
                                            String strReplace3 = str8.replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("元", "").replace("-", "");
                                            if (h2.J(strReplace3)) {
                                                billInfo.setDiscount(strReplace3);
                                            }
                                        } else if (this.a != 7 && str5.equals("订单金额") && i8 < list.size() - 1) {
                                            String strReplace4 = list.get(i8 + 1).replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("元", "").replace("-", "");
                                            if (h2.J(strReplace4) && !TextUtils.isEmpty(billInfo.getNumber())) {
                                                billInfo.setDiscount(h2.t(h2.S(strReplace4) - h2.S(billInfo.getNumber())));
                                            }
                                        }
                                    } else {
                                        int i10 = i8 + 1;
                                        if (!str3.equals(list.get(i10))) {
                                            billInfo.setRemark(list.get(i10));
                                        }
                                        if ("商品图片".equals(billInfo.getRemark())) {
                                            billInfo.setRemark(list.get(i8 + 2));
                                            context2 = context;
                                            SelectToSpeakService.E(context2, billInfo.getRemark());
                                        }
                                    }
                                } else {
                                    String strReplace5 = list.get(i8 + 1).replace("Order time", "");
                                    if (strReplace5.contains("-")) {
                                        billInfo.setTime(t0.X0(strReplace5, "yyyy-MM-dd HH:mm:ss"));
                                    } else if (strReplace5.contains("年")) {
                                        billInfo.setTime(t0.X0(strReplace5, "yyyy年MM月dd日 HH:mm:ss"));
                                    }
                                }
                                context2 = context;
                            } else {
                                String strReplace6 = str5.replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("支出", "").replace("元", "");
                                if (!h2.J(strReplace6) && i8 < list.size() - 1) {
                                    str5 = list.get(i8 + 1);
                                    strReplace6 = str5.replace("￥", "").replace("¥", "").replace(",", "").replace("+", "");
                                }
                                if (h2.J(strReplace6)) {
                                    billInfo.setNumber(Math.abs(h2.S(strReplace6)) + "");
                                    if (str5.contains("+")) {
                                        billInfo.setRemark("支付宝收款");
                                        billInfo.setShopName(billInfo.getRemark());
                                        billInfo.setIncome(true);
                                    }
                                }
                                if (TextUtils.isEmpty(billInfo.getRemark()) && i8 > 0) {
                                    billInfo.setRemark(list.get(i8 - 1));
                                    billInfo.setShopName(billInfo.getRemark());
                                }
                            }
                            context2 = context;
                        }
                        i8++;
                        billInfo2 = billInfo;
                        context3 = context2;
                        str4 = str2;
                    }
                    if (i8 > 0) {
                        int i11 = i8 - 1;
                        String strReplace7 = list.get(i11).replace("￥", "").replace("¥", "").replace(",", "").replace("+", "").replace("元", "").replace("-", "").replace("支出", "");
                        if (h2.J(strReplace7)) {
                            billInfo = billInfo3;
                            billInfo.setNumber(strReplace7);
                            if (list.get(i11).contains("+") || (!(list.get(i11).contains("-") || list.get(i11).contains("支出")) || str5.equals("退回成功"))) {
                                i7 = 1;
                                billInfo.setIncome(true);
                            } else {
                                i7 = 1;
                            }
                            if (str5.contains("扣款成功")) {
                                billInfo.setIncome(false);
                            }
                            if (i8 > i7) {
                                billInfo.setRemark(list.get(i8 - 2));
                            }
                            if (str5.equals("退款成功")) {
                                billInfo.setRemark("退款-来自" + billInfo.getRemark());
                                billInfo.setIncome(true);
                            }
                            billInfo.setShopName(billInfo.getRemark());
                        } else {
                            billInfo = billInfo3;
                            if (strReplace7.equals("NaN")) {
                                billInfo.setNumber("0");
                            }
                        }
                    } else {
                        billInfo = billInfo3;
                        if (!str5.equals("收款理由")) {
                            if (TextUtils.isEmpty(billInfo.getNumber())) {
                            }
                            if (str5.equals("Payment method")) {
                                billInfo.setAsset(list.get(i8 + 1));
                                if ("帮助".equals(billInfo.getAsset())) {
                                    billInfo.setAsset(list.get(i8 + 2));
                                }
                                if (TextUtils.isEmpty(billInfo.getAsset())) {
                                }
                            } else {
                                billInfo.setAsset(list.get(i8 + 1));
                                if ("帮助".equals(billInfo.getAsset())) {
                                    billInfo.setAsset(list.get(i8 + 2));
                                }
                                if (TextUtils.isEmpty(billInfo.getAsset())) {
                                }
                            }
                            context2 = context;
                        } else {
                            if (TextUtils.isEmpty(billInfo.getNumber())) {
                            }
                            if (str5.equals("Payment method")) {
                                billInfo.setAsset(list.get(i8 + 1));
                                if ("帮助".equals(billInfo.getAsset())) {
                                    billInfo.setAsset(list.get(i8 + 2));
                                }
                                if (TextUtils.isEmpty(billInfo.getAsset())) {
                                }
                            } else {
                                billInfo.setAsset(list.get(i8 + 1));
                                if ("帮助".equals(billInfo.getAsset())) {
                                    billInfo.setAsset(list.get(i8 + 2));
                                }
                                if (TextUtils.isEmpty(billInfo.getAsset())) {
                                }
                            }
                            context2 = context;
                        }
                        i8++;
                        billInfo2 = billInfo;
                        context3 = context2;
                        str4 = str2;
                    }
                    context2 = context;
                } else {
                    int i12 = i8 + 1;
                    String strReplace8 = list.get(i12).replace("￥", "").replace("¥", "").replace("元", "").replace(",", "").replace("支出", "");
                    if (h2.J(strReplace8)) {
                        billInfo2.setNumber(strReplace8);
                    }
                    String strReplace9 = list.get(i8 + 2).replace("￥", "").replace("¥", "").replace("元", "").replace(",", "").replace("支出", "");
                    if (!h2.J(strReplace8) && h2.J(strReplace9)) {
                        billInfo2.setNumber(strReplace9);
                        if (this.f13679f) {
                            if (("¥".equals(list.get(i12)) || "￥".equals(list.get(i12))) && i8 < list.size() - 3) {
                                billInfo2.setRemark(list.get(i8 + 3));
                            } else {
                                billInfo2.setRemark(strReplace8);
                            }
                            billInfo2.setShopName(billInfo2.getRemark());
                        }
                    } else if (!strReplace9.equals("Payment method")) {
                        if (!this.f13679f) {
                            int iD = d(list, "￥", false);
                            String strReplace10 = iD > 0 ? list.get(iD).replace("￥", "").replace("¥", "").replace(",", "") : null;
                            if (iD > 1 && !TextUtils.isEmpty(strReplace10) && h2.J(strReplace10)) {
                                strReplace9 = list.get(iD - 1);
                                if (h2.J(billInfo2.getNumber())) {
                                    billInfo2.setDiscount(h2.t(h2.S(strReplace10) - h2.S(billInfo2.getNumber())));
                                }
                            }
                        }
                        billInfo2.setRemark(strReplace9);
                        billInfo2.setShopName(strReplace9);
                    }
                    context2 = context;
                    billInfo = billInfo2;
                }
            } else {
                billInfo2.setRemark(list.get(i8 + 1));
                billInfo2.setShopName(billInfo2.getRemark());
                context2 = context3;
                billInfo = billInfo2;
                str2 = str4;
            }
            i8++;
            billInfo2 = billInfo;
            context3 = context2;
            str4 = str2;
        }
        BillInfo billInfo4 = billInfo2;
        String str9 = str4;
        if (!TextUtils.isEmpty(billInfo4.getNumber()) && TextUtils.isEmpty(billInfo4.getRemark())) {
            if (billInfo4.isIncome()) {
                billInfo4.setRemark("支付宝收款");
                billInfo4.setShopName(billInfo4.getRemark());
            } else {
                billInfo4.setRemark("支付宝付款");
                billInfo4.setShopName(billInfo4.getRemark());
            }
        }
        if (billInfo4.isIncome() && TextUtils.isEmpty(billInfo4.getAsset())) {
            str = str9;
            billInfo4.setAsset(str);
        } else {
            str = str9;
        }
        if (!TextUtils.isEmpty(billInfo4.getAsset())) {
            billInfo4.setAsset(billInfo4.getAsset().trim());
        }
        if ("账户余额".equals(billInfo4.getAsset()) || "余额".equals(billInfo4.getAsset()) || "可用余额".equals(billInfo4.getAsset())) {
            billInfo4.setAsset(str);
        }
        if (TextUtils.isEmpty(billInfo4.getRemark()) || TextUtils.isEmpty(billInfo4.getNumber())) {
            return null;
        }
        return billInfo4;
    }

    public BillInfo t(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setTransfer(true);
        billInfo.setOrigin("支付宝");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.equals("实付金额") && i6 < list.size() - 2) {
                String strReplace = list.get(i6 + 2).replace("¥", "").replace(",", "").replace("支出", "").replace("元", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                }
            } else if (str.equals("还款信用卡") && i6 < list.size() - 1) {
                String str2 = list.get(i6 + 1);
                billInfo.setToAsset((str2.contains("(") && str2.contains(")")) ? str2.substring(str2.indexOf("(") + 1, str2.lastIndexOf(")")).replace(".", "") : str2.replace(".", ""));
                billInfo.setFromAsset("支付宝");
            }
        }
        if (!TextUtils.isEmpty(billInfo.getFromAsset())) {
            billInfo.setFromAsset(billInfo.getFromAsset().trim());
        }
        if (!TextUtils.isEmpty(billInfo.getToAsset())) {
            billInfo.setToAsset(billInfo.getToAsset().trim());
        }
        if ("账户余额".equals(billInfo.getFromAsset()) || "余额".equals(billInfo.getFromAsset())) {
            billInfo.setFromAsset("支付宝");
        }
        if ("账户余额".equals(billInfo.getToAsset()) || "余额".equals(billInfo.getToAsset())) {
            billInfo.setToAsset("支付宝");
        }
        if (TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    /* JADX WARN: Code duplicated, block: B:168:0x03eb A[PHI: r0
      0x03eb: PHI (r0v50 java.lang.String) = (r0v49 java.lang.String), (r0v53 java.lang.String) binds: [B:166:0x03dc, B:162:0x03d1] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:182:0x0441  */
    /* JADX WARN: Code duplicated, block: B:184:0x044a  */
    /* JADX WARN: Code duplicated, block: B:186:0x0452  */
    /* JADX WARN: Code duplicated, block: B:214:0x045d A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:59:0x012c A[PHI: r0 r20
      0x012c: PHI (r0v25 java.lang.String) =
      (r0v26 java.lang.String)
      (r0v49 java.lang.String)
      (r0v50 java.lang.String)
      (r0v50 java.lang.String)
      (r0v50 java.lang.String)
     binds: [B:58:0x012a, B:167:0x03de, B:176:0x0419, B:178:0x0421, B:180:0x042b] A[DONT_GENERATE, DONT_INLINE]
      0x012c: PHI (r20v2 java.lang.String) =
      (r20v3 java.lang.String)
      (r20v6 java.lang.String)
      (r20v6 java.lang.String)
      (r20v6 java.lang.String)
      (r20v6 java.lang.String)
     binds: [B:58:0x012a, B:167:0x03de, B:176:0x0419, B:178:0x0421, B:180:0x042b] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:60:0x012d A[PHI: r0 r4 r20
      0x012d: PHI (r0v24 java.lang.String) = (r0v25 java.lang.String), (r0v50 java.lang.String), (r0v50 java.lang.String) binds: [B:59:0x012c, B:183:0x0448, B:181:0x042d] A[DONT_GENERATE, DONT_INLINE]
      0x012d: PHI (r4v9 boolean) = (r4v10 boolean), (r4v17 boolean), (r4v26 boolean) binds: [B:59:0x012c, B:183:0x0448, B:181:0x042d] A[DONT_GENERATE, DONT_INLINE]
      0x012d: PHI (r20v1 java.lang.String) = (r20v2 java.lang.String), (r20v6 java.lang.String), (r20v6 java.lang.String) binds: [B:59:0x012c, B:183:0x0448, B:181:0x042d] A[DONT_GENERATE, DONT_INLINE]] */
    public BillInfo u(List<String> list) {
        String str;
        String str2;
        boolean z5;
        boolean z6;
        String str3;
        int i6;
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setTransfer(true);
        String str4 = "支付宝";
        billInfo.setOrigin("支付宝");
        String str5 = "充值成功";
        String str6 = "提现金额";
        String str7 = "充值说明";
        if (a(list, "充值成功", true) || a(list, "余额转入", true) || a(list, "充值说明", true)) {
            billInfo.setToAsset("支付宝");
        } else if (a(list, "提现金额", true) || a(list, "提现说明", true) || a(list, "提现成功", true)) {
            billInfo.setFromAsset("支付宝");
        } else if (a(list, "网商银行转账", true)) {
            billInfo.setToAsset("网商银行余利宝");
        } else if (a(list, "余额宝-单次转入", true)) {
            billInfo.setToAsset("余额宝");
        } else if (a(list, "余额宝-转出到余额", true)) {
            billInfo.setFromAsset("余额宝");
            billInfo.setToAsset("支付宝");
        } else if (a(list, "开始计算收益", true) && a(list, "转入成功", false)) {
            billInfo.setToAsset("余额宝");
        } else if (a(list, "转出成功", true)) {
            billInfo.setFromAsset("余额宝");
        }
        int i7 = 0;
        while (i7 < list.size()) {
            String str8 = list.get(i7);
            String str9 = str6;
            String str10 = str4;
            String str11 = str7;
            if ((str8.equals(str6) || str8.equals(str5) || str8.equals("转入成功")) && i7 < list.size() - 1) {
                String strReplace = list.get(i7 + 1).replace("¥", "").replace(",", "").replace("支出", "").replace("元", "");
                if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                } else if (strReplace.equals("NaN")) {
                    billInfo.setNumber("0");
                }
            } else {
                if (str8.contains("提现金额¥")) {
                    String strReplace2 = str8.replace("提现金额¥", "").replace(",", "").replace("支出", "").replace("元", "");
                    if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace2)) {
                        billInfo.setNumber(Math.abs(h2.S(strReplace2)) + "");
                    } else if (strReplace2.equals("NaN")) {
                        billInfo.setNumber("0");
                    }
                } else {
                    str2 = str5;
                    if (str8.equals("转出成功") && i7 < list.size() - 2 && "¥".equals(list.get(i7 + 1))) {
                        String strReplace3 = list.get(i7 + 2).replace(",", "");
                        if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace3)) {
                            billInfo.setNumber(Math.abs(h2.S(strReplace3)) + "");
                        } else if (strReplace3.equals("NaN")) {
                            billInfo.setNumber("0");
                        }
                    } else if (str8.startsWith("成功转出") && str8.contains("元至")) {
                        String strReplace4 = str8.substring(str8.indexOf("成功转出") + 4, str8.indexOf("元至")).replace(",", "");
                        if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace4)) {
                            billInfo.setNumber(Math.abs(h2.S(strReplace4)) + "");
                        } else if (strReplace4.equals("NaN")) {
                            billInfo.setNumber("0");
                        }
                        String strReplace5 = str8.substring(str8.indexOf("元至") + 1).replace("。", "");
                        if ("支付宝账户余额".equals(strReplace5)) {
                            strReplace5 = str10;
                        }
                        billInfo.setToAsset(strReplace5);
                    } else if ((str8.equals("交易成功") || str8.equals("还款成功")) && (i6 = i7 - 1) >= 0) {
                        String strReplace6 = list.get(i6).replace("¥", "").replace(",", "").replace("支出", "").replace("元", "");
                        if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace6)) {
                            billInfo.setNumber(Math.abs(h2.S(strReplace6)) + "");
                        } else if (strReplace6.equals("NaN")) {
                            billInfo.setNumber("0");
                        }
                    } else if (str8.contains("成功转入")) {
                        String strReplace7 = str8.replace("成功转入", "").replace(",", "").replace("元", "");
                        if (TextUtils.isEmpty(billInfo.getNumber()) && h2.J(strReplace7)) {
                            billInfo.setNumber(Math.abs(h2.S(strReplace7)) + "");
                        } else if (strReplace7.equals("NaN")) {
                            billInfo.setNumber("0");
                        }
                    } else if ((str8.equals("到账银行卡") || str8.equals("提现到") || str8.equals("还款到") || str8.equals("到账账户")) && i7 < list.size() - 1) {
                        billInfo.setToAsset(list.get(i7 + 1));
                    } else if ((str8.equals("付款方式") || str8.equals("对方账户")) && i7 < list.size() - 1) {
                        billInfo.setFromAsset(list.get(i7 + 1));
                    } else if (str8.equals("服务费") && i7 < list.size() - 1) {
                        String strReplace8 = list.get(i7 + 1).replace("¥", "").replace(",", "");
                        if (h2.J(strReplace8)) {
                            billInfo.setServiceCharge(Math.abs(h2.S(strReplace8)));
                        }
                    } else if (!str8.equals("创建时间") || i7 >= list.size() - 1) {
                        if (str8.equals("提现说明") || str8.equals("转账备注")) {
                            str3 = str11;
                        } else {
                            str3 = str11;
                            if (!str8.equals(str3) && !str8.equals("商品说明")) {
                                if (str8.equals("转出说明") || i7 >= list.size() - 1) {
                                    z6 = false;
                                    if (str8.equals("转入账户")) {
                                        z5 = true;
                                        if (i7 < list.size() - 1) {
                                            billInfo.setToAsset(list.get(i7 + 1));
                                        }
                                    }
                                } else {
                                    String str12 = list.get(i7 + 1);
                                    if (str12.contains("-")) {
                                        str12.substring(0, str12.indexOf("-"));
                                    }
                                    if (TextUtils.isEmpty(str12) || !str12.contains("转出到")) {
                                        z6 = false;
                                    } else {
                                        String[] strArrSplit = str12.split("转出到");
                                        if (strArrSplit.length == 2) {
                                            z6 = false;
                                            billInfo.setFromAsset(strArrSplit[0].replace("转出说明", ""));
                                            billInfo.setToAsset(strArrSplit[1]);
                                        } else {
                                            z6 = false;
                                        }
                                    }
                                }
                                z5 = true;
                            }
                        }
                        if (i7 < list.size() - 1) {
                            billInfo.setRemark(list.get(i7 + 1));
                        } else if (str8.equals("转出说明")) {
                            z6 = false;
                            if (str8.equals("转入账户")) {
                                z5 = true;
                                if (i7 < list.size() - 1) {
                                    billInfo.setToAsset(list.get(i7 + 1));
                                }
                            } else {
                                z5 = true;
                            }
                        } else {
                            z6 = false;
                            if (str8.equals("转入账户")) {
                                z5 = true;
                                if (i7 < list.size() - 1) {
                                    billInfo.setToAsset(list.get(i7 + 1));
                                }
                            } else {
                                z5 = true;
                            }
                        }
                        z6 = false;
                        z5 = true;
                    } else {
                        billInfo.setTime(t0.X0(list.get(i7 + 1).replace("创建时间", ""), "yyyy-MM-dd HH:mm:ss"));
                    }
                    str3 = str11;
                    z6 = false;
                    z5 = true;
                }
                i7++;
                str7 = str3;
                str6 = str9;
                str4 = str10;
                str5 = str2;
            }
            str2 = str5;
            str3 = str11;
            z6 = false;
            z5 = true;
            i7++;
            str7 = str3;
            str6 = str9;
            str4 = str10;
            str5 = str2;
        }
        String str13 = str4;
        if (!TextUtils.isEmpty(billInfo.getFromAsset())) {
            billInfo.setFromAsset(billInfo.getFromAsset().trim());
        }
        if (!TextUtils.isEmpty(billInfo.getToAsset())) {
            billInfo.setToAsset(billInfo.getToAsset().trim());
        }
        if ("账户余额".equals(billInfo.getFromAsset()) || "余额".equals(billInfo.getFromAsset())) {
            str = str13;
            billInfo.setFromAsset(str);
        } else {
            str = str13;
        }
        if ("账户余额".equals(billInfo.getToAsset()) || "余额".equals(billInfo.getToAsset())) {
            billInfo.setToAsset(str);
        }
        if (TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo v(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("支付宝");
        if (s.i()) {
            if (b2.m0(this.f13678e, System.currentTimeMillis())) {
                billInfo.setTime(this.f13678e);
            } else {
                this.f13678e = System.currentTimeMillis();
            }
        }
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.equals("收款方") && i6 < list.size() - 1) {
                billInfo.setRemark("转账给" + list.get(i6 + 1));
                billInfo.setShopName(billInfo.getRemark());
            } else if (str.equals("转账成功") && i6 < list.size() - 2) {
                String strReplace = list.get(i6 + 1).replace("￥", "").replace("¥", "").replace("元", "").replace(",", "");
                if (!h2.J(strReplace)) {
                    strReplace = list.get(i6 + 2).replace("￥", "").replace("¥", "").replace("元", "").replace(",", "");
                }
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                } else if (strReplace.equals("NaN")) {
                    billInfo.setNumber("0");
                }
            } else if (str.equals("付款方式") && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1));
            }
        }
        if (!TextUtils.isEmpty(billInfo.getRemark()) && !TextUtils.isEmpty(billInfo.getAsset()) && TextUtils.isEmpty(billInfo.getRemark())) {
            billInfo.setRemark("支付宝转账");
            billInfo.setShopName(billInfo.getRemark());
        }
        if (!TextUtils.isEmpty(billInfo.getAsset())) {
            billInfo.setAsset(billInfo.getAsset().trim());
        }
        if ("账户余额".equals(billInfo.getAsset()) || "余额".equals(billInfo.getAsset())) {
            billInfo.setAsset("支付宝");
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public void w(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        List<String> listG;
        HashMap<String, Boolean> map;
        boolean z5;
        int iIndexOf;
        String str2 = "确认即付款";
        if (!str.equals("com.eg.android.AlipayGphone.AlipayLogin")) {
            if ((!this.f13679f && str.equals("com.alipay.android.msp.ui.views.MspContainerActivity")) || str.equals("com.alipay.android.msp.ui.views.MspUniRenderActivity") || str.equals("com.alipay.android.phone.discovery.envelope.get.SnsCouponDetailActivity")) {
                this.b = true;
                listG = null;
                map = null;
            } else {
                if (str.equals("com.alipay.android.phone.nfcpay.ui.NfcPayActivity") || str.equals("com.alipay.android.phone.businesscommon.ucdp.nfc.activity.NResPageActivity")) {
                    this.f13679f = true;
                } else if (str.equals("com.alipay.mobile.scan.as.main.MainCaptureActivity") || str.equals("com.alipay.mobile.onsitepay.merge.OnsitepayActivity")) {
                    this.f13679f = false;
                }
                if (accessibilityNodeInfo != null) {
                    listG = g(accessibilityNodeInfo);
                    map = new HashMap<>();
                    Iterator<String> it = listG.iterator();
                    while (it.hasNext()) {
                        map.put(it.next(), Boolean.TRUE);
                    }
                    if (!this.f13679f && map.containsKey("碰一下，支付更便捷") && map.containsKey("确认即付款")) {
                        this.f13679f = true;
                    }
                    if (!this.f13679f && str.equals("com.alipay.mobile.quinox.SchemeLauncherActivity") && map.containsKey("使用说明")) {
                        this.f13679f = true;
                    }
                    if (map.containsKey("向商家付钱") || (!this.f13679f && map.containsKey("支付成功") && map.containsKey("交易方式"))) {
                        z5 = true;
                    } else {
                        if ((this.f13679f || (!(map.containsKey("支付成功") || map.containsKey("代付成功") || map.containsKey("转账成功")) || (!(map.containsKey("付款方式") || map.containsKey("交易方式") || map.containsKey("完成")) || map.containsKey("账单详情") || map.containsKey("支付时间")))) && ((!this.f13679f || !map.containsKey("支付成功") || map.containsKey("账单详情") || map.containsKey("支付时间") || map.containsKey("计算中") || !((map.containsKey("付款方式") || map.containsKey("交易方式")) && (map.containsKey("¥") || map.containsKey("￥")))) && !(map.containsKey("充值成功") && map.containsKey("话费已到账")))) {
                            str2 = "确认即付款";
                            if ((map.containsKey("结果详情") && map.containsKey("提现金额")) || ((map.containsKey("充值成功") && !map.containsKey("充值号码") && !map.containsKey("手机充值") && !map.containsKey("话费已到账")) || map.containsKey("提现成功") || map.containsKey("转入成功") || map.containsKey("转出成功") || (map.containsKey("创建时间") && !map.containsKey("充值号码") && !map.containsKey("手机充值") && !map.containsKey("话费已到账") && b(map, this.f13680g)))) {
                                this.a = 9;
                                this.b = true;
                            } else if (map.containsKey("还款信用卡") && map.containsKey("银行处理中")) {
                                this.a = 10;
                                this.b = true;
                            } else if ((map.containsKey("账单详情") && !map.containsKey("交易详情") && !map.containsKey("商品图片")) || ((map.containsKey("账单详情") && map.containsKey("退款成功")) || map.containsKey("余额明细详情") || (map.containsKey("交易成功") && !map.containsKey("交易详情") && !map.containsKey("商品图片") && map.containsKey("付款方式") && map.containsKey("支付时间")))) {
                                this.a = 6;
                                this.b = true;
                            } else if ((map.containsKey("账单管理") || map.containsKey("账单详情")) && (map.containsKey("交易详情") || map.containsKey("商品图片"))) {
                                this.a = 6;
                                this.b = true;
                                if (map.containsKey("交易详情") && (iIndexOf = listG.indexOf("交易详情")) > 0 && iIndexOf < listG.size() - 1) {
                                    int i6 = iIndexOf + 1;
                                    if ("更多".equals(listG.get(i6)) || "推荐服务".equals(listG.get(i6)) || "服务推荐".equals(listG.get(i6))) {
                                        this.b = false;
                                    }
                                }
                            } else if (map.containsKey("收款时间") && map.containsKey("已收款")) {
                                this.a = 7;
                                this.b = true;
                            } else if ((map.containsKey("红包详情") || map.containsKey("领取成功，已存入余额")) && map.containsKey("查看红包记录")) {
                                this.a = 6;
                                this.b = true;
                            } else if (map.containsKey("开始计算收益") && map.containsKey("收益到账") && map.containsKey("付款方式")) {
                                this.a = 9;
                                this.b = true;
                            }
                        } else {
                            z5 = true;
                        }
                        if (s.i() && ((map.containsKey("优先使用此付款方式") && map.containsKey("向商家付款")) || ((map.containsKey("确认付款") && map.containsKey("使用密码")) || ((map.containsKey("更改付款方式") && map.containsKey("极速付款")) || ((map.containsKey("请输入支付密码") && map.containsKey("使用指纹")) || ((map.containsKey("扫二维码/条码/小程序码") && map.containsKey("收付款")) || this.f13679f)))))) {
                            this.f13678e = System.currentTimeMillis();
                            y.l("sssss-time", "time:" + this.f13678e);
                        }
                    }
                    this.a = 2;
                    this.b = z5;
                    if (s.i()) {
                        this.f13678e = System.currentTimeMillis();
                        y.l("sssss-time", "time:" + this.f13678e);
                    }
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
            if (s.i() && ((map.containsKey("优先使用此付款方式") && map.containsKey("向商家付款")) || ((map.containsKey("确认付款") && map.containsKey("使用密码")) || ((map.containsKey("更改付款方式") && map.containsKey("极速付款")) || ((map.containsKey("请输入支付密码") && map.containsKey("使用指纹")) || ((map.containsKey("扫二维码/条码/小程序码") && map.containsKey("收付款")) || (map.containsKey("碰一下，支付更便捷") && map.containsKey(str2)))))))) {
                this.f13678e = System.currentTimeMillis();
                y.l("sssss-time", "time:" + this.f13678e);
            }
            y(context, listG, map);
            return;
        }
        this.a = 0;
        this.b = false;
        this.f13679f = false;
        listG = null;
        map = null;
        if (this.b) {
        }
    }

    public BillInfo x(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("支付宝");
        billInfo.setAsset("支付宝");
        billInfo.setIncome(true);
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.contains("元") && i6 > 2) {
                String strReplace = str.replace("元", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                    billInfo.setRemark(list.get(i6 - 3).replace("送你的红包", "") + "的红包");
                    billInfo.setShopName(billInfo.getRemark());
                    return billInfo;
                }
            }
        }
        return null;
    }

    public BillInfo z(List<String> list) {
        int iIndexOf;
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("支付宝");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.contains("红包金额")) {
                int iIndexOf2 = str.indexOf("元");
                if (iIndexOf2 > 4) {
                    String strReplace = str.substring(0, iIndexOf2).replace("红包金额", "").replace(",", "");
                    if (h2.J(strReplace)) {
                        billInfo.setNumber(strReplace);
                        billInfo.setRemark("发送支付宝红包");
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
                        billInfo.setRemark("发送支付宝红包");
                        billInfo.setShopName(billInfo.getRemark());
                        return billInfo;
                    }
                } else {
                    continue;
                }
            } else if (str.contains("已领取") && (iIndexOf = str.indexOf("元")) > 4 && str.contains("/")) {
                String strReplace3 = str.substring(str.lastIndexOf("/") + 1, iIndexOf).replace(",", "");
                if (h2.J(strReplace3)) {
                    billInfo.setNumber(strReplace3);
                    billInfo.setRemark("发送支付宝红包");
                    billInfo.setShopName(billInfo.getRemark());
                    return billInfo;
                }
            }
        }
        return null;
    }
}
