package com.swyp.FinQ.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "password-reset")
public record PasswordResetProperties(
        Duration codeExpiration,
        Duration resendCooldown
) {
}
