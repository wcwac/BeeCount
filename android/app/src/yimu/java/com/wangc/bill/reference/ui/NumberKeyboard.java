package com.wangc.bill.reference.ui;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;

/** Uses the recovered NumberKeyboardView's actual layout and key order. */
public final class NumberKeyboard extends LinearLayout {
 public interface Listener { void key(String key); }
 private Listener listener;
 private final View keys;
 public NumberKeyboard(Context c,AttributeSet a){super(c,a);setOrientation(VERTICAL);keys=Ui.layout(c,"layout_number_input");addView(keys,new LayoutParams(-1,Ui.dp(c,220)));
  String[][] mapping={{"one","1"},{"two","2"},{"three","3"},{"four","4"},{"five","5"},{"six","6"},{"seven","7"},{"eight","8"},{"nine","9"},{"zero","0"},{"point","."},{"add","+"},{"reduce","-"},{"ride","×"},{"except","÷"},{"delete_one","⌫"},{"add_bill_again","再记"}};
  for(String[] pair:mapping)Ui.click(keys,pair[0],()->{if(listener!=null)listener.key(pair[1]);});
  Ui.click(keys,"cancel",()->{if(listener!=null)listener.key(((TextView)Ui.view(keys,"cancel")).getText().toString());});
  View delete=Ui.view(keys,"delete_one");if(delete!=null)delete.setOnLongClickListener(v->{if(listener!=null)listener.key("清空");return true;});
 }
 public void setListener(Listener l){listener=l;}
 public void expression(String value){boolean calculation=value.matches(".*[+×÷].*")||value.indexOf('-',1)>=0;Ui.text(keys,"cancel",calculation?"=":"完成");}
}
