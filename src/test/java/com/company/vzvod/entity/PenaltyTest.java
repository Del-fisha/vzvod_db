package com.company.vzvod.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Penalty auto-completion")
class PenaltyTest {

    @Test
    @DisplayName("ACTIVE penalty older than one year becomes COMPLETED")
    void autoCompleteIfExpired_whenOlderThanYear() {
        Penalty penalty = new Penalty();
        penalty.setPenaltyStatus(PenaltyStatus.ACTIVE);
        penalty.setDate(LocalDate.of(2020, 1, 1));

        assertTrue(penalty.autoCompleteIfExpired(LocalDate.of(2025, 1, 1)));
        assertEquals(PenaltyStatus.COMPLETED, penalty.getPenaltyStatus());
    }

    @Test
    @DisplayName("ACTIVE penalty exactly one year old becomes COMPLETED")
    void autoCompleteIfExpired_whenExactlyOneYear() {
        Penalty penalty = new Penalty();
        penalty.setPenaltyStatus(PenaltyStatus.ACTIVE);
        penalty.setDate(LocalDate.of(2024, 5, 23));

        assertTrue(penalty.autoCompleteIfExpired(LocalDate.of(2025, 5, 23)));
        assertEquals(PenaltyStatus.COMPLETED, penalty.getPenaltyStatus());
    }

    @Test
    @DisplayName("ACTIVE penalty younger than one year stays ACTIVE")
    void autoCompleteIfExpired_whenYoungerThanYear() {
        Penalty penalty = new Penalty();
        penalty.setPenaltyStatus(PenaltyStatus.ACTIVE);
        penalty.setDate(LocalDate.of(2025, 1, 1));

        assertFalse(penalty.autoCompleteIfExpired(LocalDate.of(2025, 5, 23)));
        assertEquals(PenaltyStatus.ACTIVE, penalty.getPenaltyStatus());
    }

    @Test
    @DisplayName("REMOVED and COMPLETED penalties are not changed")
    void autoCompleteIfExpired_skipsNonActive() {
        Penalty removed = new Penalty();
        removed.setPenaltyStatus(PenaltyStatus.REMOVED);
        removed.setDate(LocalDate.of(2020, 1, 1));
        assertFalse(removed.autoCompleteIfExpired(LocalDate.now()));

        Penalty completed = new Penalty();
        completed.setPenaltyStatus(PenaltyStatus.COMPLETED);
        completed.setDate(LocalDate.of(2020, 1, 1));
        assertFalse(completed.autoCompleteIfExpired(LocalDate.now()));
    }
}
