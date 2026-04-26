package com.company.vzvod.aop;

import com.company.vzvod.entity.ServiceInfo;
import io.jmix.core.SaveContext;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.model.DataContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Diagnostics for "Access denied" on ServiceInfo save.
 * Enabled in test/dev profiles only.
 */
@Profile({"test", "dev"})
@Aspect
@Component
public class ServiceInfoSaveAccessDiagnosticsAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceInfoSaveAccessDiagnosticsAspect.class);

    private final CurrentAuthentication currentAuthentication;

    public ServiceInfoSaveAccessDiagnosticsAspect(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Around("execution(* io.jmix.core.DataManager.save(..))")
    public Object aroundDataManagerSave(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        ServiceInfo si = extractServiceInfo(args);
        SaveContext saveContext = extractSaveContext(args);
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            // In UI save pipeline, "access denied" may be thrown as different exception types.
            // Log whenever ServiceInfo is involved in the SaveContext.
            if (si != null) {
                logDenied("DataManager.save", si, e);
            } else if (saveContext != null) {
                boolean hasServiceInfo = saveContext.getEntitiesToSave().stream().anyMatch(ServiceInfo.class::isInstance)
                        || saveContext.getEntitiesToRemove().stream().anyMatch(ServiceInfo.class::isInstance);
                if (hasServiceInfo) {
                    logDeniedSaveContext("DataManager.save", saveContext, e);
                }
            }
            throw e;
        }
    }

    @Around("execution(* io.jmix.flowui.model.DataContext.save(..))")
    public Object aroundDataContextSave(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            log.error("SECURITY DIAG: DataContext.save failed. exClass={}, actor={}, msg={}",
                    e.getClass().getName(),
                    currentAuthentication.getUser(),
                    e.getMessage()
            );
            throw e;
        }
    }

    private ServiceInfo extractServiceInfo(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object a : args) {
            if (a instanceof ServiceInfo si) {
                return si;
            }
        }
        return null;
    }

    private SaveContext extractSaveContext(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object a : args) {
            if (a instanceof SaveContext sc) {
                return sc;
            }
        }
        return null;
    }

    private void logDenied(String op, ServiceInfo si, Throwable e) {
        Object principal = currentAuthentication.getUser();
        String authorities = currentAuthentication.getAuthentication().getAuthorities().stream()
                .map(a -> a.getAuthority() == null ? "<null>" : a.getAuthority())
                .sorted()
                .collect(Collectors.joining(", "));

        log.error(
                "SECURITY DIAG: {} failed. exClass={}, actor={}, authorities=[{}], serviceInfoId={}, userId={}, post={}, status={}, medicalExamination={}, msg={}",
                op,
                e.getClass().getName(),
                principal,
                authorities,
                si.getId(),
                si.getUser() != null ? si.getUser().getId() : null,
                si.getPost(),
                si.getStatus(),
                si.getMedicalExamination(),
                e.getMessage()
        );
    }

    private void logDeniedSaveContext(String op, SaveContext sc, Throwable e) {
        Object principal = currentAuthentication.getUser();
        String authorities = currentAuthentication.getAuthentication().getAuthorities().stream()
                .map(a -> a.getAuthority() == null ? "<null>" : a.getAuthority())
                .sorted()
                .collect(Collectors.joining(", "));

        long serviceInfoToSave = sc.getEntitiesToSave().stream().filter(ServiceInfo.class::isInstance).count();
        long serviceInfoToRemove = sc.getEntitiesToRemove().stream().filter(ServiceInfo.class::isInstance).count();

        String saveTypes = sc.getEntitiesToSave().stream()
                .map(o -> o == null ? "<null>" : o.getClass().getSimpleName())
                .sorted()
                .collect(Collectors.joining(", "));

        String removeTypes = sc.getEntitiesToRemove().stream()
                .map(o -> o == null ? "<null>" : o.getClass().getSimpleName())
                .sorted()
                .collect(Collectors.joining(", "));

        ServiceInfo firstSi = sc.getEntitiesToSave().stream()
                .filter(ServiceInfo.class::isInstance)
                .map(ServiceInfo.class::cast)
                .findFirst()
                .orElse(null);

        log.error(
                "SECURITY DIAG: {} failed. exClass={}, actor={}, authorities=[{}], saveTypes=[{}], removeTypes=[{}], serviceInfoToSave={}, serviceInfoToRemove={}, firstServiceInfoId={}, msg={}",
                op,
                e.getClass().getName(),
                principal,
                authorities,
                saveTypes,
                removeTypes,
                serviceInfoToSave,
                serviceInfoToRemove,
                firstSi != null ? firstSi.getId() : null,
                e.getMessage()
        );
    }
}

