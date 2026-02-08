package com.company.vzvod.view.idcard;

import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;

@Route(value = "id-cards/:id", layout = MainViewTopMenu.class)
@ViewController(id = "IdCard.detail")
@ViewDescriptor(path = "id-card-detail-view.xml")
@EditedEntityContainer("idCardDc")
public class IdCardDetailView extends StandardDetailView<IdCard> {

    @ViewComponent
    InstanceLoader idCardDl;

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    private ServiceInfo serviceInfo;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        idCardDl.setParameter("serviceInfo", serviceInfo);
        idCardDl.load();
    }

}