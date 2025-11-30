package com.company.svc.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ConfigurationResponse {

    private UUID id;
    private String name;
    private String application;
    private String environment;
    private Integer version;
    private String content;
    private OffsetDateTime created;
    private OffsetDateTime updated;
}
