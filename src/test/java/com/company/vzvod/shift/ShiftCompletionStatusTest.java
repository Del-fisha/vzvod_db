package com.company.vzvod.shift;

import com.company.vzvod.entity.Shift;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShiftCompletionStatus")
class ShiftCompletionStatusTest {

    @Test
    void withoutEndTime_isStarted_greenModifier() {
        assertFalse(ShiftCompletionStatus.isFinished((LocalTime) null));
        assertEquals(ShiftCompletionStatus.Kind.STARTED, ShiftCompletionStatus.of((LocalTime) null));
        assertEquals("started", ShiftCompletionStatus.cssModifier(ShiftCompletionStatus.Kind.STARTED));
    }

    @Test
    void withEndTime_isFinished_redModifier() {
        LocalTime end = LocalTime.of(21, 0);
        assertTrue(ShiftCompletionStatus.isFinished(end));
        assertEquals(ShiftCompletionStatus.Kind.FINISHED, ShiftCompletionStatus.of(end));
        assertEquals("finished", ShiftCompletionStatus.cssModifier(ShiftCompletionStatus.Kind.FINISHED));
    }

    @Test
    void fromShiftEntity() {
        Shift open = new Shift();
        open.setEndTime(null);
        assertEquals(ShiftCompletionStatus.Kind.STARTED, ShiftCompletionStatus.of(open));

        Shift closed = new Shift();
        closed.setEndTime(LocalTime.of(22, 30));
        assertEquals(ShiftCompletionStatus.Kind.FINISHED, ShiftCompletionStatus.of(closed));
    }
}
