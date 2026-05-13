package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotShiftItem(
        UUID id,
        LocalDate date,
        /** Идентификатор маршрута {@link com.company.vzvod.entity.NumberOfShift#getId()} (как в UI). */
        String route,
        String shiftType,
        /** Идентификатор типа {@link com.company.vzvod.entity.TypeOfShift#getId()} для PUT. */
        String typeOfShiftId,
        /** Отделение на смену (как в карточке). */
        String departmentToday,
        LocalTime startTime,
        LocalTime endTime,
        Integer countOfStatements,
        Integer countOfClaims,
        Integer ibdWithMigrant,
        Integer ibdWithoutMigrant,
        /** Первый напарник (совместимость со старыми клиентами); null, если кроме вас никого нет. */
        UUID partnerServiceInfoId,
        /** Все участники смены кроме текущего пользователя (id {@code ServiceInfo}), по возрастанию id. */
        List<UUID> otherParticipantServiceInfoIds
) {
}
