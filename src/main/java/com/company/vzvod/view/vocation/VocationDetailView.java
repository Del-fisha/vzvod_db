package com.company.vzvod.view.vocation;

import com.company.vzvod.entity.Vocation;
import com.company.vzvod.entity.VocationType;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "vocations/:id", layout = MainView.class)
@ViewController(id = "Vocation.detail")
@ViewDescriptor(path = "vocation-detail-view.xml")
@EditedEntityContainer("vocationDc")
public class VocationDetailView extends StandardDetailView<Vocation> {

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Vocation> event) {
        event.getEntity().setType(VocationType.MAIN);
    }
}