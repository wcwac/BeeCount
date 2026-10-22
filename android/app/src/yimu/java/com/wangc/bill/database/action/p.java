package com.wangc.bill.database.action;
import java.util.List;
import java.util.ArrayList;
import com.wangc.bill.reference.ReferenceApplication;
public final class p {
 public static List<Long> d(String source) { String value=ReferenceApplication.prefs().getString("source_tag_"+source,"");List<Long> result=new ArrayList<>();if(value!=null&&!value.isEmpty())try{result.add(Long.parseLong(value));}catch(NumberFormatException ignored){}return result; }
}
