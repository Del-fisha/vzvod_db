package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotCatalogOptionsResponse;
import com.company.vzvod.bot.dto.BotEnumOption;
import com.company.vzvod.bot.dto.BotShiftRouteOption;
import com.company.vzvod.bot.dto.BotStringEnumOption;
import com.company.vzvod.entity.MetroStation;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.VocationType;
import io.jmix.core.UnconstrainedDataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BotMeShiftsVocationsService catalog-options")
class BotMeShiftsVocationsServiceCatalogTest {

    @Mock
    private UnconstrainedDataManager unconstrainedDataManager;

    @Mock
    private BotActiveUserChecker activeUserChecker;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private BotMeShiftsVocationsService service;

    @BeforeEach
    void stubMessages() {
        when(messageSource.getMessage(anyString(), isNull(), any(Locale.class)))
                .thenAnswer(invocation -> {
                    String code = invocation.getArgument(0);
                    int dot = code.lastIndexOf('.');
                    return dot >= 0 ? code.substring(dot + 1) : code;
                });
    }

    @Test
    @DisplayName("возвращает все типы отпусков, включая Учебный и Цпп")
    void loadCatalogOptions_includesAllVocationTypes() {
        BotCatalogOptionsResponse response = service.loadCatalogOptions();

        assertEquals(VocationType.values().length, response.vocationTypes().size());
        Set<Integer> ids = response.vocationTypes().stream()
                .map(BotEnumOption::id)
                .collect(Collectors.toSet());
        assertTrue(ids.contains(VocationType.MAIN.getId()));
        assertTrue(ids.contains(VocationType.ADDITIONAL.getId()));
        assertTrue(ids.contains(VocationType.PART_OF_MAIN.getId()));
        assertTrue(ids.contains(VocationType.STUDY_LEAVE.getId()));
        assertTrue(ids.contains(VocationType.PTC.getId()));
    }

    @Test
    @DisplayName("возвращает все маршруты и типы смен с defaultTypeOfShiftId")
    void loadCatalogOptions_includesAllRoutesAndTypesWithDefaults() {
        BotCatalogOptionsResponse response = service.loadCatalogOptions();

        assertEquals(NumberOfShift.values().length, response.shiftRoutes().size());
        assertEquals(TypeOfShift.values().length, response.shiftTypes().size());

        Set<String> routeIds = response.shiftRoutes().stream()
                .map(BotShiftRouteOption::id)
                .collect(Collectors.toSet());
        for (NumberOfShift route : NumberOfShift.values()) {
            assertTrue(routeIds.contains(route.getId()), "missing route " + route.name());
        }

        Set<String> typeIds = response.shiftTypes().stream()
                .map(BotStringEnumOption::id)
                .collect(Collectors.toSet());
        for (TypeOfShift type : TypeOfShift.values()) {
            assertTrue(typeIds.contains(type.getId()), "missing type " + type.name());
        }

        BotShiftRouteOption mp28 = response.shiftRoutes().stream()
                .filter(r -> NumberOfShift._28.getId().equals(r.id()))
                .findFirst()
                .orElseThrow();
        assertEquals(TypeOfShift.VZVOD_ROUTE.getId(), mp28.defaultTypeOfShiftId());

        BotShiftRouteOption another = response.shiftRoutes().stream()
                .filter(r -> NumberOfShift.ANOTHER.getId().equals(r.id()))
                .findFirst()
                .orElseThrow();
        assertEquals(TypeOfShift.CHECKING.getId(), another.defaultTypeOfShiftId());
    }

    @Test
    @DisplayName("возвращает все станции метро с подписями")
    void loadCatalogOptions_includesAllMetroStations() {
        BotCatalogOptionsResponse response = service.loadCatalogOptions();

        assertEquals(MetroStation.values().length, response.metroStations().size());
        Set<Integer> ids = response.metroStations().stream()
                .map(BotEnumOption::id)
                .collect(Collectors.toSet());
        for (MetroStation station : MetroStation.values()) {
            assertTrue(ids.contains(station.getId()), "missing metro station " + station.name());
        }

        BotEnumOption baltiyskaya = response.metroStations().stream()
                .filter(m -> MetroStation.BALTIYSKAYA.getId().equals(m.id()))
                .findFirst()
                .orElseThrow();
        assertEquals("BALTIYSKAYA", baltiyskaya.label());
    }
}
