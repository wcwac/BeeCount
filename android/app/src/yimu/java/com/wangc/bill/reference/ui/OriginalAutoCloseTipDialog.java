package com.wangc.bill.reference.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;

/** AutoCloseTipDialog's actual clicks and spans; native Main scheduling is not reconstructed here. */
public final class OriginalAutoCloseTipDialog extends DialogFragment {
    public interface Navigation {void openAutoWork();void openAutoActivity();}
    @FunctionalInterface interface SettingsLauncher {void launch(Activity activity,Intent intent);}
    private Navigation navigation;
    private SettingsLauncher settings=Activity::startActivity;

    public static OriginalAutoCloseTipDialog create(){return new OriginalAutoCloseTipDialog();}
    /** The host supplies actual route consumers; callbacks, like source fields, are not persisted. */
    public OriginalAutoCloseTipDialog navigation(Navigation value){
        if(value==null)throw new IllegalArgumentException("Permission guidance navigation is required");
        navigation=value;return this;
    }
    OriginalAutoCloseTipDialog settingsLauncher(SettingsLauncher value){
        if(value==null)throw new IllegalArgumentException("Settings launcher is required");settings=value;return this;
    }
    @Override public void onCreate(Bundle state){super.onCreate(state);OriginalAutoTipDialog.style(this);}
    @Override public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle state){
        View root=OriginalAutoTipDialog.inflate(inflater,container,"dialog_auto_close_tip");
        OriginalAutoTipDialog.text(root,"cancel").setOnClickListener(view->dismiss());
        OriginalAutoTipDialog.text(root,"confirm").setOnClickListener(view->{
            Activity activity=requireActivity();dismiss();
            try{settings.launch(activity,new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}
            catch(Exception ignored){
                // The source fallback is outside another catch. Exception text/SQL is not logged.
                settings.launch(activity,new Intent(Settings.ACTION_SETTINGS));
            }
        });return root;
    }
    @Override public void onResume(){
        Dialog dialog=requireDialog();Window window=OriginalAutoTipDialog.window(this);
        WindowManager.LayoutParams attributes=window.getAttributes();
        attributes.width=OriginalAutoTipDialog.screenWidth(requireContext())-Ui.dp(requireContext(),40);
        attributes.height=ViewGroup.LayoutParams.WRAP_CONTENT;window.setAttributes(attributes);
        dialog.setCancelable(true);dialog.setCanceledOnTouchOutside(true);
        span(OriginalAutoTipDialog.text(requireView(),"tip_content"),16,24,true);
        span(OriginalAutoTipDialog.text(requireView(),"close_content"),11,15,false);super.onResume();
    }
    private void span(TextView text,int start,int end,boolean work){
        SpannableString value=new SpannableString(text.getText());
        value.setSpan(new ForegroundColorSpan(OriginalAutoTipDialog.primary(requireContext())),start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        value.setSpan(new ClickableSpan(){@Override public void onClick(View widget){
            // Original links do not dismiss, toggle autoBill, or change autoJump.
            if(navigation!=null){if(work)navigation.openAutoWork();else navigation.openAutoActivity();}
            else Ui.page(requireActivity(),work?"auto_work":"capture");
        }},start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setMovementMethod(LinkMovementMethod.getInstance());text.setText(value);
    }
}
