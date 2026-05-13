package com.company.vzvod.messaging.client;

import com.company.vzvod.messaging.dto.DashboardMessageDispatchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationServiceClient {

    private final RestClient restClient;

    public NotificationServiceClient(@Value("${notification.microservice.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void sendDashboardMessage(DashboardMessageDispatchRequest request) {
        restClient.post()
                .uri("/api/notifications/dashboard-message")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
