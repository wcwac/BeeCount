// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 fe57bdbc2c5ec0fe31b6d1369455e77a5ee2b668256f7f4d01d50be25aaf2785
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
public class k extends c implements x1.a {

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private static final int f13726j = 1;

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    private static final int f13727k = 2;

    /* JADX INFO: renamed from: l, reason: collision with root package name */
    private static final int f13728l = 3;

    /* JADX INFO: renamed from: m, reason: collision with root package name */
    private static final int f13729m = 4;

    /* JADX INFO: renamed from: n, reason: collision with root package name */
    private static k f13730n;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private String f13731e;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13732f;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private String f13733g;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private String f13734h;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private boolean f13735i = false;

    private void q(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        List<String> listG;
        int iE;
        if (!this.f13735i || accessibilityNodeInfo == null) {
            listG = null;
        } else {
            listG = g(accessibilityNodeInfo);
            int i6 = 1;
            if ((listG.contains("创建时间") || listG.contains("退款受理时间") || listG.contains("下单时间")) && (listG.contains("退款成功") || listG.contains("支付成功") || listG.contains("先用后付自动扣款成功") || (listG.contains("支付方式") && listG.contains("其他账单服务")))) {
                this.a = 4;
                this.b = true;
            } else if (a(listG, "交易成功 ¥", false)) {
                int iF = f(listG, "交易成功 ¥", false);
                if (iF >= 0) {
                    String strReplace = listG.get(iF).replace("交易成功 ¥", "").replace(",", "");
                    if (h2.J(strReplace)) {
                        this.f13733g = strReplace;
                    }
                    if (iF < listG.size() - 1) {
                        String str2 = listG.get(iF + 1);
                        this.f13731e = str2;
                        if (str2.contains("已优惠¥")) {
                            String str3 = this.f13731e;
                            String strReplace2 = str3.substring(str3.indexOf("已优惠¥")).replace("已优惠¥", "").replace(",", "");
                            if (h2.J(strReplace2)) {
                                this.f13734h = strReplace2;
                            }
                            String str4 = this.f13731e;
                            this.f13731e = str4.substring(0, str4.indexOf("已优惠"));
                        }
                    }
                    y.l("sssss", "asset:" + this.f13731e, "num:" + this.f13733g, "discount:" + this.f13734h);
                }
                this.a = 1;
                this.b = true;
            } else if (a(listG, "支付成功 ¥", false)) {
                int iD = d(listG, "支付成功 ¥", false);
                if (iD >= 0) {
                    String strReplace3 = listG.get(iD).replace("支付成功 ¥", "").replace(",", "");
                    if (h2.J(strReplace3)) {
                        this.f13733g = strReplace3;
                    }
                    int iD2 = d(listG, "➤", true);
                    if (iD2 >= 0 && iD2 < listG.size() - 1) {
                        this.f13731e = listG.get(iD2 + 1);
                    } else if (iD < listG.size() - 1) {
                        this.f13731e = listG.get(iD + 1);
                    }
                    y.l("sssss", "asset:" + this.f13731e, "num:" + this.f13733g);
                }
                this.a = 1;
                this.b = true;
            } else if (listG.contains("交易详情") && a(listG, "交易时间", false)) {
                this.a = 3;
                this.b = true;
            } else if (listG.contains("提交订单") && listG.contains("外卖配送")) {
                int iIndexOf = listG.indexOf("点餐请适量，环保又健康");
                if (iIndexOf > 0 && iIndexOf < listG.size() - 2 && (iE = e(listG, "¥", iIndexOf, true)) > 0) {
                    this.f13732f = listG.get(iE - 1);
                }
                int iIndexOf2 = listG.indexOf("合计");
                if (iIndexOf2 > 0) {
                    if (iIndexOf2 < listG.size() - 3) {
                        for (int i7 = 3; i6 < i7; i7 = 3) {
                            String strReplace4 = listG.get(iIndexOf2 + i6).replace("¥", "").replace(",", "");
                            if (h2.J(strReplace4)) {
                                this.f13733g = strReplace4;
                                break;
                            }
                            i6++;
                        }
                    }
                }
                y.l("sssss", "asset:" + this.f13731e, "remark:" + this.f13732f, "num:" + this.f13733g);
            } else if (listG.contains("提交订单") || listG.contains("极速支付") || listG.contains("提交月付订单")) {
                if (listG.contains("美团红包")) {
                    int iC = listG.contains("优享送") ? c(listG, "¥", 2, true) : listG.indexOf("¥");
                    if (iC >= 0 && iC < listG.size() - 2) {
                        this.f13732f = listG.get(iC - 1);
                    }
                } else {
                    int iIndexOf3 = listG.indexOf("周一至周日 ");
                    if (iIndexOf3 > 0) {
                        this.f13732f = listG.get(iIndexOf3 - 1);
                    } else {
                        int iIndexOf4 = listG.indexOf("周一至周五 ");
                        if (iIndexOf4 > 0) {
                            this.f13732f = listG.get(iIndexOf4 - 1);
                        } else {
                            int iIndexOf5 = listG.indexOf("过期退");
                            if (iIndexOf5 <= 0 || iIndexOf5 >= listG.size() - 1) {
                                int iIndexOf6 = listG.indexOf("周一至周日 全场通用");
                                if (iIndexOf6 > 0 && iIndexOf6 < listG.size() - 1) {
                                    this.f13732f = listG.get(iIndexOf6 + 1);
                                }
                            } else {
                                this.f13732f = listG.get(iIndexOf5 + 1);
                            }
                        }
                    }
                }
                int iIndexOf7 = listG.indexOf("合计");
                if (iIndexOf7 < listG.size() - 2) {
                    int i8 = iIndexOf7 + 1;
                    String strReplace5 = "¥".equals(listG.get(i8)) ? listG.get(iIndexOf7 + 2).replace(",", "").replace("¥", "") : listG.get(i8).replace(",", "").replace("¥", "");
                    if (h2.J(strReplace5)) {
                        this.f13733g = strReplace5;
                    }
                }
                if (TextUtils.isEmpty(this.f13733g) && iIndexOf7 < listG.size() - 3) {
                    String strReplace6 = listG.get(iIndexOf7 + 2).replace(",", "").replace("¥", "");
                    if (h2.J(strReplace6)) {
                        this.f13733g = strReplace6;
                    }
                }
                if (TextUtils.isEmpty(this.f13733g) && iIndexOf7 < listG.size() - 4) {
                    String strReplace7 = listG.get(iIndexOf7 + 3).replace(",", "").replace("¥", "");
                    if (h2.J(strReplace7)) {
                        this.f13733g = strReplace7;
                    }
                }
                if (listG.contains("极速支付")) {
                    this.f13731e = "美团";
                } else {
                    int iIndexOf8 = listG.indexOf("支付方式");
                    if (iIndexOf8 >= 0 && iIndexOf8 < listG.size() - 2) {
                        String str5 = listG.get(iIndexOf8 + 1);
                        this.f13731e = str5;
                        if (str5.equals("极速支付")) {
                            this.f13731e = "美团";
                        } else if (this.f13731e.contains("外卖券") && iIndexOf8 < listG.size() - 3) {
                            String str6 = listG.get(iIndexOf8 + 2);
                            this.f13731e = str6;
                            if (str6.equals("极速支付")) {
                                this.f13731e = "美团";
                            }
                        }
                    }
                }
                y.l("sssss", "asset:" + this.f13731e, "remark:" + this.f13732f, "num:" + this.f13733g);
            }
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        w(context, listG);
    }

    private void w(Context context, List<String> list) {
        BillInfo billInfoV;
        a2.a("start find node");
        if (list == null || list.isEmpty()) {
            return;
        }
        int i6 = this.a;
        if (i6 == 1 || i6 == 3 || i6 == 4) {
            a2.a("start check pay:" + this.a);
            int i7 = this.a;
            if (i7 == 1) {
                billInfoV = s(this.f13731e, this.f13732f, this.f13733g);
            } else {
                billInfoV = i7 == 4 ? v(list) : u(list);
            }
            if (billInfoV != null) {
                this.b = false;
                this.f13731e = null;
                this.f13732f = null;
                this.f13733g = null;
                this.f13734h = null;
                billInfoV.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                SelectToSpeakService.l(context, billInfoV, this);
            }
        }
    }

    public static k x() {
        if (f13730n == null) {
            f13730n = new k();
        }
        return f13730n;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
        this.b = false;
        this.f13731e = null;
        this.f13732f = null;
        this.f13733g = null;
    }

    public void r(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        if (str.equals("com.dianping.v1.NovaMainActivity") || str.equals("com.dianping.shopshell.PexusPoiActivity") || str.equals("com.dianping.nova.picasso.DPPicassoBoxActivity")) {
            this.a = 0;
            this.b = false;
            this.f13735i = false;
            this.f13733g = null;
            this.f13731e = null;
            this.f13732f = null;
            this.f13734h = null;
        } else if (str.equals("com.sankuai.waimai.bussiness.order.confirm.OrderConfirmActivity") || str.equals("com.meituan.android.mrn.container.MRNBaseActivity") || str.equals("com.meituan.android.mrn.container.MRNStandardActivity") || str.equals("com.sankuai.waimai.bussiness.order.confirm.OrderConfirmNoTransActivity") || str.equals("com.meituan.android.movie.tradebase.activity.PaySeatActivity") || str.equals("com.sankuai.eh.framework.EHContainerActivity") || str.equals("com.meituan.android.neohybrid.neo.loading.b") || str.equals("com.meituan.android.neohybrid.neo.loading.a") || str.equals("com.meituan.traveltools.mrncontainer.HTMRNBaseActivity")) {
            this.f13735i = true;
        }
        q(context, str, accessibilityNodeInfo);
    }

    public BillInfo s(String str, String str2, String str3) {
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("美团");
        billInfo.setRemark("美团");
        billInfo.setShopName("美团");
        if (!TextUtils.isEmpty(str)) {
            billInfo.setAsset(str);
        }
        if (!TextUtils.isEmpty(str2)) {
            billInfo.setRemark(str2);
        }
        if (!TextUtils.isEmpty(str3)) {
            billInfo.setNumber(str3);
        }
        if (!TextUtils.isEmpty(this.f13734h)) {
            billInfo.setDiscount(this.f13734h);
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public void t(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        if (str.equals("com.meituan.android.food.deal.FoodDealDetailActivity") || str.equals("com.meituan.android.pt.homepage.activity.MainActivity") || str.equals("com.meituan.android.mtgb.business.main.MTGMainActivity") || str.equals("com.sankuai.meituan.search.result.SearchResultActivity") || str.equals("com.sankuai.waimai.business.page.homepage.TakeoutActivity") || str.equals("com.sankuai.waimai.business.restaurant.poicontainer.WMRestaurantActivity") || str.equals("com.sankuai.waimai.business.page.homepage.MainActivity") || str.equals("com.meituan.android.hotel.search.HotelSearchResultActivity") || str.equals("com.meituan.android.hotel.reuse.detail.HotelPoiDetailActivity")) {
            this.a = 0;
            this.b = false;
            this.f13735i = false;
            this.f13733g = null;
            this.f13731e = null;
            this.f13732f = null;
            this.f13734h = null;
        } else if (str.equals("com.sankuai.waimai.bussiness.order.confirm.OrderConfirmActivity") || str.equals("com.meituan.android.mrn.container.MRNBaseActivity") || str.equals("com.meituan.android.mrn.container.MRNStandardActivity") || str.equals("com.sankuai.waimai.bussiness.order.confirm.OrderConfirmNoTransActivity") || str.equals("com.meituan.android.movie.tradebase.activity.PaySeatActivity") || str.equals("com.sankuai.eh.framework.EHContainerActivity") || str.equals("com.meituan.android.neohybrid.neo.loading.b") || str.equals("com.meituan.android.neohybrid.neo.loading.a") || str.equals("com.meituan.traveltools.mrncontainer.HTMRNBaseActivity")) {
            this.f13735i = true;
        }
        q(context, str, accessibilityNodeInfo);
    }

    public BillInfo u(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("美团");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.equals("交易详情") && i6 < list.size() - 4) {
                if ("退款".equals(list.get(i6 + 4))) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("退款-");
                    int i7 = i6 + 2;
                    sb.append(list.get(i7));
                    billInfo.setShopName(sb.toString());
                    billInfo.setRemark("退款-" + list.get(i7));
                } else {
                    int i8 = i6 + 2;
                    billInfo.setShopName(list.get(i8));
                    billInfo.setRemark(list.get(i8));
                }
                int i9 = i6 + 3;
                String strReplace = list.get(i9).replace("+", "").replace("-", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                    if (list.get(i9).contains("+")) {
                        billInfo.setIncome(true);
                    }
                }
            } else if (str.contains("交易时间")) {
                String strReplace2 = str.replace("交易时间", "");
                if (!TextUtils.isEmpty(strReplace2) || i6 >= list.size() - 1) {
                    billInfo.setTime(t0.X0(strReplace2, "yyyy-MM-dd HH:mm:ss"));
                } else {
                    String str2 = list.get(i6 + 1);
                    if (str2.contains(":")) {
                        billInfo.setTime(t0.X0(str2, "yyyy-MM-dd HH:mm:ss"));
                    }
                }
            } else if (str.equals("支付方式") && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1));
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo v(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("美团");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if ((str.equals("支付成功") || str.equals("退款成功") || str.equals("先用后付自动扣款成功") || str.startsWith("已退款")) && i6 > 0) {
                String str2 = list.get(i6 - 1);
                if (str2.contains("+")) {
                    billInfo.setIncome(true);
                }
                String strReplace = str2.replace("+", "").replace("-", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                }
                if (i6 > 1) {
                    billInfo.setRemark(list.get(i6 - 2));
                    billInfo.setShopName(billInfo.getRemark());
                }
            } else if ((str.contains("创建时间") || str.contains("退款受理时间") || str.contains("下单时间")) && i6 < list.size() - 1) {
                billInfo.setTime(t0.X0(list.get(i6 + 1), "yyyy-MM-dd HH:mm:ss"));
            } else if ((str.equals("退款至") || str.equals("支付方式")) && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1));
            } else if (str.equals("支付优惠")) {
                String strReplace2 = list.get(i6 + 1).replace(",", "").replace("-", "").replace(" ", "");
                if (h2.J(strReplace2)) {
                    billInfo.setDiscount(strReplace2);
                }
            } else if (str.equals("订单详情") && i6 < list.size() - 2 && list.get(i6 + 1).startsWith("good-detail")) {
                billInfo.setRemark(list.get(i6 + 2));
            } else if ((str.contains("退款处理完成，微信已入账") || str.contains("点击查看美团月付详情") || str.contains("退款处理完成，支付宝已入账")) && i6 < list.size() - 1) {
                String str3 = list.get(i6 + 1);
                if (t0.X0(str3, "yyyy-MM-dd HH:mm:ss") > 0) {
                    billInfo.setTime(t0.X0(str3, "yyyy-MM-dd HH:mm:ss"));
                }
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }
}
