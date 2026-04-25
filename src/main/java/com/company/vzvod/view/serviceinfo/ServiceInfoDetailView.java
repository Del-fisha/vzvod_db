package com.company.vzvod.view.serviceinfo;

import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.Qualification;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.view.idcard.IdCardDetailView;
import com.company.vzvod.view.incentive.IncentiveListView;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.penalty.PenaltyListView;
import com.company.vzvod.view.shift.ShiftListView;
import com.company.vzvod.view.vocation.VocationListView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Route(value = "service-infoes/:id", layout = MainView.class)
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

    @Autowired
    private EntityStates entityStates;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<ServiceInfo> event) {
        ServiceInfo serviceInfo = event.getEntity();
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setPost(Post.POLICEMAN);
        serviceInfo.setMedicalExamination(false);
        serviceInfo.setQualificationClass(Qualification.NONE);

    }

    @Subscribe(id = "idCardCreateButton", subject = "clickListener")
    public void onIdCardCreateButtonClick(final ClickEvent<JmixButton> event) {
        ServiceInfo serviceInfo = serviceInfoDc.getItemOrNull();
        if (serviceInfo == null) {
            return;
        }

        IdCard idCard = serviceInfo.getIdCard();
        if (idCard == null) {
            idCard = dataManager.create(IdCard.class);
        }

        DialogWindow<IdCardDetailView> window = dialogWindows.detail(this, IdCard.class)
                .withViewClass(IdCardDetailView.class)
                .editEntity(idCard)
                .build();

        window.addAfterCloseListener(closeEvent -> {
            if (!closeEvent.closedWith(StandardOutcome.SAVE)) {
                return;
            }

            IdCard savedIdCard = window.getView().getEditedEntity();
            serviceInfo.setIdCard(savedIdCard);

            if (entityStates.isNew(serviceInfo) || serviceInfo.getId() == null) {
                // ServiceInfo isn't in DB yet: relation will be persisted when ServiceInfo is saved
                return;
            }

            ServiceInfo persisted = dataManager.load(ServiceInfo.class)
                    .id(serviceInfo.getId())
                    .one();
            persisted.setIdCard(savedIdCard);
            dataManager.save(persisted);
        });

        window.open();
    }


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