package com.company.vzvod.view.shiftblank;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.User;
import com.vaadin.flow.router.Route;
import com.company.vzvod.view.main.MainView;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;

import java.util.List;
import java.util.UUID;

@Route(value = "shift-blank", layout = MainView.class)
@ViewController("ShiftBlankView")
@ViewDescriptor("shift-blank-view.xml")
public class ShiftBlankView extends StandardView {

    @ViewComponent
    private InstanceContainer<Shift> shiftDc;

    @ViewComponent
    private InstanceLoader<Shift> shiftDl;

    @ViewComponent
    private CollectionContainer<User> unitsDc;

    @ViewComponent
    private CollectionPropertyContainer<AdministrativeViolation> administrativeViolationsDc;

    @ViewComponent
    private CollectionPropertyContainer<CriminalViolation> criminalViolationsDc;

    /**
     * Читаем shiftId из query-параметров и грузим смену.
     */
    @Subscribe
    public void onQueryParametersChange(QueryParametersChangeEvent event) {
        List<String> params = event.getQueryParameters()
                .getParameters()
                .get("shiftId");

        if (params == null || params.isEmpty()) {
            return;
        }

        UUID id = UUID.fromString(params.get(0));
        shiftDl.setEntityId(id);
        shiftDl.load();
    }

}