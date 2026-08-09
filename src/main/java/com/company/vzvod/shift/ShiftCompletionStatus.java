package com.company.vzvod.shift;

import com.company.vzvod.entity.Shift;

import java.time.LocalTime;

/**
 * Статус наряда по {@link Shift#getEndTime()}:
 * нет endTime → смена начата; есть endTime → смена окончена.
 */
public final class ShiftCompletionStatus {

    public enum Kind {
        STARTED,
        FINISHED
    }

    private ShiftCompletionStatus() {
    }

    public static boolean isFinished(LocalTime endTime) {
        return endTime != null;
    }

    public static boolean isFinished(Shift shift) {
        return shift != null && isFinished(shift.getEndTime());
    }

    public static Kind of(LocalTime endTime) {
        return isFinished(endTime) ? Kind.FINISHED : Kind.STARTED;
    }

    public static Kind of(Shift shift) {
        return of(shift == null ? null : shift.getEndTime());
    }

    /** CSS-модификатор кружка: started = зелёный, finished = красный. */
    public static String cssModifier(Kind kind) {
        return kind == Kind.FINISHED ? "finished" : "started";
    }
}
