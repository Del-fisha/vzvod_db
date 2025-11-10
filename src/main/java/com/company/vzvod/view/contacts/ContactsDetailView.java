package com.company.vzvod.view.contacts;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.address.AddressDetailView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "contactses/:id", layout = MainViewTopMenu.class)
@ViewController(id = "Contacts.detail")
@ViewDescriptor(path = "contacts-detail-view.xml")
@EditedEntityContainer("contactsDc")
public class ContactsDetailView extends StandardDetailView<Contacts> {

    @ViewComponent
    private InstanceContainer<Contacts> contactsDc;

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private DataManager dataManager;

    @Subscribe(id = "registerAddressCreateButton", subject = "clickListener")
    public void onRegisterAddressCreateButtonClick(final ClickEvent<JmixButton> event) {

        Contacts contact = contactsDc.getItem();

        if (contact == null) {
            return;
        }

        Address addressRegistration = contact.getRegistration();

        if (addressRegistration == null) {
            addressRegistration = dataManager.create(Address.class);
            contact.setRegistration(addressRegistration);
        }

        dialogWindows.detail(this, Address.class)
                .withViewClass(AddressDetailView.class)
                .editEntity(addressRegistration)
                .open();
    }

    @Subscribe(id = "habitationAddressCreateButton", subject = "clickListener")
    public void onHabitationAddressCreateButtonClick(final ClickEvent<JmixButton> event) {
        Contacts contact = contactsDc.getItem();

        if (contact == null) {
            return;
        }

        Address addressHabitation = contact.getHabitation();

        if (addressHabitation == null) {
            addressHabitation = dataManager.create(Address.class);
            contact.setHabitation(addressHabitation);
        }

        dialogWindows.detail(this, Address.class)
                .withViewClass(AddressDetailView.class)
                .editEntity(addressHabitation)
                .open();
    }



}