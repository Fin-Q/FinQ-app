package com.swyp.FinQ.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답")
public record TokenRefreshResponse(
        @Schema(description = "새 FinQ Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "회전 발급된 새 FinQ Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,
        @Schema(description = "인증 타입", example = "Bearer")
        String tokenType,
        @Schema(description = "Access Token 만료 시간(초)", example = "3600")
        long accessTokenExpiresIn,
        @Schema(description = "Refresh Token 만료 시간(초)", example = "1209600")
        long refreshTokenExpiresIn
) {
}
