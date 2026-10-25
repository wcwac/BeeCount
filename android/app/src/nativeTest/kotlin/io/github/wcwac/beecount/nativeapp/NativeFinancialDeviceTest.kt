package io.github.wcwac.beecount.nativeapp

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.wcwac.beecount.data.NativeDatabase
import io.github.wcwac.beecount.data.NativeRepository
import io.github.wcwac.beecount.domain.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Small migration of existing atomic accounting checks; each test owns an empty Room database. */
@RunWith(AndroidJUnit4::class)
class NativeFinancialDeviceTest {
    private lateinit var database: NativeDatabase
    private lateinit var repository: NativeRepository
    private lateinit var book: Book
    private lateinit var cash: Account
    private var config = CaptureConfig()

    @Before fun prepare() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, NativeDatabase::class.java).build()
        repository = NativeRepository(database, { config })
        book = repository.initialize()
        cash = Account(newId(), "验收现金")
        repository.saveAccount(cash, 10_000)
        config = CaptureConfig(true, setOf(ALIPAY), book.id)
    }
    @After fun close() { if (::database.isInitialized) database.close() }
    private suspend fun balance(id: String = cash.id) = repository.observeAccounts().first().single { it.id == id }.balance
    private suspend fun save(draft: EntryDraft): CommitReceipt {
        val result = repository.execute(FinancialCommand.Save(draft))
        assertTrue(result.toString(), result is CommitResult.Success)
        return (result as CommitResult.Success).receipt
    }
    private fun draft(amount: Long, kind: EntryKind = EntryKind.EXPENSE) = EntryDraft(bookId = book.id, kind = kind, amount = Money(amount), accountId = cash.id)
    private suspend fun summary() = repository.observeSummary(book.id, 1, Long.MAX_VALUE).first()
    private suspend fun candidate(): CaptureCandidate {
        repository.saveMapping(AccountMapping(packageName = ALIPAY, paymentMethod = "余额宝", accountId = cash.id))
        val identity = newId()
        return requireNotNull(repository.upsert(CaptureProposal(ALIPAY, "Fixture", identity, 1, EntryKind.EXPENSE, Money(3140), merchant = "实验商户", paymentMethod = "余额宝", paymentId = identity, identityKind = "PAYMENT_ID", strongKey = identity, windowKey = identity, requiresConfirmation = false)).candidate)
    }

    @Test fun expenseAndIncomeChangeBalanceAndSummary() = runBlocking {
        save(draft(1230)); save(draft(5000, EntryKind.INCOME))
        assertEquals(13_770L, balance())
        assertEquals(Summary(5000, 1230), summary())
    }

    @Test fun refundDeleteAndRestoreReverseExactlyOnePosting() = runBlocking {
        val expense = draft(2000); save(expense)
        val refund = draft(500, EntryKind.REFUND).copy(parentId = expense.id)
        val saved = save(refund)
        assertEquals(8500L, balance()); assertEquals(1500L, summary().expense)
        assertTrue(repository.execute(FinancialCommand.Delete(refund.id, saved.revision)) is CommitResult.Success)
        assertEquals(8000L, balance()); assertEquals(2000L, summary().expense)
        val deleted = requireNotNull(repository.entry(refund.id))
        assertTrue(repository.execute(FinancialCommand.Restore(refund.id, deleted.revision)) is CommitResult.Success)
        assertEquals(8500L, balance()); assertEquals(1500L, summary().expense)
    }

    @Test fun excessiveRefundLeavesNoPartialChanges() = runBlocking {
        val expense = draft(2000); save(expense)
        val invalid = draft(2001, EntryKind.REFUND).copy(parentId = expense.id)
        assertTrue(repository.execute(FinancialCommand.Save(invalid)) is CommitResult.Rejected)
        assertNull(repository.entry(invalid.id)); assertEquals(8000L, balance()); assertEquals(2000L, summary().expense)
    }

    @Test fun transferFeeUpdatesBothAccountsWithoutIncome() = runBlocking {
        val bank = Account(newId(), "验收银行", kind = AccountKind.BANK); repository.saveAccount(bank)
        save(draft(2500, EntryKind.TRANSFER).copy(otherAccountId = bank.id, feeMinor = 25))
        assertEquals(7475L, balance()); assertEquals(2500L, balance(bank.id)); assertEquals(Summary(0, 25), summary())
    }

    @Test fun candidateConfirmationIsIdempotentAcrossSaveAttempts() = runBlocking {
        val candidate = candidate(); val entry = requireNotNull(repository.candidateDraft(candidate.id))
        val command = FinancialCommand.Save(entry, candidateId = candidate.id, candidateRevision = candidate.revision)
        val first = repository.execute(command)
        assertTrue(first.toString(), first is CommitResult.Success)
        assertEquals(first, repository.execute(command))
        val retried = repository.execute(command.copy(operationId = newId()))
        assertTrue(retried.toString(), retried is CommitResult.Success)
        assertEquals((first as CommitResult.Success).receipt.entryId, (retried as CommitResult.Success).receipt.entryId)
        assertEquals(6860L, balance()); assertEquals(3140L, summary().expense)
        assertEquals(CandidateState.SAVED, repository.candidate(candidate.id)?.state)
        assertTrue(repository.observePending().first().isEmpty())
    }

    @Test fun lateCandidateFailureRollsBackAccountingAndAllowsRetry() = runBlocking {
        val candidate = candidate(); val entry = requireNotNull(repository.candidateDraft(candidate.id))
        val command = FinancialCommand.Save(entry, candidateId = candidate.id, candidateRevision = candidate.revision)
        database.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_saved_candidate BEFORE UPDATE OF state ON candidates WHEN NEW.state='SAVED' BEGIN SELECT RAISE(ABORT,'synthetic candidate state failure'); END")
        try {
            assertTrue(repository.execute(command) is CommitResult.Rejected)
            assertNull(repository.entry(entry.id)); assertEquals(10_000L, balance())
            assertEquals(CandidateState.PENDING, repository.candidate(candidate.id)?.state)
            assertEquals(Summary(), summary())
        } finally { database.openHelper.writableDatabase.execSQL("DROP TRIGGER reject_saved_candidate") }
        assertTrue(repository.execute(command) is CommitResult.Success)
        assertEquals(6860L, balance())
    }

    @Test fun loanCollectionSeparatesPrincipalAndInterest() = runBlocking {
        val receivable = Account(newId(), "验收应收", kind = AccountKind.RECEIVABLE); repository.saveAccount(receivable)
        val lending = draft(3000, EntryKind.LEND).copy(otherAccountId = receivable.id); save(lending)
        save(draft(1000, EntryKind.COLLECT).copy(parentId = lending.id, interestMinor = 50))
        assertEquals(8050L, balance()); assertEquals(2000L, balance(receivable.id)); assertEquals(Summary(50, 0), summary())
        assertEquals(2000L, repository.observeOutstanding(lending.id).first())
    }

    @Test fun instalmentPaymentDoesNotCountPrincipalTwice() = runBlocking {
        val credit = Account(newId(), "验收信用卡", kind = AccountKind.CREDIT); repository.saveAccount(credit)
        val purchase = draft(1200).copy(accountId = credit.id); save(purchase)
        repository.setInstalments(purchase.id, 3, 30, System.currentTimeMillis())
        val first = repository.observeInstalments(purchase.id).first().first()
        val operation = newId()
        assertTrue(repository.payInstalment(first.id, cash.id, operation) is CommitResult.Success)
        assertTrue(repository.payInstalment(first.id, cash.id, operation) is CommitResult.Success)
        assertEquals(9590L, balance()); assertEquals(-800L, balance(credit.id)); assertEquals(1210L, summary().expense)
    }

    private companion object { const val ALIPAY = "com.eg.android.AlipayGphone" }
}
