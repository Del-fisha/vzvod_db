package com.company.vzvod.messaging;

public final class DashboardMessageFormatter {

    private DashboardMessageFormatter() {
    }

    public static String formatTelegramBody(String senderDisplayName, String messageBody) {
        return "Отправитель: " + senderDisplayName + "\n\n" + messageBody;
    }
}
