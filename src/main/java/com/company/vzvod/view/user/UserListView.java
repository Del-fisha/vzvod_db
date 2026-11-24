package com.company.vzvod.view.user;

import com.company.vzvod.entity.User;
import com.company.vzvod.util.EmployeeOrdering;
import com.company.vzvod.util.SortedListLoader;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Route(value = "users", layout = MainView.class)
@ViewController(id = "User.list")
@ViewDescriptor(path = "user-list-view.xml")
@LookupComponent("usersDataGrid")
@DialogMode(width = "64em")
public class UserListView extends StandardListView<User> {

    @Autowired
    private DataManager dataManager;

    /**
     * Фамилия зашифрована, поэтому должность → звание → фамилия считаются после загрузки.
     */
    @Install(to = "usersDl", target = Target.DATA_LOADER)
    private List<User> usersDlLoadDelegate(LoadContext<User> loadContext) {
        return SortedListLoader.load(dataManager, loadContext, EmployeeOrdering.userComparator());
    }
}