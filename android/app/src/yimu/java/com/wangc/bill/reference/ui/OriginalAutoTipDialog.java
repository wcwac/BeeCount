package com.wangc.bill.reference.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/** AutoTipDialog and its six-brand local entry guide; native AutoActivity initialization is separate. */
public final class OriginalAutoTipDialog extends DialogFragment {
    private Runnable confirmed;
    private GuideDialog guide;
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
        attributes.width=screenWidth(requireContext())-Ui.dp(requireContext(),40);
        attributes.height=ViewGroup.LayoutParams.WRAP_CONTENT;window.setAttributes(attributes);
        dialog.setCancelable(true);dialog.setCanceledOnTouchOutside(true);
        TextView content=text(requireView(),"content");SpannableString value=new SpannableString(content.getText());
        value.setSpan(new ForegroundColorSpan(primary(requireContext())),59,69,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        value.setSpan(new ClickableSpan(){@Override public void onClick(View view){guide=new GuideDialog(requireContext());guide.show();}},59,69,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
    /** h1/b8/view.l recovered from DEX; Material version is pinned to the APK's1.10.0 marker. */
    private static final class GuideDialog extends BottomSheetDialog {
        GuideDialog(Context context){
            // Current host night resources bridge the original skin-name predicate MyApplication.n.
            super(context,requiredStyle(context,(context.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES?"SheetDialogStyleNight":"SheetDialogStyle"));
            View root=Ui.layout(context,"dialog_bottom_tip");View close=Ui.view(root,"btn_close");
            if(close==null)throw new IllegalStateException("原版机型提示关闭控件缺失");close.setOnClickListener(view->dismiss());
            RecyclerView list=Ui.view(root,"tip_list");if(list==null)throw new IllegalStateException("原版机型提示列表缺失");
            list.setLayoutManager(new LinearLayoutManager(context));list.setAdapter(new GuideAdapter());setContentView(root);
            getBehavior().setPeekHeight((int)((float)screenHeight(context)*0.5f));setCancelable(true);setCanceledOnTouchOutside(true);
        }
        @Override public void show(){
            super.show();Window window=getWindow();if(window==null)return;
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            View root=findViewById(Ui.id(getContext(),"total_layout"));if(root==null)root=findViewById(Ui.id(getContext(),"parent_layout"));
            if(root!=null){int height=getContext().getResources().getIdentifier("navigation_bar_height","dimen","android");
                int navigation=height==0?0:getContext().getResources().getDimensionPixelSize(height);
                root.setPadding(root.getPaddingLeft(),root.getPaddingTop(),root.getPaddingRight(),root.getPaddingTop()+navigation);}
        }
        private static int requiredStyle(Context context,String name){int value=Ui.res(context,"style",name);if(value==0)throw new IllegalStateException("原版机型提示样式缺失："+name);return value;}
    }
    private static final class GuideHolder extends RecyclerView.ViewHolder {GuideHolder(View item){super(item);}}
    private static final class GuideAdapter extends RecyclerView.Adapter<GuideHolder> {
        @Override public GuideHolder onCreateViewHolder(ViewGroup parent,int type){
            return new GuideHolder(inflate(LayoutInflater.from(parent.getContext()),parent,"item_dialog_tip"));
        }
        @Override public void onBindViewHolder(GuideHolder holder,int position){
            text(holder.itemView,"title").setVisibility(View.VISIBLE);text(holder.itemView,"title").setText(GUIDE[position][0]);
            TextView content=text(holder.itemView,"content");content.setText(GUIDE[position][1]);
            int color=Ui.res(holder.itemView.getContext(),"color","darkGrey");
            if(color==0)throw new IllegalStateException("原版机型提示颜色缺失");content.setTextColor(holder.itemView.getContext().getColor(color));
        }
        @Override public int getItemCount(){return GUIDE.length;}
    }
}
