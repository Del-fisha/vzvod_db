package com.company.vzvod.integration;

import com.company.vzvod.bot.BotTelegramAccessService;
import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.MetroStation;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserTelegramBinding;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Закрытие доступа Telegram-бота при изменении данных сотрудника")
class BotTelegramAccessIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private UnconstrainedDataManager unconstrainedDataManager;

    @Autowired
    private BotTelegramAccessService botTelegramAccessService;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID createdUserId;
    private static final long CHAT_ID = 88_088_088_088L;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        if (createdUserId != null) {
            systemAuthenticator.runWithSystem(() -> {
                unconstrainedDataManager.load(UserTelegramBinding.class)
                        .query("select b from UserTelegramBinding b where b.user.id = :uid")
                        .parameter("uid", createdUserId)
                        .list()
                        .forEach(unconstrainedDataManager::remove);
                dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove);
            });
            createdUserId = null;
        }
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Смена телефона снимает привязку Telegram")
    void phoneChange_revokesBinding() {
        User user = persistActiveUserWithPhoneAndBinding("+79112223344");
        Contacts contacts = dataManager.load(Contacts.class)
                .query("select c from Contacts c where c.user.id = :uid")
                .parameter("uid", user.getId())
                .one();
        contacts.setPhoneNumber("+79115556677");
        dataManager.save(contacts);

        assertEquals(0L, bindingCount(user.getId()));
    }

    @Test
    @DisplayName("Статус не ACTIVE снимает привязку Telegram")
    void statusNotActive_revokesBinding() {
        User user = persistActiveUserWithPhoneAndBinding("+79113334455");
        ServiceInfo serviceInfo = dataManager.load(ServiceInfo.class)
                .query("select s from ServiceInfo s where s.user.id = :uid")
                .parameter("uid", user.getId())
                .one();
        serviceInfo.setStatus(StatusInService.SICK_LEAVE);
        dataManager.save(serviceInfo);

        assertEquals(0L, bindingCount(user.getId()));
    }

    @Test
    @DisplayName("Ежедневная сверка снимает привязку неактивного сотрудника")
    void reconciliation_revokesInactiveBinding() {
        User user = persistActiveUserWithPhoneAndBinding("+79114445566");
        jdbcTemplate.update(
                "update service_info set status = ? where user_id = ?",
                StatusInService.VOCATION.getId(),
                user.getId());

        int[] closed = {0};
        systemAuthenticator.runWithSystem(() ->
                closed[0] = botTelegramAccessService.reconcileStaleBindings());
        assertEquals(1, closed[0]);
        assertEquals(0L, bindingCount(user.getId()));
    }

    private User persistActiveUserWithPhoneAndBinding(String phone) {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setArmyService(ArmyService.NOT_SERVED);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);
        serviceInfo.setStatus(StatusInService.ACTIVE);

        user = dataManager.save(user);
        createdUserId = user.getId();

        Contacts contacts = dataManager.create(Contacts.class);
        Address address = dataManager.create(Address.class);
        contacts.setUser(user);
        contacts.setPhoneNumber(phone);
        contacts.setHabitation(address);
        contacts.setRegistration(address);
        contacts.setNearestMetroStation(MetroStation.BALTIYSKAYA);
        dataManager.save(contacts);

        UserTelegramBinding binding = unconstrainedDataManager.create(UserTelegramBinding.class);
        binding.setUser(user);
        binding.setChatId(CHAT_ID);
        binding.setRegisteredAt(OffsetDateTime.now(ZoneOffset.UTC));
        unconstrainedDataManager.save(binding);

        return user;
    }

    private long bindingCount(UUID userId) {
        return unconstrainedDataManager.loadValue(
                        "select count(b) from UserTelegramBinding b where b.user.id = :uid",
                        Long.class)
                .parameter("uid", userId)
                .one();
    }
}
