package com.wangc.bill.reference.ui;

import com.wangc.bill.reference.*;
import org.json.JSONObject;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

public final class CsvFiles {
    private CsvFiles(){}
    public static String exportBook(long book){ReferenceStore store=ReferenceApplication.store();List<List<String>> rows=new ArrayList<>();rows.add(Arrays.asList("时间","类型","金额","币种","分类","账户","转入账户","备注","商户","来源","优惠","手续费"));
        if(store.scalar("SELECT COUNT(*) n FROM transactions WHERE book_id=? AND deleted=0",""+book)>100000)throw new IllegalArgumentException("CSV单次最多导出十万条，请改用完整备份");
        for(JSONObject value:store.query("SELECT t.*,c.name category,a.name account FROM transactions t LEFT JOIN categories c ON c.id=t.category_id LEFT JOIN accounts a ON a.id=t.account_id WHERE t.book_id=? AND t.deleted=0 ORDER BY t.time DESC,t.id DESC",""+book)){
            JSONObject destination=store.one("SELECT name FROM accounts WHERE id=?",""+value.optLong("to_account_id"));String c=value.optString("currency");rows.add(Arrays.asList(Ui.date(value.optLong("time"),"yyyy-MM-dd HH:mm:ss"),value.optString("kind").equals("income")?"收入":value.optString("kind").equals("transfer")?"转账":"支出",Money.text(value.optLong("amount"),c),c,value.optString("category","其他"),value.optString("account",""),destination==null?"":destination.optString("name"),value.optString("remark"),value.optString("merchant"),value.optString("origin"),Money.text(value.optLong("discount"),c),Money.text(value.optLong("fee"),c)));
        }return CsvCodec.write(rows);
    }
    public static int[] importBook(String text,long book){ReferenceStore store=ReferenceApplication.store();JSONObject ledger=store.book(book);if(ledger==null)throw new IllegalArgumentException("目标账本不存在");List<List<String>> rows=CsvCodec.parse(text);if(rows.size()<2)throw new IllegalArgumentException("CSV需要表头和至少一条账单");List<String> headers=rows.get(0);int time=column(headers,"时间","日期","记账时间","交易时间"),amount=column(headers,"金额","交易金额"),kind=column(headers,"类型","收支类型","收支"),category=column(headers,"分类","一级分类"),account=column(headers,"账户","付款账户"),to=column(headers,"转入账户"),note=column(headers,"备注","说明"),merchant=column(headers,"商户","商家"),currency=column(headers,"币种"),origin=column(headers,"来源"),discount=column(headers,"优惠"),fee=column(headers,"手续费");if(time<0||amount<0||kind<0)throw new IllegalArgumentException("CSV必须有时间、类型、金额列");
        synchronized(store){String fingerprint=LocalFiles.hash(text);android.database.sqlite.SQLiteDatabase db=store.getWritableDatabase();int added=0,skipped=0;db.beginTransaction();try{
            for(int index=1;index<rows.size();index++)try{
                List<String> row=rows.get(index);String importKey=LocalFiles.hash(book+":"+fingerprint+":"+index);if(store.one("SELECT id FROM transactions WHERE import_key=?",importKey)!=null){skipped++;continue;}
                String k=cell(row,kind).trim();k=k.equals("收入")||k.equals("income")?"income":k.equals("支出")||k.equals("expense")?"expense":k.equals("转账")||k.equals("transfer")?"transfer":"";if(k.isEmpty())throw new IllegalArgumentException("类型需为收入、支出或转账");
                String cur=cell(row,currency).trim();if(cur.isEmpty())cur=ledger.optString("currency");if(!cur.equals(ledger.optString("currency")))throw new IllegalArgumentException("币种与目标账本不同，请分别导入");
                String label=cell(row,category).trim();long categoryId=store.defaultCategory(k.equals("transfer")?"expense":k);if(!label.isEmpty()&&!k.equals("transfer")){JSONObject c=store.one("SELECT id FROM categories WHERE name=? AND kind=? AND archived=0",label,k);categoryId=c==null?store.saveCategory(0,label,k,k.equals("income")?"ic_income_other":"ic_parent_other"):c.optLong("id");}
                long accountId=accountId(store,cell(row,account),cur),destinationId=accountId(store,cell(row,to),cur);long when=parseDate(cell(row,time));
                JSONObject data=Json.object("book_id",book,"kind",k,"amount",Money.positive(cell(row,amount),cur),"category_id",categoryId,"account_id",accountId,"to_account_id",destinationId,"remark",cell(row,note),"merchant",cell(row,merchant),"origin",cell(row,origin),"time",when,"import_key",importKey);
                if(!cell(row,discount).isEmpty())Json.put(data,"discount",Money.signed(cell(row,discount),cur));if(!cell(row,fee).isEmpty())Json.put(data,"fee",Money.signed(cell(row,fee),cur));store.saveLegacyImport(data);added++;
            }catch(Exception e){throw new IllegalArgumentException("第"+(index+1)+"行："+(e.getMessage()==null?"数据无效":e.getMessage()));}
            db.setTransactionSuccessful();return new int[]{added,skipped};
        }finally{db.endTransaction();}}
    }
    private static int column(List<String> headers,String... options){for(String option:options)for(int i=0;i<headers.size();i++)if(option.equals(headers.get(i).trim()))return i;return -1;}
    private static String cell(List<String> row,int at){return at<0||at>=row.size()?"":row.get(at);}
    private static long accountId(ReferenceStore store,String raw,String currency){String name=raw.trim();if(name.isEmpty()||name.equals("null")||name.equals("无账户"))return 0;JSONObject row=store.one("SELECT id FROM accounts WHERE name=? AND currency=? AND archived=0",name,currency);return row==null?store.saveAccount(0,name,"cash",currency,0):row.optLong("id");}
    private static long parseDate(String raw){for(String pattern:new String[]{"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm","yyyy-MM-dd","yyyy/MM/dd HH:mm:ss","yyyy/MM/dd"}){ParsePosition p=new ParsePosition(0);SimpleDateFormat f=new SimpleDateFormat(pattern,Locale.CHINA);f.setLenient(false);java.util.Date d=f.parse(raw.trim(),p);if(d!=null&&p.getIndex()==raw.trim().length())return d.getTime();}throw new IllegalArgumentException("时间格式无效");}
}
