package io.github.wcwac.beecount.capture

data class CaptureApp(val packageName: String, val name: String, val capabilities: String)
object CaptureApps {
    val all = listOf(
        CaptureApp("com.eg.android.AlipayGphone", "支付宝", "扫码付款、转账、红包、提现、账单详情"),
        CaptureApp("com.tencent.mm", "微信", "扫码付款、转账、红包、提现、账单详情"),
        CaptureApp("com.unionpay", "云闪付", "扫码付款、转账、账单详情"),
        CaptureApp("com.jd.jrapp", "京东金融", "扫码付款、账单详情"),
        CaptureApp("com.jingdong.app.mall", "京东", "购物、钱包账单"),
        CaptureApp("com.xunmeng.pinduoduo", "拼多多", "购物"),
        CaptureApp("com.sankuai.meituan", "美团", "团购、外卖"),
        CaptureApp("com.dianping.v1", "大众点评", "团购、外卖"),
        CaptureApp("com.sankuai.meituan.takeoutnew", "美团外卖", "外卖"),
        CaptureApp("com.alibaba.wireless", "阿里巴巴", "购物"),
        CaptureApp("com.taobao.idlefish", "闲鱼", "购物"),
        CaptureApp("com.wudaokou.hippo", "盒马", "付款码付款"),
        CaptureApp("me.ele", "饿了么", "外卖"),
        CaptureApp("com.huawei.wallet", "华为钱包", "付款、账单详情"),
        CaptureApp("com.ccb.longjiLife", "建行生活", "付款、支付记录"),
        CaptureApp("com.smile.gifmaker", "快手", "直播购物"),
        CaptureApp("com.kuaishou.nebula", "快手极速版", "直播购物"),
        CaptureApp("com.ss.android.ugc.aweme", "抖音", "直播购物，部分页面受应用限制"),
        CaptureApp("com.ss.android.ugc.livelite", "抖音商城", "购物，部分页面受应用限制"),
        CaptureApp("com.ss.android.ugc.aweme.lite", "抖音极速版", "直播购物，部分页面受应用限制"),
        CaptureApp("com.ss.android.yumme.video", "抖音精选", "直播购物，部分页面受应用限制"),
        CaptureApp("com.taobao.taobao", "淘宝", "购物，部分页面受应用限制")
    )
    val packages: Set<String> = all.map { it.packageName }.toSet()
}
