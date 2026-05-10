package com.company.vzvod.service.raport;

import com.company.vzvod.service.dto.raport.ServiceBookSupplementDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class ServiceBookSupplementSender {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ServiceBookSupplementSender(@Value("${raport.microservice.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    private static String normalizeBaseUrl(String raw) {
        if (raw == null) {
            return "";
        }
        String b = raw.trim();
        while (b.endsWith("/")) {
            b = b.substring(0, b.length() - 1);
        }
        return b;
    }

    public void send(ServiceBookSupplementDto payload) {
        String url = baseUrl + "/api/reports/service-book";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ServiceBookSupplementDto> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to send raport, status = " + response.getStatusCode());
        }
    }

    public byte[] sendPdf(ServiceBookSupplementDto payload) {
        String url = baseUrl + "/api/reports/service-book/pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_PDF));

        HttpEntity<ServiceBookSupplementDto> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.postForEntity(url, request, byte[].class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Failed to get PDF, status = " + response.getStatusCode());
            }
            return response.getBody() == null ? new byte[0] : response.getBody();
        } catch (HttpStatusCodeException e) {
            throw e;
        }
    }
}

