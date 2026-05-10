package com.company.vzvod.service.dto.raport;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceBookSupplementDto {
    /**
     * "Кому" (сотрудник, для которого формируется дополнение к служебной книжке).
     */
    private PersonDto employee;

    /**
     * "Кто подписывает" (подписант).
     */
    private PersonDto petitioner;

    /**
     * "Дата подписания" (дата отчёта/подписания).
     * Формат: dd.MM.yyyy (как и в других рапортах core → raport-service).
     */
    private String reportDate;
}

