package com.company.vzvod.view.userlist;

import com.company.vzvod.entity.User;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.usercard.UserCardView;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Route(value = "user-list-view", layout = MainView.class)
@ViewController("UserListView")
@ViewDescriptor("user-list-view.xml")
public class UserListView extends StandardView {

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private CollectionLoader<User> usersNoDeptDl;

    @ViewComponent
    private CollectionLoader<User> dept1UsersDl;

    @ViewComponent
    private CollectionLoader<User> dept2UsersDl;

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

    @Install(to = "usersNoDeptDl", target = Target.DATA_LOADER)
    private List<User> usersNoDeptDlLoadDelegate(io.jmix.core.LoadContext<User> loadContext) {
        // Keep DB ordering by non-sensitive fields only (post). Names are encrypted, so sort by names in-memory if needed.
        return dataManager.load(User.class)
                .query("""
                        select u
                        from User u
                        left join u.serviceInfo si
                        left join si.department d
                        where d is null
                        order by u.serviceInfo.post
                        """)
                .fetchPlan(loadContext.getFetchPlan())
                .list();
    }

    @Install(to = "dept1UsersDl", target = Target.DATA_LOADER)
    private List<User> dept1UsersDlLoadDelegate(io.jmix.core.LoadContext<User> loadContext) {
        List<User> users = dataManager.load(User.class)
                .query("""
                        select u
                        from User u
                        join u.serviceInfo si
                        join si.department d
                        where d.number = 1
                        order by u.serviceInfo.post, u.serviceInfo.rank desc
                        """)
                .fetchPlan(loadContext.getFetchPlan())
                .list();

        users.sort(defaultUserNameComparator());
        return users;
    }

    @Install(to = "dept2UsersDl", target = Target.DATA_LOADER)
    private List<User> dept2UsersDlLoadDelegate(io.jmix.core.LoadContext<User> loadContext) {
        List<User> users = dataManager.load(User.class)
                .query("""
                        select u
                        from User u
                        join u.serviceInfo si
                        join si.department d
                        where d.number = 2
                        order by u.serviceInfo.post, u.serviceInfo.rank desc
                        """)
                .fetchPlan(loadContext.getFetchPlan())
                .list();

        users.sort(defaultUserNameComparator());
        return users;
    }

    private static Comparator<User> defaultUserNameComparator() {
        return Comparator
                .comparing((User u) -> normalize(u.getLastName()), Comparator.nullsLast(String::compareTo))
                .thenComparing(u -> normalize(u.getFirstName()), Comparator.nullsLast(String::compareTo))
                .thenComparing(u -> normalize(u.getPatronymic()), Comparator.nullsLast(String::compareTo));
    }

    private static String normalize(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim().replaceAll("\\s+", " ");
        return t.toLowerCase(Locale.ROOT);
    }
}