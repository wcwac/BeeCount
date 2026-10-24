package com.wangc.bill.reference.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wangc.bill.reference.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** One bounded history page. A short list keeps its original adapter and header. */
public final class HistoryRecordList {
    private HistoryRecordList() {}
    public static Controller bind(Activity activity,RecyclerView list,View header,AccountingHistory.Filter filter,RecordAdapter.Click click,boolean allowTrash) {
        return new Controller(activity,list,header,filter,click,allowTrash,false);
    }
    public static Controller attachments(Activity activity,RecyclerView list,AccountingHistory.Filter filter) {
        AccountingHistory.Filter files=filter.copy();files.attachments=true;
        return new Controller(activity,list,null,files,null,false,true);
    }
    public static final class Controller {
        private final Activity activity;
        private final RecyclerView list;
        private final View header;
        private final AccountingHistory.Filter filter;
        private final RecordAdapter.Click click;
        private final boolean allowTrash,files;
        private final List<AccountingHistory.Cursor> cursors=new ArrayList<>();
        private AccountingHistory.Page current;
        private int index;
        private boolean rangeEdited;
        private Controller(Activity activity,RecyclerView list,View header,AccountingHistory.Filter filter,RecordAdapter.Click click,boolean allowTrash,boolean files) {
            this.activity=activity;this.list=list;this.header=header;this.filter=filter.copy();this.click=click;this.allowTrash=allowTrash;this.files=files;
            cursors.add(null);list.setLayoutManager(new LinearLayoutManager(activity));load();
        }
        public boolean empty(){return current!=null&&current.rows.isEmpty()&&!current.more&&index==0;}
        private void load(){
            try {
                AccountingHistory.Page loaded=AccountingHistory.page(ReferenceApplication.store(),filter,cursors.get(index));
                current=loaded;
                RecyclerView.Adapter<?> records;
                if(files)records=new AttachmentsAdapter(activity,current.rows);
                else {RecordAdapter adapter=new RecordAdapter(current.rows,header,click);adapter.allowTrash=allowTrash;adapter.historyReload=this::load;records=adapter;}
                // Preserve the exact previous row/header structure for short fixtures/lists.
                if(current.more||index>0||rangeEdited)list.setAdapter(new ConcatAdapter(records,new SingleViewAdapter(navigation())));
                else if(files&&current.rows.isEmpty())list.setAdapter(new SingleViewAdapter(Ui.row(activity,"暂无附件","在记账页面点击附件按钮添加图片、PDF或文本。",null)));
                else list.setAdapter(records);
                list.scrollToPosition(0);
            }catch(Exception failure){Ui.error(activity,failure);}
        }
        private View navigation(){
            LinearLayout panel=Ui.column(activity);int pad=Ui.dp(activity,14);panel.setPadding(pad,pad,pad,pad);
            String detail=current.rows.isEmpty()?(current.more?"本页未找到匹配项，可继续查看较早记录。":"所选范围暂无记录。")
                    :"第 "+(index+1)+" 页 · "+current.rows.size()+" 条记录"+(files?"的附件":"");
            TextView text=Ui.label(activity,detail,13);panel.addView(text);
            if(filter.start!=0||filter.end!=Long.MAX_VALUE)panel.addView(Ui.label(activity,"日期："+Ui.date(filter.start,"yyyy-MM-dd")+" — "+Ui.date(filter.end-1,"yyyy-MM-dd"),12));
            LinearLayout controls=new LinearLayout(activity);panel.addView(controls);
            Button previous=new Button(activity);previous.setText("较新一页");previous.setEnabled(index>0);controls.addView(previous,new LinearLayout.LayoutParams(0,-2,1));
            previous.setOnClickListener(view->{index--;load();});
            Button next=new Button(activity);next.setText("较早一页");next.setEnabled(current.more);controls.addView(next,new LinearLayout.LayoutParams(0,-2,1));
            next.setOnClickListener(view->{if(!current.more||current.next==null)return;if(cursors.size()>index+1)cursors.set(index+1,current.next);else cursors.add(current.next);index++;load();});
            LinearLayout options=new LinearLayout(activity);panel.addView(options);
            Button date=new Button(activity);date.setText("筛选日期");options.addView(date,new LinearLayout.LayoutParams(0,-2,1));date.setOnClickListener(view->chooseRange());
            Button refresh=new Button(activity);refresh.setText("刷新最新");options.addView(refresh,new LinearLayout.LayoutParams(0,-2,1));refresh.setOnClickListener(view->reset());
            return panel;
        }
        private void reset(){index=0;cursors.clear();cursors.add(null);load();}
        private void chooseRange(){
            new AlertDialog.Builder(activity).setTitle("历史记录日期").setItems(new String[]{"选择起止日期","全部日期"},(dialog,which)->{
                if(which==1){filter.start=0;filter.end=Long.MAX_VALUE;rangeEdited=true;reset();return;}
                pick(filter.start==0?System.currentTimeMillis():filter.start,start->pick(filter.end==Long.MAX_VALUE?System.currentTimeMillis():filter.end-1,end->{
                    Calendar following=Calendar.getInstance();following.setTimeInMillis(end);following.add(Calendar.DATE,1);
                    if(following.getTimeInMillis()<=start){Ui.toast(activity,"结束日期不能早于开始日期");return;}
                    filter.start=start;filter.end=following.getTimeInMillis();rangeEdited=true;reset();
                }));
            }).show();
        }
        private void pick(long initial,Day result){Calendar calendar=Calendar.getInstance();calendar.setTimeInMillis(initial);
            new DatePickerDialog(activity,(picker,y,m,d)->{Calendar chosen=Calendar.getInstance();chosen.clear();chosen.set(y,m,d);result.accept(chosen.getTimeInMillis());},calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();}
    }
    private interface Day{void accept(long time);}
    private static final class SingleViewAdapter extends RecyclerView.Adapter<RecordAdapter.Holder> {
        private final View view;SingleViewAdapter(View view){this.view=view;}
        public int getItemCount(){return 1;}
        public RecordAdapter.Holder onCreateViewHolder(ViewGroup parent,int type){return new RecordAdapter.Holder(view);}
        public void onBindViewHolder(RecordAdapter.Holder holder,int position){}
    }
    private static final class AttachmentsAdapter extends RecyclerView.Adapter<RecordAdapter.Holder> {
        private final Activity activity;private final List<JSONObject> rows=new ArrayList<>(),owners=new ArrayList<>();
        AttachmentsAdapter(Activity activity,List<JSONObject> entries){this.activity=activity;for(JSONObject entry:entries){JSONArray files=Json.array(entry.optString("attachments","[]"));for(int i=0;i<files.length();i++){JSONObject file=files.optJSONObject(i);if(file==null)continue;rows.add(file);owners.add(entry);}}}
        public int getItemCount(){return rows.size();}
        public RecordAdapter.Holder onCreateViewHolder(ViewGroup parent,int type){return new RecordAdapter.Holder(Ui.row(activity,"","",null));}
        public void onBindViewHolder(RecordAdapter.Holder holder,int position){JSONObject file=rows.get(position),owner=owners.get(position);LinearLayout row=(LinearLayout)holder.itemView;
            ((TextView)row.getChildAt(0)).setText(file.optString("name","附件"));
            ((TextView)row.getChildAt(1)).setText(Ui.date(owner.optLong("time"),"yyyy-MM-dd")+" · "+owner.optString("remark"));
            row.setOnClickListener(view->LocalFiles.openAttachment(activity,file));row.setOnLongClickListener(view->{Ui.record(activity,owner);return true;});
        }
    }
}
