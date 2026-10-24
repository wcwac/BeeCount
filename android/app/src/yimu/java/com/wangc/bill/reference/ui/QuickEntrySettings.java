package com.wangc.bill.reference.ui;

import android.widget.Switch;

final class QuickEntrySettings {
    private QuickEntrySettings() {}
    static void render(ManagerActivity activity) {
        activity.body("快捷记账", "刷新", activity::reload);
        Switch enabled = new Switch(activity);
        enabled.setText("显示快捷记账悬浮球");
        enabled.setContentDescription("显示快捷记账悬浮球");
        enabled.setPadding(Ui.dp(activity, 18), Ui.dp(activity, 10), Ui.dp(activity, 18), Ui.dp(activity, 10));
        enabled.setMinHeight(Ui.dp(activity, 56));
        enabled.setChecked(QuickEntryWindow.enabled());
        enabled.setOnCheckedChangeListener((button, value) -> {
            QuickEntryWindow.setEnabled(value);
            if (value && !OverlayPermission.granted()) OverlayPermission.request(activity);
            activity.reload();
        });
        activity.rows.addView(enabled);
        activity.rows.addView(Ui.row(activity, "悬浮球状态", QuickEntryWindow.statusText(), activity::reload));
        activity.rows.addView(Ui.row(activity, "悬浮窗权限", OverlayPermission.granted() ? "已授权，点击管理" : "点击授权后可在其他应用上显示", () -> {
            if (OverlayPermission.granted()) OverlayPermission.requestDirect(activity); else OverlayPermission.request(activity);
        }));
        activity.rows.addView(Ui.row(activity, "立即记一笔", "打开记账页面", () -> Ui.edit(activity, 0, null)));
        activity.rows.addView(Ui.row(activity, "重置悬浮球位置", "将悬浮球移回屏幕右侧", () -> {
            QuickEntryWindow.resetPosition(); activity.reload();
        }));
        activity.rows.addView(Ui.row(activity, "使用方式", "点击悬浮球进入记账，拖动可移动位置。快捷记账可单独使用，无需开启自动采集。", null));
    }
}
