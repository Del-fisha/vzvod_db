package com.company.vzvod.security;

import com.company.vzvod.entity.*;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.model.RowLevelBiPredicate;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;
import org.springframework.context.ApplicationContext;

@RowLevelRole(name = "PolicemanRowLevel", code = PolicemanRowLevelRole.CODE)
public interface PolicemanRowLevelRole {

    String CODE = "policeman-row-level";

    @PredicateRowLevelPolicy(entityClass = Penalty.class, actions = RowLevelPolicyAction.READ)
    default RowLevelBiPredicate<Penalty, ApplicationContext> penaltyReadOnlySelf() {
        return (penalty, applicationContext) -> isSelfServiceInfo(penalty.getUserServiceInfo(), applicationContext);
    }

    @PredicateRowLevelPolicy(entityClass = Incentive.class, actions = RowLevelPolicyAction.READ)
    default RowLevelBiPredicate<Incentive, ApplicationContext> incentiveReadOnlySelf() {
        return (incentive, applicationContext) -> isSelfServiceInfo(incentive.getUserServiceInfo(), applicationContext);
    }

    @PredicateRowLevelPolicy(entityClass = Vocation.class, actions = RowLevelPolicyAction.READ)
    default RowLevelBiPredicate<Vocation, ApplicationContext> vocationReadOnlySelf() {
        return (vocation, applicationContext) -> isSelfServiceInfo(vocation.getUserServiceInfo(), applicationContext);
    }

    @PredicateRowLevelPolicy(entityClass = AdministrativeViolation.class, actions = RowLevelPolicyAction.READ)
    default RowLevelBiPredicate<AdministrativeViolation, ApplicationContext> administrativeViolationReadByShiftMembership() {
        return (violation, applicationContext) -> isCurrentUserInShiftUnits(violation, applicationContext);
    }

    @PredicateRowLevelPolicy(entityClass = CriminalViolation.class, actions = RowLevelPolicyAction.READ)
    default RowLevelBiPredicate<CriminalViolation, ApplicationContext> criminalViolationReadByShiftMembership() {
        return (violation, applicationContext) -> isCurrentUserInShiftUnits(violation, applicationContext);
    }

    private static boolean isSelfServiceInfo(ServiceInfo owner, ApplicationContext applicationContext) {
        ServiceInfo current = currentServiceInfo(applicationContext);
        return current != null
                && current.getId() != null
                && owner != null
                && owner.getId() != null
                && current.getId().equals(owner.getId());
    }

    private static boolean isCurrentUserInShiftUnits(Violation violation, ApplicationContext applicationContext) {
        ServiceInfo current = currentServiceInfo(applicationContext);
        if (current == null || current.getId() == null) {
            return false;
        }
        if (violation.getShift() == null || violation.getShift().getUnits() == null) {
            return false;
        }
        return violation.getShift().getUnits().stream()
                .anyMatch(si -> si != null && si.getId() != null && si.getId().equals(current.getId()));
    }

    private static ServiceInfo currentServiceInfo(ApplicationContext applicationContext) {
        CurrentAuthentication currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
        Object u = currentAuthentication.getUser();
        if (!(u instanceof User user)) {
            return null;
        }
        return user.getServiceInfo();
    }
}
