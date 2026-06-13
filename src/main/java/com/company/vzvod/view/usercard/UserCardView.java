package com.company.vzvod.view.usercard;

import com.company.vzvod.entity.*;
import com.company.vzvod.service.PenaltyExpirationService;
import com.company.vzvod.service.VocationBalanceService;
import com.company.vzvod.view.shiftblank.ShiftBlankView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.company.vzvod.view.main.MainView;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.accordion.JmixAccordion;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.model.KeyValueCollectionContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Route(value = "user-card", layout = MainView.class)
@ViewController("UserCardView")
@ViewDescriptor("user-card-view.xml")
public class UserCardView extends StandardView {

    @ViewComponent
    private InstanceContainer<User> userDc;

    @ViewComponent
    private InstanceLoader<User> userDl;

    @ViewComponent
    private InstanceContainer<ServiceInfo> serviceInfoDc;

    @ViewComponent
    private InstanceContainer<Contacts> contactsDc;

    @ViewComponent
    private InstanceContainer<Address> regAddressDc;

    @ViewComponent
    private InstanceContainer<Address> habAddressDc;

    @ViewComponent
    private InstanceContainer<IdCard> idCardDc;

    @ViewComponent
    private CollectionLoader<Shift> shiftsDl;

    @ViewComponent
    private CollectionLoader<Incentive> incentivesDl;

    @ViewComponent
    private CollectionLoader<Penalty> penaltiesDl;

    @ViewComponent
    private CollectionLoader<Vehicle> vehiclesDl;

    @ViewComponent
    private CollectionLoader<User> colleaguesDl;

    @ViewComponent
    private DataGrid<User> colleaguesDataGrid;

    @ViewComponent
    private DataGrid<Incentive> incentivesDataGrid;

    @ViewComponent
    private DataGrid<Penalty> penaltiesDataGrid;

    @ViewComponent
    private DataGrid<Vehicle> vehiclesDataGrid;

    @ViewComponent
    private DataGrid<KeyValueEntity> workResultsDataGrid;

    @ViewComponent
    private DataGrid<Shift> shiftsDataGrid;

    @ViewComponent
    private CollectionContainer<User> colleaguesDc;

    @ViewComponent
    private KeyValueCollectionContainer workResultsDc;

    @ViewComponent
    private H2 header;

    @ViewComponent
    JmixAccordion mainAccordion;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private VocationBalanceService vocationBalanceService;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Metadata metadata;

    @Autowired
    private PenaltyExpirationService penaltyExpirationService;

    @Subscribe
    public void onQueryParametersChange(QueryParametersChangeEvent event) {
        List<String> params = event.getQueryParameters()
                .getParameters()
                .get("userId");

        if (params != null && !params.isEmpty()) {
            UUID id = UUID.fromString(params.get(0));
            loadUserIntoView(id);
        }
    }

    /**
     * Загружает пользователя с fetch plan карточки. Без этого при выборе из {@code colleaguesDc}
     * в persistence context остаётся урезанный инстанс (без dateOfBirth, contactsInfo и т.д.).
     */
    private void loadUserIntoView(UUID userId) {
        User user = dataManager.load(User.class)
                .id(userId)
                .fetchPlan(userDl.getFetchPlan())
                .one();
        userDl.setEntityId(userId);
        userDc.setItem(user);
        refreshUserData();
    }

    public User getViewedUser() {
        return userDc.getItemOrNull();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        refreshUserData();
        mainAccordion.close();
        applyAccordionContentSizing();
    }

    /**
     * После полной инициализации гридов заголовки гарантированно есть — убираем для «линейных» списков.
     */
    @Subscribe
    public void onReady(ReadyEvent event) {
        applyAccordionContentSizing();
    }

    @Subscribe(id = "penaltiesDl", target = Target.DATA_LOADER)
    public void onPenaltiesDlPostLoad(CollectionLoader.PostLoadEvent<Penalty> event) {
        penaltyExpirationService.saveChanged(event.getLoadedEntities(), LocalDate.now());
    }

    @Subscribe("colleaguesDataGrid")
    public void onColleaguesDataGridItemClick(ItemClickEvent<User> event) {
        User selected = event.getItem();
        if (selected == null || selected.getId() == null) {
            return;
        }
        loadUserIntoView(selected.getId());
    }

    /**
     * Открытие редактора смены по двойному клику в нижнем гриде.
     */
    @Subscribe("shiftsDataGrid")
    public void onShiftsDataGridItemDoubleClick(ItemDoubleClickEvent<Shift> event) {
        Shift shift = event.getItem();
        if (shift == null) {
            return;
        }

        viewNavigators.view(this, ShiftBlankView.class)
                .withQueryParameters(
                        QueryParameters.of("shiftId", shift.getId().toString())
                )
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe("backButton")
    public void onBackButtonClick(ClickEvent<JmixButton> event) {
        // Uses browser history; works with .withBackwardNavigation(true)
        UI.getCurrent().getPage().getHistory().back();
    }

    private void refreshUserData() {
        User user = userDc.getItemOrNull();
        if (user == null) {
            return;
        }

        header.setText(user.getDisplayName());

        ServiceInfo serviceInfo = user.getServiceInfo();
        if (serviceInfo != null && serviceInfo.getId() != null) {
            var stats = vocationBalanceService.recalcAndSave(serviceInfo.getId());
            // локально обновим отображаемый инстанс (в fetchPlan эти поля загружены, поэтому безопасно)
            serviceInfo.setVacationDaysEntitled(stats.entitled());
            serviceInfo.setVacationDaysAvailable(stats.available());
        }
        serviceInfoDc.setItem(serviceInfo);
        idCardDc.setItem(serviceInfo != null ? serviceInfo.getIdCard() : null);

        Contacts contacts = user.getContactsInfo();
        contactsDc.setItem(contacts);

        if (contacts != null) {
            regAddressDc.setItem(contacts.getRegistration());
            habAddressDc.setItem(contacts.getHabitation());
        } else {
            regAddressDc.setItem(null);
            habAddressDc.setItem(null);
        }

        shiftsDl.setParameter("user", user);
        shiftsDl.load();

        incentivesDl.setParameter("serviceInfo", serviceInfo);
        incentivesDl.load();

        penaltiesDl.setParameter("serviceInfo", serviceInfo);
        penaltiesDl.load();

        vehiclesDl.setParameter("user", user);
        vehiclesDl.load();

        workResultsDc.setItems(buildWorkResults(serviceInfo));

        loadColleagues(user);
        applyAccordionContentSizing();
    }

    /**
     * Высота гридов в аккордеонах по фактическому числу строк (пустой список — минимум, без «колодца»).
     */
    private void applyAccordionContentSizing() {
        compactInlineListGrid(incentivesDataGrid);
        compactInlineListGrid(penaltiesDataGrid);
        compactInlineListGrid(vehiclesDataGrid);
        compactHeightToRows(workResultsDataGrid);
        compactHeightToRows(shiftsDataGrid);
    }

    /**
     * Таблицы в аккордеоне только с данными, без строки названий столбцов.
     * Атрибут {@code hide-header-row} на разметке Flow не попадает на {@code vaadin-grid} —
     * используется серверный API {@link com.vaadin.flow.component.grid.Grid#removeAllHeaderRows()}.
     */
    private void compactInlineListGrid(DataGrid<?> grid) {
        compactHeightToRows(grid);
        stripGridHeaderRow(grid);
    }

    private void stripGridHeaderRow(DataGrid<?> grid) {
        if (grid == null) {
            return;
        }
        if (grid.getHeaderRows().isEmpty()) {
            return;
        }
        grid.removeAllHeaderRows();
    }

    private void compactHeightToRows(DataGrid<?> grid) {
        if (grid == null) {
            return;
        }
        grid.setAllRowsVisible(true);
    }

    private List<KeyValueEntity> buildWorkResults(ServiceInfo serviceInfo) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate yearStart = now.withDayOfYear(1);

        int adminMonth = serviceInfo == null ? 0 : countAdministrative(serviceInfo, monthStart);
        int adminYear = serviceInfo == null ? 0 : countAdministrative(serviceInfo, yearStart);
        int adminTotal = serviceInfo == null ? 0 : countAdministrative(serviceInfo, null);

        int criminalMonth = serviceInfo == null ? 0 : countCriminal(serviceInfo, monthStart);
        int criminalYear = serviceInfo == null ? 0 : countCriminal(serviceInfo, yearStart);
        int criminalTotal = serviceInfo == null ? 0 : countCriminal(serviceInfo, null);

        int ibdrMonth = serviceInfo == null ? 0 : sumIbdr(serviceInfo, monthStart);
        int ibdrYear = serviceInfo == null ? 0 : sumIbdr(serviceInfo, yearStart);
        int ibdrTotal = serviceInfo == null ? 0 : sumIbdr(serviceInfo, null);

        return List.of(
                kvRow("АП", adminMonth, adminYear, adminTotal),
                kvRow("УП", criminalMonth, criminalYear, criminalTotal),
                kvRow("ИБДР", ibdrMonth, ibdrYear, ibdrTotal)
        );
    }

    private KeyValueEntity kvRow(String category, int month, int year, int total) {
        KeyValueEntity e = metadata.create(KeyValueEntity.class);
        e.setValue("category", category);
        e.setValue("month", month);
        e.setValue("year", year);
        e.setValue("total", total);
        return e;
    }

    private int countAdministrative(ServiceInfo serviceInfo, LocalDate from) {
        String q = """
                select count(v)
                from AdministrativeViolation v
                join v.shift s
                join s.units si
                where si = :serviceInfo
                """;
        if (from != null) {
            q += " and s.date >= :from";
        }

        var loader = dataManager.loadValue(q, Long.class)
                .parameter("serviceInfo", serviceInfo);
        if (from != null) {
            loader.parameter("from", from);
        }
        Long value = loader.one();
        return value == null ? 0 : value.intValue();
    }

    private int countCriminal(ServiceInfo serviceInfo, LocalDate from) {
        String q = """
                select count(v)
                from CriminalViolation v
                join v.shift s
                join s.units si
                where si = :serviceInfo
                """;
        if (from != null) {
            q += " and s.date >= :from";
        }

        var loader = dataManager.loadValue(q, Long.class)
                .parameter("serviceInfo", serviceInfo);
        if (from != null) {
            loader.parameter("from", from);
        }
        Long value = loader.one();
        return value == null ? 0 : value.intValue();
    }

    private int sumIbdr(ServiceInfo serviceInfo, LocalDate from) {
        String q = """
                select coalesce(sum(coalesce(s.ibdWithMigrant, 0) + coalesce(s.ibdWithoutMigrant, 0)), 0)
                from Shift s
                join s.units si
                where si = :serviceInfo
                """;
        if (from != null) {
            q += " and s.date >= :from";
        }

        var loader = dataManager.loadValue(q, Long.class)
                .parameter("serviceInfo", serviceInfo);
        if (from != null) {
            loader.parameter("from", from);
        }
        Long value = loader.one();
        return value == null ? 0 : value.intValue();
    }

    private void loadColleagues(User user) {
        ServiceInfo si = user.getServiceInfo();
        Department department = si != null ? si.getDepartment() : null;

        colleaguesDl.setParameter("department", department);
        colleaguesDl.load();

        if (colleaguesDataGrid != null && user.getId() != null) {
            colleaguesDc.getItems().stream()
                    .filter(u -> user.getId().equals(u.getId()))
                    .findFirst()
                    .ifPresent(colleaguesDataGrid::select);
        }
    }

    @Supply(to = "colleaguesDataGrid.shortFio", subject = "renderer")
    private Renderer<User> colleaguesShortFioRenderer() {
        return new TextRenderer<>(u -> u == null ? "" : u.getShortFio());
    }
}