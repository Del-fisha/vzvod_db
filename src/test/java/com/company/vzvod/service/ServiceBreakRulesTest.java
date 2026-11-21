package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceBreak;
import com.company.vzvod.entity.ServiceInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServiceBreakRulesTest {

    @Test
    @DisplayName("Пустой список перерывов сохранять можно")
    void emptyBreaksAreValid() {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setStartDate(LocalDate.of(2014, 1, 10));
        serviceInfo.setServiceBreaks(new ArrayList<>());

        assertNull(ServiceBreakRules.problem(serviceInfo));
    }

    @Test
    @DisplayName("Перерыв без начала службы, с пустой датой, с «По» раньше «С», раньше службы и с пересечением")
    void rejectsIncompleteAndOverlappingBreaks() {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceBreaks(List.of(breakBetween(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 2, 1))));
        assertEquals(ServiceBreakRules.NO_START, ServiceBreakRules.problem(serviceInfo));

        serviceInfo.setStartDate(LocalDate.of(2014, 1, 10));
        serviceInfo.setServiceBreaks(List.of(breakBetween(null, LocalDate.of(2016, 2, 1))));
        assertEquals(ServiceBreakRules.DATES_REQUIRED, ServiceBreakRules.problem(serviceInfo));

        serviceInfo.setServiceBreaks(List.of(breakBetween(LocalDate.of(2016, 3, 1), LocalDate.of(2016, 2, 1))));
        assertEquals(ServiceBreakRules.END_BEFORE_START, ServiceBreakRules.problem(serviceInfo));

        serviceInfo.setServiceBreaks(List.of(breakBetween(LocalDate.of(2013, 12, 1), LocalDate.of(2014, 2, 1))));
        assertEquals(ServiceBreakRules.BEFORE_SERVICE, ServiceBreakRules.problem(serviceInfo));

        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2016, 2, 1), LocalDate.of(2016, 2, 20)),
                breakBetween(LocalDate.of(2016, 2, 10), LocalDate.of(2016, 2, 25))
        ));
        assertEquals(ServiceBreakRules.OVERLAP, ServiceBreakRules.problem(serviceInfo));
    }

    @Test
    @DisplayName("Соседние перерывы, которые не делят один день, сохранять можно")
    void adjacentBreaksAreValid() {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setStartDate(LocalDate.of(2014, 1, 10));
        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2016, 2, 1), LocalDate.of(2016, 2, 20)),
                breakBetween(LocalDate.of(2016, 2, 21), LocalDate.of(2016, 3, 10))
        ));

        assertNull(ServiceBreakRules.problem(serviceInfo));
    }

    private static ServiceBreak breakBetween(LocalDate from, LocalDate to) {
        ServiceBreak serviceBreak = new ServiceBreak();
        serviceBreak.setFromDate(from);
        serviceBreak.setToDate(to);
        return serviceBreak;
    }
}
