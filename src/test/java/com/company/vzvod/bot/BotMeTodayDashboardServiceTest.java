package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotTodayDashboardResponse;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.service.DepartmentConverter;
import com.company.vzvod.service.TodayShiftDashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotMeTodayDashboardServiceTest {

    @Mock BotActiveUserChecker activeUserChecker;
    @Mock TodayShiftDashboardService todayShiftDashboardService;
    @InjectMocks BotMeTodayDashboardService service;

    @Test
    void loadDayDashboard_usesDepartmentConverterAndExistingLoader() {
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 8, 10);
        Dep dep = DepartmentConverter.departmentFromDate(date);
        BotTodayDashboardResponse expected = new BotTodayDashboardResponse(
                date, dep.getId(), List.of(), null
        );
        when(todayShiftDashboardService.loadBotDashboard(date, dep)).thenReturn(expected);

        BotTodayDashboardResponse actual = service.loadDayDashboard(userId, date);

        verify(activeUserChecker).requireActive(userId);
        verify(todayShiftDashboardService).loadBotDashboard(date, dep);
        assertSame(expected, actual);
    }
}
