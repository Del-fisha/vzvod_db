package com.company.vzvod.audit;

import com.company.vzvod.logging.LoggingServiceClient;
import io.jmix.core.event.EntityChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EmployeeChangeAuditListener {

    private static final Logger log = LoggerFactory.getLogger(EmployeeChangeAuditListener.class);

    private final AuditActorResolver actorResolver;
    private final AuditChangeSourceResolver changeSourceResolver;
    private final EmployeeAuditService auditService;
    private final LoggingServiceClient loggingClient;

    public EmployeeChangeAuditListener(
            AuditActorResolver actorResolver,
            AuditChangeSourceResolver changeSourceResolver,
            EmployeeAuditService auditService,
            LoggingServiceClient loggingClient
    ) {
        this.actorResolver = actorResolver;
        this.changeSourceResolver = changeSourceResolver;
        this.auditService = auditService;
        this.loggingClient = loggingClient;
    }

    @EventListener
    public void onEntityChanged(EntityChangedEvent<?> event) {
        try {
            String actor = actorResolver.resolveActorLabel();
            String changeSource = changeSourceResolver.resolve();
            for (String message : auditService.buildAuditMessages(event, actor, changeSource)) {
                loggingClient.logMain(message);
            }
        } catch (RuntimeException e) {
            log.warn("Не удалось записать аудит изменения {}: {}", event.getEntityId(), e.toString());
        }
    }
}
