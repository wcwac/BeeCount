package com.wangc.bill.reference;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.wangc.bill.R;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.json.JSONObject;

/** Local category suggestions. No network, membership or main-thread model loading. */
public final class LocalCategoryClassifier {
    private static final Object LOAD=new Object();
    private static volatile RecoveredCategoryModel model;
    private static long retryAfter;
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final ThreadPoolExecutor WORKER=new ThreadPoolExecutor(1,1,30,TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(8),task->new Thread(task,"local-category-model"),new ThreadPoolExecutor.AbortPolicy());
    private LocalCategoryClassifier(){}
    public static final class Choice {
        public final long categoryRowId;
        public final int parentSemanticId,childSemanticId;
        public final String label,status;
        public final double score;
        private Choice(long row,int parent,int child,String label,double score,String status){categoryRowId=row;parentSemanticId=parent;childSemanticId=child;this.label=label;this.score=score;this.status=status;}
        public boolean found(){return categoryRowId>0;}
        public void apply(JSONObject value){
            if(!found())return;
            Json.put(value,"category_id",categoryRowId);Json.put(value,"parent_semantic_id",parentSemanticId);Json.put(value,"child_semantic_id",childSemanticId);
            Json.put(value,"classification_source",RecoveredCategoryModel.TOKENIZER);Json.put(value,"classification_label",label);Json.put(value,"classification_score",score);
        }
    }
    private static Choice none(String status){return new Choice(0,0,-1,null,Double.NaN,status);}
    public static void warmUp(Context context){
        Context app=context.getApplicationContext();
        try{WORKER.execute(()->loaded(app));}catch(java.util.concurrent.RejectedExecutionException ignored){}
    }
    public static void suggestAsync(Context context,ReferenceStore store,long book,String kind,String text,long time,Consumer<Choice> callback){
        Context app=context.getApplicationContext();
        try{WORKER.execute(()->{Choice result=suggestOnWorker(app,store,book,kind,text,time);MAIN.post(()->callback.accept(result));});}
        catch(java.util.concurrent.RejectedExecutionException full){MAIN.post(()->callback.accept(none("BUSY")));}
    }
    /** Capture already owns a serial worker. Explicit editor requests use suggestAsync. */
    public static Choice suggestOnWorker(Context context,ReferenceStore store,long book,String kind,String text,long time){
        if(Looper.myLooper()==Looper.getMainLooper())return none("MAIN_THREAD_REJECTED");
        if(!"expense".equals(kind)&&!"income".equals(kind)||text==null||text.isEmpty())return none("NO_TEXT");
        RecoveredCategoryModel current=loaded(context.getApplicationContext());if(current==null)return none("MODEL_UNAVAILABLE");
        try{
            RecoveredCategoryModel.Result prediction=current.predict(text);
            String label=prediction.label;if(label==null||!Double.isFinite(prediction.score))return none("NO_CONFIDENT_MATCH");
            if(label.equals("食品餐饮-其他")||label.equals("食品餐饮-早餐")||label.equals("食品餐饮-午餐")||label.equals("食品餐饮-晚餐")){
                Calendar calendar=Calendar.getInstance();if(time>0)calendar.setTimeInMillis(time);int hour=calendar.get(Calendar.HOUR_OF_DAY);
                label="食品餐饮-"+(hour>=4&&hour<10?"早餐":hour>=10&&hour<16?"午餐":"晚餐");
            }
            int separator=label.indexOf('-');if(separator<=0||separator==label.length()-1)return none("INVALID_LABEL");
            String parentName=label.substring(0,separator),childName=label.substring(separator+1);
            if(kind.equals("income")!=parentName.equals("收入"))return none("DIRECTION_MISMATCH");
            synchronized(store){
                JSONObject ledger=store.book(book);if(ledger==null||ledger.optInt("archived")!=0)return none("BOOK_UNAVAILABLE");
                JSONObject parent=store.one("SELECT * FROM categories WHERE level='parent' AND name=? AND kind=? AND archived=0 ORDER BY id LIMIT 1",parentName,kind);
                if(parent==null||parent.isNull("semantic_id")||store.categoryRepository().isHiddenInBook(parent.optLong("id"),book))return none("PARENT_UNAVAILABLE");
                int parentId=parent.optInt("semantic_id");
                JSONObject child=store.one("SELECT * FROM categories WHERE level='child' AND parent_semantic_id=? AND name=? AND kind=? AND archived=0 AND parent_child_semantic_id=0 ORDER BY id LIMIT 1",""+parentId,childName,kind);
                if(child!=null&&!store.categoryRepository().isHiddenInBook(child.optLong("id"),book))
                    return new Choice(child.optLong("id"),parentId,child.optInt("semantic_id"),label,prediction.score,"SUGGESTED");
                // A removed/renamed child falls back to its recovered parent; income
                // has no selectable parent row, so keep the existing editor default.
                if(kind.equals("expense"))return new Choice(parent.optLong("id"),parentId,-1,label,prediction.score,"PARENT_FALLBACK");
                return none("CHILD_UNAVAILABLE");
            }
        }catch(RuntimeException failure){return none("CLASSIFICATION_FAILED");}
    }
    private static RecoveredCategoryModel loaded(Context context){
        if(Looper.myLooper()==Looper.getMainLooper())return null;
        RecoveredCategoryModel current=model;if(current!=null)return current;
        synchronized(LOAD){
            if(model!=null)return model;if(SystemClock.uptimeMillis()<retryAfter)return null;
            try(java.io.InputStream weights=context.getResources().openRawResource(R.raw.reference_category_model);
                    java.io.InputStream dictionary=context.getResources().openRawResource(R.raw.reference_category_dictionary)){
                // Fixed resource references also keep both inert files in the resource closure.
                model=RecoveredCategoryModel.read(weights,dictionary);
                return model;
            }catch(java.io.IOException|RuntimeException invalid){retryAfter=SystemClock.uptimeMillis()+60000;com.wangc.bill.reference.capture.ReferenceDiagnostics.failure("CATEGORY_MODEL_LOAD_FAILED",invalid);return null;}
        }
    }
}
