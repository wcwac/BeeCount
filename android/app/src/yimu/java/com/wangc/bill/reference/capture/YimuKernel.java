package com.wangc.bill.reference.capture;

import android.os.Handler;
import android.os.Looper;
import com.wangc.bill.auto.x1;
import com.wangc.bill.http.entity.BillInfo;
import com.wangc.bill.reference.Json;
import com.wangc.bill.reference.Money;
import com.wangc.bill.reference.ReferenceApplication;
import com.wangc.bill.reference.ReferenceStore;
import com.wangc.bill.reference.ui.CaptureOverlay;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class YimuKernel {
    private static volatile Thread workerThread;
    public static final ThreadPoolExecutor WORKER = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(32), task -> { Thread thread=new Thread(task,"yimu-reference");workerThread=thread;return thread; }, new ThreadPoolExecutor.AbortPolicy());
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    static final ThreadLocal<String> SOURCE = new ThreadLocal<>();
    private static String lastCandidate, lastPackage;
    private static volatile long restoreEpoch;
    // Parser callbacks may be singletons. Each durable candidate owns its own registration.
    private static final java.util.concurrent.ConcurrentHashMap<String,x1.a> HANDLERS = new java.util.concurrent.ConcurrentHashMap<>();
    private YimuKernel() {}
    /** Share the event submission lock so callbacks remain queue ordering barriers. */
    public static void enqueue(Runnable task){synchronized(WORKER){WORKER.execute(task);}}
    static long observationEpoch(){return restoreEpoch;}
    public static boolean isWorkerThread(){return Thread.currentThread()==workerThread;}
    public static int registeredCallbackCount(){return HANDLERS.size();}
    public static boolean hasRegisteredCallback(String candidateId){return candidateId!=null&&HANDLERS.containsKey(candidateId);}
    /** Called on the serial worker after a successful full database replacement. */
    public static synchronized void resetAfterRestore(){
        if(!isWorkerThread())throw new IllegalStateException("采集状态必须在采集工作线程重置");
        restoreEpoch++;
        java.util.Set<x1.a> callbacks=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        callbacks.addAll(HANDLERS.values());HANDLERS.clear();
        lastCandidate=null;lastPackage=null;CaptureEvidence.clear();
        com.google.android.accessibility.selecttospeak.SelectToSpeakService.f1838j=0;
        for(x1.a callback:callbacks)try{callback.dismiss();}catch(RuntimeException error){ReferenceDiagnostics.failure("RESTORE_PARSER_RESET_FAILED",error);}
    }

    public static synchronized void submit(BillInfo bill, x1.a callback) {
        ReferenceDiagnostics.attempt();
        String pkg = SOURCE.get();
        if (pkg == null || !AutoApps.accepts(pkg)) { ReferenceDiagnostics.result("STOPPED"); return; }
        // Every accepted recognition reaches durable candidate identity. A process-global
        // clock cannot distinguish duplicate screens from different real transactions.
        long now = System.currentTimeMillis();
        if (!com.wangc.bill.utils.h2.J(bill.getNumber())) { ReferenceDiagnostics.result("INVALID_AMOUNT"); return; }
        ReferenceStore store = ReferenceApplication.store();
        long requestedBookId = ReferenceApplication.prefs().getLong("capture_book", 1);
        long bookId=requestedBookId;JSONObject book=store.book(bookId);
        boolean unresolvedBook=book==null||book.optInt("archived")!=0||!"CNY".equals(book.optString("currency"));
        if(unresolvedBook){
            // A candidate needs a valid FK, not a guessed financial destination.
            // All save paths reject the unresolved flag until the user chooses a book.
            JSONObject anchor=store.one("SELECT id FROM books ORDER BY id LIMIT 1");
            if(anchor==null){ReferenceDiagnostics.result("NO_INBOX_STORAGE");MAIN.post(CaptureNotifications::error);return;}
            bookId=anchor.optLong("id");book=store.book(bookId);
        }
        String kind = bill.isTransfer() ? "transfer" : bill.isIncome() ? "income" : "expense";
        long amount = 0, discount = 0, fee = 0;
        try { amount = CapturedAmount.minorUnits(bill.getNumber()); } catch (IllegalArgumentException ignored) {}
        try { if (bill.getDiscount() != null) discount = Math.max(0, Money.signed(bill.getDiscount(), "CNY")); } catch (IllegalArgumentException ignored) {}
        try { fee = Math.max(0, Money.signed(BigDecimal.valueOf(bill.getServiceCharge()).toPlainString(), "CNY")); } catch (IllegalArgumentException ignored) {}
        long account = 0, toAccount = 0;
        long category = store.defaultCategory(kind.equals("transfer") ? "expense" : kind,bookId);
        // BillInfo category IDs identify original ParentCategory/ChildCategory records,
        // never local categories.id. Keep a recognized pair through the local FK bridge.
        JSONObject capturedCategory=null;
        if(!bill.isTransfer()&&bill.getParentCategoryId()>0&&bill.getChildCategoryId()>=-1
            &&kind.equals(bill.getParentCategoryId()==9?"income":"expense")){
            capturedCategory=store.categoryRepository().rowForSelection(kind,bill.getParentCategoryId(),bill.getChildCategoryId());
            if(capturedCategory!=null)category=capturedCategory.optLong("id");
        }
        String merchant = safe(bill.getShopName());
        JSONObject rule = store.one("SELECT * FROM rules WHERE merchant=?", merchant);
        // Generic parser origin labels are not real merchant identities.
        boolean trustedMerchant=!merchant.isEmpty() && !merchant.equals(safe(bill.getOrigin()))
                && !merchant.equals("淘宝") && !merchant.equals("美团") && !merchant.equals("微信") && !merchant.equals("支付宝");
        Long merchantAccount=null;
        if (trustedMerchant && rule != null && ReferenceApplication.prefs().getBoolean("remember_merchants", true)) {
            JSONObject c = store.one("SELECT * FROM categories WHERE id=? AND archived=0", "" + rule.optLong("category_id"));
            if (c != null && kind.equals(c.optString("kind"))) category = c.optLong("id");
            if(!rule.isNull("account_id"))merchantAccount=rule.optLong("account_id");
        }
        CapturedAccountResolver.Resolution accountResolution=CaptureAccountMappings.resolve(store,bookId,pkg,
                bill.isTransfer()?bill.getFromAsset():bill.getAsset(),bill.isTransfer()?null:merchantAccount,!bill.isTransfer());
        account=accountResolution.accountId;
        CapturedAccountResolver.Resolution destination=null;
        if(bill.isTransfer()) {
            destination=CaptureAccountMappings.resolve(store,bookId,pkg,bill.getToAsset(),null,false);
            toAccount=destination.accountId;
        }
        CaptureEvidence.Context evidence=CaptureEvidence.current();
        java.util.List<String> capturedTexts=evidence==null?java.util.Collections.emptyList():evidence.tokens();
        CaptureIdentity.Identity identity=CaptureIdentity.identify(pkg,evidence==null?"":evidence.sessionId,evidence==null?-1:evidence.windowId,kind,amount,
                CaptureIdentity.visibleTransactionTime(bill.getTime(),capturedTexts),merchant,
                bill.isTransfer()?safe(bill.getFromAsset())+" -> "+safe(bill.getToAsset()):bill.getAsset(),capturedTexts);
        if(unresolvedBook){account=0;toAccount=0;}
        boolean accountManual=unresolvedBook||accountResolution.requiresConfirmation || destination!=null&&destination.requiresConfirmation
                || bill.isTransfer()&&(account==0||toAccount==0||account==toAccount);
        JSONArray tags = new JSONArray(); if (bill.getTagList() != null) for (Long id : bill.getTagList()) if (store.one("SELECT id FROM tags WHERE id=?", "" + id) != null) tags.put(id);
        JSONObject data = Json.object("book_id", bookId, "kind", kind, "amount", amount, "currency", "CNY", "original_number", bill.getNumber(), "category_id", category,
            "account_id", account, "to_account_id", toAccount, "remark", ReferenceApplication.prefs().getBoolean("add_remark",true)?safe(bill.getRemark()):"", "merchant", merchant,
            "origin", safe(bill.getOrigin()), "asset_text", safe(bill.getAsset()), "discount", discount, "fee", fee,
            "time", bill.getTime() > 0 ? bill.getTime() : now, "not_total", bill.isNotIntoTotal()||ReferenceApplication.prefs().getBoolean("capture_not_total",false), "not_budget", bill.isNotIntoBudget()||ReferenceApplication.prefs().getBoolean("capture_not_budget",false),
            "tag_ids", tags, "originalNumberValid", amount > 0,
            // Original repeat checks keep BillInfo's captured values, including a missing time (0).
            "original_amount", amount, "original_time", bill.getTime(), "original_income", bill.isIncome(), "original_currency", "CNY", "original_remark", safe(bill.getRemark()));
        Json.put(data,"capture_requires_confirmation",unresolvedBook||identity.requiresConfirmation||accountManual||amount<=0);
        Json.put(data,"capture_target_book_unresolved",unresolvedBook);
        Json.put(data,"capture_requested_book_id",requestedBookId);
        Json.put(data,"from_asset_text",safe(bill.getFromAsset()));Json.put(data,"to_asset_text",safe(bill.getToAsset()));
        Json.put(data,"capture_window_key",identity.windowKey);
        Json.put(data,"capture_identity_kind",identity.kind);
        Json.put(data,"capture_duplicate_hint",identity.requiresConfirmation);
        Json.put(data,"capture_payment_id",identity.paymentId);
        Json.put(data,"capture_order_reference",identity.orderReference);
        Json.put(data,"capture_business_type",identity.businessType);
        Json.put(data,"capture_account_resolution",unresolvedBook?"TARGET_BOOK_UNRESOLVED":accountResolution.reason);
        Json.put(data,"capture_account_requires_confirmation",accountManual);
        if(destination!=null)Json.put(data,"capture_to_account_resolution",destination.reason);
        if(evidence!=null)Json.put(data,"capture_source_route",evidence.sourceRoute);
        if(capturedCategory!=null&&capturedCategory.optLong("id")==category){
            Json.put(data,"parent_semantic_id",bill.getParentCategoryId());Json.put(data,"child_semantic_id",bill.getChildCategoryId());
        }
        try {
            JSONObject prior=identity.key==null?null:store.captureIdentity(identity.key);
            if(prior==null&&identity.windowKey!=null)prior=store.captureIdentity(identity.windowKey);
            String id = store.upsertCapturedCandidate(data,pkg,bookId,identity.key,identity.kind);
            JSONObject persisted=store.candidate(id);
            if(persisted==null)throw new IllegalStateException("Persisted capture disappeared");
            if(!"PENDING".equals(persisted.optString("state"))) {
                JSONArray conflicts=Json.parse(persisted.optString("payload")).optJSONArray("capture_conflict_fields");
                if(conflicts!=null&&conflicts.length()>0){ReferenceDiagnostics.result("PAYMENT_EVIDENCE_CONFLICT");MAIN.post(CaptureNotifications::evidenceConflict);}
                else ReferenceDiagnostics.result("PAYMENT_ALREADY_CAPTURED");
                dismissCallback(callback);return;
            }
            com.google.android.accessibility.selecttospeak.SelectToSpeakService.f1838j=now;
            lastCandidate=id;lastPackage=pkg;
            if(callback!=null)HANDLERS.putIfAbsent(id,callback);
            boolean reused=prior!=null&&id.equals(prior.optString("id"));
            ReferenceDiagnostics.result(reused?"DRAFT_REUSED":"DRAFT_SAVED");
            if(unresolvedBook)ReferenceDiagnostics.result("DRAFT_WAITING_FOR_BOOK");
            // AutoSetting.autoAdd belongs to the attached entry window: it counts down for 5s
            // and invokes the same validated save action as a manual click. Never bypass it here.
            long epoch=restoreEpoch;
            MAIN.post(() -> {
                if(epoch!=restoreEpoch)return;
                if (!unresolvedBook && ReferenceApplication.prefs().getBoolean("show_overlay", true)) CaptureOverlay.show(id, callback);
                CaptureNotifications.update();
            });
        } catch (Exception error) { ReferenceDiagnostics.failure("STORE_FAILED",error); MAIN.post(CaptureNotifications::error); }
    }
    private static String safe(String value) { return value == null ? "" : value.substring(0, Math.min(2000, value.length())); }
    public static void enrichRemark(String remark) {
        String pkg = SOURCE.get();
        if (pkg == null || remark == null || remark.isEmpty() || !ReferenceApplication.prefs().getBoolean("add_remark",true)) return;
        String candidate;long epoch;
        synchronized (YimuKernel.class) { candidate = pkg.equals(lastPackage) ? lastCandidate : null;epoch=restoreEpoch; }
        if (candidate != null) MAIN.postDelayed(() -> {
            try { enqueue(() -> {
                if(epoch!=restoreEpoch)return;
                ReferenceApplication.store().updateCandidateRemark(candidate, remark);
                MAIN.post(() -> CaptureOverlay.enrichRemark(candidate, remark));
            }); }
            catch (java.util.concurrent.RejectedExecutionException ignored) { ReferenceDiagnostics.skipped(); }
        }, 1000);
    }
    public static void dismissCallback(x1.a callback) {
        if (callback == null) return;
        try { enqueue(callback::dismiss); } catch (java.util.concurrent.RejectedExecutionException ignored) { ReferenceDiagnostics.skipped(); }
    }
    public static void complete(String candidateId) {
        if(candidateId==null)return;
        // This separate timestamp belongs only to a recovered handler's page state machine.
        com.google.android.accessibility.selecttospeak.SelectToSpeakService.f1838j=System.currentTimeMillis();
        x1.a callback=HANDLERS.get(candidateId);
        if(callback!=null){
            try{enqueue(()->{if(HANDLERS.remove(candidateId,callback))callback.dismiss();});}
            catch(java.util.concurrent.RejectedExecutionException ignored){ReferenceDiagnostics.skipped();}
        }
    }
}
