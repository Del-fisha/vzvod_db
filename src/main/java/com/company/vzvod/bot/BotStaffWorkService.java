package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotStaffWorkResponse;
import com.company.vzvod.bot.dto.BotVocationItem;
import com.company.vzvod.bot.dto.BotWorkTotals;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.entity.VocationType;
import com.company.vzvod.service.VocationService;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class BotStaffWorkService {

    private static final Locale RU = Locale.forLanguageTag("ru");

    private final UnconstrainedDataManager dataManager;
    private final BotMeProfileService profileService;
    private final MessageSource messageSource;

    public BotStaffWorkService(
            UnconstrainedDataManager dataManager,
            BotMeProfileService profileService,
            MessageSource messageSource
    ) {
        this.dataManager = dataManager;
        this.profileService = profileService;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public BotStaffWorkResponse load(UUID viewerId, UUID targetUserId) {
        profileService.requireCommander(viewerId);
        ServiceInfo si = dataManager.load(ServiceInfo.class)
                .query("select s from ServiceInfo s where s.user.id = :uid")
                .parameter("uid", targetUserId)
                .fetchPlan(fp -> fp.add("vacationDaysEntitled").add("vacationDaysAvailable"))
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        LocalDate today = LocalDate.now(StaffWorkPeriods.MOSCOW);
        StaffWorkPeriods.Range month = StaffWorkPeriods.month(today);
        StaffWorkPeriods.Range year = StaffWorkPeriods.year(today);
        int entitled = si.getVacationDaysEntitled() == null ? 0 : si.getVacationDaysEntitled();
        int remaining = si.getVacationDaysAvailable() == null ? 0 : si.getVacationDaysAvailable();
        return new BotStaffWorkResponse(
                today.getYear(),
                totals(si.getId(), month.from(), month.to()),
                totals(si.getId(), year.from(), year.to()),
                totals(si.getId(), null, null),
                entitled,
                remaining,
                vacationsThisYear(si.getId(), year)
        );
    }

    private BotWorkTotals totals(UUID serviceInfoId, LocalDate from, LocalDate to) {
        return new BotWorkTotals(
                sumIbdr(serviceInfoId, from, to),
                countViolations("AdministrativeViolation", serviceInfoId, from, to),
                countViolations("CriminalViolation", serviceInfoId, from, to)
        );
    }

    private int sumIbdr(UUID serviceInfoId, LocalDate from, LocalDate to) {
        String jpql = """
                select coalesce(sum(coalesce(s.ibdr, 0)), 0)
                from Shift s join s.units u
                where u.id = :sid
                """;
        return number(jpql, serviceInfoId, from, to);
    }

    private int countViolations(String entity, UUID serviceInfoId, LocalDate from, LocalDate to) {
        String jpql = """
                select count(v)
                from %s v join v.shift s join s.units u
                where u.id = :sid
                """.formatted(entity);
        return number(jpql, serviceInfoId, from, to);
    }

    private int number(String jpql, UUID serviceInfoId, LocalDate from, LocalDate to) {
        String sql = from == null ? jpql : jpql + " and s.date >= :from and s.date <= :to";
        var loader = dataManager.loadValue(sql, Long.class).parameter("sid", serviceInfoId);
        if (from != null) {
            loader.parameter("from", from).parameter("to", to);
        }
        Long value = loader.one();
        return value == null ? 0 : value.intValue();
    }

    private List<BotVocationItem> vacationsThisYear(UUID serviceInfoId, StaffWorkPeriods.Range year) {
        List<Vocation> vocations = dataManager.load(Vocation.class)
                .query("""
                        select v from Vocation v
                        where v.userServiceInfo.id = :sid
                          and v.startDate <= :yearEnd
                          and v.endDate >= :yearStart
                        order by v.startDate desc
                        """)
                .parameter("sid", serviceInfoId)
                .parameter("yearStart", year.from())
                .parameter("yearEnd", year.to())
                .list();
        List<BotVocationItem> items = new ArrayList<>(vocations.size());
        for (Vocation v : vocations) {
            VocationType type = v.getType();
            items.add(new BotVocationItem(
                    v.getId(),
                    v.getStartDate(),
                    VocationService.absenceEnd(v.getEndDate(), v.getDaysAddedByDeparture()),
                    v.getCountOfDays(),
                    v.getTypeId(),
                    type == null ? null : enumMessage("VocationType", type.name()),
                    v.isHasDeparture(),
                    v.getCityToDrive(),
                    v.getDaysAddedByDeparture()
            ));
        }
        return items;
    }

    private String enumMessage(String enumSimpleName, String enumConstantName) {
        String code = "com.company.vzvod.entity/" + enumSimpleName + "." + enumConstantName;
        try {
            return messageSource.getMessage(code, null, RU);
        } catch (NoSuchMessageException e) {
            return enumConstantName;
        }
    }
}
