package com.company.vzvod.view.serviceinfo;

import com.company.vzvod.bot.BotMeShiftsVocationsService;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.util.EmployeeOrdering;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Route(value = "service-infoes", layout = MainView.class)
@ViewController(id = "ServiceInfo.list")
@ViewDescriptor(path = "service-info-list-view.xml")
@LookupComponent("serviceInfoesDataGrid")
@DialogMode(width = "64em")
public class ServiceInfoListView extends StandardListView<ServiceInfo> {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private BotMeShiftsVocationsService shiftsVocationsService;

    private UUID shiftPartnerUserId;
    private UUID excludeShiftId;
    private Set<UUID> excludeServiceInfoIds = Set.of();

    /**
     * Лукап «добавить в смену»: только «В строю», не занятые другой сменой,
     * своё отделение сверху, затем другое, внутри — по фамилии.
     */
    public void configureShiftPartnerLookup(UUID currentUserId, UUID shiftId, Set<UUID> alreadyOnShift) {
        this.shiftPartnerUserId = currentUserId;
        this.excludeShiftId = shiftId;
        this.excludeServiceInfoIds = alreadyOnShift == null ? Set.of() : Set.copyOf(alreadyOnShift);
    }

    /**
     * ФИО зашифрованы — сортировка по фамилии только в памяти после загрузки.
     */
    @Install(to = "serviceInfoesDl", target = Target.DATA_LOADER)
    private List<ServiceInfo> serviceInfoesDlLoadDelegate(LoadContext<ServiceInfo> loadContext) {
        if (shiftPartnerUserId != null) {
            return shiftsVocationsService.loadPartnerServiceInfos(
                    shiftPartnerUserId, excludeShiftId, excludeServiceInfoIds);
        }
        List<ServiceInfo> all = dataManager.load(ServiceInfo.class)
                .query("select e from ServiceInfo e")
                .fetchPlan(loadContext.getFetchPlan())
                .list();
        all.sort(EmployeeOrdering.serviceInfoComparator());
        return all;
    }
}
