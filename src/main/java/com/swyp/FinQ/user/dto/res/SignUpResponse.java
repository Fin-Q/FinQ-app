package com.swyp.FinQ.user.dto.res;

import com.swyp.FinQ.user.domain.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 회원가입 응답")
public record SignUpResponse(
        @Schema(description = "생성된 사용자 ID", example = "1")
        String userId,
        @Schema(description = "FinQ Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "FinQ Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,
        @Schema(description = "인증 타입", example = "Bearer")
        String tokenType,
        @Schema(description = "Access Token 만료 시간(초)", example = "3600")
        long accessTokenExpiresIn,
        @Schema(description = "회원가입 직후 온보딩 단계", example = "INTEREST_SELECTION",
                allowableValues = {"INTEREST_SELECTION", "CHARACTER_GUIDE", "COMPLETED"})
        OnboardingStatus onboardingStatus
) {
}
