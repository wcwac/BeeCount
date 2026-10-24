package com.wangc.bill.reference.capture;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import com.wangc.bill.reference.ReferenceApplication;
import com.wangc.bill.reference.ui.ManagerActivity;

public final class CaptureNotifications {
    public static void update() {
        long count = ReferenceApplication.store().scalar("SELECT COUNT(*) n FROM candidates WHERE state='PENDING'");
        if (count == 0) { manager().cancel(6300); return; }
        post("有 " + count + " 条自动记账待确认", "点击核对采集结果并保存", false);
    }
    public static void evidenceConflict() { post("同一支付凭据的识别结果不一致", "未重复入账，请核对已保存账单及采集诊断", true); }
    public static void error() { post("自动记账未能保存采集结果", "请打开自动记账诊断查看状态", true); }
    private static NotificationManager manager() { return (NotificationManager) ReferenceApplication.app().getSystemService(android.content.Context.NOTIFICATION_SERVICE); }
    private static void post(String title, String message, boolean failure) {
        try {
            if (Build.VERSION.SDK_INT >= 26) manager().createNotificationChannel(new NotificationChannel("yimu_capture", "自动记账", NotificationManager.IMPORTANCE_DEFAULT));
            Intent intent = new Intent(ReferenceApplication.app(), ManagerActivity.class).putExtra("page", failure ? "capture" : "inbox");
            PendingIntent click = PendingIntent.getActivity(ReferenceApplication.app(), failure ? 6301 : 6300, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(ReferenceApplication.app(), "yimu_capture") : new Notification.Builder(ReferenceApplication.app());
            manager().notify(failure ? 6301 : 6300, builder.setSmallIcon(android.R.drawable.ic_menu_edit).setContentTitle(title).setContentText(message).setContentIntent(click).setAutoCancel(true).setOnlyAlertOnce(true).setVisibility(Notification.VISIBILITY_PRIVATE).build());
        } catch (RuntimeException ignored) { /* Persistent drafts and in-app diagnostics remain available. */ }
    }
}
