@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 e2fd89f7a7e0c187288d580abc97d594b0c8add7baac7dff216a6b6798330e7d.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class AlibabaRule : RuleBase(), RuleCallback {
    private var f13668e: String? = null

    private var f13669f: String? = null

    private var f13670g: Boolean = false

    private fun s(context: RuleContext?, list: MutableList<String?>?, str: String?): Unit {

        if (((list == null) || ((list)!!.size <= 0))) {
            return
        }
        if ((a(list, "支付成功", true) || a(list, "付款成功", true))) {
            var billInfoR: RuleBill? = (if ((this.a == 1)) r(this.f13668e, this.f13669f) else null)
            if ((billInfoR != null)) {
                this.b = false
                this.f13668e = null
                this.f13669f = null
                this.f13670g = false
                (billInfoR)!!.setTagList(RuleTags.d(this.f13681d))
                RuleBridge.l(context, billInfoR, this)
            }
        }
    }

    override fun dismiss(): Unit {
        this.b = false
        this.f13668e = null
        this.f13669f = null
    }

    fun q(context: RuleContext?, str: String?, accessibilityNodeInfo: CaptureNode?): Unit {
        var listM: MutableList<String?>? = null
        if ((((str)!!.equals("com.alibaba.wireless.launch.home.V5HomeActivity") || (str)!!.equals("com.alibaba.wireless.search.aksearch.resultpage.SearchResultActivity")) || (str)!!.equals("com.alibaba.wireless.detail_flow.OfferDetailFlowActivity"))) {
            this.a = 0
            this.b = false
            this.f13670g = false
        } else if (((str)!!.equals("com.alibaba.wireless.container.windvane.AliWindvaneActivity") || (str)!!.equals("com.alibaba.wireless.popwindow.core.PopPageWindow"))) {
            this.f13670g = true
        }
        if ((!(this.f13670g) || (accessibilityNodeInfo == null))) {
            listM = null
        } else {
            listM = m(accessibilityNodeInfo)
            if ((((listM)!! as List<Any?>).contains("确认订单") || ((listM)!! as List<Any?>).contains("提交订单"))) {
                var iD: Int = d(listM, "有限公司", false)
                if (((iD >= 0) && (iD < ((listM)!!.size - 1)))) {
                    this.f13668e = (listM)!![(iD + 1)]
                } else if (((listM)!!.size > 7)) {
                    this.f13668e = (listM)!![7]
                }
                var iF: Int = f(listM, "￥", true)
                if ((iF < ((listM)!!.size - 2))) {
                    var str2: String? = ((listM)!![(iF + 1)] + (listM)!![(iF + 2)])
                    if (Amounts.J(str2)) {
                        this.f13669f = str2
                    }
                }
            } else if ((((listM)!! as List<Any?>).contains("立即支付") || ((listM)!! as List<Any?>).contains("0元下单，收货后付款"))) {
                var iD2: Int = d(listM, "立即支付", true)
                if ((iD2 == -(1))) {
                    iD2 = d(listM, "0元下单，收货后付款", true)
                }
                var str3: String? = (listM)!![(iD2 + 1)]
                if ((str3)!!.contains(("￥")!!)) {
                    var strReplace: String? = ((str3)!!.replace(("￥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                    if (Amounts.J(strReplace)) {
                        this.f13669f = strReplace
                    }
                }
            } else if ((((((listM)!! as List<Any?>).contains("支付成功") || ((listM)!! as List<Any?>).contains("付款成功"))) && ((listM)!! as List<Any?>).contains("查看订单"))) {
                this.b = true
                this.a = 1
            }
        }
        if ((!(this.b) || (accessibilityNodeInfo == null))) {
            return
        }
        s(context, listM, str)
    }

    fun r(str: String?, str2: String?): RuleBill? {
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("阿里巴巴")
        (billInfo)!!.setRemark("阿里巴巴购物")
        (billInfo)!!.setShopName("阿里巴巴")
        (billInfo)!!.setAsset("支付宝")
        if (!(TextUtils.isEmpty(str))) {
            (billInfo)!!.setRemark(str)
        }
        if (!(TextUtils.isEmpty(str2))) {
            (billInfo)!!.setNumber(str2)
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    companion object {
        private var f13666h: Int = 1

        private var f13667i: AlibabaRule? = null

        fun t(): AlibabaRule? {
            if ((f13667i == null)) {
                f13667i = AlibabaRule()
            }
            return f13667i
        }
    }
}
