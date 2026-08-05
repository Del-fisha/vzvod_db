package com.company.vzvod.orientations.dto;

import java.util.List;

public record ScanRequest(String sessionId, List<DocumentFileDto> files) {
}
