@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 15bf2a2af12dfe5285f0626e5c94d99efc643a16c7d54ccfbf2afcadbb500187.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class TaobaoRule : RuleBase(), RuleCallback {
    private var f13748e: String? = null

    private var f13749f: String? = null

    private var f13750g: String? = null

    private var f13751h: Boolean = false

    private var f13752i: Boolean = false

    private fun r(context: RuleContext?, list: MutableList<String?>?, str: String?): Unit {

        if ((((((list != null) && !((list)!!.isEmpty()))) || (str)!!.equals("com.taobao.android.tbabilitykit.pop.StdPopContainerActivity")) || (str)!!.equals("com.alibaba.android.ultron.vfw.weex2.highPerformance.widget.UltronTradeHybridActivity"))) {
            var zA: Boolean = (((str)!!.equals("com.taobao.android.tbabilitykit.pop.StdPopContainerActivity") || (str)!!.equals("com.alibaba.android.ultron.vfw.weex2.highPerformance.widget.UltronTradeHybridActivity")) || (str)!!.equals("com.taobao.themis.container.app.TMSActivity"))
            if ((!(zA) && (this.a == 1))) {
                zA = (a(list, "正在加载中", true) && a(list, "请稍后...", true))
            }
            if (!(zA)) {
                zA = a(list, "免密支付成功", true)
            }
            if (!(zA)) {
                zA = a(list, "支付成功", true)
            }
            if (!(zA)) {
                zA = (((((((list)!! as List<Any?>).contains("下单成功，商家备餐中") || ((list)!! as List<Any?>).contains("下单成功，商家正在备餐")) || ((list)!! as List<Any?>).contains("下单成功，商家已接单")) || ((list)!! as List<Any?>).contains("下单成功，商家备货完成")) || ((list)!! as List<Any?>).contains("下单成功，等待商家接单")) || (((((list)!! as List<Any?>).contains("闪购") && ((list)!! as List<Any?>).contains("返回")) && ((list)!! as List<Any?>).contains("ꁭ"))))
            }
            if (zA) {

                var billInfoT: RuleBill? = (if ((this.a == 1)) t(this.f13748e, this.f13749f, this.f13750g) else null)
                if ((billInfoT != null)) {
                    this.b = false
                    this.f13748e = null
                    this.f13749f = null
                    this.f13750g = null
                    this.f13751h = false
                    this.f13752i = false
                    (billInfoT)!!.setTagList(RuleTags.d(this.f13681d))
                    RuleBridge.l(context, billInfoT, this)
                }
            }
        }
    }

    override fun dismiss(): Unit {
        this.b = false
        this.f13748e = null
        this.f13749f = null
        this.f13750g = null
    }

    fun q(str: String?, str2: String?): Boolean {
        return ((str)!!.equals("com.taobao.taobao") && ((((str2)!!.equals("com.taobao.android.tbabilitykit.pop.StdPopContainerActivity") || (str2)!!.equals("com.taobao.android.purchase.aura.TBBuyActivity")) || (str2)!!.equals("com.taobao.android.sku.widget.SkuDialogFragment\$1"))))
    }

    fun s(context: RuleContext?, str: String?, accessibilityNodeInfo: CaptureNode?): Unit {
        var listM: MutableList<String?>? = null
        if ((((((str)!!.equals("com.taobao.tao.TBMainActivity") || (str)!!.equals("com.taobao.tao.welcome.Welcome")) || (str)!!.equals("com.taobao.search.searchdoor.SearchDoorActivity")) || (str)!!.equals("com.taobao.search.sf.MainSearchResultActivity")) || (str)!!.equals("com.taobao.android.detail.alittdetail.TTDetailActivity"))) {
            this.a = 0
            this.b = false
            this.f13751h = false
            this.f13752i = false
        } else if (((((str)!!.equals("com.taobao.android.sku.widget.a") || (str)!!.equals("com.taobao.android.purchase.aura.TBBuyActivity")) || (str)!!.equals("com.taobao.android.sku.widget.SkuDialogFragment\$1")) || (str)!!.equals("com.taobao.themis.container.app.TMSActivity"))) {
            this.f13751h = true
        }
        if ((((str)!!.equals("com.taobao.android.detail.wrapper.activity.DetailActivity") || (str)!!.equals("com.taobao.android.sku.widget.SkuDialogFragment\$1")) || (str)!!.equals("com.taobao.android.tbsku.TBXSkuActivity"))) {
            this.f13752i = true
        }
        if ((!(this.f13751h) || (accessibilityNodeInfo == null))) {
            listM = null
        } else {
            listM = m(accessibilityNodeInfo)
            if (((listM)!! as List<Any?>).contains("确认付款")) {
                var iIndexOf: Int = ((listM)!! as List<Any?>).indexOf("确认付款")
                if ((iIndexOf >= 5)) {
                    this.f13748e = (listM)!![(iIndexOf - 2)]
                    var strReplace: String? = ((listM)!![(iIndexOf - 5)])!!.replace((",")!!, ("")!!)
                    if (Amounts.J(strReplace)) {
                        this.f13750g = strReplace
                        this.a = 1
                        this.b = true
                    }
                }
            } else if (((listM)!! as List<Any?>).contains("实付价")) {
                var iIndexOf2: Int = ((listM)!! as List<Any?>).indexOf("实付价")
                if ((iIndexOf2 < ((listM)!!.size - 2))) {
                    var strReplace2: String? = ((listM)!![(iIndexOf2 + 2)])!!.replace((",")!!, ("")!!)
                    this.f13748e = "支付宝"
                    if (Amounts.J(strReplace2)) {
                        this.f13750g = strReplace2
                        this.a = 1
                    }
                }
            } else if (((listM)!! as List<Any?>).contains("免密支付")) {
                var iIndexOf3: Int = ((listM)!! as List<Any?>).indexOf("免密支付")
                if ((iIndexOf3 < ((listM)!!.size - 1))) {
                    var strReplace3: String? = (((listM)!![(iIndexOf3 + 1)])!!.replace((",")!!, ("")!!))!!.replace(("￥")!!, ("")!!)
                    if (Amounts.J(strReplace3)) {
                        this.f13750g = strReplace3
                        this.f13748e = "支付宝"
                        this.a = 1
                    }
                }
            } else if (((((((((((str)!!.equals("com.taobao.android.tbabilitykit.pop.StdPopContainerActivity") || (str)!!.equals("com.taobao.themis.container.app.TMSActivity")) || ((listM)!! as List<Any?>).contains("免密支付成功")) || ((listM)!! as List<Any?>).contains("支付成功")) || ((listM)!! as List<Any?>).contains("下单成功，商家备餐中")) || ((listM)!! as List<Any?>).contains("下单成功，商家正在备餐")) || ((listM)!! as List<Any?>).contains("下单成功，商家已接单")) || ((listM)!! as List<Any?>).contains("下单成功，商家备货完成")) || ((listM)!! as List<Any?>).contains("下单成功，等待商家接单")) || ((((listM)!! as List<Any?>).contains("返回") && ((listM)!! as List<Any?>).contains("ꁭ"))))) {
                this.a = 1
                this.b = true
            } else if ((((listM)!! as List<Any?>).contains("确认订单") || ((listM)!! as List<Any?>).contains("提交订单"))) {
                var iD: Int = d(listM, "￥", false)
                if ((iD > 0)) {
                    var str2: String? = (listM)!![(iD - 1)]
                    if ((((("七天无理由退换")!!.equals(str2) || ("券后")!!.equals(str2))) && (iD > 1))) {
                        str2 = (listM)!![(iD - 2)]
                        iD--
                    }
                    if ((!((str2)!!.contains((":")!!)) || (iD <= 1))) {
                        this.f13749f = str2
                    } else {
                        this.f13749f = (listM)!![(iD - 2)]
                    }
                }
                var iIndexOf4: Int = ((listM)!! as List<Any?>).indexOf("合计:")
                if (((iIndexOf4 < 0) || (iIndexOf4 >= ((listM)!!.size - 1)))) {
                    var iD2: Int = d(listM, "合计¥", false)
                    if ((iD2 >= 0)) {
                        var strReplace4: String? = (((listM)!![iD2])!!.replace(("合计¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if ((!(TextUtils.isEmpty(strReplace4)) && Amounts.J(strReplace4))) {
                            this.f13750g = strReplace4
                        }
                    }
                    var iD3: Int = d(listM, "蜂鸟准时达", true)
                    if ((iD3 > 0)) {
                        this.f13749f = (listM)!![(iD3 - 1)]
                    }
                } else {
                    var str3: String? = (listM)!![(iIndexOf4 + 1)]
                    if ((!(TextUtils.isEmpty(str3)) && (str3)!!.contains(("￥")!!))) {
                        var strReplace5: String? = ((str3)!!.replace(("￥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace5)) {
                            this.f13750g = strReplace5
                        }
                    }
                }
            }
        }
        if ((!(this.b) || (accessibilityNodeInfo == null))) {
            return
        }
        r(context, listM, str)
    }

    fun t(str: String?, str2: String?, str3: String?): RuleBill? {
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("淘宝")
        (billInfo)!!.setRemark("淘宝购物")
        (billInfo)!!.setShopName("淘宝")
        if (!(TextUtils.isEmpty(str))) {
            (billInfo)!!.setAsset(str)
        }
        if (!(TextUtils.isEmpty(str2))) {
            (billInfo)!!.setRemark(str2)
        }
        if (!(TextUtils.isEmpty(str3))) {
            (billInfo)!!.setNumber(str3)
        }
        if ((!(TextUtils.isEmpty((billInfo)!!.getRemark())) && !(TextUtils.isEmpty((billInfo)!!.getNumber())))) {
            return billInfo
        }
        if (!(this.f13752i)) {
            return null
        }
        (billInfo)!!.setNumber("0")
        return billInfo
    }

    companion object {
        private var f13746j: Int = 1

        private var f13747k: TaobaoRule? = null

        fun u(): TaobaoRule? {
            if ((f13747k == null)) {
                f13747k = TaobaoRule()
            }
            return f13747k
        }
    }
}
