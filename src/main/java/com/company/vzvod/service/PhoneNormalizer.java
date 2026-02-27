package com.company.vzvod.service;

public class PhoneNormalizer {

    public String normalize(String phone) {
        if (phone.length() == 11) {
            phone = phone.substring(1, 11);
        }
        return "+7".concat(phone);
    }
}
