@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 af8f2b23261797b8bae0fdc70d104e71234d3290b09d1d5dd868e791875ff32b.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class HemaRule : RuleBase(), RuleCallback {
    private var f13702e: Boolean = false

    private var f13703f: String? = null

    private fun s(context: RuleContext?, list: MutableList<String?>?, str: String?): Unit {

        if (((list == null) || ((list)!!.size <= 0))) {
            return
        }
        var billInfoR: RuleBill? = (if ((((this.a == 1) && ((list)!! as List<Any?>).contains("支付成功")))) r(list) else null)
        if ((billInfoR != null)) {
            this.b = false
            this.f13702e = false
            (billInfoR)!!.setTagList(RuleTags.d(this.f13681d))
            RuleBridge.l(context, billInfoR, this)
        }
    }

    override fun dismiss(): Unit {

    }

    fun q(context: RuleContext?, str: String?, accessibilityNodeInfo: CaptureNode?): Unit {
        var listM: MutableList<String?>? = null
        var iD: Int = 0
        if (((str)!!.equals("com.wudaokou.hippo.launcher.splash.SplashActivity") || (str)!!.equals("com.wudaokou.hippo.mine.MinePageActivity"))) {
            this.a = 0
            this.b = false
            this.f13702e = false
        } else if ((str)!!.equals("com.wudaokou.hippo.pay.PayCodeActivity")) {
            this.f13702e = true
            this.a = 1
        }
        if ((!(this.f13702e) || (accessibilityNodeInfo == null))) {
            listM = null
        } else {
            listM = m(accessibilityNodeInfo)
            if ((a(listM, "优先使用此付款方式", false) && (((d(listM, "优先使用此付款方式", false)).also { iD = it }) >= 0))) {
                this.f13703f = (((listM)!![iD])!!.replace(("优先使用此付款方式")!!, ("")!!))!!.replace(("\n")!!, ("")!!)
            }
            if ((a(listM, "支付成功", true) && a(listM, "完成", true))) {
                this.b = true
            }
        }
        if ((!(this.b) || (accessibilityNodeInfo == null))) {
            return
        }
        s(context, listM, str)
    }

    fun r(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("盒马")
        (billInfo)!!.setAsset(this.f13703f)
        (billInfo)!!.setTime(System.currentTimeMillis())
        (billInfo)!!.setRemark("盒马购物")
        (billInfo)!!.setShopName("盒马购物")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if ((str)!!.contains(("支付成功：¥")!!)) {
                        var strReplace: String? = ((str)!!.replace(("支付成功：¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                            break
                        }
                    }
                i6++
            }
        }
        if (TextUtils.isEmpty((billInfo)!!.getNumber())) {
            return null
        }
        return billInfo
    }

    companion object {
        private var f13700g: Int = 1

        private var f13701h: HemaRule? = null

        fun t(): HemaRule? {
            if ((f13701h == null)) {
                f13701h = HemaRule()
            }
            return f13701h
        }
    }
}
