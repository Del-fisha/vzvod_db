package com.company.vzvod.view.serviceinfo;

import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.Qualification;
import com.company.vzvod.entity.ServiceBreak;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.idcard.IdCardDetailView;
import com.company.vzvod.view.incentive.IncentiveListView;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.penalty.PenaltyListView;
import com.company.vzvod.view.shift.ShiftListView;
import com.company.vzvod.view.vocation.VocationListView;
import com.company.vzvod.service.ServiceBreakRules;
import com.company.vzvod.service.ServiceInfoDialogSaveService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.UUID;

@Route(value = "service-infoes/:id", layout = MainView.class)
@ViewController(id = "ServiceInfo.detail")
@ViewDescriptor(path = "service-info-detail-view.xml")
@EditedEntityContainer("serviceInfoDc")
public class ServiceInfoDetailView extends StandardDetailView<ServiceInfo> {

    private static final String FIELD_WIDTH = "16em";


    @ViewComponent
    private InstanceContainer<ServiceInfo> serviceInfoDc;

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private ServiceInfoDialogSaveService serviceInfoDialogSaveService;

    @Autowired
    private UiAccessService uiAccessService;

    @Autowired
    private Notifications notifications;

    @Autowired
    private UiComponents uiComponents;

    @Autowired
    private Messages messages;

    @Autowired
    private DatatypeRegistry datatypeRegistry;

    @ViewComponent
    private CollectionContainer<ServiceBreak> serviceBreaksDc;

    @ViewComponent
    private VerticalLayout serviceBreaksBox;

    @ViewComponent
    private io.jmix.flowui.component.combobox.EntityComboBox<?> departmentField;
    @ViewComponent
    private io.jmix.flowui.component.select.JmixSelect<?> rankField;
    @ViewComponent
    private io.jmix.flowui.component.select.JmixSelect<?> postField;
    @ViewComponent
    private io.jmix.flowui.component.datepicker.TypedDatePicker<?> startDateField;
    @ViewComponent
    private io.jmix.flowui.component.datepicker.TypedDatePicker<?> startOfPostField;
    @ViewComponent
    private JmixButton addServiceBreakBtn;

    @ViewComponent
    private JmixButton incentiveListButton;

    @ViewComponent
    private JmixButton penaltyListButton;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<ServiceInfo> event) {
        ServiceInfo serviceInfo = event.getEntity();
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setPost(Post.POLICEMAN);
        serviceInfo.setMedicalExamination(false);
        serviceInfo.setQualificationClass(Qualification.NONE);
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        renderServiceBreaks();
        if (uiAccessService.hasFullAccessRole()) {
            return;
        }
        // Служебная информация: видит, но изменить не может
        if (departmentField != null) departmentField.setReadOnly(true);
        if (rankField != null) rankField.setReadOnly(true);
        if (postField != null) postField.setReadOnly(true);
        if (startDateField != null) startDateField.setReadOnly(true);
        if (startOfPostField != null) startOfPostField.setReadOnly(true);
        if (addServiceBreakBtn != null) addServiceBreakBtn.setVisible(false);

        // Поощрения/взыскания: кнопки видны, но не активны
        if (incentiveListButton != null) incentiveListButton.setEnabled(false);
        if (penaltyListButton != null) penaltyListButton.setEnabled(false);
    }

    @Subscribe(id = "serviceBreaksDc", target = Target.DATA_CONTAINER)
    public void onServiceBreaksDcCollectionChange(CollectionContainer.CollectionChangeEvent<ServiceBreak> event) {
        renderServiceBreaks();
    }

    @Subscribe(id = "addServiceBreakBtn", subject = "clickListener")
    public void onAddServiceBreakBtnClick(ClickEvent<JmixButton> event) {
        ServiceBreak created = getViewData().getDataContext().create(ServiceBreak.class);
        created.setServiceInfo(getEditedEntity());
        serviceBreaksDc.getMutableItems().add(created);
    }

    private void renderServiceBreaks() {
        if (serviceBreaksBox == null || serviceBreaksDc == null) {
            return;
        }
        boolean editable = uiAccessService.hasFullAccessRole();
        if (addServiceBreakBtn != null) {
            addServiceBreakBtn.setVisible(editable);
        }
        serviceBreaksBox.removeAll();

        HorizontalLayout header = new HorizontalLayout();
        header.setPadding(false);
        header.setSpacing(true);
        header.add(columnLabel("com.company.vzvod.entity/ServiceBreak.fromDate"));
        header.add(columnLabel("com.company.vzvod.entity/ServiceBreak.toDate"));
        if (editable) {
            header.add(removeSpacer());
        }
        serviceBreaksBox.add(header);

        for (ServiceBreak serviceBreak : serviceBreaksDc.getItems()) {
            serviceBreaksBox.add(breakRow(serviceBreak, editable));
        }
    }

    private HorizontalLayout breakRow(ServiceBreak serviceBreak, boolean editable) {
        HorizontalLayout row = new HorizontalLayout();
        row.setPadding(false);
        row.setSpacing(true);
        row.add(dateField(serviceBreak.getFromDate(), editable, serviceBreak::setFromDate));
        row.add(dateField(serviceBreak.getToDate(), editable, serviceBreak::setToDate));
        if (editable) {
            JmixButton remove = uiComponents.create(JmixButton.class);
            remove.setText("×");
            remove.addThemeNames("tertiary-inline", "error");
            remove.setWidth("2.5em");
            remove.addClickListener(click -> {
                getViewData().getDataContext().remove(serviceBreak);
                serviceBreaksDc.getMutableItems().remove(serviceBreak);
            });
            row.add(remove);
        }
        return row;
    }

    private com.vaadin.flow.component.html.Span columnLabel(String messageKey) {
        com.vaadin.flow.component.html.Span label = new com.vaadin.flow.component.html.Span(messages.getMessage(messageKey));
        label.setWidth(FIELD_WIDTH);
        return label;
    }

    private com.vaadin.flow.component.html.Span removeSpacer() {
        com.vaadin.flow.component.html.Span spacer = new com.vaadin.flow.component.html.Span();
        spacer.setWidth("2.5em");
        return spacer;
    }

    private TypedDatePicker<LocalDate> dateField(LocalDate value, boolean editable, java.util.function.Consumer<LocalDate> setter) {
        TypedDatePicker<LocalDate> picker = uiComponents.create(TypedDatePicker.class);
        picker.setDatatype(datatypeRegistry.get(LocalDate.class));
        picker.setWidth(FIELD_WIDTH);
        picker.setReadOnly(!editable);
        picker.setValue(value);
        picker.addValueChangeListener(change -> {
            if (change.isFromClient()) {
                setter.accept(change.getValue());
            }
        });
        return picker;
    }

    /**
     * When this view is opened as a dialog window from another screen,
     * we want ServiceInfo to be persisted right here on SAVE and avoid
     * parent-screen double-persist issues.
     */
    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        String breakProblem = ServiceBreakRules.problem(getEditedEntity());
        if (breakProblem != null) {
            event.preventSave();
            notifications.create(breakProblem).show();
            return;
        }
        event.preventSave();

        ServiceInfo saved = serviceInfoDialogSaveService.saveFromDialog(getEditedEntity());

        // IMPORTANT:
        // `setEntityToEdit(saved)` may mark DataContext as modified again (merge/value changes),
        // which then triggers the "unsaved changes" dialog on close and may loop.
        // We reset the DataContext state explicitly and only update the container item.
        getViewData().getDataContext().clear();
        serviceInfoDc.setItem(saved);
        clearChanges();
        event.resume(close(StandardOutcome.SAVE));
    }

    @Subscribe(id = "idCardCreateButton", subject = "clickListener")
    public void onIdCardCreateButtonClick(final ClickEvent<JmixButton> event) {
        ServiceInfo serviceInfo = serviceInfoDc.getItemOrNull();
        if (serviceInfo == null) {
            return;
        }

        IdCard idCard = serviceInfo.getIdCard();
        if (idCard == null) {
            // If ServiceInfo is being edited within a parent DataContext (e.g. new User),
            // create IdCard inside the same DataContext to avoid detached instances.
            idCard = (entityStates.isNew(serviceInfo) || serviceInfo.getId() == null)
                    ? getViewData().getDataContext().create(IdCard.class)
                    : dataManager.create(IdCard.class);
        }

        boolean serviceInfoPersisted = !entityStates.isNew(serviceInfo) && serviceInfo.getId() != null;

        DialogWindow<IdCardDetailView> window = serviceInfoPersisted
                ? dialogWindows.detail(this, IdCard.class)
                .withViewClass(IdCardDetailView.class)
                .editEntity(idCard)
                .build()
                : dialogWindows.detail(this, IdCard.class)
                .withViewClass(IdCardDetailView.class)
                .withParentDataContext(getViewData().getDataContext())
                .editEntity(idCard)
                .build();

        window.addAfterCloseListener(closeEvent -> {
            if (!closeEvent.closedWith(StandardOutcome.SAVE)) {
                return;
            }

            IdCard savedIdCard = window.getView().getEditedEntity();
            if (entityStates.isNew(serviceInfo) || serviceInfo.getId() == null) {
                // ServiceInfo isn't in DB yet: keep everything inside the same DataContext
                // so the whole graph is persisted once when ServiceInfo is saved.
                IdCard mergedIdCard = getViewData().getDataContext().merge(savedIdCard);
                serviceInfo.setIdCard(mergedIdCard);
                return;
            }

            // ServiceInfo is already persisted. Never keep instances coming from the IdCard dialog DataContext:
            // load a fresh persisted IdCard and attach it to THIS view's DataContext, otherwise it can be re-inserted
            // during ServiceInfo save (PK_ID_CARD violation).
            IdCard persistedIdCard = dataManager.load(IdCard.class).id(savedIdCard.getId()).one();
            serviceInfo.setIdCard(getViewData().getDataContext().merge(persistedIdCard));

            ServiceInfo persisted = dataManager.load(ServiceInfo.class)
                    .id(serviceInfo.getId())
                    .one();
            // Persisted ServiceInfo is saved via DataManager (separate context),
            // so use a managed IdCard instance for that save as well.
            persisted.setIdCard(dataManager.load(IdCard.class).id(savedIdCard.getId()).one());
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