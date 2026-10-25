@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 4701e77e519b45c17dc27fcb1f20a00a2a40c2fc12c20952458310d2bfc24bd7.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class DouyinRule : RuleBase(), RuleCallback {
    private var f13756e: String? = null

    private var f13757f: String? = null

    private var f13758g: String? = null

    private var f13759h: String? = null

    private var f13760i: Boolean = false

    private var f13761j: Boolean = false

    private fun q(context: RuleContext?, list: MutableList<String?>?): Unit {
        var billInfoU: RuleBill? = null

        if (((list == null) || (list)!!.isEmpty())) {
            return
        }
        if ((this.a == 4)) {
            if (a(list, "抖音月付", false)) {
                this.f13756e = "抖音月付"
            }

            billInfoU = r(this.f13756e, this.f13757f, this.f13758g)
        } else {
            billInfoU = (if (((((list)!! as List<Any?>).contains("抖音月付还款") || ((list)!! as List<Any?>).contains("还款成功")))) u(list) else t(context, list))
        }
        if ((billInfoU != null)) {
            if ((this.a != 5)) {
                this.f13760i = false
            }
            this.b = false
            this.f13756e = null
            this.f13757f = null
            this.f13759h = null
            this.f13758g = null
            (billInfoU)!!.setTagList(RuleTags.d(this.f13681d))
            RuleBridge.l(context, billInfoU, this)
        }
    }

    override fun dismiss(): Unit {

    }

    fun r(str: String?, str2: String?, str3: String?): RuleBill? {
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("抖音")
        (billInfo)!!.setRemark("抖音")
        (billInfo)!!.setShopName("抖音")
        if (TextUtils.isEmpty(str)) {
            (billInfo)!!.setAsset("抖音支付")
        } else {
            (billInfo)!!.setAsset(str)
        }
        if (!(TextUtils.isEmpty(str2))) {
            (billInfo)!!.setRemark(str2)
        }
        if ((!(TextUtils.isEmpty(str3)) && Amounts.J(str3))) {
            (billInfo)!!.setNumber(str3)
        }
        if ((!(TextUtils.isEmpty(this.f13759h)) && Amounts.J(this.f13759h))) {
            (billInfo)!!.setDiscount(this.f13759h)
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun s(arg0: RuleContext?, arg1: String?, arg2: CaptureNode?): Unit {
        var v: Array<Any?>? = arrayOfNulls<Any>(24)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![20] = this
        (v)!![21] = arg0
        (v)!![22] = arg1
        (v)!![23] = arg2
        while (true) {
            if ((pc == -(1))) {
                return
            }
            if ((pc < 0xe9)) pc = recovered_s_block0(v, result, pc) else if ((pc < 0x204)) pc = recovered_s_block1(v, result, pc) else if ((pc < 0x320)) pc = recovered_s_block2(v, result, pc) else if ((pc < 0x469)) pc = recovered_s_block3(v, result, pc) else pc = recovered_s_block4(v, result, pc)
        }
    }

    private fun recovered_s_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (v)!![0] = (v)!![20]
                        (v)!![1] = (v)!![22]
                        (v)!![2] = (v)!![23]
                        (v)!![3] = "com.ss.android.ugc.aweme.main.MainActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        (v)!![4] = dexN(0L)
                        (v)!![5] = dexN(0L)
                        pc = (if ((!(dexZero((v)!![3])))) 0x48 else 0x10)
                        continue
                }
                0x10 -> {
                        (v)!![3] = "com.ss.android.ugc.aweme.newdetail.DitoPoiDetailActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x48 else 0x18)
                        continue
                }
                0x18 -> {
                        (v)!![3] = "com.ss.android.ugc.aweme.search.activity.SearchResultActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x48 else 0x20)
                        continue
                }
                0x20 -> {
                        (v)!![3] = "com.ss.android.ugc.aweme.detail.ui.LiveDetailActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x48 else 0x28)
                        continue
                }
                0x28 -> {
                        (v)!![3] = "com.ss.android.ugc.aweme.detail.ui.DetailActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x48 else 0x30)
                        continue
                }
                0x30 -> {
                        (v)!![3] = "com.ss.android.ugc.aweme.live.LivePlayActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x48 else 0x38)
                        continue
                }
                0x38 -> {
                        (v)!![3] = "com.ss.android.ugc.aweme.commerce.sdk.MallContainerActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x48 else 0x40)
                        continue
                }
                0x40 -> {
                        (v)!![3] = "com.bytedance.android.shopping.anchorv4.containers.AnchorV4Activity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x56 else 0x48)
                        continue
                }
                0x48 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13760i = dexZ((v)!![5])
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13757f = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13756e = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13759h = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![5])
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13761j = dexZ((v)!![5])
                        pc = 0x56
                        continue
                }
                0x56 -> {
                        (v)!![3] = "com.ss.android.ugc.aweme.bullet.ui.BulletContainerActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        (v)!![6] = "提交订单"
                        (v)!![7] = dexN(1L)
                        pc = (if ((!(dexZero((v)!![3])))) 0xcf else 0x61)
                        continue
                }
                0x61 -> {
                        (v)!![3] = "com.android.ttcjpaysdk.base.ui.dialog.CJPayCommonDialog"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xcf else 0x69)
                        continue
                }
                0x69 -> {
                        (v)!![3] = "com.android.ttcjpaysdk.thirdparty.front.counter.dypay.activity.DyPayETActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xcf else 0x71)
                        continue
                }
                0x71 -> {
                        (v)!![3] = "com.bytedance.android.ec.common.impl.view.FixedDialog"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xcf else 0x79)
                        continue
                }
                0x79 -> {
                        (v)!![3] = "com.bytedance.android.shopping.buynow.container.BuyNowFullscreenActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xcf else 0x81)
                        continue
                }
                0x81 -> {
                        (v)!![3] = "com.bytedance.ies.xelement.overlay.LynxOverlayDialog"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xcf else 0x89)
                        continue
                }
                0x89 -> {
                        (v)!![3] = "com.ss.android.ugc.aweme.live.LiveDummyActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xcf else 0x91)
                        continue
                }
                0x91 -> {
                        (v)!![3] = "com.bytedance.android.shopping.buynow.container.BuyNowFullscreenDialog"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xcf else 0x99)
                        continue
                }
                0x99 -> {
                        (v)!![3] = "com.android.ttcjpaysdk.thirdparty.front.counter.dypay.activity.DyPayStandardActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xcf else 0xa1)
                        continue
                }
                0xa1 -> {
                        (v)!![3] = "com.android.ttcjpaysdk.bdpay.security.loading.view.CJPaySecurityLoadingActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xcf else 0xa9)
                        continue
                }
                0xa9 -> {
                        (v)!![3] = "X."
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.startsWith((((dexR((v)!![3]) as String?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0xcf else 0xb1)
                        continue
                }
                0xb1 -> {
                        (v)!![3] = "com.lynx.tasm.behavior.KeyboardMonitor"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((dexZero((v)!![8]))) 0xc6 else 0xb9)
                        continue
                }
                0xb9 -> {
                        pc = (if ((dexZero((v)!![2]))) 0xc6 else 0xbb)
                        continue
                }
                0xbb -> {
                        (result)!![0] = (((dexR((v)!![2]) as CaptureNode?)))!!.findAccessibilityNodeInfosByText(((dexR((v)!![6]) as String?)))
                        (v)!![8] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as DouyinRule?)))!!.w(((dexR((v)!![8]) as MutableList<CaptureNode>?))))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((dexZero((v)!![8]))) 0xc6 else 0xc5)
                        continue
                }
                0xc5 -> {
                        pc = 0xcf
                        continue
                }
                0xc6 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0xd1 else 0xcc)
                        continue
                }
                0xcc -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13761j = dexZ((v)!![7])
                        pc = 0xd1
                        continue
                }
                0xcf -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13760i = dexZ((v)!![7])
                        pc = 0xd1
                        continue
                }
                0xd1 -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as DouyinRule?)))!!.f13761j)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0xe5 else 0xd5)
                        continue
                }
                0xd5 -> {
                        pc = (if ((dexZero((v)!![2]))) 0xe5 else 0xd7)
                        continue
                }
                0xd7 -> {
                        (result)!![0] = (((dexR((v)!![2]) as CaptureNode?)))!!.findAccessibilityNodeInfosByText(((dexR((v)!![6]) as String?)))
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as DouyinRule?)))!!.w(((dexR((v)!![1]) as MutableList<CaptureNode>?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0xe5 else 0xe1)
                        continue
                }
                0xe1 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13761j = dexZ((v)!![5])
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13760i = dexZ((v)!![7])
                        pc = 0xe5
                        continue
                }
                0xe5 -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as DouyinRule?)))!!.f13760i)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x5c2 else 0xe9)
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
                0xe9 -> {
                        (v)!![1] = dexN(4L)
                        (v)!![3] = "支付成功"
                        (v)!![8] = ","
                        (v)!![9] = dexN(2L)
                        (v)!![10] = "¥"
                        (v)!![11] = ""
                        pc = (if ((dexZero((v)!![2]))) 0x4bd else 0xf5)
                        continue
                }
                0xf5 -> {
                        (result)!![0] = (((dexR((v)!![0]) as RuleBase?)))!!.l(((dexR((v)!![2]) as CaptureNode?)))
                        (v)!![12] = (result)!![0]
                        (v)!![13] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.c)
                        (v)!![14] = dexN(199L)
                        pc = (if (((dexI((v)!![13]) < dexI((v)!![14])))) 0x11a else 0xff)
                        continue
                }
                0xff -> {
                        (v)!![13] = "扫一扫"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as MutableList<String?>?)))!! as List<Any?>).contains(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x11a else 0x107)
                        continue
                }
                0x107 -> {
                        (v)!![13] = "通知消息"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as MutableList<String?>?)))!! as List<Any?>).contains(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x11a else 0x10f)
                        continue
                }
                0x10f -> {
                        (v)!![13] = "常用功能"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as MutableList<String?>?)))!! as List<Any?>).contains(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x11a else 0x117)
                        continue
                }
                0x117 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13760i = dexZ((v)!![5])
                        return -(1)
                }
                0x11a -> {
                        (v)!![13] = "账单详情"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as MutableList<String?>?)))!! as List<Any?>).contains(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        (v)!![14] = dexN(5L)
                        (v)!![15] = " "
                        (v)!![4] = dexN(3L)
                        pc = (if ((dexZero((v)!![13]))) 0x174 else 0x126)
                        continue
                }
                0x126 -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![7])
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![14])
                        (result)!![0] = dexN((((dexR((v)!![12]) as MutableList<String?>?)))!!.size)
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexEqual((v)!![6], (v)!![1])))) 0x14f else 0x130)
                        continue
                }
                0x130 -> {
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![6] = (result)!![0]
                        (v)!![6] = ((dexR((v)!![6]) as CharSequence?))
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![6]) as CharSequence?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x14f else 0x13c)
                        continue
                }
                0x13c -> {
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (result)!![0] = javaSplit((((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![15]) as String?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as Array<Any?>?)))!!.toMutableList()
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as MutableList<String?>?)))!!.addAll((((dexR((v)!![4]) as MutableCollection<String?>?)))!!))) 1 else 0))
                        pc = 0x4bc
                        continue
                }
                0x14f -> {
                        (result)!![0] = dexN((((dexR((v)!![12]) as MutableList<String?>?)))!!.size)
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexEqual((v)!![6], (v)!![4])))) 0x4bc else 0x155)
                        continue
                }
                0x155 -> {
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![9])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as CharSequence?))
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x4bc else 0x161)
                        continue
                }
                0x161 -> {
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![9])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (result)!![0] = javaSplit((((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![15]) as String?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as Array<Any?>?)))!!.toMutableList()
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as MutableList<String?>?)))!!.addAll((((dexR((v)!![4]) as MutableCollection<String?>?)))!!))) 1 else 0))
                        pc = 0x4bc
                        continue
                }
                0x174 -> {
                        (result)!![0] = dexN((((dexR((v)!![12]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![7])))
                        (v)!![16] = dexN(0L)
                        pc = 0x17b
                        continue
                }
                0x17b -> {
                        pc = (if (((dexI((v)!![13]) < 0))) 0x47a else 0x17d)
                        continue
                }
                0x17d -> {
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![13])]
                        (v)!![17] = (result)!![0]
                        (v)!![14] = (v)!![17]
                        (v)!![14] = ((dexR((v)!![14]) as String?))
                        (v)!![1] = "【"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![1]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x197 else 0x18d)
                        continue
                }
                0x18d -> {
                        (v)!![1] = "】"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![1]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x197 else 0x195)
                        continue
                }
                0x195 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13757f = ((dexR((v)!![14]) as String?))
                        pc = 0x197
                        continue
                }
                0x197 -> {
                        (v)!![1] = "团购头图"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![18] = (result)!![0]
                        pc = (if ((dexZero((v)!![18]))) 0x1b7 else 0x19f)
                        continue
                }
                0x19f -> {
                        (result)!![0] = dexN((((dexR((v)!![12]) as MutableList<String?>?)))!!.size)
                        (v)!![18] = (result)!![0]
                        (v)!![4] = dexN((dexI((v)!![18]) + (-(1))))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![4])))) 0x1b7 else 0x1a7)
                        continue
                }
                0x1a7 -> {
                        (v)!![4] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x1b7 else 0x1b5)
                        continue
                }
                0x1b5 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13757f = ((dexR((v)!![4]) as String?))
                        pc = 0x1b7
                        continue
                }
                0x1b7 -> {
                        (v)!![1] = "已选择"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.startsWith((((dexR((v)!![1]) as String?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1c5 else 0x1bf)
                        continue
                }
                0x1bf -> {
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.replace((((dexR((v)!![1]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13757f = ((dexR((v)!![1]) as String?))
                        pc = 0x1c5
                        continue
                }
                0x1c5 -> {
                        (v)!![1] = "实付"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![1]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        (v)!![7] = "元"
                        pc = (if ((dexZero((v)!![4]))) 0x1d5 else 0x1cf)
                        continue
                }
                0x1cf -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![7]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x1e1 else 0x1d5)
                        continue
                }
                0x1d5 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![1]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1fc else 0x1db)
                        continue
                }
                0x1db -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![10]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1fc else 0x1e1)
                        continue
                }
                0x1e1 -> {
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.replace((((dexR((v)!![1]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![7]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1f9 else 0x1f7)
                        continue
                }
                0x1f7 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g = ((dexR((v)!![1]) as String?))
                        pc = 0x1f9
                        continue
                }
                0x1f9 -> {
                        (v)!![4] = dexN(3L)
                        pc = 0x2d6
                        continue
                }
                0x1fc -> {
                        (v)!![1] = "提交订单,按钮"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x248 else 0x204)
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
                0x204 -> {
                        pc = (if (((dexI((v)!![13]) <= dexI((v)!![9])))) 0x248 else 0x206)
                        continue
                }
                0x206 -> {
                        (v)!![1] = dexN((dexI((v)!![13]) + (-(1))))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![10]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x21c else 0x214)
                        continue
                }
                0x214 -> {
                        (v)!![1] = dexN((dexI((v)!![13]) + (-(2))))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        pc = 0x21c
                        continue
                }
                0x21c -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![10]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1f9 else 0x222)
                        continue
                }
                0x222 -> {
                        (result)!![0] = javaSplit((((dexR((v)!![1]) as String?)))!!, (((dexR((v)!![10]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (v)!![4] = dexN(dexLength(dexR((v)!![1])))
                        pc = (if (((dexI((v)!![4]) <= 0))) 0x1f9 else 0x229)
                        continue
                }
                0x229 -> {
                        (v)!![4] = (((dexR((v)!![1]) as Array<Any?>?)))!![dexI((v)!![5])]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![19] = (result)!![0]
                        pc = (if ((dexZero((v)!![19]))) 0x237 else 0x231)
                        continue
                }
                0x231 -> {
                        (v)!![5] = dexN(dexLength(dexR((v)!![1])))
                        (v)!![9] = dexN(1L)
                        pc = (if (((dexI((v)!![5]) <= dexI((v)!![9])))) 0x237 else 0x235)
                        continue
                }
                0x235 -> {
                        (v)!![4] = (((dexR((v)!![1]) as Array<Any?>?)))!![dexI((v)!![9])]
                        pc = 0x237
                        continue
                }
                0x237 -> {
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1f9 else 0x245)
                        continue
                }
                0x245 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g = ((dexR((v)!![1]) as String?))
                        pc = 0x1f9
                        continue
                }
                0x248 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x281 else 0x24e)
                        continue
                }
                0x24e -> {
                        (v)!![1] = dexN(2L)
                        pc = (if (((dexI((v)!![13]) <= dexI((v)!![1])))) 0x281 else 0x251)
                        continue
                }
                0x251 -> {
                        (v)!![1] = dexN((dexI((v)!![13]) + (-(1))))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x277 else 0x267)
                        continue
                }
                0x267 -> {
                        (v)!![1] = dexN((dexI((v)!![13]) + (-(2))))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        pc = 0x277
                        continue
                }
                0x277 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1f9 else 0x27d)
                        continue
                }
                0x27d -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g = ((dexR((v)!![1]) as String?))
                        pc = 0x1f9
                        continue
                }
                0x281 -> {
                        (v)!![1] = "实付价"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x1f9 else 0x289)
                        continue
                }
                0x289 -> {
                        (v)!![1] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![10]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x1f9 else 0x295)
                        continue
                }
                0x295 -> {
                        (result)!![0] = dexN((((dexR((v)!![12]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![4] = dexN(3L)
                        (v)!![1] = dexN((dexI((v)!![1]) - dexI((v)!![4])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![1])))) 0x2d6 else 0x29d)
                        continue
                }
                0x29d -> {
                        (v)!![1] = dexN((dexI((v)!![13]) + (2)))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (v)!![5] = dexN((dexI((v)!![13]) + (3)))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![5])]
                        (v)!![5] = (result)!![0]
                        (v)!![5] = ((dexR((v)!![5]) as String?))
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![5]) as CharSequence?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![9])))) 0x2ce else 0x2b7)
                        continue
                }
                0x2b7 -> {
                        (v)!![9] = "."
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as String?)))!!.startsWith((((dexR((v)!![9]) as String?)))!!))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((dexZero((v)!![9]))) 0x2ce else 0x2bf)
                        continue
                }
                0x2bf -> {
                        (v)!![9] = DexReference()
                        ((((v)!![9] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![9]) as StringBuilder?)))!!.append(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![9]) as StringBuilder?)))!!.append(((dexR((v)!![5]) as String?)))
                        (result)!![0] = (((dexR((v)!![9]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        pc = 0x2ce
                        continue
                }
                0x2ce -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x2d6 else 0x2d4)
                        continue
                }
                0x2d4 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g = ((dexR((v)!![1]) as String?))
                        pc = 0x2d6
                        continue
                }
                0x2d6 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as MutableList<String?>?)))!! as List<Any?>).contains(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        (v)!![5] = " "
                        pc = (if ((dexZero((v)!![1]))) 0x2f8 else 0x2de)
                        continue
                }
                0x2de -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![10]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x2f8 else 0x2e4)
                        continue
                }
                0x2e4 -> {
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((dexZero((v)!![9]))) 0x2f8 else 0x2f6)
                        continue
                }
                0x2f6 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g = ((dexR((v)!![1]) as String?))
                        pc = 0x2f8
                        continue
                }
                0x2f8 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x320 else 0x2fe)
                        continue
                }
                0x2fe -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![10]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x320 else 0x304)
                        continue
                }
                0x304 -> {
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((dexZero((v)!![9]))) 0x320 else 0x31e)
                        continue
                }
                0x31e -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g = ((dexR((v)!![1]) as String?))
                        pc = 0x320
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_s_block3(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x320 -> {
                        (v)!![1] = "支付方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        (v)!![4] = "("
                        pc = (if ((dexZero((v)!![9]))) 0x35e else 0x32a)
                        continue
                }
                0x32a -> {
                        (result)!![0] = dexN((((dexR((v)!![12]) as MutableList<String?>?)))!!.size)
                        (v)!![9] = (result)!![0]
                        (v)!![18] = dexN(1L)
                        (v)!![9] = dexN((dexI((v)!![9]) + (-(1))))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![9])))) 0x35e else 0x334)
                        continue
                }
                0x334 -> {
                        (v)!![9] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![9])]
                        (v)!![9] = (result)!![0]
                        (v)!![9] = ((dexR((v)!![9]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![9]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x35e else 0x342)
                        continue
                }
                0x342 -> {
                        (v)!![1] = "IB"
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![1]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (v)!![9] = ")"
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (v)!![9] = "​"
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13756e = ((dexR((v)!![1]) as String?))
                        pc = 0x35e
                        continue
                }
                0x35e -> {
                        (v)!![1] = "付款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![1]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x3a4 else 0x366)
                        continue
                }
                0x366 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![10]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x3a4 else 0x36c)
                        continue
                }
                0x36c -> {
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (v)!![9] = "付款成功"
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x38a else 0x388)
                        continue
                }
                0x388 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g = ((dexR((v)!![1]) as String?))
                        pc = 0x38a
                        continue
                }
                0x38a -> {
                        (v)!![1] = (((dexR((v)!![0]) as DouyinRule?)))!!.f13756e
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x3a4 else 0x392)
                        continue
                }
                0x392 -> {
                        (result)!![0] = dexN((((dexR((v)!![12]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![5] = dexN(1L)
                        (v)!![1] = dexN((dexI((v)!![1]) - dexI((v)!![5])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![1])))) 0x3a4 else 0x39a)
                        continue
                }
                0x39a -> {
                        (v)!![1] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13756e = ((dexR((v)!![1]) as String?))
                        pc = 0x3a4
                        continue
                }
                0x3a4 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![10]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x3ec else 0x3aa)
                        continue
                }
                0x3aa -> {
                        (result)!![0] = dexN((((dexR((v)!![12]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![5] = dexN(4L)
                        (v)!![1] = dexN((dexI((v)!![1]) - dexI((v)!![5])))
                        pc = (if ((dexEqual((v)!![13], (v)!![1]))) 0x3bb else 0x3b2)
                        continue
                }
                0x3b2 -> {
                        (result)!![0] = dexN((((dexR((v)!![12]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![5] = dexN(5L)
                        (v)!![1] = dexN((dexI((v)!![1]) - dexI((v)!![5])))
                        pc = (if ((!(dexEqual((v)!![13], (v)!![1])))) 0x3ed else 0x3ba)
                        continue
                }
                0x3ba -> {
                        pc = 0x3bc
                        continue
                }
                0x3bb -> {
                        (v)!![5] = dexN(5L)
                        pc = 0x3bc
                        continue
                }
                0x3bc -> {
                        (result)!![0] = dexN((((dexR((v)!![12]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![9] = dexN(2L)
                        (v)!![1] = dexN((dexI((v)!![1]) - dexI((v)!![9])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![1])))) 0x3ed else 0x3c4)
                        continue
                }
                0x3c4 -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![9] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![9])]
                        (v)!![9] = (result)!![0]
                        (v)!![9] = ((dexR((v)!![9]) as String?))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![9]) as String?)))
                        (v)!![9] = dexN((dexI((v)!![13]) + (2)))
                        (result)!![0] = (((dexR((v)!![12]) as MutableList<String?>?)))!![dexI((v)!![9])]
                        (v)!![9] = (result)!![0]
                        (v)!![9] = ((dexR((v)!![9]) as String?))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![9]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((dexZero((v)!![9]))) 0x3ed else 0x3e9)
                        continue
                }
                0x3e9 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g = ((dexR((v)!![1]) as String?))
                        pc = 0x3ed
                        continue
                }
                0x3ec -> {
                        (v)!![5] = dexN(5L)
                        pc = 0x3ed
                        continue
                }
                0x3ed -> {
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![16]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x403 else 0x3f3)
                        continue
                }
                0x3f3 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x403 else 0x3f9)
                        continue
                }
                0x3f9 -> {
                        (v)!![1] = ")"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![1]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x403 else 0x401)
                        continue
                }
                0x401 -> {
                        (v)!![1] = (v)!![14]
                        pc = 0x405
                        continue
                }
                0x403 -> {
                        (v)!![1] = (v)!![16]
                        pc = 0x405
                        continue
                }
                0x405 -> {
                        (v)!![4] = (((dexR((v)!![0]) as DouyinRule?)))!!.f13756e
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x42d else 0x40d)
                        continue
                }
                0x40d -> {
                        (v)!![4] = "，已选中"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x42d else 0x415)
                        continue
                }
                0x415 -> {
                        (v)!![4] = "，已选中"
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13756e = ((dexR((v)!![4]) as String?))
                        (v)!![4] = "抖音支付"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x42d else 0x425)
                        continue
                }
                0x425 -> {
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x42d else 0x42b)
                        continue
                }
                0x42b -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13756e = ((dexR((v)!![1]) as String?))
                        pc = 0x42d
                        continue
                }
                0x42d -> {
                        (v)!![4] = "已优惠"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.startsWith((((dexR((v)!![4]) as String?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x46d else 0x435)
                        continue
                }
                0x435 -> {
                        (v)!![4] = "已优惠"
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![7]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13759h = ((dexR((v)!![4]) as String?))
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = StringBuilder()
                        (v)!![7] = "discountNum:"
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![7]) as String?)))
                        (v)!![7] = (((dexR((v)!![0]) as DouyinRule?)))!!.f13759h
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![7]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.toString()
                        (v)!![4] = (result)!![0]
                        (v)!![7] = "sssss"
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![7]) as Any?)), ((dexR((v)!![4]) as Any?)))
                        (v)!![4] = (result)!![0]

                        (v)!![4] = (((dexR((v)!![0]) as DouyinRule?)))!!.f13759h
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x46d else 0x469)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_s_block4(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x469 -> {
                        (v)!![4] = dexN(0L)
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13759h = ((dexR((v)!![4]) as String?))
                        pc = 0x46e
                        continue
                }
                0x46d -> {
                        (v)!![4] = dexN(0L)
                        pc = 0x46e
                        continue
                }
                0x46e -> {
                        (v)!![13] = dexN((dexI((v)!![13]) + (-(1))))
                        (v)!![16] = (v)!![1]
                        (v)!![14] = (v)!![5]
                        (v)!![1] = dexN(4L)
                        (v)!![4] = dexN(3L)
                        (v)!![5] = dexN(0L)
                        (v)!![7] = dexN(1L)
                        (v)!![9] = dexN(2L)
                        pc = 0x17b
                        continue
                }
                0x47a -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![4] = "asset:"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![4]) as String?)))
                        (v)!![4] = (((dexR((v)!![0]) as DouyinRule?)))!!.f13756e
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![4]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = StringBuilder()
                        (v)!![5] = "remark:"
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![5]) as String?)))
                        (v)!![5] = (((dexR((v)!![0]) as DouyinRule?)))!!.f13757f
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![5]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.toString()
                        (v)!![4] = (result)!![0]
                        (v)!![5] = DexReference()
                        ((((v)!![5] as DexReference?)))!!.value = StringBuilder()
                        (v)!![6] = "num:"
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.append(((dexR((v)!![6]) as String?)))
                        (v)!![6] = (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.append(((dexR((v)!![6]) as String?)))
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.toString()
                        (v)!![5] = (result)!![0]
                        (v)!![6] = "sssss"
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![6]) as Any?)), ((dexR((v)!![1]) as Any?)), ((dexR((v)!![4]) as Any?)), ((dexR((v)!![5]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x4bc
                        continue
                }
                0x4bc -> {
                        (v)!![4] = (v)!![12]
                        pc = 0x4bd
                        continue
                }
                0x4bd -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.b)) 1 else 0))
                        pc = (if ((!(dexZero((v)!![1])))) 0x5c2 else 0x4c1)
                        continue
                }
                0x4c1 -> {
                        pc = (if ((dexZero((v)!![2]))) 0x5c2 else 0x4c3)
                        continue
                }
                0x4c3 -> {
                        (v)!![1] = "购买成功"
                        (v)!![5] = dexN(1L)
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![4]) as MutableList<String?>?)), ((dexR((v)!![1]) as String?)), dexZ((v)!![5])))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x4d4 else 0x4cc)
                        continue
                }
                0x4cc -> {
                        (v)!![1] = dexN(0L)
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![4]) as MutableList<String?>?)), ((dexR((v)!![3]) as String?)), dexZ((v)!![1])))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((dexZero((v)!![6]))) 0x5c2 else 0x4d3)
                        continue
                }
                0x4d3 -> {
                        pc = 0x4d5
                        continue
                }
                0x4d4 -> {
                        (v)!![1] = dexN(0L)
                        pc = 0x4d5
                        continue
                }
                0x4d5 -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![5])
                        (v)!![5] = dexN(4L)
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![5])
                        (v)!![5] = "抖音支付 ("
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![4]) as MutableList<String?>?)), ((dexR((v)!![5]) as String?)), dexZ((v)!![1])))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((dexZero((v)!![6]))) 0x51a else 0x4e2)
                        continue
                }
                0x4e2 -> {
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.d(((dexR((v)!![4]) as MutableList<String?>?)), ((dexR((v)!![5]) as String?)), dexZ((v)!![1])))
                        (v)!![1] = (result)!![0]
                        pc = (if (((dexI((v)!![1]) < 0))) 0x51a else 0x4e8)
                        continue
                }
                0x4e8 -> {
                        (result)!![0] = dexN((((dexR((v)!![4]) as MutableList<String?>?)))!!.size)
                        (v)!![6] = (result)!![0]
                        pc = (if (((dexI((v)!![1]) >= dexI((v)!![6])))) 0x51a else 0x4ee)
                        continue
                }
                0x4ee -> {
                        (result)!![0] = (((dexR((v)!![4]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (v)!![5] = ") 付款"
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13756e = ((dexR((v)!![1]) as String?))
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![5] = "check asset : "
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![5]) as String?)))
                        (v)!![5] = (((dexR((v)!![0]) as DouyinRule?)))!!.f13756e
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![5]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x51a
                        continue
                }
                0x51a -> {
                        (v)!![1] = dexN(1L)
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![4]) as MutableList<String?>?)), ((dexR((v)!![3]) as String?)), dexZ((v)!![1])))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x5a8 else 0x521)
                        continue
                }
                0x521 -> {
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.d(((dexR((v)!![4]) as MutableList<String?>?)), ((dexR((v)!![3]) as String?)), dexZ((v)!![1])))
                        (v)!![3] = (result)!![0]
                        pc = (if (((dexI((v)!![3]) < 0))) 0x5a8 else 0x527)
                        continue
                }
                0x527 -> {
                        (result)!![0] = dexN((((dexR((v)!![4]) as MutableList<String?>?)))!!.size)
                        (v)!![5] = (result)!![0]
                        (v)!![5] = dexN((dexI((v)!![5]) - dexI((v)!![1])))
                        pc = (if (((dexI((v)!![3]) >= dexI((v)!![5])))) 0x5a8 else 0x52e)
                        continue
                }
                0x52e -> {
                        (v)!![1] = dexN((dexI((v)!![3]) + (1)))
                        (result)!![0] = (((dexR((v)!![4]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![10]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x5a8 else 0x53c)
                        continue
                }
                0x53c -> {
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (v)!![6] = "确认收货后抖音月付付款"
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![5]) as String?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((dexZero((v)!![6]))) 0x56d else 0x550)
                        continue
                }
                0x550 -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g = ((dexR((v)!![5]) as String?))
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "check num : "
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (v)!![3] = (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x5a8
                        continue
                }
                0x56d -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![10]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x5a8 else 0x573)
                        continue
                }
                0x573 -> {
                        (result)!![0] = dexN((((dexR((v)!![4]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![5] = dexN(2L)
                        (v)!![1] = dexN((dexI((v)!![1]) - dexI((v)!![5])))
                        pc = (if (((dexI((v)!![3]) >= dexI((v)!![1])))) 0x5a8 else 0x57b)
                        continue
                }
                0x57b -> {
                        (v)!![3] = dexN((dexI((v)!![3]) + dexI((v)!![5])))
                        (result)!![0] = (((dexR((v)!![4]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![8]) as CharSequence?)))!!, (((dexR((v)!![11]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x5a8 else 0x58c)
                        continue
                }
                0x58c -> {
                        (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g = ((dexR((v)!![1]) as String?))
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "check num : "
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (v)!![3] = (((dexR((v)!![0]) as DouyinRule?)))!!.f13758g
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x5a8
                        continue
                }
                0x5a8 -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "nodeInfo tiktok can add : "
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (v)!![3] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.b)) 1 else 0))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(dexZ((v)!![3]))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x5c2
                        continue
                }
                0x5c2 -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.b)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x5cf else 0x5c6)
                        continue
                }
                0x5c6 -> {
                        pc = (if ((dexZero((v)!![2]))) 0x5cf else 0x5c8)
                        continue
                }
                0x5c8 -> {
                        pc = (if ((dexZero((v)!![4]))) 0x5cf else 0x5ca)
                        continue
                }
                0x5ca -> {
                        (v)!![1] = (v)!![21]
                        (((dexR((v)!![0]) as DouyinRule?)))!!.q(((dexR((v)!![1]) as RuleContext?)), ((dexR((v)!![4]) as MutableList<String?>?)))
                        pc = 0x5cf
                        continue
                }
                0x5cf -> {
                        return -(1)
                }
                else -> {
                    return pc
                }
            }
        }
    }

    fun t(arg0: RuleContext?, arg1: MutableList<String?>?): RuleBill? {
        var v: Array<Any?>? = arrayOfNulls<Any>(18)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![15] = this
        (v)!![16] = arg0
        (v)!![17] = arg1
        while (true) {
            if ((pc == -(1))) {
                return ((dexR((result)!![1]) as RuleBill?))
            }
            if ((pc < 0x164)) pc = recovered_t_block0(v, result, pc) else if ((pc < 0x2be)) pc = recovered_t_block1(v, result, pc) else pc = recovered_t_block2(v, result, pc)
        }
    }

    private fun recovered_t_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (v)!![0] = (v)!![17]
                        (result)!![0] = dexN((if (((((dexR((v)!![17]) as MutableList<String?>?)))!!.isEmpty())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        (v)!![2] = dexN(0L)
                        pc = (if ((dexZero((v)!![1]))) 0xa else 0x9)
                        continue
                }
                0x9 -> {
                        (result)!![1] = (v)!![2]
                        return -(1)
                }
                0xa -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = RuleBill()
                        (v)!![3] = "抖音"
                        (((dexR((v)!![1]) as RuleBill?)))!!.setOrigin(((dexR((v)!![3]) as String?)))
                        (v)!![3] = dexN(0L)
                        pc = 0x15
                        continue
                }
                0x15 -> {
                        (result)!![0] = dexN((((dexR((v)!![17]) as MutableList<String?>?)))!!.size)
                        (v)!![4] = (result)!![0]
                        (v)!![5] = ""
                        pc = (if (((dexI((v)!![3]) >= dexI((v)!![4])))) 0x34d else 0x1d)
                        continue
                }
                0x1d -> {
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (v)!![6] = "支付成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        (v)!![7] = dexN(2L)
                        (v)!![8] = dexN(1L)
                        pc = (if ((!(dexZero((v)!![6])))) 0x75 else 0x2d)
                        continue
                }
                0x2d -> {
                        (v)!![6] = "退款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x75 else 0x35)
                        continue
                }
                0x35 -> {
                        (v)!![6] = "已过期"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x75 else 0x3d)
                        continue
                }
                0x3d -> {
                        (v)!![6] = "已全额退款"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x75 else 0x45)
                        continue
                }
                0x45 -> {
                        (v)!![6] = "入账成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x75 else 0x4d)
                        continue
                }
                0x4d -> {
                        (v)!![6] = "确认收货后付款"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x75 else 0x55)
                        continue
                }
                0x55 -> {
                        (v)!![6] = "到账成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x75 else 0x5d)
                        continue
                }
                0x5d -> {
                        (v)!![6] = "充值成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x75 else 0x65)
                        continue
                }
                0x65 -> {
                        (v)!![6] = "自动付款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x75 else 0x6d)
                        continue
                }
                0x6d -> {
                        (v)!![6] = "已退款(￥"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.startsWith((((dexR((v)!![6]) as String?)))!!))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((dexZero((v)!![6]))) 0x168 else 0x75)
                        continue
                }
                0x75 -> {
                        pc = (if (((dexI((v)!![3]) <= dexI((v)!![7])))) 0x168 else 0x77)
                        continue
                }
                0x77 -> {
                        (v)!![4] = dexN((dexI((v)!![3]) + (-(1))))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (v)!![6] = "¥"
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (v)!![9] = ","
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (v)!![10] = "+"
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0xad else 0x97)
                        continue
                }
                0x97 -> {
                        (v)!![4] = dexN((dexI((v)!![3]) + (-(2))))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (v)!![6] = (v)!![7]
                        pc = 0xca
                        continue
                }
                0xad -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0xc9 else 0xb3)
                        continue
                }
                0xb3 -> {
                        (v)!![4] = dexN((dexI((v)!![3]) + (-(3))))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (v)!![6] = dexN(3L)
                        pc = 0xca
                        continue
                }
                0xc9 -> {
                        (v)!![6] = (v)!![8]
                        pc = 0xca
                        continue
                }
                0xca -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getNumber()
                        (v)!![9] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![9]) as CharSequence?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((dexZero((v)!![9]))) 0x164 else 0xd4)
                        continue
                }
                0xd4 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((dexZero((v)!![9]))) 0x164 else 0xda)
                        continue
                }
                0xda -> {
                        (v)!![9] = DexReference()
                        ((((v)!![9] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![4]) as String?)))))
                        (v)!![11] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![11]))))
                        (v)!![11] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as StringBuilder?)))!!.append(dexD((v)!![11]))
                        (result)!![0] = (((dexR((v)!![9]) as StringBuilder?)))!!.append(((dexR((v)!![5]) as String?)))
                        (result)!![0] = (((dexR((v)!![9]) as StringBuilder?)))!!.toString()
                        (v)!![4] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setNumber(((dexR((v)!![4]) as String?)))
                        (v)!![4] = "png"
                        pc = (if (((dexI((v)!![3]) <= dexI((v)!![6])))) 0x124 else 0xf8)
                        continue
                }
                0xf8 -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getRemark()
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![5]) as CharSequence?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x124 else 0x102)
                        continue
                }
                0x102 -> {
                        (v)!![5] = dexN((dexI((v)!![3]) - dexI((v)!![6])))
                        (v)!![5] = dexN((dexI((v)!![5]) - dexI((v)!![8])))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![5])]
                        (v)!![9] = (result)!![0]
                        (v)!![9] = ((dexR((v)!![9]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![9]) as String?)))!!.startsWith((((dexR((v)!![4]) as String?)))!!))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![9])))) 0x124 else 0x111)
                        continue
                }
                0x111 -> {
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![5])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![1]) as RuleBill?)))!!.setRemark(((dexR((v)!![4]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![5])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![1]) as RuleBill?)))!!.setShopName(((dexR((v)!![4]) as String?)))
                        pc = 0x153
                        continue
                }
                0x124 -> {
                        (v)!![5] = dexN((dexI((v)!![6]) + (1)))
                        pc = (if (((dexI((v)!![3]) <= dexI((v)!![5])))) 0x153 else 0x128)
                        continue
                }
                0x128 -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getRemark()
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![5]) as CharSequence?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x153 else 0x132)
                        continue
                }
                0x132 -> {
                        (v)!![5] = dexN((dexI((v)!![3]) - dexI((v)!![6])))
                        (v)!![5] = dexN((dexI((v)!![5]) - dexI((v)!![7])))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![5])]
                        (v)!![7] = (result)!![0]
                        (v)!![7] = ((dexR((v)!![7]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![7]) as String?)))!!.startsWith((((dexR((v)!![4]) as String?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x153 else 0x141)
                        continue
                }
                0x141 -> {
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![5])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![1]) as RuleBill?)))!!.setRemark(((dexR((v)!![4]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![5])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![1]) as RuleBill?)))!!.setShopName(((dexR((v)!![4]) as String?)))
                        pc = 0x153
                        continue
                }
                0x153 -> {
                        (v)!![4] = dexN((dexI((v)!![3]) - dexI((v)!![6])))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.contains((((dexR((v)!![10]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x164 else 0x161)
                        continue
                }
                0x161 -> {
                        (((dexR((v)!![1]) as RuleBill?)))!!.setIncome(dexZ((v)!![8]))
                        pc = 0x164
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_t_block1(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x164 -> {
                        (v)!![6] = (v)!![16]
                        pc = 0x349
                        continue
                }
                0x168 -> {
                        (v)!![5] = "支付方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x190 else 0x170)
                        continue
                }
                0x170 -> {
                        (v)!![5] = "退款方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x190 else 0x178)
                        continue
                }
                0x178 -> {
                        (v)!![5] = "到账方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x190 else 0x180)
                        continue
                }
                0x180 -> {
                        (v)!![5] = "充值方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x190 else 0x188)
                        continue
                }
                0x188 -> {
                        (v)!![5] = "付款方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x1a3 else 0x190)
                        continue
                }
                0x190 -> {
                        (result)!![0] = dexN((((dexR((v)!![17]) as MutableList<String?>?)))!!.size)
                        (v)!![5] = (result)!![0]
                        (v)!![5] = dexN((dexI((v)!![5]) - dexI((v)!![7])))
                        pc = (if (((dexI((v)!![3]) >= dexI((v)!![5])))) 0x1a3 else 0x197)
                        continue
                }
                0x197 -> {
                        (v)!![4] = dexN((dexI((v)!![3]) + (2)))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![1]) as RuleBill?)))!!.setAsset(((dexR((v)!![4]) as String?)))
                        pc = 0x164
                        continue
                }
                0x1a3 -> {
                        (v)!![5] = "交易方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x1b3 else 0x1ab)
                        continue
                }
                0x1ab -> {
                        (v)!![5] = "入账账户"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x1c6 else 0x1b3)
                        continue
                }
                0x1b3 -> {
                        (result)!![0] = dexN((((dexR((v)!![17]) as MutableList<String?>?)))!!.size)
                        (v)!![5] = (result)!![0]
                        (v)!![5] = dexN((dexI((v)!![5]) - dexI((v)!![8])))
                        pc = (if (((dexI((v)!![3]) >= dexI((v)!![5])))) 0x1c6 else 0x1ba)
                        continue
                }
                0x1ba -> {
                        (v)!![4] = dexN((dexI((v)!![3]) + (1)))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![1]) as RuleBill?)))!!.setAsset(((dexR((v)!![4]) as String?)))
                        pc = 0x164
                        continue
                }
                0x1c6 -> {
                        (v)!![5] = "退款时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        (v)!![6] = "yyyy-MM-dd"
                        (v)!![9] = " "
                        (v)!![10] = dexN(-(1L))
                        (v)!![12] = "yyyy-MM-dd HH:mm:ss"
                        pc = (if ((!(dexZero((v)!![5])))) 0x1fe else 0x1d6)
                        continue
                }
                0x1d6 -> {
                        (v)!![5] = "过期时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x1fe else 0x1de)
                        continue
                }
                0x1de -> {
                        (v)!![5] = "入账时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x1fe else 0x1e6)
                        continue
                }
                0x1e6 -> {
                        (v)!![5] = "到账时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x1fe else 0x1ee)
                        continue
                }
                0x1ee -> {
                        (v)!![5] = "发起话费充值"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x1fe else 0x1f6)
                        continue
                }
                0x1f6 -> {
                        (v)!![5] = "充值时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x261 else 0x1fe)
                        continue
                }
                0x1fe -> {
                        (result)!![0] = dexN((((dexR((v)!![17]) as MutableList<String?>?)))!!.size)
                        (v)!![5] = (result)!![0]
                        (v)!![5] = dexN((dexI((v)!![5]) - dexI((v)!![8])))
                        pc = (if (((dexI((v)!![3]) >= dexI((v)!![5])))) 0x261 else 0x205)
                        continue
                }
                0x205 -> {
                        (v)!![4] = dexN((dexI((v)!![3]) + (1)))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![5] = (result)!![0]
                        (v)!![5] = ((dexR((v)!![5]) as String?))
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![5]) as String?)), ((dexR((v)!![12]) as String?))))
                        (v)!![13] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setTime(dexL((v)!![13]))
                        (result)!![0] = dexN((((dexR((v)!![1]) as RuleBill?)))!!.getTime())
                        (v)!![13] = (result)!![0]
                        (v)!![5] = dexN(java.lang.Long.compare(dexL((v)!![13]), dexL((v)!![10])))
                        pc = (if ((!(dexZero((v)!![5])))) 0x24a else 0x21c)
                        continue
                }
                0x21c -> {
                        (result)!![0] = dexN((((dexR((v)!![17]) as MutableList<String?>?)))!!.size)
                        (v)!![5] = (result)!![0]
                        (v)!![5] = dexN((dexI((v)!![5]) - dexI((v)!![7])))
                        pc = (if (((dexI((v)!![3]) >= dexI((v)!![5])))) 0x24a else 0x223)
                        continue
                }
                0x223 -> {
                        (v)!![5] = DexReference()
                        ((((v)!![5] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![7] = (result)!![0]
                        (v)!![7] = ((dexR((v)!![7]) as String?))
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.append(((dexR((v)!![7]) as String?)))
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.append(((dexR((v)!![9]) as String?)))
                        (v)!![7] = dexN((dexI((v)!![3]) + (2)))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![7])]
                        (v)!![7] = (result)!![0]
                        (v)!![7] = ((dexR((v)!![7]) as String?))
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.append(((dexR((v)!![7]) as String?)))
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.toString()
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![5]) as String?)), ((dexR((v)!![12]) as String?))))
                        (v)!![7] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setTime(dexL((v)!![7]))
                        pc = 0x24a
                        continue
                }
                0x24a -> {
                        (result)!![0] = dexN((((dexR((v)!![1]) as RuleBill?)))!!.getTime())
                        (v)!![7] = (result)!![0]
                        (v)!![5] = dexN(java.lang.Long.compare(dexL((v)!![7]), dexL((v)!![10])))
                        pc = (if ((!(dexZero((v)!![5])))) 0x164 else 0x252)
                        continue
                }
                0x252 -> {
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![4]) as String?)), ((dexR((v)!![6]) as String?))))
                        (v)!![4] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setTime(dexL((v)!![4]))
                        pc = 0x164
                        continue
                }
                0x261 -> {
                        (v)!![5] = "创建时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x271 else 0x269)
                        continue
                }
                0x269 -> {
                        (v)!![5] = "支付时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x2cd else 0x271)
                        continue
                }
                0x271 -> {
                        (result)!![0] = dexN((((dexR((v)!![17]) as MutableList<String?>?)))!!.size)
                        (v)!![5] = (result)!![0]
                        (v)!![5] = dexN((dexI((v)!![5]) - dexI((v)!![8])))
                        pc = (if (((dexI((v)!![3]) >= dexI((v)!![5])))) 0x2cd else 0x278)
                        continue
                }
                0x278 -> {
                        (v)!![4] = dexN((dexI((v)!![3]) + (1)))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![5] = (result)!![0]
                        (v)!![5] = ((dexR((v)!![5]) as String?))
                        (result)!![0] = dexN((((dexR((v)!![17]) as MutableList<String?>?)))!!.size)
                        (v)!![8] = (result)!![0]
                        (v)!![8] = dexN((dexI((v)!![8]) - dexI((v)!![7])))
                        pc = (if (((dexI((v)!![3]) >= dexI((v)!![8])))) 0x2af else 0x287)
                        continue
                }
                0x287 -> {
                        (v)!![7] = dexN((dexI((v)!![3]) + (2)))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![7])]
                        (v)!![7] = (result)!![0]
                        (v)!![7] = ((dexR((v)!![7]) as String?))
                        (v)!![8] = ":"
                        (result)!![0] = dexN((if (((((dexR((v)!![7]) as String?)))!!.contains((((dexR((v)!![8]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x2af else 0x297)
                        continue
                }
                0x297 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as String?)))!!.contains((((dexR((v)!![8]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![8])))) 0x2af else 0x29d)
                        continue
                }
                0x29d -> {
                        (v)!![8] = DexReference()
                        ((((v)!![8] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![8]) as StringBuilder?)))!!.append(((dexR((v)!![5]) as String?)))
                        (result)!![0] = (((dexR((v)!![8]) as StringBuilder?)))!!.append(((dexR((v)!![9]) as String?)))
                        (result)!![0] = (((dexR((v)!![8]) as StringBuilder?)))!!.append(((dexR((v)!![7]) as String?)))
                        (result)!![0] = (((dexR((v)!![8]) as StringBuilder?)))!!.toString()
                        (v)!![5] = (result)!![0]
                        pc = 0x2af
                        continue
                }
                0x2af -> {
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![5]) as String?)), ((dexR((v)!![12]) as String?))))
                        (v)!![7] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setTime(dexL((v)!![7]))
                        (result)!![0] = dexN((((dexR((v)!![1]) as RuleBill?)))!!.getTime())
                        (v)!![7] = (result)!![0]
                        (v)!![5] = dexN(java.lang.Long.compare(dexL((v)!![7]), dexL((v)!![10])))
                        pc = (if ((!(dexZero((v)!![5])))) 0x164 else 0x2be)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_t_block2(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x2be -> {
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![4]) as String?)), ((dexR((v)!![6]) as String?))))
                        (v)!![4] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setTime(dexL((v)!![4]))
                        pc = 0x164
                        continue
                }
                0x2cd -> {
                        (v)!![5] = "商品订单"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x164 else 0x2d5)
                        continue
                }
                0x2d5 -> {
                        (result)!![0] = dexN((((dexR((v)!![17]) as MutableList<String?>?)))!!.size)
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN((dexI((v)!![4]) - dexI((v)!![7])))
                        pc = (if (((dexI((v)!![3]) >= dexI((v)!![4])))) 0x164 else 0x2dc)
                        continue
                }
                0x2dc -> {
                        (v)!![4] = dexN((dexI((v)!![3]) + (1)))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![1]) as RuleBill?)))!!.setRemark(((dexR((v)!![2]) as String?)))
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x2fd else 0x2ed)
                        continue
                }
                0x2ed -> {
                        (v)!![5] = "~240x0"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.endsWith((((dexR((v)!![5]) as String?)))!!))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x305 else 0x2f5)
                        continue
                }
                0x2f5 -> {
                        (v)!![5] = "&x-expires="
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.contains((((dexR((v)!![5]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x305 else 0x2fd)
                        continue
                }
                0x2fd -> {
                        (v)!![5] = "在此商户的交易"
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as String?)))!!.equals(((dexR((v)!![4]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x311 else 0x305)
                        continue
                }
                0x305 -> {
                        (v)!![4] = dexN((dexI((v)!![3]) + (2)))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (((dexR((v)!![1]) as RuleBill?)))!!.setRemark(((dexR((v)!![4]) as String?)))
                        pc = 0x334
                        continue
                }
                0x311 -> {
                        (v)!![5] = "申请电子回单"
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as String?)))!!.equals(((dexR((v)!![4]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x348 else 0x319)
                        continue
                }
                0x319 -> {
                        (v)!![5] = "查看月付账单"
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as String?)))!!.equals(((dexR((v)!![4]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x348 else 0x321)
                        continue
                }
                0x321 -> {
                        (v)!![5] = "联系商家"
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as String?)))!!.equals(((dexR((v)!![4]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x348 else 0x329)
                        continue
                }
                0x329 -> {
                        (v)!![5] = "申请电子交易凭证"
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as String?)))!!.equals(((dexR((v)!![4]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x348 else 0x331)
                        continue
                }
                0x331 -> {
                        (((dexR((v)!![1]) as RuleBill?)))!!.setRemark(((dexR((v)!![4]) as String?)))
                        pc = 0x334
                        continue
                }
                0x334 -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getRemark()
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x164 else 0x33e)
                        continue
                }
                0x33e -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getRemark()
                        (v)!![4] = (result)!![0]
                        (v)!![6] = (v)!![16]
                        RuleBridge.E(((dexR((v)!![6]) as RuleContext?)), ((dexR((v)!![4]) as String?)))
                        pc = 0x349
                        continue
                }
                0x348 -> {
                        (result)!![1] = (v)!![2]
                        return -(1)
                }
                0x349 -> {
                        (v)!![3] = dexN((dexI((v)!![3]) + (1)))
                        pc = 0x15
                        continue
                }
                0x34d -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getAsset()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![0]) as CharSequence?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x37e else 0x357)
                        continue
                }
                0x357 -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getAsset()
                        (v)!![0] = (result)!![0]
                        (v)!![3] = "抖音支付"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((dexZero((v)!![0]))) 0x37e else 0x363)
                        continue
                }
                0x363 -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getAsset()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![0] = (result)!![0]
                        (v)!![3] = "("
                        (result)!![0] = (((dexR((v)!![0]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![0] = (result)!![0]
                        (v)!![3] = ")"
                        (result)!![0] = (((dexR((v)!![0]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![5]) as CharSequence?)))!!)
                        (v)!![0] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as String?)))!!.trim()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setAsset(((dexR((v)!![0]) as String?)))
                        pc = 0x37e
                        continue
                }
                0x37e -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getRemark()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![0]) as CharSequence?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x393 else 0x388)
                        continue
                }
                0x388 -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getNumber()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![0]) as CharSequence?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x393 else 0x392)
                        continue
                }
                0x392 -> {
                        (result)!![1] = (v)!![1]
                        return -(1)
                }
                0x393 -> {
                        (result)!![1] = (v)!![2]
                        return -(1)
                }
                else -> {
                    return pc
                }
            }
        }
    }

    fun u(list: MutableList<String?>?): RuleBill? {
        if ((list)!!.isEmpty()) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setTransfer(true)
        (billInfo)!!.setOrigin("抖音")
        (billInfo)!!.setToAsset("抖音月付")
        (billInfo)!!.setRemark("抖音月付还款")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((((str)!!.equals("支付成功") || (str)!!.equals("还款成功"))) && (i6 > 0))) {
                        var strReplace: String? = ((((list)!![(i6 - 1)])!!.replace(("¥")!!, ("")!!))!!.replace((",")!!, ("")!!))!!.replace(("+")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace))).toString() + ""))
                        }
                        if (((str)!!.equals("还款成功") && (i6 > 1))) {
                            (billInfo)!!.setRemark((list)!![(i6 - 2)])
                        }
                    } else if (((((str)!!.equals("还款方式") || (str)!!.equals("付款方式"))) && (i6 < ((list)!!.size - 2)))) {
                        (billInfo)!!.setFromAsset((((((list)!![(i6 + 2)])!!.replace(("抖音支付")!!, ("")!!))!!.replace(("(")!!, ("")!!))!!.replace((")")!!, ("")!!))!!.trim())
                    } else if (((str)!!.equals("还款到") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setToAsset(((((list)!![(i6 + 1)])!!.replace(("(")!!, ("")!!))!!.replace((")")!!, ("")!!))!!.trim())
                    } else if (((((str)!!.equals("还款时间") || (str)!!.equals("发起时间"))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setTime(Dates.X0((list)!![(i6 + 1)], "yyyy-MM-dd HH:mm:ss"))
                    }
                i6++
            }
        }
        if (TextUtils.isEmpty((billInfo)!!.getNumber())) {
            return null
        }
        return billInfo
    }

    fun w(list: MutableList<CaptureNode>?): Boolean {
        return (if ((((list == null) || (list)!!.isEmpty()))) false else true)
    }

    companion object {
        private var f13753k: DouyinRule? = null

        private var f13754l: Int = 4

        private var f13755m: Int = 5

        fun v(): DouyinRule? {
            if ((f13753k == null)) {
                f13753k = DouyinRule()
            }
            return f13753k
        }
    }
}
