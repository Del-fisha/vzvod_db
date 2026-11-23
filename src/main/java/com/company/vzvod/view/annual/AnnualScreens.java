package com.company.vzvod.view.annual;

import com.company.vzvod.annual.AnnualBoardService;
import com.company.vzvod.annual.AnnualDtos.AnnualBoardDto;
import com.company.vzvod.annual.AnnualDtos.AnnualItemDto;
import com.company.vzvod.annual.AnnualDtos.AnnualLineDto;
import com.company.vzvod.annual.AnnualDtos.AnnualPatchRequest;
import com.company.vzvod.annual.AnnualDtos.AnnualPersonDto;
import com.company.vzvod.annual.AnnualDtos.AnnualRosterDto;
import com.company.vzvod.annual.AnnualRosterOrder;
import com.company.vzvod.annual.AnnualSection;
import com.company.vzvod.annual.AnnualSort;
import com.company.vzvod.entity.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class AnnualScreens {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final AnnualBoardService annualBoardService;
    private final CurrentAuthentication currentAuthentication;

    public AnnualScreens(AnnualBoardService annualBoardService, CurrentAuthentication currentAuthentication) {
        this.annualBoardService = annualBoardService;
        this.currentAuthentication = currentAuthentication;
    }

    public void showOwn(VerticalLayout body, AnnualSection section) {
        body.removeAll();
        UUID userId = currentUserId();
        AnnualBoardDto board;
        try {
            board = annualBoardService.loadOwn(userId);
        } catch (RuntimeException ex) {
            body.add(new Span(message(ex)));
            return;
        }
        body.add(editor(userId, section, board, false));
    }

    public void showOthers(VerticalLayout body, boolean commander, OthersState state) {
        body.removeAll();
        if (state.editingUserId != null && commander) {
            body.add(backButton(state));
            AnnualBoardDto board;
            try {
                board = annualBoardService.loadForCommander(currentUserId(), state.editingUserId);
            } catch (RuntimeException ex) {
                body.add(new Span(message(ex)));
                return;
            }
            Span name = new Span(state.editingName == null ? "" : state.editingName);
            name.getStyle().set("font-weight", "600");
            body.add(name);
            AnnualSection editSection = state.section == null ? AnnualSection.ALL : state.section;
            body.add(editor(state.editingUserId, editSection, board, true));
            return;
        }
        body.add(filters(state));
        AnnualRosterDto roster;
        try {
            roster = annualBoardService.roster(currentUserId(), state.section, state.department, state.sort);
        } catch (RuntimeException ex) {
            body.add(new Span(message(ex)));
            return;
        }
        Integer previousDepartment = Integer.MIN_VALUE;
        boolean first = true;
        for (AnnualPersonDto person : roster.people()) {
            if (state.department == null && (first || !sameDepartment(previousDepartment, person.departmentNumber()))) {
                Span head = new Span(AnnualRosterOrder.departmentTitle(person.departmentNumber()));
                head.getStyle().set("font-weight", "600").set("margin-top", "12px");
                body.add(head);
                previousDepartment = person.departmentNumber();
                first = false;
            }
            body.add(personCard(person, commander, state));
        }
        if (roster.people().isEmpty()) {
            body.add(new Span("Нет сотрудников"));
        }
    }

    private com.vaadin.flow.component.Component editor(
            UUID userId,
            AnnualSection section,
            AnnualBoardDto board,
            boolean commander
    ) {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(false);
        box.setSpacing(true);
        box.setWidthFull();
        LiveBoard live = new LiveBoard();
        if (section == AnnualSection.PROFOSMOTR || section == AnnualSection.ALL) {
            Span med = new Span(medicalLine(board.medicalPassed()));
            med.getStyle().set("font-weight", "600");
            live.medical = med;
            box.add(med);
        }
        addGroup(box, "Все врачи", board, "DOCTOR", section, userId, commander, live);
        addGroup(box, "Анализы", board, "ANALYSIS", section, userId, commander, live);
        addGroup(box, "ЦПД", board, "CPD", section, userId, commander, live);
        addGroup(box, "Зачёты", board, "TEST", section, userId, commander, live);
        addGroup(box, "Психолог", board, "PSYCHOLOGIST", section, userId, commander, live);
        return box;
    }

    private void addGroup(
            VerticalLayout box,
            String title,
            AnnualBoardDto board,
            String group,
            AnnualSection section,
            UUID userId,
            boolean commander,
            LiveBoard live
    ) {
        if (!sectionShows(section, group)) {
            return;
        }
        List<AnnualItemDto> items = board.items().stream().filter(item -> group.equals(item.group())).toList();
        if (items.isEmpty()) {
            return;
        }
        boolean analyses = "ANALYSIS".equals(group);
        Span head = new Span(title);
        head.getStyle().set("font-weight", "700").set("text-decoration", "underline");
        box.add(head);
        Div grid = new Div();
        grid.setWidthFull();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", analyses
                        ? "max-content 148px max-content auto"
                        : "max-content max-content auto")
                .set("justify-content", "start")
                .set("column-gap", "12px")
                .set("row-gap", "0")
                .set("align-items", "center");
        grid.add(columnHead("Пункт"));
        if (analyses) {
            grid.add(columnHead("Дата"));
        }
        grid.add(columnHead("Статус"), columnHead(""));
        for (AnnualItemDto item : items) {
            addItemCells(grid, userId, item, commander, live, analyses);
        }
        box.add(grid);
    }

    private void addItemCells(Div grid, UUID userId, AnnualItemDto item, boolean commander, LiveBoard live, boolean showDate) {
        Checkbox checkbox = new Checkbox(item.title());
        checkbox.setValue(item.checked());
        live.checks.put(item.kind(), checkbox);
        checkbox.addValueChangeListener(event -> {
            if (!event.isFromClient()) {
                return;
            }
            AnnualBoardDto updated = save(userId, new AnnualPatchRequest(item.kind(), event.getValue(), null, null, null), commander);
            if (updated == null) {
                checkbox.setValue(event.getOldValue());
            } else {
                live.apply(updated);
            }
        });
        grid.add(checkbox);
        if (showDate && item.dateAllowed()) {
            DatePicker date = new DatePicker();
            date.setLocale(Locale.forLanguageTag("ru"));
            date.setWidth("140px");
            date.setValue(item.performedOn());
            live.dates.put(item.kind(), date);
            date.addValueChangeListener(event -> {
                if (!event.isFromClient()) {
                    return;
                }
                LocalDate value = event.getValue();
                AnnualPatchRequest request = value == null
                        ? new AnnualPatchRequest(item.kind(), null, null, null, true)
                        : new AnnualPatchRequest(item.kind(), null, null, value, null);
                AnnualBoardDto updated = save(userId, request, commander);
                if (updated == null) {
                    date.setValue(event.getOldValue());
                } else {
                    live.apply(updated);
                }
            });
            grid.add(date);
        }
        Span term = new Span(termText(item.counts(), item.validUntil()));
        paintTerm(term, item.counts());
        live.terms.put(item.kind(), term);
        grid.add(term);
        if (item.extendable()) {
            grid.add(new Button("Продлить", click -> {
                AnnualBoardDto updated = save(userId, new AnnualPatchRequest(item.kind(), null, true, null, null), commander);
                if (updated != null) {
                    live.apply(updated);
                }
            }));
        } else {
            grid.add(new Span(""));
        }
    }

    public static String termText(boolean counts, LocalDate validUntil) {
        if (!counts) {
            return "Не пройден";
        }
        if (validUntil != null) {
            return "до " + DATE.format(validUntil);
        }
        return "Пройден";
    }

    public static String medicalLine(boolean passed) {
        return "Профосмотр: " + (passed ? "Пройден" : "Не пройден");
    }

    private static void paintTerm(Span term, boolean counts) {
        term.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", counts ? "#2e7d32" : "#c62828");
    }

    private static Span columnHead(String text) {
        Span span = new Span(text);
        span.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");
        return span;
    }

    private AnnualBoardDto save(UUID userId, AnnualPatchRequest request, boolean commander) {
        try {
            if (commander && !userId.equals(currentUserId())) {
                return annualBoardService.patchByCommander(currentUserId(), userId, request);
            }
            return annualBoardService.patchOwn(userId, request);
        } catch (ResponseStatusException ex) {
            Notification.show(message(ex));
            return null;
        } catch (RuntimeException ex) {
            Notification.show("Не удалось сохранить");
            return null;
        }
    }

    private com.vaadin.flow.component.Component filters(OthersState state) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        ComboBox<AnnualSection> section = new ComboBox<>("Раздел");
        section.setItems(AnnualSection.values());
        section.setItemLabelGenerator(AnnualSection::title);
        section.setValue(state.section);
        section.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                state.section = event.getValue();
                state.rerender.run();
            }
        });
        ComboBox<String> department = new ComboBox<>("Отделение");
        department.setItems("Оба отделения", "1-е", "2-е");
        department.setValue(state.department == null ? "Оба отделения" : state.department + "-е");
        department.addValueChangeListener(event -> {
            if (!event.isFromClient() || event.getValue() == null) {
                return;
            }
            state.department = switch (event.getValue()) {
                case "1-е" -> 1;
                case "2-е" -> 2;
                default -> null;
            };
            state.rerender.run();
        });
        ComboBox<AnnualSort> sort = new ComboBox<>("Сортировка");
        sort.setItems(AnnualSort.values());
        sort.setItemLabelGenerator(value -> switch (value) {
            case DEFAULT -> "Отделение, должность, звание, фамилия";
            case PERCENT_ASC -> "Процент от меньшего";
            case PERCENT_DESC -> "Процент от большего";
        });
        sort.setValue(state.sort);
        sort.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                state.sort = event.getValue();
                state.rerender.run();
            }
        });
        row.add(section, department, sort);
        return row;
    }

    private com.vaadin.flow.component.Component personCard(AnnualPersonDto person, boolean commander, OthersState state) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(false);
        card.setSpacing(false);
        card.setWidthFull();
        card.add(bar(person.percent()));
        Span name = new Span(person.shortFio() + "  " + person.percent() + "%");
        name.getStyle()
                .set("font-weight", "700")
                .set("text-align", "center")
                .set("display", "block")
                .set("width", "100%");
        card.add(name);
        for (AnnualLineDto line : person.lines()) {
            Span title = new Span(line.title() + ":");
            title.getStyle()
                    .set("font-weight", "700")
                    .set("text-decoration", "underline")
                    .set("display", "block");
            Span text = new Span(line.text());
            text.getStyle().set("display", "block").set("margin-bottom", "4px");
            card.add(title, text);
        }
        if (commander) {
            card.add(new Button("Изменить", click -> {
                state.editingUserId = person.userId();
                state.editingName = person.shortFio();
                state.rerender.run();
            }));
        }
        card.add(new Hr());
        return card;
    }

    private Button backButton(OthersState state) {
        return new Button("К списку", click -> {
            state.editingUserId = null;
            state.editingName = null;
            state.rerender.run();
        });
    }

    private static Div bar(int percent) {
        Div track = new Div();
        track.setWidthFull();
        track.getStyle().set("height", "4px").set("background", "#e6e6e6");
        Div fill = new Div();
        fill.getStyle().set("height", "4px").set("width", percent + "%").set("background", "#2e7d32");
        track.add(fill);
        return track;
    }

    private static boolean sectionShows(AnnualSection section, String group) {
        return switch (section) {
            case ALL -> true;
            case PROFOSMOTR -> "DOCTOR".equals(group) || "ANALYSIS".equals(group);
            case TESTS -> "TEST".equals(group);
            case CPD -> "CPD".equals(group);
            case PSYCHOLOGIST -> "PSYCHOLOGIST".equals(group);
        };
    }

    private static boolean sameDepartment(Integer previous, Integer current) {
        return previous == null ? current == null : previous.equals(current);
    }

    private UUID currentUserId() {
        return ((User) currentAuthentication.getUser()).getId();
    }

    private static String message(ResponseStatusException ex) {
        String reason = ex.getReason();
        return reason == null || reason.isBlank() ? "Не удалось сохранить" : reason;
    }

    private static String message(RuntimeException ex) {
        if (ex instanceof ResponseStatusException status) {
            return message(status);
        }
        return "Не удалось открыть раздел";
    }

    private static final class LiveBoard {
        private Span medical;
        private final Map<String, Checkbox> checks = new HashMap<>();
        private final Map<String, DatePicker> dates = new HashMap<>();
        private final Map<String, Span> terms = new HashMap<>();

        private void apply(AnnualBoardDto board) {
            if (medical != null) {
                medical.setText(medicalLine(board.medicalPassed()));
            }
            for (AnnualItemDto item : board.items()) {
                Checkbox checkbox = checks.get(item.kind());
                if (checkbox != null && !Objects.equals(checkbox.getValue(), item.checked())) {
                    checkbox.setValue(item.checked());
                }
                DatePicker date = dates.get(item.kind());
                if (date != null && !Objects.equals(date.getValue(), item.performedOn())) {
                    date.setValue(item.performedOn());
                }
                Span term = terms.get(item.kind());
                if (term != null) {
                    term.setText(termText(item.counts(), item.validUntil()));
                    paintTerm(term, item.counts());
                }
            }
        }
    }

    public static final class OthersState {
        public AnnualSection section = AnnualSection.ALL;
        public Integer department;
        public AnnualSort sort = AnnualSort.DEFAULT;
        public UUID editingUserId;
        public String editingName;
        public Runnable rerender = () -> {
        };
    }
}
