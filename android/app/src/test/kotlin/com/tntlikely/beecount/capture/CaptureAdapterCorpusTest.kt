package com.tntlikely.beecount.capture

import org.junit.Assert.*
import org.junit.Test

/** Synthetic page sequences from inspected layouts, NOT captured device fixtures.
 * Each family has an explicit expected amount/field meaning. The corpus includes
 * every registered client; a new package cannot get coverage just by registration.
 */
class CaptureAdapterCorpusTest {
    private data class Example(
        val packages: List<String>,
        val checkout: List<String> = emptyList(),
        val receipt: List<String>,
        val amount: Long,
        val merchant: String? = null,
        val remark: String? = null,
        val method: String? = null,
        val discount: Long? = null,
        val direction: String? = "expense",
        val issues: Set<String> = emptySet(),
    )

    private val examples = listOf(
        Example(listOf("com.tencent.mm"),
            receipt = listOf("支付成功", "收款方", "早餐店", "¥", "18", ".50"),
            amount = 1850, merchant = "早餐店"),
        Example(listOf("com.eg.android.AlipayGphone"),
            receipt = listOf("支付成功", "收款方", "便利店", "付款金额", "25.00", "付款方式", "储蓄卡"),
            amount = 2500, merchant = "便利店", method = "储蓄卡"),
        Example(listOf("com.unionpay"),
            receipt = listOf("交易成功", "商户名称", "食堂", "实付金额（元）", "33.50", "订单金额", "40.00"),
            amount = 3350, merchant = "食堂"),
        Example(listOf("com.jd.jrapp", "com.jingdong.app.mall"),
            receipt = listOf("咖啡店", "-16.00", "交易成功", "创建时间", "2026-10-21 08:00:00", "支付方式", "京东支付"),
            amount = 1600, merchant = "咖啡店", method = "京东支付"),
        Example(listOf("com.xunmeng.pinduoduo"),
            receipt = listOf("当前状态", "支付成功", "商品详情", "手套", "实付款", "￥8.80", "优惠", "￥2.00"),
            amount = 880, remark = "手套"),
        Example(listOf("com.sankuai.meituan", "com.sankuai.meituan.takeoutnew", "com.dianping.v1"),
            receipt = listOf("交易成功 ¥36.50", "支付宝已优惠¥3.00"),
            amount = 3650, method = "支付宝", discount = 300),
        Example(listOf("com.alibaba.wireless"),
            checkout = listOf("确认订单", "合计", "￥20.00", "商品名称", "螺丝"),
            receipt = listOf("支付成功", "查看订单"), amount = 2000, remark = "螺丝"),
        Example(listOf("com.taobao.idlefish"),
            checkout = listOf("现价1,234.56,免运费", "二手手机"),
            receipt = listOf("支付成功"), amount = 123456, remark = "二手手机"),
        Example(listOf("com.wudaokou.hippo"),
            receipt = listOf("支付成功：¥99.80", "付款方式", "盒马账户"),
            amount = 9980, method = "盒马账户"),
        Example(listOf("me.ele"),
            checkout = listOf("¥25.40", "提交订单", "已选", "支付宝"),
            receipt = listOf("下单成功，等待商家接单", "完成"), amount = 2540,
            method = "支付宝", direction = null, issues = setOf("PAYMENT_UNCONFIRMED")),
        Example(listOf("com.huawei.wallet"),
            receipt = listOf("+¥40.00", "交易成功", "商户甲", "付款银行", "银行卡"),
            amount = 4000, merchant = "商户甲", method = "银行卡", direction = "income"),
        Example(listOf("com.ccb.longjiLife"),
            receipt = listOf("交易详情", "-8.00", "商户名", "茶店", "支付方式", "建设银行", "交易时间", "2026-10-21 08:00:00"),
            amount = 800, merchant = "茶店", method = "建设银行", issues = setOf("PAYMENT_UNCONFIRMED")),
        Example(listOf("com.smile.gifmaker", "com.kuaishou.nebula"),
            receipt = listOf("购买成功", "实付", "¥", "2", ".99", "完成"), amount = 299),
        Example(listOf("com.ss.android.ugc.aweme", "com.ss.android.ugc.livelite",
            "com.ss.android.ugc.aweme.lite", "com.ss.android.yumme.video"),
            receipt = listOf("账单详情", "零食店", "¥12.60", "支付成功", "创建时间", "2026-10-21 08:00:00", "付款方式", "抖音支付"),
            amount = 1260, merchant = "零食店", method = "抖音支付"),
        Example(listOf("com.taobao.taobao"),
            checkout = listOf("349.00", "商品标题", "其他信息", "招商银行卡", "支付", "确认付款"),
            receipt = listOf("免密支付成功"), amount = 34900, method = "招商银行卡"),
    )

    private fun snapshot(packageName: String, texts: List<String>, elapsed: Long) = CaptureSnapshot(
        packageName, "fixture.Receipt", 1, 1_792_598_400_000L + elapsed, elapsed, texts.map { CaptureNode(it) },
    )

    @Test fun allClientVariantsPreserveTheDocumentedFieldMeanings() {
        val represented = examples.flatMap { it.packages }
        assertEquals(22, represented.size)
        assertEquals(CaptureApps.byPackage.keys, represented.toSet())
        val registry = CaptureAdapterRegistry()
        for (example in examples) for (packageName in example.packages) {
            val engine = CaptureEngine()
            if (example.checkout.isNotEmpty()) {
                assertTrue("$packageName checkout must retain a queue slot",
                    registry.mayAffectSession(snapshot(packageName, example.checkout, 1000)))
                assertNull("$packageName checkout is not a completed payment",
                    engine.accept(snapshot(packageName, example.checkout, 1000), 1))
            }
            assertTrue("$packageName receipt must retain a queue slot",
                registry.mayAffectSession(snapshot(packageName, example.receipt, 2000)))
            val result = engine.accept(snapshot(packageName, example.receipt, 2000), 1)
            assertNotNull("$packageName result", result)
            result!!
            assertEquals(packageName, result.sourcePackage)
            assertEquals("$packageName amount", example.amount, result.amountMinor)
            assertEquals("$packageName merchant", example.merchant, result.merchant)
            assertEquals("$packageName remark", example.remark, result.remark)
            assertEquals("$packageName method", example.method, result.paymentMethod)
            assertEquals("$packageName discount", example.discount, result.discountMinor)
            assertEquals("$packageName direction", example.direction, result.direction)
            assertTrue("$packageName issues", result.issues.containsAll(example.issues))
        }
    }

    @Test fun anInstructionContainingSuccessWordsDoesNotCreateAPayment() {
        for (app in CaptureApps.all) {
            assertNull(app.packageName, CaptureEngine().accept(snapshot(app.packageName,
                listOf("支付成功后请返回商家", "付款成功后可申请退款", "￥99.00"), 1000), 1))
        }
    }
}
