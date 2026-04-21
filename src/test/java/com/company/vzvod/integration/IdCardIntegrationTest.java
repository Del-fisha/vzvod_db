package com.company.vzvod.integration;

import com.company.vzvod.entity.IdCard;
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
@DisplayName("Интеграционный тест IdCard")
public class IdCardIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private IdCard idCard;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        idCard = dataManager.create(IdCard.class);
        PreTestEntities.updateIdCard(idCard);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        IdCard saved = dataManager.save(idCard);
        UUID id = saved.getId();
        assertNotNull(id);

        IdCard loaded = dataManager.load(IdCard.class).id(id).one();
        assertEquals(saved.getSpl(), loaded.getSpl());
        assertEquals(saved.getIssued(), loaded.getIssued());
        assertEquals(saved.getUntil(), loaded.getUntil());
    }

    @Test
    @DisplayName("Тест изменения в БД")
    void testUpdate() {
        IdCard saved = dataManager.save(idCard);
        UUID id = saved.getId();

        IdCard loaded = dataManager.load(IdCard.class).id(id).one();
        loaded.setSpl("654321");
        loaded.setIssued(LocalDate.now().minusYears(2));
        loaded.setUntil(LocalDate.now().plusYears(5));

        IdCard updated = dataManager.save(loaded);
        assertEquals("654321", updated.getSpl());
        assertEquals(id, updated.getId());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        IdCard saved = dataManager.save(idCard);
        UUID id = saved.getId();

        dataManager.remove(saved);
        IdCard deleted = dataManager.load(IdCard.class).id(id).optional().orElse(null);
        assertNull(deleted);
    }
}