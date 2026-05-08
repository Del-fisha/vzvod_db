package com.company.vzvod.service.raport;

import com.company.vzvod.service.dto.raport.CompensatoryTimeRaportDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class CompensatoryTimeRaportSender {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CompensatoryTimeRaportSender(
            @Value("${raport.microservice.url}") String baseUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public void sendOtgulRaport(CompensatoryTimeRaportDto raport) {
        String url = baseUrl + "/api/reports/otgul";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CompensatoryTimeRaportDto> request = new HttpEntity<>(raport, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to send raport, status = " + response.getStatusCode());
        }
    }

    public byte[] sendOtgulRaportPdf(CompensatoryTimeRaportDto raport) {
        String url = baseUrl + "/api/reports/otgul/pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_PDF));

        HttpEntity<CompensatoryTimeRaportDto> request = new HttpEntity<>(raport, headers);

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