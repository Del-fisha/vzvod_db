package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotAdministrativeViolationCreateRequest;
import com.company.vzvod.bot.dto.BotCatalogOptionsResponse;
import com.company.vzvod.bot.dto.BotColleagueItem;
import com.company.vzvod.bot.dto.BotColleaguesResponse;
import com.company.vzvod.bot.dto.BotCriminalViolationCreateRequest;
import com.company.vzvod.bot.dto.BotEnumOption;
import com.company.vzvod.bot.dto.BotShiftEndTimeRequest;
import com.company.vzvod.bot.dto.BotShiftItem;
import com.company.vzvod.bot.dto.BotShiftMetricDeltaRequest;
import com.company.vzvod.bot.dto.BotShiftRouteOption;
import com.company.vzvod.bot.dto.BotShiftUpsertRequest;
import com.company.vzvod.bot.dto.BotShiftsResponse;
import com.company.vzvod.bot.dto.BotStringEnumOption;
import com.company.vzvod.bot.dto.BotVacationBalance;
import com.company.vzvod.bot.dto.BotVacationsResponse;
import com.company.vzvod.bot.dto.BotViolationOptionsResponse;
import com.company.vzvod.bot.dto.BotVocationCreateRequest;
import com.company.vzvod.bot.dto.BotVocationItem;
import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.Impact;
import com.company.vzvod.entity.MetroStation;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfCriminal;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.User;
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
import java.util.LinkedHashSet;
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
    public BotShiftsResponse loadShifts(UUID userId) {
        UUID serviceInfoId = loadServiceInfoId(userId);
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
                        .add("ibdr")
                        .add("migrant")
                        .add("units"))
                .list();
        List<BotShiftItem> items = new ArrayList<>(shifts.size());
        for (Shift s : shifts) {
            items.add(toShiftItem(s, serviceInfoId));
        }
        return new BotShiftsResponse(items);
    }

    @Transactional(readOnly = true)
    public BotShiftItem loadShift(UUID userId, UUID shiftId) {
        ServiceInfo si = requireServiceInfo(userId);
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
                        .add("ibdr")
                        .add("migrant"))
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "shift not found"));
        boolean member = shift.getUnits().stream().anyMatch(u -> u.getId().equals(si.getId()));
        if (!member) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "shift not accessible");
        }
        return toShiftItem(shift, si.getId());
    }

    /**
     * Список активных сотрудников для выбора напарника.
     * {@code preferredDepartment} — отделение «сегодня» (первыми в списке), затем другое.
     * В ответе всегда оба отделения; подпись — краткое ФИО ({@code Фамилия И. О.}).
     */
    @Transactional(readOnly = true)
    public BotColleaguesResponse loadColleagues(UUID userId, int preferredDepartment, int page, UUID excludeShiftId) {
        if (preferredDepartment != 1 && preferredDepartment != 2) {
            preferredDepartment = 1;
        }
        if (page < 0) {
            page = 0;
        }
        User me = loadUserWithServiceInfo(userId);
        UUID myId = me.getId();
        Set<UUID> busyInOpenShifts = openShiftParticipantIdsExcept(excludeShiftId);
        List<User> users = unconstrainedDataManager.load(User.class)
                .query("select u from User u join u.serviceInfo si join si.department d "
                        + "where d.number in (1, 2) and u.id <> :uid and si.status = :st "
                        + "order by u.lastName, u.firstName")
                .parameter("uid", myId)
                .parameter("st", StatusInService.ACTIVE.getId())
                .maxResults(COLLEAGUES_MAX_FETCH)
                .list();
        int preferred = preferredDepartment;
        List<User> available = new ArrayList<>(users.size());
        for (User u : users) {
            ServiceInfo si = u.getServiceInfo();
            if (si == null || busyInOpenShifts.contains(si.getId())) {
                continue;
            }
            available.add(u);
        }
        available.sort((a, b) -> {
            int da = departmentNumber(a);
            int db = departmentNumber(b);
            boolean aPref = da == preferred;
            boolean bPref = db == preferred;
            if (aPref != bPref) {
                return aPref ? -1 : 1;
            }
            String la = a.getLastName() == null ? "" : a.getLastName();
            String lb = b.getLastName() == null ? "" : b.getLastName();
            int byLast = la.compareToIgnoreCase(lb);
            if (byLast != 0) {
                return byLast;
            }
            String fa = a.getFirstName() == null ? "" : a.getFirstName();
            String fb = b.getFirstName() == null ? "" : b.getFirstName();
            return fa.compareToIgnoreCase(fb);
        });
        // Для мобильного клиента отдаём весь доступный список одной страницей.
        int pageSize = Math.max(COLLEAGUES_PAGE_SIZE, available.size());
        int from = page * pageSize;
        if (from >= available.size()) {
            return new BotColleaguesResponse(List.of(), false);
        }
        int to = Math.min(from + pageSize, available.size());
        boolean hasMore = to < available.size();
        List<BotColleagueItem> items = new ArrayList<>(to - from);
        for (int i = from; i < to; i++) {
            User u = available.get(i);
            ServiceInfo si = u.getServiceInfo();
            if (si == null) {
                continue;
            }
            items.add(new BotColleagueItem(si.getId(), colleagueLabel(u), departmentNumber(u)));
        }
        return new BotColleaguesResponse(items, hasMore);
    }

    private static int departmentNumber(User u) {
        if (u == null || u.getServiceInfo() == null || u.getServiceInfo().getDepartment() == null) {
            return 0;
        }
        Integer n = u.getServiceInfo().getDepartment().getNumber();
        return n == null ? 0 : n;
    }

    @Transactional
    public BotShiftItem createShift(UUID userId, BotShiftUpsertRequest req) {
        validateCreate(req);
        User user = loadUserWithServiceInfo(userId);
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
        for (UUID partnerId : resolvePartnerIds(req)) {
            ServiceInfo partner = loadAndValidatePartner(partnerId, user.getId());
            assertNotInOtherOpenShift(partner.getId(), null);
            shift.getUnits().add(partner);
        }
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved, si.getId());
    }

    @Transactional
    public BotShiftItem updateShift(UUID userId, UUID shiftId, BotShiftUpsertRequest req) {
        validateUpdate(req);
        ServiceInfo si = requireServiceInfo(userId);
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
                        .add("ibdr")
                        .add("migrant"))
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
        List<UUID> partnerIds = resolvePartnerIds(req);
        if (!partnerIds.isEmpty()) {
            User user = loadUserWithServiceInfo(userId);
            for (UUID partnerId : partnerIds) {
                ServiceInfo partner = loadAndValidatePartner(partnerId, user.getId());
                UUID partnerSid = partner.getId();
                boolean already = shift.getUnits().stream()
                        .anyMatch(u -> u != null && partnerSid.equals(u.getId()));
                if (already) {
                    continue;
                }
                assertNotInOtherOpenShift(partnerSid, shiftId);
                shift.getUnits().add(partner);
            }
        }
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved, si.getId());
    }

    @Transactional
    public BotShiftItem setShiftEndTime(UUID userId, UUID shiftId, BotShiftEndTimeRequest body) {
        if (body == null || body.endTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime required");
        }
        ServiceInfo si = requireServiceInfo(userId);
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
    public BotShiftItem adjustIbdr(UUID userId, UUID shiftId, BotShiftMetricDeltaRequest body) {
        int delta = requireMetricDelta(body);
        ServiceInfo si = requireServiceInfo(userId);
        Shift shift = loadOpenShiftForParticipant(shiftId, si.getId());
        int current = shift.getIbdr() == null ? 0 : shift.getIbdr();
        int next = Math.max(0, current + delta);
        if (next == current) {
            return toShiftItem(shift, si.getId());
        }
        shift.setIbdr(next);
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved, si.getId());
    }

    @Transactional
    public BotShiftItem adjustCountOfStatements(UUID userId, UUID shiftId, BotShiftMetricDeltaRequest body) {
        int delta = requireMetricDelta(body);
        ServiceInfo si = requireServiceInfo(userId);
        Shift shift = loadOpenShiftForParticipant(shiftId, si.getId());
        int current = shift.getCountOfStatements() == null ? 0 : shift.getCountOfStatements();
        int next = Math.max(0, current + delta);
        if (next == current) {
            return toShiftItem(shift, si.getId());
        }
        shift.setCountOfStatements(next);
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved, si.getId());
    }

    @Transactional
    public BotShiftItem adjustMigrant(UUID userId, UUID shiftId, BotShiftMetricDeltaRequest body) {
        int delta = requireMetricDelta(body);
        ServiceInfo si = requireServiceInfo(userId);
        Shift shift = loadOpenShiftForParticipant(shiftId, si.getId());
        int current = shift.getMigrant() == null ? 0 : shift.getMigrant();
        int next = Math.max(0, current + delta);
        if (next == current) {
            return toShiftItem(shift, si.getId());
        }
        shift.setMigrant(next);
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved, si.getId());
    }

    @Transactional
    public BotShiftItem adjustCountOfClaims(UUID userId, UUID shiftId, BotShiftMetricDeltaRequest body) {
        int delta = requireMetricDelta(body);
        ServiceInfo si = requireServiceInfo(userId);
        Shift shift = loadOpenShiftForParticipant(shiftId, si.getId());
        int current = shift.getCountOfClaims() == null ? 0 : shift.getCountOfClaims();
        int next = Math.max(0, current + delta);
        if (next == current) {
            return toShiftItem(shift, si.getId());
        }
        shift.setCountOfClaims(next);
        Shift saved = unconstrainedDataManager.save(shift);
        return toShiftItem(saved, si.getId());
    }

    /**
     * «Мигрант + ИБДР ±»: одновременно меняет migrant и ibdr на delta (±1).
     * Ниже нуля не уходит — остаётся 0 без ошибки.
     */
    @Transactional
    public BotShiftItem adjustMigrantAndIbdr(UUID userId, UUID shiftId, BotShiftMetricDeltaRequest body) {
        int delta = requireMetricDelta(body);
        ServiceInfo si = requireServiceInfo(userId);
        Shift shift = loadOpenShiftForParticipant(shiftId, si.getId());
        int without = shift.getMigrant() == null ? 0 : shift.getMigrant();
        int with = shift.getIbdr() == null ? 0 : shift.getIbdr();
        int nextWithout = Math.max(0, without + delta);
        int nextWith = Math.max(0, with + delta);
        if (nextWithout == without && nextWith == with) {
            return toShiftItem(shift, si.getId());
        }
        shift.setMigrant(nextWithout);
        shift.setIbdr(nextWith);
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

    @Transactional(readOnly = true)
    public BotCatalogOptionsResponse loadCatalogOptions() {
        List<BotEnumOption> vocationTypes = new ArrayList<>(VocationType.values().length);
        for (VocationType type : VocationType.values()) {
            vocationTypes.add(new BotEnumOption(type.getId(), enumMessage("VocationType", type.name())));
        }

        List<BotShiftRouteOption> shiftRoutes = new ArrayList<>(NumberOfShift.values().length);
        for (NumberOfShift route : NumberOfShift.values()) {
            TypeOfShift defaultType = route.defaultTypeOfShift();
            shiftRoutes.add(new BotShiftRouteOption(
                    route.getId(),
                    enumMessage("NumberOfShift", route.name()),
                    defaultType == null ? null : defaultType.getId()
            ));
        }

        List<BotStringEnumOption> shiftTypes = new ArrayList<>(TypeOfShift.values().length);
        for (TypeOfShift type : TypeOfShift.values()) {
            shiftTypes.add(new BotStringEnumOption(type.getId(), enumMessage("TypeOfShift", type.name())));
        }

        List<BotEnumOption> metroStations = new ArrayList<>(MetroStation.values().length);
        for (MetroStation station : MetroStation.values()) {
            metroStations.add(new BotEnumOption(station.getId(), enumMessage("MetroStation", station.name())));
        }

        return new BotCatalogOptionsResponse(
                List.copyOf(vocationTypes),
                List.copyOf(shiftRoutes),
                List.copyOf(shiftTypes),
                List.copyOf(metroStations)
        );
    }

    @Transactional
    public BotShiftItem createAdministrativeViolation(UUID userId, UUID shiftId,
                                                      BotAdministrativeViolationCreateRequest body) {
        if (body == null || body.impactId() == null || body.articleId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "impactId and articleId required");
        }
        Impact impact = requireImpact(body.impactId());
        ArticleOfAdministrative article = requireAdministrativeArticle(body.articleId());
        ServiceInfo si = requireServiceInfo(userId);
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
    public BotShiftItem createCriminalViolation(UUID userId, UUID shiftId,
                                                BotCriminalViolationCreateRequest body) {
        if (body == null || body.impactId() == null || body.typeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "impactId and typeId required");
        }
        Impact impact = requireImpact(body.impactId());
        TypeOfCriminal type = requireCriminalType(body.typeId());
        ServiceInfo si = requireServiceInfo(userId);
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
                        .add("ibdr")
                        .add("migrant"))
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
        if (resolvePartnerIds(req).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "partnerServiceInfoId required");
        }
    }

    /**
     * Объединяет одиночный partnerServiceInfoId и список partnerServiceInfoIds без дублей.
     */
    private static List<UUID> resolvePartnerIds(BotShiftUpsertRequest req) {
        LinkedHashSet<UUID> ids = new LinkedHashSet<>();
        if (req.partnerServiceInfoId() != null) {
            ids.add(req.partnerServiceInfoId());
        }
        if (req.partnerServiceInfoIds() != null) {
            for (UUID id : req.partnerServiceInfoIds()) {
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return List.copyOf(ids);
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
        if (u == null) {
            return "—";
        }
        String shortFio = u.getShortFio();
        return shortFio.isBlank() ? "Сотрудник" : shortFio;
    }

    private static void applyCreate(Shift shift, BotShiftUpsertRequest req) {
        shift.setDate(req.date());
        shift.setNumber(NumberOfShift.fromId(req.routeId().trim()));
        shift.setTypeOfShift(TypeOfShift.fromId(req.typeOfShiftId().trim()));
        shift.setStartTime(req.startTime());
        shift.setEndTime(req.endTime());
        shift.setCountOfStatements(req.countOfStatements() != null ? req.countOfStatements() : 0);
        shift.setCountOfClaims(req.countOfClaims() != null ? req.countOfClaims() : 0);
        shift.setIbdr(req.ibdr() != null ? req.ibdr() : 0);
        shift.setMigrant(req.migrant() != null ? req.migrant() : 0);
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
        if (req.ibdr() != null) {
            shift.setIbdr(req.ibdr());
        }
        if (req.migrant() != null) {
            shift.setMigrant(req.migrant());
        }
    }

    private User loadUserWithServiceInfo(UUID userId) {
        activeUserChecker.requireActive(userId);
        return unconstrainedDataManager.load(User.class)
                .id(userId)
                .fetchPlan(fp -> fp.add("serviceInfo", sf -> sf.add("id")))
                .one();
    }

    private ServiceInfo requireServiceInfo(UUID userId) {
        User user = loadUserWithServiceInfo(userId);
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
                s.getIbdr(),
                s.getMigrant(),
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
    public BotVacationsResponse loadVacations(UUID userId) {
        User user = loadUserWithVacationFields(userId);
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
                    v.getId(),
                    v.getStartDate(),
                    v.getEndDate(),
                    v.getCountOfDays(),
                    v.getTypeId(),
                    typeLabel,
                    v.isHasDeparture(),
                    v.getCityToDrive(),
                    v.getDaysAddedByDeparture()
            ));
        }
        return new BotVacationsResponse(balance, items);
    }

    @Transactional
    public BotVacationsResponse createVocation(UUID userId, BotVocationCreateRequest body) {
        if (body == null || body.typeId() == null || body.startDate() == null || body.endDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "typeId, startDate, endDate required");
        }
        if (body.endDate().isBefore(body.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate before startDate");
        }
        VocationType type = VocationType.fromId(body.typeId());
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown vocation type");
        }
        User user = loadUserWithVacationFields(userId);
        ServiceInfo si = user.getServiceInfo();
        if (si == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "service info missing");
        }
        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(body.startDate(), body.endDate()) + 1;
        boolean departure = Boolean.TRUE.equals(body.hasDeparture());
        int added = body.daysAddedByDeparture() == null ? 0 : Math.max(0, body.daysAddedByDeparture());
        int debit = days + (departure ? added : 0);
        int available = si.getVacationDaysAvailable() == null ? 0 : si.getVacationDaysAvailable();
        if (debit > available) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough vacation days");
        }
        if (departure && (body.cityToDrive() == null || body.cityToDrive().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cityToDrive required when hasDeparture");
        }
        Vocation v = unconstrainedDataManager.create(Vocation.class);
        v.setUserServiceInfo(si);
        v.setType(type);
        v.setStartDate(body.startDate());
        v.setEndDate(body.endDate());
        v.setCountOfDays(days);
        v.setHasDeparture(departure);
        v.setCityToDrive(departure ? body.cityToDrive().trim() : null);
        v.setDaysAddedByDeparture(departure ? added : 0);
        unconstrainedDataManager.save(v);
        return loadVacations(userId);
    }

    @Transactional
    public BotVacationsResponse deleteVocation(UUID userId, UUID vocationId) {
        ServiceInfo si = requireServiceInfo(userId);
        Vocation v = unconstrainedDataManager.load(Vocation.class)
                .id(vocationId)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "vocation not found"));
        if (v.getUserServiceInfo() == null || !si.getId().equals(v.getUserServiceInfo().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your vocation");
        }
        if (v.getStartDate() != null && !v.getStartDate().isAfter(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only future vocations can be deleted");
        }
        unconstrainedDataManager.remove(v);
        return loadVacations(userId);
    }

    private UUID loadServiceInfoId(UUID userId) {
        ServiceInfo si = requireServiceInfo(userId);
        return si.getId();
    }

    private User loadUserWithVacationFields(UUID userId) {
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
