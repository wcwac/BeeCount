package com.wangc.bill.reference.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wangc.bill.reference.CategoryRepository;
import com.wangc.bill.reference.Json;
import com.wangc.bill.reference.OriginalCategorySelection;
import com.wangc.bill.reference.ReferenceStore;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/** Recovered 5/6-column category grids with expansion after the selected item's row. */
public final class CategoryPicker {
    private static final int CELL=0,EXPANSION=1,LEGACY_HEADING=2;
    private final Context context;
    private final CategoryRepository repository;
    private final JSONObject data;
    private final State state;
    private final boolean automatic,income;
    private final int columns;
    private final long book;
    private final Runnable changed,manage;
    private final View root;
    private final RecyclerView list;
    private final List<Item> items=new ArrayList<>();

    public static final class State {
        private OriginalCategorySelection selection=OriginalCategorySelection.empty();
        private long legacyExpense,legacyIncome;

        public static State from(Bundle saved,ReferenceStore store,JSONObject data){
            State result=new State();
            int[] ids=saved==null?null:saved.getIntArray("original_category_selection");
            if(ids!=null&&ids.length==4){
                result.selection=result.selection.withExpense(ids[0],ids[1]).withIncome(ids[2],ids[3]);
                result.legacyExpense=saved.getLong("legacy_expense_category",0);result.legacyIncome=saved.getLong("legacy_income_category",0);
                return result;
            }
            JSONObject row=store.categoryRepository().byRowId(data.optLong("category_id"));
            if(row==null)return result;
            if(CategoryRepository.LEGACY.equals(row.optString("level"))){
                if("income".equals(row.optString("kind")))result.legacyIncome=row.optLong("id");else result.legacyExpense=row.optLong("id");
            }else if("income".equals(row.optString("kind"))&&CategoryRepository.CHILD.equals(row.optString("level"))){
                int child=row.optInt("semantic_id"),top=row.optInt("parent_child_semantic_id");
                result.selection=result.selection.withIncome(top>0?top:child,child);
            }else if("income".equals(row.optString("kind"))){
                // AddBill.O0 restores 0 as a real, non--1 selection. Rebinding
                // must not silently turn the automatic-entry 9/0 pair into 9/911.
                int child=data.optInt("child_semantic_id",-1);
                result.selection=result.selection.withIncome(child,child);
            }else if(!"income".equals(row.optString("kind"))){
                int parent=CategoryRepository.PARENT.equals(row.optString("level"))?row.optInt("semantic_id"):row.optInt("parent_semantic_id");
                int child=CategoryRepository.CHILD.equals(row.optString("level"))?row.optInt("semantic_id"):data.optInt("child_semantic_id",-1);
                result.selection=result.selection.withExpense(parent,child);
            }
            return result;
        }

        public void save(Bundle target){
            target.putIntArray("original_category_selection",new int[]{selection.expenseParentCategoryId,selection.expenseChildCategoryId,selection.incomeTopLevelChildCategoryId,selection.incomeChildCategoryId});
            target.putLong("legacy_expense_category",legacyExpense);target.putLong("legacy_income_category",legacyIncome);
        }

        private long legacy(boolean income){return income?legacyIncome:legacyExpense;}
        private void legacy(boolean income,long value){if(income)legacyIncome=value;else legacyExpense=value;}
    }

    private record Item(int type,JSONObject row,List<JSONObject> children,boolean manage){}

    public CategoryPicker(Context context,ReferenceStore store,long book,String kind,JSONObject data,State state,boolean automatic,Runnable changed,Runnable manage){
        this.context=context;repository=store.categoryRepository();this.book=book;this.data=data;this.state=state;this.automatic=automatic;
        this.income="income".equals(kind);columns=automatic?6:5;this.changed=changed;this.manage=manage;
        root=Ui.layout(context,(automatic?"item_auto_":"item_")+(income?"income_category":"pay_category"));
        list=Ui.view(root,income?"bill_income_type_list":"bill_parent_type_list");
        GridLayoutManager layout=new GridLayoutManager(context,columns);
        layout.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){@Override public int getSpanSize(int position){return items.get(position).type==CELL?1:columns;}});
        list.setLayoutManager(layout);list.setItemAnimator(null);list.setAdapter(new Adapter());
        rebuild();
    }

    public View view(){return root;}

    private void rebuild(){
        List<JSONObject> top=income?repository.incomeTopLevel(book):repository.expenseParents(book);
        if(state.legacy(income)==0){
            List<Integer> ids=new ArrayList<>();for(JSONObject row:top)ids.add(row.optInt("semantic_id"));
            state.selection=income?state.selection.bindIncome(ids):state.selection.bindExpense(ids);
        }
        items.clear();int selected=income?state.selection.incomeTopLevelChildCategoryId:state.selection.expenseParentCategoryId,selectedIndex=-1;
        for(JSONObject row:top){if(state.legacy(income)==0&&row.optInt("semantic_id")==selected)selectedIndex=items.size();items.add(new Item(CELL,row,null,false));}
        if(!automatic&&manage!=null)items.add(new Item(CELL,null,null,true));
        if(selectedIndex>=0){
            List<JSONObject> children=income?repository.nestedIncome(selected,book):repository.children(selected,book);
            if(!children.isEmpty())items.add(Math.min(((selectedIndex/columns)+1)*columns,items.size()),new Item(EXPANSION,null,children,false));
        }
        List<JSONObject> legacy=repository.legacy(income?"income":"expense",book);
        if(!legacy.isEmpty()){
            items.add(new Item(LEGACY_HEADING,null,null,false));
            for(JSONObject row:legacy)items.add(new Item(CELL,row,null,false));
        }
        syncData();list.getAdapter().notifyDataSetChanged();
    }

    private void syncData(){
        long legacy=state.legacy(income);
        if(legacy>0){Json.put(data,"category_id",legacy);data.remove("parent_semantic_id");data.remove("child_semantic_id");return;}
        OriginalCategorySelection.CategoryPair pair=income?state.selection.incomePair():state.selection.expensePair();
        JSONObject row=repository.rowForSelection(income?"income":"expense",pair.parentCategoryId,pair.childCategoryId);
        Json.put(data,"parent_semantic_id",pair.parentCategoryId);
        Json.put(data,"child_semantic_id",automatic&&pair.childCategoryId==-1?0:pair.childCategoryId);
        Json.put(data,"category_id",row==null?0:row.optLong("id"));
    }

    private void select(JSONObject row,boolean child){
        if(CategoryRepository.LEGACY.equals(row.optString("level")))state.legacy(income,row.optLong("id"));
        else{
            state.legacy(income,0);int id=row.optInt("semantic_id");
            state.selection=income?(child?state.selection.selectIncomeChild(id):state.selection.selectIncomeTopLevel(id))
                :(child?state.selection.selectExpenseChild(id):state.selection.selectExpenseParent(id));
        }
        rebuild();if(changed!=null)changed.run();
    }

    private boolean selected(JSONObject row,boolean child){
        if(CategoryRepository.LEGACY.equals(row.optString("level")))return state.legacy(income)==row.optLong("id");
        if(state.legacy(income)!=0)return false;
        int id=row.optInt("semantic_id");
        return income?id==(child?state.selection.incomeChildCategoryId:state.selection.incomeTopLevelChildCategoryId)
            :id==(child?state.selection.expenseChildCategoryId:state.selection.expenseParentCategoryId);
    }

    private void bindCell(View view,JSONObject row,boolean child,boolean management){
        Ui.text(view,"name",management?"设置":row.optString("name"));
        Ui.image(view,"icon",management?"ic_menu_add":row.optString("icon","ic_parent_other"));
        ImageView icon=Ui.view(view,"icon");
        icon.setBackgroundResource(!management&&selected(row,child)?Ui.res(context,"drawable","shape_type_select"):child?0:Ui.res(context,"drawable","shape_type_normal"));
        if(Ui.view(view,"icon_more")!=null){
            boolean expand=false;
            if(!management&&!CategoryRepository.LEGACY.equals(row.optString("level")))expand=!(income?repository.nestedIncome(row.optInt("semantic_id"),book):repository.children(row.optInt("semantic_id"),book)).isEmpty();
            Ui.show(view,"icon_more",expand);
        }
        view.setOnClickListener(v->{if(management)manage.run();else select(row,child);});
    }

    private final class Adapter extends RecyclerView.Adapter<RecordAdapter.Holder>{
        @Override public int getItemCount(){return items.size();}
        @Override public int getItemViewType(int position){return items.get(position).type;}
        @Override public RecordAdapter.Holder onCreateViewHolder(ViewGroup parent,int type){
            View view=type==LEGACY_HEADING?Ui.label(context,"旧版分类（保留原账单关联）",11):Ui.layout(context,type==EXPANSION?(automatic?"item_auto_all_child_category":"item_all_child_category"):(automatic?"item_auto_parent_category":"item_parent_category"));
            view.setLayoutParams(new RecyclerView.LayoutParams(-1,-2));return new RecordAdapter.Holder(view);
        }
        @Override public void onBindViewHolder(RecordAdapter.Holder holder,int position){
            Item item=items.get(position);
            if(item.type==CELL)bindCell(holder.itemView,item.row,false,item.manage);
            else if(item.type==EXPANSION){
                RecyclerView nested=Ui.view(holder.itemView,"data_list");nested.setLayoutManager(new GridLayoutManager(context,columns));nested.setNestedScrollingEnabled(false);nested.setItemAnimator(null);
                nested.setAdapter(new RecyclerView.Adapter<RecordAdapter.Holder>(){
                    @Override public int getItemCount(){return item.children.size();}
                    @Override public RecordAdapter.Holder onCreateViewHolder(ViewGroup parent,int type){View view=Ui.layout(context,automatic?"item_auto_bill_child_type":"item_bill_child_type");view.setLayoutParams(new RecyclerView.LayoutParams(-1,-2));return new RecordAdapter.Holder(view);}
                    @Override public void onBindViewHolder(RecordAdapter.Holder child,int index){bindCell(child.itemView,item.children.get(index),true,false);}
                });
            }
        }
    }
}
