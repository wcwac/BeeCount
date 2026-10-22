package com.wangc.bill.reference.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.core.content.FileProvider;
import com.wangc.bill.reference.*;
import com.wangc.bill.reference.capture.AutoApps;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;

public final class LocalFiles {
    private static final int MAX_ATTACHMENT=16*1024*1024,MAX_JSON=16*1024*1024,MAX_ARCHIVE=128*1024*1024;
    private static final String SAFE_FILE="[A-Za-z0-9_-]{20,60}\\.(jpg|png|webp|pdf|txt|csv)";
    private LocalFiles(){}
    private static File folder(Context c){File dir=new File(c.getFilesDir(),"attachments");if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException("无法创建附件目录");return dir;}
    private static File file(Context c,String name){if(name==null||!name.matches(SAFE_FILE))throw new IllegalArgumentException("附件名称无效");return new File(folder(c),name);}
    private static long copy(InputStream input,OutputStream output,long limit)throws IOException{byte[] buffer=new byte[8192];long total=0;int n;while((n=input.read(buffer))!=-1){total+=n;if(total>limit)throw new IOException("文件超出允许大小");output.write(buffer,0,n);}return total;}
    public static JSONObject importAttachment(Context context,Uri uri)throws Exception{
        String mime=context.getContentResolver().getType(uri);if(mime==null)mime="application/octet-stream";
        String extension=switch(mime){case "image/jpeg"->"jpg";case "image/png"->"png";case "image/webp"->"webp";case "application/pdf"->"pdf";case "text/plain"->"txt";case "text/csv"->"csv";default->throw new IllegalArgumentException("附件支持图片、PDF和文本文件");};
        String title="附件."+extension;try(android.database.Cursor cursor=context.getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){if(cursor!=null&&cursor.moveToFirst())title=cursor.getString(0);}
        String name=UUID.randomUUID()+"."+extension;File target=file(context,name);try(InputStream input=context.getContentResolver().openInputStream(uri);OutputStream output=new FileOutputStream(target)){if(input==null)throw new IOException("无法读取附件");copy(input,output,MAX_ATTACHMENT);}catch(Exception e){target.delete();throw e;}
        return Json.object("file",name,"name",title==null?"附件":title.substring(0,Math.min(150,title.length())),"mime",mime);
    }
    public static void openAttachment(Context context,JSONObject item){try{File f=file(context,item.optString("file"));if(!f.isFile())throw new IllegalArgumentException("附件文件不存在");Uri uri=FileProvider.getUriForFile(context,context.getPackageName()+".files",f);context.startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(uri,item.optString("mime","application/octet-stream")).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));}catch(Exception e){Ui.error(context,e);}}
    public static void exportBackup(Context context,Uri destination)throws Exception{
        JSONObject backup=ReferenceApplication.store().backup();Set<String> attachments=attachmentNames(backup);byte[] json=backup.toString().getBytes(StandardCharsets.UTF_8);if(json.length>MAX_JSON)throw new IllegalArgumentException("数据量超过实验版单个备份上限");
        OutputStream stream=context.getContentResolver().openOutputStream(destination,"wt");if(stream==null)throw new IOException("无法写入文件");long total=json.length;
        try(ZipOutputStream zip=new ZipOutputStream(stream)){
            zip.putNextEntry(new ZipEntry("data.json"));zip.write(json);zip.closeEntry();
            for(String name:attachments){File source=file(context,name);if(!source.isFile())throw new IOException("部分附件已丢失，请先核对附件列表");zip.putNextEntry(new ZipEntry("attachments/"+name));try(InputStream in=new FileInputStream(source)){total+=copy(in,zip,MAX_ATTACHMENT);}zip.closeEntry();if(total>MAX_ARCHIVE)throw new IOException("备份附件总量超过128MB");}
        }
    }
    public static void restoreBackup(Context context,Uri uri)throws Exception{
        File staging=new File(context.getCacheDir(),"restore-"+UUID.randomUUID());if(!staging.mkdirs())throw new IOException("无法准备恢复目录");
        List<File> moved=new ArrayList<>();boolean committed=false;try{
            JSONObject backup=null;Set<String> entries=new HashSet<>();Map<String,File> pending=new HashMap<>();long total=0;int count=0;
            InputStream input=context.getContentResolver().openInputStream(uri);if(input==null)throw new IOException("无法读取备份");
            try(ZipInputStream zip=new ZipInputStream(input)){
                ZipEntry entry;while((entry=zip.getNextEntry())!=null){String name=entry.getName();if(++count>10001||!entries.add(name))throw new IOException("备份条目过多或存在重复条目");
                    if(entry.isDirectory()){if(!name.equals("attachments/"))throw new IOException("备份目录无效");continue;}
                    if(name.equals("data.json")){ByteArrayOutputStream bytes=new ByteArrayOutputStream();total+=copy(zip,bytes,MAX_JSON);backup=Json.parse(bytes.toString(StandardCharsets.UTF_8.name()));}
                    else if(name.startsWith("attachments/")&&name.substring(12).matches(SAFE_FILE)){String filename=name.substring(12);File target=new File(staging,filename);try(OutputStream out=new FileOutputStream(target)){total+=copy(zip,out,MAX_ATTACHMENT);}pending.put(filename,target);}
                    else throw new IOException("备份包含不支持的文件条目");
                    if(total>MAX_ARCHIVE)throw new IOException("备份解压后超过128MB");zip.closeEntry();
                }
            }
            if(backup==null)throw new IOException("备份缺少账单数据");Set<String> referenced=attachmentNames(backup);Map<String,String> names=new HashMap<>();
            for(String original:referenced){File staged=pending.get(original);if(staged==null)throw new IOException("备份缺少引用的附件");String renamed=UUID.randomUUID()+original.substring(original.lastIndexOf('.'));File target=file(context,renamed);if(!staged.renameTo(target))throw new IOException("无法恢复附件文件");moved.add(target);names.put(original,renamed);}
            rewriteAttachments(backup,names);
            AutoApps.enable(false);CaptureOverlay.close();ReferenceApplication.store().restore(backup);committed=true;
            long book=ReferenceApplication.store().books().get(0).optLong("id");android.content.SharedPreferences.Editor preferences=ReferenceApplication.prefs().edit().putLong("current_book",book).putLong("capture_book",book).putLong("default_account",0).putBoolean("auto_save",false);
            for(String key:ReferenceApplication.prefs().getAll().keySet())if(key.startsWith("source_tag_"))preferences.remove(key);preferences.commit();
        }finally{File[] staged=staging.listFiles();if(staged!=null)for(File f:staged)f.delete();staging.delete();if(!committed)for(File f:moved)f.delete();}
    }
    private static Set<String> attachmentNames(JSONObject backup){Set<String> names=new HashSet<>();JSONObject tables=backup.optJSONObject("tables");if(tables==null)throw new IllegalArgumentException("备份格式错误");JSONArray rows=tables.optJSONArray("transactions");if(rows==null)throw new IllegalArgumentException("备份缺少账单表");for(int i=0;i<rows.length();i++){JSONObject row=rows.optJSONObject(i);if(row==null)throw new IllegalArgumentException("账单格式错误");JSONArray items=Json.array(row.optString("attachments","[]"));if(items.length()>100)throw new IllegalArgumentException("单条账单的附件过多");for(int j=0;j<items.length();j++){JSONObject item=items.optJSONObject(j);if(item==null||!item.optString("file").matches(SAFE_FILE))throw new IllegalArgumentException("附件路径无效");names.add(item.optString("file"));}}return names;}
    private static void rewriteAttachments(JSONObject backup,Map<String,String> names){JSONArray rows=backup.optJSONObject("tables").optJSONArray("transactions");for(int i=0;i<rows.length();i++){JSONObject row=rows.optJSONObject(i);JSONArray files=Json.array(row.optString("attachments","[]"));for(int j=0;j<files.length();j++){JSONObject item=files.optJSONObject(j);Json.put(item,"file",names.get(item.optString("file")));}Json.put(row,"attachments",files.toString());}}
    public static String readText(Context context,Uri uri)throws Exception{InputStream in=context.getContentResolver().openInputStream(uri);if(in==null)throw new IOException("无法读取文件");try(in;ByteArrayOutputStream out=new ByteArrayOutputStream()){copy(in,out,MAX_JSON);return out.toString(StandardCharsets.UTF_8.name());}}
    public static void writeText(Context context,Uri uri,String text)throws Exception{OutputStream out=context.getContentResolver().openOutputStream(uri,"wt");if(out==null)throw new IOException("无法写入文件");try(out){out.write(text.getBytes(StandardCharsets.UTF_8));}}
    public static String hash(String text){try{byte[] value=MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte b:value)s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}catch(java.security.NoSuchAlgorithmException e){throw new AssertionError(e);}}
}
