package com.tntlikely.beecount.capture

data class CaptureApp(val packageName: String, val name: String, val adapter: String, val limited: Boolean = false) {
    fun asMap(): Map<String, Any> = mapOf("packageName" to packageName, "name" to name,
        "adapter" to adapter, "limited" to limited, "verification" to "experimental")
}

/** A registry entry is an experimental rule set, not a claim of device compatibility. */
object CaptureApps {
    val all = listOf(
        CaptureApp("com.eg.android.AlipayGphone", "支付宝", "alipay"),
        CaptureApp("com.tencent.mm", "微信", "wechat"),
        CaptureApp("com.unionpay", "云闪付", "unionpay"),
        CaptureApp("com.jd.jrapp", "京东金融", "jingdong"),
        CaptureApp("com.jingdong.app.mall", "京东商城", "jingdong"),
        CaptureApp("com.xunmeng.pinduoduo", "拼多多", "pinduoduo"),
        CaptureApp("com.sankuai.meituan", "美团", "meituan"),
        CaptureApp("com.dianping.v1", "大众点评", "meituan"),
        CaptureApp("com.sankuai.meituan.takeoutnew", "美团外卖", "meituan"),
        CaptureApp("com.alibaba.wireless", "阿里巴巴", "alibaba"),
        CaptureApp("com.taobao.idlefish", "闲鱼", "idlefish"),
        CaptureApp("com.wudaokou.hippo", "盒马", "hippo"),
        CaptureApp("me.ele", "饿了么", "eleme"),
        CaptureApp("com.huawei.wallet", "华为钱包", "huawei"),
        CaptureApp("com.ccb.longjiLife", "建行生活", "ccb"),
        CaptureApp("com.smile.gifmaker", "快手", "kuaishou"),
        CaptureApp("com.kuaishou.nebula", "快手极速版", "kuaishou"),
        CaptureApp("com.ss.android.ugc.aweme", "抖音", "douyin", true),
        CaptureApp("com.ss.android.ugc.livelite", "抖音商城", "douyin", true),
        CaptureApp("com.ss.android.ugc.aweme.lite", "抖音极速版", "douyin", true),
        CaptureApp("com.ss.android.yumme.video", "抖音精选", "douyin", true),
        CaptureApp("com.taobao.taobao", "淘宝", "taobao", true),
    )
    val byPackage = all.associateBy { it.packageName }
    val defaults = setOf("com.tencent.mm", "com.eg.android.AlipayGphone", "com.sankuai.meituan",
        "com.sankuai.meituan.takeoutnew", "com.dianping.v1")
}
