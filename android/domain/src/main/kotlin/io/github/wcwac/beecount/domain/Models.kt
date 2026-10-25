package io.github.wcwac.beecount.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

@JvmInline
value class CurrencyCode(val value: String) {
    init { require(value.matches(Regex("[A-Z]{3}"))) { "币种代码无效" }; require(scale in 0..3) { "不支持该币种精度" } }
    val scale: Int get() = Currency.getInstance(value).defaultFractionDigits
    companion object { val CNY = CurrencyCode("CNY") }
}

data class Money(val minor: Long, val currency: CurrencyCode = CurrencyCode.CNY) {
    fun decimal(): String = BigDecimal.valueOf(minor, currency.scale).toPlainString()
    fun display(): String = "${currency.value} ${decimal()}"
    companion object {
        const val MAX_INPUT_MINOR = 99_999_999_999_999L
        fun parse(text: String, currency: CurrencyCode = CurrencyCode.CNY): Money {
            require(text.length <= 100 && text.trim().matches(Regex("[+-]?(?:[0-9]+|[0-9]{1,3}(?:,[0-9]{3})+)(?:\\.[0-9]+)?"))) { "请输入有效金额" }
            val decimal = text.trim().replace(",", "").toBigDecimal()
            require(decimal.scale() <= currency.scale) { "金额最多支持 ${currency.scale} 位小数" }
            val minor = try { decimal.movePointRight(currency.scale).setScale(0, RoundingMode.UNNECESSARY).longValueExact() }
                catch (_: ArithmeticException) { throw IllegalArgumentException("金额超出允许范围") }
            require(minor in -MAX_INPUT_MINOR..MAX_INPUT_MINOR) { "金额超出允许范围" }
            return Money(minor, currency)
        }
    }
}

enum class EntryKind { EXPENSE, INCOME, TRANSFER, CREDIT_PAYMENT, LEND, BORROW, COLLECT, REPAY, REFUND, REIMBURSEMENT, REIMBURSEMENT_PAYMENT, OPENING, ADJUSTMENT }
enum class AccountKind { CASH, BANK, CREDIT, RECEIVABLE, PAYABLE, STORED_VALUE }
enum class CandidateState { PENDING, SAVED, DISMISSED }
enum class ConfirmationReason { WEAK_IDENTITY, ACCOUNT_UNRESOLVED, BOOK_UNAVAILABLE, CURRENCY_CONFLICT, EVIDENCE_CONFLICT, POSSIBLE_DUPLICATE }

data class Book(val id: String, val name: String, val currency: String = "CNY", val archived: Boolean = false)
data class Account(val id: String, val name: String, val currency: String = "CNY", val kind: AccountKind = AccountKind.CASH, val balance: Long = 0, val archived: Boolean = false, val statementDay: Int = 1, val repaymentDay: Int = 10)
data class Category(val id: String, val name: String, val income: Boolean = false, val parentId: String? = null, val archived: Boolean = false)
data class Tag(val id: String, val name: String)

data class EntryDraft(
    val id: String = newId(), val bookId: String, val kind: EntryKind = EntryKind.EXPENSE,
    val amount: Money, val accountId: String? = null, val otherAccountId: String? = null,
    val categoryId: String? = null, val merchant: String = "", val note: String = "",
    val occurredAt: Long = System.currentTimeMillis(), val parentId: String? = null,
    val feeMinor: Long = 0, val interestMinor: Long = 0,
    val excludedFromTotals: Boolean = false, val excludedFromBudget: Boolean = false,
    val tagIds: List<String> = emptyList(), val attachmentIds: List<String> = emptyList()
)
data class LedgerEntry(val draft: EntryDraft, val revision: Long = 1, val deleted: Boolean = false, val categoryName: String = "", val accountName: String = "")
data class HistoryFilter(val bookId: String? = null, val accountId: String? = null, val start: Long? = null, val endExclusive: Long? = null, val query: String = "", val deleted: Boolean = false, val categoryId: String? = null, val attachmentsOnly: Boolean = false, val kinds: Set<EntryKind> = emptySet(), val budgetId: String? = null)
data class Summary(val income: Long = 0, val expense: Long = 0)
data class ChartPoint(val label: String, val income: Long = 0, val expense: Long = 0)
data class CategoryTotal(val categoryId: String?, val label: String, val amount: Long)
data class CreditStatement(val start: Long, val endExclusive: Long, val repaymentAt: Long, val charges: Long, val repayments: Long, val closingBalance: Long)

sealed interface FinancialCommand {
    val operationId: String
    data class Save(val draft: EntryDraft, val expectedRevision: Long? = null, val candidateId: String? = null, val candidateRevision: Long? = null, override val operationId: String = newId(), val expectedGeneration: Long? = null) : FinancialCommand
    data class Delete(val id: String, val expectedRevision: Long, override val operationId: String = newId()) : FinancialCommand
    data class Restore(val id: String, val expectedRevision: Long, override val operationId: String = newId()) : FinancialCommand
    data class Purge(val id: String, val expectedRevision: Long, override val operationId: String = newId()) : FinancialCommand
    data class MarkReimbursement(val id: String, val expectedRevision: Long, val receivableAccountId: String, override val operationId: String = newId()) : FinancialCommand
}
data class CommitReceipt(val operationId: String, val entryId: String, val revision: Long)
sealed interface CommitResult {
    data class Success(val receipt: CommitReceipt) : CommitResult
    data class Rejected(val reason: String) : CommitResult
}
interface FinancialWriter { suspend fun execute(command: FinancialCommand): CommitResult }

data class CaptureProposal(
    val packageName: String, val pageClass: String, val sessionId: String,
    val windowId: Int, val kind: EntryKind, val amount: Money,
    val merchant: String = "", val description: String = "", val paymentMethod: String = "",
    val occurredAt: Long = System.currentTimeMillis(), val visibleTransactionTime: Boolean = false,
    val paymentId: String? = null, val orderReference: String? = null,
    val identityKind: String = "UNRESOLVED", val strongKey: String? = null, val windowKey: String,
    val requiresConfirmation: Boolean = true,
    val fromPaymentMethod: String = "", val toPaymentMethod: String = "",
    val feeMinor: Long = 0, val discountMinor: Long = 0, val sourceRoute: String = ""
)
data class CaptureCandidate(
    val id: String, val proposal: CaptureProposal, val bookId: String?, val accountId: String?,
    val categoryId: String?, val revision: Long = 1, val state: CandidateState = CandidateState.PENDING,
    val reasons: Set<ConfirmationReason> = emptySet(), val entryId: String? = null,
    val otherAccountId: String? = null
)
enum class CaptureUpsertKind { CREATED, UPDATED, ALREADY_HANDLED, CONFLICT }
data class CaptureUpsertResult(val kind: CaptureUpsertKind, val candidate: CaptureCandidate?)

data class CaptureConfig(
    val enabled: Boolean = false, val selectedPackages: Set<String> = emptySet(),
    val bookId: String? = null, val overlayEnabled: Boolean = true, val autoSave: Boolean = false
)
data class CaptureStatus(
    val connected: Boolean = false, val events: Long = 0, val reads: Long = 0,
    val proposals: Long = 0, val candidates: Long = 0, val skipped: Long = 0,
    val stage: String = "IDLE", val lastResult: String = "NOT_STARTED",
    val lastPackage: String = "", val lastPage: String = "", val lastEventAt: Long = 0,
    val failure: String = "", val effectivePackages: Set<String> = emptySet(),
    val overlayResult: String = "NOT_REQUESTED"
)

data class UserPreferences(
    val currentBookId: String? = null, val capture: CaptureConfig = CaptureConfig(),
    val theme: String = "system", val hideAmounts: Boolean = false,
    val appLock: Boolean = false, val secureScreen: Boolean = false,
    val backupTree: String? = null, val backupRetention: Int = 7
)

data class Budget(val id: String = newId(), val bookId: String, val name: String, val amount: Long, val start: Long, val endExclusive: Long, val categoryId: String? = null, val spent: Long = 0)
data class Goal(val id: String = newId(), val bookId: String, val name: String, val target: Long, val progress: Long = 0, val mode: String = "manual", val accountId: String? = null)
data class RecurringRule(val id: String = newId(), val draft: EntryDraft, val interval: Int = 1, val unit: String = "MONTH", val nextAt: Long, val until: Long? = null, val maxCount: Int? = null, val executed: Int = 0, val enabled: Boolean = true)
data class Instalment(val id: String = newId(), val entryId: String, val sequence: Int, val dueAt: Long, val principal: Long, val fee: Long = 0, val paymentId: String? = null)
data class Investment(val id: String = newId(), val bookId: String, val name: String, val currency: String = "CNY", val quantity: String = "0", val cost: Long = 0, val marketValue: Long? = null, val realized: Long = 0)
data class InvestmentEvent(val id: String = newId(), val investmentId: String, val kind: String, val quantity: String = "0", val amount: Long, val occurredAt: Long = System.currentTimeMillis())
data class Attachment(val id: String, val name: String, val mime: String, val size: Long, val localPath: String)
data class MerchantRule(val id: String = newId(), val merchant: String = "", val packageName: String = "", val keyword: String = "", val categoryId: String?, val accountId: String? = null)
data class AccountMapping(val id: String = newId(), val packageName: String, val paymentMethod: String = "", val accountId: String)
