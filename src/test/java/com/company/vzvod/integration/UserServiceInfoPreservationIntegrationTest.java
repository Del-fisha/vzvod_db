package com.company.vzvod.integration;

import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.service.UserDialogSaveService;
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
@DisplayName("ServiceInfo не должна теряться при сохранении User")
class UserServiceInfoPreservationIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private UserDialogSaveService userDialogSaveService;

    private UUID createdUserId;
    private UUID createdServiceInfoId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        if (createdUserId != null) {
            dataManager.loadValue(
                            "select si.id from ServiceInfo si where si.user.id = :uid",
                            UUID.class
                    )
                    .parameter("uid", createdUserId)
                    .list()
                    .forEach(id -> dataManager.load(ServiceInfo.class).id(id).optional().ifPresent(dataManager::remove));
            dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove);
            createdUserId = null;
            createdServiceInfoId = null;
        }
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("saveFromDialog с null serviceInfo не удаляет существующую ServiceInfo")
    void saveFromDialog_nullServiceInfo_preservesExisting() {
        User user = createUserWithServiceInfo(Post.COM_OTD);
        UUID serviceInfoId = user.getServiceInfo().getId();

        User edited = dataManager.create(User.class);
        PreTestEntities.updateUser(edited);
        edited.setId(user.getId());
        edited.setUsername(user.getUsername());
        edited.setLastName("НОВАЯ_ФАМИЛИЯ");
        edited.setServiceInfo(null);

        userDialogSaveService.saveFromDialog(edited);

        ServiceInfo loaded = dataManager.load(ServiceInfo.class).id(serviceInfoId).one();
        assertEquals(Post.COM_OTD, loaded.getPost());
        assertEquals(user.getId(), loaded.getUser().getId());
    }

    @Test
    @DisplayName("saveFromDialog с новой ServiceInfo не заменяет существующую запись")
    void saveFromDialog_newServiceInfo_doesNotReplaceExisting() {
        User user = createUserWithServiceInfo(Post.COM_OTD);
        UUID serviceInfoId = user.getServiceInfo().getId();

        Penalty penalty = dataManager.create(Penalty.class);
        PreTestEntities.updatePenalty(penalty);
        penalty.setUserServiceInfo(user.getServiceInfo());
        dataManager.save(penalty);

        User edited = dataManager.create(User.class);
        PreTestEntities.updateUser(edited);
        edited.setId(user.getId());
        edited.setUsername(user.getUsername());

        ServiceInfo replacement = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(replacement);
        replacement.setPost(Post.POLICEMAN);
        replacement.setUser(edited);
        edited.setServiceInfo(replacement);

        userDialogSaveService.saveFromDialog(edited);

        ServiceInfo original = dataManager.load(ServiceInfo.class).id(serviceInfoId).one();
        assertEquals(Post.COM_OTD, original.getPost());

        Long penaltyCount = dataManager.loadValue(
                        "select count(p) from Penalty p where p.userServiceInfo.id = :id",
                        Long.class
                )
                .parameter("id", serviceInfoId)
                .one();
        assertEquals(1L, penaltyCount);

        User reloaded = dataManager.load(User.class).id(user.getId()).one();
        assertNotNull(reloaded.getServiceInfo());
        assertEquals(serviceInfoId, reloaded.getServiceInfo().getId());

        Long serviceInfoCount = dataManager.loadValue(
                        "select count(si) from ServiceInfo si where si.user.id = :uid",
                        Long.class
                )
                .parameter("uid", user.getId())
                .one();
        assertEquals(1L, serviceInfoCount);
    }

  private User createUserWithServiceInfo(Post post) {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setPost(post);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);

        User saved = dataManager.save(user);
        createdUserId = saved.getId();
        createdServiceInfoId = saved.getServiceInfo().getId();
        return saved;
    }
}
