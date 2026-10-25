package io.github.wcwac.beecount.nativeapp

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import io.github.wcwac.beecount.domain.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

private val Context.nativeDataStore by preferencesDataStore("native_preferences")

class NativeSettings(context: Context, scope: CoroutineScope) {
    private val store = context.applicationContext.nativeDataStore
    private val currentBook = stringPreferencesKey("book")
    private val enabled = booleanPreferencesKey("capture_enabled")
    private val packages = stringSetPreferencesKey("capture_packages")
    private val targetBook = stringPreferencesKey("capture_book")
    private val overlay = booleanPreferencesKey("overlay")
    private val autoSave = booleanPreferencesKey("auto_save")
    private val theme = stringPreferencesKey("theme")
    private val hide = booleanPreferencesKey("hide_amounts")
    private val lock = booleanPreferencesKey("app_lock")
    private val secure = booleanPreferencesKey("secure_screen")
    private val tree = stringPreferencesKey("backup_tree")
    private val retention = intPreferencesKey("backup_retention")

    private fun decode(p: Preferences) = UserPreferences(
        currentBookId = p[currentBook],
        capture = CaptureConfig(p[enabled] ?: false, p[packages] ?: emptySet(), p[targetBook], p[overlay] ?: true, p[autoSave] ?: false),
        theme = p[theme] ?: "system", hideAmounts = p[hide] ?: false, appLock = p[lock] ?: false,
        secureScreen = p[secure] ?: false, backupTree = p[tree], backupRetention = p[retention] ?: 7
    )
    val preferences: StateFlow<UserPreferences> = store.data.map(::decode).stateIn(scope, SharingStarted.Eagerly, UserPreferences())
    suspend fun load(): UserPreferences = decode(store.data.first())

    suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        store.edit { p ->
            val v = transform(decode(p))
            require(v.theme in setOf("system", "light", "dark")) { "未知主题" }
            require(v.backupRetention in setOf(7, 14, 30)) { "备份保留数量无效" }
            v.currentBookId?.let { p[currentBook] = it } ?: p.remove(currentBook)
            p[enabled] = v.capture.enabled; p[packages] = v.capture.selectedPackages
            v.capture.bookId?.let { p[targetBook] = it } ?: p.remove(targetBook)
            p[overlay] = v.capture.overlayEnabled; p[autoSave] = v.capture.autoSave
            p[theme] = v.theme; p[hide] = v.hideAmounts; p[lock] = v.appLock; p[secure] = v.secureScreen
            v.backupTree?.let { p[tree] = it } ?: p.remove(tree)
            p[retention] = v.backupRetention
        }
    }
}
