package com.company.vzvod.orientations;

import com.company.vzvod.orientations.client.OrientationsServiceClient;
import com.company.vzvod.orientations.dto.DocumentFileDto;
import com.company.vzvod.orientations.dto.ScanRequest;
import com.company.vzvod.orientations.dto.ScanResponse;
import com.company.vzvod.security.UiAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrientationsBrowseService {

    private final OrientationsServiceClient client;
    private final UiAccessService uiAccessService;

    public OrientationsBrowseService(OrientationsServiceClient client, UiAccessService uiAccessService) {
        this.client = client;
        this.uiAccessService = uiAccessService;
    }

    public String fetchDefaultPath() {
        requireFullAccess();
        return client.fetchDefaultPath();
    }

    public ScanResponse scan(String sessionId, List<DocumentFileDto> files) {
        requireFullAccess();
        ScanResponse response = client.scan(new ScanRequest(sessionId, files));
        if (response == null) {
            return new ScanResponse(sessionId, List.of());
        }
        if (response.orientations() == null) {
            return new ScanResponse(response.sessionId(), List.of());
        }
        return response;
    }

    private void requireFullAccess() {
        if (!uiAccessService.hasFullAccessRole()) {
            throw new AccessDeniedException("Orientations require FullAccessRole");
        }
    }
}
