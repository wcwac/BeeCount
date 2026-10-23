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
    private static long lastSubmit;
    private static String lastKey, lastCandidate, lastPackage;
    private static volatile long restoreEpoch;
    // Parser callbacks may be singletons. Each durable candidate owns its own registration.
    private static final java.util.concurrent.ConcurrentHashMap<String,x1.a> HANDLERS = new java.util.concurrent.ConcurrentHashMap<>();
    private YimuKernel() {}
    /** Share the event submission lock so callbacks remain queue ordering barriers. */
    public static void enqueue(Runnable task){synchronized(WORKER){WORKER.execute(task);}}
    public static boolean isWorkerThread(){return Thread.currentThread()==workerThread;}
    public static int registeredCallbackCount(){return HANDLERS.size();}
    /** Called on the serial worker after a successful full database replacement. */
    public static synchronized void resetAfterRestore(){
        if(!isWorkerThread())throw new IllegalStateException("采集状态必须在采集工作线程重置");
        restoreEpoch++;
        java.util.Set<x1.a> callbacks=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        callbacks.addAll(HANDLERS.values());HANDLERS.clear();
        lastSubmit=0;lastKey=null;lastCandidate=null;lastPackage=null;
        com.google.android.accessibility.selecttospeak.SelectToSpeakService.f1838j=0;
        for(x1.a callback:callbacks)try{callback.dismiss();}catch(RuntimeException error){ReferenceDiagnostics.failure("RESTORE_PARSER_RESET_FAILED",error);}
    }

    public static synchronized void submit(BillInfo bill, x1.a callback) {
        ReferenceDiagnostics.attempt();
        String pkg = SOURCE.get();
        if (pkg == null || !AutoApps.accepts(pkg)) { ReferenceDiagnostics.result("STOPPED"); return; }
        // Recovered SelectToSpeakService.l: 1s between submissions, identical
        // captured number+time rejected for 10s. Retained for comparison.
        long now = System.currentTimeMillis();
        if (!com.wangc.bill.utils.h2.J(bill.getNumber())) { ReferenceDiagnostics.result("INVALID_AMOUNT"); return; }
        String key = bill.getAutoRepeatString();
        if (now - lastSubmit <= 1000 || (key.equals(lastKey) && now - lastSubmit <= 10000)) { ReferenceDiagnostics.result("ORIGINAL_REPEAT_GATE"); return; }
        ReferenceStore store = ReferenceApplication.store();
        long bookId = ReferenceApplication.prefs().getLong("capture_book", 1);
        JSONObject book = store.book(bookId);
        if (book == null || book.optInt("archived") != 0 || !"CNY".equals(book.optString("currency"))) { ReferenceDiagnostics.result("TARGET_BOOK_UNAVAILABLE"); return; }
        String kind = bill.isTransfer() ? "transfer" : bill.isIncome() ? "income" : "expense";
        long amount = 0, discount = 0, fee = 0;
        try { amount = CapturedAmount.minorUnits(bill.getNumber()); } catch (IllegalArgumentException ignored) {}
        try { if (bill.getDiscount() != null) discount = Math.max(0, Money.signed(bill.getDiscount(), "CNY")); } catch (IllegalArgumentException ignored) {}
        try { fee = Math.max(0, Money.signed(BigDecimal.valueOf(bill.getServiceCharge()).toPlainString(), "CNY")); } catch (IllegalArgumentException ignored) {}
        long account = ReferenceApplication.prefs().getLong("default_account", 0), toAccount = 0;
        if(account>0&&store.one("SELECT id FROM accounts WHERE id=? AND archived=0 AND currency='CNY'",""+account)==null)account=0;
        if (bill.isTransfer()) { account = accountNamed(store, bill.getFromAsset()); toAccount = accountNamed(store, bill.getToAsset()); }
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
        if (rule != null && ReferenceApplication.prefs().getBoolean("remember_merchants", true)) {
            JSONObject c = store.one("SELECT * FROM categories WHERE id=? AND archived=0", "" + rule.optLong("category_id"));
            if (c != null && kind.equals(c.optString("kind"))) category = c.optLong("id");
            JSONObject a = store.one("SELECT * FROM accounts WHERE id=? AND archived=0 AND currency='CNY'", "" + rule.optLong("account_id"));
            if (a != null) account = a.optLong("id");
        }
        JSONArray tags = new JSONArray(); if (bill.getTagList() != null) for (Long id : bill.getTagList()) if (store.one("SELECT id FROM tags WHERE id=?", "" + id) != null) tags.put(id);
        JSONObject data = Json.object("book_id", bookId, "kind", kind, "amount", amount, "currency", "CNY", "original_number", bill.getNumber(), "category_id", category,
            "account_id", account, "to_account_id", toAccount, "remark", ReferenceApplication.prefs().getBoolean("add_remark",true)?safe(bill.getRemark()):"", "merchant", merchant,
            "origin", safe(bill.getOrigin()), "asset_text", safe(bill.getAsset()), "discount", discount, "fee", fee,
            "time", bill.getTime() > 0 ? bill.getTime() : now, "not_total", bill.isNotIntoTotal()||ReferenceApplication.prefs().getBoolean("capture_not_total",false), "not_budget", bill.isNotIntoBudget()||ReferenceApplication.prefs().getBoolean("capture_not_budget",false),
            "tag_ids", tags, "originalNumberValid", amount > 0,
            // Original repeat checks keep BillInfo's captured values, including a missing time (0).
            "original_amount", amount, "original_time", bill.getTime(), "original_income", bill.isIncome(), "original_currency", "CNY");
        if(capturedCategory!=null&&capturedCategory.optLong("id")==category){
            Json.put(data,"parent_semantic_id",bill.getParentCategoryId());Json.put(data,"child_semantic_id",bill.getChildCategoryId());
        }
        try {
            String id = store.addCandidate(data, pkg, bookId);
            lastSubmit = now;com.google.android.accessibility.selecttospeak.SelectToSpeakService.f1838j=now; lastKey = key; lastCandidate = id; lastPackage = pkg;
            if (callback != null) HANDLERS.put(id, callback);
            ReferenceDiagnostics.result("DRAFT_SAVED");
            // AutoSetting.autoAdd belongs to the attached entry window: it counts down for 5s
            // and invokes the same validated save action as a manual click. Never bypass it here.
            long epoch=restoreEpoch;
            MAIN.post(() -> {
                if(epoch!=restoreEpoch)return;
                if (ReferenceApplication.prefs().getBoolean("show_overlay", true)) CaptureOverlay.show(id, callback);
                CaptureNotifications.update();
            });
        } catch (Exception error) { ReferenceDiagnostics.failure("STORE_FAILED",error); MAIN.post(CaptureNotifications::error); }
    }
    private static long accountNamed(ReferenceStore store, String value) { if (value == null || value.isEmpty()) return 0; JSONObject row = store.one("SELECT id FROM accounts WHERE name=? AND archived=0 AND currency='CNY'", value); return row == null ? 0 : row.optLong("id"); }
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
        // Original closeBtn/u0 restart SelectToSpeakService.j's debounce clock on dismissal.
        synchronized(YimuKernel.class){lastSubmit=System.currentTimeMillis();com.google.android.accessibility.selecttospeak.SelectToSpeakService.f1838j=lastSubmit;}
        x1.a callback=HANDLERS.get(candidateId);
        if(callback!=null){
            try{enqueue(()->{if(HANDLERS.remove(candidateId,callback))callback.dismiss();});}
            catch(java.util.concurrent.RejectedExecutionException ignored){ReferenceDiagnostics.skipped();}
        }
    }
}
