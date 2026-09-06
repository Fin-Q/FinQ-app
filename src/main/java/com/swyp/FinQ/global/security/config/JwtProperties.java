package com.swyp.FinQ.global.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {
}
