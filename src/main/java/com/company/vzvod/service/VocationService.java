package com.company.vzvod.service;

import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;

import java.time.LocalDate;
import java.time.Period;

public class VocationService {

    private static final int DEFAULT_FAYS = 40;
    private static final int ADDITIONAL_DAYS_10_YEARS = 5;
    private static final int ADDITIONAL_DAYS_15_YEARS = 10;
    private static final int ADDITIONAL_DAYS_20_YEARS = 15;

    public static int daysAvailable(ServiceInfo serviceInfo, LocalDate date) {
        int result = DEFAULT_FAYS;

        User user = serviceInfo.getUser();
        int yearsOfService = Period.between(serviceInfo.getStartDate(), date)
                .getYears();
        if (user.getArmyService() == ArmyService.SERVED) {
            yearsOfService += 1;
        }

        if (yearsOfService >= 20) {
            result += ADDITIONAL_DAYS_20_YEARS;
        } else if (yearsOfService >= 15) {
            result += ADDITIONAL_DAYS_15_YEARS;
        } else if (yearsOfService >= 10) {
            result += ADDITIONAL_DAYS_10_YEARS;
        }
        return result;
    }
}
