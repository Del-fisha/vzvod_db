package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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


}
