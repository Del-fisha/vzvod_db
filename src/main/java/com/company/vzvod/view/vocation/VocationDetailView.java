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

        refreshBalance(serviceInfo);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Vocation vocation = getEditedEntity();
        ServiceInfo serviceInfo = vocation != null ? vocation.getUserServiceInfo() : null;
        refreshBalance(serviceInfo);
    }

    private void refreshBalance(ServiceInfo serviceInfo) {
        if (serviceInfo == null) {
            vacationDaysUsedField.clear();
            vacationDaysAvailableField.clear();
            return;
        }

        var stats = vocationBalanceService.recalcAndSave(serviceInfo.getId());
        vacationDaysUsedField.setValue(stats.used());
        vacationDaysAvailableField.setValue(stats.available());
    }
}