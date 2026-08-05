package com.company.vzvod.orientations;

import com.company.vzvod.orientations.dto.OrientationDto;
import com.company.vzvod.orientations.dto.OrientationImageDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class OrientationsListUpdater {

    public record UpdateResult(List<OrientationDto> orientations, int currentIndex, boolean changed) {
    }

    private OrientationsListUpdater() {
    }

    public static UpdateResult merge(
            List<OrientationDto> previous,
            int previousIndex,
            List<OrientationDto> scanned
    ) {
        List<OrientationDto> next = scanned == null ? List.of() : List.copyOf(scanned);
        if (sameOrientations(previous, next)) {
            return new UpdateResult(previous == null ? List.of() : new ArrayList<>(previous), previousIndex, false);
        }

        String currentKey = orientationKeyAt(previous, previousIndex);
        int nextIndex = findIndexByKey(next, currentKey);
        if (nextIndex < 0) {
            nextIndex = clampIndex(previousIndex, next.size());
        }
        return new UpdateResult(next, nextIndex, true);
    }

    static String orientationKey(OrientationDto orientation) {
        if (orientation == null) {
            return "";
        }
        StringBuilder imagePart = new StringBuilder();
        if (orientation.images() != null) {
            for (OrientationImageDto image : orientation.images()) {
                imagePart.append(':');
                imagePart.append(image.base64() == null ? 0 : image.base64().length());
            }
        }
        return orientation.fileName() + "|" + orientation.text() + "|" + imagePart;
    }

    private static String orientationKeyAt(List<OrientationDto> orientations, int index) {
        if (orientations == null || index < 0 || index >= orientations.size()) {
            return null;
        }
        return orientationKey(orientations.get(index));
    }

    private static int findIndexByKey(List<OrientationDto> orientations, String key) {
        if (key == null || orientations == null) {
            return -1;
        }
        for (int i = 0; i < orientations.size(); i++) {
            if (key.equals(orientationKey(orientations.get(i)))) {
                return i;
            }
        }
        return -1;
    }

    private static int clampIndex(int previousIndex, int size) {
        if (size <= 0) {
            return -1;
        }
        if (previousIndex < 0) {
            return 0;
        }
        return Math.min(previousIndex, size - 1);
    }

    private static boolean sameOrientations(List<OrientationDto> left, List<OrientationDto> right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.size() != right.size()) {
            return false;
        }
        for (int i = 0; i < left.size(); i++) {
            if (!Objects.equals(orientationKey(left.get(i)), orientationKey(right.get(i)))) {
                return false;
            }
        }
        return true;
    }
}
