package com.company.vzvod.integration;

import com.company.vzvod.entity.*;
import com.company.vzvod.notification.OverdueItemDto;
import com.company.vzvod.notification.OverdueItemType;
import com.company.vzvod.notification.OverdueNotificationRequest;
import com.company.vzvod.notification.UserNotificationService;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест уведомлений (просрочки)")
public class UserNotificationIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    UserNotificationService userNotificationService;

    private UUID dep1Id;
    private UUID dep2Id;

    private UUID userAId; // COM_OTD dep1
    private UUID userBId; // COM_OTD dep2
    private UUID userVId; // POLICEMAN dep1
    private UUID userGId; // POLICEMAN dep2
    private UUID userDId; // COM_VZVOD dep null

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        Department dep1 = dataManager.create(Department.class);
        dep1.setNumber(1);
        dep1 = dataManager.save(dep1);
        dep1Id = dep1.getId();

        Department dep2 = dataManager.create(Department.class);
        dep2.setNumber(2);
        dep2 = dataManager.save(dep2);
        dep2Id = dep2.getId();

        userAId = createUserWithServiceInfo(Post.COM_OTD, dep1);
        userBId = createUserWithServiceInfo(Post.COM_OTD, dep2);
        userVId = createUserWithServiceInfo(Post.POLICEMAN, dep1);
        userGId = createUserWithServiceInfo(Post.POLICEMAN, dep2);
        userDId = createUserWithServiceInfo(Post.COM_VZVOD, null);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("POLICEMAN: уведомление видит он и COM_OTD того же отделения")
    void policemanAndCommanderSameDepartmentSee() {
        UUID nId = userNotificationService.createOverdueNotification(
                new OverdueNotificationRequest(
                        userVId,
                        List.of(new OverdueItemDto(OverdueItemType.ID_CARD_UNTIL, LocalDate.now().plusDays(10)))
                ),
                null
        );

        assertTrue(userNotificationService.loadActiveForUser(userVId).stream().anyMatch(n -> n.getId().equals(nId)));
        assertTrue(userNotificationService.loadActiveForUser(userAId).stream().anyMatch(n -> n.getId().equals(nId)));
        assertFalse(userNotificationService.loadActiveForUser(userBId).stream().anyMatch(n -> n.getId().equals(nId)));
    }

    @Test
    @DisplayName("Без отделения: уведомление видит только сам пользователь")
    void noDepartmentOnlyUserSees() {
        UUID nId = userNotificationService.createOverdueNotification(
                new OverdueNotificationRequest(
                        userDId,
                        List.of(
                                new OverdueItemDto(OverdueItemType.ID_CARD_UNTIL, LocalDate.now().plusDays(10)),
                                new OverdueItemDto(OverdueItemType.VEHICLE_INSURANCE, LocalDate.now().plusDays(10))
                        )
                ),
                null
        );

        assertTrue(userNotificationService.loadActiveForUser(userDId).stream().anyMatch(n -> n.getId().equals(nId)));
        assertFalse(userNotificationService.loadActiveForUser(userAId).stream().anyMatch(n -> n.getId().equals(nId)));
        assertFalse(userNotificationService.loadActiveForUser(userBId).stream().anyMatch(n -> n.getId().equals(nId)));
    }

    @Test
    @DisplayName("Исправлено: пропадает у всех получателей")
    void resolveHidesForAllRecipients() {
        UUID nId = userNotificationService.createOverdueNotification(
                new OverdueNotificationRequest(
                        userGId,
                        List.of(new OverdueItemDto(OverdueItemType.VEHICLE_INSURANCE, LocalDate.now().plusDays(10)))
                ),
                null
        );

        assertTrue(userNotificationService.loadActiveForUser(userGId).stream().anyMatch(n -> n.getId().equals(nId)));
        assertTrue(userNotificationService.loadActiveForUser(userBId).stream().anyMatch(n -> n.getId().equals(nId)));

        userNotificationService.resolve(nId, userBId);

        assertFalse(userNotificationService.loadActiveForUser(userGId).stream().anyMatch(n -> n.getId().equals(nId)));
        assertFalse(userNotificationService.loadActiveForUser(userBId).stream().anyMatch(n -> n.getId().equals(nId)));
    }

    private UUID createUserWithServiceInfo(Post post, Department department) {
        User u = dataManager.create(User.class);
        PreTestEntities.updateUser(u);
        u = dataManager.save(u);

        IdCard idCard = dataManager.create(IdCard.class);
        PreTestEntities.updateIdCard(idCard);
        idCard = dataManager.save(idCard);

        ServiceInfo si = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(si);
        si.setUser(u);
        si.setPost(post);
        si.setDepartment(department);
        si.setIdCard(idCard);

        dataManager.save(si);
        return u.getId();
    }
}

