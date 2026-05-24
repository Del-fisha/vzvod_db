package com.company.vzvod.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class LoggingServiceClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingServiceClient.class);

    private final RestClient restClient;
    private final String ingestToken;
    private final String deployment;
    private final boolean enabled;
    private final boolean async;
    private final ExecutorService executor;

    @Autowired
    public LoggingServiceClient(
            @Value("${logging.microservice.url:}") String baseUrl,
            @Value("${logging.microservice.token:}") String ingestToken,
            @Value("${logging.microservice.deployment:test-postgres}") String deployment,
            @Value("${logging.microservice.enabled:true}") boolean enabledProperty,
            @Value("${logging.microservice.async:false}") boolean async
    ) {
        String url = baseUrl == null ? "" : baseUrl.trim();
        this.enabled = enabledProperty && !url.isEmpty();
        this.ingestToken = ingestToken == null ? "" : ingestToken.trim();
        this.deployment = deployment == null || deployment.isBlank() ? "test-postgres" : deployment.trim();
        this.async = async;
        this.restClient = this.enabled ? RestClient.builder().baseUrl(url).build() : null;
        this.executor = async ? Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "logging-client");
            t.setDaemon(true);
            return t;
        }) : null;
    }

    public LoggingServiceClient(RestClient restClient, String ingestToken, String deployment, boolean enabled, boolean async) {
        this.restClient = restClient;
        this.ingestToken = ingestToken == null ? "" : ingestToken;
        this.deployment = deployment == null || deployment.isBlank() ? "test-postgres" : deployment.trim();
        this.enabled = enabled;
        this.async = async;
        this.executor = async ? Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "logging-client");
            t.setDaemon(true);
            return t;
        }) : null;
    }

    public void logMain(String message) {
        send("CORE", "MAIN", message);
    }

    public void logError(String message) {
        send("CORE", "ERROR", message);
    }

    private void send(String service, String category, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        if (!enabled) {
            log.warn("logging-service отключён (logging.microservice.url пустой), запись не отправлена [{}]: {}",
                    category, message.lines().findFirst().orElse(message));
            return;
        }
        Runnable task = () -> doSend(service, category, message.trim());
        if (async) {
            executor.execute(task);
        } else {
            task.run();
        }
    }

    private void doSend(String service, String category, String message) {
        try {
            RestClient.RequestBodySpec spec = restClient.post()
                    .uri("/api/logs")
                    .contentType(MediaType.APPLICATION_JSON);
            if (!ingestToken.isEmpty()) {
                spec = spec.header("X-Internal-Token", ingestToken);
            }
            spec.body(new RemoteLogPayload(service, category, message, deployment))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Не удалось отправить запись в logging-service: {}", e.toString());
        }
    }

    private record RemoteLogPayload(String service, String category, String message, String deployment) {
    }
}
