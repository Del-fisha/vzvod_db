package com.company.vzvod.integration;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.Impact;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = "spring.profiles.active=test-postgres")
@Testcontainers
@DisplayName("Интеграционный тест Address")
public class AdministrativeViolationTest {

    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @BeforeAll
    static void startContainer() {
        postgreSQLContainer.start();
    }

    AdministrativeViolation administrativeViolation;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        administrativeViolation = dataManager.create(AdministrativeViolation.class);
        administrativeViolation.setArticle(ArticleOfAdministrative._18_8);
        administrativeViolation.setImpact(Impact.WITHOUT_IMPACT);
        administrativeViolation.setShift();
    }





    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }
}
