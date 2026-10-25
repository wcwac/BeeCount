package io.github.wcwac.beecount.nativeapp

import android.app.UiAutomation
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

/** Adapted from ReferenceShell: shell mutation is confined to the disposable CI emulator. */
internal object NativeDeviceSupport {
    fun automation(): UiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
    fun cloudOnly() {
        check(InstrumentationRegistry.getArguments().getString("native_cloud_only") == "true" && Build.VERSION.SDK_INT == 36 && Build.HARDWARE in setOf("ranchu", "goldfish") && "x86_64" in Build.SUPPORTED_ABIS) { "Fixture setup requires the disposable cloud emulator" }
    }
    fun quote(value: String) = "'" + value.replace("'", "'\\''") + "'"
    fun shell(automation: UiAutomation, command: String): String {
        cloudOnly(); require(command.length <= 16384 && '\u0000' !in command)
        val marker = "NATIVE_COMMAND_EXIT_${UUID.randomUUID().toString().replace("-", "")}:"
        val script = "exec 2>&1\n{\n$command\n} 2>&1\nnative_command_exit=\$?\nprintf '\\n${marker}%s\\n' \"\$native_command_exit\"\nexit \"\$native_command_exit\"\n"
        val channels = automation.executeShellCommandRw("/system/bin/sh")
        return ParcelFileDescriptor.AutoCloseInputStream(channels[0]).use { input ->
            ParcelFileDescriptor.AutoCloseOutputStream(channels[1]).use { it.write(script.toByteArray(Charsets.UTF_8)) }
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            while (true) { val count = input.read(buffer); if (count < 0) break; output.write(buffer, 0, count); check(output.size() <= 1_048_576) }
            val text = output.toString("UTF-8"); val boundary = text.lastIndexOf("\n$marker")
            check(boundary >= 0) { "Fixture shell did not report exit status" }
            val status = text.substring(boundary + marker.length + 1).trim()
            check(status == "0") { "Fixture command failed ($status): ${text.substring(0, boundary)}" }
            text.substring(0, boundary)
        }
    }
    fun await(message: String, timeout: Long = 15_000, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeout
        do { if (condition()) return; SystemClock.sleep(100) } while (SystemClock.uptimeMillis() < deadline)
        throw AssertionError(message)
    }
    private fun scan(automation: UiAutomation, visit: (AccessibilityNodeInfo) -> Unit) {
        val roots = automation.windows.mapNotNull { it.root }.ifEmpty { listOfNotNull(automation.rootInActiveWindow) }
        val pending = ArrayDeque<AccessibilityNodeInfo>(); pending.addAll(roots)
        var count = 0
        try {
            while (pending.isNotEmpty() && count++ < 4096) {
                val node = pending.removeFirst()
                try { visit(node); repeat(node.childCount) { node.getChild(it)?.let(pending::addLast) } }
                finally { node.recycle() }
            }
        } finally { pending.forEach { it.recycle() } }
    }
    fun hasText(automation: UiAutomation, text: String): Boolean {
        var found = false
        scan(automation) { if (it.isVisibleToUser && matches(it, text)) found = true }
        return found
    }
    private fun matches(node: AccessibilityNodeInfo, text: String): Boolean = listOfNotNull(node.text?.toString(), node.contentDescription?.toString()).any { value -> value.lineSequence().any { it.trim() == text } }
    fun clickText(automation: UiAutomation, text: String, scroll: Boolean = false) {
        repeat(if (scroll) 8 else 1) {
            var selected: Rect? = null
            scan(automation) { node ->
                if (node.isVisibleToUser && matches(node, text)) {
                    val bounds = Rect(); node.getBoundsInScreen(bounds)
                    if (!bounds.isEmpty && (selected == null || bounds.bottom > requireNotNull(selected).bottom)) selected = bounds
                }
            }
            selected?.let { bounds ->
                val down = SystemClock.uptimeMillis()
                for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP)) {
                    val event = MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, bounds.exactCenterX(), bounds.exactCenterY(), 0)
                    event.source = InputDevice.SOURCE_TOUCHSCREEN
                    try { check(automation.injectInputEvent(event, true)) } finally { event.recycle() }
                }
                return
            }
            if (scroll) { scan(automation) { node -> if (node.isScrollable && node.isVisibleToUser) node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) }; SystemClock.sleep(250) }
        }
        throw AssertionError("Cannot find visible control: $text")
    }
    fun screenshot(automation: UiAutomation, name: String) {
        val dir = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "test-evidence").apply { mkdirs() }
        val bitmap = requireNotNull(automation.takeScreenshot()) { "Screenshot unavailable" }
        try { File(dir, "$name.png").outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) } }
        finally { bitmap.recycle() }
    }
}
