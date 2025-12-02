package com.company.svc.service;

import com.company.svc.model.Configuration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RedisCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(1);

    public RedisCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void put(Configuration configuration) {

        try {
            String json = objectMapper.writeValueAsString(configuration);

            try {
                redisTemplate.opsForValue().set(key(configuration.getId()), json, DEFAULT_TTL);
            } catch (RedisSystemException e) {
                logger.warn("Redis unavailable while storing configuration [id={}]. Proceeding without cache.",
                        configuration.getId(), e);
                return;
            }

            logger.info("Stored configuration in Redis [id={}, name={}, app={}, env={}]",
                    configuration.getId(), configuration.getName(),
                    configuration.getApplication(), configuration.getEnvironment());

        } catch (JsonProcessingException e) {
            logger.error("Could not serialize configuration [id={}] to JSON for Redis.", configuration.getId(), e);
            throw new RuntimeException("Failed to serialize Configuration for Redis", e);
        }
    }

    public Configuration get(UUID configurationId) {
        String json;
        try {
            json = redisTemplate.opsForValue().get(key(configurationId));
        } catch (RedisSystemException e) {
            // Redis down → degrade to no-cache
            logger.warn("Redis unavailable while reading configuration [id={}]. Returning cache miss.",
                    configurationId, e);
            return null;
        }

        if (json == null) {
            logger.info("Cache miss for configuration id [{}]", configurationId);
            return null;
        }

        try {
            Configuration configuration = objectMapper.readValue(json, Configuration.class);
            logger.info("Cache hit for configuration id [{}]", configurationId);
            return configuration;
        } catch (JsonProcessingException e) {
            // Corrupted cache → evict and fallback to DB
            evict(configurationId);
            logger.error("Failed to parse cached Configuration [id={}] - evicted entry", configurationId, e);
            return null;
        }
    }

    public void evict(UUID configurationId) {
        logger.info("Evicting configuration from Redis [id={}]", configurationId);
        try {
            redisTemplate.delete(key(configurationId));
        } catch (RedisSystemException e) {
            // If Redis is down, nothing to evict — ignore
            logger.warn("Redis unavailable while evicting configuration [id={}]. Ignoring.", configurationId, e);
        }
    }

    private String key(UUID configurationId) {

        return "config:" + configurationId;
    }
}
