package com.company.vzvod.view.usercard;

import com.company.vzvod.entity.*;
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
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.accordion.JmixAccordion;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

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
    private CollectionLoader<Shift> shiftsDl;

    @ViewComponent
    private CollectionLoader<User> colleaguesDl;

    @ViewComponent
    private DataGrid<User> colleaguesDataGrid;

    @ViewComponent
    private CollectionContainer<User> colleaguesDc;

    @ViewComponent
    private H2 header;

    @ViewComponent
    JmixAccordion mainAccordion;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private VocationBalanceService vocationBalanceService;

    @Subscribe
    public void onQueryParametersChange(QueryParametersChangeEvent event) {
        List<String> params = event.getQueryParameters()
                .getParameters()
                .get("userId");

        if (params != null && !params.isEmpty()) {
            UUID id = UUID.fromString(params.get(0));
            userDl.setEntityId(id);
            userDl.load();
        }
    }

    public User getViewedUser() {
        return userDc.getItemOrNull();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        refreshUserData();
        mainAccordion.close();
    }

    @Subscribe("colleaguesDataGrid")
    public void onColleaguesDataGridItemClick(ItemClickEvent<User> event) {
        User selected = event.getItem();
        if (selected == null) {
            return;
        }

        userDl.setEntityId(selected.getId());
        userDl.load();
        refreshUserData();
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

        loadColleagues(user);
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