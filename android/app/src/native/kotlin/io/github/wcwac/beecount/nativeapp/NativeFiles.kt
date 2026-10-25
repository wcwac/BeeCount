package io.github.wcwac.beecount.nativeapp

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import androidx.documentfile.provider.DocumentFile
import androidx.room.Room
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.wcwac.beecount.capture.NativeCaptureService
import io.github.wcwac.beecount.data.NativeDatabase
import io.github.wcwac.beecount.data.NativeRepository
import io.github.wcwac.beecount.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.time.Instant
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/** All archive input is data. No serialized objects, executable resources or old APK formats. */
class NativeFiles(private val container: NativeContainer) {
    private val context = container.context
    private val repo get() = container.repository
    private val attachmentRoot get() = File(context.filesDir, "attachments").apply { mkdirs() }
    private val marker get() = File(context.filesDir, "restore.marker")

    fun hasPendingRecovery(): Boolean = marker.exists()

    data class BackupPreferences(val theme: String, val hideAmounts: Boolean, val secureScreen: Boolean, val currentBookId: String?, val captureBookId: String?, val selectedPackages: Set<String>, val overlayEnabled: Boolean, val backupRetention: Int)
    data class RestorePackage(val id: String, val databaseName: String, val attachmentDirectory: File, val rows: Long, val preferences: BackupPreferences? = null)
    data class CsvPreview(val bookId: String, val entries: List<EntryDraft>)

    suspend fun exportBackup(uri: Uri) = withContext(Dispatchers.IO) {
        requireNotNull(context.contentResolver.openOutputStream(uri, "wt")) { "无法写入备份文件" }.use { output ->
            val preferences = container.settings.load()
            repo.maintenanceMutex.withLock { exportLocked(output, preferences) }
        }
    }

    private fun exportLocked(output: OutputStream, preferences: UserPreferences) {
        val db = container.database.openHelper.readableDatabase
        val budget = ExpansionBudget()
        var rows = 0L
        ZipOutputStream(BufferedOutputStream(output)).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            BoundedOutput(zip, 65536, budget).write(JSONObject().put("format", "BeeCountNativeBackup").put("version", 1)
                .put("createdAt", System.currentTimeMillis()).put("preferences", backupPreferencesJson(preferences)).put("tables", org.json.JSONArray(TABLES)).toString().toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            for (table in TABLES) {
                zip.putNextEntry(ZipEntry("tables/$table.json"))
                val writer = JsonWriter(OutputStreamWriter(BoundedOutput(zip, MAX_TABLE_BYTES, budget), Charsets.UTF_8))
                writer.beginArray()
                db.query("SELECT * FROM `$table`").use { cursor ->
                    writer.beginArray(); cursor.columnNames.forEach(writer::value); writer.endArray()
                    while (cursor.moveToNext()) {
                        require(++rows <= 1_000_000) { "备份记录超过当前格式上限" }
                        writer.beginArray()
                        for (column in 0 until cursor.columnCount) {
                            when {
                                table == "attachments" && cursor.getColumnName(column) == "localPath" -> writer.value(cursor.getString(cursor.getColumnIndexOrThrow("id")))
                                cursor.isNull(column) -> writer.nullValue()
                                cursor.getType(column) == Cursor.FIELD_TYPE_INTEGER -> writer.value(cursor.getLong(column))
                                cursor.getType(column) == Cursor.FIELD_TYPE_STRING -> writer.value(cursor.getString(column))
                                else -> error("不支持的备份字段类型：$table")
                            }
                        }
                        writer.endArray()
                    }
                }
                writer.endArray(); writer.flush(); zip.closeEntry()
            }
            db.query("SELECT id,localPath,size FROM attachments").use { cursor ->
                var count = 0
                while (cursor.moveToNext()) {
                    require(++count <= MAX_ATTACHMENTS) { "备份附件超过一万个" }
                    val id = checkedId(cursor.getString(0))
                    val file = checkedAttachment(cursor.getString(1))
                    require(file.isFile && file.length() == cursor.getLong(2) && file.length() <= MAX_ATTACHMENT_BYTES) { "附件缺失或大小不一致：$id" }
                    zip.putNextEntry(ZipEntry("attachments/$id")); file.inputStream().use { it.copyTo(BoundedOutput(zip, MAX_ATTACHMENT_BYTES, budget)) }; zip.closeEntry()
                }
            }
        }
    }

    suspend fun prepareRestore(uri: Uri): RestorePackage = withContext(Dispatchers.IO) {
        val id = newId()
        val source = File(context.cacheDir, "restore-$id.zip")
        val directory = File(attachmentRoot, "restore-$id").apply { mkdirs() }
        val databaseName = "native_restore_$id.sqlite"
        val staged = Room.databaseBuilder(context, NativeDatabase::class.java, databaseName).build()
        try {
            requireNotNull(context.contentResolver.openInputStream(uri)) { "无法读取备份" }.use { input -> source.outputStream().use { copyBounded(input, it, MAX_ARCHIVE_BYTES) } }
            var rows = 0L
            var preferences: BackupPreferences? = null
            ZipFile(source).use { zip ->
                val names = mutableSetOf<String>()
                var expanded = 0L
                val actualBudget = ExpansionBudget()
                val iterator = zip.entries()
                while (iterator.hasMoreElements()) {
                    val entry = iterator.nextElement()
                    require(!entry.isDirectory && names.add(entry.name)) { "备份存在重复条目" }
                    require(names.size <= TABLES.size + MAX_ATTACHMENTS + 1) { "备份文件条目过多" }
                    require(entry.size in 0..MAX_ARCHIVE_BYTES) { "备份条目大小无效" }
                    expanded = Math.addExact(expanded, entry.size)
                    require(expanded <= MAX_ARCHIVE_BYTES) { "备份解压后超过 512 MiB" }
                    require(entry.name == "manifest.json" || TABLES.any { entry.name == "tables/$it.json" } ||
                        (entry.name.startsWith("attachments/") && runCatching { checkedId(entry.name.removePrefix("attachments/")); true }.getOrDefault(false))) { "备份包含未知文件" }
                }
                val manifestEntry = requireNotNull(zip.getEntry("manifest.json")) { "缺少备份说明" }
                require(manifestEntry.size <= 65536) { "备份说明过大" }
                val manifest = JSONObject(BoundedInput(zip.getInputStream(manifestEntry), 65536, actualBudget, manifestEntry.crc).bufferedReader(Charsets.UTF_8).use { it.readText() })
                require(manifest.optString("format") == "BeeCountNativeBackup" && manifest.optInt("version") == 1) { "仅支持本原生版的完整备份" }
                preferences = parseBackupPreferences(manifest.optJSONObject("preferences"))
                val manifestTables = manifest.getJSONArray("tables")
                require((0 until manifestTables.length()).map { manifestTables.getString(it) } == TABLES) { "备份表清单不完整" }
                val stageDb = staged.openHelper.writableDatabase
                staged.withTransaction {
                    for (table in TABLES) {
                        val entry = requireNotNull(zip.getEntry("tables/$table.json")) { "备份缺少 $table" }
                        require(entry.size <= MAX_TABLE_BYTES) { "数据表超过单表大小限制" }
                        BoundedInput(zip.getInputStream(entry), MAX_TABLE_BYTES, actualBudget, entry.crc).bufferedReader(Charsets.UTF_8).use { text ->
                            val reader = JsonReader(JsonSafetyReader(text))
                            reader.beginArray(); reader.beginArray()
                            val columns = mutableListOf<String>()
                            while (reader.hasNext()) { require(columns.size < 100); columns.add(reader.nextString()) }
                            reader.endArray()
                            val schema = tableSchema(stageDb, table)
                            require(columns == schema.map { it.name }) { "数据表结构不匹配：$table" }
                            val sql = "INSERT INTO `$table` (${columns.joinToString { "`$it`" }}) VALUES (${columns.joinToString { "?" }})"
                            stageDb.compileStatement(sql).use { statement ->
                                while (reader.hasNext()) {
                                    require(++rows <= 1_000_000) { "备份记录过多" }
                                    statement.clearBindings(); reader.beginArray()
                                    val values = ArrayList<Any?>(columns.size)
                                    for (column in columns.indices) {
                                        require(reader.hasNext()) { "备份行缺少字段" }
                                        values += when (reader.peek()) {
                                            JsonToken.NULL -> { reader.nextNull(); null }
                                            JsonToken.STRING -> reader.nextString().also { require(it.length <= 1_048_576) { "备份字段过长" } }
                                            JsonToken.NUMBER -> reader.nextString().toLongOrNull() ?: error("备份数字超出范围")
                                            else -> error("备份字段类型无效")
                                        }
                                    }
                                    require(!reader.hasNext()) { "备份行字段过多" }; reader.endArray()
                                    values.forEachIndexed { column, value ->
                                        val definition = schema[column]
                                        require(if (value == null) !definition.required else when (definition.type) { "INTEGER" -> value is Long; "TEXT" -> value is String; else -> false }) { "备份字段类型与结构不符：$table.${definition.name}" }
                                    }
                                    if (table == "attachments") {
                                        val attachmentId = checkedId(values[columns.indexOf("id")] as String)
                                        values[columns.indexOf("localPath")] = File(directory, attachmentId).absolutePath
                                    }
                                    values.forEachIndexed { index, value -> when (value) {
                                        null -> statement.bindNull(index + 1)
                                        is Long -> statement.bindLong(index + 1, value)
                                        is String -> statement.bindString(index + 1, value)
                                        else -> error("字段类型无效")
                                    } }
                                    statement.executeInsert()
                                }
                            }
                            reader.endArray(); require(reader.peek() == JsonToken.END_DOCUMENT) { "备份数据后存在多余内容" }
                        }
                    }
                }
                stageDb.query("SELECT id,size FROM attachments").use { cursor ->
                    var expectedAttachments = 0
                    while (cursor.moveToNext()) {
                        expectedAttachments++
                        val attachmentId = checkedId(cursor.getString(0)); val size = cursor.getLong(1)
                        require(size in 0..MAX_ATTACHMENT_BYTES) { "附件超过 16 MiB" }
                        val entry = requireNotNull(zip.getEntry("attachments/$attachmentId")) { "备份缺少附件" }
                        require(entry.size == size) { "附件大小与记录不一致" }
                        BoundedInput(zip.getInputStream(entry), MAX_ATTACHMENT_BYTES, actualBudget, entry.crc).use { input -> File(directory, attachmentId).outputStream().use { output -> require(copyBounded(input, output, MAX_ATTACHMENT_BYTES) == size) } }
                    }
                    require(names.count { it.startsWith("attachments/") } == expectedAttachments) { "备份含未关联的附件文件" }
                }
                NativeRepository(staged).validateForRestore()
            }
            RestorePackage(id, databaseName, directory, rows, preferences)
        } catch (failure: Throwable) {
            staged.close(); context.deleteDatabase(databaseName); directory.deleteRecursively(); throw failure
        } finally { staged.close(); source.delete() }
    }

    suspend fun applyRestore(prepared: RestorePackage) = withContext(Dispatchers.IO) {
        container.withRestoreGate {
        checkedId(prepared.id)
        require(prepared.databaseName == "native_restore_${prepared.id}.sqlite" && prepared.attachmentDirectory.canonicalFile == File(attachmentRoot, "restore-${prepared.id}").canonicalFile) { "恢复准备信息无效" }
        require(context.getDatabasePath(prepared.databaseName).isFile && prepared.attachmentDirectory.isDirectory) { "恢复准备已失效，请重新选择备份" }
        val before = container.settings.load()
        var staged: NativeDatabase? = null
        var committed = false
        try {
            container.updatePreferences { it.copy(capture = it.capture.copy(enabled = false, autoSave = false)) }
            container.invalidateForRestore()
            NativeCaptureService.invalidateAndDrain()
            val sourceDatabase = Room.databaseBuilder(context, NativeDatabase::class.java, prepared.databaseName).build().also { staged = it }
            repo.maintenanceMutex.withLock {
                val markerBody = JSONObject().put("id", prepared.id).put("database", prepared.databaseName).put("attachments", prepared.attachmentDirectory.absolutePath).put("captureEnabled", before.capture.enabled).put("preferences", prepared.preferences?.let(::backupPreferencesJson))
                atomicWrite(marker, markerBody.toString())
                val source = sourceDatabase.openHelper.readableDatabase
                source.query("SELECT COUNT(*) FROM books WHERE archived=0").use { require(it.moveToFirst() && it.getLong(0) > 0) { "恢复准备数据不完整" } }
                container.database.withTransaction {
                    val target = container.database.openHelper.writableDatabase
                    TABLES.asReversed().forEach { target.execSQL("DELETE FROM `$it`") }
                    for (table in TABLES) {
                        source.query("SELECT * FROM `$table`").use { cursor ->
                            val names = cursor.columnNames
                            target.compileStatement("INSERT INTO `$table` (${names.joinToString { "`$it`" }}) VALUES (${names.joinToString { "?" }})").use { statement ->
                                while (cursor.moveToNext()) {
                                    statement.clearBindings()
                                    for (i in names.indices) when (cursor.getType(i)) {
                                        Cursor.FIELD_TYPE_NULL -> statement.bindNull(i + 1)
                                        Cursor.FIELD_TYPE_INTEGER -> statement.bindLong(i + 1, cursor.getLong(i))
                                        Cursor.FIELD_TYPE_STRING -> statement.bindString(i + 1, cursor.getString(i))
                                        else -> error("恢复字段类型无效")
                                    }
                                    statement.executeInsert()
                                }
                            }
                        }
                    }
                    target.execSQL("INSERT INTO operation_receipts(operationId,fingerprint,entryId,revision) VALUES(?,?,?,?)", arrayOf(prepared.id, restoreFingerprint(prepared.id), "restore:${prepared.id}", 1L))
                }
                committed = true
            }
            val book = repo.observeBooks().first().firstOrNull { !it.archived } ?: error("备份没有可用账本")
            applyBackupPreferences(prepared.preferences, book.id, before.capture.enabled)
            marker.delete()
        } catch (failure: Throwable) {
            committed = committed || withContext(NonCancellable + Dispatchers.IO) { restoreCommitted(prepared.id) }
            if (!committed) throw failure
            // The ledger is already committed: finish preferences in this process instead of
            // reporting a rolled-back import or waiting for a cold application restart.
            withContext(NonCancellable + Dispatchers.IO) { recoverInterruptedRestore() }
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                try {
                    // Cancellation can arrive after SQLite commits but before withTransaction returns.
                    committed = committed || restoreCommitted(prepared.id)
                    if (committed && marker.exists()) recoverInterruptedRestore()
                    staged?.close(); context.deleteDatabase(prepared.databaseName)
                    if (!committed) {
                        prepared.attachmentDirectory.deleteRecursively()
                        container.updatePreferences { it.copy(currentBookId = before.currentBookId, capture = before.capture) }
                        marker.delete()
                    }
                } finally { NativeCaptureService.resumeAfterRestore() }
            }
        }
        }
    }

    suspend fun recoverInterruptedRestore() = withContext(Dispatchers.IO) {
        if (!marker.exists()) return@withContext
        require(marker.length() <= 65536) { "恢复状态文件损坏" }
        val state = JSONObject(marker.readText())
        val id = checkedId(state.getString("id"))
        val finished = restoreCommitted(id)
        val directory = checkedAttachment(state.getString("attachments"))
        require(directory == File(attachmentRoot, "restore-$id").canonicalFile) { "恢复附件目录无效" }
        if (!finished) directory.deleteRecursively()
        require(state.getString("database") == "native_restore_$id.sqlite")
        context.deleteDatabase(state.getString("database"))
        val book = container.database.openHelper.readableDatabase.query("SELECT id FROM books WHERE archived=0 ORDER BY name,id LIMIT 1").use { if (it.moveToFirst()) it.getString(0) else null }
        if (finished) applyBackupPreferences(parseBackupPreferences(state.optJSONObject("preferences")), requireNotNull(book), state.optBoolean("captureEnabled", false))
        else container.settings.update { it.copy(capture = it.capture.copy(enabled = state.optBoolean("captureEnabled", it.capture.enabled), autoSave = false)) }
        marker.delete()
    }

    suspend fun discardRestore(prepared: RestorePackage) = withContext(Dispatchers.IO) { context.deleteDatabase(prepared.databaseName); prepared.attachmentDirectory.deleteRecursively(); Unit }

    suspend fun importAttachment(uri: Uri): Attachment = withContext(Dispatchers.IO) {
        var name = "附件"
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) name = it.getString(0).take(200) }
        val id = newId(); val file = File(attachmentRoot, id)
        try {
            val size = requireNotNull(context.contentResolver.openInputStream(uri)) { "无法读取附件" }.use { input -> file.outputStream().use { output -> copyBounded(input, output, MAX_ATTACHMENT_BYTES) } }
            val attachment = Attachment(id, name, context.contentResolver.getType(uri) ?: "application/octet-stream", size, file.absolutePath)
            repo.saveAttachment(attachment); attachment
        } catch (failure: Throwable) {
            // A cancellation may arrive after Room committed the metadata. Preserve
            // the file whenever commitment is confirmed or cannot be determined.
            withContext(NonCancellable + Dispatchers.IO) {
                val referenced = runCatching { repo.dao.attachment(id) != null }.getOrDefault(true)
                if (!referenced) file.delete()
            }
            throw failure
        }
    }

    suspend fun backupNow(): String = withContext(Dispatchers.IO) {
        val preferences = container.settings.load()
        val uri = requireNotNull(preferences.backupTree) { "请先选择自动备份文件夹" }
        val folder = requireNotNull(DocumentFile.fromTreeUri(context, Uri.parse(uri))) { "备份目录不可用" }
        require(folder.canWrite()) { "备份目录写入权限已失效，请重新选择" }
        val name = "beecount-native-${System.currentTimeMillis()}.zip"
        val target = requireNotNull(folder.createFile("application/zip", name)) { "无法创建备份" }
        try { exportBackup(target.uri) } catch (failure: Throwable) { target.delete(); throw failure }
        folder.listFiles().filter { it.isFile && it.name?.matches(Regex("beecount-native-[0-9]+\\.zip")) == true }
            .sortedByDescending { it.name }.drop(preferences.backupRetention).forEach { it.delete() }
        name
    }

    suspend fun exportCsv(bookId: String, uri: Uri) = withContext(Dispatchers.IO) {
        repo.maintenanceMutex.withLock {
            requireNotNull(context.contentResolver.openOutputStream(uri, "wt")).bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(CSV_HEADERS.joinToString(",")); writer.newLine()
                container.database.openHelper.readableDatabase.query("SELECT e.*,a.name accountName,c.name categoryName FROM entries e LEFT JOIN accounts a ON a.id=e.accountId LEFT JOIN categories c ON c.id=e.categoryId WHERE e.bookId=? AND e.deleted=0 AND e.kind IN ('EXPENSE','INCOME') ORDER BY e.occurredAt,e.id", arrayOf(bookId)).use { cursor ->
                    fun text(name: String): String = cursor.getColumnIndexOrThrow(name).let { if (cursor.isNull(it)) "" else cursor.getString(it) }
                    while (cursor.moveToNext()) {
                        val amount = Money(text("amount").toLong(), CurrencyCode(text("currency"))).decimal()
                        val fee = Money(text("feeMinor").toLong(), CurrencyCode(text("currency"))).decimal()
                        val row = listOf(text("kind"), amount, text("currency"), Instant.ofEpochMilli(text("occurredAt").toLong()).toString(), text("accountName"), text("categoryName"), text("merchant"), text("note"), text("excludedFromTotals"), text("excludedFromBudget"), fee)
                        writer.write(row.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }); writer.newLine()
                    }
                }
            }
        }
    }

    suspend fun previewCsv(bookId: String, uri: Uri): CsvPreview = withContext(Dispatchers.IO) {
        val book = repo.observeBooks().first().firstOrNull { it.id == bookId && !it.archived } ?: error("账本不可用")
        val accounts = repo.observeAccounts().first().filter { !it.archived }
        val categories = repo.observeCategories().first().filter { !it.archived }
        val drafts = mutableListOf<EntryDraft>()
        requireNotNull(context.contentResolver.openInputStream(uri)).bufferedReader(Charsets.UTF_8).use { reader ->
            val rows = csvRows(reader).iterator()
            require(rows.hasNext() && rows.next().map { it.removePrefix("\uFEFF") } == CSV_HEADERS) { "CSV 表头不匹配，请使用本原生版导出的格式" }
            while (rows.hasNext()) {
                val row = rows.next(); if (row.all { it.isBlank() }) continue
                require(drafts.size < 2000) { "每次最多导入 2000 条，请拆分文件" }
                require(row.size == CSV_HEADERS.size) { "第 ${drafts.size + 2} 行字段数量错误" }
                val kind = EntryKind.valueOf(row[0]); require(kind == EntryKind.EXPENSE || kind == EntryKind.INCOME) { "CSV 仅导入普通收支，完整财务关系请使用 ZIP" }
                val currency = CurrencyCode(row[2])
                val amount = Money.parse(row[1], currency); require(amount.minor > 0)
                val fee = Money.parse(row[10], currency); require(fee.minor >= 0) { "手续费不能为负" }
                val account = if (row[4].isBlank()) null else accounts.filter { it.name == row[4] && it.currency == currency.value }.singleOrNull() ?: error("账户不存在或重名：${row[4]}")
                val category = if (row[5].isBlank()) null else categories.filter { it.name == row[5] && it.income == (kind == EntryKind.INCOME) }.singleOrNull() ?: error("分类不存在或重名：${row[5]}")
                require(row[8] in listOf("0", "1") && row[9] in listOf("0", "1")) { "统计标记必须为 0 或 1" }
                drafts += EntryDraft(bookId = bookId, kind = kind, amount = amount, accountId = account?.id, categoryId = category?.id,
                    merchant = row[6], note = row[7], occurredAt = Instant.parse(row[3]).toEpochMilli(), excludedFromTotals = row[8] == "1", excludedFromBudget = row[9] == "1", feeMinor = fee.minor)
            }
        }
        require(drafts.isNotEmpty()) { "CSV 没有可导入记录" }
        repo.validateImportEntries(drafts)
        CsvPreview(bookId, drafts)
    }

    suspend fun importCsv(preview: CsvPreview): Int = repo.importEntries(preview.entries)

    private fun csvRows(reader: Reader): Sequence<List<String>> = sequence {
        val input = PushbackReader(reader, 1); var row = mutableListOf<String>(); val field = StringBuilder()
        var quoted = false; var closedQuote = false; var total = 0
        while (true) {
            val number = input.read(); if (number == -1) break
            require(++total <= 8 * 1024 * 1024) { "CSV 超过 8 MiB" }
            val ch = number.toChar()
            if (quoted) {
                if (ch == '"') { val next = input.read(); if (next == '"'.code) field.append('"') else { quoted = false; closedQuote = true; if (next != -1) input.unread(next) } }
                else field.append(ch)
            } else when (ch) {
                '"' -> { require(field.isEmpty() && !closedQuote) { "CSV 引号格式错误" }; quoted = true }
                ',' -> { row.add(field.toString()); field.setLength(0); closedQuote = false; require(row.size <= 20) }
                '\n', '\r' -> {
                    if (ch == '\r') { val next = input.read(); if (next != '\n'.code && next != -1) input.unread(next) }
                    row.add(field.toString()); yield(row); row = mutableListOf(); field.setLength(0); closedQuote = false
                }
                else -> { require(!closedQuote) { "CSV 引号后存在多余内容" }; field.append(ch) }
            }
            require(field.length <= 16384) { "CSV 字段过长" }
        }
        require(!quoted) { "CSV 引号未闭合" }
        if (field.isNotEmpty() || row.isNotEmpty() || closedQuote) { row.add(field.toString()); yield(row) }
    }

    private fun backupPreferencesJson(value: UserPreferences) = backupPreferencesJson(BackupPreferences(value.theme, value.hideAmounts, value.secureScreen, value.currentBookId, value.capture.bookId, value.capture.selectedPackages, value.capture.overlayEnabled, value.backupRetention))
    private fun backupPreferencesJson(value: BackupPreferences): JSONObject = JSONObject()
        .put("theme", value.theme).put("hideAmounts", value.hideAmounts).put("secureScreen", value.secureScreen)
        .put("currentBookId", value.currentBookId).put("captureBookId", value.captureBookId)
        .put("selectedPackages", org.json.JSONArray(value.selectedPackages.sorted())).put("overlayEnabled", value.overlayEnabled).put("backupRetention", value.backupRetention)
    private fun parseBackupPreferences(json: JSONObject?): BackupPreferences? {
        if (json == null) return null
        val theme = json.getString("theme"); require(theme in setOf("system", "light", "dark")) { "备份主题无效" }
        val packages = json.getJSONArray("selectedPackages"); require(packages.length() <= 22)
        val selected = (0 until packages.length()).map { packages.getString(it) }.toSet()
        require(selected.all { it in io.github.wcwac.beecount.capture.CaptureApps.packages }) { "备份采集应用配置无效" }
        val retention = json.getInt("backupRetention"); require(retention in setOf(7, 14, 30))
        fun id(name: String): String? = json.optString(name).takeIf { it.isNotBlank() }?.let(::checkedId)
        return BackupPreferences(theme, json.getBoolean("hideAmounts"), json.getBoolean("secureScreen"), id("currentBookId"), id("captureBookId"), selected, json.getBoolean("overlayEnabled"), retention)
    }
    private suspend fun applyBackupPreferences(value: BackupPreferences?, defaultBook: String, enabled: Boolean) {
        val books = repo.observeBooks().first().filter { !it.archived }.map { it.id }.toSet()
        container.settings.update { old -> old.copy(
            theme = value?.theme ?: old.theme, hideAmounts = value?.hideAmounts ?: old.hideAmounts,
            secureScreen = value?.secureScreen ?: old.secureScreen, backupRetention = value?.backupRetention ?: old.backupRetention,
            currentBookId = value?.currentBookId?.takeIf { it in books } ?: defaultBook,
            capture = old.capture.copy(bookId = value?.captureBookId?.takeIf { it in books } ?: defaultBook,
                selectedPackages = value?.selectedPackages ?: old.capture.selectedPackages,
                overlayEnabled = value?.overlayEnabled ?: old.capture.overlayEnabled, enabled = enabled, autoSave = false)
        ) }
    }

    private fun checkedAttachment(path: String): File = File(path).canonicalFile.also {
        require(it.path.startsWith(attachmentRoot.canonicalPath + File.separator)) { "附件路径越界" }
    }
    private fun checkedId(value: String): String { require(UUID.fromString(value).toString() == value) { "对象标识无效" }; return value }
    private data class ColumnDefinition(val name: String, val type: String, val required: Boolean)
    private fun tableSchema(db: SupportSQLiteDatabase, table: String): List<ColumnDefinition> = db.query("PRAGMA table_info(`$table`)").use { c -> buildList {
        while (c.moveToNext()) add(ColumnDefinition(c.getString(c.getColumnIndexOrThrow("name")), c.getString(c.getColumnIndexOrThrow("type")).uppercase(), c.getInt(c.getColumnIndexOrThrow("notnull")) != 0 || c.getInt(c.getColumnIndexOrThrow("pk")) != 0))
    } }
    private fun restoreFingerprint(id: String): String = MessageDigest.getInstance("SHA-256").digest("restore:$id".toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it.toInt() and 255) }
    private fun restoreCommitted(id: String): Boolean = container.database.openHelper.readableDatabase.query("SELECT 1 FROM operation_receipts WHERE operationId=? AND fingerprint=? AND entryId=?", arrayOf(id, restoreFingerprint(id), "restore:$id")).use { it.moveToFirst() }
    private class ExpansionBudget {
        private var total = 0L
        fun add(bytes: Int) { total = Math.addExact(total, bytes.toLong()); require(total <= MAX_ARCHIVE_BYTES) { "备份解压内容超过 512 MiB" } }
    }
    private class BoundedInput(private val source: InputStream, private val limit: Long, private val budget: ExpansionBudget, private val expectedCrc: Long) : InputStream() {
        private val checksum = java.util.zip.CRC32()
        private var total = 0L
        private fun charge(count: Int) { if (count > 0) { total += count; require(total <= limit) { "备份条目实际大小超限" }; budget.add(count) } }
        override fun read(): Int = source.read().also { if (it >= 0) { charge(1); checksum.update(it) } else verify() }
        override fun read(buffer: ByteArray, offset: Int, length: Int): Int = source.read(buffer, offset, length).also { if (it >= 0) { charge(it); checksum.update(buffer, offset, it) } else verify() }
        private fun verify() { require(expectedCrc >= 0 && checksum.value == expectedCrc) { "备份文件内容校验失败" } }
        override fun close() = source.close()
    }
    private class BoundedOutput(private val target: OutputStream, private val limit: Long, private val budget: ExpansionBudget) : OutputStream() {
        private var total = 0L
        private fun charge(count: Int) { total += count; require(total <= limit) { "备份条目超过允许大小" }; budget.add(count) }
        override fun write(value: Int) { charge(1); target.write(value) }
        override fun write(buffer: ByteArray, offset: Int, length: Int) { charge(length); target.write(buffer, offset, length) }
        override fun flush() = target.flush()
    }
    /** Reject huge single JSON tokens before JsonReader allocates their full contents. */
    private class JsonSafetyReader(private val source: Reader) : Reader() {
        private var quoted = false
        private var escaped = false
        private var token = 0
        override fun read(buffer: CharArray, offset: Int, length: Int): Int {
            val count = source.read(buffer, offset, length)
            for (index in offset until offset + count.coerceAtLeast(0)) {
                val ch = buffer[index]
                if (quoted) {
                    require(++token <= 1_048_576) { "备份字段过长" }
                    when { escaped -> escaped = false; ch == '\\' -> escaped = true; ch == '"' -> { quoted = false; token = 0 } }
                } else when { ch == '"' -> { quoted = true; token = 0 }; ch.isWhitespace() || ch in "[]{}:," -> token = 0; else -> require(++token <= 64) { "备份数值字段过长" } }
            }
            return count
        }
        override fun close() = source.close()
    }
    private fun copyBounded(input: InputStream, output: OutputStream, limit: Long): Long {
        val buffer = ByteArray(32768); var total = 0L
        while (true) { val size = input.read(buffer); if (size < 0) return total; total += size; require(total <= limit) { "文件超过允许大小" }; output.write(buffer, 0, size) }
    }
    private fun atomicWrite(target: File, text: String) {
        val temporary = File(target.parentFile, target.name + ".tmp")
        FileOutputStream(temporary).use { it.write(text.toByteArray(Charsets.UTF_8)); it.fd.sync() }
        require(temporary.renameTo(target)) { "无法保存恢复状态" }
    }

    companion object {
        private const val MAX_ARCHIVE_BYTES = 512L * 1024 * 1024
        private const val MAX_TABLE_BYTES = 128L * 1024 * 1024
        private const val MAX_ATTACHMENT_BYTES = 16L * 1024 * 1024
        private const val MAX_ATTACHMENTS = 10_000
        private val CSV_HEADERS = listOf("type", "amount", "currency", "time", "account", "category", "merchant", "note", "excluded_from_totals", "excluded_from_budget", "fee")
        val TABLES = listOf("books", "accounts", "categories", "tags", "attachments", "entries", "account_entries", "entry_tags", "entry_attachments", "operation_receipts", "candidates", "candidate_templates", "budgets", "goals", "recurring_rules", "instalments", "investments", "investment_events", "merchant_rules", "account_mappings", "period_notes")
    }
}
