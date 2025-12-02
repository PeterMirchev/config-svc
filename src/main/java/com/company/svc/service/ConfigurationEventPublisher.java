package com.company.svc.service;

import com.company.svc.event.ConfigurationEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ConfigurationEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationEventPublisher.class);

    private final KafkaTemplate<String, ConfigurationEvent> kafkaTemplate;

    private static final String TOPIC = "configuration-updates";

    @Retryable(include = {Exception.class}, maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2.0))
    public void publish(ConfigurationEvent event) {

        try {
            var future = kafkaTemplate.send(TOPIC, event);
            var result = future.get();
            logger.info("ConfigurationEventPublisher sent to TOPIC {} partition {} offset {} with EVENT {}",
                    TOPIC,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    event);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while sending Kafka message", ie);
        } catch (ExecutionException ee) {
            throw new RuntimeException("Kafka send failed", ee.getCause() != null ? ee.getCause() : ee);
        }
    }

    @Recover
    public void recover(Exception ex, ConfigurationEvent event) {

        logger.error("Failed to publish ConfigurationEvent to Kafka after retries. Event={}"
                + ", reason={}", event, ex.toString());
    }
}
