package com.company.sdk;

import com.company.svc.web.dto.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

public class ConfigurationClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    public ConfigurationClient(String baseUrl, RestTemplate restTemplate) {

        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restTemplate = restTemplate;
    }

    public ConfigurationResponse createConfiguration(ConfigurationCreateRequest request) {

        return restTemplate.postForObject(baseUrl, request, ConfigurationResponse.class);
    }

    public ConfigurationResponse updateConfiguration(UUID configurationId, ConfigurationUpdateRequest request) {

        restTemplate.put(baseUrl  + "/" + configurationId, request);
        return getConfiguration(configurationId);
    }

    public ConfigurationResponse patchConfiguration(UUID configurationId, ConfigurationPatchRequest request) {

        HttpEntity<ConfigurationPatchRequest> entity = new HttpEntity<>(request);
        ResponseEntity<ConfigurationResponse> response = restTemplate.exchange(
                baseUrl + "/" + configurationId,
                HttpMethod.PATCH,
                entity,
                ConfigurationResponse.class
        );

        return response.getBody();
    }

    public ConfigurationResponse getConfiguration(UUID configurationId) {

        return restTemplate.getForObject(baseUrl + "/" + configurationId, ConfigurationResponse.class);
    }

    public ConfigurationCollectionResponse getConfigurations(String name, String application, String environment) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/");
        if (name != null) {
            builder.queryParam("name", name);
        }
        if (application != null) {
            builder.queryParam("application", application);
        }
        if (environment != null) {
            builder.queryParam("environment", environment);
        }

        return restTemplate.getForObject(builder.toUriString(), ConfigurationCollectionResponse.class);
    }

    public void deleteConfiguration(UUID configurationId) {

        restTemplate.delete(baseUrl + "/" + configurationId);
    }
}
