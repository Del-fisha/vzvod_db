package com.company.vzvod.integration;

import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.EducationStatus;
import com.company.vzvod.entity.TypeOfEducation;
import com.company.vzvod.entity.User;
import com.company.vzvod.service.UserDialogSaveService;
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
@DisplayName("Интеграционный тест upsert User из диалога")
public class UserDialogSaveIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private UserDialogSaveService userDialogSaveService;

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
    @DisplayName("Если id уже есть в БД, saveFromDialog делает UPDATE, а не INSERT (без USER__pkey)")
    void existingId_updatesNotInserts() {
        User u = dataManager.create(User.class);
        PreTestEntities.updateUser(u);
        u = dataManager.save(u);

        UUID id = u.getId();
        assertNotNull(id);

        // Simulate UI bug: a "new" instance that carries an existing id
        User edited = dataManager.create(User.class);
        PreTestEntities.updateUser(edited);
        edited.setId(id);
        edited.setLastName("НОВАЯ_ФАМИЛИЯ");

        User saved = userDialogSaveService.saveFromDialog(edited);
        assertEquals(id, saved.getId());

        User loaded = dataManager.load(User.class).id(id).one();
        assertEquals("НОВАЯ_ФАМИЛИЯ", loaded.getLastName());
    }

    @Test
    @DisplayName("saveFromDialog сохраняет связанное Education при UPDATE User")
    void existingId_updatesEducation() {
        User u = dataManager.create(User.class);
        PreTestEntities.updateUser(u);
        u = dataManager.save(u);
        createdUserId = u.getId();

        Education education = dataManager.create(Education.class);
        education.setStarted(LocalDate.now().minusYears(4));
        education.setUntil(LocalDate.now().plusYears(2));
        education.setNameOfInstitution("Институт");
        education.setType(TypeOfEducation.UNIVERSITY);
        education = dataManager.save(education);

        u.setEducation(education);
        u = dataManager.save(u);

        User edited = dataManager.create(User.class);
        PreTestEntities.updateUser(edited);
        edited.setId(u.getId());

        Education editedEducation = dataManager.load(Education.class).id(education.getId()).one();
        editedEducation.setNameOfInstitution("Университет");
        edited.setEducation(editedEducation);

        userDialogSaveService.saveFromDialog(edited);

        User loaded = dataManager.load(User.class).id(u.getId()).one();
        assertNotNull(loaded.getEducation());
        assertEquals("Университет", loaded.getEducation().getNameOfInstitution());
        assertEquals(EducationStatus.AT_THE_MOMENT, loaded.getEducation().getStatus());
    }
}

