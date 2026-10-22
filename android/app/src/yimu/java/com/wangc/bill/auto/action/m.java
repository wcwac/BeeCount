// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 324a97f845f3f8f04868c6fd24f973b78222746d248002f1ee7d4a1386b22643
package com.wangc.bill.auto.action;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import com.blankj.utilcode.util.y;
import com.google.android.accessibility.selecttospeak.SelectToSpeakService;
import com.wangc.bill.auto.x1;
import com.wangc.bill.http.entity.BillInfo;
import com.wangc.bill.utils.a2;
import com.wangc.bill.utils.h2;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class m extends c implements x1.a {

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private static final int f13746j = 1;

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    private static m f13747k;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private String f13748e;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13749f;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private String f13750g;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private boolean f13751h = false;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private boolean f13752i = false;

    private void r(Context context, List<String> list, String str) {
        y.l("start find node :" + str);
        if ((list != null && !list.isEmpty()) || str.equals("com.taobao.android.tbabilitykit.pop.StdPopContainerActivity") || str.equals("com.alibaba.android.ultron.vfw.weex2.highPerformance.widget.UltronTradeHybridActivity")) {
            boolean zA = str.equals("com.taobao.android.tbabilitykit.pop.StdPopContainerActivity") || str.equals("com.alibaba.android.ultron.vfw.weex2.highPerformance.widget.UltronTradeHybridActivity") || str.equals("com.taobao.themis.container.app.TMSActivity");
            if (!zA && this.a == 1) {
                zA = a(list, "正在加载中", true) && a(list, "请稍后...", true);
            }
            if (!zA) {
                zA = a(list, "免密支付成功", true);
            }
            if (!zA) {
                zA = a(list, "支付成功", true);
            }
            if (!zA) {
                zA = list.contains("下单成功，商家备餐中") || list.contains("下单成功，商家正在备餐") || list.contains("下单成功，商家已接单") || list.contains("下单成功，商家备货完成") || list.contains("下单成功，等待商家接单") || (list.contains("闪购") && list.contains("返回") && list.contains("ꁭ"));
            }
            if (zA) {
                a2.a("start check pay:" + this.a);
                BillInfo billInfoT = this.a == 1 ? t(this.f13748e, this.f13749f, this.f13750g) : null;
                if (billInfoT != null) {
                    this.b = false;
                    this.f13748e = null;
                    this.f13749f = null;
                    this.f13750g = null;
                    this.f13751h = false;
                    this.f13752i = false;
                    billInfoT.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
                    SelectToSpeakService.l(context, billInfoT, this);
                }
            }
        }
    }

    public static m u() {
        if (f13747k == null) {
            f13747k = new m();
        }
        return f13747k;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
        this.b = false;
        this.f13748e = null;
        this.f13749f = null;
        this.f13750g = null;
    }

    public boolean q(String str, String str2) {
        return str.equals("com.taobao.taobao") && (str2.equals("com.taobao.android.tbabilitykit.pop.StdPopContainerActivity") || str2.equals("com.taobao.android.purchase.aura.TBBuyActivity") || str2.equals("com.taobao.android.sku.widget.SkuDialogFragment$1"));
    }

    public void s(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        List<String> listM;
        if (str.equals("com.taobao.tao.TBMainActivity") || str.equals("com.taobao.tao.welcome.Welcome") || str.equals("com.taobao.search.searchdoor.SearchDoorActivity") || str.equals("com.taobao.search.sf.MainSearchResultActivity") || str.equals("com.taobao.android.detail.alittdetail.TTDetailActivity")) {
            this.a = 0;
            this.b = false;
            this.f13751h = false;
            this.f13752i = false;
        } else if (str.equals("com.taobao.android.sku.widget.a") || str.equals("com.taobao.android.purchase.aura.TBBuyActivity") || str.equals("com.taobao.android.sku.widget.SkuDialogFragment$1") || str.equals("com.taobao.themis.container.app.TMSActivity")) {
            this.f13751h = true;
        }
        if (str.equals("com.taobao.android.detail.wrapper.activity.DetailActivity") || str.equals("com.taobao.android.sku.widget.SkuDialogFragment$1") || str.equals("com.taobao.android.tbsku.TBXSkuActivity")) {
            this.f13752i = true;
        }
        if (!this.f13751h || accessibilityNodeInfo == null) {
            listM = null;
        } else {
            listM = m(accessibilityNodeInfo);
            if (listM.contains("确认付款")) {
                int iIndexOf = listM.indexOf("确认付款");
                if (iIndexOf >= 5) {
                    this.f13748e = listM.get(iIndexOf - 2);
                    String strReplace = listM.get(iIndexOf - 5).replace(",", "");
                    if (h2.J(strReplace)) {
                        this.f13750g = strReplace;
                        this.a = 1;
                        this.b = true;
                        y.l("sssss", "asset:" + this.f13748e, "num:" + this.f13750g);
                    }
                }
            } else if (listM.contains("实付价")) {
                int iIndexOf2 = listM.indexOf("实付价");
                if (iIndexOf2 < listM.size() - 2) {
                    String strReplace2 = listM.get(iIndexOf2 + 2).replace(",", "");
                    this.f13748e = "支付宝";
                    if (h2.J(strReplace2)) {
                        this.f13750g = strReplace2;
                        this.a = 1;
                        y.l("sssss", "asset:" + this.f13748e, "num:" + this.f13750g);
                    }
                }
            } else if (listM.contains("免密支付")) {
                int iIndexOf3 = listM.indexOf("免密支付");
                if (iIndexOf3 < listM.size() - 1) {
                    String strReplace3 = listM.get(iIndexOf3 + 1).replace(",", "").replace("￥", "");
                    if (h2.J(strReplace3)) {
                        this.f13750g = strReplace3;
                        this.f13748e = "支付宝";
                        this.a = 1;
                        y.l("sssss", "num:" + this.f13750g);
                    }
                }
            } else if (str.equals("com.taobao.android.tbabilitykit.pop.StdPopContainerActivity") || str.equals("com.taobao.themis.container.app.TMSActivity") || listM.contains("免密支付成功") || listM.contains("支付成功") || listM.contains("下单成功，商家备餐中") || listM.contains("下单成功，商家正在备餐") || listM.contains("下单成功，商家已接单") || listM.contains("下单成功，商家备货完成") || listM.contains("下单成功，等待商家接单") || (listM.contains("返回") && listM.contains("ꁭ"))) {
                this.a = 1;
                this.b = true;
            } else if (listM.contains("确认订单") || listM.contains("提交订单")) {
                int iD = d(listM, "￥", false);
                if (iD > 0) {
                    String str2 = listM.get(iD - 1);
                    if (("七天无理由退换".equals(str2) || "券后".equals(str2)) && iD > 1) {
                        str2 = listM.get(iD - 2);
                        iD--;
                    }
                    if (!str2.contains(":") || iD <= 1) {
                        this.f13749f = str2;
                    } else {
                        this.f13749f = listM.get(iD - 2);
                    }
                }
                int iIndexOf4 = listM.indexOf("合计:");
                if (iIndexOf4 < 0 || iIndexOf4 >= listM.size() - 1) {
                    int iD2 = d(listM, "合计¥", false);
                    if (iD2 >= 0) {
                        String strReplace4 = listM.get(iD2).replace("合计¥", "").replace(",", "");
                        if (!TextUtils.isEmpty(strReplace4) && h2.J(strReplace4)) {
                            this.f13750g = strReplace4;
                        }
                    }
                    int iD3 = d(listM, "蜂鸟准时达", true);
                    if (iD3 > 0) {
                        this.f13749f = listM.get(iD3 - 1);
                    }
                } else {
                    String str3 = listM.get(iIndexOf4 + 1);
                    if (!TextUtils.isEmpty(str3) && str3.contains("￥")) {
                        String strReplace5 = str3.replace("￥", "").replace(",", "");
                        if (h2.J(strReplace5)) {
                            this.f13750g = strReplace5;
                        }
                    }
                }
                y.l("sssss", "remark:" + this.f13749f, "num:" + this.f13750g);
            }
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        r(context, listM, str);
    }

    public BillInfo t(String str, String str2, String str3) {
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("淘宝");
        billInfo.setRemark("淘宝购物");
        billInfo.setShopName("淘宝");
        if (!TextUtils.isEmpty(str)) {
            billInfo.setAsset(str);
        }
        if (!TextUtils.isEmpty(str2)) {
            billInfo.setRemark(str2);
        }
        if (!TextUtils.isEmpty(str3)) {
            billInfo.setNumber(str3);
        }
        if (!TextUtils.isEmpty(billInfo.getRemark()) && !TextUtils.isEmpty(billInfo.getNumber())) {
            return billInfo;
        }
        if (!this.f13752i) {
            return null;
        }
        billInfo.setNumber("0");
        return billInfo;
    }
}
