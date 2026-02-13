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


    @PredicateRowLevelPolicy(
            entityClass = User.class,
            actions = {RowLevelPolicyAction.UPDATE}
    )
    default RowLevelBiPredicate<User, ApplicationContext> userUpdateOnlySelf() {
        return (user, applicationContext) -> {
            CurrentAuthentication currentAuth =
                    applicationContext.getBean(CurrentAuthentication.class);
            User currentUser = (User) currentAuth.getUser();
            return user.getId() != null
                    && user.getId().equals(currentUser.getId());
        };
    }


    @PredicateRowLevelPolicy(
            entityClass = ServiceInfo.class,
            actions = {RowLevelPolicyAction.UPDATE}
    )
    default RowLevelBiPredicate<ServiceInfo, ApplicationContext> serviceInfoUpdateOnlySelf() {
        return (serviceInfo, applicationContext) -> {
            CurrentAuthentication currentAuthentication =
                    applicationContext.getBean(CurrentAuthentication.class);
            User currentUser = (User) currentAuthentication.getUser();

            return serviceInfo.getUser() != null
                    && serviceInfo.getUser().getId().equals(currentUser.getId());
        };
    }


    @PredicateRowLevelPolicy(
            entityClass = IdCard.class,
            actions = {RowLevelPolicyAction.UPDATE}
    )
    default RowLevelBiPredicate<IdCard, ApplicationContext> idCardUpdateOnlySelf() {
        return (idCard, applicationContext) -> {
            CurrentAuthentication currentAuthentication =
                    applicationContext.getBean(CurrentAuthentication.class);
            User currentUser = (User) currentAuthentication.getUser();

            ServiceInfo serviceInfo = currentUser.getServiceInfo();
            return serviceInfo != null
                    && serviceInfo.getIdCard() != null
                    && serviceInfo.getIdCard().getId().equals(idCard.getId());
        };
    }


    @PredicateRowLevelPolicy(
            entityClass = Shift.class,
            actions = {RowLevelPolicyAction.UPDATE}
    )
    default RowLevelBiPredicate<Shift, ApplicationContext> shiftUpdateOnlySelf() {
        return (shift, applicationContext) -> {
            CurrentAuthentication currentAuthentication =
                    applicationContext.getBean(CurrentAuthentication.class);
            User currentUser = (User) currentAuthentication.getUser();

            ServiceInfo serviceInfo = currentUser.getServiceInfo();
            if (serviceInfo == null) {
                return false;
            }

            if (shift.getUnits() == null) {
                return false;
            }

            return shift.getUnits().contains(serviceInfo);
        };
    }


    @PredicateRowLevelPolicy(
            entityClass = Shift.class,
            actions = {RowLevelPolicyAction.UPDATE}
    )
    default RowLevelBiPredicate<Vocation, ApplicationContext> vocationUpdateOnlySelf() {
        return (vocation, applicationContext) -> {
            CurrentAuthentication currentAuthentication =
                    applicationContext.getBean(CurrentAuthentication.class);
            User currentUser = (User) currentAuthentication.getUser();

            ServiceInfo serviceInfo = currentUser.getServiceInfo();
            if (serviceInfo == null) {
                return false;
            }

            if (vocation.getUserServiceInfo() == null) {
                return false;
            }

            return vocation.getUserServiceInfo().equals(serviceInfo);
        };
    }


    @PredicateRowLevelPolicy(
            entityClass = Contacts.class,
            actions = {RowLevelPolicyAction.UPDATE}
    )
    default RowLevelBiPredicate<Contacts, ApplicationContext> contactsUpdateOnlySelf() {
        return (contact, applicationContext) -> {
            CurrentAuthentication currentAuthentication =
                    applicationContext.getBean(CurrentAuthentication.class);
            User currentUser = (User) currentAuthentication.getUser();

            return currentUser.getContactsInfo().getId().equals(contact.getId());
        };
    }

}