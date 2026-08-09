package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.entity.VocationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("VocationDialogSaveService")
class VocationDialogSaveServiceTest {

    @Test
    @DisplayName("applyEditableFields копирует изменённые поля, включая тип отпуска")
    void applyEditableFields_copiesChangedFieldsIncludingType() {
        ServiceInfo si = new ServiceInfo();
        si.setId(UUID.randomUUID());

        Vocation from = new Vocation();
        from.setUserServiceInfo(si);
        from.setType(VocationType.ADDITIONAL);
        from.setStartDate(LocalDate.of(2026, 4, 1));
        from.setEndDate(LocalDate.of(2026, 4, 5));
        from.setCityToDrive("Казань");
        from.setDaysAddedByDeparture(1);
        from.setHasDeparture(true);

        Vocation to = new Vocation();
        to.setType(VocationType.MAIN);
        to.setStartDate(LocalDate.of(2026, 1, 1));
        to.setEndDate(LocalDate.of(2026, 1, 10));
        to.setCityToDrive("Москва");
        to.setDaysAddedByDeparture(2);
        to.setHasDeparture(false);

        VocationDialogSaveService.applyEditableFields(from, to);

        assertEquals(VocationType.ADDITIONAL, to.getType());
        assertEquals(VocationType.ADDITIONAL.getId(), to.getTypeId());
        assertEquals(LocalDate.of(2026, 4, 1), to.getStartDate());
        assertEquals(LocalDate.of(2026, 4, 5), to.getEndDate());
        assertEquals("Казань", to.getCityToDrive());
        assertEquals(1, to.getDaysAddedByDeparture());
        assertEquals(true, to.isHasDeparture());
        assertEquals(si.getId(), to.getUserServiceInfo().getId());
    }
}
