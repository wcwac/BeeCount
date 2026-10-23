package com.wangc.bill.reference;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import com.wangc.bill.auto.action.*;
import com.wangc.bill.http.entity.BillInfo;
import com.wangc.bill.reference.capture.AutoApps;
@RunWith(AndroidJUnit4.class)
public final class OriginalParserDeviceTest {
    @Test public void allRecoveredPackagesAreUnique(){java.util.Set<String> names=new java.util.HashSet<>();for(AutoApps.App app:AutoApps.ALL)assertTrue(names.add(app.packageName));assertEquals(22,names.size());}
    @Test public void taobaoRetainsRecoveredFieldSemantics(){BillInfo bill=m.u().t("支付宝","实验商品","23.45");assertNotNull(bill);assertEquals("淘宝",bill.getOrigin());assertEquals("淘宝",bill.getShopName());assertEquals("实验商品",bill.getRemark());assertEquals("支付宝",bill.getAsset());assertEquals(2345,Money.positive(bill.getNumber(),"CNY"));}
    @Test public void meituanRetainsRecoveredFieldSemantics(){BillInfo bill=k.x().s("微信","实验商家","18.80");assertNotNull(bill);assertEquals("美团",bill.getOrigin());assertEquals("美团",bill.getShopName());assertEquals("实验商家",bill.getRemark());assertEquals(1880,Money.positive(bill.getNumber(),"CNY"));}
    @Test public void wechatSuccessPageParsesThroughRecoveredDexBranches(){BillInfo bill=new p().w(java.util.Arrays.asList("微信支付","支付成功","实验小卖部","￥12.30"),"零钱");assertNotNull(bill);assertEquals("实验小卖部",bill.getRemark());assertEquals("微信钱包",bill.getAsset());assertEquals(1230,Money.positive(bill.getNumber(),"CNY"));}
    @Test public void alipayBalanceAliasesNormalizeToAlipayInRecoveredParser(){
        // Original b.r, classes4 method 27983/code 0x4c149c, pc 0x96e..0x992.
        // Reading the balance label successfully still returns the canonical asset.
        for(String alias:new String[]{"余额","账户余额","可用余额"}){
            BillInfo bill=alipaySuccess("付款方式",alias);assertNotNull(bill);assertEquals("支付宝",bill.getAsset());
            assertEquals("实验支付宝商户",bill.getRemark());assertEquals(1860,Money.positive(bill.getNumber(),"CNY"));
        }
    }
    @Test public void alipayPaymentMethodNeighborIsRequiredAndRetainsItsChannel(){
        // b.r pc 0x42b..0x47e reads the next item, or skips one 帮助 item.
        // No 商品说明/收益发放 branch exists in these synthetic lists.
        BillInfo ordinary=alipaySuccess("付款方式","余额宝");assertNotNull(ordinary);assertEquals("余额宝",ordinary.getAsset());
        BillInfo help=alipaySuccess("付款方式","帮助","余额宝");assertNotNull(help);assertEquals("余额宝",help.getAsset());
        BillInfo noLabel=alipaySuccess("余额宝");assertNotNull(noLabel);assertNull("The standalone channel must not bypass the payment-method label",noLabel.getAsset());
    }
    private static BillInfo alipaySuccess(String... method){
        java.util.List<String> text=new java.util.ArrayList<>(java.util.Arrays.asList("支付成功","￥18.60","收款方","实验支付宝商户"));
        java.util.Collections.addAll(text,method);return new AlipaySuccessParser().r(ApplicationProvider.getApplicationContext(),text);
    }
    /** Select the exact recovered state normally selected by b.w's success-page gate. */
    private static final class AlipaySuccessParser extends b { AlipaySuccessParser(){this.a=2;} }
}
