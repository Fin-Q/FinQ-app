package com.swyp.FinQ.home.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "character-image")
public record CharacterImageProperties(String baseUrl) {

    public CharacterImageProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Character image base URL must be configured");
        }

        URI uri = URI.create(baseUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getUserInfo() != null
                || uri.getQuery() != null
                || uri.getFragment() != null) {
            throw new IllegalArgumentException(
                    "Character image base URL must be an HTTPS URL without credentials, query or fragment"
            );
        }

        baseUrl = baseUrl.replaceAll("/+$", "");
    }
}
