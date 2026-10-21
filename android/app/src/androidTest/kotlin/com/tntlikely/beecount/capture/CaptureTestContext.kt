package com.tntlikely.beecount.capture

import android.content.Context
import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry

/** Keep applicationContext normalization inside the test APK's sandbox too. */
internal fun captureTestContext(): Context {
    val test = InstrumentationRegistry.getInstrumentation().context
    check(test.packageName.endsWith(".test"))
    return object : ContextWrapper(test) {
        override fun getApplicationContext(): Context = this
    }
}
