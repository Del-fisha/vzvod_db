package com.company.vzvod.entity;

import com.company.vzvod.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

//@ExtendWith(AuthenticatedAsAdmin.class)
@DisplayName("Интеграционный тест ServiceInfo - полный цикл")
@SpringBootTest
//@Transactional
@Testcontainers
@ActiveProfiles("test-postgres")
class ServiceInfoIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("vzvod_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.database-platform",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Validator validator;

    private User testUser;
    private ServiceInfo serviceInfo;

    @BeforeEach
    void setUp() {
        // ✅ 1. Создаём АДМИНА для @AuthenticatedAsAdmin
        User adminUser = dataManager.create(User.class);
        adminUser.setUsername("admin");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("Adminov");
        adminUser.setPassword("admin");  // ← КРИТИЧНО!
//        adminUser = dataManager.save(adminUser);

        // ✅ 2. Обычный тестовый user
        testUser = dataManager.create(User.class);
        testUser.setFirstName("Иван");
        testUser.setLastName("Петров");
        testUser.setPatronymic("Иванович");
        testUser.setDateOfBirth(LocalDate.now().minusYears(30));
        testUser.setUsername("petrov");
        testUser.setPassword("pet");
//        testUser = dataManager.save(testUser);

        // ✅ 3. ServiceInfo как раньше
        serviceInfo = dataManager.create(ServiceInfo.class);
        serviceInfo.setUser(testUser);
        serviceInfo.setToken("TEST-TOKEN-123");
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setStartDate(LocalDate.now());
//        serviceInfo = dataManager.save(serviceInfo);
    }


    @Test
    @DisplayName("CREATE + SAVE: создание и сохранение ServiceInfo")
    void testCreateAndSave() {

//        SaveContext saveContext = new SaveContext().saving(serviceInfo);
//        serviceInfo = dataManager.save(saveContext).get(serviceInfo);

        assertNotNull(serviceInfo.getId());
        assertEquals("TEST-TOKEN-123", serviceInfo.getToken());
        assertEquals(testUser, serviceInfo.getUser());
        assertEquals(StatusInService.ACTIVE, serviceInfo.getStatus());

        Set<ConstraintViolation<ServiceInfo>> violations =
                validator.validate(serviceInfo);
        assertTrue(violations.isEmpty(), "Валидация прошла");
    }

    @Test
    @DisplayName("READ: поиск по token")
    void testFindByToken() {

//        serviceInfo = dataManager.save(serviceInfo);

        List<ServiceInfo> found = dataManager.load(ServiceInfo.class)
                .query("SELECT e FROM ServiceInfo e WHERE e.token = :token")
                .parameter("token", "TEST-TOKEN-123")
                .list();

        assertEquals(1, found.size());
        assertEquals(serviceInfo.getId(), found.getFirst().getId());
    }


    @Test
    @DisplayName("UPDATE: обновление полей после сохранения")
    void testUpdateFields() {

//        serviceInfo = dataManager.save(serviceInfo);
        UUID savedId = serviceInfo.getId();

        serviceInfo.setRank(Rank.CAPTAIN);
        serviceInfo.setPost(Post.COM_VZVOD);
        serviceInfo.setBreastplate("00123456");
        serviceInfo.setMedicalExamination(true);
        serviceInfo.setQualificationClass(Qualification.FIRST);
        serviceInfo = dataManager.save(serviceInfo);

        assertEquals(savedId, serviceInfo.getId());
        assertEquals(Rank.CAPTAIN, serviceInfo.getRank());
        assertEquals(Post.COM_VZVOD, serviceInfo.getPost());
        assertEquals("00123456", serviceInfo.getBreastplate());
        assertTrue(serviceInfo.getMedicalExamination());
    }

    @Test
    @DisplayName("VALIDATION: проверка @PastOrPresent на будущую дату")
    void testPastOrPresentValidation() {
        serviceInfo.setStartDate(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<ServiceInfo>> violations =
                validator.validate(serviceInfo);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("startDate")));
    }

    @Test
    @DisplayName("CREATE с композицией IdCard")
    void testCreateWithIdCard() {
        IdCard idCard = dataManager.create(IdCard.class);
        idCard.setSpl("123456");
        idCard.setIssued(LocalDate.now().minusMonths(11));
        idCard.setUntil(idCard.getIssued().plusYears(4));
//        idCard = dataManager.save(idCard);


        Department dept = dataManager.create(Department.class);
        dept.setNumber(1);
        dept = dataManager.save(dept);

        serviceInfo.setIdCard(idCard);
        serviceInfo.setDepartment(dept);

        serviceInfo = dataManager.save(serviceInfo);

        assertNotNull(serviceInfo.getIdCard());
    }

    @Test
    @DisplayName("CREATE с manyToOne Department")
    void testCreateWithDepartment() {
        Department department = dataManager.create(Department.class);
        department.setNumber(1);
        department = dataManager.save(department);

        ServiceInfo newServiceInfo = dataManager.create(ServiceInfo.class);
        newServiceInfo.setUser(testUser);
        newServiceInfo.setToken("NEW-TOKEN");
        newServiceInfo.setStatus(StatusInService.ACTIVE);
        newServiceInfo.setStartDate(LocalDate.now());

        newServiceInfo.setDepartment(department);
        department.getServiceInfos().add(newServiceInfo);

        department = dataManager.save(department);

        assertNotNull(newServiceInfo.getDepartment());
        assertTrue(department.getServiceInfos().contains(newServiceInfo));
    }


}
