// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 f2c99ce204cc28fc9132ac4abab076d1162586dd4c686ef86555ab6ad725d97d
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

    /** DEX-exact branch transcription of s; original register and block addresses retained. */
    public void s(android.content.Context arg0, java.lang.String arg1, android.view.accessibility.AccessibilityNodeInfo arg2) {
        Object[] v=new Object[13]; Object[] result=new Object[2]; int pc=0;
        v[9]=this;
        v[10]=arg0;
        v[11]=arg1;
        v[12]=arg2;
        while(true) {
            if(pc==-1) { return; }
            if(pc<0xf8) pc=recovered_s_block0(v,result,pc);
            else pc=recovered_s_block1(v,result,pc);
        }
    }
    private int recovered_s_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                v[0]="com.unionpay.activity.UPActivityMain";
                result[0]=dexN((((java.lang.String)dexR(v[11])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[0]=result[0];
                v[1]=dexN(0L);
                pc=(dexZero(v[0]))?0x11:0x9; continue;
            }
            case 0x9: {
                ((com.wangc.bill.auto.action.c)dexR(v[9])).a=dexI(v[1]);
                ((com.wangc.bill.auto.action.o)dexR(v[9])).f13768g=dexZ(v[1]);
                ((com.wangc.bill.auto.action.c)dexR(v[9])).b=dexZ(v[1]);
                pc=0x151; continue;
            }
            case 0x11: {
                v[0]="com.unionpay.activity.payment.UPActivityScan";
                result[0]=dexN((((java.lang.String)dexR(v[11])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[0]=result[0];
                v[2]=dexN(4L);
                v[3]=dexN(1L);
                pc=(!dexZero(v[0]))?0x14d:0x1b; continue;
            }
            case 0x1b: {
                v[0]="com.unionpay.activity.payment.UPActivityPaymentQrCodeOut";
                result[0]=dexN((((java.lang.String)dexR(v[11])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x25:0x23; continue;
            }
            case 0x23: {
                pc=0x14d; continue;
            }
            case 0x25: {
                pc=(dexZero(v[12]))?0x151:0x27; continue;
            }
            case 0x27: {
                v[11]="start get data";
                v[0]="sssss";
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[0])),((java.lang.Object)dexR(v[11]))};
                v[11]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[11])));
                result[0]=((com.wangc.bill.auto.action.c)dexR(v[9])).g(((android.view.accessibility.AccessibilityNodeInfo)dexR(v[12])));
                v[11]=result[0];
                v[4]=new DexReference(); // new java.util.HashMap
                ((DexReference)v[4]).value=new java.util.HashMap();
                result[0]=((java.util.List)dexR(v[11])).iterator();
                v[5]=result[0];
                pc=0x3f; continue;
            }
            case 0x3f: {
                result[0]=dexN((((java.util.Iterator)dexR(v[5])).hasNext())?1:0);
                v[6]=result[0];
                pc=(dexZero(v[6]))?0x51:0x45; continue;
            }
            case 0x45: {
                result[0]=((java.util.Iterator)dexR(v[5])).next();
                v[6]=result[0];
                v[6]=((java.lang.String)dexR(v[6]));
                v[7]=java.lang.Boolean.TRUE;
                result[0]=((java.util.HashMap)dexR(v[4])).put(((java.lang.Object)dexR(v[6])),((java.lang.Object)dexR(v[7])));
                pc=0x3f; continue;
            }
            case 0x51: {
                v[5]="支付成功";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x67:0x59; continue;
            }
            case 0x59: {
                v[5]="付款方式";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x67:0x61; continue;
            }
            case 0x61: {
                ((com.wangc.bill.auto.action.c)dexR(v[9])).a=dexI(v[2]);
                ((com.wangc.bill.auto.action.c)dexR(v[9])).b=dexZ(v[3]);
                pc=0x153; continue;
            }
            case 0x67: {
                v[2]="账单详情";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[2]))))?1:0);
                v[5]=result[0];
                v[6]=dexN(12L);
                pc=(dexZero(v[5]))?0x87:0x71; continue;
            }
            case 0x71: {
                v[5]="转给本人";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x87:0x79; continue;
            }
            case 0x79: {
                v[5]="转账金额";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x87:0x81; continue;
            }
            case 0x81: {
                ((com.wangc.bill.auto.action.c)dexR(v[9])).a=dexI(v[6]);
                ((com.wangc.bill.auto.action.c)dexR(v[9])).b=dexZ(v[3]);
                pc=0x153; continue;
            }
            case 0x87: {
                v[5]="实收金额（元）";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x147:0x8f; continue;
            }
            case 0x8f: {
                v[5]="实付金额（元）";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x147:0x97; continue;
            }
            case 0x97: {
                v[5]="当前状态";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0xa7:0x9f; continue;
            }
            case 0x9f: {
                v[7]="订单时间";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[7]))))?1:0);
                v[7]=result[0];
                pc=(!dexZero(v[7]))?0x147:0xa7; continue;
            }
            case 0xa7: {
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0xb5:0xad; continue;
            }
            case 0xad: {
                v[7]="扣款时间";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[7]))))?1:0);
                v[7]=result[0];
                pc=(!dexZero(v[7]))?0x147:0xb5; continue;
            }
            case 0xb5: {
                v[7]="交易详情";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[7]))))?1:0);
                v[8]=result[0];
                pc=(dexZero(v[8]))?0xc5:0xbd; continue;
            }
            case 0xbd: {
                v[8]="时间";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[8]))))?1:0);
                v[8]=result[0];
                pc=(!dexZero(v[8]))?0x147:0xc5; continue;
            }
            case 0xc5: {
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[7]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0xd3:0xcb; continue;
            }
            case 0xcb: {
                v[7]="交易时间";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[7]))))?1:0);
                v[7]=result[0];
                pc=(!dexZero(v[7]))?0x147:0xd3; continue;
            }
            case 0xd3: {
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0xe2:0xd9; continue;
            }
            case 0xd9: {
                v[5]="退款时间";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0xe2:0xe1; continue;
            }
            case 0xe1: {
                pc=0x147; continue;
            }
            case 0xe2: {
                v[5]="转账成功";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0xf2:0xea; continue;
            }
            case 0xea: {
                v[5]="完成";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x100:0xf2; continue;
            }
            case 0xf2: {
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[2]))))?1:0);
                v[2]=result[0];
                pc=(dexZero(v[2]))?0x105:0xf8; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0xf8: {
                v[2]="转出成功";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[2]))))?1:0);
                v[2]=result[0];
                pc=(dexZero(v[2]))?0x105:0x100; continue;
            }
            case 0x100: {
                ((com.wangc.bill.auto.action.c)dexR(v[9])).a=dexI(v[6]);
                ((com.wangc.bill.auto.action.c)dexR(v[9])).b=dexZ(v[3]);
                pc=0x153; continue;
            }
            case 0x105: {
                v[2]="本次还款金额";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[2]))))?1:0);
                v[2]=result[0];
                pc=(dexZero(v[2]))?0x153:0x10d; continue;
            }
            case 0x10d: {
                v[2]="本期账单";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[2]))))?1:0);
                v[2]=result[0];
                pc=(dexZero(v[2]))?0x153:0x115; continue;
            }
            case 0x115: {
                v[2]="立即还";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[2]))))?1:0);
                v[2]=result[0];
                pc=(dexZero(v[2]))?0x153:0x11d; continue;
            }
            case 0x11d: {
                v[2]="分期还";
                result[0]=dexN((((java.util.HashMap)dexR(v[4])).containsKey(((java.lang.Object)dexR(v[2]))))?1:0);
                v[2]=result[0];
                pc=(dexZero(v[2]))?0x153:0x125; continue;
            }
            case 0x125: {
                v[2]="[";
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[9])).f(((java.util.List)dexR(v[11])),((java.lang.String)dexR(v[2])),dexZ(v[1])));
                v[1]=result[0];
                pc=(dexI(v[1])<0)?0x137:0x12d; continue;
            }
            case 0x12d: {
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.auto.action.o)dexR(v[9])).f13766e=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.auto.action.o)dexR(v[9])).f13768g=dexZ(v[3]);
                pc=0x137; continue;
            }
            case 0x137: {
                v[1]=dexN((((com.wangc.bill.auto.action.o)dexR(v[9])).f13768g)?1:0);
                result[0]=java.lang.Boolean.valueOf(dexZ(v[1]));
                v[1]=result[0];
                v[2]=((com.wangc.bill.auto.action.o)dexR(v[9])).f13766e;
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[0])),((java.lang.Object)dexR(v[1])),((java.lang.Object)dexR(v[2]))};
                v[0]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[0])));
                pc=0x153; continue;
            }
            case 0x147: {
                v[0]=dexN(5L);
                ((com.wangc.bill.auto.action.c)dexR(v[9])).a=dexI(v[0]);
                ((com.wangc.bill.auto.action.c)dexR(v[9])).b=dexZ(v[3]);
                pc=0x153; continue;
            }
            case 0x14d: {
                ((com.wangc.bill.auto.action.c)dexR(v[9])).a=dexI(v[2]);
                ((com.wangc.bill.auto.action.c)dexR(v[9])).b=dexZ(v[3]);
                pc=0x151; continue;
            }
            case 0x151: {
                v[11]=dexN(0L);
                v[4]=v[11];
                pc=0x153; continue;
            }
            case 0x153: {
                v[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[9])).b)?1:0);
                pc=(dexZero(v[0]))?0x17d:0x157; continue;
            }
            case 0x157: {
                pc=(dexZero(v[12]))?0x17d:0x159; continue;
            }
            case 0x159: {
                pc=(!dexZero(v[11]))?0x17a:0x15b; continue;
            }
            case 0x15b: {
                result[0]=((com.wangc.bill.auto.action.c)dexR(v[9])).g(((android.view.accessibility.AccessibilityNodeInfo)dexR(v[12])));
                v[11]=result[0];
                v[4]=new DexReference(); // new java.util.HashMap
                ((DexReference)v[4]).value=new java.util.HashMap();
                result[0]=((java.util.List)dexR(v[11])).iterator();
                v[12]=result[0];
                pc=0x168; continue;
            }
            case 0x168: {
                result[0]=dexN((((java.util.Iterator)dexR(v[12])).hasNext())?1:0);
                v[0]=result[0];
                pc=(dexZero(v[0]))?0x17a:0x16e; continue;
            }
            case 0x16e: {
                result[0]=((java.util.Iterator)dexR(v[12])).next();
                v[0]=result[0];
                v[0]=((java.lang.String)dexR(v[0]));
                v[1]=java.lang.Boolean.TRUE;
                result[0]=((java.util.HashMap)dexR(v[4])).put(((java.lang.Object)dexR(v[0])),((java.lang.Object)dexR(v[1])));
                pc=0x168; continue;
            }
            case 0x17a: {
                ((com.wangc.bill.auto.action.o)dexR(v[9])).r(((android.content.Context)dexR(v[10])),((java.util.List)dexR(v[11])),((java.util.HashMap)dexR(v[4])));
                pc=0x17d; continue;
            }
            case 0x17d: {
                return -1;
            }
            default: return pc;
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

    /** DEX-exact branch transcription of u; original register and block addresses retained. */
    public com.wangc.bill.http.entity.BillInfo u(java.util.List arg0) {
        Object[] v=new Object[22]; Object[] result=new Object[2]; int pc=0;
        v[20]=this;
        v[21]=arg0;
        while(true) {
            if(pc==-1) { return ((com.wangc.bill.http.entity.BillInfo)dexR(result[1])); }
            if(pc<0x156) pc=recovered_u_block0(v,result,pc);
            else pc=recovered_u_block1(v,result,pc);
        }
    }
    private int recovered_u_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                v[0]=v[21];
                result[0]=dexN(((java.util.List)dexR(v[21])).size());
                v[1]=result[0];
                v[2]=dexN(0L);
                pc=(!dexZero(v[1]))?0xa:0x9; continue;
            }
            case 0x9: {
                result[1]=v[2]; return -1;
            }
            case 0xa: {
                v[1]=new DexReference(); // new com.wangc.bill.http.entity.BillInfo
                ((DexReference)v[1]).value=new com.wangc.bill.http.entity.BillInfo();
                v[3]="云闪付";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setOrigin(((java.lang.String)dexR(v[3])));
                v[4]=dexN(0L);
                v[5]=dexN(0L);
                pc=0x16; continue;
            }
            case 0x16: {
                result[0]=dexN(((java.util.List)dexR(v[21])).size());
                v[6]=result[0];
                pc=(dexI(v[4])>=dexI(v[6]))?0x20f:0x1c; continue;
            }
            case 0x1c: {
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[6]=result[0];
                v[6]=((java.lang.String)dexR(v[6]));
                v[7]="实付金额（元）";
                result[0]=dexN((((java.lang.String)dexR(v[6])).equals(((java.lang.Object)dexR(v[7]))))?1:0);
                v[7]=result[0];
                v[8]="实收金额（元）";
                v[9]=dexN(1L);
                v[10]="";
                pc=(!dexZero(v[7]))?0x35:0x2f; continue;
            }
            case 0x2f: {
                result[0]=dexN((((java.lang.String)dexR(v[6])).equals(((java.lang.Object)dexR(v[8]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x7c:0x35; continue;
            }
            case 0x35: {
                result[0]=dexN(((java.util.List)dexR(v[21])).size());
                v[7]=result[0];
                v[7]=dexN(dexI(v[7])-dexI(v[9]));
                pc=(dexI(v[4])>=dexI(v[7]))?0x7c:0x3c; continue;
            }
            case 0x3c: {
                result[0]=dexN((((java.lang.String)dexR(v[6])).equals(((java.lang.Object)dexR(v[8]))))?1:0);
                v[6]=result[0];
                pc=(dexZero(v[6]))?0x46:0x42; continue;
            }
            case 0x42: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setIncome(dexZ(v[9]));
                v[5]=v[9];
                pc=0x46; continue;
            }
            case 0x46: {
                v[6]=dexN(dexI(v[4])+(1));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[6]));
                v[6]=result[0];
                v[6]=((java.lang.String)dexR(v[6]));
                v[7]="元";
                result[0]=((java.lang.String)dexR(v[6])).replace(((java.lang.CharSequence)dexR(v[7])),((java.lang.CharSequence)dexR(v[10])));
                v[6]=result[0];
                v[7]=",";
                result[0]=((java.lang.String)dexR(v[6])).replace(((java.lang.CharSequence)dexR(v[7])),((java.lang.CharSequence)dexR(v[10])));
                v[6]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[6]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x20b:0x60; continue;
            }
            case 0x60: {
                v[7]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[7]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[6])))));
                v[8]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[8]))));
                v[8]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[7])).append(dexD(v[8]));
                result[0]=((java.lang.StringBuilder)dexR(v[7])).append(((java.lang.String)dexR(v[10])));
                result[0]=((java.lang.StringBuilder)dexR(v[7])).toString();
                v[6]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setNumber(((java.lang.String)dexR(v[6])));
                pc=0x20b; continue;
            }
            case 0x7c: {
                v[7]="订单状态";
                result[0]=dexN((((java.lang.String)dexR(v[6])).contains(((java.lang.CharSequence)dexR(v[7]))))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x20b:0x84; continue;
            }
            case 0x84: {
                v[7]="交易类型";
                result[0]=dexN((((java.lang.String)dexR(v[6])).contains(((java.lang.CharSequence)dexR(v[7]))))?1:0);
                v[8]=result[0];
                pc=(dexZero(v[8]))?0x20b:0x8c; continue;
            }
            case 0x8c: {
                v[8]=" ";
                result[0]=((java.lang.String)dexR(v[6])).split(((java.lang.String)dexR(v[8])));
                v[6]=result[0];
                result[0]=java.util.Arrays.asList(((java.lang.Object[])dexR(v[6])));
                v[6]=result[0];
                result[0]=dexN(((java.util.List)dexR(v[6])).size());
                v[11]=result[0];
                pc=(dexI(v[11])<=0)?0x20b:0x9c; continue;
            }
            case 0x9c: {
                v[11]=dexN(0L);
                pc=0x9d; continue;
            }
            case 0x9d: {
                result[0]=dexN(((java.util.List)dexR(v[6])).size());
                v[12]=result[0];
                pc=(dexI(v[11])>=dexI(v[12]))?0x20b:0xa3; continue;
            }
            case 0xa3: {
                result[0]=((java.util.List)dexR(v[6])).get(dexI(v[11]));
                v[12]=result[0];
                v[12]=((java.lang.String)dexR(v[12]));
                v[13]="商户名称";
                result[0]=dexN((((java.lang.String)dexR(v[12])).contains(((java.lang.CharSequence)dexR(v[13]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0xbc:0xb1; continue;
            }
            case 0xb1: {
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[10])));
                v[12]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setShopName(((java.lang.String)dexR(v[12])));
                pc=0xb8; continue;
            }
            case 0xb8: {
                v[17]=v[10];
                pc=0x204; continue;
            }
            case 0xbc: {
                v[13]="乘车线路";
                result[0]=dexN((((java.lang.String)dexR(v[12])).contains(((java.lang.CharSequence)dexR(v[13]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0xcc:0xc4; continue;
            }
            case 0xc4: {
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[10])));
                v[12]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setShopName(((java.lang.String)dexR(v[12])));
                pc=0xb8; continue;
            }
            case 0xcc: {
                result[0]=dexN((((java.lang.String)dexR(v[12])).contains(((java.lang.CharSequence)dexR(v[7]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0xe4:0xd2; continue;
            }
            case 0xd2: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getShopName();
                v[13]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0xe4:0xdc; continue;
            }
            case 0xdc: {
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[7])),((java.lang.CharSequence)dexR(v[10])));
                v[12]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setShopName(((java.lang.String)dexR(v[12])));
                pc=0xb8; continue;
            }
            case 0xe4: {
                v[13]="付款卡号";
                result[0]=dexN((((java.lang.String)dexR(v[12])).contains(((java.lang.CharSequence)dexR(v[13]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0xfe:0xec; continue;
            }
            case 0xec: {
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[10])));
                v[12]=result[0];
                result[0]=dexN(((java.lang.String)dexR(v[12])).length());
                v[13]=result[0];
                v[13]=dexN(dexI(v[13])+(-4));
                result[0]=((java.lang.String)dexR(v[12])).substring(dexI(v[13]));
                v[12]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setAsset(((java.lang.String)dexR(v[12])));
                pc=0xb8; continue;
            }
            case 0xfe: {
                v[13]="信用卡卡号";
                result[0]=dexN((((java.lang.String)dexR(v[12])).contains(((java.lang.CharSequence)dexR(v[13]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x118:0x106; continue;
            }
            case 0x106: {
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[10])));
                v[12]=result[0];
                result[0]=dexN(((java.lang.String)dexR(v[12])).length());
                v[13]=result[0];
                v[13]=dexN(dexI(v[13])+(-4));
                result[0]=((java.lang.String)dexR(v[12])).substring(dexI(v[13]));
                v[12]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setAsset(((java.lang.String)dexR(v[12])));
                pc=0xb8; continue;
            }
            case 0x118: {
                v[13]="转入卡号";
                result[0]=dexN((((java.lang.String)dexR(v[12])).contains(((java.lang.CharSequence)dexR(v[13]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x132:0x120; continue;
            }
            case 0x120: {
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[10])));
                v[12]=result[0];
                result[0]=dexN(((java.lang.String)dexR(v[12])).length());
                v[13]=result[0];
                v[13]=dexN(dexI(v[13])+(-4));
                result[0]=((java.lang.String)dexR(v[12])).substring(dexI(v[13]));
                v[12]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setAsset(((java.lang.String)dexR(v[12])));
                pc=0xb8; continue;
            }
            case 0x132: {
                v[13]="银行名称";
                result[0]=dexN((((java.lang.String)dexR(v[12])).contains(((java.lang.CharSequence)dexR(v[13]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x156:0x13a; continue;
            }
            case 0x13a: {
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[10])));
                v[12]=result[0];
                v[13]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[13]).value=new java.lang.StringBuilder();
                result[0]=((java.lang.StringBuilder)dexR(v[13])).append(((java.lang.String)dexR(v[12])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getAsset();
                v[12]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[13])).append(((java.lang.String)dexR(v[12])));
                result[0]=((java.lang.StringBuilder)dexR(v[13])).toString();
                v[12]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setAsset(((java.lang.String)dexR(v[12])));
                pc=0xb8; continue;
            }
            default: return pc;
        }
    }
    private int recovered_u_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x156: {
                v[13]="付款卡";
                result[0]=dexN((((java.lang.String)dexR(v[12])).contains(((java.lang.CharSequence)dexR(v[13]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x167:0x15e; continue;
            }
            case 0x15e: {
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[10])));
                v[12]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setAsset(((java.lang.String)dexR(v[12])));
                pc=0xb8; continue;
            }
            case 0x167: {
                v[13]="收款卡";
                result[0]=dexN((((java.lang.String)dexR(v[12])).contains(((java.lang.CharSequence)dexR(v[13]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x178:0x16f; continue;
            }
            case 0x16f: {
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[10])));
                v[12]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setAsset(((java.lang.String)dexR(v[12])));
                pc=0xb8; continue;
            }
            case 0x178: {
                v[13]="订单时间";
                result[0]=dexN((((java.lang.String)dexR(v[12])).contains(((java.lang.CharSequence)dexR(v[13]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0xb8:0x180; continue;
            }
            case 0x180: {
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[10])));
                v[12]=result[0];
                result[0]=dexN(((java.util.List)dexR(v[6])).size());
                v[13]=result[0];
                v[13]=dexN(dexI(v[13])-dexI(v[9]));
                v[14]="yyyy-MM-dd HH:mm:ss";
                v[15]=dexN(-1L);
                pc=(dexI(v[11])>=dexI(v[13]))?0x1bf:0x18f; continue;
            }
            case 0x18f: {
                v[13]=dexN(dexI(v[11])+(1));
                result[0]=((java.util.List)dexR(v[6])).get(dexI(v[13]));
                v[13]=result[0];
                v[13]=((java.lang.String)dexR(v[13]));
                v[3]=":";
                result[0]=dexN((((java.lang.String)dexR(v[13])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x1bf:0x19f; continue;
            }
            case 0x19f: {
                v[3]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[3]).value=new java.lang.StringBuilder();
                result[0]=((java.lang.StringBuilder)dexR(v[3])).append(((java.lang.String)dexR(v[12])));
                result[0]=((java.lang.StringBuilder)dexR(v[3])).append(((java.lang.String)dexR(v[8])));
                result[0]=((java.lang.StringBuilder)dexR(v[3])).append(((java.lang.String)dexR(v[13])));
                result[0]=((java.lang.StringBuilder)dexR(v[3])).toString();
                v[3]=result[0];
                v[17]=v[10];
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[3])),((java.lang.String)dexR(v[14]))));
                v[9]=result[0];
                v[3]=dexN(Long.compare(dexL(v[9]),dexL(v[15])));
                pc=(dexZero(v[3]))?0x1c1:0x1bb; continue;
            }
            case 0x1bb: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setTime(dexL(v[9]));
                pc=0x1c1; continue;
            }
            case 0x1bf: {
                v[17]=v[10];
                pc=0x1c1; continue;
            }
            case 0x1c1: {
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getTime());
                v[9]=result[0];
                v[18]=dexN(0L);
                v[3]=dexN(Long.compare(dexL(v[9]),dexL(v[18])));
                pc=(dexI(v[3])>0)?0x204:0x1cb; continue;
            }
            case 0x1cb: {
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[12])),((java.lang.String)dexR(v[14]))));
                v[9]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setTime(dexL(v[9]));
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getTime());
                v[9]=result[0];
                v[3]=dexN(Long.compare(dexL(v[9]),dexL(v[15])));
                pc=(dexZero(v[3]))?0x1e2:0x1da; continue;
            }
            case 0x1da: {
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getTime());
                v[9]=result[0];
                v[3]=dexN(Long.compare(dexL(v[9]),dexL(v[18])));
                pc=(!dexZero(v[3]))?0x1eb:0x1e2; continue;
            }
            case 0x1e2: {
                v[3]="yyyy-MM-dd HH:mm";
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[12])),((java.lang.String)dexR(v[3]))));
                v[9]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setTime(dexL(v[9]));
                pc=0x1eb; continue;
            }
            case 0x1eb: {
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getTime());
                v[9]=result[0];
                v[3]=dexN(Long.compare(dexL(v[9]),dexL(v[15])));
                pc=(dexZero(v[3]))?0x1fb:0x1f3; continue;
            }
            case 0x1f3: {
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getTime());
                v[9]=result[0];
                v[3]=dexN(Long.compare(dexL(v[9]),dexL(v[18])));
                pc=(!dexZero(v[3]))?0x204:0x1fb; continue;
            }
            case 0x1fb: {
                v[3]="yyyy-MM-dd";
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[12])),((java.lang.String)dexR(v[3]))));
                v[9]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setTime(dexL(v[9]));
                pc=0x204; continue;
            }
            case 0x204: {
                v[11]=dexN(dexI(v[11])+(1));
                v[10]=v[17];
                v[9]=dexN(1L);
                pc=0x9d; continue;
            }
            case 0x20b: {
                v[4]=dexN(dexI(v[4])+(1));
                pc=0x16; continue;
            }
            case 0x20f: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getRemark();
                v[0]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(dexZero(v[0]))?0x232:0x219; continue;
            }
            case 0x219: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getShopName();
                v[0]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x22b:0x223; continue;
            }
            case 0x223: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getShopName();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setRemark(((java.lang.String)dexR(v[0])));
                pc=0x232; continue;
            }
            case 0x22b: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getOrigin();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setRemark(((java.lang.String)dexR(v[0])));
                pc=0x232; continue;
            }
            case 0x232: {
                pc=(dexZero(v[5]))?0x24c:0x234; continue;
            }
            case 0x234: {
                v[0]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[0]).value=new java.lang.StringBuilder();
                v[3]="收入-";
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(((java.lang.String)dexR(v[3])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getShopName();
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).toString();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setShopName(((java.lang.String)dexR(v[0])));
                pc=0x24c; continue;
            }
            case 0x24c: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getRemark();
                v[0]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x261:0x256; continue;
            }
            case 0x256: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getNumber();
                v[0]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x261:0x260; continue;
            }
            case 0x260: {
                result[1]=v[1]; return -1;
            }
            case 0x261: {
                result[1]=v[2]; return -1;
            }
            default: return pc;
        }
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

    /** DEX-exact branch transcription of x; original register and block addresses retained. */
    public com.wangc.bill.http.entity.BillInfo x(java.util.List arg0, java.lang.String arg1, java.lang.String arg2) {
        Object[] v=new Object[14]; Object[] result=new Object[2]; int pc=0;
        v[10]=this;
        v[11]=arg0;
        v[12]=arg1;
        v[13]=arg2;
        while(true) {
            if(pc==-1) { return ((com.wangc.bill.http.entity.BillInfo)dexR(result[1])); }
            if(pc<0x194) pc=recovered_x_block0(v,result,pc);
            else pc=recovered_x_block1(v,result,pc);
        }
    }
    private int recovered_x_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                result[0]=dexN(((java.util.List)dexR(v[11])).size());
                v[0]=result[0];
                v[1]=dexN(0L);
                pc=(!dexZero(v[0]))?0x8:0x7; continue;
            }
            case 0x7: {
                result[1]=v[1]; return -1;
            }
            case 0x8: {
                v[0]=new DexReference(); // new com.wangc.bill.http.entity.BillInfo
                ((DexReference)v[0]).value=new com.wangc.bill.http.entity.BillInfo();
                v[2]=dexN(1L);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setTransfer(dexZ(v[2]));
                v[3]="云闪付";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setOrigin(((java.lang.String)dexR(v[3])));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setFromAsset(((java.lang.String)dexR(v[12])));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setToAsset(((java.lang.String)dexR(v[13])));
                v[12]=dexN(0L);
                v[13]=v[12];
                pc=0x1e; continue;
            }
            case 0x1e: {
                result[0]=dexN(((java.util.List)dexR(v[11])).size());
                v[3]=result[0];
                pc=(dexI(v[13])>=dexI(v[3]))?0x29a:0x24; continue;
            }
            case 0x24: {
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[13]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="¥";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[5]=result[0];
                v[6]=",";
                v[7]="";
                pc=(dexZero(v[5]))?0x6a:0x36; continue;
            }
            case 0x36: {
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[7])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[7])));
                v[3]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getNumber();
                v[4]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x296:0x48; continue;
            }
            case 0x48: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x296:0x4e; continue;
            }
            case 0x4e: {
                v[4]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[4]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[5]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[5]))));
                v[5]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(dexD(v[5]));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[7])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).toString();
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setNumber(((java.lang.String)dexR(v[3])));
                pc=0x296; continue;
            }
            case 0x6a: {
                v[5]="手续费";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[8]=result[0];
                pc=(dexZero(v[8]))?0xcf:0x72; continue;
            }
            case 0x72: {
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0xb0:0x78; continue;
            }
            case 0x78: {
                result[0]=dexN(((java.util.List)dexR(v[11])).size());
                v[4]=result[0];
                v[4]=dexN(dexI(v[4])-dexI(v[2]));
                pc=(dexI(v[13])>=dexI(v[4]))?0xb0:0x7f; continue;
            }
            case 0x7f: {
                v[4]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[4]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                v[9]="元";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[9]))))?1:0);
                v[8]=result[0];
                pc=(dexZero(v[8]))?0xb0:0x8f; continue;
            }
            case 0x8f: {
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[4]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[7])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[7])));
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x296:0xa3; continue;
            }
            case 0xa3: {
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[3]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[3]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setServiceCharge(dexD(v[3]));
                pc=0x296; continue;
            }
            case 0xb0: {
                result[0]=dexN(((java.lang.String)dexR(v[3])).lastIndexOf(((java.lang.String)dexR(v[5]))));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).substring(dexI(v[12]),dexI(v[4]));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[7])));
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x296:0xc2; continue;
            }
            case 0xc2: {
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[3]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[3]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setServiceCharge(dexD(v[3]));
                pc=0x296; continue;
            }
            case 0xcf: {
                v[5]="订单金额";
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x110:0xd7; continue;
            }
            case 0xd7: {
                result[0]=dexN(((java.util.List)dexR(v[11])).size());
                v[5]=result[0];
                v[5]=dexN(dexI(v[5])-dexI(v[2]));
                pc=(dexI(v[13])>=dexI(v[5]))?0x110:0xde; continue;
            }
            case 0xde: {
                v[3]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[7])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[7])));
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x296:0xf4; continue;
            }
            case 0xf4: {
                v[4]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[4]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[5]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[5]))));
                v[5]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(dexD(v[5]));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[7])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).toString();
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setNumber(((java.lang.String)dexR(v[3])));
                pc=0x296; continue;
            }
            case 0x110: {
                v[5]="转账金额";
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x151:0x118; continue;
            }
            case 0x118: {
                result[0]=dexN(((java.util.List)dexR(v[11])).size());
                v[5]=result[0];
                v[5]=dexN(dexI(v[5])-dexI(v[2]));
                pc=(dexI(v[13])>=dexI(v[5]))?0x151:0x11f; continue;
            }
            case 0x11f: {
                v[3]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[7])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[7])));
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x296:0x135; continue;
            }
            case 0x135: {
                v[4]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[4]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[5]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[5]))));
                v[5]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(dexD(v[5]));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[7])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).toString();
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setNumber(((java.lang.String)dexR(v[3])));
                pc=0x296; continue;
            }
            case 0x151: {
                v[4]="收款银行";
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x169:0x159; continue;
            }
            case 0x159: {
                v[4]="信用卡号";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x169:0x161; continue;
            }
            case 0x161: {
                v[4]="收款方式";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x17d:0x169; continue;
            }
            case 0x169: {
                result[0]=dexN(((java.util.List)dexR(v[11])).size());
                v[4]=result[0];
                v[4]=dexN(dexI(v[4])-dexI(v[2]));
                pc=(dexI(v[13])>=dexI(v[4]))?0x17d:0x170; continue;
            }
            case 0x170: {
                v[3]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setToAsset(((java.lang.String)dexR(v[3])));
                pc=0x296; continue;
            }
            case 0x17d: {
                v[4]="付款银行";
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x18d:0x185; continue;
            }
            case 0x185: {
                v[4]="付款方式";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1a1:0x18d; continue;
            }
            case 0x18d: {
                result[0]=dexN(((java.util.List)dexR(v[11])).size());
                v[4]=result[0];
                v[4]=dexN(dexI(v[4])-dexI(v[2]));
                pc=(dexI(v[13])>=dexI(v[4]))?0x1a1:0x194; continue;
            }
            default: return pc;
        }
    }
    private int recovered_x_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x194: {
                v[3]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setFromAsset(((java.lang.String)dexR(v[3])));
                pc=0x296; continue;
            }
            case 0x1a1: {
                v[4]="付款卡号";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1e0:0x1a9; continue;
            }
            case 0x1a9: {
                result[0]=dexN(((java.util.List)dexR(v[11])).size());
                v[4]=result[0];
                v[4]=dexN(dexI(v[4])-dexI(v[2]));
                pc=(dexI(v[13])>=dexI(v[4]))?0x1e0:0x1b0; continue;
            }
            case 0x1b0: {
                v[3]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                result[0]=dexN(((java.lang.String)dexR(v[3])).length());
                v[4]=result[0];
                v[4]=dexN(dexI(v[4])+(-4));
                result[0]=((java.lang.String)dexR(v[3])).substring(dexI(v[4]));
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x296:0x1c8; continue;
            }
            case 0x1c8: {
                v[4]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[4]).value=new java.lang.StringBuilder();
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getFromAsset();
                v[5]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[5])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).toString();
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setFromAsset(((java.lang.String)dexR(v[3])));
                pc=0x296; continue;
            }
            case 0x1e0: {
                v[4]="收款卡号";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x21f:0x1e8; continue;
            }
            case 0x1e8: {
                result[0]=dexN(((java.util.List)dexR(v[11])).size());
                v[4]=result[0];
                v[4]=dexN(dexI(v[4])-dexI(v[2]));
                pc=(dexI(v[13])>=dexI(v[4]))?0x21f:0x1ef; continue;
            }
            case 0x1ef: {
                v[3]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                result[0]=dexN(((java.lang.String)dexR(v[3])).length());
                v[4]=result[0];
                v[4]=dexN(dexI(v[4])+(-4));
                result[0]=((java.lang.String)dexR(v[3])).substring(dexI(v[4]));
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x296:0x207; continue;
            }
            case 0x207: {
                v[4]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[4]).value=new java.lang.StringBuilder();
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getToAsset();
                v[5]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[5])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).toString();
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setToAsset(((java.lang.String)dexR(v[3])));
                pc=0x296; continue;
            }
            case 0x21f: {
                v[4]="创建时间";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x276:0x227; continue;
            }
            case 0x227: {
                result[0]=dexN(((java.util.List)dexR(v[11])).size());
                v[4]=result[0];
                v[4]=dexN(dexI(v[4])-dexI(v[2]));
                pc=(dexI(v[13])>=dexI(v[4]))?0x276:0x22e; continue;
            }
            case 0x22e: {
                v[3]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="yyyy-MM-dd HH:mm:ss";
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[3])),((java.lang.String)dexR(v[4]))));
                v[4]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setTime(dexL(v[4]));
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getTime());
                v[4]=result[0];
                v[6]=dexN(-1L);
                v[4]=dexN(Long.compare(dexL(v[4]),dexL(v[6])));
                v[8]=dexN(0L);
                pc=(dexZero(v[4]))?0x253:0x24b; continue;
            }
            case 0x24b: {
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getTime());
                v[4]=result[0];
                v[4]=dexN(Long.compare(dexL(v[4]),dexL(v[8])));
                pc=(!dexZero(v[4]))?0x25c:0x253; continue;
            }
            case 0x253: {
                v[4]="yyyy-MM-dd HH:mm";
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[3])),((java.lang.String)dexR(v[4]))));
                v[4]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setTime(dexL(v[4]));
                pc=0x25c; continue;
            }
            case 0x25c: {
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getTime());
                v[4]=result[0];
                v[4]=dexN(Long.compare(dexL(v[4]),dexL(v[6])));
                pc=(dexZero(v[4]))?0x26c:0x264; continue;
            }
            case 0x264: {
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getTime());
                v[4]=result[0];
                v[4]=dexN(Long.compare(dexL(v[4]),dexL(v[8])));
                pc=(!dexZero(v[4]))?0x296:0x26c; continue;
            }
            case 0x26c: {
                v[4]="yyyy-MM-dd";
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[3])),((java.lang.String)dexR(v[4]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setTime(dexL(v[3]));
                pc=0x296; continue;
            }
            case 0x276: {
                v[4]="订单时间";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x296:0x27e; continue;
            }
            case 0x27e: {
                result[0]=dexN(((java.util.List)dexR(v[11])).size());
                v[3]=result[0];
                v[3]=dexN(dexI(v[3])-dexI(v[2]));
                pc=(dexI(v[13])>=dexI(v[3]))?0x296:0x285; continue;
            }
            case 0x285: {
                v[3]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[11])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="yyyy年MM月dd日 HH:mm:ss";
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[3])),((java.lang.String)dexR(v[4]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).setTime(dexL(v[3]));
                pc=0x296; continue;
            }
            case 0x296: {
                v[13]=dexN(dexI(v[13])+(1));
                pc=0x1e; continue;
            }
            case 0x29a: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[0])).getNumber();
                v[11]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x2a5:0x2a4; continue;
            }
            case 0x2a4: {
                result[1]=v[0]; return -1;
            }
            case 0x2a5: {
                result[1]=v[1]; return -1;
            }
            default: return pc;
        }
    }
}
