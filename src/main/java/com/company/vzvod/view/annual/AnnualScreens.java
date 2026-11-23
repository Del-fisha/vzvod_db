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
import java.util.List;
import java.util.Locale;
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

    public void showOwn(VerticalLayout body, AnnualSection section, Runnable refresh) {
        body.removeAll();
        UUID userId = currentUserId();
        AnnualBoardDto board;
        try {
            board = annualBoardService.loadOwn(userId);
        } catch (ResponseStatusException ex) {
            body.add(new Span(message(ex)));
            return;
        }
        body.add(editor(userId, section, board, false, refresh));
    }

    public void showOthers(VerticalLayout body, boolean commander, OthersState state) {
        body.removeAll();
        if (state.editingUserId != null && commander) {
            body.add(backButton(state));
            AnnualBoardDto board;
            try {
                board = annualBoardService.loadForCommander(currentUserId(), state.editingUserId);
            } catch (ResponseStatusException ex) {
                body.add(new Span(message(ex)));
                return;
            }
            Span name = new Span(state.editingName == null ? "" : state.editingName);
            name.getStyle().set("font-weight", "600");
            body.add(name);
            AnnualSection editSection = state.section == null ? AnnualSection.ALL : state.section;
            body.add(editor(state.editingUserId, editSection, board, true, state.rerender));
            return;
        }
        body.add(filters(state));
        AnnualRosterDto roster;
        try {
            roster = annualBoardService.roster(currentUserId(), state.section, state.department, state.sort);
        } catch (ResponseStatusException ex) {
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
            boolean commander,
            Runnable refresh
    ) {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(false);
        box.setSpacing(true);
        box.setWidthFull();
        if (section == AnnualSection.PROFOSMOTR || section == AnnualSection.ALL) {
            Span med = new Span("Профосмотр: " + (board.medicalPassed() ? "Пройден" : "Не пройден"));
            med.getStyle().set("font-weight", "600");
            box.add(med);
        }
        addGroup(box, "Все врачи", board, "DOCTOR", section, userId, commander, refresh);
        addGroup(box, "Анализы", board, "ANALYSIS", section, userId, commander, refresh);
        addGroup(box, "ЦПД", board, "CPD", section, userId, commander, refresh);
        addGroup(box, "Зачёты", board, "TEST", section, userId, commander, refresh);
        addGroup(box, "Психолог", board, "PSYCHOLOGIST", section, userId, commander, refresh);
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
            Runnable refresh
    ) {
        if (!sectionShows(section, group)) {
            return;
        }
        List<AnnualItemDto> items = board.items().stream().filter(item -> group.equals(item.group())).toList();
        if (items.isEmpty()) {
            return;
        }
        Span head = new Span(title);
        head.getStyle().set("font-weight", "600");
        box.add(head);
        for (AnnualItemDto item : items) {
            box.add(itemRow(userId, item, commander, refresh));
        }
    }

    private com.vaadin.flow.component.Component itemRow(UUID userId, AnnualItemDto item, boolean commander, Runnable refresh) {
        VerticalLayout row = new VerticalLayout();
        row.setPadding(false);
        row.setSpacing(false);
        row.setWidthFull();
        HorizontalLayout line = new HorizontalLayout();
        line.setAlignItems(HorizontalLayout.Alignment.CENTER);
        line.setWidthFull();
        Checkbox checkbox = new Checkbox(item.title());
        checkbox.setValue(item.checked());
        checkbox.addValueChangeListener(event -> {
            if (!event.isFromClient()) {
                return;
            }
            save(userId, new AnnualPatchRequest(item.kind(), event.getValue(), null, null, null), commander, refresh);
        });
        line.add(checkbox);
        if (item.extendable()) {
            Button extend = new Button("Продлить", click ->
                    save(userId, new AnnualPatchRequest(item.kind(), null, true, null, null), commander, refresh));
            line.add(extend);
        }
        row.add(line);
        if (item.counts() && item.validUntil() != null) {
            Span until = new Span("Актуально до " + DATE.format(item.validUntil()));
            until.getStyle().set("color", "#2e7d32").set("font-size", "var(--lumo-font-size-s)");
            row.add(until);
        } else if (!item.counts()) {
            Span missing = new Span("Не пройден");
            missing.getStyle().set("color", "#c62828").set("font-size", "var(--lumo-font-size-s)");
            row.add(missing);
        }
        if (item.dateAllowed()) {
            DatePicker date = new DatePicker("Дата");
            date.setLocale(Locale.forLanguageTag("ru"));
            date.setValue(item.performedOn());
            date.addValueChangeListener(event -> {
                if (!event.isFromClient()) {
                    return;
                }
                LocalDate value = event.getValue();
                if (value == null) {
                    save(userId, new AnnualPatchRequest(item.kind(), null, null, null, true), commander, refresh);
                } else {
                    save(userId, new AnnualPatchRequest(item.kind(), null, null, value, null), commander, refresh);
                }
            });
            row.add(date);
        }
        return row;
    }

    private void save(UUID userId, AnnualPatchRequest request, boolean commander, Runnable refresh) {
        try {
            if (commander && !userId.equals(currentUserId())) {
                annualBoardService.patchByCommander(currentUserId(), userId, request);
            } else {
                annualBoardService.patchOwn(userId, request);
            }
            refresh.run();
        } catch (ResponseStatusException ex) {
            Notification.show(message(ex));
            refresh.run();
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
        department.setItems("Оба", "1-е", "2-е");
        department.setValue(state.department == null ? "Оба" : state.department + "-е");
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
        name.getStyle().set("font-weight", "600");
        card.add(name);
        for (AnnualLineDto line : person.lines()) {
            card.add(new Span(line.title() + ": " + line.text()));
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
