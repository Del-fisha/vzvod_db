package com.company.vzvod.integration;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.MetroStation;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.StatusOfHousing;
import com.company.vzvod.entity.TypeOfHousing;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserTelegramBinding;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("GET /api/bot/me/profile — по X-Telegram-Chat-Id")
class BotMeProfileIntegrationTest {

    private static final String API_KEY = "test-bot-api-key";
    private static final long CHAT_ID = 77_077_077_077L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataManager dataManager;

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
            systemAuthenticator.runWithSystem(() -> removeUserAndBinding(createdUserId));
            createdUserId = null;
        }
        systemAuthenticator.end();
    }

    private void removeUserAndBinding(UUID userId) {
        dataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.user.id = :uid")
                .parameter("uid", userId)
                .list()
                .forEach(dataManager::remove);
        dataManager.load(User.class).id(userId).optional().ifPresent(dataManager::remove);
    }

    private void persistUserWithBinding() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setArmyService(ArmyService.NOT_SERVED);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);
        serviceInfo.setStatus(StatusInService.ACTIVE);

        Department department = dataManager.create(Department.class);
        PreTestEntities.updateDepartment(department);
        department = dataManager.save(department);
        serviceInfo.setDepartment(department);

        IdCard idCard = dataManager.create(IdCard.class);
        PreTestEntities.updateIdCard(idCard);
        idCard = dataManager.save(idCard);
        serviceInfo.setIdCard(idCard);

        user = dataManager.save(user);
        createdUserId = user.getId();

        Contacts contacts = dataManager.create(Contacts.class);
        Address reg = dataManager.create(Address.class);
        reg.setIndex("191014");
        reg.setCity("Санкт-Петербург");
        reg.setStreet("Невский проспект");
        reg.setHouseNumber("1");
        reg.setFlat("10");
        reg.setTypeOfHousing(TypeOfHousing.FLAT);
        reg.setStatusOfHousing(StatusOfHousing.RENTED);
        Address hab = dataManager.create(Address.class);
        hab.setIndex("191014");
        hab.setCity("Санкт-Петербург");
        hab.setStreet("Садовая");
        hab.setHouseNumber("2");
        hab.setTypeOfHousing(TypeOfHousing.FLAT);
        hab.setStatusOfHousing(StatusOfHousing.OWNER);
        contacts.setUser(user);
        contacts.setPhoneNumber("+79115557788");
        contacts.setHabitation(hab);
        contacts.setRegistration(reg);
        contacts.setNearestMetroStation(MetroStation.BALTIYSKAYA);
        dataManager.save(contacts);

        UserTelegramBinding binding = dataManager.create(UserTelegramBinding.class);
        binding.setUser(user);
        binding.setChatId(CHAT_ID);
        binding.setRegisteredAt(OffsetDateTime.now(ZoneOffset.UTC));
        dataManager.save(binding);
    }

    @Test
    @DisplayName("200: профиль по привязанному chat_id")
    void profile_ok() throws Exception {
        persistUserWithBinding();

        mockMvc.perform(get("/api/bot/me/profile")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", String.valueOf(CHAT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(createdUserId.toString()))
                .andExpect(jsonPath("$.displayName").exists())
                .andExpect(jsonPath("$.rank").value("Старший сержант"))
                .andExpect(jsonPath("$.post").value("Командир отделения"))
                .andExpect(jsonPath("$.department").value("Отделение № 1"))
                .andExpect(jsonPath("$.breastplate").value("00659874"))
                .andExpect(jsonPath("$.medicalExamination").value(false))
                .andExpect(jsonPath("$.mobilePhoneMasked").value("+7 *** *** 77 88"))
                .andExpect(jsonPath("$.registration.city").value("Санкт-Петербург"))
                .andExpect(jsonPath("$.habitation.city").value("Санкт-Петербург"))
                .andExpect(jsonPath("$.idCardIssued").exists())
                .andExpect(jsonPath("$.idCardUntil").exists());
    }

    @Test
    @DisplayName("404 без привязки chat_id")
    void profile_notBound_returns404() throws Exception {
        mockMvc.perform(get("/api/bot/me/profile")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", "1234567890123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 без заголовка X-Telegram-Chat-Id")
    void profile_missingChatHeader_returns400() throws Exception {
        mockMvc.perform(get("/api/bot/me/profile")
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("401 при неверном X-Api-Key")
    void profile_wrongApiKey_returns401() throws Exception {
        mockMvc.perform(get("/api/bot/me/profile")
                        .header("X-Api-Key", "wrong-key")
                        .header("X-Telegram-Chat-Id", String.valueOf(CHAT_ID)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT 200: нагрудный знак ровно 8 символов")
    void patch_breastplate_ok() throws Exception {
        persistUserWithBinding();
        mockMvc.perform(put("/api/bot/me/profile")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", String.valueOf(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"breastplate\":\"ABCDEFGH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.breastplate").value("ABCDEFGH"));
    }

    @Test
    @DisplayName("PUT 400: нагрудный знак не 8 символов")
    void patch_breastplate_badLength_returns400() throws Exception {
        persistUserWithBinding();
        mockMvc.perform(put("/api/bot/me/profile")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", String.valueOf(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"breastplate\":\"SHORT\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT 200: профосмотр")
    void patch_medical_ok() throws Exception {
        persistUserWithBinding();
        mockMvc.perform(put("/api/bot/me/profile")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", String.valueOf(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicalExamination\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicalExamination").value(true));
    }

    @Test
    @DisplayName("PUT 200: адрес проживания (город)")
    void patch_habitation_city_ok() throws Exception {
        persistUserWithBinding();
        mockMvc.perform(put("/api/bot/me/profile")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", String.valueOf(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"habitation\":{\"city\":\"Казань\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registration.city").value("Санкт-Петербург"))
                .andExpect(jsonPath("$.habitation.city").value("Казань"));
    }

    @Test
    @DisplayName("PUT 400: индекс адреса не 6 цифр")
    void patch_registration_badIndex_returns400() throws Exception {
        persistUserWithBinding();
        mockMvc.perform(put("/api/bot/me/profile")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", String.valueOf(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"registration\":{\"index\":\"123\"}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT 400: пустое тело")
    void patch_empty_returns400() throws Exception {
        persistUserWithBinding();
        mockMvc.perform(put("/api/bot/me/profile")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", String.valueOf(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
