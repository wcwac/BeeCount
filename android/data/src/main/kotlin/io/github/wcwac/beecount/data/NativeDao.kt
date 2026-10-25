package io.github.wcwac.beecount.data

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface NativeDao {
    @Query("SELECT * FROM books ORDER BY archived,name") fun observeBooks(): Flow<List<BookEntity>>
    @Query("SELECT * FROM books WHERE id=:id") suspend fun book(id: String): BookEntity?
    @Query("SELECT * FROM books ORDER BY archived,name") suspend fun books(): List<BookEntity>
    @Upsert suspend fun putBook(value: BookEntity)
    @Query("SELECT a.*,COALESCE(SUM(x.amount),0) balance FROM accounts a LEFT JOIN account_entries x ON a.id=x.accountId GROUP BY a.id ORDER BY a.archived,a.name") fun observeAccounts(): Flow<List<AccountRow>>
    @Query("SELECT * FROM accounts WHERE id=:id") suspend fun account(id: String): AccountEntity?
    @Query("SELECT COALESCE(SUM(amount),0) FROM account_entries WHERE accountId=:id") suspend fun balance(id: String): Long
    @Query("SELECT COALESCE(SUM(x.amount),0) FROM account_entries x JOIN entries e ON e.id=x.entryId WHERE x.accountId=:id AND e.occurredAt<:endExclusive") suspend fun balanceBefore(id: String, endExclusive: Long): Long
    @Query("SELECT COALESCE(SUM(CASE WHEN x.amount<0 THEN -x.amount ELSE 0 END),0) charges,COALESCE(SUM(CASE WHEN x.amount>0 THEN x.amount ELSE 0 END),0) repayments FROM account_entries x JOIN entries e ON e.id=x.entryId WHERE x.accountId=:id AND e.occurredAt>=:start AND e.occurredAt<:endExclusive AND e.kind NOT IN ('OPENING','ADJUSTMENT')") suspend fun creditMovement(id: String, start: Long, endExclusive: Long): CreditMovementRow
    @Upsert suspend fun putAccount(value: AccountEntity)
    @Query("SELECT * FROM categories ORDER BY archived,income,parentId,name") fun observeCategories(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories WHERE id=:id") suspend fun category(id: String): CategoryEntity?
    @Query("SELECT * FROM categories ORDER BY archived,income,name") suspend fun categories(): List<CategoryEntity>
    @Upsert suspend fun putCategory(value: CategoryEntity)
    @Query("SELECT * FROM tags ORDER BY name") fun observeTags(): Flow<List<TagEntity>>
    @Query("SELECT * FROM tags WHERE id=:id") suspend fun tag(id: String): TagEntity?
    @Upsert suspend fun putTag(value: TagEntity)
    @Query("DELETE FROM tags WHERE id=:id") suspend fun deleteTag(id: String)
    @Query("SELECT e.*,c.name categoryName,a.name accountName FROM entries e LEFT JOIN categories c ON c.id=e.categoryId LEFT JOIN accounts a ON a.id=e.accountId WHERE e.id=:id") fun observeEntry(id: String): Flow<EntryRow?>
    @Query("SELECT e.*,c.name categoryName,a.name accountName FROM entries e LEFT JOIN categories c ON c.id=e.categoryId LEFT JOIN accounts a ON a.id=e.accountId WHERE e.id=:id") suspend fun entryRow(id: String): EntryRow?
    @Query("SELECT * FROM entries WHERE id=:id") suspend fun entry(id: String): EntryEntity?
    @Query("SELECT * FROM entries WHERE id>:after ORDER BY id LIMIT :limit") suspend fun entriesPage(after: String, limit: Int): List<EntryEntity>
    @Query("SELECT * FROM entries WHERE parentId=:id AND deleted=0") suspend fun children(id: String): List<EntryEntity>
    @Query("SELECT COUNT(*) FROM entries WHERE parentId=:id") suspend fun childCount(id: String): Int
    @Query("SELECT COUNT(*) FROM entries WHERE bookId=:id") suspend fun bookEntryCount(id: String): Int
    @Query("SELECT COUNT(*) FROM entries WHERE accountId=:id OR otherAccountId=:id") suspend fun accountEntryCount(id: String): Int
    @Query("SELECT COUNT(*) FROM entries WHERE categoryId=:id") suspend fun categoryEntryCount(id: String): Int
    @RawQuery(observedEntities = [EntryEntity::class, CategoryEntity::class, AccountEntity::class, EntryAttachmentEntity::class, EntryTagEntity::class, BudgetEntity::class]) fun history(query: SupportSQLiteQuery): PagingSource<Int, EntryRow>
    @Upsert suspend fun putEntry(value: EntryEntity)
    @Query("DELETE FROM entries WHERE id=:id") suspend fun deleteEntry(id: String)
    @Query("DELETE FROM account_entries WHERE entryId=:id") suspend fun deletePostings(id: String)
    @Insert suspend fun putPostings(values: List<AccountEntryEntity>)
    @Query("SELECT * FROM account_entries WHERE entryId=:id") suspend fun postings(id: String): List<AccountEntryEntity>
    @Query("SELECT tagId FROM entry_tags WHERE entryId=:id ORDER BY tagId") suspend fun entryTags(id: String): List<String>
    @Query("DELETE FROM entry_tags WHERE entryId=:id") suspend fun deleteEntryTags(id: String)
    @Insert suspend fun putEntryTags(values: List<EntryTagEntity>)
    @Query("SELECT attachmentId FROM entry_attachments WHERE entryId=:id ORDER BY attachmentId") suspend fun entryAttachments(id: String): List<String>
    @Query("DELETE FROM entry_attachments WHERE entryId=:id") suspend fun deleteEntryAttachments(id: String)
    @Insert suspend fun putEntryAttachments(values: List<EntryAttachmentEntity>)
    @Query("SELECT * FROM operation_receipts WHERE operationId=:id") suspend fun receipt(id: String): ReceiptEntity?
    @Query("SELECT COUNT(*) FROM operation_receipts WHERE entryId=:id") suspend fun receiptEntryCount(id: String): Int
    @Insert suspend fun putReceipt(value: ReceiptEntity)
    @Query("SELECT * FROM candidates WHERE state='PENDING' ORDER BY occurredAt DESC LIMIT 1000") fun observePending(): Flow<List<CandidateEntity>>
    @Query("SELECT * FROM candidates WHERE id=:id") suspend fun candidate(id: String): CandidateEntity?
    @Query("SELECT * FROM candidates WHERE strongIdentity=:identity LIMIT 1") suspend fun strongCandidate(identity: String): CandidateEntity?
    @Query("SELECT * FROM candidates WHERE pendingWindow=:window LIMIT 1") suspend fun windowCandidate(window: String): CandidateEntity?
    @Query("SELECT COUNT(*) FROM candidates WHERE state='PENDING'") suspend fun pendingCount(): Int
    @Upsert suspend fun putCandidate(value: CandidateEntity)
    @Query("SELECT * FROM candidate_templates WHERE candidateId=:id") suspend fun candidateTemplate(id: String): CandidateTemplateEntity?
    @Upsert suspend fun putCandidateTemplate(value: CandidateTemplateEntity)
    @Query("SELECT COALESCE(SUM(" + ReportSql.INCOME + "),0) income,COALESCE(SUM(" + ReportSql.EXPENSE + "),0) expense FROM entries e WHERE e.bookId=:bookId AND e.currency=(SELECT currency FROM books WHERE id=:bookId) AND e.deleted=0 AND e.excludedFromTotals=0 AND e.occurredAt>=:start AND e.occurredAt<:endExclusive") fun observeSummary(bookId: String, start: Long, endExclusive: Long): Flow<TotalsRow>
    @Query("SELECT COALESCE(SUM(" + ReportSql.INCOME + "),0) income,COALESCE(SUM(" + ReportSql.EXPENSE + "),0) expense FROM entries e WHERE e.bookId=:bookId AND e.currency=(SELECT currency FROM books WHERE id=:bookId) AND e.deleted=0 AND e.excludedFromTotals=0") suspend fun totals(bookId: String): TotalsRow
    @Query("SELECT strftime('%Y-%m-%d',e.occurredAt/1000,'unixepoch','localtime') label,COALESCE(SUM(" + ReportSql.INCOME + "),0) income,COALESCE(SUM(" + ReportSql.EXPENSE + "),0) expense FROM entries e WHERE e.bookId=:bookId AND e.currency=(SELECT currency FROM books WHERE id=:bookId) AND e.deleted=0 AND e.excludedFromTotals=0 AND e.occurredAt>=:start AND e.occurredAt<:endExclusive GROUP BY label ORDER BY label") fun observeChart(bookId: String, start: Long, endExclusive: Long): Flow<List<ChartRow>>
    @Query("SELECT e.categoryId categoryId,COALESCE(c.name,'其他') label,SUM(" + ReportSql.EXPENSE + ") amount FROM entries e LEFT JOIN categories c ON c.id=e.categoryId WHERE e.bookId=:bookId AND e.currency=(SELECT currency FROM books WHERE id=:bookId) AND e.deleted=0 AND e.excludedFromTotals=0 AND e.occurredAt>=:start AND e.occurredAt<:endExclusive GROUP BY e.categoryId HAVING amount<>0 ORDER BY amount DESC") fun observeCategoryTotals(bookId: String, start: Long, endExclusive: Long): Flow<List<CategoryTotalRow>>
    @Query("SELECT COALESCE(SUM(" + ReportSql.EXPENSE + "),0) FROM entries e WHERE e.bookId=:bookId AND e.currency=(SELECT currency FROM books WHERE id=:bookId) AND e.deleted=0 AND e.excludedFromBudget=0 AND e.occurredAt>=:start AND e.occurredAt<:endExclusive AND (:categoryId IS NULL OR e.categoryId=:categoryId OR e.categoryId IN (SELECT id FROM categories WHERE parentId=:categoryId))") suspend fun budgetSpent(bookId: String, start: Long, endExclusive: Long, categoryId: String?): Long
    @Query("SELECT * FROM budgets WHERE bookId=:bookId ORDER BY start DESC") fun observeBudgets(bookId: String): Flow<List<BudgetEntity>>
    @Query("SELECT * FROM budgets WHERE id=:id") suspend fun budget(id: String): BudgetEntity?
    @Upsert suspend fun putBudget(value: BudgetEntity)
    @Query("DELETE FROM budgets WHERE id=:id") suspend fun deleteBudget(id: String)
    @Query("SELECT * FROM goals WHERE bookId=:bookId ORDER BY name") fun observeGoals(bookId: String): Flow<List<GoalEntity>>
    @Upsert suspend fun putGoal(value: GoalEntity)
    @Query("DELETE FROM goals WHERE id=:id") suspend fun deleteGoal(id: String)
    @Query("SELECT * FROM recurring_rules WHERE template_bookId=:bookId ORDER BY nextAt") fun observeRecurring(bookId: String): Flow<List<RecurringEntity>>
    @Query("SELECT * FROM recurring_rules WHERE id=:id") suspend fun recurring(id: String): RecurringEntity?
    @Query("SELECT * FROM recurring_rules WHERE enabled=1 AND nextAt<=:now ORDER BY nextAt LIMIT :limit") suspend fun dueRecurring(now: Long, limit: Int): List<RecurringEntity>
    @Upsert suspend fun putRecurring(value: RecurringEntity)
    @Query("DELETE FROM recurring_rules WHERE id=:id") suspend fun deleteRecurring(id: String)
    @Query("SELECT * FROM instalments WHERE entryId=:entryId ORDER BY sequence") fun observeInstalments(entryId: String): Flow<List<InstalmentEntity>>
    @Query("SELECT * FROM instalments WHERE entryId=:entryId ORDER BY sequence") suspend fun instalments(entryId: String): List<InstalmentEntity>
    @Query("SELECT * FROM instalments WHERE id=:id") suspend fun instalment(id: String): InstalmentEntity?
    @Query("SELECT * FROM instalments WHERE paymentId=:id LIMIT 1") suspend fun instalmentForPayment(id: String): InstalmentEntity?
    @Upsert suspend fun putInstalment(value: InstalmentEntity)
    @Query("DELETE FROM instalments WHERE entryId=:id") suspend fun deleteInstalments(id: String)
    @Query("SELECT * FROM investments WHERE bookId=:bookId ORDER BY name") fun observeInvestments(bookId: String): Flow<List<InvestmentEntity>>
    @Query("SELECT * FROM investments WHERE id=:id") suspend fun investment(id: String): InvestmentEntity?
    @Upsert suspend fun putInvestment(value: InvestmentEntity)
    @Query("DELETE FROM investments WHERE id=:id") suspend fun deleteInvestment(id: String)
    @Query("SELECT * FROM investment_events WHERE investmentId=:id ORDER BY occurredAt,id") fun observeInvestmentEvents(id: String): Flow<List<InvestmentEventEntity>>
    @Query("SELECT * FROM investment_events WHERE investmentId=:id ORDER BY occurredAt,id") suspend fun investmentEvents(id: String): List<InvestmentEventEntity>
    @Query("SELECT * FROM investment_events WHERE investmentId=:id AND (occurredAt>:afterTime OR (occurredAt=:afterTime AND id>:afterId)) ORDER BY occurredAt,id LIMIT :limit") suspend fun investmentEventsPage(id: String, afterTime: Long, afterId: String, limit: Int): List<InvestmentEventEntity>
    @Query("SELECT COUNT(*) FROM investment_events WHERE investmentId=:id") suspend fun investmentEventCount(id: String): Int
    @Query("SELECT * FROM investment_events WHERE id=:id") suspend fun investmentEvent(id: String): InvestmentEventEntity?
    @Upsert suspend fun putInvestmentEvent(value: InvestmentEventEntity)
    @Query("DELETE FROM investment_events WHERE id=:id") suspend fun deleteInvestmentEvent(id: String)
    @Query("SELECT * FROM merchant_rules ORDER BY id") fun observeRules(): Flow<List<MerchantRuleEntity>>
    @Query("SELECT * FROM merchant_rules ORDER BY id") suspend fun rules(): List<MerchantRuleEntity>
    @Upsert suspend fun putRule(value: MerchantRuleEntity)
    @Query("DELETE FROM merchant_rules WHERE id=:id") suspend fun deleteRule(id: String)
    @Query("SELECT * FROM account_mappings ORDER BY packageName,paymentMethod") fun observeMappings(): Flow<List<AccountMappingEntity>>
    @Query("SELECT * FROM account_mappings ORDER BY packageName,paymentMethod") suspend fun mappings(): List<AccountMappingEntity>
    @Upsert suspend fun putMapping(value: AccountMappingEntity)
    @Query("DELETE FROM account_mappings WHERE id=:id") suspend fun deleteMapping(id: String)
    @Query("SELECT * FROM attachments ORDER BY name,id") fun pagedAttachments(): PagingSource<Int, AttachmentEntity>
    @Query("SELECT * FROM attachments WHERE id IN (:ids) ORDER BY name,id") fun observeAttachments(ids: List<String>): Flow<List<AttachmentEntity>>
    @Query("SELECT * FROM attachments WHERE id=:id") suspend fun attachment(id: String): AttachmentEntity?
    @Query("SELECT COUNT(*) FROM entry_attachments WHERE attachmentId=:id") suspend fun attachmentReferences(id: String): Int
    @Upsert suspend fun putAttachment(value: AttachmentEntity)
    @Query("DELETE FROM attachments WHERE id=:id") suspend fun deleteAttachment(id: String)
    @Query("SELECT COALESCE(MAX(revision),0)+COUNT(*) FROM entries") fun observeEntryChanges(): Flow<Long>
    @Query("SELECT COUNT(*) FROM investment_events") fun observeInvestmentChanges(): Flow<Long>
    @Query("SELECT EXISTS(SELECT 1 FROM entries LIMIT 1)+EXISTS(SELECT 1 FROM account_entries LIMIT 1)+EXISTS(SELECT 1 FROM instalments LIMIT 1)+EXISTS(SELECT 1 FROM accounts LIMIT 1)") fun observeWidgetChanges(): Flow<Int>
    @Query("SELECT note FROM period_notes WHERE bookId=:bookId AND start=:start AND endExclusive=:endExclusive") fun observePeriodNote(bookId: String, start: Long, endExclusive: Long): Flow<String?>
    @Upsert suspend fun putPeriodNote(value: PeriodNoteEntity)
}
