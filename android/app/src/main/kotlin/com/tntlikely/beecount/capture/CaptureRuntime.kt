package com.tntlikely.beecount.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.util.concurrent.Executors

/** The service and bridge share one serialized owner of sessions and the native inbox. */
object CaptureRuntime {
    val worker = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "bee-capture").apply { isDaemon = true } }
    private val adapters = CaptureAdapterRegistry()
    private val engine = CaptureEngine(adapters)
    @Volatile private var storageError: String? = null
    private var store: CaptureInboxStore? = null
    private data class Observation(val context: Context, val snapshot: CaptureSnapshot, val settings: CaptureSettings,
        val sessionRevision: Long)
    private data class ContentKey(val source: String, val packageName: String, val windowId: Int,
        val ledgerId: Long?, val ledgerKey: String?, val consentGeneration: Long, val sessionRevision: Long)
    // At most 31 queued snapshots plus the one currently being processed.
    private val pending = CaptureScheduler<Observation>(capacity = 31, reservedImportant = 8)
    private var drainScheduled = false
    private var sessionRevision = 0L
    @Volatile private var overflow: CaptureOverflowTracker? = null

    fun inbox(context: Context): CaptureInboxStore = store ?: CaptureInboxStore(context).also { store = it }

    /** No unbounded event queue; overload is surfaced instead of silently consuming memory. */
    @Synchronized fun submit(context: Context, snapshot: CaptureSnapshot) {
        // Freeze the target when the observation enters the pipeline, not when
        // a delayed worker eventually runs after a settings/ledger change.
        val capturedSettings = CaptureSettings.read(context)
        if (!capturedSettings.accepts(snapshot.packageName) ||
            (snapshot.source == "notification" && !capturedSettings.notifications)) return
        val appContext = context.applicationContext
        val key = ContentKey(snapshot.source, snapshot.packageName, snapshot.windowId,
            capturedSettings.ledgerId, capturedSettings.ledgerKey, capturedSettings.consentGeneration, sessionRevision)
        val outcome = pending.offer(Observation(appContext, snapshot, capturedSettings, sessionRevision), key,
            adapters.mayAffectSession(snapshot))
        if (outcome.skipped) {
            recordSkipped(appContext, outcome.importantSkipped)
        }
        if (outcome.accepted && !drainScheduled) {
            drainScheduled = true
            worker.execute { drainOne() }
        }
    }

    private fun drainOne() {
        val observation = synchronized(this) {
            pending.poll().also { if (it == null) drainScheduled = false }
        } ?: return
        var writing = false
        try {
            val (context, snapshot, capturedSettings) = observation
            val settings = CaptureSettings.read(context)
            if (settings.consentGeneration == capturedSettings.consentGeneration &&
                settings.accepts(snapshot.packageName) && (snapshot.source != "notification" || settings.notifications)) {
                val evaluation = engine.evaluate(snapshot, capturedSettings.ledgerId!!, capturedSettings.ledgerKey,
                    observation.sessionRevision, capturedSettings.consentGeneration)
                val candidate = evaluation.candidate
                if (candidate != null) {
                    writing = true
                    val saved = inbox(context).put(candidate)
                    CaptureDiagnostics.recordProcessed(snapshot.packageName, snapshot.source,
                        if (saved) "DRAFT_SAVED" else "DUPLICATE")
                    if (saved) notifyPending(context)
                    storageError = null // Successful work does not erase the loss cursor.
                } else {
                    CaptureDiagnostics.recordProcessed(snapshot.packageName, snapshot.source, when (evaluation.page) {
                        CapturePage.CHECKOUT -> "WAITING_RESULT"
                        CapturePage.FAILURE -> "PAYMENT_FAILED"
                        else -> "UNRECOGNIZED"
                    })
                }
            } else {
                CaptureDiagnostics.recordProcessed(snapshot.packageName, snapshot.source, "CONSENT_CHANGED")
            }
        } catch (error: Exception) {
            storageError = when {
                error.message == "INBOX_FULL" -> "INBOX_FULL"
                writing -> "CAPTURE_STORE_FAILED"
                else -> "CAPTURE_PROCESSING_FAILED"
            }
            CaptureDiagnostics.recordProcessed(observation.snapshot.packageName, observation.snapshot.source, "PROCESSING_FAILED")
            // A later successful write cannot prove that the failed candidate
            // was recovered. Retain that possible loss until user acknowledgement.
            recordSkipped(observation.context, important = true)
        }
        finally {
            synchronized(this) {
                // Yield between observations so status/ACK/settings work on the
                // same serial worker cannot be starved by a constant event feed.
                if (pending.isEmpty) drainScheduled = false else worker.execute { drainOne() }
            }
        }
    }

    // Access queue health under the runtime monitor, including its persistence.
    @Synchronized private fun queueHealth(context: Context): CaptureOverflowTracker {
        return overflow ?: context.getSharedPreferences("capture_queue_health", Context.MODE_PRIVATE).let { p ->
            CaptureOverflowTracker(p.getLong("total", 0), p.getLong("acknowledged", 0), p.getLong("lastImportant", 0))
                .also { overflow = it }
        }
    }

    @Synchronized internal fun healthStatus(context: Context): Map<String, Any?> {
        val state = queueHealth(context).snapshot()
        return mapOf("lastError" to (storageError ?: if (state.pending > 0) "CAPTURE_BUSY" else null),
            "skippedObservations" to state.pending, "importantObservationsSkipped" to state.includesImportant,
            "overflowCursor" to state.total)
    }

    @Synchronized fun acknowledgeOverflow(context: Context, observedCursor: Long) {
        queueHealth(context).acknowledge(observedCursor)
        persistQueueHealth(context)
    }

    @Synchronized private fun recordSkipped(context: Context, important: Boolean) {
        // Reporting failure must not throw out of a service callback if the
        // preferences themselves are unavailable. Keep the original error.
        try {
            queueHealth(context).record(important)
            persistQueueHealth(context)
        } catch (_: Exception) { storageError = "CAPTURE_STORE_FAILED" }
    }

    private fun persistQueueHealth(context: Context) {
        val state = queueHealth(context).snapshot()
        context.getSharedPreferences("capture_queue_health", Context.MODE_PRIVATE).edit()
            .putLong("total", state.total).putLong("acknowledged", state.acknowledged)
            .putLong("lastImportant", state.lastImportant).apply()
    }

    // Freeze the reset boundary on future observations instead of inserting a
    // clear job between old checkout/result snapshots already in the queue.
    @Synchronized fun reset() { sessionRevision++ }

    fun notifyPending(context: Context) {
        // Notification permission/platform failures must never turn an already
        // durable inbox write or ACK into a reported storage failure.
        try { postPendingNotification(context) } catch (_: Exception) { /* Drafts remain in the inbox. */ }
    }

    private fun postPendingNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel("capture_review", "交易采集 / Captured transactions", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse("beecount://open?page=capture")
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(context, 7201, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val count = inbox(context).count()
        if (count == 0) { manager.cancel(7201); return }
        @Suppress("DEPRECATION")
        val language = if (Build.VERSION.SDK_INT >= 24) context.resources.configuration.locales[0].language
            else context.resources.configuration.locale.language
        val zh = language == "zh"
        @Suppress("DEPRECATION")
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, "capture_review") else Notification.Builder(context)
        val notification = builder.setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle(if (zh) "有 $count 条采集记录待处理" else "$count captured records to process")
            .setContentText(if (zh) "点击核对已保存状态，检查交易后处理" else "Open to check saved status and review captured records")
            .setContentIntent(pending).setAutoCancel(true).setOnlyAlertOnce(true)
            .setVisibility(Notification.VISIBILITY_PRIVATE).build()
        manager.notify(7201, notification)
    }
}
