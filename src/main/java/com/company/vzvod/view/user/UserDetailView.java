package com.company.vzvod.view.user;

import com.company.vzvod.entity.*;
import com.company.vzvod.view.contacts.ContactsDetailView;
import com.company.vzvod.view.education.EducationDetailView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.company.vzvod.view.serviceinfo.ServiceInfoDetailView;
import com.company.vzvod.view.vehicle.VehicleDetailView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Route(value = "users/:id", layout = MainViewTopMenu.class)
@ViewController(id = "User.detail")
@ViewDescriptor(path = "user-detail-view.xml")
@EditedEntityContainer("userDc")
public class UserDetailView extends StandardDetailView<User> {

    @ViewComponent
    private InstanceContainer<User> userDc;

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private JmixButton vehicleCreateButton;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        // Делаем кнопку неактивной сразу при открытии экрана
        vehicleCreateButton.setEnabled(false);
    }





    @Subscribe(id = "serviceInfoCreateButton", subject = "clickListener")
    public void onServiceInfoCreateButtonClick1(final ClickEvent<JmixButton> event) {
        User user = userDc.getItem();
        if (user == null) {
            return;
        }

        ServiceInfo serviceInfo = user.getServiceInfo();
        if (serviceInfo == null) {
            serviceInfo = dataManager.create(ServiceInfo.class);
            serviceInfo.setUser(user);
            user.setServiceInfo(serviceInfo);
        }

        dialogWindows.detail(this, ServiceInfo.class)
                .withViewClass(ServiceInfoDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(serviceInfo)
                .open();
    }

    @Subscribe(id = "contactsCreateButton", subject = "clickListener")
    public void onContactsCreateButtonClick(final ClickEvent<JmixButton> event) {

        User user = userDc.getItem();
        if (user == null) {
            return;
        }
        Contacts contact = user.getContactsInfo();

        if (contact == null) {
            contact = dataManager.create(Contacts.class);
            user.setContactsInfo(contact);
        }

        dialogWindows.detail(this, Contacts.class)
                .withViewClass(ContactsDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(contact)
                .open();
    }

    @Subscribe(id = "educationCreateButton", subject = "clickListener")
    public void onEducationCreateButtonClick(final ClickEvent<JmixButton> event) {
        User user = userDc.getItem();
        if (user == null) {
            return;
        }

        Education education = user.getEducation();
        if (education == null) {
            education = dataManager.create(Education.class);
            user.setEducation(education);
        }

        dialogWindows.detail(this, Education.class)
                .withViewClass(EducationDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(education)
                .open();
    }

    @Subscribe(id = "vehicleCreateButton", subject = "clickListener")
    public void onVehicleCreateButtonClick(final ClickEvent<JmixButton> event) {
        User user = userDc.getItem();
        if (user == null) {
            return;
        }

        Vehicle vehicle = user.getVehicleInfo().get(0);
        if (vehicle == null) {
            vehicle = dataManager.create(Vehicle.class);
            // ToDo Разработать выдачу листа машин
            List<Vehicle> vehicles = new ArrayList<>();
            vehicles.add(vehicle);
            user.setVehicleInfo(vehicles);
        }

        dialogWindows.detail(this, Vehicle.class)
                .withViewClass(VehicleDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(vehicle)
                .open();

    }
}