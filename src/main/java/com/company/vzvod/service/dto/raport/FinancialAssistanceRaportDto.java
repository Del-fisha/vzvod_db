package com.company.vzvod.service.dto.raport;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FinancialAssistanceRaportDto {
    private List<PersonDto> employees;
    private PersonDto recipient;
    private String reportDate;
    private String year;
}
