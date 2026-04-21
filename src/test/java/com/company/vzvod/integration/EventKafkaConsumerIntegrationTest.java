package com.company.vzvod.integration;

import com.company.vzvod.entity.DeletedEvent;
import com.company.vzvod.entity.Event;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import practice.dto.EventDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест Kafka-консьюмера EventKafkaConsumer")
public class EventKafkaConsumerIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.4"));

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        registry.add("spring.kafka.consumer.group-id", () -> "event-parser-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.key-deserializer", () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer", () -> "org.springframework.kafka.support.serializer.JsonDeserializer");
        registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages", () -> "practice.dto");
        registry.add("spring.kafka.consumer.properties.spring.json.value.default.type", () -> "practice.dto.EventDto");
        registry.add("spring.kafka.consumer.properties.spring.json.use.type.headers", () -> "false");

        registry.add("spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer", () -> "org.springframework.kafka.support.serializer.JsonSerializer");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Kafka container может стартовать чуть раньше, чем listener успевает назначить партиции.
        // Ждём assignment, чтобы отправка сообщения точно попала в активного consumer'а.
        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Сообщение в топике events приводит к созданию/обновлению Event в БД")
    void eventConsumedAndSaved() throws Exception {
        String name = "Зенит - тест матч";
        EventDto dto = new EventDto("Стадион", name, LocalDate.now().plusDays(1), LocalTime.of(19, 30));

        kafkaTemplate.send("events", dto).get();

        Event saved = waitForEventByName(name, 30_000);
        assertNotNull(saved);
        assertEquals(dto.getName(), saved.getName());
        assertEquals(dto.getPlace(), saved.getPlace());
        assertEquals(dto.getDate(), saved.getDate());
        assertEquals(dto.getTime(), saved.getTime());
        assertNotNull(saved.getEventType(), "EventType должен быть выставлен (OTHER или SPORT)");
        assertNotNull(saved.getShiftOfDepartment(), "shiftOfDepartment должен вычисляться из даты");
    }

    @Test
    @DisplayName("Если событие есть в DeletedEvent, то Event не создаётся/не обновляется")
    void deletedEventBlocksCreateOrUpdate() throws Exception {
        String name = "Удалённое событие";

        systemAuthenticator.begin();
        try {
            DeletedEvent deleted = dataManager.create(DeletedEvent.class);
            deleted.setName(name);
            deleted.setDate(LocalDate.now().plusDays(2));
            deleted.setTime(LocalTime.of(12, 0));
            deleted = dataManager.save(deleted);
            assertNotNull(deleted.getId());
        } finally {
            systemAuthenticator.end();
        }

        EventDto dto = new EventDto("Где-то", name, LocalDate.now().plusDays(2), LocalTime.of(12, 0));
        kafkaTemplate.send("events", dto).get();

        assertNull(waitForEventByName(name, 3_000), "Event не должен быть создан, если есть DeletedEvent");
    }

    private Event waitForEventByName(String name, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            UUID id;
            systemAuthenticator.begin();
            try {
                id = findEventIdByNameNative(name);
            } finally {
                systemAuthenticator.end();
            }
            if (id != null) {
                systemAuthenticator.begin();
                try {
                    return dataManager.load(Event.class).id(id).one();
                } finally {
                    systemAuthenticator.end();
                }
            }
            Thread.sleep(200);
        }
        return null;
    }

    private UUID findEventIdByNameNative(String name) {
        Object res = entityManager.createNativeQuery("SELECT ID FROM EVENT WHERE NAME = ? LIMIT 1")
                .setParameter(1, name)
                .getResultStream()
                .findFirst()
                .orElse(null);
        return (UUID) res;
    }
}

