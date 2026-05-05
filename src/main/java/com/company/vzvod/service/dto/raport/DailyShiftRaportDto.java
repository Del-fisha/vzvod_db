package com.company.vzvod.service.dto.raport;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyShiftRaportDto {
    private PersonDto employee;
    private PersonDto recipient;   // commander (кому рапорт)
    private PersonDto petitioner;  // от кого рапорт

    private String reportDate;
    private String firstTimeDate;
    private String secondTimeDate;
    private String newTimeDate;
}

