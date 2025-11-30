package com.company.svc.web;

import com.company.svc.model.Configuration;
import com.company.svc.service.ConfigurationService;
import com.company.svc.util.ConfigurationMapper;
import com.company.svc.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.company.svc.web.ApiConstants.EndpointPaths.CONFIGURATIONS;
import static com.company.svc.web.ApiConstants.Versions.V1;

@RestController
@RequestMapping(V1 + CONFIGURATIONS)
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostMapping
    public ResponseEntity<ConfigurationResponse> createConfiguration(@RequestBody @Valid ConfigurationCreateRequest request) {

        Configuration configuration = configurationService.create(request);
        ConfigurationResponse response = ConfigurationMapper.mapToResponse(configuration);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{configurationId}")
    public ResponseEntity<ConfigurationResponse> updateConfiguration(@RequestBody @Valid ConfigurationUpdateRequest request,
                                                                     @PathVariable UUID configurationId) {

        Configuration configuration = configurationService.update(request, configurationId);
        ConfigurationResponse response = ConfigurationMapper.mapToResponse(configuration);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{configurationId}")
    public ResponseEntity<ConfigurationResponse> patchConfiguration(@RequestBody ConfigurationPatchRequest request,
                                                                    @PathVariable UUID configurationId) {

        Configuration configuration = configurationService.patch(request, configurationId);
        ConfigurationResponse response = ConfigurationMapper.mapToResponse(configuration);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{configurationId}")
    public ResponseEntity<ConfigurationResponse> getConfiguration(@PathVariable UUID configurationId) {

        Configuration configuration = configurationService.getById(configurationId);
        ConfigurationResponse response = ConfigurationMapper.mapToResponse(configuration);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<ConfigurationCollectionResponse> getConfigurations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String application,
            @RequestParam(required = false) String environment) {

        List<Configuration> configurations = configurationService.findConfigurations(name, application, environment);
        ConfigurationCollectionResponse response = ConfigurationMapper.mapToCollectionResponse(configurations);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{configurationId}")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable UUID configurationId) {

        configurationService.delete(configurationId);
        return ResponseEntity.noContent().build();
    }
}
