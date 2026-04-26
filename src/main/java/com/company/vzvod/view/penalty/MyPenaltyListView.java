package com.company.vzvod.view.penalty;

import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shared.CurrentUserServiceInfoLoader;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ServiceInfo serviceInfo = currentUserServiceInfoLoader.loadCurrentUserServiceInfo();
        penaltiesDl.setParameter("serviceInfo", serviceInfo);
        penaltiesDl.load();
    }

    @Subscribe(id = "penaltiesDl", target = Target.DATA_LOADER)
    public void onPenaltiesDlPostLoad(CollectionLoader.PostLoadEvent<Penalty> event) {
        List<Penalty> toSave = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (Penalty p : event.getLoadedEntities()) {
            if (p != null && p.autoCompleteIfExpired(now)) {
                toSave.add(p);
            }
        }

        if (!toSave.isEmpty()) {
            dataManager.save(new SaveContext().saving(toSave));
        }
    }
}

