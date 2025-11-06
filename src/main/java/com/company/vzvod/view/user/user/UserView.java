package com.company.vzvod.view.user.user;


import com.company.vzvod.entity.User;
import com.company.vzvod.view.contacts.ContactsDetailView;
import com.company.vzvod.view.education.EducationDetailView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.company.vzvod.view.serviceinfo.ServiceInfoDetailView;
import com.company.vzvod.view.user.UserDetailView;
import com.company.vzvod.view.vehicle.VehicleDetailView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "user-view", layout = MainViewTopMenu.class)
@ViewController(id = "UserView")
@ViewDescriptor(path = "user-view.xml")
public class UserView extends StandardView {

    @ViewComponent
    private InstanceContainer<User> userDc;

    @ViewComponent
    private TextField serviceInfoDisplay;

    @ViewComponent
    private TextField contactsDisplay;

    @ViewComponent
    private TextField vehiclesDisplay;

    @ViewComponent
    private TextField educationDisplay;

    @Autowired
    private ViewNavigators viewNavigators;


    @Subscribe
    public void onInit(final InitEvent event) {
        // Заполняем поля instance names
        updateDisplayFields();
    }

    private void updateDisplayFields() {
        User user = userDc.getItemOrNull();

        if (user == null) {
            return;
        }

        // Service Info
        if (user.getServiceInfo() != null) {
            serviceInfoDisplay.setValue(user.getServiceInfo().toString());
        }

        // Contacts
        if (user.getContactsInfo() != null) {
            contactsDisplay.setValue(user.getContactsInfo().toString());
        }

        // Vehicles (если OneToMany, показываем количество)
        if (user.getVehicleInfo() != null && !user.getVehicleInfo().isEmpty()) {
            vehiclesDisplay.setValue(user.getVehicleInfo().size() + " vehicles");
        }

        // Education (если существует)
        if (user.getEducation() != null) {
            educationDisplay.setValue(user.getEducation().toString());
        }
    }


    @Subscribe("serviceInfoEdit")
    public void onServiceInfoEditClick(final ClickEvent<Button> event) {
        viewNavigators.view(this, ServiceInfoDetailView.class)
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe("serviceInfoViewDetails")
    public void onServiceInfoViewDetailsClick(final ClickEvent<Button> event) {
        viewNavigators.view(this, ServiceInfoDetailView.class)
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe(id = "contactsEdit", subject = "clickListener")
    public void onContactsEditClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, ContactsDetailView.class)
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe(id = "contactsViewDetails", subject = "clickListener")
    public void onContactsViewDetailsClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, ContactsDetailView.class)
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe(id = "vehiclesEdit", subject = "clickListener")
    public void onVehiclesEditClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, VehicleDetailView.class)
                .withBackwardNavigation(true
                ).navigate();
    }

    @Subscribe(id = "vehiclesViewDetails", subject = "clickListener")
    public void onVehiclesViewDetailsClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, VehicleDetailView.class)
                .withBackwardNavigation(true
                ).navigate();
    }

    @Subscribe(id = "educationEdit", subject = "clickListener")
    public void onEducationEditClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, EducationDetailView.class)
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe(id = "educationViewDetails", subject = "clickListener")
    public void onEducationViewDetailsClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, EducationDetailView.class)
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe(id = "editBtn", subject = "clickListener")
    public void onEditBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, UserDetailView.class)
                .withBackwardNavigation(true)
                .navigate();
    }
}