package com.company.vzvod.service.raport;

import com.company.vzvod.service.dto.raport.DailyShiftRaportDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DailyShiftRaportSender {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public DailyShiftRaportSender(@Value("${raport.microservice.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public void sendDailyShiftRaport(DailyShiftRaportDto raport) {
        String url = baseUrl + "/api/reports/sutki";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DailyShiftRaportDto> request = new HttpEntity<>(raport, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to send raport, status = " + response.getStatusCode());
        }
    }
}

