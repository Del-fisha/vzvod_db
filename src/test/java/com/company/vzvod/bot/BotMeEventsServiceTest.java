package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotEventCreateRequest;
import com.company.vzvod.bot.dto.BotEventItem;
import com.company.vzvod.bot.dto.BotEventsResponse;
import com.company.vzvod.entity.Event;
import com.company.vzvod.entity.EventType;
import com.company.vzvod.service.DepartmentConverter;
import io.jmix.core.FluentLoader;
import io.jmix.core.UnconstrainedDataManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BotMeEventsService")
class BotMeEventsServiceTest {

    @Mock
    private UnconstrainedDataManager unconstrainedDataManager;

    @Mock
    private BotActiveUserChecker activeUserChecker;

    @InjectMocks
    private BotMeEventsService service;

    @Test
    @DisplayName("сортирует события по дате и времени, подставляет «—» для пустого имени")
    void loadUpcomingEvents_sortsAndNormalizesBlankName() {
        UUID userId = UUID.randomUUID();
        doNothing().when(activeUserChecker).requireActive(userId);

        Event later = event(LocalDate.of(2026, 6, 1), LocalTime.of(12, 0), "Позже");
        Event earlier = event(LocalDate.of(2026, 5, 20), LocalTime.of(9, 0), "  ");

        stubEventLoad(new ArrayList<>(List.of(later, earlier)));

        BotEventsResponse response = service.loadUpcomingEvents(userId);

        assertEquals(2, response.items().size());
        assertEquals("—", response.items().get(0).name());
        assertEquals(LocalDate.of(2026, 5, 20), response.items().get(0).date());
        assertEquals("Позже", response.items().get(1).name());
        verify(activeUserChecker).requireActive(userId);
    }

    @Test
    @DisplayName("заполняет place, shiftOfDepartment и eventType при загрузке")
    void loadUpcomingEvents_fillsPlaceAndShiftOfDepartmentAndEventType() {
        UUID userId = UUID.randomUUID();
        doNothing().when(activeUserChecker).requireActive(userId);

        Event event = event(LocalDate.of(2026, 6, 1), LocalTime.of(12, 0), "Матч");
        event.setPlace("СКК");
        event.setShiftOfDepartment(2);
        event.setEventType(EventType.SPORT);

        stubEventLoad(new ArrayList<>(List.of(event)));

        BotEventsResponse response = service.loadUpcomingEvents(userId);

        assertEquals(1, response.items().size());
        BotEventItem item = response.items().get(0);
        assertEquals("СКК", item.place());
        assertEquals(2, item.shiftOfDepartment());
        assertEquals(EventType.SPORT.getId(), item.eventType());
        assertEquals(LocalTime.of(12, 0), item.time());
    }

    @Test
    @DisplayName("пробрасывает запрет для неактивного пользователя")
    void loadUpcomingEvents_propagatesInactiveUser() {
        UUID userId = UUID.randomUUID();
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "user inactive"))
                .when(activeUserChecker).requireActive(userId);

        assertThrows(ResponseStatusException.class, () -> service.loadUpcomingEvents(userId));
    }

    @Test
    @DisplayName("createEvent: спортивное название переводит тип в SPORT и заполняет shiftOfDepartment по дате")
    void createEvent_sportName_setsSportTypeAndShiftOfDepartment() {
        UUID userId = UUID.randomUUID();
        doNothing().when(activeUserChecker).requireActive(userId);
        Event created = new Event();
        when(unconstrainedDataManager.create(Event.class)).thenReturn(created);
        when(unconstrainedDataManager.save(created)).thenReturn(created);

        LocalDate date = LocalDate.of(2026, 6, 5);
        BotEventCreateRequest req = new BotEventCreateRequest(
                "Матч Зенит", date, LocalTime.of(19, 0), "Газпром Арена", "описание", null);

        BotEventItem item = service.createEvent(userId, req);

        assertEquals(EventType.SPORT.getId(), item.eventType());
        assertEquals(DepartmentConverter.departmentFromDateToInt(date), item.shiftOfDepartment());
        assertEquals("Матч Зенит", item.name());
        assertEquals("Газпром Арена", item.place());
        verify(activeUserChecker).requireActive(userId);
    }

    @Test
    @DisplayName("createEvent: по умолчанию тип OTHER, если название не спортивное и тип не передан")
    void createEvent_defaultsToOtherType() {
        UUID userId = UUID.randomUUID();
        doNothing().when(activeUserChecker).requireActive(userId);
        Event created = new Event();
        when(unconstrainedDataManager.create(Event.class)).thenReturn(created);
        when(unconstrainedDataManager.save(created)).thenReturn(created);

        LocalDate date = LocalDate.of(2026, 6, 6);
        BotEventCreateRequest req = new BotEventCreateRequest(
                "Собрание", date, null, null, null, null);

        BotEventItem item = service.createEvent(userId, req);

        assertEquals(EventType.OTHER.getId(), item.eventType());
        assertEquals(DepartmentConverter.departmentFromDateToInt(date), item.shiftOfDepartment());
    }

    @Test
    @DisplayName("createEvent: явно переданный eventType используется как есть")
    void createEvent_explicitEventTypeIsUsed() {
        UUID userId = UUID.randomUUID();
        doNothing().when(activeUserChecker).requireActive(userId);
        Event created = new Event();
        when(unconstrainedDataManager.create(Event.class)).thenReturn(created);
        when(unconstrainedDataManager.save(created)).thenReturn(created);

        LocalDate date = LocalDate.of(2026, 6, 7);
        BotEventCreateRequest req = new BotEventCreateRequest(
                "Концерт", date, null, null, null, EventType.CONCERT.getId());

        BotEventItem item = service.createEvent(userId, req);

        assertEquals(EventType.CONCERT.getId(), item.eventType());
    }

    @Test
    @DisplayName("createEvent: 400 без имени или даты")
    void createEvent_missingNameOrDate_returns400() {
        UUID userId = UUID.randomUUID();

        assertThrows(ResponseStatusException.class,
                () -> service.createEvent(userId, new BotEventCreateRequest(null, LocalDate.now(), null, null, null, null)));
        assertThrows(ResponseStatusException.class,
                () -> service.createEvent(userId, new BotEventCreateRequest("Имя", null, null, null, null, null)));
    }

    @SuppressWarnings("unchecked")
    private void stubEventLoad(List<Event> events) {
        FluentLoader<Event> loader = mock(FluentLoader.class);
        FluentLoader.ByQuery<Event> query = mock(FluentLoader.ByQuery.class);
        when(unconstrainedDataManager.load(Event.class)).thenReturn(loader);
        when(loader.query(any())).thenReturn(query);
        when(query.parameter(eq("today"), any(LocalDate.class))).thenReturn(query);
        when(query.maxResults(200)).thenReturn(query);
        when(query.list()).thenReturn(events);
    }

    private static Event event(LocalDate date, LocalTime time, String name) {
        Event event = new Event();
        event.setDate(date);
        event.setTime(time);
        event.setName(name);
        return event;
    }
}
