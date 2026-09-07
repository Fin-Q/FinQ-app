package com.swyp.FinQ.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail.ses")
public record SesMailProperties(
        String region,
        String fromAddress
) {
}
