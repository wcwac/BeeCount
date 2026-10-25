package io.github.wcwac.beecount.nativeapp

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeScreenDeviceTest {
    @Test fun mainFourTabsRenderAndProduceScreenshots() {
        val automation = NativeDeviceSupport.automation()
        ActivityScenario.launch(MainActivity::class.java).use {
            NativeDeviceSupport.await("Main screen did not open") { NativeDeviceSupport.hasText(automation, "更多") }
            listOf(Triple("账单", "ledger", "全部流水"), Triple("资产", "assets", "净资产 · CNY"), Triple("统计", "statistics", "近7天"), Triple("更多", "more", "账务管理")).forEach { (label, name, expected) ->
                NativeDeviceSupport.clickText(automation, label)
                NativeDeviceSupport.await("Tab $label did not render content") { NativeDeviceSupport.hasText(automation, expected) }
                NativeDeviceSupport.screenshot(automation, "main-$name")
            }
        }
    }

    @Test fun editorAndCaptureSettingsRenderWithoutOriginalBitmapBackgrounds() {
        val instrumentation = InstrumentationRegistry.getInstrumentation(); val automation = NativeDeviceSupport.automation()
        ActivityScenario.launch<MainActivity>(Intent(instrumentation.targetContext, MainActivity::class.java).putExtra("route", "editor")).use {
            NativeDeviceSupport.await("Editor did not open") { NativeDeviceSupport.hasText(automation, "类型：支出") }
            NativeDeviceSupport.screenshot(automation, "entry-editor")
        }
        ActivityScenario.launch<MainActivity>(Intent(instrumentation.targetContext, MainActivity::class.java).putExtra("route", "capture")).use {
            NativeDeviceSupport.await("Capture settings did not open") { NativeDeviceSupport.hasText(automation, "无障碍授权") }
            NativeDeviceSupport.screenshot(automation, "capture-settings")
        }
    }
}
