package com.swyp.FinQ.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "password-reset")
public record PasswordResetProperties(
        Duration codeExpiration,
        Duration resendCooldown,
        Duration tokenExpiration,
        int maximumFailedAttempts
) {
    public PasswordResetProperties {
        if (maximumFailedAttempts < 1 || tokenExpiration == null
                || tokenExpiration.isZero() || tokenExpiration.isNegative()) {
            throw new IllegalArgumentException("Password reset token expiration and failure limit must be positive");
        }
    }
}
