package com.company.vzvod.integration;

import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.service.DepartmentConverter;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = "spring.profiles.active=test-postgres")
@Testcontainers
@DisplayName("Интеграционный тест Shift")
public class ShiftIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer("postgres:16")
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
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private DataManager dataManager;

    Shift shift;

    @BeforeAll
    static void startContainer() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        shift = dataManager.create(Shift.class);

        shift.setDepartmentToday(DepartmentConverter.departmentFromDate(LocalDate.now()));
        shift.setDate(LocalDate.now());
        shift.setStartTime(LocalTime.of(10,0));
        shift.setEndTime(LocalTime.of(22,0));
        shift.setIbdWithoutMigrant(45);
        shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shift.setCountOfClaims(1);
        shift.setCountOfStatements(2);
        shift.setIbdWithMigrant(60);
        shift.setNumber(NumberOfShift._28);
    }

    @Test
    void testConnection() {
    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        Shift savedShift = dataManager.save(shift);
        UUID savedShiftId = savedShift.getId();
        assertNotNull(savedShiftId);

        Shift loadedShift = dataManager.load(Shift.class).id(savedShiftId).one();

        assertEquals(savedShift.getIbdWithoutMigrant(), loadedShift.getIbdWithoutMigrant());
        assertEquals(savedShift.getCountOfStatements(), loadedShift.getCountOfStatements());
        assertEquals(savedShift.getDepartmentToday(), loadedShift.getDepartmentToday());
        assertEquals(savedShift.getIbdWithMigrant(), loadedShift.getIbdWithMigrant());
        assertEquals(savedShift.getCountOfClaims(), loadedShift.getCountOfClaims());
        assertEquals(savedShift.getTypeOfShift(), loadedShift.getTypeOfShift());
        assertEquals(savedShift.getStartTime(), loadedShift.getStartTime());
        assertEquals(savedShift.getEndTime(), loadedShift.getEndTime());
        assertEquals(savedShift.getNumber(), loadedShift.getNumber());
        assertEquals(savedShift.getDate(), loadedShift.getDate());
        assertEquals(savedShiftId, loadedShift.getId());
    }


    @Test
    @DisplayName("Тест изменения в БД")
    void updateTest() {
        Shift savedShift = dataManager.save(shift);
        UUID savedShiftId = savedShift.getId();
        assertNotNull(savedShiftId);

        Shift loadedShift = dataManager.load(Shift.class).id(savedShiftId).one();

        loadedShift.setDate(LocalDate.now().minusDays(2));
        loadedShift.setStartTime(LocalTime.of(9,0));
        loadedShift.setEndTime(LocalTime.of(21,0));
        loadedShift.setTypeOfShift(TypeOfShift.BAT_POST);
        loadedShift.setDepartmentToday(Dep.FIRST);
        loadedShift.setNumber(NumberOfShift._6);
        loadedShift.setIbdWithoutMigrant(30);
        loadedShift.setCountOfStatements(3);
        loadedShift.setIbdWithMigrant(66);
        loadedShift.setCountOfClaims(0);

        Shift saveLoadedShift = dataManager.save(loadedShift);
        Shift updatedShift = dataManager.load(Shift.class).id(saveLoadedShift.getId()).one();

        assertEquals(loadedShift.getIbdWithoutMigrant(), updatedShift.getIbdWithoutMigrant());
        assertEquals(loadedShift.getCountOfStatements(), updatedShift.getCountOfStatements());
        assertEquals(loadedShift.getDepartmentToday(), updatedShift.getDepartmentToday());
        assertEquals(loadedShift.getIbdWithMigrant(), updatedShift.getIbdWithMigrant());
        assertEquals(loadedShift.getCountOfClaims(), updatedShift.getCountOfClaims());
        assertEquals(loadedShift.getTypeOfShift(), updatedShift.getTypeOfShift());
        assertEquals(loadedShift.getStartTime(), updatedShift.getStartTime());
        assertEquals(loadedShift.getEndTime(), updatedShift.getEndTime());
        assertEquals(loadedShift.getNumber(), updatedShift.getNumber());
        assertEquals(loadedShift.getDate(), updatedShift.getDate());
        assertEquals(loadedShift.getId(), updatedShift.getId());


        assertEquals(TypeOfShift.BAT_POST, updatedShift.getTypeOfShift());
        assertEquals(30, updatedShift.getIbdWithoutMigrant());
        assertEquals(3, updatedShift.getCountOfStatements());
        assertEquals(66, updatedShift.getIbdWithMigrant());
        assertEquals(Dep.FIRST, updatedShift.getDepartmentToday());
        assertEquals(0, updatedShift.getCountOfClaims());
        assertEquals(NumberOfShift._6, updatedShift.getNumber());

    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }
}
