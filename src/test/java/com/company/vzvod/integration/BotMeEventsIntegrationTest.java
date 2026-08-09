package com.company.vzvod.integration;

import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.Event;
import com.company.vzvod.entity.EventType;
import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
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
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("GET /api/bot/me/events")
class BotMeEventsIntegrationTest {

    private static final String API_KEY = "test-bot-api-key";
    private static final long CHAT_ID = 88_088_088_089L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private UUID createdUserId;
    private final List<UUID> createdEventIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        if (createdUserId != null || !createdEventIds.isEmpty()) {
            systemAuthenticator.runWithSystem(() -> {
                for (UUID eventId : createdEventIds) {
                    dataManager.load(Event.class).id(eventId).optional().ifPresent(dataManager::remove);
                }
                createdEventIds.clear();
                if (createdUserId != null) {
                    dataManager.load(UserTelegramBinding.class)
                            .query("select b from UserTelegramBinding b where b.user.id = :uid")
                            .parameter("uid", createdUserId)
                            .list()
                            .forEach(dataManager::remove);
                    dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove);
                }
            });
            createdUserId = null;
        }
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("возвращает только сегодня и будущие, по возрастанию даты")
    void returnsUpcomingSortedAscending() throws Exception {
        persistUserBinding();
        LocalDate today = LocalDate.now();
        createdEventIds.add(saveEvent("Прошлое", today.minusDays(3)).getId());
        createdEventIds.add(saveEvent("Далёкое", today.plusDays(30)).getId());
        createdEventIds.add(saveEvent("Сегодня", today).getId());
        createdEventIds.add(saveEvent("Скоро", today.plusDays(2)).getId());

        mockMvc.perform(get("/api/bot/me/events")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].name").value("Сегодня"))
                .andExpect(jsonPath("$.items[1].name").value("Скоро"))
                .andExpect(jsonPath("$.items[2].name").value("Далёкое"));
    }

    @Test
    @DisplayName("пустое имя события заменяется на «—»")
    void blankEventNameBecomesDash() throws Exception {
        persistUserBinding();
        LocalDate today = LocalDate.now();
        createdEventIds.add(saveEvent("   ", today).getId());

        mockMvc.perform(get("/api/bot/me/events")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].name").value("—"));
    }

    @Test
    @DisplayName("POST 201: создаёт спортивное мероприятие по названию и заполняет shiftOfDepartment")
    void createEvent_sportName_returnsCreatedWithSportType() throws Exception {
        persistUserBinding();
        LocalDate date = LocalDate.now().plusDays(10);

        String response = mockMvc.perform(post("/api/bot/me/events")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Матч Зенит\",\"date\":\"" + date + "\",\"place\":\"Газпром Арена\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Матч Зенит"))
                .andExpect(jsonPath("$.place").value("Газпром Арена"))
                .andExpect(jsonPath("$.eventType").value(EventType.SPORT.getId()))
                .andExpect(jsonPath("$.shiftOfDepartment").exists())
                .andReturn().getResponse().getContentAsString();

        UUID createdId = UUID.fromString(JsonPath.read(response, "$.id").toString());
        createdEventIds.add(createdId);
    }

    @Test
    @DisplayName("POST 400: без имени или даты")
    void createEvent_missingFields_returns400() throws Exception {
        persistUserBinding();
        mockMvc.perform(post("/api/bot/me/events")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("404 если chat_id не привязан")
    void notFoundWhenChatNotBound() throws Exception {
        mockMvc.perform(get("/api/bot/me/events")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", "99999999999"))
                .andExpect(status().isNotFound());
    }

    private Event saveEvent(String name, LocalDate date) {
        Event event = dataManager.create(Event.class);
        event.setName(name);
        event.setDate(date);
        event.setTime(LocalTime.of(10, 0));
        event.setEventType(EventType.OTHER);
        return dataManager.save(event);
    }

    private void persistUserBinding() {
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
        contacts.setUser(user);
        dataManager.save(contacts);

        UserTelegramBinding binding = dataManager.create(UserTelegramBinding.class);
        binding.setUser(user);
        binding.setChatId(CHAT_ID);
        binding.setRegisteredAt(OffsetDateTime.now(ZoneOffset.UTC));
        dataManager.save(binding);
    }
}
