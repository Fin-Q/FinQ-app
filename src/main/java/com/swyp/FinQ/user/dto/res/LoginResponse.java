package com.swyp.FinQ.user.dto.res;

import com.swyp.FinQ.user.domain.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일반 로그인 응답")
public record LoginResponse(
        @Schema(description = "사용자 ID", example = "1")
        String userId,
        @Schema(description = "사용자 닉네임", example = "Minter")
        String nickname,
        @Schema(description = "FinQ Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "FinQ Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,
        @Schema(description = "인증 타입", example = "Bearer")
        String tokenType,
        @Schema(description = "Access Token 만료 시간(초)", example = "3600")
        long accessTokenExpiresIn,
        @Schema(description = "현재 온보딩 단계", example = "COMPLETED",
                allowableValues = {"INTEREST_SELECTION", "CHARACTER_GUIDE", "COMPLETED"})
        OnboardingStatus onboardingStatus
) {
}
