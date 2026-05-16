package com.company.vzvod.integration;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("POST /api/bot/auth — телефон, статус, привязка Telegram")
class BotAuthIntegrationTest {

    private static final String API_KEY = "test-bot-api-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private UnconstrainedDataManager unconstrainedDataManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private UUID createdUserId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        if (createdUserId != null) {
            systemAuthenticator.runWithSystem(() ->
                    dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove)
            );
            createdUserId = null;
        }
        systemAuthenticator.end();
    }

    private User persistActiveUserWithPhone(String phoneDigitsOrPlus) {
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
        contacts.setPhoneNumber(phoneDigitsOrPlus);
        contacts.setHabitation(address);
        contacts.setRegistration(address);
        contacts.setNearestMetroStation(MetroStation.BALTIYSKAYA);
        dataManager.save(contacts);

        return dataManager.load(User.class).id(user.getId()).one();
    }

    @Test
    @DisplayName("401 без X-Api-Key при настроенном ключе")
    void missingApiKey_unauthorized() throws Exception {
        mockMvc.perform(post("/api/bot/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+79990001122\",\"chatId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401 при неверном X-Api-Key")
    void wrongApiKey_unauthorized() throws Exception {
        mockMvc.perform(post("/api/bot/auth")
                        .header("X-Api-Key", "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+79990001122\",\"chatId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("404 если номер не найден")
    void unknownPhone_notFound() throws Exception {
        mockMvc.perform(post("/api/bot/auth")
                        .header("X-Api-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+79990001122\",\"chatId\":424242}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("403 если сотрудник не в статусе ACTIVE")
    void inactiveUser_forbidden() throws Exception {
        String inactivePhone = "+7982" + String.format("%07d", ThreadLocalRandom.current().nextInt(10_000_000));

        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setArmyService(ArmyService.NOT_SERVED);
        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);
        user = dataManager.save(user);
        createdUserId = user.getId();

        Contacts contacts = dataManager.create(Contacts.class);
        Address address = dataManager.create(Address.class);
        contacts.setUser(user);
        contacts.setPhoneNumber(inactivePhone);
        contacts.setHabitation(address);
        contacts.setRegistration(address);
        contacts.setNearestMetroStation(MetroStation.BALTIYSKAYA);
        dataManager.save(contacts);

        jdbcTemplate.update(
                "update service_info set status = ? where user_id = ?",
                StatusInService.VOCATION.getId(),
                createdUserId);

        mockMvc.perform(post("/api/bot/auth")
                        .header("X-Api-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + inactivePhone + "\",\"chatId\":111}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("200: userId, displayName и строка в USER_TELEGRAM_BINDING")
    void happyPath_createsBinding() throws Exception {
        persistActiveUserWithPhone("89115557788");
        long chatId = 55_055_055_055L;

        mockMvc.perform(post("/api/bot/auth")
                        .header("X-Api-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"89115557788\",\"chatId\":" + chatId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(createdUserId.toString()))
                .andExpect(jsonPath("$.displayName").exists());

        UserTelegramBinding binding = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.user.id = :uid")
                .parameter("uid", createdUserId)
                .optional()
                .orElseThrow();
        assertEquals(chatId, binding.getChatId());
        assertNotNull(binding.getRegisteredAt());
    }

    @Test
    @DisplayName("Повторная авторизация обновляет chat_id у того же пользователя")
    void sameUser_newChat_updatesBinding() throws Exception {
        persistActiveUserWithPhone("89116668899");

        mockMvc.perform(post("/api/bot/auth")
                        .header("X-Api-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"89116668899\",\"chatId\":100}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/bot/auth")
                        .header("X-Api-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+79116668899\",\"chatId\":200}"))
                .andExpect(status().isOk());

        long cnt = unconstrainedDataManager.loadValue(
                        "select count(b) from UserTelegramBinding b where b.user.id = :uid",
                        Long.class)
                .parameter("uid", createdUserId)
                .one();
        assertEquals(1L, cnt);

        UserTelegramBinding b = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.user.id = :uid")
                .parameter("uid", createdUserId)
                .one();
        assertEquals(200L, b.getChatId());
    }
}
