package com.company.vzvod.view.mainviewtopmenu;

import com.company.vzvod.view.dashboard.WorkResultsStatisticsDialog;
import com.google.common.base.Strings;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController(id = "MainViewTopMenu")
@ViewDescriptor(path = "main-view-top-menu.xml")
public class MainViewTopMenu extends StandardMainView {

    @Autowired
    private DialogWindows dialogWindows;

    @ViewComponent
    private JmixButton openWorkResultsStatsBtn;

    @Subscribe(id = "openWorkResultsStatsBtn", subject = "clickListener")
    public void onOpenWorkResultsStatsBtnClick(final ClickEvent<JmixButton> event) {
        DialogWindow<WorkResultsStatisticsDialog> w = dialogWindows.view(this, WorkResultsStatisticsDialog.class).build();
        w.open();
    }

    @Override
    protected void updateTitle() {
        super.updateTitle();

        String viewTitle = getTitleFromOpenedView();
        UiComponentUtils.findComponent(getContent(), "viewHeaderBox")
                .ifPresent(component -> component.setVisible(!Strings.isNullOrEmpty(viewTitle)));
    }
}