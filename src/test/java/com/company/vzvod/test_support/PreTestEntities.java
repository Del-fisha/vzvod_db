package com.company.vzvod.test_support;

import com.company.vzvod.entity.*;
import com.company.vzvod.service.DepartmentConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;

public class PreTestEntities {

    public static void updateUser(User user) {

        user.setDateOfBirth(LocalDate.now().minusYears(30));
        user.setPatronymic("Петрович");
        user.setLastName("Петров");
        user.setFirstName("Пётр");
        user.setPassword("123");
        user.setUsername("123");
    }

    public static void updateContact(Contacts contact) {

        contact.setNearestMetroStation(MetroStation.BALTIYSKAYA);
        contact.setPhoneNumber("89112291515");
    }

    public static void updateShift(Shift shift) {

        shift.setDepartmentToday(DepartmentConverter.departmentFromDate(LocalDate.now()));
        shift.setStartTime(LocalTime.of(10,0));
        shift.setEndTime(LocalTime.of(22,0));
        shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shift.setNumber(NumberOfShift._28);
        shift.setDate(LocalDate.now());
        shift.setIbdWithoutMigrant(45);
        shift.setCountOfStatements(2);
        shift.setIbdWithMigrant(60);
        shift.setCountOfClaims(1);
    }

    public static void updateServiceInfo(ServiceInfo serviceInfo) {

        serviceInfo.setStartOfPost(LocalDate.ofYearDay(2013, 56));
        serviceInfo.setQualificationClass(Qualification.THIRD);
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setVocations(new ArrayList<>());
        serviceInfo.setIncentive(new ArrayList<>());
        serviceInfo.setPenalty(new ArrayList<>());
        serviceInfo.setRank(Rank.SENIOR_SERGEANT);
        serviceInfo.setMedicalExamination(false);
        serviceInfo.setBreastplate("00659874");
        serviceInfo.setShifts(new HashSet<>());
        serviceInfo.setPost(Post.COM_OTD);
        serviceInfo.setToken("65492");
    }

    public static void updateIncentive(Incentive incentive) {

        incentive.setIncentiveType(IncentiveType.BONUS);
        incentive.setOrderNumber("9874561316546");
        incentive.setInitiator(Initiator.METRO);
        incentive.setDate(LocalDate.now());
        incentive.setDescription("To all");
    }
}
