package com.company.vzvod.integration;

import com.company.vzvod.entity.*;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        address.setIndex("198456");
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


    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        Address savedAddress = dataManager.save(address);

        Address loadedAddress = dataManager.load(Address.class).id(savedAddress.getId()).one();

        assertEquals(loadedAddress.getId(), savedAddress.getId());
        assertEquals(loadedAddress.getBody(), savedAddress.getBody());
        assertEquals(loadedAddress.getCity(), savedAddress.getCity());
        assertEquals(loadedAddress.getIndex(), savedAddress.getIndex());
        assertEquals(loadedAddress.getFlat(), savedAddress.getFlat());
        assertEquals(loadedAddress.getHouseNumber(), savedAddress.getHouseNumber());
        assertEquals(loadedAddress.getStreet(), savedAddress.getStreet());
        assertEquals(loadedAddress.getStatusOfHousing(), savedAddress.getStatusOfHousing());
        assertEquals(loadedAddress.getTypeOfHousing(), savedAddress.getTypeOfHousing());

        assertEquals(TypeOfHousing.FLAT, savedAddress.getTypeOfHousing());
        assertEquals(StatusOfHousing.OWNER, savedAddress.getStatusOfHousing());
    }
}
