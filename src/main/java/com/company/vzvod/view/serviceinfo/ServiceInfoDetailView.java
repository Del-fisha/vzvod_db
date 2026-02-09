package com.company.vzvod.view.serviceinfo;

import com.company.vzvod.entity.*;
import com.company.vzvod.view.idcard.IdCardDetailView;
import com.company.vzvod.view.incentive.IncentiveListView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.company.vzvod.view.penalty.PenaltyListView;
import com.company.vzvod.view.shift.ShiftListView;
import com.company.vzvod.view.vocation.VocationListView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "service-infoes/:id", layout = MainViewTopMenu.class)
@ViewController(id = "ServiceInfo.detail")
@ViewDescriptor(path = "service-info-detail-view.xml")
@EditedEntityContainer("serviceInfoDc")
public class ServiceInfoDetailView extends StandardDetailView<ServiceInfo> {

    @ViewComponent
    private InstanceContainer<ServiceInfo> serviceInfoDc;

    @Autowired
    private DialogWindows dialogWindows;


    @Subscribe(id = "vocationListButton", subject = "clickListener")
    public void onVocationListButtonClick(final ClickEvent<JmixButton> event) {
        ServiceInfo serviceInfo = serviceInfoDc.getItem();
        if (serviceInfo == null) {
            return;
        }

        DialogWindow<VocationListView> window = dialogWindows.view(this, VocationListView.class).build();
        window.getView().setServiceInfo(serviceInfo);

        window.open();

    }

    @Subscribe(id = "incentiveListButton", subject = "clickListener")
    public void onIncentiveListButtonClick(final ClickEvent<JmixButton> event) {
        ServiceInfo serviceInfo = serviceInfoDc.getItem();
        if (serviceInfo == null) {
            return;
        }

        DialogWindow<IncentiveListView> window = dialogWindows.view(this, IncentiveListView.class).build();
        window.getView().setServiceInfo(serviceInfo);
        window.open();
    }

    @Subscribe(id = "penaltyListButton", subject = "clickListener")
    public void onPenaltyListButtonClick(final ClickEvent<JmixButton> event) {
        ServiceInfo serviceInfo = serviceInfoDc.getItem();
        if (serviceInfo == null) {
            return;
        }

        DialogWindow<PenaltyListView> window = dialogWindows.view(this, PenaltyListView.class).build();
        window.getView().setServiceInfo(serviceInfo);
        window.open();
    }

    @Subscribe(id = "shiftListButton", subject = "clickListener")
    public void onShiftListButtonClick(final ClickEvent<JmixButton> event) {
        ServiceInfo serviceInfo = serviceInfoDc.getItem();
        if (serviceInfo == null) {
            return;
        }

        DialogWindow<ShiftListView> window = dialogWindows.view(this, ShiftListView.class).build();
        window.getView().setServiceInfo(serviceInfo);
        window.open();
    }
}