package de.jakob.game.scheduler;

import de.jakob.game.logger.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class GameScheduler {

    public static volatile boolean debug = true;
    public static volatile boolean debugOnlyOnLag = true;
    public static volatile long debugIntervalTicks = 50L;
    public static volatile long lagThresholdMillis = 25L;
    public static volatile int maxCatchUpTicksPerFrame = 5;

    public static final long targetedTPS = 50;
    private static final long tickDurationNanos = 1_000_000_000L / targetedTPS;

    private final long startTimeMillis = System.currentTimeMillis();
    private final long startTimeNanos = System.nanoTime();

    private volatile double currentTPS = 0;

    private volatile long lastDebugTick = 0;
    private volatile long lastDebugNanos = 0;

    private static final Comparator<ScheduledTask> TASK_ORDER =
            Comparator.comparingLong((ScheduledTask t) -> t.nextRun)
                    .thenComparingLong(t -> t.sequence);

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicLong sequence = new AtomicLong();

    private final Object syncLock = new Object();
    private final Object asyncLock = new Object();

    private final PriorityQueue<ScheduledTask> syncTasks = new PriorityQueue<>(TASK_ORDER);
    private final PriorityQueue<ScheduledTask> asyncTasks = new PriorityQueue<>(TASK_ORDER);

    private final Queue<ScheduledTask> pendingSyncTasks = new ConcurrentLinkedQueue<>();
    private final Queue<ScheduledTask> pendingAsyncTasks = new ConcurrentLinkedQueue<>();

    private final ExecutorService asyncPool = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()),
            daemonFactory("GameScheduler-Async")
    );

    private final ScheduledExecutorService asyncScheduler =
            Executors.newSingleThreadScheduledExecutor(daemonFactory("GameScheduler-Delay"));

    private volatile long tick = 0;
    private volatile long lastNanos = 0;

    private AnimationTimer timer;

    public GameScheduler() {}

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public double getTPS() {
        return currentTPS;
    }

    public void start() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::start);
            return;
        }

        if (!running.get() || !started.compareAndSet(false, true)) return;

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!running.get()) {
                    stop();
                    return;
                }

                if (lastNanos == 0) {
                    lastNanos = now;
                    lastDebugNanos = now;
                    return;
                }

                int advancedTicks = 0;

                while (now - lastNanos >= tickDurationNanos &&
                        advancedTicks < maxCatchUpTicksPerFrame) {

                    lastNanos += tickDurationNanos;
                    tick++;
                    advancedTicks++;

                    processTick();
                }

                updateTPS(now);

                if (debug) {
                    boolean lagging =
                            TimeUnit.NANOSECONDS.toMillis(now - lastNanos) >= lagThresholdMillis;

                    if (debugOnlyOnLag) {
                        if (lagging || advancedTicks >= maxCatchUpTicksPerFrame) {
                            printLagDebug(now, advancedTicks);
                        }
                    } else if (tick - lastDebugTick >= debugIntervalTicks) {
                        printPeriodicDebug(now);
                    }
                }
            }
        };

        timer.start();
    }

    private void updateTPS(long now) {
        long deltaTicks = tick - lastDebugTick;
        long deltaNanos = now - lastDebugNanos;

        if (deltaNanos > 0) {
            currentTPS = (deltaTicks * 1_000_000_000.0) / deltaNanos;
        }
    }

    private void processTick() {
        drainPending(syncTasks, pendingSyncTasks, syncLock);
        drainPending(asyncTasks, pendingAsyncTasks, asyncLock);

        processQueue(syncTasks, syncLock, false);
        processQueue(asyncTasks, asyncLock, true);
    }

    private void drainPending(PriorityQueue<ScheduledTask> queue,
                              Queue<ScheduledTask> pending,
                              Object lock) {
        synchronized (lock) {
            ScheduledTask task;
            while ((task = pending.poll()) != null) {
                queue.offer(task);
            }
        }
    }

    private void processQueue(PriorityQueue<ScheduledTask> queue,
                              Object lock,
                              boolean async) {

        while (true) {
            ScheduledTask task;

            synchronized (lock) {
                task = queue.peek();
                if (task == null || task.nextRun > tick) return;
                queue.poll();
            }

            if (task.cancelled.get() || !running.get()) continue;

            if (async) {
                try {
                    asyncPool.execute(task.runnable);
                } catch (RejectedExecutionException ignored) {}
            } else {
                task.runnable.run();
            }

            if (task.period > 0 && !task.cancelled.get()) {
                task.nextRun += task.period;
                synchronized (lock) {
                    queue.offer(task);
                }
            }
        }
    }

    public void runLater(Runnable runnable, long delayTicks) {
        Objects.requireNonNull(runnable);
        enqueueSync(new ScheduledTask(
                runnable,
                tick + Math.max(0, delayTicks),
                -1,
                false,
                sequence.getAndIncrement()
        ));
    }

    public ScheduledTask runRepeating(Runnable runnable, long delay, long period) {
        ScheduledTask task = new ScheduledTask(
                runnable,
                tick + Math.max(0, delay),
                Math.max(1, period),
                false,
                sequence.getAndIncrement()
        );
        enqueueSync(task);
        return task;
    }

    public void runAsync(Runnable runnable) {
        asyncPool.execute(runnable);
    }

    public void runNextTick(Runnable runnable) {
        runLater(runnable, 1);
    }

    public void shutdown() {
        if (!running.compareAndSet(true, false)) return;

        if (Platform.isFxApplicationThread()) {
            if (timer != null) timer.stop();
        } else {
            Platform.runLater(() -> {
                if (timer != null) timer.stop();
            });
        }

        asyncScheduler.shutdownNow();
        asyncPool.shutdownNow();
    }

    private void enqueueSync(ScheduledTask task) {
        pendingSyncTasks.add(task);
    }

    private void enqueueAsync(ScheduledTask task) {
        pendingAsyncTasks.add(task);
    }

    private void printPeriodicDebug(long now) {
        Logger.info("[Scheduler] Tick=" + tick +
                " TPS=" + String.format("%.2f", currentTPS));
        lastDebugTick = tick;
        lastDebugNanos = now;
    }

    private void printLagDebug(long now, int advancedTicks) {
        long lagMs = TimeUnit.NANOSECONDS.toMillis(now - lastNanos);

        Logger.warn("[Scheduler Lag] Tick=" + tick +
                " Advanced=" + advancedTicks +
                " Lag=" + lagMs + "ms");

        lastDebugTick = tick;
        lastDebugNanos = now;
    }

    private static ThreadFactory daemonFactory(String name) {
        return r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        };
    }

    public static class ScheduledTask {
        private final Runnable runnable;
        private volatile long nextRun;
        private final long period;
        private final boolean async;
        private final long sequence;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        public ScheduledTask(Runnable runnable, long nextRun,
                             long period, boolean async, long sequence) {
            this.runnable = runnable;
            this.nextRun = nextRun;
            this.period = period;
            this.async = async;
            this.sequence = sequence;
        }

        public void cancel() {
            cancelled.set(true);
        }
    }
}