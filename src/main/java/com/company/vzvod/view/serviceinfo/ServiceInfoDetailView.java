package com.company.vzvod.view.serviceinfo;

import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.view.idcard.IdCardDetailView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "service-infoes/:id", layout = MainViewTopMenu.class)
@ViewController(id = "ServiceInfo.detail")
@ViewDescriptor(path = "service-info-detail-view.xml")
@EditedEntityContainer("serviceInfoDc")
public class ServiceInfoDetailView extends StandardDetailView<ServiceInfo> {

    @ViewComponent
    private InstanceContainer<ServiceInfo> serviceInfoDc;

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private DataManager dataManager;

    @Subscribe(id = "idCardCreateButton", subject = "clickListener")
    public void onIdCardCreateButtonClick(final ClickEvent<JmixButton> event) {

        ServiceInfo serviceInfo = serviceInfoDc.getItem();
        if (serviceInfo == null) {
            return;
        }


        IdCard idCard = serviceInfo.getIdCard();
        if (idCard == null) {
            idCard = dataManager.create(IdCard.class);
            serviceInfo.setIdCard(idCard);

        dialogWindows.detail(this, IdCard.class)
                .withViewClass(IdCardDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(idCard)
                .open();
    }
    }
}