package com.company.vzvod.aop;

import com.company.vzvod.entity.User;
import com.company.vzvod.service.UserReadService;
import com.vaadin.flow.server.VaadinServlet;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.UUID;

/**
 * Загрузка userDl до готовности {@code @Autowired} во view (вызов из {@code beforeEnter}).
 * Логика вынесена из view, чтобы не дублировать бины в делегате.
 */
public final class UserDetailViewDataLoadSupport {

    private UserDetailViewDataLoadSupport() {
    }

    public static User load(LoadContext<User> loadContext) {
        UUID id = (UUID) loadContext.getId();
        WebApplicationContext springContext = WebApplicationContextUtils
                .getRequiredWebApplicationContext(VaadinServlet.getCurrent().getServletContext());
        springContext.getBean(UserReadService.class).getUserCached(id);
        return springContext.getBean(DataManager.class).load(User.class).id(id).one();
    }
}
