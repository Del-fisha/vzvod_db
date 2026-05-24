package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotAdministrativeViolationCreateRequest;
import com.company.vzvod.bot.dto.BotColleagueItem;
import com.company.vzvod.bot.dto.BotColleaguesResponse;
import com.company.vzvod.bot.dto.BotCriminalViolationCreateRequest;
import com.company.vzvod.bot.dto.BotEnumOption;
import com.company.vzvod.bot.dto.BotShiftEndTimeRequest;
import com.company.vzvod.bot.dto.BotShiftItem;
import com.company.vzvod.bot.dto.BotShiftMetricDeltaRequest;
import com.company.vzvod.bot.dto.BotShiftUpsertRequest;
import com.company.vzvod.bot.dto.BotShiftsResponse;
import com.company.vzvod.bot.dto.BotVacationBalance;
import com.company.vzvod.bot.dto.BotVacationsResponse;
import com.company.vzvod.bot.dto.BotViolationOptionsResponse;
import com.company.vzvod.bot.dto.BotVocationItem;
import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.Impact;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfCriminal;
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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
                        .add("endTime")
                        .add("countOfStatements")
                        .add("countOfClaims")
                        .add("ibdWithMigrant")
                        .add("ibdWithoutMigrant")
                        .add("units"))
                .list();
        List<BotShiftItem> items = new ArrayList<>(shifts.size());
        for (Shift s : shifts) {
            items.add(toShiftItem(s, serviceInfoId));
        }
        return new BotShiftsResponse(items);
    }

    @Transactional(readOnly = true)
    public BotShiftItem loadShift(long telegramChatId, UUID shiftId) {
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
        return toShiftItem(shift, si.getId());
    }

    @Transactional(readOnly = true)
    public BotColleaguesResponse loadColleagues(long telegramChatId, int department, int page, UUID excludeShiftId) {
        if (department != 1 && department != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "department must be 1 or 2");
        }
        if (page < 0) {
            page = 0;
        }
        User me = loadUserWithServiceInfo(telegramChatId);
        UUID myId = me.getId();
        Set<UUID> busyInOpenShifts = openShiftParticipantIdsExcept(excludeShiftId);
        List<User> users = unconstrainedDataManager.load(User.class)
                .query("select u from User u join u.serviceInfo si join si.department d "
                        + "where d.number = :dn and u.id <> :uid and si.status = :st "
                        + "order by si.post, si.rank desc, u.lastName")
                .parameter("dn", department)
                .parameter("uid", myId)
                .parameter("st", StatusInService.ACTIVE.getId())
                .maxResults(COLLEAGUES_MAX_FETCH)
                .list();
        List<User> available = new ArrayList<>(users.size());
        for (User u : users) {
            ServiceInfo si = u.getServiceInfo();
            if (si == null || busyInOpenShifts.contains(si.getId())) {
                continue;
            }
            available.add(u);
        }
        int from = page * COLLEAGUES_PAGE_SIZE;
        if (from >= available.size()) {
            return new BotColleaguesResponse(List.of(), false);
        }
        int to = Math.min(from + COLLEAGUES_PAGE_SIZE, available.size());
        boolean hasMore = to < available.size();
        List<BotColleagueItem> items = new ArrayList<>(to - from);
        for (int i = from; i < to; i++) {
            User u = available.get(i);
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
        assertNotInOtherOpenShift(si.getId(), null);
        Shift shift = unconstrainedDataManager.create(Shift.class);
        applyCreate(shift, req);
        shift.setDepartmentToday(DepartmentConverter.departmentFromDate(req.date()));
        shift.getUnits().clear();
        shift.getUnits().add(si);
        ServiceInfo partner = loadAndValidatePartner(req.partnerServiceInfoId(), user.getId());
        assertNotInOtherOpenShift(partner.getId(), null);
        shift.getUnits().add(partner);
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved, si.getId());
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
        UUID mySid = si.getId();
        boolean member = shift.getUnits().stream()
                .anyMatch(u -> u != null && mySid.equals(u.getId()));
        if (!member) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "shift not accessible");
        }
        applyUpdate(shift, req);
        shift.setDepartmentToday(DepartmentConverter.departmentFromDate(req.date()));
        if (req.partnerServiceInfoId() != null) {
            User user = loadUserWithServiceInfo(telegramChatId);
            ServiceInfo partner = loadAndValidatePartner(req.partnerServiceInfoId(), user.getId());
            UUID partnerSid = partner.getId();
            boolean already = shift.getUnits().stream()
                    .anyMatch(u -> u != null && partnerSid.equals(u.getId()));
            if (already) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user already in shift");
            }
            assertNotInOtherOpenShift(partnerSid, shiftId);
            shift.getUnits().add(partner);
        }
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved, si.getId());
    }

    @Transactional
    public BotShiftItem setShiftEndTime(long telegramChatId, UUID shiftId, BotShiftEndTimeRequest body) {
        if (body == null || body.endTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime required");
        }
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
                        .add("endTime"))
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "shift not found"));
        boolean member = shift.getUnits().stream().anyMatch(u -> u.getId().equals(si.getId()));
        if (!member) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "shift not accessible");
        }
        if (shift.getEndTime() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "end time already set");
        }
        LocalTime start = shift.getStartTime();
        if (start == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shift has no start time");
        }
        if (!body.endTime().isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }
        shift.setEndTime(body.endTime());
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved, si.getId());
    }

    @Transactional
    public BotShiftItem adjustIbdWithMigrant(long telegramChatId, UUID shiftId, BotShiftMetricDeltaRequest body) {
        int delta = requireMetricDelta(body);
        ServiceInfo si = requireServiceInfo(telegramChatId);
        Shift shift = loadOpenShiftForParticipant(shiftId, si.getId());
        int current = shift.getIbdWithMigrant() == null ? 0 : shift.getIbdWithMigrant();
        if (delta > 0) {
            shift.setIbdWithMigrant(current + delta);
        } else {
            if (current + delta < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ibdWithMigrant cannot be negative");
            }
            shift.setIbdWithMigrant(current + delta);
        }
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved, si.getId());
    }

    @Transactional
    public BotShiftItem adjustCountOfStatements(long telegramChatId, UUID shiftId, BotShiftMetricDeltaRequest body) {
        int delta = requireMetricDelta(body);
        ServiceInfo si = requireServiceInfo(telegramChatId);
        Shift shift = loadOpenShiftForParticipant(shiftId, si.getId());
        int current = shift.getCountOfStatements() == null ? 0 : shift.getCountOfStatements();
        if (delta > 0) {
            shift.setCountOfStatements(current + delta);
        } else {
            if (current + delta < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "countOfStatements cannot be negative");
            }
            shift.setCountOfStatements(current + delta);
        }
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved, si.getId());
    }

    @Transactional(readOnly = true)
    public BotViolationOptionsResponse loadViolationOptions() {
        List<BotEnumOption> impacts = new ArrayList<>(Impact.values().length);
        for (Impact impact : Impact.values()) {
            impacts.add(new BotEnumOption(impact.getId(), enumMessage("Impact", impact.name())));
        }
        List<BotEnumOption> articles = new ArrayList<>(ArticleOfAdministrative.values().length);
        for (ArticleOfAdministrative article : ArticleOfAdministrative.values()) {
            articles.add(new BotEnumOption(article.getId(), enumMessage("ArticleOfAdministrative", article.name())));
        }
        List<BotEnumOption> types = new ArrayList<>(TypeOfCriminal.values().length);
        for (TypeOfCriminal type : TypeOfCriminal.values()) {
            types.add(new BotEnumOption(type.getId(), enumMessage("TypeOfCriminal", type.name())));
        }
        return new BotViolationOptionsResponse(List.copyOf(impacts), List.copyOf(articles), List.copyOf(types));
    }

    @Transactional
    public BotShiftItem createAdministrativeViolation(long telegramChatId, UUID shiftId,
                                                      BotAdministrativeViolationCreateRequest body) {
        if (body == null || body.impactId() == null || body.articleId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "impactId and articleId required");
        }
        Impact impact = requireImpact(body.impactId());
        ArticleOfAdministrative article = requireAdministrativeArticle(body.articleId());
        ServiceInfo si = requireServiceInfo(telegramChatId);
        Shift shift = loadOpenShiftForParticipant(shiftId, si.getId());
        AdministrativeViolation violation = unconstrainedDataManager.create(AdministrativeViolation.class);
        violation.setShift(shift);
        violation.setImpact(impact);
        violation.setArticle(article);
        unconstrainedDataManager.save(violation);
        Shift reloaded = unconstrainedDataManager.load(Shift.class).id(shiftId).one();
        return toShiftItem(reloaded, si.getId());
    }

    @Transactional
    public BotShiftItem createCriminalViolation(long telegramChatId, UUID shiftId,
                                                BotCriminalViolationCreateRequest body) {
        if (body == null || body.impactId() == null || body.typeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "impactId and typeId required");
        }
        Impact impact = requireImpact(body.impactId());
        TypeOfCriminal type = requireCriminalType(body.typeId());
        ServiceInfo si = requireServiceInfo(telegramChatId);
        Shift shift = loadOpenShiftForParticipant(shiftId, si.getId());
        CriminalViolation violation = unconstrainedDataManager.create(CriminalViolation.class);
        violation.setShift(shift);
        violation.setImpact(impact);
        violation.setType(type);
        unconstrainedDataManager.save(violation);
        Shift reloaded = unconstrainedDataManager.load(Shift.class).id(shiftId).one();
        return toShiftItem(reloaded, si.getId());
    }

    private static int requireMetricDelta(BotShiftMetricDeltaRequest body) {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body required");
        }
        int delta = body.delta();
        if (delta != 1 && delta != -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "delta must be 1 or -1");
        }
        return delta;
    }

    private Shift loadOpenShiftForParticipant(UUID shiftId, UUID serviceInfoId) {
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
        boolean member = shift.getUnits().stream()
                .anyMatch(u -> u != null && serviceInfoId.equals(u.getId()));
        if (!member) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "shift not accessible");
        }
        if (shift.getEndTime() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "shift is closed");
        }
        return shift;
    }

    private Impact requireImpact(Integer impactId) {
        Impact impact = Impact.fromId(impactId);
        if (impact == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid impactId");
        }
        return impact;
    }

    private ArticleOfAdministrative requireAdministrativeArticle(Integer articleId) {
        ArticleOfAdministrative article = ArticleOfAdministrative.fromId(articleId);
        if (article == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid articleId");
        }
        return article;
    }

    private TypeOfCriminal requireCriminalType(Integer typeId) {
        TypeOfCriminal type = TypeOfCriminal.fromId(typeId);
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid typeId");
        }
        return type;
    }

    private int countAdministrativeViolations(UUID shiftId) {
        Long count = unconstrainedDataManager.loadValue(
                        "select count(v) from AdministrativeViolation v where v.shift.id = :sid",
                        Long.class)
                .parameter("sid", shiftId)
                .one();
        return count == null ? 0 : count.intValue();
    }

    private int countCriminalViolations(UUID shiftId) {
        Long count = unconstrainedDataManager.loadValue(
                        "select count(v) from CriminalViolation v where v.shift.id = :sid",
                        Long.class)
                .parameter("sid", shiftId)
                .one();
        return count == null ? 0 : count.intValue();
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

    private Set<UUID> openShiftParticipantIdsExcept(UUID exceptShiftId) {
        List<UUID> ids;
        if (exceptShiftId == null) {
            ids = unconstrainedDataManager.loadValue(
                            "select distinct u.id from Shift s join s.units u where s.endTime is null",
                            UUID.class)
                    .list();
        } else {
            ids = unconstrainedDataManager.loadValue(
                            "select distinct u.id from Shift s join s.units u "
                                    + "where s.endTime is null and s.id <> :exceptId",
                            UUID.class)
                    .parameter("exceptId", exceptShiftId)
                    .list();
        }
        return new HashSet<>(ids);
    }

    private void assertNotInOtherOpenShift(UUID serviceInfoId, UUID exceptShiftId) {
        if (serviceInfoId == null) {
            return;
        }
        Long count;
        if (exceptShiftId == null) {
            count = unconstrainedDataManager.loadValue(
                            "select count(distinct s) from Shift s join s.units u "
                                    + "where u.id = :sid and s.endTime is null",
                            Long.class)
                    .parameter("sid", serviceInfoId)
                    .one();
        } else {
            count = unconstrainedDataManager.loadValue(
                            "select count(distinct s) from Shift s join s.units u "
                                    + "where u.id = :sid and s.endTime is null and s.id <> :exceptId",
                            Long.class)
                    .parameter("sid", serviceInfoId)
                    .parameter("exceptId", exceptShiftId)
                    .one();
        }
        if (count != null && count > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "open shift conflict");
        }
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

    private static void applyCreate(Shift shift, BotShiftUpsertRequest req) {
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

    /**
     * Обновление: {@code null} в счётчиках и {@code endTime} не перезаписывает уже сохранённые значения.
     */
    private static void applyUpdate(Shift shift, BotShiftUpsertRequest req) {
        shift.setDate(req.date());
        shift.setNumber(NumberOfShift.fromId(req.routeId().trim()));
        shift.setTypeOfShift(TypeOfShift.fromId(req.typeOfShiftId().trim()));
        shift.setStartTime(req.startTime());
        if (req.endTime() != null) {
            shift.setEndTime(req.endTime());
        }
        if (req.countOfStatements() != null) {
            shift.setCountOfStatements(req.countOfStatements());
        }
        if (req.countOfClaims() != null) {
            shift.setCountOfClaims(req.countOfClaims());
        }
        if (req.ibdWithMigrant() != null) {
            shift.setIbdWithMigrant(req.ibdWithMigrant());
        }
        if (req.ibdWithoutMigrant() != null) {
            shift.setIbdWithoutMigrant(req.ibdWithoutMigrant());
        }
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

    private BotShiftItem toShiftItem(Shift s, UUID viewerServiceInfoId) {
        NumberOfShift route = s.getNumber();
        TypeOfShift type = s.getTypeOfShift();
        Dep dep = s.getDepartmentToday();
        List<UUID> others = otherParticipantServiceInfoIds(s, viewerServiceInfoId);
        UUID partnerSiId = others.isEmpty() ? null : others.get(0);
        return new BotShiftItem(
                s.getId(),
                s.getDate(),
                route == null ? null : route.getId(),
                type == null ? null : enumMessage("TypeOfShift", type.name()),
                type == null ? null : type.getId(),
                dep == null ? null : enumMessage("Dep", dep.name()),
                s.getStartTime(),
                s.getEndTime(),
                s.getCountOfStatements(),
                s.getCountOfClaims(),
                s.getIbdWithMigrant(),
                s.getIbdWithoutMigrant(),
                countAdministrativeViolations(s.getId()),
                countCriminalViolations(s.getId()),
                partnerSiId,
                others
        );
    }

    private static List<UUID> otherParticipantServiceInfoIds(Shift s, UUID viewerServiceInfoId) {
        if (viewerServiceInfoId == null || s.getUnits() == null) {
            return List.of();
        }
        List<UUID> ids = new ArrayList<>();
        for (ServiceInfo u : s.getUnits()) {
            if (u != null && u.getId() != null && !viewerServiceInfoId.equals(u.getId())) {
                ids.add(u.getId());
            }
        }
        ids.sort(Comparator.naturalOrder());
        return List.copyOf(ids);
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
