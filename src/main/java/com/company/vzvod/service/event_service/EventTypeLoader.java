package com.company.vzvod.service.event_service;

import java.util.Arrays;

public class EventTypeLoader {
    public static boolean isSport(String nameOfEvent) {
        String[] sportWords = {"Зенит", "Ска", "Драконы", "Dragons", "Россия"};

        return Arrays.stream(sportWords)
                .anyMatch(word -> nameOfEvent.toLowerCase().contains(word.toLowerCase()));
    }
}
