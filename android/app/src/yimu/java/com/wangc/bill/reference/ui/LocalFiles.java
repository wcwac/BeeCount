package com.wangc.bill.reference.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.core.content.FileProvider;
import com.wangc.bill.reference.*;
import com.wangc.bill.reference.capture.AutoApps;
import com.wangc.bill.reference.capture.YimuKernel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LocalFiles {
    private static final int MAX_ATTACHMENT=16*1024*1024,MAX_JSON=16*1024*1024,MAX_ARCHIVE=128*1024*1024;
    private static final String SAFE_FILE="[A-Za-z0-9_-]{20,60}\\.(jpg|png|webp|pdf|txt|csv)";
    private LocalFiles(){}
    private static File folder(Context c){File dir=new File(c.getFilesDir(),"attachments");if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException("无法创建附件目录");return dir;}
    private static File file(Context c,String name){if(name==null||!name.matches(SAFE_FILE))throw new IllegalArgumentException("附件名称无效");return new File(folder(c),name);}
    private static long copy(InputStream input,OutputStream output,long limit)throws IOException{byte[] buffer=new byte[8192];long total=0;int n;while((n=input.read(buffer))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("文件操作已中止");total+=n;if(total>limit)throw new IOException("文件超出允许大小");output.write(buffer,0,n);}return total;}
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
        if(attachments.size()>10000)throw new IllegalArgumentException("备份附件超过一万个，请先减少附件或分批迁移");
        OutputStream stream=context.getContentResolver().openOutputStream(destination,"wt");if(stream==null)throw new IOException("无法写入文件");long total=json.length;
        try(ZipOutputStream zip=new ZipOutputStream(stream)){
            zip.putNextEntry(new ZipEntry("data.json"));zip.write(json);zip.closeEntry();
            for(String name:attachments){File source=file(context,name);if(!source.isFile())throw new IOException("部分附件已丢失，请先核对附件列表");zip.putNextEntry(new ZipEntry("attachments/"+name));try(InputStream in=new FileInputStream(source)){total+=copy(in,zip,MAX_ATTACHMENT);}zip.closeEntry();if(total>MAX_ARCHIVE)throw new IOException("备份附件总量超过128MB");}
        }
    }
    public static void restoreBackup(Context context,Uri uri)throws Exception{
        if(android.os.Looper.myLooper()==android.os.Looper.getMainLooper())throw new IllegalStateException("备份恢复需要在文件工作线程执行");
        if(YimuKernel.isWorkerThread())throw new IllegalStateException("备份恢复不能在采集工作线程中等待自身任务");
        File staging=new File(context.getCacheDir(),"restore-"+UUID.randomUUID());if(!staging.mkdirs())throw new IOException("无法准备恢复目录");
        List<File> moved=new ArrayList<>();AtomicBoolean committed=new AtomicBoolean();try{
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
            AutoApps.enable(false);CaptureOverlay.closeForRestore();
            JSONObject restored=backup;
            FutureTask<Void> operation=new FutureTask<>(()->{
                ReferenceApplication.store().restore(restored);committed.set(true);
                // Local row IDs can identify different assets after database replacement.
                // Clear only after the DB commit succeeds, before parser/cache cleanup can fail.
                ReferenceApplication.prefs().edit().remove("capture_account_mappings").commit();
                YimuKernel.resetAfterRestore();
                long book=ReferenceApplication.store().books().get(0).optLong("id");android.content.SharedPreferences.Editor preferences=ReferenceApplication.prefs().edit().putLong("current_book",book).putLong("capture_book",book).putLong("default_account",0).putBoolean("auto_save",false);
                for(String key:ReferenceApplication.prefs().getAll().keySet())if(key.startsWith("source_tag_"))preferences.remove(key);preferences.commit();
                OriginalFinancialEffects.bookSelectionChanged(ReferenceApplication.store());
                return null;
            });
            // Capture, window saves, remark callbacks and plans use this same serial worker.
            // Finish any in-flight transaction before replacing the database; old queued
            // window saves are invalidated by close() and cannot write after restoration.
            YimuKernel.enqueue(operation);
            boolean interrupted=false;
            try{
                for(;;)try{operation.get();break;}catch(InterruptedException error){interrupted=true;}
            }catch(ExecutionException error){
                Throwable cause=error.getCause();if(cause instanceof Exception failure)throw failure;if(cause instanceof Error failure)throw failure;throw new IOException("无法恢复账单数据",cause);
            }finally{if(interrupted)Thread.currentThread().interrupt();}
        }finally{File[] staged=staging.listFiles();if(staged!=null)for(File f:staged)f.delete();staging.delete();if(!committed.get())for(File f:moved)f.delete();}
    }
    private interface AttachmentVisitor { boolean visit(JSONObject record); }
    /** Exact host attachment fields, shared by collection and rename. No recursive guessing. */
    private static void visitAttachmentRecords(JSONObject backup,AttachmentVisitor visitor){
        JSONObject tables=backup.optJSONObject("tables");
        if(tables==null)throw new IllegalArgumentException("备份格式错误");
        JSONArray bills=tables.optJSONArray("transactions");
        if(bills==null)throw new IllegalArgumentException("备份缺少账单表");
        for(int i=0;i<bills.length();i++){
            JSONObject row=bills.optJSONObject(i);if(row==null)throw new IllegalArgumentException("账单格式错误");
            visitor.visit(row);
        }
        // Pending/saved typed candidates and manual typed operation receipts hold JSON text.
        // Plans can retain the same attachment fields for future generated candidates.
        for(String table:new String[]{"candidates","accounting_operations","accounting_financial_operations","plans"}){
            // Match the Store's versioned table whitelist: an older archive cannot
            // smuggle file references through an unrecognized operations table.
            if(table.equals("accounting_operations")&&backup.optInt("version")<9)continue;
            if(table.equals("accounting_financial_operations")&&backup.optInt("version")<10)continue;
            JSONArray rows=tables.optJSONArray(table);
            if(rows==null)throw new IllegalArgumentException("备份记录表格式错误");
            for(int i=0;i<rows.length();i++){
                JSONObject row=rows.optJSONObject(i);
                if(row==null||!(row.opt("payload") instanceof String))throw new IllegalArgumentException("备份记录内容格式错误");
                JSONObject payload=Json.parse(row.optString("payload"));
                if(visitor.visit(payload))Json.put(row,"payload",payload.toString());
            }
        }
    }
    private static JSONArray attachmentItems(JSONObject record){
        Object value=record.opt("attachments");
        if(value==null||value==JSONObject.NULL)return null;
        JSONArray items=value instanceof JSONArray?(JSONArray)value:value instanceof String?Json.array((String)value):null;
        if(items==null)throw new IllegalArgumentException("附件列表格式错误");
        if(items.length()>100)throw new IllegalArgumentException("单条记录的附件过多");
        for(int i=0;i<items.length();i++){
            JSONObject item=items.optJSONObject(i);
            if(item==null||!item.optString("file").matches(SAFE_FILE))throw new IllegalArgumentException("附件路径无效");
        }
        return items;
    }
    private static Set<String> attachmentNames(JSONObject backup){
        Set<String> names=new HashSet<>();
        visitAttachmentRecords(backup,record->{
            JSONArray items=attachmentItems(record);
            if(items!=null)for(int i=0;i<items.length();i++)names.add(items.optJSONObject(i).optString("file"));
            return false;
        });
        return names;
    }
    private static void rewriteAttachments(JSONObject backup,Map<String,String> names){
        visitAttachmentRecords(backup,record->{
            JSONArray items=attachmentItems(record);if(items==null||items.length()==0)return false;
            boolean arrayValue=record.opt("attachments") instanceof JSONArray;
            for(int i=0;i<items.length();i++){
                JSONObject item=items.optJSONObject(i);String replacement=names.get(item.optString("file"));
                if(replacement==null||!replacement.matches(SAFE_FILE))throw new IllegalArgumentException("备份缺少附件名称映射");
                Json.put(item,"file",replacement);
            }
            // Keep the array/string encoding expected by each financial entry, then
            // write its enclosing payload string back through visitAttachmentRecords.
            Json.put(record,"attachments",arrayValue?items:items.toString());return true;
        });
    }
    public static String readText(Context context,Uri uri)throws Exception{InputStream in=context.getContentResolver().openInputStream(uri);if(in==null)throw new IOException("无法读取文件");try(in;ByteArrayOutputStream out=new ByteArrayOutputStream()){copy(in,out,MAX_JSON);return out.toString(StandardCharsets.UTF_8.name());}}
    public static void writeText(Context context,Uri uri,String text)throws Exception{OutputStream out=context.getContentResolver().openOutputStream(uri,"wt");if(out==null)throw new IOException("无法写入文件");try(out){out.write(text.getBytes(StandardCharsets.UTF_8));}}
    public static String hash(String text){try{byte[] value=MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte b:value)s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}catch(java.security.NoSuchAlgorithmException e){throw new AssertionError(e);}}
}
