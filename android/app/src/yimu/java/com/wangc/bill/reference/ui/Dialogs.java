package com.wangc.bill.reference.ui;
import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import com.wangc.bill.reference.Money;

final class Dialogs {
    interface Text{void value(String value);}
    static void input(Context context,String title,String value,boolean numeric,Text result){EditText field=new EditText(context);field.setText(value);field.setPadding(Ui.dp(context,20),Ui.dp(context,15),Ui.dp(context,20),Ui.dp(context,15));if(numeric)field.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);field.selectAll();AlertDialog dialog=new AlertDialog.Builder(context).setTitle(title).setView(field).setNegativeButton("取消",null).setPositiveButton("保存",null).create();dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{result.value(field.getText().toString());dialog.dismiss();}catch(Exception e){Ui.error(context,e);}}));dialog.show();}
    static void currencies(Context context,String selected,Text result){String[] codes={"CNY","USD","HKD","EUR","GBP","JPY","AUD","CAD","SGD","KRW","TWD"};int at=0;for(int i=0;i<codes.length;i++)if(codes[i].equals(selected))at=i;new AlertDialog.Builder(context).setTitle("选择币种").setSingleChoiceItems(codes,at,(d,w)->{result.value(codes[w]);d.dismiss();}).setNegativeButton("取消",null).show();}
}
