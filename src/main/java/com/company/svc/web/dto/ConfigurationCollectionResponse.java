package com.company.svc.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConfigurationCollectionResponse {

    private List<ConfigurationResponse> configurations;
}
