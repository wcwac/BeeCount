package com.wangc.bill.reference.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import com.wangc.bill.reference.*;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/** Local entry form for a consumption Bill's actual credit projection. No second principal entry. */
public final class InstalmentScreens {
    private InstalmentScreens(){}
    public static boolean available(long billRow){return new AccountingInstalments(ReferenceApplication.store()).eligible(billRow);}
    public static void show(Context context,long billRow){
        try{
            AccountingInstalments commands=new AccountingInstalments(ReferenceApplication.store());JSONObject current=commands.current(billRow);
            LinearLayout fields=Ui.column(context);int pad=Ui.dp(context,18);fields.setPadding(pad,pad,pad,pad);
            TextView notice=Ui.label(context,"信用账户："+current.optString("account_name")+"\n仅改变本笔消费的还款安排，不再次扣除本金。手续费加入各期信用账单；不会另建重复消费。",14);fields.addView(notice);
            EditText amount=number(context,fields,"分期本金（不超过 "+Money.text(current.optLong("maximum"),"CNY")+"）",Money.text(current.optLong("amount"),"CNY"),true);
            EditText count=number(context,fields,"期数（1—120）",""+current.optInt("periods"),false);
            String[] firstMonth={current.optString("first_month")};TextView first=Ui.label(context,"",16);first.setPadding(0,pad,0,pad);fields.addView(first);
            Runnable renderMonth=()->first.setText("首次账期："+firstMonth[0]+"　点击修改");renderMonth.run();
            first.setOnClickListener(view->{String[] parts=firstMonth[0].split("-");Calendar now=Calendar.getInstance();int year=parts.length==2?Integer.parseInt(parts[0]):now.get(Calendar.YEAR),month=parts.length==2?Integer.parseInt(parts[1])-1:now.get(Calendar.MONTH);
                new DatePickerDialog(context,(picker,y,m,d)->{firstMonth[0]=String.format(Locale.ROOT,"%04d-%02d",y,m+1);renderMonth.run();},year,month,1).show();});
            Spinner mode=new Spinner(context);mode.setAdapter(new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item,new String[]{"每期收取手续费","仅首期收取一次手续费"}));mode.setSelection(current.optString("fee_mode").equals("each")?0:1);fields.addView(mode);
            EditText fee=number(context,fields,"手续费金额（选择每期时，填写每期金额）",Money.text(current.optLong("fee"),"CNY"),true);
            fields.addView(Ui.label(context,"金额尾差归最后一期。首次账期按信用账户的账单日和还款日计算；保存前可查看完整计划。",13));
            if(current.optBoolean("custom"))fields.addView(Ui.label(context,"原计划手续费合计 "+Money.text(current.optLong("total_fee"),"CNY")+"。原计划含自定义或无法按整分均分的金额；当前表单会重新生成，请预览并核对新的手续费合计。",13));
            ScrollView scroll=new ScrollView(context);scroll.addView(fields);
            AlertDialog dialog=new AlertDialog.Builder(context).setTitle(current.optBoolean("exists")?"管理消费分期":"设置消费分期").setView(scroll)
                    .setNegativeButton("取消",null).setNeutralButton("预览计划",null).setPositiveButton("保存分期",null).create();
            if(current.optBoolean("exists")){
                TextView remove=Ui.label(context,"移除此笔消费的分期",16);remove.setTextColor(0xffb3261e);remove.setPadding(0,pad,0,pad);fields.addView(remove);
                remove.setOnClickListener(view->Ui.confirm(context,"移除分期","恢复为普通信用消费账期。原消费和已记录的还款均保留，不重复改动本金余额。",()->{
                    commands.remove(billRow);dialog.dismiss();Ui.refresh(context);Ui.toast(context,"已移除分期安排");
                }));
            }
            dialog.setOnShowListener(ignored->{
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view->{try{preview(context,commands.preview(billRow,input(amount,count,fee,mode,firstMonth[0])));}catch(Exception error){Ui.error(context,error);}});
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view->{try{
                    JSONObject value=input(amount,count,fee,mode,firstMonth[0]);JSONObject plan=commands.preview(billRow,value);
                    new AlertDialog.Builder(context).setTitle("确认分期计划").setMessage("本金 "+Money.text(plan.optLong("amount"),"CNY")+"，手续费合计 "+Money.text(plan.optLong("total_fee"),"CNY")+"，共 "+plan.optJSONArray("periods").length()+" 期。\n原消费金额与账户本金余额保持不变。")
                            .setNegativeButton("返回修改",null).setPositiveButton("确认保存",(confirm,which)->{try{commands.save(billRow,value);dialog.dismiss();Ui.refresh(context);Ui.toast(context,"分期已保存，信用账单正在刷新");}catch(Exception error){Ui.error(context,error);}}).show();
                }catch(Exception error){Ui.error(context,error);}});
            });dialog.show();
        }catch(Exception error){Ui.error(context,error);}
    }
    /** Account-level entry for statements containing only instalments or synthetic credit rows. */
    public static void chooseConsumption(Context context,long accountRow){
        try{List<JSONObject> rows=new AccountingInstalments(ReferenceApplication.store()).consumptionBills(accountRow);
            if(rows.isEmpty()){Ui.toast(context,"此信用账户暂无可分期的本币消费，请先记录一笔普通消费");return;}
            String[] labels=new String[rows.size()];for(int i=0;i<labels.length;i++){JSONObject row=rows.get(i);labels[i]=Ui.date(row.optLong("time"),"yyyy-MM-dd")+" · "+Money.text(row.optLong("amount"),"CNY")+" · "+row.optString("category")+(row.optString("remark").isEmpty()?"":" · "+row.optString("remark"));}
            new AlertDialog.Builder(context).setTitle("选择消费账单（最近300条）").setItems(labels,(dialog,index)->show(context,rows.get(index).optLong("id"))).setNegativeButton("取消",null).show();
        }catch(Exception error){Ui.error(context,error);}
    }
    /** Credit detail rows carry raw Bill IDs, never transactions PKs. */
    public static void openCreditItem(Context context,int semantic,int owner,int bookOwner){
        try{if(semantic<=0){Ui.toast(context,"此条为还款或汇总明细，请从具体消费账单设置分期");return;}
            OriginalBillRecord raw=new OriginalBillRepository(ReferenceApplication.store()).findAccessible(owner,bookOwner,semantic);
            if(raw==null){Ui.toast(context,"原消费账单已不存在");return;}show(context,raw.localRowId);
        }catch(Exception error){Ui.error(context,error);}
    }
    private static EditText number(Context context,LinearLayout parent,String hint,String value,boolean decimal){parent.addView(Ui.label(context,hint,13));EditText input=new EditText(context);input.setSingleLine(true);input.setHint(hint);input.setContentDescription(hint);input.setInputType(InputType.TYPE_CLASS_NUMBER|(decimal?InputType.TYPE_NUMBER_FLAG_DECIMAL:0));input.setText(value);parent.addView(input);return input;}
    private static JSONObject input(EditText amount,EditText count,EditText fee,Spinner mode,String month){
        int periods;try{periods=Integer.parseInt(count.getText().toString().trim());}catch(NumberFormatException invalid){throw new IllegalArgumentException("请输入有效期数");}
        return Json.object("amount",Money.positive(amount.getText().toString(),"CNY"),"periods",periods,"fee",Money.signed(fee.getText().toString(),"CNY"),"fee_mode",mode.getSelectedItemPosition()==0?"each":"once","first_month",month);
    }
    private static void preview(Context context,JSONObject value){
        LinearLayout rows=Ui.column(context);int pad=Ui.dp(context,18);rows.setPadding(pad,pad,pad,pad);
        rows.addView(Ui.label(context,"本金 "+Money.text(value.optLong("amount"),"CNY")+"　手续费 "+Money.text(value.optLong("total_fee"),"CNY")+"\n合计 "+Money.text(value.optLong("total"),"CNY"),16));
        JSONArray periods=value.optJSONArray("periods");for(int i=0;i<periods.length();i++){JSONObject part=periods.optJSONObject(i);
            rows.addView(Ui.row(context,"第 "+part.optInt("period")+" 期 · "+part.optString("month")+" · "+Money.text(part.optLong("amount"),"CNY"),"本金 "+Money.text(part.optLong("principal"),"CNY")+" + 手续费 "+Money.text(part.optLong("fee"),"CNY")+"\n还款日 "+part.optString("repayment_date"),null));}
        ScrollView scroll=new ScrollView(context);scroll.addView(rows);new AlertDialog.Builder(context).setTitle("分期计划预览").setView(scroll).setPositiveButton("关闭",null).show();
    }
}
