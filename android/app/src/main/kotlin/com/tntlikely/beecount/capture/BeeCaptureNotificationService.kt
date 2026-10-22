package com.tntlikely.beecount.capture

import android.app.Notification
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.lang.ref.WeakReference

/** Optional source, off by default; no historical notification sweep. */
class BeeCaptureNotificationService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        connected = WeakReference(this)
    }

    override fun onListenerDisconnected() {
        clearConnection()
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        clearConnection()
        super.onDestroy()
    }

    private fun clearConnection() {
        if (connected?.get() === this) connected = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val settings = CaptureSettings.read(this)
        if (!settings.notifications || !settings.accepts(sbn.packageName)) return
        CaptureDiagnostics.recordEvent(sbn.packageName, "notification")
        try { readNotification(sbn) }
        catch (_: RuntimeException) {
            CaptureDiagnostics.recordRead(sbn.packageName, 0, 0, "READ_FAILED", "notification")
        }
    }

    private fun readNotification(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val texts = buildList {
            if (extras != null) {
                for (key in listOf(Notification.EXTRA_TITLE, Notification.EXTRA_TEXT, Notification.EXTRA_BIG_TEXT)) {
                    extras.getCharSequence(key)?.toString()?.trim()?.take(1000)?.takeIf { it.isNotEmpty() }?.let(::add)
                }
                extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.take(8)?.forEach {
                    it?.toString()?.trim()?.take(300)?.takeIf(String::isNotEmpty)?.let(::add)
                }
            }
        }.distinct()
        CaptureDiagnostics.recordRead(sbn.packageName, texts.size, texts.size,
            if (texts.isEmpty()) "EMPTY" else "READABLE", "notification")
        if (texts.isEmpty()) return
        val identity = captureHash("${sbn.key}|${sbn.postTime}|${captureHash(texts.joinToString("|"))}")
        CaptureRuntime.submit(applicationContext, CaptureSnapshot(
            packageName = sbn.packageName, pageClass = "notification", windowId = -1,
            observedAt = sbn.postTime, elapsedAt = SystemClock.elapsedRealtime(),
            nodes = texts.map { CaptureNode(it) }, source = "notification", sourceEventId = identity,
        ))
    }

    companion object {
        @Volatile private var connected: WeakReference<BeeCaptureNotificationService>? = null
        fun isConnected() = connected?.get() != null
    }
}
