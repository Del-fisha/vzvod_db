package com.company.vzvod.view.idcard;

import com.company.vzvod.entity.IdCard;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "id-cards/:id", layout = MainViewTopMenu.class)
@ViewController(id = "IdCard.detail")
@ViewDescriptor(path = "id-card-detail-view.xml")
@EditedEntityContainer("idCardDc")
public class IdCardDetailView extends StandardDetailView<IdCard> {
}