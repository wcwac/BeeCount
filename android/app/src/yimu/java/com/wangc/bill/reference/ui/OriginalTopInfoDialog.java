package com.wangc.bill.reference.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.fragment.app.DialogFragment;

/** Recovered TopInfoDialog presentation; native caller initialization remains separate. */
public final class OriginalTopInfoDialog extends DialogFragment {
    public static OriginalTopInfoDialog create(String title,String content){
        OriginalTopInfoDialog result=new OriginalTopInfoDialog();Bundle args=new Bundle();
        args.putString("title",title);args.putString("content",content);result.setArguments(args);return result;
    }
    @Override public void onCreate(Bundle state){super.onCreate(state);OriginalAutoTipDialog.style(this);}
    @Override public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle state){
        View root=OriginalAutoTipDialog.inflate(inflater,container,"dialog_top_info");
        OriginalAutoTipDialog.text(root,"title").setText(requireArguments().getString("title"));
        OriginalAutoTipDialog.text(root,"content").setText(requireArguments().getString("content"));return root;
    }
    @Override public void onResume(){
        Dialog dialog=requireDialog();Window window=OriginalAutoTipDialog.window(this);
        WindowManager.LayoutParams attributes=window.getAttributes();
        attributes.width=OriginalAutoTipDialog.screenWidth(requireContext())-OriginalDimensions.dp(20f);
        attributes.height=ViewGroup.LayoutParams.WRAP_CONTENT;attributes.gravity=Gravity.TOP;window.setAttributes(attributes);
        dialog.setCancelable(false);dialog.setCanceledOnTouchOutside(false);super.onResume();
    }
    /** Original P -> Q(true,false): hide the Dialog now; fragment removal may complete later. */
    public void dismissForOutcome(){dismissAllowingStateLoss();}
}
