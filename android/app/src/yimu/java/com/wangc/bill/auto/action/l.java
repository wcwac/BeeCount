// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 7322871c7364ce26db50802d59c6e4639841c4f5672ae1039bdc4f4192c6b721
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

    /** DEX-exact branch transcription of s; original register and block addresses retained. */
    public void s(android.content.Context arg0, java.lang.String arg1, android.view.accessibility.AccessibilityNodeInfo arg2) {
        Object[] v=new Object[20]; Object[] result=new Object[2]; int pc=0;
        v[16]=this;
        v[17]=arg0;
        v[18]=arg1;
        v[19]=arg2;
        while(true) {
            if(pc==-1) { return; }
            if(pc<0xed) pc=recovered_s_block0(v,result,pc);
            else if(pc<0x29c) pc=recovered_s_block1(v,result,pc);
            else pc=recovered_s_block2(v,result,pc);
        }
    }
    private int recovered_s_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                v[0]=v[16];
                v[1]=v[18];
                v[2]=v[19];
                v[3]="com.xunmeng.pinduoduo.ui.activity.HomeActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                v[4]=dexN(1L);
                v[5]=dexN(0L);
                v[6]=dexN(0L);
                pc=(dexZero(v[3]))?0x29:0x11; continue;
            }
            case 0x11: {
                v[1]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).f13745k)?1:0);
                pc=(dexZero(v[1]))?0x25:0x15; continue;
            }
            case 0x15: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[5]);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[5]);
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13739e=((java.lang.String)dexR(v[6]));
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13740f=((java.lang.String)dexR(v[6]));
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13742h=dexZ(v[5]);
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13743i=dexZ(v[5]);
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13744j=dexZ(v[5]);
                pc=0xed; continue;
            }
            case 0x25: {
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13745k=dexZ(v[4]);
                pc=0xed; continue;
            }
            case 0x29: {
                v[3]="com.xunmeng.pinduoduo.ui.activity.MainFrameActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x41:0x31; continue;
            }
            case 0x31: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[5]);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[5]);
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13739e=((java.lang.String)dexR(v[6]));
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13740f=((java.lang.String)dexR(v[6]));
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13742h=dexZ(v[5]);
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13743i=dexZ(v[5]);
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13744j=dexZ(v[5]);
                pc=0xed; continue;
            }
            case 0x41: {
                v[3]="com.xunmeng.pinduoduo.sku_checkout.a";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xe9:0x49; continue;
            }
            case 0x49: {
                v[3]="e.s.y.h9.q0";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xe9:0x51; continue;
            }
            case 0x51: {
                pc=(dexZero(v[2]))?0x61:0x53; continue;
            }
            case 0x53: {
                v[3]="com.xunmeng.pinduoduo:id/tv_select_sku";
                result[0]=((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])).findAccessibilityNodeInfosByViewId(((java.lang.String)dexR(v[3])));
                v[3]=result[0];
                result[0]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).v(((java.util.List)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x61:0x5f; continue;
            }
            case 0x5f: {
                pc=0xe9; continue;
            }
            case 0x61: {
                v[3]="com.xunmeng.pinduoduo.activity.NewPageActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x6f:0x69; continue;
            }
            case 0x69: {
                pc=(dexZero(v[2]))?0x6f:0x6b; continue;
            }
            case 0x6b: {
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13743i=dexZ(v[4]);
                pc=0xed; continue;
            }
            case 0x6f: {
                v[3]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).f13743i)?1:0);
                pc=(dexZero(v[3]))?0x7f:0x73; continue;
            }
            case 0x73: {
                v[3]="android.webkit.WebView";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x7f:0x7b; continue;
            }
            case 0x7b: {
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13744j=dexZ(v[4]);
                pc=0xed; continue;
            }
            case 0x7f: {
                v[1]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).f13744j)?1:0);
                pc=(dexZero(v[1]))?0x94:0x83; continue;
            }
            case 0x83: {
                pc=(dexZero(v[2]))?0x94:0x85; continue;
            }
            case 0x85: {
                v[1]="账单详情";
                result[0]=((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])).findAccessibilityNodeInfosByText(((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).v(((java.util.List)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x94:0x91; continue;
            }
            case 0x91: {
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13742h=dexZ(v[4]);
                pc=0xed; continue;
            }
            case 0x94: {
                v[1]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).f13743i)?1:0);
                pc=(dexZero(v[1]))?0xed:0x98; continue;
            }
            case 0x98: {
                pc=(dexZero(v[2]))?0xed:0x9a; continue;
            }
            case 0x9a: {
                v[1]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).f13742h)?1:0);
                pc=(dexZero(v[1]))?0xed:0x9e; continue;
            }
            case 0x9e: {
                v[1]="商品";
                result[0]=((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])).findAccessibilityNodeInfosByText(((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).v(((java.util.List)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0xc2:0xaa; continue;
            }
            case 0xaa: {
                v[1]="搜索";
                result[0]=((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])).findAccessibilityNodeInfosByText(((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).v(((java.util.List)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0xc2:0xb6; continue;
            }
            case 0xb6: {
                v[1]="综合";
                result[0]=((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])).findAccessibilityNodeInfosByText(((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).v(((java.util.List)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0xe6:0xc2; continue;
            }
            case 0xc2: {
                v[1]="我的订单";
                result[0]=((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])).findAccessibilityNodeInfosByText(((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).v(((java.util.List)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0xed:0xce; continue;
            }
            case 0xce: {
                v[1]="全部";
                result[0]=((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])).findAccessibilityNodeInfosByText(((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).v(((java.util.List)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0xed:0xda; continue;
            }
            case 0xda: {
                v[1]="待付款";
                result[0]=((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])).findAccessibilityNodeInfosByText(((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).v(((java.util.List)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0xed:0xe6; continue;
            }
            case 0xe6: {
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13742h=dexZ(v[5]);
                pc=0xed; continue;
            }
            case 0xe9: {
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13742h=dexZ(v[4]);
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13745k=dexZ(v[5]);
                pc=0xed; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0xed: {
                v[1]=dexN((((com.wangc.bill.auto.action.l)dexR(v[0])).f13742h)?1:0);
                pc=(dexZero(v[1]))?0x351:0xf1; continue;
            }
            case 0xf1: {
                pc=(dexZero(v[2]))?0x351:0xf3; continue;
            }
            case 0xf3: {
                result[0]=((com.wangc.bill.auto.action.c)dexR(v[0])).g(((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])));
                v[6]=result[0];
                v[1]=((com.wangc.bill.auto.action.l)dexR(v[0])).f13740f;
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                v[3]="nodeInfo pdd can add : ";
                pc=(!dexZero(v[1]))?0x14f:0x101; continue;
            }
            case 0x101: {
                v[1]="拼单成功";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[6])),((java.lang.String)dexR(v[1])),dexZ(v[5])))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x119:0x109; continue;
            }
            case 0x109: {
                v[1]="已支付";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[6])),((java.lang.String)dexR(v[1])),dexZ(v[5])))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x119:0x111; continue;
            }
            case 0x111: {
                v[1]="免拼成功";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[6])),((java.lang.String)dexR(v[1])),dexZ(v[5])))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x14f:0x119; continue;
            }
            case 0x119: {
                v[1]="微信好友";
                result[0]=dexN((((java.util.List)dexR(v[6])).contains(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x131:0x121; continue;
            }
            case 0x121: {
                v[1]="继续逛逛";
                result[0]=dexN((((java.util.List)dexR(v[6])).contains(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x131:0x129; continue;
            }
            case 0x129: {
                v[1]="查看订单详情";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[6])),((java.lang.String)dexR(v[1])),dexZ(v[5])))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x14f:0x131; continue;
            }
            case 0x131: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[4]);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[4]);
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                v[3]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).b)?1:0);
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(dexZ(v[3]));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x351; continue;
            }
            case 0x14f: {
                v[1]="当前状态";
                result[0]=dexN((((java.util.List)dexR(v[6])).contains(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                v[7]=dexN(2L);
                pc=(dexZero(v[1]))?0x17e:0x158; continue;
            }
            case 0x158: {
                v[1]="交易单号";
                result[0]=dexN((((java.util.List)dexR(v[6])).contains(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x17e:0x160; continue;
            }
            case 0x160: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[7]);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[4]);
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                v[3]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).b)?1:0);
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(dexZ(v[3]));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x351; continue;
            }
            case 0x17e: {
                v[1]=((com.wangc.bill.auto.action.l)dexR(v[0])).f13740f;
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x1ac:0x186; continue;
            }
            case 0x186: {
                v[1]="支付成功";
                result[0]=dexN((((java.util.List)dexR(v[6])).contains(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x1ac:0x18e; continue;
            }
            case 0x18e: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[4]);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[4]);
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                v[3]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).b)?1:0);
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(dexZ(v[3]));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x351; continue;
            }
            case 0x1ac: {
                v[1]="已选：";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[6])),((java.lang.String)dexR(v[1])),dexZ(v[5])))?1:0);
                v[3]=result[0];
                v[8]="已选:";
                pc=(!dexZero(v[3]))?0x1bc:0x1b6; continue;
            }
            case 0x1b6: {
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[6])),((java.lang.String)dexR(v[8])),dexZ(v[5])))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x351:0x1bc; continue;
            }
            case 0x1bc: {
                v[3]="¥";
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).f(((java.util.List)dexR(v[6])),((java.lang.String)dexR(v[3])),dexZ(v[5])));
                v[9]=result[0];
                v[10]="sssss";
                v[11]="";
                pc=(dexI(v[9])<=0)?0x263:0x1c8; continue;
            }
            case 0x1c8: {
                result[0]=((java.util.List)dexR(v[6])).get(dexI(v[9]));
                v[12]=result[0];
                v[12]=((java.lang.String)dexR(v[12]));
                result[0]=dexN(((java.lang.String)dexR(v[12])).indexOf(((java.lang.String)dexR(v[3]))));
                v[13]=result[0];
                v[14]=",";
                v[15]=" ";
                pc=(dexI(v[13])>=dexI(v[7]))?0x225:0x1d8; continue;
            }
            case 0x1d8: {
                v[7]=dexN(5L);
                pc=(dexI(v[4])>=dexI(v[7]))?0x225:0x1db; continue;
            }
            case 0x1db: {
                v[7]=dexN(dexI(v[9])-dexI(v[4]));
                pc=(dexI(v[7])<0)?0x225:0x1df; continue;
            }
            case 0x1df: {
                result[0]=((java.util.List)dexR(v[6])).get(dexI(v[7]));
                v[7]=result[0];
                v[7]=((java.lang.String)dexR(v[7]));
                result[0]=dexN((((java.lang.String)dexR(v[7])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x222:0x1eb; continue;
            }
            case 0x1eb: {
                result[0]=dexN(((java.lang.String)dexR(v[7])).indexOf(((java.lang.String)dexR(v[3]))));
                v[13]=result[0];
                result[0]=((java.lang.String)dexR(v[7])).substring(dexI(v[13]));
                v[7]=result[0];
                result[0]=((java.lang.String)dexR(v[7])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[11])));
                v[7]=result[0];
                result[0]=((java.lang.String)dexR(v[7])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[11])));
                v[7]=result[0];
                result[0]=((java.lang.String)dexR(v[7])).replace(((java.lang.CharSequence)dexR(v[14])),((java.lang.CharSequence)dexR(v[11])));
                v[7]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[7]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x222:0x205; continue;
            }
            case 0x205: {
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13741g=((java.lang.String)dexR(v[7]));
                v[3]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[3]).value=new java.lang.StringBuilder();
                v[4]="num1:";
                result[0]=((java.lang.StringBuilder)dexR(v[3])).append(((java.lang.String)dexR(v[4])));
                v[4]=((com.wangc.bill.auto.action.l)dexR(v[0])).f13741g;
                result[0]=((java.lang.StringBuilder)dexR(v[3])).append(((java.lang.String)dexR(v[4])));
                result[0]=((java.lang.StringBuilder)dexR(v[3])).toString();
                v[3]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[10])),((java.lang.Object)dexR(v[3]))};
                v[3]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[3])));
                pc=0x263; continue;
            }
            case 0x222: {
                v[4]=dexN(dexI(v[4])+(1));
                pc=0x1d8; continue;
            }
            case 0x225: {
                v[4]=" ¥";
                result[0]=dexN((((java.lang.String)dexR(v[12])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x263:0x22d; continue;
            }
            case 0x22d: {
                result[0]=dexN(((java.lang.String)dexR(v[12])).indexOf(((java.lang.String)dexR(v[3]))));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[12])).substring(dexI(v[4]));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[11])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[11])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[14])),((java.lang.CharSequence)dexR(v[11])));
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x263:0x247; continue;
            }
            case 0x247: {
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13741g=((java.lang.String)dexR(v[3]));
                v[3]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[3]).value=new java.lang.StringBuilder();
                v[4]="num2:";
                result[0]=((java.lang.StringBuilder)dexR(v[3])).append(((java.lang.String)dexR(v[4])));
                v[4]=((com.wangc.bill.auto.action.l)dexR(v[0])).f13741g;
                result[0]=((java.lang.StringBuilder)dexR(v[3])).append(((java.lang.String)dexR(v[4])));
                result[0]=((java.lang.StringBuilder)dexR(v[3])).toString();
                v[3]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[10])),((java.lang.Object)dexR(v[3]))};
                v[3]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[3])));
                pc=0x263; continue;
            }
            case 0x263: {
                v[3]="已选择：";
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).d(((java.util.List)dexR(v[6])),((java.lang.String)dexR(v[3])),dexZ(v[5])));
                v[4]=result[0];
                v[7]="remark:";
                pc=(dexI(v[4])<0)?0x296:0x26d; continue;
            }
            case 0x26d: {
                result[0]=((java.util.List)dexR(v[6])).get(dexI(v[4]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).trim();
                v[1]=result[0];
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13740f=((java.lang.String)dexR(v[1]));
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[7])));
                v[3]=((com.wangc.bill.auto.action.l)dexR(v[0])).f13740f;
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[10])),((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x2f3; continue;
            }
            case 0x296: {
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).d(((java.util.List)dexR(v[6])),((java.lang.String)dexR(v[1])),dexZ(v[5])));
                v[3]=result[0];
                pc=(dexI(v[3])<0)?0x2c5:0x29c; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block2(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x29c: {
                result[0]=((java.util.List)dexR(v[6])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[1])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).trim();
                v[1]=result[0];
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13740f=((java.lang.String)dexR(v[1]));
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[7])));
                v[3]=((com.wangc.bill.auto.action.l)dexR(v[0])).f13740f;
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[10])),((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x2f3; continue;
            }
            case 0x2c5: {
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).d(((java.util.List)dexR(v[6])),((java.lang.String)dexR(v[8])),dexZ(v[5])));
                v[1]=result[0];
                pc=(dexI(v[1])<0)?0x2f3:0x2cb; continue;
            }
            case 0x2cb: {
                result[0]=((java.util.List)dexR(v[6])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).trim();
                v[1]=result[0];
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13740f=((java.lang.String)dexR(v[1]));
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[7])));
                v[3]=((com.wangc.bill.auto.action.l)dexR(v[0])).f13740f;
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[10])),((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x2f3; continue;
            }
            case 0x2f3: {
                v[1]="使用#";
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).d(((java.util.List)dexR(v[6])),((java.lang.String)dexR(v[1])),dexZ(v[5])));
                v[3]=result[0];
                pc=(dexI(v[3])<=0)?0x351:0x2fb; continue;
            }
            case 0x2fb: {
                result[0]=((java.util.List)dexR(v[6])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="优先使用#";
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[11])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[1])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13739e=((java.lang.String)dexR(v[1]));
                v[3]="，";
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x321:0x315; continue;
            }
            case 0x315: {
                v[1]=((com.wangc.bill.auto.action.l)dexR(v[0])).f13739e;
                result[0]=dexN(((java.lang.String)dexR(v[1])).indexOf(((java.lang.String)dexR(v[3]))));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).substring(dexI(v[5]),dexI(v[3]));
                v[1]=result[0];
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13739e=((java.lang.String)dexR(v[1]));
                pc=0x321; continue;
            }
            case 0x321: {
                v[1]=((com.wangc.bill.auto.action.l)dexR(v[0])).f13739e;
                v[3]="最高减";
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x337:0x32b; continue;
            }
            case 0x32b: {
                v[1]=((com.wangc.bill.auto.action.l)dexR(v[0])).f13739e;
                result[0]=dexN(((java.lang.String)dexR(v[1])).indexOf(((java.lang.String)dexR(v[3]))));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).substring(dexI(v[5]),dexI(v[3]));
                v[1]=result[0];
                ((com.wangc.bill.auto.action.l)dexR(v[0])).f13739e=((java.lang.String)dexR(v[1]));
                pc=0x337; continue;
            }
            case 0x337: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="asset:";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                v[3]=((com.wangc.bill.auto.action.l)dexR(v[0])).f13739e;
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[10])),((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x351; continue;
            }
            case 0x351: {
                v[1]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).b)?1:0);
                pc=(dexZero(v[1]))?0x35c:0x355; continue;
            }
            case 0x355: {
                pc=(dexZero(v[2]))?0x35c:0x357; continue;
            }
            case 0x357: {
                v[1]=v[17];
                ((com.wangc.bill.auto.action.l)dexR(v[0])).q(((android.content.Context)dexR(v[1])),((java.util.List)dexR(v[6])));
                pc=0x35c; continue;
            }
            case 0x35c: {
                return -1;
            }
            default: return pc;
        }
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
