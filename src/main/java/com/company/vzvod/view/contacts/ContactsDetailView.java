package com.company.vzvod.view.contacts;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.address.AddressDetailView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
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

    @ViewComponent
    private JmixCheckbox sameAddressCheckbox;

    @ViewComponent
    private JmixButton habitationAddressCreateButton;

    @ViewComponent
    private JmixButton registerAddressCreateButton;


    @Subscribe
    public void onInit(final InitEvent event) {
        Contacts contact = contactsDc.getItemOrNull();

        boolean same = contact != null
                && contact.getRegistration() != null
                && contact.getRegistration() == contact.getHabitation();

        sameAddressCheckbox.setValue(same);
        applySameAddressMode(same);

        if (same && contact != null && contact.getHabitation() == null) {
            contact.setHabitation(contact.getRegistration());
        }
    }

    @Subscribe("sameAddressCheckbox")
    public void onSameAddressCheckboxComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {

        if (!event.isFromClient()) {
            return;
        }

        boolean same = Boolean.TRUE.equals(event.getValue());
        applySameAddressMode(same);

        if (!same) {
            Contacts contact = contactsDc.getItemOrNull();
            if (contact != null && contact.getHabitation() == contact.getRegistration()) {
                contact.setHabitation(null);
            }
        }
    }

    @Subscribe(id = "contactsDc", target = Target.DATA_CONTAINER)
    public void onContactsDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Contacts> event) {
        if ("registration".equals(event.getProperty())
                && Boolean.TRUE.equals(sameAddressCheckbox.getValue())) {
            Contacts c = contactsDc.getItemOrNull();
            if (c != null) {
                c.setHabitation((Address) event.getValue());
            }
        }
    }


    private void applySameAddressMode(boolean same) {
        Contacts contact = contactsDc.getItemOrNull();
        if (contact == null) {
            return;
        }
        if (same) {
            if (contact.getRegistration() != null) {
                contact.setHabitation(contact.getRegistration());
            }
            habitationAddressCreateButton.setEnabled(false);
        } else {
            habitationAddressCreateButton.setEnabled(true);
        }
    }

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