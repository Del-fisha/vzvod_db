package com.company.vzvod.entity;

import io.jmix.core.DataManager;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Unit-тесты для User Entity")
class UserTest {

    @Autowired
    protected DataManager dataManager;

    private User user;

    @Autowired(required = false)
    private Validator validator;

    @BeforeEach
    void setUp() {
        user = dataManager.create(User.class);
    }


    @Test
    @DisplayName("Проверка установки и получения ID")
    void testSetAndGetId() {
        UUID testId = UUID.randomUUID();

        user.setId(testId);

        assertEquals(testId, user.getId(), "ID должен быть установлен корректно");
    }

    @Test
    @DisplayName("ID автоматически генерируется при создании через dataManager")
    void testIdIsGeneratedByDataManager() {

        assertNotNull(user.getId(), "ID должен быть автоматически сгенерирован dataManager'ом");
        assertInstanceOf(UUID.class, user.getId(), "ID должен быть типа UUID");
    }


    @Test
    @DisplayName("Проверка установки и получения username")
    void testSetAndGetUsername() {
        String testUsername = "admin";

        user.setUsername(testUsername);

        assertEquals(testUsername, user.getUsername(), "Username должен быть установлен корректно");
    }

    @Test
    @DisplayName("Username может быть установлен в пустую строку программно (валидация на уровне БД)")
    void testSetUsernameToEmpty() {
        user.setUsername("");

        assertEquals("", user.getUsername(), "Username может быть пустой строкой программно");
    }


    @Test
    @DisplayName("Проверка установки и получения password")
    void testSetAndGetPassword() {

        String testPassword = "encryptedPassword123";

        user.setPassword(testPassword);

        assertEquals(testPassword, user.getPassword(), "Password должен быть установлен корректно");
    }

    @Test
    @DisplayName("Password может быть null (это нормально для пользователей)")
    void testPasswordIsNullByDefault() {

        assertNull(user.getPassword(), "Password должен быть null по умолчанию");
    }

    @Test
    @DisplayName("Password может быть установлен на null")
    void testSetPasswordToNull() {

        user.setPassword("somePassword");


        user.setPassword(null);

        assertNull(user.getPassword(), "Password может быть установлен на null");
    }

    @Test
    @DisplayName("Проверка установки и получения firstName")
    void testSetAndGetFirstName() {
        String testFirstName = "Иван";

        user.setFirstName(testFirstName);

        assertEquals(testFirstName, user.getFirstName(), "FirstName должен быть установлен корректно");
    }

    @Test
    @DisplayName("FirstName длиной ровно в 20 символов должен приниматься")
    void testSetFirstNameWithMaxLength() {
        String testFirstName = "А".repeat(20);

        user.setFirstName(testFirstName);

        assertEquals(testFirstName, user.getFirstName(), "FirstName длиной 20 символов должен приниматься");
    }

    @Test
    @DisplayName("FirstName может быть только одним символом")
    void testSetFirstNameWithMinLength() {

        String testFirstName = "В";

        user.setFirstName(testFirstName);

        assertEquals(testFirstName, user.getFirstName(), "FirstName может быть одним символом");
    }


    @Test
    @DisplayName("Проверка установки и получения lastName")
    void testSetAndGetLastName() {

        String testLastName = "Петров";

        user.setLastName(testLastName);

        assertEquals(testLastName, user.getLastName(), "LastName должен быть установлен корректно");
    }

    @Test
    @DisplayName("Проверка установки и получения patronymic")
    void testSetAndGetPatronymic() {

        String testPatronymic = "Иванович";

        user.setPatronymic(testPatronymic);

        assertEquals(testPatronymic, user.getPatronymic(), "Patronymic должен быть установлен корректно");
    }

    @Test
    @DisplayName("Проверка установки и получения dateOfBirth")
    void testSetAndGetDateOfBirth() {

        LocalDate testDate = LocalDate.of(1990, 5, 15);

        user.setDateOfBirth(testDate);

        assertEquals(testDate, user.getDateOfBirth(), "DateOfBirth должен быть установлен корректно");
    }

    @Test
    @DisplayName("DateOfBirth может быть датой в прошлом")
    void testSetDateOfBirthToOldDate() {

        LocalDate oldDate = LocalDate.of(1950, 1, 1);

        user.setDateOfBirth(oldDate);

        assertEquals(oldDate, user.getDateOfBirth(), "DateOfBirth может быть очень старой датой");
    }

    @Test
    @DisplayName("DateOfBirth может быть датой вчера")
    void testSetDateOfBirthToYesterday() {

        LocalDate yesterday = LocalDate.now().minusDays(1);

        user.setDateOfBirth(yesterday);

        assertEquals(yesterday, user.getDateOfBirth(), "DateOfBirth может быть вчера");
    }


    @Test
    @DisplayName("Проверка установки и получения serviceInfo")
    void testSetAndGetServiceInfo() {

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        serviceInfo.setId(UUID.randomUUID());

        user.setServiceInfo(serviceInfo);

        assertNotNull(user.getServiceInfo(), "ServiceInfo должен быть установлен");
        assertEquals(serviceInfo.getId(), user.getServiceInfo().getId(), "ServiceInfo должен совпадать");
    }

    @Test
    @DisplayName("ServiceInfo может быть null")
    void testServiceInfoIsNullByDefault() {

        assertNull(user.getServiceInfo(), "ServiceInfo должен быть null по умолчанию");
    }

    @Test
    @DisplayName("Проверка установки и получения contactsInfo")
    void testSetAndGetContactsInfo() {

        Contacts contacts = dataManager.create(Contacts.class);
        contacts.setId(UUID.randomUUID());

        user.setContactsInfo(contacts);

        assertNotNull(user.getContactsInfo(), "ContactsInfo должен быть установлен");
        assertEquals(contacts.getId(), user.getContactsInfo().getId(), "ContactsInfo должен совпадать");
    }

    @Test
    @DisplayName("Проверка установки и получения education")
    void testSetAndGetEducation() {

        Education education = dataManager.create(Education.class);
        education.setId(UUID.randomUUID());

        user.setEducation(education);

        assertNotNull(user.getEducation(), "Education должен быть установлен");
        assertEquals(education.getId(), user.getEducation().getId(), "Education должен совпадать");
    }

    @Test
    @DisplayName("Проверка установки и получения vehicleInfo (список)")
    void testSetAndGetVehicleInfo() {

        Vehicle vehicle1 = dataManager.create(Vehicle.class);
        vehicle1.setId(UUID.randomUUID());

        Vehicle vehicle2 = dataManager.create(Vehicle.class);
        vehicle2.setId(UUID.randomUUID());

        List<Vehicle> vehicles = Arrays.asList(vehicle1, vehicle2);

        user.setVehicleInfo(vehicles);

        assertNotNull(user.getVehicleInfo(), "VehicleInfo не должен быть null");
        assertEquals(2, user.getVehicleInfo().size(), "Должно быть 2 транспортных средства");
        assertTrue(user.getVehicleInfo().contains(vehicle1), "Список должен содержать vehicle1");
        assertTrue(user.getVehicleInfo().contains(vehicle2), "Список должен содержать vehicle2");
    }

    @Test
    @DisplayName("VehicleInfo может быть пустым списком")
    void testSetVehicleInfoToEmptyList() {

        List<Vehicle> emptyList = new ArrayList<>();

        user.setVehicleInfo(emptyList);

        assertNotNull(user.getVehicleInfo(), "VehicleInfo не должен быть null");
        assertTrue(user.getVehicleInfo().isEmpty(), "Список должен быть пустым");
    }


    @Test
    @DisplayName("getDisplayName() возвращает \"Фамилия Имя Отчество\"")
    void testGetDisplayNameWithAllFields() {

        user.setLastName("Петров");
        user.setFirstName("Иван");
        user.setPatronymic("Иванович");

        String displayName = user.getDisplayName();

        assertEquals("Петров Иван Иванович", displayName, "DisplayName должен быть в формате 'Фамилия Имя Отчество'");
    }

    @Test
    @DisplayName("getDisplayName() обрезает пробелы, если фамилия отсутствует")
    void testGetDisplayNameWithoutLastName() {

        user.setLastName(null);
        user.setFirstName("Иван");
        user.setPatronymic("Иванович");

        String displayName = user.getDisplayName();

        assertEquals("Иван Иванович", displayName, "DisplayName должен быть 'Имя Отчество' без лишних пробелов");
    }

    @Test
    @DisplayName("getDisplayName() обрезает пробелы, если имя отсутствует")
    void testGetDisplayNameWithoutFirstName() {

        user.setLastName("Петров");
        user.setFirstName(null);
        user.setPatronymic("Иванович");

        String displayName = user.getDisplayName();

        assertEquals("Петров Иванович", displayName, "DisplayName должен быть 'Фамилия Отчество' без лишних пробелов");
    }

    @Test
    @DisplayName("getDisplayName() обрезает пробелы, если отчество отсутствует")
    void testGetDisplayNameWithoutPatronymic() {

        user.setLastName("Петров");
        user.setFirstName("Иван");
        user.setPatronymic(null);

        String displayName = user.getDisplayName();

        assertEquals("Петров Иван", displayName, "DisplayName должен быть 'Фамилия Имя' без лишних пробелов");
    }

    @Test
    @DisplayName("getDisplayName() возвращает пустую строку, если все поля null")
    void testGetDisplayNameWithAllFieldsNull() {

        user.setLastName(null);
        user.setFirstName(null);
        user.setPatronymic(null);

        String displayName = user.getDisplayName();

        assertEquals("", displayName, "DisplayName должен быть пустой строкой");
    }

    @Test
    @DisplayName("getDisplayName() возвращает пустую строку, если все поля - пустые строки")
    void testGetDisplayNameWithEmptyStrings() {

        user.setLastName("");
        user.setFirstName("");
        user.setPatronymic("");

        String displayName = user.getDisplayName();

        assertEquals("", displayName, "DisplayName должен быть пустой строкой");
    }

    @Test
    @DisplayName("getDisplayName() только с фамилией")
    void testGetDisplayNameOnlyLastName() {

        user.setLastName("Петров");
        user.setFirstName(null);
        user.setPatronymic(null);

        String displayName = user.getDisplayName();

        assertEquals("Петров", displayName, "DisplayName должен быть только 'Фамилия'");
    }

    @Test
    @DisplayName("getUsername() возвращает то же значение, что и поле username")
    void testGetUsernameFromJmixUserDetails() {

        String username = "testUser";
        user.setUsername(username);

        String result = user.getUsername();

        assertEquals(username, result, "getUsername() должен вернуть установленное значение");
    }

    @Test
    @DisplayName("getAuthorities() возвращает пустую коллекцию по умолчанию")
    void testGetAuthoritiesDefaultEmpty() {

        Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities, "Authorities не должен быть null");
        assertTrue(authorities.isEmpty(), "Authorities должен быть пустым по умолчанию");
    }

    @Test
    @DisplayName("setAuthorities() устанавливает права доступа")
    void testSetAuthorities() {

        List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
        org.springframework.security.core.GrantedAuthority authority =
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER");
        authorities.add(authority);

        user.setAuthorities(authorities);

        assertNotNull(user.getAuthorities(), "Authorities не должны быть null");
        assertEquals(1, user.getAuthorities().size(), "Должна быть одна role");
        assertTrue(user.getAuthorities().contains(authority), "Authorities должны содержать установленную роль");
    }

    @Test
    @DisplayName("isAccountNonExpired() всегда возвращает true")
    void testIsAccountNonExpired() {

        boolean result = user.isAccountNonExpired();

        assertTrue(result, "isAccountNonExpired() должен возвращать true");
    }

    @Test
    @DisplayName("isAccountNonLocked() всегда возвращает true")
    void testIsAccountNonLocked() {

        boolean result = user.isAccountNonLocked();

        assertTrue(result, "isAccountNonLocked() должен возвращать true");
    }

    @Test
    @DisplayName("isCredentialsNonExpired() всегда возвращает true")
    void testIsCredentialsNonExpired() {

        boolean result = user.isCredentialsNonExpired();

        assertTrue(result, "isCredentialsNonExpired() должен возвращать true");
    }

    @Test
    @DisplayName("Создание полностью заполненного User объекта")
    void testCreateFullyPopulatedUser() {

        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setUsername("petrov_i");
        user.setPassword("encrypted_pwd");
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setPatronymic("Иванович");
        user.setDateOfBirth(LocalDate.of(1990, 5, 15));


        assertEquals(userId, user.getId());
        assertEquals("petrov_i", user.getUsername());
        assertEquals("encrypted_pwd", user.getPassword());
        assertEquals("Иван", user.getFirstName());
        assertEquals("Петров", user.getLastName());
        assertEquals("Иванович", user.getPatronymic());
        assertEquals(LocalDate.of(1990, 5, 15), user.getDateOfBirth());
        assertEquals("Петров Иван Иванович", user.getDisplayName());
    }

    @Test
    @DisplayName("Проверка что разные объекты User имеют разные ID")
    void testDifferentUserObjectsHaveDifferentIds() {

        User user1 = dataManager.create(User.class);
        User user2 = dataManager.create(User.class);
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        user1.setId(id1);
        user2.setId(id2);

        assertNotEquals(user1.getId(), user2.getId(), "Разные User объекты должны иметь разные ID");
    }

    @Test
    @DisplayName("@NotEmpty: username пустая строка вызывает ошибку валидации")
    void testUsernameEmptyStringValidation() {

        user.setUsername("");
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setPatronymic("Иванович");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("username")),
                "Должна быть ошибка валидации для пустого username");
    }

    @Test
    @DisplayName("@NotEmpty: username null вызывает ошибку валидации")
    void testUsernameNullValidation() {

        user.setUsername(null);
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setPatronymic("Иванович");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("username")),
                "Должна быть ошибка валидации для null username");
    }

    @Test
    @DisplayName("@NotBlank: username только с пробелами вызывает ошибку")
    void testUsernameBlankValidation() {

        user.setUsername("   ");
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setPatronymic("Иванович");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("username")),
                "Username только из пробелов должен не пройти @NotBlank");
    }

    @Test
    @DisplayName("@NotEmpty: firstName пустая строка вызывает ошибку валидации")
    void testFirstNameEmptyStringValidation() {

        user.setFirstName("");
        user.setLastName("Петров");
        user.setPatronymic("Иванович");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")),
                "Должна быть ошибка валидации для пустого firstName");
    }

    @Test
    @DisplayName("@NotEmpty: firstName null вызывает ошибку валидации")
    void testFirstNameNullValidation() {

        user.setFirstName(null);
        user.setLastName("Петров");
        user.setPatronymic("Иванович");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")),
                "Должна быть ошибка валидации для null firstName");
    }

    @Test
    @DisplayName("@NotBlank: firstName только с пробелами вызывает ошибку")
    void testFirstNameBlankValidation() {

        user.setFirstName("   ");
        user.setLastName("Петров");
        user.setPatronymic("Иванович");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")),
                "FirstName только из пробелов должен не пройти @NotBlank");
    }

    @Test
    @DisplayName("@NotEmpty: lastName пустая строка вызывает ошибку валидации")
    void testLastNameEmptyStringValidation() {

        user.setLastName("");
        user.setFirstName("Иван");
        user.setPatronymic("Иванович");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("lastName")),
                "Должна быть ошибка валидации для пустого lastName");
    }

    @Test
    @DisplayName("@NotEmpty: lastName null вызывает ошибку валидации")
    void testLastNameNullValidation() {

        user.setLastName(null);
        user.setFirstName("Иван");
        user.setPatronymic("Иванович");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("lastName")),
                "Должна быть ошибка валидации для null lastName");
    }

    @Test
    @DisplayName("@NotBlank: lastName только с пробелами вызывает ошибку")
    void testLastNameBlankValidation() {

        user.setLastName("   ");
        user.setFirstName("Иван");
        user.setPatronymic("Иванович");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("lastName")),
                "LastName только из пробелов должен не пройти @NotBlank");
    }

    @Test
    @DisplayName("@NotEmpty: patronymic пустая строка вызывает ошибку валидации")
    void testPatronymicEmptyStringValidation() {

        user.setPatronymic("");
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("patronymic")),
                "Должна быть ошибка валидации для пустого patronymic");
    }

    @Test
    @DisplayName("@NotEmpty: patronymic null вызывает ошибку валидации")
    void testPatronymicNullValidation() {

        user.setPatronymic(null);
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("patronymic")),
                "Должна быть ошибка валидации для null patronymic");
    }

    @Test
    @DisplayName("@NotBlank: patronymic только с пробелами вызывает ошибку")
    void testPatronymicBlankValidation() {

        user.setPatronymic("   ");
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("patronymic")),
                "Patronymic только из пробелов должен не пройти @NotBlank");
    }

    @Test
    @DisplayName("@Past: dateOfBirth в будущем вызывает ошибку валидации")
    void testDateOfBirthFutureValidation() {

        user.setDateOfBirth(java.time.LocalDate.now().plusDays(1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")),
                "DateOfBirth в будущем должен вызвать ошибку валидации");
    }

    @Test
    @DisplayName("@Past: dateOfBirth сегодня вызывает ошибку валидации")
    void testDateOfBirthTodayValidation() {

        user.setDateOfBirth(java.time.LocalDate.now());

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")),
                "DateOfBirth сегодня должен вызвать ошибку валидации");
    }

    @Test
    @DisplayName("@Past: dateOfBirth вчера проходит валидацию")
    void testDateOfBirthYesterdayValidation() {

        user.setDateOfBirth(java.time.LocalDate.now().minusDays(1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")),
                "DateOfBirth вчера должен пройти валидацию");
    }

    @Test
    @DisplayName("@Past: dateOfBirth в далёком прошлом проходит валидацию")
    void testDateOfBirthOldDateValidation() {

        user.setDateOfBirth(java.time.LocalDate.of(1950, 1, 1));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")),
                "DateOfBirth в далёком прошлом должен пройти валидацию");
    }

    @Test
    @DisplayName("Валидация: все поля null вызывают ошибки")
    void testAllFieldsNullValidation() {

        user.setUsername(null);
        user.setFirstName(null);
        user.setLastName(null);
        user.setPatronymic(null);
        user.setDateOfBirth(null);

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertEquals(8, violations.size(), "Должно быть 8 ошибок валидации (2 на каждое строковое поле: @NotEmpty + @NotBlank)");
    }


    @Test
    @DisplayName("Валидация: все поля пустые строки вызывают ошибки")
    void testAllFieldsEmptyValidation() {

        user.setUsername("");
        user.setFirstName("");
        user.setLastName("");
        user.setPatronymic("");

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.size() >= 4, "Должно быть минимум 4 ошибки валидации");
    }

    @Test
    @DisplayName("Валидация: корректный User проходит все проверки")
    void testValidUserPassesValidation() {

        user.setUsername("petrov_ivan_1990");
        user.setFirstName("Иван");
        user.setLastName("Петров");
        user.setPatronymic("Иванович");
        user.setDateOfBirth(java.time.LocalDate.of(1990, 5, 15));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Корректный User должен пройти всю валидацию без ошибок");
    }

    @Test
    @DisplayName("Валидация: firstName с максимальной длиной (20 символов) проходит")
    void testFirstNameMaxLengthValidation() {

        user.setFirstName("А".repeat(20));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")),
                "FirstName с 20 символами должен пройти валидацию");
    }

    @Test
    @DisplayName("Валидация: lastName с максимальной длиной (20 символов) проходит")
    void testLastNameMaxLengthValidation() {

        user.setLastName("П".repeat(20));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("lastName")),
                "LastName с 20 символами должен пройти валидацию");
    }

    @Test
    @DisplayName("Валидация: patronymic с максимальной длиной (20 символов) проходит")
    void testPatronymicMaxLengthValidation() {

        user.setPatronymic("И".repeat(20));

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("patronymic")),
                "Patronymic с 20 символами должен пройти валидацию");
    }

    @Test
    @DisplayName("Валидация: username с русскими символами проходит")
    void testUsernameWithCyrillicValidation() {

        user.setUsername("пользователь123");

        java.util.Set<jakarta.validation.ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("username")),
                "Username с русскими символами должен пройти валидацию");
    }

}
