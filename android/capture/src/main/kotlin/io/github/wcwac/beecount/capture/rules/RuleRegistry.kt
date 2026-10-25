package io.github.wcwac.beecount.capture.rules

/** Shared implementations, independent package sessions: no singleton state crosses applications. */
internal class RuleRegistry {
    private val handlers = mutableMapOf<String, RuleBase>()
    fun accept(context: RuleContext, pkg: String, page: String, source: CaptureNode?) {
        val rule = handlers.getOrPut(pkg) { when (pkg) {
            "com.eg.android.AlipayGphone" -> AlipayRule()
            "com.tencent.mm" -> WechatRule()
            "com.unionpay" -> UnionPayRule()
            "com.jd.jrapp", "com.jingdong.app.mall" -> JingdongRule()
            "com.xunmeng.pinduoduo" -> PinduoduoRule()
            "com.sankuai.meituan", "com.sankuai.meituan.takeoutnew", "com.dianping.v1" -> MeituanRule()
            "com.alibaba.wireless" -> AlibabaRule()
            "com.taobao.idlefish" -> IdlefishRule()
            "com.wudaokou.hippo" -> HemaRule()
            "me.ele" -> ElemeRule()
            "com.huawei.wallet" -> HuaweiRule()
            "com.ccb.longjiLife" -> CcbRule()
            "com.smile.gifmaker", "com.kuaishou.nebula" -> KuaishouRule()
            "com.ss.android.ugc.aweme", "com.ss.android.ugc.livelite", "com.ss.android.ugc.aweme.lite", "com.ss.android.yumme.video" -> DouyinRule()
            "com.taobao.taobao" -> TaobaoRule()
            else -> error("Unregistered capture package")
        } }
        rule.p(pkg)
        when (rule) {
            is AlipayRule -> rule.w(context, page, source)
            is WechatRule -> rule.t(context, page, source)
            is UnionPayRule -> rule.s(context, page, source)
            is JingdongRule -> if (pkg == "com.jd.jrapp") rule.s(context, page, source) else rule.u(context, page, source)
            is PinduoduoRule -> rule.s(context, page, source)
            is MeituanRule -> if (pkg == "com.dianping.v1") rule.r(context, page, source) else rule.t(context, page, source)
            is AlibabaRule -> rule.q(context, page, source)
            is IdlefishRule -> rule.s(context, page, source)
            is HemaRule -> rule.q(context, page, source)
            is ElemeRule -> rule.q(context, page, source)
            is HuaweiRule -> rule.q(context, page, source)
            is CcbRule -> rule.q(context, page, source)
            is KuaishouRule -> rule.s(context, page, source)
            is DouyinRule -> rule.s(context, page, source)
            is TaobaoRule -> rule.s(context, page, source)
        }
    }
    fun reset() { handlers.values.forEach { (it as? RuleCallback)?.dismiss() }; handlers.clear(); RuleBridge.f1838j = 0 }
}
