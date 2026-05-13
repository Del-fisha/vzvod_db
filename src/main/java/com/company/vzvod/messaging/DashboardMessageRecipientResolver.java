package com.company.vzvod.messaging;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.service.DepartmentConverter;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DashboardMessageRecipientResolver {

    private final UnconstrainedDataManager dataManager;

    public DashboardMessageRecipientResolver(UnconstrainedDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Set<UUID> resolve(DashboardMessageAudience audience, UUID senderUserId, LocalDate operationalDate) {
        return switch (audience) {
            case TODAY_SHIFT_EMPLOYEES -> loadTodayShiftEmployeeIds(operationalDate);
            case MY_DEPARTMENT_EMPLOYEES -> loadMyDepartmentEmployeeIds(senderUserId);
            case ALL_DEPARTMENT_COMMANDERS -> loadAllDepartmentCommanderIds();
            case TODAY_DEPARTMENT_COMMANDERS -> loadTodayDepartmentCommanderIds(operationalDate);
            case ALL_EMPLOYEES -> loadAllEmployeeIds();
            case ACTIVE_EMPLOYEES -> loadActiveEmployeeIds();
        };
    }

    private Set<UUID> loadTodayShiftEmployeeIds(LocalDate operationalDate) {
        List<UUID> ids = dataManager.loadValue(
                        "select distinct u.id from Shift s join s.units si join si.user u where s.date = :date",
                        UUID.class)
                .parameter("date", operationalDate)
                .list();
        return new LinkedHashSet<>(ids);
    }

    private Set<UUID> loadMyDepartmentEmployeeIds(UUID senderUserId) {
        ServiceInfo senderServiceInfo = dataManager.load(ServiceInfo.class)
                .query("select si from ServiceInfo si left join fetch si.department where si.user.id = :userId")
                .parameter("userId", senderUserId)
                .optional()
                .orElse(null);
        if (senderServiceInfo == null || senderServiceInfo.getDepartment() == null) {
            return Set.of();
        }

        List<UUID> ids = dataManager.loadValue(
                        "select u.id from User u join u.serviceInfo si "
                                + "where si.department.id = :departmentId "
                                + "and si.post not in :excludedPosts",
                        UUID.class)
                .parameter("departmentId", senderServiceInfo.getDepartment().getId())
                .parameter("excludedPosts", List.of(Post.COM_VZVOD.getId(), Post.ZAM_COM_VZVOD.getId()))
                .list();
        return new LinkedHashSet<>(ids);
    }

    private Set<UUID> loadAllDepartmentCommanderIds() {
        List<UUID> ids = dataManager.loadValue(
                        "select u.id from User u join u.serviceInfo si where si.post = :post",
                        UUID.class)
                .parameter("post", Post.COM_OTD.getId())
                .list();
        return new LinkedHashSet<>(ids);
    }

    private Set<UUID> loadTodayDepartmentCommanderIds(LocalDate operationalDate) {
        int departmentNumber = DepartmentConverter.departmentFromDateToInt(operationalDate);
        List<UUID> ids = dataManager.loadValue(
                        "select u.id from User u join u.serviceInfo si join si.department d "
                                + "where si.post = :post and d.number = :departmentNumber",
                        UUID.class)
                .parameter("post", Post.COM_OTD.getId())
                .parameter("departmentNumber", departmentNumber)
                .list();
        return new LinkedHashSet<>(ids);
    }

    private Set<UUID> loadAllEmployeeIds() {
        List<UUID> ids = dataManager.loadValue(
                        "select u.id from User u join u.serviceInfo si",
                        UUID.class)
                .list();
        return new LinkedHashSet<>(ids);
    }

    private Set<UUID> loadActiveEmployeeIds() {
        List<UUID> ids = dataManager.loadValue(
                        "select u.id from User u join u.serviceInfo si where si.status = :status",
                        UUID.class)
                .parameter("status", StatusInService.ACTIVE.getId())
                .list();
        return new LinkedHashSet<>(ids);
    }
}
