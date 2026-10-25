@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "USELESS_CAST", "DEPRECATION")
package io.github.wcwac.beecount.capture.rules

import android.text.TextUtils
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet
import java.util.Collections

// Kotlin transcription of the verified 6.3.0/73 parser; source SHA-256 3f3eb73d6a18c3ae19a7725ce22b2e6699c5276ba089fd5894b6ea86189d0bf7.
// Page gates, ordered token offsets and recovered branch addresses are intentionally retained.
internal class AlipayRule : RuleBase(), RuleCallback {
    private var f13678e: Long = 0L

    private var f13679f: Boolean = false

    private var f13680g: MutableList<String?>? = mutableListOf("提现说明", "转出说明", "余额转入", "充值说明", "网商银行转账", "还款成功", "余额宝-单次转入", "余额宝-转出到余额")

    private fun y(context: RuleContext?, list: MutableList<String?>?, map: HashMap<String?, Boolean>?): Unit {
        var zA: Boolean = false
        var billInfoR: RuleBill? = null
        var billInfoR2: RuleBill? = null

        if (((list == null) || (list)!!.isEmpty())) {
            return
        }
        var z5: Boolean = true
        var z6: Boolean = ((((((((((((((((this.a != 2) || ((map)!! as Map<Any?, Boolean>).containsKey("支付成功")) || ((map)!! as Map<Any?, Boolean>).containsKey("转账成功")) || ((map)!! as Map<Any?, Boolean>).containsKey("代付成功")) || ((map)!! as Map<Any?, Boolean>).containsKey("交易成功")) || ((map)!! as Map<Any?, Boolean>).containsKey("充值成功")) || ((map)!! as Map<Any?, Boolean>).containsKey("还款成功")) || ((map)!! as Map<Any?, Boolean>).containsKey("退款成功")) || ((map)!! as Map<Any?, Boolean>).containsKey("赔付成功")) || ((map)!! as Map<Any?, Boolean>).containsKey("自动扣款成功")) || ((map)!! as Map<Any?, Boolean>).containsKey("有退款")) || ((map)!! as Map<Any?, Boolean>).containsKey("已全额退款")) || ((map)!! as Map<Any?, Boolean>).containsKey("缴费中")) || ((map)!! as Map<Any?, Boolean>).containsKey("派送中")) || a(list, "亲情卡付款成功", false)) || a(list, "付款成功", false))
        var zContainsKey: Boolean = ((map)!! as Map<Any?, Boolean>).containsKey("转账成功")
        var z7: Boolean = ((((map)!! as Map<Any?, Boolean>).containsKey("已收款") || ((map)!! as Map<Any?, Boolean>).containsKey("资金待入账")) || ((map)!! as Map<Any?, Boolean>).containsKey("你已收款"))
        if (((!(((map)!! as Map<Any?, Boolean>).containsKey("支付宝红包")) && !(((map)!! as Map<Any?, Boolean>).containsKey("查看红包记录"))) && !(((map)!! as Map<Any?, Boolean>).containsKey("查看红包")))) {
            z5 = false
        }
        if (z5) {
            zA = a(list, "红包金额", false)
            if (!(zA)) {
                zA = a(list, "个红包共", false)
            }
            if (!(zA)) {
                zA = a(list, "发的红包", false)
            }
            if (!(zA)) {
                zA = a(list, "领取成功", false)
            }
            if (!(zA)) {
                zA = ((map)!! as Map<Any?, Boolean>).containsKey("查看红包")
            }
            if (zA) {
                z6 = false
            }
        } else {
            zA = false
        }
        if (z6) {

            var i6: Int = this.a
            if ((((i6 == 2) || (i6 == 6)) || (i6 == 7))) {
                billInfoR2 = r(context, list)
            } else if ((i6 == 9)) {
                billInfoR2 = u(list)
            } else if ((i6 == 10)) {
                billInfoR2 = t(list)
            } else {
                billInfoR2 = (if ((i6 == 11)) s(context, list) else null)
            }
            if ((billInfoR2 != null)) {
                this.b = false
                this.f13679f = false
                (billInfoR2)!!.setTagList(RuleTags.d(this.f13681d))
                RuleBridge.l(context, billInfoR2, this)
                return
            }
            return
        }
        if (zContainsKey) {
            var billInfoV: RuleBill? = v(list)
            if ((billInfoV != null)) {
                this.b = false
                this.f13679f = false
                (billInfoV)!!.setTagList(RuleTags.d(this.f13681d))
                RuleBridge.l(context, billInfoV, this)
                return
            }
            return
        }
        if (z7) {
            if (((this.a != 2) || (((r(context, list)).also { billInfoR = it }) == null))) {
                return
            }
            this.b = false
            this.f13679f = false
            (billInfoR)!!.setTagList(RuleTags.d(this.f13681d))
            RuleBridge.l(context, billInfoR, this)
            return
        }
        if (zA) {
            var billInfoZ: RuleBill? = z(list)
            if ((billInfoZ == null)) {
                billInfoZ = x(list)
            }
            if ((billInfoZ != null)) {
                this.b = false
                this.f13679f = false
                (billInfoZ)!!.setTagList(RuleTags.d(this.f13681d))
                RuleBridge.l(context, billInfoZ, this)
            }
        }
    }

    override fun dismiss(): Unit {
        this.b = false
    }

    fun q(str: String?, str2: String?): Boolean {
        return ((str)!!.equals("com.eg.android.AlipayGphone") && (((((str2)!!.equals("com.eg.android.AlipayGphone.AlipayLogin") || (str2)!!.equals("com.alipay.android.msp.ui.views.MspContainerActivity")) || (str2)!!.equals("com.alipay.android.msp.ui.views.MspUniRenderActivity")) || (str2)!!.equals("com.alipay.android.phone.discovery.envelope.get.SnsCouponDetailActivity"))))
    }

    fun r(arg0: RuleContext?, arg1: MutableList<String?>?): RuleBill? {
        var v: Array<Any?>? = arrayOfNulls<Any>(24)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![21] = this
        (v)!![22] = arg0
        (v)!![23] = arg1
        while (true) {
            if ((pc == -(1))) {
                return ((dexR((result)!![1]) as RuleBill?))
            }
            if ((pc < 0x13f)) pc = recovered_r_block0(v, result, pc) else if ((pc < 0x23c)) pc = recovered_r_block1(v, result, pc) else if ((pc < 0x343)) pc = recovered_r_block2(v, result, pc) else if ((pc < 0x488)) pc = recovered_r_block3(v, result, pc) else if ((pc < 0x5fe)) pc = recovered_r_block4(v, result, pc) else if ((pc < 0x76f)) pc = recovered_r_block5(v, result, pc) else if ((pc < 0x902)) pc = recovered_r_block6(v, result, pc) else pc = recovered_r_block7(v, result, pc)
        }
    }

    private fun recovered_r_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (v)!![0] = (v)!![21]
                        (v)!![1] = (v)!![22]
                        (v)!![2] = (v)!![23]
                        (result)!![0] = dexN((if (((((dexR((v)!![23]) as MutableList<String?>?)))!!.isEmpty())) 1 else 0))
                        (v)!![3] = (result)!![0]
                        (v)!![4] = dexN(0L)
                        pc = (if ((dexZero((v)!![3]))) 0xe else 0xd)
                        continue
                }
                0xd -> {
                        (result)!![1] = (v)!![4]
                        return -(1)
                }
                0xe -> {
                        (v)!![3] = DexReference()
                        ((((v)!![3] as DexReference?)))!!.value = RuleBill()
                        (v)!![5] = "支付宝"
                        (((dexR((v)!![3]) as RuleBill?)))!!.setOrigin(((dexR((v)!![5]) as String?)))
                        (result)!![0] = dexN((if ((RuleSettings.i())) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((dexZero((v)!![6]))) 0x36 else 0x1e)
                        continue
                }
                0x1e -> {
                        (v)!![6] = dexN((((dexR((v)!![0]) as AlipayRule?)))!!.f13678e)
                        (result)!![0] = dexN(System.currentTimeMillis())
                        (v)!![8] = (result)!![0]
                        (result)!![0] = dexN((if ((Dates.m0(dexL((v)!![6]), dexL((v)!![8])))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((dexZero((v)!![6]))) 0x30 else 0x2a)
                        continue
                }
                0x2a -> {
                        (v)!![6] = dexN((((dexR((v)!![0]) as AlipayRule?)))!!.f13678e)
                        (((dexR((v)!![3]) as RuleBill?)))!!.setTime(dexL((v)!![6]))
                        pc = 0x36
                        continue
                }
                0x30 -> {
                        (result)!![0] = dexN(System.currentTimeMillis())
                        (v)!![6] = (result)!![0]
                        (((dexR((v)!![0]) as AlipayRule?)))!!.f13678e = dexL((v)!![6])
                        pc = 0x36
                        continue
                }
                0x36 -> {
                        (v)!![7] = dexN(0L)
                        pc = 0x37
                        continue
                }
                0x37 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![8] = (result)!![0]
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![8])))) 0x90b else 0x3d)
                        continue
                }
                0x3d -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![7])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (v)!![9] = "收款方"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![9]) as Any?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        (v)!![10] = dexN(1L)
                        pc = (if ((dexZero((v)!![9]))) 0x6c else 0x4c)
                        continue
                }
                0x4c -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![9] = (result)!![0]
                        (v)!![9] = dexN((dexI((v)!![9]) - dexI((v)!![10])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![9])))) 0x6c else 0x53)
                        continue
                }
                0x53 -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (((dexR((v)!![3]) as RuleBill?)))!!.setRemark(((dexR((v)!![8]) as String?)))
                        (result)!![0] = (((dexR((v)!![3]) as RuleBill?)))!!.getRemark()
                        (v)!![8] = (result)!![0]
                        (((dexR((v)!![3]) as RuleBill?)))!!.setShopName(((dexR((v)!![8]) as String?)))
                        (v)!![13] = (v)!![1]
                        (v)!![12] = (v)!![3]
                        (v)!![17] = (v)!![5]
                        pc = 0x69
                        continue
                }
                0x69 -> {
                        (v)!![11] = dexN(0L)
                        pc = 0x902
                        continue
                }
                0x6c -> {
                        (v)!![9] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        (v)!![11] = "付款方式"
                        (v)!![12] = "代付成功"
                        (v)!![13] = "充值成功"
                        (v)!![14] = "支付成功"
                        (v)!![15] = "支出"
                        (v)!![4] = "元"
                        (v)!![10] = ","
                        (v)!![6] = "¥"
                        (v)!![17] = (v)!![5]
                        (v)!![5] = "￥"
                        (v)!![1] = ""
                        (v)!![18] = (v)!![11]
                        (v)!![11] = dexN(2L)
                        pc = (if ((!(dexEqual((v)!![9], (v)!![11])))) 0xa4 else 0x89)
                        continue
                }
                0x89 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![9])))) 0xa8 else 0x8f)
                        continue
                }
                0x8f -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![9])))) 0xa8 else 0x95)
                        continue
                }
                0x95 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![12]) as Any?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![9])))) 0xa8 else 0x9b)
                        continue
                }
                0x9b -> {
                        (v)!![9] = "转账成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![9]) as Any?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((dexZero((v)!![9]))) 0xa4 else 0xa3)
                        continue
                }
                0xa3 -> {
                        pc = 0xa8
                        continue
                }
                0xa4 -> {
                        (v)!![9] = (v)!![18]
                        pc = 0x19f
                        continue
                }
                0xa8 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![9] = (result)!![0]
                        (v)!![11] = dexN(2L)
                        (v)!![9] = dexN((dexI((v)!![9]) - dexI((v)!![11])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![9])))) 0xa4 else 0xb0)
                        continue
                }
                0xb0 -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![9] = (result)!![0]
                        (v)!![9] = ((dexR((v)!![9]) as String?))
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![9]) as String?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0xd5 else 0xd2)
                        continue
                }
                0xd2 -> {
                        (((dexR((v)!![3]) as RuleBill?)))!!.setNumber(((dexR((v)!![9]) as String?)))
                        pc = 0xd5
                        continue
                }
                0xd5 -> {
                        (v)!![11] = dexN((dexI((v)!![7]) + (2)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![11])]
                        (v)!![11] = (result)!![0]
                        (v)!![11] = ((dexR((v)!![11]) as String?))
                        (result)!![0] = (((dexR((v)!![11]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![11] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![11]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![11] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![11]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![9]) as String?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x137 else 0xf7)
                        continue
                }
                0xf7 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x137 else 0xfd)
                        continue
                }
                0xfd -> {
                        (((dexR((v)!![3]) as RuleBill?)))!!.setNumber(((dexR((v)!![4]) as String?)))
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x19a else 0x104)
                        continue
                }
                0x104 -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![6]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x118 else 0x10e)
                        continue
                }
                0x10e -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x12c else 0x118)
                        continue
                }
                0x118 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![1] = dexN((dexI((v)!![1]) + (-(3))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![1])))) 0x12c else 0x120)
                        continue
                }
                0x120 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (3)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![3]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x12f
                        continue
                }
                0x12c -> {
                        (((dexR((v)!![3]) as RuleBill?)))!!.setRemark(((dexR((v)!![9]) as String?)))
                        pc = 0x12f
                        continue
                }
                0x12f -> {
                        (result)!![0] = (((dexR((v)!![3]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![3]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x19a
                        continue
                }
                0x137 -> {
                        (v)!![9] = (v)!![18]
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![9]) as Any?))))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![8])))) 0x19a else 0x13f)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_r_block1(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x13f -> {
                        (v)!![8] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        pc = (if ((!(dexZero((v)!![8])))) 0x194 else 0x143)
                        continue
                }
                0x143 -> {
                        (v)!![8] = dexN(0L)
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.d(((dexR((v)!![2]) as MutableList<String?>?)), ((dexR((v)!![5]) as String?)), dexZ((v)!![8])))
                        (v)!![9] = (result)!![0]
                        pc = (if (((dexI((v)!![9]) <= 0))) 0x15e else 0x14a)
                        continue
                }
                0x14a -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![9])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        pc = 0x15c
                        continue
                }
                0x15c -> {
                        (v)!![5] = dexN(1L)
                        pc = 0x160
                        continue
                }
                0x15e -> {
                        (v)!![1] = dexN(0L)
                        pc = 0x15c
                        continue
                }
                0x160 -> {
                        pc = (if (((dexI((v)!![9]) <= dexI((v)!![5])))) 0x194 else 0x162)
                        continue
                }
                0x162 -> {
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x194 else 0x168)
                        continue
                }
                0x168 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x194 else 0x16e)
                        continue
                }
                0x16e -> {
                        (v)!![9] = dexN((dexI((v)!![9]) + (-(1))))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![9])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (result)!![0] = (((dexR((v)!![3]) as RuleBill?)))!!.getNumber()
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![5]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x194 else 0x180)
                        continue
                }
                0x180 -> {
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![1]) as String?)))))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as RuleBill?)))!!.getNumber()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![1]) as String?)))))
                        (v)!![8] = (result)!![0]
                        (v)!![5] = dexN(java.lang.Double.doubleToRawLongBits((dexD((v)!![5]) - dexD((v)!![8]))))
                        (result)!![0] = Amounts.t(dexD((v)!![5]))
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![3]) as RuleBill?)))!!.setDiscount(((dexR((v)!![1]) as String?)))
                        pc = 0x194
                        continue
                }
                0x194 -> {
                        (((dexR((v)!![3]) as RuleBill?)))!!.setRemark(((dexR((v)!![4]) as String?)))
                        (((dexR((v)!![3]) as RuleBill?)))!!.setShopName(((dexR((v)!![4]) as String?)))
                        pc = 0x19a
                        continue
                }
                0x19a -> {
                        (v)!![13] = (v)!![22]
                        (v)!![12] = (v)!![3]
                        pc = 0x69
                        continue
                }
                0x19f -> {
                        (v)!![11] = "有退款"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        (v)!![18] = (v)!![9]
                        (v)!![9] = "-"
                        (v)!![19] = (v)!![3]
                        (v)!![3] = "+"
                        pc = (if ((!(dexZero((v)!![11])))) 0x287 else 0x1af)
                        continue
                }
                0x1af -> {
                        (v)!![11] = "自动扣款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x287 else 0x1b7)
                        continue
                }
                0x1b7 -> {
                        (v)!![11] = "已全额退款"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x287 else 0x1bf)
                        continue
                }
                0x1bf -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        (v)!![20] = (v)!![14]
                        (v)!![14] = dexN(6L)
                        pc = (if ((dexZero((v)!![11]))) 0x1cc else 0x1c8)
                        continue
                }
                0x1c8 -> {
                        (v)!![11] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        pc = (if ((dexEqual((v)!![11], (v)!![14]))) 0x289 else 0x1cc)
                        continue
                }
                0x1cc -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x1d6 else 0x1d2)
                        continue
                }
                0x1d2 -> {
                        (v)!![11] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        pc = (if ((dexEqual((v)!![11], (v)!![14]))) 0x289 else 0x1d6)
                        continue
                }
                0x1d6 -> {
                        (v)!![11] = "退款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x1de)
                        continue
                }
                0x1de -> {
                        (v)!![11] = "赔付成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x1e6)
                        continue
                }
                0x1e6 -> {
                        (v)!![11] = "交易成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x1ee)
                        continue
                }
                0x1ee -> {
                        (v)!![11] = "还款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x1f6)
                        continue
                }
                0x1f6 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![12]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x1fc)
                        continue
                }
                0x1fc -> {
                        (v)!![11] = "派送中"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x204)
                        continue
                }
                0x204 -> {
                        (v)!![11] = "领取中"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x20c)
                        continue
                }
                0x20c -> {
                        (v)!![11] = "等待对方发货"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x214)
                        continue
                }
                0x214 -> {
                        (v)!![11] = "等待确认收货"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x21c)
                        continue
                }
                0x21c -> {
                        (v)!![11] = "交易成功(代付成功)"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x224)
                        continue
                }
                0x224 -> {
                        (v)!![11] = "退回成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x22c)
                        continue
                }
                0x22c -> {
                        (v)!![11] = "支付成功(代付成功)"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x234)
                        continue
                }
                0x234 -> {
                        (v)!![11] = "付款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![11]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x23c)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_r_block2(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x23c -> {
                        (v)!![11] = "缴费中"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![11]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x244)
                        continue
                }
                0x244 -> {
                        (v)!![11] = "支付宝小荷包付款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![11]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x24c)
                        continue
                }
                0x24c -> {
                        (v)!![11] = "已退款("
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![11]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x254)
                        continue
                }
                0x254 -> {
                        (v)!![11] = "亲情卡付款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![11]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x25c)
                        continue
                }
                0x25c -> {
                        (v)!![11] = "免密支付成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x264)
                        continue
                }
                0x264 -> {
                        (v)!![11] = "自动续费成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x26c)
                        continue
                }
                0x26c -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x272)
                        continue
                }
                0x272 -> {
                        (v)!![11] = "收款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x27a)
                        continue
                }
                0x27a -> {
                        (v)!![11] = "等待对方确认收货"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x283 else 0x282)
                        continue
                }
                0x282 -> {
                        pc = 0x289
                        continue
                }
                0x283 -> {
                        (v)!![12] = (v)!![19]
                        pc = 0x347
                        continue
                }
                0x287 -> {
                        (v)!![20] = (v)!![14]
                        pc = 0x289
                        continue
                }
                0x289 -> {
                        pc = (if (((dexI((v)!![7]) <= 0))) 0x283 else 0x28b)
                        continue
                }
                0x28b -> {
                        (v)!![11] = dexN((dexI((v)!![7]) + (-(1))))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![11])]
                        (v)!![12] = (result)!![0]
                        (v)!![12] = ((dexR((v)!![12]) as String?))
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x334 else 0x2b5)
                        continue
                }
                0x2b5 -> {
                        (v)!![12] = (v)!![19]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setNumber(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![11])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x2df else 0x2c6)
                        continue
                }
                0x2c6 -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![11])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![9]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x2e1 else 0x2d2)
                        continue
                }
                0x2d2 -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![11])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![15]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x2df else 0x2de)
                        continue
                }
                0x2de -> {
                        pc = 0x2e1
                        continue
                }
                0x2df -> {
                        (v)!![1] = dexN(1L)
                        pc = 0x2ec
                        continue
                }
                0x2e1 -> {
                        (v)!![1] = "退回成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x2ea else 0x2e9)
                        continue
                }
                0x2e9 -> {
                        pc = 0x2df
                        continue
                }
                0x2ea -> {
                        (v)!![1] = dexN(1L)
                        pc = 0x2ef
                        continue
                }
                0x2ec -> {
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![1]))
                        pc = 0x2ef
                        continue
                }
                0x2ef -> {
                        (v)!![3] = "扣款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x2fb else 0x2f7)
                        continue
                }
                0x2f7 -> {
                        (v)!![3] = dexN(0L)
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![3]))
                        pc = 0x2fb
                        continue
                }
                0x2fb -> {
                        pc = (if (((dexI((v)!![7]) <= dexI((v)!![1])))) 0x308 else 0x2fd)
                        continue
                }
                0x2fd -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (-(2))))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x308
                        continue
                }
                0x308 -> {
                        (v)!![1] = "退款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x32c else 0x310)
                        continue
                }
                0x310 -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "退款-来自"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (v)!![1] = dexN(1L)
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![1]))
                        pc = 0x32c
                        continue
                }
                0x32c -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x343
                        continue
                }
                0x334 -> {
                        (v)!![12] = (v)!![19]
                        (v)!![3] = "NaN"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x343 else 0x33e)
                        continue
                }
                0x33e -> {
                        (v)!![1] = "0"
                        (((dexR((v)!![12]) as RuleBill?)))!!.setNumber(((dexR((v)!![1]) as String?)))
                        pc = 0x343
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_r_block3(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x343 -> {
                        (v)!![13] = (v)!![22]
                        pc = 0x69
                        continue
                }
                0x347 -> {
                        (v)!![11] = "收款理由"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x369 else 0x34f)
                        continue
                }
                0x34f -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![11] = (result)!![0]
                        (v)!![13] = dexN(1L)
                        (v)!![11] = dexN((dexI((v)!![11]) - dexI((v)!![13])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![11])))) 0x369 else 0x357)
                        continue
                }
                0x357 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "收款理由"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x343
                        continue
                }
                0x369 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![11] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![11]) as CharSequence?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x392 else 0x373)
                        continue
                }
                0x373 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![5]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x396 else 0x379)
                        continue
                }
                0x379 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![6]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x396 else 0x37f)
                        continue
                }
                0x37f -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![9]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x396 else 0x385)
                        continue
                }
                0x385 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x396 else 0x38b)
                        continue
                }
                0x38b -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![15]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x392 else 0x391)
                        continue
                }
                0x391 -> {
                        pc = 0x396
                        continue
                }
                0x392 -> {
                        (v)!![11] = (v)!![18]
                        pc = 0x42b
                        continue
                }
                0x396 -> {
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![9])))) 0x3d5 else 0x3b4)
                        continue
                }
                0x3b4 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![9] = (result)!![0]
                        (v)!![11] = dexN(1L)
                        (v)!![9] = dexN((dexI((v)!![9]) - dexI((v)!![11])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![9])))) 0x3d5 else 0x3bc)
                        continue
                }
                0x3bc -> {
                        (v)!![4] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![8] = (v)!![4]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        pc = 0x3d5
                        continue
                }
                0x3d5 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x40b else 0x3db)
                        continue
                }
                0x3db -> {
                        (v)!![5] = DexReference()
                        ((((v)!![5] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![4]) as String?)))))
                        (v)!![9] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![9]))))
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.append(dexD((v)!![9]))
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.append(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setNumber(((dexR((v)!![1]) as String?)))
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x40b else 0x3fb)
                        continue
                }
                0x3fb -> {
                        (v)!![1] = "支付宝收款"
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        (v)!![1] = dexN(1L)
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![1]))
                        pc = 0x40b
                        continue
                }
                0x40b -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x343 else 0x415)
                        continue
                }
                0x415 -> {
                        pc = (if (((dexI((v)!![7]) <= 0))) 0x343 else 0x417)
                        continue
                }
                0x417 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (-(1))))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x343
                        continue
                }
                0x42b -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x44c else 0x431)
                        continue
                }
                0x431 -> {
                        (v)!![11] = "付款信息"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x44c else 0x439)
                        continue
                }
                0x439 -> {
                        (v)!![11] = "交易方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x44c else 0x441)
                        continue
                }
                0x441 -> {
                        (v)!![11] = "退款方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x44a else 0x449)
                        continue
                }
                0x449 -> {
                        pc = 0x44c
                        continue
                }
                0x44a -> {
                        (v)!![11] = dexN(0L)
                        pc = 0x4a4
                        continue
                }
                0x44c -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![11] = (result)!![0]
                        (v)!![13] = dexN(1L)
                        (v)!![11] = dexN((dexI((v)!![11]) - dexI((v)!![13])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![11])))) 0x44a else 0x454)
                        continue
                }
                0x454 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![1]) as String?)))
                        (v)!![1] = "帮助"
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x47e else 0x46b)
                        continue
                }
                0x46b -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![3] = dexN(2L)
                        (v)!![1] = dexN((dexI((v)!![1]) - dexI((v)!![3])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![1])))) 0x47e else 0x473)
                        continue
                }
                0x473 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (2)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![1]) as String?)))
                        pc = 0x47e
                        continue
                }
                0x47e -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x4a2 else 0x488)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_r_block4(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x488 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![1] = (result)!![0]
                        (v)!![3] = "亲情卡"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x4a2 else 0x494)
                        continue
                }
                0x494 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as RuleBill?)))!!.isIncome())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x4a2 else 0x49a)
                        continue
                }
                0x49a -> {
                        (v)!![11] = dexN(0L)
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![11]))
                        pc = 0x49e
                        continue
                }
                0x49e -> {
                        (v)!![13] = (v)!![22]
                        pc = 0x902
                        continue
                }
                0x4a2 -> {
                        (v)!![11] = dexN(0L)
                        pc = 0x49e
                        continue
                }
                0x4a4 -> {
                        (v)!![13] = "创建时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        (v)!![14] = "yyyy-MM-dd HH:mm:ss"
                        pc = (if ((dexZero((v)!![13]))) 0x4cc else 0x4ae)
                        continue
                }
                0x4ae -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![15] = dexN(1L)
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![15])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x4cc else 0x4b6)
                        continue
                }
                0x4b6 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "创建时间"
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![1]) as String?)), ((dexR((v)!![14]) as String?))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setTime(dexL((v)!![3]))
                        pc = 0x49e
                        continue
                }
                0x4cc -> {
                        (v)!![13] = "支付时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x50a else 0x4d4)
                        continue
                }
                0x4d4 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![15] = dexN(1L)
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![15])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x50a else 0x4dc)
                        continue
                }
                0x4dc -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "支付时间"
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![9]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x4f8 else 0x4f0)
                        continue
                }
                0x4f0 -> {
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![1]) as String?)), ((dexR((v)!![14]) as String?))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setTime(dexL((v)!![3]))
                        pc = 0x49e
                        continue
                }
                0x4f8 -> {
                        (v)!![3] = "年"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x49e else 0x500)
                        continue
                }
                0x500 -> {
                        (v)!![3] = "yyyy年MM月dd日 HH:mm:ss"
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![1]) as String?)), ((dexR((v)!![3]) as String?))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setTime(dexL((v)!![3]))
                        pc = 0x49e
                        continue
                }
                0x50a -> {
                        (v)!![13] = "收款时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x52e else 0x512)
                        continue
                }
                0x512 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![15] = dexN(1L)
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![15])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x52f else 0x51a)
                        continue
                }
                0x51a -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![1]) as String?)), ((dexR((v)!![14]) as String?))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setTime(dexL((v)!![3]))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![15]))
                        pc = 0x49e
                        continue
                }
                0x52e -> {
                        (v)!![15] = dexN(1L)
                        pc = 0x52f
                        continue
                }
                0x52f -> {
                        (v)!![13] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        (v)!![14] = dexN(7L)
                        pc = (if ((!(dexEqual((v)!![13], (v)!![14])))) 0x56e else 0x534)
                        continue
                }
                0x534 -> {
                        (v)!![13] = "订单金额"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x56e else 0x53c)
                        continue
                }
                0x53c -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![15])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x56e else 0x543)
                        continue
                }
                0x543 -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x49e else 0x569)
                        continue
                }
                0x569 -> {
                        (((dexR((v)!![12]) as RuleBill?)))!!.setNumber(((dexR((v)!![1]) as String?)))
                        pc = 0x49e
                        continue
                }
                0x56e -> {
                        (v)!![13] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        pc = (if ((!(dexEqual((v)!![13], (v)!![14])))) 0x5d1 else 0x572)
                        continue
                }
                0x572 -> {
                        (v)!![13] = "花呗收钱服务费"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![13])))) 0x582 else 0x57a)
                        continue
                }
                0x57a -> {
                        (v)!![13] = "平台服务费"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x5d1 else 0x582)
                        continue
                }
                0x582 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![15] = dexN(1L)
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![15])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x5d1 else 0x58a)
                        continue
                }
                0x58a -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x49e else 0x5b0)
                        continue
                }
                0x5b0 -> {
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![5]) as String?)))))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![8] = (result)!![0]
                        (v)!![5] = dexN(java.lang.Double.doubleToRawLongBits((dexD((v)!![5]) - dexD((v)!![8]))))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(dexD((v)!![5]))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setNumber(((dexR((v)!![1]) as String?)))
                        pc = 0x49e
                        continue
                }
                0x5d1 -> {
                        (v)!![13] = "商品说明"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x611 else 0x5d9)
                        continue
                }
                0x5d9 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![15] = dexN(1L)
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![15])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x611 else 0x5e1)
                        continue
                }
                0x5e1 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "商品说明"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (v)!![3] = "余额宝"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x49e else 0x5fe)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_r_block5(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x5fe -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (v)!![3] = "收益发放"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x49e else 0x60a)
                        continue
                }
                0x60a -> {
                        (v)!![1] = "余额宝"
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![1]) as String?)))
                        pc = 0x49e
                        continue
                }
                0x611 -> {
                        (v)!![13] = "订单详情"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x65a else 0x619)
                        continue
                }
                0x619 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![15] = dexN(1L)
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![15])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x65a else 0x621)
                        continue
                }
                0x621 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![3] = (result)!![0]
                        (v)!![4] = (v)!![20]
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x638 else 0x62f)
                        continue
                }
                0x62f -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x638
                        continue
                }
                0x638 -> {
                        (v)!![1] = "商品图片"
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x49e else 0x644)
                        continue
                }
                0x644 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (2)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (v)!![13] = (v)!![22]
                        RuleBridge.E(((dexR((v)!![13]) as RuleContext?)), ((dexR((v)!![1]) as String?)))
                        pc = 0x902
                        continue
                }
                0x65a -> {
                        (v)!![13] = (v)!![22]
                        (v)!![15] = "交易详情"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((dexZero((v)!![15]))) 0x6b6 else 0x664)
                        continue
                }
                0x664 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![15] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![15] = dexN((dexI((v)!![15]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![15])))) 0x6b6 else 0x66e)
                        continue
                }
                0x66e -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x902 else 0x67c)
                        continue
                }
                0x67c -> {
                        (v)!![3] = "更多"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x902 else 0x684)
                        continue
                }
                0x684 -> {
                        (v)!![3] = "推荐服务"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x902 else 0x68c)
                        continue
                }
                0x68c -> {
                        (v)!![3] = "服务推荐"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x902 else 0x694)
                        continue
                }
                0x694 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![3] = dexN(2L)
                        (v)!![1] = dexN((dexI((v)!![1]) - dexI((v)!![3])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![1])))) 0x902 else 0x69c)
                        continue
                }
                0x69c -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (2)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x902 else 0x6aa)
                        continue
                }
                0x6aa -> {
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        RuleBridge.E(((dexR((v)!![13]) as RuleContext?)), ((dexR((v)!![1]) as String?)))
                        pc = 0x902
                        continue
                }
                0x6b6 -> {
                        (v)!![15] = "缴费说明"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((dexZero((v)!![15]))) 0x6db else 0x6be)
                        continue
                }
                0x6be -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![15] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![15] = dexN((dexI((v)!![15]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![15])))) 0x6db else 0x6c8)
                        continue
                }
                0x6c8 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "缴费说明"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x902
                        continue
                }
                0x6db -> {
                        (v)!![15] = "充值说明"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((dexZero((v)!![15]))) 0x700 else 0x6e3)
                        continue
                }
                0x6e3 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![15] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![15] = dexN((dexI((v)!![15]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![15])))) 0x700 else 0x6ed)
                        continue
                }
                0x6ed -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "充值说明"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x902
                        continue
                }
                0x700 -> {
                        (v)!![15] = "付款备注"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((dexZero((v)!![15]))) 0x725 else 0x708)
                        continue
                }
                0x708 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![15] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![15] = dexN((dexI((v)!![15]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![15])))) 0x725 else 0x712)
                        continue
                }
                0x712 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "付款备注"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x902
                        continue
                }
                0x725 -> {
                        (v)!![15] = "红包说明"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((dexZero((v)!![15]))) 0x74a else 0x72d)
                        continue
                }
                0x72d -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![15] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![15] = dexN((dexI((v)!![15]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![15])))) 0x74a else 0x737)
                        continue
                }
                0x737 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "红包说明"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x902
                        continue
                }
                0x74a -> {
                        (v)!![15] = "理由"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((dexZero((v)!![15]))) 0x76f else 0x752)
                        continue
                }
                0x752 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![15] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![15] = dexN((dexI((v)!![15]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![15])))) 0x76f else 0x75c)
                        continue
                }
                0x75c -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "理由"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x902
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_r_block6(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x76f -> {
                        (v)!![15] = "转账备注"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((dexZero((v)!![15]))) 0x812 else 0x777)
                        continue
                }
                0x777 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![15] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![15] = dexN((dexI((v)!![15]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![15])))) 0x812 else 0x781)
                        continue
                }
                0x781 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "转账"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x7cc else 0x791)
                        continue
                }
                0x791 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as RuleBill?)))!!.isIncome())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x7b3 else 0x797)
                        continue
                }
                0x797 -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "收到"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getShopName()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![4]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x7d5
                        continue
                }
                0x7b3 -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "转账给"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getShopName()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x7d5
                        continue
                }
                0x7cc -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x7d5
                        continue
                }
                0x7d5 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as RuleBill?)))!!.isIncome())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x7f8 else 0x7db)
                        continue
                }
                0x7db -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "收到"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getShopName()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![4]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x902
                        continue
                }
                0x7f8 -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "转账给"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getShopName()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x902
                        continue
                }
                0x812 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getDiscount()
                        (v)!![15] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![15]) as CharSequence?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((dexZero((v)!![15]))) 0x8aa else 0x81c)
                        continue
                }
                0x81c -> {
                        (v)!![15] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        pc = (if ((dexZero((v)!![15]))) 0x828 else 0x820)
                        continue
                }
                0x820 -> {
                        (v)!![15] = "-￥"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.startsWith((((dexR((v)!![15]) as String?)))!!))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![15])))) 0x860 else 0x828)
                        continue
                }
                0x828 -> {
                        (v)!![15] = "碰一下支付立减"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![15])))) 0x860 else 0x830)
                        continue
                }
                0x830 -> {
                        (v)!![15] = "支付宝随机立减"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![15])))) 0x860 else 0x838)
                        continue
                }
                0x838 -> {
                        (v)!![15] = "碰一下立减"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![15])))) 0x860 else 0x840)
                        continue
                }
                0x840 -> {
                        (v)!![15] = "碰一下共减"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![15])))) 0x860 else 0x848)
                        continue
                }
                0x848 -> {
                        (v)!![15] = "视频红包"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![15])))) 0x860 else 0x850)
                        continue
                }
                0x850 -> {
                        (v)!![15] = "优惠"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![15])))) 0x860 else 0x858)
                        continue
                }
                0x858 -> {
                        (v)!![15] = "网商银行福利金抵扣"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((dexZero((v)!![15]))) 0x8aa else 0x860)
                        continue
                }
                0x860 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![15] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![15] = dexN((dexI((v)!![15]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![15])))) 0x8aa else 0x86a)
                        continue
                }
                0x86a -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x888 else 0x878)
                        continue
                }
                0x878 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![15] = dexN(2L)
                        (v)!![14] = dexN((dexI((v)!![14]) - dexI((v)!![15])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x888 else 0x880)
                        continue
                }
                0x880 -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (2)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        pc = 0x888
                        continue
                }
                0x888 -> {
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x902 else 0x8a6)
                        continue
                }
                0x8a6 -> {
                        (((dexR((v)!![12]) as RuleBill?)))!!.setDiscount(((dexR((v)!![1]) as String?)))
                        pc = 0x902
                        continue
                }
                0x8aa -> {
                        (v)!![15] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        pc = (if ((dexEqual((v)!![15], (v)!![14]))) 0x902 else 0x8ae)
                        continue
                }
                0x8ae -> {
                        (v)!![14] = "订单金额"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((dexZero((v)!![8]))) 0x902 else 0x8b6)
                        continue
                }
                0x8b6 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![8] = (result)!![0]
                        (v)!![14] = dexN(1L)
                        (v)!![8] = dexN((dexI((v)!![8]) - dexI((v)!![14])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![8])))) 0x902 else 0x8be)
                        continue
                }
                0x8be -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x902 else 0x8e4)
                        continue
                }
                0x8e4 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![3]) as CharSequence?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x902 else 0x8ee)
                        continue
                }
                0x8ee -> {
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![1]) as String?)))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![1]) as String?)))))
                        (v)!![5] = (result)!![0]
                        (v)!![3] = dexN(java.lang.Double.doubleToRawLongBits((dexD((v)!![3]) - dexD((v)!![5]))))
                        (result)!![0] = Amounts.t(dexD((v)!![3]))
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setDiscount(((dexR((v)!![1]) as String?)))
                        pc = 0x902
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_r_block7(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x902 -> {
                        (v)!![7] = dexN((dexI((v)!![7]) + (1)))
                        (v)!![3] = (v)!![12]
                        (v)!![1] = (v)!![13]
                        (v)!![5] = (v)!![17]
                        (v)!![4] = dexN(0L)
                        pc = 0x37
                        continue
                }
                0x90b -> {
                        (v)!![12] = (v)!![3]
                        (v)!![17] = (v)!![5]
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x941 else 0x918)
                        continue
                }
                0x918 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x941 else 0x922)
                        continue
                }
                0x922 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as RuleBill?)))!!.isIncome())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x935 else 0x928)
                        continue
                }
                0x928 -> {
                        (v)!![1] = "支付宝收款"
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x941
                        continue
                }
                0x935 -> {
                        (v)!![1] = "支付宝付款"
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x941
                        continue
                }
                0x941 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as RuleBill?)))!!.isIncome())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x957 else 0x947)
                        continue
                }
                0x947 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x957 else 0x951)
                        continue
                }
                0x951 -> {
                        (v)!![1] = (v)!![17]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![1]) as String?)))
                        pc = 0x959
                        continue
                }
                0x957 -> {
                        (v)!![1] = (v)!![17]
                        pc = 0x959
                        continue
                }
                0x959 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![2] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![2]) as CharSequence?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![2])))) 0x96e else 0x963)
                        continue
                }
                0x963 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![2] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![2]) as String?)))!!.trim()
                        (v)!![2] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![2]) as String?)))
                        pc = 0x96e
                        continue
                }
                0x96e -> {
                        (v)!![2] = "账户余额"
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![2]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![2])))) 0x992 else 0x97a)
                        continue
                }
                0x97a -> {
                        (v)!![2] = "余额"
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![2]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![2])))) 0x992 else 0x986)
                        continue
                }
                0x986 -> {
                        (v)!![2] = "可用余额"
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![2]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((dexZero((v)!![2]))) 0x995 else 0x992)
                        continue
                }
                0x992 -> {
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![1]) as String?)))
                        pc = 0x995
                        continue
                }
                0x995 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x9aa else 0x99f)
                        continue
                }
                0x99f -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x9aa else 0x9a9)
                        continue
                }
                0x9a9 -> {
                        (result)!![1] = (v)!![12]
                        return -(1)
                }
                0x9aa -> {
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

    fun s(arg0: RuleContext?, arg1: MutableList<String?>?): RuleBill? {
        var v: Array<Any?>? = arrayOfNulls<Any>(24)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![21] = this
        (v)!![22] = arg0
        (v)!![23] = arg1
        while (true) {
            if ((pc == -(1))) {
                return ((dexR((result)!![1]) as RuleBill?))
            }
            if ((pc < 0x13f)) pc = recovered_s_block0(v, result, pc) else if ((pc < 0x23c)) pc = recovered_s_block1(v, result, pc) else if ((pc < 0x343)) pc = recovered_s_block2(v, result, pc) else if ((pc < 0x488)) pc = recovered_s_block3(v, result, pc) else if ((pc < 0x5fe)) pc = recovered_s_block4(v, result, pc) else if ((pc < 0x76f)) pc = recovered_s_block5(v, result, pc) else if ((pc < 0x907)) pc = recovered_s_block6(v, result, pc) else pc = recovered_s_block7(v, result, pc)
        }
    }

    private fun recovered_s_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (v)!![0] = (v)!![21]
                        (v)!![1] = (v)!![22]
                        (v)!![2] = (v)!![23]
                        (result)!![0] = dexN((if (((((dexR((v)!![23]) as MutableList<String?>?)))!!.isEmpty())) 1 else 0))
                        (v)!![3] = (result)!![0]
                        (v)!![4] = dexN(0L)
                        pc = (if ((dexZero((v)!![3]))) 0xe else 0xd)
                        continue
                }
                0xd -> {
                        (result)!![1] = (v)!![4]
                        return -(1)
                }
                0xe -> {
                        (v)!![3] = DexReference()
                        ((((v)!![3] as DexReference?)))!!.value = RuleBill()
                        (v)!![5] = "支付宝"
                        (((dexR((v)!![3]) as RuleBill?)))!!.setOrigin(((dexR((v)!![5]) as String?)))
                        (result)!![0] = dexN((if ((RuleSettings.i())) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((dexZero((v)!![6]))) 0x36 else 0x1e)
                        continue
                }
                0x1e -> {
                        (v)!![6] = dexN((((dexR((v)!![0]) as AlipayRule?)))!!.f13678e)
                        (result)!![0] = dexN(System.currentTimeMillis())
                        (v)!![8] = (result)!![0]
                        (result)!![0] = dexN((if ((Dates.m0(dexL((v)!![6]), dexL((v)!![8])))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((dexZero((v)!![6]))) 0x30 else 0x2a)
                        continue
                }
                0x2a -> {
                        (v)!![6] = dexN((((dexR((v)!![0]) as AlipayRule?)))!!.f13678e)
                        (((dexR((v)!![3]) as RuleBill?)))!!.setTime(dexL((v)!![6]))
                        pc = 0x36
                        continue
                }
                0x30 -> {
                        (result)!![0] = dexN(System.currentTimeMillis())
                        (v)!![6] = (result)!![0]
                        (((dexR((v)!![0]) as AlipayRule?)))!!.f13678e = dexL((v)!![6])
                        pc = 0x36
                        continue
                }
                0x36 -> {
                        (v)!![7] = dexN(0L)
                        pc = 0x37
                        continue
                }
                0x37 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![8] = (result)!![0]
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![8])))) 0x907 else 0x3d)
                        continue
                }
                0x3d -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![7])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (v)!![9] = "收款方"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![9]) as Any?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        (v)!![10] = dexN(1L)
                        pc = (if ((dexZero((v)!![9]))) 0x6c else 0x4c)
                        continue
                }
                0x4c -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![9] = (result)!![0]
                        (v)!![9] = dexN((dexI((v)!![9]) - dexI((v)!![10])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![9])))) 0x6c else 0x53)
                        continue
                }
                0x53 -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (((dexR((v)!![3]) as RuleBill?)))!!.setRemark(((dexR((v)!![8]) as String?)))
                        (result)!![0] = (((dexR((v)!![3]) as RuleBill?)))!!.getRemark()
                        (v)!![8] = (result)!![0]
                        (((dexR((v)!![3]) as RuleBill?)))!!.setShopName(((dexR((v)!![8]) as String?)))
                        (v)!![13] = (v)!![1]
                        (v)!![12] = (v)!![3]
                        (v)!![17] = (v)!![5]
                        pc = 0x69
                        continue
                }
                0x69 -> {
                        (v)!![11] = dexN(0L)
                        pc = 0x8fe
                        continue
                }
                0x6c -> {
                        (v)!![9] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        (v)!![11] = "Payment method"
                        (v)!![12] = "代付成功"
                        (v)!![13] = "充值成功"
                        (v)!![14] = "支付成功"
                        (v)!![15] = "支出"
                        (v)!![4] = "元"
                        (v)!![10] = ","
                        (v)!![6] = "¥"
                        (v)!![17] = (v)!![5]
                        (v)!![5] = "￥"
                        (v)!![1] = ""
                        (v)!![18] = (v)!![11]
                        (v)!![11] = dexN(2L)
                        pc = (if ((!(dexEqual((v)!![9], (v)!![11])))) 0xa4 else 0x89)
                        continue
                }
                0x89 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![9])))) 0xa8 else 0x8f)
                        continue
                }
                0x8f -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![9])))) 0xa8 else 0x95)
                        continue
                }
                0x95 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![12]) as Any?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![9])))) 0xa8 else 0x9b)
                        continue
                }
                0x9b -> {
                        (v)!![9] = "转账成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![9]) as Any?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((dexZero((v)!![9]))) 0xa4 else 0xa3)
                        continue
                }
                0xa3 -> {
                        pc = 0xa8
                        continue
                }
                0xa4 -> {
                        (v)!![9] = (v)!![18]
                        pc = 0x19f
                        continue
                }
                0xa8 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![9] = (result)!![0]
                        (v)!![11] = dexN(2L)
                        (v)!![9] = dexN((dexI((v)!![9]) - dexI((v)!![11])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![9])))) 0xa4 else 0xb0)
                        continue
                }
                0xb0 -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![9] = (result)!![0]
                        (v)!![9] = ((dexR((v)!![9]) as String?))
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![9]) as String?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0xd5 else 0xd2)
                        continue
                }
                0xd2 -> {
                        (((dexR((v)!![3]) as RuleBill?)))!!.setNumber(((dexR((v)!![9]) as String?)))
                        pc = 0xd5
                        continue
                }
                0xd5 -> {
                        (v)!![11] = dexN((dexI((v)!![7]) + (2)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![11])]
                        (v)!![11] = (result)!![0]
                        (v)!![11] = ((dexR((v)!![11]) as String?))
                        (result)!![0] = (((dexR((v)!![11]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![11] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![11]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![11] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![11]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![9]) as String?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x137 else 0xf7)
                        continue
                }
                0xf7 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x137 else 0xfd)
                        continue
                }
                0xfd -> {
                        (((dexR((v)!![3]) as RuleBill?)))!!.setNumber(((dexR((v)!![4]) as String?)))
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x19a else 0x104)
                        continue
                }
                0x104 -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![6]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x118 else 0x10e)
                        continue
                }
                0x10e -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![5]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x12c else 0x118)
                        continue
                }
                0x118 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![1] = dexN((dexI((v)!![1]) + (-(3))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![1])))) 0x12c else 0x120)
                        continue
                }
                0x120 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (3)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![3]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x12f
                        continue
                }
                0x12c -> {
                        (((dexR((v)!![3]) as RuleBill?)))!!.setRemark(((dexR((v)!![9]) as String?)))
                        pc = 0x12f
                        continue
                }
                0x12f -> {
                        (result)!![0] = (((dexR((v)!![3]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![3]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x19a
                        continue
                }
                0x137 -> {
                        (v)!![9] = (v)!![18]
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![9]) as Any?))))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![8])))) 0x19a else 0x13f)
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
                0x13f -> {
                        (v)!![8] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        pc = (if ((!(dexZero((v)!![8])))) 0x194 else 0x143)
                        continue
                }
                0x143 -> {
                        (v)!![8] = dexN(0L)
                        (result)!![0] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.d(((dexR((v)!![2]) as MutableList<String?>?)), ((dexR((v)!![5]) as String?)), dexZ((v)!![8])))
                        (v)!![9] = (result)!![0]
                        pc = (if (((dexI((v)!![9]) <= 0))) 0x15e else 0x14a)
                        continue
                }
                0x14a -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![9])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        pc = 0x15c
                        continue
                }
                0x15c -> {
                        (v)!![5] = dexN(1L)
                        pc = 0x160
                        continue
                }
                0x15e -> {
                        (v)!![1] = dexN(0L)
                        pc = 0x15c
                        continue
                }
                0x160 -> {
                        pc = (if (((dexI((v)!![9]) <= dexI((v)!![5])))) 0x194 else 0x162)
                        continue
                }
                0x162 -> {
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![5])))) 0x194 else 0x168)
                        continue
                }
                0x168 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x194 else 0x16e)
                        continue
                }
                0x16e -> {
                        (v)!![9] = dexN((dexI((v)!![9]) + (-(1))))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![9])]
                        (v)!![4] = (result)!![0]
                        (v)!![4] = ((dexR((v)!![4]) as String?))
                        (result)!![0] = (((dexR((v)!![3]) as RuleBill?)))!!.getNumber()
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![5]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x194 else 0x180)
                        continue
                }
                0x180 -> {
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![1]) as String?)))))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as RuleBill?)))!!.getNumber()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![1]) as String?)))))
                        (v)!![8] = (result)!![0]
                        (v)!![5] = dexN(java.lang.Double.doubleToRawLongBits((dexD((v)!![5]) - dexD((v)!![8]))))
                        (result)!![0] = Amounts.t(dexD((v)!![5]))
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![3]) as RuleBill?)))!!.setDiscount(((dexR((v)!![1]) as String?)))
                        pc = 0x194
                        continue
                }
                0x194 -> {
                        (((dexR((v)!![3]) as RuleBill?)))!!.setRemark(((dexR((v)!![4]) as String?)))
                        (((dexR((v)!![3]) as RuleBill?)))!!.setShopName(((dexR((v)!![4]) as String?)))
                        pc = 0x19a
                        continue
                }
                0x19a -> {
                        (v)!![13] = (v)!![22]
                        (v)!![12] = (v)!![3]
                        pc = 0x69
                        continue
                }
                0x19f -> {
                        (v)!![11] = "有退款"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        (v)!![18] = (v)!![9]
                        (v)!![9] = "-"
                        (v)!![19] = (v)!![3]
                        (v)!![3] = "+"
                        pc = (if ((!(dexZero((v)!![11])))) 0x287 else 0x1af)
                        continue
                }
                0x1af -> {
                        (v)!![11] = "Auto debit successful"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x287 else 0x1b7)
                        continue
                }
                0x1b7 -> {
                        (v)!![11] = "已全额退款"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x287 else 0x1bf)
                        continue
                }
                0x1bf -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        (v)!![20] = (v)!![14]
                        (v)!![14] = dexN(6L)
                        pc = (if ((dexZero((v)!![11]))) 0x1cc else 0x1c8)
                        continue
                }
                0x1c8 -> {
                        (v)!![11] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        pc = (if ((dexEqual((v)!![11], (v)!![14]))) 0x289 else 0x1cc)
                        continue
                }
                0x1cc -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x1d6 else 0x1d2)
                        continue
                }
                0x1d2 -> {
                        (v)!![11] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        pc = (if ((dexEqual((v)!![11], (v)!![14]))) 0x289 else 0x1d6)
                        continue
                }
                0x1d6 -> {
                        (v)!![11] = "退款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x1de)
                        continue
                }
                0x1de -> {
                        (v)!![11] = "赔付成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x1e6)
                        continue
                }
                0x1e6 -> {
                        (v)!![11] = "Transaction successful"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x1ee)
                        continue
                }
                0x1ee -> {
                        (v)!![11] = "还款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x1f6)
                        continue
                }
                0x1f6 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![12]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x1fc)
                        continue
                }
                0x1fc -> {
                        (v)!![11] = "派送中"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x204)
                        continue
                }
                0x204 -> {
                        (v)!![11] = "领取中"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x20c)
                        continue
                }
                0x20c -> {
                        (v)!![11] = "等待对方发货"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x214)
                        continue
                }
                0x214 -> {
                        (v)!![11] = "等待确认收货"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x21c)
                        continue
                }
                0x21c -> {
                        (v)!![11] = "交易成功(代付成功)"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x224)
                        continue
                }
                0x224 -> {
                        (v)!![11] = "退回成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x22c)
                        continue
                }
                0x22c -> {
                        (v)!![11] = "支付成功(代付成功)"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x234)
                        continue
                }
                0x234 -> {
                        (v)!![11] = "付款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![11]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x23c)
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
                0x23c -> {
                        (v)!![11] = "缴费中"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![11]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x244)
                        continue
                }
                0x244 -> {
                        (v)!![11] = "支付宝小荷包付款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![11]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x24c)
                        continue
                }
                0x24c -> {
                        (v)!![11] = "已退款("
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![11]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x254)
                        continue
                }
                0x254 -> {
                        (v)!![11] = "亲情卡付款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![11]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x25c)
                        continue
                }
                0x25c -> {
                        (v)!![11] = "免密支付成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x264)
                        continue
                }
                0x264 -> {
                        (v)!![11] = "自动续费成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x26c)
                        continue
                }
                0x26c -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x272)
                        continue
                }
                0x272 -> {
                        (v)!![11] = "收款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x289 else 0x27a)
                        continue
                }
                0x27a -> {
                        (v)!![11] = "等待对方确认收货"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x283 else 0x282)
                        continue
                }
                0x282 -> {
                        pc = 0x289
                        continue
                }
                0x283 -> {
                        (v)!![12] = (v)!![19]
                        pc = 0x347
                        continue
                }
                0x287 -> {
                        (v)!![20] = (v)!![14]
                        pc = 0x289
                        continue
                }
                0x289 -> {
                        pc = (if (((dexI((v)!![7]) <= 0))) 0x283 else 0x28b)
                        continue
                }
                0x28b -> {
                        (v)!![11] = dexN((dexI((v)!![7]) + (-(1))))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![11])]
                        (v)!![12] = (result)!![0]
                        (v)!![12] = ((dexR((v)!![12]) as String?))
                        (result)!![0] = (((dexR((v)!![12]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x334 else 0x2b5)
                        continue
                }
                0x2b5 -> {
                        (v)!![12] = (v)!![19]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setNumber(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![11])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x2df else 0x2c6)
                        continue
                }
                0x2c6 -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![11])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![9]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x2e1 else 0x2d2)
                        continue
                }
                0x2d2 -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![11])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![15]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x2df else 0x2de)
                        continue
                }
                0x2de -> {
                        pc = 0x2e1
                        continue
                }
                0x2df -> {
                        (v)!![1] = dexN(1L)
                        pc = 0x2ec
                        continue
                }
                0x2e1 -> {
                        (v)!![1] = "退回成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x2ea else 0x2e9)
                        continue
                }
                0x2e9 -> {
                        pc = 0x2df
                        continue
                }
                0x2ea -> {
                        (v)!![1] = dexN(1L)
                        pc = 0x2ef
                        continue
                }
                0x2ec -> {
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![1]))
                        pc = 0x2ef
                        continue
                }
                0x2ef -> {
                        (v)!![3] = "扣款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x2fb else 0x2f7)
                        continue
                }
                0x2f7 -> {
                        (v)!![3] = dexN(0L)
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![3]))
                        pc = 0x2fb
                        continue
                }
                0x2fb -> {
                        pc = (if (((dexI((v)!![7]) <= dexI((v)!![1])))) 0x308 else 0x2fd)
                        continue
                }
                0x2fd -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (-(2))))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x308
                        continue
                }
                0x308 -> {
                        (v)!![1] = "退款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x32c else 0x310)
                        continue
                }
                0x310 -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "退款-来自"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (v)!![1] = dexN(1L)
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![1]))
                        pc = 0x32c
                        continue
                }
                0x32c -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x343
                        continue
                }
                0x334 -> {
                        (v)!![12] = (v)!![19]
                        (v)!![3] = "NaN"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x343 else 0x33e)
                        continue
                }
                0x33e -> {
                        (v)!![1] = "0"
                        (((dexR((v)!![12]) as RuleBill?)))!!.setNumber(((dexR((v)!![1]) as String?)))
                        pc = 0x343
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
                0x343 -> {
                        (v)!![13] = (v)!![22]
                        pc = 0x69
                        continue
                }
                0x347 -> {
                        (v)!![11] = "收款理由"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x369 else 0x34f)
                        continue
                }
                0x34f -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![11] = (result)!![0]
                        (v)!![13] = dexN(1L)
                        (v)!![11] = dexN((dexI((v)!![11]) - dexI((v)!![13])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![11])))) 0x369 else 0x357)
                        continue
                }
                0x357 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "收款理由"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x343
                        continue
                }
                0x369 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![11] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![11]) as CharSequence?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x392 else 0x373)
                        continue
                }
                0x373 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![5]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x396 else 0x379)
                        continue
                }
                0x379 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![6]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x396 else 0x37f)
                        continue
                }
                0x37f -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![9]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x396 else 0x385)
                        continue
                }
                0x385 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x396 else 0x38b)
                        continue
                }
                0x38b -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![15]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x392 else 0x391)
                        continue
                }
                0x391 -> {
                        pc = 0x396
                        continue
                }
                0x392 -> {
                        (v)!![11] = (v)!![18]
                        pc = 0x42b
                        continue
                }
                0x396 -> {
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![9]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![9] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![9])))) 0x3d5 else 0x3b4)
                        continue
                }
                0x3b4 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![9] = (result)!![0]
                        (v)!![11] = dexN(1L)
                        (v)!![9] = dexN((dexI((v)!![9]) - dexI((v)!![11])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![9])))) 0x3d5 else 0x3bc)
                        continue
                }
                0x3bc -> {
                        (v)!![4] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![4])]
                        (v)!![4] = (result)!![0]
                        (v)!![8] = (v)!![4]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        pc = 0x3d5
                        continue
                }
                0x3d5 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![4]) as String?))))) 1 else 0))
                        (v)!![5] = (result)!![0]
                        pc = (if ((dexZero((v)!![5]))) 0x40b else 0x3db)
                        continue
                }
                0x3db -> {
                        (v)!![5] = DexReference()
                        ((((v)!![5] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![4]) as String?)))))
                        (v)!![9] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![9]))))
                        (v)!![9] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.append(dexD((v)!![9]))
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.append(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![5]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setNumber(((dexR((v)!![1]) as String?)))
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x40b else 0x3fb)
                        continue
                }
                0x3fb -> {
                        (v)!![1] = "支付宝收款"
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        (v)!![1] = dexN(1L)
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![1]))
                        pc = 0x40b
                        continue
                }
                0x40b -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x343 else 0x415)
                        continue
                }
                0x415 -> {
                        pc = (if (((dexI((v)!![7]) <= 0))) 0x343 else 0x417)
                        continue
                }
                0x417 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (-(1))))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x343
                        continue
                }
                0x42b -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x44c else 0x431)
                        continue
                }
                0x431 -> {
                        (v)!![11] = "付款信息"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x44c else 0x439)
                        continue
                }
                0x439 -> {
                        (v)!![11] = "交易方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![11])))) 0x44c else 0x441)
                        continue
                }
                0x441 -> {
                        (v)!![11] = "退款方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![11] = (result)!![0]
                        pc = (if ((dexZero((v)!![11]))) 0x44a else 0x449)
                        continue
                }
                0x449 -> {
                        pc = 0x44c
                        continue
                }
                0x44a -> {
                        (v)!![11] = dexN(0L)
                        pc = 0x4a4
                        continue
                }
                0x44c -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![11] = (result)!![0]
                        (v)!![13] = dexN(1L)
                        (v)!![11] = dexN((dexI((v)!![11]) - dexI((v)!![13])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![11])))) 0x44a else 0x454)
                        continue
                }
                0x454 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![1]) as String?)))
                        (v)!![1] = "帮助"
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x47e else 0x46b)
                        continue
                }
                0x46b -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![3] = dexN(2L)
                        (v)!![1] = dexN((dexI((v)!![1]) - dexI((v)!![3])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![1])))) 0x47e else 0x473)
                        continue
                }
                0x473 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (2)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![1]) as String?)))
                        pc = 0x47e
                        continue
                }
                0x47e -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x4a2 else 0x488)
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
                0x488 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![1] = (result)!![0]
                        (v)!![3] = "亲情卡"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x4a2 else 0x494)
                        continue
                }
                0x494 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as RuleBill?)))!!.isIncome())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x4a2 else 0x49a)
                        continue
                }
                0x49a -> {
                        (v)!![11] = dexN(0L)
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![11]))
                        pc = 0x49e
                        continue
                }
                0x49e -> {
                        (v)!![13] = (v)!![22]
                        pc = 0x8fe
                        continue
                }
                0x4a2 -> {
                        (v)!![11] = dexN(0L)
                        pc = 0x49e
                        continue
                }
                0x4a4 -> {
                        (v)!![13] = "Order time"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        (v)!![15] = "yyyy-MM-dd HH:mm:ss"
                        pc = (if ((dexZero((v)!![14]))) 0x4cc else 0x4ae)
                        continue
                }
                0x4ae -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![14] = dexN((dexI((v)!![14]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x4cc else 0x4b8)
                        continue
                }
                0x4b8 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![1]) as String?)), ((dexR((v)!![15]) as String?))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setTime(dexL((v)!![3]))
                        pc = 0x49e
                        continue
                }
                0x4cc -> {
                        (v)!![14] = "Payment time"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x50a else 0x4d4)
                        continue
                }
                0x4d4 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![14] = dexN((dexI((v)!![14]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x50a else 0x4de)
                        continue
                }
                0x4de -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![9]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x4f8 else 0x4f0)
                        continue
                }
                0x4f0 -> {
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![1]) as String?)), ((dexR((v)!![15]) as String?))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setTime(dexL((v)!![3]))
                        pc = 0x49e
                        continue
                }
                0x4f8 -> {
                        (v)!![3] = "年"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x49e else 0x500)
                        continue
                }
                0x500 -> {
                        (v)!![3] = "yyyy年MM月dd日 HH:mm:ss"
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![1]) as String?)), ((dexR((v)!![3]) as String?))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setTime(dexL((v)!![3]))
                        pc = 0x49e
                        continue
                }
                0x50a -> {
                        (v)!![13] = "收款时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x52e else 0x512)
                        continue
                }
                0x512 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![14] = dexN(1L)
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![14])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x52f else 0x51a)
                        continue
                }
                0x51a -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![1]) as String?)), ((dexR((v)!![15]) as String?))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setTime(dexL((v)!![3]))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setIncome(dexZ((v)!![14]))
                        pc = 0x49e
                        continue
                }
                0x52e -> {
                        (v)!![14] = dexN(1L)
                        pc = 0x52f
                        continue
                }
                0x52f -> {
                        (v)!![13] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        (v)!![15] = dexN(7L)
                        pc = (if ((!(dexEqual((v)!![13], (v)!![15])))) 0x56e else 0x534)
                        continue
                }
                0x534 -> {
                        (v)!![13] = "订单金额"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x56e else 0x53c)
                        continue
                }
                0x53c -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![14])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x56e else 0x543)
                        continue
                }
                0x543 -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x49e else 0x569)
                        continue
                }
                0x569 -> {
                        (((dexR((v)!![12]) as RuleBill?)))!!.setNumber(((dexR((v)!![1]) as String?)))
                        pc = 0x49e
                        continue
                }
                0x56e -> {
                        (v)!![13] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        pc = (if ((!(dexEqual((v)!![13], (v)!![15])))) 0x5d1 else 0x572)
                        continue
                }
                0x572 -> {
                        (v)!![13] = "花呗收钱服务费"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![13])))) 0x582 else 0x57a)
                        continue
                }
                0x57a -> {
                        (v)!![13] = "平台服务费"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x5d1 else 0x582)
                        continue
                }
                0x582 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![14] = dexN(1L)
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![14])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x5d1 else 0x58a)
                        continue
                }
                0x58a -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x49e else 0x5b0)
                        continue
                }
                0x5b0 -> {
                        (v)!![4] = DexReference()
                        ((((v)!![4] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![5]) as String?)))))
                        (v)!![5] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![8] = (result)!![0]
                        (v)!![5] = dexN(java.lang.Double.doubleToRawLongBits((dexD((v)!![5]) - dexD((v)!![8]))))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(dexD((v)!![5]))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.append(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![4]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setNumber(((dexR((v)!![1]) as String?)))
                        pc = 0x49e
                        continue
                }
                0x5d1 -> {
                        (v)!![13] = "Item description"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x611 else 0x5d9)
                        continue
                }
                0x5d9 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![14] = dexN(1L)
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![14])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x611 else 0x5e1)
                        continue
                }
                0x5e1 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "Item description"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (v)!![3] = "余额宝"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x49e else 0x5fe)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_s_block5(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x5fe -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (v)!![3] = "收益发放"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x49e else 0x60a)
                        continue
                }
                0x60a -> {
                        (v)!![1] = "余额宝"
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![1]) as String?)))
                        pc = 0x49e
                        continue
                }
                0x611 -> {
                        (v)!![13] = "Order Details"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x65a else 0x619)
                        continue
                }
                0x619 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![13] = (result)!![0]
                        (v)!![14] = dexN(1L)
                        (v)!![13] = dexN((dexI((v)!![13]) - dexI((v)!![14])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![13])))) 0x65a else 0x621)
                        continue
                }
                0x621 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![3] = (result)!![0]
                        (v)!![4] = (v)!![20]
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x638 else 0x62f)
                        continue
                }
                0x62f -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x638
                        continue
                }
                0x638 -> {
                        (v)!![1] = "商品图片"
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x49e else 0x644)
                        continue
                }
                0x644 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (2)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (v)!![13] = (v)!![22]
                        RuleBridge.E(((dexR((v)!![13]) as RuleContext?)), ((dexR((v)!![1]) as String?)))
                        pc = 0x8fe
                        continue
                }
                0x65a -> {
                        (v)!![13] = (v)!![22]
                        (v)!![14] = "交易详情"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x6b6 else 0x664)
                        continue
                }
                0x664 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![14] = dexN((dexI((v)!![14]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x6b6 else 0x66e)
                        continue
                }
                0x66e -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x8fe else 0x67c)
                        continue
                }
                0x67c -> {
                        (v)!![3] = "更多"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x8fe else 0x684)
                        continue
                }
                0x684 -> {
                        (v)!![3] = "推荐服务"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x8fe else 0x68c)
                        continue
                }
                0x68c -> {
                        (v)!![3] = "服务推荐"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x8fe else 0x694)
                        continue
                }
                0x694 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![1] = (result)!![0]
                        (v)!![3] = dexN(2L)
                        (v)!![1] = dexN((dexI((v)!![1]) - dexI((v)!![3])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![1])))) 0x8fe else 0x69c)
                        continue
                }
                0x69c -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (2)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x8fe else 0x6aa)
                        continue
                }
                0x6aa -> {
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        RuleBridge.E(((dexR((v)!![13]) as RuleContext?)), ((dexR((v)!![1]) as String?)))
                        pc = 0x8fe
                        continue
                }
                0x6b6 -> {
                        (v)!![14] = "Payment Instructions"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x6db else 0x6be)
                        continue
                }
                0x6be -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![14] = dexN((dexI((v)!![14]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x6db else 0x6c8)
                        continue
                }
                0x6c8 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "Payment Instructions"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x8fe
                        continue
                }
                0x6db -> {
                        (v)!![14] = "充值说明"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x700 else 0x6e3)
                        continue
                }
                0x6e3 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![14] = dexN((dexI((v)!![14]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x700 else 0x6ed)
                        continue
                }
                0x6ed -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "充值说明"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x8fe
                        continue
                }
                0x700 -> {
                        (v)!![14] = "付款备注"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x725 else 0x708)
                        continue
                }
                0x708 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![14] = dexN((dexI((v)!![14]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x725 else 0x712)
                        continue
                }
                0x712 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "付款备注"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x8fe
                        continue
                }
                0x725 -> {
                        (v)!![14] = "红包说明"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x74a else 0x72d)
                        continue
                }
                0x72d -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![14] = dexN((dexI((v)!![14]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x74a else 0x737)
                        continue
                }
                0x737 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "红包说明"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x8fe
                        continue
                }
                0x74a -> {
                        (v)!![14] = "Reason"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x76f else 0x752)
                        continue
                }
                0x752 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![14] = dexN((dexI((v)!![14]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x76f else 0x75c)
                        continue
                }
                0x75c -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "Reason"
                        (result)!![0] = replaceFirstRegex((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!, (((dexR((v)!![1]) as String?)))!!)
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x8fe
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_s_block6(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x76f -> {
                        (v)!![14] = "Transfer notes"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x816 else 0x777)
                        continue
                }
                0x777 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![14] = dexN((dexI((v)!![14]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x816 else 0x781)
                        continue
                }
                0x781 -> {
                        (v)!![1] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "Transfer"
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x7ce else 0x791)
                        continue
                }
                0x791 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as RuleBill?)))!!.isIncome())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x7b5 else 0x797)
                        continue
                }
                0x797 -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "收到"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getShopName()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (v)!![3] = "转账"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x7d7
                        continue
                }
                0x7b5 -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "转账给"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getShopName()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x7d7
                        continue
                }
                0x7ce -> {
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![1])]
                        (v)!![1] = (result)!![0]
                        (v)!![1] = ((dexR((v)!![1]) as String?))
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        pc = 0x7d7
                        continue
                }
                0x7d7 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as RuleBill?)))!!.isIncome())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x7fc else 0x7dd)
                        continue
                }
                0x7dd -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "收到"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getShopName()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (v)!![3] = "转账"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x8fe
                        continue
                }
                0x7fc -> {
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![3] = "转账给"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getShopName()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![3]) as String?)))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x8fe
                        continue
                }
                0x816 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getDiscount()
                        (v)!![14] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![14]) as CharSequence?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x8a6 else 0x820)
                        continue
                }
                0x820 -> {
                        (v)!![14] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        pc = (if ((dexZero((v)!![14]))) 0x82c else 0x824)
                        continue
                }
                0x824 -> {
                        (v)!![14] = "-￥"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.startsWith((((dexR((v)!![14]) as String?)))!!))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![14])))) 0x85c else 0x82c)
                        continue
                }
                0x82c -> {
                        (v)!![14] = "碰一下支付立减"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![14])))) 0x85c else 0x834)
                        continue
                }
                0x834 -> {
                        (v)!![14] = "支付宝随机立减"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![14])))) 0x85c else 0x83c)
                        continue
                }
                0x83c -> {
                        (v)!![14] = "碰一下立减"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![14])))) 0x85c else 0x844)
                        continue
                }
                0x844 -> {
                        (v)!![14] = "碰一下共减"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![14])))) 0x85c else 0x84c)
                        continue
                }
                0x84c -> {
                        (v)!![14] = "视频红包"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![14])))) 0x85c else 0x854)
                        continue
                }
                0x854 -> {
                        (v)!![14] = "优惠"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x8a6 else 0x85c)
                        continue
                }
                0x85c -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![14] = dexN((dexI((v)!![14]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x8a6 else 0x866)
                        continue
                }
                0x866 -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x884 else 0x874)
                        continue
                }
                0x874 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        (v)!![15] = dexN(2L)
                        (v)!![14] = dexN((dexI((v)!![14]) - dexI((v)!![15])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x884 else 0x87c)
                        continue
                }
                0x87c -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (2)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        pc = 0x884
                        continue
                }
                0x884 -> {
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x8fe else 0x8a2)
                        continue
                }
                0x8a2 -> {
                        (((dexR((v)!![12]) as RuleBill?)))!!.setDiscount(((dexR((v)!![1]) as String?)))
                        pc = 0x8fe
                        continue
                }
                0x8a6 -> {
                        (v)!![14] = dexN((((dexR((v)!![0]) as RuleBase?)))!!.a)
                        pc = (if ((dexEqual((v)!![14], (v)!![15]))) 0x8fe else 0x8aa)
                        continue
                }
                0x8aa -> {
                        (v)!![14] = "订单金额"
                        (result)!![0] = dexN((if (((((dexR((v)!![8]) as String?)))!!.equals(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((dexZero((v)!![8]))) 0x8fe else 0x8b2)
                        continue
                }
                0x8b2 -> {
                        (result)!![0] = dexN((((dexR((v)!![23]) as MutableList<String?>?)))!!.size)
                        (v)!![8] = (result)!![0]
                        (v)!![14] = dexN(1L)
                        (v)!![8] = dexN((dexI((v)!![8]) - dexI((v)!![14])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![8])))) 0x8fe else 0x8ba)
                        continue
                }
                0x8ba -> {
                        (v)!![8] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![2]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (v)!![8] = ((dexR((v)!![8]) as String?))
                        (result)!![0] = (((dexR((v)!![8]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![10]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![9]) as CharSequence?)))!!, (((dexR((v)!![1]) as CharSequence?)))!!)
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![1]) as String?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x8fe else 0x8e0)
                        continue
                }
                0x8e0 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![3]) as CharSequence?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x8fe else 0x8ea)
                        continue
                }
                0x8ea -> {
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![1]) as String?)))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![1]) as String?)))))
                        (v)!![5] = (result)!![0]
                        (v)!![3] = dexN(java.lang.Double.doubleToRawLongBits((dexD((v)!![3]) - dexD((v)!![5]))))
                        (result)!![0] = Amounts.t(dexD((v)!![3]))
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setDiscount(((dexR((v)!![1]) as String?)))
                        pc = 0x8fe
                        continue
                }
                0x8fe -> {
                        (v)!![7] = dexN((dexI((v)!![7]) + (1)))
                        (v)!![3] = (v)!![12]
                        (v)!![1] = (v)!![13]
                        (v)!![5] = (v)!![17]
                        (v)!![4] = dexN(0L)
                        pc = 0x37
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_s_block7(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x907 -> {
                        (v)!![12] = (v)!![3]
                        (v)!![17] = (v)!![5]
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x93d else 0x914)
                        continue
                }
                0x914 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x93d else 0x91e)
                        continue
                }
                0x91e -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as RuleBill?)))!!.isIncome())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x931 else 0x924)
                        continue
                }
                0x924 -> {
                        (v)!![1] = "支付宝收款"
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x93d
                        continue
                }
                0x931 -> {
                        (v)!![1] = "支付宝付款"
                        (((dexR((v)!![12]) as RuleBill?)))!!.setRemark(((dexR((v)!![1]) as String?)))
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setShopName(((dexR((v)!![1]) as String?)))
                        pc = 0x93d
                        continue
                }
                0x93d -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![12]) as RuleBill?)))!!.isIncome())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x953 else 0x943)
                        continue
                }
                0x943 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x953 else 0x94d)
                        continue
                }
                0x94d -> {
                        (v)!![1] = (v)!![17]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![1]) as String?)))
                        pc = 0x955
                        continue
                }
                0x953 -> {
                        (v)!![1] = (v)!![17]
                        pc = 0x955
                        continue
                }
                0x955 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![2] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![2]) as CharSequence?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![2])))) 0x96a else 0x95f)
                        continue
                }
                0x95f -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![2] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![2]) as String?)))!!.trim()
                        (v)!![2] = (result)!![0]
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![2]) as String?)))
                        pc = 0x96a
                        continue
                }
                0x96a -> {
                        (v)!![2] = "账户余额"
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![2]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![2])))) 0x98e else 0x976)
                        continue
                }
                0x976 -> {
                        (v)!![2] = "余额"
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![2]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![2])))) 0x98e else 0x982)
                        continue
                }
                0x982 -> {
                        (v)!![2] = "可用余额"
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getAsset()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![2]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((dexZero((v)!![2]))) 0x991 else 0x98e)
                        continue
                }
                0x98e -> {
                        (((dexR((v)!![12]) as RuleBill?)))!!.setAsset(((dexR((v)!![1]) as String?)))
                        pc = 0x991
                        continue
                }
                0x991 -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getRemark()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x9a6 else 0x99b)
                        continue
                }
                0x99b -> {
                        (result)!![0] = (((dexR((v)!![12]) as RuleBill?)))!!.getNumber()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![1]) as CharSequence?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x9a6 else 0x9a5)
                        continue
                }
                0x9a5 -> {
                        (result)!![1] = (v)!![12]
                        return -(1)
                }
                0x9a6 -> {
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

    fun t(list: MutableList<String?>?): RuleBill? {
        if (((list)!!.size == 0)) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setTransfer(true)
        (billInfo)!!.setOrigin("支付宝")
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((str)!!.equals("实付金额") && (i6 < ((list)!!.size - 2)))) {
                        var strReplace: String? = (((((list)!![(i6 + 2)])!!.replace(("¥")!!, ("")!!))!!.replace((",")!!, ("")!!))!!.replace(("支出")!!, ("")!!))!!.replace(("元")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(((Math.abs(Amounts.S(strReplace))).toString() + ""))
                        }
                    } else if (((str)!!.equals("还款信用卡") && (i6 < ((list)!!.size - 1)))) {
                        var str2: String? = (list)!![(i6 + 1)]
                        (billInfo)!!.setToAsset((if ((((str2)!!.contains(("(")!!) && (str2)!!.contains((")")!!)))) ((str2)!!.substring(((str2)!!.indexOf(("(")!!) + 1), (str2)!!.lastIndexOf((")")!!)))!!.replace((".")!!, ("")!!) else (str2)!!.replace((".")!!, ("")!!)))
                        (billInfo)!!.setFromAsset("支付宝")
                    }
                i6++
            }
        }
        if (!(TextUtils.isEmpty((billInfo)!!.getFromAsset()))) {
            (billInfo)!!.setFromAsset(((billInfo)!!.getFromAsset())!!.trim())
        }
        if (!(TextUtils.isEmpty((billInfo)!!.getToAsset()))) {
            (billInfo)!!.setToAsset(((billInfo)!!.getToAsset())!!.trim())
        }
        if ((("账户余额")!!.equals((billInfo)!!.getFromAsset()) || ("余额")!!.equals((billInfo)!!.getFromAsset()))) {
            (billInfo)!!.setFromAsset("支付宝")
        }
        if ((("账户余额")!!.equals((billInfo)!!.getToAsset()) || ("余额")!!.equals((billInfo)!!.getToAsset()))) {
            (billInfo)!!.setToAsset("支付宝")
        }
        if (TextUtils.isEmpty((billInfo)!!.getNumber())) {
            return null
        }
        return billInfo
    }

    fun u(arg0: MutableList<String?>?): RuleBill? {
        var v: Array<Any?>? = arrayOfNulls<Any>(25)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![23] = this
        (v)!![24] = arg0
        while (true) {
            if ((pc == -(1))) {
                return ((dexR((result)!![1]) as RuleBill?))
            }
            if ((pc < 0x11f)) pc = recovered_u_block0(v, result, pc) else if ((pc < 0x261)) pc = recovered_u_block1(v, result, pc) else if ((pc < 0x3cb)) pc = recovered_u_block2(v, result, pc) else pc = recovered_u_block3(v, result, pc)
        }
    }

    private fun recovered_u_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (v)!![0] = (v)!![23]
                        (v)!![1] = (v)!![24]
                        (result)!![0] = dexN((if (((((dexR((v)!![24]) as MutableList<String?>?)))!!.isEmpty())) 1 else 0))
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
                        (v)!![4] = dexN(1L)
                        (((dexR((v)!![2]) as RuleBill?)))!!.setTransfer(dexZ((v)!![4]))
                        (v)!![5] = "支付宝"
                        (((dexR((v)!![2]) as RuleBill?)))!!.setOrigin(((dexR((v)!![5]) as String?)))
                        (v)!![6] = "充值成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![6]) as String?)), dexZ((v)!![4])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        (v)!![8] = "转出成功"
                        (v)!![9] = "转入成功"
                        (v)!![10] = "提现说明"
                        (v)!![11] = "提现金额"
                        (v)!![12] = "充值说明"
                        (v)!![13] = dexN(0L)
                        pc = (if ((!(dexZero((v)!![7])))) 0x9c else 0x2d)
                        continue
                }
                0x2d -> {
                        (v)!![7] = "余额转入"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![7]) as String?)), dexZ((v)!![4])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![7])))) 0x9c else 0x35)
                        continue
                }
                0x35 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![12]) as String?)), dexZ((v)!![4])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x3c else 0x3b)
                        continue
                }
                0x3b -> {
                        pc = 0x9c
                        continue
                }
                0x3c -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![11]) as String?)), dexZ((v)!![4])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![7])))) 0x98 else 0x42)
                        continue
                }
                0x42 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![10]) as String?)), dexZ((v)!![4])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![7])))) 0x98 else 0x48)
                        continue
                }
                0x48 -> {
                        (v)!![7] = "提现成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![7]) as String?)), dexZ((v)!![4])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x51 else 0x50)
                        continue
                }
                0x50 -> {
                        pc = 0x98
                        continue
                }
                0x51 -> {
                        (v)!![7] = "网商银行转账"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![7]) as String?)), dexZ((v)!![4])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x5f else 0x59)
                        continue
                }
                0x59 -> {
                        (v)!![7] = "网商银行余利宝"
                        (((dexR((v)!![2]) as RuleBill?)))!!.setToAsset(((dexR((v)!![7]) as String?)))
                        pc = 0x9f
                        continue
                }
                0x5f -> {
                        (v)!![7] = "余额宝-单次转入"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![7]) as String?)), dexZ((v)!![4])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        (v)!![14] = "余额宝"
                        pc = (if ((dexZero((v)!![7]))) 0x6d else 0x69)
                        continue
                }
                0x69 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setToAsset(((dexR((v)!![14]) as String?)))
                        pc = 0x9f
                        continue
                }
                0x6d -> {
                        (v)!![7] = "余额宝-转出到余额"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![7]) as String?)), dexZ((v)!![4])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x7c else 0x75)
                        continue
                }
                0x75 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setFromAsset(((dexR((v)!![14]) as String?)))
                        (((dexR((v)!![2]) as RuleBill?)))!!.setToAsset(((dexR((v)!![5]) as String?)))
                        pc = 0x9f
                        continue
                }
                0x7c -> {
                        (v)!![7] = "开始计算收益"
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![7]) as String?)), dexZ((v)!![4])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x8e else 0x84)
                        continue
                }
                0x84 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![9]) as String?)), dexZ((v)!![13])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x8e else 0x8a)
                        continue
                }
                0x8a -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setToAsset(((dexR((v)!![14]) as String?)))
                        pc = 0x9f
                        continue
                }
                0x8e -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.a(((dexR((v)!![1]) as MutableList<String?>?)), ((dexR((v)!![8]) as String?)), dexZ((v)!![4])))) 1 else 0))
                        (v)!![7] = (result)!![0]
                        pc = (if ((dexZero((v)!![7]))) 0x9f else 0x94)
                        continue
                }
                0x94 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setFromAsset(((dexR((v)!![14]) as String?)))
                        pc = 0x9f
                        continue
                }
                0x98 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setFromAsset(((dexR((v)!![5]) as String?)))
                        pc = 0x9f
                        continue
                }
                0x9c -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setToAsset(((dexR((v)!![5]) as String?)))
                        pc = 0x9f
                        continue
                }
                0x9f -> {
                        (v)!![7] = (v)!![13]
                        pc = 0xa0
                        continue
                }
                0xa0 -> {
                        (result)!![0] = dexN((((dexR((v)!![24]) as MutableList<String?>?)))!!.size)
                        (v)!![14] = (result)!![0]
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![14])))) 0x46d else 0xa6)
                        continue
                }
                0xa6 -> {
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![7])]
                        (v)!![14] = (result)!![0]
                        (v)!![14] = ((dexR((v)!![14]) as String?))
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        (v)!![3] = "支出"
                        (v)!![13] = "元"
                        (v)!![4] = "¥"
                        (v)!![0] = "0"
                        (v)!![17] = (v)!![11]
                        (v)!![11] = "NaN"
                        (v)!![18] = (v)!![5]
                        (v)!![5] = ","
                        (v)!![19] = (v)!![12]
                        (v)!![12] = ""
                        pc = (if ((!(dexZero((v)!![15])))) 0xd2 else 0xc6)
                        continue
                }
                0xc6 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![15])))) 0xd2 else 0xcc)
                        continue
                }
                0xcc -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![9]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((dexZero((v)!![15]))) 0x130 else 0xd2)
                        continue
                }
                0xd2 -> {
                        (result)!![0] = dexN((((dexR((v)!![24]) as MutableList<String?>?)))!!.size)
                        (v)!![15] = (result)!![0]
                        (v)!![16] = dexN(1L)
                        (v)!![15] = dexN((dexI((v)!![15]) + (-(1))))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![15])))) 0x130 else 0xdc)
                        continue
                }
                0xdc -> {
                        (v)!![14] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![14])]
                        (v)!![14] = (result)!![0]
                        (v)!![14] = ((dexR((v)!![14]) as String?))
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getNumber()
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x11f else 0xfe)
                        continue
                }
                0xfe -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x11f else 0x104)
                        continue
                }
                0x104 -> {
                        (v)!![0] = DexReference()
                        ((((v)!![0] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![3]))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(dexD((v)!![3]))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(((dexR((v)!![12]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.toString()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x128
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
                0x11f -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x128 else 0x125)
                        continue
                }
                0x125 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x128
                        continue
                }
                0x128 -> {
                        (v)!![20] = (v)!![6]
                        pc = 0x12a
                        continue
                }
                0x12a -> {
                        (v)!![0] = (v)!![19]
                        pc = 0x12c
                        continue
                }
                0x12c -> {
                        (v)!![4] = dexN(0L)
                        pc = 0x12d
                        continue
                }
                0x12d -> {
                        (v)!![5] = dexN(1L)
                        pc = 0x45d
                        continue
                }
                0x130 -> {
                        (v)!![15] = "提现金额¥"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![15]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![20] = (result)!![0]
                        pc = (if ((dexZero((v)!![20]))) 0x17d else 0x138)
                        continue
                }
                0x138 -> {
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.replace((((dexR((v)!![15]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getNumber()
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x173 else 0x152)
                        continue
                }
                0x152 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x173 else 0x158)
                        continue
                }
                0x158 -> {
                        (v)!![0] = DexReference()
                        ((((v)!![0] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![3]))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(dexD((v)!![3]))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(((dexR((v)!![12]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.toString()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x128
                        continue
                }
                0x173 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x128 else 0x179)
                        continue
                }
                0x179 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x128
                        continue
                }
                0x17d -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![8]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        (v)!![20] = (v)!![6]
                        (v)!![6] = dexN(2L)
                        pc = (if ((dexZero((v)!![15]))) 0x1dc else 0x186)
                        continue
                }
                0x186 -> {
                        (result)!![0] = dexN((((dexR((v)!![24]) as MutableList<String?>?)))!!.size)
                        (v)!![15] = (result)!![0]
                        (v)!![15] = dexN((dexI((v)!![15]) - dexI((v)!![6])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![15])))) 0x1dc else 0x18d)
                        continue
                }
                0x18d -> {
                        (v)!![15] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![15])]
                        (v)!![15] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![4]) as String?)))!!.equals(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![15] = (result)!![0]
                        pc = (if ((dexZero((v)!![15]))) 0x1dc else 0x199)
                        continue
                }
                0x199 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (2)))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getNumber()
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1d1 else 0x1af)
                        continue
                }
                0x1af -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x1d1 else 0x1b5)
                        continue
                }
                0x1b5 -> {
                        (v)!![0] = DexReference()
                        ((((v)!![0] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![3]))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(dexD((v)!![3]))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(((dexR((v)!![12]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.toString()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x12a
                        continue
                }
                0x1d1 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x12a else 0x1d7)
                        continue
                }
                0x1d7 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x12a
                        continue
                }
                0x1dc -> {
                        (v)!![15] = "成功转出"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.startsWith((((dexR((v)!![15]) as String?)))!!))) 1 else 0))
                        (v)!![21] = (result)!![0]
                        pc = (if ((dexZero((v)!![21]))) 0x251 else 0x1e4)
                        continue
                }
                0x1e4 -> {
                        (v)!![6] = "元至"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![6]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![22] = (result)!![0]
                        pc = (if ((dexZero((v)!![22]))) 0x251 else 0x1ec)
                        continue
                }
                0x1ec -> {
                        (result)!![0] = dexN((((dexR((v)!![14]) as String?)))!!.indexOf((((dexR((v)!![15]) as String?)))!!))
                        (v)!![3] = (result)!![0]
                        (v)!![3] = dexN((dexI((v)!![3]) + (4)))
                        (result)!![0] = dexN((((dexR((v)!![14]) as String?)))!!.indexOf((((dexR((v)!![6]) as String?)))!!))
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.substring(dexI((v)!![3]), dexI((v)!![4]))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getNumber()
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x229 else 0x208)
                        continue
                }
                0x208 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x229 else 0x20e)
                        continue
                }
                0x20e -> {
                        (v)!![0] = DexReference()
                        ((((v)!![0] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![3]))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(dexD((v)!![3]))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(((dexR((v)!![12]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.toString()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x232
                        continue
                }
                0x229 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x232 else 0x22f)
                        continue
                }
                0x22f -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x232
                        continue
                }
                0x232 -> {
                        (result)!![0] = dexN((((dexR((v)!![14]) as String?)))!!.indexOf((((dexR((v)!![6]) as String?)))!!))
                        (v)!![0] = (result)!![0]
                        (v)!![3] = dexN(1L)
                        (v)!![0] = dexN((dexI((v)!![0]) + dexI((v)!![3])))
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.substring(dexI((v)!![0]))
                        (v)!![0] = (result)!![0]
                        (v)!![3] = "。"
                        (result)!![0] = (((dexR((v)!![0]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![0] = (result)!![0]
                        (v)!![3] = "支付宝账户余额"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x24c else 0x24a)
                        continue
                }
                0x24a -> {
                        (v)!![0] = (v)!![18]
                        pc = 0x24c
                        continue
                }
                0x24c -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setToAsset(((dexR((v)!![0]) as String?)))
                        pc = 0x12a
                        continue
                }
                0x251 -> {
                        (v)!![6] = "交易成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![6])))) 0x261 else 0x259)
                        continue
                }
                0x259 -> {
                        (v)!![6] = "还款成功"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![6] = (result)!![0]
                        pc = (if ((dexZero((v)!![6]))) 0x2b2 else 0x261)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_u_block2(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x261 -> {
                        (v)!![6] = dexN((dexI((v)!![7]) + (-(1))))
                        pc = (if (((dexI((v)!![6]) < 0))) 0x2b2 else 0x265)
                        continue
                }
                0x265 -> {
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![6])]
                        (v)!![6] = (result)!![0]
                        (v)!![6] = ((dexR((v)!![6]) as String?))
                        (result)!![0] = (((dexR((v)!![6]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![4] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![4]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getNumber()
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x2a7 else 0x285)
                        continue
                }
                0x285 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x2a7 else 0x28b)
                        continue
                }
                0x28b -> {
                        (v)!![0] = DexReference()
                        ((((v)!![0] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![3]))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(dexD((v)!![3]))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(((dexR((v)!![12]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.toString()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x12a
                        continue
                }
                0x2a7 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x12a else 0x2ad)
                        continue
                }
                0x2ad -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x12a
                        continue
                }
                0x2b2 -> {
                        (v)!![3] = "成功转入"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.contains((((dexR((v)!![3]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x2ff else 0x2ba)
                        continue
                }
                0x2ba -> {
                        (v)!![3] = "成功转入"
                        (result)!![0] = (((dexR((v)!![14]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.replace((((dexR((v)!![13]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getNumber()
                        (v)!![4] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![4]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x2f4 else 0x2d2)
                        continue
                }
                0x2d2 -> {
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![3]) as String?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x2f4 else 0x2d8)
                        continue
                }
                0x2d8 -> {
                        (v)!![0] = DexReference()
                        ((((v)!![0] as DexReference?)))!!.value = StringBuilder()
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![3]) as String?)))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![3]))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(dexD((v)!![3]))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.append(((dexR((v)!![12]) as String?)))
                        (result)!![0] = (((dexR((v)!![0]) as StringBuilder?)))!!.toString()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x12a
                        continue
                }
                0x2f4 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.equals(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x12a else 0x2fa)
                        continue
                }
                0x2fa -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setNumber(((dexR((v)!![0]) as String?)))
                        pc = 0x12a
                        continue
                }
                0x2ff -> {
                        (v)!![0] = "到账银行卡"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x31f else 0x307)
                        continue
                }
                0x307 -> {
                        (v)!![0] = "提现到"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x31f else 0x30f)
                        continue
                }
                0x30f -> {
                        (v)!![0] = "还款到"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x31f else 0x317)
                        continue
                }
                0x317 -> {
                        (v)!![0] = "到账账户"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((dexZero((v)!![0]))) 0x334 else 0x31f)
                        continue
                }
                0x31f -> {
                        (result)!![0] = dexN((((dexR((v)!![24]) as MutableList<String?>?)))!!.size)
                        (v)!![0] = (result)!![0]
                        (v)!![3] = dexN(1L)
                        (v)!![0] = dexN((dexI((v)!![0]) - dexI((v)!![3])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![0])))) 0x334 else 0x327)
                        continue
                }
                0x327 -> {
                        (v)!![0] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![0])]
                        (v)!![0] = (result)!![0]
                        (v)!![0] = ((dexR((v)!![0]) as String?))
                        (((dexR((v)!![2]) as RuleBill?)))!!.setToAsset(((dexR((v)!![0]) as String?)))
                        pc = 0x12a
                        continue
                }
                0x334 -> {
                        (v)!![0] = "付款方式"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x344 else 0x33c)
                        continue
                }
                0x33c -> {
                        (v)!![0] = "对方账户"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((dexZero((v)!![0]))) 0x359 else 0x344)
                        continue
                }
                0x344 -> {
                        (result)!![0] = dexN((((dexR((v)!![24]) as MutableList<String?>?)))!!.size)
                        (v)!![0] = (result)!![0]
                        (v)!![3] = dexN(1L)
                        (v)!![0] = dexN((dexI((v)!![0]) - dexI((v)!![3])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![0])))) 0x359 else 0x34c)
                        continue
                }
                0x34c -> {
                        (v)!![0] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![0])]
                        (v)!![0] = (result)!![0]
                        (v)!![0] = ((dexR((v)!![0]) as String?))
                        (((dexR((v)!![2]) as RuleBill?)))!!.setFromAsset(((dexR((v)!![0]) as String?)))
                        pc = 0x12a
                        continue
                }
                0x359 -> {
                        (v)!![0] = "服务费"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((dexZero((v)!![0]))) 0x38c else 0x361)
                        continue
                }
                0x361 -> {
                        (result)!![0] = dexN((((dexR((v)!![24]) as MutableList<String?>?)))!!.size)
                        (v)!![0] = (result)!![0]
                        (v)!![3] = dexN(1L)
                        (v)!![0] = dexN((dexI((v)!![0]) - dexI((v)!![3])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![0])))) 0x38c else 0x369)
                        continue
                }
                0x369 -> {
                        (v)!![0] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![0])]
                        (v)!![0] = (result)!![0]
                        (v)!![0] = ((dexR((v)!![0]) as String?))
                        (result)!![0] = (((dexR((v)!![0]) as String?)))!!.replace((((dexR((v)!![4]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![0] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as String?)))!!.replace((((dexR((v)!![5]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![0] = (result)!![0]
                        (result)!![0] = dexN((if ((Amounts.J(((dexR((v)!![0]) as String?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x12a else 0x37f)
                        continue
                }
                0x37f -> {
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Amounts.S(((dexR((v)!![0]) as String?)))))
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN(java.lang.Double.doubleToRawLongBits(Math.abs(dexD((v)!![3]))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setServiceCharge(dexD((v)!![3]))
                        pc = 0x12a
                        continue
                }
                0x38c -> {
                        (v)!![0] = "创建时间"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((dexZero((v)!![0]))) 0x3b5 else 0x394)
                        continue
                }
                0x394 -> {
                        (result)!![0] = dexN((((dexR((v)!![24]) as MutableList<String?>?)))!!.size)
                        (v)!![0] = (result)!![0]
                        (v)!![3] = dexN(1L)
                        (v)!![0] = dexN((dexI((v)!![0]) - dexI((v)!![3])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![0])))) 0x3b5 else 0x39c)
                        continue
                }
                0x39c -> {
                        (v)!![0] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![0])]
                        (v)!![0] = (result)!![0]
                        (v)!![0] = ((dexR((v)!![0]) as String?))
                        (v)!![3] = "创建时间"
                        (result)!![0] = (((dexR((v)!![0]) as String?)))!!.replace((((dexR((v)!![3]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![0] = (result)!![0]
                        (v)!![3] = "yyyy-MM-dd HH:mm:ss"
                        (result)!![0] = dexN(Dates.X0(((dexR((v)!![0]) as String?)), ((dexR((v)!![3]) as String?))))
                        (v)!![3] = (result)!![0]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setTime(dexL((v)!![3]))
                        pc = 0x12a
                        continue
                }
                0x3b5 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![10]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x3d4 else 0x3bb)
                        continue
                }
                0x3bb -> {
                        (v)!![0] = "转账备注"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x3d4 else 0x3c3)
                        continue
                }
                0x3c3 -> {
                        (v)!![0] = (v)!![19]
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![0]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x3d6 else 0x3cb)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_u_block3(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x3cb -> {
                        (v)!![3] = "商品说明"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x3eb else 0x3d3)
                        continue
                }
                0x3d3 -> {
                        pc = 0x3d6
                        continue
                }
                0x3d4 -> {
                        (v)!![0] = (v)!![19]
                        pc = 0x3d6
                        continue
                }
                0x3d6 -> {
                        (result)!![0] = dexN((((dexR((v)!![24]) as MutableList<String?>?)))!!.size)
                        (v)!![3] = (result)!![0]
                        (v)!![4] = dexN(1L)
                        (v)!![3] = dexN((dexI((v)!![3]) - dexI((v)!![4])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![3])))) 0x3eb else 0x3de)
                        continue
                }
                0x3de -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (((dexR((v)!![2]) as RuleBill?)))!!.setRemark(((dexR((v)!![3]) as String?)))
                        pc = 0x12c
                        continue
                }
                0x3eb -> {
                        (v)!![3] = "转出说明"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x441 else 0x3f3)
                        continue
                }
                0x3f3 -> {
                        (result)!![0] = dexN((((dexR((v)!![24]) as MutableList<String?>?)))!!.size)
                        (v)!![3] = (result)!![0]
                        (v)!![4] = dexN(1L)
                        (v)!![3] = dexN((dexI((v)!![3]) - dexI((v)!![4])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![3])))) 0x441 else 0x3fb)
                        continue
                }
                0x3fb -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (v)!![4] = "-"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x415 else 0x40b)
                        continue
                }
                0x40b -> {
                        (v)!![4] = "-"
                        (result)!![0] = dexN((((dexR((v)!![3]) as String?)))!!.indexOf((((dexR((v)!![4]) as String?)))!!))
                        (v)!![4] = (result)!![0]
                        (v)!![5] = dexN(0L)
                        (result)!![0] = (((dexR((v)!![3]) as String?)))!!.substring(dexI((v)!![5]), dexI((v)!![4]))
                        pc = 0x415
                        continue
                }
                0x415 -> {
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![3]) as CharSequence?))))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![4])))) 0x12c else 0x41b)
                        continue
                }
                0x41b -> {
                        (v)!![4] = "转出到"
                        (result)!![0] = dexN((if (((((dexR((v)!![3]) as String?)))!!.contains((((dexR((v)!![4]) as CharSequence?)))!!))) 1 else 0))
                        (v)!![4] = (result)!![0]
                        pc = (if ((dexZero((v)!![4]))) 0x12c else 0x423)
                        continue
                }
                0x423 -> {
                        (v)!![4] = "转出到"
                        (result)!![0] = javaSplit((((dexR((v)!![3]) as String?)))!!, (((dexR((v)!![4]) as String?)))!!)
                        (v)!![3] = (result)!![0]
                        (v)!![4] = dexN(dexLength(dexR((v)!![3])))
                        (v)!![5] = dexN(2L)
                        pc = (if ((!(dexEqual((v)!![4], (v)!![5])))) 0x12c else 0x42d)
                        continue
                }
                0x42d -> {
                        (v)!![4] = dexN(0L)
                        (v)!![5] = (((dexR((v)!![3]) as Array<Any?>?)))!![dexI((v)!![4])]
                        (v)!![6] = "转出说明"
                        (result)!![0] = (((dexR((v)!![5]) as String?)))!!.replace((((dexR((v)!![6]) as CharSequence?)))!!, (((dexR((v)!![12]) as CharSequence?)))!!)
                        (v)!![5] = (result)!![0]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setFromAsset(((dexR((v)!![5]) as String?)))
                        (v)!![5] = dexN(1L)
                        (v)!![3] = (((dexR((v)!![3]) as Array<Any?>?)))!![dexI((v)!![5])]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setToAsset(((dexR((v)!![3]) as String?)))
                        pc = 0x12d
                        continue
                }
                0x441 -> {
                        (v)!![4] = dexN(0L)
                        (v)!![3] = "转入账户"
                        (result)!![0] = dexN((if (((((dexR((v)!![14]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x12d else 0x44a)
                        continue
                }
                0x44a -> {
                        (result)!![0] = dexN((((dexR((v)!![24]) as MutableList<String?>?)))!!.size)
                        (v)!![3] = (result)!![0]
                        (v)!![5] = dexN(1L)
                        (v)!![3] = dexN((dexI((v)!![3]) - dexI((v)!![5])))
                        pc = (if (((dexI((v)!![7]) >= dexI((v)!![3])))) 0x45d else 0x452)
                        continue
                }
                0x452 -> {
                        (v)!![3] = dexN((dexI((v)!![7]) + (1)))
                        (result)!![0] = (((dexR((v)!![1]) as MutableList<String?>?)))!![dexI((v)!![3])]
                        (v)!![3] = (result)!![0]
                        (v)!![3] = ((dexR((v)!![3]) as String?))
                        (((dexR((v)!![2]) as RuleBill?)))!!.setToAsset(((dexR((v)!![3]) as String?)))
                        pc = 0x45d
                        continue
                }
                0x45d -> {
                        (v)!![7] = dexN((dexI((v)!![7]) + (1)))
                        (v)!![12] = (v)!![0]
                        (v)!![13] = (v)!![4]
                        (v)!![4] = (v)!![5]
                        (v)!![11] = (v)!![17]
                        (v)!![5] = (v)!![18]
                        (v)!![6] = (v)!![20]
                        (v)!![3] = dexN(0L)
                        (v)!![0] = (v)!![23]
                        pc = 0xa0
                        continue
                }
                0x46d -> {
                        (v)!![18] = (v)!![5]
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getFromAsset()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![0]) as CharSequence?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x484 else 0x479)
                        continue
                }
                0x479 -> {
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getFromAsset()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as String?)))!!.trim()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setFromAsset(((dexR((v)!![0]) as String?)))
                        pc = 0x484
                        continue
                }
                0x484 -> {
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getToAsset()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![0]) as CharSequence?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x499 else 0x48e)
                        continue
                }
                0x48e -> {
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getToAsset()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = (((dexR((v)!![0]) as String?)))!!.trim()
                        (v)!![0] = (result)!![0]
                        (((dexR((v)!![2]) as RuleBill?)))!!.setToAsset(((dexR((v)!![0]) as String?)))
                        pc = 0x499
                        continue
                }
                0x499 -> {
                        (v)!![0] = "账户余额"
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getFromAsset()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x4b1 else 0x4a5)
                        continue
                }
                0x4a5 -> {
                        (v)!![0] = "余额"
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getFromAsset()
                        (v)!![1] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((dexZero((v)!![0]))) 0x4b4 else 0x4b1)
                        continue
                }
                0x4b1 -> {
                        (v)!![0] = (v)!![18]
                        pc = 0x4b7
                        continue
                }
                0x4b4 -> {
                        (v)!![0] = (v)!![18]
                        pc = 0x4ba
                        continue
                }
                0x4b7 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setFromAsset(((dexR((v)!![0]) as String?)))
                        pc = 0x4ba
                        continue
                }
                0x4ba -> {
                        (v)!![1] = "账户余额"
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getToAsset()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x4d2 else 0x4c6)
                        continue
                }
                0x4c6 -> {
                        (v)!![1] = "余额"
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getToAsset()
                        (v)!![3] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x4d5 else 0x4d2)
                        continue
                }
                0x4d2 -> {
                        (((dexR((v)!![2]) as RuleBill?)))!!.setToAsset(((dexR((v)!![0]) as String?)))
                        pc = 0x4d5
                        continue
                }
                0x4d5 -> {
                        (result)!![0] = (((dexR((v)!![2]) as RuleBill?)))!!.getNumber()
                        (v)!![0] = (result)!![0]
                        (result)!![0] = dexN((if ((TextUtils.isEmpty(((dexR((v)!![0]) as CharSequence?))))) 1 else 0))
                        (v)!![0] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![0])))) 0x4e0 else 0x4df)
                        continue
                }
                0x4df -> {
                        (result)!![1] = (v)!![2]
                        return -(1)
                }
                0x4e0 -> {
                        (v)!![0] = dexN(0L)
                        (result)!![1] = (v)!![0]
                        return -(1)
                }
                else -> {
                    return pc
                }
            }
        }
    }

    fun v(list: MutableList<String?>?): RuleBill? {
        if ((list)!!.isEmpty()) {
            return null
        }
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("支付宝")
        if (RuleSettings.i()) {
            if (Dates.m0(this.f13678e, System.currentTimeMillis())) {
                (billInfo)!!.setTime(this.f13678e)
            } else {
                this.f13678e = System.currentTimeMillis()
            }
        }
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((str)!!.equals("收款方") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setRemark(("转账给" + (list)!![(i6 + 1)]))
                        (billInfo)!!.setShopName((billInfo)!!.getRemark())
                    } else if (((str)!!.equals("转账成功") && (i6 < ((list)!!.size - 2)))) {
                        var strReplace: String? = (((((list)!![(i6 + 1)])!!.replace(("￥")!!, ("")!!))!!.replace(("¥")!!, ("")!!))!!.replace(("元")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (!(Amounts.J(strReplace))) {
                            strReplace = (((((list)!![(i6 + 2)])!!.replace(("￥")!!, ("")!!))!!.replace(("¥")!!, ("")!!))!!.replace(("元")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        }
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                        } else if ((strReplace)!!.equals("NaN")) {
                            (billInfo)!!.setNumber("0")
                        }
                    } else if (((str)!!.equals("付款方式") && (i6 < ((list)!!.size - 1)))) {
                        (billInfo)!!.setAsset((list)!![(i6 + 1)])
                    }
                i6++
            }
        }
        if (((!(TextUtils.isEmpty((billInfo)!!.getRemark())) && !(TextUtils.isEmpty((billInfo)!!.getAsset()))) && TextUtils.isEmpty((billInfo)!!.getRemark()))) {
            (billInfo)!!.setRemark("支付宝转账")
            (billInfo)!!.setShopName((billInfo)!!.getRemark())
        }
        if (!(TextUtils.isEmpty((billInfo)!!.getAsset()))) {
            (billInfo)!!.setAsset(((billInfo)!!.getAsset())!!.trim())
        }
        if ((("账户余额")!!.equals((billInfo)!!.getAsset()) || ("余额")!!.equals((billInfo)!!.getAsset()))) {
            (billInfo)!!.setAsset("支付宝")
        }
        if ((TextUtils.isEmpty((billInfo)!!.getRemark()) || TextUtils.isEmpty((billInfo)!!.getNumber()))) {
            return null
        }
        return billInfo
    }

    fun w(arg0: RuleContext?, arg1: String?, arg2: CaptureNode?): Unit {
        var v: Array<Any?>? = arrayOfNulls<Any>(21)
        var result: Array<Any?>? = arrayOfNulls<Any>(2)
        var pc: Int = 0
        (v)!![17] = this
        (v)!![18] = arg0
        (v)!![19] = arg1
        (v)!![20] = arg2
        while (true) {
            if ((pc == -(1))) {
                return
            }
            if ((pc < 0xce)) pc = recovered_w_block0(v, result, pc) else if ((pc < 0x193)) pc = recovered_w_block1(v, result, pc) else if ((pc < 0x266)) pc = recovered_w_block2(v, result, pc) else if ((pc < 0x355)) pc = recovered_w_block3(v, result, pc) else pc = recovered_w_block4(v, result, pc)
        }
    }

    private fun recovered_w_block0(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x0 -> {
                        (v)!![0] = (v)!![17]
                        (v)!![1] = (v)!![19]
                        (v)!![2] = (v)!![20]
                        (v)!![3] = "com.eg.android.AlipayGphone.AlipayLogin"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        (v)!![4] = "使用密码"
                        (v)!![5] = "确认付款"
                        (v)!![6] = "向商家付款"
                        (v)!![7] = "优先使用此付款方式"
                        (v)!![8] = "确认即付款"
                        (v)!![9] = "碰一下，支付更便捷"
                        (v)!![10] = dexN(0L)
                        pc = (if ((dexZero((v)!![3]))) 0x25 else 0x1b)
                        continue
                }
                0x1b -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![10])
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![10])
                        (((dexR((v)!![0]) as AlipayRule?)))!!.f13679f = dexZ((v)!![10])
                        pc = 0x21
                        continue
                }
                0x21 -> {
                        (v)!![16] = (v)!![8]
                        pc = 0x37a
                        continue
                }
                0x25 -> {
                        (v)!![3] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        (v)!![11] = dexN(1L)
                        pc = (if ((!(dexZero((v)!![3])))) 0x38 else 0x2a)
                        continue
                }
                0x2a -> {
                        (v)!![3] = "com.alipay.android.msp.ui.views.MspContainerActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x33 else 0x32)
                        continue
                }
                0x32 -> {
                        pc = 0x38
                        continue
                }
                0x33 -> {
                        (v)!![16] = (v)!![8]
                        (v)!![1] = (v)!![11]
                        pc = 0x378
                        continue
                }
                0x38 -> {
                        (v)!![3] = "com.alipay.android.msp.ui.views.MspUniRenderActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x33 else 0x40)
                        continue
                }
                0x40 -> {
                        (v)!![3] = "com.alipay.android.phone.discovery.envelope.get.SnsCouponDetailActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x49 else 0x48)
                        continue
                }
                0x48 -> {
                        pc = 0x33
                        continue
                }
                0x49 -> {
                        (v)!![3] = "com.alipay.android.phone.nfcpay.ui.NfcPayActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x6d else 0x51)
                        continue
                }
                0x51 -> {
                        (v)!![3] = "com.alipay.android.phone.businesscommon.ucdp.nfc.activity.NResPageActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x5a else 0x59)
                        continue
                }
                0x59 -> {
                        pc = 0x6d
                        continue
                }
                0x5a -> {
                        (v)!![3] = "com.alipay.mobile.scan.as.main.MainCaptureActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![3])))) 0x6a else 0x62)
                        continue
                }
                0x62 -> {
                        (v)!![3] = "com.alipay.mobile.onsitepay.merge.OnsitepayActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![3]) as Any?))))) 1 else 0))
                        (v)!![3] = (result)!![0]
                        pc = (if ((dexZero((v)!![3]))) 0x6f else 0x6a)
                        continue
                }
                0x6a -> {
                        (((dexR((v)!![0]) as AlipayRule?)))!!.f13679f = dexZ((v)!![10])
                        pc = 0x6f
                        continue
                }
                0x6d -> {
                        (((dexR((v)!![0]) as AlipayRule?)))!!.f13679f = dexZ((v)!![11])
                        pc = 0x6f
                        continue
                }
                0x6f -> {
                        pc = (if ((dexZero((v)!![2]))) 0x21 else 0x71)
                        continue
                }
                0x71 -> {
                        (result)!![0] = (((dexR((v)!![0]) as RuleBase?)))!!.g(((dexR((v)!![2]) as CaptureNode?)))
                        (v)!![3] = (result)!![0]
                        (v)!![12] = DexReference()
                        ((((v)!![12] as DexReference?)))!!.value = HashMap<String?, Boolean>()
                        (result)!![0] = (((dexR((v)!![3]) as MutableList<String?>?)))!!.iterator()
                        (v)!![13] = (result)!![0]
                        pc = 0x7e
                        continue
                }
                0x7e -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![13]) as Iterator<String?>?)))!!.hasNext())) 1 else 0))
                        (v)!![14] = (result)!![0]
                        pc = (if ((dexZero((v)!![14]))) 0x90 else 0x84)
                        continue
                }
                0x84 -> {
                        (result)!![0] = (((dexR((v)!![13]) as Iterator<String?>?)))!!.next()
                        (v)!![14] = (result)!![0]
                        (v)!![14] = ((dexR((v)!![14]) as String?))
                        (v)!![15] = true
                        (result)!![0] = (((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!!.put((((dexR((v)!![14]) as Any?)) as String?), (((dexR((v)!![15]) as Any?)) as Boolean))
                        pc = 0x7e
                        continue
                }
                0x90 -> {
                        (v)!![13] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        pc = (if ((!(dexZero((v)!![13])))) 0xa2 else 0x94)
                        continue
                }
                0x94 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![9]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0xa2 else 0x9a)
                        continue
                }
                0x9a -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![8]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0xa2 else 0xa0)
                        continue
                }
                0xa0 -> {
                        (((dexR((v)!![0]) as AlipayRule?)))!!.f13679f = dexZ((v)!![11])
                        pc = 0xa2
                        continue
                }
                0xa2 -> {
                        (v)!![13] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        pc = (if ((!(dexZero((v)!![13])))) 0xb8 else 0xa6)
                        continue
                }
                0xa6 -> {
                        (v)!![13] = "com.alipay.mobile.quinox.SchemeLauncherActivity"
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0xb8 else 0xae)
                        continue
                }
                0xae -> {
                        (v)!![1] = "使用说明"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0xb8 else 0xb6)
                        continue
                }
                0xb6 -> {
                        (((dexR((v)!![0]) as AlipayRule?)))!!.f13679f = dexZ((v)!![11])
                        pc = 0xb8
                        continue
                }
                0xb8 -> {
                        (v)!![1] = "向商家付钱"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0xd5 else 0xc0)
                        continue
                }
                0xc0 -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        (v)!![13] = "交易方式"
                        (v)!![14] = "支付成功"
                        pc = (if ((!(dexZero((v)!![1])))) 0xda else 0xc8)
                        continue
                }
                0xc8 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0xda else 0xce)
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
                0xce -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0xd5 else 0xd4)
                        continue
                }
                0xd4 -> {
                        pc = 0xda
                        continue
                }
                0xd5 -> {
                        (v)!![16] = (v)!![8]
                        (v)!![1] = (v)!![11]
                        pc = 0x2fe
                        continue
                }
                0xda -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        (v)!![15] = "支付时间"
                        (v)!![10] = "付款方式"
                        (v)!![11] = "账单详情"
                        pc = (if ((!(dexZero((v)!![1])))) 0x120 else 0xe4)
                        continue
                }
                0xe4 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0xfa else 0xea)
                        continue
                }
                0xea -> {
                        (v)!![1] = "代付成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0xfa else 0xf2)
                        continue
                }
                0xf2 -> {
                        (v)!![1] = "转账成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x120 else 0xfa)
                        continue
                }
                0xfa -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![10]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x10e else 0x100)
                        continue
                }
                0x100 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x10e else 0x106)
                        continue
                }
                0x106 -> {
                        (v)!![1] = "完成"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x120 else 0x10e)
                        continue
                }
                0x10e -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x120 else 0x114)
                        continue
                }
                0x114 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x11b else 0x11a)
                        continue
                }
                0x11a -> {
                        pc = 0x120
                        continue
                }
                0x11b -> {
                        (v)!![16] = (v)!![8]
                        (v)!![1] = dexN(1L)
                        pc = 0x2fe
                        continue
                }
                0x120 -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x15a else 0x124)
                        continue
                }
                0x124 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x15a else 0x12a)
                        continue
                }
                0x12a -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x15a else 0x130)
                        continue
                }
                0x130 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x15a else 0x136)
                        continue
                }
                0x136 -> {
                        (v)!![1] = "计算中"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x15a else 0x13e)
                        continue
                }
                0x13e -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![10]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x14a else 0x144)
                        continue
                }
                0x144 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x15a else 0x14a)
                        continue
                }
                0x14a -> {
                        (v)!![1] = "¥"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x11b else 0x152)
                        continue
                }
                0x152 -> {
                        (v)!![1] = "￥"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x11b else 0x15a)
                        continue
                }
                0x15a -> {
                        (v)!![1] = "充值成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        (v)!![14] = "话费已到账"
                        pc = (if ((dexZero((v)!![13]))) 0x16b else 0x164)
                        continue
                }
                0x164 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((dexZero((v)!![13]))) 0x16b else 0x16a)
                        continue
                }
                0x16a -> {
                        pc = 0x11b
                        continue
                }
                0x16b -> {
                        (v)!![13] = "结果详情"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        (v)!![16] = (v)!![8]
                        pc = (if ((dexZero((v)!![13]))) 0x183 else 0x175)
                        continue
                }
                0x175 -> {
                        (v)!![13] = "提现金额"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![13] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![13])))) 0x17e else 0x17d)
                        continue
                }
                0x17d -> {
                        pc = 0x183
                        continue
                }
                0x17e -> {
                        (v)!![1] = dexN(1L)
                        (v)!![8] = dexN(9L)
                        pc = 0x2f9
                        continue
                }
                0x183 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        (v)!![13] = "手机充值"
                        (v)!![8] = "充值号码"
                        pc = (if ((dexZero((v)!![1]))) 0x19f else 0x18d)
                        continue
                }
                0x18d -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![8]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x19f else 0x193)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_w_block2(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x193 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x19f else 0x199)
                        continue
                }
                0x199 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x17e else 0x19f)
                        continue
                }
                0x19f -> {
                        (v)!![1] = "提现成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x17e else 0x1a7)
                        continue
                }
                0x1a7 -> {
                        (v)!![1] = "转入成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x17e else 0x1af)
                        continue
                }
                0x1af -> {
                        (v)!![1] = "转出成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x17e else 0x1b7)
                        continue
                }
                0x1b7 -> {
                        (v)!![1] = "创建时间"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x1da else 0x1bf)
                        continue
                }
                0x1bf -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![8]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x1da else 0x1c5)
                        continue
                }
                0x1c5 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x1da else 0x1cb)
                        continue
                }
                0x1cb -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x1da else 0x1d1)
                        continue
                }
                0x1d1 -> {
                        (v)!![1] = (((dexR((v)!![0]) as AlipayRule?)))!!.f13680g
                        (result)!![0] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.b(((dexR((v)!![12]) as HashMap<String?, Boolean>?)), ((dexR((v)!![1]) as MutableList<String?>?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x1da else 0x1d9)
                        continue
                }
                0x1d9 -> {
                        pc = 0x17e
                        continue
                }
                0x1da -> {
                        (v)!![1] = "还款信用卡"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x1f3 else 0x1e2)
                        continue
                }
                0x1e2 -> {
                        (v)!![1] = "银行处理中"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x1f3 else 0x1ea)
                        continue
                }
                0x1ea -> {
                        (v)!![1] = dexN(10L)
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![1])
                        (v)!![1] = dexN(1L)
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![1])
                        pc = 0x303
                        continue
                }
                0x1f3 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        (v)!![8] = dexN(6L)
                        (v)!![13] = "商品图片"
                        (v)!![14] = "交易详情"
                        pc = (if ((dexZero((v)!![1]))) 0x20a else 0x1fe)
                        continue
                }
                0x1fe -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x20a else 0x204)
                        continue
                }
                0x204 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x2f3 else 0x20a)
                        continue
                }
                0x20a -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x218 else 0x210)
                        continue
                }
                0x210 -> {
                        (v)!![1] = "退款成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x2f3 else 0x218)
                        continue
                }
                0x218 -> {
                        (v)!![1] = "余额明细详情"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x2f3 else 0x220)
                        continue
                }
                0x220 -> {
                        (v)!![1] = "交易成功"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x242 else 0x228)
                        continue
                }
                0x228 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x242 else 0x22e)
                        continue
                }
                0x22e -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x242 else 0x234)
                        continue
                }
                0x234 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![10]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x242 else 0x23a)
                        continue
                }
                0x23a -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![15]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x242 else 0x240)
                        continue
                }
                0x240 -> {
                        pc = 0x2f3
                        continue
                }
                0x242 -> {
                        (v)!![1] = "账单管理"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x250 else 0x24a)
                        continue
                }
                0x24a -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![11]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x25e else 0x250)
                        continue
                }
                0x250 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x25c else 0x256)
                        continue
                }
                0x256 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![13]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x25e else 0x25c)
                        continue
                }
                0x25c -> {
                        (v)!![1] = dexN(1L)
                        pc = 0x2b3
                        continue
                }
                0x25e -> {
                        (v)!![1] = "收款时间"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x276 else 0x266)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_w_block3(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x266 -> {
                        (v)!![1] = "已收款"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x276 else 0x26e)
                        continue
                }
                0x26e -> {
                        (v)!![1] = dexN(7L)
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![1])
                        (v)!![1] = dexN(1L)
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![1])
                        pc = 0x303
                        continue
                }
                0x276 -> {
                        (v)!![1] = "红包详情"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x286 else 0x27e)
                        continue
                }
                0x27e -> {
                        (v)!![1] = "领取成功，已存入余额"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x295 else 0x286)
                        continue
                }
                0x286 -> {
                        (v)!![1] = "查看红包记录"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x295 else 0x28e)
                        continue
                }
                0x28e -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![8])
                        (v)!![1] = dexN(1L)
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![1])
                        pc = 0x303
                        continue
                }
                0x295 -> {
                        (v)!![1] = "开始计算收益"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x303 else 0x29d)
                        continue
                }
                0x29d -> {
                        (v)!![1] = "收益到账"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x303 else 0x2a5)
                        continue
                }
                0x2a5 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![10]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x303 else 0x2ab)
                        continue
                }
                0x2ab -> {
                        (v)!![1] = dexN(9L)
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![1])
                        (v)!![1] = dexN(1L)
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![1])
                        pc = 0x303
                        continue
                }
                0x2b3 -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![8])
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![1])
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![14]) as Any?))))) 1 else 0))
                        (v)!![8] = (result)!![0]
                        pc = (if ((dexZero((v)!![8]))) 0x303 else 0x2bd)
                        continue
                }
                0x2bd -> {
                        (result)!![0] = dexN(((((dexR((v)!![3]) as MutableList<String?>?)))!! as List<Any?>).indexOf(((dexR((v)!![14]) as Any?))))
                        (v)!![8] = (result)!![0]
                        pc = (if (((dexI((v)!![8]) <= 0))) 0x303 else 0x2c3)
                        continue
                }
                0x2c3 -> {
                        (result)!![0] = dexN((((dexR((v)!![3]) as MutableList<String?>?)))!!.size)
                        (v)!![10] = (result)!![0]
                        (v)!![10] = dexN((dexI((v)!![10]) - dexI((v)!![1])))
                        pc = (if (((dexI((v)!![8]) >= dexI((v)!![10])))) 0x303 else 0x2ca)
                        continue
                }
                0x2ca -> {
                        (v)!![8] = dexN((dexI((v)!![8]) + dexI((v)!![1])))
                        (result)!![0] = (((dexR((v)!![3]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![1] = (result)!![0]
                        (v)!![10] = "更多"
                        (result)!![0] = dexN((if (((((dexR((v)!![10]) as String?)))!!.equals(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x2ef else 0x2d7)
                        continue
                }
                0x2d7 -> {
                        (v)!![1] = "推荐服务"
                        (result)!![0] = (((dexR((v)!![3]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![10] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![10]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x2ef else 0x2e3)
                        continue
                }
                0x2e3 -> {
                        (v)!![1] = "服务推荐"
                        (result)!![0] = (((dexR((v)!![3]) as MutableList<String?>?)))!![dexI((v)!![8])]
                        (v)!![8] = (result)!![0]
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as String?)))!!.equals(((dexR((v)!![8]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x303 else 0x2ef)
                        continue
                }
                0x2ef -> {
                        (v)!![1] = dexN(0L)
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![1])
                        pc = 0x303
                        continue
                }
                0x2f3 -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![8])
                        (v)!![1] = dexN(1L)
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![1])
                        pc = 0x303
                        continue
                }
                0x2f9 -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![8])
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![1])
                        pc = 0x303
                        continue
                }
                0x2fe -> {
                        (v)!![8] = dexN(2L)
                        (((dexR((v)!![0]) as RuleBase?)))!!.a = dexI((v)!![8])
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![1])
                        pc = 0x303
                        continue
                }
                0x303 -> {
                        (result)!![0] = dexN((if ((RuleSettings.i())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x37c else 0x309)
                        continue
                }
                0x309 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x315 else 0x30f)
                        continue
                }
                0x30f -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x355 else 0x315)
                        continue
                }
                0x315 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x321 else 0x31b)
                        continue
                }
                0x31b -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![4]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x355 else 0x321)
                        continue
                }
                0x321 -> {
                        (v)!![1] = "更改付款方式"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x331 else 0x329)
                        continue
                }
                0x329 -> {
                        (v)!![1] = "极速付款"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x355 else 0x331)
                        continue
                }
                0x331 -> {
                        (v)!![1] = "请输入支付密码"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x341 else 0x339)
                        continue
                }
                0x339 -> {
                        (v)!![1] = "使用指纹"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x355 else 0x341)
                        continue
                }
                0x341 -> {
                        (v)!![1] = "扫二维码/条码/小程序码"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x351 else 0x349)
                        continue
                }
                0x349 -> {
                        (v)!![1] = "收付款"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x355 else 0x351)
                        continue
                }
                0x351 -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as AlipayRule?)))!!.f13679f)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x37c else 0x355)
                        continue
                }
                else -> {
                    return pc
                }
            }
        }
    }

    private fun recovered_w_block4(v: Array<Any?>?, result: Array<Any?>?, initial_pc: Int): Int {
        var pc = initial_pc
        while (true) {
            when (pc) {
                0x355 -> {
                        (result)!![0] = dexN(System.currentTimeMillis())
                        (v)!![10] = (result)!![0]
                        (((dexR((v)!![0]) as AlipayRule?)))!!.f13678e = dexL((v)!![10])
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![8] = "time:"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![8]) as String?)))
                        (v)!![10] = dexN((((dexR((v)!![0]) as AlipayRule?)))!!.f13678e)
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(dexL((v)!![10]))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (v)!![8] = "sssss-time"
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![8]) as Any?)), ((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x37c
                        continue
                }
                0x378 -> {
                        (((dexR((v)!![0]) as RuleBase?)))!!.b = dexZ((v)!![1])
                        pc = 0x37a
                        continue
                }
                0x37a -> {
                        (v)!![3] = dexN(0L)
                        (v)!![12] = (v)!![3]
                        pc = 0x37c
                        continue
                }
                0x37c -> {
                        (v)!![1] = dexN((if (((((dexR((v)!![0]) as RuleBase?)))!!.b)) 1 else 0))
                        pc = (if ((dexZero((v)!![1]))) 0x426 else 0x380)
                        continue
                }
                0x380 -> {
                        pc = (if ((dexZero((v)!![2]))) 0x426 else 0x382)
                        continue
                }
                0x382 -> {
                        pc = (if ((!(dexZero((v)!![3])))) 0x3a3 else 0x384)
                        continue
                }
                0x384 -> {
                        (result)!![0] = (((dexR((v)!![0]) as RuleBase?)))!!.g(((dexR((v)!![2]) as CaptureNode?)))
                        (v)!![3] = (result)!![0]
                        (v)!![12] = DexReference()
                        ((((v)!![12] as DexReference?)))!!.value = HashMap<String?, Boolean>()
                        (result)!![0] = (((dexR((v)!![3]) as MutableList<String?>?)))!!.iterator()
                        (v)!![1] = (result)!![0]
                        pc = 0x391
                        continue
                }
                0x391 -> {
                        (result)!![0] = dexN((if (((((dexR((v)!![1]) as Iterator<String?>?)))!!.hasNext())) 1 else 0))
                        (v)!![2] = (result)!![0]
                        pc = (if ((dexZero((v)!![2]))) 0x3a3 else 0x397)
                        continue
                }
                0x397 -> {
                        (result)!![0] = (((dexR((v)!![1]) as Iterator<String?>?)))!!.next()
                        (v)!![2] = (result)!![0]
                        (v)!![2] = ((dexR((v)!![2]) as String?))
                        (v)!![8] = true
                        (result)!![0] = (((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!!.put((((dexR((v)!![2]) as Any?)) as String?), (((dexR((v)!![8]) as Any?)) as Boolean))
                        pc = 0x391
                        continue
                }
                0x3a3 -> {
                        (result)!![0] = dexN((if ((RuleSettings.i())) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x421 else 0x3a9)
                        continue
                }
                0x3a9 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![7]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x3b5 else 0x3af)
                        continue
                }
                0x3af -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![6]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x3ff else 0x3b5)
                        continue
                }
                0x3b5 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![5]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x3c1 else 0x3bb)
                        continue
                }
                0x3bb -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![4]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x3ff else 0x3c1)
                        continue
                }
                0x3c1 -> {
                        (v)!![1] = "更改付款方式"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x3d1 else 0x3c9)
                        continue
                }
                0x3c9 -> {
                        (v)!![1] = "极速付款"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x3ff else 0x3d1)
                        continue
                }
                0x3d1 -> {
                        (v)!![1] = "请输入支付密码"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x3e1 else 0x3d9)
                        continue
                }
                0x3d9 -> {
                        (v)!![1] = "使用指纹"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x3ff else 0x3e1)
                        continue
                }
                0x3e1 -> {
                        (v)!![1] = "扫二维码/条码/小程序码"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x3f1 else 0x3e9)
                        continue
                }
                0x3e9 -> {
                        (v)!![1] = "收付款"
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((!(dexZero((v)!![1])))) 0x3ff else 0x3f1)
                        continue
                }
                0x3f1 -> {
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![9]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x421 else 0x3f7)
                        continue
                }
                0x3f7 -> {
                        (v)!![1] = (v)!![16]
                        (result)!![0] = dexN((if ((((((dexR((v)!![12]) as HashMap<String?, Boolean>?)))!! as Map<Any?, Boolean>).containsKey(((dexR((v)!![1]) as Any?))))) 1 else 0))
                        (v)!![1] = (result)!![0]
                        pc = (if ((dexZero((v)!![1]))) 0x421 else 0x3ff)
                        continue
                }
                0x3ff -> {
                        (result)!![0] = dexN(System.currentTimeMillis())
                        (v)!![1] = (result)!![0]
                        (((dexR((v)!![0]) as AlipayRule?)))!!.f13678e = dexL((v)!![1])
                        (v)!![1] = DexReference()
                        ((((v)!![1] as DexReference?)))!!.value = StringBuilder()
                        (v)!![2] = "time:"
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(((dexR((v)!![2]) as String?)))
                        (v)!![4] = dexN((((dexR((v)!![0]) as AlipayRule?)))!!.f13678e)
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.append(dexL((v)!![4]))
                        (result)!![0] = (((dexR((v)!![1]) as StringBuilder?)))!!.toString()
                        (v)!![1] = (result)!![0]
                        (v)!![2] = "sssss-time"
                        (result)!![0] = arrayOf<Any?>(((dexR((v)!![2]) as Any?)), ((dexR((v)!![1]) as Any?)))
                        (v)!![1] = (result)!![0]

                        pc = 0x421
                        continue
                }
                0x421 -> {
                        (v)!![1] = (v)!![18]
                        (((dexR((v)!![0]) as AlipayRule?)))!!.y(((dexR((v)!![1]) as RuleContext?)), ((dexR((v)!![3]) as MutableList<String?>?)), ((dexR((v)!![12]) as HashMap<String?, Boolean>?)))
                        pc = 0x426
                        continue
                }
                0x426 -> {
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
        (billInfo)!!.setOrigin("支付宝")
        (billInfo)!!.setAsset("支付宝")
        (billInfo)!!.setIncome(true)
        run {
            var i6: Int = 0
            while ((i6 < (list)!!.size)) {
                    var str: String? = (list)!![i6]
                    if (((str)!!.contains(("元")!!) && (i6 > 2))) {
                        var strReplace: String? = ((str)!!.replace(("元")!!, ("")!!))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace)) {
                            (billInfo)!!.setNumber(strReplace)
                            (billInfo)!!.setRemark((((list)!![(i6 - 3)])!!.replace(("送你的红包")!!, ("")!!) + "的红包"))
                            (billInfo)!!.setShopName((billInfo)!!.getRemark())
                            return billInfo
                        }
                    }
                i6++
            }
        }
        return null
    }

    fun z(list: MutableList<String?>?): RuleBill? {
        var iIndexOf: Int = 0
        var billInfo: RuleBill? = RuleBill()
        (billInfo)!!.setOrigin("支付宝")
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
                                (billInfo)!!.setRemark("发送支付宝红包")
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
                                (billInfo)!!.setRemark("发送支付宝红包")
                                (billInfo)!!.setShopName((billInfo)!!.getRemark())
                                return billInfo
                            }
                        } else {
                            i6++
                            continue
                        }
                    } else if ((((str)!!.contains(("已领取")!!) && ((((str)!!.indexOf(("元")!!)).also { iIndexOf = it }) > 4)) && (str)!!.contains(("/")!!))) {
                        var strReplace3: String? = ((str)!!.substring(((str)!!.lastIndexOf(("/")!!) + 1), iIndexOf))!!.replace((",")!!, ("")!!)
                        if (Amounts.J(strReplace3)) {
                            (billInfo)!!.setNumber(strReplace3)
                            (billInfo)!!.setRemark("发送支付宝红包")
                            (billInfo)!!.setShopName((billInfo)!!.getRemark())
                            return billInfo
                        }
                    }
                i6++
            }
        }
        return null
    }

    companion object {
        private var f13671h: Int = 2

        private var f13672i: Int = 6

        private var f13673j: Int = 7

        private var f13674k: Int = 9

        private var f13675l: Int = 10

        private var f13676m: Int = 11

        private var f13677n: AlipayRule? = null

        fun A(): AlipayRule? {
            if ((f13677n == null)) {
                f13677n = AlipayRule()
            }
            return f13677n
        }
    }
}
