package com.company.vzvod.logging;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Клиент logging-service")
class LoggingServiceClientTest {

    private MockWebServer server;
    private LoggingServiceClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse().setResponseCode(204));

        RestClient restClient = RestClient.builder().baseUrl(server.url("/").toString()).build();
        client = new LoggingServiceClient(restClient, "", "test-postgres", true, false);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    @DisplayName("logMain отправляет JSON в /api/logs синхронно")
    void logMain_postsPayload() throws Exception {
        client.logMain("Иванов И. О. изменил \"фамилия\" у сотрудника Петров П. П.");

        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request.getPath()).isEqualTo("/api/logs");
        assertThat(request.getBody().readUtf8())
                .contains("\"service\":\"CORE\"")
                .contains("\"category\":\"MAIN\"")
                .contains("\"deployment\":\"test-postgres\"")
                .contains("изменил");
    }
}
