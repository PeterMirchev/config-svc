package com.company.svc.service;

import com.company.svc.event.ConfigurationEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigurationEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationEventPublisher.class);

    private final KafkaTemplate<String, ConfigurationEvent> kafkaTemplate;

    private static final String TOPIC = "configuration-updates";

    public void publish(ConfigurationEvent event) {

        kafkaTemplate.send(TOPIC, event);
        logger.info("ConfigurationEventPublisher sent to TOPIC {} with EVENT {}", TOPIC, event);
    }
}
