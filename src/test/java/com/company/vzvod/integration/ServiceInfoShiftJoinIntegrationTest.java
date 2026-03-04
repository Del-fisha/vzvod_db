package com.company.vzvod.integration;

import com.company.vzvod.entity.*;
import com.company.vzvod.service.DepartmentConverter;
import com.company.vzvod.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест JOIN-таблицы Shift:ServiceInfo")
public class ServiceInfoShiftJoinIntegrationTest {

    @Autowired
    DataManager dataManager;

    ServiceInfo serviceInfo;

    Shift shift;

    User user;

    @BeforeEach
    void setUp() {
        user = dataManager.create(User.class);
        serviceInfo = dataManager.create(ServiceInfo.class);
        shift = dataManager.create(Shift.class);

        shift.setDepartmentToday(DepartmentConverter.departmentFromDate(LocalDate.now()));
        shift.setStartTime(LocalTime.of(10,0));
        shift.setEndTime(LocalTime.of(22,0));
        shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shift.setNumber(NumberOfShift._28);
        shift.setDate(LocalDate.now());
        shift.setIbdWithoutMigrant(45);
        shift.setCountOfStatements(2);
        shift.setIbdWithMigrant(60);
        shift.setCountOfClaims(1);

        serviceInfo.setStartOfPost(LocalDate.ofYearDay(2013, 56));
        serviceInfo.setToken("65492_" + System.currentTimeMillis());
        serviceInfo.setQualificationClass(Qualification.THIRD);
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setRank(Rank.SENIOR_SERGEANT);
        serviceInfo.setMedicalExamination(false);
        serviceInfo.setBreastplate("00659874");
        serviceInfo.setPost(Post.COM_OTD);

        user.setDateOfBirth(LocalDate.now().minusYears(30));
        user.setUsername("123_" + System.currentTimeMillis());
        user.setPatronymic("Петрович");
        user.setLastName("Петров");
        user.setFirstName("Пётр");
        user.setPassword("123");
    }

    @Test
    void test() {

    }
}