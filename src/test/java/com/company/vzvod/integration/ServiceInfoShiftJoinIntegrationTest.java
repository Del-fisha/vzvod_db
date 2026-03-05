package com.company.vzvod.integration;

import com.company.vzvod.entity.*;
import com.company.vzvod.service.DepartmentConverter;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.security.SystemAuthenticator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test-postgres")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест JOIN-таблицы Shift:ServiceInfo")
public class ServiceInfoShiftJoinIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    FetchPlans fetchPlans;

    @PersistenceContext
    EntityManager entityManager;

    ServiceInfo serviceInfo;

    Shift shift;

    User user;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        user = dataManager.create(User.class);
        user.setDateOfBirth(LocalDate.now().minusYears(30));
        user.setUsername("123_" + System.currentTimeMillis());
        user.setPatronymic("Петрович");
        user.setLastName("Петров");
        user.setFirstName("Пётр");
        user.setPassword("123");

        User savedUser = dataManager.save(user);

        serviceInfo = dataManager.create(ServiceInfo.class);
        serviceInfo.setStartOfPost(LocalDate.ofYearDay(2013, 56));
        serviceInfo.setToken("65492_" + System.currentTimeMillis());
        serviceInfo.setQualificationClass(Qualification.THIRD);
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setRank(Rank.SENIOR_SERGEANT);
        serviceInfo.setMedicalExamination(false);
        serviceInfo.setBreastplate("00659874");
        serviceInfo.setPost(Post.COM_OTD);
        serviceInfo.setUser(savedUser);

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
    }

    @Test
    @DisplayName("Проверка создания связи ManyToMany через соединительную таблицу")
    void testManyToManyJoin() {

        ServiceInfo savedServiceInfo = dataManager.save(serviceInfo);

        shift.getUnits().add(savedServiceInfo);

        Shift savedShift = dataManager.save(shift);

        entityManager.clear();

        FetchPlan shiftPlan = fetchPlans.builder(Shift.class)
                .add("units")
                .build();
        Shift loadedShift = dataManager.load(Shift.class)
                .id(savedShift.getId())
                .fetchPlan(shiftPlan)
                .one();

        assertThat(loadedShift.getUnits())
                .isNotEmpty()
                .anyMatch(si -> si.getId().equals(savedServiceInfo.getId()));

        FetchPlan serviceInfoPlan = fetchPlans.builder(ServiceInfo.class)
                .add("shifts")
                .build();
        ServiceInfo loadedServiceInfo = dataManager.load(ServiceInfo.class)
                .id(savedServiceInfo.getId())
                .fetchPlan(serviceInfoPlan)
                .one();

        assertThat(loadedServiceInfo.getShifts())
                .isNotEmpty()
                .anyMatch(s -> s.getId().equals(savedShift.getId()));
    }

    @AfterEach
    void tearDown(){
        systemAuthenticator.end();
    }
}