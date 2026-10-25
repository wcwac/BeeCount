package io.github.wcwac.beecount.nativeapp

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

object NativeWork {
    fun schedule(context: Context) {
        val manager = WorkManager.getInstance(context)
        manager.enqueueUniquePeriodicWork("native-recurring", ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<RecurringWorker>(1, TimeUnit.HOURS).setInitialDelay(1, TimeUnit.HOURS).build())
        manager.enqueueUniquePeriodicWork("native-backup", ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).setRequiresStorageNotLow(true).build())
                .setInitialDelay(1, TimeUnit.DAYS).build())
    }
}

class RecurringWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        return try {
            val container = NativeApplication.container(applicationContext)
            container.initialize()
            container.repository.generateRecurringCandidates()
            NativeWidget.updateAll(applicationContext)
            Result.success()
        } catch (failure: Exception) {
            if (failure is CancellationException) throw failure
            if (runAttemptCount < 3) Result.retry() else Result.failure(workDataOf("error" to failure.javaClass.simpleName))
        }
    }
}

class BackupWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        return try {
            val container = NativeApplication.container(applicationContext)
            container.initialize()
            if (container.settings.load().backupTree != null) container.files.backupNow()
            Result.success()
        } catch (failure: Exception) {
            if (failure is CancellationException) throw failure
            if (failure is SecurityException || failure is IllegalArgumentException || runAttemptCount >= 3) Result.failure(workDataOf("error" to failure.javaClass.simpleName)) else Result.retry()
        }
    }
}
