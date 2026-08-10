package com.company.vzvod.listener;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.service.ServiceInfoVocationStatusService;
import com.company.vzvod.service.VocationBalanceService;
import io.jmix.core.FluentLoader;
import io.jmix.core.Id;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VocationChangedListenerTest {

    @Mock UnconstrainedDataManager dataManager;
    @Mock VocationBalanceService vocationBalanceService;
    @Mock ServiceInfoVocationStatusService serviceInfoVocationStatusService;
    @Mock SystemAuthenticator systemAuthenticator;

    VocationChangedListener listener;

    @BeforeEach
    void setUp() {
        listener = new VocationChangedListener(
                dataManager,
                vocationBalanceService,
                serviceInfoVocationStatusService,
                systemAuthenticator
        );
        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(systemAuthenticator).runWithSystem(any(Runnable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void onCreate_usesUnconstrainedLoad_andRecalcUnderSystemAuth() {
        UUID vocationId = UUID.randomUUID();
        UUID serviceInfoId = UUID.randomUUID();

        ServiceInfo serviceInfo = mock(ServiceInfo.class);
        when(serviceInfo.getId()).thenReturn(serviceInfoId);

        Vocation vocation = mock(Vocation.class);
        when(vocation.getUserServiceInfo()).thenReturn(serviceInfo);

        FluentLoader.ById<Vocation> byId = mock(FluentLoader.ById.class);
        when(dataManager.load(any(Id.class))).thenReturn(byId);
        when(byId.one()).thenReturn(vocation);

        EntityChangedEvent<Vocation> event = mock(EntityChangedEvent.class);
        when(event.getEntityId()).thenReturn(Id.of(vocationId, Vocation.class));
        when(event.getType()).thenReturn(EntityChangedEvent.Type.CREATED);

        listener.onVocationChangedBeforeCommit(event);

        verify(systemAuthenticator).runWithSystem(any(Runnable.class));
        verify(dataManager).load(any(Id.class));
        verify(vocationBalanceService).recalcAndSave(serviceInfoId);
        verify(serviceInfoVocationStatusService).syncForServiceInfo(any(), any());
    }
}
