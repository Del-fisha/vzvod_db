package com.company.vzvod.audit;

import com.company.vzvod.entity.User;
import io.jmix.core.Id;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.event.EntityChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Формирование сообщений аудита сотрудников")
class EmployeeAuditServiceTest {

    @Mock
    private EmployeeAuditSupport auditSupport;

    private EmployeeAuditService auditService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        auditService = new EmployeeAuditService(auditSupport);
        when(auditSupport.isAuditableEntity(User.class)).thenReturn(true);
        when(auditSupport.resolveEmployeeFio(User.class, userId)).thenReturn("Петров П. П.");
        lenient().when(auditSupport.propertyCaption(eq(User.class), any())).thenAnswer(invocation ->
                EmployeeAuditMessageFormatter.fieldLabel(invocation.getArgument(1)));
    }

    private static final String MANUAL_SOURCE = "Способ: вручную (интерфейс)";

    @Test
    @DisplayName("CREATED User — создал сотрудника")
    void createdUser_emitsCreateMessage() {
        EntityChangedEvent<User> event = mock(EntityChangedEvent.class);
        when(event.getEntityId()).thenReturn(Id.of(userId, User.class));
        when(event.getType()).thenReturn(EntityChangedEvent.Type.CREATED);

        List<String> messages = auditService.buildAuditMessages(event, "Иванов И. О.", MANUAL_SOURCE);

        assertThat(messages).containsExactly(
                "Иванов И. О. создал сотрудника Петров П. П." + ". " + MANUAL_SOURCE
        );
    }

    @Test
    @DisplayName("UPDATED User — изменил поле")
    void updatedUser_emitsChangeMessage() {
        User user = new User();
        user.setId(userId);
        user.setLastName("Новая");

        AttributeChanges changes = mock(AttributeChanges.class);
        when(changes.getAttributes()).thenReturn(Set.of("lastName"));
        when(changes.isChanged("lastName")).thenReturn(true);

        EntityChangedEvent<User> event = mock(EntityChangedEvent.class);
        when(event.getEntityId()).thenReturn(Id.of(userId, User.class));
        when(event.getType()).thenReturn(EntityChangedEvent.Type.UPDATED);
        when(event.getChanges()).thenReturn(changes);
        when(auditSupport.loadEntity(event)).thenReturn(user);

        List<String> messages = auditService.buildAuditMessages(event, "Иванов И. О.", MANUAL_SOURCE);

        assertThat(messages).containsExactly(
                "Иванов И. О. изменил \"фамилия\" у сотрудника Петров П. П." + ". " + MANUAL_SOURCE
        );
    }

    @Test
    @DisplayName("UPDATED с очисткой поля — удалено поле")
    void updatedUser_clearedField_emitsDeleteFieldMessage() {
        User user = new User();
        user.setId(userId);
        user.setLastName(null);

        AttributeChanges changes = mock(AttributeChanges.class);
        when(changes.getAttributes()).thenReturn(Set.of("lastName"));
        when(changes.isChanged("lastName")).thenReturn(true);

        EntityChangedEvent<User> event = mock(EntityChangedEvent.class);
        when(event.getEntityId()).thenReturn(Id.of(userId, User.class));
        when(event.getType()).thenReturn(EntityChangedEvent.Type.UPDATED);
        when(event.getChanges()).thenReturn(changes);
        when(auditSupport.loadEntity(event)).thenReturn(user);

        List<String> messages = auditService.buildAuditMessages(event, "Иванов И. О.", MANUAL_SOURCE);

        assertThat(messages).containsExactly(
                "Удалено поле \"фамилия\" у сотрудника Петров П. П. (Иванов И. О.)" + ". " + MANUAL_SOURCE
        );
    }

    @Test
    @DisplayName("DELETED — удалил сущность")
    void deleted_emitsDeleteEntityMessage() {
        EntityChangedEvent<User> event = mock(EntityChangedEvent.class);
        when(event.getEntityId()).thenReturn(Id.of(userId, User.class));
        when(event.getType()).thenReturn(EntityChangedEvent.Type.DELETED);

        List<String> messages = auditService.buildAuditMessages(event, "Иванов И. О.", MANUAL_SOURCE);

        assertThat(messages).containsExactly(
                "Иванов И. О. удалил сотрудника Петров П. П." + ". " + MANUAL_SOURCE
        );
    }
}
