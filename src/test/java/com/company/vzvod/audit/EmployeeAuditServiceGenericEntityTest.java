package com.company.vzvod.audit;

import com.company.vzvod.entity.Event;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Аудит произвольных сущностей БД")
class EmployeeAuditServiceGenericEntityTest {

    @Mock
    private EmployeeAuditSupport auditSupport;

    private EmployeeAuditService auditService;

    private final UUID eventId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        auditService = new EmployeeAuditService(auditSupport);
        when(auditSupport.isAuditableEntity(Event.class)).thenReturn(true);
        when(auditSupport.resolveEmployeeFio(Event.class, eventId)).thenReturn("—");
        when(auditSupport.entityCaption(Event.class)).thenReturn("Событие");
        when(auditSupport.instanceRef(eq(Event.class), eq(eventId), any())).thenReturn("Патруль 01.01.26");
    }

    @Test
    @DisplayName("CREATED Event — общее сообщение и способ")
    void createdEvent() {
        Event event = new Event();
        event.setId(eventId);
        event.setName("Патруль");

        EntityChangedEvent<Event> changedEvent = mock(EntityChangedEvent.class);
        when(changedEvent.getEntityId()).thenReturn(Id.of(eventId, Event.class));
        when(changedEvent.getType()).thenReturn(EntityChangedEvent.Type.CREATED);
        when(auditSupport.loadEntity(changedEvent)).thenReturn(event);

        List<String> messages = auditService.buildAuditMessages(
                changedEvent,
                "Система",
                "Способ: программно — EventParserJob.run"
        );

        assertThat(messages).containsExactly(
                "Система создал Событие (Патруль 01.01.26). Способ: программно — EventParserJob.run"
        );
    }
}
