package com.wangc.bill.reference.ui;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.graphics.drawable.Drawable;
import android.widget.*;
import com.wangc.bill.reference.ReferenceApplication;

public final class ReferenceBottomBar extends LinearLayout {
    public interface Listener { void select(int index); }
    private Listener listener;
    private int selected;
    private final String[] names={"首页","日历","资产","统计","我的"},icons={"main","calendar","asset","statistics","my"};
    public ReferenceBottomBar(Context c,AttributeSet a){super(c,a);setOrientation(HORIZONTAL);setBackgroundColor(Ui.color(c,"white",-1));rebuild();}
    public void setListener(Listener l){listener=l;}
    public void select(int index){selected=index;rebuild();}
    private void rebuild(){
        removeAllViews();boolean labels=ReferenceApplication.prefs().getBoolean("navigation_labels",false);
        for(int i=0;i<names.length;i++){
            final int index=i;LinearLayout item=new LinearLayout(getContext());item.setOrientation(VERTICAL);item.setGravity(Gravity.CENTER);item.setContentDescription(names[i]);
            ImageView icon=new ImageView(getContext());int id=Ui.res(getContext(),"drawable","selector_"+icons[i]);
            if(id!=0){Drawable d=getContext().getDrawable(id);d.setState(i==selected?new int[]{android.R.attr.state_checked}:new int[]{});icon.setImageDrawable(d.getCurrent());}
            item.addView(icon,new LinearLayout.LayoutParams(Ui.dp(getContext(),24),Ui.dp(getContext(),24)));
            if(labels){TextView label=Ui.label(getContext(),names[i],10);label.setTextColor(i==selected?Ui.GREEN:0xff888888);item.addView(label);}
            addView(item,new LinearLayout.LayoutParams(0,Ui.dp(getContext(),56),1));item.setOnClickListener(v->{select(index);if(listener!=null)listener.select(index);});
        }
    }
}
