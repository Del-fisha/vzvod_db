package com.company.vzvod.integration;

import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.service.ServiceInfoDialogSaveService;
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
@DisplayName("Интеграционный тест сохранения ServiceInfo из диалога")
public class ServiceInfoDialogSaveIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private ServiceInfoDialogSaveService serviceInfoDialogSaveService;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("save new User+ServiceInfo with existing IdCard without PK_ID_CARD violation")
    void testSaveNewGraphWithExistingIdCard() {
        // 1) IdCard already exists in DB (simulates user saves IdCard dialog first)
        IdCard idCard = dataManager.create(IdCard.class);
        PreTestEntities.updateIdCard(idCard);
        IdCard savedIdCard = dataManager.save(idCard);

        UUID idCardId = savedIdCard.getId();
        assertNotNull(idCardId);

        // 2) Create NEW User + NEW ServiceInfo, but reuse the detached IdCard instance
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setArmyService(ArmyService.SERVED);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);

        // wire relations exactly like UI
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);
        serviceInfo.setIdCard(savedIdCard);

        // 3) Save from "ServiceInfo dialog"
        ServiceInfo savedServiceInfo = serviceInfoDialogSaveService.saveFromDialog(serviceInfo);
        assertNotNull(savedServiceInfo.getId());
        assertNotNull(savedServiceInfo.getIdCard());
        assertEquals(idCardId, savedServiceInfo.getIdCard().getId());

        // 4) Ensure IdCard was not inserted twice
        Long cnt = dataManager.loadValue(
                        "select count(i) from IdCard i where i.id = :id",
                        Long.class
                )
                .parameter("id", idCardId)
                .one();
        assertEquals(1L, cnt);
    }
}

