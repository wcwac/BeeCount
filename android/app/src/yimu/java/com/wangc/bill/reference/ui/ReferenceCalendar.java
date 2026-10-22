package com.wangc.bill.reference.ui;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.*;
import java.util.Calendar;

public final class ReferenceCalendar extends LinearLayout {
    public interface Listener{void day(long time);}
    private long selected=System.currentTimeMillis();private Listener listener;
    public ReferenceCalendar(Context c,AttributeSet a){super(c,a);setOrientation(VERTICAL);render();}
    public void configure(long time,Listener l){selected=time;listener=l;render();}
    private void render(){removeAllViews();Calendar month=Calendar.getInstance();month.setTimeInMillis(selected);int chosen=month.get(Calendar.DAY_OF_MONTH);month.set(Calendar.DAY_OF_MONTH,1);int offset=month.get(Calendar.DAY_OF_WEEK)-1,days=month.getActualMaximum(Calendar.DAY_OF_MONTH);String[] week={"日","一","二","三","四","五","六"};
        LinearLayout head=new LinearLayout(getContext());for(String w:week){TextView t=Ui.label(getContext(),w,12);t.setGravity(Gravity.CENTER);head.addView(t,new LinearLayout.LayoutParams(0,Ui.dp(getContext(),28),1));}addView(head);
        for(int row=0;row<(offset+days+6)/7;row++){LinearLayout line=new LinearLayout(getContext());for(int col=0;col<7;col++){int day=row*7+col-offset+1;TextView t=Ui.label(getContext(),day<1||day>days?"":String.valueOf(day),14);t.setGravity(Gravity.CENTER);if(day==chosen){t.setTextColor(-1);t.setBackground(Ui.rounded(Ui.GREEN,24,getContext()));}line.addView(t,new LinearLayout.LayoutParams(0,Ui.dp(getContext(),40),1));if(day>=1&&day<=days)t.setOnClickListener(v->{Calendar d=Calendar.getInstance();d.setTimeInMillis(selected);d.set(Calendar.DAY_OF_MONTH,day);selected=d.getTimeInMillis();render();if(listener!=null)listener.day(selected);});}addView(line);}}
}
