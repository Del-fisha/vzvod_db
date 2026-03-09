package com.company.vzvod.view.contacts;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.view.address.AddressDetailView;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "contactses/:id", layout = MainView.class)
@ViewController(id = "Contacts.detail")
@ViewDescriptor(path = "contacts-detail-view.xml")
@EditedEntityContainer("contactsDc")
public class ContactsDetailView extends StandardDetailView<Contacts> {

    @ViewComponent
    private InstanceContainer<Contacts> contactsDc;

    @Autowired
    private CurrentAuthentication currentAuthentication;

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
                && contact.getHabitation() != null
                && addressesAreEqual(contact.getRegistration(), contact.getHabitation());

        sameAddressCheckbox.setValue(same);
        applySameAddressMode(same);
    }

    private boolean addressesAreEqual(Address address1, Address address2) {
        if (address1 == null || address2 == null) {
            return false;
        }

        return Objects.equals(address1.getIndex(), address2.getIndex())
                && Objects.equals(address1.getCity(), address2.getCity())
                && Objects.equals(address1.getStreet(), address2.getStreet())
                && Objects.equals(address1.getHouseNumber(), address2.getHouseNumber())
                && Objects.equals(address1.getBody(), address2.getBody())
                && Objects.equals(address1.getFlat(), address2.getFlat())
                && Objects.equals(address1.getTypeOfHousing(), address2.getTypeOfHousing())
                && Objects.equals(address1.getStatusOfHousing(), address2.getStatusOfHousing());
    }


    @Subscribe("sameAddressCheckbox")
    public void onSameAddressCheckboxComponentValueChange(
            AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {
        boolean same = Boolean.TRUE.equals(event.getValue());
        Contacts contact = contactsDc.getItemOrNull();
        if (contact == null) {
            return;
        }

        if (same) {
            Address reg = contact.getRegistration();
            if (reg != null) {
                contact.setHabitation(copyAddress(reg));
            } else {
                contact.setHabitation(null);
            }
            habitationAddressCreateButton.setEnabled(false);
        } else {
            contact.setHabitation(null);
            habitationAddressCreateButton.setEnabled(true);
        }
    }


    @Subscribe(id = "contactsDc", target = Target.DATA_CONTAINER)
    public void onContactsDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Contacts> event) {
        if (!"registration".equals(event.getProperty())) {
            return;
        }

        if (!Boolean.TRUE.equals(sameAddressCheckbox.getValue())) {
            return;
        }

        Contacts contact = event.getItem();
        Address newReg = (Address) event.getValue();

        contact.setHabitation(newReg != null ? copyAddress(newReg) : null);
    }


    private void applySameAddressMode(boolean same) {
        Contacts contact = contactsDc.getItemOrNull();
        if (contact == null) {
            return;
        }

        if (same) {
            if (contact.getRegistration() != null) {
                Address registrationCopy = copyAddress(contact.getRegistration());
                contact.setHabitation(registrationCopy);
            }
            habitationAddressCreateButton.setEnabled(false);
        } else {
            habitationAddressCreateButton.setEnabled(true);
        }
    }

    @Subscribe(id = "registerAddressCreateButton", subject = "clickListener")
    public void onRegisterAddressCreateButtonClick(final ClickEvent<JmixButton> event) {
        Contacts contact = contactsDc.getItem();
        Address addressRegistration = contact.getRegistration();
        if (addressRegistration == null) {
            addressRegistration = dataManager.create(Address.class);
            contact.setRegistration(addressRegistration);
        }
        dialogWindows.detail(this, Address.class)
                .withViewClass(AddressDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(addressRegistration)
                .open();
    }

    @Subscribe(id = "habitationAddressCreateButton", subject = "clickListener")
    public void onHabitationAddressCreateButtonClick(final ClickEvent<JmixButton> event) {
        Contacts contact = contactsDc.getItem();
        Address addressHabitation = contact.getHabitation();
        if (addressHabitation == null) {
            addressHabitation = dataManager.create(Address.class);
            contact.setHabitation(addressHabitation);
        }
        dialogWindows.detail(this, Address.class)
                .withViewClass(AddressDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(addressHabitation)
                .open();
    }


    private Address copyAddress(Address source) {
        if (source == null) {
            return null;
        }

        Address copy = dataManager.create(Address.class);

        copy.setIndex(source.getIndex());
        copy.setCity(source.getCity());
        copy.setStreet(source.getStreet());
        copy.setHouseNumber(source.getHouseNumber());
        copy.setBody(source.getBody());
        copy.setFlat(source.getFlat());
        copy.setTypeOfHousing(source.getTypeOfHousing());
        copy.setStatusOfHousing(source.getStatusOfHousing());

        return copy;
    }


    private void initSameCheckboxFromEntity() {
        Contacts contact = contactsDc.getItemOrNull();
        if (contact == null) {
            return;
        }

        boolean same = contact.getRegistration() != null
                && contact.getHabitation() != null
                && addressesAreEqual(contact.getRegistration(), contact.getHabitation());

        sameAddressCheckbox.setValue(same);
        habitationAddressCreateButton.setEnabled(!same);
    }

    @Subscribe(id = "contactsDl", target = Target.DATA_LOADER)
    public void onContactsDlPostLoad(InstanceLoader.PostLoadEvent event) {
        initSameCheckboxFromEntity();
    }


    @Subscribe
    public void onReady(final ReadyEvent event) {
        Contacts contacts = getEditedEntity();
        User currentUser = (User) currentAuthentication.getUser();

        boolean isOwnContacts = currentUser.getContactsInfo() != null
                && currentUser.getContactsInfo().getId().equals(contacts.getId());

        sameAddressCheckbox.setReadOnly(!isOwnContacts);
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<Contacts> event) {
        sameAddressCheckbox.setValue(false);
        habitationAddressCreateButton.setEnabled(true);
    }

}