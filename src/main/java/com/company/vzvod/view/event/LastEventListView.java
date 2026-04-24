package com.company.vzvod.view.event;

import com.company.vzvod.entity.Event;
import com.company.vzvod.security.FullAccessRole;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "last-events-list", layout = MainView.class)
@ViewController(id = "LastEvent.list")
@ViewDescriptor(path = "last-event-list-view.xml")
@LookupComponent("eventsDataGrid")
@DialogMode(width = "64em")
public class LastEventListView extends StandardListView<Event> {

    @ViewComponent
    private DataGrid<Event> eventsDataGrid;

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    @ViewComponent
    private Button createButton;
    @ViewComponent
    private Button editButton;
    @ViewComponent
    private Button removeButton;

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (!hasRole(FullAccessRole.CODE)) {
            createButton.setVisible(false);
            editButton.setVisible(false);
            removeButton.setVisible(false);

            var createAction = eventsDataGrid.getAction("createAction");
            if (createAction != null) {
                createAction.setEnabled(false);
            }
            var editAction = eventsDataGrid.getAction("editAction");
            if (editAction != null) {
                editAction.setEnabled(false);
            }
            var removeAction = eventsDataGrid.getAction("removeAction");
            if (removeAction != null) {
                removeAction.setEnabled(false);
            }
        }
    }

    private boolean hasRole(String roleCode) {
        String prefix = roleGrantedAuthorityUtils.getDefaultRolePrefix();
        return currentAuthentication.getUser().getAuthorities().stream()
                .anyMatch(grantedAuthority -> {
                    String a = grantedAuthority.getAuthority();
                    return roleCode.equals(a) || (prefix + roleCode).equals(a);
                });
    }
}