package com.company.vzvod.audit;

final class GenericEntityAuditMessageFormatter {

    private GenericEntityAuditMessageFormatter() {
    }

    static String createdMessage(String actor, String entityCaption, String instanceRef, String employeeFio) {
        return "%s создал %s (%s)%s".formatted(
                actor,
                entityCaption,
                instanceRef,
                employeeSuffix(employeeFio)
        );
    }

    static String deletedMessage(String actor, String entityCaption, String instanceRef, String employeeFio) {
        return "%s удалил %s (%s)%s".formatted(
                actor,
                entityCaption,
                instanceRef,
                employeeSuffix(employeeFio)
        );
    }

    static String changedFieldMessage(
            String actor,
            String fieldLabel,
            String entityCaption,
            String instanceRef,
            String employeeFio
    ) {
        return "%s изменил поле «%s» в %s (%s)%s".formatted(
                actor,
                fieldLabel,
                entityCaption,
                instanceRef,
                employeeSuffix(employeeFio)
        );
    }

    static String clearedFieldMessage(
            String actor,
            String fieldLabel,
            String entityCaption,
            String instanceRef,
            String employeeFio
    ) {
        return "%s очистил поле «%s» в %s (%s)%s".formatted(
                actor,
                fieldLabel,
                entityCaption,
                instanceRef,
                employeeSuffix(employeeFio)
        );
    }

    private static String employeeSuffix(String employeeFio) {
        if (employeeFio == null || employeeFio.isBlank() || "—".equals(employeeFio)) {
            return "";
        }
        return ", сотрудник " + employeeFio;
    }
}
