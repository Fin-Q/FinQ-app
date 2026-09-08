package com.swyp.FinQ.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "apple")
public record AppleProperties(
        String clientId,
        String teamId,
        String keyId,
        String privateKeyBase64,
        URI issuer,
        URI jwkSetUri,
        URI tokenUri,
        Duration clientSecretExpiration
) {
}
