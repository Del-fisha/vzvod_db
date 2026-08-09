package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.VocationType;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.UnconstrainedDataManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ServiceInfoVocationStatusService")
class ServiceInfoVocationStatusServiceTest {

    @Test
    @DisplayName("resolveFromTypeIds: пусто -> ACTIVE")
    void resolve_empty_active() {
        assertEquals(StatusInService.ACTIVE, ServiceInfoVocationStatusService.resolveFromTypeIds(List.of()));
    }

    @Test
    @DisplayName("resolveFromTypeIds: основной/доп/часть -> VOCATION")
    void resolve_regular_vocation() {
        assertEquals(StatusInService.VOCATION,
                ServiceInfoVocationStatusService.resolveFromTypeIds(List.of(VocationType.MAIN.getId())));
        assertEquals(StatusInService.VOCATION,
                ServiceInfoVocationStatusService.resolveFromTypeIds(List.of(VocationType.ADDITIONAL.getId())));
        assertEquals(StatusInService.VOCATION,
                ServiceInfoVocationStatusService.resolveFromTypeIds(List.of(VocationType.PART_OF_MAIN.getId())));
    }

    @Test
    @DisplayName("resolveFromTypeIds: учебный отпуск -> STUDY_LEAVE")
    void resolve_studyLeave() {
        assertEquals(StatusInService.STUDY_LEAVE,
                ServiceInfoVocationStatusService.resolveFromTypeIds(List.of(VocationType.STUDY_LEAVE.getId())));
    }

    @Test
    @DisplayName("resolveFromTypeIds: Цпп -> PTC")
    void resolve_ptc() {
        assertEquals(StatusInService.PTC,
                ServiceInfoVocationStatusService.resolveFromTypeIds(List.of(VocationType.PTC.getId())));
    }

    @Test
    @DisplayName("resolveFromTypeIds: Цпп приоритетнее учебного и обычного")
    void resolve_ptc_winsOverOthers() {
        assertEquals(StatusInService.PTC, ServiceInfoVocationStatusService.resolveFromTypeIds(List.of(
                VocationType.MAIN.getId(),
                VocationType.STUDY_LEAVE.getId(),
                VocationType.PTC.getId()
        )));
    }

    @Test
    @DisplayName("resolveFromTypeIds: учебный приоритетнее обычного отпуска")
    void resolve_studyLeave_winsOverRegular() {
        assertEquals(StatusInService.STUDY_LEAVE, ServiceInfoVocationStatusService.resolveFromTypeIds(List.of(
                VocationType.MAIN.getId(),
                VocationType.STUDY_LEAVE.getId()
        )));
    }

    @Test
    @DisplayName("isManagedStatus: ACTIVE/VOCATION/STUDY_LEAVE/PTC управляются, остальные нет")
    void managedStatuses() {
        assertTrue(ServiceInfoVocationStatusService.isManagedStatus(StatusInService.ACTIVE));
        assertTrue(ServiceInfoVocationStatusService.isManagedStatus(StatusInService.VOCATION));
        assertTrue(ServiceInfoVocationStatusService.isManagedStatus(StatusInService.STUDY_LEAVE));
        assertTrue(ServiceInfoVocationStatusService.isManagedStatus(StatusInService.PTC));
        assertFalse(ServiceInfoVocationStatusService.isManagedStatus(StatusInService.SICK_LEAVE));
        assertFalse(ServiceInfoVocationStatusService.isManagedStatus(StatusInService.BUSINESS_TRIP));
        assertFalse(ServiceInfoVocationStatusService.isManagedStatus(null));
    }

    @Test
    @DisplayName("syncForServiceInfo: при учебном отпуске сегодня ставит STUDY_LEAVE")
    void sync_setsStudyLeave() {
        UnconstrainedDataManager dataManager = mock(UnconstrainedDataManager.class, RETURNS_DEEP_STUBS);
        UUID id = UUID.randomUUID();
        LocalDate today = LocalDate.of(2026, 8, 6);
        ServiceInfo si = new ServiceInfo();
        si.setId(id);
        si.setStatus(StatusInService.ACTIVE);

        stubServiceInfo(dataManager, id, si);
        stubTypeIds(dataManager, List.of(VocationType.STUDY_LEAVE.getId()));

        new ServiceInfoVocationStatusService(dataManager).syncForServiceInfo(id, today);

        assertEquals(StatusInService.STUDY_LEAVE, si.getStatus());
        verify(dataManager).save(si);
    }

    @Test
    @DisplayName("syncForServiceInfo: при Цпп сегодня ставит PTC")
    void sync_setsPtc() {
        UnconstrainedDataManager dataManager = mock(UnconstrainedDataManager.class, RETURNS_DEEP_STUBS);
        UUID id = UUID.randomUUID();
        LocalDate today = LocalDate.of(2026, 8, 6);
        ServiceInfo si = new ServiceInfo();
        si.setId(id);
        si.setStatus(StatusInService.ACTIVE);

        stubServiceInfo(dataManager, id, si);
        stubTypeIds(dataManager, List.of(VocationType.PTC.getId()));

        new ServiceInfoVocationStatusService(dataManager).syncForServiceInfo(id, today);

        assertEquals(StatusInService.PTC, si.getStatus());
        verify(dataManager).save(si);
    }

    @Test
    @DisplayName("syncForServiceInfo: без отпуска возвращает ACTIVE из STUDY_LEAVE")
    void sync_clearsStudyLeaveToActive() {
        UnconstrainedDataManager dataManager = mock(UnconstrainedDataManager.class, RETURNS_DEEP_STUBS);
        UUID id = UUID.randomUUID();
        LocalDate today = LocalDate.of(2026, 8, 6);
        ServiceInfo si = new ServiceInfo();
        si.setId(id);
        si.setStatus(StatusInService.STUDY_LEAVE);

        stubServiceInfo(dataManager, id, si);
        stubTypeIds(dataManager, List.of());

        new ServiceInfoVocationStatusService(dataManager).syncForServiceInfo(id, today);

        assertEquals(StatusInService.ACTIVE, si.getStatus());
        verify(dataManager).save(si);
    }

    @Test
    @DisplayName("syncForServiceInfo: больничный не перезаписывается")
    void sync_doesNotTouchSickLeave() {
        UnconstrainedDataManager dataManager = mock(UnconstrainedDataManager.class, RETURNS_DEEP_STUBS);
        UUID id = UUID.randomUUID();
        LocalDate today = LocalDate.of(2026, 8, 6);
        ServiceInfo si = new ServiceInfo();
        si.setId(id);
        si.setStatus(StatusInService.SICK_LEAVE);

        stubServiceInfo(dataManager, id, si);

        new ServiceInfoVocationStatusService(dataManager).syncForServiceInfo(id, today);

        verify(dataManager, never()).save(any(ServiceInfo.class));
    }

    private static void stubServiceInfo(UnconstrainedDataManager dataManager, UUID id, ServiceInfo si) {
        when(dataManager.load(ServiceInfo.class).id(id)
                .fetchPlan(anyFetchPlan())
                .optional()).thenReturn(Optional.of(si));
    }

    private static void stubTypeIds(UnconstrainedDataManager dataManager, List<Integer> typeIds) {
        when(dataManager.loadValue(anyString(), eq(Integer.class))
                .parameter(anyString(), any())
                .parameter(anyString(), any())
                .list()).thenReturn(typeIds);
    }

    @SuppressWarnings("unchecked")
    private static Consumer<FetchPlanBuilder> anyFetchPlan() {
        return any(Consumer.class);
    }
}
