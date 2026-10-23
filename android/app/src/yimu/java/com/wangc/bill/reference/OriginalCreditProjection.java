package com.wangc.bill.reference;

import com.wangc.bill.utils.h2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;

/** s2.g/l projection with explicit typed inputs; it never persists a balance delta. */
public final class OriginalCreditProjection {
    private OriginalCreditProjection(){}
    public static final class Asset {
        public long semanticId;public String inAccountDate,outAccountDate;public boolean outDayNotNext;
        /** Retained loaded object's getter, evaluated after querying source rows. */
        public DoubleSupplier balance;
    }
    public static final class Receipt {public long assetId,time;public double amount;public int type;public String remark;}
    public static final class Transfer {public long fromAssetId,toAssetId,time;public double cost;public String remark;}
    public static final class Lend {public long assetId,repaymentAssetId,time,outTime;public int type,billId;public double fromCost,interest;public boolean generalServiceBill;public String remark;}
    public static final class Stock {public long stockAssetId,doTime;public int type;public double totalCost;public String remark;}
    public static final class RefundAdjustment {public final boolean exists;public final double amount;public RefundAdjustment(boolean exists,double amount){this.exists=exists;this.amount=amount;}}
    public interface DataSource {
        List<OriginalBillRecord> bills(long assetSemanticId);
        RefundAdjustment refund(OriginalBillRecord bill);
        OriginalInstalmentRecord instalmentForBill(int billSemanticId);
        List<OriginalInstalmentRecord> instalments(long assetSemanticId);
        List<Receipt> receipts(long assetSemanticId);
        List<Transfer> transfers(long assetSemanticId);
        List<Lend> lends(long assetSemanticId);
        List<Stock> stocks(long assetSemanticId);
        List<Lend> interestRows(long assetSemanticId,long inclusiveStart,long inclusiveEnd);
        OriginalBillRecord billBySemantic(int semanticId);
        Integer assetType(long semanticId);
        String assetName(long semanticId);
        String stockName(long semanticId);
        OriginalCreditBillInfo billInfo(OriginalBillRecord bill);
        String operationIcon(int type,String fallback);
        int configuredMonthStart();
        long now();
    }
    public static final class Result {
        public final List<OriginalCreditBill> statements;
        /** null means source s2.l never wrote its service-remainder map for this account. */
        public final Double remainingServiceCost;
        private Result(List<OriginalCreditBill> rows,Double remaining){statements=rows;remainingServiceCost=remaining;}
    }
    /** s2.l exposes its actual period objects before reminder allocation mutates them. */
    public interface Publication {void periods(List<OriginalCreditBill> rows);void remainingServiceCost(double value);}
    public static Result build(Asset asset,DataSource data,boolean markCurrent){
        return build(asset,data,markCurrent,null);
    }
    public static Result build(Asset asset,DataSource data,boolean markCurrent,Publication publication){
        Map<String,OriginalCreditBill> map=new HashMap<>();int day=OriginalInstalmentSchedule.statementDay(asset.outAccountDate,asset.outDayNotNext);
        for(OriginalBillRecord original:data.bills(asset.semanticId)){
            OriginalBillRecord bill=copyBill(original);RefundAdjustment refund=data.refund(bill);
            if(refund.exists&&empty(bill.currencyInfo))bill.cost=Math.abs(bill.cost)+refund.amount;
            OriginalInstalmentRecord instalment=data.instalmentForBill(bill.billId);
            if(instalment==null){OriginalCreditBill period=period(asset,day,bill.inAssetTime==0?bill.time:bill.inAssetTime,map,data);if(bill.parentCategoryId==9)period.reduceNumber(Math.abs(bill.getCurrencyCost()));else period.addNumber(Math.abs(bill.getCurrencyCost()));period.addBill(data.billInfo(bill));}
            else{
                if(bill.parentCategoryId!=9&&instalment.totalNumber<Math.abs(bill.getCurrencyCost())){OriginalCreditBill period=period(asset,day,bill.inAssetTime==0?bill.time:bill.inAssetTime,map,data);period.addNumber(Math.abs(bill.getCurrencyCost())-instalment.totalNumber);OriginalCreditBillInfo info=data.billInfo(bill);info.setNumber(instalment.totalNumber-Math.abs(bill.getCurrencyCost()));period.addBill(info);}
                for(OriginalInstalmentSchedule.Period item:OriginalInstalmentSchedule.periods(instalment)){
                    OriginalCreditBill period=instalmentPeriod(asset,day,item,map,data);OriginalCreditBillInfo info=data.billInfo(bill);info.setDate(OriginalInstalmentSchedule.parse(item.date,"yyyy年MM月dd日"));info.setTitle("["+item.periods+"/"+instalment.periods+"]"+info.getTitle());info.setNumber(Math.abs(item.number)*-1.0d);period.addInstalment(info);
                }
            }
        }
        for(Receipt item:data.receipts(asset.semanticId)){
            OriginalCreditBill period=period(asset,day,item.time,map,data);period.reduceNumber(item.amount);OriginalCreditBillInfo info=new OriginalCreditBillInfo();info.setNumber(item.amount);info.setRemark(item.remark);info.setTitle(item.type==1?"报销到账":"退款");info.setIcon(data.operationIcon(item.type==1?8:9,item.type==1?"ic_data_baoxiao":"ic_refund"));info.setDate(item.time);
            // Original s2.j sets billId to that newly-created info's own default0.
            period.addBill(info);
        }
        for(Transfer item:data.transfers(asset.semanticId)){
            OriginalCreditBill period=period(asset,day,item.time,map,data);if(item.fromAssetId==asset.semanticId)period.addNumber(Math.abs(item.cost));else period.reduceNumber(Math.abs(item.cost));
            OriginalCreditBillInfo info=new OriginalCreditBillInfo();info.setNumber(item.fromAssetId==asset.semanticId?Math.abs(item.cost)*-1.0d:Math.abs(item.cost));info.setRemark(item.remark);info.setDate(item.time);
            if(item.fromAssetId==asset.semanticId){String name=data.assetName(item.toAssetId);info.setTitle(name==null?"转账到已删账户":"转账到"+name);info.setIcon(data.operationIcon(3,"ic_transfer_out"));}
            else if(item.toAssetId==asset.semanticId){String name=data.assetName(item.fromAssetId);info.setTitle(name==null?"从已删账户转入":"从"+name+"转入");info.setIcon(data.operationIcon(2,"ic_transfer_in"));}else info.setTitle("");period.addBill(info);
        }
        for(Lend item:data.lends(asset.semanticId)){
            boolean first=(item.type==2||item.type==1||item.type==3)&&item.billId>0;
            OriginalBillRecord linked=item.billId>0?data.billBySemantic(item.billId):null;
            if(first&&linked!=null&&linked.assetId==asset.semanticId)continue;
            Integer linkedType=item.type==4?data.assetType(item.assetId):null;
            if(!first&&item.type==4&&item.billId>0&&!item.generalServiceBill&&linkedType!=null&&linkedType==7&&linked!=null&&linked.assetId==asset.semanticId)continue;
            OriginalCreditBill period=period(asset,day,item.time,map,data);boolean repaymentSide=item.repaymentAssetId==asset.semanticId&&(item.type==2||item.type==3);
            if(first){if(repaymentSide)period.reduceNumber(Math.abs(item.fromCost));else period.addNumber(Math.abs(item.fromCost));}else{if(repaymentSide)period.addNumber(Math.abs(item.fromCost));else period.reduceNumber(Math.abs(item.fromCost));}
            OriginalCreditBillInfo info=new OriginalCreditBillInfo();info.setNumber(repaymentSide?Math.abs(item.fromCost):Math.abs(item.fromCost)*-1.0d);info.setRemark(item.remark);info.setDate(item.time);
            int operation=item.type==1?6:item.type==2?5:item.type==3?7:4;String title=item.type==1?"借出-":item.type==2?"收款-":item.type==3?"借入-":item.type==4?"还款-":"";info.setTitle(title.isEmpty()?"":title+data.assetName(item.assetId));if(item.type>=1&&item.type<=4)info.setIcon(data.operationIcon(operation,item.type==1?"ic_data_jiechu":item.type==2?"ic_data_collection":item.type==3?"ic_data_jieru":"ic_data_repayment"));period.addBill(info);
        }
        for(Stock item:data.stocks(asset.semanticId)){
            OriginalCreditBill period=period(asset,day,item.doTime,map,data);if(item.type==2)period.addNumber(Math.abs(item.totalCost));else period.reduceNumber(Math.abs(item.totalCost));OriginalCreditBillInfo info=new OriginalCreditBillInfo();info.setNumber(item.type==2?Math.abs(item.totalCost)*-1.0d:Math.abs(item.totalCost));info.setRemark(item.remark);info.setDate(item.doTime);info.setTitle(item.type==1?"卖出理财-"+data.stockName(item.stockAssetId):item.type==2?"买入理财-"+data.stockName(item.stockAssetId):"");if(item.type==1||item.type==2)info.setIcon(data.operationIcon(item.type==1?11:10,item.type==1?"ic_stock_out":"ic_stock_in"));period.addBill(info);
        }
        List<OriginalInstalmentRecord> standalone=data.instalments(asset.semanticId);
        for(OriginalInstalmentRecord row:standalone)for(OriginalInstalmentSchedule.Period item:OriginalInstalmentSchedule.periods(row)){
            OriginalCreditBill period=instalmentPeriod(asset,day,item,map,data);OriginalCreditBillInfo info=new OriginalCreditBillInfo();info.setTitle("["+item.periods+"/"+row.periods+"]"+row.accountMonth+"还款分期");info.setIcon("font:分");info.setNumber(Math.abs(item.number)*-1.0d);info.setDate(OriginalInstalmentSchedule.parse(item.date,"yyyy年MM月dd日"));period.addInstalment(info);
        }
        for(OriginalInstalmentRecord row:standalone)if(!empty(row.accountMonth)){OriginalCreditBill period=map.get(row.accountMonth+"账单");if(period!=null){period.addInstalmentNumber(row.totalNumber);period.reduceNumber(row.totalNumber);}}
        List<OriginalCreditBill> result=new ArrayList<>(map.values());if(markCurrent)for(OriginalCreditBill period:result)if(data.now()>period.getMinTime()&&data.now()<period.getMaxTime())period.setCurrent(true);
        if(publication!=null)publication.periods(result);
        Collections.sort(result);
        double balance=asset.balance.getAsDouble();Double remaining=null;
        for(OriginalCreditBill period:result){
            if(h2.r(balance)<0.0d){double next=h2.r((Math.max(0.0d,period.getNumber())+balance)-period.getServiceCost());double interest=interest(asset,day,period,data);period.setRemainServiceCost(Math.max(0.0d,Math.abs(period.getServiceCost())-interest));period.setRemindNumber(next<0.0d?period.getNumber():h2.r(Math.abs(balance)+Math.max(0.0d,Math.abs(period.getServiceCost())-interest)));remaining=remaining==null?Math.abs(period.getServiceCost())-interest:(remaining+Math.abs(period.getServiceCost()))-interest;balance=next;}
            else{
                if(h2.r(balance)!=0.0d||period.getServiceCost()<=0.0d||OriginalInstalmentSchedule.parse(period.getRepaymentDay(),"yyyy-MM-dd")<=OriginalInstalmentSchedule.dayEnd(data.now()))break;
                double interest=interest(asset,day,period,data);period.setRemainServiceCost(Math.max(0.0d,Math.abs(period.getServiceCost())-interest));if(interest<period.getServiceCost()){period.setRemindNumber(h2.r(period.getServiceCost()-interest));remaining=remaining==null?Math.abs(period.getServiceCost())-interest:(remaining+Math.abs(period.getServiceCost()))-interest;}
            }
            if(remaining!=null&&publication!=null)publication.remainingServiceCost(remaining);
        }
        return new Result(result,remaining);
    }
    private static double interest(Asset asset,int statementDay,OriginalCreditBill period,DataSource data){
        OriginalDayInfo previous=OriginalInstalmentSchedule.statementPeriod(statementDay,OriginalInstalmentSchedule.addDays(period.getMinTime(),-1),data.configuredMonthStart());String previousRepayment=OriginalInstalmentSchedule.repaymentDay(asset.inAccountDate,previous,true);
        long start=OriginalInstalmentSchedule.dayStart(OriginalInstalmentSchedule.addDays(OriginalInstalmentSchedule.parse(previousRepayment,"yyyy-MM-dd"),1)),end=OriginalInstalmentSchedule.dayEnd(OriginalInstalmentSchedule.parse(period.getRepaymentDay(),"yyyy-MM-dd"));double sum=0.0d;List<Lend> rows=data.interestRows(asset.semanticId,start,end);if(rows!=null)for(Lend row:rows)if(row.interest>0.0d)sum+=row.interest;return sum;
    }
    private static OriginalCreditBill instalmentPeriod(Asset asset,int day,OriginalInstalmentSchedule.Period item,Map<String,OriginalCreditBill> map,DataSource data){OriginalCreditBill period=period(asset,day,OriginalInstalmentSchedule.parse(item.date,"yyyy年MM月dd日"),map,data);period.addServiceCost(Math.abs(item.serviceCost));period.addNumber(Math.abs(item.number));return period;}
    private static OriginalCreditBill period(Asset asset,int day,long time,Map<String,OriginalCreditBill> map,DataSource data){
        OriginalDayInfo boundaries=OriginalInstalmentSchedule.statementPeriod(day,time,data.configuredMonthStart());String title=OriginalInstalmentSchedule.year(boundaries.getMaxDay())+"年"+OriginalInstalmentSchedule.month(boundaries.getMaxDay())+"月账单";OriginalCreditBill existing=map.get(title);if(existing!=null)return existing;
        OriginalCreditBill value=new OriginalCreditBill();value.setInDate(OriginalInstalmentSchedule.format(boundaries.getMinDay(),"yyyy.MM.dd")+"-"+OriginalInstalmentSchedule.format(boundaries.getMaxDay(),"yyyy.MM.dd"));value.setMaxTime(boundaries.getMaxDay());value.setMinTime(boundaries.getMinDay());value.setTime(boundaries.getMaxDay());value.setYear(OriginalInstalmentSchedule.year(boundaries.getMaxDay()));value.setMonth(OriginalInstalmentSchedule.month(boundaries.getMaxDay()));value.setTitle(title);value.setAssetId(asset.semanticId);value.setRepaymentDay(OriginalInstalmentSchedule.repaymentDay(asset.inAccountDate,boundaries,true));map.put(title,value);return value;
    }
    private static boolean empty(String value){return value==null||value.isEmpty();}
    public static OriginalBillRecord copyBill(OriginalBillRecord r){
        OriginalBillRecord v=new OriginalBillRecord();v.localRowId=r.localRowId;v.assetId=r.assetId;v.billId=r.billId;v.billShareInfo=r.billShareInfo;v.billType=r.billType;v.bookId=r.bookId;v.childCategoryId=r.childCategoryId;v.cost=r.cost;v.currencyAssetNumber=r.currencyAssetNumber;v.currencyDiscountNumber=r.currencyDiscountNumber;v.currencyInfo=r.currencyInfo;v.discountNumber=r.discountNumber;v.fromUserId=r.fromUserId;v.inAssetTime=r.inAssetTime;v.notIntoBudget=r.notIntoBudget;v.notIntoTotal=r.notIntoTotal;v.parentCategoryId=r.parentCategoryId;v.poiAddress=r.poiAddress;v.recordTime=r.recordTime;v.reimbursement=r.reimbursement;v.reimbursementEnd=r.reimbursementEnd;v.remark=r.remark;v.tags=r.tags==null?null:new ArrayList<>(r.tags);v.time=r.time;v.totalAddress=r.totalAddress;v.updateTime=r.updateTime;v.userId=r.userId;return v;
    }
}
