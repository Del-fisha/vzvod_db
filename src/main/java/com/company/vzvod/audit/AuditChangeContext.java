package com.company.vzvod.audit;

import java.util.function.Supplier;

/**
 * Явная причина программного изменения (джоба, интеграция), если стек недостаточен.
 */
public final class AuditChangeContext {

    private static final ThreadLocal<String> REASON = new ThreadLocal<>();

    private AuditChangeContext() {
    }

    public static void setReason(String reason) {
        REASON.set(reason);
    }

    public static String getReason() {
        return REASON.get();
    }

    public static void clear() {
        REASON.remove();
    }

    public static <T> T runWithReason(String reason, Supplier<T> action) {
        setReason(reason);
        try {
            return action.get();
        } finally {
            clear();
        }
    }

    public static void runWithReason(String reason, Runnable action) {
        runWithReason(reason, () -> {
            action.run();
            return null;
        });
    }
}
