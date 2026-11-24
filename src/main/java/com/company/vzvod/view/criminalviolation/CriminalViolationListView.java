package com.company.vzvod.view.criminalviolation;

import com.company.vzvod.entity.CriminalViolation;
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

@Route(value = "criminal-violations", layout = MainView.class)
@ViewController(id = "CriminalViolation.list")
@ViewDescriptor(path = "criminal-violation-list-view.xml")
@LookupComponent("criminalViolationsDataGrid")
@DialogMode(width = "64em")
public class CriminalViolationListView extends StandardListView<CriminalViolation> {

    @ViewComponent
    private CollectionLoader<CriminalViolation> criminalViolationsDl;

    @Autowired
    private DataManager dataManager;

    private ServiceInfo serviceInfo;

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        criminalViolationsDl.setParameter("serviceInfo", serviceInfo);
        criminalViolationsDl.load();
    }

    @Install(to = "criminalViolationsDl", target = Target.DATA_LOADER)
    private List<CriminalViolation> criminalViolationsDlLoadDelegate(LoadContext<CriminalViolation> loadContext) {
        return SortedListLoader.load(dataManager, loadContext, DirectoryListOrder.criminalOrder());
    }
}