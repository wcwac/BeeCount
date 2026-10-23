package com.wangc.bill.reference.capture;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Bounds redundant accessibility events on the existing, shared serial worker. */
public final class EventBacklog {
    private EventBacklog() {}

    public enum Rejection { NONE, QUEUE_FULL, SHUTDOWN }

    public static final class Result {
        public final boolean accepted;
        /** Older content events replaced within the queue's final uninterrupted content run. */
        public final int coalesced;
        /** Pending content events removed to make room for a window-state event. */
        public final int evicted;
        public final Rejection rejection;

        private Result(boolean accepted, int coalesced, int evicted, Rejection rejection) {
            this.accepted = accepted;
            this.coalesced = coalesced;
            this.evicted = evicted;
            this.rejection = rejection;
        }
    }

    /**
     * Owns its event's resources after construction. Submit each instance once, through enqueue.
     * The resource is closed once, whether processed, replaced, rejected, or explicitly discarded.
     */
    public abstract static class EventTask implements Runnable {
        public final String packageName;
        public final boolean contentEvent;
        private final AtomicBoolean submitted = new AtomicBoolean();
        private final AtomicBoolean closed = new AtomicBoolean();
        // 0: pending, 1: running, 2: discarded, 3: completed.
        private final AtomicInteger state = new AtomicInteger();

        protected EventTask(String packageName, boolean contentEvent) {
            this.packageName = Objects.requireNonNull(packageName, "packageName");
            this.contentEvent = contentEvent;
        }

        protected abstract void process();

        /** Must be quick and must not throw; for the service this only recycles its own node. */
        protected abstract void closeResource();

        @Override public final void run() {
            if (!state.compareAndSet(0, 1)) return;
            try { process(); }
            finally {
                state.set(3);
                closeOnce();
            }
        }

        /** A running task retains ownership and releases its resource when it returns. */
        public final boolean discard() {
            if (!state.compareAndSet(0, 2)) return false;
            closeOnce();
            return true;
        }

        private void closeOnce() {
            if (closed.compareAndSet(false, true)) closeResource();
        }
    }

    /**
     * Uses the same executor as the recovered handlers, dismiss callbacks and enrichment tasks.
     * Only queued EventTask instances can be removed; running tasks and ordinary worker tasks
     * are never cancelled. The executor must use AbortPolicy so acceptance is observable.
     */
    public static Result enqueue(ThreadPoolExecutor worker, EventTask task) {
        Objects.requireNonNull(worker, "worker");
        Objects.requireNonNull(task, "task");
        if (!(worker.getRejectedExecutionHandler() instanceof ThreadPoolExecutor.AbortPolicy)) {
            throw new IllegalArgumentException("Event worker must use AbortPolicy");
        }
        if (!task.submitted.compareAndSet(false, true)) {
            throw new IllegalArgumentException("An event task can only be submitted once");
        }
        // This serializes event producers, but does not lock the worker or change its ordering.
        // Non-event producers may still fill a freed slot, so execute's rejection is handled.
        synchronized (worker) {
            if (worker.isShutdown()) return reject(task, 0, 0, Rejection.SHUTDOWN);
            int coalesced = 0;
            if (task.contentEvent) {
                Object[] queuedTasks = worker.getQueue().toArray();
                for (int i = queuedTasks.length - 1; i >= 0; i--) {
                    // Checkout information before a state change may be needed by its parser.
                    // Dismiss/enrichment callbacks also delimit distinct handler state.
                    if (!(queuedTasks[i] instanceof EventTask pending) || !pending.contentEvent) break;
                    if (pending.packageName.equals(task.packageName) && worker.remove(pending)) {
                        if (pending.discard()) coalesced++;
                    }
                }
            }
            int evicted = 0;
            // Bound retries even if unrelated worker producers repeatedly claim a freed slot.
            int evictionLimit = worker.getQueue().size();
            while (true) {
                try {
                    worker.execute(task);
                    return new Result(true, coalesced, evicted, Rejection.NONE);
                } catch (RejectedExecutionException rejected) {
                    if (worker.isShutdown()) return reject(task, coalesced, evicted, Rejection.SHUTDOWN);
                    if (!task.contentEvent && evicted < evictionLimit && evictContent(worker)) {
                        evicted++;
                        continue;
                    }
                    return reject(task, coalesced, evicted, Rejection.QUEUE_FULL);
                }
            }
        }
    }

    private static boolean evictContent(ThreadPoolExecutor worker) {
        Object[] queuedTasks = worker.getQueue().toArray();
        int firstContent = queuedTasks.length;
        for (int i = queuedTasks.length - 1; i >= 0; i--) {
            if (!(queuedTasks[i] instanceof EventTask pending) || !pending.contentEvent) break;
            firstContent = i;
        }
        for (int i = firstContent; i < queuedTasks.length; i++) {
            EventTask pending = (EventTask) queuedTasks[i];
            if (worker.remove(pending)) {
                pending.discard();
                return true;
            }
        }
        return false;
    }

    private static Result reject(EventTask task, int coalesced, int evicted, Rejection reason) {
        task.discard();
        return new Result(false, coalesced, evicted, reason);
    }
}
