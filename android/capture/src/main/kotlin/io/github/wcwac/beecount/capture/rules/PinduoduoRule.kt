@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 6f843c7d06749c13c960e25368f590abec89ae8f619f309f7e997c215c3af7cf.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class PinduoduoRule : RuleBase(), RuleCallback {
    private var f13739e: String? = null

    private var f13740f: String? = null

    private var f13741g: String? = null

    private var f13742h: Boolean = false

    private var f13743i: Boolean = false

    private var f13744j: Boolean = false

    private var f13745k: Boolean = false

    private fun q(context: RuleContext?, list: MutableList<String?>?): Unit {
        var billInfoT: RuleBill? = null

        if (((list == null) || (list)!!.isEmpty())) {
            return
        }
        var i6: Int = this.a
        var zA: Boolean = (i6 == 2)
        if ((i6 == 1)) {
            zA = a(list, "拼单成功", false)
        }
        if ((!(zA) && (this.a == 1))) {
            zA = a(list, "支付成功", true)
        }
        if (!(zA)) {
            zA = a(list, "已支付", false)
        }
        if (zA) {

            var i7: Int = this.a
            if ((i7 == 1)) {
                billInfoT = r(list, this.f13739e, this.f13740f, this.f13741g)
            } else {
                billInfoT = (if ((i7 == 2)) t(list) else null)
            }
            if ((billInfoT != null)) {
                this.b = false
                this.f13742h = false
                this.f13743i = false
                this.f13744j = false
                this.f13739e = null
                this.f13740f = null
                this.f13745k = false
                (billInfoT)!!.setTagList(RuleTags.d(this.f13681d))
                RuleBridge.l(context, billInfoT, this)
            }
        }
    }

    override fun dismiss(): Unit {
        this.b = false
        this.f13739e = null
        this.f13740f = null
    }

    fun r(list: MutableList<String?>?, str: String?, str2: String?, str3: String?): RuleBill? {
        var iIndexOf: Int = 0
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("拼多多")
        (billInfo)!!.setRemark("拼多多")
        (billInfo)!!.setShopName("拼多多")
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
            if (((a(list, "已支付，多多支付已减", true) && (((((list)!! as List<Any?>).indexOf("已支付，多多支付已减")).also { iIndexOf = it }) >= 0)) && (iIndexOf < ((list)!!.size - 1)))) {
                var strReplace: String? = (((list)!![(iIndexOf + 1)])!!.replace(("¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                if (Amounts.J(strReplace)) {
                    (billInfo)!!.setDiscount(strReplace)
                    (billInfo)!!.setNumber(((((Amounts.S(str3) - Amounts.S(strReplace)))).toString() + ""))
                }
            }
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun s(arg0: RuleContext?, arg1: String?, arg2: CaptureNode?): Unit {
        var v: Array<Any?>? = arrayOfNulls<Any>(20)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![16] = this
        (v)!![17] = arg0
        (v)!![18] = arg1
        (v)!![19] = arg2
        while (true) {
            if ((pc == -(1))) {
                return
            }
            if ((pc < 0xed)) pc = recovered_s_block0(v, result, pc) else if ((pc < 0x29c)) pc = recovered_s_block1(v, result, pc) else pc = recovered_s_block2(v, result, pc)
        }
    }

    private fun recovered_s_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (v)!![0] = (v)!![16]
                        (v)!![1] = (v)!![18]
                        (v)!![2] = (v)!![19]
                        (v)!![3] = "com.xunmeng.pinduoduo.ui.activity.HomeActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        (v)!![4] = dexN(1L)
                        (v)!![5] = dexN(0L)
                        (v)!![6] = dexN(0L)
                        pc = (if ((dexZero((v)!![3]))) 0x29 else 0x11)
                        continue
                }
                0x11 -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.f13745k)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x25 else 0x15)
                        continue
                }
                0x15 -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![5])
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![5])
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13739e = ((dexR((v)!![6]) as String?))
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13740f = ((dexR((v)!![6]) as String?))
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13742h = dexZ((v)!![5])
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13743i = dexZ((v)!![5])
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13744j = dexZ((v)!![5])
                        pc = 0xed
                        continue
                }
                0x25 -> {
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13745k = dexZ((v)!![4])
                        pc = 0xed
                        continue
                }
                0x29 -> {
                        (v)!![3] = "com.xunmeng.pinduoduo.ui.activity.MainFrameActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x41 else 0x31)
                        continue
                }
                0x31 -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![5])
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![5])
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13739e = ((dexR((v)!![6]) as String?))
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13740f = ((dexR((v)!![6]) as String?))
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13742h = dexZ((v)!![5])
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13743i = dexZ((v)!![5])
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13744j = dexZ((v)!![5])
                        pc = 0xed
                        continue
                }
                0x41 -> {
                        (v)!![3] = "com.xunmeng.pinduoduo.sku_checkout.a"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xe9 else 0x49)
                        continue
                }
                0x49 -> {
                        (v)!![3] = "e.s.y.h9.q0"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xe9 else 0x51)
                        continue
                }
                0x51 -> {
                        pc = (if ((dexZero((v)!![2]))) 0x61 else 0x53)
                        continue
                }
                0x53 -> {
                        (v)!![3] = "com.xunmeng.pinduoduo:id/tv_select_sku"
                        (result)!![0] = (((dexR((v)!![2]) as CaptureNode?)))!!.findAccessibilityNodeInfosByViewId(((dexR((v)!![3]) as String?)))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.v(((dexR((v)!![3]) as MutableList<CaptureNode>?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x61 else 0x5f)
                        continue
                }
                0x5f -> {
                        pc = 0xe9
                        continue
                }
                0x61 -> {
                        (v)!![3] = "com.xunmeng.pinduoduo.activity.NewPageActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x6f else 0x69)
                        continue
                }
                0x69 -> {
                        pc = (if ((dexZero((v)!![2]))) 0x6f else 0x6b)
                        continue
                }
                0x6b -> {
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13743i = dexZ((v)!![4])
                        pc = 0xed
                        continue
                }
                0x6f -> {
                        (v)!![3] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.f13743i)) 1 else 0))
                        pc = (if ((dexZero((v)!![3]))) 0x7f else 0x73)
                        continue
                }
                0x73 -> {
                        (v)!![3] = "android.webkit.WebView"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x7f else 0x7b)
                        continue
                }
                0x7b -> {
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13744j = dexZ((v)!![4])
                        pc = 0xed
                        continue
                }
                0x7f -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.f13744j)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x94 else 0x83)
                        continue
                }
                0x83 -> {
                        pc = (if ((dexZero((v)!![2]))) 0x94 else 0x85)
                        continue
                }
                0x85 -> {
                        (v)!![1] = "账单详情"
                        (result)!![0] = (((dexR((v)!![2]) as CaptureNode?)))!!.findAccessibilityNodeInfosByText(((dexR((v)!![1]) as String?)))
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.v(((dexR((v)!![1]) as MutableList<CaptureNode>?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x94 else 0x91)
                        continue
                }
                0x91 -> {
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13742h = dexZ((v)!![4])
                        pc = 0xed
                        continue
                }
                0x94 -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.f13743i)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0xed else 0x98)
                        continue
                }
                0x98 -> {
                        pc = (if ((dexZero((v)!![2]))) 0xed else 0x9a)
                        continue
                }
                0x9a -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.f13742h)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0xed else 0x9e)
                        continue
                }
                0x9e -> {
                        (v)!![1] = "商品"
                        (result)!![0] = (((dexR((v)!![2]) as CaptureNode?)))!!.findAccessibilityNodeInfosByText(((dexR((v)!![1]) as String?)))
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.v(((dexR((v)!![1]) as MutableList<CaptureNode>?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0xc2 else 0xaa)
                        continue
                }
                0xaa -> {
                        (v)!![1] = "搜索"
                        (result)!![0] = (((dexR((v)!![2]) as CaptureNode?)))!!.findAccessibilityNodeInfosByText(((dexR((v)!![1]) as String?)))
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.v(((dexR((v)!![1]) as MutableList<CaptureNode>?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0xc2 else 0xb6)
                        continue
                }
                0xb6 -> {
                        (v)!![1] = "综合"
                        (result)!![0] = (((dexR((v)!![2]) as CaptureNode?)))!!.findAccessibilityNodeInfosByText(((dexR((v)!![1]) as String?)))
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.v(((dexR((v)!![1]) as MutableList<CaptureNode>?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0xe6 else 0xc2)
                        continue
                }
                0xc2 -> {
                        (v)!![1] = "我的订单"
                        (result)!![0] = (((dexR((v)!![2]) as CaptureNode?)))!!.findAccessibilityNodeInfosByText(((dexR((v)!![1]) as String?)))
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.v(((dexR((v)!![1]) as MutableList<CaptureNode>?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0xed else 0xce)
                        continue
                }
                0xce -> {
                        (v)!![1] = "全部"
                        (result)!![0] = (((dexR((v)!![2]) as CaptureNode?)))!!.findAccessibilityNodeInfosByText(((dexR((v)!![1]) as String?)))
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.v(((dexR((v)!![1]) as MutableList<CaptureNode>?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0xed else 0xda)
                        continue
                }
                0xda -> {
                        (v)!![1] = "待付款"
                        (result)!![0] = (((dexR((v)!![2]) as CaptureNode?)))!!.findAccessibilityNodeInfosByText(((dexR((v)!![1]) as String?)))
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.v(((dexR((v)!![1]) as MutableList<CaptureNode>?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0xed else 0xe6)
                        continue
                }
                0xe6 -> {
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13742h = dexZ((v)!![5])
                        pc = 0xed
                        continue
                }
                0xe9 -> {
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13742h = dexZ((v)!![4])
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13745k = dexZ((v)!![5])
                        pc = 0xed
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_s_block1(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0xed -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as PinduoduoRule?)))!!.f13742h)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x351 else 0xf1)
                        continue
                }
                0xf1 -> {
                        pc = (if ((dexZero((v)!![2]))) 0x351 else 0xf3)
                        continue
                }
                0xf3 -> {
                        (result)!![0] = (((dexR((v)!![0]) as RuleBase?)))!!.g(((dexR((v)!![2]) as CaptureNode?)))
                        (v)!![6] = (result)!![0]
                        (v)!![1] = (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13740f
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        (v)!![3] = "nodeInfo pdd can add : "
                        pc = (if ((!(dexZero((v)!![1])))) 0x14f else 0x101)
                        continue
                }
                0x101 -> {
                        (v)!![1] = "拼单成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![6]) as MutableList<String?>?)), ((dexR((v)!![1]) as String?)), dexZ((v)!![5])))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x119 else 0x109)
                        continue
                }
                0x109 -> {
                        (v)!![1] = "已支付"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![6]) as MutableList<String?>?)), ((dexR((v)!![1]) as String?)), dexZ((v)!![5])))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x119 else 0x111)
                        continue
                }
                0x111 -> {
                        (v)!![1] = "免拼成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![6]) as MutableList<String?>?)), ((dexR((v)!![1]) as String?)), dexZ((v)!![5])))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x14f else 0x119)
                        continue
                }
                0x119 -> {
                        (v)!![1] = "微信好友"
                        (result)!![0] = dexN((if ((((((dexR((v)!![6]) as MutableList<String?>?)))!! as List<Any?>).contains(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x131 else 0x121)
                        continue
                }
                0x121 -> {
                        (v)!![1] = "继续逛逛"
                        (result)!![0] = dexN((if ((((((dexR((v)!![6]) as MutableList<String?>?)))!! as List<Any?>).contains(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x131 else 0x129)
                        continue
                }
                0x129 -> {
                        (v)!![1] = "查看订单详情"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![6]) as MutableList<String?>?)), ((dexR((v)!![1]) as String?)), dexZ((v)!![5])))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x14f else 0x131)
                        continue
                }
                0x131 -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![4])
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![4])
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (v)!![3] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.b)) 1 else 0))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(dexZ((v)!![3]))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x351
                        continue
                }
                0x14f -> {
                        (v)!![1] = "当前状态"
                        (result)!![0] = dexN((if ((((((dexR((v)!![6]) as MutableList<String?>?)))!! as List<Any?>).contains(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        (v)!![7] = dexN(2L)
                        pc = (if ((dexZero((v)!![1]))) 0x17e else 0x158)
                        continue
                }
                0x158 -> {
                        (v)!![1] = "交易单号"
                        (result)!![0] = dexN((if ((((((dexR((v)!![6]) as MutableList<String?>?)))!! as List<Any?>).contains(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x17e else 0x160)
                        continue
                }
                0x160 -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![7])
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![4])
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (v)!![3] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.b)) 1 else 0))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(dexZ((v)!![3]))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x351
                        continue
                }
                0x17e -> {
                        (v)!![1] = (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13740f
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x1ac else 0x186)
                        continue
                }
                0x186 -> {
                        (v)!![1] = "支付成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![6]) as MutableList<String?>?)))!! as List<Any?>).contains(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x1ac else 0x18e)
                        continue
                }
                0x18e -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![4])
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![4])
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (v)!![3] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.b)) 1 else 0))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(dexZ((v)!![3]))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x351
                        continue
                }
                0x1ac -> {
                        (v)!![1] = "已选："
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![6]) as MutableList<String?>?)), ((dexR((v)!![1]) as String?)), dexZ((v)!![5])))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        (v)!![8] = "已选:"
                        pc = (if ((!(dexZero((v)!![3])))) 0x1bc else 0x1b6)
                        continue
                }
                0x1b6 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![6]) as MutableList<String?>?)), ((dexR((v)!![8]) as String?)), dexZ((v)!![5])))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x351 else 0x1bc)
                        continue
                }
                0x1bc -> {
                        (v)!![3] = "¥"
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.f(((dexR((v)!![6]) as MutableList<String?>?)), ((dexR((v)!![3]) as String?)), dexZ((v)!![5])))
                        (v)!![9] = (result)!![0]
                        (v)!![10] = "sssss"
                        (v)!![11] = ""
                        pc = (if (((dexI((v)!![9]) <= 0))) 0x263 else 0x1c8)
                        continue
                }
                0x1c8 -> {
                        (result)!![0] = (((dexR((v)!![6]) as MutableList<String?>?)))!![dexI((v)!![9])]
                        (v)!![12] = (result)!![0]
                        (v)!![12] = ((dexR((v)!![12]) as String?))
                        (result)!![0] = dexN((((dexR((v)!![12]) as String?)))!!.indexOf((((dexR((v)!![3]) as String?)))!!))
                        (v)!![13] = (result)!![0]
                        (v)!![14] = ","
                        (v)!![15] = " "
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![7])))) 0x225 else 0x1d8)
                        continue
                }
                0x1d8 -> {
                        (v)!![7] = dexN(5L)
                        pc = (if (((dexI((v)!![4]) >= dexI((v)!![7])))) 0x225 else 0x1db)
                        continue
                }
                0x1db -> {
                        (v)!![7] = dexN((dexI((v)!![9]) - dexI((v)!![4])))
                        pc = (if (((dexI((v)!![7]) < 0))) 0x225 else 0x1df)
                        continue
                }
                0x1df -> {
                        (result)!![0] = (((dexR((v)!![6]) as MutableList<String?>?)))!![dexI((v)!![7])]
                        (v)!![7] = (result)!![0]
                        (v)!![7] = ((dexR((v)!![7]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![7]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x222 else 0x1eb)
                        continue
                }
                0x1eb -> {
                        (result)!![0] = dexN((((dexR((v)!![7]) as String?)))!!.indexOf((((dexR((v)!![3]) as String?)))!!))
                        (v)!![13] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![7]) as String?)))!!.substring(dexI((v)!![13]))
                        (v)!![7] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![7]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![7] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![7]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![7] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![7]) as String?)))!!.replace((((dexR((v)!![14]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![7] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![7]) as String?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x222 else 0x205)
                        continue
                }
                0x205 -> {
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13741g = ((dexR((v)!![7]) as String?))
                        (v)!![3] = DexReference()
                        ((((v)!![3] as DexReference?)))!!.value = StringBuilder()
                        (v)!![4] = "num1:"
                        (result)!![0] = (((dexR((v)!![3]) as StringBuilder?)))!!.append(((dexR((v)!![4]) as String?)))
                        (v)!![4] = (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13741g
                        (result)!![0] = (((dexR((v)!![3]) as StringBuilder?)))!!.append(((dexR((v)!![4]) as String?)))
                        (result)!![0] = (((dexR((v)!![3]) as StringBuilder?)))!!.toString()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![10]) as Any?)), ((dexR((v)!![3]) as Any?)))
                        (v)!![3] = (result)!![0]

                        pc = 0x263
                        continue
                }
                0x222 -> {
                        (v)!![4] = dexN((dexI((v)!![4]) + (1)))
                        pc = 0x1d8
                        continue
                }
                0x225 -> {
                        (v)!![4] = " ¥"
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x263 else 0x22d)
                        continue
                }
                0x22d -> {
                        (result)!![0] = dexN((((dexR((v)!![12]) as String?)))!!.indexOf((((dexR((v)!![3]) as String?)))!!))
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.substring(dexI((v)!![4]))
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![14]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x263 else 0x247)
                        continue
                }
                0x247 -> {
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13741g = ((dexR((v)!![3]) as String?))
                        (v)!![3] = DexReference()
                        ((((v)!![3] as DexReference?)))!!.value = StringBuilder()
                        (v)!![4] = "num2:"
                        (result)!![0] = (((dexR((v)!![3]) as StringBuilder?)))!!.append(((dexR((v)!![4]) as String?)))
                        (v)!![4] = (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13741g
                        (result)!![0] = (((dexR((v)!![3]) as StringBuilder?)))!!.append(((dexR((v)!![4]) as String?)))
                        (result)!![0] = (((dexR((v)!![3]) as StringBuilder?)))!!.toString()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![10]) as Any?)), ((dexR((v)!![3]) as Any?)))
                        (v)!![3] = (result)!![0]

                        pc = 0x263
                        continue
                }
                0x263 -> {
                        (v)!![3] = "已选择："
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.d(((dexR((v)!![6]) as MutableList<String?>?)), ((dexR((v)!![3]) as String?)), dexZ((v)!![5])))
                        (v)!![4] = (result)!![0]
                        (v)!![7] = "remark:"
                        pc = (if (((dexI((v)!![4]) < 0))) 0x296 else 0x26d)
                        continue
                }
                0x26d -> {
                        (result)!![0] = (((dexR((v)!![6]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.trim()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13740f = ((dexR((v)!![1]) as String?))
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![7]) as String?)))
                        (v)!![3] = (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13740f
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![10]) as Any?)), ((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x2f3
                        continue
                }
                0x296 -> {
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.d(((dexR((v)!![6]) as MutableList<String?>?)), ((dexR((v)!![1]) as String?)), dexZ((v)!![5])))
                        (v)!![3] = (result)!![0]
                        pc = (if (((dexI((v)!![3]) < 0))) 0x2c5 else 0x29c)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_s_block2(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x29c -> {
                        (result)!![0] = (((dexR((v)!![6]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![1]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.trim()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13740f = ((dexR((v)!![1]) as String?))
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![7]) as String?)))
                        (v)!![3] = (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13740f
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![10]) as Any?)), ((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x2f3
                        continue
                }
                0x2c5 -> {
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.d(((dexR((v)!![6]) as MutableList<String?>?)), ((dexR((v)!![8]) as String?)), dexZ((v)!![5])))
                        (v)!![1] = (result)!![0]
                        pc = (if (((dexI((v)!![1]) < 0))) 0x2f3 else 0x2cb)
                        continue
                }
                0x2cb -> {
                        (result)!![0] = (((dexR((v)!![6]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.trim()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13740f = ((dexR((v)!![1]) as String?))
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![7]) as String?)))
                        (v)!![3] = (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13740f
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![10]) as Any?)), ((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x2f3
                        continue
                }
                0x2f3 -> {
                        (v)!![1] = "使用#"
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.d(((dexR((v)!![6]) as MutableList<String?>?)), ((dexR((v)!![1]) as String?)), dexZ((v)!![5])))
                        (v)!![3] = (result)!![0]
                        pc = (if (((dexI((v)!![3]) <= 0))) 0x351 else 0x2fb)
                        continue
                }
                0x2fb -> {
                        (result)!![0] = (((dexR((v)!![6]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "优先使用#"
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![1]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13739e = ((dexR((v)!![1]) as String?))
                        (v)!![3] = "，"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x321 else 0x315)
                        continue
                }
                0x315 -> {
                        (v)!![1] = (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13739e
                        (result)!![0] = dexN((((dexR((v)!![1]) as String?)))!!.indexOf((((dexR((v)!![3]) as String?)))!!))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.substring(dexI((v)!![5]), dexI((v)!![3]))
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13739e = ((dexR((v)!![1]) as String?))
                        pc = 0x321
                        continue
                }
                0x321 -> {
                        (v)!![1] = (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13739e
                        (v)!![3] = "最高减"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x337 else 0x32b)
                        continue
                }
                0x32b -> {
                        (v)!![1] = (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13739e
                        (result)!![0] = dexN((((dexR((v)!![1]) as String?)))!!.indexOf((((dexR((v)!![3]) as String?)))!!))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.substring(dexI((v)!![5]), dexI((v)!![3]))
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13739e = ((dexR((v)!![1]) as String?))
                        pc = 0x337
                        continue
                }
                0x337 -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "asset:"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (v)!![3] = (((dexR((v)!![0]) as PinduoduoRule?)))!!.f13739e
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![10]) as Any?)), ((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x351
                        continue
                }
                0x351 -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.b)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x35c else 0x355)
                        continue
                }
                0x355 -> {
                        pc = (if ((dexZero((v)!![2]))) 0x35c else 0x357)
                        continue
                }
                0x357 -> {
                        (v)!![1] = (v)!![17]
                        (((dexR((v)!![0]) as PinduoduoRule?)))!!.q(((dexR((v)!![1]) as RuleContext?)), ((dexR((v)!![6]) as MutableList<String?>?)))
                        pc = 0x35c
                        continue
                }
                0x35c -> {
                        return -(1)
                }
                else -> {
                    return pc
                }
            }
        }
    }

    fun t(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("拼多多")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((((str)!!.contains(("-")!!) || (str)!!.contains(("+")!!))) && (i6 > 0))) {
                        if ((str)!!.contains(("+")!!)) {
                            (billInfo)!!.setIncome(true)
                        }
                        var strReplace: String? = (((str)!!.replace(("+")!!, ("")!!))!!.replace(("-")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                            (billInfo)!!.setRemark((list)!![(i6 - 1)])
                            (billInfo)!!.setShopName((billInfo)!!.getRemark())
                        }
                    } else if ((((((str)!!.contains(("支付时间")!!) || (str)!!.contains(("退款时间")!!)) || (str)!!.contains(("到账时间")!!))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setTime(Dates.X0((list)!![(i6 + 1)], "yyyy-MM-dd HH:mm:ss"))
                    } else if ((((((str)!!.equals("商品详情") || (str)!!.equals("退款商品")) || (str)!!.equals("关联商品"))) && (i6 < ((list)!!.size - 1)))) {
                        if ((str)!!.equals("退款商品")) {
                            (billInfo)!!.setRemark(("退款-" + (list)!![(i6 + 1)]))
                        } else {
                            (billInfo)!!.setRemark((list)!![(i6 + 1)])
                        }
                    } else if (((((str)!!.equals("支付方式") || (str)!!.equals("退款至"))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset((list)!![(i6 + 1)])
                    } else if (((str)!!.equals("优惠") && (i6 < ((list)!!.size - 1)))) {
                        var strReplace2: String? = ((list)!![(i6 + 1)])!!.replace(("多多支付立减优惠¥")!!, ("")!!)
                        if (Amounts.J(strReplace2)) {
                            (billInfo)!!.setDiscount(strReplace2)
                        }
                    }
                i6++
            }
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun v(list: MutableList<CaptureNode>?): Boolean {
        return (if ((((list == null) || (list)!!.isEmpty()))) false else true)
    }

    companion object {
        private var f13736l: Int = 1

        private var f13737m: Int = 2

        private var f13738n: PinduoduoRule? = null

        fun u(): PinduoduoRule? {
            if ((f13738n == null)) {
                f13738n = PinduoduoRule()
            }
            return f13738n
        }
    }
}
