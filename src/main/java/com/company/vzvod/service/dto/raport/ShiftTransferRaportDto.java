package com.company.vzvod.service.dto.raport;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShiftTransferRaportDto {
    private PersonDto employee;
    private PersonDto recipient;
    private PersonDto petitioner;

    private String reportDate;
    private String oldTimeDate;
    private String newTimeDate;
}

