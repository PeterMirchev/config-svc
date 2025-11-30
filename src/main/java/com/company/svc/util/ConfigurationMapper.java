package com.company.svc.util;

import com.company.svc.event.ConfigurationEvent;
import com.company.svc.event.EventType;
import com.company.svc.model.Configuration;
import com.company.svc.web.dto.ConfigurationCollectionResponse;
import com.company.svc.web.dto.ConfigurationCreateRequest;
import com.company.svc.web.dto.ConfigurationResponse;

import java.time.OffsetDateTime;
import java.util.List;

public class ConfigurationMapper {

    public static Configuration mapToConfiguration(ConfigurationCreateRequest request) {

        return Configuration.builder()
                .name(request.getName())
                .application(request.getApplication())
                .environment(request.getEnvironment())
                .version(1)
                .content(request.getContent())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static ConfigurationResponse mapToResponse(Configuration configuration) {

        return ConfigurationResponse.builder()
                .id(configuration.getId())
                .name(configuration.getName())
                .application(configuration.getApplication())
                .environment(configuration.getEnvironment())
                .version(configuration.getVersion())
                .content(configuration.getContent())
                .created(configuration.getCreatedAt())
                .updated(configuration.getUpdatedAt())
                .build();
    }

    public static ConfigurationCollectionResponse mapToCollectionResponse(List<Configuration> configurations) {

        List<ConfigurationResponse> response = configurations
                .stream()
                .map(ConfigurationMapper::mapToResponse)
                .toList();

        return ConfigurationCollectionResponse.builder()
                .configurations(response).build();
    }

    public static ConfigurationEvent mapToConfigurationEvent(Configuration persistedConfiguration, EventType eventType) {

        return ConfigurationEvent.builder()
                .id(persistedConfiguration.getId())
                .name(persistedConfiguration.getName())
                .application(persistedConfiguration.getApplication())
                .environment(persistedConfiguration.getEnvironment())
                .content(persistedConfiguration.getContent())
                .eventType(eventType)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
