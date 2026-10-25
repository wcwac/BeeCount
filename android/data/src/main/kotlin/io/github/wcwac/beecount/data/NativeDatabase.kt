package io.github.wcwac.beecount.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BookEntity::class, AccountEntity::class, CategoryEntity::class, TagEntity::class,
    EntryEntity::class, AccountEntryEntity::class, EntryTagEntity::class, AttachmentEntity::class, EntryAttachmentEntity::class,
    ReceiptEntity::class, CandidateEntity::class, CandidateTemplateEntity::class, BudgetEntity::class, GoalEntity::class, RecurringEntity::class,
    InstalmentEntity::class, InvestmentEntity::class, InvestmentEventEntity::class, MerchantRuleEntity::class, AccountMappingEntity::class, PeriodNoteEntity::class], version = 1, exportSchema = true)
abstract class NativeDatabase : RoomDatabase() {
    abstract fun dao(): NativeDao
    companion object {
        fun open(context: Context): NativeDatabase = Room.databaseBuilder(context.applicationContext, NativeDatabase::class.java, "beecount_native.sqlite")
            .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING).build()
    }
}
