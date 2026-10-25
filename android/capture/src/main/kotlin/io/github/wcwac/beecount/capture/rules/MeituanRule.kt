@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 b396d9ea420bb5ab15e7442bf6195b4f46fd1203374c587f604a61bade9caea3.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class MeituanRule : RuleBase(), RuleCallback {
    private var f13731e: String? = null

    private var f13732f: String? = null

    private var f13733g: String? = null

    private var f13734h: String? = null

    private var f13735i: Boolean = false

    private fun q(context: RuleContext?, str: String?, accessibilityNodeInfo: CaptureNode?): Unit {
        var listG: MutableList<String?>? = null
        var iE: Int = 0
        if ((!(this.f13735i) || (accessibilityNodeInfo == null))) {
            listG = null
        } else {
            listG = g(accessibilityNodeInfo)
            var i6: Int = 1
            if (((((((listG)!! as List<Any?>).contains("创建时间") || ((listG)!! as List<Any?>).contains("退款受理时间")) || ((listG)!! as List<Any?>).contains("下单时间"))) && ((((((listG)!! as List<Any?>).contains("退款成功") || ((listG)!! as List<Any?>).contains("支付成功")) || ((listG)!! as List<Any?>).contains("先用后付自动扣款成功")) || ((((listG)!! as List<Any?>).contains("支付方式") && ((listG)!! as List<Any?>).contains("其他账单服务"))))))) {
                this.a = 4
                this.b = true
            } else if (a(listG, "交易成功 ¥", false)) {
                var iF: Int = f(listG, "交易成功 ¥", false)
                if ((iF >= 0)) {
                    var strReplace: String? = (((listG)!![iF])!!.replace(("交易成功 ¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                    if (Amounts.J(strReplace)) {
                        this.f13733g = strReplace
                    }
                    if ((iF < ((listG)!!.size - 1))) {
                        var str2: String? = (listG)!![(iF + 1)]
                        this.f13731e = str2
                        if ((str2)!!.contains(("已优惠¥")!!)) {
                            var str3: String? = this.f13731e
                            var strReplace2: String? = (((str3)!!.substring((str3)!!.indexOf(("已优惠¥")!!)))!!.replace(("已优惠¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                            if (Amounts.J(strReplace2)) {
                                this.f13734h = strReplace2
                            }
                            var str4: String? = this.f13731e
                            this.f13731e = (str4)!!.substring(0, (str4)!!.indexOf(("已优惠")!!))
                        }
                    }
                }
                this.a = 1
                this.b = true
            } else if (a(listG, "支付成功 ¥", false)) {
                var iD: Int = d(listG, "支付成功 ¥", false)
                if ((iD >= 0)) {
                    var strReplace3: String? = (((listG)!![iD])!!.replace(("支付成功 ¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                    if (Amounts.J(strReplace3)) {
                        this.f13733g = strReplace3
                    }
                    var iD2: Int = d(listG, "➤", true)
                    if (((iD2 >= 0) && (iD2 < ((listG)!!.size - 1)))) {
                        this.f13731e = (listG)!![(iD2 + 1)]
                    } else if ((iD < ((listG)!!.size - 1))) {
                        this.f13731e = (listG)!![(iD + 1)]
                    }
                }
                this.a = 1
                this.b = true
            } else if ((((listG)!! as List<Any?>).contains("交易详情") && a(listG, "交易时间", false))) {
                this.a = 3
                this.b = true
            } else if ((((listG)!! as List<Any?>).contains("提交订单") && ((listG)!! as List<Any?>).contains("外卖配送"))) {
                var iIndexOf: Int = ((listG)!! as List<Any?>).indexOf("点餐请适量，环保又健康")
                if ((((iIndexOf > 0) && (iIndexOf < ((listG)!!.size - 2))) && (((e(listG, "¥", iIndexOf, true)).also { iE = it }) > 0))) {
                    this.f13732f = (listG)!![(iE - 1)]
                }
                var iIndexOf2: Int = ((listG)!! as List<Any?>).indexOf("合计")
                if ((iIndexOf2 > 0)) {
                    if ((iIndexOf2 < ((listG)!!.size - 3))) {
                        run {
                            var i7: Int = 3
                            while ((i6 < i7)) {
                                    var strReplace4: String? = (((listG)!![(iIndexOf2 + i6)])!!.replace(("¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                                    if (Amounts.J(strReplace4)) {
                                        this.f13733g = strReplace4
                                        break
                                    }
                                    i6++
                                i7 = 3
                            }
                        }
                    }
                }
            } else if (((((listG)!! as List<Any?>).contains("提交订单") || ((listG)!! as List<Any?>).contains("极速支付")) || ((listG)!! as List<Any?>).contains("提交月付订单"))) {
                if (((listG)!! as List<Any?>).contains("美团红包")) {
                    var iC: Int = (if (((listG)!! as List<Any?>).contains("优享送")) c(listG, "¥", 2, true) else ((listG)!! as List<Any?>).indexOf("¥"))
                    if (((iC >= 0) && (iC < ((listG)!!.size - 2)))) {
                        this.f13732f = (listG)!![(iC - 1)]
                    }
                } else {
                    var iIndexOf3: Int = ((listG)!! as List<Any?>).indexOf("周一至周日 ")
                    if ((iIndexOf3 > 0)) {
                        this.f13732f = (listG)!![(iIndexOf3 - 1)]
                    } else {
                        var iIndexOf4: Int = ((listG)!! as List<Any?>).indexOf("周一至周五 ")
                        if ((iIndexOf4 > 0)) {
                            this.f13732f = (listG)!![(iIndexOf4 - 1)]
                        } else {
                            var iIndexOf5: Int = ((listG)!! as List<Any?>).indexOf("过期退")
                            if (((iIndexOf5 <= 0) || (iIndexOf5 >= ((listG)!!.size - 1)))) {
                                var iIndexOf6: Int = ((listG)!! as List<Any?>).indexOf("周一至周日 全场通用")
                                if (((iIndexOf6 > 0) && (iIndexOf6 < ((listG)!!.size - 1)))) {
                                    this.f13732f = (listG)!![(iIndexOf6 + 1)]
                                }
                            } else {
                                this.f13732f = (listG)!![(iIndexOf5 + 1)]
                            }
                        }
                    }
                }
                var iIndexOf7: Int = ((listG)!! as List<Any?>).indexOf("合计")
                if ((iIndexOf7 < ((listG)!!.size - 2))) {
                    var i8: Int = (iIndexOf7 + 1)
                    var strReplace5: String? = (if (("¥")!!.equals((listG)!![i8])) (((listG)!![(iIndexOf7 + 2)])!!.replace((",")!!, ("")!!))!!.replace(("¥")!!, ("")!!) else (((listG)!![i8])!!.replace((",")!!, ("")!!))!!.replace(("¥")!!, ("")!!))
                    if (Amounts.J(strReplace5)) {
                        this.f13733g = strReplace5
                    }
                }
                if ((TextUtils.isEmpty(this.f13733g) && (iIndexOf7 < ((listG)!!.size - 3)))) {
                    var strReplace6: String? = (((listG)!![(iIndexOf7 + 2)])!!.replace((",")!!, ("")!!))!!.replace(("¥")!!, ("")!!)
                    if (Amounts.J(strReplace6)) {
                        this.f13733g = strReplace6
                    }
                }
                if ((TextUtils.isEmpty(this.f13733g) && (iIndexOf7 < ((listG)!!.size - 4)))) {
                    var strReplace7: String? = (((listG)!![(iIndexOf7 + 3)])!!.replace((",")!!, ("")!!))!!.replace(("¥")!!, ("")!!)
                    if (Amounts.J(strReplace7)) {
                        this.f13733g = strReplace7
                    }
                }
                if (((listG)!! as List<Any?>).contains("极速支付")) {
                    this.f13731e = "美团"
                } else {
                    var iIndexOf8: Int = ((listG)!! as List<Any?>).indexOf("支付方式")
                    if (((iIndexOf8 >= 0) && (iIndexOf8 < ((listG)!!.size - 2)))) {
                        var str5: String? = (listG)!![(iIndexOf8 + 1)]
                        this.f13731e = str5
                        if ((str5)!!.equals("极速支付")) {
                            this.f13731e = "美团"
                        } else if (((this.f13731e)!!.contains(("外卖券")!!) && (iIndexOf8 < ((listG)!!.size - 3)))) {
                            var str6: String? = (listG)!![(iIndexOf8 + 2)]
                            this.f13731e = str6
                            if ((str6)!!.equals("极速支付")) {
                                this.f13731e = "美团"
                            }
                        }
                    }
                }
            }
        }
        if ((!(this.b) || (accessibilityNodeInfo == null))) {
            return
        }
        w(context, listG)
    }

    private fun w(context: RuleContext?, list: MutableList<String?>?): Unit {
        var billInfoV: RuleBill? = null

        if (((list == null) || (list)!!.isEmpty())) {
            return
        }
        var i6: Int = this.a
        if ((((i6 == 1) || (i6 == 3)) || (i6 == 4))) {

            var i7: Int = this.a
            if ((i7 == 1)) {
                billInfoV = s(this.f13731e, this.f13732f, this.f13733g)
            } else {
                billInfoV = (if ((i7 == 4)) v(list) else u(list))
            }
            if ((billInfoV != null)) {
                this.b = false
                this.f13731e = null
                this.f13732f = null
                this.f13733g = null
                this.f13734h = null
                (billInfoV)!!.setTagList(RuleTags.d(this.f13681d))
                RuleBridge.l(context, billInfoV, this)
            }
        }
    }

    override fun dismiss(): Unit {
        this.b = false
        this.f13731e = null
        this.f13732f = null
        this.f13733g = null
    }

    fun r(context: RuleContext?, str: String?, accessibilityNodeInfo: CaptureNode?): Unit {
        if ((((str)!!.equals("com.dianping.v1.NovaMainActivity") || (str)!!.equals("com.dianping.shopshell.PexusPoiActivity")) || (str)!!.equals("com.dianping.nova.picasso.DPPicassoBoxActivity"))) {
            this.a = 0
            this.b = false
            this.f13735i = false
            this.f13733g = null
            this.f13731e = null
            this.f13732f = null
            this.f13734h = null
        } else if ((((((((((str)!!.equals("com.sankuai.waimai.bussiness.order.confirm.OrderConfirmActivity") || (str)!!.equals("com.meituan.android.mrn.container.MRNBaseActivity")) || (str)!!.equals("com.meituan.android.mrn.container.MRNStandardActivity")) || (str)!!.equals("com.sankuai.waimai.bussiness.order.confirm.OrderConfirmNoTransActivity")) || (str)!!.equals("com.meituan.android.movie.tradebase.activity.PaySeatActivity")) || (str)!!.equals("com.sankuai.eh.framework.EHContainerActivity")) || (str)!!.equals("com.meituan.android.neohybrid.neo.loading.b")) || (str)!!.equals("com.meituan.android.neohybrid.neo.loading.a")) || (str)!!.equals("com.meituan.traveltools.mrncontainer.HTMRNBaseActivity"))) {
            this.f13735i = true
        }
        q(context, str, accessibilityNodeInfo)
    }

    fun s(str: String?, str2: String?, str3: String?): RuleBill? {
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("美团")
        (billInfo)!!.setRemark("美团")
        (billInfo)!!.setShopName("美团")
        if (!(TextUtils.isEmpty(str))) {
            (billInfo)!!.setAsset(str)
        }
        if (!(TextUtils.isEmpty(str2))) {
            (billInfo)!!.setRemark(str2)
        }
        if (!(TextUtils.isEmpty(str3))) {
            (billInfo)!!.setNumber(str3)
        }
        if (!(TextUtils.isEmpty(this.f13734h))) {
            (billInfo)!!.setDiscount(this.f13734h)
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun t(context: RuleContext?, str: String?, accessibilityNodeInfo: CaptureNode?): Unit {
        if ((((((((((str)!!.equals("com.meituan.android.food.deal.FoodDealDetailActivity") || (str)!!.equals("com.meituan.android.pt.homepage.activity.MainActivity")) || (str)!!.equals("com.meituan.android.mtgb.business.main.MTGMainActivity")) || (str)!!.equals("com.sankuai.meituan.search.result.SearchResultActivity")) || (str)!!.equals("com.sankuai.waimai.business.page.homepage.TakeoutActivity")) || (str)!!.equals("com.sankuai.waimai.business.restaurant.poicontainer.WMRestaurantActivity")) || (str)!!.equals("com.sankuai.waimai.business.page.homepage.MainActivity")) || (str)!!.equals("com.meituan.android.hotel.search.HotelSearchResultActivity")) || (str)!!.equals("com.meituan.android.hotel.reuse.detail.HotelPoiDetailActivity"))) {
            this.a = 0
            this.b = false
            this.f13735i = false
            this.f13733g = null
            this.f13731e = null
            this.f13732f = null
            this.f13734h = null
        } else if ((((((((((str)!!.equals("com.sankuai.waimai.bussiness.order.confirm.OrderConfirmActivity") || (str)!!.equals("com.meituan.android.mrn.container.MRNBaseActivity")) || (str)!!.equals("com.meituan.android.mrn.container.MRNStandardActivity")) || (str)!!.equals("com.sankuai.waimai.bussiness.order.confirm.OrderConfirmNoTransActivity")) || (str)!!.equals("com.meituan.android.movie.tradebase.activity.PaySeatActivity")) || (str)!!.equals("com.sankuai.eh.framework.EHContainerActivity")) || (str)!!.equals("com.meituan.android.neohybrid.neo.loading.b")) || (str)!!.equals("com.meituan.android.neohybrid.neo.loading.a")) || (str)!!.equals("com.meituan.traveltools.mrncontainer.HTMRNBaseActivity"))) {
            this.f13735i = true
        }
        q(context, str, accessibilityNodeInfo)
    }

    fun u(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("美团")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((str)!!.equals("交易详情") && (i6 < ((list)!!.size - 4)))) {
                        if (("退款")!!.equals((list)!![(i6 + 4)])) {
                            var sb: StringBuilder? = StringBuilder()
                            (sb)!!.append("退款-")
                            var i7: Int = (i6 + 2)
                            (sb)!!.append((list)!![i7])
                            (billInfo)!!.setShopName((sb)!!.toString())
                            (billInfo)!!.setRemark(("退款-" + (list)!![i7]))
                        } else {
                            var i8: Int = (i6 + 2)
                            (billInfo)!!.setShopName((list)!![i8])
                            (billInfo)!!.setRemark((list)!![i8])
                        }
                        var i9: Int = (i6 + 3)
                        var strReplace: String? = ((((list)!![i9])!!.replace(("+")!!, ("")!!))!!.replace(("-")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                            if (((list)!![i9])!!.contains(("+")!!)) {
                                (billInfo)!!.setIncome(true)
                            }
                        }
                    } else if ((str)!!.contains(("交易时间")!!)) {
                        var strReplace2: String? = (str)!!.replace(("交易时间")!!, ("")!!)
                        if ((!(TextUtils.isEmpty(strReplace2)) || (i6 >= ((list)!!.size - 1)))) {
                            (billInfo)!!.setTime(Dates.X0(strReplace2, "yyyy-MM-dd HH:mm:ss"))
                        } else {
                            var str2: String? = (list)!![(i6 + 1)]
                            if ((str2)!!.contains((":")!!)) {
                                (billInfo)!!.setTime(Dates.X0(str2, "yyyy-MM-dd HH:mm:ss"))
                            }
                        }
                    } else if (((str)!!.equals("支付方式") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset((list)!![(i6 + 1)])
                    }
                i6++
            }
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun v(list: MutableList<String?>?): RuleBill? {
        if ((list)!!.isEmpty()) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("美团")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((((((str)!!.equals("支付成功") || (str)!!.equals("退款成功")) || (str)!!.equals("先用后付自动扣款成功")) || (str)!!.startsWith(("已退款")!!))) && (i6 > 0))) {
                        var str2: String? = (list)!![(i6 - 1)]
                        if ((str2)!!.contains(("+")!!)) {
                            (billInfo)!!.setIncome(true)
                        }
                        var strReplace: String? = (((str2)!!.replace(("+")!!, ("")!!))!!.replace(("-")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                        }
                        if ((i6 > 1)) {
                            (billInfo)!!.setRemark((list)!![(i6 - 2)])
                            (billInfo)!!.setShopName((billInfo)!!.getRemark())
                        }
                    } else if ((((((str)!!.contains(("创建时间")!!) || (str)!!.contains(("退款受理时间")!!)) || (str)!!.contains(("下单时间")!!))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setTime(Dates.X0((list)!![(i6 + 1)], "yyyy-MM-dd HH:mm:ss"))
                    } else if (((((str)!!.equals("退款至") || (str)!!.equals("支付方式"))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset((list)!![(i6 + 1)])
                    } else if ((str)!!.equals("支付优惠")) {
                        var strReplace2: String? = ((((list)!![(i6 + 1)])!!.replace((",")!!, ("")!!))!!.replace(("-")!!, ("")!!))!!.replace((" ")!!, ("")!!)
                        if (Amounts.J(strReplace2)) {
                            (billInfo)!!.setDiscount(strReplace2)
                        }
                    } else if ((((str)!!.equals("订单详情") && (i6 < ((list)!!.size - 2))) && ((list)!![(i6 + 1)])!!.startsWith(("good-detail")!!))) {
                        (billInfo)!!.setRemark((list)!![(i6 + 2)])
                    } else if ((((((str)!!.contains(("退款处理完成，微信已入账")!!) || (str)!!.contains(("点击查看美团月付详情")!!)) || (str)!!.contains(("退款处理完成，支付宝已入账")!!))) && (i6 < ((list)!!.size - 1)))) {
                        var str3: String? = (list)!![(i6 + 1)]
                        if ((Dates.X0(str3, "yyyy-MM-dd HH:mm:ss") > 0)) {
                            (billInfo)!!.setTime(Dates.X0(str3, "yyyy-MM-dd HH:mm:ss"))
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

    companion object {
        private var f13726j: Int = 1

        private var f13727k: Int = 2

        private var f13728l: Int = 3

        private var f13729m: Int = 4

        private var f13730n: MeituanRule? = null

        fun x(): MeituanRule? {
            if ((f13730n == null)) {
                f13730n = MeituanRule()
            }
            return f13730n
        }
    }
}
