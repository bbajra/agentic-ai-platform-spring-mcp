package com.example.aigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "runbooks")
public record RunbookProperties(String basePath, int maxResults, int snippetRadius) {
}
