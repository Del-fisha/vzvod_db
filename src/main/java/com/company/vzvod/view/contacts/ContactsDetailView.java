package com.company.vzvod.view.contacts;

import com.company.vzvod.entity.Contacts;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "contactses/:id", layout = MainViewTopMenu.class)
@ViewController(id = "Contacts.detail")
@ViewDescriptor(path = "contacts-detail-view.xml")
@EditedEntityContainer("contactsDc")
public class ContactsDetailView extends StandardDetailView<Contacts> {
}