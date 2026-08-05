package com.company.vzvod.orientations.client;

import com.company.vzvod.orientations.dto.DocumentFileDto;
import com.company.vzvod.orientations.dto.ScanRequest;
import com.company.vzvod.orientations.dto.ScanResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Клиент orientations-service")
class OrientationsServiceClientTest {

    private MockWebServer server;
    private OrientationsServiceClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        client = new OrientationsServiceClient(RestClient.builder()
                .baseUrl(server.url("/").toString())
                .build());
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    @DisplayName("fetchDefaultPath запрашивает /api/orientations/default-path")
    void fetchDefaultPath() throws Exception {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"path\":\"C:\\\\Users\\\\Admin\\\\Desktop\\\\ОРЕНТИРОВКИ\\\\2026\\\\Июнь\\\\13.06\"}"));

        String path = client.fetchDefaultPath();

        assertThat(path).isEqualTo("C:\\Users\\Admin\\Desktop\\ОРЕНТИРОВКИ\\2026\\Июнь\\13.06");
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/api/orientations/default-path");
        assertThat(request.getMethod()).isEqualTo("GET");
    }

    @Test
    @DisplayName("scan отправляет файлы в /api/orientations/scan")
    void scanPostsFiles() throws Exception {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"sessionId\":\"session-1\",\"orientations\":[{\"fileName\":\"a.docx\",\"text\":\"Текст\",\"images\":[{\"base64\":\"aW1n\",\"contentType\":\"image/png\"}]}]}"));

        ScanResponse response = client.scan(new ScanRequest(null, List.of(
                new DocumentFileDto("a.docx", "Y29udGVudA==")
        )));

        assertThat(response.orientations()).hasSize(1);
        assertThat(response.orientations().get(0).text()).isEqualTo("Текст");
        assertThat(response.orientations().get(0).images()).hasSize(1);
        assertThat(response.orientations().get(0).images().get(0).base64()).isEqualTo("aW1n");
        assertThat(response.sessionId()).isEqualTo("session-1");

        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/api/orientations/scan");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getBody().readUtf8()).contains("a.docx");
    }
}
