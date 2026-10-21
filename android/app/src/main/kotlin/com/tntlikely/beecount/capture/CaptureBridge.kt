package com.tntlikely.beecount.capture

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class CaptureBridge(private val activity: Activity, messenger: BinaryMessenger) : MethodChannel.MethodCallHandler {
    private val channel = MethodChannel(messenger, "beecount/local_capture")
    private val main = Handler(Looper.getMainLooper())
    init { channel.setMethodCallHandler(this) }
    fun close() { channel.setMethodCallHandler(null) }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getStatus" -> background(result) {
                val health = CaptureRuntime.healthStatus(activity)
                val component = ComponentName(activity, BeeCaptureService::class.java)
                val enabled = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                    ?.split(':')?.any { ComponentName.unflattenFromString(it) == component } == true
                val notificationComponent = ComponentName(activity, BeeCaptureNotificationService::class.java)
                val notificationsGranted = Settings.Secure.getString(activity.contentResolver, "enabled_notification_listeners")
                    ?.split(':')?.any { ComponentName.unflattenFromString(it) == notificationComponent } == true
                mapOf("supported" to true, "permissionGranted" to enabled,
                    "serviceConnected" to BeeCaptureService.isConnected(),
                    "notificationsGranted" to notificationsGranted,
                    "settings" to CaptureSettings.read(activity).asMap(),
                    "apps" to CaptureApps.all.map { it.asMap() },
                    "pendingCount" to CaptureRuntime.inbox(activity).count()) + health
            }
            "updateSettings" -> {
                try {
                    val args = call.arguments as? Map<*, *> ?: error("SETTINGS_REQUIRED")
                    val packages = (args["packages"] as? List<*>)?.map { it as String }?.toSet() ?: error("PACKAGES_REQUIRED")
                    val ledger = (args["ledgerId"] as? Number)?.toLong()
                    val settings = CaptureSettings(args["enabled"] == true, packages, ledger,
                        args["ledgerKey"] as? String, args["notifications"] == true)
                    CaptureSettings.save(activity, settings)
                    BeeCaptureService.settingsChanged()
                    result.success(null)
                } catch (_: Exception) { result.error("INVALID_SETTINGS", "Choose a ledger and supported applications", null) }
            }
            "openAccessibilitySettings" -> {
                try { activity.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); result.success(null) }
                catch (_: Exception) { result.error("SETTINGS_UNAVAILABLE", "Accessibility settings unavailable", null) }
            }
            "openNotificationSettings" -> {
                try { activity.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)); result.success(null) }
                catch (_: Exception) { result.error("SETTINGS_UNAVAILABLE", "Notification access settings unavailable", null) }
            }
            "listCandidates" -> background(result) {
                CaptureRuntime.inbox(activity).pending((call.argument<Number>("limit")?.toInt() ?: 100))
            }
            "acknowledgeOverflow" -> {
                val cursor = call.argument<Number>("observedCursor")?.toLong()
                if (cursor == null || cursor < 0) {
                    result.error("INVALID_WARNING", "A warning cursor is required", null); return
                }
                background(result) { CaptureRuntime.acknowledgeOverflow(activity, cursor); null }
            }
            "ackCommitted", "dismissCandidate" -> {
                val id = call.argument<String>("id")
                val tx = call.argument<Number>("transactionId")?.toLong()
                if (id.isNullOrBlank() || id.length > 100 || (call.method == "ackCommitted" && (tx == null || tx <= 0))) {
                    result.error("INVALID_CANDIDATE", "A candidate identity is required", null); return
                }
                background(result) {
                    CaptureRuntime.inbox(activity).complete(id, if (call.method == "ackCommitted") tx else null)
                    CaptureRuntime.notifyPending(activity)
                    null
                }
            }
            else -> result.notImplemented()
        }
    }

    private fun background(result: MethodChannel.Result, action: () -> Any?) {
        CaptureRuntime.worker.execute {
            try { val value = action(); main.post { result.success(value) } }
            catch (_: Exception) { main.post { result.error("CAPTURE_UNAVAILABLE", "Local capture storage unavailable", null) } }
        }
    }
}
