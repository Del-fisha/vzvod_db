package com.company.vzvod.integration;

import com.company.vzvod.entity.*;
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
@DisplayName("Интеграционный тест ServiceInfo")
public class ServiceInfoIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private ServiceInfo serviceInfo;
    private User user;
    private Department department;
    private IdCard idCard;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user = dataManager.save(user);

        department = dataManager.create(Department.class);
        PreTestEntities.updateDepartment(department);
        department = dataManager.save(department);

        idCard = dataManager.create(IdCard.class);
        PreTestEntities.updateIdCard(idCard);

        serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        serviceInfo.setDepartment(department);
        serviceInfo.setIdCard(idCard);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        ServiceInfo saved = dataManager.save(serviceInfo);
        UUID id = saved.getId();
        assertNotNull(id);

        ServiceInfo loaded = dataManager.load(ServiceInfo.class).id(id).one();
        assertEquals(saved.getToken(), loaded.getToken());
        assertEquals(saved.getBreastplate(), loaded.getBreastplate());
        assertEquals(saved.getRank(), loaded.getRank());
        assertEquals(saved.getPost(), loaded.getPost());
        assertEquals(saved.getStatus(), loaded.getStatus());
        assertEquals(saved.getMedicalExamination(), loaded.getMedicalExamination());
        assertEquals(saved.getQualificationClass(), loaded.getQualificationClass());
        assertEquals(saved.getUser().getId(), loaded.getUser().getId());
        assertEquals(saved.getDepartment().getId(), loaded.getDepartment().getId());
        assertNotNull(loaded.getIdCard());
        assertEquals(saved.getIdCard().getId(), loaded.getIdCard().getId());
    }

    @Test
    @DisplayName("Тест изменения в БД")
    void testUpdate() {
        ServiceInfo saved = dataManager.save(serviceInfo);
        UUID id = saved.getId();

        ServiceInfo loaded = dataManager.load(ServiceInfo.class).id(id).one();
        loaded.setToken("NEW_TOKEN");
        loaded.setBreastplate("99999999");
        loaded.setRank(Rank.CAPTAIN);
        loaded.setPost(Post.COM_VZVOD);
        loaded.setMedicalExamination(true);
        loaded.setQualificationClass(Qualification.MASTER);

        ServiceInfo updated = dataManager.save(loaded);
        assertEquals("NEW_TOKEN", updated.getToken());
        assertEquals("99999999", updated.getBreastplate());
        assertEquals(Rank.CAPTAIN, updated.getRank());
        assertEquals(Post.COM_VZVOD, updated.getPost());
        assertTrue(updated.getMedicalExamination());
        assertEquals(Qualification.MASTER, updated.getQualificationClass());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        ServiceInfo saved = dataManager.save(serviceInfo);
        UUID id = saved.getId();

        dataManager.remove(saved);
        ServiceInfo deleted = dataManager.load(ServiceInfo.class).id(id).optional().orElse(null);
        assertNull(deleted);
    }

    @Test
    @DisplayName("Тест каскадного удаления IdCard при удалении ServiceInfo")
    void testCascadeDeleteIdCard() {
        ServiceInfo saved = dataManager.save(serviceInfo);
        UUID idCardId = saved.getIdCard().getId();

        dataManager.remove(saved);
        IdCard deletedIdCard = dataManager.load(IdCard.class).id(idCardId).optional().orElse(null);
        assertNull(deletedIdCard, "IdCard должен быть удалён каскадно");
    }
}