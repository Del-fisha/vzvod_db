package com.company.vzvod.service.dto.raport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto {
    private String firstName;
    private String lastName;
    private String middleName;
    private String rank;
    private String position;
    private String gender;
}
