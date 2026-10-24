package com.wangc.bill.reference.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.fragment.app.DialogFragment;

/** The specific CommonDialog.j0 call in AutoActivity.allowRunBack. */
public final class OriginalCaptureBackgroundDialog extends DialogFragment {
    private Runnable confirmed;

    public static OriginalCaptureBackgroundDialog create(Runnable confirm) {
        OriginalCaptureBackgroundDialog dialog = new OriginalCaptureBackgroundDialog();
        Bundle args = new Bundle();
        args.putString("title", "允许后台运行");
        args.putString("content", "点击「耗电管理」-「允许完全后台行为」");
        args.putString("confirmText", "前往设置"); args.putString("cancelText", "取消");
        dialog.setArguments(args); dialog.confirmed = confirm; return dialog;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); OriginalAutoTipDialog.style(this);
    }
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        View root = OriginalAutoTipDialog.inflate(inflater, parent, "dialog_common");
        Bundle args = requireArguments();
        OriginalAutoTipDialog.text(root, "title").setText(args.getString("title"));
        OriginalAutoTipDialog.text(root, "content").setText(args.getString("content"));
        OriginalAutoTipDialog.text(root, "confirm").setText(args.getString("confirmText"));
        OriginalAutoTipDialog.text(root, "cancel").setText(args.getString("cancelText"));
        OriginalAutoTipDialog.text(root, "cancel").setOnClickListener(view -> dismiss());
        OriginalAutoTipDialog.text(root, "confirm").setOnClickListener(view -> {
            dismiss(); if (confirmed != null) confirmed.run();
        });
        return root;
    }
    @Override public void onResume() {
        Dialog dialog = requireDialog(); Window window = OriginalAutoTipDialog.window(this);
        WindowManager.LayoutParams size = window.getAttributes();
        size.width = OriginalAutoTipDialog.screenWidth(requireContext()) - OriginalDimensions.dp(60f);
        size.height = ViewGroup.LayoutParams.WRAP_CONTENT; window.setAttributes(size);
        // CommonDialog.I defaults true. This callsite never invokes l0(false).
        dialog.setCancelable(true); dialog.setCanceledOnTouchOutside(true);
        super.onResume();
    }
}
