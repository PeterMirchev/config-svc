package com.company.svc.service;

import com.company.svc.repository.ConfigurationRepository;
import com.company.svc.event.ConfigurationEvent;
import com.company.svc.event.EventType;
import com.company.svc.model.Configuration;
import com.company.svc.web.dto.ConfigurationCreateRequest;
import com.company.svc.web.dto.ConfigurationPatchRequest;
import com.company.svc.web.dto.ConfigurationUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ConfigurationServiceTest {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @MockBean
    private ConfigurationEventPublisher configurationEventPublisher;

    @MockBean
    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {

        configurationRepository.deleteAll();
    }

    @Test
    @DisplayName("create() should persist configuration, cache it and publish event")
    void createPersistCachesAndPublishes_thenHappyPath() {

        ConfigurationCreateRequest request = ConfigurationCreateRequest.builder()
                .name("feature.toggle")
                .application("payments")
                .environment("dev")
                .content("test content")
                .build();

        Configuration created = configurationService.create(request);


        assertNotNull(created.getId());
        assertEquals(1, created.getVersion());

        Optional<Configuration> fetched = configurationRepository.findById(created.getId());
        assertNotNull(fetched);

        verify(redisCacheService).put(argThat(c -> c.getId().equals(created.getId())));

        ArgumentCaptor<ConfigurationEvent> captor = ArgumentCaptor.forClass(ConfigurationEvent.class);
        verify(configurationEventPublisher).publish(captor.capture());
        ConfigurationEvent event = captor.getValue();

        assertEquals(EventType.CREATED, event.getEventType());
        assertEquals(created.getId(), event.getId());
    }

    @Test
    @DisplayName("getById() should return from cache when present")
    void getByIdReturnsFromCache_thenHappyPath() {


        Configuration cached = Configuration.builder()
                .name("n1")
                .application("app")
                .environment("dev")
                .version(1)
                .content("content").build();

        UUID id = UUID.randomUUID();
        cached.setId(id);
        cached.setCreatedAt(OffsetDateTime.now());
        cached.setUpdatedAt(OffsetDateTime.now());

        when(redisCacheService.get(id)).thenReturn(cached);

        Configuration result = configurationService.getById(id);

        assertEquals(result,cached);

        verify(redisCacheService, never()).put(any());
    }

    @Test
    @DisplayName("getById() should load from DB, cache it when cache miss")
    void getByIdLoadsFromDBWhenMissingOnCache_thenHappyPath() {

        ConfigurationCreateRequest req = ConfigurationCreateRequest.builder()
                .name("db.config")
                .application("portal")
                .environment("test")
                .content("content")
                .build();
        Configuration persisted = configurationService.create(req);

        reset(redisCacheService, configurationEventPublisher);


        when(redisCacheService.get(persisted.getId())).thenReturn(null);

        Configuration found = configurationService.getById(persisted.getId());

        assertEquals(found.getId(),persisted.getId());
        verify(redisCacheService, times(1)).put(any());
        verify(redisCacheService).put(argThat(c -> c.getId().equals(persisted.getId())));
    }

    @Test
    @DisplayName("findConfigurations() delegates based on provided filters")
    void findConfigurationsDelegatesByFilters_thenHappyPath() {

        configurationService.create(ConfigurationCreateRequest.builder()
                .name("alpha")
                .application("svc")
                .environment("dev")
                .content("content")
                .build());
        configurationService.create(ConfigurationCreateRequest.builder()
                .name("beta")
                .application("svc")
                .environment("prod")
                .content("content")
                .build());
        configurationService.create(ConfigurationCreateRequest.builder()
                .name("alpha")
                .application("web")
                .environment("dev")
                .content("content")
                .build());

        List<Configuration> threeElements = configurationService.findConfigurations(null, null, null);
        assertEquals(3, threeElements.size());

        List<Configuration> alpha = configurationService.findConfigurations("alpha", null, null);
        assertEquals(2, alpha.size());

        List<Configuration> oneResult = configurationService.findConfigurations(null, "svc", "prod");
        assertEquals(1, oneResult.size());

        List<Configuration> allParams = configurationService.findConfigurations("alpha", "svc", "dev");
        assertEquals(1, allParams.size());
    }

    @Test
    @DisplayName("update() should update entity and publish UPDATED event")
    void update_updates_and_publishes() {

        Configuration created = configurationService.create(ConfigurationCreateRequest.builder()
                .name("n")
                .application("a")
                .environment("e")
                .content("c1")
                .build());

        Mockito.reset(configurationEventPublisher, redisCacheService);

        ConfigurationUpdateRequest update = new ConfigurationUpdateRequest("n2", "a2", "e2", "c2");
        Configuration updated = configurationService.update(update, created.getId());

        assertEquals(2, updated.getVersion());
        assertEquals("n2", updated.getName());
        assertEquals("a2", updated.getApplication());
        assertEquals("e2", updated.getEnvironment());
        assertEquals("c2", updated.getContent());

        verify(redisCacheService, times(2))
                .put(argThat(c -> c.getId().equals(created.getId())));

        ArgumentCaptor<ConfigurationEvent> captor = ArgumentCaptor.forClass(ConfigurationEvent.class);
        verify(configurationEventPublisher).publish(captor.capture());
        assertEquals(EventType.UPDATED, captor.getValue().getEventType());
    }

    @Test
    @DisplayName("patch() should partially update entity and publish UPDATED event")
    void patch_updates_and_publishes() {

        Configuration created = configurationService.create(ConfigurationCreateRequest.builder()
                .name("n")
                .application("a")
                .environment("e")
                .content("c1")
                .build());

        reset(configurationEventPublisher, redisCacheService);

        ConfigurationPatchRequest patch = new ConfigurationPatchRequest();
        patch.setContent("c3");
        Configuration updated = configurationService.patch(patch, created.getId());

        assertEquals(2, updated.getVersion());
        assertEquals("c3", updated.getContent());
        assertEquals("n", updated.getName());


        verify(redisCacheService, times(2))
                .put(argThat(c -> c.getId().equals(created.getId())));
        verify(configurationEventPublisher).publish(argThat(ev -> ev.getEventType() == EventType.UPDATED));
    }

    @Test
    @DisplayName("delete() should remove entity and publish DELETED event")
    void delete_removes_and_publishes() {

        Configuration created = configurationService.create(ConfigurationCreateRequest.builder()
                .name("n")
                .application("a")
                .environment("e")
                .content("c1")
                .build());

        reset(configurationEventPublisher, redisCacheService);

        configurationService.delete(created.getId());

        verify(redisCacheService).evict(created.getId());
        verify(configurationEventPublisher).publish(argThat(ev -> ev.getEventType() == EventType.DELETED));
    }
}
