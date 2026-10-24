package com.wangc.bill.reference.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Recovered BaseToolBarActivity + AutoWorkActivity.Q/R/S/T.
 * The host calls render once as an explicit bridge for native AutoWork.onCreate;
 * that native method's initialization order is not claimed to be reconstructed.
 */
public final class OriginalAutoWorkScreen {
    private OriginalAutoWorkScreen() {}

    public static OriginalAutoWorkController render(ManagerActivity host) {
        return render(host, OriginalAutoWorkController.DeviceInfo.current(),
                androidx.fragment.app.FragmentActivity::startActivity);
    }

    /** Conditional OEM/Intent tests still install this actual page in the host window. */
    static OriginalAutoWorkController render(ManagerActivity host,
            OriginalAutoWorkController.DeviceInfo device, OriginalAutoWorkController.Launcher launcher) {
        View root = Ui.layout(host, "activity_base_tool_bar");
        View toolbar = required(root, "tool_bar");
        int statusHeight = statusBarHeight(host);
        ViewGroup.LayoutParams toolbarSize = toolbar.getLayoutParams();
        toolbarSize.height = OriginalDimensions.dp(50f) + statusHeight;
        toolbar.setLayoutParams(toolbarSize);
        toolbar.setPadding(0, statusHeight, 0, 0);
        View content = required(root, "content_layout");
        if (!(content instanceof FrameLayout frame)) throw new IllegalStateException("原版工具栏内容容器不匹配");
        int layout = Ui.res(host, "layout", "activity_notice_work");
        if (layout == 0) throw new IllegalStateException("原版自动记账帮助布局缺失");
        LayoutInflater.from(host).inflate(layout, frame, true);
        ((TextView) required(root, "title")).setText("自动记账不工作");
        // Original S() is null: the empty right TextView remains visible.
        ((TextView) required(root, "right_text")).setText(null);
        required(root, "right_icon").setVisibility(View.GONE);
        required(root, "btn_back").setOnClickListener(view -> host.finish());
        OriginalAutoWorkController controller = new OriginalAutoWorkController(host, root, device, launcher);
        controller.bind();
        host.template = root;
        // Ui.content applies another top inset. The recovered toolbar already owns
        // its status-bar padding, so this page installs its original wrapper directly.
        host.setContentView(root);
        return controller;
    }

    /** AutoActivity.allowRunBack is a separate source entry, not this page's row. */
    public static void showCaptureBackgroundSettings(ManagerActivity host) {
        OriginalAutoWorkController.showCaptureBackgroundSettings(host,
                OriginalAutoWorkController.DeviceInfo.current(),
                androidx.fragment.app.FragmentActivity::startActivity);
    }

    static void showCaptureBackgroundSettings(ManagerActivity host,
            OriginalAutoWorkController.DeviceInfo device, OriginalAutoWorkController.Launcher launcher) {
        OriginalAutoWorkController.showCaptureBackgroundSettings(host, device, launcher);
    }

    static View required(View root, String id) {
        View result = Ui.view(root, id);
        if (result == null) throw new IllegalStateException("原版自动记账帮助控件缺失：" + id);
        return result;
    }

    static int statusBarHeight(Context context) {
        // Original blankj.f.i m6860@0x3396fc reads Application resources and
        // directly resolves the dimension. Missing identifiers must propagate;
        // only a successfully returned zero height uses BaseToolBar's fallback.
        android.content.res.Resources resources = context.getApplicationContext().getResources();
        int id = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(id);
        return height == 0 ? OriginalDimensions.dp(24f) : height;
    }
}
