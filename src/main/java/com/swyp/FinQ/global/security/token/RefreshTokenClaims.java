package com.swyp.FinQ.global.security.token;

import java.time.Instant;

public record RefreshTokenClaims(
        Long userId,
        String sessionId,
        Instant expiresAt
) {
}
