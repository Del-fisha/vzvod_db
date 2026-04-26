package com.company.vzvod.service;

import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VocationBalanceServiceTest {

    @Test
    void calcCurrentYearStats_entitledOnJan1_usedByQuery_availableIsEntitledPlusAddedMinusUsed() {
        DataManager dataManager = mock(DataManager.class, RETURNS_DEEP_STUBS);
        when(dataManager.loadValue(anyString(), eq(Integer.class))
                .parameter(anyString(), any())
                .parameter(anyString(), any())
                .parameter(anyString(), any())
                .parameter(anyString(), any())
                .one()).thenReturn(12, 3);

        VocationBalanceService service = new VocationBalanceService(dataManager);

        User user = new User();
        user.setArmyService(ArmyService.SERVED);

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setId(UUID.randomUUID());
        serviceInfo.setUser(user);
        serviceInfo.setStartDate(LocalDate.of(2010, 1, 1));

        var stats = service.calcCurrentYearStats(serviceInfo, LocalDate.of(2026, 4, 24));

        // 2010-01-01 -> 2026-01-01 = 16 лет + 1 год за армию => 17 => >=15 => +10 дней.
        assertEquals(53, stats.entitled());
        assertEquals(12, stats.used());
        assertEquals(41, stats.available());
    }
}

