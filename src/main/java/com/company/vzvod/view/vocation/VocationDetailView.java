package com.company.vzvod.view.vocation;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.entity.VocationType;
import com.company.vzvod.service.VocationBalanceService;
import com.company.vzvod.view.main.MainView;
import io.jmix.flowui.component.textfield.JmixIntegerField;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "vocations/:id", layout = MainView.class)
@ViewController(id = "Vocation.detail")
@ViewDescriptor(path = "vocation-detail-view.xml")
@EditedEntityContainer("vocationDc")
public class VocationDetailView extends StandardDetailView<Vocation> {


    @Autowired
    CurrentAuthentication currentAuthentication;

    @Autowired
    private VocationBalanceService vocationBalanceService;

    @ViewComponent
    private JmixIntegerField vacationDaysUsedField;

    @ViewComponent
    private JmixIntegerField vacationDaysAvailableField;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Vocation> event) {
        Vocation vocation = event.getEntity();
        ServiceInfo serviceInfo = vocation.getUserServiceInfo();
        if (serviceInfo == null) {
            User user = (User) currentAuthentication.getUser();
            serviceInfo = user.getServiceInfo();
            vocation.setUserServiceInfo(serviceInfo);
        }
        event.getEntity().setType(VocationType.MAIN);

        refreshBalanceDisplay(serviceInfo);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Vocation vocation = getEditedEntity();
        ServiceInfo serviceInfo = vocation != null ? vocation.getUserServiceInfo() : null;
        refreshBalanceDisplay(serviceInfo);
    }

    @Subscribe
    public void onAfterSave(AfterSaveEvent event) {
        Vocation vocation = getEditedEntity();
        ServiceInfo serviceInfo = vocation != null ? vocation.getUserServiceInfo() : null;
        refreshBalanceDisplay(serviceInfo);
    }

    /** Только отображение; сохранение счётчиков на {@link ServiceInfo} — при коммите записи отпуска ({@link com.company.vzvod.listener.VocationChangedListener}). */
    private void refreshBalanceDisplay(ServiceInfo serviceInfo) {
        if (serviceInfo == null || serviceInfo.getId() == null) {
            vacationDaysUsedField.clear();
            vacationDaysAvailableField.clear();
            return;
        }

        var stats = vocationBalanceService.calcCurrentYearStats(serviceInfo.getId(), LocalDate.now());
        vacationDaysUsedField.setValue(stats.used());
        vacationDaysAvailableField.setValue(stats.available());
    }
}