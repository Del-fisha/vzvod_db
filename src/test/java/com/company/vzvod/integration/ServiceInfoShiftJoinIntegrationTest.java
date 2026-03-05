package com.company.vzvod.integration;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.User;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.security.SystemAuthenticator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test-postgres")
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
        PreTestEntities.updateUser(user);

        User savedUser = dataManager.save(user);

        serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(savedUser);

        shift = dataManager.create(Shift.class);
        PreTestEntities.updateShift(shift);
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
    void tearDown() {
        systemAuthenticator.end();
    }
}