package com.company.vzvod.integration;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.StatusOfHousing;
import com.company.vzvod.entity.TypeOfHousing;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
    void testConnection() {
    }


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

    @Test
    @DisplayName("Тест изменения в БД")
    void updateTest() {
        Address savedAddress = dataManager.save(address);
        UUID addressId = savedAddress.getId();

        String indexError = "7894563";
        String newIndex = "789456";
        String newCity = "Москва";
        String newStreet = "Соломина";
        String newHouseNumber = "98";
        String newBody = "1";
        String newFlat = "64";

        savedAddress.setTypeOfHousing(TypeOfHousing.ROOM);
        savedAddress.setStatusOfHousing(StatusOfHousing.SHARED);
        savedAddress.setIndex(indexError);
        savedAddress.setCity(newCity);
        savedAddress.setStreet(newStreet);
        savedAddress.setHouseNumber(newHouseNumber);
        savedAddress.setBody(newBody);
        savedAddress.setFlat(newFlat);

        assertThrows(ConstraintViolationException.class, () ->
                dataManager.save(savedAddress));

        savedAddress.setIndex(newIndex);

        Address newAddress = dataManager.save(savedAddress);
        Address loadedAddress = dataManager.load(Address.class).id(addressId).one();

        assertEquals(addressId, loadedAddress.getId());
        assertEquals(newIndex, loadedAddress.getIndex());
        assertEquals(newCity, loadedAddress.getCity());
        assertEquals(newStreet, loadedAddress.getStreet());
        assertEquals(newHouseNumber, loadedAddress.getHouseNumber());
        assertEquals(newBody, loadedAddress.getBody());
        assertEquals(newFlat, loadedAddress.getFlat());
        assertEquals(savedAddress.getTypeOfHousing(), loadedAddress.getTypeOfHousing());
        assertEquals(savedAddress.getStatusOfHousing(), loadedAddress.getStatusOfHousing());

        assertEquals(TypeOfHousing.ROOM, loadedAddress.getTypeOfHousing());
        assertEquals(StatusOfHousing.SHARED, loadedAddress.getStatusOfHousing());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        Address savedAddress = dataManager.save(address);
        UUID addressId = savedAddress.getId();

        dataManager.remove(address);

        Address deletedAddress = dataManager.load(Address.class).id(addressId).optional().orElse(null);
        assertNull(deletedAddress);
    }

    @Test
    @DisplayName("Тест каскадного удаления")
    void cascadeDeleteTest() {
        Contacts contact = PreTestEntities.getContacts();
        contact.setRegistration(address);
        Contacts savedContact = dataManager.save(contact);

        Address savedAddress = savedContact.getRegistration();
        UUID addressId = savedAddress.getId();

        assertEquals(address.getIndex(), savedAddress.getIndex());

        dataManager.remove(savedContact);

        Address loadedAddress = dataManager.load(Address.class)
                .id(addressId)
                .optional()
                .orElse(null);
        assertNull(loadedAddress);
    }
}
