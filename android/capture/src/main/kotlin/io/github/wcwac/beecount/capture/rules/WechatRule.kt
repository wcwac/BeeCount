@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 7065c033c395d2804031f518dfc22e06e634f185f0f65669fe0e6ec76bdae8b5.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class WechatRule : RuleBase(), RuleCallback {
    private var f13780j: Long = 0L

    var f13775e: String? = null

    var f13776f: String? = null

    private var f13777g: Boolean = false

    private var f13778h: Boolean = false

    private var f13779i: Boolean = false

    private var f13781k: Boolean = false

    private fun r(context: RuleContext?, list: MutableList<String?>?, map: HashMap<String?, Boolean>?): Unit {
        var z5: Boolean = false
        var billInfoU: RuleBill? = null
        var iIndexOf: Int = 0
        var iIndexOf2: Int = 0
        var iF: Int = 0


        if (((list == null) || (list)!!.isEmpty())) {
            return
        }
        if ((this.a == 1)) {
            z5 = (((((map)!! as Map<Any?, Boolean>).containsKey("付款成功") || ((map)!! as Map<Any?, Boolean>).containsKey("支付成功"))) && !(((map)!! as Map<Any?, Boolean>).containsKey("当前状态")))
            if ((((map)!! as Map<Any?, Boolean>).containsKey("付款方式") && ((((A())!!.f(list, "付款方式", true)).also { iF = it }) < ((list)!!.size - 2)))) {
                var str: String? = (list)!![(iF + 1)]
                this.f13775e = str
                if ((("更改")!!.equals(str) && (iF < ((list)!!.size - 3)))) {
                    var str2: String? = (list)!![(iF + 2)]
                    this.f13775e = str2
                    if ((("更改")!!.equals(str2) && (iF < ((list)!!.size - 4)))) {
                        this.f13775e = (list)!![(iF + 3)]
                    }
                }
            }
            if (((map)!! as Map<Any?, Boolean>).containsKey("优先使用此支付方式付款")) {
                var iIndexOf3: Int = (((list)!! as List<Any?>).indexOf("优先使用此支付方式付款") - 1)
                if ((iIndexOf3 >= 0)) {
                    this.f13775e = (list)!![iIndexOf3]
                }
            } else if (((map)!! as Map<Any?, Boolean>).containsKey("优先使用此付款方式")) {
                var iIndexOf4: Int = (((list)!! as List<Any?>).indexOf("优先使用此付款方式") - 1)
                if ((iIndexOf4 >= 0)) {
                    this.f13775e = (list)!![iIndexOf4]
                }
            } else if ((((map)!! as Map<Any?, Boolean>).containsKey("优先付款方式") && (((((list)!! as List<Any?>).indexOf("优先付款方式")).also { iIndexOf2 = it }) < ((list)!!.size - 2)))) {
                var str3: String? = (list)!![(iIndexOf2 + 1)]
                this.f13775e = str3
                if (("更改")!!.equals(str3)) {
                    this.f13775e = (list)!![(iIndexOf2 + 2)]
                }
            }
        } else {
            z5 = true
        }
        if ((this.a == 10)) {
            if (((map)!! as Map<Any?, Boolean>).containsKey("充值方式")) {
                var iIndexOf5: Int = ((list)!! as List<Any?>).indexOf("充值方式")
                if ((iIndexOf5 < ((list)!!.size - 1))) {
                    this.f13776f = (list)!![(iIndexOf5 + 1)]
                }
            } else if ((((map)!! as Map<Any?, Boolean>).containsKey("到账银行卡") && (((((list)!! as List<Any?>).indexOf("到账银行卡")).also { iIndexOf = it }) < ((list)!!.size - 1)))) {
                this.f13776f = (list)!![(iIndexOf + 1)]
            }
        }
        var z6: Boolean = (((((map)!! as Map<Any?, Boolean>).containsKey("已收款") || ((map)!! as Map<Any?, Boolean>).containsKey("资金待入账")) || a(list, "你已收款", false)) || a(list, "已收款", false))
        var zA: Boolean = a(list, "的红包", false)
        if (!(z5)) {
            if (z6) {

                var billInfoX: RuleBill? = x(list)
                if ((billInfoX != null)) {
                    this.b = false
                    (billInfoX)!!.setTagList(RuleTags.d(this.f13681d))
                    RuleBridge.l(context, billInfoX, this)
                    return
                }
                return
            }
            if (zA) {

                var billInfoS: RuleBill? = s(list)
                if ((billInfoS == null)) {
                    billInfoS = q(list)
                }
                if ((billInfoS != null)) {
                    this.b = false
                    (billInfoS)!!.setTagList(RuleTags.d(this.f13681d))
                    RuleBridge.l(context, billInfoS, this)
                    return
                }
                return
            }
            return
        }

        var i6: Int = this.a
        if ((i6 == 1)) {
            billInfoU = w(list, this.f13775e)
        } else if ((i6 == 3)) {
            billInfoU = v(list)
        } else if ((i6 == 10)) {
            billInfoU = y(list, this.f13776f)
        } else if ((i6 == 11)) {
            billInfoU = z(list)
        } else {
            billInfoU = (if ((i6 == 4)) u(list) else null)
        }
        if ((billInfoU != null)) {
            this.f13776f = null
            this.b = false
            this.f13781k = false
            this.f13779i = false

            (billInfoU)!!.setTagList(RuleTags.d(this.f13681d))
            RuleBridge.l(context, billInfoU, this)
        }
    }

    fun B(list: MutableList<CaptureNode>?): Boolean {
        return (if ((((list == null) || (list)!!.isEmpty()))) false else true)
    }

    override fun dismiss(): Unit {
        this.b = false
        this.f13781k = false
        this.f13779i = false
    }

    fun q(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("微信")
        (billInfo)!!.setAsset("微信")
        (billInfo)!!.setIncome(true)
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var strReplace: String? = (((list)!![i6])!!.replace(("元")!!, ("")!!))!!.replace((",")!!, ("")!!)
                    if (Amounts.J(strReplace)) {
                        (billInfo)!!.setNumber(strReplace)
                        if (((i6 < ((list)!!.size - 1)) && ((((list)!![i6])!!.contains(("元")!!) || ((list)!![(i6 + 1)])!!.contains(("元")!!))))) {
                            if ((i6 > 1)) {
                                var i7: Int = (i6 - 2)
                                if (((list)!![i7])!!.contains(("的红包")!!)) {
                                    (billInfo)!!.setRemark((list)!![i7])
                                    (billInfo)!!.setShopName((billInfo)!!.getRemark())
                                    return billInfo
                                }
                            }
                            if ((i6 > 0)) {
                                var i8: Int = (i6 - 1)
                                if (((list)!![i8])!!.contains(("的红包")!!)) {
                                    (billInfo)!!.setRemark((list)!![i8])
                                    (billInfo)!!.setShopName((billInfo)!!.getRemark())
                                    return billInfo
                                }
                            } else {
                                i6++
                                continue
                            }
                        }
                    }
                i6++
            }
        }
        return null
    }

    fun t(context: RuleContext?, page: String?, node: CaptureNode?): Unit {
        var texts: MutableList<String?>? = null
        var keys: HashMap<String?, Boolean>? = null
        if (((page)!!.equals("com.tencent.mm.ui.LauncherUI") && (node != null))) {
            this.a = 0
            this.b = false
            this.f13777g = false
            this.f13778h = false
        } else if (recognizedWechatPage(page)) {
            this.f13777g = true
            if ((page)!!.equals("com.tencent.mm.plugin.aa.ui.PaylistAAUI")) this.f13778h = true
        } else if ((page)!!.equals("com.tencent.mm.plugin.scanner.ui.BaseScanUI")) {
            this.f13781k = true
        } else if ((page)!!.equals("com.tencent.mm.plugin.appbrand.ui.AppBrandPluginUI")) {
            this.f13779i = true
        } else {
            var scanPay: Boolean = ((this.f13781k && (node != null)) && ((B((node)!!.findAccessibilityNodeInfosByText("支付成功")) || B((node)!!.findAccessibilityNodeInfosByText("付款方式")))))
            if (scanPay) {
                this.f13777g = true
            } else if ((this.f13779i && (node != null))) {
                texts = g(node)
                if ((((texts)!! as List<Any?>).contains("当前状态") && ((texts)!! as List<Any?>).contains("交易单号"))) this.f13777g = true
            } else if (((((((this.f13781k || this.f13779i)) && (node != null)) && B((node)!!.findAccessibilityNodeInfosByText("通讯录"))) && B((node)!!.findAccessibilityNodeInfosByText("发现"))) && B((node)!!.findAccessibilityNodeInfosByText("微信")))) {
                this.f13781k = false
                this.f13779i = false
                this.a = 0
                this.b = false
                this.f13777g = false
                this.f13778h = false
            }
        }
        if ((this.f13777g && (node != null))) {
            texts = m(node)
            keys = HashMap<String?, Boolean>()
            for (text in (texts)!!) (keys)!!.put((text as String?), (true as Boolean))
            var kind: Int = recoveredPageKind(page, texts, this.f13778h, (keys)!!.keys)
            if ((kind != 0)) {
                this.a = kind
                this.b = true
            }
            var priority: Boolean = (((keys)!! as Map<Any?, Boolean>).containsKey("二维码收款") && (((((keys)!! as Map<Any?, Boolean>).containsKey("优先使用此付款方式") || ((keys)!! as Map<Any?, Boolean>).containsKey("优先付款方式")) || ((keys)!! as Map<Any?, Boolean>).containsKey("优先使用此支付方式付款"))))
            var password: Boolean = ((((((keys)!! as Map<Any?, Boolean>).containsKey("使用密码") || ((keys)!! as Map<Any?, Boolean>).containsKey("使用指纹")) || ((keys)!! as Map<Any?, Boolean>).containsKey("请输入支付密码")) || (((((keys)!! as Map<Any?, Boolean>).containsKey("更改") && ((keys)!! as Map<Any?, Boolean>).containsKey("1")) && ((keys)!! as Map<Any?, Boolean>).containsKey("2")))) || ((((keys)!! as Map<Any?, Boolean>).containsKey("更改") && ((keys)!! as Map<Any?, Boolean>).containsKey("付款方式"))))
            if (((RuleSettings.i() && !(((keys)!! as Map<Any?, Boolean>).containsKey("支付成功"))) && ((priority || ((((keys)!! as Map<Any?, Boolean>).containsKey("付款方式") && password)))))) {
                this.f13780j = System.currentTimeMillis()
            }
        } else if (paymentWechatPage(page)) {
            this.a = 1
            this.b = true
        }
        if ((!(this.b) || (node == null))) return
        if ((texts == null)) {
            texts = g(node)
            keys = HashMap<String?, Boolean>()
            for (text in (texts)!!) (keys)!!.put((text as String?), (true as Boolean))
        }
        r(context, texts, keys)
    }

    fun u(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("微信")
        (billInfo)!!.setAsset("微信")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if ((str)!!.contains(("¥")!!)) {
                        if ((str)!!.contains(("已收到")!!)) {
                            (billInfo)!!.setIncome(true)
                            var strReplace: String? = ((str)!!.replace(("已收到¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                            if (Amounts.J(strReplace)) {
                                (billInfo)!!.setNumber(strReplace)
                                if ((i6 > 0)) {
                                    (billInfo)!!.setRemark((list)!![(i6 - 1)])
                                    (billInfo)!!.setShopName((billInfo)!!.getRemark())
                                }
                            }
                        } else if (((str)!!.contains(("已支付")!!) && !(((list)!! as List<Any?>).contains("你需支付")))) {
                            (billInfo)!!.setIncome(false)
                            var strReplace2: String? = ((str)!!.replace(("已支付¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                            if (Amounts.J(strReplace2)) {
                                (billInfo)!!.setNumber(strReplace2)
                                if ((i6 > 0)) {
                                    (billInfo)!!.setRemark((list)!![(i6 - 1)])
                                    (billInfo)!!.setShopName((billInfo)!!.getRemark())
                                }
                            }
                        } else if ((str)!!.contains(("收到")!!)) {
                            (billInfo)!!.setIncome(true)
                            var strReplace3: String? = ((str)!!.replace(("收到¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                            if (Amounts.J(strReplace3)) {
                                (billInfo)!!.setNumber(strReplace3)
                            }
                        }
                    } else if (((str)!!.contains(("已收齐")!!) && (i6 > 0))) {
                        (billInfo)!!.setRemark((list)!![(i6 - 1)])
                        (billInfo)!!.setShopName((billInfo)!!.getRemark())
                    }
                    if (!(TextUtils.isEmpty((billInfo)!!.getNumber()))) {
                        return billInfo
                    }
                i6++
            }
        }
        return null
    }

    fun v(list: MutableList<String?>?): RuleBill? {
        if ((list)!!.isEmpty()) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("微信")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((str)!!.contains((",支出")!!) && (str)!!.contains(("元")!!))) {
                        var strReplace: String? = ((str)!!.substring(((str)!!.indexOf((",支出")!!) + 3), ((str)!!.length - 1)))!!.replace((",")!!, ("")!!)
                        var strSubstring: String? = (str)!!.substring(0, (str)!!.indexOf((",支出")!!))


                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace))).toString() + ""))
                            (billInfo)!!.setRemark(strSubstring)
                            (billInfo)!!.setShopName(strSubstring)
                            (billInfo)!!.setIncome(false)
                        }
                    } else if (((str)!!.contains((",收入")!!) && (str)!!.contains(("元")!!))) {
                        var strReplace2: String? = ((str)!!.substring(((str)!!.indexOf((",收入")!!) + 3), ((str)!!.length - 1)))!!.replace((",")!!, ("")!!)
                        var strSubstring2: String? = (str)!!.substring(0, (str)!!.indexOf((",收入")!!))


                        if (Amounts.J(strReplace2)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace2))).toString() + ""))
                            (billInfo)!!.setRemark(strSubstring2)
                            (billInfo)!!.setShopName(strSubstring2)
                            (billInfo)!!.setIncome(true)
                        }
                    } else if (((str)!!.contains(("零钱通转出-到")!!) && (i6 < ((list)!!.size - 1)))) {
                        if (!((str)!!.contains(("支出¥")!!))) {
                            str = (list)!![(i6 + 1)]
                        }
                        if ((str)!!.contains(("支出¥")!!)) {
                            var strReplace3: String? = ((str)!!.substring(((str)!!.indexOf(("支出¥")!!) + 3)))!!.replace((",")!!, ("")!!)
                            if (Amounts.J(strReplace3)) {
                                (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace3))).toString() + ""))
                                (billInfo)!!.setRemark("零钱通转出")
                                (billInfo)!!.setTransfer(true)
                                (billInfo)!!.setFromAsset("零钱通")
                                (billInfo)!!.setToAsset("微信零钱")
                            }
                        }
                    } else if (((str)!!.contains(("信用卡还款-")!!) && (i6 < ((list)!!.size - 1)))) {
                        var str2: String? = (list)!![(i6 + 1)]
                        if (Amounts.J(str2)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(str2))).toString() + ""))
                            (billInfo)!!.setRemark("信用卡还款")
                            (billInfo)!!.setTransfer(true)
                            (billInfo)!!.setFromAsset("零钱通")
                            (billInfo)!!.setToAsset(((str)!!.replace(("信用卡还款-")!!, ("")!!))!!.replace(("还款")!!, ("")!!))
                        }
                    } else if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && (str)!!.contains(("收入¥")!!))) {
                        var strReplace4: String? = ((str)!!.substring(((str)!!.indexOf(("收入¥")!!) + 3)))!!.replace((",")!!, ("")!!)

                        if (Amounts.J(strReplace4)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace4))).toString() + ""))
                            (billInfo)!!.setIncome(true)
                            (billInfo)!!.setRemark((str)!!.substring(0, (str)!!.indexOf(("收入¥")!!)))
                            (billInfo)!!.setShopName((billInfo)!!.getRemark())
                        }
                    } else if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && ((((str)!!.contains(("-")!!) || (str)!!.contains(("+")!!)) || (str)!!.contains(("¥")!!))))) {
                        var strReplace5: String? = ((((str)!!.replace(("+")!!, ("")!!))!!.replace(("-")!!, ("")!!))!!.replace(("¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if ((Amounts.J(strReplace5) && (Amounts.S(strReplace5) > 0.0))) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace5))).toString() + ""))
                            if ((i6 > 0)) {
                                (billInfo)!!.setRemark((list)!![(i6 - 1)])
                                (billInfo)!!.setShopName((billInfo)!!.getRemark())
                            }
                            if ((str)!!.contains(("+")!!)) {
                                (billInfo)!!.setIncome(true)
                            }
                        }
                    } else if (((((((((str)!!.equals("支付时间") || (str)!!.equals("转账时间")) || (str)!!.equals("收款时间")) || (str)!!.equals("到账时间")) || (str)!!.equals("退款时间")) || (str)!!.equals("交易时间"))) && (i6 < ((list)!!.size - 1)))) {
                        if (((!((str)!!.equals("收款时间")) || (billInfo)!!.isIncome()) || ((billInfo)!!.getTime() == 0L))) {
                            var i7: Int = (i6 + 1)
                            if ((Dates.X0((list)!![i7], "yyyy年MM月dd日 HH:mm:ss") != -(1L))) {
                                (billInfo)!!.setTime(Dates.X0((list)!![i7], "yyyy年MM月dd日 HH:mm:ss"))
                            } else if ((Dates.X0((list)!![i7], "yyyy-MM-dd HH:mm:ss") != -(1L))) {
                                (billInfo)!!.setTime(Dates.X0((list)!![i7], "yyyy-MM-dd HH:mm:ss"))
                            } else if ((Dates.X0((list)!![i7], "yyyy年MM月dd日 HH点mm分ss秒") != -(1L))) {
                                (billInfo)!!.setTime(Dates.X0((list)!![i7], "yyyy年MM月dd日 HH点mm分ss秒"))
                            }
                        }
                    } else if ((str)!!.contains(("交易时间")!!)) {
                        if ((Dates.X0((str)!!.replace(("交易时间 ")!!, ("")!!), "yyyy年MM月dd日 HH点mm分ss秒") != -(1L))) {
                            (billInfo)!!.setTime(Dates.X0((str)!!.replace(("交易时间 ")!!, ("")!!), "yyyy年MM月dd日 HH点mm分ss秒"))
                        }
                    } else if (((str)!!.equals("优惠") && (i6 < ((list)!!.size - 1)))) {
                        var str3: String? = (list)!![(i6 + 1)]
                        if (((str3)!!.equals("支付成功") && (i6 < ((list)!!.size - 2)))) {
                            str3 = (list)!![(i6 + 2)]
                        }
                        if ((str3)!!.contains(("￥")!!)) {
                            var strSubstring3: String? = (str3)!!.substring((str3)!!.indexOf(("￥")!!))
                            if (!(TextUtils.isEmpty(strSubstring3))) {
                                var strReplace6: String? = ((strSubstring3)!!.replace((",")!!, ("")!!))!!.replace(("￥")!!, ("")!!)
                                if (Amounts.J(strReplace6)) {
                                    (billInfo)!!.setDiscount(strReplace6)
                                }
                            }
                        }
                    } else if ((((((str)!!.equals("支付方式") || (str)!!.equals("收款方式")) || (str)!!.equals("退款方式"))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset((list)!![(i6 + 1)])
                        if ((billInfo)!!.isTransfer()) {
                            if (("零钱")!!.equals((billInfo)!!.getAsset())) {
                                (billInfo)!!.setFromAsset("微信钱包")
                            } else {
                                (billInfo)!!.setFromAsset((billInfo)!!.getAsset())
                            }
                        }
                    } else if ((((((((((str)!!.equals("商品") || (str)!!.equals("商品名称")) || (str)!!.equals("付款备注")) || (str)!!.equals("转账说明")) || (str)!!.equals("付款方留言")) || (str)!!.equals("支付场景")) || (str)!!.equals("商户简称"))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setRemark((list)!![(i6 + 1)])
                    } else if (((str)!!.contains(("收款账号")!!) && (billInfo)!!.isTransfer())) {
                        (billInfo)!!.setToAsset(((str)!!.replace(("收款账号")!!, ("")!!))!!.trim())
                    } else if (((str)!!.equals("收款方备注") && (i6 < ((list)!!.size - 1)))) {
                        var str4: String? = (list)!![(i6 + 1)]
                        if ((!(("二维码收款")!!.equals(str4)) || TextUtils.isEmpty((billInfo)!!.getRemark()))) {
                            (billInfo)!!.setRemark(str4)
                        }
                        if ((("二维码收款")!!.equals(str4) && TextUtils.isEmpty((billInfo)!!.getAsset()))) {
                            (billInfo)!!.setAsset("微信")
                        }
                    } else if ((str)!!.equals("已存入经营账户")) {
                        (billInfo)!!.setAsset("微信经营账户")
                    } else if ((str)!!.equals("已存入零钱")) {
                        (billInfo)!!.setAsset("微信钱包")
                    } else if (((str)!!.equals("优惠") && (i6 < ((list)!!.size - 1)))) {
                        var str5: String? = (list)!![(i6 + 1)]
                        if ((str5)!!.contains(("¥")!!)) {
                            var strArrSplit: Array<String?>? = javaSplit((str5)!!, ("¥")!!)
                            if (((strArrSplit)!!.size > 0)) {
                                var strReplace7: String? = (((strArrSplit)!![((strArrSplit)!!.size - 1)])!!.replace("¥", ""))!!.replace((",")!!, ("")!!)
                                if (Amounts.J(strReplace7)) {
                                    (billInfo)!!.setDiscount(strReplace7)
                                }
                            }
                        }
                    }
                i6++
            }
        }
        if (("零钱")!!.equals((billInfo)!!.getAsset())) {
            (billInfo)!!.setAsset("微信钱包")
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun w(arg0: MutableList<String?>?, arg1: String?): RuleBill? {
        var v: Array<Any?>? = arrayOfNulls<Any>(16)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![13] = this
        (v)!![14] = arg0
        (v)!![15] = arg1
        while (true) {
            if ((pc == -(1))) {
                return ((dexR((result)!![1]) as RuleBill?))
            }
            if ((pc < 0x12a)) pc = recovered_w_block0(v, result, pc) else pc = recovered_w_block1(v, result, pc)
        }
    }

    private fun recovered_w_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as MutableList<String?>?)))!!.isEmpty())) 1 else 0))
                        (v)!![0] = (result)!![0]
                        (v)!![1] = dexN(0L)
                        pc = (if ((dexZero((v)!![0]))) 0x8 else 0x7)
                        continue
                }
                0x7 -> {
                        (result)!![1] = (v)!![1]
                        return -(1)
                }
                0x8 -> {
                        (v)!![0] = DexReference()
                        ((((v)!![0] as DexReference?)))!!.value = RuleBill()
                        (v)!![2] = "微信"
                        (((dexR((v)!![0]) as RuleBill?)))!!.setOrigin(((dexR((v)!![2]) as String?)))
                        (result)!![0] = dexN((if ((RuleSettings.i())) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((dexZero((v)!![2]))) 0x31 else 0x18)
                        continue
                }
                0x18 -> {
                        (v)!![2] = dexN((((dexR((v)!![13]) as WechatRule?)))!!.f13780j)
                        (result)!![0] = dexN(System.currentTimeMillis())
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((Dates.m0(dexL((v)!![2]), dexL((v)!![4])))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((dexZero((v)!![2]))) 0x2a else 0x24)
                        continue
                }
                0x24 -> {
                        (v)!![2] = dexN((((dexR((v)!![13]) as WechatRule?)))!!.f13780j)
                        (((dexR((v)!![0]) as RuleBill?)))!!.setTime(dexL((v)!![2]))
                        pc = 0x31
                        continue
                }
                0x2a -> {
                        (result)!![0] = dexN(System.currentTimeMillis())
                        (v)!![2] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setTime(dexL((v)!![2]))
                        pc = 0x31
                        continue
                }
                0x31 -> {
                        (v)!![2] = dexN(0L)
                        (v)!![3] = (v)!![2]
                        pc = 0x33
                        continue
                }
                0x33 -> {
                        (result)!![0] = dexN((((dexR((v)!![14]) as MutableList<String?>?)))!!.size)
                        (v)!![4] = (result)!![0]
                        pc = (if (((dexI((v)!![2]) >= dexI((v)!![4])))) 0x1d5 else 0x39)
                        continue
                }
                0x39 -> {
                        (result)!![0] = (((dexR((v)!![14]) as MutableList<String?>?)))!![dexI((v)!![2])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (v)!![5] = "￥"
                        (v)!![6] = ""
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![6]) as CharSequence?)))!!)
                        (v)!![7] = (result)!![0]
                        (v)!![8] = "¥"
                        (result)!![0] = (((dexR((v)!![7]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![6]) as CharSequence?)))!!)
                        (v)!![7] = (result)!![0]
                        (v)!![9] = ","
                        (result)!![0] = (((dexR((v)!![7]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![6]) as CharSequence?)))!!)
                        (v)!![7] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![4]) as Any?))))) 1 else 0))
                        (v)!![10] = (result)!![0]
                        (v)!![11] = dexN(1L)
                        pc = (if ((!(dexZero((v)!![10])))) 0x60 else 0x5a)
                        continue
                }
                0x5a -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as String?)))!!.equals(((dexR((v)!![4]) as Any?))))) 1 else 0))
                        (v)!![10] = (result)!![0]
                        pc = (if ((dexZero((v)!![10]))) 0x7a else 0x60)
                        continue
                }
                0x60 -> {
                        (result)!![0] = dexN((((dexR((v)!![14]) as MutableList<String?>?)))!!.size)
                        (v)!![10] = (result)!![0]
                        (v)!![10] = dexN((dexI((v)!![10]) - dexI((v)!![11])))
                        pc = (if (((dexI((v)!![2]) >= dexI((v)!![10])))) 0x7a else 0x67)
                        continue
                }
                0x67 -> {
                        (v)!![10] = dexN((dexI((v)!![2]) + (1)))
                        (result)!![0] = (((dexR((v)!![14]) as MutableList<String?>?)))!![dexI((v)!![10])]
                        (v)!![10] = (result)!![0]
                        (v)!![10] = ((dexR((v)!![10]) as String?))
                        (result)!![0] = (((dexR((v)!![10]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![6]) as CharSequence?)))!!)
                        (v)!![10] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![10]) as String?))))) 1 else 0))
                        (v)!![12] = (result)!![0]
                        pc = (if ((dexZero((v)!![12]))) 0x7a else 0x79)
                        continue
                }
                0x79 -> {
                        (v)!![7] = (v)!![10]
                        pc = 0x7a
                        continue
                }
                0x7a -> {
                        pc = (if ((!(dexZero((v)!![3])))) 0x86 else 0x7c)
                        continue
                }
                0x7c -> {
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getNumber()
                        (v)!![10] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![10]) as CharSequence?))))) 1 else 0))
                        (v)!![10] = (result)!![0]
                        pc = (if ((dexZero((v)!![10]))) 0xb1 else 0x86)
                        continue
                }
                0x86 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![7]) as String?))))) 1 else 0))
                        (v)!![10] = (result)!![0]
                        pc = (if ((dexZero((v)!![10]))) 0xb1 else 0x8c)
                        continue
                }
                0x8c -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.contains((((dexR((v)!![5]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![10] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![10])))) 0x98 else 0x92)
                        continue
                }
                0x92 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.contains((((dexR((v)!![8]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![10] = (result)!![0]
                        pc = (if ((dexZero((v)!![10]))) 0xb1 else 0x98)
                        continue
                }
                0x98 -> {
                        (((dexR((v)!![0]) as RuleBill?)))!!.setNumber(((dexR((v)!![7]) as String?)))
                        pc = (if ((dexZero((v)!![3]))) 0x1d1 else 0x9d)
                        continue
                }
                0x9d -> {
                        (v)!![4] = dexN((dexI((v)!![2]) + (-(1))))
                        (result)!![0] = (((dexR((v)!![14]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![0]) as RuleBill?)))!!.setRemark(((dexR((v)!![4]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getRemark()
                        (v)!![4] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setShopName(((dexR((v)!![4]) as String?)))
                        pc = 0x1d1
                        continue
                }
                0xb1 -> {
                        (v)!![7] = "优惠"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0xef else 0xb9)
                        continue
                }
                0xb9 -> {
                        (result)!![0] = dexN((((dexR((v)!![14]) as MutableList<String?>?)))!!.size)
                        (v)!![7] = (result)!![0]
                        (v)!![7] = dexN((dexI((v)!![7]) - dexI((v)!![11])))
                        pc = (if (((dexI((v)!![2]) >= dexI((v)!![7])))) 0xef else 0xc0)
                        continue
                }
                0xc0 -> {
                        (v)!![4] = dexN((dexI((v)!![2]) + (1)))
                        (result)!![0] = (((dexR((v)!![14]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.contains((((dexR((v)!![5]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x1d1 else 0xce)
                        continue
                }
                0xce -> {
                        (result)!![0] = dexN((((dexR((v)!![4]) as String?)))!!.indexOf((((dexR((v)!![5]) as String?)))!!))
                        (v)!![7] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.substring(dexI((v)!![7]))
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![7])))) 0x1d1 else 0xdc)
                        continue
                }
                0xdc -> {
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![6]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![6]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x1d1 else 0xea)
                        continue
                }
                0xea -> {
                        (((dexR((v)!![0]) as RuleBill?)))!!.setDiscount(((dexR((v)!![4]) as String?)))
                        pc = 0x1d1
                        continue
                }
                0xef -> {
                        (v)!![7] = "收款方"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x112 else 0xf7)
                        continue
                }
                0xf7 -> {
                        (result)!![0] = dexN((((dexR((v)!![14]) as MutableList<String?>?)))!!.size)
                        (v)!![7] = (result)!![0]
                        (v)!![7] = dexN((dexI((v)!![7]) - dexI((v)!![11])))
                        pc = (if (((dexI((v)!![2]) >= dexI((v)!![7])))) 0x112 else 0xfe)
                        continue
                }
                0xfe -> {
                        (v)!![4] = dexN((dexI((v)!![2]) + (1)))
                        (result)!![0] = (((dexR((v)!![14]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![0]) as RuleBill?)))!!.setRemark(((dexR((v)!![4]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getRemark()
                        (v)!![4] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setShopName(((dexR((v)!![4]) as String?)))
                        pc = 0x1d1
                        continue
                }
                0x112 -> {
                        (v)!![7] = "支付成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![7])))) 0x122 else 0x11a)
                        continue
                }
                0x11a -> {
                        (v)!![7] = "付款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x187 else 0x122)
                        continue
                }
                0x122 -> {
                        (result)!![0] = dexN((((dexR((v)!![14]) as MutableList<String?>?)))!!.size)
                        (v)!![7] = (result)!![0]
                        (v)!![7] = dexN((dexI((v)!![7]) + (-(2))))
                        pc = (if (((dexI((v)!![2]) >= dexI((v)!![7])))) 0x187 else 0x12a)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_w_block1(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x12a -> {
                        (v)!![7] = dexN((dexI((v)!![2]) + (1)))
                        (result)!![0] = (((dexR((v)!![14]) as MutableList<String?>?)))!![dexI((v)!![7])]
                        (v)!![10] = (result)!![0]
                        (v)!![10] = ((dexR((v)!![10]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![10]) as String?)))!!.contains((((dexR((v)!![8]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![10] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![10])))) 0x187 else 0x138)
                        continue
                }
                0x138 -> {
                        (result)!![0] = (((dexR((v)!![14]) as MutableList<String?>?)))!![dexI((v)!![7])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![0]) as RuleBill?)))!!.setRemark(((dexR((v)!![4]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getRemark()
                        (v)!![4] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setShopName(((dexR((v)!![4]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getRemark()
                        (v)!![4] = (result)!![0]
                        (v)!![5] = "待"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.startsWith((((dexR((v)!![5]) as String?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1d1 else 0x154)
                        continue
                }
                0x154 -> {
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getRemark()
                        (v)!![4] = (result)!![0]
                        (v)!![5] = "确认收款"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.endsWith((((dexR((v)!![5]) as String?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1d1 else 0x160)
                        continue
                }
                0x160 -> {
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = StringBuilder()
                        (v)!![5] = "转账给"
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![5]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getRemark()
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getRemark()
                        (v)!![6] = (result)!![0]
                        (result)!![0] = dexN((((dexR((v)!![6]) as String?)))!!.length)
                        (v)!![6] = (result)!![0]
                        (v)!![6] = dexN((dexI((v)!![6]) + (-(4))))
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.substring(dexI((v)!![11]), dexI((v)!![6]))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![5]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.toString()
                        (v)!![4] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setRemark(((dexR((v)!![4]) as String?)))
                        pc = 0x1d1
                        continue
                }
                0x187 -> {
                        (v)!![7] = "保存收款码"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x194 else 0x18f)
                        continue
                }
                0x18f -> {
                        (((dexR((v)!![0]) as RuleBill?)))!!.setIncome(dexZ((v)!![11]))
                        (v)!![3] = (v)!![11]
                        pc = 0x1d1
                        continue
                }
                0x194 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.contains((((dexR((v)!![5]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![7])))) 0x1a0 else 0x19a)
                        continue
                }
                0x19a -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.contains((((dexR((v)!![8]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x1d1 else 0x1a0)
                        continue
                }
                0x1a0 -> {
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![6]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![6]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![6]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getNumber()
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![5]) as CharSequence?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x1c0 else 0x1b6)
                        continue
                }
                0x1b6 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x1c0 else 0x1bc)
                        continue
                }
                0x1bc -> {
                        (((dexR((v)!![0]) as RuleBill?)))!!.setNumber(((dexR((v)!![4]) as String?)))
                        pc = 0x1d1
                        continue
                }
                0x1c0 -> {
                        (v)!![5] = "订单状态"
                        (result)!![0] = dexN((if ((((((dexR((v)!![14]) as MutableList<String?>?)))!! as List<Any?>).contains(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x1d1 else 0x1c8)
                        continue
                }
                0x1c8 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x1d1 else 0x1ce)
                        continue
                }
                0x1ce -> {
                        (((dexR((v)!![0]) as RuleBill?)))!!.setNumber(((dexR((v)!![4]) as String?)))
                        pc = 0x1d1
                        continue
                }
                0x1d1 -> {
                        (v)!![2] = dexN((dexI((v)!![2]) + (1)))
                        pc = 0x33
                        continue
                }
                0x1d5 -> {
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![15]) as CharSequence?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![14])))) 0x1de else 0x1db)
                        continue
                }
                0x1db -> {
                        (((dexR((v)!![0]) as RuleBill?)))!!.setAsset(((dexR((v)!![15]) as String?)))
                        pc = 0x1de
                        continue
                }
                0x1de -> {
                        (v)!![14] = "零钱"
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getAsset()
                        (v)!![2] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![2]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x1ef else 0x1ea)
                        continue
                }
                0x1ea -> {
                        (v)!![14] = "微信钱包"
                        (((dexR((v)!![0]) as RuleBill?)))!!.setAsset(((dexR((v)!![14]) as String?)))
                        pc = 0x1ef
                        continue
                }
                0x1ef -> {
                        (v)!![14] = DexReference()
                        ((((v)!![14] as DexReference?)))!!.value = StringBuilder()
                        (v)!![2] = "asset:"
                        (result)!![0] = (((dexR((v)!![14]) as StringBuilder?)))!!.append(((dexR((v)!![2]) as String?)))
                        (result)!![0] = (((dexR((v)!![14]) as StringBuilder?)))!!.append(((dexR((v)!![15]) as String?)))
                        (result)!![0] = (((dexR((v)!![14]) as StringBuilder?)))!!.toString()
                        (v)!![14] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![14]) as Any?)))
                        (v)!![14] = (result)!![0]

                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getRemark()
                        (v)!![14] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![14]) as CharSequence?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![14])))) 0x21c else 0x211)
                        continue
                }
                0x211 -> {
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getNumber()
                        (v)!![14] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![14]) as CharSequence?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![14])))) 0x21c else 0x21b)
                        continue
                }
                0x21b -> {
                        (result)!![1] = (v)!![0]
                        return -(1)
                }
                0x21c -> {
                        (result)!![1] = (v)!![1]
                        return -(1)
                }
                else -> {
                    return pc
                }
            }
        }
    }

    fun x(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("微信")
        (billInfo)!!.setAsset("微信")
        var z5: Boolean = false
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((((str)!!.contains(("已收款")!!) || (str)!!.contains(("你已收款")!!))) && (i6 < ((list)!!.size - 2)))) {
                        var strReplace: String? = (((((list)!![(i6 + 1)])!!.replace(("￥")!!, ("")!!))!!.replace(("¥")!!, ("")!!))!!.replace(("元")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                        }
                        if ((str)!!.contains(("你已收款")!!)) {
                            z5 = true
                        }
                    } else if (((z5 && (str)!!.equals("收款时间")) && (i6 < ((list)!!.size - 1)))) {
                        var jX0: Long = Dates.X0((list)!![(i6 + 1)], "yyyy年MM月dd日 HH:mm:ss")
                        if ((jX0 != -(1L))) {
                            (billInfo)!!.setTime(jX0)
                        }
                    } else if (((!(z5) && (str)!!.equals("转账时间")) && (i6 < ((list)!!.size - 1)))) {
                        var jX1: Long = Dates.X0((list)!![(i6 + 1)], "yyyy年MM月dd日 HH:mm:ss")
                        if ((jX1 != -(1L))) {
                            (billInfo)!!.setTime(jX1)
                        }
                    } else if (((str)!!.equals("转账说明") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setRemark((list)!![(i6 + 1)])
                    }
                i6++
            }
        }
        if (TextUtils.isEmpty((billInfo)!!.getNumber())) {
            return null
        }
        if (TextUtils.isEmpty((billInfo)!!.getRemark())) {
            if (z5) {
                (billInfo)!!.setRemark("微信收款")
            } else {
                (billInfo)!!.setRemark("微信转账")
            }
        }
        (billInfo)!!.setIncome(z5)
        (billInfo)!!.setShopName((billInfo)!!.getRemark())
        return billInfo
    }

    fun y(list: MutableList<String?>?, str: String?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setTransfer(true)
        (billInfo)!!.setOrigin("微信")
        if (a(list, "充值成功", true)) {
            (billInfo)!!.setFromAsset(str)
            (billInfo)!!.setToAsset("微信钱包")
        } else if (a(list, "已转入", true)) {
            (billInfo)!!.setFromAsset("微信钱包")
            (billInfo)!!.setToAsset("零钱通")
        } else if (a(list, "已转出", true)) {
            (billInfo)!!.setFromAsset("零钱通")
            (billInfo)!!.setToAsset("微信钱包")
        } else if (a(list, "零钱提现", true)) {
            (billInfo)!!.setFromAsset("微信钱包")
            (billInfo)!!.setToAsset(str)
        } else if (a(list, "转出金额", true)) {
            (billInfo)!!.setFromAsset("零钱通")
            (billInfo)!!.setToAsset(str)
        }
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str2: String? = (list)!![i6]
                    if (((((str2)!!.equals("充值成功") || (str2)!!.equals("提现金额"))) && (i6 < ((list)!!.size - 1)))) {
                        var strReplace: String? = ((((list)!![(i6 + 1)])!!.replace(("¥")!!, ("")!!))!!.replace(("￥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && Amounts.J(strReplace))) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace))).toString() + ""))
                        }
                    } else if (((str2)!!.equals("已转入") && (i6 < ((list)!!.size - 2)))) {
                        var strReplace2: String? = ((((list)!![(i6 + 2)])!!.replace(("¥")!!, ("")!!))!!.replace(("￥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && Amounts.J(strReplace2))) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace2))).toString() + ""))
                        }
                    } else if (((str2)!!.equals("已转出") && (i6 < ((list)!!.size - 2)))) {
                        var strReplace3: String? = ((((list)!![(i6 + 2)])!!.replace(("¥")!!, ("")!!))!!.replace(("￥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && Amounts.J(strReplace3))) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace3))).toString() + ""))
                        }
                    } else if (((str2)!!.equals("转出金额") && (i6 < ((list)!!.size - 1)))) {
                        var strReplace4: String? = ((((list)!![(i6 + 1)])!!.replace(("¥")!!, ("")!!))!!.replace(("￥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && Amounts.J(strReplace4))) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace4))).toString() + ""))
                        }
                    } else if (((str2)!!.equals("服务费") && (i6 < ((list)!!.size - 1)))) {
                        var strReplace5: String? = ((((list)!![(i6 + 1)])!!.replace(("¥")!!, ("")!!))!!.replace(("￥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace5)) {
                            (billInfo)!!.setServiceCharge(Math.abs(Amounts.S(strReplace5)))
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

    fun z(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setTransfer(true)
        (billInfo)!!.setOrigin("微信")
        if (a(list, "零钱提现", false)) {
            (billInfo)!!.setFromAsset("微信钱包")
        } else if (a(list, "零钱充值", false)) {
            (billInfo)!!.setToAsset("微信钱包")
        } else if (a(list, "转入零钱通", false)) {
            (billInfo)!!.setToAsset("零钱通")
        }
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((str)!!.equals("提现金额") && (i6 < ((list)!!.size - 1)))) {
                        var strReplace: String? = ((((list)!![(i6 + 1)])!!.replace(("¥")!!, ("")!!))!!.replace(("￥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && Amounts.J(strReplace))) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace))).toString() + ""))
                        }
                    } else if (((str)!!.equals("服务费") && (i6 < ((list)!!.size - 1)))) {
                        var strReplace2: String? = ((((list)!![(i6 + 1)])!!.replace(("¥")!!, ("")!!))!!.replace(("￥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace2)) {
                            (billInfo)!!.setServiceCharge(Math.abs(Amounts.S(strReplace2)))
                        }
                    } else if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && (str)!!.contains(("收入¥")!!))) {
                        var strReplace3: String? = ((str)!!.substring(((str)!!.indexOf(("收入¥")!!) + 3)))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace3)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace3))).toString() + ""))
                            (billInfo)!!.setIncome(true)
                            (billInfo)!!.setRemark((str)!!.substring(0, (str)!!.indexOf(("收入¥")!!)))
                            (billInfo)!!.setShopName((billInfo)!!.getRemark())
                        }
                    } else if (((TextUtils.isEmpty((billInfo)!!.getNumber()) && (i6 < ((list)!!.size - 1))) && ("当前状态")!!.equals((list)!![(i6 + 1)]))) {
                        var strReplace4: String? = (str)!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace4)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace4))).toString() + ""))
                            if ((i6 > 0)) {
                                (billInfo)!!.setRemark((list)!![(i6 - 1)])
                                (billInfo)!!.setShopName((billInfo)!!.getRemark())
                            }
                        }
                    } else if ((str)!!.contains(("转入方式")!!)) {
                        (billInfo)!!.setFromAsset(((str)!!.replace(("转入方式")!!, ("")!!))!!.trim())
                    } else if (((str)!!.equals("提现银行") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setToAsset((list)!![(i6 + 1)])
                    } else if (((str)!!.equals("支付方式") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setFromAsset((list)!![(i6 + 1)])
                    } else if (((((((str)!!.equals("申请时间") || (str)!!.equals("到账时间")) || (str)!!.equals("充值时间")) || (str)!!.equals("支付时间"))) && (i6 < ((list)!!.size - 1)))) {
                        var i7: Int = (i6 + 1)
                        (billInfo)!!.setTime(Dates.X0((list)!![i7], "yyyy年MM月dd日 HH:mm:ss"))
                        if (((billInfo)!!.getTime() == -(1L))) {
                            (billInfo)!!.setTime(Dates.X0((list)!![i7], "yyyy-MM-dd HH:mm:ss"))
                        }
                    } else if ((str)!!.contains(("交易时间")!!)) {
                        if ((Dates.X0((str)!!.replace(("交易时间 ")!!, ("")!!), "yyyy年MM月dd日 HH点mm分ss秒") != -(1L))) {
                            (billInfo)!!.setTime(Dates.X0((str)!!.replace(("交易时间 ")!!, ("")!!), "yyyy年MM月dd日 HH点mm分ss秒"))
                        }
                    } else if (((str)!!.contains((",")!!) && (str)!!.contains(("元")!!))) {
                        var strSubstring: String? = (str)!!.substring(((str)!!.lastIndexOf((",")!!) + 1), ((str)!!.length - 1))
                        if (Amounts.J(strSubstring)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strSubstring))).toString() + ""))
                        }
                    } else if (((str)!!.contains(("零钱充值-")!!) && (i6 < ((list)!!.size - 1)))) {
                        var strReplace5: String? = (((list)!![(i6 + 1)])!!.replace(("元")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && Amounts.J(strReplace5))) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace5))).toString() + ""))
                        }
                    } else if (((str)!!.contains(("转入零钱通-")!!) && (i6 < ((list)!!.size - 1)))) {
                        var strReplace6: String? = (((list)!![(i6 + 1)])!!.replace(("元")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && Amounts.J(strReplace6))) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace6))).toString() + ""))
                        }
                    }
                i6++
            }
        }
        if (("零钱")!!.equals((billInfo)!!.getAsset())) {
            (billInfo)!!.setAsset("微信钱包")
        }
        if (TextUtils.isEmpty((billInfo)!!.getNumber())) {
            return null
        }
        return billInfo
    }

    companion object {
        private var f13769l: Int = 1

        private var f13770m: Int = 3

        private var f13771n: Int = 4

        private var f13772o: Int = 10

        private var f13773p: Int = 11

        private var f13774q: WechatRule? = null

        fun A(): WechatRule? {
            if ((f13774q == null)) {
                f13774q = WechatRule()
            }
            return f13774q
        }

        fun s(list: MutableList<String?>?): RuleBill? {
            var iIndexOf: Int = 0
            var billInfo: RuleBill? = RuleBill()
            (billInfo)!!.setOrigin("微信")
            run {
                var i6: Int = 0
                while ((i6 < (list)!!.size)) {
                        var str: String? = (list)!![i6]
                        if ((str)!!.contains(("红包金额")!!)) {
                            var iIndexOf2: Int = (str)!!.indexOf(("元")!!)
                            if ((iIndexOf2 > 4)) {
                                var strReplace: String? = (((str)!!.substring(0, iIndexOf2))!!.replace(("红包金额")!!, ("")!!))!!.replace((",")!!, ("")!!)
                                if (Amounts.J(strReplace)) {
                                    (billInfo)!!.setNumber(strReplace)
                                    (billInfo)!!.setRemark("发送微信红包")
                                    (billInfo)!!.setShopName((billInfo)!!.getRemark())
                                    return billInfo
                                }
                            } else {
                                i6++
                                continue
                            }
                        } else if ((str)!!.contains(("个红包共")!!)) {
                            var iIndexOf3: Int = (str)!!.indexOf(("元")!!)
                            if ((iIndexOf3 > 4)) {
                                var strReplace2: String? = ((str)!!.substring(((str)!!.indexOf(("个红包共")!!) + 4), iIndexOf3))!!.replace((",")!!, ("")!!)
                                if (Amounts.J(strReplace2)) {
                                    (billInfo)!!.setNumber(strReplace2)
                                    (billInfo)!!.setRemark("发送微信红包")
                                    (billInfo)!!.setShopName((billInfo)!!.getRemark())
                                    return billInfo
                                }
                            } else {
                                i6++
                                continue
                            }
                        } else if ((str)!!.contains(("人已领取")!!)) {
                            var iIndexOf4: Int = (str)!!.indexOf(("元")!!)
                            if ((iIndexOf4 > 4)) {
                                var strReplace3: String? = ((str)!!.substring(((str)!!.indexOf(("人已领取")!!) + 6), iIndexOf4))!!.replace((",")!!, ("")!!)
                                if (Amounts.J(strReplace3)) {
                                    (billInfo)!!.setNumber(strReplace3)
                                    (billInfo)!!.setRemark("发送微信红包")
                                    (billInfo)!!.setShopName((billInfo)!!.getRemark())
                                    return billInfo
                                }
                            } else {
                                i6++
                                continue
                            }
                        } else if (((str)!!.contains(("个，共")!!) && ((((str)!!.indexOf(("元")!!)).also { iIndexOf = it }) > 4))) {
                            var strReplace4: String? = ((str)!!.substring(((str)!!.lastIndexOf(("/")!!) + 1), iIndexOf))!!.replace((",")!!, ("")!!)
                            if (Amounts.J(strReplace4)) {
                                (billInfo)!!.setNumber(strReplace4)
                                (billInfo)!!.setRemark("发送微信红包")
                                (billInfo)!!.setShopName((billInfo)!!.getRemark())
                                return billInfo
                            }
                        }
                    i6++
                }
            }
            return null
        }

        private fun recognizedWechatPage(page: String?): Boolean {
            return (paymentWechatPage(page) || (when (page) { "com.tencent.mm.plugin.webview.ui.tools.MMWebViewUI", "com.tencent.mm.plugin.webview.ui.tools.WebViewUI", "com.tencent.mm.framework.app.UIPageFragmentActivity", "com.tencent.mm.plugin.wallet_core.ui.WalletOrderInfoNewUI", "com.tencent.mm.plugin.lite.ui.WxaLiteAppTransparentLiteUI", "com.tencent.mm.plugin.aa.ui.PaylistAAUI", "com.tencent.mm.plugin.appbrand.ui.AppBrandUI00" -> true; else -> false }))
        }

        private fun paymentWechatPage(page: String?): Boolean {
            return (when (page) { "com.tencent.mm.plugin.remittance.ui.RemittanceBusiUI", "com.tencent.mm.plugin.remittance.ui.RemittanceUI", "com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI", "com.tencent.mm.plugin.wallet_index.ui.WalletBrandUI", "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyPrepareUI", "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI", "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNewDetailUI", "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI", "com.tencent.mm.plugin.wallet_index.ui.OrderHandlerUI", "com.tencent.mm.plugin.remittance.ui.RemittanceDetailUI" -> true; else -> false })
        }

        fun recoveredPageKind(page: String?, texts: MutableList<String?>?, groupMode: Boolean): Int {
            return recoveredPageKind(page, texts, groupMode, HashSet<String?>((texts)!!))
        }

        private fun recoveredPageKind(page: String?, texts: MutableList<String?>?, groupMode: Boolean, keys: MutableSet<String?>?): Int {
            var special: Boolean = ((((keys)!!.contains("当前状态") && ((((keys)!!.contains("提现单号") || (keys)!!.contains("充值完成")) || (keys)!!.contains("还款成功"))))) || (((keys)!!.contains("交易单号") && containsRecovered(texts, "转入零钱通"))))
            if (((special && !((keys)!!.contains("查看账单详情"))) && !((keys)!!.contains("常见问题")))) return 11
            var detail: Boolean = (((((((keys)!!.contains("账单详情") || (((keys)!!.contains("商品名称") && (keys)!!.contains("交易状态")))) || (((keys)!!.contains("当前状态") && (((((keys)!!.contains("转账单号") || (keys)!!.contains("交易单号")) || (keys)!!.contains("付款单号")) || (keys)!!.contains("收款单号")))))) || (((keys)!!.contains("退款状态") && (keys)!!.contains("退款单号")))) || (((keys)!!.contains("付款商家") && (keys)!!.contains("付款单号")))) || (((keys)!!.contains("详情") && containsRecovered(texts, "零钱通余额")))) || (((keys)!!.contains("零钱通余额") && (keys)!!.contains("交易单号"))))
            if (detail) return 3
            if ((groupMode && containsRecovered(texts, "发起的群收款"))) return 4
            var success: Boolean = (((((((((keys)!!.contains("支付成功") || (keys)!!.contains("付款成功"))) && !((keys)!!.contains("当前状态"))) && !((keys)!!.contains("查看账单详情"))) && !((keys)!!.contains("常见问题"))) && !((keys)!!.contains("订单支付成功通知"))) && !((keys)!!.contains("交易单号"))) && !((keys)!!.contains("交易时间")))
            var priority: Boolean = ((keys)!!.contains("二维码收款") && ((((keys)!!.contains("优先付款方式") || (keys)!!.contains("优先使用此支付方式付款")) || (keys)!!.contains("优先使用此付款方式"))))
            var password: Boolean = ((keys)!!.contains("付款方式") && (((((keys)!!.contains("使用密码") || (keys)!!.contains("使用指纹")) || (keys)!!.contains("请输入支付密码")) || ((((keys)!!.contains("更改") && (keys)!!.contains("1")) && (keys)!!.contains("2"))))))
            if ((((paymentWechatPage(page) || success) || priority) || password)) return 1
            var transfer: Boolean = (((((((keys)!!.contains("充值成功") && (keys)!!.contains("完成"))) || (((keys)!!.contains("零钱提现") && (keys)!!.contains("到账成功")))) || (((keys)!!.contains("已转入") && (keys)!!.contains("完成")))) || (((keys)!!.contains("已转出") && (keys)!!.contains("完成")))) || (((keys)!!.contains("转出金额") && (keys)!!.contains("完成"))))
            return (if (transfer) 10 else 0)
        }

        private fun containsRecovered(texts: MutableList<String?>?, part: String?): Boolean {
            for (text in (texts)!!) if (((text != null) && (text)!!.contains((part)!!))) return true
            return false
        }
    }
}
