package com.company.svc.web.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigurationUpdateRequest {

    private String name;
    private String application;
    private String environment;
    private String content;
}
