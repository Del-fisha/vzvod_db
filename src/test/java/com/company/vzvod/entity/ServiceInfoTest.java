package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

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

    @Test
    @DisplayName("Работа с коллекцией penalty")
    void testPenaltyCollection() {
        serviceInfo.setPenalty(new ArrayList<>());
        assertTrue(serviceInfo.getPenalty().isEmpty());

        Penalty p1 = dataManager.create(Penalty.class);
        Penalty p2 = dataManager.create(Penalty.class);
        Penalty p3 = dataManager.create(Penalty.class);

        List<Penalty> list = new ArrayList<>();
        list.add(p1);
        list.add(p2);
        serviceInfo.setPenalty(list);

        assertEquals(2, serviceInfo.getPenalty().size());
        assertTrue(serviceInfo.getPenalty().contains(p1));

        serviceInfo.getPenalty().add(p3);
        assertEquals(3, serviceInfo.getPenalty().size());
        assertTrue(serviceInfo.getPenalty().contains(p3));
    }

    @Test
    @DisplayName("Работа с коллекцией incentive")
    void testIncentiveCollection() {
        serviceInfo.setIncentive(new ArrayList<>());
        assertTrue(serviceInfo.getIncentive().isEmpty());

        Incentive incentive1 = dataManager.create(Incentive.class);
        Incentive incentive2 = dataManager.create(Incentive.class);
        Incentive incentive3 = dataManager.create(Incentive.class);

        List<Incentive> list = new ArrayList<>();
        list.add(incentive1);
        list.add(incentive2);
        serviceInfo.setIncentive(list);

        assertEquals(2, serviceInfo.getIncentive().size());
        assertTrue(serviceInfo.getIncentive().contains(incentive1));

        serviceInfo.getIncentive().add(incentive3);
        assertEquals(3, serviceInfo.getIncentive().size());
        assertTrue(serviceInfo.getIncentive().contains(incentive3));
    }

    @Test
    @DisplayName("Работа с коллекцией shifts")
    void testShiftsCollection() {
        serviceInfo.setShifts(new HashSet<>());
        assertTrue(serviceInfo.getShifts().isEmpty());

        Shift shift1 = dataManager.create(Shift.class);
        Shift shift2 = dataManager.create(Shift.class);
        Shift shift3 = dataManager.create(Shift.class);

        Set<Shift> list = new HashSet<>();
        list.add(shift1);
        list.add(shift2);
        serviceInfo.setShifts(list);

        assertEquals(2, serviceInfo.getShifts().size());
        assertTrue(serviceInfo.getShifts().contains(shift1));

        serviceInfo.getShifts().add(shift3);
        assertEquals(3, serviceInfo.getShifts().size());
        assertTrue(serviceInfo.getShifts().contains(shift3));
    }

    @Test
    @DisplayName("Работа с коллекцией vocations")
    void testVocationCollection() {
        serviceInfo.setVocations(new ArrayList<>());
        assertTrue(serviceInfo.getVocations().isEmpty());

        Vocation vocation1 = dataManager.create(Vocation.class);
        Vocation vocation2 = dataManager.create(Vocation.class);
        Vocation vocation3 = dataManager.create(Vocation.class);

        List<Vocation> list = new ArrayList<>();
        list.add(vocation1);
        list.add(vocation2);
        serviceInfo.setVocations(list);

        assertEquals(2, serviceInfo.getVocations().size());
        assertTrue(serviceInfo.getVocations().contains(vocation1));

        serviceInfo.getVocations().add(vocation3);
        assertEquals(3, serviceInfo.getVocations().size());
        assertTrue(serviceInfo.getVocations().contains(vocation3));
    }

    @Test
    @DisplayName("Установка и получение medicalExamination")
    void testMedicalExamination() {
        serviceInfo.setMedicalExamination(true);
        assertTrue(serviceInfo.getMedicalExamination());

        serviceInfo.setMedicalExamination(false);
        assertFalse(serviceInfo.getMedicalExamination());
    }

    @Test
    @DisplayName("Установка и получение qualificationClass")
    void testQualificationClass() {
        serviceInfo.setQualificationClass(Qualification.MASTER);
        assertEquals(Qualification.MASTER, serviceInfo.getQualificationClass());

        serviceInfo.setQualificationClass(null);
        assertNull(serviceInfo.getQualificationClass());
    }

}
