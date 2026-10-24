package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Offline goal progress and recurring drafts. Dream's asset/income/balance,
 * ratio, book/date scope and log fields and Cycle's end/count/interval/tags/files
 * are evidenced in the original DEX model inventory. These named modes are a
 * host API, not guessed values of the original private integer enums.
 * Calendar advancement reuses BeeCount recurring_transaction_service's retained
 * target day, month-end/leap-year clamp and unavailable-account pause behavior.
 */
public final class LocalGoalPlans {
    public static final String GOALS="local_goal_config",HISTORY="local_goal_entries",PLANS="local_plan_config";
    private static final long MAX=99999999999999L,MAX_TIME=8640000000000000L;
    private final ReferenceStore store;
    public LocalGoalPlans(ReferenceStore store){if(store==null)throw new IllegalArgumentException("数据库不能为空");this.store=store;}

    /** Three additive tables; old goal/plan primary keys and payloads stay intact. */
    public static void installV11Schema(SQLiteDatabase db){
        db.execSQL("CREATE TABLE local_goal_config(goal_id INTEGER PRIMARY KEY REFERENCES goals(id) ON DELETE CASCADE,currency TEXT NOT NULL DEFAULT 'CNY',mode TEXT NOT NULL DEFAULT 'manual' CHECK(mode IN('manual','assets','income','expense','balance')),asset_mode TEXT NOT NULL DEFAULT 'net' CHECK(asset_mode IN('net','deposit')),ratio_bps INTEGER NOT NULL DEFAULT 10000 CHECK(ratio_bps BETWEEN 0 AND 10000),start_time INTEGER NOT NULL DEFAULT 0 CHECK(start_time>=0),end_time INTEGER NOT NULL DEFAULT 0 CHECK(end_time>=0),book_ids TEXT NOT NULL DEFAULT '[]',account_ids TEXT NOT NULL DEFAULT '[]')");
        db.execSQL("CREATE TABLE local_goal_entries(id INTEGER PRIMARY KEY AUTOINCREMENT,goal_id INTEGER NOT NULL REFERENCES goals(id) ON DELETE CASCADE,operation_id TEXT NOT NULL UNIQUE,delta INTEGER NOT NULL,balance INTEGER NOT NULL,time INTEGER NOT NULL,remark TEXT NOT NULL DEFAULT '')");
        db.execSQL("CREATE INDEX local_goal_entries_time ON local_goal_entries(goal_id,time DESC,id DESC)");
        db.execSQL("CREATE TABLE local_plan_config(plan_id INTEGER PRIMARY KEY REFERENCES plans(id) ON DELETE CASCADE,every_n INTEGER NOT NULL DEFAULT 1 CHECK(every_n BETWEEN 1 AND 1000),max_occurrences INTEGER NOT NULL DEFAULT 0 CHECK(max_occurrences BETWEEN 0 AND 100000),until_time INTEGER NOT NULL DEFAULT 0 CHECK(until_time>=0),emitted_count INTEGER NOT NULL DEFAULT 0 CHECK(emitted_count>=0),last_generated INTEGER NOT NULL DEFAULT 0 CHECK(last_generated>=0),last_error TEXT NOT NULL DEFAULT '')");
    }

    public List<JSONObject> goals(){synchronized(store){List<JSONObject> result=new ArrayList<>();for(JSONObject row:store.query("SELECT id FROM goals ORDER BY id DESC"))result.add(goal(row.optLong("id")));return result;}}
    public JSONObject goal(long id){synchronized(store){
        JSONObject row=store.one("SELECT * FROM goals WHERE id=?",""+id);if(row==null)throw new IllegalArgumentException("目标不存在");
        JSONObject config=goalConfig(id);long automatic=automatic(config),progress=Math.max(0,Math.addExact(row.optLong("saved"),automatic));
        JSONObject result=Json.copy(row);copy(config,result,"currency","mode","asset_mode","ratio_bps","start_time","end_time");
        Json.put(result,"book_ids",ids(config.optString("book_ids")));Json.put(result,"account_ids",ids(config.optString("account_ids")));
        Json.put(result,"automatic",automatic);Json.put(result,"progress",progress);Json.put(result,"remaining",Math.max(0,row.optLong("target")-progress));
        Json.put(result,"completed",progress>=row.optLong("target"));return result;
    }}
    public long saveGoal(long id,JSONObject input){JSONObject value=Json.copy(input);return AccountingCommitScope.run(store,()->{
        String name=name(value.optString("name")),currency=value.optString("currency","CNY"),mode=value.optString("mode","manual"),assetMode=value.optString("asset_mode","net");Money.scale(currency);
        if(!oneOf(mode,"manual","assets","income","expense","balance")||!oneOf(assetMode,"net","deposit"))throw new IllegalArgumentException("目标进度方式无效");
        long target=positive(value,"target"),saved=integer(value,"saved",0),start=timestamp(value,"start_time",0),end=timestamp(value,"end_time",0);
        if(saved< -MAX||saved>MAX||mode.equals("manual")&&saved<0||end!=0&&end<start)throw new IllegalArgumentException("目标起始金额或日期范围无效");
        int ratio=range(value,"ratio_bps",10000,0,10000);JSONArray books=array(value,"book_ids"),accounts=array(value,"account_ids");
        validateSelection(store,currency,books,accounts);
        JSONObject old=id==0?null:store.one("SELECT * FROM goals WHERE id=?",""+id);if(id>0&&old==null)throw new IllegalArgumentException("目标不存在");
        if(old!=null&&!currency.equals(goalConfig(id).optString("currency")))throw new IllegalArgumentException("已有目标的币种不能更改，请新建另一币种目标以保留历史含义");
        ContentValues goal=new ContentValues();goal.put("name",name);goal.put("target",target);goal.put("saved",saved);
        long row=id;if(row==0){goal.put("created",System.currentTimeMillis());row=store.getWritableDatabase().insertOrThrow("goals",null,goal);if(row<=0)throw new IllegalStateException("目标未能保存");}
        else AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("goals",goal,"id=?",new String[]{""+row}));
        ContentValues config=new ContentValues();config.put("goal_id",row);config.put("currency",currency);config.put("mode",mode);config.put("asset_mode",assetMode);config.put("ratio_bps",ratio);
        config.put("start_time",start);config.put("end_time",end);config.put("book_ids",books.toString());config.put("account_ids",accounts.toString());
        upsert(GOALS,"goal_id",row,config);
        long delta=saved-(old==null?0:old.optLong("saved"));if(delta!=0)journal(row,delta,goal(row).optLong("progress"),System.currentTimeMillis(),old==null?"设置起始进度":"编辑起始进度",UUID.randomUUID().toString());
        return row;
    });}
    /** Adjusts goal progress only. It never pretends to transfer money between assets. */
    public JSONObject adjustGoal(long id,long delta,String remark,long time,String operationId){String key=uuid(operationId);return AccountingCommitScope.run(store,()->{
        JSONObject prior=store.one("SELECT * FROM local_goal_entries WHERE operation_id=?",key);if(prior!=null){if(prior.optLong("goal_id")!=id)throw new IllegalArgumentException("调整标识已用于另一目标");return goal(id);}
        if(delta==0||delta< -MAX||delta>MAX||time<=0||time>MAX_TIME)throw new IllegalArgumentException("请输入有效的调整金额与时间");
        JSONObject previous=goal(id);long progress=Math.addExact(previous.optLong("progress"),delta),saved=Math.subtractExact(progress,previous.optLong("automatic"));
        if(saved< -MAX||saved>MAX||progress<0||previous.optString("mode").equals("manual")&&saved<0)throw new IllegalArgumentException("调整后进度不能小于零");
        ContentValues update=new ContentValues();update.put("saved",saved);AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("goals",update,"id=?",new String[]{""+id}));
        journal(id,delta,goal(id).optLong("progress"),time,text(remark,2000),key);return goal(id);
    });}
    public List<JSONObject> goalHistory(long id){synchronized(store){goal(id);return store.query("SELECT * FROM local_goal_entries WHERE goal_id=? ORDER BY time DESC,id DESC",""+id);}}
    public void deleteGoal(long id){AccountingCommitScope.run(store,()->{AccountingCommitScope.requireChanged(store,store.getWritableDatabase().delete("goals","id=?",new String[]{""+id}));return null;});}
    private void journal(long goal,long delta,long balance,long time,String remark,String uuid){ContentValues entry=new ContentValues();entry.put("goal_id",goal);entry.put("operation_id",uuid);entry.put("delta",delta);entry.put("balance",balance);entry.put("time",time);entry.put("remark",remark);if(store.getWritableDatabase().insertOrThrow(HISTORY,null,entry)<=0)throw new IllegalStateException("目标调整历史未能保存");}

    private JSONObject goalConfig(long id){JSONObject row=store.one("SELECT * FROM local_goal_config WHERE goal_id=?",""+id);return row==null?Json.object("goal_id",id,"currency","CNY","mode","manual","asset_mode","net","ratio_bps",10000,"start_time",0,"end_time",0,"book_ids","[]","account_ids","[]"):row;}
    private long automatic(JSONObject config){
        String mode=config.optString("mode"),currency=config.optString("currency");if(mode.equals("manual"))return 0;
        long total=0;JSONArray accounts=ids(config.optString("account_ids")),books=ids(config.optString("book_ids"));
        if(mode.equals("assets")){
            for(JSONObject row:store.query("SELECT * FROM accounts")){
                if(accounts.length()==0&&row.optInt("archived")!=0||accounts.length()>0&&!contains(accounts,row.optLong("id")))continue;
                if(!displayCurrency(row).equals(currency))continue;
                long amount=store.accountBalance(row.optLong("id"));if(config.optString("asset_mode").equals("deposit")&&amount<0)continue;total=Math.addExact(total,amount);
            }
        }else{
            long start=config.optLong("start_time"),end=config.optLong("end_time");
            String amount=mode.equals("income")?"CASE WHEN kind='income' THEN amount ELSE 0 END":mode.equals("expense")?"CASE WHEN kind='expense' THEN amount+fee ELSE fee END":"CASE WHEN kind='income' THEN amount-fee WHEN kind='expense' THEN -amount-fee ELSE -fee END";
            StringBuilder sql=new StringBuilder("SELECT COALESCE(SUM("+amount+"),0) n FROM transactions WHERE deleted=0 AND not_total=0 AND currency=? AND time>=? AND time<=?");
            List<String> args=new ArrayList<>();args.add(currency);args.add(""+start);args.add(""+(end==0?MAX_TIME:end));
            in(sql,args,"book_id",books);in(sql,args,"account_id",accounts);total=store.scalar(sql.toString(),args.toArray(new String[0]));
        }
        return BigDecimal.valueOf(total).multiply(BigDecimal.valueOf(config.optInt("ratio_bps"))).divide(BigDecimal.valueOf(10000),0,RoundingMode.HALF_UP).longValueExact();
    }

    public List<JSONObject> plans(){synchronized(store){List<JSONObject> result=new ArrayList<>();for(JSONObject row:store.query("SELECT id FROM plans ORDER BY next_due,id"))result.add(plan(row.optLong("id")));return result;}}
    public JSONObject plan(long id){synchronized(store){JSONObject row=store.one("SELECT * FROM plans WHERE id=?",""+id);if(row==null)throw new IllegalArgumentException("周期规则不存在");JSONObject result=Json.copy(row),config=planConfig(id);copy(config,result,"every_n","max_occurrences","until_time","emitted_count","last_generated","last_error");Json.put(result,"data",Json.parse(row.optString("payload")));return result;}}
    public long savePlan(long id,JSONObject input){JSONObject value=Json.copy(input);return AccountingCommitScope.run(store,()->{
        String name=name(value.optString("name")),frequency=value.optString("interval");if(!oneOf(frequency,"daily","weekly","monthly","yearly"))throw new IllegalArgumentException("请选择有效周期");
        JSONObject payload=value.optJSONObject("data");if(payload==null)throw new IllegalArgumentException("周期账单内容缺失");payload=Json.copy(payload);validatePlanPayload(store,payload,true);
        long due=timestamp(value,"next_due",0),until=timestamp(value,"until_time",0);if(due<=0||until!=0&&due>until)throw new IllegalArgumentException("首次日期或截至日期无效");
        int every=range(value,"every_n",1,1,1000),limit=range(value,"max_occurrences",0,0,100000);
        JSONObject previous=id==0?null:store.one("SELECT * FROM plans WHERE id=?",""+id);if(id>0&&previous==null)throw new IllegalArgumentException("周期规则不存在");
        JSONObject config=planConfig(id);long last=config.optLong("last_generated");
        if(id>0&&store.one("SELECT plan_id FROM local_plan_config WHERE plan_id=?",""+id)==null&&due<previous.optLong("next_due"))throw new IllegalArgumentException("旧规则没有历史生成标记，首次升级时不能将下一次日期提前");
        Calendar anchor=Calendar.getInstance();anchor.setTimeInMillis(due);
        JSONObject oldPayload=previous==null?null:Json.parse(previous.optString("payload"));
        boolean retainAnchor=previous!=null&&due==previous.optLong("next_due")&&frequency.equals(previous.optString("interval"));
        Json.put(payload,"anchor_day",retainAnchor?oldPayload.optInt("anchor_day",anchor.get(Calendar.DAY_OF_MONTH)):anchor.get(Calendar.DAY_OF_MONTH));
        Json.put(payload,"anchor_month",retainAnchor?oldPayload.optInt("anchor_month",anchor.get(Calendar.MONTH)):anchor.get(Calendar.MONTH));
        ContentValues values=new ContentValues();values.put("name",name);values.put("interval",frequency);values.put("payload",payload.toString());values.put("next_due",due);values.put("enabled",flag(value,"enabled",true)?1:0);
        long row=id;if(row==0){row=store.getWritableDatabase().insertOrThrow("plans",null,values);if(row<=0)throw new IllegalStateException("周期规则未保存");}
        else AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("plans",values,"id=?",new String[]{""+row}));
        ContentValues options=new ContentValues();options.put("plan_id",row);options.put("every_n",every);options.put("max_occurrences",limit);options.put("until_time",until);options.put("emitted_count",config.optLong("emitted_count"));options.put("last_generated",last);options.put("last_error","");upsert(PLANS,"plan_id",row,options);return row;
    });}
    public void enablePlan(long id,boolean enabled){AccountingCommitScope.run(store,()->{JSONObject rule=plan(id);if(enabled){validatePlanPayload(store,rule.optJSONObject("data"),true);if(ended(rule.optLong("next_due"),rule))throw new IllegalArgumentException("此周期规则已达到结束条件，请先编辑规则");}ContentValues values=new ContentValues();values.put("enabled",enabled?1:0);AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update("plans",values,"id=?",new String[]{""+id}));store.getWritableDatabase().execSQL("UPDATE local_plan_config SET last_error='' WHERE plan_id=?",new Object[]{id});return null;});}
    public void deletePlan(long id){AccountingCommitScope.run(store,()->{AccountingCommitScope.requireChanged(store,store.getWritableDatabase().delete("plans","id=?",new String[]{""+id}));return null;});}
    private JSONObject planConfig(long id){JSONObject value=store.one("SELECT * FROM local_plan_config WHERE plan_id=?",""+id);return value==null?Json.object("plan_id",id,"every_n",1,"max_occurrences",0,"until_time",0,"emitted_count",0,"last_generated",0,"last_error",""):value;}

    /** Each rule commits independently; one stale account cannot starve unrelated rules. */
    public int processDue(long now){synchronized(store){
        if(now<=0||now>MAX_TIME)throw new IllegalArgumentException("检查时间无效");SQLiteDatabase db=store.getWritableDatabase();if(db.inTransaction())throw new IllegalStateException("周期检查不能加入外部事务");int generated=0;
        for(JSONObject listed:store.query("SELECT id FROM plans WHERE enabled=1 AND next_due<=? ORDER BY next_due,id LIMIT 50",""+now)){
            long id=listed.optLong("id");int added=0;RuntimeException failure=null;db.beginTransaction();
            try{
                JSONObject current=plan(id),payload=current.optJSONObject("data");validatePlanPayload(store,payload,true);
                long due=current.optLong("next_due"),last=current.optLong("last_generated"),count=current.optLong("emitted_count");
                int attempts=0;boolean stop=false;
                while(due<=now&&attempts++<12){
                    Json.put(current,"emitted_count",count);if(ended(due,current)){stop=true;break;}
                    if(due>last){JSONObject draft=Json.copy(payload);Json.put(draft,"time",due);Json.put(draft,"origin","周期记账");Json.put(draft,"plan_id",id);Json.put(draft,"plan_scheduled_at",due);Json.put(draft,"capture_requires_confirmation",true);
                        store.addCandidate(draft,"local.plan",payload.optLong("book_id"));count++;last=due;added++;}
                    due=next(due,current.optString("interval"),current.optInt("every_n"),payload);
                }
                Json.put(current,"emitted_count",count);stop|=ended(due,current);
                ContentValues state=new ContentValues();state.put("next_due",due);if(stop)state.put("enabled",0);AccountingCommitScope.requireChanged(store,db.update("plans",state,"id=?",new String[]{""+id}));
                ContentValues config=new ContentValues();config.put("plan_id",id);config.put("every_n",current.optInt("every_n"));config.put("max_occurrences",current.optInt("max_occurrences"));config.put("until_time",current.optLong("until_time"));config.put("emitted_count",count);config.put("last_generated",last);config.put("last_error","");upsert(PLANS,"plan_id",id,config);
                db.setTransactionSuccessful();
            }catch(RuntimeException error){failure=error;}finally{db.endTransaction();}
            if(failure==null)generated+=added;
            else pause(id,failure instanceof IllegalArgumentException?"规则引用的账本、账户或内容已变化，请打开规则修正后启用":"待确认列表或本地保存未完成，请检查后重新启用");
        }
        return generated;
    }}
    private void pause(long id,String reason){SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{JSONObject previous=planConfig(id);ContentValues config=new ContentValues();config.put("plan_id",id);config.put("every_n",previous.optInt("every_n"));config.put("max_occurrences",previous.optInt("max_occurrences"));config.put("until_time",previous.optLong("until_time"));config.put("emitted_count",previous.optLong("emitted_count"));config.put("last_generated",previous.optLong("last_generated"));config.put("last_error",reason);upsert(PLANS,"plan_id",id,config);db.execSQL("UPDATE plans SET enabled=0 WHERE id=?",new Object[]{id});db.setTransactionSuccessful();}finally{db.endTransaction();}}
    private static boolean ended(long due,JSONObject rule){return rule.optLong("until_time")>0&&due>rule.optLong("until_time")||rule.optLong("max_occurrences")>0&&rule.optLong("emitted_count")>=rule.optLong("max_occurrences");}
    private static long next(long due,String frequency,int every,JSONObject payload){
        Calendar date=Calendar.getInstance();date.setTimeInMillis(due);
        if(frequency.equals("monthly")||frequency.equals("yearly")){int day=payload.optInt("anchor_day",date.get(Calendar.DAY_OF_MONTH));date.set(Calendar.DAY_OF_MONTH,1);date.add(frequency.equals("monthly")?Calendar.MONTH:Calendar.YEAR,every);
            if(frequency.equals("yearly"))date.set(Calendar.MONTH,payload.optInt("anchor_month",date.get(Calendar.MONTH)));
            date.set(Calendar.DAY_OF_MONTH,Math.min(day,date.getActualMaximum(Calendar.DAY_OF_MONTH)));
        }else date.add(Calendar.DATE,Math.multiplyExact(every,frequency.equals("weekly")?7:1));
        long result=date.getTimeInMillis();if(result<=due||result>MAX_TIME)throw new IllegalArgumentException("周期日期超出范围");return result;
    }

    /** forSave=false preserves dormant valid rules even if an account is archived. */
    public static void validatePlanPayload(ReferenceStore store,JSONObject payload,boolean forSave){synchronized(store){
        if(payload==null)throw new IllegalArgumentException("周期内容缺失");JSONObject book=store.book(payload.optLong("book_id"));
        if(book==null||forSave&&book.optInt("archived")!=0)throw new IllegalArgumentException("周期账本不可用");Money.scale(book.optString("currency"));
        AccountingTargets.requireResolved(store,payload,null);positive(payload,"amount");String kind=payload.optString("kind");if(!oneOf(kind,"expense","income","transfer"))throw new IllegalArgumentException("周期账单方向无效");
        if(forSave&&!kind.equals("transfer")){JSONObject category=store.categoryRepository().byRowId(payload.optLong("category_id"));if(category==null||!kind.equals(category.optString("kind"))||category.optInt("archived")!=0)throw new IllegalArgumentException("请选择周期账单的有效分类");}
        long from=payload.optLong("account_id"),to=payload.optLong("to_account_id");
        if(forSave&&from>0)validateAccount(store,from,book.optString("currency"),true);if(forSave&&to>0)validateAccount(store,to,book.optString("currency"),true);
        if(kind.equals("transfer")&&(from<=0||to<=0||from==to))throw new IllegalArgumentException("周期转账需要不同的转出和转入账户");
        long fee=integer(payload,"fee",0);if(fee< -MAX||fee>MAX||!kind.equals("transfer")&&fee!=0)throw new IllegalArgumentException("普通周期收支不支持额外手续费，请单独记账");
        JSONArray tags=array(payload,"tag_ids");for(int i=0;i<tags.length();i++)if(forSave&&store.one("SELECT id FROM tags WHERE id=?",""+longId(tags,i))==null)throw new IllegalArgumentException("周期标签已删除");
        Object raw=payload.opt("attachments");JSONArray files=raw==null||raw==JSONObject.NULL?new JSONArray():raw instanceof String?Json.array((String)raw):raw instanceof JSONArray?(JSONArray)raw:null;
        if(files==null||files.length()>100)throw new IllegalArgumentException("周期附件列表无效");
        for(int i=0;i<files.length();i++){JSONObject file=files.optJSONObject(i);if(file==null||!file.optString("file").matches("[A-Za-z0-9_-]{20,60}\\.(jpg|png|webp|pdf|txt|csv)"))throw new IllegalArgumentException("周期附件路径无效");}
        Json.put(payload,"attachments",files.toString());Json.put(payload,"currency",book.optString("currency"));Json.put(payload,"tag_ids",tags);
    }}
    private static void validateAccount(ReferenceStore store,long id,String currency,boolean active){JSONObject row=store.assetRepository().byRowId(id);if(row==null||!displayCurrency(row).equals(currency)||active&&row.optInt("archived")!=0||row.optInt("asset_type")==9||active&&AssetRepository.ORIGINAL.equals(row.optString("asset_origin"))&&!row.isNull("asset_currency")&&!row.optString("asset_currency").isEmpty())throw new IllegalArgumentException("周期账户不可用或币种不同");}
    private static void validateSelection(ReferenceStore store,String currency,JSONArray books,JSONArray accounts){if(books.length()+accounts.length()>900)throw new IllegalArgumentException("单目标所选账本与账户合计最多900个；留空可跟踪全部同币种数据");for(int i=0;i<books.length();i++){JSONObject book=store.book(longId(books,i));if(book==null||!currency.equals(book.optString("currency")))throw new IllegalArgumentException("目标账本币种不一致");}for(int i=0;i<accounts.length();i++){JSONObject account=store.assetRepository().byRowId(longId(accounts,i));if(account==null||!currency.equals(displayCurrency(account)))throw new IllegalArgumentException("目标账户币种不一致");}}
    public static boolean usesBook(ReferenceStore store,long id){synchronized(store){for(JSONObject row:store.query("SELECT book_ids FROM local_goal_config"))if(contains(ids(row.optString("book_ids")),id))return true;return false;}}
    public static void validateRestored(ReferenceStore store){synchronized(store){LocalGoalPlans service=new LocalGoalPlans(store);
        for(JSONObject row:store.query("SELECT * FROM goals")){if(integer(row,"target",0)<=0||integer(row,"saved",0)< -MAX||row.optLong("saved")>MAX)throw new IllegalArgumentException("目标金额无效");JSONObject config=service.goalConfig(row.optLong("id"));Money.scale(config.optString("currency"));if(config.optString("mode").equals("manual")&&row.optLong("saved")<0)throw new IllegalArgumentException("手动目标进度不能小于零");}
        for(JSONObject config:store.query("SELECT * FROM local_goal_config")){if(store.one("SELECT id FROM goals WHERE id=?",""+config.optLong("goal_id"))==null)throw new IllegalArgumentException("目标配置关联无效");timestamp(config,"start_time",0);timestamp(config,"end_time",0);if(config.optLong("end_time")!=0&&config.optLong("end_time")<config.optLong("start_time"))throw new IllegalArgumentException("目标日期范围无效");validateSelection(store,config.optString("currency"),ids(config.optString("book_ids")),ids(config.optString("account_ids")));}
        for(JSONObject entry:store.query("SELECT * FROM local_goal_entries")){uuid(entry.optString("operation_id"));if(store.one("SELECT id FROM goals WHERE id=?",""+entry.optLong("goal_id"))==null||entry.optLong("time")<=0||entry.optLong("time")>MAX_TIME||entry.optLong("balance")<0||entry.optLong("delta")< -MAX||entry.optLong("delta")>MAX)throw new IllegalArgumentException("目标历史关联或金额无效");}
        for(JSONObject rule:store.query("SELECT * FROM plans")){if(!oneOf(rule.optString("interval"),"daily","weekly","monthly","yearly")||rule.optLong("next_due")<=0||rule.optLong("next_due")>MAX_TIME)throw new IllegalArgumentException("周期规则无效");validatePlanPayload(store,Json.parse(rule.optString("payload")),false);}
        for(JSONObject config:store.query("SELECT * FROM local_plan_config")){if(store.one("SELECT id FROM plans WHERE id=?",""+config.optLong("plan_id"))==null||config.optLong("until_time")>MAX_TIME||config.optLong("last_generated")>MAX_TIME)throw new IllegalArgumentException("周期配置关联无效");}
    }}

    private void upsert(String table,String key,long id,ContentValues values){if(store.one("SELECT "+key+" FROM "+table+" WHERE "+key+"=?",""+id)==null){if(store.getWritableDatabase().insertOrThrow(table,null,values)<0)throw new IllegalStateException("配置未能保存");}else AccountingCommitScope.requireChanged(store,store.getWritableDatabase().update(table,values,key+"=?",new String[]{""+id}));}
    private static String displayCurrency(JSONObject row){return AssetRepository.ORIGINAL.equals(row.optString("asset_origin"))&&row.optString("currency").isEmpty()?"CNY":row.optString("currency");}
    private static String name(String raw){String value=raw==null?"":raw.trim();if(value.isEmpty()||value.length()>100)throw new IllegalArgumentException("名称需要1至100个字符");return value;}
    private static String text(String value,int max){if(value==null)return "";if(value.length()>max)throw new IllegalArgumentException("备注过长");return value;}
    private static long integer(JSONObject value,String key,long fallback){Object raw=value.opt(key);if(raw==null||raw==JSONObject.NULL)return fallback;if(!(raw instanceof Integer||raw instanceof Long))throw new IllegalArgumentException("数值必须是整数："+key);return ((Number)raw).longValue();}
    private static long positive(JSONObject value,String key){long result=integer(value,key,0);if(result<=0||result>MAX)throw new IllegalArgumentException("金额必须大于零且不超出范围");return result;}
    private static int range(JSONObject value,String key,int fallback,int min,int max){long number=integer(value,key,fallback);if(number<min||number>max)throw new IllegalArgumentException("数值超出范围："+key);return (int)number;}
    private static long timestamp(JSONObject value,String key,long fallback){long number=integer(value,key,fallback);if(number<0||number>MAX_TIME)throw new IllegalArgumentException("日期无效");return number;}
    private static boolean oneOf(String value,String... allowed){for(String item:allowed)if(item.equals(value))return true;return false;}
    private static boolean flag(JSONObject value,String key,boolean fallback){Object raw=value.opt(key);return raw==null||raw==JSONObject.NULL?fallback:Boolean.TRUE.equals(raw)||raw instanceof Number&&((Number)raw).longValue()==1||"true".equals(raw);}
    private static JSONArray array(JSONObject value,String key){Object raw=value.opt(key);if(raw==null||raw==JSONObject.NULL)return new JSONArray();if(!(raw instanceof JSONArray))throw new IllegalArgumentException("请选择有效的范围："+key);JSONArray result=new JSONArray();Set<Long> seen=new HashSet<>();JSONArray input=(JSONArray)raw;if(input.length()>1000)throw new IllegalArgumentException("选择范围过大");for(int i=0;i<input.length();i++){long id=longId(input,i);if(seen.add(id))result.put(id);}return result;}
    private static JSONArray ids(String raw){JSONArray result=Json.array(raw);return array(Json.object("ids",result),"ids");}
    private static long longId(JSONArray values,int index){Object value=values.opt(index);if(!(value instanceof Integer||value instanceof Long)||((Number)value).longValue()<=0)throw new IllegalArgumentException("范围标识无效");return ((Number)value).longValue();}
    private static boolean contains(JSONArray values,long id){for(int i=0;i<values.length();i++)if(values.optLong(i)==id)return true;return false;}
    private static void in(StringBuilder sql,List<String> args,String column,JSONArray values){if(values.length()==0)return;sql.append(" AND ").append(column).append(" IN(");for(int i=0;i<values.length();i++){if(i>0)sql.append(',');sql.append('?');args.add(""+values.optLong(i));}sql.append(')');}
    private static void copy(JSONObject from,JSONObject to,String... fields){for(String key:fields)Json.put(to,key,from.opt(key));}
    private static String uuid(String value){try{if(value==null||!UUID.fromString(value).toString().equals(value))throw new IllegalArgumentException();return value;}catch(IllegalArgumentException error){throw new IllegalArgumentException("操作标识无效",error);}}
}
