package com.company.vzvod.entity;

import io.jmix.core.DataManager;
import io.jmix.core.MetadataTools;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Unit-тесты для Address Entity")
class AddressTest {

    @Autowired
    protected DataManager dataManager;

    @Autowired(required = false)
    private Validator validator;

    @Autowired
    private MetadataTools metadataTools;

    private Address address;

    @BeforeEach
    void setUp() {
        address = dataManager.create(Address.class);
    }

    @Test
    @DisplayName("Проверка установки и получения ID")
    void testSetAndGetId() {
        UUID testId = UUID.randomUUID();
        address.setId(testId);
        assertEquals(testId, address.getId(), "ID должен быть установлен корректно");
    }

    @Test
    @DisplayName("ID автоматически генерируется при создании через dataManager")
    void testIdIsGeneratedByDataManager() {
        assertNotNull(address.getId(), "ID должен быть автоматически сгенерирован");
        assertInstanceOf(UUID.class, address.getId(), "ID должен быть типа UUID");
    }

    @Test
    @DisplayName("Проверка установки и получения index")
    void testSetAndGetIndex() {
        String testIndex = "123456";
        address.setIndex(testIndex);
        assertEquals(testIndex, address.getIndex(), "Index должен быть установлен корректно");
    }

    @Test
    @DisplayName("Index может быть null")
    void testIndexIsNullByDefault() {
        assertNull(address.getIndex(), "Index должен быть null по умолчанию");
    }

    @Test
    @DisplayName("@Pattern: index с 6 цифрами проходит валидацию")
    void testIndexValidPattern() {
        address.setIndex("654321");
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("index")),
                "Index '654321' должен пройти валидацию @Pattern");
    }

    @Test
    @DisplayName("@Pattern: index с буквами не проходит валидацию")
    void testIndexInvalidPatternWithLetters() {
        address.setIndex("12345a");
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("index")),
                "Index '12345a' должен не пройти валидацию @Pattern");
    }

    @Test
    @DisplayName("@Pattern: index с 5 цифрами не проходит валидацию")
    void testIndexInvalidPatternTooShort() {
        address.setIndex("12345");
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("index")),
                "Index '12345' должен не пройти валидацию @Pattern (требуется ровно 6 цифр)");
    }

    @Test
    @DisplayName("@Pattern: index с 7 цифрами не проходит валидацию")
    void testIndexInvalidPatternTooLong() {
        address.setIndex("1234567");
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("index")),
                "Index '1234567' должен не пройти валидацию @Pattern (требуется ровно 6 цифр)");
    }

    @Test
    @DisplayName("Проверка установки и получения city")
    void testSetAndGetCity() {
        String testCity = "Санкт-Петербург";
        address.setCity(testCity);
        assertEquals(testCity, address.getCity(), "City должен быть установлен корректно");
    }

    @Test
    @DisplayName("City может быть null")
    void testCityIsNullByDefault() {
        assertNull(address.getCity(), "City должен быть null по умолчанию");
    }

    @Test
    @DisplayName("City может быть пустой строкой")
    void testCityCanBeEmpty() {
        address.setCity("");
        assertEquals("", address.getCity(), "City может быть пустой строкой");
    }

    @Test
    @DisplayName("City длиной ровно в 50 символов должен приниматься")
    void testSetCityWithMaxLength() {
        String testCity = "А".repeat(50);
        address.setCity(testCity);
        assertEquals(testCity, address.getCity(), "City длиной 50 символов должен приниматься");
    }

    @Test
    @DisplayName("Проверка установки и получения street")
    void testSetAndGetStreet() {
        String testStreet = "Невский проспект";
        address.setStreet(testStreet);
        assertEquals(testStreet, address.getStreet(), "Street должен быть установлен корректно");
    }

    @Test
    @DisplayName("Street может быть null")
    void testStreetIsNullByDefault() {
        assertNull(address.getStreet(), "Street должен быть null по умолчанию");
    }

    @Test
    @DisplayName("Проверка установки и получения houseNumber")
    void testSetAndGetHouseNumber() {
        String testHouseNumber = "42";
        address.setHouseNumber(testHouseNumber);
        assertEquals(testHouseNumber, address.getHouseNumber(), "HouseNumber должен быть установлен корректно");
    }

    @Test
    @DisplayName("HouseNumber может быть null")
    void testHouseNumberIsNullByDefault() {
        assertNull(address.getHouseNumber(), "HouseNumber должен быть null по умолчанию");
    }

    @Test
    @DisplayName("HouseNumber длиной ровно в 5 символов должен приниматься")
    void testSetHouseNumberWithMaxLength() {
        String testHouseNumber = "42/16";
        address.setHouseNumber(testHouseNumber);
        assertEquals(testHouseNumber, address.getHouseNumber(), "HouseNumber длиной 5 символов должен приниматься");
    }

    @Test
    @DisplayName("Проверка установки и получения body")
    void testSetAndGetBody() {
        String testBody = "3";
        address.setBody(testBody);
        assertEquals(testBody, address.getBody(), "Body должен быть установлен корректно");
    }

    @Test
    @DisplayName("Body может быть null")
    void testBodyIsNullByDefault() {
        assertNull(address.getBody(), "Body должен быть null по умолчанию");
    }

    @Test
    @DisplayName("Body длиной ровно в 4 символа должен приниматься")
    void testSetBodyWithMaxLength() {
        String testBody = "корп";
        address.setBody(testBody);
        assertEquals(testBody, address.getBody(), "Body длиной 4 символа должен приниматься");
    }

    @Test
    @DisplayName("Проверка установки и получения flat")
    void testSetAndGetFlat() {
        String testFlat = "128";
        address.setFlat(testFlat);
        assertEquals(testFlat, address.getFlat(), "Flat должен быть установлен корректно");
    }

    @Test
    @DisplayName("Flat может быть null")
    void testFlatIsNullByDefault() {
        assertNull(address.getFlat(), "Flat должен быть null по умолчанию");
    }

    @Test
    @DisplayName("Flat длиной ровно в 10 символов должен приниматься")
    void testSetFlatWithMaxLength() {
        String testFlat = "128-корп-3";
        address.setFlat(testFlat);
        assertEquals(testFlat, address.getFlat(), "Flat длиной 10 символов должен приниматься");
    }

    @Test
    @DisplayName("Проверка установки и получения typeOfHousing через enum")
    void testSetAndGetTypeOfHousing() {
        TypeOfHousing typeOfHousing = TypeOfHousing.FLAT;
        address.setTypeOfHousing(typeOfHousing);
        assertEquals(typeOfHousing, address.getTypeOfHousing(), "TypeOfHousing должен быть установлен корректно");
    }

    @Test
    @DisplayName("TypeOfHousing может быть null")
    void testTypeOfHousingIsNullByDefault() {
        assertNull(address.getTypeOfHousing(), "TypeOfHousing должен быть null по умолчанию");
    }

    @Test
    @DisplayName("TypeOfHousing может быть установлен на null")
    void testSetTypeOfHousingToNull() {
        address.setTypeOfHousing(TypeOfHousing.FLAT);
        address.setTypeOfHousing(null);
        assertNull(address.getTypeOfHousing(), "TypeOfHousing может быть установлен на null");
    }

    @Test
    @DisplayName("Проверка установки и получения statusOfHousing через enum")
    void testSetAndGetStatusOfHousing() {
        StatusOfHousing statusOfHousing = StatusOfHousing.OWNER;
        address.setStatusOfHousing(statusOfHousing);
        assertEquals(statusOfHousing, address.getStatusOfHousing(), "StatusOfHousing должен быть установлен корректно");
    }

    @Test
    @DisplayName("StatusOfHousing может быть null")
    void testStatusOfHousingIsNullByDefault() {
        assertNull(address.getStatusOfHousing(), "StatusOfHousing должен быть null по умолчанию");
    }

    @Test
    @DisplayName("StatusOfHousing может быть установлен на null")
    void testSetStatusOfHousingToNull() {
        address.setStatusOfHousing(StatusOfHousing.OWNER);
        address.setStatusOfHousing(null);
        assertNull(address.getStatusOfHousing(), "StatusOfHousing может быть установлен на null");
    }

    @Test
    @DisplayName("getInstanceName() возвращает отформатированный адрес")
    void testGetInstanceNameWithAllFields() {
        address.setCity("Санкт-Петербург");
        address.setStreet("Невский проспект");
        address.setHouseNumber("42");
        address.setBody("3");
        address.setFlat("128");

        String instanceName = address.getInstanceName();
        assertNotNull(instanceName, "InstanceName не должен быть null");
        assertTrue(instanceName.contains("Санкт-Петербург"), "InstanceName должен содержать город");
        assertTrue(instanceName.contains("Невский проспект"), "InstanceName должен содержать улицу");
        assertTrue(instanceName.contains("42"), "InstanceName должен содержать номер дома");
    }

    @Test
    @DisplayName("getInstanceName() обрезает пробелы при null полях")
    void testGetInstanceNameWithNullFields() {
        address.setCity(null);
        address.setStreet("Невский проспект");
        address.setHouseNumber(null);
        address.setBody(null);
        address.setFlat("128");

        String instanceName = address.getInstanceName();
        assertNotNull(instanceName, "InstanceName не должен быть null");
    }

    @Test
    @DisplayName("getInstanceName() возвращает непустую строку")
    void testGetInstanceNameIsNotEmpty() {
        address.setCity("Москва");
        address.setStreet("Красная площадь");
        address.setHouseNumber("1");
        address.setBody("А");
        address.setFlat("1");

        String instanceName = address.getInstanceName();
        assertFalse(instanceName.isEmpty(), "InstanceName не должен быть пустой строкой");
    }

    @Test
    @DisplayName("Создание полностью заполненного Address объекта")
    void testCreateFullyPopulatedAddress() {
        UUID addressId = UUID.randomUUID();
        address.setId(addressId);
        address.setIndex("191186");
        address.setCity("Санкт-Петербург");
        address.setStreet("Невский проспект");
        address.setHouseNumber("42");
        address.setBody("3");
        address.setFlat("128");
        address.setTypeOfHousing(TypeOfHousing.FLAT);
        address.setStatusOfHousing(StatusOfHousing.OWNER);

        assertEquals(addressId, address.getId());
        assertEquals("191186", address.getIndex());
        assertEquals("Санкт-Петербург", address.getCity());
        assertEquals("Невский проспект", address.getStreet());
        assertEquals("42", address.getHouseNumber());
        assertEquals("3", address.getBody());
        assertEquals("128", address.getFlat());
        assertEquals(TypeOfHousing.FLAT, address.getTypeOfHousing());
        assertEquals(StatusOfHousing.OWNER, address.getStatusOfHousing());
    }

    @Test
    @DisplayName("Проверка что разные объекты Address имеют разные ID")
    void testDifferentAddressObjectsHaveDifferentIds() {
        Address address1 = dataManager.create(Address.class);
        Address address2 = dataManager.create(Address.class);

        assertNotEquals(address1.getId(), address2.getId(), "Разные Address объекты должны иметь разные ID");
    }

    @Test
    @DisplayName("Index с ровно 6 нулями проходит валидацию")
    void testIndexAllZeros() {
        address.setIndex("000000");
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("index")),
                "Index '000000' должен пройти валидацию");
    }

    @Test
    @DisplayName("Index с пробелом не проходит валидацию")
    void testIndexWithSpace() {
        address.setIndex("12345 ");
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("index")),
                "Index с пробелом должен не пройти валидацию");
    }

    @Test
    @DisplayName("Index null проходит валидацию")
    void testIndexNull() {
        address.setIndex(null);
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("index")),
                "Index null должен пройти валидацию (не требуется)");
    }

    @Test
    @DisplayName("City с максимальной длиной 50 символов проходит валидацию")
    void testCityMaxLength() {
        address.setCity("П".repeat(50));
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("city")),
                "City с 50 символами должен пройти валидацию");
    }

    @Test
    @DisplayName("HouseNumber с максимальной длиной 5 символов проходит валидацию")
    void testHouseNumberMaxLength() {
        address.setHouseNumber("42/16");
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("houseNumber")),
                "HouseNumber с 5 символами должен пройти валидацию");
    }

    @Test
    @DisplayName("Body с максимальной длиной 4 символа проходит валидацию")
    void testBodyMaxLength() {
        address.setBody("корп");
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("body")),
                "Body с 4 символами должен пройти валидацию");
    }

    @Test
    @DisplayName("Flat с максимальной длиной 10 символов проходит валидацию")
    void testFlatMaxLength() {
        address.setFlat("128-корп-3");
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("flat")),
                "Flat с 10 символами должен пройти валидацию");
    }

    @Test
    @DisplayName("Все enum значения TypeOfHousing работают корректно")
    void testAllTypeOfHousingValues() {
        for (TypeOfHousing type : TypeOfHousing.values()) {
            address.setTypeOfHousing(type);
            assertEquals(type, address.getTypeOfHousing(), "TypeOfHousing " + type + " должен работать корректно");
        }
    }

    @Test
    @DisplayName("Все enum значения StatusOfHousing работают корректно")
    void testAllStatusOfHousingValues() {
        for (StatusOfHousing status : StatusOfHousing.values()) {
            address.setStatusOfHousing(status);
            assertEquals(status, address.getStatusOfHousing(), "StatusOfHousing " + status + " должен работать корректно");
        }
    }

    @Test
    @DisplayName("Index и typeOfHousing независимо друг от друга")
    void testIndexAndTypeOfHousingIndependent() {
        address.setIndex("123456");
        address.setTypeOfHousing(TypeOfHousing.FLAT);

        address.setIndex("654321");
        assertEquals(TypeOfHousing.FLAT, address.getTypeOfHousing(), "TypeOfHousing должен оставаться неизменным");

        address.setTypeOfHousing(TypeOfHousing.HOUSE);
        assertEquals("654321", address.getIndex(), "Index должен оставаться неизменным");
    }

    @Test
    @DisplayName("Смешанные символы в city поддерживаются")
    void testCityWithMixedCharacters() {
        address.setCity("Санкт-Петербург 2.0");
        Set<ConstraintViolation<Address>> violations = validator.validate(address);
        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("city")),
                "City с смешанными символами должен пройти валидацию");
    }

    @Test
    @DisplayName("Специальные символы в street поддерживаются")
    void testStreetWithSpecialCharacters() {
        address.setStreet("Св. проспект (центр)");
        assertEquals("Св. проспект (центр)", address.getStreet(), "Street со специальными символами должен работать");
    }

    @Test
    @DisplayName("TypeOfHousing.FLAT имеет ID 'A'")
    void testTypeOfHousingFlatId() {
        assertEquals("A", TypeOfHousing.FLAT.getId(), "FLAT должен иметь ID 'A'");
    }

    @Test
    @DisplayName("TypeOfHousing.ROOM имеет ID 'B'")
    void testTypeOfHousingRoomId() {
        assertEquals("B", TypeOfHousing.ROOM.getId(), "ROOM должен иметь ID 'B'");
    }

    @Test
    @DisplayName("TypeOfHousing.HOUSE имеет ID 'C'")
    void testTypeOfHousingHouseId() {
        assertEquals("C", TypeOfHousing.HOUSE.getId(), "HOUSE должен иметь ID 'C'");
    }

    @Test
    @DisplayName("StatusOfHousing.OWNER имеет ID 'A'")
    void testStatusOfHousingOwnerId() {
        assertEquals("A", StatusOfHousing.OWNER.getId(), "OWNER должен иметь ID 'A'");
    }

    @Test
    @DisplayName("StatusOfHousing.SUBLEASED имеет ID 'B'")
    void testStatusOfHousingSubleasedId() {
        assertEquals("B", StatusOfHousing.SUBLEASED.getId(), "SUBLEASED должен иметь ID 'B'");
    }

    @Test
    @DisplayName("StatusOfHousing.RENTED имеет ID 'C'")
    void testStatusOfHousingRentedId() {
        assertEquals("C", StatusOfHousing.RENTED.getId(), "RENTED должен иметь ID 'C'");
    }

    @Test
    @DisplayName("StatusOfHousing.SHARED имеет ID 'D'")
    void testStatusOfHousingShaRedId() {
        assertEquals("D", StatusOfHousing.SHARED.getId(), "SHARED должен иметь ID 'D'");
    }

    @Test
    @DisplayName("TypeOfHousing.fromId('A') возвращает FLAT")
    void testTypeOfHousingFromIdFlat() {
        assertEquals(TypeOfHousing.FLAT, TypeOfHousing.fromId("A"), "fromId('A') должен вернуть FLAT");
    }

    @Test
    @DisplayName("TypeOfHousing.fromId('B') возвращает ROOM")
    void testTypeOfHousingFromIdRoom() {
        assertEquals(TypeOfHousing.ROOM, TypeOfHousing.fromId("B"), "fromId('B') должен вернуть ROOM");
    }

    @Test
    @DisplayName("TypeOfHousing.fromId('C') возвращает HOUSE")
    void testTypeOfHousingFromIdHouse() {
        assertEquals(TypeOfHousing.HOUSE, TypeOfHousing.fromId("C"), "fromId('C') должен вернуть HOUSE");
    }

    @Test
    @DisplayName("TypeOfHousing.fromId(null) возвращает null")
    void testTypeOfHousingFromIdNull() {
        assertNull(TypeOfHousing.fromId(null), "fromId(null) должен вернуть null");
    }

    @Test
    @DisplayName("TypeOfHousing.fromId('invalid') возвращает null")
    void testTypeOfHousingFromIdInvalid() {
        assertNull(TypeOfHousing.fromId("invalid"), "fromId('invalid') должен вернуть null");
    }

    @Test
    @DisplayName("StatusOfHousing.fromId('A') возвращает OWNER")
    void testStatusOfHousingFromIdOwner() {
        assertEquals(StatusOfHousing.OWNER, StatusOfHousing.fromId("A"), "fromId('A') должен вернуть OWNER");
    }

    @Test
    @DisplayName("StatusOfHousing.fromId('B') возвращает SUBLEASED")
    void testStatusOfHousingFromIdSubleased() {
        assertEquals(StatusOfHousing.SUBLEASED, StatusOfHousing.fromId("B"), "fromId('B') должен вернуть SUBLEASED");
    }

    @Test
    @DisplayName("StatusOfHousing.fromId('C') возвращает RENTED")
    void testStatusOfHousingFromIdRented() {
        assertEquals(StatusOfHousing.RENTED, StatusOfHousing.fromId("C"), "fromId('C') должен вернуть RENTED");
    }

    @Test
    @DisplayName("StatusOfHousing.fromId('D') возвращает SHARED")
    void testStatusOfHousingFromIdShared() {
        assertEquals(StatusOfHousing.SHARED, StatusOfHousing.fromId("D"), "fromId('D') должен вернуть SHARED");
    }

    @Test
    @DisplayName("StatusOfHousing.fromId(null) возвращает null")
    void testStatusOfHousingFromIdNull() {
        assertNull(StatusOfHousing.fromId(null), "fromId(null) должен вернуть null");
    }

    @Test
    @DisplayName("StatusOfHousing.fromId('invalid') возвращает null")
    void testStatusOfHousingFromIdInvalid() {
        assertNull(StatusOfHousing.fromId("invalid"), "fromId('invalid') должен вернуть null");
    }
}