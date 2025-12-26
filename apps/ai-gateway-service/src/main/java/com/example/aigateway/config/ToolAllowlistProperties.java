package com.example.aigateway.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tools")
public record ToolAllowlistProperties(List<String> allowlist) {
}
