package com.company.vzvod.integration;

import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.EducationStatus;
import com.company.vzvod.entity.TypeOfEducation;
import com.company.vzvod.entity.User;
import com.company.vzvod.service.EducationDialogSaveService;
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
@DisplayName("Интеграционный тест сохранения Education из диалога")
class EducationDialogSaveIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private EducationDialogSaveService educationDialogSaveService;

    private UUID createdUserId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        if (createdUserId != null) {
            dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove);
            createdUserId = null;
        }
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("saveFromDialog сохраняет поля и привязывает Education к User")
    void saveFromDialog_persistsEducationAndLinksToUser() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user = dataManager.save(user);
        createdUserId = user.getId();

        Education edited = dataManager.create(Education.class);
        edited.setStarted(LocalDate.now().minusYears(4));
        edited.setUntil(LocalDate.now().plusYears(1));
        edited.setNameOfInstitution("Академия");
        edited.setType(TypeOfEducation.UNIVERSITY);

        Education saved = educationDialogSaveService.saveFromDialog(edited);
        assertNotNull(saved.getId());
        assertEquals(EducationStatus.AT_THE_MOMENT, saved.getStatus());

        user.setEducation(saved);
        dataManager.save(user);

        User loaded = dataManager.load(User.class).id(user.getId()).one();
        Education loadedEducation = loaded.getEducation();
        assertNotNull(loadedEducation);
        assertEquals("Академия", loadedEducation.getNameOfInstitution());
        assertEquals(TypeOfEducation.UNIVERSITY, loadedEducation.getType());
        assertEquals(EducationStatus.AT_THE_MOMENT, loadedEducation.getStatus());
    }

    @Test
    @DisplayName("saveFromDialog обновляет существующую запись")
    void saveFromDialog_updatesExisting() {
        Education education = dataManager.create(Education.class);
        education.setStarted(LocalDate.now().minusYears(10));
        education.setUntil(LocalDate.now().minusYears(5));
        education.setNameOfInstitution("Старое");
        education.setType(TypeOfEducation.SPECIFIC);
        education = educationDialogSaveService.saveFromDialog(education);

        education.setNameOfInstitution("Новое");
        Education updated = educationDialogSaveService.saveFromDialog(education);

        Education loaded = dataManager.load(Education.class).id(updated.getId()).one();
        assertEquals("Новое", loaded.getNameOfInstitution());
        assertEquals(EducationStatus.FINISHED, loaded.getStatus());
    }
}
