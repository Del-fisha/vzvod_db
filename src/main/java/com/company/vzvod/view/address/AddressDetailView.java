package com.company.vzvod.view.address;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.StatusOfHousing;
import com.company.vzvod.entity.TypeOfHousing;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "addresses/:id", layout = MainViewTopMenu.class)
@ViewController(id = "Address.detail")
@ViewDescriptor(path = "address-detail-view.xml")
@EditedEntityContainer("addressDc")
public class AddressDetailView extends StandardDetailView<Address> {
    @Subscribe
    public void onInitEntity(final InitEntityEvent<Address> event) {

        Address address = event.getEntity();

        address.setCity("Санкт-Петербург");
        address.setTypeOfHousing(TypeOfHousing.FLAT);
        address.setStatusOfHousing(StatusOfHousing.OWNER);
    }
}