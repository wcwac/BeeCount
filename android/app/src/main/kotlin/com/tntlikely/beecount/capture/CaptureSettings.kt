package com.tntlikely.beecount.capture

import android.content.Context

data class CaptureSettings(val enabled: Boolean, val packages: Set<String>, val ledgerId: Long?, val ledgerKey: String? = null,
    val notifications: Boolean = false, val consentGeneration: Long = 0) {
    fun asMap(): Map<String, Any?> = mapOf("enabled" to enabled, "packages" to packages.sorted(), "ledgerId" to ledgerId,
        "ledgerKey" to ledgerKey, "notifications" to notifications)
    fun accepts(packageName: String) = enabled && ledgerId != null && packageName in packages

    fun validate() {
        require(!enabled || (ledgerId != null && ledgerId > 0 &&
            !ledgerKey.isNullOrBlank() && ledgerKey.length <= 100))
        require(packages.all { it in CaptureApps.byPackage })
    }

    companion object {
        @Synchronized fun read(context: Context): CaptureSettings {
            val p = context.getSharedPreferences("local_capture_settings", Context.MODE_PRIVATE)
            return CaptureSettings(p.getBoolean("enabled", false),
                p.getStringSet("packages", CaptureApps.defaults)!!.intersect(CaptureApps.byPackage.keys),
                p.getLong("ledgerId", 0).takeIf { it > 0 }, p.getString("ledgerKey", null),
                p.getBoolean("notifications", false), p.getLong("consent_generation", 0))
        }
        @Synchronized fun save(context: Context, settings: CaptureSettings) {
            settings.validate()
            val previous = read(context)
            val revokesAccess = (previous.enabled && !settings.enabled) ||
                !settings.packages.containsAll(previous.packages) || (previous.notifications && !settings.notifications)
            val generation = previous.consentGeneration + if (revokesAccess) 1L else 0L
            check(context.getSharedPreferences("local_capture_settings", Context.MODE_PRIVATE).edit()
                .putBoolean("enabled", settings.enabled)
                .putStringSet("packages", settings.packages.toSet())
                .putString("ledgerKey", settings.ledgerKey)
                .putBoolean("notifications", settings.notifications)
                .putLong("consent_generation", generation)
                .putLong("ledgerId", settings.ledgerId ?: 0).commit())
        }
    }
}
