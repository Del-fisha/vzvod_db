package com.company.vzvod.integration;

import com.company.vzvod.bot.dto.BotShiftEndTimeRequest;
import com.company.vzvod.bot.dto.BotShiftUpsertRequest;
import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserTelegramBinding;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.entity.VocationType;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("GET /api/bot/me/shifts и /vacations")
class BotMeShiftsVacationsIntegrationTest {

    private static final String API_KEY = "test-bot-api-key";
    private static final long CHAT_ID = 88_088_088_087L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private UUID createdUserId;
    private UUID createdPartnerUserId;
    private UUID createdShiftId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.runWithSystem(() -> {
            dataManager.load(UserTelegramBinding.class)
                    .query("select b from UserTelegramBinding b where b.chatId = :cid")
                    .parameter("cid", CHAT_ID)
                    .list()
                    .forEach(dataManager::remove);
            if (createdUserId != null) {
                removeUserGraph(createdUserId);
            }
            if (createdPartnerUserId != null) {
                removeUserGraph(createdPartnerUserId);
            }
        });
        createdUserId = null;
        createdPartnerUserId = null;
        createdShiftId = null;
        systemAuthenticator.end();
    }

    private void removeUserGraph(UUID userId) {
        dataManager.load(User.class).id(userId).optional().ifPresent(user -> {
            if (user.getServiceInfo() != null) {
                UUID sid = user.getServiceInfo().getId();
                dataManager.load(Shift.class)
                        .query("select s from Shift s join s.units u where u.id = :sid")
                        .parameter("sid", sid)
                        .list()
                        .forEach(this::removeShiftGraph);
                dataManager.load(Vocation.class)
                        .query("select v from Vocation v where v.userServiceInfo.id = :sid")
                        .parameter("sid", sid)
                        .list()
                        .forEach(dataManager::remove);
            }
            dataManager.load(UserTelegramBinding.class)
                    .query("select b from UserTelegramBinding b where b.user.id = :uid")
                    .parameter("uid", userId)
                    .list()
                    .forEach(dataManager::remove);
            dataManager.remove(user);
        });
    }

    private void removeShiftGraph(Shift shift) {
        dataManager.load(CriminalViolation.class)
                .query("select v from CriminalViolation v where v.shift.id = :shiftId")
                .parameter("shiftId", shift.getId())
                .list()
                .forEach(dataManager::remove);
        dataManager.load(AdministrativeViolation.class)
                .query("select v from AdministrativeViolation v where v.shift.id = :shiftId")
                .parameter("shiftId", shift.getId())
                .list()
                .forEach(dataManager::remove);
        dataManager.remove(shift);
    }

    private void persistUserShiftAndVocation() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setArmyService(ArmyService.NOT_SERVED);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setVacationDaysEntitled(40);
        serviceInfo.setVacationDaysAvailable(28);

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

        Shift shift = dataManager.create(Shift.class);
        shift.setDate(LocalDate.of(2026, 1, 15));
        shift.setNumber(NumberOfShift._28);
        shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shift.setDepartmentToday(Dep.FIRST);
        shift.setStartTime(LocalTime.of(8, 0));
        shift.setEndTime(LocalTime.of(20, 0));
        shift.setCountOfStatements(2);
        shift.setCountOfClaims(3);
        shift.setIbdWithMigrant(4);
        shift.setIbdWithoutMigrant(5);
        Set<ServiceInfo> units = new HashSet<>();
        units.add(serviceInfo);
        shift.setUnits(units);
        shift = dataManager.save(shift);
        createdShiftId = shift.getId();

        Vocation vocation = dataManager.create(Vocation.class);
        vocation.setUserServiceInfo(serviceInfo);
        vocation.setType(VocationType.MAIN);
        vocation.setStartDate(LocalDate.of(2025, 7, 1));
        vocation.setEndDate(LocalDate.of(2025, 7, 14));
        vocation.setCountOfDays(14);
        dataManager.save(vocation);

        UserTelegramBinding binding = dataManager.create(UserTelegramBinding.class);
        binding.setUser(user);
        binding.setChatId(CHAT_ID);
        binding.setRegisteredAt(OffsetDateTime.now(ZoneOffset.UTC));
        dataManager.save(binding);

        ServiceInfo siReload = dataManager.load(ServiceInfo.class).id(serviceInfo.getId()).one();
        siReload.setVacationDaysEntitled(40);
        siReload.setVacationDaysAvailable(28);
        dataManager.save(siReload);
    }

    private UUID persistActiveColleagueInDepartment(Department department) {
        User colleague = dataManager.create(User.class);
        PreTestEntities.updateUser(colleague);
        colleague.setArmyService(ArmyService.NOT_SERVED);

        ServiceInfo colleagueSi = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(colleagueSi);
        colleagueSi.setUser(colleague);
        colleague.setServiceInfo(colleagueSi);
        colleagueSi.setStatus(StatusInService.ACTIVE);
        colleagueSi.setDepartment(department);

        IdCard idCard = dataManager.create(IdCard.class);
        PreTestEntities.updateIdCard(idCard);
        idCard = dataManager.save(idCard);
        colleagueSi.setIdCard(idCard);

        colleague = dataManager.save(colleague);
        createdPartnerUserId = colleague.getId();

        Contacts contacts = dataManager.create(Contacts.class);
        contacts.setUser(colleague);
        dataManager.save(contacts);

        return colleague.getServiceInfo().getId();
    }

    private void persistUserBindingOnly() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setArmyService(ArmyService.NOT_SERVED);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setVacationDaysEntitled(40);
        serviceInfo.setVacationDaysAvailable(40);

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

        ServiceInfo siReload = dataManager.load(ServiceInfo.class).id(serviceInfo.getId()).one();
        siReload.setVacationDaysEntitled(40);
        siReload.setVacationDaysAvailable(40);
        dataManager.save(siReload);
    }

    @Test
    @DisplayName("GET /shifts — смены пользователя")
    void shifts_ok() throws Exception {
        persistUserShiftAndVocation();

        mockMvc.perform(get("/api/bot/me/shifts")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").exists())
                .andExpect(jsonPath("$.items[0].route").value("МП 28"))
                .andExpect(jsonPath("$.items[0].shiftType").value("Маршрут взвода"))
                .andExpect(jsonPath("$.items[0].typeOfShiftId").value("VZVOD_ROUTE"));
    }

    @Test
    @DisplayName("GET /shifts/{id} — одна смена участника")
    void getShift_ok() throws Exception {
        persistUserShiftAndVocation();

        mockMvc.perform(get("/api/bot/me/shifts/" + createdShiftId)
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdShiftId.toString()))
                .andExpect(jsonPath("$.typeOfShiftId").value("VZVOD_ROUTE"))
                .andExpect(jsonPath("$.countOfStatements").value(2));
    }

    @Test
    @DisplayName("PUT /shifts/{id} — без счётчиков в JSON не затирают БД")
    void putShift_preservesCountersWhenOmitted() throws Exception {
        persistUserShiftAndVocation();

        String json = """
                {"date":"2026-06-01","routeId":"МП 32","typeOfShiftId":"BAT_POST","startTime":"10:00","endTime":"22:00"}
                """;

        mockMvc.perform(put("/api/bot/me/shifts/" + createdShiftId)
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countOfStatements").value(2))
                .andExpect(jsonPath("$.countOfClaims").value(3));
    }

    @Test
    @DisplayName("GET /vacations — баланс и записи отпусков")
    void vacations_ok() throws Exception {
        persistUserShiftAndVocation();

        mockMvc.perform(get("/api/bot/me/vacations")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance.entitled").value(40))
                .andExpect(jsonPath("$.balance.available").value(28))
                .andExpect(jsonPath("$.balance.used").value(12))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].typeLabel").value("Основной"));
    }

    @Test
    @DisplayName("POST /shifts — создание смены (без времени окончания, с напарником)")
    void postShift_created() throws Exception {
        persistUserBindingOnly();
        final UUID[] partnerSid = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            Department dep = bound.getServiceInfo().getDepartment();
            partnerSid[0] = persistActiveColleagueInDepartment(dep);
        });

        BotShiftUpsertRequest req = new BotShiftUpsertRequest(
                LocalDate.of(2026, 5, 10),
                "МП 31",
                "VZVOD_ROUTE",
                LocalTime.of(8, 0),
                null,
                partnerSid[0],
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/bot/me/shifts")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.route").value("МП 31"))
                .andExpect(jsonPath("$.shiftType").value("Маршрут взвода"))
                .andExpect(jsonPath("$.partnerServiceInfoId").value(partnerSid[0].toString()))
                .andExpect(jsonPath("$.otherParticipantServiceInfoIds.length()").value(1))
                .andExpect(jsonPath("$.otherParticipantServiceInfoIds[0]").value(partnerSid[0].toString()))
                .andExpect(jsonPath("$.endTime").doesNotExist());
    }

    @Test
    @DisplayName("POST /shifts/{id}/end-time — задать окончание открытой смены")
    void postShiftEndTime_ok() throws Exception {
        persistUserBindingOnly();
        final UUID[] partnerSid = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            Department dep = bound.getServiceInfo().getDepartment();
            partnerSid[0] = persistActiveColleagueInDepartment(dep);
        });

        BotShiftUpsertRequest req = new BotShiftUpsertRequest(
                LocalDate.of(2026, 5, 11),
                "МП 31",
                "VZVOD_ROUTE",
                LocalTime.of(8, 0),
                null,
                partnerSid[0],
                null,
                null,
                null,
                null
        );

        MvcResult created = mockMvc.perform(post("/api/bot/me/shifts")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(created.getResponse().getContentAsString());
        UUID shiftId = UUID.fromString(node.get("id").asText());

        BotShiftEndTimeRequest endReq = new BotShiftEndTimeRequest(LocalTime.of(20, 0));
        mockMvc.perform(post("/api/bot/me/shifts/" + shiftId + "/end-time")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endTime").exists());
    }

    @Test
    @DisplayName("POST /shifts/{id}/end-time — 409 если окончание уже задано")
    void postShiftEndTime_conflict() throws Exception {
        persistUserShiftAndVocation();
        BotShiftEndTimeRequest endReq = new BotShiftEndTimeRequest(LocalTime.of(23, 0));
        mockMvc.perform(post("/api/bot/me/shifts/" + createdShiftId + "/end-time")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endReq)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /shifts/{id}/end-time — 400 если окончание не позже начала")
    void postShiftEndTime_badEndBeforeStart() throws Exception {
        persistUserBindingOnly();
        final UUID[] partnerSid = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            Department dep = bound.getServiceInfo().getDepartment();
            partnerSid[0] = persistActiveColleagueInDepartment(dep);
        });

        BotShiftUpsertRequest req = new BotShiftUpsertRequest(
                LocalDate.of(2026, 5, 12),
                "МП 31",
                "VZVOD_ROUTE",
                LocalTime.of(10, 0),
                null,
                partnerSid[0],
                null,
                null,
                null,
                null
        );

        MvcResult created = mockMvc.perform(post("/api/bot/me/shifts")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID shiftId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

        BotShiftEndTimeRequest endReq = new BotShiftEndTimeRequest(LocalTime.of(9, 0));
        mockMvc.perform(post("/api/bot/me/shifts/" + shiftId + "/end-time")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void colleagues_ok() throws Exception {
        persistUserBindingOnly();
        final UUID[] colleagueSiId = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            Department dep = bound.getServiceInfo().getDepartment();
            colleagueSiId[0] = persistActiveColleagueInDepartment(dep);
        });

        MvcResult result = mockMvc.perform(get("/api/bot/me/colleagues")
                        .param("department", "1")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode items = objectMapper.readTree(result.getResponse().getContentAsString()).get("items");
        assertThat(items).anySatisfy(item -> {
            assertThat(item.get("serviceInfoId").asText()).isEqualTo(colleagueSiId[0].toString());
            assertThat(item.get("label").asText()).isEqualTo("Пётр П.");
        });
    }

    @Test
    @DisplayName("PUT /shifts/{id} — обновление своей смены")
    void putShift_updates() throws Exception {
        persistUserShiftAndVocation();

        BotShiftUpsertRequest req = new BotShiftUpsertRequest(
                LocalDate.of(2026, 6, 1),
                "МП 32",
                TypeOfShift.BAT_POST.getId(),
                LocalTime.of(10, 0),
                LocalTime.of(22, 0),
                null,
                1,
                2,
                0,
                0
        );

        mockMvc.perform(put("/api/bot/me/shifts/" + createdShiftId)
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdShiftId.toString()))
                .andExpect(jsonPath("$.route").value("МП 32"))
                .andExpect(jsonPath("$.shiftType").value("Пост батальона"));
    }

    @Test
    @DisplayName("PUT /shifts/{id} — добавление напарника к смене только с текущим пользователем")
    void putShift_addPartner() throws Exception {
        persistUserShiftAndVocation();
        final UUID[] partnerSi = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            Department dep = bound.getServiceInfo().getDepartment();
            partnerSi[0] = persistActiveColleagueInDepartment(dep);
        });

        BotShiftUpsertRequest req = new BotShiftUpsertRequest(
                LocalDate.of(2026, 1, 15),
                NumberOfShift._28.getId(),
                TypeOfShift.VZVOD_ROUTE.getId(),
                LocalTime.of(8, 0),
                LocalTime.of(20, 0),
                partnerSi[0],
                null,
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/bot/me/shifts/" + createdShiftId)
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partnerServiceInfoId").value(partnerSi[0].toString()))
                .andExpect(jsonPath("$.otherParticipantServiceInfoIds.length()").value(1))
                .andExpect(jsonPath("$.otherParticipantServiceInfoIds[0]").value(partnerSi[0].toString()));
    }

    @Test
    @DisplayName("PUT /shifts/{id} — добавление второго напарника не удаляет первого")
    void putShift_appendsSecondParticipant() throws Exception {
        persistUserBindingOnly();
        final UUID[] p1 = new UUID[1];
        final UUID[] p2 = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            Department dep = bound.getServiceInfo().getDepartment();
            p1[0] = persistActiveColleagueInDepartment(dep);
            p2[0] = persistActiveColleagueInDepartment(dep);
        });

        BotShiftUpsertRequest createReq = new BotShiftUpsertRequest(
                LocalDate.of(2026, 5, 18),
                "МП 31",
                "VZVOD_ROUTE",
                LocalTime.of(8, 0),
                null,
                p1[0],
                null,
                null,
                null,
                null
        );

        MvcResult created = mockMvc.perform(post("/api/bot/me/shifts")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID shiftId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

        BotShiftUpsertRequest addSecond = new BotShiftUpsertRequest(
                LocalDate.of(2026, 5, 18),
                "МП 31",
                "VZVOD_ROUTE",
                LocalTime.of(8, 0),
                null,
                p2[0],
                null,
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/bot/me/shifts/" + shiftId)
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addSecond)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.otherParticipantServiceInfoIds.length()").value(2));
    }

    @Test
    @DisplayName("PUT /shifts/{id} — нельзя добавить в смену того же участника повторно")
    void putShift_duplicateParticipantReturns400() throws Exception {
        persistUserBindingOnly();
        final UUID[] partnerSid = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            Department dep = bound.getServiceInfo().getDepartment();
            partnerSid[0] = persistActiveColleagueInDepartment(dep);
        });

        BotShiftUpsertRequest req = new BotShiftUpsertRequest(
                LocalDate.of(2026, 5, 10),
                "МП 31",
                "VZVOD_ROUTE",
                LocalTime.of(8, 0),
                null,
                partnerSid[0],
                null,
                null,
                null,
                null
        );

        MvcResult created = mockMvc.perform(post("/api/bot/me/shifts")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID shiftId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(put("/api/bot/me/shifts/" + shiftId)
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /shifts — 400 если у создателя уже есть незавершённая смена")
    void postShift_openShiftCreatorReturns400() throws Exception {
        persistUserBindingOnly();
        final UUID[] partnerSid = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            Department dep = bound.getServiceInfo().getDepartment();
            partnerSid[0] = persistActiveColleagueInDepartment(dep);

            ServiceInfo creatorSi = bound.getServiceInfo();
            Shift open = dataManager.create(Shift.class);
            open.setDate(LocalDate.of(2026, 5, 20));
            open.setNumber(NumberOfShift._31);
            open.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
            open.setDepartmentToday(Dep.FIRST);
            open.setStartTime(LocalTime.of(8, 0));
            Set<ServiceInfo> units = new HashSet<>();
            units.add(creatorSi);
            open.setUnits(units);
            dataManager.save(open);
        });

        BotShiftUpsertRequest req = new BotShiftUpsertRequest(
                LocalDate.of(2026, 5, 21),
                "МП 31",
                "VZVOD_ROUTE",
                LocalTime.of(9, 0),
                null,
                partnerSid[0],
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/bot/me/shifts")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /colleagues — не показывает сотрудника в другой незавершённой смене")
    void colleagues_excludesParticipantInOpenShift() throws Exception {
        persistUserBindingOnly();
        final UUID[] busyColleagueSiId = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            Department dep = bound.getServiceInfo().getDepartment();
            busyColleagueSiId[0] = persistActiveColleagueInDepartment(dep);

            ServiceInfo colleagueSi = dataManager.load(ServiceInfo.class).id(busyColleagueSiId[0]).one();
            Shift open = dataManager.create(Shift.class);
            open.setDate(LocalDate.of(2026, 5, 22));
            open.setNumber(NumberOfShift._31);
            open.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
            open.setDepartmentToday(Dep.FIRST);
            open.setStartTime(LocalTime.of(8, 0));
            Set<ServiceInfo> units = new HashSet<>();
            units.add(colleagueSi);
            open.setUnits(units);
            dataManager.save(open);
        });

        MvcResult result = mockMvc.perform(get("/api/bot/me/colleagues")
                        .param("department", "1")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode items = objectMapper.readTree(result.getResponse().getContentAsString()).get("items");
        assertThat(items).noneMatch(item ->
                busyColleagueSiId[0].toString().equals(item.get("serviceInfoId").asText()));
    }

    @Test
    @DisplayName("POST /shifts/{id}/ibd-with-migrant — инкремент и декремент на открытой смене")
    void postShift_adjustIbdWithMigrant() throws Exception {
        persistUserBindingOnly();
        final UUID[] shiftId = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            ServiceInfo si = bound.getServiceInfo();
            Shift open = dataManager.create(Shift.class);
            open.setDate(LocalDate.of(2026, 5, 23));
            open.setNumber(NumberOfShift._31);
            open.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
            open.setDepartmentToday(Dep.FIRST);
            open.setStartTime(LocalTime.of(8, 0));
            open.setIbdWithMigrant(0);
            Set<ServiceInfo> units = new HashSet<>();
            units.add(si);
            open.setUnits(units);
            shiftId[0] = dataManager.save(open).getId();
        });

        mockMvc.perform(post("/api/bot/me/shifts/" + shiftId[0] + "/ibd-with-migrant")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ibdWithMigrant").value(1));

        mockMvc.perform(post("/api/bot/me/shifts/" + shiftId[0] + "/ibd-with-migrant")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":-1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ibdWithMigrant").value(0));
    }

    @Test
    @DisplayName("POST /shifts/{id}/count-of-statements — инкремент и декремент на открытой смене")
    void postShift_adjustCountOfStatements() throws Exception {
        persistUserBindingOnly();
        final UUID[] shiftId = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            ServiceInfo si = bound.getServiceInfo();
            Shift open = dataManager.create(Shift.class);
            open.setDate(LocalDate.of(2026, 5, 24));
            open.setNumber(NumberOfShift._31);
            open.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
            open.setDepartmentToday(Dep.FIRST);
            open.setStartTime(LocalTime.of(8, 0));
            open.setCountOfStatements(0);
            Set<ServiceInfo> units = new HashSet<>();
            units.add(si);
            open.setUnits(units);
            shiftId[0] = dataManager.save(open).getId();
        });

        mockMvc.perform(post("/api/bot/me/shifts/" + shiftId[0] + "/count-of-statements")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countOfStatements").value(1));

        mockMvc.perform(post("/api/bot/me/shifts/" + shiftId[0] + "/count-of-statements")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":-1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countOfStatements").value(0));
    }

    @Test
    @DisplayName("POST /shifts/{id}/administrative-violations — создание записи с полями")
    void postShift_createAdministrativeViolation() throws Exception {
        persistUserBindingOnly();
        final UUID[] shiftId = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            ServiceInfo si = bound.getServiceInfo();
            Shift open = dataManager.create(Shift.class);
            open.setDate(LocalDate.of(2026, 5, 24));
            open.setNumber(NumberOfShift._31);
            open.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
            open.setDepartmentToday(Dep.FIRST);
            open.setStartTime(LocalTime.of(8, 0));
            Set<ServiceInfo> units = new HashSet<>();
            units.add(si);
            open.setUnits(units);
            shiftId[0] = dataManager.save(open).getId();
        });

        mockMvc.perform(post("/api/bot/me/shifts/" + shiftId[0] + "/administrative-violations")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"impactId\":0,\"articleId\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.administrativeViolationsCount").value(1));
    }

    @Test
    @DisplayName("POST /shifts/{id}/criminal-violations — создание записи с полями")
    void postShift_createCriminalViolation() throws Exception {
        persistUserBindingOnly();
        final UUID[] shiftId = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            ServiceInfo si = bound.getServiceInfo();
            Shift open = dataManager.create(Shift.class);
            open.setDate(LocalDate.of(2026, 5, 25));
            open.setNumber(NumberOfShift._31);
            open.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
            open.setDepartmentToday(Dep.FIRST);
            open.setStartTime(LocalTime.of(8, 0));
            Set<ServiceInfo> units = new HashSet<>();
            units.add(si);
            open.setUnits(units);
            shiftId[0] = dataManager.save(open).getId();
        });

        mockMvc.perform(post("/api/bot/me/shifts/" + shiftId[0] + "/criminal-violations")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"impactId\":0,\"typeId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criminalViolationsCount").value(1));
    }
}
