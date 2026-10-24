package com.wangc.bill.reference.ui;

import android.content.ContentValues;
import com.wangc.bill.reference.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

/** Portable ledger CSV. The ZIP backup remains the format for attachments and full model state. */
public final class CsvFiles {
    private static final int MAX_ROWS=99999;
    private static final List<String> HEADERS=Arrays.asList("时间","类型","金额","币种","分类","账户","转入账户","备注","商户","来源","优惠","手续费",
            "tags_json","not_total","not_budget","settled","entry_kind","entry_key","financial_type","loan_parent_entry_key","interest","due_time",
            "account_origin","account_type","to_account_origin","to_account_type","bill_origin","generated_by_entry_key","related_entry_key","time_millis",
            "account_key","to_account_key","account_out_day","account_in_day","to_account_out_day","to_account_in_day","operation_kind","csv_version");
    private CsvFiles(){}

    public static String exportBook(long book){
        ReferenceStore store=ReferenceApplication.store();
        synchronized(store){
            if(store.book(book)==null)throw new IllegalArgumentException("目标账本不存在");
            LinkedHashMap<String,JSONObject> entries=new LinkedHashMap<>();
            // The UI query is deliberately capped at 2,000; exports must not use it.
            for(JSONObject bill:store.query("SELECT t.*,c.name category_name FROM transactions t LEFT JOIN categories c ON c.id=t.category_id WHERE t.book_id=? AND t.deleted=0 ORDER BY t.time,t.id",""+book)){
                Json.put(bill,"entry_kind","bill");Json.put(bill,"entry_id",bill.optLong("id"));Json.put(bill,"entry_key","bill:"+bill.optLong("id"));putEntry(entries,bill);
            }
            for(JSONObject receipt:store.query("SELECT entry_kind,entry_id FROM accounting_operations WHERE book_id=?",""+book))
                putTyped(store,entries,receipt.optString("entry_kind"),receipt.optLong("entry_id"));
            for(JSONObject candidate:store.query("SELECT c.id,c.save_kind FROM candidates c WHERE c.book_id=? AND c.state='SAVED' AND c.save_kind IN('transfer','lend')",""+book)){
                String kind=candidate.optString("save_kind");JSONObject receipt=store.one("SELECT "+kind+"_row_id FROM candidate_"+kind+"_receipts WHERE candidate_id=?",candidate.optString("id"));
                if(receipt!=null)putTyped(store,entries,kind,receipt.optLong(kind+"_row_id"));
            }
            for(JSONObject loan:AccountingFinancialFlows.entries(store,book,0,Long.MAX_VALUE,null,false))putEntry(entries,loan);
            if(entries.size()>MAX_ROWS)throw new IllegalArgumentException("CSV单次最多导出99999条，请改用完整备份");
            Map<Long,String> feeParents=new HashMap<>();
            for(JSONObject value:entries.values())if(!"bill".equals(value.optString("entry_kind"))&&value.optLong("fee_bill_id")>0){
                long fee=value.optLong("fee_bill_id");String old=feeParents.put(fee,value.optString("entry_key"));
                if(old!=null&&!old.equals(value.optString("entry_key")))throw new IllegalArgumentException("手续费关联重复，请使用完整备份");
            }
            List<JSONObject> ordered=new ArrayList<>(entries.values());ordered.sort(Comparator.comparingLong((JSONObject x)->x.optLong("time")).thenComparing(x->x.optString("entry_key")));
            List<List<String>> rows=new ArrayList<>();rows.add(HEADERS);
            for(JSONObject value:ordered){
                boolean bill="bill".equals(value.optString("entry_kind"));String cur=value.optString("currency","CNY");
                JSONObject from=store.assetRepository().byRowId(value.optLong("account_id")),to=store.assetRepository().byRowId(value.optLong("to_account_id"));
                String parent=value.optString("entry_kind").equals("lend")&&value.optLong("related_id")>0?"lend:"+value.optLong("related_id"):"";
                long when=value.optLong("time");long interest=value.optLong("interest");
                rows.add(Arrays.asList(Ui.date(when,"yyyy-MM-dd HH:mm:ss"),label(value),Money.text(value.optLong("amount"),cur),cur,
                        value.optString("category_name",value.optString("category")),name(from),name(to),value.optString("remark"),value.optString("merchant"),value.optString("origin"),
                        Money.text(value.optLong("discount"),cur),Money.text(value.optLong("fee"),cur),tagNames(store,value).toString(),flag(value,"not_total"),flag(value,"not_budget"),flag(value,"settled"),
                        value.optString("entry_kind"),value.optString("entry_key"),value.optString("financial_type"),parent,Money.text(interest,cur),value.optLong("in_time")>0?Long.toString(value.optLong("in_time")):"",
                        origin(from),assetType(from),origin(to),assetType(to),bill?value.optString("bill_origin","legacy"):"",bill?feeParents.getOrDefault(value.optLong("id"),""):"",
                        bill&&value.optLong("related_id")>0?"bill:"+value.optLong("related_id"):"",Long.toString(when),accountKey(from),accountKey(to),day(from,"out_account_date"),day(from,"in_account_date"),
                        day(to,"out_account_date"),day(to,"in_account_date"),value.optString("operation_kind"),"2"));
            }
            String output=CsvCodec.write(rows);if(output.length()>16*1024*1024)throw new IllegalArgumentException("CSV超过16MB，请使用完整备份");return output;
        }
    }
    private static void putTyped(ReferenceStore store,Map<String,JSONObject> entries,String kind,long id){JSONObject value=store.accountingEntry(kind,id);if(value.optInt("deleted")==0)putEntry(entries,value);}
    private static void putEntry(Map<String,JSONObject> entries,JSONObject value){entries.put(value.optString("entry_key"),Json.copy(value));if(entries.size()>MAX_ROWS)throw new IllegalArgumentException("CSV记录过多，请使用完整备份");}
    private static String label(JSONObject value){
        String kind=value.optString("entry_kind"),financial=value.optString("financial_type");
        if(kind.equals("lend")&&(financial.equals("lend")||financial.equals("borrow"))){int type=value.optInt("lend_type");return type==1?"借出":type==2?"收回借款":type==3?"借入":"归还借款";}
        if(kind.equals("lend"))return "信用还款";if(kind.equals("transfer"))return "转账";
        return value.optString("kind").equals("income")?"收入":value.optString("kind").equals("transfer")?"转账":"支出";
    }
    private static JSONArray tagNames(ReferenceStore store,JSONObject value){
        JSONArray names=new JSONArray();Set<String> seen=new LinkedHashSet<>();
        if(value.optString("entry_kind").equals("bill"))for(JSONObject row:store.query("SELECT g.name FROM transaction_tags x JOIN tags g ON g.id=x.tag_id WHERE x.transaction_id=? ORDER BY g.id",""+value.optLong("entry_id")))seen.add(row.optString("name"));
        else{JSONArray ids=value.optJSONArray("tag_ids");if(ids!=null)for(int i=0;i<ids.length();i++){JSONObject tag=store.one("SELECT name FROM tags WHERE id=?",""+ids.optLong(i));if(tag!=null)seen.add(tag.optString("name"));}}
        for(String name:seen)names.put(name);return names;
    }
    private static String name(JSONObject value){return value==null?"":value.optString("name");}
    private static String origin(JSONObject value){return value==null?"":value.optString("asset_origin","legacy");}
    private static String assetType(JSONObject value){return value==null?"":origin(value).equals(AssetRepository.ORIGINAL)?""+value.optInt("asset_type"):value.optString("type","cash");}
    private static String accountKey(JSONObject value){return value==null?"":origin(value)+":"+value.optLong("id");}
    private static String day(JSONObject value,String key){return value==null||value.isNull(key)?"":value.optString(key);}
    private static String flag(JSONObject value,String key){return value.optBoolean(key)||value.optInt(key)!=0?"1":"0";}

    public static int[] importBook(String text,long book){
        ReferenceStore store=ReferenceApplication.store();List<List<String>> rows=CsvCodec.parse(text);
        if(rows.size()<2)throw new IllegalArgumentException("CSV需要表头和至少一条账单");
        if(rows.size()-1>MAX_ROWS)throw new IllegalArgumentException("CSV单次最多导入99999条");
        return AccountingCommitScope.run(store,()->new Importer(store,book,rows,LocalFiles.hash(text)).run());
    }
    private static final class Importer{
        final ReferenceStore store;final long book;final JSONObject ledger;final List<List<String>> rows;final List<String> headers;final String fingerprint;final boolean extended;
        final Map<String,Integer> sourceRows=new HashMap<>();final Map<Integer,JSONObject> imported=new HashMap<>();final Set<Integer> visiting=new HashSet<>();
        final Map<String,Long> accounts=new HashMap<>();final Map<String,String> accountDefinitions=new HashMap<>();final Map<Long,String> accountOwners=new HashMap<>();int added,skipped;
        Importer(ReferenceStore store,long book,List<List<String>> rows,String fingerprint){
            this.store=store;this.book=book;this.rows=rows;this.headers=rows.get(0);this.fingerprint=fingerprint;ledger=store.book(book);
            if(ledger==null||ledger.optInt("archived")!=0)throw new IllegalArgumentException("请选择有效目标账本");
            if(column(headers,"时间","日期","记账时间","交易时间")<0||column(headers,"金额","交易金额")<0||column(headers,"类型","收支类型","收支")<0)throw new IllegalArgumentException("CSV必须有时间、类型、金额列");
            Set<String> unique=new HashSet<>();for(String h:headers)if(!unique.add(h.trim()))throw new IllegalArgumentException("CSV存在重复表头");
            extended=column(headers,"csv_version")>=0;
            for(int i=1;i<rows.size();i++){
                if(extended&&!get(i,"csv_version").equals("2"))throw line(i,new IllegalArgumentException("CSV版本不受支持"));
                String key=get(i,"entry_key");if(!key.isEmpty()&&sourceRows.put(key,i)!=null)throw line(i,new IllegalArgumentException("entry_key重复"));
            }
        }
        int[] run(){for(int i=1;i<rows.size();i++)importRow(i);return new int[]{added,skipped};}
        JSONObject importRow(int index){
            if(imported.containsKey(index))return imported.get(index);
            if(!visiting.add(index))throw line(index,new IllegalArgumentException("记录关联形成循环"));
            try{
                String hash=LocalFiles.hash(book+":"+fingerprint+":"+index),operation=UUID.nameUUIDFromBytes(("csv:"+hash).getBytes(StandardCharsets.UTF_8)).toString();
                JSONObject previous=prior(operation,hash);
                if(previous!=null){imported.put(index,previous);skipped++;return previous;}
                String cur=get(index,"币种");if(cur.isEmpty())cur=ledger.optString("currency");
                if(!cur.equals(ledger.optString("currency")))throw new IllegalArgumentException("币种与目标账本不同，请分别导入");
                String type=get(index,"类型","收支类型","收支"),entry=extended?get(index,"entry_kind"):"bill";
                if(entry.isEmpty())entry="bill";if(!Arrays.asList("bill","transfer","lend").contains(entry))throw new IllegalArgumentException("记录类别无效");
                String k=type.equals("收入")||type.equals("income")?"income":type.equals("支出")||type.equals("expense")?"expense":"transfer";
                if(entry.equals("bill")&&!(type.equals("收入")||type.equals("income")||type.equals("支出")||type.equals("expense")||type.equals("转账")||type.equals("transfer")))throw new IllegalArgumentException("普通记录类型无效");
                JSONObject value=Json.object("book_id",book,"kind",k,"amount",Money.positive(get(index,"金额","交易金额"),cur),"currency",cur,
                        "remark",get(index,"备注","说明"),"merchant",get(index,"商户","商家"),"origin",get(index,"来源"),"time",when(index),
                        "discount",money(index,"优惠",cur),"fee",money(index,"手续费",cur),"not_total",bit(index,"not_total"),"not_budget",bit(index,"not_budget"),"settled",bit(index,"settled"),"tag_ids",tags(index));
                boolean typed=!entry.equals("bill"),original=typed||extended&&get(index,"bill_origin").equals("original");
                if(typed&&!cur.equals("CNY"))throw new IllegalArgumentException("转账、信用还款和借贷只支持人民币");
                String financial=get(index,"financial_type"),generated=get(index,"generated_by_entry_key");
                if(!generated.isEmpty()){
                    JSONObject parent=dependency(index,generated);long fee=parent.optLong("fee_bill_id");
                    JSONObject actual=fee>0?store.transaction(fee):null;
                    if(actual==null||actual.optLong("amount")!=value.optLong("amount")||!actual.optString("kind").equals(k))throw new IllegalArgumentException("手续费行与关联记录不一致");
                    Json.put(value,"account_id",actual.optLong("account_id"));Json.put(value,"to_account_id",0);Json.put(value,"category_id",category(index,k,original));
                    if(value.optLong("fee")!=0)throw new IllegalArgumentException("关联手续费行不能再次包含手续费");
                    store.saveTransaction(value,fee,null);stampBill(fee,operation,hash,value);JSONObject result=bill(fee);imported.put(index,result);added++;return result;
                }
                Json.put(value,"account_id",account(index,false,original,typed?1:0));Json.put(value,"to_account_id",account(index,true,typed,0));
                JSONObject result;
                if(entry.equals("transfer")||entry.equals("lend")&&!financial.equals("lend")&&!financial.equals("borrow")){
                    Json.put(value,"transfer_mode",0); // Exported amount is the actual raw principal, not a fee-inclusive editor amount.
                    result=entry.equals("transfer")?store.saveTransfer(value,operation):store.saveCreditRepayment(value,operation);
                }else if(entry.equals("lend")){
                    Json.put(value,"financial_type",financial);Json.put(value,"interest",money(index,"interest",cur));Json.put(value,"in_time",optionalLong(index,"due_time"));Json.put(value,"counterparty",value.optString("merchant"));
                    String parent=get(index,"loan_parent_entry_key");
                    String expected=parent.isEmpty()?(financial.equals("lend")?"loan":"borrow"):(financial.equals("lend")?"collection":"repayment");
                    if(!expected.equals(get(index,"operation_kind")))throw new IllegalArgumentException("借贷方向或父记录缺失，不能改为另一类借贷");
                    if(parent.isEmpty())result=new AccountingFinancialFlows(store).saveLoan(value,operation);
                    else{JSONObject loan=dependency(index,parent);
                        if(!"lend".equals(loan.optString("entry_kind"))||!financial.equals(loan.optString("financial_type"))||!(loan.optInt("lend_type")==1||loan.optInt("lend_type")==3))throw new IllegalArgumentException("还款父记录必须为同方向的原借出或借入");
                        result=new AccountingFinancialFlows(store).settleLoan(loan.optLong("entry_id"),value,operation);}
                }else{
                    Json.put(value,"category_id",category(index,k,original));Json.put(value,"import_key",hash);Json.put(value,"financial_type",financial);
                    String related=get(index,"related_entry_key");if(!related.isEmpty()){JSONObject parent=dependency(index,related);if(!"bill".equals(parent.optString("entry_kind")))throw new IllegalArgumentException("普通回款父记录必须是普通账单");Json.put(value,"related_id",parent.optLong("entry_id"));}
                    long id;
                    if(original){if(!financial.isEmpty()||value.optLong("related_id")>0)throw new IllegalArgumentException("原版退款或报销聚合请使用完整备份恢复，不会降为普通收支");id=store.saveManualTransaction(value,operation);}
                    else id=store.saveLegacyImport(value);
                    stampBill(id,operation,hash,value);result=bill(id);
                }
                preserveTypedMetadata(result,value);imported.put(index,result);added++;return result;
            }catch(Exception error){throw line(index,error);}finally{visiting.remove(index);}
        }
        JSONObject dependency(int child,String key){Integer row=sourceRows.get(key);if(row==null)throw new IllegalArgumentException("关联的父记录不在此文件中："+key);return importRow(row);}
        JSONObject prior(String operation,String hash){
            JSONObject row=store.one("SELECT id FROM transactions WHERE manual_operation_id=? OR import_key=?",operation,hash);if(row!=null)return bill(row.optLong("id"));
            row=store.one("SELECT payload FROM accounting_operations WHERE operation_id=?",operation);if(row!=null)return Json.parse(row.optString("payload"));
            row=store.one("SELECT payload FROM accounting_financial_operations WHERE operation_id=?",operation);return row==null?null:Json.parse(row.optString("payload"));
        }
        JSONObject bill(long id){JSONObject value=store.transaction(id);Json.put(value,"entry_kind","bill");Json.put(value,"entry_id",id);Json.put(value,"entry_key","bill:"+id);return value;}
        void stampBill(long id,String operation,String hash,JSONObject value){
            ContentValues fields=new ContentValues();fields.put("manual_operation_id",operation);fields.put("import_key",hash);fields.put("settled",value.optInt("settled"));
            AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("transactions",fields,"id=?",new String[]{""+id}));
        }
        void preserveTypedMetadata(JSONObject result,JSONObject input){
            if("bill".equals(result.optString("entry_kind")))return;
            for(String key:new String[]{"merchant","origin","not_total","not_budget","settled","tag_ids"})Json.put(result,key,input.opt(key));
            String operation=result.optString("operation_id");ContentValues values=new ContentValues();values.put("payload",result.toString());
            String table=result.optString("entry_kind").equals("lend")&&AccountingFinancialFlows.ownsLend(store,result.optLong("entry_id"))?"accounting_financial_operations":"accounting_operations";
            AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update(table,values,"operation_id=?",new String[]{operation}));
        }
        long category(int row,String kind,boolean original){
            if(kind.equals("transfer"))return store.defaultCategory("expense",book);
            String name=get(row,"分类","一级分类");if(name.isEmpty())return store.defaultCategory(kind,book);
            List<JSONObject> matches=store.query("SELECT * FROM categories WHERE name=? AND kind=? AND archived=0"+(original?" AND level<>'legacy'":"")+" ORDER BY id",name,kind);
            return matches.isEmpty()?store.saveCategory(0,name,kind,kind.equals("income")?"ic_income_other":"ic_parent_other"):matches.get(0).optLong("id");
        }
        long account(int row,boolean destination,boolean forceOriginal,int defaultType){
            String name=get(row,destination?"转入账户":"账户",destination?"转入账户":"付款账户").trim();
            if(name.isEmpty()||name.equals("null")||name.equals("无账户"))return 0;
            if(name.length()>100)throw new IllegalArgumentException("账户名称过长");
            String cur=ledger.optString("currency"),rawOrigin=get(row,destination?"to_account_origin":"account_origin");
            String origin=forceOriginal?AssetRepository.ORIGINAL:rawOrigin.isEmpty()?AssetRepository.LEGACY:rawOrigin;
            if(!origin.equals(AssetRepository.ORIGINAL)&&!origin.equals(AssetRepository.LEGACY))throw new IllegalArgumentException("账户来源无效");
            if(forceOriginal&&!rawOrigin.isEmpty()&&!rawOrigin.equals(AssetRepository.ORIGINAL))throw new IllegalArgumentException("此金融记录需要原版账户，不能改接同名旧账户");
            String type=get(row,destination?"to_account_type":"account_type");if(type.isEmpty())type=origin.equals(AssetRepository.ORIGINAL)?""+(defaultType==0?1:defaultType):"cash";
            String source=get(row,destination?"to_account_key":"account_key");String key=source.isEmpty()?origin+"|"+type+"|"+name+"|"+cur:source;
            String definition=origin+"|"+type+"|"+name+"|"+cur;String previous=accountDefinitions.put(key,definition);
            if(previous!=null&&!previous.equals(definition))throw new IllegalArgumentException("同一账户键出现不同名称或类型，请先核对CSV");
            if(accounts.containsKey(key))return accounts.get(key);
            List<JSONObject> matches;
            int originalType=0;
            if(origin.equals(AssetRepository.ORIGINAL)){
                if(!cur.equals("CNY"))throw new IllegalArgumentException("原版账户CSV只支持人民币");originalType=Integer.parseInt(type);
                if(originalType<1||originalType>8)throw new IllegalArgumentException("此原版账户类型不能由CSV创建，请使用完整备份");
                matches=store.query("SELECT * FROM accounts WHERE name=? AND asset_origin='original' AND owner_semantic_id=? AND asset_type=? AND archived=0 AND (asset_currency IS NULL OR asset_currency='') ORDER BY id",name,""+ReferenceStore.LOCAL_OFFLINE_OWNER,""+originalType);
            }else matches=store.query("SELECT * FROM accounts WHERE name=? AND asset_origin='legacy' AND currency=? AND type=? AND archived=0 ORDER BY id",name,cur,type);
            List<JSONObject> unused=new ArrayList<>();for(JSONObject match:matches)if(!accountOwners.containsKey(match.optLong("id"))||accountOwners.get(match.optLong("id")).equals(key))unused.add(match);
            if(unused.size()>1)throw new IllegalArgumentException("存在多个同名同类型账户，请先重命名后导入："+name);
            long id;
            if(!unused.isEmpty())id=unused.get(0).optLong("id");
            else if(origin.equals(AssetRepository.LEGACY))id=store.saveAccount(0,name,type,cur,0);
            else{
                AssetRepository.Definition assetDefinition=new AssetRepository.Definition(name,originalType,null);assetDefinition.intoTotalAsset=true;
                if(!ledger.isNull("semantic_id"))assetDefinition.showBook.add(ledger.optLong("semantic_id"));
                assetDefinition.outAccountDate=get(row,destination?"to_account_out_day":"account_out_day");assetDefinition.inAccountDate=get(row,destination?"to_account_in_day":"account_in_day");
                id=store.assetRepository().createOriginal(ReferenceStore.LOCAL_OFFLINE_OWNER,assetDefinition);
                new OriginalFinancialEffects(store,ReferenceStore.LOCAL_OFFLINE_OWNER,ReferenceStore.LOCAL_OFFLINE_OWNER).assetCreatedOrEdited(store.assetRepository().byRowId(id).optLong("semantic_id"));
            }
            accounts.put(key,id);accountOwners.put(id,key);return id;
        }
        JSONArray tags(int row){String raw=get(row,"tags_json","标签");JSONArray values=new JSONArray();if(raw.isEmpty())return values;
            JSONArray names=raw.trim().startsWith("[")?Json.array(raw):new JSONArray(Arrays.asList(raw.split("[;；]")));Set<Long> seen=new HashSet<>();
            for(int i=0;i<names.length();i++){Object item=names.opt(i);if(!(item instanceof String))throw new IllegalArgumentException("标签必须为名称字符串");String name=((String)item).trim();if(name.isEmpty()||name.length()>100)throw new IllegalArgumentException("标签名称无效");
                JSONObject old=store.one("SELECT id FROM tags WHERE name=? ORDER BY id LIMIT 1",name);long id=old==null?store.saveTag(name):old.optLong("id");if(seen.add(id))values.put(id);}
            return values;
        }
        String get(int row,String... names){int at=column(headers,names);return at<0||at>=rows.get(row).size()?"":rows.get(row).get(at);}
        long money(int row,String key,String cur){String value=get(row,key);return value.isEmpty()?0:Money.signed(value,cur);}
        long optionalLong(int row,String key){String raw=get(row,key).trim();if(raw.isEmpty())return 0;try{long value=Long.parseLong(raw);if(value<0||value>8640000000000000L)throw new NumberFormatException();return value;}catch(NumberFormatException invalid){throw new IllegalArgumentException(key+"无效");}}
        long when(int row){long raw=optionalLong(row,"time_millis");return raw>0?raw:parseDate(get(row,"时间","日期","记账时间","交易时间"));}
        int bit(int row,String key){String raw=get(row,key).trim();if(raw.isEmpty()||raw.equals("0")||raw.equalsIgnoreCase("false")||raw.equals("否"))return 0;if(raw.equals("1")||raw.equalsIgnoreCase("true")||raw.equals("是"))return 1;throw new IllegalArgumentException(key+"必须为0或1");}
    }
    private static IllegalArgumentException line(int index,Exception cause){return new IllegalArgumentException("第"+(index+1)+"行："+(cause.getMessage()==null?"数据无效":cause.getMessage()),cause);}
    private static int column(List<String> headers,String... options){for(String option:options)for(int i=0;i<headers.size();i++)if(option.equals(headers.get(i).trim()))return i;return -1;}
    private static long parseDate(String raw){for(String pattern:new String[]{"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm","yyyy-MM-dd","yyyy/MM/dd HH:mm:ss","yyyy/MM/dd"}){ParsePosition p=new ParsePosition(0);SimpleDateFormat f=new SimpleDateFormat(pattern,Locale.CHINA);f.setLenient(false);Date d=f.parse(raw.trim(),p);if(d!=null&&p.getIndex()==raw.trim().length())return d.getTime();}throw new IllegalArgumentException("时间格式无效");}
}
