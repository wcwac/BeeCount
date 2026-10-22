package com.wangc.bill.reference;

import java.util.ArrayList;
import java.util.List;

/** RFC4180-style local CSV. Cell data is never interpreted as code or formulas. */
public final class CsvCodec {
    private CsvCodec(){}
    public static List<List<String>> parse(String text){
        if(text==null||text.length()>16*1024*1024)throw new IllegalArgumentException("CSV文件过大");
        if(text.startsWith("\uFEFF"))text=text.substring(1);
        List<List<String>> rows=new ArrayList<>();List<String> row=new ArrayList<>();StringBuilder cell=new StringBuilder();boolean quoted=false,closed=false;
        for(int i=0;i<text.length();i++){char c=text.charAt(i);
            if(quoted){if(c=='"'){if(i+1<text.length()&&text.charAt(i+1)=='"'){cell.append('"');i++;}else{quoted=false;closed=true;}}else cell.append(c);}
            else if(c=='"'){if(cell.length()!=0||closed)throw new IllegalArgumentException("CSV引号位置无效");quoted=true;}
            else if(c==','){row.add(cell.toString());cell.setLength(0);closed=false;}
            else if(c=='\r'||c=='\n'){if(c=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;row.add(cell.toString());cell.setLength(0);closed=false;if(row.size()>1||!row.get(0).isEmpty())rows.add(row);row=new ArrayList<>();}
            else{if(closed&&!Character.isWhitespace(c))throw new IllegalArgumentException("CSV字段结束后有多余字符");if(!closed)cell.append(c);}
            if(cell.length()>100000||row.size()>100||rows.size()>100000)throw new IllegalArgumentException("CSV字段或行数过多");
        }
        if(quoted)throw new IllegalArgumentException("CSV引号未闭合");if(cell.length()>0||!row.isEmpty()||closed){row.add(cell.toString());rows.add(row);}return rows;
    }
    public static String write(List<List<String>> rows){StringBuilder out=new StringBuilder("\uFEFF");for(List<String> row:rows){for(int i=0;i<row.size();i++){if(i>0)out.append(',');String s=row.get(i)==null?"":row.get(i);out.append('"').append(s.replace("\"","\"\"")).append('"');}out.append("\r\n");}return out.toString();}
}
