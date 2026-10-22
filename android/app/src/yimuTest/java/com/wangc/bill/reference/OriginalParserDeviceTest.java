package com.wangc.bill.reference;
import androidx.test.ext.junit.runners.AndroidJUnit4;
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
}
