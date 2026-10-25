package io.github.wcwac.beecount.ui

import io.github.wcwac.beecount.domain.*
import kotlinx.coroutines.flow.StateFlow

data class PermissionSnapshot(
    val accessibilityGranted: Boolean = false,
    val overlayGranted: Boolean = false,
    val notificationsGranted: Boolean = false,
    val batteryUnrestricted: Boolean = false,
)

/** Transient editor input; only an opaque token crosses an Intent/navigation boundary. */
data class EditorDraftSnapshot(val draft: EntryDraft, val amountText: String)
data class EditorDraftHandoff(val snapshot: EditorDraftSnapshot, val candidateId: String?, val candidateRevision: Long?, val generation: Long)

/** Activity-owned system operations. UI and capture never hold each other's lifecycle. */
interface NativePlatformActions {
    val preferences: StateFlow<UserPreferences>
    val captureStatus: StateFlow<CaptureStatus>
    val permissions: StateFlow<PermissionSnapshot>
    val attachmentResults: StateFlow<Map<String, List<String>>>
    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences)
    fun refreshPermissions()
    fun openAccessibilitySettings()
    fun openOverlaySettings()
    fun requestNotifications()
    fun openBatterySettings()
    fun previewOverlay()
    fun exportBackup()
    fun restoreBackup()
    fun exportCsv(bookId: String)
    fun importCsv(bookId: String)
    fun selectBackupDirectory()
    fun backupNow()
    fun pickAttachments(draftId: String)
    fun consumeAttachmentResults(draftId: String, ids: List<String>)
    fun stageEditorDraft(snapshot: EditorDraftSnapshot, candidateId: String?, candidateRevision: Long?): String
    fun editorDraft(token: String): EditorDraftHandoff?
    fun forgetEditorDraft(token: String)
    fun openAttachment(attachment: Attachment)
    fun pinWidget(kind: String)
    fun copyDiagnostics()
    fun showQuickEntry()
}
