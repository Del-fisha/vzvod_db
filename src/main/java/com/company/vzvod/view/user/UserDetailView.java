package com.company.vzvod.view.user;

import com.company.vzvod.entity.*;
import com.company.vzvod.service.UserReadService;
import com.company.vzvod.view.contacts.ContactsDetailView;
import com.company.vzvod.view.education.EducationDetailView;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.serviceinfo.ServiceInfoDetailView;
import com.company.vzvod.view.vehicle.VehicleDetailView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Route(value = "users/:id", layout = MainView.class)
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserReadService userReadService;

    @Install(to = "userDl", target = Target.DATA_LOADER)
    private User userDlLoadDelegate(LoadContext<User> loadContext) {
        UUID id = (UUID) loadContext.getId();

        userReadService.getUserCached(id);

        return dataManager.load(User.class).id(id).one();
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        User user = getEditedEntity();
        String password = user.getPassword();

        if (password != null && !password.isBlank() && !password.startsWith("{bcrypt}")) {
            user.setPassword(passwordEncoder.encode(password));
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        vehicleCreateButton.setEnabled(false);
    }


    @Subscribe("changePasswordButton")
    public void onChangePasswordButtonClick(ClickEvent<JmixButton> event) {
        User user = getEditedEntity();
        if (user == null || user.getId() == null) {
            return;
        }

        DialogWindow<ChangePasswordView> window =
                dialogWindows.view(this, ChangePasswordView.class).build();

        window.getView().setUserId(user.getId());
        window.open();
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
        User user = userDc.getItemOrNull();
        if (user == null) {
            return;
        }

        Contacts contact = user.getContactsInfo();
        if (contact == null) {
            contact = getViewData().getDataContext().create(Contacts.class);

            contact.setUser(user);

            user.setContactsInfo(contact);
        } else {
            if (contact.getUser() == null) {
                contact.setUser(user);
            }
        }

        dialogWindows.detail(this, Contacts.class)
                .withViewClass(ContactsDetailView.class)
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

    public User getViewedUser() {
        return userDc.getItemOrNull();
    }
}