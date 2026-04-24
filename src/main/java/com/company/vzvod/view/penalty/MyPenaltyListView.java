package com.company.vzvod.view.penalty;

import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shared.CurrentUserServiceInfoLoader;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "my-penalties", layout = MainView.class)
@ViewController(id = "MyPenalty.list")
@ViewDescriptor(path = "my-penalty-list-view.xml")
@LookupComponent("penaltiesDataGrid")
@DialogMode(width = "64em")
public class MyPenaltyListView extends StandardListView<Penalty> {

    @ViewComponent
    private CollectionLoader<Penalty> penaltiesDl;

    @Autowired
    private CurrentUserServiceInfoLoader currentUserServiceInfoLoader;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ServiceInfo serviceInfo = currentUserServiceInfoLoader.loadCurrentUserServiceInfo();
        penaltiesDl.setParameter("serviceInfo", serviceInfo);
        penaltiesDl.load();
    }
}

