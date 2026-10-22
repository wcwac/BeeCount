package com.wangc.bill.reference.ui;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.*;

public final class ReferenceTabs extends HorizontalScrollView {
    public interface Listener { void select(int index); }
    private final LinearLayout strip;private String[] labels=new String[0];private int selected;private Listener listener;
    public ReferenceTabs(Context c,AttributeSet a){super(c,a);setHorizontalScrollBarEnabled(false);strip=new LinearLayout(c);strip.setGravity(Gravity.CENTER_VERTICAL);addView(strip,new LayoutParams(-2,-1));}
    public void configure(String[] names,int active,Listener l){labels=names;selected=active;listener=l;render();}
    public void select(int value){selected=value;render();}
    private void render(){strip.removeAllViews();for(int i=0;i<labels.length;i++){final int index=i;TextView text=Ui.label(getContext(),labels[i],15);text.setGravity(Gravity.CENTER);text.setTextColor(i==selected?Ui.GREEN:Ui.color(getContext(),"black",0xff000000));text.setTypeface(null,i==selected?android.graphics.Typeface.BOLD:android.graphics.Typeface.NORMAL);strip.addView(text,new LinearLayout.LayoutParams(Ui.dp(getContext(),68),-1));text.setOnClickListener(v->{selected=index;render();if(listener!=null)listener.select(index);});}}
}
