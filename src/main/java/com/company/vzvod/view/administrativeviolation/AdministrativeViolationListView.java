package com.company.vzvod.view.administrativeviolation;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.util.DirectoryListOrder;
import com.company.vzvod.util.SortedListLoader;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "administrative-violations", layout = MainView.class)
@ViewController(id = "AdministrativeViolation.list")
@ViewDescriptor(path = "administrative-violation-list-view.xml")
@LookupComponent("administrativeViolationsDataGrid")
@DialogMode(width = "64em")
public class AdministrativeViolationListView extends StandardListView<AdministrativeViolation> {

    @ViewComponent
    private CollectionLoader<AdministrativeViolation> administrativeViolationsDl;

    @Autowired
    private DataManager dataManager;

    private ServiceInfo serviceInfo;

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        administrativeViolationsDl.setParameter("serviceInfo", serviceInfo);
        administrativeViolationsDl.load();
    }

    @Install(to = "administrativeViolationsDl", target = Target.DATA_LOADER)
    private List<AdministrativeViolation> administrativeViolationsDlLoadDelegate(
            LoadContext<AdministrativeViolation> loadContext) {
        return SortedListLoader.load(dataManager, loadContext, DirectoryListOrder.administrativeOrder());
    }
}