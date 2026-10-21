package com.tntlikely.beecount.capture

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/** Instrumentation runs with the target UID; isolate fixtures under that UID's
 * writable storage rather than using the test APK's differently owned data dir.
 * No unprefixed app database or preference file is opened by this wrapper.
 */
internal fun captureTestContext(fixtureGroup: String? = null): Context {
    require(fixtureGroup == null || fixtureGroup == "runtime_bridge")
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val test = instrumentation.context
    val target = instrumentation.targetContext
    check(target.packageName == "io.github.wcwac.beecount.offline.dev.debug")
    check(test.packageName == "${target.packageName}.test")
    fun fixtureName(name: String): String {
        require(name.isNotBlank() && '/' !in name && '\\' !in name)
        val prefix = if (fixtureGroup == null) "capture_instrumentation_"
            else "capture_instrumentation_${fixtureGroup}_"
        return "$prefix$name"
    }
    return object : ContextWrapper(test) {
        override fun getApplicationContext(): Context = this
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
            target.getSharedPreferences(fixtureName(name), mode)
        override fun getDatabasePath(name: String): File = target.getDatabasePath(fixtureName(name))
        override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase =
            target.openOrCreateDatabase(fixtureName(name), mode, factory)
        override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?,
            errorHandler: DatabaseErrorHandler?): SQLiteDatabase =
            target.openOrCreateDatabase(fixtureName(name), mode, factory, errorHandler)
        override fun deleteDatabase(name: String): Boolean = target.deleteDatabase(fixtureName(name))
    }
}
