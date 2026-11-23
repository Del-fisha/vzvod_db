package com.company.vzvod.annual;

import com.company.vzvod.annual.AnnualDtos.AnnualBoardDto;
import com.company.vzvod.annual.AnnualDtos.AnnualFlagsRequest;
import com.company.vzvod.annual.AnnualDtos.AnnualItemDto;
import com.company.vzvod.annual.AnnualDtos.AnnualLineDto;
import com.company.vzvod.annual.AnnualDtos.AnnualPatchRequest;
import com.company.vzvod.annual.AnnualDtos.AnnualPersonDto;
import com.company.vzvod.annual.AnnualDtos.AnnualRosterDto;
import com.company.vzvod.annual.AnnualRules.ItemState;
import com.company.vzvod.annual.AnnualRules.Snap;
import com.company.vzvod.bot.BotMeProfileService;
import com.company.vzvod.entity.AnnualMark;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.Gender;
import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.Rank;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AnnualBoardService {

    private final UnconstrainedDataManager dataManager;
    private final BotMeProfileService profileService;
    private final JdbcTemplate jdbcTemplate;

    public AnnualBoardService(
            UnconstrainedDataManager dataManager,
            BotMeProfileService profileService,
            JdbcTemplate jdbcTemplate
    ) {
        this.dataManager = dataManager;
        this.profileService = profileService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public AnnualBoardDto loadOwn(UUID userId) {
        profileService.requireActive(userId);
        return board(loadUser(userId), AnnualRules.todayMoscow());
    }

    @Transactional(readOnly = true)
    public AnnualBoardDto loadForCommander(UUID actorId, UUID targetUserId) {
        profileService.requireCommander(actorId);
        return board(loadUser(targetUserId), AnnualRules.todayMoscow());
    }

    @Transactional
    public AnnualBoardDto patchOwn(UUID userId, AnnualPatchRequest request) {
        profileService.requireActive(userId);
        return applyPatch(loadUser(userId), request, AnnualRules.todayMoscow(), true);
    }

    @Transactional
    public AnnualBoardDto patchByCommander(UUID actorId, UUID targetUserId, AnnualPatchRequest request) {
        profileService.requireCommander(actorId);
        return applyPatch(loadUser(targetUserId), request, AnnualRules.todayMoscow(), true);
    }

    @Transactional
    public AnnualBoardDto setFlags(UUID userId, AnnualFlagsRequest request) {
        profileService.requireActive(userId);
        if (request == null
                || (request.medicalExamination() == null && request.cpd() == null && request.psychologist() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no fields");
        }
        User user = loadUser(userId);
        LocalDate today = AnnualRules.todayMoscow();
        ServiceInfo si = user.getServiceInfo();
        if (request.medicalExamination() != null) {
            writeMedical(si.getId(), request.medicalExamination());
        }
        if (request.cpd() != null) {
            setChecked(si, AnnualItemKind.CPD, today.getYear(), request.cpd());
        }
        if (request.psychologist() != null) {
            setChecked(si, AnnualItemKind.PSYCHOLOGIST, today.getYear(), request.psychologist());
        }
        Boolean medical = request.medicalExamination();
        return board(user, today, medical);
    }

    @Transactional(readOnly = true)
    public AnnualRosterDto roster(UUID viewerId, AnnualSection section, Integer department, AnnualSort sort) {
        profileService.requireActive(viewerId);
        AnnualSection safeSection = section == null ? AnnualSection.ALL : section;
        AnnualSort safeSort = sort == null ? AnnualSort.DEFAULT : sort;
        LocalDate today = AnnualRules.todayMoscow();
        Map<UUID, List<AnnualMark>> byService = marksByService();
        List<PersonRow> rows = new ArrayList<>();
        for (User user : loadRosterUsers()) {
            ServiceInfo si = user.getServiceInfo();
            if (si == null || si.getId() == null) {
                continue;
            }
            Integer number = departmentNumber(si);
            if (department != null && (number == null || !department.equals(number))) {
                continue;
            }
            rows.add(toRow(user, si, byService.getOrDefault(si.getId(), List.of()), safeSection, today));
        }
        rows.sort(personComparator(safeSort));
        List<AnnualPersonDto> people = new ArrayList<>();
        for (PersonRow row : rows) {
            people.add(row.dto());
        }
        return new AnnualRosterDto(people);
    }

    private AnnualBoardDto applyPatch(User user, AnnualPatchRequest request, LocalDate today, boolean syncMedical) {
        if (request == null || request.kind() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kind required");
        }
        AnnualItemKind kind = AnnualItemKind.fromId(request.kind());
        if (kind == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown kind");
        }
        boolean female = isFemale(user);
        if (!kind.appliesTo(female)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kind is not available");
        }
        ServiceInfo si = user.getServiceInfo();
        AnnualMark mark = findOrCreate(si, kind, kind.storageYear(today.getYear()));
        Snap snap = snap(mark);
        if (Boolean.TRUE.equals(request.clearDate())) {
            mark.setPerformedOn(null);
            mark.setValidUntil(null);
            snap = snap(mark);
        }
        if (request.performedOn() != null) {
            mark.setPerformedOn(request.performedOn());
            mark.setValidUntil(AnnualRules.initialValidUntil(kind, request.performedOn(), user.getDateOfBirth(), today));
            if (kind.group() == AnnualItemKind.Group.ANALYSIS) {
                mark.setChecked(true);
            }
            snap = snap(mark);
        }
        if (Boolean.TRUE.equals(request.extend())) {
            if (!kind.rollingAnalysis() || !AnnualRules.counts(kind, snap, user.getDateOfBirth(), today)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Продлить можно только актуальную Флюорографию или ЭКГ");
            }
            AnnualRules.Extension extension = AnnualRules.extend(kind, snap, user.getDateOfBirth(), today);
            mark.setPerformedOn(extension.performedOn());
            mark.setValidUntil(extension.validUntil());
            mark.setChecked(true);
        } else if (request.checked() != null) {
            mark.setChecked(request.checked());
        }
        dataManager.save(mark);
        Boolean medical = null;
        if (syncMedical) {
            medical = syncMedical(user, si, today);
        }
        return board(user, today, medical);
    }

    private void setChecked(ServiceInfo si, AnnualItemKind kind, int year, boolean checked) {
        AnnualMark mark = findOrCreate(si, kind, kind.storageYear(year));
        mark.setChecked(checked);
        dataManager.save(mark);
    }

    private boolean syncMedical(User user, ServiceInfo si, LocalDate today) {
        List<ItemState> items = states(user, marksOf(si.getId()), today);
        boolean passed = items.stream().filter(item -> item.kind().profosmotr()).allMatch(ItemState::counts);
        writeMedical(si.getId(), passed);
        return passed;
    }

    /**
     * Точечное обновление. Сохранение всей ServiceInfo здесь нельзя:
     * карточка загружена не целиком, и EclipseLink падает на незапрошенном status.
     */
    private void writeMedical(UUID serviceInfoId, boolean passed) {
        jdbcTemplate.update(
                "update service_info set medical_examination = ? where id = ?",
                passed,
                serviceInfoId
        );
    }

    private AnnualBoardDto board(User user, LocalDate today) {
        return board(user, today, null);
    }

    private AnnualBoardDto board(User user, LocalDate today, Boolean medicalOverride) {
        ServiceInfo si = user.getServiceInfo();
        List<ItemState> items = states(user, marksOf(si.getId()), today);
        int done = 0;
        List<AnnualItemDto> dtos = new ArrayList<>();
        for (ItemState item : items) {
            if (item.counts()) {
                done++;
            }
            dtos.add(new AnnualItemDto(
                    item.kind().name(),
                    item.kind().title(),
                    item.kind().group().name(),
                    item.checked(),
                    item.counts(),
                    item.extendable(),
                    item.kind().dateAllowed(),
                    item.performedOn(),
                    item.validUntil()
            ));
        }
        boolean medical = medicalOverride != null
                ? medicalOverride
                : si.getMedicalExamination() != null && si.getMedicalExamination();
        return new AnnualBoardDto(isFemale(user), medical, AnnualRules.percent(done, items.size()), dtos);
    }

    private PersonRow toRow(User user, ServiceInfo si, List<AnnualMark> marks, AnnualSection section, LocalDate today) {
        List<ItemState> items = states(user, marks, today);
        List<ItemState> scoped = new ArrayList<>();
        for (ItemState item : items) {
            if (section.includes(item.kind())) {
                scoped.add(item);
            }
        }
        int done = 0;
        for (ItemState item : scoped) {
            if (item.counts()) {
                done++;
            }
        }
        List<AnnualLineDto> lines = lines(items, section);
        AnnualPersonDto dto = new AnnualPersonDto(
                user.getId(),
                user.getShortFio(),
                departmentNumber(si),
                AnnualRules.percent(done, scoped.size()),
                lines
        );
        Post post = si.getPost();
        Rank rank = si.getRank();
        AnnualRosterOrder.Key key = new AnnualRosterOrder.Key(
                departmentNumber(si),
                post == null ? null : post.getId(),
                rank == null ? null : rank.getId(),
                user.getLastName(),
                dto.percent()
        );
        return new PersonRow(key, dto);
    }

    private static List<AnnualLineDto> lines(List<ItemState> items, AnnualSection section) {
        List<AnnualLineDto> lines = new ArrayList<>();
        if (section == AnnualSection.ALL || section == AnnualSection.PROFOSMOTR) {
            lines.add(new AnnualLineDto("Профосмотр", missingLine(items, AnnualItemKind.Group.DOCTOR, AnnualItemKind.Group.ANALYSIS)));
        }
        if (section == AnnualSection.ALL || section == AnnualSection.TESTS) {
            lines.add(new AnnualLineDto("Зачёты", missingLine(items, AnnualItemKind.Group.TEST)));
        }
        if (section == AnnualSection.ALL || section == AnnualSection.CPD) {
            lines.add(new AnnualLineDto("ЦПД", AnnualRules.passedLine(singleCounts(items, AnnualItemKind.CPD))));
        }
        if (section == AnnualSection.ALL || section == AnnualSection.PSYCHOLOGIST) {
            lines.add(new AnnualLineDto("Психолог", AnnualRules.passedLine(singleCounts(items, AnnualItemKind.PSYCHOLOGIST))));
        }
        return lines;
    }

    private static String missingLine(List<ItemState> items, AnnualItemKind.Group... groups) {
        List<String> missing = new ArrayList<>();
        for (ItemState item : items) {
            boolean inGroup = false;
            for (AnnualItemKind.Group group : groups) {
                if (item.kind().group() == group) {
                    inGroup = true;
                    break;
                }
            }
            if (inGroup && !item.counts()) {
                missing.add(item.kind().title());
            }
        }
        return AnnualRules.statusLine(String.join(", ", missing));
    }

    private static boolean singleCounts(List<ItemState> items, AnnualItemKind kind) {
        for (ItemState item : items) {
            if (item.kind() == kind) {
                return item.counts();
            }
        }
        return false;
    }

    private List<ItemState> states(User user, List<AnnualMark> marks, LocalDate today) {
        Map<String, AnnualMark> index = new HashMap<>();
        for (AnnualMark mark : marks) {
            index.put(mark.getKind() + ":" + mark.getMarkYear(), mark);
        }
        boolean female = isFemale(user);
        List<ItemState> items = new ArrayList<>();
        for (AnnualItemKind kind : AnnualItemKind.values()) {
            if (!kind.appliesTo(female)) {
                continue;
            }
            AnnualMark mark = index.get(kind.name() + ":" + kind.storageYear(today.getYear()));
            Snap snap = mark == null ? Snap.EMPTY : snap(mark);
            items.add(AnnualRules.view(kind, snap, user.getDateOfBirth(), today));
        }
        return items;
    }

    private static Snap snap(AnnualMark mark) {
        return new Snap(Boolean.TRUE.equals(mark.getChecked()), mark.getPerformedOn(), mark.getValidUntil());
    }

    private AnnualMark findOrCreate(ServiceInfo si, AnnualItemKind kind, int year) {
        return dataManager.load(AnnualMark.class)
                .query("select m from AnnualMark m where m.serviceInfo = :si and m.kind = :kind and m.markYear = :year")
                .parameter("si", si)
                .parameter("kind", kind.name())
                .parameter("year", year)
                .optional()
                .orElseGet(() -> {
                    AnnualMark created = dataManager.create(AnnualMark.class);
                    created.setServiceInfo(si);
                    created.setKind(kind.name());
                    created.setMarkYear(year);
                    created.setChecked(false);
                    return created;
                });
    }

    private List<AnnualMark> marksOf(UUID serviceInfoId) {
        return dataManager.load(AnnualMark.class)
                .query("select m from AnnualMark m where m.serviceInfo.id = :id")
                .parameter("id", serviceInfoId)
                .list();
    }

    private Map<UUID, List<AnnualMark>> marksByService() {
        Map<UUID, List<AnnualMark>> grouped = new HashMap<>();
        for (AnnualMark mark : dataManager.load(AnnualMark.class)
                .query("select m from AnnualMark m")
                .fetchPlan(fp -> fp.add("kind").add("markYear").add("checked").add("performedOn").add("validUntil")
                        .add("serviceInfo", si -> si.add("id")))
                .list()) {
            if (mark.getServiceInfo() == null || mark.getServiceInfo().getId() == null) {
                continue;
            }
            grouped.computeIfAbsent(mark.getServiceInfo().getId(), id -> new ArrayList<>()).add(mark);
        }
        return grouped;
    }

    private User loadUser(UUID userId) {
        return dataManager.load(User.class)
                .id(userId)
                .fetchPlan(this::userPlan)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }

    private List<User> loadRosterUsers() {
        return dataManager.load(User.class)
                .query("select u from User u where u.serviceInfo is not null")
                .fetchPlan(this::userPlan)
                .list();
    }

    private void userPlan(io.jmix.core.FetchPlanBuilder fp) {
        fp.add("firstName")
                .add("lastName")
                .add("patronymic")
                .add("gender")
                .add("dateOfBirth")
                .add("serviceInfo", si -> si
                        .add("post")
                        .add("rank")
                        .add("medicalExamination")
                        .add("department", d -> d.add("number")));
    }

    private static boolean isFemale(User user) {
        return user.getGender() == Gender.FEMALE;
    }

    private static Integer departmentNumber(ServiceInfo si) {
        Department department = si.getDepartment();
        return department == null ? null : department.getNumber();
    }

    private static Comparator<PersonRow> personComparator(AnnualSort sort) {
        Comparator<AnnualRosterOrder.Key> keys = AnnualRosterOrder.comparator(sort);
        return (left, right) -> keys.compare(left.key(), right.key());
    }

    private record PersonRow(AnnualRosterOrder.Key key, AnnualPersonDto dto) {
    }
}
