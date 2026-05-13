package com.company.vzvod.messaging;

import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserTelegramBinding;
import com.company.vzvod.messaging.dto.MessagingDeliveryTargetDto;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Цели доставки дашборд-сообщений")
class MessagingDeliveryTargetServiceTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    MessagingDeliveryTargetService deliveryTargetService;

    private UUID userWithTelegramId;
    private UUID userWithoutTelegramId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        userWithTelegramId = createUser("telegram-user");
        userWithoutTelegramId = createUser("no-telegram-user");

        UserTelegramBinding binding = dataManager.create(UserTelegramBinding.class);
        binding.setUser(dataManager.getReference(User.class, userWithTelegramId));
        binding.setChatId(123456789L);
        binding.setRegisteredAt(OffsetDateTime.now());
        dataManager.save(binding);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Возвращает только пользователей с привязкой Telegram")
    void returnsTelegramTargetsOnly() {
        List<MessagingDeliveryTargetDto> targets = deliveryTargetService.resolveTelegramTargets(
                Set.of(userWithTelegramId, userWithoutTelegramId)
        );

        assertEquals(1, targets.size());
        assertEquals(userWithTelegramId, targets.getFirst().userId());
        assertEquals(123456789L, targets.getFirst().chatId());
    }

    @Test
    @DisplayName("Пустой список получателей даёт пустой результат")
    void emptyRecipients() {
        assertTrue(deliveryTargetService.resolveTelegramTargets(Set.of()).isEmpty());
    }

    private UUID createUser(String username) {
        User user = dataManager.create(User.class);
        user.setUsername(username + "-" + UUID.randomUUID());
        user.setPassword("pwd");
        user.setFirstName("Имя");
        user.setLastName("Фамилия");
        user.setPatronymic("Отчество");
        user = dataManager.save(user);
        return user.getId();
    }
}
