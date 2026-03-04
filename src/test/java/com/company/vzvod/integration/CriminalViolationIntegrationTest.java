package com.company.vzvod.integration;

import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfCriminal;
import com.company.vzvod.entity.Impact;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(properties = "spring.profiles.active=test-postgres")
@Testcontainers
@DisplayName("Интеграционный тест CriminalViolation")
public class CriminalViolationIntegrationTest {

    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @BeforeAll
    static void startContainer() {
        postgreSQLContainer.start();
    }

    CriminalViolation violation;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        Shift shift = PreTestEntities.getNewShift();
        shift = dataManager.save(shift);

        violation = dataManager.create(CriminalViolation.class);
        violation.setType(TypeOfCriminal.FEDERAL_WANTED);
        violation.setImpact(Impact.WITHOUT_IMPACT);
        violation.setShift(shift);
    }

    @Test
    @DisplayName("Проверка соединения")
    void connectionTest() {

    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        CriminalViolation savedViolation = dataManager.save(violation);
        UUID savedViolationId = savedViolation.getId();
        assertNotNull(savedViolationId);

        CriminalViolation loadedViolation = dataManager.load(CriminalViolation.class)
                .id(savedViolationId)
                .one();

        assertEquals(savedViolationId, loadedViolation.getId());
        assertEquals(savedViolation.getShift(), loadedViolation.getShift());
        assertEquals(savedViolation.getType(), loadedViolation.getType());
        assertEquals(savedViolation.getImpact(), loadedViolation.getImpact());

        assertEquals(Impact.WITHOUT_IMPACT, loadedViolation.getImpact());
    }

    @Test
    @DisplayName("Тест изменения в БД")
    void updateTest() {
        CriminalViolation savedViolation = dataManager.save(violation);
        UUID savedViolationId = savedViolation.getId();

        CriminalViolation loadedViolation = dataManager.load(CriminalViolation.class)
                .id(savedViolationId)
                .one();

        loadedViolation.setShift(dataManager.create(Shift.class));
        loadedViolation.setImpact(Impact.SPECIAL_TOOLS);
        loadedViolation.setType(TypeOfCriminal.WATCH_LIST);

        CriminalViolation updatedViolation = dataManager.save(loadedViolation);

        assertEquals(loadedViolation.getId(), updatedViolation.getId());
        assertEquals(loadedViolation.getImpact(), updatedViolation.getImpact());
        assertEquals(loadedViolation.getType(), updatedViolation.getType());
        assertEquals(loadedViolation.getShift(), updatedViolation.getShift());

        assertEquals(Impact.SPECIAL_TOOLS, updatedViolation.getImpact());
        assertEquals(TypeOfCriminal.WATCH_LIST, updatedViolation.getType());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        CriminalViolation savedViolation = dataManager.save(violation);
        UUID savedViolationId = savedViolation.getId();

        dataManager.remove(savedViolation);

        CriminalViolation removedViolation = dataManager.load(CriminalViolation.class)
                .id(savedViolationId)
                .optional()
                .orElse(null);
        assertNull(removedViolation);
    }

    @Test
    @DisplayName("Тест UNLINK удаления")
    void unlinkDeleteTest() {
        CriminalViolation savedViolation = dataManager.save(violation);
        Shift shift = savedViolation.getShift();

        dataManager.remove(shift);

        CriminalViolation loadedViolation = dataManager.load(CriminalViolation.class)
                .id(savedViolation.getId())
                .optional()
                .orElse(null);

        assertNotNull(loadedViolation);
        assertNull(loadedViolation.getShift());
    }


    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }
}
