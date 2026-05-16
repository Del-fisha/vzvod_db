package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotEventsResponse;
import com.company.vzvod.entity.Event;
import com.company.vzvod.entity.UserTelegramBinding;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
        long chatId = 42L;
        UUID userId = UUID.randomUUID();
        stubActiveBinding(chatId, userId);

        Event later = event(LocalDate.of(2026, 6, 1), LocalTime.of(12, 0), "Позже");
        Event earlier = event(LocalDate.of(2026, 5, 20), LocalTime.of(9, 0), "  ");

        stubEventLoad(new ArrayList<>(List.of(later, earlier)));

        BotEventsResponse response = service.loadUpcomingEvents(chatId);

        assertEquals(2, response.items().size());
        assertEquals("—", response.items().get(0).name());
        assertEquals(LocalDate.of(2026, 5, 20), response.items().get(0).date());
        assertEquals("Позже", response.items().get(1).name());
        verify(activeUserChecker).requireActive(userId);
    }

    @Test
    @DisplayName("404 если telegram chat не привязан")
    void loadUpcomingEvents_notFoundWhenUnbound() {
        stubBindingLoad(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.loadUpcomingEvents(99L)
        );
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("пробрасывает запрет для неактивного пользователя")
    void loadUpcomingEvents_propagatesInactiveUser() {
        long chatId = 7L;
        UUID userId = UUID.randomUUID();
        UserTelegramBinding binding = mock(UserTelegramBinding.class);
        com.company.vzvod.entity.User user = mock(com.company.vzvod.entity.User.class);
        when(binding.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        stubBindingLoad(Optional.of(binding));
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "user inactive"))
                .when(activeUserChecker).requireActive(userId);

        assertThrows(ResponseStatusException.class, () -> service.loadUpcomingEvents(chatId));
    }

    private void stubActiveBinding(long chatId, UUID userId) {
        UserTelegramBinding binding = mock(UserTelegramBinding.class);
        com.company.vzvod.entity.User user = mock(com.company.vzvod.entity.User.class);
        when(binding.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        stubBindingLoad(Optional.of(binding));
        doNothing().when(activeUserChecker).requireActive(userId);
    }

    @SuppressWarnings("unchecked")
    private void stubBindingLoad(Optional<UserTelegramBinding> binding) {
        FluentLoader<UserTelegramBinding> loader = mock(FluentLoader.class);
        FluentLoader.ByQuery<UserTelegramBinding> query = mock(FluentLoader.ByQuery.class);
        when(unconstrainedDataManager.load(UserTelegramBinding.class)).thenReturn(loader);
        when(loader.query(any())).thenReturn(query);
        when(query.parameter(eq("cid"), anyLong())).thenReturn(query);
        when(query.optional()).thenReturn(binding);
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
