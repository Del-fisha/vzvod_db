package com.company.vzvod.view.contacts;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.address.AddressDetailView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@ViewController(id = "Contacts.detail")
@ViewDescriptor(path = "contacts-detail-view.xml")
@EditedEntityContainer("contactsDc")
public class ContactsDetailView extends StandardDetailView<Contacts> {

    @ViewComponent
    private InstanceContainer<Contacts> contactsDc;

    @ViewComponent
    private JmixCheckbox sameAddressCheckbox;

    @ViewComponent
    private JmixButton habitationAddressCreateButton;

    @ViewComponent
    private TypedTextField<String> phoneNumberField;

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private UiAccessService uiAccessService;

    private boolean internalChange;


    @Subscribe
    public void onReady(ReadyEvent event) {
        if (!uiAccessService.hasFullAccessRole() && phoneNumberField != null) {
            phoneNumberField.setReadOnly(true);
        }
    }

    @Subscribe
    public void onAfterClose(View.AfterCloseEvent event) {
    }

    @Subscribe(id = "contactsDl", target = Target.DATA_LOADER)
    public void onContactsDlPostLoad(InstanceLoader.PostLoadEvent event) {
        Contacts c = contactsDc.getItemOrNull();
        boolean same = c != null
                && c.getRegistration() != null
                && c.getHabitation() != null
                && addressesAreEqual(c.getRegistration(), c.getHabitation());

        internalChange = true;
        try {
            sameAddressCheckbox.setValue(same);
            applySameMode(same);
        } finally {
            internalChange = false;
        }
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<Contacts> event) {
        internalChange = true;
        try {
            sameAddressCheckbox.setValue(false);
            applySameMode(false);
            event.getEntity().setRegistration(null);
            event.getEntity().setHabitation(null);
        } finally {
            internalChange = false;
        }
    }

    @Subscribe("sameAddressCheckbox")
    public void onSameAddressCheckboxValueChange(
            AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {

        if (internalChange) {
            return;
        }

        boolean same = Boolean.TRUE.equals(event.getValue());
        Contacts c = contactsDc.getItemOrNull();
        if (c == null) {
            applySameMode(same);
            return;
        }

        internalChange = true;
        try {
            if (same) {
                c.setHabitation(copyAddressOrNull(c.getRegistration()));
            } else {
                c.setHabitation(null);
            }
            applySameMode(same);
        } finally {
            internalChange = false;
        }
    }

    @Subscribe(id = "contactsDc", target = Target.DATA_CONTAINER)
    public void onContactsDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Contacts> event) {
        if (internalChange) {
            return;
        }
        if (!"registration".equals(event.getProperty())) {
            return;
        }
        if (!Boolean.TRUE.equals(sameAddressCheckbox.getValue())) {
            return;
        }

        Contacts c = event.getItem();

        internalChange = true;
        try {
            c.setHabitation(copyAddressOrNull(c.getRegistration()));
        } finally {
            internalChange = false;
        }
    }

    private void applySameMode(boolean same) {
        habitationAddressCreateButton.setEnabled(!same);
    }

    @Subscribe(id = "registerAddressCreateButton", subject = "clickListener")
    public void onRegisterAddressCreateButtonClick(final ClickEvent<JmixButton> event) {
        Contacts c = contactsDc.getItem();

        if (c.getRegistration() == null) {
            c.setRegistration(getViewData().getDataContext().create(Address.class));
        }

        dialogWindows.detail(this, Address.class)
                .withViewClass(AddressDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(c.getRegistration())
                .open();
    }

    @Subscribe(id = "habitationAddressCreateButton", subject = "clickListener")
    public void onHabitationAddressCreateButtonClick(final ClickEvent<JmixButton> event) {
        Contacts c = contactsDc.getItem();

        if (c.getHabitation() == null) {
            c.setHabitation(getViewData().getDataContext().create(Address.class));
        }

        dialogWindows.detail(this, Address.class)
                .withViewClass(AddressDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(c.getHabitation())
                .open();
    }

    private Address copyAddressOrNull(Address source) {
        if (source == null) {
            return null;
        }

        Address copy = getViewData().getDataContext().create(Address.class);

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

    private boolean addressesAreEqual(Address a1, Address a2) {
        if (a1 == null || a2 == null) {
            return false;
        }
        return Objects.equals(a1.getIndex(), a2.getIndex())
                && Objects.equals(a1.getCity(), a2.getCity())
                && Objects.equals(a1.getStreet(), a2.getStreet())
                && Objects.equals(a1.getHouseNumber(), a2.getHouseNumber())
                && Objects.equals(a1.getBody(), a2.getBody())
                && Objects.equals(a1.getFlat(), a2.getFlat())
                && Objects.equals(a1.getTypeOfHousing(), a2.getTypeOfHousing())
                && Objects.equals(a1.getStatusOfHousing(), a2.getStatusOfHousing());
    }
}