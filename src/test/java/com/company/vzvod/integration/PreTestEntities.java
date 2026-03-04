package com.company.vzvod.integration;

import com.company.vzvod.entity.User;

import java.time.LocalDate;
import java.util.UUID;

public class PreTestEntities {

    static User getNewUser() {
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
}
