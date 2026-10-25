@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 c4a46d48355326741462c3d214111e8031d43bb1d178f015244a80f55ac86dfe.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class HuaweiRule : RuleBase(), RuleCallback {
    private var f13698e: Boolean = false

    private var f13699f: String? = null

    private fun v(context: RuleContext?, list: MutableList<String?>?, str: String?): Unit {

        if (((list == null) || ((list)!!.size <= 0))) {
            return
        }
        var billInfoR: RuleBill? = (if ((this.a == 1)) r(list) else null)
        var i6: Int = this.a
        if ((i6 == 3)) {
            billInfoR = u(list)
        } else if ((i6 == 2)) {
            billInfoR = s(list)
        } else if ((i6 == 4)) {
            billInfoR = t(list)
        }
        if ((billInfoR != null)) {
            this.b = false
            this.f13698e = false
            (billInfoR)!!.setTagList(RuleTags.d(this.f13681d))
            RuleBridge.l(context, billInfoR, this)
        }
    }

    override fun dismiss(): Unit {

    }

    fun q(arg0: RuleContext?, arg1: String?, arg2: CaptureNode?): Unit {
        var v: Array<Any?>? = arrayOfNulls<Any>(11)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![7] = this
        (v)!![8] = arg0
        (v)!![9] = arg1
        (v)!![10] = arg2
        while (true) {
            if ((pc == -(1))) {
                return
            }
            if ((pc < 0x84)) pc = recovered_q_block0(v, result, pc) else pc = recovered_q_block1(v, result, pc)
        }
    }

    private fun recovered_q_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (v)!![0] = dexN(4L)
                        (v)!![1] = dexN(3L)
                        (v)!![2] = dexN(2L)
                        (v)!![3] = dexN(0L)
                        (result)!![0] = dexN((((dexR((v)!![9]) as String?)))!!.hashCode())
                        (v)!![4] = dexN(1L)
                        (v)!![5] = dexN(-(1L))
                        (result)!![0] = dexN((((dexR((v)!![9]) as String?)))!!.hashCode())
                        (v)!![6] = (result)!![0]
                        when (dexI((v)!![6])) {
                            -(2030087667) -> {
                                pc = 0x5f
                            }
                            -(1349345186) -> {
                                pc = 0x54
                            }
                            -(1175845833) -> {
                                pc = 0x49
                            }
                            -(855483783) -> {
                                pc = 0x3e
                            }
                            219764026 -> {
                                pc = 0x33
                            }
                            998460551 -> {
                                pc = 0x28
                            }
                            1086175120 -> {
                                pc = 0x1d
                            }
                            1454072758 -> {
                                pc = 0x12
                            }
                            else -> {
                                pc = 0x10
                            }
                        }
                        continue
                }
                0x10 -> {
                        pc = 0x69
                        continue
                }
                0x12 -> {
                        (v)!![6] = "com.huawei.wallet.view.MainActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![9]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x1b else 0x1a)
                        continue
                }
                0x1a -> {
                        pc = 0x69
                        continue
                }
                0x1b -> {
                        (v)!![5] = dexN(7L)
                        pc = 0x69
                        continue
                }
                0x1d -> {
                        (v)!![6] = "com.huawei.nfc.carrera.ui.swipe.SwipeActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![9]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x26 else 0x25)
                        continue
                }
                0x25 -> {
                        pc = 0x69
                        continue
                }
                0x26 -> {
                        (v)!![5] = dexN(6L)
                        pc = 0x69
                        continue
                }
                0x28 -> {
                        (v)!![6] = "com.huawei.wallet.nfc.bankcard.paymentcode.ui.PaymentCodeActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![9]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x31 else 0x30)
                        continue
                }
                0x30 -> {
                        pc = 0x69
                        continue
                }
                0x31 -> {
                        (v)!![5] = dexN(5L)
                        pc = 0x69
                        continue
                }
                0x33 -> {
                        (v)!![6] = "com.huawei.wallet.bankcard.scanpay.ui.activity.ScanQrCodePayResultActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![9]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x3c else 0x3b)
                        continue
                }
                0x3b -> {
                        pc = 0x69
                        continue
                }
                0x3c -> {
                        (v)!![5] = (v)!![0]
                        pc = 0x69
                        continue
                }
                0x3e -> {
                        (v)!![6] = "com.huawei.petalpay.userapp.function.billdetails.activity.BillDetailInfoActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![9]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x47 else 0x46)
                        continue
                }
                0x46 -> {
                        pc = 0x69
                        continue
                }
                0x47 -> {
                        (v)!![5] = (v)!![1]
                        pc = 0x69
                        continue
                }
                0x49 -> {
                        (v)!![6] = "com.huawei.wallet.nfc.bankcard.transactionhistory.ui.activity.UnCardTransRecordDetailActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![9]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x52 else 0x51)
                        continue
                }
                0x51 -> {
                        pc = 0x69
                        continue
                }
                0x52 -> {
                        (v)!![5] = (v)!![2]
                        pc = 0x69
                        continue
                }
                0x54 -> {
                        (v)!![6] = "com.huawei.wallet.nfc.bankcard.transactionhistory.ui.activity.UnCardTransRecordFilterActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![9]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x5d else 0x5c)
                        continue
                }
                0x5c -> {
                        pc = 0x69
                        continue
                }
                0x5d -> {
                        (v)!![5] = (v)!![4]
                        pc = 0x69
                        continue
                }
                0x5f -> {
                        (v)!![6] = "com.huawei.nfc.carrera.ui.swipe.SwipeResultActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![9]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x68 else 0x67)
                        continue
                }
                0x67 -> {
                        pc = 0x69
                        continue
                }
                0x68 -> {
                        (v)!![5] = (v)!![3]
                        pc = 0x69
                        continue
                }
                0x69 -> {
                        when (dexI((v)!![5])) {
                            0 -> {
                                pc = 0xeb
                            }
                            1 -> {
                                pc = 0xe4
                            }
                            2 -> {
                                pc = 0xdd
                            }
                            3 -> {
                                pc = 0xd6
                            }
                            4 -> {
                                pc = 0xcf
                            }
                            5 -> {
                                pc = 0x6e
                            }
                            6 -> {
                                pc = 0x6e
                            }
                            7 -> {
                                pc = 0xe4
                            }
                            else -> {
                                pc = 0x6c
                            }
                        }
                        continue
                }
                0x6c -> {
                        pc = 0xf1
                        continue
                }
                0x6e -> {
                        pc = (if ((dexZero((v)!![10]))) 0xf1 else 0x70)
                        continue
                }
                0x70 -> {
                        (result)!![0] = (((dexR((v)!![7]) as RuleBase?)))!!.m(((dexR((v)!![10]) as CaptureNode?)))
                        (v)!![0] = (result)!![0]
                        pc = (if ((dexZero((v)!![0]))) 0xf2 else 0x76)
                        continue
                }
                0x76 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as MutableList<String?>?)))!!.isEmpty())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0xf2 else 0x7c)
                        continue
                }
                0x7c -> {
                        (v)!![1] = "二维码付款码"
                        (result)!![0] = dexN(((((dexR((v)!![0]) as MutableList<String?>?)))!! as List<Any?>).indexOf(((dexR((v)!![1]) as Any?))))
                        (v)!![1] = (result)!![0]
                        pc = (if (((dexI((v)!![1]) < 0))) 0xf2 else 0x84)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_q_block1(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x84 -> {
                        (result)!![0] = dexN((((dexR((v)!![0]) as MutableList<String?>?)))!!.size)
                        (v)!![2] = (result)!![0]
                        (v)!![2] = dexN((dexI((v)!![2]) - dexI((v)!![4])))
                        pc = (if (((dexI((v)!![1]) >= dexI((v)!![2])))) 0xf2 else 0x8b)
                        continue
                }
                0x8b -> {
                        (v)!![1] = dexN((dexI((v)!![1]) + dexI((v)!![4])))
                        (result)!![0] = (((dexR((v)!![0]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (v)!![2] = "（"
                        (v)!![3] = ""
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![2]) as CharSequence?)))!!, (((dexR((v)!![3]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (v)!![2] = "）"
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![2]) as CharSequence?)))!!, (((dexR((v)!![3]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (v)!![2] = "尾号"
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![2]) as CharSequence?)))!!, (((dexR((v)!![3]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (v)!![2] = " "
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.replace((((dexR((v)!![2]) as CharSequence?)))!!, (((dexR((v)!![3]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as String?)))!!.trim()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![7]) as HuaweiRule?)))!!.f13699f = ((dexR((v)!![1]) as String?))
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![2] = "asset:"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![2]) as String?)))
                        (v)!![2] = (((dexR((v)!![7]) as HuaweiRule?)))!!.f13699f
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![2]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (v)!![2] = "sssss"
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![2]) as Any?)), ((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0xf2
                        continue
                }
                0xcf -> {
                        (((dexR((v)!![7]) as RuleBase?)))!!.a = dexI((v)!![1])
                        (((dexR((v)!![7]) as HuaweiRule?)))!!.f13698e = dexZ((v)!![4])
                        (((dexR((v)!![7]) as RuleBase?)))!!.b = dexZ((v)!![4])
                        pc = 0xf1
                        continue
                }
                0xd6 -> {
                        (((dexR((v)!![7]) as RuleBase?)))!!.a = dexI((v)!![0])
                        (((dexR((v)!![7]) as HuaweiRule?)))!!.f13698e = dexZ((v)!![4])
                        (((dexR((v)!![7]) as RuleBase?)))!!.b = dexZ((v)!![4])
                        pc = 0xf1
                        continue
                }
                0xdd -> {
                        (((dexR((v)!![7]) as RuleBase?)))!!.a = dexI((v)!![2])
                        (((dexR((v)!![7]) as HuaweiRule?)))!!.f13698e = dexZ((v)!![4])
                        (((dexR((v)!![7]) as RuleBase?)))!!.b = dexZ((v)!![4])
                        pc = 0xf1
                        continue
                }
                0xe4 -> {
                        (((dexR((v)!![7]) as RuleBase?)))!!.a = dexI((v)!![3])
                        (((dexR((v)!![7]) as RuleBase?)))!!.b = dexZ((v)!![3])
                        (((dexR((v)!![7]) as HuaweiRule?)))!!.f13698e = dexZ((v)!![3])
                        pc = 0xf1
                        continue
                }
                0xeb -> {
                        (((dexR((v)!![7]) as RuleBase?)))!!.a = dexI((v)!![4])
                        (((dexR((v)!![7]) as HuaweiRule?)))!!.f13698e = dexZ((v)!![4])
                        (((dexR((v)!![7]) as RuleBase?)))!!.b = dexZ((v)!![4])
                        pc = 0xf1
                        continue
                }
                0xf1 -> {
                        (v)!![0] = dexN(0L)
                        pc = 0xf2
                        continue
                }
                0xf2 -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![7]) as HuaweiRule?)))!!.f13698e)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0xfc else 0xf6)
                        continue
                }
                0xf6 -> {
                        pc = (if ((dexZero((v)!![10]))) 0xfc else 0xf8)
                        continue
                }
                0xf8 -> {
                        (result)!![0] = (((dexR((v)!![7]) as RuleBase?)))!!.m(((dexR((v)!![10]) as CaptureNode?)))
                        (v)!![0] = (result)!![0]
                        pc = 0xfc
                        continue
                }
                0xfc -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![7]) as RuleBase?)))!!.b)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x105 else 0x100)
                        continue
                }
                0x100 -> {
                        pc = (if ((dexZero((v)!![10]))) 0x105 else 0x102)
                        continue
                }
                0x102 -> {
                        (((dexR((v)!![7]) as HuaweiRule?)))!!.v(((dexR((v)!![8]) as RuleContext?)), ((dexR((v)!![0]) as MutableList<String?>?)), ((dexR((v)!![9]) as String?)))
                        pc = 0x105
                        continue
                }
                0x105 -> {
                        return -(1)
                }
                else -> {
                    return pc
                }
            }
        }
    }

    fun r(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("华为支付")
        var iD: Int = d(list, "¥", true)
        if (((iD > 0) && (iD < ((list)!!.size - 1)))) {
            var str: String? = (list)!![(iD + 1)]
            if (Amounts.J(str)) {
                (billInfo)!!.setNumber(str)
                (billInfo)!!.setRemark((list)!![(iD - 1)])
                if ((iD > 2)) {
                    (billInfo)!!.setAsset(((list)!![(iD - 3)] + (list)!![(iD - 2)]))
                } else {
                    (billInfo)!!.setAsset(this.f13699f)
                }
                return billInfo
            }
        }
        return null
    }

    fun s(list: MutableList<String?>?): RuleBill? {
        if ((list)!!.isEmpty()) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("华为支付")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if ((((str)!!.contains(("¥")!!) && TextUtils.isEmpty((billInfo)!!.getNumber())) && (i6 < ((list)!!.size - 3)))) {
                        var strReplace: String? = ((list)!![(i6 + 1)])!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                            (billInfo)!!.setIncome(!((str)!!.contains(("-")!!)))
                            (billInfo)!!.setAsset((list)!![(i6 + 3)])
                        }
                    } else if (((str)!!.equals("商家") && (i6 < ((list)!!.size - 1)))) {
                        var str2: String? = (list)!![(i6 + 1)]
                        (billInfo)!!.setRemark(str2)
                        (billInfo)!!.setShopName(str2)
                    } else if (((str)!!.equals("优惠金额") && (i6 < ((list)!!.size - 1)))) {
                        var strReplace2: String? = ((list)!![(i6 + 1)])!!.replace(("¥")!!, ("")!!)
                        if (Amounts.J(strReplace2)) {
                            (billInfo)!!.setDiscount(strReplace2)
                        }
                    } else if (((str)!!.equals("日期") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setTime(Dates.X0((list)!![(i6 + 1)], "yyyy-MM-dd HH:mm:ss"))
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
        if ((list)!!.isEmpty()) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("华为支付")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if ((((str)!!.equals("交易成功") && (i6 > 0)) && (i6 < ((list)!!.size - 1)))) {
                        var str2: String? = (list)!![(i6 - 1)]
                        var strReplace: String? = ((((str2)!!.replace((",")!!, ("")!!))!!.replace(("+")!!, ("")!!))!!.replace(("-")!!, ("")!!))!!.replace((" ")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                            (billInfo)!!.setIncome(!((str2)!!.contains(("-")!!)))
                            (billInfo)!!.setRemark((list)!![(i6 + 1)])
                            (billInfo)!!.setShopName((billInfo)!!.getRemark())
                        }
                    } else if (((str)!!.equals("付款银行") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset(((((list)!![(i6 + 1)])!!.replace((" ")!!, ("")!!))!!.replace(("(")!!, ("")!!))!!.replace((")")!!, ("")!!))
                    } else if (((str)!!.equals("商品名称") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setRemark((list)!![(i6 + 1)])
                    } else if (((str)!!.equals("交易时间") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setTime(Dates.X0((list)!![(i6 + 1)], "yyyy-MM-dd HH:mm:ss"))
                    }
                i6++
            }
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun u(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("华为支付")
        var iD: Int = d(list, "¥", true)
        if (((iD > 2) && (iD < ((list)!!.size - 2)))) {
            var str: String? = (list)!![(iD + 1)]
            if (Amounts.J(str)) {
                (billInfo)!!.setNumber(str)
                (billInfo)!!.setRemark((list)!![(iD + 2)])
                var iD2: Int = d(list, "付款方式", true)
                if (((iD2 > 0) && (iD2 < ((list)!!.size - 1)))) {
                    (billInfo)!!.setAsset((list)!![(iD2 + 1)])
                }
                return billInfo
            }
        }
        return null
    }

    companion object {
        private var f13693g: Int = 1

        private var f13694h: Int = 2

        private var f13695i: Int = 3

        private var f13696j: Int = 4

        private var f13697k: HuaweiRule? = null

        fun w(): HuaweiRule? {
            if ((f13697k == null)) {
                f13697k = HuaweiRule()
            }
            return f13697k
        }
    }
}
