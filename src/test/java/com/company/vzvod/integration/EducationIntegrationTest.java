package com.company.vzvod.integration;

import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.EducationStatus;
import com.company.vzvod.entity.TypeOfEducation;
import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(properties = "spring.profiles.active=test-postgres")
@DisplayName("Интеграционный тест Education")
public class EducationIntegrationTest {

    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withPassword("test")
            .withUsername("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private Education education;

    @BeforeAll
    static void startContainer() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        education = dataManager.create(Education.class);
        education.setStarted(LocalDate.now().minusYears(15));
        education.setUntil(LocalDate.now().minusYears(10));
        education.setNameOfInstitution("ПТПП");
        education.setType(TypeOfEducation.SPECIFIC);
        education.setStatus(EducationStatus.FINISHED);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        Education savedEducation = dataManager.save(education);
        UUID educationId = savedEducation.getId();
        assertNotNull(educationId);

        Education loadedEducation = dataManager.load(Education.class).id(educationId).one();

        assertEquals(loadedEducation.getStatus(), savedEducation.getStatus());
        assertEquals(loadedEducation.getStarted(), savedEducation.getStarted());
        assertEquals(loadedEducation.getUntil(), savedEducation.getUntil());
        assertEquals(loadedEducation.getId(), savedEducation.getId());
        assertEquals(loadedEducation.getNameOfInstitution(), savedEducation.getNameOfInstitution());
        assertEquals(loadedEducation.getType(), savedEducation.getType());

        assertEquals(EducationStatus.FINISHED, loadedEducation.getStatus());
        assertEquals(TypeOfEducation.SPECIFIC, loadedEducation.getType());
        assertEquals("ПТПП", loadedEducation.getNameOfInstitution());
    }

    @Test
    @DisplayName("Тест изменения в БД")
    void updateTest() {
        Education savedEducation = dataManager.save(education);
        UUID educationId = savedEducation.getId();

        String newNameOfInstitution = "ГУСПБ";
        LocalDate newStartDate = LocalDate.now().minusMonths(4);
        LocalDate newUntilDate = LocalDate.now().minusMonths(4).plusYears(4);

        Education loadedEducation = dataManager.load(Education.class).id(educationId).one();

        loadedEducation.setStatus(EducationStatus.AT_THE_MOMENT);
        loadedEducation.setType(TypeOfEducation.UNIVERSITY);
        loadedEducation.setNameOfInstitution(newNameOfInstitution);
        loadedEducation.setStarted(newStartDate);
        loadedEducation.setUntil(newUntilDate);

        Education updatedEducation = dataManager.save(loadedEducation);

        assertEquals(loadedEducation.getStatus(), updatedEducation.getStatus());
        assertEquals(loadedEducation.getStarted(), updatedEducation.getStarted());
        assertEquals(loadedEducation.getUntil(), updatedEducation.getUntil());
        assertEquals(loadedEducation.getId(), updatedEducation.getId());
        assertEquals(loadedEducation.getNameOfInstitution(), updatedEducation.getNameOfInstitution());
        assertEquals(loadedEducation.getType(), updatedEducation.getType());

        assertEquals(EducationStatus.AT_THE_MOMENT, updatedEducation.getStatus());
        assertEquals(TypeOfEducation.UNIVERSITY, updatedEducation.getType());
        assertEquals("ГУСПБ", updatedEducation.getNameOfInstitution());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        Education savedEducation = dataManager.save(education);
        UUID educationId = savedEducation.getId();

        dataManager.remove(education);

        Education deletedEducation = dataManager.load(Education.class).id(educationId).optional().orElse(null);
        assertNull(deletedEducation);
    }

    @Test
    @DisplayName("Тест каскадного удаления")
    void cascadeDeleteTest() {

        User user = PreTestEntities.getNewUser();
        user.setEducation(education);

        User savedUser = dataManager.save(user);
        Education savedEducation = savedUser.getEducation();
        UUID savedEducationId = savedEducation.getId();

        assertEquals(education.getId(), savedEducationId);

        dataManager.remove(savedUser);

        Education loadedEducation = dataManager.load(Education.class)
                .id(savedEducationId)
                .optional()
                .orElse(null);

        assertNull(loadedEducation);
    }
}
