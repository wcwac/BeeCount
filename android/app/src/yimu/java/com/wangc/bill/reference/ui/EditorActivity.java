package com.wangc.bill.reference.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.wangc.bill.reference.*;
import com.wangc.bill.reference.capture.CaptureNotifications;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public final class EditorActivity extends AppCompatActivity {
    private View root;private JSONObject data;private String candidateId,amount="0",operationId;private long editingId,typedId;private String typedKind="";private boolean saving,committed;private int selectedTab;
    private CategoryPicker.State categoryState;private EditText settlementInterest;
    private int categorySuggestionSerial;
    private final OriginalFinancialEffects.Listener financialListener=event->{
        if(root!=null&&data!=null&&!isFinishing()){
            // Consume the current account snapshot without rebuilding the keyboard,
            // amount, remark, or category selection while the user is editing.
            Ui.text(root,"asset_name",accountName(data.optLong("account_id")));
        }
    };
    private static final String[] NAMES={"支出","收入","转账","借出","借入","还款","报销"};
    private static final String[] KINDS={"expense","income","transfer","expense","income","expense","expense"},FINANCE={"","","","lend","borrow","repayment","reimbursement"};
    private long defaultAccount(long book){long id=ReferenceApplication.prefs().getLong("default_account",0);JSONObject b=store().book(book);return b!=null&&store().accountInDisplayCurrency(id,b.optString("currency"))!=null?id:0;}
    private ReferenceStore store(){return ReferenceApplication.store();}
    private String currency(){JSONObject book=store().book(data.optLong("book_id"));return book==null?"CNY":book.optString("currency");}
    @Override protected void onCreate(Bundle state){super.onCreate(state);
        if(state!=null&&state.getBoolean("committed")){finish();return;}
        try{
            candidateId=getIntent().getStringExtra("candidate");editingId=getIntent().getLongExtra("transaction",0);typedKind=getIntent().getStringExtra("entry_kind");if(typedKind==null)typedKind="";typedId=getIntent().getLongExtra("entry_id",0);
            operationId=state==null?UUID.randomUUID().toString():state.getString("operation_id",UUID.randomUUID().toString());
            if(state!=null&&state.containsKey("data")){data=Json.parse(state.getString("data"));amount=state.getString("amount","0");}
            else if(candidateId!=null){JSONObject candidate=store().candidate(candidateId);if(candidate==null||!"PENDING".equals(candidate.optString("state")))throw new IllegalArgumentException("采集记录已处理或不存在");data=Json.parse(candidate.optString("payload"));amount=Money.text(data.optLong("amount"),currency());}
            else if(typedId>0){data=store().accountingEntry(typedKind,typedId);if(data==null||data.optInt("deleted")!=0)throw new IllegalArgumentException("记录已删除或不存在");
                data=Json.copy(data);if(typedKind.equals("transfer")){Json.put(data,"kind","transfer");Json.put(data,"financial_type","");}
                else if(data.optInt("lend_type")==1){Json.put(data,"kind","expense");Json.put(data,"financial_type","lend");}
                else if(data.optInt("lend_type")==3){Json.put(data,"kind","income");Json.put(data,"financial_type","borrow");}
                else if(data.optLong("related_id")>0){Json.put(data,"kind",data.optInt("lend_type")==2?"income":"expense");Json.put(data,"financial_type","loan_settlement");}
                else{Json.put(data,"kind","expense");Json.put(data,"financial_type","repayment");}
                amount=Money.text(data.optLong("amount"),currency());}
            else if(editingId>0){data=store().transaction(editingId);if(data==null||data.optInt("deleted")!=0)throw new IllegalArgumentException("账单已不存在");amount=Money.text(data.optLong("amount"),currency());JSONArray tags=new JSONArray();for(JSONObject tag:store().query("SELECT tag_id FROM transaction_tags WHERE transaction_id=?",""+editingId))tags.put(tag.optLong("tag_id"));Json.put(data,"tag_ids",tags);for(String key:new String[]{"not_total","not_budget"})Json.put(data,key,data.optInt(key)!=0);}
            else {long book=ReferenceApplication.prefs().getLong("current_book",1);if(store().book(book)==null)book=store().books().get(0).optLong("id");data=Json.object("book_id",book,"kind","expense","category_id",store().defaultCategory("expense",book),"account_id",defaultAccount(book),"amount",0,"time",getIntent().getLongExtra("time",System.currentTimeMillis()),"tag_ids",new JSONArray(),"attachments","[]");}
            if(state==null&&getIntent().hasExtra("financial")){String finance=getIntent().getStringExtra("financial");Json.put(data,"financial_type",finance);Json.put(data,"kind",finance.equals("borrow")?"income":"expense");Json.put(data,"category_id",store().defaultCategory(data.optString("kind"),data.optLong("book_id")));data.remove("parent_semantic_id");data.remove("child_semantic_id");}
            if(data.optBoolean("transfer_repayment_route")&&data.optString("kind").equals("transfer")){Json.put(data,"financial_type","repayment");Json.put(data,"kind","expense");}
            categoryState=CategoryPicker.State.from(state,store(),data);
            for(int i=0;i<NAMES.length;i++)if(KINDS[i].equals(data.optString("kind"))&&FINANCE[i].equals(data.optString("financial_type")))selectedTab=i;
            if(data.optString("financial_type").equals("repayment"))selectedTab=5;
            if(data.optString("financial_type").equals("loan_settlement"))selectedTab=data.optInt("lend_type")==2?1:5;
            root=Ui.layout(this,"activity_add_bill");Ui.content(this,root);Ui.view(root,"bottom_Layout").getLayoutParams().width=ViewGroup.LayoutParams.MATCH_PARENT;
            ReferenceTabs tabs=Ui.view(root,"tab_layout");tabs.configure(NAMES,selectedTab,index->{if(typedId>0&&index!=selectedTab||editingId>0&&index!=selectedTab&&(index==2||index==3||index==4||index==5)){Ui.toast(this,"请在原类型中编辑；其他类型请另建记录");tabs.select(selectedTab);return;}syncNote();selectedTab=index;Json.put(data,"kind",KINDS[index]);Json.put(data,"financial_type",FINANCE[index]);Json.put(data,"transfer_repayment_route",index==5);render();});
            EditText note=Ui.view(root,"remark");note.setText(data.optString("remark"));note.setImeOptions(note.getImeOptions()|EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);
            Ui.click(root,"btn_back",this::finish);Ui.click(root,"account_book_name",this::chooseBook);Ui.click(root,"cost",()->amountDialog("金额",amount,value->{amount=value;renderAmount();}));
            Ui.click(root,"date_layout",this::chooseDate);Ui.click(root,"asset_layout",()->chooseAccount(false));Ui.click(root,"discount_layout",()->amountDialog("优惠（仅作记录，不再次扣减金额）",Money.text(data.optLong("discount"),currency()),value->{Json.put(data,"discount",Math.max(0,Money.signed(value,currency())));render();}));
            Ui.click(root,"service_charge_layout",()->amountDialog("手续费",Money.text(data.optLong("fee"),currency()),value->{Json.put(data,"fee",Math.max(0,Money.signed(value,currency())));render();}));
            Ui.click(root,"config_layout",this::options);Ui.click(root,"tag_btn",this::chooseTags);Ui.click(root,"reimbursement_layout",()->{if(data.optString("kind").equals("transfer")||data.optString("financial_type").equals("repayment"))return;
                JSONObject current=editingId>0?store().transaction(editingId):null;
                if(current!=null&&(current.optString("financial_type").equals("reimbursement")||current.optInt("bill_reimbursement")!=0)){Ui.toast(this,"请先在账单详情的更多操作中取消待报销");return;}Json.put(data,"financial_type",data.optString("financial_type").equals("reimbursement")?"":"reimbursement");Json.put(data,"transfer_repayment_route",false);render();});Ui.click(root,"add_file_layout",()->{Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(pick,401);});
            NumberKeyboard keyboard=Ui.view(root,"number_key_board");keyboard.setListener(this::key);render();
        }catch(Exception error){Ui.error(this,error);finish();}
    }
    @Override protected void onSaveInstanceState(Bundle out){syncNote();super.onSaveInstanceState(out);if(data!=null){out.putString("data",data.toString());out.putString("amount",amount);out.putString("operation_id",operationId);out.putBoolean("committed",committed);if(categoryState!=null)categoryState.save(out);}}
    private void syncNote(){if(root!=null&&data!=null){EditText note=Ui.view(root,"remark");if(note!=null)Json.put(data,"remark",note.getText().toString());}}
    private void render(){categorySuggestionSerial++;JSONObject book=store().book(data.optLong("book_id"));Ui.text(root,"account_book_name",book==null?"账本不存在":book.optString("name"));Ui.text(root,"date",Ui.date(data.optLong("time"),"MM月dd日"));JSONObject account=store().one("SELECT name FROM accounts WHERE id=?",""+data.optLong("account_id"));Ui.text(root,"asset_name",account==null?"无账户":account.optString("name"));Ui.image(root,"asset_icon","ic_asset_cash");Ui.show(root,"reimbursement_layout",!data.optString("kind").equals("transfer")&&!data.optString("financial_type").equals("repayment"));Ui.text(root,"reimbursement_name",data.optString("financial_type").equals("reimbursement")?"待报销":"不报销");Ui.text(root,"discount",data.optLong("discount")==0?"优惠":"优惠 "+Money.text(data.optLong("discount"),currency()));Ui.text(root,"service_charge",data.optLong("fee")==0?"手续费":"手续费 "+Money.text(data.optLong("fee"),currency()));Ui.hide(root,"currency_asset_info","currency_info","discount_currency","service_charge_currency","group_asset_list","asset_num_tip","lend_date_layout");Ui.show(root,"service_charge_layout",data.optLong("fee")>0||data.optString("kind").equals("transfer")||data.optString("financial_type").equals("repayment"));renderAmount();renderCategories();renderChips();}
    private void renderAmount(){Ui.text(root,"cost",amount.isEmpty()?"0.00":amount);Ui.show(root,"symbol",true);Ui.text(root,"symbol",currency().equals("CNY")?"¥":currency());TextView cost=Ui.view(root,"cost");cost.setTextColor(data.optString("kind").equals("income")?Ui.GREEN:Ui.RED);NumberKeyboard keyboard=Ui.view(root,"number_key_board");if(keyboard!=null)keyboard.expression(amount);}
    private void renderCategories(){FrameLayout host=Ui.view(root,"category_pager");host.removeAllViews();settlementInterest=null;
        if(typedId>0&&typedKind.equals("lend")&&data.optLong("related_id")>0){
            LinearLayout box=Ui.column(this);box.addView(Ui.row(this,data.optInt("lend_type")==2?"回款到账账户":"还款资金账户",accountName(data.optLong("account_id")),()->chooseAccount(false)));
            settlementInterest=new EditText(this);settlementInterest.setHint("利息（不计入本金）");settlementInterest.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);settlementInterest.setText(Money.text(data.optLong("interest"),currency()));
            settlementInterest.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence value,int start,int count,int after){}public void onTextChanged(CharSequence value,int start,int before,int count){try{Json.put(data,"interest",Money.signed(value.toString(),currency()));}catch(RuntimeException ignored){}}public void afterTextChanged(android.text.Editable value){}});box.addView(settlementInterest);host.addView(box);return;
        }
        if(data.optString("kind").equals("transfer")||data.optString("financial_type").equals("repayment")){boolean repayment=data.optString("financial_type").equals("repayment")||data.optBoolean("transfer_repayment_route");LinearLayout box=Ui.column(this);box.addView(Ui.row(this,repayment?"还款账户":"转出账户",accountName(data.optLong("account_id")),()->chooseAccount(false)));box.addView(Ui.row(this,repayment?"信用账户":"转入账户",accountName(data.optLong("to_account_id")),()->chooseAccount(true)));host.addView(box);return;}
        if(data.optString("financial_type").equals("lend")||data.optString("financial_type").equals("borrow")){
            LinearLayout box=Ui.column(this);boolean borrow=data.optString("financial_type").equals("borrow");
            box.addView(Ui.row(this,borrow?"借款到账账户":"借出资金账户",accountName(data.optLong("account_id")),()->chooseAccount(false)));
            EditText person=new EditText(this);person.setHint(borrow?"出借人或借款说明":"借款人或借出说明");person.setSingleLine(true);person.setText(data.optString("counterparty"));
            person.setImeOptions(EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);person.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence value,int start,int count,int after){}public void onTextChanged(CharSequence value,int start,int before,int count){Json.put(data,"counterparty",value.toString());}public void afterTextChanged(android.text.Editable value){}});box.addView(person);
            box.addView(Ui.row(this,borrow?"应付账户":"应收账户",data.optLong("to_account_id")>0?accountName(data.optLong("to_account_id")):"保存时按往来人建立专用账户",this::chooseDebtAccount));host.addView(box);return;
        }
        host.addView(new CategoryPicker(this,store(),data.optLong("book_id"),data.optString("kind"),data,categoryState,false,()->categorySuggestionSerial++,()->Ui.page(this,"categories")).view(),new FrameLayout.LayoutParams(-1,-1));}
    private String accountName(long id){JSONObject a=store().one("SELECT name FROM accounts WHERE id=?",""+id);return a==null?"请选择账户":a.optString("name");}
    private void renderChips(){JSONArray tags=data.optJSONArray("tag_ids");StringBuilder labels=new StringBuilder();if(tags!=null)for(int i=0;i<tags.length();i++){JSONObject t=store().one("SELECT name FROM tags WHERE id=?",""+tags.optLong(i));if(t!=null){if(labels.length()>0)labels.append(" · ");labels.append(t.optString("name"));}}Ui.text(root,"tag_btn_name",labels.length()==0?"标签":labels.toString());Ui.hide(root,"tag_list");androidx.recyclerview.widget.RecyclerView list=Ui.view(root,"file_list");JSONArray files=Json.array(data.optString("attachments","[]"));if(list!=null){list.setVisibility(files.length()==0?View.GONE:View.VISIBLE);list.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this,androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,false));list.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter<MainActivity.SimpleHolder>(){public int getItemCount(){return files.length();}public MainActivity.SimpleHolder onCreateViewHolder(ViewGroup p,int type){TextView label=Ui.label(EditorActivity.this,"",12);label.setPadding(12,8,12,8);return new MainActivity.SimpleHolder(label);}public void onBindViewHolder(MainActivity.SimpleHolder h,int position){JSONObject file=files.optJSONObject(position);((TextView)h.itemView).setText(file.optString("name","附件"));h.itemView.setOnClickListener(v->new AlertDialog.Builder(EditorActivity.this).setItems(new String[]{"打开附件","从账单移除"},(d,w)->{if(w==0)LocalFiles.openAttachment(EditorActivity.this,file);else{JSONArray remaining=new JSONArray();for(int i=0;i<files.length();i++)if(i!=position)remaining.put(files.opt(i));Json.put(data,"attachments",remaining.toString());renderChips();}}).show());}});}}
    private void chooseBook(){List<JSONObject> books=store().books();String[] labels=new String[books.size()];for(int i=0;i<labels.length;i++)labels[i]=books.get(i).optString("name")+" · "+books.get(i).optString("currency");new AlertDialog.Builder(this).setTitle("选择账本").setItems(labels,(d,w)->{String old=currency();Json.put(data,"book_id",books.get(w).optLong("id"));if(!old.equals(currency())){amount="0";store().selectAccountForInput(data,"account_id",0,false);store().selectAccountForInput(data,"to_account_id",0,false);Json.put(data,"fee",0);Json.put(data,"discount",0);Ui.toast(this,"币种已切换，请重新输入金额");}render();}).show();}
    private void chooseAccount(boolean destination){List<JSONObject> all=store().selectableAccounts(data.optLong("book_id")),accounts=new ArrayList<>();for(JSONObject a:all)if(currency().equals(a.optString("currency"))&&(!destination||!data.optString("financial_type").equals("repayment")||a.optInt("asset_type")==2))accounts.add(a);String[] labels=new String[accounts.size()+1];labels[0]=destination?"取消选择":"无账户";for(int i=0;i<accounts.size();i++)labels[i+1]=accounts.get(i).optString("name")+" · "+Money.text(accounts.get(i).optLong("balance"),currency());new AlertDialog.Builder(this).setTitle(destination?"转入账户":"记账账户").setItems(labels,(d,w)->{store().selectAccountForInput(data,destination?"to_account_id":"account_id",w==0?0:accounts.get(w-1).optLong("id"),false);render();}).setNeutralButton("管理账户",(d,w)->Ui.page(this,"accounts")).show();}
    private void chooseDebtAccount(){
        boolean borrow=data.optString("financial_type").equals("borrow");List<JSONObject> choices=new ArrayList<>();
        for(JSONObject account:store().accounts())if(account.optInt("archived")==0&&(account.optInt("asset_type")== (borrow?7:6)||account.optString("type").equals(borrow?"debt":"receivable")))choices.add(account);
        String[] names=new String[choices.size()+1];names[0]="按往来人新建专用账户";for(int i=0;i<choices.size();i++)names[i+1]=choices.get(i).optString("name");
        new AlertDialog.Builder(this).setTitle(borrow?"应付账户":"应收账户").setItems(names,(dialog,index)->{Json.put(data,"to_account_id",index==0?0:choices.get(index-1).optLong("id"));renderCategories();}).show();
    }
    private void chooseDate(){Calendar c=Calendar.getInstance();c.setTimeInMillis(data.optLong("time"));new DatePickerDialog(this,(view,y,m,d)->{Calendar v=Calendar.getInstance();v.setTimeInMillis(data.optLong("time"));v.set(y,m,d);Json.put(data,"time",v.getTimeInMillis());render();},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
    private interface AmountResult{void value(String raw);}
    private void amountDialog(String title,String initial,AmountResult result){EditText edit=new EditText(this);edit.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);edit.setText(initial);edit.selectAll();AlertDialog dialog=new AlertDialog.Builder(this).setTitle(title).setView(edit).setNegativeButton("取消",null).setPositiveButton("确定",null).create();dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{Money.signed(edit.getText().toString(),currency());result.value(edit.getText().toString());dialog.dismiss();}catch(Exception error){Ui.error(this,error);}}));dialog.show();}
    private void chooseTags(){List<JSONObject> tags=store().query("SELECT * FROM tags ORDER BY id");String[] names=new String[tags.size()];boolean[] checked=new boolean[tags.size()];JSONArray previous=data.optJSONArray("tag_ids");for(int i=0;i<names.length;i++){names[i]=tags.get(i).optString("name");if(previous!=null)for(int j=0;j<previous.length();j++)if(previous.optLong(j)==tags.get(i).optLong("id"))checked[i]=true;}new AlertDialog.Builder(this).setTitle("账单标签").setMultiChoiceItems(names,checked,(d,w,v)->checked[w]=v).setPositiveButton("确定",(d,w)->{JSONArray selected=new JSONArray();for(int i=0;i<checked.length;i++)if(checked[i])selected.put(tags.get(i).optLong("id"));Json.put(data,"tag_ids",selected);renderChips();}).setNeutralButton("管理标签",(d,w)->Ui.page(this,"tags")).setNegativeButton("取消",null).show();}
    private void options(){String[] labels={"不计入收支合计","不计入预算","记住此商户的分类和账户","显示手续费"};boolean[] values={data.optBoolean("not_total"),data.optBoolean("not_budget"),data.optBoolean("remember_rule"),true};new AlertDialog.Builder(this).setTitle("记账配置").setMultiChoiceItems(labels,values,(d,w,v)->values[w]=v).setPositiveButton("确定",(d,w)->{Json.put(data,"not_total",values[0]);Json.put(data,"not_budget",values[1]);Json.put(data,"remember_rule",values[2]);Ui.show(root,"service_charge_layout",values[3]);}).setNeutralButton("建议分类",(dialog,which)->suggestCategory()).setNegativeButton("取消",null).show();}
    private void suggestCategory(){
        syncNote();String kind=data.optString("kind"),financial=data.optString("financial_type");
        if(typedId>0||!(kind.equals("expense")||kind.equals("income"))||!financial.isEmpty()&&!financial.equals("reimbursement")){Ui.toast(this,"请在普通收入或支出中建议分类");return;}
        String text=data.optString("remark");if(text.isEmpty())text=data.optString("merchant");
        if(text.isEmpty()){Ui.toast(this,"请先填写备注或商户");return;}
        final String requestedText=text,requestedRemark=data.optString("remark"),requestedMerchant=data.optString("merchant"),draft=operationId;
        final long book=data.optLong("book_id"),category=data.optLong("category_id");final int request=++categorySuggestionSerial;
        Ui.toast(this,"正在本地分析分类");
        LocalCategoryClassifier.suggestAsync(getApplicationContext(),store(),book,kind,requestedText,data.optLong("time"),choice->{
            if(isFinishing()||isDestroyed()||saving||committed||request!=categorySuggestionSerial||!draft.equals(operationId))return;
            syncNote();
            if(book!=data.optLong("book_id")||!kind.equals(data.optString("kind"))||!financial.equals(data.optString("financial_type"))
                    ||category!=data.optLong("category_id")||!requestedRemark.equals(data.optString("remark"))||!requestedMerchant.equals(data.optString("merchant")))return;
            if(!choice.found()){Ui.toast(this,"没有合适建议，已保留当前分类");return;}
            choice.apply(data);categoryState=CategoryPicker.State.from(null,store(),data);renderCategories();Ui.toast(this,"建议分类："+choice.label);
        });
    }
    private void key(String key){if(saving)return;try{if(key.equals("=")){amount=Calculator.evaluate(amount,Money.scale(currency()));renderAmount();return;}if(key.equals("完成")||key.equals("再记")){if(amount.matches(".*[+×÷].*")||amount.indexOf('-',1)>=0)amount=Calculator.evaluate(amount,Money.scale(currency()));save(key.equals("再记"));return;}if(key.equals("取消")){finish();return;}if(key.equals("清空")){amount="0";}else if(key.equals("⌫")){amount=amount.length()>1?amount.substring(0,amount.length()-1):"0";}else if("+-×÷".contains(key)){if(amount.length()>0){if("+-×÷".indexOf(amount.charAt(amount.length()-1))>=0)amount=amount.substring(0,amount.length()-1);amount+=key;}}else if(key.equals(".")){String[] parts=amount.split("[+×÷-]",-1);if(!parts[parts.length-1].contains("."))amount+=parts[parts.length-1].isEmpty()?"0.":".";}else if(amount.length()<60){amount=amount.equals("0")?key:amount+key;}renderAmount();}catch(Exception e){Ui.error(this,e);}}
    private void save(boolean another){if(saving||committed)return;saving=true;try{syncNote();AccountingTargets.requireResolved(store(),data,candidateId);if(settlementInterest!=null)Json.put(data,"interest",Money.signed(settlementInterest.getText().toString(),currency()));Json.put(data,"amount",Money.positive(amount,currency()));boolean transfer=data.optString("kind").equals("transfer"),repayment=data.optString("financial_type").equals("repayment")||data.optBoolean("transfer_repayment_route");
            if(typedId>0){store().editAccountingEntry(typedKind,typedId,data,operationId);}
            else if(editingId>0&&(repayment||transfer&&!data.optString("bill_origin").equals("legacy")))throw new IllegalArgumentException("请在对应的转账或还款记录中处理此操作");
            else if(candidateId!=null&&(transfer||repayment)){
                JSONObject typed=Json.copy(data);Json.put(typed,"kind","transfer");
                Json.put(typed,"transfer_amount_text",Money.text(data.optLong("amount"),currency()));
                Json.put(typed,"transfer_fee_text",Money.text(data.optLong("fee"),currency()));
                Json.put(typed,"transfer_repayment_route",repayment);
                if(repayment)store().saveCandidateCreditRepayment(typed,candidateId,()->{throw new IllegalArgumentException("到账金额必须大于零");});
                else store().saveCandidateTransfer(typed,candidateId,()->{throw new IllegalArgumentException("到账金额必须大于零");});
            }else if(data.optString("financial_type").equals("lend")||data.optString("financial_type").equals("borrow")){
                if(editingId>0)throw new IllegalArgumentException("已有收支账单不能转换为借贷，请另建记录");
                if(candidateId!=null)store().saveCandidateFinancial(data,candidateId,operationId,true);else new AccountingFinancialFlows(store()).saveLoan(data,operationId);
            }else if(data.optString("financial_type").equals("reimbursement")&&editingId==0){
                if(candidateId!=null)store().saveCandidateFinancial(data,candidateId,operationId,false);else new AccountingFinancialFlows(store()).saveReimbursementBill(data,operationId);
            }else if(transfer&&currency().equals("CNY"))store().saveTransfer(data,operationId);
            else if(repayment)store().saveCreditRepayment(data,operationId);
            else if(candidateId==null&&editingId==0)store().saveManualTransaction(data,operationId);
            else if(editingId>0){
                JSONObject before=store().transaction(editingId);
                boolean marked=before!=null&&(before.optString("financial_type").equals("reimbursement")||before.optInt("bill_reimbursement")!=0);
                if(marked)new AccountingFinancialFlows(store()).editReimbursementBill(editingId,data,operationId);
                else if(data.optString("financial_type").equals("reimbursement"))AccountingCommitScope.run(store(),()->{
                    JSONObject ordinary=Json.copy(data);Json.put(ordinary,"financial_type","");Json.put(ordinary,"kind","expense");
                    store().saveTransaction(ordinary,editingId,candidateId);
                    return new AccountingFinancialFlows(store()).markReimbursement(editingId,true,operationId);
                });
                else store().saveTransaction(data,editingId,candidateId);
            }else store().saveTransaction(data,editingId,candidateId);
            committed=true;
            CaptureOverlay.complete(candidateId);CaptureNotifications.update();Ui.toast(this,editingId>0||typedId>0?"已保存修改":"已保存账单");if(another&&candidateId==null&&editingId==0&&typedId==0){committed=false;operationId=UUID.randomUUID().toString();amount="0";Json.put(data,"amount",0);Json.put(data,"remark","");Json.put(data,"merchant","");Json.put(data,"origin","");Json.put(data,"discount",0);Json.put(data,"fee",0);Json.put(data,"time",System.currentTimeMillis());Ui.text(root,"remark","");render();}else finish();}catch(Exception e){if(committed){Ui.toast(this,"账单已保存");finish();}else Ui.error(this,e);}finally{saving=false;}}
    @Override protected void onActivityResult(int request,int result,Intent intent){super.onActivityResult(request,result,intent);if(request==401&&result==RESULT_OK&&intent!=null&&intent.getData()!=null){FileTasks.run(this,"正在保存附件",()->LocalFiles.importAttachment(this,intent.getData()),file->{JSONArray files=Json.array(data.optString("attachments","[]"));if(files.length()>=100){Ui.toast(this,"单条账单最多100个附件");return;}files.put(file);Json.put(data,"attachments",files.toString());renderChips();Ui.toast(this,"附件已加入账单");});}}
    @Override protected void onResume(){super.onResume();if(root!=null){renderCategories();renderChips();}}
    @Override protected void onStart(){super.onStart();OriginalFinancialEffects.subscribe(store(),financialListener);}
    @Override protected void onStop(){OriginalFinancialEffects.unsubscribe(store(),financialListener);super.onStop();}
}
