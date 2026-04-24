package com.company.vzvod.integration;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.security.FullAccessRole;
import com.company.vzvod.security.PolicemanRole;
import com.company.vzvod.security.UiMinimalRole;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PostChangeRoleAssignmentIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @BeforeEach
    void begin() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void end() {
        systemAuthenticator.end();
    }

    @Test
    void changingPost_switchesBetweenPolicemanAndFullAccess() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setUsername("admino-" + java.util.UUID.randomUUID()); // unique for parallel/local runs

        ServiceInfo si = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(si);
        si.setUser(user);
        si.setPost(Post.INTERN);
        user.setServiceInfo(si);

        User saved = dataManager.save(user);

        Set<String> roles1 = loadRoles(saved.getUsername());
        assertTrue(roles1.contains(UiMinimalRole.CODE));
        assertTrue(roles1.contains(PolicemanRole.CODE));
        assertFalse(roles1.contains(FullAccessRole.CODE));

        ServiceInfo loadedSi = dataManager.load(ServiceInfo.class).id(saved.getServiceInfo().getId()).one();
        loadedSi.setPost(Post.COM_OTD);
        dataManager.save(loadedSi);

        Set<String> roles2 = loadRoles(saved.getUsername());
        assertTrue(roles2.contains(UiMinimalRole.CODE));
        assertTrue(roles2.contains(FullAccessRole.CODE));
        assertTrue(roles2.contains(PolicemanRole.CODE));
    }

    private Set<String> loadRoles(String username) {
        return dataManager.load(RoleAssignmentEntity.class)
                .query("select ra from sec_RoleAssignmentEntity ra where ra.username = :u")
                .parameter("u", username)
                .list()
                .stream()
                .map(RoleAssignmentEntity::getRoleCode)
                .collect(Collectors.toSet());
    }
}

