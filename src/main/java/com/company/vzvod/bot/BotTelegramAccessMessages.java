package com.company.vzvod.bot;

public final class BotTelegramAccessMessages {

    public static final String PHONE_CHANGED =
            "Доступ к боту закрыт: в карточке сотрудника изменён номер телефона. "
                    + "Для входа снова нажмите /start и отправьте актуальный номер.";

    public static final String USER_REMOVED =
            "Доступ к боту закрыт: учётная запись сотрудника удалена из системы.";

    public static final String NOT_ACTIVE =
            "Доступ к боту закрыт: сотрудник не в статусе «В строю».";

    public static final String PHONE_MISSING =
            "Доступ к боту закрыт: в карточке сотрудника не указан номер телефона.";

    public static final String RECONCILIATION =
            "Доступ к боту закрыт: данные сотрудника больше не соответствуют условиям входа. "
                    + "Для повторного входа нажмите /start.";

    private BotTelegramAccessMessages() {
    }
}
