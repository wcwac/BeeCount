@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 3efc32050424ef01d6d1e71c0a3b9d83afdaaa89cb2f2b9f93b4c9b05b882db6.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class ElemeRule : RuleBase(), RuleCallback {
    private var f13683e: String? = null

    private var f13684f: String? = null

    private var f13685g: String? = null

    private var f13686h: Boolean = false

    private fun s(context: RuleContext?, list: MutableList<String?>?): Unit {

        if (((list == null) || ((list)!!.size <= 0))) {
            return
        }

        var billInfoR: RuleBill? = r(this.f13683e, this.f13684f, this.f13685g)
        if ((billInfoR != null)) {
            this.b = false
            this.f13683e = null
            this.f13684f = null
            this.f13685g = null
            (billInfoR)!!.setTagList(RuleTags.d(this.f13681d))
            RuleBridge.l(context, billInfoR, this)
        }
    }

    override fun dismiss(): Unit {
        this.b = false
        this.f13683e = null
        this.f13684f = null
        this.f13685g = null
    }

    fun q(context: RuleContext?, str: String?, accessibilityNodeInfo: CaptureNode?): Unit {
        var listG: MutableList<String?>? = null
        if (((str)!!.equals("me.ele.application.ui.Launcher.LauncherActivity") || (str)!!.equals("me.ele.shopdetailv2.lmagex.ShopDetailV3Activity"))) {
            this.a = 0
            this.b = false
            this.f13686h = false
        } else if (((((str)!!.equals("me.ele.newbooking.checkout.entrypoint.WMCheckoutActivity") || (str)!!.equals("me.ele.newretail.submit.RetailSubmitActivity")) || (str)!!.equals("me.ele.shopdetailv2.ShopDetailV2Activity")) || (str)!!.equals("me.ele.android.emagex.container.EMagexPopupActivity"))) {
            this.f13686h = true
        }
        if ((!(this.f13686h) || (accessibilityNodeInfo == null))) {
            listG = null
        } else {
            listG = g(accessibilityNodeInfo)
            if ((((listG)!! as List<Any?>).contains("点餐") && ((listG)!! as List<Any?>).contains("评价"))) {
                this.f13684f = (listG)!![0]
            } else if (((listG)!! as List<Any?>).contains("提交订单")) {
                var iIndexOf: Int = ((listG)!! as List<Any?>).indexOf("提交订单")
                if ((iIndexOf > 0)) {
                    var strReplace: String? = (((listG)!![(iIndexOf - 1)])!!.replace((",")!!, ("")!!))!!.replace(("¥")!!, ("")!!)
                    if (Amounts.J(strReplace)) {
                        this.f13685g = strReplace
                    }
                }
                var iIndexOf2: Int = ((listG)!! as List<Any?>).indexOf("已选")
                if (((iIndexOf2 != -(1)) && (iIndexOf2 < ((listG)!!.size - 1)))) {
                    this.f13683e = (listG)!![(iIndexOf2 + 1)]
                }
            } else if ((((((((((listG)!! as List<Any?>).contains("下单成功，等待商家接单") || ((listG)!! as List<Any?>).contains("下单成功，商家已接单")) || ((listG)!! as List<Any?>).contains("下单成功，商家正在备餐")) || ((listG)!! as List<Any?>).contains("下单成功，商家备餐中")) || ((listG)!! as List<Any?>).contains("下单成功，商家备货完成"))) && ((listG)!! as List<Any?>).contains("完成")) && ((System.currentTimeMillis() - RuleBridge.f1838j) > 30000))) {
                this.b = true
            }
        }
        if ((!(this.b) || (accessibilityNodeInfo == null))) {
            return
        }
        s(context, listG)
    }

    fun r(str: String?, str2: String?, str3: String?): RuleBill? {
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("饿了么")
        (billInfo)!!.setRemark("饿了么")
        (billInfo)!!.setShopName("饿了么")
        if (TextUtils.isEmpty(str)) {
            (billInfo)!!.setAsset("支付宝")
        } else {
            (billInfo)!!.setAsset(str)
        }
        if (!(TextUtils.isEmpty(str2))) {
            (billInfo)!!.setRemark(str2)
        }
        if (!(TextUtils.isEmpty(str3))) {
            (billInfo)!!.setNumber(str3)
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    companion object {
        private var f13682i: ElemeRule? = null

        fun t(): ElemeRule? {
            if ((f13682i == null)) {
                f13682i = ElemeRule()
            }
            return f13682i
        }
    }
}
