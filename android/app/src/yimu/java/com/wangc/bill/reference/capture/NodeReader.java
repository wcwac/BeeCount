package com.wangc.bill.reference.capture;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** A bounded, ordered traversal. The caller's root is borrowed, never released. */
public final class NodeReader {
    public static final Limits DEFAULT_LIMITS = new Limits(2048, 512, 64, 2000);

    public interface Adapter<N> {
        int childCount(N node);
        N child(N node, int index);
        CharSequence text(N node);
        CharSequence description(N node);
        boolean same(N first, N second);
        void release(N node);
    }
    public interface Clock { long now(); }
    public interface Observer {
        void started();
        void progress(int visited);
        void finished(int texts, int branches);
        void aborted();
    }
    public static final class Limits {
        public final int nodes, texts, depth;
        public final long milliseconds;
        public Limits(int nodes, int texts, int depth, long milliseconds) {
            if (nodes < 1 || texts < 1 || depth < 0 || milliseconds < 1) throw new IllegalArgumentException("Invalid node limits");
            this.nodes = nodes; this.texts = texts; this.depth = depth; this.milliseconds = milliseconds;
        }
    }
    public enum Reason { NODE_LIMIT, TEXT_LIMIT, DEPTH_LIMIT, TIME_LIMIT, CYCLE, INTERRUPTED }
    /** Reject the whole tree; a truncated list must never reach a bill parser. */
    public static final class Aborted extends RuntimeException {
        public final Reason reason;
        public Aborted(Reason reason) { super(reason.name()); this.reason = reason; }
    }
    public static final class Result {
        public final List<String> texts;
        public final int visited, branches;
        private Result(List<String> texts, int visited, int branches) { this.texts = texts; this.visited = visited; this.branches = branches; }
    }
    private static final class Frame<N> {
        final N node; final int children, depth; final boolean owned;
        int next;
        Frame(N node, int children, int depth, boolean owned) { this.node=node; this.children=children; this.depth=depth; this.owned=owned; }
    }
    private static final class Budget {
        final Limits limits; final Clock clock; final Observer observer; final long started;
        int visited, branches;
        Budget(Limits limits, Clock clock, Observer observer) { this.limits=limits; this.clock=clock; this.observer=observer; started=clock.now(); }
        void check() {
            if (Thread.currentThread().isInterrupted()) throw new Aborted(Reason.INTERRUPTED);
            if (clock.now()-started >= limits.milliseconds) throw new Aborted(Reason.TIME_LIMIT);
        }
        void visit(int depth) {
            check();
            if (depth > limits.depth) throw new Aborted(Reason.DEPTH_LIMIT);
            if (visited >= limits.nodes) throw new Aborted(Reason.NODE_LIMIT);
            // Count child lookups too: a provider returning null repeatedly is bounded.
            visited++; observer.progress(visited); check();
        }
    }
    private NodeReader() {}

    public static <N> Result read(N root, boolean descriptions, Adapter<N> adapter, Clock clock, Observer observer) {
        return read(root, descriptions, adapter, clock, observer, DEFAULT_LIMITS);
    }
    public static <N> Result read(N root, boolean descriptions, Adapter<N> adapter, Clock clock, Observer observer, Limits limits) {
        ArrayDeque<Frame<N>> stack = new ArrayDeque<>();
        List<String> texts = new ArrayList<>();
        Budget budget = new Budget(limits, clock, observer);
        observer.started();
        try {
            if (root != null) {
                budget.visit(0);
                int children = count(root, adapter, budget);
                if (children > 0) budget.branches++;
                // Original readers only inspect descendants, not the source root's text.
                stack.push(new Frame<>(root, children, 0, false));
            }
            while (!stack.isEmpty()) {
                budget.check();
                Frame<N> parent = stack.peek();
                if (parent.next >= parent.children) {
                    stack.pop();
                    if (parent.owned) release(parent.node, adapter);
                    budget.check();
                    continue;
                }
                int depth = parent.depth + 1;
                budget.visit(depth);
                N child = adapter.child(parent.node, parent.next++);
                // Own returned nodes immediately, including calls exceeding the deadline.
                boolean owned = child != null && !borrowed(child, root, stack);
                boolean transferred = false;
                try {
                    budget.check();
                    if (child == null) continue;
                    for (Frame<N> ancestor : stack) {
                        budget.check();
                        boolean cycle = adapter.same(child, ancestor.node);
                        budget.check();
                        if (cycle) throw new Aborted(Reason.CYCLE);
                    }
                    int children = count(child, adapter, budget);
                    if (children > 0) budget.branches++;
                    else append(child, descriptions, texts, adapter, budget);
                    stack.push(new Frame<>(child, children, depth, owned));
                    transferred = true;
                } finally {
                    if (owned && !transferred) release(child, adapter);
                }
            }
            budget.check();
            observer.finished(texts.size(), budget.branches);
            return new Result(texts, budget.visited, budget.branches);
        } catch (RuntimeException error) {
            observer.aborted();
            throw error;
        } finally {
            // Cleanup also runs on provider failures; never release the caller's root.
            while (!stack.isEmpty()) {
                Frame<N> frame = stack.pop();
                if (frame.owned) release(frame.node, adapter);
            }
        }
    }
    private static <N> boolean borrowed(N node, N root, ArrayDeque<Frame<N>> stack) {
        if (node == root) return true;
        for (Frame<N> frame : stack) if (node == frame.node) return true;
        return false;
    }
    private static <N> int count(N node, Adapter<N> adapter, Budget budget) {
        budget.check(); int count=adapter.childCount(node); budget.check();
        if (count < 0) throw new IllegalStateException("Invalid accessibility child count");
        return count;
    }
    private static <N> void append(N node, boolean descriptions, List<String> texts, Adapter<N> adapter, Budget budget) {
        budget.check(); CharSequence value=adapter.text(node); budget.check();
        if ((value == null || value.length() == 0) && descriptions) {
            budget.check(); value=adapter.description(node); budget.check();
        }
        if (value == null || value.length() == 0) return;
        if (texts.size() >= budget.limits.texts) throw new Aborted(Reason.TEXT_LIMIT);
        budget.check(); String text=value.toString(); budget.check();
        texts.add(text); budget.check();
    }
    private static <N> void release(N node, Adapter<N> adapter) {
        try { adapter.release(node); } catch (RuntimeException ignored) { /* Preserve the traversal failure. */ }
    }
}
