package com.wangc.bill.reference;

import com.wangc.bill.reference.capture.EventBacklog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public final class EventBacklogTest {
    private static final class Task extends EventBacklog.EventTask {
        final String label;
        final List<String> observed;
        final AtomicInteger processed = new AtomicInteger();
        final AtomicInteger closed = new AtomicInteger();
        CountDownLatch entered;
        CountDownLatch release;

        Task(String pkg, boolean content, String label, List<String> observed) {
            super(pkg, content);
            this.label = label;
            this.observed = observed;
        }

        @Override protected void process() {
            processed.incrementAndGet();
            observed.add(label);
            if (entered != null) entered.countDown();
            if (release != null) awaitWorker(release);
        }

        @Override protected void closeResource() { closed.incrementAndGet(); }
    }

    private static ThreadPoolExecutor worker(int capacity) {
        return new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(capacity), new ThreadPoolExecutor.AbortPolicy());
    }

    private static void awaitWorker(CountDownLatch latch) {
        try { latch.await(); }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); }
    }

    private static void block(ThreadPoolExecutor worker, CountDownLatch release) throws InterruptedException {
        CountDownLatch entered = new CountDownLatch(1);
        worker.execute(() -> { entered.countDown(); awaitWorker(release); });
        assertTrue("Worker did not start", entered.await(3, TimeUnit.SECONDS));
    }

    private static void drain(ThreadPoolExecutor worker, CountDownLatch release) throws InterruptedException {
        release.countDown();
        worker.shutdown();
        assertTrue("Worker did not drain", worker.awaitTermination(3, TimeUnit.SECONDS));
    }

    private static void cleanup(ThreadPoolExecutor worker, CountDownLatch release) throws InterruptedException {
        release.countDown();
        for (Runnable queued : worker.shutdownNow()) {
            if (queued instanceof EventBacklog.EventTask event) event.discard();
        }
        assertTrue("Worker did not terminate", worker.awaitTermination(3, TimeUnit.SECONDS));
    }

    @Test(timeout = 10000) public void aContentBurstProcessesOnlyTheNewestPendingNode() throws Exception {
        ThreadPoolExecutor worker = worker(3);
        CountDownLatch release = new CountDownLatch(1);
        List<String> observed = Collections.synchronizedList(new ArrayList<>());
        List<Task> tasks = new ArrayList<>();
        try {
            block(worker, release);
            int coalesced = 0;
            for (int i = 0; i < 500; i++) {
                Task task = new Task("payment", true, "content-" + i, observed);
                tasks.add(task);
                EventBacklog.Result result = EventBacklog.enqueue(worker, task);
                assertTrue(result.accepted);
                assertEquals(EventBacklog.Rejection.NONE, result.rejection);
                coalesced += result.coalesced;
            }
            assertEquals(499, coalesced);
            assertEquals(1, worker.getQueue().size());
            drain(worker, release);
            assertEquals(Collections.singletonList("content-499"), observed);
            for (int i = 0; i < tasks.size(); i++) {
                assertEquals(i == 499 ? 1 : 0, tasks.get(i).processed.get());
                assertEquals(1, tasks.get(i).closed.get());
                assertFalse(tasks.get(i).discard());
            }
        } finally { cleanup(worker, release); }
    }

    @Test(timeout = 10000) public void coalescingPreservesStateOrderAndOtherPackages() throws Exception {
        ThreadPoolExecutor worker = worker(5);
        CountDownLatch release = new CountDownLatch(1);
        List<String> observed = Collections.synchronizedList(new ArrayList<>());
        Task state1 = new Task("a", false, "state-1", observed);
        Task old = new Task("a", true, "old-a", observed);
        Task other = new Task("b", true, "content-b", observed);
        Task state2 = new Task("a", false, "state-2", observed);
        Task latest = new Task("a", true, "latest-a", observed);
        try {
            block(worker, release);
            for (Task task : Arrays.asList(state1, state2, old, other)) assertTrue(EventBacklog.enqueue(worker, task).accepted);
            EventBacklog.Result result = EventBacklog.enqueue(worker, latest);
            assertEquals(1, result.coalesced);
            assertEquals(0, result.evicted);
            drain(worker, release);
            assertEquals(Arrays.asList("state-1", "state-2", "content-b", "latest-a"), observed);
            for (Task task : Arrays.asList(state1, old, other, state2, latest)) assertEquals(1, task.closed.get());
        } finally { cleanup(worker, release); }
    }

    @Test(timeout = 10000) public void stateEventEvictsPendingContentWhenTheQueueIsFull() throws Exception {
        ThreadPoolExecutor worker = worker(3);
        CountDownLatch release = new CountDownLatch(1);
        List<String> observed = Collections.synchronizedList(new ArrayList<>());
        Task oldest = new Task("a", true, "oldest-content", observed);
        Task other = new Task("b", true, "other-content", observed);
        Task state1 = new Task("a", false, "state-1", observed);
        Task state2 = new Task("a", false, "state-2", observed);
        try {
            block(worker, release);
            for (Task task : Arrays.asList(state1, oldest, other)) assertTrue(EventBacklog.enqueue(worker, task).accepted);
            EventBacklog.Result result = EventBacklog.enqueue(worker, state2);
            assertTrue(result.accepted);
            assertEquals(1, result.evicted);
            assertEquals(0, result.coalesced);
            assertEquals(EventBacklog.Rejection.NONE, result.rejection);
            assertEquals(1, oldest.closed.get());
            drain(worker, release);
            assertEquals(Arrays.asList("state-1", "other-content", "state-2"), observed);
            for (Task task : Arrays.asList(oldest, other, state1, state2)) assertEquals(1, task.closed.get());
        } finally { cleanup(worker, release); }
    }

    @Test(timeout = 10000) public void fullStateQueueRejectsSafelyWithoutDroppingStateOrKernelTasks() throws Exception {
        ThreadPoolExecutor worker = worker(3);
        CountDownLatch release = new CountDownLatch(1);
        List<String> observed = Collections.synchronizedList(new ArrayList<>());
        Task checkout = new Task("a", true, "checkout", observed);
        Task state1 = new Task("a", false, "state-1", observed);
        Task rejected = new Task("a", false, "rejected-state", observed);
        Task content = new Task("a", true, "rejected-content", observed);
        try {
            block(worker, release);
            assertTrue(EventBacklog.enqueue(worker, checkout).accepted);
            assertTrue(EventBacklog.enqueue(worker, state1).accepted);
            worker.execute(() -> observed.add("kernel-dismiss"));
            for (Task task : Arrays.asList(rejected, content)) {
                EventBacklog.Result result = EventBacklog.enqueue(worker, task);
                assertFalse(result.accepted);
                assertEquals(EventBacklog.Rejection.QUEUE_FULL, result.rejection);
                assertEquals(0, result.coalesced);
                assertEquals(0, result.evicted);
                assertEquals(1, task.closed.get());
            }
            drain(worker, release);
            assertEquals(Arrays.asList("checkout", "state-1", "kernel-dismiss"), observed);
            assertEquals(1, checkout.closed.get());
            assertEquals(1, state1.closed.get());
        } finally { cleanup(worker, release); }
    }

    @Test(timeout = 10000) public void stateBoundaryPreservesTheCheckoutContentItMayNeed() throws Exception {
        ThreadPoolExecutor worker = worker(3);
        CountDownLatch release = new CountDownLatch(1);
        List<String> observed = Collections.synchronizedList(new ArrayList<>());
        Task checkout = new Task("a", true, "checkout", observed);
        Task success = new Task("a", false, "success", observed);
        Task result = new Task("a", true, "result", observed);
        try {
            block(worker, release);
            assertTrue(EventBacklog.enqueue(worker, checkout).accepted);
            assertTrue(EventBacklog.enqueue(worker, success).accepted);
            EventBacklog.Result queued = EventBacklog.enqueue(worker, result);
            assertTrue(queued.accepted);
            assertEquals(0, queued.coalesced);
            drain(worker, release);
            assertEquals(Arrays.asList("checkout", "success", "result"), observed);
            for (Task task : Arrays.asList(checkout, success, result)) assertEquals(1, task.closed.get());
        } finally { cleanup(worker, release); }
    }

    @Test(timeout = 10000) public void kernelCallbackIsAlsoABoundaryBetweenContentEvents() throws Exception {
        ThreadPoolExecutor worker = worker(3);
        CountDownLatch release = new CountDownLatch(1);
        List<String> observed = Collections.synchronizedList(new ArrayList<>());
        Task before = new Task("a", true, "before-dismiss", observed);
        Task after = new Task("a", true, "after-dismiss", observed);
        try {
            block(worker, release);
            assertTrue(EventBacklog.enqueue(worker, before).accepted);
            worker.execute(() -> observed.add("kernel-dismiss"));
            EventBacklog.Result queued = EventBacklog.enqueue(worker, after);
            assertTrue(queued.accepted);
            assertEquals(0, queued.coalesced);
            drain(worker, release);
            assertEquals(Arrays.asList("before-dismiss", "kernel-dismiss", "after-dismiss"), observed);
            assertEquals(1, before.closed.get());
            assertEquals(1, after.closed.get());
        } finally { cleanup(worker, release); }
    }

    @Test(timeout = 10000) public void replacingContentNeverDiscardsTheAlreadyRunningEvent() throws Exception {
        ThreadPoolExecutor worker = worker(2);
        CountDownLatch release = new CountDownLatch(1);
        List<String> observed = Collections.synchronizedList(new ArrayList<>());
        Task running = new Task("a", true, "running", observed);
        running.entered = new CountDownLatch(1);
        running.release = release;
        Task pending = new Task("a", true, "pending", observed);
        Task newest = new Task("a", true, "newest", observed);
        try {
            assertTrue(EventBacklog.enqueue(worker, running).accepted);
            assertTrue(running.entered.await(3, TimeUnit.SECONDS));
            assertFalse(running.discard());
            assertEquals(0, running.closed.get());
            assertTrue(EventBacklog.enqueue(worker, pending).accepted);
            assertEquals(1, EventBacklog.enqueue(worker, newest).coalesced);
            drain(worker, release);
            assertEquals(Arrays.asList("running", "newest"), observed);
            for (Task task : Arrays.asList(running, pending, newest)) assertEquals(1, task.closed.get());
        } finally { cleanup(worker, release); }
    }

    @Test(timeout = 10000) public void shutdownReleasesTheRejectedNodeOnce() throws Exception {
        ThreadPoolExecutor worker = worker(2);
        CountDownLatch release = new CountDownLatch(1);
        List<String> observed = Collections.synchronizedList(new ArrayList<>());
        Task task = new Task("a", true, "rejected", observed);
        try {
            worker.shutdown();
            EventBacklog.Result result = EventBacklog.enqueue(worker, task);
            assertFalse(result.accepted);
            assertEquals(EventBacklog.Rejection.SHUTDOWN, result.rejection);
            assertEquals(1, task.closed.get());
            assertFalse(task.discard());
            task.run();
            assertEquals(1, task.closed.get());
            assertTrue(observed.isEmpty());
        } finally { cleanup(worker, release); }
    }

    @Test(timeout = 10000) public void shutdownBetweenAdmissionAndExecuteIsHandledAsARejection() throws Exception {
        ThreadPoolExecutor worker = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2), new ThreadPoolExecutor.AbortPolicy()) {
                @Override public void execute(Runnable task) {
                    // Deterministically produce the race after enqueue's initial isShutdown check.
                    shutdown();
                    super.execute(task);
                }
            };
        CountDownLatch release = new CountDownLatch(1);
        List<String> observed = Collections.synchronizedList(new ArrayList<>());
        Task task = new Task("a", false, "rejected", observed);
        try {
            EventBacklog.Result result = EventBacklog.enqueue(worker, task);
            assertFalse(result.accepted);
            assertEquals(EventBacklog.Rejection.SHUTDOWN, result.rejection);
            assertEquals(1, task.closed.get());
            assertFalse(task.discard());
            assertTrue(observed.isEmpty());
        } finally { cleanup(worker, release); }
    }

    @Test(timeout = 10000) public void concurrentEventProducersStillKeepOnlyOnePendingContentTask() throws Exception {
        ThreadPoolExecutor worker = worker(2);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(4);
        List<String> observed = Collections.synchronizedList(new ArrayList<>());
        List<Task> tasks = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger coalesced = new AtomicInteger();
        try {
            block(worker, release);
            for (int producer = 0; producer < 4; producer++) {
                final int id = producer;
                new Thread(() -> {
                    awaitWorker(start);
                    try {
                        for (int i = 0; i < 100; i++) {
                            Task task = new Task("a", true, id + ":" + i, observed);
                            tasks.add(task);
                            EventBacklog.Result result = EventBacklog.enqueue(worker, task);
                            if (result.accepted) accepted.incrementAndGet();
                            coalesced.addAndGet(result.coalesced);
                        }
                    } finally { done.countDown(); }
                }, "content-producer-" + producer).start();
            }
            start.countDown();
            assertTrue(done.await(3, TimeUnit.SECONDS));
            assertEquals(400, accepted.get());
            assertEquals(399, coalesced.get());
            assertEquals(1, worker.getQueue().size());
            drain(worker, release);
            assertEquals(1, observed.size());
            for (Task task : tasks) assertEquals(1, task.closed.get());
        } finally { start.countDown(); cleanup(worker, release); }
    }
}
