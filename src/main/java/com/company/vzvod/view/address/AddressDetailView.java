package com.company.vzvod.view.address;

import com.company.vzvod.entity.Address;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "addresses/:id", layout = MainViewTopMenu.class)
@ViewController(id = "Address.detail")
@ViewDescriptor(path = "address-detail-view.xml")
@EditedEntityContainer("addressDc")
public class AddressDetailView extends StandardDetailView<Address> {
}