@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 070e9611005e3e1aebd5210e48127692619d171be37180242a47ef0e6b0187b0.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class IdlefishRule : RuleBase(), RuleCallback {
    private var f13706e: String? = null

    private var f13707f: String? = null

    private var f13708g: Boolean = false

    private fun q(context: RuleContext?, list: MutableList<String?>?, str: String?): Unit {

        if (((((list == null) || ((list)!!.size <= 0)) || !(a(list, "支付成功", false))) || !(a(list, "返回首页", false)))) {
            return
        }
        var billInfoR: RuleBill? = (if ((this.a == 1)) r(this.f13706e, this.f13707f) else null)
        if ((billInfoR != null)) {
            this.b = false
            this.f13706e = null
            this.f13707f = null
            this.f13708g = false
            (billInfoR)!!.setTagList(RuleTags.d(this.f13681d))
            RuleBridge.l(context, billInfoR, this)
        }
    }

    override fun dismiss(): Unit {
        this.b = false
        this.f13706e = null
        this.f13707f = null
    }

    fun r(str: String?, str2: String?): RuleBill? {
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("闲鱼")
        (billInfo)!!.setRemark("购物")
        (billInfo)!!.setShopName("闲鱼")
        (billInfo)!!.setAsset("支付宝")
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
        if (((str)!!.equals("com.taobao.idlefish.maincontainer.activity.MainActivity") || (str)!!.equals("com.taobao.idlefish.search_implement.SearchResultActivity"))) {
            this.a = 0
            this.b = false
            this.f13708g = false
        } else if ((str)!!.equals("com.idlefish.flutterbridge.flutterboost.boost.FishFlutterBoostActivity")) {
            this.f13708g = true
        }
        if ((!(this.f13708g) || (accessibilityNodeInfo == null))) {
            listM = null
        } else {
            listM = m(accessibilityNodeInfo)
            if ((d(listM, "现价", false) > 0)) {
                var iD: Int = d(listM, "现价", false)
                if ((iD > 0)) {
                    var str2: String? = (listM)!![iD]
                    this.f13707f = str2
                    var strReplace: String? = ((str2)!!.substring(((str2)!!.indexOf(("现价")!!) + 2)))!!.replace((" ")!!, ("")!!)
                    this.f13707f = strReplace
                    if ((strReplace)!!.contains((",")!!)) {
                        var str3: String? = this.f13707f
                        this.f13707f = (str3)!!.substring(0, (str3)!!.indexOf((",")!!))
                    }
                    if ((iD < ((listM)!!.size - 1))) {
                        var str4: String? = (listM)!![(iD + 1)]
                        this.f13706e = str4
                        if (!(TextUtils.isEmpty(str4))) {
                            this.f13706e = (javaSplit((this.f13706e)!!, ("\n")!!))!![0]
                        }
                    }
                }
            } else if (a(listM, "支付成功", false)) {
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
        private var f13704h: Int = 1

        private var f13705i: IdlefishRule? = null

        fun t(): IdlefishRule? {
            if ((f13705i == null)) {
                f13705i = IdlefishRule()
            }
            return f13705i
        }
    }
}
