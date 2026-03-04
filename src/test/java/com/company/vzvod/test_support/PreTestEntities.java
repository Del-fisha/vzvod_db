package com.company.vzvod.test_support;

import com.company.vzvod.entity.*;
import com.company.vzvod.service.DepartmentConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class PreTestEntities {

    public static User getNewUser() {
        User user = new User();

        user.setFirstName("Пётр");
        user.setLastName("Петров");
        user.setPatronymic("Петрович");
        user.setDateOfBirth(LocalDate.now().minusYears(30));
        user.setPassword("123");
        user.setUsername("123");
        user.setId(UUID.randomUUID());

        return user;
    }

    public static Contacts getNewContact() {
        Contacts contacts = new Contacts();

        contacts.setPhoneNumber("89112291515");
        contacts.setNearestMetroStation(MetroStation.BALTIYSKAYA);
        contacts.setId(UUID.randomUUID());

        return contacts;
    }

    public static Shift getNewShift() {
        Shift shift = new Shift();

        shift.setDepartmentToday(DepartmentConverter.departmentFromDate(LocalDate.now()));
        shift.setId(UUID.randomUUID());
        shift.setDate(LocalDate.now());
        shift.setStartTime(LocalTime.of(10,0));
        shift.setEndTime(LocalTime.of(22,0));
        shift.setIbdWithoutMigrant(45);
        shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shift.setCountOfClaims(1);
        shift.setCountOfStatements(2);
        shift.setIbdWithMigrant(60);
        shift.setNumber(NumberOfShift._28);

        return shift;
    }

    public static ServiceInfo getNewServiceInfo() {
        ServiceInfo serviceInfo = new ServiceInfo();

        serviceInfo.setStartOfPost(LocalDate.ofYearDay(2013, 56));
        serviceInfo.setQualificationClass(Qualification.THIRD);
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setRank(Rank.SENIOR_SERGEANT);
        serviceInfo.setMedicalExamination(false);
        serviceInfo.setBreastplate("00659874");
        serviceInfo.setId(UUID.randomUUID());
        serviceInfo.setPost(Post.COM_OTD);
        serviceInfo.setToken("65492");

        return serviceInfo;
    }
}
