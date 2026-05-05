package com.company.vzvod.dialog_beans;

import com.company.vzvod.view.raport.CompensatoryTimeRaportView;
import com.company.vzvod.view.raport.DailyShiftRaportView;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("raportMenuBean")
public class RaportMenuBean {

    @Autowired
    private DialogWindows dialogWindows;

    public void openCompensatoryRaport() {
        View<?> currentView = UiComponentUtils.getCurrentView();

        dialogWindows
                .view(currentView, CompensatoryTimeRaportView.class)
                .open();
    }

    public void openDailyShiftRaport() {
        View<?> currentView = UiComponentUtils.getCurrentView();

        dialogWindows
                .view(currentView, DailyShiftRaportView.class)
                .open();
    }
}