package com.company.vzvod.audit;

import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Поиск сотрудника для аудита связанных сущностей")
class EmployeeAuditSupportLinkedUserTest {

    @Mock
    private DataManager dataManager;
    @Mock
    private Metadata metadata;
    @Mock
    private Messages messages;
    @Mock
    private MessageTools messageTools;
    @Mock
    private MetadataTools metadataTools;

    private EmployeeAuditSupport auditSupport;

    @BeforeEach
    void setUp() {
        auditSupport = new EmployeeAuditSupport(dataManager, metadata, messages, messageTools, metadataTools);
    }

    @Test
    @DisplayName("IdCard без поля user — не бросает исключение")
    void idCard_withoutUserProperty_doesNotThrow() {
        IdCard idCard = new IdCard();
        idCard.setId(UUID.randomUUID());
        idCard.setSpl("123456");

        MetaClass metaClass = mock(MetaClass.class);
        when(metadata.getClass(IdCard.class)).thenReturn(metaClass);
        when(metaClass.findProperty("user")).thenReturn(null);

        io.jmix.core.FluentLoader<User> fluentLoader = mock(io.jmix.core.FluentLoader.class);
        io.jmix.core.FluentLoader.ByQuery<User> queryLoader = mock(io.jmix.core.FluentLoader.ByQuery.class);
        when(dataManager.load(eq(User.class))).thenReturn(fluentLoader);
        when(fluentLoader.query(any(String.class))).thenReturn(queryLoader);
        when(queryLoader.parameter(any(), any())).thenReturn(queryLoader);
        when(queryLoader.optional()).thenReturn(Optional.empty());

        assertThatCode(() -> auditSupport.resolveEmployeeFio(idCard))
                .doesNotThrowAnyException();
        assertThat(auditSupport.resolveEmployeeFio(idCard)).isEqualTo("—");
    }
}
