package com.company.vzvod.shift;

import com.company.vzvod.entity.Shift;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.Messages;

import java.time.LocalTime;

/**
 * Маленький кружок + подпись «Смена начата» / «Смена окончена».
 */
public final class ShiftStatusBadgeFactory {

    private static final String MSG_STARTED = "com.company.vzvod.shift/shiftStatus.started";
    private static final String MSG_FINISHED = "com.company.vzvod.shift/shiftStatus.finished";

    private ShiftStatusBadgeFactory() {
    }

    public static HorizontalLayout create(Shift shift, Messages messages) {
        return create(shift == null ? null : shift.getEndTime(), messages);
    }

    public static HorizontalLayout create(LocalTime endTime, Messages messages) {
        ShiftCompletionStatus.Kind kind = ShiftCompletionStatus.of(endTime);

        Span dot = new Span();
        dot.addClassName("shift-status-dot");
        dot.addClassName("shift-status-dot--" + ShiftCompletionStatus.cssModifier(kind));
        dot.getElement().setAttribute("aria-hidden", "true");

        Span label = new Span(kind == ShiftCompletionStatus.Kind.FINISHED
                ? messages.getMessage(MSG_FINISHED)
                : messages.getMessage(MSG_STARTED));
        label.addClassName("shift-status-label");

        HorizontalLayout row = new HorizontalLayout(dot, label);
        row.setSpacing(true);
        row.setPadding(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.addClassName("shift-status-badge");
        row.setWidthFull();
        return row;
    }
}
