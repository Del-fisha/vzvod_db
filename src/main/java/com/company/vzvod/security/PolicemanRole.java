package com.company.vzvod.security;

import com.company.vzvod.entity.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "PolicemanRole", code = PolicemanRole.CODE, scope = SecurityScope.UI)
public interface PolicemanRole {
    String CODE = "policeman-role";

    @EntityAttributePolicy(
            entityClass = User.class,
            attributes = {
                    "id",
                    "username",
                    "password",
                    "firstName",
                    "lastName",
                    "patronymic",
                    "dateOfBirth",
                    "serviceInfo",
                    "contactsInfo",
                    "education",
                    "armyService",
                    "vehicleInfo",
                    "gender",
                    "authorities"
            },
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.ALL)
    void userEntityFullAccess();

    @EntityAttributePolicy(
            entityClass = ServiceInfo.class,
            attributes = {
                    "id",
                    "user",
                    "department",
                    "rank",
                    "status",
                    "post",
                    "idCard",
                    "token",
                    "breastplate",
                    "startDate",
                    "startOfPost",
                    "penalty",
                    "incentive",
                    "shifts",
                    "vocations",
                    "medicalExamination",
                    "qualificationClass",
                    "vacationDaysEntitled",
                    "vacationDaysAvailable",
                    "vacationDaysUsed"
            },
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = ServiceInfo.class, actions = EntityPolicyAction.ALL)
    void serviceInfoFullAccess();

    @EntityAttributePolicy(
            entityClass = IdCard.class,
            attributes = {"id", "issued", "spl", "until"},
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = IdCard.class, actions = EntityPolicyAction.ALL)
    void idCardFullAccess();

    @EntityAttributePolicy(
            entityClass = Penalty.class,
            attributes = {
                    "id",
                    "date",
                    "description",
                    "initiator",
                    "orderNumber",
                    "penaltyStatus",
                    "penaltyType",
                    "userServiceInfo"
            },
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = Penalty.class, actions = EntityPolicyAction.ALL)
    void penaltyFullAccess();

    @EntityAttributePolicy(
            entityClass = Incentive.class,
            attributes = {
                    "id",
                    "date",
                    "description",
                    "incentiveType",
                    "initiator",
                    "orderNumber",
                    "userServiceInfo"
            },
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = Incentive.class, actions = EntityPolicyAction.ALL)
    void incentiveFullAccess();

    @EntityAttributePolicy(
            entityClass = Shift.class,
            attributes = {
                    "id",
                    "date",
                    "departmentToday",
                    "typeOfShift",
                    "number",
                    "startTime",
                    "endTime",
                    "units",
                    "countOfStatements",
                    "countOfClaims",
                    "ibdWithoutMigrant",
                    "ibdWithMigrant",
                    "administrativeViolations",
                    "criminalViolations"
            },
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = Shift.class, actions = EntityPolicyAction.ALL)
    void shiftFullAccess();

    @EntityAttributePolicy(
            entityClass = Vocation.class,
            attributes = {
                    "id",
                    "userServiceInfo",
                    "typeId",
                    "type",
                    "startDate",
                    "endDate",
                    "countOfDays",
                    "hasDeparture",
                    "cityToDrive",
                    "daysAddedByDeparture"
            },
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = Vocation.class, actions = EntityPolicyAction.ALL)
    void vocationFullAccess();

    @EntityAttributePolicy(
            entityClass = Contacts.class,
            attributes = {
                    "id",
                    "user",
                    "phoneNumber",
                    "registration",
                    "habitation",
                    "nearestMetroStation"
            },
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = Contacts.class, actions = EntityPolicyAction.ALL)
    void contactsFullAccess();

    @EntityAttributePolicy(
            entityClass = Address.class,
            attributes = {
                    "id",
                    "index",
                    "city",
                    "street",
                    "houseNumber",
                    "body",
                    "flat",
                    "typeOfHousing",
                    "statusOfHousing"
            },
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = Address.class, actions = EntityPolicyAction.ALL)
    void addressFullAccess();

    @EntityAttributePolicy(
            entityClass = Education.class,
            attributes = {
                    "id",
                    "status",
                    "type",
                    "nameOfInstitution",
                    "started",
                    "until"
            },
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = Education.class, actions = EntityPolicyAction.ALL)
    void educationFullAccess();

    @EntityAttributePolicy(
            entityClass = AdministrativeViolation.class,
            attributes = {"id", "impact", "shift", "article"},
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = AdministrativeViolation.class, actions = EntityPolicyAction.ALL)
    void administrativeViolationFullAccess();

    @EntityAttributePolicy(
            entityClass = CriminalViolation.class,
            attributes = {"id", "impact", "shift", "type"},
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = CriminalViolation.class, actions = EntityPolicyAction.ALL)
    void criminalViolationFullAccess();

    @EntityAttributePolicy(
            entityClass = Event.class,
            attributes = {"id", "name", "description", "eventType", "date", "time", "place", "shiftOfDepartment"},
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = Event.class, actions = EntityPolicyAction.ALL)
    void eventFullAccess();

    @EntityAttributePolicy(
            entityClass = DeletedEvent.class,
            attributes = {
                    "id",
                    "name",
                    "description",
                    "eventType",
                    "date",
                    "time",
                    "place",
                    "shiftOfDepartment",
                    "originalEventId"
            },
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = DeletedEvent.class, actions = EntityPolicyAction.ALL)
    void deletedEventFullAccess();

    @EntityAttributePolicy(
            entityClass = Vehicle.class,
            attributes = {
                    "id",
                    "user",
                    "ownerShortFio",
                    "brand",
                    "model",
                    "stateNumber",
                    "registrationCertificate",
                    "insurance"
            },
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = Vehicle.class, actions = EntityPolicyAction.ALL)
    void vehicleFullAccess();

    @EntityAttributePolicy(
            entityClass = Department.class,
            attributes = {"id", "number", "serviceInfos"},
            action = EntityAttributePolicyAction.MODIFY
    )
    @EntityPolicy(entityClass = Department.class, actions = EntityPolicyAction.ALL)
    void departmentFullAccess();

    @ViewPolicy(viewIds = {
            "MainView",
            "UserListView",
            "UserCardView",
            "User.detail",
            "ServiceInfo.list",
            "ServiceInfo.detail",
            "IdCard.detail",
            "Penalty.detail",
            "Penalty.list",
            "MyPenalty.list",
            "Incentive.detail",
            "Incentive.list",
            "MyIncentive.list",
            "Shift.detail",
            "Shift.list",
            "Vocation.list",
            "Vocation.detail",
            "Contacts.detail",
            "Address.detail",
            "Education.detail",
            "UserCardView",
            "ShiftBlankView",
            "AdministrativeViolation.detail",
            "AdministrativeViolation.list",
            "MyAdministrativeViolation.list",
            "ProfileRedirect",
            "CriminalViolation.detail",
            "CriminalViolation.list",
            "MyCriminalViolation.list",
            "MyShift.list",
            "Event.list",
            "LastEvent.list",
            "LastEvent.detail",
            "Event.detail",
            "DeletedEvent.detail",
            "DeletedEvent.list",
            "Vehicle.list",
            "Vehicle.detail",
            "MyVehicle.list",
            "Department.list",
            "MainViewTopMenu",
            "CompensatoryTimeRaportView"
    })
    void userViews();

    @MenuPolicy(menuIds = {
            "my_data",
            "all_employees_to_read",
            "my_profile",
            "my_transport",
            "my_shifts",
            "my_administrative_violations",
            "my_criminal_violations",
            "my_penalties",
            "my_incentives",
            "application",
            "events",
            "raports",
            "all_vocations",
            "all_administrative_violations",
            "all_criminal_violations",
            "Vocation.list",
            "AdministrativeViolation.list",
            "CriminalViolation.list",
            "future_events",
            "last_events",
            "deleted_events",
            "menuAllShifts",
            "User.list",
            "Penalty.list",
            "Incentive.list",
            "DeletedEvent.list",
            "Vehicle.list",
            "CompensatoryTimeRaportView"
    })
    void userMenu();

    @SpecificPolicy(resources = "ui.loginToUi")
    void loginToUi();
}