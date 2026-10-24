package com.wangc.bill.reference.ui;

import android.content.Intent;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.CompoundButton;
import com.wangc.bill.reference.*;

final class LocalBackupScreens {
    private LocalBackupScreens() {}
    static void bind(ManagerActivity a, View root) {
        View toggle = Ui.view(root,"switch_file_auto");
        if (toggle instanceof CompoundButton button) {
            button.setEnabled(true);button.setChecked(ReferenceApplication.prefs().getBoolean(LocalBackupScheduler.ENABLED,false));
            button.setOnCheckedChangeListener((v,enabled)->{
                if(enabled && ReferenceApplication.prefs().getString(LocalBackupScheduler.TREE,"").isEmpty()) {
                    button.setChecked(false);a.requestBackupDirectory();return;
                }
                ReferenceApplication.prefs().edit().putBoolean(LocalBackupScheduler.ENABLED,enabled).apply();LocalBackupScheduler.sync(a);
            });
        }
        Ui.click(root,"auto_backup",()->a.open("backup_history",0));
        Ui.click(root,"history_data",()->a.open("backup_history",0));
        Ui.click(root,"local_backup_file_manager",()->a.open("backup_history",0));
        Ui.click(root,"local_backup_move",a::requestBackupDirectory);
        Ui.click(root,"local_backup_path",a::requestBackupDirectory);
        Ui.text(root,"local_backup_path",ReferenceApplication.prefs().getString(LocalBackupScheduler.TREE,"").isEmpty()?"选择本地自动备份目录":"自动备份目录已授权 · 点击更换");
    }
    static void history(ManagerActivity a) {
        a.body("本地自动备份","选择目录",a::requestBackupDirectory);
        a.rows.addView(Ui.row(a,"每日自动备份","完整ZIP，包含全部账本和附件。系统在电量及空间允许时执行，省电策略可能延迟。",null));
        long last=ReferenceApplication.prefs().getLong("local_backup_last_success",0);
        a.rows.addView(Ui.row(a,"最近状态",ReferenceApplication.prefs().getString("local_backup_status","尚未自动备份")+(last>0?" · "+Ui.date(last,"yyyy-MM-dd HH:mm"):""),null));
        int keep=ReferenceApplication.prefs().getInt("local_backup_keep",7);
        a.rows.addView(Ui.row(a,"保留备份数量",keep+" 份，仅清理本功能生成的文件",()->new android.app.AlertDialog.Builder(a).setItems(new String[]{"7份","14份","30份"},(dialog,index)->{ReferenceApplication.prefs().edit().putInt("local_backup_keep",new int[]{7,14,30}[index]).apply();a.reload();}).show()));
        a.rows.addView(Ui.row(a,"立即备份到已选目录","首次选择目录后可立即验证授权。",()->FileTasks.run(a,"正在备份",()->LocalBackupScheduler.backupNow(a),uri->{a.reload();Ui.toast(a,"完整备份已保存");})));
        if(ReferenceApplication.prefs().getString(LocalBackupScheduler.TREE,"").isEmpty())return;
        FileTasks.run(a,"正在读取备份目录",()->LocalBackupScheduler.files(a),items->{
            for(LocalBackupScheduler.BackupFile item:items)a.rows.addView(Ui.row(a,item.name,(item.size/1024)+" KB",()->new android.app.AlertDialog.Builder(a).setTitle("本地备份").setItems(new String[]{"恢复此备份","删除此文件"},(dialog,index)->{
                if(index==0)Ui.confirm(a,"恢复完整备份","将替换当前本地数据。请先备份现有数据。",()->FileTasks.run(a,"正在恢复",()->{LocalFiles.restoreBackup(a,item.uri);return true;},done->{com.wangc.bill.reference.capture.PlanScheduler.sync();a.reload();Ui.toast(a,"恢复完成，请重新核对采集设置");}));
                else Ui.confirm(a,"删除备份","此文件会永久删除，现有账单不受影响。",()->FileTasks.run(a,"正在删除",()->DocumentsContract.deleteDocument(a.getContentResolver(),item.uri),deleted->{if(!deleted)Ui.toast(a,"文件未删除");a.reload();}));
            }).show()));
            if(items.isEmpty())a.rows.addView(Ui.row(a,"暂无自动备份","可以点击立即备份。",null));
        });
    }
}
