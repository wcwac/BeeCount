// Yimu 6.3.0 recovered DEX/JADX; original Java SHA256 e6f7273a540a98a76f38ae8a4276bc6fbeec7deb30a56e1ed47baf0af795e55f
package com.wangc.bill.auto.action;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import com.blankj.utilcode.util.y;
import com.google.android.accessibility.selecttospeak.SelectToSpeakService;
import com.wangc.bill.auto.x1;
import com.wangc.bill.http.entity.BillInfo;
import com.wangc.bill.utils.h2;
import java.util.List;

/* JADX INFO: loaded from: /home/kotori/aw/yimu-actual/dex/classes4.dex */
public class g extends c implements x1.a {

    /* JADX INFO: renamed from: g, reason: collision with root package name */
    private static final int f13700g = 1;

    /* JADX INFO: renamed from: h, reason: collision with root package name */
    private static g f13701h;

    /* JADX INFO: renamed from: e, reason: collision with root package name */
    private boolean f13702e = false;

    /* JADX INFO: renamed from: f, reason: collision with root package name */
    private String f13703f;

    private void s(Context context, List<String> list, String str) {
        y.l("start find node :" + str);
        if (list == null || list.size() <= 0) {
            return;
        }
        BillInfo billInfoR = (this.a == 1 && list.contains("支付成功")) ? r(list) : null;
        if (billInfoR != null) {
            this.b = false;
            this.f13702e = false;
            billInfoR.setTagList(com.wangc.bill.database.action.p.d(this.f13681d));
            SelectToSpeakService.l(context, billInfoR, this);
        }
    }

    public static g t() {
        if (f13701h == null) {
            f13701h = new g();
        }
        return f13701h;
    }

    @Override // com.wangc.bill.auto.x1.a
    public void dismiss() {
    }

    public void q(Context context, String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        List<String> listM;
        int iD;
        if (str.equals("com.wudaokou.hippo.launcher.splash.SplashActivity") || str.equals("com.wudaokou.hippo.mine.MinePageActivity")) {
            this.a = 0;
            this.b = false;
            this.f13702e = false;
        } else if (str.equals("com.wudaokou.hippo.pay.PayCodeActivity")) {
            this.f13702e = true;
            this.a = 1;
        }
        if (!this.f13702e || accessibilityNodeInfo == null) {
            listM = null;
        } else {
            listM = m(accessibilityNodeInfo);
            if (a(listM, "优先使用此付款方式", false) && (iD = d(listM, "优先使用此付款方式", false)) >= 0) {
                this.f13703f = listM.get(iD).replace("优先使用此付款方式", "").replace("\n", "");
                y.l("sssss", "asset:" + this.f13703f);
            }
            if (a(listM, "支付成功", true) && a(listM, "完成", true)) {
                this.b = true;
            }
        }
        if (!this.b || accessibilityNodeInfo == null) {
            return;
        }
        s(context, listM, str);
    }

    public BillInfo r(List<String> list) {
        if (list.size() == 0) {
            return null;
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setOrigin("盒马");
        billInfo.setAsset(this.f13703f);
        billInfo.setTime(System.currentTimeMillis());
        billInfo.setRemark("盒马购物");
        billInfo.setShopName("盒马购物");
        for (int i6 = 0; i6 < list.size(); i6++) {
            String str = list.get(i6);
            if (str.contains("支付成功：¥")) {
                String strReplace = str.replace("支付成功：¥", "").replace(",", "");
                if (h2.J(strReplace)) {
                    billInfo.setNumber(strReplace);
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(billInfo.getNumber())) {
            return null;
        }
        return billInfo;
    }
}
