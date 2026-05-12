package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotColleagueItem;
import com.company.vzvod.bot.dto.BotColleaguesResponse;
import com.company.vzvod.bot.dto.BotShiftItem;
import com.company.vzvod.bot.dto.BotShiftUpsertRequest;
import com.company.vzvod.bot.dto.BotShiftsResponse;
import com.company.vzvod.bot.dto.BotVacationBalance;
import com.company.vzvod.bot.dto.BotVacationsResponse;
import com.company.vzvod.bot.dto.BotVocationItem;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserTelegramBinding;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.entity.VocationType;
import com.company.vzvod.service.DepartmentConverter;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class BotMeShiftsVocationsService {

    private static final Locale RU = Locale.forLanguageTag("ru");
    private static final int MAX_SHIFTS = 35;
    private static final int MAX_VOCATIONS = 30;
    private static final int COLLEAGUES_PAGE_SIZE = 8;
    private static final int COLLEAGUES_MAX_FETCH = 200;

    private final UnconstrainedDataManager unconstrainedDataManager;
    private final BotActiveUserChecker activeUserChecker;
    private final MessageSource messageSource;

    public BotMeShiftsVocationsService(
            UnconstrainedDataManager unconstrainedDataManager,
            BotActiveUserChecker activeUserChecker,
            MessageSource messageSource
    ) {
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.activeUserChecker = activeUserChecker;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public BotShiftsResponse loadShifts(long telegramChatId) {
        UUID serviceInfoId = loadServiceInfoId(telegramChatId);
        List<Shift> shifts = unconstrainedDataManager.load(Shift.class)
                .query("select distinct s from Shift s join s.units u where u.id = :sid order by s.date desc, s.startTime desc")
                .parameter("sid", serviceInfoId)
                .maxResults(MAX_SHIFTS)
                .fetchPlan(f -> f
                        .add("id")
                        .add("date")
                        .add("number")
                        .add("typeOfShift")
                        .add("departmentToday")
                        .add("startTime")
                        .add("endTime"))
                .list();
        List<BotShiftItem> items = new ArrayList<>(shifts.size());
        for (Shift s : shifts) {
            items.add(toShiftItem(s));
        }
        return new BotShiftsResponse(items);
    }

    @Transactional(readOnly = true)
    public BotColleaguesResponse loadColleagues(long telegramChatId, int department, int page) {
        if (department != 1 && department != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "department must be 1 or 2");
        }
        if (page < 0) {
            page = 0;
        }
        User me = loadUserWithServiceInfo(telegramChatId);
        UUID myId = me.getId();
        List<User> users = unconstrainedDataManager.load(User.class)
                .query("select u from User u join u.serviceInfo si join si.department d "
                        + "where d.number = :dn and u.id <> :uid and si.status = :st order by u.lastName, u.firstName")
                .parameter("dn", department)
                .parameter("uid", myId)
                .parameter("st", StatusInService.ACTIVE.getId())
                .maxResults(COLLEAGUES_MAX_FETCH)
                .list();
        int from = page * COLLEAGUES_PAGE_SIZE;
        if (from >= users.size()) {
            return new BotColleaguesResponse(List.of(), false);
        }
        int to = Math.min(from + COLLEAGUES_PAGE_SIZE, users.size());
        boolean hasMore = to < users.size();
        List<BotColleagueItem> items = new ArrayList<>(to - from);
        for (int i = from; i < to; i++) {
            User u = users.get(i);
            ServiceInfo si = u.getServiceInfo();
            if (si == null) {
                continue;
            }
            items.add(new BotColleagueItem(si.getId(), colleagueLabel(u)));
        }
        return new BotColleaguesResponse(items, hasMore);
    }

    @Transactional
    public BotShiftItem createShift(long telegramChatId, BotShiftUpsertRequest req) {
        validateCreate(req);
        User user = loadUserWithServiceInfo(telegramChatId);
        ServiceInfo si = user.getServiceInfo();
        if (si == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "service info missing");
        }
        Shift shift = unconstrainedDataManager.create(Shift.class);
        applyUpsert(shift, req);
        shift.setDepartmentToday(DepartmentConverter.departmentFromDate(req.date()));
        shift.getUnits().clear();
        shift.getUnits().add(si);
        ServiceInfo partner = loadAndValidatePartner(req.partnerServiceInfoId(), user.getId());
        shift.getUnits().add(partner);
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved);
    }

    @Transactional
    public BotShiftItem updateShift(long telegramChatId, UUID shiftId, BotShiftUpsertRequest req) {
        validateUpdate(req);
        ServiceInfo si = requireServiceInfo(telegramChatId);
        Shift shift = unconstrainedDataManager.load(Shift.class)
                .id(shiftId)
                .fetchPlan(f -> f
                        .add("units")
                        .add("id")
                        .add("date")
                        .add("number")
                        .add("typeOfShift")
                        .add("departmentToday")
                        .add("startTime")
                        .add("endTime")
                        .add("countOfStatements")
                        .add("countOfClaims")
                        .add("ibdWithMigrant")
                        .add("ibdWithoutMigrant"))
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "shift not found"));
        boolean member = shift.getUnits().stream().anyMatch(u -> u.getId().equals(si.getId()));
        if (!member) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "shift not accessible");
        }
        applyUpsert(shift, req);
        shift.setDepartmentToday(DepartmentConverter.departmentFromDate(req.date()));
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved);
    }

    private void validateCreate(BotShiftUpsertRequest req) {
        validateCommon(req);
        if (req.partnerServiceInfoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "partnerServiceInfoId required");
        }
    }

    private void validateUpdate(BotShiftUpsertRequest req) {
        validateCommon(req);
    }

    private void validateCommon(BotShiftUpsertRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body required");
        }
        if (req.date() == null || req.startTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date and startTime are required");
        }
        if (req.routeId() == null || req.routeId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "routeId required");
        }
        if (NumberOfShift.fromId(req.routeId().trim()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid routeId");
        }
        if (req.typeOfShiftId() == null || req.typeOfShiftId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "typeOfShiftId required");
        }
        if (TypeOfShift.fromId(req.typeOfShiftId().trim()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid typeOfShiftId");
        }
    }

    private ServiceInfo loadAndValidatePartner(UUID partnerServiceInfoId, UUID creatorUserId) {
        ServiceInfo partner = unconstrainedDataManager.load(ServiceInfo.class)
                .id(partnerServiceInfoId)
                .fetchPlan(fp -> fp
                        .add("user", u -> u.add("id"))
                        .add("department", d -> d.add("number"))
                        .add("status"))
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "partner not found"));
        if (partner.getUser() == null || partner.getUser().getId().equals(creatorUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid partner");
        }
        activeUserChecker.requireActive(partner.getUser().getId());
        if (partner.getStatus() != StatusInService.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "partner not active");
        }
        Department dep = partner.getDepartment();
        Integer num = dep == null ? null : dep.getNumber();
        if (num == null || (num != 1 && num != 2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "partner must be from department 1 or 2");
        }
        return partner;
    }

    private static String colleagueLabel(User u) {
        String fn = u.getFirstName();
        String ln = u.getLastName();
        if (ln == null || ln.isBlank()) {
            return fn == null ? "—" : fn;
        }
        String initial = ln.substring(0, 1).toUpperCase(Locale.ROOT) + ".";
        return (fn == null ? "" : fn + " ") + initial;
    }

    private static void applyUpsert(Shift shift, BotShiftUpsertRequest req) {
        shift.setDate(req.date());
        shift.setNumber(NumberOfShift.fromId(req.routeId().trim()));
        shift.setTypeOfShift(TypeOfShift.fromId(req.typeOfShiftId().trim()));
        shift.setStartTime(req.startTime());
        shift.setEndTime(req.endTime());
        shift.setCountOfStatements(req.countOfStatements() != null ? req.countOfStatements() : 0);
        shift.setCountOfClaims(req.countOfClaims() != null ? req.countOfClaims() : 0);
        shift.setIbdWithMigrant(req.ibdWithMigrant() != null ? req.ibdWithMigrant() : 0);
        shift.setIbdWithoutMigrant(req.ibdWithoutMigrant() != null ? req.ibdWithoutMigrant() : 0);
    }

    private User loadUserWithServiceInfo(long telegramChatId) {
        UserTelegramBinding binding = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.chatId = :cid")
                .parameter("cid", telegramChatId)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "telegram chat not bound"));

        UUID userId = binding.getUser().getId();
        activeUserChecker.requireActive(userId);

        return unconstrainedDataManager.load(User.class)
                .id(userId)
                .fetchPlan(fp -> fp.add("serviceInfo", sf -> sf.add("id")))
                .one();
    }

    private ServiceInfo requireServiceInfo(long telegramChatId) {
        User user = loadUserWithServiceInfo(telegramChatId);
        ServiceInfo si = user.getServiceInfo();
        if (si == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "service info missing");
        }
        return si;
    }

    private BotShiftItem toShiftItem(Shift s) {
        NumberOfShift route = s.getNumber();
        TypeOfShift type = s.getTypeOfShift();
        Dep dep = s.getDepartmentToday();
        return new BotShiftItem(
                s.getId(),
                s.getDate(),
                route == null ? null : route.getId(),
                type == null ? null : enumMessage("TypeOfShift", type.name()),
                dep == null ? null : enumMessage("Dep", dep.name()),
                s.getStartTime(),
                s.getEndTime()
        );
    }

    @Transactional(readOnly = true)
    public BotVacationsResponse loadVacations(long telegramChatId) {
        User user = loadUserWithVacationFields(telegramChatId);
        ServiceInfo si = user.getServiceInfo();
        if (si == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "service info missing");
        }
        int entitled = si.getVacationDaysEntitled() == null ? 0 : si.getVacationDaysEntitled();
        int available = si.getVacationDaysAvailable() == null ? 0 : si.getVacationDaysAvailable();
        int used = Math.max(0, entitled - available);
        BotVacationBalance balance = new BotVacationBalance(entitled, available, used);

        List<Vocation> vocations = unconstrainedDataManager.load(Vocation.class)
                .query("select v from Vocation v where v.userServiceInfo.id = :sid order by v.startDate desc")
                .parameter("sid", si.getId())
                .maxResults(MAX_VOCATIONS)
                .list();
        List<BotVocationItem> items = new ArrayList<>(vocations.size());
        for (Vocation v : vocations) {
            VocationType t = v.getType();
            String typeLabel = t == null ? null : enumMessage("VocationType", t.name());
            items.add(new BotVocationItem(
                    v.getStartDate(),
                    v.getEndDate(),
                    v.getCountOfDays(),
                    typeLabel
            ));
        }
        return new BotVacationsResponse(balance, items);
    }

    private UUID loadServiceInfoId(long telegramChatId) {
        ServiceInfo si = requireServiceInfo(telegramChatId);
        return si.getId();
    }

    private User loadUserWithVacationFields(long telegramChatId) {
        UserTelegramBinding binding = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.chatId = :cid")
                .parameter("cid", telegramChatId)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "telegram chat not bound"));

        UUID userId = binding.getUser().getId();
        activeUserChecker.requireActive(userId);

        return unconstrainedDataManager.load(User.class)
                .id(userId)
                .fetchPlan(fp -> fp.add("serviceInfo", sf -> sf
                        .add("vacationDaysEntitled")
                        .add("vacationDaysAvailable")))
                .one();
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
