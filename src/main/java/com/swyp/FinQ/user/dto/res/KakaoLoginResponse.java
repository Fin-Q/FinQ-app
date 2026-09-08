package com.swyp.FinQ.user.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swyp.FinQ.user.domain.OnboardingStatus;
import com.swyp.FinQ.user.service.KakaoLoginResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kakao 소셜 로그인 응답")
public record KakaoLoginResponse(
        @Schema(description = "FinQ 사용자 ID", example = "1")
        String userId,
        @Schema(description = "사용자 닉네임", example = "Minter")
        String nickname,
        @JsonProperty("isNewUser")
        @Schema(description = "신규 가입 사용자 여부", example = "true")
        boolean isNewUser,
        @Schema(description = "FinQ Access Token")
        String accessToken,
        @Schema(description = "FinQ Refresh Token")
        String refreshToken,
        @Schema(description = "인증 타입", example = "Bearer")
        String tokenType,
        @Schema(description = "Access Token 만료 시간(초)", example = "3600")
        long accessTokenExpiresIn,
        @Schema(description = "현재 온보딩 단계", example = "INTEREST_SECTION")
        OnboardingStatus onboardingStatus
) {

    public static KakaoLoginResponse from(KakaoLoginResult result) {
        return new KakaoLoginResponse(
                result.userId(),
                result.nickname(),
                result.isNewUser(),
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.accessTokenExpiresIn(),
                result.onboardingStatus()
        );
    }
}
