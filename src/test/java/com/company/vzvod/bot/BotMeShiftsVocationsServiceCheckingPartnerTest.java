package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotShiftUpsertRequest;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.TypeOfShift;
import io.jmix.core.UnconstrainedDataManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName("BotMeShiftsVocationsService: напарник для CHECKING")
class BotMeShiftsVocationsServiceCheckingPartnerTest {

    @Mock
    private UnconstrainedDataManager unconstrainedDataManager;

    @Mock
    private BotActiveUserChecker activeUserChecker;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private BotMeShiftsVocationsService service;

    @Test
    @DisplayName("обычная смена без напарника — ошибка")
    void validateCreate_requiresPartnerForRegularShift() {
        BotShiftUpsertRequest req = upsert(
                NumberOfShift._28.getId(),
                TypeOfShift.VZVOD_ROUTE.getId(),
                null,
                null
        );
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.validateCreate(req));
        assertEquals("partnerServiceInfoId required", ex.getReason());
    }

    @Test
    @DisplayName("CHECKING без напарника — допустимо")
    void validateCreate_allowsMissingPartnerForChecking() {
        BotShiftUpsertRequest req = upsert(
                NumberOfShift.ANOTHER.getId(),
                TypeOfShift.CHECKING.getId(),
                null,
                null
        );
        assertDoesNotThrow(() -> service.validateCreate(req));
    }

    @Test
    @DisplayName("CHECKING с напарником — допустимо")
    void validateCreate_allowsPartnerForChecking() {
        UUID partner = UUID.randomUUID();
        BotShiftUpsertRequest req = upsert(
                NumberOfShift.ANOTHER.getId(),
                TypeOfShift.CHECKING.getId(),
                partner,
                List.of(partner)
        );
        assertDoesNotThrow(() -> service.validateCreate(req));
    }

    private static BotShiftUpsertRequest upsert(
            String routeId,
            String typeId,
            UUID partnerId,
            List<UUID> partnerIds
    ) {
        return new BotShiftUpsertRequest(
                LocalDate.of(2026, 8, 10),
                routeId,
                typeId,
                LocalTime.of(9, 0),
                null,
                partnerId,
                0,
                0,
                0,
                0,
                partnerIds
        );
    }
}
