package com.wangc.bill.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;

/**
 * Offline holding diary. It never changes cash accounts, ordinary Bills or spending statistics.
 * Amounts are cents; quantity and quoted CNY/unit prices have six decimal places.
 * Cost allocation follows BeeCount's moving-average, integer proportional allocation.
 * This is a local host model, not a claim about the original Stock* database algorithm.
 */
public final class LocalInvestments {
    public static final long MAX_MONEY = 99_999_999_999_999L;
    public static final long MAX_FIXED = 1_000_000_000_000_000L;
    private static final long MAX_TIME = 8_640_000_000_000_000L;
    private final ReferenceStore store;
    public LocalInvestments(ReferenceStore store) {
        if (store == null) throw new IllegalArgumentException("数据库不能为空");
        this.store = store;
    }
    public static String[] tables() {
        return new String[]{"local_investments", "local_investment_events", "local_investment_operations"};
    }
    public static void installV11Schema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS local_investments(id INTEGER PRIMARY KEY AUTOINCREMENT,book_id INTEGER NOT NULL REFERENCES books(id),name TEXT NOT NULL,code TEXT NOT NULL DEFAULT '',instrument_type TEXT NOT NULL CHECK(instrument_type IN ('stock','fund','custom')),currency TEXT NOT NULL DEFAULT 'CNY' CHECK(currency='CNY'),remark TEXT NOT NULL DEFAULT '',archived INTEGER NOT NULL DEFAULT 0 CHECK(archived IN (0,1)),created INTEGER NOT NULL,updated INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS local_investments_book ON local_investments(book_id,archived,id)");
        db.execSQL("CREATE TABLE IF NOT EXISTS local_investment_events(id INTEGER PRIMARY KEY AUTOINCREMENT,investment_id INTEGER NOT NULL REFERENCES local_investments(id),action TEXT NOT NULL CHECK(action IN ('buy','sell','dividend','price')),quantity_micros INTEGER NOT NULL DEFAULT 0,amount_minor INTEGER NOT NULL DEFAULT 0,fee_minor INTEGER NOT NULL DEFAULT 0,price_micros INTEGER NOT NULL DEFAULT 0,happened_at INTEGER NOT NULL,remark TEXT NOT NULL DEFAULT '',deleted INTEGER NOT NULL DEFAULT 0 CHECK(deleted IN (0,1)),created INTEGER NOT NULL,updated INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS local_investment_event_order ON local_investment_events(investment_id,happened_at,id)");
        db.execSQL("CREATE TABLE IF NOT EXISTS local_investment_operations(operation_id TEXT PRIMARY KEY,action TEXT NOT NULL CHECK(action IN ('instrument_create','instrument_edit','event_create','event_edit','event_delete','event_restore','archive','unarchive')),investment_id INTEGER NOT NULL REFERENCES local_investments(id),event_id INTEGER REFERENCES local_investment_events(id),payload TEXT NOT NULL,created INTEGER NOT NULL)");
    }
    public static void migrate(SQLiteDatabase db, int oldVersion) {
        if (oldVersion < 11) installV11Schema(db);
    }

    public JSONObject saveInstrument(long id, JSONObject input, String operationId) {
        JSONObject snapshot = Json.copy(input);
        String uuid = uuid(operationId), action = id == 0 ? "instrument_create" : "instrument_edit";
        nonnegativeId(id);
        return AccountingCommitScope.run(store, () -> {
            JSONObject prior = operation(uuid, action, id);
            if (prior != null) return position(prior.optLong("investment_id"));
            JSONObject old = id == 0 ? null : requiredInstrument(id);
            long bookId = integer(snapshot, "book_id");
            requireBook(bookId, false);
            String name = text(snapshot, "name", 100, false).trim();
            if (name.isEmpty()) throw new IllegalArgumentException("请输入标的名称");
            String code = optionalText(snapshot, "code", 40);
            String type = text(snapshot, "instrument_type", 10, false);
            if (!(type.equals("stock") || type.equals("fund") || type.equals("custom")))
                throw new IllegalArgumentException("请选择股票、基金或自定义标的");
            if (old != null && old.optInt("archived") != 0)
                throw new IllegalArgumentException("请先恢复已归档的标的");
            long now = System.currentTimeMillis();
            ContentValues values = new ContentValues();
            values.put("book_id", bookId); values.put("name", name); values.put("code", code);
            values.put("instrument_type", type); values.put("currency", "CNY");
            values.put("remark", optionalText(snapshot, "remark", 2000)); values.put("updated", now);
            long rowId = id;
            if (id == 0) {
                values.put("created", now); values.put("archived", 0);
                rowId = insert("local_investments", values);
            } else changed(store.getWritableDatabase().update("local_investments", values, "id=?", args(id)));
            validateTotals(bookId,old==null?0:old.optLong("book_id"));
            record(uuid, action, id, rowId, null, now);
            return position(rowId);
        });
    }

    public JSONObject saveEvent(long id, JSONObject input, String operationId) {
        JSONObject snapshot = Json.copy(input);
        String uuid = uuid(operationId), action = id == 0 ? "event_create" : "event_edit";
        nonnegativeId(id);
        return AccountingCommitScope.run(store, () -> {
            JSONObject prior = operation(uuid, action, id);
            if (prior != null) return result(prior.optLong("event_id"));
            long investmentId = integer(snapshot, "investment_id");
            requireEditable(investmentId);
            if (id != 0 && requiredEvent(id).optLong("investment_id") != investmentId)
                throw new IllegalArgumentException("不能将交易移动到另一个标的");
            if (id != 0 && requiredEvent(id).optInt("deleted") != 0)
                throw new IllegalArgumentException("请先恢复已删除的交易");
            validateEvent(snapshot, false);
            long now = System.currentTimeMillis();
            ContentValues values = new ContentValues();
            values.put("investment_id", investmentId); values.put("action", snapshot.optString("action"));
            for (String field : new String[]{"quantity_micros", "amount_minor", "fee_minor", "price_micros", "happened_at"})
                values.put(field, integer(snapshot, field));
            values.put("remark", optionalText(snapshot, "remark", 2000)); values.put("updated", now);
            long rowId = id;
            if (id == 0) {
                values.put("created", now); values.put("deleted", 0);
                rowId = insert("local_investment_events", values);
            } else changed(store.getWritableDatabase().update("local_investment_events", values, "id=?", args(id)));
            // Replaying the whole ordered history also guards backdated edits and deleted purchases.
            position(investmentId);
            validateTotals(requiredInstrument(investmentId).optLong("book_id"),0);
            touch(investmentId, now);
            record(uuid, action, id, investmentId, rowId, now);
            return result(rowId);
        });
    }

    public JSONObject trashEvent(long id, boolean deleted, String operationId) {
        if (id <= 0) throw new IllegalArgumentException("交易不存在");
        String uuid = uuid(operationId), action = deleted ? "event_delete" : "event_restore";
        return AccountingCommitScope.run(store, () -> {
            JSONObject prior = operation(uuid, action, id);
            if (prior != null) return result(prior.optLong("event_id"));
            JSONObject event = requiredEvent(id);
            long investmentId = event.optLong("investment_id");
            requireEditable(investmentId);
            long now = System.currentTimeMillis();
            ContentValues values = new ContentValues(); values.put("deleted", deleted ? 1 : 0); values.put("updated", now);
            changed(store.getWritableDatabase().update("local_investment_events", values, "id=?", args(id)));
            position(investmentId);
            validateTotals(requiredInstrument(investmentId).optLong("book_id"),0);
            touch(investmentId, now);
            record(uuid, action, id, investmentId, id, now);
            return result(id);
        });
    }

    public JSONObject setArchived(long id, boolean archived, String operationId) {
        if (id <= 0) throw new IllegalArgumentException("标的不存在");
        String uuid = uuid(operationId), action = archived ? "archive" : "unarchive";
        return AccountingCommitScope.run(store, () -> {
            JSONObject prior = operation(uuid, action, id);
            if (prior != null) return position(prior.optLong("investment_id"));
            JSONObject state = position(id);
            if (archived && state.optLong("quantity_micros") != 0)
                throw new IllegalArgumentException("仍有持仓，请先卖出后再归档");
            long now = System.currentTimeMillis();
            ContentValues values = new ContentValues(); values.put("archived", archived ? 1 : 0); values.put("updated", now);
            changed(store.getWritableDatabase().update("local_investments", values, "id=?", args(id)));
            validateTotals(state.optLong("book_id"),0);
            record(uuid, action, id, id, null, now);
            return position(id);
        });
    }

    public JSONObject position(long id) {
        synchronized (store) { return replay(requiredInstrument(id), events(id, false)); }
    }
    public List<JSONObject> positions(long bookId, boolean includeArchived) {
        synchronized (store) {
            if (bookId < 0) throw new IllegalArgumentException("账本无效");
            List<JSONObject> result = new ArrayList<>();
            String sql = "SELECT * FROM local_investments WHERE 1=1";
            if (bookId != 0) sql += " AND book_id=" + bookId;
            if (!includeArchived) sql += " AND archived=0";
            for (JSONObject row : store.query(sql + " ORDER BY archived,updated DESC,id DESC"))
                result.add(replay(row, events(row.optLong("id"), false)));
            return result;
        }
    }
    public List<JSONObject> events(long investmentId, boolean includeDeleted) {
        synchronized (store) {
            requiredInstrument(investmentId);
            return store.query("SELECT * FROM local_investment_events WHERE investment_id=?"
                    + (includeDeleted ? "" : " AND deleted=0") + " ORDER BY happened_at,id", "" + investmentId);
        }
    }
    public JSONObject portfolio(long bookId) {
        synchronized (store) {
            long value=0, cost=0, realized=0, unrealized=0, dividends=0, fees=0; int open=0, unpriced=0;
            List<JSONObject> all=positions(bookId,true);
            for (JSONObject row : all) {
                value=add(value,row.optLong("market_value_minor")); cost=add(cost,row.optLong("cost_minor"));
                realized=add(realized,row.optLong("realized_minor")); unrealized=add(unrealized,row.optLong("unrealized_minor"));
                dividends=add(dividends,row.optLong("dividends_minor")); fees=add(fees,row.optLong("fees_minor"));
                if(row.optLong("quantity_micros")>0){open++;if(!row.optBoolean("has_price"))unpriced++;}
            }
            return Json.object("book_id",bookId,"currency","CNY","market_value_minor",value,"cost_minor",cost,
                    "realized_minor",realized,"unrealized_minor",unrealized,"total_profit_minor",add(realized,unrealized),
                    "dividends_minor",dividends,"fees_minor",fees,"instrument_count",all.size(),"open_count",open,
                    "unpriced_count",unpriced,"journal_only",true);
        }
    }

    private static JSONObject replay(JSONObject instrument, List<JSONObject> events) {
        long quantity=0,cost=0,realized=0,dividends=0,fees=0,price=0,priceTime=0; boolean priced=false;
        for(JSONObject event:events){
            validateEvent(event,true);
            long units=event.optLong("quantity_micros"),amount=event.optLong("amount_minor"),fee=event.optLong("fee_minor");
            switch(event.optString("action")){
                case "buy" -> {
                    quantity=fixedAdd(quantity,units); cost=add(cost,add(amount,fee));
                }
                case "sell" -> {
                    if(units>quantity)throw new IllegalArgumentException("交易顺序会造成超卖，请先修正买入或卖出记录");
                    long removed=units==quantity?cost:proportion(cost,units,quantity);
                    cost=add(cost,-removed); quantity-=units;
                    realized=add(realized,add(add(amount,-fee),-removed));
                }
                case "dividend" -> {
                    long net=add(amount,-fee); dividends=add(dividends,net); realized=add(realized,net);
                }
                case "price" -> { priced=true;price=event.optLong("price_micros");priceTime=event.optLong("happened_at"); }
                default -> throw new IllegalArgumentException("投资交易类型无效");
            }
            fees=add(fees,fee);
        }
        long value=quantity==0?0:priced?rounded(BigInteger.valueOf(quantity).multiply(BigInteger.valueOf(price)),BigInteger.TEN.pow(10)):cost;
        long unrealized=add(value,-cost);
        JSONObject result=Json.copy(instrument);
        Json.put(result,"quantity_micros",quantity);Json.put(result,"quantity_text",unitsText(quantity));
        Json.put(result,"cost_minor",cost);Json.put(result,"market_value_minor",value);
        Json.put(result,"realized_minor",realized);Json.put(result,"unrealized_minor",unrealized);
        Json.put(result,"total_profit_minor",add(realized,unrealized));Json.put(result,"dividends_minor",dividends);
        Json.put(result,"fees_minor",fees);Json.put(result,"has_price",priced);Json.put(result,"price_micros",price);
        Json.put(result,"price_text",fixedText(price));Json.put(result,"price_time",priceTime);
        Json.put(result,"average_cost_text",quantity==0?"0":BigDecimal.valueOf(cost,2).divide(BigDecimal.valueOf(quantity,6),6,RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
        Json.put(result,"journal_only",true);return result;
    }
    private static long proportion(long value,long part,long whole){
        return rounded(BigInteger.valueOf(value).multiply(BigInteger.valueOf(part)),BigInteger.valueOf(whole));
    }
    private static long rounded(BigInteger numerator,BigInteger denominator){
        try { long result=numerator.add(denominator.divide(BigInteger.valueOf(2))).divide(denominator).longValueExact();moneyRange(result);return result; }
        catch(ArithmeticException overflow){throw new IllegalArgumentException("投资金额超出可保存范围",overflow);}
    }
    private static long add(long left,long right){
        try{long value=Math.addExact(left,right);moneyRange(value);return value;}
        catch(ArithmeticException overflow){throw new IllegalArgumentException("投资金额过大",overflow);}
    }
    private static long fixedAdd(long left,long right){
        try{long value=Math.addExact(left,right);if(value<0||value>MAX_FIXED)throw new ArithmeticException();return value;}
        catch(ArithmeticException overflow){throw new IllegalArgumentException("持仓份额过大",overflow);}
    }
    private static void moneyRange(long value){if(value < -MAX_MONEY || value > MAX_MONEY)throw new IllegalArgumentException("投资金额超出可保存范围");}
    public static long units(String raw){return parseFixed(raw,"份额");}
    public static long price(String raw){return parseFixed(raw,"单价");}
    public static String unitsText(long value){return fixedText(value);}
    public static String priceText(long value){return fixedText(value);}
    private static String fixedText(long value){return BigDecimal.valueOf(value,6).stripTrailingZeros().toPlainString();}
    private static long parseFixed(String raw,String label){
        if(raw==null||!raw.trim().matches("[0-9]+(?:\\.[0-9]+)?"))throw new IllegalArgumentException("请输入有效的"+label);
        try{long value=new BigDecimal(raw.trim()).movePointRight(6).longValueExact();if(value<0||value>MAX_FIXED)throw new ArithmeticException();return value;}
        catch(ArithmeticException bad){throw new IllegalArgumentException(label+"最多支持6位小数，且不能过大");}
    }

    private JSONObject result(long eventId){JSONObject event=requiredEvent(eventId);return Json.object("event",event,"position",position(event.optLong("investment_id")));}
    private JSONObject requiredInstrument(long id){
        JSONObject row=store.one("SELECT * FROM local_investments WHERE id=?",""+id);
        if(row==null)throw new IllegalArgumentException("投资标的不存在");return row;
    }
    private JSONObject requiredEvent(long id){
        JSONObject row=store.one("SELECT * FROM local_investment_events WHERE id=?",""+id);
        if(row==null)throw new IllegalArgumentException("投资交易不存在");return row;
    }
    private void requireEditable(long id){JSONObject row=requiredInstrument(id);if(row.optInt("archived")!=0)throw new IllegalArgumentException("请先恢复已归档的标的");requireBook(row.optLong("book_id"),false);}
    private void requireBook(long id,boolean restoring){
        JSONObject book=store.book(id);
        if(book==null||!"CNY".equals(book.optString("currency"))||(!restoring&&book.optInt("archived")!=0))
            throw new IllegalArgumentException("本地投资日记需要可用的人民币账本");
    }
    private long insert(String table,ContentValues values){long id=store.getWritableDatabase().insertOrThrow(table,null,values);if(id<=0)throw new IllegalStateException("投资记录未完整保存");return id;}
    private void changed(int count){AccountingCommitScope.requireChanged(store,count);if(count!=1)throw new IllegalStateException("投资数据已变化，请刷新后重试");}
    private void touch(long id,long now){ContentValues value=new ContentValues();value.put("updated",now);changed(store.getWritableDatabase().update("local_investments",value,"id=?",args(id)));}
    /** Summary values share the displayed money bound; never commit a state the asset page cannot show. */
    private void validateTotals(long bookId,long previousBookId){
        portfolio(0);portfolio(bookId);
        if(previousBookId!=0&&previousBookId!=bookId)portfolio(previousBookId);
    }
    private static String[] args(long id){return new String[]{""+id};}
    private JSONObject operation(String uuid,String action,long requestedId){
        JSONObject previous=store.one("SELECT * FROM local_investment_operations WHERE operation_id=?",uuid);
        if(previous!=null&&(!action.equals(previous.optString("action"))||integer(Json.parse(previous.optString("payload")),"requested_id")!=requestedId))
            throw new IllegalArgumentException("此操作编号已用于另一项投资操作");return previous;
    }
    private void record(String uuid,String action,long requestedId,long investmentId,Long eventId,long now){
        ContentValues value=new ContentValues();value.put("operation_id",uuid);value.put("action",action);value.put("investment_id",investmentId);
        if(eventId==null)value.putNull("event_id");else value.put("event_id",eventId);
        value.put("payload",Json.object("requested_id",requestedId).toString());value.put("created",now);insert("local_investment_operations",value);
    }
    private static String uuid(String raw){
        try{if(raw==null||!UUID.fromString(raw).toString().equals(raw))throw new IllegalArgumentException();return raw;}
        catch(IllegalArgumentException bad){throw new IllegalArgumentException("投资操作编号无效");}
    }
    private static long integer(JSONObject value,String key){Object raw=value.opt(key);if(!(raw instanceof Integer||raw instanceof Long))throw new IllegalArgumentException("投资字段需要整数："+key);return ((Number)raw).longValue();}
    private static String text(JSONObject value,String key,int max,boolean empty){Object raw=value.opt(key);if(!(raw instanceof String))throw new IllegalArgumentException("投资文本无效："+key);String result=(String)raw;if(result.length()>max||(!empty&&result.trim().isEmpty()))throw new IllegalArgumentException("投资文本为空或过长："+key);return result;}
    private static String optionalText(JSONObject value,String key,int max){return !value.has(key)||value.isNull(key)?"":text(value,key,max,true);}
    private static void nonnegativeId(long id){if(id<0)throw new IllegalArgumentException("记录编号无效");}
    private static void timestamp(long time){if(time<=0||time>MAX_TIME)throw new IllegalArgumentException("投资记录时间无效");}
    private static void validateEvent(JSONObject value,boolean stored){
        String action=text(value,"action",12,false);long q=integer(value,"quantity_micros"),a=integer(value,"amount_minor"),f=integer(value,"fee_minor"),p=integer(value,"price_micros");
        if(q<0||q>MAX_FIXED||a<0||a>MAX_MONEY||f<0||f>MAX_MONEY||p<0||p>MAX_FIXED)throw new IllegalArgumentException("投资份额或金额超出范围");
        timestamp(integer(value,"happened_at"));optionalText(value,"remark",2000);
        switch(action){
            case "buy" -> {if(q<=0||a<=0||p!=0)throw new IllegalArgumentException("买入需要正份额和成交金额");add(a,f);}
            case "sell" -> {if(q<=0||p!=0)throw new IllegalArgumentException("卖出需要正份额");}
            case "dividend" -> {if(q!=0||a<=0||f>a||p!=0)throw new IllegalArgumentException("分红金额需要大于零，费用不能超过分红");}
            case "price" -> {if(q!=0||a!=0||f!=0)throw new IllegalArgumentException("价格记录不能包含成交份额、金额或费用");}
            default -> throw new IllegalArgumentException("投资交易类型无效");
        }
        if(stored){if(integer(value,"id")<=0||integer(value,"investment_id")<=0)throw new IllegalArgumentException("投资交易编号无效");flag(value,"deleted");timestamp(integer(value,"created"));timestamp(integer(value,"updated"));}
    }
    private static void flag(JSONObject value,String field){long n=integer(value,field);if(n!=0&&n!=1)throw new IllegalArgumentException("投资开关值无效");}

    /** Read-only checks; caller runs this before marking the whole backup restore transaction successful. */
    public static void validateRestored(ReferenceStore store){
        synchronized(store){
            LocalInvestments investments=new LocalInvestments(store);
            for(JSONObject row:store.query("SELECT * FROM local_investments")){
                long id=integer(row,"id");if(id<=0)throw new IllegalArgumentException("投资标的编号无效");
                investments.requireBook(integer(row,"book_id"),true);
                text(row,"name",100,false);text(row,"code",40,true);text(row,"remark",2000,true);
                if(!"CNY".equals(text(row,"currency",3,false)))throw new IllegalArgumentException("投资币种无效");
                String type=text(row,"instrument_type",10,false);if(!(type.equals("stock")||type.equals("fund")||type.equals("custom")))throw new IllegalArgumentException("投资标的类型无效");
                flag(row,"archived");timestamp(integer(row,"created"));timestamp(integer(row,"updated"));
                JSONObject state=investments.position(id);if(row.optInt("archived")!=0&&state.optLong("quantity_micros")!=0)throw new IllegalArgumentException("归档投资仍存在持仓");
            }
            for(JSONObject event:store.query("SELECT * FROM local_investment_events")){
                validateEvent(event,true);investments.requiredInstrument(integer(event,"investment_id"));
            }
            for(JSONObject op:store.query("SELECT * FROM local_investment_operations")){
                uuid(text(op,"operation_id",36,false));String action=text(op,"action",20,false);
                long investmentId=integer(op,"investment_id");investments.requiredInstrument(investmentId);timestamp(integer(op,"created"));
                long requested=integer(Json.parse(text(op,"payload",200,false)),"requested_id");nonnegativeId(requested);
                boolean event=action.startsWith("event_");
                if(event){
                    long eventId=integer(op,"event_id");if(investments.requiredEvent(eventId).optLong("investment_id")!=investmentId)throw new IllegalArgumentException("投资操作交易关联无效");
                    if(!(action.equals("event_create")||action.equals("event_edit")||action.equals("event_delete")||action.equals("event_restore")))throw new IllegalArgumentException("投资操作类型无效");
                    if(action.equals("event_create")?requested!=0:requested!=eventId)throw new IllegalArgumentException("投资操作请求编号无效");
                }else{
                    if(!op.isNull("event_id")||!(action.equals("instrument_create")||action.equals("instrument_edit")||action.equals("archive")||action.equals("unarchive")))throw new IllegalArgumentException("投资操作类型或关联无效");
                    if(action.equals("instrument_create")?requested!=0:requested!=investmentId)throw new IllegalArgumentException("投资操作标的关联无效");
                }
            }
            // Include archived realized returns and check bounded whole-portfolio sums as well.
            investments.portfolio(0);
            for(JSONObject book:store.query("SELECT id FROM books"))investments.portfolio(book.optLong("id"));
        }
    }
}
