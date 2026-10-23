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
    public static final ThreadPoolExecutor WORKER = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(32), task -> new Thread(task, "yimu-reference"), new ThreadPoolExecutor.AbortPolicy());
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    static final ThreadLocal<String> SOURCE = new ThreadLocal<>();
    private static long lastSubmit;
    private static String lastKey, lastCandidate, lastPackage;
    private static final java.util.concurrent.ConcurrentHashMap<x1.a,String> HANDLERS = new java.util.concurrent.ConcurrentHashMap<>();
    private YimuKernel() {}
    /** Share the event submission lock so callbacks remain queue ordering barriers. */
    public static void enqueue(Runnable task){synchronized(WORKER){WORKER.execute(task);}}

    public static synchronized void submit(BillInfo bill, x1.a callback) {
        ReferenceDiagnostics.attempt();
        String pkg = SOURCE.get();
        if (pkg == null || !AutoApps.accepts(pkg)) { ReferenceDiagnostics.result("STOPPED"); return; }
        // Recovered SelectToSpeakService.l: 1s between submissions, identical
        // normalized number+time rejected for 10s. Retained for comparison.
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
        try { amount = Money.positive(bill.getNumber(), "CNY"); } catch (IllegalArgumentException ignored) {}
        try { if (bill.getDiscount() != null) discount = Math.max(0, Money.signed(bill.getDiscount(), "CNY")); } catch (IllegalArgumentException ignored) {}
        try { fee = Math.max(0, Money.signed(BigDecimal.valueOf(bill.getServiceCharge()).toPlainString(), "CNY")); } catch (IllegalArgumentException ignored) {}
        long account = ReferenceApplication.prefs().getLong("default_account", 0), toAccount = 0;
        if(account>0&&store.one("SELECT id FROM accounts WHERE id=? AND archived=0 AND currency='CNY'",""+account)==null)account=0;
        if (bill.isTransfer()) { account = accountNamed(store, bill.getFromAsset()); toAccount = accountNamed(store, bill.getToAsset()); }
        long category = store.defaultCategory(kind.equals("transfer") ? "expense" : kind);
        String merchant = safe(bill.getShopName());
        JSONObject rule = store.one("SELECT * FROM rules WHERE merchant=?", merchant);
        if (rule != null && ReferenceApplication.prefs().getBoolean("remember_merchants", true)) {
            JSONObject c = store.one("SELECT * FROM categories WHERE id=? AND archived=0", "" + rule.optLong("category_id"));
            if (c != null && kind.equals(c.optString("kind"))) category = c.optLong("id");
            JSONObject a = store.one("SELECT * FROM accounts WHERE id=? AND archived=0 AND currency='CNY'", "" + rule.optLong("account_id"));
            if (a != null) account = a.optLong("id");
        }
        JSONArray tags = new JSONArray(); if (bill.getTagList() != null) for (Long id : bill.getTagList()) if (store.one("SELECT id FROM tags WHERE id=?", "" + id) != null) tags.put(id);
        JSONObject data = Json.object("book_id", bookId, "kind", kind, "amount", amount, "currency", "CNY", "category_id", category,
            "account_id", account, "to_account_id", toAccount, "remark", ReferenceApplication.prefs().getBoolean("add_remark",true)?safe(bill.getRemark()):"", "merchant", merchant,
            "origin", safe(bill.getOrigin()), "asset_text", safe(bill.getAsset()), "discount", discount, "fee", fee,
            "time", bill.getTime() > 0 ? bill.getTime() : now, "not_total", bill.isNotIntoTotal()||ReferenceApplication.prefs().getBoolean("capture_not_total",false), "not_budget", bill.isNotIntoBudget()||ReferenceApplication.prefs().getBoolean("capture_not_budget",false),
            "tag_ids", tags, "originalNumberValid", amount > 0);
        try {
            String id = store.addCandidate(data, pkg, bookId);
            lastSubmit = now;com.google.android.accessibility.selecttospeak.SelectToSpeakService.f1838j=now; lastKey = key; lastCandidate = id; lastPackage = pkg;
            if (callback != null) HANDLERS.put(callback, id);
            ReferenceDiagnostics.result("DRAFT_SAVED");
            if (ReferenceApplication.prefs().getBoolean("auto_save", false) && amount > 0 && (!kind.equals("transfer") || (account > 0 && toAccount > 0))) {
                store.saveTransaction(data, 0, id);
                ReferenceDiagnostics.result("AUTO_SAVED");
                if (callback != null) callback.dismiss();
            } else {
                MAIN.post(() -> {
                    if (ReferenceApplication.prefs().getBoolean("show_overlay", true)) CaptureOverlay.show(id, callback);
                    CaptureNotifications.update();
                });
            }
        } catch (Exception error) { ReferenceDiagnostics.failure("STORE_FAILED",error); MAIN.post(CaptureNotifications::error); }
    }
    private static long accountNamed(ReferenceStore store, String value) { if (value == null || value.isEmpty()) return 0; JSONObject row = store.one("SELECT id FROM accounts WHERE name=? AND archived=0 AND currency='CNY'", value); return row == null ? 0 : row.optLong("id"); }
    private static String safe(String value) { return value == null ? "" : value.substring(0, Math.min(2000, value.length())); }
    public static void enrichRemark(String remark) {
        String pkg = SOURCE.get();
        if (pkg == null || remark == null || remark.isEmpty() || !ReferenceApplication.prefs().getBoolean("add_remark",true)) return;
        String candidate;
        synchronized (YimuKernel.class) { candidate = pkg.equals(lastPackage) ? lastCandidate : null; }
        if (candidate != null) MAIN.postDelayed(() -> {
            try { enqueue(() -> ReferenceApplication.store().updateCandidateRemark(candidate, remark)); }
            catch (java.util.concurrent.RejectedExecutionException ignored) { ReferenceDiagnostics.skipped(); }
        }, 1000);
    }
    public static void dismissCallback(x1.a callback) {
        if (callback == null) return;
        try { enqueue(callback::dismiss); } catch (java.util.concurrent.RejectedExecutionException ignored) { ReferenceDiagnostics.skipped(); }
    }
    public static void complete(String candidateId) {
        if(candidateId==null)return;
        for(java.util.Map.Entry<x1.a,String> entry:HANDLERS.entrySet())if(candidateId.equals(entry.getValue())){
            try{enqueue(()->{if(HANDLERS.remove(entry.getKey(),candidateId))entry.getKey().dismiss();});}
            catch(java.util.concurrent.RejectedExecutionException ignored){ReferenceDiagnostics.skipped();}
        }
    }
}
