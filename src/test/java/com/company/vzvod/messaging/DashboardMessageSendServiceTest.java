package com.company.vzvod.messaging;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.User;
import com.company.vzvod.messaging.client.NotificationServiceClient;
import com.company.vzvod.messaging.dto.DashboardMessageDispatchRequest;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.service.ShiftOperationalDay;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Отправка дашборд-сообщения")
class DashboardMessageSendServiceTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    DashboardMessageSendService dashboardMessageSendService;

    @MockBean
    NotificationServiceClient notificationServiceClient;

    @MockBean
    UiAccessService uiAccessService;

    private UUID senderId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        senderId = createUser();
    }

    @AfterEach
    void tearDown() {
        dataManager.load(User.class).id(senderId).optional().ifPresent(dataManager::remove);
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Без FullAccessRole отправка запрещена")
    void requiresFullAccessRole() {
        when(uiAccessService.hasFullAccessRole()).thenReturn(false);

        User sender = dataManager.load(User.class).id(senderId).one();
        assertThrows(
                AccessDeniedException.class,
                () -> systemAuthenticator.withUser(sender.getUsername(), () -> {
                    dashboardMessageSendService.send(DashboardMessageAudience.ALL_EMPLOYEES, "Текст");
                    return null;
                })
        );
    }

    @Test
    @DisplayName("Формирует запрос в notification-service с отправителем и текстом")
    void sendsFormattedMessage() {
        when(uiAccessService.hasFullAccessRole()).thenReturn(true);

        User sender = dataManager.load(User.class).id(senderId).one();
        systemAuthenticator.withUser(sender.getUsername(), () -> {
            dashboardMessageSendService.send(DashboardMessageAudience.ALL_EMPLOYEES, "Строка 1\nСтрока 2");
            return null;
        });

        verify(notificationServiceClient).sendDashboardMessage(argThat(request ->
                request.senderUserId().equals(senderId)
                        && request.senderDisplayName().equals("Фамилия И. О.")
                        && request.body().equals("Строка 1\nСтрока 2")
                        && request.recipientUserIds().contains(senderId)
        ));
    }

    private UUID createUser() {
        User user = dataManager.create(User.class);
        user.setUsername("sender-" + UUID.randomUUID());
        user.setPassword("pwd");
        user.setFirstName("Имя");
        user.setLastName("Фамилия");
        user.setPatronymic("Отчество");
        user = dataManager.save(user);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        serviceInfo.setUser(user);
        serviceInfo.setPost(Post.COM_OTD);
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setToken("token-" + UUID.randomUUID());
        dataManager.save(serviceInfo);

        return user.getId();
    }
}
