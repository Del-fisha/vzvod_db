package com.company.vzvod.service;

import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.EducationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("EducationStatusService")
class EducationStatusServiceTest {

    private final EducationStatusService service = new EducationStatusService();

    @Test
    @DisplayName("До даты окончания включительно — Учится")
    void beforeOrOnUntil_studying() {
        Education education = new Education();
        LocalDate until = LocalDate.now().plusMonths(6);
        education.setUntil(until);

        service.applyStatusFromUntil(education);

        assertEquals(EducationStatus.AT_THE_MOMENT, education.getStatus());
    }

    @Test
    @DisplayName("После даты окончания — Закончил")
    void afterUntil_finished() {
        Education education = new Education();
        education.setUntil(LocalDate.now().minusDays(1));

        service.applyStatusFromUntil(education);

        assertEquals(EducationStatus.FINISHED, education.getStatus());
    }

    @Test
    @DisplayName("Без даты окончания статус не меняется")
    void withoutUntil_statusUnchanged() {
        Education education = new Education();
        education.setStatus(EducationStatus.AT_THE_MOMENT);

        service.applyStatusFromUntil(education);

        assertEquals(EducationStatus.AT_THE_MOMENT, education.getStatus());
    }

    @Test
    @DisplayName("null education — без ошибки")
    void nullEducation_noOp() {
        service.applyStatusFromUntil(null);
    }

    @Test
    @DisplayName("Без until статус остаётся null")
    void withoutUntil_nullStatus() {
        Education education = new Education();

        service.applyStatusFromUntil(education);

        assertNull(education.getStatus());
    }
}
