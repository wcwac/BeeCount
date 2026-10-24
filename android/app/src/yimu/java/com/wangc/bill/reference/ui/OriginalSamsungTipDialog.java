package com.wangc.bill.reference.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.fragment.app.DialogFragment;

/** SamsungTipDialog: both buttons dismiss; neither opens settings or changes a preference. */
public final class OriginalSamsungTipDialog extends DialogFragment {
    public static OriginalSamsungTipDialog create() {
        return new OriginalSamsungTipDialog();
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        OriginalAutoTipDialog.style(this);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View root = OriginalAutoTipDialog.inflate(inflater, container, "dialog_samsung_tip");
        OriginalAutoTipDialog.text(root, "cancel").setOnClickListener(button -> dismiss());
        OriginalAutoTipDialog.text(root, "confirm").setOnClickListener(button -> dismiss());
        return root;
    }

    @Override public void onResume() {
        Dialog dialog = requireDialog();
        Window window = OriginalAutoTipDialog.window(this);
        WindowManager.LayoutParams parameters = window.getAttributes();
        parameters.width = OriginalAutoTipDialog.screenWidth(requireContext())
                - OriginalDimensions.dp(40f);
        parameters.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(parameters);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        super.onResume();
    }
}
