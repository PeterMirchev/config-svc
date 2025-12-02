package com.company.svc.web;

import com.company.svc.service.ConfigurationEventPublisher;
import com.company.svc.service.RedisCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConfigurationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigurationEventPublisher configurationEventPublisher;

    @MockBean
    private RedisCacheService redisCacheService;

    private String baseUrl() {
        return "/api/v1/configurations";
    }

    @Test
    @DisplayName("Create configuration then fetch by id")
    void createAndGetConfiguration() throws Exception {

        String payload = "{" +
                "\"name\":\"feature.toggle\"," +
                "\"application\":\"payments\"," +
                "\"environment\":\"dev\"," +
                "\"content\":\"{\\\"enabled\\\":true}\"" +
                "}";

        String location = mockMvc.perform(post(baseUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("feature.toggle")))
                .andExpect(jsonPath("$.application", is("payments")))
                .andExpect(jsonPath("$.environment", is("dev")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = location.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get(baseUrl() + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("feature.toggle")))
                .andExpect(jsonPath("$.application", is("payments")))
                .andExpect(jsonPath("$.environment", is("dev")));
    }

    @Test
    @DisplayName("List configurations returns collection")
    void listConfigurations() throws Exception {

        String payload = "{" +
                "\"name\":\"app.config\"," +
                "\"application\":\"portal\"," +
                "\"environment\":\"test\"," +
                "\"content\":\"{\\\"k\\\":\\\"v\\\"}\"" +
                "}";


        mockMvc.perform(post(baseUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(get(baseUrl()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.configurations", notNullValue()))
                .andExpect(jsonPath("$.configurations", isA(List.class)))
                .andExpect(jsonPath("$.configurations.size()", greaterThanOrEqualTo(1)));
    }
}
