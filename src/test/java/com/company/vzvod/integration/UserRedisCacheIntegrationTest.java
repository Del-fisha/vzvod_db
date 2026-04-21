package com.company.vzvod.integration;

import com.company.vzvod.entity.User;
import com.company.vzvod.service.UserReadService;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест Redis-кэша пользователя (Cacheable + инвалидирование)")
public class UserRedisCacheIntegrationTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.cache.type", () -> "redis");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private UserReadService userReadService;

    @Autowired
    private CacheManager cacheManager;

    private User user;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user = dataManager.save(user);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Первый вызов кладёт значение в кэш, второй читает из кэша")
    void cacheablePutsValueIntoRedisCache() {
        UUID userId = user.getId();

        Cache cache = cacheManager.getCache("userById");
        assertNotNull(cache, "Cache userById должен быть сконфигурирован");

        assertNull(cache.get(userId), "До первого вызова кэш должен быть пуст");

        userReadService.getUserCached(userId);

        assertNotNull(cache.get(userId), "После первого вызова значение должно появиться в кэше");

        userReadService.getUserCached(userId);

        assertNotNull(cache.get(userId), "После второго вызова значение должно оставаться в кэше");
    }

    @Test
    @DisplayName("После изменения пользователя кэш по id инвалидируется")
    void cacheInvalidatedAfterUserChange() {
        UUID userId = user.getId();

        Cache cache = cacheManager.getCache("userById");
        assertNotNull(cache, "Cache userById должен быть сконфигурирован");

        userReadService.getUserCached(userId);
        assertNotNull(cache.get(userId), "После прогрева значение должно быть в кэше");

        User loaded = dataManager.load(User.class).id(userId).one();
        loaded.setFirstName("НовоеИмя");
        dataManager.save(loaded);

        assertNull(cache.get(userId), "После сохранения пользователь должен быть удалён из кэша");
    }
}

