package com.company.vzvod.orientations.dto;

import java.util.List;

public record ScanResponse(String sessionId, List<OrientationDto> orientations) {
}
