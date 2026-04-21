package com.company.vzvod.integration;

import com.company.vzvod.entity.Event;
import com.company.vzvod.entity.EventType;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест Event (CRUD)")
public class EventIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private Event event;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        event = dataManager.create(Event.class);
        event.setName("Тест событие");
        event.setPlace("Стадион");
        event.setDate(LocalDate.now().plusDays(1));
        event.setTime(LocalTime.of(19, 30));
        event.setShiftOfDepartment(1);
        event.setEventType(EventType.OTHER);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Сохранение и чтение из БД")
    void saveAndLoad() {
        Event saved = dataManager.save(event);
        UUID id = saved.getId();
        assertNotNull(id);

        Event loaded = dataManager.load(Event.class).id(id).one();
        assertEquals(saved.getName(), loaded.getName());
        assertEquals(saved.getPlace(), loaded.getPlace());
        assertEquals(saved.getDate(), loaded.getDate());
        assertEquals(saved.getTime(), loaded.getTime());
        assertEquals(saved.getShiftOfDepartment(), loaded.getShiftOfDepartment());
        assertEquals(saved.getEventType(), loaded.getEventType());
    }
}

