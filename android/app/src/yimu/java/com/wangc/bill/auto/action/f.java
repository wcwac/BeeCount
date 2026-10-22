// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 62986e41eec2777e302e2501373ebcc74a45b2a4dfd6fd7280ab723b3efcf728
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
import com.wangc.bill.utils.h2;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class f extends c implements x1.a {

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private static final int f13693g = 1;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private static final int f13694h = 2;

    /* JADX INFO: renamed from: i, reason: collision with root package name */
    private static final int f13695i = 3;

    /* JADX INFO: renamed from: j, reason: collision with root package name */
    private static final int f13696j = 4;

    /* JADX INFO: renamed from: k, reason: collision with root package name */
    private static f f13697k;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private boolean f13698e = false;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13699f;

    private void v(Context context, List<String> list, String str) {
        y.l("start find node :" + str);
        if (list == null || list.size() <= 0) {
            return;
        }
        BillInfo billInfoR = this.a == 1 ? r(list) : null;
        int i6 = this.a;
        if (i6 == 3) {
            billInfoR = u(list);
        } else if (i6 == 2) {
            billInfoR = s(list);
        } else if (i6 == 4) {
            billInfoR = t(list);
        }
        if (billInfoR != null) {
            this.b = false;
            this.f13698e = false;
            billInfoR.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
            SelectToSpeakService.l(context, billInfoR, this);
        }
    }

    public static f w() {
        if (f13697k == null) {
            f13697k = new f();
        }
        return f13697k;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
    }

    /** DEX-exact branch transcription of q; original register and block addresses retained. */
    public void q(android.content.Context arg0, java.lang.String arg1, android.view.accessibility.AccessibilityNodeInfo arg2) {
        Object[] v=new Object[11]; Object[] result=new Object[2]; int pc=0;
        v[7]=this;
        v[8]=arg0;
        v[9]=arg1;
        v[10]=arg2;
        while(true) {
            if(pc==-1) { return; }
            if(pc<0x84) pc=recovered_q_block0(v,result,pc);
            else pc=recovered_q_block1(v,result,pc);
        }
    }
    private int recovered_q_block0(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x0: {
                v[0]=dexN(4L);
                v[1]=dexN(3L);
                v[2]=dexN(2L);
                v[3]=dexN(0L);
                result[0]=dexN(((java.lang.String)dexR(v[9])).hashCode());
                v[4]=dexN(1L);
                v[5]=dexN(-1L);
                result[0]=dexN(((java.lang.String)dexR(v[9])).hashCode());
                v[6]=result[0];
                switch(dexI(v[6])) {
                case -2030087667: pc=0x5f; break;
                case -1349345186: pc=0x54; break;
                case -1175845833: pc=0x49; break;
                case -855483783: pc=0x3e; break;
                case 219764026: pc=0x33; break;
                case 998460551: pc=0x28; break;
                case 1086175120: pc=0x1d; break;
                case 1454072758: pc=0x12; break;
                default: pc=0x10; break;
                } continue;
            }
            case 0x10: {
                pc=0x69; continue;
            }
            case 0x12: {
                v[6]="com.huawei.wallet.view.MainActivity";
                result[0]=dexN((((java.lang.String)dexR(v[9])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x1b:0x1a; continue;
            }
            case 0x1a: {
                pc=0x69; continue;
            }
            case 0x1b: {
                v[5]=dexN(7L);
                pc=0x69; continue;
            }
            case 0x1d: {
                v[6]="com.huawei.nfc.carrera.ui.swipe.SwipeActivity";
                result[0]=dexN((((java.lang.String)dexR(v[9])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x26:0x25; continue;
            }
            case 0x25: {
                pc=0x69; continue;
            }
            case 0x26: {
                v[5]=dexN(6L);
                pc=0x69; continue;
            }
            case 0x28: {
                v[6]="com.huawei.wallet.nfc.bankcard.paymentcode.ui.PaymentCodeActivity";
                result[0]=dexN((((java.lang.String)dexR(v[9])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x31:0x30; continue;
            }
            case 0x30: {
                pc=0x69; continue;
            }
            case 0x31: {
                v[5]=dexN(5L);
                pc=0x69; continue;
            }
            case 0x33: {
                v[6]="com.huawei.wallet.bankcard.scanpay.ui.activity.ScanQrCodePayResultActivity";
                result[0]=dexN((((java.lang.String)dexR(v[9])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x3c:0x3b; continue;
            }
            case 0x3b: {
                pc=0x69; continue;
            }
            case 0x3c: {
                v[5]=v[0];
                pc=0x69; continue;
            }
            case 0x3e: {
                v[6]="com.huawei.petalpay.userapp.function.billdetails.activity.BillDetailInfoActivity";
                result[0]=dexN((((java.lang.String)dexR(v[9])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x47:0x46; continue;
            }
            case 0x46: {
                pc=0x69; continue;
            }
            case 0x47: {
                v[5]=v[1];
                pc=0x69; continue;
            }
            case 0x49: {
                v[6]="com.huawei.wallet.nfc.bankcard.transactionhistory.ui.activity.UnCardTransRecordDetailActivity";
                result[0]=dexN((((java.lang.String)dexR(v[9])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x52:0x51; continue;
            }
            case 0x51: {
                pc=0x69; continue;
            }
            case 0x52: {
                v[5]=v[2];
                pc=0x69; continue;
            }
            case 0x54: {
                v[6]="com.huawei.wallet.nfc.bankcard.transactionhistory.ui.activity.UnCardTransRecordFilterActivity";
                result[0]=dexN((((java.lang.String)dexR(v[9])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x5d:0x5c; continue;
            }
            case 0x5c: {
                pc=0x69; continue;
            }
            case 0x5d: {
                v[5]=v[4];
                pc=0x69; continue;
            }
            case 0x5f: {
                v[6]="com.huawei.nfc.carrera.ui.swipe.SwipeResultActivity";
                result[0]=dexN((((java.lang.String)dexR(v[9])).equals(((java.lang.Object)dexR(v[6]))))?1:0);
                v[6]=result[0];
                pc=(!dexZero(v[6]))?0x68:0x67; continue;
            }
            case 0x67: {
                pc=0x69; continue;
            }
            case 0x68: {
                v[5]=v[3];
                pc=0x69; continue;
            }
            case 0x69: {
                switch(dexI(v[5])) {
                case 0: pc=0xeb; break;
                case 1: pc=0xe4; break;
                case 2: pc=0xdd; break;
                case 3: pc=0xd6; break;
                case 4: pc=0xcf; break;
                case 5: pc=0x6e; break;
                case 6: pc=0x6e; break;
                case 7: pc=0xe4; break;
                default: pc=0x6c; break;
                } continue;
            }
            case 0x6c: {
                pc=0xf1; continue;
            }
            case 0x6e: {
                pc=(dexZero(v[10]))?0xf1:0x70; continue;
            }
            case 0x70: {
                result[0]=((com.wangc.bill.auto.action.c)dexR(v[7])).m(((android.view.accessibility.AccessibilityNodeInfo)dexR(v[10])));
                v[0]=result[0];
                pc=(dexZero(v[0]))?0xf2:0x76; continue;
            }
            case 0x76: {
                result[0]=dexN((((java.util.List)dexR(v[0])).isEmpty())?1:0);
                v[1]=result[0];
                pc=(!dexZero(v[1]))?0xf2:0x7c; continue;
            }
            case 0x7c: {
                v[1]="二维码付款码";
                result[0]=dexN(((java.util.List)dexR(v[0])).indexOf(((java.lang.Object)dexR(v[1]))));
                v[1]=result[0];
                pc=(dexI(v[1])<0)?0xf2:0x84; continue;
            }
            default: return pc;
        }
    }
    private int recovered_q_block1(Object[] v,Object[] result,int pc) {
        while(true) switch(pc) {
            case 0x84: {
                result[0]=dexN(((java.util.List)dexR(v[0])).size());
                v[2]=result[0];
                v[2]=dexN(dexI(v[2])-dexI(v[4]));
                pc=(dexI(v[1])>=dexI(v[2]))?0xf2:0x8b; continue;
            }
            case 0x8b: {
                v[1]=dexN(dexI(v[1])+dexI(v[4]));
                result[0]=((java.util.List)dexR(v[0])).get(dexI(v[1]));
                v[1]=result[0];
                v[1]=((java.lang.String)dexR(v[1]));
                v[2]="（";
                v[3]="";
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[2])),((java.lang.CharSequence)dexR(v[3])));
                v[1]=result[0];
                v[2]="）";
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[2])),((java.lang.CharSequence)dexR(v[3])));
                v[1]=result[0];
                v[2]="尾号";
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[2])),((java.lang.CharSequence)dexR(v[3])));
                v[1]=result[0];
                v[2]=" ";
                result[0]=((java.lang.String)dexR(v[1])).replace(((java.lang.CharSequence)dexR(v[2])),((java.lang.CharSequence)dexR(v[3])));
                v[1]=result[0];
                result[0]=((java.lang.String)dexR(v[1])).trim();
                v[1]=result[0];
                ((com.wangc.bill.auto.action.f)dexR(v[7])).f13699f=((java.lang.String)dexR(v[1]));
                v[1]=new DexReference(); // new java.lang.StringBuilder
                ((DexReference)v[1]).value=new java.lang.StringBuilder();
                v[2]="asset:";
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[2])));
                v[2]=((com.wangc.bill.auto.action.f)dexR(v[7])).f13699f;
                result[0]=((java.lang.StringBuilder)dexR(v[1])).append(((java.lang.String)dexR(v[2])));
                result[0]=((java.lang.StringBuilder)dexR(v[1])).toString();
                v[1]=result[0];
                v[2]="sssss";
                result[0]=new java.lang.Object[]{((java.lang.Object)dexR(v[2])),((java.lang.Object)dexR(v[1]))};
                v[1]=result[0];
                com.blankj.utilcode.util.y.l(((java.lang.Object[])dexR(v[1])));
                pc=0xf2; continue;
            }
            case 0xcf: {
                ((com.wangc.bill.auto.action.c)dexR(v[7])).a=dexI(v[1]);
                ((com.wangc.bill.auto.action.f)dexR(v[7])).f13698e=dexZ(v[4]);
                ((com.wangc.bill.auto.action.c)dexR(v[7])).b=dexZ(v[4]);
                pc=0xf1; continue;
            }
            case 0xd6: {
                ((com.wangc.bill.auto.action.c)dexR(v[7])).a=dexI(v[0]);
                ((com.wangc.bill.auto.action.f)dexR(v[7])).f13698e=dexZ(v[4]);
                ((com.wangc.bill.auto.action.c)dexR(v[7])).b=dexZ(v[4]);
                pc=0xf1; continue;
            }
            case 0xdd: {
                ((com.wangc.bill.auto.action.c)dexR(v[7])).a=dexI(v[2]);
                ((com.wangc.bill.auto.action.f)dexR(v[7])).f13698e=dexZ(v[4]);
                ((com.wangc.bill.auto.action.c)dexR(v[7])).b=dexZ(v[4]);
                pc=0xf1; continue;
            }
            case 0xe4: {
                ((com.wangc.bill.auto.action.c)dexR(v[7])).a=dexI(v[3]);
                ((com.wangc.bill.auto.action.c)dexR(v[7])).b=dexZ(v[3]);
                ((com.wangc.bill.auto.action.f)dexR(v[7])).f13698e=dexZ(v[3]);
                pc=0xf1; continue;
            }
            case 0xeb: {
                ((com.wangc.bill.auto.action.c)dexR(v[7])).a=dexI(v[4]);
                ((com.wangc.bill.auto.action.f)dexR(v[7])).f13698e=dexZ(v[4]);
                ((com.wangc.bill.auto.action.c)dexR(v[7])).b=dexZ(v[4]);
                pc=0xf1; continue;
            }
            case 0xf1: {
                v[0]=dexN(0L);
                pc=0xf2; continue;
            }
            case 0xf2: {
                v[1]=dexN((((com.wangc.bill.auto.action.f)dexR(v[7])).f13698e)?1:0);
                pc=(dexZero(v[1]))?0xfc:0xf6; continue;
            }
            case 0xf6: {
                pc=(dexZero(v[10]))?0xfc:0xf8; continue;
            }
            case 0xf8: {
                result[0]=((com.wangc.bill.auto.action.c)dexR(v[7])).m(((android.view.accessibility.AccessibilityNodeInfo)dexR(v[10])));
                v[0]=result[0];
                pc=0xfc; continue;
            }
            case 0xfc: {
                v[1]=dexN((((com.wangc.bill.auto.action.c)dexR(v[7])).b)?1:0);
                pc=(dexZero(v[1]))?0x105:0x100; continue;
            }
            case 0x100: {
                pc=(dexZero(v[10]))?0x105:0x102; continue;
            }
            case 0x102: {
                ((com.wangc.bill.auto.action.f)dexR(v[7])).v(((android.content.Context)dexR(v[8])),((java.util.List)dexR(v[0])),((java.lang.String)dexR(v[9])));
                pc=0x105; continue;
            }
            case 0x105: {
                return -1;
            }
            default: return pc;
        }
    }

    public BillInfo r(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("华为支付");
        int iD = d(list, "¥", true);
        if (iD > 0 && iD < list.size() - 1) {
            String str = list.get(iD + 1);
            if (h2.J(str)) {
                billInfo.setNumber(str);
                billInfo.setRemark(list.get(iD - 1));
                if (iD > 2) {
                    billInfo.setAsset(list.get(iD - 3) + list.get(iD - 2));
                } else {
                    billInfo.setAsset(this.f13699f);
                }
                return billInfo;
            }
        }
        return null;
    }

    public BillInfo s(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("华为支付");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.contains("¥") && TextUtils.isEmpty(billInfo.getNumber()) && i6 < list.size() - 3) {
                String strReplace = list.get(i6 + 1).replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                    billInfo.setIncome(!str.contains("-"));
                    billInfo.setAsset(list.get(i6 + 3));
                }
            } else if (str.equals("商家") && i6 < list.size() - 1) {
                String str2 = list.get(i6 + 1);
                billInfo.setRemark(str2);
                billInfo.setShopName(str2);
            } else if (str.equals("优惠金额") && i6 < list.size() - 1) {
                String strReplace2 = list.get(i6 + 1).replace("¥", "");
                if (h2.J(strReplace2)) {
                    billInfo.setDiscount(strReplace2);
                }
            } else if (str.equals("日期") && i6 < list.size() - 1) {
                billInfo.setTime(t0.X0(list.get(i6 + 1), "yyyy-MM-dd HH:mm:ss"));
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo t(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("华为支付");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.equals("交易成功") && i6 > 0 && i6 < list.size() - 1) {
                String str2 = list.get(i6 - 1);
                String strReplace = str2.replace(",", "").replace("+", "").replace("-", "").replace(" ", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                    billInfo.setIncome(!str2.contains("-"));
                    billInfo.setRemark(list.get(i6 + 1));
                    billInfo.setShopName(billInfo.getRemark());
                }
            } else if (str.equals("付款银行") && i6 < list.size() - 1) {
                billInfo.setAsset(list.get(i6 + 1).replace(" ", "").replace("(", "").replace(")", ""));
            } else if (str.equals("商品名称") && i6 < list.size() - 1) {
                billInfo.setRemark(list.get(i6 + 1));
            } else if (str.equals("交易时间") && i6 < list.size() - 1) {
                billInfo.setTime(t0.X0(list.get(i6 + 1), "yyyy-MM-dd HH:mm:ss"));
            }
        }
        if (TextUtils.isEmpty(billInfo.getRemark()) || TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }

    public BillInfo u(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("华为支付");
        int iD = d(list, "¥", true);
        if (iD > 2 && iD < list.size() - 2) {
            String str = list.get(iD + 1);
            if (h2.J(str)) {
                billInfo.setNumber(str);
                billInfo.setRemark(list.get(iD + 2));
                int iD2 = d(list, "付款方式", true);
                if (iD2 > 0 && iD2 < list.size() - 1) {
                    billInfo.setAsset(list.get(iD2 + 1));
                }
                return billInfo;
            }
        }
        return null;
    }
}
