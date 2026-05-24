package com.company.vzvod.util;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.Rank;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;

import java.util.Comparator;
import java.util.Locale;

/**
 * Единый порядок списков сотрудников: должность, звание (убыв.), фамилия.
 */
public final class EmployeeOrdering {

    public static final String JPQL_USER_SUFFIX =
            "order by u.serviceInfo.post, u.serviceInfo.rank desc, u.lastName";

    public static final String JPQL_SERVICE_INFO_SUFFIX =
            "order by e.post, e.rank desc, e.user.lastName";

    private EmployeeOrdering() {
    }

    public static Comparator<ServiceInfo> serviceInfoComparator() {
        return Comparator
                .comparing(EmployeeOrdering::postSortKey, Comparator.nullsLast(String::compareTo))
                .thenComparing(EmployeeOrdering::rankSortKey, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(si -> lastNameSortKey(si.getUser()), Comparator.nullsLast(String::compareTo));
    }

    public static Comparator<User> userComparator() {
        return Comparator
                .comparing((User u) -> postSortKey(u.getServiceInfo()), Comparator.nullsLast(String::compareTo))
                .thenComparing(u -> rankSortKey(u.getServiceInfo()), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(u -> lastNameSortKey(u), Comparator.nullsLast(String::compareTo));
    }

    public static void sortServiceInfosInPlace(Iterable<ServiceInfo> items) {
        if (items instanceof java.util.List<ServiceInfo> list) {
            list.sort(serviceInfoComparator());
        }
    }

    private static String postSortKey(ServiceInfo si) {
        if (si == null) {
            return null;
        }
        Post post = si.getPost();
        return post == null ? null : post.getId();
    }

    private static String rankSortKey(ServiceInfo si) {
        if (si == null) {
            return null;
        }
        Rank rank = si.getRank();
        return rank == null ? null : rank.getId();
    }

    private static String lastNameSortKey(User user) {
        if (user == null || user.getLastName() == null) {
            return null;
        }
        return user.getLastName().trim().toLowerCase(Locale.ROOT);
    }
}
