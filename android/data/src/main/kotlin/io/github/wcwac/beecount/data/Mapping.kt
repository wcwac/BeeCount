package io.github.wcwac.beecount.data

import io.github.wcwac.beecount.domain.*

internal fun BookEntity.domain() = Book(id, name, currency, archived)
internal fun AccountRow.domain() = Account(account.id, account.name, account.currency, AccountKind.valueOf(account.kind), balance, account.archived, account.statementDay, account.repaymentDay)
internal fun CategoryEntity.domain() = Category(id, name, income, parentId, archived)
internal fun EntryDraft.entity(revision: Long = 1, deleted: Boolean = false) = EntryEntity(id, bookId, kind.name, amount.minor, amount.currency.value, accountId, otherAccountId, categoryId, merchant, note, occurredAt, parentId, feeMinor, interestMinor, excludedFromTotals, excludedFromBudget, revision, deleted)
internal fun EntryEntity.draft(tags: List<String> = emptyList(), attachments: List<String> = emptyList()) = EntryDraft(id, bookId, EntryKind.valueOf(kind), Money(amount, CurrencyCode(currency)), accountId, otherAccountId, categoryId, merchant, note, occurredAt, parentId, feeMinor, interestMinor, excludedFromTotals, excludedFromBudget, tags, attachments)
// Validated IDs cannot contain '|'; sorting makes relation order independent of SQLite's plan.
internal fun EntryRow.domain(): LedgerEntry = LedgerEntry(entry.draft(splitIds(tagIds).sorted(), splitIds(attachmentIds).sorted()), entry.revision, entry.deleted, categoryName.orEmpty(), accountName.orEmpty())
internal fun CandidateEntity.domain(): CaptureCandidate = CaptureCandidate(id, CaptureProposal(packageName, pageClass, sessionId, windowId, EntryKind.valueOf(kind), Money(amount, CurrencyCode(currency)), merchant, description, paymentMethod, occurredAt, visibleTransactionTime, paymentId, orderReference, identityKind, strongKey, windowKey, requiresConfirmation, fromPaymentMethod, toPaymentMethod, feeMinor, discountMinor, sourceRoute), bookId, accountId, categoryId, revision, CandidateState.valueOf(state), reasons.split('|').filter { it.isNotEmpty() }.map { ConfirmationReason.valueOf(it) }.toSet(), entryId, otherAccountId)
internal fun InstalmentEntity.domain() = Instalment(id, entryId, sequence, dueAt, principal, fee, paymentId)
internal fun MerchantRuleEntity.domain() = MerchantRule(id, merchant, packageName, keyword, categoryId, accountId)
internal fun AccountMappingEntity.domain() = AccountMapping(id, packageName, paymentMethod, accountId)
internal fun AttachmentEntity.domain() = Attachment(id, name, mime, size, localPath)
internal fun RecurringEntity.domain() = RecurringRule(id, template.draft(splitIds(tagIds), splitIds(attachmentIds)), interval, unit, nextAt, until, maxCount, executed, enabled)
internal fun InvestmentEventEntity.domain() = InvestmentEvent(id, investmentId, kind, quantity, amount, occurredAt)
internal fun splitIds(value: String) = value.split('|').filter { it.isNotEmpty() }
