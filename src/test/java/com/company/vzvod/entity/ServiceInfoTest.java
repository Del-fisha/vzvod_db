package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceInfoTest extends EntityTestSupport {

    private ServiceInfo serviceInfo;

    @BeforeEach
    void setUp() {
        serviceInfo = dataManager.create(ServiceInfo.class);
    }

    @Test
    @DisplayName("Проверка установки и получения ID")
    void testId() {
        UUID originalId = serviceInfo.getId();

        assertNotNull(originalId);

        UUID newId = UUID.randomUUID();
        serviceInfo.setId(newId);
        assertEquals(serviceInfo.getId(), newId);
    }

    @Test
    @DisplayName("Проверка поля user")
    void testUser() {
        assertNull(serviceInfo.getUser());

        User user = dataManager.create(User.class);
        serviceInfo.setUser(user);

        assertEquals(user, serviceInfo.getUser());
    }

    @Test
    @DisplayName("Проверка поля department")
    void testDepartment() {
        assertNull(serviceInfo.getDepartment());

        Department department = dataManager.create(Department.class);
        serviceInfo.setDepartment(department);
        assertEquals(department, serviceInfo.getDepartment());
    }

    @Test
    @DisplayName("Проверка поля rank (enum)")
    void testRank() {
        assertNull(serviceInfo.getRank());

        serviceInfo.setRank(Rank.INTERN);
        assertEquals(Rank.INTERN, serviceInfo.getRank());
    }

    @Test
    @DisplayName("Проверка поля status (enum)")
    void testStatus() {
        assertNull(serviceInfo.getStatus());
        var violations = validator.validate(serviceInfo);
        assertFalse(violations.isEmpty());

        serviceInfo.setStatus(StatusInService.ACTIVE);
        assertEquals(StatusInService.ACTIVE, serviceInfo.getStatus());
    }

    @Test
    @DisplayName("Проверка поля post (enum)")
    void testPost() {
        assertNull(serviceInfo.getPost());
        var violations = validator.validate(serviceInfo);
        assertFalse(violations.isEmpty());

        serviceInfo.setPost(Post.COM_OTD);
        assertEquals(Post.COM_OTD, serviceInfo.getPost());
    }

    @Test
    @DisplayName("Проверка поля idCard")
    void testIdCard() {
        assertNull(serviceInfo.getIdCard());

        IdCard idCard = dataManager.create(IdCard.class);

        serviceInfo.setIdCard(idCard);
        assertEquals(serviceInfo.getIdCard(), idCard);
    }

    @Test
    @DisplayName("Проверка поля token")
    void testToken() {
        assertNull(serviceInfo.getToken());

        String emptyToken = "";
        String blankToken = " ";
        String goodToken = "154786";

        serviceInfo.setToken(emptyToken);
        var violationEmptyToken = validator.validate(serviceInfo);
        assertFalse(violationEmptyToken.isEmpty());

        serviceInfo.setToken(blankToken);
        var violationBlankToken = validator.validate(serviceInfo);
        assertFalse(violationBlankToken.isEmpty());

        serviceInfo.setToken(goodToken);
        assertEquals(goodToken, serviceInfo.getToken());
    }

    @Test
    @DisplayName("Проверка поля breastplate")
    void testBreastplate() {
        assertNull(serviceInfo.getBreastplate());

        String breastplateLen7 = "1234567";
        String breastplateLen9 = "123456789";
        String breastplateEmpty = "";
        String breastplateBlank = " ";
        String breastplateGood = "12345678";

        serviceInfo.setBreastplate(breastplateLen7);
        var violationLen7 = validator.validate(serviceInfo);
        assertFalse(violationLen7.isEmpty());

        serviceInfo.setBreastplate(breastplateLen9);
        var violationLen9 = validator.validate(serviceInfo);
        assertFalse(violationLen9.isEmpty());

        serviceInfo.setBreastplate(breastplateEmpty);
        var violationEmpty = validator.validate(serviceInfo);
        assertFalse(violationEmpty.isEmpty());

        serviceInfo.setBreastplate(breastplateBlank);
        var violationBlank = validator.validate(serviceInfo);
        assertFalse(violationBlank.isEmpty());

        serviceInfo.setBreastplate(breastplateGood);
        assertEquals(breastplateGood, serviceInfo.getBreastplate());
    }

    @Test
    @DisplayName("Проверка поля startDate")
    void testStartDate() {
        assertNull(serviceInfo.getStartDate());

        serviceInfo.setToken("123456");
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setUser(dataManager.create(User.class));

        LocalDate past = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        LocalDate future = LocalDate.now().plusDays(1);

        serviceInfo.setStartDate(future);
        var violationFuture = validator.validate(serviceInfo);
        assertFalse(violationFuture.isEmpty());

        serviceInfo.setStartDate(today);
        var violationToday = validator.validate(serviceInfo);
        assertTrue(violationToday.isEmpty());
        assertEquals(today, serviceInfo.getStartDate());

        serviceInfo.setStartDate(past);
        var violationPast = validator.validate(serviceInfo);
        assertTrue(violationPast.isEmpty());
        assertEquals(past, serviceInfo.getStartDate());
    }

    @Test
    @DisplayName("Проверка поля startOfPost")
    void testStartOfPost() {
        assertNull(serviceInfo.getStartDate());

        serviceInfo.setToken("123456");
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setUser(dataManager.create(User.class));

        LocalDate past = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        LocalDate future = LocalDate.now().plusDays(1);

        serviceInfo.setStartOfPost(future);
        var violationFuture = validator.validate(serviceInfo);
        assertFalse(violationFuture.isEmpty());

        serviceInfo.setStartOfPost(today);
        var violationToday = validator.validate(serviceInfo);
        assertTrue(violationToday.isEmpty());
        assertEquals(today, serviceInfo.getStartOfPost());

        serviceInfo.setStartOfPost(past);
        var violationPast = validator.validate(serviceInfo);
        assertTrue(violationPast.isEmpty());
        assertEquals(past, serviceInfo.getStartOfPost());
    }
}
