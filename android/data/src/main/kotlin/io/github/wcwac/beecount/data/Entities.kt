package io.github.wcwac.beecount.data

import androidx.room.*

@Entity(tableName = "books")
data class BookEntity(@PrimaryKey val id: String, val name: String, val currency: String, val archived: Boolean = false)
@Entity(tableName = "accounts")
data class AccountEntity(@PrimaryKey val id: String, val name: String, val currency: String, val kind: String, val archived: Boolean = false, val statementDay: Int = 1, val repaymentDay: Int = 10)
@Entity(tableName = "categories", indices = [Index("parentId")])
data class CategoryEntity(@PrimaryKey val id: String, val name: String, val income: Boolean = false, val parentId: String? = null, val archived: Boolean = false)
@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(@PrimaryKey val id: String, val name: String)
@Entity(tableName = "entries", indices = [Index(value = ["bookId", "deleted", "occurredAt", "id"]), Index("accountId"), Index("otherAccountId"), Index("categoryId"), Index("parentId")], foreignKeys = [ForeignKey(entity = BookEntity::class, parentColumns = ["id"], childColumns = ["bookId"]), ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"]), ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["otherAccountId"]), ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"])])
data class EntryEntity(@PrimaryKey val id: String, val bookId: String, val kind: String, val amount: Long, val currency: String,
    val accountId: String? = null, val otherAccountId: String? = null, val categoryId: String? = null,
    val merchant: String = "", val note: String = "", val occurredAt: Long,
    val parentId: String? = null, val feeMinor: Long = 0, val interestMinor: Long = 0,
    val excludedFromTotals: Boolean = false, val excludedFromBudget: Boolean = false,
    val revision: Long = 1, val deleted: Boolean = false)
@Entity(tableName = "account_entries", primaryKeys = ["entryId", "accountId"], indices = [Index("accountId")], foreignKeys = [ForeignKey(entity = EntryEntity::class, parentColumns = ["id"], childColumns = ["entryId"], onDelete = ForeignKey.CASCADE), ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"])])
data class AccountEntryEntity(val entryId: String, val accountId: String, val amount: Long)
@Entity(tableName = "entry_tags", primaryKeys = ["entryId", "tagId"], indices = [Index("tagId")], foreignKeys = [ForeignKey(entity = EntryEntity::class, parentColumns = ["id"], childColumns = ["entryId"], onDelete = ForeignKey.CASCADE), ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)])
data class EntryTagEntity(val entryId: String, val tagId: String)
@Entity(tableName = "attachments")
data class AttachmentEntity(@PrimaryKey val id: String, val name: String, val mime: String, val size: Long, val localPath: String)
@Entity(tableName = "entry_attachments", primaryKeys = ["entryId", "attachmentId"], indices = [Index("attachmentId")], foreignKeys = [ForeignKey(entity = EntryEntity::class, parentColumns = ["id"], childColumns = ["entryId"], onDelete = ForeignKey.CASCADE), ForeignKey(entity = AttachmentEntity::class, parentColumns = ["id"], childColumns = ["attachmentId"])])
data class EntryAttachmentEntity(val entryId: String, val attachmentId: String)
@Entity(tableName = "operation_receipts")
data class ReceiptEntity(@PrimaryKey val operationId: String, val fingerprint: String, val entryId: String, val revision: Long)
@Entity(tableName = "candidates", indices = [Index(value = ["strongIdentity"], unique = true), Index(value = ["pendingWindow"], unique = true), Index(value = ["state", "occurredAt"])])
data class CandidateEntity(@PrimaryKey val id: String, val packageName: String, val pageClass: String, val sessionId: String, val windowId: Int,
    val kind: String, val amount: Long, val currency: String, val merchant: String, val description: String, val paymentMethod: String,
    val occurredAt: Long, val visibleTransactionTime: Boolean, val paymentId: String?, val orderReference: String?, val identityKind: String,
    val strongIdentity: String?, val windowKey: String, val pendingWindow: String?, val requiresConfirmation: Boolean,
    val bookId: String?, val accountId: String?, val categoryId: String?, val revision: Long = 1, val state: String = "PENDING",
    val reasons: String = "", val entryId: String? = null, val fromPaymentMethod: String = "", val toPaymentMethod: String = "",
    val feeMinor: Long = 0, val discountMinor: Long = 0, val sourceRoute: String = "", val otherAccountId: String? = null,
    val templateId: String? = null, val strongKey: String? = null)
@Entity(tableName = "candidate_templates")
data class CandidateTemplateEntity(@PrimaryKey val candidateId: String, @Embedded(prefix = "template_") val template: EntryEntity, val tagIds: String = "", val attachmentIds: String = "")
@Entity(tableName = "budgets", indices = [Index("bookId")])
data class BudgetEntity(@PrimaryKey val id: String, val bookId: String, val name: String, val amount: Long, val start: Long, val endExclusive: Long, val categoryId: String?)
@Entity(tableName = "goals", indices = [Index("bookId")])
data class GoalEntity(@PrimaryKey val id: String, val bookId: String, val name: String, val target: Long, val progress: Long, val mode: String, val accountId: String?)
@Entity(tableName = "recurring_rules")
data class RecurringEntity(@PrimaryKey val id: String, @Embedded(prefix = "template_") val template: EntryEntity,
    val interval: Int, val unit: String, val nextAt: Long, val until: Long?, val maxCount: Int?, val executed: Int, val enabled: Boolean,
    val tagIds: String = "", val attachmentIds: String = "", val anchorDay: Int = 1)
@Entity(tableName = "instalments", indices = [Index(value = ["entryId", "sequence"], unique = true), Index("paymentId")])
data class InstalmentEntity(@PrimaryKey val id: String, val entryId: String, val sequence: Int, val dueAt: Long, val principal: Long, val fee: Long, val paymentId: String? = null)
@Entity(tableName = "investments", indices = [Index("bookId")])
data class InvestmentEntity(@PrimaryKey val id: String, val bookId: String, val name: String, val currency: String)
@Entity(tableName = "investment_events", indices = [Index(value = ["investmentId", "occurredAt", "id"])], foreignKeys = [ForeignKey(entity = InvestmentEntity::class, parentColumns = ["id"], childColumns = ["investmentId"], onDelete = ForeignKey.CASCADE)])
data class InvestmentEventEntity(@PrimaryKey val id: String, val investmentId: String, val kind: String, val quantity: String, val amount: Long, val occurredAt: Long)
@Entity(tableName = "merchant_rules")
data class MerchantRuleEntity(@PrimaryKey val id: String, val merchant: String, val packageName: String, val keyword: String, val categoryId: String?, val accountId: String?)
@Entity(tableName = "account_mappings", indices = [Index(value = ["packageName", "paymentMethod"], unique = true)])
data class AccountMappingEntity(@PrimaryKey val id: String, val packageName: String, val paymentMethod: String, val accountId: String)
@Entity(tableName = "period_notes", primaryKeys = ["bookId", "start", "endExclusive"])
data class PeriodNoteEntity(val bookId: String, val start: Long, val endExclusive: Long, val note: String)

data class AccountRow(@Embedded val account: AccountEntity, val balance: Long)
data class EntryRow(@Embedded val entry: EntryEntity, val categoryName: String?, val accountName: String?)
data class TotalsRow(val income: Long, val expense: Long)
data class ChartRow(val label: String, val income: Long, val expense: Long)
data class CategoryTotalRow(val categoryId: String?, val label: String, val amount: Long)
data class CreditMovementRow(val charges: Long, val repayments: Long)

/** Shared SQL expressions keep dashboard, budget and categories on one definition. */
internal object ReportSql {
    const val INCOME = "CASE WHEN e.kind='INCOME' THEN e.amount WHEN e.kind='COLLECT' THEN e.interestMinor ELSE 0 END"
    const val EXPENSE = "CASE WHEN e.kind='EXPENSE' THEN e.amount+e.feeMinor WHEN e.kind='REFUND' THEN -e.amount+e.feeMinor WHEN e.kind='REPAY' THEN e.interestMinor+e.feeMinor ELSE e.feeMinor END"
}
