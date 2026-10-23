package com.wangc.bill.reference;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.wangc.bill.reference.ui.EditorActivity;
import com.wangc.bill.reference.ui.Ui;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real editor grids and saves, with project-owned local records on the CI emulator. */
@RunWith(AndroidJUnit4.class)
public final class CategoryUiDeviceTest {
    private ReferenceStore store;
    private long book;
    private final List<Long> createdCategories=new ArrayList<>();
    private List<JSONObject> previousMappings;

    @Before public void prepare(){
        assertEquals("true",InstrumentationRegistry.getArguments().getString("reference_cloud_only"));
        assertEquals(36,Build.VERSION.SDK_INT);
        assertTrue(Build.HARDWARE.equals("ranchu")||Build.HARDWARE.equals("goldfish"));
        assertTrue(Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64"));
        store=ReferenceApplication.store();
        ReferenceTestAccounts.installLegacyFixture(store);
        previousMappings=store.query("SELECT * FROM category_accounts");
        book=store.saveBook(0,"分类界面合成测试","CNY",0);
    }

    @After public void cleanup(){
        if(store==null||book==0)return;
        synchronized(store){
            SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();
            try{
                db.delete("transactions","book_id=?",new String[]{""+book});
                db.delete("candidates","book_id=?",new String[]{""+book});
                db.delete("books","id=?",new String[]{""+book});
                db.delete("category_accounts",null,null);
                for(JSONObject row:previousMappings){
                    ContentValues values=new ContentValues();
                    java.util.Iterator<String> columns=row.keys();
                    while(columns.hasNext()){
                        String column=columns.next();Object value=row.opt(column);
                        if(value==null||value==JSONObject.NULL)values.putNull(column);
                        else if(value instanceof Double||value instanceof Float)values.put(column,((Number)value).doubleValue());
                        else if(value instanceof Number)values.put(column,((Number)value).longValue());
                        else if(value instanceof String)values.put(column,(String)value);
                        else throw new AssertionError("Unsupported saved mapping column: "+column);
                    }
                    // Keep PK, owner, raw ID and category-asset metadata as well as the local FK.
                    db.insertOrThrow("category_accounts",null,values);
                }
                for(int i=createdCategories.size()-1;i>=0;i--)db.delete("categories","id=?",new String[]{""+createdCategories.get(i)});
                db.setTransactionSuccessful();
            }finally{db.endTransaction();}
        }
    }

    @Test public void expenseChildSurvivesKindChangesAndActivityRecreation(){
        String id=candidate("expense",99,-1);
        try(ActivityScenario<EditorActivity> screen=editor(id)){
            screen.onActivity(a->{
                RecyclerView list=a.findViewById(com.wangc.bill.R.id.bill_parent_type_list);
                assertEquals(5,((GridLayoutManager)list.getLayoutManager()).getSpanCount());
                clickCategory(a,"购物消费");
            });idle();
            screen.onActivity(a->{
                RecyclerView list=a.findViewById(com.wangc.bill.R.id.bill_parent_type_list);
                // The selected parent is second: the child panel follows the whole five-cell row.
                GridLayoutManager layout=(GridLayoutManager)list.getLayoutManager();
                assertEquals(5,layout.getSpanSizeLookup().getSpanSize(5));
                clickCategory(a,"日常家居");
                clickTab(a,"收入");
            });idle();
            screen.onActivity(a->clickCategory(a,"工资"));idle();
            screen.onActivity(a->clickTab(a,"支出"));idle();
            screen.recreate();idle();
            screen.onActivity(this::save);
        }
        assertSaved(id,"expense",8,811,store.categoryRepository().child(8,811).optLong("id"));
    }

    @Test public void choosingAnExpenseParentClearsItsPreviousChildAndSavesManualMinusOne(){
        String id=candidate("expense",8,811);
        try(ActivityScenario<EditorActivity> screen=editor(id)){
            screen.onActivity(a->clickCategory(a,"食品餐饮"));idle();
            screen.onActivity(this::save);
        }
        assertSaved(id,"expense",7,-1,store.categoryRepository().parent(7).optLong("id"));
    }

    @Test public void nestedIncomeKeepsItsTopLevelAcrossTabsAndRecreation(){
        long top=store.categoryRepository().saveChild(0,"合成收入上级","ic_income_other",9,0);createdCategories.add(top);
        int topId=store.categoryRepository().byRowId(top).optInt("semantic_id");
        long nested=store.categoryRepository().saveChild(0,"合成收入细类","ic_income_other",9,topId);createdCategories.add(nested);
        int nestedId=store.categoryRepository().byRowId(nested).optInt("semantic_id");
        String id=candidate("expense",99,-1);
        try(ActivityScenario<EditorActivity> screen=editor(id)){
            screen.onActivity(a->clickTab(a,"收入"));idle();
            screen.onActivity(a->clickCategory(a,"合成收入上级"));idle();
            screen.onActivity(a->{clickCategory(a,"合成收入细类");clickTab(a,"支出");});idle();
            screen.recreate();idle();
            screen.onActivity(a->clickTab(a,"收入"));idle();
            screen.onActivity(this::save);
        }
        assertSaved(id,"income",9,nestedId,nested);
    }

    @Test public void aKnownIncomeZeroChildIsNotReplacedByFirstVisibleIncome(){
        String id=candidate("income",9,0);
        try(ActivityScenario<EditorActivity> screen=editor(id)){
            screen.recreate();idle();screen.onActivity(this::save);
        }
        assertSaved(id,"income",9,0,store.categoryRepository().parent(9).optLong("id"));
    }

    @Test public void anExistingSelectionHiddenInTheEditorBookRemainsSavable(){
        long parent=store.categoryRepository().parent(8).optLong("id"),child=store.categoryRepository().child(8,811).optLong("id");
        store.categoryRepository().setHiddenInBook(parent,book,true);store.categoryRepository().setHiddenInBook(child,book,true);
        String id=candidate("expense",8,811);
        try(ActivityScenario<EditorActivity> screen=editor(id)){
            screen.onActivity(a->{assertNull(find(Ui.view(a.getWindow().getDecorView(),"category_pager"),"购物消费"));assertNull(find(Ui.view(a.getWindow().getDecorView(),"category_pager"),"日常家居"));});
            screen.recreate();idle();screen.onActivity(this::save);
        }
        assertSaved(id,"expense",8,811,child);
    }

    @Test public void recreationDoesNotReapplyTheInitialFinancialEntryAfterChangingTabs(){
        String id=candidate("expense",99,-1);
        Intent intent=new Intent(ReferenceApplication.app(),EditorActivity.class).putExtra("candidate",id).putExtra("financial","borrow");
        try(ActivityScenario<EditorActivity> screen=ActivityScenario.launch(intent)){
            idle();screen.onActivity(a->clickTab(a,"支出"));idle();
            screen.recreate();idle();screen.onActivity(this::save);
        }
        assertSaved(id,"expense",99,-1,store.categoryRepository().parent(99).optLong("id"));
        assertEquals("",store.transaction(store.candidate(id).optLong("transaction_id")).optString("financial_type"));
    }

    @Test public void bothAutomaticCategoryListsHonorTheOriginal300dpMeasurementCap(){
        String id=candidate("expense",99,-1);
        try(ActivityScenario<EditorActivity> screen=editor(id)){
            screen.onActivity(a->{
                for(String kind:new String[]{"pay","income"}){
                    View root=Ui.layout(a,"item_auto_"+kind+"_category");
                    RecyclerView list=Ui.view(root,kind.equals("income")?"bill_income_type_list":"bill_parent_type_list");
                    list.setLayoutManager(new GridLayoutManager(a,6));
                    list.setAdapter(new RecyclerView.Adapter<HeightHolder>(){
                        @Override public int getItemCount(){return 180;}
                        @Override public HeightHolder onCreateViewHolder(ViewGroup parent,int type){
                            View view=new View(a);view.setLayoutParams(new RecyclerView.LayoutParams(-1,Ui.dp(a,30)));return new HeightHolder(view);
                        }
                        @Override public void onBindViewHolder(HeightHolder holder,int position){}
                    });
                    // Original onMeasure replaces even an EXACTLY parent spec with AT_MOST.
                    list.measure(View.MeasureSpec.makeMeasureSpec(Ui.dp(a,360),View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(Ui.dp(a,1000),View.MeasureSpec.EXACTLY));
                    assertEquals(kind+" no longer consumes app:maxHeight from the recovered layout",Ui.dp(a,300),list.getMeasuredHeight());
                }
            });
        }
    }

    private static final class HeightHolder extends RecyclerView.ViewHolder{HeightHolder(View view){super(view);}}

    private String candidate(String kind,int parent,int child){
        JSONObject row=store.categoryRepository().rowForSelection(kind,parent,child);assertNotNull(row);
        JSONObject data=Json.object("book_id",book,"kind",kind,"category_id",row.optLong("id"),"parent_semantic_id",parent,"child_semantic_id",child,
            "amount",1234,"time",1735689600000L,"account_id",0,"fee",0,"discount",0,"remark","分类界面合成数据");
        return store.addCandidate(data,"io.github.wcwac.categoryfixture",book);
    }
    private ActivityScenario<EditorActivity> editor(String candidate){
        Intent intent=new Intent(ReferenceApplication.app(),EditorActivity.class).putExtra("candidate",candidate);
        ActivityScenario<EditorActivity> screen=ActivityScenario.launch(intent);idle();
        screen.onActivity(a->assertFalse("Editor fell back to finish",a.isFinishing()));return screen;
    }
    private void save(EditorActivity activity){
        View keyboard=activity.findViewById(com.wangc.bill.R.id.number_key_board);
        assertEquals("完成",((TextView)Ui.view(keyboard,"cancel")).getText().toString());Ui.view(keyboard,"cancel").performClick();
    }
    private void assertSaved(String id,String kind,int parent,int child,long category){
        idle();JSONObject candidate=store.candidate(id);assertEquals(candidate.toString(),"SAVED",candidate.optString("state"));
        JSONObject transaction=store.transaction(candidate.optLong("transaction_id"));assertNotNull(transaction);
        assertEquals(kind,transaction.optString("kind"));assertEquals(1234,transaction.optLong("amount"));
        assertEquals(parent,transaction.optInt("parent_semantic_id"));assertEquals(child,transaction.optInt("child_semantic_id"));assertEquals(category,transaction.optLong("category_id"));
    }
    private static void clickCategory(EditorActivity activity,String text){
        View label=find(Ui.view(activity.getWindow().getDecorView(),"category_pager"),text);assertNotNull("Missing attached category: "+text,label);
        View cell=(View)label.getParent();assertTrue("Category is not visible: "+text,cell.isShown());assertTrue(cell.performClick());
    }
    private static void clickTab(EditorActivity activity,String text){
        View tab=find(activity.findViewById(com.wangc.bill.R.id.tab_layout),text);assertNotNull(text,tab);assertTrue(tab.performClick());
    }
    private static View find(View root,String text){
        if(root instanceof TextView label&&text.contentEquals(label.getText()))return label;
        if(root instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){View found=find(group.getChildAt(i),text);if(found!=null)return found;}
        return null;
    }
    private static void idle(){InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
}
