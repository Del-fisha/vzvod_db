package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit-тесты для Department Entity")
public class DepartmentTest extends EntityTestSupport {

    private Department department;

    @BeforeEach
    void setUp() {
        department = dataManager.create(Department.class);
    }

    @Test
    @DisplayName("Проверка установки и получения ID")
    void testId() {
        UUID originalId = department.getId();
        assertNotNull(originalId, "ID не должен быть null после создания через DataManager");
        UUID newId = UUID.randomUUID();
        department.setId(newId);
        assertEquals(newId, department.getId(), "ID должен измениться после setter");
    }

    @Test
    @DisplayName("Проверка установки и получения number")
    void testNumber() {
        Integer number = 2;
        department.setNumber(number);
        assertEquals(2, department.getNumber());
    }

    @Test
    @DisplayName("Проверка добавления/удаления serviceInfo")
    void testServiceInfo() {
        assertNotNull(department.getServiceInfos());

        assertTrue(department.getServiceInfos().isEmpty());

        ServiceInfo serviceInfo1 = dataManager.create(ServiceInfo.class);
        ServiceInfo serviceInfo2 = dataManager.create(ServiceInfo.class);

        department.getServiceInfos().add(serviceInfo1);
        department.getServiceInfos().add(serviceInfo2);

        assertEquals(2, department.getServiceInfos().size());

        assertNotSame(department.getServiceInfos().get(0).getId(), department.getServiceInfos().get(1).getId());

        assertTrue(department.getServiceInfos().contains(serviceInfo1));
        assertTrue(department.getServiceInfos().contains(serviceInfo1));
    }

    @Test // ToDo Тест падает
    @DisplayName("Проверка метода getInstanceName()")
    void testInstanceName() {
        department.setNumber(10);
        String instanceName = department.getInstanceName(datatypeFormatter);
        assertEquals("10", instanceName, "InstanceName должен быть строковым представлением числа");

        department.setNumber(null);
        instanceName = department.getInstanceName(datatypeFormatter);
        assertNull(instanceName, "При number = null метод должен возвращать null (потенциальная проблема)");
    }
}
