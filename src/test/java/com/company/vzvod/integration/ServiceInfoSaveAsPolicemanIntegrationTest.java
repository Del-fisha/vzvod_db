package com.company.vzvod.integration;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.security.PostBasedRoleAssignmentService;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.DataContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ServiceInfoSaveAsPolicemanIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    PostBasedRoleAssignmentService postBasedRoleAssignmentService;

    @Autowired
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    @Autowired
    DataComponents dataComponents;

    private String username;
    private UUID userId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        username = "policeman-" + UUID.randomUUID();
        user.setUsername(username);

        ServiceInfo si = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(si);
        si.setPost(Post.POLICEMAN);
        si.setUser(user);
        user.setServiceInfo(si);

        User saved = dataManager.save(user);
        userId = saved.getId();

        postBasedRoleAssignmentService.ensurePostBasedRole(username, false);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        try {
            // Remove only the user created in setUp().
            // Some tests intentionally call systemAuthenticator.end() before running assertions,
            // so we must ensure we have system authentication here.
            systemAuthenticator.begin();
            if (userId != null) {
                dataManager.load(User.class).id(userId).optional().ifPresent(dataManager::remove);
                userId = null;
            }
        } finally {
            systemAuthenticator.end();
        }
    }

    @Test
    @DisplayName("Без FullAccessRole: можно сохранить ServiceInfo (профосмотр) и затем сохранить User")
    void canSaveServiceInfoAndThenUser_withoutFullAccess() {
        systemAuthenticator.end();
        authenticateAsPoliceman(username, userId);

        User loaded = dataManager.load(User.class).id(userId).one();
        assertNotNull(loaded.getServiceInfo());

        // имитируем сценарий: поменяли профосмотр в ServiceInfo, вернулись и сохраняем карточку User
        loaded.getServiceInfo().setMedicalExamination(Boolean.TRUE);
        loaded.setLastName(loaded.getLastName() + "-x");

        assertDoesNotThrow(() -> dataManager.save(loaded));
    }

    @Test
    @DisplayName("Без FullAccessRole: сохранение ServiceInfo через DataContext (как в UI) работает")
    void canSaveServiceInfoViaDataContext_withoutFullAccess() {
        systemAuthenticator.end();
        authenticateAsPoliceman(username, userId);

        User loaded = dataManager.load(User.class).id(userId).one();
        ServiceInfo si = loaded.getServiceInfo();
        assertNotNull(si);

        si.setMedicalExamination(Boolean.TRUE);

        DataContext dc = dataComponents.createDataContext();
        dc.merge(loaded); // closer to UI: User + linked ServiceInfo in same context
        ServiceInfo merged = dc.merge(si);

        assertDoesNotThrow(() -> {
            dc.save();
            return null;
        });
        assertNotNull(merged.getId());
    }

    private void authenticateAsPoliceman(String username, UUID userId) {
        User principal = new User();
        principal.setUsername(username);
        principal.setId(userId);

        var authorities = java.util.List.of(
                roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(com.company.vzvod.security.UiMinimalRole.CODE),
                roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(com.company.vzvod.security.PolicemanRole.CODE)
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "n/a", authorities)
        );
    }
}

