package com.company.svc.event;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ConfigurationEvent {

    private UUID id;
    private String name;
    private String application;
    private String environment;
    private String content;
    private EventType eventType;
    private OffsetDateTime timestamp;
}
