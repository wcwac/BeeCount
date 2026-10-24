package com.wangc.bill.reference.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import com.wangc.bill.reference.ReferenceApplication;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** i2.u's singleton notification contract, with an explicitly host-owned native-dispatch bridge. */
public final class OriginalNotificationPermission {
    static final String TITLE = "通知栏权限使用说明";
    static final String CONTENT = "通知栏可显示自动记账当前状态，并提高自动记账稳定性";
    private static final String HOST_TOKEN = "reference_notification_owner_token";
    private static final String LOG_TAG = "ReferencePermission";
    // Native UtilsTransActivity registration/result forwarding is not recovered.
    // This process-local registry is a host boundary, not reconstructed original code.
    private static final Map<String, Pending> REQUESTS = new HashMap<>();
    private static volatile String lastBridgeFailure = "";
    private OriginalNotificationPermission() {}

    public interface Outcome {
        void granted();
        void denied();
        /** Host lifecycle/dispatch failure, distinct from either source permission outcome. */
        default void unavailable(String code) {}
    }
    @FunctionalInterface interface Launcher { void launch(FragmentActivity host, Intent intent); }

    public static boolean granted() {
        return ContextCompat.checkSelfPermission(ReferenceApplication.app(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void request(FragmentActivity host, Outcome outcome) {
        request(host, outcome, FragmentActivity::startActivity);
    }

    /** The injected launch boundary observes the real explicit Intent; permission checks remain real. */
    static void request(FragmentActivity host, Outcome outcome, Launcher launcher) {
        mainThread();
        if (host == null || launcher == null) throw new IllegalArgumentException("Notification permission host is required");
        onHostStarted(host);
        if (granted()) { if (outcome != null) outcome.granted(); return; }
        if (host.isFinishing() || host.isDestroyed()) throw new IllegalStateException("Notification permission host is closing");
        // A second request must not overwrite the first caller's callback or TopInfo.
        if (!REQUESTS.isEmpty()) throw new IllegalStateException("A notification permission request is already pending");
        String token = UUID.randomUUID().toString();
        OriginalTopInfoDialog tip = OriginalTopInfoDialog.create(TITLE, CONTENT);
        tip.requireArguments().putString(HOST_TOKEN, token);
        Pending pending = new Pending(host, outcome, tip, token);
        REQUESTS.put(token, pending); host.getLifecycle().addObserver(pending.ownerObserver);
        boolean launched = false;
        try {
            tip.show(host.getSupportFragmentManager(), "tip");
            // Make the source explanation an attached Dialog before the host
            // transient Activity requests the real platform permission.
            host.getSupportFragmentManager().executePendingTransactions();
            launcher.launch(host, new Intent(host, OriginalPermissionRequestActivity.class)
                    .putExtra(OriginalPermissionRequestActivity.EXTRA_TOKEN, token)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            launched = true;
        } finally {
            // Source synchronous show/launch exceptions propagate. They are not
            // transformed into a user denial or an asynchronous continuation.
            if (!launched) discard(token, "LAUNCH_FAILED", false);
        }
    }

    static boolean contains(String token) { mainThread(); return token != null && REQUESTS.containsKey(token); }
    static int pendingRequestCount() { mainThread(); return REQUESTS.size(); }
    public static String lastBridgeFailureCode() { return lastBridgeFailure; }

    /** Host-only recovery: saved FragmentManager state cannot resurrect a process-local callback. */
    public static void onHostStarted(FragmentActivity host) {
        mainThread();
        androidx.fragment.app.Fragment fragment = host.getSupportFragmentManager().findFragmentByTag("tip");
        if (!(fragment instanceof OriginalTopInfoDialog tip) || tip.getArguments() == null
                || !tip.getArguments().containsKey(HOST_TOKEN)) return;
        String token = tip.requireArguments().getString(HOST_TOKEN);
        Pending pending = REQUESTS.get(token);
        if (pending != null && pending.owner.get() == host) return;
        // ON_DESTROY can dismiss the old instance after its state was saved;
        // dismiss the separately restored panel as well, never unrelated tips.
        tip.dismissForOutcome();
        if (pending != null) discard(token, "OWNER_STATE_MISSING", true);
        else bridgeFailure("OWNER_STATE_MISSING");
    }

    /** Called after the transient Activity's finish(), with result arrays intentionally unused. */
    static void permissionResult(String token) {
        mainThread(); Pending pending = take(token);
        if (pending == null) { bridgeFailure("REQUEST_STATE_MISSING"); return; }
        boolean dismissed = false;
        try {
            // c0.e -> D/w rechecks actual state instead of trusting result arrays.
            boolean actualGrant = granted();
            pending.tip.dismissForOutcome(); dismissed = true;
            if (pending.outcome != null) {
                if (actualGrant) pending.outcome.granted(); else pending.outcome.denied();
            }
        } finally {
            if (!dismissed) dismiss(pending);
        }
    }

    static void transientDestroyed(String token) { mainThread(); discard(token, "TRANSIENT_DESTROYED", true); }
    static void missingTransientState() { mainThread(); bridgeFailure("REQUEST_STATE_MISSING"); }

    private static Pending take(String token) {
        Pending pending = token == null ? null : REQUESTS.remove(token);
        if (pending != null) {
            FragmentActivity host = pending.owner.get();
            if (host != null) host.getLifecycle().removeObserver(pending.ownerObserver);
        }
        return pending;
    }
    private static void discard(String token, String code, boolean notify) {
        Pending pending = take(token);
        if (pending == null) return;
        bridgeFailure(code); dismiss(pending);
        if (notify && pending.outcome != null) pending.outcome.unavailable(code);
    }
    private static void dismiss(Pending pending) {
        if (pending.tip.isAdded() || pending.tip.getDialog() != null) pending.tip.dismissForOutcome();
    }
    private static void bridgeFailure(String code) { lastBridgeFailure = code; Log.w(LOG_TAG, code); }
    private static void mainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) throw new IllegalStateException("Notification permission requires MAIN");
    }
    private static final class Pending {
        final WeakReference<FragmentActivity> owner;
        final Outcome outcome;
        final OriginalTopInfoDialog tip;
        final LifecycleEventObserver ownerObserver;
        Pending(FragmentActivity host, Outcome outcome, OriginalTopInfoDialog tip, String token) {
            owner = new WeakReference<>(host); this.outcome = outcome; this.tip = tip;
            // A destroyed/recreated caller cannot inherit a stale closure. The
            // new caller must explicitly request again; no grant/denial is fabricated.
            ownerObserver = (source, event) -> {
                if (event == Lifecycle.Event.ON_DESTROY) discard(token, "OWNER_DESTROYED", true);
            };
        }
    }
}
