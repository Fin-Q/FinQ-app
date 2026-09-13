package com.swyp.FinQ.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "profile-image")
public record ProfileImageProperties(String baseUrl) {

    public ProfileImageProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Profile image base URL must be configured");
        }

        URI uri = URI.create(baseUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getUserInfo() != null
                || uri.getQuery() != null
                || uri.getFragment() != null) {
            throw new IllegalArgumentException(
                    "Profile image base URL must be an HTTPS URL without credentials, query or fragment"
            );
        }

        baseUrl = baseUrl.replaceAll("/+$", "");
    }
}
