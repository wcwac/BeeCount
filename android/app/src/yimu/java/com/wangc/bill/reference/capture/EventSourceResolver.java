package com.wangc.bill.reference.capture;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Resolves only a selected event's own application window; it never visits node children. */
public final class EventSourceResolver {
    public static final long MAX_LOOKUP_MS = 2000;
    public static final long MAX_FALLBACK_AGE_MS = 2500;
    public static final int MAX_WINDOW_CANDIDATES = 8;

    public interface Clock { long now(); }
    public interface Provider<N, W> {
        N activeRoot();
        List<W> windows();
        boolean isApplication(W window);
        int windowId(W window);
        boolean isActive(W window);
        boolean isFocused(W window);
        N windowRoot(W window);
        String packageName(N node);
        int nodeWindowId(N node);
        void releaseNode(N node);
        void releaseWindow(W window);
    }
    public enum Route { EVENT_SOURCE, ACTIVE_ROOT, INTERACTIVE_WINDOW, NONE }
    public enum Reason { RESOLVED, UNAVAILABLE, EXPIRED, DEADLINE, PACKAGE_MISMATCH, WINDOW_MISMATCH, LOOKUP_FAILED }
    public static final class Result<N> {
        /** Ownership transfers to the caller. The resolver releases all other acquired objects. */
        public final N node;
        public final Route route;
        public final Reason reason;
        public final int lookups, candidateWindows, errors;
        /** Only the exception's class, never its message or any page content. */
        public final String lastErrorType;
        private Result(N node, Route route, Reason reason, int lookups, int candidateWindows, int errors, String lastErrorType) {
            this.node=node; this.route=route; this.reason=reason; this.lookups=lookups;
            this.candidateWindows=candidateWindows; this.errors=errors; this.lastErrorType=lastErrorType;
        }
    }
    private EventSourceResolver() {}

    /**
     * Takes ownership of initialNode, including rejected or expired inputs. A valid event source
     * needs no new window lookup. A fallback is allowed only while this event is still fresh.
     */
    public static <N, W> Result<N> resolve(N initialNode, String expectedPackage, int eventWindowId,
            long eventAgeMs, Provider<N, W> provider, Clock clock) {
        return resolve(initialNode,expectedPackage,eventWindowId,eventAgeMs,provider,clock,false);
    }
    /** The comparison mode prefers a fresh active root, while retaining a valid event source. */
    public static <N, W> Result<N> resolve(N initialNode, String expectedPackage, int eventWindowId,
            long eventAgeMs, Provider<N, W> provider, Clock clock, boolean preferActive) {
        Objects.requireNonNull(expectedPackage, "expectedPackage");
        if (expectedPackage.isEmpty()) throw new IllegalArgumentException("Empty event package");
        State<N, W> state=new State<>(expectedPackage,eventWindowId,eventAgeMs,provider,clock);
        state.ownNode(initialNode);
        boolean deliver=false;
        try {
            try { resolveOwned(initialNode,eventWindowId,eventAgeMs,provider,preferActive,state); }
            catch (RuntimeException error) { state.failure(error); }
            deliver=true;
        } finally { state.finish(deliver); }
        // Cleanup precedes the immutable result so release failures are included in its metadata.
        return state.result();
    }
    private static <N, W> void resolveOwned(N initialNode,int eventWindowId,long eventAgeMs,
            Provider<N,W> provider,boolean preferActive,State<N,W> state) {
        if (initialNode != null) {
            if (state.accept(initialNode,false)) {
                if (!preferActive) { state.select(initialNode,Route.EVENT_SOURCE); return; }
                state.retainedSource=initialNode;
            }
        }
        if (eventAgeMs > MAX_FALLBACK_AGE_MS) { state.reason=Reason.EXPIRED; return; }
        try {
            if (!state.withinBudget()) return;
            state.lookups++;
            N active=provider.activeRoot();
            state.ownNode(active);
            if (state.withinBudget() && active != null && state.accept(active,true)) {
                state.select(active,Route.ACTIVE_ROOT);
            }
        } catch (RuntimeException error) { state.failure(error); }
        if (state.selected != null || state.retainedSource != null) return;
        if (!state.withinBudget()) return;

        try {
            state.lookups++;
            List<W> windows=provider.windows();
            state.ownWindows(windows);
            if (!state.withinBudget() || windows == null) return;
            for (W window : windows) {
                if (!state.withinBudget() || state.candidateWindows >= MAX_WINDOW_CANDIDATES) break;
                if (window == null) continue;
                try {
                    boolean application=provider.isApplication(window);
                    if (!state.withinBudget()) break;
                    if (!application) continue;
                    if (eventWindowId >= 0) {
                        int windowId=provider.windowId(window);
                        if (!state.withinBudget()) break;
                        if (windowId != eventWindowId) continue;
                    } else {
                        boolean foreground=provider.isActive(window);
                        if (!state.withinBudget()) break;
                        if (!foreground) foreground=provider.isFocused(window);
                        if (!state.withinBudget()) break;
                        if (!foreground) continue;
                    }
                    state.candidateWindows++;
                    state.lookups++;
                    N root=provider.windowRoot(window);
                    state.ownNode(root);
                    if (!state.withinBudget()) break;
                    if (root != null && state.accept(root,true)) {
                        state.select(root,Route.INTERACTIVE_WINDOW);
                        break;
                    }
                } catch (RuntimeException error) { state.failure(error); }
            }
        } catch (RuntimeException error) { state.failure(error); }
    }

    private static final class State<N, W> {
        final String expectedPackage;
        final int eventWindowId;
        final Provider<N, W> provider;
        final Clock clock;
        final long started, eventAgeMs;
        final Set<N> ownedNodes=Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<W> ownedWindows=Collections.newSetFromMap(new IdentityHashMap<>());
        N selected, retainedSource;
        Route route=Route.NONE;
        Reason reason=Reason.UNAVAILABLE;
        int lookups, candidateWindows, errors;
        String lastErrorType="";
        State(String expectedPackage,int eventWindowId,long eventAgeMs,Provider<N,W> provider,Clock clock) {
            this.expectedPackage=expectedPackage; this.eventWindowId=eventWindowId;
            this.eventAgeMs=Math.max(0,eventAgeMs);
            this.provider=Objects.requireNonNull(provider,"provider"); this.clock=Objects.requireNonNull(clock,"clock");
            started=clock.now();
        }
        boolean withinBudget() {
            long elapsed=clock.now()-started;
            if (elapsed >= MAX_LOOKUP_MS) { reason=Reason.DEADLINE; return false; }
            if (eventAgeMs > MAX_FALLBACK_AGE_MS-elapsed) { reason=Reason.EXPIRED; return false; }
            return true;
        }
        boolean accept(N node,boolean bounded) {
            try {
                String name=provider.packageName(node);
                if (bounded && !withinBudget()) return false;
                if (!expectedPackage.equals(name)) { reason=Reason.PACKAGE_MISMATCH; return false; }
                if (eventWindowId >= 0) {
                    int id=provider.nodeWindowId(node);
                    if (bounded && !withinBudget()) return false;
                    if (id != eventWindowId) { reason=Reason.WINDOW_MISMATCH; return false; }
                }
                return true;
            } catch (RuntimeException error) { failure(error); return false; }
        }
        void failure(RuntimeException error) {
            errors++; lastErrorType=error.getClass().getName();
            if (selected == null && reason != Reason.DEADLINE && reason != Reason.EXPIRED) reason=Reason.LOOKUP_FAILED;
        }
        void ownNode(N node) { if (node != null) ownedNodes.add(node); }
        void ownWindows(List<W> windows) {
            // getWindows returns owned metadata even when its root will never be requested.
            if (windows != null) for (W window : windows) if (window != null) ownedWindows.add(window);
        }
        void select(N node,Route route) { selected=node; this.route=route; reason=Reason.RESOLVED; }
        void finish(boolean deliver) {
            // A delayed window lookup cannot invalidate the event source already owned and checked.
            if (deliver && selected == null && retainedSource != null) select(retainedSource,Route.EVENT_SOURCE);
            if (!deliver) { selected=null; route=Route.NONE; }
            retainedSource=null;
            // On API 24–32 recycle returns nodes to a process-wide pool. Holding every candidate
            // until all root RPCs finish prevents a later root reusing an already rejected object.
            for (N node : ownedNodes) if (node != selected) {
                try { provider.releaseNode(node); } catch (RuntimeException error) { failure(error); }
            }
            ownedNodes.clear();
            for (W window : ownedWindows) {
                try { provider.releaseWindow(window); } catch (RuntimeException error) { failure(error); }
            }
            ownedWindows.clear();
        }
        Result<N> result() { return new Result<>(selected,route,reason,lookups,candidateWindows,errors,lastErrorType); }
    }
}
