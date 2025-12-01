package com.company.svc.web;

import com.company.svc.model.Configuration;
import com.company.svc.service.ConfigurationService;
import com.company.svc.util.ConfigurationMapper;
import com.company.svc.web.api.ConfigurationApi;
import com.company.svc.web.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class ConfigurationController implements ConfigurationApi {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public ResponseEntity<ConfigurationResponse> createConfiguration(ConfigurationCreateRequest request) {

        Configuration configuration = configurationService.create(request);
        ConfigurationResponse response = ConfigurationMapper.mapToResponse(configuration);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ConfigurationResponse> updateConfiguration(ConfigurationUpdateRequest request, UUID configurationId) {

        Configuration configuration = configurationService.update(request, configurationId);
        ConfigurationResponse response = ConfigurationMapper.mapToResponse(configuration);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ConfigurationResponse> patchConfiguration(ConfigurationPatchRequest request, UUID configurationId) {

        Configuration configuration = configurationService.patch(request, configurationId);
        ConfigurationResponse response = ConfigurationMapper.mapToResponse(configuration);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ConfigurationResponse> getConfiguration(UUID configurationId) {

        Configuration configuration = configurationService.getById(configurationId);
        ConfigurationResponse response = ConfigurationMapper.mapToResponse(configuration);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ConfigurationCollectionResponse> getConfigurations(String name, String application, String environment) {

        List<Configuration> configurations = configurationService.findConfigurations(name, application, environment);
        ConfigurationCollectionResponse response = ConfigurationMapper.mapToCollectionResponse(configurations);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteConfiguration(UUID configurationId) {

        configurationService.delete(configurationId);
        return ResponseEntity.noContent().build();
    }
}
