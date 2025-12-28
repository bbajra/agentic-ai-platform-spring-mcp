package com.example.aigateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    ToolAllowlistProperties.class,
    AiModelProperties.class,
    RunbookProperties.class
})
public class AppConfig {
}
