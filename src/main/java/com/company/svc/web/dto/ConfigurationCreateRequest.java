package com.company.svc.web.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigurationCreateRequest {

    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Application is required.")
    private String application;

    @NotBlank(message = "Environment is required.")
    private String environment;

    @NotBlank(message = "Content is required.")
    private String content;
}
