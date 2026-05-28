package com.company.vzvod.integration;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.User;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Удаление сотрудника из Shift.units не должно удалять ServiceInfo")
class ShiftUnitExcludeIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private FetchPlans fetchPlans;

    private UUID createdUserId;
    private UUID createdPartnerUserId;
    private UUID createdServiceInfoId;
    private UUID createdPartnerServiceInfoId;
    private UUID createdShiftId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        if (createdShiftId != null) {
            dataManager.load(Shift.class).id(createdShiftId).optional().ifPresent(dataManager::remove);
            createdShiftId = null;
        }
        if (createdPartnerUserId != null) {
            dataManager.load(User.class).id(createdPartnerUserId).optional().ifPresent(dataManager::remove);
            createdPartnerUserId = null;
            createdPartnerServiceInfoId = null;
        }
        if (createdUserId != null) {
            dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove);
            createdUserId = null;
            createdServiceInfoId = null;
        }
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("remove из units только отвязывает ServiceInfo от смены")
    void removeUnitFromShift_preservesServiceInfo() {
        ServiceInfo primary = createUserWithServiceInfo();
        ServiceInfo partner = createPartnerServiceInfo();

        Shift shift = dataManager.create(Shift.class);
        PreTestEntities.updateShift(shift);
        shift.getUnits().add(primary);
        shift.getUnits().add(partner);
        Shift savedShift = dataManager.save(shift);
        createdShiftId = savedShift.getId();

        FetchPlan shiftPlan = fetchPlans.builder(Shift.class).add("units").build();
        Shift loaded = dataManager.load(Shift.class).id(savedShift.getId()).fetchPlan(shiftPlan).one();
        ServiceInfo toDetach = loaded.getUnits().stream()
                .filter(si -> si.getId().equals(partner.getId()))
                .findFirst()
                .orElseThrow();
        loaded.getUnits().remove(toDetach);
        dataManager.save(loaded);

        ServiceInfo reloadedPartner = dataManager.load(ServiceInfo.class).id(partner.getId()).one();
        assertThat(reloadedPartner.getPost()).isEqualTo(Post.POLICEMAN);
        assertThat(reloadedPartner.getUser()).isNotNull();
        assertThat(reloadedPartner.getUser().getId()).isEqualTo(createdPartnerUserId);

        Shift reloadedShift = dataManager.load(Shift.class).id(savedShift.getId()).fetchPlan(shiftPlan).one();
        assertThat(reloadedShift.getUnits())
                .hasSize(1)
                .anyMatch(si -> si.getId().equals(primary.getId()));
        assertThat(reloadedShift.getUnits()).noneMatch(si -> si.getId().equals(partner.getId()));
    }

    @Test
    @DisplayName("dataManager.remove(ServiceInfo) удаляет запись (так работал list_remove в UI)")
    void removeServiceInfoFromDataManager_deletesRecord() {
        ServiceInfo serviceInfo = createUserWithServiceInfo();
        UUID serviceInfoId = serviceInfo.getId();
        UUID userId = createdUserId;

        dataManager.remove(serviceInfo);
        createdServiceInfoId = null;

        assertThat(dataManager.load(ServiceInfo.class).id(serviceInfoId).optional()).isEmpty();
        User user = dataManager.load(User.class).id(userId).one();
        assertThat(user.getServiceInfo()).isNull();
    }

    private ServiceInfo createUserWithServiceInfo() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setPost(Post.COM_OTD);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);

        User saved = dataManager.save(user);
        createdUserId = saved.getId();
        createdServiceInfoId = saved.getServiceInfo().getId();
        return saved.getServiceInfo();
    }

    private ServiceInfo createPartnerServiceInfo() {
        User partnerUser = dataManager.create(User.class);
        PreTestEntities.updateUser(partnerUser);
        partnerUser.setUsername("partner-" + System.currentTimeMillis());

        ServiceInfo partner = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(partner);
        partner.setPost(Post.POLICEMAN);
        partner.setUser(partnerUser);
        partnerUser.setServiceInfo(partner);

        User savedPartnerUser = dataManager.save(partnerUser);
        createdPartnerUserId = savedPartnerUser.getId();
        createdPartnerServiceInfoId = savedPartnerUser.getServiceInfo().getId();
        return savedPartnerUser.getServiceInfo();
    }
}
