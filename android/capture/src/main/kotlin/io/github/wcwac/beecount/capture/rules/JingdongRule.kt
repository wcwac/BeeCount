@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 0e8059f5cfa0f430b14c9f239e11286412be549525a449297ee6db730572286c.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class JingdongRule : RuleBase(), RuleCallback {
    private var f13714e: String? = null

    private var f13715f: String? = null

    private var f13716g: MutableList<String?>? = mutableListOf("支付方式", "创建时间")

    private var f13717h: MutableList<String?>? = mutableListOf("交易成功", "退款成功", "运输中", "查看订单状态")

    private var f13718i: Boolean = false

    private var f13719j: Boolean = false

    private fun w(context: RuleContext?, list: MutableList<String?>?, map: HashMap<String?, Boolean>?): Unit {
        var billInfoV: RuleBill? = null
        var iIndexOf: Int = 0


        if (((list == null) || (list)!!.isEmpty())) {
            return
        }
        var i6: Int = this.a
        var z5: Boolean = true
        var zContainsKey: Boolean = ((i6 == 7) || (i6 == 10))
        if ((!(zContainsKey) && (i6 == 8))) {
            zContainsKey = ((map)!! as Map<Any?, Boolean>).containsKey("支付成功")
        }
        if ((!(zContainsKey) && (this.a == 9))) {
            if ((!(((map)!! as Map<Any?, Boolean>).containsKey("支付成功")) && !(((map)!! as Map<Any?, Boolean>).containsKey("付款成功")))) {
                z5 = false
            }
            zContainsKey = z5
        }
        if ((((this.a == 8) && ((map)!! as Map<Any?, Boolean>).containsKey("¥")) && (((((list)!! as List<Any?>).indexOf("¥")).also { iIndexOf = it }) < ((list)!!.size - 2)))) {
            this.f13714e = (list)!![(iIndexOf + 2)]
        }
        if (zContainsKey) {

            var i7: Int = this.a
            if ((i7 == 7)) {
                billInfoV = q(list)
            } else if ((i7 == 10)) {
                billInfoV = r(list)
            } else if ((i7 == 8)) {
                billInfoV = t(list, this.f13714e)
            } else {
                billInfoV = (if ((i7 == 9)) v(list, this.f13714e, this.f13715f) else null)
            }
            if ((billInfoV != null)) {
                this.b = false
                this.f13714e = null
                this.f13715f = null
                this.f13719j = false
                this.f13718i = false
                (billInfoV)!!.setTagList(RuleTags.d(this.f13681d))
                RuleBridge.l(context, billInfoV, this)
            }
        }
    }

    override fun dismiss(): Unit {
        this.b = false
        this.f13714e = null
        this.f13715f = null
    }

    fun q(list: MutableList<String?>?): RuleBill? {
        if ((list)!!.isEmpty()) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("京东金融")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var strTrim: String? = ((list)!![i6])!!.trim()
                    if ((((((((strTrim)!!.equals("交易成功") || (strTrim)!!.equals("退款成功")) || (strTrim)!!.equals("运输中")) || (strTrim)!!.contains(("已退款")!!)) || (strTrim)!!.contains(("查看订单状态")!!))) && (i6 > 1))) {
                        var i7: Int = (i6 - 1)
                        var strReplace: String? = ((((list)!![i7])!!.replace(("+")!!, ("")!!))!!.replace(("-")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                            if (((list)!![i7])!!.contains(("+")!!)) {
                                (billInfo)!!.setIncome(true)
                            }
                        }
                        var str: String? = (list)!![(i6 - 2)]
                        (billInfo)!!.setRemark(str)
                        (billInfo)!!.setShopName(str)
                    } else if (((((((strTrim)!!.equals("交易说明：") || (strTrim)!!.equals("退款说明：")) || (strTrim)!!.equals("交易说明")) || (strTrim)!!.equals("退款说明"))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setRemark((list)!![(i6 + 1)])
                    } else if (((((((strTrim)!!.equals("支付方式：") || (strTrim)!!.equals("退款至：")) || (strTrim)!!.equals("支付方式")) || (strTrim)!!.equals("退款至"))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset((list)!![(i6 + 1)])
                    } else if (((((strTrim)!!.equals("创建时间：") || (strTrim)!!.equals("创建时间"))) && (i6 < ((list)!!.size - 1)))) {
                        var i8: Int = (i6 + 1)
                        (billInfo)!!.setTime(Dates.X0((list)!![i8], "yyyy-MM-dd HH:mm:ss"))
                        if ((((billInfo)!!.getTime() == -(1)) && (i6 < ((list)!!.size - 2)))) {
                            (billInfo)!!.setTime(Dates.X0((((list)!![i8] + " ") + (list)!![(i6 + 2)]), "yyyy-MM-dd HH:mm:ss"))
                        }
                        if (((billInfo)!!.getTime() == -(1))) {
                            (billInfo)!!.setTime(Dates.X0((list)!![i8], "yyyy-MM-dd"))
                        }
                    } else if (((strTrim)!!.equals("支付立减") && (i6 < ((list)!!.size - 1)))) {
                        var strReplace2: String? = ((((list)!![(i6 + 1)])!!.replace(("+")!!, ("")!!))!!.replace(("-")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace2)) {
                            (billInfo)!!.setDiscount(strReplace2)
                        }
                    } else if (((((strTrim)!!.equals("完成") || (strTrim)!!.equals("等待收货"))) && (i6 > 5))) {
                        var str2: String? = (list)!![(i6 - 1)]
                        if (((str2)!!.startsWith(("共")!!) && (str2)!!.endsWith(("件")!!))) {
                            (billInfo)!!.setRemark((list)!![(i6 - 2)])
                        }
                    } else if ((strTrim)!!.equals("服务详情")) {
                        var str3: String? = (list)!![(i6 + 1)]
                        if ((((str3)!!.startsWith(("共")!!) && (str3)!!.endsWith(("笔订单")!!)) && (i6 < ((list)!!.size - 2)))) {
                            var sb: StringBuilder? = StringBuilder()
                            run {
                                var i9: Int = (i6 + 2)
                                while ((i9 < ((list)!!.size - 1))) {
                                        (sb)!!.append((list)!![i9])
                                    i9++
                                }
                            }
                            (billInfo)!!.setRemark((sb)!!.toString())
                        }
                    }
                i6++
            }
        }
        if (TextUtils.isEmpty((billInfo)!!.getRemark())) {
            (billInfo)!!.setRemark((billInfo)!!.getShopName())
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun r(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("京东金融")
        (billInfo)!!.setTransfer(true)
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((str)!!.equals("还款成功") && (i6 > 1))) {
                        var strReplace: String? = ((((list)!![(i6 - 1)])!!.replace(("+")!!, ("")!!))!!.replace(("-")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                            var str2: String? = (list)!![(i6 - 2)]
                            (billInfo)!!.setRemark(str2)
                            (billInfo)!!.setShopName(str2)
                            if ((str2)!!.equals("京东白条")) {
                                (billInfo)!!.setToAsset("京东白条")
                            }
                        }
                    } else if (((((str)!!.equals("交易说明：") || (str)!!.equals("交易说明"))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setRemark((list)!![(i6 + 1)])
                    } else if (((((str)!!.equals("支付方式：") || (str)!!.equals("支付方式"))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setFromAsset((list)!![(i6 + 1)])
                    } else if (((((str)!!.equals("创建时间：") || (str)!!.equals("创建时间"))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setTime(Dates.X0((list)!![(i6 + 1)], "yyyy-MM-dd HH:mm:ss"))
                    } else if (((str)!!.equals("支付立减") && (i6 < ((list)!!.size - 1)))) {
                        var strReplace2: String? = (((list)!![(i6 + 1)])!!.replace(("+")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace2)) {
                            (billInfo)!!.setServiceCharge(Amounts.S(strReplace2))
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

    fun s(context: RuleContext?, str: String?, accessibilityNodeInfo: CaptureNode?): Unit {
        var map: HashMap<String?, Boolean>? = null
        var listG: MutableList<String?>? = null
        if ((str)!!.equals("com.jd.jrapp.bm.mainbox.main.MainActivity")) {
            this.a = 0
            this.f13715f = null
            this.b = false
            this.f13719j = false
        } else if ((str)!!.equals("com.jd.jrapp.bm.jrv8.JRCustomDyPageActivity")) {
            this.f13719j = true
        }
        if ((!(this.f13719j) || (accessibilityNodeInfo == null))) {
            map = null
        } else {
            listG = g(accessibilityNodeInfo)
            map = HashMap<String?, Boolean>()
            var it: Iterator<String?>? = (listG)!!.iterator()
            while ((it)!!.hasNext()) {
                (map)!!.put((((it)!!.next())!!.trim() as String?), (true as Boolean))
            }
            if (b(map, this.f13716g)) {
                if ((b(map, this.f13717h) || a(listG, "已退款", false))) {
                    this.a = 7
                    this.b = true
                } else if (((map)!! as Map<Any?, Boolean>).containsKey("还款成功")) {
                    this.a = 10
                    this.b = true
                }
            } else if ((((((map)!! as Map<Any?, Boolean>).containsKey("向京东商城支付") && ((map)!! as Map<Any?, Boolean>).containsKey("京东支付"))) || ((map)!! as Map<Any?, Boolean>).containsKey("支付成功"))) {
                this.a = 8
                this.b = true
            } else if (((listG)!!.size < 4)) {
                run {
                    var i6: Int = 0
                    while ((i6 < (listG)!!.size)) {
                            var strArrSplit: Array<String?>? = javaSplit(((listG)!![i6])!!, (" ")!!)
                            if (((strArrSplit)!!.size > 10)) {
                                (listG)!!.addAll(((strArrSplit)!!.toMutableList())!!)
                                this.a = 7
                                this.b = true
                                break
                            }
                        i6++
                    }
                }
            }
        }
        if ((!(this.b) || (accessibilityNodeInfo == null))) {
            return
        }
        if ((listG == null)) {
            listG = g(accessibilityNodeInfo)
            map = HashMap<String?, Boolean>()
            var it2: Iterator<String?>? = (listG)!!.iterator()
            while ((it2)!!.hasNext()) {
                (map)!!.put((((it2)!!.next())!!.trim() as String?), (true as Boolean))
            }
        }
        w(context, listG, map)
    }

    fun t(list: MutableList<String?>?, str: String?): RuleBill? {
        if ((list)!!.isEmpty()) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("京东支付")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str2: String? = (list)!![i6]
                    if (((str2)!!.equals("元") && (i6 > 0))) {
                        var strTrim: String? = (((list)!![(i6 - 1)])!!.replace((",")!!, ("")!!))!!.trim()
                        if (Amounts.J(strTrim)) {
                            (billInfo)!!.setNumber(strTrim)
                        }
                    } else if (((str2)!!.equals("收款方") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setRemark((list)!![(i6 + 1)])
                        (billInfo)!!.setShopName((billInfo)!!.getRemark())
                    }
                i6++
            }
        }
        if (!(TextUtils.isEmpty(str))) {
            (billInfo)!!.setAsset(str)
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun u(context: RuleContext?, str: String?, accessibilityNodeInfo: CaptureNode?): Unit {
        var map: HashMap<String?, Boolean>? = null
        var iD: Int = 0
        var iD2: Int = 0
        (str)!!.hashCode()
        var listG: MutableList<String?>? = null
        when (str) {
            "com.jd.lib.search.view.Activity.ProductListActivity", "com.jingdong.app.mall.MainFrameActivity", "com.jd.lib.productdetail.ProductDetailActivity", "com.jd.lib.cart.ShoppingCartNewActivity" -> {
                this.a = 0
                this.b = false
                this.f13718i = false
                this.f13719j = false
                this.f13715f = null
            }
            "com.jingdong.common.jdreactFramework.activities.JDReactNativeCommonActivity", "com.wangyin.payment.jdpaysdk.counter.ui.pay.CounterActivity", "com.jingdong.app.mall.bundle.cashierfinish.view.CashierUserContentCompletePopActivity", "com.jd.lib.settlement.fillorder.activity.PopupNewFillOrderActivity", "com.jd.lib.cashier.complete.view.CashierCompleteActivity", "com.jd.lib.jdpaysdk.JDPayActivity", "com.jd.lib.settlement.fillorder.activity.NewFillOrderActivity", "com.jd.lib.cashier.sdk.pay.view.CashierPayActivity" -> {
                this.f13718i = true
            }
            "com.jingdong.app.mall.WebActivity" -> {
                this.f13719j = true
            }
        }
        if (((accessibilityNodeInfo == null) || !(this.f13718i))) {
            map = null
        } else {
            listG = g(accessibilityNodeInfo)
            map = HashMap<String?, Boolean>()
            var it: Iterator<String?>? = (listG)!!.iterator()
            while ((it)!!.hasNext()) {
                (map)!!.put(((it)!!.next() as String?), (true as Boolean))
            }
            if ((((str)!!.equals("com.jd.lib.cashier.complete.view.CashierCompleteActivity") || (str)!!.equals("com.jd.lib.cashier.sdk.complete.view.CashierCompleteActivity")) || ((((((map)!! as Map<Any?, Boolean>).containsKey("付款成功") && a(listG, "实付", false))) || ((((map)!! as Map<Any?, Boolean>).containsKey("支付成功") && ((map)!! as Map<Any?, Boolean>).containsKey("查看订单"))))))) {
                this.a = 9
                this.b = true
            } else if (((map)!! as Map<Any?, Boolean>).containsKey("单单免费做公益")) {
                var iIndexOf: Int = ((listG)!! as List<Any?>).indexOf("单单免费做公益")
                if ((iIndexOf < ((listG)!!.size - 1))) {
                    var str2: String? = (listG)!![(iIndexOf + 1)]
                    this.f13714e = str2
                    if ((!(TextUtils.isEmpty(str2)) && (this.f13714e)!!.contains(("白条")!!))) {
                        this.f13714e = "白条"
                    }
                }
            } else if (((map)!! as Map<Any?, Boolean>).containsKey("单单做公益")) {
                var iIndexOf2: Int = ((listG)!! as List<Any?>).indexOf("单单做公益")
                if ((iIndexOf2 < ((listG)!!.size - 1))) {
                    var str3: String? = (listG)!![(iIndexOf2 + 1)]
                    this.f13714e = str3
                    if ((!(TextUtils.isEmpty(str3)) && (this.f13714e)!!.contains(("白条")!!))) {
                        this.f13714e = "白条"
                    }
                }
            } else if (((((map)!! as Map<Any?, Boolean>).containsKey("填写订单") || ((map)!! as Map<Any?, Boolean>).containsKey("确认订单")) || ((((map)!! as Map<Any?, Boolean>).containsKey("自己买") && ((map)!! as Map<Any?, Boolean>).containsKey("送朋友"))))) {
                if (((map)!! as Map<Any?, Boolean>).containsKey("商品名称")) {
                    var iIndexOf3: Int = ((listG)!! as List<Any?>).indexOf("商品名称")
                    if ((iIndexOf3 < ((listG)!!.size - 1))) {
                        this.f13715f = (listG)!![(iIndexOf3 + 1)]
                    }
                } else if ((TextUtils.isEmpty(this.f13715f) && (((d(listG, "¥", false)).also { iD = it }) > 0))) {
                    var str4: String? = (listG)!![(iD - 1)]
                    if (!(("送朋友")!!.equals(str4))) {
                        if ((iD > 1)) {
                            var str5: String? = (listG)!![(iD - 2)]
                            if (!(("京东自营")!!.equals(str5))) {
                                this.f13715f = ((str5 + " ") + str4)
                            }
                        } else {
                            this.f13715f = str4
                        }
                    }
                }
            } else if (((map)!! as Map<Any?, Boolean>).containsKey("列表")) {
                var iD3: Int = d(listG, "%用户选择", false)
                if ((iD3 > 0)) {
                    this.f13715f = (listG)!![(iD3 - 1)]
                } else if (((TextUtils.isEmpty(this.f13715f) && (((d(listG, "列表", true)).also { iD2 = it }) > 0)) && (iD2 < ((listG)!!.size - 1)))) {
                    this.f13715f = (listG)!![(iD2 + 1)]
                }
            } else if ((((map)!! as Map<Any?, Boolean>).containsKey("打包费") || ((map)!! as Map<Any?, Boolean>).containsKey("送达备注"))) {
                var iD4: Int = d(listG, "¥", false)
                if ((iD4 > 0)) {
                    this.f13715f = (listG)!![(iD4 - 1)]
                }
            }
        }
        if (((accessibilityNodeInfo != null) && this.f13719j)) {
            listG = g(accessibilityNodeInfo)
            map = HashMap<String?, Boolean>()
            var it2: Iterator<String?>? = (listG)!!.iterator()
            while ((it2)!!.hasNext()) {
                (map)!!.put(((it2)!!.next() as String?), (true as Boolean))
            }
            if (b(map, this.f13716g)) {
                if ((b(map, this.f13717h) || a(listG, "已退款", false))) {
                    this.a = 7
                    this.b = true
                } else if (((map)!! as Map<Any?, Boolean>).containsKey("还款成功")) {
                    this.a = 10
                    this.b = true
                }
            } else if (((listG)!!.size < 4)) {
                run {
                    var i6: Int = 0
                    while ((i6 < (listG)!!.size)) {
                            var strArrSplit: Array<String?>? = javaSplit(((listG)!![i6])!!, (" ")!!)
                            if (((strArrSplit)!!.size > 10)) {
                                (listG)!!.addAll(((strArrSplit)!!.toMutableList())!!)
                                this.a = 7
                                this.b = true
                            }
                        i6++
                    }
                }
            }
        }
        if ((!(this.b) || (accessibilityNodeInfo == null))) {
            return
        }
        if ((listG == null)) {
            listG = g(accessibilityNodeInfo)
            map = HashMap<String?, Boolean>()
            var it3: Iterator<String?>? = (listG)!!.iterator()
            while ((it3)!!.hasNext()) {
                (map)!!.put(((it3)!!.next() as String?), (true as Boolean))
            }
        }
        w(context, listG, map)
    }

    fun v(arg0: MutableList<String?>?, arg1: String?, arg2: String?): RuleBill? {
        var v: Array<Any?>? = arrayOfNulls<Any>(20)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![16] = this
        (v)!![17] = arg0
        (v)!![18] = arg1
        (v)!![19] = arg2
        while (true) {
            if ((pc == -(1))) {
                return ((dexR((result)!![1]) as RuleBill?))
            }
            if ((pc < 0x158)) pc = recovered_v_block0(v, result, pc) else pc = recovered_v_block1(v, result, pc)
        }
    }

    private fun recovered_v_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (v)!![0] = (v)!![16]
                        (v)!![1] = (v)!![17]
                        (result)!![0] = dexN((if (((((dexR((v)!![17]) as MutableList<String?>?)))!!.isEmpty())) 1 else 0))
                        (v)!![2] = (result)!![0]
                        (v)!![3] = dexN(0L)
                        pc = (if ((dexZero((v)!![2]))) 0xc else 0xb)
                        continue
                }
                0xb -> {
                        (result)!![1] = (v)!![3]
                        return -(1)
                }
                0xc -> {
                        (v)!![2] = DexReference()
                        ((((v)!![2] as DexReference?)))!!.value = RuleBill()
                        (v)!![4] = "京东商城"
                        (((dexR((v)!![2]) as RuleBill?)))!!.setOrigin(((dexR((v)!![4]) as String?)))
                        (v)!![5] = "元"
                        (v)!![6] = dexN(0L)
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.d(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![5]) as String?)), dexZ((v)!![6])))
                        (v)!![7] = (result)!![0]
                        (v)!![8] = "微信支付"
                        (v)!![9] = "白条付款"
                        (v)!![10] = "数字人民币"
                        (v)!![11] = ","
                        (v)!![12] = "¥"
                        (v)!![13] = dexN(1L)
                        (v)!![14] = ""
                        pc = (if (((dexI((v)!![7]) <= 0))) 0x9a else 0x2c)
                        continue
                }
                0x2c -> {
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![7])]
                        (v)!![15] = (result)!![0]
                        (v)!![15] = ((dexR((v)!![15]) as String?))
                        (v)!![3] = "实付"
                        (result)!![0] = (((dexR((v)!![15]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![11]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.trim()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x4d else 0x4a)
                        continue
                }
                0x4a -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![3]) as String?)))
                        pc = 0x4d
                        continue
                }
                0x4d -> {
                        (result)!![0] = dexN((((dexR((v)!![17]) as MutableList<String?>?)))!!.size)
                        (v)!![3] = (result)!![0]
                        (v)!![3] = dexN((dexI((v)!![3]) - dexI((v)!![13])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![3])))) 0x9a else 0x54)
                        continue
                }
                0x54 -> {
                        (v)!![7] = dexN((dexI((v)!![7]) + dexI((v)!![13])))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![7])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![5] = "已优惠"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![5]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x9a else 0x63)
                        continue
                }
                0x63 -> {
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (v)!![7] = "京东支付"
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![7]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (v)!![7] = "在线支付"
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![7]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (v)!![5] = "云闪付"
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![12]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![11]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.trim()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x9a else 0x97)
                        continue
                }
                0x97 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setDiscount(((dexR((v)!![3]) as String?)))
                        pc = 0x9a
                        continue
                }
                0x9a -> {
                        (v)!![3] = "支付成功"
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.d(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![3]) as String?)), dexZ((v)!![6])))
                        (v)!![3] = (result)!![0]
                        pc = (if (((dexI((v)!![3]) < 0))) 0x126 else 0xa2)
                        continue
                }
                0xa2 -> {
                        (result)!![0] = dexN((((dexR((v)!![17]) as MutableList<String?>?)))!!.size)
                        (v)!![5] = (result)!![0]
                        (v)!![5] = dexN((dexI((v)!![5]) - dexI((v)!![13])))
                        pc = (if (((dexI((v)!![3]) >= dexI((v)!![5])))) 0x126 else 0xa9)
                        continue
                }
                0xa9 -> {
                        (v)!![3] = dexN((dexI((v)!![3]) + dexI((v)!![13])))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![12]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x126 else 0xb6)
                        continue
                }
                0xb6 -> {
                        (v)!![3] = "，"
                        (result)!![0] = javaSplit((((dexR((v)!![1]) as String?)))!!, (((dexR((v)!![3]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (v)!![3] = (((dexR((v)!![1]) as Array<Any?>?)))!![dexI((v)!![6])]
                        (result)!![0] = dexN((((dexR((v)!![3]) as String?)))!!.indexOf((((dexR((v)!![12]) as String?)))!!))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.substring(dexI((v)!![5]))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![12]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![11]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.trim()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0xdb else 0xd8)
                        continue
                }
                0xd8 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![3]) as String?)))
                        pc = 0xdb
                        continue
                }
                0xdb -> {
                        (v)!![3] = (((dexR((v)!![1]) as Array<Any?>?)))!![dexI((v)!![6])]
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![9]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0xe6 else 0xe3)
                        continue
                }
                0xe3 -> {
                        (v)!![3] = "白条"
                        pc = 0xfd
                        continue
                }
                0xe6 -> {
                        (v)!![3] = (((dexR((v)!![1]) as Array<Any?>?)))!![dexI((v)!![6])]
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![10]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0xf0 else 0xee)
                        continue
                }
                0xee -> {
                        (v)!![3] = (v)!![10]
                        pc = 0xfd
                        continue
                }
                0xf0 -> {
                        (v)!![3] = (((dexR((v)!![1]) as Array<Any?>?)))!![dexI((v)!![6])]
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![8]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0xfb else 0xf8)
                        continue
                }
                0xf8 -> {
                        (v)!![3] = "微信钱包"
                        pc = 0xfd
                        continue
                }
                0xfb -> {
                        (v)!![3] = (v)!![18]
                        pc = 0xfd
                        continue
                }
                0xfd -> {
                        (v)!![5] = dexN(dexLength(dexR((v)!![1])))
                        pc = (if (((dexI((v)!![5]) <= dexI((v)!![13])))) 0x128 else 0x100)
                        continue
                }
                0x100 -> {
                        (v)!![5] = (((dexR((v)!![1]) as Array<Any?>?)))!![dexI((v)!![13])]
                        (v)!![6] = "共优惠"
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as String?)))!!.contains((((dexR((v)!![6]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x128 else 0x10a)
                        continue
                }
                0x10a -> {
                        (v)!![1] = (((dexR((v)!![1]) as Array<Any?>?)))!![dexI((v)!![13])]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![12]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![11]) as CharSequence?)))!!, (((dexR((v)!![14]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.trim()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x128 else 0x122)
                        continue
                }
                0x122 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setDiscount(((dexR((v)!![1]) as String?)))
                        pc = 0x128
                        continue
                }
                0x126 -> {
                        (v)!![3] = (v)!![18]
                        pc = 0x128
                        continue
                }
                0x128 -> {
                        (v)!![1] = "京东购物"
                        (((dexR((v)!![2]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (((dexR((v)!![2]) as RuleBill?)))!!.setShopName(((dexR((v)!![4]) as String?)))
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![3]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x139 else 0x136)
                        continue
                }
                0x136 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setAsset(((dexR((v)!![3]) as String?)))
                        pc = 0x139
                        continue
                }
                0x139 -> {
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![19]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x144 else 0x13f)
                        continue
                }
                0x13f -> {
                        (v)!![1] = (v)!![19]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x144
                        continue
                }
                0x144 -> {
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x159 else 0x14e)
                        continue
                }
                0x14e -> {
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getNumber()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x159 else 0x158)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_v_block1(v: Array<Any?>?, result: Array<Any?>?, pc: Int): Int {
        while (true) {
            when (pc) {
                0x158 -> {
                        (result)!![1] = (v)!![2]
                        return -(1)
                }
                0x159 -> {
                        (v)!![1] = dexN(0L)
                        (result)!![1] = (v)!![1]
                        return -(1)
                }
                else -> {
                    return pc
                }
            }
        }
    }

    companion object {
        private var f13709k: Int = 7

        private var f13710l: Int = 8

        private var f13711m: Int = 9

        private var f13712n: Int = 10

        private var f13713o: JingdongRule? = null

        fun x(): JingdongRule? {
            if ((f13713o == null)) {
                f13713o = JingdongRule()
            }
            return f13713o
        }
    }
}
