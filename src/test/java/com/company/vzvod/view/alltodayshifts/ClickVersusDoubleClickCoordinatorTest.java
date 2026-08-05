package com.company.vzvod.view.alltodayshifts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Разделение single/double click")
class ClickVersusDoubleClickCoordinatorTest {

    @Test
    @DisplayName("Одиночный клик вызывает onSingle после задержки")
    void singleClick_firesAfterDelay() throws Exception {
        List<String> actions = new ArrayList<>();
        ManualScheduler scheduler = new ManualScheduler();
        ClickVersusDoubleClickCoordinator coordinator = new ClickVersusDoubleClickCoordinator(
                200,
                scheduler,
                () -> actions.add("single"),
                () -> actions.add("double")
        );

        coordinator.onClick();
        assertTrue(actions.isEmpty());
        scheduler.runDue();
        assertEquals(List.of("single"), actions);
    }

    @Test
    @DisplayName("Двойной клик отменяет single и вызывает onDouble")
    void doubleClick_cancelsSingle() {
        List<String> actions = new ArrayList<>();
        ManualScheduler scheduler = new ManualScheduler();
        ClickVersusDoubleClickCoordinator coordinator = new ClickVersusDoubleClickCoordinator(
                200,
                scheduler,
                () -> actions.add("single"),
                () -> actions.add("double")
        );

        coordinator.onClick();
        coordinator.onClick();
        coordinator.onDoubleClick();
        scheduler.runDue();

        assertEquals(List.of("double"), actions);
    }

    private static final class ManualScheduler implements ClickVersusDoubleClickCoordinator.Scheduler {
        private final AtomicReference<Runnable> pending = new AtomicReference<>();

        @Override
        public void schedule(long delayMs, Runnable task) {
            pending.set(task);
        }

        @Override
        public void cancel() {
            pending.set(null);
        }

        void runDue() {
            Runnable task = pending.getAndSet(null);
            if (task != null) {
                task.run();
            }
        }
    }
}
