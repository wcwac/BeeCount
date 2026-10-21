package com.tntlikely.beecount.capture

import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/** Selectors are tied to semantic labels; positional layouts are explicit per adapter. */
internal class CaptureReader(val snapshot: CaptureSnapshot) {
    val texts = snapshot.texts
    fun has(vararg words: String) = texts.any { text -> words.any { text.contains(it) } }
    private fun stateMatches(text: String, word: String): Boolean {
        val pattern = STATE_PATTERNS.getOrPut(word) {
            Regex("(?:^|[\\s：:])${Regex.escape(word)}(?=$|[\\s¥￥：:，,0-9+−()（）-])")
        }
        return text == word || pattern.containsMatchIn(text)
    }
    fun state(word: String) = texts.any { stateMatches(it, word) }
    fun stateIndex(words: List<String>) = texts.indexOfFirst { text -> words.any { stateMatches(text, it) } }
    fun after(vararg labels: String): String? {
        for (label in labels) for ((index, text) in texts.withIndex()) {
            if (text == label || text == "$label：" || text == "$label:") {
                texts.getOrNull(index + 1)?.takeIf { it.length <= 160 }?.let { return it }
            } else if (text.startsWith("$label：") || text.startsWith("$label:")) {
                return text.substring(label.length + 1).trim().takeIf { it.isNotEmpty() }
            }
        }
        return null
    }
    private fun moneyFrom(first: String, nextIndex: Int): Long? {
        val next = texts.getOrNull(nextIndex)
        val joined = when {
            first.endsWith('.') -> first + (next ?: return null)
            next == "." && !first.contains('.') && !first.endsWith("元") ->
                first + "." + (texts.getOrNull(nextIndex + 1) ?: return null)
            next?.startsWith('.') == true && !first.contains('.') && !first.endsWith("元") -> first + next
            else -> first
        }
        // An observed decimal fragment must validate as a whole. Never silently
        // turn an incomplete/invalid 12 + .345 into the different amount 12.00.
        return captureMoney(joined)
    }
    private fun moneyAt(index: Int): Long? = texts.getOrNull(index)?.let { moneyFrom(it, index + 1) }

    fun labelMoney(vararg labels: String): Pair<Long?, String?> {
        for (label in labels) {
            val found = mutableSetOf<Long>()
            for ((index, text) in texts.withIndex()) {
                if (!text.startsWith(label)) continue
                val suffix = text.removePrefix(label).trim(' ', ':', '：')
                if (suffix.isNotEmpty()) moneyFrom(suffix, index + 1)?.let(found::add)
                else {
                    val next = texts.getOrNull(index + 1)
                    val at = if (next == "¥" || next == "￥") index + 2 else index + 1
                    moneyAt(at)?.let(found::add)
                }
            }
            if (found.size == 1) return found.first() to label
            if (found.size > 1) return null to "CONFLICT"
        }
        return null to null
    }
    /** Labels in this group describe the same value, rather than price priorities. */
    fun consistentLabelMoney(vararg labels: String): Pair<Long?, String?> {
        var selected: Pair<Long?, String?> = null to null
        for (label in labels) {
            val found = labelMoney(label)
            if (found.second == "CONFLICT" ||
                (found.first != null && selected.first != null && found.first != selected.first)) return null to "CONFLICT"
            if (found.first != null) selected = found
        }
        return selected
    }
    fun currencyAmounts(): Set<Long> = texts.flatMapIndexed { index, text ->
        if (index > 0 && texts[index - 1].contains(DISCOUNT_LABEL)) emptyList()
        else if (text == "¥" || text == "￥") {
            listOfNotNull(moneyAt(index + 1))
        }
        else if (SPLIT_CURRENCY_WHOLE.matches(text)) listOfNotNull(moneyFrom(text, index + 1))
        else CURRENCY.findAll(text).mapNotNull {
            if (it.range.last == text.lastIndex) moneyFrom(it.value, index + 1) else captureMoney(it.value)
        }.toList()
    }.toSet()

    fun transactionTime(refund: Boolean = false): Long? {
        val labels = if (refund) arrayOf("退款时间", "退款受理时间", "到账时间", "交易时间", "创建时间", "日期")
            else arrayOf("交易时间", "支付时间", "创建时间", "下单时间", "日期")
        val raw = after(*labels) ?: return null
        for (format in TIME_FORMATS) {
            val parsed = runCatching {
                val position = ParsePosition(0)
                val date = SimpleDateFormat(format, Locale.CHINA).apply { isLenient = false }.parse(raw, position)
                date?.time?.takeIf { position.index == raw.length }
            }.getOrNull()
            if (parsed != null && parsed in 946684800000L..snapshot.observedAt + 86_400_000L) return parsed
        }
        return null
    }

    companion object {
        private val CURRENCY = Regex("[¥￥]\\s*[0-9]{1,9}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?(?![0-9.,])")
        private val DISCOUNT_LABEL = Regex("优惠|红包|立减|原价|服务费")
        private val SPLIT_CURRENCY_WHOLE = Regex("[¥￥]\\s*[0-9]{1,9}(?:,[0-9]{3})*\\.")
        private val STATE_PATTERNS = ConcurrentHashMap<String, Regex>()
        private val TIME_FORMATS = listOf("yyyy-MM-dd HH:mm:ss", "yyyy年MM月dd日 HH:mm:ss", "yyyy/MM/dd HH:mm:ss",
            "yyyy-MM-dd HH:mm", "yyyy年MM月dd日 HH:mm", "yyyy/MM/dd HH:mm")
    }
}

data class CaptureProfile(
    val id: String,
    val successWords: List<String> = listOf("支付成功", "付款成功", "交易成功"),
    val amountLabels: List<String> = listOf("实付金额", "实付款", "实际支付", "支付金额", "付款金额", "交易金额"),
    val merchantLabels: List<String> = listOf("商户名称", "商家", "收款方", "交易对象"),
)

/** One family per real application group; no downloaded or executable rule scripts. */
class CaptureAdapterRegistry {
    private val profiles = listOf(
        CaptureProfile("wechat", successWords = listOf("支付成功", "付款成功", "交易成功", "收款成功", "收款到账", "已收到", "已支付"),
            merchantLabels = listOf("商户全称", "商户名称", "收款方", "收款人")),
        CaptureProfile("alipay", successWords = listOf("支付成功", "付款成功", "交易成功", "自动扣款成功", "代付成功", "免密支付成功"),
            merchantLabels = listOf("收款方", "商家", "商户名称", "交易对方")),
        CaptureProfile("unionpay", amountLabels = listOf("实付金额（元）", "实收金额（元）", "收入金额", "支出金额", "付款金额", "交易金额", "支付金额", "订单金额")),
        CaptureProfile("jingdong", amountLabels = listOf("实付金额", "支付金额", "交易金额", "付款金额"),
            merchantLabels = listOf("收款方", "商户名称", "交易商户")),
        CaptureProfile("pinduoduo", successWords = listOf("支付成功", "已支付", "拼单成功", "免拼成功"),
            amountLabels = listOf("实付款", "实付", "支付金额", "交易金额")),
        CaptureProfile("meituan", successWords = listOf("支付成功", "付款成功", "交易成功", "先用后付自动扣款成功"),
            amountLabels = listOf("实付", "支付金额", "交易金额", "交易成功", "支付成功")),
        CaptureProfile("alibaba", amountLabels = listOf("实付款", "实付", "合计", "支付金额")),
        CaptureProfile("idlefish", amountLabels = listOf("实付款", "实付", "支付金额", "付款金额")),
        CaptureProfile("hippo", amountLabels = listOf("支付成功", "实付金额", "实际支付")),
        CaptureProfile("eleme", amountLabels = listOf("实付", "合计", "支付金额")),
        CaptureProfile("huawei", amountLabels = listOf("交易金额", "支付金额", "付款金额"),
            merchantLabels = listOf("商家", "商品名称", "商户名称")),
        CaptureProfile("ccb", amountLabels = listOf("交易金额", "支付金额", "付款金额"),
            merchantLabels = listOf("商户名称", "商户名", "商品名称")),
        CaptureProfile("kuaishou", successWords = listOf("购买成功", "支付成功", "付款成功"),
            amountLabels = listOf("实付款", "实付", "合计", "支付金额")),
        CaptureProfile("douyin", amountLabels = listOf("实付价", "实付款", "实付", "合计", "支付金额")),
        CaptureProfile("taobao", successWords = listOf("支付成功", "免密支付成功", "付款成功"),
            amountLabels = listOf("实付价", "实付款", "实付", "合计", "支付金额")),
    ).associateBy { it.id }
    private val importantLabels = (profiles.values.flatMap { it.successWords } + FAILURE_STATES + REFUND_STATES +
        MOVEMENT_STATES + CHECKOUT_LABELS + listOf("现价", "交易详情", "账单详情", "下单成功")).distinct()

    /** Cheap conservative gate only; the serial parser still decides page/state. */
    fun mayAffectSession(snapshot: CaptureSnapshot): Boolean = isResetPage(snapshot.pageClass) ||
        snapshot.nodes.any { node -> importantLabels.any(node.text::contains) }

    fun parse(app: CaptureApp, snapshot: CaptureSnapshot): CaptureFields {
        val profile = profiles.getValue(app.adapter)
        val r = CaptureReader(snapshot)
        val failed = FAILURE_STATES.any(r::state)
        val refund = REFUND_STATES.any(r::state)
        val fundsMove = app.adapter in setOf("wechat", "alipay", "unionpay", "huawei", "ccb", "jingdong", "douyin") &&
            MOVEMENT_STATES.any(r::state)
        val success = fundsMove || profile.successWords.any(r::state)
        val incoming = INCOMING_STATES.any(r::state) || r.has("收入金额", "实收金额")
        val checkout = r.has(*CHECKOUT_LABELS.toTypedArray()) ||
            (app.adapter == "idlefish" && r.has("现价"))
        val orderOnly = r.has("下单成功，商家", "下单成功，等待商家")
        val detailOnly = r.has("交易详情", "账单详情") &&
            r.has("交易时间", "支付时间", "创建时间", "订单时间", "扣款时间", "退款时间", "日期")
        val page = when {
            failed -> CapturePage.FAILURE
            refund -> CapturePage.REFUND
            success -> CapturePage.SUCCESS
            detailOnly -> CapturePage.SUCCESS
            orderOnly && app.adapter in setOf("eleme", "taobao") -> CapturePage.SUCCESS
            checkout -> CapturePage.CHECKOUT
            else -> CapturePage.NONE
        }
        if (page == CapturePage.NONE || page == CapturePage.FAILURE) return CaptureFields(page,
            resetSession = isResetPage(snapshot.pageClass))
        val evidence = mutableMapOf("state" to if (refund) "refund-label" else if (success) "success-label" else if (checkout) "checkout-label" else "order-label")
        val issues = mutableSetOf<String>()
        if (snapshot.truncated) issues += "SNAPSHOT_TRUNCATED"
        if (r.has("美元", "USD", "港币", "HKD", "日元", "JPY", "欧元", "EUR")) issues += "CURRENCY_UNCERTAIN"
        if (orderOnly && !success) issues += "PAYMENT_UNCONFIRMED"
        if (detailOnly && !success && !refund) issues += "PAYMENT_UNCONFIRMED"
        // A refund page may retain the original payment and requested/cumulative
        // totals. Those are not interchangeable with the current refund amount.
        val refundHasOtherAmounts = refund && r.has(*NON_CURRENT_REFUND_LABELS.toTypedArray())
        val amountLabels = when {
            refund -> REFUND_AMOUNT_LABELS
            fundsMove -> MOVEMENT_AMOUNT_LABELS + profile.amountLabels
            incoming -> INCOMING_AMOUNT_LABELS + profile.amountLabels
            else -> profile.amountLabels
        }
        val labeledAmount = if (refund) r.consistentLabelMoney(*amountLabels.toTypedArray())
            else r.labelMoney(*amountLabels.toTypedArray())
        val anchored = if (refund && labeledAmount.second == null) r.labelMoney("退款成功", "退款已到账") else labeledAmount
        var amount = anchored.first
        if (anchored.second == "CONFLICT") issues += "AMOUNT_CONFLICT"
        else if (amount != null) evidence["amount"] = "label:${anchored.second}"
        val labeledMerchant = r.after(*profile.merchantLabels.toTypedArray())?.takeIf(::validMerchant)
        var merchant = labeledMerchant
        var remark = r.after("商品说明", "交易说明", "商品名称", "订单说明", "商品详情")
        var method = if (refund) r.after("退款至", "退款账户", "退款方式")
            else r.after("付款方式", "支付方式", "付款银行")
        var inlineDiscount: Long? = null
        var signedResult: String? = null

        // These are guarded legacy layouts, not offsets applied to every page.
        when (app.adapter) {
            "wechat" -> if (page == CapturePage.SUCCESS && merchant == null) {
                val status = r.texts.indexOfFirst { it == "支付成功" || it == "付款成功" }
                if (status >= 0) merchant = r.texts.getOrNull(status + 1)?.takeIf {
                    captureMoney(it) == null && !it.contains('¥') && !it.contains('￥')
                }
            }
            "idlefish" -> if (page == CapturePage.CHECKOUT) {
                val index = r.texts.indexOfFirst { it.startsWith("现价") }
                if (index >= 0) {
                    val raw = r.texts[index].removePrefix("现价").trim()
                    // Preserve thousands separators; a trailing descriptive
                    // comma is different from the comma in 1,234.00.
                    val value = if (raw.substringAfterLast(',').any(Char::isLetter)) raw.substringBeforeLast(',') else raw
                    captureMoney(value)?.let { amount = it; evidence["amount"] = "idlefish:current-price" }
                    remark = r.texts.getOrNull(index + 1)?.lineSequence()?.firstOrNull()
                }
            }
            "eleme" -> if (page == CapturePage.CHECKOUT) {
                val index = r.texts.indexOf("提交订单")
                if (index > 0 && amount == null) r.texts.getOrNull(index - 1)?.let(::captureMoney)?.let {
                    amount = it; evidence["amount"] = "eleme:submit-adjacent"
                }
                method = method ?: r.after("已选")
            }
            "taobao" -> if (page == CapturePage.CHECKOUT) {
                val pay = r.texts.indexOf("确认付款")
                val actual = r.texts.indexOf("实付价")
                val passwordless = r.texts.indexOf("免密支付")
                val legacy = when {
                    pay >= 5 -> r.texts.getOrNull(pay - 5)
                    actual >= 0 -> r.texts.getOrNull(actual + 2)
                    passwordless >= 0 -> r.texts.getOrNull(passwordless + 1)
                    else -> null
                }?.let(::captureMoney)
                if (amount == null && legacy != null) { amount = legacy; evidence["amount"] = "taobao:checkout-layout" }
                if (pay >= 2) method = r.texts[pay - 2]
                // Product text is a remark, not a confirmed merchant name.
                val yuan = r.texts.indexOfFirst { it.startsWith("￥") }
                if (remark == null && yuan > 0) remark = r.texts[yuan - 1].takeIf { it !in setOf("券后", "七天无理由退换", "合计") }
            }
            "huawei" -> {
                val status = r.texts.indexOf("交易成功")
                if (!refund && status > 0 && amount == null && "AMOUNT_CONFLICT" !in issues) r.texts.getOrNull(status - 1)?.let(::captureMoney)?.let {
                    amount = it; evidence["amount"] = "huawei:transaction-result"
                }
                if (!refund && status > 0 && r.texts.getOrNull(status - 1)?.let(::captureMoney) == amount) {
                    signedResult = r.texts[status - 1]
                }
                if (status >= 0 && merchant == null) merchant = r.texts.getOrNull(status + 1)
            }
            "ccb", "unionpay" -> {
                val index = r.texts.indexOf("交易详情")
                val offset = if (app.adapter == "ccb") 1 else 2
                if (!refund && detailOnly && index >= 0 && amount == null && "AMOUNT_CONFLICT" !in issues) r.texts.getOrNull(index + offset)?.let(::captureMoney)?.let {
                    amount = it; evidence["amount"] = "${app.adapter}:transaction-detail"
                }
            }
            "meituan", "jingdong", "douyin" -> {
                val status = r.stateIndex(if (refund) REFUND_STATES else profile.successWords)
                val adjacentIsOriginal = refund && r.texts.getOrNull(status - 2)?.let { label ->
                    NON_CURRENT_REFUND_LABELS.any(label::startsWith)
                } == true
                if (status > 0 && amount == null && !adjacentIsOriginal && "AMOUNT_CONFLICT" !in issues) {
                    r.texts.getOrNull(status - 1)?.let(::captureMoney)?.let { amount = it; evidence["amount"] = "${app.adapter}:status-adjacent" }
                }
                if (status > 0 && r.texts.getOrNull(status - 1)?.let(::captureMoney) == amount) {
                    signedResult = r.texts[status - 1]
                }
                if (status > 1 && merchant == null && amount != null && r.has("创建时间", "交易时间", "下单时间", "退款受理时间")) {
                    merchant = r.texts[status - 2].takeIf { captureMoney(it) == null && it.length in 2..80 }
                }
                if (app.adapter == "meituan") {
                    val inline = r.texts.indexOfFirst { it.startsWith("交易成功 ¥") }
                    if (!refund && inline >= 0) r.texts.getOrNull(inline + 1)?.let { value ->
                        if (method == null) method = value.substringBefore("已优惠").trim().takeIf(String::isNotEmpty)
                        Regex("已优惠[¥￥]\\s*[0-9]+(?:\\.[0-9]{1,2})?").find(value)?.value
                            ?.removePrefix("已优惠")?.let(::captureMoney)?.let { inlineDiscount = it }
                    }
                    val details = r.texts.indexOf("交易详情")
                    if (details >= 0 && r.has("交易时间")) {
                        if (!refund && amount == null && "AMOUNT_CONFLICT" !in issues) r.texts.getOrNull(details + 3)?.let(::captureMoney)?.let { amount = it; evidence["amount"] = "meituan:transaction-detail" }
                        if (merchant == null) merchant = r.texts.getOrNull(details + 2)
                    }
                }
            }
        }
        if (amount == null && "AMOUNT_CONFLICT" !in issues && !refundHasOtherAmounts) {
            val amounts = r.currencyAmounts()
            if (amounts.size == 1) {
                amount = amounts.first(); evidence["amount"] = "single-currency-amount"
                issues += "AMOUNT_UNANCHORED"
            }
            else if (amounts.size > 1) issues += "AMOUNT_CONFLICT"
        }
        if (amount == null && refundHasOtherAmounts) evidence["amount"] = "refund:original-amount-rejected"
        val transfer = fundsMove || r.has("转账", "提现", "充值成功", "还款成功")
        val positiveDetail = signedResult?.startsWith("+") == true ||
            (detailOnly && r.texts.any { it.startsWith("+") && captureMoney(it) == amount })
        val negativeDetail = detailOnly && r.texts.any { (it.startsWith("-") || it.startsWith("−")) && captureMoney(it) == amount }
        val direction = when { transfer -> null; refund || incoming || positiveDetail -> "income";
            success || checkout || negativeDetail -> "expense"; else -> null }
        evidence["direction"] = when {
            transfer -> "funds-move"
            refund -> "refund"
            incoming -> "incoming-label"
            signedResult?.firstOrNull() in setOf('+', '-', '−') -> "signed-result"
            positiveDetail || negativeDetail -> "signed-detail"
            success -> "payment-state"
            checkout -> "checkout"
            else -> "unknown"
        }
        if (transfer) issues += "DIRECTION_UNCERTAIN"
        if (!refund && r.labelMoney("手续费", "服务费").first != null) issues += "FEE_PRESENT"
        val discount = if (refund) null else (r.labelMoney("支付优惠", "已优惠", "优惠金额", "支付立减").first ?: inlineDiscount)
        val transactionReference = if (refund) r.after("退款单号", "退款交易单号", "交易单号", "交易订单号")
            else r.after("交易单号", "交易订单号")
        val reference = (transactionReference ?: r.after("商户单号", "订单编号", "订单号"))
            ?.trim()?.takeIf { Regex("[A-Za-z0-9_-]{8,80}").matches(it) }
            ?.let { captureHash("${app.adapter}:$it") }
        merchant = merchant?.takeIf(::validMerchant)
        if (merchant != null && labeledMerchant == null) issues += "MERCHANT_UNCERTAIN"
        return CaptureFields(page, amount, merchant, remark, method, discount, r.transactionTime(refund), reference,
            direction, evidence, issues, referenceIsTransaction = transactionReference != null)
    }

    private fun validMerchant(value: String): Boolean = value.length in 2..80 &&
        captureMoney(value) == null && !value.contains('¥') && !value.contains('￥') &&
        value !in MERCHANT_CONTROL_LABELS && !value.startsWith("https://") && !value.startsWith("http://") &&
        !DATE_TEXT.matches(value)

    companion object {
        internal fun isResetPage(pageClass: String) = pageClass in HOME_PAGES
        private val FAILURE_STATES = listOf("支付失败", "付款失败", "交易失败", "已取消", "订单已取消")
        private val MOVEMENT_STATES = listOf("转账成功", "充值成功", "提现成功", "还款成功", "已转入", "已转出")
        private val CHECKOUT_LABELS = listOf("确认付款", "提交订单", "确认订单", "立即支付", "极速支付", "提交月付订单")
        private val REFUND_STATES = listOf("退款成功", "已退款", "已全额退款", "退款已到账")
        private val INCOMING_STATES = listOf("收款成功", "收款到账", "已收款", "已收到")
        private val REFUND_AMOUNT_LABELS = listOf("本次退款金额", "实际退款金额", "实际退款", "退款到账金额",
            "退款金额（元）", "退款金额(元)", "退款金额")
        private val NON_CURRENT_REFUND_LABELS = listOf("原订单金额", "订单金额", "原支付金额", "实付", "实际支付",
            "支付金额", "付款金额", "交易金额", "合计", "原价", "申请退款", "退款申请金额", "累计退款", "已退金额", "已退款金额", "退款合计", "退款总额")
        private val MOVEMENT_AMOUNT_LABELS = listOf("转账金额", "转出金额", "转入金额", "提现金额", "充值金额", "还款金额", "到账金额")
        private val INCOMING_AMOUNT_LABELS = listOf("实收金额（元）", "实收金额", "收款金额", "收入金额", "到账金额")
        private val MERCHANT_CONTROL_LABELS = setOf(
            "支付成功", "付款成功", "交易成功", "退款成功", "完成", "返回", "查看订单", "更多", "返回商家",
            "支付金额", "付款金额", "交易金额", "实付款", "实付金额", "实付", "合计", "订单金额",
            "支付方式", "付款方式", "退款至", "付款银行", "收款方", "收款人", "商户名称", "商家", "商户名", "商户全称",
            "交易单号", "交易订单号", "商户单号", "订单编号", "订单号", "退款单号",
            "创建时间", "交易时间", "支付时间", "退款时间", "下单时间", "交易详情", "账单详情", "订单详情",
            "确认付款", "确认订单", "提交订单", "查看详情", "收货地址", "商品详情", "商品名称",
            "退款金额", "本次退款金额", "实际退款金额", "退款账户", "退款方式", "转账金额", "转入金额", "转出金额",
            "手续费", "服务费", "到账金额", "收入金额", "实收金额", "收款金额",
        )
        private val DATE_TEXT = Regex("[0-9]{4}[-/年][0-9]{1,2}[-/月][0-9]{1,2}.*")
        private val HOME_PAGES = setOf(
            "com.taobao.tao.TBMainActivity", "com.taobao.tao.welcome.Welcome",
            "com.taobao.idlefish.maincontainer.activity.MainActivity",
            "com.meituan.android.pt.homepage.activity.MainActivity", "com.dianping.v1.NovaMainActivity",
            "com.tencent.mm.ui.LauncherUI", "com.xunmeng.pinduoduo.ui.activity.HomeActivity",
            "com.ss.android.ugc.aweme.main.MainActivity", "com.yxcorp.gifshow.HomeActivity",
            "com.jd.jrapp.bm.mainbox.main.MainActivity", "com.ccb.longjiLife.MainActivity",
        )
    }
}
