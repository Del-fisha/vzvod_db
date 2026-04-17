package com.company.vzvod.service.dto.raport;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompensatoryTimeRaportDto {
    private PersonDto employee;
    private PersonDto recipient;
    private PersonDto interceder;
    private String reportDate;
    private String dayOffDate;
}
