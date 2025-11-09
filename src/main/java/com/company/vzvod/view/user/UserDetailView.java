package com.company.vzvod.view.user;

import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.view.contacts.ContactsDetailView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.company.vzvod.view.serviceinfo.ServiceInfoDetailView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

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
}