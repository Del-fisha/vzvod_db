package com.company.vzvod.integration;

import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.TypeOfEducation;
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

import java.time.LocalDate;

@Testcontainers
@SpringBootTest(properties = "spring.profiles.active=test-postgres")
@DisplayName("Интеграционный тест Education")
public class EducationIntegrationTest {

    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withPassword("test")
            .withUsername("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private Education education;

    @BeforeAll
    static void startContainer() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        education = dataManager.create(Education.class);
        education.setStarted(LocalDate.now().minusYears(15));
        education.setUntil(LocalDate.now().minusYears(10));
        education.setNameOfInstitution("ПТПП");
        education.setType(TypeOfEducation.SPECIFIC);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }
}
