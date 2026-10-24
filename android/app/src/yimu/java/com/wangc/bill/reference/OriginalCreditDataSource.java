package com.wangc.bill.reference;

import android.content.Context;
import android.content.SharedPreferences;
import com.wangc.bill.utils.h2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;

/** Real current-store Bill/Refund/Reimbursement/Instalment/Transfer/Lend credit inputs. */
final class OriginalCreditDataSource implements OriginalCreditProjection.DataSource {
    private final ReferenceStore store;
    private final int owner;
    private final OriginalBillRepository bills;
    private final OriginalInstalmentRepository instalments;
    OriginalCreditDataSource(ReferenceStore store,int owner){this.store=store;this.owner=owner;bills=new OriginalBillRepository(store);instalments=new OriginalInstalmentRepository(store);}
    private SharedPreferences preferences(){return store.context().getSharedPreferences("reference_settings",Context.MODE_PRIVATE);}
    @Override public List<OriginalBillRecord> bills(long asset){
        synchronized(store){List<OriginalBillRecord> result=new ArrayList<>();for(JSONObject row:store.query("SELECT id FROM transactions WHERE bill_origin='original' AND deleted=0 AND bill_asset_semantic_id=? AND (bill_user_id=? OR bill_from_user_id=?) ORDER BY bill_time DESC,id DESC",""+asset,""+owner,""+owner))result.add(bills.byLocalRowId(row.optLong("id")));return result;}
    }
    @Override public OriginalCreditProjection.RefundAdjustment refund(OriginalBillRecord bill){
        OriginalRefundRepository.Snapshot row=new OriginalRefundRepository(store).firstByParent(owner,bill.billId);
        if(row==null)return new OriginalCreditProjection.RefundAdjustment(false,0.0d);
        // s2.g/l -> a2.F -> a2.x reads fresh refundNum, not a2.y's separate cache.
        if(bill.cost!=0.0d)return new OriginalCreditProjection.RefundAdjustment(true,row.data.refundNumber);
        double sum=0.0d;if(row.data.rawInfos!=null)for(String raw:row.data.rawInfos){OriginalRefundInfo info=OriginalRefundCodec.decodeStrict(raw);double restore=info.getCurrentRestoreNum();sum+=Math.abs(restore==0.0d&&info.relatedBillId==0?info.number:restore);}return new OriginalCreditProjection.RefundAdjustment(true,sum);
    }
    @Override public OriginalInstalmentRecord instalmentForBill(int semantic){return instalments.firstByBill(owner,semantic);}
    @Override public List<OriginalInstalmentRecord> instalments(long semantic){return instalments.forAsset(owner,semantic);}
    // a5.m uses a0.d2 (current user/fromUser), not a0.a0's current-book-owner expansion.
    private OriginalBillRecord receiptParent(int semantic){return bills.findAccessible(owner,owner,semantic);}
    private boolean assetExists(long semantic){return store.assetRepository().p(semantic,owner)!=null;}
    private String parentRemark(OriginalBillRecord bill){return empty(bill.remark)?categoryTitle(bill):bill.remark;}
    /** a5.m: reimbursements group equal (asset,time), refunds remain separate receipt rows. */
    @Override public List<OriginalCreditProjection.Receipt> receipts(long semantic){
        synchronized(store){
            List<OriginalCreditProjection.Receipt> result=new ArrayList<>();
            for(OriginalReimbursementRepository.Snapshot row:new OriginalReimbursementRepository(store).rawAssetLike(owner,semantic)){
                OriginalBillRecord parent=receiptParent(row.data.parentBillSemanticId);if(parent==null||row.data.rawEntries==null)continue;
                for(String raw:row.data.rawEntries){String[] pieces=raw.split(":");if(pieces.length<3)continue;long asset=Long.parseLong(pieces[0]);if(asset!=semantic||!assetExists(asset))continue;long time=Long.parseLong(pieces[2]);OriginalCreditProjection.Receipt receipt=null;
                    for(OriginalCreditProjection.Receipt previous:result)if(previous.assetId==asset&&previous.time==time){receipt=previous;break;}
                    if(receipt==null){receipt=new OriginalCreditProjection.Receipt();receipt.assetId=asset;receipt.time=time;receipt.amount=h2.S(pieces[1]);receipt.remark=parentRemark(parent);receipt.type=1;result.add(receipt);}else{receipt.amount=h2.r(receipt.amount+h2.S(pieces[1]));receipt.remark="批量报销";}
                    if(pieces.length==4)receipt.remark=pieces[3];
                }
            }
            for(OriginalRefundRepository.Snapshot row:new OriginalRefundRepository(store).rawAssetLike(owner,semantic)){
                OriginalBillRecord parent=receiptParent(row.data.parentBillSemanticId);if(parent==null||row.data.rawInfos==null)continue;
                for(String raw:row.data.rawInfos){OriginalRefundInfo info=OriginalRefundCodec.decodeStrict(raw);OriginalRefundCodec.applyLegacyAliasesForDetail(info,raw);if(info.assetId!=semantic||!assetExists(info.assetId))continue;OriginalCreditProjection.Receipt receipt=new OriginalCreditProjection.Receipt();receipt.assetId=semantic;receipt.time=info.inTime==0?info.time:info.inTime;receipt.amount=info.assetNum==0.0d?info.number:info.assetNum;receipt.type=2;receipt.remark=empty(info.remark)?parentRemark(parent):info.remark;result.add(receipt);}
            }
            result.sort((a,b)->Long.compare(b.time,a.time));return result;
        }
    }
    /** These original domains have no table in the current experiment, hence no representable rows. */
    public List<String> unportedDomains(){return java.util.Collections.unmodifiableList(java.util.Arrays.asList("StockInfo","OperationIcon"));}
    /** s2.g/l -> w2.b0: current owner, either endpoint, time DESC; no book/hide gate. */
    @Override public List<OriginalCreditProjection.Transfer> transfers(long asset){
        // s2 deliberately reads cost for both directions, never toCost/getCurrencyCost.
        // w2.R showBook filtering and w2.J aggregate scopes belong to other callers.
        synchronized(store){
            List<OriginalCreditProjection.Transfer> result=new ArrayList<>();
            for(OriginalTransferRecord row:new OriginalTransferRepository(store).forAsset(owner,asset)){
                if(row.originalDeleted)continue;
                OriginalCreditProjection.Transfer value=new OriginalCreditProjection.Transfer();
                value.fromAssetId=row.fromAssetId;value.toAssetId=row.toAssetId;
                value.cost=row.cost;value.time=row.time;value.remark=row.remark;
                result.add(value);
            }
            return result;
        }
    }
    /** s2.g/l -> i1.V: current owner and repaymentAssetId only; no ORDER BY. */
    @Override public List<OriginalCreditProjection.Lend> lends(long asset){
        synchronized(store){
            List<OriginalCreditProjection.Lend> result=new ArrayList<>();
            for(OriginalLendRecord row:new OriginalLendRepository(store).forRepaymentAsset(owner,asset))if(!row.originalDeleted)result.add(lend(row));
            return result;
        }
    }
    @Override public List<OriginalCreditProjection.Stock> stocks(long asset){return Collections.emptyList();}
    /** i1.g0 uses the principal asset, type4 and inclusive outTime bounds, newest first. */
    @Override public List<OriginalCreditProjection.Lend> interestRows(long asset,long start,long end){
        synchronized(store){
            List<OriginalCreditProjection.Lend> result=new ArrayList<>();
            for(OriginalLendRecord row:new OriginalLendRepository(store).interestRows(owner,asset,start,end))if(!row.originalDeleted)result.add(lend(row));
            return result;
        }
    }
    private static OriginalCreditProjection.Lend lend(OriginalLendRecord row){
        OriginalCreditProjection.Lend value=new OriginalCreditProjection.Lend();
        value.assetId=row.assetId;value.repaymentAssetId=row.repaymentAssetId;
        value.type=row.type;value.billId=row.billId;value.time=row.outTime;value.outTime=row.outTime;
        // s2 reads getFromCost directly, including zero; getCurrencyCost/number is a different API.
        value.fromCost=row.fromCost;value.interest=row.interest;
        value.generalServiceBill=row.generalServiceBill;value.remark=row.remark;
        return value;
    }
    // Linked-loan lookup s2 -> a0.N has the same current-user/fromUser scope.
    @Override public OriginalBillRecord billBySemantic(int semantic){return bills.findAccessible(owner,owner,semantic);}
    @Override public Integer assetType(long semantic){JSONObject row=store.assetRepository().p(semantic,owner);return row==null?null:row.optInt("asset_type");}
    @Override public String assetName(long semantic){JSONObject row=OriginalFinancialEffects.cachedAsset(store,owner,semantic);return row==null?null:row.optString("name");}
    @Override public String stockName(long semantic){return null;}
    private String categoryTitle(OriginalBillRecord bill){
        JSONObject parent=store.categoryRepository().parent(bill.parentCategoryId),child=store.one("SELECT * FROM categories WHERE level='child' AND semantic_id=? ORDER BY id LIMIT 1",""+bill.childCategoryId);String parentName=parent==null?null:parent.optString("name");
        if(child==null)return parentName;String name=child.optString("name");if(!preferences().getBoolean("category_simple",false))return parentName+"-"+name;return "其他".equals(name)?parentName:name;
    }
    @Override public OriginalCreditBillInfo billInfo(OriginalBillRecord bill){
        OriginalCreditBillInfo result=new OriginalCreditBillInfo();result.setNumber(bill.parentCategoryId==9?Math.abs(bill.getCurrencyCost()):Math.abs(bill.getCurrencyCost())*-1.0d);result.setRemark(bill.remark);String title=categoryTitle(bill);
        if(OriginalSettings.remarkMain(preferences())&&!empty(bill.remark)){result.setTitle(bill.remark);result.setRemark(title);}else result.setTitle(title);
        JSONObject icon=store.one("SELECT icon FROM categories WHERE level='child' AND semantic_id=? ORDER BY id LIMIT 1",""+bill.childCategoryId);if(icon==null)icon=store.categoryRepository().parent(bill.parentCategoryId);result.setIcon(icon==null?null:icon.optString("icon"));result.setBillId(bill.billId);result.setDate(bill.inAssetTime==0?bill.time:bill.inAssetTime);return result;
    }
    @Override public String operationIcon(int type,String fallback){return fallback;}
    @Override public int configuredMonthStart(){return preferences().getInt("month_start",0);}
    @Override public long now(){return System.currentTimeMillis();}
    private static boolean empty(String value){return value==null||value.isEmpty();}
}
