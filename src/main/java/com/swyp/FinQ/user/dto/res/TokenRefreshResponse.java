package com.swyp.FinQ.user.dto.res;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn
) {
}
