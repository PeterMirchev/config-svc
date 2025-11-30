package com.company.svc.web.dto;

import lombok.Data;

@Data
public class ConfigurationPatchRequest {

    private String name;

    private String application;

    private String environment;

    private String content;
}
