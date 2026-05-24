package com.company.vzvod.view.user;

import com.company.vzvod.aop.UserDetailViewDataLoadSupport;
import com.company.vzvod.entity.*;
import com.company.vzvod.service.UserReadService;
import com.company.vzvod.service.UserDialogSaveService;
import com.company.vzvod.view.contacts.ContactsDetailView;
import com.company.vzvod.view.education.EducationDetailView;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.serviceinfo.ServiceInfoDetailView;
import com.company.vzvod.view.vehicle.VehicleListView;
import com.vaadin.flow.component.ClickEvent;
import com.company.vzvod.security.UiAccessService;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.LoadContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
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

    @ViewComponent
    private DataContext dataContext;

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private JmixButton vehicleCreateButton;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Notifications notifications;

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private UserReadService userReadService;

    @Autowired
    private UserDialogSaveService userDialogSaveService;

    @ViewComponent
    private JmixButton changePasswordButton;

    @Autowired
    private UiAccessService uiAccessService;

    @ViewComponent
    private TypedTextField<String> lastNameField;
    @ViewComponent
    private TypedTextField<String> firstNameField;
    @ViewComponent
    private TypedTextField<String> patronymicField;
    @ViewComponent
    private TypedDatePicker<LocalDate> dateOfBirthField;
    @ViewComponent
    private JmixComboBox<ArmyService> armyServiceField;
    @ViewComponent
    private JmixComboBox<Gender> genderField;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<User> event) {
        User user = event.getEntity();

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword("0000");
        }

        if (user.getGender() == null) {
            user.setGender(Gender.MALE);
        }

        // по умолчанию: СЛУЖИЛ
        if (user.getArmyService() == null) {
            user.setArmyService(ArmyService.SERVED);
        }

        if (user.getServiceInfo() == null) {
            ServiceInfo serviceInfo = dataContext.create(ServiceInfo.class);
            serviceInfo.setUser(user);
            user.setServiceInfo(serviceInfo);

            serviceInfo.setStatus(StatusInService.ACTIVE);
            serviceInfo.setToken("TEMP");
        }
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        updateChangePasswordButtonState();
        applyFieldReadOnlyMode();
    }

    private void applyFieldReadOnlyMode() {
        if (uiAccessService.hasFullAccessRole()) {
            return;
        }
        // Мой профиль: видит, но изменить не может
        lastNameField.setReadOnly(true);
        firstNameField.setReadOnly(true);
        patronymicField.setReadOnly(true);
        dateOfBirthField.setReadOnly(true);
        armyServiceField.setReadOnly(true);
        genderField.setReadOnly(true);
    }

    private void updateChangePasswordButtonState() {
        User user = getEditedEntity();
        boolean existingUser = user != null && !entityStates.isNew(user);
        changePasswordButton.setEnabled(existingUser);
    }

    @Install(to = "userDl", target = Target.DATA_LOADER)
    private User userDlLoadDelegate(LoadContext<User> loadContext) {
        return UserDetailViewDataLoadSupport.load(loadContext);
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        // This view uses detail_saveClose action.
        // We persist via DataManager directly to guarantee UPDATE semantics in case
        // the User was already saved by nested dialogs (ServiceInfo etc), but this view still holds
        // a "new" instance with an existing id (otherwise: USER__pkey).
        event.preventSave();

        User user = getEditedEntity();
        String password = user.getPassword();

        if (password != null && !password.isBlank() && !password.startsWith("{bcrypt}")) {
            user.setPassword(passwordEncoder.encode(password));
        }

        User saved = userDialogSaveService.saveFromDialog(user);
        getViewData().getDataContext().clear();
        userDc.setItem(saved);
        clearChanges();
        event.resume(close(StandardOutcome.SAVE));
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        vehicleCreateButton.setEnabled(isUserPersisted());
        updateChangePasswordButtonState();
    }

    private boolean isUserPersisted() {
        User user = getEditedEntity();
        return user != null && !entityStates.isNew(user) && user.getId() != null;
    }


    @Subscribe
    public void onAfterSave(final AfterSaveEvent event) {
        updateChangePasswordButtonState();
    }

    @Subscribe("changePasswordButton")
    public void onChangePasswordButtonClick(ClickEvent<JmixButton> event) {
        User user = getEditedEntity();
        if (user == null || entityStates.isNew(user)) {
            return;
        }

        DialogWindow<ChangePasswordView> window =
                dialogWindows.view(this, ChangePasswordView.class).build();

        window.getView().setUser(user);
        window.open();
    }

    @Subscribe(id = "serviceInfoCreateButton", subject = "clickListener")
    public void onServiceInfoCreateButtonClick1(final ClickEvent<JmixButton> event) {
        User user = userDc.getItem();
        if (user == null) {
            return;
        }

        boolean userPersisted = !entityStates.isNew(user) && user.getId() != null;

        ServiceInfo serviceInfo = user.getServiceInfo();
        if (serviceInfo == null && userPersisted) {
            serviceInfo = dataManager.load(ServiceInfo.class)
                    .query("select si from ServiceInfo si where si.user.id = :uid")
                    .parameter("uid", user.getId())
                    .optional()
                    .orElse(null);
            if (serviceInfo != null) {
                user.setServiceInfo(serviceInfo);
            }
        }
        if (serviceInfo == null) {
            // For persisted User we want ServiceInfo to save in its own dialog (DB),
            // so avoid creating it inside parent's DataContext.
            serviceInfo = userPersisted
                    ? dataManager.create(ServiceInfo.class)
                    : getViewData().getDataContext().create(ServiceInfo.class);
            serviceInfo.setStatus(StatusInService.ACTIVE);
            serviceInfo.setUser(user);
            user.setServiceInfo(serviceInfo);
        }

        DialogWindow<ServiceInfoDetailView> window = userPersisted
                ? dialogWindows.detail(this, ServiceInfo.class)
                .withViewClass(ServiceInfoDetailView.class)
                .editEntity(serviceInfo)
                .build()
                : dialogWindows.detail(this, ServiceInfo.class)
                .withViewClass(ServiceInfoDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(serviceInfo)
                .build();

        window.addAfterCloseListener(closeEvent -> {
            if (!closeEvent.closedWith(StandardOutcome.SAVE)) {
                return;
            }

            ServiceInfo savedServiceInfo = window.getView().getEditedEntity();

            // Always use fresh persisted instances from DB (never instances from dialog DataContext),
            // otherwise the parent view may try to INSERT again (e.g. USER__pkey / PK_ID_CARD).
            ServiceInfo persistedServiceInfo = dataManager.load(ServiceInfo.class)
                    .id(savedServiceInfo.getId())
                    .one();

            // If ServiceInfo dialog saved a NEW user (required for FK), the parent UserDetailView still
            // holds a "new" User instance in its DataContext. Replace it with the persisted one.
            if (!userPersisted) {
                User persistedUser = dataManager.load(User.class)
                        .id(persistedServiceInfo.getUser().getId())
                        .one();
                getViewData().getDataContext().clear();
                userDc.setItem(persistedUser);
                clearChanges();
                return;
            }

            user.setServiceInfo(persistedServiceInfo);

            User persistedUser = dataManager.load(User.class)
                    .id(user.getId())
                    .one();
            persistedUser.setServiceInfo(persistedServiceInfo);
            dataManager.save(persistedUser);
        });

        window.open();
    }

    @Subscribe(id = "contactsCreateButton", subject = "clickListener")
    public void onContactsCreateButtonClick(final ClickEvent<JmixButton> event) {
        User user = userDc.getItemOrNull();
        if (user == null) {
            return;
        }

        boolean userPersisted = !entityStates.isNew(user) && user.getId() != null;

        Contacts contact = user.getContactsInfo();
        if (contact == null) {
            // For persisted User we save Contacts in its own dialog (DB),
            // so we must NOT create Contacts inside the parent's DataContext.
            contact = userPersisted
                    ? dataManager.create(Contacts.class)
                    : getViewData().getDataContext().create(Contacts.class);
            contact.setUser(user);
            user.setContactsInfo(contact);
        } else {
            if (contact.getUser() == null) {
                contact.setUser(user);
            }
        }

        DialogWindow<ContactsDetailView> window = userPersisted
                ? dialogWindows.detail(this, Contacts.class)
                .withViewClass(ContactsDetailView.class)
                .editEntity(contact)
                .build()
                : dialogWindows.detail(this, Contacts.class)
                .withViewClass(ContactsDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(contact)
                .build();

        if (userPersisted) {
            window.addAfterCloseListener(closeEvent -> {
                if (!closeEvent.closedWith(StandardOutcome.SAVE)) {
                    return;
                }

                Contacts savedContacts = window.getView().getEditedEntity();
                // Important: take a fresh persisted graph from DB, not instances from the dialog DataContext.
                // Otherwise nested compositions (e.g. Address) may be treated as "new" and re-inserted.
                Contacts persistedContacts = dataManager.load(Contacts.class)
                        .id(savedContacts.getId())
                        .one();
                user.setContactsInfo(persistedContacts);

                User persistedUser = dataManager.load(User.class)
                        .id(user.getId())
                        .one();
                persistedUser.setContactsInfo(persistedContacts);
                dataManager.save(persistedUser);
            });
        }

        window.open();
    }


    @Subscribe(id = "educationCreateButton", subject = "clickListener")
    public void onEducationCreateButtonClick(final ClickEvent<JmixButton> event) {
        User user = userDc.getItem();
        if (user == null) {
            return;
        }

        boolean userPersisted = !entityStates.isNew(user) && user.getId() != null;

        Education education = user.getEducation();
        if (education == null) {
            education = userPersisted
                    ? dataManager.create(Education.class)
                    : getViewData().getDataContext().create(Education.class);
            user.setEducation(education);
        }

        DialogWindow<EducationDetailView> window = userPersisted
                ? dialogWindows.detail(this, Education.class)
                .withViewClass(EducationDetailView.class)
                .editEntity(education)
                .build()
                : dialogWindows.detail(this, Education.class)
                .withViewClass(EducationDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(education)
                .build();

        if (userPersisted) {
            window.addAfterCloseListener(closeEvent -> {
                if (!closeEvent.closedWith(StandardOutcome.SAVE)) {
                    return;
                }

                Education savedEducation = window.getView().getEditedEntity();
                Education persistedEducation = dataManager.load(Education.class)
                        .id(savedEducation.getId())
                        .one();
                user.setEducation(persistedEducation);

                User persistedUser = dataManager.load(User.class)
                        .id(user.getId())
                        .one();
                persistedUser.setEducation(persistedEducation);
                dataManager.save(persistedUser);
            });
        }

        window.open();
    }

    @Subscribe(id = "vehicleCreateButton", subject = "clickListener")
    public void onVehicleCreateButtonClick(final ClickEvent<JmixButton> event) {
        User user = userDc.getItemOrNull();
        if (user == null || !isUserPersisted()) {
            return;
        }

        DialogWindow<VehicleListView> window = dialogWindows.view(this, VehicleListView.class).build();
        window.getView().setUser(user);
        window.open();
    }

    public User getViewedUser() {
        return userDc.getItemOrNull();
    }
}