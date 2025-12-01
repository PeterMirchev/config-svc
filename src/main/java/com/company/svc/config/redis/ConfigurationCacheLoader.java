package com.company.svc.config.redis;

import com.company.svc.model.Configuration;
import com.company.svc.repository.ConfigurationRepository;
import com.company.svc.service.RedisCacheService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConfigurationCacheLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationCacheLoader.class);

    private final ConfigurationRepository configurationRepository;
    private final RedisCacheService cacheService;

    @PostConstruct
    public void loadCache() {

        logger.info("Loading configurations into Redis cache...");

        int count = 0;
        for (Configuration config : configurationRepository.findAll()) {
            cacheService.put(config);
            count++;
        }

        logger.info("Loaded {} configurations into Redis cache", count);
    }
}
