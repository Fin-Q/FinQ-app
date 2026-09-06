package com.swyp.FinQ.global.security.token;

import java.time.Instant;

public record IssuedTokenPair(
        String accessToken,
        String refreshToken,
        String sessionId,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        Instant refreshTokenExpiresAt
) {
}
