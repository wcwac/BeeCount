// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 3f8290ad250d3c17b53e594bd3946cd582ad17ee985df88c860d2ed71fc57649
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
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class n extends c implements x1.a {

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    private static n f13753k = null;

    /* JADX INFO: renamed from: l, reason: collision with root package name */
    private static final int f13754l = 4;

    /* JADX INFO: renamed from: m, reason: collision with root package name */
    private static final int f13755m = 5;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private String f13756e;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13757f;

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private String f13758g;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private String f13759h;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private boolean f13760i = false;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private boolean f13761j = false;

    private void q(Context context, List<String> list) {
        BillInfo billInfoU;
        a2.a("start find node");
        if (list == null || list.isEmpty()) {
            return;
        }
        if (this.a == 4) {
            if (a(list, "抖音月付", false)) {
                this.f13756e = "抖音月付";
            }
            a2.a("start check pay:" + this.a);
            billInfoU = r(this.f13756e, this.f13757f, this.f13758g);
        } else {
            billInfoU = (list.contains("抖音月付还款") || list.contains("还款成功")) ? u(list) : t(context, list);
        }
        if (billInfoU != null) {
            if (this.a != 5) {
                this.f13760i = false;
            }
            this.b = false;
            this.f13756e = null;
            this.f13757f = null;
            this.f13759h = null;
            this.f13758g = null;
            billInfoU.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
            SelectToSpeakService.l(context, billInfoU, this);
        }
    }

    public static n v() {
        if (f13753k == null) {
            f13753k = new n();
        }
        return f13753k;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
    }

    public BillInfo r(String str, String str2, String str3) {
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("抖音");
        billInfo.setRemark("抖音");
        billInfo.setShopName("抖音");
        if (TextUtils.isEmpty(str)) {
            billInfo.setAsset("抖音支付");
        } else {
            billInfo.setAsset(str);
        }
        if (!TextUtils.isEmpty(str2)) {
            billInfo.setRemark(str2);
        }
        if (!TextUtils.isEmpty(str3) && h2.J(str3)) {
            billInfo.setNumber(str3);
        }
        if (!TextUtils.isEmpty(this.f13759h) && h2.J(this.f13759h)) {
            billInfo.setDiscount(this.f13759h);
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    /** DEX-exact branch transcription of s; original register and block addresses retained. */
    public void s(android.content.Context arg0, java.lang.String arg1, android.view.accessibility.AccessibilityNodeInfo arg2) {
        Object[] v=new Object[24]; Object[] result=new Object[2]; int pc=0;
        v[20]=this;
        v[21]=arg0;
        v[22]=arg1;
        v[23]=arg2;
        while(true) {
            if(pc==-1) { return; }
            if(pc<0xe9) pc=recovered_s_block0(v,result,pc);
            else if(pc<0x204) pc=recovered_s_block1(v,result,pc);
            else if(pc<0x320) pc=recovered_s_block2(v,result,pc);
            else if(pc<0x469) pc=recovered_s_block3(v,result,pc);
            else pc=recovered_s_block4(v,result,pc);
        }
    }
    private int recovered_s_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                v[0]=v[20];
                v[1]=v[22];
                v[2]=v[23];
                v[3]="com.ss.android.ugc.aweme.main.MainActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                v[4]=dexN(0L);
                v[5]=dexN(0L);
                pc=(!dexZero(v[3]))?0x48:0x10; continue;
            }
            case 0x10: {
                v[3]="com.ss.android.ugc.aweme.newdetail.DitoPoiDetailActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x48:0x18; continue;
            }
            case 0x18: {
                v[3]="com.ss.android.ugc.aweme.search.activity.SearchResultActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x48:0x20; continue;
            }
            case 0x20: {
                v[3]="com.ss.android.ugc.aweme.detail.ui.LiveDetailActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x48:0x28; continue;
            }
            case 0x28: {
                v[3]="com.ss.android.ugc.aweme.detail.ui.DetailActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x48:0x30; continue;
            }
            case 0x30: {
                v[3]="com.ss.android.ugc.aweme.live.LivePlayActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x48:0x38; continue;
            }
            case 0x38: {
                v[3]="com.ss.android.ugc.aweme.commerce.sdk.MallContainerActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0x48:0x40; continue;
            }
            case 0x40: {
                v[3]="com.bytedance.android.shopping.anchorv4.containers.AnchorV4Activity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x56:0x48; continue;
            }
            case 0x48: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13760i=dexZ(v[5]);
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13757f=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13756e=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13759h=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[5]);
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13761j=dexZ(v[5]);
                pc=0x56; continue;
            }
            case 0x56: {
                v[3]="com.ss.android.ugc.aweme.bullet.ui.BulletContainerActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                v[6]="提交订单";
                v[7]=dexN(1L);
                pc=(!dexZero(v[3]))?0xcf:0x61; continue;
            }
            case 0x61: {
                v[3]="com.android.ttcjpaysdk.base.ui.dialog.CJPayCommonDialog";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xcf:0x69; continue;
            }
            case 0x69: {
                v[3]="com.android.ttcjpaysdk.thirdparty.front.counter.dypay.activity.DyPayETActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xcf:0x71; continue;
            }
            case 0x71: {
                v[3]="com.bytedance.android.ec.common.impl.view.FixedDialog";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xcf:0x79; continue;
            }
            case 0x79: {
                v[3]="com.bytedance.android.shopping.buynow.container.BuyNowFullscreenActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xcf:0x81; continue;
            }
            case 0x81: {
                v[3]="com.bytedance.ies.xelement.overlay.LynxOverlayDialog";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xcf:0x89; continue;
            }
            case 0x89: {
                v[3]="com.ss.android.ugc.aweme.live.LiveDummyActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xcf:0x91; continue;
            }
            case 0x91: {
                v[3]="com.bytedance.android.shopping.buynow.container.BuyNowFullscreenDialog";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xcf:0x99; continue;
            }
            case 0x99: {
                v[3]="com.android.ttcjpaysdk.thirdparty.front.counter.dypay.activity.DyPayStandardActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xcf:0xa1; continue;
            }
            case 0xa1: {
                v[3]="com.android.ttcjpaysdk.bdpay.security.loading.view.CJPaySecurityLoadingActivity";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xcf:0xa9; continue;
            }
            case 0xa9: {
                v[3]="X.";
                result[0]=dexN((((java.lang.String)dexR(v[1])).startsWith(((java.lang.String)dexR(v[3]))))?1:0);
                v[3]=result[0];
                pc=(!dexZero(v[3]))?0xcf:0xb1; continue;
            }
            case 0xb1: {
                v[3]="com.lynx.tasm.behavior.KeyboardMonitor";
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[8]=result[0];
                pc=(dexZero(v[8]))?0xc6:0xb9; continue;
            }
            case 0xb9: {
                pc=(dexZero(v[2]))?0xc6:0xbb; continue;
            }
            case 0xbb: {
                result[0]=((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])).findAccessibilityNodeInfosByText(((java.lang.String)dexR(v[6])));
                v[8]=result[0];
                result[0]=dexN((((com.wangc.bill.auto.action.n)dexR(v[0])).w(((java.util.List)dexR(v[8]))))?1:0);
                v[8]=result[0];
                pc=(dexZero(v[8]))?0xc6:0xc5; continue;
            }
            case 0xc5: {
                pc=0xcf; continue;
            }
            case 0xc6: {
                result[0]=dexN((((java.lang.String)dexR(v[1])).equals(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0xd1:0xcc; continue;
            }
            case 0xcc: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13761j=dexZ(v[7]);
                pc=0xd1; continue;
            }
            case 0xcf: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13760i=dexZ(v[7]);
                pc=0xd1; continue;
            }
            case 0xd1: {
                v[1]=dexN((((com.wangc.bill.auto.action.n)dexR(v[0])).f13761j)?1:0);
                pc=(dexZero(v[1]))?0xe5:0xd5; continue;
            }
            case 0xd5: {
                pc=(dexZero(v[2]))?0xe5:0xd7; continue;
            }
            case 0xd7: {
                result[0]=((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])).findAccessibilityNodeInfosByText(((java.lang.String)dexR(v[6])));
                v[1]=result[0];
                result[0]=dexN((((com.wangc.bill.auto.action.n)dexR(v[0])).w(((java.util.List)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0xe5:0xe1; continue;
            }
            case 0xe1: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13761j=dexZ(v[5]);
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13760i=dexZ(v[7]);
                pc=0xe5; continue;
            }
            case 0xe5: {
                v[1]=dexN((((com.wangc.bill.auto.action.n)dexR(v[0])).f13760i)?1:0);
                pc=(dexZero(v[1]))?0x5c2:0xe9; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0xe9: {
                v[1]=dexN(4L);
                v[3]="支付成功";
                v[8]=",";
                v[9]=dexN(2L);
                v[10]="¥";
                v[11]="";
                pc=(dexZero(v[2]))?0x4bd:0xf5; continue;
            }
            case 0xf5: {
                result[0]=((com.wangc.bill.auto.action.c)dexR(v[0])).l(((android.view.accessibility.AccessibilityNodeInfo)dexR(v[2])));
                v[12]=result[0];
                v[13]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).c);
                v[14]=dexN(199L);
                pc=(dexI(v[13])<dexI(v[14]))?0x11a:0xff; continue;
            }
            case 0xff: {
                v[13]="扫一扫";
                result[0]=dexN((((java.util.List)dexR(v[12])).contains(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x11a:0x107; continue;
            }
            case 0x107: {
                v[13]="通知消息";
                result[0]=dexN((((java.util.List)dexR(v[12])).contains(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x11a:0x10f; continue;
            }
            case 0x10f: {
                v[13]="常用功能";
                result[0]=dexN((((java.util.List)dexR(v[12])).contains(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x11a:0x117; continue;
            }
            case 0x117: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13760i=dexZ(v[5]);
                return -1;
            }
            case 0x11a: {
                v[13]="账单详情";
                result[0]=dexN((((java.util.List)dexR(v[12])).contains(((java.lang.Object)dexR(v[13]))))?1:0);
                v[13]=result[0];
                v[14]=dexN(5L);
                v[15]=" ";
                v[4]=dexN(3L);
                pc=(dexZero(v[13]))?0x174:0x126; continue;
            }
            case 0x126: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[7]);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[14]);
                result[0]=dexN(((java.util.List)dexR(v[12])).size());
                v[6]=result[0];
                pc=(!dexEqual(v[6],v[1]))?0x14f:0x130; continue;
            }
            case 0x130: {
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[4]));
                v[6]=result[0];
                v[6]=((java.lang.CharSequence)dexR(v[6]));
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x14f:0x13c; continue;
            }
            case 0x13c: {
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                result[0]=((java.lang.String)dexR(v[4])).split(((java.lang.String)dexR(v[15])));
                v[4]=result[0];
                result[0]=java.util.Arrays.asList(((java.lang.Object[])dexR(v[4])));
                v[4]=result[0];
                result[0]=dexN((((java.util.List)dexR(v[12])).addAll(((java.util.Collection)dexR(v[4]))))?1:0);
                pc=0x4bc; continue;
            }
            case 0x14f: {
                result[0]=dexN(((java.util.List)dexR(v[12])).size());
                v[6]=result[0];
                pc=(!dexEqual(v[6],v[4]))?0x4bc:0x155; continue;
            }
            case 0x155: {
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[9]));
                v[4]=result[0];
                v[4]=((java.lang.CharSequence)dexR(v[4]));
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x4bc:0x161; continue;
            }
            case 0x161: {
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[9]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                result[0]=((java.lang.String)dexR(v[4])).split(((java.lang.String)dexR(v[15])));
                v[4]=result[0];
                result[0]=java.util.Arrays.asList(((java.lang.Object[])dexR(v[4])));
                v[4]=result[0];
                result[0]=dexN((((java.util.List)dexR(v[12])).addAll(((java.util.Collection)dexR(v[4]))))?1:0);
                pc=0x4bc; continue;
            }
            case 0x174: {
                result[0]=dexN(((java.util.List)dexR(v[12])).size());
                v[13]=result[0];
                v[13]=dexN(dexI(v[13])-dexI(v[7]));
                v[16]=dexN(0L);
                pc=0x17b; continue;
            }
            case 0x17b: {
                pc=(dexI(v[13])<0)?0x47a:0x17d; continue;
            }
            case 0x17d: {
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[13]));
                v[17]=result[0];
                v[14]=v[17];
                v[14]=((java.lang.String)dexR(v[14]));
                v[1]="【";
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x197:0x18d; continue;
            }
            case 0x18d: {
                v[1]="】";
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x197:0x195; continue;
            }
            case 0x195: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13757f=((java.lang.String)dexR(v[14]));
                pc=0x197; continue;
            }
            case 0x197: {
                v[1]="团购头图";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[18]=result[0];
                pc=(dexZero(v[18]))?0x1b7:0x19f; continue;
            }
            case 0x19f: {
                result[0]=dexN(((java.util.List)dexR(v[12])).size());
                v[18]=result[0];
                v[4]=dexN(dexI(v[18])+(-1));
                pc=(dexI(v[13])>=dexI(v[4]))?0x1b7:0x1a7; continue;
            }
            case 0x1a7: {
                v[4]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x1b7:0x1b5; continue;
            }
            case 0x1b5: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13757f=((java.lang.String)dexR(v[4]));
                pc=0x1b7; continue;
            }
            case 0x1b7: {
                v[1]="已选择";
                result[0]=dexN((((java.lang.String)dexR(v[14])).startsWith(((java.lang.String)dexR(v[1]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1c5:0x1bf; continue;
            }
            case 0x1bf: {
                result[0]=((java.lang.String)dexR(v[14])).replace(((java.lang.CharSequence)dexR(v[1])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13757f=((java.lang.String)dexR(v[1]));
                pc=0x1c5; continue;
            }
            case 0x1c5: {
                v[1]="实付";
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[4]=result[0];
                v[7]="元";
                pc=(dexZero(v[4]))?0x1d5:0x1cf; continue;
            }
            case 0x1cf: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[7]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x1e1:0x1d5; continue;
            }
            case 0x1d5: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1fc:0x1db; continue;
            }
            case 0x1db: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[10]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1fc:0x1e1; continue;
            }
            case 0x1e1: {
                result[0]=((java.lang.String)dexR(v[14])).replace(((java.lang.CharSequence)dexR(v[1])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[7])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1f9:0x1f7; continue;
            }
            case 0x1f7: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g=((java.lang.String)dexR(v[1]));
                pc=0x1f9; continue;
            }
            case 0x1f9: {
                v[4]=dexN(3L);
                pc=0x2d6; continue;
            }
            case 0x1fc: {
                v[1]="提交订单,按钮";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x248:0x204; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block2(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x204: {
                pc=(dexI(v[13])<=dexI(v[9]))?0x248:0x206; continue;
            }
            case 0x206: {
                v[1]=dexN(dexI(v[13])+(-1));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[10]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x21c:0x214; continue;
            }
            case 0x214: {
                v[1]=dexN(dexI(v[13])+(-2));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                pc=0x21c; continue;
            }
            case 0x21c: {
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[10]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1f9:0x222; continue;
            }
            case 0x222: {
                result[0]=((java.lang.String)dexR(v[1])).split(((java.lang.String)dexR(v[10])));
                v[1]=result[0];
                v[4]=dexN(dexLength(dexR(v[1])));
                pc=(dexI(v[4])<=0)?0x1f9:0x229; continue;
            }
            case 0x229: {
                v[4]=((Object[])dexR(v[1]))[dexI(v[5])];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[19]=result[0];
                pc=(dexZero(v[19]))?0x237:0x231; continue;
            }
            case 0x231: {
                v[5]=dexN(dexLength(dexR(v[1])));
                v[9]=dexN(1L);
                pc=(dexI(v[5])<=dexI(v[9]))?0x237:0x235; continue;
            }
            case 0x235: {
                v[4]=((Object[])dexR(v[1]))[dexI(v[9])];
                pc=0x237; continue;
            }
            case 0x237: {
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1f9:0x245; continue;
            }
            case 0x245: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g=((java.lang.String)dexR(v[1]));
                pc=0x1f9; continue;
            }
            case 0x248: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x281:0x24e; continue;
            }
            case 0x24e: {
                v[1]=dexN(2L);
                pc=(dexI(v[13])<=dexI(v[1]))?0x281:0x251; continue;
            }
            case 0x251: {
                v[1]=dexN(dexI(v[13])+(-1));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x277:0x267; continue;
            }
            case 0x267: {
                v[1]=dexN(dexI(v[13])+(-2));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                pc=0x277; continue;
            }
            case 0x277: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x1f9:0x27d; continue;
            }
            case 0x27d: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g=((java.lang.String)dexR(v[1]));
                pc=0x1f9; continue;
            }
            case 0x281: {
                v[1]="实付价";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x1f9:0x289; continue;
            }
            case 0x289: {
                v[1]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[1]));
                v[1]=result[0];
                result[0]=dexN((((java.lang.String)dexR(v[10])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x1f9:0x295; continue;
            }
            case 0x295: {
                result[0]=dexN(((java.util.List)dexR(v[12])).size());
                v[1]=result[0];
                v[4]=dexN(3L);
                v[1]=dexN(dexI(v[1])-dexI(v[4]));
                pc=(dexI(v[13])>=dexI(v[1]))?0x2d6:0x29d; continue;
            }
            case 0x29d: {
                v[1]=dexN(dexI(v[13])+(2));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                v[5]=dexN(dexI(v[13])+(3));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[5]));
                v[5]=result[0];
                v[5]=((java.lang.String)dexR(v[5]));
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[9]=result[0];
                pc=(!dexZero(v[9]))?0x2ce:0x2b7; continue;
            }
            case 0x2b7: {
                v[9]=".";
                result[0]=dexN((((java.lang.String)dexR(v[5])).startsWith(((java.lang.String)dexR(v[9]))))?1:0);
                v[9]=result[0];
                pc=(dexZero(v[9]))?0x2ce:0x2bf; continue;
            }
            case 0x2bf: {
                v[9]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[9]).value=new java.lang.StringBuilder();
                result[0]=((java.lang.StringBuilder)dexR(v[9])).append(((java.lang.String)dexR(v[1])));
                result[0]=((java.lang.StringBuilder)dexR(v[9])).append(((java.lang.String)dexR(v[5])));
                result[0]=((java.lang.StringBuilder)dexR(v[9])).toString();
                v[1]=result[0];
                pc=0x2ce; continue;
            }
            case 0x2ce: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x2d6:0x2d4; continue;
            }
            case 0x2d4: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g=((java.lang.String)dexR(v[1]));
                pc=0x2d6; continue;
            }
            case 0x2d6: {
                result[0]=dexN((((java.util.List)dexR(v[12])).contains(((java.lang.Object)dexR(v[3]))))?1:0);
                v[1]=result[0];
                v[5]=" ";
                pc=(dexZero(v[1]))?0x2f8:0x2de; continue;
            }
            case 0x2de: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[10]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x2f8:0x2e4; continue;
            }
            case 0x2e4: {
                result[0]=((java.lang.String)dexR(v[14])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[9]=result[0];
                pc=(dexZero(v[9]))?0x2f8:0x2f6; continue;
            }
            case 0x2f6: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g=((java.lang.String)dexR(v[1]));
                pc=0x2f8; continue;
            }
            case 0x2f8: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x320:0x2fe; continue;
            }
            case 0x2fe: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[10]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x320:0x304; continue;
            }
            case 0x304: {
                result[0]=((java.lang.String)dexR(v[14])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[9]=result[0];
                pc=(dexZero(v[9]))?0x320:0x31e; continue;
            }
            case 0x31e: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g=((java.lang.String)dexR(v[1]));
                pc=0x320; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block3(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x320: {
                v[1]="支付方式";
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[9]=result[0];
                v[4]="(";
                pc=(dexZero(v[9]))?0x35e:0x32a; continue;
            }
            case 0x32a: {
                result[0]=dexN(((java.util.List)dexR(v[12])).size());
                v[9]=result[0];
                v[18]=dexN(1L);
                v[9]=dexN(dexI(v[9])+(-1));
                pc=(dexI(v[13])>=dexI(v[9]))?0x35e:0x334; continue;
            }
            case 0x334: {
                v[9]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[9]));
                v[9]=result[0];
                v[9]=((java.lang.String)dexR(v[9]));
                result[0]=dexN((((java.lang.String)dexR(v[9])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x35e:0x342; continue;
            }
            case 0x342: {
                v[1]="IB";
                result[0]=((java.lang.String)dexR(v[9])).replace(((java.lang.CharSequence)dexR(v[1])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                v[9]=")";
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                v[9]="​";
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13756e=((java.lang.String)dexR(v[1]));
                pc=0x35e; continue;
            }
            case 0x35e: {
                v[1]="付款成功";
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x3a4:0x366; continue;
            }
            case 0x366: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[10]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x3a4:0x36c; continue;
            }
            case 0x36c: {
                result[0]=((java.lang.String)dexR(v[14])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                v[9]="付款成功";
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[15])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x38a:0x388; continue;
            }
            case 0x388: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g=((java.lang.String)dexR(v[1]));
                pc=0x38a; continue;
            }
            case 0x38a: {
                v[1]=((com.wangc.bill.auto.action.n)dexR(v[0])).f13756e;
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x3a4:0x392; continue;
            }
            case 0x392: {
                result[0]=dexN(((java.util.List)dexR(v[12])).size());
                v[1]=result[0];
                v[5]=dexN(1L);
                v[1]=dexN(dexI(v[1])-dexI(v[5]));
                pc=(dexI(v[13])>=dexI(v[1]))?0x3a4:0x39a; continue;
            }
            case 0x39a: {
                v[1]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13756e=((java.lang.String)dexR(v[1]));
                pc=0x3a4; continue;
            }
            case 0x3a4: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).equals(((java.lang.Object)dexR(v[10]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x3ec:0x3aa; continue;
            }
            case 0x3aa: {
                result[0]=dexN(((java.util.List)dexR(v[12])).size());
                v[1]=result[0];
                v[5]=dexN(4L);
                v[1]=dexN(dexI(v[1])-dexI(v[5]));
                pc=(dexEqual(v[13],v[1]))?0x3bb:0x3b2; continue;
            }
            case 0x3b2: {
                result[0]=dexN(((java.util.List)dexR(v[12])).size());
                v[1]=result[0];
                v[5]=dexN(5L);
                v[1]=dexN(dexI(v[1])-dexI(v[5]));
                pc=(!dexEqual(v[13],v[1]))?0x3ed:0x3ba; continue;
            }
            case 0x3ba: {
                pc=0x3bc; continue;
            }
            case 0x3bb: {
                v[5]=dexN(5L);
                pc=0x3bc; continue;
            }
            case 0x3bc: {
                result[0]=dexN(((java.util.List)dexR(v[12])).size());
                v[1]=result[0];
                v[9]=dexN(2L);
                v[1]=dexN(dexI(v[1])-dexI(v[9]));
                pc=(dexI(v[13])>=dexI(v[1]))?0x3ed:0x3c4; continue;
            }
            case 0x3c4: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[9]=dexN(dexI(v[13])+(1));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[9]));
                v[9]=result[0];
                v[9]=((java.lang.String)dexR(v[9]));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[9])));
                v[9]=dexN(dexI(v[13])+(2));
                result[0]=((java.util.List)dexR(v[12])).get(dexI(v[9]));
                v[9]=result[0];
                v[9]=((java.lang.String)dexR(v[9]));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[9])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[9]=result[0];
                pc=(dexZero(v[9]))?0x3ed:0x3e9; continue;
            }
            case 0x3e9: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g=((java.lang.String)dexR(v[1]));
                pc=0x3ed; continue;
            }
            case 0x3ec: {
                v[5]=dexN(5L);
                pc=0x3ed; continue;
            }
            case 0x3ed: {
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[16]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x403:0x3f3; continue;
            }
            case 0x3f3: {
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x403:0x3f9; continue;
            }
            case 0x3f9: {
                v[1]=")";
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x403:0x401; continue;
            }
            case 0x401: {
                v[1]=v[14];
                pc=0x405; continue;
            }
            case 0x403: {
                v[1]=v[16];
                pc=0x405; continue;
            }
            case 0x405: {
                v[4]=((com.wangc.bill.auto.action.n)dexR(v[0])).f13756e;
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x42d:0x40d; continue;
            }
            case 0x40d: {
                v[4]="，已选中";
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x42d:0x415; continue;
            }
            case 0x415: {
                v[4]="，已选中";
                result[0]=((java.lang.String)dexR(v[14])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[11])));
                v[4]=result[0];
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13756e=((java.lang.String)dexR(v[4]));
                v[4]="抖音支付";
                result[0]=dexN((((java.lang.String)dexR(v[14])).contains(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x42d:0x425; continue;
            }
            case 0x425: {
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[1]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x42d:0x42b; continue;
            }
            case 0x42b: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13756e=((java.lang.String)dexR(v[1]));
                pc=0x42d; continue;
            }
            case 0x42d: {
                v[4]="已优惠";
                result[0]=dexN((((java.lang.String)dexR(v[14])).startsWith(((java.lang.String)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x46d:0x435; continue;
            }
            case 0x435: {
                v[4]="已优惠";
                result[0]=((java.lang.String)dexR(v[14])).replace(((java.lang.CharSequence)dexR(v[4])),((java.lang.CharSequence)dexR(v[11])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[7])),((java.lang.CharSequence)dexR(v[11])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[4]=result[0];
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13759h=((java.lang.String)dexR(v[4]));
                v[4]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[4]).value=new java.lang.StringBuilder();
                v[7]="discountNum:";
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[7])));
                v[7]=((com.wangc.bill.auto.action.n)dexR(v[0])).f13759h;
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[7])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).toString();
                v[4]=result[0];
                v[7]="sssss";
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[7])),((java.lang.Object)dexR(v[4]))};
                v[4]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[4])));
                v[4]=((com.wangc.bill.auto.action.n)dexR(v[0])).f13759h;
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x46d:0x469; continue;
            }
            default: return pc;
        }
    }
    private int recovered_s_block4(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x469: {
                v[4]=dexN(0L);
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13759h=((java.lang.String)dexR(v[4]));
                pc=0x46e; continue;
            }
            case 0x46d: {
                v[4]=dexN(0L);
                pc=0x46e; continue;
            }
            case 0x46e: {
                v[13]=dexN(dexI(v[13])+(-1));
                v[16]=v[1];
                v[14]=v[5];
                v[1]=dexN(4L);
                v[4]=dexN(3L);
                v[5]=dexN(0L);
                v[7]=dexN(1L);
                v[9]=dexN(2L);
                pc=0x17b; continue;
            }
            case 0x47a: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[4]="asset:";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[4])));
                v[4]=((com.wangc.bill.auto.action.n)dexR(v[0])).f13756e;
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[4])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                v[4]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[4]).value=new java.lang.StringBuilder();
                v[5]="remark:";
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[5])));
                v[5]=((com.wangc.bill.auto.action.n)dexR(v[0])).f13757f;
                result[0]=((java.lang.StringBuilder)dexR(v[4])).append(((java.lang.String)dexR(v[5])));
                result[0]=((java.lang.StringBuilder)dexR(v[4])).toString();
                v[4]=result[0];
                v[5]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[5]).value=new java.lang.StringBuilder();
                v[6]="num:";
                result[0]=((java.lang.StringBuilder)dexR(v[5])).append(((java.lang.String)dexR(v[6])));
                v[6]=((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g;
                result[0]=((java.lang.StringBuilder)dexR(v[5])).append(((java.lang.String)dexR(v[6])));
                result[0]=((java.lang.StringBuilder)dexR(v[5])).toString();
                v[5]=result[0];
                v[6]="sssss";
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[6])),((java.lang.Object)dexR(v[1])),((java.lang.Object)dexR(v[4])),((java.lang.Object)dexR(v[5]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x4bc; continue;
            }
            case 0x4bc: {
                v[4]=v[12];
                pc=0x4bd; continue;
            }
            case 0x4bd: {
                v[1]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).b)?1:0);
                pc=(!dexZero(v[1]))?0x5c2:0x4c1; continue;
            }
            case 0x4c1: {
                pc=(dexZero(v[2]))?0x5c2:0x4c3; continue;
            }
            case 0x4c3: {
                v[1]="购买成功";
                v[5]=dexN(1L);
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[4])),((java.lang.String)dexR(v[1])),dexZ(v[5])))?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0x4d4:0x4cc; continue;
            }
            case 0x4cc: {
                v[1]=dexN(0L);
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[4])),((java.lang.String)dexR(v[3])),dexZ(v[1])))?1:0);
                v[6]=result[0];
                pc=(dexZero(v[6]))?0x5c2:0x4d3; continue;
            }
            case 0x4d3: {
                pc=0x4d5; continue;
            }
            case 0x4d4: {
                v[1]=dexN(0L);
                pc=0x4d5; continue;
            }
            case 0x4d5: {
                ((com.wangc.bill.auto.action.c)dexR(v[0])).b=dexZ(v[5]);
                v[5]=dexN(4L);
                ((com.wangc.bill.auto.action.c)dexR(v[0])).a=dexI(v[5]);
                v[5]="抖音支付 (";
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[4])),((java.lang.String)dexR(v[5])),dexZ(v[1])))?1:0);
                v[6]=result[0];
                pc=(dexZero(v[6]))?0x51a:0x4e2; continue;
            }
            case 0x4e2: {
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).d(((java.util.List)dexR(v[4])),((java.lang.String)dexR(v[5])),dexZ(v[1])));
                v[1]=result[0];
                pc=(dexI(v[1])<0)?0x51a:0x4e8; continue;
            }
            case 0x4e8: {
                result[0]=dexN(((java.util.List)dexR(v[4])).size());
                v[6]=result[0];
                pc=(dexI(v[1])>=dexI(v[6]))?0x51a:0x4ee; continue;
            }
            case 0x4ee: {
                result[0]=((java.util.List)dexR(v[4])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                v[5]=") 付款";
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[5])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13756e=((java.lang.String)dexR(v[1]));
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[5]="check asset : ";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[5])));
                v[5]=((com.wangc.bill.auto.action.n)dexR(v[0])).f13756e;
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[5])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x51a; continue;
            }
            case 0x51a: {
                v[1]=dexN(1L);
                result[0]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).a(((java.util.List)dexR(v[4])),((java.lang.String)dexR(v[3])),dexZ(v[1])))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x5a8:0x521; continue;
            }
            case 0x521: {
                result[0]=dexN(((com.wangc.bill.auto.action.c)dexR(v[0])).d(((java.util.List)dexR(v[4])),((java.lang.String)dexR(v[3])),dexZ(v[1])));
                v[3]=result[0];
                pc=(dexI(v[3])<0)?0x5a8:0x527; continue;
            }
            case 0x527: {
                result[0]=dexN(((java.util.List)dexR(v[4])).size());
                v[5]=result[0];
                v[5]=dexN(dexI(v[5])-dexI(v[1]));
                pc=(dexI(v[3])>=dexI(v[5]))?0x5a8:0x52e; continue;
            }
            case 0x52e: {
                v[1]=dexN(dexI(v[3])+(1));
                result[0]=((java.util.List)dexR(v[4])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=dexN((((java.lang.String)dexR(v[1])).contains(((java.lang.CharSequence)dexR(v[10]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x5a8:0x53c; continue;
            }
            case 0x53c: {
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[11])));
                v[5]=result[0];
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[5]=result[0];
                v[6]="确认收货后抖音月付付款";
                result[0]=((java.lang.String)dexR(v[5])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[11])));
                v[5]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[5]))))?1:0);
                v[6]=result[0];
                pc=(dexZero(v[6]))?0x56d:0x550; continue;
            }
            case 0x550: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g=((java.lang.String)dexR(v[5]));
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="check num : ";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                v[3]=((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g;
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x5a8; continue;
            }
            case 0x56d: {
                result[0]=dexN((((java.lang.String)dexR(v[10])).equals(((java.lang.Object)dexR(v[1]))))?1:0);
                v[1]=result[0];
                pc=(dexZero(v[1]))?0x5a8:0x573; continue;
            }
            case 0x573: {
                result[0]=dexN(((java.util.List)dexR(v[4])).size());
                v[1]=result[0];
                v[5]=dexN(2L);
                v[1]=dexN(dexI(v[1])-dexI(v[5]));
                pc=(dexI(v[3])>=dexI(v[1]))?0x5a8:0x57b; continue;
            }
            case 0x57b: {
                v[3]=dexN(dexI(v[3])+dexI(v[5]));
                result[0]=((java.util.List)dexR(v[4])).get(dexI(v[3]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[8])),((java.lang.CharSequence)dexR(v[11])));
                v[1]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[1]))))?1:0);
                v[3]=result[0];
                pc=(dexZero(v[3]))?0x5a8:0x58c; continue;
            }
            case 0x58c: {
                ((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g=((java.lang.String)dexR(v[1]));
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="check num : ";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                v[3]=((com.wangc.bill.auto.action.n)dexR(v[0])).f13758g;
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x5a8; continue;
            }
            case 0x5a8: {
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[3]="nodeInfo tiktok can add : ";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[3])));
                v[3]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).b)?1:0);
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(dexZ(v[3]));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0x5c2; continue;
            }
            case 0x5c2: {
                v[1]=dexN((((com.wangc.bill.auto.action.c)dexR(v[0])).b)?1:0);
                pc=(dexZero(v[1]))?0x5cf:0x5c6; continue;
            }
            case 0x5c6: {
                pc=(dexZero(v[2]))?0x5cf:0x5c8; continue;
            }
            case 0x5c8: {
                pc=(dexZero(v[4]))?0x5cf:0x5ca; continue;
            }
            case 0x5ca: {
                v[1]=v[21];
                ((com.wangc.bill.auto.action.n)dexR(v[0])).q(((android.content.Context)dexR(v[1])),((java.util.List)dexR(v[4])));
                pc=0x5cf; continue;
            }
            case 0x5cf: {
                return -1;
            }
            default: return pc;
        }
    }

    /** DEX-exact branch transcription of t; original register and block addresses retained. */
    public com.wangc.bill.http.entity.BillInfo t(android.content.Context arg0, java.util.List arg1) {
        Object[] v=new Object[18]; Object[] result=new Object[2]; int pc=0;
        v[15]=this;
        v[16]=arg0;
        v[17]=arg1;
        while(true) {
            if(pc==-1) { return ((com.wangc.bill.http.entity.BillInfo)dexR(result[1])); }
            if(pc<0x164) pc=recovered_t_block0(v,result,pc);
            else if(pc<0x2be) pc=recovered_t_block1(v,result,pc);
            else pc=recovered_t_block2(v,result,pc);
        }
    }
    private int recovered_t_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                v[0]=v[17];
                result[0]=dexN((((java.util.List)dexR(v[17])).isEmpty())?1:0);
                v[1]=result[0];
                v[2]=dexN(0L);
                pc=(dexZero(v[1]))?0xa:0x9; continue;
            }
            case 0x9: {
                result[1]=v[2]; return -1;
            }
            case 0xa: {
                v[1]=new DexReference(); // new com.wangc.bill.http.entity.BillInfo
                ((DexReference)v[1]).value=new com.wangc.bill.http.entity.BillInfo();
                v[3]="抖音";
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setOrigin(((java.lang.String)dexR(v[3])));
                v[3]=dexN(0L);
                pc=0x15; continue;
            }
            case 0x15: {
                result[0]=dexN(((java.util.List)dexR(v[17])).size());
                v[4]=result[0];
                v[5]="";
                pc=(dexI(v[3])>=dexI(v[4]))?0x34d:0x1d; continue;
            }
            case 0x1d: {
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[3]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                v[6]="支付成功";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                v[7]=dexN(2L);
                v[8]=dexN(1L);
                pc=(!dexZero(v[6]))?0x75:0x2d; continue;
            }
            case 0x2d: {
                v[6]="退款成功";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x75:0x35; continue;
            }
            case 0x35: {
                v[6]="已过期";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x75:0x3d; continue;
            }
            case 0x3d: {
                v[6]="已全额退款";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x75:0x45; continue;
            }
            case 0x45: {
                v[6]="入账成功";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x75:0x4d; continue;
            }
            case 0x4d: {
                v[6]="确认收货后付款";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x75:0x55; continue;
            }
            case 0x55: {
                v[6]="到账成功";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x75:0x5d; continue;
            }
            case 0x5d: {
                v[6]="充值成功";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x75:0x65; continue;
            }
            case 0x65: {
                v[6]="自动付款成功";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x75:0x6d; continue;
            }
            case 0x6d: {
                v[6]="已退款(￥";
                result[0]=dexN((((java.lang.String)dexR(v[4])).startsWith(((java.lang.String)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(dexZero(v[6]))?0x168:0x75; continue;
            }
            case 0x75: {
                pc=(dexI(v[3])<=dexI(v[7]))?0x168:0x77; continue;
            }
            case 0x77: {
                v[4]=dexN(dexI(v[3])+(-1));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                v[6]="¥";
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[5])));
                v[4]=result[0];
                v[9]=",";
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[5])));
                v[4]=result[0];
                v[10]="+";
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[5])));
                v[4]=result[0];
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0xad:0x97; continue;
            }
            case 0x97: {
                v[4]=dexN(dexI(v[3])+(-2));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[5])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[5])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[5])));
                v[4]=result[0];
                v[6]=v[7];
                pc=0xca; continue;
            }
            case 0xad: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[11]=result[0];
                pc=(!dexZero(v[11]))?0xc9:0xb3; continue;
            }
            case 0xb3: {
                v[4]=dexN(dexI(v[3])+(-3));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[6])),((java.lang.CharSequence)dexR(v[5])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[9])),((java.lang.CharSequence)dexR(v[5])));
                v[4]=result[0];
                result[0]=((java.lang.String)dexR(v[4])).replace(((java.lang.CharSequence)dexR(v[10])),((java.lang.CharSequence)dexR(v[5])));
                v[4]=result[0];
                v[6]=dexN(3L);
                pc=0xca; continue;
            }
            case 0xc9: {
                v[6]=v[8];
                pc=0xca; continue;
            }
            case 0xca: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getNumber();
                v[9]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[9]))))?1:0);
                v[9]=result[0];
                pc=(dexZero(v[9]))?0x164:0xd4; continue;
            }
            case 0xd4: {
                result[0]=dexN((com.wangc.bill.utils.h2.J(((java.lang.String)dexR(v[4]))))?1:0);
                v[9]=result[0];
                pc=(dexZero(v[9]))?0x164:0xda; continue;
            }
            case 0xda: {
                v[9]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[9]).value=new java.lang.StringBuilder();
                result[0]=dexN(Double.doubleToRawLongBits(com.wangc.bill.utils.h2.S(((java.lang.String)dexR(v[4])))));
                v[11]=result[0];
                result[0]=dexN(Double.doubleToRawLongBits(java.lang.Math.abs(dexD(v[11]))));
                v[11]=result[0];
                result[0]=((java.lang.StringBuilder)dexR(v[9])).append(dexD(v[11]));
                result[0]=((java.lang.StringBuilder)dexR(v[9])).append(((java.lang.String)dexR(v[5])));
                result[0]=((java.lang.StringBuilder)dexR(v[9])).toString();
                v[4]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setNumber(((java.lang.String)dexR(v[4])));
                v[4]="png";
                pc=(dexI(v[3])<=dexI(v[6]))?0x124:0xf8; continue;
            }
            case 0xf8: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getRemark();
                v[5]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x124:0x102; continue;
            }
            case 0x102: {
                v[5]=dexN(dexI(v[3])-dexI(v[6]));
                v[5]=dexN(dexI(v[5])-dexI(v[8]));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[5]));
                v[9]=result[0];
                v[9]=((java.lang.String)dexR(v[9]));
                result[0]=dexN((((java.lang.String)dexR(v[9])).startsWith(((java.lang.String)dexR(v[4]))))?1:0);
                v[9]=result[0];
                pc=(!dexZero(v[9]))?0x124:0x111; continue;
            }
            case 0x111: {
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[5]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setRemark(((java.lang.String)dexR(v[4])));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[5]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setShopName(((java.lang.String)dexR(v[4])));
                pc=0x153; continue;
            }
            case 0x124: {
                v[5]=dexN(dexI(v[6])+(1));
                pc=(dexI(v[3])<=dexI(v[5]))?0x153:0x128; continue;
            }
            case 0x128: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getRemark();
                v[5]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x153:0x132; continue;
            }
            case 0x132: {
                v[5]=dexN(dexI(v[3])-dexI(v[6]));
                v[5]=dexN(dexI(v[5])-dexI(v[7]));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[5]));
                v[7]=result[0];
                v[7]=((java.lang.String)dexR(v[7]));
                result[0]=dexN((((java.lang.String)dexR(v[7])).startsWith(((java.lang.String)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x153:0x141; continue;
            }
            case 0x141: {
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[5]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setRemark(((java.lang.String)dexR(v[4])));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[5]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setShopName(((java.lang.String)dexR(v[4])));
                pc=0x153; continue;
            }
            case 0x153: {
                v[4]=dexN(dexI(v[3])-dexI(v[6]));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                result[0]=dexN((((java.lang.String)dexR(v[4])).contains(((java.lang.CharSequence)dexR(v[10]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x164:0x161; continue;
            }
            case 0x161: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setIncome(dexZ(v[8]));
                pc=0x164; continue;
            }
            default: return pc;
        }
    }
    private int recovered_t_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x164: {
                v[6]=v[16];
                pc=0x349; continue;
            }
            case 0x168: {
                v[5]="支付方式";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x190:0x170; continue;
            }
            case 0x170: {
                v[5]="退款方式";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x190:0x178; continue;
            }
            case 0x178: {
                v[5]="到账方式";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x190:0x180; continue;
            }
            case 0x180: {
                v[5]="充值方式";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x190:0x188; continue;
            }
            case 0x188: {
                v[5]="付款方式";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x1a3:0x190; continue;
            }
            case 0x190: {
                result[0]=dexN(((java.util.List)dexR(v[17])).size());
                v[5]=result[0];
                v[5]=dexN(dexI(v[5])-dexI(v[7]));
                pc=(dexI(v[3])>=dexI(v[5]))?0x1a3:0x197; continue;
            }
            case 0x197: {
                v[4]=dexN(dexI(v[3])+(2));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setAsset(((java.lang.String)dexR(v[4])));
                pc=0x164; continue;
            }
            case 0x1a3: {
                v[5]="交易方式";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x1b3:0x1ab; continue;
            }
            case 0x1ab: {
                v[5]="入账账户";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x1c6:0x1b3; continue;
            }
            case 0x1b3: {
                result[0]=dexN(((java.util.List)dexR(v[17])).size());
                v[5]=result[0];
                v[5]=dexN(dexI(v[5])-dexI(v[8]));
                pc=(dexI(v[3])>=dexI(v[5]))?0x1c6:0x1ba; continue;
            }
            case 0x1ba: {
                v[4]=dexN(dexI(v[3])+(1));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setAsset(((java.lang.String)dexR(v[4])));
                pc=0x164; continue;
            }
            case 0x1c6: {
                v[5]="退款时间";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                v[6]="yyyy-MM-dd";
                v[9]=" ";
                v[10]=dexN(-1L);
                v[12]="yyyy-MM-dd HH:mm:ss";
                pc=(!dexZero(v[5]))?0x1fe:0x1d6; continue;
            }
            case 0x1d6: {
                v[5]="过期时间";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x1fe:0x1de; continue;
            }
            case 0x1de: {
                v[5]="入账时间";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x1fe:0x1e6; continue;
            }
            case 0x1e6: {
                v[5]="到账时间";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x1fe:0x1ee; continue;
            }
            case 0x1ee: {
                v[5]="发起话费充值";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x1fe:0x1f6; continue;
            }
            case 0x1f6: {
                v[5]="充值时间";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x261:0x1fe; continue;
            }
            case 0x1fe: {
                result[0]=dexN(((java.util.List)dexR(v[17])).size());
                v[5]=result[0];
                v[5]=dexN(dexI(v[5])-dexI(v[8]));
                pc=(dexI(v[3])>=dexI(v[5]))?0x261:0x205; continue;
            }
            case 0x205: {
                v[4]=dexN(dexI(v[3])+(1));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[5]=result[0];
                v[5]=((java.lang.String)dexR(v[5]));
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[5])),((java.lang.String)dexR(v[12]))));
                v[13]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setTime(dexL(v[13]));
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getTime());
                v[13]=result[0];
                v[5]=dexN(Long.compare(dexL(v[13]),dexL(v[10])));
                pc=(!dexZero(v[5]))?0x24a:0x21c; continue;
            }
            case 0x21c: {
                result[0]=dexN(((java.util.List)dexR(v[17])).size());
                v[5]=result[0];
                v[5]=dexN(dexI(v[5])-dexI(v[7]));
                pc=(dexI(v[3])>=dexI(v[5]))?0x24a:0x223; continue;
            }
            case 0x223: {
                v[5]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[5]).value=new java.lang.StringBuilder();
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[7]=result[0];
                v[7]=((java.lang.String)dexR(v[7]));
                result[0]=((java.lang.StringBuilder)dexR(v[5])).append(((java.lang.String)dexR(v[7])));
                result[0]=((java.lang.StringBuilder)dexR(v[5])).append(((java.lang.String)dexR(v[9])));
                v[7]=dexN(dexI(v[3])+(2));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[7]));
                v[7]=result[0];
                v[7]=((java.lang.String)dexR(v[7]));
                result[0]=((java.lang.StringBuilder)dexR(v[5])).append(((java.lang.String)dexR(v[7])));
                result[0]=((java.lang.StringBuilder)dexR(v[5])).toString();
                v[5]=result[0];
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[5])),((java.lang.String)dexR(v[12]))));
                v[7]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setTime(dexL(v[7]));
                pc=0x24a; continue;
            }
            case 0x24a: {
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getTime());
                v[7]=result[0];
                v[5]=dexN(Long.compare(dexL(v[7]),dexL(v[10])));
                pc=(!dexZero(v[5]))?0x164:0x252; continue;
            }
            case 0x252: {
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[6]))));
                v[4]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setTime(dexL(v[4]));
                pc=0x164; continue;
            }
            case 0x261: {
                v[5]="创建时间";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x271:0x269; continue;
            }
            case 0x269: {
                v[5]="支付时间";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x2cd:0x271; continue;
            }
            case 0x271: {
                result[0]=dexN(((java.util.List)dexR(v[17])).size());
                v[5]=result[0];
                v[5]=dexN(dexI(v[5])-dexI(v[8]));
                pc=(dexI(v[3])>=dexI(v[5]))?0x2cd:0x278; continue;
            }
            case 0x278: {
                v[4]=dexN(dexI(v[3])+(1));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[5]=result[0];
                v[5]=((java.lang.String)dexR(v[5]));
                result[0]=dexN(((java.util.List)dexR(v[17])).size());
                v[8]=result[0];
                v[8]=dexN(dexI(v[8])-dexI(v[7]));
                pc=(dexI(v[3])>=dexI(v[8]))?0x2af:0x287; continue;
            }
            case 0x287: {
                v[7]=dexN(dexI(v[3])+(2));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[7]));
                v[7]=result[0];
                v[7]=((java.lang.String)dexR(v[7]));
                v[8]=":";
                result[0]=dexN((((java.lang.String)dexR(v[7])).contains(((java.lang.CharSequence)dexR(v[8]))))?1:0);
                v[13]=result[0];
                pc=(dexZero(v[13]))?0x2af:0x297; continue;
            }
            case 0x297: {
                result[0]=dexN((((java.lang.String)dexR(v[5])).contains(((java.lang.CharSequence)dexR(v[8]))))?1:0);
                v[8]=result[0];
                pc=(!dexZero(v[8]))?0x2af:0x29d; continue;
            }
            case 0x29d: {
                v[8]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[8]).value=new java.lang.StringBuilder();
                result[0]=((java.lang.StringBuilder)dexR(v[8])).append(((java.lang.String)dexR(v[5])));
                result[0]=((java.lang.StringBuilder)dexR(v[8])).append(((java.lang.String)dexR(v[9])));
                result[0]=((java.lang.StringBuilder)dexR(v[8])).append(((java.lang.String)dexR(v[7])));
                result[0]=((java.lang.StringBuilder)dexR(v[8])).toString();
                v[5]=result[0];
                pc=0x2af; continue;
            }
            case 0x2af: {
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[5])),((java.lang.String)dexR(v[12]))));
                v[7]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setTime(dexL(v[7]));
                result[0]=dexN(((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getTime());
                v[7]=result[0];
                v[5]=dexN(Long.compare(dexL(v[7]),dexL(v[10])));
                pc=(!dexZero(v[5]))?0x164:0x2be; continue;
            }
            default: return pc;
        }
    }
    private int recovered_t_block2(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x2be: {
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                result[0]=dexN(com.blankj.utilcode.util.t0.X0(((java.lang.String)dexR(v[4])),((java.lang.String)dexR(v[6]))));
                v[4]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setTime(dexL(v[4]));
                pc=0x164; continue;
            }
            case 0x2cd: {
                v[5]="商品订单";
                result[0]=dexN((((java.lang.String)dexR(v[4])).equals(((java.lang.Object)dexR(v[5]))))?1:0);
                v[4]=result[0];
                pc=(dexZero(v[4]))?0x164:0x2d5; continue;
            }
            case 0x2d5: {
                result[0]=dexN(((java.util.List)dexR(v[17])).size());
                v[4]=result[0];
                v[4]=dexN(dexI(v[4])-dexI(v[7]));
                pc=(dexI(v[3])>=dexI(v[4]))?0x164:0x2dc; continue;
            }
            case 0x2dc: {
                v[4]=dexN(dexI(v[3])+(1));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setRemark(((java.lang.String)dexR(v[2])));
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x2fd:0x2ed; continue;
            }
            case 0x2ed: {
                v[5]="~240x0";
                result[0]=dexN((((java.lang.String)dexR(v[4])).endsWith(((java.lang.String)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x305:0x2f5; continue;
            }
            case 0x2f5: {
                v[5]="&x-expires=";
                result[0]=dexN((((java.lang.String)dexR(v[4])).contains(((java.lang.CharSequence)dexR(v[5]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x305:0x2fd; continue;
            }
            case 0x2fd: {
                v[5]="在此商户的交易";
                result[0]=dexN((((java.lang.String)dexR(v[5])).equals(((java.lang.Object)dexR(v[4]))))?1:0);
                v[5]=result[0];
                pc=(dexZero(v[5]))?0x311:0x305; continue;
            }
            case 0x305: {
                v[4]=dexN(dexI(v[3])+(2));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[4]));
                v[4]=result[0];
                v[4]=((java.lang.String)dexR(v[4]));
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setRemark(((java.lang.String)dexR(v[4])));
                pc=0x334; continue;
            }
            case 0x311: {
                v[5]="申请电子回单";
                result[0]=dexN((((java.lang.String)dexR(v[5])).equals(((java.lang.Object)dexR(v[4]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x348:0x319; continue;
            }
            case 0x319: {
                v[5]="查看月付账单";
                result[0]=dexN((((java.lang.String)dexR(v[5])).equals(((java.lang.Object)dexR(v[4]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x348:0x321; continue;
            }
            case 0x321: {
                v[5]="联系商家";
                result[0]=dexN((((java.lang.String)dexR(v[5])).equals(((java.lang.Object)dexR(v[4]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x348:0x329; continue;
            }
            case 0x329: {
                v[5]="申请电子交易凭证";
                result[0]=dexN((((java.lang.String)dexR(v[5])).equals(((java.lang.Object)dexR(v[4]))))?1:0);
                v[5]=result[0];
                pc=(!dexZero(v[5]))?0x348:0x331; continue;
            }
            case 0x331: {
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setRemark(((java.lang.String)dexR(v[4])));
                pc=0x334; continue;
            }
            case 0x334: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getRemark();
                v[4]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[4]))))?1:0);
                v[4]=result[0];
                pc=(!dexZero(v[4]))?0x164:0x33e; continue;
            }
            case 0x33e: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getRemark();
                v[4]=result[0];
                v[6]=v[16];
                com.google.android.accessibility.selecttospeak.SelectToSpeakService.E(((android.content.Context)dexR(v[6])),((java.lang.String)dexR(v[4])));
                pc=0x349; continue;
            }
            case 0x348: {
                result[1]=v[2]; return -1;
            }
            case 0x349: {
                v[3]=dexN(dexI(v[3])+(1));
                pc=0x15; continue;
            }
            case 0x34d: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getAsset();
                v[0]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x37e:0x357; continue;
            }
            case 0x357: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getAsset();
                v[0]=result[0];
                v[3]="抖音支付";
                result[0]=dexN((((java.lang.String)dexR(v[0])).contains(((java.lang.CharSequence)dexR(v[3]))))?1:0);
                v[0]=result[0];
                pc=(dexZero(v[0]))?0x37e:0x363; continue;
            }
            case 0x363: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getAsset();
                v[0]=result[0];
                result[0]=((java.lang.String)dexR(v[0])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[5])));
                v[0]=result[0];
                v[3]="(";
                result[0]=((java.lang.String)dexR(v[0])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[5])));
                v[0]=result[0];
                v[3]=")";
                result[0]=((java.lang.String)dexR(v[0])).replace(((java.lang.CharSequence)dexR(v[3])),((java.lang.CharSequence)dexR(v[5])));
                v[0]=result[0];
                result[0]=((java.lang.String)dexR(v[0])).trim();
                v[0]=result[0];
                ((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).setAsset(((java.lang.String)dexR(v[0])));
                pc=0x37e; continue;
            }
            case 0x37e: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getRemark();
                v[0]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x393:0x388; continue;
            }
            case 0x388: {
                result[0]=((com.wangc.bill.http.entity.BillInfo)dexR(v[1])).getNumber();
                v[0]=result[0];
                result[0]=dexN((android.text.TextUtils.isEmpty(((java.lang.CharSequence)dexR(v[0]))))?1:0);
                v[0]=result[0];
                pc=(!dexZero(v[0]))?0x393:0x392; continue;
            }
            case 0x392: {
                result[1]=v[1]; return -1;
            }
            case 0x393: {
                result[1]=v[2]; return -1;
            }
            default: return pc;
        }
    }

    public BillInfo u(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setTransfer(true);
        billInfo.setOrigin("抖音");
        billInfo.setToAsset("抖音月付");
        billInfo.setRemark("抖音月付还款");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if ((str.equals("支付成功") || str.equals("还款成功")) && i6 > 0) {
                String strReplace = list.get(i6 - 1).replace("¥", "").replace(",", "").replace("+", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(Math.abs(h2.S(strReplace)) + "");
                }
                if (str.equals("还款成功") && i6 > 1) {
                    billInfo.setRemark(list.get(i6 - 2));
                }
            } else if ((str.equals("还款方式") || str.equals("付款方式")) && i6 < list.size() - 2) {
                billInfo.setFromAsset(list.get(i6 + 2).replace("抖音支付", "").replace("(", "").replace(")", "").trim());
            } else if (str.equals("还款到") && i6 < list.size() - 1) {
                billInfo.setToAsset(list.get(i6 + 1).replace("(", "").replace(")", "").trim());
            } else if ((str.equals("还款时间") || str.equals("发起时间")) && i6 < list.size() - 1) {
                billInfo.setTime(t0.X0(list.get(i6 + 1), "yyyy-MM-dd HH:mm:ss"));
            }
        }
        if (TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public boolean w(List<AccessibilityNodeInfo> list) {
        return (list == null || list.isEmpty()) ? false : true;
    }
}
