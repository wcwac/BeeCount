package com.wangc.bill.reference.ui;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import com.wangc.bill.reference.Money;
import org.json.JSONObject;
import java.util.List;

/** Small local renderer in the recovered chart containers; no WebView or chart SDK. */
public final class StatsChart extends View {
    private final List<JSONObject> data;private final String mode,currency;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int[] colors={0xff48ab93,0xffefb16c,0xff7ca7cf,0xffc095c8,0xffeb8f93,0xff97b772,0xff77c2c1,0xffceae84};
    public StatsChart(Context c,String mode,List<JSONObject> data,String currency){super(c);this.mode=mode;this.data=data;this.currency=currency;setMinimumHeight(Ui.dp(c,180));setContentDescription("统计图表");}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);float w=getWidth(),h=getHeight(),pad=Ui.dp(getContext(),22);long total=0,max=1;for(JSONObject r:data){long n=Math.max(0,r.optLong("amount"));total+=n;max=Math.max(max,n);}paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(Ui.dp(getContext(),12));
        if(total==0){paint.setColor(0xff999999);canvas.drawText("暂无数据",w/2,h/2,paint);return;}
        if(mode.equals("pie")){float radius=Math.min(w,h)*.40f;RectF rect=new RectF(w/2-radius,h/2-radius,w/2+radius,h/2+radius);float angle=-90;for(int i=0;i<data.size();i++){float sweep=360f*Math.max(0,data.get(i).optLong("amount"))/total;paint.setColor(colors[i%colors.length]);canvas.drawArc(rect,angle,sweep,true,paint);angle+=sweep;}paint.setColor(Ui.color(getContext(),"white",Color.WHITE));canvas.drawCircle(w/2,h/2,radius*.62f,paint);paint.setColor(Ui.color(getContext(),"black",Color.BLACK));paint.setTextSize(Ui.dp(getContext(),15));canvas.drawText(Money.text(total,currency),w/2,h/2+Ui.dp(getContext(),5),paint);return;}
        float usable=h-pad*2,step=(w-pad*2)/Math.max(1,data.size());Path line=new Path();
        for(int i=0;i<data.size();i++){JSONObject r=data.get(i);float x=pad+step*(i+.5f),y=h-pad-usable*Math.max(0,r.optLong("amount"))/max;paint.setColor(Ui.GREEN);if(mode.equals("line")){if(i==0)line.moveTo(x,y);else line.lineTo(x,y);canvas.drawCircle(x,y,Ui.dp(getContext(),2),paint);}else canvas.drawRoundRect(x-step*.28f,y,x+step*.28f,h-pad,Ui.dp(getContext(),3),Ui.dp(getContext(),3),paint);if(data.size()<=12||i%Math.max(1,data.size()/7)==0){paint.setColor(0xff999999);paint.setTextSize(Ui.dp(getContext(),9));canvas.drawText(r.optString("name"),x,h-Ui.dp(getContext(),5),paint);}}
        if(mode.equals("line")){paint.setColor(Ui.GREEN);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Ui.dp(getContext(),2));canvas.drawPath(line,paint);paint.setStyle(Paint.Style.FILL);}
    }
}
