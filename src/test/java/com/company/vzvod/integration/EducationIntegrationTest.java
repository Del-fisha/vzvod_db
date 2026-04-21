package com.company.vzvod.integration;

import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.EducationStatus;
import com.company.vzvod.entity.TypeOfEducation;
import com.company.vzvod.entity.User;
import com.company.vzvod.test_support.PreTestEntities;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест Education")
public class EducationIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private Education education;

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

        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
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
