package com.company.vzvod.messaging;

import com.company.vzvod.entity.*;
import com.company.vzvod.service.DepartmentConverter;
import com.company.vzvod.service.ShiftOperationalDay;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Получатели дашборд-сообщения")
class DashboardMessageRecipientResolverTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    DashboardMessageRecipientResolver recipientResolver;

    private LocalDate operationalDate;
    private Department dep1;
    private Department dep2;
    private UUID senderId;
    private UUID shiftWorkerDep1Id;
    private UUID shiftWorkerDep2Id;
    private UUID departmentWorkerDep1Id;
    private UUID commanderDep1Id;
    private UUID commanderDep2Id;
    private UUID comVzvodId;
    private UUID inactiveWorkerId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        operationalDate = ShiftOperationalDay.resolveOperationalDate(LocalDateTime.now(), ZoneId.systemDefault());

        dep1 = saveDepartment(1);
        dep2 = saveDepartment(2);

        senderId = createUserWithServiceInfo(Post.COM_OTD, dep1, StatusInService.ACTIVE);
        shiftWorkerDep1Id = createUserWithServiceInfo(Post.POLICEMAN, dep1, StatusInService.ACTIVE);
        shiftWorkerDep2Id = createUserWithServiceInfo(Post.POLICEMAN, dep2, StatusInService.ACTIVE);
        departmentWorkerDep1Id = createUserWithServiceInfo(Post.INTERN, dep1, StatusInService.ACTIVE);
        commanderDep1Id = createUserWithServiceInfo(Post.COM_OTD, dep1, StatusInService.ACTIVE);
        commanderDep2Id = createUserWithServiceInfo(Post.COM_OTD, dep2, StatusInService.ACTIVE);
        comVzvodId = createUserWithServiceInfo(Post.COM_VZVOD, null, StatusInService.ACTIVE);
        inactiveWorkerId = createUserWithServiceInfo(Post.POLICEMAN, dep1, StatusInService.VOCATION);

        Shift shiftDep1 = dataManager.create(Shift.class);
        shiftDep1.setDate(operationalDate);
        shiftDep1.setNumber(NumberOfShift._28);
        shiftDep1.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shiftDep1.setDepartmentToday(DepartmentConverter.departmentFromDate(operationalDate));
        shiftDep1.setUnits(new HashSet<>(Set.of(
                loadServiceInfo(shiftWorkerDep1Id),
                loadServiceInfo(departmentWorkerDep1Id)
        )));
        dataManager.save(shiftDep1);

        Shift shiftDep2 = dataManager.create(Shift.class);
        shiftDep2.setDate(operationalDate);
        shiftDep2.setNumber(NumberOfShift._30);
        shiftDep2.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shiftDep2.setDepartmentToday(DepartmentConverter.departmentFromDate(operationalDate));
        shiftDep2.setUnits(new HashSet<>(Set.of(loadServiceInfo(shiftWorkerDep2Id))));
        dataManager.save(shiftDep2);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Сегодняшние сотрудники смены — только участники смен на операционную дату")
    void todayShiftEmployees() {
        Set<UUID> recipients = recipientResolver.resolve(
                DashboardMessageAudience.TODAY_SHIFT_EMPLOYEES,
                senderId,
                operationalDate
        );

        assertEquals(
                Set.of(shiftWorkerDep1Id, departmentWorkerDep1Id, shiftWorkerDep2Id),
                recipients
        );
    }

    @Test
    @DisplayName("Сотрудники своего отделения — без должностей A и B")
    void myDepartmentEmployees() {
        Set<UUID> recipients = recipientResolver.resolve(
                DashboardMessageAudience.MY_DEPARTMENT_EMPLOYEES,
                senderId,
                operationalDate
        );

        assertTrue(recipients.contains(shiftWorkerDep1Id));
        assertTrue(recipients.contains(departmentWorkerDep1Id));
        assertTrue(recipients.contains(commanderDep1Id));
        assertFalse(recipients.contains(shiftWorkerDep2Id));
        assertFalse(recipients.contains(comVzvodId));
    }

    @Test
    @DisplayName("Все командиры отделения — все сотрудники с должностью C")
    void allDepartmentCommanders() {
        Set<UUID> recipients = recipientResolver.resolve(
                DashboardMessageAudience.ALL_DEPARTMENT_COMMANDERS,
                senderId,
                operationalDate
        );

        assertEquals(Set.of(senderId, commanderDep1Id, commanderDep2Id), recipients);
    }

    @Test
    @DisplayName("Сегодняшние командиры отделения — C в отделении по ротации")
    void todayDepartmentCommanders() {
        int todayDepartmentNumber = DepartmentConverter.departmentFromDateToInt(operationalDate);
        Department todayDepartment = todayDepartmentNumber == 1 ? dep1 : dep2;
        UUID expectedCommanderId = todayDepartment.getId().equals(dep1.getId()) ? commanderDep1Id : commanderDep2Id;

        Set<UUID> recipients = recipientResolver.resolve(
                DashboardMessageAudience.TODAY_DEPARTMENT_COMMANDERS,
                senderId,
                operationalDate
        );

        assertEquals(Set.of(expectedCommanderId), recipients);
    }

    @Test
    @DisplayName("Все сотрудники — все учётные записи с ServiceInfo")
    void allEmployees() {
        Set<UUID> recipients = recipientResolver.resolve(
                DashboardMessageAudience.ALL_EMPLOYEES,
                senderId,
                operationalDate
        );

        assertTrue(recipients.containsAll(Set.of(
                senderId,
                shiftWorkerDep1Id,
                shiftWorkerDep2Id,
                departmentWorkerDep1Id,
                commanderDep1Id,
                commanderDep2Id,
                comVzvodId,
                inactiveWorkerId
        )));
    }

    @Test
    @DisplayName("Сотрудники в строю — только StatusInService.ACTIVE")
    void activeEmployees() {
        Set<UUID> recipients = recipientResolver.resolve(
                DashboardMessageAudience.ACTIVE_EMPLOYEES,
                senderId,
                operationalDate
        );

        assertTrue(recipients.contains(shiftWorkerDep1Id));
        assertFalse(recipients.contains(inactiveWorkerId));
    }

    private Department saveDepartment(int number) {
        Department department = dataManager.create(Department.class);
        department.setNumber(number);
        return dataManager.save(department);
    }

    private UUID createUserWithServiceInfo(Post post, Department department, StatusInService status) {
        User user = dataManager.create(User.class);
        user.setUsername("user-" + UUID.randomUUID());
        user.setPassword("pwd");
        user.setFirstName("Имя");
        user.setLastName("Фамилия");
        user.setPatronymic("Отчество");
        user = dataManager.save(user);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        serviceInfo.setUser(user);
        serviceInfo.setPost(post);
        serviceInfo.setStatus(status);
        serviceInfo.setDepartment(department);
        serviceInfo.setToken("token-" + UUID.randomUUID());
        dataManager.save(serviceInfo);
        return user.getId();
    }

    private ServiceInfo loadServiceInfo(UUID userId) {
        return dataManager.load(ServiceInfo.class)
                .query("select si from ServiceInfo si where si.user.id = :userId")
                .parameter("userId", userId)
                .one();
    }
}
