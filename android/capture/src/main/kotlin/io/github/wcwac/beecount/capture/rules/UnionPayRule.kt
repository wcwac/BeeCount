@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 f869137cff375c7b384bd34111c81fb743681c2ef33dbc3cd095c840a50d0652.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class UnionPayRule : RuleBase(), RuleCallback {
    var f13766e: String? = null

    var f13767f: String? = null

    private var f13768g: Boolean = false

    private fun r(context: RuleContext?, list: MutableList<String?>?, map: HashMap<String?, Boolean>?): Unit {
        var billInfoX: RuleBill? = null
        var iIndexOf: Int = 0
        var iIndexOf2: Int = 0


        if (((list == null) || (list)!!.isEmpty())) {
            return
        }
        var zContainsKey: Boolean = ((map)!! as Map<Any?, Boolean>).containsKey("支付成功")
        if ((this.a == 12)) {
            if ((((map)!! as Map<Any?, Boolean>).containsKey("付款卡") && (((((list)!! as List<Any?>).indexOf("付款卡")).also { iIndexOf2 = it }) < ((list)!!.size - 1)))) {
                this.f13767f = (list)!![(iIndexOf2 + 1)]
            }
            if ((((map)!! as Map<Any?, Boolean>).containsKey("收款卡") && (((((list)!! as List<Any?>).indexOf("收款卡")).also { iIndexOf = it }) < ((list)!!.size - 1)))) {
                this.f13766e = (list)!![(iIndexOf + 1)]
            }
        }
        if ((!(zContainsKey) && (this.a == 12))) {
            zContainsKey = true
        }
        if ((!(zContainsKey) && (this.a == 5))) {
            zContainsKey = true
        }
        if (zContainsKey) {

            var i6: Int = this.a
            if ((i6 == 4)) {
                billInfoX = w(list)
            } else if ((i6 != 5)) {
                billInfoX = (if ((i6 == 12)) x(list, this.f13767f, this.f13766e) else null)
            } else if ((a(list, "信用卡号", true) && a(list, "还款:", false))) {
                billInfoX = x(list, null, null)
            } else if ((a(list, "订单状态", true) || !(a(list, "订单状态", false)))) {
                billInfoX = (if (((a(list, "当前状态", true) && (((a(list, "订单时间", true) || a(list, "扣款时间", true)) || a(list, "退款时间", true)))))) v(list) else t(list))
            } else {
                billInfoX = u(list)
            }
            if ((billInfoX != null)) {
                this.f13767f = null
                this.f13766e = null
                this.b = false
                this.f13768g = false
                (billInfoX)!!.setTagList(RuleTags.d(this.f13681d))
                RuleBridge.l(context, billInfoX, this)
            }
        }
    }

    override fun dismiss(): Unit {
        this.f13767f = null
        this.f13766e = null
        this.b = false
    }

    fun q(str: String?, str2: String?): Boolean {
        return ((str)!!.equals("com.unionpay") && ((((str2)!!.equals("com.unionpay.activity.UPActivityMain") || (str2)!!.equals("com.unionpay.activity.payment.UPActivityScan")) || (str2)!!.equals("com.unionpay.activity.payment.UPActivityPaymentQrCodeOut"))))
    }

    fun s(arg0: RuleContext?, arg1: String?, arg2: CaptureNode?): Unit {
        var v: Array<Any?>? = arrayOfNulls<Any>(13)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![9] = this
        (v)!![10] = arg0
        (v)!![11] = arg1
        (v)!![12] = arg2
        while (true) {
            if ((pc == -(1))) {
                return
            }
            if ((pc < 0xf8)) pc = recovered_s_block0(v, result, pc) else pc = recovered_s_block1(v, result, pc)
        }
    }

    private fun recovered_s_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (v)!![0] = "com.unionpay.activity.UPActivityMain"
                        (result)!![0] = dexN((if (((((dexR((v)!![11]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        (v)!![1] = dexN(0L)
                        pc = (if ((dexZero((v)!![0]))) 0x11 else 0x9)
                        continue
                }
                0x9 -> {
                        (((dexR((v)!![9]) as RuleBase?)))!!.a = dexI((v)!![1])
                        (((dexR((v)!![9]) as UnionPayRule?)))!!.f13768g = dexZ((v)!![1])
                        (((dexR((v)!![9]) as RuleBase?)))!!.b = dexZ((v)!![1])
                        pc = 0x151
                        continue
                }
                0x11 -> {
                        (v)!![0] = "com.unionpay.activity.payment.UPActivityScan"
                        (result)!![0] = dexN((if (((((dexR((v)!![11]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        (v)!![2] = dexN(4L)
                        (v)!![3] = dexN(1L)
                        pc = (if ((!(dexZero((v)!![0])))) 0x14d else 0x1b)
                        continue
                }
                0x1b -> {
                        (v)!![0] = "com.unionpay.activity.payment.UPActivityPaymentQrCodeOut"
                        (result)!![0] = dexN((if (((((dexR((v)!![11]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x25 else 0x23)
                        continue
                }
                0x23 -> {
                        pc = 0x14d
                        continue
                }
                0x25 -> {
                        pc = (if ((dexZero((v)!![12]))) 0x151 else 0x27)
                        continue
                }
                0x27 -> {
                        (v)!![11] = "start get data"
                        (v)!![0] = "sssss"
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![0]) as Any?)), ((dexR((v)!![11]) as Any?)))
                        (v)!![11] = (result)!![0]

                        (result)!![0] = (((dexR((v)!![9]) as RuleBase?)))!!.g(((dexR((v)!![12]) as CaptureNode?)))
                        (v)!![11] = (result)!![0]
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = HashMap<String?, Boolean>()
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!!.iterator()
                        (v)!![5] = (result)!![0]
                        pc = 0x3f
                        continue
                }
                0x3f -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as Iterator<String?>?)))!!.hasNext())) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((dexZero((v)!![6]))) 0x51 else 0x45)
                        continue
                }
                0x45 -> {
                        (result)!![0] = (((dexR((v)!![5]) as Iterator<String?>?)))!!.next()
                        (v)!![6] = (result)!![0]
                        (v)!![6] = ((dexR((v)!![6]) as String?))
                        (v)!![7] = true
                        (result)!![0] = (((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!!.put((((dexR((v)!![6]) as Any?)) as String?), (((dexR((v)!![7]) as Any?)) as Boolean))
                        pc = 0x3f
                        continue
                }
                0x51 -> {
                        (v)!![5] = "支付成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x67 else 0x59)
                        continue
                }
                0x59 -> {
                        (v)!![5] = "付款方式"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x67 else 0x61)
                        continue
                }
                0x61 -> {
                        (((dexR((v)!![9]) as RuleBase?)))!!.a = dexI((v)!![2])
                        (((dexR((v)!![9]) as RuleBase?)))!!.b = dexZ((v)!![3])
                        pc = 0x153
                        continue
                }
                0x67 -> {
                        (v)!![2] = "账单详情"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![2]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        (v)!![6] = dexN(12L)
                        pc = (if ((dexZero((v)!![5]))) 0x87 else 0x71)
                        continue
                }
                0x71 -> {
                        (v)!![5] = "转给本人"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x87 else 0x79)
                        continue
                }
                0x79 -> {
                        (v)!![5] = "转账金额"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x87 else 0x81)
                        continue
                }
                0x81 -> {
                        (((dexR((v)!![9]) as RuleBase?)))!!.a = dexI((v)!![6])
                        (((dexR((v)!![9]) as RuleBase?)))!!.b = dexZ((v)!![3])
                        pc = 0x153
                        continue
                }
                0x87 -> {
                        (v)!![5] = "实收金额（元）"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x147 else 0x8f)
                        continue
                }
                0x8f -> {
                        (v)!![5] = "实付金额（元）"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x147 else 0x97)
                        continue
                }
                0x97 -> {
                        (v)!![5] = "当前状态"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0xa7 else 0x9f)
                        continue
                }
                0x9f -> {
                        (v)!![7] = "订单时间"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![7])))) 0x147 else 0xa7)
                        continue
                }
                0xa7 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0xb5 else 0xad)
                        continue
                }
                0xad -> {
                        (v)!![7] = "扣款时间"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![7])))) 0x147 else 0xb5)
                        continue
                }
                0xb5 -> {
                        (v)!![7] = "交易详情"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((dexZero((v)!![8]))) 0xc5 else 0xbd)
                        continue
                }
                0xbd -> {
                        (v)!![8] = "时间"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![8]) as Any?))))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![8])))) 0x147 else 0xc5)
                        continue
                }
                0xc5 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0xd3 else 0xcb)
                        continue
                }
                0xcb -> {
                        (v)!![7] = "交易时间"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![7])))) 0x147 else 0xd3)
                        continue
                }
                0xd3 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0xe2 else 0xd9)
                        continue
                }
                0xd9 -> {
                        (v)!![5] = "退款时间"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0xe2 else 0xe1)
                        continue
                }
                0xe1 -> {
                        pc = 0x147
                        continue
                }
                0xe2 -> {
                        (v)!![5] = "转账成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0xf2 else 0xea)
                        continue
                }
                0xea -> {
                        (v)!![5] = "完成"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x100 else 0xf2)
                        continue
                }
                0xf2 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![2]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((dexZero((v)!![2]))) 0x105 else 0xf8)
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
                0xf8 -> {
                        (v)!![2] = "转出成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![2]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((dexZero((v)!![2]))) 0x105 else 0x100)
                        continue
                }
                0x100 -> {
                        (((dexR((v)!![9]) as RuleBase?)))!!.a = dexI((v)!![6])
                        (((dexR((v)!![9]) as RuleBase?)))!!.b = dexZ((v)!![3])
                        pc = 0x153
                        continue
                }
                0x105 -> {
                        (v)!![2] = "本次还款金额"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![2]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((dexZero((v)!![2]))) 0x153 else 0x10d)
                        continue
                }
                0x10d -> {
                        (v)!![2] = "本期账单"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![2]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((dexZero((v)!![2]))) 0x153 else 0x115)
                        continue
                }
                0x115 -> {
                        (v)!![2] = "立即还"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![2]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((dexZero((v)!![2]))) 0x153 else 0x11d)
                        continue
                }
                0x11d -> {
                        (v)!![2] = "分期还"
                        (result)!![0] = dexN((if ((((((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![2]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((dexZero((v)!![2]))) 0x153 else 0x125)
                        continue
                }
                0x125 -> {
                        (v)!![2] = "["
                        (result)!![0] = dexN((((dexR((v)!![9]) as RuleBase?)))!!.f(((dexR((v)!![11]) as MutableList<String?>?)), ((dexR((v)!![2]) as String?)), dexZ((v)!![1])))
                        (v)!![1] = (result)!![0]
                        pc = (if (((dexI((v)!![1]) < 0))) 0x137 else 0x12d)
                        continue
                }
                0x12d -> {
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![9]) as UnionPayRule?)))!!.f13766e = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![9]) as UnionPayRule?)))!!.f13768g = dexZ((v)!![3])
                        pc = 0x137
                        continue
                }
                0x137 -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![9]) as UnionPayRule?)))!!.f13768g)) 1 else 0))
                        (result)!![0] = java.lang.Boolean.valueOf(dexZ((v)!![1]))
                        (v)!![1] = (result)!![0]
                        (v)!![2] = (((dexR((v)!![9]) as UnionPayRule?)))!!.f13766e
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![0]) as Any?)), ((dexR((v)!![1]) as Any?)), ((dexR((v)!![2]) as Any?)))
                        (v)!![0] = (result)!![0]

                        pc = 0x153
                        continue
                }
                0x147 -> {
                        (v)!![0] = dexN(5L)
                        (((dexR((v)!![9]) as RuleBase?)))!!.a = dexI((v)!![0])
                        (((dexR((v)!![9]) as RuleBase?)))!!.b = dexZ((v)!![3])
                        pc = 0x153
                        continue
                }
                0x14d -> {
                        (((dexR((v)!![9]) as RuleBase?)))!!.a = dexI((v)!![2])
                        (((dexR((v)!![9]) as RuleBase?)))!!.b = dexZ((v)!![3])
                        pc = 0x151
                        continue
                }
                0x151 -> {
                        (v)!![11] = dexN(0L)
                        (v)!![4] = (v)!![11]
                        pc = 0x153
                        continue
                }
                0x153 -> {
                        (v)!![0] = dexN((if (((((dexR((v)!![9]) as RuleBase?)))!!.b)) 1 else 0))
                        pc = (if ((dexZero((v)!![0]))) 0x17d else 0x157)
                        continue
                }
                0x157 -> {
                        pc = (if ((dexZero((v)!![12]))) 0x17d else 0x159)
                        continue
                }
                0x159 -> {
                        pc = (if ((!(dexZero((v)!![11])))) 0x17a else 0x15b)
                        continue
                }
                0x15b -> {
                        (result)!![0] = (((dexR((v)!![9]) as RuleBase?)))!!.g(((dexR((v)!![12]) as CaptureNode?)))
                        (v)!![11] = (result)!![0]
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = HashMap<String?, Boolean>()
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!!.iterator()
                        (v)!![12] = (result)!![0]
                        pc = 0x168
                        continue
                }
                0x168 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as Iterator<String?>?)))!!.hasNext())) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((dexZero((v)!![0]))) 0x17a else 0x16e)
                        continue
                }
                0x16e -> {
                        (result)!![0] = (((dexR((v)!![12]) as Iterator<String?>?)))!!.next()
                        (v)!![0] = (result)!![0]
                        (v)!![0] = ((dexR((v)!![0]) as String?))
                        (v)!![1] = true
                        (result)!![0] = (((dexR((v)!![4]) as HashMap<String?, Boolean>?)))!!.put((((dexR((v)!![0]) as Any?)) as String?), (((dexR((v)!![1]) as Any?)) as Boolean))
                        pc = 0x168
                        continue
                }
                0x17a -> {
                        (((dexR((v)!![9]) as UnionPayRule?)))!!.r(((dexR((v)!![10]) as RuleContext?)), ((dexR((v)!![11]) as MutableList<String?>?)), ((dexR((v)!![4]) as HashMap<String?, Boolean>?)))
                        pc = 0x17d
                        continue
                }
                0x17d -> {
                        return -(1)
                }
                else -> {
                    return pc
                }
            }
        }
    }

    fun t(list: MutableList<String?>?): RuleBill? {
        if ((list)!!.isEmpty()) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("云闪付")
        var z5: Boolean = false
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((((str)!!.equals("支出金额") || (str)!!.equals("订单金额"))) && (i6 < ((list)!!.size - 1)))) {
                        var strReplace: String? = (((list)!![(i6 + 1)])!!.replace(("元")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && Amounts.J(strReplace))) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace))).toString() + ""))
                        }
                    } else if (((((str)!!.equals("实付金额（元）") || (str)!!.equals("实收金额（元）"))) && (i6 < ((list)!!.size - 1)))) {
                        var strReplace2: String? = (((list)!![(i6 + 1)])!!.replace(("元")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace2)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace2))).toString() + ""))
                        }
                    } else if (((str)!!.equals("收入金额") && (i6 < ((list)!!.size - 1)))) {
                        var strReplace3: String? = (((list)!![(i6 + 1)])!!.replace(("元")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace3)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace3))).toString() + ""))
                        }
                        (billInfo)!!.setIncome(true)
                        z5 = true
                    } else if (((str)!!.equals("交易详情") && (i6 < ((list)!!.size - 2)))) {
                        var strReplace4: String? = ((((list)!![(i6 + 2)])!!.replace(("¥")!!, ("")!!))!!.replace((",")!!, ("")!!))!!.replace(("+")!!, ("")!!)
                        if (Amounts.J(strReplace4)) {
                            var dS: Double = Amounts.S(strReplace4)
                            (billInfo)!!.setNumber(((Math.abs(dS)).toString() + ""))
                            if ((dS > 0.0)) {
                                (billInfo)!!.setIncome(true)
                                z5 = true
                            }
                        }
                        var i7: Int = (i6 + 1)
                        (billInfo)!!.setRemark((list)!![i7])
                        (billInfo)!!.setShopName((list)!![i7])
                    } else if (((((str)!!.equals("商户名称") || (str)!!.equals("乘车线路"))) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setShopName((list)!![(i6 + 1)])
                    } else if ((((str)!!.equals("交易类型") && TextUtils.isEmpty((billInfo)!!.getShopName())) && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setShopName((list)!![(i6 + 1)])
                    } else if (((str)!!.equals("卡号") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset(((list)!![(i6 + 1)])!!.replace(("尾号")!!, ("")!!))
                        if ((i6 < ((list)!!.size - 2))) {
                            var i8: Int = (i6 + 2)
                            if (((list)!![i8])!!.contains(("****")!!)) {
                                (billInfo)!!.setAsset(((billInfo)!!.getAsset() + ((list)!![i8])!!.substring((((list)!![i8])!!.indexOf(("****")!!) + 4))))
                            }
                        }
                    } else if (((str)!!.equals("付款卡") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset((list)!![(i6 + 1)])
                    } else if (((str)!!.equals("收款卡") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset((list)!![(i6 + 1)])
                    } else if (((str)!!.equals("付款卡号") && (i6 < ((list)!!.size - 1)))) {
                        var str2: String? = (list)!![(i6 + 1)]
                        (billInfo)!!.setAsset((str2)!!.substring(((str2)!!.length - 4)))
                    } else if (((((((str)!!.equals("时间") || (str)!!.equals("订单时间")) || (str)!!.equals("扣款时间")) || (str)!!.equals("交易时间"))) && (i6 < ((list)!!.size - 1)))) {
                        var i9: Int = (i6 + 1)
                        (billInfo)!!.setTime(Dates.X0((list)!![i9], "yyyy-MM-dd HH:mm"))
                        if ((((billInfo)!!.getTime() == -(1)) || ((billInfo)!!.getTime() == 0))) {
                            (billInfo)!!.setTime(Dates.X0((list)!![i9], "yyyy-MM-dd HH:mm:ss"))
                        }
                    } else if (((str)!!.equals("分类") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setRemark((list)!![(i6 + 1)])
                    }
                i6++
            }
        }
        if (TextUtils.isEmpty((billInfo)!!.getRemark())) {
            if (TextUtils.isEmpty((billInfo)!!.getShopName())) {
                (billInfo)!!.setRemark((billInfo)!!.getOrigin())
            } else {
                (billInfo)!!.setRemark((billInfo)!!.getShopName())
            }
        }
        if (z5) {
            (billInfo)!!.setShopName(("收入-" + (billInfo)!!.getShopName()))
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun u(arg0: MutableList<String?>?): RuleBill? {
        var v: Array<Any?>? = arrayOfNulls<Any>(22)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![20] = this
        (v)!![21] = arg0
        while (true) {
            if ((pc == -(1))) {
                return ((dexR((result)!![1]) as RuleBill?))
            }
            if ((pc < 0x156)) pc = recovered_u_block0(v, result, pc) else pc = recovered_u_block1(v, result, pc)
        }
    }

    private fun recovered_u_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (v)!![0] = (v)!![21]
                        (result)!![0] = dexN((((dexR((v)!![21]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![2] = dexN(0L)
                        pc = (if ((!(dexZero((v)!![1])))) 0xa else 0x9)
                        continue
                }
                0x9 -> {
                        (result)!![1] = (v)!![2]
                        return -(1)
                }
                0xa -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = RuleBill()
                        (v)!![3] = "云闪付"
                        (((dexR((v)!![1]) as RuleBill?)))!!.setOrigin(((dexR((v)!![3]) as String?)))
                        (v)!![4] = dexN(0L)
                        (v)!![5] = dexN(0L)
                        pc = 0x16
                        continue
                }
                0x16 -> {
                        (result)!![0] = dexN((((dexR((v)!![21]) as MutableList<String?>?)))!!.size)
                        (v)!![6] = (result)!![0]
                        pc = (if (((dexI((v)!![4]) >= dexI((v)!![6])))) 0x20f else 0x1c)
                        continue
                }
                0x1c -> {
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![6] = (result)!![0]
                        (v)!![6] = ((dexR((v)!![6]) as String?))
                        (v)!![7] = "实付金额（元）"
                        (result)!![0] = dexN((if (((((dexR((v)!![6]) as String?)))!!.equals(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        (v)!![8] = "实收金额（元）"
                        (v)!![9] = dexN(1L)
                        (v)!![10] = ""
                        pc = (if ((!(dexZero((v)!![7])))) 0x35 else 0x2f)
                        continue
                }
                0x2f -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![6]) as String?)))!!.equals(((dexR((v)!![8]) as Any?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x7c else 0x35)
                        continue
                }
                0x35 -> {
                        (result)!![0] = dexN((((dexR((v)!![21]) as MutableList<String?>?)))!!.size)
                        (v)!![7] = (result)!![0]
                        (v)!![7] = dexN((dexI((v)!![7]) - dexI((v)!![9])))
                        pc = (if (((dexI((v)!![4]) >= dexI((v)!![7])))) 0x7c else 0x3c)
                        continue
                }
                0x3c -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![6]) as String?)))!!.equals(((dexR((v)!![8]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((dexZero((v)!![6]))) 0x46 else 0x42)
                        continue
                }
                0x42 -> {
                        (((dexR((v)!![1]) as RuleBill?)))!!.setIncome(dexZ((v)!![9]))
                        (v)!![5] = (v)!![9]
                        pc = 0x46
                        continue
                }
                0x46 -> {
                        (v)!![6] = dexN((dexI((v)!![4]) + (1)))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![6])]
                        (v)!![6] = (result)!![0]
                        (v)!![6] = ((dexR((v)!![6]) as String?))
                        (v)!![7] = "元"
                        (result)!![0] = (((dexR((v)!![6]) as String?)))!!.replace((((dexR((v)!![7]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![6] = (result)!![0]
                        (v)!![7] = ","
                        (result)!![0] = (((dexR((v)!![6]) as String?)))!!.replace((((dexR((v)!![7]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![6] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![6]) as String?))))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x20b else 0x60)
                        continue
                }
                0x60 -> {
                        (v)!![7] = DexReference()
                        ((((v)!![7] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![6]) as String?)))))
                        (v)!![8] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![8]))))
                        (v)!![8] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![7]) as StringBuilder?)))!!.append(dexD((v)!![8]))
                        (result)!![0] = (((dexR((v)!![7]) as StringBuilder?)))!!.append(((dexR((v)!![10]) as String?)))
                        (result)!![0] = (((dexR((v)!![7]) as StringBuilder?)))!!.toString()
                        (v)!![6] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setNumber(((dexR((v)!![6]) as String?)))
                        pc = 0x20b
                        continue
                }
                0x7c -> {
                        (v)!![7] = "订单状态"
                        (result)!![0] = dexN((if (((((dexR((v)!![6]) as String?)))!!.contains((((dexR((v)!![7]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x20b else 0x84)
                        continue
                }
                0x84 -> {
                        (v)!![7] = "交易类型"
                        (result)!![0] = dexN((if (((((dexR((v)!![6]) as String?)))!!.contains((((dexR((v)!![7]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((dexZero((v)!![8]))) 0x20b else 0x8c)
                        continue
                }
                0x8c -> {
                        (v)!![8] = " "
                        (result)!![0] = javaSplit((((dexR((v)!![6]) as String?)))!!, (((dexR((v)!![8]) as String?)))!!)
                        (v)!![6] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![6]) as Array<Any?>?)))!!.toMutableList()
                        (v)!![6] = (result)!![0]
                        (result)!![0] = dexN((((dexR((v)!![6]) as MutableList<String?>?)))!!.size)
                        (v)!![11] = (result)!![0]
                        pc = (if (((dexI((v)!![11]) <= 0))) 0x20b else 0x9c)
                        continue
                }
                0x9c -> {
                        (v)!![11] = dexN(0L)
                        pc = 0x9d
                        continue
                }
                0x9d -> {
                        (result)!![0] = dexN((((dexR((v)!![6]) as MutableList<String?>?)))!!.size)
                        (v)!![12] = (result)!![0]
                        pc = (if (((dexI((v)!![11]) >= dexI((v)!![12])))) 0x20b else 0xa3)
                        continue
                }
                0xa3 -> {
                        (result)!![0] = (((dexR((v)!![6]) as MutableList<String?>?)))!![dexI((v)!![11])]
                        (v)!![12] = (result)!![0]
                        (v)!![12] = ((dexR((v)!![12]) as String?))
                        (v)!![13] = "商户名称"
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as String?)))!!.contains((((dexR((v)!![13]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0xbc else 0xb1)
                        continue
                }
                0xb1 -> {
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![12] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setShopName(((dexR((v)!![12]) as String?)))
                        pc = 0xb8
                        continue
                }
                0xb8 -> {
                        (v)!![17] = (v)!![10]
                        pc = 0x204
                        continue
                }
                0xbc -> {
                        (v)!![13] = "乘车线路"
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as String?)))!!.contains((((dexR((v)!![13]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0xcc else 0xc4)
                        continue
                }
                0xc4 -> {
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![12] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setShopName(((dexR((v)!![12]) as String?)))
                        pc = 0xb8
                        continue
                }
                0xcc -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as String?)))!!.contains((((dexR((v)!![7]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0xe4 else 0xd2)
                        continue
                }
                0xd2 -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getShopName()
                        (v)!![13] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![13]) as CharSequence?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0xe4 else 0xdc)
                        continue
                }
                0xdc -> {
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![7]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![12] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setShopName(((dexR((v)!![12]) as String?)))
                        pc = 0xb8
                        continue
                }
                0xe4 -> {
                        (v)!![13] = "付款卡号"
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as String?)))!!.contains((((dexR((v)!![13]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0xfe else 0xec)
                        continue
                }
                0xec -> {
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![12] = (result)!![0]
                        (result)!![0] = dexN((((dexR((v)!![12]) as String?)))!!.length)
                        (v)!![13] = (result)!![0]
                        (v)!![13] = dexN((dexI((v)!![13]) + (-(4))))
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.substring(dexI((v)!![13]))
                        (v)!![12] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setAsset(((dexR((v)!![12]) as String?)))
                        pc = 0xb8
                        continue
                }
                0xfe -> {
                        (v)!![13] = "信用卡卡号"
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as String?)))!!.contains((((dexR((v)!![13]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x118 else 0x106)
                        continue
                }
                0x106 -> {
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![12] = (result)!![0]
                        (result)!![0] = dexN((((dexR((v)!![12]) as String?)))!!.length)
                        (v)!![13] = (result)!![0]
                        (v)!![13] = dexN((dexI((v)!![13]) + (-(4))))
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.substring(dexI((v)!![13]))
                        (v)!![12] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setAsset(((dexR((v)!![12]) as String?)))
                        pc = 0xb8
                        continue
                }
                0x118 -> {
                        (v)!![13] = "转入卡号"
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as String?)))!!.contains((((dexR((v)!![13]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x132 else 0x120)
                        continue
                }
                0x120 -> {
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![12] = (result)!![0]
                        (result)!![0] = dexN((((dexR((v)!![12]) as String?)))!!.length)
                        (v)!![13] = (result)!![0]
                        (v)!![13] = dexN((dexI((v)!![13]) + (-(4))))
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.substring(dexI((v)!![13]))
                        (v)!![12] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setAsset(((dexR((v)!![12]) as String?)))
                        pc = 0xb8
                        continue
                }
                0x132 -> {
                        (v)!![13] = "银行名称"
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as String?)))!!.contains((((dexR((v)!![13]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x156 else 0x13a)
                        continue
                }
                0x13a -> {
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![12] = (result)!![0]
                        (v)!![13] = DexReference()
                        ((((v)!![13] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![13]) as StringBuilder?)))!!.append(((dexR((v)!![12]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getAsset()
                        (v)!![12] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![13]) as StringBuilder?)))!!.append(((dexR((v)!![12]) as String?)))
                        (result)!![0] = (((dexR((v)!![13]) as StringBuilder?)))!!.toString()
                        (v)!![12] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setAsset(((dexR((v)!![12]) as String?)))
                        pc = 0xb8
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_u_block1(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x156 -> {
                        (v)!![13] = "付款卡"
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as String?)))!!.contains((((dexR((v)!![13]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x167 else 0x15e)
                        continue
                }
                0x15e -> {
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![12] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setAsset(((dexR((v)!![12]) as String?)))
                        pc = 0xb8
                        continue
                }
                0x167 -> {
                        (v)!![13] = "收款卡"
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as String?)))!!.contains((((dexR((v)!![13]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x178 else 0x16f)
                        continue
                }
                0x16f -> {
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![12] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setAsset(((dexR((v)!![12]) as String?)))
                        pc = 0xb8
                        continue
                }
                0x178 -> {
                        (v)!![13] = "订单时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as String?)))!!.contains((((dexR((v)!![13]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0xb8 else 0x180)
                        continue
                }
                0x180 -> {
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![10]) as CharSequence?)))!!)
                        (v)!![12] = (result)!![0]
                        (result)!![0] = dexN((((dexR((v)!![6]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![9])))
                        (v)!![14] = "yyyy-MM-dd HH:mm:ss"
                        (v)!![15] = dexN(-(1L))
                        pc = (if (((dexI((v)!![11]) >= dexI((v)!![13])))) 0x1bf else 0x18f)
                        continue
                }
                0x18f -> {
                        (v)!![13] = dexN((dexI((v)!![11]) + (1)))
                        (result)!![0] = (((dexR((v)!![6]) as MutableList<String?>?)))!![dexI((v)!![13])]
                        (v)!![13] = (result)!![0]
                        (v)!![13] = ((dexR((v)!![13]) as String?))
                        (v)!![3] = ":"
                        (result)!![0] = dexN((if (((((dexR((v)!![13]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x1bf else 0x19f)
                        continue
                }
                0x19f -> {
                        (v)!![3] = DexReference()
                        ((((v)!![3] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![3]) as StringBuilder?)))!!.append(((dexR((v)!![12]) as String?)))
                        (result)!![0] = (((dexR((v)!![3]) as StringBuilder?)))!!.append(((dexR((v)!![8]) as String?)))
                        (result)!![0] = (((dexR((v)!![3]) as StringBuilder?)))!!.append(((dexR((v)!![13]) as String?)))
                        (result)!![0] = (((dexR((v)!![3]) as StringBuilder?)))!!.toString()
                        (v)!![3] = (result)!![0]
                        (v)!![17] = (v)!![10]
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![3]) as String?)), ((dexR((v)!![14]) as String?))))
                        (v)!![9] = (result)!![0]
                        (v)!![3] = dexN(java.lang.Long.compare(dexL((v)!![9]), dexL((v)!![15])))
                        pc = (if ((dexZero((v)!![3]))) 0x1c1 else 0x1bb)
                        continue
                }
                0x1bb -> {
                        (((dexR((v)!![1]) as RuleBill?)))!!.setTime(dexL((v)!![9]))
                        pc = 0x1c1
                        continue
                }
                0x1bf -> {
                        (v)!![17] = (v)!![10]
                        pc = 0x1c1
                        continue
                }
                0x1c1 -> {
                        (result)!![0] = dexN((((dexR((v)!![1]) as RuleBill?)))!!.getTime())
                        (v)!![9] = (result)!![0]
                        (v)!![18] = dexN(0L)
                        (v)!![3] = dexN(java.lang.Long.compare(dexL((v)!![9]), dexL((v)!![18])))
                        pc = (if (((dexI((v)!![3]) > 0))) 0x204 else 0x1cb)
                        continue
                }
                0x1cb -> {
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![12]) as String?)), ((dexR((v)!![14]) as String?))))
                        (v)!![9] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setTime(dexL((v)!![9]))
                        (result)!![0] = dexN((((dexR((v)!![1]) as RuleBill?)))!!.getTime())
                        (v)!![9] = (result)!![0]
                        (v)!![3] = dexN(java.lang.Long.compare(dexL((v)!![9]), dexL((v)!![15])))
                        pc = (if ((dexZero((v)!![3]))) 0x1e2 else 0x1da)
                        continue
                }
                0x1da -> {
                        (result)!![0] = dexN((((dexR((v)!![1]) as RuleBill?)))!!.getTime())
                        (v)!![9] = (result)!![0]
                        (v)!![3] = dexN(java.lang.Long.compare(dexL((v)!![9]), dexL((v)!![18])))
                        pc = (if ((!(dexZero((v)!![3])))) 0x1eb else 0x1e2)
                        continue
                }
                0x1e2 -> {
                        (v)!![3] = "yyyy-MM-dd HH:mm"
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![12]) as String?)), ((dexR((v)!![3]) as String?))))
                        (v)!![9] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setTime(dexL((v)!![9]))
                        pc = 0x1eb
                        continue
                }
                0x1eb -> {
                        (result)!![0] = dexN((((dexR((v)!![1]) as RuleBill?)))!!.getTime())
                        (v)!![9] = (result)!![0]
                        (v)!![3] = dexN(java.lang.Long.compare(dexL((v)!![9]), dexL((v)!![15])))
                        pc = (if ((dexZero((v)!![3]))) 0x1fb else 0x1f3)
                        continue
                }
                0x1f3 -> {
                        (result)!![0] = dexN((((dexR((v)!![1]) as RuleBill?)))!!.getTime())
                        (v)!![9] = (result)!![0]
                        (v)!![3] = dexN(java.lang.Long.compare(dexL((v)!![9]), dexL((v)!![18])))
                        pc = (if ((!(dexZero((v)!![3])))) 0x204 else 0x1fb)
                        continue
                }
                0x1fb -> {
                        (v)!![3] = "yyyy-MM-dd"
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![12]) as String?)), ((dexR((v)!![3]) as String?))))
                        (v)!![9] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setTime(dexL((v)!![9]))
                        pc = 0x204
                        continue
                }
                0x204 -> {
                        (v)!![11] = dexN((dexI((v)!![11]) + (1)))
                        (v)!![10] = (v)!![17]
                        (v)!![9] = dexN(1L)
                        pc = 0x9d
                        continue
                }
                0x20b -> {
                        (v)!![4] = dexN((dexI((v)!![4]) + (1)))
                        pc = 0x16
                        continue
                }
                0x20f -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getRemark()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![0]) as CharSequence?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((dexZero((v)!![0]))) 0x232 else 0x219)
                        continue
                }
                0x219 -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getShopName()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![0]) as CharSequence?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x22b else 0x223)
                        continue
                }
                0x223 -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getShopName()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setRemark(((dexR((v)!![0]) as String?)))
                        pc = 0x232
                        continue
                }
                0x22b -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getOrigin()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setRemark(((dexR((v)!![0]) as String?)))
                        pc = 0x232
                        continue
                }
                0x232 -> {
                        pc = (if ((dexZero((v)!![5]))) 0x24c else 0x234)
                        continue
                }
                0x234 -> {
                        (v)!![0] = DexReference()
                        ((((v)!![0] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "收入-"
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getShopName()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.toString()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![1]) as RuleBill?)))!!.setShopName(((dexR((v)!![0]) as String?)))
                        pc = 0x24c
                        continue
                }
                0x24c -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getRemark()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![0]) as CharSequence?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x261 else 0x256)
                        continue
                }
                0x256 -> {
                        (result)!![0] = (((dexR((v)!![1]) as RuleBill?)))!!.getNumber()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![0]) as CharSequence?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x261 else 0x260)
                        continue
                }
                0x260 -> {
                        (result)!![1] = (v)!![1]
                        return -(1)
                }
                0x261 -> {
                        (result)!![1] = (v)!![2]
                        return -(1)
                }
                else -> {
                    return pc
                }
            }
        }
    }

    fun v(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("云闪付")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((((str)!!.contains(("当前状态")!!) || (str)!!.contains(("优惠信息")!!))) && (i6 > 0))) {
                        var i7: Int = (i6 - 1)
                        var strReplace: String? = ((((list)!![i7])!!.replace(("¥")!!, ("")!!))!!.replace((",")!!, ("")!!))!!.replace(("+")!!, ("")!!)
                        if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && Amounts.J(strReplace))) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace))).toString() + ""))
                            if (((i6 > 1) && TextUtils.isEmpty((billInfo)!!.getRemark()))) {
                                var i8: Int = (i6 - 2)
                                (billInfo)!!.setRemark((list)!![i8])
                                (billInfo)!!.setShopName((list)!![i8])
                            }
                            if (((list)!![i7])!!.contains(("+")!!)) {
                                (billInfo)!!.setIncome(true)
                            }
                        }
                    } else if (((str)!!.equals("卡号") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset(((list)!![(i6 + 1)])!!.replace(("尾号")!!, ("")!!))
                        if ((i6 < ((list)!!.size - 2))) {
                            var i9: Int = (i6 + 2)
                            if (((list)!![i9])!!.contains(("****")!!)) {
                                (billInfo)!!.setAsset(((billInfo)!!.getAsset() + ((list)!![i9])!!.substring((((list)!![i9])!!.indexOf(("****")!!) + 4))))
                            }
                        }
                    } else if (((str)!!.equals("收款方式") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset(((list)!![(i6 + 1)])!!.replace(("****")!!, ("")!!))
                    } else if (((str)!!.equals("付款方式") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset(((list)!![(i6 + 1)])!!.replace(("****")!!, ("")!!))
                    } else if (((((str)!!.equals("订单时间") || (str)!!.equals("退款时间"))) && (i6 < ((list)!!.size - 1)))) {
                        var i10: Int = (i6 + 1)
                        (billInfo)!!.setTime(Dates.X0((list)!![i10], "yyyy年MM月dd日 HH:mm:ss"))
                        if (((billInfo)!!.getTime() == -(1))) {
                            (billInfo)!!.setTime(Dates.X0((list)!![i10], "yyyy.MM.dd HH:mm:ss"))
                        }
                    } else if (((str)!!.equals("扣款时间") && (i6 < ((list)!!.size - 1)))) {
                        var i11: Int = (i6 + 1)
                        (billInfo)!!.setTime(Dates.X0(((list)!![i11])!!.replace(("延迟扣费|")!!, ("")!!), "yyyy年MM月dd日 HH:mm:ss"))
                        if (((billInfo)!!.getTime() == -(1))) {
                            (billInfo)!!.setTime(Dates.X0(((list)!![i11])!!.replace(("延迟扣费|")!!, ("")!!), "yyyy.MM.dd HH:mm:ss"))
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

    fun w(list: MutableList<String?>?): RuleBill? {
        if ((list)!!.isEmpty()) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("云闪付")
        if (this.f13768g) {
            (billInfo)!!.setTransfer(true)
            (billInfo)!!.setToAsset(this.f13766e)
        }
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if ((TextUtils.isEmpty((billInfo)!!.getNumber()) && (str)!!.contains(("¥")!!))) {
                        var strReplace: String? = ((str)!!.replace(("¥")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace))).toString() + ""))
                            if ((i6 > 0)) {
                                (billInfo)!!.setRemark((list)!![(i6 - 1)])
                                (billInfo)!!.setShopName((billInfo)!!.getRemark())
                            }
                        }
                    } else if (((str)!!.equals("订单信息") && (i6 < ((list)!!.size - 1)))) {
                        var i7: Int = (i6 + 1)
                        (billInfo)!!.setRemark((list)!![i7])
                        (billInfo)!!.setShopName((list)!![i7])
                    } else if (((str)!!.equals("付款方式") && (i6 < ((list)!!.size - 1)))) {
                        var i8: Int = (i6 + 1)
                        (billInfo)!!.setAsset((list)!![i8])
                        if ((i6 < ((list)!!.size - 3))) {
                            var i9: Int = (i6 + 3)
                            if ((((list)!![i9])!!.contains(("[")!!) && ((list)!![i9])!!.contains(("]")!!))) {
                                (billInfo)!!.setAsset((((list)!![i8] + (list)!![(i6 + 2)]) + (((list)!![i9])!!.substring(0, ((list)!![i9])!!.indexOf(("[")!!)))!!.replace((" ")!!, ("")!!)))
                            } else {
                                var i10: Int = (i6 + 2)
                                if ((((list)!![i10])!!.contains(("[")!!) && ((list)!![i10])!!.contains(("]")!!))) {
                                    (billInfo)!!.setAsset(((list)!![i8] + (((list)!![i10])!!.substring(0, ((list)!![i10])!!.indexOf(("[")!!)))!!.replace((" ")!!, ("")!!)))
                                }
                            }
                        } else if ((i6 < ((list)!!.size - 2))) {
                            var i11: Int = (i6 + 2)
                            if ((((list)!![i11])!!.contains(("[")!!) && ((list)!![i11])!!.contains(("]")!!))) {
                                (billInfo)!!.setAsset(((list)!![i8] + (((list)!![i11])!!.substring(0, ((list)!![i11])!!.indexOf(("[")!!)))!!.replace((" ")!!, ("")!!)))
                            }
                        }
                        if (this.f13768g) {
                            (billInfo)!!.setFromAsset((billInfo)!!.getAsset())
                        }
                    }
                i6++
            }
        }
        (billInfo)!!.setTime(System.currentTimeMillis())
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun x(arg0: MutableList<String?>?, arg1: String?, arg2: String?): RuleBill? {
        var v: Array<Any?>? = arrayOfNulls<Any>(14)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![10] = this
        (v)!![11] = arg0
        (v)!![12] = arg1
        (v)!![13] = arg2
        while (true) {
            if ((pc == -(1))) {
                return ((dexR((result)!![1]) as RuleBill?))
            }
            if ((pc < 0x194)) pc = recovered_x_block0(v, result, pc) else pc = recovered_x_block1(v, result, pc)
        }
    }

    private fun recovered_x_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (result)!![0] = dexN((((dexR((v)!![11]) as MutableList<String?>?)))!!.size)
                        (v)!![0] = (result)!![0]
                        (v)!![1] = dexN(0L)
                        pc = (if ((!(dexZero((v)!![0])))) 0x8 else 0x7)
                        continue
                }
                0x7 -> {
                        (result)!![1] = (v)!![1]
                        return -(1)
                }
                0x8 -> {
                        (v)!![0] = DexReference()
                        ((((v)!![0] as DexReference?)))!!.value = RuleBill()
                        (v)!![2] = dexN(1L)
                        (((dexR((v)!![0]) as RuleBill?)))!!.setTransfer(dexZ((v)!![2]))
                        (v)!![3] = "云闪付"
                        (((dexR((v)!![0]) as RuleBill?)))!!.setOrigin(((dexR((v)!![3]) as String?)))
                        (((dexR((v)!![0]) as RuleBill?)))!!.setFromAsset(((dexR((v)!![12]) as String?)))
                        (((dexR((v)!![0]) as RuleBill?)))!!.setToAsset(((dexR((v)!![13]) as String?)))
                        (v)!![12] = dexN(0L)
                        (v)!![13] = (v)!![12]
                        pc = 0x1e
                        continue
                }
                0x1e -> {
                        (result)!![0] = dexN((((dexR((v)!![11]) as MutableList<String?>?)))!!.size)
                        (v)!![3] = (result)!![0]
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![3])))) 0x29a else 0x24)
                        continue
                }
                0x24 -> {
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![13])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "¥"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        (v)!![6] = ","
                        (v)!![7] = ""
                        pc = (if ((dexZero((v)!![5]))) 0x6a else 0x36)
                        continue
                }
                0x36 -> {
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![7]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![7]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getNumber()
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x296 else 0x48)
                        continue
                }
                0x48 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x296 else 0x4e)
                        continue
                }
                0x4e -> {
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![5]))))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(dexD((v)!![5]))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![7]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.toString()
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setNumber(((dexR((v)!![3]) as String?)))
                        pc = 0x296
                        continue
                }
                0x6a -> {
                        (v)!![5] = "手续费"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![5]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((dexZero((v)!![8]))) 0xcf else 0x72)
                        continue
                }
                0x72 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0xb0 else 0x78)
                        continue
                }
                0x78 -> {
                        (result)!![0] = dexN((((dexR((v)!![11]) as MutableList<String?>?)))!!.size)
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN((dexI((v)!![4]) - dexI((v)!![2])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![4])))) 0xb0 else 0x7f)
                        continue
                }
                0x7f -> {
                        (v)!![4] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (v)!![9] = "元"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![9]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((dexZero((v)!![8]))) 0xb0 else 0x8f)
                        continue
                }
                0x8f -> {
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![7]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![7]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x296 else 0xa3)
                        continue
                }
                0xa3 -> {
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![3]))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setServiceCharge(dexD((v)!![3]))
                        pc = 0x296
                        continue
                }
                0xb0 -> {
                        (result)!![0] = dexN((((dexR((v)!![3]) as String?)))!!.lastIndexOf((((dexR((v)!![5]) as String?)))!!))
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.substring(dexI((v)!![12]), dexI((v)!![4]))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![7]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x296 else 0xc2)
                        continue
                }
                0xc2 -> {
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![3]))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setServiceCharge(dexD((v)!![3]))
                        pc = 0x296
                        continue
                }
                0xcf -> {
                        (v)!![5] = "订单金额"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x110 else 0xd7)
                        continue
                }
                0xd7 -> {
                        (result)!![0] = dexN((((dexR((v)!![11]) as MutableList<String?>?)))!!.size)
                        (v)!![5] = (result)!![0]
                        (v)!![5] = dexN((dexI((v)!![5]) - dexI((v)!![2])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![5])))) 0x110 else 0xde)
                        continue
                }
                0xde -> {
                        (v)!![3] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![7]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![7]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x296 else 0xf4)
                        continue
                }
                0xf4 -> {
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![5]))))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(dexD((v)!![5]))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![7]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.toString()
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setNumber(((dexR((v)!![3]) as String?)))
                        pc = 0x296
                        continue
                }
                0x110 -> {
                        (v)!![5] = "转账金额"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x151 else 0x118)
                        continue
                }
                0x118 -> {
                        (result)!![0] = dexN((((dexR((v)!![11]) as MutableList<String?>?)))!!.size)
                        (v)!![5] = (result)!![0]
                        (v)!![5] = dexN((dexI((v)!![5]) - dexI((v)!![2])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![5])))) 0x151 else 0x11f)
                        continue
                }
                0x11f -> {
                        (v)!![3] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![7]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![7]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x296 else 0x135)
                        continue
                }
                0x135 -> {
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![5]))))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(dexD((v)!![5]))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![7]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.toString()
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setNumber(((dexR((v)!![3]) as String?)))
                        pc = 0x296
                        continue
                }
                0x151 -> {
                        (v)!![4] = "收款银行"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![4]) as Any?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x169 else 0x159)
                        continue
                }
                0x159 -> {
                        (v)!![4] = "信用卡号"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x169 else 0x161)
                        continue
                }
                0x161 -> {
                        (v)!![4] = "收款方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x17d else 0x169)
                        continue
                }
                0x169 -> {
                        (result)!![0] = dexN((((dexR((v)!![11]) as MutableList<String?>?)))!!.size)
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN((dexI((v)!![4]) - dexI((v)!![2])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![4])))) 0x17d else 0x170)
                        continue
                }
                0x170 -> {
                        (v)!![3] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (((dexR((v)!![0]) as RuleBill?)))!!.setToAsset(((dexR((v)!![3]) as String?)))
                        pc = 0x296
                        continue
                }
                0x17d -> {
                        (v)!![4] = "付款银行"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![4]) as Any?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x18d else 0x185)
                        continue
                }
                0x185 -> {
                        (v)!![4] = "付款方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1a1 else 0x18d)
                        continue
                }
                0x18d -> {
                        (result)!![0] = dexN((((dexR((v)!![11]) as MutableList<String?>?)))!!.size)
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN((dexI((v)!![4]) - dexI((v)!![2])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![4])))) 0x1a1 else 0x194)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_x_block1(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x194 -> {
                        (v)!![3] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (((dexR((v)!![0]) as RuleBill?)))!!.setFromAsset(((dexR((v)!![3]) as String?)))
                        pc = 0x296
                        continue
                }
                0x1a1 -> {
                        (v)!![4] = "付款卡号"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1e0 else 0x1a9)
                        continue
                }
                0x1a9 -> {
                        (result)!![0] = dexN((((dexR((v)!![11]) as MutableList<String?>?)))!!.size)
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN((dexI((v)!![4]) - dexI((v)!![2])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![4])))) 0x1e0 else 0x1b0)
                        continue
                }
                0x1b0 -> {
                        (v)!![3] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (result)!![0] = dexN((((dexR((v)!![3]) as String?)))!!.length)
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN((dexI((v)!![4]) + (-(4))))
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.substring(dexI((v)!![4]))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x296 else 0x1c8)
                        continue
                }
                0x1c8 -> {
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getFromAsset()
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![5]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.toString()
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setFromAsset(((dexR((v)!![3]) as String?)))
                        pc = 0x296
                        continue
                }
                0x1e0 -> {
                        (v)!![4] = "收款卡号"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x21f else 0x1e8)
                        continue
                }
                0x1e8 -> {
                        (result)!![0] = dexN((((dexR((v)!![11]) as MutableList<String?>?)))!!.size)
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN((dexI((v)!![4]) - dexI((v)!![2])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![4])))) 0x21f else 0x1ef)
                        continue
                }
                0x1ef -> {
                        (v)!![3] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (result)!![0] = dexN((((dexR((v)!![3]) as String?)))!!.length)
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN((dexI((v)!![4]) + (-(4))))
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.substring(dexI((v)!![4]))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x296 else 0x207)
                        continue
                }
                0x207 -> {
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getToAsset()
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![5]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.toString()
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setToAsset(((dexR((v)!![3]) as String?)))
                        pc = 0x296
                        continue
                }
                0x21f -> {
                        (v)!![4] = "创建时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x276 else 0x227)
                        continue
                }
                0x227 -> {
                        (result)!![0] = dexN((((dexR((v)!![11]) as MutableList<String?>?)))!!.size)
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN((dexI((v)!![4]) - dexI((v)!![2])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![4])))) 0x276 else 0x22e)
                        continue
                }
                0x22e -> {
                        (v)!![3] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "yyyy-MM-dd HH:mm:ss"
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![3]) as String?)), ((dexR((v)!![4]) as String?))))
                        (v)!![4] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setTime(dexL((v)!![4]))
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBill?)))!!.getTime())
                        (v)!![4] = (result)!![0]
                        (v)!![6] = dexN(-(1L))
                        (v)!![4] = dexN(java.lang.Long.compare(dexL((v)!![4]), dexL((v)!![6])))
                        (v)!![8] = dexN(0L)
                        pc = (if ((dexZero((v)!![4]))) 0x253 else 0x24b)
                        continue
                }
                0x24b -> {
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBill?)))!!.getTime())
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN(java.lang.Long.compare(dexL((v)!![4]), dexL((v)!![8])))
                        pc = (if ((!(dexZero((v)!![4])))) 0x25c else 0x253)
                        continue
                }
                0x253 -> {
                        (v)!![4] = "yyyy-MM-dd HH:mm"
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![3]) as String?)), ((dexR((v)!![4]) as String?))))
                        (v)!![4] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setTime(dexL((v)!![4]))
                        pc = 0x25c
                        continue
                }
                0x25c -> {
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBill?)))!!.getTime())
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN(java.lang.Long.compare(dexL((v)!![4]), dexL((v)!![6])))
                        pc = (if ((dexZero((v)!![4]))) 0x26c else 0x264)
                        continue
                }
                0x264 -> {
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBill?)))!!.getTime())
                        (v)!![4] = (result)!![0]
                        (v)!![4] = dexN(java.lang.Long.compare(dexL((v)!![4]), dexL((v)!![8])))
                        pc = (if ((!(dexZero((v)!![4])))) 0x296 else 0x26c)
                        continue
                }
                0x26c -> {
                        (v)!![4] = "yyyy-MM-dd"
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![3]) as String?)), ((dexR((v)!![4]) as String?))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setTime(dexL((v)!![3]))
                        pc = 0x296
                        continue
                }
                0x276 -> {
                        (v)!![4] = "订单时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x296 else 0x27e)
                        continue
                }
                0x27e -> {
                        (result)!![0] = dexN((((dexR((v)!![11]) as MutableList<String?>?)))!!.size)
                        (v)!![3] = (result)!![0]
                        (v)!![3] = dexN((dexI((v)!![3]) - dexI((v)!![2])))
                        pc = (if (((dexI((v)!![13]) >= dexI((v)!![3])))) 0x296 else 0x285)
                        continue
                }
                0x285 -> {
                        (v)!![3] = dexN((dexI((v)!![13]) + (1)))
                        (result)!![0] = (((dexR((v)!![11]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "yyyy年MM月dd日 HH:mm:ss"
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![3]) as String?)), ((dexR((v)!![4]) as String?))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![0]) as RuleBill?)))!!.setTime(dexL((v)!![3]))
                        pc = 0x296
                        continue
                }
                0x296 -> {
                        (v)!![13] = dexN((dexI((v)!![13]) + (1)))
                        pc = 0x1e
                        continue
                }
                0x29a -> {
                        (result)!![0] = (((dexR((v)!![0]) as RuleBill?)))!!.getNumber()
                        (v)!![11] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![11]) as CharSequence?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x2a5 else 0x2a4)
                        continue
                }
                0x2a4 -> {
                        (result)!![1] = (v)!![0]
                        return -(1)
                }
                0x2a5 -> {
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
        private var f13762h: Int = 4

        private var f13763i: Int = 5

        private var f13764j: Int = 12

        private var f13765k: UnionPayRule? = null

        fun y(): UnionPayRule? {
            if ((f13765k == null)) {
                f13765k = UnionPayRule()
            }
            return f13765k
        }
    }
}
