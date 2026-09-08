package com.swyp.FinQ.user.service;

import com.swyp.FinQ.user.domain.OnboardingStatus;

public record KakaoLoginResult(
        String userId,
        String nickname,
        boolean isNewUser,
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        OnboardingStatus onboardingStatus
) {
}
