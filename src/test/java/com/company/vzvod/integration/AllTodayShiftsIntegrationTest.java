package com.company.vzvod.integration;

import com.company.vzvod.entity.AllTodayShifts;
import com.company.vzvod.entity.Dep;
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
@DisplayName("Интеграционный тест AllTodayShifts")
class AllTodayShiftsIntegrationTest {

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private DataManager dataManager;

    private AllTodayShifts entity;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        entity = dataManager.create(AllTodayShifts.class);
        entity.setDate(LocalDate.of(2098, 6, 15));
        entity.setDepartment(Dep.SECOND);
    }

    @AfterEach
    void tearDown() {
        if (entity != null && entity.getId() != null) {
            dataManager.load(AllTodayShifts.class)
                    .id(entity.getId())
                    .optional()
                    .ifPresent(dataManager::remove);
        }
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Сохранение и чтение даты и отделения")
    void saveAndLoad() {
        AllTodayShifts saved = dataManager.save(entity);
        UUID id = saved.getId();
        assertNotNull(id);

        AllTodayShifts loaded = dataManager.load(AllTodayShifts.class).id(id).one();
        assertEquals(LocalDate.of(2098, 6, 15), loaded.getDate());
        assertEquals(Dep.SECOND, loaded.getDepartment());
        assertEquals(id, loaded.getId());
    }
}
