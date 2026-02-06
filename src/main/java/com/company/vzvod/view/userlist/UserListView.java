package com.company.vzvod.view.userlist;

import com.company.vzvod.entity.User;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.company.vzvod.view.usercard.UserCardView;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "user-list-view", layout = MainViewTopMenu.class)
@ViewController("UserListView")
@ViewDescriptor("user-list-view.xml")
public class UserListView extends StandardView {

    @Autowired
    private ViewNavigators viewNavigators;

    private void openUserCard(User user) {
        if (user == null) {
            return;
        }

        viewNavigators.view(this, UserCardView.class)
                .withQueryParameters(
                        QueryParameters.of("userId", user.getId().toString())
                )
                .withBackwardNavigation(true)
                .navigate();
    }

    // пример подписки на клик по строке грида без отдела
    @Subscribe("noDeptDataGrid")
    public void onNoDeptDataGridItemClick(ItemClickEvent<User> event) {
        openUserCard(event.getItem());
    }

    // аналогично для других гридов:
    @Subscribe("dept1DataGrid")
    public void onDept1DataGridItemClick(ItemClickEvent<User> event) {
        openUserCard(event.getItem());
    }

    @Subscribe("dept2DataGrid")
    public void onDept2DataGridItemClick(ItemClickEvent<User> event) {
        openUserCard(event.getItem());
    }
}