package com.company.svc.service;

import com.company.svc.event.ConfigurationEvent;
import com.company.svc.event.EventType;
import com.company.svc.exception.ResourceNotFoundException;
import com.company.svc.repository.ConfigurationRepository;
import com.company.svc.model.Configuration;
import com.company.svc.util.ConfigurationMapper;
import com.company.svc.web.dto.ConfigurationCreateRequest;
import com.company.svc.web.dto.ConfigurationPatchRequest;
import com.company.svc.web.dto.ConfigurationUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    private final ConfigurationRepository configurationRepository;
    private final ConfigurationEventPublisher eventPublisher;

    public ConfigurationService(ConfigurationRepository configurationRepository, ConfigurationEventPublisher eventPublisher) {
        this.configurationRepository = configurationRepository;
        this.eventPublisher = eventPublisher;
    }

    public Configuration create(ConfigurationCreateRequest request) {

        Configuration configuration = ConfigurationMapper.mapToConfiguration(request);

        Configuration persistedConfiguration = configurationRepository.save(configuration);
        logger.info("Configuration created with ID [{}] ", persistedConfiguration.getId());

        ConfigurationEvent configurationEvent = ConfigurationMapper.mapToConfigurationEvent(persistedConfiguration, EventType.CREATED);
        eventPublisher.publish(configurationEvent);

        return persistedConfiguration;
    }

    public Configuration getById(UUID configurationId) {

        return configurationRepository.findById(configurationId)
                .orElseThrow(() -> {
                    logger.warn("Configuration with ID [{}] not found.", configurationId);
                    return new ResourceNotFoundException(String.format("Configuration with ID [%s] not found.", configurationId));
                });
    }

    public List<Configuration> getAll() {

        return configurationRepository.findAll();
    }

    public List<Configuration> getByName(String name) {

        List<Configuration> configurations = configurationRepository.findAllByName(name);

        if (configurations.isEmpty()) {
            logger.warn("Configuration with name [{}] not found.", name);
            throw new ResourceNotFoundException(String.format("Configuration with name [%s] not found.", name));
        }

        return configurations;
    }

    public List<Configuration> findConfigurations(String name, String application, String environment) {

        if (name != null && application != null && environment != null) {

            return getByApplicationAndEnvironmentAndName(application, environment, name);
        } else if (name != null) {
            return getByName(name);
        } else if (application != null && environment != null) {
            return getByApplicationAndEnvironment(application, environment);
        } else {
            return getAll();
        }
    }

    public List<Configuration> getByApplicationAndEnvironmentAndName(String application, String environment, String name) {

        List<Configuration> configurations = configurationRepository.findByApplicationAndEnvironmentAndName(application, environment, name);
        if (configurations.isEmpty()) {
            logger.warn("Configuration with application [{}] and environment [{}] and name [{}] not found.",
                    application, environment, name);
            throw new ResourceNotFoundException(
                    String.format("Configuration with application [%s] and environment [%s] and name [%s] not found.",
                            application, environment, name));
        }

        return configurations;
    }

    public List<Configuration> getByApplicationAndEnvironment(String application, String environment) {

        List<Configuration> configurations = configurationRepository.findByApplicationAndEnvironment(application, environment);

        if (configurations.isEmpty()) {
            logger.warn("Configuration with application [{}] and environment [{}] not found.", application, environment);
            throw new ResourceNotFoundException(String.format("Configuration with application [%s] and environment [%s] not found.", application, environment));
        }

        return configurations;
    }

    public Configuration update(ConfigurationUpdateRequest request, UUID configurationId) {

        Configuration configuration = getById(configurationId);

        applyUpdate(configuration, request);

        Configuration updatedConfiguration = configurationRepository.save(configuration);
        logger.info("Configuration updated with ID [{}] ", updatedConfiguration.getId());

        ConfigurationEvent configurationEvent = ConfigurationMapper.mapToConfigurationEvent(updatedConfiguration, EventType.UPDATED);
        eventPublisher.publish(configurationEvent);

        return updatedConfiguration;
    }

    public Configuration patch(ConfigurationPatchRequest request, UUID configurationId) {

        Configuration configuration = getById(configurationId);

        applyPatch(configuration, request);

        Configuration updatedConfiguration = configurationRepository.save(configuration);
        logger.info("Configuration partially updated with ID [{}] ", updatedConfiguration.getId());

        ConfigurationEvent configurationEvent = ConfigurationMapper.mapToConfigurationEvent(updatedConfiguration, EventType.UPDATED);
        eventPublisher.publish(configurationEvent);

        return updatedConfiguration;
    }

    public void delete(UUID configurationId) {

        Configuration configuration = getById(configurationId);
        configurationRepository.delete(configuration);
        logger.info("Configuration with ID [{}] has been deleted.", configurationId);

        ConfigurationEvent configurationEvent = ConfigurationMapper.mapToConfigurationEvent(configuration, EventType.DELETED);
        eventPublisher.publish(configurationEvent);
    }

    private void applyPatch(Configuration configuration, ConfigurationPatchRequest request) {

        if (request.getName() != null) {
            configuration.setName(request.getName());
        }
        if (request.getApplication() != null) {
            configuration.setApplication(request.getApplication());
        }
        if (request.getEnvironment() != null) {
            configuration.setEnvironment(request.getEnvironment());
        }
        if (request.getContent() != null) {
            configuration.setContent(request.getContent());
        }
        configuration.setVersion(configuration.getVersion() + 1);
        configuration.setUpdatedAt(OffsetDateTime.now());
    }


    private void applyUpdate(Configuration configuration, ConfigurationUpdateRequest request) {

        configuration.setName(request.getName());
        configuration.setApplication(request.getApplication());
        configuration.setEnvironment(request.getEnvironment());
        configuration.setVersion(configuration.getVersion() + 1);
        configuration.setContent(request.getContent());
        configuration.setUpdatedAt(OffsetDateTime.now());
    }
}
