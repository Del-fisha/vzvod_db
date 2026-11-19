package com.company.vzvod.service;

import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.VocationType;
import io.jmix.core.UnconstrainedDataManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VocationBalanceServiceTest {

    @Test
    @DisplayName("В баланс входят MAIN/ADDITIONAL/PART_OF_MAIN, учебный и Цпп — нет")
    void balanceAffectingTypes_includeMainAdditionalPartOfMain_only() {
        var types = VocationBalanceService.balanceAffectingTypeIds();
        assertTrue(types.contains(VocationType.MAIN.getId()));
        assertTrue(types.contains(VocationType.ADDITIONAL.getId()));
        assertTrue(types.contains(VocationType.PART_OF_MAIN.getId()));
        assertFalse(types.contains(VocationType.STUDY_LEAVE.getId()));
        assertFalse(types.contains(VocationType.PTC.getId()));
    }

    @Test
    @DisplayName("Положено — только норматив выслуги, использовано — календарные дни котла")
    void calcCurrentYearStats_entitledIsSeniorityTier_usedIsCalendarPoolDays() {
        // Unconstrained: мобильный/бот create vacation без UserDetails в SecurityContext.
        UnconstrainedDataManager dataManager = mock(UnconstrainedDataManager.class, RETURNS_DEEP_STUBS);
        when(dataManager.loadValue(anyString(), eq(Integer.class))
                .parameter(anyString(), any())
                .parameter(anyString(), any())
                .parameter(anyString(), any())
                .parameter(anyString(), any())
                .one()).thenReturn(12);

        VocationBalanceService service = new VocationBalanceService(dataManager);

        User user = new User();
        user.setArmyService(ArmyService.SERVED);

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setId(UUID.randomUUID());
        serviceInfo.setUser(user);
        serviceInfo.setStartDate(LocalDate.of(2010, 1, 1));

        var stats = service.calcCurrentYearStats(serviceInfo, LocalDate.of(2026, 4, 24));

        // 2010-01-01 -> 2026-01-01 = 16 календарных лет + 1 год за армию => 50 дней.
        // «Добавлено дней» в положено не входит и из использованного не вычитается.
        assertEquals(50, stats.entitled());
        assertEquals(12, stats.used());
        assertEquals(38, stats.available());
    }

    @Test
    @DisplayName("Без даты начала службы котёл 40, списание — календарные дни")
    void calcCurrentYearStats_withoutStartDate_isFortyMinusCalendarDays() {
        UnconstrainedDataManager dataManager = mock(UnconstrainedDataManager.class, RETURNS_DEEP_STUBS);
        when(dataManager.loadValue(anyString(), eq(Integer.class))
                .parameter(anyString(), any())
                .parameter(anyString(), any())
                .parameter(anyString(), any())
                .parameter(anyString(), any())
                .one()).thenReturn(11);

        VocationBalanceService service = new VocationBalanceService(dataManager);

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setId(UUID.randomUUID());
        serviceInfo.setUser(new User());

        var stats = service.calcCurrentYearStats(serviceInfo, LocalDate.of(2026, 2, 20));

        assertEquals(40, stats.entitled());
        assertEquals(11, stats.used());
        assertEquals(29, stats.available());
    }
}
