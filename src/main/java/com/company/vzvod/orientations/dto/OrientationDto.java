package com.company.vzvod.orientations.dto;

import java.util.List;

public record OrientationDto(
        String fileName,
        String text,
        List<OrientationImageDto> images
) {
}
