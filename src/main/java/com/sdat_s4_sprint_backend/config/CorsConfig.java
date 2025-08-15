package com.sdat_s4_sprint_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:}")
    private String allowedOriginsCsv;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String allowedMethodsCsv;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeadersCsv;

    @Value("${app.cors.exposed-headers:Location}")
    private String exposedHeadersCsv;

    @Value("${app.cors.max-age-seconds:3600}")
    private long maxAgeSeconds;

    private static String[] splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return new String[0];
        return Arrays.stream(csv.split("\\s*,\\s*"))
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = splitCsv(allowedOriginsCsv);
        String[] methods = splitCsv(allowedMethodsCsv);
        String[] allowedHeaders = splitCsv(allowedHeadersCsv);
        String[] exposedHeaders = splitCsv(exposedHeadersCsv);

        registry.addMapping("/**")
                .allowedOrigins(origins)          // exact origins only (required when credentials=true)
                .allowedMethods(methods)
                .allowedHeaders(allowedHeaders.length == 0 ? new String[] {"*"} : allowedHeaders)
                .exposedHeaders(exposedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(maxAgeSeconds);
    }
}
