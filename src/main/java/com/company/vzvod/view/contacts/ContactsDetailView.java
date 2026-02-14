package com.company.vzvod.view.contacts;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.view.address.AddressDetailView;
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

@Route(value = "contactses/:id", layout = MainViewTopMenu.class)
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

        // Проверяем, совпадают ли данные адресов
        boolean same = contact != null
                && contact.getRegistration() != null
                && contact.getHabitation() != null
                && addressesAreEqual(contact.getRegistration(), contact.getHabitation());

        sameAddressCheckbox.setValue(same);
        applySameAddressMode(same);
    }

    // Вспомогательный метод для сравнения адресов по содержимому
    private boolean addressesAreEqual(Address addr1, Address addr2) {
        if (addr1 == null || addr2 == null) {
            return false;
        }

        // Сравниваем все поля
        return Objects.equals(addr1.getIndex(), addr2.getIndex())
                && Objects.equals(addr1.getCity(), addr2.getCity())
                && Objects.equals(addr1.getStreet(), addr2.getStreet())
                && Objects.equals(addr1.getHouseNumber(), addr2.getHouseNumber())
                && Objects.equals(addr1.getBody(), addr2.getBody())
                && Objects.equals(addr1.getFlat(), addr2.getFlat())
                && Objects.equals(addr1.getTypeOfHousing(), addr2.getTypeOfHousing())
                && Objects.equals(addr1.getStatusOfHousing(), addr2.getStatusOfHousing());
    }


    @Subscribe("sameAddressCheckbox")
    public void onSameAddressCheckboxComponentValueChange(
            AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {

        if (!event.isFromClient()) { // чтобы инициализация из кода не запускала логику
            return;
        }

        boolean same = Boolean.TRUE.equals(event.getValue());
        Contacts contact = contactsDc.getItemOrNull();
        if (contact == null) {
            return;
        }

        if (same) {
            // Копируем текущий адрес регистрации в адрес проживания
            Address reg = contact.getRegistration();
            if (reg != null) {
                contact.setHabitation(copyAddress(reg));
            } else {
                // при желании тут можно показать уведомление, что регистрация пустая
                contact.setHabitation(null);
            }
            habitationAddressCreateButton.setEnabled(false);
        } else {
            // Отключили режим "одинаковые адреса" -> чистим адрес проживания и даём редактировать
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
        Address newReg = (Address) event.getValue(); // новое значение registration

        if (contact != null) {
            contact.setHabitation(newReg != null ? copyAddress(newReg) : null);
        }
    }


    private void applySameAddressMode(boolean same) {
        Contacts contact = contactsDc.getItemOrNull();
        if (contact == null) {
            return;
        }

        if (same) {
            // Если регистрации существует, создаём копию для проживания
            if (contact.getRegistration() != null) {
                // Создаём копию адреса регистрации
                Address registrationCopy = copyAddress(contact.getRegistration());
                contact.setHabitation(registrationCopy);
            }
            // Отключаем кнопку, т.к. адрес проживания связан с адресом регистрации
            habitationAddressCreateButton.setEnabled(false);
        } else {
            // При выключенном чекбоксе включаем кнопку редактирования адреса проживания
            habitationAddressCreateButton.setEnabled(true);
        }
    }


    // регистр. адрес
    @Subscribe(id = "registerAddressCreateButton", subject = "clickListener")
    public void onRegisterAddressCreateButtonClick(final ClickEvent<JmixButton> event) {
        Contacts contact = contactsDc.getItem();
        if (contact == null) return;
        Address addressRegistration = contact.getRegistration();
        if (addressRegistration == null) {
            addressRegistration = dataManager.create(Address.class);
            contact.setRegistration(addressRegistration);
        }
        dialogWindows.detail(this, Address.class)
                .withViewClass(AddressDetailView.class)
                .withParentDataContext(getViewData().getDataContext())   // добавили общий DataContext
                .editEntity(addressRegistration)
                .open();
    }

    // адрес проживания
    @Subscribe(id = "habitationAddressCreateButton", subject = "clickListener")
    public void onHabitationAddressCreateButtonClick(final ClickEvent<JmixButton> event) {
        Contacts contact = contactsDc.getItem();
        if (contact == null) return;
        Address addressHabitation = contact.getHabitation();
        if (addressHabitation == null) {
            addressHabitation = dataManager.create(Address.class);
            contact.setHabitation(addressHabitation);
        }
        dialogWindows.detail(this, Address.class)
                .withViewClass(AddressDetailView.class)
                .withParentDataContext(getViewData().getDataContext())   // добавили общий DataContext
                .editEntity(addressHabitation)
                .open();
    }


    private Address copyAddress(Address source) {
        if (source == null) {
            return null;
        }

        // Создаём новый Address с помощью dataManager
        // dataManager.create() автоматически генерирует новый UUID
        Address copy = dataManager.create(Address.class);

        // Копируем все поля из source в copy
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

        sameAddressCheckbox.setValue(same); // isFromClient=false, логика копирования не запустится
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