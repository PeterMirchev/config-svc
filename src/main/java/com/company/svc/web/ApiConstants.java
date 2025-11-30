package com.company.svc.web;

import lombok.Data;

@Data
public class ApiConstants {

    @Data
    public static class Versions {

        public static final String V1 = "/api/v1";
    }

    @Data
    public static class EndpointPaths {

        public static final String CONFIGURATIONS = "/configurations";
    }

}
