package com.company.vzvod.orientations.client;

import com.company.vzvod.orientations.dto.DefaultPathResponse;
import com.company.vzvod.orientations.dto.ScanRequest;
import com.company.vzvod.orientations.dto.ScanResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrientationsServiceClient {

    private final RestClient restClient;

    @Autowired
    public OrientationsServiceClient(@Value("${orientations.microservice.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    OrientationsServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public String fetchDefaultPath() {
        DefaultPathResponse response = restClient.get()
                .uri("/api/orientations/default-path")
                .retrieve()
                .body(DefaultPathResponse.class);
        return response != null ? response.path() : "";
    }

    public ScanResponse scan(ScanRequest request) {
        return restClient.post()
                .uri("/api/orientations/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ScanResponse.class);
    }
}
