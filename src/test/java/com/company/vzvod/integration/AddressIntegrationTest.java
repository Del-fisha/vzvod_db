package com.company.vzvod.integration;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.StatusOfHousing;
import com.company.vzvod.entity.TypeOfHousing;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(properties = "spring.profiles.active=test-postgres")
@DisplayName("Интеграционный тест Address")
public class AddressIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer("postgres:16")
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

    private Address address;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private DataManager dataManager;

    @BeforeAll
    static void startContainer() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        address = dataManager.create(Address.class);
        address.setIndex("1987456");
        address.setCity("Санкт-Петербург");
        address.setStreet("Невский пр.");
        address.setHouseNumber("78");
        address.setBody("3");
        address.setFlat("698");
        address.setStatusOfHousing(StatusOfHousing.OWNER);
        address.setTypeOfHousing(TypeOfHousing.FLAT);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    void testConnection() {}


}
