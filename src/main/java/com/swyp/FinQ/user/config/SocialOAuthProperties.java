package com.swyp.FinQ.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "social-oauth")
public record SocialOAuthProperties(
        String tokenEncryptionKey
) {
}
