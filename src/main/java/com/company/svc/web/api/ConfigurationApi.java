package com.company.svc.web.api;

import com.company.svc.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.company.svc.web.api.ApiConstants.EndpointPaths.CONFIGURATIONS;
import static com.company.svc.web.api.ApiConstants.Versions.V1;

@RequestMapping(V1 + CONFIGURATIONS)
@Tag(name = "Configuration API", description = "Manage configurations for applications")
public interface ConfigurationApi {

    @PostMapping
    @Operation(summary = "Create a new configuration",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Configuration created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            })
    ResponseEntity<ConfigurationResponse> createConfiguration(
            @RequestBody @Valid ConfigurationCreateRequest request);

    @PutMapping("/{configurationId}")
    @Operation(summary = "Update a configuration by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Configuration updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Configuration not found")
            })
    ResponseEntity<ConfigurationResponse> updateConfiguration(
            @RequestBody @Valid ConfigurationUpdateRequest request,
            @PathVariable UUID configurationId);

    @PatchMapping("/{configurationId}")
    @Operation(summary = "Patch a configuration by ID")
    ResponseEntity<ConfigurationResponse> patchConfiguration(
            @RequestBody ConfigurationPatchRequest request,
            @PathVariable UUID configurationId);

    @GetMapping("/{configurationId}")
    @Operation(summary = "Get a configuration by ID")
    ResponseEntity<ConfigurationResponse> getConfiguration(@PathVariable UUID configurationId);

    @GetMapping
    @Operation(summary = "Find configurations with optional filters")
    ResponseEntity<ConfigurationCollectionResponse> getConfigurations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String application,
            @RequestParam(required = false) String environment);

    @DeleteMapping("/{configurationId}")
    @Operation(summary = "Delete configuration by ID")
    ResponseEntity<Void> deleteConfiguration(@PathVariable UUID configurationId);
}
