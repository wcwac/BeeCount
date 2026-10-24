package com.wangc.bill.reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONObject;

/** Actual local financial effects. Transport callbacks and unported model stores stay explicit. */
public final class OriginalFinancialEffects implements OriginalFinancialOrchestrator.Effects {
    public enum Event {ASSETS_CHANGED,BILLS_CHANGED,TRANSFERS_CHANGED,LENDS_CHANGED,CREDIT_CHANGED,RESTORED}
    public interface Listener {void onFinancialEvent(Event event);}
    public enum CreditStatus {LOADING,READY,FAILED}
    private static final Map<ReferenceStore,State> STATES=new IdentityHashMap<>();
    private final ReferenceStore store;
    private final int owner,bookOwner;
    private final State state;
    private final Map<OriginalBillRecord,Boolean> newBeforeSave=new IdentityHashMap<>();
    public OriginalFinancialEffects(ReferenceStore store,int owner,int bookOwner){if(store==null)throw new IllegalArgumentException("Store is required");this.store=store;this.owner=owner;this.bookOwner=bookOwner;synchronized(store){state=state(store);}}
    private static final class State {
        final OriginalFinancialScheduler scheduler=new OriginalFinancialScheduler();
        final CopyOnWriteArrayList<Listener> listeners=new CopyOnWriteArrayList<>();
        final Map<String,Manager> managers=new HashMap<>();
        final Map<String,List<JSONObject>> homeDays=new HashMap<>(),calendarDays=new HashMap<>();
        final Map<String,Long> monthMaximum=new HashMap<>();
        final Map<String,Double> refunds=new HashMap<>();
        final Map<String,JSONObject> assets=new HashMap<>();
        volatile long creditGeneration;
        State(){scheduler.initializeCredit();}
    }
    /** Caller always obtains store before STATES. No callback runs while holding STATES. */
    private static State state(ReferenceStore store){synchronized(STATES){State value=STATES.get(store);if(value==null){value=new State();STATES.put(store,value);}return value;}}
    private Manager manager(){return manager(store,state,owner,bookOwner);}
    private static Manager manager(ReferenceStore store,State state,int owner,int bookOwner){String key=owner+":"+bookOwner;Manager result=state.managers.get(key);if(result==null){result=new Manager(store,state,owner);state.managers.put(key,result);result.initialize();}return result;}
    public static void subscribe(ReferenceStore store,Listener listener){if(listener==null)throw new IllegalArgumentException("Listener is required");synchronized(store){state(store).listeners.addIfAbsent(listener);}}
    public static void unsubscribe(ReferenceStore store,Listener listener){synchronized(store){State state; synchronized(STATES){state=STATES.get(store);}if(state!=null)state.listeners.remove(listener);}}
    private static void publish(State state,Event event){long generation=state.creditGeneration;state.scheduler.executeMain(()->{if(generation==state.creditGeneration)for(Listener listener:state.listeners)listener.onFinancialEvent(event);});}
    @Override public void schedule(Runnable callback,long delayMillis){long stamp=state.scheduler.epoch();state.scheduler.schedule(()->{synchronized(store){if(stamp==state.scheduler.epoch())callback.run();}},delayMillis);}
    @Override public void assetChanged(OriginalAssetBalanceStore.LoadedAsset asset,boolean refreshCredit){
        if(asset==null)return;synchronized(store){if(asset.assetType==2&&refreshCredit)manager().refresh(asset);asset.getAssetNumber();}
        // f.l/m's H0 getter is retained; no fake HTTP failure or eager sync debt is generated.
    }
    @Override public void assetBalanceChangeCompleted(OriginalAssetBalanceStore.LoadedAsset asset){if(asset!=null)publish(state,Event.ASSETS_CHANGED);}
    @Override public void assetOperationRecorded(OriginalAssetBalanceStore.LoadedAsset asset){if(asset!=null)new OriginalCurdHistoryRepository(store).recordAssetOperation(owner,asset.semanticId);}
    @Override public void beforeBillEntitySave(OriginalBillRecord bill){
        synchronized(store){newBeforeSave.put(bill,bill.localRowId==0);}
        // t.y requires original AutoTag storage; current experiment cannot represent those rows.
        // Never substitute merchant rules/local tag PKs for original AutoTag semantic identities.
    }
    @Override public void afterBillEntitySaveBeforeLearning(OriginalBillRecord bill,boolean saved){synchronized(store){Boolean created=newBeforeSave.remove(bill);if(Boolean.FALSE.equals(created))refreshGroup(bill);refreshCredit(bill);invalidateDates(bill);}}
    @Override public void billSaved(OriginalBillRecord bill,boolean saved){billSaved(bill,saved,true);}
    @Override public void billSaved(OriginalBillRecord bill,boolean saved,boolean emitNormalEvents){if(emitNormalEvents){publish(state,Event.BILLS_CHANGED);publish(state,Event.ASSETS_CHANGED);}}
    /** w2.g/q0's g7.n0 tail also runs after save=false. It is not s2.A or a Bill event. */
    public void transferChanged(OriginalTransferRecord transfer,boolean saved){
        if(transfer==null)throw new IllegalArgumentException("Transfer is required");
        publish(state,Event.TRANSFERS_CHANGED);
    }
    /** AutoAdd.X posts g7.u after attachments, including save=false; i1.d/w0 do not. */
    public void lendChanged(OriginalLendRecord lend,boolean saved){
        if(lend==null)throw new IllegalArgumentException("Lend is required");
        // This notification is not s2.A: the source asset callbacks own any queued rebuild.
        publish(state,Event.LENDS_CHANGED);
    }
    /** Date caches are invalidated even after a returned-false entity save. */
    public void parentUpdated(OriginalBillRecord parent,boolean saved){
        invalidateDates(parent);if(parent!=null)publish(state,Event.BILLS_CHANGED);
    }
    private void invalidateDates(OriginalBillRecord parent){
        if(parent==null)return;synchronized(store){String day=OriginalInstalmentSchedule.format(parent.time,"yyyy-MM-dd"),month=OriginalInstalmentSchedule.year(parent.time)+"-"+OriginalInstalmentSchedule.month(parent.time);state.homeDays.keySet().removeIf(key->key.endsWith(":"+day));state.calendarDays.keySet().removeIf(key->key.endsWith(":"+day));state.monthMaximum.remove(month);}
    }
    public void beforeSimpleBillDeletion(OriginalBillRecord bill){invalidateDates(bill);}
    public void afterSimpleBillDeletion(OriginalBillRecord bill){if(bill!=null&&bill.assetId!=-1L)publish(state,Event.ASSETS_CHANGED);}
    /** s2.A: fresh lookup at this call, then retain that exact handle for the queued rebuild. */
    public void refreshCredit(OriginalBillRecord bill){if(bill==null||bill.assetId==0)return;creditRefresh(bill.assetId);}
    public void creditRefresh(long semanticAsset){synchronized(store){OriginalAssetBalanceStore.LoadedAsset asset=new OriginalAssetBalanceStore(store).load(owner,semanticAsset);if(asset!=null&&asset.assetType==2)manager().refresh(asset);}}
    public void refundAggregateChanged(int parentSemantic,double total,boolean saved){synchronized(store){state.refunds.put(owner+":"+parentSemantic,total);}}
    /** c0.B's original BillGroup rows are not representable in the current host schema. */
    public void refreshGroup(OriginalBillRecord parent){
        // Explicit remaining port: no BillGroup rows exist in this host schema. Do not emit
        // a fake E3 event or convert unrelated legacy data into an original group.
    }
    /** f.d/q1's actual semantic metadata cache and MAIN g7.d tail. */
    public void assetCreatedOrEdited(long semanticAsset){JSONObject row;synchronized(store){row=store.assetRepository().p(semanticAsset,owner);}assetCreatedOrEdited(semanticAsset,row);}
    /** Original d/q1 still publish retained in-memory metadata when entity save returnsfalse. */
    public void assetCreatedOrEdited(long semanticAsset,JSONObject retainedMetadata){synchronized(store){String key=owner+":"+semanticAsset;if(retainedMetadata==null)state.assets.remove(key);else state.assets.put(key,Json.copy(retainedMetadata));}publish(state,Event.ASSETS_CHANGED);}
    public void assetDeleted(long semanticAsset){synchronized(store){state.assets.remove(owner+":"+semanticAsset);manager().remove(semanticAsset);}publish(state,Event.ASSETS_CHANGED);}
    public static JSONObject cachedAsset(ReferenceStore store,int owner,long semanticAsset){synchronized(store){State state=state(store);String key=owner+":"+semanticAsset;JSONObject row=state.assets.get(key);if(row==null){row=store.assetRepository().p(semanticAsset,owner);if(row!=null)state.assets.put(key,Json.copy(row));}return row==null?null:Json.copy(row);}}
    static Double cachedRefund(ReferenceStore store,int owner,int parent){synchronized(store){State state=state(store);return state.refunds.get(owner+":"+parent);}}
    public static List<JSONObject> dayBills(ReferenceStore store,long book,long time,boolean calendar){
        synchronized(store){State state=state(store);String day=book+":"+OriginalInstalmentSchedule.format(time,"yyyy-MM-dd");Map<String,List<JSONObject>> cache=calendar?state.calendarDays:state.homeDays;List<JSONObject> rows=cache.get(day);
            if(rows==null){rows=store.transactions(book,OriginalInstalmentSchedule.dayStart(time),OriginalInstalmentSchedule.dayEnd(time)+1,"",false);cache.put(day,rows);}List<JSONObject> copy=new ArrayList<>();for(JSONObject row:rows)copy.add(Json.copy(row));return copy;}
    }
    public static List<OriginalCreditBill> creditStatements(ReferenceStore store,int owner,int bookOwner,long semanticAsset){synchronized(store){return manager(store,state(store),owner,bookOwner).snapshot(semanticAsset);}}
    public static CreditStatus creditStatus(ReferenceStore store,int owner,int bookOwner,long semanticAsset){synchronized(store){return manager(store,state(store),owner,bookOwner).status.getOrDefault(semanticAsset,CreditStatus.LOADING);}}
    public static String creditFailureType(ReferenceStore store,int owner,int bookOwner,long semanticAsset){synchronized(store){return manager(store,state(store),owner,bookOwner).failures.getOrDefault(semanticAsset,"");}}
    /** Read-only completed-build identity; useful when two revisions have the same rows. */
    public static long creditRevision(ReferenceStore store,int owner,int bookOwner,long semanticAsset){synchronized(store){return manager(store,state(store),owner,bookOwner).revisions.getOrDefault(semanticAsset,0L);}}
    /** Raw feeds and independent n0/u events are wired; complete domain command/UI parity is separate. */
    public static List<String> unportedDomains(){return java.util.Collections.unmodifiableList(java.util.Arrays.asList("TransferCommands","TransferUI","LendCommands","LendUI","StockInfo","OperationIcon","AutoTag","BillGroup"));}
    public static Map<String,List<OriginalCreditBill>> repaymentSchedule(ReferenceStore store,int owner,int bookOwner){synchronized(store){Manager manager=manager(store,state(store),owner,bookOwner);Map<String,List<OriginalCreditBill>> result=new HashMap<>();for(Map.Entry<String,List<OriginalCreditBill>> entry:manager.byDate.entrySet()){List<OriginalCreditBill> list=new ArrayList<>();for(OriginalCreditBill bill:entry.getValue())list.add(bill.copy());result.put(entry.getKey(),list);}return result;}}
    public static void resetAfterRestore(ReferenceStore store){
        State state;synchronized(store){synchronized(STATES){state=STATES.get(store);}if(state==null)return;state.scheduler.resetAfterRestore();state.scheduler.initializeCredit();state.managers.clear();state.homeDays.clear();state.calendarDays.clear();state.monthMaximum.clear();state.refunds.clear();state.assets.clear();}publish(state,Event.RESTORED);
    }
    /** Host context/epoch extension; current_book remains a local row ID, never a semantic ID. */
    public static void bookSelectionChanged(ReferenceStore store){
        State state;synchronized(store){synchronized(STATES){state=STATES.get(store);}if(state==null)return;state.creditGeneration++;state.scheduler.initializeCredit();state.managers.clear();state.homeDays.clear();state.calendarDays.clear();state.monthMaximum.clear();manager(store,state,ReferenceStore.LOCAL_OFFLINE_OWNER,selectedBookOwner(store));}publish(state,Event.BILLS_CHANGED);publish(state,Event.ASSETS_CHANGED);publish(state,Event.CREDIT_CHANGED);
    }
    private static JSONObject selectedBook(ReferenceStore store){long row=store.context().getSharedPreferences("reference_settings",android.content.Context.MODE_PRIVATE).getLong("current_book",1);JSONObject book=store.book(row);if(book==null&&!store.books().isEmpty())book=store.books().get(0);return book;}
    private static int selectedBookOwner(ReferenceStore store){JSONObject book=selectedBook(store);return book==null||book.isNull("owner_semantic_id")?ReferenceStore.LOCAL_OFFLINE_OWNER:book.optInt("owner_semantic_id");}
    public static void close(ReferenceStore store){State state;synchronized(store){synchronized(STATES){state=STATES.remove(store);}if(state==null)return;state.listeners.clear();state.scheduler.close();}}
    private static final class Manager {
        final ReferenceStore store;final State state;final int owner;final OriginalCreditDataSource data;final long generation;
        final Map<String,List<OriginalCreditBill>> byDate=new HashMap<>();
        final Map<String,OriginalCreditBill> byDateAndAsset=new HashMap<>();
        final Map<Long,Double> serviceRemainder=new HashMap<>();
        final Map<Long,CreditStatus> status=new HashMap<>();final Map<Long,String> failures=new HashMap<>();
        final Map<Long,Long> revisions=new HashMap<>();
        Manager(ReferenceStore store,State state,int owner){this.store=store;this.state=state;this.owner=owner;data=new OriginalCreditDataSource(store,owner);generation=state.creditGeneration;}
        boolean current(long epoch){return epoch==state.scheduler.epoch()&&generation==state.creditGeneration;}
        void initialize(){long stamp=state.scheduler.epoch();state.scheduler.executeCredit(()->{boolean completed=true;synchronized(store){if(!current(stamp))return;byDate.clear();byDateAndAsset.clear();serviceRemainder.clear();JSONObject book=selectedBook(store);if(book!=null&&!book.isNull("semantic_id"))for(JSONObject row:store.query("SELECT a.semantic_id FROM accounts a WHERE a.asset_origin='original' AND a.owner_semantic_id=? AND a.asset_type=2 AND (NOT EXISTS(SELECT 1 FROM asset_show_books s WHERE s.account_row_id=a.id) OR EXISTS(SELECT 1 FROM asset_show_books s WHERE s.account_row_id=a.id AND s.book_semantic_id=?)) ORDER BY a.position_weight DESC",""+owner,""+book.optLong("semantic_id"))){OriginalAssetBalanceStore.LoadedAsset asset=new OriginalAssetBalanceStore(store).load(owner,row.optLong("semantic_id"));if(asset!=null)completed=build(asset)&&completed;}}if(current(stamp)){if(completed)changed(stamp);else failed(stamp);}});}
        void refresh(OriginalAssetBalanceStore.LoadedAsset asset){
            // Capture metadata at enqueue; preserve numeric getter on the same loaded handle.
            JSONObject metadata=asset.sourceMetadata();long stamp=state.scheduler.epoch();status.put(asset.semanticId,CreditStatus.LOADING);state.scheduler.executeCredit(()->{boolean completed=false;synchronized(store){if(!current(stamp))return;removeEntries(asset.semanticId);serviceRemainder.remove(asset.semanticId);completed=build(asset,metadata);}if(current(stamp)){if(completed)changed(stamp);else failed(stamp);}});
        }
        boolean build(OriginalAssetBalanceStore.LoadedAsset asset){return build(asset,asset.sourceMetadata());}
        boolean build(OriginalAssetBalanceStore.LoadedAsset asset,JSONObject metadata){
            try{OriginalCreditProjection.Asset input=new OriginalCreditProjection.Asset();input.semanticId=asset.semanticId;input.inAccountDate=metadata.isNull("in_account_date")?null:metadata.optString("in_account_date");input.outAccountDate=metadata.isNull("out_account_date")?null:metadata.optString("out_account_date");input.outDayNotNext=metadata.optInt("out_day_not_next")!=0;input.balance=asset::getAssetNumber;
                OriginalCreditProjection.build(input,data,false,new OriginalCreditProjection.Publication(){public void periods(List<OriginalCreditBill> rows){for(OriginalCreditBill bill:rows){byDate.computeIfAbsent(bill.getRepaymentDay(),key->new ArrayList<>()).add(bill);byDateAndAsset.put(bill.getRepaymentDay()+":"+bill.getAssetId(),bill);}}public void remainingServiceCost(double value){serviceRemainder.put(asset.semanticId,value);}});status.put(asset.semanticId,CreditStatus.READY);failures.remove(asset.semanticId);return true;
            }catch(Exception error){status.put(asset.semanticId,CreditStatus.FAILED);failures.put(asset.semanticId,error.getClass().getName());return false;}finally{revisions.put(asset.semanticId,revisions.getOrDefault(asset.semanticId,0L)+1);}
        }
        void removeEntries(long semantic){for(List<OriginalCreditBill> list:byDate.values())list.removeIf(bill->bill.getAssetId()==semantic);Iterator<Map.Entry<String,OriginalCreditBill>> iterator=byDateAndAsset.entrySet().iterator();while(iterator.hasNext())if(iterator.next().getValue().getAssetId()==semantic)iterator.remove();}
        void remove(long semantic){long stamp=state.scheduler.epoch();state.scheduler.executeCredit(()->{synchronized(store){if(!current(stamp))return;removeEntries(semantic);status.remove(semantic);failures.remove(semantic);}if(current(stamp))changed(stamp);});} // s2.x leaves serviceRemainder intact.
        List<OriginalCreditBill> snapshot(long semantic){List<OriginalCreditBill> result=new ArrayList<>();for(OriginalCreditBill bill:byDateAndAsset.values())if(bill.getAssetId()==semantic)result.add(bill.copy());java.util.Collections.sort(result);return result;}
        void changed(long stamp){synchronized(store){if(!current(stamp))return;OriginalRepaymentWidget.refreshInstalled(store);publish(state,Event.CREDIT_CHANGED);}}
        void failed(long stamp){synchronized(store){if(current(stamp))publish(state,Event.CREDIT_CHANGED);}} // Visible error metadata, no false widget-success notification.
    }
}
