// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 2f70da8df9fa2a299ae071afbae0bd49e3869b2f37fddcee6dbe1dff1dd0b7d9
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
    /** DEX-exact branch transcription of r; original register and block addresses retained. */
    public com.wangc.bill.http.entity.BillInfo r(android.content.Context arg0, java.util.List arg1) {
        Object[] v=new Object[24]; Object[] result=new Object[2]; int pc=0;
        v[21]=this;
        v[22]=arg0;
        v[23]=arg1;
        while(true) {
            if(pc==-1) { return ((com.wangc.bill.http.entity.BillInfo)dexR(result[1])); }
            if(pc<0x13f) pc=recovered_r_block0(v,result,pc);
            else if(pc<0x23c) pc=recovered_r_block1(v,result,pc);
            else if(pc<0x343) pc=recovered_r_block2(v,result,pc);
            else if(pc<0x488) pc=recovered_r_block3(v,result,pc);
            else if(pc<0x5fe) pc=recovered_r_block4(v,result,pc);
            else if(pc<0x76f) pc=recovered_r_block5(v,result,pc);
            else if(pc<0x902) pc=recovered_r_block6(v,result,pc);
            else pc=recovered_r_block7(v,result,pc);
        }
    }
    private int recovered_r_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                v[0]=v[21];
                v[1]=v[22];
                v[2]=v[23];
                result[0]=dexN((((java.util.List)dexR(v[23])).isEmpty())?1:0);
                v[3]=result[0];
                v[4]=dexN(0L);
                pc=(dexZero(v[3]))?0xe:0xd; continue;
            }
            case 0xd: {
                result[1]=v[4]; return -1;
            }
            case 0xe: {
                v[3]=new DexReference(); // new com.wangc.bill.http.entity.BillInfo
                ((DexReference)v[3]).value=new com.wangc.bill.http.entity.BillInfo();
                v[5]="支付宝";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setOrigin(((java.lang.String)dexR(v[5])));
                result[0]=dexN((com.wangc.bill.database.action.s.i())?1:0);
                v[6]=result[0];
                pc=(dexZero(v[6]))?0x36:0x1e; continue;
            }
            case 0x1e: {
                v[6]=dexN(((com.wangc.bill.auto.action.b)dexR(v[0])).f13678e);
                result[0]=dexN(java.lang.System.currentTimeMillis());
                v[8]=result[0];
                result[0]=dexN((com.wangc.bill.utils.b2.m0(dexL(v[6]),dexL(v[8])))?1:0);
                v[6]=result[0];
                pc=(dexZero(v[6]))?0x30:0x2a; continue;
            }
            case 0x2a: {
                v[6]=dexN(((com.wangc.bill.auto.action.b)dexR(v[0])).f13678e);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setTime(dexL(v[6]));
                pc=0x36; continue;
            }
            case 0x30: {
                result[0]=dexN(java.lang.System.currentTimeMillis());
                v[6]=result[0];
                ((com.wangc.bill.auto.action.b)dexR(v[0])).f13678e=dexL(v[6]);
                pc=0x36; continue;
            }
            case 0x36: {
                v[7]=dexN(0L);
                pc=0x37; continue;
            }
            case 0x37: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[8]=result[0];
                pc=(dexI(v[7])>=dexI(v[8]))?0x90b:0x3d; continue;
            }
            case 0x3d: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[7]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                v[9]="收款方";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[9]))))?1:0);
                v[9]=result[0];
                v[10]=dexN(1L);
                pc=(dexZero(v[9]))?0x6c:0x4c; continue;
            }
            case 0x4c: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[9]=result[0];
                v[9]=dexN(dexI(v[9])-dexI(v[10]));
                pc=(dexI(v[7])>=dexI(v[9]))?0x6c:0x53; continue;
            }
            case 0x53: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setRemark(((java.lang.String)dexR(v[8])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).getRemark();
                v[8]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setShopName(((java.lang.String)dexR(v[8])));
                v[13]=v[1];
                v[12]=v[3];
                v[17]=v[5];
                pc=0x69; continue;
            }
            case 0x69: {
                v[11]=dexN(0L);
                pc=0x902; continue;
            }
            case 0x6c: {
                v[9]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                v[11]="付款方式";
                v[12]="代付成功";
                v[13]="充值成功";
                v[14]="支付成功";
                v[15]="支出";
                v[4]="元";
                v[10]=",";
                v[6]="¥";
                v[17]=v[5];
                v[5]="￥";
                v[1]="";
                v[18]=v[11];
                v[11]=dexN(2L);
                pc=(!dexEqual(v[9],v[11]))?0xa4:0x89; continue;
            }
            case 0x89: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[9]=result[0];
                pc=(!dexZero(v[9]))?0xa8:0x8f; continue;
            }
            case 0x8f: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[9]=result[0];
                pc=(!dexZero(v[9]))?0xa8:0x95; continue;
            }
            case 0x95: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[12]))))?1:0);
                v[9]=result[0];
                pc=(!dexZero(v[9]))?0xa8:0x9b; continue;
            }
            case 0x9b: {
                v[9]="转账成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[9]))))?1:0);
                v[9]=result[0];
                pc=(dexZero(v[9]))?0xa4:0xa3; continue;
            }
            case 0xa3: {
                pc=0xa8; continue;
            }
            case 0xa4: {
                v[9]=v[18];
                pc=0x19f; continue;
            }
            case 0xa8: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[9]=result[0];
                v[11]=dexN(2L);
                v[9]=dexN(dexI(v[9])-dexI(v[11]));
                pc=(dexI(v[7])>=dexI(v[9]))?0xa4:0xb0; continue;
            }
            case 0xb0: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[9]=result[0];
                v[9]=((java.lang.String)dexR(v[9]));
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[9]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0xd5:0xd2; continue;
            }
            case 0xd2: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setNumber(((java.lang.String)dexR(v[9])));
                pc=0xd5; continue;
            }
            case 0xd5: {
                v[11]=dexN(dexI(v[7])+(2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[11]));
                v[11]=result[0];
                v[11]=((java.lang.String)dexR(v[11]));
                result[0]=((java.lang.String)dexR(v[11])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[11]=result[0];
                result[0]=((java.lang.String)dexR(v[11])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[11]=result[0];
                result[0]=((java.lang.String)dexR(v[11])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[9]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x137:0xf7; continue;
            }
            case 0xf7: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x137:0xfd; continue;
            }
            case 0xfd: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setNumber(((java.lang.String)dexR(v[4])));
                v[1]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                pc=(dexZero(v[1]))?0x19a:0x104; continue;
            }
            case 0x104: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[1]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[6])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x118:0x10e; continue;
            }
            case 0x10e: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[1]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[5])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x12c:0x118; continue;
            }
            case 0x118: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[1]=result[0];
                v[1]=dexN(dexI(v[1])+(-3));
                pc=(dexI(v[7])>=dexI(v[1]))?0x12c:0x120; continue;
            }
            case 0x120: {
                v[1]=dexN(dexI(v[7])+(3));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x12f; continue;
            }
            case 0x12c: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setRemark(((java.lang.String)dexR(v[9])));
                pc=0x12f; continue;
            }
            case 0x12f: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x19a; continue;
            }
            case 0x137: {
                v[9]=v[18];
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[9]))))?1:0);
                v[8]=result[0];
                pc=(!dexZero(v[8]))?0x19a:0x13f; continue;
            }
            default: return pc;
        }
    }
    private int recovered_r_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x13f: {
                v[8]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                pc=(!dexZero(v[8]))?0x194:0x143; continue;
            }
            case 0x143: {
                v[8]=dexN(0L);
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).d(((java.util.List)dexR(v[2])),((java.lang.String)dexR(v[5])),dexZ(v[8])));
                v[9]=result[0];
                pc=(dexI(v[9])<=0)?0x15e:0x14a; continue;
            }
            case 0x14a: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[9]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                pc=0x15c; continue;
            }
            case 0x15c: {
                v[5]=dexN(1L);
                pc=0x160; continue;
            }
            case 0x15e: {
                v[1]=dexN(0L);
                pc=0x15c; continue;
            }
            case 0x160: {
                pc=(dexI(v[9])<=dexI(v[5]))?0x194:0x162; continue;
            }
            case 0x162: {
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x194:0x168; continue;
            }
            case 0x168: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x194:0x16e; continue;
            }
            case 0x16e: {
                v[9]=dexN(dexI(v[9])+(-1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[9]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).getNumber();
                v[5]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x194:0x180; continue;
            }
            case 0x180: {
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[1])))));
                v[5]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).getNumber();
                v[1]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[1])))));
                v[8]=result[0];
                v[5]=dexN(Double.doubleToRawLongBits(dexD(v[5])-dexD(v[8])));
                result[0]=com.wangc.bill.utils.h2.t(dexD(v[5]));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setDiscount(((java.lang.String)dexR(v[1])));
                pc=0x194; continue;
            }
            case 0x194: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setRemark(((java.lang.String)dexR(v[4])));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setShopName(((java.lang.String)dexR(v[4])));
                pc=0x19a; continue;
            }
            case 0x19a: {
                v[13]=v[22];
                v[12]=v[3];
                pc=0x69; continue;
            }
            case 0x19f: {
                v[11]="有退款";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                v[18]=v[9];
                v[9]="-";
                v[19]=v[3];
                v[3]="+";
                pc=(!dexZero(v[11]))?0x287:0x1af; continue;
            }
            case 0x1af: {
                v[11]="自动扣款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x287:0x1b7; continue;
            }
            case 0x1b7: {
                v[11]="已全额退款";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x287:0x1bf; continue;
            }
            case 0x1bf: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[11]=result[0];
                v[20]=v[14];
                v[14]=dexN(6L);
                pc=(dexZero(v[11]))?0x1cc:0x1c8; continue;
            }
            case 0x1c8: {
                v[11]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                pc=(dexEqual(v[11],v[14]))?0x289:0x1cc; continue;
            }
            case 0x1cc: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x1d6:0x1d2; continue;
            }
            case 0x1d2: {
                v[11]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                pc=(dexEqual(v[11],v[14]))?0x289:0x1d6; continue;
            }
            case 0x1d6: {
                v[11]="退款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x1de; continue;
            }
            case 0x1de: {
                v[11]="赔付成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x1e6; continue;
            }
            case 0x1e6: {
                v[11]="交易成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x1ee; continue;
            }
            case 0x1ee: {
                v[11]="还款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x1f6; continue;
            }
            case 0x1f6: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[12]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x1fc; continue;
            }
            case 0x1fc: {
                v[11]="派送中";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x204; continue;
            }
            case 0x204: {
                v[11]="领取中";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x20c; continue;
            }
            case 0x20c: {
                v[11]="等待对方发货";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x214; continue;
            }
            case 0x214: {
                v[11]="等待确认收货";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x21c; continue;
            }
            case 0x21c: {
                v[11]="交易成功(代付成功)";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x224; continue;
            }
            case 0x224: {
                v[11]="退回成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x22c; continue;
            }
            case 0x22c: {
                v[11]="支付成功(代付成功)";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x234; continue;
            }
            case 0x234: {
                v[11]="付款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x23c; continue;
            }
            default: return pc;
        }
    }
    private int recovered_r_block2(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x23c: {
                v[11]="缴费中";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x244; continue;
            }
            case 0x244: {
                v[11]="支付宝小荷包付款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x24c; continue;
            }
            case 0x24c: {
                v[11]="已退款(";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x254; continue;
            }
            case 0x254: {
                v[11]="亲情卡付款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x25c; continue;
            }
            case 0x25c: {
                v[11]="免密支付成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x264; continue;
            }
            case 0x264: {
                v[11]="自动续费成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x26c; continue;
            }
            case 0x26c: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x272; continue;
            }
            case 0x272: {
                v[11]="收款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x27a; continue;
            }
            case 0x27a: {
                v[11]="等待对方确认收货";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x283:0x282; continue;
            }
            case 0x282: {
                pc=0x289; continue;
            }
            case 0x283: {
                v[12]=v[19];
                pc=0x347; continue;
            }
            case 0x287: {
                v[20]=v[14];
                pc=0x289; continue;
            }
            case 0x289: {
                pc=(dexI(v[7])<=0)?0x283:0x28b; continue;
            }
            case 0x28b: {
                v[11]=dexN(dexI(v[7])+(-1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[11]));
                v[12]=result[0];
                v[12]=((java.lang.String)dexR(v[12]));
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x334:0x2b5; continue;
            }
            case 0x2b5: {
                v[12]=v[19];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setNumber(((java.lang.String)dexR(v[1])));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[11]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x2df:0x2c6; continue;
            }
            case 0x2c6: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[11]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[9]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x2e1:0x2d2; continue;
            }
            case 0x2d2: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[11]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[15]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x2df:0x2de; continue;
            }
            case 0x2de: {
                pc=0x2e1; continue;
            }
            case 0x2df: {
                v[1]=dexN(1L);
                pc=0x2ec; continue;
            }
            case 0x2e1: {
                v[1]="退回成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x2ea:0x2e9; continue;
            }
            case 0x2e9: {
                pc=0x2df; continue;
            }
            case 0x2ea: {
                v[1]=dexN(1L);
                pc=0x2ef; continue;
            }
            case 0x2ec: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[1]));
                pc=0x2ef; continue;
            }
            case 0x2ef: {
                v[3]="扣款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x2fb:0x2f7; continue;
            }
            case 0x2f7: {
                v[3]=dexN(0L);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[3]));
                pc=0x2fb; continue;
            }
            case 0x2fb: {
                pc=(dexI(v[7])<=dexI(v[1]))?0x308:0x2fd; continue;
            }
            case 0x2fd: {
                v[1]=dexN(dexI(v[7])+(-2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x308; continue;
            }
            case 0x308: {
                v[1]="退款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x32c:0x310; continue;
            }
            case 0x310: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="退款-来自";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                v[1]=dexN(1L);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[1]));
                pc=0x32c; continue;
            }
            case 0x32c: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x343; continue;
            }
            case 0x334: {
                v[12]=v[19];
                v[3]="NaN";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x343:0x33e; continue;
            }
            case 0x33e: {
                v[1]="0";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setNumber(((java.lang.String)dexR(v[1])));
                pc=0x343; continue;
            }
            default: return pc;
        }
    }
    private int recovered_r_block3(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x343: {
                v[13]=v[22];
                pc=0x69; continue;
            }
            case 0x347: {
                v[11]="收款理由";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x369:0x34f; continue;
            }
            case 0x34f: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[11]=result[0];
                v[13]=dexN(1L);
                v[11]=dexN(dexI(v[11])-dexI(v[13]));
                pc=(dexI(v[7])>=dexI(v[11]))?0x369:0x357; continue;
            }
            case 0x357: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="收款理由";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x343; continue;
            }
            case 0x369: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[11]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x392:0x373; continue;
            }
            case 0x373: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x396:0x379; continue;
            }
            case 0x379: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[6]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x396:0x37f; continue;
            }
            case 0x37f: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[9]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x396:0x385; continue;
            }
            case 0x385: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x396:0x38b; continue;
            }
            case 0x38b: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[15]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x392:0x391; continue;
            }
            case 0x391: {
                pc=0x396; continue;
            }
            case 0x392: {
                v[11]=v[18];
                pc=0x42b; continue;
            }
            case 0x396: {
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[9]=result[0];
                pc=(!dexZero(v[9]))?0x3d5:0x3b4; continue;
            }
            case 0x3b4: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[9]=result[0];
                v[11]=dexN(1L);
                v[9]=dexN(dexI(v[9])-dexI(v[11]));
                pc=(dexI(v[7])>=dexI(v[9]))?0x3d5:0x3bc; continue;
            }
            case 0x3bc: {
                v[4]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[4]));
                v[4]=result[0];
                v[8]=v[4];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                pc=0x3d5; continue;
            }
            case 0x3d5: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x40b:0x3db; continue;
            }
            case 0x3db: {
                v[5]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[5]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[4])))));
                v[9]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[9]))));
                v[9]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[5])).append(dexD(v[9]));
                result[0]=((java.lang.StringBuilder)dexR(v[5])).append(((java.lang.String)dexR(v[1])));
                result[0]=((java.lang.StringBuilder)dexR(v[5])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setNumber(((java.lang.String)dexR(v[1])));
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x40b:0x3fb; continue;
            }
            case 0x3fb: {
                v[1]="支付宝收款";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                v[1]=dexN(1L);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[1]));
                pc=0x40b; continue;
            }
            case 0x40b: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x343:0x415; continue;
            }
            case 0x415: {
                pc=(dexI(v[7])<=0)?0x343:0x417; continue;
            }
            case 0x417: {
                v[1]=dexN(dexI(v[7])+(-1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x343; continue;
            }
            case 0x42b: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x44c:0x431; continue;
            }
            case 0x431: {
                v[11]="付款信息";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x44c:0x439; continue;
            }
            case 0x439: {
                v[11]="交易方式";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x44c:0x441; continue;
            }
            case 0x441: {
                v[11]="退款方式";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x44a:0x449; continue;
            }
            case 0x449: {
                pc=0x44c; continue;
            }
            case 0x44a: {
                v[11]=dexN(0L);
                pc=0x4a4; continue;
            }
            case 0x44c: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[11]=result[0];
                v[13]=dexN(1L);
                v[11]=dexN(dexI(v[11])-dexI(v[13]));
                pc=(dexI(v[7])>=dexI(v[11]))?0x44a:0x454; continue;
            }
            case 0x454: {
                v[1]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[1])));
                v[1]="帮助";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x47e:0x46b; continue;
            }
            case 0x46b: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[1]=result[0];
                v[3]=dexN(2L);
                v[1]=dexN(dexI(v[1])-dexI(v[3]));
                pc=(dexI(v[7])>=dexI(v[1]))?0x47e:0x473; continue;
            }
            case 0x473: {
                v[1]=dexN(dexI(v[7])+(2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[1])));
                pc=0x47e; continue;
            }
            case 0x47e: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x4a2:0x488; continue;
            }
            default: return pc;
        }
    }
    private int recovered_r_block4(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x488: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[1]=result[0];
                v[3]="亲情卡";
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x4a2:0x494; continue;
            }
            case 0x494: {
                result[0]=dexN((((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).isIncome())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x4a2:0x49a; continue;
            }
            case 0x49a: {
                v[11]=dexN(0L);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[11]));
                pc=0x49e; continue;
            }
            case 0x49e: {
                v[13]=v[22];
                pc=0x902; continue;
            }
            case 0x4a2: {
                v[11]=dexN(0L);
                pc=0x49e; continue;
            }
            case 0x4a4: {
                v[13]="创建时间";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                v[14]="yyyy-MM-dd HH:mm:ss";
                pc=(dexZero(v[13]))?0x4cc:0x4ae; continue;
            }
            case 0x4ae: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[15]=dexN(1L);
                v[13]=dexN(dexI(v[13])-dexI(v[15]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x4cc:0x4b6; continue;
            }
            case 0x4b6: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="创建时间";
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[1])),((java.lang.String)dexR(v[14]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setTime(dexL(v[3]));
                pc=0x49e; continue;
            }
            case 0x4cc: {
                v[13]="支付时间";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x50a:0x4d4; continue;
            }
            case 0x4d4: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[15]=dexN(1L);
                v[13]=dexN(dexI(v[13])-dexI(v[15]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x50a:0x4dc; continue;
            }
            case 0x4dc: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="支付时间";
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[9]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x4f8:0x4f0; continue;
            }
            case 0x4f0: {
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[1])),((java.lang.String)dexR(v[14]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setTime(dexL(v[3]));
                pc=0x49e; continue;
            }
            case 0x4f8: {
                v[3]="年";
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x49e:0x500; continue;
            }
            case 0x500: {
                v[3]="yyyy年MM月dd日 HH:mm:ss";
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[1])),((java.lang.String)dexR(v[3]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setTime(dexL(v[3]));
                pc=0x49e; continue;
            }
            case 0x50a: {
                v[13]="收款时间";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x52e:0x512; continue;
            }
            case 0x512: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[15]=dexN(1L);
                v[13]=dexN(dexI(v[13])-dexI(v[15]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x52f:0x51a; continue;
            }
            case 0x51a: {
                v[1]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[1])),((java.lang.String)dexR(v[14]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setTime(dexL(v[3]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[15]));
                pc=0x49e; continue;
            }
            case 0x52e: {
                v[15]=dexN(1L);
                pc=0x52f; continue;
            }
            case 0x52f: {
                v[13]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                v[14]=dexN(7L);
                pc=(!dexEqual(v[13],v[14]))?0x56e:0x534; continue;
            }
            case 0x534: {
                v[13]="订单金额";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x56e:0x53c; continue;
            }
            case 0x53c: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[13]=dexN(dexI(v[13])-dexI(v[15]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x56e:0x543; continue;
            }
            case 0x543: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x49e:0x569; continue;
            }
            case 0x569: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setNumber(((java.lang.String)dexR(v[1])));
                pc=0x49e; continue;
            }
            case 0x56e: {
                v[13]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                pc=(!dexEqual(v[13],v[14]))?0x5d1:0x572; continue;
            }
            case 0x572: {
                v[13]="花呗收钱服务费";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(!dexZero(v[13]))?0x582:0x57a; continue;
            }
            case 0x57a: {
                v[13]="平台服务费";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x5d1:0x582; continue;
            }
            case 0x582: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[15]=dexN(1L);
                v[13]=dexN(dexI(v[13])-dexI(v[15]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x5d1:0x58a; continue;
            }
            case 0x58a: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x49e:0x5b0; continue;
            }
            case 0x5b0: {
                v[4]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[4]).value=new java.lang.StringBuilder();
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[5]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[5])))));
                v[5]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[8]=result[0];
                v[5]=dexN(Double.doubleToRawLongBits(dexD(v[5])-dexD(v[8])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(dexD(v[5]));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[1])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setNumber(((java.lang.String)dexR(v[1])));
                pc=0x49e; continue;
            }
            case 0x5d1: {
                v[13]="商品说明";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x611:0x5d9; continue;
            }
            case 0x5d9: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[15]=dexN(1L);
                v[13]=dexN(dexI(v[13])-dexI(v[15]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x611:0x5e1; continue;
            }
            case 0x5e1: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="商品说明";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                v[3]="余额宝";
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x49e:0x5fe; continue;
            }
            default: return pc;
        }
    }
    private int recovered_r_block5(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x5fe: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                v[3]="收益发放";
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x49e:0x60a; continue;
            }
            case 0x60a: {
                v[1]="余额宝";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[1])));
                pc=0x49e; continue;
            }
            case 0x611: {
                v[13]="订单详情";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x65a:0x619; continue;
            }
            case 0x619: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[15]=dexN(1L);
                v[13]=dexN(dexI(v[13])-dexI(v[15]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x65a:0x621; continue;
            }
            case 0x621: {
                v[1]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[3]=result[0];
                v[4]=v[20];
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x638:0x62f; continue;
            }
            case 0x62f: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x638; continue;
            }
            case 0x638: {
                v[1]="商品图片";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x49e:0x644; continue;
            }
            case 0x644: {
                v[1]=dexN(dexI(v[7])+(2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                v[13]=v[22];
                com.google.android.accessibility.selecttospeak.SelectToSpeakService.E(((android.content.Context)dexR(v[13])),((java.lang.String)dexR(v[1])));
                pc=0x902; continue;
            }
            case 0x65a: {
                v[13]=v[22];
                v[15]="交易详情";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(dexZero(v[15]))?0x6b6:0x664; continue;
            }
            case 0x664: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[15]=result[0];
                v[16]=dexN(1L);
                v[15]=dexN(dexI(v[15])+(-1));
                pc=(dexI(v[7])>=dexI(v[15]))?0x6b6:0x66e; continue;
            }
            case 0x66e: {
                v[1]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x902:0x67c; continue;
            }
            case 0x67c: {
                v[3]="更多";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x902:0x684; continue;
            }
            case 0x684: {
                v[3]="推荐服务";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x902:0x68c; continue;
            }
            case 0x68c: {
                v[3]="服务推荐";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x902:0x694; continue;
            }
            case 0x694: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[1]=result[0];
                v[3]=dexN(2L);
                v[1]=dexN(dexI(v[1])-dexI(v[3]));
                pc=(dexI(v[7])>=dexI(v[1]))?0x902:0x69c; continue;
            }
            case 0x69c: {
                v[1]=dexN(dexI(v[7])+(2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x902:0x6aa; continue;
            }
            case 0x6aa: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                com.google.android.accessibility.selecttospeak.SelectToSpeakService.E(((android.content.Context)dexR(v[13])),((java.lang.String)dexR(v[1])));
                pc=0x902; continue;
            }
            case 0x6b6: {
                v[15]="缴费说明";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(dexZero(v[15]))?0x6db:0x6be; continue;
            }
            case 0x6be: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[15]=result[0];
                v[16]=dexN(1L);
                v[15]=dexN(dexI(v[15])+(-1));
                pc=(dexI(v[7])>=dexI(v[15]))?0x6db:0x6c8; continue;
            }
            case 0x6c8: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="缴费说明";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x902; continue;
            }
            case 0x6db: {
                v[15]="充值说明";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(dexZero(v[15]))?0x700:0x6e3; continue;
            }
            case 0x6e3: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[15]=result[0];
                v[16]=dexN(1L);
                v[15]=dexN(dexI(v[15])+(-1));
                pc=(dexI(v[7])>=dexI(v[15]))?0x700:0x6ed; continue;
            }
            case 0x6ed: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="充值说明";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x902; continue;
            }
            case 0x700: {
                v[15]="付款备注";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(dexZero(v[15]))?0x725:0x708; continue;
            }
            case 0x708: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[15]=result[0];
                v[16]=dexN(1L);
                v[15]=dexN(dexI(v[15])+(-1));
                pc=(dexI(v[7])>=dexI(v[15]))?0x725:0x712; continue;
            }
            case 0x712: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="付款备注";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x902; continue;
            }
            case 0x725: {
                v[15]="红包说明";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(dexZero(v[15]))?0x74a:0x72d; continue;
            }
            case 0x72d: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[15]=result[0];
                v[16]=dexN(1L);
                v[15]=dexN(dexI(v[15])+(-1));
                pc=(dexI(v[7])>=dexI(v[15]))?0x74a:0x737; continue;
            }
            case 0x737: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="红包说明";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x902; continue;
            }
            case 0x74a: {
                v[15]="理由";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(dexZero(v[15]))?0x76f:0x752; continue;
            }
            case 0x752: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[15]=result[0];
                v[16]=dexN(1L);
                v[15]=dexN(dexI(v[15])+(-1));
                pc=(dexI(v[7])>=dexI(v[15]))?0x76f:0x75c; continue;
            }
            case 0x75c: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="理由";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x902; continue;
            }
            default: return pc;
        }
    }
    private int recovered_r_block6(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x76f: {
                v[15]="转账备注";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(dexZero(v[15]))?0x812:0x777; continue;
            }
            case 0x777: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[15]=result[0];
                v[16]=dexN(1L);
                v[15]=dexN(dexI(v[15])+(-1));
                pc=(dexI(v[7])>=dexI(v[15]))?0x812:0x781; continue;
            }
            case 0x781: {
                v[1]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="转账";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x7cc:0x791; continue;
            }
            case 0x791: {
                result[0]=dexN((((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).isIncome())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x7b3:0x797; continue;
            }
            case 0x797: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="收到";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getShopName();
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[4])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x7d5; continue;
            }
            case 0x7b3: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="转账给";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getShopName();
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x7d5; continue;
            }
            case 0x7cc: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x7d5; continue;
            }
            case 0x7d5: {
                result[0]=dexN((((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).isIncome())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x7f8:0x7db; continue;
            }
            case 0x7db: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="收到";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getShopName();
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[4])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x902; continue;
            }
            case 0x7f8: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="转账给";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getShopName();
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x902; continue;
            }
            case 0x812: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getDiscount();
                v[15]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(dexZero(v[15]))?0x8aa:0x81c; continue;
            }
            case 0x81c: {
                v[15]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                pc=(dexZero(v[15]))?0x828:0x820; continue;
            }
            case 0x820: {
                v[15]="-￥";
                result[0]=dexN((((java.lang.String)dexR(v[8])).startsWith(((java.lang.String)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(!dexZero(v[15]))?0x860:0x828; continue;
            }
            case 0x828: {
                v[15]="碰一下支付立减";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(!dexZero(v[15]))?0x860:0x830; continue;
            }
            case 0x830: {
                v[15]="支付宝随机立减";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(!dexZero(v[15]))?0x860:0x838; continue;
            }
            case 0x838: {
                v[15]="碰一下立减";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(!dexZero(v[15]))?0x860:0x840; continue;
            }
            case 0x840: {
                v[15]="碰一下共减";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(!dexZero(v[15]))?0x860:0x848; continue;
            }
            case 0x848: {
                v[15]="视频红包";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(!dexZero(v[15]))?0x860:0x850; continue;
            }
            case 0x850: {
                v[15]="优惠";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(!dexZero(v[15]))?0x860:0x858; continue;
            }
            case 0x858: {
                v[15]="网商银行福利金抵扣";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(dexZero(v[15]))?0x8aa:0x860; continue;
            }
            case 0x860: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[15]=result[0];
                v[16]=dexN(1L);
                v[15]=dexN(dexI(v[15])+(-1));
                pc=(dexI(v[7])>=dexI(v[15]))?0x8aa:0x86a; continue;
            }
            case 0x86a: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x888:0x878; continue;
            }
            case 0x878: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[15]=dexN(2L);
                v[14]=dexN(dexI(v[14])-dexI(v[15]));
                pc=(dexI(v[7])>=dexI(v[14]))?0x888:0x880; continue;
            }
            case 0x880: {
                v[8]=dexN(dexI(v[7])+(2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                pc=0x888; continue;
            }
            case 0x888: {
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x902:0x8a6; continue;
            }
            case 0x8a6: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setDiscount(((java.lang.String)dexR(v[1])));
                pc=0x902; continue;
            }
            case 0x8aa: {
                v[15]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                pc=(dexEqual(v[15],v[14]))?0x902:0x8ae; continue;
            }
            case 0x8ae: {
                v[14]="订单金额";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[8]=result[0];
                pc=(dexZero(v[8]))?0x902:0x8b6; continue;
            }
            case 0x8b6: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[8]=result[0];
                v[14]=dexN(1L);
                v[8]=dexN(dexI(v[8])-dexI(v[14]));
                pc=(dexI(v[7])>=dexI(v[8]))?0x902:0x8be; continue;
            }
            case 0x8be: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x902:0x8e4; continue;
            }
            case 0x8e4: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[3]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x902:0x8ee; continue;
            }
            case 0x8ee: {
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[1])))));
                v[3]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[1]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[1])))));
                v[5]=result[0];
                v[3]=dexN(Double.doubleToRawLongBits(dexD(v[3])-dexD(v[5])));
                result[0]=com.wangc.bill.utils.h2.t(dexD(v[3]));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setDiscount(((java.lang.String)dexR(v[1])));
                pc=0x902; continue;
            }
            default: return pc;
        }
    }
    private int recovered_r_block7(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x902: {
                v[7]=dexN(dexI(v[7])+(1));
                v[3]=v[12];
                v[1]=v[13];
                v[5]=v[17];
                v[4]=dexN(0L);
                pc=0x37; continue;
            }
            case 0x90b: {
                v[12]=v[3];
                v[17]=v[5];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x941:0x918; continue;
            }
            case 0x918: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x941:0x922; continue;
            }
            case 0x922: {
                result[0]=dexN((((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).isIncome())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x935:0x928; continue;
            }
            case 0x928: {
                v[1]="支付宝收款";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x941; continue;
            }
            case 0x935: {
                v[1]="支付宝付款";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x941; continue;
            }
            case 0x941: {
                result[0]=dexN((((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).isIncome())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x957:0x947; continue;
            }
            case 0x947: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x957:0x951; continue;
            }
            case 0x951: {
                v[1]=v[17];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[1])));
                pc=0x959; continue;
            }
            case 0x957: {
                v[1]=v[17];
                pc=0x959; continue;
            }
            case 0x959: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[2]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[2]))))?1:0);
                v[2]=result[0];
                pc=(!dexZero(v[2]))?0x96e:0x963; continue;
            }
            case 0x963: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[2]=result[0];
                result[0]=((java.lang.String)dexR(v[2])).trim();
                v[2]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[2])));
                pc=0x96e; continue;
            }
            case 0x96e: {
                v[2]="账户余额";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[2])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[2]=result[0];
                pc=(!dexZero(v[2]))?0x992:0x97a; continue;
            }
            case 0x97a: {
                v[2]="余额";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[2])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[2]=result[0];
                pc=(!dexZero(v[2]))?0x992:0x986; continue;
            }
            case 0x986: {
                v[2]="可用余额";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[2])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[2]=result[0];
                pc=(dexZero(v[2]))?0x995:0x992; continue;
            }
            case 0x992: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[1])));
                pc=0x995; continue;
            }
            case 0x995: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x9aa:0x99f; continue;
            }
            case 0x99f: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x9aa:0x9a9; continue;
            }
            case 0x9a9: {
                result[1]=v[12]; return -1;
            }
            case 0x9aa: {
                v[1]=dexN(0L);
                result[1]=v[1]; return -1;
            }
            default: return pc;
        }
    }

    /* JADX WARN: Code duplicated, block: B:130:0x0283 A[PHI: r20
      0x0283: PHI (r20v1 java.lang.String) = (r20v2 java.lang.String), (r20v3 java.lang.String) binds: [B:132:0x0289, B:128:0x0280] A[DONT_GENERATE, DONT_INLINE]] */
    /** DEX-exact branch transcription of s; original register and block addresses retained. */
    public com.wangc.bill.http.entity.BillInfo s(android.content.Context arg0, java.util.List arg1) {
        Object[] v=new Object[24]; Object[] result=new Object[2]; int pc=0;
        v[21]=this;
        v[22]=arg0;
        v[23]=arg1;
        while(true) {
            if(pc==-1) { return ((com.wangc.bill.http.entity.BillInfo)dexR(result[1])); }
            if(pc<0x13f) pc=recovered_s_block0(v,result,pc);
            else if(pc<0x23c) pc=recovered_s_block1(v,result,pc);
            else if(pc<0x343) pc=recovered_s_block2(v,result,pc);
            else if(pc<0x488) pc=recovered_s_block3(v,result,pc);
            else if(pc<0x5fe) pc=recovered_s_block4(v,result,pc);
            else if(pc<0x76f) pc=recovered_s_block5(v,result,pc);
            else if(pc<0x907) pc=recovered_s_block6(v,result,pc);
            else pc=recovered_s_block7(v,result,pc);
        }
    }
    private int recovered_s_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                v[0]=v[21];
                v[1]=v[22];
                v[2]=v[23];
                result[0]=dexN((((java.util.List)dexR(v[23])).isEmpty())?1:0);
                v[3]=result[0];
                v[4]=dexN(0L);
                pc=(dexZero(v[3]))?0xe:0xd; continue;
            }
            case 0xd: {
                result[1]=v[4]; return -1;
            }
            case 0xe: {
                v[3]=new DexReference(); // new com.wangc.bill.http.entity.BillInfo
                ((DexReference)v[3]).value=new com.wangc.bill.http.entity.BillInfo();
                v[5]="支付宝";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setOrigin(((java.lang.String)dexR(v[5])));
                result[0]=dexN((com.wangc.bill.database.action.s.i())?1:0);
                v[6]=result[0];
                pc=(dexZero(v[6]))?0x36:0x1e; continue;
            }
            case 0x1e: {
                v[6]=dexN(((com.wangc.bill.auto.action.b)dexR(v[0])).f13678e);
                result[0]=dexN(java.lang.System.currentTimeMillis());
                v[8]=result[0];
                result[0]=dexN((com.wangc.bill.utils.b2.m0(dexL(v[6]),dexL(v[8])))?1:0);
                v[6]=result[0];
                pc=(dexZero(v[6]))?0x30:0x2a; continue;
            }
            case 0x2a: {
                v[6]=dexN(((com.wangc.bill.auto.action.b)dexR(v[0])).f13678e);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setTime(dexL(v[6]));
                pc=0x36; continue;
            }
            case 0x30: {
                result[0]=dexN(java.lang.System.currentTimeMillis());
                v[6]=result[0];
                ((com.wangc.bill.auto.action.b)dexR(v[0])).f13678e=dexL(v[6]);
                pc=0x36; continue;
            }
            case 0x36: {
                v[7]=dexN(0L);
                pc=0x37; continue;
            }
            case 0x37: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[8]=result[0];
                pc=(dexI(v[7])>=dexI(v[8]))?0x907:0x3d; continue;
            }
            case 0x3d: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[7]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                v[9]="收款方";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[9]))))?1:0);
                v[9]=result[0];
                v[10]=dexN(1L);
                pc=(dexZero(v[9]))?0x6c:0x4c; continue;
            }
            case 0x4c: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[9]=result[0];
                v[9]=dexN(dexI(v[9])-dexI(v[10]));
                pc=(dexI(v[7])>=dexI(v[9]))?0x6c:0x53; continue;
            }
            case 0x53: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setRemark(((java.lang.String)dexR(v[8])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).getRemark();
                v[8]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setShopName(((java.lang.String)dexR(v[8])));
                v[13]=v[1];
                v[12]=v[3];
                v[17]=v[5];
                pc=0x69; continue;
            }
            case 0x69: {
                v[11]=dexN(0L);
                pc=0x8fe; continue;
            }
            case 0x6c: {
                v[9]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                v[11]="Payment method";
                v[12]="代付成功";
                v[13]="充值成功";
                v[14]="支付成功";
                v[15]="支出";
                v[4]="元";
                v[10]=",";
                v[6]="¥";
                v[17]=v[5];
                v[5]="￥";
                v[1]="";
                v[18]=v[11];
                v[11]=dexN(2L);
                pc=(!dexEqual(v[9],v[11]))?0xa4:0x89; continue;
            }
            case 0x89: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[9]=result[0];
                pc=(!dexZero(v[9]))?0xa8:0x8f; continue;
            }
            case 0x8f: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[9]=result[0];
                pc=(!dexZero(v[9]))?0xa8:0x95; continue;
            }
            case 0x95: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[12]))))?1:0);
                v[9]=result[0];
                pc=(!dexZero(v[9]))?0xa8:0x9b; continue;
            }
            case 0x9b: {
                v[9]="转账成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[9]))))?1:0);
                v[9]=result[0];
                pc=(dexZero(v[9]))?0xa4:0xa3; continue;
            }
            case 0xa3: {
                pc=0xa8; continue;
            }
            case 0xa4: {
                v[9]=v[18];
                pc=0x19f; continue;
            }
            case 0xa8: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[9]=result[0];
                v[11]=dexN(2L);
                v[9]=dexN(dexI(v[9])-dexI(v[11]));
                pc=(dexI(v[7])>=dexI(v[9]))?0xa4:0xb0; continue;
            }
            case 0xb0: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[9]=result[0];
                v[9]=((java.lang.String)dexR(v[9]));
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[9]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0xd5:0xd2; continue;
            }
            case 0xd2: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setNumber(((java.lang.String)dexR(v[9])));
                pc=0xd5; continue;
            }
            case 0xd5: {
                v[11]=dexN(dexI(v[7])+(2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[11]));
                v[11]=result[0];
                v[11]=((java.lang.String)dexR(v[11]));
                result[0]=((java.lang.String)dexR(v[11])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[11]=result[0];
                result[0]=((java.lang.String)dexR(v[11])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[11]=result[0];
                result[0]=((java.lang.String)dexR(v[11])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[9]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x137:0xf7; continue;
            }
            case 0xf7: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x137:0xfd; continue;
            }
            case 0xfd: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setNumber(((java.lang.String)dexR(v[4])));
                v[1]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                pc=(dexZero(v[1]))?0x19a:0x104; continue;
            }
            case 0x104: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[1]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[6])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x118:0x10e; continue;
            }
            case 0x10e: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[1]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[5])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x12c:0x118; continue;
            }
            case 0x118: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[1]=result[0];
                v[1]=dexN(dexI(v[1])+(-3));
                pc=(dexI(v[7])>=dexI(v[1]))?0x12c:0x120; continue;
            }
            case 0x120: {
                v[1]=dexN(dexI(v[7])+(3));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x12f; continue;
            }
            case 0x12c: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setRemark(((java.lang.String)dexR(v[9])));
                pc=0x12f; continue;
            }
            case 0x12f: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x19a; continue;
            }
            case 0x137: {
                v[9]=v[18];
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[9]))))?1:0);
                v[8]=result[0];
                pc=(!dexZero(v[8]))?0x19a:0x13f; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x13f: {
                v[8]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                pc=(!dexZero(v[8]))?0x194:0x143; continue;
            }
            case 0x143: {
                v[8]=dexN(0L);
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).d(((java.util.List)dexR(v[2])),((java.lang.String)dexR(v[5])),dexZ(v[8])));
                v[9]=result[0];
                pc=(dexI(v[9])<=0)?0x15e:0x14a; continue;
            }
            case 0x14a: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[9]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                pc=0x15c; continue;
            }
            case 0x15c: {
                v[5]=dexN(1L);
                pc=0x160; continue;
            }
            case 0x15e: {
                v[1]=dexN(0L);
                pc=0x15c; continue;
            }
            case 0x160: {
                pc=(dexI(v[9])<=dexI(v[5]))?0x194:0x162; continue;
            }
            case 0x162: {
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x194:0x168; continue;
            }
            case 0x168: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x194:0x16e; continue;
            }
            case 0x16e: {
                v[9]=dexN(dexI(v[9])+(-1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[9]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).getNumber();
                v[5]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x194:0x180; continue;
            }
            case 0x180: {
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[1])))));
                v[5]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).getNumber();
                v[1]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[1])))));
                v[8]=result[0];
                v[5]=dexN(Double.doubleToRawLongBits(dexD(v[5])-dexD(v[8])));
                result[0]=com.wangc.bill.utils.h2.t(dexD(v[5]));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setDiscount(((java.lang.String)dexR(v[1])));
                pc=0x194; continue;
            }
            case 0x194: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setRemark(((java.lang.String)dexR(v[4])));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[3])).setShopName(((java.lang.String)dexR(v[4])));
                pc=0x19a; continue;
            }
            case 0x19a: {
                v[13]=v[22];
                v[12]=v[3];
                pc=0x69; continue;
            }
            case 0x19f: {
                v[11]="有退款";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                v[18]=v[9];
                v[9]="-";
                v[19]=v[3];
                v[3]="+";
                pc=(!dexZero(v[11]))?0x287:0x1af; continue;
            }
            case 0x1af: {
                v[11]="Auto debit successful";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x287:0x1b7; continue;
            }
            case 0x1b7: {
                v[11]="已全额退款";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x287:0x1bf; continue;
            }
            case 0x1bf: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[11]=result[0];
                v[20]=v[14];
                v[14]=dexN(6L);
                pc=(dexZero(v[11]))?0x1cc:0x1c8; continue;
            }
            case 0x1c8: {
                v[11]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                pc=(dexEqual(v[11],v[14]))?0x289:0x1cc; continue;
            }
            case 0x1cc: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x1d6:0x1d2; continue;
            }
            case 0x1d2: {
                v[11]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                pc=(dexEqual(v[11],v[14]))?0x289:0x1d6; continue;
            }
            case 0x1d6: {
                v[11]="退款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x1de; continue;
            }
            case 0x1de: {
                v[11]="赔付成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x1e6; continue;
            }
            case 0x1e6: {
                v[11]="Transaction successful";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x1ee; continue;
            }
            case 0x1ee: {
                v[11]="还款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x1f6; continue;
            }
            case 0x1f6: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[12]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x1fc; continue;
            }
            case 0x1fc: {
                v[11]="派送中";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x204; continue;
            }
            case 0x204: {
                v[11]="领取中";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x20c; continue;
            }
            case 0x20c: {
                v[11]="等待对方发货";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x214; continue;
            }
            case 0x214: {
                v[11]="等待确认收货";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x21c; continue;
            }
            case 0x21c: {
                v[11]="交易成功(代付成功)";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x224; continue;
            }
            case 0x224: {
                v[11]="退回成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x22c; continue;
            }
            case 0x22c: {
                v[11]="支付成功(代付成功)";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x234; continue;
            }
            case 0x234: {
                v[11]="付款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x23c; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block2(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x23c: {
                v[11]="缴费中";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x244; continue;
            }
            case 0x244: {
                v[11]="支付宝小荷包付款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x24c; continue;
            }
            case 0x24c: {
                v[11]="已退款(";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x254; continue;
            }
            case 0x254: {
                v[11]="亲情卡付款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x25c; continue;
            }
            case 0x25c: {
                v[11]="免密支付成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x264; continue;
            }
            case 0x264: {
                v[11]="自动续费成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x26c; continue;
            }
            case 0x26c: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x272; continue;
            }
            case 0x272: {
                v[11]="收款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x289:0x27a; continue;
            }
            case 0x27a: {
                v[11]="等待对方确认收货";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x283:0x282; continue;
            }
            case 0x282: {
                pc=0x289; continue;
            }
            case 0x283: {
                v[12]=v[19];
                pc=0x347; continue;
            }
            case 0x287: {
                v[20]=v[14];
                pc=0x289; continue;
            }
            case 0x289: {
                pc=(dexI(v[7])<=0)?0x283:0x28b; continue;
            }
            case 0x28b: {
                v[11]=dexN(dexI(v[7])+(-1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[11]));
                v[12]=result[0];
                v[12]=((java.lang.String)dexR(v[12]));
                result[0]=((java.lang.String)dexR(v[12])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x334:0x2b5; continue;
            }
            case 0x2b5: {
                v[12]=v[19];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setNumber(((java.lang.String)dexR(v[1])));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[11]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x2df:0x2c6; continue;
            }
            case 0x2c6: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[11]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[9]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x2e1:0x2d2; continue;
            }
            case 0x2d2: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[11]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[15]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x2df:0x2de; continue;
            }
            case 0x2de: {
                pc=0x2e1; continue;
            }
            case 0x2df: {
                v[1]=dexN(1L);
                pc=0x2ec; continue;
            }
            case 0x2e1: {
                v[1]="退回成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x2ea:0x2e9; continue;
            }
            case 0x2e9: {
                pc=0x2df; continue;
            }
            case 0x2ea: {
                v[1]=dexN(1L);
                pc=0x2ef; continue;
            }
            case 0x2ec: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[1]));
                pc=0x2ef; continue;
            }
            case 0x2ef: {
                v[3]="扣款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x2fb:0x2f7; continue;
            }
            case 0x2f7: {
                v[3]=dexN(0L);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[3]));
                pc=0x2fb; continue;
            }
            case 0x2fb: {
                pc=(dexI(v[7])<=dexI(v[1]))?0x308:0x2fd; continue;
            }
            case 0x2fd: {
                v[1]=dexN(dexI(v[7])+(-2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x308; continue;
            }
            case 0x308: {
                v[1]="退款成功";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x32c:0x310; continue;
            }
            case 0x310: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="退款-来自";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                v[1]=dexN(1L);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[1]));
                pc=0x32c; continue;
            }
            case 0x32c: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x343; continue;
            }
            case 0x334: {
                v[12]=v[19];
                v[3]="NaN";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x343:0x33e; continue;
            }
            case 0x33e: {
                v[1]="0";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setNumber(((java.lang.String)dexR(v[1])));
                pc=0x343; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block3(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x343: {
                v[13]=v[22];
                pc=0x69; continue;
            }
            case 0x347: {
                v[11]="收款理由";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x369:0x34f; continue;
            }
            case 0x34f: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[11]=result[0];
                v[13]=dexN(1L);
                v[11]=dexN(dexI(v[11])-dexI(v[13]));
                pc=(dexI(v[7])>=dexI(v[11]))?0x369:0x357; continue;
            }
            case 0x357: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="收款理由";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x343; continue;
            }
            case 0x369: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[11]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x392:0x373; continue;
            }
            case 0x373: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x396:0x379; continue;
            }
            case 0x379: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[6]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x396:0x37f; continue;
            }
            case 0x37f: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[9]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x396:0x385; continue;
            }
            case 0x385: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x396:0x38b; continue;
            }
            case 0x38b: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[15]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x392:0x391; continue;
            }
            case 0x391: {
                pc=0x396; continue;
            }
            case 0x392: {
                v[11]=v[18];
                pc=0x42b; continue;
            }
            case 0x396: {
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[1])));
                v[9]=result[0];
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[9]=result[0];
                pc=(!dexZero(v[9]))?0x3d5:0x3b4; continue;
            }
            case 0x3b4: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[9]=result[0];
                v[11]=dexN(1L);
                v[9]=dexN(dexI(v[9])-dexI(v[11]));
                pc=(dexI(v[7])>=dexI(v[9]))?0x3d5:0x3bc; continue;
            }
            case 0x3bc: {
                v[4]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[4]));
                v[4]=result[0];
                v[8]=v[4];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[4]=result[0];
                pc=0x3d5; continue;
            }
            case 0x3d5: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x40b:0x3db; continue;
            }
            case 0x3db: {
                v[5]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[5]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[4])))));
                v[9]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[9]))));
                v[9]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[5])).append(dexD(v[9]));
                result[0]=((java.lang.StringBuilder)dexR(v[5])).append(((java.lang.String)dexR(v[1])));
                result[0]=((java.lang.StringBuilder)dexR(v[5])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setNumber(((java.lang.String)dexR(v[1])));
                result[0]=dexN((((java.lang.String)dexR(v[8])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x40b:0x3fb; continue;
            }
            case 0x3fb: {
                v[1]="支付宝收款";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                v[1]=dexN(1L);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[1]));
                pc=0x40b; continue;
            }
            case 0x40b: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x343:0x415; continue;
            }
            case 0x415: {
                pc=(dexI(v[7])<=0)?0x343:0x417; continue;
            }
            case 0x417: {
                v[1]=dexN(dexI(v[7])+(-1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x343; continue;
            }
            case 0x42b: {
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x44c:0x431; continue;
            }
            case 0x431: {
                v[11]="付款信息";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x44c:0x439; continue;
            }
            case 0x439: {
                v[11]="交易方式";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0x44c:0x441; continue;
            }
            case 0x441: {
                v[11]="退款方式";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[11]=result[0];
                pc=(dexZero(v[11]))?0x44a:0x449; continue;
            }
            case 0x449: {
                pc=0x44c; continue;
            }
            case 0x44a: {
                v[11]=dexN(0L);
                pc=0x4a4; continue;
            }
            case 0x44c: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[11]=result[0];
                v[13]=dexN(1L);
                v[11]=dexN(dexI(v[11])-dexI(v[13]));
                pc=(dexI(v[7])>=dexI(v[11]))?0x44a:0x454; continue;
            }
            case 0x454: {
                v[1]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[1])));
                v[1]="帮助";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x47e:0x46b; continue;
            }
            case 0x46b: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[1]=result[0];
                v[3]=dexN(2L);
                v[1]=dexN(dexI(v[1])-dexI(v[3]));
                pc=(dexI(v[7])>=dexI(v[1]))?0x47e:0x473; continue;
            }
            case 0x473: {
                v[1]=dexN(dexI(v[7])+(2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[1])));
                pc=0x47e; continue;
            }
            case 0x47e: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x4a2:0x488; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block4(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x488: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[1]=result[0];
                v[3]="亲情卡";
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x4a2:0x494; continue;
            }
            case 0x494: {
                result[0]=dexN((((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).isIncome())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x4a2:0x49a; continue;
            }
            case 0x49a: {
                v[11]=dexN(0L);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[11]));
                pc=0x49e; continue;
            }
            case 0x49e: {
                v[13]=v[22];
                pc=0x8fe; continue;
            }
            case 0x4a2: {
                v[11]=dexN(0L);
                pc=0x49e; continue;
            }
            case 0x4a4: {
                v[13]="Order time";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[14]=result[0];
                v[15]="yyyy-MM-dd HH:mm:ss";
                pc=(dexZero(v[14]))?0x4cc:0x4ae; continue;
            }
            case 0x4ae: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[16]=dexN(1L);
                v[14]=dexN(dexI(v[14])+(-1));
                pc=(dexI(v[7])>=dexI(v[14]))?0x4cc:0x4b8; continue;
            }
            case 0x4b8: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[1])),((java.lang.String)dexR(v[15]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setTime(dexL(v[3]));
                pc=0x49e; continue;
            }
            case 0x4cc: {
                v[14]="Payment time";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x50a:0x4d4; continue;
            }
            case 0x4d4: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[16]=dexN(1L);
                v[14]=dexN(dexI(v[14])+(-1));
                pc=(dexI(v[7])>=dexI(v[14]))?0x50a:0x4de; continue;
            }
            case 0x4de: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[9]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x4f8:0x4f0; continue;
            }
            case 0x4f0: {
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[1])),((java.lang.String)dexR(v[15]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setTime(dexL(v[3]));
                pc=0x49e; continue;
            }
            case 0x4f8: {
                v[3]="年";
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x49e:0x500; continue;
            }
            case 0x500: {
                v[3]="yyyy年MM月dd日 HH:mm:ss";
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[1])),((java.lang.String)dexR(v[3]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setTime(dexL(v[3]));
                pc=0x49e; continue;
            }
            case 0x50a: {
                v[13]="收款时间";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x52e:0x512; continue;
            }
            case 0x512: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[14]=dexN(1L);
                v[13]=dexN(dexI(v[13])-dexI(v[14]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x52f:0x51a; continue;
            }
            case 0x51a: {
                v[1]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[1])),((java.lang.String)dexR(v[15]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setTime(dexL(v[3]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setIncome(dexZ(v[14]));
                pc=0x49e; continue;
            }
            case 0x52e: {
                v[14]=dexN(1L);
                pc=0x52f; continue;
            }
            case 0x52f: {
                v[13]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                v[15]=dexN(7L);
                pc=(!dexEqual(v[13],v[15]))?0x56e:0x534; continue;
            }
            case 0x534: {
                v[13]="订单金额";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x56e:0x53c; continue;
            }
            case 0x53c: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[13]=dexN(dexI(v[13])-dexI(v[14]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x56e:0x543; continue;
            }
            case 0x543: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x49e:0x569; continue;
            }
            case 0x569: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setNumber(((java.lang.String)dexR(v[1])));
                pc=0x49e; continue;
            }
            case 0x56e: {
                v[13]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                pc=(!dexEqual(v[13],v[15]))?0x5d1:0x572; continue;
            }
            case 0x572: {
                v[13]="花呗收钱服务费";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(!dexZero(v[13]))?0x582:0x57a; continue;
            }
            case 0x57a: {
                v[13]="平台服务费";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x5d1:0x582; continue;
            }
            case 0x582: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[14]=dexN(1L);
                v[13]=dexN(dexI(v[13])-dexI(v[14]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x5d1:0x58a; continue;
            }
            case 0x58a: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x49e:0x5b0; continue;
            }
            case 0x5b0: {
                v[4]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[4]).value=new java.lang.StringBuilder();
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[5]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[5])))));
                v[5]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[8]=result[0];
                v[5]=dexN(Double.doubleToRawLongBits(dexD(v[5])-dexD(v[8])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(dexD(v[5]));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[1])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setNumber(((java.lang.String)dexR(v[1])));
                pc=0x49e; continue;
            }
            case 0x5d1: {
                v[13]="Item description";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x611:0x5d9; continue;
            }
            case 0x5d9: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[14]=dexN(1L);
                v[13]=dexN(dexI(v[13])-dexI(v[14]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x611:0x5e1; continue;
            }
            case 0x5e1: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="Item description";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                v[3]="余额宝";
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x49e:0x5fe; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block5(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x5fe: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                v[3]="收益发放";
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x49e:0x60a; continue;
            }
            case 0x60a: {
                v[1]="余额宝";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[1])));
                pc=0x49e; continue;
            }
            case 0x611: {
                v[13]="Order Details";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x65a:0x619; continue;
            }
            case 0x619: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[13]=result[0];
                v[14]=dexN(1L);
                v[13]=dexN(dexI(v[13])-dexI(v[14]));
                pc=(dexI(v[7])>=dexI(v[13]))?0x65a:0x621; continue;
            }
            case 0x621: {
                v[1]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[3]=result[0];
                v[4]=v[20];
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x638:0x62f; continue;
            }
            case 0x62f: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x638; continue;
            }
            case 0x638: {
                v[1]="商品图片";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x49e:0x644; continue;
            }
            case 0x644: {
                v[1]=dexN(dexI(v[7])+(2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                v[13]=v[22];
                com.google.android.accessibility.selecttospeak.SelectToSpeakService.E(((android.content.Context)dexR(v[13])),((java.lang.String)dexR(v[1])));
                pc=0x8fe; continue;
            }
            case 0x65a: {
                v[13]=v[22];
                v[14]="交易详情";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x6b6:0x664; continue;
            }
            case 0x664: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[16]=dexN(1L);
                v[14]=dexN(dexI(v[14])+(-1));
                pc=(dexI(v[7])>=dexI(v[14]))?0x6b6:0x66e; continue;
            }
            case 0x66e: {
                v[1]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x8fe:0x67c; continue;
            }
            case 0x67c: {
                v[3]="更多";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x8fe:0x684; continue;
            }
            case 0x684: {
                v[3]="推荐服务";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x8fe:0x68c; continue;
            }
            case 0x68c: {
                v[3]="服务推荐";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x8fe:0x694; continue;
            }
            case 0x694: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[1]=result[0];
                v[3]=dexN(2L);
                v[1]=dexN(dexI(v[1])-dexI(v[3]));
                pc=(dexI(v[7])>=dexI(v[1]))?0x8fe:0x69c; continue;
            }
            case 0x69c: {
                v[1]=dexN(dexI(v[7])+(2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x8fe:0x6aa; continue;
            }
            case 0x6aa: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                com.google.android.accessibility.selecttospeak.SelectToSpeakService.E(((android.content.Context)dexR(v[13])),((java.lang.String)dexR(v[1])));
                pc=0x8fe; continue;
            }
            case 0x6b6: {
                v[14]="Payment Instructions";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x6db:0x6be; continue;
            }
            case 0x6be: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[16]=dexN(1L);
                v[14]=dexN(dexI(v[14])+(-1));
                pc=(dexI(v[7])>=dexI(v[14]))?0x6db:0x6c8; continue;
            }
            case 0x6c8: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="Payment Instructions";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x8fe; continue;
            }
            case 0x6db: {
                v[14]="充值说明";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x700:0x6e3; continue;
            }
            case 0x6e3: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[16]=dexN(1L);
                v[14]=dexN(dexI(v[14])+(-1));
                pc=(dexI(v[7])>=dexI(v[14]))?0x700:0x6ed; continue;
            }
            case 0x6ed: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="充值说明";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x8fe; continue;
            }
            case 0x700: {
                v[14]="付款备注";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x725:0x708; continue;
            }
            case 0x708: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[16]=dexN(1L);
                v[14]=dexN(dexI(v[14])+(-1));
                pc=(dexI(v[7])>=dexI(v[14]))?0x725:0x712; continue;
            }
            case 0x712: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="付款备注";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x8fe; continue;
            }
            case 0x725: {
                v[14]="红包说明";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x74a:0x72d; continue;
            }
            case 0x72d: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[16]=dexN(1L);
                v[14]=dexN(dexI(v[14])+(-1));
                pc=(dexI(v[7])>=dexI(v[14]))?0x74a:0x737; continue;
            }
            case 0x737: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="红包说明";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x8fe; continue;
            }
            case 0x74a: {
                v[14]="Reason";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x76f:0x752; continue;
            }
            case 0x752: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[16]=dexN(1L);
                v[14]=dexN(dexI(v[14])+(-1));
                pc=(dexI(v[7])>=dexI(v[14]))?0x76f:0x75c; continue;
            }
            case 0x75c: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="Reason";
                result[0]=((java.lang.String)dexR(v[3])).replaceFirst(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[1])));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x8fe; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block6(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x76f: {
                v[14]="Transfer notes";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x816:0x777; continue;
            }
            case 0x777: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[16]=dexN(1L);
                v[14]=dexN(dexI(v[14])+(-1));
                pc=(dexI(v[7])>=dexI(v[14]))?0x816:0x781; continue;
            }
            case 0x781: {
                v[1]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="Transfer";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x7ce:0x791; continue;
            }
            case 0x791: {
                result[0]=dexN((((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).isIncome())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x7b5:0x797; continue;
            }
            case 0x797: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="收到";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getShopName();
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                v[3]="转账";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x7d7; continue;
            }
            case 0x7b5: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="转账给";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getShopName();
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x7d7; continue;
            }
            case 0x7ce: {
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                pc=0x7d7; continue;
            }
            case 0x7d7: {
                result[0]=dexN((((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).isIncome())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x7fc:0x7dd; continue;
            }
            case 0x7dd: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="收到";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getShopName();
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                v[3]="转账";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x8fe; continue;
            }
            case 0x7fc: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="转账给";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getShopName();
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x8fe; continue;
            }
            case 0x816: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getDiscount();
                v[14]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x8a6:0x820; continue;
            }
            case 0x820: {
                v[14]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                pc=(dexZero(v[14]))?0x82c:0x824; continue;
            }
            case 0x824: {
                v[14]="-￥";
                result[0]=dexN((((java.lang.String)dexR(v[8])).startsWith(((java.lang.String)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(!dexZero(v[14]))?0x85c:0x82c; continue;
            }
            case 0x82c: {
                v[14]="碰一下支付立减";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(!dexZero(v[14]))?0x85c:0x834; continue;
            }
            case 0x834: {
                v[14]="支付宝随机立减";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(!dexZero(v[14]))?0x85c:0x83c; continue;
            }
            case 0x83c: {
                v[14]="碰一下立减";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(!dexZero(v[14]))?0x85c:0x844; continue;
            }
            case 0x844: {
                v[14]="碰一下共减";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(!dexZero(v[14]))?0x85c:0x84c; continue;
            }
            case 0x84c: {
                v[14]="视频红包";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(!dexZero(v[14]))?0x85c:0x854; continue;
            }
            case 0x854: {
                v[14]="优惠";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x8a6:0x85c; continue;
            }
            case 0x85c: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[16]=dexN(1L);
                v[14]=dexN(dexI(v[14])+(-1));
                pc=(dexI(v[7])>=dexI(v[14]))?0x8a6:0x866; continue;
            }
            case 0x866: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x884:0x874; continue;
            }
            case 0x874: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[14]=result[0];
                v[15]=dexN(2L);
                v[14]=dexN(dexI(v[14])-dexI(v[15]));
                pc=(dexI(v[7])>=dexI(v[14]))?0x884:0x87c; continue;
            }
            case 0x87c: {
                v[8]=dexN(dexI(v[7])+(2));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                pc=0x884; continue;
            }
            case 0x884: {
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x8fe:0x8a2; continue;
            }
            case 0x8a2: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setDiscount(((java.lang.String)dexR(v[1])));
                pc=0x8fe; continue;
            }
            case 0x8a6: {
                v[14]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).a);
                pc=(dexEqual(v[14],v[15]))?0x8fe:0x8aa; continue;
            }
            case 0x8aa: {
                v[14]="订单金额";
                result[0]=dexN((((java.lang.String)dexR(v[8])).equals(((java.lang.Object)dexR(v[14]))))?1:0);
                v[8]=result[0];
                pc=(dexZero(v[8]))?0x8fe:0x8b2; continue;
            }
            case 0x8b2: {
                result[0]=dexN(((java.util.List)dexR(v[23])).size());
                v[8]=result[0];
                v[14]=dexN(1L);
                v[8]=dexN(dexI(v[8])-dexI(v[14]));
                pc=(dexI(v[7])>=dexI(v[8]))?0x8fe:0x8ba; continue;
            }
            case 0x8ba: {
                v[8]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[2])).get(dexI(v[8]));
                v[8]=result[0];
                v[8]=((java.lang.String)dexR(v[8]));
                result[0]=((java.lang.String)dexR(v[8])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[1])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[1])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[1])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x8fe:0x8e0; continue;
            }
            case 0x8e0: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[3]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x8fe:0x8ea; continue;
            }
            case 0x8ea: {
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[1])))));
                v[3]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[1]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[1])))));
                v[5]=result[0];
                v[3]=dexN(Double.doubleToRawLongBits(dexD(v[3])-dexD(v[5])));
                result[0]=com.wangc.bill.utils.h2.t(dexD(v[3]));
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setDiscount(((java.lang.String)dexR(v[1])));
                pc=0x8fe; continue;
            }
            case 0x8fe: {
                v[7]=dexN(dexI(v[7])+(1));
                v[3]=v[12];
                v[1]=v[13];
                v[5]=v[17];
                v[4]=dexN(0L);
                pc=0x37; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block7(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x907: {
                v[12]=v[3];
                v[17]=v[5];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x93d:0x914; continue;
            }
            case 0x914: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x93d:0x91e; continue;
            }
            case 0x91e: {
                result[0]=dexN((((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).isIncome())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x931:0x924; continue;
            }
            case 0x924: {
                v[1]="支付宝收款";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x93d; continue;
            }
            case 0x931: {
                v[1]="支付宝付款";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setRemark(((java.lang.String)dexR(v[1])));
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setShopName(((java.lang.String)dexR(v[1])));
                pc=0x93d; continue;
            }
            case 0x93d: {
                result[0]=dexN((((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).isIncome())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x953:0x943; continue;
            }
            case 0x943: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x953:0x94d; continue;
            }
            case 0x94d: {
                v[1]=v[17];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[1])));
                pc=0x955; continue;
            }
            case 0x953: {
                v[1]=v[17];
                pc=0x955; continue;
            }
            case 0x955: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[2]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[2]))))?1:0);
                v[2]=result[0];
                pc=(!dexZero(v[2]))?0x96a:0x95f; continue;
            }
            case 0x95f: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[2]=result[0];
                result[0]=((java.lang.String)dexR(v[2])).trim();
                v[2]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[2])));
                pc=0x96a; continue;
            }
            case 0x96a: {
                v[2]="账户余额";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[2])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[2]=result[0];
                pc=(!dexZero(v[2]))?0x98e:0x976; continue;
            }
            case 0x976: {
                v[2]="余额";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[2])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[2]=result[0];
                pc=(!dexZero(v[2]))?0x98e:0x982; continue;
            }
            case 0x982: {
                v[2]="可用余额";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getAsset();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[2])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[2]=result[0];
                pc=(dexZero(v[2]))?0x991:0x98e; continue;
            }
            case 0x98e: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).setAsset(((java.lang.String)dexR(v[1])));
                pc=0x991; continue;
            }
            case 0x991: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getRemark();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x9a6:0x99b; continue;
            }
            case 0x99b: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[12])).getNumber();
                v[1]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x9a6:0x9a5; continue;
            }
            case 0x9a5: {
                result[1]=v[12]; return -1;
            }
            case 0x9a6: {
                v[1]=dexN(0L);
                result[1]=v[1]; return -1;
            }
            default: return pc;
        }
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
    /** DEX-exact branch transcription of u; original register and block addresses retained. */
    public com.wangc.bill.http.entity.BillInfo u(java.util.List arg0) {
        Object[] v=new Object[25]; Object[] result=new Object[2]; int pc=0;
        v[23]=this;
        v[24]=arg0;
        while(true) {
            if(pc==-1) { return ((com.wangc.bill.http.entity.BillInfo)dexR(result[1])); }
            if(pc<0x11f) pc=recovered_u_block0(v,result,pc);
            else if(pc<0x261) pc=recovered_u_block1(v,result,pc);
            else if(pc<0x3cb) pc=recovered_u_block2(v,result,pc);
            else pc=recovered_u_block3(v,result,pc);
        }
    }
    private int recovered_u_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                v[0]=v[23];
                v[1]=v[24];
                result[0]=dexN((((java.util.List)dexR(v[24])).isEmpty())?1:0);
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
                v[4]=dexN(1L);
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setTransfer(dexZ(v[4]));
                v[5]="支付宝";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setOrigin(((java.lang.String)dexR(v[5])));
                v[6]="充值成功";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[6])),dexZ(v[4])))?1:0);
                v[7]=result[0];
                v[8]="转出成功";
                v[9]="转入成功";
                v[10]="提现说明";
                v[11]="提现金额";
                v[12]="充值说明";
                v[13]=dexN(0L);
                pc=(!dexZero(v[7]))?0x9c:0x2d; continue;
            }
            case 0x2d: {
                v[7]="余额转入";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[7])),dexZ(v[4])))?1:0);
                v[7]=result[0];
                pc=(!dexZero(v[7]))?0x9c:0x35; continue;
            }
            case 0x35: {
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[12])),dexZ(v[4])))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x3c:0x3b; continue;
            }
            case 0x3b: {
                pc=0x9c; continue;
            }
            case 0x3c: {
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[11])),dexZ(v[4])))?1:0);
                v[7]=result[0];
                pc=(!dexZero(v[7]))?0x98:0x42; continue;
            }
            case 0x42: {
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[10])),dexZ(v[4])))?1:0);
                v[7]=result[0];
                pc=(!dexZero(v[7]))?0x98:0x48; continue;
            }
            case 0x48: {
                v[7]="提现成功";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[7])),dexZ(v[4])))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x51:0x50; continue;
            }
            case 0x50: {
                pc=0x98; continue;
            }
            case 0x51: {
                v[7]="网商银行转账";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[7])),dexZ(v[4])))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x5f:0x59; continue;
            }
            case 0x59: {
                v[7]="网商银行余利宝";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setToAsset(((java.lang.String)dexR(v[7])));
                pc=0x9f; continue;
            }
            case 0x5f: {
                v[7]="余额宝-单次转入";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[7])),dexZ(v[4])))?1:0);
                v[7]=result[0];
                v[14]="余额宝";
                pc=(dexZero(v[7]))?0x6d:0x69; continue;
            }
            case 0x69: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setToAsset(((java.lang.String)dexR(v[14])));
                pc=0x9f; continue;
            }
            case 0x6d: {
                v[7]="余额宝-转出到余额";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[7])),dexZ(v[4])))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x7c:0x75; continue;
            }
            case 0x75: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setFromAsset(((java.lang.String)dexR(v[14])));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setToAsset(((java.lang.String)dexR(v[5])));
                pc=0x9f; continue;
            }
            case 0x7c: {
                v[7]="开始计算收益";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[7])),dexZ(v[4])))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x8e:0x84; continue;
            }
            case 0x84: {
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[9])),dexZ(v[13])))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x8e:0x8a; continue;
            }
            case 0x8a: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setToAsset(((java.lang.String)dexR(v[14])));
                pc=0x9f; continue;
            }
            case 0x8e: {
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[1])),((java.lang.String)dexR(v[8])),dexZ(v[4])))?1:0);
                v[7]=result[0];
                pc=(dexZero(v[7]))?0x9f:0x94; continue;
            }
            case 0x94: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setFromAsset(((java.lang.String)dexR(v[14])));
                pc=0x9f; continue;
            }
            case 0x98: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setFromAsset(((java.lang.String)dexR(v[5])));
                pc=0x9f; continue;
            }
            case 0x9c: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setToAsset(((java.lang.String)dexR(v[5])));
                pc=0x9f; continue;
            }
            case 0x9f: {
                v[7]=v[13];
                pc=0xa0; continue;
            }
            case 0xa0: {
                result[0]=dexN(((java.util.List)dexR(v[24])).size());
                v[14]=result[0];
                pc=(dexI(v[7])>=dexI(v[14]))?0x46d:0xa6; continue;
            }
            case 0xa6: {
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[7]));
                v[14]=result[0];
                v[14]=((java.lang.String)dexR(v[14]));
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[15]=result[0];
                v[3]="支出";
                v[13]="元";
                v[4]="¥";
                v[0]="0";
                v[17]=v[11];
                v[11]="NaN";
                v[18]=v[5];
                v[5]=",";
                v[19]=v[12];
                v[12]="";
                pc=(!dexZero(v[15]))?0xd2:0xc6; continue;
            }
            case 0xc6: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[15]=result[0];
                pc=(!dexZero(v[15]))?0xd2:0xcc; continue;
            }
            case 0xcc: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[9]))))?1:0);
                v[15]=result[0];
                pc=(dexZero(v[15]))?0x130:0xd2; continue;
            }
            case 0xd2: {
                result[0]=dexN(((java.util.List)dexR(v[24])).size());
                v[15]=result[0];
                v[16]=dexN(1L);
                v[15]=dexN(dexI(v[15])+(-1));
                pc=(dexI(v[7])>=dexI(v[15]))?0x130:0xdc; continue;
            }
            case 0xdc: {
                v[14]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[14]));
                v[14]=result[0];
                v[14]=((java.lang.String)dexR(v[14]));
                result[0]=((java.lang.String)dexR(v[14])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[12])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[12])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[12])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[12])));
                v[3]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getNumber();
                v[4]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x11f:0xfe; continue;
            }
            case 0xfe: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x11f:0x104; continue;
            }
            case 0x104: {
                v[0]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[0]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[3]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[3]))));
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(dexD(v[3]));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(((java.lang.String)dexR(v[12])));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).toString();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x128; continue;
            }
            default: return pc;
        }
    }
    private int recovered_u_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x11f: {
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x128:0x125; continue;
            }
            case 0x125: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x128; continue;
            }
            case 0x128: {
                v[20]=v[6];
                pc=0x12a; continue;
            }
            case 0x12a: {
                v[0]=v[19];
                pc=0x12c; continue;
            }
            case 0x12c: {
                v[4]=dexN(0L);
                pc=0x12d; continue;
            }
            case 0x12d: {
                v[5]=dexN(1L);
                pc=0x45d; continue;
            }
            case 0x130: {
                v[15]="提现金额¥";
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[15]))))?1:0);
                v[20]=result[0];
                pc=(dexZero(v[20]))?0x17d:0x138; continue;
            }
            case 0x138: {
                result[0]=((java.lang.String)dexR(v[14])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[12])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[12])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[12])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[12])));
                v[3]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getNumber();
                v[4]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x173:0x152; continue;
            }
            case 0x152: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x173:0x158; continue;
            }
            case 0x158: {
                v[0]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[0]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[3]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[3]))));
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(dexD(v[3]));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(((java.lang.String)dexR(v[12])));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).toString();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x128; continue;
            }
            case 0x173: {
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x128:0x179; continue;
            }
            case 0x179: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x128; continue;
            }
            case 0x17d: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[8]))))?1:0);
                v[15]=result[0];
                v[20]=v[6];
                v[6]=dexN(2L);
                pc=(dexZero(v[15]))?0x1dc:0x186; continue;
            }
            case 0x186: {
                result[0]=dexN(((java.util.List)dexR(v[24])).size());
                v[15]=result[0];
                v[15]=dexN(dexI(v[15])-dexI(v[6]));
                pc=(dexI(v[7])>=dexI(v[15]))?0x1dc:0x18d; continue;
            }
            case 0x18d: {
                v[15]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[15]));
                v[15]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[15]))))?1:0);
                v[15]=result[0];
                pc=(dexZero(v[15]))?0x1dc:0x199; continue;
            }
            case 0x199: {
                v[3]=dexN(dexI(v[7])+(2));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[12])));
                v[3]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getNumber();
                v[4]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1d1:0x1af; continue;
            }
            case 0x1af: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1d1:0x1b5; continue;
            }
            case 0x1b5: {
                v[0]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[0]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[3]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[3]))));
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(dexD(v[3]));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(((java.lang.String)dexR(v[12])));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).toString();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x12a; continue;
            }
            case 0x1d1: {
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x12a:0x1d7; continue;
            }
            case 0x1d7: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x12a; continue;
            }
            case 0x1dc: {
                v[15]="成功转出";
                result[0]=dexN((((java.lang.String)dexR(v[14])).startsWith(((java.lang.String)dexR(v[15]))))?1:0);
                v[21]=result[0];
                pc=(dexZero(v[21]))?0x251:0x1e4; continue;
            }
            case 0x1e4: {
                v[6]="元至";
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[6]))))?1:0);
                v[22]=result[0];
                pc=(dexZero(v[22]))?0x251:0x1ec; continue;
            }
            case 0x1ec: {
                result[0]=dexN(((java.lang.String)dexR(v[14])).indexOf(((java.lang.String)dexR(v[15]))));
                v[3]=result[0];
                v[3]=dexN(dexI(v[3])+(4));
                result[0]=dexN(((java.lang.String)dexR(v[14])).indexOf(((java.lang.String)dexR(v[6]))));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[14])).substring(dexI(v[3]),dexI(v[4]));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[12])));
                v[3]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getNumber();
                v[4]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x229:0x208; continue;
            }
            case 0x208: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x229:0x20e; continue;
            }
            case 0x20e: {
                v[0]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[0]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[3]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[3]))));
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(dexD(v[3]));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(((java.lang.String)dexR(v[12])));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).toString();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x232; continue;
            }
            case 0x229: {
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x232:0x22f; continue;
            }
            case 0x22f: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x232; continue;
            }
            case 0x232: {
                result[0]=dexN(((java.lang.String)dexR(v[14])).indexOf(((java.lang.String)dexR(v[6]))));
                v[0]=result[0];
                v[3]=dexN(1L);
                v[0]=dexN(dexI(v[0])+dexI(v[3]));
                result[0]=((java.lang.String)dexR(v[14])).substring(dexI(v[0]));
                v[0]=result[0];
                v[3]="。";
                result[0]=((java.lang.String)dexR(v[0])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[12])));
                v[0]=result[0];
                v[3]="支付宝账户余额";
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x24c:0x24a; continue;
            }
            case 0x24a: {
                v[0]=v[18];
                pc=0x24c; continue;
            }
            case 0x24c: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setToAsset(((java.lang.String)dexR(v[0])));
                pc=0x12a; continue;
            }
            case 0x251: {
                v[6]="交易成功";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x261:0x259; continue;
            }
            case 0x259: {
                v[6]="还款成功";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(dexZero(v[6]))?0x2b2:0x261; continue;
            }
            default: return pc;
        }
    }
    private int recovered_u_block2(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x261: {
                v[6]=dexN(dexI(v[7])+(-1));
                pc=(dexI(v[6])<0)?0x2b2:0x265; continue;
            }
            case 0x265: {
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[6]));
                v[6]=result[0];
                v[6]=((java.lang.String)dexR(v[6]));
                result[0]=((java.lang.String)dexR(v[6])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[12])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[12])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[12])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[12])));
                v[3]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getNumber();
                v[4]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x2a7:0x285; continue;
            }
            case 0x285: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x2a7:0x28b; continue;
            }
            case 0x28b: {
                v[0]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[0]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[3]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[3]))));
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(dexD(v[3]));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(((java.lang.String)dexR(v[12])));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).toString();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x12a; continue;
            }
            case 0x2a7: {
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x12a:0x2ad; continue;
            }
            case 0x2ad: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x12a; continue;
            }
            case 0x2b2: {
                v[3]="成功转入";
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x2ff:0x2ba; continue;
            }
            case 0x2ba: {
                v[3]="成功转入";
                result[0]=((java.lang.String)dexR(v[14])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[12])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[12])));
                v[3]=result[0];
                result[0]=((java.lang.String)dexR(v[3])).replace(((java.lang.CharSequence)dexR(v[13])),((java.lang.CharSequence)dexR(v[12])));
                v[3]=result[0];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getNumber();
                v[4]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x2f4:0x2d2; continue;
            }
            case 0x2d2: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x2f4:0x2d8; continue;
            }
            case 0x2d8: {
                v[0]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[0]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[3])))));
                v[3]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[3]))));
                v[3]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(dexD(v[3]));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).append(((java.lang.String)dexR(v[12])));
                result[0]=((java.lang.StringBuilder)dexR(v[0])).toString();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x12a; continue;
            }
            case 0x2f4: {
                result[0]=dexN((((java.lang.String)dexR(v[3])).equals(((java.lang.Object)dexR(v[11]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x12a:0x2fa; continue;
            }
            case 0x2fa: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setNumber(((java.lang.String)dexR(v[0])));
                pc=0x12a; continue;
            }
            case 0x2ff: {
                v[0]="到账银行卡";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x31f:0x307; continue;
            }
            case 0x307: {
                v[0]="提现到";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x31f:0x30f; continue;
            }
            case 0x30f: {
                v[0]="还款到";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x31f:0x317; continue;
            }
            case 0x317: {
                v[0]="到账账户";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(dexZero(v[0]))?0x334:0x31f; continue;
            }
            case 0x31f: {
                result[0]=dexN(((java.util.List)dexR(v[24])).size());
                v[0]=result[0];
                v[3]=dexN(1L);
                v[0]=dexN(dexI(v[0])-dexI(v[3]));
                pc=(dexI(v[7])>=dexI(v[0]))?0x334:0x327; continue;
            }
            case 0x327: {
                v[0]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[0]));
                v[0]=result[0];
                v[0]=((java.lang.String)dexR(v[0]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setToAsset(((java.lang.String)dexR(v[0])));
                pc=0x12a; continue;
            }
            case 0x334: {
                v[0]="付款方式";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x344:0x33c; continue;
            }
            case 0x33c: {
                v[0]="对方账户";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(dexZero(v[0]))?0x359:0x344; continue;
            }
            case 0x344: {
                result[0]=dexN(((java.util.List)dexR(v[24])).size());
                v[0]=result[0];
                v[3]=dexN(1L);
                v[0]=dexN(dexI(v[0])-dexI(v[3]));
                pc=(dexI(v[7])>=dexI(v[0]))?0x359:0x34c; continue;
            }
            case 0x34c: {
                v[0]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[0]));
                v[0]=result[0];
                v[0]=((java.lang.String)dexR(v[0]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setFromAsset(((java.lang.String)dexR(v[0])));
                pc=0x12a; continue;
            }
            case 0x359: {
                v[0]="服务费";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(dexZero(v[0]))?0x38c:0x361; continue;
            }
            case 0x361: {
                result[0]=dexN(((java.util.List)dexR(v[24])).size());
                v[0]=result[0];
                v[3]=dexN(1L);
                v[0]=dexN(dexI(v[0])-dexI(v[3]));
                pc=(dexI(v[7])>=dexI(v[0]))?0x38c:0x369; continue;
            }
            case 0x369: {
                v[0]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[0]));
                v[0]=result[0];
                v[0]=((java.lang.String)dexR(v[0]));
                result[0]=((java.lang.String)dexR(v[0])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[12])));
                v[0]=result[0];
                result[0]=((java.lang.String)dexR(v[0])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[12])));
                v[0]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[0]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x12a:0x37f; continue;
            }
            case 0x37f: {
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[0])))));
                v[3]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[3]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setServiceCharge(dexD(v[3]));
                pc=0x12a; continue;
            }
            case 0x38c: {
                v[0]="创建时间";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(dexZero(v[0]))?0x3b5:0x394; continue;
            }
            case 0x394: {
                result[0]=dexN(((java.util.List)dexR(v[24])).size());
                v[0]=result[0];
                v[3]=dexN(1L);
                v[0]=dexN(dexI(v[0])-dexI(v[3]));
                pc=(dexI(v[7])>=dexI(v[0]))?0x3b5:0x39c; continue;
            }
            case 0x39c: {
                v[0]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[0]));
                v[0]=result[0];
                v[0]=((java.lang.String)dexR(v[0]));
                v[3]="创建时间";
                result[0]=((java.lang.String)dexR(v[0])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[12])));
                v[0]=result[0];
                v[3]="yyyy-MM-dd HH:mm:ss";
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[0])),((java.lang.String)dexR(v[3]))));
                v[3]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setTime(dexL(v[3]));
                pc=0x12a; continue;
            }
            case 0x3b5: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[10]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x3d4:0x3bb; continue;
            }
            case 0x3bb: {
                v[0]="转账备注";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x3d4:0x3c3; continue;
            }
            case 0x3c3: {
                v[0]=v[19];
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[0]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x3d6:0x3cb; continue;
            }
            default: return pc;
        }
    }
    private int recovered_u_block3(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x3cb: {
                v[3]="商品说明";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x3eb:0x3d3; continue;
            }
            case 0x3d3: {
                pc=0x3d6; continue;
            }
            case 0x3d4: {
                v[0]=v[19];
                pc=0x3d6; continue;
            }
            case 0x3d6: {
                result[0]=dexN(((java.util.List)dexR(v[24])).size());
                v[3]=result[0];
                v[4]=dexN(1L);
                v[3]=dexN(dexI(v[3])-dexI(v[4]));
                pc=(dexI(v[7])>=dexI(v[3]))?0x3eb:0x3de; continue;
            }
            case 0x3de: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setRemark(((java.lang.String)dexR(v[3])));
                pc=0x12c; continue;
            }
            case 0x3eb: {
                v[3]="转出说明";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x441:0x3f3; continue;
            }
            case 0x3f3: {
                result[0]=dexN(((java.util.List)dexR(v[24])).size());
                v[3]=result[0];
                v[4]=dexN(1L);
                v[3]=dexN(dexI(v[3])-dexI(v[4]));
                pc=(dexI(v[7])>=dexI(v[3]))?0x441:0x3fb; continue;
            }
            case 0x3fb: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                v[4]="-";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x415:0x40b; continue;
            }
            case 0x40b: {
                v[4]="-";
                result[0]=dexN(((java.lang.String)dexR(v[3])).indexOf(((java.lang.String)dexR(v[4]))));
                v[4]=result[0];
                v[5]=dexN(0L);
                result[0]=((java.lang.String)dexR(v[3])).substring(dexI(v[5]),dexI(v[4]));
                pc=0x415; continue;
            }
            case 0x415: {
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x12c:0x41b; continue;
            }
            case 0x41b: {
                v[4]="转出到";
                result[0]=dexN((((java.lang.String)dexR(v[3])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x12c:0x423; continue;
            }
            case 0x423: {
                v[4]="转出到";
                result[0]=((java.lang.String)dexR(v[3])).split(((java.lang.String)dexR(v[4])));
                v[3]=result[0];
                v[4]=dexN(dexLength(dexR(v[3])));
                v[5]=dexN(2L);
                pc=(!dexEqual(v[4],v[5]))?0x12c:0x42d; continue;
            }
            case 0x42d: {
                v[4]=dexN(0L);
                v[5]=((Object[])dexR(v[3]))[dexI(v[4])];
                v[6]="转出说明";
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[12])));
                v[5]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setFromAsset(((java.lang.String)dexR(v[5])));
                v[5]=dexN(1L);
                v[3]=((Object[])dexR(v[3]))[dexI(v[5])];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setToAsset(((java.lang.String)dexR(v[3])));
                pc=0x12d; continue;
            }
            case 0x441: {
                v[4]=dexN(0L);
                v[3]="转入账户";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x12d:0x44a; continue;
            }
            case 0x44a: {
                result[0]=dexN(((java.util.List)dexR(v[24])).size());
                v[3]=result[0];
                v[5]=dexN(1L);
                v[3]=dexN(dexI(v[3])-dexI(v[5]));
                pc=(dexI(v[7])>=dexI(v[3]))?0x45d:0x452; continue;
            }
            case 0x452: {
                v[3]=dexN(dexI(v[7])+(1));
                result[0]=((java.util.List)dexR(v[1])).get(dexI(v[3]));
                v[3]=result[0];
                v[3]=((java.lang.String)dexR(v[3]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setToAsset(((java.lang.String)dexR(v[3])));
                pc=0x45d; continue;
            }
            case 0x45d: {
                v[7]=dexN(dexI(v[7])+(1));
                v[12]=v[0];
                v[13]=v[4];
                v[4]=v[5];
                v[11]=v[17];
                v[5]=v[18];
                v[6]=v[20];
                v[3]=dexN(0L);
                v[0]=v[23];
                pc=0xa0; continue;
            }
            case 0x46d: {
                v[18]=v[5];
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getFromAsset();
                v[0]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x484:0x479; continue;
            }
            case 0x479: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getFromAsset();
                v[0]=result[0];
                result[0]=((java.lang.String)dexR(v[0])).trim();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setFromAsset(((java.lang.String)dexR(v[0])));
                pc=0x484; continue;
            }
            case 0x484: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getToAsset();
                v[0]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x499:0x48e; continue;
            }
            case 0x48e: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getToAsset();
                v[0]=result[0];
                result[0]=((java.lang.String)dexR(v[0])).trim();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setToAsset(((java.lang.String)dexR(v[0])));
                pc=0x499; continue;
            }
            case 0x499: {
                v[0]="账户余额";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getFromAsset();
                v[1]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[0])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x4b1:0x4a5; continue;
            }
            case 0x4a5: {
                v[0]="余额";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getFromAsset();
                v[1]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[0])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[0]=result[0];
                pc=(dexZero(v[0]))?0x4b4:0x4b1; continue;
            }
            case 0x4b1: {
                v[0]=v[18];
                pc=0x4b7; continue;
            }
            case 0x4b4: {
                v[0]=v[18];
                pc=0x4ba; continue;
            }
            case 0x4b7: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setFromAsset(((java.lang.String)dexR(v[0])));
                pc=0x4ba; continue;
            }
            case 0x4ba: {
                v[1]="账户余额";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getToAsset();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x4d2:0x4c6; continue;
            }
            case 0x4c6: {
                v[1]="余额";
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getToAsset();
                v[3]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x4d5:0x4d2; continue;
            }
            case 0x4d2: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).setToAsset(((java.lang.String)dexR(v[0])));
                pc=0x4d5; continue;
            }
            case 0x4d5: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[2])).getNumber();
                v[0]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x4e0:0x4df; continue;
            }
            case 0x4df: {
                result[1]=v[2]; return -1;
            }
            case 0x4e0: {
                v[0]=dexN(0L);
                result[1]=v[0]; return -1;
            }
            default: return pc;
        }
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

    /** DEX-exact branch transcription of w; original register and block addresses retained. */
    public void w(android.content.Context arg0, java.lang.String arg1, android.view.accessibility.AccessibilityNodeInfo arg2) {
        Object[] v=new Object[21]; Object[] result=new Object[2]; int pc=0;
        v[17]=this;
        v[18]=arg0;
        v[19]=arg1;
        v[20]=arg2;
        while(true) {
            if(pc==-1) { return; }
            if(pc<0xce) pc=recovered_w_block0(v,result,pc);
            else if(pc<0x193) pc=recovered_w_block1(v,result,pc);
            else if(pc<0x266) pc=recovered_w_block2(v,result,pc);
            else if(pc<0x355) pc=recovered_w_block3(v,result,pc);
            else pc=recovered_w_block4(v,result,pc);
        }
    }
    private int recovered_w_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                v[0]=v[17];
                v[1]=v[19];
                v[2]=v[20];
                v[3]="com.eg.android.AlipayGphone.AlipayLogin";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                v[4]="使用密码";
                v[5]="确认付款";
                v[6]="向商家付款";
                v[7]="优先使用此付款方式";
                v[8]="确认即付款";
                v[9]="碰一下，支付更便捷";
                v[10]=dexN(0L);
                pc=(dexZero(v[3]))?0x25:0x1b; continue;
            }
            case 0x1b: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[10]);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[10]);
                ((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f=dexZ(v[10]);
                pc=0x21; continue;
            }
            case 0x21: {
                v[16]=v[8];
                pc=0x37a; continue;
            }
            case 0x25: {
                v[3]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                v[11]=dexN(1L);
                pc=(!dexZero(v[3]))?0x38:0x2a; continue;
            }
            case 0x2a: {
                v[3]="com.alipay.android.msp.ui.views.MspContainerActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x33:0x32; continue;
            }
            case 0x32: {
                pc=0x38; continue;
            }
            case 0x33: {
                v[16]=v[8];
                v[1]=v[11];
                pc=0x378; continue;
            }
            case 0x38: {
                v[3]="com.alipay.android.msp.ui.views.MspUniRenderActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x33:0x40; continue;
            }
            case 0x40: {
                v[3]="com.alipay.android.phone.discovery.envelope.get.SnsCouponDetailActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x49:0x48; continue;
            }
            case 0x48: {
                pc=0x33; continue;
            }
            case 0x49: {
                v[3]="com.alipay.android.phone.nfcpay.ui.NfcPayActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x6d:0x51; continue;
            }
            case 0x51: {
                v[3]="com.alipay.android.phone.businesscommon.ucdp.nfc.activity.NResPageActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x5a:0x59; continue;
            }
            case 0x59: {
                pc=0x6d; continue;
            }
            case 0x5a: {
                v[3]="com.alipay.mobile.scan.as.main.MainCaptureActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x6a:0x62; continue;
            }
            case 0x62: {
                v[3]="com.alipay.mobile.onsitepay.merge.OnsitepayActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x6f:0x6a; continue;
            }
            case 0x6a: {
                ((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f=dexZ(v[10]);
                pc=0x6f; continue;
            }
            case 0x6d: {
                ((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f=dexZ(v[11]);
                pc=0x6f; continue;
            }
            case 0x6f: {
                pc=(dexZero(v[2]))?0x21:0x71; continue;
            }
            case 0x71: {
                result[0]=((com.wangc.bill.auto.action.c)dexR(v[0])).g(((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])));
                v[3]=result[0];
                v[12]=new DexReference(); // new java.util.HashMap
                ((DexReference)v[12]).value=new java.util.HashMap();
                result[0]=((java.util.List)dexR(v[3])).iterator();
                v[13]=result[0];
                pc=0x7e; continue;
            }
            case 0x7e: {
                result[0]=dexN((((java.util.Iterator)dexR(v[13])).hasNext())?1:0);
                v[14]=result[0];
                pc=(dexZero(v[14]))?0x90:0x84; continue;
            }
            case 0x84: {
                result[0]=((java.util.Iterator)dexR(v[13])).next();
                v[14]=result[0];
                v[14]=((java.lang.String)dexR(v[14]));
                v[15]=java.lang.Boolean.TRUE;
                result[0]=((java.util.HashMap)dexR(v[12])).put(((java.lang.Object)dexR(v[14])),((java.lang.Object)dexR(v[15])));
                pc=0x7e; continue;
            }
            case 0x90: {
                v[13]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                pc=(!dexZero(v[13]))?0xa2:0x94; continue;
            }
            case 0x94: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[9]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0xa2:0x9a; continue;
            }
            case 0x9a: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[8]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0xa2:0xa0; continue;
            }
            case 0xa0: {
                ((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f=dexZ(v[11]);
                pc=0xa2; continue;
            }
            case 0xa2: {
                v[13]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                pc=(!dexZero(v[13]))?0xb8:0xa6; continue;
            }
            case 0xa6: {
                v[13]="com.alipay.mobile.quinox.SchemeLauncherActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[13]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0xb8:0xae; continue;
            }
            case 0xae: {
                v[1]="使用说明";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0xb8:0xb6; continue;
            }
            case 0xb6: {
                ((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f=dexZ(v[11]);
                pc=0xb8; continue;
            }
            case 0xb8: {
                v[1]="向商家付钱";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0xd5:0xc0; continue;
            }
            case 0xc0: {
                v[1]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                v[13]="交易方式";
                v[14]="支付成功";
                pc=(!dexZero(v[1]))?0xda:0xc8; continue;
            }
            case 0xc8: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[14]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0xda:0xce; continue;
            }
            default: return pc;
        }
    }
    private int recovered_w_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0xce: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[13]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0xd5:0xd4; continue;
            }
            case 0xd4: {
                pc=0xda; continue;
            }
            case 0xd5: {
                v[16]=v[8];
                v[1]=v[11];
                pc=0x2fe; continue;
            }
            case 0xda: {
                v[1]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                v[15]="支付时间";
                v[10]="付款方式";
                v[11]="账单详情";
                pc=(!dexZero(v[1]))?0x120:0xe4; continue;
            }
            case 0xe4: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[14]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0xfa:0xea; continue;
            }
            case 0xea: {
                v[1]="代付成功";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0xfa:0xf2; continue;
            }
            case 0xf2: {
                v[1]="转账成功";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x120:0xfa; continue;
            }
            case 0xfa: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[10]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x10e:0x100; continue;
            }
            case 0x100: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[13]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x10e:0x106; continue;
            }
            case 0x106: {
                v[1]="完成";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x120:0x10e; continue;
            }
            case 0x10e: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[11]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x120:0x114; continue;
            }
            case 0x114: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[15]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x11b:0x11a; continue;
            }
            case 0x11a: {
                pc=0x120; continue;
            }
            case 0x11b: {
                v[16]=v[8];
                v[1]=dexN(1L);
                pc=0x2fe; continue;
            }
            case 0x120: {
                v[1]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                pc=(dexZero(v[1]))?0x15a:0x124; continue;
            }
            case 0x124: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[14]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x15a:0x12a; continue;
            }
            case 0x12a: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[11]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x15a:0x130; continue;
            }
            case 0x130: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[15]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x15a:0x136; continue;
            }
            case 0x136: {
                v[1]="计算中";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x15a:0x13e; continue;
            }
            case 0x13e: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[10]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x14a:0x144; continue;
            }
            case 0x144: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[13]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x15a:0x14a; continue;
            }
            case 0x14a: {
                v[1]="¥";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x11b:0x152; continue;
            }
            case 0x152: {
                v[1]="￥";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x11b:0x15a; continue;
            }
            case 0x15a: {
                v[1]="充值成功";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[13]=result[0];
                v[14]="话费已到账";
                pc=(dexZero(v[13]))?0x16b:0x164; continue;
            }
            case 0x164: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[14]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x16b:0x16a; continue;
            }
            case 0x16a: {
                pc=0x11b; continue;
            }
            case 0x16b: {
                v[13]="结果详情";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                v[16]=v[8];
                pc=(dexZero(v[13]))?0x183:0x175; continue;
            }
            case 0x175: {
                v[13]="提现金额";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(!dexZero(v[13]))?0x17e:0x17d; continue;
            }
            case 0x17d: {
                pc=0x183; continue;
            }
            case 0x17e: {
                v[1]=dexN(1L);
                v[8]=dexN(9L);
                pc=0x2f9; continue;
            }
            case 0x183: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                v[13]="手机充值";
                v[8]="充值号码";
                pc=(dexZero(v[1]))?0x19f:0x18d; continue;
            }
            case 0x18d: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[8]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x19f:0x193; continue;
            }
            default: return pc;
        }
    }
    private int recovered_w_block2(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x193: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[13]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x19f:0x199; continue;
            }
            case 0x199: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[14]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x17e:0x19f; continue;
            }
            case 0x19f: {
                v[1]="提现成功";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x17e:0x1a7; continue;
            }
            case 0x1a7: {
                v[1]="转入成功";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x17e:0x1af; continue;
            }
            case 0x1af: {
                v[1]="转出成功";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x17e:0x1b7; continue;
            }
            case 0x1b7: {
                v[1]="创建时间";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x1da:0x1bf; continue;
            }
            case 0x1bf: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[8]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x1da:0x1c5; continue;
            }
            case 0x1c5: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[13]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x1da:0x1cb; continue;
            }
            case 0x1cb: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[14]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x1da:0x1d1; continue;
            }
            case 0x1d1: {
                v[1]=((com.wangc.bill.auto.action.b)dexR(v[0])).f13680g;
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).b(((java.util.HashMap)dexR(v[12])),((java.util.List)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x1da:0x1d9; continue;
            }
            case 0x1d9: {
                pc=0x17e; continue;
            }
            case 0x1da: {
                v[1]="还款信用卡";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x1f3:0x1e2; continue;
            }
            case 0x1e2: {
                v[1]="银行处理中";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x1f3:0x1ea; continue;
            }
            case 0x1ea: {
                v[1]=dexN(10L);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[1]);
                v[1]=dexN(1L);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[1]);
                pc=0x303; continue;
            }
            case 0x1f3: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[11]))))?1:0);
                v[1]=result[0];
                v[8]=dexN(6L);
                v[13]="商品图片";
                v[14]="交易详情";
                pc=(dexZero(v[1]))?0x20a:0x1fe; continue;
            }
            case 0x1fe: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[14]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x20a:0x204; continue;
            }
            case 0x204: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[13]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x2f3:0x20a; continue;
            }
            case 0x20a: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[11]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x218:0x210; continue;
            }
            case 0x210: {
                v[1]="退款成功";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x2f3:0x218; continue;
            }
            case 0x218: {
                v[1]="余额明细详情";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x2f3:0x220; continue;
            }
            case 0x220: {
                v[1]="交易成功";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x242:0x228; continue;
            }
            case 0x228: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[14]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x242:0x22e; continue;
            }
            case 0x22e: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[13]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x242:0x234; continue;
            }
            case 0x234: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[10]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x242:0x23a; continue;
            }
            case 0x23a: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[15]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x242:0x240; continue;
            }
            case 0x240: {
                pc=0x2f3; continue;
            }
            case 0x242: {
                v[1]="账单管理";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x250:0x24a; continue;
            }
            case 0x24a: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[11]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x25e:0x250; continue;
            }
            case 0x250: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[14]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x25c:0x256; continue;
            }
            case 0x256: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[13]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x25e:0x25c; continue;
            }
            case 0x25c: {
                v[1]=dexN(1L);
                pc=0x2b3; continue;
            }
            case 0x25e: {
                v[1]="收款时间";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x276:0x266; continue;
            }
            default: return pc;
        }
    }
    private int recovered_w_block3(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x266: {
                v[1]="已收款";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x276:0x26e; continue;
            }
            case 0x26e: {
                v[1]=dexN(7L);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[1]);
                v[1]=dexN(1L);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[1]);
                pc=0x303; continue;
            }
            case 0x276: {
                v[1]="红包详情";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x286:0x27e; continue;
            }
            case 0x27e: {
                v[1]="领取成功，已存入余额";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x295:0x286; continue;
            }
            case 0x286: {
                v[1]="查看红包记录";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x295:0x28e; continue;
            }
            case 0x28e: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[8]);
                v[1]=dexN(1L);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[1]);
                pc=0x303; continue;
            }
            case 0x295: {
                v[1]="开始计算收益";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x303:0x29d; continue;
            }
            case 0x29d: {
                v[1]="收益到账";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x303:0x2a5; continue;
            }
            case 0x2a5: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[10]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x303:0x2ab; continue;
            }
            case 0x2ab: {
                v[1]=dexN(9L);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[1]);
                v[1]=dexN(1L);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[1]);
                pc=0x303; continue;
            }
            case 0x2b3: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[8]);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[1]);
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[14]))))?1:0);
                v[8]=result[0];
                pc=(dexZero(v[8]))?0x303:0x2bd; continue;
            }
            case 0x2bd: {
                result[0]=dexN(((java.util.List)dexR(v[3])).indexOf(((java.lang.Object)dexR(v[14]))));
                v[8]=result[0];
                pc=(dexI(v[8])<=0)?0x303:0x2c3; continue;
            }
            case 0x2c3: {
                result[0]=dexN(((java.util.List)dexR(v[3])).size());
                v[10]=result[0];
                v[10]=dexN(dexI(v[10])-dexI(v[1]));
                pc=(dexI(v[8])>=dexI(v[10]))?0x303:0x2ca; continue;
            }
            case 0x2ca: {
                v[8]=dexN(dexI(v[8])+dexI(v[1]));
                result[0]=((java.util.List)dexR(v[3])).get(dexI(v[8]));
                v[1]=result[0];
                v[10]="更多";
                result[0]=dexN((((java.lang.String)dexR(v[10])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x2ef:0x2d7; continue;
            }
            case 0x2d7: {
                v[1]="推荐服务";
                result[0]=((java.util.List)dexR(v[3])).get(dexI(v[8]));
                v[10]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[10]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x2ef:0x2e3; continue;
            }
            case 0x2e3: {
                v[1]="服务推荐";
                result[0]=((java.util.List)dexR(v[3])).get(dexI(v[8]));
                v[8]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[8]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x303:0x2ef; continue;
            }
            case 0x2ef: {
                v[1]=dexN(0L);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[1]);
                pc=0x303; continue;
            }
            case 0x2f3: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[8]);
                v[1]=dexN(1L);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[1]);
                pc=0x303; continue;
            }
            case 0x2f9: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[8]);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[1]);
                pc=0x303; continue;
            }
            case 0x2fe: {
                v[8]=dexN(2L);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[8]);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[1]);
                pc=0x303; continue;
            }
            case 0x303: {
                result[0]=dexN((com.wangc.bill.database.action.s.i())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x37c:0x309; continue;
            }
            case 0x309: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[7]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x315:0x30f; continue;
            }
            case 0x30f: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[6]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x355:0x315; continue;
            }
            case 0x315: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x321:0x31b; continue;
            }
            case 0x31b: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[4]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x355:0x321; continue;
            }
            case 0x321: {
                v[1]="更改付款方式";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x331:0x329; continue;
            }
            case 0x329: {
                v[1]="极速付款";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x355:0x331; continue;
            }
            case 0x331: {
                v[1]="请输入支付密码";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x341:0x339; continue;
            }
            case 0x339: {
                v[1]="使用指纹";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x355:0x341; continue;
            }
            case 0x341: {
                v[1]="扫二维码/条码/小程序码";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x351:0x349; continue;
            }
            case 0x349: {
                v[1]="收付款";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x355:0x351; continue;
            }
            case 0x351: {
                v[1]=dexN((((com.wangc.bill.auto.action.b)dexR(v[0])).f13679f)?1:0);
                pc=(dexZero(v[1]))?0x37c:0x355; continue;
            }
            default: return pc;
        }
    }
    private int recovered_w_block4(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x355: {
                result[0]=dexN(java.lang.System.currentTimeMillis());
                v[10]=result[0];
                ((com.wangc.bill.auto.action.b)dexR(v[0])).f13678e=dexL(v[10]);
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[8]="time:";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[8])));
                v[10]=dexN(((com.wangc.bill.auto.action.b)dexR(v[0])).f13678e);
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(dexL(v[10]));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                v[8]="sssss-time";
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[8])),((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x37c; continue;
            }
            case 0x378: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[1]);
                pc=0x37a; continue;
            }
            case 0x37a: {
                v[3]=dexN(0L);
                v[12]=v[3];
                pc=0x37c; continue;
            }
            case 0x37c: {
                v[1]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).b)?1:0);
                pc=(dexZero(v[1]))?0x426:0x380; continue;
            }
            case 0x380: {
                pc=(dexZero(v[2]))?0x426:0x382; continue;
            }
            case 0x382: {
                pc=(!dexZero(v[3]))?0x3a3:0x384; continue;
            }
            case 0x384: {
                result[0]=((com.wangc.bill.auto.action.c)dexR(v[0])).g(((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])));
                v[3]=result[0];
                v[12]=new DexReference(); // new java.util.HashMap
                ((DexReference)v[12]).value=new java.util.HashMap();
                result[0]=((java.util.List)dexR(v[3])).iterator();
                v[1]=result[0];
                pc=0x391; continue;
            }
            case 0x391: {
                result[0]=dexN((((java.util.Iterator)dexR(v[1])).hasNext())?1:0);
                v[2]=result[0];
                pc=(dexZero(v[2]))?0x3a3:0x397; continue;
            }
            case 0x397: {
                result[0]=((java.util.Iterator)dexR(v[1])).next();
                v[2]=result[0];
                v[2]=((java.lang.String)dexR(v[2]));
                v[8]=java.lang.Boolean.TRUE;
                result[0]=((java.util.HashMap)dexR(v[12])).put(((java.lang.Object)dexR(v[2])),((java.lang.Object)dexR(v[8])));
                pc=0x391; continue;
            }
            case 0x3a3: {
                result[0]=dexN((com.wangc.bill.database.action.s.i())?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x421:0x3a9; continue;
            }
            case 0x3a9: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[7]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x3b5:0x3af; continue;
            }
            case 0x3af: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[6]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x3ff:0x3b5; continue;
            }
            case 0x3b5: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[5]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x3c1:0x3bb; continue;
            }
            case 0x3bb: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[4]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x3ff:0x3c1; continue;
            }
            case 0x3c1: {
                v[1]="更改付款方式";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x3d1:0x3c9; continue;
            }
            case 0x3c9: {
                v[1]="极速付款";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x3ff:0x3d1; continue;
            }
            case 0x3d1: {
                v[1]="请输入支付密码";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x3e1:0x3d9; continue;
            }
            case 0x3d9: {
                v[1]="使用指纹";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x3ff:0x3e1; continue;
            }
            case 0x3e1: {
                v[1]="扫二维码/条码/小程序码";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x3f1:0x3e9; continue;
            }
            case 0x3e9: {
                v[1]="收付款";
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x3ff:0x3f1; continue;
            }
            case 0x3f1: {
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[9]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x421:0x3f7; continue;
            }
            case 0x3f7: {
                v[1]=v[16];
                result[0]=dexN((((java.util.HashMap)dexR(v[12])).containsKey(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x421:0x3ff; continue;
            }
            case 0x3ff: {
                result[0]=dexN(java.lang.System.currentTimeMillis());
                v[1]=result[0];
                ((com.wangc.bill.auto.action.b)dexR(v[0])).f13678e=dexL(v[1]);
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[2]="time:";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[2])));
                v[4]=dexN(((com.wangc.bill.auto.action.b)dexR(v[0])).f13678e);
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(dexL(v[4]));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                v[2]="sssss-time";
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[2])),((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x421; continue;
            }
            case 0x421: {
                v[1]=v[18];
                ((com.wangc.bill.auto.action.b)dexR(v[0])).y(((android.content.Context)dexR(v[1])),((java.util.List)dexR(v[3])),((java.util.HashMap)dexR(v[12])));
                pc=0x426; continue;
            }
            case 0x426: {
                return -1;
            }
            default: return pc;
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
