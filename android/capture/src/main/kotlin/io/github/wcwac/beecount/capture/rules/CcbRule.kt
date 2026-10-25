@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 1043c323e5e9c6940500ce5ffc38c9bfdd20a841f44b48636cfd01da30f533cc.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class CcbRule : RuleBase(), RuleCallback {
    private var f13724e: Boolean = false

    private var f13725f: String? = null

    private fun u(context: RuleContext?, list: MutableList<String?>?, str: String?): Unit {
        var billInfoT: RuleBill? = null

        if (((list == null) || ((list)!!.size <= 0))) {
            return
        }
        if (((this.a == 1) && ((list)!! as List<Any?>).contains("支付成功"))) {
            billInfoT = r(list)
        } else {
            var i6: Int = this.a
            if ((i6 == 2)) {
                billInfoT = s(list)
            } else {
                billInfoT = (if ((i6 == 3)) t(list) else null)
            }
        }
        if ((billInfoT != null)) {
            this.b = false
            this.f13724e = false
            (billInfoT)!!.setTagList(RuleTags.d(this.f13681d))
            RuleBridge.l(context, billInfoT, this)
        }
    }

    override fun dismiss(): Unit {

    }

    fun q(context: RuleContext?, str: String?, accessibilityNodeInfo: CaptureNode?): Unit {
        var listM: MutableList<String?>? = null
        if ((((str)!!.equals("com.ccb.longjiLife.MainActivity") || (str)!!.equals("com.ccb.cloudmerchant.wallet.newOne.transactions.TransactionsActivity2")) || (str)!!.equals("com.ccb.cloudmerchant.wallet.newOne.WalletActivity"))) {
            this.a = 0
            this.b = false
            this.f13724e = false
        } else if (((str)!!.equals("com.ccb.cloudmerchant.wallet.newOne.view.CodeActivity") || (str)!!.equals("com.ccb.cloudmerchant.view.WebViewActivity"))) {
            this.f13724e = true
        } else if ((str)!!.equals("com.ccb.cloudmerchant.wallet.newOne.view.CodeResultActivity")) {
            this.b = true
            this.a = 1
        } else if ((str)!!.equals("com.ccb.cloudmerchant.wallet.transactions.TransactionsDetailActivity")) {
            this.b = true
            this.f13724e = true
            this.a = 2
        }
        if ((!(this.f13724e) || (accessibilityNodeInfo == null))) {
            listM = null
        } else {
            listM = m(accessibilityNodeInfo)
            var iD: Int = d(listM, "优先选择此方式支付", true)
            if ((iD > 0)) {
                this.f13725f = (listM)!![(iD - 1)]
                if ((iD > 1)) {
                    var i6: Int = (iD - 2)
                    if (((listM)!![i6])!!.contains(("数字人民币")!!)) {
                        this.f13725f = (listM)!![i6]
                    }
                }
            }
            if ((a(listM, "支付成功", true) && a(listM, "订单号", true))) {
                this.b = true
                this.a = 3
            }
        }
        if ((!(this.b) || (accessibilityNodeInfo == null))) {
            return
        }
        u(context, listM, str)
    }

    fun r(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("建行生活")
        (billInfo)!!.setAsset(this.f13725f)
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if ((((str)!!.contains(("¥")!!) && TextUtils.isEmpty((billInfo)!!.getNumber())) && (i6 < ((list)!!.size - 1)))) {
                        var strReplace: String? = ((list)!![(i6 + 1)])!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                        }
                    } else if (((str)!!.equals("商户名称") && (i6 < ((list)!!.size - 1)))) {
                        var str2: String? = (list)!![(i6 + 1)]
                        (billInfo)!!.setRemark(str2)
                        (billInfo)!!.setShopName(str2)
                    }
                i6++
            }
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun s(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("建行生活")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((str)!!.equals("交易详情") && (i6 < ((list)!!.size - 1)))) {
                        var strReplace: String? = ((list)!![(i6 + 1)])!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(((strReplace)!!.replace(("-")!!, ("")!!))!!.replace(("+")!!, ("")!!))
                            (billInfo)!!.setIncome(!((strReplace)!!.contains(("-")!!)))
                        }
                    } else if (((str)!!.equals("商户名") && (i6 < ((list)!!.size - 1)))) {
                        var str2: String? = (list)!![(i6 + 1)]
                        (billInfo)!!.setRemark(str2)
                        (billInfo)!!.setShopName(str2)
                    } else if (((str)!!.equals("支付方式") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset((list)!![(i6 + 1)])
                    } else if (((str)!!.equals("交易时间") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setTime(Dates.X0((list)!![(i6 + 1)], "yyyy/MM/dd HH:mm:ss"))
                    }
                i6++
            }
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun t(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("建行生活")
        (billInfo)!!.setAsset("建设银行")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if ((str)!!.contains(("¥")!!)) {
                        var strReplace: String? = (((str)!!.replace((" ")!!, ("")!!))!!.replace(("¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                        }
                    } else if ((str)!!.contains(("商品名称")!!)) {
                        var strReplace2: String? = (str)!!.replace(("商品名称")!!, ("")!!)
                        (billInfo)!!.setRemark(strReplace2)
                        (billInfo)!!.setShopName(strReplace2)
                    }
                i6++
            }
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    companion object {
        private var f13720g: Int = 1

        private var f13721h: Int = 2

        private var f13722i: Int = 3

        private var f13723j: CcbRule? = null

        fun v(): CcbRule? {
            if ((f13723j == null)) {
                f13723j = CcbRule()
            }
            return f13723j
        }
    }
}
