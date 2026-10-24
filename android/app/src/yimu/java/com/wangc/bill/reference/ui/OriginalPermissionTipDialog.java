package com.wangc.bill.reference.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;

/** Recovered PermissionTipDialog h0/i0, using the original layout, style and dismissal order. */
public final class OriginalPermissionTipDialog extends DialogFragment {
    public interface Callback { void confirm(); void cancel(); }
    private Callback callback;

    public static OriginalPermissionTipDialog create(String title, String content,
            String confirmText, String cancelText) {
        OriginalPermissionTipDialog dialog = new OriginalPermissionTipDialog();
        Bundle args = new Bundle();
        args.putString("title", title); args.putString("content", content);
        args.putString("confirmText", confirmText); args.putString("cancelText", cancelText);
        dialog.setArguments(args); return dialog;
    }

    /** Like the original F field, callbacks belong to this instance, not persisted Bundle data. */
    public OriginalPermissionTipDialog callbacks(Callback callback) { this.callback = callback; return this; }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        int style = Ui.res(requireContext(), "style", "AlertDialogStyle");
        if (style == 0) throw new IllegalStateException("原版权限弹窗样式缺失");
        setStyle(STYLE_NO_TITLE, style);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        int layout = Ui.res(requireContext(), "layout", "dialog_common");
        if (layout == 0) throw new IllegalStateException("原版权限弹窗布局缺失");
        View root = inflater.inflate(layout, container, false);
        Bundle args = requireArguments();
        text(root, "title").setText(args.getString("title"));
        text(root, "content").setText(args.getString("content"));
        TextView cancel = text(root, "cancel"), confirm = text(root, "confirm");
        String cancelText = args.getString("cancelText");
        cancel.setVisibility(TextUtils.isEmpty(cancelText) ? View.GONE : View.VISIBLE);
        if (!TextUtils.isEmpty(cancelText)) cancel.setText(cancelText);
        confirm.setText(args.getString("confirmText"));
        cancel.setOnClickListener(view -> { dismiss(); if (callback != null) callback.cancel(); });
        confirm.setOnClickListener(view -> { dismiss(); if (callback != null) callback.confirm(); });
        return root;
    }

    @Override public void onResume() {
        Dialog dialog = requireDialog(); Window window = dialog.getWindow();
        if (window == null) throw new IllegalStateException("原版权限弹窗窗口缺失");
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = getResources().getDisplayMetrics().widthPixels - Ui.dp(requireContext(), 60);
        attributes.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(attributes);
        dialog.setCancelable(false); dialog.setCanceledOnTouchOutside(false);
        super.onResume();
    }

    private static TextView text(View root, String name) {
        View value = Ui.view(root, name);
        if (!(value instanceof TextView text)) throw new IllegalStateException("原版权限弹窗控件缺失：" + name);
        return text;
    }
}
