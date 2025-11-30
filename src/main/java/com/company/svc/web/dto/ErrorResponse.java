package com.company.svc.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String message;
    private List<String> messages;
    private OffsetDateTime timestamp;
}
