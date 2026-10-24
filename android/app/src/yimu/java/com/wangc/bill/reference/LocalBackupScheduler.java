package com.wangc.bill.reference;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.DocumentsContract;
import com.wangc.bill.reference.ui.LocalFiles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Host local directory backups. Uses only a user-granted document tree, never network APIs. */
public final class LocalBackupScheduler {
    public static final int JOB_ID = 6303;
    public static final String ENABLED = "local_backup_enabled", TREE = "local_backup_tree";
    private static final String PREFIX = "YimuReference-auto-";
    private static final long DAY = 24 * 60 * 60 * 1000L;
    private LocalBackupScheduler() {}
    public static void sync(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) return;
        SharedPreferences prefs = ReferenceApplication.prefs();
        if (!prefs.getBoolean(ENABLED, false) || prefs.getString(TREE, "").isEmpty()) {
            scheduler.cancel(JOB_ID); return;
        }
        JobInfo info = new JobInfo.Builder(JOB_ID, new ComponentName(context, LocalBackupJobService.class))
                .setPeriodic(DAY, 60 * 60 * 1000L).setPersisted(true)
                .setRequiresBatteryNotLow(true).setRequiresStorageNotLow(true).build();
        JobInfo existing = scheduler.getPendingJob(JOB_ID);
        if (existing == null) scheduler.schedule(info);
    }
    public static Uri directory() {
        String value = ReferenceApplication.prefs().getString(TREE, "");
        if (value.isEmpty()) throw new IllegalStateException("请先选择自动备份目录");
        Uri uri = Uri.parse(value);
        if (!"content".equals(uri.getScheme()) || !DocumentsContract.isTreeUri(uri))
            throw new IllegalArgumentException("备份目录授权无效，请重新选择");
        return uri;
    }
    /** Caller runs on the file worker; a completed ZIP is kept before pruning old owned files. */
    public static synchronized Uri backupNow(Context context) throws Exception {
        if (Thread.currentThread().isInterrupted()) throw new java.io.InterruptedIOException("备份已中止");
        SharedPreferences prefs = ReferenceApplication.prefs();
        prefs.edit().putLong("local_backup_last_attempt", System.currentTimeMillis()).apply();
        Uri destination = null;
        try {
            Uri tree = directory();
            Uri parent = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree));
            String name = PREFIX + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID() + ".zip";
            destination = DocumentsContract.createDocument(context.getContentResolver(), parent, "application/zip", name);
            if (destination == null) throw new java.io.IOException("无法在已选目录创建备份");
            LocalFiles.exportBackup(context, destination);
            if (Thread.currentThread().isInterrupted()) throw new java.io.InterruptedIOException("备份已中止");
            prefs.edit().putLong("local_backup_last_success", System.currentTimeMillis())
                    .putString("local_backup_status", "备份完成").apply();
            // Retention failure never removes or misreports the successful backup.
            try { prune(context, destination); }
            catch (Exception error) { prefs.edit().putString("local_backup_status", "备份完成，旧文件清理未完成").apply(); }
            return destination;
        } catch (Exception error) {
            if (destination != null) try { DocumentsContract.deleteDocument(context.getContentResolver(), destination); } catch (Exception ignored) {}
            prefs.edit().putString("local_backup_status", "备份失败，请检查目录授权和可用空间").apply();
            throw error;
        }
    }
    public static final class BackupFile {
        public final String name; public final Uri uri; public final long modified, size;
        BackupFile(String name, Uri uri, long modified, long size) { this.name=name;this.uri=uri;this.modified=modified;this.size=size; }
    }
    public static List<BackupFile> files(Context context) throws Exception {
        Uri tree = directory();
        Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree));
        String[] columns = { DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_SIZE };
        List<BackupFile> result = new ArrayList<>();
        try (android.database.Cursor cursor = context.getContentResolver().query(children, columns, null, null, null)) {
            if (cursor == null) throw new java.io.IOException("无法读取备份目录");
            while (cursor.moveToNext()) {
                String name = cursor.getString(1);
                if (name == null || !name.matches("YimuReference-auto-[0-9]{13,17}-[0-9a-f-]{36}\\.zip")) continue;
                if (result.size() >= 10000) throw new java.io.IOException("备份目录文件过多");
                result.add(new BackupFile(name, DocumentsContract.buildDocumentUriUsingTree(tree,cursor.getString(0)), cursor.getLong(2), cursor.getLong(3)));
            }
        }
        result.sort(Comparator.comparing((BackupFile item) -> item.name).reversed());
        return result;
    }
    private static void prune(Context context, Uri newest) throws Exception {
        int keep = ReferenceApplication.prefs().getInt("local_backup_keep", 7);
        if (keep != 7 && keep != 14 && keep != 30) keep = 7;
        List<BackupFile> items = files(context);
        int retained = 1;
        for (BackupFile item : items) {
            if (item.uri.equals(newest)) continue;
            if (retained++ < keep) continue;
            DocumentsContract.deleteDocument(context.getContentResolver(), item.uri);
        }
    }
}
