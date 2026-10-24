package com.wangc.bill.reference.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
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

/** AutoTipDialog and its six-brand local entry guide; native AutoActivity initialization is separate. */
public final class OriginalAutoTipDialog extends DialogFragment {
    private Runnable confirmed;
    private OriginalLocalTipSheetDialog guide;
    private static final String[][] GUIDE={
        {"小米","无障碍→已下载的应用→自动记账"},{"OPPO","无障碍→已下载的应用→自动记账"},
        {"华为","无障碍→已安装的服务→自动记账"},{"VIVO","无障碍→已下载的服务→自动记账"},
        {"魅族","无障碍→已下载的应用→自动记账"},{"三星","辅助功能→已安装的服务→自动记账"}};

    public static OriginalAutoTipDialog create(){return new OriginalAutoTipDialog();}
    public OriginalAutoTipDialog onConfirm(Runnable value){confirmed=value;return this;}
    @Override public void onCreate(Bundle state){super.onCreate(state);style(this);}
    @Override public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle state){
        View root=inflate(inflater,container,"dialog_auto_tip");
        text(root,"cancel").setOnClickListener(view->dismiss());
        text(root,"confirm").setOnClickListener(view->{dismiss();if(confirmed!=null)confirmed.run();});return root;
    }
    @Override public void onResume(){
        Dialog dialog=requireDialog();Window window=window(this);WindowManager.LayoutParams attributes=window.getAttributes();
        attributes.width=screenWidth(requireContext())-OriginalDimensions.dp(40f);
        attributes.height=ViewGroup.LayoutParams.WRAP_CONTENT;window.setAttributes(attributes);
        dialog.setCancelable(true);dialog.setCanceledOnTouchOutside(true);
        TextView content=text(requireView(),"content");SpannableString value=new SpannableString(content.getText());
        value.setSpan(new ForegroundColorSpan(primary(requireContext())),59,69,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        value.setSpan(new ClickableSpan(){@Override public void onClick(View view){guide=new OriginalLocalTipSheetDialog(requireContext(),GUIDE);guide.show();}},59,69,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        content.setMovementMethod(LinkMovementMethod.getInstance());content.setText(value);super.onResume();
    }
    @Override public void onDestroyView(){
        // Host lifecycle cleanup prevents a subsidiary window retaining a destroyed Activity.
        if(guide!=null){guide.dismiss();guide=null;}super.onDestroyView();
    }
    static void style(DialogFragment fragment){
        int value=Ui.res(fragment.requireContext(),"style","AlertDialogStyle");
        if(value==0)throw new IllegalStateException("原版提示样式缺失");fragment.setStyle(STYLE_NO_TITLE,value);
    }
    static View inflate(LayoutInflater inflater,ViewGroup container,String layout){
        int value=Ui.res(inflater.getContext(),"layout",layout);
        if(value==0)throw new IllegalStateException("原版提示布局缺失："+layout);return inflater.inflate(value,container,false);
    }
    static TextView text(View root,String name){
        View value=Ui.view(root,name);if(!(value instanceof TextView text))throw new IllegalStateException("原版提示控件缺失："+name);return text;
    }
    static Window window(DialogFragment fragment){
        Window value=fragment.requireDialog().getWindow();if(value==null)throw new IllegalStateException("原版提示窗口缺失");return value;
    }
    static int primary(Context context){int value=Ui.res(context,"color","colorPrimary");if(value==0)throw new IllegalStateException("原版提示颜色缺失");return context.getColor(value);}
    /** m0.c/d use the application's WindowManager, including current metrics on API30+. */
    static int screenWidth(Context context){
        WindowManager manager=(WindowManager)context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if(manager==null)return -1;if(Build.VERSION.SDK_INT>=30)return manager.getCurrentWindowMetrics().getBounds().width();
        Point size=new Point();manager.getDefaultDisplay().getSize(size);return size.x;
    }
    static int screenHeight(Context context){
        WindowManager manager=(WindowManager)context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if(manager==null)return -1;if(Build.VERSION.SDK_INT>=30)return manager.getCurrentWindowMetrics().getBounds().height();
        Point size=new Point();manager.getDefaultDisplay().getRealSize(size);return size.y;
    }
}
