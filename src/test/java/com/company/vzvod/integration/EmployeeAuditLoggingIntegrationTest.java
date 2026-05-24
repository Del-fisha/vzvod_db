package com.company.vzvod.integration;

import com.company.vzvod.entity.User;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграция Core → logging-service (аудит сотрудников)")
class EmployeeAuditLoggingIntegrationTest {

    private static MockWebServer loggingServer;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private UUID createdUserId;
    private User auditActor;

    @BeforeAll
    static void startMockLoggingServer() throws Exception {
        loggingServer = new MockWebServer();
        loggingServer.start();
    }

    @AfterAll
    static void stopMockLoggingServer() throws Exception {
        loggingServer.shutdown();
    }

    @DynamicPropertySource
    static void loggingProperties(DynamicPropertyRegistry registry) {
        registry.add("logging.microservice.url", () -> loggingServer.url("/").toString().replaceAll("/$", ""));
        registry.add("logging.microservice.async", () -> "false");
        registry.add("logging.microservice.token", () -> "");
        registry.add("logging.microservice.deployment", () -> "test-postgres");
    }

    @BeforeEach
    void setUp() {
        loggingServer.enqueue(new MockResponse().setResponseCode(204));
        loggingServer.enqueue(new MockResponse().setResponseCode(204));
        systemAuthenticator.begin();
        auditActor = dataManager.create(User.class);
        PreTestEntities.updateUser(auditActor);
        auditActor.setUsername("audit-log-actor-" + UUID.randomUUID().toString().substring(0, 8));
        auditActor = dataManager.save(auditActor);
        systemAuthenticator.end();
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.begin();
        if (createdUserId != null) {
            dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove);
            createdUserId = null;
        }
        if (auditActor != null) {
            dataManager.load(User.class).id(auditActor.getId()).optional().ifPresent(dataManager::remove);
            auditActor = null;
        }
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("сохранение нового сотрудника отправляет запись в logging-service")
    void createUser_sendsLog() throws Exception {
        systemAuthenticator.withUser(auditActor.getUsername(), () -> {
            User user = dataManager.create(User.class);
            PreTestEntities.updateUser(user);
            User saved = dataManager.save(user);
            createdUserId = saved.getId();
            return null;
        });

        RecordedRequest request = loggingServer.takeRequest(5, TimeUnit.SECONDS);
        String body = request.getBody().readUtf8();
        assertThat(body).contains("\"service\":\"CORE\"");
        assertThat(body).contains("\"deployment\":\"test-postgres\"");
        assertThat(body).contains("создал сотрудника");
    }

    @Test
    @DisplayName("изменение фамилии отправляет запись в logging-service")
    void updateUser_sendsLog() throws Exception {
        UUID[] userIdHolder = new UUID[1];
        systemAuthenticator.withUser(auditActor.getUsername(), () -> {
            User user = dataManager.create(User.class);
            PreTestEntities.updateUser(user);
            User saved = dataManager.save(user);
            userIdHolder[0] = saved.getId();
            createdUserId = saved.getId();
            return null;
        });
        loggingServer.takeRequest(5, TimeUnit.SECONDS);

        systemAuthenticator.withUser(auditActor.getUsername(), () -> {
            User user = dataManager.load(User.class).id(userIdHolder[0]).one();
            user.setLastName("Сидоров");
            dataManager.save(user);
            return null;
        });

        RecordedRequest request = loggingServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getBody().readUtf8()).contains("изменил").contains("фамилия");
    }
}
