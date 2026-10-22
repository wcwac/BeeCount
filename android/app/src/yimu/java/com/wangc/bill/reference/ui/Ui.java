package com.wangc.bill.reference.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.wangc.bill.reference.ReferenceApplication;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class Ui {
    public static final int GREEN = Color.rgb(72,171,147), RED = Color.rgb(251,86,85);
    private Ui() {}
    public static int dp(Context c, float value) { return Math.round(value * c.getResources().getDisplayMetrics().density); }
    public static int id(Context c, String name) { return res(c,"id",name); }
    public static int res(Context c, String type, String name) { if (name == null || !name.matches("[A-Za-z_][A-Za-z0-9_]*")) return 0; int id=c.getResources().getIdentifier(name,type,c.getPackageName());return id!=0?id:c.getResources().getIdentifier(name,type,"com.wangc.bill"); }
    public static View layout(Context c, String name) { int id=res(c,"layout",name);if(id==0)throw new IllegalArgumentException("界面资源不存在："+name);return LayoutInflater.from(c).inflate(id,null,false); }
    @SuppressWarnings("unchecked") public static <T extends View> T view(View root,String name) { int id=id(root.getContext(),name);return id==0?null:(T)root.findViewById(id); }
    public static void text(View root,String name,CharSequence value) { View v=view(root,name);if(v instanceof TextView t)t.setText(value); }
    public static void image(View root,String name,String resource) { View v=view(root,name);if(v instanceof ImageView i){int id=res(root.getContext(),"mipmap",resource);if(id==0)id=res(root.getContext(),"drawable",resource);if(id!=0)i.setImageResource(id);} }
    public static void hide(View root,String... names) { for(String name:names){View v=view(root,name);if(v!=null)v.setVisibility(View.GONE);} }
    public static void show(View root,String name,boolean visible) { View v=view(root,name);if(v!=null)v.setVisibility(visible?View.VISIBLE:View.GONE); }
    public static void click(View root,String name,Runnable action) { View v=view(root,name);if(v!=null)v.setOnClickListener(x->action.run()); }
    public static void content(Activity activity,View root) {
        int left=root.getPaddingLeft(),top=root.getPaddingTop(),right=root.getPaddingRight(),bottom=root.getPaddingBottom();
        root.setOnApplyWindowInsetsListener((v,insets)->{
            if(Build.VERSION.SDK_INT>=30){android.graphics.Insets i=insets.getInsets(WindowInsets.Type.systemBars());v.setPadding(left+i.left,top+i.top,right+i.right,bottom+i.bottom);return WindowInsets.CONSUMED;}
            v.setPadding(left+insets.getSystemWindowInsetLeft(),top+insets.getSystemWindowInsetTop(),right+insets.getSystemWindowInsetRight(),bottom+insets.getSystemWindowInsetBottom());return insets.consumeSystemWindowInsets();
        });
        activity.setContentView(root);root.requestApplyInsets();
    }
    public static LinearLayout column(Context context) { LinearLayout c=new LinearLayout(context);c.setOrientation(LinearLayout.VERTICAL);c.setBackgroundColor(color(context,"backgroundLight",0xfff4f4f4));return c; }
    public static int color(Context c,String name,int fallback) { int r=res(c,"color",name);return r==0?fallback:c.getColor(r); }
    public static TextView label(Context c,String text,int size) { TextView t=new TextView(c);t.setText(text);t.setTextSize(size);t.setTextColor(color(c,"black",Color.BLACK));return t; }
    public static GradientDrawable rounded(int color,int radius,Context c) { GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));return d; }
    public static LinearLayout row(Context c,String title,String detail,Runnable action) {
        LinearLayout r=new LinearLayout(c);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(c,18),dp(c,14),dp(c,18),dp(c,14));r.setBackgroundColor(color(c,"white",Color.WHITE));
        r.addView(label(c,title,15));TextView d=label(c,detail==null?"":detail,12);d.setTextColor(color(c,"grey",0xff999999));d.setPadding(0,dp(c,5),0,0);r.addView(d);if(action!=null)r.setOnClickListener(v->action.run());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(c,1);r.setLayoutParams(p);return r;
    }
    public static TextView section(Context c,String title) { TextView t=label(c,title,13);t.setTextColor(color(c,"textGray",0xff888888));t.setPadding(dp(c,18),dp(c,18),dp(c,18),dp(c,8));return t; }
    public static View toolbar(Activity a,String title,String right,Runnable action) {
        LinearLayout bar=new LinearLayout(a);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setBackgroundColor(color(a,"white",Color.WHITE));bar.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(a,50)));
        TextView back=label(a,"‹",32);back.setGravity(Gravity.CENTER);bar.addView(back,new LinearLayout.LayoutParams(dp(a,48),-1));back.setContentDescription("返回");back.setOnClickListener(v->a.finish());
        TextView heading=label(a,title,18);heading.setTypeface(null,Typeface.BOLD);bar.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        if(right!=null){TextView button=label(a,right,15);button.setPadding(dp(a,16),0,dp(a,16),0);button.setGravity(Gravity.CENTER);bar.addView(button,new LinearLayout.LayoutParams(-2,-1));button.setOnClickListener(v->action.run());}return bar;
    }
    public static void page(Context c,String page) { c.startActivity(new Intent(c,ManagerActivity.class).putExtra("page",page)); }
    public static void edit(Context c,long transaction,String candidate) { Intent i=new Intent(c,EditorActivity.class);if(transaction>0)i.putExtra("transaction",transaction);if(candidate!=null)i.putExtra("candidate",candidate);c.startActivity(i); }
    public static void error(Context c,Exception e) { Toast.makeText(c,e.getMessage()==null?"操作失败，请重试":e.getMessage(),Toast.LENGTH_LONG).show(); }
    public static void toast(Context c,String text) { Toast.makeText(c,text,Toast.LENGTH_SHORT).show(); }
    public static void confirm(Context c,String title,String message,Runnable action) { new AlertDialog.Builder(c).setTitle(title).setMessage(message).setNegativeButton("取消",null).setPositiveButton("确定",(d,w)->{try{action.run();}catch(Exception e){error(c,e);}}).show(); }
    public static void offline(Context c) { new AlertDialog.Builder(c).setTitle("实验版仅使用本地功能").setMessage("云同步、在线识别、会员和联网服务未接入。账单和配置保存在本机，可使用本地备份。").setPositiveButton("知道了",null).show(); }
    public static String date(long time,String pattern) { return new SimpleDateFormat(pattern,Locale.CHINA).format(new java.util.Date(time)); }
    public static long[] month(long time) { Calendar c=Calendar.getInstance();c.setTimeInMillis(time);c.set(Calendar.DAY_OF_MONTH,1);midnight(c);long start=c.getTimeInMillis();c.add(Calendar.MONTH,1);return new long[]{start,c.getTimeInMillis()}; }
    public static long[] year(long time) { Calendar c=Calendar.getInstance();c.setTimeInMillis(time);c.set(Calendar.MONTH,0);c.set(Calendar.DAY_OF_MONTH,1);midnight(c);long start=c.getTimeInMillis();c.add(Calendar.YEAR,1);return new long[]{start,c.getTimeInMillis()}; }
    public static long[] day(long time) { Calendar c=Calendar.getInstance();c.setTimeInMillis(time);midnight(c);long start=c.getTimeInMillis();c.add(Calendar.DATE,1);return new long[]{start,c.getTimeInMillis()}; }
    private static void midnight(Calendar c) { c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0); }
}
