package com.company.sdk;

import com.company.svc.service.ConfigurationEventPublisher;
import com.company.svc.service.RedisCacheService;
import com.company.svc.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = com.company.svc.Application.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ConfigurationClientTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private RestTemplate restTemplate;

    private ConfigurationClient client;

    @MockBean
    private ConfigurationEventPublisher configurationEventPublisher;

    @MockBean
    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {

        this.restTemplate = testRestTemplate.getRestTemplate();
        String baseUrl = "http://localhost:" + port + "/api/v1/configurations";
        this.client = new ConfigurationClient(baseUrl, restTemplate);
    }

    @Test
    @DisplayName("createConfiguration() posts request and returns response body")
    void createConfigurationPostsAndReturns_thenHappyPath() {

        ConfigurationCreateRequest req = ConfigurationCreateRequest.builder()
                .name("feature.toggle")
                .application("payments")
                .environment("dev")
                .content("test content")
                .build();

        ConfigurationResponse resp = client.createConfiguration(req);

        assertNotNull(resp.getId());
        assertEquals("feature.toggle", resp.getName());
        assertEquals("payments", resp.getApplication());
        assertEquals("dev", resp.getEnvironment());
    }

    @Test
    @DisplayName("updateConfiguration() puts request then fetches by id")
    void updateConfigurationPutsThenGets_happyPath() {

        ConfigurationCreateRequest req = ConfigurationCreateRequest.builder()
                .name("app.conf")
                .application("svc")
                .environment("test")
                .content("c1")
                .build();

        ConfigurationResponse created = client.createConfiguration(req);

        ConfigurationUpdateRequest update = new ConfigurationUpdateRequest("name","application","env","content");
        ConfigurationResponse updated = client.updateConfiguration(created.getId(), update);

        assertEquals(created.getId(), updated.getId());
        assertEquals("name", updated.getName());
        assertEquals("application", updated.getApplication());
        assertEquals("env", updated.getEnvironment());
        assertEquals("content", updated.getContent());
    }

    @Test
    @DisplayName("patchConfiguration() sends PATCH and returns body")
    void patchConfigurationSendsPatchAndReturns() {

        ConfigurationCreateRequest req = ConfigurationCreateRequest.builder()
                .name("name")
                .application("application")
                .environment("dev")
                .content("content")
                .build();
        ConfigurationResponse created = client.createConfiguration(req);

        ConfigurationPatchRequest patch = new ConfigurationPatchRequest();
        patch.setContent("content updated");
        ConfigurationResponse patched = client.patchConfiguration(created.getId(), patch);

        assertEquals(created.getId(), patched.getId());
        assertEquals("content updated", patched.getContent());
    }

    @Test
    @DisplayName("getConfiguration() retrieves by id")
    void getConfigurationRetrievesById_thenHappyPath() {

        ConfigurationCreateRequest req = ConfigurationCreateRequest.builder()
                .name("name")
                .application("svc")
                .environment("qa")
                .content("c")
                .build();
        ConfigurationResponse created = client.createConfiguration(req);

        ConfigurationResponse got = client.getConfiguration(created.getId());
        assertEquals(created.getId(), got.getId());
        assertEquals("name", got.getName());
    }

    @Test
    @DisplayName("getConfigurations() builds query params and returns collection")
    void getConfigurationsBuildsQueryAndReturnsCollection_thenHappyPath() {

        client.createConfiguration(ConfigurationCreateRequest.builder()
                .name("a")
                .application("svc")
                .environment("dev")
                .content("c")
                .build());
        client.createConfiguration(ConfigurationCreateRequest.builder()
                .name("b")
                .application("svc")
                .environment("prod")
                .content("c")
                .build());
        client.createConfiguration(ConfigurationCreateRequest.builder()
                .name("a")
                .application("web")
                .environment("dev")
                .content("c")
                .build());

        ConfigurationCollectionResponse all = client.getConfigurations(null, null, null);
        assertNotNull(all);

        ConfigurationCollectionResponse byName = client.getConfigurations("a", null, null);
        assertNotNull(byName);

        ConfigurationCollectionResponse byAppEnv = client.getConfigurations(null, "svc", "prod");
        assertNotNull(byAppEnv);
    }

    @Test
    @DisplayName("deleteConfiguration() deletes by id")
    void deleteConfigurationDeletesById_thenHappyPath() {

        ConfigurationResponse created = client.createConfiguration(ConfigurationCreateRequest.builder()
                .name("d")
                .application("svc")
                .environment("test")
                .content("c")
                .build());

        client.deleteConfiguration(created.getId());

        try {
            ConfigurationResponse resp = client.getConfiguration(created.getId());
            if (resp != null) {
                assertNull(resp.getId());
            }
        } catch (HttpClientErrorException.NotFound ex) {
            assertEquals(404, ex.getStatusCode().value());
        }
    }
}
