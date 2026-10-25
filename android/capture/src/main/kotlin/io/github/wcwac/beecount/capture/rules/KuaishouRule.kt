@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 074a2b704c6803546435f7c7d255fc3f6d3ab06012276a63ce66b6a4b1f3b5f7.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class KuaishouRule : RuleBase(), RuleCallback {
    private var f13689e: String? = null

    private var f13690f: String? = null

    private var f13691g: String? = null

    private var f13692h: Boolean = false

    private fun q(context: RuleContext?, list: MutableList<String?>?, str: String?): Unit {

        if (((list == null) || (list)!!.isEmpty())) {
            return
        }
        if ((a(list, "支付成功", false) || a(list, "购买成功", false))) {
            var billInfoR: RuleBill? = (if ((this.a == 1)) r(this.f13689e, this.f13690f, this.f13691g) else null)
            if ((billInfoR != null)) {
                this.b = false
                this.f13689e = null
                this.f13691g = null
                this.f13690f = null
                this.f13692h = false
                (billInfoR)!!.setTagList(RuleTags.d(this.f13681d))
                RuleBridge.l(context, billInfoR, this)
            }
        }
    }

    override fun dismiss(): Unit {
        this.b = false
        this.f13689e = null
        this.f13690f = null
    }

    fun r(str: String?, str2: String?, str3: String?): RuleBill? {
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("快手")
        (billInfo)!!.setRemark("快手")
        (billInfo)!!.setShopName("快手")
        if (!(TextUtils.isEmpty(str3))) {
            (billInfo)!!.setAsset(str3)
        }
        if (!(TextUtils.isEmpty(str))) {
            (billInfo)!!.setRemark(str)
        }
        if ((!(TextUtils.isEmpty(str2)) && Amounts.J(str2))) {
            (billInfo)!!.setNumber(str2)
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun s(context: RuleContext?, str: String?, accessibilityNodeInfo: CaptureNode?): Unit {
        var listM: MutableList<String?>? = null
        if ((((((((str)!!.equals("com.yxcorp.gifshow.HomeActivity") || (str)!!.equals("com.kuaishou.merchant.pagedy.page.MerchantEraActivity")) || (str)!!.equals("com.kuaishou.merchant.container.base.MerchantKwaiDialog")) || (str)!!.equals("com.yxcorp.plugin.search.SearchActivity")) || (str)!!.equals("com.yxcorp.gifshow.detail.PhotoDetailActivity")) || (str)!!.equals("com.kuaishou.live.core.basic.activity.LivePlayActivity")) || (str)!!.equals("com.kuaishou.merchant.transaction.detail.contentdetail.MerchantContentDetailActivity"))) {
            this.a = 0
            this.b = false
            this.f13692h = false
            this.f13689e = null
            this.f13690f = null
            this.f13691g = null
        } else if ((((str)!!.equals("com.kuaishou.merchant.transaction.live.dynamic.page.MerchantPurchasePanelContainerV2Activity") || (str)!!.equals("com.kwai.kds.krn.api.page.KwaiRnActivity")) || (str)!!.equals("com.kuaishou.merchant.basic.widget.MerchantInterceptOutsideTouchDialog"))) {
            this.f13692h = true
        }
        if ((this.f13692h && (accessibilityNodeInfo != null))) {
            listM = m(accessibilityNodeInfo)
            run {
                var size: Int = ((listM)!!.size - 1)
                while ((size >= 0)) {
                        var str2: String? = (listM)!![size]
                        var strReplace: String? = ""
                        if ((str2)!!.startsWith(("立即支付")!!)) {
                            strReplace = (((((str2)!!.replace(("立即支付")!!, ("")!!))!!.replace(("¥")!!, ("")!!))!!.replace((" ")!!, ("")!!))!!.replace((" ")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        } else if ((str2)!!.startsWith(("0元下单")!!)) {
                            strReplace = (((((str2)!!.replace(("0元下单, 确认收货后付款")!!, ("")!!))!!.replace(("¥")!!, ("")!!))!!.replace((" ")!!, ("")!!))!!.replace((" ")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        } else if ((str2)!!.startsWith(("免密支付")!!)) {
                            strReplace = (((((str2)!!.replace(("免密支付")!!, ("")!!))!!.replace(("¥")!!, ("")!!))!!.replace((" ")!!, ("")!!))!!.replace((" ")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        } else if ((str2)!!.startsWith(("实付")!!)) {
                            strReplace = (((((str2)!!.replace(("实付")!!, ("")!!))!!.replace(("¥")!!, ("")!!))!!.replace((" ")!!, ("")!!))!!.replace((" ")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        }
                        if (Amounts.J(strReplace)) {
                            this.f13690f = strReplace
                        }
                        if ((TextUtils.isEmpty(this.f13689e) && ((((("关注")!!.equals(str2) || ("进店")!!.equals(str2))) && (size < ((listM)!!.size - 1)))))) {
                            this.f13689e = (listM)!![(size + 1)]
                        }
                        if (((TextUtils.isEmpty(this.f13689e) && (str2)!!.endsWith(("门店通用")!!)) && (size < ((listM)!!.size - 1)))) {
                            this.f13689e = (listM)!![(size + 1)]
                        }
                    size--
                }
            }

            if ((!(this.b) && a(listM, "支付成功", true))) {
                this.b = true
                this.a = 1
                var iD: Int = d(listM, "¥", true)
                if (((iD >= 0) && (iD < ((listM)!!.size - 2)))) {
                    var sb: StringBuilder? = StringBuilder()
                    var i6: Int = (iD + 1)
                    (sb)!!.append((listM)!![i6])
                    (sb)!!.append((listM)!![(iD + 2)])
                    var string: String? = (sb)!!.toString()
                    this.f13690f = string
                    if (!(Amounts.J(string))) {
                        this.f13690f = (listM)!![i6]
                    }
                    var str3: String? = (listM)!![d(listM, "付款", false)]
                    this.f13691g = str3
                    this.f13691g = (str3)!!.substring(0, (str3)!!.indexOf(("付款")!!))
                }
            } else if (((!(this.b) && ((listM)!! as List<Any?>).contains("购买成功")) && ((listM)!! as List<Any?>).contains("完成"))) {
                this.b = true
                this.a = 1
            }
        }
        if ((!(this.b) || (accessibilityNodeInfo == null))) {
            return
        }
        q(context, listM, str)
    }

    companion object {
        private var f13687i: Int = 1

        private var f13688j: KuaishouRule? = null

        fun t(): KuaishouRule? {
            if ((f13688j == null)) {
                f13688j = KuaishouRule()
            }
            return f13688j
        }
    }
}
