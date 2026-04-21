package com.company.vzvod.integration;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.Impact;
import com.company.vzvod.entity.Shift;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест AdministrativeViolation")
public class AdministrativeViolationIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    AdministrativeViolation violation;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        Shift shift = dataManager.create(Shift.class);
        PreTestEntities.updateShift(shift);
        shift = dataManager.save(shift);

        violation = dataManager.create(AdministrativeViolation.class);
        violation.setArticle(ArticleOfAdministrative._18_8);
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
        AdministrativeViolation savedViolation = dataManager.save(violation);
        UUID savedViolationId = savedViolation.getId();
        assertNotNull(savedViolationId);

        AdministrativeViolation loadedViolation = dataManager.load(AdministrativeViolation.class)
                .id(savedViolationId)
                .one();

        assertEquals(savedViolationId, loadedViolation.getId());
        assertEquals(savedViolation.getShift(), loadedViolation.getShift());
        assertEquals(savedViolation.getArticle(), loadedViolation.getArticle());
        assertEquals(savedViolation.getImpact(), loadedViolation.getImpact());

        assertEquals(Impact.WITHOUT_IMPACT, loadedViolation.getImpact());
    }

    @Test
    @DisplayName("Тест изменения в БД")
    void updateTest() {
        AdministrativeViolation savedViolation = dataManager.save(violation);
        UUID savedViolationId = savedViolation.getId();

        AdministrativeViolation loadedViolation = dataManager.load(AdministrativeViolation.class)
                .id(savedViolationId)
                .one();

        loadedViolation.setShift(dataManager.create(Shift.class));
        loadedViolation.setImpact(Impact.SPECIAL_TOOLS);
        loadedViolation.setArticle(ArticleOfAdministrative._20_1_2);

        AdministrativeViolation updatedViolation = dataManager.save(loadedViolation);

        assertEquals(loadedViolation.getId(), updatedViolation.getId());
        assertEquals(loadedViolation.getImpact(), updatedViolation.getImpact());
        assertEquals(loadedViolation.getArticle(), updatedViolation.getArticle());
        assertEquals(loadedViolation.getShift(), updatedViolation.getShift());

        assertEquals(Impact.SPECIAL_TOOLS, updatedViolation.getImpact());
        assertEquals(ArticleOfAdministrative._20_1_2, updatedViolation.getArticle());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        AdministrativeViolation savedViolation = dataManager.save(violation);
        UUID savedViolationId = savedViolation.getId();

        dataManager.remove(savedViolation);

        AdministrativeViolation removedViolation = dataManager.load(AdministrativeViolation.class)
                .id(savedViolationId)
                .optional()
                .orElse(null);
        assertNull(removedViolation);
    }

    @Test
    @DisplayName("Тест UNLINK удаления")
    void unlinkDeleteTest() {
        AdministrativeViolation savedViolation = dataManager.save(violation);
        Shift shift = savedViolation.getShift();

        assertThrows(Exception.class, () -> dataManager.remove(shift),
                "Удаление Shift при наличии ссылок должно падать (FK), т.к. UNLINK не настроен");
    }


    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }
}
