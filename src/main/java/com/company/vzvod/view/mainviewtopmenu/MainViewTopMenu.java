package com.company.vzvod.view.mainviewtopmenu;

import com.google.common.base.Strings;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route("")
@ViewController(id = "MainViewTopMenu")
@ViewDescriptor(path = "main-view-top-menu.xml")
public class MainViewTopMenu extends StandardMainView {

    @Override
    protected void updateTitle() {
        super.updateTitle();

        String viewTitle = getTitleFromOpenedView();
        UiComponentUtils.findComponent(getContent(), "viewHeaderBox")
                .ifPresent(component -> component.setVisible(!Strings.isNullOrEmpty(viewTitle)));
    }
}