package io.github.wcwac.beecount.capture

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object CapturePermissions {
    fun accessibilityGranted(context: Context): Boolean {
        val expected = ComponentName(context, NativeCaptureService::class.java)
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.split(':')?.any { ComponentName.unflattenFromString(it) == expected } == true
    }
    fun connected(): Boolean = NativeCaptureService.isConnected()
    fun overlayGranted(context: Context): Boolean = Settings.canDrawOverlays(context)
    fun accessibilityIntent(): Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    fun overlayIntent(context: Context): Intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
}
