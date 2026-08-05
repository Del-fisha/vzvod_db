package com.company.vzvod.view.alltodayshifts;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Отличает одиночный клик от двойного: single откладывается, double отменяет pending single.
 */
public final class ClickVersusDoubleClickCoordinator {

    public interface Scheduler {
        void schedule(long delayMs, Runnable task);

        void cancel();
    }

    private final long delayMs;
    private final Scheduler scheduler;
    private final Runnable onSingle;
    private final Runnable onDouble;

    public ClickVersusDoubleClickCoordinator(long delayMs, Runnable onSingle, Runnable onDouble) {
        this(delayMs, new DefaultScheduler(), onSingle, onDouble);
    }

    public ClickVersusDoubleClickCoordinator(
            long delayMs,
            Scheduler scheduler,
            Runnable onSingle,
            Runnable onDouble
    ) {
        this.delayMs = delayMs;
        this.scheduler = Objects.requireNonNull(scheduler);
        this.onSingle = Objects.requireNonNull(onSingle);
        this.onDouble = Objects.requireNonNull(onDouble);
    }

    public void onClick() {
        scheduler.cancel();
        scheduler.schedule(delayMs, onSingle);
    }

    public void onDoubleClick() {
        scheduler.cancel();
        onDouble.run();
    }

    private static final class DefaultScheduler implements Scheduler {
        private static final ScheduledExecutorService EXECUTOR =
                Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "click-vs-dblclick");
                    t.setDaemon(true);
                    return t;
                });

        private final AtomicReference<ScheduledFuture<?>> pending = new AtomicReference<>();

        @Override
        public void schedule(long delayMs, Runnable task) {
            cancel();
            pending.set(EXECUTOR.schedule(task, delayMs, TimeUnit.MILLISECONDS));
        }

        @Override
        public void cancel() {
            ScheduledFuture<?> future = pending.getAndSet(null);
            if (future != null) {
                future.cancel(false);
            }
        }
    }
}
