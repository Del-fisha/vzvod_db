package com.company.vzvod.integration;

import com.company.vzvod.entity.Department;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Интеграционный тест Department")
public class DepartmentIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private Department department;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        department = dataManager.create(Department.class);
        PreTestEntities.updateDepartment(department);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        Department saved = dataManager.save(department);
        UUID id = saved.getId();
        assertNotNull(id);

        Department loaded = dataManager.load(Department.class).id(id).one();
        assertEquals(saved.getNumber(), loaded.getNumber());
    }

    @Test
    @DisplayName("Тест изменения в БД")
    void testUpdate() {
        Department saved = dataManager.save(department);
        UUID id = saved.getId();

        Department loaded = dataManager.load(Department.class).id(id).one();
        loaded.setNumber(2);
        Department updated = dataManager.save(loaded);

        assertEquals(2, updated.getNumber());
        assertEquals(id, updated.getId());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        Department saved = dataManager.save(department);
        UUID id = saved.getId();

        dataManager.remove(saved);
        Department deleted = dataManager.load(Department.class).id(id).optional().orElse(null);
        assertNull(deleted);
    }
}