package com.wangc.bill.reference;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Bill-linked credit schedules. Saving a schedule never debits the consumption again. */
public final class AccountingInstalments {
    private static final int OWNER=ReferenceStore.LOCAL_OFFLINE_OWNER,MAX_PERIODS=120;
    private final ReferenceStore store;
    public AccountingInstalments(ReferenceStore store){if(store==null)throw new IllegalArgumentException("数据库不能为空");this.store=store;}

    public boolean eligible(long billRow){synchronized(store){try{load(billRow);return true;}catch(IllegalArgumentException unavailable){return false;}}}
    public List<JSONObject> consumptionBills(long accountRow){
        synchronized(store){List<JSONObject> values=new ArrayList<>();
            for(JSONObject row:store.query("SELECT t.*,c.name category FROM transactions t LEFT JOIN categories c ON c.id=t.category_id WHERE t.account_id=? AND t.deleted=0 AND t.kind='expense' AND t.bill_origin='original' AND t.currency='CNY' AND t.amount>0 ORDER BY t.time DESC,t.id DESC LIMIT 300",""+accountRow))
                if(eligible(row.optLong("id")))values.add(row);
            return values;
        }
    }
    /** Settings use minor CNY units; original metadata/identities stay in their existing table. */
    public JSONObject current(long billRow){
        synchronized(store){BillContext context=load(billRow);OriginalInstalmentRecord row=existing(context.bill.billId);
            long anchor=row==null?(context.bill.inAssetTime==0?context.bill.time:context.bill.inAssetTime):row.inAssetTime;
            OriginalDayInfo first=statement(context,anchor);
            int periods=row==null?12:row.periods;long fee=row==null?0:minor(row.serviceNumber);
            boolean each=row!=null&&row.serviceType==1;
            return Json.object("exists",row!=null,"bill_id",billRow,"amount",row==null?minor(Math.abs(context.bill.cost)):minor(row.totalNumber),"periods",periods,
                    "first_month",OriginalInstalmentSchedule.format(first.getMaxDay(),"yyyy-MM"),"fee_mode",each?"each":"once",
                    "fee",each&&periods>0?fee/periods:fee,"total_fee",fee,
                    "custom",row!=null&&(row.selfInfo!=null&&!row.selfInfo.isEmpty()&&!row.selfInfo.equals("[]")||row.serviceType!=1&&row.serviceType!=2||each&&periods>0&&fee%periods!=0),
                    "maximum",minor(Math.abs(context.bill.cost)),"account_id",context.asset.optLong("id"),"account_name",context.asset.optString("name"));
        }
    }
    public JSONObject preview(long billRow,JSONObject input){synchronized(store){BillContext context=load(billRow);return describe(context,build(context,input));}}
    public JSONObject save(long billRow,JSONObject input){
        JSONObject snapshot=Json.copy(input);
        return AccountingCommitScope.run(store,()->{
            BillContext context=load(billRow);OriginalInstalmentRecord before=existing(context.bill.billId),row=build(context,snapshot);
            JSONObject result=describe(context,row); // Validate the exact projected amounts before persistence.
            OriginalFinancialEffects effects=new OriginalFinancialEffects(store,OWNER,context.book.optInt("owner_semantic_id"));
            OriginalInstalmentCommands commands=new OriginalInstalmentCommands(store,OWNER,context.book.optInt("owner_semantic_id"),effects::creditRefresh);
            if(before!=null){row.localRowId=before.localRowId;row.instalmentId=before.instalmentId;}
            OriginalInstalmentRepository.SaveResult saved=before==null?commands.saveAssigned(row):commands.saveUpdated(row);
            if(!saved.saved)throw new IllegalStateException("分期未能完整保存，请重试");
            if(context.bill.assetId==0)effects.creditRefresh(0); // Original IDs can include zero; retain the real current-store consumer.
            Json.put(result,"instalment_row_id",row.localRowId);return result;
        });
    }
    public void remove(long billRow){
        AccountingCommitScope.run(store,()->{
            BillContext context=load(billRow);OriginalInstalmentRecord row=existing(context.bill.billId);if(row==null)return null;
            OriginalFinancialEffects effects=new OriginalFinancialEffects(store,OWNER,context.book.optInt("owner_semantic_id"));
            int removed=new OriginalInstalmentCommands(store,OWNER,context.book.optInt("owner_semantic_id"),effects::creditRefresh).delete(row);
            AccountingCommitScope.requireChanged(store,removed);if(context.bill.assetId==0)effects.creditRefresh(0);return null;
        });
    }
    /** Ordinary editing must not leave a larger or differently funded repayment schedule. */
    public static void validateBillEdit(ReferenceStore store,long billRow,JSONObject input){
        synchronized(store){
            OriginalBillRecord bill=new OriginalBillRepository(store).byLocalRowId(billRow);
            if(bill==null||bill.billId<=0||bill.userId!=OWNER)return;
            AccountingInstalments service=new AccountingInstalments(store);OriginalInstalmentRecord row=service.existing(bill.billId);if(row==null)return;
            JSONObject host=store.transaction(billRow);String kind=input.optString("kind",host.optString("kind"));
            long account=input.optLong("account_id",host.optLong("account_id"));JSONObject selected=store.assetRepository().byRowId(account);
            if(!kind.equals("expense")||selected==null||!AssetRepository.ORIGINAL.equals(selected.optString("asset_origin"))
                    ||selected.optLong("owner_semantic_id",Long.MIN_VALUE)!=OWNER||selected.optLong("semantic_id",Long.MIN_VALUE)!=bill.assetId||selected.optInt("asset_type")!=2)
                throw new IllegalArgumentException("此消费已有分期，请先管理或移除分期，再更换账户或收支方向");
            long amount=integer(input,"amount");
            if(amount<minor(row.totalNumber))throw new IllegalArgumentException("消费金额不能小于已设置的分期本金，请先调整或移除分期");
        }
    }
    /** Reimbursement changes the funding model and cannot retain a consumption schedule. */
    public static void requireNoSchedule(ReferenceStore store,long billRow){
        synchronized(store){OriginalBillRecord bill=new OriginalBillRepository(store).byLocalRowId(billRow);
            if(bill!=null&&bill.billId>0&&bill.userId==OWNER&&new OriginalInstalmentRepository(store).firstByBill(OWNER,bill.billId)!=null)
                throw new IllegalArgumentException("此消费已有分期，请先移除分期，再标记待报销");
        }
    }
    private BillContext load(long id){
        JSONObject host=store.transaction(id);OriginalBillRecord bill=new OriginalBillRepository(store).byLocalRowId(id);
        if(host==null||bill==null||host.optInt("deleted")!=0||!"expense".equals(host.optString("kind"))||!"CNY".equals(host.optString("currency"))
                ||bill.userId!=OWNER||bill.parentCategoryId==9||bill.billId<=0||!Double.isFinite(bill.cost)||bill.cost<=0
                ||bill.reimbursement||bill.currencyInfo!=null&&!bill.currencyInfo.isEmpty())
            throw new IllegalArgumentException("请选择本币信用账户中有效的普通消费账单；报销、外币或旧编号账单请先保留原记录");
        JSONObject asset=store.assetRepository().p(bill.assetId,OWNER),book=store.book(host.optLong("book_id"));
        if(asset==null||asset.optInt("archived")!=0||asset.optInt("asset_type")!=2||(!asset.isNull("asset_currency")&&!asset.optString("asset_currency").isEmpty())
                ||book==null||book.optInt("archived")!=0||book.isNull("owner_semantic_id")||book.optInt("owner_semantic_id")!=OWNER)
            throw new IllegalArgumentException("消费账单必须使用当前离线身份的本币信用账户和账本");
        if(AccountingLinks.ownerOfBill(store,id)!=null)throw new IllegalArgumentException("手续费或利息请从其原金融记录处理，不能再次分期");
        BillContext result=new BillContext();result.host=host;result.bill=bill;result.asset=asset;result.book=book;return result;
    }
    private OriginalInstalmentRecord existing(int semantic){
        List<JSONObject> rows=store.query("SELECT id FROM original_instalments WHERE owner_semantic_id=? AND parent_bill_semantic_id=?",""+OWNER,""+semantic);
        if(rows.size()>1)throw new IllegalArgumentException("此消费关联多条旧分期，请先整理原记录，避免覆盖错误计划");
        return rows.isEmpty()?null:new OriginalInstalmentRepository(store).byLocalRowId(rows.get(0).optLong("id"));
    }
    private OriginalInstalmentRecord build(BillContext context,JSONObject input){
        long amount=integer(input,"amount"),fee=integer(input,"fee"),periodCount=integer(input,"periods");
        if(amount<=0||amount>minor(Math.abs(context.bill.cost)))throw new IllegalArgumentException("分期本金必须大于零且不超过当前消费金额");
        if(periodCount<1||periodCount>MAX_PERIODS||amount<periodCount)throw new IllegalArgumentException("期数需在1至120之间，且每期本金至少一分");
        if(fee<0)throw new IllegalArgumentException("手续费不能小于零");
        String mode=input.optString("fee_mode");if(!mode.equals("each")&&!mode.equals("once"))throw new IllegalArgumentException("请选择每期或一次性手续费");
        long totalFee=mode.equals("each")?Math.multiplyExact(fee,periodCount):fee;
        // The recovered split() narrows cents to int. Reject values outside that
        // algorithm's exact working range instead of showing an overflowed plan.
        if(amount>Integer.MAX_VALUE-totalFee)throw new IllegalArgumentException("分期本金和手续费合计过大，请减少金额");
        OriginalInstalmentRecord row=new OriginalInstalmentRecord();row.userId=OWNER;row.billId=context.bill.billId;
        // A host linkage sentinel keeps Bill schedules out of g1.w's standalone
        // asset traversal, including the valid original credit-asset semantic ID 0.
        row.assetId=-1;row.accountMonth="";row.instalmentType=0;row.selfInfo="";row.originalDeleted=false;
        row.totalNumber=BigDecimal.valueOf(amount,2).doubleValue();row.periods=(int)periodCount;
        row.serviceNumber=BigDecimal.valueOf(totalFee,2).doubleValue();row.serviceType=mode.equals("each")?1:2;row.remainderType=1;
        row.inAssetTime=anchor(context,input.optString("first_month"));
        return row;
    }
    private JSONObject describe(BillContext context,OriginalInstalmentRecord row){
        JSONArray periods=new JSONArray();long total=0,fees=0;
        for(OriginalInstalmentSchedule.Period part:OriginalInstalmentSchedule.periods(row)){
            long due=minor(part.number),fee=minor(part.serviceCost);if(due<fee||due<0||fee<0)throw new IllegalArgumentException("分期计划金额无效，请调整期数或手续费");
            long at=OriginalInstalmentSchedule.parse(part.date,"yyyy年MM月dd日");OriginalDayInfo cycle=statement(context,at);
            String repayment=OriginalInstalmentSchedule.repaymentDay(nullable(context.asset,"in_account_date"),cycle,true);
            periods.put(Json.object("period",part.periods,"month",OriginalInstalmentSchedule.format(cycle.getMaxDay(),"yyyy-MM"),"repayment_date",repayment,"amount",due,"principal",due-fee,"fee",fee));
            total=Math.addExact(total,due);fees=Math.addExact(fees,fee);
        }
        long principal=minor(row.totalNumber),expectedFee=minor(row.serviceNumber);
        if(total!=Math.addExact(principal,expectedFee)||fees!=expectedFee)throw new IllegalArgumentException("分期取整无法与本金、手续费总额相符，请调整设置");
        return Json.object("amount",principal,"total_fee",fees,"total",total,"periods",periods,"account_id",context.asset.optLong("id"),"account_name",context.asset.optString("name"));
    }
    private long anchor(BillContext context,String month){
        if(month==null||!month.matches("[0-9]{4}-[0-9]{2}"))throw new IllegalArgumentException("首次账期格式为年-月，例如2026-10");
        int year=Integer.parseInt(month.substring(0,4)),index=Integer.parseInt(month.substring(5));if(year<1970||year>2200||index<1||index>12)throw new IllegalArgumentException("首次账期无效");
        int day=statementDay(context);Calendar calendar=Calendar.getInstance();calendar.clear();calendar.set(year,index-1,1,12,0,0);
        calendar.set(Calendar.DAY_OF_MONTH,day<=1?calendar.getActualMaximum(Calendar.DAY_OF_MONTH):Math.min(day-1,calendar.getActualMaximum(Calendar.DAY_OF_MONTH)));
        long at=calendar.getTimeInMillis();String actual=OriginalInstalmentSchedule.format(statement(context,at).getMaxDay(),"yyyy-MM");
        if(!month.equals(actual))throw new IllegalArgumentException("账户账单日无法对应此首次账期，请先修改信用账户日期");
        long original=context.bill.inAssetTime==0?context.bill.time:context.bill.inAssetTime;
        String earliest=OriginalInstalmentSchedule.format(statement(context,original).getMaxDay(),"yyyy-MM");
        if(month.compareTo(earliest)<0)throw new IllegalArgumentException("首次账期不能早于原消费账期（"+earliest+"）");
        return at;
    }
    private int statementDay(BillContext context){int day=OriginalInstalmentSchedule.statementDay(nullable(context.asset,"out_account_date"),context.asset.optInt("out_day_not_next")!=0);return day==0?store.context().getSharedPreferences("reference_settings",android.content.Context.MODE_PRIVATE).getInt("month_start",0):day;}
    private OriginalDayInfo statement(BillContext context,long at){return OriginalInstalmentSchedule.statementPeriod(statementDay(context),at,0);}
    private static String nullable(JSONObject value,String key){return value.isNull(key)?null:value.optString(key);}
    private static long integer(JSONObject value,String key){Object number=value.opt(key);if(!(number instanceof Integer||number instanceof Long))throw new IllegalArgumentException("分期数值无效："+key);return ((Number)number).longValue();}
    private static long minor(double value){if(!Double.isFinite(value))throw new IllegalArgumentException("分期金额无效");try{return BigDecimal.valueOf(value).movePointRight(2).setScale(0,RoundingMode.HALF_UP).longValueExact();}catch(ArithmeticException invalid){throw new IllegalArgumentException("分期金额过大");}}
    private static final class BillContext{JSONObject host,asset,book;OriginalBillRecord bill;}
}
